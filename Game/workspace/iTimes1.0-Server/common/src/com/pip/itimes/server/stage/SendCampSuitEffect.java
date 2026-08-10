package com.pip.itimes.server.stage;

public class SendCampSuitEffect extends Effect {
	private int sex;
	private int camp;
    private int level;
	public SendCampSuitEffect(int sex, int camp, int level) {
		this.sex = sex;
		this.camp = camp;
		this.level = level;
	}

	public byte getType() {
		return 64;
	}
	
	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getCamp() {
		return camp;
	}

	public void setCamp(int camp) {
		this.camp = camp;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}
