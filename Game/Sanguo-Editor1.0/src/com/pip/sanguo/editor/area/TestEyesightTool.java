package com.pip.sanguo.editor.area;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu;

import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pip.mapeditor.*;
import com.pip.mapeditor.data.*;
import com.pip.mapeditor.tool.IMapEditTool;
import com.pip.sanguo.data.map.*;
import com.pip.sanguo.editor.EditorPlugin;
import com.pipimage.image.*;
import com.swtdesigner.SWTResourceManager;

/**
 * 测试视线遮挡的算法。
 * @author lighthu
 */
public class TestEyesightTool implements IMapEditTool {
    // 父编辑器
    private GameAreaEditor editor;
    // 附着的编辑器
    private GameMapViewer viewer;
    // 是否正在拖动
    private boolean dragging;
    // 是否正在拖动起点
    private boolean draggingStartPos;
    // 路径的起点和终点
    private Point startPos, endPos;
    // 当前找出的路径的点
    private List<int[]> path;

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public TestEyesightTool(GameAreaEditor editor, GameMapViewer viewer) {
        this.editor = editor;
        this.viewer = viewer;
    }
    
    /*
     * 计算当前选中的起点和终点之间的路径，保存到成员变量path中。
     */
    protected void computePath() {
        if (startPos == null || endPos == null) {
            path = null;
            return;
        }
        path = viewer.getMapInfo().getEyesightPath(startPos.x, startPos.y, endPos.x, endPos.y);
//        long time1 = System.currentTimeMillis();
//        for (int i = 0; i < 100000; i++) {
//            viewer.getMapInfo().canSee(startPos.x, startPos.y, endPos.x, endPos.y, viewer.getMap());
//        }
//        System.out.println("Find Path Used: " + (System.currentTimeMillis() - time1) + "ms, 100000 times");
    }
    
    /**
     * 鼠标按下事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseDown(int x, int y, int mask) {
        Point pt = new Point(x, y);
        if (pt.x < 0) {
            pt.x = 0;
        }
        if (pt.x >= viewer.getMap().width) {
            pt.x = viewer.getMap().width - 1; 
        }
        if (pt.y < 0) {
            pt.y = 0;
        }
        if (pt.y >= viewer.getMap().height) {
            pt.y = viewer.getMap().height - 1;
        }
        if ((mask & SWT.SHIFT) != 0) {
            endPos = pt;
        } else {
            startPos = pt;
        }
        dragging = true;
        draggingStartPos = (mask & SWT.SHIFT) == 0; 
        computePath();
        viewer.redraw();
    }
    
    /**
     * 鼠标抬起事件。如果当前处于拖动NPC的状态，则这里确定这个NPC的最终坐标。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask) {
        dragging = false;
    }
    
    /**
     * 鼠标移动事件。被拖动的NPC跟随移动。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     */
    public void mouseMove(int x, int y) {
        if (dragging) {
            Point pt = new Point(x, y);
            if (pt.x < 0) {
                pt.x = 0;
            }
            if (pt.x >= viewer.getMap().width) {
                pt.x = viewer.getMap().width - 1; 
            }
            if (pt.y < 0) {
                pt.y = 0;
            }
            if (pt.y >= viewer.getMap().height) {
                pt.y = viewer.getMap().height - 1;
            }
            if (!draggingStartPos) {
                endPos = pt;
            } else {
                startPos = pt;
            }
            computePath();
            viewer.redraw();
        }
    }
    
    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc) {
        // 如果路径存在，用线条把路径点连接起来
        if (path != null) {
            for (int i = 0; i < path.size(); i++) {
                int[] point = path.get(i);
                Rectangle rect = new Rectangle(point[0] * 8, point[1] * 8, 8 , 8);
                viewer.map2screen(rect);
                if (viewer.getMap().eyesightBlock[point[1]][point[0]]) {
                    gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
                } else {
                    gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
                }
                gc.fillRectangle(rect);
            }
        }
        
        // 如果终点和起点存在，则在终点和起点位置花标记
        Image img = EditorPlugin.getDefault().getImageRegistry().get("flag");
        if (startPos != null) {
            Point pt = new Point(startPos.x, startPos.y);
            viewer.map2screen(pt);
            gc.drawImage(img, pt.x - 10, pt.y - 33);
        }
        if (endPos != null) {
            Point pt = new Point(endPos.x, endPos.y);
            viewer.map2screen(pt);
            gc.drawImage(img, pt.x - 10, pt.y - 33);
        }
    }
    
    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GLGraphics gc) {
        // 如果路径存在，用线条把路径点连接起来
        if (path != null) {
            for (int i = 0; i < path.size(); i++) {
                int[] point = path.get(i);
                Rectangle rect = new Rectangle(point[0] * 8, point[1] * 8, 8 , 8);
                viewer.map2screen(rect);
                if (viewer.getMap().eyesightBlock[point[1]][point[0]]) {
                    gc.setColor(SWTResourceManager.getColor(SWT.COLOR_RED));
                } else {
                    gc.setColor(SWTResourceManager.getColor(SWT.COLOR_BLACK));
                }
                gc.fillRect(rect);
            }
        }
        
        // 如果终点和起点存在，则在终点和起点位置花标记
        Image img = EditorPlugin.getDefault().getImageRegistry().get("flag");
        if (startPos != null) {
            Point pt = new Point(startPos.x, startPos.y);
            viewer.map2screen(pt);
            gc.drawTexture(GLUtils.loadImage(img), 0, 0, pt.x - 10, pt.y - 33);
        }
        if (endPos != null) {
            Point pt = new Point(endPos.x, endPos.y);
            viewer.map2screen(pt);
            gc.drawTexture(GLUtils.loadImage(img), 0, 0, pt.x - 10, pt.y - 33);
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

    public void mouseDoubleClick(int x, int y) {
        // TODO Auto-generated method stub
        
    }
}

