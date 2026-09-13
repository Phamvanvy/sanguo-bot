"""Offline tests for the extension flow catalog and configurable macros."""
from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import MagicMock, call, patch

import src.extension_server as extension_server
from src.config import PROJECT_ROOT
from src.extension_server import (
    _refresh_status,
    adapt_canvas_region,
    expand_activation_codes,
    flow_catalog,
    is_game_url,
    log_network_event,
    run_blessing,
    run_code_redeem,
    run_macro,
    run_worker,
    start_flow,
    stop_flow,
)
from src.os_input import attach_existing


class FakeControl:
    def __init__(self):
        self.actions = []

    def click(self, x, y):
        self.actions.append(("click", x, y))

    def press(self, key):
        self.actions.append(("press", key))

    def type_text(self, text):
        self.actions.append(("type", text))


class FakeSession:
    def client_rect(self):
        return SimpleNamespace(width=1920, height=1000)


class FakeWindowCaptureSession(FakeSession):
    window_capture = True

    def client_rect(self):
        return SimpleNamespace(left=10, top=20, width=1920, height=1000)

    def renderer_rect(self):
        return SimpleNamespace(left=14, top=100, width=1911, height=915)


class ExtensionServerTest(unittest.TestCase):
    def test_game_url_accepts_only_the_expected_https_host(self):
        self.assertTrue(is_game_url("https://play.minhchauh5.com/"))
        self.assertTrue(is_game_url("https://play.minhchauh5.com/server/1"))
        self.assertFalse(is_game_url("http://play.minhchauh5.com/"))
        self.assertFalse(is_game_url("https://play.minhchauh5.com.evil.example/"))

    @patch("src.extension_server.NETWORK_LOG_PATH")
    def test_network_event_is_written_as_json_line(self, log_path):
        log_path.parent.mkdir = MagicMock()
        handle = MagicMock()
        log_path.open.return_value.__enter__.return_value = handle
        event = log_network_event({
            "type": "ws_close", "code": 1006, "reason": "", "cycle": 77,
            "step": "result_wait", "navigatorOnline": True, "extensionVersion": "0.4.1",
        })
        self.assertEqual(1006, event["code"])
        self.assertEqual(77, event["cycle"])
        self.assertEqual("result_wait", event["step"])
        self.assertTrue(event["navigator_online"])
        self.assertEqual("0.4.1", event["extension_version"])
        written = handle.write.call_args.args[0]
        self.assertEqual(1006, json.loads(written)["code"])

    @patch("src.os_input.OsGameSession.focus")
    @patch("src.os_input._find_window", return_value=12345)
    def test_attach_existing_targets_selected_title_without_launching(self, find_window, _focus):
        cfg = {"game": {"os_input": {
            "browser_exe": "C:/Program Files/BraveSoftware/Brave-Browser/Application/brave.exe",
            "window_title_hint": "fallback",
        }}}
        session = attach_existing(cfg, "Minh Châu H5")
        self.assertEqual(12345, session.hwnd)
        self.assertIsNone(session.process)
        find_window.assert_called_once_with(
            "Minh Châu H5",
            process_exe=cfg["game"]["os_input"]["browser_exe"],
            timeout=3.0,
        )

    @patch("src.os_input.OsGameSession.focus")
    @patch("src.os_input._find_window", side_effect=[TimeoutError, 54321])
    def test_attach_existing_falls_back_from_brave_to_edge(self, find_window, _focus):
        brave = "C:/Program Files/BraveSoftware/Brave-Browser/Application/brave.exe"
        edge = "C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"
        cfg = {"game": {"os_input": {
            "browser_exe": brave,
            "browser_exe_fallbacks": [edge],
            "window_title_hint": "fallback",
        }}}

        session = attach_existing(cfg, "Minh Châu H5")

        self.assertEqual(54321, session.hwnd)
        self.assertEqual([
            call("Minh Châu H5", process_exe=brave, timeout=3.0),
            call("Minh Châu H5", process_exe=edge, timeout=3.0),
        ], find_window.call_args_list)

    @patch("src.os_input.OsGameSession.focus")
    @patch("src.os_input._find_window", return_value=12345)
    def test_background_attach_does_not_take_foreground_focus(self, _find_window, focus):
        cfg = {"game": {"os_input": {
            "browser_exe": "edge.exe",
            "background_clicks": True,
            "window_title_hint": "Minh Chau H5",
        }}}

        session = attach_existing(cfg)

        self.assertTrue(session.background_clicks)
        focus.assert_not_called()

    def test_worker_forces_os_input_on_the_selected_game_window(self):
        cfg = {
            "runtime": {"dry_run": True},
            "game": {"control_mode": "cdp_attach", "os_input": {}},
        }
        session = MagicMock()
        with (
            patch("src.extension_server.load_config", return_value=cfg),
            patch("src.extension_server.flow_catalog", return_value=[{"id": "accept_quests"}]),
            patch("src.extension_server.attach_existing", return_value=session) as attach,
            patch("src.extension_server.adapt_canvas_region") as adapt,
            patch("src.extension_server.GameActions") as actions,
        ):
            actions.return_value.accept_all_map_quests.return_value = 0
            self.assertEqual(0, run_worker("accept_quests", "Minh Châu H5"))

        self.assertEqual("os_input", cfg["game"]["control_mode"])
        attach.assert_called_once_with(cfg, "Minh Châu H5", process_name=None)
        adapt.assert_called_once()
        session.set_dry_run.assert_called_once_with(False)
        session.close.assert_called_once_with()

    def test_worker_targets_the_requested_browser_window_token(self):
        cfg = {
            "runtime": {"dry_run": True},
            "game": {"control_mode": "os_input", "os_input": {}},
        }
        session = MagicMock()
        with (
            patch("src.extension_server.load_config", return_value=cfg),
            patch("src.extension_server.flow_catalog", return_value=[{"id": "accept_quests"}]),
            patch("src.extension_server.attach_existing", return_value=session) as attach,
            patch("src.extension_server.adapt_canvas_region"),
            patch("src.extension_server.GameActions") as actions,
        ):
            actions.return_value.accept_all_map_quests.return_value = 0
            run_worker(
                "accept_quests", "Minh Châu H5",
                browser_process="msedge.exe", window_token="SG-edge-1",
            )

        attach.assert_called_once_with(cfg, "SG-edge-1", process_name="msedge.exe")

    def test_controller_runs_and_stops_independent_window_sessions(self):
        first = MagicMock(pid=101)
        first.poll.return_value = None
        second = MagicMock(pid=202)
        second.poll.return_value = None
        with tempfile.TemporaryDirectory() as temp_dir:
            with (
                patch.object(extension_server, "_workers", {}),
                patch.object(extension_server, "_last_results", {}),
                patch.object(extension_server, "LOG_PATH", Path(temp_dir) / "flow.log"),
                patch("src.extension_server.subprocess.Popen", side_effect=[first, second]),
            ):
                edge = start_flow(
                    "full_auto", "Minh Châu H5", "https://play.minhchauh5.com/",
                    session_id="edge-window-1", browser_process="msedge.exe", window_token="SG-edge-1",
                )
                brave = start_flow(
                    "full_auto", "Minh Châu H5", "https://play.minhchauh5.com/",
                    session_id="brave-window-2", browser_process="brave.exe", window_token="SG-brave-2",
                )

                self.assertEqual(101, edge["pid"])
                self.assertEqual(202, brave["pid"])
                self.assertEqual(2, _refresh_status()["runningCount"])
                stopped = stop_flow("edge-window-1")

                self.assertEqual("stopped", stopped["state"])
                self.assertEqual("running", _refresh_status("brave-window-2")["state"])
                first.terminate.assert_called_once_with()
                second.terminate.assert_not_called()
                stop_flow("brave-window-2")

    def test_extension_protocol_includes_window_identity(self):
        background = (PROJECT_ROOT / "extension" / "background.js").read_text(encoding="utf-8")
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        manifest = json.loads((PROJECT_ROOT / "extension" / "manifest.json").read_text(encoding="utf-8"))

        self.assertIn("sessionId", background)
        self.assertIn("windowToken", background)
        self.assertIn("browserProcess", background)
        self.assertIn('"msedge.exe"', content)
        self.assertIn("storage", manifest["permissions"])

    def test_activation_code_ranges_expand_in_order(self):
        macro = {
            "codes": ["MCH5EXPH1-100"],
            "code_ranges": [
                {"prefix": "MCH5VIP", "start": 1, "end": 2},
                {"prefix": "MCH5TEST", "start": 1, "end": 3},
            ],
        }
        self.assertEqual(
            ["MCH5EXPH1-100", "MCH5VIP1", "MCH5VIP2", "MCH5TEST1", "MCH5TEST2", "MCH5TEST3"],
            expand_activation_codes(macro),
        )

    def test_canvas_pixel_insets_scale_for_maximized_window(self):
        cfg = {"game": {"os_input": {"canvas_insets_px": {
            "left": 4, "top": 80, "right": 5, "bottom": 5,
        }}}}
        control = FakeControl()
        control.session = FakeSession()
        adapt_canvas_region(control, cfg)
        region = cfg["game"]["os_input"]["canvas_region"]
        self.assertAlmostEqual(80 / 1000, region["y"])
        self.assertAlmostEqual(915 / 1000, region["h"])

    def test_canvas_uses_chromium_renderer_bounds_for_window_capture(self):
        cfg = {"game": {"os_input": {"canvas_insets_px": {
            "left": 999, "top": 999, "right": 999, "bottom": 999,
        }}}}
        control = FakeControl()
        control.session = FakeWindowCaptureSession()

        adapt_canvas_region(control, cfg)

        region = cfg["game"]["os_input"]["canvas_region"]
        self.assertAlmostEqual(4 / 1920, region["x"])
        self.assertAlmostEqual(80 / 1000, region["y"])
        self.assertAlmostEqual(1911 / 1920, region["w"])
        self.assertAlmostEqual(915 / 1000, region["h"])

    def test_canvas_uses_full_client_area_in_fullscreen(self):
        cfg = {"game": {"os_input": {"canvas_insets_px": {
            "left": 4, "top": 80, "right": 5, "bottom": 5,
        }}}}
        control = FakeControl()
        control.session = FakeSession()
        adapt_canvas_region(control, cfg, fullscreen=True)
        self.assertEqual(
            {"x": 0.0, "y": 0.0, "w": 1.0, "h": 1.0},
            cfg["game"]["os_input"]["canvas_region"],
        )

    def test_catalog_contains_core_and_configured_flows(self):
        cfg = {"activity_macros": {"blessing": {"label": "Cầu phúc", "steps": []}}}
        ids = {flow["id"] for flow in flow_catalog(cfg)}
        self.assertTrue({"full_auto", "accept_quests", "do_quests", "blessing"} <= ids)

    def test_default_catalog_contains_auto_attack(self):
        flows = {flow["id"]: flow for flow in flow_catalog()}
        self.assertEqual("auto_attack_loop", flows["auto_attack"]["runner"])

    def test_default_catalog_omits_code_redeem(self):
        flows = {flow["id"]: flow for flow in flow_catalog()}
        self.assertNotIn("mch5exp_redeem", flows)
        config = (PROJECT_ROOT / "config.yaml").read_text(encoding="utf-8")
        self.assertNotIn("mch5exp_redeem:", config)
        self.assertNotIn('codes: ["MCH5EXP"]', config)

    def test_default_catalog_contains_looping_star_reappraisal(self):
        flows = {flow["id"]: flow for flow in flow_catalog()}
        self.assertEqual("star_reappraisal_loop", flows["star_reappraisal"]["runner"])
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        self.assertIn('async function runStarReappraisal', content)
        self.assertIn('flow === "star_reappraisal"', content)
        self.assertIn('macro.star_button_point || [0.227, 0.869]', content)
        self.assertIn('macro.reappraise_point || [0.498, 0.756]', content)
        self.assertIn('macro.confirm_point || [0.499, 0.693]', content)

    def test_default_catalog_contains_one_shot_mount_skill_learning(self):
        flows = {flow["id"]: flow for flow in flow_catalog()}
        self.assertEqual("mount_skill_learn_once", flows["mount_skill_learn"]["runner"])
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        self.assertIn('async function runMountSkillLearnOnce', content)
        self.assertIn('flow === "mount_skill_learn"', content)
        self.assertIn('macro.book_point || [0.289, 0.807]', content)
        self.assertIn('macro.learn_point || [0.703, 0.224]', content)
        self.assertIn('macro.confirm_point || [0.696, 0.628]', content)
        one_shot = content[
            content.index("async function runMountSkillLearnOnce"):
            content.index("async function runAutoAttack")
        ]
        self.assertNotIn("for (", one_shot)
        self.assertNotIn("while (", one_shot)

    def test_default_catalog_has_no_gem_upgrade(self):
        flows = {flow["id"]: flow for flow in flow_catalog()}
        self.assertNotIn("gem_upgrade", flows)
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        self.assertNotIn("gem_upgrade", content)
        self.assertNotIn("runGemUpgradeSequence", content)

    def test_inventory_left_batches_use_99_then_sort_forever(self):
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        self.assertIn('label: "Ô trái ×99 + sắp xếp"', content)
        self.assertIn('overrides: { item_slot: "left", auto_sort_batches: true }', content)
        runner = content[
            content.index("async function runUseInventoryItem"):
            content.index("async function runCoinShake")
        ]
        self.assertIn('Number(macro.max_cycles ?? 99)', runner)
        self.assertIn('Number(macro.max_batches ?? 0)', runner)
        self.assertIn('for (let cycle = 0; cycle < batchSize; cycle += 1)', runner)
        self.assertIn('macro.batch_sort_point || [0.813, 0.917]', runner)
        config = (PROJECT_ROOT / "config.yaml").read_text(encoding="utf-8")
        self.assertIn('max_cycles: 99', config)
        self.assertIn('max_batches: 0', config)

    def test_activity_macros_route_to_dom_extension_runner(self):
        extension_dir = PROJECT_ROOT / "extension"
        content = (extension_dir / "content.js").read_text(encoding="utf-8")
        background = (extension_dir / "background.js").read_text(encoding="utf-8")
        manifest = (extension_dir / "manifest.json").read_text(encoding="utf-8")
        probe = (extension_dir / "network_probe.js").read_text(encoding="utf-8")
        for runner in (
            "blessing_loop", "code_redeem_loop", "discard_loop", "use_item_loop", "coin_shake_loop",
            "auto_attack_loop",
        ):
            self.assertIn(f'"{runner}"', content)
        for flow in (
            "blessing", "code_redeem", "discard_items", "use_inventory_item", "coin_shake", "auto_attack",
        ):
            self.assertIn(f'flow === "{flow}"', content)
        self.assertNotIn('type: "run-native"', content)
        self.assertNotIn('"debugger"', manifest)
        self.assertNotIn("chrome.debugger", background)
        self.assertIn('"version": "0.5.0"', manifest)
        self.assertIn("typeof PointerEvent", content)
        self.assertIn("new KeyboardEvent", content)
        self.assertIn('label: "Ô trái", overrides: { item_slot: "left" }', content)
        self.assertIn('label: "Ô phải", overrides: { item_slot: "right" }', content)
        self.assertIn('(macro.item_points || {})[itemSlot]', content)
        self.assertIn('await domClick(token, attackPoint)', content)
        self.assertIn('const BLESSING_SPEED_FACTOR = 1.0', content)
        self.assertIn('async function runBlessing', content)
        self.assertIn('flow === "blessing"', content)
        dom_click = content[content.index("async function domClick"):content.index("function dispatchKey")]
        self.assertIn('dispatchMouse(target, "mousedown"', dom_click)
        self.assertIn('dispatchMouse(target, "mouseup"', dom_click)
        self.assertNotIn('"pointerdown"', dom_click)
        self.assertNotIn('"pointerup"', dom_click)
        self.assertNotIn('"mousemove"', dom_click)
        self.assertNotIn('"click"', dom_click)
        self.assertIn('document.getElementById("__mch5_guard")', content)
        self.assertIn('Rớt do guard: WebSocket 4001/guard', content)
        self.assertIn('"world": "MAIN"', manifest)
        self.assertIn('"run_at": "document_start"', manifest)
        self.assertIn('socket.addEventListener("close"', probe)
        self.assertIn('code: Number(event.code)', probe)
        self.assertIn('const EVENT_QUEUE_KEY = "sanguo-network-event-queue"', probe)
        self.assertIn('queue.slice(-100)', probe)
        self.assertIn('addEventListener("offline"', probe)
        self.assertIn('addEventListener("pagehide"', probe)
        self.assertIn('addEventListener("unhandledrejection"', probe)
        self.assertIn('extensionVersion: chrome.runtime.getManifest().version', content)
        self.assertIn('controller.diagnosticsVersion !== 1', content)
        self.assertIn('Controller cũ: hãy restart', content)
        self.assertIn('["ws_open", "ws_close"].includes(event.type)', content)
        self.assertIn('Flow đã tự dừng.', content)
        self.assertIn('startedAt: Date.now()', content)
        self.assertIn('new RTCPeerConnection({ iceServers: [] })', content)
        self.assertIn('timer_keepalive_open', content)
        self.assertIn('stopTimerKeepAlive();', content)

    @patch("src.extension_server.time.sleep", return_value=None)
    def test_macro_runs_click_and_key_steps(self, _sleep):
        cfg = {"activity_macros": {"daily": {"steps": [
            {"click": [0.2, 0.3], "wait": 0},
            {"press": "escape", "wait": 0},
        ]}}}
        control = FakeControl()
        run_macro(control, cfg, "daily")
        self.assertEqual([("click", 0.2, 0.3), ("press", "escape")], control.actions)

    @patch("src.extension_server.time.sleep", return_value=None)
    def test_blessing_first_cycle_clicks_once_then_later_twice(self, _sleep):
        cfg = {"activity_macros": {"blessing": {
            "open_point": [0.73, 0.07],
            "ten_times_point": [0.66, 0.84],
            "ok_point": [0.70, 0.64],
            "max_cycles": 2,
        }}}
        control = FakeControl()
        run_blessing(control, cfg)
        self.assertEqual([
            ("click", 0.73, 0.07),
            ("click", 0.66, 0.84),
            ("click", 0.70, 0.64),
            ("click", 0.66, 0.84),
            ("click", 0.66, 0.84),
            ("click", 0.70, 0.64),
        ], control.actions)

    @patch("src.extension_server.time.sleep")
    def test_blessing_takes_a_periodic_rest(self, sleep):
        cfg = {"activity_macros": {"blessing": {
            "max_cycles": 2,
            "rest_every_cycles": 2,
            "rest_delay_seconds": 5,
        }}}
        run_blessing(FakeControl(), cfg)
        self.assertIn(call(5.0), sleep.call_args_list)

    @patch("src.extension_server.time.sleep", return_value=None)
    def test_code_redeem_reopens_npc_for_each_code(self, _sleep):
        cfg = {"activity_macros": {"code_redeem": {
            "codes": ["CODE1", "CODE2"],
            "npc_point": [0.1, 0.2],
            "option_point": [0.3, 0.4],
            "input_point": [0.5, 0.6],
            "submit_point": [0.7, 0.8],
        }}}
        control = FakeControl()
        run_code_redeem(control, cfg)
        self.assertEqual(2, control.actions.count(("click", 0.1, 0.2)))
        self.assertIn(("type", "CODE1"), control.actions)
        self.assertIn(("type", "CODE2"), control.actions)


if __name__ == "__main__":
    unittest.main()
