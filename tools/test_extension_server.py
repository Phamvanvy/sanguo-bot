"""Offline tests for the extension flow catalog and configurable macros."""
from __future__ import annotations

import unittest
from types import SimpleNamespace
from unittest.mock import MagicMock, call, patch

from src.config import PROJECT_ROOT
from src.extension_server import (
    adapt_canvas_region,
    expand_activation_codes,
    flow_catalog,
    is_game_url,
    run_blessing,
    run_code_redeem,
    run_macro,
    run_worker,
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


class ExtensionServerTest(unittest.TestCase):
    def test_game_url_accepts_only_the_expected_https_host(self):
        self.assertTrue(is_game_url("https://play.minhchauh5.com/"))
        self.assertTrue(is_game_url("https://play.minhchauh5.com/server/1"))
        self.assertFalse(is_game_url("http://play.minhchauh5.com/"))
        self.assertFalse(is_game_url("https://play.minhchauh5.com.evil.example/"))

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
        attach.assert_called_once_with(cfg, "Minh Châu H5")
        adapt.assert_called_once()
        session.set_dry_run.assert_called_once_with(False)
        session.close.assert_called_once_with()

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
        self.assertIn("typeof PointerEvent", content)
        self.assertIn("new KeyboardEvent", content)
        self.assertIn('["left", "Ô trái"]', content)
        self.assertIn('["right", "Ô phải"]', content)
        self.assertIn('(macro.item_points || {})[itemSlot]', content)
        self.assertIn('await domClick(token, attackPoint)', content)
        self.assertIn('const BLESSING_SPEED_FACTOR = 1.0', content)
        self.assertIn('document.getElementById("__mch5_guard")', content)
        self.assertIn('Rớt do guard: WebSocket 4001/guard', content)
        self.assertIn('macro.rest_every_cycles || 10', content)
        self.assertIn('"world": "MAIN"', manifest)
        self.assertIn('"run_at": "document_start"', manifest)
        self.assertIn('socket.addEventListener("close"', probe)
        self.assertIn('code: Number(event.code)', probe)

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
