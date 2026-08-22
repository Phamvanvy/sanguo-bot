/*
 * Resource spine — the minimum asset plumbing the UI scripts need, wrapped so
 * syscalls stay SYNCHRONOUS like the Java ones.
 *
 * Java resolved every resource from a local cache (ResourceManager.findResource);
 * the browser must prefetch. So the contract is:
 *
 *   const store = new ResourceStore({ fetchBytes, rasterise, inflate, decodePNG });
 *   await store.preload(['ui_res480.pip', 'mainmenu.ctn', ...]); // before execute()
 *   ...later, inside a syscall: store.imageSetSync(name)   // throws if not preloaded
 *
 * `rasterise({width,height,rgba})` turns decoded pixels into something
 * Graphics.drawImage accepts (a canvas in the browser, a recording stub in
 * tests) — injected so this module never touches the DOM.
 */
import { PipImage, transformRGBA } from '../assets/pip-image.js';
import { PipAnimateSet } from '../assets/pip-animate-set.js';

/** Thrown by the resource layer when a script touches something not preloaded. */
export class MissingResourceError extends Error {
  constructor(kind, name) {
    super(`${kind} not preloaded: "${name}"`);
    this.name = 'MissingResourceError';
    this.kind = kind;
    this.resource = name;
  }
}

export class ResourceStore {
  /**
   * @param {object} opts
   * @param {(name: string) => Promise<Uint8Array>} opts.fetchBytes
   * @param {({width:number,height:number,rgba:Uint8Array}) => any} opts.rasterise
   * @param {(bytes: Uint8Array) => Uint8Array} [opts.inflate]
   * @param {(bytes: Uint8Array) => {width:number,height:number,rgba:Uint8Array}} [opts.decodePNG]
   */
  constructor({ fetchBytes, fetchBytesSync = null, rasterise, inflate, decodePNG }) {
    this.fetchBytes = fetchBytes;
    // Optional synchronous fetch (Node tests: fs.readFileSync). The resource
    // syscalls are synchronous like the Java ones; in the browser everything
    // must come from preload(), here a miss can still be resolved on demand.
    this.fetchBytesSync = fetchBytesSync;
    this.rasterise = rasterise;
    this.codecs = { inflate, decodePNG };
    /** @type {Map<string, Promise<Uint8Array>>} */
    this._bytes = new Map();
    /** @type {Map<string, Uint8Array>} settled bytes, readable synchronously */
    this._syncBytes = new Map();
    /** @type {Map<string, VMImageSet>} */
    this._imageSets = new Map();
    /** @type {Map<string, VMAnimateSet>} */
    this._animateSets = new Map();
  }

  /** raw bytes for a resource name, fetched once */
  load(name) {
    let p = this._bytes.get(name);
    if (!p) {
      p = this.fetchBytes(name);
      this._bytes.set(name, p);
      p.catch(() => this._bytes.delete(name)); // allow a retry after failure
    }
    return p;
  }

  /**
   * prefetch a list of names; resolves when every one is cached AND readable
   * synchronously — the boot sequence must await this before running scripts,
   * because the resource syscalls are synchronous like the Java ones.
   */
  async preload(names) {
    await Promise.all(names.map(async (n) => {
      const bytes = await this.load(n);
      this._syncBytes.set(n, bytes);
    }));
  }

  /** synchronous ImageSet lookup — the shape every ImageSet_* syscall needs */
  imageSetSync(name) {
    let set = this._imageSets.get(name);
    if (!set) {
      const bytes = this._cachedBytes(name);
      if (bytes == null) throw new MissingResourceError('ImageSet', name);
      set = new VMImageSet(this.makePip(bytes), this.rasterise);
      this._imageSets.set(name, set);
    }
    return set;
  }

  /** synchronous AnimateSet lookup over a preloaded .ctn/.anp definition */
  animateSetSync(name, imageNames) {
    let set = this._animateSets.get(name);
    if (!set) {
      const bytes = this._cachedBytes(name);
      if (bytes == null) throw new MissingResourceError('AnimateSet', name);
      const images = (imageNames ?? []).map((n, i) => {
        try {
          return this.imageSetSync(n).pip;
        } catch {
          return null; // a missing sheet degrades one limb, like AnimatedSprites
        }
      });
      set = new VMAnimateSet(this.makeAnimateSet(bytes, images), this.rasterise);
      this._animateSets.set(name, set);
    }
    return set;
  }

