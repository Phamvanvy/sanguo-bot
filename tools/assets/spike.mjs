#!/usr/bin/env node
/*
 * G3a asset feasibility spike.
 *
 * Proves the original game's art can be extracted and drawn, without converting anything
 * wholesale: it decodes ONE area package straight out of the server's live data directory and
 * writes PNGs -- a single tile, a single sprite frame, and the whole spawn map composited from
 * its tiles + map decor.
 *
 * Everything is a software rasteriser here on purpose: the point is to verify the decoders
 * against files you can open, before any renderer or canvas code exists.
 *
 * Usage:
 *   node tools/assets/spike.mjs [--map 1395] [--data <dir>] [--out <dir>] [--sprite <file.pip>]
 *
 *   --map     global map id (default 1395, the character spawn map). areaId = id >> 4.
 *   --data    server data dir (default selfhost/runtime/data)
 *   --out     output dir      (default web/client/assets/spike)
 *   --sprite  a .ctn animate set or .pip image to dump frames from
 *             (default <data>/client_pkg/Flash/male.ctn, the player character)
 *   --at      "x,y" where to stand the character on the map (default: the G2 spawn point)
 */
import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { fileURLToPath } from 'node:url';
import { GamePackage, tileFlags } from '../../web/client/src/assets/package-file.js';
import { PipImage } from '../../web/client/src/assets/pip-image.js';
import { PipAnimateSet } from '../../web/client/src/assets/pip-animate-set.js';
import { createBlurMapBuffer, decodeBlurCell, BACKGROUND_PRECISE } from '../../web/client/src/assets/game-map.js';
import { encodePNG, decodePNG } from './png.mjs';

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');

// Java's Tool.inflate reads a gzip stream; the browser equivalent would be DecompressionStream.
const codecs = {
  inflate: (b) => new Uint8Array(zlib.gunzipSync(Buffer.from(b.buffer, b.byteOffset, b.length))),
  decodePNG,
};

function parseArgs(argv) {
  const args = {
    map: 1395,
    data: path.join(REPO, 'selfhost/runtime/data'),
    out: path.join(REPO, 'web/client/assets/spike'),
    sprite: null,
    at: null,
  };
  for (let i = 0; i < argv.length; i += 2) {
    const key = argv[i].replace(/^--/, '');
    const val = argv[i + 1];
    if (!(key in args)) throw new Error(`unknown option ${argv[i]}`);
    if (key === 'map') args.map = Number(val);
    else if (key === 'at') {
      const [x, y] = val.split(',').map(Number);
      if (!Number.isFinite(x) || !Number.isFinite(y)) throw new Error('--at expects "x,y"');
      args.at = { x, y };
    } else args[key] = val;
  }
  return args;
}

/* ---------- a tiny RGBA canvas ---------- */

function createCanvas(width, height, fill = [0, 0, 0, 0]) {
  const rgba = new Uint8Array(width * height * 4);
  for (let i = 0; i < width * height; i++) rgba.set(fill, i * 4);
  return { width, height, rgba };
}

/** Source-over alpha blit, clipped to the destination. */
function blit(dst, src, dx, dy) {
  for (let y = 0; y < src.height; y++) {
    const ty = dy + y;
    if (ty < 0 || ty >= dst.height) continue;
    for (let x = 0; x < src.width; x++) {
      const tx = dx + x;
      if (tx < 0 || tx >= dst.width) continue;
      const s = (y * src.width + x) * 4;
      const a = src.rgba[s + 3];
      if (a === 0) continue;
      const d = (ty * dst.width + tx) * 4;
      if (a === 255) {
        dst.rgba[d] = src.rgba[s];
        dst.rgba[d + 1] = src.rgba[s + 1];
        dst.rgba[d + 2] = src.rgba[s + 2];
        dst.rgba[d + 3] = 255;
      } else {
        const ia = 255 - a;
        for (let c = 0; c < 3; c++) {
          dst.rgba[d + c] = Math.round((src.rgba[s + c] * a + dst.rgba[d + c] * ia) / 255);
        }
        dst.rgba[d + 3] = Math.min(255, a + Math.round((dst.rgba[d + 3] * ia) / 255));
      }
    }
  }
}

