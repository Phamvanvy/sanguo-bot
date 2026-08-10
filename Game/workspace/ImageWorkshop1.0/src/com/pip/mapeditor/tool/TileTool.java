package com.pip.mapeditor.tool;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu;

import com.pip.mapeditor.*;
import com.pip.mapeditor.data.*;
import com.pipimage.image.*;
import com.swtdesigner.SWTResourceManager;

/**
 * 精确地图贴图工具。这个工具根据在贴图查看器中选中的Tile，修改当前选中地图层的贴图信息。
 * @author lighthu
 */
public class TileTool implements IMapEditTool {
    // 附着的编辑器
    private MapViewer viewer;
    // 贴图选择影响的贴图查看器对象。当选中某个贴图块时会在贴图查看器中也选中。
    private MapTileSelector tileView;
    // 最近一次检测到的鼠标位置
    private int lastX, lastY;
    // 当前是否拖动状态
    private boolean isDragging;
    // 最近一次拖动绘制的格点集合
    private HashSet<Point> renderCells;

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public TileTool(MapViewer viewer, MapTileSelector tv) {
        this.viewer = viewer;
        tileView = tv;
        renderCells = new HashSet<Point>();
        lastX = -1;
        lastY = -1;
    }

    // 根据坐标计算此坐标对应的格点坐标
    private void map2cell(Point pt) {
        pt.x /= viewer.getMap().parent.getTileWidth();
        pt.y /= viewer.getMap().parent.getTileHeight();
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
        isDragging = true;
        renderCells.clear();
        Point pt = new Point(x, y);
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
            AccurateMapLayer alayer = getCurrentLayer();
            if (alayer == null) {
                return;
            }
            short[][] data = alayer.getLayerData();
            int tileValue = tileView.getSelectedFrame() - 1;
            for (Point pt : renderCells) {
                data[pt.y][pt.x] = (short) tileValue;
            }
            if (renderCells.size() > 0) {
                viewer.fireContentChanged();
                viewer.refresh();
            }
        }
    }
    
    // 取得绘制的目标地图层。如果当前选中的层是精确图层，则就在这层上绘制，否则在最
    // 顶层的精确图层上绘制。
    // 如果找不到绘制目标图层，返回null
    private AccurateMapLayer getCurrentLayer() {
        int activeLayer = viewer.getActiveLayer();
        if (activeLayer != -1) {
            IMapLayer layer = viewer.getMap().layers.get(activeLayer);
            if (layer instanceof AccurateMapLayer) {
                return (AccurateMapLayer)layer;
            }
        }
        ArrayList<IMapLayer> layers = viewer.getMap().layers;
        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i) instanceof AccurateMapLayer) {
                return (AccurateMapLayer)layers.get(i);
            }
        }
        return null;
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
        Image tileImage;
        if (tileView.getSelectedFrame() == 0) {
            tileImage = null;
        } else {
            TileSet ts = map.parent.getTileImage();
            TileInfo ti = ts.tileInfo.get(tileView.getSelectedFrame() - 1);
            tileImage = ts.image.getImageDraw(ti.frameID).createSWTImage(gc.getDevice(), ti.transit);
        }
        if (isDragging) {
            // 绘制本次拖动影响到的Tile
            for (Point pt : renderCells) {
                drawTile(gc, pt, tileImage);
            }
        }

        // 绘制当前坐标上的Tile
        Point pt = new Point(lastX, lastY);
        if (isValidCell(pt)) {
            map2cell(pt);
            drawTile(gc, pt, tileImage);
        }

        if (tileImage != null) {
            tileImage.dispose();
        }

        // 绘制座标
        String coordStr = lastX + "," + lastY + "," + (lastX / map.parent.getCellSize()) + "," + (lastY / map.parent.getCellSize());
        Point size = viewer.getSize();
        Point ts = gc.textExtent(coordStr);
        gc.setForeground(MapViewer.invert(viewer.getBackground()));
        gc.setBackground(viewer.getBackground());
        gc.drawRectangle(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
        gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
    }

    /**
     * 绘制一个Tile。
     * @param gc
     * @param pt 地图中格点坐标
     * @param image 如果为null，表示绘制透明
     */
    private void drawTile(GC gc, Point pt, Image image) {
        int tw = viewer.getMap().parent.getTileWidth();
        int th = viewer.getMap().parent.getTileHeight();
        Rectangle rect = new Rectangle(pt.x * tw, pt.y * th, tw, th);
        viewer.map2screen(rect);
        if (image != null) {
            gc.drawImage(image, 0, 0, tw, th, rect.x, rect.y, rect.width, rect.height);
        } else {
            MapViewer.paintTransparentBackground(gc, rect.x, rect.y, rect.width, rect.height);
        }
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
