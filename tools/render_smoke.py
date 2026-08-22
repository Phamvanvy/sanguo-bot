#!/usr/bin/env python3
"""G3b live check: drive the browser client against the self-hosted server and prove it renders.

Unit tests cover the maths; this covers the part only a real browser can answer -- that the
2011 art actually decodes and paints on a canvas, that the character is on the map where the
server put it, and that walking moves it. It logs in for real, so it needs the stack up:

    docker compose -f selfhost/docker-compose.yml up -d
    node web/bridge/bridge.js

Credentials come from the environment or the command line, never the source:

    SANGUO_ACCOUNT=... SANGUO_PASSWORD=... python tools/render_smoke.py
    python tools/render_smoke.py --account x --password y [--url http://127.0.0.1:8090/game.html]

Screenshots land in a gitignored directory (default web/client/assets/spike/) and are the
artefact worth looking at afterwards.
"""
from __future__ import annotations

import argparse
import os
import sys
import time
from pathlib import Path

from playwright.sync_api import sync_playwright

REPO = Path(__file__).resolve().parent.parent
DEFAULT_OUT = REPO / "web" / "client" / "assets" / "spike"

# The client is in Vietnamese and so are the names it prints back; a Windows console defaults
# to a codepage that cannot encode them, which would fail the run over a log line.
for stream in (sys.stdout, sys.stderr):
    stream.reconfigure(encoding="utf-8", errors="replace")


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--url", default="http://127.0.0.1:8090/game.html")
    p.add_argument("--account", default=os.environ.get("SANGUO_ACCOUNT", ""))
    p.add_argument("--password", default=os.environ.get("SANGUO_PASSWORD", ""))
    p.add_argument("--out", default=str(DEFAULT_OUT))
    p.add_argument("--headed", action="store_true", help="watch it run")
    p.add_argument("--timeout", type=float, default=60.0, help="seconds to wait for the world")
    return p.parse_args()


