/*
 * PipImage — decoder for the game's `.pip` sprite/tile format.
 *
 * Port of client/src/com/pip/image/PipImage.java (`load`, `readPalette`, `readFrame`, `make`).
 *
 * A .pip is a *block* image: N palettes x M frame blocks. Drawable frame `id` means
 * "block id%M drawn with palette floor(id/M)", so a single file holds every recolour of a
 * sprite (the game uses this for faction colours). Pixels are stored as palette indices,
 * not RGB — `frameToRGBA()` is where the palette is applied.
 *
 * File layout (big-endian throughout):
 *   3 bytes magic:
 *     "PIP" 8-bit indices, fixed palette      "PIE" 8-bit indices, replaceable palette
 *     "PJP" 16-bit indices, fixed palette     "PJE" 16-bit indices, replaceable palette
 *     "PIM" merge mode: frames live inside embedded PNGs (see below)
 *   u8  paletteCount
 *   paletteCount x { i32 entryCount, 4 bytes "PLTE", entryCount x i32 ARGB }
 *   u8  blockCount
 *   blockCount x {
 *     i32 dataLen+6, 4 bytes chunk id ("DATA" = gzipped, "DUNZ" = stored),
 *     i8 flip, i8 frameIdx, i16 width, i16 height,
 *     i8 collisionFlag, [i32 collisionBox if flag == 1],
 *     dataLen bytes of palette indices (row-major, width*height*bytesPerPixel)
 *   }
 *
 * gzip is injected (`{ inflate }`) rather than imported: Node has zlib, the browser has
 * DecompressionStream, and this module must stay usable from both.
 */
import { DataIn } from './data-in.js';

const PLTE = 'PLTE';

/**
 * MIDP 2.0 Sprite transform codes. The transform is "mirror horizontally (if the code implies
 * it), then rotate clockwise": codes 0..3 keep the frame's width and height, codes 4..7 swap
 * them -- which is why the Java draw code branches on `trans < 4` when applying anchors.
 * Map tile data only ever stores 0..3 (2 bits), but sprite pieces use the full range.
 */
export const TRANS_NONE = 0;
export const TRANS_MIRROR_ROT180 = 1;  // == flip vertically
export const TRANS_MIRROR = 2;         // == flip horizontally
export const TRANS_ROT180 = 3;
export const TRANS_MIRROR_ROT270 = 4;
export const TRANS_ROT90 = 5;
export const TRANS_ROT270 = 6;
export const TRANS_MIRROR_ROT90 = 7;

/** trans code -> { mirror first?, then rotate clockwise by this many degrees }. */
const TRANS_TABLE = [
  { mirror: false, rot: 0 },    // 0 TRANS_NONE
  { mirror: true, rot: 180 },   // 1 TRANS_MIRROR_ROT180
  { mirror: true, rot: 0 },     // 2 TRANS_MIRROR
  { mirror: false, rot: 180 },  // 3 TRANS_ROT180
  { mirror: true, rot: 270 },   // 4 TRANS_MIRROR_ROT270
  { mirror: false, rot: 90 },   // 5 TRANS_ROT90
  { mirror: false, rot: 270 },  // 6 TRANS_ROT270
  { mirror: true, rot: 90 },    // 7 TRANS_MIRROR_ROT90
];

export class PipImage {
  /**
   * @param {Uint8Array} bytes raw .pip file
   * @param {{inflate?: (b: Uint8Array) => Uint8Array,
   *          decodePNG?: (b: Uint8Array) => {width:number, height:number, rgba:Uint8Array}}} [opts]
   */
  constructor(bytes, opts = {}) {
    this.inflate = opts.inflate;
    this.decodePNG = opts.decodePNG;
    /** @type {string} the 3-byte magic */
    this.magic = String.fromCharCode(bytes[0], bytes[1], bytes[2]);
    /** true when the palette may be swapped at runtime (magic ends in 'E') */
    this.canChangeColor = this.magic[2] === 'E';
    /** merge mode keeps pixels in embedded PNGs instead of palette indices */
    this.mergeMode = this.magic[2] === 'M';
    /** 16-bit palette indices when the magic's 2nd byte is 'J' */
    this.bytesPerPixel = this.magic[1] === 'J' ? 2 : 1;

    /** @type {Int32Array[]} ARGB palettes */
    this.palettes = [];
    /** @type {{flip:number, frameIdx:number, width:number, height:number, collision:number, data:Uint8Array}[]} */
    this.blocks = [];
    /** merge mode only: reconstructed PNG files + the frame rectangles inside them */
    this.mergePNGs = [];
    this.mergeFrames = [];

    const dis = new DataIn(bytes, 3);
    if (this.mergeMode) this._loadMerge(dis);
    else this._loadPaletted(dis);
  }

