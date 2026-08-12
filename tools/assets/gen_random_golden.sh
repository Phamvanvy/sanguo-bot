#!/bin/sh
# Regenerate tools/assets/golden/java-random.json.
#
# The blurred-map terrain is reconstructed from a single seed by replaying the game's PRNG, so
# the JS port has to match it exactly -- a one-step drift silently changes every tile. This
# script compiles the game's OWN com/pip/engine/Random.java (unmodified, it has no MIDP
# dependencies) inside the world container's JDK 8 and dumps reference values for the JS tests.
#
#   sh tools/assets/gen_random_golden.sh [container]
#
# Then: node web/client/src/assets/assets.test.mjs
set -e

CONTAINER="${1:-selfhost-world-1}"
REPO="$(cd "$(dirname "$0")/../.." && pwd)"
SRC="$REPO/Game/sangobuildVn/client/src/com/pip/engine/Random.java"
OUT="$REPO/tools/assets/golden/java-random.json"
GEN="$(dirname "$0")/RandomGolden.java"

[ -f "$SRC" ] || { echo "missing $SRC" >&2; exit 1; }
docker exec "$CONTAINER" true 2>/dev/null || { echo "container $CONTAINER is not running" >&2; exit 1; }

mkdir -p "$(dirname "$OUT")"
# Container-side paths stay inside sh -c quotes: Git Bash's MSYS layer rewrites bare /tmp/...
# arguments into Windows paths, which docker then cannot find.
docker exec "$CONTAINER" sh -c 'rm -rf /tmp/randomgolden && mkdir -p /tmp/randomgolden/com/pip/engine'
docker cp "$SRC" "$CONTAINER:/tmp/randomgolden/com/pip/engine/Random.java"
docker cp "$GEN" "$CONTAINER:/tmp/randomgolden/RandomGolden.java"
docker exec "$CONTAINER" sh -c \
  'cd /tmp/randomgolden && javac -encoding UTF-8 com/pip/engine/Random.java RandomGolden.java -d . && java -cp . RandomGolden' \
  > "$OUT"

# Validate with a repo-relative path: under Git Bash "$REPO" is an MSYS path (/e/repos/...)
# that node would resolve against the wrong drive root.
(cd "$REPO" && node -e "JSON.parse(require('fs').readFileSync('tools/assets/golden/java-random.json','utf8'))")
echo "wrote tools/assets/golden/java-random.json"
