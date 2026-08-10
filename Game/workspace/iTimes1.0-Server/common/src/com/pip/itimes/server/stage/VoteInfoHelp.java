package com.pip.itimes.server.stage;

/**
 * 用于内存里的选举记录只有两个属性，id和选角的点数
 * @author wpjiang
 *
 */
public class VoteInfoHelp implements Comparable{
	
	public VoteInfoHelp(String name, int votePoint/*, String playerName*/){
		this.name = name;
		this.votePoint = votePoint;
		//this.playerName = playerName;
	}

	public int getVotePoint() {
		return votePoint;
	}
	public void setVotePoint(int votePoint) {
		this.votePoint = votePoint;
	}
	
	String name; //被选举的玩家id;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	int votePoint; //被选举的点数；
	/**
	 * 被选举的玩家名字
	 */
/*	String playerName;
	
	
	
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}*/
	public int compareTo(Object o){
		VoteInfoHelp other = (VoteInfoHelp)o;
		if(this.votePoint > other.votePoint){
			return -1;
		}else if(this.votePoint < other.votePoint){
			return 1; 
		}
		return 0;
	}
	
}