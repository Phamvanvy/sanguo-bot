package com.pip.itimes.server.world.ItemGroup;

import com.pip.itimes.server.stage.IItemTemplate;

public class ItemInfo {
	private int itemid;
	private int point;
	private byte counttype;
	private int count;
	private byte refreshtype;
	private int refresh;
	private String desc;
	private int level;
	private long timer;
	private IItemTemplate item;
	
	public ItemInfo copy(){
		ItemInfo itemInfoNew = new ItemInfo();
		itemInfoNew.setCount(getCount());
		itemInfoNew.setCountType(getCountType());
		itemInfoNew.setDesc(getDesc());
		itemInfoNew.setItemID(getItemID());
		itemInfoNew.setLevel(getLevel());
		itemInfoNew.setPoint(getPoint());
		itemInfoNew.setRefresh(getRefresh());
		itemInfoNew.setRefreshType(getRefreshType());
		itemInfoNew.setTimer(getTimer());
		itemInfoNew.setItem(getItem());
		return itemInfoNew;
	}
//	public ItemInfo copy(ItemInfo itemInfo){
//		ItemInfo itemInfoNew = new ItemInfo();
//		itemInfoNew.setCount(itemInfo.getCount());
//		itemInfoNew.setCountType(itemInfo.getCountType());
//		itemInfoNew.setDesc(itemInfo.getDesc());
//		itemInfoNew.setItemID(itemInfo.getItemID());
//		itemInfoNew.setLevel(itemInfo.getLevel());
//		itemInfoNew.setPoint(itemInfo.getPoint());
//		itemInfoNew.setRefresh(itemInfo.getRefresh());
//		itemInfoNew.setRefreshType(itemInfo.getRefreshType());
//		itemInfoNew.setTimer(itemInfo.getTimer());
//		return itemInfoNew;
//	}
	
	public void setItemID(int itemid){
		this.itemid = itemid;
	}
	
	public int getItemID(){
		return itemid;
	}
	
	public void setPoint(int point){
		this.point = point;
	}
	
	public int getPoint(){
		return point;
	}
	
	public void setCountType(byte counttype){
		this.counttype = counttype;
	}
	
	public byte getCountType(){
		return counttype;
	}
	
	public void setCount(int count){
		this.count = count;
	}
	
	public int getCount(){
		return count;
	}
	
	public void setRefreshType(byte refreshtype){
		this.refreshtype = refreshtype;
	}
	
	public byte getRefreshType(){
		return refreshtype;
	}
	
	public void setRefresh(int refresh){
		this.refresh = refresh;
	}
	
	public int getRefresh(){
		return refresh;
	}
	
	public void setLevel(int level){
		this.level = level;
	}
	
	public int getLevel(){
		return level;
	}
	
	public void setDesc(String desc){
		this.desc = desc;
	}
	
	public String getDesc(){
		return desc;
	}
	
	public void setTimer(long timer){
		this.timer = timer;
	}
	
	public long getTimer(){
		return timer;
	}
	
	public void setItem(IItemTemplate item){
		this.item = item;
	}
	
	public IItemTemplate getItem(){
		return item;
	}
}
