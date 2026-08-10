package com.pip.sanguo.editor.quest;

import java.util.HashMap;
import java.util.List;

import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GameAreaInfo;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 本类缓存所有在任务编辑器中载入过的地图信息，以加快加载速度。当任务地图修改时，应更新此缓存。
 * @author lighthu
 */
public class GameAreaCache {
    // 地图数据缓存
    private static HashMap<Integer, GameAreaInfo> areaInfoCache = new HashMap<Integer, GameAreaInfo>();
 
    /**
     * 取得一个关卡的地图信息。如果这个关卡的地图信息没有在缓存中，则载入之并加入缓存。
     * @param areaID
     * @return
     */
    public static GameAreaInfo getAreaInfo(int areaID) {
        GameAreaInfo areaInfo = areaInfoCache.get(areaID);
        if (areaInfo == null) {
            GameArea area = (GameArea)EditorApplication.getInstance().getProjectData().findObject(GameArea.class, areaID);
            if (area == null) {
                return null;
            }
            areaInfo = new GameAreaInfo(area);
            try {
                areaInfo.load();
                areaInfoCache.put(areaID, areaInfo);
            } catch (Exception e) {
                return null;
            }
        }
        return areaInfo;
    }

    /**
     * 清除缓存的关卡信息。当一个关卡被修改时需要调用此方法清除缓存，下次使用时再载入。
     * @param areaID
     */
    public static void clearAreaInfo(int areaID) {
        areaInfoCache.remove(areaID);
    }
}
