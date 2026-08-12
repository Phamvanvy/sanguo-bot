/*
 * UA segment codec — a faithful port of the J2ME client's
 *   client/src/com/pip/io/UASegment.java  (+ Tool.readUTF / DataOutputStream.writeUTF).
 *
 * A "segment" is the message body that lives inside a UA frame:
 *     segment = [ type:int16 BE ] [ fields... ]
 * The reader starts AFTER the 2-byte type (Java: pos = 2), so the first field a handler
 * reads is usually the echoed `serial` int. Framing ('UA' + int32 len + segment) is in
 * ua-framing.js — this file is only about reading/writing field values, matching Java
 * DataInput/DataOutputStream semantics byte-for-byte.
 *
 * Numbers are big-endian. `long` (64-bit) is represented as BigInt to avoid the 2^53
 * precision cliff. Strings use Java "modified UTF-8" (writeUTF/readUTF), NOT standard UTF-8.
 */

// ---------- Java modified UTF-8 ----------

/**
 * Encode a JS string the way java.io.DataOutputStream.writeUTF does:
 *   - u16 byte-length prefix
 *   - U+0000 -> 0xC0 0x80 (two bytes, never a NUL byte)
 *   - U+0001..U+007F -> 1 byte
 *   - U+0080..U+07FF -> 2 bytes
 *   - U+0800..U+FFFF -> 3 bytes
 *   - supplementary (> U+FFFF) -> its UTF-16 surrogate pair, each surrogate as 3 bytes
 * Throws if the encoded length exceeds 65535 (Java's UTFDataFormatException).
 */
export function encodeModifiedUTF(str) {
  const s = str == null ? '' : String(str);
  const out = [];
  // iterate by UTF-16 code unit (NOT code point) so surrogates are emitted as 3 bytes each,
  // exactly like Java, which operates on char values.
  for (let i = 0; i < s.length; i++) {
    const c = s.charCodeAt(i);
    if (c >= 0x0001 && c <= 0x007f) {
      out.push(c);
    } else if (c === 0x0000 || (c >= 0x0080 && c <= 0x07ff)) {
      out.push(0xc0 | (c >> 6), 0x80 | (c & 0x3f));
    } else {
      out.push(0xe0 | (c >> 12), 0x80 | ((c >> 6) & 0x3f), 0x80 | (c & 0x3f));
    }
  }
  if (out.length > 0xffff) throw new RangeError(`writeUTF: encoded length ${out.length} > 65535`);
  const buf = new Uint8Array(2 + out.length);
  buf[0] = (out.length >>> 8) & 0xff;
  buf[1] = out.length & 0xff;
  buf.set(out, 2);
  return buf;
}

/**
 * Decode Java modified UTF-8 starting at `pos` (which points at the u16 length prefix).
 * Returns { value, next } where `next` is the byte offset just past the string.
 * Mirrors DataInputStream.readUTF.
 */
export function decodeModifiedUTF(bytes, pos) {
  const utflen = (bytes[pos] << 8) | bytes[pos + 1];
  pos += 2;
  const end = pos + utflen;
  let out = '';
  while (pos < end) {
    const b1 = bytes[pos] & 0xff;
    const top = b1 >> 4;
    if (top < 0x8) {            // 0xxxxxxx
      pos += 1;
      out += String.fromCharCode(b1);
    } else if (top === 0xc || top === 0xd) {  // 110xxxxx 10xxxxxx
      const b2 = bytes[pos + 1] & 0xff;
      pos += 2;
      out += String.fromCharCode(((b1 & 0x1f) << 6) | (b2 & 0x3f));
    } else if (top === 0xe) {  // 1110xxxx 10xxxxxx 10xxxxxx
      const b2 = bytes[pos + 1] & 0xff;
      const b3 = bytes[pos + 2] & 0xff;
      pos += 3;
      out += String.fromCharCode(((b1 & 0x0f) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f));
    } else {
      throw new Error(`readUTF: malformed input byte 0x${b1.toString(16)} at ${pos}`);
    }
  }
  return { value: out, next: end };
}

// ---------- reader ----------

export class UASegmentReader {
  /** @param {Uint8Array} segment  bytes starting at the 2-byte type field */
  constructor(segment) {
    this.data = segment;
    this.type = ((segment[0] & 0xff) << 8) | (segment[1] & 0xff);  // int16 opcode
    this.opcode = this.type;
    this.pos = 2;  // matches Java UASegment: skip the type
  }
  reset() { this.pos = 2; }
  remaining() { return this.data.length - this.pos; }

  /**
   * Java throws EOFException past the end; a JS typed array quietly yields `undefined`,
   * which would turn a wrong layout into plausible-looking garbage instead of a failure.
   * Every read goes through this so a short packet is a loud, catchable error.
   */
  _need(n) {
    if (this.pos + n > this.data.length) {
      throw new RangeError(`read past end of segment: need ${n} byte(s) at ${this.pos}, have ${this.remaining()}`);
    }
  }

