package com.pip.sanguo.editor.area;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.pip.mango.jni.GLGraphics;
import com.pip.mango.jni.GLUtils;
import com.pip.mapeditor.MapEditor;
import com.pip.mapeditor.MapViewer;
import com.pip.mapeditor.data.*;
import com.pip.sanguo.data.map.*;
import com.pip.sanguo.editor.EditorPlugin;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.swtdesigner.SWTResourceManager;

/**
 * 游戏地图编辑器。它从标准地图编辑器MapViewer对象中继承了地图的绘制功能，并额外实现和游戏元素相关的
 * 内容的绘制和编辑。
 * @author lighthu
 */
public class GameMapViewer extends MapViewer {
    protected GameMapInfo mapInfo;
    protected HashMap<File, PipAnimateSet> npcImageCache = new HashMap<File, PipAnimateSet>();
    protected boolean showMapNPC = true;
    protected PipImage exitIcon;
    protected boolean useLarge;
    
    public GameMapViewer(Composite parent, int style) {
        super(parent, style);
    }
    
    /**
     * 是否需要显示所有同类型NPC的追击范围和视野范围
     */
    private boolean showAllNpcRange;
    
    /**
     * 设置编辑对象。
     */
    public void setInput(GameMap map, GameMapInfo mapInfo) {
        this.mapInfo = mapInfo;
        setInput(map);
    }
    
    public GameMapInfo getMapInfo() {
        return mapInfo;
    }
    
    public void setUseLarge(boolean value) {
        useLarge = value;
    }
    
    public void setShowMapNPC(boolean value) {
        showMapNPC = value;
        redraw();
    }
    
    public void setShowNpcRange(boolean needShow){
        showAllNpcRange = needShow;
    }
    
    public boolean getShowNpcRange(){
        return showAllNpcRange;
    }
    
    /**
     * 获得NPC视野范围
     * @param npc 当前选中NPC
     * @return
     */
    public Rectangle getEyeShot(GameMapNPC npc) {
        Rectangle bounds = getCachedNPCImage(npc).getAnimate(0).getBounds();
        int eyeShotRange = npc.template.eyeshot;
        Rectangle eyeShot = new Rectangle(bounds.x + bounds.width / 2 - eyeShotRange,
                                          bounds.y + bounds.height / 2 - eyeShotRange,
                                          eyeShotRange * 2, eyeShotRange * 2);
        eyeShot.x += npc.x;
        eyeShot.y += npc.y;
        return eyeShot;
    }
    
    /**
     * 获得NPC追击范围
     * @param npc 当前选中NPC
     * @return
     */
    public Rectangle getChaseDistance(GameMapNPC npc) {
        Rectangle bounds = getCachedNPCImage(npc).getAnimate(0).getBounds();
        int chaseRange = npc.template.chaseDistance;
        Rectangle chaseDis = new Rectangle(bounds.x + bounds.width / 2 - chaseRange,
                                          bounds.y + bounds.height / 2 - chaseRange,
                                          chaseRange * 2, chaseRange * 2);
        chaseDis.x += npc.x;
        chaseDis.y += npc.y;
        return chaseDis;
    }
    
