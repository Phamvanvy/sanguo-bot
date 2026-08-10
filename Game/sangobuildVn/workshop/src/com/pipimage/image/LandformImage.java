package com.pipimage.image;

import java.io.*;
import java.util.*;

/**
 * 地形文件。地形文件在普通PipImage的基础上加上了每帧对应的地形块的信息。
 * @author lighthu
 */
public class LandformImage extends PipImage {
    /** 占满全部格子的贴图 */
    public static final int TILE_100 = 0;
    /** 占满右下75%格子的贴图 */
    public static final int TILE_75 = 1;
    /** 占满下方50%格子的贴图 */
    public static final int TILE_50H  = 2;
    /** 占满右方50%格子的贴图 */
    public static final int TILE_50V = 3;
    /** 占满左上和右下角格子的贴图 */
    public static final int TILE_50S = 4;
    /** 占右下25%格子的贴图 */
    public static final int TILE_25 = 5;
    /** 占满右上75%格子的贴图（可选）*/
    public static final int TILE_75_DOWN = 6;
    /** 占满上方50%格子的贴图（可选）*/
    public static final int TILE_50H_DOWN = 7;
    /** 占满左方50%格子的贴图（可选）*/
    public static final int TILE_50V_RIGHT = 8;
    /** 占满右上25%格子的贴图（可选）*/
    public static final int TILE_25_DOWN = 9;
    
    // 每个图片帧在地形中的作用，一个图片用一个整数：高16位表示图片类型（100%、75%、50%水平、50%垂直、50%对角、25%），低16位表示出现权重
    protected HashMap<Integer, Integer> frameDesc = new HashMap<Integer, Integer>();
    // 贴图类型对应贴图查找表
    protected int[][] frameSearchTable;
    
    // 不同类型的格子对应的贴图画法。数组索引是格子类型，按一个格子划分为4个小格的方法，每一小格用1位
    // 来表示，这样得到一个0-16的整数。4个小格子中，最左上角的格子占最高位，右下角的占最低位。
    // 翻转值：1垂直翻转，2水平翻转
    protected final static int[][] TYPE_MAP = {
        { -1, 0 },  // 0000
        { TILE_25,  0 },  // 0001
        { TILE_25,  2 },  // 0010
        { TILE_50H, 0 },  // 0011
        { TILE_25_DOWN,   0, TILE_25,  1 },  // 0100
        { TILE_50V, 0 },  // 0101
        { TILE_50S, 2 },  // 0110
        { TILE_75,  0 },  // 0111
        { TILE_25_DOWN,   2, TILE_25,  3 },  // 1000
        { TILE_50S, 0 },  // 1001
        { TILE_50V_RIGHT, 0, TILE_50V, 2 },  // 1010
        { TILE_75,  2 },  // 1011
        { TILE_50H_DOWN,  0, TILE_50H, 1 },  // 1100
        { TILE_75_DOWN,   0, TILE_75,  1 },  // 1101
        { TILE_75_DOWN,   2, TILE_75,  3 },  // 1110
        { TILE_100, 0 }   // 1111
    };
    
    /**
     * 载入图片。在PipImage的基础上要读取模糊贴图信息。
     */
    public void load(DataInputStream dis) throws IOException {
        super.load(dis);
        try {
            frameDesc.clear();
            for (int i = 0; i < data.size(); i++) {
                frameDesc.put(i, dis.readInt());
            }
        } catch (Exception e) {
        }
    }
    
    /**
     * 载入图片。在PipImage的基础上要保存模糊贴图信息。
     */
    public void save(DataOutputStream dos, boolean compress) throws IOException{
        super.save(dos, compress);
        for (int i = 0; i < data.size(); i++) {
            if (frameDesc.containsKey(i)) {
                dos.writeInt(frameDesc.get(i));
            } else {
                dos.writeInt(0x00000064);
            }
        }
    }
    
