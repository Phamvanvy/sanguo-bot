"""Vision layer: OCR + LLM vision parsing for game screens.

Primary engine: local Qwen VL multimodal LLM (reads images directly).
This completely bypasses the Vietnamese pixel-font OCR problem.
Fallback: PaddleOCR (vi) when LLM is unavailable.

All public functions return structured data, not raw text.
"""
from __future__ import annotations

import base64
import json
import re
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Optional

import cv2
import numpy as np
import requests

from src.config import load_config


@dataclass
class QuestEntry:
    """One quest row parsed from a quest panel screenshot."""
    index: int          # quest group number [0], [6], [17], [20]
    tag: str            # category: "Tuần hoàn", "Chú", "Chi", "Hàng ngày", ...
    title: str          # quest name
    status: str         # "Chưa xong" or "Hoàn Thành"


LLM_QUEST_PROMPT = """Bạn là một bot đọc màn hình game. Hãy phân tích ảnh panel "Nhiệm vụ quản lý"
và trả về JSON có cấu trúc chính xác.

Format trả lời (CHỈ JSON, không giải thích):
[
  {
    "index": 0,
    "tag": "Tuần hoàn",
    "title": "Lấy cũ thay mới",
    "status": "Chưa xong"
  }
]

Luật:
- index: số trong [X] ở cột trái
- tag: từ trong [X] ngay trước tiêu đề
- title: tên nhiệm vụ (loại bỏ tag)
- status: "Chưa xong" hoặc "Hoàn Thành" (dựa trên màu: xanh lá = Hoàn Thành, tím/đỏ = Chưa xong)
- Nếu có nhiều quest cùng index, trả về riêng từng dòng.
"""


def encode_image(img_path: str | Path) -> str:
    with open(img_path, "rb") as f:
        return base64.b64encode(f.read()).decode()


def _call_llm_vision(image_path: str | Path, prompt: str, cfg: dict) -> str:
    """Send an image + prompt to local Qwen VL, return the text response."""
    llm_cfg = cfg.get("llm", {})
    base_url = llm_cfg.get("base_url", "http://localhost:8080/v1")
    model = llm_cfg.get("model", "Qwen3.6-35B-A3B-Uncensored-HauhauCS-Aggressive")

    b64 = encode_image(image_path)
    url = f"{base_url}/chat/completions"
    payload = {
        "model": model,
        "messages": [
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": prompt},
                    {
                        "type": "image_url",
                        "image_url": {"url": f"data:image/png;base64,{b64}"}
                    }
                ]
            }
        ],
        "max_tokens": 4096,
        "temperature": 0.1,
    }

    for attempt in range(3):
        try:
            resp = requests.post(url, json=payload, timeout=300)
            resp.raise_for_status()
            data = resp.json()
            return data["choices"][0]["message"]["content"].strip()
        except Exception as e:
            if attempt == 2:
                raise
            print(f"[llm_vision] attempt {attempt+1} failed: {e}, retrying...")
            time.sleep(5)


def _parse_llm_json_response(text: str) -> list:
    """Extract JSON array from LLM response (handles markdown wrapping)."""
    # Strip markdown code fence
    m = re.search(r'```(?:json)?\s*([\s\S]*?)```', text)
    if m:
        text = m.group(1)

    text = text.strip()
    start = text.find('[')
    end = text.rfind(']')
    if start >= 0 and end > start:
        text = text[start:end + 1]

    return json.loads(text)


def parse_quest_list_from_image(
    image: np.ndarray,
    cfg: Optional[dict] = None,
    panel_region: Optional[tuple[float, float, float, float]] = None,
) -> list[QuestEntry]:
    """Parse quest list from a full canvas screenshot.

    Args:
        image: BGR numpy array (full canvas frame).
        cfg: bot config (for LLM endpoint).
        panel_region: optional (y0, x0, w, h) as fractions of image to crop.
            Defaults to the region known from quest_panel2.png.

    Returns:
        List of QuestEntry, one per quest row.
    """
    cfg = cfg or load_config()
    h, w = image.shape[:2]

    if panel_region:
        y0, x0, frac_w, frac_h = panel_region
        y1 = int(y0 * h)
        x1 = int(x0 * w)
        cw = int(frac_w * w)
        ch = int(frac_h * h)
        panel = image[y1:y1 + ch, x1:x1 + cw]
    else:
        # Default: top ~45% of canvas, full width
        panel = image[int(0.04 * h):int(0.48 * h), :]

    # Save temp crop for LLM
    tmp_path = Path(cfg.get("_resolved_log_dir", "logs")) / "tmp_panel.png"
    cv2.imwrite(str(tmp_path), panel)

    use_llm = cfg.get("llm", {}).get("enabled", True)
    if use_llm:
        try:
            raw = _call_llm_vision(str(tmp_path), LLM_QUEST_PROMPT, cfg)
            data = _parse_llm_json_response(raw)
            return [
                QuestEntry(
                    index=q.get("index", 0),
                    tag=q.get("tag", ""),
                    title=q.get("title", ""),
                    status=q.get("status", "Chưa xong"),
                )
                for q in data
            ]
        except Exception as e:
            print(f"[parse_quest_list] LLM vision failed ({e}), falling back to OCR")

    # Fallback: PaddleOCR (kept for completeness; usually unreliable on this game)
    return _parse_quest_list_ocr(panel, cfg)


