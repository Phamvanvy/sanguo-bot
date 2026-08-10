package com.pip.itimes.server.stage;

public class SuperQimoneyEffect extends Effect {

    private int outtype;
    private int imoney;

    public SuperQimoneyEffect(int outtype,int imoney) {
        this.outtype = outtype;
        this.imoney = imoney;
    }

	public int getOuttype() {
		return outtype;
	}

	public void setOuttype(int outtype) {
		this.outtype = outtype;
	}

	public int getImoney() {
		return imoney;
	}

	public void setImoney(int imoney) {
		this.imoney = imoney;
	}

	public byte getType() {
		return 53;
	}

}
