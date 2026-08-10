package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Ai90002_5 extends BaseMonsterAI {

    public static final Ability ability = Ability.getAbility(3); //çÍ·×Á¬»÷6
    public static final IItem mana = Items.getTemplate(13).newInstance(); //¾ÞÐ§ÉúÃü

    private boolean crited = false;
    private boolean life_used = false;

    private int oldCrit = 0;
    private int oldPMax = 0;
    private int oldPMin = 0;

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
            if (!crited && allTeamHpLeastAt(bs, our, ourPet, 0)) {
                crited = true;
                bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                bs.attributes[BattleSprite.ATTR_PMAX] *= 8;
                bs.attributes[BattleSprite.ATTR_PMIN] *= 8;
            }
            if (bs.hp < getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                        10) && !life_used) {
                life_used = true;
                useItemForSelf(bs,mana);
                return false;
            }
            if(crited){
                if(bs.mp>=ability.getMpUse(bs.level,bs.mp)){
                    return useAbilityToHighestUnit(bs, ability, them, themPet);
                }
            }else{
                if (round % 6 == 0){
                    oldCrit = bs.attributes[BattleSprite.ATTR_PCRI];
                    oldPMax = bs.attributes[BattleSprite.ATTR_PMAX];
                    oldPMin = bs.attributes[BattleSprite.ATTR_PMIN];
                    bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                    bs.attributes[BattleSprite.ATTR_PMAX] *= 3;
                    bs.attributes[BattleSprite.ATTR_PMIN] *= 3;
                }
                if (round % 7 == 0){
                    bs.attributes[BattleSprite.ATTR_PCRI] = oldCrit;
                    bs.attributes[BattleSprite.ATTR_PMAX] = oldPMax;
                    bs.attributes[BattleSprite.ATTR_PMIN] = oldPMin;
                }
                if (round % 2 == 0 && bs.mp >= ability.getMpUse(bs.level, bs.mp)) {
                    return useAbilityToHighestUnit(bs, ability, them, themPet);
                }
            }
            return super.useDefaultAttackToHighestUnit(bs, them, themPet);
        }
    }

}
