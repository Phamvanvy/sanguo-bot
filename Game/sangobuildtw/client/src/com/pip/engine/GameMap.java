package com.pip.engine;


import java.io.DataInputStream;
import java.util.Vector;

import com.pip.common.Tool;


/**
 * 游戏地图类。
 */
public class GameMap{
    /** 所属关卡对象 */
    public GamePackage owner;

    /** 地图全局ID。*/
    public int id;
    /** 地图名称。*/
    public String name;
    /** 地图宽度。*/
    public int width;
    /** 地图高度。 */
    public int height;

    /** 出口ID数组 */
    public short[] exitIDs;
    /** 出口X位置数组 */
    public short[] exitX;
    /** 出口Y位置数组 */
    public short[] exitY;
    /** 出口目标地图ID数组 */
    public int[] exitTargetMaps;
    /** 出口目标地图名称数组 */
    public String[] exitTargetMapNames;
    /** 出口目标X位置数组 */
    public short[] exitTargetX;
    /** 出口目标Y位置数组 */
    public short[] exitTargetY;

    /** 背景层类型：0 - 精确、1 - 模糊 */
    public int backgroundType;
    /** 背景层格点数据，精确或模糊地图都用此数组存储 */
    public byte[][] mapData;
    /** 模糊地图的随机数种子 */
    public int randomSeed;
    /** 模糊地图的背景地形，-1表示无 */
    public int baseLandform;

    /* 下面是各NPC层的NPC信息，每一层由3个short数组组成，第一个数组包含NPC的动画ID、第二个包含X、第三个包含Y */

    /** 地面层NPC列表，这些NPC在人物层之前绘制 */
    public short[][] groundNPCs;
    /** 人物层NPC列表，这些NPC和人物一起绘制，需要计算遮挡关系 */
    public short[][] roleNPCs;
    /** 天空层NPC列表，这些NPC在人物层之后绘制 */
    public short[][] skyNPCs;
    
    public GameMap(GamePackage pkg){
        owner = pkg;
    }

    /**
     * 从文件中载入。
     * @param dis
     * @throws Exception
     */
    public void load(DataInputStream dis) throws Exception{
        id = dis.readByte();
        name = Tool.readUTF(dis);
        width = dis.readShort();
        height = dis.readShort();

        // 读取出口信息
        int exitCount = dis.readByte() & 0xFF;
        exitIDs = new short[exitCount];
        exitX = new short[exitCount];
        exitY = new short[exitCount];
        exitTargetMaps = new int[exitCount];
        exitTargetMapNames = new String[exitCount];
        exitTargetX = new short[exitCount];
        exitTargetY = new short[exitCount];
        for(int i = 0; i < exitCount; i++){
            exitIDs[i] = dis.readShort();
            exitX[i] = dis.readShort();
            exitY[i] = dis.readShort();
            exitTargetMaps[i] = dis.readInt();
            exitTargetMapNames[i] = Tool.readUTF(dis);
            exitTargetX[i] = dis.readShort();
            exitTargetY[i] = dis.readShort();
        }

        // 读取地图层，必须有4层：背景层、地面层、人物层和天空层
        int layerCount = dis.readByte();
        if(layerCount != 4){
            throw new IllegalArgumentException();
        }

        // 读取背景层
        byte layerType = dis.readByte();
        if(layerType == 0){
            backgroundType = 0;
            int cw = width / owner.tileWidth;
            int ch = height / owner.tileHeight;
            mapData = new byte[ch][cw];
            for(int i = 0; i < ch; i++){
                dis.readFully(mapData[i]);
            }
        }else if(layerType == 1){
            backgroundType = 1;
            randomSeed = dis.readInt();
            baseLandform = dis.readByte();
            int cw = width / owner.blurTileWidth;
            int ch = height / owner.blurTileHeight;
            mapData = new byte[ch][cw];
            for(int i = 0; i < ch; i++){
                dis.readFully(mapData[i]);
            }
        }else{
            throw new IllegalArgumentException();
        }

        // 读取NPC层
        for(int i = 0; i < 3; i++){
            layerType = dis.readByte();
            dis.skipBytes(1); //是否为人物层，三国固定为010
            if(layerType != 2){
                throw new IllegalArgumentException();
            }
            short[][] npcs = loadNPCList(dis);
            if(i == 0){
                groundNPCs = npcs;
            }else if(i == 1){
                roleNPCs = npcs;
            }else{
                skyNPCs = npcs;
            }
        }
    }

    // 读取一个NPC层的数据
    private short[][] loadNPCList(DataInputStream dis) throws Exception{
        int count = dis.readShort();
        short[][] data = new short[count][3];
        
        for(int i = 0; i < count; i++){
            data[i][0] = dis.readShort();
            data[i][1] = dis.readShort();
            data[i][2] = dis.readShort();
        }
        
        for(int i = 0; i < count - 1; i++){
            for(int j = i + 1; j < count; j ++){
                if((data[i][2] > data[j][2]) || (data[i][2] == data[j][2] && data[i][1] > data[j][1])){
                    short[] tmp = data[i];
                    data[i] = data[j];
                    data[j] = tmp;
                }
            }
        }
        
        short[][] ret = new short[4][count];
        
        for(int i = 0; i < count; i++){
            ret[0][i] = data[i][0];
            ret[1][i] = data[i][1];
            ret[2][i] = data[i][2];
            ret[3][i] = 0;
        }
        
        return ret;
    }

