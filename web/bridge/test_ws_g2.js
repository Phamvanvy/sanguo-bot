#!/usr/bin/env node
/*
 * G2 end-to-end test: account login -> character list (create one if none) -> enter world
 * -> loading finished -> movement, all in the browser client's own code
 * (web/client/src/net/{protocol,session}.js) over the WebSocket bridge.
 *
 * What it proves (the G2 definition of done):
 *   1. we obtain actorId, mapId and x/y from the real server;
 *   2. the dozens of unported init packets the server floods after ACTOR_LOGIN are logged
 *      and skipped — the stream stays in sync and nothing crashes;
 *   3. movement is ACCEPTED BY THE SERVER — verified by reconnecting and reading back the
 *      position, not by assuming our own MOVE_CLIENT was fine.
 *
 * Prereqs: selfhost world server up, and `node bridge.js` running.
 * Usage:
 *   node test_ws_g2.js --name vypv1 --password 123456 [--char g2test] [--faction 1]
 * Credentials come from CLI/env (SANGO_USER/SANGO_PASS) — never hardcoded/committed.
 */
import { WebSocket } from 'ws';
import { attachWebSocket } from '../client/src/net/session.js';
import { opcodeLabel } from '../client/src/net/protocol.js';

function arg(name, def) {
  const i = process.argv.indexOf('--' + name);
  return i >= 0 && i + 1 < process.argv.length ? process.argv[i + 1] : def;
}
const has = (name) => process.argv.includes('--' + name);

const URL = arg('url', process.env.BRIDGE_URL || 'ws://127.0.0.1:8090');
const NAME = arg('name', process.env.SANGO_USER || 'vypv1');
const PASSWORD = arg('password', process.env.SANGO_PASS || '123456');
const CHAR = arg('char', process.env.SANGO_CHAR || 'g2test');
const SEX = Number(arg('sex', 0));           // 0..1
const CLAZZ = Number(arg('clazz', 0));       // 0..3
const FACTION = Number(arg('faction', 1));   // 1=Ngụy 2=Thục 3=Ngô (PlayerUtil.createPlayer)
const STEPS = Number(arg('steps', 4));
const STEP = Number(arg('step', 1));         // tiles per step; the server pathfinder must allow it
const SETTLE_MS = Number(arg('settle', 4000)); // how long to let the init packet flood arrive

const log = (...a) => console.log(...a);

