"""SQLite-backed memory of quests seen and their logged outcomes. This is the
ground truth the RAG layer (src/memory/retrieve.py) retrieves over -- plain
structured facts, no embeddings stored here. Embeddings are recomputed on
read in embed.py since the corpus (a few hundred quest titles at most) is far
too small to justify a persisted vector index.
"""
from __future__ import annotations

import datetime as dt
import pathlib
import sqlite3
from dataclasses import dataclass
from typing import Optional

_SCHEMA = """
CREATE TABLE IF NOT EXISTS quests (
    key TEXT PRIMARY KEY,
    tag TEXT NOT NULL,
    title TEXT NOT NULL,
    first_seen_at TEXT NOT NULL,
    last_seen_at TEXT NOT NULL,
    times_seen INTEGER NOT NULL DEFAULT 1,
    last_status TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    quest_key TEXT NOT NULL,
    result TEXT NOT NULL,          -- 'done' | 'skipped' | 'failed'
    reward_text TEXT,
    minutes_spent REAL,
    note TEXT,
    recorded_at TEXT NOT NULL,
    FOREIGN KEY (quest_key) REFERENCES quests(key)
);
"""


@dataclass
class QuestRecord:
    key: str
    tag: str
    title: str
    first_seen_at: str
    last_seen_at: str
    times_seen: int
    last_status: str


@dataclass
class EventRecord:
    id: int
    quest_key: str
    result: str
    reward_text: Optional[str]
    minutes_spent: Optional[float]
    note: Optional[str]
    recorded_at: str


class MemoryStore:
    def __init__(self, db_path: pathlib.Path):
        db_path.parent.mkdir(parents=True, exist_ok=True)
        self.conn = sqlite3.connect(db_path)
        self.conn.row_factory = sqlite3.Row
        self.conn.executescript(_SCHEMA)
        self.conn.commit()

    def close(self) -> None:
        self.conn.close()

    def upsert_seen(self, key: str, tag: str, title: str, status: str) -> None:
        now = dt.datetime.now(dt.timezone.utc).isoformat()
        cur = self.conn.execute("SELECT times_seen FROM quests WHERE key = ?", (key,))
        row = cur.fetchone()
        if row is None:
            self.conn.execute(
                "INSERT INTO quests (key, tag, title, first_seen_at, last_seen_at, times_seen, last_status) "
                "VALUES (?, ?, ?, ?, ?, 1, ?)",
                (key, tag, title, now, now, status),
            )
        else:
            self.conn.execute(
                "UPDATE quests SET last_seen_at = ?, times_seen = times_seen + 1, last_status = ? WHERE key = ?",
                (now, status, key),
            )
        self.conn.commit()

    def log_event(
        self,
        quest_key: str,
        result: str,
        reward_text: Optional[str] = None,
        minutes_spent: Optional[float] = None,
        note: Optional[str] = None,
    ) -> None:
        now = dt.datetime.now(dt.timezone.utc).isoformat()
        self.conn.execute(
            "INSERT INTO events (quest_key, result, reward_text, minutes_spent, note, recorded_at) "
            "VALUES (?, ?, ?, ?, ?, ?)",
            (quest_key, result, reward_text, minutes_spent, note, now),
        )
        self.conn.commit()

    def all_quests(self) -> list[QuestRecord]:
        cur = self.conn.execute("SELECT * FROM quests ORDER BY last_seen_at DESC")
        return [QuestRecord(**dict(r)) for r in cur.fetchall()]

    def all_events(self) -> list[EventRecord]:
        cur = self.conn.execute("SELECT * FROM events ORDER BY recorded_at DESC")
        return [EventRecord(**dict(r)) for r in cur.fetchall()]

    def events_for_quest(self, quest_key: str) -> list[EventRecord]:
        cur = self.conn.execute(
            "SELECT * FROM events WHERE quest_key = ? ORDER BY recorded_at DESC", (quest_key,)
        )
        return [EventRecord(**dict(r)) for r in cur.fetchall()]

    def find_quest_key(self, tag: str, title_query: str) -> Optional[str]:
        """Fuzzy-ish lookup for the `log` CLI command: exact key match first,
        then a substring match on title so the user doesn't have to retype
        the exact title verbatim."""
        exact = f"{tag}::{title_query}".strip().lower()
        cur = self.conn.execute("SELECT key FROM quests WHERE key = ?", (exact,))
        row = cur.fetchone()
        if row:
            return row["key"]
        cur = self.conn.execute(
            "SELECT key FROM quests WHERE tag = ? AND title LIKE ? ORDER BY last_seen_at DESC LIMIT 1",
            (tag, f"%{title_query}%"),
        )
        row = cur.fetchone()
        return row["key"] if row else None


def open_store(cfg: dict) -> MemoryStore:
    from src.config import PROJECT_ROOT

    rel = cfg.get("memory", {}).get("db_path", "./data/memory.db")
    return MemoryStore(PROJECT_ROOT / rel)
