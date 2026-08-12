/*
 * decodePNG — PNG -> RGBA8888, dependency-free and synchronous.
 *
 * Merge-mode `.pip` files ("PIM", which is every Flash/Android asset in this data set) keep
 * their frames inside embedded PNGs, so a PNG decoder is not optional for rendering. The
 * browser has `createImageBitmap`, but it is async and would infect the whole decoder chain
 * (see the note in inflate.js), so this decodes in-process in both Node and the browser.
 *
 * Supported: bit depth 8, colour types 0/2/3/4/6, non-interlaced — which covers everything
 * the game ships. Anything else throws rather than guessing.
 */
import { inflateZlib } from './inflate.js';

const SIGNATURE = [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a];
/** Channel count per colour type: 0 grey, 2 RGB, 3 indexed, 4 grey+alpha, 6 RGBA. */
const CHANNELS = { 0: 1, 2: 3, 3: 1, 4: 2, 6: 4 };

const u32 = (b, o) => ((b[o] << 24) | (b[o + 1] << 16) | (b[o + 2] << 8) | b[o + 3]) >>> 0;

/**
 * @param {Uint8Array} bytes a PNG file
 * @param {{inflate?: (b: Uint8Array) => Uint8Array}} [opts] override the zlib decoder
 *   (Node passes `zlib.inflateSync` where throughput matters)
 * @returns {{width:number, height:number, rgba:Uint8Array}}
 */
export function decodePNG(bytes, opts = {}) {
  for (let i = 0; i < SIGNATURE.length; i++) {
    if (bytes[i] !== SIGNATURE[i]) throw new Error('decodePNG: not a PNG');
  }

  let width = 0, height = 0, depth = 0, colorType = 0;
  let palette = null, trns = null;
  const idat = [];
  let o = 8;
  while (o + 8 <= bytes.length) {
    const len = u32(bytes, o);
    const type = String.fromCharCode(bytes[o + 4], bytes[o + 5], bytes[o + 6], bytes[o + 7]);
    const data = bytes.subarray(o + 8, o + 8 + len);
    if (type === 'IHDR') {
      width = u32(data, 0);
      height = u32(data, 4);
      depth = data[8];
      colorType = data[9];
      if (data[12] !== 0) throw new Error('decodePNG: interlaced PNGs are not supported');
    } else if (type === 'PLTE') palette = data;
    else if (type === 'tRNS') trns = data;
    else if (type === 'IDAT') idat.push(data);
    else if (type === 'IEND') break;
    o += 12 + len;
  }
  if (depth !== 8) throw new Error(`decodePNG: bit depth ${depth} is not supported (only 8)`);
  const channels = CHANNELS[colorType];
  if (!channels) throw new Error(`decodePNG: colour type ${colorType} is not supported`);
  if (colorType === 3 && !palette) throw new Error('decodePNG: indexed PNG without a PLTE chunk');

  const inflate = opts.inflate || inflateZlib;
  const raw = inflate(concat(idat));

  const bpp = channels;                       // depth is 8, so bytes == channels
  const stride = width * bpp;
  if (raw.length < height * (stride + 1)) {
    throw new Error(`decodePNG: IDAT holds ${raw.length} bytes, need ${height * (stride + 1)}`);
  }
  const lines = new Uint8Array(height * stride);

  // Undo the per-scanline filters (PNG spec 9.2). `prev` is the reconstructed line above.
  for (let y = 0; y < height; y++) {
    const filter = raw[y * (stride + 1)];
    const src = y * (stride + 1) + 1;
    const cur = y * stride;
    const prev = (y - 1) * stride;
    for (let i = 0; i < stride; i++) {
      const a = i >= bpp ? lines[cur + i - bpp] : 0;             // left
      const b = y > 0 ? lines[prev + i] : 0;                     // up
      const c = y > 0 && i >= bpp ? lines[prev + i - bpp] : 0;   // up-left
      let v = raw[src + i];
      if (filter === 1) v += a;
      else if (filter === 2) v += b;
      else if (filter === 3) v += (a + b) >> 1;
      else if (filter === 4) {
        const p = a + b - c;
        const pa = Math.abs(p - a), pb = Math.abs(p - b), pc = Math.abs(p - c);
        v += pa <= pb && pa <= pc ? a : pb <= pc ? b : c;
      } else if (filter !== 0) throw new Error(`decodePNG: unknown filter ${filter} on row ${y}`);
      lines[cur + i] = v & 0xff;
    }
  }

  const rgba = new Uint8Array(width * height * 4);
  for (let i = 0; i < width * height; i++) {
    const s = i * bpp;
    const d = i * 4;
    if (colorType === 3) {
      const idx = lines[s];
      rgba[d] = palette[idx * 3];
      rgba[d + 1] = palette[idx * 3 + 1];
      rgba[d + 2] = palette[idx * 3 + 2];
      rgba[d + 3] = trns && idx < trns.length ? trns[idx] : 0xff;
    } else if (colorType === 0 || colorType === 4) {
      rgba[d] = rgba[d + 1] = rgba[d + 2] = lines[s];
      rgba[d + 3] = colorType === 4 ? lines[s + 1] : 0xff;
    } else {
      rgba[d] = lines[s];
      rgba[d + 1] = lines[s + 1];
      rgba[d + 2] = lines[s + 2];
      rgba[d + 3] = colorType === 6 ? lines[s + 3] : 0xff;
    }
  }
  return { width, height, rgba };
}

function concat(chunks) {
  if (chunks.length === 1) return chunks[0];
  const total = chunks.reduce((n, c) => n + c.length, 0);
  const out = new Uint8Array(total);
  let o = 0;
  for (const c of chunks) { out.set(c, o); o += c.length; }
  return out;
}