def main() -> int:
    args = parse_args()
    if not args.account or not args.password:
        print("need --account/--password (or SANGUO_ACCOUNT/SANGUO_PASSWORD)", file=sys.stderr)
        return 2

    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)
    errors: list[str] = []

    with sync_playwright() as pw:
        browser = pw.chromium.launch(headless=not args.headed)
        page = browser.new_page(viewport={"width": 1000, "height": 700})
        page.on("console", lambda m: errors.append(f"console.{m.type}: {m.text}")
                if m.type == "error" else None)
        page.on("pageerror", lambda e: errors.append(f"pageerror: {e}"))

        page.goto(args.url, wait_until="domcontentloaded")
        # The client boots behind a splash while it decodes its UI art and the area index;
        # the login form only exists to be filled once that is done.
        page.wait_for_selector("#login:not([hidden]) #account", timeout=int(args.timeout * 1000))
        page.fill("#account", args.account)
        page.fill("#password", args.password)
        page.click("#loginBtn")

        # The character list appears once ACCOUNT_LOGIN + ACTOR_LIST have come back.
        page.wait_for_selector("#charList button", timeout=int(args.timeout * 1000))
        character = page.locator("#charList button").first.inner_text()
        page.locator("#charList button").first.click()

        # "In world" means the map finished decoding, not merely that the socket is up.
        page.wait_for_function("() => window.__game?.renderer?.scene && window.__game.player",
                               timeout=int(args.timeout * 1000))
        state = page.evaluate("""() => {
            const g = window.__game;
            return {
                map: g.renderer.scene.id, name: g.renderer.scene.name,
                w: g.renderer.scene.width, h: g.renderer.scene.height,
                tiles: g.renderer.scene.stats.tiles, decor: g.renderer.scene.stats.decor,
                buildMs: g.renderer.scene.stats.buildMs,
                x: g.player.x, y: g.player.y, name_: g.player.name,
                animate: g.player.animateId, hasSprites: !!g.player.sprites,
            };
        }""")
        print(f"character   : {' · '.join(character.split())}")
        print(f"map         : {state['name']} ({state['map']}) {state['w']}x{state['h']}, "
              f"{state['tiles']} tile blits, {state['decor']} decor, built in {state['buildMs']}ms")
        print(f"spawn       : x={state['x']} y={state['y']} animate={state['animate']} "
              f"sprites={'yes' if state['hasSprites'] else 'NO'}")

        time.sleep(0.5)                       # let a frame or two land before capturing
        page.screenshot(path=str(out / "g3b_world.png"))

        # --- walk: send the character along the ground and watch it get there ---
        # The server persists where the last run left the character, so "always walk right"
        # eventually starts from the map's right edge with no room to move. Walk towards
        # whichever side has space instead, and assert on the direction actually asked for.
        before = page.evaluate("""() => {
            const g = window.__game;
            const span = 60;
            const room = g.renderer.scene.width - 8 - g.player.x;
            const dx = room >= span ? span : -span;
            const tx = Math.max(8, Math.min(g.renderer.scene.width - 8, g.player.x + dx));
            g.player.setTarget(tx, g.player.y, performance.now());
            return { x: g.player.x, y: g.player.y, tx, dx: Math.sign(tx - g.player.x) };
        }""")
        time.sleep(0.35)
        walking = page.evaluate("""() => ({x: window.__game.player.x, y: window.__game.player.y,
                                           moving: window.__game.player.moving,
                                           animate: window.__game.player.animateId})""")
        page.screenshot(path=str(out / "g3b_walking.png"))
        deadline = time.time() + 10
        while time.time() < deadline and page.evaluate("() => window.__game.player.moving"):
            time.sleep(0.2)
        after = page.evaluate("""() => ({x: window.__game.player.x, y: window.__game.player.y,
                                         moving: window.__game.player.moving,
                                         sent: window.__game.session.stats.sent})""")
        page.screenshot(path=str(out / "g3b_after_walk.png"))
        print(f"walk        : {before['x']},{before['y']} -> mid {walking['x']},{walking['y']} "
              f"(animate {walking['animate']}, moving={walking['moving']}) -> {after['x']},{after['y']} "
              f"(target {before['tx']})")

        # --- the pixels: is anything actually drawn? ---
        pixels = page.evaluate("""() => {
            const c = document.getElementById('view');
            const d = c.getContext('2d').getImageData(0, 0, c.width, c.height).data;
            const seen = new Set();
            let lit = 0;
            for (let i = 0; i < d.length; i += 4) {
                if (d[i] || d[i+1] || d[i+2]) lit++;
                if (i % 400 === 0) seen.add((d[i] << 16) | (d[i+1] << 8) | d[i+2]);
            }
            return { lit, total: d.length / 4, colours: seen.size };
        }""")
        print(f"canvas      : {pixels['lit']}/{pixels['total']} non-black pixels, "
              f"~{pixels['colours']} distinct colours sampled")

        stats = page.evaluate("() => window.__game.session.stats")
        print(f"packets     : {stats}")
        browser.close()

    ok = True

    def check(cond: bool, msg: str) -> None:
        nonlocal ok
        print(("  ok   " if cond else "  FAIL ") + msg)
        ok = ok and cond

    print("\nchecks:")
    check(state["tiles"] > 0, f"map background drew {state['tiles']} tiles")
    check(state["hasSprites"], "character art loaded")
    check(pixels["lit"] > pixels["total"] * 0.2, "the canvas is not mostly black")
    check(pixels["colours"] > 20, "the map is real art, not flat colour")
    check(walking["moving"] or walking["x"] != before["x"], "the character was walking mid-click")
    moved = (after["x"] - before["x"]) * before["dx"]
    check(moved > 0, f"the character moved towards {before['tx']} ({before['x']} -> {after['x']})")
    check(after["sent"] > 0, "MOVE packets went to the server")
    check(not errors, "no console errors" + ("" if not errors else ": " + "; ".join(errors[:5])))

    print(f"\nscreenshots in {out}")
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
