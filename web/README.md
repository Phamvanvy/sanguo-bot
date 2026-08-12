# web/ — Browser client for the self-hosted sanguo server

Layers (see the plan in `~/.claude/plans/`): a browser talks WebSocket to a bridge, the
bridge relays raw bytes to the Java world server over TCP, and a JS codec speaks the game's
"UA" wire protocol.

```
Browser (JS)  --WebSocket(binary)-->  bridge  --TCP "UA"-->  world :7000
   codec + framing (web/client/src/net)     1 WS = 1 TCP = 1 session
```

## Status

- **G1 bridge + codec: DONE and tested.** A raw login round-trips through the bridge using
  the shared codec (`ACCOUNT_LOGIN_CLIENT` -> `ACCOUNT_LOGIN_SERVER`, `accountId`/`name`
  asserted). The codec is pinned byte-for-byte to the exact frame the live server accepted.
- **G2 login -> character -> world -> movement: DONE and tested.** Against the live server:
  account login, character list, character creation, `ACTOR_LOGIN`, spawn map + coords from
  `GOMAP_ALLOW_SERVER`, and movement the server accepts — verified by reconnecting and
  reading the position back, not by trusting our own send. The ~9 unported init packets the
  server pushes after `ACTOR_LOGIN` are logged and skipped; the stream stays in sync.
  Works both from Node (`test_ws_g2.js`) and from the browser page (`client/index.html`).

## Layout

- `bridge/` — Node `ws` <-> TCP passthrough. 1 WebSocket = 1 TCP = 1 game session, byte
  passthrough both ways, close propagation, per-direction buffer cap + backpressure. It also
  serves `client/` over HTTP on the same port (dev convenience; `STATIC_DIR=off` disables).
- `client/index.html` + `client/src/app/debug-client.js` — the G2 browser client: log in,
  pick/create a character, enter the world, move with the arrow keys, watch the packet log.
  No game rendering — the canvas is a debug dot plot. Real map/sprite rendering is G3.
- `client/src/net/` — the wire layer, framework-free ES modules:
  - `ua-codec.js` — `UASegmentReader` / `UASegmentWriter` + Java modified-UTF8. Port of
    `client/src/com/pip/io/UASegment.java`. Reads are bounds-checked: a short packet raises
    instead of quietly returning garbage.
  - `ua-framing.js` — `frameSegment` + `UAFrameAccumulator` (handles split/merged frames).
    Port of `client/src/com/pip/io/UASocketConnection.java`.
  - `opcodes.js` — AUTO-GENERATED from `OpCode.java`; do not edit. Regenerate with
    `node tools/gen_opcodes.mjs`.
  - `protocol.js` — the protocol registry: per-message field layouts, each traced to the
    Java method that writes/reads it. `decodeSegment()` never throws — an unported opcode or
    a bad body comes back as a result object to log and skip.
  - `session.js` — `GameSession`: serial-matched request/response, events per message,
    unknown/error counters, and the login -> list -> create -> `enterWorld` -> move flow.
    Only a framing desync is fatal.
  - `codec.test.mjs`, `protocol.test.mjs` — unit tests (no server needed).

### Layout gotchas found the hard way

- `Packet.put(byte[])` writes an **int32 length prefix**, so `ACTOR_LOGIN_SERVER` is
  `int serial | int blobLen | Player.toClientBytes()` — not the blob bare.
- `Packet.put(int)` truncates to **one byte** (counts, levels, flags).
- `Packet.putString` is standard UTF-8 with a UTF-16BE escape hatch (high bit of the length),
  while `DataOutputStream.writeUTF` inside `toClientBytes` is Java *modified* UTF-8.
  `readString()` handles both.
- `ERROR` is opcode `-1`, i.e. `0xFFFF` on the wire — normalise before lookup.
- `ACTOR_LOGIN_SERVER` and `UNIT_MOVE_SERVER` are decoded **partially** on purpose (`partial:
  true`, `restBytes`): the tails are equipment/buff/cooldown sub-formats G2 does not need.

## Run it

```bash
# 1. server must be up (see selfhost/)
cd selfhost && docker compose up -d

# 2. start the bridge (env: WORLD_HOST, WORLD_PORT=7000, BRIDGE_PORT=8080, STATIC_DIR)
cd web/bridge && npm install && node bridge.js

# 3. unit tests (no server needed)
node web/client/src/net/codec.test.mjs
node web/client/src/net/protocol.test.mjs

# 4. end-to-end over the bridge (server + bridge running)
cd web/bridge && node test_ws_login.js --name vypv1 --password 123456   # G1
cd web/bridge && node test_ws_g2.js    --name vypv1 --password 123456   # G2

# 5. the browser client
#    open http://127.0.0.1:8080/  -> log in -> pick/create a character -> arrow keys
```

`test_ws_g2.js` creates a character if the account has none (`--char`, `--sex`, `--clazz`,
`--faction`), then moves `--steps` x `--step` tiles; `--no-move` skips the movement checks.
Movement must land on tiles the server's pathfinder accepts, so keep steps small.

Regenerate opcodes after changing `OpCode.java`: `node tools/gen_opcodes.mjs`.
Credentials for tests come from CLI/env (`SANGO_USER`/`SANGO_PASS`) — never hardcoded.

## Next (G3)

Asset feasibility spike first: extract and render **one** map tile + **one** sprite from the
`PipImage`/`ImageSet` format in `dist/data.7z` before committing to a renderer. Then Canvas
2D, and widen opcode coverage feature by feature (scene units -> chat -> bag -> combat).
Ported-opcode backlog, straight from the G2 run: `SYNC_PLAYER_SERVER(144)`,
`UNIT_MULTI_REFRESH/UNIT_DETAIL_SERVER(194)`, `AREAQUEST_INFO_SERVER(161)`,
`QUEST_START_ADDED_SERVER(125)`, `SYNC_BUFF_SERVER(291)`, `VIEW_ACCEPT_SERVER(412)`,
`MUSIC_SERVER(620)`, `ANTI_PLUG_SERVER(2038)`.
