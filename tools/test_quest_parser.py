"""Test quest parser on a previously captured quest panel screenshot.

Usage:
    .venv/Scripts/python -m tools.test_quest_parser
"""
from __future__ import annotations

import argparse
import sys
import cv2
import numpy as np
import json

from src.config import load_config
from src.game.quests import QuestParser, QuestStatus


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("screenshot", nargs="?", default="logs/quest_panel.png",
                        help="Path to quest panel screenshot (default: logs/quest_panel.png)")
    args = parser.parse_args()

    cfg = load_config()

    # Load screenshot
    frame = cv2.imread(args.screenshot)
    if frame is None:
        print(f"ERROR: Cannot read screenshot: {args.screenshot}")
        return 1

    print(f"Loaded screenshot: {args.screenshot} shape={frame.shape}")

    # Parse quest panel
    quest_cfg = cfg.get("quests", {})
    parser = QuestParser(cfg)

    # Since we're parsing from a full screenshot (not raw canvas), we need to
    # first crop to the canvas region, then parse
    canvas_region = cfg["game"]["os_input"].get("canvas_region")
    if canvas_region:
        h, w = frame.shape[:2]
        x = int(canvas_region["x"] * w)
        y = int(canvas_region["y"] * h)
        cw = int(canvas_region["w"] * w)
        ch = int(canvas_region["h"] * h)
        canvas_crop = frame[y:y+ch, x:x+cw]
        print(f"Cropped to canvas region: ({x},{y},{cw},{ch})")
    else:
        canvas_crop = frame
        print("Using full frame as canvas (no canvas_region set)")

    panel = parser.parse_panel(canvas_crop)

    # Output results
    print(f"\n{'='*60}")
    print(f"QuestPanel: {panel.count_completed}/{panel.count_total} completed")
    print(f"{'='*60}")

    if not panel.quests:
        print("\nNo quests found! This may happen if:")
        print("  1. The screenshot doesn't match the expected format")
        print("  2. OCR engine not available (PaddleOCR/easyocr/LLM)")
        print("  3. Template for quest_panel_header.png not found")
        print("\nTry:")
        print("  - Running with a fresh quest_panel.png screenshot")
        print("  - Ensuring at least one vision engine is available")
        return 0

    for q in panel.quests:
        status_icon = "✓" if q.is_finished else "○"
        tag_display = f"[{q.tag}]" if q.tag else "[?]"
        color_info = f" color={q.status_color}" if q.status_color else ""
        print(f"\n  {status_icon} #{q.index}: {tag_display} {q.title}")
        print(f"      raw: {q.full_text}")
        print(f"      status={q.status.value}{color_info}")
        print(f"      y_frac={q.row_y_fraction:.4f}")

    # Save structured output as JSON for debugging
    output = {
        "count_completed": panel.count_completed,
        "count_total": panel.count_total,
        "quests": [
            {
                "index": q.index,
                "tag": q.tag,
                "title": q.title,
                "full_text": q.full_text,
                "status": q.status.value,
                "status_color": q.status_color,
                "row_y_fraction": q.row_y_fraction,
            }
            for q in panel.quests
        ]
    }

    print(f"\n{'='*60}")
    print("JSON output:")
    print(json.dumps(output, indent=2, ensure_ascii=False))

    # Check if we have any unfinished quests
    if panel.has_any_unfinished:
        first = panel.first_unfinished()
        print(f"\nFirst unfinished: {first.tag}/{first.title} ({first.status.value})")
    else:
        print("\nAll quests are complete!")

    return 0


if __name__ == "__main__":
    sys.exit(main())