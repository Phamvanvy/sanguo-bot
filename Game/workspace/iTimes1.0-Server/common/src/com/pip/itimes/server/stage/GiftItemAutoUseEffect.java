package com.pip.itimes.server.stage;

/**
 * @author sky
 * @version 1.0
 */
public class GiftItemAutoUseEffect extends Effect {
	
	public static final int USETYPE_MARRIAGE = 1; // ∑Ú∆ﬁπÿœµ
	
	private int itemtype;
    private int itemid;
    private int usetype;
    private int count;
    private int paramtype;
    private int auto;
    private int addgroupid;
    public GiftItemAutoUseEffect(int itemtype,int itemid,int usetype,int count,int paramtype ,int auto,int addgroupid) {
    	this.itemtype = itemtype;
    	this.itemid = itemid;
    	this.usetype = usetype;
    	this.count = count;
    	this.paramtype = paramtype;
    	this.auto = auto;
    	this.addgroupid = addgroupid;
    }

    public int getItemtype() {
		return itemtype;
	}

	public void setItemtype(int itemtype) {
		this.itemtype = itemtype;
	}

	public int getItemid() {
		return itemid;
	}

	public void setItemid(int itemid) {
		this.itemid = itemid;
	}

	public int getUsetype() {
		return usetype;
	}

	public void setUsetype(int usetype) {
		this.usetype = usetype;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getParamtype() {
		return paramtype;
	}

	public void setParamtype(int paramtype) {
		this.paramtype = paramtype;
	}

	public int getAuto() {
		return auto;
	}

	public void setAuto(int auto) {
		this.auto = auto;
	}

	public int getAddgroupid() {
		return addgroupid;
	}

	public void setAddgroupid(int addgroupid) {
		this.addgroupid = addgroupid;
	}

	public byte getType() {
        return 68;
    }

}
