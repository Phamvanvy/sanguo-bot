package com.pip.itimes.server.stage;

public class ActivationCodeNpcType extends TaskNpcType {
	public int typeId;
	public ActivationCodeNpcType(int id, String name, int type) {
        super(id, name, type);
    }
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	
}
