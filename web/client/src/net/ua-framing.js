/*
 * UA frame framing — port of the framing in
 *   client/src/com/pip/io/UASocketConnection.java  (write / readSegment).
 *
 * Wire frame:
 *     'U' | variant | length | segment
 *   variant 'A' -> length is int32 BE (4 bytes)   <- what the world server uses
 *   variant 'B' -> length is uint16 BE (2 bytes)
 *   variant 'C' -> length is uint8    (1 byte)
 *   `length` counts the WHOLE frame: 1 ('U') + 1 (variant) + lenFieldSize + segment.
 *   => segmentLen = length - 2 - lenFieldSize, and the segment begins at offset 2+lenFieldSize.
 *
 * A "segment" begins with the int16 opcode/type (see ua-codec.js UASegmentReader).
 *
 * TCP (and the WebSocket bridge) preserve no message boundaries: a chunk may hold a partial
 * frame or several frames. UAFrameAccumulator buffers bytes and only emits complete frames.
 */

const U = 0x55; // 'U'
const LEN_FIELD_SIZE = { 0x41: 4 /*A*/, 0x42: 2 /*B*/, 0x43: 1 /*C*/ };

/** Wrap raw segment bytes into a UA frame. Default variant 'A' (int32 length). */
export function frameSegment(segment, variant = 'A') {
  const v = variant.charCodeAt(0);
  const lenSize = LEN_FIELD_SIZE[v];
  if (!lenSize) throw new Error('unknown UA variant ' + variant);
  const total = 2 + lenSize + segment.length;
  const out = new Uint8Array(total);
  out[0] = U;
  out[1] = v;
  // big-endian length into the lenSize field
  let t = total;
  for (let i = 1 + lenSize; i >= 2; i--) { out[i] = t & 0xff; t = Math.floor(t / 256); }
  out.set(segment, 2 + lenSize);
  return out;
}

/** Convenience: take a UASegmentWriter, return the framed bytes ready for ws.send. */
export function frameWriter(writer, variant = 'A') {
  return frameSegment(writer.toBytes(), variant);
}

export class UAFrameAccumulator {
  constructor() { this._buf = new Uint8Array(0); }

  _append(chunk) {
    const u8 = chunk instanceof Uint8Array ? chunk : new Uint8Array(chunk);
    if (this._buf.length === 0) { this._buf = u8; return; }
    const merged = new Uint8Array(this._buf.length + u8.length);
    merged.set(this._buf, 0);
    merged.set(u8, this._buf.length);
    this._buf = merged;
  }

  /**
   * Feed a chunk of received bytes. Returns an array of { opcode, segment } for every
   * complete frame now available (possibly empty). Leftover partial bytes are retained.
   * Throws on a corrupt head (desync) — the caller should close the session.
   */
  push(chunk) {
    this._append(chunk);
    const frames = [];
    let off = 0;
    const buf = this._buf;
    while (buf.length - off >= 2) {
      if (buf[off] !== U) throw new Error(`UA desync: expected 'U' at ${off}, got 0x${buf[off].toString(16)}`);
      const lenSize = LEN_FIELD_SIZE[buf[off + 1]];
      if (!lenSize) throw new Error(`UA desync: bad variant 0x${buf[off + 1].toString(16)} at ${off + 1}`);
      if (buf.length - off < 2 + lenSize) break; // length field not fully arrived
      let total = 0;
      for (let i = 0; i < lenSize; i++) total = (total << 8) | buf[off + 2 + i];
      if (total < 2 + lenSize) throw new Error(`UA bad frame length ${total} at ${off}`);
      if (buf.length - off < total) break;       // whole frame not arrived yet
      const segStart = off + 2 + lenSize;
      const segment = buf.subarray(segStart, off + total);
      const opcode = ((segment[0] & 0xff) << 8) | (segment[1] & 0xff);
      frames.push({ opcode, segment: new Uint8Array(segment) });
      off += total;
    }
    this._buf = off > 0 ? this._buf.subarray(off) : this._buf;
    return frames;
  }
}
