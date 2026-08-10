package com.pip.mapeditor.tool;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;

import com.pip.mango.jni.GLGraphics;
import com.pip.mapeditor.MapEditor;
import com.pip.mapeditor.MapViewer;
import com.pip.mapeditor.data.AccurateMapLayer;
import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.IMapLayer;
import com.pip.mapeditor.data.MapNPC;
import com.pip.mapeditor.data.MapNPCLayer;
import com.pip.mapeditor.data.MultiAnimNPC;
import com.pipimage.image.PipAnimate;
import com.swtdesigner.SWTResourceManager;

/**
 * 多选拾取工具。这个工具让用户可以用来选择多个NPC，并用鼠标拖动。
 * @author lighthu
 */
public class MultiPickupTool implements IMapEditTool {
    // 附着的编辑器
    private MapViewer viewer;
    // 当前选中的NPC，空表示没有
    private List<MapNPC> selectedNPC = new ArrayList<MapNPC>();
    // 当前选中的NPC所在的地图层
    private List<MapNPCLayer> selectedLayer = new ArrayList<MapNPCLayer>();
    // 是否正在拖动
    private boolean isDragging;
    // 拖动的起始点
    private Point dragStartPoint;
    // 最近一次检测到的鼠标位置
    private int lastX, lastY;
    // 正在移动NPC的按键
    private int movingKeyCode;
    // 重复按键次数
    private int repeatKeyCount;
    // 重复按键状态下，累计移动的像素数
    private int keyMovingStep;
    // 是否正在拖框选中
    private boolean isSelecting;
    // 选中框起始位置
    private Point selectStartPoint;
    
    /**
     * 缺省构造方法
     * @param viewer 编辑器
     * @param tv 贴图查看器
     */
    public MultiPickupTool(MapViewer viewer) {
        this.viewer = viewer;
    }
    
    /**
     * 清除选择。
     */
    public void clearSelection() {
    	selectedNPC.clear();
    	selectedLayer.clear();
    }
    
    protected boolean isInSelectedNPC(int x, int y) {
    	for (MapNPC npc : selectedNPC) {
	        if (npc instanceof MultiAnimNPC) {
	        	for (MapNPC npc2 : ((MultiAnimNPC)npc).getChildren()) {
	        		if (isInNPC(npc2, x, y)) {
	        			return true;
	        		}
	        	}
	        } else {
	        	if (isInNPC(npc, x, y)) {
	        		return true;
	        	}
	        }
    	}
    	return false;
    }
    
    protected boolean isInNPC(MapNPC npc, int x, int y) {
    	GameMap map = viewer.getMap();
    	PipAnimate npcAnimate;
        if (map.parent.isLibMode) {
        	npcAnimate = map.parent.getAnimate(npc.animateSetRef, npc.animate);
        } else {
        	npcAnimate = map.parent.getAnimates().getAnimate(npc.animate);
        }
        Rectangle rect = npcAnimate.getBounds();
        rect.x += npc.x;
        rect.y += npc.y;
        if (rect.contains(x, y)) {
            return true;
        }
        return false;
    }
    
    protected boolean isInNPC(MapNPC npc, Rectangle selArea) {
        if (npc instanceof MultiAnimNPC) {
        	for (MapNPC npc2 : ((MultiAnimNPC)npc).getChildren()) {
        		if (isInNPC2(npc2, selArea)) {
        			return true;
        		}
        	}
        } else {
        	if (isInNPC2(npc, selArea)) {
        		return true;
        	}
        }
    	return false;
    }
    
    protected boolean isInNPC2(MapNPC npc, Rectangle selArea) {
    	GameMap map = viewer.getMap();
    	PipAnimate npcAnimate;
        if (map.parent.isLibMode) {
        	npcAnimate = map.parent.getAnimate(npc.animateSetRef, npc.animate);
        } else {
        	npcAnimate = map.parent.getAnimates().getAnimate(npc.animate);
        }
        Rectangle rect = npcAnimate.getBounds();
        rect.x += npc.x;
        rect.y += npc.y;
        if (rect.intersects(selArea)) {
        	return true;
        }
        return false;
    }
    
