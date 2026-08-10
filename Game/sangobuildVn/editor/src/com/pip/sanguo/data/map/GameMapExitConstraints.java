package com.pip.sanguo.data.map;

import org.jdom.Element;

import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 地图出口限制。
 * @author lighthu
 */
public class GameMapExitConstraints {
    /**
     * 允许阵营，-1表示不限制。1表示魏国，2表示蜀国，3表示吴国，-2表示非魏国，-3表示非蜀国，-4表示非魏国。
     */
    public int allowFaction = -1;
    /**
     * 最下通过级别（含）
     */
    public int minLevel = 1;
    /**
     * 最大通过级别（含）
     */
    public int maxLevel = 200;
    /**
     * 最小通过军衔（含），-1表示不限制
     */
    public int minRank = -1;
    /**
     * 是否允许战斗状态通过
     */
    public boolean allowBattle = true;
    /**
     * 要求拥有的任务
     */
    public int requireQuest = -1;
    /**
     * 要求完成的任务
     */
    public int requireFinishQuest = -1;
    /**
     * 检查属性变量
     */
    public String requireProperty = "";
    /**
     * 属性变量需要达到的值
     */
    public int requirePropertyValue = 0;
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (allowFaction != -1) {
            sb.append("阵营为" + allowFaction);
        }
        if (minLevel != 1 || maxLevel != 200) {
            sb.append(minLevel + "-" + maxLevel + "级");
        }
        if (minRank != -1) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            Rank rank = (Rank)EditorApplication.getProj().findDictObject(Rank.class, minRank);
            sb.append(rank.title + "或以上军衔");
        }
        if (!allowBattle) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("非战斗状态");
        }
        if (requireQuest != -1) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            Quest quest = (Quest)EditorApplication.getProj().findObject(Quest.class, requireQuest);
            sb.append("拥有任务：" + quest.title);
        }
        if (requireFinishQuest != -1) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            Quest quest = (Quest)EditorApplication.getProj().findObject(Quest.class, requireFinishQuest);
            sb.append("完成任务：" + quest.title);
        }
        if (requireProperty.length() > 0) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(requireProperty + "达到" + requirePropertyValue);
        }
        if (sb.length() == 0) {
            return "无";
        } else {
            return sb.toString();
        }
    }
    
    public boolean checkFaction(int fac) {
        if (allowFaction == -1) {
            return true;
        }
        return allowFaction >0 ? allowFaction == fac : allowFaction != (-1 - fac);
//        return allowFaction == fac || allowFaction == -1 - fac;
    }
    
    public GameMapExitConstraints dup() {
        GameMapExitConstraints ret = new GameMapExitConstraints();
        ret.allowFaction = allowFaction;
        ret.minLevel = minLevel;
        ret.maxLevel = maxLevel;
        ret.minRank = minRank;
        ret.allowBattle = allowBattle;
        ret.requireQuest = requireQuest;
        ret.requireFinishQuest = requireFinishQuest;
        ret.requireProperty = requireProperty;
        ret.requirePropertyValue = requirePropertyValue;
        return ret;
    }
    
    public boolean equals(Object o) {
        if (o == null || !(o instanceof GameMapExitConstraints)) {
            return false;
        }
        GameMapExitConstraints oo = (GameMapExitConstraints)o;
        return allowFaction == oo.allowFaction && minLevel == oo.minLevel && maxLevel == oo.maxLevel && minRank == oo.minRank &&
            allowBattle == oo.allowBattle && requireQuest == oo.requireQuest && 
            requireFinishQuest == oo.requireFinishQuest && requireProperty.equals(oo.requireProperty) &&
            requirePropertyValue == oo.requirePropertyValue;
    }
    
    public void load(Element elem) {
        try {
            allowFaction = Integer.parseInt(elem.getAttributeValue("faction"));
        } catch (Exception e) {
        }
        minLevel = Integer.parseInt(elem.getAttributeValue("minlevel"));
        maxLevel = Integer.parseInt(elem.getAttributeValue("maxlevel"));
        minRank = Integer.parseInt(elem.getAttributeValue("minrank"));
        allowBattle = "1".equals(elem.getAttributeValue("allowbattle"));
        requireQuest = Integer.parseInt(elem.getAttributeValue("requirequest"));
        try {
            requireFinishQuest = Integer.parseInt(elem.getAttributeValue("requirefinishquest"));
        } catch (Exception e) {
        }
        requireProperty = elem.getAttributeValue("requireproperty");
        if (requireProperty == null) {
            requireProperty = "";
        }
        try {
            requirePropertyValue = Integer.parseInt(elem.getAttributeValue("requirepropertyvalue"));
        } catch (Exception e) {
        }
    }
    
    public Element save() {
        Element elem = new Element("constraints");
        if (allowFaction != -1) {
            elem.addAttribute("faction", String.valueOf(allowFaction));
        }
        elem.addAttribute("minlevel", String.valueOf(minLevel));
        elem.addAttribute("maxlevel", String.valueOf(maxLevel));
        elem.addAttribute("minrank", String.valueOf(minRank));
        elem.addAttribute("allowbattle", allowBattle ? "1" : "0");
        elem.addAttribute("requirequest", String.valueOf(requireQuest));
        elem.addAttribute("requirefinishquest", String.valueOf(requireFinishQuest));
        elem.addAttribute("requireproperty", requireProperty);
        elem.addAttribute("requirepropertyvalue", String.valueOf(requirePropertyValue));
        return elem;
    }
}
