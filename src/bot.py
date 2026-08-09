"""Command-line entry point for automatic quest execution."""
from __future__ import annotations

import argparse
import logging
import sys
from pathlib import Path

from src.capture import GameControl, open_control
from src.config import load_config
from src.game.actions import GameActions
from src.game.quests import QuestExecutor


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.FileHandler(Path("logs/bot.log"), encoding="utf-8"),
        logging.StreamHandler(sys.stdout),
    ],
)
logger = logging.getLogger(__name__)


class QuestBot:
    """Attach to the game and run incomplete quests in panel order."""

    def __init__(self, cfg: dict, max_quests: int = 0):
        self.cfg = cfg
        self.max_quests = max_quests
        self.gc: GameControl | None = None

    def start(self) -> int:
        logger.info("Sanguo auto-quest starting (dry_run=%s)", self.cfg["runtime"]["dry_run"])
        self.gc = open_control(self.cfg)
        self.gc.set_dry_run(bool(self.cfg["runtime"]["dry_run"]))
        try:
            if self.cfg.get("quest_actions", {}).get("accept_map_quests_before_run", True):
                accepted = GameActions(self.gc, self.cfg).accept_all_map_quests()
                logger.info("Accepted %s available map quest(s) before execution", accepted)
            completed = QuestExecutor(self.gc, self.cfg).run_all_incomplete(self.max_quests)
            logger.info("Finished: %s quest(s) completed", completed)
            return completed
        finally:
            self.gc.close()


def main() -> None:
    parser = argparse.ArgumentParser(description="Sanguo Auto-Quest Bot")
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--dry-run", action="store_true", help="Capture/log without sending input")
    mode.add_argument("--live", action="store_true", help="Enable real mouse/keyboard input")
    parser.add_argument("--max-quests", type=int, default=0, help="Maximum quests this run (0 = all)")
    args = parser.parse_args()

    cfg = load_config()
    if args.dry_run:
        cfg["runtime"]["dry_run"] = True
    elif args.live:
        cfg["runtime"]["dry_run"] = False
    QuestBot(cfg, max_quests=max(0, args.max_quests)).start()


if __name__ == "__main__":
    main()
