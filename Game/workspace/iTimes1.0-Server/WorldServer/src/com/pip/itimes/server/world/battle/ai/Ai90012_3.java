package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.Skill;

public class Ai90012_3 extends BaseMonsterAI {
	//mengjie add moyan100 dieman
    public static final Ability MA7 = Ability.getAbility(7); //缤纷连击7
    public static final Ability MA8 = Ability.getAbility(8); //缤纷连击8
    public static final Ability GOH5 = Ability.getAbility(173); //天堂圣盾5
    public static final Ability ROE5 = Ability.getAbility(202); //大地复生5
    public static final Ability crash8 = Ability.getAbility(24); //8级崩溃一击



    public boolean life_used = false;
    public boolean goh5_used = false;
    public boolean crited = false;
    public int pcri = 0;
    public int mcri = 0;
    public int pmax = 0;
    public int pmin = 0;
    public int mmax = 0;
    public int mmin = 0;
    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
    	if(round==1){
            defenceAllDebuff(bs);
        }
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
        	//判断是否有互为师徒关系
            boolean ismaster = false;
            if (them != null){
                if (them.length == 3){
                	if ((them[2] != null) && (them[1] != null) && (them[0] != null)) {
                    	if ((Server.instance.masterService.hasRelation(them[0].player,them[1].player)) ||
                    			(Server.instance.masterService.hasRelation(them[0].player,them[2].player)) || 
                    			(Server.instance.masterService.hasRelation(them[1].player,them[2].player))){
                    		ismaster = true;
                    	}else{
                    		ismaster = false;
                    	}
                    }else if ((them[2] == null) && (them[1] != null) && (them[0] != null)){
                    	ismaster = Server.instance.masterService.hasRelation(them[0].player,them[1].player);
                    }else if ((them[2] == null) && (them[1] == null) && (them[0] != null)){
                    	ismaster = false;
                    }else{
                    	ismaster = false;
                    }
                }else if (them.length == 2){
                    if ((them[1] != null) && (them[0] != null)){
                        ismaster = Server.instance.masterService.hasRelation(them[0].player,them[1].player);
                    }else if ((them[1] == null) && (them[0] != null)){
                        ismaster = false;
                    }else{
                        ismaster = false;
                    }
                }else if (them.length <= 1){
                    if (them[0] != null){
                        ismaster = false;
                    }else{
                        ismaster = false;
                    }
                }
            }
        	if(crited){
        		bs.attributes[BattleSprite.ATTR_PCRI] = pcri;
        		bs.attributes[BattleSprite.ATTR_MCRI] = mcri;
                bs.attributes[BattleSprite.ATTR_PMAX] = pmax;
                bs.attributes[BattleSprite.ATTR_PMIN] = pmin;
                bs.attributes[BattleSprite.ATTR_MMAX] = mmax;
                bs.attributes[BattleSprite.ATTR_MMIN] = mmin;
        	}
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         20)&&!crited) {
                crited = true;
                pcri = bs.attributes[BattleSprite.ATTR_PCRI];
                mcri = bs.attributes[BattleSprite.ATTR_MCRI];
                pmax = bs.attributes[BattleSprite.ATTR_PMAX];
                pmin = bs.attributes[BattleSprite.ATTR_PMIN];
                mmax = bs.attributes[BattleSprite.ATTR_MMAX];
                mmin = bs.attributes[BattleSprite.ATTR_MMIN];
                
                bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                //bs.attributes[BattleSprite.ATTR_PMAX] *= 10;
                //bs.attributes[BattleSprite.ATTR_PMIN] *= 10;
                bs.attributes[BattleSprite.ATTR_MCRI] = 100;
                //bs.attributes[BattleSprite.ATTR_MMAX] *= 10;
                //bs.attributes[BattleSprite.ATTR_MMIN] *= 10;
                if (!ismaster){
                	bs.attributes[BattleSprite.ATTR_PMAX] *= 10;
                	bs.attributes[BattleSprite.ATTR_PMIN] *= 10;
                	bs.attributes[BattleSprite.ATTR_MMAX] *= 10;
                	bs.attributes[BattleSprite.ATTR_MMIN] *= 10;
                }
            }
            if (crited && bs.mp >=MA7.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA7, them, themPet);
            }
            if (hasTeamHpLeastAt(bs, our, ourPet, 30) && !life_used) {
                life_used = true;
                return useAbilityForSelf(bs, ROE5);
            }

            if(!goh5_used&&bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         50)){
                goh5_used = true;
                return useAbilityForSelf(bs,GOH5);
            }
            if (round % 5 == 0 && bs.mp >= MA8.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA8, them, themPet);
            }
            if (round % 2 == 0){
            	if (Utils.hit(rnd, 50, 100) &&
                    bs.mp >= crash8.getMpUse(bs.level, bs.mp)) { //50％使用崩溃一击8
                    return useAbilityToHighestUnit(bs, crash8, them, themPet);
                }else if(bs.mp >= MA7.getMpUse(bs.level, bs.mp)) { //50％使用缤纷连击7
                	return useAbilityToHighestUnit(bs, MA7, them, themPet);
                }else{
                	return useDefaultAttackToHighestUnit(bs, them, themPet);
                }
            }else{
            	return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
