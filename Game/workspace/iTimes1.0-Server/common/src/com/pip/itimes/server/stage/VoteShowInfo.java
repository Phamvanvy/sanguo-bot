package com.pip.itimes.server.stage;

/**
 * 选举列表中的用于进行展示玩家的细节
 * @author wpjiang
 *
 */
public class VoteShowInfo {

	int level;
	boolean valid;
	String playerName;
	String tongName;
	/**
	 * 选举宣言
	 */
	String voteContent;
	
	public VoteShowInfo(int level, String playerName, String tongName, String voteContent, boolean valid){
		this.level = level;
		this.playerName = playerName;
		this.tongName = tongName;
		this.voteContent = voteContent;
		this.valid = valid;
	}
	
	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getTongName() {
		return tongName;
	}

	public void setTongName(String tongName) {
		this.tongName = tongName;
	}

	public String getVoteContent() {
		return voteContent;
	}

	public void setVoteContent(String voteContent) {
		this.voteContent = voteContent;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public boolean getValid () {
		return valid;
	}
	
	public void setValid (boolean valid) {
		this.valid = valid;
	}
}
