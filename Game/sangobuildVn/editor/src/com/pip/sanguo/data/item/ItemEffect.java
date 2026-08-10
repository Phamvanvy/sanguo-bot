package com.pip.sanguo.data.item;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Attribute;
import org.jdom.Element;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.editor.item.ItemEffectPropertySource;

/**
 * 物品（Item）或者技能（Skill）的一个使用效果
 * @author Joy Yan
 *
 */
public class ItemEffect {
    /**
     * 使用效果：回复生命
     */
    public static final int TYPE_ADDHP = 0;
    /**
     * 使用效果：回复法力
     */
    public static final int TYPE_ADDMP = 1;
    /**
     * 使用效果：使用技能
     */
    public static final int TYPE_SKILL = 2;
    /**
     * 使用效果：获得物品
     */
    public static final int TYPE_GETITEM = 3;
    /**
     * 使用效果：触发任务
     */
    public static final int TYPE_QUEST = 4;
    /**
     * 使用效果：复活
     */
    public static final int TYPE_RELIVE = 5;
    /**
     * 使用效果：召唤坐骑
     */
    public static final int TYPE_HORSE = 6;
    /**
     * 使用效果：学习技能
     */
    public static final int TYPE_LEARNSKILL = 7;
    /**
     * 使用效果：扩展
     */
    public static final int TYPE_EXTEND = 8;
    /**
     * 使用效果：获得称号
     */
    public static final int TYPE_TITLE = 9;
    /**
     * 使用效果：坐骑喂食
     */
    public static final int TYPE_FEEDHORSE = 10;
    /**
     * 使用效果：获得坐骑
     */
    public static final int TYPE_GETHORSE = 11;
    /**
     * 使用效果：掉落物品
     */
    public static final int TYPE_DROPGROUP = 12;
    /**
     * 使用效果：传送。
     */
    public static final int TYPE_TELEPORT = 13;
    /**
     * 使用效果：学习打造配方。
     */
    public static final int TYPE_FORMULA = 14;
    /**
     * 使用效果：夫妻传送。
     */
    public static final int TYPE_MARRIAGETELEPORT = 15;
    /**
     * 使用效果：强制离婚。
     */
    public static final int TYPE_DIVORCE = 16;
    /**
     * 使用效果：扩展背包。
     */
    public static final int TYPE_EXTENDBAG = 17;
    /**
     * 使用效果：洗技能点。
     */
    public static final int TYPE_REFRESHSKILLPOINTS = 18;
    /**
     * 使用效果：扩展仓库。
     */
    public static final int TYPE_EXTENDSTORAGE = 19;
    /**
     * 使用效果：获得经验。
     */
    public static final int TYPE_EXP = 20;
    /**
     * 使用效果：获得坐骑经验。
     */
    public static final int TYPE_HORSEEXP = 21;
    /**
     * 使用效果：获得荣誉（战功）。
     */
    public static final int TYPE_HONOR = 22;
    /**
     * 使用效果：获得声望。
     */
    public static final int TYPE_CREDIT = 23;
    /**
     * 使用效果：获得金钱。
     */
    public static final int TYPE_MONEY = 24;
    /**
     * 使用效果：使用国家技能。
     */
    public static final int TYPE_NATIONSKILL = 25;
    
    /**
     * 使用效果：重置属性点
     */
    public static final int TYPE_REFRESH_PROPERTY = 26;
    
    /**
     * 使用效果：修理身上的所有装备，包括当前骑的马的装备
     */
    public static final int TYPE_REPAIR_EQUIPMENTS = 27;
    
    /**
     * 元宝卡
     */
    public static final int TYPE_IMONEY_CARD = 28;
    
    /**
     * 灯谜
     */
    public static final int TYPE_RIDDLE = 29;
    
    /**
     * 退排战场
     */
    public static final int TYPE_UNSIGNUP = 30;
    
    /**
     * 获得在线经验
     */
    public static final int TYPE_CLICKEXP = 31;
    
    /**
     * 召唤城战车
     */
    public static final int TYPE_TONGBATTLECAR = 32;
    
    /**
     * 使用效果：恢复国公生命
     */
    public static final int TYPE_ADDKINGHP = 33;
    
