#!/usr/bin/env node
/*
 * Walkability grids for the browser extension's dungeon routes.
 *
 * Port of GameView.rebuildMapCollisionData + GamePackage.loadNPCCollision: a map
 * cell is blocked when the TOP tile of its background stack has info bit 0 set,
 * and every ground / role decor object blocks the path tiles under its npc.col
 * boxes. Path tiles are map cells halved (PATH_SHIFT = 1) - 8x8 px on the
 * 16x16 blurred maps, which is also the unit of the coordinates the game prints
 * next to the map name.
 *
 * The extension plans each walk on these grids so every Map click lands on a
 * spot the game's own pathfinder can reach (see extension/content.js walkToCoord).
 *
 * Usage:
 *   node tools/assets/walk_grid.mjs [--data <dir>] [--out <file>] [mapId ...]
 *   node tools/assets/walk_grid.mjs --check 977 "8,89 20,31"   (print blocked of each x,y)
 */
import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { fileURLToPath } from 'node:url';
import { GamePackage } from '../../web/client/src/assets/package-file.js';
import { DataIn } from '../../web/client/src/assets/data-in.js';
import { createBlurMapBuffer, BACKGROUND_PRECISE } from '../../web/client/src/assets/game-map.js';
import { decodePNG } from './png.mjs';

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const codecs = {
  inflate: (b) => new Uint8Array(zlib.gunzipSync(Buffer.from(b.buffer, b.byteOffset, b.length))),
  decodePNG,
};

// Dungeon maps the extension routes walk (live ids = the data files' ids):
// Cổ Mộ lobby + easy 769-773 + hard 1137-1141, Hà Đông 432/433/448-450,
// Thiên Long lobby 976 + trận 977/978.
const DEFAULT_MAPS = [
  768, 769, 770, 771, 772, 773, 1137, 1138, 1139, 1140, 1141,
  432, 433, 448, 449, 450, 976, 977, 978,
];

/** GamePackage.loadNPCCollision: per decor animation, its blocking boxes. */
function loadNpcCollision(pkg) {
  const dis = new DataIn(pkg.requireFile('/npc.col'));
  const count = dis.readShort();
  const out = [];
  for (let i = 0; i < count; i += 1) {
    const boxes = [];
    for (let j = dis.readUnsignedByte(); j > 0; j -= 1) {
      // (short)(packed >> 16) / (short)(packed & 0xFFFF): signed offsets from the decor's anchor.
      const x = dis.readShort();
      const y = dis.readShort();
      boxes.push({ x, y, w: dis.readUnsignedByte(), h: dis.readUnsignedByte() });
    }
    out.push(boxes);
  }
  return out;
}

/** GameView.rebuildMapCollisionData. @returns {{w:number, h:number, blocked:Uint8Array}} */
export function walkGrid(pkg, map) {
  const tileW = map.cellWidth;
  const tileH = map.cellHeight;
  const w = map.cols << 1;
  const h = map.rows << 1;
  const blocked = new Uint8Array(w * h);
  const stamp = (i, j) => {
    for (let dy = 0; dy < 2; dy += 1) {
      for (let dx = 0; dx < 2; dx += 1) blocked[((i << 1) + dy) * w + (j << 1) + dx] = 1;
    }
  };
  if (map.backgroundType === BACKGROUND_PRECISE) {
    const { info } = pkg.getTileInfo();
    for (let i = 0; i < map.rows; i += 1) {
      for (let j = 0; j < map.cols; j += 1) if (info[map.mapData[i][j] & 0xff] & 1) stamp(i, j);
    }
  } else {
    const landforms = pkg.loadAllLandformImages();
    const infos = landforms.map((_, lid) => pkg.loadLandformTileInfo(lid));
    const buffer = createBlurMapBuffer(map, landforms);
    for (let i = 0; i < map.rows; i += 1) {
      for (let j = 0; j < map.cols; j += 1) {
        const cc = buffer[i][j];
        // Only the top layer decides (GameView: "通过性只判断最上层").
        let lfid;
        let fid;
        if ((cc & 0x7ff) !== 0) {
          lfid = (cc >> 7) & 0x0f;
          fid = (cc & 0x1f) - 1;
        } else if ((cc & 0x3ff800) !== 0) {
          lfid = (cc >> 18) & 0x0f;
          fid = ((cc >> 11) & 0x1f) - 1;
        } else {
          lfid = (cc >> 29) & 0x07;
          fid = ((cc >> 22) & 0x1f) - 1;
        }
        if (fid >= 0 && infos[lfid] && (infos[lfid][fid] & 1) === 1) stamp(i, j);
      }
    }
  }
  const collision = loadNpcCollision(pkg);
  const pathW = tileW >> 1;
  const pathH = tileH >> 1;
  const clampX = (v) => Math.min(w - 1, Math.max(0, v));
  const clampY = (v) => Math.min(h - 1, Math.max(0, v));
  for (const npc of [...map.groundNPCs, ...map.roleNPCs]) {
    for (const box of collision[npc.animateId] || []) {
      const x = box.x + npc.x;
      const y = box.y + npc.y;
      // Java int division truncates toward zero.
      const startX = clampX(Math.trunc(x / pathW));
      const startY = clampY(Math.trunc(y / pathH));
      const endX = clampX(Math.trunc((x + box.w) / pathW));
      const endY = clampY(Math.trunc((y + box.h) / pathH));
      for (let cy = startY; cy <= endY; cy += 1) {
        for (let cx = startX; cx <= endX; cx += 1) blocked[cy * w + cx] = 1;
      }
    }
  }
  return { w, h, pathW, pathH, blocked };
}

