# Sango Game Server — Live Reconnaissance Report

**Target:** `https://play.minhchauh5.com/`  
**Date:** 2026-08-11  
**Tester:** Security Audit PoC

---

## 1. Target Architecture Discovery

### DNS & Hosting
```
play.minhchauh5.com -> 104.21.88.156, 172.67.185.199 (Cloudflare)
```
The site is behind Cloudflare CDN. Origin IP is hidden.

### Technology Stack (Live Site)
The live site is an **H5 (HTML5) version**, NOT the original Java server:

| Component | Technology |
|-----------|-----------|
| Frontend | Vite + TeaVM (Java -> JS compiler) |
| Game Logic | `spike.js` (1.9MB, obfuscated TeaVM output) |
| Transport | WebSocket (`wss://play.minhchauh5.com/ws`) |
| Assets | Service Worker + Cache Storage (`cdn.minhchauh5.com`) |
| Rendering | WebGL2 (`<canvas id="screen">`) |

### Key Difference from Source Code
The source code in `Game/Sango1.0-Server/` is the **original Java MIDP server**.  
The live site is a **TeaVM-compiled browser version** where Java bytecode is cross-compiled to JavaScript.

---

## 2. Endpoint Reconnaissance

### HTTP Endpoints Tested

| Endpoint | Status | Finding |
|----------|--------|---------|
| `GET /` | 200 | SPA shell (Vite + TeaVM) |
| `GET /ws` | 502 | WebSocket endpoint exists (needs Upgrade header) |
| `GET /ServerManager/execute` | 200 | Returns SPA shell (catch-all routing) |
| `GET /ServerManager/uploadfile` | 200 | Returns SPA shell (catch-all routing) |
| `GET /ServerManager/downloadfile` | 200 | Returns SPA shell (catch-all routing) |
| `GET /assets/shell-*.js` | 200 | Vite shell (16KB) |
| `GET /spike-*.js` | 200 | TeaVM game logic (1.9MB) |
| `GET /resources/jad.properties` | 200 | Game metadata |

### ServerManager Status: NOT DEPLOYED
The Java `ServerManager` servlets (`/execute`, `/uploadfile`, `/downloadfile`) are **not present** on the live server. All paths return the SPA shell due to catch-all routing. This means **PoC #2 (ServerManager RCE) does NOT apply to the live site** in its current deployment.

---

## 3. Client-Side Analysis

### Anti-Debugging / Guard Mechanisms (shell.js)
The shell contains multiple anti-tampering measures:
- **DevTools detection**: Blocks F12, Ctrl+Shift+I/J/C, Ctrl+U
- **Debugger trap**: `debugger` statement in loop
- **Timing check**: `performance.now()` diff > 120ms triggers guard
- **Window size check**: outerWidth - innerWidth > 200 triggers guard
- **WebSocket wrapper**: All WebSocket connections are tracked
- **Console clearing**: All console methods are no-ops
- **Context menu**: Right-click is blocked

### Game Logic (spike.js)
The 1.9MB spike.js is the TeaVM-compiled game. Key observations:
- Contains all game logic: maps, NPCs, items, combat, quests
- Contains network protocol handlers
- Contains anti-cheat logic (compiled from Java)
- Obfuscated variable names (e.g., `wgS`, `wgr`, `wgd`)
- String table contains game strings in Vietnamese

### WebSocket Protocol
- Endpoint: `wss://play.minhchauh5.com/ws`
- Protocol appears to be the same binary protocol from the Java server
- Client sends movement, chat, combat packets
- Server sends game state updates

---

## 4. Live PoC Execution Results

All 5 PoC scripts were executed against `play.minhchauh5.com`. Results below.

### PoC #1: GM Default Password — `poc/01_gm_default_password.py`
```
Target: play.minhchauh5.com:9000
Result: ALL 5 password attempts TIMED OUT
  - '123456789' -> timeout
  - 'admin'      -> timeout
  - 'password'   -> timeout
  - 'gm123'      -> timeout
  - '123456'     -> timeout
```
**Verdict:** GM port 9000 is NOT exposed on the public web server. The GM service may run on a backend game server behind Cloudflare, or on a different port. The vulnerability exists in the source code but the attack surface is not reachable from the public internet on this port.

### PoC #2: ServerManager Replay/RCE — `poc/02_servermanager_replay_rce.py`
```
Target: https://play.minhchauh5.com
Token forgery: SUCCESS — token generated with hardcoded 3DES key
Replay attack: CONFIRMED — impossible timestamp check (&& instead of ||)
Endpoints tested:
  /execute      -> 200 (SPA shell, servlet not deployed)
  /uploadfile   -> 200 (SPA shell, servlet not deployed)
  /downloadfile -> 200 (SPA shell, servlet not deployed)
```
**Verdict:** The ServerManager servlets are NOT deployed on the live web server. All paths return the Vite SPA shell (catch-all routing). The hardcoded 3DES key and impossible timestamp check are real code vulnerabilities, but the attack surface (Java servlets) does not exist in this deployment. **This PoC does NOT apply to the live site.**

