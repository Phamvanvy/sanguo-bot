"""Local HTTP controller used by the Brave/Chrome extension popup.

The extension is deliberately only a UI. Each flow runs in a separate Python
process and uses the control backend selected in config.yaml.
"""
from __future__ import annotations

import argparse
import json
import subprocess
import sys
import threading
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Any
from urllib.parse import parse_qs, urlparse

from src.capture import GameControl
from src.config import PROJECT_ROOT, load_config
from src.game.actions import GameActions
from src.game.quests import QuestExecutor
from src.os_input import attach_existing


HOST = "127.0.0.1"
PORT = 8765
DIAGNOSTICS_VERSION = 1
LOG_PATH = PROJECT_ROOT / "logs" / "extension-flow.log"
NETWORK_LOG_PATH = PROJECT_ROOT / "logs" / "extension-network.log"

_lock = threading.Lock()
_process: subprocess.Popen | None = None
_log_handle = None
_active_flow: str | None = None
_last_result: dict[str, Any] = {"state": "idle", "message": "Chưa chạy flow nào"}


def log_network_event(data: dict[str, Any]) -> dict[str, Any]:
    event = {
        "received_at": time.strftime("%Y-%m-%dT%H:%M:%S%z"),
        "type": str(data.get("type", "unknown"))[:32],
        "code": int(data.get("code", 0) or 0),
        "reason": str(data.get("reason", ""))[:200],
        "clean": bool(data.get("clean", False)),
        "url": str(data.get("url", ""))[:500],
        "browser_at": int(data.get("at", 0) or 0),
        "flow": str(data.get("flow", ""))[:64],
        "cycle": int(data.get("cycle", 0) or 0),
        "step": str(data.get("step", ""))[:100],
        "event_id": str(data.get("id", ""))[:100],
        "page_session": str(data.get("pageSession", ""))[:100],
        "page_url": str(data.get("pageUrl", ""))[:500],
        "started_at": int(data.get("startedAt", 0) or 0),
        "updated_at": int(data.get("updatedAt", 0) or 0),
        "flow_elapsed_ms": max(
            0,
            int(data.get("updatedAt", 0) or 0) - int(data.get("startedAt", 0) or 0),
        ),
        "navigator_online": bool(data.get("navigatorOnline", True)),
        "visibility": str(data.get("visibility", ""))[:32],
        "document_state": str(data.get("documentState", ""))[:32],
        "performance_ms": int(data.get("perfMs", 0) or 0),
        "persisted": bool(data.get("persisted", False)),
        "message": str(data.get("message", ""))[:500],
        "filename": str(data.get("filename", ""))[:500],
        "line": int(data.get("line", 0) or 0),
        "column": int(data.get("column", 0) or 0),
        "extension_version": str(data.get("extensionVersion", ""))[:32],
    }
    NETWORK_LOG_PATH.parent.mkdir(parents=True, exist_ok=True)
    with _lock:
        with NETWORK_LOG_PATH.open("a", encoding="utf-8") as handle:
            handle.write(json.dumps(event, ensure_ascii=False) + "\n")
    return event


def is_game_url(value: str) -> bool:
    parsed = urlparse(value)
    return parsed.scheme == "https" and parsed.hostname == "play.minhchauh5.com"


def flow_catalog(cfg: dict | None = None) -> list[dict[str, str]]:
    cfg = cfg or load_config()
    flows = [
        {"id": "full_auto", "label": "Full auto", "description": "Gom rồi làm toàn bộ nhiệm vụ", "icon": "⚡"},
        {"id": "accept_quests", "label": "Gom nhiệm vụ", "description": "Nhận nhiều nhiệm vụ từ dấu !", "icon": "!"},
        {"id": "do_quests", "label": "Làm nhiệm vụ", "description": "Làm các nhiệm vụ đã nhận", "icon": "✓"},
    ]
    for flow_id, macro in cfg.get("activity_macros", {}).items():
        flows.append({
            "id": flow_id,
            "label": str(macro.get("label", flow_id)),
            "description": str(macro.get("description", "Hoạt động nhanh")),
            "icon": str(macro.get("icon", "◆")),
            "runner": str(macro.get("runner", "macro")),
        })
    return flows


def run_macro(control: GameControl, cfg: dict, flow_id: str) -> None:
    macro = cfg.get("activity_macros", {}).get(flow_id)
    if not macro:
        raise ValueError(f"Unknown flow: {flow_id}")
    for step in macro.get("steps", []):
        if "press" in step:
            control.press(str(step["press"]))
        elif "click" in step:
            x, y = step["click"]
            control.click(float(x), float(y))
        time.sleep(float(step.get("wait", 0.5)))


