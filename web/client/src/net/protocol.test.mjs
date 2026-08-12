#!/usr/bin/env node
/*
 * G2 protocol registry + session tests. No server required.
 *
 * Server-sent fixtures are built with UASegmentWriter mirroring the exact Java writes
 * (each test cites the source method), so a decoder drifting from the Java layout fails
 * here rather than silently mis-reading a live packet.
 *
 * Run: node web/client/src/net/protocol.test.mjs
 */
import { UASegmentWriter } from './ua-codec.js';
import { frameSegment } from './ua-framing.js';
import { OpCode } from './opcodes.js';
import { encode, decodeSegment, frame, ChatChannel, UnitType, MoveFlag, normalizeOpcode } from './protocol.js';
import { GameSession } from './session.js';

// Tests are registered, then run in order at the end so async ones are actually awaited.
const registry = [];
function test(name, fn) { registry.push({ name, fn }); }
function section(name) { registry.push({ section: name }); }
function eq(actual, expected, what = '') {
  const a = JSON.stringify(actual), b = JSON.stringify(expected);
  if (a !== b) throw new Error(`${what}: got ${a}, want ${b}`);
}
function ok(cond, what) { if (!cond) throw new Error(what); }

section('protocol: client encoders');

test('accountLogin segment matches the byte layout the live server accepted', () => {
  const seg = encode.accountLogin(1, { name: 'vypv1', password: '123456' }).toBytes();
  // [op:2][serial:4][UTF name][UTF pass][UTF model][UTF version][UTF ''][int -1][UTF '']
  eq([seg[0], seg[1]], [0, OpCode.ACCOUNT_LOGIN_CLIENT], 'opcode');
  eq([...seg.subarray(2, 6)], [0, 0, 0, 1], 'serial');
  eq([...seg.subarray(6, 13)], [0, 5, 0x76, 0x79, 0x70, 0x76, 0x31], 'UTF "vypv1"');
});

test('actorList is serial-only', () => {
  const seg = encode.actorList(7).toBytes();
  eq(seg.length, 6, 'length');
  eq([...seg.subarray(2)], [0, 0, 0, 7], 'serial');
});

test('actorLogin: serial + actorId + UTF imei', () => {
  const seg = encode.actorLogin(3, { actorId: 4242 }).toBytes();
  eq([...seg.subarray(2, 6)], [0, 0, 0, 3], 'serial');
  eq([...seg.subarray(6, 10)], [0, 0, 0x10, 0x92], 'actorId 4242');
  eq([...seg.subarray(10)], [0, 0], 'empty UTF imei');
});

test('actorCreate: name then three bytes (sex, clazz, faction)', () => {
  const seg = encode.actorCreate(2, { name: 'ab', sex: 1, clazz: 2, faction: 3 }).toBytes();
  eq([...seg.subarray(6)], [0, 2, 0x61, 0x62, 1, 2, 3], 'body');
});

test('move: int time, short x, short y, byte direct, short state — and NO serial', () => {
  const seg = encode.move({ time: 1000, x: 300, y: -2, direct: 2, state: 1 }).toBytes();
  eq(seg.length, 2 + 4 + 2 + 2 + 1 + 2, 'length');
  eq([...seg.subarray(2, 6)], [0, 0, 3, 232], 'time 1000');
  eq([...seg.subarray(6, 8)], [1, 44], 'x 300');
  eq([...seg.subarray(8, 10)], [255, 254], 'y -2 (two\'s complement short)');
  eq([...seg.subarray(10)], [2, 0, 1], 'direct + state');
});

test('loadingFinished is a bare opcode', () => {
  eq([...encode.loadingFinished().toBytes()], [0, OpCode.LOADING_FINISHED_CLIENT], 'segment');
});

test('chat attachment uses an int32 length prefix (Packet.put(byte[]))', () => {
  const seg = encode.chat({ channel: ChatChannel.AREA, destId: 0, message: 'hi' }).toBytes();
  eq([...seg.subarray(seg.length - 4)], [0, 0, 0, 0], 'empty attachment length');
});

