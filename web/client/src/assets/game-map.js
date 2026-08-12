/*
 * GameMap — reader for an area package's `<id>.m` entry (gzipped).
 *
 * Port of client/src/com/pip/engine/GameMap.java `load()`.
 *
 * Layout (big-endian, after gunzip):
 *   i8 localMapId, utf name, i16 width, i16 height   <- width/height are in PIXELS
 *   u8 exitCount, exitCount x { i16 id, i16 x, i16 y, i32 targetMap, utf targetMapName,
 *                               i16 targetX, i16 targetY }
 *   u8 layerCount (must be 4)
 *   background layer: i8 type
 *     type 0 ("precise"): rows of width/tileWidth bytes, each an index into tile.ts
 *     type 1 ("blurred"): i32 randomSeed, i8 baseLandform, then rows of
 *                         width/blurTileWidth bytes, each a landform id -- the actual tiles
 *                         are generated procedurally from that seed (GameMap.createBlurMapBuffer)
 *   3 x NPC layer: i8 type (must be 2), i8 (ignored), i16 count, count x { i16 animId, i16 x, i16 y }
 *     in order: ground, role, sky
 *
 * Note the map grid is a plain orthogonal grid drawn at x = col*tileWidth, y = row*tileHeight
 * (GameView.drawMapNoBuffer) -- tiles are non-square (16x8 here) but the layout is not isometric.
 */
import { JavaRandom } from './java-random.js';

export const BACKGROUND_PRECISE = 0;
export const BACKGROUND_BLURRED = 1;

export class GameMap {
  /** @param {import('./package-file.js').GamePackage} owner */
  constructor(owner) {
    this.owner = owner;
    this.id = 0;
    this.name = '';
    this.width = 0;   // pixels
    this.height = 0;  // pixels
    this.exits = [];
    this.backgroundType = BACKGROUND_PRECISE;
    /** @type {Uint8Array[]} one entry per grid row */
    this.mapData = [];
    this.randomSeed = 0;
    this.baseLandform = -1;
    this.groundNPCs = [];
    this.roleNPCs = [];
    this.skyNPCs = [];
  }

  /** Grid size in cells, and the pixel size of one cell. */
  get cols() { return this.mapData.length ? this.mapData[0].length : 0; }
  get rows() { return this.mapData.length; }
  get cellWidth() {
    return this.backgroundType === BACKGROUND_PRECISE ? this.owner.tileWidth : this.owner.blurTileWidth;
  }
  get cellHeight() {
    return this.backgroundType === BACKGROUND_PRECISE ? this.owner.tileHeight : this.owner.blurTileHeight;
  }

  /** @param {import('./data-in.js').DataIn} dis */
  load(dis) {
    this.id = dis.readByte();
    this.name = dis.readUTF();
    this.width = dis.readShort();
    this.height = dis.readShort();

    const exitCount = dis.readUnsignedByte();
    for (let i = 0; i < exitCount; i++) {
      this.exits.push({
        id: dis.readShort(),
        x: dis.readShort(),
        y: dis.readShort(),
        targetMap: dis.readInt(),
        targetMapName: dis.readUTF(),
        targetX: dis.readShort(),
        targetY: dis.readShort(),
      });
    }

    const layerCount = dis.readByte();
    if (layerCount !== 4) throw new Error(`GameMap: layerCount is ${layerCount}, expected 4`);

    const bgType = dis.readByte();
    if (bgType === BACKGROUND_PRECISE) {
      this.backgroundType = BACKGROUND_PRECISE;
      this._readGrid(dis, this.owner.tileWidth, this.owner.tileHeight);
    } else if (bgType === BACKGROUND_BLURRED) {
      this.backgroundType = BACKGROUND_BLURRED;
      this.randomSeed = dis.readInt();
      this.baseLandform = dis.readByte();
      this._readGrid(dis, this.owner.blurTileWidth, this.owner.blurTileHeight);
    } else {
      throw new Error(`GameMap: unknown background layer type ${bgType}`);
    }

    for (let layer = 0; layer < 3; layer++) {
      const type = dis.readByte();
      dis.skip(1);  // "is object layer" flag, fixed at 0/1/0 -- ignored by the client too
      if (type !== 2) throw new Error(`GameMap: NPC layer ${layer} has type ${type}, expected 2`);
      const npcs = readNPCList(dis);
      if (layer === 0) this.groundNPCs = npcs;
      else if (layer === 1) this.roleNPCs = npcs;
      else this.skyNPCs = npcs;
    }
  }

  _readGrid(dis, cw, ch) {
    const cols = Math.floor(this.width / cw);
    const rows = Math.floor(this.height / ch);
    this.mapData = [];
    for (let r = 0; r < rows; r++) this.mapData.push(dis.readBytes(cols).slice());
  }

  /** Cell value at a pixel position, or -1 outside the map. */
  cellAtPixel(px, py) {
    const col = Math.floor(px / this.cellWidth);
    const row = Math.floor(py / this.cellHeight);
    if (row < 0 || row >= this.rows || col < 0 || col >= this.cols) return -1;
    return this.mapData[row][col];
  }
}

/**
 * The client sorts NPCs by y then x so painting order matches depth; we keep the file order
 * and expose y so a renderer can sort when it needs to.
 */
function readNPCList(dis) {
  const count = dis.readShort();
  const out = [];
  for (let i = 0; i < count; i++) {
    out.push({ animateId: dis.readShort(), x: dis.readShort(), y: dis.readShort() });
  }
  return out;
}

/* ---------- blurred (procedural) background ---------- */

