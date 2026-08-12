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

/**
 * Resolve a URL path under the data directory to a real file, or null if it escapes.
 * Callers must treat null as 403/404 — this is the only thing standing between a dev server
 * and the rest of the disk.
 */
export function resolveDataFile(dataDir, relPath) {
  const root = path.resolve(dataDir);
  const file = path.resolve(root, '.' + (relPath.startsWith('/') ? relPath : '/' + relPath));
  if (file !== root && !file.startsWith(root + path.sep)) return null;
  return file;
}
