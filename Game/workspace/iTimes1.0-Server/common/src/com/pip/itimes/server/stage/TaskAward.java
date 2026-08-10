package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TaskAward {
	private Logger log = Logger.getLogger(TaskAward.class);

    private short taskId;
    private SubTaskAward common;
    private Map map = new TreeMap();

    public TaskAward() {
    }



    public void setTaskId(short taskId){
        this.taskId = taskId;
    }

    public short getTaskId(){
        return taskId;
    }

    public SubTaskAward getCommonAward(){
        return common;
    }

    public void addCommonAward(SubTaskAward award){
        common = award;
    }

    public SubTaskAward getAward(int subId){
        return (SubTaskAward)map.get(new Integer(subId));
    }

    public void addAward(SubTaskAward award){
        map.put(new Integer(award.getSubId()),award);
    }

    public TemplateGrid[] getRemoveItems(int subId){
    	SubTaskAward award = getAward(subId);
    	if(award == null){
    		log.info("TaskAward Exception subTaskAward is null task ID[" + taskId + "] subId[" + subId + "]");
    		return null;
    	}
        return award.getRemoveItems();
    }

    public TemplateGrid[] getAddItems(int subId){
        TemplateGrid[] sAdd = getAward(subId).getAddItems();
        if(subId>=1000){
            TemplateGrid[] cAdd = common.getAddItems();
            TemplateGrid[] ret = new TemplateGrid[sAdd.length+cAdd.length];
            System.arraycopy(sAdd,0,ret,0,sAdd.length);
            System.arraycopy(cAdd,0,ret,sAdd.length,cAdd.length);
            return ret;
        }else{
            return sAdd;
        }

    }

    public int getExp(int subId){
    	int exp = (int)((common.getExp()+ getAward(subId).getExp()) * 1.70);
        return (int)exp;
    }

    public int getMoney(int subId){
        return common.getMoney()+getAward(subId).getMoney();
    }

    public int getCredit(int subId){
        return common.getCredit()+getAward(subId).getCredit();
    }
    
    public Set getAwardMapKey(){
    	return map.keySet();
    }
    
    public String getAwardDesc(){
    	String desc = "\n任务奖励：";
    	Set set = map.keySet();
    	Integer[] key = new Integer[set.size()];
    	set.toArray(key);
    	int awardCount = 0;
    	for(int i=0; i<key.length; i++){
    		if(key[i] > 1000){
    			awardCount ++;
    		}
    	}
    	if(awardCount != 1){
    		return null;
    	}
    	for(int i=0; i<key.length; i++){
    		if(key[i] > 1000){
    			SubTaskAward award = (SubTaskAward)map.get(key[i]);
    			int money = common.getMoney() + award.getMoney();
    			int exp = getExp(key[i]);//common.getExp() + award.getExp();
    			if(exp > 0){
    				desc += "\n　经验：" + exp;
    			}
    			if(money > 0){
    				desc += "\n　金钱：" + money;
    			}
    			int credit = common.getCredit() + award.getCredit();
    			if(credit > 0){
    				desc += "\n　荣誉：" + credit;
    			}
    			TemplateGrid[] grid = getAddItems(key[i]);
    			if(grid != null){
    				for(TemplateGrid g : grid){
    					int color = Utils.CLR_EQUIP[g.template.getQuality()];
    					desc += "\n　<c" + Integer.toHexString(color) + ">"+ g.template.getName() + "</c> * " + g.count;
    				}
    			}
    		}
    	}
    	return desc;
    }
}
