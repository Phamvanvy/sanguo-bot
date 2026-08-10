package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class HopeGrassFinderEffect extends Effect{

    private int grassType;

    public HopeGrassFinderEffect(int grassType) {
        this.grassType = grassType;
    }

    public int getGrassType(){
        return grassType;
    }

    public byte getType(){
        return 13;
    }
}
