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


def monsters(info: str, min_refresh: int, max_refresh: int, ignore: set[str]) -> list[tuple[int, int]]:
    out = []
    for match in re.finditer(r"<npc ([^>]*)/?>", info):
        attrs = dict(re.findall(r'(\w+)="([^"]*)"', match.group(1)))
        refresh = int(attrs.get("refreshinterval", "-1"))
        if (attrs.get("faction") == "4" and attrs.get("canattack") == "1"
                and min_refresh <= refresh <= max_refresh and attrs.get("name", "").strip() not in ignore):
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


def read_info(data_dirs: list[str], map_id: int) -> str:
    """The first data dir's info.xml for the map's area, in its declared encoding."""
    for data in data_dirs:
        path = Path(data) / "Areas" / str(map_id >> 4) / "info.xml"
        if path.exists():
            raw = path.read_bytes()
            declared = re.search(rb'encoding="([^"]+)"', raw[:200])
            return raw.decode(declared.group(1).decode() if declared else "utf-8", errors="replace")
    raise SystemExit(f"map {map_id}: no Areas/{map_id >> 4}/info.xml under {', '.join(data_dirs)}")


def main() -> None:
    sys.stdout.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser()
    parser.add_argument("maps", nargs="+", type=int, help="live map ids (area folder * 16)")
    # Searched in turn: the 2011 data, then the later one with Ô Sào 2000, Nam
    # Việt 2016 and the live Nam Hải 2032 (its info.xml is GBK).
    parser.add_argument("--data", action="append",
                        help="data dir(s); default selfhost/runtime/data, then Game/Sanguo1.0-Data/data")
    parser.add_argument("--radius", type=int, default=160)
    parser.add_argument("--min", type=int, default=3)
    parser.add_argument("--max-refresh", type=int, default=300)
    # Faster than 90 s are festival ones (20 s "fake teacher", 60 s zongzi
    # spirits on Nam Việt / Nam Hải) the live maps may not have; the year-round
    # ones respawn in 90-120 s.
    parser.add_argument("--min-refresh", type=int, default=90)
    parser.add_argument("--ignore", nargs="*", default=["Kế hoạch tam quốc minh châu"])
    # patrol_points: the densest cluster and the next densest ones within
    # --patrol-within px of it, at most --patrol of them - a short run back and
    # forth between a few full spots instead of the whole map.
    parser.add_argument("--patrol", type=int, default=3)
    parser.add_argument("--patrol-within", type=int, default=480)
    parser.add_argument("--skip", nargs="*", default=[], help="x,y tiles to leave out (unreachable on foot)")
    args = parser.parse_args()
    skip = {tuple(map(int, item.split(","))) for item in args.skip}
    data = args.data or [str(REPO / "selfhost/runtime/data"), str(REPO / "Game/Sanguo1.0-Data/data")]
    for map_id in args.maps:
        info = read_info(data, map_id)
        found = [(c, n) for c, n in clusters(monsters(info, args.min_refresh, args.max_refresh, set(args.ignore)), args.radius, args.min)
                 if (c[0] // 8, c[1] // 8) not in skip]
        counts = {centre: count for centre, count in found}
        order = tour([centre for centre, _ in found])
        tiles = lambda points: [[x // 8, y // 8] for x, y in points]
        print(f"{map_id}: {len(order)} cụm, {sum(counts.values())} quái")
        print(f"  roam_points: {tiles(order)}")
        print(f"  # số quái mỗi cụm: {[counts[c] for c in order]}")
        best = found[0][0]
        near = [c for c, _ in found if math.dist(c, best) <= args.patrol_within][:args.patrol]
        patrol = tour(near)
        print(f"  patrol_points: {tiles(patrol)}")
        print(f"  # số quái mỗi bãi: {[counts[c] for c in patrol]}")


if __name__ == "__main__":
    main()
