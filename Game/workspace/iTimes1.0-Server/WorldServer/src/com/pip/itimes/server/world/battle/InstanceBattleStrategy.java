package com.pip.itimes.server.world.battle;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class InstanceBattleStrategy extends BattleStrategy {
    public void fillSpriteStatus(BattleSprite bs, BattleDataProcess battle) {
        if(bs==null)
            return;
        if(bs.bsType==BattleSprite.TYPE_PLAYER){
            bs.setStatus(BattleSprite.SEAL_SKILL_CATCH,true);
        }
        //jwp add start 宠物再生技能恢复
        bs.endProcess(battle.battleMovie, battle);
        //jwp add end 
    }

    public boolean testRun(BattleSprite bs,int enemyLevel, int bout){
        return false;
    }
}
