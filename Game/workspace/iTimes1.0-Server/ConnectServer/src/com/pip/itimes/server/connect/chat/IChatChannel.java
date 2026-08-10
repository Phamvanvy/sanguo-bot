package com.pip.itimes.server.connect.chat;


/**
 * @author Jeffrey
 * @version 1.0
 */
public interface IChatChannel {
    public int getId();
    public String getName();
    public boolean isPrivateChannle();
    public int[] getPlayers();
    public void registry(int playerId);
    public void unRegistry(int playerId);
}
