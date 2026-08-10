package com.pip.itimes.server.stage;

/**
 * 用于内存里的选举记录只有两个属性，id和选角的点数
 * @author wpjiang
 *
 */
public class VoteInfo  implements Comparable{
	
	public VoteInfo(int id, int votePoint/*, String playerName*/){
		this.id = id;
		this.votePoint = votePoint;
		//this.playerName = playerName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getVotePoint() {
		return votePoint;
	}
	public void setVotePoint(int votePoint) {
		this.votePoint = votePoint;
	}
	
	int id; //被选举的玩家id;
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
		VoteInfo other = (VoteInfo)o;
		if(this.votePoint > other.votePoint){
			return -1;
		}else if(this.votePoint < other.votePoint){
			return 1; 
		}
		if(this.id > other.id){
			return -1;
		}else if(this.id < other.id){
			return 1;
		}
		return 0;
	}
	
}
