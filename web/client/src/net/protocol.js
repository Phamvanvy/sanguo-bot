/*
 * Protocol registry — G2 subset (login -> character list/create -> enter map -> move/chat).
 *
 * Every entry here is TRACED from the Java sources, never guessed. `OpCode.java` is the
 * source of truth for IDs only; the field layout of each message comes from the code that
 * reads it (server handler) or writes it (server response / DB call), cited per message.
 *
 * Wire-type cheat sheet for reading the Java (peony/net/Packet.java):
 *   pt.put(x)        -> 1 byte            (int overload truncates to a byte!)
 *   pt.putShort(x)   -> int16 BE
 *   pt.putInt(x)     -> int32 BE
 *   pt.putString(s)  -> u16 len + UTF-8, or (len|0x8000) + UTF-16BE when that is shorter
 *   pt.putUTF(s)     -> u16 len + UTF-8 (no UTF-16 branch)
 *   pt.put(byte[])   -> int32 len + raw bytes
 * and inside Player.toClientBytes (a raw DataOutputStream, not a Packet):
 *   dos.write(x)     -> 1 byte,  dos.writeUTF(s) -> Java modified UTF-8
 * UASegmentReader.readString() handles both string flavours (see ua-codec.js).
 *
 * Decoders return a plain object. Messages whose tail we deliberately do NOT decode set
 * `partial: true` and `restBytes` — G2 needs ids/coords, not every stat blob, and a frame
 * is skipped safely by its length regardless (ua-framing.js).
 */

import { UASegmentWriter, UASegmentReader } from './ua-codec.js';
import { frameSegment } from './ua-framing.js';
import { OpCode, OpCodeNames } from './opcodes.js';

/** Opcodes travel as int16; ERROR is -1 == 0xFFFF on the wire. Normalise to the signed form. */
export function normalizeOpcode(op) {
  return op > 0x7fff ? op - 0x10000 : op;
}

/** Human label for logging an opcode we may or may not know. */
export function opcodeLabel(op) {
  const names = OpCodeNames[String(normalizeOpcode(op))];
  return names && names.length ? `${names.join('/')}(${normalizeOpcode(op)})` : `UNKNOWN(${normalizeOpcode(op)})`;
}

// ---------------------------------------------------------------------------
// client -> server encoders
// ---------------------------------------------------------------------------
// Each returns the raw segment; use `frame()` (below) to get bytes for the socket.
// `serial` is an app-level request id echoed back in the matching response.

export const encode = {
  /** PlayerPacketHandler.accountLogin: int serial|UTF name|UTF password|UTF model|UTF version|UTF phone|int playerId|UTF imei */
  accountLogin(serial, { name, password, model = 'GenericMidp2/GenericMidp2', version = '2.4-CCCCCPiP', phone = '', playerId = -1, imei = '' }) {
    return new UASegmentWriter(OpCode.ACCOUNT_LOGIN_CLIENT, true, serial)
      .writeString(name).writeString(password).writeString(model).writeString(version)
      .writeString(phone).writeInt(playerId).writeString(imei);
  },

  /** PlayerPacketHandler.actorList: int serial (no other field) */
  actorList(serial) {
    return new UASegmentWriter(OpCode.ACTOR_LIST_CLIENT, true, serial);
  },

  /** PlayerPacketHandler.actorCreate: int serial|UTF name|byte sex|byte clazz|byte faction */
  actorCreate(serial, { name, sex, clazz, faction }) {
    return new UASegmentWriter(OpCode.ACTOR_CREATE_CLIENT, true, serial)
      .writeString(name).writeByte(sex).writeByte(clazz).writeByte(faction);
  },

  /** PlayerPacketHandler.login (ACTOR_LOGIN_CLIENT): int serial|int actorId|UTF imei */
  actorLogin(serial, { actorId, imei = '' }) {
    return new UASegmentWriter(OpCode.ACTOR_LOGIN_CLIENT, true, serial)
      .writeInt(actorId).writeString(imei);
  },

  /** PlayerPacketHandler.loadingFinished: no body. */
  loadingFinished() {
    return new UASegmentWriter(OpCode.LOADING_FINISHED_CLIENT);
  },

  /** PlayerPacketHandler.syncTime: int clientTime (no serial). */
  syncTime(clientTime) {
    return new UASegmentWriter(OpCode.SYNC_TIME_CLIENT).writeInt(clientTime);
  },

  /**
   * PlayerPacketHandler.move: int time|short x|short y|byte direct|short state  (no serial).
   * `time` is the client's Time.currTime; the server rejects times > server clock + 3s.
   */
  move({ time, x, y, direct = 0, state = 0 }) {
    return new UASegmentWriter(OpCode.MOVE_CLIENT)
      .writeInt(time).writeShort(x).writeShort(y).writeByte(direct).writeShort(state);
  },

  /** PlayerPacketHandler.chat: byte channel|int destId|UTF message|bytes attachment (int32 len). */
  chat({ channel = 2, destId = 0, message, attachment = new Uint8Array(0) }) {
    return new UASegmentWriter(OpCode.CHAT_CLIENT)
      .writeByte(channel).writeInt(destId).writeString(message).writeBytes(attachment);
  },
};

