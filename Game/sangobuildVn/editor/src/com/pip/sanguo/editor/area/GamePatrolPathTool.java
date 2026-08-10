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
 * 巡逻路径编辑工具。
 * @author lighthu
 */
public class GamePatrolPathTool implements IMapEditTool {
    // 父编辑器
    private GameAreaEditor editor;
    // 附着的编辑器
    private GameMapViewer viewer;
    // 当前选中的游戏对象，null表示没有
    private GameMapNPC selectedObject;
    // 是否正在拖动路点
    private boolean isDragging;
    // 正在拖动的路点在当前选中游戏对象的运动路径中的索引，-1表示拖动本体（创建新路点），-2表示拖动NPC
    private int draggingPointIndex;
    // 拖动的起始点，以及拖动开始时被拖动路点的位置
    private Point dragStartPoint, dragStartPos;
    // 最近一次检测到的鼠标位置
    private int lastX, lastY;

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public GamePatrolPathTool(GameAreaEditor editor, GameMapViewer viewer) {
        this.editor = editor;
        this.viewer = viewer;
    }
    
    /**
     * 鼠标按下事件。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseDown(int x, int y, int mask) {
        isDragging = false;
        lastX = x;
        lastY = y;
        
        // 先判断是否点中了某个路点
        if (selectedObject != null) {
            int pointIndex = detectPoint(x, y);
            if (pointIndex != -3) {
                isDragging = true;
                draggingPointIndex = pointIndex;
                dragStartPoint = new Point(x, y);
                if (pointIndex == -1 || pointIndex == -2) {
                    dragStartPos = new Point(selectedObject.x, selectedObject.y);
                } else {
                    int[] roadPoint = selectedObject.patrolPath.get(pointIndex);
                    dragStartPos = new Point(roadPoint[0] + selectedObject.x, roadPoint[1] + selectedObject.y);
                }
                if (pointIndex == -2) {
                    // 从对象列表中临时删除，放开鼠标时再加入
                    for (int i = viewer.getMapInfo().objects.size() - 1; i >= 0; i--) {
                        if (viewer.getMapInfo().objects.get(i) == selectedObject) {
                            viewer.getMapInfo().objects.remove(i);
                            break;
                        }
                    }
                }
                viewer.redraw();
                return;
            }
        }
        
        // 如果没有点中路点，则判断是否点中了某个NPC
        selectedObject = detectObject(x, y);
        viewer.redraw();
    }
    
    // 检查两个点是否在点击误差范围内
    private boolean isMatchPoint(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) < 3 && Math.abs(y1 - y2) < 3;
    }
    
    // 检查某一位置的路点
    private int detectPoint(int x, int y) {
        // 判断是否点中本体
        if (isMatchPoint(x, y, selectedObject.x, selectedObject.y)) {
            return -1;
        }
        
        // 依次判断每个路点是否被点中
        for (int i = 0; i < selectedObject.patrolPath.size(); i++) {
            int[] roadPoint = selectedObject.patrolPath.get(i);
            if (isMatchPoint(x, y, roadPoint[0] + selectedObject.x, roadPoint[1] + selectedObject.y)) {
                return i;
            }
        }
        
        // 判断是否点中选中的NPC
        if (getObjectBounds(selectedObject).contains(x, y)) {
            return -2;
        }
        return -3;
    }
    
    // 检查某一位置的对象
    private GameMapNPC detectObject(int x, int y) {
        for (GameMapObject obj : viewer.getMapInfo().objects) {
            if (obj instanceof GameMapNPC && getObjectBounds(obj).contains(x, y)) {
                return (GameMapNPC)obj;
            }
        }
        return null;
    }
    
    // 取得一个地图对象的外框。
    private Rectangle getObjectBounds(GameMapObject obj) {
        if (obj instanceof GameMapNPC) {
            GameMapNPC npc = (GameMapNPC)obj;
            Rectangle bounds = viewer.getCachedNPCImage(npc).getAnimate(0).getBounds();
            bounds.x += npc.x;
            bounds.y += npc.y;
            return bounds;
        }
        return new Rectangle(0, 0, 0, 0);
    }

    // 调整NPC位置以保证NPC有一部分在屏幕内。
    private void normalize(GameMapObject obj) {
        GameMap map = viewer.getMap();
        Rectangle bounds = this.getObjectBounds(obj);
        if (bounds.x + bounds.width - 3 < 0) {
            obj.x += 3 - bounds.x - bounds.width;
        } else if (bounds.x - map.width + 3 > 0) {
            obj.x -= bounds.x - map.width + 3;
        }
        if (bounds.y + bounds.height - 3 < 0) {
            obj.y += 3 - bounds.y - bounds.height;
        } else if (bounds.y - map.height + 3 > 0) {
            obj.y -= bounds.y - map.height + 3;
        }
    }
    
    /**
     * 鼠标抬起事件。如果当前处于拖动NPC的状态，则这里确定这个NPC的最终坐标。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask) {
        lastX = x;
        lastY = y;
        if (isDragging) {
            isDragging = false;
            int newx = dragStartPos.x + x - dragStartPoint.x - selectedObject.x;
            int newy = dragStartPos.y + y - dragStartPoint.y - selectedObject.y;
            
            boolean modified = false;
            if (draggingPointIndex == -2) {
                // 如果拖动的是NPC本身，调整NPC位置后重新加入
                moveNPC(selectedObject, dragStartPos.x + x - dragStartPoint.x, dragStartPos.y + y - dragStartPoint.y);
                viewer.getMapInfo().objects.add(selectedObject);
                modified = true;
            } else if (draggingPointIndex == -1) {
                // 如果拖动的是本体，并且新的位置不在本体范围内，则增加一个新的路点
                if (!isMatchPoint(newx, newy, 0, 0)) {
                    selectedObject.patrolPath.add(new int[] { newx, newy });
                    modified = true;
                }
            } else {
                // 如果拖动的是普通路点，则修改路点位置。如果路点的新位置在本体范围内，则删除这个路点
                int[] oldPoint = selectedObject.patrolPath.get(draggingPointIndex);
                if (isMatchPoint(newx, newy, 0, 0)) {
                    selectedObject.patrolPath.remove(draggingPointIndex);
                    modified = true;
                } else if (oldPoint[0] != newx || oldPoint[1] != newy) {
                    oldPoint[0] = newx;
                    oldPoint[1] = newy;
                    modified = true;
                }
            }
            if (modified) {
                viewer.fireContentChanged();
            }
            viewer.redraw();
        }
    }
    
    /**
     * 鼠标移动事件。被拖动的NPC跟随移动。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     */
    public void mouseMove(int x, int y) {
        lastX = x;
        lastY = y;
        if (isDragging && draggingPointIndex == -2) {
            moveNPC(selectedObject, dragStartPos.x + x - dragStartPoint.x, dragStartPos.y + y - dragStartPoint.y);
        }
        viewer.redraw();
    }
    