function drawCrosshair(dst, cx, cy, color, r = 6) {
  for (let d = -r; d <= r; d++) {
    for (const [x, y] of [[cx + d, cy], [cx, cy + d]]) {
      if (x < 0 || x >= dst.width || y < 0 || y >= dst.height) continue;
      const o = (y * dst.width + x) * 4;
      dst.rgba.set(color, o);
    }
  }
}

function writePNG(outDir, name, img) {
  const file = path.join(outDir, name);
  fs.writeFileSync(file, encodePNG(img.rgba, img.width, img.height));
  console.log(`  wrote ${path.relative(REPO, file)}  (${img.width}x${img.height})`);
  return file;
}

/* ---------- area lookup ---------- */

/**
 * Areas live in directories whose names ("87", "87_1", "12_1") do not reliably equal the area
 * id, so the id is read out of each package's own header. The map id encodes it: (areaId << 4)
 * | localMapId, per GamePackage.loadMap.
 */
function findAreaPackage(dataDir, globalMapId) {
  const wantArea = globalMapId >> 4;
  const localMapId = globalMapId & 0x0f;
  const areasDir = path.join(dataDir, 'Areas');
  const candidates = fs.readdirSync(areasDir)
    .filter((d) => fs.existsSync(path.join(areasDir, d, 'client.pkg')))
    // A directory literally named after the area id is the overwhelmingly common case; try
    // those first so the usual lookup is one file read instead of a hundred.
    .sort((a, b) => (parseInt(a, 10) === wantArea ? -1 : 0) - (parseInt(b, 10) === wantArea ? -1 : 0));

  for (const dir of candidates) {
    const file = path.join(areasDir, dir, 'client.pkg');
    let pkg;
    try {
      pkg = new GamePackage(new Uint8Array(fs.readFileSync(file)), codecs);
    } catch {
      continue;  // not every directory under Areas/ is a well-formed package
    }
    if (pkg.areaID === wantArea && pkg.localMapIds.includes(localMapId)) {
      return { pkg, dir, localMapId };
    }
  }
  throw new Error(`no area package under ${areasDir} provides map ${globalMapId} `
    + `(area ${wantArea}, local map ${localMapId})`);
}

/* ---------- map rendering ---------- */

/** Precise background (layer type 0): every cell indexes tile.ts, which indexes tile.pip. */
function renderPreciseMap(pkg, map, canvas, stats) {
  const tileImage = pkg.loadTileImage();
  const { frames, info } = pkg.getTileInfo();
  const cache = new Map();
  for (let row = 0; row < map.rows; row++) {
    for (let col = 0; col < map.cols; col++) {
      const cell = map.mapData[row][col];
      if (cell >= frames.length) { stats.missing++; continue; }
      const frame = frames[cell];
      const trans = tileFlags(info[cell]).trans;
      const key = `${frame}:${trans}`;
      if (!cache.has(key)) cache.set(key, tileImage.frameToRGBA(frame, trans));
      blit(canvas, cache.get(key), col * map.cellWidth, row * map.cellHeight);
      stats.tiles++;
    }
  }
}

/**
 * Blurred background (layer type 1): tiles are regenerated from the map's seed, then up to 3
 * are stacked per cell. Frame -1 means "the landform had nothing for that corner pattern".
 */
function renderBlurredMap(pkg, map, canvas, stats) {
  const landforms = pkg.loadAllLandformImages();
  const buffer = createBlurMapBuffer(map, landforms);
  const cache = new Map();
  for (let row = 0; row < map.rows; row++) {
    for (let col = 0; col < map.cols; col++) {
      for (const layer of decodeBlurCell(buffer[row][col])) {
        const lf = landforms[layer.landform];
        if (!lf || layer.frame < 0 || layer.frame >= lf.frameCount) { stats.missing++; continue; }
        const key = `${layer.landform}:${layer.frame}:${layer.trans}`;
        if (!cache.has(key)) cache.set(key, lf.image.frameToRGBA(layer.frame, layer.trans));
        blit(canvas, cache.get(key), col * map.cellWidth, row * map.cellHeight);
        stats.tiles++;
      }
    }
  }
  return { landforms, buffer };
}

