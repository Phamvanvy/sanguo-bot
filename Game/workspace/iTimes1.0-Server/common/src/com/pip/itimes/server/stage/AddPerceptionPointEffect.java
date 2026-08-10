package com.pip.itimes.server.stage;

public class AddPerceptionPointEffect extends Effect {
    private int value;

    public AddPerceptionPointEffect(int value) {
        this.value = value;
    }

    public int getValue () {
        return value;
    }

    public byte getType() {
        return 66;
    }
}
