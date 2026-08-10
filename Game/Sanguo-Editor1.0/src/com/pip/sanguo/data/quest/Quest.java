package com.pip.sanguo.data.quest;

import org.jdom.*;
import java.util.*;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.GameArea;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.quest.pqe.ExpressionList;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.quest.RichTextEditor;
import com.pip.util.Utils;

/**
 * 一个游戏任务。这个类只包含任务的基本属性，而任务的详细内容包含在对应的任务文件中。
 */
public class Quest extends DataObject {
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * 对应的任务文件。
     */
    public java.io.File source;
    /**
     * 任务类型：0 - 普通，1 - 场景。
     */
    public int type;
    /**
     * 任务分类: 0 - 普通, 1 - 主线任务, 2-支线任务, 3-副本任务, 4-每日任务, 5-随从任务, 6-不可用
     */
    public int questType = 0;
    /**
     * 场景任务对应的地区ID.
     */
    public int areaID;
    /**
     * 重复类型：0 - 不可重复、1 - 每月可完成1次、2 - 每周可完成1次、3 - 每天可完成1次、4 - 无限重复
     */
    public int repeatType;
    /**
     * 授予任务的NPC。-1表示没有。
     */
    public int startNPC = -1;
    /**
     * 交还任务的NPC。-1表示没有。
     */
    public int finishNPC = -1;
    /**
     * 任务级别。
     */
    public int level;
    /**
     * 接受任务的条件。
     */
    public String condition = "";
    /**
     * 接受任务时需要几个空的背包空间。
     */
    public int requireFreeBag;
    /**
     * 任务完成的条件，这个条件是由所有任务目标的条件联合组成的。
     */
    public String finishCondition = "";
    /**
     * 接受任务时的描述。
     */
    public String preDescription = "";
    /**
     * 完成任务时的描述。
     */
    public String postDescription = "";
    /**
     * 未完成任务时的描述。
     */
    public String unfinishDescription = "";
    /**
     * 任务目标。
     */
    public List<QuestTarget> targets = new ArrayList<QuestTarget>();
    /**
     * 任务奖励分支。
     */
    public List<QuestRewardSet> rewards = new ArrayList<QuestRewardSet>();
    /**
     * 任务目标全部达成时是否需要在客户端显示通知消息。
     */
    public boolean notifyFinish = true;
    /**
     * 是否在接受时自动共享给其他玩家。
     */
    public boolean autoShare = false;
    /**
     * 是否是开启状态
     */
    public boolean active = true;

    public Quest(ProjectData owner) {
        this.owner = owner;
    }

    public int getID() {
        return id;
    }
    
    public String getStartNpcName(){
        String npcName = "";
        if(startNPC == -1){
            npcName = "未设置";
        }
        else{
            GameMapObject startNpc = GameMapNPC.findByID(owner, startNPC);
            if (startNpc instanceof GameMapNPC) {
                npcName = startNpc.toString();
            } else {
                npcName = "错误对象";
            }
        }
        return npcName;
    }
    
    public String getTargetsCondition(){
        if(targets == null || targets.size() == 0){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<targets.size(); i++){
            sb.append((targets.get(i)).condition);
            if(i != targets.size() - 1)
                sb.append("\n");
        }
//        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    public String getRewardsItem(){
        if(rewards == null || rewards.size() == 0){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<rewards.size(); i++){
            sb.append((rewards.get(i)).getRewardItem());
            if(i != rewards.size() - 1)
                sb.append("\n");
        }
//        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    public String getComments() {
        if (type == 1) {
            // 场景任务
            GameArea ga = (GameArea)owner.findObject(GameArea.class, areaID);
            if (ga == null) {
                return "场景任务(未知场景)";
            } else {
                return "场景任务(" + ga.title + ")";
            }
        } else {
            String ret = "";
            if (startNPC == -1) {
                ret = "起始：未设置";
            } else {
                GameMapObject startNpc = GameMapNPC.findByID(owner, startNPC);
                if (startNpc instanceof GameMapNPC) {
                    ret = "起始：" + startNpc.toString();
                } else {
                    ret = "起始：错误对象";
                }
            }
            ret += "，";
            if (finishNPC == -1) {
                ret += "结束：未设置";
            } else {
                GameMapObject finishNpc = GameMapNPC.findByID(owner, finishNPC);
                if (finishNpc instanceof GameMapNPC) {
                    ret += "结束：" + finishNpc.toString();
                } else {
                    ret += "结束：错误对象";
                }
            }
            return ret;
        }
    }

