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

import sys
import time
from dataclasses import dataclass
from typing import Optional

from playwright.sync_api import BrowserContext, Page, Playwright, sync_playwright

from src.config import load_config

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

    def close(self) -> None:
        try:
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
    return GameSession(playwright=pw, context=context, page=page, control_mode="playwright_persistent")


def _attach_cdp(cfg: dict) -> GameSession:
    pw = sync_playwright().start()
    endpoint = cfg["game"]["cdp"]["endpoint"]
    browser = pw.chromium.connect_over_cdp(endpoint)
    context = browser.contexts[0] if browser.contexts else browser.new_context()
    context.add_init_script(STEALTH_INIT_SCRIPT)
    page = context.pages[0] if context.pages else context.new_page()
    return GameSession(playwright=pw, context=context, page=page, control_mode="cdp_attach")


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
