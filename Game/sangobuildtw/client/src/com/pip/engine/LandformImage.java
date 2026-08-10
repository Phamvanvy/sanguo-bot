package com.pip.engine;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import com.pip.image.ImageSet;


/**
 * 模糊地图地形。
 */
public class LandformImage extends ImageSet{
    /** 占满全部格子的贴图 */
    public static final int TILE_100 = 0;
    /** 占满右下75%格子的贴图 */
    public static final int TILE_75 = 1;
    /** 占满下方50%格子的贴图 */
    public static final int TILE_50H = 2;
    /** 占满右方50%格子的贴图 */
    public static final int TILE_50V = 3;
    /** 占满左上和右下角格子的贴图 */
    public static final int TILE_50S = 4;
    /** 占右下25%格子的贴图 */
    public static final int TILE_25 = 5;

    // 每个图块对应的地形类型
    protected short[] tileTypes;
    // 每个图块的出现权重
    protected short[] tilePrior;

    // 贴图类型对应贴图查找表
    public int[][] frameSearchTable;

    // 不同类型的格子对应的贴图画法。数组索引是格子类型，按一个格子划分为4个小格的方法，每一小格用1位
    // 来表示，这样得到一个0-16的整数。4个小格子中，最左上角的格子占最高位，右下角的占最低位。
    protected final static int[][] TYPE_MAP = {
                    {
                                    -1, 0
                    }, // 0000
                    {
                                    TILE_25, 0
                    }, // 0001
                    {
                                    TILE_25, 2
                    }, // 0010
                    {
                                    TILE_50H, 0
                    }, // 0011
                    {
                                    TILE_25, 1
                    }, // 0100
                    {
                                    TILE_50V, 0
                    }, // 0101
                    {
                                    TILE_50S, 2
                    }, // 0110
                    {
                                    TILE_75, 0
                    }, // 0111
                    {
                                    TILE_25, 3
                    }, // 1000
                    {
                                    TILE_50S, 0
                    }, // 1001
                    {
                                    TILE_50V, 2
                    }, // 1010
                    {
                                    TILE_75, 2
                    }, // 1011
                    {
                                    TILE_50H, 1
                    }, // 1100
                    {
                                    TILE_75, 1
                    }, // 1101
                    {
                                    TILE_75, 3
                    }, // 1110
                    {
                                    TILE_100, 0
                    }
    // 1111
    };

    /**
     * 从文件中载入创建。
     * @param data
     */
    public LandformImage(byte[] data) throws Exception{
        super(data);
        int fcount = getFrameCount();
        ByteArrayInputStream bis = new ByteArrayInputStream(data, data.length - 4 * fcount, 4 * fcount);
        DataInputStream dis = new DataInputStream(bis);
        tileTypes = new short[fcount];
        tilePrior = new short[fcount];
        for(int i = 0; i < fcount; i++){
            tileTypes[i] = dis.readShort();
            tilePrior[i] = dis.readShort();
        }
    }

    /**
     * 生成不同格子类型对应的贴图的查找表。在计算模糊地图之前执行此方法，可以提高查找效率。
     */
    public void generateSearchTable(){
        frameSearchTable = new int[6][];
        int count = tileTypes.length;
        for(int t = 0; t < 6; t++){
            int[] candidates = new int[count];
            int canCount = 0;
            for(int i = 0; i < count; i++){
                if(tileTypes[i] == t){
                    candidates[canCount++] = i;
                }
            }
            frameSearchTable[t] = new int[canCount];
            System.arraycopy(candidates, 0, frameSearchTable[t], 0, canCount);
        }
    }

    /**
     * 按照设定的出现频率随机选择一个指定类型的模糊贴图。
     * @param rand 随机数生成器
     * @param tileType 模糊贴图类型
     */
    private int randomChooseTile(Random rand, int tileType){
        if(tileType == -1){
            return -1;
        }
        int[] candidates = frameSearchTable[tileType];
        if(candidates.length == 0){
            candidates = frameSearchTable[TILE_100];
        }
        int totalPrior = 0;
        for(int i = 0; i < candidates.length; i++){
            totalPrior += tilePrior[candidates[i]] & 0xFFFF;
        }
        if(totalPrior == 0){
            return -1;
        }
        //#if MemoryMode == "Small"
        //# return candidates[0];
        //#else
        int point = rand.nextInt(totalPrior);
        for(int i = 0; i < candidates.length; i++){
            point -= tilePrior[candidates[i]] & 0xFFFF;
            if(point <= 0){
                return candidates[i];
            }
        }
        return -1;
        //#endif
    }

    /**
     * 根据格子类型随机查找出一个贴图。
     * @param rand 随机数生成器
     * @param gridType 格子类型，0-16
     * @return 数据中有2个元素：对应贴图在PipImage中的索引，翻转值
     */
    public int[] getTile(Random rand, int gridType){
        int tileIndex = randomChooseTile(rand, TYPE_MAP[gridType][0]);
        return new int[]{
                        tileIndex, TYPE_MAP[gridType][1]
        };
    }
}
