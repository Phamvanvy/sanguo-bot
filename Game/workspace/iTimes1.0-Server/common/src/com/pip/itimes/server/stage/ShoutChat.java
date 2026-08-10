package com.pip.itimes.server.stage;

public class ShoutChat {
	/**
	 * 类型：世界，阵营
	 */
	private String type;
	/**
	 * 内容
	 */
	private String messageContent;
	/**
	 * 次数
	 */
	private int count;
	/**
	 * 礼物ID;
	 */
	private int giftId;
	/**
	 * 特殊奖品
	 */
	private int specialId;
	/**
	 * 地图Id
	 */
	private int mapId;
	
	public void setType (String type) {
		this.type = type;
	}
	public String getType () {
		return type;
	}
	
	public void setMessage (String messageContent) {
		this.messageContent = messageContent;
	}
	public String getMessage () {
		return messageContent;
	}
	
	public void setCount (int count) {
		this.count = count;
	}
	public int getCount () {
		return count;
	}
	
	public void setGiftId (int giftId) {
		this.giftId = giftId;
	}
	public int getGiftId () {
		return giftId;
	}
	
	public void setMapId (int mapId) {
		this.mapId = mapId;
	}
	public int getMapId () {
		return mapId;
	}
	
	public void setSpecialId (int specialId) {
		this.specialId = specialId;
	}
	public int getSpecialId () {
		return specialId;
	}
}
