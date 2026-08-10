package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.SkillConstants;

public class Ai90013_1 extends BaseMonsterAI{
	//mengjie add consort key  
    public static final Ability crash8 = Ability.getAbility(24); //8级崩溃一击
//    血量为：男女角色较高级别数*200
//    怪物如遇到队伍中任何一个人使用荆棘之墙或吸收攻击则使用崩溃一击，其他情况都使用普通攻击，且只攻击人物，不攻击宠物
//    怪物初始属性（力智体敏）为玩家中最高级别人物的实际属性值（穿装以后）加1点。
//    如果是一男一女角色（相互有好度大于等于99）组队战斗
//    每回合每人额外增加伤害为：男女角色高级别*10
//    如果除上述情况组队战斗
//    每回合每人额外增加伤害为：男女角色高级别*100
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
            //判断是否为一男一女角色（相互有好度大于等于99）组队
            if (them != null){
                if (them.length == 3){
                	isconsort = false;
                }else if (them.length == 2){
                    if ((them[1] != null) && (them[0] != null) && (them[0].player instanceof WorldPlayer) && (them[1].player instanceof WorldPlayer)){
                    	WorldPlayer wp0 = (WorldPlayer)them[0].player;
                    	WorldPlayer wp1 = (WorldPlayer)them[1].player;
                    	if ((wp0.getFriendFavorite(wp1) >= 99) & 
                    			(wp1.getFriendFavorite(wp0) >= 99)){
                    		if (them[0].player.getSex() == 0){
                    			if (them[1].player.getSex() ==1){
                    				isconsort = true;
                    			}else{
                    				isconsort = false;
                    			}
                    		}else{
                    			if (them[1].player.getSex() ==0){
                    				isconsort = true;
                    			}else{
                    				isconsort = false;
                    			}
                    		}
                    	}else{
                    		isconsort = false;
                    	}
                    }else if ((them[1] == null) && (them[0] != null)){
                    	isconsort = false;
                    }else{
                    	isconsort = false;
                    }
                }else if (them.length <= 1){
                    if (them[0] != null){
                    	isconsort = false;
                    }else{
                    	isconsort = false;
                    }
                }
            }
            if (isconsort){
            	DamageAdd = maxlevel * 10;
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
