package com.pip.sanguo.data.pkg;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import com.pip.mapeditor.data.*;
import com.pip.sanguo.data.*;
import com.pip.sanguo.data.map.*;
import com.pipimage.image.PipAnimate;
import com.pipimage.image.PipAnimateFrameRef;
import com.pipimage.image.PipAnimateSet;
import com.pipimage.image.PipImage;

/**
 * 和客户端打包有关的一些方法集合。
 * @author lighthu
 */
public class PackageUtils {
    /**
     * 把一个关卡转换为客户端下载格式。
     * @param area 关卡定义
     * @param map 关卡地图文件，null表示需要载入
     * @param info 关卡信息文件，null表示需要载入
     * @param target 目标文件
     * @param ratio 坐标放大倍数
     */
    public static void makeClientPackage(GameArea area, MapFile map, GameAreaInfo info, PackageFile target, float ratio) throws Exception {
        target.setName("SANGUOPKG");
        target.setVersion(0);
        
        // 生成关卡信息文件0.stg
        target.addFile("0.stg", makeStageInfo(area, map));
        
        // 生成所有地图文件
        int mapCount = map.getMaps().size();
        for (int i = 0; i < mapCount; i++) {
            target.addFile(i + ".m", makeMapFile(area, map.getMaps().get(i), info.maps.get(i), ratio));
        }
        
        // 精确地图贴图文件
        // System.out.println("导出贴图...");
        target.addFile("tile.pip", makeImageFile(map.getTileImage().image, true));
        target.addFile("tile.ts", makeTileSetFile(map.getTileImage()));
        
        // 模糊地图地形文件
        // System.out.println("导出地形...");
        for (int i = 0; i < map.getLandforms().size(); i++) {
            TileSet ts = map.getLandforms().get(i);
            target.addFile("l" + i + ".ldf", makeImageFile(ts.image, true));
            target.addFile("l" + i + ".ts", makeTileSetFile(ts));
        }
        
        // 地图NPC动画打包文件，打包前检查地图NPC动画的有效性
        checkNPCAnimates(map.getAnimates());
        // System.out.println("导出NPC...");
        target.addFile("npc.anp", makeAnimatePackage(map.getAnimates()));
        
        // 地图NPC碰撞区域定义文件
        target.addFile("npc.col", makeCollisionFile(map));
    }
    
    /**
     * 导出世界地图资源文件
     * @param map 世界地图文件
     * @param target 目标文件
     * */
    public static void makeClientPackage(MapFile map, PackageFile target) throws Exception{
        target.setName("SANGUOWM");
        target.setVersion(0);
        
        // 生成所有地图文件
        System.out.println("生成世界地图数据...");
        int mapCount = map.getMaps().size();
        for (int i = 0; i < mapCount; i++) {
            byte[] _data = makeMapFile(map, map.getMaps().get(i));
            target.addFile(i + ".m", _data);
            System.out.println("_dataLength:::" + _data.length);
        }
        
        // 精确地图贴图文件
        System.out.println("导出世界地图贴图...");
        target.addFile("tile.pip", makeImageFile(map.getTileImage().image, true));
        target.addFile("tile.ts", makeTileSetFile(map.getTileImage(), 0));
        
        // 模糊地图地形文件
//        System.out.println("导出地形...");
//        for (int i = 0; i < map.getLandforms().size(); i++) {
//            TileSet ts = map.getLandforms().get(i);
//            target.addFile("l" + i + ".ldf", makeImageFile(ts.image, true));
//            target.addFile("l" + i + ".ts", makeTileSetFile(ts));
//        }
        
        // 地图NPC动画打包文件，打包前检查地图NPC动画的有效性
        checkNPCAnimates(map.getAnimates());
        System.out.println("导出世界地图NPC...");
        target.addFile("npc.anp", makeAnimatePackage(map.getAnimates()));
        
        // 地图NPC碰撞区域定义文件
//        target.addFile("npc.col", makeCollisionFile(map));        
    }
    
    /*
     * 检查NPC动画的有效性，检查两种情况：
     * 1. 动画有多帧，但都完全一样
     * 2. 动画只有1帧，但delay不等于1
     */
    private static void checkNPCAnimates(PipAnimateSet as) {
        for (int i = 0; i < as.getAnimateCount(); i++) {
            PipAnimate ani = as.getAnimate(i);
            
            // 检查是不是所有动画帧都一样
            if (ani.getFrameCount() > 1) {
                PipAnimateFrameRef f1 = ani.getFrame(0);
                for (int j = 1; j < ani.getFrameCount(); j++) {
                    // 如果某一帧和第一帧完全一样，那么把这帧删除
                    PipAnimateFrameRef f2 = ani.getFrame(j);
                    if (f1.getFrame() == f2.getFrame() && f1.getDx() == f2.getDx() && f1.getDy() == f2.getDy()) {
                        ani.removeFrame(j);
                        j--;
                    } else {
                        // 如果发现一个不一样的，跳出循环
                        break;
                    }
                }
            }
            
            // 检查只有1帧的动画的delay必须是1
            if (ani.getFrameCount() == 1) {
                PipAnimateFrameRef f1 = ani.getFrame(0);
                f1.setDelay(1);
            }
        }
    }

