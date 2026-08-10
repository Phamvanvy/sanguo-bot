package com.pip.itimes.server.stage;

public class MassageEffect extends Effect {

    private int deleteflag;
    private String msg;

    public MassageEffect(int deleteflag,String msg) {
        this.deleteflag = deleteflag;
        this.msg = msg;
    }

    public byte getType() {
        return 55;
    }

	public int getDeleteflag() {
		return deleteflag;
	}

	public String getMsg() {
		return msg;
	}
    
}
