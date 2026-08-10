package com.pip.mapeditor.tool;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.pip.mapeditor.*;
import com.pip.mapeditor.data.*;
import com.pipimage.image.*;
import com.swtdesigner.SWTResourceManager;

/**
 * 拾取工具。这个工具让用户可以选择当前显示的地图元素，包括NPC和贴图。对于NPC，还可以通过鼠标进行拖动。
 * @author lighthu
 */
public class PickupTool implements IMapEditTool, SelectionListener {
    // 附着的编辑器
    private MapViewer viewer;
    // 贴图选择影响的贴图查看器对象。当选中某个贴图块时会在贴图查看器中也选中。
    private MapTileSelector tileView;
    // 当前选中的NPC，null表示没有
    private MapNPC selectedNPC;
    // 当前选中的NPC所在的地图层
    private MapNPCLayer selectedLayer;
    // 是否正在拖动
    private boolean isDragging;
    // 拖动的起始点，以及拖动开始时被拖动NPC的位置
    private Point dragStartPoint, dragStartPos;
    // 最近一次检测到的鼠标位置
    private int lastX, lastY;
    // 右键菜单
    private Menu popupMenu;
    // 正在移动NPC的按键
    private int movingKeyCode;
    // 重复按键次数
    private int repeatKeyCount;
    
    public MenuItem upOrderMove, downOrderMove, firstOrderMove, lastOrderMove;

    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public PickupTool(MapViewer viewer, MapTileSelector tv) {
        this.viewer = viewer;
        tileView = tv;
    }
    
    /**
     * 清除选择。
     */
    public void clearSelection() {
        selectedNPC = null;
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
        selectedNPC = null;
        
        Object[] clickedObject = detectObject(x, y);
        if (clickedObject == null) {
        	viewer.setMenu(null);
        } else if (clickedObject[0] instanceof MapNPCLayer) {
            // 点中NPC
            isDragging = true;
            selectedNPC = (MapNPC)clickedObject[1];
            selectedLayer = (MapNPCLayer)clickedObject[0];
            updateMenu();
            viewer.setMenu(popupMenu);
            dragStartPoint = new Point(x, y);
            dragStartPos = new Point(selectedNPC.x, selectedNPC.y);
            int index = (Integer)clickedObject[2];
//            selectedLayer.getNpcs().remove(index);
            
            selectedLayer.getNpcs().get(index).isShow = false;
            
            int npcIndex = selectedLayer.getNpcs().lastIndexOf(selectedNPC);
            upOrderMove.setEnabled(false);
            downOrderMove.setEnabled(false);
            if(npcIndex > 0) {
            	upOrderMove.setEnabled(selectedLayer.getForceAddOrderDraw());            	
            }
            if(npcIndex < selectedLayer.getNpcs().size() - 1) {
            	downOrderMove.setEnabled(selectedLayer.getForceAddOrderDraw());            	
            }
            
            firstOrderMove.setEnabled(selectedLayer.getForceAddOrderDraw());
            lastOrderMove.setEnabled(selectedLayer.getForceAddOrderDraw());
            
            viewer.makeDirty(selectedLayer.getNpcs().get(index));
            viewer.redraw();
        } else if (clickedObject[0] instanceof AccurateMapLayer) {
            // 点中Tile
            int frame = ((Short)clickedObject[3]).intValue();
            if (frame != -1) {
                tileView.setSelectedFrame(frame + 1);
            }
            viewer.setMenu(null);
        }
    }
    
