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
 * 当此boss血量低于15%时会使用技能，满血复活消耗此boss70%的蓝
 * boss每5回合恢复自身10%的蓝 
 * boss【百分之30的几率】会使用一些简单的攻击技能，开放，狂暴，崩溃，聚法，群7，群8，缤纷7，缤纷8，霜冻
 * @author yufengchen
 *
 */
public class Ai90014_3 extends BaseMonsterAI {

	public static final Ability ability3 = Ability.getAbility(40);	//狂暴8
	public static final Ability ability4 = Ability.getAbility(24); //8级崩溃一击
	public static final Ability ability5 = Ability.getAbility(72);	//聚法8
	public static final Ability ability6 = Ability.getAbility(79);	//群7
	public static final Ability ability7 = Ability.getAbility(80);	//群8
	public static final Ability ability8 = Ability.getAbility(7);	//缤7
	public static final Ability ability9 = Ability.getAbility(8);	//缤8
	public static final Ability ability10 = Ability.getAbility(112);	//霜冻8
	public static int demag = 0;

	public boolean action(BattleSprite bs, int index, BattleSprite[] our,
			BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet,
			Vector battleMovie,
            BattleDataProcess battleDataProcess,int round) {
		// 免疫状态
		if(round == 1){		//免疫所有状态
			demag = getMonsterLife(them[0],2);
			defenceAllDebuff(bs);
		}
		int nowDemag = bs.getAttribute(5);
		int nowhit	= bs.getAttribute(8);
		bs.AddAttrBuf(1,0,0,0,0,0,0,0,0,0,nowDemag*demag,nowhit*demag,100003);
		if(round >= 40){
			//秒杀所有玩家
        	for(int i = 0; i < them.length; i++){
//        		if(bs.monster.getName().equals("安拉旺") || bs.monster.getName().equals("兽人王")){
//        			Server.instance.chatService.sendPrivateRoarMessage(-10, "狮子吼", "愚蠢的人类，下地狱去吧！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0, them[i].id, bs.monster.getName());
//        		}
        		shout("愚蠢的人类，下地狱去吧！", bs, themPet);
        	}
//			antiDefence(bs);				//本回合的任何攻击附带崩溃一击效果	
//			bs.AddAttrBuf(1,0,0,0,0,0,0,100000,0,0,100000,100000,100003);
//			return useAbilityToHighestUnit(bs,ability10,them,themPet);
        	return killAllPlayer(bs, them, themPet);
		}
		int flag = 0;
		if(bs.hp <=
            getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],15)
               && bs.mp >= getPercentValue(bs.attributes[BattleSprite.ATTR_MPMAX],70)){
			Skill skill = (Skill)Skill.getSkill(218).clone();
			skill.parm1 = bs.attributes[BattleSprite.ATTR_HPMAX] - bs.hp;
			skill.mpUse = (short)getPercentValue(bs.attributes[BattleSprite.ATTR_MPMAX],70);
			return useSkillForSelf(bs,skill);
		}else{
			if(round % 5 == 0){
				Skill skill = (Skill)Skill.getSkill(1019).clone();
				skill.parm1 = 200; //getPercentValue(bs.attributes[BattleSprite.ATTR_MPMAX],20);
				skill.mpUse = 0;
				return useSkillForSelf(bs,skill);
			}else{
				for(int i = 0; i < them.length; i++){
		        	if(them[i].skill.effect == SkillConstants.EFFECT_ANTI_PHY){
		        		//发现是荆棘之墙改变为崩溃一击
		        		flag = 1;
		        	}
		        }
				if (flag == 0){
					if(Utils.hit(50, 100)){
						Random rnd = new Random();
			        	int rndI = rnd.nextInt(8);
			        	if(rndI == 0){
			        		return useAbilityToHighestUnit(bs, ability10, them, themPet);
			        	}else if(rndI == 1){
			        		return useAbilityToHighestUnit(bs, ability3, them, themPet);
			        	}else if(rndI == 2){
			        		return useAbilityToHighestUnit(bs, ability4, them, themPet);
			        	}else if(rndI == 3){
			        		return useAbilityToHighestUnit(bs, ability5, them, themPet);
			        	}else if(rndI == 4){
			        		return useAbilityToHighestUnit(bs, ability6, them, themPet);
			        	}else if(rndI == 5){
			        		return useAbilityToHighestUnit(bs, ability7, them, themPet);
			        	}else if(rndI == 6){
			        		return useAbilityToHighestUnit(bs, ability8, them, themPet);
			        	}else{
			        		return useAbilityToHighestUnit(bs, ability9, them, themPet);
			        	}		
					}else{
						return useDefaultAttackToHighestUnit(bs,them,themPet);
					}	
		        }else{
		        	return useAbilityToHighestUnit(bs, ability4, them, themPet);
		        }
				
			}
		}
	}

}
