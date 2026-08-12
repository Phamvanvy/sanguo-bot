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
 */
import net from 'node:net';
import { WebSocketServer } from 'ws';

const BRIDGE_HOST = process.env.BRIDGE_HOST || '0.0.0.0';
const BRIDGE_PORT = Number(process.env.BRIDGE_PORT || 8080);
const WORLD_HOST = process.env.WORLD_HOST || '127.0.0.1';
const WORLD_PORT = Number(process.env.WORLD_PORT || 7000);
const MAX_BUFFER = Number(process.env.MAX_BUFFER || 4 * 1024 * 1024);

let sessionSeq = 0;
const log = (id, ...a) => console.log(`[bridge#${id}]`, ...a);

const wss = new WebSocketServer({ host: BRIDGE_HOST, port: BRIDGE_PORT });

wss.on('listening', () => {
  console.log(`[bridge] WS listening ws://${BRIDGE_HOST}:${BRIDGE_PORT}  ->  tcp ${WORLD_HOST}:${WORLD_PORT}`);
});
wss.on('error', (err) => {
  console.error('[bridge] server error:', err.message);
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
  process.on(sig, () => { console.log(`\n[bridge] ${sig}, closing`); wss.close(() => process.exit(0)); });
}
