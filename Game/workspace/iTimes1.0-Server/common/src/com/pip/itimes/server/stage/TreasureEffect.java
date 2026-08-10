package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TreasureEffect extends Effect{

    private short mapId;
    private short minX,minY,maxX,maxY;

    private int itemGroupId, shovelId;

    public TreasureEffect() {
    }
    public byte getType(){
        return 10;
    }



    public short getMapId() {
        return mapId;
    }

    public void setItemGroupId(int itemGroupId) {
        this.itemGroupId = itemGroupId;
    }



    public void setMapId(short mapId) {
        this.mapId = mapId;
    }

    public void setMinY(short minY) {
        this.minY = minY;
    }

    public void setMinX(short minX) {
        this.minX = minX;
    }

    public void setMaxY(short maxY) {
        this.maxY = maxY;
    }

    public void setMaxX(short maxX) {
        this.maxX = maxX;
    }

    public int getItemGroupId() {
        return itemGroupId;
    }

    public short getMinY() {
        return minY;
    }

    public short getMinX() {
        return minX;
    }

    public short getMaxY() {
        return maxY;
    }

    public short getMaxX() {
        return maxX;
    }
    
    public void setShovelId (int shovelId) {
    	this.shovelId = shovelId;
    }
    
    public int getShovelId () {
    	return shovelId;
    }

}