def run_blessing(control: GameControl, cfg: dict, flow_id: str = "blessing") -> None:
    """Repeat the fixed Cầu phúc 10 lần → OK interaction until stopped."""
    macro = cfg.get("activity_macros", {}).get(flow_id)
    if not macro:
        raise ValueError(f"Unknown flow: {flow_id}")
    open_point = macro.get("open_point", [0.73, 0.07])
    ten_point = macro.get("ten_times_point", [0.66, 0.84])
    ok_point = macro.get("ok_point", [0.70, 0.64])
    click_delay = float(macro.get("click_delay_seconds", 0.55))
    confirm_delay = float(macro.get("confirm_delay_seconds", 1.2))
    result_delay = float(macro.get("result_delay_seconds", 1.5))
    rest_every = int(macro.get("rest_every_cycles", 10))
    rest_delay = float(macro.get("rest_delay_seconds", 5.0))
    max_cycles = int(macro.get("max_cycles", 0))

    control.click(float(open_point[0]), float(open_point[1]))
    time.sleep(float(macro.get("open_delay_seconds", 1.5)))
    cycle = 0
    while max_cycles <= 0 or cycle < max_cycles:
        # First cycle opens the confirmation directly. Later cycles need one
        # click to dismiss the result notification, then another real click.
        clicks = 1 if cycle == 0 else 2
        for _ in range(clicks):
            control.click(float(ten_point[0]), float(ten_point[1]))
            time.sleep(click_delay)
        time.sleep(confirm_delay)
        control.click(float(ok_point[0]), float(ok_point[1]))
        cycle += 1
        print(f"[extension] blessing cycle={cycle}", flush=True)
        time.sleep(result_delay)
        if rest_every > 0 and cycle % rest_every == 0:
            print(f"[extension] blessing resting after cycle={cycle}", flush=True)
            time.sleep(rest_delay)


def expand_activation_codes(macro: dict) -> list[str]:
    """Expand literal codes and numeric prefix ranges in config order."""
    codes = [str(code).strip() for code in macro.get("codes", []) if str(code).strip()]
    for item in macro.get("code_ranges", []):
        prefix = str(item.get("prefix", ""))
        start = int(item.get("start", 1))
        end = int(item.get("end", 0))
        codes.extend(f"{prefix}{number}" for number in range(start, end + 1))
    return codes


def run_code_redeem(control: GameControl, cfg: dict, flow_id: str = "code_redeem") -> None:
    """Open the activation-code NPC and submit every configured code."""
    macro = cfg.get("activity_macros", {}).get(flow_id)
    if not macro:
        raise ValueError(f"Unknown flow: {flow_id}")
    codes = expand_activation_codes(macro)
    npc_point = macro.get("npc_point", [0.29, 0.39])
    option_point = macro.get("option_point", [0.50, 0.43])
    input_point = macro.get("input_point", [0.50, 0.51])
    submit_point = macro.get("submit_point", [0.50, 0.57])
    open_delay = float(macro.get("open_delay_seconds", 1.0))
    option_delay = float(macro.get("option_delay_seconds", 1.0))
    submit_delay = float(macro.get("submit_delay_seconds", 1.4))
    dismiss_delay = float(macro.get("dismiss_delay_seconds", 0.8))

    for index, code in enumerate(codes, 1):
        control.click(float(npc_point[0]), float(npc_point[1]))
        time.sleep(open_delay)
        control.click(float(option_point[0]), float(option_point[1]))
        time.sleep(option_delay)
        control.click(float(input_point[0]), float(input_point[1]))
        control.type_text(code)
        control.click(float(submit_point[0]), float(submit_point[1]))
        time.sleep(submit_delay)
        control.press(str(macro.get("notification_dismiss_key", "enter")))
        time.sleep(dismiss_delay)
        control.press("escape")
        time.sleep(dismiss_delay)
        print(f"[extension] code {index}/{len(codes)} submitted: {code}", flush=True)


