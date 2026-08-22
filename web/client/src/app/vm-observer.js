/*
 * VMObserver — the G3d-f/g "observer mode" adapter between the live GameSession
 * and the ETF UI VM.
 *
 * Division of labour (nothing is taken away from the existing client):
 *   - GameSession keeps owning login / enterWorld / movement, exactly as before;
 *   - this layer OBSERVES: every complete incoming frame arrives via the
 *     session's 'rawSegment' event — including opcodes the JS port has not
 *     ported and frames that fail to decode — and is offered to the .etf
 *     scripts through VMGameManager.handleIncoming (the GameMain.handleSegment
 *     routing: engine sync -> world-owned -> scripts -> world fallback);
 *   - while a script runs, GetNextPacket (0x0089) returns the packet being
 *     dispatched, so the scripts read the REAL server data;
 *   - each frame the caller drives cycle() + draw(): the scripts' windows are
 *     painted over the world renderer, scaled from the 240x320 art the 2011
 *     client was drawn for onto whatever canvas we are given.
 *
 * Start order (G3d-g part 1): the observer attaches to the session BEFORE the
 * login flow sends anything, so ACCOUNT_LOGIN_SERVER / ACTOR_LIST_SERVER /
 * ACTOR_LOGIN_SERVER / GOMAP_ALLOW_SERVER and the post-login init flood are
 * all seen. While the ETF boot is still loading, raw segments wait in a
 * bounded FIFO buffer and are replayed exactly once, in arrival order, the
 * moment the boot finishes; packets arriving DURING the replay queue behind
 * it instead of overtaking. The buffer holds private byte copies, is capped
 * by both packet count and total bytes, is cleared on disconnect, and an
 * overflow is logged loudly (never silently dropped).
 *
 * Keyboard (G3d-g): browser keys map onto the handset key codes the scripts
 * know (Utilities.java's active block: UP=1 DOWN=2 LEFT=3 RIGHT=4 FIRE=5,
 * LSOFT=6 RSOFT=7, digits = ASCII). The syscalls see them through the normal
 * keyPressed/noKeyPressed/multiKeyCheck hooks — no new widget surface.
 */
import { UASegment } from '../vm/ua-segment.js';
import { VMGameManager, VM_TYPE_GAME, CONN_SYNC_SERVER } from '../vm/vmgame.js';
import { ResourceStore } from '../vm/resources.js';
import { Graphics, Font } from '../vm/gfx.js';
import { parseETF } from '../vm/etf.js';
import { codecs as assetCodecs } from '../assets/codecs.js';

/** The art the scripts were drawn for (GWindow.uiMax* defaults follow it). */
const VM_WIDTH = 240;
const VM_HEIGHT = 320;
const MODEL = 'Flash';

/** Utilities.java's ACTIVE key block (the //#else branch — the shipped build). */
export const VM_KEY = Object.freeze({
  UP: 1, DOWN: 2, LEFT: 3, RIGHT: 4,
  FIRE: 5, LEFT_SOFT: 6, RIGHT_SOFT: 7,
  NUM0: 48, NUM1: 49, NUM2: 50, NUM3: 51,
  NUM4: 52, NUM5: 53, NUM6: 54, NUM7: 55, NUM8: 56, NUM9: 57,
  STAR: 42, POUND: 35,
});

/** Candidate locations for a bare asset name inside the bridge's /data tree. */
function assetCandidates(name) {
  return [
    `client_pkg/${MODEL}/${name}`,
    `client_pkg/${name}`,
    `client_res/${MODEL}/${name}`,
    `client_res/${name}`,
    name,
  ];
}

/** gunzip in the browser; Node tests inject their own loadScript instead. */
async function gunzip(bytes) {
  const ds = new DecompressionStream('gzip');
  const stream = new Blob([bytes]).stream().pipeThrough(ds);
  const buf = await new Response(stream).arrayBuffer();
  return new Uint8Array(buf);
}

