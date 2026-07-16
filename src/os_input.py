"""OS-level control backend: drives a REAL browser process via OS input,
with no CDP/automation attachment ever involved.

Why this exists (M0 result): control_mode=playwright_persistent was blocked
by guard.js -- "Da phat hien cong cu debug" (see logs/boot_canvas_gone.png)
-- even with the DevTools panel never opened. Playwright always drives the
page over the Chrome DevTools Protocol, and just enabling the Runtime domain
(required for page.evaluate/screenshot/console) triggers the classic
getter-preview devtools-detection trick many disable-devtool libraries use.
That means cdp_attach would very likely be caught the same way -- the
problem is CDP itself, not which client speaks it. So this module launches
Brave as a plain, unmodified process (a human could have double-clicked the
same icon) and does everything else -- screen capture, mouse/keyboard --
at the OS level, where the page's JS has no visibility at all.

Run directly to execute the gate check:
    .venv/Scripts/python -m src.os_input
It launches Brave, finds its window, screenshots the client area to
logs/boot_os_input.png, and prints a coarse verdict. Always eyeball the
screenshot yourself -- the automated check only rules out a flat/blank frame,
it cannot distinguish a real game board from e.g. a solid-color block modal.
"""
from __future__ import annotations

import ctypes
import subprocess
import sys
import time
from dataclasses import dataclass
from typing import Optional

# Must happen before any GetWindowRect/GetClientRect/mss call: on a
# multi-monitor setup with mixed display scaling, a process that hasn't
# declared itself DPI-aware gets *virtualized* (scaled/repositioned)
# window rects from Windows, which silently desyncs click coordinates and
# screen capture regions from what's actually on screen. Per-Monitor-V2
# awareness makes every rect we read/use be in real physical pixels.
try:
    ctypes.windll.shcore.SetProcessDpiAwareness(2)  # PROCESS_PER_MONITOR_DPI_AWARE
except Exception:  # noqa: BLE001
    try:
        ctypes.windll.user32.SetProcessDPIAware()
    except Exception:  # noqa: BLE001
        pass

import pathlib

import cv2
import mss
import numpy as np
import psutil
import pydirectinput
import pygetwindow as gw
import win32gui
import win32process

from src.config import load_config

# SendInput-based moves/clicks -- indistinguishable at the OS level from a
# real mouse. Disable pyautogui-style corner failsafe (not applicable here).
pydirectinput.FAILSAFE = False


@dataclass
class Rect:
    left: int
    top: int
    width: int
    height: int


@dataclass
class OsGameSession:
    process: Optional[subprocess.Popen]
    hwnd: int

    def __post_init__(self) -> None:
        self._dry_run = False

    def client_rect(self) -> Rect:
        return _client_rect(self.hwnd)

    def focus(self) -> None:
        _focus(self.hwnd)

    def capture(self, rect: Optional[Rect] = None) -> np.ndarray:
        rect = rect or self.client_rect()
        return _grab(rect)

    def set_dry_run(self, dry_run: bool) -> None:
        """Enable dry-run mode: skip focus() calls. Used when bot is
        logging actions without actually sending input to the game."""
        self._dry_run = dry_run

    def click_fraction(self, fx: float, fy: float, rect: Optional[Rect] = None) -> None:
        """Click a point given as a fraction (0..1) of the window's client area."""
        if getattr(self, "_dry_run", False):
            return
        rect = rect or self.client_rect()
        x = int(rect.left + fx * rect.width)
        y = int(rect.top + fy * rect.height)
        self.focus()
        pydirectinput.moveTo(x, y)
        time.sleep(0.05)
        pydirectinput.click()

    def type_text(self, text: str, interval: float = 0.03) -> None:
        """Type ASCII text into whatever field currently has focus (click it
        first). pydirectinput sends real key-down/up events, same as typing
        on the real keyboard."""
        if getattr(self, "_dry_run", False):
            return
        self.focus()
        pydirectinput.write(text, interval=interval)

    def press(self, key: str) -> None:
        if getattr(self, "_dry_run", False):
            return
        self.focus()
        pydirectinput.press(key)

    def close(self) -> None:
        # Deliberately does not kill the process -- this session is meant to
        # look like an ordinary user browsing session. Close the window
        # yourself, or call self.process.terminate() explicitly if needed.
        pass


def _client_rect(hwnd: int) -> Rect:
    left, top, right, bottom = win32gui.GetClientRect(hwnd)  # (0, 0, w, h)
    left, top = win32gui.ClientToScreen(hwnd, (left, top))
    right, bottom = win32gui.ClientToScreen(hwnd, (right, bottom))
    return Rect(left=left, top=top, width=right - left, height=bottom - top)


