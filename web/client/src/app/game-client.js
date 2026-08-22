/*
 * The browser client: log in, pick a character, and play on the real map.
 *
 * The wire layer (net/), the asset decoders (assets/) and the renderer (game/) are unchanged
 * from G3b; what lives here is the shell around them — the boot/login/character screens and the
 * in-world HUD, laid out the way the game's own H5 client lays them out (portrait and bars top
 * left, place and action bar top right, walk pad bottom left, chat along the bottom).
 *
 * Two rules keep the HUD honest:
 *   - it only shows numbers the server actually sent us. ACTOR_LOGIN_SERVER carries hp/mp/exp/
 *     attributes/money, so those are real; there is no ported opcode that *updates* them yet,
 *     so they are labelled as "at login" rather than pretended to be live.
 *   - a feature whose opcodes are not ported says so, instead of drawing an empty bag.
 *
 * Movement is still client-led and server-checked: a click, a key or the pad sets a target, the
 * unit walks at Unit.SPEED (45 px/s), and a MOVE_CLIENT goes out every MOVE_INTERVAL_MS with the
 * position reached so far. The server re-runs its pathfinder on each one and silently drops what
 * it dislikes, so small steps are what keep us in sync with it.
 */
import { attachWebSocket } from '../net/session.js';
import { ChatChannel, UnitType } from '../net/protocol.js';
import { AssetSource } from '../game/asset-source.js';
import { MapScene } from '../game/map-scene.js';
import { AnimatedSprites } from '../game/sprite-set.js';
import { Renderer } from '../game/renderer.js';
import { UnitView } from '../game/unit-view.js';
import { directionFromDelta } from '../game/movement.js';
import { UiAssets, portraitCanvas, UI_RES, ABILITY, DIRECT_KEY, ICON, ABILITY_ICON, DPAD } from '../ui/ui-assets.js';
import { Minimap } from '../ui/minimap.js';
import { Joystick } from '../ui/joystick.js';
import { VMObserver, VM_KEY } from './vm-observer.js';

const $ = (id) => document.getElementById(id);
const show = (el, on) => { el.hidden = !on; };

/** How often a walking character reports in. 200 ms at 45 px/s is a ~9 px step. */
const MOVE_INTERVAL_MS = 200;
/** Units the server stops mentioning are dropped: "unit left" is not a ported opcode yet. */
const UNIT_TTL_MS = 30000;
/** The player character's art. Other players share it until equipment/sex are decoded. */
const PLAYER_CTN = 'client_pkg/Flash/male.ctn';
/** The 2011 splash art, used as the backdrop for every out-of-world screen. */
const SPLASH = 'client_pkg/Flash/menu480.png';
/** How far ahead of the character the walk pad aims. Small enough that the server accepts it. */
const PAD_LOOKAHEAD = 24;
/** peony/game/Unit.CLASS_1..4 */
const CLASSES = ['Võ tướng', 'Thích khách', 'Mưu sĩ', 'Phương sĩ'];
/** data/factions.xml */
const FACTIONS = { 0: 'Trung lập', 1: 'Ngụy', 2: 'Thục', 3: 'Ngô' };

const source = new AssetSource({ baseUrl: '/data' });
const ui = new UiAssets(source);
const renderer = new Renderer($('view'), { zoom: 2 });
const minimap = new Minimap($('minimap'));
const joystick = new Joystick($('pad'), $('padKnob'));

let session = null;
let clockOffset = 0;        // serverTime - Date.now(), so MOVE carries a server-ish clock
let player = null;          // UnitView for us
let playerSprites = null;
let actor = null;           // the ACTOR_LOGIN_SERVER body: the only real stats we have
let currentMapId = null;
let sceneLoading = false;
const units = new Map();    // instanceId -> UnitView (everyone except us)
let lastMoveSentAt = 0;
let lastSentPos = null;
/** G3d-f observer mode: the .etf UI VM watching the live session (null until in-world). */
let vmObserver = null;

