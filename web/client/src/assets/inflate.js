/*
 * inflate — DEFLATE (RFC 1951), zlib (RFC 1950) and gzip (RFC 1952) decompression.
 *
 * Why hand-written instead of the platform's: every decoder in this directory
 * (PipImage blocks, merge-mode PNG sheets, GamePackage maps) is SYNCHRONOUS, because the Java
 * code it is ported from is. Node has `zlib.gunzipSync`, but the browser only offers
 * `DecompressionStream`, which is async and would force every caller — and every caller's
 * caller — to become async for no gain. ~200 lines of DEFLATE keeps one code path that runs
 * unchanged in both, and the tests check it byte-for-byte against `node:zlib` on the game's
 * own files.
 *
 * Decoding follows the "puff" reference structure: canonical Huffman tables described by
 * per-length symbol counts, decoded one bit at a time. Simple enough to verify by eye and
 * fast enough for what the client inflates (a map is tens of KB, the largest sprite sheet a
 * few hundred).
 */

const MAX_BITS = 15;

/** Lengths for symbols 257..285, and the extra-bit counts that go with them. */
const LENGTH_BASE = [3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59,
  67, 83, 99, 115, 131, 163, 195, 227, 258];
const LENGTH_EXTRA = [0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3,
  4, 4, 4, 4, 5, 5, 5, 5, 0];
const DIST_BASE = [1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513,
  769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577];
const DIST_EXTRA = [0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8,
  9, 9, 10, 10, 11, 11, 12, 12, 13, 13];
/** Order the code-length code lengths are stored in (RFC 1951 3.2.7). */
const CLEN_ORDER = [16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15];

class BitReader {
  constructor(bytes) {
    this.bytes = bytes;
    this.pos = 0;      // next byte
    this.bitBuf = 0;
    this.bitCount = 0;
  }

  /** `n` bits, LSB first (DEFLATE's bit order for everything except Huffman codes). */
  bits(n) {
    let val = this.bitBuf;
    while (this.bitCount < n) {
      if (this.pos >= this.bytes.length) throw new Error('inflate: out of input');
      val |= this.bytes[this.pos++] << this.bitCount;
      this.bitCount += 8;
    }
    this.bitBuf = val >>> n;
    this.bitCount -= n;
    return val & ((1 << n) - 1);
  }

  /** Drop the partial byte — used before a stored (uncompressed) block. */
  alignToByte() {
    this.bitBuf = 0;
    this.bitCount = 0;
  }
}

/**
 * A canonical Huffman table: `count[len]` = how many codes have that length, `symbol` = the
 * symbols sorted by (length, symbol). That pair is all a bit-at-a-time decoder needs.
 */
function buildHuffman(lengths) {
  const count = new Int32Array(MAX_BITS + 1);
  for (const l of lengths) count[l]++;
  count[0] = 0;

  const offs = new Int32Array(MAX_BITS + 2);
  for (let len = 1; len <= MAX_BITS; len++) offs[len + 1] = offs[len] + count[len];
  const symbol = new Int32Array(lengths.length);
  for (let s = 0; s < lengths.length; s++) if (lengths[s]) symbol[offs[lengths[s]]++] = s;

  return { count, symbol };
}

/** Walk the code bit by bit until it falls inside the range of codes of the current length. */
function decodeSymbol(br, huff) {
  let code = 0;
  let first = 0;
  let index = 0;
  for (let len = 1; len <= MAX_BITS; len++) {
    code |= br.bits(1);
    const c = huff.count[len];
    if (code - first < c) return huff.symbol[index + (code - first)];
    index += c;
    first = (first + c) << 1;
    code <<= 1;
  }
  throw new Error('inflate: invalid Huffman code');
}

/** Output buffer that grows geometrically — the decompressed size is not known up front. */
class Output {
  constructor(initial) {
    this.buf = new Uint8Array(Math.max(1024, initial | 0));
    this.len = 0;
  }

