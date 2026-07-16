"""RAG retrieval: given a quest currently on screen, find similar past quests
that have logged outcomes (see MemoryStore.log_event, wired to `advisor.py
log`) and summarize them for the advisor's prompt. This is what lets
suggestions stay useful once new quests become scarce at higher levels --
the advisor reasons from what similar quests paid off before, not just what's
in the current panel.
"""
from __future__ import annotations

from dataclasses import dataclass

from src.memory.embed import get_embedder
from src.memory.store import EventRecord, MemoryStore, QuestRecord

_TAG_MATCH_BOOST = 0.15
_MIN_SIMILARITY = 0.12


@dataclass
class RetrievedMemory:
    quest: QuestRecord
    similarity: float
    events: list[EventRecord]

    def summary_line(self) -> str:
        if not self.events:
            return f"[{self.quest.tag}] {self.quest.title}: seen {self.quest.times_seen}x, no logged outcome yet"
        results = [e.result for e in self.events]
        done = results.count("done")
        skipped = results.count("skipped")
        failed = results.count("failed")
        minutes = [e.minutes_spent for e in self.events if e.minutes_spent is not None]
        avg_min = f"{sum(minutes) / len(minutes):.1f}min avg" if minutes else "time unknown"
        rewards = [e.reward_text for e in self.events if e.reward_text]
        reward_note = f", reward: {rewards[-1]}" if rewards else ""
        notes = [e.note for e in self.events if e.note]
        note_part = f" -- note: {notes[-1]}" if notes else ""
        return (
            f"[{self.quest.tag}] {self.quest.title}: {done} done / {skipped} skipped / "
            f"{failed} failed, {avg_min}{reward_note}{note_part}"
        )


def similar_quest_history(
    cfg: dict, store: MemoryStore, tag: str, title: str, k: int = 3
) -> list[RetrievedMemory]:
    candidates = [q for q in store.all_quests() if store.events_for_quest(q.key)]
    if not candidates:
        return []

    embedder = get_embedder(cfg)
    corpus = [f"[{q.tag}] {q.title}" for q in candidates]
    sims = embedder.similarities(f"[{tag}] {title}", corpus)

    scored = []
    for q, sim in zip(candidates, sims):
        score = float(sim) + (_TAG_MATCH_BOOST if q.tag == tag else 0.0)
        scored.append((score, q))
    scored.sort(key=lambda pair: pair[0], reverse=True)

    out: list[RetrievedMemory] = []
    for score, q in scored:
        if len(out) >= k:
            break
        if score < _MIN_SIMILARITY and q.tag != tag:
            continue
        out.append(RetrievedMemory(quest=q, similarity=score, events=store.events_for_quest(q.key)))
    return out
