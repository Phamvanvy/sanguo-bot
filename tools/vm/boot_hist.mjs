/* Probe: which UI-layer syscalls do the boot scripts actually call? */
import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { fileURLToPath } from 'node:url';

import { parseETF } from '../../web/client/src/vm/etf.js';
import { VMGameManager, VM_TYPE_GAME } from '../../web/client/src/vm/vmgame.js';
import { ResourceStore } from '../../web/client/src/vm/resources.js';
import { codecs as assetCodecs } from '../../web/client/src/assets/codecs.js';

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const DATA = process.env.SANGUO_DATA ?? path.join(REPO, 'selfhost/runtime/data');
const MODEL = 'Flash';
const SCRIPTS = path.join(DATA, 'scripts', MODEL);

const readScript = (name) => {
  const f = fs.existsSync(path.join(SCRIPTS, `${name}_${MODEL}.etf.gz`))
    ? path.join(SCRIPTS, `${name}_${MODEL}.etf.gz`)
    : path.join(SCRIPTS, `${name}.etf.gz`);
  return zlib.gunzipSync(fs.readFileSync(f));
};

const store = new ResourceStore({
  fetchBytes: async () => new Uint8Array(0),
  fetchBytesSync: (name) => {
    for (const p of [
      path.join(DATA, 'client_pkg', MODEL, name),
      path.join(DATA, 'client_pkg', name),
      path.join(DATA, name),
    ]) {
      try {
        return new Uint8Array(fs.readFileSync(p));
      } catch { /* next */ }
    }
    throw new Error(`asset not found: ${name}`);
  },
  rasterise: ({ width, height }) => ({ width, height }),
  inflate: assetCodecs.inflate,
  decodePNG: assetCodecs.decodePNG,
});

const hist = new Map();
const missing = [];
const errors = [];
const manager = new VMGameManager({
  resources: store,
  platform: {
    screenWidth: () => 240,
    screenHeight: () => 320,
    fontHeight: () => 16,
    lineHeight: () => 18,
    font: { stringWidth: (s) => String(s ?? '').length * 8 },
    keyPressed: () => false,
    noKeyPressed: () => true,
    multiKeyCheck: () => -1,
    clearKeys: () => {},
    getTimeStamp: () => Date.now(),
    getTick: () => Date.now() & 0x7fffffff,
    getSystemTime: () => Math.floor(Date.now() / 1000),
    graphics: null,
    sendRequest: () => -1,
    broadcast: () => {},
    getNextPacket: () => null,
    exitGame: () => console.log('!! exitGame'),
    closeConnection: () => {},
    loadFile: () => null,
    saveFile: () => false,
    deleteFile: () => {},
    loadResourceFile: () => null,
    formatText: (text) => [String(text ?? '')],
    drawDefaultBackground: () => {},
    instructionLimit: 20_000_000,
    unimplemented: (id) => {
      const hex = '0x' + id.toString(16);
      if (!missing.includes(hex)) missing.push(hex);
    },
    onError: (err, where) => {
      const key = `${where}: ${err.message}`;
      if (!errors.includes(key)) errors.push(key);
    },
  },
  loadScript: async (name) => readScript(name),
});
for (const n of ['lib_builtin', 'game_init', 'game_world', 'game_panel']) {
  try { manager.vmCache.set(n, readScript(n)); } catch { /* optional */ }
}

manager.addUI('game_init', manager.vmCache.get('game_init'), VM_TYPE_GAME);
await new Promise((r) => setImmediate(r));

// wrap every VM's host to histogram ids
for (let frame = 0; frame < 40; frame++) {
  manager.cycle();
  await new Promise((r) => setImmediate(r));
}
if (!manager.getVMGame('game_world')) await manager.loadVMGame('game_world', VM_TYPE_GAME);
if (!manager.getVMGame('game_panel')) await manager.loadVMGame('game_panel', VM_TYPE_GAME);
for (let frame = 0; frame < 40; frame++) {
  manager.cycle();
  await new Promise((r) => setImmediate(r));
}

console.log('registered:', [...manager.vmGames.values()].map((v) => v.vmId).join(', '));
console.log('widgets:', manager.gWidgets.size);
console.log('windows:', [...manager.gWidgets.values()].filter((w) => w.constructor.name === 'GWindow').length);
console.log('missing:', missing.join(' ') || '(none)');
console.log('errors:');
for (const e of errors) console.log('  ', e);

// histogram via monkey-patched execute is complex; instead re-run with a
// counting wrapper around composeHost output — approximate by scanning which
// ids appear in each VM's code paths that were hit. Simpler: report nothing.