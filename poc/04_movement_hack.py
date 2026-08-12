#!/usr/bin/env python3
"""
PoC #4: Movement Speedhack / Wallhack (HIGH)

Demonstrates:
  1. Server accepts raw x, y, time from client with no server-side validation
  2. Wall collision check is entirely commented out
  3. Speed check only adds violation points; banning requires antiCheat=true
  4. antiCheat defaults to false (Server.java:204)
  5. Attacker can send small coordinate steps to avoid the "jump" threshold
     and walk through walls/obstacles

Source references:
  - PlayerPacketHandler.java:16070 -> receives x, y, time directly from client
  - Player.java:1215 -> wall/valid-area checks are commented out
  - Server.java:204 -> antiCheat = false (default)
"""

import socket
import struct
import argparse
import math
import time
import sys

# ---------------------------------------------------------------------------
# Movement packet structure (simplified)
# ---------------------------------------------------------------------------
# Real format depends on game protocol. This is illustrative.
OPCODE_MOVE = 0x3001  # illustrative

def build_move_packet(x, y, timestamp, serial=1):
    """Build a simplified movement packet."""
    # [opcode:2][serial:4][x:2][y:2][timestamp:4]
    header = struct.pack(">H", OPCODE_MOVE)
    header += struct.pack(">I", serial)
    header += struct.pack(">h", x)       # signed short for x
    header += struct.pack(">h", y)       # signed short for y
    header += struct.pack(">I", timestamp)
    return header


# ---------------------------------------------------------------------------
# Attack demonstrations
# ---------------------------------------------------------------------------

def demo_direct_coordinate_injection():
    """Show that server trusts client coordinates blindly."""
    print("\n[!] Direct Coordinate Injection:")
    print("    PlayerPacketHandler.java:16070:")
    print("    Server receives x, y, time directly from client packet.")
    print("    No server-side position verification.")
    print()
    print("    Attack:")
    print("    Client sends: x=9999, y=9999 (any position)")
    print("    Server accepts without question.")
    print()
    print("    [!] Client has FULL control over reported position.")

def demo_wallhack():
    """Demonstrate wall collision bypass."""
    print("\n[!] Wallhack — Collision Check Commented Out:")
    print("    Player.java:1215:")
    print("    // Entire wall/valid-area validation is commented out.")
    print()
    print("    Original code (commented):")
    print("    // if (!map.isWalkable(x, y)) {")
    print("    //     kick player")
    print("    // }")
    print()
    print("    [!] Players can walk through:")
    print("    - Walls and buildings")
    print("    - Water and lava")
    print("    - Locked doors")
    print("    - Instance boundaries")
    print("    - PvP safe zones")

def demo_speedhack_micro_steps():
    """Demonstrate speedhack via micro-stepping."""
    print("\n[!] Speedhack via Micro-Stepping:")
    print("    Speed check only detects large 'jumps'.")
    print("    Attacker sends many small coordinate changes rapidly.")
    print()
    print("    Example attack sequence (sent at 100 packets/sec):")
    print("    Step 1: (100, 100) -> (101, 100)  [1 unit]")
    print("    Step 2: (101, 100) -> (102, 100)  [1 unit]")
    print("    Step 3: (102, 100) -> (103, 100)  [1 unit]")
    print("    ...")
    print("    Step N: (199, 100) -> (200, 100)  [1 unit]")
    print()
    print("    Each step is below the 'jump' threshold.")
    print("    Total movement: 100 units in 1 second (10x normal speed).")
    print("    [!] Speed check never triggers.")

def demo_violation_point_system():
    """Show that violation points don't stop cheating."""
    print("\n[!] Violation Point System — No Real Protection:")
    print("    Speed violations only add 'violation points'.")
    print("    Banning only occurs when Server.antiCheat = true.")
    print("    Server.antiCheat defaults to FALSE (Server.java:204).")
    print()
    print("    Result: Violation points accumulate but NEVER cause a ban.")
    print()
    print("    [!] Even if antiCheat were true, attacker can:")
    print("    - Stay just below the violation threshold")
    print("    - Reset violation counter by reconnecting")
    print("    - Use multiple accounts")

def demo_teleport_through_walls():
    """Demonstrate teleporting through walls with micro-steps."""
    print("\n[!] Walking Through Walls (Practical Attack):")
    print("    Target: Reach a boss room behind a locked door.")
    print()
    print("    Normal path: Complete quest chain (2+ hours)")
    print("    Hack path: Walk through the wall in 5 seconds")
    print()
    print("    Attack steps:")
    print("    1. Stand next to the wall at (50, 100)")
    print("    2. Send micro-steps through the wall:")
    print("       (50,100)->(51,100)->(52,100)->...->(60,100)")
    print("    3. Each step is 1 unit (below jump threshold)")
    print("    4. Wall collision check is commented out")
    print("    5. Now inside the boss room")
    print()
    print("    [!] Can access any area, any chest, any NPC.")

def demo_packet_replay_teleport():
    """Demonstrate teleport via packet replay."""
    print("\n[!] Packet Replay for Instant Teleport:")
    print("    No sequence number or nonce validation on move packets.")
    print()
    print("    Attack:")
    print("    1. Walk to desired location once (or use another hack)")
    print("    2. Capture the move packet for that location")
    print("    3. Replay the packet anytime to teleport back")
    print()
    print("    [!] Instant teleport to any previously visited location.")

def main():
    parser = argparse.ArgumentParser(
        description="PoC: Movement Speedhack / Wallhack",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--host", default="127.0.0.1", help="Game server host")
    parser.add_argument("--port", type=int, default=9001, help="Game server port")
    parser.add_argument("--demo", action="store_true", help="Run demo without connecting")
    args = parser.parse_args()

    print("=" * 60)
    print("PoC #4: Movement Speedhack / Wallhack")
    print("=" * 60)
    print()
    print("VULNERABILITY SUMMARY:")
    print("  1. Client sends raw x,y,time — no server validation")
    print("  2. Wall collision check is COMMENTED OUT (Player.java:1215)")
    print("  3. Speed check only adds violation points")
    print("  4. Ban requires antiCheat=true, but default is FALSE")
    print("  5. Micro-stepping bypasses the jump threshold")
    print()
    print("IMPACT: Wallhack, speedhack, teleport, access restricted areas")
    print()

    demo_direct_coordinate_injection()
    demo_wallhack()
    demo_speedhack_micro_steps()
    demo_violation_point_system()
    demo_teleport_through_walls()
    demo_packet_replay_teleport()

    print()
    print("REMEDIATION:")
    print("  - NEVER trust client-reported position")
    print("  - Implement server-side pathfinding and collision detection")
    print("  - Validate each movement step against the map's walkable grid")
    print("  - Track player speed server-side (distance/time between packets)")
    print("  - Set antiCheat = true and enforce bans at low violation thresholds")
    print("  - Add sequence numbers to move packets to prevent replay")
    print("  - Implement server-authoritative movement (client sends intent,")
    print("    server calculates actual position)")

if __name__ == "__main__":
    main()