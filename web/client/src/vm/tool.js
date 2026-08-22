/*
 * Port of the com.pip.common.Tool static helpers the syscall layer and the
 * widget toolkit call. Only the functions observable from .etf scripts are here;
 * asset/wire helpers live in their own layers.
 */

const LF = String.fromCharCode(10);

/** Tool.splitString(s, ch) — split on EVERY occurrence, no empty-filtering:
 *  "a<LF><LF>b" -> ["a", "", "b"], and a trailing separator yields a final "". */
export function splitString(s, ch = LF) {
  const out = [];
  let start = 0;
  while (true) {
    const end = s.indexOf(ch, start);
    if (end === -1) {
      out.push(s.substring(start));
      break;
    }
    out.push(s.substring(start, end));
    start = end + 1;
  }
  return out;
}

/** Tool.mergeString — plain concatenation of every element. */
export function mergeString(v) {
  let out = '';
  for (let i = 0; i < v.size(); i++) out += v.elementAt(i);
  return out;
}

/** Tool.mergeString2 — join with LF, skipping null/empty, no trailing newline. */
export function mergeString2(v) {
  const parts = [];
  for (let i = 0; i < v.size(); i++) {
    const s = v.elementAt(i);
    if (s != null && s !== '') parts.push(s);
  }
  return parts.join(LF);
}

/** Tool.sqrt — integer square root. */
export function isqrt(v) {
  if (v <= 0) return 0;
  return Math.floor(Math.sqrt(v));
}

/** Tool.distance — (int) sqrt(dx*dx + dy*dy). */
export function distance(x1, y1, x2, y2) {
  const dx = x2 - x1;
  const dy = y2 - y1;
  return isqrt(dx * dx + dy * dy);
}

/** Tool.rectIn — does the rect contain the POINT (inclusive edges). */
export function rectIn(x1, y1, w1, h1, x2, y2) {
  return x1 <= x2 && x1 + w1 >= x2 && y1 <= y2 && y1 + h1 >= y2;
}

/** Tool.rectIntersect — exclusive edges: touching rects do NOT intersect. */
export function rectIntersect(x1, y1, w1, h1, x2, y2, w2, h2) {
  return !(x1 + w1 <= x2 || x1 >= x2 + w2 || y1 + h1 <= y2 || y1 >= y2 + h2);
}

/** Tool.rectContain — is rect 2 fully inside rect 1 (inclusive edges). */
export function rectContain(x1, y1, w1, h1, x2, y2, w2, h2) {
  return x1 <= x2 && x1 + w1 >= x2 + w2 && y1 <= y2 && y1 + h1 >= y2 + h2;
}

/** Tool.rectGetIntersection — clamped intersection rect (used by GWidget.getIntersect). */
export function rectGetIntersection(x1, y1, w1, h1, x2, y2, w2, h2) {
  const nx = Math.max(x1, x2);
  const ny = Math.max(y1, y2);
  const nx2 = Math.min(x1 + w1, x2 + w2);
  const ny2 = Math.min(y1 + h1, y2 + h2);
  return [nx, ny, Math.max(0, nx2 - nx), Math.max(0, ny2 - ny)];
}