/*
 * Java runtime value types the syscall layer hands to scripts.
 *
 * The .etf scripts treat these as opaque objects — every interaction goes
 * through a syscall — so the ports only have to be faithful where a syscall
 * can observe them: ordering, equality and the exact arithmetic widths.
 *
 * Ported from java.util.Vector / com.pip.util.SortHashtable /
 * java.io.DataInputStream / java.io.ByteArrayOutputStream as used by VM.java.
 */

/**
 * java.util.Vector. Scripts see it only through syscalls 0x0091..0x0098 and
 * Tool.mergeString, so element identity is what matters.
 */
export class JavaVector {
  constructor() {
    /** @type {any[]} */
    this.elements = [];
  }

  size() {
    return this.elements.length;
  }

  addElement(obj) {
    this.elements.push(obj);
  }

  insertElementAt(obj, index) {
    if (index < 0 || index > this.elements.length) return;
    this.elements.splice(index, 0, obj);
  }

  removeElementAt(index) {
    if (index < 0 || index >= this.elements.length) return;
    this.elements.splice(index, 1);
  }

  removeAllElements() {
    this.elements.length = 0;
  }

  elementAt(index) {
    return this.elements[index];
  }

  toArray() {
    return this.elements.slice();
  }
}

/** Java object equality as the hashtable syscalls observe it. */
function keyEquals(a, b) {
  if (a === b) return true;
  if (a instanceof VMInteger && b instanceof VMInteger) return a.value === b.value;
  return false;
}

/**
 * com.pip.util.SortHashtable. Despite the name it is INSERTION ordered:
 * vecKeys/vecValues keep put() order, remove() reindexes the tail. Keys compare
 * by Java equality — a boxed Integer(5) and the string "5" are different keys,
 * and two separately boxed Integer(5)s are the same one.
 */
export class SortHashtable {
  constructor() {
    /** @type {{key: any, value: any}[]} */
    this.entries = [];
  }

  clear() {
    this.entries.length = 0;
  }

  put(key, value) {
    this.remove(key);
    this.entries.push({ key, value });
  }

  remove(key) {
    const i = this.index(key);
    if (i >= 0) this.entries.splice(i, 1);
  }

  index(key) {
    for (let i = 0; i < this.entries.length; i++) {
      if (keyEquals(this.entries[i].key, key)) return i;
    }
    return -1;
  }

  get(key) {
    const i = this.index(key);
    return i >= 0 ? this.entries[i].value : null;
  }

  getKey(index) {
    return this.entries[index] ? this.entries[index].key : null;
  }

  getValue(index) {
    return this.entries[index] ? this.entries[index].value : null;
  }

  keys() {
    return this.entries.map((e) => e.key);
  }

  values() {
    return this.entries.map((e) => e.value);
  }

  size() {
    return this.entries.length;
  }
}

/**
 * java.lang.Integer as a heap object. Scripts box ints with 0x00B3 IntToObj and
 * unbox with 0x00C3 ObjToInt; hashtables keyed by them must match by value.
 */
export class VMInteger {
  constructor(value) {
    this.value = value | 0;
  }
}

/* ------------------------------------------------------------------ streams */

const i32be = (b, p) =>
  (((b[p] << 24) | (b[p + 1] << 16) | (b[p + 2] << 8) | b[p + 3]) | 0);

/**
 * java.io.DataInputStream over an in-memory buffer, as created by
 * Stream_Create (0x0051). Every read advances the position; reading past the
 * end throws like Java's EOFException would (the VM catches and reports).
 */
export class DataInputStream {
  /** @param {Uint8Array} bytes */
  constructor(bytes) {
    this.bytes = bytes;
    this.pos = 0;
  }

  _need(n) {
    if (this.pos + n > this.bytes.length) {
      throw new Error(`EOF after ${this.pos} of ${this.bytes.length} bytes`);
    }
  }

  readByte() {
    this._need(1);
    return (this.bytes[this.pos++] << 24) >> 24;
  }

  readUnsignedByte() {
    this._need(1);
    return this.bytes[this.pos++];
  }

  readBoolean() {
    return this.readByte() !== 0;
  }

  readShort() {
    this._need(2);
    const v = ((this.bytes[this.pos] << 8) | this.bytes[this.pos + 1]) << 16 >> 16;
    this.pos += 2;
    return v;
  }

  readUnsignedShort() {
    this._need(2);
    const v = (this.bytes[this.pos] << 8) | this.bytes[this.pos + 1];
    this.pos += 2;
    return v;
  }

  readInt() {
    this._need(4);
    const v = i32be(this.bytes, this.pos);
    this.pos += 4;
    return v;
  }

  /** readFully — fills the whole target array or throws. */
  readFully(target) {
    this._need(target.length);
    target.set(this.bytes.subarray(this.pos, this.pos + target.length));
    this.pos += target.length;
  }

  /**
   * readUTF — Java modified UTF-8 with a 2-byte unsigned length prefix.
   * decodeModifiedUTF(bytes, pos) expects exactly that layout.
   */
  readUTF() {
    const utflen = this.readUnsignedShort();
    this._need(utflen);
    // lazy import avoided: decodeModifiedUTF is cheap to inline via dynamic import? No —
    // keep the module dependency explicit instead.
    const str = decodeUTF(this.bytes, this.pos, utflen);
    this.pos += utflen;
    return str;
  }
}

import { decodeModifiedUTF } from '../net/ua-codec.js';

/** decode `len` bytes of modified UTF-8 starting at pos. */
function decodeUTF(bytes, pos, len) {
  // decodeModifiedUTF reads its own 2-byte length prefix, so hand it a view
  // whose first two bytes ARE that prefix.
  const view = new Uint8Array(len + 2);
  view[0] = (len >> 8) & 0xff;
  view[1] = len & 0xff;
  view.set(bytes.subarray(pos, pos + len), 2);
  return decodeModifiedUTF(view, 0).value;
}

/**
 * java.io.ByteArrayOutputStream behind Stream_Create2 (0x0052), written through
 * DataOutputStream wrappers in the syscalls.
 */
export class ByteArrayOutputStream {
  constructor() {
    /** @type {number[]} */
    this.buf = [];
  }

  _push(...bytes) {
    for (const b of bytes) this.buf.push(b & 0xff);
  }

  size() {
    return this.buf.length;
  }

  reset() {
    this.buf.length = 0;
  }

  writeByte(v) {
    this._push(v);
  }

  writeBoolean(v) {
    this._push(v ? 1 : 0);
  }

  writeShort(v) {
    this._push((v >> 8) & 0xff, v & 0xff);
  }

  writeInt(v) {
    this._push((v >>> 24) & 0xff, (v >> 16) & 0xff, (v >> 8) & 0xff, v & 0xff);
  }

  /** writeUTF — modified UTF-8 with the 2-byte length prefix. */
  writeUTF(str) {
    const body = encodeUTFBody(str);
    this.writeShort(body.length);
    for (const b of body) this.buf.push(b);
  }

  writeBytes(bytes) {
    for (let i = 0; i < bytes.length; i++) this.buf.push(bytes[i] & 0xff);
  }

  toByteArray() {
    return Uint8Array.from(this.buf);
  }
}

import { encodeModifiedUTF } from '../net/ua-codec.js';

/** encode a string as modified UTF-8 WITHOUT the length prefix. */
function encodeUTFBody(str) {
  // encodeModifiedUTF writes the 2-byte prefix first; strip it.
  const full = encodeModifiedUTF(str == null ? '' : String(str));
  return full.subarray(2);
}