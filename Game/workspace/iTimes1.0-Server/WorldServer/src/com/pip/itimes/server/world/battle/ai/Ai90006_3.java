package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.Skill;

public class Ai90006_3 extends BaseMonsterAI {

//    public static final Ability MA3 = Ability.getAbility(3); //çÍ·×Á¬»÷3
    public static final Ability MA7 = Ability.getAbility(7); //çÍ·×Á¬»÷7
    public static final Ability MA8 = Ability.getAbility(8); //çÍ·×Á¬»÷8
    public static final Ability GOH5 = Ability.getAbility(173); //ÌìÌÃÊ¥¶Ü5
    public static final Ability ROE5 = Ability.getAbility(202); //´óµØ¸´Éú5




    public boolean life_used = false;
    public boolean goh5_used = false;
    public boolean crited = false;

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
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         20)&&!crited) {
                crited = true;
                bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                bs.attributes[BattleSprite.ATTR_PMAX] *= 10;
                bs.attributes[BattleSprite.ATTR_PMIN] *= 10;
                bs.attributes[BattleSprite.ATTR_MCRI] = 100;
                bs.attributes[BattleSprite.ATTR_MMAX] *= 10;
                bs.attributes[BattleSprite.ATTR_MMIN] *= 10;
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
            if (round % 2 == 0 && bs.mp >= MA7.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA7, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
