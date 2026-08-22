/*
 * G3d-e boot smoke — run the REAL shipped .etf scripts as far as they go:
 *
 *   node --test web/client/src/vm/boot-smoke.test.mjs
 *
 * Milestones asserted here:
 *   1. lib_builtin + game_init (+ every library they link) load and INIT runs;
 *   2. game_world gets registered (the scripts' own LoadGameVm path);
 *   3. at least one GWidget was created by script code (windows need a live
 *      server session — login packets — so they land with the bridge);
 *   4. one full manager.cycle() completes;
 *   5. one full manager.drawAll() completes;
 *   6. ZERO missing syscalls and ZERO VM errors on the way — anything missing
 *      fails the test WITH the syscall id / script name, never silently.
 *
 * Needs selfhost/runtime/data (the server's own script directory); skips otherwise.
 */
import assert from 'node:assert/strict';
import test from 'node:test';
import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { fileURLToPath } from 'node:url';

import { parseETF } from './etf.js';
import { VMGameManager, VM_TYPE_GAME } from './vmgame.js';
import { ResourceStore } from './resources.js';
import { Graphics } from './gfx.js';
import { codecs as assetCodecs } from '../assets/codecs.js';

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../../..');
const DATA = process.env.SANGUO_DATA ?? path.join(REPO, 'selfhost/runtime/data');
const MODEL = process.env.SANGUO_UIMODEL ?? 'Flash';
const SCRIPTS = path.join(DATA, 'scripts', MODEL);
const skipNoData = fs.existsSync(SCRIPTS) ? false : `no scripts at ${SCRIPTS}`;

/* ------------------------------------------------------------- fakes */

/** a Canvas2D context stub that tolerates every call and every property set */
function fakeCtx() {
  const target = {};
  return new Proxy(target, {
    get(t, prop) {
      if (prop in t) return t[prop];
      return () => ({ width: 10, height: 10 }); // measureText-shaped fallback
    },
    set(t, prop, v) {
      t[prop] = v;
      return true;
    },
  });
}

function makePlatform(log) {
  return {
    screenWidth: () => 240,
    screenHeight: () => 320,
    fontHeight: () => 16,
    lineHeight: () => 18,
    font: { stringWidth: (s) => String(s ?? '').length * 8 },
    fontObj: null,
    keyPressed: () => false,
    noKeyPressed: () => true,
    multiKeyCheck: () => -1,
    clearKeys: () => {},
    getTimeStamp: () => Date.now(),
    getTick: () => Date.now() & 0x7fffffff,
    getSystemTime: () => Math.floor(Date.now() / 1000),
    graphics: null, // set per draw pass
    sendRequest: () => -1,
    broadcast: () => {},
    getNextPacket: () => null,
    exitGame: () => log.exits.push('exitGame'),
    closeConnection: () => {},
    loadFile: () => null,
    saveFile: () => false,
    deleteFile: () => {},
    loadResourceFile: () => null,
    formatText: (text) => [String(text ?? '')],
    drawDefaultBackground: () => {},
    instructionLimit: 20_000_000,
    // FAIL LOUD bookkeeping
    unimplemented: (id, why) => {
      log.missingSyscalls.push({ id: `0x${id.toString(16)}`, why });
    },
    onError: (err, where) => {
      log.vmErrors.push({ where, message: String(err?.message ?? err) });
    },
  };
}

/** fetch bytes for an asset name from the server data tree, a few layouts deep */
function assetFetcher(dataRoot) {
  const candidates = (name) => [
    path.join(dataRoot, 'client_pkg', MODEL, name),
    path.join(dataRoot, 'client_pkg', name),
    path.join(dataRoot, 'client_res', MODEL, name),
    path.join(dataRoot, 'client_res', name),
    path.join(dataRoot, name),
  ];
  const cache = new Map();
  return async (name) => {
    if (cache.has(name)) return cache.get(name);
    for (const p of candidates(name)) {
      try {
        const buf = fs.readFileSync(p);
        const bytes = new Uint8Array(buf);
        cache.set(name, bytes);
        return bytes;
      } catch {
        /* try next layout */
      }
    }
    throw new Error(`asset not found in data tree: "${name}"`);
  };
}

/* ---------------------------------------------------------------- test */

/** synchronous twin of assetFetcher for the Node smoke run */
function syncAssetFetcher(dataRoot) {
  const candidates = (name) => [
    path.join(dataRoot, 'client_pkg', MODEL, name),
    path.join(dataRoot, 'client_pkg', name),
    path.join(dataRoot, 'client_res', MODEL, name),
    path.join(dataRoot, 'client_res', name),
    path.join(dataRoot, name),
  ];
  return (name) => {
    for (const p of candidates(name)) {
      try {
        return new Uint8Array(fs.readFileSync(p));
      } catch {
        /* try next layout */
      }
    }
    throw new Error(`asset not found in data tree: "${name}"`);
  };
}