    // 绘制NPC层
    protected void drawNPCLayer(GC gc, MapNPCLayer layer, int offx, int offy, Rectangle visibleRange, List<Rectangle> dirtyList, boolean includeAnimate) {
        if (layer != map.groundLayer) {
            if (showMapNPC) {
                super.drawNPCLayer(gc, layer, offx, offy, visibleRange, dirtyList, includeAnimate);
            }
            return;
        }
        
        // 人物层，需要加入绘制NPC和出口的方法
        PipAnimateSet animates = map.parent.getAnimates();
        Object[] arr1 = layer.getNpcs().toArray();
        Object[] arr2 = mapInfo.objects.toArray();
        Object[] arr = new Object[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, arr, 0, arr1.length);
        System.arraycopy(arr2, 0, arr, arr1.length, arr2.length);
        Arrays.sort(arr, new YOrderComparator(useLarge));
        for (Object obj : arr) {
            if (obj instanceof MapNPC) {
                if (showMapNPC) {
                    MapNPC npc = (MapNPC)obj;
                    drawRawNPC(gc, offx, offy, visibleRange, npc, dirtyList, includeAnimate);
                }
            } else if (obj instanceof GameMapNPC) {
                if (!includeAnimate) {
                    continue;
                }
                GameMapNPC npc = (GameMapNPC)obj;
                try {
                    File source = useLarge ? npc.template.image.largeSource : npc.template.image.source;
                    if (!npcImageCache.containsKey(source)) {
                        PipAnimateSet pas = new PipAnimateSet();
                        pas.load(source);
                        npcImageCache.put(source, pas);
                    }
                    PipAnimate animate = npcImageCache.get(source).getAnimate(0);
                    
                    // 检查是否在屏幕范围内
                    Rectangle rect = animate.getBounds();
                    rect.x += npc.x * (useLarge ? 2 : 1);
                    rect.y += npc.y * (useLarge ? 2 : 1);
                    if (!visibleRange.intersects(rect)) {
                        continue;
                    }
                    if (dirtyList != null) {
                        dirtyList.add(rect);
                    }
                    
                    int frame = animate.getFrameAtTime(getCurrentTime());
                    int rx = (int)(npc.x * ratio * (useLarge ? 2 : 1)) + offx;
                    int ry = (int)(npc.y * ratio * (useLarge ? 2 : 1)) + offy;
                    animate.drawFrame(gc, frame, rx, ry, ratio, MapEditor.imageCache);
                    
                    Rectangle bounds = animate.getBounds();
                    int texty = (int)(ry + bounds.y * ratio);
                    int textx = (int)(rx + (bounds.x + bounds.width / 2) * ratio);
                    Point ts = gc.textExtent(npc.name);
                    gc.setForeground(SWTResourceManager.getColor(0, 0, 0));
                    gc.drawText(npc.name, textx - ts.x / 2 - 1, texty - 2 - ts.y, true);
                    gc.drawText(npc.name, textx - ts.x / 2 + 1, texty - 2 - ts.y, true);
                    gc.drawText(npc.name, textx - ts.x / 2, texty - 2 - ts.y - 1, true);
                    gc.drawText(npc.name, textx - ts.x / 2, texty - 2 - ts.y - 1, true);
                    gc.setForeground(SWTResourceManager.getColor(0xFF, 0xFF, 0xFF));
                    gc.drawText(npc.name, textx - ts.x / 2, texty - 2 - ts.y, true);
                    
                    if(showAllNpcRange){
                        //绘制视野范围
                        Rectangle eyeShot = getEyeShot(npc);
                        map2screen(eyeShot);
                        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                        gc.drawRectangle(eyeShot);
                        //绘追击范围
                        Rectangle chaseDis = getChaseDistance(npc);
                        map2screen(chaseDis);
                        if(chaseDis.equals(eyeShot)){
                            chaseDis = new Rectangle(eyeShot.x - 1, eyeShot.y - 1, eyeShot.width + 2, eyeShot.height + 2);
                        }
                        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
                        gc.drawRectangle(chaseDis);
                    }
                } catch (Exception e) {
                }
            } else if (obj instanceof GameMapExit) {
                if (!includeAnimate) {
                    continue;
                }
                GameMapExit exit = (GameMapExit)obj;
                Rectangle imgSize = getExitIcon().getImageDraw(0).getBounds(0);
                int rx = (exit.x * (useLarge ? 2 : 1) - imgSize.width / 2);
                int ry = (exit.y * (useLarge ? 2 : 1) - imgSize.height / 2);
                int rw = imgSize.width;
                int rh = imgSize.height;
                if (dirtyList != null) {
                    dirtyList.add(new Rectangle(rx, ry, rw, rh));
                }
                rx = (int)(rx * ratio) + offx;
                ry = (int)(ry * ratio) + offy;
                rw = (int)(rw * ratio);
                rh = (int)(rh * ratio);
                int frame = (getCurrentTime() % 10) / 5;
                Image img = getExitIcon().getImageDraw(frame).createSWTImage(gc.getDevice(), 0);
                gc.drawImage(img, 0, 0, imgSize.width, imgSize.height, rx, ry, rw, rh);
                img.dispose();
            }
        }
    }
    