export class VMObserver {
  /**
   * @param {object} opts
   * @param {import('../net/session.js').GameSession} opts.session
   * @param {{width:number,height:number}} [opts.canvas]  drawn OVER the world renderer
   * @param {(name: string) => Promise<Uint8Array>} [opts.loadScript]
   *        inflated etf bytes (tests); default fetches /data/scripts/<Model>/…
   * @param {(name: string) => Promise<Uint8Array>} [opts.fetchBytes]
   *        raw asset bytes (tests); default fetches the /data tree
   * @param {number} [opts.maxPendingPackets]  FIFO cap while booting (default 500)
   * @param {number} [opts.maxPendingBytes]    FIFO byte cap while booting (default 4 MiB)
   * @param {{log?: Function}} [opts]
   */
  constructor({ session, canvas = null, loadScript, fetchBytes,
                maxPendingPackets = 500, maxPendingBytes = 4 * 1024 * 1024,
                log = () => {} }) {
    this.session = session;
    this.canvas = canvas;
    this.log = log;
    this.started = false;
    this.error = null;

    // ---- G3d-g part 1: bounded FIFO for packets that arrive before boot ----
    this._booted = false;
    this._draining = false;
    this._pending = [];            // Uint8Array copies, strictly FIFO
    this._pendingBytes = 0;
    this._maxPendingPackets = maxPendingPackets;
    this._maxPendingBytes = maxPendingBytes;

    // ---- G3d-g part 2: live observability ----------------------------------
    this.stats = {
      rawReceived: 0,
      handledByVM: 0,
      handledByWorld: 0,
      unhandled: 0,
      overflowed: 0,
      /** opcode -> count (decimal string keys, JSON friendly) */
      opcodes: new Map(),
      /** first gap the scripts hit, so one trace pinpoints the next port */
      firstMissingSyscall: null,   // "0x...."
      firstVmError: null,          // "where: message"
      /** window bookkeeping, refreshed every cycle() */
      liveVMs: 0,
      windowCount: 0,
      windows: [],                 // ["game_panel/main", ...]
      lastWindowEvent: null,       // { added:[...], opcodeBefore }
    };
    this._lastOpcode = null;
    this._knownWindows = new Set();

    const baseFetch = fetchBytes ?? ((name) => this._fetchAsset(name));
    const scriptLoad = loadScript ?? (async (name) => {
      const gz = await this._fetchAsset(`scripts/${MODEL}/${name}_${MODEL}.etf.gz`);
      return gunzip(gz);
    });

    // ---- resources: decode the original art straight from /data ------------
    const store = new ResourceStore({
      fetchBytes: baseFetch,
      rasterise: ({ width, height, rgba }) => {
        const c = document.createElement('canvas');
        c.width = width;
        c.height = height;
        // copy: the decoder's buffer is reused across frames
        c.getContext('2d').putImageData(
          new ImageData(new Uint8ClampedArray(rgba), width, height), 0, 0);
        return c;
      },
      // codecs.js is DOM-free and runs in the browser as-is
      inflate: assetCodecs.inflate,
      decodePNG: assetCodecs.decodePNG,
    });

    const defaultFont = new Font();
    // ---- platform hooks -----------------------------------------------------
    const platform = {
      screenWidth: () => VM_WIDTH,
      screenHeight: () => VM_HEIGHT,
      fontHeight: () => defaultFont.getHeight(),
      lineHeight: () => Math.round(defaultFont.getHeight() * 1.15),
      font: { stringWidth: (s) => this._stringWidth(String(s ?? '')) },
      fontObj: defaultFont,
      // ---- keyboard: the handset key state the scripts poll ---------------
      keyPressed: (code, clear) => {
        const has = this._keys.has(code | 0);
        if (has && clear) this._keys.delete(code | 0);
        return has;
      },
      noKeyPressed: () => this._keys.size === 0,
      multiKeyCheck: (codes, clear) => {
        if (!codes) return -1;
        for (let i = 0; i < codes.length; i++) {
          if (this._keys.has(codes[i] | 0)) {
            if (clear) this._keys.delete(codes[i] | 0);
            return i;
          }
        }
        return -1;
      },
      clearKeys: () => this._keys.clear(),
      getTimeStamp: () => Date.now(),
      getTick: () => Date.now() & 0x7fffffff,
      getSystemTime: () => Math.floor(Date.now() / 1000),
      graphics: null,
      // SendRequest: ship the script's segment on the SAME session. The wire
      // framing is identical to what session.sendWriter produces for its own
      // writers — UASegment.toBytes() is the same shape.
      sendRequest: (segment) => {
        try {
          this.session.sendWriter(segment);
          return segment.serial >= 0 ? segment.serial : 0;
        } catch (e) {
          this.log(`[vm] sendRequest failed: ${e.message}`);
          return -1;
        }
      },
      broadcast: () => {},
      getNextPacket: () => this.manager ? this.manager.currentPacket : null,
      exitGame: () => this.log('[vm] exitGame requested by script'),
      closeConnection: () => this.log('[vm] closeConnection requested by script'),
      loadFile: () => null,
      saveFile: () => false,
      deleteFile: () => {},
      loadResourceFile: () => null,
      formatText: (text) => [String(text ?? '')],
      drawDefaultBackground: () => {},
      instructionLimit: 5_000_000,
      unimplemented: (id) => {
        const hex = `0x${id.toString(16)}`;
        if (!this.stats.firstMissingSyscall) this.stats.firstMissingSyscall = hex;
        this.log(`[vm] missing syscall ${hex}`);
      },
      onError: (err, where) => {
        const text = `${where}: ${err?.message ?? err}`;
        if (!this.stats.firstVmError) this.stats.firstVmError = text;
        this.log(`[vm] error ${text}`);
      },
    };

    this.manager = new VMGameManager({ platform, resources: store, loadScript: scriptLoad });
    this.platform = platform;
    this.defaultFont = defaultFont;
    this._keys = new Set();
    this._offRaw = null;
    this._offClose = null;

    // Attach IMMEDIATELY (constructor time), so nothing the session receives
    // before start() finishes can be missed. Until the boot completes the
    // segments wait in the bounded FIFO.
    this._offRaw = session.on('rawSegment', ({ opcode, segment }) => {
      this._onRaw(opcode, segment);
    });
    this._offClose = session.on('close', () => this._clearPending());
  }

