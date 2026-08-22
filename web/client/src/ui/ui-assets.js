/*
 * UiAssets — the original client's own interface art, made usable from the DOM.
 *
 * The HUD is HTML, not canvas, so it cannot blit a decoded `.pip` frame directly. This turns
 * one into an <img>-able data URL, going through the same decoders the renderer uses, so the
 * icons on the HUD are literally the game's icons rather than lookalikes.
 *
 * Frame indices are positions in the file's block table (`ui_res480.pip` is a 9-wide contact
 * sheet when dumped in order), so they are recorded here by name once instead of being spelled
 * out at every call site. Everything is cached: an icon is decoded and encoded exactly once.
 */
import { PipImage } from '../assets/pip-image.js';
import { imageToCanvas } from '../game/graphics.js';

/** The shared UI atlas: bars, arrows, and the icon set the action bar draws from. */
export const UI_RES = 'client_pkg/Flash/ui_res480.pip';
/** The touch D-pad. Frame 0 is the whole pad, 1..4 the individual arrows. */
export const DIRECT_KEY = 'client_pkg/Flash/directKey.pip';
/** 42x42 skill/item icons — 155 of them. */
export const ABILITY = 'client_pkg/Flash/ability42x42.pip';

/**
 * Named frames, each checked by rendering it rather than counted off the sheet — the atlas has
 * no names of its own and an index that is one out gives you a plausible-looking wrong icon.
 */
export const ICON = {
  // ui_res480.pip — the small HUD glyphs
  bag: 36,
  chat: 37,
  mail: 38,
  alert: 39,
  exp: 43,
  money: 34,
  purse: 35,
  sword: 51,
};

/** ability42x42.pip — the framed 42x42 icons the action bar uses. */
export const ABILITY_ICON = {
  person: 112,
  chest: 110,
  scroll: 52,
  book: 55,
  crest: 113,
  speech: 116,
  runner: 114,
};

/** Frames of `directKey.pip`. */
export const DPAD = { base: 0, up: 1, down: 2, left: 3, right: 4 };

export class UiAssets {
  /** @param {import('../game/asset-source.js').AssetSource} source */
  constructor(source) {
    this.source = source;
    this._pips = new Map();   // path -> Promise<PipImage>
    this._urls = new Map();   // "path:frame:scale" -> Promise<string>
  }

  /** A decoded `.pip`, shared between every icon taken out of it. */
  pip(relPath) {
    if (!this._pips.has(relPath)) {
      this._pips.set(relPath, this.source.bytes(relPath)
        .then((b) => new PipImage(b, this.source.codecs))
        .catch((e) => { this._pips.delete(relPath); throw e; }));
    }
    return this._pips.get(relPath);
  }

  /**
   * One frame as a data URL, nearest-neighbour scaled.
   * Resolves to '' when the art is missing so a decorative icon can never break the HUD.
   */
  frameURL(relPath, frame, scale = 1) {
    const key = `${relPath}:${frame}:${scale}`;
    if (!this._urls.has(key)) {
      this._urls.set(key, this._render(relPath, frame, scale)
        .catch((e) => {
          console.warn(`[ui] ${relPath}#${frame}: ${e.message}`);
          return '';
        }));
    }
    return this._urls.get(key);
  }

  async _render(relPath, frame, scale) {
    const pip = await this.pip(relPath);
    const src = imageToCanvas(pip.frameToRGBA(frame, 0));
    // A real <canvas>, not the OffscreenCanvas graphics.js prefers: only this one has toDataURL.
    const out = document.createElement('canvas');
    out.width = Math.max(1, Math.round(src.width * scale));
    out.height = Math.max(1, Math.round(src.height * scale));
    const ctx = out.getContext('2d');
    ctx.imageSmoothingEnabled = false;
    ctx.drawImage(src, 0, 0, out.width, out.height);
    return out.toDataURL('image/png');
  }

  /** Set an element's background to a frame. Fire-and-forget: the HUD renders before the art. */
  async setBackground(el, relPath, frame, scale = 1) {
    const url = await this.frameURL(relPath, frame, scale);
    if (url) el.style.backgroundImage = `url(${url})`;
  }

  /** Point an <img> at a frame. */
  async setImage(img, relPath, frame, scale = 1) {
    const url = await this.frameURL(relPath, frame, scale);
    if (url) img.src = url;
  }
}

/**
 * A character portrait: the head of an idle pose, cropped out of the walking sprite.
 *
 * The art has no separate portrait, so the sprite is drawn into a small canvas anchored the way
 * the renderer anchors it (offsets are relative to the feet) and the top of the figure is kept.
 *
 * @param {import('../game/sprite-set.js').AnimatedSprites} sprites
 * @param {{size?:number, animateId?:number, zoom?:number}} [opts]
 * @returns {HTMLCanvasElement}
 */
export function portraitCanvas(sprites, opts = {}) {
  const size = opts.size || 64;
  const zoom = opts.zoom || 2;
  const canvas = document.createElement('canvas');
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext('2d');
  ctx.imageSmoothingEnabled = false;
  if (!sprites) return canvas;

  const pieces = sprites.pieces(opts.animateId ?? 0, 0);
  if (pieces.length === 0) return canvas;

  // Piece offsets are relative to the feet, so the figure's own bounding box has to be measured
  // rather than assumed: a drawn weapon reaches further up and further out than the body does.
  const top = Math.min(...pieces.map((p) => p.dy));
  const bottom = Math.max(...pieces.map((p) => p.dy + p.canvas.height));
  const left = Math.min(...pieces.map((p) => p.dx));
  const right = Math.max(...pieces.map((p) => p.dx + p.canvas.width));
  const height = Math.max(1, bottom - top);

  // A portrait shows the top HEAD_FRACTION of the figure blown up to fill the box; `head:false`
  // fits the whole figure instead, which is what the character-select slots want.
  const HEAD_FRACTION = 0.42;
  const fit = opts.head === false
    ? Math.min(zoom, size / height)
    : Math.min(zoom * 2, size / (height * HEAD_FRACTION));

  ctx.save();
  ctx.translate(size / 2 - ((left + right) / 2) * fit, -top * fit);
  ctx.scale(fit, fit);
  for (const p of pieces) ctx.drawImage(p.canvas, p.dx, p.dy);
  ctx.restore();
  return canvas;
}