### PoC #3: Cheat Commands — `poc/03_cheat_commands.py`
```
Secret: 'timeismoney@@!!' (hardcoded in Server.java:208)
Commands: /money, /credit, /exp, /item, /charge, /go, /load, /shut
Key risk: /shut calls System.exit(0) — kills server
         /load loads arbitrary Java classes (RCE)
Prerequisite: testserver=true in backend config
```
**Verdict:** Cannot confirm without backend access. If the backend game server runs with `testserver=true`, any player can type the secret in chat to unlock all cheat commands. The TeaVM client (spike.js) contains the compiled chat handler — the secret check and cheat commands may be compiled into the client-side code. **Risk remains if backend has testserver enabled.**

### PoC #4: Movement Hack — `poc/04_movement_hack.py`
```
Client sends raw x, y, time to server (PlayerPacketHandler.java:16070)
Wall collision check COMMENTED OUT (Player.java:1215)
Speed check only adds violation points
Ban requires antiCheat=true, but default is FALSE (Server.java:204)
Micro-stepping bypasses jump threshold
```
**Verdict:** The vulnerability is in the server-side Java code. Since the backend game server likely runs the same codebase, movement validation is probably still disabled. The TeaVM client sends raw coordinates via WebSocket — the same vulnerable protocol. **This is LIKELY exploitable** on the live server via crafted WebSocket packets.

### PoC #5: Anti-Bot Bypass — `poc/05_anti_bot_bypass.py`
```
antiBotModel defaults to LOG (Player.java:518)
antiPlugModel defaults to LOG
isBot() only effective in NONBENEFIT mode (not default)
Challenge only targets old phone models (bypass with modern UA)
Challenge uses client-reported metrics (trivially faked)
CRC algorithm shared between client and server
```
**Verdict:** Anti-bot is effectively disabled by default. Bots are only logged, never blocked. The challenge mechanism is trivially bypassed by reporting a modern phone model. **This is LIKELY exploitable** — bots can farm 24/7 without consequence.

---

## 5. Attack Surface Summary

### Confirmed Exposed
- WebSocket game protocol on `wss://play.minhchauh5.com/ws`
- All game assets publicly accessible
- Client-side anti-debugging (bypassable)

### Confirmed NOT Exposed
- GM admin port 9000 (timed out — not publicly accessible)
- ServerManager servlets (`/execute`, `/uploadfile`, `/downloadfile`) — not deployed

### Likely Exploitable (Needs Backend Confirmation)
- Movement speedhack/wallhack via WebSocket (PoC #4)
- Anti-bot bypass — LOG-only mode (PoC #5)
- Cheat commands if `testserver=true` on backend (PoC #3)

### Key Risk
The TeaVM compilation means the **same vulnerable Java code** is running as JavaScript in the browser AND potentially as Java on the backend game server. The backend game server (connected via WebSocket) likely runs the original Java codebase with all the vulnerabilities described in the PoCs.

---

## 6. WebSocket Live Test (PoC #6)

Script `poc/06_websocket_live_test.py` was executed against `wss://play.minhchauh5.com/ws`.

### Connection Test: SUCCESS
```
[OPEN] Connected to wss://play.minhchauh5.com/ws
[OK] WebSocket connected!
```
WebSocket endpoint is **live and accessible** — no authentication required to establish connection.

### Protocol Discovery: PARTIAL
Server **accepts connections** but **does not respond** to raw crafted packets. This indicates:
- The protocol uses a **specific binary format** (not plain text)
- Packets likely require **proper header structure** with correct opcodes
- The server **silently drops** invalid/malformed packets

### Key Findings from spike.js String Table:
| String | Meaning |
|--------|---------|
| `[ws] send op=` | WebSocket send with opcode |
| `[ws] recv` | WebSocket receive |
| `[ws] open ->` | Connection open handler |
| `[ws] closed` | Connection close handler |
| `[RECONN] ws CLOSED` | Auto-reconnect on disconnect |
| `[MOVE] type=0x` | Movement packet logging |
| `ui_chat_check_msg` | Chat message validation |
| `init secure transaction:` | **Encryption handshake** |
| `enc` | Encryption reference |
| `phone.imei` | IMEI-based device identification |
| `game_netplayer` | Network player entity |

### Critical Discovery: Encryption Layer
The strings `init secure transaction:`, `enc`, `finish secure transaction:` indicate the game protocol has an **encryption layer**. This means:
- Raw binary packets are **encrypted/obfuscated**
- A handshake (`init secure transaction`) must complete before game packets are accepted
- The encryption likely uses a shared key embedded in both client (spike.js) and server
- This is an **additional security layer** beyond the protocol format

### Protocol Structure (from spike.js analysis):
- Binary protocol over WebSocket
- Opcode-based packet dispatch (`[ws] send op=`)
- Movement packets: `[MOVE] type=0x` with spriteType and position data
- Chat packets: `ui_chat_check_msg` validation
- Secure transaction layer: `init secure transaction` → `enc` → `finish secure transaction`

---

## 7. Recommendations for Further Testing

1. **Extract encryption key from spike.js**: Find the `init secure transaction` implementation to extract the shared key/algorithm
2. **Reverse engineer packet format**: Trace `[ws] send op=` to find the DataOutputStream write order for each packet type
3. **Implement protocol handshake**: Complete the secure transaction handshake before sending game packets
4. **Backend discovery**: Scan for game server ports (typically 9000-9100 for GM, custom ports for game)
5. **Test movement validation**: Once protocol is understood, send crafted movement packets to test wall collision
6. **Test cheat commands**: If testserver mode can be determined, test the `/money`, `/item`, `/shut` commands
