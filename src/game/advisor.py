"""Human-in-the-loop quest advisor: reads the quest panel, remembers what you
tell it about how quests went, and suggests what to do next. This module
never accepts, completes, or claims a quest -- every read here is
informational, and every suggestion is something YOU act on by hand in the
game window. See CLAUDE-facing note in src/game/quests.py for why.

Usage (from project root, venv active):

    # Capture + parse the currently-open quest panel, save to memory, print it.
    .venv/Scripts/python -m src.game.advisor read

    # Same, but also print a ranked "what to do next" suggestion grounded in
    # past logged outcomes (RAG over src/memory).
    .venv/Scripts/python -m src.game.advisor suggest

    # Record how a quest actually went, after you did it by hand -- this is
    # what makes future suggestions smarter, especially once few new quests
    # are left at higher levels.
    .venv/Scripts/python -m src.game.advisor log --tag 17 --title "tổ ong" \\
        --result done --minutes 4 --reward "200 vàng, 50 exp" --note "dễ, gần thành"
"""
from __future__ import annotations

import argparse
import json
import sys

from src.capture import open_control
from src.config import load_config
from src.game.quests import Quest, open_quest_panel, read_quest_panel
from src.memory.retrieve import RetrievedMemory, similar_quest_history
from src.memory.store import MemoryStore, open_store

_RESULTS = ("done", "skipped", "failed")


def _print_panel(reading) -> None:
    header = f"Nhiệm vụ quản lý ({reading.completed}/{reading.total})" if reading.completed is not None else "Nhiệm vụ quản lý"
    print(header)
    for q in reading.quests:
        flag = "" if q.status_source != "color-mismatch" else "  [!! LLM/color mismatch, trusted color]"
        mark = "x" if q.status == "done" else " "
        print(f"  [{mark}] [{q.tag}] {q.title:<40} ({q.status}){flag}")


def cmd_read(cfg: dict, navigate: bool) -> None:
    control = open_control(cfg)
    if navigate:
        open_quest_panel(control, cfg)
    reading = read_quest_panel(control, cfg)

    store = open_store(cfg)
    try:
        for q in reading.quests:
            store.upsert_seen(q.key, q.tag, q.title, q.status)
    finally:
        store.close()

    _print_panel(reading)


def _suggest_from_panel(cfg: dict, store: MemoryStore, quests: list[Quest]) -> None:
    pending = [q for q in quests if q.status == "not_done"]
    if not pending:
        print("Không có nhiệm vụ nào đang 'Chưa xong' trong panel hiện tại.")
        _suggest_from_memory_only(cfg, store)
        return

    per_quest_memory: dict[str, list[RetrievedMemory]] = {}
    for q in pending:
        per_quest_memory[q.key] = similar_quest_history(cfg, store, q.tag, q.title, k=2)

    context_lines = []
    for q in pending:
        mems = per_quest_memory[q.key]
        mem_text = "; ".join(m.summary_line() for m in mems) if mems else "chưa có lịch sử"
        context_lines.append(f"- [{q.tag}] {q.title} | lịch sử liên quan: {mem_text}")

    ranked = _rank_with_llm(cfg, context_lines)
    if ranked is None:
        print("(LLM ranking không khả dụng -- liệt kê nguyên trạng, chưa xếp hạng)")
        for line in context_lines:
            print(" ", line)
        return

    print("Gợi ý thứ tự nên làm (dựa trên lịch sử đã ghi lại):\n")
    for i, item in enumerate(ranked, 1):
        tag = str(item.get("tag", "")).strip("[]")
        print(f"{i}. [{tag}] {item.get('title')}")
        if item.get("reason"):
            print(f"   -> {item['reason']}")


def _suggest_from_memory_only(cfg: dict, store: MemoryStore) -> None:
    """Fallback for when the panel has no pending quests -- the scenario the
    advisor exists for at higher levels. Surfaces which quest *types* paid
    off best historically, purely from logged events, so you know what's
    worth doing again once something reopens (daily/weekly resets, new tier
    unlocks, etc.) instead of guessing."""
    events_by_quest: dict[str, list] = {}
    for q in store.all_quests():
        evs = store.events_for_quest(q.key)
        if evs:
            events_by_quest[q.key] = evs

    if not events_by_quest:
        print("Chưa có lịch sử nhiệm vụ nào được ghi lại (dùng lệnh 'log' sau khi làm xong một nhiệm vụ).")
        return

    print("Không có nhiệm vụ mới -- dưới đây là các nhiệm vụ từng đáng làm nhất theo lịch sử:\n")
    quest_by_key = {q.key: q for q in store.all_quests()}
    ranked_keys = sorted(
        events_by_quest.keys(),
        key=lambda k: sum(1 for e in events_by_quest[k] if e.result == "done"),
        reverse=True,
    )
    for key in ranked_keys[:5]:
        q = quest_by_key[key]
        mem = RetrievedMemory(quest=q, similarity=1.0, events=events_by_quest[key])
        print(" -", mem.summary_line())


