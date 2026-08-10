package com.pip.itimes.server.stage;

public class HouseNpcType extends TaskNpcType {

    private short areaId;

    public HouseNpcType(int id, String name, int type) {
        super(id, name, type);
    }

    public void setAreaId(short areaId) {
        this.areaId = areaId;
    }

    public short getAreaId() {
        return areaId;
    }
}
