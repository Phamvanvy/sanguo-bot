package com.pip.itimes.server.world.battle;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PkBattleStrategy extends BattleStrategy {
	public final int[] roundItem = new int[]{10, 25, 50, 75, 100, 10000000, 100000000};
    public void fillSpriteStatus(BattleSprite bs, BattleDataProcess battle) {
        if(bs==null)
            return;
        if((bs.bsType==BattleSprite.TYPE_PLAYER||bs.bsType==BattleSprite.TYPE_MONSTER)&&bs.canAction()){
        	//int time = (int) (5 * Math.pow(2, bs.usedTimes) -1);
        	if((battle.getRound()) >= roundItem[bs.usedTimes]){
        		bs.setStatus(BattleSprite.SEAL_SKILL_ITEM,false);
        		//如果已经到下回合该吃药了，则放弃上回合
        		if((battle.getRound()) >= roundItem[bs.usedTimes + 1]){
        			bs.usedTimes = bs.usedTimes + 1;
        		}
	        }else{
	            bs.setStatus(BattleSprite.SEAL_SKILL_ITEM,true);
	        }
        	
        	/*if((battle.getRound()-bs.lastUsedRound)>=((bs.usedTimes+1)*5-1)){
                bs.setStatus(BattleSprite.SEAL_SKILL_ITEM,false);
            }else{
                bs.setStatus(BattleSprite.SEAL_SKILL_ITEM,true);
            }*/
        	
        	//可以吃药
        /*	int time = (int) (5 * Math.pow(2, bs.usedTimes));
        	if(battle.getRound() % time == 0){
        		bs.setStatus(BattleSprite.SEAL_SKILL_ITEM,false); 
        	}else{
        		 bs.setStatus(BattleSprite.SEAL_SKILL_ITEM,true);
        	}*/
        }
        //jwp add start 宠物再生技能恢复
        bs.endProcess(battle.battleMovie, battle);
        //jwp add end 
    }

    public boolean testRun(BattleSprite bs,int enemyLevel, int bout){
        return false;
    }
}
