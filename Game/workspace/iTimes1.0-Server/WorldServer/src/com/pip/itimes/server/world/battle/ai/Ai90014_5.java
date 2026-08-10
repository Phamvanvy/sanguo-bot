package com.pip.itimes.server.world.battle.ai;

import java.util.Random;
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
 * 此boss会给玩家一个中毒buff，3回合后如果玩家的中毒buff没有解除，
 * 那么boss会秒杀掉当前战场上中有毒buff的玩家，并且每死亡一个玩家boss回复33%的血量。
 * boss会使用一些简单的攻击技能，开放，狂暴，崩溃，聚法，群鹰7，缤纷7等。使用几率百分之30。
 * 如果对方使用了荆棘之墙，则boss100%使用崩溃。
 * @author yufengchen
 *
 */
public class Ai90014_5 extends BaseMonsterAI {

	
	public static final Ability ability1 = Ability.getAbility(88); //致命之毒8
	
	public static final Ability ability3 = Ability.getAbility(40);	//狂暴8
	public static final Ability ability4 = Ability.getAbility(24); //8级崩溃一击
	public static final Ability ability5 = Ability.getAbility(72);	//聚法8
	public static final Ability ability6 = Ability.getAbility(79); //群鹰7
	public static final Ability ability7 = Ability.getAbility(7); //缤纷7
	public static final Ability ability8 = Ability.getAbility(112); 	//霜冻魔法
	
	public int roundIndex = 1;
	public static int demag = 0;
	public boolean action(BattleSprite bs, int index, BattleSprite[] our,
			BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet,Vector battleMovie,
            BattleDataProcess battleDataProcess,
			int round) {
		// 免疫状态
		if(round == 1){		//免疫所有状态
			demag = getMonsterLife(them[0],4);
			defenceAllDebuff(bs);
		}
		// 宠物使用了免疫技能，则杀死全部玩家
		
		int nowDemag = bs.getAttribute(5);
		int nowhit	= bs.getAttribute(8);
		bs.AddAttrBuf(1,0,0,0,0,0,0,0,0,0,nowDemag*demag,nowhit*demag,100003);
		
		if(themPet != null){
			for(int i = 0; i < themPet.length; i++){
				if(themPet[i] != null && themPet[i].skill.effect == Skill.EFFECT_PET_IMM_STATUS){
					//宠物使用了免疫
					for(int j = 0; j < them.length; j ++){
						them[j].setDeBufStatus(1, Skill.STATUS_SLEEP, 0, 0, 0, 0, 0);
					}
					bs.AddAttrBuf(1,0,0,0,0,0,0,100000,0,0,100000,100000,100003);
					return useAbilityToHighestUnit(bs,ability8,them,themPet);
				}	
			}
		}
		int flag = 0;
		boolean ispoison = false;
		if(round == 1){	//第一回合
			roundIndex = round;
			bs.AddAttrBuf(100,0,0,0,0,0,0,100000,0,0,0,0,100003);
			return useAbilityToHighestUnitTwo(bs,ability1,them,themPet);
		}else{
			if(round >= 40){
				//秒杀所有玩家
//	        	for(int i = 0; i < them.length; i++){
//	        		if(bs.monster.getName().equals("狄奥尔提斯")){
//	        			Server.instance.chatService.sendPrivateRoarMessage(-10, "狮子吼", "愚蠢的人类，下地狱去吧！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0, them[i].id, bs.monster.getName());
//	        		}
//	        	}
//				antiDefence(bs);				//本回合的任何攻击附带崩溃一击效果	
//				bs.AddAttrBuf(1,0,0,0,0,0,0,100000,0,0,100000,100000,100003);
//				return useAbilityToHighestUnit(bs,ability8,them,themPet);
				shout("愚蠢的人类，下地狱去吧！", bs, themPet);
				return killAllPlayer(bs, them, themPet);
			}
			
			for(int i = 0; i < them.length; i++){
				if(them[i].getDebufStatus() == Skill.STATUS_POISON){
					//有玩家处于，中毒状态
					ispoison = true;
					them[i].changeHp(-2000, null, null);
					if((round - roundIndex) >= 3){
						them[i].changeHp(-them[i].attributes[BattleSprite.
					                                            ATTR_HPMAX], null, null);
						if(them[i].skill.effect ==SkillConstants.EFFECT_SAVE_LIFE ){
							// 玩家使用了再生之灵
							them[i].setBufStatus(1, 0, 0, -1000, 0, 0, 0);
						}
						bs.hp += getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],5);
						if(bs.hp >= bs.attributes[BattleSprite.ATTR_HPMAX]){
							bs.hp = bs.attributes[BattleSprite.ATTR_HPMAX];
						}
					}
				}
				
			}
			for(int i = 0; i < themPet.length; i++){
				if(themPet[i] != null && themPet[i].getDebufStatus() == Skill.STATUS_POISON){
					//有玩家处于，中毒状态
					ispoison = true;
					themPet[i].changeHp(-2000, null, null);
					if((round - roundIndex) >= 3){
						themPet[i].changeHp(-them[i].attributes[BattleSprite.
				                                            ATTR_HPMAX], null, null);
						bs.hp += getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],5);
						if(bs.hp >= bs.attributes[BattleSprite.ATTR_HPMAX]){
							bs.hp = bs.attributes[BattleSprite.ATTR_HPMAX];
						}
					}
				}
				
			}
			if(ispoison){

				if(Utils.hit(50, 100)){
					for(int i = 0; i < them.length; i++){
			        	if(them[i].skill.effect == SkillConstants.EFFECT_ANTI_PHY){
			        		//发现是荆棘之墙改变为崩溃一击
			        		flag = 1;
			        	}
			        }
					if (flag == 0){
						Random rnd = new Random();
			        	int rndI = rnd.nextInt(5);
			        	if(rndI == 0){
			        		return useAbilityToHighestUnit(bs, ability7, them, themPet);
			        	}else if(rndI == 1){
			        		return useAbilityToHighestUnit(bs, ability3, them, themPet);
			        	}else if(rndI == 2){
			        		return useAbilityToHighestUnit(bs, ability4, them, themPet);
			        	}else if(rndI == 3){
			        		return useAbilityToHighestUnit(bs, ability5, them, themPet);
			        	}else{
			        		return useAbilityToHighestUnit(bs, ability6, them, themPet);
			        	}
			        }else{
			        	return useAbilityToHighestUnit(bs, ability4, them, themPet);
			        }
				}else{
					return useDefaultAttackToHighestUnit(bs,them,themPet);
				}		
			}else{
				roundIndex = round;
				bs.AddAttrBuf(100,0,0,0,0,0,0,100000,0,0,0,0,100003);
				return useAbilityToHighestUnitTwo(bs,ability1,them,themPet);
			}
		}
		
		
	}

}
