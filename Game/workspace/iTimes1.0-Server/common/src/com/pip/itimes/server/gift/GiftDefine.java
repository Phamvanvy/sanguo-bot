package com.pip.itimes.server.gift;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.pip.itimes.server.bean.Gift;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.stage.TemplateGrid;
import com.pip.itimes.server.util.Utils;


public class GiftDefine{
    private int id;
    private int beginLevel;
    private int endLevel;
    private int maxCount;
    private int allCount;
    private int maxRepeat;
    private int timeSpace; //1-24的数，为领取物品的小时间隔
    private int beginTime; //开放领取的时间 小时
    private int endTime; //开放领取的时间 小时
    private Vector<TemplateGrid[]> needItems = new Vector<TemplateGrid[]>();
    private Vector<TemplateGrid> giveItems = new Vector<TemplateGrid>();

    private static final int ONE_HORE = 3600 * 1000;
    
    private static final int ONE_WEEK = 7 * 3600 * 1000;
    private static final int ONE_MONTH = 30 * 3600 * 1000;
    private static final int ONE_YEAR = 365 * 3600 * 1000;
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

    public int getMaxCount(){
        return maxCount;
    }

    public void setMaxCount(int maxCount){
        this.maxCount = maxCount;
    }
    
    public void setAllCount(int allCount){
    	this.allCount = allCount;
    }
    
    public int getAllCount(){
    	return allCount;
    }
    
    public int getMaxRepeat(){
        return maxRepeat;
    }
    
    public void setMaxRepeat(int maxRepeat){
        this.maxRepeat = maxRepeat;
    }
    
    public void setTimeSpace(int timeSpace){
        if(timeSpace == -1){
            this.timeSpace = 0;
        }else if(timeSpace == -2){
        	this.timeSpace = -2;
        }else if(timeSpace == -3){
        	this.timeSpace = -3;
        }else if(timeSpace == -4){
        	this.timeSpace = -4;
        }else if(timeSpace >= 24){
            this.timeSpace = 24 * ONE_HORE;
        }else{
            this.timeSpace = timeSpace * ONE_HORE;
        }
    }

    public void setBeginTime(int beginTime){
        if(beginTime < 0){
            this.beginTime = 0;
        }else if(beginTime >= 24){
            this.beginTime = 23 * ONE_HORE;
        }else{
            this.beginTime = beginTime * ONE_HORE;
        }
    }

    public void setEndTime(int endTime){
        if(endTime < 0 || endTime >= 24){
            this.endTime = 24 * ONE_HORE;
        }else{
            this.endTime = endTime * ONE_HORE;
        }
    }