  _cachedBytes(name) {
    let bytes = this._syncBytes.get(name);
    if (bytes == null && this.fetchBytesSync) {
      try {
        bytes = this.fetchBytesSync(name);
        this._syncBytes.set(name, bytes);
      } catch {
        bytes = null;
      }
    }
    return bytes;
  }

  /**
   * build a PipImage decoder over raw .pip bytes — or, when the script hands
   * us a plain .png (progress_bar_480.png, logo480.png, ...), a single-frame
   * shim with the same getWidth/getHeight/frameToRGBA surface.
   */
  makePip(bytes) {
    if (bytes != null && bytes.length > 8 &&
        bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4e && bytes[3] === 0x47) {
      const img = this.codecs.decodePNG(bytes);
      return {
        getWidth: () => img.width,
        getHeight: () => img.height,
        frameToRGBA: (frame, trans = 0) =>
          trans ? transformRGBA(img.rgba, img.width, img.height, trans)
                : { width: img.width, height: img.height, rgba: img.rgba },
      };
    }
    return new PipImage(bytes, this.codecs);
  }

  /** build a PipAnimateSet over raw .ctn/.anp bytes plus its sheets */
  makeAnimateSet(bytes, images) {
    return PipAnimateSet.fromPackage
      ? PipAnimateSet.fromPackage(bytes, { images, ...this.codecs })
      : new PipAnimateSet(images, bytes);
  }
}

/**
 * com.pip.sanguo.ImageSet as scripts see it: frame dimensions, drawing one
 * frame with the game's trans codes, and the grey/lighter/darker/mask filters
 * (0x0048..0x004B) applied over the decoded RGBA.
 */
export class VMImageSet {
  /** @param {PipImage} pip */
  constructor(pip, rasterise) {
    this.pip = pip;
    this.rasterise = rasterise;
    /** @type {Map<string, any>} "frame:trans" -> drawable */
    this._cache = new Map();
  }

  getFrameWidth(frame) {
    return this.pip.getWidth(frame);
  }

  getFrameHeight(frame) {
    return this.pip.getHeight(frame);
  }

  /** rasterise once per (frame, trans), then blit through the MIDP Graphics */
  drawFrame(g, frame, x, y, trans = 0, anchor = 0) {
    const key = `${frame}:${trans}`;
    let img = this._cache.get(key);
    if (img === undefined) {
      try {
        const rgba = this.pip.frameToRGBA(frame, trans);
        img = rgba.width > 0 && rgba.height > 0 ? this.rasterise(rgba) : null;
      } catch {
        img = null; // one bad frame must not take the paint pass down
      }
      this._cache.set(key, img);
    }
    if (img != null) g.drawImage(img, x, y, anchor);
  }

  /** 0x0048..0x004B filters — applied to every cached frame and future decodes. */
  _filter(fn) {
    // The decoders produce fresh RGBA per call, so a filter is a per-frame
    // transform applied lazily: wrap the rasteriser.
    const inner = this.rasterise;
    this.rasterise = (rgba) => inner(fn(rgba));
    this._cache.clear();
  }

  gray() {
    this._filter(({ width, height, rgba }) => {
      const out = rgba.slice();
      for (let i = 0; i < out.length; i += 4) {
        const v = (out[i] * 77 + out[i + 1] * 150 + out[i + 2] * 29) >> 8;
        out[i] = out[i + 1] = out[i + 2] = v;
      }
      return { width, height, rgba: out };
    });
  }

  lighter(v) {
    this._filter(({ width, height, rgba }) => {
      const out = rgba.slice();
      for (let i = 0; i < out.length; i += 4) {
        out[i] = Math.min(255, out[i] + v);
        out[i + 1] = Math.min(255, out[i + 1] + v);
        out[i + 2] = Math.min(255, out[i + 2] + v);
      }
      return { width, height, rgba: out };
    });
  }

  darker(v) {
    this.lighter(-v);
  }