def _rank_with_llm(cfg: dict, context_lines: list[str]):
    llm_cfg = cfg["llm"]
    if not llm_cfg.get("enabled"):
        return None
    try:
        from openai import OpenAI

        client = OpenAI(base_url=llm_cfg["base_url"], api_key=llm_cfg.get("api_key", "not-needed"))
        prompt = (
            "Bạn là cố vấn nhiệm vụ cho game webgame Tam Quốc. Dưới đây là các nhiệm vụ "
            "CHƯA XONG hiện tại, kèm lịch sử các nhiệm vụ tương tự đã từng làm (nếu có):\n\n"
            + "\n".join(context_lines)
            + "\n\nXếp hạng các nhiệm vụ theo thứ tự nên làm trước (ưu tiên phần thưởng tốt, "
            "tốn ít thời gian, và nhiệm vụ hàng ngày/tuần hoàn sắp hết hạn dùng nếu có dấu hiệu). "
            'Trả về CHỈ một JSON array, không giải thích thêm ngoài JSON: '
            '[{"tag": "...", "title": "...", "reason": "một câu ngắn"}, ...] '
            "theo đúng thứ tự ưu tiên giảm dần, đủ hết các nhiệm vụ được liệt kê ở trên."
        )
        resp = client.chat.completions.create(
            model=llm_cfg["model"],
            temperature=0.2,
            messages=[{"role": "user", "content": prompt}],
        )
        raw = resp.choices[0].message.content or ""
        import re

        match = re.search(r"\[.*\]", raw, re.DOTALL)
        if not match:
            return None
        return json.loads(match.group(0))
    except Exception as exc:  # noqa: BLE001
        print(f"(LLM ranking lỗi: {exc})", file=sys.stderr)
        return None


def cmd_suggest(cfg: dict, navigate: bool) -> None:
    control = open_control(cfg)
    if navigate:
        open_quest_panel(control, cfg)
    reading = read_quest_panel(control, cfg)

    store = open_store(cfg)
    try:
        for q in reading.quests:
            store.upsert_seen(q.key, q.tag, q.title, q.status)
        _suggest_from_panel(cfg, store, reading.quests)
    finally:
        store.close()


def cmd_log(cfg: dict, args: argparse.Namespace) -> None:
    store = open_store(cfg)
    try:
        key = store.find_quest_key(args.tag, args.title)
        if key is None:
            key = f"{args.tag}::{args.title}".strip().lower()
            print(
                f"(chưa từng thấy nhiệm vụ này qua lệnh 'read' -- ghi tạm với key={key!r}, "
                "chạy 'read' trước để khớp chính xác hơn lần sau)"
            )
        store.log_event(
            quest_key=key,
            result=args.result,
            reward_text=args.reward,
            minutes_spent=args.minutes,
            note=args.note,
        )
        print(f"Đã ghi: {key} -> {args.result}")
    finally:
        store.close()


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="cmd", required=True)

    p_read = sub.add_parser("read", help="capture + parse the quest panel, save to memory")
    p_read.add_argument("--no-nav", action="store_true", help="assume the panel is already open")

    p_suggest = sub.add_parser("suggest", help="read + rank pending quests using memory (RAG)")
    p_suggest.add_argument("--no-nav", action="store_true", help="assume the panel is already open")

    p_log = sub.add_parser("log", help="record how a quest went (you did it by hand)")
    p_log.add_argument("--tag", required=True)
    p_log.add_argument("--title", required=True, help="full or partial title, matched against memory")
    p_log.add_argument("--result", required=True, choices=_RESULTS)
    p_log.add_argument("--minutes", type=float, default=None)
    p_log.add_argument("--reward", default=None)
    p_log.add_argument("--note", default=None)

    args = parser.parse_args()
    cfg = load_config()

    if args.cmd == "read":
        cmd_read(cfg, navigate=not args.no_nav)
    elif args.cmd == "suggest":
        cmd_suggest(cfg, navigate=not args.no_nav)
    elif args.cmd == "log":
        cmd_log(cfg, args)


if __name__ == "__main__":
    main()
