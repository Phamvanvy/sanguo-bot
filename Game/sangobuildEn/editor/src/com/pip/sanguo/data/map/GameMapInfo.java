package com.pip.sanguo.data.map;

import java.io.File;
import java.util.*;
import org.jdom.*;

import com.pip.mapeditor.data.GameMap;
import com.pip.mapeditor.data.MapFile;
import com.pip.sanguo.data.Faction;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.GameAreaInfo;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.quest.GameAreaCache;
import com.pipimage.utils.Utils;

/**
 * 一个关卡中一个地图的附加信息，包括名称、NPC和出口等信息。这些信息被保存在关卡目录中的info.xml文件里。
 * @author lighthu
 */
public class GameMapInfo {
    /** 所属关卡 */
    public GameArea owner;
    /** 关卡内地图ID */
    public int id;
    /** 地图名称 */
    public String name;
    /** 魏国复活点 */
    public int[] renascenceWei;
    /** 蜀国复活点 */
    public int[] renascenceShu;
    /** 吴国复活点 */
    public int[] renascenceWu;
    /** 是否中立（不允许攻击对方阵营玩家）*/
    public boolean neutral = false;
    /** 是否允许己方PK */
    public boolean allowDuel = true;
    /** 是否启动被杀保护: 0 - 不保护、1 - 1次保护、2 - 2次保护 */
    public int protect = 0;
    /** 是否允许跟随 */
    public boolean allowFollow = true;
    /** 是否隔离阵营 */
    public boolean splitFaction = false;
    /** 声音文件ID */
    public int backgroundMusic;
    /** 地图中的对象 */
    public List<GameMapObject> objects = new ArrayList<GameMapObject>();
    /** 此地图的路径查找工具（仅服务器模式有效）*/
    protected PathFinder pathFinder;
    /** 阵营 */
    public Faction faction;
    /** 最大容纳人数，0表示不限制 */
    public int maxPlayer = 0;

    public GameMapInfo(GameArea owner) {
        this.owner = owner;
        
        renascenceWei = new int[3];
        renascenceWei[0] = -1;
        renascenceShu = new int[3];
        renascenceShu[0] = -1;
        renascenceWu = new int[3];
        renascenceWu[0] = -1;
    }
    
    public int getGlobalID() {
        return (owner.id << 4) | id;
    }
    
    public void load(Element elem) throws Exception {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        name = elem.getAttributeValue("name");
        neutral = "1".equals(elem.getAttributeValue("neutral"));
        allowDuel = !("0".equals(elem.getAttributeValue("allowduel")));
        try {
            protect = Integer.parseInt(elem.getAttributeValue("protect"));
        } catch (Exception e) {
            protect = 0;
        }
        allowFollow = !("0".equals(elem.getAttributeValue("allowfollow")));
        splitFaction = "1".equals(elem.getAttributeValue("splitfaction"));
        try {
            backgroundMusic = Integer.parseInt(elem.getAttributeValue("backgroundmusic"));
        } catch (Exception e) {
        }
        List list = elem.getChildren("exit");
        for (Object obj : list) {
            objects.add(loadExit((Element)obj));
        }
        list = elem.getChildren("npc");
        for (Object obj : list) {
            objects.add(loadNPC((Element)obj));
        }
        
        Element renascence = elem.getChild("renascenceWei");
        if(renascence != null){
            renascenceWei[0] = Integer.parseInt(renascence.getAttributeValue("mapId"));
            renascenceWei[1] = Integer.parseInt(renascence.getAttributeValue("x"));
            renascenceWei[2] = Integer.parseInt(renascence.getAttributeValue("y"));
        }
        renascence = elem.getChild("renascenceShu");
        if(renascence != null){
            renascenceShu[0] = Integer.parseInt(renascence.getAttributeValue("mapId"));
            renascenceShu[1] = Integer.parseInt(renascence.getAttributeValue("x"));
            renascenceShu[2] = Integer.parseInt(renascence.getAttributeValue("y"));
        }
        renascence = elem.getChild("renascenceWu");
        if(renascence != null){
            renascenceWu[0] = Integer.parseInt(renascence.getAttributeValue("mapId"));
            renascenceWu[1] = Integer.parseInt(renascence.getAttributeValue("x"));
            renascenceWu[2] = Integer.parseInt(renascence.getAttributeValue("y"));
        }
        
        Attribute att = elem.getAttribute("faction");
        if(att == null){
            this.faction = null;
        }else{
            this.faction = (Faction)owner.owner.findDictObject(Faction.class, att.getIntValue());
        }
        
        if (elem.getAttributeValue("maxplayer") != null) {
            maxPlayer = Integer.parseInt(elem.getAttributeValue("maxplayer"));
        }
//        long t = System.currentTimeMillis();
        
        // 如果是服务器模式，生成路径查找工具
        if (owner.owner.serverMode) {
            pathFinder = new PathFinder(this);
            File bufFile = new File(owner.owner.baseDir, "PathFinder/" + getGlobalID() + ".pth");
            File mapFile = new File(owner.source, "game.map");
            if (bufFile.exists() && bufFile.lastModified() > mapFile.lastModified()) {
                byte[] bytes = Utils.loadFileData(bufFile);
                try {
                    pathFinder.loadPathBuffer(bytes);
                } catch (Exception e) {
                    pathFinder.buildPathBuffer();
                    Utils.saveFileData(bufFile, pathFinder.savePathBuffer());
                }
            } else {
                pathFinder.buildPathBuffer();
                Utils.saveFileData(bufFile, pathFinder.savePathBuffer());
            }
        }
//        System.out.println("timeall: " + (System.currentTimeMillis() - t));
    }
    
