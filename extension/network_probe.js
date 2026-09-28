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
      socket.addEventListener("open", () => {
        remember({ type: "ws_open", url: String(url) });
        const openedAt = Date.now();
        setTimeout(() => dumpTraceNow("10s đầu sau khi mở kết nối mới", openedAt), 10000);
      });
      socket.addEventListener("error", () => remember({ type: "ws_error", url: String(url) }));
      socket.addEventListener("close", (event) => {
        remember({
          type: "ws_close",
          url: String(url),
          code: Number(event.code),
          reason: String(event.reason || ""),
          clean: Boolean(event.wasClean),
        });
        dumpTraceNow("20s trước khi đóng kết nối", Date.now() - 20000);
        dumpCloseCause(Date.now() - 20000);
      });
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
  // What we hit, straight from the fight. A boss can be a unit we do not track
  // (Thiên Long trận: no boss ever showed up in the creature list), so landing
  // damage is the one signal that says the fight is still on (user, 2026-09-16).
  const OP_SKILL_ATTACK = 185;         // client -> server: int time | POINT | byte dir | int target | int skill
  const OP_SKILL_ATTACKED = 187;       // server -> client: int target | int time | int source
                                       //   | byte result (0 hit, 1 miss, 2 immune, 3 crit)
                                       //   | byte kind (0 physical, 1 magic, 4 heal) | int damage
  const OP_LOADING_FINISHED1 = 2452;
  // Answers to a command we send ourselves (see sendGamePacket below).
  const OP_INSTANCE_CLEAR_SERVER = 535;  // 清除所有副本进度 done: int serial
  const OP_WORLD_TELEPORT_SERVER = 617;  // T.Giới Map teleport done: int serial
  // Our own property changes (Changed.sendAndClean): byte count | count items,
  // then the notify half. Items go out grouped by type, ints (type 0: byte 0 |
  // byte id | int value) first, so the leading ints are read and the rest is
  // left alone. STATE (id 38) carries STATE_ATTACK (2): set while any monster
  // has us on its threat list, and a door refuses us then - "Trạng thái chiến
  // đấu không thể thông qua" (PlayerPacketHandler touchExit: threatCount > 0).
  const OP_SYNC_PLAYER_SERVER = 144;
  // The bag, so a flow can throw items away by name with the game's own
  // "remove item" packet instead of clicking through the H.Trang popup
  // (user, 2026-09-29). Player.sendBagInfo: byte size | size * (int length |
  // grid) - every grid carries its own length, so an item we do not decode in
  // full (equipment) is simply stepped over. BagGrid.toClientByte: byte id |
  // byte count | GameItem when count > 0: ItemTemplate (int id | UTF name |
  // byte maxCount | short show/bind | byte useLevel | byte quality | int price
  // | UseType | byte itemType, 1 = equipment) ... | int instanceId (last).
  const OP_BAG_SERVER = 147;
  const ITEM_TYPE_EQUIP = 1;
  const SYNC_STATE = 38;
  const STATE_ATTACK = 2;
  const OP_ERROR = 0xffff;               // OpCode.ERROR (-1): int serial | short type | UTF message
  // Who we are and who else is on the account, so a flow never has to be told
  // which card in "Chọn NV" to click. Both shapes are the ones already ported
  // in web/client/src/net/protocol.js (decodeActorLogin / decodeActorList).
  const OP_ACTOR_LOGIN_SERVER = 104;     // int serial | int blobLen | int id | STR name | ...
  const OP_ACTOR_LIST_SERVER = 169;      // int serial | byte count | n * actor
  // What an NPC says back when we touch it, so a flow can see the popup
  // instead of clicking where it hopes the popup is.
  // All three open with the quest that asks (Player.chat / message / question;
  // game_world.gtl handleNpcChat / handleMessage / handleQuestion).
  const OP_NPC_CHAT_SERVER = 121;        // int questId | int npcId | STR message | int notifyId
  const OP_MESSAGE_SERVER = 122;         // int questId | STR message | int timeout | int notifyId
  const OP_QUESTION_SERVER = 123;        // int questId | STR message | STR options | int notifyId
  // What the real client sends when a person clicks an NPC and picks an option.
  // Logged as-is: the values are decided by the map's script, not by anything
  // we can read off the server source, so they are copied from a real click.
  const OP_TOUCHNPC_CLIENT = 120;        // int npc instanceId | int questId
  const OP_NOTIFY_CLIENT = 174;          // int questId | byte notifyId | byte type (3 = question) | byte answer
  const TYPE_PLAYER = 1;
  const TYPE_CREATURE = 3;             // server TYPE_CREATURE (client SPRITE_TYPE_NPC)
  const TYPES_WITH_IMAGE = new Set([3, 5, 6]);  // Tool.recvUnitView reads an imageId for these
  const STATE_MOVING = 1;              // MOVE_CLIENT state bit, cleared when we stop
  const STATE_DIE = 8;                 // GameObject.STATE_DIE
  const LEN_FIELD = { 0x41: 4, 0x42: 2, 0x43: 1 };

  const world = {
    messages: 0, frames: 0, desync: 0, badSegments: 0, firstBytes: "",
    mapId: null, mapInstanceId: -1, mapChangedAt: 0, mapLoadedAt: 0,
    me: null, meMoving: false, meMovedAt: 0,
    meFighting: null, meFightingAt: 0, // the game's own "in combat" mark on us; null = not heard yet
    bag: null, bagAt: 0,               // [{ grid, count, itemId, name, level, equip, instanceId }], filled grids only
    creatures: new Map(),              // instanceId -> { x, y, hp (0-200), state, name }
    npcIds: new Set(),                 // creatures the server flags as functional NPCs
    attackFails: [],                   // recent ATTACK_FAILs: { reason, target, at } (14 = target out of sight)
    aim: null,                         // last target we pressed Đánh on: { target, at }
    hit: null,                         // last damage we landed on it: { target, damage, at }
    kills: [],                         // units we saw die, whatever their type: { id, at }
    replies: [],                       // answers to our own commands: { serial, ok, type, message, at }
    actorId: null,                     // the character we are logged in as
    actorName: "",
    actors: [],                        // every character on the account, in the order Chọn NV draws them
    dialogs: [],                       // recent NPC popups: { kind, questId, message, options, notifyId, at }
    touches: [],                       // NPC touches sent (ours and the client's own)
    answers: [],                       // popup answers sent (ours and the client's own)
  };

  // The game's own socket, so a flow can send exactly what a UI button sends.
  // Only one we have really decoded a frame from counts: the page opens others.
  let gameSocket = null;

  // One UA frame (web/client/src/net/ua-framing.js frameSegment + ua-codec.js
  // UASegmentWriter): 'U' | 'A' | int32 total | int16 opcode | body.
  // fields: ["i32", n], ["u8", n] and ["str", s] - the shapes our commands need.
  function packField(kind, value) {
    if (kind === "u8") return new Uint8Array([Number(value) & 0xff]);
    if (kind === "i32") {
      const bytes = new Uint8Array(4);
      let rest = Number(value);
      for (let i = 3; i >= 0; i -= 1) { bytes[i] = rest & 0xff; rest = Math.floor(rest / 256); }
      return bytes;
    }
    // Packet.putString: int16 byte count, then the bytes.
    const text = new TextEncoder().encode(String(value));
    const bytes = new Uint8Array(2 + text.length);
    bytes[0] = (text.length >> 8) & 0xff;
    bytes[1] = text.length & 0xff;
    bytes.set(text, 2);
    return bytes;
  }

  function sendGamePacket(opcode, fields) {
    if (!gameSocket || gameSocket.readyState !== 1) return "chưa có kết nối game (mở lại tab game)";
    const body = (fields || []).map(([kind, value]) => packField(kind, value));
    const total = 8 + body.reduce((sum, part) => sum + part.length, 0);
    const frame = new Uint8Array(total);
    let rest = total;
    for (let i = 5; i >= 2; i -= 1) { frame[i] = rest & 0xff; rest = Math.floor(rest / 256); }
    frame[0] = 0x55;                   // 'U'
    frame[1] = 0x41;                   // variant 'A': the length is an int32
    frame[6] = (opcode >> 8) & 0xff;
    frame[7] = opcode & 0xff;
    let at = 8;
    for (const part of body) { frame.set(part, at); at += part.length; }
    gameSocket.send(frame);
    return "";
  }

  window.addEventListener("message", (event) => {
    if (event.source !== window || event.origin !== location.origin) return;
    if (event.data?.source !== "sanguo-send") return;
    const error = sendGamePacket(Number(event.data.opcode), event.data.fields);
    window.postMessage({ source: "sanguo-sent", id: event.data.id, error }, location.origin);
  });

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
      pos: () => pos,
      length: segment.length,
      byteAt: (at) => segment[at],
      bytes: (from, to) => segment.subarray(from, to),
      hex: (at, count) => Array.from(segment.subarray(at, Math.min(segment.length, at + count)),
        (byte) => byte.toString(16).padStart(2, "0")).join(""),
      seek: (at) => { if (at > segment.length) throw new RangeError("short segment"); pos = at; },
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
    // Which packet moved us, when it was none we decode (Hà Đông dễ, 2026-09-23
    // 20:49: a second run stood at the door 24,20 and came back "map ?" with no
    // unit around). Opcodes only, so the whole 6 s fit in one log line.
    const from = Date.now() - 6000;
    const lines = trace.filter((item) => item.at >= from && !(item.out && item.opcode === OP_MOVE_CLIENT))
      .map((item) => `${item.at - from}ms ${item.out ? ">" : "<"}${item.opcode}(${item.length})`);
    remember({ type: "map_trace", message: `nạp map không qua gói đổi map (map cũ ${world.mapId}): ${lines.slice(-22).join(" | ")}` });
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

  // Minh Châu ends every item the same way, whatever sits before it: int
  // instanceId | byte 1 | UTF source ("Rơi thế giới, tiệm tạp hóa") | 3 bytes
  // (2026-09-29 samples). Read from the grid's end backwards, that gives the
  // instance of equipment too, whose middle (mask-driven stats plus the
  // server's own additions) is not decoded here.
  const STRICT_UTF8 = new TextDecoder("utf-8", { fatal: true });
  function tailInstance(reader, start, end) {
    for (let p = end - 6; p >= start + 6; p -= 1) {
      if (reader.byteAt(p) !== 1) continue;
      const length = (reader.byteAt(p + 1) << 8) | reader.byteAt(p + 2);
      const after = end - (p + 3 + length);
      if (after < 0 || after > 8) continue;
      try { STRICT_UTF8.decode(reader.bytes(p + 3, p + 3 + length)); } catch (_) { continue; }
      return (reader.byteAt(p - 4) << 24) | (reader.byteAt(p - 3) << 16) | (reader.byteAt(p - 2) << 8) | reader.byteAt(p - 1);
    }
    return null;
  }

  // One bad grid must not cost the whole bag: the item details are read only
  // to tell equipment apart, and whatever cannot be read there leaves the
  // grid's id, item, name and instance - all a remove needs - still usable.
  // Every bag read is logged (bag_read), with the raw bytes of the first grid
  // that did not decode, so a server that writes items differently shows how.
  function readBag(reader) {
    const bag = [];
    let size = 0;
    let failed = 0;
    let firstFail = "";
    const samples = new Map();               // raw grids worth a look in the log
    let mismatched = 0;
    try {
      size = reader.u8();
      for (let n = 0; n < size; n += 1) {
        const length = reader.i32();
        const start = reader.pos();
        const end = start + length;
        if (length < 2 || end > reader.length) throw new RangeError(`ô ${n}: độ dài ${length} vượt gói`);
        const grid = reader.u8();
        const count = reader.u8();
        if (count > 0) {
          const item = { grid, count, itemId: 0, name: "", level: 0, equip: null, kind: null, instanceId: 0 };
          try {
            item.itemId = reader.i32();
            item.name = reader.str();
            // Minh Châu writes one byte more than the original after maxCount
            // and one more before a plain item's instance id, and appends a
            // source after it ("Thương Thành Nguyên Bảo"): Bao hương hoàn hồn,
            // 2026-09-29, 63 00 00e3 01 02 ffffffff 00 0a 00000000 00000000 00
            // ffffffff 01 "…" - bind type 3 with bound id 0 (player) and a
            // stack's instance -1, as the real client sends it back.
            reader.skip(1 + 1);                      // maxCount, Minh Châu's extra byte
            item.kind = reader.u16() >> 2;           // show type / icon
            item.level = reader.u8();
            reader.skip(1 + 4);                      // quality, price
            const use = reader.u8();                 // UseType: bit 0 = usable, then its details
            if (use & 1) {
              reader.skip(2 + 2 + 4 + 1 + 1 + 1);    // spellTime, cooldown id/time, distance, useCount, useClazz
              reader.str();                          // useConfirm
            }
            item.equip = reader.u8() === ITEM_TYPE_EQUIP;
            if (!item.equip) {
              if (use & 1) reader.skip(1);           // uses left
              reader.skip(4 + 4 + 1);                // valid time, bound id, Minh Châu's extra byte
              item.instanceId = reader.i32();
              item.sure = true;
              if ((use & 1) && count === 1 && !samples.has("dùng được")) {
                samples.set("dùng được", `${item.name} #${item.instanceId}: ${reader.hex(start, Math.min(length, 200))}`);
              }
            }
          } catch (error) {
            failed += 1;
            if (!firstFail) firstFail = `ô ${grid} "${item.name}": ${error.message}; ${reader.hex(start, Math.min(length, 96))}`;
          }
          const tail = tailInstance(reader, start, end);
          if (item.equip && tail != null && tail > 0) {
            item.instanceId = tail;
            item.sure = true;
          } else if (item.sure && tail != null && tail !== item.instanceId) {
            mismatched += 1;
            if (!samples.has("lệch")) samples.set("lệch", `${item.name} đọc ${item.instanceId}, đuôi ${tail}: ${reader.hex(start, Math.min(length, 200))}`);
          }
          // A stack has no instance of its own: the real client removes one
          // with -1 (Hà Đông 2026-09-27: "16 0000062c ffffffff 34").
          if (count > 1) item.instanceId = -1;
          if (!item.sure && !samples.has("?")) samples.set("?", reader.hex(start, Math.min(length, 160)));
          if (item.equip && !samples.has("equip")) samples.set("equip", reader.hex(start, Math.min(length, 200)));
          if (item.itemId) bag.push(item);
        }
        reader.seek(end);
      }
    } catch (error) {
      remember({ type: "bag_read", message: `lỗi đọc túi sau ${bag.length} món: ${error.message}; đầu gói ${reader.hex(2, 64)}` });
      return;
    }
    world.bag = bag;
    world.bagAt = Date.now();
    remember({
      type: "bag_read",
      message: `${size} ô, ${bag.length} ô có đồ (${bag.filter((item) => item.equip).length} trang bị)`
        + (failed ? `; ${failed} ô không đọc hết: ${firstFail}` : "")
        + (mismatched ? `; ${mismatched} ô instance lệch với đuôi` : "")
        + [...samples].map(([what, hex]) => `; mẫu ${what}: ${hex}`).join("")
        + `; vd ${bag.slice(0, 4).map((item) => `${item.grid}:${item.name}`).join(", ")}`,
    });
  }

  function readOwnSync(reader) {
    const count = reader.u8();
    for (let n = 0; n < count; n += 1) {
      if (reader.u8() !== 0) return;             // past the ints: nothing more we read
      const id = reader.u8();
      const value = reader.i32();
      if (id !== SYNC_STATE) continue;
      const fighting = (value & STATE_ATTACK) !== 0;
      if (fighting !== world.meFighting) world.meFightingAt = Date.now();
      world.meFighting = fighting;
    }
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
    // Deaths are noted for every unit type, not just the creatures we track:
    // a dungeon boss is none of them, and its death is how a fight knows it is
    // really over instead of waiting out the damage grace (user, 2026-09-19).
    if ((((update.state ?? 0) & STATE_DIE) !== 0) || update.hp === 0) {
      world.kills = [...world.kills.slice(-19), { id: instanceId, at: Date.now() }];
    }
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

  // Every segment either way for the last few seconds, so what the client
  // sends around an NPC click can be read off the log instead of guessed:
  // the touch and the answer alone did not take us into Thiên Long trận
  // (2026-09-23), so something else the client sends must count.
  const TRACE_SKIP_IN = new Set([OP_UNIT_MOVE, OP_UNIT_REFRESH, OP_UNIT_MULTI_REFRESH]);
  // Never copied into the log: account / session / password bodies.
  // SYSTEM_NEWSESSION 10, ACCOUNT_LOGIN_CLIENT 166, CHANGE_PASSWORD_CLIENT 334,
  // ADMIN_LOGIN_CLIENT 1001, PROXY_LOGIN 30001 (web/client/src/net/opcodes.js).
  const TRACE_SECRET = new Set([10, 166, 334, 1001, 30001]);
  let trace = [];
  let traceDumpAt = 0;
  function traceSegment(segment, outgoing, opcode) {
    const now = Date.now();
    trace = trace.filter((item) => now - item.at < 30000);
    if (!outgoing && TRACE_SKIP_IN.has(opcode)) return;
    trace.push({
      at: now, out: outgoing, opcode, length: segment.length,
      hex: TRACE_SECRET.has(opcode) ? "(ẩn)"
        : Array.from(segment.subarray(2, 26), (byte) => byte.toString(16).padStart(2, "0")).join(""),
    });
  }
  // A character switch shows up only as the socket closing and a new one
  // opening (user, 2026-09-23 16:29): the client logs in again from scratch.
  // So both ends are traced - what went out before the close, and what the
  // client says on the new socket - to see which packets make the switch.
  function dumpTraceNow(reason, from, to = Date.now()) {
    const lines = trace.filter((item) => item.at >= from && item.at <= to)
      .map((item) => `${item.at - from}ms ${item.out ? ">" : "<"}${item.opcode}(${item.length})`
        + (item.out ? ` ${item.hex}` : ""))
      .slice(0, 200);
    remember({ type: "switch_trace", message: `${reason}: ${lines.join(" | ")}` });
  }

  // Why the game dropped us (Hà Đông, 2026-09-23: three 1006 closes, one with
  // no flow running). The full trace is all fight packets and gets cut at 500
  // characters, so this line keeps what can explain a kick: every packet WE
  // sent but our position, the anti-bot challenges (2038) and our answers
  // (2039), and anything the server said in words.
  function dumpCloseCause(from) {
    const sent = trace.filter((item) => item.at >= from && item.out && item.opcode !== 105)
      .map((item) => `${item.at - from}ms >${item.opcode} ${item.hex}`);
    const challenges = trace.filter((item) => item.at >= from && !item.out && item.opcode === 2038)
      .map((item) => `${item.at - from}ms <2038`);
    const said = [...world.replies.filter((reply) => !reply.ok), ...world.dialogs]
      .filter((item) => item.at >= from)
      .map((item) => `${item.at - from}ms "${item.message}"`);
    remember({
      type: "close_cause",
      message: `gửi đi: ${sent.slice(-12).join(" | ") || "không"}; `
        + `2038: ${challenges.join(" | ") || "không"}; server báo: ${said.join(" | ") || "không"}`,
    });
  }

  function dumpTraceLater(reason) {
    const from = Date.now() - 4000;
    if (traceDumpAt > from) return;       // one dump already covers this click
    traceDumpAt = Date.now();
    setTimeout(() => {
      const lines = trace.filter((item) => item.at >= from)
        .map((item) => `${item.at - from}ms ${item.out ? ">" : "<"}${item.opcode}(${item.length}) ${item.hex}`);
      remember({ type: "npc_trace", message: `${reason}: ${lines.join(" | ")}` });
    }, 6000);
  }

  function readSegment(segment, outgoing) {
    const opcode = (segment[0] << 8) | segment[1];
    const reader = segmentReader(segment);
    traceSegment(segment, outgoing, opcode);
    try {
      if (outgoing) {
        if (opcode === OP_MOVE_CLIENT) readOwnMove(reader);
        else if (opcode === OP_LOADING_FINISHED || opcode === OP_LOADING_FINISHED1) {
          // The client's own word that the new map is drawn and takes clicks.
          world.mapLoadedAt = Date.now();
          onMapLoaded();
        }
        else if (opcode === OP_SKILL_ATTACK) {
          reader.skip(9);              // time, x, y, dir
          world.aim = { target: reader.i32(), at: Date.now() };
        } else if (opcode === OP_TOUCHNPC_CLIENT) {
          const target = reader.i32();
          const questId = reader.i32();
          world.touches = [...(world.touches || []).slice(-9), { target, questId, at: Date.now() }];
          remember({ type: "npc_touch_sent", message: `instanceId ${target} questId ${questId}` });
          dumpTraceLater(`quanh lúc bấm NPC ${target}`);
        } else if (opcode === OP_NOTIFY_CLIENT) {
          const questId = reader.i32();
          const notifyId = reader.u8();
          const kind = reader.u8();
          const answer = reader.u8();
          world.answers = [...(world.answers || []).slice(-9),
            { questId, notifyId, kind, answer, at: Date.now() }];
          remember({
            type: "npc_answer_sent",
            message: `questId ${questId} notifyId ${notifyId} type ${kind} answer ${answer}`,
          });
          dumpTraceLater(`quanh lúc trả lời quest ${questId}`);
        }
      } else if (opcode === OP_SKILL_ATTACKED) {
        // Only what we aimed at: other players fight around us in the same map.
        const target = reader.i32();
        reader.i32();
        reader.i32();
        const result = reader.u8();
        const kind = reader.u8();
        const damage = reader.i32();
        if ((result === 0 || result === 3) && kind <= 1 && damage > 0 && world.aim?.target === target) {
          world.hit = { target, damage, at: Date.now() };
        }
      } else if (opcode === OP_GOMAP_ALLOW || opcode === OP_FORCE_GOMAP) {
        readGoMap(reader);
      } else if (opcode === OP_UNIT_INFO) {
        readUnitInfo(reader);
      } else if (opcode === OP_ATTACK_FAIL) {
        const reason = reader.u8();
        reader.i32();
        // Keep a few: several targets can fail between two fight checks.
        world.attackFails = [...world.attackFails.slice(-19), { reason, target: reader.i32(), at: Date.now() }];
      } else if (opcode === OP_BAG_SERVER) {
        readBag(reader);
      } else if (opcode === OP_SYNC_PLAYER_SERVER) {
        readOwnSync(reader);
      } else if (opcode === OP_INSTANCE_CLEAR_SERVER || opcode === OP_WORLD_TELEPORT_SERVER) {
        world.replies = [...world.replies.slice(-9),
          { serial: reader.i32(), ok: true, type: opcode, message: "", at: Date.now() }];
      } else if (opcode === OP_ERROR) {
        // The game's own words for why it said no; the flow shows them as is.
        const serial = reader.i32();
        const type = reader.i16();
        world.replies = [...world.replies.slice(-9),
          { serial, ok: false, type, message: reader.str(), at: Date.now() }];
      } else if (opcode === OP_QUESTION_SERVER || opcode === OP_MESSAGE_SERVER
        || opcode === OP_NPC_CHAT_SERVER) {
        const questId = reader.i32();
        if (opcode === OP_NPC_CHAT_SERVER) reader.i32();   // npcId
        const message = reader.str();
        const options = opcode === OP_QUESTION_SERVER ? reader.str() : "";
        if (opcode === OP_MESSAGE_SERVER) reader.i32();    // timeout
        const kind = opcode === OP_QUESTION_SERVER ? "question"
          : (opcode === OP_MESSAGE_SERVER ? "message" : "chat");
        const notifyId = reader.i32();
        world.dialogs = [...world.dialogs.slice(-9),
          { kind, questId, message, options, notifyId, at: Date.now() }];
        // Logged as well as kept: a popup that the SERVER sends proves the
        // packet route can drive this NPC. One that never shows up here while
        // the client still draws it means the client's own quest VM made it,
        // and then only a real click on the NPC will do.
        remember({
          type: "npc_dialog",
          message: `${kind} questId ${questId} notifyId ${notifyId}: ${message}${options ? ` [${options}]` : ""}`,
        });
      } else if (opcode === OP_ACTOR_LOGIN_SERVER) {
        reader.i32();                  // serial
        reader.i32();                  // Packet.put(byte[]) writes the blob length first
        world.actorId = reader.i32();
        world.actorName = reader.str();
      } else if (opcode === OP_ACTOR_LIST_SERVER) {
        reader.i32();                  // serial
        const actors = [];
        for (let count = reader.u8(); count > 0; count -= 1) {
          const id = reader.i32();
          const name = reader.str();
          reader.u8();                 // sex
          const level = reader.u8();
          reader.skip(2);              // clazz, faction
          reader.skip(13);             // head / body / weapon score, flashLevel
          reader.str();                // map name
          actors.push({ id, name, level });
        }
        world.actors = actors;
        world.actorsAt = Date.now();     // when Chọn NV last opened
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
      const before = world.frames;
      reader(buffer, (segment) => readSegment(segment, outgoing));
      if (!outgoing && world.frames > before) gameSocket = socket;
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