  _ensure(extra) {
    if (this.len + extra <= this.buf.length) return;
    let cap = this.buf.length * 2;
    while (cap < this.len + extra) cap *= 2;
    const next = new Uint8Array(cap);
    next.set(this.buf.subarray(0, this.len));
    this.buf = next;
  }

  byte(b) {
    this._ensure(1);
    this.buf[this.len++] = b;
  }

  /** LZ77 back-reference: copy `length` bytes from `distance` back, overlapping on purpose. */
  copy(distance, length) {
    if (distance > this.len) throw new Error(`inflate: distance ${distance} before start of output`);
    this._ensure(length);
    let from = this.len - distance;
    for (let i = 0; i < length; i++) this.buf[this.len++] = this.buf[from++];
  }

  raw(bytes) {
    this._ensure(bytes.length);
    this.buf.set(bytes, this.len);
    this.len += bytes.length;
  }

  result() { return this.buf.subarray(0, this.len); }
}

let FIXED_LIT = null;
let FIXED_DIST = null;

/** The fixed tables of RFC 1951 3.2.6, built once. */
function fixedTables() {
  if (!FIXED_LIT) {
    const lit = new Uint8Array(288);
    lit.fill(8, 0, 144); lit.fill(9, 144, 256); lit.fill(7, 256, 280); lit.fill(8, 280, 288);
    FIXED_LIT = buildHuffman(lit);
    FIXED_DIST = buildHuffman(new Uint8Array(30).fill(5));
  }
  return [FIXED_LIT, FIXED_DIST];
}

/** Read the dynamic (per-block) literal/distance tables of RFC 1951 3.2.7. */
function dynamicTables(br) {
  const hlit = br.bits(5) + 257;
  const hdist = br.bits(5) + 1;
  const hclen = br.bits(4) + 4;

  const clenLengths = new Uint8Array(19);
  for (let i = 0; i < hclen; i++) clenLengths[CLEN_ORDER[i]] = br.bits(3);
  const clenHuff = buildHuffman(clenLengths);

  // Code lengths are themselves Huffman-coded, with three repeat opcodes (16/17/18).
  const lengths = new Uint8Array(hlit + hdist);
  for (let i = 0; i < lengths.length;) {
    const sym = decodeSymbol(br, clenHuff);
    if (sym < 16) { lengths[i++] = sym; continue; }
    let repeat;
    let value = 0;
    if (sym === 16) {
      if (i === 0) throw new Error('inflate: repeat with no previous code length');
      value = lengths[i - 1];
      repeat = 3 + br.bits(2);
    } else if (sym === 17) repeat = 3 + br.bits(3);
    else repeat = 11 + br.bits(7);
    if (i + repeat > lengths.length) throw new Error('inflate: code length repeat overruns');
    for (let r = 0; r < repeat; r++) lengths[i++] = value;
  }

  return [buildHuffman(lengths.subarray(0, hlit)), buildHuffman(lengths.subarray(hlit))];
}

/**
 * Raw DEFLATE stream -> bytes.
 * @param {Uint8Array} bytes
 * @param {number} [sizeHint] expected output size, to skip a few reallocations
 * @returns {Uint8Array}
 */
