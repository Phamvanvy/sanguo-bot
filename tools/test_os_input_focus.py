"""Tests for focusing an existing game window without changing its size."""
from __future__ import annotations

import unittest
import tempfile
from pathlib import Path
from unittest.mock import patch

import numpy as np

from src.os_input import OsGameSession, Rect, _focus, launch


class OsInputFocusTest(unittest.TestCase):
    @patch("src.os_input.OsGameSession.focus")
    @patch("src.os_input._find_window", return_value=12345)
    @patch("src.os_input.subprocess.Popen")
    def test_launch_loads_the_unpacked_extension(self, popen, _find_window, _focus) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            cfg = {
                "_resolved_os_profile_dir": Path(temp_dir) / "profile",
                "game": {
                    "url": "https://play.minhchauh5.com/",
                    "window": {"width": 1280, "height": 800},
                    "os_input": {
                        "browser_exe": "brave.exe",
                        "window_title_hint": "Minh Chau H5",
                        "load_extension": True,
                    },
                },
            }

            launch(cfg)

        args = popen.call_args.args[0]
        self.assertTrue(any(value.startswith("--load-extension=") for value in args))
        self.assertIn("--new-window", args)
        self.assertIn("--disable-background-timer-throttling", args)
        self.assertIn("--disable-backgrounding-occluded-windows", args)
        self.assertIn("--disable-renderer-backgrounding", args)
        self.assertIn(
            "--disable-features=CalculateNativeWinOcclusion,IntensiveWakeUpThrottling",
            args,
        )

    @patch("src.os_input._print_window_region")
    def test_window_capture_reads_the_selected_hwnd(self, print_window_region) -> None:
        expected = np.zeros((120, 200, 3), dtype=np.uint8)
        print_window_region.return_value = expected
        rect = Rect(left=100, top=200, width=200, height=120)
        session = OsGameSession(process=None, hwnd=12345, window_capture=True)

        actual = session.capture(rect)

        self.assertIs(expected, actual)
        print_window_region.assert_called_once_with(12345, rect)

    @patch("src.os_input.pydirectinput.click")
    @patch("src.os_input.pydirectinput.moveTo")
    @patch("src.os_input._post_background_click")
    def test_background_click_does_not_move_the_real_cursor(
        self, post_background_click, move_to, direct_click
    ) -> None:
        session = OsGameSession(process=None, hwnd=12345, background_clicks=True)

        session.click_fraction(0.5, 0.5, Rect(left=100, top=200, width=200, height=120))

        post_background_click.assert_called_once_with(12345, 200, 260)
        move_to.assert_not_called()
        direct_click.assert_not_called()

    @patch("src.os_input.pydirectinput.press")
    @patch("src.os_input._post_background_key")
    def test_background_key_does_not_take_foreground_focus(self, post_background_key, direct_press) -> None:
        session = OsGameSession(process=None, hwnd=12345, background_clicks=True)

        session.press("6")

        post_background_key.assert_called_once_with(12345, "6")
        direct_press.assert_not_called()

    @patch("src.os_input.time.sleep", return_value=None)
    @patch("src.os_input.win32gui.SetForegroundWindow")
    @patch("src.os_input.win32gui.ShowWindow")
    @patch("src.os_input.win32gui.IsIconic", return_value=False)
    def test_maximized_window_is_focused_without_restore(
        self, _is_iconic, show_window, set_foreground, _sleep
    ) -> None:
        _focus(12345)

        show_window.assert_not_called()
        set_foreground.assert_called_once_with(12345)

    @patch("src.os_input.time.sleep", return_value=None)
    @patch("src.os_input.win32gui.SetForegroundWindow")
    @patch("src.os_input.win32gui.ShowWindow")
    @patch("src.os_input.win32gui.IsIconic", return_value=True)
    def test_minimized_window_is_restored_before_focus(
        self, _is_iconic, show_window, set_foreground, _sleep
    ) -> None:
        _focus(12345)

        show_window.assert_called_once_with(12345, 9)
        set_foreground.assert_called_once_with(12345)


if __name__ == "__main__":
    unittest.main()
