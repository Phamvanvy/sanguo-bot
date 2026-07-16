"""Debug: print raw LLM response and check quest panel state."""
import sys
import time
import io
import base64
from pathlib import Path

# Ensure we can find src module even when running from tools/
sys.path.insert(0, str(Path(__file__).parent.parent))

import cv2
import requests

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

from src.capture import open_control
from src.config import load_config


def image_to_base64(path: Path) -> str:
    """Encode an image file as base64 data URL."""
    data = path.read_bytes()
    b64 = base64.b64encode(data).decode('ascii')
    return f"data:image/png;base64,{b64}"


def main():
    cfg = load_config()
    gc = open_control(cfg)

    # Open quest panel
    print("[debug] Opening quest panel...")
    gc.press('6')
    time.sleep(1)

    frame = gc.capture()
    cv2.imwrite(str(cfg["_resolved_log_dir"] / "debug_quest.png"), frame.image)
    print(f"[debug] Captured: {frame.width}x{frame.height}")

    # Send directly to llama-server and inspect raw response
    quest_area = frame.image[int(0.35 * frame.height):, :]
    temp_path = cfg["_resolved_log_dir"] / "debug_quest_area.png"
    cv2.imwrite(str(temp_path), quest_area)

    b64_img = image_to_base64(temp_path)
    payload = {
        "model": cfg["llm"]["model"],
        "messages": [
            {
                "role": "user",
                "content": [
                    {"type": "image_url", "image_url": {"url": b64_img}},
                    {"type": "text", "text": "Return a JSON array of quests from the image. Each item: {index, tag, title, status}. Status is 'completed' or 'pending'. Vietnamese text."}
                ]
            }
        ],
        "max_tokens": 500,
        "temperature": 0.1,
    }

    print(f"[debug] Calling llama-server at {cfg['llm']['base_url']}")
    try:
        resp = requests.post(cfg["llm"]["base_url"], json=payload, timeout=60)
        print(f"[debug] Status: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            content = data["choices"][0]["message"]["content"]
            print(f"[debug] Raw response ({len(content)} chars):\n{content}")
        else:
            print(f"[debug] Error: {resp.text[:500]}")
    except Exception as e:
        print(f"[debug] Request failed: {e}")

    gc.close()
    print("[debug] Done")


if __name__ == "__main__":
    main()