package com.pip.sanguo.editor.area;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;

import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pip.mapeditor.MapEditor;
import com.pip.mapeditor.MapViewer;
import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.MapFile;
import com.pip.mapeditor.tool.IMapEditTool;
import com.pip.sanguo.data.Faction;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pipimage.image.PipAnimate;
import com.swtdesigner.SWTResourceManager;

/**
 * 拾取工具。这个工具让用户可以选择当前显示的地图中的游戏地图元素，包括NPC和出口。还可以通过鼠标进行拖动。
 * @author lighthu
 */
public class GamePickupTool implements IMapEditTool {
    // 父编辑器
    private GameAreaEditor editor;
    // 附着的编辑器
    private GameMapViewer viewer;
    // 当前选中的游戏对象，null表示没有
    private GameMapObject selectedObject;
    // 是否正在拖动
    private boolean isDragging;
    // 是否按下shift拖动
    private boolean shiftDragging;
    // 拖动的起始点，以及拖动开始时被拖动NPC的位置
    private Point dragStartPoint, dragStartPos;
    // 最近一次检测到的鼠标位置
    private int lastX, lastY;
    // 正在移动NPC的按键
    private int movingKeyCode;
    // 重复按键次数
    private int repeatKeyCount;

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public GamePickupTool(GameAreaEditor editor, GameMapViewer viewer) {
        this.editor = editor;
        this.viewer = viewer;
    }
    
    /**
     * 清除选择。
     */
    public void clearSelection() {
        selectedObject = null;
        editor.setEditingObject(null);
    }
    
    // 检查某一位置的对象
    private GameMapObject detectObject(int x, int y) {
        for (GameMapObject obj : viewer.getMapInfo().objects) {
            if (getObjectBounds(obj).contains(x, y)) {
                return obj;
            }
        }
        return null;
    }
    
    // 取得一个地图对象的外框。
    private Rectangle getObjectBounds(GameMapObject obj) {
        if (obj instanceof GameMapExit) {
            GameMapExit exit = (GameMapExit)obj;
            Rectangle bounds = viewer.getExitIcon().getImageDraw(0).getBounds(0);
            bounds.x = exit.x - bounds.width / 2;
            bounds.y = exit.y - bounds.height / 2;
            return bounds;
        } else if (obj instanceof GameMapNPC) {
            GameMapNPC npc = (GameMapNPC)obj;
            Rectangle bounds = viewer.getCachedNPCImage(npc).getAnimate(0).getBounds();
            bounds.x += npc.x;
            bounds.y += npc.y;
            return bounds;
        }
        return new Rectangle(0, 0, 0, 0);
    }
    
