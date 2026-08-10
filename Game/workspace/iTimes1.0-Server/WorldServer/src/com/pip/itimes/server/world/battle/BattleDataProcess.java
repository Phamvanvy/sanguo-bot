package com.pip.itimes.server.world.battle;

import java.util.Hashtable;
import java.util.Vector;

public interface BattleDataProcess{
    public Hashtable spriteDoneSkill = new Hashtable();
    public Vector battleMovie = new Vector(); 
    
    public BattleSprite getSprite(int spriteType, int spriteIndex);
    public void spriteDoneSkill(BattleSprite bs, int index, boolean force);
    public int getRound();
}