    public Element save() throws Exception {
        // 保存前对所有的对象按ID进行排序
        sortObjects();
        
        Element elem = new Element("map");
        elem.addAttribute("id", String.valueOf(id));
        elem.addAttribute("name", name);
        elem.addAttribute("neutral", neutral ? "1" : "0");
        elem.addAttribute("allowduel", allowDuel ? "1" : "0");
        elem.addAttribute("protect", String.valueOf(protect));
        elem.addAttribute("allowfollow", allowFollow ? "1" : "0");
        elem.addAttribute("splitfaction", splitFaction ? "1" : "0");
        elem.addAttribute("backgroundmusic", String.valueOf(backgroundMusic));
        if(faction != null)
            elem.addAttribute("faction", String.valueOf(faction.id));
        if (maxPlayer != 0) {
            elem.addAttribute("maxplayer", String.valueOf(maxPlayer));
        }
        
        Element renascence = new Element("renascenceWei");
        renascence.addAttribute("mapId", String.valueOf(renascenceWei[0]));
        renascence.addAttribute("x", String.valueOf(renascenceWei[1]));
        renascence.addAttribute("y", String.valueOf(renascenceWei[2]));
        elem.getMixedContent().add(renascence);
        
        renascence = new Element("renascenceShu");
        renascence.addAttribute("mapId", String.valueOf(renascenceShu[0]));
        renascence.addAttribute("x", String.valueOf(renascenceShu[1]));
        renascence.addAttribute("y", String.valueOf(renascenceShu[2]));
        elem.getMixedContent().add(renascence);
        
        renascence = new Element("renascenceWu");
        renascence.addAttribute("mapId", String.valueOf(renascenceWu[0]));
        renascence.addAttribute("x", String.valueOf(renascenceWu[1]));
        renascence.addAttribute("y", String.valueOf(renascenceWu[2]));
        elem.getMixedContent().add(renascence);
        
        for (GameMapObject obj : objects) {
            if (obj instanceof GameMapExit) {
                elem.getMixedContent().add(saveExit((GameMapExit)obj));
            } else if (obj instanceof GameMapNPC) {
                elem.getMixedContent().add(saveNPC((GameMapNPC)obj));
            }
        }
        return elem;
    }
    
    /*
     * 把地图中所有的游戏对象按ID进行排序。
     */
    private void sortObjects() {
        int count = objects.size();
        for (int i = 0; i < count - 1; i++) {
            for (int j = i + 1; j < count; j++) {
                GameMapObject obj1 = objects.get(i);
                GameMapObject obj2 = objects.get(j);
                if (obj1.id > obj2.id) {
                    objects.set(i, obj2);
                    objects.set(j, obj1);
                }
            }
        }
    }
    