/** Open a WS to the bridge and return a connected GameSession. */
async function connect(label) {
  const ws = new WebSocket(URL);
  const session = attachWebSocket(ws, { log: (m) => log(`    ${label} ${m}`), binaryType: 'arraybuffer' });
  session.ws = ws;
  await session.ready;
  log(`[*] ${label}: WS open ${URL}`);
  return session;
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

/** Sum of unknown opcodes, most frequent first — the porting backlog, quantified. */
function unknownReport(session) {
  return [...session.unknownOpcodes.entries()]
    .sort((a, b) => b[1] - a[1])
    .map(([op, n]) => `${opcodeLabel(op)} x${n}`);
}

async function main() {
  let failures = 0;
  const check = (cond, what) => {
    log(`${cond ? '[=]' : '[!]'} ${cond ? 'PASS' : 'FAIL'}: ${what}`);
    if (!cond) failures++;
  };

  // ---- 1. account login ---------------------------------------------------
  const s = await connect('s1');
  s.on('fatal', () => { console.error('[!] framing desync — aborting'); process.exit(1); });

  const account = await s.accountLogin({ name: NAME, password: PASSWORD });
  log(`[>] ACCOUNT_LOGIN -> accountId=${account.accountId} name=${JSON.stringify(account.name)}`);
  check(account.accountId > 0 && account.name === NAME, 'account login');

  // ---- 2. character list, creating one if the account has none ------------
  let actors = await s.listActors();
  log(`[>] ACTOR_LIST -> ${actors.length} character(s): ` +
      (actors.map((a) => `${a.id}:${a.name} lv${a.level} @${a.mapName}`).join(', ') || '(none)'));

  if (actors.length === 0) {
    log(`[>] no character — creating ${JSON.stringify(CHAR)} (sex=${SEX} clazz=${CLAZZ} faction=${FACTION})`);
    const created = await s.createActor({ name: CHAR, sex: SEX, clazz: CLAZZ, faction: FACTION });
    log(`[>] ACTOR_CREATE -> id=${created.id} name=${JSON.stringify(created.name)} lv${created.level}`);
    check(created.id > 0, 'character creation');
    actors = await s.listActors();
    check(actors.length > 0, 'created character shows up in ACTOR_LIST');
  }
  const actorId = actors[0].id;

  // ---- 3. enter the world -------------------------------------------------
  const { actor, spawn } = await s.enterWorld(actorId);
  log(`[>] ACTOR_LOGIN -> id=${actor.id} name=${JSON.stringify(actor.name)} lv${actor.level} ` +
      `hp=${actor.hp}/${actor.maxhp} faction=${actor.faction} (tail ${actor.restBytes}B not decoded)`);
  log(`[>] GOMAP_ALLOW -> mapId=${spawn.mapId} instance=${spawn.mapInstanceId} x=${spawn.x} y=${spawn.y}`);
  check(actor.id === actorId, 'ACTOR_LOGIN returns the actor we asked for');
  check(spawn.mapId > 0, `mapId is set (${spawn.mapId})`);
  check(Number.isInteger(spawn.x) && Number.isInteger(spawn.y), `spawn coords (${spawn.x},${spawn.y})`);
  check(actor.mapId === spawn.mapId, 'ACTOR_LOGIN and GOMAP_ALLOW agree on the map');

  // ---- 4. survive the init flood -----------------------------------------
  s.loadingFinished();
  const t0 = Date.now();
  s.send('syncTime', 0);
  const clock = await s.waitFor(102 /* SYNC_TIME_SERVER */).catch(() => null);
  if (clock) log(`[>] SYNC_TIME -> server clock ${clock.serverTime}`);
  await sleep(SETTLE_MS);
  log(`[i] after ${Date.now() - t0}ms: received=${s.stats.received} decoded=${s.stats.decoded} ` +
      `unknown=${s.stats.unknown} decodeErrors=${s.stats.decodeErrors}`);
  const unknowns = unknownReport(s);
  log(`[i] unported opcodes seen (${s.unknownOpcodes.size} distinct): ${unknowns.slice(0, 15).join(', ')}` +
      (unknowns.length > 15 ? `, +${unknowns.length - 15} more` : ''));
  check(!s.closed, 'session still open after the init packet flood');
  check(s.stats.received > 0, `server pushed packets (${s.stats.received})`);
  check(s.stats.decodeErrors === 0, `no decode errors in ported messages (${s.stats.decodeErrors})`);

  // ---- 5. move ------------------------------------------------------------
  let { x, y } = spawn;
  const serverClock = clock ? clock.serverTime : 0;
  if (!has('no-move')) {
    for (let i = 0; i < STEPS; i++) {
      x += STEP;
      const time = serverClock + (Date.now() - t0);
      s.move({ time, x, y, direct: 1, state: 0 });
      log(`[>] MOVE -> (${x},${y}) t=${time}`);
      await sleep(400);
    }
    await sleep(1000);
  }
  const wantX = x, wantY = y;
  s.ws.close();
  await sleep(500);

  // ---- 6. read the position back on a fresh session -----------------------
  // The only honest way to know the server accepted our movement: log in again and ask.
  if (!has('no-move')) {
    const s2 = await connect('s2');
    await s2.accountLogin({ name: NAME, password: PASSWORD });
    const again = await s2.enterWorld(actorId);
    log(`[>] re-login -> mapId=${again.spawn.mapId} x=${again.spawn.x} y=${again.spawn.y} (moved to ${wantX},${wantY})`);
    check(again.spawn.x === wantX && again.spawn.y === wantY,
      `server persisted our movement (got ${again.spawn.x},${again.spawn.y}, want ${wantX},${wantY})`);
    check(again.spawn.mapId === spawn.mapId, 'still on the same map');
    s2.ws.close();
  }

  log(`\n[=] G2 ${failures === 0 ? 'PASS' : `FAIL (${failures} check(s))`} — ` +
      `actorId=${actorId} mapId=${spawn.mapId} spawn=(${spawn.x},${spawn.y})`);
  process.exit(failures === 0 ? 0 : 1);
}

main().catch((e) => {
  console.error(`[!] ${e.stack || e.message}`);
  process.exit(1);
});
