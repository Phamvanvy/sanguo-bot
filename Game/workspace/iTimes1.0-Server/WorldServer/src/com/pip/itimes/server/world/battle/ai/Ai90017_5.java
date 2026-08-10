package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.BattleStrategy;
import com.pip.itimes.server.world.battle.SkillConstants;

/**
 * @author leo
 * @version 1.0
 */

/**
 * 群鹰出击7，20%
 * 缤纷连击8，20%
 * 缤纷连击7，20%
 * 群鹰出击8，20%
 * 霜冻魔法8，20%
 */
public class Ai90017_5 extends BaseMonsterAI{
    /**
     * 腐蚀光环
     * boss每回合减少自己当前血量的1%的生命，并增加自身物攻魔攻30点，光环开始boss喊话：“你觉得我是在自杀么？！”
     */
    private boolean hale_a = false;
    /**
     * 暴击光环
     * 持续给怪加暴击等级，每回合加70暴击等级。光环开始boss喊话：“到底痛不痛？”
     */
    private boolean hale_b = false;
    /**
     * 命中光环
     * boss每回合增加自身30点命中等级。光环开始boss喊话：“你无法从我的刀下闪过！”
     */
    private boolean hale_c = false;
    /**
     * 闪避光环
     * 持续加闪避等级，每回合加30闪避等级。光环开始boss喊话：“你那迟钝的攻击！”
     */
    private boolean hale_d = false;
    /**
     * 鲁莽光环
     * boss每回合增加自身的物防和魔防50点。光环开始boss喊话：“你的鲁莽对我来说没有任何意义！”
     */
    private boolean hale_e = false;

    public Ai90017_5(){
        addSkill(79, 20, true);
        addSkill(80, 20, true);
        addSkill(7, 20, true);
        addSkill(8, 20, true);
        addSkill(112, 20, true);
    }

    public int getSpecialHp(){
        return 2500000;
    }

    public boolean action(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie, BattleDataProcess battleDataProcess,
                    int round){
    	if(them[0].player!=null && them[0].player.getMap()!=null && them[0].player.getMap().getMapId() == 209){
	    	//Boss1未死亡狂暴
	        if(!monsterDie(them[0], 0xD12003)){
	            shout("你还没资格挑战我!", bs, them);
	            return killAllPlayer(bs, them, them);
	
	        }else{
	            //另2个boss有死亡，则狂暴
	            if(monsterDie(them[0], 0xD12000) || monsterDie(them[0], 0xD12002)){
	                shout("你杀掉了我的兄弟，就别想再杀掉我！", bs, them);
	                return killAllPlayer(bs, them, themPet);
	            }
	        }
    	}
        if(!bs.canAction()){
            defaultCannotActionAction(bs);
            return false;
        }else{
            switch(round){
                case 2:
                    hale_a = true;
                    shout("你觉得我是在自杀么？！", bs, them);
                    break;
                case 3:
                    hale_b = true;
                    shout("到底痛不痛？", bs, them);
                    break;
                case 4:
                    hale_c = true;
                    shout("你无法从我的刀下闪过！", bs, them);
                    break;
                case 5:
                    hale_d = true;
                    shout("你那迟钝的攻击！", bs, them);
                    break;
                case 6:
                    hale_e = true;
                    shout("你的鲁莽对我来说没有任何意义！", bs, them);
                    break;
            }

            //boss每回合减少自己当前血量的1%的生命，并增加自身物攻魔攻30点
            if(hale_a){
                int hpDec = -(bs.hp / 100);
                bs.changeHp(hpDec, battleMovie, battleDataProcess);
                int[] movie = BattleStrategy.makeMovieSub(bs.bsType, index, bs.bsType, index, SkillConstants.SKILL_LIFE_MAGIC, SkillConstants.ANIMATE_NONE, SkillConstants.POSITION_STAY,
                                SkillConstants.OVER_POSITION_BACK, SkillConstants.MOVIE_SPEED_FAST, SkillConstants.HIT_HIT, bs.getDebufStatus(), SkillConstants.ATTACK_NO_CRI, hpDec, 0, 0, 0);
                battleMovie.addElement(movie);

                bs.attributes[BattleSprite.ATTR_PMIN] += 40;
                bs.attributes[BattleSprite.ATTR_PMAX] += 40;
                bs.attributes[BattleSprite.ATTR_MMIN] += 40;
                bs.attributes[BattleSprite.ATTR_MMAX] += 40;
            }

            //每回合加70暴击等级。
            if(hale_b){
                bs.attributes[BattleSprite.ATTR_PCRI] += 90;
                bs.attributes[BattleSprite.ATTR_MCRI] += 90;
            }

            //boss每回合增加自身30点命中等级。
            if(hale_c){
                bs.attributes[BattleSprite.ATTR_PHIT] += 50;
                bs.attributes[BattleSprite.ATTR_MHIT] += 50;
            }

            //每回合加30闪避等级。
            if(hale_d){
                bs.attributes[BattleSprite.ATTR_FLEE] += 50;
            }

            //boss每回合增加自身的物防和魔防50点。
            if(hale_e){
                bs.attributes[BattleSprite.ATTR_PDEF] += 80;
                bs.attributes[BattleSprite.ATTR_MDEF] += 80;
            }

            return useSkillToHighestUnit(bs, pickSkill(), them, themPet);
        }
    }
}
