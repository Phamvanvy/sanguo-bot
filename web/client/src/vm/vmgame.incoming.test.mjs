/*
 * VMGameManager.handleIncoming — the GameMain.handleSegment routing (G3d-f):
 *
 *   CONN_SYNC_SERVER (102)  -> engine only, scripts never see it
 *   world-owned opcode      -> world fallback, scripts never see it
 *   anything else           -> scripts top-down; if none handles it, the
 *                              world fallback gets a turn
 *   while a script runs     -> manager.currentPacket is what GetNextPacket
 *                              (0x0089) returns; null again afterwards
 */
import test from 'node:test';
import assert from 'node:assert/strict';
import { VMGameManager, VM_TYPE_GAME, CONN_SYNC_SERVER } from './vmgame.js';
import { UASegment } from './ua-segment.js';
import { PROCESSPACKET } from './vm.js';

/** a manager with one fake GAME vm whose PROCESSPACKET marks handled */
function managerWithFakeVM({ handled = true } = {}) {
  const manager = new VMGameManager({ platform: {}, loadScript: async () => new Uint8Array(0) });
  const seenPackets = [];
  const fakeVM = {
    vmKey: 1,
    vmType: VM_TYPE_GAME,
    closed: false,
    gtvm: {
      execute(type) {
        if (type === PROCESSPACKET) {
          seenPackets.push(manager.currentPacket);   // what GetNextPacket sees
          if (handled) manager.currentPacket.handled = true;
        }
      },
    },
    processCommonCallback() {},
  };
  manager.vmGames.put(fakeVM.vmKey, fakeVM);
  return { manager, seenPackets };
}

test('CONN_SYNC_SERVER never reaches the scripts', () => {
  const { manager, seenPackets } = managerWithFakeVM();
  const seg = UASegment.fromBytes(Uint8Array.of(0x00, CONN_SYNC_SERVER, ...[1, 2, 3, 4]));
  assert.equal(manager.handleIncoming(seg), false);
  assert.equal(seenPackets.length, 0, 'the fake VM must not have executed');
});

test('a world-owned opcode goes to the world fallback, not the scripts', () => {
  const { manager, seenPackets } = managerWithFakeVM();
  let worldRan = false;
  manager.worldPacketOpcodes.add(500);
  const seg = UASegment.fromBytes(Uint8Array.of(0x01, 0xf4, 9));
  assert.equal(manager.handleIncoming(seg, () => { worldRan = true; }), false);
  assert.equal(worldRan, true);
  assert.equal(seenPackets.length, 0);
});

test('a script-handled packet: currentPacket visible during dispatch, cleared after', () => {
  const { manager, seenPackets } = managerWithFakeVM({ handled: true });
  const seg = UASegment.fromBytes(Uint8Array.of(0x00, 0x7b, 1, 2, 3, 4));
  assert.equal(manager.handleIncoming(seg), true);
  assert.equal(seenPackets.length, 1);
  assert.equal(seenPackets[0], seg, 'GetNextPacket returns THE packet being dispatched');
  assert.equal(manager.currentPacket, null, 'cleared once dispatch finishes');
});

test('an unhandled packet falls through to the world and reports false', () => {
  const { manager, seenPackets } = managerWithFakeVM({ handled: false });
  let worldRuns = 0;
  const seg = UASegment.fromBytes(Uint8Array.of(0x00, 0x7c, 5));
  assert.equal(manager.handleIncoming(seg, () => { worldRuns++; }), false);
  assert.equal(worldRuns, 1, 'world fallback ran exactly once');
  assert.equal(seenPackets.length, 1, 'scripts still got their chance first');
  assert.equal(manager.currentPacket, null);
});

test('currentPacket is null outside dispatch even with no scripts', () => {
  const manager = new VMGameManager({ platform: {}, loadScript: async () => new Uint8Array(0) });
  const seg = UASegment.fromBytes(Uint8Array.of(0x00, 0x10));
  assert.equal(manager.handleIncoming(seg), false);
  assert.equal(manager.currentPacket, null);
});