    /**
     * 设置一帧图片的模糊贴图信息。
     * @param frame 图片索引
     * @param type 对应的模糊贴图类型
     * @param priority 出现频率
     */
    public void setFrameAttr(int frame, int type, int priority) {
        frameDesc.put(frame, (type << 16) | priority);
    }
    
    /**
     * 取得一帧图片对应的模糊贴图类型。
     */
    public int getFrameType(int frame) {
        if (frameDesc.containsKey(frame)) {
            return frameDesc.get(frame) >> 16;
        } else {
            return 0;
        }
    }

    /**
     * 取得一帧图片的出现频率。
     */
    public int getFramePriority(int frame) {
        if (frameDesc.containsKey(frame)) {
            return frameDesc.get(frame) & 0xFFFF;
        } else {
            return 100;
        }
    }
    
    /**
     * 交换两帧图片的顺序
     * @param frame1
     * @param frame2
     */
    public void onFrameSwap(int frame1, int frame2) {
        Integer desc1 = frameDesc.remove(frame1);
        Integer desc2 = frameDesc.remove(frame2);
        if (desc1 != null) {
            frameDesc.put(frame2, desc1);
        }
        if (desc2 != null) {
            frameDesc.put(frame1, desc2);
        }
    }
    
    /**
     * 删除一帧图片
     * @param frame
     */
    public void onFrameRemoved(int frame) {
        frameDesc.remove(frame);
        for (int i = frame; i < data.size(); i++) {
            Integer desc = frameDesc.remove(i + 1);
            if (desc != null) {
                frameDesc.put(i, desc);
            }
        }
    }
    
    /**
     * 生成不同格子类型对应的贴图的查找表。在计算模糊地图之前执行此方法，可以提高查找效率。
     */
    public void generateSearchTable() {
        frameSearchTable = new int[10][];
        int count = data.size();
        for (int t = 0; t < 10; t++) {
            int[] candidates = new int[count];
            int canCount = 0;
            for (int i = 0; i < count; i++) {
                if (frameDesc.containsKey(i)) {
                    int tt = frameDesc.get(i);
                    if ((tt >> 16) == t) {
                        candidates[canCount++] = i;
                    }
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
     * @return -1,empty; -2, 此类型缺失,寻找替代类型.
     */
    private int randomChooseTile(Random rand, int tileType) {
        if (tileType == -1) {
            return -1;
        }
        int[] candidates = frameSearchTable[tileType];
        if (candidates.length == 0) {
            // candidates = frameSearchTable[TILE_100];
        	return -2;
        }
        int totalPrior = 0;
        for (int i = 0; i < candidates.length; i++) {
            totalPrior += frameDesc.get(candidates[i]) & 0xFFFF;
        }
        if (totalPrior == 0) {
            return -1;
        }
        int point = rand.nextInt(totalPrior);
        for (int i = 0; i < candidates.length; i++) {
            point -= frameDesc.get(candidates[i]) & 0xFFFF;
            if (point <= 0) {
                return candidates[i];
            }
        }
        return -1;
    }
    
    /**
     * 根据格子类型随机查找出一个贴图。
     * @param rand 随机数生成器
     * @param gridType 格子类型，0-16
     * @return 数据中有2个元素：对应贴图在PipImage中的索引，翻转值
     */
    public int[] getTile(Random rand, int gridType) {
        int tileIndex = randomChooseTile(rand, TYPE_MAP[gridType][0]);
        int[] ret = new int[] { tileIndex, TYPE_MAP[gridType][1] };
        if (tileIndex == -2 && TYPE_MAP[gridType].length > 2) {
        	tileIndex = randomChooseTile(rand, TYPE_MAP[gridType][2]);
        	ret = new int[] { tileIndex, TYPE_MAP[gridType][3] };
        }
        if (tileIndex == -2) {
        	tileIndex = randomChooseTile(rand, TILE_100);
        	ret = new int[] { tileIndex, TILE_100 };
        }
        if (ret[0] == -2) {
        	ret[0] = -1;
        }
        return ret;
    }
}
