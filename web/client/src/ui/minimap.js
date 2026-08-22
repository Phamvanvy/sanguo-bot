/*
 * Minimap — the whole map at a glance, drawn from the scene's own background bitmap.
 *
 * MapScene already rasterises the background into one offscreen canvas, so the minimap is that
 * canvas scaled down: no second decode, no separate art, and it can never disagree with what
 * the main view shows. Units are dots on top, and the rectangle is the part of the map the
 * camera is currently showing.
 *
 * The placement maths is a pure function so it can be tested without a canvas, and because the
 * same numbers are needed in reverse to turn a click on the minimap into a world position.
 */

/**
 * How a map of `map` pixels is laid out inside a `box` of pixels: uniform scale, centred.
 * @returns {{scale:number, w:number, h:number, ox:number, oy:number}}
 */
export function fitMap(map, box) {
  const scale = Math.min(box.width / Math.max(1, map.width), box.height / Math.max(1, map.height));
  const w = map.width * scale;
  const h = map.height * scale;
  return { scale, w, h, ox: (box.width - w) / 2, oy: (box.height - h) / 2 };
}

/** Minimap pixel -> world pixel, clamped to the map. Inverse of `fitMap`. */
export function minimapToWorld(fit, map, mx, my) {
  return {
    x: Math.max(0, Math.min(map.width - 1, Math.round((mx - fit.ox) / fit.scale))),
    y: Math.max(0, Math.min(map.height - 1, Math.round((my - fit.oy) / fit.scale))),
  };
}

export class Minimap {
  /** @param {HTMLCanvasElement} canvas */
  constructor(canvas) {
    this.canvas = canvas;
    this.ctx = canvas.getContext('2d');
    this.fit = null;
    this.map = null;
  }

  /**
   * @param {{scene:object, player:object, units:Iterable<object>,
   *          view:{width:number,height:number}, camera:{x:number,y:number}, zoom:number}} frame
   */
  draw({ scene, player, units = [], view, camera, zoom }) {
    const ctx = this.ctx;
    const box = { width: this.canvas.width, height: this.canvas.height };
    ctx.clearRect(0, 0, box.width, box.height);
    if (!scene) { this.fit = null; return; }

    this.map = { width: scene.width, height: scene.height };
    const fit = fitMap(this.map, box);
    this.fit = fit;

    ctx.imageSmoothingEnabled = true;                 // downscaled terrain, not pixel art here
    ctx.drawImage(scene.background, fit.ox, fit.oy, fit.w, fit.h);

    // The slice of the map the main view is showing.
    if (view && camera && zoom) {
      ctx.strokeStyle = 'rgba(255,255,255,.85)';
      ctx.lineWidth = 1;
      ctx.strokeRect(
        fit.ox + camera.x * fit.scale, fit.oy + camera.y * fit.scale,
        (view.width / zoom) * fit.scale, (view.height / zoom) * fit.scale,
      );
    }

    // The canvas is backed at twice its CSS size for a sharp downscale, so marker radii are
    // scaled with it — otherwise the dots shrink to invisibility on the HUD's small minimap.
    const px = box.width / 150;
    for (const u of units) this._dot(fit, u.x, u.y, '#ffb43c', 3 * px);
    if (player) this._dot(fit, player.x, player.y, '#6bff6b', 5 * px);
  }

  _dot(fit, x, y, colour, r) {
    const ctx = this.ctx;
    ctx.beginPath();
    ctx.arc(fit.ox + x * fit.scale, fit.oy + y * fit.scale, r, 0, Math.PI * 2);
    ctx.fillStyle = colour;
    ctx.fill();
    ctx.lineWidth = 1;
    ctx.strokeStyle = 'rgba(0,0,0,.7)';
    ctx.stroke();
  }

  /** Where a click at canvas coordinates (mx,my) points to in the world, or null. */
  toWorld(mx, my) {
    if (!this.fit || !this.map) return null;
    return minimapToWorld(this.fit, this.map, mx, my);
  }
}
