/*
 * javax.microedition.lcdui.Graphics + Font + Image, ported onto Canvas2D.
 *
 * The .etf UI scripts draw through syscalls that call straight into these MIDP
 * semantics, so fidelity lives here: inclusive-edge rects, intersecting clips,
 * anchor arithmetic, MIDP arc angles (degrees, CCW from 3 o'clock) and the
 * integer-only colour model. Everything routes through one ctx wrapper so the
 * widget toolkit above stays testable with a recording stub.
 *
 * MIDP constants used by scripts:
 *   LEFT=4 RIGHT=8 HCENTER=1 TOP=16 BOTTOM=32 VCENTER=2 BASELINE=64
 *   SOLID=0 DOTTED=1
 */

export const LEFT = 4;
export const RIGHT = 8;
export const HCENTER = 1;
export const TOP = 16;
export const BOTTOM = 32;
export const VCENTER = 2;
export const BASELINE = 64;

/* MIDP Font selectors */
export const FACE_SYSTEM = 0;
export const FACE_MONOSPACE = 32;
export const FACE_PROPORTIONAL = 64;
export const STYLE_PLAIN = 0;
export const STYLE_BOLD = 1;
export const STYLE_ITALIC = 2;
export const STYLE_UNDERLINED = 4;
export const SIZE_SMALL = 8;
export const SIZE_MEDIUM = 0;
export const SIZE_LARGE = 16;

/** Pixel sizes per MIDP SIZE_* — the knob that makes the whole HUD scale. */
const SIZE_PX = { [SIZE_SMALL]: 13, [SIZE_MEDIUM]: 15, [SIZE_LARGE]: 17 };

let measureCtx = null;
function measuringContext() {
  if (!measureCtx && typeof document !== 'undefined') {
    const c = document.createElement('canvas');
    c.width = c.height = 8;
    measureCtx = c.getContext('2d');
  }
  return measureCtx;
}

/**
 * javax.microedition.lcdui.Font. Caches its CSS font string; widths come from
 * Canvas2D measureText when a DOM exists, otherwise from a char-count estimate
 * (Node tests only care that metrics are self-consistent).
 */
export class Font {
  constructor(face = FACE_SYSTEM, style = STYLE_PLAIN, size = SIZE_MEDIUM) {
    this.face = face;
    this.style = style;
    this.size = size;
    this.px = SIZE_PX[size] ?? SIZE_PX[SIZE_MEDIUM];
    const family = face === FACE_MONOSPACE ? 'monospace' : 'sans-serif';
    const weight = (style & STYLE_BOLD) ? 'bold ' : '';
    const italic = (style & STYLE_ITALIC) ? 'italic ' : '';
    this.css = `${weight}${italic}${this.px}px ${family}`;
    this._height = Math.round(this.px * 1.25);
    this._baseline = Math.round(this.px * 1.0);
  }

  static getFont(face, style, size) {
    return new Font(face, style, size);
  }

  getHeight() {
    return this._height;
  }

  getBaselinePosition() {
    return this._baseline;
  }

  stringWidth(s) {
    s = s == null ? '' : String(s);
    const ctx = measuringContext();
    if (ctx) {
      ctx.font = this.css;
      return Math.ceil(ctx.measureText(s).width);
    }
    return s.length * this.px; // deterministic fallback
  }

  charWidth(ch) {
    return this.stringWidth(String.fromCharCode(ch));
  }

  substringWidth(s, offset, len) {
    return this.stringWidth(String(s ?? '').substring(offset, offset + len));
  }
}

const DEFAULT_FONT = new Font();

/**
 * An drawable image handle. Wraps anything blittable (an HTMLCanvasElement,
 * OffscreenCanvas, or an ImageBitmap) plus its pixel size — the shape every
 * Image.createImage / decoded .pip frame takes in this port.
 */
export class VMImage {
  constructor(source, width, height) {
    this.source = source;
    this.width = width;
    this.height = height;
  }
}

/** Image.createImage(w, h) — a fresh transparent image with its own Graphics. */
export function createImage(width, height) {
  let canvas;
  if (typeof OffscreenCanvas !== 'undefined') {
    canvas = new OffscreenCanvas(Math.max(1, width), Math.max(1, height));
  } else {
    canvas = document.createElement('canvas');
    canvas.width = Math.max(1, width);
    canvas.height = Math.max(1, height);
  }
  const img = new VMImage(canvas, canvas.width, canvas.height);
  img.getGraphics = () => new Graphics(canvas.getContext('2d'), canvas.width, canvas.height);
  return img;
}

/**
 * The Graphics port. One instance wraps one canvas context; all MIDP entry
 * points the syscall switch can reach are implemented.
 */