function findMap(dataDir, mapId) {
  const areasDir = path.join(dataDir, 'Areas');
  for (const dir of fs.readdirSync(areasDir).sort()) {
    const file = path.join(areasDir, dir, 'client.pkg');
    if (!fs.existsSync(file)) continue;
    let pkg;
    try {
      pkg = new GamePackage(new Uint8Array(fs.readFileSync(file)), codecs);
    } catch {
      continue;
    }
    if (pkg.areaID !== mapId >> 4 || !pkg.localMapIds.includes(mapId & 0x0f)) continue;
    return { pkg, map: pkg.loadMap(mapId & 0x0f) };
  }
  throw new Error(`map ${mapId} not found under ${areasDir}`);
}

function toBase64Bits(blocked) {
  const bytes = new Uint8Array(Math.ceil(blocked.length / 8));
  for (let i = 0; i < blocked.length; i += 1) if (blocked[i]) bytes[i >> 3] |= 1 << (i & 7);
  return Buffer.from(bytes).toString('base64');
}

function main(argv) {
  let data = path.join(REPO, 'selfhost/runtime/data');
  let out = path.join(REPO, 'extension/walk_grids.js');
  let check = null;
  const ids = [];
  for (let i = 0; i < argv.length; i += 1) {
    if (argv[i] === '--data') data = argv[++i];
    else if (argv[i] === '--out') out = argv[++i];
    else if (argv[i] === '--check') check = { id: Number(argv[++i]), points: argv[++i] };
    else ids.push(Number(argv[i]));
  }
  if (check) {
    const { pkg, map } = findMap(data, check.id);
    const grid = walkGrid(pkg, map);
    for (const point of check.points.trim().split(/\s+/)) {
      const [x, y] = point.split(',').map(Number);
      console.log(`${point}\t${grid.blocked[y * grid.w + x] ? 'BLOCKED' : 'ok'}`);
    }
    return;
  }
  const grids = {};
  for (const id of ids.length ? ids : DEFAULT_MAPS) {
    const { pkg, map } = findMap(data, id);
    const grid = walkGrid(pkg, map);
    const open = grid.blocked.length - grid.blocked.reduce((a, b) => a + b, 0);
    grids[id] = {
      name: map.name.trim(),
      size: [map.width, map.height],
      unit: grid.pathW,
      w: grid.w,
      h: grid.h,
      // Map exits (portals) from the game data, as path tiles.
      exits: map.exits.map((exit) => [Math.floor(exit.x / grid.pathW), Math.floor(exit.y / grid.pathH)]),
      blocked: toBase64Bits(grid.blocked),
    };
    console.log(`${id}\t${map.name.trim()}\t${map.width}x${map.height}\tgrid ${grid.w}x${grid.h}`
      + ` of ${grid.pathW}x${grid.pathH}px\t${open} open`);
  }
  const body = `// Generated by tools/assets/walk_grid.mjs from the game's own map data - do not edit.\n`
    + `// Per map id: size in pixels, unit = pixels per path tile (= one game coordinate),\n`
    + `// blocked = row-major bitset of path tiles the game's pathfinder will not enter,\n`
    + `// exits = the map's portals as path tiles.\n`
    + `globalThis.SANGUO_WALK_GRIDS = ${JSON.stringify(grids, null, 1)};\n`;
  fs.writeFileSync(out, body);
  console.log(`wrote ${path.relative(REPO, out)} (${body.length} bytes)`);
}

main(process.argv.slice(2));
