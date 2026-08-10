package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.BattleSprite;

public class Ai90011_3 extends BaseMonsterAI {

    public static final Ability MA7 = Ability.getAbility(7); //缤纷连击7
    public static final Ability DE7 = Ability.getAbility(23); //崩溃7
    public static final Ability AS7 = Ability.getAbility(183); //攻击吸收7

//    ⑴50%使用普通物理攻击；
//    (2)30%使用技能7级“缤纷连击”（魔法值够的情况下）
//    (3)20%使用技能7级“崩溃一击”（魔法值够的情况下）
//    (4)生命低于30%时(包括30%)使用7级攻击吸收
//    (5)魔法值不足时使用普通物理攻击




    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         30)&&bs.mp>=AS7.getMpUse(bs.level,bs.mp)) {
                return useAbilityToHighestUnit(bs, AS7, them, themPet);
            }
            if (Utils.hit(rnd, 20, 100) &&
                bs.mp >= DE7.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, DE7, them, themPet);
            }
            if (Utils.hit(rnd, 30, 100) &&
                bs.mp >= MA7.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA7, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
