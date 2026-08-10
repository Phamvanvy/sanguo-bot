package com.pip.itimes.server.world;

import com.pip.itimes.server.bean.Player;

/**
 * @author Jeffery
 * @version 1.0
 */
public interface IChatChannel {



    public int getId();
    public String getName();
    public boolean isPrivateChannle();
    public int[] getPlayers();
    public void registry(Player player);
    public void unRegistry(Player player);
}