/* --------------------------------------------------------------------- messaging */

function setStatus(text, cls = '') {
  $('status').textContent = text;
  $('status').className = cls;
}

function toast(text) {
  const div = document.createElement('div');
  div.textContent = text;
  $('toast').appendChild(div);
  setTimeout(() => div.remove(), 2800);
}

const MAX_ROWS = 200;
function appendRow(host, text, cls) {
  const div = document.createElement('div');
  div.className = 'row ' + cls;
  div.textContent = text;
  host.appendChild(div);
  while (host.childElementCount > MAX_ROWS) host.removeChild(host.firstChild);
  host.scrollTop = host.scrollHeight;
}

/** Player-facing messages: chat, hints, what just happened. */
const chatLine = (text, cls = '') => appendRow($('chatLog'), text, cls);
/** Protocol-facing messages. This is a port; the packet log stays, just out of the way. */
const devLine = (text, cls = '') => appendRow($('devLog'), text, cls);

/* ------------------------------------------------------------------------- boot */

async function boot() {
  const steps = [
    ['Đang tải giao diện…', () => Promise.all([
      ui.frameURL(DIRECT_KEY, DPAD.base, 1),
      ...Object.values(ICON).map((f) => ui.frameURL(UI_RES, f, 2)),
      ...Object.values(ABILITY_ICON).map((f) => ui.frameURL(ABILITY, f, 1)),
    ])],
    ['Đang tải danh mục bản đồ…', () => source.areaIndex()],
    // Loaded before the character list rather than at spawn, because the list draws each
    // character with this art.
    ['Đang tải hình nhân vật…', async () => { playerSprites = await loadPlayerSprites(); }],
  ];
  for (let i = 0; i < steps.length; i++) {
    const [label, run] = steps[i];
    $('bootMsg').textContent = label;
    setProgress(i / steps.length);
    try {
      await run();
    } catch (e) {
      // A missing decoration must not block the door: the game still plays without it.
      console.warn(`[boot] ${label} ${e.message}`);
      $('bootMsg').textContent = `${label} bỏ qua (${e.message})`;
    }
  }
  setProgress(1);
  await dressUi();
  show($('boot'), false);
  show($('login'), true);
  $('account').focus();
}

function setProgress(fraction) {
  const pct = Math.round(fraction * 100);
  $('bootFill').style.width = `${pct}%`;
  $('bootPct').textContent = `${pct}%`;
}

/** Hang the game's own art on the shell: splash backdrop, pad, action-bar icons. */
async function dressUi() {
  for (const el of [$('boot'), $('login'), $('select')]) {
    el.style.backgroundImage = `url(/data/${SPLASH})`;
  }
  ui.setBackground($('pad'), DIRECT_KEY, DPAD.base, 1);
  const icons = {
    character: ABILITY_ICON.person,
    bag: ABILITY_ICON.chest,
    quest: ABILITY_ICON.scroll,
    map: ABILITY_ICON.book,
    menu: ABILITY_ICON.crest,
  };
  for (const btn of document.querySelectorAll('#actionBar .act')) {
    const img = btn.querySelector('img');
    if (img) ui.setImage(img, ABILITY, icons[btn.dataset.panel], 1);
  }
}

/* ------------------------------------------------------------------- connection */