  mask(rgb) {
    const r = (rgb >> 16) & 0xff;
    const gch = (rgb >> 8) & 0xff;
    const b = rgb & 0xff;
    this._filter(({ width, height, rgba }) => {
      const out = rgba.slice();
      for (let i = 0; i < out.length; i += 4) {
        if (out[i + 3] !== 0) {
          out[i] = r; out[i + 1] = gch; out[i + 2] = b;
        }
      }
      return { width, height, rgba: out };
    });
  }
}

/**
 * com.pip.sanguo.PipAnimateSet as scripts see it: animate lengths, bounding
 * boxes, and drawing either one composite frame or an animated frame at a tick.
 */
export class VMAnimateSet {
  /** @param {PipAnimateSet} set */
  constructor(set, rasterise) {
    this.set = set;
    this.rasterise = rasterise;
    /** @type {Map<string, any>} "imgIdx:frame:trans" -> drawable */
    this._cache = new Map();
    this._imageIds = new Map();
    set.images.forEach((img, i) => { if (img) this._imageIds.set(img, i); });
  }

  getAnimateLength(index) {
    return this.set.animateLength(index);
  }

  /** int[4] bounding box over every step of the animation, origin at the anchor */
  getAnimateBox(index) {
    let minX = 0; let minY = 0; let maxX = 0; let maxY = 0;
    const length = this.set.animateLength(index);
    for (let t = 0; t < length; t += 1) {
      for (const p of this.set.resolveAnimate(index, t)) {
        if (!p.image) continue;
        const w = p.image.getWidth(p.pipFrame);
        const h = p.image.getHeight(p.pipFrame);
        minX = Math.min(minX, p.x); minY = Math.min(minY, p.y);
        maxX = Math.max(maxX, p.x + w); maxY = Math.max(maxY, p.y + h);
      }
    }
    return Int32Array.of(minX, minY, maxX - minX, maxY - minY);
  }

  _drawable(p) {
    if (!p.image) return null;
    const key = `${this._imageIds.get(p.image) ?? -1}:${p.pipFrame}:${p.trans}`;
    if (!this._cache.has(key)) {
      let img = null;
      try {
        const rgba = p.image.frameToRGBA(p.pipFrame, p.trans);
        img = rgba.width > 0 && rgba.height > 0 ? this.rasterise(rgba) : null;
      } catch {
        img = null;
      }
      this._cache.set(key, img);
    }
    return this._cache.get(key);
  }

  /** draw every piece of one composite frame id */
  drawFrame(g, frame, x, y) {
    for (const p of this.set.framePieces(frame)) {
      const img = this._drawable(p);
      if (img != null) g.drawImage(img, x + p.x, y + p.y, 0);
    }
  }

  /** draw the animation `index` at time `tick`, anchored at (x, y) */
  drawAnimateFrame(g, index, tick, x, y) {
    for (const p of this.set.resolveAnimate(index, tick)) {
      const img = this._drawable(p);
      if (img != null) g.drawImage(img, x + p.x, y + p.y, 0);
    }
  }
}

/**
 * AnimatePlayer — a playing instance of a VMAnimateSet: current animation,
 * play type, elapsed time. Only what 0x1303/0x1304/0x1326 and the sprite
 * syscalls observe; more behaviour lands with the world layer.
 */
export const ANIMATE_PLAY_TYPE_ALWAYS = 0;

export class VMAnimatePlayer {
  /** @param {VMAnimateSet} animateSet */
  constructor(animateSet) {
    this.animateSet = animateSet;
    this.animateIndex = 0;
    this.elapsed = 0;
    this.shown = true;
    this.anchor = 0;
    this.order = 0;
  }

  init(animateSet) {
    this.animateSet = animateSet;
    return this;
  }

  setAnimate(index /* , playType, callback, callbackSprite */) {
    this.animateIndex = index;
    this.elapsed = 0;
  }

  setShown(shown) {
    this.shown = shown;
  }

  setAnchor(anchor) {
    this.anchor = anchor;
  }

  setOrder(order) {
    this.order = order;
  }

  advance(ms) {
    this.elapsed += ms;
  }

  draw(g, x, y) {
    if (!this.shown || !this.animateSet) return;
    this.animateSet.drawAnimateFrame(g, this.animateIndex, this.elapsed, x, y);
  }
}