test('a framed segment carries the whole-frame length', () => {
  const bytes = frame(encode.actorList(1));
  eq([bytes[0], bytes[1]], [0x55, 0x41], "'U','A'");
  eq((bytes[2] << 24) | (bytes[3] << 16) | (bytes[4] << 8) | bytes[5], bytes.length, 'length counts whole frame');
});

section('protocol: server decoders (fixtures mirror the Java writes)');

test('ACTOR_LIST_SERVER — LoadActorListCall.callFinish', () => {
  const w = new UASegmentWriter(OpCode.ACTOR_LIST_SERVER);
  w.writeInt(11);          // serial
  w.writeByte(2);          // pt.put(actorList.size()) -> ONE byte
  for (const a of [{ id: 5, name: 'Trương Phi' }, { id: 6, name: 'B' }]) {
    w.writeInt(a.id).writeString(a.name);
    w.writeByte(1).writeByte(30).writeByte(2).writeByte(3);       // sex level clazz faction
    w.writeInt(100).writeInt(200).writeInt(300).writeByte(4);     // 3 scores + flashLevel
    w.writeString('Lạc Dương');
  }
  const m = decodeSegment(frameBody(w));
  ok(m.known && !m.error, 'decoded');
  eq(m.body.serial, 11, 'serial');
  eq(m.body.actors.length, 2, 'count');
  eq(m.body.actors[0].name, 'Trương Phi', 'diacritics survive');
  eq(m.body.actors[0].level, 30, 'level');
  eq(m.body.actors[1].mapName, 'Lạc Dương', 'map name');
});

test('ACTOR_LOGIN_SERVER — Player.toClientBytes head, stops before the equipment blob', () => {
  // Player.toClientBytes() — a raw DataOutputStream blob...
  const b = UASegmentWriter.raw();
  b.writeInt(4242).writeString('Vy');              // id, name
  b.writeByte(1).writeByte(42).writeByte(2).writeByte(3);   // sex level clazz faction
  for (const v of [500, 300, 480, 290, 10, 11, 12, 13]) b.writeShort(v);  // maxhp..intellect
  for (let i = 0; i < 14; i++) b.writeShort(i);    // combat shorts we skip
  b.writeShort(1).writeShort(2).writeShort(3).writeShort(4);  // restores, skill/property points
  b.writeInt(12345).writeInt(20000).writeInt(777); // exp, nextLevelExp, money
  b.writeShort(101);                               // mapId
  b.writeInt(-1);                                  // map instance
  b.writeShort(640).writeShort(480).writeShort(2).writeShort(0);  // x y direct state
  b.writeInt(50).writeString('Dân').writeString('');              // credit, creditName, guild
  b.writeInt(0x01020304);                          // start of the tail we do not decode (4B)

  const w = new UASegmentWriter(OpCode.ACTOR_LOGIN_SERVER);
  w.writeInt(9);                                   // serial
  w.writeBytes(b.toBytes());                       // ...embedded with its int32 length prefix
  const m = decodeSegment(frameBody(w));
  ok(m.known && !m.error, 'decoded');
  eq(m.body.id, 4242, 'actorId');
  eq(m.body.name, 'Vy', 'name');
  eq(m.body.level, 42, 'level');
  eq([m.body.mapId, m.body.x, m.body.y], [101, 640, 480], 'map + coords');
  eq(m.body.creditName, 'Dân', 'creditName');
  ok(m.body.partial === true && m.body.restBytes === 4, `partial tail measured from blobLen (restBytes=${m.body.restBytes})`);
});

test('GOMAP_ALLOW_SERVER — four ints then a byte', () => {
  const w = new UASegmentWriter(OpCode.GOMAP_ALLOW_SERVER);
  w.writeInt(101).writeInt(-1).writeInt(640).writeInt(480).writeByte(1);
  const m = decodeSegment(frameBody(w));
  eq(m.body, { mapId: 101, mapInstanceId: -1, x: 640, y: 480, allowFollow: true }, 'body');
});

