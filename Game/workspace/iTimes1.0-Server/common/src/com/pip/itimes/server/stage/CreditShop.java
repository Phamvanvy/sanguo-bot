package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;

/**
 * @author mengjie
 * @version 1.0
 */
public class CreditShop {
	
	protected int Id;
	protected int type;
    protected int itemID;//物品ID
    protected int groupID;//掉落组ID
    protected int price;//起始价格
    protected String title;//物品名称
    protected int time;//拍卖时长
    protected int areaId;//可用
    protected String desc;//描述
    protected String corn;//时间段
    protected static int count = -1;
    
    public CreditShop(int Id,int type,int itemID,int groupID,int price,String title,int time,int areaId,String desc,String corn) {
    	this.Id = Id;
    	this.type = type;
    	this.itemID = itemID;//物品ID
    	this.groupID = groupID;//掉落组ID
    	this.price = price;//起始价格
    	this.title = title;//物品名称
    	this.time = time;//拍卖时长
    	this.areaId = areaId;//可用
    	this.desc = desc;//描述
    	this.corn = corn;
    }
    
    private static Map creditshopmap = new HashMap();
    
    public static CreditShop getCreditShop(int ID){
		return (CreditShop)creditshopmap.get(ID);
    }
    public static void addCreditShop(int ID,CreditShop credit){
    	creditshopmap.put(ID, credit);
    }
    public static  void addCount(int Count){
    	CreditShop.count = Count;
    }

	public static int getCount() {
		return count;
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getItemID() {
		return itemID;
	}

	public void setItemID(int itemID) {
		this.itemID = itemID;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getAreaId() {
		return areaId;
	}

	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getCorn() {
		return corn;
	}

	public void setCorn(String corn) {
		this.corn = corn;
	}

	public static Map getCreditshopmap() {
		return creditshopmap;
	}

	public static void setCreditshopmap(Map creditshopmap) {
		CreditShop.creditshopmap = creditshopmap;
	}

	public static void setCount(int count) {
		CreditShop.count = count;
	}
	public int getGroupID() {
		return groupID;
	}
	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}    

	//get
	
}
