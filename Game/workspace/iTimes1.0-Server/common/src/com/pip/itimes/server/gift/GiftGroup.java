package com.pip.itimes.server.gift;


import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import com.pip.itimes.server.bean.Gift;
import com.pip.itimes.server.stage.PlayerData;


public class GiftGroup{
    private int id;
    private Date beginTime;
    private Date endTime;
    private int canSeeType;
    private int giveType;

    private boolean valid;

    
    public boolean isValid() {
		return valid;
	}
    private boolean versionvalid;
    public boolean isVersionValid() {
		return versionvalid;
	}
	private String message_error;
    private String message_group;
    private String message_gift;
    private String message_count;
    private String message_allcount;
    private String message_repeat;
    private String message_time;
    private String message_item;
    private String message_give;
    private String message_bag;
    private String message_mail;
    private String message_about;
    private String message_aboutmsg;
    
    private Vector<GiftDefine> gifts = new Vector<GiftDefine>();
    //jwp add
    private boolean directwayCanSee;
    public boolean isDirectwayCanSee() {
		return directwayCanSee;
	}

	public void setDirectwayCanSee(boolean directwayCanSee) {
		this.directwayCanSee = directwayCanSee;
	}
	public void setVersionValid(boolean versionvalid) {
		this.versionvalid = versionvalid;
	}
	//jwp end
    private static final String REPLACE_COUNT = "acount";
    private static final String REPLACE_MAX_COUNT = "max";
    private static final String REPLACE_REPEAT_COUNT = "rcount";
    private static final String REPLACE_MAX_REPEAT = "repeat";
    private static final String REPLACE_BEGIN_TIME = "begintime";
    private static final String REPLACE_END_TIME = "endtime";
    private static final String REPLACE_NEED_ITEM = "needitme";
    private static final String REPLACE_GIVE_ITEM = "giveitme";
    
    public static final int GIFT_AVAILABLE = 0;
    public static final int GIFT_ERROR_COUNT = 1;
    public static final int GIFT_ERROR_REPEAT = 2;
    public static final int GIFT_ERROR_TIME = 3;
    public static final int GIFT_ERROR_ITEM = 4;
    public static final int GIFT_ERROR_BAG = 5;
    public static final int GIFT_INFO_SUPERQ = 6;
    public static final int GIFT_INFO_SUPERQ_nomal = 7;
    /**
     * 只显示满足条件的记录，满足条件只判断时间，级别等等，不考虑物品情况
     */
    public static final int CAN_SEE_CAN_DO = 0;

    /**
     * 不满足条件的记录也会显示
     */
    public static final int CAN_SEE_ALL = 1;

    /**
     * 通过下发GetItem直接加到背包中，如果背包满，则此次操作无效并给予相应的提示但不记录次数
     */
    public static final int GIVE_BY_PUSH = 0;

    /**
     * 通过邮件发送，如果有多个物品则发多封邮件
     */
    public static final int GIVE_BY_MAIL = 1;
    /**
     * 超级QQ发送，判断是否为超级QQ，2009年1月15日0时前注册的，大于等于20级
     */
    public static final int GIVE_BY_SUPERQ = 99;
    /**
     * 超级QQ发送，大于等于10级
     */
    public static final int GIVE_BY_SUPERQ_nomal = 98;

