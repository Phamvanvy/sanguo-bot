#!/usr/bin/env node
/*
 * WebSocket <-> TCP bridge for the self-hosted sanguo world server.
 *
 *   Browser  --WebSocket(binary)-->  this bridge  --TCP "UA" stream-->  world:7000
 *
 * Invariants (see plan G1):
 *   - 1 WebSocket connection == 1 TCP connection == 1 game session.
 *   - Pure byte passthrough in BOTH directions. The bridge does NOT parse UA frames;
 *     framing/codec lives in the browser client. TCP has no packet boundaries, so a
 *     browser WS message may carry a partial or multiple UA frames — that's fine, the
 *     client's accumulator handles it.
 *   - Close propagation: WS closes -> TCP destroyed, and TCP ends/errors -> WS closed.
 *   - Backpressure + a hard buffer cap in each direction so a stalled peer can't blow up memory.
 *
 * Config via env:
 *   BRIDGE_HOST   (default 0.0.0.0)   WS listen address
 *   BRIDGE_PORT   (default 8080)      WS listen port
 *   WORLD_HOST    (default 127.0.0.1) target TCP host (the world server)
 *   WORLD_PORT    (default 7000)      target TCP port
 *   MAX_BUFFER    (default 4194304)   per-direction buffered bytes before the session is killed
 *   STATIC_DIR    (default ../client) also serve this dir over HTTP on the same port so the
 *                                     browser client is same-origin with the WS; 'off' disables
 *   DATA_DIR      (default ../../selfhost/runtime/data) the server's game data directory.
 *                                     Only the client-asset trees are served, read-only,
 *                                     under /data/ (see asset-index.js); 'off' disables
 *   MAX_SESSIONS  (default 64)        concurrent WS sessions before new ones are refused
 *   MAX_PER_IP    (default 8)         concurrent WS sessions from one address
 *   RATE_BURST    (default 300)       HTTP requests one address may make back-to-back
 *   RATE_PER_SEC  (default 30)        …and the rate that budget refills at
 */
import net from 'node:net';
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { WebSocketServer } from 'ws';
import { areaIndex, resolveDataFile } from './asset-index.js';

const BRIDGE_HOST = process.env.BRIDGE_HOST || '0.0.0.0';
const BRIDGE_PORT = Number(process.env.BRIDGE_PORT || 8080);
const WORLD_HOST = process.env.WORLD_HOST || '127.0.0.1';
const WORLD_PORT = Number(process.env.WORLD_PORT || 7000);
const MAX_BUFFER = Number(process.env.MAX_BUFFER || 4 * 1024 * 1024);
const MAX_SESSIONS = Number(process.env.MAX_SESSIONS || 64);
const MAX_PER_IP = Number(process.env.MAX_PER_IP || 8);
const RATE_BURST = Number(process.env.RATE_BURST || 300);
const RATE_PER_SEC = Number(process.env.RATE_PER_SEC || 30);
const MAX_URL_LENGTH = 2048;

let sessionSeq = 0;
const log = (id, ...a) => console.log(`[bridge#${id}]`, ...a);

// ---- crude per-address limits ---------------------------------------------
// Not a security boundary — the bridge is meant to sit behind something real if it is ever
// exposed. This is here so one runaway tab (or one bored visitor) cannot spin the disk or
// exhaust the world server's session table.
const buckets = new Map();   // ip -> {tokens, at}
const wsPerIp = new Map();   // ip -> count
let wsCount = 0;

function rateLimited(ip) {
  const now = Date.now();
  let b = buckets.get(ip);
  if (!b) { b = { tokens: RATE_BURST, at: now }; buckets.set(ip, b); }
  b.tokens = Math.min(RATE_BURST, b.tokens + ((now - b.at) / 1000) * RATE_PER_SEC);
  b.at = now;
  if (b.tokens < 1) return true;
  b.tokens -= 1;
  return false;
}
// Buckets for addresses that stopped talking are full anyway; drop them so the map cannot grow.
setInterval(() => {
  const cutoff = Date.now() - 5 * 60_000;
  for (const [ip, b] of buckets) if (b.at < cutoff) buckets.delete(ip);
}, 60_000).unref();

// ---- static file serving (dev convenience) --------------------------------
// Sharing the port keeps the page same-origin with the WS endpoint, so the browser client
// needs no host configuration and no CORS. It is a dev server: local files, GET only.
const HERE = path.dirname(fileURLToPath(import.meta.url));
const STATIC_DIR = process.env.STATIC_DIR === 'off' ? null
  : path.resolve(HERE, process.env.STATIC_DIR || '../client');
const MIME = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8', '.json': 'application/json', '.png': 'image/png',
  '.mjs': 'text/javascript; charset=utf-8' };

