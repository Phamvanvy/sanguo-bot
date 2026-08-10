package com.pip.sanguo.data;

import java.util.*;

import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.map.PathFinder;

/**
 * 自动寻路工具。
 * @author lighthu
 */
public class AutoPathFinder {
    protected ProjectData owner;
    // 所有地图的出口信息
    protected Map<Integer, GameMapExit[]> mapLinks;
    
    /**
     * 初始化自动寻路工具，找出所有场景之间的通达关系。
     * @param proj
     */
    public AutoPathFinder(ProjectData proj) {
        owner = proj;
        mapLinks = new HashMap<Integer, GameMapExit[]>();
        for (DataObject dobj : proj.getDataListByType(GameArea.class)) {
            GameArea ga = (GameArea)dobj;
            for (GameMapInfo gmi : ga.getAreaInfo().maps) {
                // 找出所有出口
                List<GameMapExit> links = new ArrayList<GameMapExit>();
                for (GameMapObject gmo : gmi.objects) {
                    if (gmo instanceof GameMapExit) {
                        links.add((GameMapExit)gmo);
                    }
                }
                GameMapExit[] arr = new GameMapExit[links.size()];
                links.toArray(arr);
                mapLinks.put(gmi.getGlobalID(), arr);
            }
        }
    }
    
    /**
     * 查找任意地图任意两点之间的路径。
     * @param smap 源地图全局ID
     * @param sx 源X位置（像素）
     * @param sy 源Y位置（像素）
     * @param tmap 目标地图全局ID
     * @param tx 目标X位置（像素）
     * @param ty 目标Y位置（像素）
     * @return 如果两点在同一场景并且可以通达，返回空数组。如果路径未找到，返回null。如果找到路径
     *    但需要通过几次场景跳转才能到达，返回路径上的所有出口。
     */
    public GameMapExit[] findPath(int faction, int smap, int sx, int sy, int tmap, int tx, int ty) {
        // 同场景寻路特殊处理
        if (smap == tmap) {
            if (checkPath(smap, sx, sy, tx, ty)) {
                return new GameMapExit[0];
            }
        }

        // 在开始场景中，找出从起始点能够到达的所有出口，加入备选路径表
        List<GameMapExit[]> findBuffer = new ArrayList<GameMapExit[]>();
        Set<GameMapExit> visited = new HashSet<GameMapExit>();
        GameMapExit[] arr = mapLinks.get(smap);
        if (arr == null) {
            return null;
        }
        GameMapInfo gmi = GameMapInfo.findByID(owner, smap);
        if (gmi == null) {
            return null;
        }
        PathFinder pf = gmi.getPathFinder();
        for (int i = 0; i < arr.length; i++) {
            if (!arr[i].constraints.checkFaction(faction)) {
                continue;
            }
            if (pf.canReach(sx, sy, i)) {
                findBuffer.add(new GameMapExit[] { arr[i] });
                visited.add(arr[i]);
            }
        }
        
        // 广度遍历所有可能的路径，直到找出能到达目标点的路径，或者候选路径表为空
        while (findBuffer.size() > 0) {
            GameMapExit[] path = findBuffer.remove(0);
            GameMapExit lastNode = path[path.length - 1];
            int em = lastNode.targetMap;
            int ex = lastNode.targetX;
            int ey = lastNode.targetY;
            
            // 检查是否已经到达目标地图，且可以通达目标点
            if (em == tmap && checkPath(tmap, ex, ey, tx, ty)) {
                return path;
            }
            
            // 继续向前一步，找出目标场景的所有出口（需要避免重复遍历）
            arr = mapLinks.get(em);
            if (arr == null) {
                continue;
            }
            gmi = GameMapInfo.findByID(owner, em);
            if (gmi == null) {
                continue;
            }
            pf = gmi.getPathFinder();
            for (int i = 0; i < arr.length; i++) {
                if (!arr[i].constraints.checkFaction(faction)) {
                    continue;
                }
                if (!visited.contains(arr[i]) && pf.canReach(ex, ey, i)) {
                    GameMapExit[] newarr = new GameMapExit[path.length + 1];
                    System.arraycopy(path, 0, newarr, 0, path.length);
                    newarr[path.length] = arr[i];
                    findBuffer.add(newarr);
                    visited.add(arr[i]);
                }
            }
        }
        return null;
    }
    
//    /**
//     * 检查一个地图上的某一个点与任何一个出口之间是否有通道
//     */
//    public boolean checkPathWithExit(int map,int sx,int sy){
//        GameMapExit[] exits = mapLinks.get(map);
//        if(exits == null){
//            return false;
//        }
//        GameMapInfo gmi = GameMapInfo.findByID(owner, map);
//        if(gmi == null){
//            return false;
//        }
//        PathFinder pf = gmi.getPathFinder();
//        boolean find = false;
//        for(int i=0;i<exits.length;i++){
//            if(pf.canReach(sx, sy, i)){
//                find = true;
//                return true;
//            }
//            if(find){
//                break;
//            }
//        }
//        return false;
//    }
    
    /**
     * 检查一个地图的两个点之间是否有通道（可以到达同一个出口）。
     */
    public boolean checkPath(int map, int sx, int sy, int tx, int ty) {
       GameMapExit[] exits = mapLinks.get(map);
       if (exits == null) {
           return false;
       }
       GameMapInfo gmi = GameMapInfo.findByID(owner, map);
       if (gmi == null) {
           return false;
       }
       PathFinder pf = gmi.getPathFinder();
       for (int i = 0; i < exits.length; i++) {
           if (pf.canReach(sx, sy, i) && pf.canReach(tx, ty, i)) {
               return true;
           }
       }
       return false;
    }
}
