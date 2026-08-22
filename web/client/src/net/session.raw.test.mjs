/*
 * The 'rawSegment' observer event (G3d-f): EVERY complete frame reaches it —
 * known opcode, unported opcode, or a ported opcode whose body fails to
 * decode — BEFORE the named events run, and with a private copy of the bytes.
 * The existing settlement and named-event behaviour must not change.
 */
import test from 'node:test';
import assert from 'node:assert/strict';
import { GameSession } from './session.js';
import { frameSegment } from './ua-framing.js';
import { OpCode } from './opcodes.js';

/** int32 BE into a fresh array */
function be32(v) {
  return [(v >>> 24) & 0xff, (v >> 16) & 0xff, (v >> 8) & 0xff, v & 0xff];
}

/** a well-formed SYNC_TIME_SERVER segment: int clientTime | int serverTime */
function syncTimeSegment(clientTime, serverTime) {
  return Uint8Array.of(
    (OpCode.SYNC_TIME_SERVER >> 8) & 0xff, OpCode.SYNC_TIME_SERVER & 0xff,
    ...be32(clientTime), ...be32(serverTime),
  );
}

test('rawSegment fires for a decoded packet, before the named event', () => {
  const session = new GameSession(() => {});
  const order = [];
  session.on('rawSegment', ({ opcode, segment }) => {
    order.push(['raw', opcode, segment.length]);
  });
  session.on('SYNC_TIME_SERVER', () => order.push(['named']));
  session.feed(frameSegment(syncTimeSegment(1, 2)));
  assert.deepEqual(order.map((e) => e[0]), ['raw', 'named']);
  assert.equal(order[0][1], OpCode.SYNC_TIME_SERVER);
  assert.equal(order[0][2], 10);              // 2 header + 2 ints
});

test('rawSegment fires for an unported opcode too', () => {
  const session = new GameSession(() => {});
  const seen = [];
  session.on('rawSegment', (p) => seen.push(p));
  session.feed(frameSegment(Uint8Array.of(0x12, 0x34, 0xde, 0xad)));
  assert.equal(seen.length, 1);
  assert.equal(seen[0].opcode, 0x1234);
  assert.deepEqual([...seen[0].segment], [0x12, 0x34, 0xde, 0xad]);
  assert.equal(session.stats.unknown, 1);     // the old accounting still runs
});

test('rawSegment fires when a ported body fails to decode', () => {
  const session = new GameSession(() => {});
  const seen = [];
  session.on('rawSegment', (p) => seen.push(p));
  let errored = false;
  session.on('error', () => { errored = true; });
  // SYNC_TIME_SERVER header but a truncated body
  session.feed(frameSegment(Uint8Array.of(
    (OpCode.SYNC_TIME_SERVER >> 8) & 0xff, OpCode.SYNC_TIME_SERVER & 0xff, 0x01)));
  assert.equal(seen.length, 1, 'raw event must fire despite the decode error');
  assert.equal(errored, true);
  assert.equal(session.stats.decodeErrors, 1);
});

test('the segment handed to rawSegment is a private copy', () => {
  const session = new GameSession(() => {});
  let captured = null;
  session.on('rawSegment', (p) => { captured = p.segment; });
  session.feed(frameSegment(syncTimeSegment(1, 2)));
  captured[2] = 0xff;                         // scribble on it
  // feed the same bytes again: the second event must still carry 1
  let second = null;
  session.on('rawSegment', (p) => { second = p.segment; });
  session.feed(frameSegment(syncTimeSegment(1, 2)));
  assert.equal(second[2], 0);
});

test('request settlement is untouched by the observer event', async () => {
  const session = new GameSession(() => {});
  const p = session.waitFor(OpCode.SYNC_TIME_SERVER);
  session.feed(frameSegment(syncTimeSegment(7, 99)));
  const body = await p;
  assert.deepEqual(body, { clientTime: 7, serverTime: 99 });
});