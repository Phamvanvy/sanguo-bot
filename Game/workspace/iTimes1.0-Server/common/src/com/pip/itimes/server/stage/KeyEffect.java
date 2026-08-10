package com.pip.itimes.server.stage;

public class KeyEffect extends Effect {

    private int group1;
    private int group2;
    private int boxId;
    private String msg;

    public KeyEffect(int group1,int group2,int boxId,String msg) {
        this.group1 = group1;
        this.group2 = group2;
        this.boxId = boxId;
        this.msg = msg;
    }

    public byte getType() {
        return 39;
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
    
}
