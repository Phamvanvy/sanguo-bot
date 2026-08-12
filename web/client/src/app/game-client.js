/*
 * G3b browser client: the G2 wire layer with a real view on top.
 *
 * Flow: account login -> character -> enter world -> load the spawn map's own art out of the
 * server's data directory -> render it, with the character walking on it.
 *
 * Movement is deliberately client-led and server-checked: a click sets a target, the unit
 * walks towards it at Unit.SPEED (45 px/s), and a MOVE_CLIENT goes out every MOVE_INTERVAL_MS
 * with the position reached so far. The server re-runs its pathfinder on each of those and
 * silently drops the ones it dislikes, so small steps are what keep us in sync with it.
 */
import { attachWebSocket } from '../net/session.js';
import { ChatChannel, UnitType } from '../net/protocol.js';
import { AssetSource } from '../game/asset-source.js';
import { MapScene } from '../game/map-scene.js';
import { AnimatedSprites } from '../game/sprite-set.js';
import { Renderer } from '../game/renderer.js';
import { UnitView } from '../game/unit-view.js';
import { directionFromDelta } from '../game/movement.js';

const $ = (id) => document.getElementById(id);
const show = (el, on) => { el.hidden = !on; };

/** How often a walking character reports in. 200 ms at 45 px/s is a ~9 px step. */
const MOVE_INTERVAL_MS = 200;
/** Units the server stops mentioning are dropped: "unit left" is not a ported opcode yet. */
const UNIT_TTL_MS = 30000;
/** The player character's art. Other players share it until equipment/sex are decoded. */
const PLAYER_CTN = 'client_pkg/Flash/male.ctn';

const source = new AssetSource({ baseUrl: '/data' });
const renderer = new Renderer($('view'), { zoom: 2 });

let session = null;
let clockOffset = 0;        // serverTime - Date.now(), so MOVE carries a server-ish clock
let player = null;          // UnitView for us
let playerSprites = null;
let currentMapId = null;
let sceneLoading = false;
const units = new Map();    // instanceId -> UnitView (everyone except us)
let lastMoveSentAt = 0;
let lastSentPos = null;

/* ------------------------------------------------------------------ HUD */

function setStatus(text, cls = '') {
  $('status').textContent = text;
  $('status').className = cls;
}

const MAX_LOG_ROWS = 200;
function logLine(text, cls = '') {
  const div = document.createElement('div');
  div.className = 'row ' + cls;
  div.textContent = text;
  $('log').appendChild(div);
  while ($('log').childElementCount > MAX_LOG_ROWS) $('log').removeChild($('log').firstChild);
  $('log').scrollTop = $('log').scrollHeight;
}

/* ------------------------------------------------------------ connection */

async function connect() {
  const url = $('url').value.trim();
  session = attachWebSocket(new WebSocket(url));

  session.on('unknown', (m) => logLine(`<- ${m.name} ${m.segment.length}B (unported)`, 'skip'));
  session.on('error', (m) => logLine(`!! decode error in ${m.name}: ${m.error.message}`, 'err'));
  session.on('fatal', (e) => { logLine(`!! FATAL ${e.message}`, 'err'); setStatus('desynced — reload', 'err'); });
  session.on('close', () => setStatus('disconnected', 'err'));
  session.on('SYNC_TIME_SERVER', (b) => { clockOffset = b.serverTime - Date.now(); });
  session.on('CHAT_SERVER', (b) => logLine(`${b.sourceName}: ${b.message}`, 'chat'));
  session.on('PUSH_HINT_SERVER', (b) => logLine(`hint: ${b.hint}`, 'chat'));
  session.on('UNIT_MOVE_SERVER', onUnitMove);
  session.on('GOMAP_ALLOW_SERVER', (b) => {
    // Fires at spawn (handled by enterWorld, which awaits it) and again on every map change,
    // which is the case this handler is here for.
    if (!player) return;
    player.placeAt(b.x, b.y, performance.now());
    loadScene(b.mapId).catch((e) => setStatus('map load failed: ' + e.message, 'err'));
  });

  await session.ready;
  setStatus(`connected to ${url}`, 'ok');
}

