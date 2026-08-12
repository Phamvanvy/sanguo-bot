/*
 * The only place in the client that touches canvas APIs, so everything above it stays
 * testable in Node.
 *
 * The asset decoders produce raw RGBA buffers; Canvas2D can only *blend* things that are
 * drawable images, not ImageData (putImageData replaces pixels, alpha and all). So every
 * decoded frame is turned into a small canvas once and blitted from then on.
 */

/** OffscreenCanvas where available (no DOM, cheaper), a detached <canvas> otherwise. */
export function createCanvas(width, height) {
  if (typeof OffscreenCanvas !== 'undefined') return new OffscreenCanvas(width, height);
  const c = document.createElement('canvas');
  c.width = width;
  c.height = height;
  return c;
}

/**
 * RGBA8888 -> a drawable canvas.
 * @param {{width:number, height:number, rgba:Uint8Array}} img
 */
export function imageToCanvas(img) {
  const canvas = createCanvas(Math.max(1, img.width), Math.max(1, img.height));
  const ctx = canvas.getContext('2d');
  // Uint8ClampedArray must share the buffer, not copy it -- these are decoded per frame.
  const data = new ImageData(new Uint8ClampedArray(img.rgba.buffer, img.rgba.byteOffset, img.width * img.height * 4),
    img.width, img.height);
  ctx.putImageData(data, 0, 0);
  return canvas;
}

/** Nearest-neighbour scaling: this is pixel art, smoothing turns it to mush. */
export function disableSmoothing(ctx) {
  ctx.imageSmoothingEnabled = false;
  ctx.mozImageSmoothingEnabled = false;
  ctx.webkitImageSmoothingEnabled = false;
}
