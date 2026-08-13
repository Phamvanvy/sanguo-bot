/*
 * Area index for the browser client.
 *
 * The client is given a map id by the server (GOMAP_ALLOW_SERVER) and has to find the area
 * package that holds it. Two facts make that a lookup rather than a computation: an area's
 * directory name is NOT reliably its area id (`87_1` holds area 87, but not by rule), and the
 * global map id is `(areaId << 4) | localMapId`. So the id lives inside each package's `0.stg`
 * header, and someone has to read all of them.
 *
 * Doing that in the browser would mean downloading ~96 packages to find one. Doing it here
 * costs one pass over ~60 MB of local files, once per bridge process, and the browser fetches
 * a few KB of JSON instead.
 */
import fs from 'node:fs';
import path from 'node:path';
import { GamePackage } from '../client/src/assets/package-file.js';

/**
 * Scan `<dataDir>/Areas/ * /client.pkg` and describe every map the client can enter.
 * @param {string} dataDir the server's data directory (the one peony.xml points at)
 * @returns {{areas: {dir:string, areaId:number, title:string, maps:number[]}[],
 *            mapToArea: Record<number, string>}}
 */
export function buildAreaIndex(dataDir) {
  const areasDir = path.join(dataDir, 'Areas');
  const areas = [];
  const mapToArea = {};
  const failures = [];

  for (const dir of fs.readdirSync(areasDir).sort()) {
    const file = path.join(areasDir, dir, 'client.pkg');
    if (!fs.existsSync(file)) continue;
    let pkg;
    try {
      pkg = new GamePackage(new Uint8Array(fs.readFileSync(file)));
    } catch (e) {
      // Not every directory under Areas/ is a well-formed package (CVS leftovers, partial
      // exports). One bad directory must not cost the client the other 95.
      failures.push(`${dir}: ${e.message}`);
      continue;
    }
    const maps = pkg.localMapIds.map((id) => pkg.globalMapId(id));
    areas.push({ dir, areaId: pkg.areaID, title: pkg.title, maps });
    for (const m of maps) {
      // First package wins, and a collision is worth knowing about: two packages claiming the
      // same map means the data directory is inconsistent.
      if (mapToArea[m] === undefined) mapToArea[m] = dir;
      else failures.push(`map ${m} is in both Areas/${mapToArea[m]} and Areas/${dir}`);
    }
  }

  return { areas, mapToArea, failures };
}

/** Cache the scan for the life of the process — the data directory is static, 2011 vintage. */
let cached = null;
export function areaIndex(dataDir) {
  if (!cached) cached = buildAreaIndex(dataDir);
  return cached;
}

/*
 * What the browser is allowed to fetch out of the data directory.
 *
 * The data directory is not a public asset bundle: it is the SERVER's, and most of it is
 * game logic — `scripts/` (the quest VM sources), `Quests/`, `npc.xml`, `config.xml`,
 * drop tables, and per-area `info.xml` / `game.map` (spawns, exits, collision). Serving the
 * whole tree hands all of that to anyone who can reach the bridge, so only the three
 * client-asset trees are reachable, and inside `Areas/` only the client packages — never
 * the map or info files that sit next to them.
 *
 * Anything the client legitimately needs from the rest of the tree should be computed here
 * and published as a small derived document (that is what /data/areas.json is), not by
 * widening this list.
 */
const DATA_ALLOW = [
  /^Areas\/[^/]+\/client(_l)?\.pkg$/,
  /^client_pkg\/.+$/,
  /^client_res\/.+$/,
];

/** True if `rel` (a root-relative, forward-slash path) may be served. */
export function isAllowedDataPath(rel) {
  if (typeof rel !== 'string' || rel === '' || rel.includes('\0')) return false;
  const segments = rel.split('/');
  // empty, dot-prefixed or backslash-bearing segments never appear in a legitimate asset
  // path; CVS holds the 2011 checkout metadata, which lists files we do not serve.
  if (segments.some((s) => s === '' || s.startsWith('.') || s === 'CVS' || s.includes('\\'))) {
    return false;
  }
  return DATA_ALLOW.some((re) => re.test(rel));
}

/**
 * Resolve a URL path under the data directory to a real file, or null if it escapes the
 * directory or is not a client asset. Callers must treat null as 403 — this is the only
 * thing standing between the bridge and the rest of the server's data (and the disk).
 */
export function resolveDataFile(dataDir, relPath) {
  const root = path.resolve(dataDir);
  const file = path.resolve(root, '.' + (relPath.startsWith('/') ? relPath : '/' + relPath));
  // containment first: path.resolve has already collapsed any ".." by here
  if (file !== root && !file.startsWith(root + path.sep)) return null;
  const rel = file.slice(root.length + 1).split(path.sep).join('/');
  if (!isAllowedDataPath(rel)) return null;
  return file;
}
