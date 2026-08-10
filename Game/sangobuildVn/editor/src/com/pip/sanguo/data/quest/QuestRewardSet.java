package com.pip.sanguo.data.quest;

import org.jdom.*;
import java.util.*;

/**
 * 任务的一个奖励分支。一个分支可以包含多个物品。
 */
public class QuestRewardSet {
	public Quest owner;
	/**
	 * 分支ID。
	 */
	public int id;
	/**
	 * 是否是任务结束奖励。
	 */
	public boolean isFinishReward;
	/**
	 * 奖励项。
	 */
	public List<QuestRewardItem> rewardItems = new ArrayList<QuestRewardItem>();

    public QuestRewardSet(Quest owner) {
        this.owner = owner;
    }

    public int getID() {
        return id;
    }
    
    public String toString() {
        return "分支" + id + (isFinishReward ? "(结束奖励)" : "");
    }

    public boolean equals(Object o) {
        return this == o;
    }
    
    public String getRewardItem(){
        if(rewardItems == null || rewardItems.size() == 0){
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<rewardItems.size(); i++){
            sb.append(rewardItems.get(i).toString());
            if(i != rewardItems.size() - 1)
                sb.append("\n");
        }
//        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        isFinishReward = "true".equals(elem.getAttributeValue("isfinishreward"));
        List itemElems = elem.getChildren("rewarditem");
        for (int i = 0; i < itemElems.size(); i++) {
        	QuestRewardItem item = new QuestRewardItem(this);
        	item.load((Element)itemElems.get(i));
        	rewardItems.add(item);
        }
    }
    
    public Element save() {
        Element ret = new Element("rewardset");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("isfinishreward", isFinishReward ? "true" : "false");
        for (QuestRewardItem item : rewardItems) {
        	ret.addContent(item.save());
        }
        return ret;
    }
    
    public QuestRewardSet duplicate() {
    	QuestRewardSet ret = new QuestRewardSet(owner);
    	ret.id = id;
    	ret.isFinishReward = isFinishReward;
    	for (QuestRewardItem item : rewardItems) {
    		QuestRewardItem newItem = item.duplicate();
    		newItem.owner = ret;
    		ret.rewardItems.add(newItem);
    	}
    	return ret;
    }
}