async function connect() {
  const url = $('url').value.trim();
  session = attachWebSocket(new WebSocket(url));

  session.on('unknown', (m) => devLine(`<- ${m.name} ${m.segment.length}B (chưa port)`, 'skip'));
  session.on('error', (m) => devLine(`!! lỗi giải mã ${m.name}: ${m.error.message}`, 'err'));
  session.on('fatal', (e) => {
    devLine(`!! FATAL ${e.message}`, 'err');
    setStatus('mất đồng bộ — tải lại trang', 'err');
    chatLine('Kết nối mất đồng bộ, hãy tải lại trang.', 'err');
  });
  session.on('close', () => {
    setStatus('đã ngắt kết nối', 'err');
    chatLine('Đã ngắt kết nối khỏi máy chủ.', 'err');
  });
  session.on('SYNC_TIME_SERVER', (b) => { clockOffset = b.serverTime - Date.now(); });
  session.on('CHAT_SERVER', (b) => chatLine(`${b.sourceName}: ${b.message}`, 'chat'));
  session.on('PUSH_HINT_SERVER', (b) => chatLine(b.hint, 'sys'));
  session.on('UNIT_MOVE_SERVER', onUnitMove);
  session.on('GOMAP_ALLOW_SERVER', (b) => {
    // Fires at spawn (handled by enterWorld, which awaits it) and again on every map change,
    // which is the case this handler is here for.
    if (!player) return;
    player.placeAt(b.x, b.y, performance.now());
    loadScene(b.mapId).catch((e) => chatLine('Không tải được bản đồ: ' + e.message, 'err'));
  });

  await session.ready;
  setStatus(`đã kết nối ${url}`, 'ok');
  // G3d-g: attach the ETF UI observer BEFORE any login packet goes out, so
  // the scripts see ACCOUNT_LOGIN / ACTOR_LIST / ACTOR_LOGIN / GOMAP_ALLOW
  // and the whole post-login init flood. While its boot loads, raw packets
  // wait in a bounded FIFO inside the observer and replay in order.
  startVMObserver();
}

async function doLogin() {
  try {
    $('loginBtn').disabled = true;
    setStatus('đang đăng nhập…');
    if (!session || session.closed) await connect();
    const account = await session.accountLogin({
      name: $('account').value.trim(),
      password: $('password').value,
    });
    setStatus(`đăng nhập: ${account.name}`, 'ok');
    await refreshCharacters();
    show($('login'), false);
    show($('select'), true);
  } catch (e) {
    setStatus('đăng nhập thất bại: ' + e.message, 'err');
  } finally {
    $('loginBtn').disabled = false;
  }
}

/* ------------------------------------------------------- character selection */

/** The server caps an account at 4 characters, and the H5 client shows all four slots. */
const MAX_SLOTS = 4;

async function refreshCharacters() {
  const actors = await session.listActors();
  const list = $('charList');
  list.textContent = '';
  for (const a of actors) list.appendChild(characterSlot(a));
  for (let i = actors.length; i < MAX_SLOTS; i++) list.appendChild(emptySlot());
  show($('createForm'), actors.length < MAX_SLOTS);
}

function characterSlot(a) {
  const slot = document.createElement('button');
  slot.className = 'slot';
  slot.onclick = () => enterWorld(a);

  const who = document.createElement('div');
  who.className = 'who';
  who.innerHTML = `<span class="clazz"></span><span class="nm"></span>`;
  who.querySelector('.clazz').textContent = CLASSES[a.clazz] ?? `Hệ ${a.clazz}`;
  who.querySelector('.nm').textContent = a.name;

  const art = document.createElement('div');
  art.className = 'art';
  if (playerSprites) {
    art.appendChild(portraitCanvas(playerSprites, { size: 84, zoom: 2, animateId: 0, head: false }));
  }

  const meta = document.createElement('div');
  meta.className = 'meta';
  meta.textContent = `Cấp: ${a.level}\nNước: ${FACTIONS[a.faction] ?? a.faction}\nBản đồ: ${a.mapName}`;
  meta.style.whiteSpace = 'pre-line';

  slot.append(who, art, meta);
  return slot;
}

function emptySlot() {
  const slot = document.createElement('div');
  slot.className = 'slot empty';
  slot.innerHTML = '<div class="who"><span class="nm">Ô trống</span></div><div class="art"></div>';
  const btn = document.createElement('button');
  btn.className = 'btn green';
  btn.textContent = 'Tạo';
  btn.onclick = () => { show($('createForm'), true); $('charName').focus(); };
  slot.appendChild(btn);
  return slot;
}