    // 绘制NPC层
    protected void drawNPCLayer(GLGraphics gc, MapNPCLayer layer, int offx, int offy, Rectangle visibleRange, List<Rectangle> dirtyList, boolean includeAnimate) {
        if (layer != map.groundLayer) {
            if (showMapNPC) {
                super.drawNPCLayer(gc, layer, offx, offy, visibleRange, dirtyList, includeAnimate);
            }
            return;
        }
        
        // 人物层，需要加入绘制NPC和出口的方法
        PipAnimateSet animates = map.parent.getAnimates();
        Object[] arr1 = layer.getNpcs().toArray();
        Object[] arr2 = mapInfo.objects.toArray();
        Object[] arr = new Object[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, arr, 0, arr1.length);
        System.arraycopy(arr2, 0, arr, arr1.length, arr2.length);
        Arrays.sort(arr, new YOrderComparator(useLarge));
        for (Object obj : arr) {
            if (obj instanceof MapNPC) {
                if (showMapNPC) {
                    MapNPC npc = (MapNPC)obj;
                    drawRawNPC(gc, offx, offy, visibleRange, npc, dirtyList, includeAnimate);
                }
            } else if (obj instanceof GameMapNPC) {
                if (!includeAnimate) {
                    continue;
                }
                GameMapNPC npc = (GameMapNPC)obj;
                try {
                    File source = useLarge ? npc.template.image.largeSource : npc.template.image.source;
                    if (!npcImageCache.containsKey(source)) {
                        PipAnimateSet pas = new PipAnimateSet();
                        pas.load(source);
                        npcImageCache.put(source, pas);
                    }
                    PipAnimate animate = npcImageCache.get(source).getAnimate(0);
                    
                    // 检查是否在屏幕范围内
                    Rectangle rect = animate.getBounds();
                    rect.x += npc.x * (useLarge ? 2 : 1);
                    rect.y += npc.y * (useLarge ? 2 : 1);
                    if (!visibleRange.intersects(rect)) {
                        continue;
                    }
                    if (dirtyList != null) {
                        dirtyList.add(rect);
                    }
                    
                    int frame = animate.getFrameAtTime(getCurrentTime());
                    int rx = (int)(npc.x * ratio * (useLarge ? 2 : 1)) + offx;
                    int ry = (int)(npc.y * ratio * (useLarge ? 2 : 1)) + offy;
                    animate.drawFrame(gc, frame, rx, ry, ratio, MapEditor.imageCache);
                    
                    Rectangle bounds = animate.getBounds();
                    int texty = (int)(ry + bounds.y * ratio);
                    int textx = (int)(rx + (bounds.x + bounds.width / 2) * ratio);
                    Point ts = gc.textExtent(npc.name);
                    gc.setColor(SWTResourceManager.getColor(0, 0, 0));
                    gc.drawText(npc.name, textx - ts.x / 2 - 1, texty - 2 - ts.y);
                    gc.drawText(npc.name, textx - ts.x / 2 + 1, texty - 2 - ts.y);
                    gc.drawText(npc.name, textx - ts.x / 2, texty - 2 - ts.y - 1);
                    gc.drawText(npc.name, textx - ts.x / 2, texty - 2 - ts.y - 1);
                    gc.setColor(SWTResourceManager.getColor(0xFF, 0xFF, 0xFF));
                    gc.drawText(npc.name, textx - ts.x / 2, texty - 2 - ts.y);
                    
                    if(showAllNpcRange){
                        //绘制视野范围
                        Rectangle eyeShot = getEyeShot(npc);
                        map2screen(eyeShot);
                        gc.setColor(SWTResourceManager.getColor(SWT.COLOR_GREEN));
                        gc.drawRect(eyeShot);
                        //绘追击范围
                        Rectangle chaseDis = getChaseDistance(npc);
                        map2screen(chaseDis);
                        if(chaseDis.equals(eyeShot)){
                            chaseDis = new Rectangle(eyeShot.x - 1, eyeShot.y - 1, eyeShot.width + 2, eyeShot.height + 2);
                        }
                        gc.setColor(SWTResourceManager.getColor(SWT.COLOR_DARK_RED));
                        gc.drawRect(chaseDis);
                    }
                } catch (Exception e) {
                }
            } else if (obj instanceof GameMapExit) {
                if (!includeAnimate) {
                    continue;
                }
                GameMapExit exit = (GameMapExit)obj;
                Rectangle imgSize = getExitIcon().getImageDraw(0).getBounds(0);
                int rx = (exit.x * (useLarge ? 2 : 1) - imgSize.width / 2);
                int ry = (exit.y * (useLarge ? 2 : 1) - imgSize.height / 2);
                int rw = imgSize.width;
                int rh = imgSize.height;
                if (dirtyList != null) {
                    dirtyList.add(new Rectangle(rx, ry, rw, rh));
                }
                rx = (int)(rx * ratio) + offx;
                ry = (int)(ry * ratio) + offy;
                rw = (int)(rw * ratio);
                rh = (int)(rh * ratio);
                int frame = (getCurrentTime() % 10) / 5;
                Image img = MapEditor.imageCache.get(getExitIcon(), frame, 0);
                if (img == null) {
                    img = getExitIcon().getImageDraw(frame).createSWTImage(null, 0);
                    MapEditor.imageCache.add(getExitIcon(), frame, 0, img);
                }
                gc.drawTexture(GLUtils.loadImage(img), 0, 0, rx, ry, rw, rh);
            }
        }
    }
    
    public PipAnimateSet getCachedNPCImage(GameMapNPC npc) {
        if (useLarge) {
            return npcImageCache.get(npc.template.image.largeSource);
        } else {
            return npcImageCache.get(npc.template.image.source);
        }
    }
    
    public PipImage getExitIcon() {
        if (exitIcon == null) {
            exitIcon = new PipImage();
            try {
                if (useLarge) {
                    exitIcon.load(getClass().getResourceAsStream("/com/pip/sanguo/editor/area/exit_l.pip"));
                } else {
                    exitIcon.load(getClass().getResourceAsStream("/com/pip/sanguo/editor/area/exit.pip"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return exitIcon;
    }
}
