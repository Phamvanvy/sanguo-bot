/*
 * DataIn — a JS port of java.io.DataInputStream over a Uint8Array.
 *
 * The game's asset files (unlike the UA wire protocol) are plain
 * DataInputStream dumps: big-endian primitives + Tool.readUTF (Java modified UTF-8).
 * `web/client/src/net/ua-codec.js` already speaks that string encoding, so the
 * decoder is shared rather than re-implemented.
 *
 * Every read is bounds-checked: a truncated/misparsed asset must fail loudly
 * instead of yielding `undefined`-flavoured garbage that looks like real data.
 */
import { decodeModifiedUTF } from '../net/ua-codec.js';

export class DataIn {
  /** @param {Uint8Array} bytes @param {number} [pos] */
  constructor(bytes, pos = 0) {
    this.data = bytes;
    this.pos = pos;
  }

  remaining() { return this.data.length - this.pos; }

  _need(n) {
    if (this.pos + n > this.data.length) {
      throw new RangeError(
        `DataIn: read past end: need ${n} byte(s) at ${this.pos}, have ${this.remaining()}`);
    }
  }

  _num(len) {
    this._need(len);
    let v = 0;
    for (let i = 0; i < len; i++) v = (v << 8) | (this.data[this.pos + i] & 0xff);
    this.pos += len;
    return v;
  }

  readByte() { this._need(1); const b = this.data[this.pos++]; return b > 0x7f ? b - 0x100 : b; }
  readUnsignedByte() { this._need(1); return this.data[this.pos++] & 0xff; }
  readShort() { const v = this._num(2); return v > 0x7fff ? v - 0x10000 : v; }
  readUnsignedShort() { return this._num(2); }
  readInt() { return this._num(4) | 0; }

  /** Tool.readUTF — Java modified UTF-8 with a u16 byte-length prefix. */
  readUTF() {
    this._need(2);
    const { value, next } = decodeModifiedUTF(this.data, this.pos);
    this.pos = next;
    return value;
  }

  /** DataInputStream.readFully — returns a view, not a copy, when possible. */
  readBytes(n) {
    this._need(n);
    const out = this.data.subarray(this.pos, this.pos + n);
    this.pos += n;
    return out;
  }

  skip(n) { this._need(n); this.pos += n; }
}
