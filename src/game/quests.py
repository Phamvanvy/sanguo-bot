"""Quest model, parser, and handlers.

Provides:
- QuestState: dataclass wrapping one quest row
- QuestReader: parse full quest list from canvas screenshot via LLM vision
- QuestExecutor: execute a single quest (click → wait combat → claim)
"""
from __future__ import annotations

import time
from dataclasses import dataclass, field
from typing import Optional

import cv2
import numpy as np

from src.capture import GameControl, Frame
from src.config import load_config
from src.game.actions import GameActions
from src.vision.ocr import QuestEntry, parse_quest_list_from_image

import re


# --- Quest row regions (fractions of canvas) calibrated from quest_panel2.png ---
# Each quest row is roughly at y=0.08 + index*0.07, width ~0.65, height ~0.06
QUEST_ROW_Y_START = 0.08
QUEST_ROW_HEIGHT = 0.065
QUEST_ROW_X = 0.1
QUEST_ROW_WIDTH = 0.55
QUEST_STATUS_X = 0.72
QUEST_STATUS_W = 0.12


@dataclass
class QuestState:
    """Full state of one quest row, including click coordinates."""
    index: int
    tag: str
    title: str
    status: str  # "Chưa xong" or "Hoàn Thành"
    row_position: int  # 0-based position in the visible panel list
    click_coord: tuple[float, float] = (0.35, 0.0)  # filled by QuestReader
    completed: bool = False

    @property
    def is_done(self) -> bool:
        return self.status == "Hoàn Thành" or self.completed


@dataclass
class QuestPanel:
    """Parsed state of the entire quest panel."""
    quests: list[QuestState] = field(default_factory=list)
    total_completed: int = 0
    total_count: int = 0

    @property
    def incomplete(self) -> list[QuestState]:
        return [q for q in self.quests if not q.is_done]

    @property
    def completed_count(self) -> int:
        return sum(1 for q in self.quests if q.is_done)


# --- Backward-compat aliases for bot.py ---
Quest = QuestState  # alias


class QuestStatus:
    """Enum-like constants for bot.py compatibility."""
    INCOMPLETE = "Chưa xong"
    COMPLETED = "Hoàn Thành"


@dataclass
class QuestTarget:
    """Target parsed from quest detail for bot.py compatibility."""
    npc_name: str = ""
    coord_text: str = ""
    coord_x: Optional[float] = None
    coord_y: Optional[float] = None


def parse_quest_tag(title: str) -> str:
    """Extract tag from quest title like '[Chú] Đột tổ ong' → 'Chú'."""
    m = re.search(r'\[([^\]]+)\]\s+.+', title)
    return m.group(1) if m else ""


def parse_quest_target(title: str) -> QuestTarget:
    """Extract target info from quest title (coord/NPC mention)."""
    target = QuestTarget()
    target.npc_name = title.split(' [')[0] if '[' in title else title
    return target


class QuestReader:
    """Parse quest panel from a canvas screenshot."""

    def __init__(self, cfg: Optional[dict] = None):
        self.cfg = cfg or load_config()

    def read_panel(self, frame: Frame) -> QuestPanel:
        """Parse all quest rows from a canvas frame.

        Uses LLM vision (primary) or OCR (fallback) to extract quest data,
        then assigns click coordinates based on row position.
        """
        entries = parse_quest_list_from_image(frame.image, self.cfg)
        quests = []
        for i, e in enumerate(entries):
            # Calculate click y: row start + row_index * row_height + row_height/2
            click_y = QUEST_ROW_Y_START + i * QUEST_ROW_HEIGHT + QUEST_ROW_HEIGHT / 2
            click_x = QUEST_ROW_X + QUEST_ROW_WIDTH / 2
            quests.append(QuestState(
                index=e.index,
                tag=e.tag,
                title=e.title,
                status=e.status,
                row_position=i,
                click_coord=(click_x, click_y),
            ))

        return QuestPanel(
            quests=quests,
            total_completed=sum(1 for q in quests if q.is_done),
            total_count=len(quests),
        )


