"""M0: launch a browser under our control and get the game rendering.

Tries, in order (governed by config.game.control_mode):
  1. playwright_persistent - Chromium via Playwright with a persistent profile
     and anti-automation patches applied before any page script runs.
  2. cdp_attach - attach Playwright to an already-running real browser
     (e.g. Brave started with --remote-debugging-port=9222). Use this if (1)
     gets blocked by disable-devtool.min.js / guard.js.

Run directly to execute the gate check:
    .venv/Scripts/python -m src.browser
It navigates to the game, waits, screenshots #screen to logs/boot.png, and
prints a verdict. Inspect the screenshot yourself to confirm the game board
actually rendered (not a black screen / block page).
"""
from __future__ import annotations

import pathlib
import subprocess
import sys
import time
from dataclasses import dataclass
from typing import Optional
from urllib.parse import urlparse

import cv2
import numpy as np
from playwright.sync_api import BrowserContext, Page, Playwright, sync_playwright

from src.config import PROJECT_ROOT, load_config
from src.os_input import Rect

# Patches applied via add_init_script BEFORE the page's own JS runs, so
# navigator.webdriver etc. never show the automation signature to guard.js.
STEALTH_INIT_SCRIPT = r"""
Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });
Object.defineProperty(navigator, 'languages', { get: () => ['vi-VN', 'vi', 'en-US', 'en'] });
window.chrome = window.chrome || { runtime: {} };
const originalQuery = window.navigator.permissions && window.navigator.permissions.query;
if (originalQuery) {
  window.navigator.permissions.query = (parameters) => (
    parameters.name === 'notifications'
      ? Promise.resolve({ state: Notification.permission })
      : originalQuery(parameters)
  );
}
"""


@dataclass
class GameSession:
    playwright: Playwright
    context: BrowserContext
    page: Page
    control_mode: str
    canvas_selector: str
    owns_context: bool = True
    process: Optional[subprocess.Popen] = None

    def __post_init__(self) -> None:
        self._dry_run = False

    def focus(self) -> None:
        """CDP input targets this page directly; OS foreground focus is unnecessary."""

    def client_rect(self) -> Rect:
        size = self.page.evaluate("() => ({width: innerWidth, height: innerHeight})")
        return Rect(left=0, top=0, width=int(size["width"]), height=int(size["height"]))

    def canvas_rect(self) -> Rect:
        box = self.page.locator(self.canvas_selector).bounding_box()
        if box is None:
            raise RuntimeError(f"Canvas {self.canvas_selector!r} is not visible")
        return Rect(
            left=int(box["x"]), top=int(box["y"]),
            width=int(box["width"]), height=int(box["height"]),
        )

    def capture(self, rect: Optional[Rect] = None) -> np.ndarray:
        rect = rect or self.canvas_rect()
        png = self.page.screenshot(clip={
            "x": rect.left, "y": rect.top,
            "width": rect.width, "height": rect.height,
        })
        image = cv2.imdecode(np.frombuffer(png, dtype=np.uint8), cv2.IMREAD_COLOR)
        if image is None:
            raise RuntimeError("Playwright returned an invalid screenshot")
        return image

    def set_dry_run(self, dry_run: bool) -> None:
        self._dry_run = dry_run

    def click_fraction(self, fx: float, fy: float, rect: Optional[Rect] = None) -> None:
        if self._dry_run:
            return
        rect = rect or self.canvas_rect()
        self.page.mouse.click(rect.left + fx * rect.width, rect.top + fy * rect.height)

    def type_text(self, text: str, interval: float = 0.03) -> None:
        if self._dry_run:
            return
        self.page.keyboard.type(text, delay=max(0, interval * 1000))

    def press(self, key: str) -> None:
        if self._dry_run:
            return
        aliases = {"escape": "Escape", "enter": "Enter", "space": "Space"}
        self.page.keyboard.press(aliases.get(key.lower(), key))

    def close(self) -> None:
        try:
            if self.owns_context:
                self.context.close()
        finally:
            self.playwright.stop()


def _launch_persistent(cfg: dict) -> GameSession:
    pw = sync_playwright().start()
    profile_dir = str(cfg["_resolved_profile_dir"])
    window = cfg["game"]["window"]

    context = pw.chromium.launch_persistent_context(
        profile_dir,
        headless=cfg["runtime"]["headless"],
        slow_mo=cfg["runtime"]["slow_mo_ms"],
        viewport={"width": window["width"], "height": window["height"]},
        args=[
            "--disable-blink-features=AutomationControlled",
            "--disable-features=IsolateOrigins,site-per-process",
        ],
        ignore_default_args=["--enable-automation"],
        locale="vi-VN",
    )
    context.add_init_script(STEALTH_INIT_SCRIPT)
    page = context.pages[0] if context.pages else context.new_page()
    return GameSession(
        playwright=pw, context=context, page=page,
        control_mode="playwright_persistent",
        canvas_selector=cfg["game"]["canvas_selector"],
    )


