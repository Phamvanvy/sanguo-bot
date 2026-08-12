# Sango Game Server — Vulnerability Proof-of-Concept Scripts

This directory contains PoC scripts demonstrating critical security vulnerabilities
found in the Sango 1.0 game server codebase.

## Quick Start

```bash
# Run all PoCs in demo mode (no network connection needed)
python 01_gm_default_password.py --demo
python 02_servermanager_replay_rce.py --demo
python 03_cheat_commands.py --demo
python 04_movement_hack.py --demo
python 05_anti_bot_bypass.py --demo
```

## Vulnerability Summary

| # | Severity | Title | Script |
|---|----------|-------|--------|
| 1 | **CRITICAL** | GM Default Password & RCE | `01_gm_default_password.py` |
| 2 | **CRITICAL** | ServerManager Replay & RCE | `02_servermanager_replay_rce.py` |
| 3 | **CRITICAL** | Cheat Commands in Chat | `03_cheat_commands.py` |
| 4 | **HIGH** | Movement Speedhack/Wallhack | `04_movement_hack.py` |
| 5 | **HIGH** | Anti-Bot Bypass | `05_anti_bot_bypass.py` |

---

## PoC #1: GM Default Password & Arbitrary Code Execution

**Source files:**
- `AdminService.java:13` — password = `"123456789"`
- `AdminPacketHandler.java:839-870` — adminLogin (no rate limit, logs password)
- `AdminPacketHandler.java:682-696` — exec() via `Class.forName().newInstance()`
- `AdminPacketHandler.java:225-260` — hotswap() via `Instrumentation.redefineClasses()`

**Attack flow:**
1. Discover GM port (often on same IP as game server)
2. Login with default password `123456789`
3. Execute arbitrary Java `Runnable` via reflection
4. Hot-swap running classes at runtime

**Impact:** Full server compromise if GM port is reachable from attacker.

---

## PoC #2: ServerManager Replay Attack & RCE

**Source files:**
- `SecurityUtils.java:9` — hardcoded 3DES key `"laoutqqhd9272l;javnzy220"`
- `SecurityUtils.java:140` — impossible timestamp check `if (gap < -60000 && gap > 60000)`
- `ExecuteServlet.java:56` — arbitrary shell command execution
- `UploadFileServlet.java:41` — arbitrary file write + auto-runs `sync.sh`
- `DownloadFileServlet.java:46` — path traversal (no canonicalization)
- `Configuration.java:55` — prefix-only path check
- `web.xml` — public endpoints: `/execute`, `/uploadfile`, `/downloadfile`

**Attack flow:**
1. Extract hardcoded 3DES key from source/binary
2. Forge valid authentication tokens
3. Replay captured tokens indefinitely (timestamp check is broken)
4. Execute shell commands via `/execute`
5. Upload webshells via `/uploadfile`
6. Read arbitrary files via `/downloadfile` with `../` traversal

**Impact:** Full host compromise, data exfiltration, persistence.

---

## PoC #3: Cheat Commands in Chat

**Source files:**
- `Server.java:204` — `antiCheat = false` (default)
- `Server.java:208` — `cheat = "timeismoney@@!!"`
- `PlayerPacketHandler.java:13754` — secret check enables `player.cheat`
- `PlayerPacketHandler.java:13935` — `/money`, `/credit`
- `PlayerPacketHandler.java:14233` — `/shut` calls `System.exit(0)`

**Attack flow (when `testserver=true`):**
1. Type secret `timeismoney@@!!` in chat
2. `player.cheat` is set to `true`
3. Use chat commands: `/money`, `/credit`, `/exp`, `/item`, `/charge`, `/go`, `/load`, `/shut`

**Impact:** Economy destruction, server kill (`/shut`), RCE (`/load`).

---

## PoC #4: Movement Speedhack / Wallhack

**Source files:**
- `PlayerPacketHandler.java:16070` — receives raw x, y, time from client
- `Player.java:1215` — wall collision check is COMMENTED OUT
- `Server.java:204` — `antiCheat = false` (default)

**Attack flow:**
1. Send arbitrary x, y coordinates (server trusts client)
2. Walk through walls (collision check is commented out)
3. Send micro-steps to avoid speed/jump threshold
4. Violation points accumulate but never cause ban (`antiCheat=false`)

**Impact:** Wallhack, speedhack, teleport, access restricted areas.

---

## PoC #5: Anti-Bot Bypass

**Source files:**
- `Player.java:518` — `antiBotModel = LOG`, `antiPlugModel = LOG`
- `Player.java:7571` — `isBot()` only effective in `NONBENEFIT` mode
- `Player.java` — challenge only for old phone models
- `Player.java` — challenge uses client-reported `tickCount`, `moveDistance`
- CRC algorithm shared between client and server

**Attack flow:**
1. Set client model to modern phone (bypasses challenge entirely)
2. Even if challenged, report fake `tickCount`/`moveDistance`
3. Even if CRC fails, model is LOG-only (no action taken)
4. Even if detected, `isBot()` returns false (not NONBENEFIT mode)

**Impact:** Fully automated bot farming with zero risk of ban.

---

## Dependencies

PoC #2 requires `pycryptodome` for 3DES token forgery:

```bash
pip install pycryptodome
```

All other PoCs use only Python standard library.

## Disclaimer

These scripts are provided for educational and security auditing purposes only.
Use only against systems you own or have explicit permission to test.