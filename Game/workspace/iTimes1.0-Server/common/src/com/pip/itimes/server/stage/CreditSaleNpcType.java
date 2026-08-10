package com.pip.itimes.server.stage;

public class CreditSaleNpcType extends TaskNpcType {

	public CreditSaleNpcType(int id, String name, int type) {
        super(id, name, type);
    }
	private int clazz;
    private short areaId;
    public int getClazz(){
        return clazz;
    }

    public void setClazz(int clazz){
        this.clazz = clazz;
    }

    public short getAreaId(){
        return areaId;
    }

    public void setAreaId(short areaId){
        this.areaId = areaId;
    }
}
