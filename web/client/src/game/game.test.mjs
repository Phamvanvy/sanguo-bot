/*
 * Tests for the parts of the renderer that are decisions rather than pixels: framing, paint
 * order, facing, walking, and animation selection.
 *
 * Nothing here touches a canvas — the DOM-bound code lives in graphics.js / map-scene.js /
 * renderer.js's draw calls, and is covered by the live browser run instead (see
 * tools/render_smoke.py). What is tested here is what would silently look "nearly right":
 * a camera that shows outside the map, decor that paints over a character standing in front
 * of it, a walk that overshoots, or an animation that plays the wrong direction.
 */
import { test } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { fileURLToPath } from 'node:url';

import { computeCamera, visibleWorldRect, worldToScreen, screenToWorld, inRect } from './camera.js';
import { advance, directionFromDelta, walkPath, DIR_DOWN, DIR_UP, DIR_LEFT, DIR_RIGHT, BASE_SPEED } from './movement.js';
import { animateIndex, loopedStep, STAND, WALK, TICK_MS } from './animation.js';
import { buildDrawList, CULL_MARGIN } from './renderer.js';
import { UnitView } from './unit-view.js';
import { AssetSource } from './asset-source.js';
import { PipAnimateSet } from '../assets/pip-animate-set.js';
import { PipImage } from '../assets/pip-image.js';
import { codecs } from '../assets/codecs.js';

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../../..');
const DATA = path.join(REPO, 'selfhost/runtime/data');
const skipNoData = { skip: fs.existsSync(DATA) ? false : `no game data at ${DATA}` };

/* ---------- camera ---------- */

test('camera: centres on the player and never shows past the map edge', () => {
  const view = { width: 800, height: 600 };
  const map = { width: 1504, height: 1600 };
  const middle = computeCamera({ x: 700, y: 800 }, view, map, 1);
  assert.deepEqual(middle, { x: 300, y: 500 });

  // At the corners the player goes off-centre rather than the view going off-map.
  assert.deepEqual(computeCamera({ x: 10, y: 10 }, view, map, 1), { x: 0, y: 0 });
  assert.deepEqual(computeCamera({ x: 9999, y: 9999 }, view, map, 1),
    { x: map.width - view.width, y: map.height - view.height });
});

test('camera: a map smaller than the viewport is centred, not clamped', () => {
  // The spawn map is 352x320 — smaller than any desktop window, so this is the common case.
  const cam = computeCamera({ x: 66, y: 176 }, { width: 800, height: 600 }, { width: 352, height: 320 }, 1);
  assert.deepEqual(cam, { x: (352 - 800) / 2, y: (320 - 600) / 2 });
});

test('camera: zoom shrinks the world rectangle the view covers', () => {
  const view = { width: 800, height: 600 };
  const map = { width: 4000, height: 4000 };
  const at1 = computeCamera({ x: 2000, y: 2000 }, view, map, 1);
  const at2 = computeCamera({ x: 2000, y: 2000 }, view, map, 2);
  assert.deepEqual(at1, { x: 1600, y: 1700 });
  assert.deepEqual(at2, { x: 1800, y: 1850 });
  const rect = visibleWorldRect(at2, view, 2, 0);
  assert.equal(rect.x1 - rect.x0, 400);
  assert.equal(rect.y1 - rect.y0, 300);
});

test('camera: screen and world coordinates round-trip', () => {
  const cam = { x: 120, y: 240 };
  const screen = worldToScreen(cam, 2, 200, 300);
  assert.deepEqual(screen, { x: 160, y: 120 });
  assert.deepEqual(screenToWorld(cam, 2, screen.x, screen.y), { x: 200, y: 300 });
});

/* ---------- paint order ---------- */

