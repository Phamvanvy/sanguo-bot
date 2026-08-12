/*
 * Tests for the dependency-free inflate + PNG decoder that let the asset pipeline run in the
 * browser.
 *
 * The bar is parity with `node:zlib` on the game's OWN files, not on synthetic data: the whole
 * point of this code is to replace zlib for exactly those bytes, and Java's GZIPOutputStream
 * is the only producer that matters.
 */
import { test } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { fileURLToPath } from 'node:url';

import { inflateRaw, inflateZlib, gunzip, decompress } from './inflate.js';
import { decodePNG } from './png.js';
import { codecs } from './codecs.js';
import { GamePackage } from './package-file.js';
import { PipImage } from './pip-image.js';

const REPO = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../../../..');
const DATA = path.join(REPO, 'selfhost/runtime/data');
const hasData = fs.existsSync(DATA);
const skipNoData = { skip: hasData ? false : `no game data at ${DATA}` };

const read = (p) => new Uint8Array(fs.readFileSync(p));
const bytes = (buf) => new Uint8Array(buf.buffer, buf.byteOffset, buf.length);

/* ---------- DEFLATE containers ---------- */

test('inflate: gzip round-trips what node:zlib produced', () => {
  const src = new TextEncoder().encode('Tân đào viên'.repeat(500));
  assert.deepEqual(gunzip(bytes(zlib.gzipSync(src))), src);
});

test('inflate: zlib and raw streams decode too', () => {
  const src = new Uint8Array(10000).map((_, i) => (i * 7) & 0xff);
  assert.deepEqual(inflateZlib(bytes(zlib.deflateSync(src))), src);
  assert.deepEqual(inflateRaw(bytes(zlib.deflateRawSync(src))), src);
  assert.deepEqual(decompress(bytes(zlib.gzipSync(src))), src);
  assert.deepEqual(decompress(bytes(zlib.deflateSync(src))), src);
});

test('inflate: handles stored blocks, fixed-Huffman blocks and empty input', () => {
  // Random bytes do not compress, so zlib emits stored blocks; a tiny input gets the fixed
  // tables. Both are code paths the game's own files rarely exercise.
  const noise = new Uint8Array(70000);
  for (let i = 0; i < noise.length; i++) noise[i] = (i * 2654435761) & 0xff;
  const shuffled = noise.slice().sort(() => 0);
  assert.deepEqual(inflateRaw(bytes(zlib.deflateRawSync(shuffled, { level: 0 }))), shuffled);
  assert.deepEqual(gunzip(bytes(zlib.gzipSync(new Uint8Array(0)))), new Uint8Array(0));
  assert.deepEqual(gunzip(bytes(zlib.gzipSync(new Uint8Array([42])))), new Uint8Array([42]));
});

test('inflate: back-references that overlap the write cursor expand correctly', () => {
  // "aaaa..." compresses to a length/distance pair whose distance (1) is shorter than its
  // length, so the copy has to read bytes it is writing.
  const src = new Uint8Array(5000).fill(97);
  assert.deepEqual(inflateRaw(bytes(zlib.deflateRawSync(src))), src);
});

test('inflate: rejects corrupt input instead of returning garbage', () => {
  assert.throws(() => gunzip(new Uint8Array([1, 2, 3])), /too short|not a gzip/);
  assert.throws(() => gunzip(bytes(zlib.gzipSync(new Uint8Array(64)))
    .map((b, i) => (i > 12 && i < 20 ? b ^ 0xff : b))), /inflate:/);
  assert.throws(() => inflateZlib(new Uint8Array([0x78, 0x9c, 0, 0, 0, 0])), /inflate:/);
});

test('inflate: gzip files written by Java decode byte-for-byte like zlib', skipNoData, () => {
  const pkg = new GamePackage(read(path.join(DATA, 'Areas/87_1/client.pkg')), codecs);
  let checked = 0;
  for (const name of pkg.fileNames) {
    const f = pkg.getFile('/' + name);
    if (f[0] !== 0x1f || f[1] !== 0x8b) continue;      // only the gzipped entries
    assert.deepEqual(gunzip(f), bytes(zlib.gunzipSync(Buffer.from(f))), `mismatch in ${name}`);
    checked++;
  }
  assert.ok(checked > 0, 'expected at least one gzipped entry in the area package');
});

/* ---------- PNG ---------- */

test('png: decodes what the tooling encodes, for every filter zlib picks', () => {
  const w = 61, h = 37;                                 // odd sizes: no accidental alignment
  const rgba = new Uint8Array(w * h * 4);
  for (let i = 0; i < w * h; i++) {
    rgba[i * 4] = i & 0xff;
    rgba[i * 4 + 1] = (i * 3) & 0xff;
    rgba[i * 4 + 2] = (i >> 3) & 0xff;
    rgba[i * 4 + 3] = 255 - (i & 0x3f);
  }
  const png = encodeViaZlib(rgba, w, h);
  const out = decodePNG(png);
  assert.equal(out.width, w);
  assert.equal(out.height, h);
  assert.deepEqual(out.rgba, rgba);
});