/**
 * Map decor ("still map NPCs"): trees, buildings, fences. Positions are pixel coordinates and
 * the art comes from npc.anp, a nested package whose entry 0 is the animation table and whose
 * remaining entries are .pip images. Drawn ground -> role -> sky, which is the client's paint
 * order, and within a layer sorted by y so nearer objects overlap farther ones.
 */
function drawDecor(pkg, map, canvas, stats) {
  const anp = pkg.getFile('/npc.anp');
  if (!anp) { console.log('  (no npc.anp in this package -- skipping map decor)'); return; }
  const anim = PipAnimateSet.fromPackage(anp, codecs);
  console.log(`  npc.anp: ${anim.images.length} images, ${anim.frameCount} frames, `
    + `${anim.animateCount} animations, v${anim.version}`);

  const cache = new Map();
  for (const layer of [map.groundNPCs, map.roleNPCs, map.skyNPCs]) {
    for (const npc of [...layer].sort((a, b) => a.y - b.y || a.x - b.x)) {
      if (npc.animateId < 0 || npc.animateId >= anim.animateCount) { stats.decorMissing++; continue; }
      // time 0 = the first step of the animation, i.e. the pose a screenshot would show.
      const pieces = anim.resolveAnimate(npc.animateId, 0);
      if (pieces.length === 0) { stats.decorMissing++; continue; }
      for (const p of pieces) {
        if (!p.image) { stats.decorMissing++; continue; }
        const key = `${anim.images.indexOf(p.image)}:${p.pipFrame}:${p.trans}`;
        if (!cache.has(key)) {
          try {
            cache.set(key, p.image.frameToRGBA(p.pipFrame, p.trans));
          } catch (e) {
            cache.set(key, null);
            console.log(`  (decor piece ${key} failed: ${e.message})`);
          }
        }
        const img = cache.get(key);
        if (!img) { stats.decorMissing++; continue; }
        blit(canvas, img, npc.x + p.x, npc.y + p.y);
        stats.decor++;
      }
    }
  }
}

/* ---------- sprite dump ---------- */

/**
 * Composite one animation frame of a .ctn animate set into its own tight image.
 * Piece offsets are relative to the unit's anchor and are often negative (the anchor sits at
 * the character's feet), so the bounding box has to be measured before anything is blitted.
 */
function compositeAnimate(set, animateId, timeOrStep) {
  const pieces = set.resolveAnimate(animateId, timeOrStep)
    .map((p) => ({ ...p, img: p.image ? p.image.frameToRGBA(p.pipFrame, p.trans) : null }))
    .filter((p) => p.img);
  if (pieces.length === 0) return null;
  const minX = Math.min(...pieces.map((p) => p.x));
  const minY = Math.min(...pieces.map((p) => p.y));
  const maxX = Math.max(...pieces.map((p) => p.x + p.img.width));
  const maxY = Math.max(...pieces.map((p) => p.y + p.img.height));
  const canvas = createCanvas(maxX - minX, maxY - minY);
  for (const p of pieces) blit(canvas, p.img, p.x - minX, p.y - minY);
  return { canvas, anchorX: -minX, anchorY: -minY, pieces: pieces.length };
}

function distinctFrames(set, animateId) {
  return new Set(set.animateSteps(animateId).map((s) => s.frame)).size;
}

