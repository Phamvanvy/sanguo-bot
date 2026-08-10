package com.pip.itimes.server.gift;


import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import com.pip.itimes.server.bean.Gift;
import com.pip.itimes.server.stage.PlayerData;


public class OnlyGiftGroup{

	private int id;//物品赠送id
	private int beginLevel;//赠送限制最低级别
	private int endLevel;//赠送限制最高几倍
	private int maxCount;//赠送限制的最多次数
	private boolean valid;//赠送的有效性

	private String message_error; //礼品提示错误
	private String message_maxcount;//最大领取数据量
	private String message_give;//获得物品不足的提示
	private String message_bag;//背包满的提示
	private String message_content;//赠送活动的详细内容提示 
	private String message_title;//赠送活动的列表提示 
    
	
	private static final String REPLACE_BEGIN_LEVEL = "beginlevel";
	private static final String REPLACE_END_LEVEL = "endlevel";
	private static final String REPLACE_MAX= "max";
	private static final String REPLACE_RECOUNT = "recount";
	private static final String REPLACE_GIVEITEM = "giveitem";
	    
    public String getMessage_title() {
		return message_title;
	}

	public void setMessage_title(String message_title) {
		this.message_title = message_title;
	}

	private Vector<OnlyGiftDefine> gifts = new Vector<OnlyGiftDefine>();//天赋组
    
    public Vector<OnlyGiftDefine> getGifts() {
		return gifts;
	}

	public void setGifts(Vector<OnlyGiftDefine> gifts) {
		this.gifts = gifts;
	}

	public void addOnlyGift(OnlyGiftDefine onlyGiftDefine){
        gifts.add(onlyGiftDefine);
    }

    public GiftDefine getAvailableGift(){
        for(int i = 0; i < gifts.size(); i++){
        	OnlyGiftDefine gift = gifts.get(i);
        }
        
        return null;
    }
    public OnlyGiftGroup(boolean valid){
        this.valid = valid;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBeginLevel() {
		return beginLevel;
	}

	public void setBeginLevel(int beginLevel) {
		this.beginLevel = beginLevel;
	}

	public int getEndLevel() {
		return endLevel;
	}

	public void setEndLevel(int endLevel) {
		this.endLevel = endLevel;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getMessage_error() {
		return message_error;
	}

	public void setMessage_error(String message_error) {
		this.message_error = message_error;
	}


	public String getMessage_maxcount() {
		return message_maxcount;
	}

	public void setMessage_maxcount(String message_maxcount) {
		this.message_maxcount = message_maxcount;
	}
	public String getMessage_give() {
		return message_give;
	}

	public void setMessage_give(String message_give) {
		this.message_give = message_give;
	}

	public String getMessage_bag() {
		return message_bag;
	}

	public void setMessage_bag(String message_bag) {
		this.message_bag = message_bag;
	}

	public String getMessage_content() {
		return message_content;
	}

	public void setMessage_content(String message_content) {
		this.message_content = message_content;
	}
	  public static String getReplaceMessage(String message, Gift gift, OnlyGiftDefine onlyGiftDefine, OnlyGiftGroup onlyGiftGroup, PlayerData player){
	        String result = message;
	        
	        if(gift != null && message.indexOf(REPLACE_MAX) >= 0){
	            result = result.replaceAll(REPLACE_MAX, String.valueOf(gift.getCount()));
	        }
	        if(gift != null && message.indexOf(REPLACE_RECOUNT) >= 0){
	            result = result.replaceAll(REPLACE_RECOUNT, String.valueOf(gift.getRcount()));
	        }
	        if(gift != null && message.indexOf(REPLACE_BEGIN_LEVEL) >= 0){
	            result = result.replaceAll(REPLACE_BEGIN_LEVEL, String.valueOf(onlyGiftGroup.getBeginLevel()));
	        }
	        if(gift != null && message.indexOf(REPLACE_END_LEVEL) >= 0){
	            result = result.replaceAll(REPLACE_END_LEVEL, String.valueOf(onlyGiftGroup.getEndLevel()));
	        }
	        if(gift != null && message.indexOf(REPLACE_GIVEITEM) >= 0){
	            result = result.replaceAll(REPLACE_GIVEITEM, String.valueOf(onlyGiftDefine.getGiveItemString()));
	        }
	        return result;
	  }
  }