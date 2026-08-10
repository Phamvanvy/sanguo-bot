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
public class Ai90002_4 extends BaseMonsterAI {
    public static final Ability magic = Ability.getAbility(67); //聚法3
    public static final Ability eagle = Ability.getAbility(74);//群鹰2
    public static final IItem mana = Items.getTemplate(10).newInstance(); //极效魔法

    private boolean crited = false;
    private boolean magic_used = false;
    private boolean eagle_used = false;


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
            if (!crited&&bs.hp<=getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],20)) {
                crited = true;
                bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                bs.attributes[BattleSprite.ATTR_PMAX] *= 2;
                bs.attributes[BattleSprite.ATTR_PMIN] *= 2;
            }
            if (bs.hp < getPercentValue(bs.attributes[BattleSprite.ATTR_MPMAX],
                                        20) && !magic_used) {
                magic_used = true;
                useItemForSelf(bs,mana);
                return false;
            }
            if (bs.hp < getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                        75) && !eagle_used &&
                bs.mp >= eagle.getMpUse(bs.level, bs.mp)) {
                eagle_used = true;
                return useAbilityToHighestUnit(bs, eagle, them, themPet);
            }
            if (bs.mp>=magic.getMpUse(bs.level,bs.mp)) {
                return useAbilityToHighestUnit(bs, magic, them, themPet);
            }
            return super.useDefaultAttackToHighestUnit(bs, them, themPet);
        }
    }

}
