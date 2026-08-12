#!/usr/bin/env python3
"""
PoC #5: Anti-Bot Bypass (HIGH)

Demonstrates:
  1. antiBotModel and antiPlugModel both default to LOG mode (Player.java:518)
     -> Bots are detected but only logged, never blocked
  2. Player.isBot() only affects gameplay in NONBENEFIT mode (Player.java:7571)
  3. Anti-bot challenge only applies to old phone models
  4. Challenge uses client-reported tickCount, moveDistance — trivially faked
  5. Challenge uses CRC algorithm present in both client and server
     -> Modified client can compute correct responses

Source references:
  - Player.java:518 -> antiBotModel = LOG, antiPlugModel = LOG
  - Player.java:7571 -> isBot() only matters for NONBENEFIT
  - Player.java antiBotModels[] -> old phone model list
  - Player.java -> tickCount, moveDistance from client
  - CRC algorithm shared between client and server
"""

import struct
import hashlib
import argparse
import sys

# ---------------------------------------------------------------------------
# Anti-bot model constants (from Player.java)
# ---------------------------------------------------------------------------
ANTIPLUG_MODEL_LOG = 0         # Only log, no action
ANTIPLUG_MODEL_NONBENEFIT = 1  # No benefits (only mode that blocks)
ANTIPLUG_MODEL_KICK = 2        # Kick player (NOT USED in current code)

# Default models — both set to LOG
DEFAULT_ANTI_BOT_MODEL = ANTIPLUG_MODEL_LOG
DEFAULT_ANTI_PLUG_MODEL = ANTIPLUG_MODEL_LOG

# Old phone models that trigger anti-bot challenge
OLD_PHONE_MODELS = [
    "GenericMidp2", "Nokia7610", "Nokia6681", "Nokia3250",
    "NokiaN73", "Nokia7370", "NokiaE62", "SEK750", "SEK790",
    "MotoE2", "Midp2Touch", "Lenovo", "NokiaS60V3", "NokiaS60V2",
]


# ---------------------------------------------------------------------------
# CRC simulation (simplified — real CRC matches client/server shared algo)
# ---------------------------------------------------------------------------

def simulate_crc_response(tick_count, move_distance, secret=0xDEADBEEF):
    """
    Simulate the CRC-based anti-bot challenge response.
    Since the CRC algorithm is in both client and server code,
    a modified client can compute the correct response trivially.
    """
    # Simplified CRC — real algorithm is shared between client/server
    data = struct.pack(">II", tick_count, move_distance)
    crc = hashlib.md5(data).digest()
    # XOR with a shared secret (extractable from client binary)
    result = int.from_bytes(crc[:4], "big") ^ secret
    return result


# ---------------------------------------------------------------------------
# Attack demonstrations
# ---------------------------------------------------------------------------

def demo_log_only_mode():
    """Show that anti-bot only logs, never blocks."""
    print("\n[!] Anti-Bot is LOG-ONLY by Default:")
    print("    Player.java:518:")
    print("    antiBotModel = ANTIPLUG_MODEL_LOG (0)")
    print("    antiPlugModel = ANTIPLUG_MODEL_LOG (0)")
    print()
    print("    Result: Bots are detected but NEVER blocked.")
    print("    They can farm 24/7 without any consequence.")
    print()
    print("    The only blocking mode (NONBENEFIT = 1) is NOT the default.")
    print("    Even NONBENEFIT only removes benefits — doesn't kick/ban.")

def demo_isbot_ineffective():
    """Show that isBot() has almost no effect."""
    print("\n[!] Player.isBot() Has Minimal Impact:")
    print("    Player.java:7571:")
    print("    if (antiPlug != null && antiPlug.isBot")
    print("        && antiPlugModel == ANTIPLUG_MODEL_NONBENEFIT)")
    print("        return true;")
    print()
    print("    isBot() only returns true when:")
    print("    1. antiPlug.isBot is true (bot detected)")
    print("    2. antiPlugModel == NONBENEFIT (not default)")
    print()
    print("    Since antiPlugModel defaults to LOG (0), isBot()")
    print("    effectively NEVER returns true in default config.")
    print()
    print("    [!] Even detected bots play with full benefits.")