def _focus(hwnd: int) -> None:
    try:
        win32gui.ShowWindow(hwnd, 9)  # SW_RESTORE
        win32gui.SetForegroundWindow(hwnd)
    except Exception:  # noqa: BLE001
        pass
    time.sleep(0.1)


def _grab(rect: Rect) -> np.ndarray:
    with mss.mss() as sct:
        monitor = {"left": rect.left, "top": rect.top, "width": rect.width, "height": rect.height}
        img = np.array(sct.grab(monitor))
    return cv2.cvtColor(img, cv2.COLOR_BGRA2BGR)


def _owning_process_name(hwnd: int) -> str:
    try:
        _, pid = win32process.GetWindowThreadProcessId(hwnd)
        return psutil.Process(pid).name().lower()
    except Exception:  # noqa: BLE001
        return ""


def _find_window(title_hint: str, process_exe: Optional[str] = None, timeout: float = 30.0) -> int:
    """Find the game window by title AND by owning process. Title alone is
    NOT enough: this project's own folder is named "sanguo-bot", so a
    title_hint like "Sanguo" also substring-matches this very VS Code
    window, and picking the "widest match" used to prefer VS Code (much
    bigger) over the actual, smaller Brave window -- silently sending every
    click/keypress to the editor instead of the game. Filtering by the
    process's exe name (e.g. "brave.exe") makes the match unambiguous
    regardless of what title collisions exist.
    """
    expected_proc = pathlib.Path(process_exe).name.lower() if process_exe else None
    deadline = time.time() + timeout
    while time.time() < deadline:
        candidates = [w for w in gw.getAllWindows() if w.title.strip() and title_hint.lower() in w.title.lower()]
        if expected_proc:
            candidates = [w for w in candidates if _owning_process_name(w._hWnd) == expected_proc]
        if candidates:
            best = max(candidates, key=lambda w: w.width * w.height)
            return best._hWnd
        time.sleep(0.5)
    raise TimeoutError(
        f"No window matching title={title_hint!r} process={expected_proc!r} appeared within {timeout}s"
    )


def launch(cfg: Optional[dict] = None) -> OsGameSession:
    """Start Brave as a normal process (no debugging/automation flags) and
    navigate it to the game URL by passing the URL as a command-line arg,
    exactly like a shortcut would."""
    cfg = cfg or load_config()
    oi = cfg["game"]["os_input"]
    window = cfg["game"]["window"]
    profile_dir = cfg["_resolved_os_profile_dir"]
    profile_dir.mkdir(parents=True, exist_ok=True)

    process = subprocess.Popen([
        oi["browser_exe"],
        f"--user-data-dir={profile_dir}",
        f"--window-size={window['width']},{window['height']}",
        f"--window-position={window.get('pos_x', 0)},{window.get('pos_y', 0)}",
        cfg["game"]["url"],
    ])
    hwnd = _find_window(oi["window_title_hint"], process_exe=oi["browser_exe"])
    session = OsGameSession(process=process, hwnd=hwnd)
    session.focus()
    return session


def attach_or_launch(cfg: Optional[dict] = None) -> OsGameSession:
    """For calibration/dev tools: reuse an already-open game window if one
    exists (avoids spawning a duplicate Brave instance every time a
    calibration command runs), otherwise launch a fresh one."""
    cfg = cfg or load_config()
    oi = cfg["game"]["os_input"]
    try:
        hwnd = _find_window(oi["window_title_hint"], process_exe=oi["browser_exe"], timeout=2.0)
        session = OsGameSession(process=None, hwnd=hwnd)
        session.focus()
        return session
    except TimeoutError:
        return launch(cfg)


def gate_check(cfg: Optional[dict] = None) -> bool:
    cfg = cfg or load_config()
    session = launch(cfg)

    print("[os_input.gate_check] waiting for page to boot (first run may also show a Brave welcome dialog)...")
    time.sleep(10)

    rect = session.client_rect()
    frame = session.capture(rect)
    out_path = cfg["_resolved_log_dir"] / "boot_os_input.png"
    cv2.imwrite(str(out_path), frame)

    std = float(frame.std())
    print(f"[os_input.gate_check] window_rect={rect} frame_std={std:.2f} screenshot={out_path}")
    print("Inspect the screenshot yourself: this heuristic only rules out a flat/blank frame.")
    return std > 5.0


if __name__ == "__main__":
    ok = gate_check()
    print("GATE PASS (frame is not flat/blank)" if ok else "GATE FAIL - inspect logs/boot_os_input.png")
    sys.exit(0 if ok else 1)
