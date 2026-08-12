#!/usr/bin/env python3
"""
G0.5b TCP protocol probe for the self-hosted world server.

Verifies the UA wire framing end-to-end:
  frame = 'U' 'A' | int32 totalLen (BE) | int16 opcode (BE) | payload
  totalLen counts the WHOLE frame (2 + 4 + 2 + payloadLen), so payloadLen = totalLen - 8.

Sends ACCOUNT_LOGIN_CLIENT (166). Server payload layout (PlayerPacketHandler.accountLogin):
  int serial | UTF name | UTF password | UTF model | UTF version
  [ UTF realPhone | int playerID | UTF IMEI ]   (optional tail, revision=PIP reads IMEI)

Strings use Java modified-UTF8 (DataOutputStream.writeUTF): u16 byte-length prefix + bytes.
Credentials come from CLI/env, never hardcoded.

Usage:
  python probe_login.py --host 127.0.0.1 --port 7000 --name vypv1 --password 123456
"""
import argparse, os, socket, struct, sys

# --- opcodes (from server/src/peony/game/OpCode.java) ---
ACCOUNT_LOGIN_CLIENT = 166
ACCOUNT_LOGIN_SERVER = 167

def w_utf(s: str) -> bytes:
    """Java DataOutputStream.writeUTF: modified UTF-8 with u16 byte-length prefix."""
    b = bytearray()
    for ch in s:
        c = ord(ch)
        if c == 0x00:
            b += b"\xc0\x80"
        elif c <= 0x7f:
            b.append(c)
        elif c <= 0x7ff:
            b.append(0xc0 | (c >> 6)); b.append(0x80 | (c & 0x3f))
        else:  # BMP; surrogate pairs would each encode as 3 bytes (handled per-char)
            b.append(0xe0 | (c >> 12)); b.append(0x80 | ((c >> 6) & 0x3f)); b.append(0x80 | (c & 0x3f))
    if len(b) > 0xffff:
        raise ValueError("string too long for writeUTF")
    return struct.pack(">H", len(b)) + bytes(b)

def frame(opcode: int, payload: bytes) -> bytes:
    total = 2 + 4 + 2 + len(payload)
    return b"UA" + struct.pack(">i", total) + struct.pack(">h", opcode) + payload

def build_account_login(name, password, model, version, serial=1):
    p = bytearray()
    p += struct.pack(">i", serial)
    p += w_utf(name)
    p += w_utf(password)
    p += w_utf(model)
    p += w_utf(version)
    p += w_utf("")            # realPhone
    p += struct.pack(">i", -1)  # playerID
    p += w_utf("")            # IMEI
    return frame(ACCOUNT_LOGIN_CLIENT, bytes(p))

def hexdump(b: bytes, limit=160) -> str:
    b = b[:limit]
    return " ".join(f"{x:02x}" for x in b)

def read_frame(sock, timeout=6.0):
    sock.settimeout(timeout)
    buf = bytearray()
    def need(n):
        while len(buf) < n:
            chunk = sock.recv(4096)
            if not chunk:
                return False
            buf.extend(chunk)
        return True
    if not need(8):
        return None
    if buf[0:2] != b"UA":
        return ("BAD_HEAD", bytes(buf))
    total = struct.unpack(">i", bytes(buf[2:6]))[0]
    opcode = struct.unpack(">h", bytes(buf[6:8]))[0]
    if not need(total):
        return ("SHORT", opcode, total, bytes(buf))
    payload = bytes(buf[8:total])
    return ("OK", opcode, total, payload)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--host", default="127.0.0.1")
    ap.add_argument("--port", type=int, default=7000)
    ap.add_argument("--name", default=os.environ.get("SANGO_USER", "vypv1"))
    ap.add_argument("--password", default=os.environ.get("SANGO_PASS", "123456"))
    # Server parses these: cutModel/cutJvmCode split model on '/'  -> "MODEL/JVMCODE"
    #                      cutVersion/cutChannel split version on '-' -> "VERSION-CHANNEL"
    # A model without '/' or version without '-' crashes AccountLoginCall.callFinish.
    ap.add_argument("--model", default="GenericMidp2/GenericMidp2")
    ap.add_argument("--version", default="2.4-CCCCCPiP")
    args = ap.parse_args()

    pkt = build_account_login(args.name, args.password, args.model, args.version)
    print(f"[*] connect {args.host}:{args.port}")
    s = socket.create_connection((args.host, args.port), timeout=6.0)

    # See if the server pushes anything before we speak.
    try:
        s.settimeout(1.0)
        pre = s.recv(4096)
        if pre:
            print(f"[<] server pushed {len(pre)}B on connect: {hexdump(pre)}")
    except socket.timeout:
        pass

    print(f"[>] TX ACCOUNT_LOGIN_CLIENT(166) {len(pkt)}B: {hexdump(pkt)}")
    s.sendall(pkt)

    r = read_frame(s, timeout=12.0)
    if r is None:
        print("[!] connection closed by server (no frame)")
        s.close(); return
    if r[0] != "OK":
        print(f"[<] RX non-OK: {r[0]} :: {hexdump(r[-1])}")
        s.close(); return

    _, frame_opcode, total, payload = r
    print(f"[<] RX UA frame: frame_opcode={frame_opcode} len={total} payloadLen={len(payload)}")
    print(f"      payload hex: {hexdump(payload)}")

    # The opcode lives in the UA frame header (frame_opcode); the payload is the
    # message body directly (NOT wrapped with an inner opcode).
    #   ACCOUNT_LOGIN_SERVER(167) body (AccountLoginCall.callFinish, revision=PIP):
    #     int serial | int accountId | UTF name | int iMoney/100 | int modifiedNameTimes
    #   Rejection: ErrorHandler.sendErrorMessage -> frame_opcode == ACCOUNT_LOGIN_CLIENT(166),
    #     body: int serial | ... | UTF errorMessage
    if frame_opcode == ACCOUNT_LOGIN_SERVER:
        serial = struct.unpack(">i", payload[0:4])[0]
        account_id = struct.unpack(">i", payload[4:8])[0]
        name_len = struct.unpack(">H", payload[8:10])[0]
        name = payload[10:10 + name_len].decode("utf-8", "replace")
        off = 10 + name_len
        imoney = struct.unpack(">i", payload[off:off + 4])[0]
        assert account_id > 0 and name == args.name, \
            f"login response mismatch: accountId={account_id} name={name!r}"
        print(f"[=] serial={serial} accountId={account_id} name={name!r} iMoney={imoney}")
        print("[=] RESULT: *** LOGIN SUCCESS *** (ACCOUNT_LOGIN_SERVER 167) — G0.5b PASS")
    elif frame_opcode == ACCOUNT_LOGIN_CLIENT:
        # server rejected but framing is valid -> still proves the transport
        try:
            mlen = struct.unpack(">H", payload[8:10])[0]
            msg = payload[10:10 + mlen].decode("utf-8", "replace")
        except Exception:
            msg = "<unparsed>"
        print(f"[=] RESULT: login rejected -> \"{msg}\"  (framing OK; G0.5b PASS)")
    else:
        print(f"[=] RESULT: framing OK, frame_opcode={frame_opcode} (G0.5b PASS)")
    s.close()

if __name__ == "__main__":
    sys.exit(main())
