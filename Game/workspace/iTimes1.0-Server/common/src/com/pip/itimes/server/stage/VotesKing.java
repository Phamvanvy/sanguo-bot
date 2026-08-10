package com.pip.itimes.server.stage;

public class VotesKing implements Comparable {
	int id; 		// King ID;
	long votes; 		// King VOTE£»
	
	public VotesKing(int id, long votes){
		this.id = id;
		this.votes = votes;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getvotes() {
		return votes;
	}
	public void setvotes(int votes) {
		this.votes = votes;
	}
	
	public int compareTo(Object o){
		VotesKing other = (VotesKing)o;
		if(this.votes > other.votes){
			return -1;
		}else if(this.votes < other.votes){
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
