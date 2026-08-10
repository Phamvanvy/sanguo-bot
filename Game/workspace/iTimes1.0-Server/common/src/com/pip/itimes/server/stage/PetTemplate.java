package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PetTemplate {


    public PetTemplate() {
    }


    public int getItemId() {
        return 0;
    }

    public String getName() {
        return "";
    }

    public short getLevel() {
        return 0;
    }

    public byte getQuality() {
        return 0;
    }

    public byte getType() {
        return 0;
    }

    public int getPrice() {
        return 0;
    }

    public IItem newInstance() {
        return null;
    }
}
