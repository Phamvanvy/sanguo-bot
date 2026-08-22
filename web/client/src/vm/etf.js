/*
 * ETF reader — the container that holds the game's UI.
 *
 * Port of VM.loadETF (Game/sangobuildVn/client/src/com/pip/ui/VM.java). One .etf is
 * one script: "game_world", "game_panel", "ui_bag", ... The server already ships
 * them at data/scripts/<UIModel>/<name>_<UIModel>.etf.gz, so the browser client can
 * run the ORIGINAL interface instead of a re-drawn one.
 *
 * Layout (big-endian, sections in order):
 *   "EGL" + languageVersion(1 byte, 0 or 1)
 *   i16 fileVersion | i16 libraryID | 4 bytes last-modified (skipped)
 *   i16 heapSize | i16 taskAttr | utf16 name | utf16 description | i32 source length
 *   "ST" i16 count | i16 byteLen | count x utf16   -- string table (optional)
 *   "CT" i16 count | i32 byteLen |                 -- code table
 *        count x { u8 paramCount | paramCount bytes (param types, skipped)
 *                  u16 localVariables | i32 codeLen | codeLen bytes }
 *   languageVersion 1 only:
 *   "CB" i16 count | i16 byteLen | count x (utf16 name, i16 funcId)  -- callbacks
 *   "LB" i16 count | i16 byteLen | count x utf16                     -- linked libs
 *
 * Strings are UTF-16BE with a 1- or 2-byte CHARACTER count (Tool.readUTF16), not the
 * modified UTF-8 the wire protocol and the asset files use.
 */

const HEAD_EGL0 = 0x45474c00;
const HEAD_EGL1 = 0x45474c01;
const TAG_ST = 0x5354;
const TAG_CT = 0x4354;
const TAG_CB = 0x4342;
const TAG_LB = 0x4c42;

class EtfIn {
  /** @param {Uint8Array} bytes */
  constructor(bytes) {
    this.data = bytes;
    this.pos = 0;
  }

  _need(n) {
    if (this.pos + n > this.data.length) {
      throw new RangeError(`ETF: read past end: need ${n} at ${this.pos}, ` +
        `have ${this.data.length - this.pos}`);
    }
  }

  u8() { this._need(1); return this.data[this.pos++]; }
  i8() { const v = this.u8(); return v > 0x7f ? v - 0x100 : v; }
  u16() { this._need(2); const v = (this.data[this.pos] << 8) | this.data[this.pos + 1]; this.pos += 2; return v; }
  i16() { const v = this.u16(); return v > 0x7fff ? v - 0x10000 : v; }
  i32() {
    this._need(4);
    const d = this.data, p = this.pos;
    this.pos += 4;
    return ((d[p] << 24) | (d[p + 1] << 16) | (d[p + 2] << 8) | d[p + 3]) | 0;
  }

  bytes(n) { this._need(n); const out = this.data.subarray(this.pos, this.pos + n); this.pos += n; return out; }
  skip(n) { this._need(n); this.pos += n; }

  /** Tool.readUTF16: u8/u15 character count then that many UTF-16BE code units. */
  utf16() {
    let len = this.u8();
    if ((len & 0x80) !== 0) len = ((len & 0x7f) << 8) + this.u8();
    let s = '';
    for (let i = 0; i < len; i++) s += String.fromCharCode(this.u16());
    return s;
  }
}

/** Byte cost of a string inside a section's declared length, per VM.loadETF. */
function utf16Size(s) {
  return (s.length < 128 ? 1 : 2) + 2 * s.length;
}

/**
 * @typedef {object} EtfFunction
 * @property {number} paramCount
 * @property {number} localVariables
 * @property {number} start  offset into `code`
 * @property {number} end    exclusive offset into `code`
 */

/**
 * @typedef {object} Etf
 * @property {number} languageVersion 0 = no callbacks section, 1 = has one
 * @property {number} fileVersion  0 = a normal script, 1 = a serialised/resumed one
 * @property {number} libraryID
 * @property {number} heapSize     static heap slots
 * @property {number} stackSize    VM.taskAttr, used as the stack size
 * @property {string} name
 * @property {string} description
 * @property {string[]} stringTable
 * @property {EtfFunction[]} functions
 * @property {Int32Array} functionTable  VM.functions: 3 ints per function,
 *   [(paramCount << 16) | localVariables, codeStart, codeEnd]
 * @property {Uint8Array} code     all function bodies, concatenated
 * @property {Map<string, number>} callbacks  callback name -> function id
 * @property {string[]} libNames   scripts this one links against
 */