    public String toString() {
        return id + ": " + title;
    }

    public boolean equals(Object o) {
        return this == o;
    }

    public void update(DataObject obj) {
        Quest oo = (Quest) obj;
        id = oo.id;
        source = oo.source;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        
        type = oo.type;
        questType = oo.questType;
        areaID = oo.areaID;
        startNPC = oo.startNPC;
        repeatType = oo.repeatType;
        finishNPC = oo.finishNPC;
        level = oo.level;
        condition = oo.condition;
        requireFreeBag = oo.requireFreeBag;
        finishCondition = oo.finishCondition;
        preDescription = oo.preDescription;
        postDescription = oo.postDescription;
        unfinishDescription = oo.unfinishDescription;
        notifyFinish = oo.notifyFinish;
        autoShare = oo.autoShare;
        active = oo.active;
        targets.clear();
        for (QuestTarget target : oo.targets) {
            QuestTarget newTarget = target.duplicate();
            newTarget.owner = this;
            targets.add(newTarget);
        }
        rewards.clear();
        for (QuestRewardSet reward : oo.rewards) {
            QuestRewardSet newReward = reward.duplicate();
            newReward.owner = this;
            rewards.add(newReward);
        }
    }

    public DataObject duplicate() {
        Quest ret = new Quest(owner);
        ret.update(this);
        return ret;
    }
    
    @Override
    public boolean changed(DataObject obj) {
        // 因为没有缓存QuestInfo对象，导致QuestInfo可能无法比较，所以只能全部更新。
        return true;
    }


    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        source = new java.io.File(owner.baseDir, "Quests/" + elem.getAttributeValue("source"));
        title = elem.getAttributeValue("title");
        description = elem.getChild("desc").getText();
        categoryName = elem.getAttributeValue("category");
        if (categoryName == null) {
            categoryName = "";
        }
        
        type = Integer.parseInt(elem.getAttributeValue("type"));
        try {
            questType = Integer.parseInt(elem.getAttributeValue("questtype"));
        } catch (Exception e) {
            questType = 0;
        }
        areaID = Integer.parseInt(elem.getAttributeValue("areaid"));
        startNPC = Utils.parseHex(elem.getAttributeValue("startnpc"));
        String repeatTypeStr = elem.getAttributeValue("repeattype");
        if (repeatTypeStr == null || repeatTypeStr.equals("")) {
            repeatTypeStr = "0";
        }
        repeatType = Integer.parseInt(repeatTypeStr);
        finishNPC = Utils.parseHex(elem.getAttributeValue("finishnpc"));
        level = Integer.parseInt(elem.getAttributeValue("level"));
        condition = elem.getAttributeValue("condition");
        try {
            requireFreeBag = Integer.parseInt(elem.getAttributeValue("requirefreebag"));
        }
        catch (Exception e) {
        }
        finishCondition = elem.getAttributeValue("finishcondition");
        preDescription = elem.getChild("predesc").getText();
        postDescription = elem.getChild("postdesc").getText();
        try {
            unfinishDescription = elem.getChild("unfindesc").getText();
        } catch (Exception e) {
            unfinishDescription = description;
        }
        notifyFinish = !("0".equals(elem.getAttributeValue("notifyfinish")));
        autoShare = "1".equals(elem.getAttributeValue("autoshare"));
        active = !("0".equals(elem.getAttributeValue("active")));

