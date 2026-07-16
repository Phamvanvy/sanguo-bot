"""Template matching module for the Sanguo auto-quest bot.

Provides template matching utilities for UI elements in the game,
including quest panel elements, buttons, status indicators, and other
interactive UI components. Uses OpenCV's matchTemplate with multiple
matching methods and a threshold-based approach.
"""
from __future__ import annotations

import pathlib
from dataclasses import dataclass
from typing import Optional

import cv2
import numpy as np


@dataclass
class TemplateMatch:
    """Result of a template match operation."""
    confidence: float
    x: int  # center x of matched region
    y: int  # center y of matched region
    w: int  # width of matched region
    h: int  # height of matched region
    top_left: tuple[int, int] = (0, 0)  # top-left corner of matched region

    @property
    def fraction(self) -> tuple[float, float]:
        """Return center as fraction of (w, h) — always (0.5, 0.5)."""
        return self.x / self.w if self.w > 0 else 0.5, self.y / self.h if self.h > 0 else 0.5

    def __repr__(self) -> str:
        return f"TemplateMatch(conf={self.confidence:.3f}, center=({self.x}, {self.y}))"


class TemplateMatcher:
    """Manages template matching against game screenshots.
    
    Templates are loaded from the templates/ directory and can be matched
    against game frames using various OpenCV matching methods.
    """

    def __init__(self, template_dir: pathlib.Path):
        self.template_dir = template_dir
        self._templates: dict[str, np.ndarray] = {}

    def load_template(self, name: str) -> np.ndarray:
        """Load a template image by name. Returns the BGR template array."""
        if name not in self._templates:
            path = self.template_dir / f"{name}.png"
            if not path.exists():
                raise FileNotFoundError(f"Template not found: {path}")
            img = cv2.imread(str(path))
            if img is None:
                raise ValueError(f"Failed to read template: {path}")
            self._templates[name] = img
        return self._templates[name]

    def match(
        self,
        template_name: str,
        frame: np.ndarray,
        threshold: float = 0.85,
        method: str = "cv2.TM_CCOEFF_NORMED",
        max_matches: int = 1,
    ) -> list[TemplateMatch]:
        """Match a template against a frame, returning matches above threshold.
        
        Args:
            template_name: Name of template to load and match
            frame: The frame to search in (BGR)
            threshold: Minimum confidence to consider a match (0-1)
            method: OpenCV matching method name (default: TM_CCOEFF_NORMED)
            max_matches: Maximum number of matches to return
            
        Returns:
            List of TemplateMatch objects sorted by confidence descending
        """
        template = self.load_template(template_name)
        method = getattr(cv2, method)

        result = cv2.matchTemplate(frame, template, method)
        h_t, w_t = template.shape[:2]

        # Get all matches above threshold
        matches = np.where(result >= threshold)
        if len(matches[0]) == 0:
            return []

        # Convert to (row, col) pairs
        match_points = list(zip(matches[0], matches[1]))

        # Non-maximum suppression: group overlapping matches and pick best
        matched = set()
        final_matches = []
        for i, (ry, rx) in enumerate(match_points):
            if i in matched:
                continue
            # Find all matches within template size
            best_idx = i
            best_conf = result[ry, rx]
            for j in range(i + 1, len(match_points)):
                if j in matched:
                    continue
                rj, cxj = match_points[j]
                if abs(rj - ry) < h_t and abs(cxj - rx) < w_t:
                    conf_j = result[rj, cxj]
                    if conf_j > best_conf:
                        best_idx = j
                        best_conf = conf_j
                        # Overlap the previous best
                        if best_idx == i:
                            matched.add(i)

            matched.add(best_idx)
            ry_best, rx_best = match_points[best_idx]
            conf = result[ry_best, rx_best]
            final_matches.append(TemplateMatch(
                confidence=conf,
                x=rx_best + w_t // 2,
                y=ry_best + h_t // 2,
                w=w_t,
                h=h_t,
                top_left=(rx_best, ry_best),
            ))

        # Sort by confidence descending, limit to max_matches
        final_matches.sort(key=lambda m: m.confidence, reverse=True)
        return final_matches[:max_matches]

    def match_color(
        self,
        frame: np.ndarray,
        lower: np.ndarray,
        upper: np.ndarray,
        min_ratio: float = 0.01,
        max_ratio: float = 0.15,
    ) -> Optional[TemplateMatch]:
        """Match a color range in a frame, returning the centroid of the mask.
        
        Args:
            frame: Input BGR frame
            lower: Lower bound (BGR)
            upper: Upper bound (BGR)
            min_ratio: Minimum area ratio to be considered a valid match
            max_ratio: Maximum area ratio to avoid large-region false positives
            
        Returns:
            TemplateMatch if found, None otherwise
        """
        mask = cv2.inRange(frame, lower, upper)
        total_pixels = frame.shape[0] * frame.shape[1]
        mask_pixels = cv2.countNonZero(mask)
        ratio = mask_pixels / total_pixels

        if ratio < min_ratio or ratio > max_ratio:
            return None

        # Find centroids of connected components
        num_labels, labels, stats, centroids = cv2.connectedComponentsWithStats(mask, connectivity=8)
        if num_labels <= 1:
            return None

        # Find the largest component (skip background label 0)
        max_label = 1
        max_area = 0
        for label in range(1, num_labels):
            area = stats[label, cv2.CC_STAT_AREA]
            if area > max_area:
                max_area = area
                max_label = label

        cx, cy = int(centroids[max_label][0]), int(centroids[max_label][1])
        return TemplateMatch(
            confidence=min(ratio * 2, 1.0),  # Heuristic confidence
            x=cx,
            y=cy,
            w=int(stats[max_label, cv2.CC_STAT_WIDTH]),
            h=int(stats[max_label, cv2.CC_STAT_HEIGHT]),
            top_left=(int(stats[max_label, cv2.CC_STAT_LEFT]), int(stats[max_label, cv2.CC_STAT_TOP])),
        )

    def find_quest_row_status(
        self,
        frame: np.ndarray,
        row_index: int,
        row_top_frac: float = 0.35,
        row_height_frac: float = 0.05,
    ) -> str:
        """Determine if a quest row is completed (green) or pending (pink/purple).
        
        Analyzes the right portion of a quest row for color hints:
        - Green tint → completed
        - Pink/purple tint → pending
        
        Args:
            frame: Game frame (BGR)
            row_index: Index of the quest row (0-6)
            row_top_frac: Fraction from top where the row starts
            row_height_frac: Fraction height of the row
            
        Returns:
            "completed", "pending", or "unknown"
        """
        h, w = frame.shape[:2]
        y0 = int((row_top_frac + row_index * row_height_frac) * h)
        y1 = int((row_top_frac + (row_index + 1) * row_height_frac) * h)
        x0 = int(0.3 * w)
        x1 = int(0.95 * w)
        row_roi = frame[y0:y1, x0:x1]
        if row_roi.size == 0:
            return "unknown"

        # Check for green (completed)
        green_lower = np.array([80, 180, 80])
        green_upper = np.array([130, 255, 200])
        green_mask = cv2.inRange(row_roi, green_lower, green_upper)
        green_ratio = cv2.countNonZero(green_mask) / row_roi.size

        # Check for pink/purple (pending)
        pink_lower = np.array([160, 100, 180])
        pink_upper = np.array([220, 150, 255])
        pink_mask = cv2.inRange(row_roi, pink_lower, pink_upper)
        pink_ratio = cv2.countNonZero(pink_mask) / row_roi.size

        if green_ratio > 0.1:
            return "completed"
        if pink_ratio > 0.1:
            return "pending"
        return "unknown"