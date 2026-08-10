package com.pip.sanguo.editor.area;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu;

import com.pip.mapeditor.*;
import com.pip.mapeditor.data.*;
import com.pip.mapeditor.tool.IMapEditTool;
import com.pip.sanguo.data.map.*;
import com.pip.sanguo.editor.EditorPlugin;
import com.pipimage.image.*;
import com.swtdesigner.SWTResourceManager;

/**
 * 测试怪物寻路算法的工具。
 * @author lighthu
 */
public class PathFinderTool implements IMapEditTool {
    // 父编辑器
    private GameAreaEditor editor;
    // 附着的编辑器
    private GameMapViewer viewer;
    // 路径查找工具
    private PathFinder pathFinder;
    // 是否正在拖动
    private boolean dragging;
    // 是否正在拖动起点
    private boolean draggingStartPos;
    // 路径的起点和终点
    private Point startPos, endPos;
    // 当前找出的路径的点
    private List<Point> path;

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public PathFinderTool(GameAreaEditor editor, GameMapViewer viewer) {
        this.editor = editor;
        this.viewer = viewer;
        long time1 = System.currentTimeMillis();
        try {
            pathFinder = new PathFinder(viewer.getMapInfo(), viewer.getMap());
            pathFinder.buildPathBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("Create PathFinder: " + (System.currentTimeMillis() - time1) + "ms");
    }
    
    /*
     * 计算当前选中的起点和终点之间的路径，保存到成员变量path中。
     */
    protected void computePath() {
        if (startPos == null || endPos == null) {
            path = null;
            return;
        }
        long time1 = System.currentTimeMillis();
//        for (int i = 0; i < 10000; i++) {
            path = new ArrayList<Point>();
            int curx = startPos.x;
            int cury = startPos.y;
            path.add(new Point(curx, cury));
            int xdist = Math.abs(curx - endPos.x);
            int ydist = Math.abs(cury - endPos.y);
            while (path.size() < 100 && (xdist + ydist) > 16) {
                int[] ret = pathFinder.findPath(curx, cury, endPos.x, endPos.y);
                curx = ret[0];
                cury = ret[1];
                path.add(new Point(curx, cury));
                xdist = Math.abs(curx - endPos.x);
                ydist = Math.abs(cury - endPos.y);
            }
//        }
//        System.out.println("Find Path: " + (System.currentTimeMillis() - time1) + "ms, " + (path.size() - 1) * 10000 + " times");
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
            for (int i = 0; i < path.size() - 1; i++) {
                Point point1 = path.get(i);
                Point point2 = path.get(i + 1);
                
                Rectangle rect = new Rectangle(point1.x - 2, point1.y - 2, 5, 5);
                viewer.map2screen(rect);
                gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
                gc.fillRectangle(rect);
                
                Point pt1 = new Point(point1.x, point1.y);
                Point pt2 = new Point(point2.x, point2.y);
                viewer.map2screen(pt1);
                viewer.map2screen(pt2);
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
                gc.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
            }
        }
        
        // 如果终点和起点存在，则在终点和起点位置花标记
        Image img = EditorPlugin.getDefault().getImageRegistry().get("flag");
        if (startPos != null) {
            Point pt = new Point(startPos.x, startPos.y);
            viewer.map2screen(pt);
            gc.drawImage(img, pt.x - 10, pt.y - 33);
            
            int[] pos = pathFinder.tryOutPrison(startPos.x, startPos.y);
            if (pos != null) {
                pt = new Point(pos[0], pos[1]);
                viewer.map2screen(pt);
                gc.drawImage(img, pt.x - 10, pt.y - 33);
            }
        }
        if (endPos != null) {
            Point pt = new Point(endPos.x, endPos.y);
            viewer.map2screen(pt);
            gc.drawImage(img, pt.x - 10, pt.y - 33);
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

