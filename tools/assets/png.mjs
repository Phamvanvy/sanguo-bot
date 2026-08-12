/*
 * PNG writing for the Node-side asset tooling (Node, zlib only, no dependencies).
 *
 * encodePNG: RGBA8888 -> PNG, colour type 6, bit depth 8, filter 0 on every scanline. Used by
 * the asset spike to write decoded frames and rendered maps to disk so they can be eyeballed.
 *
 * Decoding lives in the client's own png.js (it has to run in the browser too); this file just
 * hands it `zlib.inflateSync`, which is a good deal faster than the JS DEFLATE for the big
 * merge-mode sprite sheets the spike walks through.
 */
import zlib from 'node:zlib';
import { decodePNG as decodePNGShared } from '../../web/client/src/assets/png.js';

const CRC_TABLE = (() => {
  const t = new Int32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c;
  }
  return t;
})();

function crc32(buf) {
  let c = -1;
  for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xff] ^ (c >>> 8);
  return (c ^ -1) >>> 0;
}

function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length, 0);
  const body = Buffer.concat([Buffer.from(type, 'latin1'), Buffer.from(data)]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(body), 0);
  return Buffer.concat([len, body, crc]);
}

/**
 * @param {Uint8Array} rgba width*height*4 bytes
 * @returns {Buffer} a complete PNG file
 */
export function encodePNG(rgba, width, height) {
  if (rgba.length < width * height * 4) {
    throw new Error(`encodePNG: need ${width * height * 4} bytes for ${width}x${height}, got ${rgba.length}`);
  }
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8;   // bit depth
  ihdr[9] = 6;   // colour type: truecolour + alpha
  // 10..12: compression 0, filter 0, interlace 0 -- already zero

  const raw = Buffer.alloc((width * 4 + 1) * height);
  for (let y = 0; y < height; y++) {
    const o = y * (width * 4 + 1);
    raw[o] = 0;  // filter type: none
    Buffer.from(rgba.buffer, rgba.byteOffset + y * width * 4, width * 4).copy(raw, o + 1);
  }

  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('IDAT', zlib.deflateSync(raw, { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}

/**
 * PNG -> RGBA8888. Same decoder the browser client uses, with Node's zlib swapped in for the
 * IDAT stream.
 * @param {Uint8Array} bytes a PNG file
 * @returns {{width:number, height:number, rgba:Uint8Array}}
 */
export function decodePNG(bytes) {
  return decodePNGShared(bytes, {
    inflate: (b) => new Uint8Array(zlib.inflateSync(Buffer.from(b.buffer, b.byteOffset, b.length))),
  });
}
