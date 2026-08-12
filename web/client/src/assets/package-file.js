/*
 * PackageFile / GamePackage — readers for the "SANGUOPKG" container and for an area's
 * client.pkg (map + tileset bundle).
 *
 * Port of client/src/com/pip/engine/PackageFile.java and GamePackage.java.
 *
 * Container layout (big-endian):
 *   utf name ("SANGUOPKG"), i32 version, i16 fileCount,
 *   fileCount x utf fileName, then fileCount x { i32 length, length bytes }
 *
 * An area's client.pkg holds: "0.stg" (area header), "<mapId>.m" (gzipped maps),
 * "tile.pip" + "tile.ts" (the precise tileset and its per-cell table),
 * "l<n>.ldf" + "l<n>.ts" (blurred-landform tilesets), "npc.anp"/"npc.col" (map decor).
 */
import { DataIn } from './data-in.js';
import { PipImage } from './pip-image.js';
import { GameMap } from './game-map.js';
import { LandformImage } from './landform-image.js';

export class PackageFile {
  /** @param {Uint8Array} bytes */
  constructor(bytes) {
    const dis = new DataIn(bytes);
    this.name = dis.readUTF();
    this.version = dis.readInt();
    const fileCount = dis.readShort();
    this.fileNames = [];
    for (let i = 0; i < fileCount; i++) this.fileNames.push(dis.readUTF());
    this.fileContents = [];
    for (let i = 0; i < fileCount; i++) this.fileContents.push(dis.readBytes(dis.readInt()));
    /** @type {Map<string, number>} keys carry the leading "/" the Java code uses */
    this.nameIndexMap = new Map(this.fileNames.map((n, i) => ['/' + n, i]));
  }

  /** @param {string} name e.g. "/tile.pip" @returns {Uint8Array|null} */
  getFile(name) {
    const i = this.nameIndexMap.get(name);
    return i === undefined ? null : this.fileContents[i];
  }

  /** Like getFile but throws instead of returning null, for required entries. */
  requireFile(name) {
    const f = this.getFile(name);
    if (!f) throw new Error(`PackageFile "${this.name}": no entry ${name} (has: ${this.fileNames.join(', ')})`);
    return f;
  }
}

export class GamePackage extends PackageFile {
  /**
   * @param {Uint8Array} bytes an area's client.pkg
   * @param {{inflate?: (b: Uint8Array) => Uint8Array,
   *          decodePNG?: (b: Uint8Array) => {width:number, height:number, rgba:Uint8Array}}} [opts]
   *   gzip and PNG decoding are injected so this module works in both Node and the browser.
   */
  constructor(bytes, opts = {}) {
    super(bytes);
    this.codecs = { inflate: opts.inflate, decodePNG: opts.decodePNG };
    this.inflate = opts.inflate;

    const stg = new DataIn(this.requireFile('/0.stg'));
    this.tileWidth = stg.readShort();
    this.tileHeight = stg.readShort();
    this.blurTileWidth = stg.readShort();
    this.blurTileHeight = stg.readShort();
    this.areaID = stg.readShort() & 0xffff;
    this.title = stg.readUTF();

    this.landformCount = this.fileNames.filter((n) => n.endsWith('.ldf')).length;
    /** map ids present in this package, as they appear inside the file (0-based per area) */
    this.localMapIds = this.fileNames
      .filter((n) => /^\d+\.m$/.test(n))
      .map((n) => parseInt(n, 10))
      .sort((a, b) => a - b);
  }

  /** Global map id as the server uses it: (areaID << 4) | localMapId (GamePackage.loadMap). */
  globalMapId(localMapId) { return (this.areaID << 4) | localMapId; }

  /** @param {number} globalId e.g. 1395 @returns {number} the local id inside this package */
  static localMapId(globalId) { return globalId & 0x0f; }

  /** The precise tileset image (`tile.pip`). */
  loadTileImage() {
    return new PipImage(this.requireFile('/tile.pip'), this.codecs);
  }

  /**
   * The precise tile table (`tile.ts`): i16 count, then count x { i8 frame, i8 info }.
   * `frame` indexes tile.pip; `info` packs, high bit to low: 2 bits `trans` (mirror code),
   * 5 bits minimap colour index, 1 bit passability. Cells in a map's background layer are
   * indices into this table, NOT frame numbers -- see GameView.drawMapTile.
   * @returns {{frames: Uint8Array, info: Uint8Array}}
   */
  getTileInfo() { return parseTileInfo(this.requireFile('/tile.ts')); }

  /** One blurred-landform tileset (`l<id>.ldf`). */
  loadLandformImage(lid) {
    return new LandformImage(this.requireFile(`/l${lid}.ldf`), this.codecs);
  }

  /** All landform tilesets, indexed by landform id -- what createBlurMapBuffer expects. */
  loadAllLandformImages() {
    const out = [];
    for (let i = 0; i < this.landformCount; i++) out.push(this.loadLandformImage(i));
    return out;
  }

  /** A landform's own tile table (`l<id>.ts`); only the info byte is used by the client. */
  loadLandformTileInfo(lid) { return parseTileInfo(this.requireFile(`/l${lid}.ts`)).info; }

  /** Decompress and parse one map out of this package. */
  loadMap(localMapId) {
    const gz = this.requireFile(`/${localMapId}.m`);
    if (!this.inflate) throw new Error('GamePackage: maps are gzipped but no inflate() was provided');
    const map = new GameMap(this);
    map.load(new DataIn(this.inflate(gz)));
    map.id |= this.areaID << 4;
    return map;
  }
}

/** Shared by tile.ts and l<n>.ts (GamePackage.parseTileInfo). */
export function parseTileInfo(bytes) {
  const dis = new DataIn(bytes);
  const count = dis.readShort();
  const frames = new Uint8Array(count);
  const info = new Uint8Array(count);
  for (let i = 0; i < count; i++) {
    frames[i] = dis.readUnsignedByte();
    info[i] = dis.readUnsignedByte();
  }
  return { frames, info };
}

/** Decode one tile-table `info` byte. */
export function tileFlags(infoByte) {
  return {
    trans: (infoByte >> 6) & 0x03,       // GameView.drawMapTile
    thumbColor: (infoByte >> 1) & 0x1f,  // GameView minimap palette index
    passable: (infoByte & 0x01) === 1,
  };
}
