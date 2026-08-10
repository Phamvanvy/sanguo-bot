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
 * 通过性编辑工具。
 * @author lighthu
 */
public class PassableTool implements IMapEditTool {
    // 附着的编辑器
    private MapViewer viewer;
    // 贴图选择影响的贴图查看器对象。当选中某个贴图块时会在贴图查看器中也选中。
    private MapTileSelector tileView;
    // 最近一次检测到的鼠标位置
    private int lastX = -1, lastY = -1;

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public PassableTool(MapViewer viewer, MapTileSelector tv) {
        this.viewer = viewer;
        tileView = tv;
    }

    // 根据坐标计算此坐标对应的格点坐标
    private void map2cell(Point pt) {
        pt.x /= viewer.getMap().parent.getTileWidth();
        pt.y /= viewer.getMap().parent.getTileHeight();
    }
    
    private void map2cell2(Point pt){
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
     * 鼠标按下事件。
     * 贴图查看器中当前选中贴图。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseDown(int x, int y, int mask) {
    }

    /**
     * 鼠标抬起事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask) {
        // 从顶层到底层检查点中的Tile
        GameMap map = viewer.getMap();
        for (int i = map.layers.size() - 1; i >= 0; i--) {
            if (!viewer.getShowLayers()[i]) {
                continue;
            }
            Point pt = new Point(x, y);
            if (!isValidCell(pt)) {
                continue;
            }
            IMapLayer layer = map.layers.get(i);
            if (layer instanceof AccurateMapLayer) {
                // 如果是地图层，检查是否点中某个Tile了
                AccurateMapLayer alayer = (AccurateMapLayer)layer;
                map2cell(pt);
                if (alayer.getLayerData()[pt.y][pt.x] != -1) {
                    int frame = alayer.getLayerData()[pt.y][pt.x];
                    TileInfo info = map.parent.getTileImage().tileInfo.get(frame);
                    info.unpassable = !info.unpassable;
                    viewer.fireContentChanged();
                    viewer.redraw();
                    tileView.redraw();
                    return;
                }
            } else if (layer instanceof BlurMapLayer){
                // 模糊地图层，检查点中的Tile
                BlurMapLayer alayer = (BlurMapLayer)layer;
                map2cell2(pt);
                int[] cellData = alayer.getMapData()[pt.y][pt.x];
                for (int j = cellData.length - 1; j >= 0; j--) {
                    if (cellData[j] != -1) {
                        int lfid = cellData[j] >> 16;
                        int tid = cellData[j] & 0xFF;
                        TileInfo info = map.parent.getLandforms().get(lfid).tileInfo.get(tid);
                        info.unpassable = !info.unpassable;
                        viewer.fireContentChanged();
                        viewer.redraw();
                        return;
                    }
                }
            }
        }
    }

    /**
     * 鼠标移动事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     */
    public void mouseMove(int x, int y) {
        lastX = x;
        lastY = y;
        viewer.redraw();
    }

    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc) {
        GameMap map = viewer.getMap();
        ArrayList<TileInfo> tiles = map.parent.getTileImage().tileInfo;
        int tw = map.parent.getTileWidth();
        int th = map.parent.getTileHeight();
        int btw = map.parent.getBlurTileWidth();
        int bth = map.parent.getBlurTileHeight();
        
        // 绘制所有地图层的通过性
        gc.setAlpha(0x80);
        gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        boolean isBlur = false;
        boolean afterGround = false;
        for (IMapLayer layer : map.layers) {
            if (layer instanceof AccurateMapLayer) {
                // 扫描贴图数据，不可通过的Tile画为黑色
                short[][] mapData = ((AccurateMapLayer)layer).getLayerData();
                for (int i = 0; i < mapData.length; i++) {
                    for (int j = 0; j < mapData[i].length; j++) {
                        int cc = mapData[i][j];
                        if (cc == -1) {
                            continue;
                        }
                        TileInfo cinfo = tiles.get(cc);
                        if (cinfo.unpassable) {
                            Rectangle rect = new Rectangle(j * tw, i * th, tw, th);
                            viewer.map2screen(rect);
                            gc.fillRectangle(rect);
                        }
                    }
                }
            } else if (layer instanceof MapNPCLayer) {
                if (afterGround) {
                    // 天空层不计算碰撞区域
                    continue;
                }
                if (layer == map.groundLayer) {
                    afterGround = true;
                }
                for (MapNPC npc : ((MapNPCLayer)layer).getNpcs()) {
                	long key = map.parent.getNPCKey(npc);
                    NPCImageInfo ninfo = map.parent.getNPCs().get(key);
                    if (ninfo == null) {
                        continue;
                    }
                    for (int i = 0; i < ninfo.cx.length; i++) {
                        Rectangle rect = new Rectangle(ninfo.cx[i], ninfo.cy[i], ninfo.cw[i], ninfo.ch[i]);
                        rect.x += npc.x;
                        rect.y += npc.y;
                        viewer.map2screen(rect);
                        gc.fillRectangle(rect);
                    }
                }
            } else if (layer instanceof BlurMapLayer) {
                int[][][] mapData = ((BlurMapLayer)layer).getMapData();
                for (int i = 0; i < mapData.length; i++) {
                    for (int j = 0; j < mapData[i].length; j++) {
                        int[] cc = mapData[i][j];
                        for (int k = cc.length - 1; k >= 0; k--) {
                            if (cc[k] != -1) {
                                int lfid = cc[k] >> 16;
                                int tid = cc[k] & 0xFF;
                                TileInfo cinfo = map.parent.getLandforms().get(lfid).tileInfo.get(tid);
                                if (cinfo.unpassable) {
                                    Rectangle rect = new Rectangle(j * btw, i * bth, btw, bth);
                                    viewer.map2screen(rect);
                                    gc.fillRectangle(rect);
                                }
                                break;
                            }
                        }
                    }
                }
                isBlur = true;
            }
        }
        
        // 绘制当前鼠标指向的位置
        Point pt = new Point(lastX, lastY);
        if (isValidCell(pt)) {
            Rectangle rect;
            if (isBlur) {
                map2cell2(pt);
                rect = new Rectangle(pt.x * btw, pt.y * bth, btw, bth);
            } else {
                map2cell(pt);
                rect = new Rectangle(pt.x * tw, pt.y * th, tw, th);
            }
            viewer.map2screen(rect);
            gc.fillRectangle(rect);
        }
        gc.setAlpha(0xFF);

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
