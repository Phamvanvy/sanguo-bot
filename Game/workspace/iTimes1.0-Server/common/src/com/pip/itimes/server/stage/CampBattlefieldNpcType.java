package com.pip.itimes.server.stage;

/**
 * 阵营战场NPC类
 * @author hchen
 *
 */
public class CampBattlefieldNpcType extends TaskNpcType {
	private String battlefieldType;
	private int instanceID;
	private int campType;
	private String message;
	
	public CampBattlefieldNpcType (int id, String name, int type) {
		super(id, name, type);
	}
	
	public int getInstanceID() {
		return instanceID;
	}
	public void setInstanceID(int instanceID) {
		this.instanceID = instanceID;
	}
	
	public String getBattlefieldType() {
		return battlefieldType;
	}
	public void setBattlefieldType(String battlefieldType) {
		this.battlefieldType = battlefieldType;
	}
	
	public int getCampType() {
		return campType;
	}
	public void setCampType(int campType) {
		this.campType = campType;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
