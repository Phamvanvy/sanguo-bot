/* Probe: boot like the smoke test and trace one syscall interactively. */
import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { fileURLToPath } from 'node:url';

import { parseETF } from '../../web/client/src/vm/etf.js';
import { VMGameManager, VM_TYPE_GAME } from '../../web/client/src/vm/vmgame.js';
import { ResourceStore } from '../../web/client/src/vm/resources.js';

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

const names = new Set(['lib_builtin', 'game_init']);
const queue = ['lib_builtin', 'game_init'];
while (queue.length) {
  const n = queue.shift();
  for (const lib of parseETF(readScript(n)).libNames) {
    if (!names.has(lib)) { names.add(lib); queue.push(lib); }
  }
}
console.log('closure:', [...names].join(', '));

const store = new ResourceStore({
  fetchBytes: async () => new Uint8Array(0),
  rasterise: ({ width, height }) => ({ width, height }),
});

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
    exitGame: () => {},
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
      missing.push(hex);
      if (hex === '0x1237') console.log('MISSING 0x1237 stack:', new Error().stack);
    },
    onError: (err, where) => errors.push(`${where}: ${err.message}`),
  },
  loadScript: async (name) => readScript(name),
});
for (const n of names) manager.vmCache.set(n, readScript(n));

manager.addUI('game_init', manager.vmCache.get('game_init'), VM_TYPE_GAME);
await new Promise((r) => setImmediate(r));
await new Promise((r) => setImmediate(r));

// drive some frames: the scripts chain their own loads during CYCLE
for (let i = 0; i < 10; i++) {
  manager.cycle();
  await new Promise((r) => setImmediate(r));
}
console.log('registered:', manager.vmGames.values().map((v) => v.vmId).join(', ') || '(none)');
console.log('missing:', missing.join(' ') || '(none)');
console.log('errors:');
for (const e of errors) console.log('  ', e);

// who owns a VM that saw 0x1237? instrument by hand: call CreateGWidget path
const gw = manager.getVMGame('game_world') ?? manager.getVMGame('ui_update');
if (gw) {
  console.log('owner check:', typeof gw.getGWidget, 'vmId=', gw.vmId);
}