export function inflateRaw(bytes, sizeHint = bytes.length * 4) {
  const br = new BitReader(bytes);
  const out = new Output(sizeHint);

  for (;;) {
    const final = br.bits(1);
    const type = br.bits(2);

    if (type === 0) {
      br.alignToByte();
      if (br.pos + 4 > bytes.length) throw new Error('inflate: truncated stored block header');
      const len = bytes[br.pos] | (bytes[br.pos + 1] << 8);
      const nlen = bytes[br.pos + 2] | (bytes[br.pos + 3] << 8);
      if ((len ^ 0xffff) !== nlen) throw new Error('inflate: stored block length check failed');
      br.pos += 4;
      if (br.pos + len > bytes.length) throw new Error('inflate: truncated stored block');
      out.raw(bytes.subarray(br.pos, br.pos + len));
      br.pos += len;
    } else if (type === 1 || type === 2) {
      const [litHuff, distHuff] = type === 1 ? fixedTables() : dynamicTables(br);
      for (;;) {
        const sym = decodeSymbol(br, litHuff);
        if (sym < 256) { out.byte(sym); continue; }
        if (sym === 256) break;                       // end of block
        const li = sym - 257;
        if (li >= LENGTH_BASE.length) throw new Error(`inflate: invalid length symbol ${sym}`);
        const length = LENGTH_BASE[li] + br.bits(LENGTH_EXTRA[li]);
        const dsym = decodeSymbol(br, distHuff);
        if (dsym >= DIST_BASE.length) throw new Error(`inflate: invalid distance symbol ${dsym}`);
        out.copy(DIST_BASE[dsym] + br.bits(DIST_EXTRA[dsym]), length);
      }
    } else {
      throw new Error('inflate: reserved block type 3');
    }

    if (final) break;
  }

  return out.result();
}

/** zlib container (RFC 1950): 2-byte header, deflate stream, Adler-32 trailer. */
export function inflateZlib(bytes) {
  if (bytes.length < 6) throw new Error('inflate: zlib stream too short');
  const cmf = bytes[0];
  const flg = bytes[1];
  if ((cmf & 0x0f) !== 8) throw new Error(`inflate: unsupported zlib compression method ${cmf & 0x0f}`);
  if (((cmf << 8) | flg) % 31 !== 0) throw new Error('inflate: bad zlib header check');
  if (flg & 0x20) throw new Error('inflate: zlib preset dictionaries are not supported');
  // The Adler-32 trailer is not verified: a corrupt stream fails inside the Huffman decoder
  // long before it would matter, and this decoder is only ever fed the game's own files.
  return inflateRaw(bytes.subarray(2, bytes.length - 4));
}

/**
 * gzip container (RFC 1952). The game's `.pip` "DATA" chunks and `<id>.m` maps are gzip,
 * because Java's GZIPOutputStream wrote them.
 */
export function gunzip(bytes) {
  if (bytes.length < 18) throw new Error('inflate: gzip stream too short');
  if (bytes[0] !== 0x1f || bytes[1] !== 0x8b) throw new Error('inflate: not a gzip stream');
  if (bytes[2] !== 8) throw new Error(`inflate: unsupported gzip method ${bytes[2]}`);
  const flg = bytes[3];
  let p = 10;
  if (flg & 0x04) p += 2 + (bytes[p] | (bytes[p + 1] << 8));       // FEXTRA
  if (flg & 0x08) while (bytes[p++] !== 0);                        // FNAME
  if (flg & 0x10) while (bytes[p++] !== 0);                        // FCOMMENT
  if (flg & 0x02) p += 2;                                          // FHCRC
  if (p >= bytes.length - 8) throw new Error('inflate: truncated gzip header');
  // ISIZE (the last 4 bytes, little-endian) is the exact output size mod 2^32 — a free,
  // usually exact hint that avoids growing the output buffer.
  const n = bytes.length;
  const isize = (bytes[n - 4] | (bytes[n - 3] << 8) | (bytes[n - 2] << 16) | (bytes[n - 1] << 24)) >>> 0;
  return inflateRaw(bytes.subarray(p, n - 8), isize);
}

/** Pick the container from the magic bytes. Handy where a file's framing is not fixed. */
export function decompress(bytes) {
  if (bytes[0] === 0x1f && bytes[1] === 0x8b) return gunzip(bytes);
  if ((bytes[0] & 0x0f) === 8 && ((bytes[0] << 8) | bytes[1]) % 31 === 0) return inflateZlib(bytes);
  return inflateRaw(bytes);
}
