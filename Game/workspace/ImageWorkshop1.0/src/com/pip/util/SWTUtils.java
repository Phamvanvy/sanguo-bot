package com.pip.util;

import java.io.*;
import java.util.*;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;

import com.swtdesigner.SWTResourceManager;

public class SWTUtils {
	private static final String punctation = ",.?:\"!;，。？：“”！；";
	
	/**
	 * 把一个字符串按指定绘制宽度拆分成行。
	 * @param text
	 * @param gc
	 * @param width
	 * @return
	 */
	public static String[] formatText(String text, GC gc, int width) {
	    List<String> retList = new ArrayList<String>();

	    int lineStart = 0;
        int lineWid = 0;
        int charCount = text.length();

        // Loop to break the text into lines.
        int i = 0;

        while (i < charCount) {
            char ch = text.charAt(i);

            if (ch == '\n') {
                // If new line is found, record current line information and
                // step to next line.
                if (i > 0 && text.charAt(i - 1) == '\r') {
                    retList.add(text.substring(lineStart, i - 1));
                } else {
                    retList.add(text.substring(lineStart, i));
                }

                lineStart = i + 1;
                lineWid = 0;
            } else {
                int charWid = gc.textExtent("" + ch).x;

                if (lineWid == 0 || lineWid + charWid <= width) {
                    // If current character is the first in current line, or
                    // it doesn't exceed the given width, just add it into
                    // current line.
                    lineWid += charWid;
                } else {
                    // If current character exceed the given width, record
                    // current line information and add current character into
                    // the next line.

                    // Don't put punctation at the head of line
                    if (punctation.indexOf(ch) >= 0) {
                        i--;
                        charWid += gc.textExtent(text.substring(i, i + 1)).x;
                    }

                    retList.add(text.substring(lineStart, i));
                    lineStart = i;
                    lineWid = charWid;
                }
            }

            i++;
        }

        // Handle the last line.
        if (lineWid > 0) {
            retList.add(text.substring(lineStart));
        }

        // Construct return values.
        String[] ret = new String[retList.size()];
        retList.toArray(ret);

        return ret;
	}

