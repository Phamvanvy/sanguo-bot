package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class GenEffect extends Effect{

    private int itemId;
    private byte count;
    private byte itemType;

    public GenEffect() {
    }

    public byte getType(){
        return 2;
    }

    public int getItemId(){
        return itemId;
    }

    public void setItemId(int itemId){
        this.itemId = itemId;
    }

    public int getCount(){
        return count;
    }

    public void setCount(byte count){
        this.count = count;
    }

    public byte getItemType(){
        return itemType;
    }

    public void setItemType(byte itemType){
        this.itemType = itemType;
    }
}