const NL = String.fromCharCode(10);
const report = (head, problems) => `${head}${NL}${problems.join(NL)}`;

test('boot smoke: real ETFs reach first window + cycle + drawAll', async (t) => {
  if (skipNoData) {
    t.skip(skipNoData);
    return;
  }

  const log = { missingSyscalls: [], vmErrors: [], exits: [] };

  // ---- collect the transitive library closure of the boot script ----------
  const readScript = (name) => {
    const gz = path.join(SCRIPTS, `${name}_${MODEL}.etf.gz`);
    const plain = path.join(SCRIPTS, `${name}.etf.gz`);
    const file = fs.existsSync(gz) ? gz : plain;
    return zlib.gunzipSync(fs.readFileSync(file));
  };

  const names = new Set(['lib_builtin', 'game_init']);
  const queue = ['lib_builtin', 'game_init'];
  while (queue.length > 0) {
    const name = queue.shift();
    let libs = [];
    try {
      libs = parseETF(readScript(name)).libNames;
    } catch (err) {
      assert.fail(`cannot parse boot script ${name}: ${err.message}`);
    }
    for (const lib of libs) {
      if (!names.has(lib)) {
        names.add(lib);
        queue.push(lib);
      }
    }
  }

  // ---- resources ----------------------------------------------------------
  const store = new ResourceStore({
    fetchBytes: assetFetcher(DATA),
    // Node-only: resource syscalls are synchronous, so an asset a script asks
    // for without preload can still be served straight from the data tree
    fetchBytesSync: syncAssetFetcher(DATA),
    rasterise: ({ width, height }) => ({ width, height, fakeCanvas: true }),
    // the game's compressed blobs are Java GZIP streams — use the shared codecs
    inflate: assetCodecs.inflate,
    decodePNG: assetCodecs.decodePNG,
  });

  // ---- manager + platform -------------------------------------------------
  const platform = makePlatform(log);
  const manager = new VMGameManager({
    platform,
    resources: store,
    loadScript: async (name) => readScript(name),
  });

  // preload every script of the closure so addUI's sync lib path always hits
  for (const name of names) {
    manager.vmCache.set(name, readScript(name));
  }

  // ---- boot ---------------------------------------------------------------
  let bootError = null;
  try {
    manager.addUI('game_init', manager.vmCache.get('game_init'), VM_TYPE_GAME);
  } catch (err) {
    bootError = err;
  }
  // Drive boot frames; ui_update chains its own loads during CYCLE.
  for (let frame = 0; frame < 30; frame++) {
    manager.cycle();
    await new Promise((r) => setImmediate(r));
  }

  // In the Java client GameWorld.java:502 / GamePanel.java:149 load these when
  // their screens start — the engine, not a script. Stand in for both here.
  if (!manager.getVMGame('game_world')) {
    await manager.loadVMGame('game_world', VM_TYPE_GAME);
  }
  for (let frame = 0; frame < 20; frame++) {
    manager.cycle();
    await new Promise((r) => setImmediate(r));
  }
  if (!manager.getVMGame('game_panel')) {
    await manager.loadVMGame('game_panel', VM_TYPE_GAME);
  }
  for (let frame = 0; frame < 20; frame++) {
    manager.cycle();
    await new Promise((r) => setImmediate(r));
  }
  await new Promise((r) => setImmediate(r));

  const problems = () => [
    ...log.missingSyscalls.map((m) => `missing syscall ${m.id} (${m.why})`),
    ...log.vmErrors.map((e) => `vm error in ${e.where}: ${e.message}`),
  ];

  if (bootError) {
    assert.fail(report(`boot threw: ${bootError.stack}`, problems()));
  }

  // ---- milestone 1+2: the world registered itself -------------------------
  const world = manager.getVMGame('game_world');
  assert.ok(world, report('game_world not registered after boot.', problems()));

  // ---- milestone 3: script UI code built at least one widget ---------------
  assert.ok(manager.gWidgets.size > 0,
    report('no GWidget created by script code.', problems()));

  // ---- milestone 4: one full cycle ----------------------------------------
  let cycleError = null;
  try {
    manager.cycle();
  } catch (err) {
    cycleError = err;
  }
  assert.ok(!cycleError, report(`cycle() threw: ${cycleError?.stack}`, problems()));

  // ---- milestone 5: one full paint ----------------------------------------
  const g = new Graphics(fakeCtx(), 240, 320);
  platform.graphics = g;
  let drawError = null;
  try {
    manager.drawAll(g);
  } catch (err) {
    drawError = err;
  }
  assert.ok(!drawError, report(`drawAll() threw: ${drawError?.stack}`, problems()));

  // ---- milestone 6: nothing went missing or wrong -------------------------
  assert.deepEqual(problems(), [],
    'the boot path hit missing syscalls or VM errors (see list)');
});