    private GameMapExit loadExit(Element elem) {
        GameMapExit ret = new GameMapExit();
        ret.owner = this;
        ret.id = Integer.parseInt(elem.getAttributeValue("id"));
        ret.x = Integer.parseInt(elem.getAttributeValue("x"));
        ret.y = Integer.parseInt(elem.getAttributeValue("y"));
        ret.targetMap = Integer.parseInt(elem.getAttributeValue("targetmap"));
        ret.targetX = Integer.parseInt(elem.getAttributeValue("targetx"));
        ret.targetY = Integer.parseInt(elem.getAttributeValue("targety"));
        ret.showName = !("0".equals(elem.getAttributeValue("showname")));
        try {
            ret.exitType = Integer.parseInt(elem.getAttributeValue("exittype"));
        } catch (Exception e) {
        }
        ret.positionVarName = elem.getAttributeValue("posvarname");
        if (ret.positionVarName == null) {
            ret.positionVarName = "";
        }
        Element celem = elem.getChild("constraints");
        if (celem != null) {
            ret.constraints.load(celem);
        }
        return ret;
    }
    
    private Element saveExit(GameMapExit exit) {
        Element elem = new Element("exit");
        elem.addAttribute("id", String.valueOf(exit.id));
        elem.addAttribute("x", String.valueOf(exit.x));
        elem.addAttribute("y", String.valueOf(exit.y));
        elem.addAttribute("targetmap", String.valueOf(exit.targetMap));
        elem.addAttribute("targetx", String.valueOf(exit.targetX));
        elem.addAttribute("targety", String.valueOf(exit.targetY));
        elem.addAttribute("showname", exit.showName ? "1" : "0");
        elem.addAttribute("exittype", String.valueOf(exit.exitType));
        elem.addAttribute("posvarname", exit.positionVarName);
        elem.addContent(exit.constraints.save());
        return elem;
    }
    
    private GameMapNPC loadNPC(Element elem) {
        GameMapNPC ret = new GameMapNPC();
        ret.owner = this;
        ret.id = Integer.parseInt(elem.getAttributeValue("id"));
        ret.x = Integer.parseInt(elem.getAttributeValue("x"));
        ret.y = Integer.parseInt(elem.getAttributeValue("y"));
        int tid = Integer.parseInt(elem.getAttributeValue("template"));
        ret.template = (NPCTemplate)owner.owner.findObject(NPCTemplate.class, tid);
        ret.name = elem.getAttributeValue("name");
        int fid = Integer.parseInt(elem.getAttributeValue("faction"));
        /* 修改阵营时代码
        switch(fid){
            case 0:
                fid = 1;
                break;
            case 1:
                fid = 2;
                break;
            case 2:
                fid = 3;
                break;
            case 3:
                fid = 4;
                break;
            case 4:
                fid = 5;
                break;
            case 5:
                fid = 0;
                break;
        }//*/
        ret.faction = (Faction)owner.owner.findDictObject(Faction.class, fid);
        ret.visible = "1".equals(elem.getAttributeValue("visible"));
        ret.canAttack = !("0".equals(elem.getAttributeValue("canattack")));
        ret.refreshInterval = Integer.parseInt(elem.getAttributeValue("refreshinterval"));
        ret.dynamicRefresh = !("0".equals(elem.getAttributeValue("dynamicrefresh")));
        try {
            ret.linkDistance = Integer.parseInt(elem.getAttributeValue("linkdistance"));
        } catch (Exception e) {
            ret.linkDistance = 0;
        }
        ret.isGuard = "1".equals(elem.getAttributeValue("isguard"));
        ret.isStatic = "1".equals(elem.getAttributeValue("isstatic"));
        try {
            ret.liveTime = Integer.parseInt(elem.getAttributeValue("livetime"));
        } catch (Exception e) {
            ret.liveTime = 0;
        }
        String[] pathPts = elem.getAttributeValue("patrolpath").split(";");
        for (String s : pathPts) {
            if (s.length() == 0) {
                continue;
            }
            String[] secs = s.split(",");
            ret.patrolPath.add(new int[] { Integer.parseInt(secs[0]), Integer.parseInt(secs[1]) });
        }
        ret.canPass = !("0".equals(elem.getAttributeValue("canpass")));
        ret.isFunctional = "1".equals(elem.getAttributeValue("functional"));
        ret.functionName = elem.getAttributeValue("funcname");
        if (ret.functionName == null) {
            ret.functionName = "";
        }
        ret.functionScript = elem.getAttributeValue("funcscript");
        if (ret.functionScript == null) {
            ret.functionScript = "";
        }
        try {
            ret.dieRefresh = Integer.parseInt(elem.getAttributeValue("dierefresh"));
        } catch (Exception e) {
            ret.dieRefresh = -1;
        }
        ret.broadcastDie = "1".equals(elem.getAttributeValue("broadcastdie"));
        ret.searchName = elem.getAttributeValue("searchname");
        if (ret.searchName == null) {
            ret.searchName = "";
        }
        String periods = elem.getAttributeValue("period");
        ret.periods = new ArrayList<Period>();
        if(periods != null && periods.length() != 0){
            Period[] ps = Period.parse(periods);
            for(Period p:ps){
                ret.periods.add(p);
            }
        }
        ret.revision = elem.getAttributeValue("revision");
        if (ret.revision == null) {
            ret.revision = "";
        }
        return ret;
    }

