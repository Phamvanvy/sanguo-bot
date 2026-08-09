"""Vision-LLM helper: send a cropped region to the local multimodal model
(Qwen VL via llama-server, OpenAI-compatible /v1/chat/completions) and get
structured JSON back.

Used for quest-panel parsing when OCR is unreliable (pixel Vietnamese font) or
unavailable. The local server is configured in config.yaml under `llm`.
"""
from __future__ import annotations

import base64
import json
import cv2
import numpy as np
from dataclasses import dataclass
from typing import Optional

from src.config import load_config


@dataclass
class VisionLLM:
    base_url: str
    api_key: str
    model: str
    enabled: bool = True

    @classmethod
    def from_config(cls, cfg: Optional[dict] = None) -> "VisionLLM":
        cfg = cfg or load_config()
        llm = cfg.get("llm", {})
        return cls(
            base_url=llm.get("base_url", "http://localhost:8080/v1"),
            api_key=llm.get("api_key", "not-needed"),
            model=llm.get("model", "local-model"),
            enabled=bool(llm.get("enabled", False)) or bool(llm.get("use_for_quest_parsing", False)),
        )

    def _encode(self, image: np.ndarray) -> str:
        ok, buf = cv2.imencode(".png", image)
        if not ok:
            raise RuntimeError("imencode failed")
        return base64.b64encode(buf.tobytes()).decode("ascii")

    def ask_json(self, image: np.ndarray, prompt: str, timeout: float = 90.0) -> Optional[dict]:
        """Send image + prompt, expect a JSON object back. Returns parsed dict
        or None on any failure."""
        if not self.enabled:
            return None
        try:
            import requests
        except ImportError:
            print("[llm_vision] 'requests' not installed; cannot call LLM")
            return None
        payload = {
            "model": self.model,
            "messages": [
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": prompt},
                        {
                            "type": "image_url",
                            "image_url": {"url": f"data:image/png;base64,{self._encode(image)}"},
                        },
                    ],
                }
            ],
            "temperature": 0.0,
            "max_tokens": 1024,
        }
        try:
            resp = requests.post(
                f"{self.base_url.rstrip('/')}/chat/completions",
                json=payload,
                headers={"Authorization": f"Bearer {self.api_key}"},
                timeout=timeout,
            )
            resp.raise_for_status()
            content = resp.json()["choices"][0]["message"]["content"]
        except Exception as e:  # noqa: BLE001
            print(f"[llm_vision] request failed: {e}")
            return None
        return _extract_json(content)


def _extract_json(text: str) -> Optional[dict]:
    t = text.strip()
    if t.startswith("```"):
        parts = t.split("```", 2)
        if len(parts) >= 2:
            t = parts[1]
        if t.lower().startswith("json"):
            t = t[4:]
    start = t.find("{")
    end = t.rfind("}")
    if start == -1 or end == -1:
        return None
    try:
        return json.loads(t[start : end + 1])
    except json.JSONDecodeError:
        return None