  /* ------------------------------------------------------------ raw path */

  /**
   * One complete incoming frame. Buffers while booting/draining, otherwise
   * dispatches straight away. Never throws into the session's event loop.
   */
  _onRaw(opcode, segment) {
    const s = this.stats;
    s.rawReceived++;
    s.opcodes.set(opcode, (s.opcodes.get(opcode) || 0) + 1);
    this._lastOpcode = opcode;

    if (!this._booted || this._draining) {
      // private copy: the accumulator's buffer is its own, but the contract
      // is that whatever we keep cannot be mutated behind our back
      const copy = new Uint8Array(segment);
      if (this._pending.length >= this._maxPendingPackets ||
          this._pendingBytes + copy.length > this._maxPendingBytes) {
        s.overflowed++;
        this.log(`[vm] raw buffer overflow (${this._pending.length} packets, `
          + `${this._pendingBytes}B) — dropping opcode ${opcode}; the UI VM may miss state`);
        return;
      }
      this._pending.push(copy);
      this._pendingBytes += copy.length;
      if (this._booted && !this._draining) this._scheduleDrain();
      return;
    }

    this._dispatchSegment(segment);
  }

  /** Dispatch one raw segment to the VM routing. Errors stay contained. */
  _dispatchSegment(segment) {
    try {
      const opcode = ((segment[0] & 0xff) << 8) | (segment[1] & 0xff);
      const handled = this.manager.handleIncoming(UASegment.fromBytes(segment),
        () => { this.stats.handledByWorld++; });
      if (handled) this.stats.handledByVM++;
      else if (opcode !== CONN_SYNC_SERVER &&
               !this.manager.worldPacketOpcodes.has(opcode)) {
        this.stats.unhandled++;
      }
    } catch (e) {
      this.log(`[vm] dispatch failed: ${e.message}`);
    }
  }

  /** Drain the FIFO exactly once, in order; arrivals during the drain queue. */
  _scheduleDrain() {
    if (this._draining) return;
    this._draining = true;
    // a microtask keeps the session's feed() loop reentrancy simple: any
    // packet that arrives while we drain is buffered and drained after
    Promise.resolve().then(() => {
      try {
        while (this._pending.length > 0) {
          const seg = this._pending.shift();
          this._pendingBytes -= seg.length;
          this._dispatchSegment(seg);
        }
      } finally {
        this._draining = false;
        if (this._pending.length > 0) this._scheduleDrain();
      }
    });
  }

  _clearPending() {
    if (this._pending.length > 0) {
      this.log(`[vm] clearing ${this._pending.length} buffered packet(s) on disconnect`);
    }
    this._pending = [];
    this._pendingBytes = 0;
  }

  /* ---------------------------------------------------------------- boot */

  /**
   * Load the boot closure and flip the buffer into live dispatch. Resolves
   * once game_init's INIT has run (or failed — observer mode never breaks the
   * game either way); the FIFO replays exactly once from here.
   */
  async start() {
    if (this.started) return;
    this.started = true;
    try {
      // preload the transitive library closure of game_init so addUI's sync
      // lib path always hits (same contract as the boot smoke test)
      const names = new Set(['lib_builtin', 'game_init']);
      const queue = ['lib_builtin', 'game_init'];
      while (queue.length > 0) {
        const name = queue.shift();
        const libs = parseETF(await this.manager.loadScript(name)).libNames;
        for (const lib of libs) {
          if (!names.has(lib)) { names.add(lib); queue.push(lib); }
        }
      }
      for (const name of names) {
        this.manager.vmCache.set(name, await this.manager.loadScript(name));
      }
      this.manager.addUI('game_init', this.manager.vmCache.get('game_init'), VM_TYPE_GAME);
    } catch (e) {
      this.error = e;
      this.log(`[vm] observer boot failed: ${e.message}`);
    } finally {
      // boot finished (either way): replay what piled up, then go live
      this._booted = true;
      this._scheduleDrain();
    }
  }

