package com.pip.mapeditor.data;

/**
 * 一个地图NPC对象。
 * @author lighthu
 */
public class MapNPC implements Comparable<MapNPC> {
    /** 库模式下animateSet的索引,使用cts节点的hashCode属性值*/
	public int animateSetRef;
	/** NPC动画在动画文件中的索引 */
    public int animate;
    /** X位置(像素) */
    public int x;
    /** Y位置(像素) */
    public int y;
    
    public boolean isShow = true;
    
    public int compareTo(MapNPC o) {
        if (y < o.y) {
            return -1;
        } else if (y == o.y) {
            return 0;
        } else {
            return 1;
        }
    }
    
    public MapNPC dup() {
    	MapNPC ret = new MapNPC();
    	ret.animateSetRef = animateSetRef;
    	ret.animate = animate;
    	ret.x = x;
    	ret.y = y;
    	return ret;
    }
}
