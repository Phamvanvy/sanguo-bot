package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.SkillConstants;

public class Ai90013_4 extends BaseMonsterAI{
	//mengjie add consort lock  
    public static final Ability crash8 = Ability.getAbility(24); //8级崩溃一击
//    血量为：玩家级别数*80
//    怪物如遇到队伍中任何一个人使用荆棘之墙或吸收攻击则使用崩溃一击，其他情况都使用普通攻击，且只攻击人物，不攻击宠物
//    怪物初始属性（力智体敏）为玩家中最高级别人物的实际属性值（穿装以后）加1点。
//    身上有“同心钥”物品的玩家战斗
//    每回合每人额外增加伤害为：级别*5
//    身上无“同心钥”物品的玩家战斗
//    每回合每人额外增加伤害为：级别*100
//    只有普通物理攻击和聚法一击对其有效，按正常伤害计算。其他所有技能都免疫，伤害统一为1点。
    public int DamageAdd = 0;
    public boolean isconsort = false;
    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                      BattleSprite[] them, BattleSprite[] ourPet,
                      BattleSprite[] themPet, Vector battleMovie,
                      BattleDataProcess battleDataProcess,int round) {
    	if (round == 1) {
        	//初始化
            defenceAllDebuff(bs);
            int maxlevel = 0;
            for(int i = 0; i < them.length; i++){
            	if (maxlevel < them[i].player.getLevel()){
            		maxlevel = them[i].player.getLevel();
            	}
            }            
//            //判断是否有同心钥匙
//            if (them != null){
//                if (them.length == 3){
//                	if ((them[0].player.getItemCount(200327)>0) || 
//                			(them[1].player.getItemCount(200327)>0) || 
//                			(them[2].player.getItemCount(200327)>0)){
//                    	isconsort = true;
//                    }else{
//                    	isconsort = false;
//                    }
//                }else if (them.length == 2){
//                	if ((them[0].player.getItemCount(200327)>0) || 
//                			(them[1].player.getItemCount(200327)>0)){
//                    	isconsort = true;
//                    }else{
//                    	isconsort = false;
//                    }
//                }else if (them.length <= 1){
//                    if (them[0].player.getItemCount(200327)>0){
//                    	isconsort = true;
//                    }else{
//                    	isconsort = false;
//                    }
//                }
//            }
            //判断是为1个人
            if (them != null){
                if (them.length == 3){
                	isconsort = false;
                }else if (them.length == 2){
                	isconsort = false;
                }else if (them.length <= 1){
                	isconsort = true;
                }
            }
            if (isconsort){
            	DamageAdd = maxlevel * 5;
            }else{
            	DamageAdd = maxlevel * 100;            	
            }
        }
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
        	bs.AddAttrBuf(1,0,0,0,0,0,0,0,0,0,DamageAdd,DamageAdd,100003);
        	int flag = 0;
            for(int i = 0; i < them.length; i++){
            	if(them[i].skill.effect == SkillConstants.EFFECT_ANTI_PHY){
            		//发现是荆棘之墙改变为崩溃一击
            		flag = 1;
            	}else if(them[i].skill.effect == SkillConstants.EFFECT_PHY_DMG_TO_HP){
            		//发现是攻击吸收改变为崩溃一击
            		flag = 1;
            	}else if(them[i].skill.effect == SkillConstants.SKILL_ATTACK){
            	}else if(them[i].skill.effect == SkillConstants.SKILL_NOT_READY){	
            	}else if(them[i].skill.effect == SkillConstants.ANIMATE_ASS_START){
            		
            	}else{
            		//免疫所有非普通攻击和聚法一击攻击
            		them[i].AddAttrBuf(1,0,0,0,0,0,0,0,0,0,-9999,-9999,100004);
            	}
            }
            for(int i = 0; i < themPet.length; i++){
            	//免疫所有宠物攻击
            	if(themPet[i] != null){
            		themPet[i].AddAttrBuf(1,0,0,0,0,0,0,0,0,0,-9999,-9999,100004);
            	}
            }
            if (flag == 0){
            	return useDefaultAttackToHighestUnit_nopet(bs, them, themPet);
            }else{
            	return useAbilityToHighestUnit_nopet(bs, crash8, them, themPet);
            }
        }
        
    }
}
