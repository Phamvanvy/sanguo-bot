package com.pip.itimes.server.world.ItemGroup;

public class PetEvolutionTopData {
	private int playerId;
	private String playerName;
	private int camp;//阵营
	private String petName;
	private int petId;
	private String evoluitonName;
	private int evolutionLevel;
	private int evolutionCurrentPoint;
	private int evolutionPoint;
	public PetEvolutionTopData() {
	
	}	
	public PetEvolutionTopData(int playerId, String playerName, int camp,
			String petName, int petId, String evoluitonName,
			int evolutionLevel, int evolutionCurrentPoint, int evolutionPoint) {
		super();
		this.playerId = playerId;
		this.playerName = playerName;
		this.camp = camp;
		this.petName = petName;
		this.petId = petId;
		this.evoluitonName = evoluitonName;
		this.evolutionLevel = evolutionLevel;
		this.evolutionCurrentPoint = evolutionCurrentPoint;
		this.evolutionPoint = evolutionPoint;
	}


	public int getPlayerId() {
		return playerId;
	}
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public int getCamp() {
		return camp;
	}
	public void setCamp(int camp) {
		this.camp = camp;
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
	public String getEvoluitonName() {
		return evoluitonName;
	}
	public void setEvoluitonName(String evoluitonName) {
		this.evoluitonName = evoluitonName;
	}
	public int getEvolutionLevel() {
		return evolutionLevel;
	}
	public void setEvolutionLevel(int evolutionLevel) {
		this.evolutionLevel = evolutionLevel;
	}
	public int getEvolutionCurrentPoint() {
		return evolutionCurrentPoint;
	}
	public void setEvolutionCurrentPoint(int evolutionCurrentPoint) {
		this.evolutionCurrentPoint = evolutionCurrentPoint;
	}
	public int getEvolutionPoint() {
		return evolutionPoint;
	}
	public void setEvolutionPoint(int evolutionPoint) {
		this.evolutionPoint = evolutionPoint;
	}
	public String toString(){
		return petName + "(" + playerName + "的宠物) :" + evoluitonName + "  进化点数:" + evolutionPoint;
	}
}
