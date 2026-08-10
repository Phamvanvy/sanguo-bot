package com.pip.mapeditor.tool;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu;

import com.pip.mapeditor.*;
import com.pip.mapeditor.data.*;
import com.pipimage.image.*;
import com.swtdesigner.SWTResourceManager;

/**
 * 创建NPC工具。这个工具根据在NPC查看器中选中的NPC图片，在地图中创建NPC。
 * @author lighthu
 */
public class MapNPCTool implements IMapEditTool {
    // 附着的编辑器
    private MapViewer viewer;
    // NPC查看器
    private MapNpcSelector npcView;
    // 最近一次检测到的鼠标位置
    private int lastX, lastY;

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public MapNPCTool(MapViewer viewer, MapNpcSelector tv) {
        this.viewer = viewer;
        npcView = tv;
    }

    // 判断一个坐标是否可以放置当前选中的NPC。必须保证NPC有一部分在屏幕内。
    private boolean isValidPos(Point pt) {
        GameMap map = viewer.getMap();
        PipAnimate animate = map.parent.getAnimates().getAnimate(npcView.getSelectedFrame());
        Rectangle bounds = animate.getBounds();
        bounds.x += pt.x;
        bounds.y += pt.y;
        if (bounds.x + bounds.width - 3 < 0) {
            return false;
        } else if (bounds.x - map.width + 3 > 0) {
            return false;
        }
        if (bounds.y + bounds.height - 3 < 0) {
            return false;
        } else if (bounds.y - map.height + 3 > 0) {
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
    }

    /**
     * 鼠标抬起事件。拖动绘制结束，把拖动过的点都设置为选中的Tile。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask) {
        if (npcView.getSelectedFrame() == -1) {
            return;
        }
        Point pt = new Point(x, y);
        if (isValidPos(pt)) {
            // 创建一个新NPC
            MapNPCLayer nlayer = getCurrentLayer();
            if (nlayer == null) {
                return;
            }
            MapNPC npc = new MapNPC();
            npc.animate = npcView.getSelectedFrame();
            if(nlayer.parent.parent.isLibMode){
            	int[] ref = nlayer.parent.parent.getNpcAnimateRef(npc.animate);
            	npc.animateSetRef = ref[0];
            	npc.animate = ref[1];
            }
            npc.x = x;
            npc.y = y;
            nlayer.getNpcs().add(npc);
            viewer.fireContentChanged();
            viewer.makeDirty(npc);
            viewer.redraw();
        }
    }

    // 取得绘制的目标地图层。如果当前选中的层是NPC图层，则就在这层上绘制；如果地图指定
    // 了地面层，且地面层是NPC图层，则在地面层绘制；否则在最顶层的NPC层绘制。
    private MapNPCLayer getCurrentLayer() {
        int activeLayer = viewer.getActiveLayer();
        if (activeLayer != -1) {
            IMapLayer layer = viewer.getMap().layers.get(activeLayer);
            if (layer instanceof MapNPCLayer) {
                return (MapNPCLayer)layer;
            }
        }
        if (viewer.getMap().groundLayer != null && viewer.getMap().groundLayer instanceof MapNPCLayer) {
            return (MapNPCLayer)viewer.getMap().groundLayer;
        }
        ArrayList<IMapLayer> layers = viewer.getMap().layers;
        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i) instanceof MapNPCLayer) {
                return (MapNPCLayer)layers.get(i);
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
        viewer.redraw();
    }

    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc) {
        GameMap map = viewer.getMap();
        int index = npcView.getSelectedFrame();
        if (index == -1) {
            return;
        }
        
        // 绘制NPC
        PipAnimate animate = map.parent.getAnimates().getAnimate(index);
        Point pt = new Point(lastX, lastY);
        viewer.map2screen(pt);
        int frame = animate.getFrameAtTime(viewer.getCurrentTime());
        animate.drawFrame(gc, frame, pt.x, pt.y, viewer.getRatio());
        
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
