package com.pip.itimes.server.world.game;

import com.pip.itimes.server.world.battle.CreditPkBattle;

public interface BattleInstanceModel extends InstanceModel {
    public void start(long forbidEnterTime,long forbidTime,long endTime) throws BattleFieldException;
    public void battleEnded(CreditPkBattle battle);
}