test('draw list: units and decor interleave by y so depth reads correctly', () => {
  const rect = { x0: 0, y0: 0, x1: 500, y1: 500 };
  const decor = [{ x: 100, y: 100, animateId: 1 }, { x: 100, y: 300, animateId: 2 }];
  const units = [{ x: 100, y: 200 }, { x: 100, y: 400 }];
  const order = buildDrawList(decor, units, rect).map((e) => `${e.kind}@${e.item.y}`);
  assert.deepEqual(order, ['decor@100', 'unit@200', 'decor@300', 'unit@400']);
});

test('draw list: a unit standing on an object is drawn in front of it', () => {
  const rect = { x0: 0, y0: 0, x1: 500, y1: 500 };
  const order = buildDrawList([{ x: 50, y: 200 }], [{ x: 50, y: 200 }], rect).map((e) => e.kind);
  assert.deepEqual(order, ['decor', 'unit']);
});

test('draw list: anything outside the padded viewport is culled', () => {
  const rect = { x0: 0, y0: 0, x1: 100, y1: 100 };
  const list = buildDrawList([{ x: -10, y: 50 }, { x: 50, y: 50 }], [{ x: 50, y: 999 }], rect);
  assert.equal(list.length, 1);
  assert.equal(list[0].item.x, 50);
  assert.ok(CULL_MARGIN > 0, 'sprites hang above their anchor, so culling needs slack');
  assert.ok(inRect(rect, 0, 100) && !inRect(rect, 101, 50));
});

/* ---------- facing and walking ---------- */

test('movement: facing follows the dominant axis, ties going horizontal', () => {
  // Port check against Tool.calulateDirWithWayPointMatrix: `Math.abs(dx) - Math.abs(dy) >= 0`.
  assert.equal(directionFromDelta(10, 3), DIR_RIGHT);
  assert.equal(directionFromDelta(-10, 3), DIR_LEFT);
  assert.equal(directionFromDelta(3, 10), DIR_DOWN);
  assert.equal(directionFromDelta(3, -10), DIR_UP);
  assert.equal(directionFromDelta(5, 5), DIR_RIGHT, 'a 45-degree move counts as horizontal');
  assert.equal(directionFromDelta(0, 0, DIR_UP), DIR_UP, 'standing still keeps the old facing');
});

test('movement: a step never overshoots its target', () => {
  assert.deepEqual(advance({ x: 0, y: 0 }, { x: 100, y: 0 }, 30), { x: 30, y: 0, arrived: false });
  assert.deepEqual(advance({ x: 0, y: 0 }, { x: 10, y: 0 }, 30), { x: 10, y: 0, arrived: true });
  assert.deepEqual(advance({ x: 5, y: 5 }, { x: 5, y: 5 }, 30), { x: 5, y: 5, arrived: true });
  const diagonal = advance({ x: 0, y: 0 }, { x: 100, y: 100 }, 10);
  assert.equal(Math.round(Math.hypot(diagonal.x, diagonal.y)), 10, 'diagonal steps are not faster');
});

test('movement: a walk is split into steps small enough for the server to accept', () => {
  const path = walkPath({ x: 0, y: 0 }, { x: 100, y: 0 }, 9);
  assert.equal(path.at(-1).x, 100, 'the path ends exactly on the target');
  assert.ok(path.length >= 11 && path.length <= 13, `expected ~12 steps, got ${path.length}`);
  for (const step of path) assert.equal(step.direct, DIR_RIGHT);
  // A target the rounding cannot move towards must terminate rather than spin.
  assert.deepEqual(walkPath({ x: 0, y: 0 }, { x: 0, y: 0 }, 9), []);
  assert.ok(walkPath({ x: 0, y: 0 }, { x: 1e6, y: 0 }, 1, 20).length === 20, 'maxSteps caps the path');
});

/* ---------- unit state ---------- */

