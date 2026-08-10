package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;

public class Ai90006_1 extends BaseMonsterAI {
    public static final Ability MA7 = Ability.getAbility(7); //缤纷连击7
    public static final IItem life = Items.getTemplate(6).newInstance(); //强效生命药剂

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
                life_used = true;
                useItemForSelf(bs, life);
                return false;
            }
            if (Utils.hit(rnd, 50, 100) &&
                bs.mp >= MA7.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA7, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