async function createCharacter() {
  try {
    const created = await session.createActor({
      name: $('charName').value.trim(),
      sex: Number($('sex').value),
      clazz: 0,
      faction: Number($('faction').value),
    });
    toast(`Đã tạo ${created.name}`);
    await refreshCharacters();
  } catch (e) {
    toast('Tạo nhân vật thất bại: ' + e.message);
  }
}

/* -------------------------------------------------------------------- the world */

async function enterWorld(actorSummary) {
  try {
    toast('Đang vào thế giới…');
    const entered = await session.enterWorld(actorSummary.id);
    actor = entered.actor;
    const spawn = entered.spawn;
    session.loadingFinished();
    session.send('syncTime', 0);

    playerSprites = playerSprites || await loadPlayerSprites();
    player = new UnitView({
      id: actor.id, x: spawn.x, y: spawn.y, name: actor.name, self: true, sprites: playerSprites,
    });
    await loadScene(spawn.mapId);

    show($('select'), false);
    show($('hud'), true);
    paintPortrait();
    updateVitals();
    chatLine(`Chào mừng ${actor.name} — cấp ${actor.level}, ${FACTIONS[actor.faction] ?? actor.faction}.`, 'ok');
    chatLine('Chạm vào bản đồ hoặc kéo phím tròn để đi. Bấm Chat để nói chuyện.', 'dim');
  } catch (e) {
    toast('Vào thế giới thất bại: ' + e.message);
    setStatus('vào thế giới thất bại: ' + e.message, 'err');
    devLine('!! ' + e.stack, 'err');
    show($('select'), true);
  }
}

async function loadPlayerSprites() {
  try {
    return new AnimatedSprites(await source.animateSet(PLAYER_CTN));
  } catch (e) {
    // Without sprites the renderer falls back to markers, which still proves the map and the
    // movement loop — better than refusing to enter the world.
    devLine(`!! không tải được hình nhân vật: ${e.message}`, 'err');
    return null;
  }
}

async function loadScene(mapId) {
  if (mapId === currentMapId || sceneLoading) return;
  sceneLoading = true;
  try {
    const scene = await MapScene.load(source, mapId);
    renderer.setScene(scene);
    fitZoomToMap(scene);
    currentMapId = mapId;
    units.clear();                              // unit ids are per map instance
    chatLine(`Đã vào ${scene.name}.`, 'sys');
    devLine(`map ${scene.id} "${scene.name}" ${scene.width}x${scene.height} — `
      + `${scene.stats.tiles} tiles, ${scene.stats.decor} decor, ${scene.stats.buildMs}ms`);
  } finally {
    sceneLoading = false;
  }
}

/**
 * Start the ETF UI VM in observer mode: GameSession keeps owning login and
 * movement; the VM only watches raw incoming packets and paints its windows
 * over the world once real server data makes a script open one.
 */
async function startVMObserver() {
  // one observer per session, ever — a reconnect makes a new session object
  if (!session || session.__vmObserverAttached) return;
  session.__vmObserverAttached = true;
  try {
    const obs = new VMObserver({ session, canvas: $('view'), log: devLine });
    vmObserver = obs;                           // stats visible while booting
    await obs.start();
    if (obs.error) {
      devLine(`!! VM observer không khởi động được: ${obs.error.message}`, 'err');
      vmObserver = null;
      return;
    }
    chatLine('Giao diện script ETF đang chạy ở chế độ quan sát.', 'dim');
  } catch (e) {
    devLine('!! VM observer: ' + e.message, 'err');
    vmObserver = null;
  }
}

