package com.pip.itimes.server.world.battle;

public class BattleFieldBattleStrategy extends BattleStrategy {

    public void fillSpriteStatus(BattleSprite bs, BattleDataProcess battle) {
        if(bs==null)
            return;
       /* if((bs.bsType==BattleSprite.TYPE_PLAYER||bs.bsType==BattleSprite.TYPE_MONSTER)&&bs.canAction()){
            int round = battle.getRound();
            if(round>=4){
                if(bs.lastUsedRound==0)
                    bs.setStatus(BattleSprite.SEAL_SKILL_ITEM,false);
                else{
                    bs.setStatus(BattleSprite.SEAL_SKILL_ITEM,true);
                }
            }else{
                bs.setStatus(BattleSprite.SEAL_SKILL_ITEM,true);
            }

        }*/
        //jwp add start 宠物再生技能恢复
        bs.endProcess(battle.battleMovie, battle);
        //jwp add end 
    }

    public boolean testRun(BattleSprite bs,int enemyLevel, int bout){
        return false;
    }
}
