package com.pip.itimes.server.world.refresh;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class LockException extends Exception{

    private int type;

    public static final int ALREADY_LOCKED = 1;
    public static final int LOCK_FULL = 2;
    public static final int NO_LOCKED = 3;
    public static final int INVISIBLE = 4;
    public static final int INEXISTENCE = 5;

    public LockException(int type) {
        this.type = type;
    }

    public int getType(){
        return type;
    }
}