/** Browser key -> handset key code the .etf scripts poll (Utilities.java). */
const VM_KEYMAP = {
  ArrowUp: VM_KEY.UP, ArrowDown: VM_KEY.DOWN, ArrowLeft: VM_KEY.LEFT, ArrowRight: VM_KEY.RIGHT,
  w: VM_KEY.UP, s: VM_KEY.DOWN, a: VM_KEY.LEFT, d: VM_KEY.RIGHT,
  Enter: VM_KEY.FIRE,
};
for (let n = 0; n <= 9; n++) VM_KEYMAP[String(n)] = VM_KEY.NUM0 + n;

function forwardVmKey(ev, down) {
  if (!vmObserver) return false;
  const code = VM_KEYMAP[ev.key];
  if (code === undefined) return false;
  if (down) vmObserver.keyDown(code); else vmObserver.keyUp(code);
  return true;
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

/* ---------------------------------------------------------------------- the HUD */

function paintPortrait() {
  const box = $('portrait');
  box.textContent = '';
  if (playerSprites) box.appendChild(portraitCanvas(playerSprites, { size: 50, zoom: 2 }));
}

/** The bars. Only ACTOR_LOGIN_SERVER feeds these, so they are a snapshot, not a live gauge. */
function updateVitals() {
  if (!actor) return;
  $('vitalName').innerHTML = '<span class="nm"></span><span class="lv"></span><span class="faction"></span>';
  $('vitalName').querySelector('.nm').textContent = actor.name;
  $('vitalName').querySelector('.lv').textContent = `Lv ${actor.level}`;
  $('vitalName').querySelector('.faction').textContent = FACTIONS[actor.faction] ?? '';

  fillBar('hpFill', 'hpText', actor.hp, actor.maxhp);
  fillBar('mpFill', 'mpText', actor.mp, actor.maxmp);

  const pct = actor.nextLevelExp > 0 ? Math.max(0, Math.min(1, actor.exp / actor.nextLevelExp)) : 0;
  $('expFill').style.width = `${(pct * 100).toFixed(1)}%`;
  $('expText').textContent = `KN ${actor.exp.toLocaleString('vi-VN')} / ${actor.nextLevelExp.toLocaleString('vi-VN')}`;
}

function fillBar(fillId, textId, value, max) {
  const pct = max > 0 ? Math.max(0, Math.min(1, value / max)) : 0;
  $(fillId).style.width = `${(pct * 100).toFixed(1)}%`;
  $(textId).textContent = `${value} / ${max}`;
}

function updateHud() {
  const scene = renderer.scene;
  $('place').textContent = scene ? `${scene.name} (${player.x}, ${player.y})` : '—';
  minimap.draw({
    scene, player, units: units.values(),
    view: { width: $('view').width, height: $('view').height },
    camera: renderer.camera, zoom: renderer.zoom,
  });
  if (!$('devPanel').hidden) {
    const s = session?.stats;
    $('devStats').textContent = `${fps.toFixed(0)} fps · zoom ${renderer.zoom}x · `
      + `${renderer.lastDrawn.decor} decor · ${units.size} units`
      + (s ? ` · rx ${s.received} decoded ${s.decoded} unported ${s.unknown} err ${s.decodeErrors} tx ${s.sent}` : '');
  }
}

/* --------------------------------------------------------------------- panels */

function openPanel(title, build) {
  const host = $('panels');
  host.textContent = '';
  const panel = document.createElement('div');
  panel.className = 'panel';
  panel.innerHTML = `<header><div class="plaque"></div><button class="close">✕</button></header>`
    + `<div class="body"></div>`;
  panel.querySelector('.plaque').textContent = title;
  panel.querySelector('.close').onclick = closePanel;
  build(panel.querySelector('.body'));
  host.appendChild(panel);
  show(host, true);
}

function closePanel() {
  $('panels').textContent = '';
  show($('panels'), false);
}

/** A feature whose opcodes are not ported says exactly that, and which ones are missing. */
function notPorted(body, what, opcodes) {
  const p = document.createElement('p');
  p.className = 'note';
  p.textContent = `${what} chưa dùng được: máy chủ có gửi dữ liệu, nhưng client chưa port các gói `
    + `${opcodes.join(', ')}. Chúng đang được ghi vào nhật ký gói tin (nút "Log").`;
  body.appendChild(p);
}

const PANELS = {
  character(body) {
    if (!actor) return;
    const dl = document.createElement('dl');
    dl.className = 'kv';
    const rows = [
      ['Tên', actor.name],
      ['Cấp', actor.level],
      ['Hệ phái', CLASSES[actor.clazz] ?? `Hệ ${actor.clazz}`],
      ['Nước', FACTIONS[actor.faction] ?? actor.faction],
      ['Bang hội', actor.guildName || '—'],
      ['Sinh lực', `${actor.hp} / ${actor.maxhp}`],
      ['Nội lực', `${actor.mp} / ${actor.maxmp}`],
      ['Sức', actor.strength], ['Nhanh', actor.agility],
      ['Thể', actor.stamina], ['Trí', actor.intellect],
      ['Điểm kỹ năng', actor.skillPoint], ['Điểm thuộc tính', actor.propertyPoint],
      ['Kinh nghiệm', `${actor.exp.toLocaleString('vi-VN')} / ${actor.nextLevelExp.toLocaleString('vi-VN')}`],
      ['Bạc', actor.money.toLocaleString('vi-VN')],
      ['Vị trí', renderer.scene ? `${renderer.scene.name} (${player.x}, ${player.y})` : '—'],
    ];
    for (const [k, v] of rows) {
      const dt = document.createElement('dt');
      dt.textContent = k;
      const dd = document.createElement('dd');
      dd.textContent = String(v);
      dl.append(dt, dd);
    }
    body.appendChild(dl);
    const note = document.createElement('p');
    note.className = 'note';
    note.style.marginTop = '10px';
    note.textContent = 'Số liệu lấy từ ACTOR_LOGIN lúc vào game. Gói cập nhật trực tiếp '
      + '(SYNC_PLAYER_SERVER) chưa được port nên chúng không đổi trong lúc chơi.';
    body.appendChild(note);
  },

  bag(body) { notPorted(body, 'Hành trang', ['BAG_*', 'UNIT_DETAIL_SERVER(194)']); },

  quest(body) {
    notPorted(body, 'Nhiệm vụ', ['AREAQUEST_INFO_SERVER(161)', 'QUEST_START_ADDED_SERVER(125)']);
  },

  map(body) {
    const canvas = document.createElement('canvas');
    canvas.width = 520;
    canvas.height = 520;
    canvas.style.cssText = 'width:100%;height:auto;border-radius:8px;cursor:pointer;background:#120a04';
    body.appendChild(canvas);
    const big = new Minimap(canvas);
    const paint = () => big.draw({
      scene: renderer.scene, player, units: units.values(),
      view: { width: $('view').width, height: $('view').height },
      camera: renderer.camera, zoom: renderer.zoom,
    });
    paint();
    const timer = setInterval(() => ($('panels').hidden ? clearInterval(timer) : paint()), 250);
    canvas.onclick = (ev) => {
      const r = canvas.getBoundingClientRect();
      const world = big.toWorld(
        (ev.clientX - r.left) * (canvas.width / r.width),
        (ev.clientY - r.top) * (canvas.height / r.height),
      );
      if (world) { player.setTarget(world.x, world.y, performance.now()); closePanel(); }
    };
    const note = document.createElement('p');
    note.className = 'note';
    note.textContent = 'Chạm vào bản đồ để đi tới. Nhân vật đi bộ từng bước — máy chủ kiểm tra '
      + 'đường đi nên quãng dài có thể bị chặn ở vật cản.';
    body.appendChild(note);
  },

  menu(body) {
    const wrap = document.createElement('div');
    wrap.style.cssText = 'display:grid;gap:10px';
    const add = (label, fn) => {
      const b = document.createElement('button');
      b.className = 'btn wide';
      b.textContent = label;
      b.onclick = fn;
      wrap.appendChild(b);
    };
    add('Phóng to / thu nhỏ', cycleZoom);
    add('Ẩn / hiện tên nhân vật', toggleNames);
    add('Nhật ký gói tin', () => { toggleDev(); closePanel(); });
    add('Đăng xuất', () => location.reload());
    body.appendChild(wrap);
    const note = document.createElement('p');
    note.className = 'note';
    note.style.marginTop = '10px';
    note.textContent = 'Bàn phím: WASD / mũi tên để đi, +/− để phóng, F1 mở nhật ký, Enter để chat.';
    body.appendChild(note);
  },
};

/* ---------------------------------------------------------------------- input */

/** Set once the player picks a zoom, so a map change stops overriding their choice. */
let zoomChosen = false;

/**
 * The art was drawn for a 240x320 phone, so on a desktop window a small map wants more zoom
 * than a big one. Fit the map *inside* the viewport rather than covering it: covering would
 * push the camera hard against the map edge and leave the character pinned to a corner, which
 * is worse than the letterbox it avoids. 2x stays the floor — below that the art is unreadable.
 */
function fitZoomToMap(scene) {
  if (zoomChosen) return;
  const contain = Math.min($('view').width / scene.width, $('view').height / scene.height);
  setZoom(Math.max(2, Math.min(4, Math.floor(contain))), false);
}

function setZoom(z, chosen = true) {
  renderer.zoom = z;
  if (chosen) zoomChosen = true;
  $('btnZoom').textContent = `${z}x`;
}

function cycleZoom() { setZoom(renderer.zoom >= 4 ? 1 : renderer.zoom + 1); }

function toggleNames() {
  renderer.showNames = !renderer.showNames;
  $('btnNames').style.opacity = renderer.showNames ? '1' : '.5';
}

function toggleDev() { show($('devPanel'), $('devPanel').hidden); }

function openChat(on) {
  $('chatRow').classList.toggle('open', on);
  if (on) $('chatText').focus();
}

function sendChat() {
  const message = $('chatText').value.trim();
  if (!message || !session) return;
  session.send('chat', { channel: ChatChannel.AREA, destId: 0, message });
  $('chatText').value = '';
}

/** Clamp a world point to the loaded map, so a target can never leave it. */
function clampToMap(x, y) {
  const s = renderer.scene;
  return {
    x: Math.max(0, Math.min((s?.width ?? 1e9) - 1, Math.round(x))),
    y: Math.max(0, Math.min((s?.height ?? 1e9) - 1, Math.round(y))),
  };
}

$('view').addEventListener('click', (ev) => {
  if (!player || !renderer.scene) return;
  const r = $('view').getBoundingClientRect();
  const world = renderer.toWorld(ev.clientX - r.left, ev.clientY - r.top);
  const p = clampToMap(world.x, world.y);
  player.setTarget(p.x, p.y, performance.now());
});

$('minimap').addEventListener('click', (ev) => {
  if (!player) return;
  const r = $('minimap').getBoundingClientRect();
  const world = minimap.toWorld(
    (ev.clientX - r.left) * ($('minimap').width / r.width),
    (ev.clientY - r.top) * ($('minimap').height / r.height),
  );
  if (world) player.setTarget(world.x, world.y, performance.now());
});

const KEY_STEPS = {
  ArrowUp: [0, -1], ArrowDown: [0, 1], ArrowLeft: [-1, 0], ArrowRight: [1, 0],
  w: [0, -1], s: [0, 1], a: [-1, 0], d: [1, 0],
};
const KEY_STEP_PIXELS = 16;

window.addEventListener('keydown', (ev) => {
  forwardVmKey(ev, true);
  if (ev.key === 'F1') { ev.preventDefault(); toggleDev(); return; }
  if (ev.key === 'Escape') { closePanel(); openChat(false); return; }
  if (ev.target.tagName === 'INPUT' || ev.target.tagName === 'SELECT') {
    if (ev.key === 'Enter' && ev.target.id === 'chatText') sendChat();
    if (ev.key === 'Enter' && ev.target.id === 'password') $('loginBtn').click();
    return;
  }
  if (ev.key === 'Enter' && player) { openChat(true); return; }
  if (ev.key === '+' || ev.key === '=') { cycleZoom(); return; }
  if (ev.key === '-') { setZoom(Math.max(1, renderer.zoom - 1)); return; }
  const step = KEY_STEPS[ev.key];
  if (!step || !player) return;
  ev.preventDefault();
  // Steer from where we are heading, so holding a key walks continuously instead of
  // restarting from the current position each time.
  const from = player.target || player;
  const p = clampToMap(from.x + step[0] * KEY_STEP_PIXELS, from.y + step[1] * KEY_STEP_PIXELS);
  player.setTarget(p.x, p.y, performance.now());
  player.dir = directionFromDelta(step[0], step[1], player.dir);
});

// key-up feeds the VM's key state too (scripts poll pressed/released pairs)
window.addEventListener('keyup', (ev) => { forwardVmKey(ev, false); });

for (const btn of document.querySelectorAll('#actionBar .act')) {
  btn.onclick = () => {
    const key = btn.dataset.panel;
    openPanel(btn.querySelector('span:last-child').textContent, PANELS[key]);
  };
}
$('panels').addEventListener('click', (ev) => { if (ev.target === $('panels')) closePanel(); });

$('btnZoom').onclick = cycleZoom;
$('btnNames').onclick = toggleNames;
$('btnDev').onclick = toggleDev;
$('btnChat').onclick = () => openChat(!$('chatRow').classList.contains('open'));
$('chatSend').onclick = sendChat;
$('loginBtn').onclick = doLogin;
$('createBtn').onclick = createCharacter;
$('refreshBtn').onclick = () => refreshCharacters().catch((e) => toast(e.message));
$('selectBack').onclick = () => location.reload();

/* ------------------------------------------------------------------ game loop */

let lastFrameMs = 0;
let fps = 0;

function frame(nowMs) {
  requestAnimationFrame(frame);
  const dt = lastFrameMs ? Math.min(nowMs - lastFrameMs, 250) : 0;
  lastFrameMs = nowMs;
  fps += (1000 / Math.max(dt, 1) - fps) * 0.05;

  resizeCanvas();
  if (!player) return;

  // The pad reports a direction, so keep the target a short way ahead of the character while
  // it is held: that is what turns "held down" into a continuous walk.
  const v = joystick.vector;
  if (v) {
    const p = clampToMap(player.x + v.x * PAD_LOOKAHEAD, player.y + v.y * PAD_LOOKAHEAD);
    player.setTarget(p.x, p.y, nowMs);
  }

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

  // The scripts' own frame: advance time first, then paint their windows over
  // the world. A script window covering the screen IS the game's original UI.
  if (vmObserver) {
    vmObserver.cycle();
    vmObserver.draw($('view').getContext('2d'));
  }
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
  const dw = Math.floor(window.innerWidth);
  const dh = Math.floor(window.innerHeight);
  if (c.width !== dw || c.height !== dh) { c.width = dw; c.height = dh; }
}

// The bridge serves this page, so its WS endpoint is this origin — whatever host, port or
// scheme the page was actually opened on (a TLS proxy in front means wss).
$('url').value = `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}`;
resizeCanvas();
requestAnimationFrame(frame);
boot();

// Exposed for the Playwright smoke test in tools/ and for poking at state from the console.
window.__game = { renderer, source, ui, minimap, joystick,
  get player() { return player; }, get units() { return units; },
  get actor() { return actor; }, get session() { return session; },
  get vmObserver() { return vmObserver; } };
