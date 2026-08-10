package com.pip.itimes.server.stage;

public class EquModleEffect extends Effect {
	private int id;
	private int itemid;
	
	public EquModleEffect(int id, int itemid){
		this.id = id;
		this.itemid = itemid;
	}
	
	@Override
	public byte getType() {
		return 78;
	}
	
	public int getId(){
		return id;
	}
	
	public int getEquid(){
		return itemid;
	}
}
