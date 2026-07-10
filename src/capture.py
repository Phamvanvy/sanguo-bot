"""Unified capture/click interface so the vision and game layers don't care
which control backend is active. Only os_input is confirmed to work against
this game's guard (see src/os_input.py); the playwright path is kept for
possible future use against pages without CDP detection, but is NOT wired to
a live GameSession here since gate_check proved it gets blocked.

All coordinates in this module are fractions (0..1) of the game canvas's
own width/height, resolution/DPI independent. `Frame` bundles the raw image
with the pixel rect it came from so vision code can convert a pixel it found
back into a fraction, and action code can convert a fraction into a click.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Optional

import numpy as np

from src.config import load_config
from src.os_input import OsGameSession, Rect, attach_or_launch


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
    """Wraps an OsGameSession, scoped to the game canvas region within the
    browser window (config game.os_input.canvas_region), so every fraction
    coordinate used elsewhere in the bot is relative to the canvas, not the
    whole browser chrome (tabs/URL bar)."""

    def __init__(self, session: OsGameSession, cfg: dict):
        self.session = session
        self.cfg = cfg

    def canvas_rect(self) -> Rect:
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

    def close(self) -> None:
        self.session.close()


def open_control(cfg: Optional[dict] = None) -> GameControl:
    cfg = cfg or load_config()
    mode = cfg["game"]["control_mode"]
    if mode != "os_input":
        raise ValueError(
            f"control_mode={mode!r} is not usable against this game -- M0 confirmed "
            "guard.js blocks any CDP-based backend (playwright_persistent, cdp_attach). "
            "Set game.control_mode: os_input in config.yaml."
        )
    # attach_or_launch reuses an already-open game window if one exists,
    # instead of spawning a new browser process/tab every call -- important
    # during dev/calibration iteration.
    session = attach_or_launch(cfg)
    return GameControl(session=session, cfg=cfg)
