"""High-level game actions: open menu, navigate to quest panel, click a quest row,
claim rewards, use HP potions, etc. All coordinates are canvas fractions (0..1).

These actions are the building blocks for QuestExecutor in quests.py.
"""
from __future__ import annotations

import time
import unicodedata
from typing import Optional

import cv2
import numpy as np

from src.capture import GameControl, Frame
from src.config import load_config
from src.vision.ocr import QuestEntry, parse_quest_list_from_image, detect_status_by_color

# --- Menu shortcuts discovered from in-game MENU panel ---
# Pressing number keys opens sub-panels:
#  1=Thế giới  2=Bán hàng  3=Nhân vật  4=Phái hệ  5=Báu vật
#  6=Nhiệm vụ  7=Bạn bè    8=Thiết lập  0=Đóng
# ESC or 0 closes most panels.

MENU_KEY_QUESTS = "6"
MENU_KEY_CLOSE = "0"


def open_quest_panel(gc: GameControl) -> None:
    """Open the quest list panel. First closes any open panel (press 0),
    then presses 6 (Danh sách nhiệm vụ)."""
    gc.press(MENU_KEY_CLOSE)
    time.sleep(0.4)
    gc.press(MENU_KEY_QUESTS)
    time.sleep(1.0)


def close_any_panel(gc: GameControl) -> None:
    """Close the current overlay panel."""
    gc.press(MENU_KEY_CLOSE)
    time.sleep(0.5)


def read_quest_list(gc: GameControl) -> list[QuestEntry]:
    """Capture current frame and parse the quest list."""
    frame = gc.capture()
    return parse_quest_list_from_image(frame.image)


def click_quest_row(gc: GameControl, row_index: int, cfg: Optional[dict] = None) -> None:
    """Click the (row_index)-th visible quest row in the quest panel.

    row_index is 0-based (top row = 0). The coordinate is computed from
    calibrated UI fractions in config.yaml.
    """
    cfg = cfg or load_config()
    ui = cfg.get("ui", {})

    # Quest list region: start below the header, each row has fixed height
    row_start_x = ui.get("quest_row_start_x", 0.05)
    row_start_y = ui.get("quest_row_start_y", 0.10)
    row_height = ui.get("quest_row_height", 0.04)
    row_click_x = ui.get("quest_row_click_x", 0.25)  # center-ish X to click

    y_frac = row_start_y + row_height * row_index + row_height * 0.5
    gc.click(row_click_x, y_frac)
    time.sleep(0.8)


def click_quest_detail_claim(gc: GameControl, cfg: Optional[dict] = None) -> None:
    """Click the 'Nhận thưởng' (claim reward) button in the quest detail panel.
    After clicking a quest row, a detail panel appears with quest info and
    a claim button at the bottom-right area."""
    cfg = cfg or load_config()
    ui = cfg.get("ui", {})
    claim_x = ui.get("claim_button_x", 0.85)
    claim_y = ui.get("claim_button_y", 0.85)
    gc.click(claim_x, claim_y)
    time.sleep(0.8)


def close_quest_detail(gc: GameControl) -> None:
    """Close the quest detail panel (X button or press 0)."""
    gc.press(MENU_KEY_CLOSE)
    time.sleep(0.5)


def use_hp_potion(gc: GameControl, cfg: Optional[dict] = None) -> bool:
    """Attempt to click the HP potion slot. Returns True if clicked."""
    cfg = cfg or load_config()
    ui = cfg.get("ui", {})
    potion_x = ui.get("hp_potion_x", 0.5)
    potion_y = ui.get("hp_potion_y", 0.92)
    gc.click(potion_x, potion_y)
    time.sleep(0.5)
    return True


def wait_for_status_change(gc: GameControl, timeout: float = 15.0) -> bool:
    """Wait until at least one quest row changes from 'Chưa xong' to 'Hoàn Thành'.
    Captures frames periodically and compares quest lists."""
    deadline = time.time() + timeout
    baseline = read_quest_list(gc)
    baseline_statuses = {q.index: q.status for q in baseline}

    while time.time() < deadline:
        time.sleep(2.0)
        current = read_quest_list(gc)
        for q in current:
            if q.index in baseline_statuses:
                old = baseline_statuses[q.index]
                if old == "Chưa xong" and q.status == "Hoàn Thành":
                    return True
        # Update baseline
        baseline_statuses.update({q.index: q.status for q in current})

    return False


def detect_combat_state(gc: GameControl) -> bool:
    """Heuristic: if the frame has high variance in the bottom-center region
    (where combat UI appears) and contains red/orange pixels (HP bars / effects),
    we assume combat is active."""
    frame = gc.capture()
    img = frame.image
    h, w = img.shape[:2]

    # Combat UI is typically in the bottom 20%
    bottom = img[int(h * 0.8):, :]
    if bottom.size == 0:
        return False

    # Check for combat indicators: look for red HP bar colors
    hsv = cv2.cvtColor(bottom, cv2.COLOR_BGR2HSV)
    # Red is around 0° or 180° hue
    lower_red = np.array([0, 100, 100])
    upper_red = np.array([15, 255, 255])
    mask = cv2.inRange(hsv, lower_red, upper_red)
    red_pct = float(mask > 0) / mask.size
    return red_pct > 0.02


