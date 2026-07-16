"""Main bot module: state machine + quest automation loop.

This is the entry point for the auto-quest bot. It orchestrates:
1. Game control (open_attach or launch Brave)
2. State detection (login, quest panel, combat, etc.)
3. Quest reading (OCR/LLM)
4. Quest execution (click, wait, claim reward)
5. Full loop: HOME → open panel → read quests → execute pending → claim → repeat

Usage:
    # Dry-run (no clicks, just log what would happen)
    python -m src.bot --dry-run

    # Run for real
    python -m src.bot

    # Run a single quest
    python -m src.bot --single --quest-index 0
"""
from __future__ import annotations

import argparse
import logging
import sys
import time
from dataclasses import dataclass, field
from datetime import datetime
from enum import Enum
from pathlib import Path
from typing import Optional

import cv2
import numpy as np

from src.capture import GameControl, open_control
from src.config import load_config
from src.game.actions import GameActions
from src.game.quests import Quest, QuestStatus, QuestTarget, parse_quest_tag, parse_quest_target
from src.game.screen_state import ScreenDetector, ScreenState
from src.vision.ocr import QuestItem, detect_status_by_color, read_quest_panel

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.FileHandler(Path("logs/bot.log"), encoding="utf-8"),
        logging.StreamHandler(sys.stdout),
    ],
)
logger = logging.getLogger(__name__)


class BotState(Enum):
    """Bot state machine states."""
    IDLE = "idle"
    LOGGED_IN = "logged_in"
    IN_GAME = "in_game"
    QUEST_PANEL_OPEN = "quest_panel_open"
    EXECUTING_QUEST = "executing_quest"
    CLAIMING_REWARD = "claiming_reward"
    ERROR = "error"
    DONE = "done"


@dataclass
class BotConfig:
    """Runtime configuration for the bot."""
    dry_run: bool = True
    max_quests_per_session: int = 20
    quest_timeout_seconds: int = 90
    max_retries: int = 3
    skip_completed: bool = True
    single_quest_index: Optional[int] = None


