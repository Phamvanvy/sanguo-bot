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
- **G3a asset spike: DONE and verified visually.** The original `.pip`/`.ldf`/`.ctn` art and
  the `client.pkg` map format decode in JS; the spawn map and the player character both render
  from `selfhost/runtime/data`. See "G3a — asset spike" below.
- **G3b Canvas2D renderer: DONE and verified in a real browser.** `client/game.html` logs in,
  loads the spawn map's own art out of the server's data directory, and draws it: background
  tiles, animated map decor, and the character walking on it in the right direction, at the
  coordinates the server accepts. Verified by `tools/render_smoke.py` (Playwright, against the
  live stack): 585 tile blits + 146 decor objects, character art loaded, click-to-walk moves it
  right, `MOVE_CLIENT` accepted, zero console errors.
- **G3c the game shell: NOT THE REAL UI — frozen as a debug shell.** `client/game.html` boots
  behind a splash, logs in, picks a character and plays under a HUD laid out after the live H5
  client (play.minhchauh5.com). It works, but it was *drawn by us* — `theme.css`, a custom walk
  pad, a custom minimap — and the goal of this project is the original game, not a re-creation
  of it. So it stays only as a harness for testing the renderer, protocol, movement and NPCs,
  and **gets no further UI features**: no bag, quest, shop or skill panels in HTML/CSS. It is
  retired once G3d renders the world UI. See "G3c — the shell" below.
- **G3d the original UI runtime: IN PROGRESS.** The client's interface is not layout code, it
  is bytecode: every window, panel and button is a function in a `.etf` script that the client's
  own VM executes. Those scripts are already on our server. The container reader, the
  instruction set and the interpreter are ported and tested; the syscall layer and the widget
  toolkit are next. See "G3d — the original UI runtime" below.

## Layout

- `bridge/` — Node `ws` <-> TCP passthrough. 1 WebSocket = 1 TCP = 1 game session, byte
  passthrough both ways, close propagation, per-direction buffer cap + backpressure. It also
  serves `client/` over HTTP on the same port (dev convenience; `STATIC_DIR=off` disables), and
  part of the server's game data read-only under `/data/` (`DATA_DIR`, `off` disables) plus
  `/data/areas.json` — the map id -> area package index built by `bridge/asset-index.js`.
  It runs as the `bridge` service in `selfhost/docker-compose.yml`; run
  `npm --prefix web/bridge ci` once, since `web/` is mounted read-only.

  **`/data/` is an allow-list, not a file server.** That directory is the *server's*: quest
  scripts, drop tables, NPC spawns, per-map collision and exits all live in it. Only
  `Areas/*/client(_l).pkg`, `client_pkg/**` and `client_res/**` are reachable; everything
  else — including `info.xml`/`game.map` sitting next to the packages a map needs — is 403.
  Anything the client needs from the rest should be *derived* into a small document served
  by the bridge (that is what `/data/areas.json` is), not exposed by widening the list.
  `bridge/asset-index.test.mjs` pins this, along with the containment check that keeps
  `..` inside the data directory. Assets are cached hard (`immutable` + ETag/304); the
  client files revalidate. Also: GET only, a per-address request budget (429 over it), and
  caps on concurrent sessions overall and per address (`MAX_SESSIONS`, `MAX_PER_IP`).
  None of this makes the bridge safe to expose directly — it makes an accident survivable.
- `client/game.html` + `client/src/app/game-client.js` — **the client**: boot, log in, pick a
  character, and play on the rendered map. Click or tap the map, drag the walk pad, WASD/arrows
  to step, `+`/`-` to zoom, Enter to chat, F1 for the packet log.
- `client/src/ui/` — the shell around the renderer. See "G3c — the shell" below.
- `client/index.html` + `client/src/app/debug-client.js` — the G2 wire-layer client: same flow
  with a packet log and a debug dot plot instead of a view. Kept because it isolates protocol
  problems from rendering ones.
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
- `client/src/assets/` — the asset layer (G3a): decoders for the game's map and image formats.
  See "G3a — asset spike" below.
