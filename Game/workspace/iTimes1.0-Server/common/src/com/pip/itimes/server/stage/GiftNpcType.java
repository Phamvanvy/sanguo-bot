package com.pip.itimes.server.stage;

public class GiftNpcType extends TaskNpcType {
    private int[] giftGroupIds = new int[0];
    
	public GiftNpcType(int id, String name, int type) {
        super(id, name, type);
    }
	
	public int[] getGiftGroupIds(){
	    return giftGroupIds;
	}
	
	public void setGiftGroupIds(int[] giftGroupIds){
	    this.giftGroupIds = giftGroupIds;
	}
}