/**
 * Parse one decompressed .etf image.
 *
 * @param {Uint8Array} bytes
 * @returns {Etf}
 */
export function parseETF(bytes) {
  const is = new EtfIn(bytes);

  const head = is.i32();
  if (head !== HEAD_EGL0 && head !== HEAD_EGL1) {
    throw new Error(`ETF: bad magic 0x${(head >>> 0).toString(16)} (expected EGL0/EGL1)`);
  }
  const languageVersion = head & 0xff;
  const fileVersion = is.i16();
  const libraryID = is.i16();
  is.skip(4); // last-modified time
  const heapSize = is.u16();
  const stackSize = is.u16();
  const name = is.utf16();
  const description = is.utf16();
  is.i32(); // length of the .gtl source, unused at runtime

  // --- ST: string table ---------------------------------------------------
  let tag = is.u16();
  /** @type {string[]} */
  let stringTable = [];
  if (tag === TAG_ST) {
    const count = is.i16();
    if (count <= 0) throw new Error('ETF: empty string table');
    let len = is.i16();
    while (len > 0) {
      const s = is.utf16();
      len -= utf16Size(s);
      stringTable.push(s);
    }
    if (len !== 0 || stringTable.length !== count) {
      throw new Error(`ETF: string table mismatch (${stringTable.length}/${count}, ${len} bytes left)`);
    }
    tag = is.u16();
  }

  // --- CT: code table -----------------------------------------------------
  if (tag !== TAG_CT) throw new Error('ETF: missing code table');
  const funcCount = is.i16();
  if (funcCount <= 0) throw new Error('ETF: empty code table');
  let ctLen = is.i32();
  const code = new Uint8Array(ctLen);
  /** @type {EtfFunction[]} */
  const functions = [];
  // The interpreter indexes the flat form (VM.functions) directly, so build both.
  const functionTable = new Int32Array(funcCount * 3);
  let codePos = 0;
  for (let i = 0; i < funcCount; i++) {
    const paramCount = is.u8();
    is.skip(paramCount); // per-parameter type tags
    const localVariables = is.u16();
    const funcLen = is.i32();
    code.set(is.bytes(funcLen), codePos);
    functions.push({ paramCount, localVariables, start: codePos, end: codePos + funcLen });
    functionTable[i * 3] = (paramCount << 16) | localVariables;
    functionTable[i * 3 + 1] = codePos;
    functionTable[i * 3 + 2] = codePos + funcLen;
    codePos += funcLen;
    ctLen -= 1 + paramCount + 2 + 4 + funcLen;
  }
  if (ctLen !== 0) throw new Error(`ETF: code table mismatch (${ctLen} bytes left)`);

  /** @type {Map<string, number>} */
  const callbacks = new Map();
  /** @type {string[]} */
  const libNames = [];

  if (languageVersion === 1) {
    // --- CB: callbacks ----------------------------------------------------
    if (is.u16() !== TAG_CB) throw new Error('ETF: missing callback table');
    const cbCount = is.i16();
    if (cbCount < 0) throw new Error('ETF: bad callback count');
    let cbLen = is.i16();
    for (let i = 0; i < cbCount; i++) {
      const s = is.utf16();
      cbLen -= utf16Size(s);
      callbacks.set(s, is.i16());
      cbLen -= 2;
    }
    if (cbLen !== 0) throw new Error(`ETF: callback table mismatch (${cbLen} bytes left)`);

    // --- LB: linked libraries --------------------------------------------
    if (is.u16() !== TAG_LB) throw new Error('ETF: missing library table');
    const libCount = is.i16();
    if (libCount < 0) throw new Error('ETF: bad library count');
    let lbLen = is.i16();
    for (let i = 0; i < libCount; i++) {
      const s = is.utf16();
      lbLen -= utf16Size(s);
      libNames.push(s);
    }
    if (lbLen !== 0) throw new Error(`ETF: library table mismatch (${lbLen} bytes left)`);
  }

  return {
    languageVersion, fileVersion, libraryID, heapSize, stackSize,
    name, description, stringTable, functions, functionTable, code, callbacks, libNames,
  };
}