    /**
     * 生成模糊地图的地图数据。
     * @param landforms 地图包中的所有地形
     */
    public int[][] createBlurMapBuffer(LandformImage[] landforms){
        int rows = mapData.length;
        int cols = mapData[0].length;

        // 地图数据初始化为全透明（-1）
        int[][] mapDataBuffer = new int[rows][cols];

        // 如果有基础地形，则铺满基础地形的100%块(1111)
        if(baseLandform != -1){
            Random rand = new Random(randomSeed);
            LandformImage image = landforms[baseLandform];
            image.generateSearchTable();
            for(int i = 0; i < rows; i++){
                for(int j = 0; j < cols; j++){
                    int[] tile = image.getTile(rand, 0x0F);
                    if(tile[0] != -1){
                        mapDataBuffer[i][j] = makeLayerBits(baseLandform, tile[0], tile[1]) << 22;
                    }
                }
            }
        }

        // 按照地形的优先顺序开始铺地图
        for(int lf = 0; lf < landforms.length; lf++){
            if(lf == baseLandform){
                continue;
            }
            Random rand = new Random(randomSeed);
            byte[][] tmpData = makeLayer(mapData, lf);
            LandformImage image = landforms[lf];
            image.generateSearchTable();
            for(int i = 0; i < rows; i++){
                for(int j = 0; j < cols; j++){
                    int[] tile = image.getTile(rand, tmpData[i + 1][j + 1]);
                    if(tile[0] != -1){
                        if(tmpData[i + 1][j + 1] == 0x0F){
                            // 如果计算出来这个Tile已经是占100%了，则覆盖以前的所有Tile
                            mapDataBuffer[i][j] = makeLayerBits(lf, tile[0], tile[1]) << 22;
                        }else{
                            // 如果不占满，则合并
                            mapDataBuffer[i][j] = mergeGridData(mapDataBuffer[i][j], makeLayerBits(lf, tile[0], tile[1]));
                        }
                    }
                }
            }
            image.frameSearchTable = null;
        }

        return mapDataBuffer;
    }

    // 生成最终地图数据中一个格点一个地形层的数据（10-11位，最低5位为地形ID，然后是2位翻转，最高
    // 位为3-4位的地形ID。
    private int makeLayerBits(int lfid, int tileid, int transit){
        return (lfid << 7) | (transit << 5) | (tileid + 1);
    }

    // 把一层地形格点数据合并到最终地图数据中（最多3层，按10/11/11的格局占一个int，高位为底层）
    private int mergeGridData(int cell, int newLayer){
        int layer1 = (cell >> 22) & 0x3FF;
        if (layer1 == 0) {
            return newLayer << 22;
        }
        int layer2 = (cell >> 11) & 0x7FF;
        if (layer2 == 0) {
            return (layer1 << 22) | (newLayer << 11);
        }
        return (layer1 << 22) | (layer2 << 11) | newLayer;
    }

    /**
     * 生成一个矩形。
     * @param w 宽度
     * @param h 高度
     * @return
     */
    public static byte[][] makeRectangle(int w, int h){
        byte[][] ret = new byte[h][w];
        for(int i = 0; i < h; i++){
            for(int j = 0; j < w; j++){
                if(i == 0){
                    if(j == 0){
                        ret[i][j] = 1; // 0001
                    }else if(j == w - 1){
                        ret[i][j] = 2; // 0010
                    }else{
                        ret[i][j] = 3; // 0011
                    }
                }else if(i == h - 1){
                    if(j == 0){
                        ret[i][j] = 4; // 0100
                    }else if(j == w - 1){
                        ret[i][j] = 8; // 1000
                    }else{
                        ret[i][j] = 12; // 1100
                    }
                }else{
                    if(j == 0){
                        ret[i][j] = 5; // 0101
                    }else if(j == w - 1){
                        ret[i][j] = 10; // 1010
                    }else{
                        ret[i][j] = 15; // 1111
                    }
                }
            }
        }
        return ret;
    }

    /**
     * 根据模糊图层的数据生成指定一种地形的格点数据。
     * @param data 模糊图层数据
     * @param lfid 需求地形
     * @return
     */
    public static byte[][] makeLayer(byte[][] data, int lfid){
        int rows = data.length;
        int cols = data[0].length;
        byte[][] ret = new byte[rows + 2][cols + 2];
        byte[][] sg = makeRectangle(3, 3);
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                if(data[i][j] != lfid){
                    continue;
                }
                ret[i][j] |= sg[0][0];
                ret[i][j + 1] |= sg[0][1];
                ret[i][j + 2] |= sg[0][2];
                ret[i + 1][j] |= sg[1][0];
                ret[i + 1][j + 1] |= sg[1][1];
                ret[i + 1][j + 2] |= sg[1][2];
                ret[i + 2][j] |= sg[2][0];
                ret[i + 2][j + 1] |= sg[2][1];
                ret[i + 2][j + 2] |= sg[2][2];
            }
        }
        return ret;
    }
}
