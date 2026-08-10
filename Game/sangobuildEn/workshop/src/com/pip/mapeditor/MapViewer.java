package com.pip.mapeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import com.pip.image.workshop.WorkshopPlugin;
import com.pip.image.workshop.editor.AbstractImageViewer;
import com.pipimage.image.*;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.ResourceManager;
import com.swtdesigner.SWTResourceManager;
import com.pip.mapeditor.data.*;
import com.pip.mapeditor.tool.IMapEditTool;

/**
 * 地图编辑器组件。
 */
public class MapViewer extends AbstractImageViewer {
	protected GameMap map;
	protected IMapEditTool tool;
	protected int activeLayer;
	protected boolean[] showLayers;
	protected int currentTime;
    protected Image mapBuffer;
    protected List<Rectangle> npcDirtyList = new ArrayList<Rectangle>();
    
    public void makeDirty(MapNPC npc) {
    	if (npc instanceof MultiAnimNPC) {
    		Rectangle rect = null;
			for (MapNPC mNpc: ((MultiAnimNPC) npc).getChildren()) {
				if (rect == null) {
					rect = getNPCRect(mNpc);
				} else {
					Rectangle rect2 = getNPCRect(mNpc);
					rect.add(rect2);
				}
			}
			if (rect != null) {
				if (npcDirtyList.size() == 0) {
					npcDirtyList.add(rect);
				} else {
					npcDirtyList.get(0).add(rect);
				}
			}
		}else{
			if (npcDirtyList.size() == 0) {
				npcDirtyList.add(getNPCRect(npc));
			} else {
				npcDirtyList.get(0).add(getNPCRect(npc));
			}
		}
    }
    
    protected Rectangle getNPCRect(MapNPC npc) {
    	PipAnimateSet animates = map.parent.getAnimates();
		PipAnimate animate;
    	if (map.parent.isLibMode) {
    		animate = map.parent.getAnimate(npc.animateSetRef, npc.animate);
    	} else {
    		animate = animates.getAnimate(npc.animate);
    	}
    	Rectangle rect = animate.getBounds();
        rect.x += npc.x;
        rect.y += npc.y;
        return rect;
    }
    