// ---- game assets ----------------------------------------------------------
// The client decodes the original 2011 art itself (see web/client/src/assets), so it needs the
// raw files: an area's client.pkg, the .ctn animate sets and their .pip images. They are served
// straight out of the server's data directory — nothing is pre-converted, and nothing is
// written back. Plus /api/areas.json, the map id -> area directory lookup that would otherwise
// cost the browser 96 downloads (see asset-index.js).
const DATA_DIR = process.env.DATA_DIR === 'off' ? null
  : path.resolve(HERE, process.env.DATA_DIR || '../../selfhost/runtime/data');

const BASE_HEADERS = { 'x-content-type-options': 'nosniff' };

/** Send a file with an mtime/size ETag, answering a conditional request with 304. */
function sendFile(req, res, file, cacheControl) {
  fs.stat(file, (err, st) => {
    if (err || !st.isFile()) { res.writeHead(404, BASE_HEADERS).end('not found'); return; }
    const etag = `"${st.size.toString(16)}-${st.mtimeMs.toString(16)}"`;
    const headers = {
      ...BASE_HEADERS,
      'content-type': MIME[path.extname(file).toLowerCase()] || 'application/octet-stream',
      'cache-control': cacheControl,
      etag,
      'last-modified': st.mtime.toUTCString(),
    };
    if (req.headers['if-none-match'] === etag) { res.writeHead(304, headers).end(); return; }
    fs.readFile(file, (err2, data) => {
      if (err2) { res.writeHead(404, BASE_HEADERS).end('not found'); return; }
      res.writeHead(200, { ...headers, 'content-length': data.length });
      res.end(data);
    });
  });
}

function serveData(req, res, rel) {
  if (!DATA_DIR) { res.writeHead(404, BASE_HEADERS).end('data serving disabled'); return; }
  if (rel === '/areas.json') {
    let index;
    try {
      index = areaIndex(DATA_DIR);
    } catch (e) {
      console.error('[bridge] area index failed:', e.message);
      res.writeHead(500, BASE_HEADERS).end('area index failed: ' + e.message);
      return;
    }
    if (index.failures.length) console.warn('[bridge] area index warnings:', index.failures.join('; '));
    res.writeHead(200, { ...BASE_HEADERS, 'content-type': 'application/json', 'cache-control': 'public, max-age=3600' });
    res.end(JSON.stringify({ areas: index.areas, mapToArea: index.mapToArea }));
    return;
  }
  const file = resolveDataFile(DATA_DIR, rel);
  if (!file) { res.writeHead(403, BASE_HEADERS).end('forbidden'); return; }
  // The data directory is a frozen 2011 export: a file at a given path never changes, so the
  // browser should never re-download a 200 KB tileset it already has.
  sendFile(req, res, file, 'public, max-age=86400, immutable');
}

const httpServer = http.createServer((req, res) => {
  const ip = req.socket.remoteAddress || '?';
  if (req.method !== 'GET') { res.writeHead(405, BASE_HEADERS).end('method not allowed'); return; }
  if (!req.url || req.url.length > MAX_URL_LENGTH) { res.writeHead(414, BASE_HEADERS).end('uri too long'); return; }
  // GET only, so anything with a body is a client we do not understand.
  if (Number(req.headers['content-length'] || 0) > 0) { res.writeHead(413, BASE_HEADERS).end('payload too large'); return; }
  if (rateLimited(ip)) { res.writeHead(429, { ...BASE_HEADERS, 'retry-after': '1' }).end('slow down'); return; }
  let rel;
  try {
    rel = decodeURIComponent(new URL(req.url, 'http://x').pathname);
  } catch {
    res.writeHead(400, BASE_HEADERS).end('bad request');   // malformed percent-encoding
    return;
  }
  if (rel === '/data' || rel.startsWith('/data/')) { serveData(req, res, rel.slice(5) || '/'); return; }
  if (!STATIC_DIR) { res.writeHead(404, BASE_HEADERS).end('not found'); return; }
  const file = path.resolve(STATIC_DIR, '.' + (rel === '/' ? '/index.html' : rel));
  // Never serve outside STATIC_DIR, whatever the path contains.
  if (file !== STATIC_DIR && !file.startsWith(STATIC_DIR + path.sep)) { res.writeHead(403, BASE_HEADERS).end('forbidden'); return; }
  // The client is edited while the stack runs, so it must revalidate rather than be cached.
  sendFile(req, res, file, 'no-cache');
});

const wss = new WebSocketServer({ server: httpServer });