    /**
     * 移动NPC位置，但保持路点位置不变。
     */
    private void moveNPC(GameMapNPC npc, int x, int y) {
        int oldx = npc.x;
        int oldy = npc.y;
        npc.x = x;
        npc.y = y;
        normalize(npc);
        int xoff = x - oldx;
        int yoff = y - oldy;
        for (int[] point : npc.patrolPath) {
            point[0] -= xoff;
            point[1] -= yoff;
        }
    }
    
    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc) {
        GameMap map = viewer.getMap();

        if (selectedObject != null) {
            // 如果正在拖动NPC，先绘制次选中NPC
            if (isDragging && draggingPointIndex == -2) {
                PipAnimate animate = viewer.getCachedNPCImage(selectedObject).getAnimate(0);
                int frame = animate.getFrameAtTime(viewer.getCurrentTime());
                Point pt = new Point(selectedObject.x, selectedObject.y);
                viewer.map2screen(pt);
                animate.drawFrame(gc, frame, pt.x, pt.y, viewer.getRatio());
            }
            
            // 绘制选中的NPC的外框
            Rectangle bounds = getObjectBounds(selectedObject);
            viewer.map2screen(bounds);
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
            gc.drawRectangle(bounds);
            
            // 绘制选中的NPC的巡逻路径
            List<int[]> path = new ArrayList<int[]>();
            path.add(new int[] { 0, 0 });
            path.addAll(selectedObject.patrolPath);
            path.add(new int[] { 0, 0 });
            int baseX = selectedObject.x;
            int baseY = selectedObject.y;
            for (int i = 0; i < path.size() - 1; i++) {
                int[] point1 = path.get(i);
                int[] point2 = path.get(i + 1);
                
                Rectangle rect = new Rectangle(point1[0] - 2 + baseX, point1[1] - 2 + baseY, 5, 5);
                viewer.map2screen(rect);
                gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
                gc.fillRectangle(rect);
                
                Point pt1 = new Point(point1[0] + baseX, point1[1] + baseY);
                Point pt2 = new Point(point2[0] + baseX, point2[1] + baseY);
                viewer.map2screen(pt1);
                viewer.map2screen(pt2);
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
                gc.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
            }
        }
        
        
        if (isDragging) {
            // 绘制当前拖动位置
            int newx = dragStartPos.x + lastX - dragStartPoint.x;
            int newy = dragStartPos.y + lastY - dragStartPoint.y;
            Rectangle rect = new Rectangle(newx - 2, newy - 2, 5, 5);
            viewer.map2screen(rect);
            gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
            gc.fillRectangle(rect);
        } else {
            // 没有在拖动状态时，高亮绘制当前鼠标位置上的对象
            
            // 先判断是否点中了某个路点
            Point pt = null;
            if (selectedObject != null) {
                int pointIndex = detectPoint(lastX, lastY);
                if (pointIndex != -2 && pointIndex != -3) {
                    if (pointIndex == -1) {
                        pt = new Point(selectedObject.x, selectedObject.y);
                    } else {
                        int[] roadPoint = selectedObject.patrolPath.get(pointIndex);
                        pt = new Point(roadPoint[0] + selectedObject.x, roadPoint[1] + selectedObject.y);
                    }
                }
            }
            if (pt != null) {
                Rectangle rect = new Rectangle(pt.x - 2, pt.y - 2, 5, 5);
                viewer.map2screen(rect);
                gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
                gc.fillRectangle(rect);
            } else {
                // 如果没有点中路点，则判断是否点中了某个NPC
                GameMapNPC npc = detectObject(lastX, lastY);
                if (npc != null) {
                    // 绘制NPC外框
                    Rectangle bounds = getObjectBounds(npc);
                    viewer.map2screen(bounds);
                    gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
                    gc.drawRectangle(bounds);
                }
            }
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

