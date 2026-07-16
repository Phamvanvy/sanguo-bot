"""Test: use local Qwen VL (multimodal LLM) to parse quest panel directly from
a screenshot. This bypasses the Vietnamese pixel-font OCR problem entirely.

Usage:
  .venv/Scripts/python -m tools.test_llm_vision_parse [image_path]
  (defaults to logs/quest_panel2.png)
"""
from __future__ import annotations

import base64
import json
import sys
from pathlib import Path

import cv2
import numpy as np
import requests


PROMPT = """Bạn là một bot đọc màn hình game. Hãy phân tích ảnh panel "Nhiệm vụ quản lý"
và trả về JSON có cấu trúc chính xác.

Format trả lời (CHỈ JSON, không giải thích):
[
  {
    "index": 0,
    "tag": "Tuần hoàn",
    "title": "Lấy cũ thay mới",
    "status": "Chưa xong"
  },
  ...
]

Luật:
- index: số trong [X] ở cột trái
- tag: từ trong [X] ngay trước tiêu đề
- title: tên nhiệm vụ (loại bỏ tag)
- status: "Chưa xong" hoặc "Hoàn Thành" (dựa trên màu: xanh lá = Hoàn Thành, tím/đỏ = Chưa xong)
"""


def encode_image(img_path: str) -> str:
    with open(img_path, "rb") as f:
        return base64.b64encode(f.read()).decode()


def main() -> None:
    img_path = sys.argv[1] if len(sys.argv) > 1 else "logs/quest_panel2.png"
    img_path = Path(img_path)
    if not img_path.exists():
        print(f"ERROR: {img_path} not found")
        sys.exit(1)

    # Crop to quest panel region (known from screenshots)
    img = cv2.imread(str(img_path))
    h, w = img.shape[:2]
    # Panel occupies roughly top 25% of canvas
    panel = img[int(0.04 * h):int(0.48 * h), :]
    tmp = img_path.with_stem(img_path.stem + "_panel_crop")
    cv2.imwrite(str(tmp), panel)
    print(f"Cropped panel: {tmp} ({panel.shape[1]}x{panel.shape[0]})")

    b64 = encode_image(str(tmp))

    url = "http://localhost:8080/v1/chat/completions"
    headers = {"Content-Type": "application/json"}
    payload = {
        "model": "Qwen3.6-35B-A3B-Uncensored-HauhauCS-Aggressive",
        "messages": [
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": PROMPT},
                    {
                        "type": "image_url",
                        "image_url": {
                            "url": f"data:image/png;base64,{b64}"
                        }
                    }
                ]
            }
        ],
        "max_tokens": 4096,
        "temperature": 0.1,
    }

    print("Sending to Qwen VL (may take 2-3 minutes for multimodal)...")
    resp = requests.post(url, headers=headers, json=payload, timeout=300)
    resp.raise_for_status()
    data = resp.json()
    text = data["choices"][0]["message"]["content"].strip()
    print(f"\nLLM response:\n{text}\n")

    # Try to parse JSON from response
    # Often LLMs wrap JSON in markdown ```json ... ```
    import re
    m = re.search(r'```(?:json)?\s*([\s\S]*?)```', text)
    if m:
        text = m.group(1)

    # Strip any trailing/prefix non-JSON text
    text = text.strip()
    # Ensure it starts with [ and ends with ]
    start = text.find('[')
    end = text.rfind(']')
    if start >= 0 and end > start:
        text = text[start:end+1]

    quests = json.loads(text)
    print(f"\nParsed {len(quests)} quests:")
    for q in quests:
        status_mark = "✅" if q["status"] == "Hoàn Thành" else "❌"
        print(f"  {status_mark} [{q['index']}] [{q['tag']}] {q['title']} → {q['status']}")


if __name__ == "__main__":
    main()