#!/usr/bin/env bash
#
# Rebuild selfhost/runtime/ from scratch.
#
# selfhost/runtime/ is a *staging* directory: everything in it is a copy of
# something else, so it is gitignored and this script is the only thing that
# may create it. Nothing in it is hand-edited any more.
#
#   inputs                                                        -> output
#   ---------------------------------------------------------------------------
#   Game/                    original 2014 workspace (not in git)     bin, lib,
#                                                                     jars, data
#   Game/**/src/*.java       the three legacy bug fixes (IN GIT)      patched
#                                                                     .class
#   selfhost/overlay/        self-host config overrides (IN GIT)      configs
#
# Usage:
#   sh selfhost/build_runtime.sh                 # full clean rebuild
#   sh selfhost/build_runtime.sh --only patches  # just recompile the patches
#   sh selfhost/build_runtime.sh --skip-data     # keep the 257 MB data/ as-is
#
# Env:
#   GAME_ROOT   original workspace       (default: <repo>/Game)
#   DEST        staging directory        (default: <repo>/selfhost/runtime)
#   JAVAC8      host JDK 8 javac to use  (default: run javac in a
#                                         eclipse-temurin:8-jdk container)
#
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
GAME_ROOT=${GAME_ROOT:-$REPO_ROOT/Game}
DEST=${DEST:-$SCRIPT_DIR/runtime}
OVERLAY=$SCRIPT_DIR/overlay

STAGES="accountserver gameaccountserver world data patches"
SKIP_DATA=0
FORCE=0

while [ $# -gt 0 ]; do
	case "$1" in
		--only) STAGES=$2; shift 2 ;;
		--skip-data) SKIP_DATA=1; shift ;;
		--dest) DEST=$2; shift 2 ;;
		--force) FORCE=1; shift ;;
		-h|--help) sed -n '2,30p' "$0"; exit 0 ;;
		*) echo "unknown argument: $1" >&2; exit 2 ;;
	esac
done

say() { printf '\n=== %s\n' "$*"; }
die() { printf 'ERROR: %s\n' "$*" >&2; exit 1; }
have_stage() { case " $STAGES " in *" $1 "*) return 0 ;; *) return 1 ;; esac; }

# Docker on Windows needs E:/foo, not /e/foo.
dockerpath() { if command -v cygpath >/dev/null 2>&1; then cygpath -m "$1"; else printf '%s' "$1"; fi; }

# copy_tree SRC DST  -- recursive copy, dropping the 2014 CVS metadata dirs
copy_tree() {
	[ -d "$1" ] || die "missing input directory: $1"
	mkdir -p "$2"
	(cd "$1" && tar -cf - --exclude=CVS --exclude=.cvsignore .) | (cd "$2" && tar -xf -)
}

# copy_files SRC DST f1 f2 ...
copy_files() {
	src=$1 dst=$2; shift 2
	mkdir -p "$dst"
	for f in "$@"; do
		[ -f "$src/$f" ] || die "missing input file: $src/$f"
		cp "$src/$f" "$dst/$f"
	done
}

# ---------------------------------------------------------------------------
# preflight
# ---------------------------------------------------------------------------
[ -d "$GAME_ROOT" ] || die "GAME_ROOT not found: $GAME_ROOT
The original workspace is far too large for git. Restore it from the archive
(8pKq80XlJPUpwj39明珠三国.zip) or point GAME_ROOT at your copy."

for d in \
	"$GAME_ROOT/workspace/AccountServer/bin" \
	"$GAME_ROOT/workspace/GameAccountServer/bin" \
	"$GAME_ROOT/workspace/db/bin" \
	"$GAME_ROOT/net/bin" \
	"$GAME_ROOT/sangobuildVn/dist" \
	"$GAME_ROOT/sangobuildVn/server/lib" \
	"$GAME_ROOT/sangobuildVn/editor/lib" \
	"$GAME_ROOT/Sango1.0-Server/lib" \
	"$GAME_ROOT/sangobuildVn/data/data_vi_VN/data"
do
	[ -d "$d" ] || die "missing input directory: $d"
done

if [ "$DEST" = "$SCRIPT_DIR/runtime" ] && [ "$FORCE" -eq 0 ]; then
	if command -v docker >/dev/null 2>&1 &&
	   [ -n "$(cd "$SCRIPT_DIR" && docker compose ps -q 2>/dev/null || true)" ]; then
		die "the stack is running and bind-mounts $DEST.
