package com.pip.itimes.server.stage;

public class CMCCKeyEffect extends Effect {
	private int cmcctype;//物品效果中配置，1：广东金钥匙；2：广东幸运礼券；3：福建亚运火炬
    private int group1;
    private int group2;
    private int boxId;
    private int itemId;
    private String msg;

    public CMCCKeyEffect(int cmcctype,int group1,int group2,int itemId,int boxId,String msg) {
    	this.cmcctype = cmcctype;
    	this.group1 = group1;
        this.group2 = group2;
        this.boxId = boxId;
        this.itemId = itemId;
        this.msg = msg;
    }

    public byte getType() {
        return 54;
    }

    public int getGroup1(){
        return group1;
    }

    public int getGroup2(){
        return group2;
    }

    public int getBoxId(){
        return boxId;
    }

	public String getMsg() {
		return msg;
	}

	public int getCmcctype() {
		return cmcctype;
	}

	public void setCmcctype(int cmcctype) {
		this.cmcctype = cmcctype;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
    
}
