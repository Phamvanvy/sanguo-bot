/*
 * VMObserver unit tests (G3d-g): the bounded pre-boot FIFO, the live stats,
 * and the keyboard bridge. The observer is constructed with injected
 * loadScript/fetchBytes so no DOM and no game data are needed; the real-ETF
 * boot path is covered by web/client/src/vm/boot-smoke.test.mjs.
 */
import test from 'node:test';
import assert from 'node:assert/strict';
import { VMObserver, VM_KEY } from './vm-observer.js';
import { PROCESSPACKET, CYCLE } from '../vm/vm.js';

/** minimal session double: records listeners, lets tests emit events */
function fakeSession() {
  const handlers = new Map();
  return {
    closed: false,
    sent: [],
    on(ev, fn) {
      if (!handlers.has(ev)) handlers.set(ev, new Set());
      handlers.get(ev).add(fn);
      return () => handlers.get(ev).delete(fn);
    },
    emit(ev, payload) { for (const fn of [...(handlers.get(ev) ?? [])]) fn(payload); },
    sendWriter(w) { this.sent.push(w); },
  };
}

/** segment bytes with a distinctive payload byte at offset 2 */
function seg(payload) {
  return Uint8Array.of(0x00, 0x42 & 0xff, payload);
}

/** an observer whose ETF boot always fails fast (observer mode must survive) */
function failingObserver(session, extra = {}) {
  return new VMObserver({
    session,
    loadScript: async () => { throw new Error('no scripts in unit test'); },
    fetchBytes: async () => new Uint8Array(0),
    log: () => {},
    ...extra,
  });
}

const flush = () => new Promise((r) => setImmediate(r));

test('packets arriving before boot replay exactly once, in FIFO order', async () => {
  const session = fakeSession();
  const obs = failingObserver(session);

  // spy on the routing entry point (handleIncoming receives a UASegment)
  const seen = [];
  obs.manager.handleIncoming = (s) => { seen.push(s.buf[2]); return false; };

  // three packets land while the "boot" has not run yet
  session.emit('rawSegment', { opcode: 0x42, segment: seg(1) });
  session.emit('rawSegment', { opcode: 0x42, segment: seg(2) });
  session.emit('rawSegment', { opcode: 0x42, segment: seg(3) });
  assert.equal(obs.stats.rawReceived, 3);
  assert.equal(seen.length, 0, 'nothing dispatched while booting');

  await obs.start();
  await flush();
  assert.deepEqual(seen, [1, 2, 3], 'FIFO replay after boot');
  assert.equal(obs._pending.length, 0);

  // a second start() must NOT replay again
  await obs.start();
  await flush();
  assert.deepEqual(seen, [1, 2, 3]);
});

test('packets arriving DURING the drain queue behind it (no overtaking)', async () => {
  const session = fakeSession();
  const obs = failingObserver(session);
  const seen = [];
  obs.manager.handleIncoming = (s) => {
    seen.push(s.buf[2]);
    if (s.buf[2] === 1) {
      // simulate a packet arriving mid-replay (as a WS message would)
      session.emit('rawSegment', { opcode: 0x42, segment: seg(99) });
    }
    return false;
  };

  session.emit('rawSegment', { opcode: 0x42, segment: seg(1) });
  await obs.start();
  await flush();
  await flush();
  assert.deepEqual(seen, [1, 99], 'the late packet went last, not first');
});

test('the FIFO is bounded by packet count and overflow is loud, not silent', async () => {
  const session = fakeSession();
  const logs = [];
  const obs = failingObserver(session, { maxPendingPackets: 2, log: (m) => logs.push(m) });
  const seen = [];
  obs.manager.handleIncoming = (s) => { seen.push(s.buf[2]); return false; };

  for (const p of [1, 2, 3, 4]) {
    session.emit('rawSegment', { opcode: 0x42, segment: seg(p) });
  }
  assert.equal(obs.stats.overflowed, 2, 'packets 3 and 4 do not fit a 2-slot buffer');
  assert.ok(logs.some((m) => m.includes('overflow')), 'overflow is logged loudly');

  await obs.start();
  await flush();
  assert.deepEqual(seen, [1, 2], 'the oldest packets survived; the newest was dropped');
});