def adapt_canvas_region(control: GameControl, cfg: dict, fullscreen: bool = False) -> None:
    """Convert constant browser-chrome pixel insets for the current window size."""
    if fullscreen:
        cfg["game"]["os_input"]["canvas_region"] = {"x": 0.0, "y": 0.0, "w": 1.0, "h": 1.0}
        return
    insets = cfg.get("game", {}).get("os_input", {}).get("canvas_insets_px")
    if not insets:
        return
    rect = control.session.client_rect()
    left = max(0, int(insets.get("left", 0)))
    top = max(0, int(insets.get("top", 0)))
    right = max(0, int(insets.get("right", 0)))
    bottom = max(0, int(insets.get("bottom", 0)))
    if left + right >= rect.width or top + bottom >= rect.height:
        raise ValueError("canvas_insets_px is larger than the Brave client area")
    cfg["game"]["os_input"]["canvas_region"] = {
        "x": left / rect.width,
        "y": top / rect.height,
        "w": (rect.width - left - right) / rect.width,
        "h": (rect.height - top - bottom) / rect.height,
    }


def run_worker(flow_id: str, tab_title: str, fullscreen: bool = False) -> int:
    cfg = load_config()
    cfg["runtime"]["dry_run"] = False
    valid_ids = {flow["id"] for flow in flow_catalog(cfg)}
    if flow_id not in valid_ids:
        raise ValueError(f"Unknown flow: {flow_id}")

    # The live game deliberately closes its WebSocket when a debugger/CDP
    # client is detected. Extension workers must therefore stay on the
    # ordinary browser window selected by the user and use OS-level input,
    # regardless of the backend configured for standalone tools.
    cfg["game"]["control_mode"] = "os_input"
    control = GameControl(session=attach_existing(cfg, tab_title), cfg=cfg)
    adapt_canvas_region(control, cfg, fullscreen=fullscreen)
    control.set_dry_run(False)
    try:
        actions = GameActions(control, cfg)
        if flow_id == "full_auto":
            accepted = actions.accept_all_map_quests()
            print(f"[extension] accepted={accepted}", flush=True)
            completed = QuestExecutor(control, cfg).run_all_incomplete()
            print(f"[extension] completed={completed}", flush=True)
        elif flow_id == "accept_quests":
            accepted = actions.accept_all_map_quests()
            print(f"[extension] accepted={accepted}", flush=True)
        elif flow_id == "do_quests":
            completed = QuestExecutor(control, cfg).run_all_incomplete()
            print(f"[extension] completed={completed}", flush=True)
        elif cfg.get("activity_macros", {}).get(flow_id, {}).get("runner") == "blessing_loop":
            run_blessing(control, cfg, flow_id)
        elif cfg.get("activity_macros", {}).get(flow_id, {}).get("runner") == "code_redeem_loop":
            run_code_redeem(control, cfg, flow_id)
        else:
            run_macro(control, cfg, flow_id)
            print(f"[extension] macro={flow_id} done", flush=True)
        return 0
    finally:
        control.close()


def _refresh_status() -> dict[str, Any]:
    global _process, _log_handle, _last_result
    with _lock:
        if _process is not None:
            code = _process.poll()
            if code is None:
                return {"state": "running", "flow": _active_flow, "pid": _process.pid}
            if _log_handle is not None:
                _log_handle.close()
                _log_handle = None
            _last_result = {
                "state": "done" if code == 0 else "error",
                "flow": _active_flow,
                "returncode": code,
                "message": "Flow hoàn tất" if code == 0 else "Flow bị lỗi; xem logs/extension-flow.log",
            }
            _process = None
        return dict(_last_result)


def start_flow(flow_id: str, tab_title: str, tab_url: str, fullscreen: bool = False) -> dict[str, Any]:
    global _process, _log_handle, _active_flow, _last_result
    _refresh_status()
    valid_ids = {flow["id"] for flow in flow_catalog()}
    if flow_id not in valid_ids:
        return {"ok": False, "error": "Flow không tồn tại"}
    if not tab_title.strip() or not is_game_url(tab_url):
        return {"ok": False, "error": "Hãy chọn tab https://play.minhchauh5.com/ trước khi chạy"}
    with _lock:
        if _process is not None:
            return {"ok": False, "error": "Một flow khác đang chạy"}
        LOG_PATH.parent.mkdir(parents=True, exist_ok=True)
        _log_handle = open(LOG_PATH, "a", encoding="utf-8")
        _log_handle.write(f"\n[{time.strftime('%Y-%m-%d %H:%M:%S')}] START {flow_id}\n")
        _log_handle.flush()
        flags = subprocess.CREATE_NO_WINDOW if sys.platform == "win32" else 0
        worker_args = [
                sys.executable,
                "-m",
                "src.extension_server",
                "--worker",
                flow_id,
                "--tab-title",
                tab_title,
            ]
        if fullscreen:
            worker_args.append("--fullscreen")
        _process = subprocess.Popen(
            worker_args,
            cwd=PROJECT_ROOT,
            stdout=_log_handle,
            stderr=subprocess.STDOUT,
            creationflags=flags,
        )
        _active_flow = flow_id
        _last_result = {"state": "running", "flow": flow_id, "pid": _process.pid}
        return {"ok": True, **_last_result}