    /**
     * 鼠标按下事件。
     * 选择工具从最顶上的地图层开始向下扫描，如果选中了某个NPC，开始拖动这个NPC；如果选中了某个贴图，则更新
     * 贴图查看器中当前选中贴图。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseDown(int x, int y, int mask) {
        isDragging = false;
        shiftDragging = false;
        selectedObject = detectObject(x, y);
        if (selectedObject != null) {
            // 如果按下SHIFT拖动NPC，则复制NPC
            if (selectedObject instanceof GameMapNPC && (mask & SWT.SHIFT) != 0) {
                GameMapNPC newnpc = new GameMapNPC();
                GameMapNPC oldnpc = (GameMapNPC)selectedObject;
                newnpc.owner = oldnpc.owner;
                newnpc.id = 0;
                while (true) {
                    // 确保ID不重复
                    boolean dup = false;
                    for (int i = viewer.getMapInfo().objects.size() - 1; i >= 0; i--) {
                        if (viewer.getMapInfo().objects.get(i).id == newnpc.id) {
                            dup = true;
                            break;
                        }
                    }
                    if (dup) {
                        newnpc.id++;
                    } else {
                        break;
                    }
                }
                newnpc.x = oldnpc.x;
                newnpc.y = oldnpc.y;
                newnpc.template = oldnpc.template;
                newnpc.name = oldnpc.name;
                newnpc.faction = oldnpc.faction;
                newnpc.visible = oldnpc.visible;
                newnpc.canAttack = oldnpc.canAttack;
                newnpc.refreshInterval = oldnpc.refreshInterval;
                newnpc.dynamicRefresh = oldnpc.dynamicRefresh;
                newnpc.linkDistance = oldnpc.linkDistance;
                newnpc.isGuard = oldnpc.isGuard;
                newnpc.isStatic = oldnpc.isStatic;
                for (int[] pt : oldnpc.patrolPath) {
                    newnpc.patrolPath.add(new int[] { pt[0], pt[1] });;
                }
                newnpc.canPass = oldnpc.canPass;
                newnpc.isFunctional = oldnpc.isFunctional;
                newnpc.functionName = oldnpc.functionName;
                newnpc.functionScript = oldnpc.functionScript;
                selectedObject = newnpc;
                shiftDragging = true;
            }
            isDragging = true;
            dragStartPoint = new Point(x, y);
            dragStartPos = new Point(selectedObject.x, selectedObject.y);
            
            // 从对象列表中临时删除，放开鼠标时再加入
            for (int i = viewer.getMapInfo().objects.size() - 1; i >= 0; i--) {
                if (viewer.getMapInfo().objects.get(i) == selectedObject) {
                    viewer.getMapInfo().objects.remove(i);
                    break;
                }
            }
            
            viewer.redraw();
        }
    }
    
    /**
     * 鼠标抬起事件。如果当前处于拖动NPC的状态，则这里确定这个NPC的最终坐标。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask) {
        if (isDragging) {
            isDragging = false;
            selectedObject.x = dragStartPos.x + x - dragStartPoint.x;
            selectedObject.y = dragStartPos.y + y - dragStartPoint.y;
            normalize(selectedObject);
            if (!shiftDragging) {
                viewer.getMapInfo().objects.add(selectedObject);
                if (x != dragStartPoint.x || y != dragStartPoint.y) {
                    viewer.fireContentChanged();
                    viewer.redraw();
                } else {
                    viewer.redraw();
                }
            } else if ((mask & SWT.SHIFT) != 0) {
                if (x != dragStartPoint.x || y != dragStartPoint.y) {
                    viewer.getMapInfo().objects.add(selectedObject);
                    viewer.fireContentChanged();
                    viewer.redraw();
                } else {
                    selectedObject = null;
                    viewer.redraw();
                }
            } else {
                selectedObject = null;
                viewer.redraw();
            }
        }
        editor.setEditingObject(selectedObject);
    }
    
    /**
     * 鼠标移动事件。被拖动的NPC跟随移动。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     */
    public void mouseMove(int x, int y) {
        lastX = x;
        lastY = y;
        if (isDragging) {
            selectedObject.x = dragStartPos.x + x - dragStartPoint.x;
            selectedObject.y = dragStartPos.y + y - dragStartPoint.y;
            normalize(selectedObject);
        }
        viewer.redraw();
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
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc) {
        GameMap map = viewer.getMap();
        if (isDragging || movingKeyCode != 0) {
            // 绘制被拖动的对象
            if (selectedObject instanceof GameMapNPC) {
                GameMapNPC npc = (GameMapNPC)selectedObject;
                PipAnimate animate = viewer.getCachedNPCImage(npc).getAnimate(0);
                int frame = animate.getFrameAtTime(viewer.getCurrentTime());
                Point pt = new Point(npc.x, npc.y);
                viewer.map2screen(pt);
                animate.drawFrame(gc, frame, pt.x, pt.y, viewer.getRatio());
                
                //绘制视野范围
                Rectangle eyeShot = viewer.getEyeShot(npc);
                viewer.map2screen(eyeShot);
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                gc.drawRectangle(eyeShot);
                //绘追击范围
                Rectangle chaseDis = viewer.getChaseDistance(npc);
                viewer.map2screen(chaseDis);
                if(chaseDis.equals(eyeShot)){
                    chaseDis = new Rectangle(eyeShot.x - 1, eyeShot.y - 1, eyeShot.width + 2, eyeShot.height + 2);
                }
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
                gc.drawRectangle(chaseDis);
            } else if (selectedObject instanceof GameMapExit) {
                GameMapExit exit = (GameMapExit)selectedObject;
                Rectangle imgSize = viewer.getExitIcon().getImageDraw(0).getBounds(0);
                int ow = imgSize.width;
                int oh = imgSize.height;
                imgSize.x = exit.x - imgSize.width / 2;
                imgSize.y = exit.y - imgSize.height / 2;
                viewer.map2screen(imgSize);
                int frame = (viewer.getCurrentTime() % 10) / 5;
                Image img = viewer.getExitIcon().getImageDraw(frame).createSWTImage(gc.getDevice(), 0);
                gc.drawImage(img, 0, 0, ow, oh, imgSize.x, imgSize.y, imgSize.width, imgSize.height);
                img.dispose();
            }
            
            // 绘制NPC外框
            Rectangle bounds = getObjectBounds(selectedObject);
            viewer.map2screen(bounds);
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
            gc.drawRectangle(bounds);
        } else {
            // 绘制选中的NPC的外框
            if (selectedObject != null) {
                Rectangle bounds = getObjectBounds(selectedObject);
                viewer.map2screen(bounds);
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
                gc.drawRectangle(bounds);
                
                if(selectedObject instanceof GameMapNPC){
                    GameMapNPC npc = (GameMapNPC)selectedObject;
                    
                    //绘制视野范围
                    Rectangle eyeShot = viewer.getEyeShot(npc);
                    viewer.map2screen(eyeShot);
                    gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                    gc.drawRectangle(eyeShot);
                    //绘追击范围
                    Rectangle chaseDis = viewer.getChaseDistance(npc);
                    viewer.map2screen(chaseDis);
                    if(chaseDis.equals(eyeShot)){
                        chaseDis = new Rectangle(eyeShot.x - 1, eyeShot.y - 1, eyeShot.width + 2, eyeShot.height + 2);
                    }
                    gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
                    gc.drawRectangle(chaseDis);
                }
            }
            
            // 查找当前鼠标位置上的对象，并画框
            GameMapObject clickedObject = detectObject(lastX, lastY);
            Rectangle bounds = null;
            if (clickedObject == null) {
                // 啥都没点中，显示一个Tile大小的框
//                int cx = lastX / map.parent.getTileWidth();
//                int cy = lastY / map.parent.getTileHeight();
//                int tw = map.parent.getTileWidth();
//                int th = map.parent.getTileHeight();
//                bounds = new Rectangle(cx * tw, cy * th, tw, th);
            } else {
                // 点中NPC
                bounds = getObjectBounds(clickedObject);
                viewer.map2screen(bounds);
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
                gc.drawRectangle(bounds);
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
     * 绘制当前工具
     * @param gc
     */
    public void draw(GLGraphics gc) {
        GameMap map = viewer.getMap();
        if (isDragging || movingKeyCode != 0) {
            // 绘制被拖动的对象
            if (selectedObject instanceof GameMapNPC) {
                GameMapNPC npc = (GameMapNPC)selectedObject;
                PipAnimate animate = viewer.getCachedNPCImage(npc).getAnimate(0);
                int frame = animate.getFrameAtTime(viewer.getCurrentTime());
                Point pt = new Point(npc.x, npc.y);
                viewer.map2screen(pt);
                animate.drawFrame(gc, frame, pt.x, pt.y, viewer.getRatio(), MapEditor.imageCache);
                
                //绘制视野范围
                Rectangle eyeShot = viewer.getEyeShot(npc);
                viewer.map2screen(eyeShot);
                gc.setColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                gc.drawRect(eyeShot);
                //绘追击范围
                Rectangle chaseDis = viewer.getChaseDistance(npc);
                viewer.map2screen(chaseDis);
                if(chaseDis.equals(eyeShot)){
                    chaseDis = new Rectangle(eyeShot.x - 1, eyeShot.y - 1, eyeShot.width + 2, eyeShot.height + 2);
                }
                gc.setColor(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
                gc.drawRect(chaseDis);
            } else if (selectedObject instanceof GameMapExit) {
                GameMapExit exit = (GameMapExit)selectedObject;
                Rectangle imgSize = viewer.getExitIcon().getImageDraw(0).getBounds(0);
                int ow = imgSize.width;
                int oh = imgSize.height;
                imgSize.x = exit.x - imgSize.width / 2;
                imgSize.y = exit.y - imgSize.height / 2;
                viewer.map2screen(imgSize);
                int frame = (viewer.getCurrentTime() % 10) / 5;
                Image img = MapEditor.imageCache.get(viewer.getExitIcon(), frame, 0);
                if (img == null) {
                    img = viewer.getExitIcon().getImageDraw(frame).createSWTImage(null, 0);
                    MapEditor.imageCache.add(viewer.getExitIcon(), frame, 0, img);
                }
                gc.drawTexture(GLUtils.loadImage(img), 0, 0, imgSize.x, imgSize.y, imgSize.width, imgSize.height);
            }
            
            // 绘制NPC外框
            Rectangle bounds = getObjectBounds(selectedObject);
            viewer.map2screen(bounds);
            gc.setColor(SWTResourceManager.getColor(SWT.COLOR_RED));
            gc.drawRect(bounds);
        } else {
            // 绘制选中的NPC的外框
            if (selectedObject != null) {
                Rectangle bounds = getObjectBounds(selectedObject);
                viewer.map2screen(bounds);
                gc.setColor(SWTResourceManager.getColor(SWT.COLOR_BLUE));
                gc.drawRect(bounds);
                
                if(selectedObject instanceof GameMapNPC){
                    GameMapNPC npc = (GameMapNPC)selectedObject;
                    
                    //绘制视野范围
                    Rectangle eyeShot = viewer.getEyeShot(npc);
                    viewer.map2screen(eyeShot);
                    gc.setColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                    gc.drawRect(eyeShot);
                    //绘追击范围
                    Rectangle chaseDis = viewer.getChaseDistance(npc);
                    viewer.map2screen(chaseDis);
                    if(chaseDis.equals(eyeShot)){
                        chaseDis = new Rectangle(eyeShot.x - 1, eyeShot.y - 1, eyeShot.width + 2, eyeShot.height + 2);
                    }
                    gc.setColor(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
                    gc.drawRect(chaseDis);
                }
            }
            
            // 查找当前鼠标位置上的对象，并画框
            GameMapObject clickedObject = detectObject(lastX, lastY);
            Rectangle bounds = null;
            if (clickedObject == null) {
                // 啥都没点中，显示一个Tile大小的框
//                int cx = lastX / map.parent.getTileWidth();
//                int cy = lastY / map.parent.getTileHeight();
//                int tw = map.parent.getTileWidth();
//                int th = map.parent.getTileHeight();
//                bounds = new Rectangle(cx * tw, cy * th, tw, th);
            } else {
                // 点中NPC
                bounds = getObjectBounds(clickedObject);
                viewer.map2screen(bounds);
                gc.setColor(SWTResourceManager.getColor(SWT.COLOR_RED));
                gc.drawRect(bounds);
            }
        }

        // 绘制座标
        String coordStr = lastX + "," + lastY + "," + (lastX / map.parent.getCellSize()) + "," + (lastY / map.parent.getCellSize());
        Point size = viewer.getSize();
        Point ts = gc.textExtent(coordStr);
        gc.setColor(MapViewer.invert(viewer.getBackground()));
        gc.drawRect(size.x - ts.x - 9, size.y - ts.y - 8, ts.x + 7, ts.y + 6);
        gc.drawText(coordStr, size.x - ts.x - 5, size.y - ts.y - 5);
    }
    
    /*
     * 查找当前选中的对象在对象列表中的索引
     */
    private int getSelectedObjectIndex() {
        for (int i = viewer.getMapInfo().objects.size() - 1; i >= 0; i--) {
            if (viewer.getMapInfo().objects.get(i) == selectedObject) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 键按下事件。响应：删除，上下左右
     */
    public void onKeyDown(int keyCode) {
        if (!isDragging && selectedObject != null) {
            if (movingKeyCode != 0 && keyCode != movingKeyCode) {
                // 如果正在移动NPC，则忽略其他按键
                return;
            }
            if (keyCode == SWT.DEL) {
                // 删除键，删除选中NPC
                int index = getSelectedObjectIndex();
                viewer.getMapInfo().objects.remove(index);
                selectedObject = null;
                editor.setEditingObject(null);
                viewer.fireContentChanged();
                viewer.refresh();
            } else if (keyCode == SWT.ARROW_UP || keyCode == SWT.ARROW_DOWN || 
                    keyCode == SWT.ARROW_LEFT || keyCode == SWT.ARROW_RIGHT) {
                boolean isStart = false;
                int step;
                if (movingKeyCode == 0) {
                    isStart = true;
                    repeatKeyCount = 1;
                    step = 1;
                } else {
                    repeatKeyCount++;
                    step = repeatKeyCount / 3 + 1;
                }
                movingKeyCode = keyCode;
                switch (keyCode) {
                case SWT.ARROW_UP:
                    selectedObject.y -= step;
                    break;
                case SWT.ARROW_DOWN:
                    selectedObject.y += step;
                    break;
                case SWT.ARROW_LEFT:
                    selectedObject.x -= step;
                    break;
                case SWT.ARROW_RIGHT:
                    selectedObject.x += step;
                    break;
                }
                normalize(selectedObject);
                
                // 暂时把选中NPC从列表中删除
                if (isStart) {
                    int index = getSelectedObjectIndex();
                    viewer.getMapInfo().objects.remove(index);
                }
                viewer.refresh();
            }
        }
    }

    /**
     * 键松开事件。
     */
    public void onKeyUp(int keyCode) {
        if (movingKeyCode != 0 && keyCode == movingKeyCode) {
            movingKeyCode = 0;
            viewer.getMapInfo().objects.add(selectedObject);
            viewer.fireContentChanged();
            viewer.refresh();
        }
    }

    /**
     * 得到工具右键菜单。
     */
    public Menu getMenu() {
        return null;
    }
}
