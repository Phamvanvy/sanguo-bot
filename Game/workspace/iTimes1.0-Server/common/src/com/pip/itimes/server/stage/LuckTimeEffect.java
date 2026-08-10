package com.pip.itimes.server.stage;

public class LuckTimeEffect extends Effect{

	private int lucktime;
	
	public LuckTimeEffect(int lucktime){
		this.lucktime = lucktime;
	}
	
	public byte getType() {
		return 81;
	}
	
	public int getLuckTime(){
		return lucktime;
	}

}
