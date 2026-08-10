package com.pip.sanguo.data;

import java.io.*;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.pip.mapeditor.MapEditor;
import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.MapExport;
import com.pip.mapeditor.data.MapFile;
import com.pip.mapeditor.data.MapNPC;
import com.pip.mapeditor.data.MapNPCLayer;
import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.area.YOrderComparator;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;
import com.pipimage.png.PngEncoder;
import com.swtdesigner.SWTResourceManager;

public class PlayerLocationMapMaker {
    private List<int[]> playerInfo = new ArrayList<int[]>();
    private Map<Integer, Map<Integer, Integer>> locationMap = new HashMap<Integer, Map<Integer, Integer>>();
    
//    id  mapid   x   y   level
//    2836    16  551 187 13
//    1834    32  51  166 13
//    2658    32  123 238 13
//    2156    32  126 458 13
//    3190    32  147 263 13
//    3573    32  147 263 14
    public PlayerLocationMapMaker(File f) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        while ((line = br.readLine()) != null) {
            String[] secs = line.trim().split("\t");
            if (secs.length >= 5 && secs[0].length() > 0 && Character.isDigit(secs[0].charAt(0))) {
                int[] arr = new int[] {
                        Integer.parseInt(secs[0]),
                        Integer.parseInt(secs[1]),
                        Integer.parseInt(secs[2]),
                        Integer.parseInt(secs[3]),
                        Integer.parseInt(secs[4])
                };
                playerInfo.add(arr);
                
                int mapID = arr[1];
                int x = arr[2];
                int y = arr[3];
                if (!locationMap.containsKey(mapID)) {
                    locationMap.put(mapID, new HashMap<Integer, Integer>());
                }
                Map<Integer, Integer> map = locationMap.get(mapID);
                int key = (x << 16) | y;
                if (map.containsKey(key)) {
                    map.put(key, map.get(key) + 1);
                } else {
                    map.put(key, 1);
                }
            }
        }
    }
    
    public void make() throws Exception {
        Font f = new Font(EditorApplication.getInstance().display, "Arial", 8, 0);
        for (int mapID : locationMap.keySet()) {
            Image img = drawMap(mapID);
            
            // 在图上绘制玩家所在点
            GC gc = new GC(img);
            Map<Integer, Integer> map = locationMap.get(mapID);
            for (int key : map.keySet()) {
                int x = key >> 16;
                int y = key & 0xFFFF;
                int count = map.get(key);
                x *= 2;
                y *= 2;
                
                // gc.setBackground(SWTResourceManager.getColor(255, 0, 0));
                // gc.fillArc(x, y, 16, 16, 0, 360);
                
                gc.setForeground(SWTResourceManager.getColor(255, 0, 0));
                gc.setFont(f);
                String str = String.valueOf(count);
                Point p = gc.textExtent(str);
                gc.drawText(str, x - p.x / 2, y - p.y / 2, true);
            }
            gc.dispose();
            
            // 保存文件
            try {
                ImageLoader imageLoader = new ImageLoader();
                imageLoader.data = new ImageData[] { img.getImageData() }; 
                imageLoader.save("d:\\temp\\" + mapID + ".png", SWT.IMAGE_PNG);
            } catch (Exception e) {
                e.printStackTrace();
            }
            img.dispose();
        }
    }
    
    private Image drawMap(int mapID) throws Exception {
        GameArea ga = (GameArea)EditorApplication.getProj().findObject(GameArea.class, mapID >> 4);
        GameAreaInfo gai = new GameAreaInfo(ga);
        gai.load();
        
        MapFile mapFile = new MapFile();
        File mapf = new File(ga.source, "game_l.map");
        mapFile.load(mapf);
        GameMap map = mapFile.getMaps().get(mapID & 0x0F);
        MapExport mapView = new MapExport(map);    
            
        // 创建内存图片
        Image img = new Image(EditorApplication.getInstance().display, map.width, map.height);
        GC gc = new GC(img);
        mapView.drawMapOnBuffer(gc);
        drawNPCLayer(gc, map, gai.maps.get(mapID & 0x0F), true);
        gc.dispose();
        return img;
    }
    
    private void drawNPCLayer(GC gc, GameMap map, GameMapInfo gmi, boolean useLarge) {
        // 人物层，需要加入绘制NPC和出口的方法
        PipAnimateSet animates = map.parent.getAnimates();
        for (GameMapObject obj : gmi.objects) {
            if (obj instanceof GameMapNPC) {
                GameMapNPC npc = (GameMapNPC)obj;
                try {
                    File source = useLarge ? npc.template.image.largeSource : npc.template.image.source;
                    PipAnimateSet pas = new PipAnimateSet();
                    pas.load(source);
                    PipAnimate animate = pas.getAnimate(0);
                    
                    int frame = animate.getFrameAtTime(0);
                    int rx = (int)(npc.x * (useLarge ? 2 : 1));
                    int ry = (int)(npc.y * (useLarge ? 2 : 1));
                    animate.drawFrame(gc, frame, rx , ry, 1, MapEditor.imageCache);
                    
                    Rectangle bounds = animate.getBounds();
                    int texty = (int)(ry + bounds.y);
                    int textx = (int)(rx + (bounds.x + bounds.width / 2));
                    Point ts = gc.textExtent(npc.name);
                    gc.setForeground(SWTResourceManager.getColor(0, 0, 0));
                    gc.drawText(npc.name, textx - ts.x / 2 - 1, texty - 2 - ts.y, true);
                    gc.drawText(npc.name, textx - ts.x / 2 + 1, texty - 2 - ts.y, true);
                    gc.drawText(npc.name, textx - ts.x / 2, texty - 2 - ts.y - 1, true);
                    gc.drawText(npc.name, textx - ts.x / 2, texty - 2 - ts.y - 1, true);
                    gc.setForeground(SWTResourceManager.getColor(0xFF, 0xFF, 0xFF));
                    gc.drawText(npc.name, textx - ts.x / 2, texty - 2 - ts.y, true);
                } catch (Exception e) {
                }
            } else if (obj instanceof GameMapExit) {
                GameMapExit exit = (GameMapExit)obj;
                Rectangle imgSize = getExitIcon(useLarge).getImageDraw(0).getBounds(0);
                int rx = (int)((exit.x * (useLarge ? 2 : 1) - imgSize.width / 2) * 1);
                int ry = (int)((exit.y * (useLarge ? 2 : 1) - imgSize.height / 2) * 1);
                int rw = (int)(imgSize.width * 1);
                int rh = (int)(imgSize.height * 1);
                int frame = 0;
                Image img = getExitIcon(useLarge).getImageDraw(frame).createSWTImage(gc.getDevice(), 0);
                gc.drawImage(img, 0, 0, imgSize.width, imgSize.height, rx, ry, rw, rh);
                img.dispose();
            }
        }
    }
    
    protected PipImage exitIcon;
    public PipImage getExitIcon(boolean useLarge) {
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
