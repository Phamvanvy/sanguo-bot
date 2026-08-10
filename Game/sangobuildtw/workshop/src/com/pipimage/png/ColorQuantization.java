package com.pipimage.png;

import java.util.*;

public class ColorQuantization {
    protected int[] pixels = null;
    protected int maxCount;
    protected static int[] gridMasks = {
        0xC0C0C0C0, 0xE0E0E0E0, 0xF0F0F0F0, 0xF8F8F8F8, 0xFCFCFCFC, 0xFEFEFEFE, 0xFFFFFFFF
    };
    protected Map<Integer, Integer> colorScores = new HashMap<Integer, Integer>();
    protected int[] chosenColors;
    protected Map<Integer, Integer> colorMap = new HashMap<Integer, Integer>();

    public ColorQuantization(int[] pixels, int mc) {
        this.pixels = pixels;
        this.maxCount = mc;
    }
    
    public int convert(int clr) {
        if (!colorMap.containsKey(clr)) {
            return clr;
        }
        return colorMap.get(clr);
    }
    
    public void process() {
        analzyeColorUsage();
        if (maxCount >= colorScores.size()) {
            return;
        }
        Map<Integer, Double> gridScores = null;
        int usedMask = 0;
        for (int i = 0; i < gridMasks.length; i++) {
            usedMask = gridMasks[i];
            gridScores = gridColors(usedMask);
            if (gridScores.size() >= maxCount) {
                break;
            }
        }
        makeDistPrior(gridScores);
        int[] bestgrids = chooseGrids(gridScores);
        chooseColorInGrids(bestgrids, usedMask);
        mapRemovedColors();
    }
    
    protected void analzyeColorUsage() {
        colorScores.clear();
        int len = pixels.length;
        for (int i = 0; i < len; i++) {
            int p = pixels[i];
            if (colorScores.containsKey(p)) {
                colorScores.put(p, colorScores.get(p) + 1);
            } else {
                colorScores.put(p, 1);
            }
        }
    }
    
    protected Map<Integer, Double> gridColors(int mask) {
        Map<Integer, Double> gridScores = new HashMap<Integer, Double>();
        for (int clr : colorScores.keySet()) {
            int score = colorScores.get(clr);
            clr &= mask;
            if (gridScores.containsKey(clr)) {
                gridScores.put(clr, gridScores.get(clr) + score);
            } else {
                gridScores.put(clr, (double)score);
            }
        }
        return gridScores;
    }
    
    protected double dist(int p1, int p2) {
        int a = ((p1 >> 24) & 0xFF) - ((p2 >> 24) & 0xFF);
        int r = ((p1 >> 16) & 0xFF) - ((p2 >> 16) & 0xFF);
        int g = ((p1 >> 8) & 0xFF) - ((p2 >> 8) & 0xFF);
        int b = ((p1 >> 0) & 0xFF) - ((p2 >> 0) & 0xFF);
        return Math.sqrt(a * a + r * r + g * g + b * b);
    }
    
    protected void makeDistPrior(Map<Integer, Double> gridScores) {
        Object[] arr = gridScores.keySet().toArray();
        for (int i = 0; i < arr.length; i++) {
            int c1 = ((Integer)arr[i]).intValue();
            double tdist = 0.0;
            for (int j = 0; j < arr.length; j++) {
                int c2 = ((Integer)arr[j]).intValue();
                tdist += dist(c1, c2);
            }
            double oldScore = gridScores.get(c1);
            gridScores.put(c1, oldScore * tdist);
        }
    }
    
    protected int[] chooseGrids(Map<Integer, Double> gridScores) {
        int[] keys = new int[gridScores.size()];
        double[] scores = new double[gridScores.size()];
        Object[] arr = gridScores.keySet().toArray();
        for (int i = 0; i < arr.length; i++) {
            keys[i] = ((Integer)arr[i]).intValue();
            scores[i] = gridScores.get(keys[i]);
        }
        for (int i = 0; i < keys.length; i++) {
            for (int j = i + 1; j < keys.length; j++) {
                if (scores[i] < scores[j]) {
                    int itemp = keys[i];
                    keys[i] = keys[j];
                    keys[j] = itemp;
                    double dtemp = scores[i];
                    scores[i] = scores[j];
                    scores[j] = dtemp;
                }
            }
        }
        int[] ret = new int[maxCount];
        System.arraycopy(keys, 0, ret, 0, maxCount);
        return ret;
    }
    
    protected void chooseColorInGrids(int[] grids, int mask) {
        colorMap.clear();
        chosenColors = new int[grids.length];
        for (int i = 0; i < grids.length; i++) {
            chosenColors[i] = chooseColorInGrid(grids[i], mask);
        }
    }
    
    protected int chooseColorInGrid(int gridValue, int mask) {
        int bestClr = 0, bestScore = 0;
        List<Integer> gridClrs = new ArrayList<Integer>();
        for (int clr : colorScores.keySet()) {
            if ((clr & mask) == gridValue) {
                int score = colorScores.get(clr);
                if (score > bestScore) {
                    bestClr = clr;
                    bestScore = score;
                }
                gridClrs.add(clr);
            }
        }
        for (int clr : gridClrs) {
            colorMap.put(clr, bestClr);
        }
        return bestClr;
    }
    
    protected void mapRemovedColors() {
        for (int clr : colorScores.keySet()) {
            if (colorMap.containsKey(clr)) {
                continue;
            }
            colorMap.put(clr, chooseBestMatch(clr));
        }
    }
    
    protected int chooseBestMatch(int clr) {
        int bestClr = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < chosenColors.length; i++) {
            double dist = dist(clr, chosenColors[i]);
            if (dist < bestDist) {
                bestClr = chosenColors[i];
                bestDist = dist;
            }
        }
        return bestClr;
    }
}
