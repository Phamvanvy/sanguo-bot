package com.pip.sanguo.data.map;

import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GameAreaInfo;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.quest.GameAreaCache;

/**
 * 地图中的游戏对象。
 * @author lighthu
 */
public class GameMapObject {
    /** 所属地图 */
    public GameMapInfo owner;
    /** 全局出口ID */
    public int id;
    /** X位置（像素） */
    public int x;
    /** Y位置（像素） */
    public int y;

    /**
     * 取得对象的全局ID。
     * @return
     */
    public int getGlobalID() {
        return (owner.getGlobalID() << 12) | id;
    }
    
    /**
     * 在项目中根据对象ID查找一个对象。
     * @param id
     * @return
     */
    public static GameMapObject findByID(ProjectData project, int id) {
        try {
            int areaID = (id >> 16) & 0xFFFF;
            int mapID = (id >> 12) & 0x0F;
            int npcIndex = id & 0xFFF;
            GameAreaInfo areaInfo;
            if (project.serverMode) {
                // 服务器模式，直接从GameArea对象获取GameAreaInfo
                GameArea ga = (GameArea)project.findObject(GameArea.class, areaID);
                areaInfo = ga.getAreaInfo();
            } else {
                // 编辑器中有缓存提高速度
                areaInfo = GameAreaCache.getAreaInfo(areaID);
            }
            GameMapInfo mapInfo = null;
            for (GameMapInfo mi : areaInfo.maps) {
                if (mi.id == mapID) {
                    mapInfo = mi;
                    break;
                }
            }
            for (GameMapObject obj : mapInfo.objects) {
                if (obj.id == npcIndex) {
                    return obj;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    /**
     * 根据ID查找一个对象并得到它的字符串表示。
     */
    public static String toString(ProjectData proj, int id) {
        if (id == -1) {
            return "无";
        }
        GameMapObject obj = findByID(proj, id);
        if (obj == null) {
            return "未找到";
        } else {
            return obj.toString();
        }
    }
}
