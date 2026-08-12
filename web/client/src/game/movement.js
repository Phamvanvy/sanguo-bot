/*
 * Movement geometry: which way a unit faces, and how far it gets in a frame.
 *
 * Pure functions on purpose — the walk loop is the part most likely to be wrong (the server
 * silently drops moves it considers impossible), so it has to be testable without a socket.
 *
 * Direction codes are the ones both sides already agree on: peony.game.Unit.DIRECT_* on the
 * server and com.pip.common.Tool.DIR_* in the original client are the same four values.
 */

export const DIR_DOWN = 0;
export const DIR_RIGHT = 1;
export const DIR_LEFT = 2;
export const DIR_UP = 3;

/** Base walking speed in world pixels per second: peony.game.Unit.SPEED. */
export const BASE_SPEED = 45;

/**
 * Facing for a movement vector. Port of Tool.calulateDirWithWayPointMatrix: the dominant axis
 * wins, and a tie counts as horizontal (`|dx| - |dy| >= 0`).
 * @param {number} fallback direction to keep when the vector is zero
 */
export function directionFromDelta(dx, dy, fallback = DIR_DOWN) {
  if (dx === 0 && dy === 0) return fallback;
  if (Math.abs(dx) - Math.abs(dy) >= 0) return dx >= 0 ? DIR_RIGHT : DIR_LEFT;
  return dy >= 0 ? DIR_DOWN : DIR_UP;
}

/**
 * Step from `from` towards `to`, at most `distance` world pixels, never overshooting.
 * @returns {{x:number, y:number, arrived:boolean}} rounded to whole pixels, because that is
 *   what MOVE_CLIENT carries (int16) and what the server stores.
 */
export function advance(from, to, distance) {
  const dx = to.x - from.x;
  const dy = to.y - from.y;
  const len = Math.hypot(dx, dy);
  if (len <= distance || len === 0) return { x: Math.round(to.x), y: Math.round(to.y), arrived: true };
  return {
    x: Math.round(from.x + (dx / len) * distance),
    y: Math.round(from.y + (dy / len) * distance),
    arrived: false,
  };
}

/**
 * Break a walk into the waypoints to send to the server.
 *
 * The server re-runs its own pathfinder on each MOVE and silently drops one whose destination
 * it cannot reach from the last accepted position, so a long jump straight to the click point
 * gets thrown away. Walking it in speed-sized steps is both what the original client does and
 * what keeps every individual step trivially reachable.
 *
 * @param {{x:number,y:number}} from
 * @param {{x:number,y:number}} to
 * @param {number} stepPixels distance covered by one step
 * @param {number} [maxSteps] cap, so a click across a huge map cannot queue forever
 */
export function walkPath(from, to, stepPixels, maxSteps = 256) {
  const path = [];
  let cur = { x: from.x, y: from.y };
  for (let i = 0; i < maxSteps; i++) {
    const next = advance(cur, to, stepPixels);
    if (next.x === cur.x && next.y === cur.y) break;   // rounding stalled: nothing left to walk
    path.push({ x: next.x, y: next.y, direct: directionFromDelta(next.x - cur.x, next.y - cur.y) });
    if (next.arrived) break;
    cur = next;
  }
  return path;
}
