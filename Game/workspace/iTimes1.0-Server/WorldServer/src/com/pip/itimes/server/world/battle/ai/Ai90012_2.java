package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
//mengjie add moyan100 fishman
public class Ai90012_2 extends BaseMonsterAI {
    public static final Ability chaos = Ability.getAbility(120); //8级混乱
    public static final Ability eagle = Ability.getAbility(79); //群鹰7
    public static final Ability eagle8 = Ability.getAbility(80); //群鹰8
    public static final Ability CS8 = Ability.getAbility(144); //彩云遮日8
    public static final IItem life = Items.getTemplate(120).newInstance(); //全效急救恢复药水

    public boolean mana_used = false;
    public boolean life_used = false;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         20) && !life_used) {
                //mana_used = true;
                life_used = true;
                useItemForSelf(bs, life);
                return false;
            }
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         40) && !mana_used) {
                mana_used = true;
                useAbilityForSelf(bs, CS8);
                return false;
            }
            //判断是否有互为师徒关系
            boolean ismaster = false;
            if (them != null){
                if (them.length == 3){
                	if ((them[2] != null) && (them[1] != null) && (them[0] != null)){
                    	if ((Server.instance.masterService.isMaster(them[0].player)) ||
                    			(Server.instance.masterService.isMaster(them[1].player)) ||
                    			(Server.instance.masterService.isMaster(them[2].player)) ||
            					(Server.instance.masterService.isPrentice(them[0].player)) ||
                    			(Server.instance.masterService.isPrentice(them[1].player)) || 
                    			(Server.instance.masterService.isPrentice(them[2].player))){
                    		ismaster = true;
                    	}else{
                    		ismaster = false;
                    	}
                    }else if ((them[2] == null) && (them[1] != null) && (them[0] != null)){
                    	if ((Server.instance.masterService.isMaster(them[0].player)) ||
                    			(Server.instance.masterService.isMaster(them[1].player)) || 
            					(Server.instance.masterService.isPrentice(them[0].player)) ||
                    			(Server.instance.masterService.isPrentice(them[1].player))){
                    		ismaster = true;
                    	}else{
                    		ismaster = false;
                    	}
                    }else if ((them[2] == null) && (them[1] == null) && (them[0] != null)){
                    	if ((Server.instance.masterService.isMaster(them[0].player)) ||
                    			(Server.instance.masterService.isPrentice(them[0].player))){
                    		ismaster = true;
                    	}else{
                    		ismaster = false;
                    	}
                    }else{
                    	ismaster = false;
                    }
                }else if (them.length == 2){
                    if ((them[1] != null) && (them[0] != null)){
                        if ((Server.instance.masterService.isMaster(them[0].player)) ||
                                (Server.instance.masterService.isMaster(them[1].player)) || 
                                (Server.instance.masterService.isPrentice(them[0].player)) ||
                                (Server.instance.masterService.isPrentice(them[1].player))){
                            ismaster = true;
                        }else{
                            ismaster = false;
                        }
                    }else if ((them[1] == null) && (them[0] != null)){
                        if ((Server.instance.masterService.isMaster(them[0].player)) ||
                                (Server.instance.masterService.isPrentice(them[0].player))){
                            ismaster = true;
                        }else{
                            ismaster = false;
                        }
                    }else{
                        ismaster = false;
                    }
                }else if (them.length <= 1){
                    if (them[0] != null){
                        if ((Server.instance.masterService.isMaster(them[0].player)) ||
                                (Server.instance.masterService.isPrentice(them[0].player))){
                            ismaster = true;
                        }else{
                            ismaster = false;
                        }
                    }else{
                        ismaster = false;
                    }
                }
                
            }
            if (Utils.hit(rnd, 30, 100) &&
                bs.mp >= chaos.getMpUse(bs.level, bs.mp)) { //30％使用混乱魔法
                return useAbilityToHighestUnit(bs, chaos, them, themPet);
            }
            if ((bs.mp >= eagle8.getMpUse(bs.level, bs.mp)) && (ismaster == false)) { //70％使用群鹰
                //非师徒，使用群鹰8
            	return useAbilityToHighestUnit(bs, eagle8, them, themPet);
            } else if ((bs.mp >= eagle.getMpUse(bs.level, bs.mp)) && (ismaster == true)) {
            	//师徒，使用群鹰7攻击全体
            	return useAbilityToHighestUnit(bs, eagle, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
