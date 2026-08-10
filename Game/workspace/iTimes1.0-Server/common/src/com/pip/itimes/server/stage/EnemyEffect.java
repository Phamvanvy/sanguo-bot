package com.pip.itimes.server.stage;

public class EnemyEffect extends Effect {

    private int level;

    public EnemyEffect() {
    }


    public byte getType() {
        return 38;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