    // 检查某一位置的对象
    private Object[] detectObject(int x, int y) {
        // 从顶层到底层检查点中的目标
        GameMap map = viewer.getMap();
        for (int i = map.layers.size() - 1; i >= 0; i--) {
            if (!viewer.getShowLayers()[i]) {
                continue;
            }
            IMapLayer layer = map.layers.get(i);
            if (layer instanceof MapNPCLayer) {
                // 如果是NPC层，检查是否点中某个NPC
                MapNPCLayer nlayer = (MapNPCLayer)layer;
                ArrayList<MapNPC> npcs = nlayer.getNpcs();
                int count = npcs.size();
                for (int j = 0; j < count; j++) {
                    MapNPC npc = npcs.get(j);
                    if(npc instanceof MultiAnimNPC){
                    	continue;
                    }
                    PipAnimate npcAnimate;
                    if(map.parent.isLibMode){
                    	npcAnimate = map.parent.getAnimate(npc.animateSetRef, npc.animate);
                    }else{
                    	npcAnimate = map.parent.getAnimates().getAnimate(npc.animate);
                    }
                    Rectangle rect = npcAnimate.getBounds();
                    rect.x += npc.x;
                    rect.y += npc.y;
                    if (rect.contains(x, y)) {
                        // 点中了这个NPC了
                        return new Object[] { nlayer, npc, j };
                    }
                }
            } else if (layer instanceof AccurateMapLayer) {
                // 如果是地图层，检查是否点中某个Tile了
                AccurateMapLayer alayer = (AccurateMapLayer)layer;
                int cx = x / map.parent.getTileWidth();
                int cy = y / map.parent.getTileHeight();
                if (cx >= 0 && cx < alayer.getLayerData()[0].length && cy >= 0 && cy < alayer.getLayerData().length &&
                        alayer.getLayerData()[cy][cx] != -1) {
                    return new Object[] { alayer, cx, cy, alayer.getLayerData()[cy][cx] };
                }
            }
        }
        return null;
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
            selectedNPC.x = dragStartPos.x + x - dragStartPoint.x;
            selectedNPC.y = dragStartPos.y + y - dragStartPoint.y;
            normalize(selectedNPC);
//            selectedLayer.getNpcs().add(selectedNPC);
            selectedNPC.isShow = true;
            if (x != dragStartPoint.x || y != dragStartPoint.y) {
                viewer.fireContentChanged();
                viewer.makeDirty(selectedNPC);
                viewer.redraw();
            } else {
            	viewer.makeDirty(selectedNPC);
                viewer.redraw();
            }
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
        if (isDragging) {
            selectedNPC.x = dragStartPos.x + x - dragStartPoint.x;
            selectedNPC.y = dragStartPos.y + y - dragStartPoint.y;
            normalize(selectedNPC);
        }
        viewer.redraw();
    }
    
    // 调整NPC位置以保证NPC有一部分在屏幕内。
    private void normalize(MapNPC npc) {
        GameMap map = viewer.getMap();
        PipAnimate animate = map.parent.getAnimates().getAnimate(selectedNPC.animate);
        Rectangle bounds = animate.getBounds();
        bounds.x += npc.x;
        bounds.y += npc.y;
        if (bounds.x + bounds.width - 3 < 0) {
            npc.x += 3 - bounds.x - bounds.width;
        } else if (bounds.x - map.width + 3 > 0) {
            npc.x -= bounds.x - map.width + 3;
        }
        if (bounds.y + bounds.height - 3 < 0) {
            npc.y += 3 - bounds.y - bounds.height;
        } else if (bounds.y - map.height + 3 > 0) {
            npc.y -= bounds.y - map.height + 3;
        }
    }
    
