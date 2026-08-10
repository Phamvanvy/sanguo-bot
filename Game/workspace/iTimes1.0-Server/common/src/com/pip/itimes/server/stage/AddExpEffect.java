package com.pip.itimes.server.stage;

public class AddExpEffect extends Effect {

    private float count;

    public AddExpEffect(float count) {
        this.count = count;
    }

    public float getCount(){
        return count;
    }

    public byte getType() {
        return 41;
    }
}
