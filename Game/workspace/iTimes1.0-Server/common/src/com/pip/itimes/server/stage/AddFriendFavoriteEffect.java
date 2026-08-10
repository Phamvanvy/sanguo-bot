package com.pip.itimes.server.stage;

public class AddFriendFavoriteEffect extends Effect {
	
	private int count;
	 
	public AddFriendFavoriteEffect(int count) {
        this.count = count;
    }
	public int getCount(){
	        return count;
	    }
	public byte getType() {
        return 51;
    }
}