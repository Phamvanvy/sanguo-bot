package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.util.Utils;

public class Ai90009_2 extends BaseMonsterAI {
    public static final Ability EAGLE5 = Ability.getAbility(80); //群鹰8
    public static final Ability CM8 = Ability.getAbility(64);  //狂暴意志

//    (1)50％使用普通物理攻击;
//    (2)30％使用技能8级"群鹰出击"；（魔法值够的情况下）
//    (3)生命低于30%时(包括30%)使用技能8级"狂暴意志"
//    (4)魔法值不足时使用普通物理攻击

    private boolean enhanced = false;


    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         30) && !enhanced) {
                return useAbilityForSelf(bs,CM8);
            }
            if (Utils.hit(rnd, 30, 100) &&
                bs.mp >= EAGLE5.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, EAGLE5, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