/** Lay images out in a padded grid on a dark background. */
function contactSheet(images, columns, bg = [24, 24, 32, 255], pad = 2) {
  const cols = Math.min(columns, images.length);
  const rows = Math.ceil(images.length / cols);
  const cw = Math.max(...images.map((i) => i.width)) + pad;
  const ch = Math.max(...images.map((i) => i.height)) + pad;
  const sheet = createCanvas(cols * cw + pad, rows * ch + pad, bg);
  images.forEach((img, i) => {
    blit(sheet, img, pad + (i % cols) * cw, pad + Math.floor(i / cols) * ch);
  });
  return sheet;
}

/**
 * A .ctn animate set: the player character (male.ctn) and every unit/effect are built this
 * way -- a definition file plus the .pip images it names, resolved from the same directory.
 */
function dumpAnimateSet(file, outDir) {
  const dir = path.dirname(file);
  const name = path.basename(file, '.ctn');
  const def = new Uint8Array(fs.readFileSync(file));
  const probe = new PipAnimateSet([], def);
  const images = probe.imageNames.map((n) => {
    const p = path.join(dir, n);
    if (!fs.existsSync(p)) { console.log(`  missing source image ${n}`); return null; }
    return new PipImage(new Uint8Array(fs.readFileSync(p)), codecs);
  });
  const set = new PipAnimateSet(images, def);
  console.log(`  ${path.basename(file)}: v${set.version} ${set.frameCount} frames, `
    + `${set.animateCount} animations, images=[${set.imageNames.join(' ')}]`);

  // The animation with the most distinct frames is the one worth looking at: it shows the
  // per-step composition a renderer has to play back, not just a single pose.
  let best = 0;
  for (let a = 1; a < set.animateCount; a++) {
    if (distinctFrames(set, a) > distinctFrames(set, best)) best = a;
  }
  const steps = set.animateSteps(best);
  const cycle = steps.map((s) => compositeAnimate(set, best, s)).filter(Boolean);
  if (cycle.length) {
    console.log(`  animation ${best}: ${steps.length} steps (${distinctFrames(set, best)} distinct `
      + `frames, ${set.animateDuration(best)} ticks); first frame ${cycle[0].canvas.width}x`
      + `${cycle[0].canvas.height} from ${cycle[0].pieces} pieces, anchor `
      + `(${cycle[0].anchorX},${cycle[0].anchorY})`);
    writePNG(outDir, `sprite_${name}_frame0.png`, cycle[0].canvas);
    writePNG(outDir, `sprite_${name}_frame0_4x.png`, upscale(cycle[0].canvas, 4));
    writePNG(outDir, `sprite_${name}_anim${best}_cycle.png`, contactSheet(cycle.map((c) => c.canvas), cycle.length));
  }

  // Every animation's first frame: the full pose/direction inventory in one image.
  const poses = [];
  for (let a = 0; a < set.animateCount; a++) {
    const c = compositeAnimate(set, a, 0);
    if (c) poses.push(c.canvas);
  }
  console.log(`  ${poses.length}/${set.animateCount} animations composite at time 0`);
  if (poses.length) writePNG(outDir, `sprite_${name}_poses.png`, contactSheet(poses, 13));
  return set;
}

function dumpSprite(file, outDir, maxFrames = 8) {
  const bytes = new Uint8Array(fs.readFileSync(file));
  const pip = new PipImage(bytes, codecs);
  const name = path.basename(file, path.extname(file));
  console.log(`  ${path.basename(file)}: magic=${pip.magic} palettes=${pip.paletteCount} `
    + `blocks=${pip.blockCount} drawableFrames=${pip.frameCount} bpp=${pip.bytesPerPixel}`);

  const written = [];
  const n = Math.min(maxFrames, pip.frameCount);
  for (let f = 0; f < n; f++) {
    const img = pip.frameToRGBA(f);
    if (img.width === 0 || img.height === 0) continue;
    written.push(writePNG(outDir, `sprite_${name}_frame${f}.png`, img));
  }

  // A contact sheet of the first palette's blocks makes the whole sprite readable at a glance.
  const blocks = [];
  for (let b = 0; b < pip.blockCount; b++) blocks.push(pip.frameToRGBA(b));
  written.push(writePNG(outDir, `sprite_${name}_sheet.png`, contactSheet(blocks, Math.min(blocks.length, 16))));
  return { pip, written };
}

