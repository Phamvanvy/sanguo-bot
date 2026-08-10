package com.pip.engine;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;

import com.pip.common.Tool;
import com.pip.image.ImageSet;
import com.pip.image.PipAnimateSet;


/**
 * 三国关卡游戏包类。关卡游戏包是一个打包文件，里面应该包含以下几类文件：关卡信息0.stg，地图文件*.m，精确贴图tile.pip和tile.ts，
 * 地形文件l*.ldf和l*.ts，地图NPC动画文件npc.anp，地图NPC碰撞区域文件npc.col，以及其他和关卡有关的文件。
 */
public class GamePackage extends PackageFile{
    /** 精确贴图宽度 */
    public int tileWidth;
    /** 精确贴图高度 */
    public int tileHeight;
    /** 模糊贴图宽度 */
    public int blurTileWidth;
    /** 模糊贴图高度 */
    public int blurTileHeight;
    /** 关卡ID */
    public int areaID;
    /** 关卡名字 */
    public String title;
    /** 地形总数 */
    public int landFromCount;

    /**
     * 载入一个游戏包文件。
     * @param data
     * @throws IOException
     */
    public GamePackage(byte[] data) throws Exception{
        super(data);

        //. 读取关卡基本信息
        ByteArrayInputStream bis = new ByteArrayInputStream(getFile("/0.stg"));
        DataInputStream dis = new DataInputStream(bis);
        tileWidth = dis.readShort();
        tileHeight = dis.readShort();
        blurTileWidth = dis.readShort();
        blurTileHeight = dis.readShort();
        
        areaID = dis.readShort() & 0xFFFF;
        title = Tool.readUTF(dis);

        landFromCount = 0;
        Enumeration emu = nameIndexMap.keys();

        while(emu.hasMoreElements()){
            String fileName = (String)emu.nextElement();

            if(fileName.endsWith(".ldf")){
                landFromCount++;
            }
        }
    }

    /**
     * 载入关卡精确贴图图片文件。
     * @return
     * @throws Exception
     */
    public ImageSet loadTileImage() throws Exception{
        return new ImageSet(getFile("/tile.pip"));
    }

    /**
     * 载入关卡精确贴图描述文件。
     * @return 返回2个长度为贴图数量的byte数组，第一个数组中记录每一贴图对应的图片帧的索引，第二个数组是
     *     帧描述信息，从高位到低位包括3部分信息：2位翻转值、5位缩略图颜色索引、1位通过标志（1表示不可通过）
     * @throws Exception
     */
    public byte[][] getTileInfo() throws Exception{
        return parseTileInfo(getFile("/tile.ts"));
    }

    /**
     * 解析贴图信息描述文件。
     * @param data
     * @return 返回2个长度为贴图数量的byte数组，第一个数组中记录每一贴图对应的图片帧的索引，第二个数组是
     *     帧描述信息，从高位到低位包括3部分信息：2位翻转值、5位缩略图颜色索引、1位通过标志（1表示不可通过）
     * @throws Exception
     */
    private byte[][] parseTileInfo(byte[] data) throws Exception{
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);
        int tcount = dis.readShort();
        byte[][] ret = new byte[2][tcount];
        for(int i = 0; i < tcount; i++){
            ret[0][i] = dis.readByte();
            ret[1][i] = dis.readByte();
        }
        return ret;
    }

    /**
     * 载入所有地形的图片文件。
     * @return 
     * @throws Exception
     */
    public LandformImage[] loadAllLandformImage() throws Exception{
        LandformImage[] result = new LandformImage[landFromCount];

        for(int i = 0; i < result.length; i++){
            result[i] = loadLandformImage(i);
        }

        return result;
    }

    /**
     * 载入所有地形文件的贴图描述文件
     * @return
     * @throws Exception
     */
    public byte[][] loadAllLandformTileInfof() throws Exception{
        byte[][] result = new byte[landFromCount][];

        for(int i = 0; i < result.length; i++){
            result[i] = loadLandformTileInfo(i);
        }

        return result;
    }

    /**
     * 载入一个地形的图片文件。
     * @param lid
     * @return 
     * @throws Exception
     */
    public LandformImage loadLandformImage(int lid) throws Exception{
        return new LandformImage(getFile("/l" + lid + ".ldf"));
    }

    /**
     * 载入一个地形的贴图描述文件。
     * @param lid
     * @return 返回2个长度为贴图数量的byte数组，第一个数组中记录每一贴图对应的图片帧的索引，第二个数组是
     *     帧描述信息，从高位到低位包括3部分信息：2位翻转值(未用)、5位缩略图颜色索引、1位通过标志（1表示不可通过）
     */
    public byte[] loadLandformTileInfo(int lid) throws Exception{
        return parseTileInfo(getFile("/l" + lid + ".ts"))[1];
    }

    /**
     * 载入地图NPC动画集合文件。
     * @return
     */
    public PipAnimateSet loadNPCAnimates() throws Exception{
        return parseAnimatesPackage(getFile("/npc.anp"));
    }

    /**
     * 解析一个打包的动画文件。这个动画文件是一个PackageFile格式的文件，其中第一个文件为CTN，后面依次是每一个动画
     * 中用到的PIP图片文件。
     * @param fileData
     * @return
     */
    public PipAnimateSet parseAnimatesPackage(byte[] fileData) throws Exception{
        PackageFile pkg = new PackageFile(fileData);
        int fcount = pkg.fileContents.length;
        ImageSet[] imgs = new ImageSet[fcount - 1];
        for(int i = 1; i < fcount; i++){
            imgs[i - 1] = new ImageSet(pkg.fileContents[i]);
        }
        return new PipAnimateSet(imgs, pkg.fileContents[0]);
    }

    /**
     * 载入地图NPC的阻挡区域设置。
     * @return 返回地图NPC动画数量相同的2维int数组，第2维是npc身上碰撞区域的数量*2，碰撞区域的x，y拼成到第1个int，w，h拼成第2个int
     * @throws Exception
     */
    public int[][] loadNPCCollision() throws Exception{
        ByteArrayInputStream bis = new ByteArrayInputStream(getFile("/npc.col"));
        DataInputStream dis = new DataInputStream(bis);
        int count = dis.readShort();
        int[][] ret = new int[count][];
        for(int i = 0; i < count; i++){
        	int subCount = dis.readUnsignedByte();
        	ret[i] = new int[subCount << 1];
            
        	for(int j = 0; j < subCount; j++){
        	    ret[i][j << 1] = (dis.readUnsignedShort() << 16) | dis.readUnsignedShort();
        	    ret[i][(j << 1) + 1] = (dis.readUnsignedByte() << 16) | dis.readUnsignedByte();
        	}
        }
        return ret;
    }

    /**
     * 载入一个地图文件。
     * @param mapID 地图ID
     * @return
     * @throws Exception
     */
    public GameMap loadMap(int mapID) throws Exception{
        ByteArrayInputStream bis = new ByteArrayInputStream(Tool.inflate(getFile("/" + mapID + ".m")));
        DataInputStream dis = new DataInputStream(bis);
        GameMap ret = new GameMap(this);
        ret.load(dis);
        ret.id |= areaID << 4;
        return ret;
    }
}
