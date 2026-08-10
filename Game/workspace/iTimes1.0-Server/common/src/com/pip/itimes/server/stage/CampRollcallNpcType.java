package com.pip.itimes.server.stage;

public class CampRollcallNpcType extends TaskNpcType {
	/**
	 * camp = 1黑暗，2光明,0无阵营
	 */
	private int camp;

	public CampRollcallNpcType (int id, String name, int type) {
		super(id, name, type);
	}
	public int getCamp() {
		return camp;
	}
	public void setCamp(int camp) {
		this.camp = camp;
	}
}
