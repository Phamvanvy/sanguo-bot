/*
 * UnitView — one character on the map: where it is, where it is heading, and which animation
 * that implies.
 *
 * The same model covers us and everyone else, because the inputs are the same shape: a target
 * position (our click, or the position the server reports for another unit in
 * UNIT_MOVE_SERVER) that the unit walks to at its speed. Nobody teleports — a unit that jumps
 * to each reported position looks broken, and the server only reports every few hundred ms.
 */
import { advance, directionFromDelta, BASE_SPEED, DIR_DOWN } from './movement.js';
import { animateIndex, STAND, WALK } from './animation.js';

export class UnitView {
  /**
   * @param {{id:number, x:number, y:number, name?:string, self?:boolean,
   *          sprites?:import('./sprite-set.js').AnimatedSprites, speed?:number}} opts
   */
  constructor(opts) {
    this.id = opts.id;
    this.x = opts.x;
    this.y = opts.y;
    this.name = opts.name || '';
    this.self = !!opts.self;
    this.sprites = opts.sprites || null;
    this.speed = opts.speed || BASE_SPEED;
    this.unitType = opts.unitType;
    this.dir = DIR_DOWN;
    this.moving = false;
    /** @type {{x:number,y:number}|null} */
    this.target = null;
    /** When the current animation started, so its cycle restarts on a state change. */
    this.animateStart = 0;
    this._group = STAND;
    this.lastSeenMs = 0;
  }

  get animateId() { return animateIndex(this._group, this.dir); }

  /** Walk towards a point. Facing is set immediately, so turning in place reads as a turn. */
  setTarget(x, y, nowMs = 0) {
    this.target = { x, y };
    const dir = directionFromDelta(x - this.x, y - this.y, this.dir);
    this._setState(dir, WALK, nowMs);
  }

  /** Drop the unit exactly where the server says it is (spawn, map change, correction). */
  placeAt(x, y, nowMs = 0) {
    this.x = x;
    this.y = y;
    this.target = null;
    this._setState(this.dir, STAND, nowMs);
  }

  /**
   * Advance towards the target.
   * @param {number} dtMs time since the last update
   * @returns {boolean} whether the position changed (the caller may need to send a MOVE)
   */
  update(dtMs, nowMs = 0) {
    if (!this.target) return false;
    const step = (this.speed * dtMs) / 1000;
    const next = advance(this, this.target, step);
    const moved = next.x !== this.x || next.y !== this.y;
    if (moved) {
      this.dir = directionFromDelta(next.x - this.x, next.y - this.y, this.dir);
      this.x = next.x;
      this.y = next.y;
      if (this._group !== WALK) this._setState(this.dir, WALK, nowMs);
    }
    if (next.arrived) {
      this.target = null;
      this._setState(this.dir, STAND, nowMs);
    }
    return moved;
  }

  _setState(dir, group, nowMs) {
    if (this.dir === dir && this._group === group) return;
    this.dir = dir;
    this._group = group;
    this.moving = group === WALK;
    this.animateStart = nowMs;
  }
}