    public List<TemplateGrid[]> getNeedItems(){
    	ArrayList<TemplateGrid[]> result = new ArrayList<TemplateGrid[]>();
    	
    	for(TemplateGrid[] t : needItems){
    		result.add(t);
    	}

        return result;
    }
//特殊处理，，表示关系为或
    public void addNeedItem(int[] itemId, int count, int []percent){
    	TemplateGrid[] item = null;
		item = new TemplateGrid[itemId.length];
    	for (int i = 0; i< itemId.length ; i++){
    		item[i] = new TemplateGrid(Items.getTemplate(itemId[i]), count);
    		if(percent != null){
    			item[i].setPercent(percent[i]);
    		}
    	}
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
    
    public int isAvailable(Gift gift, PlayerData player, GiftGroup giftGroup){
        if(!testCount(gift, player)){
            return GiftGroup.GIFT_ERROR_COUNT;
        }
        
        if(!testTime(gift, player)){
            return GiftGroup.GIFT_ERROR_TIME;
        }
        
        if(!testRepeat(gift, player)){
            return GiftGroup.GIFT_ERROR_REPEAT;
        }
        
        if(!testNeedItem(gift, player)){
            return GiftGroup.GIFT_ERROR_ITEM;
        }
        
        if(giftGroup.getGiveType() == GiftGroup.GIVE_BY_PUSH && !testBag(gift, player)){
            return GiftGroup.GIFT_ERROR_BAG;
        }else if (giftGroup.getGiveType() == GiftGroup.GIVE_BY_SUPERQ){
        	if (!testBag(gift, player)){
        		return GiftGroup.GIFT_ERROR_BAG;
        	}else{
        		return GiftGroup.GIFT_INFO_SUPERQ;
        	}
        }else if (giftGroup.getGiveType() == GiftGroup.GIVE_BY_SUPERQ_nomal){
        	if (!testBag(gift, player)){
        		return GiftGroup.GIFT_ERROR_BAG;
        	}else{
        		return GiftGroup.GIFT_INFO_SUPERQ_nomal;
        	}
        }
        
        return GiftGroup.GIFT_AVAILABLE;
    }
    
    public boolean isNewTime(Gift gift, PlayerData player){
        boolean result = false;
        
        long now = System.currentTimeMillis();
        long last = gift.getModifytime().getTime();
        long todayStart = Utils.getTodayStart();
        long nextdayStart = todayStart + 24 * ONE_HORE;
        long begin = todayStart + beginTime;
        long end = todayStart + endTime;
        if (timeSpace == -3){
        	if (gift.getCount() > 0){
        		Calendar callast = Calendar.getInstance();
            	callast.setTime(gift.getModifytime());
            	
            	Calendar calnow = Calendar.getInstance();
            	calnow.setTime(new Date());
            	
            	if (callast.get(Calendar.MONTH) == calnow.get(Calendar.MONTH)){
            		return result;
            	}else{
            		return true;
            	}
        	}else{
        		return true;
        	}
        	
        }
        while(begin < nextdayStart && timeSpace > 0){
            if(begin <= now && end >= now && begin > last){
                result = true;
                
                break;
            }
            
            begin += timeSpace;
            end += timeSpace;
            
            if(end > nextdayStart){
                end = nextdayStart;
            }
        }
        
        return result;
    }
    
    private boolean testRepeat(Gift gift, PlayerData player){
        boolean result = true;
        
        if(maxRepeat > 0 && gift.getRcount() >= maxRepeat){
            result = false;
        }
        
        return result;
    }
    
    private boolean testCount(Gift gift, PlayerData player){
        boolean result = true;
        
        if(maxCount > 0 && gift.getCount() >= maxCount){
            result = false;
        }
        
        return result;
    }
    
    private boolean testTime(Gift gift, PlayerData player){
        boolean result = false;
        
        long now = System.currentTimeMillis();
        long todayStart = Utils.getTodayStart();
        long nextdayStart = todayStart + 24 * ONE_HORE;
        long begin = todayStart + beginTime;
        long end = todayStart + endTime;
        if (timeSpace == -3){
        	if (gift.getCount() > 0){
        		Calendar callast = Calendar.getInstance();
            	callast.setTime(gift.getModifytime());
            	
            	Calendar calnow = Calendar.getInstance();
            	calnow.setTime(new Date());
            	
            	if (callast.get(Calendar.MONTH) == calnow.get(Calendar.MONTH)){
            		return result;
            	}else{
            		return true;
            	}
        	}else{
        		return true;
        	}
        	
        }
        while(begin < nextdayStart && timeSpace > 0){
            if(begin <= now && end >= now){
                result = true;
                
                break;
            }
            
            begin += timeSpace;
            end += timeSpace;
            
            if(end > nextdayStart){
                end = nextdayStart;
            }
        }
        
        return result;
    }
    
    private boolean testNeedItem(Gift gift, PlayerData player){
        boolean result = true;
        int totalcount = 0;
        int count = 0;
        if (needItems.size() > 0){
        	TemplateGrid[] item = needItems.get(0);
        	if (item.length > 0){//或 关系
        		totalcount = item[0].count;
        		for(int i = 0; i < item.length; i++){
                    count = count + player.getItemCount2(item[i].template.getItemId());
                }
        		if (count<totalcount){
                    result = false;
        		}
        	}else{
        		//旧的逻辑
        		for(int i = 0; i < needItems.size(); i++){
                    TemplateGrid[] item_ = needItems.get(i);
                    
                    if(!player.hasItem(item_[0].template.getItemId(), item[0].count)){
                        result = false;
                        
                        break;
                    }
                }
        	}
        }
        
        
        return result;
    }
    
    private boolean testBag(Gift gift, PlayerData player){
        boolean result = true;
        
        TemplateGrid[] gives = new TemplateGrid[giveItems.size()];
        giveItems.toArray(gives);
        
        if(player.isOver(gives)){
            result = false;
        }
        
        return result;
    }
    
    public String getNeedItemString(){
        StringBuffer sb = new StringBuffer();
        String itemsname = "";
        int totalcount = 0;
        int count = 0;
        if (needItems.size() > 0){
        	TemplateGrid[] item = needItems.get(0);
        	if (item.length > 1){//或 关系
        		for(int i = 0; i < item.length; i++){
        			if (i>0){
        				itemsname = itemsname + ",";
        			}
        			itemsname = itemsname + item[i].template.getName();
                }
        		sb.append(itemsname);
        	}else{
        		//旧的逻辑
        		for(int i = 0; i < needItems.size(); i++){
                    TemplateGrid[] grid = needItems.get(i);
                    sb.append(grid[0].template.getName());
                    
                    if(grid[0].count > 1){
                        sb.append(" + ");
                        sb.append(grid[0].count);
                    }
                    
                    if(i < needItems.size() - 1){
                        sb.append(", ");
                    }
                }
        	}
        }
        
        
        
        
        return sb.toString();
    }
    
    public String getGiveItemString(){
        StringBuffer sb = new StringBuffer();
        
        for(int i = 0; i < giveItems.size(); i++){
            TemplateGrid grid = giveItems.get(i);
            sb.append(grid.template.getName());
            
            if(grid.count > 1){
                sb.append(" + ");
                sb.append(grid.count);
            }
            
            if(i < needItems.size() - 1){
                sb.append(", ");
            }
        }
        
        return sb.toString();
    }
    
    public Date getNextBeginTime(){
        long now = System.currentTimeMillis();
        long todayStart = Utils.getTodayStart();
        long todayEnd = todayStart + endTime;
        long begin = todayStart + beginTime;
        long end;
        if (timeSpace<0){
        	end = begin;
        }else{
        	end = begin + timeSpace;
        }
        
        if(end > todayEnd){
            end = todayEnd;
        }
        
        while(begin < todayEnd && timeSpace > 0){
            if(begin > now){
                break;
            }
            
            begin += timeSpace;
            end += timeSpace;
            
            if(end > todayEnd){
                end = todayEnd;
            }
        }
        
        return new Date(begin);
    }
    
    public Date getNextEndTime(){
        long now = System.currentTimeMillis();
        long todayStart = Utils.getTodayStart();
        long todayEnd = todayStart + endTime;
        long begin = todayStart + beginTime;
        long end;
        if (timeSpace<0){
        	end = begin;
        }else{
        	end = begin + timeSpace;
        }
        
        if(end > todayEnd){
            end = todayEnd;
        }
        
        while(begin < todayEnd && timeSpace > 0){
            if(begin > now){
                break;
            }
            
            begin += timeSpace;
            end += timeSpace;
            
            if(end > todayEnd){
                end = todayEnd;
            }
        }
        
        return new Date(end);
    }
}
