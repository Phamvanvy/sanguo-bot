/*
 * The VM-side UASegment — com/pip/io/UASegment.java as the .etf scripts see it
 * through the 0x0071..0x008F syscall block.
 *
 * Unlike net/ua-codec.js (which splits read/write into two one-shot classes for
 * the wire layer), a script segment is a long-lived object: UWAP_Create makes it,
 * fields are written or read in place, and SendRequest ships it. So this class
 * keeps ONE growable buffer with a read cursor, exactly like the Java original
 * keeps outputCache + a byte[] to read back after flush().
 *
 * Layout: [ type:int16 ][ serial:int32 if needSerial ][ fields... ]
 */
import { encodeModifiedUTF, decodeModifiedUTF } from '../net/ua-codec.js';

let serialKey = 0;
export function nextSerialKey() {
  return ++serialKey;
}

export class UASegment {
  /**
   * @param {number} type opcode (int16)
   * @param {boolean} needSerial when true, an int serial is allocated and written
   */
  constructor(type, needSerial) {
    /** @type {number[]} */
    this.buf = [];
    this.type = type | 0;
    this.serial = -1;
    this.handled = false;
    this.needResponse = false;
    // reads start after the header THIS constructor wrote: type (+ serial).
    // Incoming segments built from received bytes keep pos = 2, where the
    // echoed serial (if any) is just the first field the script reads.
    this.startPos = needSerial ? 6 : 2;
    this.pos = this.startPos;
    this._writeShort(this.type);
    if (needSerial) {
      this.serial = nextSerialKey();
      this._writeInt(this.serial);
    }
  }

  /* ---- writes append at the end ------------------------------------- */

  _push(...bytes) {
    for (const b of bytes) this.buf.push(b & 0xff);
  }

  _writeShort(v) {
    this._push((v >> 8) & 0xff, v & 0xff);
  }

  _writeInt(v) {
    this._push((v >>> 24) & 0xff, (v >> 16) & 0xff, (v >> 8) & 0xff, v & 0xff);
  }

  writeBoolean(v) { this._push(v ? 1 : 0); }
  writeByte(v) { this._push(v); }
  writeShort(v) { this._writeShort(v); }
  writeInt(v) { this._writeInt(v); }

  /** length prefix first (that is what readString expects), then the body. */
  writeString(s) {
    const enc = encodeModifiedUTF(s == null ? '' : String(s));
    this._writeShort(enc.length - 2);
    for (let i = 2; i < enc.length; i++) this.buf.push(enc[i]);
  }

  /* ---- array writers: u16 count prefix, matching the read*() side ---- */

  writeBooleans(a) { this._writeShort(a.length); for (const x of a) this.writeBoolean(x); }
  writeBytes(a) { this._writeInt(a.length); for (let i = 0; i < a.length; i++) this.buf.push(a[i] & 0xff); }
  writeShorts(a) { this._writeShort(a.length); for (const x of a) this.writeShort(x); }
  writeInts(a) { this._writeShort(a.length); for (const x of a) this.writeInt(x); }
  writeStrings(a) { this._writeShort(a.length); for (const x of a) this.writeString(x); }

  /* ---- reads advance pos --------------------------------------------- */

  _need(n) {
    if (this.pos + n > this.buf.length) {
      throw new RangeError(`UASegment: read past end at ${this.pos} (+${n}) of ${this.buf.length}`);
    }
  }

  _readNum(len) {
    this._need(len);
    let v = 0;
    for (let i = 0; i < len; i++) v = (v << 8) | (this.buf[this.pos + i] & 0xff);
    this.pos += len;
    return v;
  }

  readBoolean() { this._need(1); return this.buf[this.pos++] === 1; }
  readByte() { this._need(1); const b = this.buf[this.pos++]; return b > 0x7f ? b - 0x100 : b; }
  readUnsignedByte() { return this._readNum(1); }
  readShort() { const v = this._readNum(2); return v > 0x7fff ? v - 0x10000 : v; }
  readUnsignedShort() { return this._readNum(2); }
  readInt() { return this._readNum(4) | 0; }

  /** UASegment.readString: modified-UTF8 branch OR high-bit UTF-16 branch. */
  readString() {
    this._need(2);
    if ((this.buf[this.pos] & 0x80) === 0) {
      const len = ((this.buf[this.pos] & 0xff) << 8) | (this.buf[this.pos + 1] & 0xff);
      this._need(2 + len);
      const bytes = Uint8Array.from(this.buf.slice(this.pos, this.pos + 2 + len));
      const { value } = decodeModifiedUTF(bytes, 0);
      this.pos += 2 + len;
      return value;
    }
    const len = this._readNum(2) & 0x7fff;
    this._need(len);
    let out = '';
    for (let i = 0; i < (len >> 1); i++) {
      out += String.fromCharCode(((this.buf[this.pos] & 0xff) << 8) | (this.buf[this.pos + 1] & 0xff));
      this.pos += 2;
    }
    return out;
  }

  readBooleans() { const n = this.readUnsignedShort(); const r = new Array(n); for (let i = 0; i < n; i++) r[i] = this.readBoolean(); return r; }
  readBytes() {
    const n = this.readInt();
    this._need(n);
    const r = Uint8Array.from(this.buf.slice(this.pos, this.pos + n));
    this.pos += n;
    return r;
  }
  readShorts() { const n = this.readUnsignedShort(); const r = new Array(n); for (let i = 0; i < n; i++) r[i] = this.readShort(); return r; }
  readInts() { const n = this.readUnsignedShort(); const r = new Array(n); for (let i = 0; i < n; i++) r[i] = this.readInt(); return r; }
  readStrings() { const n = this.readUnsignedShort(); const r = new Array(n); for (let i = 0; i < n; i++) r[i] = this.readString(); return r; }

  /**
   * Build the script-side view of an INCOMING segment — UASocketConnection.
   * readSegment's `new UASegment(buf)`: the whole wire segment (type header
   * included) is copied, type is read from the first two bytes, and the read
   * cursor starts at 2, where the echoed serial (if any) is just the first
   * field the script reads. `bytes` must already be a private copy — the
   * accumulator hands us one, and this constructor copies again so neither
   * side can observe the other's mutations.
   */
  static fromBytes(bytes) {
    const seg = new UASegment(0, false);
    seg.buf = Array.from(bytes, (b) => b & 0xff);
    seg.type = ((seg.buf[0] & 0xff) << 8) | (seg.buf[1] & 0xff);
    seg.startPos = 2;
    seg.pos = 2;
    return seg;
  }

  /* ---- lifecycle ------------------------------------------------------ */

  /** Java flush(): seal the written bytes so they can be read/sent. Here the
   *  buffer is always current, so this only rewinds the read cursor. */
  flush() { /* no-op */ }

  reset() { this.pos = this.startPos; }

  toBytes() {
    return Uint8Array.from(this.buf);
  }
}