test('unit: walking towards a target sets the walk animation, arriving sets stand', () => {
  const u = new UnitView({ id: 1, x: 0, y: 0 });
  assert.equal(u.animateId, animateIndex(STAND, DIR_DOWN));

  u.setTarget(100, 0, 0);
  assert.equal(u.animateId, animateIndex(WALK, DIR_RIGHT));
  assert.equal(u.moving, true);

  // One second at base speed covers BASE_SPEED pixels.
  assert.equal(u.update(1000, 1000), true);
  assert.equal(u.x, BASE_SPEED);
  assert.equal(u.moving, true);

  u.update(5000, 6000);
  assert.deepEqual([u.x, u.y], [100, 0]);
  assert.equal(u.moving, false);
  assert.equal(u.animateId, animateIndex(STAND, DIR_RIGHT), 'facing is kept when stopping');
  assert.equal(u.update(1000, 7000), false, 'an idle unit does not drift');
});

test('unit: the animation cycle restarts when the state changes', () => {
  const u = new UnitView({ id: 1, x: 0, y: 0 });
  u.setTarget(0, 100, 500);
  assert.equal(u.animateStart, 500);
  assert.equal(u.animateId, animateIndex(WALK, DIR_DOWN));
  u.setTarget(0, 200, 900);
  assert.equal(u.animateStart, 500, 'still walking the same way: the cycle keeps running');
  u.setTarget(-100, 0, 1200);
  assert.equal(u.animateId, animateIndex(WALK, DIR_LEFT));
  assert.equal(u.animateStart, 1200, 'turning restarts it');
});

test('unit: placeAt teleports (spawn, map change) instead of walking', () => {
  const u = new UnitView({ id: 1, x: 0, y: 0 });
  u.setTarget(500, 0, 0);
  u.placeAt(66, 176, 10);
  assert.deepEqual([u.x, u.y, u.target, u.moving], [66, 176, null, false]);
});

/* ---------- animation table ---------- */

test('animation: group + direction picks the animation id', () => {
  assert.equal(animateIndex(STAND, DIR_DOWN), 0);
  assert.equal(animateIndex(WALK, DIR_UP), 7);
  assert.equal(animateIndex(WALK, 3), 7);
});

test('animation: the real male.ctn matches the group layout this client assumes', skipNoData, () => {
  const set = loadMale();
  assert.equal(set.animateCount, 52, 'male.ctn is 13 groups of 4 directions');
  for (let dir = 0; dir < 4; dir++) {
    const stand = set.animateSteps(animateIndex(STAND, dir));
    const walk = set.animateSteps(animateIndex(WALK, dir));
    // What separates the two groups is the shape of the cycle: a walk is four steps of four
    // distinct frames (one per leg position), while an idle re-uses two or three frames —
    // for the up-facing direction it is only two, which is why step COUNT alone won't do.
    assert.equal(walk.length, 4, `walk ${dir} should be a 4-step cycle`);
    assert.equal(new Set(walk.map((s) => s.frame)).size, 4, `walk ${dir} should have 4 distinct frames`);
    assert.ok(new Set(stand.map((s) => s.frame)).size < 4, `stand ${dir} should re-use its frames`);
    // The two groups must not share art, or "walking" would be indistinguishable from standing.
    const shared = walk.filter((w) => stand.some((s) => s.frame === w.frame));
    assert.equal(shared.length, 0, `stand and walk ${dir} share frames`);
  }
});

test('animation: playback loops instead of stopping at the end of the cycle', skipNoData, () => {
  const set = loadMale();
  const walk = animateIndex(WALK, DIR_DOWN);
  const duration = set.animateDuration(walk);
  assert.ok(duration > 0);
  assert.equal(set.animateFrameAt(walk, duration), null, 'the raw set plays once');
  const first = loopedStep(set, walk, 0);
  assert.deepEqual(loopedStep(set, walk, duration * TICK_MS), first, 'one full cycle later, back to the start');
  assert.notDeepEqual(loopedStep(set, walk, 2 * TICK_MS), first, 'and it does advance in between');
  assert.ok(loopedStep(set, walk, 99999) !== null, 'no frame is ever missing');
});

