package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.BossTips;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.Skill;
import com.pip.itimes.server.world.battle.SkillConstants;

/**
 * 20回合后狂暴，秒杀对战的全部玩家。
 * 每5回合释放一次缤纷7
 * 每10回合释放一次彩8
 * @author yufengchen
 *
 */
public class Ai90014_1 extends BaseMonsterAI {

	public static final Ability ability1 = Ability.getAbility(7); 	//缤纷7
	public static final Ability ability2 = Ability.getAbility(144);	//彩8
	public static final Ability ability3 = Ability.getAbility(112); 	//霜冻魔法
	public static final Ability ability4 = Ability.getAbility(8); 	//缤纷8
	public static final Ability ability5 = Ability.getAbility(24); //8级崩溃一击

	public boolean action(BattleSprite bs, int index, BattleSprite[] our,
			BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet,
			Vector battleMovie,
            BattleDataProcess battleDataProcess,int round) {
		int flag = 0;
		if(round == 1){		//免疫所有状态
			defenceAllDebuff(bs);
		}
		bs.AddAttrBuf(1, 0, 0, 0, 0, 0, 0, 100000, 0, 0, 0, 0, 0);		// 所有的玩家都不能miss
		if(round <= 20){		
			for(int i = 0; i < them.length; i++){
	        	if(them[i].skill.effect == SkillConstants.EFFECT_ANTI_PHY){
	        		//发现是荆棘之墙改变为崩溃一击
	        		flag = 1;
	        	}
	        }
			if(flag == 1){
				return useAbilityToHighestUnit(bs, ability5, them, themPet);
			}else{
				if( round == 2 || round == 10){									//第2，10回合，使用彩8
					return useAbilityToHighestUnit(bs,ability2,them,themPet);
				}else if(round == 4 || round == 14 || round >= 19){				//第4，14，19以后回合，使用缤纷7
					return useAbilityToHighestUnit(bs,ability1,them,themPet); 
				}else if(round == 8 || round == 16 || round == 18){				//第8，16，18回合，使用缤纷8
					return useAbilityToHighestUnit(bs,ability4,them,themPet);
				}else{
					return useDefaultAttackToHighestUnit(bs,them,themPet);
				}
			}		
		}else{
			//秒杀所有玩家
        	//for(int i = 0; i < them.length; i++){
        		//if(bs.monster.getName().equals("伦特")){
        		//	Server.instance.chatService.sendPrivateRoarMessage(-10, "狮子吼", "愚蠢的人类，下地狱去吧！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0, them[i].id, bs.monster.getName());
        		//}
        	//}
			shout("愚蠢的人类，下地狱去吧！",bs,them);
			antiDefence(bs);				//本回合的任何攻击附带崩溃一击效果			
			bs.AddAttrBuf(1,0,0,0,0,100,0,1000000,0,0,1000000,1000000,100003);
			return useAbilityToHighestUnit(bs,ability3,them,themPet);
			//return killAllPlayer(bs, them, them);
			
		}
	}

}
