/*
 * GameSession — the G2 client state machine on top of the wire layer.
 *
 *   bytes in  -> UAFrameAccumulator -> decodeSegment -> event handlers / pending requests
 *   bytes out <- frame(encode.x(...))
 *
 * Transport-agnostic on purpose: it is given a `send(bytes)` sink and is fed received
 * bytes via `feed(chunk)`, so the same object drives a browser WebSocket, the Node `ws`
 * client in the tests, or a raw TCP socket. `attachWebSocket` wires the common case.
 *
 * Rules this file exists to enforce (G2 definition of done):
 *   - after ACTOR_LOGIN the server floods dozens of init packets we have not ported;
 *     an unknown or undecodable packet is COUNTED AND LOGGED, never fatal;
 *   - only a framing desync (a corrupt stream) is fatal, because from that point on we
 *     no longer know where packets begin.
 */

import { UAFrameAccumulator } from './ua-framing.js';
import { encode, frame, decodeSegment, opcodeLabel } from './protocol.js';
import { OpCode } from './opcodes.js';

const DEFAULT_TIMEOUT_MS = 15000;

export class GameSession {
  /**
   * @param {(bytes: Uint8Array) => void} send  writes framed bytes to the socket
   * @param {{log?: Function, timeoutMs?: number}} [opts]
   */
  constructor(send, opts = {}) {
    this._send = send;
    this._log = opts.log || (() => {});
    this._timeoutMs = opts.timeoutMs || DEFAULT_TIMEOUT_MS;
    this._acc = new UAFrameAccumulator();
    this._serial = 0;
    this._pending = [];            // { opcodes:Set, serial, resolve, reject, timer }
    this._handlers = new Map();    // event name -> Set<fn>
    /** Observability for the "unknown packets must not break us" rule. */
    this.stats = { received: 0, decoded: 0, unknown: 0, decodeErrors: 0, sent: 0 };
    /** opcode -> count, for the packets we have not ported yet. */
    this.unknownOpcodes = new Map();
    /** Populated as the flow progresses. */
    this.account = null;
    this.actor = null;
    this.position = null;
    this.closed = false;
  }

  nextSerial() { return ++this._serial; }

  // ---- events -------------------------------------------------------------
  // Named events: every ported message name ('ACTOR_LIST_SERVER', ...), plus
  // 'packet' (all decoded), 'unknown' (unported opcode), 'error' (decode failure),
  // 'fatal' (framing desync).
  // 'rawSegment' fires for EVERY complete frame BEFORE any of those — known or
  // unknown opcode, decoded or decode-error — carrying the raw wire segment.
  // The VM UI layer observes it (G3d-f): the .etf scripts handle packets the
  // JS port has not even heard of. `segment` is a private copy owned by the
  // listener; the accumulator never reuses it.

  on(event, fn) {
    if (!this._handlers.has(event)) this._handlers.set(event, new Set());
    this._handlers.get(event).add(fn);
    return () => this._handlers.get(event).delete(fn);
  }

  _emit(event, payload) {
    const hs = this._handlers.get(event);
    if (!hs) return;
    for (const fn of hs) {
      try { fn(payload); } catch (e) { this._log(`[!] handler for ${event} threw: ${e.message}`); }
    }
  }

  // ---- sending ------------------------------------------------------------

  /** Send an already-built segment writer. */
  sendWriter(writer) {
    this._send(frame(writer));
    this.stats.sent++;
  }

  /** Fire-and-forget: `session.send('move', {time, x, y})`. */
  send(name, ...args) {
    if (!encode[name]) throw new Error(`no encoder for '${name}'`);
    this.sendWriter(encode[name](...args));
  }

  /**
   * Request/response: sends a serial-bearing message and resolves with the decoded
   * response whose serial matches. An ERROR packet carrying the same serial rejects.
   * @param {string} name       key in `encode` whose first arg is the serial
   * @param {number[]} expect   opcodes that count as the response
   */
  request(name, expect, ...args) {
    if (!encode[name]) throw new Error(`no encoder for '${name}'`);
    const serial = this.nextSerial();
    const writer = encode[name](serial, ...args);
    const p = this._await(new Set(expect), serial);
    this.sendWriter(writer);
    return p;
  }

