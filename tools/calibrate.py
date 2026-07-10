"""Interactive calibration helper. Must be run by a human at the keyboard --
it opens OpenCV windows and reads real mouse clicks on your screen.

Usage (run from the project root, with the venv active):

  # 1. First, find the black game canvas within the Brave window (it sits
  #    below the tab strip/URL bar). Click its top-left corner, then its
  #    bottom-right corner in the window that pops up.
  .venv/Scripts/python -m tools.calibrate canvas

  # 2. Mark a single UI point (button/icon center). Prints a ui.<name>
  #    snippet to paste into config.yaml.
  .venv/Scripts/python -m tools.calibrate point menu_button

  # 3. Crop a template image (button/icon/status color swatch) for
  #    OpenCV template matching. Drag a box, press ENTER/SPACE to confirm,
  #    ESC to cancel. Saves templates/<name>.png.
  .venv/Scripts/python -m tools.calibrate crop claim_reward_button

Every command re-attaches to an already-open game window if one exists
(tools.os_input.attach_or_launch), so you only need to launch the game once
per calibration session.
"""
from __future__ import annotations

import argparse
import sys

import cv2

from src.config import load_config
from src.os_input import Rect, attach_or_launch


def _capture_window(cfg: dict):
    session = attach_or_launch(cfg)
    rect = session.client_rect()
    frame = session.capture(rect)
    return session, rect, frame


def cmd_canvas(cfg: dict) -> None:
    session, window_rect, frame = _capture_window(cfg)
    points: list[tuple[int, int]] = []

    def on_click(event, x, y, flags, userdata):  # noqa: ANN001
        if event == cv2.EVENT_LBUTTONDOWN:
            points.append((x, y))
            print(f"  clicked pixel ({x}, {y})")

    win = "calibrate: click TOP-LEFT then BOTTOM-RIGHT of the black canvas, then press any key"
    cv2.imshow(win, frame)
    cv2.setMouseCallback(win, on_click)
    print(win)
    while len(points) < 2:
        if cv2.waitKey(50) != -1 and len(points) >= 2:
            break
    cv2.waitKey(0)
    cv2.destroyAllWindows()

    (x1, y1), (x2, y2) = points[0], points[1]
    left, top = min(x1, x2), min(y1, y2)
    w, h = abs(x2 - x1), abs(y2 - y1)
    region = {
        "x": round(left / window_rect.width, 4),
        "y": round(top / window_rect.height, 4),
        "w": round(w / window_rect.width, 4),
        "h": round(h / window_rect.height, 4),
    }
    print("\nPaste into config.yaml under game.os_input.canvas_region:\n")
    print(f"    canvas_region: {{x: {region['x']}, y: {region['y']}, w: {region['w']}, h: {region['h']}}}")


def _canvas_frame(cfg: dict):
    session = attach_or_launch(cfg)
    window_rect = session.client_rect()
    region = cfg["game"]["os_input"].get("canvas_region")
    if region:
        rect = Rect(
            left=window_rect.left + int(region["x"] * window_rect.width),
            top=window_rect.top + int(region["y"] * window_rect.height),
            width=int(region["w"] * window_rect.width),
            height=int(region["h"] * window_rect.height),
        )
    else:
        print("(warning: game.os_input.canvas_region not set yet -- using full window; "
              "run 'canvas' first for accurate fractions)")
        rect = window_rect
    frame = session.capture(rect)
    return session, rect, frame


def cmd_point(cfg: dict, name: str) -> None:
    session, rect, frame = _canvas_frame(cfg)
    picked: list[tuple[int, int]] = []

    def on_click(event, x, y, flags, userdata):  # noqa: ANN001
        if event == cv2.EVENT_LBUTTONDOWN:
            picked.append((x, y))
            print(f"  clicked pixel ({x}, {y})")

    win = f"calibrate point '{name}': click it, then press any key"
    cv2.imshow(win, frame)
    cv2.setMouseCallback(win, on_click)
    print(win)
    while not picked:
        if cv2.waitKey(50) != -1 and picked:
            break
    cv2.waitKey(0)
    cv2.destroyAllWindows()

    x, y = picked[-1]
    fx, fy = round(x / rect.width, 4), round(y / rect.height, 4)
    print(f"\nPaste into config.yaml under ui.{name}:\n")
    print(f"    {name}: {{x: {fx}, y: {fy}}}")


def cmd_crop(cfg: dict, name: str) -> None:
    session, rect, frame = _canvas_frame(cfg)
    print(f"Drag a box around '{name}', then press ENTER/SPACE to confirm (ESC to cancel).")
    x, y, w, h = cv2.selectROI("select region, then ENTER", frame, showCrosshair=True)
    cv2.destroyAllWindows()
    if w == 0 or h == 0:
        print("Cancelled -- nothing saved.")
        return

    templates_dir = cfg["_resolved_templates_dir"]
    out_path = templates_dir / f"{name}.png"
    cv2.imwrite(str(out_path), frame[y:y + h, x:x + w])

    fx, fy = round(x / rect.width, 4), round(y / rect.height, 4)
    fw, fh = round(w / rect.width, 4), round(h / rect.height, 4)
    print(f"\nSaved template: {out_path}")
    print(f"Region (fractions of canvas): {{x: {fx}, y: {fy}, w: {fw}, h: {fh}}}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="cmd", required=True)
    sub.add_parser("canvas")
    p_point = sub.add_parser("point")
    p_point.add_argument("name")
    p_crop = sub.add_parser("crop")
    p_crop.add_argument("name")

    args = parser.parse_args()
    cfg = load_config()

    if args.cmd == "canvas":
        cmd_canvas(cfg)
    elif args.cmd == "point":
        cmd_point(cfg, args.name)
    elif args.cmd == "crop":
        cmd_crop(cfg, args.name)


if __name__ == "__main__":
    sys.exit(main())
