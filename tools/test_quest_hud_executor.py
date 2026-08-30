"""Offline state-machine tests for built-in HUD quest automation."""
from __future__ import annotations

import unittest
from unittest.mock import MagicMock, call, patch

from src.game.quests import QuestExecutor


class QuestHudExecutorTest(unittest.TestCase):
    @patch("src.game.quests.time.sleep", return_value=None)
    @patch("src.game.quests.GameActions")
    def test_cycles_pending_rows_until_a_completed_row_can_be_returned(
        self, actions_class: MagicMock, _sleep: MagicMock
    ) -> None:
        cfg = {"quest_actions": {
            "max_hud_quest_clicks_per_run": 4,
            "auto_quest_timeout_seconds": 0.01,
            "return_settle_seconds": 0,
        }}
        actions = actions_class.return_value
        actions.hud_quest_rows.side_effect = [
            [(0, "pending", 0.265)],
            [(1, "pending", 0.330)],
            [(0, "completed", 0.265)],
        ]
        actions.wait_for_hud_completion.return_value = None
        actions.accept_all_map_quests.return_value = 0

        completed = QuestExecutor(MagicMock(), cfg).run_all_incomplete(max_quests=1)

        self.assertEqual(1, completed)
        self.assertEqual(
            [call(0.265), call(0.330), call(0.265)],
            actions.activate_hud_quest.call_args_list,
        )
        actions.wait_for_arrival.assert_called_once_with()

    @patch("src.game.quests.GameActions")
    def test_accepts_dialog_choice_when_hud_has_no_rows(self, actions_class: MagicMock) -> None:
        cfg = {"quest_actions": {"max_hud_quest_clicks_per_run": 1}}
        actions = actions_class.return_value
        actions.hud_quest_rows.return_value = []
        actions.accept_all_map_quests.side_effect = [1, 0]

        completed = QuestExecutor(MagicMock(), cfg).run_all_incomplete()

        self.assertEqual(0, completed)
        self.assertEqual(2, actions.accept_all_map_quests.call_count)


if __name__ == "__main__":
    unittest.main()
