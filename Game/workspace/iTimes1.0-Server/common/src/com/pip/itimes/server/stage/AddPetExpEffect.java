package com.pip.itimes.server.stage;

public class AddPetExpEffect extends Effect{
	   private int count;

	    public AddPetExpEffect(int count) {
	        this.count = count;
	    }

	    public int getCount(){
	        return count;
	    }

	    public byte getType() {
	        return 49;
	    }
}