def detect_dialog(gc: GameControl) -> bool:
    """Detect if there's a dialog/confirmation overlay (modal in center).
    Heuristic: check for a dark semi-transparent overlay in center region."""
    frame = gc.capture()
    img = frame.image
    h, w = img.shape[:2]

    # Center 30x30% region
    cy, cx = h // 2, w // 2
    region = img[cy - int(h * 0.15):cy + int(h * 0.15),
                 cx - int(w * 0.15):cx + int(w * 0.15)]

    if region.size == 0:
        return False

    # Dialog boxes often have a distinct border color or semi-transparent bg
    gray = cv2.cvtColor(region, cv2.COLOR_BGR2GRAY)
    # Look for edges/borders (high frequency content = dialog box outline)
    edges = cv2.Canny(gray, 50, 150)
    edge_pct = float(edges > 0) / edges.size
    return edge_pct > 0.1


def _point(value, default: tuple[float, float]) -> tuple[float, float]:
    """Read a canvas point from either a dict or a two-item list."""
    if isinstance(value, dict):
        return float(value.get("x", default[0])), float(value.get("y", default[1]))
    if isinstance(value, (list, tuple)) and len(value) == 2:
        return float(value[0]), float(value[1])
    return default


class GameActions:
    """Canvas-relative actions used by the automatic quest runner."""

    def __init__(self, gc: GameControl, cfg: Optional[dict] = None):
        self.gc = gc
        self.cfg = cfg or load_config()
        self.ui = self.cfg.get("ui", {})
        self.auto = self.cfg.get("quest_actions", {})

    def open_quest_panel(self) -> None:
        open_quest_panel(self.gc)

    def close_any_panel(self) -> None:
        close_any_panel(self.gc)

    def click_quest_row(self, row_index: int) -> None:
        click_quest_row(self.gc, row_index, self.cfg)

    def claim_reward(self) -> None:
        click_quest_detail_claim(self.gc, self.cfg)

    def click_back_button(self) -> None:
        close_any_panel(self.gc)

    def click_character(self) -> None:
        self.gc.click(*_point(self.ui.get("character_select"), (0.50, 0.55)))

    def activate_tracked_quest(self) -> None:
        """Click the tracked quest in the top-left HUD to start auto-path."""
        self.gc.click(*_point(self.ui.get("active_quest"), (0.14, 0.23)))
        time.sleep(float(self.auto.get("path_start_delay_seconds", 2.0)))

    def return_to_quest_giver(self) -> bool:
        """Click a completed tracked quest and wait for its automatic return path."""
        self.activate_tracked_quest()
        arrived = self.wait_for_arrival()
        time.sleep(float(self.auto.get("return_settle_seconds", 2.0)))
        return arrived

    def click_dialog_action(self) -> None:
        """Click the green Nhận/Hoàn thành button in an NPC quest dialog."""
        self.gc.click(*_point(self.ui.get("quest_dialog_action"), (0.86, 0.82)))
        time.sleep(float(self.auto.get("dialog_settle_seconds", 1.5)))

    def click_available_quest_npc(self) -> bool:
        """Find a yellow exclamation mark and click the NPC directly below it."""
        marker = self._find_yellow_exclamation(self.gc.capture().image)
        if marker is None:
            fallback = self.ui.get("available_quest_npc_fallback")
            if fallback is None:
                return False
            marker = _point(fallback, (0.55, 0.35))
            npc_y = marker[1]
        else:
            npc_y = min(0.90, marker[1] + float(self.auto.get("quest_marker_npc_offset_y", 0.09)))
        self.gc.click(marker[0], npc_y)
        time.sleep(float(self.auto.get("npc_dialog_delay_seconds", 1.0)))
        return True

    def _find_yellow_exclamation(self, image: np.ndarray) -> Optional[tuple[float, float]]:
        """Return the most likely yellow ! center as canvas fractions."""
        h, w = image.shape[:2]
        hsv = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
        lower = np.array(self.auto.get("quest_marker_hsv_lower", [18, 120, 150]), dtype=np.uint8)
        upper = np.array(self.auto.get("quest_marker_hsv_upper", [42, 255, 255]), dtype=np.uint8)
        mask = cv2.inRange(hsv, lower, upper)
        mask[:int(0.14 * h), :] = 0
        mask[int(0.78 * h):, :] = 0
        mask[:, :int(0.18 * w)] = 0
        mask[:, int(0.82 * w):] = 0
        mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, np.ones((3, 3), np.uint8))
        contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        candidates = []
        for contour in contours:
            x, y, cw, ch = cv2.boundingRect(contour)
            area = cv2.contourArea(contour)
            if 80 <= area <= 3000 and ch >= 18 and ch / max(cw, 1) >= 1.15:
                fx = (x + cw / 2) / w
                fy = (y + ch / 2) / h
                distance = (fx - 0.5) ** 2 + (fy - 0.42) ** 2
                candidates.append((distance, fx, fy))

        if not candidates:
            return None
        _, fx, fy = min(candidates)
        return fx, fy

    def accept_available_quest(self) -> bool:
        """Open the nearest ! quest NPC and press the green Nhận button."""
        if not self.click_available_quest_npc():
            return False
        self.click_dialog_action()
        return True

    def open_map(self) -> None:
        self.gc.click(*_point(self.ui.get("map_button"), (0.85, 0.07)))
        time.sleep(float(self.auto.get("map_open_delay_seconds", 1.0)))

    def accept_quest_from_map(self) -> bool:
        """Use one map ! marker, auto-path to its NPC, and press Nhận."""
        self.open_map()
        marker = self._find_yellow_exclamation(self.gc.capture().image)
        if marker is None:
            self.close_any_panel()
            return False
        self.gc.click(*marker)
        time.sleep(float(self.auto.get("path_start_delay_seconds", 2.0)))
        self.wait_for_arrival()
        time.sleep(float(self.auto.get("return_settle_seconds", 2.0)))
        if not self.click_available_quest_npc():
            return False
        self.click_dialog_action()
        return True

    def accept_all_map_quests(self) -> int:
        """Collect several available quests before starting the action phase."""
        accepted = 0
        limit = int(self.auto.get("max_map_quests_to_accept", 10))
        self.close_any_panel()
        while accepted < limit and self.accept_quest_from_map():
            accepted += 1
        return accepted

    def wait_for_arrival(self) -> bool:
        """Detect the end of auto-path when the central world view stabilizes."""
        timeout = float(self.auto.get("arrival_timeout_seconds", 20.0))
        interval = float(self.auto.get("arrival_poll_seconds", 0.75))
        threshold = float(self.auto.get("arrival_stable_threshold", 0.035))
        required = int(self.auto.get("arrival_stable_samples", 3))
        roi = self.auto.get("movement_roi", [0.24, 0.16, 0.50, 0.62])
        deadline = time.time() + timeout
        previous = None
        stable = 0
        while time.time() < deadline:
            image = self.gc.capture().image
            h, w = image.shape[:2]
            x, y, rw, rh = [float(v) for v in roi]
            crop = image[int(y * h):int((y + rh) * h), int(x * w):int((x + rw) * w)]
            if crop.size == 0:
                return False
            gray = cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY)
            gray = cv2.resize(gray, (160, 90), interpolation=cv2.INTER_AREA)
            gray = cv2.GaussianBlur(gray, (5, 5), 0)
            if previous is not None:
                change = float(cv2.absdiff(gray, previous).mean()) / 255.0
                stable = stable + 1 if change <= threshold else 0
                if stable >= required:
                    return True
            previous = gray
            time.sleep(interval)
        return False

    def quest_kind(self, title: str) -> str:
        normalized = unicodedata.normalize("NFD", title.casefold())
        plain = "".join(ch for ch in normalized if unicodedata.category(ch) != "Mn")
        keywords = self.auto.get(
            "collection_keywords", ["thu thap", "nhat", "hai", "luom", "dao", "lay", "tim"]
        )
        return "collection" if any(str(word).casefold() in plain for word in keywords) else "combat"

    def perform_action_round(self, title: str) -> None:
        """Target nearby monsters/items and use skills for one action round."""
        delay = float(self.auto.get("click_interval_seconds", 0.35))
        if self.quest_kind(title) == "collection":
            targets = self.auto.get(
                "collection_target_points", [[0.48, 0.50], [0.42, 0.57], [0.55, 0.57], [0.50, 0.65]]
            )
            action = _point(self.ui.get("interact_skill"), (0.86, 0.82))
            for target in targets:
                self.gc.click(*_point(target, (0.50, 0.55)))
                time.sleep(delay)
                self.gc.click(*action)
                time.sleep(delay)
            return

        targets = self.auto.get(
            "combat_target_points", [[0.48, 0.50], [0.42, 0.58], [0.54, 0.60], [0.47, 0.68]]
        )
        skills = self.auto.get(
            "combat_skill_points", [[0.86, 0.54], [0.80, 0.66], [0.79, 0.81], [0.86, 0.82], [0.94, 0.80]]
        )
        for index, target in enumerate(targets):
            self.gc.click(*_point(target, (0.50, 0.55)))
            time.sleep(delay)
            self.gc.click(*_point(skills[index % len(skills)], (0.94, 0.80)))
            time.sleep(delay)