export class Graphics {
  /**
   * @param {CanvasRenderingContext2D} ctx
   * @param {number} width viewport width (clip ceiling)
   * @param {number} height viewport height
   */
  constructor(ctx, width, height) {
    this.ctx = ctx;
    this.width = width;
    this.height = height;
    this.color = 0x000000;
    this.fontObj = DEFAULT_FONT;
    this.strokeStyleFlag = 0; // SOLID
    this.transX = 0;
    this.transY = 0;
    // MIDP starts clipped to the whole surface
    this.clipX = 0;
    this.clipY = 0;
    this.clipW = width;
    this.clipH = height;
    this._applyClip();
  }

  /**
   * Re-assert clip+translate. Each call replaces the previous one (restore then
   * save) so repeated setClip/translate never stack canvas state.
   */
  _applyClip() {
    const c = this.ctx;
    if (this._clipSaved) c.restore();
    c.save();
    this._clipSaved = true;
    c.beginPath();
    c.rect(this.clipX, this.clipY, this.clipW, this.clipH);
    c.clip();
    c.translate(this.transX, this.transY);
  }

  /* ------------------------------------------------------------- state */

  /** MIDP setColor(int rgb) — 24-bit, alpha forced opaque. */
  setColor(rgb) {
    this.color = rgb & 0xffffff;
    const hex = `#${(this.color).toString(16).padStart(6, '0')}`;
    this.ctx.fillStyle = hex;
    this.ctx.strokeStyle = hex;
  }

  setGrayScale(value) {
    const v = value & 0xff;
    this.setColor((v << 16) | (v << 8) | v);
  }

  getColor() {
    return this.color;
  }

  setFont(font) {
    this.fontObj = font || DEFAULT_FONT;
  }

  getFont() {
    return this.fontObj;
  }

  setStrokeStyle(style) {
    this.strokeStyleFlag = style;
    this.ctx.setLineDash(style === 1 ? [2, 2] : []);
  }

  translate(x, y) {
    this.transX += x;
    this.transY += y;
    this._applyClip();
  }

  getTranslateX() { return this.transX; }
  getTranslateY() { return this.transY; }

  /** MIDP setClip INTERSECTS with the current clip. */
  setClip(x, y, w, h) {
    const nx = Math.max(this.clipX, x);
    const ny = Math.max(this.clipY, y);
    const nx2 = Math.min(this.clipX + this.clipW, x + w);
    const ny2 = Math.min(this.clipY + this.clipH, y + h);
    this.clipX = nx;
    this.clipY = ny;
    this.clipW = Math.max(0, nx2 - nx);
    this.clipH = Math.max(0, ny2 - ny);
    this._applyClip();
  }

  getClipX() { return this.clipX; }
  getClipY() { return this.clipY; }
  getClipWidth() { return this.clipW; }
  getClipHeight() { return this.clipH; }

  /* ------------------------------------------------------------ shapes */

  fillRect(x, y, w, h) {
    if (w <= 0 || h <= 0) return;
    this.ctx.fillRect(x, y, w, h);
  }

  /** MIDP drawRect: 1px outline INSIDE the given bounds. */
  drawRect(x, y, w, h) {
    if (w <= 0 || h <= 0) return;
    this.ctx.strokeRect(x + 0.5, y + 0.5, w - 1, h - 1);
  }

  drawLine(x1, y1, x2, y2) {
    this.ctx.beginPath();
    this.ctx.moveTo(x1 + 0.5, y1 + 0.5);
    this.ctx.lineTo(x2 + 0.5, y2 + 0.5);
    this.ctx.stroke();
  }

  drawRoundRect(x, y, w, h, hr, vr) {
    this._roundPath(x, y, w, h, hr, vr);
    this.ctx.stroke();
  }

  fillRoundRect(x, y, w, h, hr, vr) {
    this._roundPath(x, y, w, h, hr, vr);
    this.ctx.fill();
  }

  _roundPath(x, y, w, h, hr, vr) {
    const rw = Math.min(hr, w / 2);
    const rh = Math.min(vr, h / 2);
    const c = this.ctx;
    c.beginPath();
    c.moveTo(x + rw, y);
    c.lineTo(x + w - rw, y);
    c.quadraticCurveTo(x + w, y, x + w, y + rh);
    c.lineTo(x + w, y + h - rh);
    c.quadraticCurveTo(x + w, y + h, x + w - rw, y + h);
    c.lineTo(x + rw, y + h);
    c.quadraticCurveTo(x, y + h, x, y + h - rh);
    c.lineTo(x, y + rh);
    c.quadraticCurveTo(x, y, x + rw, y);
    c.closePath();
  }