- `client/src/game/` — the renderer (G3b). See "G3b — the renderer" below.
- `tools/assets/` — Node-only tooling around it: `spike.mjs` (the CLI that renders proof PNGs),
  `png.mjs` (a minimal PNG codec), `gen_random_golden.sh` + `RandomGolden.java` (regenerate the
  PRNG goldens from the game's own Java source).

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
# 1. server + bridge (see selfhost/). The bridge is a compose service; it needs its one
#    dependency installed on the host first, because web/ is mounted read-only.
npm --prefix web/bridge ci
docker compose -f selfhost/docker-compose.yml up -d

# 2. …or run the bridge on the host instead, against the containers' published ports
#    (env: WORLD_HOST, WORLD_PORT=7000, BRIDGE_PORT, STATIC_DIR, DATA_DIR)
cd web/bridge && node bridge.js

# 3. unit tests (no server needed)
node --test web/client/src/net/codec.test.mjs web/client/src/net/protocol.test.mjs
node --test web/client/src/assets/assets.test.mjs web/client/src/assets/inflate.test.mjs
node --test web/client/src/game/game.test.mjs    # the last three need selfhost/runtime/data,
                                                 # else those cases skip
node --test web/client/src/ui/ui.test.mjs
node --test web/client/src/vm/vm.test.mjs web/client/src/vm/vm-exec.test.mjs

# 3b. what the shipped UI scripts actually call (scopes the G3d syscall work)
node tools/vm/census.mjs --model Flash --top 20

# 4. end-to-end over the bridge (server + bridge running)
cd web/bridge && node test_ws_login.js --name "$SANGO_USER" --password "$SANGO_PASS"   # G1
cd web/bridge && node test_ws_g2.js    --name "$SANGO_USER" --password "$SANGO_PASS"   # G2

# 5. the browser clients
#    http://127.0.0.1:8090/game.html  -> log in -> pick a character -> click to walk   (G3b)
#    http://127.0.0.1:8090/           -> the same flow with a packet log, no rendering (G2)

# 6. the rendering smoke test (server + bridge running; drives a real Chromium)
SANGUO_ACCOUNT=... SANGUO_PASSWORD=... python tools/render_smoke.py
```

`test_ws_g2.js` creates a character if the account has none (`--char`, `--sex`, `--clazz`,
`--faction`), then moves `--steps` x `--step` tiles; `--no-move` skips the movement checks.
Movement must land on tiles the server's pathfinder accepts, so keep steps small.

Regenerate opcodes after changing `OpCode.java`: `node tools/gen_opcodes.mjs`.
Credentials for tests come from CLI/env (`SANGO_USER`/`SANGO_PASS`) — never hardcoded.

## G3a — asset spike: DONE

The original art decodes and renders from the server's own data directory. Verified against
`selfhost/runtime/data`: **all 179 maps in all 96 area packages decode with zero failures**,
and both the spawn map and a player character render correctly.

```bash
node tools/assets/spike.mjs                 # spawn map 1395 + the player character
node tools/assets/spike.mjs --map 48         # a precise-tile map instead of a blurred one
node web/client/src/assets/assets.test.mjs   # 39 tests, no server needed
```

Output lands in `web/client/assets/spike/` (gitignored): `map1395.png`,
`map1395_with_character.png` (the character standing at the exact `x=66 y=176` the live server
gave G2), `sprite_male_poses.png` (all 52 animations), `tile_map1395_8x.png`.

### `client/src/assets/` — the asset layer

Framework-free ES modules, ports of the Java client's loaders. gzip and PNG decoding are
**injected** (`{ inflate, decodePNG }`) so the same code runs in Node (zlib) and in a browser
(`codecs.js`, backed by the synchronous `inflate.js`/`png.js` — see the G3b notes for why the
platform's async decompression was not an option).

- `data-in.js` — `DataInputStream` over a `Uint8Array`; reuses the modified-UTF8 decoder from
  `net/ua-codec.js`. Bounds-checked: a misparse throws instead of yielding garbage.
- `package-file.js` — the `SANGUOPKG` container + `GamePackage` (an area's `client.pkg`:
  header, maps, tilesets, landforms, decor).
- `game-map.js` — `GameMap` (`<id>.m`) plus the blurred-background generator
  (`createBlurMapBuffer` / `decodeBlurCell`).
- `pip-image.js` — the `.pip` image format, all five variants, and the 8 MIDP `trans` codes.
- `landform-image.js` — `.ldf` landform tilesets (shape/priority table + weighted picking).
- `pip-animate-set.js` — `.ctn`/`.anp` animate sets: animation -> frame -> pieces of `.pip`s.
- `java-random.js` — `java.util.Random`'s LCG, pinned to Java-generated goldens.
- `inflate.js` / `png.js` / `codecs.js` — synchronous DEFLATE-zlib-gzip and PNG decoding, so
  the whole chain runs unchanged in a browser (G3b). Parity-tested against `node:zlib`.
- `tools/assets/png.mjs` — PNG *encoding* for the Node tooling (it reuses `png.js` to decode,
  with `zlib` swapped in).

### Asset format notes worth keeping

- **Global map id = `(areaId << 4) | localMapId`.** Map 1395 = area 87, local map 3. Area
  *directory* names (`87_1`, `12_1`) are not the area id — read it from `0.stg`.
- **Two background types, and the rare one is the simple one.** Type 0 ("precise") indexes
  `tile.ts` -> `tile.pip`; only 2 of 179 maps use it. Type 1 ("blurred") stores just a landform
  id per cell plus a seed, and **regenerates every tile by replaying the game's PRNG** — so
  `java-random.js` must match Java call-for-call, including the branches that return early
  *without* drawing from the PRNG. A one-step drift changes the whole map silently, which is
  why the PRNG has Java goldens (`sh tools/assets/gen_random_golden.sh`) and the generated
  terrain is checksum-pinned in the tests.
- **Cells pack up to 3 stacked tiles into one int** (10/11/11 bits). The bottom layer's field
  is shifted to bit 22, so its top landform bit falls off the end of the int and the client
  reads back only 3 bits. That asymmetry is in the original and is locked in by a test.
- **`.pip` is a *block* image: N palettes x M blocks.** Drawable frame `id` means "block
  `id % M` with palette `id / M`", which is how one file holds every recolour. Magic bytes:
  `PIP`/`PIE` = 8-bit indices, `PJP`/`PJE` = 16-bit indices, `E` = replaceable palette,
  `PIM` = "merge mode", where pixels live in embedded PNGs instead of palette indices.
- **Merge mode needs a PNG decoder, and the Flash/Android asset sets are merge mode
  throughout.** A shared PLTE/tRNS blob is stored once and spliced into each PNG at byte 33.
  The top 2 bits of a frame's x coordinate are really an image index (up to 4 sheets per file).
- **Chunk id `DATA` means gzipped; `DUNZ` means stored.** Counter-intuitive, and reading it
  backwards produces garbage rather than an error.
- **Sprite pieces use all 8 MIDP `trans` codes**, and codes 4..7 swap width and height — map
  tiles only ever store 2 bits (codes 0..3).
- **`client_pkg/Flash/` is the right asset set for a web client** — same resolution class as a
  browser, and `male.ctn` there is the player character (52 animations over 4 body-part pips).
- **Unit coordinates from the server are ground positions.** Sprite piece offsets are relative
  to an anchor at the character's feet and are therefore negative.

## G3b — the renderer: DONE

`client/game.html` draws the live world. Nothing is pre-converted: the browser downloads the
same files the 2011 server ships (an area package is ~50 KB, the player's animate set ~200 KB)
and decodes them in JS.

### `client/src/game/`

- `asset-source.js` — fetches and caches game files from the bridge's `/data/`, resolves a map
  id to its area package through `/data/areas.json`, and loads a `.ctn` set together with the
  `.pip` images it names.
- `map-scene.js` — one loaded map: the background rasterised **once** into an offscreen canvas
  (the largest map here is 1504x1600 px, so this trades thousands of per-frame tile blits for
  one), plus the decor as a display list.
- `sprite-set.js` — a `PipAnimateSet` made drawable: every (image, frame, transform) is
  rasterised once and blitted after that. Characters, decor and effects all use it.
- `renderer.js` — the Canvas2D view. Paint order is the original client's: background, ground
  decor, then the role layer **interleaved with units by y**, then sky decor over everything.
  `buildDrawList()` is pure, so the ordering and culling rules are unit-tested.
- `camera.js`, `movement.js`, `animation.js`, `unit-view.js` — pure logic: framing/clamping,
  facing and step-by-step walking, the animation table, and per-unit state.
- `graphics.js` — the only module that touches canvas APIs, so everything above it tests in Node.
- `game.test.mjs` — 20 tests over all of the above.

### Rendering notes worth keeping

- **The asset decoders are synchronous, so the browser needs a synchronous inflate.**
  `DecompressionStream` is async and would have forced every decoder (and every caller) to
  become async, so `assets/inflate.js` implements DEFLATE/zlib/gzip directly and `assets/png.js`
  decodes the embedded sprite sheets on top of it. Both are checked byte-for-byte against
  `node:zlib` on the game's own files (`assets/inflate.test.mjs`). Node tooling still injects
  `zlib` where throughput matters — that is what the `{ inflate, decodePNG }` option is for.
- **An area directory name is not its area id**, so `bridge/asset-index.js` scans every
  `Areas/*/client.pkg` once per bridge process and serves the map -> directory map as JSON.
  The alternative was the browser downloading 96 packages to find one.
- **Animation indices come from the server's script VM in the original client**
  (`GameSprite.vm_game_set_animate_index`), which is far out of scope here. `animation.js`
  instead uses the layout read off the asset: `male.ctn` is 13 groups of 4 animations, one per
  direction in `Tool.DIR_*` order (down, right, left, up), with group 0 = idle and group 4 =
  walk. That is pinned by a test on the real file (walk = 4 steps of 4 distinct frames, idle
  re-uses 2-3, and the two groups share no art).
- **Movement is client-led and server-checked.** A click sets a target; the unit walks at
  `Unit.SPEED` (45 px/s) and a `MOVE_CLIENT` goes out every 200 ms with the position reached
  (~9 px steps). The server re-runs its pathfinder on each and silently drops what it dislikes,
  so big jumps would simply vanish. There is **no client-side collision check yet** — walking
  into a wall means the server ignores those moves while the sprite keeps going, and the next
  authoritative position snaps it back.
- **Other units are drawn from `UNIT_MOVE_SERVER` only**, which is a partial decode (the
  header's high flag bits are truncated by `Packet.put(int)`), so we get type + id + position
  but no name or appearance: players borrow `male.ctn`, everything else renders as a marker.
  There is no "unit left" opcode ported either, so units unseen for 30 s are dropped.

## G3c — the shell: DONE

The renderer had a debug page around it: a form, a packet log, and a line of counters. The
shell replaces that with the interface the game actually ships. The layout is copied from the
live H5 client at `play.minhchauh5.com` — the same game, re-released — captured screen by
screen with Playwright: portrait + HP/MP top left, place name and a labelled action bar top
right, minimap under it, walk pad bottom left, round buttons bottom right, chat log along the
bottom with an exp strip under it, and panels that are a gold header with a red title plaque
over a parchment body.

### `client/src/ui/`

- `theme.css` — the design system: gold frames, parchment panels, red plaques, stat bars.
  Everything sizes off one `--ui` variable, so the whole HUD scales together on a small screen.
- `ui-assets.js` — the game's own interface art, made usable from the DOM: a `.pip` frame in,
  a data URL out. Also `portraitCanvas`, which crops a portrait out of the walking sprite.
- `minimap.js` — the scene's background bitmap scaled down, with unit dots and the camera
  rectangle over it. `fitMap`/`minimapToWorld` are pure, so click-to-walk is unit-tested.
- `joystick.js` — the walk pad. `stickVector` is pure; the class is pointer plumbing.
- `ui.test.mjs` — 7 tests over the two pure modules.

### Shell notes worth keeping

- **The icons are the game's, the frames are CSS.** `ui_res480.pip` and `ability42x42.pip` hold
  the real bag/chest/scroll/coin icons and they are used as-is. The frames around them are not:
  the original chrome is bitmap art cut for a 480x320 phone, and blown up to a desktop window it
  reads as a postage stamp. Buttons carrying **Chinese text** (`確定`, `返回`, `選單`) are
  skipped outright — this client is in Vietnamese, like the live one.
- **Atlas indices were checked by rendering them, not counted off the sheet.** A contact sheet
  of `ui_res480.pip` is 9 wide, so it is easy to read an index one out — and one out gives you a
  plausible-looking wrong icon rather than an error. The named frames in `ui-assets.js` were
  each dumped and looked at.
- **The HUD only shows numbers the server sent.** `ACTOR_LOGIN_SERVER` carries hp/mp/exp/money
  and the four attributes, so those are real. Nothing *updates* them yet — `SYNC_PLAYER_SERVER`
  is unported — so the character panel says so rather than implying a live gauge. Features
  whose opcodes are missing (bag, quests) name the opcodes instead of drawing an empty bag.
- **The walk pad reports a direction, not a destination.** Each frame the target is pushed
  `PAD_LOOKAHEAD` px ahead of the character, which is what makes held-down movement continuous
  and keeps every `MOVE_CLIENT` a short step the server's pathfinder will accept.
- **Zoom fits the map inside the window, it does not cover it.** The maps are small (the spawn
  map is 352x320 px) and covering a 1280x720 window would need 4x, which pins the camera to the
  map edge and the character to a corner. Letterboxing a small map looks better than that.
- **The packet log did not go away, it moved.** It is the same log G2 needed, behind the "Log"
  button or F1, and still the first place to look when an opcode misbehaves.

## G3d — the original UI runtime: IN PROGRESS

The 2014 client does not hard-code its interface. `com/pip/ui/VM.java` is a bytecode
interpreter ("GTVM"), and every screen is a compiled script — `game_world`, `game_panel`,
`ui_bag`, `ui_ability`, … — running on it over the widget toolkit in `com/pip/gui/`.
`VMGame.java` wires those scripts to the server. So the way to get the real UI is to replace
the Java ME runtime under the scripts, not to redraw the screens:

```
original .etf scripts  ->  VM (this port)  ->  GWidget/GContainer/GWindow  ->  drawing
syscalls  ->  original .pip art  ->  Canvas
```

The scripts ship with the server we already run: `selfhost/runtime/data/scripts/<UIModel>/`,
and their strings are already Vietnamese. **The chosen model is `Flash`** — the same set the
live H5 re-release runs. The Java source to port from is `Game/sangobuildVn/client/src/`,
because that is the revision `selfhost/build_runtime.sh` stages the data from.

### `client/src/vm/`

- `etf.js` — the script container (`VM.loadETF`): `EGL0`/`EGL1` header, `ST` string table,
  `CT` code table, `CB` callbacks, `LB` linked libraries. Strings are UTF-16BE with a 1- or
  2-byte **character** count, unlike the modified UTF-8 the wire protocol and assets use.
- `isa.js` — the 75 instructions, with `INSTRUCTION_LENGTH`/`STACK_EFFECT` transcribed from
  VM.java, plus a disassembly walker.
- `vm.js` — the interpreter, ported call-for-call: pointer encoding, dynamic heap with its
  32-slot temp ring, stack frames, `TSWITCH`/`LSWITCH`, library calls, callbacks, and the
  block/resume path a script uses while waiting on the server. Syscalls are *injected*, so
  this file stays a pure machine and the platform layer can be filled in feature by feature.
- `vm.test.mjs` (9) + `vm-exec.test.mjs` (20) + `syscalls-core.test.mjs` (12) — all pass.
- `tools/vm/census.mjs` — scopes the remaining work from the bytecode instead of guessing:
  which syscalls the shipped scripts actually call, how often, and from where.

**G3d syscall layers (in progress).** The host is now a *stack* of layers behind one
`composeHost(platform, ...)` dispatcher; each layer returns `UNHANDLED` for ids it does not
own, so they compose without knowing each other's id maps:

- `runtime.js` — the Java value types scripts see through syscalls: `JavaVector`,
  `SortHashtable` (insertion-ordered, boxed-Integer keys compare by value), `VMInteger`,
  `DataInputStream` / `ByteArrayOutputStream` over modified UTF-8.
- `ua-segment.js` — the script-side `UASegment`: one growable buffer, read cursor after the
  type field, array writers/readers with u16 counts; what `UWAP_Create` hands back.
- `tool.js` — the `Tool` statics scripts reach: `splitString`, `mergeString(2)`, integer
  `sqrt`/`distance`, and the rect predicates with Java's exact edge semantics.
- `syscalls-core.js` — ~120 syscall cases needing no world/rendering: strings, objects,
  `Realize`, vectors, hashtables, streams, UWAP, globals, keys/time/random, and the
  `PauseUICycle`/`ResumeUICycle` pair that parks a script mid-function.
- `gfx.js` — `javax.microedition.lcdui.Graphics`/`Font`/`Image` onto Canvas2D: MIDP anchors,
  intersecting clips, degrees-CCW arcs, inclusive-edge rects, `drawRegion` with the game's
  own 8 trans codes.
- `widgets.js` — `GWidget`/`GContainer`/`GWindow`/`GScrollBar`. `vmData` is an Int32Array in
  the exact GW_* layout; layout H/V/GRID/GRID2/GRID3/BORDER, scroll bookkeeping, focus, and
  the CYCLE/CYCLEUI/PAINT/PROCESSPACKET call stacks follow GContainer.java/GWindow.java.
- `syscalls-ui.js` — the drawing block (0x0011..0x001E, 0x201E.., 0x571D..) and the widget
  block (0x12xx + the GWindow statics at 0x5705..0x571F).
- `vmgame.js` — `VMGame` + `VMGameManager`: the registry keyed by vmKey, window stacks per
  script, common callbacks, `cycle()`/`handleSegment()`/`drawAll()`, async ETF loading with
  the STATE_REQUESTING_VMUI park, and hit testing. Script loading is injected
  (`loadScript(name)` -> inflated bytes), so the same code runs under Node tests.

Still open before the boot chain runs end to end: the resource layer behind the
`ImageSet_*`/`AnimateSet_*`/`Res*` syscalls (the decoders already exist in
`client/src/assets/`; they need ImageSet/PipAnimateSet wrappers with drawFrame), the
remaining concrete widgets scripts construct (`GLabel`, `GIcon`, `GTextArea`, `GLinePanel`,
`GImageNumer`, `GGameIcon`), the game-world processor syscalls (0x50xx/0x55xx/0x56xx), and
then `game_init -> game_world -> game_panel` over `net/session.js`.

Census of the Flash set (105 scripts, all parse and fully disassemble): 4 670 functions,
256 434 instructions, 61 of 75 opcodes used, **620 distinct syscalls** — 475 of them reachable
from the core scripts. Only 19 ids (38 call sites) have no case in either Java client, and all
of them are Symbian/Android SMS/address-book hooks that a browser stubs out.

### VM notes worth keeping

- **Java int semantics are load-bearing.** The stack and static heap are `Int32Array` so stores
  truncate like Java, `MUL` goes through `Math.imul` (a double would keep bits Java drops),
  division truncates toward zero, and every `codeData[...]` byte is sign-extended.
- **A VM value is either a number or a pointer**, told apart by its top bits: bit 31 means a
  string-table reference (library id + index), otherwise bits 30..26 are a data type — ≥ 20 is
  an `Object[]`, and bit 25 marks a pointer to one *element* with its index in bits 24..12.
- **Struct instances are `int[]`**; `STLOAD`/`STSAVE` index straight into them.
- **Jump tables are relative to the END of their instruction**, not to the function start the
  way `JMP`/`JEQ`/`JNE` operands are. `TSWITCH` is dense (`0xFFFF` in a slot means "default"),
  `LSWITCH` is a sorted table binary-searched by `searchTable`.
- **Only `TSWITCH`/`LSWITCH` are variable-length.** That makes a full disassembly a real test:
  one wrong length desynchronises the decoder, so 256 k instructions decoding cleanly and every
  function ending exactly on its boundary pins the whole table.
- **The temp ring is never freed.** The first 32 heap cells are a ring buffer for objects that
  syscalls hand to scripts; `heapFree` ignores them by design, and they get overwritten in turn.
- **`instructionLimit` is ours, not the original's.** A runaway script froze a 2014 handset;
  in a browser it freezes the tab and the debugger with it, so tests and dev builds set a
  budget. Default 0 = unlimited, exactly like Java.

## Next

Continue G3d in this order:

1. **Resource layer** — `ImageSet`/`PipAnimateSet` wrappers over the existing
   `client/src/assets/` decoders, plus a `syscalls-res.js` layer for the `ImageSet_*`,
   `AnimateSet_*`, `AnimatePlayer` and `Res*` blocks (0x0031..0x004C, 0x1301..0x1339).
2. **Concrete widgets** — `GLabel`, `GIcon`, `GTextArea`, `GLinePanel`, `GImageNumer`,
   `GGameIcon` and their 0x1243..0x12D9 syscall cases, on top of the toolkit already ported.
3. **Boot chain** — preload the `lib_builtin` + core scripts through `loadScript`, boot
   `game_init -> game_world -> game_panel` in a `VMGameManager`, wire `handleSegment()` to
   `net/session.js` events (`CONN_VM_DATA` / `CONN_VM_COMMAND`) and drive
   `cycle()/drawAll()` from the existing game loop.
4. **World processor syscalls** — 0x50xx/0x55xx/0x56xx against the G3b renderer's scene.

Only after the original world UI runs do the feature scripts (`ui_bag`, `ui_ability`,
quests, shops) get opened — and only then is `client/src/ui/` retired.

Still open from earlier milestones, and now expected to come *from the scripts* rather than
from hand-written inference — G3b guesses the animation groups (0 = idle, 4 = walk) off the
asset, but in the real client the VM scripts choose animations:

Widen opcode coverage feature by feature (scene units -> chat -> bag -> combat), and give
non-player units their real art (`NPCTemplates/` + `client_pkg.xml` name the sets).
Ported-opcode backlog, straight from the G2/G3b runs: `SYNC_PLAYER_SERVER(144)`,
`UNIT_MULTI_REFRESH/UNIT_DETAIL_SERVER(194)`, `AREAQUEST_INFO_SERVER(161)`,
`QUEST_START_ADDED_SERVER(125)`, `SYNC_BUFF_SERVER(291)`, `VIEW_ACCEPT_SERVER(412)`,
`MUSIC_SERVER(620)`, `ANTI_PLUG_SERVER(2038)`.
