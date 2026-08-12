/*
 * Which animation of a character's .ctn to play, and where in it we are.
 *
 * The original client does not decide this itself: animation indices come out of the server's
 * script VM (GameSprite.vm_game_set_animate_index / vm_sprite_get_animate_para). Porting the
 * VM is far beyond G3b, so the layout below was read off the asset instead — male.ctn's 52
 * animations are 13 groups of 4, one per direction, in Tool.DIR_* order (down, right, left,
 * up), which is visible both in the step tables and in the rendered poses:
 *
 *   0..3    16 steps, 42 ticks   idle "breathing" loop        <- STAND
 *   4..7     4 steps,  8 ticks   walk cycle                   <- WALK
 *   8..11   16 steps, 42 ticks   idle, weapon drawn
 *   12..15   4 steps,  8 ticks   walk, weapon drawn
 *   16..23   8 and 5 steps       action (swing/recover)
 *   24..31   4 steps, 10 ticks   short action, twice over
 *   32..35  10 steps,100 ticks   long hold (rest/sit)
 *   36..39   2 steps, 13 ticks   two-pose action
 *   40..51                       further idle/walk variants
 *
 * Only STAND and WALK are named here, because only those two were verified frame by frame.
 * The rest keep their group numbers so a later milestone can name them without re-deriving
 * the structure.
 */

/** Milliseconds per animation tick: GameMain.MILLIS_PRE_UPDATE, the client's frame period. */
export const TICK_MS = 80;

/** First animation of a group; add the direction (Tool.DIR_*) to get the animation id. */
export const STAND = 0;
export const WALK = 4;

/** Animation id for `group` (STAND/WALK/...) facing `dir` (DIR_DOWN..DIR_UP). */
export function animateIndex(group, dir) {
  return group + (dir & 3);
}

/**
 * Where an animation is at `elapsedMs`, looping. PipAnimateSet.animateFrameAt returns null
 * past the end (it plays once), so the time is folded into the animation's duration first.
 * @param {import('../assets/pip-animate-set.js').PipAnimateSet} set
 * @returns {{frame:number, dx:number, dy:number, delay:number}|null}
 */
export function loopedStep(set, animateId, elapsedMs) {
  const duration = set.animateDuration(animateId);
  if (duration <= 0) return set.animateSteps(animateId)[0] ?? null;
  const tick = Math.floor(elapsedMs / TICK_MS) % duration;
  return set.animateFrameAt(animateId, tick);
}
