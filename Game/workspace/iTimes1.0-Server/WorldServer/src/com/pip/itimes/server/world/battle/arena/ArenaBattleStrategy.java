package com.pip.itimes.server.world.battle.arena;

import com.pip.itimes.server.world.battle.Battle2;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.BattleStrategy;

public class ArenaBattleStrategy extends BattleStrategy{
    public void fillSpriteStatus(BattleSprite bs, BattleDataProcess battle){
        if(bs == null)
            return;
        if(bs.bsType == BattleSprite.TYPE_PLAYER){
            bs.setStatus(BattleSprite.SEAL_SKILL_CATCH, true);
            bs.setStatus(BattleSprite.SEAL_SKILL_ITEM,true);
            bs.setStatus(BattleSprite.SEAL_SKILL_RUNAWAY, true);
            bs.setStatus(BattleSprite.SEAL_SKILL_SKILL, false);
        }
        //jwp add start 宠物再生技能恢复
        bs.endProcess(battle.battleMovie, battle);
        //jwp add end 
    }

    public boolean testRun(BattleSprite bs, int enemyLevel, int bout){
        return false;
    }
}
