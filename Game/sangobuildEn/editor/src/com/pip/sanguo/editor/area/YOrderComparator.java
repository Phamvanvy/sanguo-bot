package com.pip.sanguo.editor.area;

import java.util.Comparator;

import com.pip.mapeditor.data.MapNPC;
import com.pip.sanguo.data.map.*;

/**
 * Y轴绘制顺序比较器，支持地图NPC、NPC和出口3种对象。
 * @author lighthu
 */
public class YOrderComparator implements Comparator<Object> {
    private boolean useLarge;
    
    public YOrderComparator(boolean useLarge) {
        this.useLarge = useLarge;
    }
    
    public int compare(Object o1, Object o2) {
        int y1 = getY(o1);
        int y2 = getY(o2);
        if (y1 < y2) {
            return -1;
        } else if (y1 == y2) {
            return 0;
        } else {
            return 1;
        }
    }
    
    private int getY(Object o) {
        if (o instanceof MapNPC) {
            return ((MapNPC)o).y;
        } else if (o instanceof GameMapNPC) {
            return useLarge ? ((GameMapNPC)o).y * 2 : ((GameMapNPC)o).y;
        } else if (o instanceof GameMapExit) {
            return ((GameMapExit)o).y;
        } else {
            return 0;
        }
    }

    public boolean equals(Object obj) {
        return this == obj;
    }
}
