package com.pip.itimes.server.stage;

public class AddBuildProficiencyEffect extends Effect {
	private int value;

    public AddBuildProficiencyEffect(int value) {
        this.value = value;
    }

    public int getValue () {
        return value;
    }

    public byte getType() {
        return 69;
    }
}
