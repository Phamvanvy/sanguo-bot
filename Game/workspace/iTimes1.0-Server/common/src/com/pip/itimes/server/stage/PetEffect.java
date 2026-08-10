package com.pip.itimes.server.stage;

public class PetEffect extends Effect {

    private int level;
    private boolean baby;
    private byte petType;

    public PetEffect() {
        super();
    }

    public byte getType() {
        return 30;
    }

    public int getLevel() {
        return level;
    }
    
    public byte getPetType () {
    	return petType;
    }

    public boolean isBaby() {
        return baby;
    }


    public void setLevel(int level) {
        this.level = level;
    }

    public void setBaby(boolean baby) {
        this.baby = baby;
    }

    public void setPetType (byte petType) {
    	this.petType = petType;
    }
}