  stop() {
    this._offRaw?.();
    this._offClose?.();
    this._offRaw = this._offClose = null;
    this._clearPending();
  }

  /* ------------------------------------------------------------ keyboard */

  /** Browser key down -> handset key code (see VM_KEY). */
  keyDown(code) { this._keys.add(code | 0); }

  /** Browser key up -> handset key code. */
  keyUp(code) { this._keys.delete(code | 0); }

  /* ---------------------------------------------------------- frame loop */

  /** One frame of script time. Call after the world update, before draw(). */
  cycle() {
    if (this.manager.vmGames.size() > 0) {
      this.manager.cycle();
      this._trackWindows();
    }
  }

  /**
   * Window bookkeeping: count live VMGames and GWindows, and when the window
   * count grows record WHICH windows appeared and which opcode preceded the
   * growth — that opcode is the live proof of what made the UI appear.
   */
  _trackWindows() {
    const s = this.stats;
    s.liveVMs = this.manager.vmGames.size();
    const now = [];
    for (const vg of this.manager.vmGames.values()) {
      const wins = vg.vmContainers?.values?.() ?? [];
      for (let i = 0; i < wins.length; i++) {
        now.push(`${vg.vmId}/${wins[i].name ?? wins[i].vmData?.[3] ?? i}`);
      }
    }
    s.windowCount = now.length;
    s.windows = now;
    const added = now.filter((n) => !this._knownWindows.has(n));
    if (added.length > 0) {
      s.lastWindowEvent = { added, opcodeBefore: this._lastOpcode };
      this.log(`[vm] window(s) opened: ${added.join(', ')}`
        + (this._lastOpcode != null ? ` (after opcode ${this._lastOpcode})` : ''));
    }
    for (const n of now) this._knownWindows.add(n);
  }

  /**
   * Paint the scripts' UI over the world renderer. The 240x320 art is fitted
   * into the canvas (centred, uniform scale, letterboxed) — the same way the
   * phone client scaled its screen.
   */
  draw(ctx, width = this.canvas?.width ?? VM_WIDTH, height = this.canvas?.height ?? VM_HEIGHT) {
    if (this.manager.vmGames.size() === 0) return;
    const scale = Math.min(width / VM_WIDTH, height / VM_HEIGHT);
    const dx = (width - VM_WIDTH * scale) / 2;
    const dy = (height - VM_HEIGHT * scale) / 2;
    ctx.save();
    ctx.translate(dx, dy);
    ctx.scale(scale, scale);
    ctx.beginPath();
    ctx.rect(0, 0, VM_WIDTH, VM_HEIGHT);
    ctx.clip();
    const g = new Graphics(ctx, VM_WIDTH, VM_HEIGHT);
    g.fontObj = this.platform.fontObj;
    this.platform.graphics = g;
    try {
      this.manager.drawAll(g);
    } catch (e) {
      this.log(`[vm] draw failed: ${e.message}`);
    }
    ctx.restore();
  }

  /* ------------------------------------------------------------- helpers */

  _measureCtx = null;
  _stringWidth(s) {
    if (!s) return 0;
    try {
      if (!this._measureCtx) {
        this._measureCtx = document.createElement('canvas').getContext('2d');
      }
      this._measureCtx.font = this.defaultFont.css;
      return Math.ceil(this._measureCtx.measureText(s).width);
    } catch {
      return s.length * 8;
    }
  }

  /** Fetch one asset name trying every layout the data tree uses. */
  async _fetchAsset(name) {
    let lastErr = null;
    for (const rel of assetCandidates(name)) {
      try {
        const res = await fetch(`/data/${rel}`);
        if (!res.ok) { lastErr = new Error(`${res.status} ${rel}`); continue; }
        return new Uint8Array(await res.arrayBuffer());
      } catch (e) {
        lastErr = e;
      }
    }
    throw lastErr ?? new Error(`asset not found: ${name}`);
  }

  /**
   * JSON-friendly stats snapshot for the dev panel / console poking:
   * `window.__game.vmObserver.stats`.
   */
  get statsSnapshot() {
    const s = this.stats;
    const opcodes = {};
    for (const [k, v] of [...s.opcodes.entries()].sort((a, b) => b[1] - a[1]).slice(0, 40)) {
      opcodes[k] = v;
    }
    return {
      rawReceived: s.rawReceived,
      handledByVM: s.handledByVM,
      handledByWorld: s.handledByWorld,
      unhandled: s.unhandled,
      overflowed: s.overflowed,
      topOpcodes: opcodes,
      liveVMs: s.liveVMs,
      windowCount: s.windowCount,
      windows: s.windows,
      lastWindowEvent: s.lastWindowEvent,
      firstMissingSyscall: s.firstMissingSyscall,
      firstVmError: s.firstVmError,
      pendingWhileBooting: this._pending.length,
    };
  }
}