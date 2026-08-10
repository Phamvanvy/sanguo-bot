package com.pip.sanguo.data.map;

import java.util.ArrayList;
import java.util.List;

import com.pip.sanguo.data.Faction;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.ProjectData;

/**
 * 地图中的一个NPC。这里的一个NPC是用NPC模板创建的一个实例，每个实例都有唯一的ID。
 * @author lighthu
 */
public class GameMapNPC extends GameMapObject {
    /** NPC模板 */
    public NPCTemplate template;
    /** NPC名字 */
    public String name;
    /** NPC 阵营 */
    public Faction faction;
    /** 初始是否出现 */
    public boolean visible;
    /** 是否允许被攻击 */
    public boolean canAttack = true;
    /** 刷新时间(秒)，-1表示触发刷新 */
    public int refreshInterval;
    /** 是否采用动态刷新时间 */
    public boolean dynamicRefresh = true;
    /** 关联仇恨距离 */
    public int linkDistance;
    /** 是否卫兵，卫兵和怪物中立 */
    public boolean isGuard;
    /** 是否静态NPC，静态NPC一进入场景就会刷给用户 */
    public boolean isStatic;
    /** 最大生命周期，0表示永久 */
    public int liveTime;
    /** 巡逻路径，是用多个点组成的一个封闭多边形区域 */
    public List<int[]> patrolPath = new ArrayList<int[]>();
    /** 是否允许通过，如果为false，则可以阻挡玩家行动 */
    public boolean canPass = true;
    /** 是否功能NPC */
    public boolean isFunctional = false;
    /** 如果是功能NPC，说明功能名称，例如“进入拍卖行”。新版本可以支持分号分隔的多个功能 */
    public String functionName = "";
    /** 如果是功能NPC，说明启动功能的脚本。新版本支持多个，回车分隔。 */
    public String functionScript = "";
    /** 死亡后刷新的NPC，-1表示不刷新 */
    public int dieRefresh = -1;
    /** 死亡后是否广播 */
    public boolean broadcastDie = false;
    /** 寻路名称 */
    public String searchName = "";
    /** 限制版本 */
    public String revision = "";
    
    public List<Period> periods = new ArrayList<Period>();
    
    /**
     * 得到NPC的全名称，包括场景名称和NPC名称。
     */
    public String toString() {
        String realName = name;
        int pos = realName.indexOf('|');
        if (pos != -1) {
            realName = realName.substring(0, pos);
        }
        return owner.name + " -> " + realName;
    }

    /**
     * 根据ID查找一个对象的名字。
     */
    public static String toStringShort(ProjectData proj, int id) {
        if (id == -1) {
            return "无";
        }
        GameMapObject obj = findByID(proj, id);
        if (obj == null || !(obj instanceof GameMapNPC)) {
            return "未找到";
        } else {
            return ((GameMapNPC)obj).name;
        }
    }
}