def _parse_quest_list_ocr(panel: np.ndarray, cfg: dict) -> list[QuestEntry]:
    """OCR-based fallback. Uses PaddleOCR if available, else easyocr."""
    vision_cfg = cfg.get("vision", {}).get("ocr", {})
    engine = vision_cfg.get("engine", "auto")

    try:
        if engine in ("auto", "paddleocr"):
            from paddleocr import PaddleOCR
            ocr = PaddleOCR(use_angle_cls=True, lang="vi", show_log=False)
            result = ocr.ocr(panel, cls=True)
            return _ocr_lines_to_quests(result)
    except ImportError:
        pass

    try:
        if engine in ("auto", "easyocr"):
            import easyocr
            reader = easyocr.Reader(["vi"], gpu=False)
            result = reader.readtext(panel)
            return _ocr_lines_easy_to_quests(result)
    except ImportError:
        pass

    raise RuntimeError("No OCR engine available (need paddleocr or easyocr)")


def _ocr_lines_to_quests(result) -> list[QuestEntry]:
    """Convert PaddleOCR result to QuestEntry list."""
    entries = []
    for page in result:
        if not page:
            continue
        for line in page:
            box, (text, conf) = line
            if conf < 0.3:
                continue
            entry = _try_parse_ocr_line(text.strip())
            if entry:
                entries.append(entry)
    return entries


def _ocr_lines_easy_to_quests(result) -> list[QuestEntry]:
    """Convert easyocr result to QuestEntry list."""
    entries = []
    for (box, text, conf) in result:
        if conf < 0.3:
            continue
        entry = _try_parse_ocr_line(text.strip())
        if entry:
            entries.append(entry)
    return entries


def _try_parse_ocr_line(text: str) -> Optional[QuestEntry]:
    """Try to parse a single OCR text line into a QuestEntry using regex."""
    # Pattern: [X] [Tag] Title ... STATUS
    m = re.match(
        r'\[(\d+)\]\s+\[([^\]]+)\]\s+(.+?)(?:\s+(Hoàn\s*Thành|Chưa\s*xong))?\s*$',
        text,
        re.IGNORECASE,
    )
    if not m:
        return None
    idx, tag, title, status = m.groups()
    return QuestEntry(
        index=int(idx),
        tag=tag.strip(),
        title=title.strip(),
        status=status.strip() if status else "Chưa xong",
    )


def detect_status_by_color(image: np.ndarray, status_region: tuple[float, float, float, float]) -> str:
    """Detect quest completion status by sampling color in a region.

    Args:
        image: BGR numpy array.
        status_region: (x, y, w, h) as fractions of image.

    Returns:
        "Hoàn Thành" if green-dominant, else "Chưa xong".
    """
    h, w = image.shape[:2]
    fx, fy, fw, fh = status_region
    x = int(fx * w)
    y = int(fy * h)
    rw = int(fw * w)
    rh = int(fh * h)
    patch = image[y:y + rh, x:x + rw]
    if patch.size == 0:
        return "Chưa xong"

    avg = patch.mean(axis=(0, 1))
    # Green dominant = Hoàn Thành (green > red and green > blue)
    if avg[0] > avg[2] and avg[0] > avg[1]:  # BGR: index 0 = Blue, 1 = Green, 2 = Red
        pass  # Blue dominant — not completion
    if avg[1] > avg[2] and avg[1] > avg[0]:  # Green dominant
        return "Hoàn Thành"
    return "Chưa xong"


# --- Backward-compat aliases for bot.py ---
QuestItem = QuestEntry  # alias


def read_quest_panel(frame, cfg: dict) -> list[QuestItem]:
    """Read quest panel from a Frame object or numpy array and return QuestItem list.
    Compatibility wrapper used by bot.py."""
    import numpy as np
    if hasattr(frame, "image"):
        img = frame.image
    elif isinstance(frame, np.ndarray):
        img = frame
    else:
        raise TypeError(f"read_quest_panel expects Frame or np.ndarray, got {type(frame)}")
    return parse_quest_list_from_image(img, cfg)


# --- Standalone test ---
if __name__ == "__main__":
    img = cv2.imread("logs/quest_panel2.png")
    if img is None:
        print("ERROR: logs/quest_panel2.png not found")
    else:
        quests = parse_quest_list_from_image(img)
        print(f"\nParsed {len(quests)} quests:")
        for q in quests:
            mark = "✅" if q.status == "Hoàn Thành" else "❌"
            print(f"  {mark} [{q.index}] [{q.tag}] {q.title} → {q.status}")