  _num(len) {
    this._need(len);
    let v = 0;
    for (let i = 0; i < len; i++) v = (v << 8) | (this.data[this.pos + i] & 0xff);
    this.pos += len;
    return v;
  }
  readBoolean() { this._need(1); return this.data[this.pos++] === 1; }
  readByte() { this._need(1); const b = this.data[this.pos++]; return b > 0x7f ? b - 0x100 : b; }
  readUnsignedByte() { this._need(1); return this.data[this.pos++] & 0xff; }
  readShort() { const v = this._num(2); return v > 0x7fff ? v - 0x10000 : v; }
  readUnsignedShort() { return this._num(2); }
  readInt() { return this._num(4) | 0; }   // | 0 forces signed 32-bit
  readLong() {
    this._need(8);
    let v = 0n;
    for (let i = 0; i < 8; i++) v = (v << 8n) | BigInt(this.data[this.pos + i] & 0xff);
    this.pos += 8;
    return BigInt.asIntN(64, v);
  }
  /** Mirrors UASegment.readString: modified-UTF8 branch OR high-bit UTF-16 branch. */
  readString() {
    this._need(2);
    if ((this.data[this.pos] & 0x80) === 0) {
      this._need(2 + (((this.data[this.pos] & 0xff) << 8) | (this.data[this.pos + 1] & 0xff)));
      const { value, next } = decodeModifiedUTF(this.data, this.pos);
      this.pos = next;
      return value;
    }
    // high bit set: raw UTF-16, length masked with 0x7FFF, chars are 2-byte BE
    const len = this._num(2) & 0x7fff;
    this._need(len);
    let out = '';
    for (let i = 0; i < (len >> 1); i++) {
      out += String.fromCharCode(((this.data[this.pos] & 0xff) << 8) | (this.data[this.pos + 1] & 0xff));
      this.pos += 2;
    }
    return out;
  }
  readBooleans() { const n = this._num(2), r = new Array(n); for (let i = 0; i < n; i++) r[i] = this.readBoolean(); return r; }
  readBytes() { const n = this._num(4); this._need(n); const r = this.data.subarray(this.pos, this.pos + n); this.pos += n; return new Uint8Array(r); }
  readShorts() { const n = this._num(2), r = new Array(n); for (let i = 0; i < n; i++) r[i] = this.readShort(); return r; }
  readInts() { const n = this._num(2), r = new Array(n); for (let i = 0; i < n; i++) r[i] = this.readInt(); return r; }
  readLongs() { const n = this._num(2), r = new Array(n); for (let i = 0; i < n; i++) r[i] = this.readLong(); return r; }
  readStrings() { const n = this._num(2), r = new Array(n); for (let i = 0; i < n; i++) r[i] = this.readString(); return r; }
}

// ---------- writer ----------

export class UASegmentWriter {
  /**
   * @param {number} type      opcode (int16)
   * @param {boolean} needSerial  if true, writes an int serial right after the type
   * @param {number} serial    serial value to write when needSerial (client picks it)
   */
  constructor(type, needSerial = false, serial = 0) {
    this._chunks = [];
    this._len = 0;
    this.type = type & 0xffff;
    this.serial = needSerial ? (serial | 0) : -1;
    this.writeShort(this.type);
    if (needSerial) this.writeInt(this.serial);
  }
  /**
   * A writer with NO leading type field, for building a nested blob that is embedded with
   * writeBytes (Java: a DataOutputStream handed to Packet.put(byte[])).
   */
  static raw() {
    const w = Object.create(UASegmentWriter.prototype);
    w._chunks = []; w._len = 0; w.type = -1; w.serial = -1;
    return w;
  }

  _push(u8) { this._chunks.push(u8); this._len += u8.length; }
  _int(value, len) {
    const b = new Uint8Array(len);
    let v = value;
    for (let i = len - 1; i >= 0; i--) { b[i] = v & 0xff; v = Math.floor(v / 256); }
    this._push(b);
    return this;
  }
  writeBoolean(x) { this._push(Uint8Array.of(x ? 1 : 0)); return this; }
  writeByte(x) { this._push(Uint8Array.of(x & 0xff)); return this; }
  writeShort(x) { return this._int(x & 0xffff, 2); }
  writeInt(x) { return this._int(x >>> 0, 4); }
  writeLong(x) {
    let v = BigInt.asUintN(64, BigInt(x));
    const b = new Uint8Array(8);
    for (let i = 7; i >= 0; i--) { b[i] = Number(v & 0xffn); v >>= 8n; }
    this._push(b);
    return this;
  }
  writeString(s) { this._push(encodeModifiedUTF(s)); return this; }
  writeBooleans(a) { this.writeShort(a.length); for (const x of a) this.writeBoolean(x); return this; }
  writeBytes(a) { this.writeInt(a.length); this._push(a instanceof Uint8Array ? a : Uint8Array.from(a)); return this; }
  writeShorts(a) { this.writeShort(a.length); for (const x of a) this.writeShort(x); return this; }
  writeInts(a) { this.writeShort(a.length); for (const x of a) this.writeInt(x); return this; }
  writeLongs(a) { this.writeShort(a.length); for (const x of a) this.writeLong(x); return this; }
  writeStrings(a) { this.writeShort(a.length); for (const x of a) this.writeString(x); return this; }

  /** The raw segment bytes: [type][serial?][fields...]. */
  toBytes() {
    const out = new Uint8Array(this._len);
    let off = 0;
    for (const c of this._chunks) { out.set(c, off); off += c.length; }
    return out;
  }
}
