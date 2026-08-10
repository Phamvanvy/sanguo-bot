package com.pip.sanguo.data.quest;

import org.jdom.*;

import com.pip.sanguo.editor.EditorApplication;

/**
 * 任务奖励的一个奖励项。一个奖励项可能是金钱、荣誉、经验、物品或者装备。
 * @author lighthu
 */
public class QuestRewardItem {
	/** 奖励类型：金钱 */
	public static final int REWARD_MONEY = 1;
	/** 奖励类型：经验 */
	public static final int REWARD_EXP = 2;
	/** 奖励类型：荣誉 */
	public static final int REWARD_HONOR = 3;
	/** 奖励类型：声望 */
	public static final int REWARD_CREDIT = 4;
	/** 奖励类型：物品或装备 */
	public static final int REWARD_ITEM = 101;
	
	public QuestRewardSet owner;
	/**
	 * 奖励类型。
	 */
	public int rewardType = REWARD_MONEY;
	/**
	 * 奖励参数：如果是金钱、荣誉或经验，这是奖励的数量；如果是物品或装备，这是奖励物品的模板ID。
	 */
	public int rewardValue;
	/**
	 * 如果奖励的是物品，这个参数指定奖励的数量。
	 */
	public int itemCount;
	
	public QuestRewardItem(QuestRewardSet own) {
		owner = own;
	}

    public boolean equals(Object o) {
        return this == o;
    }
    
    public String toString() {
    	switch (rewardType) {
    	case REWARD_MONEY:
    		return "金钱" + rewardValue;
    	case REWARD_EXP:
    		return "经验" + rewardValue;
    	case REWARD_HONOR:
    		return "荣誉" + rewardValue;
    	case REWARD_CREDIT:
    	    return "声望" + rewardValue;
    	case REWARD_ITEM:
    		return "物品 " + EditorApplication.getProj().findItemOrEquipment(rewardValue) + " x" + itemCount; 
    	default:
    		return "未知";
    	}
    }
    
    public void load(Element elem) {
    	rewardType = Integer.parseInt(elem.getAttributeValue("type"));
    	rewardValue = Integer.parseInt(elem.getAttributeValue("value"));
    	try {
    		itemCount = Integer.parseInt(elem.getAttributeValue("count"));
    	} catch (Exception e) {
    		itemCount = 1;
    	}
    }
    
    public Element save() {
        Element ret = new Element("rewarditem");
        ret.addAttribute("type", String.valueOf(rewardType));
        ret.addAttribute("value", String.valueOf(rewardValue));
        if (itemCount != 1) {
        	ret.addAttribute("count", String.valueOf(itemCount));
        }
        return ret;
    }
    
    public QuestRewardItem duplicate() {
    	QuestRewardItem ret = new QuestRewardItem(owner);
    	ret.rewardType = rewardType;
    	ret.rewardValue = rewardValue;
    	ret.itemCount = itemCount;
    	return ret;
    }
}
