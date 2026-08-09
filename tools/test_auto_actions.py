"""Offline tests for quest action classification and ! marker detection."""
from __future__ import annotations

import unittest
from types import SimpleNamespace

import cv2
import numpy as np

from src.game.actions import GameActions


class FakeControl:
    def __init__(self, image: np.ndarray | None = None):
        self.image = image if image is not None else np.zeros((600, 1000, 3), dtype=np.uint8)
        self.clicks: list[tuple[float, float]] = []
        self.presses: list[str] = []

    def click(self, x: float, y: float) -> None:
        self.clicks.append((round(x, 3), round(y, 3)))

    def capture(self):
        return SimpleNamespace(image=self.image)

    def press(self, key: str) -> None:
        self.presses.append(key)


class GameActionsTest(unittest.TestCase):
    def setUp(self) -> None:
        self.cfg = {
            "ui": {
                "interact_skill": {"x": 0.86, "y": 0.82},
                "quest_dialog_action": {"x": 0.86, "y": 0.82},
            },
            "quest_actions": {
                "click_interval_seconds": 0,
                "npc_dialog_delay_seconds": 0,
                "dialog_settle_seconds": 0,
            },
        }

    def test_collection_and_combat_classification(self) -> None:
        actions = GameActions(FakeControl(), self.cfg)
        self.assertEqual("collection", actions.quest_kind("Thu thập dược liệu"))
        self.assertEqual("combat", actions.quest_kind("Thịt nhện quỷ nữ"))

    def test_collection_round_clicks_target_then_interact(self) -> None:
        control = FakeControl()
        GameActions(control, self.cfg).perform_action_round("Thu thập dược liệu")
        self.assertEqual(8, len(control.clicks))
        self.assertEqual((0.86, 0.82), control.clicks[-1])

    def test_yellow_exclamation_opens_npc_then_accepts(self) -> None:
        image = np.zeros((600, 1000, 3), dtype=np.uint8)
        yellow = cv2.cvtColor(np.uint8([[[30, 240, 240]]]), cv2.COLOR_HSV2BGR)[0, 0].tolist()
        cv2.rectangle(image, (540, 150), (560, 215), yellow, -1)
        control = FakeControl(image)
        actions = GameActions(control, self.cfg)

        self.assertTrue(actions.accept_available_quest())
        self.assertEqual(2, len(control.clicks))
        self.assertEqual((0.86, 0.82), control.clicks[-1])


if __name__ == "__main__":
    unittest.main()
