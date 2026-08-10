package com.pip.sanguo.editor.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.MapExport;
import com.pip.mapeditor.data.MapFile;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.editor.EditorApplication;
import com.pipimage.png.PngEncoder;

public class MapExportPng {
    //不知道为啥，很耗内存，需要运行时加参数-Xms256m -Xmx512m
    public static void exportMapPng() {
        EditorApplication.getProj().serverMode = true;
        
        File targetDir = new File(EditorApplication.getProj().baseDir, "./map_png");
        if(targetDir.exists() == false) {
            targetDir.mkdir();
        }
            
        File[] oldFiles = targetDir.listFiles();
        for (File f : oldFiles) {
            if (f.isFile() && !f.getName().equals(".cvsignore")) {
                f.delete();
            }
        }
        int count = 0;
        for (DataObject obj : EditorApplication.getProj().getDataListByType(GameArea.class)) {
            GameArea ga = (GameArea)obj;
            List<GameMapInfo> gmis = ga.getAreaInfo().maps;
            
            MapFile mapFile = new MapFile();
            File mapf = new File(ga.source, "game.map");
            try {
                mapFile.load(mapf);
                ArrayList<GameMap> maps = mapFile.getMaps();
                int i = 0;
                for(GameMap map : maps) {                    
                    MapExport mapView = new MapExport(map);    
                    
                    // 创建内存图片
                    Image img = new Image(null, map.width, map.height);
                    GC gc = new GC(img);
                    mapView.drawMapOnBuffer(gc);
                    gc.dispose();
                    
                    // 保存文件
                    FileOutputStream fos = null;
                    try {
                        PngEncoder enc = new PngEncoder(img);
                        String fileName = EditorApplication.getProj().baseDir + "\\map_png\\" + gmis.get(i).getGlobalID() + ".png";
                        
                        fos = new FileOutputStream(fileName);
                        enc.encode32(fos, false);
                        fos.close();
                        
                        System.out.println("export:" + fileName);
                        count ++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if(fos != null) {
                                fos.close(); 
                            }                           
                        } catch(Exception e) {
                            
                        }
                    }
                    img.dispose();
                    
                    i ++;
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("共导出:" + count + "个png");
        EditorApplication.getProj().serverMode = false;
    }
}