  _loadPaletted(dis) {
    const paletteCount = dis.readUnsignedByte();
    for (let i = 0; i < paletteCount; i++) {
      const entryCount = dis.readInt();
      const tag = String.fromCharCode(...dis.readBytes(4));
      if (tag !== PLTE) throw new Error(`PipImage: palette ${i} has tag "${tag}", expected ${PLTE}`);
      const pal = new Int32Array(entryCount);
      for (let e = 0; e < entryCount; e++) pal[e] = dis.readInt();
      this.palettes.push(pal);
    }

    const blockCount = dis.readUnsignedByte();
    for (let i = 0; i < blockCount; i++) {
      // The i32 counts the pixel payload plus the 6 bytes of flip/frame/width/height that
      // follow the chunk id -- the chunk id itself and the collision bytes are outside it.
      const dataLen = dis.readInt() - 6;
      const chunk = String.fromCharCode(...dis.readBytes(4));
      const flip = dis.readByte();
      const frameIdx = dis.readByte();
      const width = dis.readShort();
      const height = dis.readShort();
      let collision = 0;
      if (dis.readByte() === 1) collision = dis.readInt();
      if (dataLen < 0) throw new Error(`PipImage: block ${i} has negative data length ${dataLen}`);
      let data = dis.readBytes(dataLen);
      if (chunk[1] === 'A') {           // "DATA" = gzipped, "DUNZ" = stored
        if (!this.inflate) throw new Error('PipImage: block is gzipped but no inflate() was provided');
        data = this.inflate(data);
      }
      const expected = width * height * this.bytesPerPixel;
      if (data.length < expected) {
        throw new Error(`PipImage: block ${i} is ${width}x${height} (${expected} bytes) but only ${data.length} bytes decoded`);
      }
      this.blocks.push({ flip, frameIdx, width, height, collision, data });
    }
  }

  /*
   * Merge mode ("PIM"): one shared PLTE/tRNS blob is stored once and spliced into every
   * embedded PNG at byte 33 (right after the 33-byte signature+IHDR prefix), so the frames
   * come out as ordinary PNG files. Frame rects come from a gzipped table up front.
   */
  _loadMerge(dis) {
    const tableLen = dis.readUnsignedShort();
    const gz = dis.readBytes(tableLen);
    if (!this.inflate) throw new Error('PipImage: merge-mode frame table is gzipped but no inflate() was provided');
    const fdis = new DataIn(this.inflate(gz));
    const frameCount = fdis.readUnsignedByte();
    for (let i = 0; i < frameCount; i++) {
      const x = fdis.readUnsignedShort();
      // The top 2 bits of the x coordinate are really the image index -- a merge-mode .pip may
      // hold up to 4 sheets (ImageSet.drawFrame: `iid = (fx >> 14) & 3; fx &= 0x3FFF`).
      this.mergeFrames.push({
        image: (x >> 14) & 0x03,
        x: x & 0x3fff,
        y: fdis.readUnsignedShort(),
        width: fdis.readUnsignedByte(),
        height: fdis.readUnsignedByte(),
      });
    }

    const imgCount = dis.readUnsignedByte();
    const palette = dis.readBytes(dis.readUnsignedShort());
    for (let i = 0; i < imgCount; i++) {
      const dataLen = dis.readUnsignedShort();
      const png = new Uint8Array(palette.length + dataLen);
      png.set(dis.readBytes(33), 0);                       // PNG signature + IHDR
      png.set(palette, 33);                                // shared palette chunk(s)
      png.set(dis.readBytes(dataLen - 33), 33 + palette.length);
      this.mergePNGs.push(png);
    }
  }

  /** Number of image blocks (frames sharing one palette). */
  get blockCount() { return this.mergeMode ? this.mergeFrames.length : this.blocks.length; }
  get paletteCount() { return this.palettes.length; }
  /** Total drawable frame ids = blocks x palettes (PipImage.getFrameCount). */
  get frameCount() { return this.blockCount * Math.max(1, this.paletteCount); }

  getWidth(frame) { return this._block(frame).width; }
  getHeight(frame) { return this._block(frame).height; }

  _block(frame) {
    const list = this.mergeMode ? this.mergeFrames : this.blocks;
    return list[frame % list.length];
  }

