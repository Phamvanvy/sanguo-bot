package com.pip.mapeditor.tool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu;

import com.pip.mapeditor.*;
import com.pip.mapeditor.data.*;
import com.pipimage.image.*;
import com.swtdesigner.SWTResourceManager;

/**
 * 模糊地图地形贴图工具。这个工具根据在地形查看器中选中的地形，在当前选中的模糊地图层中绘图。
 * @author lighthu
 */
public class LandformTool implements IMapEditTool {
    // 附着的编辑器
    private MapViewer viewer;
    // 地形选择窗口
    private MapLandformSelector landformView;
    // 最近一次检测到的鼠标位置
    private int lastX, lastY;
    // 当前是否拖动状态
    private boolean isDragging;
    // 最近一次拖动绘制的格点集合
    private HashSet<Point> renderCells;
    private long randomSeed = System.currentTimeMillis();

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public LandformTool(MapViewer viewer, MapLandformSelector tv) {
        this.viewer = viewer;
        landformView = tv;
        renderCells = new HashSet<Point>();
        lastX = -1;
        lastY = -1;
    }

    // 根据坐标计算此坐标对应的格点坐标
    private void map2cell(Point pt) {
        pt.x /= viewer.getMap().parent.getBlurTileWidth();
        pt.y /= viewer.getMap().parent.getBlurTileHeight();
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
            BlurMapLayer alayer = getCurrentLayer();
            if (alayer == null) {
                return;
            }
            byte[][] data = alayer.getLayerData();
            int landform = landformView.getSelectedFrame();
            if (landform == alayer.getBaseLandform()) {
                landform = -1;
            }
            int brushSize = landformView.getBrushSize();
            for (Point pt : renderCells) {
                int sx = pt.x - (brushSize + 1) / 2;
                int ex = sx + brushSize + 2;
                int sy = pt.y - (brushSize + 1) / 2;
                int ey = sy + brushSize + 2;
                sx = Math.max(0, sx);
                ex = Math.min(ex, data[0].length);
                sy = Math.max(0, sy);
                ey = Math.min(ey, data.length);
                for (int i = sy; i < ey; i++) {
                    for (int j = sx; j < ex; j++) {
                        if (data[i][j] > landform) {
                            data[i][j] = (byte)landform;
                        }
                    }
                }
                
                sx = pt.x - (brushSize - 1) / 2;
                ex = sx + brushSize;
                sy = pt.y - (brushSize - 1) / 2;
                ey = sy + brushSize;
                sx = Math.max(0, sx);
                ex = Math.min(ex, data[0].length);
                sy = Math.max(0, sy);
                ey = Math.min(ey, data.length);
                for (int i = sy; i < ey; i++) {
                    for (int j = sx; j < ex; j++) {
                        data[i][j] = (byte)landform;
                    }
                }
            }
            if (renderCells.size() > 0) {
                alayer.clearBuffer();
                viewer.fireContentChanged();
                viewer.refresh();
            }
        }
    }
    
    // 取得绘制的目标地图层。如果当前选中的层是模糊图层，则就在这层上绘制，否则在最
    // 顶层的模糊图层上绘制。
    // 如果找不到绘制目标图层，返回null
    private BlurMapLayer getCurrentLayer() {
        int activeLayer = viewer.getActiveLayer();
        if (activeLayer != -1) {
            IMapLayer layer = viewer.getMap().layers.get(activeLayer);
            if (layer instanceof BlurMapLayer) {
                return (BlurMapLayer)layer;
            }
        }
        ArrayList<IMapLayer> layers = viewer.getMap().layers;
        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i) instanceof BlurMapLayer) {
                return (BlurMapLayer)layers.get(i);
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
        int landform = landformView.getSelectedFrame();
        if (landform != -1) {
            int tw = map.parent.getBlurTileWidth();
            int th = map.parent.getBlurTileHeight();
            int rows = map.height / th;
            int cols = map.width / tw;
            int brushSize = landformView.getBrushSize();
            byte[][] sg = BlurMapUtil.makeRectangle(brushSize + 2, brushSize + 2);
            byte[][] tmpMap = new byte[rows + 2 * brushSize][cols + 2 * brushSize];
            int brushOffset = (brushSize + 1) / 2;

            // 绘制拖动的内容
            if (isDragging) {
                for (Point pt : renderCells) {
                    updateMapData(tmpMap, pt.x - brushOffset + brushSize, pt.y - brushOffset + brushSize, sg);
                }
            }
    
            // 绘制当前坐标上的Tile
            Point pt = new Point(lastX, lastY);
            if (isValidCell(pt)) {
                map2cell(pt);
                updateMapData(tmpMap, pt.x - brushOffset + brushSize, pt.y - brushOffset + brushSize, sg);
            }
        
            pt = new Point(-tw * brushSize, -th * brushSize);
            viewer.map2screen(pt);
            BlurMapUtil.drawLandform(gc, (LandformImage)map.parent.getLandforms().get(landform).image, new Random(randomSeed), 
                    tmpMap, pt.x, pt.y, map.parent.getBlurTileWidth(), map.parent.getBlurTileHeight(), viewer.getRatio());
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
    
    private void updateMapData(byte[][] tmpMap, int x, int y, byte[][] sg) {
        for (int i = 0; i < sg.length; i++) {
            for (int j = 0; j < sg[0].length; j++) {
                tmpMap[y + i][x + j] |= sg[i][j];
            }
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
