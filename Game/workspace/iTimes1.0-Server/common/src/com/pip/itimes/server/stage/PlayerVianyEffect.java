package com.pip.itimes.server.stage;

public class PlayerVianyEffect extends Effect{
	private byte vianyType;

    public PlayerVianyEffect (byte vianyType) {
    	this.vianyType = vianyType;
    }
    
    public byte getType () {
        return 75;
    }
    
    public byte getVianyType(){
    	return vianyType;
    }
}