Run 'docker compose -f selfhost/docker-compose.yml down' first (or pass --force)."
	fi
fi

# ---------------------------------------------------------------------------
# accountserver / gameaccountserver
#
# Both are plain Eclipse projects: bin/ is the compiled output, lib/ the jars,
# and the loose files at the project root are the runtime config. Their
# .classpath also references the sibling /net and /db projects, whose bin/
# output has to be merged in (that is where com.pip.net.* and com.pip.db.*
# come from -- there is no jar for them).
# ---------------------------------------------------------------------------
if have_stage accountserver; then
	say "accountserver"
	rm -rf "$DEST/accountserver"
	copy_tree "$GAME_ROOT/workspace/AccountServer/bin" "$DEST/accountserver/bin"
	copy_tree "$GAME_ROOT/workspace/AccountServer/lib" "$DEST/accountserver/lib"
	copy_tree "$GAME_ROOT/net/bin"          "$DEST/accountserver/bin"
	copy_tree "$GAME_ROOT/workspace/db/bin" "$DEST/accountserver/bin"
	copy_files "$GAME_ROOT/workspace/AccountServer" "$DEST/accountserver" \
		5557_ZhangShangFeiXun.key.p8 AccountServer.iml SignVerProp.properties \
		account.sql appstore_config.xml backdoor.txt billing_config.properties \
		billing_log4j.properties cert_2d59.crt config.properties gameaccount.sql \
		invalidname.txt keywords.xml log4j.properties sources.txt yeepay_config.xml
	copy_tree "$OVERLAY/accountserver" "$DEST/accountserver"
	echo "  staged $(find "$DEST/accountserver" -type f | wc -l) files"
fi

if have_stage gameaccountserver; then
	say "gameaccountserver"
	rm -rf "$DEST/gameaccountserver"
	copy_tree "$GAME_ROOT/workspace/GameAccountServer/bin" "$DEST/gameaccountserver/bin"
	copy_tree "$GAME_ROOT/workspace/GameAccountServer/lib" "$DEST/gameaccountserver/lib"
	copy_tree "$GAME_ROOT/net/bin"          "$DEST/gameaccountserver/bin"
	copy_tree "$GAME_ROOT/workspace/db/bin" "$DEST/gameaccountserver/bin"
	copy_files "$GAME_ROOT/workspace/GameAccountServer" "$DEST/gameaccountserver" \
		clients.txt config.properties
	copy_tree "$OVERLAY/gameaccountserver" "$DEST/gameaccountserver"
	echo "  staged $(find "$DEST/gameaccountserver" -type f | wc -l) files"
fi

