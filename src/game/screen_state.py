"""Screen state detection for the game.

Identifies which panel/screen the game is currently showing
to guide bot decision-making.
"""
from __future__ import annotations

from enum import Enum
from typing import Optional
import cv2
import numpy as np


class ScreenState(Enum):
    HOME = "home"
    LOGIN = "login"
    CHARACTER_SELECT = "character_select"
    GAME_WORLD = "game_world"
    QUEST_PANEL = "quest_panel"
    COMBAT = "combat"
    DIALOG = "dialog"
    UNKNOWN = "unknown"


class ScreenDetector:
    """Detect current game screen state using template matching and OCR."""

    def __init__(self):
        # Template signatures for each state (to be calibrated)
        self.templates = {
            ScreenState.LOGIN: None,  # "Đăng nhập" text
            ScreenState.QUEST_PANEL: None,  # "Nhiệm vụ quản lý" header
            ScreenState.COMBAT: None,  # Combat indicators
        }

    def detect(self, frame) -> ScreenState:
        """Detect current screen state from game frame.
        Uses heuristics and template matching to identify state.
        Accepts either a numpy array or a Frame object with .image attribute."""
        # Unwrap Frame objects to numpy arrays
        if hasattr(frame, 'image'):
            frame = frame.image
        h, w = frame.shape[:2]

        # Check for login screen (top-left "Đăng nhập" text area)
        if self._is_login_screen(frame):
            return ScreenState.LOGIN

        # Check for quest panel (header text + quest list pattern)
        if self._is_quest_panel(frame):
            return ScreenState.QUEST_PANEL

        # Check for character select
        if self._is_character_select(frame):
            return ScreenState.CHARACTER_SELECT

        # Check for combat (red HP bar visible, action indicators)
        if self._is_in_combat(frame):
            return ScreenState.COMBAT

        # Default to game world if in game
        if self._is_in_game(frame):
            return ScreenState.GAME_WORLD

        return ScreenState.UNKNOWN

    def _is_login_screen(self, frame: np.ndarray) -> bool:
        """Check if login form is visible."""
        h, w = frame.shape[:2]
        # Login form typically appears in center-top area
        login_area = frame[int(0.1 * h):int(0.3 * h), int(0.3 * w):int(0.7 * w)]
        # Look for form-like structure (light background with input fields)
        gray = cv2.cvtColor(login_area, cv2.COLOR_BGR2GRAY)
        mean_val = gray.mean()
        # Login form has distinct brightness pattern
        return mean_val > 150 and gray.std() < 50

    def _is_quest_panel(self, frame: np.ndarray) -> bool:
        """Check if quest management panel is open."""
        h, w = frame.shape[:2]
        # Quest panel header area (top of panel)
        header_area = frame[int(0.05 * h):int(0.15 * h), int(0.2 * w):int(0.8 * w)]
        # Look for dark header with light text (typical panel header)
        gray = cv2.cvtColor(header_area, cv2.COLOR_BGR2GRAY)
        # Header has high contrast (dark bg, light text)
        return gray.std() > 30 and gray.mean() < 120

    def _is_character_select(self, frame: np.ndarray) -> bool:
        """Check if character selection screen is visible."""
        h, w = frame.shape[:2]
        # Character avatars typically in center row
        char_row = frame[int(0.3 * h):int(0.6 * h), int(0.1 * w):int(0.9 * w)]
        # Look for circular/rounded character portrait shapes
        gray = cv2.cvtColor(char_row, cv2.COLOR_BGR2GRAY)
        # Character portraits have distinct edge patterns
        return gray.std() > 40

    def _is_in_combat(self, frame: np.ndarray) -> bool:
        """Check if player is in combat (HP bar active, action indicators)."""
        h, w = frame.shape[:2]
        # HP bar typically at bottom-center
        hp_area = frame[int(0.85 * h):int(0.95 * h), int(0.3 * w):int(0.7 * w)]
        # Look for red/orange HP bar colors
        hsv = cv2.cvtColor(hp_area, cv2.COLOR_BGR2HSV)
        # Red-orange range
        lower_red = np.array([0, 100, 100])
        upper_red = np.array([15, 255, 255])
        mask = cv2.inRange(hsv, lower_red, upper_red)
        red_ratio = cv2.countNonZero(mask) / mask.size
        return red_ratio > 0.1

    def _is_in_game(self, frame: np.ndarray) -> bool:
        """Check if player is in the game world."""
        h, w = frame.shape[:2]
        # Game world has minimap in top-right, various UI elements
        # Heuristic: check for minimap area (dark circle in top-right)
        minimap_area = frame[int(0.05 * h):int(0.3 * h), int(0.8 * w):int(0.95 * w)]
        # Minimap is typically darker with distinct edges
        gray = cv2.cvtColor(minimap_area, cv2.COLOR_BGR2GRAY)
        return gray.mean() < 100 and gray.std() > 20