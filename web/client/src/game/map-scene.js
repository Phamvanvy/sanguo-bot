/*
 * MapScene — one loaded map, ready to draw: its background rasterised once, and its decor as
 * a list of animated objects.
 *
 * Background handling follows the original client (GameView.drawMapTile):
 *   - "precise" maps index a shared tile.pip through tile.ts, one tile per cell;
 *   - "blurred" maps (177 of the 179) store only a landform id per cell and REGENERATE their
 *     tiles by replaying java.util.Random from the map's seed, stacking up to three per cell.
 * Both are static for the life of the map, so the whole thing is drawn into one offscreen
 * canvas at load and blitted per frame. The largest map in this data set is 1504x1600 px,
 * which is a 9 MB canvas — well inside what a browser will hand out, and it turns thousands
 * of per-frame tile blits into one.
 *
 * Decor ("still map NPCs": trees, buildings, fences) stays a display list rather than being
 * baked in, because it animates and because the role layer has to interleave with units by y.
 */
import { createBlurMapBuffer, decodeBlurCell, BACKGROUND_PRECISE } from '../assets/game-map.js';
import { tileFlags } from '../assets/package-file.js';
import { PipAnimateSet } from '../assets/pip-animate-set.js';
import { createCanvas, imageToCanvas, disableSmoothing } from './graphics.js';
import { AnimatedSprites } from './sprite-set.js';

export class MapScene {
  /**
   * @param {import('./asset-source.js').AssetSource} source
   * @param {number} globalMapId the id the server sends (GOMAP_ALLOW_SERVER.mapId)
   */
  static async load(source, globalMapId) {
    const { pkg, localMapId, areaDir } = await source.packageForMap(globalMapId);
    const map = pkg.loadMap(localMapId);
    const scene = new MapScene(pkg, map, areaDir);
    scene.build();
    return scene;
  }

  constructor(pkg, map, areaDir = '') {
    this.pkg = pkg;
    this.map = map;
    this.areaDir = areaDir;
    this.background = null;
    /** @type {{ground:object[], role:object[], sky:object[]}} decor by paint layer */
    this.decor = { ground: [], role: [], sky: [] };
    /** @type {AnimatedSprites|null} the area's npc.anp, shared by all three layers */
    this.decorSprites = null;
    this.stats = { tiles: 0, missingTiles: 0, decor: 0, buildMs: 0 };
  }

  get width() { return this.map.width; }
  get height() { return this.map.height; }
  get name() { return this.map.name; }
  get id() { return this.map.id; }

  build() {
    const t0 = Date.now();
    this.background = this._renderBackground();
    this._loadDecor();
    this.stats.buildMs = Date.now() - t0;
  }

  _renderBackground() {
    const canvas = createCanvas(this.map.width, this.map.height);
    const ctx = canvas.getContext('2d');
    disableSmoothing(ctx);
    // Black under the tiles: a map's grid can be a cell short of its pixel size, and the
    // blurred generator legitimately leaves cells empty.
    ctx.fillStyle = '#000';
    ctx.fillRect(0, 0, this.map.width, this.map.height);
    if (this.map.backgroundType === BACKGROUND_PRECISE) this._drawPreciseTiles(ctx);
    else this._drawBlurredTiles(ctx);
    return canvas;
  }

  _drawPreciseTiles(ctx) {
    const image = this.pkg.loadTileImage();
    const { frames, info } = this.pkg.getTileInfo();
    const cache = new Map();
    const { cellWidth: cw, cellHeight: ch } = this.map;
    for (let row = 0; row < this.map.rows; row++) {
      for (let col = 0; col < this.map.cols; col++) {
        const cell = this.map.mapData[row][col];
        if (cell >= frames.length) { this.stats.missingTiles++; continue; }
        const trans = tileFlags(info[cell]).trans;
        const key = `${frames[cell]}:${trans}`;
        if (!cache.has(key)) cache.set(key, imageToCanvas(image.frameToRGBA(frames[cell], trans)));
        ctx.drawImage(cache.get(key), col * cw, row * ch);
        this.stats.tiles++;
      }
    }
  }

  _drawBlurredTiles(ctx) {
    const landforms = this.pkg.loadAllLandformImages();
    const buffer = createBlurMapBuffer(this.map, landforms);
    const cache = new Map();
    const { cellWidth: cw, cellHeight: ch } = this.map;
    for (let row = 0; row < this.map.rows; row++) {
      for (let col = 0; col < this.map.cols; col++) {
        for (const layer of decodeBlurCell(buffer[row][col])) {
          const lf = landforms[layer.landform];
          if (!lf || layer.frame < 0 || layer.frame >= lf.frameCount) { this.stats.missingTiles++; continue; }
          const key = `${layer.landform}:${layer.frame}:${layer.trans}`;
          if (!cache.has(key)) cache.set(key, imageToCanvas(lf.image.frameToRGBA(layer.frame, layer.trans)));
          ctx.drawImage(cache.get(key), col * cw, row * ch);
          this.stats.tiles++;
        }
      }
    }
  }

  _loadDecor() {
    const anp = this.pkg.getFile('/npc.anp');
    if (!anp) return;                      // a few areas have no decor at all
    try {
      this.decorSprites = new AnimatedSprites(PipAnimateSet.fromPackage(anp, this.pkg.codecs));
    } catch (e) {
      console.warn(`[scene] map ${this.map.id}: npc.anp unusable (${e.message})`);
      return;
    }
    const count = this.decorSprites.animateCount;
    const keep = (npcs) => npcs
      .filter((n) => n.animateId >= 0 && n.animateId < count)
      .map((n) => ({ x: n.x, y: n.y, animateId: n.animateId }))
      .sort((a, b) => a.y - b.y || a.x - b.x);   // nearer objects paint over farther ones
    this.decor = {
      ground: keep(this.map.groundNPCs),
      role: keep(this.map.roleNPCs),
      sky: keep(this.map.skyNPCs),
    };
    this.stats.decor = this.decor.ground.length + this.decor.role.length + this.decor.sky.length;
  }
}
