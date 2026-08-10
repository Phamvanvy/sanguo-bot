package com.pip.itimes.server.world;

/**
 * @author Jeffery
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
    public static final int GM = -8;
    
    public static final int	CAMP = -9;		 //ÕóÓª
    public static final int NEW = -10;
    public static final int ROAR = -12;	 // Ê¨×Óºð
    public int getSrcId();
    public String getSrcName();
    public int[] getDestIds();
    public boolean isPrivate();
    public String getMessage();
}
