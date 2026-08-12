/*
 * G2 browser client — no rendering, just the flow: account login -> character list/create
 * -> enter world -> move with the arrow keys, with a live packet log.
 *
 * This is the browser twin of web/bridge/test_ws_g2.js and runs the exact same modules
 * (session.js / protocol.js / ua-*.js). G3 will replace the placeholder canvas with real
 * map + sprite rendering; the wire layer below it does not change.
 */
import { attachWebSocket } from '../net/session.js';
import { opcodeLabel, ChatChannel } from '../net/protocol.js';
import { OpCode } from '../net/opcodes.js';

const $ = (id) => document.getElementById(id);
const show = (el, on) => { el.hidden = !on; };

let session = null;
let clockOffset = 0;          // serverTime - Date.now(), so MOVE carries a server-ish clock
let actorId = null;
const others = new Map();     // instanceId -> {x, y, type} seen via UNIT_MOVE_SERVER

// ---------------------------------------------------------------- packet log

const logEl = $('log');
const MAX_LOG_ROWS = 300;

function logLine(text, cls = '') {
  const div = document.createElement('div');
  div.className = 'row ' + cls;
  div.textContent = text;
  logEl.appendChild(div);
  while (logEl.childElementCount > MAX_LOG_ROWS) logEl.removeChild(logEl.firstChild);
  logEl.scrollTop = logEl.scrollHeight;
}

function refreshStats() {
  if (!session) return;
  const s = session.stats;
  $('stats').textContent =
    `rx ${s.received} · decoded ${s.decoded} · unported ${s.unknown} (${session.unknownOpcodes.size} distinct) · decode errors ${s.decodeErrors} · tx ${s.sent}`;
}

// ---------------------------------------------------------------- connection

async function connect() {
  const url = $('url').value.trim();
  const ws = new WebSocket(url);
  // No `log:` option — the handlers below already render every packet; passing one too
  // would print unknown/error packets twice.
  session = attachWebSocket(ws);

  session.on('packet', (m) => { logLine(`<- ${m.name} ${m.segment.length}B`, 'in'); refreshStats(); });
  session.on('unknown', (m) => { logLine(`<- ${m.name} ${m.segment.length}B (unported, skipped)`, 'skip'); refreshStats(); });
  session.on('error', (m) => { logLine(`!! decode error in ${m.name}: ${m.error.message}`, 'err'); refreshStats(); });
  session.on('fatal', (e) => { logLine(`!! FATAL ${e.message}`, 'err'); setStatus('desynced — reload', 'err'); });
  session.on('close', () => { setStatus('disconnected', 'err'); show($('world'), false); });
  session.on('SYNC_TIME_SERVER', (b) => { clockOffset = b.serverTime - Date.now(); });
  session.on('CHAT_SERVER', (b) => logLine(`   chat[${b.channel}] ${b.sourceName}: ${b.message}`, 'chat'));
  session.on('PUSH_HINT_SERVER', (b) => logLine(`   hint: ${b.hint}`, 'chat'));
  session.on('UNIT_MOVE_SERVER', (b) => {
    if (b.x !== undefined) others.set(b.instanceId, { x: b.x, y: b.y, type: b.unitType });
    draw();
  });

  await session.ready;
  setStatus(`connected to ${url}`, 'ok');
  return session;
}

function setStatus(text, cls = '') {
  $('status').textContent = text;
  $('status').className = cls;
}

// ---------------------------------------------------------------- the G2 flow

