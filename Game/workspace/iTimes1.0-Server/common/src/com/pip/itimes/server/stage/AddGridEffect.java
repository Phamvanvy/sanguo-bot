package com.pip.itimes.server.stage;

/**

 * @author Jeffrey
 * @version 1.0
 */
public class AddGridEffect extends Effect{

    private int value;

    public AddGridEffect(int value) {
        this.value = value;
    }

    public byte getType() {
        return 6;
    }

    public int getValue(){
        return value;
    }
}
