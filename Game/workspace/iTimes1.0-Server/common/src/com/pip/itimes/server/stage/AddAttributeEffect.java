package com.pip.itimes.server.stage;

public class AddAttributeEffect extends Effect {
	
	private int strength;
    private int agility;
    private int vitality;
    private int intelligence; 
	
	public AddAttributeEffect(int str,int agi,int vit,int inte){
		this.strength = str;
		this.agility = agi;
		this.vitality = vit;
		this.intelligence = inte;
	}
	
	public int getStrength() {
        return this.strength;
    }
	
	public int getAgility() {
        return this.agility;
    }
	
	public int getVitality() {
        return this.vitality;
    }
	
	public int getIntelligence() {
        return this.intelligence;
    }
	
	public byte getType() {
		return 86;
	}

}
