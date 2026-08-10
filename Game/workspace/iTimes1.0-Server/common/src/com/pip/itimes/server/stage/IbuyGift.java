package com.pip.itimes.server.stage;

import java.util.HashMap;
import java.util.Map;

public class IbuyGift {
	
	protected int Id;
    protected String name;
    protected int useitemsid;//扣除道具物品id
    protected int buyitemsid;//购买物品id
    protected int buycount;//购买数量
    protected int giftitemsid;//赠送物品id
    protected int giftcount;//赠送数量
    protected int useitemslevel;//使用道具最高等级
    protected int giftrate;	// 赠送几率
    protected boolean eachone;	// 是否每买一次都赠送
    
    protected static Map<Integer,IbuyGift> Buycount1 = new HashMap<Integer,IbuyGift>();
    protected static Map<Integer,IbuyGift> Buycount10 = new HashMap<Integer,IbuyGift>();
    protected static Map<Integer,IbuyGift> BuybyId = new HashMap<Integer,IbuyGift>();
    
    public static  void addIbuyGift(IbuyGift ibuygift){

    	if (ibuygift.getBuycount()<10){
    		Buycount1.put(ibuygift.getBuyitemsid(), ibuygift);
    	}else{
    		Buycount10.put(ibuygift.getBuyitemsid(), ibuygift);
    	}
    	BuybyId.put(ibuygift.getId(), ibuygift);
    }
    
    public static IbuyGift getIbuyGiftcount1(int Buyitemsid){
        return Buycount1.get(Buyitemsid);
    }
    
    public static IbuyGift getIbuyGiftcount10(int Buyitemsid){
        return Buycount10.get(Buyitemsid);
    }
    
    public IbuyGift(int Id,String name,int buyitemsid,int buycount,int giftitemsid,int giftcount,int useitemslevel,int useitemsid, int gifrate, boolean eachone) {
    	this.Id = Id;
    	this.name = name;
        this.buyitemsid = buyitemsid;//购买物品id
        this.buycount = buycount;//购买数量
        this.giftitemsid = giftitemsid;//赠送物品id
        this.giftcount = giftcount;//赠送数量
        this.useitemslevel = useitemslevel;
    	this.useitemsid = useitemsid;
    	this.giftrate = gifrate;
    	this.eachone = eachone;
    }

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getBuyitemsid() {
		return buyitemsid;
	}

	public void setBuyitemsid(int buyitemsid) {
		this.buyitemsid = buyitemsid;
	}

	public int getBuycount() {
		return buycount;
	}

	public void setBuycount(int buycount) {
		this.buycount = buycount;
	}

	public int getGiftitemsid() {
		return giftitemsid;
	}

	public void setGiftitemsid(int giftitemsid) {
		this.giftitemsid = giftitemsid;
	}

	public int getGiftcount() {
		return giftcount;
	}

	public void setGiftcount(int giftcount) {
		this.giftcount = giftcount;
	}
	public int getUseitemsid() {
		return useitemsid;
	}

	public void setUseitemsid(int useitemsid) {
		this.useitemsid = useitemsid;
	}

	public int getUseitemslevel() {
		return useitemslevel;
	}

	public void setUseitemslevel(int useitemslevel) {
		this.useitemslevel = useitemslevel;
	}
	
	public int getGifrate() {
		return giftrate;
	}
	public boolean getEachone() {
		return eachone;
	}
}