  /** Wait for the next matching packet without sending anything (server-pushed messages). */
  waitFor(opcodes, { serial = null, timeoutMs = this._timeoutMs } = {}) {
    return this._await(new Set(Array.isArray(opcodes) ? opcodes : [opcodes]), serial, timeoutMs);
  }

  _await(opcodes, serial, timeoutMs = this._timeoutMs) {
    return new Promise((resolve, reject) => {
      const entry = { opcodes, serial, resolve, reject };
      entry.timer = setTimeout(() => {
        this._pending = this._pending.filter((e) => e !== entry);
        const want = [...opcodes].map(opcodeLabel).join(' | ');
        reject(new Error(`timeout after ${timeoutMs}ms waiting for ${want}` +
          (serial == null ? '' : ` serial=${serial}`)));
      }, timeoutMs);
      this._pending.push(entry);
    });
  }

  _settle(msg) {
    for (const entry of this._pending) {
      const serialOk = entry.serial == null || msg.body?.serial === entry.serial;
      if (!serialOk) continue;
      if (entry.opcodes.has(msg.opcode)) {
        this._pending = this._pending.filter((e) => e !== entry);
        clearTimeout(entry.timer);
        entry.resolve(msg.body);
        return;
      }
      if (msg.opcode === OpCode.ERROR && msg.body && entry.serial != null) {
        // ERROR echoes the serial of the request it rejects, so it only settles that one;
        // a serial-less waiter (a server push) is never rejected by someone else's error.
        this._pending = this._pending.filter((e) => e !== entry);
        clearTimeout(entry.timer);
        const err = new Error(`server error: ${msg.body.message} (rejected opcode ${msg.body.type})`);
        err.serverError = msg.body;
        entry.reject(err);
        return;
      }
    }
  }

  // ---- receiving ----------------------------------------------------------

  /** Feed received bytes. Safe to call with partial or merged frames. */
  feed(chunk) {
    let frames;
    try {
      frames = this._acc.push(chunk);
    } catch (e) {
      // Desync: we can no longer find packet boundaries, so nothing after this is
      // trustworthy. This is the ONLY fatal receive path.
      this._log(`[!] FATAL framing desync: ${e.message}`);
      this._emit('fatal', e);
      this._failAllPending(e);
      return;
    }
    for (const f of frames) this._dispatch(f);
  }

  _dispatch(f) {
    this.stats.received++;
    // Observer hook first: the VM layer must see packets we cannot decode yet,
    // and must see them before the named-event handlers run.
    this._emit('rawSegment', { opcode: f.opcode, segment: f.segment });
    const msg = decodeSegment(f.segment);
    if (!msg.known) {
      this.stats.unknown++;
      this.unknownOpcodes.set(msg.opcode, (this.unknownOpcodes.get(msg.opcode) || 0) + 1);
      this._log(`[?] skip unported ${msg.name} segLen=${f.segment.length}`);
      this._emit('unknown', msg);
      return;
    }
    if (msg.error) {
      // A ported message we failed to read: our layout is wrong or the tail is a shape we
      // don't model. The frame was already delimited by its length, so we stay in sync.
      this.stats.decodeErrors++;
      this._log(`[!] decode error in ${msg.name}: ${msg.error.message}`);
      this._emit('error', msg);
      return;
    }
    this.stats.decoded++;
    this._track(msg);
    this._emit(msg.name, msg.body);
    this._emit('packet', msg);
    this._settle(msg);
  }

