# selfhost/ — running the 2014 server stack

Five containers: `mysql` 5.7, `accountserver`, `gameaccountserver`, `world`
(`peony.game.Server`), and `bridge` (the WebSocket bridge + browser client, see
`web/README.md`). The Java is the original 2014 build; only the config and three bug
fixes are ours.

## Prerequisite: the original workspace

Everything the servers actually execute — `bin/` output, jars, and the 257 MB data set —
comes from the `Game/` workspace, which is far too large for git (only its `src/` and
project files are committed). Restore it from the archive
(`8pKq80XlJPUpwj39明珠三国.zip`) before building, or point `GAME_ROOT` at your copy.

## Bring it up

```sh
sh selfhost/build_runtime.sh                       # stage selfhost/runtime/ (~25 s warm)
npm --prefix web/bridge ci                         # once; web/ is mounted read-only
docker compose -f selfhost/docker-compose.yml up -d
```

The world server takes **~90 s** to boot — it compiles every quest script to bytecode
first. Wait for `World Started`:

```sh
docker compose -f selfhost/docker-compose.yml logs -f world | grep -m1 'World Started'
```

Then create an account and log in:

```sh
sh selfhost/create_account.sh --name NAME --password PASSWORD
python selfhost/probe_login.py --name NAME --password PASSWORD    # raw TCP login
node web/bridge/test_ws_g2.js --name NAME --password PASSWORD     # login → world → move
```

`http://127.0.0.1:8090/game.html` is the playable browser client (see `web/README.md`).
The bridge serves it, relays WebSocket↔TCP, and hands out **only** the client-asset trees
out of `runtime/data` — the rest of that directory is server-side game logic. The data
mount is read-only at the container as well as in the code.

## selfhost/runtime/ is generated — never edit it

It is gitignored and `build_runtime.sh` is the only thing that writes it. Wiping it and
rebuilding is a supported operation and is how the script is tested: the rebuilt tree is
byte-for-byte identical to the one that has been running (verified by md5 over every file),
so anything you hand-edit there **will be silently lost**. Config changes belong in
`selfhost/overlay/`, code changes in `Game/**/src/`.

### What gets staged where

| runtime path | comes from |
|---|---|
| `accountserver/{bin,lib}` + root config | `Game/workspace/AccountServer/` |
| `gameaccountserver/{bin,lib}` + root config | `Game/workspace/GameAccountServer/` |
| …both `bin/` also get `com.pip.net.*`, `com.pip.db.*` | `Game/net/bin`, `Game/workspace/db/bin` — the sibling Eclipse projects both `.classpath`s reference; there is no jar for them |
| `world/libs` (49 jars) | `Game/sangobuildVn/dist` (6, incl. `peony.jar` = the server), `sangobuildVn/server/lib` (30), `sangobuildVn/editor/lib` (5), `Game/Sango1.0-Server/lib` (8: jetty/log4j/mina/slf4j/servlet-api/commons-primitives, which the Vn checkout never had its own copies of) |
| `world/app` | `Game/sangobuildVn/server/` (the working directory: `peony.xml` and the files it names) |
| `data/` | `Game/sangobuildVn/data/data_vi_VN/data` — the **Vietnamese** data set, which is what the Vn `peony.jar` expects. Not `data/data`: every `client.pkg` differs. |
| the six config files | `selfhost/overlay/` (below) |

### selfhost/overlay/ — our config, in git

Copies of the six files that had to change for the self-host, applied on top of the
originals. All they change is addressing:

- `world/app/peony.xml` — account server → `gameaccountserver:7101`, `<datadir>` → `/data`,
  jetty bind → `0.0.0.0`, CMCC `<slaveaccount>` removed.
- `world/conf/hibernate.cfg.xml`, `accountserver/bin/hibernate.cfg.xml`,
  `gameaccountserver/bin/hibernate.cfg.xml` — JDBC URL → `mysql:3306`.
- `accountserver/config.properties`, `gameaccountserver/config.properties` — bind
  `0.0.0.0`, `accountip` → `accountserver`.

### The patched classes

The 2014 `bin/` output was built by Eclipse from version-skewed sources, so some `.class`
files are stubs or disagree with the wire format. Three broke login. The **fixed sources
are in git**; `build_runtime.sh` recompiles just those files with JDK 8 (in a
`eclipse-temurin:8-jdk` container, so no host JDK is needed) against the original `bin/`.
It is deliberately not a full rebuild: most of the tree does not compile with a modern
javac, and the shipped `.class` files are the ones that have been tested.

| source (in git) | bug |
|---|---|
| `Game/workspace/AccountServer/src/…/stub/UWAPSocketStub.java` | wrote 1 `long` for `_LEGACY_LOGIN_OK`; the message model has 2 (`longBalance` + `bBalance`) |
| `Game/workspace/GameAccountServer/src/…/server/_LegacyLoginOkHandler.java` | was an Eclipse *"Unresolved compilation problem"* stub; also had to forward the 2nd long |
| `Game/net/src/com/pip/net/uwap2/mina/ByteListUtil.java` | `addString` NPE'd on null. `tbl_account.phone` is NULL on a fresh account and `UWAPSocketStub:1113` writes it into the login reply, so **every brand-new account** failed to log in until the row was hand-edited. Now nulls go on the wire as `""`. |

The build verifies each output class exists and is not an ecj stub.

## Runtime-generated state (not staged, do not commit)

- `world/app/sleepycat/` — Berkeley DB JE. The directory must exist or the server dies at
  boot (`Environment home ./sleepycat doesn't exist`); `build_runtime.sh` creates it empty.
- `data/PathFinder/*.pth` — one nav cache per map (179), written on first run.
- `data/cmccXML.xml` — rewritten by the server.
- MySQL lives in the `mysql-data` volume, so wiping `runtime/` keeps accounts and
  characters. `docker compose down -v` is what throws them away.

## Startup ordering

`gameaccountserver` connects to `accountserver:7100` exactly once at boot and its main
thread dies if that connect fails — leaving a process that accepts the world's connection
but can never answer a login (symptom: login times out, no error). `depends_on` alone does
not prevent this, so both servers now have TCP healthchecks and the dependants wait for
`service_healthy`.

## Known noise (harmless)

- `world`: `Quest 1732/1748 contains error` at boot — two quest scripts in the shipped data
  do not compile; the other ~1400 do.
- `world`: `Connection refused` every 10 min — `IpdService` reporting to the operator's
  long-dead `<ipd>` URL in `peony.xml`.
