package com.pip.itimes.server.gift;



public class OnlyGiftNeedItem{
	public OnlyGiftNeedItem(int id ,int type ,int yeartype ,int enhanceCount){
		this.id = id;
		this.type = type;
		this.yeartype = yeartype;
		this.enhanceCount = enhanceCount;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getYeartype() {
		return yeartype;
	}
	public void setYeartype(int yeartype) {
		this.yeartype = yeartype;
	}
	public int getEnhanceCount() {
		return enhanceCount;
	}
	public void setEnhanceCount(int enhanceCount) {
		this.enhanceCount = enhanceCount;
	}
	private int id;
	private int type;
	private int yeartype;
	private int enhanceCount;
}