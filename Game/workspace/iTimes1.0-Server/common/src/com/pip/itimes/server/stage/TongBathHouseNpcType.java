package com.pip.itimes.server.stage;

public class TongBathHouseNpcType extends TaskNpcType {

    private int bathId;

    public TongBathHouseNpcType(int id, String name, int type) {
        super(id, name, type);
    }

    public void setBathId(int bathId) {
        this.bathId = bathId;
    }

    public int getBathId() {
        return bathId;
    }
}
