package com.pip.itimes.server.stage;

public class RoleFaceNpcType extends TaskNpcType {

    private int face;

    public RoleFaceNpcType(int id, String name, int type) {
        super(id, name, type);
    }

    public int getFace(){
        return face;
    }

    public void setFace(int face){
        this.face = face;
    }
}
