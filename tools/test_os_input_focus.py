"""Tests for focusing an existing game window without changing its size."""
from __future__ import annotations

import unittest
from unittest.mock import patch

from src.os_input import OsGameSession, Rect, _focus


class OsInputFocusTest(unittest.TestCase):
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
