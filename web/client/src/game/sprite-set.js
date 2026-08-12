/*
 * AnimatedSprites — a PipAnimateSet made drawable.
 *
 * The same class serves characters (male.ctn), map decor (npc.anp) and effects, because they
 * are all the same asset shape: animations of composite frames of pieces of .pip images.
 * Its job is caching — a piece is decoded and rasterised once per (image, frame, transform)
 * and then blitted, which is what makes 60 fps possible on top of a software PNG decoder.
 */
import { imageToCanvas } from './graphics.js';
import { loopedStep } from './animation.js';

export class AnimatedSprites {
  /** @param {import('../assets/pip-animate-set.js').PipAnimateSet} set */
  constructor(set) {
    this.set = set;
    this._cache = new Map();     // "imageId:frame:trans" -> canvas | null (null = undrawable)
    this._imageIds = new Map();  // PipImage -> index, so cache keys don't scan the array
    set.images.forEach((img, i) => { if (img) this._imageIds.set(img, i); });
    /** Pieces we failed to decode, for the debug overlay. */
    this.failures = 0;
  }

  get animateCount() { return this.set.animateCount; }

  /**
   * Ready-to-blit pieces for an animation at `elapsedMs`, positioned relative to the unit's
   * anchor (its feet, so offsets are usually negative).
   * @returns {{canvas: any, dx:number, dy:number}[]}
   */
  pieces(animateId, elapsedMs = 0) {
    if (animateId < 0 || animateId >= this.set.animateCount) return [];
    const step = loopedStep(this.set, animateId, elapsedMs);
    if (!step) return [];
    const out = [];
    for (const p of this.set.resolveAnimate(animateId, step)) {
      const canvas = this._piece(p);
      if (canvas) out.push({ canvas, dx: p.x, dy: p.y });
    }
    return out;
  }

  _piece(p) {
    if (!p.image) return null;
    const key = `${this._imageIds.get(p.image) ?? -1}:${p.pipFrame}:${p.trans}`;
    if (!this._cache.has(key)) {
      let canvas = null;
      try {
        const img = p.image.frameToRGBA(p.pipFrame, p.trans);
        if (img.width > 0 && img.height > 0) canvas = imageToCanvas(img);
      } catch (e) {
        // One unreadable piece must not take the frame down with it: the character simply
        // renders without that limb, which is visible and debuggable.
        this.failures++;
        console.warn(`[sprites] piece ${key} failed: ${e.message}`);
      }
      this._cache.set(key, canvas);
    }
    return this._cache.get(key);
  }
}