test('ERROR (-1) decodes even though it is 0xFFFF on the wire', () => {
  const w = new UASegmentWriter(OpCode.ERROR);
  w.writeInt(4).writeShort(OpCode.ACTOR_LOGIN_CLIENT).writeString('Không tìm thấy nhân vật');
  const seg = frameBody(w);
  eq(normalizeOpcode((seg[0] << 8) | seg[1]), -1, 'wire opcode normalises to -1');
  const m = decodeSegment(seg);
  eq(m.body.serial, 4, 'serial');
  eq(m.body.message, 'Không tìm thấy nhân vật', 'message');
});

test('UNIT_MOVE_SERVER — POINT block, plain map', () => {
  const w = new UASegmentWriter(OpCode.UNIT_MOVE_SERVER);
  w.writeByte(UnitType.PLAYER | MoveFlag.POINT).writeInt(99);
  w.writeShort(101).writeShort(640).writeShort(480);
  const m = decodeSegment(frameBody(w));
  eq(m.body.unitType, UnitType.PLAYER, 'unit type');
  eq([m.body.instanceId, m.body.mapId, m.body.x, m.body.y], [99, 101, 640, 480], 'instance + point');
  eq(m.body.mapInstanceId, -1, 'not an instanced map');
});

test('UNIT_MOVE_SERVER — instanced map sets bit15 and adds the instance id', () => {
  const w = new UASegmentWriter(OpCode.UNIT_MOVE_SERVER);
  w.writeByte(UnitType.CREATURE | MoveFlag.POINT).writeInt(7);
  w.writeShort(101 | (1 << 15)).writeInt(555).writeShort(10).writeShort(20);
  const m = decodeSegment(frameBody(w));
  eq([m.body.mapId, m.body.mapInstanceId, m.body.x, m.body.y], [101, 555, 10, 20], 'instanced point');
});

test('CHAT_SERVER — WORLD channel carries a faction byte, PRIVATE a dest name', () => {
  const world = new UASegmentWriter(OpCode.CHAT_SERVER);
  world.writeByte(ChatChannel.WORLD | 0x80).writeInt(5).writeByte(2)
    .writeString('Vy').writeString('xin chào').writeBytes(new Uint8Array(0));
  const a = decodeSegment(frameBody(world)).body;
  eq([a.channel, a.isKing, a.faction, a.sourceName, a.message], [0, true, 2, 'Vy', 'xin chào'], 'world chat');

  const priv = new UASegmentWriter(OpCode.CHAT_SERVER);
  priv.writeByte(ChatChannel.PRIVATE).writeInt(5).writeString('Bob')
    .writeString('Vy').writeString('hi').writeBytes(new Uint8Array(0));
  const b = decodeSegment(frameBody(priv)).body;
  eq([b.channel, b.destName, b.sourceName], [ChatChannel.PRIVATE, 'Bob', 'Vy'], 'private chat');
});

test('an unported opcode decodes as known:false, not an exception', () => {
  const w = new UASegmentWriter(OpCode.SKILL_LIST_SERVER).writeInt(1);
  const m = decodeSegment(frameBody(w));
  eq([m.known, m.error], [false, null], 'unknown but not an error');
  ok(m.name.includes('SKILL_LIST_SERVER'), `labelled: ${m.name}`);
});

test('a truncated ported packet reports an error instead of throwing', () => {
  const w = new UASegmentWriter(OpCode.GOMAP_ALLOW_SERVER).writeInt(101);  // 4 of 17 body bytes
  const m = decodeSegment(frameBody(w));
  ok(m.known, 'still recognised');
  ok(m.error instanceof RangeError, `read past end reported, not silently garbage: ${m.error}`);
  eq(m.body, null, 'no half-decoded body handed to the app');
});

section('session: dispatch, requests, resilience');

function frameBody(writer) { return writer.toBytes(); }

/** A session whose sink records what was sent; feed it fixtures to simulate the server. */
function newSession() {
  const sent = [];
  const s = new GameSession((bytes) => sent.push(bytes));
  s.sent = sent;
  return s;
}

test('request resolves on the response with the matching serial', async () => {
  const s = newSession();
  const p = s.request('actorList', [OpCode.ACTOR_LIST_SERVER]);
  const w = new UASegmentWriter(OpCode.ACTOR_LIST_SERVER).writeInt(1).writeByte(0);
  s.feed(frameSegment(w.toBytes(), 'A'));
  return p.then((body) => eq(body.count, 0, 'empty actor list'));
});

