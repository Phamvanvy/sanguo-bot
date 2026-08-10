package com.pip.itimes.server.stage;

public class CampleaderEffect extends Effect{

	private int hpEffect;
	
	public CampleaderEffect(int hp){
		this.hpEffect = hp;
	}
	
	public int getHpEffect(){
		return this.hpEffect;
	}
	
	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return 89;
	}

}
