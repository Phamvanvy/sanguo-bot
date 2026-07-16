"""Test M2: parse quest panel from game screenshot via LLM + color detection.

Usage:
  python tools/test_m2_parse.py            # capture fresh screenshot
  python tools/test_m2_parse.py --file X   # parse an existing screenshot
"""
from __future__ import annotations

import argparse
import json
import sys
import pathlib

sys.path.insert(0, str(pathlib.Path(__file__).parent.parent))

from src.capture import open_control
from src.vision import read_quest_panel
from src.game.quests import parse_quests_from_panel


def parse_from_screenshot(frame, cfg: dict, label: str) -> None:
    print(f"\n=== {label} ===")
    try:
        ocr_items = read_quest_panel(frame, cfg)
        quests = parse_quests_from_panel(ocr_items, cfg)
        print(f"Found {len(quests)} quests:")
        for q in quests:
            status_mark = "✓" if q.is_completed else "○"
            tag = q.tag or "?"
            print(f"  [{status_mark}] [{tag}] {q.title}")
        # Also print full dict for debugging
        for q in quests:
            print(f"    {json.dumps(q.to_dict(), ensure_ascii=False, indent=2)}")
    except Exception as e:
        print(f"PARSE ERROR: {e}")
        import traceback
        traceback.print_exc()


def main() -> int:
    parser = argparse.ArgumentParser(description="Test M2 quest panel parsing")
    parser.add_argument("--file", help="Path to existing PNG screenshot to parse")
    args = parser.parse_args()

    import src.config
    import cv2
    cfg = src.config.load_config()

    if args.file:
        # Parse existing screenshot
        img = cv2.imread(args.file)
        print(f"Parsing existing file: {args.file}")
        parse_from_screenshot(img, cfg, f"File: {args.file}")
        return 0

    # Capture fresh screenshot
    gc = open_control()
    frame = gc.capture()
    print(f"Captured frame: {frame.width}x{frame.height}")

    # Save raw frame for reference
    cv2.imwrite("logs/parse_test_fresh.png", frame.image)

    # Try LLM first (multimodal)
    try:
        parse_from_screenshot(frame.image, cfg, "LLM (Qwen-VL)")
    except Exception as e:
        print(f"LLM failed (expected if server busy): {e}")

    # Fallback to color detection
    parse_from_screenshot(frame.image, cfg, "Color detection")

    return 0


if __name__ == "__main__":
    sys.exit(main())