"""High-level game actions: open menu, navigate to quest panel, click a quest row,
claim rewards, use HP potions, etc. All coordinates are canvas fractions (0..1).

These actions are the building blocks for QuestExecutor in quests.py.
"""
from __future__ import annotations

import time
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