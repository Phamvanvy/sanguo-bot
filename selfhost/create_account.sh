#!/usr/bin/env bash
#
# Create a game account on a freshly-seeded database.
#
# The 2014 stack has no self-service registration we host: accounts came from the
# operator's web/billing front end, which is not part of this self-host. But the
# account server accepts a plaintext password in tbl_account -- Account.getPasswordDec()
# treats anything not starting with "$e$" as plaintext and re-encrypts it on first
# login -- so seeding a row is enough.
#
# Credentials are arguments, never committed:
#   sh selfhost/create_account.sh --name myaccount --password mypassword
#   SANGUO_ACCOUNT=... SANGUO_PASSWORD=... sh selfhost/create_account.sh
#
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
NAME=${SANGUO_ACCOUNT:-}
PASSWORD=${SANGUO_PASSWORD:-}

while [ $# -gt 0 ]; do
	case "$1" in
		--name) NAME=$2; shift 2 ;;
		--password) PASSWORD=$2; shift 2 ;;
		-h|--help) sed -n '2,14p' "$0"; exit 0 ;;
		*) echo "unknown argument: $1" >&2; exit 2 ;;
	esac
done

[ -n "$NAME" ] && [ -n "$PASSWORD" ] || {
	echo "usage: $0 --name NAME --password PASSWORD  (or SANGUO_ACCOUNT/SANGUO_PASSWORD)" >&2
	exit 2
}
# these go into SQL below; keep them boring rather than escaping them
case "$NAME$PASSWORD" in
	*[\'\"\\\;]*) echo "ERROR: name/password must not contain quotes, backslashes or ;" >&2; exit 2 ;;
esac

# status 1 = enabled, gamecode 'sango' = what peony.xml <gamecode> sends.
docker compose -f "$SCRIPT_DIR/docker-compose.yml" exec -T mysql \
	mysql -uroot -proot --default-character-set=utf8 account <<SQL
INSERT INTO tbl_account (name, password, createtime, gamecode, status, phone)
VALUES ('$NAME', '$PASSWORD', NOW(), 'sango', 1, '');
SELECT id, name, 'created' AS state FROM tbl_account WHERE name = '$NAME';
SQL

echo "Log in with:  python selfhost/probe_login.py --name $NAME --password ****"
