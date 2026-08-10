package com.pip.itimes.server.bean;

import java.util.Date;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class HopeGrass {

    private int id;
    private int playerId;
    private int itemGroupId;
    private short mapId;
    private short x,y;
    private Date createTime;
    private Date validTime;
    private Date obsoleteTime;
    private int ratio;
    private int grassType;
    private int grouprnd;//type=1时，随机会从itemGroupId中掉落的几率
    
    public HopeGrass() {
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public short getY() {
        return y;
    }

    public short getX() {
        return x;
    }

    public Date getValidTime() {
        return validTime;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Date getObsoleteTime() {
        return obsoleteTime;
    }

    public short getMapId() {
        return mapId;
    }

    public int getItemGroupId() {
        return itemGroupId;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setY(short y) {
        this.y = y;
    }

    public void setX(short x) {
        this.x = x;
    }

    public void setValidTime(Date validTime) {
        this.validTime = validTime;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setObsoleteTime(Date obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }

    public void setMapId(short mapId) {
        this.mapId = mapId;
    }

    public void setItemGroupId(int itemGroupId) {
        this.itemGroupId = itemGroupId;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public void setGrassType(int grassType) {
        this.grassType = grassType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public int getRatio() {
        return ratio;
    }

    public int getGrassType() {
        return grassType;
    }

	public int getGrouprnd() {
		return grouprnd;
	}

	public void setGrouprnd(int grouprnd) {
		this.grouprnd = grouprnd;
	}
    
}
