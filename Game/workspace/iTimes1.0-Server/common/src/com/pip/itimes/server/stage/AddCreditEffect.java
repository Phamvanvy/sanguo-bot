package com.pip.itimes.server.stage;

public class AddCreditEffect extends Effect {

    private int min;
    private int max;

    public AddCreditEffect(int min,int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin(){
        return min;
    }

    public int getMax(){
        return max;
    }

    public byte getType() {
        return 42;
    }
}
