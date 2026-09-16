(() => {
  if (window.__SANGUO_NETWORK_PROBE__) return;
  window.__SANGUO_NETWORK_PROBE__ = true;

  const LAST_EVENT_KEY = "sanguo-last-network-event";
  const EVENT_QUEUE_KEY = "sanguo-network-event-queue";
  const FLOW_CONTEXT_KEY = "sanguo-flow-context";
  const pageSession = crypto.randomUUID?.() || `${Date.now()}-${Math.random()}`;

  function readJson(key, fallback) {
    try { return JSON.parse(localStorage.getItem(key) || "null") ?? fallback; }
    catch (_) { return fallback; }
  }

  function remember(event) {
    try {
      const flow = readJson(FLOW_CONTEXT_KEY, {});
      const entry = {
        id: crypto.randomUUID?.() || `${Date.now()}-${Math.random()}`,
        at: Date.now(),
        pageSession,
        pageUrl: location.href,
        type: event.type || "unknown",
        navigatorOnline: navigator.onLine !== false,
        visibility: document.visibilityState,
        documentState: document.readyState,
        perfMs: Math.round(performance.now()),
        ...flow,
        ...event,
      };
      const queue = readJson(EVENT_QUEUE_KEY, []);
      queue.push(entry);
      localStorage.setItem(EVENT_QUEUE_KEY, JSON.stringify(queue.slice(-100)));
      localStorage.setItem(LAST_EVENT_KEY, JSON.stringify(entry));
    } catch (_) { /* Storage may be unavailable during teardown. */ }
  }

  const NativeWebSocket = window.WebSocket;
  if (typeof NativeWebSocket === "function") {
    function ProbedWebSocket(url, protocols) {
      const socket = protocols === undefined
        ? new NativeWebSocket(url)
        : new NativeWebSocket(url, protocols);
      socket.addEventListener("open", () => remember({ type: "ws_open", url: String(url) }));
      socket.addEventListener("error", () => remember({ type: "ws_error", url: String(url) }));
      socket.addEventListener("close", (event) => remember({
        type: "ws_close",
        url: String(url),
        code: Number(event.code),
        reason: String(event.reason || ""),
        clean: Boolean(event.wasClean),
      }));
      tapGameTraffic(socket);
      return socket;
    }

    ProbedWebSocket.prototype = NativeWebSocket.prototype;
    for (const property of ["CONNECTING", "OPEN", "CLOSING", "CLOSED"]) {
      Object.defineProperty(ProbedWebSocket, property, { value: NativeWebSocket[property] });
    }
    window.WebSocket = ProbedWebSocket;
  }

  // ---- Game state tap (read-only) ------------------------------------------
  // Decodes the game's own WebSocket traffic, framed like the original client
  // ("U" + variant + length + segment; web/client/src/net/ua-framing.js), to
  // know the current map, our position and the live monsters around us.
  // Nothing is ever sent: content.js receives snapshots through postMessage.
  const OP_MOVE_CLIENT = 105;          // client -> server: our own position
  const OP_GOMAP_ALLOW = 134;          // server -> client: entered a map
  const OP_UNIT_REFRESH = 193;         // a unit enters / leaves our view
  const OP_UNIT_MULTI_REFRESH = 194;
  const OP_UNIT_MOVE = 195;
  const OP_UNIT_INFO = 197;            // Creature.getInfoPacket: functional-NPC flag
  const OP_FORCE_GOMAP = 321;          // server -> client: forced map change, same body as GOMAP_ALLOW
  const OP_LOADING_FINISHED = 133;     // client -> server: the new map finished loading
  const OP_ATTACK_FAIL = 136;          // server -> client: byte reason | int source | int target | int skill
  const OP_LOADING_FINISHED1 = 2452;
  const TYPE_PLAYER = 1;
  const TYPE_CREATURE = 3;             // server TYPE_CREATURE (client SPRITE_TYPE_NPC)
  const TYPES_WITH_IMAGE = new Set([3, 5, 6]);  // Tool.recvUnitView reads an imageId for these
  const STATE_MOVING = 1;              // MOVE_CLIENT state bit, cleared when we stop
  const STATE_DIE = 8;                 // GameObject.STATE_DIE
  const LEN_FIELD = { 0x41: 4, 0x42: 2, 0x43: 1 };

  const world = {
    messages: 0, frames: 0, desync: 0, badSegments: 0, firstBytes: "",
    mapId: null, mapInstanceId: -1, mapChangedAt: 0,
    me: null, meMoving: false, meMovedAt: 0,
    creatures: new Map(),              // instanceId -> { x, y, hp (0-200), state, name }
    npcIds: new Set(),                 // creatures the server flags as functional NPCs
    attackFails: [],                   // recent ATTACK_FAILs: { reason, target, at } (14 = target out of sight)
  };

  function hexBytes(buffer, count) {
    return Array.from(new Uint8Array(buffer, 0, Math.min(count, buffer.byteLength)),
      (byte) => byte.toString(16).padStart(2, "0")).join(" ");
  }

  // Splits a byte stream into UA segments; a chunk may hold partial or several frames.
  function frameReader() {
    let pending = new Uint8Array(0);
    return (chunk, onSegment) => {
      const bytes = new Uint8Array(chunk);
      let buffer = bytes;
      if (pending.length) {
        buffer = new Uint8Array(pending.length + bytes.length);
        buffer.set(pending);
        buffer.set(bytes, pending.length);
      }
      let offset = 0;
      while (buffer.length - offset >= 2) {
        const lenSize = LEN_FIELD[buffer[offset + 1]];
        if (buffer[offset] !== 0x55 || !lenSize) {
          world.desync += 1;
          pending = new Uint8Array(0);
          return;
        }
        if (buffer.length - offset < 2 + lenSize) break;
        let total = 0;
        for (let i = 0; i < lenSize; i += 1) total = total * 256 + buffer[offset + 2 + i];
        if (total < 4 + lenSize) {
          world.desync += 1;
          pending = new Uint8Array(0);
          return;
        }
        if (buffer.length - offset < total) break;
        onSegment(buffer.subarray(offset + 2 + lenSize, offset + total));
        world.frames += 1;
        offset += total;
      }
      pending = buffer.slice(offset);
    };
  }

  // Big-endian field reader after the 2-byte opcode (ua-codec.js UASegmentReader).
  function segmentReader(segment) {
    let pos = 2;
    const take = (count) => {
      if (pos + count > segment.length) throw new RangeError("short segment");
      const at = pos;
      pos += count;
      return at;
    };
    return {
      u8: () => segment[take(1)],
      u16: () => { const at = take(2); return (segment[at] << 8) | segment[at + 1]; },
      i16: () => {
        const at = take(2);
        const value = (segment[at] << 8) | segment[at + 1];
        return value > 0x7fff ? value - 0x10000 : value;
      },
      i32: () => {
        const at = take(4);
        return (segment[at] << 24) | (segment[at + 1] << 16) | (segment[at + 2] << 8) | segment[at + 3];
      },
      skip: (count) => { take(count); },
      // UASegment.readString: modified UTF-8, or raw UTF-16 when the length's high bit is set.
      str: () => {
        const at = take(2);
        const raw = (segment[at] << 8) | segment[at + 1];
        const length = raw & 0x7fff;
        const bytes = segment.subarray(take(length), pos);
        if (!(raw & 0x8000)) return new TextDecoder().decode(bytes);
        let text = "";
        for (let i = 0; i + 1 < length; i += 2) text += String.fromCharCode((bytes[i] << 8) | bytes[i + 1]);
        return text;
      },
    };
  }

  // PlayerLoadCall: int mapId | int mapInstanceId | int x | int y | byte allowFollow
  function readGoMap(reader) {
    world.mapId = reader.i32();
    world.mapInstanceId = reader.i32();
    world.me = { x: reader.i32(), y: reader.i32() };
    world.meMoving = false;
    world.mapChangedAt = Date.now();
    world.creatures.clear();
    world.npcIds.clear();
  }

  // The client finished loading a map. Also covers map changes announced by
  // packets we do not decode (e.g. script driven): drop the old map's units.
  function onMapLoaded() {
    if (Date.now() - world.mapChangedAt < 5000) return;
    world.mapId = null;
    world.mapChangedAt = Date.now();
    world.creatures.clear();
    world.npcIds.clear();
  }

  // Creature.getInfoPacket: int instanceId | byte canPass | byte isFunctional | STR functionName
  function readUnitInfo(reader) {
    const instanceId = reader.i32();
    reader.u8();
    if (reader.u8() === 1) world.npcIds.add(instanceId);
  }

  // GameRole.processNotifyServer: int time | short x | short y | byte dir | short state
  function readOwnMove(reader) {
    reader.i32();
    const x = reader.i16();
    const y = reader.i16();
    reader.u8();
    const state = reader.i16();
    world.me = { x, y };
    world.meMoving = (state & STATE_MOVING) !== 0;
    world.meMovedAt = Date.now();
  }

  // Tool.recvUnitView: byte type(|0x80 out of view) | int id | int instanceId | [short imageId]
  function readUnitRefresh(reader) {
    const head = reader.u8();
    const type = head & 0x7f;
    const outOfView = (head & 0x80) !== 0;
    reader.i32();
    const instanceId = reader.i32();
    if (!outOfView && TYPES_WITH_IMAGE.has(type)) reader.u16();
    if (type !== TYPE_CREATURE) return;
    if (outOfView) world.creatures.delete(instanceId);
    else if (!world.creatures.has(instanceId)) world.creatures.set(instanceId, { hp: 200, state: 0 });
  }

  // Unit/Creature.getMovePacket: byte flags|type | int instanceId | POINT | ANGLE | HPMP | STATE
  function readUnitMove(reader) {
    const head = reader.u8();
    const type = head & 0x07;
    const instanceId = reader.i32();
    const update = {};
    if (head & 0x80) {
      const mapField = reader.u16();
      if (mapField & 0x8000) reader.i32();       // instanced map: map id carries an instance id
      if (world.mapId == null && type === TYPE_CREATURE) world.mapId = mapField & 0x7fff;
      update.x = reader.i16();
      update.y = reader.i16();
    }
    if (head & 0x40) reader.skip(type === TYPE_PLAYER ? 6 : 10);  // angle, time, speed(, next point)
    if (head & 0x20) {
      update.hp = reader.u8();
      reader.u8();
    }
    if (head & 0x10) update.state = reader.i16();
    if (type !== TYPE_CREATURE) return;
    if (head & 0x08) {                           // DETAIL: mask, then name / level / faction
      const mask = reader.u8();
      if (mask & 1) update.name = reader.str();
      if (mask & 2) update.level = reader.u8();
      if (mask & 4) update.faction = reader.u8();
    }
    if (((update.state ?? 0) & STATE_DIE) || update.hp === 0) {
      world.creatures.delete(instanceId);
      return;
    }
    world.creatures.set(instanceId, { ...(world.creatures.get(instanceId) || { hp: 200, state: 0 }), ...update });
  }

  function readSegment(segment, outgoing) {
    const opcode = (segment[0] << 8) | segment[1];
    const reader = segmentReader(segment);
    try {
      if (outgoing) {
        if (opcode === OP_MOVE_CLIENT) readOwnMove(reader);
        else if (opcode === OP_LOADING_FINISHED || opcode === OP_LOADING_FINISHED1) onMapLoaded();
      } else if (opcode === OP_GOMAP_ALLOW || opcode === OP_FORCE_GOMAP) {
        readGoMap(reader);
      } else if (opcode === OP_UNIT_INFO) {
        readUnitInfo(reader);
      } else if (opcode === OP_ATTACK_FAIL) {
        const reason = reader.u8();
        reader.i32();
        // Keep a few: several targets can fail between two fight checks.
        world.attackFails = [...world.attackFails.slice(-19), { reason, target: reader.i32(), at: Date.now() }];
      } else if (opcode === OP_UNIT_REFRESH) {
        readUnitRefresh(reader);
      } else if (opcode === OP_UNIT_MULTI_REFRESH) {
        for (let count = reader.u8(); count > 0; count -= 1) readUnitRefresh(reader);
      } else if (opcode === OP_UNIT_MOVE) {
        readUnitMove(reader);
      }
    } catch (_) {
      world.badSegments += 1;
    }
  }

  let publishTimer = 0;
  function publishWorld() {
    if (publishTimer) return;
    publishTimer = setTimeout(() => {
      publishTimer = 0;
      const creatures = [];
      for (const [id, creature] of world.creatures) {
        if (creature.x == null) continue;
        creatures.push([id, creature.x, creature.y, creature.hp, creature.state,
          creature.name || "", world.npcIds.has(id) ? 1 : 0]);
      }
      const { creatures: _all, npcIds: _npcs, ...summary } = world;
      window.postMessage({ source: "sanguo-world", ...summary, creatures, at: Date.now() }, location.origin);
    }, 150);
  }
  setInterval(publishWorld, 2000);

  function tapGameTraffic(socket) {
    const inbound = frameReader();
    const outbound = frameReader();
    let blobQueue = Promise.resolve();
    const feed = (reader, data, outgoing) => {
      if (typeof Blob === "function" && data instanceof Blob) {
        blobQueue = blobQueue.then(() => data.arrayBuffer())
          .then((buffer) => feed(reader, buffer, outgoing)).catch(() => {});
        return;
      }
      const buffer = data instanceof ArrayBuffer ? data
        : ArrayBuffer.isView(data) ? data.buffer.slice(data.byteOffset, data.byteOffset + data.byteLength) : null;
      if (!buffer) return;
      if (!outgoing) {
        world.messages += 1;
        if (!world.firstBytes) world.firstBytes = hexBytes(buffer, 8);
      }
      reader(buffer, (segment) => readSegment(segment, outgoing));
      publishWorld();
    };
    socket.addEventListener("message", (event) => {
      try { feed(inbound, event.data, false); } catch (_) { world.badSegments += 1; }
    });
    const nativeSend = socket.send;
    socket.send = function send(data) {
      try { feed(outbound, data, true); } catch (_) { world.badSegments += 1; }
      return nativeSend.call(this, data);
    };
  }

  addEventListener("online", () => remember({ type: "browser_online" }));
  addEventListener("offline", () => remember({ type: "browser_offline" }));
  addEventListener("pagehide", (event) => remember({
    type: "page_hide", persisted: Boolean(event.persisted),
  }));
  addEventListener("pageshow", (event) => remember({
    type: "page_show", persisted: Boolean(event.persisted),
  }));
  addEventListener("error", (event) => remember({
    type: "window_error",
    message: String(event.message || ""),
    filename: String(event.filename || ""),
    line: Number(event.lineno || 0),
    column: Number(event.colno || 0),
  }));
  addEventListener("unhandledrejection", (event) => remember({
    type: "unhandled_rejection", message: String(event.reason?.message || event.reason || ""),
  }));

  const watchGuard = () => {
    if (document.getElementById("__mch5_guard")) remember({ type: "guard" });
  };
  new MutationObserver(watchGuard).observe(document.documentElement, { childList: true, subtree: true });
})();