def demo_old_phone_bypass():
    """Show that modern phones bypass anti-bot entirely."""
    print("\n[!] Anti-Bot Only Targets Old Phones:")
    print("    Challenge only triggers for these models:")
    for model in OLD_PHONE_MODELS:
        print(f"    - {model}")
    print()
    print("    [!] Any modern phone model (iPhone, Android, etc.)")
    print("    is NEVER challenged. Bots can simply report a modern")
    print("    User-Agent/model string to bypass completely.")
    print()
    print("    Attack: Set client model to 'iPhone' or 'Pixel'")
    print("    -> Anti-bot challenge is skipped entirely.")

def demo_client_reported_metrics():
    """Show that anti-bot trusts client-reported metrics."""
    print("\n[!] Anti-Bot Trusts Client-Reported Metrics:")
    print("    Challenge uses these client-provided values:")
    print("    - tickCount: number of game ticks (client reports)")
    print("    - moveDistance: distance moved (client reports)")
    print()
    print("    [!] A bot can report ANY values it wants.")
    print("    Example: Always report tickCount=100, moveDistance=50")
    print("    -> Perfectly consistent, never flagged.")
    print()
    print("    The server has no independent way to verify these metrics.")

def demo_crc_bypass():
    """Show that CRC challenge is trivially bypassed."""
    print("\n[!] CRC Challenge is Trivially Bypassed:")
    print("    The CRC algorithm exists in BOTH client and server code.")
    print("    A modified client can compute the correct response.")
    print()
    print("    Demonstration (simulated):")
    tick = 1000
    dist = 500
    response = simulate_crc_response(tick, dist)
    print(f"    tickCount={tick}, moveDistance={dist}")
    print(f"    Computed CRC response: 0x{response:08X}")
    print()
    print("    [!] Any bot author can extract the CRC algorithm from")
    print("    the client binary and compute valid responses.")
    print()
    print("    The 'challenge' provides zero security against")
    print("    a moderately skilled attacker.")

def demo_full_bypass_flow():
    """Show the complete bypass flow."""
    print("\n[!] Complete Bot Bypass Flow:")
    print()
    print("    Step 1: Set client model to 'iPhone' (modern phone)")
    print("    Step 2: Anti-bot challenge is SKIPPED (not in old model list)")
    print("    Step 3: Even if challenged, report fake tickCount/moveDistance")
    print("    Step 4: Even if CRC fails, antiBotModel=LOG -> no action")
    print("    Step 5: Even if detected, isBot() returns false (not NONBENEFIT)")
    print()
    print("    RESULT: Bot farms indefinitely with zero risk of ban.")
    print()
    print("    [!] Multiple layers of defense, ALL ineffective.")

def main():
    parser = argparse.ArgumentParser(
        description="PoC: Anti-Bot Bypass",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--demo", action="store_true", help="Run demo")
    args = parser.parse_args()

    print("=" * 60)
    print("PoC #5: Anti-Bot Bypass")
    print("=" * 60)
    print()
    print("VULNERABILITY SUMMARY:")
    print("  1. antiBotModel defaults to LOG — bots only logged, never blocked")
    print("  2. antiPlugModel defaults to LOG — same issue")
    print("  3. isBot() only effective in NONBENEFIT mode (not default)")
    print("  4. Challenge only targets old phone models (trivially bypassed)")
    print("  5. Challenge uses client-reported metrics (trivially faked)")
    print("  6. CRC algorithm is in client code (trivially computed)")
    print()
    print("IMPACT: Fully automated bot farming with zero risk")
    print()

    demo_log_only_mode()
    demo_isbot_ineffective()
    demo_old_phone_bypass()
    demo_client_reported_metrics()
    demo_crc_bypass()
    demo_full_bypass_flow()

    print()
    print("REMEDIATION:")
    print("  - Change antiBotModel default to KICK or NONBENEFIT")
    print("  - Implement server-side behavioral analysis (not client metrics)")
    print("  - Track metrics server-side (tick count, move distance)")
    print("  - Use server-generated nonce in challenges (not shared CRC)")
    print("  - Apply anti-bot to ALL device models, not just old ones")
    print("  - Implement progressive penalties: warn -> mute -> kick -> ban")
    print("  - Add CAPTCHA or proof-of-work for suspected bots")
    print("  - Monitor for inhuman patterns (24/7 uptime, perfect repetition)")

if __name__ == "__main__":
    main()