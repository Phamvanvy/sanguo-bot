package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class FavorEffect extends Effect{

    private int value;

    public FavorEffect(int value) {
        this.value = value;
    }

    public int getFavor(){
        return value;
    }

    public byte getType(){
        return 5;
    }
}