        List targetElems = elem.getChildren("target");
        for (int i = 0; i < targetElems.size(); i++) {
            QuestTarget target = new QuestTarget(this);
            target.load((Element) targetElems.get(i));
            targets.add(target);
        }

        List rewardElems = elem.getChildren("rewardset");
        for (int i = 0; i < rewardElems.size(); i++) {
            QuestRewardSet target = new QuestRewardSet(this);
            target.load((Element) rewardElems.get(i));
            rewards.add(target);
        }
    }

    public Element save() {
        Element ret = new Element("quest");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("source", source.getName());
        ret.addAttribute("title", title);
        Element descElem = new Element("desc");
        descElem.setText(description);
        ret.addContent(descElem);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        ret.addAttribute("type", String.valueOf(type));
        if (questType != 0) {
            ret.addAttribute("questtype", String.valueOf(questType));
        }
        ret.addAttribute("areaid", String.valueOf(areaID));
        ret.addAttribute("startnpc", "0x" + Integer.toHexString(startNPC));
        ret.addAttribute("repeattype", String.valueOf(repeatType));
        ret.addAttribute("finishnpc", "0x" + Integer.toHexString(finishNPC));
        ret.addAttribute("level", String.valueOf(level));
        ret.addAttribute("condition", condition);
        ret.addAttribute("requirefreebag", String.valueOf(requireFreeBag));
        ret.addAttribute("finishcondition", finishCondition);
        descElem = new Element("predesc");
        descElem.setText(preDescription);
        ret.addContent(descElem);
        descElem = new Element("postdesc");
        descElem.setText(postDescription);
        ret.addContent(descElem);
        descElem = new Element("unfindesc");
        descElem.setText(unfinishDescription);
        ret.addContent(descElem);
        ret.addAttribute("notifyfinish", notifyFinish ? "1" : "0");
        ret.addAttribute("autoshare", autoShare ? "1" : "0");
        ret.addAttribute("active", active ? "1" : "0");

        for (QuestTarget target : targets) {
            ret.addContent(target.save());
        }

        for (QuestRewardSet reward : rewards) {
            ret.addContent(reward.save());
        }
        return ret;
    }

    public boolean depends(DataObject obj) {
        return false;
    }

    /**
     * 查找一个任务的名字。
     * 
     * @param project
     * @param questID
     * @return
     */
    public static String toString(ProjectData project, int questID) {
        Quest q = (Quest) project.findObject(Quest.class, questID);
        if (q == null) {
            return "未知任务";
        }
        else {
            return q.toString();
        }
    }

    /**
     * 校验混合格式字符串，把其中的NPC引用和场景位置引用更新成最新的版本。
     * @param text
     * @return
     */
    public static String validateMixedText(ProjectData proj, String text) throws Exception {
        StringBuilder sb = new StringBuilder();
        int start = 0;
        while (true) {
            int pos1 = text.indexOf("<l>", start);
            int pos2 = text.indexOf("<n>", start);
            if (pos1 == -1 && pos2 == -1) {
                sb.append(text.substring(start));
                break;
            } else if (pos1 != -1 && (pos2 == -1 || pos1 < pos2)) {
                pos2 = text.indexOf("</l>", pos1);
                if (pos2 == -1) {
                    sb.append(text.substring(start));
                    break;
                }
                sb.append(text.substring(start, pos1));
                sb.append("<l>");
                sb.append(validateLocationText(proj, text.substring(pos1 + 3, pos2)));
                sb.append("</l>");
                start = pos2 + 4;
            } else {
                pos1 = pos2;
                pos2 = text.indexOf("</n>", pos1);
                if (pos2 == -1) {
                    sb.append(text.substring(start));
                    break;
                }
                sb.append(text.substring(start, pos1));
                sb.append("<n>");
                sb.append(validateNPCText(proj, text.substring(pos1 + 3, pos2)));
                sb.append("</n>");
                start = pos2 + 4;
            }
        }
        return sb.toString();
    }
    
    /*
     * <l>192,许田镇:35,26</l>
     */
    private static String validateLocationText(ProjectData proj, String text) throws Exception {
        int pos1 = text.indexOf(',');
        if (pos1 == -1) {
            throw new Exception("描述字符串引用的场景格式错误。");
        }
        int pos2 = text.lastIndexOf(':');
        if (pos2 == -1) {
            throw new Exception("描述字符串引用的场景格式错误。");
        }
        String loc = text.substring(pos2 + 1);
        int mapID;
        try {
            mapID = Integer.parseInt(text.substring(0, pos1));
        } catch (Exception e) {
            throw new Exception("描述字符串引用的场景格式错误。");
        }
        GameMapInfo gmi = GameMapInfo.findByID(proj, mapID);
        if (gmi == null) {
            throw new Exception("描述字符串中引用的场景不存在：" + mapID);
        }
        if (gmi.name.contains(":")) {
            throw new Exception("描述字符串中引用的场景名字包含':'：" + mapID);
        }
        String showName = gmi.name;
        pos1 = showName.indexOf('(');
        pos2 = showName.indexOf('|');
        int splitPos = -1;
        if (pos1 == -1) {
            splitPos = pos2;
        } else {
            if (pos2 == -1) {
                splitPos = pos1;
            } else {
                splitPos = Math.min(pos1, pos2);
            }
        }
        if (splitPos != -1) {
            showName = showName.substring(0, splitPos);
        }
        return mapID + "," + showName + ":" + loc;
    }
    
    /*
     * <n>917508,卫兵(成都城外:19,22)</n>
     */
    private static String validateNPCText(ProjectData proj, String text) throws Exception {
        int pos1 = text.indexOf(',');
        if (pos1 == -1) {
            throw new Exception("描述字符串引用的NPC格式错误。");
        }
        int npcID;
        try {
            npcID = Integer.parseInt(text.substring(0, pos1));
        } catch (Exception e) {
            throw new Exception("描述字符串引用的NPC格式错误。");
        }
        GameMapNPC npc = (GameMapNPC)GameMapNPC.findByID(proj, npcID);
        if (npc == null) {
            throw new Exception("描述字符串中引用的NPC不存在：" + npcID);
        }
        if (npc.owner.name.contains(":")) {
            throw new Exception("描述字符串中引用的场景名称中不能包含':'符号。");
        }
        String showName = npc.name;
        if (showName.contains("|")) {
            showName = showName.substring(0, showName.indexOf('|'));
        }
        String mapName = npc.owner.name;
        if (mapName.contains("|")) {
            mapName = mapName.substring(0, mapName.indexOf('|'));
        }
        return npc.getGlobalID() + "," + showName + "(" + mapName + ":" +
        (npc.x / 8) + "," + (npc.y / 8) + ")";
    }
    
    /**
     * 把用到的文本字符串中的NPC引用和场景引用都更新一遍。
     */
    public void validateMixedText() throws Exception {
        try {
            this.preDescription = validateMixedText(this.owner, this.preDescription);
            this.description = validateMixedText(this.owner, this.description);
            this.postDescription = validateMixedText(this.owner, this.postDescription);
            this.unfinishDescription = validateMixedText(this.owner, this.unfinishDescription);
            for (QuestTarget target : this.targets) {
                target.description = validateMixedText(this.owner, target.description);
                target.path = validateMixedText(this.owner, target.path);
                target.hint = validateMixedText(this.owner, target.hint);
            }
            
            QuestInfo qi = new QuestInfo(this);
            qi.load();
            for (QuestTrigger qt : qi.triggers) {
                ExpressionList el = ExpressionList.fromString(qt.condition);
                el.validateMixedText(this.owner);
                qt.condition = el.toString();
                el = ExpressionList.fromString(qt.action);
                el.validateMixedText(this.owner);
                qt.action = el.toString();
            }
            qi.save();
        } catch (Exception e) {
            throw new Exception(e.getMessage() + ", 任务ID：" + this.id);
        }
    }
}
