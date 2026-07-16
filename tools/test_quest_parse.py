"""Quick test: parse quest panel from the screenshot taken at 7/20.

Usage:
    .venv/Scripts/python tools/test_quest_parse.py
"""
import sys
import logging

logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")

# Try paddleocr first, fallback to easyocr/llm
sys.path.insert(0, "e:/repos/sanguo-bot")

from src.vision.ocr import OCREngine
from src.game.quests import QuestParser
import cv2
import numpy as np

# Load the fresh quest panel screenshot
frame = cv2.imread("logs/quest_panel_fresh.png")
if frame is None:
    # Fallback to quest_panel2.png
    frame = cv2.imread("logs/quest_panel2.png")

if frame is None:
    print("No screenshot found")
    sys.exit(1)

print(f"Loaded frame: {frame.shape}")

# Initialize OCR engine
ocr = OCREngine()
print(f"Using OCR engine: {ocr._engine_name}")

# Initialize parser
parser = QuestParser(ocr)

# Parse the panel
quests = parser.parse_panel(frame)

print(f"\nParsed {len(quests)} quests:")
for q in quests:
    print(f"  [{q.index}] [{q.tag}] {q.title} -> {q.status.value}")
    if q.target_npc:
        print(f"      NPC: {q.target_npc}")
    if q.target_coord:
        print(f"      Coords: {q.target_coord}")