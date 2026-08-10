package com.pip.itimes.server.camp;

public class CampSkillData{
    private int effect;
    private int level;
    private long lastUpgradeTime;
    private long lastMaintTime;

    public int getEffect(){
        return effect;
    }

    public void setEffect(int effect){
        this.effect = effect;
    }

    public int getLevel(){
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public long getLastUpgradeTime(){
        return lastUpgradeTime;
    }

    public void setLastUpgradeTime(long lastUpgradeTime){
        this.lastUpgradeTime = lastUpgradeTime;
    }

    public long getLastMaintTime(){
        return lastMaintTime;
    }

    public void setLastMaintTime(long lastMaintTime){
        this.lastMaintTime = lastMaintTime;
    }
}