/** Chat channels — peony/game/ChatOption.java */
export const ChatChannel = Object.freeze({
  WORLD: 0, FACTION: 1, AREA: 2, NATIVE: 3, GUILD: 4, PARTY: 5, PRIVATE: 6, SYSTEM: 7,
});

/** Wrap an encoder's writer into socket-ready frame bytes. */
export function frame(writer) {
  return frameSegment(writer.toBytes(), 'A');
}

// ---------------------------------------------------------------------------
// server -> client decoders
// ---------------------------------------------------------------------------

/** peony/game/ErrorHandler.sendErrorMessage: int serial|short type|UTF message */
function decodeError(r) {
  return { serial: r.readInt(), type: r.readShort(), message: r.readString() };
}

/** peony/service/account/AccountLoginCall.callFinish: int serial|int accountId|UTF name|int money|int renames */
function decodeAccountLogin(r) {
  const m = { serial: r.readInt(), accountId: r.readInt(), name: r.readString(), money: r.readInt() };
  if (r.remaining() >= 4) m.modifiedNameTimes = r.readInt();
  return m;
}

/**
 * peony/db/LoadActorListCall.callFinish:
 *   int serial | byte count | count * { int id | STR name | byte sex | byte level | byte clazz
 *                                     | byte faction | int headScore | int bodyScore
 *                                     | int weaponScore | byte flashLevel | STR mapName }
 */
function decodeActorList(r) {
  const serial = r.readInt();
  const count = r.readUnsignedByte();
  const actors = [];
  for (let i = 0; i < count; i++) {
    actors.push({
      id: r.readInt(),
      name: r.readString(),
      sex: r.readByte(),
      level: r.readUnsignedByte(),
      clazz: r.readByte(),
      faction: r.readByte(),
      headScore: r.readInt(),
      bodyScore: r.readInt(),
      weaponScore: r.readInt(),
      flashLevel: r.readByte(),
      mapName: r.readString(),
    });
  }
  return { serial, count, actors };
}

/** peony/db/PlayerCreateCall.callFinish: int serial|int id|STR name|byte sex|byte level|byte clazz */
function decodeActorCreate(r) {
  return {
    serial: r.readInt(),
    id: r.readInt(),
    name: r.readString(),
    sex: r.readByte(),
    level: r.readUnsignedByte(),
    clazz: r.readByte(),
  };
}