    public GiftGroup(boolean valid){
        this.valid = valid;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public Date getBeginTime(){
        return beginTime;
    }

    public void setBeginTime(long beginTime){
        if(valid){
            if(beginTime <= 0){
                this.beginTime = getTimeData(0);
            }else{
                this.beginTime = getTimeData(beginTime);
            }
        }else{
            this.beginTime = getTimeData(0);
        }
    }

    public Date getEndTime(){
        return endTime;
    }

    public void setEndTime(long endTime){
        if(valid){
            if(endTime <= 0){
                this.endTime = getTimeData(Long.MAX_VALUE);
            }else{
                this.endTime = getTimeData(endTime);
            }
        }else{
            this.endTime = getTimeData(0);
        }
    }

    public int getCanSeeType(){
        return canSeeType;
    }

    public void setCanSeeType(int canSeeType){
        this.canSeeType = canSeeType;
    }

    public int getGiveType(){
        return giveType;
    }

    public void setGiveType(int giveType){
        this.giveType = giveType;
    }

    public void addGift(GiftDefine gift){
        gifts.add(gift);
    }

    public GiftDefine getAvailableGift(int level){
        for(int i = 0; i < gifts.size(); i++){
            GiftDefine gift = gifts.get(i);

            if(gift.isLevelOK(level)){
                return gift;
            }
        }
        
        return null;
    }
    
    public GiftDefine[] getAvailableGifts(int level){
    	GiftDefine[] giftDefines = new GiftDefine[gifts.size()];
        for(int i = 0,k = 0; i < gifts.size(); i++, k++){
            GiftDefine gift = gifts.get(i);

            if(gift.isLevelOK(level)){
            	giftDefines[k] = gift;
            }
        }
        
        return giftDefines;
    }
    private Date getTimeData(long time){
        int year = (int)(time / 100000000);
        int month = (int)((time / 1000000) % 100 - 1);
        int day = (int)((time / 10000) % 100);
        int hour = (int)((time / 100) % 100);
        int minute = (int)(time % 100);

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public String getMessage_error(){
        return message_error;
    }
    
    public void setMessage_error(String message_error){
        this.message_error = message_error;
    }
    
    public String getMessage_group(Gift gift, GiftDefine giftDefine, PlayerData player){
        return getReplaceMessage(message_group, gift, giftDefine, player);
    }

    public void setMessage_group(String message_group){
        this.message_group = message_group;
    }

    public String getMessage_gift(Gift gift, GiftDefine giftDefine, PlayerData player){
        return getReplaceMessage(message_gift, gift, giftDefine, player);
    }

    public void setMessage_gift(String message_gift){
        this.message_gift = message_gift;
    }

    public String getMessage_count(Gift gift, GiftDefine giftDefine, PlayerData player){
        return getReplaceMessage(message_count, gift, giftDefine, player);
    }
    
    public void setMessage_count(String message_count){
    	this.message_count = message_count;
    }
    
    public String getMessage_allcount(Gift gift, GiftDefine giftDefine, PlayerData player){
    	return getReplaceMessage(message_allcount, gift, giftDefine, player);
    }
    
    public void setMessage_allcount(String message_allcount){
    	this.message_allcount = message_allcount;
    }
    
    public String getMessage_repeat(Gift gift, GiftDefine giftDefine, PlayerData player){
        return getReplaceMessage(message_repeat, gift, giftDefine, player);
    }

    public void setMessage_repeat(String message_repeat){
        this.message_repeat = message_repeat;
    }

    public String getMessage_time(Gift gift, GiftDefine giftDefine, PlayerData player){
        return getReplaceMessage(message_time, gift, giftDefine, player);
    }

    public void setMessage_time(String message_time){
        this.message_time = message_time;
    }

    public String getMessage_item(Gift gift, GiftDefine giftDefine, PlayerData player){
        return getReplaceMessage(message_item, gift, giftDefine, player);
    }

    public void setMessage_item(String message_item){
        this.message_item = message_item;
    }

    public String getMessage_give(Gift gift, GiftDefine giftDefine, PlayerData player){
        return getReplaceMessage(message_give, gift, giftDefine, player);
    }

    public void setMessage_give(String message_give){
        this.message_give = message_give;
    }

    public String getMessage_bag(Gift gift, GiftDefine giftDefine, PlayerData player){
        return getReplaceMessage(message_bag, gift, giftDefine, player);
    }

    public void setMessage_bag(String message_bag){
        this.message_bag = message_bag;
    }

    public String getMessage_mail(Gift gift, GiftDefine giftDefine, PlayerData player){
        return getReplaceMessage(message_mail, gift, giftDefine, player);
    }

    public void setMessage_mail(String message_mail){
        this.message_mail = message_mail;
    }
    
    public String getMessage_about(Gift gift, GiftDefine giftDefine, PlayerData player){
    	return getReplaceMessage(message_about, gift, giftDefine, player);
    }
    
    public void setMessage_about(String message_about){
    	this.message_about = message_about;
    }
    
    public String getMessage_aboutmsg(Gift gift, GiftDefine giftDefine, PlayerData player){
    	return getReplaceMessage(message_aboutmsg, gift, giftDefine, player);
    }
    
    public void setMessage_aboutmsg(String message_aboutmsg){
    	this.message_aboutmsg = message_aboutmsg;
    }
    
    public String getReplaceMessage(String message, Gift gift, GiftDefine giftDefine, PlayerData player){
    	if(message == null) return null;
        String result = message;
        
        if(gift != null && message.indexOf(REPLACE_COUNT) >= 0){
            result = result.replaceAll(REPLACE_COUNT, String.valueOf(gift.getCount()));
        }

        if(giftDefine != null && message.indexOf(REPLACE_MAX_COUNT) >= 0){
            result = result.replaceAll(REPLACE_MAX_COUNT, String.valueOf(giftDefine.getMaxCount()));
        }
        
        if(message.indexOf(REPLACE_BEGIN_TIME) >= 0){
            result = result.replaceAll(REPLACE_BEGIN_TIME, getDateTimeString(false, false, giftDefine.getNextBeginTime()));
        }
        
        if(message.indexOf(REPLACE_END_TIME) >= 0){
            result = result.replaceAll(REPLACE_END_TIME, getDateTimeString(false, true, giftDefine.getNextEndTime()));
        }
        
        if(message.indexOf(REPLACE_NEED_ITEM) >= 0){
            result = result.replaceAll(REPLACE_NEED_ITEM, giftDefine.getNeedItemString());
        }
        
        if(message.indexOf(REPLACE_GIVE_ITEM) >= 0){
            result = result.replaceAll(REPLACE_GIVE_ITEM, giftDefine.getGiveItemString());
        }
        
        if(message.indexOf(REPLACE_REPEAT_COUNT) >= 0){
            result = result.replaceAll(REPLACE_REPEAT_COUNT, String.valueOf(gift.getRcount()));
        }
        
        if(message.indexOf(REPLACE_MAX_REPEAT) >= 0){
            result = result.replaceAll(REPLACE_MAX_REPEAT, String.valueOf(giftDefine.getMaxRepeat()));
        }
        
        return result;
    }
    
    public String getDateTimeString(boolean needDate, boolean isEnd, Date time){
        String result = "";
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        
        if(needDate){
            result += cal.get(Calendar.YEAR) + "-";
            int month = cal.get(Calendar.MONTH) + 1;
            result += (month < 10? ("0" + month): month) + "-";
            int day = cal.get(Calendar.DAY_OF_MONTH);
            result += (day < 10? ("0" + day): day) + "-";
        }
        
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        
        if(isEnd){
            hour = hour == 0? 24: hour;
        }
        
        result += (hour < 10? ("0" + hour): hour) + ":";
        int minute = cal.get(Calendar.MINUTE);
        result += (minute < 10? ("0" + minute): minute);
        
        return result;
    }
}
