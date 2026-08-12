/*
 * PipAnimateSet — port of client/src/com/pip/image/PipAnimateSet.java.
 *
 * An animate set composes animations out of *pieces* of several .pip images. Three levels:
 *
 *   animation -> list of (frame, dx, dy, delay)
 *   frame     -> list of pieces (imageId, pipFrame, trans, dx, dy)
 *   piece     -> one frame of one source .pip
 *
 * Both list-of-lists are stored flattened: a per-entry `pos`/`len` index into one flat int
 * array (PipAnimateSet.readIntArray).
 *
 * Definition file layout (big-endian):
 *   i16 frameCount   -- the TOP 2 BITS are the format version, so mask with 0x3FFF
 *   frameCount x { u8 pieceCount, pieceCount x i32 piece }
 *   u8 animateCount
 *   animateCount x { u8 frameCount, frameCount x i32 animation step }
 *   u8 imageNameCount, imageNameCount x utf name
 *
 * The bit layout of a piece int depends on the version (v0 packs 9-bit offsets, v1 packs 8-bit
 * offsets but 5 bits of image id); v2 exists only on large-screen builds and is rejected here
 * rather than guessed at.
 */
import { DataIn } from './data-in.js';
import { PackageFile } from './package-file.js';
import { PipImage } from './pip-image.js';

export class PipAnimateSet {
  /**
   * @param {import('./pip-image.js').PipImage[]} images source images, index = imageId
   * @param {Uint8Array} def the definition file (entry 0 of the .anp package)
   */
  constructor(images, def) {
    this.images = images;
    this.version = (def[0] >> 6) & 0x03;

    const dis = new DataIn(def);
    const frameCount = dis.readShort() & 0x3fff;
    ({ data: this.frameData, pos: this.framePos, len: this.frameLen } = readIntArray(dis, frameCount));
    const animateCount = dis.readUnsignedByte();
    ({ data: this.animateData, pos: this.animatePos, len: this.animateLen } = readIntArray(dis, animateCount));

    this.imageNames = [];
    const nameCount = dis.readUnsignedByte();
    for (let i = 0; i < nameCount; i++) this.imageNames.push(dis.readUTF());
  }

  /**
   * Parse an `.anp` package: entry 0 is the definition, entries 1..n are the source .pip files
   * (GamePackage.parseAnimatesPackage).
   * @param {Uint8Array} bytes
   * @param {{inflate?: Function, decodePNG?: Function}} [codecs]
   */
  static fromPackage(bytes, codecs = {}) {
    const pkg = new PackageFile(bytes);
    const images = pkg.fileContents.slice(1).map((b) => new PipImage(b, codecs));
    return new PipAnimateSet(images, pkg.fileContents[0]);
  }

  get animateCount() { return this.animatePos.length; }
  get frameCount() { return this.framePos.length; }

  /** Number of animation steps; 1 means "a still image". */
  animateLength(index) { return this.animateLen[index] & 0xff; }

  /** Decode the pieces that make up one composite frame. */
  framePieces(frame) {
    const start = this.framePos[frame];
    const len = this.frameLen[frame] & 0xff;
    const out = [];
    for (let i = start; i < start + len; i++) {
      const pd = this.frameData[i];
      if (this.version === 0) {
        out.push({
          imageId: (pd >> 29) & 0x07,
          pipFrame: (pd >> 21) & 0xff,
          trans: (pd >> 18) & 0x07,
          dx: signed((pd >> 9) & 0x1ff, 9),
          dy: signed(pd & 0x1ff, 9),
        });
      } else if (this.version === 1) {
        out.push({
          imageId: (pd >> 27) & 0x1f,
          pipFrame: (pd >> 19) & 0xff,
          trans: (pd >> 16) & 0x07,
          dx: signed((pd >> 8) & 0xff, 8),
          dy: signed(pd & 0xff, 8),
        });
      } else {
        // v2 stores dy in a parallel short[] that only large-screen builds read; the files in
        // this data set are v0/v1, and inventing a layout would silently corrupt positions.
        throw new Error(`PipAnimateSet: piece format version ${this.version} is not supported`);
      }
    }
    return out;
  }

  /**
   * The steps of one animation, in play order: a composite frame, an offset, and how many
   * ticks it holds for.
   * @returns {{frame:number, dx:number, dy:number, delay:number}[]}
   */
  animateSteps(index) {
    const start = this.animatePos[index];
    const len = this.animateLen[index] & 0xff;
    const out = [];
    for (let i = start; i < start + len; i++) {
      const a = this.animateData[i];
      out.push({
        frame: (a >> 24) & 0xff,
        dx: signed((a >> 14) & 0x3ff, 10),
        dy: signed((a >> 4) & 0x3ff, 10),
        delay: a & 0x0f,
      });
    }
    return out;
  }

  /** Total duration of an animation in ticks. */
  animateDuration(index) {
    return this.animateSteps(index).reduce((s, st) => s + st.delay, 0);
  }

  /**
   * The step of an animation playing at `time` ticks, or null when time is past its end.
   * Port of the scan inside PipAnimateSet.drawAnimateFrame.
   * @returns {{frame:number, dx:number, dy:number, delay:number}|null}
   */
  animateFrameAt(index, time = 0) {
    let tick = 0;
    for (const step of this.animateSteps(index)) {
      if (time >= tick && time < tick + step.delay) return step;
      tick += step.delay;
    }
    return null;
  }

  /**
   * Flatten one animation step into ready-to-blit pieces at offsets relative to the unit's
   * anchor (which sits at its feet, so offsets are usually negative).
   * Returns [] when the animation has no step at that time.
   * @param {number} index animation id
   * @param {number|{frame:number,dx:number,dy:number}} timeOrStep tick, or a step from animateSteps()
   * @returns {{image: PipImage, pipFrame:number, trans:number, x:number, y:number}[]}
   */
  resolveAnimate(index, timeOrStep = 0) {
    const step = typeof timeOrStep === 'object' ? timeOrStep : this.animateFrameAt(index, timeOrStep);
    if (!step) return [];
    return this.framePieces(step.frame).map((p) => ({
      image: this.images[p.imageId],
      pipFrame: p.pipFrame,
      trans: p.trans,
      x: step.dx + p.dx,
      y: step.dy + p.dy,
    }));
  }
}

/** `bits`-wide two's-complement value that was masked into a positive int. */
function signed(v, bits) {
  const sign = 1 << (bits - 1);
  return v >= sign ? v - (1 << bits) : v;
}

/** Read a list-of-lists of int32 into one flat array plus pos/len indexes. */
function readIntArray(dis, count) {
  const pos = new Int32Array(count);
  const len = new Uint8Array(count);
  const chunks = [];
  let total = 0;
  for (let i = 0; i < count; i++) {
    const n = dis.readUnsignedByte();
    pos[i] = total;
    len[i] = n;
    const chunk = new Int32Array(n);
    for (let j = 0; j < n; j++) chunk[j] = dis.readInt();
    chunks.push(chunk);
    total += n;
  }
  const data = new Int32Array(total);
  let o = 0;
  for (const c of chunks) { data.set(c, o); o += c.length; }
  return { data, pos, len };
}