    /**
     * 鼠标按下事件。如果点中了选中的NPC，则开始拖动；否则开始拖框选择。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseDown(int x, int y, int mask) {
    	if (isInSelectedNPC(x, y)) {
    		// 点中了已选中的NPC，开始拖动
    		isDragging = true;
    		isSelecting = false;
    		dragStartPoint = new Point(x, y);
            for (int i = 0; i < selectedNPC.size(); i++) {
            	MapNPC npc = selectedNPC.get(i);
            	npc.isShow = false;
            	viewer.makeDirty(npc);
            }
            viewer.redraw();
    	} else {
    		// 开始画框选择
    		isSelecting = true;
    		isDragging = false;
    		selectStartPoint = new Point(x, y);
    		clearSelection();
    		viewer.redraw();
    	}
    }
    
    /**
     * 鼠标抬起事件。如果当前处于拖动NPC的状态，则这里确定这个NPC的最终坐标。如果处于选择状态，则在这里确定选择范围。
     * @param x 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param y 鼠标位置在地图中的相对位置（不是屏幕坐标）
     * @param mask 按键状态掩码
     */
    public void mouseUp(int x, int y, int mask) {
        if (isDragging) {
            isDragging = false;
            
            int xoff = x - dragStartPoint.x;
            int yoff = y - dragStartPoint.y;
            for (MapNPC npc : selectedNPC) {
            	npc.x += xoff;
            	npc.y += yoff;
            	if (npc instanceof MultiAnimNPC) {
            		for (MapNPC npc2 : ((MultiAnimNPC)npc).getChildren()) {
            			npc2.x += xoff;
            			npc2.y += yoff;
            			normalize(npc2);
            		}
            	} else {
            		normalize(npc);
            	}
            }
            if (x != dragStartPoint.x || y != dragStartPoint.y) {
                viewer.fireContentChanged();
            }
            for (int i = 0; i < selectedNPC.size(); i++) {
            	MapNPC npc = selectedNPC.get(i);
            	npc.isShow = true;
            	viewer.makeDirty(npc);
            }
            viewer.redraw();
        } else if (isSelecting) {
        	isSelecting = false;
        	Rectangle selArea = new Rectangle(selectStartPoint.x, selectStartPoint.y, x - selectStartPoint.x , y - selectStartPoint.y);
        	if (selArea.width < 0) {
        		selArea.x += selArea.width;
        		selArea.width = -selArea.width;
        	}
        	if (selArea.height < 0) {
        		selArea.y += selArea.height;
        		selArea.height = -selArea.height;
        	}
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
                        if (isInNPC(npc, selArea)) {
                        	selectedNPC.add(npc);
                        	selectedLayer.add((MapNPCLayer)layer);
                        }
                    }
                }
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
        viewer.redraw();
    }
    
    // 调整NPC位置以保证NPC有一部分在屏幕内。
    private void normalize(MapNPC npc) {
        GameMap map = viewer.getMap();
        PipAnimate animate = map.parent.getAnimates().getAnimate(npc.animate);
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
        	int offx = 0;
        	int offy = 0;
        	if (isDragging) {
        		offx = lastX - dragStartPoint.x;
        		offy = lastY - dragStartPoint.y;
        	} else {
        		switch (movingKeyCode) {
                case SWT.ARROW_UP:
                	offy = -keyMovingStep;
                	break;
                case SWT.ARROW_DOWN:
                	offy = keyMovingStep;
                    break;
                case SWT.ARROW_LEFT:
                	offx = -keyMovingStep;
                    break;
                case SWT.ARROW_RIGHT:
                	offx = keyMovingStep;
                    break;
                }
        	}
        	
            // 绘制被拖动的NPC
        	for (MapNPC npc : selectedNPC) {
        		if (npc instanceof MultiAnimNPC) {
        			for (MapNPC npc2 : ((MultiAnimNPC)npc).getChildren()) {
        				drawNPC(gc, npc2, offx, offy);
        			}
        		} else {
        			drawNPC(gc, npc, offx, offy);
        		}
            }
        } else {
            // 绘制选中的NPC的外框
        	for (MapNPC npc : selectedNPC) {
        		if (npc instanceof MultiAnimNPC) {
        			for (MapNPC npc2 : ((MultiAnimNPC)npc).getChildren()) {
        				drawNPCBounds(gc, npc2);
        			}
        		} else {
        			drawNPCBounds(gc, npc);
        		}
            }
        	
        	// 绘制选择框
        	if (isSelecting) {
        		Rectangle bounds = new Rectangle(selectStartPoint.x, selectStartPoint.y, lastX - selectStartPoint.x, lastY- selectStartPoint.y);
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
        	int offx = 0;
        	int offy = 0;
        	if (isDragging) {
        		offx = lastX - dragStartPoint.x;
        		offy = lastY - dragStartPoint.y;
        	} else {
        		switch (movingKeyCode) {
                case SWT.ARROW_UP:
                	offy = -keyMovingStep;
                	break;
                case SWT.ARROW_DOWN:
                	offy = keyMovingStep;
                    break;
                case SWT.ARROW_LEFT:
                	offx = -keyMovingStep;
                    break;
                case SWT.ARROW_RIGHT:
                	offx = keyMovingStep;
                    break;
                }
        	}
        	
            // 绘制被拖动的NPC
        	for (MapNPC npc : selectedNPC) {
        		if (npc instanceof MultiAnimNPC) {
        			for (MapNPC npc2 : ((MultiAnimNPC)npc).getChildren()) {
        				drawNPC(gc, npc2, offx, offy);
        			}
        		} else {
        			drawNPC(gc, npc, offx, offy);
        		}
            }
        } else {
            // 绘制选中的NPC的外框
        	for (MapNPC npc : selectedNPC) {
        		if (npc instanceof MultiAnimNPC) {
        			for (MapNPC npc2 : ((MultiAnimNPC)npc).getChildren()) {
        				drawNPCBounds(gc, npc2);
        			}
        		} else {
        			drawNPCBounds(gc, npc);
        		}
            }
        	
        	// 绘制选择框
        	if (isSelecting) {
        		Rectangle bounds = new Rectangle(selectStartPoint.x, selectStartPoint.y, lastX - selectStartPoint.x, lastY- selectStartPoint.y);
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
    
    private void drawNPC(GC gc, MapNPC npc, int offx, int offy) {
    	GameMap map = viewer.getMap();
    	PipAnimate animate;
        if (map.parent.isLibMode){
        	animate = map.parent.getAnimate(npc.animateSetRef, npc.animate);
        } else {
        	animate = map.parent.getAnimates().getAnimate(npc.animate);
        }
        Point pt = new Point(npc.x, npc.y);
        pt.x += offx;
        pt.y += offy;
        viewer.map2screen(pt);
        animate.drawAnimateFrame(gc, viewer.getCurrentTime(), pt.x, pt.y, viewer.getRatio(), null);
        
        // 绘制NPC外框
        Rectangle bounds = animate.getBounds();
        bounds.x += npc.x + offx;
        bounds.y += npc.y + offy;
        viewer.map2screen(bounds);
        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
        gc.drawRectangle(bounds);
    }
    
    private void drawNPC(GLGraphics gc, MapNPC npc, int offx, int offy) {
    	GameMap map = viewer.getMap();
    	PipAnimate animate;
        if (map.parent.isLibMode){
        	animate = map.parent.getAnimate(npc.animateSetRef, npc.animate);
        } else {
        	animate = map.parent.getAnimates().getAnimate(npc.animate);
        }
        Point pt = new Point(npc.x, npc.y);
        pt.x += offx;
        pt.y += offy;
        viewer.map2screen(pt);
        animate.drawAnimateFrame(gc, viewer.getCurrentTime(), pt.x, pt.y, viewer.getRatio(), MapEditor.imageCache);
        
        // 绘制NPC外框
        Rectangle bounds = animate.getBounds();
        bounds.x += npc.x + offx;
        bounds.y += npc.y + offy;
        viewer.map2screen(bounds);
        gc.setColor(SWTResourceManager.getColor(SWT.COLOR_RED));
        gc.drawRect(bounds);
    }
    
    private void drawNPCBounds(GC gc, MapNPC npc) {
    	GameMap map = viewer.getMap();
    	PipAnimate animate;
        if (map.parent.isLibMode) {
        	animate = map.parent.getAnimate(npc.animateSetRef, npc.animate);
        }else{
        	animate = map.parent.getAnimates().getAnimate(npc.animate);
        }
        Rectangle bounds = animate.getBounds();
        bounds.x += npc.x;
        bounds.y += npc.y;
        viewer.map2screen(bounds);
        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
        gc.drawRectangle(bounds);
    }
    
    private void drawNPCBounds(GLGraphics gc, MapNPC npc) {
    	GameMap map = viewer.getMap();
    	PipAnimate animate;
        if (map.parent.isLibMode) {
        	animate = map.parent.getAnimate(npc.animateSetRef, npc.animate);
        }else{
        	animate = map.parent.getAnimates().getAnimate(npc.animate);
        }
        Rectangle bounds = animate.getBounds();
        bounds.x += npc.x;
        bounds.y += npc.y;
        viewer.map2screen(bounds);
        gc.setColor(SWTResourceManager.getColor(SWT.COLOR_BLUE));
        gc.drawRect(bounds);
    }
    
    /*
     * 查找当前选中的NPC在选中层中的索引
     */
    private int getNPCIndex(MapNPCLayer layer, MapNPC npc) {
        for (int i = 0; i < layer.getNpcs().size(); i++) {
            if (layer.getNpcs().get(i) == npc) {
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
            	for (int i = 0; i < selectedNPC.size(); i++) {
            		MapNPC npc = selectedNPC.get(i);
            		MapNPCLayer layer = selectedLayer.get(i);
            		int index = getNPCIndex(layer, npc);
            		layer.getNpcs().remove(index);
            		viewer.makeDirty(npc);
            	}
            	selectedNPC.clear();
            	selectedLayer.clear();
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
                    keyMovingStep = 1;
                } else {
                    repeatKeyCount++;
                    step = repeatKeyCount / 3 + 1;
                    keyMovingStep += step;
                }
                movingKeyCode = keyCode;
                
                for (int i = 0; i < selectedNPC.size(); i++) {
                	MapNPC npc = selectedNPC.get(i);
                	npc.isShow = true;
                	viewer.makeDirty(npc);
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
            int xoff = 0;
        	int yoff = 0;
    		switch (movingKeyCode) {
            case SWT.ARROW_UP:
            	yoff = -keyMovingStep;
            	break;
            case SWT.ARROW_DOWN:
            	yoff = keyMovingStep;
                break;
            case SWT.ARROW_LEFT:
            	xoff = -keyMovingStep;
                break;
            case SWT.ARROW_RIGHT:
            	xoff = keyMovingStep;
                break;
            }
            movingKeyCode = 0;
        	
        	for (MapNPC npc : selectedNPC) {
            	npc.x += xoff;
            	npc.y += yoff;
            	if (npc instanceof MultiAnimNPC) {
            		for (MapNPC npc2 : ((MultiAnimNPC)npc).getChildren()) {
            			npc2.x += xoff;
            			npc2.y += yoff;
            			normalize(npc2);
            		}
            	} else {
            		normalize(npc);
            	}
            }
            viewer.fireContentChanged();
            for (int i = 0; i < selectedNPC.size(); i++) {
            	MapNPC npc = selectedNPC.get(i);
            	npc.isShow = true;
            	viewer.makeDirty(npc);
            }
            viewer.redraw();
        }
    }

    /**
     * 得到工具右键菜单。
     */
    public Menu getMenu() {
        return null;
    }
}
