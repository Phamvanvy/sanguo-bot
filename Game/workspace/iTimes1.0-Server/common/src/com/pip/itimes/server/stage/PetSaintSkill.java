package com.pip.itimes.server.stage;

public class PetSaintSkill {
	private int playerId;
	private String petName;
	private int petId;
	private int evolutionLevel;
	private int[] skillId;
	public PetSaintSkill() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public PetSaintSkill(int playerId, String petName,
			int petId, int evolutionLevel, int[] skillId) {
		super();
		this.playerId = playerId;
		this.petName = petName;
		this.petId = petId;
		this.evolutionLevel = evolutionLevel;
		this.skillId = skillId;
	}
	public int getPlayerId() {
		return playerId;
	}
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	public String getPetName() {
		return petName;
	}
	public void setPetName(String petName) {
		this.petName = petName;
	}
	public int getPetId() {
		return petId;
	}
	public void setPetId(int petId) {
		this.petId = petId;
	}
	public int getEvolutionLevel() {
		return evolutionLevel;
	}
	public void setEvolutionLevel(int evolutionLevel) {
		this.evolutionLevel = evolutionLevel;
	}
	public int[] getSkillId() {
		return skillId;
	}
	public void setSkillId(int[] skillId) {
		this.skillId = skillId;
	}
	
}
