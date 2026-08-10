package com.pip.itimes.server.stage;

public class GiftItemNpcType extends TaskNpcType {
	private int typeid = 0;
	private int itemid = 0;//需要的物品
	private int giftid = 0;//送给对方的物品
	private int additemid = 0;//留给自己的物品
	private boolean mailflag = true; //是否通过精灵速递给对方
	private String mailtitle = "";//精灵速递内容
	private String itemname = "";
	public GiftItemNpcType(int id, String name, int type) {
        super(id, name, type);
    }

	public int getTypeid() {
		return typeid;
	}

	public void setTypeid(int typeid) {
		this.typeid = typeid;
	}

	public int getItemid() {
		return itemid;
	}
	public void setItemid(int itemid) {
		this.itemid = itemid;
	}
	public int getGiftid() {
		return giftid;
	}
	public void setGiftid(int giftid) {
		this.giftid = giftid;
	}
	public int getAdditemid() {
		return additemid;
	}
	public void setAdditemid(int additemid) {
		this.additemid = additemid;
	}
	public boolean isMailflag() {
		return mailflag;
	}
	public void setMailflag(boolean mailflag) {
		this.mailflag = mailflag;
	}
	public String getMailtitle() {
		return mailtitle;
	}
	public void setMailtitle(String mailtitle) {
		this.mailtitle = mailtitle;
	}
	public String getItemname() {
		return itemname;
	}
	public void setItemname(String itemname) {
		this.itemname = itemname;
	}
	
}
