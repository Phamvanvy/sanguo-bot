package com.pip.itimes.server.stage;

/**
 * @author mengjie
 * @version 1.0
 */
public class LevellimitEffect extends Effect {
	private int level;
	private int itemid;
	private int type;
	private int count;
    public LevellimitEffect(int level,int itemid,int type,int count) {
    	this.level = level;
    	this.itemid = itemid;
    	this.type = type;
    	this.count = count;
    }

    public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getItemid() {
		return itemid;
	}

	public void setItemid(int itemid) {
		this.itemid = itemid;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setType(int type) {
		this.type = type;
	}

	public byte getType() {
        return 50;
    }

}