/* ---------- main ---------- */

function main() {
  const args = parseArgs(process.argv.slice(2));
  fs.mkdirSync(args.out, { recursive: true });

  console.log(`data dir : ${args.data}`);
  console.log(`out dir  : ${args.out}`);

  const { pkg, dir, localMapId } = findAreaPackage(args.data, args.map);
  console.log(`\n[area] Areas/${dir}  areaID=${pkg.areaID} title=${JSON.stringify(pkg.title)}`);
  console.log(`  tile ${pkg.tileWidth}x${pkg.tileHeight}  blurTile ${pkg.blurTileWidth}x${pkg.blurTileHeight}`
    + `  landforms=${pkg.landformCount}  maps=[${pkg.localMapIds}]`);
  console.log(`  entries: ${pkg.fileNames.join(' ')}`);

  const map = pkg.loadMap(localMapId);
  console.log(`\n[map] global=${map.id} local=${localMapId} name=${JSON.stringify(map.name)}`);
  console.log(`  ${map.width}x${map.height}px, grid ${map.cols}x${map.rows} of ${map.cellWidth}x${map.cellHeight}`
    + `, background=${map.backgroundType === BACKGROUND_PRECISE ? 'precise' : 'blurred'}`);
  if (map.backgroundType !== BACKGROUND_PRECISE) {
    console.log(`  randomSeed=${map.randomSeed} baseLandform=${map.baseLandform}`);
  }
  console.log(`  exits=${map.exits.length} decor=${map.groundNPCs.length}/${map.roleNPCs.length}/${map.skyNPCs.length}`
    + ' (ground/role/sky)');

  // --- 1 tile ---
  console.log('\n[tile] single tile from this area');
  const oneTile = extractOneTile(pkg, map);
  writePNG(args.out, `tile_map${map.id}.png`, oneTile.img);
  console.log(`  ${oneTile.description}`);
  // 8x for eyeballing: a 16x16 tile is too small to judge on screen.
  writePNG(args.out, `tile_map${map.id}_8x.png`, upscale(oneTile.img, 8));

  // --- full map ---
  console.log('\n[map render] compositing the whole map');
  const canvas = createCanvas(map.width, map.height, [0, 0, 0, 255]);
  const stats = { tiles: 0, missing: 0, decor: 0, decorMissing: 0 };
  if (map.backgroundType === BACKGROUND_PRECISE) renderPreciseMap(pkg, map, canvas, stats);
  else renderBlurredMap(pkg, map, canvas, stats);
  console.log(`  background: ${stats.tiles} tile blits, ${stats.missing} skipped`);

  drawDecor(pkg, map, canvas, stats);
  console.log(`  decor: ${stats.decor} drawn, ${stats.decorMissing} skipped`);

  writePNG(args.out, `map${map.id}.png`, canvas);

  // --- 1 sprite ---
  console.log('\n[sprite]');
  const spriteFile = args.sprite ? path.resolve(args.sprite) : pickSprite(args.data);
  const set = spriteFile.endsWith('.ctn')
    ? dumpAnimateSet(spriteFile, args.out)
    : (dumpSprite(spriteFile, args.out), null);

  // --- the two together, at coordinates the live server gave us ---
  if (set) {
    console.log('\n[composite] character on the map at the G2 spawn position');
    const scene = { width: canvas.width, height: canvas.height, rgba: canvas.rgba.slice() };
    const { x, y } = args.at ?? (map.id === 1395 ? { x: 66, y: 176 } : { x: map.width >> 1, y: map.height >> 1 });
    const placed = placeCharacter(set, scene, x, y);
    console.log(`  drew animation ${placed.animateId} at x=${x} y=${y} (anchor at the feet)`);
    writePNG(args.out, `map${map.id}_with_character.png`, scene);
    // A zoomed crop makes it obvious the sprite sits on the ground, not floating.
    writePNG(args.out, `map${map.id}_with_character_crop.png`,
      upscale(crop(scene, x - 44, y - 88, 88, 100), 3));
  }

  console.log('\nOK: map tiles and a sprite both decode and render from the live data dir.');
}

