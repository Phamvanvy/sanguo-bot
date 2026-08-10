package com.pip.itimes.server.world.game;

import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.WorldPlayer;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface InstanceModel {

    public Instance tryGotoInstance(int instance,WorldPlayer player, int battleID) throws InstanceException;

    public GameMap getGameMap(WorldPlayer player,short mapId);

    public GameMap getLoginMap(WorldPlayer player,short mapId);

    public Instance getInstance(IPlayerData player,int instanceId);

    public void playerAddedToInstance(IPlayerData player, Instance instance);
}
