package com.pip.itimes.server.stage;

public class ResetSinglePropertyEffect extends Effect {

    private int property;

    public ResetSinglePropertyEffect(int property) {
        this.property = property;
    }


    public byte getType() {
        return 40;
    }

    public int getProperty(){
        return property;
    }
}
