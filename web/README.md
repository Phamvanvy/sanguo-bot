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

## Layout

- `bridge/` — Node `ws` <-> TCP passthrough. 1 WebSocket = 1 TCP = 1 game session, byte
  passthrough both ways, close propagation, per-direction buffer cap + backpressure.
- `client/src/net/` — the wire layer, framework-free ES modules:
  - `ua-codec.js` — `UASegmentReader` / `UASegmentWriter` + Java modified-UTF8. Port of
    `client/src/com/pip/io/UASegment.java`.
  - `ua-framing.js` — `frameSegment` + `UAFrameAccumulator` (handles split/merged frames).
    Port of `client/src/com/pip/io/UASocketConnection.java`.
  - `opcodes.js` — AUTO-GENERATED from `OpCode.java`; do not edit. Regenerate with
    `node tools/gen_opcodes.mjs`.
  - `codec.test.mjs` — unit tests (modified-UTF8 edge cases, framing, server-accepted parity).

## Run it

```bash
# 1. server must be up (see selfhost/)
cd selfhost && docker compose up -d

# 2. start the bridge (env: WORLD_HOST, WORLD_PORT=7000, BRIDGE_PORT=8080)
cd web/bridge && npm install && node bridge.js

# 3. codec unit tests (no server needed)
node web/client/src/net/codec.test.mjs

# 4. end-to-end login over the bridge (server + bridge running)
cd web/bridge && node test_ws_login.js --name vypv1 --password 123456
```

Regenerate opcodes after changing `OpCode.java`: `node tools/gen_opcodes.mjs`.
Credentials for tests come from CLI/env (`SANGO_USER`/`SANGO_PASS`) — never hardcoded.

## Next (G2)

Port the minimal login -> character select/create -> enter-map flow (`ACTOR_LOGIN`,
`ACTOR_CREATE`, `MOVE`, scene init). Unknown `_SERVER` opcodes must log + skip safely
(framing already skips the right number of bytes via frame length) — never crash.