    /**
     * 使用获取金钱
     */
    public static final int TYPE_CLICKGETMONEY = 34;

    /**
     * 使用效果：恢复/扣除行动力
     */
    public static final int TYPE_ACTIVEPOWER = 35;
    
    /**
     * 使用效果：获得元宝（仅限于flash版本）
     */
    public static final int TYPE_GETIMONEY = 36;
    
    /**
     * 使用扣除物品
     */
    public static final int TYPE_USEDECITEM = 37;
    
    /**
     * 限制有军团才能使用。
     */
    public static final int TYPE_LIMITTONG = 38;
    /**
     * 使用效果: 获得技能点
     */
    public static final int TYPE_SKILL_POINT = 39;
    
    /**
     * 活动类型
     */
    public static final int TYPE_ACTIVITY = 40;
    
    /**
     * 使用扣除元宝
     */
    public static final int TYPE_DECIMONEY = 41;
    /**
    
    /**
     * 召唤塔防建筑
     */
    public static final int TYPE_TOWERDEFEND = 42;
    /**
     * 使用效果：恢复/扣除贡献度
     */
    public static final int TYPE_CONTRIBUTEPOINT = 43;
    /**
     * 使用效果：获得法宝
     */
    public static final int TYPE_TALISMAN = 44;
    /**
     * 使用效果：扩充坐骑栏
     */
    public static final int TYPE_EXTEND_HORSE_BAG_MAX = 45;
    /**
     * 支持的使用效果列表
     */
    public static final String[] EFFECT_NAMES = { 
        "回血", "回蓝", "使用技能", "获得物品", "触发任务", "复活", "召唤坐骑", "学会技能", 
        "扩展", "获得称号", "坐骑喂食", "获得坐骑", "掉落物品", "传送", "学习打造配方",
        "夫妻传送", "强制离婚", "扩展背包", "洗技能点", "扩展仓库", "获得经验", "获得坐骑经验",
        "获得荣誉(战功)", "获得声望", "获得金钱", "使用国家技能","重置属性点","修理装备","元宝卡",
        "灯谜","退排战场","获得在线经验","召唤城战车","无敌大血瓶","使用获取金钱","恢复/扣除行动力","获得元宝",
        "使用扣除物品", "军团专用", "获得技能点","活动类型","使用扣除元宝","使用获得斗阵建筑","恢复/扣除贡献度", 
        "使用获得法宝","扩充坐骑栏"
    };
    
    
    /**
     * 使用效果类型
     */
    public int effectType;
    
    /**
     * 使用效果参数列表
     */
    public Map<String, String> param = new HashMap<String, String>();

    public boolean depends(DataObject obj) {
        // TODO Auto-generated method stub
        return false;
    }

    public DataObject duplicate() {
        // TODO Auto-generated method stub
        return null;
    }

    public void load(Element elem) {
        if(elem == null){
            return;
        }
        
        effectType = Integer.parseInt(elem.getAttributeValue("type"));
        List list = elem.getAttributes();
        for(int i=0;i<list.size();i++){
            Attribute attr = (Attribute)list.get(i);
            if(attr.getName().equals("type")){
                continue;
            }
            
            param.put(attr.getName(), attr.getValue());
        }
    }