    /**
     * 绘制当前工具
     * @param gc
     */
    public void draw(GC gc) {
        GameMap map = viewer.getMap();
        if (isDragging || movingKeyCode != 0) {
            // 绘制被拖动的NPC
            PipAnimate animate;
            if(map.parent.isLibMode){
            	animate = map.parent.getAnimate(selectedNPC.animateSetRef, selectedNPC.animate);
            }else{
            	animate = map.parent.getAnimates().getAnimate(selectedNPC.animate);
            }
            int frame = animate.getFrameAtTime(viewer.getCurrentTime());
            Point pt = new Point(selectedNPC.x, selectedNPC.y);
            viewer.map2screen(pt);
            animate.drawFrame(gc, frame, pt.x, pt.y, viewer.getRatio());
            
            // 绘制NPC外框
            Rectangle bounds = animate.getBounds();
            bounds.x += selectedNPC.x;
            bounds.y += selectedNPC.y;
            viewer.map2screen(bounds);
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
            gc.drawRectangle(bounds);
        } else {
            // 绘制选中的NPC的外框
            if (selectedNPC != null) {
                PipAnimate animate;
                if(map.parent.isLibMode){
                	animate = map.parent.getAnimate(selectedNPC.animateSetRef, selectedNPC.animate);
                }else{
                	animate = map.parent.getAnimates().getAnimate(selectedNPC.animate);
                }
                Rectangle bounds = animate.getBounds();
                bounds.x += selectedNPC.x;
                bounds.y += selectedNPC.y;
                viewer.map2screen(bounds);
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
                gc.drawRectangle(bounds);
            }
            
            // 查找当前鼠标位置上的对象，并画框
            Object[] clickedObject = detectObject(lastX, lastY);
            Rectangle bounds = null;
            if (clickedObject == null) {
                // 啥都没点中，显示一个Tile大小的框
                int cx = lastX / map.parent.getTileWidth();
                int cy = lastY / map.parent.getTileHeight();
                int tw = map.parent.getTileWidth();
                int th = map.parent.getTileHeight();
                bounds = new Rectangle(cx * tw, cy * th, tw, th);
            } else if (clickedObject[0] instanceof MapNPCLayer) {
                // 点中NPC
                MapNPC hoverNPC = (MapNPC)clickedObject[1];
                PipAnimate animate;
                if(map.parent.isLibMode){
                	animate = map.parent.getAnimate(hoverNPC.animateSetRef, hoverNPC.animate);
                }else{
                	animate = map.parent.getAnimates().getAnimate(hoverNPC.animate);
                }
                bounds = animate.getBounds();
                bounds.x += hoverNPC.x;
                bounds.y += hoverNPC.y;
            } else if (clickedObject[0] instanceof AccurateMapLayer) {
                // 点中Tile
                int cx = (Integer)clickedObject[1];
                int cy = (Integer)clickedObject[2];
                int tw = map.parent.getTileWidth();
                int th = map.parent.getTileHeight();
                bounds = new Rectangle(cx * tw, cy * th, tw, th);
            }
            viewer.map2screen(bounds);
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
            gc.drawRectangle(bounds);
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
    
    /*
     * 查找当前选中的NPC在选中层中的索引
     */
    private int getSelectedNPCIndex() {
        for (int i = 0; i < selectedLayer.getNpcs().size(); i++) {
            if (selectedLayer.getNpcs().get(i) == selectedNPC) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 键按下事件。如果当前有选中的NPC，则可响应：删除、上、下、左、右。
     */
    public void onKeyDown(int keyCode) {
        if (!isDragging && selectedNPC != null) {
            if (movingKeyCode != 0 && keyCode != movingKeyCode) {
                // 如果正在移动NPC，则忽略其他按键
                return;
            }
            if (keyCode == SWT.DEL) {
                // 删除键，删除选中NPC
                int index = getSelectedNPCIndex();
                selectedLayer.getNpcs().remove(index);
                viewer.makeDirty(selectedNPC);
                selectedNPC = null;
                viewer.fireContentChanged();
                viewer.redraw();
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
                    selectedNPC.y -= step;
                    break;
                case SWT.ARROW_DOWN:
                    selectedNPC.y += step;
                    break;
                case SWT.ARROW_LEFT:
                    selectedNPC.x -= step;
                    break;
                case SWT.ARROW_RIGHT:
                    selectedNPC.x += step;
                    break;
                }
                normalize(selectedNPC);
                
                // 暂时把选中NPC从列表中删除
                if (isStart) {
                    int index = getSelectedNPCIndex();
                    selectedLayer.getNpcs().get(index).isShow = false;
                    viewer.makeDirty(selectedNPC);
                }
                viewer.redraw();
            }
        }
    }

    /**
     * 键松开事件。
     */
    public void onKeyUp(int keyCode) {
        if (movingKeyCode != 0 && keyCode == movingKeyCode) {
            movingKeyCode = 0;
            selectedNPC.isShow = true;
            viewer.fireContentChanged();
            viewer.makeDirty(selectedNPC);
            viewer.redraw();
        }
    }


    /**
     * 得到工具右键菜单。
     */
    public Menu getMenu() {
        if (popupMenu == null) {
            popupMenu = new Menu(viewer);
            GameMap map = viewer.getMap();
            if (map == null) {
            	return null;
            }
            for (IMapLayer layer : map.layers) {
                if (layer instanceof MapNPCLayer) {
                    MenuItem mi = new MenuItem(popupMenu, SWT.PUSH);
                    mi.setText("移到" + getLayerName((MapNPCLayer)layer));
                    mi.addSelectionListener(this);
                    mi.setEnabled(false);
                }
            }
            
            new MenuItem(popupMenu, SWT.SEPARATOR);
            
            upOrderMove = new MenuItem(popupMenu, SWT.PUSH);
            upOrderMove.setText("上移一个顺序");
            upOrderMove.addSelectionListener(this);
            upOrderMove.setEnabled(false);
            
            downOrderMove = new MenuItem(popupMenu, SWT.PUSH);
            downOrderMove.setText("下移一个顺序");
            downOrderMove.addSelectionListener(this);
            downOrderMove.setEnabled(false);
            
            firstOrderMove = new MenuItem(popupMenu, SWT.PUSH);
            firstOrderMove.setText("移至第一个顺序");
            firstOrderMove.addSelectionListener(this);
            firstOrderMove.setEnabled(false);
            
            lastOrderMove = new MenuItem(popupMenu, SWT.PUSH);
            lastOrderMove.setText("移至最后一个顺序");
            lastOrderMove.addSelectionListener(this);
            lastOrderMove.setEnabled(false);
        }
        return popupMenu;
    }
    private void updateMenu(){
    	GameMap map = viewer.getMap();
    	int i = 0;
    	for (IMapLayer layer : map.layers) {
            if (layer instanceof MapNPCLayer) {
                MenuItem mi = popupMenu.getItem(i);
                if(selectedNPC==null){
                	mi.setEnabled(false);
                }else if(layer == selectedLayer	){
                	mi.setEnabled(false);
                }else{
                	mi.setEnabled(true);
                }
                i++;
            }
        }
    }
    private String getLayerName(MapNPCLayer layer) {
        String name = layer.getName();
        int pos = name.indexOf('(');
        if (pos == -1) {
            return name;
        } else {
            return name.substring(0, pos);
        }
    }

    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() instanceof MenuItem) {
            MenuItem mi = (MenuItem)e.getSource();
            if (mi.getText().startsWith("移到") && selectedNPC != null) {
                String targetName = mi.getText().substring(2);
                GameMap map = viewer.getMap();
                for (IMapLayer layer : map.layers) {
                    if (layer instanceof MapNPCLayer && targetName.equals(getLayerName((MapNPCLayer)layer)) && selectedLayer != layer) {
                        // 移动NPC到另外一层
                        selectedLayer.getNpcs().remove(selectedNPC);
                        selectedLayer = (MapNPCLayer)layer;
                        selectedLayer.getNpcs().add(selectedNPC);
                        selectedLayer = (MapNPCLayer)layer;
                        updateMenu();
                        viewer.fireContentChanged();
                        viewer.makeDirty(selectedNPC);
                        viewer.redraw();
                    }
                }
            } else if("上移一个顺序".equals(mi.getText())) {
            	int index = selectedLayer.getNpcs().lastIndexOf(selectedNPC);            	
            	MapNPC preMapNpc = selectedLayer.getNpcs().remove(index);            	
            	selectedLayer.getNpcs().add(index - 1, preMapNpc);
            	
            } else if("下移一个顺序".equals(mi.getText())) {
            	int index = selectedLayer.getNpcs().lastIndexOf(selectedNPC);            	
            	MapNPC afterMapNpc = selectedLayer.getNpcs().remove(index);            	
            	selectedLayer.getNpcs().add(index + 1, afterMapNpc);
            	
            } else if("移至第一个顺序".equals(mi.getText())) {
            	int index = selectedLayer.getNpcs().lastIndexOf(selectedNPC);
            	MapNPC firstMapNpc = selectedLayer.getNpcs().remove(index);
            	selectedLayer.getNpcs().add(0, firstMapNpc);
            	
            } else if("移至最后一个顺序".equals(mi.getText())) {
            	int index = selectedLayer.getNpcs().lastIndexOf(selectedNPC);
            	MapNPC lastMapNpc = selectedLayer.getNpcs().remove(index);
            	selectedLayer.getNpcs().add(lastMapNpc);
            }
        }
    }

    public void widgetDefaultSelected(SelectionEvent e) {}
}
