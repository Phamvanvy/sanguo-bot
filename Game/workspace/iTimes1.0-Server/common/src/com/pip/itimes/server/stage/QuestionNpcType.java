package com.pip.itimes.server.stage;

public class QuestionNpcType extends TaskNpcType{
	public QuestionNpcType(int id, String name, int type) {
		super(id, name, type);
	}
	public int id;
	
	public int getTypeId() {
		return id;
	}
	public void setTypeId(int id) {
		this.id = id;
	}
	
	private int version;

	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
}