class QuestExecutor:
    """Execute quests one by one using GameControl."""

    def __init__(self, control: GameControl, cfg: Optional[dict] = None):
        self.gc = control
        self.cfg = cfg or load_config()
        self.reader = QuestReader(self.cfg)
        self.actions = GameActions(control, self.cfg)

    def open_quest_panel(self) -> None:
        """Open the quest management panel via menu shortcut key '6'."""
        self.gc.press("6")
        time.sleep(1.5)

    def close_panel(self) -> None:
        """Close current panel with Escape."""
        self.gc.press("escape")
        time.sleep(0.5)

    def execute_quest(self, quest: QuestState) -> bool:
        """Track a quest, auto-path to it, fight/collect, and check completion."""
        if quest.is_done:
            print(f"  [executor] SKIP [{quest.index}] [{quest.tag}] {quest.title} (already done)")
            return True

        print(f"  [executor] START [{quest.index}] [{quest.tag}] {quest.title}")
        self.actions.click_quest_row(quest.row_position)
        self.actions.close_any_panel()
        self.actions.activate_tracked_quest()
        arrived = self.actions.wait_for_arrival()
        print(f"  [executor] ARRIVAL {'detected' if arrived else 'timeout; continuing'}")

        timeout = self.cfg.get("quests", {}).get("quest_timeout_seconds", 90)
        start = time.time()
        while time.time() - start < timeout:
            self.actions.perform_action_round(quest.title)
            self._check_hp_and_heal()
            time.sleep(float(self.cfg.get("quests", {}).get("poll_interval_seconds", 1.0)))

            self.actions.open_quest_panel()
            panel = self.reader.read_panel(self.gc.capture())
            for current in panel.quests:
                same_quest = current.index == quest.index and current.tag == quest.tag
                if same_quest and current.is_done:
                    self.actions.close_any_panel()
                    print(f"  [executor] OBJECTIVE DONE [{quest.index}] {quest.title}; returning to NPC")
                    returned = self.actions.return_to_quest_giver()
                    print(f"  [executor] RETURN {'arrived' if returned else 'timeout; pressing complete'}")
                    self.actions.click_dialog_action()
                    print(f"  [executor] COMPLETED [{quest.index}] {quest.title}")
                    if self.cfg.get("quest_actions", {}).get("auto_accept_nearby", True):
                        accepted = self.actions.accept_all_map_quests()
                        print(f"  [executor] NEW QUESTS accepted={accepted}")
                    return True

            self.actions.close_any_panel()
            self.actions.activate_tracked_quest()

        print(f"  [executor] TIMEOUT [{quest.index}] {quest.title} after {timeout}s")
        return False

    def _check_hp_and_heal(self) -> None:
        """Check HP bar color; if low, click HP potion slot."""
        # Placeholder: will be implemented in M3 with actual HP bar detection
        pass

    def _claim_reward(self, quest: QuestState) -> None:
        """Click the claim/reward button for a completed quest."""
        self.actions.click_quest_row(quest.row_position)
        self.actions.claim_reward()

    def run_all_incomplete(self, max_quests: int = 0) -> int:
        """Run all incomplete quests in the current panel.

        Args:
            max_quests: max quests to attempt (0 = unlimited).

        Returns:
            Number of quests completed this session.
        """
        completed = 0
        attempts: dict[tuple[int, str, str], int] = {}
        max_attempts = int(self.cfg.get("quests", {}).get("max_attempts_per_quest", 3))
        while not max_quests or completed < max_quests:
            # Refresh every pass so a newly accepted NPC quest is picked up too.
            self.actions.open_quest_panel()
            panel = self.reader.read_panel(self.gc.capture())
            eligible = []
            for quest in panel.incomplete:
                key = (quest.index, quest.tag, quest.title)
                if attempts.get(key, 0) < max_attempts:
                    eligible.append(quest)

            if not eligible:
                print(f"[executor] No runnable quests ({panel.completed_count}/{panel.total_count} done)")
                self.actions.close_any_panel()
                break

            quest = eligible[0]
            key = (quest.index, quest.tag, quest.title)
            if self.execute_quest(quest):
                completed += 1
                attempts.pop(key, None)
            else:
                attempts[key] = attempts.get(key, 0) + 1
                self.actions.close_any_panel()
            time.sleep(1.0)

        print(f"[executor] Session done: {completed} quests completed")
        return completed


# --- Quick test ---
if __name__ == "__main__":
    # Test: capture current panel and print quest state
    from src.capture import open_control
    gc = open_control()

    # Make sure quest panel is open (press 6)
    gc.press("6")
    time.sleep(1.5)

    frame = gc.capture()
    reader = QuestReader()
    panel = reader.read_panel(frame)

    print(f"\nQuest Panel: {panel.completed_count}/{panel.total_count} completed")
    for q in panel.quests:
        mark = "✅" if q.is_done else "❌"
        print(f"  {mark} [{q.index}] [{q.tag}] {q.title} → {q.status}")
