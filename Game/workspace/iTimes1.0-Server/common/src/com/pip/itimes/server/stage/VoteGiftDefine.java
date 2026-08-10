package com.pip.itimes.server.stage;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.pip.itimes.server.bean.Gift;
import com.pip.itimes.server.gift.GiftGroup;
import com.pip.itimes.server.util.Utils;

public class VoteGiftDefine {
	private int id;
    private int beginLevel;
    private int endLevel;
    private byte isImoneyItem;
    private Vector<TemplateGrid> needItems = new Vector<TemplateGrid>();
    private Vector<TemplateGrid> giveItems = new Vector<TemplateGrid>();
    /**
     * 每个物品添加的点数
     */
    private Map<Integer,Integer> itemsVotePoint = new HashMap<Integer, Integer>();
    
    public int getItemsVotePoint(int itemid) {
		return itemsVotePoint.get(itemid);
	}
	public void addItemsVotePoint(int itemid, int votePoint){
    	itemsVotePoint.put(itemid, votePoint);
    }
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getBeginLevel(){
        return beginLevel;
    }

    public void setBeginLevel(int beginLevel){
        this.beginLevel = beginLevel;
    }

    public int getEndLevel(){
        return endLevel;
    }

    public void setEndLevel(int endLevel){
        this.endLevel = endLevel;
    }
    public TemplateGrid[] getNeedItems(){
        TemplateGrid[] result = new TemplateGrid[needItems.size()];
        needItems.toArray(result);

        return result;
    }

    public void addNeedItem(int itemId, int count){
        TemplateGrid item = new TemplateGrid(Items.getTemplate(itemId), count);
        needItems.add(item);
    }

    public TemplateGrid[] getGiveItems(){
        TemplateGrid[] result = new TemplateGrid[giveItems.size()];
        giveItems.toArray(result);

        return result;
    }

    public void addGiveItems(int itemId, int count){
        TemplateGrid item = new TemplateGrid(Items.getTemplate(itemId), count);
        giveItems.add(item);
    }
    
    public boolean isLevelOK(int level){
        if(level >= beginLevel && level <= endLevel){
            return true;
        }else{
            return false;
        }
    }

    public void setIsImoneyItem (byte isImoneyItem) {
    	this.isImoneyItem = isImoneyItem;
    }
    
    public byte getIsImoneyItem () {
    	return isImoneyItem;
    }
}