test('disconnect clears the buffer; nothing replays afterwards', async () => {
  const session = fakeSession();
  const obs = failingObserver(session);
  const seen = [];
  obs.manager.handleIncoming = (s) => { seen.push(s.buf[2]); return false; };

  session.emit('rawSegment', { opcode: 0x42, segment: seg(7) });
  session.emit('rawSegment', { opcode: 0x42, segment: seg(8) });
  session.emit('close', 'connection closed');
  assert.equal(obs._pending.length, 0, 'buffer cleared on disconnect');

  await obs.start();
  await flush();
  assert.deepEqual(seen, []);
});

test('stats route handled / world / unhandled correctly', async () => {
  const session = fakeSession();
  const obs = failingObserver(session);

  // a fake GAME vm that handles every other packet
  let toggle = false;
  const fakeVM = {
    vmKey: 1, vmType: 0, closed: false,
    gtvm: { execute(type) { if (type === PROCESSPACKET && (toggle = !toggle)) obs.manager.currentPacket.handled = true; } },
    processCommonCallback() {},
    vmContainers: { size: () => 0, values: () => [] },
  };
  obs.manager.vmGames.put(fakeVM.vmKey, fakeVM);

  await obs.start();
  await flush();

  session.emit('rawSegment', { opcode: 0x10, segment: Uint8Array.of(0x00, 0x10, 1) }); // handled
  session.emit('rawSegment', { opcode: 0x11, segment: Uint8Array.of(0x00, 0x11, 2) }); // not
  assert.equal(obs.stats.handledByVM, 1);
  assert.equal(obs.stats.unhandled, 1);
  // GameMain semantics: a packet NO script handled goes to world.processPacket
  // too — so "handledByWorld" counts both world-owned and fallback processing.
  assert.equal(obs.stats.handledByWorld, 1, 'the unhandled packet fell through to the world');

  obs.manager.worldPacketOpcodes.add(500);
  session.emit('rawSegment', { opcode: 500, segment: Uint8Array.of(0x01, 0xf4, 3) });
  assert.equal(obs.stats.handledByWorld, 2);
  assert.equal(obs.stats.unhandled, 1, 'world-owned packets are not counted as unhandled');
});

test('keyboard bridge: keyPressed/multiKeyCheck see the handset key state', async () => {
  const session = fakeSession();
  const obs = failingObserver(session);
  await obs.start();

  const P = obs.platform;
  assert.equal(P.noKeyPressed(), true);

  obs.keyDown(VM_KEY.UP);
  assert.equal(P.keyPressed(VM_KEY.UP, false), true);
  assert.equal(P.keyPressed(VM_KEY.UP, false), true, 'sticky without clear');
  assert.equal(P.noKeyPressed(), false);

  assert.equal(P.multiKeyCheck([VM_KEY.LEFT, VM_KEY.UP], false), 1, 'index within the scanned list');

  assert.equal(P.keyPressed(VM_KEY.UP, true), true, 'clear=true consumes');
  assert.equal(P.keyPressed(VM_KEY.UP, true), false);

  obs.keyDown(VM_KEY.FIRE);
  P.clearKeys();
  assert.equal(P.noKeyPressed(), true);
});

test('window bookkeeping counts live VMs and notices new windows', async () => {
  const session = fakeSession();
  const obs = failingObserver(session);

  const win = { name: 'main' };
  const fakeVM = {
    vmKey: 2, vmId: 'game_panel', vmType: 0, closed: false,
    gtvm: { execute() {} },
    vmCycle() { this.gtvm.execute(CYCLE); },
    processCommonCallback() {},
    vmContainers: { size: () => 1, values: () => [win] },
  };
  obs.manager.vmGames.put(fakeVM.vmKey, fakeVM);

  await obs.start();
  obs._lastOpcode = 235;                       // CONN_OPENUI_SERVER, say
  obs.cycle();

  assert.equal(obs.stats.liveVMs, 1);
  assert.equal(obs.stats.windowCount, 1);
  assert.deepEqual(obs.stats.windows, ['game_panel/main']);
  assert.deepEqual(obs.stats.lastWindowEvent, { added: ['game_panel/main'], opcodeBefore: 235 });

  // cycling again must not re-report the same window as new
  obs.cycle();
  assert.equal(obs.stats.lastWindowEvent.added.length, 1); // unchanged

  // JSON-friendly snapshot
  const snap = obs.statsSnapshot;
  assert.equal(typeof snap.rawReceived, 'number');
  assert.ok(snap.topOpcodes && typeof snap.topOpcodes === 'object');
});