/**
 * peony/db/PlayerLoadCall.callFinish: `pt.putInt(serial); pt.put(player.toClientBytes())`.
 *
 * NOTE the second write: Packet.put(byte[]) emits an int32 LENGTH before the bytes, so the
 * blob is length-delimited — verified against a live 237-byte packet (blobLen=227).
 *
 * toClientBytes is a DataOutputStream blob. We decode the fixed head through `guildName`
 * — everything G2 needs (id, name, level, map, x/y, state) — and stop before the
 * variable-length equipment/chatOption/cooldown/buff blobs, which are their own
 * sub-formats. Hence `partial: true`.
 */
function decodeActorLogin(r) {
  const serial = r.readInt();
  const blobLen = r.readInt();
  const blobStart = r.pos;
  const m = { serial, blobLen, id: r.readInt(), name: r.readString() };
  m.sex = r.readUnsignedByte();
  m.level = r.readUnsignedByte();
  m.clazz = r.readUnsignedByte();
  m.faction = r.readUnsignedByte();
  m.maxhp = r.readShort(); m.maxmp = r.readShort();
  m.hp = r.readShort(); m.mp = r.readShort();
  m.strength = r.readShort(); m.agility = r.readShort();
  m.stamina = r.readShort(); m.intellect = r.readShort();
  // 14 combat shorts we don't model yet: atkUp, atkDown, spellPower, spellHeal, defense,
  // spellDefense, crit, spellCrit, hit, spellHit, dodge, spellDodge, antiCrit, defensePercent
  for (let i = 0; i < 14; i++) r.readShort();
  m.healthRestore = r.readShort(); m.manaRestore = r.readShort();
  m.skillPoint = r.readShort(); m.propertyPoint = r.readShort();
  m.exp = r.readInt(); m.nextLevelExp = r.readInt(); m.money = r.readInt();
  m.mapId = r.readShort();
  m.mapInstanceId = r.readInt();
  m.x = r.readShort(); m.y = r.readShort();
  m.direct = r.readShort(); m.state = r.readShort();
  m.credit = r.readInt();
  m.creditName = r.readString();
  m.guildName = r.readString();
  m.partial = true;              // equipments / chatOptions / coolDowns / buffs / honor / title
  m.restBytes = blobLen - (r.pos - blobStart);
  return m;
}

/** peony/db/PlayerLoadCall.callFinish: int mapId|int mapInstanceId|int x|int y|byte allowFollow */
function decodeGoMapAllow(r) {
  return {
    mapId: r.readInt(),
    mapInstanceId: r.readInt(),
    x: r.readInt(),
    y: r.readInt(),
    allowFollow: r.readUnsignedByte() === 1,
  };
}

/** PlayerPacketHandler.syncTime: int clientTime|int serverTime */
function decodeSyncTime(r) {
  return { clientTime: r.readInt(), serverTime: r.readInt() };
}

/** PlayerLoadCall.callFinish: STR hint */
function decodePushHint(r) {
  return { hint: r.readString() };
}

/** Move-packet flag bits — peony/game/GameObject.java */
export const MoveFlag = Object.freeze({
  POINT: 1 << 7, ANGLE: 1 << 6, HPMP: 1 << 5, STATE: 1 << 4, DETAIL: 1 << 3,
  NAME: 1 << 8, LEVEL: 1 << 9, FACTION: 1 << 10, EQUIPMENT: 1 << 11,
  SEX: 1 << 12, OWNER: 1 << 13, CLAZZ: 1 << 14, HORSE: 1 << 15,
});
export const UnitType = Object.freeze({ PLAYER: 1, NPC: 2, CREATURE: 3, CORPSE: 4, GATHER: 5 });

/**
 * peony/game/Unit.getMovePacket: byte (type|moveType) | int instanceId | optional blocks.
 *
 * Only the low byte of moveType survives the `pt.put(type|moveType)` truncation, so a
 * receiver can only see POINT/ANGLE/HPMP/STATE/DETAIL — the high flags (NAME, LEVEL, ...)
 * are lost from the header even though their payload follows. That makes the tail
 * undecodable in general, so we decode the POINT block (map + x/y, what G2 needs) and
 * stop: `partial: true`.
 */
