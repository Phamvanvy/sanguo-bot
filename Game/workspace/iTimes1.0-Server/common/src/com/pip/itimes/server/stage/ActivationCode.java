package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Map;

public class ActivationCode {
	
	protected int Id;
    protected int itemsid;//道具物品id
    protected int count;//数量
    protected int level;//兑换道具最低等级
    
    protected static Map<Integer,ActivationCode> activationcodemap = new HashMap<Integer,ActivationCode>();
    
    public static  void addActivationCode(ActivationCode activationcode){
    	activationcodemap.put(activationcode.getId(), activationcode);
    }
    
    public static ActivationCode getactivationcode(int id){
        return activationcodemap.get(id);
    }
    public ActivationCode(int Id,int itemsid,int count,int level) {
    	this.Id = Id;
    	this.itemsid = itemsid;//物品id
        this.count = count;//数量
        this.level = level;
    }

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public int getItemsid() {
		return itemsid;
	}

	public void setItemsid(int itemsid) {
		this.itemsid = itemsid;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}