class QuestBot:
    """Main auto-quest bot with state machine."""

    def __init__(self, cfg: dict):
        self.cfg = cfg
        self.bot_cfg = BotConfig(
            dry_run=cfg["runtime"]["dry_run"],
            max_quests_per_session=cfg["quests"]["max_attempts_per_quest"],
            quest_timeout_seconds=cfg["quests"]["quest_timeout_seconds"],
            max_retries=cfg["quests"]["max_attempts_per_quest"],
        )
        self.gc: Optional[GameControl] = None
        self.actions: Optional[GameActions] = None
        self.detector = ScreenDetector()
        self.state = BotState.IDLE
        self.quest_log: list[dict] = []

    def start(self) -> None:
        """Launch game control and start bot loop."""
        logger.info("=" * 60)
        logger.info("Sanguo Auto-Quest Bot starting")
        logger.info(f"Dry run: {self.bot_cfg.dry_run}")
        logger.info("=" * 60)

        # Open game control
        self.gc = open_control(self.cfg)
        self.gc.set_dry_run(self.bot_cfg.dry_run)
        self.actions = GameActions(self.gc, self.cfg)

        # Main loop
        try:
            self._main_loop()
        except KeyboardInterrupt:
            logger.info("Interrupted by user")
        except Exception as e:
            logger.error(f"Bot error: {e}", exc_info=True)
            self._save_debug_screenshot("bot_error")
            raise
        finally:
            self._save_debug_screenshot("shutdown")
            if self.gc:
                self.gc.close()

    def _main_loop(self) -> None:
        """Main bot state machine loop."""
        self.state = BotState.IDLE
        completed_count = 0

        while completed_count < self.bot_cfg.max_quests_per_session:
            prev_state = self.state
            self.state = self._state_machine_step()

            # Log state transition
            if prev_state != self.state:
                logger.info(f"State: {prev_state.value} -> {self.state.value}")

            # Check for terminal states
            if self.state == BotState.DONE:
                break
            if self.state == BotState.ERROR:
                logger.warning("Bot entered error state, retrying...")
                time.sleep(2)
                self.state = BotState.IDLE

    def _state_machine_step(self) -> BotState:
        """Execute one step of the state machine."""
        if self.gc is None:
            return BotState.ERROR

        frame = self.gc.capture()
        screen = self.detector.detect(frame)
        logger.debug(f"Screen state: {screen.value}")

        # Route to appropriate handler based on state and screen
        if self.state == BotState.IDLE:
            if screen == ScreenState.LOGIN:
                self.state = BotState.LOGGED_IN
                return self.state
            elif screen == ScreenState.GAME_WORLD:
                self.state = BotState.IN_GAME
                return self.state
            else:
                # Still in some transition state
                return BotState.IDLE

        elif self.state == BotState.LOGGED_IN:
            # Click character to enter game
            self.actions.click_character()
            time.sleep(5)
            self.state = BotState.IN_GAME
            return self.state

        elif self.state == BotState.IN_GAME:
            # Open quest panel
            self.actions.open_quest_panel()
            time.sleep(1)
            self.state = BotState.QUEST_PANEL_OPEN
            return self.state

        elif self.state == BotState.QUEST_PANEL_OPEN:
            # Read quests and find next pending one
            quests = self._read_and_parse_quests(frame)
            logger.info(f"Quest panel: {sum(1 for q in quests if q.status == QuestStatus.COMPLETED)}/{len(quests)} completed")
            for q in quests:
                icon = "✓" if q.status == QuestStatus.COMPLETED else "○"
                logger.info(f"  [{icon}] [{q.tag}] {q.title}")

            pending = [q for q in quests if q.status == QuestStatus.PENDING]

            if not pending:
                logger.info("All quests completed. Done!")
                self.state = BotState.DONE
                return self.state

            # Find the first pending quest (by index order)
            first_pending = min(pending, key=lambda q: q.index)
            logger.info(f"Next quest: [{first_pending.tag}] {first_pending.title} (index={first_pending.index})")

            if self.bot_cfg.single_quest_index is not None:
                if first_pending.index != self.bot_cfg.single_quest_index:
                    logger.info("Skipping non-matching quest index")
                    return BotState.QUEST_PANEL_OPEN
                self.state = BotState.EXECUTING_QUEST
                self._execute_single_quest(first_pending)
                return self.state

            # Click and execute the quest
            self._click_and_execute_quest(first_pending)
            self.state = BotState.EXECUTING_QUEST
            return self.state

        elif self.state == BotState.EXECUTING_QUEST:
            # Wait for quest to complete (game auto-paths, auto-fights)
            if self._wait_for_quest_completion():
                self.state = BotState.CLAIMING_REWARD
                return self.state
            return BotState.EXECUTING_QUEST

        elif self.state == BotState.CLAIMING_REWARD:
            # Click claim reward if available
            self._claim_reward()
            # Close panel and go back
            self.actions.click_back_button()
            time.sleep(0.5)
            self.state = BotState.QUEST_PANEL_OPEN
            completed_count += 1
            logger.info(f"Quest {completed_count} completed")
            return self.state

        else:
            logger.warning(f"Unknown state: {self.state}")
            return self.state

    def _read_and_parse_quests(self, frame: np.ndarray) -> list[Quest]:
        """Read quest panel from screen and parse into structured Quest objects."""
        # Use vision/LLM to read the panel
        quest_items = read_quest_panel(frame, self.cfg)

        quests = []
        for item in quest_items:
            if item.status == "unknown":
                continue
            tag, title = parse_quest_tag(item.title)
            quests.append(Quest(
                index=item.index,
                tag=tag or item.tag,
                title=title or item.title,
                status=QuestStatus(item.status),
                raw_text=f"[{item.tag}] {item.title}",
            ))
        return quests

    def _click_and_execute_quest(self, quest: Quest) -> None:
        """Click on a quest and wait for it to start executing."""
        logger.info(f"Clicking quest: [{quest.tag}] {quest.title}")
        self.actions.click_quest_row(quest.index)
        time.sleep(1)
        # Save screenshot of quest detail
        frame = self.gc.capture()
        self._save_debug_screenshot(f"quest_detail_{quest.index}")

    def _wait_for_quest_completion(self) -> bool:
        """Wait for a quest to complete. Polls screen state."""
        logger.info("Waiting for quest to complete...")
        start_time = time.time()
        prev_completed_count = self._count_completed_quests()
        prev_state = self.state

        while time.time() - start_time < self.bot_cfg.quest_timeout_seconds:
            frame = self.gc.capture()
            # Check if we're still on the quest panel
            screen = self.detector.detect(frame)
            if screen == ScreenState.GAME_WORLD and self.state == self._prev_state:
                # Game returned to world view - quest auto-paths
                logger.info("Game returned to world view, quest likely executing...")

            # Re-read quests to check for status change
            quests = self._read_and_parse_quests(frame)
            completed_count = sum(1 for q in quests if q.status == QuestStatus.COMPLETED)

            if completed_count > prev_completed_count:
                logger.info(f"Quest completed! {prev_completed_count} -> {completed_count} completed")
                return True

            time.sleep(2)

        logger.warning(f"Quest timed out after {self.bot_cfg.quest_timeout_seconds}s")
        return False

    def _count_completed_quests(self) -> int:
        """Count how many quests are currently completed."""
        frame = self.gc.capture()
        quests = self._read_and_parse_quests(frame)
        return sum(1 for q in quests if q.status == QuestStatus.COMPLETED)

    def _claim_reward(self) -> None:
        """Click to claim quest reward."""
        if self.bot_cfg.dry_run:
            logger.info("[DRY RUN] Would click claim reward")
            return
        logger.info("Claiming reward...")
        self.actions.claim_reward()

    def _execute_single_quest(self, quest: Quest) -> None:
        """Execute a single quest and return (for --single mode)."""
        logger.info(f"Executing single quest: [{quest.tag}] {quest.title}")
        self._click_and_execute_quest(quest)
        if self._wait_for_quest_completion():
            self._claim_reward()

    def _save_debug_screenshot(self, name: str) -> None:
        """Save a screenshot for debugging."""
        if self.gc is None:
            return
        try:
            frame = self.gc.capture()
            log_dir = Path(self.cfg["_resolved_log_dir"])
            path = log_dir / f"{name}.png"
            cv2.imwrite(str(path), frame.image)
            logger.debug(f"Saved debug screenshot: {path}")
        except Exception as e:
            logger.warning(f"Failed to save screenshot: {e}")


def main() -> None:
    parser = argparse.ArgumentParser(description="Sanguo Auto-Quest Bot")
    parser.add_argument("--dry-run", action="store_true", help="Log actions without clicking")
    parser.add_argument("--single", action="store_true", help="Execute a single quest")
    parser.add_argument("--quest-index", type=int, default=None, help="Index of quest to execute")
    args = parser.parse_args()

    cfg = load_config()
    if args.dry_run:
        cfg["runtime"]["dry_run"] = True
    if args.single and args.quest_index is not None:
        cfg["quests"]["single_quest_index"] = args.quest_index

    bot = QuestBot(cfg)
    bot.start()


if __name__ == "__main__":
    main()