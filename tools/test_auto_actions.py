"""Offline tests for quest action classification and ! marker detection."""
from __future__ import annotations

import unittest
from types import SimpleNamespace

import cv2
import numpy as np

from src.game.actions import GameActions


class FakeControl:
    def __init__(self, image: np.ndarray | None = None, images: list[np.ndarray] | None = None):
        self.image = image if image is not None else np.zeros((600, 1000, 3), dtype=np.uint8)
        self.images = list(images or [])
        self.clicks: list[tuple[float, float]] = []
        self.presses: list[str] = []

    def click(self, x: float, y: float) -> None:
        self.clicks.append((round(x, 3), round(y, 3)))

    def capture(self):
        if self.images:
            self.image = self.images.pop(0)
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
                "hud_row_points": [0.265, 0.330],
                "hud_green_pixel_threshold": 20,
                "hud_white_pixel_threshold": 20,
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

    def test_hud_rows_distinguish_pending_white_from_completed_green(self) -> None:
        image = np.zeros((600, 1000, 3), dtype=np.uint8)
        cv2.rectangle(image, (50, 150), (170, 168), (255, 255, 255), -1)
        cv2.rectangle(image, (50, 190), (170, 208), (0, 255, 0), -1)

        rows = GameActions(FakeControl(image), self.cfg).hud_quest_rows()

        self.assertEqual([(0, "pending", 0.265), (1, "completed", 0.33)], rows)

    def test_accepts_wide_gold_dialog_row_without_map_scanning(self) -> None:
        dialog = np.zeros((600, 1000, 3), dtype=np.uint8)
        gold = cv2.cvtColor(np.uint8([[[25, 220, 220]]]), cv2.COLOR_HSV2BGR)[0, 0].tolist()
        cv2.rectangle(dialog, (200, 280), (800, 325), gold, -1)
        normal = np.zeros_like(dialog)
        control = FakeControl(images=[dialog, normal])

        accepted = GameActions(control, self.cfg).accept_all_map_quests()

        self.assertEqual(1, accepted)
        self.assertEqual((0.5, 0.504), control.clicks[0])

    def test_detects_and_dismisses_level_one_tutorial_before_quest_scan(self) -> None:
        tutorial = np.zeros((600, 1000, 3), dtype=np.uint8)
        orange = cv2.cvtColor(np.uint8([[[25, 220, 220]]]), cv2.COLOR_HSV2BGR)[0, 0].tolist()
        cv2.rectangle(tutorial, (460, 330), (540, 375), orange, -1)
        normal = np.zeros_like(tutorial)
        control = FakeControl(images=[tutorial, tutorial, normal])

        accepted = GameActions(control, self.cfg).accept_all_map_quests()

        self.assertEqual(0, accepted)
        self.assertEqual((0.5, 0.588), control.clicks[0])

    def test_dismisses_consecutive_tutorial_prompts(self) -> None:
        tutorial = np.zeros((600, 1000, 3), dtype=np.uint8)
        orange = cv2.cvtColor(np.uint8([[[25, 220, 220]]]), cv2.COLOR_HSV2BGR)[0, 0].tolist()
        cv2.rectangle(tutorial, (460, 330), (540, 375), orange, -1)
        normal = np.zeros_like(tutorial)
        control = FakeControl(images=[tutorial, tutorial, tutorial, tutorial, normal])

        accepted = GameActions(control, self.cfg).accept_all_map_quests()

        self.assertEqual(0, accepted)
        self.assertEqual([(0.5, 0.588), (0.5, 0.588)], control.clicks)

    def test_accepts_green_action_in_quest_detail_dialog(self) -> None:
        dialog = np.zeros((600, 1000, 3), dtype=np.uint8)
        green = cv2.cvtColor(np.uint8([[[60, 220, 220]]]), cv2.COLOR_HSV2BGR)[0, 0].tolist()
        cv2.rectangle(dialog, (745, 425), (825, 475), green, -1)
        normal = np.zeros_like(dialog)
        control = FakeControl(images=[dialog, dialog, dialog, normal])

        accepted = GameActions(control, self.cfg).accept_all_map_quests()

        self.assertEqual(1, accepted)
        self.assertEqual((0.785, 0.75), control.clicks[0])


if __name__ == "__main__":
    unittest.main()
