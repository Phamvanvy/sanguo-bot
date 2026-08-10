package com.pip.mapeditor.tool;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu;

import com.pip.mango.jni.GLGraphics;
import com.pip.mapeditor.*;
import com.pip.mapeditor.data.*;
import com.pipimage.image.*;
import com.swtdesigner.SWTResourceManager;

/**
 * 视野遮挡工具。
 * @author lighthu
 */
public class EyesightTool implements IMapEditTool {
    // 附着的编辑器
    private MapViewer viewer;
    // 最近一次检测到的鼠标位置
    private int lastX, lastY;
    // 当前是否拖动状态
    private boolean isDragging;
    // 设置按钮的位置和大小
    private Rectangle[] buttonBounds = new Rectangle[2];
    // 设置按钮的文本
    private String[] buttonTexts = { "设置", "清除" };
    // 选中的按钮
    private int selectedButtonIndex = 0;
    // 最近一次拖动绘制的格点集合
    private HashSet<Point> renderCells;

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public EyesightTool(MapViewer viewer) {
        this.viewer = viewer;
        renderCells = new HashSet<Point>();
        lastX = -1;
        lastY = -1;
    }

    // 根据坐标计算此坐标对应的格点坐标
    private void map2cell(Point pt) {
        pt.x /= viewer.getMap().parent.getCellSize();
        pt.y /= viewer.getMap().parent.getCellSize();
    }

    // 判断一个坐标是否合法格点位置坐标
    private boolean isValidCell(Point pt) {
        if (pt.x < 0 || pt.x >= viewer.getMap().width) {
            return false;
        }
        if (pt.y < 0 || pt.y >= viewer.getMap().height) {
            return false;
        }
        return true;
    }

