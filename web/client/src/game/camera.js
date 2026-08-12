/*
 * Camera maths for the map view. Pure functions, no canvas — so the framing rules are
 * testable without a DOM.
 *
 * World coordinates are map pixels, exactly what the server sends in MOVE/UNIT_MOVE (the
 * spawn point 66,176 is a pixel position, not a cell). Screen coordinates are canvas pixels:
 *   screen = (world - camera) * zoom
 * `zoom` exists because this is 2011 phone art — a 352x320 map is postage-stamp sized on a
 * desktop display.
 */

/**
 * Top-left world position the view should show, centred on `target` and clamped to the map.
 * A map smaller than the viewport is centred instead of clamped, which is why the result can
 * be negative.
 * @param {{x:number,y:number}} target usually the player
 * @param {{width:number,height:number}} view canvas size in screen pixels
 * @param {{width:number,height:number}} map map size in world pixels
 * @param {number} [zoom]
 */
export function computeCamera(target, view, map, zoom = 1) {
  return {
    x: axis(target.x, view.width / zoom, map.width),
    y: axis(target.y, view.height / zoom, map.height),
  };
}

function axis(center, viewSize, mapSize) {
  if (mapSize <= viewSize) return (mapSize - viewSize) / 2;   // whole map fits: centre it
  return Math.min(Math.max(center - viewSize / 2, 0), mapSize - viewSize);
}

/**
 * The world rectangle the view covers, grown by `margin` so that sprites and decor whose
 * anchor is off-screen but whose art overlaps it still get drawn.
 */
export function visibleWorldRect(camera, view, zoom = 1, margin = 0) {
  return {
    x0: camera.x - margin,
    y0: camera.y - margin,
    x1: camera.x + view.width / zoom + margin,
    y1: camera.y + view.height / zoom + margin,
  };
}

/** World -> screen. */
export function worldToScreen(camera, zoom, x, y) {
  return { x: (x - camera.x) * zoom, y: (y - camera.y) * zoom };
}

/** Screen -> world, for turning a click into a move target. */
export function screenToWorld(camera, zoom, x, y) {
  return { x: camera.x + x / zoom, y: camera.y + y / zoom };
}

/** True when a point (an anchor position) lies in the rectangle. */
export function inRect(rect, x, y) {
  return x >= rect.x0 && x <= rect.x1 && y >= rect.y0 && y <= rect.y1;
}
