package com.pip.itimes.server.stage;

public class GemstoneEffect extends Effect {

    private int color;
    private int property;
    private int value;
    private int level;

    public GemstoneEffect(int color,int property,int value,int level) {
        this.color = color;
        this.property = property;
        this.value = value;
        this.level = level;
    }

    public byte getType() {
        return 56;
    }

	public int getColor() {
		return color;
	}

	public int getProperty() {
		return property;
	}

	public int getValue() {
		return value;
	}

	public int getLevel() {
		return level;
	}


    
}