    /**
     * 鼠标按下事件。开始拖动绘制。
     * 贴图查看器中当前选中贴图。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseDown(int x, int y, int mask) {
        // 检查是否点到按钮选择框
        Point pt = new Point(x, y);
        viewer.map2screen(pt);
        for (int i = 0; i < buttonBounds.length; i++) {
            if (buttonBounds[i].contains(pt)) {
                return;
            }
        }

        isDragging = true;
        renderCells.clear();
        pt = new Point(x, y);
        if (isValidCell(pt)) {
            map2cell(pt);
            renderCells.add(pt);
        }
        viewer.redraw();
    }

    /**
     * 鼠标抬起事件。拖动绘制结束，把拖动过的点都设置为选中的Tile。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask) {
        if (isDragging) {
            isDragging = false;
            boolean[][] data = viewer.getMap().eyesightBlock;
            for (Point pt : renderCells) {
                data[pt.y][pt.x] = selectedButtonIndex == 0;
            }
            if (renderCells.size() > 0) {
                renderCells.clear();
                viewer.fireContentChanged();
                viewer.redraw();
            }
            return;
        }

        // 检查是否点到按钮选择框
        Point pt = new Point(x, y);
        viewer.map2screen(pt);
        for (int i = 0; i < buttonBounds.length; i++) {
            if (buttonBounds[i].contains(pt)) {
                this.selectedButtonIndex = i;
                viewer.redraw();
                return;
            }
        }
    }
    
    /**
     * 鼠标移动事件。拖动刷子。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     */
    public void mouseMove(int x, int y) {
        lastX = x;
        lastY = y;
        if (isDragging) {
            Point pt = new Point(x, y);
            if (isValidCell(pt)) {
                map2cell(pt);
                renderCells.add(pt);
            }
        }
        viewer.redraw();
    }

    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc) {
        GameMap map = viewer.getMap();
        int cellSize = map.parent.getCellSize();
        
        // 绘制所有标记为阻挡视线的块
        gc.setAlpha(0x80);
        gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        for (int i = 0; i < map.eyesightBlock.length; i++) {
            for (int j = 0; j < map.eyesightBlock[i].length; j++) {
                boolean value = map.eyesightBlock[i][j];
                if (!value) {
                    continue;
                }
                if (isDragging && renderCells.contains(new Point(j, i))) {
                    continue;
                }
                Rectangle rect = new Rectangle(j * cellSize, i * cellSize, cellSize, cellSize);
                viewer.map2screen(rect);
                gc.fillRectangle(rect);
            }
        }

        // 绘制本次拖动的块
        if (isDragging && selectedButtonIndex == 0) {
            for (Point pt : renderCells) {
                Rectangle rect = new Rectangle(pt.x * cellSize, pt.y * cellSize, cellSize, cellSize);
                viewer.map2screen(rect);
                gc.fillRectangle(rect);
            }
        }
        gc.setAlpha(0xFF);
        
        // 绘制按钮
        Point size = viewer.getSize();
        Point ts = gc.textExtent(buttonTexts[0]);
        int bx = 1;
        int by = size.y - ts.y - 8;
        int bw = ts.x + 7;
        int bh = ts.y + 6;
        gc.setBackground(viewer.getBackground());
        gc.setForeground(MapViewer.invert(viewer.getBackground()));
        for (int i = 0; i < buttonTexts.length; i++) {
            buttonBounds[i] = new Rectangle(bx, by, bw, bh);
            if (i == selectedButtonIndex) {
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
            }
            gc.drawRectangle(buttonBounds[i]);
            gc.drawText(buttonTexts[i], buttonBounds[i].x + 4, buttonBounds[i].y + 4);
            if (i == selectedButtonIndex) {
                gc.setForeground(MapViewer.invert(viewer.getBackground()));
            }
            bx += bw + 2;
        }
        
        // 绘制座标
        String coordStr = lastX + "," + lastY + "," + (lastX / cellSize) + "," + (lastY / cellSize);
        ts = gc.textExtent(coordStr);
        gc.setForeground(MapViewer.invert(viewer.getBackground()));
        gc.setBackground(viewer.getBackground());
        gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
        gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
    }
    
    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GLGraphics gc) {
        GameMap map = viewer.getMap();
        int cellSize = map.parent.getCellSize();
        
        // 绘制所有标记为阻挡视线的块
        gc.setColor(0x80000000);
        for (int i = 0; i < map.eyesightBlock.length; i++) {
            for (int j = 0; j < map.eyesightBlock[i].length; j++) {
                boolean value = map.eyesightBlock[i][j];
                if (!value) {
                    continue;
                }
                if (isDragging && renderCells.contains(new Point(j, i))) {
                    continue;
                }
                Rectangle rect = new Rectangle(j * cellSize, i * cellSize, cellSize, cellSize);
                viewer.map2screen(rect);
                gc.fillRect(rect.x, rect.y, rect.width, rect.height);
            }
        }

        // 绘制本次拖动的块
        if (isDragging && selectedButtonIndex == 0) {
            for (Point pt : renderCells) {
                Rectangle rect = new Rectangle(pt.x * cellSize, pt.y * cellSize, cellSize, cellSize);
                viewer.map2screen(rect);
                gc.fillRect(rect.x, rect.y, rect.width, rect.height);
            }
        }
        gc.setColor(0xFF000000);
        
        // 绘制按钮
        Point size = viewer.getSize();
        int tw = gc.stringWidth(buttonTexts[0]);
        int th = gc.getFontHeight();
        int bx = 1;
        int by = size.y - th - 8;
        int bw = tw + 7;
        int bh = th + 6;
        Color clr = MapViewer.invert(viewer.getBackground());
        gc.setColor(0xFF, clr.getRed(), clr.getGreen(), clr.getBlue());
        for (int i = 0; i < buttonTexts.length; i++) {
            buttonBounds[i] = new Rectangle(bx, by, bw, bh);
            if (i == selectedButtonIndex) {
            	gc.setColor(0xFFFF0000);
            }
            gc.drawRect(buttonBounds[i].x, buttonBounds[i].y, buttonBounds[i].width, buttonBounds[i].height);
            gc.drawString(buttonTexts[i], buttonBounds[i].x + 4, buttonBounds[i].y + 4, GLGraphics.TOP | GLGraphics.LEFT);
            if (i == selectedButtonIndex) {
            	gc.setColor(0xFF, clr.getRed(), clr.getGreen(), clr.getBlue());
            }
            bx += bw + 2;
        }
        
        // 绘制座标
        String coordStr = lastX + "," + lastY + "," + (lastX / cellSize) + "," + (lastY / cellSize);
        tw = gc.stringWidth(coordStr);
        gc.setColor(0xFF, clr.getRed(), clr.getGreen(), clr.getBlue());
        gc.drawRect(size.x - tw - 9, size.y - th - 8, tw + 7, th + 6);
        gc.drawString(coordStr, size.x - tw - 5, size.y - th - 5, GLGraphics.TOP | GLGraphics.LEFT);
    }

    /**
     * 键按下事件。
     */
    public void onKeyDown(int keyCode) {}
    
    /**
     * 键松开事件。
     */
    public void onKeyUp(int keyCode) {}

    /**
     * 得到工具右键菜单。
     */
    public Menu getMenu() {
        return null;
    }
}
