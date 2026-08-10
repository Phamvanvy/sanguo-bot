package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class FallItem{

    private byte type;
    private int min;
    private int max;
    private int id;
    private int chance;
    private int dropType;

    public FallItem() {
    }

    public FallItem(byte type,int min,int max,int id,int chance){
        this.type = type;
        this.min = min;
        this.max = max;
        this.id = id;
        this.chance = chance;
    }

    public void setType(byte type){
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public void setMin(int min){
        this.min = min;
    }

    public int getMin() {
        return min;
    }

    public void setMax(int max){
        this.max = max;
    }

    public int getMax() {
        return max;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setChance(int chance){
        this.chance = chance;
    }

    public int getChance() {
        return chance;
    }
    
    public void setDropType(int dropType){
    	this.dropType = dropType;
    }
    
    public int getDropType(){
    	return dropType;
    }
}