/**
 * Blit a character's standing pose with its anchor at (x, y). Unit coordinates from the server
 * are ground positions, so the sprite hangs above them -- exactly what a renderer must do.
 */
function placeCharacter(set, scene, x, y) {
  const animateId = 0;
  for (const p of set.resolveAnimate(animateId, 0)) {
    if (!p.image) continue;
    const img = p.image.frameToRGBA(p.pipFrame, p.trans);
    blit(scene, img, x + p.x, y + p.y);
  }
  drawCrosshair(scene, x, y, [255, 0, 0, 255], 4);
  return { animateId };
}

function crop(img, x0, y0, w, h) {
  const out = createCanvas(w, h, [0, 0, 0, 255]);
  for (let y = 0; y < h; y++) {
    const sy = y0 + y;
    if (sy < 0 || sy >= img.height) continue;
    for (let x = 0; x < w; x++) {
      const sx = x0 + x;
      if (sx < 0 || sx >= img.width) continue;
      const s = (sy * img.width + sx) * 4;
      out.rgba.set(img.rgba.subarray(s, s + 4), (y * w + x) * 4);
    }
  }
  return out;
}

/** Upscale nearest-neighbour, so tiny tiles are actually inspectable. */
function upscale(img, factor) {
  const out = createCanvas(img.width * factor, img.height * factor);
  for (let y = 0; y < out.height; y++) {
    for (let x = 0; x < out.width; x++) {
      const s = (Math.floor(y / factor) * img.width + Math.floor(x / factor)) * 4;
      out.rgba.set(img.rgba.subarray(s, s + 4), (y * out.width + x) * 4);
    }
  }
  return out;
}

/**
 * Pull exactly one tile out of the map, whichever background type it uses -- the narrow
 * "1 map tile decodes" claim of the spike, independent of the full-map composite.
 */
function extractOneTile(pkg, map) {
  if (map.backgroundType === BACKGROUND_PRECISE) {
    const { frames, info } = pkg.getTileInfo();
    const cell = map.mapData[0][0];
    const frame = frames[cell];
    const flags = tileFlags(info[cell]);
    return {
      img: pkg.loadTileImage().frameToRGBA(frame, flags.trans),
      description: `precise tile: cell(0,0)=${cell} -> tile.pip frame ${frame}, trans=${flags.trans}, `
        + `passable=${flags.passable}`,
    };
  }
  const landforms = pkg.loadAllLandformImages();
  const buffer = createBlurMapBuffer(map, landforms);
  for (let row = 0; row < map.rows; row++) {
    for (let col = 0; col < map.cols; col++) {
      const layers = decodeBlurCell(buffer[row][col]);
      for (const l of layers) {
        const lf = landforms[l.landform];
        if (!lf || l.frame < 0 || l.frame >= lf.frameCount) continue;
        return {
          img: lf.image.frameToRGBA(l.frame, l.trans),
          description: `blurred tile: cell(${col},${row}) landform=${l.landform} `
            + `frame=${l.frame} trans=${l.trans} (regenerated from seed ${map.randomSeed})`,
        };
      }
    }
  }
  throw new Error('no drawable tile found in this map');
}

/**
 * Default sprite: the player character out of the Flash client's asset set -- the closest
 * existing build to a browser client, so its resolution and packing are the right reference.
 * Falls back to any .pip under client_res.
 */
function pickSprite(dataDir) {
  const male = path.join(dataDir, 'client_pkg/Flash/male.ctn');
  if (fs.existsSync(male)) return male;
  const clientRes = path.join(dataDir, 'client_res');
  const files = fs.readdirSync(clientRes).filter((f) => f.endsWith('.pip'));
  if (!files.length) throw new Error(`no sprite source found under ${dataDir}`);
  return path.join(clientRes, files[0]);
}

main();