test('animation: every direction of stand and walk composites to real art', skipNoData, () => {
  const set = loadMale();
  for (const group of [STAND, WALK]) {
    for (let dir = 0; dir < 4; dir++) {
      const id = animateIndex(group, dir);
      for (const step of set.animateSteps(id)) {
        const pieces = set.resolveAnimate(id, step);
        assert.ok(pieces.length > 0, `animation ${id} has an empty step`);
        for (const p of pieces) {
          assert.ok(p.image, `animation ${id} references a missing image`);
          const img = p.image.frameToRGBA(p.pipFrame, p.trans);
          assert.ok(img.width > 0 && img.height > 0);
        }
      }
    }
  }
});

/* ---------- asset source ---------- */

test('assets: a map id resolves to its area package through the index', skipNoData, async () => {
  const source = new AssetSource({ baseUrl: '/data', fetch: fakeFetch() });
  const { pkg, localMapId, areaDir } = await source.packageForMap(1395);
  assert.equal(areaDir, '87_1');
  assert.equal(pkg.areaID, 87);
  assert.equal(localMapId, 3, '1395 = (87 << 4) | 3');
  assert.equal(pkg.globalMapId(localMapId), 1395);

  const again = await source.packageForMap(1394);
  assert.equal(again.pkg, pkg, 'the package is fetched once and shared by its maps');
  await assert.rejects(source.packageForMap(999999), /no area package/);
});

test('assets: an animate set pulls in the images it names', skipNoData, async () => {
  const source = new AssetSource({ baseUrl: '/data', fetch: fakeFetch() });
  const set = await source.animateSet('client_pkg/Flash/male.ctn');
  assert.equal(set.imageNames.length, set.images.length);
  assert.ok(set.images.every((i) => i instanceof PipImage), 'every named image resolved');
  assert.equal(await source.animateSet('client_pkg/Flash/male.ctn'), set, 'sets are cached');
});

test('assets: a failed fetch is not cached as a failure forever', async () => {
  let calls = 0;
  const source = new AssetSource({
    baseUrl: '/data',
    fetch: async () => {
      calls++;
      if (calls === 1) return { ok: false, status: 500 };
      return { ok: true, arrayBuffer: async () => new Uint8Array([1, 2, 3]).buffer };
    },
  });
  await assert.rejects(source.bytes('x.pip'), /HTTP 500/);
  assert.deepEqual(await source.bytes('x.pip'), new Uint8Array([1, 2, 3]));
});

/* ---------- helpers ---------- */

/** Serve the data directory from disk, in the shape AssetSource expects of `fetch`. */
function fakeFetch() {
  return async (url) => {
    const rel = url.replace(/^\/data\//, '');
    if (rel === 'areas.json') {
      const { buildAreaIndex } = await import('../../../bridge/asset-index.js');
      const index = buildAreaIndex(DATA);
      return { ok: true, json: async () => ({ areas: index.areas, mapToArea: index.mapToArea }) };
    }
    const file = path.join(DATA, rel);
    if (!fs.existsSync(file)) return { ok: false, status: 404 };
    const b = fs.readFileSync(file);
    return { ok: true, arrayBuffer: async () => b.buffer.slice(b.byteOffset, b.byteOffset + b.length) };
  };
}

let male = null;
function loadMale() {
  if (male) return male;
  const dir = path.join(DATA, 'client_pkg/Flash');
  const def = new Uint8Array(fs.readFileSync(path.join(dir, 'male.ctn')));
  const nodeCodecs = {
    inflate: (b) => new Uint8Array(zlib.gunzipSync(Buffer.from(b.buffer, b.byteOffset, b.length))),
    decodePNG: codecs.decodePNG,
  };
  const images = new PipAnimateSet([], def).imageNames
    .map((n) => new PipImage(new Uint8Array(fs.readFileSync(path.join(dir, n))), nodeCodecs));
  male = new PipAnimateSet(images, def);
  return male;
}
