/*
 * Tests for the HUD's decisions, not its pixels.
 *
 * The shell is DOM-bound and covered by the live browser run (tools/render_smoke.py). What is
 * worth pinning here is the arithmetic underneath it, because it fails quietly: a minimap that
 * places the player dot a few pixels off still looks like a minimap, and a walk pad with the
 * wrong dead zone still moves the character — just never quite where you asked.
 */
import { test } from 'node:test';
import assert from 'node:assert/strict';

import { fitMap, minimapToWorld } from './minimap.js';
import { stickVector } from './joystick.js';

/* ---------- minimap ---------- */

test('minimap: fits the map inside the box without distorting it', () => {
  // Wider than tall: the scale comes from the width and the result is centred vertically.
  const wide = fitMap({ width: 400, height: 200 }, { width: 200, height: 200 });
  assert.equal(wide.scale, 0.5);
  assert.deepEqual([wide.w, wide.h], [200, 100]);
  assert.deepEqual([wide.ox, wide.oy], [0, 50]);

  const tall = fitMap({ width: 200, height: 400 }, { width: 200, height: 200 });
  assert.equal(tall.scale, 0.5);
  assert.deepEqual([tall.ox, tall.oy], [50, 0]);
});

test('minimap: a click maps back to the world position it points at', () => {
  const map = { width: 400, height: 200 };
  const box = { width: 200, height: 200 };
  const fit = fitMap(map, box);

  // Dead centre of the drawn map is dead centre of the world.
  assert.deepEqual(minimapToWorld(fit, map, 100, 100), { x: 200, y: 100 });
  // Top-left corner of the drawn area, allowing for the vertical letterbox.
  assert.deepEqual(minimapToWorld(fit, map, 0, fit.oy), { x: 0, y: 0 });
});

test('minimap: clicks outside the map clamp to it rather than walking off the edge', () => {
  const map = { width: 400, height: 200 };
  const fit = fitMap(map, { width: 200, height: 200 });
  assert.deepEqual(minimapToWorld(fit, map, -50, -50), { x: 0, y: 0 });
  assert.deepEqual(minimapToWorld(fit, map, 999, 999), { x: 399, y: 199 });
});

test('minimap: a degenerate map does not divide by zero', () => {
  const fit = fitMap({ width: 0, height: 0 }, { width: 100, height: 100 });
  assert.ok(Number.isFinite(fit.scale));
});

/* ---------- walk pad ---------- */

test('walk pad: the dead zone swallows a tap on the middle', () => {
  assert.equal(stickVector(0, 0, 50), null);
  assert.equal(stickVector(5, 5, 50), null);          // inside 28% of the radius
  assert.notEqual(stickVector(20, 0, 50), null);      // outside it
});

test('walk pad: reports a unit direction and a knob clamped to the pad', () => {
  const right = stickVector(30, 0, 50);
  assert.deepEqual([right.x, right.y], [1, 0]);
  assert.deepEqual([right.knobX, right.knobY], [30, 0]);

  // Dragged past the rim: the direction still reads, the knob stops at the radius.
  const far = stickVector(300, 0, 50);
  assert.deepEqual([far.x, far.y], [1, 0]);
  assert.equal(far.knobX, 50);

  const diagonal = stickVector(30, 30, 50);
  assert.ok(Math.abs(diagonal.x - Math.SQRT1_2) < 1e-9);
  assert.ok(Math.abs(Math.hypot(diagonal.knobX, diagonal.knobY) - Math.hypot(30, 30)) < 1e-9);
});

test('walk pad: up on the screen is negative y, the direction the renderer expects', () => {
  const up = stickVector(0, -40, 50);
  assert.deepEqual([up.x, up.y], [0, -1]);
});
