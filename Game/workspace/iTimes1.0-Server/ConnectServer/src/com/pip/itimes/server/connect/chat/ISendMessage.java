package com.pip.itimes.server.connect.chat;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface ISendMessage {
    public static final int WORLD = -1;
    public static final int MAP = -2;
    public static final int GUILD = -3;
    public static final int GROUP = -4;
    public static final int TEAM = -5;
    public static final int FAVORITE = -6;
    public static final int SYSTEM = -7;

    public int getSrcId();

    public String getSrcName();

    public int[] getDestIds();

    public boolean isPrivate();

    public String getMessage();

}
