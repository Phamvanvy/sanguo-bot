package com.pip.itimes.server.stage;

public class EggEffect extends Effect {
    private int group;

    public EggEffect(int group) {
        this.group = group;
    }

    public byte getType() {
        return 47;
    }

    public int getGroup(){
        return group;
    }
}
