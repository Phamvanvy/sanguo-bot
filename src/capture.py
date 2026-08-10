"""Unified capture/click interface so the vision and game layers don't care
which control backend is active. Both OS input and Playwright/CDP implement the
same session contract, so vision and game actions keep their existing API.

All coordinates in this module are fractions (0..1) of the game canvas's
own width/height, resolution/DPI independent. `Frame` bundles the raw image
with the pixel rect it came from so vision code can convert a pixel it found
back into a fraction, and action code can convert a fraction into a click.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Optional

import numpy as np

from src.config import load_config
from src.os_input import Rect, attach_or_launch


@dataclass
class Frame:
    image: np.ndarray   # BGR, HxWx3
    rect: Rect          # screen rect this image was captured from

    @property
    def height(self) -> int:
        return self.image.shape[0]

    @property
    def width(self) -> int:
        return self.image.shape[1]

    def pixel_to_fraction(self, px: int, py: int) -> tuple[float, float]:
        return px / self.width, py / self.height

    def fraction_to_pixel(self, fx: float, fy: float) -> tuple[int, int]:
        return int(fx * self.width), int(fy * self.height)


class GameControl:
    """Backend-neutral control scoped to the game canvas."""

    def __init__(self, session: Any, cfg: dict):
        self.session = session
        self.cfg = cfg

    @property
    def control_mode(self) -> str:
        return getattr(self.session, "control_mode", "os_input")

    def focus(self) -> None:
        self.session.focus()

    def canvas_rect(self) -> Rect:
        if self.control_mode != "os_input":
            return self.session.canvas_rect()
        window_rect = self.session.client_rect()
        region = self.cfg["game"]["os_input"].get("canvas_region")
        if not region:
            # Not calibrated yet -- fall back to the full window client
            # area. Run tools/calibrate.py to set game.os_input.canvas_region
            # and get accurate click targets.
            return window_rect
        return Rect(
            left=window_rect.left + int(region["x"] * window_rect.width),
            top=window_rect.top + int(region["y"] * window_rect.height),
            width=int(region["w"] * window_rect.width),
            height=int(region["h"] * window_rect.height),
        )

    def capture(self) -> Frame:
        rect = self.canvas_rect()
        return Frame(image=self.session.capture(rect), rect=rect)

    def click(self, fx: float, fy: float) -> None:
        self.session.click_fraction(fx, fy, rect=self.canvas_rect())

    def type_text(self, text: str) -> None:
        self.session.type_text(text)

    def press(self, key: str) -> None:
        self.session.press(key)

    def set_dry_run(self, dry_run: bool) -> None:
        self.session.set_dry_run(dry_run)

    def close(self) -> None:
        self.session.close()


def open_control(cfg: Optional[dict] = None) -> GameControl:
    cfg = cfg or load_config()
    mode = cfg["game"]["control_mode"]
    if mode == "os_input":
        session = attach_or_launch(cfg)
    elif mode in {"playwright_persistent", "cdp_attach", "auto"}:
        from src.browser import open_game

        session = open_game(cfg)
    else:
        raise ValueError(f"Unknown game.control_mode: {mode!r}")
    return GameControl(session=session, cfg=cfg)