function decodeUnitMove(r) {
  const head = r.readUnsignedByte();
  const m = { unitType: head & 0x07, flags: head & 0xf8, instanceId: r.readInt(), partial: true };
  if (head & MoveFlag.POINT) {
    const mapField = r.readShort() & 0xffff;
    if (mapField & 0x8000) {          // instanced map: id has bit15 set, followed by instance id
      m.mapId = mapField & 0x7fff;
      m.mapInstanceId = r.readInt();
    } else {
      m.mapId = mapField;
      m.mapInstanceId = -1;
    }
    m.x = r.readShort();
    m.y = r.readShort();
  }
  m.restBytes = r.remaining();
  return m;
}

/**
 * peony/game/chat/ChatMessage.getPacket:
 *   byte channel(+king bit7/officer bit6) | int sourceId
 *   | [byte faction  if channel==WORLD] | [STR destName if channel==PRIVATE]
 *   | STR sourceName | STR message | bytes attachment
 */
function decodeChat(r) {
  const raw = r.readUnsignedByte();
  const channel = raw & 0x3f;
  const m = { channel, isKing: (raw & 0x80) !== 0, isOfficer: (raw & 0x40) !== 0, sourceId: r.readInt() };
  if (channel === ChatChannel.WORLD) m.faction = r.readUnsignedByte();
  else if (channel === ChatChannel.PRIVATE) m.destName = r.readString();
  m.sourceName = r.readString();
  m.message = r.readString();
  m.attachment = r.readBytes();
  return m;
}

/** opcode -> { name, decode }. Anything absent is "known to exist, not ported yet". */
export const SERVER_MESSAGES = new Map([
  [OpCode.ERROR, { name: 'ERROR', decode: decodeError }],
  [OpCode.ACCOUNT_LOGIN_SERVER, { name: 'ACCOUNT_LOGIN_SERVER', decode: decodeAccountLogin }],
  [OpCode.ACTOR_LIST_SERVER, { name: 'ACTOR_LIST_SERVER', decode: decodeActorList }],
  [OpCode.ACTOR_CREATE_SERVER, { name: 'ACTOR_CREATE_SERVER', decode: decodeActorCreate }],
  [OpCode.ACTOR_LOGIN_SERVER, { name: 'ACTOR_LOGIN_SERVER', decode: decodeActorLogin }],
  [OpCode.GOMAP_ALLOW_SERVER, { name: 'GOMAP_ALLOW_SERVER', decode: decodeGoMapAllow }],
  [OpCode.SYNC_TIME_SERVER, { name: 'SYNC_TIME_SERVER', decode: decodeSyncTime }],
  [OpCode.PUSH_HINT_SERVER, { name: 'PUSH_HINT_SERVER', decode: decodePushHint }],
  [OpCode.UNIT_MOVE_SERVER, { name: 'UNIT_MOVE_SERVER', decode: decodeUnitMove }],
  [OpCode.CHAT_SERVER, { name: 'CHAT_SERVER', decode: decodeChat }],
]);

/**
 * Decode one received segment. NEVER throws: an unported opcode, a short read or a bad
 * string all come back as a result object the caller can log and move on from — the frame
 * itself was already delimited by its length, so the stream stays in sync either way.
 *
 * @returns {{opcode:number, name:string, known:boolean, body:object|null, error:Error|null, segment:Uint8Array}}
 */
export function decodeSegment(segment) {
  const reader = new UASegmentReader(segment);
  const opcode = normalizeOpcode(reader.opcode);
  const spec = SERVER_MESSAGES.get(opcode);
  if (!spec) {
    return { opcode, name: opcodeLabel(opcode), known: false, body: null, error: null, segment };
  }
  try {
    return { opcode, name: spec.name, known: true, body: spec.decode(reader), error: null, segment };
  } catch (err) {
    return { opcode, name: spec.name, known: true, body: null, error: err, segment };
  }
}