httpServer.listen(BRIDGE_PORT, BRIDGE_HOST, () => {
  console.log(`[bridge] WS listening ws://${BRIDGE_HOST}:${BRIDGE_PORT}  ->  tcp ${WORLD_HOST}:${WORLD_PORT}`);
  if (STATIC_DIR) console.log(`[bridge] serving ${STATIC_DIR} at http://${BRIDGE_HOST}:${BRIDGE_PORT}/`);
  if (DATA_DIR) console.log(`[bridge] serving game data ${DATA_DIR} at /data/`);
});
httpServer.on('error', (err) => {
  console.error('[bridge] server error:', err.message);
  process.exit(1);
});
wss.on('error', (err) => {
  console.error('[bridge] ws error:', err.message);
  process.exit(1);
});

wss.on('connection', (ws, req) => {
  const id = ++sessionSeq;
  const ip = req.socket.remoteAddress || '?';
  const peer = ip + ':' + req.socket.remotePort;

  // One WS session is one TCP session on the world server, and the world has a finite
  // session table — refuse here rather than let it be filled from a browser.
  const fromIp = wsPerIp.get(ip) || 0;
  if (wsCount >= MAX_SESSIONS || fromIp >= MAX_PER_IP) {
    log(id, `refusing ${peer}: ${wsCount}/${MAX_SESSIONS} sessions, ${fromIp}/${MAX_PER_IP} from this address`);
    try { ws.close(1013, 'too many sessions'); } catch {}
    return;
  }
  wsCount += 1;
  wsPerIp.set(ip, fromIp + 1);
  let released = false;
  const release = () => {
    if (released) return;
    released = true;
    wsCount -= 1;
    const n = (wsPerIp.get(ip) || 1) - 1;
    if (n > 0) wsPerIp.set(ip, n); else wsPerIp.delete(ip);
  };

  log(id, 'WS open from', peer, '-> connecting TCP', `${WORLD_HOST}:${WORLD_PORT}`);

  let closed = false;
  const tcp = net.connect({ host: WORLD_HOST, port: WORLD_PORT });
  tcp.setNoDelay(true);

  // ---- one place that tears everything down, idempotently ----
  const shutdown = (why) => {
    if (closed) return;
    closed = true;
    release();
    log(id, 'closing:', why);
    try { tcp.destroy(); } catch {}
    try { ws.close(); } catch {}
  };

  // ---- TCP -> WS ----
  tcp.on('data', (chunk) => {
    if (closed) return;
    // ws.send buffers internally; guard against an unbounded backlog if the browser stalls.
    if (ws.bufferedAmount > MAX_BUFFER) {
      shutdown(`WS send backlog ${ws.bufferedAmount} > ${MAX_BUFFER}`);
      return;
    }
    ws.send(chunk, { binary: true }, (err) => {
      if (err) shutdown('ws.send error: ' + err.message);
    });
    // Backpressure: if the WS side is slow to flush, pause reading from TCP.
    if (ws.bufferedAmount > MAX_BUFFER / 2) tcp.pause();
  });
  // Resume TCP reads once the WS buffer has drained. ws has no 'drain' event, so poll
  // cheaply on the same cadence the socket produces data; a small timer keeps it simple.
  const drainTimer = setInterval(() => {
    if (closed) return;
    if (tcp.isPaused() && ws.bufferedAmount < MAX_BUFFER / 4) tcp.resume();
  }, 20);

  tcp.on('connect', () => log(id, 'TCP connected'));
  tcp.on('end', () => shutdown('TCP end'));
  tcp.on('close', () => shutdown('TCP close'));
  tcp.on('error', (err) => shutdown('TCP error: ' + err.message));

  // ---- WS -> TCP ----
  ws.on('message', (data, isBinary) => {
    if (closed) return;
    // Normalize to a Buffer. Browsers send ArrayBuffer; ws gives us Buffer for binary.
    const buf = Buffer.isBuffer(data) ? data
      : Array.isArray(data) ? Buffer.concat(data)
      : Buffer.from(data);
    if (!isBinary) {
      // The protocol is binary-only; a text frame means a misbehaving client.
      shutdown('unexpected text frame');
      return;
    }
    const ok = tcp.write(buf);
    // Backpressure the browser: if the kernel/TCP buffer is full, stop reading WS.
    if (!ok && tcp.writableLength > MAX_BUFFER) {
      shutdown(`TCP send backlog ${tcp.writableLength} > ${MAX_BUFFER}`);
    }
  });

  ws.on('close', () => { clearInterval(drainTimer); shutdown('WS close'); });
  ws.on('error', (err) => { clearInterval(drainTimer); shutdown('WS error: ' + err.message); });
});

// Clean shutdown on Ctrl-C / container stop.
for (const sig of ['SIGINT', 'SIGTERM']) {
  process.on(sig, () => {
    console.log(`\n[bridge] ${sig}, closing`);
    wss.close(() => httpServer.close(() => process.exit(0)));
    setTimeout(() => process.exit(0), 1000).unref();
  });
}
