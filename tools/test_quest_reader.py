"""M2 test: parse quest panel screenshot into structured JSON.

Usage:
    # From project root, with venv active and Brave showing the quest panel:
    .venv/Scripts/python -m tools.test_quest_reader

This will:
1. Capture current game canvas
2. Save screenshot to logs/m2_test_panel.png
3. Parse using LLM (Qwen VL multimodal) + color detection fallback
4. Print structured JSON
5. Save result to logs/quest_state.json
"""
from __future__ import annotations

import json
import sys
import time
from pathlib import Path

import cv2

# Add project root to path
sys.path.insert(0, str(Path(__file__).parent.parent))

from src.capture import open_control
from src.vision.ocr import read_quest_panel
from src.game.quests import QuestList


def test_parse() -> None:
    """Test M2: open quest panel → capture → parse → print quest list as JSON.

    Strategy: use keyboard shortcut '6' which is more reliable than clicking
    the MENU popup. Since we know the exact key works, just ensure focus.
    """
    cfg = None
    try:
        from src.config import load_config
        cfg = load_config()
    except Exception as e:
        print(f"(warning: config load failed: {e})")
        cfg = {}

    print("[M2] Opening game control...")
    control = open_control(cfg)

    # Focus game window first
    print("[M2] Focusing game window...")
    control.focus()
    time.sleep(0.5)

    # Check current state: is panel already open?
    print("[M2] Capturing initial state to check...")
    frame = control.capture()
    panel_path_before = Path("./logs/m2_check_before.png")
    cv2.imwrite(str(panel_path_before), frame.image)
    print(f"[M2] Saved to {panel_path_before}")

    # Check if panel already open
    print("[M2] Checking if quest panel already open...")
    has_panel = _detect_quest_panel(frame.image, cfg)

    if not has_panel:
        print("[M2] Quest panel not detected. Opening via key '6'...")
        # Press '6' to open quest panel
        for _ in range(2):
            control.press('6')
            time.sleep(0.8)
        time.sleep(1.0)
    else:
        print("[M2] Quest panel already open!")

    # Final capture
    print("[M2] Capturing final canvas...")
    frame = control.capture()
    log_dir = Path("./logs")
    log_dir.mkdir(exist_ok=True)
    panel_path = log_dir / "m2_test_panel.png"
    cv2.imwrite(str(panel_path), frame.image)
    print(f"[M2] Saved to {panel_path} ({frame.width}x{frame.height})")

    # Parse quest panel
    print("[M2] Parsing quest panel...")
    items = read_quest_panel(frame.image, cfg)
    print(f"[M2] Parsed {len(items)} quest items")

    # Print raw vision results
    for item in items:
        print(f"  [{item.index}] tag={item.tag!r} title={item.title!r} status={item.status!r}")

    # Convert to QuestList
    quest_list = QuestList.parse(items, cfg)
    print(f"\n[M2] Summary: total={quest_list.count} pending={quest_list.pending_count} completed={quest_list.completed_count}")

    # Print structured JSON
    data = quest_list.to_dict()
    json_str = json.dumps(data, indent=2, ensure_ascii=False)
    print(f"\n[M2] Quest List JSON:\n{json_str}")

    # Log to file
    quest_list.log(log_dir)
    print(f"\n[M2] Logged to {log_dir / 'quest_state.json'}")

    return quest_list


def _detect_quest_panel(frame: np.ndarray, cfg: dict) -> bool:
    """Detect if quest panel is visible by looking for 'Nhiem' text pattern."""
    import requests
    from src.config import load_config as _lc
    c = cfg or _lc()
    try:
        # Quick LLM check: "Is this a quest panel?"
        import base64
        temp = Path("./logs/detect_temp.png")
        cv2.imwrite(str(temp), frame)
        data = temp.read_bytes()
        b64 = base64.b64encode(data).decode('ascii')
        img_url = f"data:image/png;base64,{b64}"
        resp = requests.post(c["llm"]["base_url"], json={
            "model": c["llm"]["model"],
            "messages": [{
                "role": "user",
                "content": [
                    {"type": "image_url", "image_url": {"url": img_url}},
                    {"type": "text", "text": "Reply ONLY 'true' if this image shows a quest panel with Vietnamese text like 'Nhiem vu', 'Chua xong', 'Hoan Thanh'. Reply ONLY 'false' if not. Nothing else."}
                ],
            }],
            "max_tokens": 10,
            "temperature": 0,
        }, timeout=30)
        resp.raise_for_status()
        reply = resp.json()["choices"][0]["message"]["content"].strip().lower()
        return "true" in reply
    except Exception:
        return False


if __name__ == "__main__":
    result = test_parse()
    print(f"\nDone. Pending quests to execute: {result.pending_count}")