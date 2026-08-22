/*
 * Joystick — the touch pad the original client draws with `directKey.pip`, as a DOM control.
 *
 * It reports a direction, not a destination: the game loop keeps pushing the walk target a
 * little way ahead of the character while the stick is held, which is what makes held-down
 * movement continuous instead of a series of restarts. That also keeps the server happy — it
 * re-runs its pathfinder on every MOVE, so short steps survive where long jumps get dropped.
 *
 * The maths is a pure function; the class is only pointer plumbing on top of it.
 */

/**
 * Stick offset -> unit direction.
 * @param {number} dx pointer offset from the pad centre, in pixels
 * @param {number} dy
 * @param {number} radius the pad's radius; the knob is clamped to it
 * @param {number} [deadZone] fraction of the radius that reads as "no input"
 * @returns {{x:number, y:number, knobX:number, knobY:number}|null} null inside the dead zone
 */
export function stickVector(dx, dy, radius, deadZone = 0.28) {
  const dist = Math.hypot(dx, dy);
  if (dist < radius * deadZone) return null;
  const clamped = Math.min(dist, radius);
  return {
    x: dx / dist,
    y: dy / dist,
    knobX: (dx / dist) * clamped,
    knobY: (dy / dist) * clamped,
  };
}

export class Joystick {
  /**
   * @param {HTMLElement} base the pad; its size defines the radius
   * @param {HTMLElement} knob the thumb, positioned with a transform
   */
  constructor(base, knob) {
    this.base = base;
    this.knob = knob;
    /** @type {{x:number,y:number}|null} the current direction, read by the game loop */
    this.vector = null;
    this._pointerId = null;

    base.addEventListener('pointerdown', (ev) => {
      ev.preventDefault();
      this._pointerId = ev.pointerId;
      base.setPointerCapture(ev.pointerId);
      base.classList.add('active');
      this._track(ev);
    });
    base.addEventListener('pointermove', (ev) => {
      if (ev.pointerId !== this._pointerId) return;
      ev.preventDefault();
      this._track(ev);
    });
    for (const type of ['pointerup', 'pointercancel', 'lostpointercapture']) {
      base.addEventListener(type, (ev) => {
        if (ev.pointerId !== this._pointerId) return;
        this._pointerId = null;
        this.vector = null;
        base.classList.remove('active');
        knob.style.transform = 'translate(-50%, -50%)';
      });
    }
  }

  _track(ev) {
    const r = this.base.getBoundingClientRect();
    const radius = r.width / 2;
    const v = stickVector(ev.clientX - r.left - radius, ev.clientY - r.top - radius, radius);
    this.vector = v && { x: v.x, y: v.y };
    this.knob.style.transform = v
      ? `translate(calc(-50% + ${v.knobX}px), calc(-50% + ${v.knobY}px))`
      : 'translate(-50%, -50%)';
  }
}