    public Element save() {
        Element elem = new Element("effect");
        elem.addAttribute(new Attribute("type", String.valueOf(effectType)));
        Iterator<String> keys = param.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            elem.addAttribute(new Attribute(key, param.get(key)));
        }
        return elem;
    }

    public void update(DataObject obj) {
        // TODO Auto-generated method stub
        
    }
    
    public void resetParam(){
        param.clear();
        
        switch(effectType){
        case TYPE_ADDHP:
            param.put("amount", "0");
            param.put("percent", "0");
            break;
        case TYPE_ADDMP:
            param.put("amount", "0");
            param.put("percent", "0");
            break;
        case TYPE_SKILL:
            param.put("skill", "0");
            param.put("level", "0");
            param.put("usebuffs", "0");
            break;
        case TYPE_GETITEM:
            param.put("item", "0");
            param.put("count", "0");
            break;
        case TYPE_QUEST:
            param.put("quest", "0");
            param.put("questshu", "0");
            param.put("questwu", "0");
            break;
        case TYPE_RELIVE:
            param.put("percent", "15.0");
            break;
        case TYPE_HORSE:
            param.put("horseid", "0");
            break;
        case TYPE_LEARNSKILL:
            param.put("skill", "0");
            param.put("level", "0");
            break;
        case TYPE_EXTEND:
            param.put("script", "");
            break;
        case TYPE_TITLE:
            param.put("title", "0");
            break;
        case TYPE_FEEDHORSE:
            param.put("feedvalue", "0");
            break;
        case TYPE_GETHORSE:
            param.put("name", "未命名");
            param.put("level", "1");
            param.put("imageid", "0");
            param.put("horsetype", "1");
            break;
        case TYPE_DROPGROUP:
            param.put("dropgroup1", "-1");
            param.put("dropgroup2", "-1");
            param.put("controlvar", "");
            param.put("switchcount", "0");
            param.put("mail", "0");
            param.put("useitem", "-1");
            param.put("useitemcount", "0");
            break;
        case TYPE_TELEPORT:
            param.put("weimapid", "0");
            param.put("weix", "0");
            param.put("weiy", "0");
            param.put("shumapid", "0");
            param.put("shux", "0");
            param.put("shuy", "0");
            param.put("wumapid", "0");
            param.put("wux", "0");
            param.put("wuy", "0");
            break;
        case TYPE_FORMULA:
            param.put("formula", "0");
            break;
        case TYPE_MARRIAGETELEPORT:
        case TYPE_DIVORCE:
            break;
        case TYPE_EXTENDBAG:
            param.put("count", "0");
            break;
        case TYPE_REFRESHSKILLPOINTS:
            break;
        case TYPE_EXTENDSTORAGE:
            param.put("count", "0");
            break;
        case TYPE_EXP:
            param.put("amount", "0.0");
            param.put("table", "");
            break;
        case TYPE_HORSEEXP:
            param.put("amount", "0.0");
            param.put("table", "");
            break;
        case TYPE_HONOR:
            param.put("amount", "0.0");
            param.put("table", "");
            break;
        case TYPE_CREDIT:
            param.put("amount", "0.0");
            param.put("table", "");
            break;
        case TYPE_MONEY:
            param.put("amount", "0.0");
            param.put("table", "");
            break;
        case TYPE_NATIONSKILL:
            param.put("skillid", "0");
            break;
        case TYPE_REFRESH_PROPERTY:
            break;
        case TYPE_REPAIR_EQUIPMENTS:
            break;
        case TYPE_IMONEY_CARD:
            break;
        case TYPE_RIDDLE:
            break;
        case TYPE_UNSIGNUP:
            break;
        case TYPE_CLICKEXP:
            break;
        case TYPE_TONGBATTLECAR:
            break;
        case TYPE_ADDKINGHP:
            param.put("rate","0");
            break;
        case TYPE_CLICKGETMONEY:
            param.put("money", "0");
            break;
        case TYPE_ACTIVEPOWER:
            param.put("value", "0");
            break;
        case TYPE_CONTRIBUTEPOINT:
            param.put("value", "0");
            break;
        case TYPE_GETIMONEY:
            param.put("value", "0");
            break;
        case TYPE_USEDECITEM:
            param.put("itemid", "0");
            break;
        case TYPE_SKILL_POINT:
            param.put("skillpoint", "0");
            break;
        case TYPE_ACTIVITY:
            param.put("ty", "0");
            break;
        case TYPE_TOWERDEFEND:
            break;
        case TYPE_TALISMAN:
            param.put("talismanindex", "0");
            break;
        case TYPE_DECIMONEY:
            param.put("value", "0");
            break;
        case TYPE_EXTEND_HORSE_BAG_MAX:
            param.put("value", "0");
            break;
        }
    }
    
    public int getIntParam(String name){
        try {
            return Integer.parseInt(param.get(name));
        } catch (Exception e) {
            return 0;
        }
    }
    
    public float getFloatParam(String name){
        try {
            return Float.parseFloat(param.get(name));
        } catch (Exception e) {
            return 0.0f;
        }
    }

    public String toString(){
        return effectType + " ." + EFFECT_NAMES[effectType];
    }
}