    private Element saveNPC(GameMapNPC npc) throws Exception{
        Element elem = new Element("npc");
        elem.addAttribute("id", String.valueOf(npc.id));
        elem.addAttribute("x", String.valueOf(npc.x));
        elem.addAttribute("y", String.valueOf(npc.y));
        elem.addAttribute("template", String.valueOf(npc.template.id));
        elem.addAttribute("name", npc.name);
        elem.addAttribute("faction", String.valueOf(npc.faction.id));
        elem.addAttribute("visible", npc.visible ? "1" : "0");
        elem.addAttribute("canattack", npc.canAttack ? "1" : "0");
        elem.addAttribute("refreshinterval", String.valueOf(npc.refreshInterval));
        elem.addAttribute("dynamicrefresh", npc.dynamicRefresh ? "1" : "0");
        elem.addAttribute("linkdistance", String.valueOf(npc.linkDistance));
        elem.addAttribute("isguard", npc.isGuard ? "1" : "0");
        elem.addAttribute("isstatic", npc.isStatic ? "1" : "0");
        elem.addAttribute("livetime", String.valueOf(npc.liveTime));
        StringBuffer buf = new StringBuffer();
        for (int[] pt : npc.patrolPath) {
            if (buf.length() > 0) {
                buf.append(";");
            }
            buf.append(pt[0] + "," + pt[1]);
        }
        elem.addAttribute("patrolpath", buf.toString());
        elem.addAttribute("canpass", npc.canPass ? "1" : "0");
        elem.addAttribute("functional", npc.isFunctional ? "1" : "0");
        
        if(npc.isFunctional && (npc.functionName == null || "".equals(npc.functionName) || npc.functionScript == null || "".equals(npc.functionScript))){
            throw new Exception("功能NPC的功能名称和脚本不能为空！");
        }
        elem.addAttribute("funcname", npc.functionName);
        elem.addAttribute("funcscript", npc.functionScript);
        elem.addAttribute("dierefresh", String.valueOf(npc.dieRefresh));
        elem.addAttribute("broadcastdie", npc.broadcastDie ? "1" : "0");
        if (npc.searchName.length() > 0) {
            elem.addAttribute("searchname", npc.searchName);
        }
        if (npc.periods.size() > 0){
            StringBuilder sb = new StringBuilder(100);
            Period[] ps = new Period[npc.periods.size()];
            npc.periods.toArray(ps);
            elem.addAttribute("period",Period.getString(ps));
        }
        if (npc.revision.length() > 0) {
            elem.addAttribute("revision", npc.revision);
        }
        return elem;
    }
    
    /**
     * 根据ID查找一个对象。
     */
    public GameMapObject findObject(int id) {
        for (GameMapObject mo : objects) {
            if (mo.id == id) {
                return mo;
            }
        }
        return null;
    }

