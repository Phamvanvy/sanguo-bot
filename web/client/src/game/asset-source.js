/*
 * AssetSource — fetches the original game files and hands back decoded objects.
 *
 * Nothing is pre-converted: the browser downloads the same `client.pkg` / `.ctn` / `.pip`
 * files the 2011 server ships (the bridge serves them read-only under /data/) and decodes them
 * with the ported decoders in ../assets. An area package is ~50 KB, a character set ~200 KB,
 * so this is cheaper than any conversion pipeline would be — and it can never drift from the
 * server's data.
 *
 * Everything is cached by path, and in-flight fetches are shared, because a map change asks
 * for the same package from several places at once.
 */
import { GamePackage } from '../assets/package-file.js';
import { PipImage } from '../assets/pip-image.js';
import { PipAnimateSet } from '../assets/pip-animate-set.js';
import { codecs as defaultCodecs } from '../assets/codecs.js';

export class AssetSource {
  /**
   * @param {{baseUrl?: string, codecs?: object, fetch?: typeof fetch}} [opts]
   *   `fetch` is injectable so this can be driven from Node tests/tools.
   */
  constructor(opts = {}) {
    this.baseUrl = (opts.baseUrl || '/data').replace(/\/$/, '');
    this.codecs = opts.codecs || defaultCodecs;
    this._fetch = opts.fetch || ((...a) => fetch(...a));
    this._bytes = new Map();      // path -> Promise<Uint8Array>
    this._packages = new Map();   // area dir -> Promise<GamePackage>
    this._sets = new Map();       // path -> Promise<PipAnimateSet>
    this._index = null;
  }

  /** Raw bytes of a file under the data directory, e.g. "Areas/87_1/client.pkg". */
  bytes(relPath) {
    const key = relPath.replace(/^\//, '');
    if (!this._bytes.has(key)) {
      this._bytes.set(key, this._fetch(`${this.baseUrl}/${key}`).then(async (res) => {
        if (!res.ok) throw new Error(`asset ${key}: HTTP ${res.status}`);
        return new Uint8Array(await res.arrayBuffer());
      }).catch((e) => {
        this._bytes.delete(key);   // a failed fetch must not poison the cache forever
        throw e;
      }));
    }
    return this._bytes.get(key);
  }

  /** `{areas, mapToArea}` — see web/bridge/asset-index.js. */
  async areaIndex() {
    if (!this._index) {
      this._index = this._fetch(`${this.baseUrl}/areas.json`).then(async (res) => {
        if (!res.ok) throw new Error(`areas.json: HTTP ${res.status}`);
        return res.json();
      }).catch((e) => { this._index = null; throw e; });
    }
    return this._index;
  }

  /** The area package holding a global map id, plus the map's id inside that package. */
  async packageForMap(globalMapId) {
    const index = await this.areaIndex();
    const dir = index.mapToArea[globalMapId];
    if (!dir) throw new Error(`no area package holds map ${globalMapId}`);
    if (!this._packages.has(dir)) {
      this._packages.set(dir, this.bytes(`Areas/${dir}/client.pkg`)
        .then((b) => new GamePackage(b, this.codecs))
        .catch((e) => { this._packages.delete(dir); throw e; }));
    }
    return {
      pkg: await this._packages.get(dir),
      localMapId: GamePackage.localMapId(globalMapId),
      areaDir: dir,
    };
  }

  /**
   * A `.ctn` animate set (a character, a horse, an effect). Its `.pip` images are named inside
   * the definition and live next to it, so they are fetched from the same directory.
   */
  animateSet(relPath) {
    const key = relPath.replace(/^\//, '');
    if (!this._sets.has(key)) {
      this._sets.set(key, this._loadAnimateSet(key)
        .catch((e) => { this._sets.delete(key); throw e; }));
    }
    return this._sets.get(key);
  }

  async _loadAnimateSet(key) {
    const dir = key.slice(0, key.lastIndexOf('/') + 1);
    const def = await this.bytes(key);
    // The names are inside the definition, so the images can only be fetched after it lands;
    // they are then fetched together rather than one at a time.
    const names = new PipAnimateSet([], def).imageNames;
    const images = await Promise.all(names.map(async (n) => {
      try {
        return new PipImage(await this.bytes(dir + n), this.codecs);
      } catch (e) {
        // A set that names an image the data dir does not have still animates with the rest of
        // its pieces; losing the whole character would be worse.
        console.warn(`[assets] ${key}: image ${n} unavailable (${e.message})`);
        return null;
      }
    }));
    return new PipAnimateSet(images, def);
  }
}