async function doLogin() {
  try {
    $('loginBtn').disabled = true;
    if (!session || session.closed) await connect();
    const account = await session.accountLogin({
      name: $('account').value.trim(),
      password: $('password').value,
    });
    setStatus(`logged in as ${account.name}`, 'ok');
    await refreshCharacters();
    show($('characters'), true);
  } catch (e) {
    setStatus('login failed: ' + e.message, 'err');
  } finally {
    $('loginBtn').disabled = false;
  }
}

async function refreshCharacters() {
  const actors = await session.listActors();
  const list = $('charList');
  list.textContent = '';
  if (actors.length === 0) list.textContent = 'No characters yet — create one below.';
  for (const a of actors) {
    const btn = document.createElement('button');
    btn.textContent = `${a.name} · lv${a.level} · ${a.mapName}`;
    btn.onclick = () => enterWorld(a);
    list.appendChild(btn);
  }
  show($('createForm'), actors.length < 4);      // the server caps an account at 4
}

async function createCharacter() {
  try {
    const created = await session.createActor({
      name: $('charName').value.trim(),
      sex: Number($('sex').value),
      clazz: 0,
      faction: Number($('faction').value),
    });
    setStatus(`created ${created.name}`, 'ok');
    await refreshCharacters();
  } catch (e) {
    setStatus('create failed: ' + e.message, 'err');
  }
}

/* ------------------------------------------------------------- the world */

async function enterWorld(actorSummary) {
  try {
    setStatus('entering world…');
    const { actor, spawn } = await session.enterWorld(actorSummary.id);
    session.loadingFinished();
    session.send('syncTime', 0);

    playerSprites = await loadPlayerSprites();
    player = new UnitView({
      id: actor.id, x: spawn.x, y: spawn.y, name: actor.name, self: true, sprites: playerSprites,
    });
    await loadScene(spawn.mapId);

    show($('enter'), false);
    show($('hud'), true);
    show($('logPanel'), true);
    show($('hint'), true);
    $('who').textContent = `${actor.name} · lv${actor.level}`;
    setStatus('in world', 'ok');
  } catch (e) {
    setStatus('enter world failed: ' + e.message, 'err');
    logLine('!! ' + e.stack, 'err');
  }
}

async function loadPlayerSprites() {
  try {
    return new AnimatedSprites(await source.animateSet(PLAYER_CTN));
  } catch (e) {
    // Without sprites the renderer falls back to markers, which still proves the map and the
    // movement loop — better than refusing to enter the world.
    logLine(`!! character art unavailable: ${e.message}`, 'err');
    return null;
  }
}

async function loadScene(mapId) {
  if (mapId === currentMapId || sceneLoading) return;
  sceneLoading = true;
  setStatus(`loading map ${mapId}…`);
  try {
    const scene = await MapScene.load(source, mapId);
    renderer.setScene(scene);
    currentMapId = mapId;
    units.clear();                              // unit ids are per map instance
    logLine(`map ${scene.id} "${scene.name}" ${scene.width}x${scene.height} — `
      + `${scene.stats.tiles} tiles, ${scene.stats.decor} decor, ${scene.stats.buildMs}ms`);
    setStatus('in world', 'ok');
  } finally {
    sceneLoading = false;
  }
}

function onUnitMove(b) {
  if (b.x === undefined) return;                // a non-POINT update (hp, state, ...)
  if (player && b.instanceId === player.id) return;
  const now = performance.now();
  let u = units.get(b.instanceId);
  if (!u) {
    u = new UnitView({
      id: b.instanceId, x: b.x, y: b.y, unitType: b.unitType,
      // Only players are known to use male.ctn; NPCs and creatures each have their own art
      // that we cannot resolve yet, so they render as markers rather than as wrong sprites.
      sprites: b.unitType === UnitType.PLAYER ? playerSprites : null,
    });
    units.set(b.instanceId, u);
  } else {
    u.setTarget(b.x, b.y, now);
  }
  u.lastSeenMs = now;
}

/* ------------------------------------------------------------- game loop */

let lastFrameMs = 0;
let fps = 0;

function frame(nowMs) {
  requestAnimationFrame(frame);
  const dt = lastFrameMs ? Math.min(nowMs - lastFrameMs, 250) : 0;
  lastFrameMs = nowMs;
  fps += (1000 / Math.max(dt, 1) - fps) * 0.05;

  resizeCanvas();
  if (!player) return;

  if (player.update(dt, nowMs)) sendMoveIfDue(nowMs, false);
  else if (lastSentPos && (lastSentPos.x !== player.x || lastSentPos.y !== player.y)) {
    sendMoveIfDue(nowMs, true);                 // final position of a finished walk
  }

  for (const [id, u] of units) {
    if (nowMs - u.lastSeenMs > UNIT_TTL_MS) { units.delete(id); continue; }
    u.update(dt, nowMs);
  }

  renderer.render({ center: player, units: [...units.values(), player], elapsedMs: nowMs });
  updateHud();
}