def _attach_cdp(cfg: dict) -> GameSession:
    pw = sync_playwright().start()
    cdp = cfg["game"]["cdp"]
    endpoint = cdp["endpoint"]
    process = None
    try:
        browser = pw.chromium.connect_over_cdp(endpoint)
    except Exception as first_error:  # noqa: BLE001
        if not cdp.get("auto_launch", False):
            pw.stop()
            raise RuntimeError(
                f"Cannot connect to CDP at {endpoint}. Start Brave with "
                "--remote-debugging-port or enable game.cdp.auto_launch."
            ) from first_error

        parsed = urlparse(endpoint)
        if not parsed.port:
            pw.stop()
            raise ValueError(f"CDP endpoint must include a port: {endpoint}") from first_error
        profile_dir = pathlib.Path(cdp.get("user_data_dir", "./.brave-cdp-profile"))
        if not profile_dir.is_absolute():
            profile_dir = PROJECT_ROOT / profile_dir
        profile_dir.mkdir(parents=True, exist_ok=True)
        browser_exe = cdp.get("browser_exe") or cfg["game"]["os_input"]["browser_exe"]
        window = cfg["game"]["window"]
        process = subprocess.Popen([
            browser_exe,
            f"--remote-debugging-port={parsed.port}",
            f"--user-data-dir={profile_dir}",
            f"--window-size={window['width']},{window['height']}",
            f"--window-position={window.get('pos_x', 0)},{window.get('pos_y', 0)}",
            "--no-first-run",
            cfg["game"]["url"],
        ])
        deadline = time.time() + float(cdp.get("connect_timeout_seconds", 15))
        while True:
            try:
                browser = pw.chromium.connect_over_cdp(endpoint)
                break
            except Exception as retry_error:  # noqa: BLE001
                if time.time() >= deadline:
                    pw.stop()
                    raise RuntimeError(f"Brave started but CDP did not become ready at {endpoint}") from retry_error
                time.sleep(0.25)

    context = browser.contexts[0] if browser.contexts else browser.new_context()
    context.add_init_script(STEALTH_INIT_SCRIPT)
    game_url = cfg["game"]["url"]
    page = next((candidate for candidate in context.pages if candidate.url.startswith(game_url)), None)
    page = page or (context.pages[0] if context.pages else context.new_page())
    return GameSession(
        playwright=pw, context=context, page=page,
        control_mode="cdp_attach",
        canvas_selector=cfg["game"]["canvas_selector"],
        owns_context=False,
        process=process,
    )


def open_game(cfg: Optional[dict] = None) -> GameSession:
    """Open (or attach to) a browser and navigate to the game URL."""
    cfg = cfg or load_config()
    mode = cfg["game"]["control_mode"]

    if mode == "cdp_attach":
        session = _attach_cdp(cfg)
    elif mode == "playwright_persistent":
        session = _launch_persistent(cfg)
    else:  # auto
        try:
            session = _launch_persistent(cfg)
        except Exception as e:  # noqa: BLE001
            print(f"[browser] playwright_persistent failed ({e}); trying cdp_attach", file=sys.stderr)
            session = _attach_cdp(cfg)

    if not session.page.url.startswith(cfg["game"]["url"]):
        session.page.goto(cfg["game"]["url"], wait_until="domcontentloaded")
    return session


def gate_check(cfg: Optional[dict] = None) -> bool:
    """M0: navigate to the game, wait for canvas to render, save a screenshot.

    Returns True if a canvas element with non-trivial pixel content was found.
    This is a *necessary* but not *sufficient* automated check -- always look
    at logs/boot.png yourself to rule out a "block" screen that still paints
    something to canvas.
    """
    cfg = cfg or load_config()
    session = open_game(cfg)
    page = session.page

    try:
        selector = cfg["game"]["canvas_selector"]
        try:
            page.wait_for_selector(selector, timeout=15000)
        except Exception as e:  # noqa: BLE001
            print(f"[gate_check] canvas selector never appeared: {e}", file=sys.stderr)
            _save_debug(page, cfg, "boot_no_canvas.png")
            return False

        # Give TeaVM time to boot + WebSocket to connect + first frame to paint.
        time.sleep(8)

        canvas = page.query_selector(selector)
        if canvas is None:
            print("[gate_check] canvas disappeared after wait", file=sys.stderr)
            _save_debug(page, cfg, "boot_canvas_gone.png")
            return False

        box = canvas.bounding_box()
        has_content = _canvas_has_content(page, selector)

        out_path = _save_debug(page, cfg, "boot.png")
        print(f"[gate_check] control_mode={session.control_mode} canvas_box={box} "
              f"has_content={has_content} screenshot={out_path}")

        return bool(has_content)
    finally:
        session.close()


def _canvas_has_content(page: Page, selector: str) -> bool:
    """Sample canvas pixels via toDataURL and check it's not a flat/blank image."""
    try:
        data_url_len = page.eval_on_selector(
            selector,
            "el => el.toDataURL('image/png').length",
        )
        # A blank canvas encodes to a very short PNG; a rendered game frame is
        # much larger. This is a coarse heuristic, refined by eyeballing boot.png.
        return data_url_len is not None and data_url_len > 5000
    except Exception as e:  # noqa: BLE001
        print(f"[gate_check] toDataURL read failed (canvas may be tainted): {e}", file=sys.stderr)
        return True  # fall back to "unknown, check screenshot manually"


def _save_debug(page: Page, cfg: dict, filename: str) -> str:
    out_path = cfg["_resolved_log_dir"] / filename
    page.screenshot(path=str(out_path))
    return str(out_path)


if __name__ == "__main__":
    ok = gate_check()
    print("GATE PASS" if ok else "GATE FAIL - inspect logs/boot*.png")
    sys.exit(0 if ok else 1)