async function doLogin() {
  try {
    $('loginBtn').disabled = true;
    if (!session || session.closed) await connect();
    const account = await session.accountLogin({
      name: $('account').value.trim(),
      password: $('password').value,
    });
    setStatus(`logged in as ${account.name} (accountId ${account.accountId})`, 'ok');
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
  if (actors.length === 0) list.textContent = 'No characters on this account yet — create one below.';
  for (const a of actors) {
    const btn = document.createElement('button');
    btn.textContent = `${a.name} · lv${a.level} · ${a.mapName}`;
    btn.onclick = () => enterWorld(a.id);
    list.appendChild(btn);
  }
  show($('createForm'), actors.length < 4);   // server caps an account at 4 characters
}

async function createCharacter() {
  try {
    const created = await session.createActor({
      name: $('charName').value.trim(),
      sex: Number($('sex').value),
      clazz: Number($('clazz').value),
      faction: Number($('faction').value),
    });
    setStatus(`created ${created.name} (id ${created.id})`, 'ok');
    await refreshCharacters();
  } catch (e) {
    setStatus('create failed: ' + e.message, 'err');
  }
}

async function enterWorld(id) {
  try {
    setStatus('entering world…');
    const { actor, spawn } = await session.enterWorld(id);
    actorId = actor.id;
    session.loadingFinished();
    session.send('syncTime', 0);
    show($('characters'), false);
    show($('world'), true);
    $('who').textContent = `${actor.name} · lv${actor.level} · id ${actor.id}`;
    setStatus(`in world on map ${spawn.mapId}`, 'ok');
    updatePos();
    draw();
    window.addEventListener('keydown', onKey);
  } catch (e) {
    setStatus('enter world failed: ' + e.message, 'err');
  }
}

function updatePos() {
  const p = session.position;
  $('pos').textContent = p ? `map ${p.mapId} (instance ${p.mapInstanceId ?? '-'}) · x ${p.x} · y ${p.y}` : '—';
}

// ---------------------------------------------------------------- movement

const DIRECTIONS = {   // Unit.DIRECT_* — the server clamps direct to 0..3
  ArrowUp: [0, -1, 3], ArrowDown: [0, 1, 0], ArrowLeft: [-1, 0, 1], ArrowRight: [1, 0, 2],
  w: [0, -1, 3], s: [0, 1, 0], a: [-1, 0, 1], d: [1, 0, 2],
};

function onKey(ev) {
  const dir = DIRECTIONS[ev.key];
  if (!dir || !session || session.closed) return;
  ev.preventDefault();
  const step = Number($('step').value) || 1;
  const p = session.position;
  const x = p.x + dir[0] * step, y = p.y + dir[1] * step;
  // The server rejects a client time later than its own clock + 3s, so track the offset
  // learned from SYNC_TIME rather than sending Date.now().
  session.move({ time: Date.now() + clockOffset, x, y, direct: dir[2], state: 0 });
  logLine(`-> MOVE (${x},${y})`, 'out');
  updatePos();
  draw();
  refreshStats();
}

$('chatSend').onclick = () => {
  const message = $('chatText').value.trim();
  if (!message) return;
  session.send('chat', { channel: ChatChannel.AREA, destId: 0, message });
  logLine(`-> CHAT ${message}`, 'out');
  $('chatText').value = '';
};

// ---------------------------------------------------------------- placeholder view
// Deliberately NOT a game renderer: a dot for us, dots for whatever units the server tells
// us about. Enough to see that movement and unit updates are real. Real map/sprite
// rendering is G3 (see the plan's asset spike).

function draw() {
  const cv = $('view');
  const ctx = cv.getContext('2d');
  const p = session?.position;
  ctx.fillStyle = '#111'; ctx.fillRect(0, 0, cv.width, cv.height);
  if (!p) return;
  const cx = cv.width / 2, cy = cv.height / 2, scale = 4;
  ctx.strokeStyle = '#243'; ctx.beginPath();
  for (let g = -10; g <= 10; g++) {
    ctx.moveTo(cx + g * 10 * scale, 0); ctx.lineTo(cx + g * 10 * scale, cv.height);
    ctx.moveTo(0, cy + g * 10 * scale); ctx.lineTo(cv.width, cy + g * 10 * scale);
  }
  ctx.stroke();
  for (const [id, u] of others) {
    if (id === actorId) continue;
    ctx.fillStyle = u.type === 1 ? '#6cf' : '#fa4';
    ctx.fillRect(cx + (u.x - p.x) * scale - 3, cy + (u.y - p.y) * scale - 3, 6, 6);
  }
  ctx.fillStyle = '#7f7';
  ctx.beginPath(); ctx.arc(cx, cy, 5, 0, Math.PI * 2); ctx.fill();
  ctx.fillStyle = '#888'; ctx.font = '11px monospace';
  ctx.fillText(`${p.x},${p.y}`, cx + 8, cy - 8);
}

// ---------------------------------------------------------------- wiring

$('url').value = `ws://${location.host}`;
$('loginBtn').onclick = doLogin;
$('createBtn').onclick = createCharacter;
$('refreshBtn').onclick = () => refreshCharacters().catch((e) => setStatus(e.message, 'err'));
setInterval(() => { if (session && !session.closed) refreshStats(); }, 1000);
draw();