    /**
     * 生成关卡信息文件的内容。
     */
    public static byte[] makeStageInfo(GameArea area, MapFile map) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(map.getTileWidth());
        dos.writeShort(map.getTileHeight());
        dos.writeShort(map.getBlurTileWidth());
        dos.writeShort(map.getBlurTileHeight());
        dos.writeShort(area.id);
        dos.writeUTF(area.title);
        dos.flush();
        return bos.toByteArray();
    }

    /**
     * 生成地图数据（世界地图）
     * */
    public static byte[] makeMapFile(MapFile mapf, GameMap map) throws Exception{
        // 生成一个压缩流
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(bos);
        DataOutputStream dos = new DataOutputStream(zos);
        // 地图基本信息
        dos.writeShort(map.width);
        dos.writeShort(map.height);
        dos.writeByte(mapf.getTileWidth());
        dos.writeByte(mapf.getTileHeight());
        
        // 图层信息
        for (IMapLayer layer : map.layers) {
            if (layer instanceof AccurateMapLayer) {
                // 精确地图层
                AccurateMapLayer alayer = (AccurateMapLayer) layer;
                short[][] celldata = alayer.getLayerData();
                int rows = celldata.length;
                int cols = celldata[0].length;
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        dos.writeByte(celldata[i][j]);
                    }
                }
            } else if (layer instanceof MapNPCLayer) {
                // 地图NPC层
                MapNPCLayer nlayer = (MapNPCLayer) layer;
//                dos.writeByte(2);
//                if (nlayer == map.groundLayer) {
//                    dos.writeByte(1);
//                } else {
//                    dos.writeByte(0);
//                }
                List<MapNPC> npcs = nlayer.getNpcs();
                dos.writeInt(npcs.size() * 3);
                // System.out.println("npcSize:::" + npcs.size());
                for (MapNPC npc : npcs) {
                    dos.writeShort(npc.animate);
                    dos.writeShort(npc.x);
                    dos.writeShort(npc.y);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        // 对文件内容进行压缩
        dos.flush();
        dos.close();
        return bos.toByteArray();
        
    }
    
    /**
     * 生成一个地图文件的内容。
     */
    public static byte[] makeMapFile(GameArea area, GameMap map, GameMapInfo mapInfo, float ratio) throws Exception {
        // 生成一个压缩流
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream zos = new GZIPOutputStream(bos);
        DataOutputStream dos = new DataOutputStream(zos);

        // 地图基本信息
        dos.writeByte(mapInfo.id);
        dos.writeUTF(mapInfo.name);
        dos.writeShort(map.width);
        dos.writeShort(map.height);

        // 出口信息
        List<GameMapExit> exits = new ArrayList<GameMapExit>();
        for (GameMapObject obj : mapInfo.objects) {
            if (obj instanceof GameMapExit) {
                int type = ((GameMapExit)obj).exitType;
                if (type < GameMapExit.TYPE_INTERNAL) {
                    exits.add((GameMapExit) obj);
                }
            }
        }
        dos.writeByte(exits.size());
        for (GameMapExit exit : exits) {
            dos.writeShort(exit.id);
            dos.writeShort((short)(exit.x * ratio));
            dos.writeShort((short)(exit.y * ratio));
            dos.writeInt(exit.targetMap);

            // 查找目标地图名字
            String targetName = "";
            if (exit.showName) {
                try {
                    int targetArea = (exit.targetMap >> 4);
                    int targetMap = exit.targetMap & 0x0F;
                    GameArea targetAreaObj = (GameArea) area.owner.findObject(GameArea.class, targetArea);
                    GameAreaInfo targetAreaInfo = new GameAreaInfo(targetAreaObj);
                    targetAreaInfo.load();
                    targetName = targetAreaInfo.maps.get(targetMap).name;
                } catch (Exception e) {
                }
            }
            dos.writeUTF(targetName);
            // 目标位置不需要传给客户端了
            // dos.writeShort(exit.targetX);
            // dos.writeShort(exit.targetY);
            dos.writeShort(0);
            dos.writeShort(0);
        }

        // 图层信息
        dos.writeByte(map.layers.size());
        for (IMapLayer layer : map.layers) {
            if (layer instanceof AccurateMapLayer) {
                // 精确地图层
                AccurateMapLayer alayer = (AccurateMapLayer) layer;
                dos.writeByte(0);
                short[][] celldata = alayer.getLayerData();
                int rows = celldata.length;
                int cols = celldata[0].length;
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        dos.writeByte(celldata[i][j]);
                    }
                }
            } else if (layer instanceof BlurMapLayer) {
                // 模糊地图层
                BlurMapLayer blayer = (BlurMapLayer) layer;
                dos.writeByte(1);
                dos.writeInt(blayer.getRandomSeed());
                dos.writeByte(blayer.getBaseLandform());
                byte[][] celldata = blayer.getLayerData();
                int rows = celldata.length;
                for (int i = 0; i < rows; i++) {
                    dos.write(celldata[i]);
                }
            } else if (layer instanceof MapNPCLayer) {
                // 地图NPC层
                MapNPCLayer nlayer = (MapNPCLayer) layer;
                dos.writeByte(2);
                if (nlayer == map.groundLayer) {
                    dos.writeByte(1);
                } else {
                    dos.writeByte(0);
                }
                List<MapNPC> npcs = nlayer.getNpcs();
                dos.writeShort(npcs.size());
                for (MapNPC npc : npcs) {
                    dos.writeShort(npc.animate);
                    dos.writeShort(npc.x);
                    dos.writeShort(npc.y);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        // 对文件内容进行压缩
        dos.flush();
        dos.close();
        return bos.toByteArray();
    }

    /**
     * 保存PIP图片。
     * @param fullCompress 如果为true，则图片帧数据不压缩，而是做整体压缩；如果为false，图片每帧压缩。
     */
    public static byte[] makeImageFile(PipImage image, boolean fullCompress) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (fullCompress) {
            if (image.getPaletteCount() == 1) {
                image.setMergeMode(true);
            }
        }
    	DataOutputStream dos = new DataOutputStream(bos);
        image.save(dos, true);
        dos.flush();
        return bos.toByteArray();
    }
    
    /**
     * 保存TS文件。
     */
    public static byte[] makeTileSetFile(TileSet tileSet) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(tileSet.tileInfo.size());
        for (TileInfo ti : tileSet.tileInfo) {
            dos.writeByte(ti.frameID);
            int infoByte = 0;
            infoByte = ti.transit << 6;
            infoByte |= getColorID(ti.thumbColor) << 1;
            if (ti.unpassable) {
                infoByte |= 1;
            }
            dos.writeByte(infoByte);
        }
        dos.flush();
        return bos.toByteArray();
    }
    
    /**
     * 保存TS文件。(世界地图)
     */
    public static byte[] makeTileSetFile(TileSet tileSet, int iii) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(tileSet.tileInfo.size() * 2);
        // System.out.println("tileInfo.Length::" + tileSet.tileInfo.size());
        for (TileInfo ti : tileSet.tileInfo) {
            dos.writeByte(ti.frameID);
            dos.writeByte(ti.transit);
        }
        dos.flush();
        return bos.toByteArray();
    }
    
    public static final int[] thumbColors = new int[] {
        0x000000, 0x808080, 0xC0C0C0, 0xFFFFFF,
        0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF,
        0x0000FF, 0xFF00FF, 0xFFFF80, 0x00FF80,
        0x80FFFF, 0x8080FF, 0xFF0080, 0xFF8040
    };

    /**
     * 得到一个颜色在颜色表中的index。
     */
    public static int getColorID(int color) {
        for (int i = 0; i < thumbColors.length; i++) {
            if (color == thumbColors[i]) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 生成地图NPC碰撞区域定义文件。
     */
    public static byte[] makeCollisionFile(MapFile map) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        int npcCount = map.getAnimates().getAnimateCount();
        dos.writeShort(npcCount);
        for (int i = 0; i < npcCount; i++) {
            NPCImageInfo nii = map.getNPCs().get((long)i);
            if (nii != null) {
                dos.writeByte(nii.cx.length);
                for (int j = 0; j < nii.cx.length; j++) {
                    dos.writeShort(nii.cx[j]);
                    dos.writeShort(nii.cy[j]);
                    dos.writeByte(nii.cw[j]);
                    dos.writeByte(nii.ch[j]);
                }
            } else {
                dos.writeByte(0);
            }
        }
        dos.flush();
        return bos.toByteArray();
    }

    /**
     * 把一个动画集文件打包存储。
     */
    public static byte[] makeAnimatePackage(PipAnimateSet animateSet) throws Exception {
        PackageFile pkg = new PackageFile();
        pkg.setName("ANIMATESPKG");
        pkg.setVersion(0);
        
        // 添加CTN文件
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        animateSet.save(bos, false);
        pkg.addFile("0.ctn", bos.toByteArray());
        
        // 添加所有图片文件
        for (int i = 0; i < animateSet.getFileCount(); i++) {
            pkg.addFile(i + ".pip", makeImageFile(animateSet.getSourceImage(i), false));
        }
        
        bos = new ByteArrayOutputStream();
        pkg.save(bos);
        return bos.toByteArray();
    }
}