    /**
     * 设置编辑对象。
     */
	public void setInput(Object input) {
		super.setInput(input);
		map = (GameMap)input;
		tool = null;
		if (mapBuffer != null) {
		    mapBuffer.dispose();
		}
		mapBuffer = null;
		activeLayer = 0;
		if (map == null) {
		    showLayers = new boolean[0];
		} else {
		    showLayers = new boolean[map.layers.size()];
		}
		Arrays.fill(showLayers, true);
	}
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public MapViewer(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND);
		addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (isInButtonArea(e.x, e.y) || map == null) {
					return;
				}
				if (e.button == 1) {
				    if (tool != null) {
				        Point pt = new Point(e.x, e.y);
				        screen2map(pt);
				        try{
				        tool.mouseDown(pt.x, pt.y, e.stateMask);
				        }catch(Throwable t){
				        	t.printStackTrace();
				        	MessageDialog.openError(getShell(), "Error", "mouseDown:\n"+t);
				        }
				    }
				}
			}
			public void mouseUp(MouseEvent e) {
				if (isInButtonArea(e.x, e.y) || map == null) {
					return;
				}
				if (e.button == 1) {
					if (tool != null) {
                        Point pt = new Point(e.x, e.y);
                        try{
                        screen2map(pt);
                        tool.mouseUp(pt.x, pt.y, e.stateMask);
						}catch(Throwable t){
				        	t.printStackTrace();
				        	MessageDialog.openError(getShell(), "Error", "mouseUp:\n"+t);
				        }
					}
				}
			}
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
                if (tool != null && map != null) {
                    Point pt = new Point(e.x, e.y);
                    try{
                    screen2map(pt);
                    tool.mouseMove(pt.x, pt.y);
                    }catch(Throwable t){
			        	t.printStackTrace();
			        	MessageDialog.openError(getShell(), "Error", "mouseUp:\n"+t);
			        }
                }
			}
		});
	}
	
	/**
	 * 用白灰格子填充一个区域，作为透明背景。
	 * @param gc
	 * @param rx
	 * @param ry
	 * @param rw
	 * @param rh
	 */
	public static void paintTransparentBackground(GC gc, int rx, int ry, int rw, int rh) {
		Rectangle clip = gc.getClipping();
		gc.setClipping(new Rectangle(rx, ry, rw, rh));
		gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		gc.fillRectangle(rx, ry, rw, rh);
		gc.setBackground(SWTResourceManager.getColor(0xDF, 0xDF, 0xDF));
		int i = 0;
		for (int x = rx - 2; x < rx + rw; x += 4, i++) {
			int j = 0;
			for (int y = ry - 2; y < ry + rh; y += 4, j++) {
				if (((i + j) & 1) == 1) {
					gc.fillRectangle(x, y, 4, 4);
				}
			}
		}
		gc.setClipping(clip);
	}
	
	// 绘制地图到一个虚拟缓存上（原始大小）
	public void drawMapOnBuffer(GC gc) {
		for (int i = 0; i < map.layers.size(); i++) {
            IMapLayer layer = map.layers.get(i);
            if (showLayers[i]) {
            	double oldRatio = ratio;
            	ratio = 1.0;
                drawMapLayer(gc, layer, 0, 0, new Rectangle(0, 0, map.width, map.height), null, true);
                ratio = oldRatio;
            }
        }
	}
	
	// 绘制地图的一层。
	protected void drawMapLayer(GC gc, IMapLayer layer, int offx, int offy, Rectangle visibleRange, List<Rectangle> dirtyList, boolean includeAnimate) {
	    if (layer instanceof AccurateMapLayer) {
	        drawAccurateMapLayer(gc, (AccurateMapLayer)layer, offx, offy, visibleRange);
	    } else if (layer instanceof MapNPCLayer) {
	        drawNPCLayer(gc, (MapNPCLayer)layer, offx, offy, visibleRange, dirtyList, includeAnimate);
	    } else if (layer instanceof BlurMapLayer) {
	        drawBlurMapLayer(gc, (BlurMapLayer)layer, offx, offy, visibleRange);
	    }
	}
	
	// 绘制精确贴图层
	protected void drawAccurateMapLayer(GC gc, AccurateMapLayer layer, int offx, int offy, Rectangle visibleRange) {
	    int tw = map.parent.getTileWidth();
        int th = map.parent.getTileHeight();
        ArrayList<TileInfo> tiles = map.parent.getTileImage().tileInfo;
        PipImage imageSet = map.parent.getTileImage().image;
        short[][] mapData = layer.getLayerData();
        int sx = 0;
        int ex = mapData[0].length;
        int sy = 0;
        int ey = mapData.length;
    	sx = Math.max(0, visibleRange.x / tw);
    	ex = Math.min(mapData[0].length, (visibleRange.x + visibleRange.width + tw - 1) / tw);
    	sy = Math.max(0, visibleRange.y / th);
    	ey = Math.min(mapData.length, (visibleRange.y + visibleRange.height + th - 1) / th);
    	gc.setClipping((int)(offx + visibleRange.x * ratio), (int)(offy + visibleRange.y * ratio), 
    			(int)(visibleRange.width * ratio), (int)(visibleRange.height * ratio));
        for (int cy = sy; cy < ey; cy++) {
            for (int cx = sx; cx < ex; cx++) {
                int cc = mapData[cy][cx];
                if (cc == -1) {
                    continue;
                }
                int cellx = (int)(cx * tw * ratio) + offx;
                int celly = (int)(cy * th * ratio) + offy;
                TileInfo cinfo = tiles.get(cc);
                Image img = imageSet.getImageDraw(cinfo.frameID).createSWTImage(gc.getDevice(), cinfo.transit);
                gc.drawImage(img, 0, 0, tw, th, cellx, celly, (int)(tw * ratio), (int)(th * ratio));
                img.dispose();
            }
        }
    	gc.setClipping((Rectangle)null);
	}
	
	// 绘制NPC层
	protected void drawNPCLayer(GC gc, MapNPCLayer layer, int offx, int offy, Rectangle visibleRange, List<Rectangle> dirtyList, boolean includeAnimate) {
	    MapNPC[] npcs = new MapNPC[layer.getNpcs().size()];
	    layer.getNpcs().toArray(npcs);
	    if(layer.getForceAddOrderDraw() == false) {
	    	Arrays.sort(npcs);
	    	
	    }
	    
	    for (MapNPC npc : npcs) {
	    	drawRawNPC(gc, offx, offy, visibleRange, npc, dirtyList, includeAnimate);
	    }
	}
	protected void drawRawNPC(GC gc, int offx, int offy, Rectangle visibleRange, MapNPC npc, List<Rectangle> dirtyList, boolean includeAnimate){
		if(npc instanceof MultiAnimNPC){
			if (((MultiAnimNPC) npc).isShow == false) {
				return;
			}
			for(MapNPC mNpc: ((MultiAnimNPC) npc).getChildren()){
				drawNPC(gc, offx, offy, visibleRange, mNpc, dirtyList, includeAnimate);
			}
		}else{
			drawNPC(gc, offx, offy, visibleRange, npc, dirtyList, includeAnimate);
		}
	}
	private void drawNPC(GC gc, int offx, int offy, Rectangle visibleRange, MapNPC npc, List<Rectangle> dirtyList, boolean includeAnimate) {
		if(npc.isShow == false) {
			return;
		}
		PipAnimateSet animates = map.parent.getAnimates();
		PipAnimate animate;
    	if(map.parent.isLibMode){
    		animate = map.parent.getAnimate(npc.animateSetRef, npc.animate);
    	}else{
    		animate = animates.getAnimate(npc.animate);
    	}
    	if (animate == null) {
    		return;
    	}

        // 检查是否在屏幕范围内
        Rectangle rect = animate.getBounds();
        rect.x += npc.x;
        rect.y += npc.y;
        if (!visibleRange.intersects(rect)) {
            return;
        }
        rect = rect.intersection(visibleRange);
        if (!includeAnimate) {
        	if (animate.getFrameCount() > 1) {
        		return;
        	}
        }
        if (dirtyList != null) {
        	// 补漏模式，只绘制在dirty范围内的，或者帧数大于1的
        	boolean dirty = false;
        	if (animate.getFrameCount() > 1) {
        		dirty = true;
        	} else {
        		for (Rectangle r : dirtyList) {
        			if (r.intersects(rect)) {
        				dirty = true;
        				break;
        			}
        		}
        	}
        	if (!dirty) {
        		return;
        	}
        	dirtyList.add(rect);
        }

        int frame = animate.getFrameAtTime(getCurrentTime());
        int rx = (int)(npc.x * ratio) + offx;
        int ry = (int)(npc.y * ratio) + offy;
        
        Rectangle oldClip = gc.getClipping();
        gc.setClipping((int)(offx + rect.x * ratio), (int)(offy + rect.y * ratio), (int)(rect.width * ratio), (int)(rect.height * ratio));
        animate.drawFrame(gc, frame, rx, ry, ratio, MapEditor.imageCache);
        gc.setClipping(oldClip);
	}

	// 绘制模糊贴图层
    protected void drawBlurMapLayer(GC gc, BlurMapLayer layer, int offx, int offy, Rectangle visibleRange) {
        int tw = map.parent.getBlurTileWidth();
        int th = map.parent.getBlurTileHeight();
        int[][][] mapData = layer.getMapData();
        HashMap<Integer, Image> tileBuf = new HashMap<Integer, Image>();
        int sx = 0;
        int ex = mapData[0].length;
        int sy = 0;
        int ey = mapData.length;
    	sx = Math.max(0, visibleRange.x / tw);
    	ex = Math.min(mapData[0].length, (visibleRange.x + visibleRange.width + tw - 1) / tw);
    	sy = Math.max(0, visibleRange.y / th);
    	ey = Math.min(mapData.length, (visibleRange.y + visibleRange.height + th - 1) / th);
    	gc.setClipping((int)(offx + visibleRange.x * ratio), (int)(offy + visibleRange.y * ratio), 
    			(int)(visibleRange.width * ratio), (int)(visibleRange.height * ratio));
        for (int cy = sy; cy < ey; cy++) {
            for (int cx = sx; cx < ex; cx++) {
                for (int cz = 0; cz < 3; cz++) {
                    int cc = mapData[cy][cx][cz];
                    if (cc == -1) {
                        continue;
                    }
                    int lfid = cc >> 16;
                    int trans = (cc >> 8) & 0x07;
                    int fid = cc & 0xFF;
                    int cellx = (int)(cx * tw * ratio) + offx;
                    int celly = (int)(cy * th * ratio) + offy;
                    Image img = tileBuf.get(cc);
                    if (img == null) {
                        PipImage image = map.parent.getLandforms().get(lfid).image;
                        img = image.getImageDraw(fid).createSWTImage(gc.getDevice(), trans);
                        tileBuf.put(cc, img);
                    }
                    gc.drawImage(img, 0, 0, tw, th, cellx, celly, (int)(tw * ratio), (int)(th * ratio));
                }
            }
        }
        for (Image img : tileBuf.values()) {
            img.dispose();
        }
    	gc.setClipping((Rectangle)null);
    }
	
	/**
	 * 创建地图缓存图片。这个做法是为了加快绘图速度，减少跳帧。当内容有任何改变的时候，都需要重新创建
	 * 地图缓存。这个缓存中只缓存贴图，而NPC由于有动画，所以不做缓存。
	 */
	protected void createMapBuffer() {
		int mapw = (int)(map.width * ratio);
		int maph = (int)(map.height * ratio);
		mapBuffer = new Image(getDisplay(), mapw, maph);
		GC gc = new GC(mapBuffer);
		
		// 绘制底层透明背景
		paintTransparentBackground(gc, 0, 0, mapw, maph);

		// 绘制在底部的贴图层
		for (int i = 0; i < map.layers.size(); i++) {
		    IMapLayer layer = map.layers.get(i);
	        if (showLayers[i]) {
	            drawMapLayer(gc, layer, 0, 0, new Rectangle(0, 0, map.width, map.height), null, false);
	        }
		}
		gc.dispose();
	}
	
	/**
	 * 当有部分区域需要更新时，更新缓存。
	 */
	protected void updateMapBuffer() {
		GC gc = new GC(mapBuffer);

		Rectangle r = new Rectangle(0, 0, map.width, map.height);
		r = r.intersection(npcDirtyList.get(0));
		
		// 绘制底层透明背景
		paintTransparentBackground(gc, (int)(r.x * ratio), (int)(r.y * ratio), 
				(int)(r.width * ratio), (int)(r.height * ratio));
		
		// 绘制在底部的贴图层
		ArrayList<Rectangle> dirtyList = new ArrayList<Rectangle>(npcDirtyList);
		for (int i = 0; i < map.layers.size(); i++) {
		    IMapLayer layer = map.layers.get(i);
	        if (showLayers[i]) {
	            drawMapLayer(gc, layer, 0, 0, r, dirtyList, false);
	        }
		}
		gc.dispose();
	}

	/**
	 * 绘制组件主要编辑内容。
	 */
	protected void paintInput(GC gc) {
		if (input == null) {
			return;
		}
		Point size = getSize();
		
		// 绘制地图缓存
		if (mapBuffer == null) {
			createMapBuffer();
		} else if (npcDirtyList.size() > 0) {
			updateMapBuffer();
		}
		int mapx = (int)(size.x - map.width * ratio) / 2 + paintOffset.x;
		int mapy = (int)(size.y - map.height * ratio) / 2 + paintOffset.y;
		gc.drawImage(mapBuffer, mapx, mapy);
		
		// 绘制没有缓存的NPC层和地图层
		Point pt1 = new Point(0, 0);
		Point pt2 = new Point(size.x, size.y);
		screen2map(pt1);
		screen2map(pt2);
		Rectangle visibleRange = new Rectangle(pt1.x, pt1.y, pt2.x - pt1.x, pt2.y - pt1.y);
		for (int i = 0; i < map.layers.size(); i++) {
            IMapLayer layer = map.layers.get(i);
            if (showLayers[i] && layer instanceof MapNPCLayer) {
                drawMapLayer(gc, layer, mapx, mapy, visibleRange, npcDirtyList, true);
            }
        }
		npcDirtyList.clear();
	}
	
	/**
	 * 绘制基础编辑内容上层的控制信息。
	 */
	protected void drawInformation(GC gc) {
		super.drawInformation(gc);
		
		if (input == null) {
			return;
		}
		if (tool != null) {
		    tool.draw(gc);
		}
	}
	
	/**
	 * 键盘事件处理。
	 */
	protected void onKeyDown(int keyCode) {
		super.onKeyDown(keyCode);
		if (input != null) {
		    tool.onKeyDown(keyCode);
		}
	}
	
	/**
     * 键盘事件处理。
     */
    protected void onKeyUp(int keyCode) {
        super.onKeyUp(keyCode);
        if (input != null) {
            tool.onKeyUp(keyCode);
        }
    }
	
	/**
	 * 强制重构缓存并重绘。
	 */
	public void refresh() {
        if (mapBuffer != null) {
            mapBuffer.dispose();
        }
		mapBuffer = null;
		redraw();
	}
	
	public void widgetDisposed(DisposeEvent evt) {
	    super.widgetDisposed(evt);
        if (mapBuffer != null) {
            mapBuffer.dispose();
        }
	}
	
	/**
	 * 地图坐标系向屏幕坐标系转换。
	 * @param rect
	 */
	public void map2screen(Rectangle rect) {
	    Point size = getSize();
	    if(Double.compare(ratio, 1.0)==0){
	    	int mapx = (int)(size.x - map.width) / 2 + paintOffset.x;
	        int mapy = (int)(size.y - map.height) / 2 + paintOffset.y;
	        rect.x += mapx;
	        rect.y += mapy;	
	    }else{
	        int mapx = (int)(size.x - map.width * ratio) / 2 + paintOffset.x;
	        int mapy = (int)(size.y - map.height * ratio) / 2 + paintOffset.y;
	        rect.x *= ratio;
	        rect.y *= ratio;
	        rect.width *= ratio;
	        rect.height *= ratio;
	        rect.x += mapx;
	        rect.y += mapy;
	    }
	}
	
    /**
     * 地图坐标系向屏幕坐标系转换。
     * @param rect
     */
	public void map2screen(Point pt) {
        Point size = getSize();
        int mapx = (int)(size.x - map.width * ratio) / 2 + paintOffset.x;
        int mapy = (int)(size.y - map.height * ratio) / 2 + paintOffset.y;
        pt.x *= ratio;
        pt.y *= ratio;
        pt.x += mapx;
        pt.y += mapy;
	}
	
    /**
     * 屏幕坐标系向地图坐标系转换。
     * @param rect
     */
	public void screen2map(Point pt) {
        Point size = getSize();
        int mapx = (int)(size.x - map.width * ratio) / 2 + paintOffset.x;
        int mapy = (int)(size.y - map.height * ratio) / 2 + paintOffset.y;
        pt.x -= mapx;
        pt.y -= mapy;
        pt.x /= ratio;
        pt.y /= ratio;
	}
	
	/**
	 * 取得当前动画播放时间。
	 * @return
	 */
    public int getCurrentTime() {
        return currentTime;
    }

    /**
     * 动画向前一帧。
     */
    public void step() {
        this.currentTime++;
    }

    /**
     * 取得当前编辑工具。
     * @return
     */
    public IMapEditTool getTool() {
        return tool;
    }

    /**
     * 设置当前编辑工具。
     * @param tool
     */
    public void setTool(IMapEditTool tool) {
        this.tool = tool;
        if (tool != null) {
            Menu menu = tool.getMenu();
//            if (menu != null) {
                setMenu(menu);
//            }
        }
        redraw();
    }

    /**
     * 取得当前编辑层。
     * @return
     */
    public int getActiveLayer() {
        return activeLayer;
    }

    /**
     * 设置当前编辑层。
     * @param activeLayer
     */
    public void setActiveLayer(int activeLayer) {
        this.activeLayer = activeLayer;
    }

    /**
     * 取得显示层标志。
     * @return
     */
    public boolean[] getShowLayers() {
        return showLayers;
    }

    /**
     * 设置显示层标志。
     * @param showLayers
     */
    public void setShowLayers(boolean[] showLayers) {
        this.showLayers = showLayers;
        refresh();
    }
    
    /**
     * 取得当前编辑的地图对象。
     * @return
     */
    public GameMap getMap() {
        return map;
    }

    /**
     * 重载放大功能，放大时需要重构缓存。
     */
    public void zoomin() {
        if (ratio < 4) {
            ratio *= 2;
            refresh();
        }
    }
    
    /**
     * 重载缩小功能，缩小时需要重构缓存。
     */
    public void zoomout() {
        if (ratio > 0.125) {
            ratio /= 2;
            refresh();
        }
    }
}
