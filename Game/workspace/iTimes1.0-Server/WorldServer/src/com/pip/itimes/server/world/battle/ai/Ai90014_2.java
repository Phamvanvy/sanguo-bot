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
 * 当一个boss死后三回合内，另一个boss如没有死亡，择剩下的这只boss狂暴，秒杀对战的全部玩家。
 * boss1每3回合释放一次缤纷7 boss2每4回合释放一次群鹰7
 * @author yufengchen
 *
 */
public class Ai90014_2 extends BaseMonsterAI {

	public static final Ability ability1 = Ability.getAbility(7); 	//缤纷7
	public static final Ability ability2 = Ability.getAbility(79); 	//群鹰7
	public int round_die = 0;	// 当一个boss死后，开始计算回合数
	public static final Ability ability3 = Ability.getAbility(112); 	//霜冻魔法
	public static final Ability ability4 = Ability.getAbility(24); //8级崩溃一击
	
	public static final Ability ability6 = Ability.getAbility(40);	//狂暴8
	public static final Ability ability7 = Ability.getAbility(24); //8级崩溃一击
	public static final Ability ability8 = Ability.getAbility(72);	//聚法8

	public static  int demag = 0;			//增加的伤害
	public boolean action(BattleSprite bs, int index, BattleSprite[] our,
			BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet,
			Vector battleMovie,
            BattleDataProcess battleDataProcess,int round) {
		// 免疫状态
		int flag = 0;
		if(round == 1){		//免疫所有状态
			demag = getMonsterLife(them[0],1);
			defenceAllDebuff(bs);
		}
		int nowDemag = bs.getAttribute(5);
		int nowhit	= bs.getAttribute(8);
		bs.AddAttrBuf(1,0,0,0,0,0,0,0,0,0,nowDemag*demag,nowhit*demag,100003);
		
		if(round >= 40){
			//秒杀所有玩家
//			antiDefence(bs);				//本回合的任何攻击附带崩溃一击效果	
//			bs.AddAttrBuf(1,0,0,0,0,0,0,100000,0,0,100000,100000,100003);
//			return useAbilityToHighestUnit(bs,ability3,them,themPet);
			return killAllPlayer(bs, them, themPet);
		}
		if(our != null && isTeamAllDie(bs,our)){		//狂暴
			if(round_die == 1){
				for(int i = 0; i < them.length; i++){
					shout("竟敢伤害我的兄弟？！我要你血债血偿！", bs, themPet);
//	        		if(bs.monster.getName().equals("盖玛")){
//	        			Server.instance.chatService.sendPrivateRoarMessage(-10, "狮子吼", "竟敢伤害我的兄弟？！我要你血债血偿！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0, them[i].id, bs.monster.getName());
//	        		} else if(bs.monster.getName().equals("盖菲")){
//	        			Server.instance.chatService.sendPrivateRoarMessage(-10, "狮子吼", "竟敢伤害我的兄弟？！我要你血债血偿！", 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0, them[i].id, bs.monster.getName());
//	        		}
	        	}
			}
			if(round_die >=3){
				//秒杀所有玩家
//				antiDefence(bs);				//本回合的任何攻击附带崩溃一击效果	
//				bs.AddAttrBuf(1,0,0,0,0,0,0,0,0,0,100000,100000,100003);
//				return useAbilityToHighestUnit(bs,ability3,them,themPet);
				return killAllPlayer(bs, them, themPet);
			}
			round_die++;
		}
		
		for(int i = 0; i < them.length; i++){
        	if(them[i].skill.effect == SkillConstants.EFFECT_ANTI_PHY){
        		//发现是荆棘之墙改变为崩溃一击
        		flag = 1;
        	}
        }
		if(flag == 1){
			return useAbilityToHighestUnit(bs,ability4,them,themPet); 
		}else{
			if(round % 3 == 0 || round % 2 == 0){
				if(index == 0){			//boss1
					if(round % 3 == 0){
						return useAbilityToHighestUnit(bs,ability1,them,themPet); 
					}else{
						return useDefaultAttackToHighestUnit(bs,them,themPet);
					}
				}else{					//boss2
					if(round % 2 == 0){
						return useAbilityToHighestUnit(bs,ability2,them,themPet); 
					}else{
						return useDefaultAttackToHighestUnit(bs,them,themPet);
					}
				}
			}else{
				if(Utils.hit(50, 100)){
					Random rnd = new Random();
		        	int rndI = rnd.nextInt(3);
		        	if(rndI == 0){
		        		return useAbilityToHighestUnit(bs, ability6, them, themPet);
		        	}else if(rndI == 1){
		        		return useAbilityToHighestUnit(bs, ability7, them, themPet);
		        	}else{
		        		return useAbilityToHighestUnit(bs, ability8, them, themPet);
		        	}
				}else{
					return useDefaultAttackToHighestUnit(bs,them,themPet);
				}
			}
			
		}
		
	}

}
