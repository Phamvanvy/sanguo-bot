package com.pip.itimes.server.stage;

/**
 * 选举npc
 * @author wpjiang
 *
 */
public class VoteNpcType extends TaskNpcType {
	public VoteNpcType(int id, String name, int type){
        super(id, name, type);
    }
	/**
	 * 选举类型
	 */
	int voteType;
	public int getVoteType() {
		return voteType;
	}
	public void setVoteType(int voteType) {
		this.voteType = voteType;
	}
	
}
