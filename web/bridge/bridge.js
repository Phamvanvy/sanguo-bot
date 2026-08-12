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
 *   DATA_DIR      (default ../../selfhost/runtime/data) the server's game data directory,
 *                                     served read-only under /data/ so the client can decode
 *                                     the original art itself; 'off' disables
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

let sessionSeq = 0;
const log = (id, ...a) => console.log(`[bridge#${id}]`, ...a);

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

function serveData(req, res, rel) {
  if (!DATA_DIR) { res.writeHead(404).end('data serving disabled'); return; }
  if (rel === '/areas.json') {
    let index;
    try {
      index = areaIndex(DATA_DIR);
    } catch (e) {
      console.error('[bridge] area index failed:', e.message);
      res.writeHead(500).end('area index failed: ' + e.message);
      return;
    }
    if (index.failures.length) console.warn('[bridge] area index warnings:', index.failures.join('; '));
    res.writeHead(200, { 'content-type': 'application/json' });
    res.end(JSON.stringify({ areas: index.areas, mapToArea: index.mapToArea }));
    return;
  }
  const file = resolveDataFile(DATA_DIR, rel);
  if (!file) { res.writeHead(403).end('forbidden'); return; }
  fs.readFile(file, (err, data) => {
    if (err) { res.writeHead(404).end('not found'); return; }
    res.writeHead(200, {
      'content-type': MIME[path.extname(file).toLowerCase()] || 'application/octet-stream',
      // The data directory is a frozen 2011 export; re-downloading a 200 KB tileset on every
      // map change would be pure waste.
      'cache-control': 'public, max-age=86400',
    });
    res.end(data);
  });
}

const httpServer = http.createServer((req, res) => {
  if (req.method !== 'GET') { res.writeHead(405).end('method not allowed'); return; }
  const rel = decodeURIComponent(new URL(req.url, 'http://x').pathname);
  if (rel === '/data' || rel.startsWith('/data/')) { serveData(req, res, rel.slice(5) || '/'); return; }
  if (!STATIC_DIR) { res.writeHead(404).end('not found'); return; }
  const file = path.resolve(STATIC_DIR, '.' + (rel === '/' ? '/index.html' : rel));
  // Never serve outside STATIC_DIR, whatever the path contains.
  if (file !== STATIC_DIR && !file.startsWith(STATIC_DIR + path.sep)) { res.writeHead(403).end('forbidden'); return; }
  fs.readFile(file, (err, data) => {
    if (err) { res.writeHead(404).end('not found'); return; }
    res.writeHead(200, { 'content-type': MIME[path.extname(file).toLowerCase()] || 'application/octet-stream' });
    res.end(data);
  });
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
  const peer = req.socket.remoteAddress + ':' + req.socket.remotePort;
  log(id, 'WS open from', peer, '-> connecting TCP', `${WORLD_HOST}:${WORLD_PORT}`);

  let closed = false;
  const tcp = net.connect({ host: WORLD_HOST, port: WORLD_PORT });
  tcp.setNoDelay(true);

  // ---- one place that tears everything down, idempotently ----
  const shutdown = (why) => {
    if (closed) return;
    closed = true;
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
