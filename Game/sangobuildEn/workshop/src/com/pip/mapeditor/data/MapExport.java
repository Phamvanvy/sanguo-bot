package com.pip.mapeditor.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import com.pip.mapeditor.MapEditor;
import com.pip.util.Utils;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.swtdesigner.SWTResourceManager;

public class MapExport {
	protected GameMap map;
	protected int activeLayer;
	protected boolean[] showLayers;
	protected int currentTime;
    protected double ratio = 1.0;
    
	
	public MapExport(GameMap _map) {
		map = _map;
		activeLayer = 0;
		if (map == null) {
		    showLayers = new boolean[0];
		} else {
		    showLayers = new boolean[map.layers.size()];
		}
		Arrays.fill(showLayers, true);
	}
	
	// 绘制地图到一个虚拟缓存上（原始大小）
	public void drawMapOnBuffer(GC gc) {
		for (int i = 0; i < map.layers.size(); i++) {
            IMapLayer layer = map.layers.get(i);
            if (showLayers[i]) {
            	double oldRatio = ratio;
            	ratio = 1.0;
                drawMapLayer(gc, layer, 0, 0, new Rectangle(0, 0, map.width, map.height));
                ratio = oldRatio;
            }
        }
	}
	
	// 绘制地图的一层。
	protected void drawMapLayer(GC gc, IMapLayer layer, int offx, int offy, Rectangle visibleRange) {
	    if (layer instanceof AccurateMapLayer) {
	        drawAccurateMapLayer(gc, (AccurateMapLayer)layer, offx, offy, visibleRange);
	    } else if (layer instanceof MapNPCLayer) {
	        drawNPCLayer(gc, (MapNPCLayer)layer, offx, offy, visibleRange);
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
        for (int cy = 0; cy < mapData.length; cy++) {
            for (int cx = 0; cx < mapData[cy].length; cx++) {
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
	}
	
	// 绘制NPC层
	protected void drawNPCLayer(GC gc, MapNPCLayer layer, int offx, int offy, Rectangle visibleRange) {
	    MapNPC[] npcs = new MapNPC[layer.getNpcs().size()];
	    layer.getNpcs().toArray(npcs);
	    Arrays.sort(npcs);
	    for (MapNPC npc : npcs) {
	    	drawRawNPC(gc, offx, offy, visibleRange, npc);
	    }
	}
	protected void drawRawNPC(GC gc, int offx, int offy, Rectangle visibleRange, MapNPC npc){
		if(npc instanceof MultiAnimNPC){
			for(MapNPC mNpc: ((MultiAnimNPC) npc).getChildren()){
				drawNPC(gc, offx, offy, visibleRange, mNpc);
			}
		}else{
			drawNPC(gc, offx, offy, visibleRange, npc);
		}
	}
	private void drawNPC(GC gc, int offx, int offy, Rectangle visibleRange, MapNPC npc) {
		PipAnimateSet animates = map.parent.getAnimates();
		PipAnimate animate;
    	if(map.parent.isLibMode){
    		animate = map.parent.getAnimate(npc.animateSetRef, npc.animate);
    	}else{
    		animate = animates.getAnimate(npc.animate);
    	}

        // 检查是否在屏幕范围内
        Rectangle rect = animate.getBounds();
        rect.x += npc.x;
        rect.y += npc.y;
        if (!visibleRange.intersects(rect)) {
            return;
        }

        int frame = animate.getFrameAtTime(getCurrentTime());
        int rx = (int)(npc.x * ratio) + offx;
        int ry = (int)(npc.y * ratio) + offy;
        
        animate.drawFrame(gc, frame, rx, ry, ratio, MapEditor.imageCache);		
	}

	// 绘制模糊贴图层
    protected void drawBlurMapLayer(GC gc, BlurMapLayer layer, int offx, int offy, Rectangle visibleRange) {
        int tw = map.parent.getBlurTileWidth();
        int th = map.parent.getBlurTileHeight();
        int[][][] mapData = layer.getMapData();
        HashMap<Integer, Image> tileBuf = new HashMap<Integer, Image>();
        for (int cy = 0; cy < mapData.length; cy++) {
            for (int cx = 0; cx < mapData[cy].length; cx++) {
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
    }
    
    /**
     * 取得当前编辑的地图对象。
     * @return
     */
    public GameMap getMap() {
        return map;
    }
    
    //用于将美工缩小后的文件名（都是<mapid>副本.png），去掉“副本两个字”
    public static void main(String[] args) {
    	File dir = new File("C:\\map_png缩略图");
    	File targetDir = new File("C:\\map_png缩略图\\minimap");
    	
    	if(targetDir.exists() == false) {
    		targetDir.mkdir();
    	}
    	
        File[] files = dir.listFiles();
        for (File f : files) {
        	if(f.getName().endsWith(".png")) {
        		String prefix = f.getName().substring(0, f.getName().indexOf('.') - 3);
//        		f.renameTo(new File(targetDir, prefix + ".png"));
        		
        		try {
					Utils.copyFile(f, new File(targetDir, prefix + ".png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
    	
    }

}