  /**
   * Decode one drawable frame to RGBA8888 (PipImage.make + Image.createRGBImage(.., true)).
   * @param {number} frame drawable frame id: block = frame % blockCount, palette = frame / blockCount
   * @param {number} [trans] 0..3 mirror/rotate code applied to the output
   * @returns {{width:number, height:number, rgba:Uint8Array}}
   */
  frameToRGBA(frame, trans = TRANS_NONE) {
    if (this.mergeMode) return this._mergeFrameToRGBA(frame, trans);
    const blockId = frame % this.blocks.length;
    const palId = Math.floor(frame / this.blocks.length);
    const pal = this.palettes[palId];
    if (!pal) throw new Error(`PipImage: frame ${frame} needs palette ${palId}, file has ${this.palettes.length}`);

    const { width: w, height: h, data } = this.blocks[blockId];
    const rgba = new Uint8Array(w * h * 4);
    for (let i = 0; i < w * h; i++) {
      const idx = this.bytesPerPixel === 1
        ? data[i]
        : ((data[i * 2] & 0xff) << 8) | (data[i * 2 + 1] & 0xff);
      const argb = pal[idx] | 0;
      const o = i * 4;
      rgba[o] = (argb >>> 16) & 0xff;
      rgba[o + 1] = (argb >>> 8) & 0xff;
      rgba[o + 2] = argb & 0xff;
      rgba[o + 3] = (argb >>> 24) & 0xff;
    }
    return trans === TRANS_NONE
      ? { width: w, height: h, rgba }
      : transformRGBA(rgba, w, h, trans);
  }

  /** Crop one merge-mode frame out of its embedded PNG sheet. Sheets are decoded once. */
  _mergeFrameToRGBA(frame, trans) {
    if (!this.decodePNG) {
      throw new Error('PipImage: merge-mode frames are embedded PNGs; pass a decodePNG() option '
        + '(or read mergePNGs/mergeFrames directly, e.g. via createImageBitmap in a browser)');
    }
    const f = this.mergeFrames[frame % this.mergeFrames.length];
    if (!this._sheets) this._sheets = [];
    if (!this._sheets[f.image]) this._sheets[f.image] = this.decodePNG(this.mergePNGs[f.image]);
    const sheet = this._sheets[f.image];
    const rgba = new Uint8Array(f.width * f.height * 4);
    for (let y = 0; y < f.height; y++) {
      const src = ((f.y + y) * sheet.width + f.x) * 4;
      rgba.set(sheet.rgba.subarray(src, src + f.width * 4), y * f.width * 4);
    }
    return trans === TRANS_NONE
      ? { width: f.width, height: f.height, rgba }
      : transformRGBA(rgba, f.width, f.height, trans);
  }
}

/**
 * Apply a MIDP `trans` code 0..7 to an RGBA buffer. Rotations by 90/270 swap the returned
 * width and height, so callers must read them back rather than assume the input size.
 * Implemented as an inverse mapping (walk the destination, fetch the source) so no
 * intermediate buffer is needed for the mirror-then-rotate composition.
 */
export function transformRGBA(rgba, w, h, trans) {
  if (trans === TRANS_NONE) return { width: w, height: h, rgba };
  const spec = TRANS_TABLE[trans];
  if (!spec) throw new Error(`transformRGBA: unsupported trans ${trans} (only 0..7)`);
  const { mirror, rot } = spec;
  const swap = rot === 90 || rot === 270;
  const dw = swap ? h : w;
  const dh = swap ? w : h;
  const out = new Uint8Array(dw * dh * 4);
  for (let y = 0; y < dh; y++) {
    for (let x = 0; x < dw; x++) {
      // Undo the rotation to land in the mirrored image...
      let mx, my;
      if (rot === 0) { mx = x; my = y; }
      else if (rot === 90) { mx = y; my = h - 1 - x; }
      else if (rot === 180) { mx = w - 1 - x; my = h - 1 - y; }
      else { mx = w - 1 - y; my = x; }   // 270
      // ...then undo the mirror to land in the source.
      const sx = mirror ? w - 1 - mx : mx;
      const src = (my * w + sx) * 4;
      const dst = (y * dw + x) * 4;
      out[dst] = rgba[src];
      out[dst + 1] = rgba[src + 1];
      out[dst + 2] = rgba[src + 2];
      out[dst + 3] = rgba[src + 3];
    }
  }
  return { width: dw, height: dh, rgba: out };
}
