package com.pip.itimes.server.world;

import org.apache.commons.collections.primitives.ArrayShortList;
import org.apache.commons.collections.primitives.ShortList;

import com.pip.itimes.server.world.game.CampBattlefield;
import com.pip.itimes.server.world.game.InstanceModel;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class InstanceDefinition {

    private int id;
    private short map;
    private short x,y;
    private short entrance;
    private short entranceX,entranceY;
    private short entrancePixelX,entrancePixelY;
    private int maxPlayer;
    private int refreshSecond;
    private int bbsId;
    private ShortList maps = new ArrayShortList();
    private InstanceModel model;
    private String type;
    private int minLevel;
    private int maxTime;
	private int timeout;
    private int clearance;
    private int goodsLoseRate;
    private int competingGoodsID;
    private short darkEntrance;
    private short darkEntranceX, darkEntranceY;
    private short darkEntrancePixelX, darkEntrancePixelY;
    private short brightEntrance;
    private short brightEntranceX, brightEntranceY;
    private short brightEntrancePixelX, brightEntrancePixelY;
    private String rules;
    private CampBattlefield campBattlefield;
    
	public InstanceDefinition(int id,short map,short x,short y) {
        this.id = id;
        this.map = map;
        this.x = x;
        this.y = y;
    }

    public int getId(){
        return id;
    }

    //入口地图
    public short getMap(){
        return map;
    }

    public short getX(){
        return x;
    }

    public short getY(){
        return y;
    }

    public void setEntrance(short entrance){
        this.entrance = entrance;
    }

    public short getEntrance(){
        return entrance;
    }

    public void setEntrancePixelX(short x){
        this.entrancePixelX = x;
    }

    public void setEntrancePixelY(short y){
        this.entrancePixelY = y;
    }

    public short getEntrancePixelX(){
        return entrancePixelX;
    }

    public short getEntrancePixelY(){
        return entrancePixelY;
    }

    public void setEntranceX(short entranceX){
        this.entranceX = entranceX;
    }

    public void setEntranceY(short entranceY){
        this.entranceY = entranceY;
    }

    public short getEntranceX(){
        return entranceX;
    }

    public short getEntranceY(){
        return entranceY;
    }

    public void addMap(short map){
        if(!maps.contains(map)){
            maps.add(map);
        }
    }

    public short[] getMaps(){
        return maps.toArray();
    }

    public int getMaxPlayer(){
        return maxPlayer;
    }

    public void setMaxPlayer(int maxPlayer){
        this.maxPlayer = maxPlayer;
    }

    public void setRefreshSecond(int refreshSecond){
        this.refreshSecond = refreshSecond;
    }

    public int getRefreshSecond(){
        return refreshSecond;
    }

    public void setModel(InstanceModel model){
        this.model = model;
    }

    public void setBbsId(int bbsId) {
        this.bbsId = bbsId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public InstanceModel getModel(){
        return model;
    }

    public int getBbsId() {
        return bbsId;
    }

    public String getType() {
        return type;
    }

    public int getMinLevel() {
        return minLevel;
    }
    
    public int getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(int maxTime) {
		this.maxTime = maxTime;
	}
	
	/**
	 * 玩家离线timeout秒则T出战场
	 * @return
	 */
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getClearance() {
		return clearance;
	}

	public void setClearance(int clearance) {
		this.clearance = clearance;
	}
	
	public int getGoodsLoseRate() {
		return goodsLoseRate;
	}

	public void setGoodsLoseRate(int goodsLoseRate) {
		this.goodsLoseRate = goodsLoseRate;
	}

	public int getCompetingGoodsID() {
		return competingGoodsID;
	}

	public void setCompetingGoodsID(int competingGoodsID) {
		this.competingGoodsID = competingGoodsID;
	}

	public short getDarkEntrance() {
		return darkEntrance;
	}

	public void setDarkEntrance(short darkEntrance) {
		this.darkEntrance = darkEntrance;
	}

	public short getDarkEntranceX() {
		return darkEntranceX;
	}

	public void setDarkEntranceX(short darkEntranceX) {
		this.darkEntranceX = darkEntranceX;
	}

	public short getDarkEntranceY() {
		return darkEntranceY;
	}

	public void setDarkEntranceY(short darkEntranceY) {
		this.darkEntranceY = darkEntranceY;
	}

	public short getBrightEntrance() {
		return brightEntrance;
	}

	public void setBrightEntrance(short brightEntrance) {
		this.brightEntrance = brightEntrance;
	}

	public short getBrightEntranceX() {
		return brightEntranceX;
	}

	public void setBrightEntranceX(short brightEntranceX) {
		this.brightEntranceX = brightEntranceX;
	}

	public short getBrightEntranceY() {
		return brightEntranceY;
	}

	public void setBrightEntranceY(short brightEntranceY) {
		this.brightEntranceY = brightEntranceY;
	}

	public short getDarkEntrancePixelX() {
		return darkEntrancePixelX;
	}

	public void setDarkEntrancePixelX(short darkEntrancePixelX) {
		this.darkEntrancePixelX = darkEntrancePixelX;
	}

	public short getDarkEntrancePixelY() {
		return darkEntrancePixelY;
	}

	public void setDarkEntrancePixelY(short darkEntrancePixelY) {
		this.darkEntrancePixelY = darkEntrancePixelY;
	}

	public short getBrightEntrancePixelX() {
		return brightEntrancePixelX;
	}

	public void setBrightEntrancePixelX(short brightEntrancePixelX) {
		this.brightEntrancePixelX = brightEntrancePixelX;
	}

	public short getBrightEntrancePixelY() {
		return brightEntrancePixelY;
	}

	public void setBrightEntrancePixelY(short brightEntrancePixelY) {
		this.brightEntrancePixelY = brightEntrancePixelY;
	}

	public CampBattlefield getCampBattlefield() {
		return campBattlefield;
	}

	public void setCampBattlefield(CampBattlefield campBattlefield) {
		this.campBattlefield = campBattlefield;
	}

	public String getRules() {
		return rules;
	}

	public void setRules(String rules) {
		this.rules = rules;
	}
}
