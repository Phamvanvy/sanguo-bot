/*
 * Renderer — Canvas2D view of a MapScene plus the units on it.
 *
 * Paint order is the original client's (GameView): background, then the ground decor layer,
 * then the role layer INTERLEAVED with units by y so a character walks behind a tree that is
 * further down the map and in front of one further up, then the sky layer over everything.
 * That interleaving is the whole reason decor is not baked into the background bitmap.
 *
 * The draw list is built by a pure function so the ordering and culling rules can be tested
 * without a canvas.
 */
import { computeCamera, visibleWorldRect, worldToScreen, inRect } from './camera.js';
import { disableSmoothing } from './graphics.js';

/** World pixels of slack around the viewport: art extends well above its anchor. */
export const CULL_MARGIN = 96;

/**
 * Merge the role-layer decor and the units into one back-to-front list.
 * @param {{x:number,y:number,animateId:number}[]} roleDecor
 * @param {{x:number,y:number}[]} units
 * @param {{x0:number,y0:number,x1:number,y1:number}} rect visible world rect (already padded)
 * @returns {{kind:'decor'|'unit', item:object}[]} sorted by y, then x, then units last so a
 *   character standing exactly on an object's anchor is visible rather than hidden by it
 */
export function buildDrawList(roleDecor, units, rect) {
  const list = [];
  for (const item of roleDecor) if (inRect(rect, item.x, item.y)) list.push({ kind: 'decor', item });
  for (const item of units) if (inRect(rect, item.x, item.y)) list.push({ kind: 'unit', item });
  list.sort((a, b) => a.item.y - b.item.y
    || a.item.x - b.item.x
    || (a.kind === b.kind ? 0 : a.kind === 'decor' ? -1 : 1));
  return list;
}

export class Renderer {
  /**
   * @param {HTMLCanvasElement} canvas
   * @param {{zoom?: number}} [opts] zoom 2 by default: the art was drawn for a 240x320 phone
   */
  constructor(canvas, opts = {}) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.zoom = opts.zoom || 2;
    this.scene = null;
    this.camera = { x: 0, y: 0 };
    this.showNames = opts.showNames !== false;
    this.lastDrawn = { decor: 0, units: 0 };
  }

  setScene(scene) {
    this.scene = scene;
    this.camera = { x: 0, y: 0 };
  }

  /** Screen position -> world position, for click-to-move. */
  toWorld(screenX, screenY) {
    return {
      x: this.camera.x + screenX / this.zoom,
      y: this.camera.y + screenY / this.zoom,
    };
  }

  /**
   * @param {{center:{x:number,y:number}, units:object[], elapsedMs:number}} frame
   *   each unit: {x, y, sprites: AnimatedSprites, animateId, name?, self?}
   */
  render({ center, units = [], elapsedMs = 0 }) {
    const ctx = this.ctx;
    const view = { width: this.canvas.width, height: this.canvas.height };
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.fillStyle = '#000';
    ctx.fillRect(0, 0, view.width, view.height);
    if (!this.scene) return;

    const scene = this.scene;
    this.camera = computeCamera(center, view, scene, this.zoom);
    const rect = visibleWorldRect(this.camera, view, this.zoom, CULL_MARGIN);

    disableSmoothing(ctx);
    ctx.save();
    ctx.scale(this.zoom, this.zoom);
    ctx.translate(-this.camera.x, -this.camera.y);

    ctx.drawImage(scene.background, 0, 0);

    let decorDrawn = 0;
    for (const item of scene.decor.ground) {
      if (inRect(rect, item.x, item.y)) decorDrawn += this._drawDecor(item, elapsedMs);
    }
    for (const entry of buildDrawList(scene.decor.role, units, rect)) {
      if (entry.kind === 'decor') decorDrawn += this._drawDecor(entry.item, elapsedMs);
      else this._drawUnit(entry.item, elapsedMs);
    }
    for (const item of scene.decor.sky) {
      if (inRect(rect, item.x, item.y)) decorDrawn += this._drawDecor(item, elapsedMs);
    }

    ctx.restore();
    this.lastDrawn = { decor: decorDrawn, units: units.length };

    if (this.showNames) this._drawNames(units, rect);
  }

  _drawDecor(item, elapsedMs) {
    const sprites = this.scene.decorSprites;
    if (!sprites) return 0;
    for (const p of sprites.pieces(item.animateId, elapsedMs)) {
      this.ctx.drawImage(p.canvas, item.x + p.dx, item.y + p.dy);
    }
    return 1;
  }

  _drawUnit(unit, elapsedMs) {
    if (!unit.sprites) {
      // A unit whose art has not loaded (or has no .ctn yet) still has to be visible, or the
      // world looks empty when only the sprite pipeline is broken.
      this.ctx.fillStyle = unit.self ? '#7fff7f' : '#ffaa44';
      this.ctx.fillRect(unit.x - 3, unit.y - 6, 6, 6);
      return;
    }
    // Animation phase is per unit: two characters walking must not march in lockstep.
    const pieces = unit.sprites.pieces(unit.animateId ?? 0, elapsedMs - (unit.animateStart || 0));
    for (const p of pieces) this.ctx.drawImage(p.canvas, unit.x + p.dx, unit.y + p.dy);
  }

  /** Names are drawn unscaled, in screen space, so they stay legible at any zoom. */
  _drawNames(units, rect) {
    const ctx = this.ctx;
    ctx.font = '11px system-ui, sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'bottom';
    for (const u of units) {
      if (!u.name || !inRect(rect, u.x, u.y)) continue;
      const p = worldToScreen(this.camera, this.zoom, u.x, u.y);
      // The anchor is at the feet and the art is ~48 px tall, so this lands just above the head.
      const y = p.y - 52 * this.zoom;
      ctx.lineWidth = 3;
      ctx.strokeStyle = 'rgba(0,0,0,0.85)';
      ctx.strokeText(u.name, p.x, y);
      ctx.fillStyle = u.self ? '#9f9' : '#fd8';
      ctx.fillText(u.name, p.x, y);
    }
  }
}
