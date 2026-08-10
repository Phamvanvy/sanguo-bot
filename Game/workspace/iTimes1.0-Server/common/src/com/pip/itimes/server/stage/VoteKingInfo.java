package com.pip.itimes.server.stage;

public class VoteKingInfo {
	
	String playerName;
	String tongName;
	int level;
	long votes;
	
	public VoteKingInfo(int level, String playerName, String tongName, long votes){
		this.level = level;
		this.playerName = playerName;
		this.tongName = tongName;
		this.votes = votes;
	}
	
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public void setTongName(String tongName) {
		this.tongName = tongName;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public void setVotes (long votes) {
		this.votes = votes;
	}

	public String getPlayerName() {
		return playerName;
	}

	public String getTongName() {
		return tongName;
	}

	public int getLevel() {
		return level;
	}
	
	public long getVotes () {
		return votes;
	}
}