test('a response for a DIFFERENT serial does not resolve the request', () => {
  const s = newSession();
  let settled = false;
  s.request('actorList', [OpCode.ACTOR_LIST_SERVER]).then(() => { settled = true; }, () => { settled = true; });
  const w = new UASegmentWriter(OpCode.ACTOR_LIST_SERVER).writeInt(999).writeByte(0);
  s.feed(frameSegment(w.toBytes(), 'A'));
  ok(!settled, 'still pending');
  ok(s.stats.decoded === 1, 'packet was still decoded and delivered as an event');
});

test('ERROR with the request serial rejects that request', async () => {
  const s = newSession();
  const p = s.request('actorLogin', [OpCode.ACTOR_LOGIN_SERVER], { actorId: 1 });
  const w = new UASegmentWriter(OpCode.ERROR)
    .writeInt(1).writeShort(OpCode.ACTOR_LOGIN_CLIENT).writeString('Không tìm thấy nhân vật');
  s.feed(frameSegment(w.toBytes(), 'A'));
  return p.then(() => { throw new Error('should have rejected'); },
    (e) => ok(/Không tìm thấy/.test(e.message), `rejected with server text: ${e.message}`));
});

test('unknown packets are counted and skipped, and do not stop later known packets', () => {
  const s = newSession();
  const unknown = new UASegmentWriter(OpCode.SKILL_LIST_SERVER).writeInt(1).writeInt(2);
  const known = new UASegmentWriter(OpCode.SYNC_TIME_SERVER).writeInt(10).writeInt(20);
  const a = frameSegment(unknown.toBytes(), 'A'), b = frameSegment(known.toBytes(), 'A');
  const merged = new Uint8Array(a.length + b.length);
  merged.set(a, 0); merged.set(b, a.length);
  let synced = null;
  s.on('SYNC_TIME_SERVER', (m) => { synced = m; });
  s.feed(merged);
  eq(s.stats.unknown, 1, 'one unknown');
  eq(s.stats.decoded, 1, 'one decoded');
  eq(synced, { clientTime: 10, serverTime: 20 }, 'known packet after an unknown one still arrives');
});

test('a split frame is reassembled across feeds', () => {
  const s = newSession();
  const bytes = frameSegment(new UASegmentWriter(OpCode.SYNC_TIME_SERVER).writeInt(1).writeInt(2).toBytes(), 'A');
  let got = 0;
  s.on('SYNC_TIME_SERVER', () => got++);
  s.feed(bytes.subarray(0, 5));
  eq(got, 0, 'nothing emitted from a partial frame');
  s.feed(bytes.subarray(5));
  eq(got, 1, 'emitted once complete');
});

test('a framing desync is fatal and fails pending requests (does not hang)', async () => {
  const s = newSession();
  let fatal = null;
  s.on('fatal', (e) => { fatal = e; });
  const p = s.request('actorList', [OpCode.ACTOR_LIST_SERVER]);
  s.feed(Uint8Array.of(0x00, 0x41, 0, 0, 0, 8, 0, 1));   // does not start with 'U'
  ok(fatal, 'fatal emitted');
  return p.then(() => { throw new Error('should have rejected'); }, () => {});
});

test('session tracks actor + position from ACTOR_LOGIN/GOMAP_ALLOW', () => {
  const s = newSession();
  const w = new UASegmentWriter(OpCode.GOMAP_ALLOW_SERVER)
    .writeInt(101).writeInt(-1).writeInt(640).writeInt(480).writeByte(0);
  s.feed(frameSegment(w.toBytes(), 'A'));
  eq(s.position, { mapId: 101, x: 640, y: 480, mapInstanceId: -1 }, 'position');
});

let pass = 0, fail = 0;
for (const entry of registry) {
  if (entry.section) { console.log(entry.section); continue; }
  try { await entry.fn(); console.log(`  ok  ${entry.name}`); pass++; }
  catch (e) { console.error(`  FAIL ${entry.name}\n       ${e.message}`); fail++; }
}
console.log(`\n${pass} passed, ${fail} failed`);
process.exit(fail ? 1 : 0);