function sendMoveIfDue(nowMs, force) {
  if (!force && nowMs - lastMoveSentAt < MOVE_INTERVAL_MS) return;
  if (!session || session.closed) return;
  lastMoveSentAt = nowMs;
  lastSentPos = { x: player.x, y: player.y };
  session.move({
    // The server rejects a client time more than 3 s ahead of its own clock, so use the
    // offset learned from SYNC_TIME rather than the browser's idea of now.
    time: Date.now() + clockOffset,
    x: player.x, y: player.y, direct: player.dir, state: 0,
  });
}

function resizeCanvas() {
  const c = $('view');
  const w = Math.floor(window.innerWidth);
  const h = Math.floor(window.innerHeight);
  if (c.width !== w || c.height !== h) { c.width = w; c.height = h; }
}

function updateHud() {
  const s = session?.stats;
  $('where').textContent = renderer.scene
    ? `${renderer.scene.name} (${renderer.scene.id}) · x ${player.x} y ${player.y} · ${units.size} units`
    : '—';
  $('perf').textContent = `${fps.toFixed(0)} fps · zoom ${renderer.zoom}x · `
    + `${renderer.lastDrawn.decor} decor drawn`;
  if (s) {
    $('stats').textContent = `rx ${s.received} · decoded ${s.decoded} · unported ${s.unknown} `
      + `· errors ${s.decodeErrors} · tx ${s.sent}`;
  }
}

/* ----------------------------------------------------------------- input */

$('view').addEventListener('click', (ev) => {
  if (!player || !renderer.scene) return;
  const r = $('view').getBoundingClientRect();
  const world = renderer.toWorld(ev.clientX - r.left, ev.clientY - r.top);
  const x = Math.max(0, Math.min(renderer.scene.width - 1, Math.round(world.x)));
  const y = Math.max(0, Math.min(renderer.scene.height - 1, Math.round(world.y)));
  player.setTarget(x, y, performance.now());
});

const KEY_STEPS = {
  ArrowUp: [0, -1], ArrowDown: [0, 1], ArrowLeft: [-1, 0], ArrowRight: [1, 0],
  w: [0, -1], s: [0, 1], a: [-1, 0], d: [1, 0],
};
const KEY_STEP_PIXELS = 16;

window.addEventListener('keydown', (ev) => {
  if (ev.target.tagName === 'INPUT') return;
  if (ev.key === '+' || ev.key === '=') { renderer.zoom = Math.min(4, renderer.zoom + 1); return; }
  if (ev.key === '-') { renderer.zoom = Math.max(1, renderer.zoom - 1); return; }
  const step = KEY_STEPS[ev.key];
  if (!step || !player) return;
  ev.preventDefault();
  // Steer from where we are heading, so holding a key walks continuously instead of
  // restarting from the current position each time.
  const from = player.target || player;
  player.setTarget(
    Math.max(0, Math.min((renderer.scene?.width ?? 1e9) - 1, from.x + step[0] * KEY_STEP_PIXELS)),
    Math.max(0, Math.min((renderer.scene?.height ?? 1e9) - 1, from.y + step[1] * KEY_STEP_PIXELS)),
    performance.now(),
  );
  player.dir = directionFromDelta(step[0], step[1], player.dir);
});

$('chatSend').onclick = () => {
  const message = $('chatText').value.trim();
  if (!message || !session) return;
  session.send('chat', { channel: ChatChannel.AREA, destId: 0, message });
  $('chatText').value = '';
};

$('url').value = `ws://${location.host}`;
$('loginBtn').onclick = doLogin;
$('createBtn').onclick = createCharacter;
$('refreshBtn').onclick = () => refreshCharacters().catch((e) => setStatus(e.message, 'err'));
resizeCanvas();
requestAnimationFrame(frame);

// Exposed for the Playwright smoke test in tools/ and for poking at state from the console.
window.__game = { renderer, source, get player() { return player; }, get units() { return units; },
  get session() { return session; } };
