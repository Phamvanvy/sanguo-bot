package com.pip.itimes.server.stage;

/**
 * 宠物铠化石
 * @author Lelonte
 *
 */
public class PetArmorGemstoneEffect extends Effect {

	private int value;		// 改变的值（0,此部位可装备；1此部位已有装备；2此部位未解凯）
	private int parts;		// 宠物解铠的部位（头到脚，0-9）
	private int probability;// 开启的几率
 
	public PetArmorGemstoneEffect(int parts, int value, int probability) {
		this.parts = parts;
		this.value = value;
		this.probability = probability;
	}
	
	public int getParts() {
		return parts;
	}
	
	public int getValue() {
		return value;
	}
	
	public int getProbability() {
		return probability;
	}
	
	public byte getType() {
		// TODO Auto-generated method stub
		return 58;
	}

}