/**
 * Regenerate the visible tiles of a blurred map. Port of GameMap.createBlurMapBuffer.
 *
 * Each output cell is an int packing up to 3 stacked tiles ("bottom, middle, top"), 10/11/11
 * bits wide; `decodeBlurCell` unpacks it. The PRNG is re-seeded per landform and advanced for
 * every cell of the grid, so the loops must run over the whole grid in this order even where
 * nothing is drawn -- skipping a cell would desync every later cell.
 *
 * @param {GameMap} map a map with backgroundType === BACKGROUND_BLURRED
 * @param {import('./landform-image.js').LandformImage[]} landforms index = landform id
 * @returns {Int32Array[]} one row per grid row
 */
export function createBlurMapBuffer(map, landforms) {
  const rows = map.rows;
  const cols = map.cols;
  const buffer = [];
  for (let i = 0; i < rows; i++) buffer.push(new Int32Array(cols));

  // The base landform (if any) fills every cell at 100% coverage first.
  if (map.baseLandform !== -1) {
    const rand = new JavaRandom(map.randomSeed);
    const image = landforms[map.baseLandform];
    image.generateSearchTable();
    for (let i = 0; i < rows; i++) {
      for (let j = 0; j < cols; j++) {
        const [frame, trans] = image.getTile(rand, 0x0f);
        if (frame !== -1) buffer[i][j] = makeLayerBits(map.baseLandform, frame, trans) << 22;
      }
    }
  }

  // Then every other landform is painted on top, in landform-id order.
  for (let lf = 0; lf < landforms.length; lf++) {
    if (lf === map.baseLandform) continue;
    const rand = new JavaRandom(map.randomSeed);
    const corners = makeLayer(map.mapData, lf);
    const image = landforms[lf];
    image.generateSearchTable();
    for (let i = 0; i < rows; i++) {
      for (let j = 0; j < cols; j++) {
        const gridType = corners[i + 1][j + 1];
        const [frame, trans] = image.getTile(rand, gridType);
        if (frame === -1) continue;
        const bits = makeLayerBits(lf, frame, trans);
        // A fully-covering tile hides whatever is underneath, so it replaces the stack.
        if (gridType === 0x0f) buffer[i][j] = bits << 22;
        else buffer[i][j] = mergeGridData(buffer[i][j], bits);
      }
    }
  }

  return buffer;
}

/** (lfid << 7) | (trans << 5) | (frame + 1) -- frame 0 must stay non-zero, hence the +1. */
function makeLayerBits(lfid, frame, trans) {
  return (lfid << 7) | (trans << 5) | (frame + 1);
}

/** Push a new tile onto a cell's 3-deep stack (bottom layer first). */
function mergeGridData(cell, newLayer) {
  const layer1 = (cell >> 22) & 0x3ff;
  if (layer1 === 0) return newLayer << 22;
  const layer2 = (cell >> 11) & 0x7ff;
  if (layer2 === 0) return (layer1 << 22) | (newLayer << 11);
  return (layer1 << 22) | (layer2 << 11) | newLayer;
}

/**
 * Unpack a cell into the tiles to draw, bottom first. Port of GameView.drawMapTile case 1.
 * Note the bottom layer only keeps 3 bits of landform id (its 11-bit field is shifted to bit
 * 22 and so loses its top bit off the end of the int) -- that asymmetry is in the original.
 * @returns {{landform:number, frame:number, trans:number}[]}
 */
export function decodeBlurCell(cc) {
  const out = [];
  if ((cc & 0xffc00000) === 0) return out;
  out.push({ landform: (cc >> 29) & 0x07, trans: (cc >> 27) & 0x03, frame: ((cc >> 22) & 0x1f) - 1 });
  if ((cc & 0x3ff800) === 0) return out;
  out.push({ landform: (cc >> 18) & 0x0f, trans: (cc >> 16) & 0x03, frame: ((cc >> 11) & 0x1f) - 1 });
  if ((cc & 0x7ff) === 0) return out;
  out.push({ landform: (cc >> 7) & 0x0f, trans: (cc >> 5) & 0x03, frame: (cc & 0x1f) - 1 });
  return out;
}

/**
 * For one landform, build the per-cell 4-corner occupancy bits. Port of GameMap.makeLayer:
 * every cell of that landform stamps a 3x3 neighbourhood, so a corner bit is set when the
 * adjacent cell belongs to the landform. The result is (rows+2) x (cols+2); cell (i,j) reads
 * at [i+1][j+1].
 */
export function makeLayer(mapData, lfid) {
  const rows = mapData.length;
  const cols = mapData[0].length;
  const out = [];
  for (let i = 0; i < rows + 2; i++) out.push(new Uint8Array(cols + 2));
  const sg = makeRectangle(3, 3);
  for (let i = 0; i < rows; i++) {
    for (let j = 0; j < cols; j++) {
      if (mapData[i][j] !== lfid) continue;
      for (let dy = 0; dy < 3; dy++) {
        for (let dx = 0; dx < 3; dx++) out[i + dy][j + dx] |= sg[dy][dx];
      }
    }
  }
  return out;
}

/** Corner-bit stamp for a w x h block of one landform. Port of GameMap.makeRectangle. */
export function makeRectangle(w, h) {
  const ret = [];
  for (let i = 0; i < h; i++) {
    const row = new Uint8Array(w);
    for (let j = 0; j < w; j++) {
      if (i === 0) row[j] = j === 0 ? 1 : j === w - 1 ? 2 : 3;
      else if (i === h - 1) row[j] = j === 0 ? 4 : j === w - 1 ? 8 : 12;
      else row[j] = j === 0 ? 5 : j === w - 1 ? 10 : 15;
    }
    ret.push(row);
  }
  return ret;
}
