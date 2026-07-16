"""Quick script to capture template images from the game screen.

Run after logging into the quest panel. Takes screenshots and saves
template crops for UI elements we'll template-match later.
"""
from __future__ import annotations

import sys
import pathlib
import cv2

# Add project root to sys.path so we can import src
sys.path.insert(0, str(pathlib.Path(__file__).parent.parent))

from src.capture import open_control


def capture_quest_panel_templates() -> None:
    gc = open_control()
    frame = gc.capture()
    h, w = frame.height, frame.width

    # Quest panel header "Nhiệm vụ quản lý"
    header_y0 = int(0.05 * h)
    header_y1 = int(0.12 * h)
    header_x0 = int(0.2 * w)
    header_x1 = int(0.8 * w)
    header = frame.image[header_y0:header_y1, header_x0:header_x1]
    cv2.imwrite("templates/quest_panel_header.png", header)
    print(f"Saved quest_panel_header: {header.shape}")

    # Quest row area (first row)
    row_y0 = int(0.35 * h)
    row_y1 = int(0.40 * h)
    row_x0 = int(0.05 * w)
    row_x1 = int(0.95 * w)
    row1 = frame.image[row_y0:row_y1, row_x0:row_x1]
    cv2.imwrite("templates/quest_row_0.png", row1)
    print(f"Saved quest_row_0: {row1.shape}")

    # Completed row (row 6 - green)
    comp_row_y0 = int(0.70 * h)
    comp_row_y1 = int(0.75 * h)
    comp_row_x0 = int(0.05 * w)
    comp_row_x1 = int(0.95 * w)
    comp_row = frame.image[comp_row_y0:comp_row_y1, comp_row_x0:comp_row_x1]
    cv2.imwrite("templates/quest_row_completed.png", comp_row)
    print(f"Saved quest_row_completed: {comp_row.shape}")

    # MENU button (bottom-left)
    menu_y0 = int(0.78 * h)
    menu_y1 = int(0.85 * h)
    menu_x0 = int(0.01 * w)
    menu_x1 = int(0.08 * w)
    menu_btn = frame.image[menu_y0:menu_y1, menu_x0:menu_x1]
    cv2.imwrite("templates/menu_button.png", menu_btn)
    print(f"Saved menu_button: {menu_btn.shape}")

    # Close/back button area
    close_y0 = int(0.78 * h)
    close_y1 = int(0.85 * h)
    close_x0 = int(0.88 * w)
    close_x1 = int(0.95 * w)
    close_btn = frame.image[close_y0:close_y1, close_x0:close_x1]
    cv2.imwrite("templates/close_button.png", close_btn)
    print(f"Saved close_button: {close_btn.shape}")

    # Status indicator: completed green (row 6 right side)
    green_y0 = int(0.72 * h)
    green_y1 = int(0.74 * h)
    green_x0 = int(0.85 * w)
    green_x1 = int(0.93 * w)
    green_icon = frame.image[green_y0:green_y1, green_x0:green_x1]
    cv2.imwrite("templates/status_completed.png", green_icon)
    print(f"Saved status_completed: {green_icon.shape}")

    print("All templates captured.")


if __name__ == "__main__":
    capture_quest_panel_templates()