test('png: the real embedded sheets decode identically with zlib and with our inflate', skipNoData, () => {
  const nodeCodecs = {
    inflate: (b) => bytes(zlib.gunzipSync(Buffer.from(b.buffer, b.byteOffset, b.length))),
    decodePNG: (b) => decodePNG(b, {
      inflate: (z) => bytes(zlib.inflateSync(Buffer.from(z.buffer, z.byteOffset, z.length))),
    }),
  };
  const file = read(path.join(DATA, 'client_pkg/Flash/body_2.pip'));
  const viaZlib = new PipImage(file, nodeCodecs);
  const viaOurs = new PipImage(file, codecs);
  assert.ok(viaOurs.mergeMode, 'expected a merge-mode pip (embedded PNGs)');
  assert.ok(viaOurs.blockCount > 0);
  for (let f = 0; f < viaOurs.blockCount; f++) {
    assert.deepEqual(viaOurs.frameToRGBA(f), viaZlib.frameToRGBA(f), `frame ${f} differs`);
  }
});

test('png: indexed PNGs honour PLTE and tRNS', skipNoData, () => {
  // The game's merge sheets are colour type 3 with a shared palette spliced in; a transparent
  // index must come out with alpha 0, or every sprite renders on a solid block.
  const pip = new PipImage(read(path.join(DATA, 'client_pkg/Flash/body_2.pip')), codecs);
  const sheet = decodePNG(pip.mergePNGs[0]);
  let transparent = 0;
  for (let i = 3; i < sheet.rgba.length; i += 4) if (sheet.rgba[i] === 0) transparent++;
  assert.ok(transparent > 0, 'expected transparent pixels in a sprite sheet');
});

test('png: rejects what it cannot decode rather than guessing', () => {
  assert.throws(() => decodePNG(new Uint8Array(16)), /not a PNG/);
});

/** Minimal RGBA PNG writer, used only to produce input for the decoder under test. */
function encodeViaZlib(rgba, width, height) {
  const raw = Buffer.alloc((width * 4 + 1) * height);
  for (let y = 0; y < height; y++) {
    const o = y * (width * 4 + 1);
    raw[o] = y % 5;                                     // cycle through all five filter types
    Buffer.from(rgba.buffer, rgba.byteOffset + y * width * 4, width * 4).copy(raw, o + 1);
  }
  // Filters have to be applied, not just declared, or the decoder would "undo" nothing.
  applyFilters(raw, width, height);
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8;
  ihdr[9] = 6;
  return bytes(Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    pngChunk('IHDR', ihdr),
    pngChunk('IDAT', zlib.deflateSync(raw)),
    pngChunk('IEND', Buffer.alloc(0)),
  ]));
}

function applyFilters(raw, width, height) {
  const stride = width * 4;
  const bpp = 4;
  const prevOriginal = Buffer.alloc(stride);
  const curOriginal = Buffer.alloc(stride);
  for (let y = 0; y < height; y++) {
    const o = y * (stride + 1);
    const filter = raw[o];
    raw.copy(curOriginal, 0, o + 1, o + 1 + stride);
    for (let i = stride - 1; i >= 0; i--) {
      const a = i >= bpp ? curOriginal[i - bpp] : 0;
      const b = y > 0 ? prevOriginal[i] : 0;
      const c = y > 0 && i >= bpp ? prevOriginal[i - bpp] : 0;
      let sub = 0;
      if (filter === 1) sub = a;
      else if (filter === 2) sub = b;
      else if (filter === 3) sub = (a + b) >> 1;
      else if (filter === 4) {
        const p = a + b - c;
        const pa = Math.abs(p - a), pb = Math.abs(p - b), pc = Math.abs(p - c);
        sub = pa <= pb && pa <= pc ? a : pb <= pc ? b : c;
      }
      raw[o + 1 + i] = (curOriginal[i] - sub) & 0xff;
    }
    curOriginal.copy(prevOriginal);
  }
}

function pngChunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length, 0);
  const body = Buffer.concat([Buffer.from(type, 'latin1'), Buffer.from(data)]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(zlib.crc32 ? zlib.crc32(body) : crc32(body), 0);
  return Buffer.concat([len, body, crc]);
}

function crc32(buf) {
  let c = -1;
  for (let i = 0; i < buf.length; i++) {
    c ^= buf[i];
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  }
  return (c ^ -1) >>> 0;
}
