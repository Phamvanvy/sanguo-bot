package com.pip.sanguo.data.map;

/**
 * 地图中的一个出口。
 * @author lighthu
 */
public class GameMapExit extends GameMapObject {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_RECORD = 1;
    public static final int TYPE_RECALL = 2;
    public static final int TYPE_INTERNAL = 3;
    
    /** 目标地图ID */
    public int targetMap;
    /** 目标X位置（像素）*/
    public int targetX;
    /** 目标Y位置（像素）*/
    public int targetY;
    /** 是否显示名字 */
    public boolean showName = true;
    /** 通道类型：0 - 普通、1 - 记录当前位置、2 - 返回记录位置、3 - 寻路用 */
    public int exitType;
    /** 记录位置的变量名（玩家变量） */
    public String positionVarName = "";
    /** 通过限制 */
    public GameMapExitConstraints constraints = new GameMapExitConstraints();

    /**
     * 得到全名称，包括场景名称和出口位置。
     */
    public String toString() {
        return owner.name + " -> 传送点" + this.id + "(" + x + "," + y + ")";
    }
}
