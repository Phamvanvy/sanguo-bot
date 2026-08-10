package com.pip.itimes.server.stage;

public class CampVoteNpcType extends TaskNpcType {

	private int camp;		// 阵营：0为光明，1为黑暗

	public CampVoteNpcType(int id,String name,int type){
		super(id, name, type);
	}
	public int getCamp() {
		return camp;
	}
	public void setCamp(int camp) {
		this.camp = camp;
	}
	
}
