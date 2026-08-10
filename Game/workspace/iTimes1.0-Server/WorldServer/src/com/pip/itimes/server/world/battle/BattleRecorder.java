package com.pip.itimes.server.world.battle;

import java.util.Set;
import java.util.HashSet;

/**
 * @author Jeffery
 * @version 1.0
 */
public class BattleRecorder {
    public BattleSprite src;
    public Set dest = new HashSet();
    public Skill skill;

    public BattleRecorder(BattleSprite src,Skill skill){
        this.src = src;
        this.dest = dest;
        this.skill = skill;
    }

    public BattleSprite[] getDests(){
        BattleSprite[] ret = new BattleSprite[dest.size()];
        dest.toArray(ret);
        return ret;
    }

    public void addDest(BattleSprite dest){
        this.dest.add(dest);
    }
}
