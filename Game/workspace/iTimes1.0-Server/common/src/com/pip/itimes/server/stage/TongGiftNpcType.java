package com.pip.itimes.server.stage;

public class TongGiftNpcType extends TaskNpcType {
	private int tongIslandId;
	private int[] giftGroupIds = new int[0];
	
	public TongGiftNpcType(int id, String name, int type) {
		super(id, name, type);
		
	}
	
	public int getTongIslandId(){
		return tongIslandId;
	}
	
	public void setTongIslandId(int id){
		tongIslandId = id;
	}
	
	public int[] getGiftGroupIds(){
	    return giftGroupIds;
	}
	
	public void setGiftGroupIds(int[] giftGroupIds){
	    this.giftGroupIds = giftGroupIds;
	}
}