    /**
     * 在项目中根据对象ID查找一个对象。
     * @param id
     * @return
     */
    public static GameMapInfo findByID(ProjectData project, int id) {
        try {
            int areaID = (id >> 4) & 0xFFFF;
            int mapID = id & 0x0F;
            GameAreaInfo areaInfo;
            if (project.serverMode) {
                GameArea area = (GameArea)project.findObject(GameArea.class, areaID);
                areaInfo = area.getAreaInfo();
            } else {
                areaInfo = GameAreaCache.getAreaInfo(areaID);
            }
            for (GameMapInfo mi : areaInfo.maps) {
                if (mi.id == mapID) {
                    return mi;
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    /**
     * 把一个地图坐标转换为文字显示。
     * @param project 项目数据
     * @param location 地图坐标，元素依次是：地图ID、X（像素）、Y（像素）
     * @param useVirtualCoord 是否使用虚拟坐标系（单位为码）
     * @return
     */
    public static String locationToString(ProjectData project, int[] location, boolean useVirtualCoord) {
        GameMapInfo mi = findByID(project, location[0]);
        int x = location[1];
        int y = location[2];
        if (useVirtualCoord) {
            x /= 8;
            y /= 8;
        }
        if (mi == null) {
            return "未知场景(" + x + "," + y + ")";
        } else {
            return mi.name + "(" + x + "," + y + ")";
        }
    }
    
    /**
     * 得到一个场景的名字。
     * @param project 项目数据
     * @param sceneID 场景ID
     * @return
     */
    public static String toString(ProjectData project, int sceneID) {
        GameMapInfo mi = findByID(project, sceneID);
        if (mi == null) {
            return "未知场景";
        } else {
            return mi.name;
        }
    }
    
    /**
     * 取得预先生成的路径查找工具。
     * @return
     */
    public PathFinder getPathFinder() {
        return pathFinder;
    }
    
    /**
     * 检查地图的视线遮挡信息，判断从地图上的某点能否看到另外一个点。此方法只有在服务器模式才能调用。
     * @param fromx 起始点坐标（像素）
     * @param fromy 起始点坐标（像素）
     * @param tox 结束点坐标（像素）
     * @param toy 结束点坐标（像素）
     * @return
     */
    public boolean canSee(int fromx, int fromy, int tox, int toy) {
        GameMap gameMap = owner.getMapFile().getMaps().get(id);
        try {
            fromx >>= 3;
            fromy >>= 3;
            tox >>= 3;
            toy >>= 3;
            if (fromx == tox) {
                // 对于垂直这种情况，无法计算tgt值，所以需要特殊处理
                int delta = 1;
                if (toy < fromy) {
                    delta = -1;
                }
                for (int y = fromy; true; y += delta) {
                    if (gameMap.eyesightBlock[y][fromx]) {
                        return false;
                    }
                    if (y == toy) {
                        break;
                    }
                }
                return true;
            }
            
            // 计算tgt值，然后计算直线上每个点是否阻挡
            int tgt = (toy - fromy) * 10000 / (tox - fromx);
            int delta = 1;
            if (tox < fromx) {
                delta = -1;
            }
            int lastx = fromx;
            int lasty = fromy;
            for (int x = fromx + delta; true; x += delta) {
                int y = (x - fromx) * tgt / 10000 + fromy;
                if (x == tox) {
                    y = toy;
                }
                if (lasty > y) {
                    for (int yy = y; yy <= lasty; yy++) {
                        if (gameMap.eyesightBlock[yy][lastx]) {
                            return false;
                        }
                    }
                } else {
                    for (int yy = lasty; yy <= y; yy++) {
                        if (gameMap.eyesightBlock[yy][lastx]) {
                            return false;
                        }
                    }
                }
                if (x == tox) {
                    break;
                }
                lastx = x;
                lasty = y;
            }
            if (gameMap.eyesightBlock[toy][tox]) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 测试两点之间视线轨迹的方法。
     */
    public List<int[]> getEyesightPath(int fromx, int fromy, int tox, int toy) {
        List<int[]> ret = new ArrayList<int[]>();
        try {
            fromx >>= 3;
            fromy >>= 3;
            tox >>= 3;
            toy >>= 3;
            if (fromx == tox) {
                // 对于垂直这种情况，无法计算tgt值，所以需要特殊处理
                int delta = 1;
                if (toy < fromy) {
                    delta = -1;
                }
                for (int y = fromy; true; y += delta) {
                    ret.add(new int[] { fromx, y });
                    if (y == toy) {
                        break;
                    }
                }
                return ret;
            }
            
            // 计算tgt值，然后计算直线上每个点是否阻挡
            int tgt = (toy - fromy) * 10000 / (tox - fromx);
            int delta = 1;
            if (tox < fromx) {
                delta = -1;
            }
            int lastx = fromx;
            int lasty = fromy;
            for (int x = fromx + delta; true; x += delta) {
                int y = (x - fromx) * tgt / 10000 + fromy;
                if (x == tox) {
                    y = toy;
                }
                if (lasty > y) {
                    for (int yy = y; yy <= lasty; yy++) {
                        ret.add(new int[] { lastx, yy });
                    }
                } else {
                    for (int yy = lasty; yy <= y; yy++) {
                        ret.add(new int[] { lastx, yy });
                    }
                }
                if (x == tox) {
                    break;
                }
                lastx = x;
                lasty = y;
            }
            ret.add(new int[] { tox, toy });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
