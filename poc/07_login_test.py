#!/usr/bin/env python3
"""PoC #7: Live Login Test - play.minhchauh5.com"""
import websocket, struct, time, ssl, argparse, threading

WS_URL = "wss://play.minhchauh5.com/ws"
ORIGIN = "https://play.minhchauh5.com"
OP_LOGIN_CLIENT = 166
OP_LOGIN_SERVER = 167

def enc_str(s):
    u8 = s.encode("utf-8"); u16 = s.encode("utf-16-be")
    if len(u8) < len(u16): return struct.pack(">H", len(u8)) + u8
    return struct.pack(">H", len(u16) | 0x8000) + u16

def build_login(user, pwd, serial=1):
    d = struct.pack(">i", serial)
    d += enc_str(user) + enc_str(pwd) + enc_str("iPhone") + enc_str("1.0.0")
    d += enc_str("") + struct.pack(">i", -1) + enc_str("")
    total = 8 + len(d)
    return b"UA" + struct.pack(">i", total) + struct.pack(">H", OP_LOGIN_CLIENT) + d

def parse_pkt(data):
    if len(data) < 8 or data[0:2] != b"UA": return None, b""
    tl = struct.unpack(">i", data[2:6])[0]
    op = struct.unpack(">H", data[6:8])[0]
    return op, data[8:tl]

def parse_str(p, o):
    if o + 2 > len(p): return None, o
    sl = struct.unpack(">H", p[o:o+2])[0]; o += 2
    if sl & 0x8000:
        sl &= 0x7FFF; s = p[o:o+sl].decode("utf-16-be", "replace")
    else: s = p[o:o+sl].decode("utf-8", "replace")
    return s, o + sl

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--username", default="vypv1")
    ap.add_argument("--password", default="123456")
    ap.add_argument("--timeout", type=int, default=15)
    a = ap.parse_args()
    print("=" * 50)
    print(f"Login test: {a.username} / {a.password}")
    print(f"Target: {WS_URL}")
    print("=" * 50)
    got = []

    def on_msg(ws, msg):
        if isinstance(msg, bytes):
            op, pl = parse_pkt(msg)
            if op is not None:
                print(f"  [RECV] opcode={op} len={len(msg)} payload={pl[:80].hex()}")
                if op == OP_LOGIN_SERVER:
                    serial = struct.unpack(">i", pl[0:4])[0]
                    acc_id = struct.unpack(">i", pl[4:8])[0]
                    name, _ = parse_str(pl, 8)
                    print(f"  [!!!] LOGIN SUCCESS! serial={serial} accId={acc_id} name={name}")
                else:
                    try:
                        serial = struct.unpack(">i", pl[0:4])[0]
                        msg_txt, _ = parse_str(pl, 4)
                        print(f"  [!!!] Response: serial={serial} msg={msg_txt}")
                    except: print(f"  [!!!] Raw: {pl.hex()}")
            else: print(f"  [RECV] {len(msg)}B: {msg[:64].hex()}")
        else: print(f"  [RECV] text: {str(msg)[:200]}")
        got.append(msg)

    def on_open(ws):
        print("  [OPEN] Connected!")
        pkt = build_login(a.username, a.password)
        print(f"  [SEND] Login packet ({len(pkt)}B): {pkt.hex()}")
        ws.send(pkt, opcode=websocket.ABNF.OPCODE_BINARY)

    ws = websocket.WebSocketApp(WS_URL, on_open=on_open, on_message=on_msg,
        on_error=lambda w,e: print(f"  [ERROR] {e}"),
        on_close=lambda w,c,m: print(f"  [CLOSE] {c} {m}"))
    t = threading.Thread(target=lambda: ws.run_forever(
        sslopt={"cert_reqs": ssl.CERT_NONE}, origin=ORIGIN))
    t.daemon = True; t.start()
    time.sleep(a.timeout); ws.close()
    print(f"\n  [INFO] Total messages: {len(got)}")
    print("=" * 50)

if __name__ == "__main__":
    main()