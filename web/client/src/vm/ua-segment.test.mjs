/*
 * UASegment.fromBytes — the incoming-segment factory the live VM observer
 * (G3d-f) feeds every raw wire segment through. It must behave exactly like
 * UASocketConnection.readSegment's `new UASegment(buf)`:
 *   - type comes from the first two bytes;
 *   - the read cursor starts at 2, so a script reads the echoed serial (if
 *     any) as its first field;
 *   - reset() returns to that same cursor;
 *   - the buffer is a private copy — mutating the input afterwards must not
 *     change what the segment reads.
 */
import test from 'node:test';
import assert from 'node:assert/strict';
import { UASegment } from './ua-segment.js';

test('fromBytes reads type from the header and fields after it', () => {
  const seg = UASegment.fromBytes(Uint8Array.of(0x00, 0x66, 0x00, 0x00, 0x03, 0xe8, 0x2a));
  assert.equal(seg.type, 0x66);
  assert.equal(seg.pos, 2);
  assert.equal(seg.readInt(), 1000);          // 0x000003e8
  assert.equal(seg.readByte(), 0x2a);
});

test('fromBytes copies: later mutation of the input is invisible', () => {
  const bytes = Uint8Array.of(0x12, 0x34, 0x00, 0x00, 0x00, 0x07);
  const seg = UASegment.fromBytes(bytes);
  bytes[5] = 0xff;                            // clobber the source
  assert.equal(seg.readInt(), 7);
});

test('reset() rewinds to just after the type header', () => {
  const seg = UASegment.fromBytes(Uint8Array.of(0xab, 0xcd, 1, 2, 3, 4));
  seg.readInt();
  seg.reset();
  assert.equal(seg.pos, 2);
  assert.equal(seg.readInt(), 0x01020304);
});

test('a writer-built segment round-trips through its own bytes', () => {
  const out = new UASegment(0x1234, true);    // writes type + serial header
  out.writeString('xin chào');
  const back = UASegment.fromBytes(out.toBytes());
  assert.equal(back.type, 0x1234);
  // incoming view: serial is just the first field, then our string
  assert.equal(back.readInt(), out.serial);
  assert.equal(back.readString(), 'xin chào');
});