  /** Keep the few pieces of world state G2 is judged on. */
  _track(msg) {
    switch (msg.opcode) {
      case OpCode.ACCOUNT_LOGIN_SERVER:
        this.account = { id: msg.body.accountId, name: msg.body.name };
        break;
      case OpCode.ACTOR_LOGIN_SERVER:
        this.actor = msg.body;
        this.position = { mapId: msg.body.mapId, x: msg.body.x, y: msg.body.y };
        break;
      case OpCode.GOMAP_ALLOW_SERVER:
        this.position = { mapId: msg.body.mapId, x: msg.body.x, y: msg.body.y,
                          mapInstanceId: msg.body.mapInstanceId };
        break;
      default:
        break;
    }
  }

  _failAllPending(err) {
    const pending = this._pending;
    this._pending = [];
    for (const e of pending) { clearTimeout(e.timer); e.reject(err); }
  }

  /** Call when the socket closes so in-flight requests reject instead of hanging. */
  onDisconnect(reason = 'connection closed') {
    if (this.closed) return;
    this.closed = true;
    this._failAllPending(new Error(reason));
    this._emit('close', reason);
  }

  // ---- high-level flow ----------------------------------------------------

  /** ACCOUNT_LOGIN_CLIENT -> ACCOUNT_LOGIN_SERVER. Resolves with {accountId, name, ...}. */
  async accountLogin(credentials) {
    const body = await this.request('accountLogin', [OpCode.ACCOUNT_LOGIN_SERVER], credentials);
    return body;
  }

  /** ACTOR_LIST_CLIENT -> ACTOR_LIST_SERVER. Resolves with the actor array. */
  async listActors() {
    const body = await this.request('actorList', [OpCode.ACTOR_LIST_SERVER]);
    return body.actors;
  }

  /** ACTOR_CREATE_CLIENT -> ACTOR_CREATE_SERVER. */
  async createActor(spec) {
    return this.request('actorCreate', [OpCode.ACTOR_CREATE_SERVER], spec);
  }

  /**
   * ACTOR_LOGIN_CLIENT -> ACTOR_LOGIN_SERVER (+ GOMAP_ALLOW_SERVER, which is what actually
   * carries the authoritative spawn map/coords). Resolves once both have arrived.
   */
  async enterWorld(actorId, { imei = '' } = {}) {
    const goMap = this.waitFor(OpCode.GOMAP_ALLOW_SERVER);
    const actor = await this.request('actorLogin', [OpCode.ACTOR_LOGIN_SERVER], { actorId, imei });
    const spawn = await goMap;
    return { actor, spawn };
  }

  /** Tell the server our map finished loading — required before it accepts movement. */
  loadingFinished() { this.send('loadingFinished'); }

  /** MOVE_CLIENT. `time` must track the server clock (see syncTime). */
  move({ time, x, y, direct = 0, state = 0 }) {
    this.send('move', { time, x, y, direct, state });
    if (this.position) { this.position.x = x; this.position.y = y; }
  }
}

/**
 * Wire a browser `WebSocket` (or the Node `ws` client, same API) to a GameSession.
 * Returns the session; resolve `session.ready` before sending.
 */
export function attachWebSocket(ws, opts = {}) {
  ws.binaryType = opts.binaryType || 'arraybuffer';   // browser: blob|arraybuffer, node ws: +nodebuffer
  const session = new GameSession((bytes) => ws.send(bytes, { binary: true }), opts);
  session.ready = new Promise((resolve, reject) => {
    if (ws.readyState === 1) return resolve(session);
    ws.addEventListener('open', () => resolve(session));
    ws.addEventListener('error', (e) => reject(e instanceof Error ? e : new Error('websocket error')));
  });
  ws.addEventListener('message', (ev) => {
    const d = ev.data;
    if (d instanceof ArrayBuffer) session.feed(new Uint8Array(d));
    else if (ArrayBuffer.isView(d)) session.feed(new Uint8Array(d.buffer, d.byteOffset, d.byteLength));
    else if (typeof Blob !== 'undefined' && d instanceof Blob) d.arrayBuffer().then((b) => session.feed(new Uint8Array(b)));
    else session.feed(new Uint8Array(d));
  });
  ws.addEventListener('close', () => session.onDisconnect('websocket closed'));
  return session;
}
