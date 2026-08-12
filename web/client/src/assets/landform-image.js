/*
 * LandformImage — port of client/src/com/pip/engine/LandformImage.java.
 *
 * A "blurred" map (background layer type 1) stores only a landform id per cell plus one
 * random seed. The visible tiles are generated: for each cell, the 4 corner-occupancy bits of
 * that landform pick a tile *shape* (100% / 75% / 50% / 25% coverage) and a rotation, and a
 * weighted random draw picks one concrete tile of that shape.
 *
 * A `.ldf` file is an ordinary .pip image with a table appended to the very end:
 * frameCount x { i16 tileType, i16 priority }. The frame count comes from the image itself,
 * so the table offset is `data.length - 4 * frameCount`.
 */
import { DataIn } from './data-in.js';
import { PipImage } from './pip-image.js';

export const TILE_100 = 0;   // fills the whole cell
export const TILE_75 = 1;    // fills 3 of 4 quarters
export const TILE_50H = 2;   // fills the bottom half
export const TILE_50V = 3;   // fills the right half
export const TILE_50S = 4;   // fills two opposite corners
export const TILE_25 = 5;    // fills one quarter

/**
 * gridType (4 corner bits: bit0 = top-left, bit1 = top-right, bit2 = bottom-left,
 * bit3 = bottom-right) -> [tile shape, rotation]. -1 means "draw nothing".
 */
export const TYPE_MAP = [
  [-1, 0],        // 0000
  [TILE_25, 0],   // 0001
  [TILE_25, 2],   // 0010
  [TILE_50H, 0],  // 0011
  [TILE_25, 1],   // 0100
  [TILE_50V, 0],  // 0101
  [TILE_50S, 2],  // 0110
  [TILE_75, 0],   // 0111
  [TILE_25, 3],   // 1000
  [TILE_50S, 0],  // 1001
  [TILE_50V, 2],  // 1010
  [TILE_75, 2],   // 1011
  [TILE_50H, 1],  // 1100
  [TILE_75, 1],   // 1101
  [TILE_75, 3],   // 1110
  [TILE_100, 0],  // 1111
];

export class LandformImage {
  /**
   * @param {Uint8Array} data raw .ldf bytes
   * @param {{inflate?: (b: Uint8Array) => Uint8Array}} [opts]
   */
  constructor(data, opts = {}) {
    this.image = new PipImage(data, opts);
    // ImageSet.getFrameCount(): merge mode counts frame rects, paletted mode counts
    // blocks x palettes.
    const frameCount = this.image.mergeMode
      ? this.image.mergeFrames.length
      : this.image.blocks.length * this.image.palettes.length;
    this.frameCount = frameCount;
    this.tileTypes = new Int16Array(frameCount);
    this.tilePriority = new Int16Array(frameCount);
    const dis = new DataIn(data, data.length - 4 * frameCount);
    for (let i = 0; i < frameCount; i++) {
      this.tileTypes[i] = dis.readShort();
      this.tilePriority[i] = dis.readShort();
    }
    /** @type {number[][]|null} shape -> candidate frame ids */
    this.frameSearchTable = null;
  }

  generateSearchTable() {
    this.frameSearchTable = [];
    for (let t = 0; t < 6; t++) {
      const candidates = [];
      for (let i = 0; i < this.frameCount; i++) if (this.tileTypes[i] === t) candidates.push(i);
      this.frameSearchTable.push(candidates);
    }
  }

  /**
   * Weighted random pick among the frames of one shape.
   * Callers must have run generateSearchTable(). Returns -1 for "nothing to draw".
   * Every branch that returns early WITHOUT touching `rand` matters: the PRNG call sequence
   * is part of the map's identity.
   */
  randomChooseTile(rand, tileType) {
    if (tileType === -1) return -1;
    let candidates = this.frameSearchTable[tileType];
    if (candidates.length === 0) candidates = this.frameSearchTable[TILE_100];
    let totalPriority = 0;
    for (const c of candidates) totalPriority += this.tilePriority[c] & 0xffff;
    if (totalPriority === 0) return -1;
    let point = rand.nextInt(totalPriority);
    for (const c of candidates) {
      point -= this.tilePriority[c] & 0xffff;
      if (point <= 0) return c;
    }
    return -1;
  }

  /**
   * @param {import('./java-random.js').JavaRandom} rand
   * @param {number} gridType 0..15 corner-occupancy bits
   * @returns {[number, number]} [frame id or -1, rotation 0..3]
   */
  getTile(rand, gridType) {
    return [this.randomChooseTile(rand, TYPE_MAP[gridType][0]), TYPE_MAP[gridType][1]];
  }
}