# ---------------------------------------------------------------------------
# world (peony.game.Server)
#
# Runs entirely out of jars: dist/ holds the game's own jars (peony.jar is the
# server), server/lib the third-party ones it was built against. jetty/log4j/
# mina/slf4j/servlet-api and commons-primitives are NOT in the Vn server/lib --
# the Vn build inherited them from the older Sango1.0-Server checkout, and the
# 5 editor jars are needed because peony.jar loads the map/script editor code.
# app/ is the working directory (peony.xml + the data files it names); the
# classpath is /app/conf:/app/libs/*, so hibernate.cfg.xml lives in conf/.
# ---------------------------------------------------------------------------
if have_stage world; then
	say "world"
	rm -rf "$DEST/world"
	mkdir -p "$DEST/world/libs"
	cp "$GAME_ROOT/sangobuildVn/dist"/*.jar        "$DEST/world/libs/"
	cp "$GAME_ROOT/sangobuildVn/server/lib"/*.jar  "$DEST/world/libs/"
	cp "$GAME_ROOT/sangobuildVn/editor/lib"/*.jar  "$DEST/world/libs/"
	copy_files "$GAME_ROOT/Sango1.0-Server/lib" "$DEST/world/libs" \
		commons-primitives-1.0.jar jetty-6.1.19.jar jetty-util-6.1.19.jar \
		log4j-1.2.11.jar mina-core-1.1.7.jar servlet-api-2.5-6.1.1.jar \
		slf4j-api-1.4.3.jar slf4j-log4j12-1.4.3.jar
	copy_files "$GAME_ROOT/sangobuildVn/server" "$DEST/world/app" \
		invalidname.txt keywords.xml log4j1.properties model.xml \
		nationbuff.properties promptbubble.properties trustip.txt version.xml
	copy_tree "$OVERLAY/world" "$DEST/world"
	# Berkeley DB JE refuses to create its own environment home: without this the
	# server dies at boot with "Environment home ./sleepycat doesn't exist".
	mkdir -p "$DEST/world/app/sleepycat"
	echo "  staged $(ls "$DEST/world/libs" | wc -l) jars"
fi

# ---------------------------------------------------------------------------
# data  (peony.xml <datadir> -> /data, bind-mounted read-write)
#
# The Vietnamese data set, which is the one the Vn peony.jar expects. The
# server generates PathFinder/*.pth (one per map) into it on first run.
# ---------------------------------------------------------------------------
if have_stage data && [ "$SKIP_DATA" -eq 0 ]; then
	say "data (257 MB, ~15k files -- this is the slow one)"
	rm -rf "$DEST/data"
	copy_tree "$GAME_ROOT/sangobuildVn/data/data_vi_VN/data" "$DEST/data"
	echo "  staged $(find "$DEST/data" -type f | wc -l) files"
fi

# ---------------------------------------------------------------------------
# patches
#
# The 2014 bin/ output was built by Eclipse from version-skewed sources, so a
# few .class files are stubs or disagree with the wire format. Three of them
# broke login; the fixed sources are in git, and are recompiled here against
# the original bin/ (NOT a full rebuild -- most of the tree does not compile
# with a modern javac, and the shipped .class files are what has been tested).
#
#   UWAPSocketStub          accountserver wrote 1 long for _LEGACY_LOGIN_OK,
#                           the message model has 2 (longBalance + bBalance)
#   _LegacyLoginOkHandler   was an Eclipse "Unresolved compilation problem"
#                           stub; also had to forward the 2nd long
#   ByteListUtil            NPE on a null string: tbl_account.phone is NULL
#                           until something sets it, and UWAPSocketStub:1113
#                           writes it into the login reply. Every brand-new
#                           account hit this. Shared by both servers.
# ---------------------------------------------------------------------------
javac8() {
	# javac8 <app-dir> <src-root> <file.java relative to src-root>
	app=$1 src=$2 rel=$3
	if [ -n "${JAVAC8:-}" ]; then
		"$JAVAC8" -encoding UTF-8 -cp "$app/bin:$app/lib/*" -d "$app/bin" "$src/$rel"
		return
	fi
	command -v docker >/dev/null 2>&1 || die "need docker (or set JAVAC8 to a JDK 8 javac)"
	user_arg=""
	case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) ;; *) user_arg="--user $(id -u):$(id -g)" ;; esac
	MSYS_NO_PATHCONV=1 MSYS2_ARG_CONV_EXCL='*' docker run --rm $user_arg \
		-v "$(dockerpath "$app")":/app \
		-v "$(dockerpath "$src")":/src:ro \
		eclipse-temurin:8-jdk \
		javac -encoding UTF-8 -cp '/app/bin:/app/lib/*' -d /app/bin "/src/$rel"
}

if have_stage patches; then
	say "patches (recompiling the fixed .class with JDK 8)"
	javac8 "$DEST/accountserver" \
		"$GAME_ROOT/workspace/AccountServer/src" \
		com/pip/server/account/stub/UWAPSocketStub.java
	javac8 "$DEST/gameaccountserver" \
		"$GAME_ROOT/workspace/GameAccountServer/src" \
		com/pip/gameaccount/handler/server/_LegacyLoginOkHandler.java
	for app in accountserver gameaccountserver; do
		javac8 "$DEST/$app" "$GAME_ROOT/net/src" com/pip/net/uwap2/mina/ByteListUtil.java
	done

	for c in \
		"$DEST/accountserver/bin/com/pip/server/account/stub/UWAPSocketStub.class" \
		"$DEST/accountserver/bin/com/pip/server/account/stub/UWAPSocketStub\$ClientSessionHandler.class" \
		"$DEST/gameaccountserver/bin/com/pip/gameaccount/handler/server/_LegacyLoginOkHandler.class" \
		"$DEST/accountserver/bin/com/pip/net/uwap2/mina/ByteListUtil.class" \
		"$DEST/gameaccountserver/bin/com/pip/net/uwap2/mina/ByteListUtil.class"
	do
		[ -s "$c" ] || die "patch did not produce $c"
		# an ecj stub compiles fine but throws at runtime -- catch it here instead
		if grep -qa 'Unresolved compilation' "$c"; then
			die "$c is still an 'Unresolved compilation problem' stub"
		fi
		echo "  ok $(basename "$c")"
	done
fi

say "done -- $DEST"
echo "next: docker compose -f selfhost/docker-compose.yml up -d"