  /** MIDP arcs: degrees, 0 = 3 o'clock, POSITIVE sweeps counter-clockwise. */
  _arcPath(x, y, w, h, startAngle, arcAngle) {
    const cx = x + w / 2;
    const cy = y + h / 2;
    const rx = w / 2;
    const ry = h / 2;
    const a0 = -startAngle * Math.PI / 180;
    const a1 = -(startAngle + arcAngle) * Math.PI / 180;
    const c = this.ctx;
    c.beginPath();
    if (rx === ry) {
      c.arc(cx, cy, rx, a0, a1, arcAngle > 0);
    } else {
      c.ellipse(cx, cy, rx, ry, 0, a0, a1, arcAngle > 0);
    }
  }

  drawArc(x, y, w, h, startAngle, arcAngle) {
    this._arcPath(x, y, w, h, startAngle, arcAngle);
    this.ctx.stroke();
  }

  fillArc(x, y, w, h, startAngle, arcAngle) {
    this._arcPath(x, y, w, h, startAngle, arcAngle);
    this.ctx.closePath();
    this.ctx.fill();
  }

  fillTriangle(x1, y1, x2, y2, x3, y3) {
    const c = this.ctx;
    c.beginPath();
    c.moveTo(x1, y1);
    c.lineTo(x2, y2);
    c.lineTo(x3, y3);
    c.closePath();
    c.fill();
  }

  /* -------------------------------------------------------------- text */

  drawString(text, x, y, anchor) {
    text = text == null ? '' : String(text);
    const f = this.fontObj;
    const w = f.stringWidth(text);
    const h = f.getHeight();
    let dx = x;
    let dy = y;
    if (anchor & RIGHT) dx -= w;
    else if (anchor & HCENTER) dx -= w >> 1;
    // MIDP: no vertical bit means BASELINE
    if (anchor & BOTTOM) dy -= h;
    else if (anchor & VCENTER) dy -= h >> 1;
    else if (!(anchor & BASELINE)) dy -= f.getBaselinePosition();
    this.ctx.font = f.css;
    this.ctx.textBaseline = 'alphabetic';
    this.ctx.fillText(text, dx, dy);
  }

  drawChar(ch, x, y, anchor) {
    this.drawString(String.fromCharCode(ch), x, y, anchor);
  }

  drawSubstring(text, offset, len, x, y, anchor) {
    this.drawString(String(text ?? '').substring(offset, offset + len), x, y, anchor);
  }

  /* ------------------------------------------------------------- image */

  /**
   * MIDP drawImage with anchor placement. `img` is a VMImage or any object
   * exposing {source,width,height} (decoded pip frames qualify).
   */
  drawImage(img, x, y, anchor) {
    if (img == null) return;
    const w = img.width;
    const h = img.height;
    let dx = x;
    let dy = y;
    if (anchor & RIGHT) dx -= w;
    else if (anchor & HCENTER) dx -= w >> 1;
    if (anchor & BOTTOM) dy -= h;
    else if (anchor & VCENTER) dy -= h >> 1;
    this.ctx.drawImage(img.source ?? img, dx, dy);
  }

  /** MIDP drawRegion — the trans codes are the game's own 0..7 (see pip-image). */
  drawRegion(img, sx, sy, sw, sh, trans, dx, dy, anchor) {
    if (img == null) return;
    // destination size flips with the swap transforms (codes 4..7)
    const swap = trans >= 4;
    const dw = swap ? sh : sw;
    const dh = swap ? sw : sh;
    let ddx = dx;
    let ddy = dy;
    if (anchor & RIGHT) ddx -= dw;
    else if (anchor & HCENTER) ddx -= dw >> 1;
    if (anchor & BOTTOM) ddy -= dh;
    else if (anchor & VCENTER) ddy -= dh >> 1;

    const c = this.ctx;
    c.save();
    c.translate(ddx, ddy);
    // normalise the game's 8 codes onto rotate/mirror primitives
    switch (trans) {
      case 0: break;                                   // NONE
      case 1: c.transform(-1, 0, 0, 1, 0, 0); break;   // MIRROR_ROT180 (flip X)
      case 2: c.transform(1, 0, 0, -1, 0, 0); break;   // MIRROR (flip Y)
      case 3: c.transform(-1, 0, 0, -1, 0, 0); break;  // MIRROR_ROT270 (rot180)
      case 4: c.rotate(Math.PI / 2); break;            // ROT90 family
      case 5: c.rotate(Math.PI / 2); break;
      case 6: c.rotate(Math.PI); break;
      case 7: c.rotate(-Math.PI / 2); break;
      default: break;
    }
    try {
      c.drawImage(img.source ?? img, sx, sy, sw, sh, 0, 0, sw, sh);
    } finally {
      c.restore();
    }
  }
}