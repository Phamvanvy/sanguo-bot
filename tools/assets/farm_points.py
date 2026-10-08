"""Monster clusters of a map, in visiting order, for the "Farm quái" roam mode.

The server only sends the units in the 3x3 cells (120 px each) around us
(MapCell.cells), so the extension cannot see a whole map's monsters; it walks
from cluster to cluster instead. A cluster is the densest spot left among the
respawning monsters (faction 4, attackable, refresh <= --max-refresh s) with
everything within --radius px of it; it is kept with --min monsters or more.
The points are monster spawn tiles in game coordinates (map px / 8), ordered by
nearest neighbour from the first cluster, then shortened with 2-opt.

Usage:
  python tools/assets/farm_points.py 896 848 [--data selfhost/runtime/data]
"""
import argparse
import math
import re
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]


def monsters(info: str, max_refresh: int, ignore: set[str]) -> list[tuple[int, int]]:
    out = []
    for match in re.finditer(r"<npc ([^>]*)/?>", info):
        attrs = dict(re.findall(r'(\w+)="([^"]*)"', match.group(1)))
        refresh = int(attrs.get("refreshinterval", "-1"))
        if (attrs.get("faction") == "4" and attrs.get("canattack") == "1"
                and 0 < refresh <= max_refresh and attrs.get("name", "").strip() not in ignore):
            out.append((int(attrs["x"]), int(attrs["y"])))
    return out


def clusters(points: list[tuple[int, int]], radius: int, least: int) -> list[tuple[tuple[int, int], int]]:
    left = list(points)
    found = []
    while left:
        near = lambda p: [q for q in left if math.dist(p, q) <= radius]
        centre = max(left, key=lambda p: len(near(p)))
        members = near(centre)
        if len(members) < least:
            break
        found.append((centre, len(members)))
        left = [p for p in left if p not in members]
    return found


def tour(spots: list[tuple[int, int]]) -> list[tuple[int, int]]:
    if not spots:
        return []
    order = [spots[0]]
    rest = spots[1:]
    while rest:
        nxt = min(rest, key=lambda p: math.dist(order[-1], p))
        order.append(nxt)
        rest.remove(nxt)
    loop = lambda o: sum(math.dist(o[i], o[(i + 1) % len(o)]) for i in range(len(o)))
    improved = True
    while improved:
        improved = False
        for i in range(1, len(order) - 1):
            for j in range(i + 1, len(order)):
                candidate = order[:i] + order[i:j + 1][::-1] + order[j + 1:]
                if loop(candidate) < loop(order) - 1e-6:
                    order, improved = candidate, True
    return order


def main() -> None:
    sys.stdout.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser()
    parser.add_argument("maps", nargs="+", type=int, help="live map ids (area folder * 16)")
    parser.add_argument("--data", default=str(REPO / "selfhost/runtime/data"))
    parser.add_argument("--radius", type=int, default=160)
    parser.add_argument("--min", type=int, default=3)
    parser.add_argument("--max-refresh", type=int, default=300)
    parser.add_argument("--ignore", nargs="*", default=["Kế hoạch tam quốc minh châu"])
    args = parser.parse_args()
    for map_id in args.maps:
        info = (Path(args.data) / "Areas" / str(map_id >> 4) / "info.xml").read_text(encoding="utf-8")
        found = clusters(monsters(info, args.max_refresh, set(args.ignore)), args.radius, args.min)
        counts = {centre: count for centre, count in found}
        order = tour([centre for centre, _ in found])
        tiles = [[x // 8, y // 8] for x, y in order]
        print(f"{map_id}: {len(tiles)} cụm, {sum(counts.values())} quái")
        print(f"  roam_points: {tiles}".replace("], [", "], ["))
        print(f"  # số quái mỗi cụm: {[counts[c] for c in order]}")


if __name__ == "__main__":
    main()