def stop_flow() -> dict[str, Any]:
    global _process, _log_handle, _last_result
    with _lock:
        process = _process
    if process is None or process.poll() is not None:
        _refresh_status()
        return {"ok": True, "state": "idle", "message": "Không có flow đang chạy"}
    process.terminate()
    try:
        process.wait(timeout=3)
    except subprocess.TimeoutExpired:
        process.kill()
        process.wait(timeout=2)
    with _lock:
        if _log_handle is not None:
            _log_handle.close()
            _log_handle = None
        _process = None
        _last_result = {"state": "stopped", "flow": _active_flow, "message": "Đã dừng flow"}
    return {"ok": True, **_last_result}


class ExtensionHandler(BaseHTTPRequestHandler):
    server_version = "SanguoExtension/1.0"

    def _origin(self) -> str | None:
        origin = self.headers.get("Origin")
        return origin if origin and origin.startswith("chrome-extension://") else None

    def _send(self, status: int, payload: Any) -> None:
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        origin = self._origin()
        if origin:
            self.send_header("Access-Control-Allow-Origin", origin)
            self.send_header("Vary", "Origin")
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_OPTIONS(self) -> None:  # noqa: N802
        if not self._origin():
            self._send(403, {"error": "Extension origin required"})
            return
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", self._origin())
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.end_headers()

    def do_GET(self) -> None:  # noqa: N802
        parsed = urlparse(self.path)
        if parsed.path == "/api/flows":
            self._send(200, {"flows": flow_catalog()})
        elif parsed.path == "/api/status":
            self._send(200, {**_refresh_status(), "diagnosticsVersion": DIAGNOSTICS_VERSION})
        elif parsed.path == "/api/macro":
            flow_id = parse_qs(parsed.query).get("id", [""])[0]
            macro = load_config().get("activity_macros", {}).get(flow_id)
            if not macro:
                self._send(404, {"error": "Flow không tồn tại"})
            else:
                self._send(200, {"id": flow_id, "macro": macro})
        else:
            self._send(404, {"error": "Not found"})

    def do_POST(self) -> None:  # noqa: N802
        if not self._origin():
            self._send(403, {"error": "Extension origin required"})
            return
        length = int(self.headers.get("Content-Length", "0"))
        try:
            data = json.loads(self.rfile.read(length) or b"{}")
        except json.JSONDecodeError:
            self._send(400, {"error": "Invalid JSON"})
            return
        if self.path == "/api/run":
            result = start_flow(
                str(data.get("flow", "")),
                str(data.get("tabTitle", "")),
                str(data.get("tabUrl", "")),
                bool(data.get("fullscreen", False)),
            )
            self._send(200 if result.get("ok") else 409, result)
        elif self.path == "/api/stop":
            self._send(200, stop_flow())
        elif self.path == "/api/network-event":
            self._send(200, {"ok": True, "event": log_network_event(data)})
        else:
            self._send(404, {"error": "Not found"})

    def log_message(self, format: str, *args) -> None:
        return


def serve(port: int = PORT) -> None:
    log_network_event({"type": "controller_start"})
    server = ThreadingHTTPServer((HOST, port), ExtensionHandler)
    print(f"Sanguo extension controller: http://{HOST}:{port}", flush=True)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        stop_flow()
        server.server_close()


def main() -> None:
    parser = argparse.ArgumentParser(description="Sanguo extension local controller")
    parser.add_argument("--port", type=int, default=PORT)
    parser.add_argument("--worker", metavar="FLOW")
    parser.add_argument("--tab-title", default="")
    parser.add_argument("--fullscreen", action="store_true")
    args = parser.parse_args()
    if args.worker:
        if not args.tab_title:
            parser.error("--tab-title is required with --worker")
        raise SystemExit(run_worker(args.worker, args.tab_title, fullscreen=args.fullscreen))
    serve(args.port)


if __name__ == "__main__":
    main()
