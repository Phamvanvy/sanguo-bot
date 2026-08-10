package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class DropGroup {

    private int id;
    private String name;
    private long startTime;
    private long endTime;
    private int rate;
    private List dropItems = new ArrayList();
	private int minLevel;
    private int maxLevel;

    public DropGroup() {
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }
    
    public void setStartTime(long startTime){
    	this.startTime = startTime;
    }
    
    public long getStartTime(){
    	return startTime;
    }
    
    public void setEndTime(long endTime){
    	this.endTime = endTime;
    }
    
    public long getEndTime(){
    	return endTime;
    }

    public void addDropItem(DropItem dropItem){
        dropItems.add(dropItem);
    }

    public void setRate(int rate){
        this.rate = rate;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getRate(){
        return rate;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public DropItem calcDropItem(int rate){
        if(dropItems.size()==0)
            return null;
        DropItem ret = (DropItem)dropItems.get(0);
        for(int i=0;i<dropItems.size();i++){
            DropItem drop = (DropItem)dropItems.get(i);
            if(drop.getRate()>rate){
                ret = drop;
                break;
            }
        }
        return ret;
    }
    public List getDropItems() {
		return dropItems;
	}
}
