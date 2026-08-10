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
 * 此boss由1个boss首领外加2个守卫组成
 * 守卫会给boss加血，加防护，需先击杀2名守卫后才可击杀boss,40回合后狂暴，秒杀对战的全部玩家
 * @author yufengchen
 *
 */
public class Ai90014_4_1 extends BaseMonsterAI {

	public static final Ability ability3 = Ability.getAbility(112); 	//霜冻魔法
	public static final Ability ability6 = Ability.getAbility(79); //群鹰7
	public int dieIndex = -1;
	public static final Ability ability7 = Ability.getAbility(8); //缤8
	public static final Ability ability4 = Ability.getAbility(24); //8级崩溃一击
	public int dieIndex_2 = -1;								//第二个boss复活的次数
	public int demag = 0;
	
	public boolean action(BattleSprite bs, int index, BattleSprite[] our,
			BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet,Vector battleMovie,
            BattleDataProcess battleDataProcess,
			int round) {
		// 免疫状态
		if(round == 1){		//免疫所有状态
			demag = getMonsterLife(them[0],3);
			defenceAllDebuff(bs);
		}
		
		int flag = 0;
		if(our != null && isTeamAllDie(bs,our) && dieIndex == -1){
			dieIndex = round;
		}
		if(round > 40){
			//秒杀所有玩家
        	//for(int i = 0; i < them.length; i++){
        		//if(bs.monster.getName().equals("米艾尔")){
        		//	Server.instance.chatService.sendPrivateRoarMessage(-10, "狮子吼", "愚蠢的人类，下地狱去吧！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0, them[i].id, bs.monster.getName());
        		//}
        	//}
//			antiDefence(bs);				//本回合的任何攻击附带崩溃一击效果	
//			bs.AddAttrBuf(1,0,0,0,0,0,0,0,0,0,100000,100000,100003);
//			return useAbilityToHighestUnit(bs,ability3,them,themPet);
			shout("愚蠢的人类，下地狱去吧！", bs, themPet);
			return killAllPlayer(bs, them, themPet);
		}else{
			if(our != null && isTeamAllDie(bs,our)){
				if((round - dieIndex) >= 10){
					BattleSprite die = getDieTeam(bs, our);
	                if (die != null){
	                	dieIndex = -1;
	                	dieIndex_2 = round;				//第二个BOSS的时间
	                	Skill skill = (Skill)Skill.getSkill(207).clone();
	        			skill.parm1 = die.attributes[BattleSprite.ATTR_HPMAX];
	        			skill.mpUse = 0;
	                	return useSkillForTeam(bs, die, skill);
	                } 
				}
				for(int i = 0; i < them.length; i++){
		        	if(them[i].skill.effect == SkillConstants.EFFECT_ANTI_PHY){
		        		//发现是荆棘之墙改变为崩溃一击
		        		flag = 1;
		        	}
		        }
				if(flag == 1){
					return useAbilityToHighestUnit(bs, ability4, them, themPet);
				}else{
					if((round - dieIndex) % 3 == 0){
						return useAbilityToHighestUnit(bs,ability7,them,themPet);
					}else{
						return useDefaultAttackToHighestUnit(bs,them,themPet);
					}
				}			
			}else{
				if(dieIndex_2 != -1){
					if((round - dieIndex) >= 5){
						BattleSprite die = getDieTeam(bs, our);
		                if (die != null){
		                	dieIndex_2 = -1;				//第二个BOSS的时间
		                	Skill skill = (Skill)Skill.getSkill(207).clone();
		        			skill.parm1 = die.attributes[BattleSprite.ATTR_HPMAX];
		        			skill.mpUse = 0;
		                	return useSkillForTeam(bs, die, skill);
		                }else{
		                	return useAbilityToHighestUnit(bs,ability6,them,themPet);
		                }
					}else{
	                	return useAbilityToHighestUnit(bs,ability6,them,themPet);
	                }	
				}else{
					return useAbilityToHighestUnit(bs,ability6,them,themPet);
				}
				
			}
		}
	}

}