	/**
	 * 绘制带边框，渐变色填充的圆角矩形框。
	 * @param gc 环境
	 * @param bounds 区域
	 * @param borderColor 边框颜色
	 * @param fromColor 填充起始颜色
	 * @param endColor 填充结束颜色
	 */
    public static void drawRoundRect(GC gc, org.eclipse.swt.graphics.Rectangle bounds, int borderColor, int fromColor, int endColor) {
        for (int i = 1; i < bounds.height; i++) {
            int[] rgb = computeColor(fromColor, endColor, bounds.height - 2, i - 1);
            gc.setForeground(SWTResourceManager.getColor(rgb[0], rgb[1], rgb[2]));
            gc.drawLine(bounds.x + 1, bounds.y + i, bounds.x + bounds.width - 1, bounds.y + i);
        }
        gc.setForeground(SWTResourceManager.getColor((borderColor >> 16) & 0xFF, (borderColor >> 8) & 0xFF, borderColor & 0xFF));
        gc.drawRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 5, 5);
    }
    
    /**
     * 绘制带边框，渐变色填充的圆角矩形框。
     * @param gc 环境
     * @param bounds 区域
     * @param fillWidth 填充宽度
     * @param borderColor 边框颜色
     * @param fromColor 填充起始颜色
     * @param endColor 填充结束颜色
     */
    public static void drawRoundRect(GC gc, org.eclipse.swt.graphics.Rectangle bounds, int fillWidth, int borderColor, int fromColor, int endColor) {
        for (int i = 1; i < bounds.height; i++) {
            int[] rgb = computeColor(fromColor, endColor, bounds.height - 2, i - 1);
            gc.setForeground(SWTResourceManager.getColor(rgb[0], rgb[1], rgb[2]));
            if (fillWidth > 0) {
                gc.drawLine(bounds.x + 1, bounds.y + i, bounds.x + fillWidth - 1, bounds.y + i);
            }
        }
        gc.setForeground(SWTResourceManager.getColor((borderColor >> 16) & 0xFF, (borderColor >> 8) & 0xFF, borderColor & 0xFF));
        gc.drawRoundRectangle(bounds.x, bounds.y, bounds.width, bounds.height, 5, 5);
    }
    
    private static int[] computeColor(int from, int to, int length, int step) {
        int r = computeColor2((from >> 16) & 0xFF, (to >> 16) & 0xFF, length, step);
        int g = computeColor2((from >> 8) & 0xFF, (to >> 8) & 0xFF, length, step);
        int b = computeColor2(from & 0xFF, to & 0xFF, length, step);
        return new int[] { r, g, b };
    }
    
    private static int computeColor2(int from, int to, int length, int step) {
        return from + (to - from) * step / length;
    }
    
    private static byte[] lastLayoutData;
    private static byte[] lastLayoutResult;
    
    /**
     * 合并多个矩形图块为一个大的图块，是目标的面积尽量地小。
     * @param frames 传入图块的大小，传出图块的最终布局位置
     * @return 返回结果的宽度和高度。
     */
    public static Rectangle[] getBestLayout(Rectangle[] frames) {
        if (frames.length == 0) {
            return new Rectangle[] { new Rectangle(0, 0, 0, 0) };
        }

        // 检查缓存中是否存在
        byte[] layoutData = rectsToBytes(frames);
        if (Arrays.equals(layoutData, lastLayoutData)) {
            Rectangle[] rs = bytesToRects(lastLayoutResult);
            for (int i = 0; i < frames.length; i++) {
                frames[i].x = rs[i].x;
                frames[i].y = rs[i].y;
                frames[i].width = rs[i].width;
                frames[i].height = rs[i].height;
            }
            Rectangle[] ret = new Rectangle[rs.length - frames.length];
            System.arraycopy(rs, frames.length, ret, 0, rs.length - frames.length);
            return ret;
        }
        
        // 计算总面积
        int totalArea = 0;
        for (Rectangle f : frames) {
            totalArea += f.width * f.height;
        }
        
        // 全部分在一个图片里面试一次（限制大小）
        Rectangle bounds = getBestLayoutImpl(frames, 0, frames.length, true);
        if (bounds.width * bounds.height <= totalArea * 103 / 100) {
            // 如果差别在3%之内，直接返回
            double rate = (bounds.width * bounds.height - totalArea) * 100 / (double)totalArea;
            // System.out.println("合并浪费率：" + rate + "%");
            return new Rectangle[] { bounds };
        }
        
        int bestType = -1;
        int bestSplit = -1;
        int bestArea = bounds.width * bounds.height;
        
        // 按各种排序方式排序，尝试分成两个图片
        for (int splitType = 0; splitType <= 4; splitType++) {
            Rectangle[] sortFrames = frames;
            if (splitType != 0) {
                sortFrames = new Rectangle[frames.length];
                System.arraycopy(frames, 0, sortFrames, 0, frames.length);
                sortAreas(sortFrames, splitType);
            }
            int step = (int)Math.max(1, Math.sqrt(sortFrames.length) / 2);
            for (int i = 1; i < sortFrames.length; i += step) {
                Rectangle b1 = getBestLayoutImpl(sortFrames, 0, i, true);
                Rectangle b2 = getBestLayoutImpl(sortFrames, i, sortFrames.length - i, true);
                int area = b1.width * b1.height + b2.width * b2.height;
                if (area < bestArea) {
                    bestType = splitType;
                    bestSplit = i;
                    bestArea = area;
                }
            }
        }
        Rectangle[] retBounds;
        if (bestType == -1) {
            // 还是最原始的方式好
            retBounds = new Rectangle[] { getBestLayoutImpl(frames, 0, frames.length, true) };
        } else {
            // 按找出来的最佳方式重算
            Rectangle[] sortFrames = new Rectangle[frames.length];
            System.arraycopy(frames, 0, sortFrames, 0, frames.length);
            sortAreas(sortFrames, bestType);
            Rectangle b1 = getBestLayoutImpl(sortFrames, 0, bestSplit, true);
            Rectangle b2 = getBestLayoutImpl(sortFrames, bestSplit, sortFrames.length - bestSplit, true);
            for (int i = bestSplit; i < sortFrames.length; i++) {
                sortFrames[i].x |= 1 << 14;
            }
            retBounds = new Rectangle[] { b1, b2 };
        }

        double rate = (bestArea - totalArea) * 100 / (double)totalArea;
        
        // 如果用优化效率的方式得到的结果浪费率太高，那么用精细的方式重新计算
        if (rate > 10) {
        	// 全部分在一个图片里面试一次（限制大小）
            bounds = getBestLayoutImpl(frames, 0, frames.length, false);
            if (bounds.width * bounds.height <= totalArea * 103 / 100) {
                // 如果差别在3%之内，直接返回
                rate = (bounds.width * bounds.height - totalArea) * 100 / (double)totalArea;
                // System.out.println("合并浪费率：" + rate + "%");
                return new Rectangle[] { bounds };
            }
            
            bestType = -1;
            bestSplit = -1;
            bestArea = bounds.width * bounds.height;
            
            // 按各种排序方式排序，尝试分成两个图片
            for (int splitType = 0; splitType <= 4; splitType++) {
                Rectangle[] sortFrames = frames;
                if (splitType != 0) {
                    sortFrames = new Rectangle[frames.length];
                    System.arraycopy(frames, 0, sortFrames, 0, frames.length);
                    sortAreas(sortFrames, splitType);
                }
                int step = 1;
                for (int i = 1; i < sortFrames.length; i += step) {
                    Rectangle b1 = getBestLayoutImpl(sortFrames, 0, i, false);
                    Rectangle b2 = getBestLayoutImpl(sortFrames, i, sortFrames.length - i, false);
                    int area = b1.width * b1.height + b2.width * b2.height;
                    if (area < bestArea) {
                        bestType = splitType;
                        bestSplit = i;
                        bestArea = area;
                    }
                }
            }
            if (bestType == -1) {
                // 还是最原始的方式好
                retBounds = new Rectangle[] { getBestLayoutImpl(frames, 0, frames.length, false) };
            } else {
                // 按找出来的最佳方式重算
                Rectangle[] sortFrames = new Rectangle[frames.length];
                System.arraycopy(frames, 0, sortFrames, 0, frames.length);
                sortAreas(sortFrames, bestType);
                Rectangle b1 = getBestLayoutImpl(sortFrames, 0, bestSplit, false);
                Rectangle b2 = getBestLayoutImpl(sortFrames, bestSplit, sortFrames.length - bestSplit, false);
                for (int i = bestSplit; i < sortFrames.length; i++) {
                    sortFrames[i].x |= 1 << 14;
                }
                retBounds = new Rectangle[] { b1, b2 };
            }
            rate = (bestArea - totalArea) * 100 / (double)totalArea;
        }
        // System.out.println("合并浪费率：" + rate + "%");
        
        // 保存到缓存并返回
        Rectangle[] tmpRs = new Rectangle[frames.length + retBounds.length];
        System.arraycopy(frames, 0, tmpRs, 0, frames.length);
        System.arraycopy(retBounds, 0, tmpRs, frames.length, retBounds.length);
        lastLayoutData = layoutData;
        lastLayoutResult = rectsToBytes(tmpRs);
        
        return retBounds;
    }
    
    private static void sortAreas(Rectangle[] rects, int sortType) {
        if (sortType == 1) {
            // 按高度排序
            Arrays.sort(rects, new Comparator<Rectangle>() {
                public int compare(Rectangle o1, Rectangle o2) {
                    return o2.height - o1.height;
                }
                
                public boolean equals(Object obj) {
                    return true;
                }
            });
        } else if (sortType == 2) {
            // 按宽度排序
            Arrays.sort(rects, new Comparator<Rectangle>() {
                public int compare(Rectangle o1, Rectangle o2) {
                    return o2.width - o1.width;
                }
                
                public boolean equals(Object obj) {
                    return true;
                }
            });
        } else if (sortType == 3) {
            // 按面积排序
            Arrays.sort(rects, new Comparator<Rectangle>() {
                public int compare(Rectangle o1, Rectangle o2) {
                    return o2.width * o2.height - o1.width * o1.height;
                }
                
                public boolean equals(Object obj) {
                    return true;
                }
            });
        } else if (sortType == 4) {
            // 按宽高比例排序
            Arrays.sort(rects, new Comparator<Rectangle>() {
                public int compare(Rectangle o1, Rectangle o2) {
                    double r1 = (double)o1.width / o1.height;
                    double r2 = (double)o2.width / o2.height;
                    if (r1 < r2) {
                        return -1;
                    } else if (r1 > r2) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
                
                public boolean equals(Object obj) {
                    return true;
                }
            });
        }
    }
    
    private static byte[] rectsToBytes(Rectangle[] rects) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            for (Rectangle r : rects) {
                dos.writeInt(r.x);
                dos.writeInt(r.y);
                dos.writeInt(r.width);
                dos.writeInt(r.height);
            }
            dos.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
    
    private static Rectangle[] bytesToRects(byte[] arr) {
        try {
            int count = arr.length / 16;
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(arr));
            Rectangle[] ret = new Rectangle[count];
            for (int i = 0; i < count; i++) {
                ret[i] = new Rectangle(dis.readInt(), dis.readInt(), dis.readInt(), dis.readInt());
            }
            return ret;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 合并多个矩形图块为一个大的图块，是目标的面积尽量地小。
     * @param frames 传入图块的大小，传出图块的最终布局位置
     * @return 返回结果的宽度和高度。
     */
    public static Rectangle getBestLayoutImpl(Rectangle[] frames, int start, int count, boolean optimize) {
        // 计算最小图片宽度，最大图片宽度
        int minw = 10000000, maxw = 0, totalw = 0;
        for (int i = start; i < start + count; i++) {
            if (frames[i].width > maxw) {
                maxw = frames[i].width;
            }
            if (frames[i].width < minw) {
                minw = frames[i].width;
            }
            totalw += frames[i].width;
        }
        
        int valve1 = maxw;
        int valve2 = totalw;
        if (optimize) {
        	valve1 = (int)Math.max(maxw, minw * Math.sqrt(count) / 2);
        	valve2 = (int)(totalw * 2 / Math.sqrt(count));
        }
        if (valve2 > 1024) {
        	valve2 = 1024;
        }
        
        // 从最大图片宽度，递增到总宽度，补偿为最小图片宽度，穷举找出最适合的宽度
        int minSquare = Integer.MAX_VALUE;
        int minSquareW = 0;
        int minSquareOff = Integer.MAX_VALUE;
        for (int w = valve1; w <= valve2; w += minw) {
            Rectangle rect = getBestLayout(frames, start, count, w);
            if (rect.width * rect.height < minSquare) {
                minSquare = rect.width * rect.height;
                minSquareW = w;
                minSquareOff = Math.abs(rect.width - rect.height);
            } else if (rect.width * rect.height == minSquare) {
                int off = Math.abs(rect.width - rect.height);
                if (off < minSquareOff) {
                    minSquareW = w;
                    minSquareOff = off;
                }
            }
        }
        
        // 使用找出的最适合宽度重新布局
        return getBestLayout(frames, start, count, minSquareW);
    }
    
    /**
     * 在指定宽度的区域中，找出N个图块的最佳布局，保证浪费的空间最少。
     * @param frames 传入图块的大小，传出图块的最终布局位置
     * @param fitWidth 指定宽度，布局不能超出此宽度
     * @return 返回结果的宽度和高度。
     */
    public static Rectangle getBestLayout(Rectangle[] frames, int start, int count, int fitWidth) {
        // 先把图块按高度从大到小排序
        Rectangle[] sortedArr = new Rectangle[count];
        System.arraycopy(frames, start, sortedArr, 0, count);
        Arrays.sort(sortedArr, new Comparator<Rectangle>() {
            public int compare(Rectangle o1, Rectangle o2) {
                return o2.height - o1.height;
            }
            
            public boolean equals(Object obj) {
                return true;
            }
        });
        
        
        // 按从左到右方向布局，规则如下：
        // 1. 如果可用区域中有区域能包含下一个图块，则找一个差别最小的区域放置（差别=高度差+宽度差），
        //    如果目标区域不能完全包含图块，则把剩余的区域加入可用区域列表。
        // 2. 如果没有可用区域，而且当前宽度加上下一个图块的宽度没有超过fitWidth，则把下一个图块放到
        //    最右边，并把下方的空闲区域加入可用区域列表。
        // 3. 如果没有可用区域，而且当前宽度加上下一个图块的宽度超过了fitWidth，则从所有可用区域中
        //    找出宽度超过下一个图块宽度并且Y值最小的区域，把它向下扩展（所有区域都需要扩展）使其
        //    达到可以放置下一个图块的高度，把下一个图块放入它。
        // 4. 如果上面的搜索没有结果，则把下一个图块放到左边的下方。
        List<Rectangle> availAreas = new ArrayList<Rectangle>();    // 可用区域
        int right = 0, bottom = 0;
        for (int i = 0; i < sortedArr.length; i++) {
            Rectangle r = sortedArr[i];
            int bestIndex = findBestArea(availAreas, r);
            if (bestIndex != -1) {
                // 情况1，把下一个图块放在这个位置并更新可用区域大小
                Rectangle tr = availAreas.get(bestIndex);
                r.x = tr.x;
                r.y = tr.y;
                if (r.width == tr.width && r.height == tr.height) {
                    // 完全符合，从可用表中移除
                    availAreas.remove(bestIndex);
                } else if (r.width == tr.width) {
                    // 宽度符合，下面有空隙，更新y和height
                    tr.y += r.height;
                    tr.height -= r.height;
                } else if (r.height == tr.height) {
                    // 高度符合，右边有空隙，更新x和width
                    tr.x += r.width;
                    tr.width -= r.width;
                } else {
                    // 高度高度都不符合，把原来的区域修改为右边空隙，并且把左下角的空隙加入可用列表
                    Rectangle newR = new Rectangle(tr.x, tr.y + r.height, r.width, tr.height - r.height);
                    tr.x += r.width;
                    tr.width -= r.width;
                    mergeArea(availAreas, newR);
                }
            } else if (right + r.width <= fitWidth) {
                // 情况2，把下一个图块放到最右边
                r.x = right;
                r.y = 0;
                right += r.width;
                if (r.height < bottom) {
                    // 下面有空隙，加入可用区域
                    mergeArea(availAreas, new Rectangle(r.x, r.height, r.width, bottom - r.height));
                } else if (r.height > bottom) {
                    // 扩展高度，以前所有的可用区域都需要扩展
                    extendArea(availAreas, bottom, r.height - bottom, 0, right - r.width);
                    bottom = r.height;
                }
            } else {
                // 情况3或4，需要向下扩展
                bestIndex = findMinYArea(availAreas, r, bottom);
                int addY;
                if (bestIndex != -1) {
                    addY = r.height - availAreas.get(bestIndex).height;
                    extendArea(availAreas, bottom, addY, 0, right);
                } else {
                    addY = r.height;
                    right = Math.max(right, r.width);
                    extendArea(availAreas, bottom, addY, r.width, right);
                    availAreas.add(new Rectangle(0, bottom, r.width, r.height));
                }
                bottom += addY;
                i--;
            }
        }
        return new Rectangle(0, 0, right, bottom);
    }
    
    /*
     * 在一个区域列表中查找能够包含目标区域的最适合的区域，返回其索引。如果找不到，返回-1。
     */
    private static int findBestArea(List<Rectangle> list, Rectangle target) {
        int size = list.size();
        int bestIndex = -1;
        int bestWaste = 100000000;
        for (int i = 0; i < size; i++) {
            Rectangle r = list.get(i);
            int wo = r.width - target.width;
            int ho = r.height - target.height;
            if (wo >= 0 && ho >= 0 && wo * 2 + ho < bestWaste) {
                bestIndex = i;
                bestWaste = wo * 2 + ho;
            }
        }
        return bestIndex;
    }
    
    /*
     * 在一个区域列表中查找能够宽度超过目标区域的Y值最小的区域，返回其索引。如果找不到，返回-1。
     */
    private static int findMinYArea(List<Rectangle> list, Rectangle target, int bottom) {
        int size = list.size();
        int bestIndex = -1;
        int bestY = 100000000;
        for (int i = 0; i < size; i++) {
            Rectangle r = list.get(i);
            if (r.y + r.height < bottom) {
                continue;
            }
            if (r.width >= target.width && r.y < bestY) {
                bestIndex = i;
                bestY = r.y;
            }
        }
        return bestIndex;
    }
    
    /*
     * 向可用区域列表中加入一个新区域，尽量合并。
     */
    private static void mergeArea(List<Rectangle> list, Rectangle target) {
        for (Rectangle source : list) {
            if (source.x == target.x && source.width == target.width) {
                if (source.y + source.height == target.y) {
                    source.height += target.height;
                    return;
                } else if (source.y == target.y + target.height) {
                    source.y = target.y;
                    source.height += target.height;
                    return;
                }
            } else if (source.y == target.y && source.height == target.height) {
                if (source.x + source.width == target.x) {
                    source.width += target.width;
                    return;
                } else if (source.x == target.x + target.width) {
                    source.x = target.x;
                    source.width += target.width;
                    return;
                }
            }
        }
        list.add(target);
    }
    
    /*
     * 目标区域变大了，调整可用区域列表。
     */
    private static void extendArea(List<Rectangle> list, int curY, int addY, int startX, int right) {
        // 先把图块按宽度从小到大排序
        Rectangle[] sortedArr = new Rectangle[list.size()];
        list.toArray(sortedArr);
        Arrays.sort(sortedArr, new Comparator<Rectangle>() {
            public int compare(Rectangle o1, Rectangle o2) {
                return o1.x - o2.x;
            }
            
            public boolean equals(Object obj) {
                return true;
            }
        });
        
        // 扩展所有现有的区域，中间空隙的区域新建
        int curX = startX;
        for (Rectangle r : sortedArr) {
            if (r.y + r.height < curY) {
                continue;
            }
            if (curX > r.x) {
                continue;
            } else if (curX < r.x) {
                list.add(new Rectangle(curX, curY, r.x - curX, addY));
            }
            r.height += addY;
            curX = r.x + r.width;
        }
        if (curX < right) {
            list.add(new Rectangle(curX, curY, right - curX, addY));
        }
    }
    
    public static Color getColor(int clr) {
        int r = (clr >> 16) & 0xFF;
        int g = (clr >> 8) & 0xFF;
        int b = clr & 0xFF;
        return SWTResourceManager.getColor(r, g, b);
    }
    
    public static RGB getRGB(int clr) {
        int r = (clr >> 16) & 0xFF;
        int g = (clr >> 8) & 0xFF;
        int b = clr & 0xFF;
        return new RGB(r, g, b);
    }
    
    /**
     * 显示/隐藏在GridLayout中的一个控件。
     * @param c
     * @param show
     */
    public static void showControl(Control c, boolean show) {
        if (show) {
            c.setVisible(true);
            GridData gd = (GridData)c.getLayoutData();
            gd.exclude = false;
        } else {
            c.setVisible(false);
            GridData gd = (GridData)c.getLayoutData();
            gd.exclude = true;
        }
        c.getParent().layout();
    }
    
    /**
     * 读取一个图片中一部分RGB数据。
     */
    public static int[][] getImageData(Image img, Rectangle area) {
		int[][] ret = new int[area.height][area.width];
		ImageData imageData = img.getImageData();
        for (int i = 0; i < ret.length; i++) {
        	int[] samples = new int[area.width];
        	imageData.getPixels(area.x, area.y + i, area.width, samples, 0);
            for (int j = 0; j < samples.length; j++) {
            	int clr, a, r, g, b;
            	if (imageData.palette.isDirect) {
            		clr = samples[j];
            		if (imageData.depth == 24) {
            			a = 0xFF;
            		} else if (imageData.alphaData != null) {
            			a = imageData.alphaData[(i + area.y) * imageData.width + j + area.x] & 0xFF;
            		} else {
            			a = clr & 0xFF;
            		}
            		r = ((clr & imageData.palette.redMask) >> -(imageData.palette.redShift)) & 0xFF;
            		g = ((clr & imageData.palette.greenMask) >> -(imageData.palette.greenShift)) & 0xFF;
					b = ((clr & imageData.palette.blueMask) >> -(imageData.palette.blueShift)) & 0xFF;
					clr = (a << 24) | (r << 16) | (g << 8) | b;
            	} else {
            		RGB rgb = imageData.palette.colors[samples[j]];
            		clr = (rgb.red << 16) | (rgb.green << 8) | rgb.blue;
            		if (imageData.alphaData != null) {
            			byte alpha = imageData.alphaData[(area.y + i) * imageData.width + area.x + j];
            			clr |= alpha << 24;
            		} else if (samples[j] != imageData.transparentPixel) {
            			clr |= 0xFF000000;
            		}
            	}
            	if ((clr & 0xFF000000) == 0) {
            		clr = 0;
            	}
            	ret[i][j] = clr;
            }
        }
		return ret;
	}
}
