package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;

public class Ai90005_3 extends BaseMonsterAI {


    public static final Ability GOH6 = Ability.getAbility(174); //天堂圣盾6
    public static final Ability MA4 = Ability.getAbility(4); //缤纷连击4
    public static final Ability GOB5 = Ability.getAbility(149);//手足护卫5
    public static final Ability CRASH6 = Ability.getAbility(22);//崩溃6
    public static final IItem life = Items.getTemplate(6).newInstance(); //强效生命药剂
    public static final int ITEMID = 100431;//索玛特的香包

    public boolean GOB5_used = false;
    public boolean life_used = false;
    public boolean equipmented = true;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                   BattleSprite[] them, BattleSprite[] ourPet,
                   BattleSprite[] themPet,Vector battleMovie,
                   BattleDataProcess battleDataProcess,int round){

        if(round==1){
            if(!hasItem(them,ITEMID)){
                equipmented = false;
                bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                bs.attributes[BattleSprite.ATTR_PMAX] *= 7.5;
                bs.attributes[BattleSprite.ATTR_PMIN] *= 7.5;
                bs.attributes[BattleSprite.ATTR_MCRI] = 100;
                bs.attributes[BattleSprite.ATTR_MMAX] *= 7.5;
                bs.attributes[BattleSprite.ATTR_MMIN] *= 7.5;
            } else {
                return useAbilityForSelf(bs, GOH6);
            }
        }
        if(!bs.canAction()){
            defaultCannotActionAction(bs);
            return false;
        }else{
            if (!equipmented &&
                bs.mp >= CRASH6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, CRASH6, them, themPet);
            }

            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                        20)&&!life_used) {
                    life_used = true;
                    useItemForSelf(bs, life);
                    return false;
            }
            if(Utils.hit(rnd,40,100)&&bs.mp>=MA4.getMpUse(bs.level, bs.mp)){  //40％缤纷连击
                return useAbilityToHighestUnit(bs,MA4,them,themPet);
            }
            if(!GOB5_used&&hasTeamHpLeastAt(bs,our,ourPet,30)){  //队友第一次生命小于30％时使用手足护卫5
                BattleSprite lowest = selectLowestHpTeam(bs,our,ourPet);
                if(lowest!=null){
                    GOB5_used = true;
                    return useAbilityForTeam(bs,lowest,GOB5);
                }else
                    return useDefaultAttackToHighestUnit(bs,them,themPet);
            }
            else{
                return useDefaultAttackToHighestUnit(bs,them,themPet);
            }
        }
    }
}
