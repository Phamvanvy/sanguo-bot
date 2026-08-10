"""Offline contract tests for the Playwright/CDP control backend."""
from __future__ import annotations

import unittest
from types import SimpleNamespace
from unittest.mock import MagicMock, patch

import cv2
import numpy as np

from src.browser import GameSession
from src.capture import GameControl, open_control


class PlaywrightControlTest(unittest.TestCase):
    def make_session(self) -> tuple[GameSession, MagicMock]:
        page = MagicMock()
        page.locator.return_value.bounding_box.return_value = {
            "x": 10.0, "y": 20.0, "width": 800.0, "height": 600.0,
        }
        ok, png = cv2.imencode(".png", np.zeros((600, 800, 3), dtype=np.uint8))
        self.assertTrue(ok)
        page.screenshot.return_value = png.tobytes()
        session = GameSession(
            playwright=MagicMock(), context=MagicMock(), page=page,
            control_mode="cdp_attach", canvas_selector="#screen",
            owns_context=False,
        )
        return session, page

    def test_capture_and_click_keep_canvas_fraction_contract(self):
        session, page = self.make_session()
        control = GameControl(session, {"game": {"os_input": {}}})

        frame = control.capture()
        control.click(0.25, 0.5)

        self.assertEqual((600, 800, 3), frame.image.shape)
        self.assertEqual((10, 20, 800, 600), (
            frame.rect.left, frame.rect.top, frame.rect.width, frame.rect.height,
        ))
        page.mouse.click.assert_called_once_with(210.0, 320.0)

    def test_keyboard_and_dry_run_do_not_use_os_input(self):
        session, page = self.make_session()
        session.type_text("CODE1")
        session.press("escape")
        session.set_dry_run(True)
        session.click_fraction(0.5, 0.5)

        page.keyboard.type.assert_called_once_with("CODE1", delay=30.0)
        page.keyboard.press.assert_called_once_with("Escape")
        page.mouse.click.assert_not_called()

    def test_attached_session_close_only_detaches_playwright(self):
        session, _ = self.make_session()
        session.close()
        session.context.close.assert_not_called()
        session.playwright.stop.assert_called_once_with()

    @patch("src.browser.open_game")
    def test_open_control_routes_cdp_mode_to_playwright(self, open_game_mock):
        open_game_mock.return_value = SimpleNamespace(control_mode="cdp_attach")
        cfg = {"game": {"control_mode": "cdp_attach", "os_input": {}}}
        control = open_control(cfg)
        self.assertIs(open_game_mock.return_value, control.session)


if __name__ == "__main__":
    unittest.main()
