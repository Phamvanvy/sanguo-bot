"""Offline tests for the extension flow catalog and configurable macros."""
from __future__ import annotations

import json
import shutil
import subprocess
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
        self.assertTrue({"full_auto", "blessing"} <= ids)
        # The panel no longer offers the separate gather / do quest flows.
        self.assertFalse({"accept_quests", "do_quests"} & ids)

    def test_default_catalog_contains_auto_attack(self):
        flows = {flow["id"]: flow for flow in flow_catalog()}
        self.assertEqual("auto_attack_loop", flows["auto_attack"]["runner"])
        # The Đánh button is no longer pressed (user, 2026-09-21): a round is
        # the skill buttons only, and content.js presses Đánh solely for a
        # macro that still carries an attack_point.
        macros = extension_server.load_config()["activity_macros"]
        for flow_id in ("auto_attack", "co_mo_hard", "ha_dong_hard", "ha_dong_easy",
                        "thien_long_hard", "thien_long_easy"):
            self.assertNotIn("attack_point", macros[flow_id])
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        self.assertIn("if (macro.attack_point) {", content)
        self.assertNotIn("macro.attack_point || [0.927, 0.822]", content)

    def test_default_catalog_contains_instance_reset_command(self):
        """Reset p.bản goes out as the game's own packet, not a click path."""
        flows = {flow["id"]: flow for flow in flow_catalog()}
        self.assertEqual("instance_reset_once", flows["instance_reset"]["runner"])
        macro = extension_server.load_config()["activity_macros"]["instance_reset"]
        self.assertEqual(8, macro["reply_seconds"])
        # Nothing about this flow touches the UI: no points to click at all.
        self.assertFalse([key for key in macro if key.endswith("_point")])
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        probe = (PROJECT_ROOT / "extension" / "network_probe.js").read_text(encoding="utf-8")
        # OpCode.INSTANCE_CLEAR_CLIENT, body = one int serial.
        self.assertIn("const OP_INSTANCE_CLEAR = 534;", content)
        self.assertIn("instance_reset_once", content)
        # Only the page can reach the game socket, so the packet goes through
        # the probe and the flow waits for the answer to its own serial.
        self.assertIn('source: "sanguo-send"', content)
        self.assertIn('event.data?.source !== "sanguo-send"', probe)
        self.assertIn("item.serial === serial", content)
        # UA frame: 'U' | 'A' | int32 total | int16 opcode | int32 serial.
        self.assertIn("frame[0] = 0x55;", probe)
        self.assertIn("frame[1] = 0x41;", probe)
        self.assertIn("const total = 8 + body.reduce(", probe)
        self.assertIn('[["i32", serial]]', content)
        # The game's answers: 535 done, ERROR (-1) carries the refusal text.
        self.assertIn("const OP_INSTANCE_CLEAR_SERVER = 535;", probe)
        self.assertIn("const OP_ERROR = 0xffff;", probe)
        self.assertIn("Game từ chối:", content)

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

    def test_default_catalog_contains_co_mo_hard_dungeon_route(self):
        flows = {flow["id"]: flow for flow in flow_catalog()}
        macros = extension_server.load_config()["activity_macros"]
        macro = macros["co_mo_hard"]
        self.assertEqual("dungeon_route", macro["runner"])
        self.assertEqual(macros["auto_attack"]["skill_points"], macro["skill_points"])
        # Hard goes in through the NPC, clicked on the lobby's Map (user,
        # 2026-09-23); easy walks into the door at 59,11.
        self.assertEqual(1, len(macro["entry_steps"]))
        self.assertEqual("Đặc sứ triều đình Đông Hán", macro["entry_steps"][0]["touch_npc_by_map"])
        self.assertEqual([0.5, 0.509], macro["entry_steps"][0]["option_point"])
        easy = macros["co_mo_easy"]
        self.assertEqual([[59, 11]], [step["goto"] for step in easy["entry_steps"]])
        self.assertTrue(easy["entry_steps"][0]["portal"])
        self.assertEqual(macro["route_steps"], easy["route_steps"])
        route = macro["route_steps"]
        # Three bosses in three rooms; each door stays shut while monsters attack.
        # None: boss 1's room is entered fighting on the spot (the monster at
        # its door), then the middle (user, 2026-09-23).
        self.assertEqual(
            [[38, 12], [44, 10], None, [20, 18], [31, 24], [90, 30], [83, 34], [44, 34], [9, 10],
             [99, 4], [93, 6], [69, 23], [79, 21]],
            [step.get("goto") for step in route],
        )
        # Wide enough for the "Xe đâm húc" ~102 px from dễ's door (773), short
        # of khó's statue mid-room at 155 px (2026-09-24).
        self.assertEqual([128, 128], [route[2]["monster_radius"], route[2]["idle_monster_radius"]])
        # Both clear-before-door steps sweep idle adds out to 250 px: a "Xe đâm
        # húc" at 175-210 px was left and shut door 44,10 (user, 2026-09-23).
        self.assertEqual([250, 250], [route[i]["idle_monster_radius"] for i in (0, 5)])
        self.assertEqual(6, sum(1 for step in route if "fight_seconds" in step))
        # ...and out of boss 3's room at 79,21 back to the lobby (user, 2026-09-23).
        self.assertEqual([[44, 10], [31, 24], [83, 34], [9, 10], [93, 6], [79, 21]],
                         [step["goto"] for step in route if step.get("portal")])
        self.assertTrue(all(step.get("ignore_ambush") for step in route[9:11]))
        self.assertTrue(all(len(step["map_size"]) == 2 for step in route))
        for step in macro["entry_steps"] + route:
            self.assertTrue({"goto", "map_point", "click_point", "fight_seconds", "touch_npc_by_map"} & step.keys(), step)
        # Only the one Cổ Mộ card shows, counts for hard and easy; each step
        # names its maps so a run resumes where we stand.
        self.assertNotIn("co_mo_easy", flows)
        self.assertNotIn("co_mo_hard", flows)
        self.assertEqual([
            {"macro": "co_mo_hard", "label": "Khó", "times": 5},
            {"macro": "co_mo_easy", "label": "Dễ", "times": 5},
        ], flows["co_mo_pipeline"]["stages"])
        self.assertTrue(all(step.get("maps") for step in route))
        self.assertEqual(768, macro["entry_map"])
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        self.assertIn('"dungeon_route"', content)
        self.assertIn('async function runDungeonRoute', content)
        self.assertIn('macro.runner === "dungeon_route"', content)
        route_runner = content[
            content.index("async function runDungeonRoute"):
            content.index("async function startDomFlow")
        ]
        for event_type in ("dungeon_start", "dungeon_step", "dungeon_step_done"):
            self.assertIn(f'appendDiagnostic("{event_type}"', route_runner)
        self.assertIn("return { target, x, y };", content)
        # The Map panel does not close itself and the close key does not reach
        # it: every map step must name its X button.
        self.assertIn("step.map_close_point", route_runner)
        self.assertIn("thiếu map_close_point", route_runner)
        self.assertIn("thiếu map_size", route_runner)
        self.assertIn("async function walkToCoord", route_runner)
        self.assertNotIn("map_close_key", content)
        self.assertNotIn("map_close_key", macro)
        map_steps = [s for s in route if "map_point" in s]
        self.assertTrue(all("map_close_point" in s for s in map_steps), map_steps)
        # Fights end on the red-arrow check, read from game data.
        self.assertTrue(macro["combat_check"])
        # Walking never presses Đánh: the character would stop to fight.
        self.assertIs(False, macro["fight_on_the_way"])
        self.assertIn("macro.fight_on_the_way === true", route_runner)
        # A portal counts as crossed only when the map id really changes.
        self.assertIn("mapSwitchedAt > mapMark", route_runner)
        self.assertIn("mapSwitchedAt > since", route_runner)
        # Walks that keep ending on the same spot report where they are blocked.
        self.assertIn('how: "blocked"', route_runner)
        # A companion general fights beside us and must never count as a monster.
        self.assertIn("Lữ Bố", macro["ignore_monster_names"])
        # A monster attacking us is never written off as "behind a wall": it has
        # plainly reached us, and dropping it leaves a door shut (user,
        # 2026-09-23, Thiên Long's exit).
        self.assertIn("const reachable = (monster) => engaged(monster) || onFoot(monster);", route_runner)
        self.assertIn("const walled = around.filter((monster) => !reachable(monster));", route_runner)
        # A boss stands idle until hit, so idle monsters close by count; the
        # statues that never die are ignored by name instead.
        self.assertIn("Tượng đá cơ quan", macro["ignore_monster_names"])
        self.assertIn("Tượng khắc thanh ngọc cơ quan", macro["ignore_monster_names"])
        self.assertIn("macro.idle_monster_radius", route_runner)
        # Monsters still at FULL health after 30 s of Đánh with nothing else
        # dying (a statue that is no target, one out of sight) are left behind;
        # anything we did hurt is fought to the end, weak damage or not.
        self.assertEqual(30, macro["stall_giveup_seconds"])
        # A target the game refuses is left behind at once, hurt or not.
        self.assertNotIn("unseen_seconds", macro)
        self.assertIn("unhittable.has(fail.reason) && !unseen.has(fail.target)", route_runner)
        # The refusal reasons go in the log, so a fight that ends early can be read back.
        self.assertIn("refusals.set(fail.reason", route_runner)
        # Damage we land keeps the fight on even with an empty creature list:
        # Thiên Long bosses never show up in it.
        self.assertEqual(15, macro["damage_grace_seconds"])
        # The hard version's last boss (its room is map 1140) gets 2.5x the cap;
        # the easy one, sharing this route, keeps the normal 180 s.
        boss3 = macro["route_steps"][-2]
        self.assertEqual({1140: 450}, boss3["fight_seconds_by_map"])
        self.assertEqual(180, boss3["fight_seconds"])
        self.assertIn("(step.fight_seconds_by_map || {})[latestWorld?.mapId]", route_runner)
        self.assertIn("macro.damage_grace_seconds", route_runner)
        self.assertIn("latestWorld?.hit", route_runner)
        # The kill packet ends a boss fight at once; without it the fight waits.
        self.assertIn("latestWorld?.kills", route_runner)
        self.assertIn("monster.hp >= 200", route_runner)
        self.assertIn("macro.stall_giveup_seconds", route_runner)
        self.assertIn("latestWorld?.attackFails", route_runner)
        # A bell rings when the whole route is done.
        self.assertTrue(macro["finish_chime"])
        self.assertIn("playChime()", route_runner)
        # No Cổ Mộ step leaves idle monsters standing: an add that sits still
        # while we clear ("Xe đâm húc") charges us on the way to the door and the
        # game then refuses the door (user, 2026-09-21). The statues that cannot
        # be killed are filtered by name, so this costs no extra rounds.
        self.assertEqual([], [step["label"] for step in macro["route_steps"]
                              if step.get("ignore_idle_monsters")])
        self.assertIn("idle: !step.ignore_idle_monsters", route_runner)
        self.assertIn("(idle && monster.d <= idleRadius)", route_runner)
        # Boss 1 only appears once every cart and statue in its room is dead
        # (user, 2026-09-21), so that step must not take any give-up shortcut.
        boss1 = macro["route_steps"][3]
        self.assertEqual("Đánh boss 1 (20,18)", boss1["label"])
        self.assertTrue(boss1["clear_room"])
        # The statues in the room are real targets, unlike the scenery statues
        # out on the main map that the macro ignores by name.
        self.assertEqual(["Lữ Bố"], boss1["ignore_monster_names"])
        self.assertIn("Tượng khắc thanh ngọc cơ quan", macro["ignore_monster_names"])
        # The room is 352x256 px: from 20,18 the farthest corner is ~221 px, so
        # the idle radius has to match monster_radius to sweep all of it.
        self.assertEqual(250, boss1["idle_monster_radius"])
        self.assertEqual(250, macro["monster_radius"])
        self.assertEqual(160, macro["idle_monster_radius"])
        # A step's radii and ignore list override the macro defaults all the way
        # into combatState.
        self.assertIn("radius: step.monster_radius", route_runner)
        self.assertIn("idleRadius: step.idle_monster_radius", route_runner)
        self.assertIn("ignoreNames: step.ignore_monster_names", route_runner)
        self.assertIn("Number(over ?? macro.monster_radius ?? 250)", route_runner)
        self.assertIn("Number(idleOver ?? macro.idle_monster_radius ?? 160)", route_runner)
        self.assertIn("ignoreNames ?? macro.ignore_monster_names", route_runner)
        # clear_room: only "already dead" (3/4) still drops a target; the game
        # refusing a cart or statue (8/14) no longer ends the room, and neither
        # does a target that stays at full health.
        self.assertIn("clearRoom: Boolean(step.clear_room)", route_runner)
        self.assertIn("clearRoom ? new Set([3, 4]) : new Set([3, 4, 8, 14])", route_runner)
        self.assertIn("const stalled = !clearRoom &&", route_runner)
        # The refusal log names the targets, so a room that ends with no boss
        # says what the game would not let us hit.
        self.assertIn("nameOf(fail.target)", route_runner)
        self.assertEqual(2.5, macro["goto_settle_seconds"])
        # The walk ends on the coordinate the user gave (the game's own pixels / 8
        # rounded down), give or take one tile (a boss on its altar).
        self.assertEqual(1, macro["goto_tolerance"])
        self.assertIn("Math.floor(point.x / unit)", route_runner)
        # "Mục tiêu không nằm trong tầm nhìn" (ATTACK_FAIL reason 14): skip it,
        # and never count one the walk grid puts behind walls.
        self.assertIn("unhittable.has(fail.reason)", route_runner)
        self.assertIn("Mật thư", macro["ignore_monster_names"])
        # With no monster left the fight only waits out its checks, no Đánh.
        self.assertIn('if (last.state === "clear" && !hitting && rounds >= minRounds) {', route_runner)
        self.assertEqual(40, macro["monster_path_tiles"])
        self.assertIn("walkDistances(grid", route_runner)
        # A door is hit exactly (no tolerance), then the game data's exit next to it.
        self.assertIn("const tolerance = step.portal ? 0", route_runner)
        self.assertIn("function doorTiles", route_runner)
        # Blocked short of a door tile: try the map data's exit beside it, do
        # not report the way blocked and restart on the same tile every retry.
        self.assertIn("if (doors && doorIndex + 1 < doors.length) {", route_runner)
        self.assertIn("settleSeconds || macro.arrive_settle_seconds", route_runner)
        self.assertIn("Lữ Bố", macros["thien_long_hard"]["ignore_monster_names"])
        # Every place that reads monsters (the ambush check, the fight, the
        # stray-Cầu-phúc check) applies the ignore list; only the fight lets a
        # step replace it.
        self.assertEqual(3, route_runner.count("macro.ignore_monster_names)"))
        # Starting inside the dungeon skips the entry; a portal already crossed is skipped.
        self.assertEqual(768, macro["entry_map"])
        self.assertIn("macro.entry_map", route_runner)
        self.assertIn("const crossed = Boolean(step.portal)", route_runner)
        self.assertIn('appendDiagnostic("dungeon_ambush"', route_runner)
        self.assertIn("function combatState", route_runner)
        self.assertIn("async function fightUntilClear", route_runner)
        self.assertIn("async function waitForArrival", route_runner)
        self.assertIn('event.data?.source !== "sanguo-world"', content)
        # Monsters, position and map come from the game's own traffic, read-only.
        probe = (PROJECT_ROOT / "extension" / "network_probe.js").read_text(encoding="utf-8")
        self.assertNotIn("preserveDrawingBuffer", probe)
        for constant in ("OP_MOVE_CLIENT = 105", "OP_GOMAP_ALLOW = 134", "OP_UNIT_REFRESH = 193",
                         "OP_UNIT_MULTI_REFRESH = 194", "OP_UNIT_MOVE = 195", "STATE_DIE = 8",
                         "OP_UNIT_INFO = 197", "OP_FORCE_GOMAP = 321", "OP_LOADING_FINISHED = 133",
                         "OP_ATTACK_FAIL = 136", "OP_SKILL_ATTACK = 185", "OP_SKILL_ATTACKED = 187"):
            self.assertIn(constant, probe)
        self.assertIn('source: "sanguo-world"', probe)
        self.assertIn("world.kills = [...world.kills.slice(-19)", probe)
        self.assertTrue(macro["entry_steps"][-1].get("portal"))
        # Clicks map onto the (possibly letterboxed) game canvas, not the viewport.
        self.assertIn('document.querySelector("#screen")?.getBoundingClientRect()', content)

    def test_default_catalog_contains_warehouse_take(self):
        flows = {flow["id"]: flow for flow in flow_catalog()}
        macros = extension_server.load_config()["activity_macros"]
        macro = macros["warehouse_take"]
        self.assertEqual("warehouse_take_loop", flows["warehouse_take"]["runner"])
        # Kho panel, one item per round: first slot -> "Bỏ vào hành trang" ->
        # "Đồng ý" on the amount box -> "Sắp xếp kho hàng" pulls the next item up.
        for key in ("first_slot_point", "take_point", "amount_confirm_point", "sort_point"):
            self.assertEqual(2, len(macro[key]), key)
        # The detail panel's action button sits where "Sử dụng" does.
        self.assertEqual(macros["use_inventory_item"]["use_point"], macro["take_point"])
        # 0 = keep going until Stop.
        self.assertEqual(0, macro["max_cycles"])
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        self.assertIn("async function runWarehouseTake", content)
        self.assertIn("macro.amount_confirm_point != null", content)

    def test_screen_door_click_skips_tiles_under_a_unit(self):
        # Thiên Long lobby 2026-09-24: the screen click on door 56,15 hit
        # "Thái Trường Trị" and its panel took every click after it.
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        self.assertIn("const under = unitOver(tile);", content)
        self.assertIn("(macro.unit_click_box || [20, 56])", content)

    def test_discard_items_finds_vut_bo_by_its_text(self):
        # "Vứt bỏ" moves with the item's kind (user, 2026-09-24): the UI flow
        # stays, but that one button is found by its text on screen.
        macro = extension_server.load_config()["activity_macros"]["discard_items"]
        self.assertNotIn("discard_point", macro)
        for key in ("sort_point", "first_item_point", "confirm_point"):
            self.assertEqual(2, len(macro[key]), key)
        self.assertEqual(4, len(macro["discard_search_area"]))
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        self.assertIn("const found = findText(image, VUT_BO_TEXT, area);", content)
        self.assertIn("if (found.score < minScore) {", content)
        self.assertIn("await domClick(token, found.point);", content)
        background = (PROJECT_ROOT / "extension" / "background.js").read_text(encoding="utf-8")
        self.assertIn('if (message.type === "capture") {', background)

    def test_default_catalog_contains_ha_dong_dungeon_routes_and_pipeline(self):
        """One Hà Đông card runs n hard + m easy, like Thiên Long trận."""
        flows = {flow["id"]: flow for flow in flow_catalog()}
        macros = extension_server.load_config()["activity_macros"]
        hard, easy = macros["ha_dong_hard"], macros["ha_dong_easy"]
        # No card of their own (user, 2026-09-23): only the pipeline shows.
        for flow_id in ("ha_dong_hard", "ha_dong_easy"):
            self.assertNotIn(flow_id, flows)
            self.assertEqual("dungeon_route", macros[flow_id]["runner"])
        self.assertEqual([[77, 16], [57, 114], [19, 25], [30, 13], [30, 13], [22, 8], [66, 22], [59, 14]],
                         [step["goto"] for step in hard["route_steps"]])
        self.assertEqual(6, sum(1 for step in hard["route_steps"] if "fight_seconds" in step))
        # The three camps end once nothing attacks us: the camp's leader dead is
        # enough, idle soldiers are left standing. The inner fight still clears.
        # The monster by the inner door (25,9) is cleared idle ones included.
        self.assertEqual([True, True, True, None, None, None],
                         [step.get("ignore_idle_monsters") for step in hard["route_steps"] if "fight_seconds" in step])
        self.assertTrue(hard["route_steps"][5]["portal"])
        self.assertEqual([992, 992], hard["route_steps"][0]["map_size"])
        # Easy shares the inner fight and the way out at 59,14 (map data 59,12).
        # Hà Đông does not get stuck like Thiên Long, so no character switch.
        self.assertEqual(hard["route_steps"][-2:], easy["route_steps"])
        exit_step = easy["route_steps"][-1]
        self.assertEqual([608, 464], exit_step["map_size"])
        self.assertTrue(exit_step["portal"])
        self.assertNotIn("switch_actor_before", exit_step)
        self.assertNotIn("fight_seconds", exit_step)
        # Easy walks in through the door at 24,21 of map 448 (512x512).
        self.assertEqual(448, easy["entry_map"])
        self.assertEqual([[24, 21]], [step["goto"] for step in easy["entry_steps"]])
        self.assertEqual([512, 512], easy["entry_steps"][0]["map_size"])
        self.assertTrue(easy["entry_steps"][0]["portal"])
        # Hard starts at the same map 448, through the NPC by 18,55 and "1.Có".
        self.assertEqual(448, hard["entry_map"])
        hard_entry = hard["entry_steps"]
        self.assertEqual([26, 45], hard_entry[0]["goto"])
        # Not in the unit list on 448: clicked by its tile on the Map instead.
        self.assertEqual([18, 55], hard_entry[1]["npc_tile"])
        self.assertIn("(!near || item.target === near.id)",
                      (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8"))
        # A door the server refuses (ERROR to TOUCHEXIT 116) is read, not clicked
        # for minutes; out of runs for today skips the rest of that stage.
        source = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        self.assertIn("const OP_TOUCH_EXIT = 116;", source)
        self.assertIn("reply.type === OP_TOUCH_EXIT && reply.at >= since", source)
        self.assertEqual("Chiêu thảo sứ triều đình Đại Hán", hard_entry[1]["touch_npc_by_map"])
        self.assertEqual([512, 512], hard_entry[1]["map_size"])
        self.assertEqual([0.5, 0.508], hard_entry[1]["option_point"])
        self.assertTrue(hard_entry[1]["portal"])
        for macro in (hard, easy):
            self.assertEqual(macros["auto_attack"]["skill_points"], macro["skill_points"])
        # A run that starts the second the exit put us on 448 must not click
        # the Map while the map loads: the Map's X would then hit C.Phúc.
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        self.assertIn("(latestWorld?.mapLoadedAt || 0) >= mapSwitchedAt - 1000", content)
        self.assertIn("loadedAt + Number(macro.map_ready_seconds ?? 3) * 1000", content)
        self.assertIn("world.mapLoadedAt = Date.now();",
                      (PROJECT_ROOT / "extension" / "network_probe.js").read_text(encoding="utf-8"))
        self.assertEqual(2, content.count("await openMap(token, macro, clicks);"))
        self.assertEqual(1, content.count("macro.map_button_point || [0.85, 0.07]"))
        # Each step names its map(s); the map we stand on picks the first step
        # (user, 2026-09-23): in the quan nha a run starts at its fight.
        self.assertEqual([[448], [448], [450, 449], [450, 449], [450, 449], [450, 449], [450, 449], [450, 449], [432, 433], [432, 433]],
                         [step["maps"] for step in hard["entry_steps"] + hard["route_steps"]])
        self.assertEqual([[448], [432, 433], [432, 433]],
                         [step["maps"] for step in easy["entry_steps"] + easy["route_steps"]])
        self.assertIn("planned.findIndex((step) => (step.maps || []).map(Number).includes(here))", content)
        # A door already on screen is clicked on the main screen - no Map, so no
        # Map X landing on C.Phúc - with the camera stopped at the map's edges
        # (a map narrower than the screen is drawn centred).
        self.assertIn("step.screen_door_tiles ?? macro.screen_door_tiles ?? 10", content)
        self.assertIn("(size <= view ? size / 2", content)
        # Going to another map closes the Map by itself (user, 2026-09-23): its X
        # is then never clicked, or it would open C.Phúc / H.Trang beneath it.
        self.assertIn("if (mapSwitchedAt > openedAt) return", content)
        self.assertEqual(3, content.count("await closeMap(token, ") + content.count("return closeMap(token, "))
        self.assertNotIn("clicks.push(await domClick(token, panel.closePoint));", content)
        self.assertEqual("dungeon_pipeline", flows["ha_dong_pipeline"]["runner"])
        self.assertEqual([
            {"macro": "ha_dong_hard", "label": "Khó", "times": 5},
            {"macro": "ha_dong_easy", "label": "Dễ", "times": 5},
        ], flows["ha_dong_pipeline"]["stages"])

    def test_hidden_macros_get_no_card(self):
        cfg = {"activity_macros": {
            "stage": {"label": "Lẻ", "hidden": True},
            "pipe": {"label": "Gộp", "runner": "dungeon_pipeline", "stages": [{"macro": "stage", "times": 2}]},
        }}
        flows = {flow["id"]: flow for flow in flow_catalog(cfg)}
        self.assertNotIn("stage", flows)
        self.assertEqual([{"macro": "stage", "label": "Lẻ", "times": 2}], flows["pipe"]["stages"])

    def test_default_catalog_contains_thien_long_routes_and_pipeline(self):
        """Easy and hard are stage macros with no card; one card runs n hard + m easy."""
        flows = {flow["id"]: flow for flow in flow_catalog()}
        macros = extension_server.load_config()["activity_macros"]
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        probe = (PROJECT_ROOT / "extension" / "network_probe.js").read_text(encoding="utf-8")
        css = (PROJECT_ROOT / "extension" / "content.css").read_text(encoding="utf-8")
        for flow_id in ("thien_long_hard", "thien_long_easy"):
            self.assertNotIn(flow_id, flows)
            macro = macros[flow_id]
            self.assertEqual("dungeon_route", macro["runner"])
            route = macro["route_steps"]
            # Five bosses, then the way out at 84,60 (map data's exit is 85,59).
            self.assertEqual([[20, 31], [88, 10], [145, 45], [41, 117], [77, 64], [84, 60]],
                             [step["goto"] for step in route])
            self.assertTrue(all(step["map_size"] == [1280, 1024] for step in route), route)
            self.assertTrue(all("fight_seconds" in step for step in route[:-1]), route)
            self.assertTrue(route[-1]["portal"])
            self.assertNotIn("fight_seconds", route[-1])
            # The bosses are not in the unit list: each is clicked where it
            # stands so the skills have a target (boss 5 went unhit, 2026-09-24).
            self.assertTrue(all(step["click_boss"] for step in route[:-1]), route)
            # Both start from the lobby, map 976 "Của vào Thiên Long Trận".
            self.assertEqual(976, macro["entry_map"])
        # The two share one route list, so a fix lands on both.
        self.assertEqual(macros["thien_long_hard"]["route_steps"],
                         macros["thien_long_easy"]["route_steps"])
        # Easy walks straight into the door at 55,16.
        easy_entry = macros["thien_long_easy"]["entry_steps"]
        self.assertEqual([[55, 16]], [step["goto"] for step in easy_entry])
        self.assertEqual([512, 512], easy_entry[0]["map_size"])
        self.assertTrue(easy_entry[0]["portal"])
        # Hard goes through the NPC. The packets the client sends (TOUCHNPC -1,
        # NOTIFY 1649/1/16) did not take us in on their own (2026-09-23), so the
        # flow clicks it like a person: NPC on the Map, then option 1 - finding
        # the NPC by NAME in the game's own unit list, and checking each click
        # by the packet the client itself sends.
        hard_entry = macros["thien_long_hard"]["entry_steps"]
        self.assertEqual([53, 16], hard_entry[0]["goto"])
        self.assertNotIn("portal", hard_entry[0])
        self.assertEqual("Thái Trường Trị", hard_entry[1]["touch_npc_by_map"])
        self.assertEqual([512, 512], hard_entry[1]["map_size"])
        self.assertEqual([0.5, 0.508], hard_entry[1]["option_point"])
        self.assertTrue(hard_entry[1]["portal"])
        self.assertNotIn("goto", hard_entry[1])
        self.assertIn("step.touch_npc_by_map", content)
        self.assertIn("item.target === near.id", content)
        self.assertIn("latestWorld?.answers", content)
        self.assertIn('type: "npc_trace"', probe)
        self.assertIn("const OP_NOTIFY_CLIENT = 174;", content)
        self.assertIn("asked?.notifyId", content)
        # Both directions of the NPC conversation are logged, so one real click
        # by hand pins down the values the map's script expects.
        self.assertIn('type: "npc_touch_sent"', probe)
        self.assertIn('type: "npc_answer_sent"', probe)
        # And the popup the server sends back, so one real click shows whether
        # the popup comes over the wire at all.
        self.assertIn('type: "npc_dialog"', probe)
        # Every popup packet opens with the asking quest's id (Player.question).
        self.assertIn("int questId | STR message | STR options | int notifyId", probe)
        self.assertIn("const OP_TOUCH_NPC = 120;", content)
        self.assertIn("step.touch_npc", content)
        # The server refuses a touch past 80 px, so the flow says so itself.
        self.assertIn("function nearestNpc(name, withinPx = 80)", content)
        self.assertIn("near = nearestNpc(step.touch_npc_by_map, Infinity);", content)
        # A walk that touches an NPC by accident closes its popup and goes on.
        self.assertIn("macro.npc_popup_close_point || [0.842, 0.145]", content)
        self.assertIn("macro.stray_popup_limit ?? 3", content)
        # A Map leg that did not move us at all means the Map never opened and
        # its X opened Cầu phúc: close that and walk on (user, 2026-09-23).
        self.assertIn("macro.blessing_close_point || [0.933, 0.095]", content)
        # ...only where the Map's X covers C.Phúc and nothing is hitting us.
        self.assertIn("stood && !near && xClicked && strayPanel && !attacked", content)
        # H.Động too, on the 992-wide map; and no X at all after the map loaded
        # again (a refused door), which closes the Map as a switch does.
        self.assertIn("const strayPanel = !step.portal && [", content)
        self.assertIn("if ((latestWorld?.mapLoadedAt || 0) > openedAt) return", content)
        # The inner door shut with nothing left alive: log the same character in
        # again, then retry (user, 2026-09-23). One login, no spare character.
        hard = macros["ha_dong_hard"]
        # Always switch at 30,13 and clear once more before the door: a door
        # refused reloads the map, and Chọn NV will not open while it loads
        # (user, 2026-09-24).
        relog = hard["route_steps"][4]
        self.assertEqual([30, 13], relog["goto"])
        self.assertTrue(relog["switch_actor_before"])
        self.assertTrue(relog["fight_seconds"])
        self.assertIn("await waitMapReady(token, macro);\n    // Opens Chọn NV", content)
        inner_door = hard["route_steps"][5]
        self.assertEqual([22, 8], inner_door["goto"])
        self.assertTrue(inner_door["switch_actor_if_stuck"])
        self.assertFalse(hard["actor_switch"]["via_spare"])
        self.assertIn("if (shut && step.switch_actor_if_stuck && !relogged && (attempt >= 1 || doorFight.rounds === 0)) {", content)
        # A refused door is handed back at once, not clicked again and again.
        self.assertIn("if (walk.refused) return { ...walk, note:", content)
        self.assertIn("if (switcher.via_spare) {", content)
        # Still shut after that: a few skill rounds even with no monster seen.
        self.assertIn("minRounds: relogged ? Number(step.extra_rounds_after_relog ?? macro.extra_rounds_after_relog ?? 6) : 0,", content)
        self.assertIn("if (last.state === \"clear\" && !hitting && rounds >= minRounds) {", content)
        # The NPC's answer is read off the wire when the server sends one, so
        # the log carries the popup's own words. The client can draw the popup
        # by itself, so the touch does not fail on a silent server: the answer
        # step does, when the map never changes.
        self.assertIn("const OP_QUESTION_SERVER = 123;", probe)
        self.assertIn("world.dialogs = [", probe)
        self.assertIn("latestWorld?.dialogs", content)
        self.assertIn("nhưng vẫn ở map", content)

        # The pipeline: n hard then m easy on the one character (user,
        # 2026-09-23), each count chosen on the panel; 0 skips that stage.
        self.assertEqual("dungeon_pipeline", flows["thien_long_pipeline"]["runner"])
        pipeline = macros["thien_long_pipeline"]
        self.assertEqual(["thien_long_hard", "thien_long_easy"],
                         [stage["macro"] for stage in pipeline["stages"]])
        # The catalogue hands each stage's label and default count to the panel.
        self.assertEqual([
            {"macro": "thien_long_hard", "label": "Khó", "times": 5},
            {"macro": "thien_long_easy", "label": "Dễ", "times": 5},
        ], flows["thien_long_pipeline"]["stages"])
        self.assertNotIn("run_options", flows["thien_long_pipeline"])
        self.assertIn("Number(stage.times ?? macro.times ?? 1)", content)
        # One count box per stage; the run button sends every stage's count.
        self.assertIn("if (flow.stages?.length) return renderPipelineCard(flow, running);", content)
        self.assertIn("runFlow(flow, { stages: flow.stages.map(", content)
        # The status refresh must not redraw (and wipe) a box being typed into.
        self.assertIn("if (key === renderedKey) return;", content)
        self.assertIn(".sg-stage-counts input", css)
        switcher = pipeline["actor_switch"]

        self.assertEqual(4, len(switcher["actor_slot_points"]))
        for key in ("hanh_trang_point", "menu_drag_from", "menu_drag_to", "he_thong_point", "doi_nhan_vat_point"):
            self.assertEqual(2, len(switcher[key]), key)
        # The slots are read from the game, not configured: these stay null and
        # exist only to override the detection.
        for key in ("actor_slot", "spare_actor_slot"):
            self.assertIsNone(switcher[key], key)
        # Who we are (login packet) and the card order (character list packet).
        self.assertIn("const OP_ACTOR_LOGIN_SERVER = 104;", probe)
        self.assertIn("const OP_ACTOR_LIST_SERVER = 169;", probe)
        self.assertIn("world.actorId = reader.i32();", probe)
        self.assertIn("actors.push({ id, name, level });", probe)
        self.assertIn("actor.id === latestWorld?.actorId", content)
        # The left menu is scrolled to its end before H.Thống is clicked, and
        # Chọn NV only counts as open once a fresh character list arrives.
        # H.Thống only shows once the menu is DRAGGED to its end; its arrow
        # does nothing when clicked from here (user, 2026-09-23).
        self.assertEqual(2, switcher["menu_drag_times"])
        self.assertGreater(switcher["menu_drag_from"][1], switcher["menu_drag_to"][1])
        self.assertNotIn("menu_scroll_point", switcher)
        self.assertIn("async function domDrag(", content)
        # The switch by hand is a reconnect; both ends get traced, login
        # bodies never.
        self.assertIn('type: "switch_trace"', probe)
        self.assertIn("TRACE_SECRET.has(opcode)", probe)
        self.assertIn("(latestWorld?.actorsAt || 0) > listedBefore", content)
        self.assertIn("world.actorsAt = Date.now();", probe)
        self.assertIn("latestWorld?.actorId !== actor.id", content)
        # Chọn NV closes the client's socket and a new one logs in (16:45):
        # expected during a switch, a failure only if it stays closed after.
        self.assertIn('socketEvent?.type === "ws_close" && !token.reconnecting', content)
        self.assertIn("token.socketCheckFrom = switchedAt;", content)
        # "mỗi ngày chỉ có thể đi 5 lần": that stage is skipped, not the pipeline.
        self.assertIn("error.dailyLimit = /mỗi ngày/i.test(refusal.message);", content)
        self.assertIn("if (exhausted.has(id)) continue;", content)
        # A door that leaves us standing beside it is tried again a tile or two
        # to either side (user, 2026-09-23: 84,60 stopped us at 82,57).
        self.assertIn("const nudgeThrough = async ({ doorsOnly = false } = {}) => {", content)
        # Also tried first when the door is already on screen (2026-09-23).
        self.assertEqual(3, content.count("const through = await nudgeThrough();"))
        self.assertIn("const through = await nudgeThrough({ doorsOnly: true });", content)
        # ...clicked on the main screen, where the camera keeps us centred (up
        # to the map's edges) and the world is drawn at 2x, not on the Map.
        self.assertIn("0.5 + (point.x - eye.x) * worldScale / screenW", content)
        self.assertIn("const onScreen = (tile, me) => screenPointOf(macro, center(tile), me,", content)
        # An idle full-health monster the skills never reach is clicked on the
        # main screen once - made the target, walked up to - before it is
        # given up on (Cổ Mộ boss 2, 2026-09-23).
        self.assertIn("if (aim && clearOfHud(aim)) {", content)
        self.assertIn("approached.add(fresh[0].id);", content)
        self.assertIn("minRounds: Number(step.min_rounds ?? (step.click_boss ? 12 : 0)),", content)
        self.assertIn("if (bossAt && !hitting && Date.now() - bossClickedAt >= retargetMs && latestWorld?.me) {", content)
        self.assertIn("const tiles = [...doors.slice(1), goal];", content)
        # A click under our own panel still reaches the game.
        self.assertIn("hit && panel.contains(hit)", content)
        # An override wins over the detection when it is set.
        self.assertIn("forced > 0 ? forced - 1 :", content)
        self.assertIn("dungeon_pipeline", content)
        self.assertIn("async function switchActorAndBack", content)
        # The switch happens inside each run, right before the exit: the door
        # stays shut until then (user, 2026-09-23). Both buttons and the
        # pipeline share the one set of click points.
        exit_step = macros["thien_long_hard"]["route_steps"][-1]
        self.assertTrue(exit_step["switch_actor_before"])
        self.assertTrue(exit_step["portal"])
        self.assertEqual(switcher, macros["thien_long_hard"]["actor_switch"])
        self.assertEqual(switcher, macros["thien_long_easy"]["actor_switch"])
        self.assertIn("if (step.switch_actor_before) {", content)
        # A switch reloads the map; that must not read as the exit crossed.
        self.assertIn("if (latestWorld?.mapId === mapBefore) mapMark = mapSwitchedAt;", content)
        # Between runs the pipeline clears the dungeon's progress - once out of
        # it, never before the first run.
        self.assertIn("let resetDone = index === 0;", content)
        # The client's own 3 s "progress cleared" box must be gone before the
        # next run's first click.
        self.assertIn("await domWait(token, Number(macro.reset_message_seconds ?? 4));", content)
        self.assertIn("resetDone = true;", content)
        # A run cut off by a dropped connection (1006) waits for the game to
        # log in again and picks up where we stand - never after a stop or a
        # guard close (user, 2026-09-23).
        self.assertIn("error.socketClosed = { code: Number(socketEvent.code)", content)
        self.assertIn("(macro.resume_close_codes || [1006]).map(Number)", content)
        self.assertIn("await awaitReconnect(token, macro, flow, closed);", content)
        self.assertIn("(latestWorld?.mapLoadedAt || 0) > openedAt && latestWorld?.mapId != null", content)
        # One bell at the end, not after every run in the pipeline.
        self.assertIn("finish_chime: false", content)

    def test_walk_grids_match_live_positions_and_reach_every_route_target(self):
        node = shutil.which("node")
        if not node:
            self.skipTest("node is not installed")
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        grids = (PROJECT_ROOT / "extension" / "walk_grids.js").read_text(encoding="utf-8")
        manifest = json.loads((PROJECT_ROOT / "extension" / "manifest.json").read_text(encoding="utf-8"))
        scripts = [entry["js"] for entry in manifest["content_scripts"] if "content.js" in entry["js"]][0]
        self.assertEqual(["walk_grids.js", "content.js"], scripts)
        pieces = []
        for name in ("walkGridFor", "findPath", "nearestOpen", "straightLine", "planRoute"):
            start = content.index(f"  function {name}(")
            pieces.append(content[start:content.index("\n  }\n", start) + 4])
        # Tiles the character stood on in live runs (logs and screenshots).
        stood = {
            977: [[8, 89], [20, 78], [20, 27], [88, 9], [100, 10], [134, 47], [146, 50], [139, 61]],
            978: [[10, 87], [16, 28], [53, 36], [87, 25], [77, 66], [47, 84]],
            450: [[94, 100], [72, 12], [49, 50], [31, 75]], 432: [[24, 46]],
            1137: [[16, 53], [35, 34], [20, 45], [83, 34]], 769: [[83, 34]], 768: [[33, 28], [31, 24], [24, 27]], 1141: [[21, 16]],
            # Thiên Long lobby, from the user's screenshots (2026-09-23): the
            # map title read (53,16) by the NPC and (52,14) with its popup open.
            976: [[53, 16], [52, 14]],
        }
        # Every route leg: map, where it starts (after a portal: by the exit we
        # came in through), the target the user gave.
        legs = [
            [977, [8, 89], [20, 31]], [977, [20, 31], [88, 10]], [977, [88, 10], [145, 45]],
            [977, [145, 45], [41, 117]], [977, [41, 117], [77, 64]],
            [450, [94, 100], [77, 16]], [450, [77, 16], [57, 114]], [450, [57, 114], [19, 25]],
            [450, [19, 25], [22, 8]], [432, [24, 46], [66, 22]],
            [1137, [16, 53], [38, 12]], [1137, [38, 12], [44, 10]], [1141, [31, 24], [20, 18]],
            [1141, [20, 18], [31, 24]], [1137, [45, 10], [90, 30]], [1137, [90, 30], [83, 34]],
            [1139, [8, 8], [44, 34]], [1139, [44, 34], [9, 10]], [1137, [85, 33], [99, 4]],
            [1137, [99, 4], [93, 6]], [1140, [21, 75], [69, 23]],
            # Thiên Long: the lobby walk to the hard NPC and to the easy door,
            # and the way out of the trận at 84,60 (map data's exit is 85,59).
            [976, [56, 15], [53, 16]], [976, [53, 16], [55, 16]],
            [977, [77, 64], [84, 60]], [978, [77, 66], [84, 60]],
        ]
        script = grids + "const decodedGrids = new Map();\n" + "".join(pieces) + (
            "const stood = " + json.dumps(stood) + ";\nconst legs = " + json.dumps(legs) + ";\n"
            "console.log(JSON.stringify({\n"
            "  blocked: Object.entries(stood).flatMap(([id, tiles]) =>\n"
            "    tiles.filter(([x, y]) => !walkGridFor(id).open(x, y)).map((t) => [id, t])),\n"
            "  exits450: walkGridFor(450).exits,\n"
            "  ends: legs.map(([id, [fx, fy], [tx, ty]]) => {\n"
            "    const path = planRoute(walkGridFor(id), { x: fx, y: fy }, { x: tx, y: ty }, 1);\n"
            "    return path && path[path.length - 1];\n"
            "  }),\n"
            "}));\n"
        )
        # Through stdin: with the walk grids inlined, -e outgrows the Windows command line.
        result = subprocess.run([node], input=script, capture_output=True, text=True,
                                encoding="utf-8", check=True)
        report = json.loads(result.stdout)
        # The grid (ported from GameView.rebuildMapCollisionData) agrees with the game.
        self.assertEqual([], report["blocked"])
        # Hà Đông ngoài's door to the inner map, from the game data: pixel 196,67.
        self.assertIn([24, 8], report["exits450"])
        for (map_id, _start, target), end in zip(legs, report["ends"]):
            self.assertIsNotNone(end, (map_id, target))
            self.assertLessEqual(max(abs(end["x"] - target[0]), abs(end["y"] - target[1])), 1, (map_id, target))

    def test_map_panel_model_matches_measured_map_panels(self):
        node = shutil.which("node")
        if not node:
            self.skipTest("node is not installed")
        content = (PROJECT_ROOT / "extension" / "content.js").read_text(encoding="utf-8")
        start = content.index("  function mapPanel(")
        source = content[start:content.index("\n  }\n", start) + 4]
        # [map size, our position, map pixel to click or null] -> close X, click point.
        cases = [
            [[560, 432], [200, 200], [484, 67]],     # 768 lobby, easy portal clicked live
            [[800, 608], [365, 150], [365, 84]],     # 1137, portal 2 clicked live
            [[352, 256], [160, 144], None],          # 1141
            [[832, 720], [172, 606], None],          # 1140
            [[992, 992], [752, 800], [752, 800]],    # Hà Đông ngoài: us drawn in the screenshot
            [[512, 512], [264, 280], [453, 120]],    # Thiên Long lobby at 33,35: portal to the trận
            [[1280, 1024], [64, 712], [687, 472]],   # Thiên Long trận, Map opened at 8,89: centre portal
        ]
        script = source + (
            "const cases = " + json.dumps(cases) + ";\n"
            "console.log(JSON.stringify(cases.map(([size, me, at]) => {\n"
            "  const panel = mapPanel({}, size, { x: me[0], y: me[1] });\n"
            "  const spot = at || me;\n"
            "  return [panel.closePoint, at && panel.toFraction({ x: at[0], y: at[1] }),\n"
            "          panel.shows({ x: spot[0], y: spot[1] }), panel.shows({ x: 616, y: 128 })];\n"
            "})));\n"
        )
        # Through stdin: with the walk grids inlined, -e outgrows the Windows command line.
        result = subprocess.run([node], input=script, capture_output=True, text=True,
                                encoding="utf-8", check=True)
        panels = json.loads(result.stdout)
        measured = [
            ([0.745, 0.106], [0.658, 0.241]),
            ([0.839, 0.066], [0.472, 0.228]),
            ([0.663, 0.244], None),
            ([0.851, 0.066], None),
            # Screenshot, canvas at 57,137 1806x903: X at 1707,195; us at 1322,640.
            ([(1707 - 57) / 1806, (195 - 137) / 903], [(1322 - 57) / 1806, (640 - 137) / 903]),
            # Thiên Long screenshots, same canvas: lobby X 1367,195 and portal ring
            # 1238,395; trận (1280 wide, so cut to 1124) X 1800,195 and ring 1135,300.
            ([(1367 - 57) / 1806, (195 - 137) / 903], [(1238 - 57) / 1806, (395 - 137) / 903]),
            ([(1800 - 57) / 1806, (195 - 137) / 903], [(1135 - 57) / 1806, (300 - 137) / 903]),
        ]
        for (close, click, *_), (want_close, want_click) in zip(panels, measured):
            for got, want in zip(close, want_close):
                self.assertAlmostEqual(want, got, delta=0.006)
            for got, want in zip(click or [], want_click or []):
                self.assertAlmostEqual(want, got, delta=0.01)
        # 77,16 is off the Hà Đông panel from the entrance (its arrows do not
        # scroll it), so that walk goes in legs; the 768 lobby shows whole.
        self.assertIs(False, panels[4][3])
        self.assertTrue(all(panel[2] for panel in panels))  # every measured spot shows

    def test_dungeon_step_log_keeps_step_detail_message(self):
        handle = MagicMock()
        with patch.object(Path, "open") as open_file, patch.object(Path, "mkdir"):
            open_file.return_value.__enter__.return_value = handle
            event = log_network_event({
                "type": "dungeon_step_done", "flow": "co_mo_hard", "cycle": 3, "step": "step_3",
                "message": "Bước 3/19: Đi tới 1 xong sau 12.0s; click: canvas#screen@1592,200",
            })
        self.assertEqual("dungeon_step_done", event["type"])
        self.assertEqual("co_mo_hard", event["flow"])
        self.assertEqual(3, event["cycle"])
        self.assertIn("canvas#screen@1592,200", event["message"])

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
            "auto_attack_loop", "warehouse_take_loop",
        ):
            self.assertIn(f'"{runner}"', content)
        for flow in (
            "blessing", "code_redeem", "discard_items", "use_inventory_item", "coin_shake", "auto_attack",
            "warehouse_take",
        ):
            self.assertIn(f'flow === "{flow}"', content)
        self.assertNotIn('type: "run-native"', content)
        self.assertNotIn('"debugger"', manifest)
        self.assertNotIn("chrome.debugger", background)
        self.assertIn('"version": "0.16.0"', manifest)
        self.assertIn("typeof PointerEvent", content)
        self.assertIn("new KeyboardEvent", content)
        self.assertIn('label: "Ô trái", overrides: { item_slot: "left" }', content)
        self.assertIn('label: "Ô phải", overrides: { item_slot: "right" }', content)
        self.assertIn('(macro.item_points || {})[itemSlot]', content)
        # An attack round is the skill buttons; Đánh only for a macro that
        # still sets attack_point (see test_default_catalog_contains_auto_attack).
        self.assertIn('for (const point of skillPoints)', content)
        self.assertIn('const BLESSING_SPEED_FACTOR = 1.0', content)
        self.assertIn('async function runBlessing', content)
        self.assertIn('flow === "blessing"', content)
        dom_click = content[content.index("async function domClick"):content.index("function dispatchKey")]
        self.assertIn('dispatchMouse(target, "mousedown"', dom_click)
        self.assertIn('dispatchMouse(target, "mouseup"', dom_click)
        self.assertNotIn('"pointerdown"', dom_click)
        self.assertNotIn('"pointerup"', dom_click)
        self.assertNotIn('"mousemove"', dom_click)
        # The TeaVM canvas must never get a click event; the HTML dialogs the
        # game lays over it (the amount box) act on nothing else.
        self.assertIn('target.id !== "screen"', dom_click)
        self.assertIn('dispatchMouse(target, "click"', dom_click)
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
