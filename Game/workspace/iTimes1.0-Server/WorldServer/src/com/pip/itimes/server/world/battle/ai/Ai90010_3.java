package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.*;

public class Ai90010_3 extends BaseMonsterAI {
    public static final Ability EAGLE6 = Ability.getAbility(78); //群鹰6
    public static final Ability ICE6 = Ability.getAbility(110); //霜冻6
    public static final Ability STONE6 = Ability.getAbility(102); //石化6

//    ⑴30%使用普通物理攻击；
//    (2)40%使用6级[群鹰出击]（魔法值够的情况下）
//    (3)30%使用6级 [霜冻魔法]（魔法值够的情况下）
//    (4)生命低于30%时(包括30%)使用6级 [石化魔法]直至死亡
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
                                         30)&&bs.mp>=STONE6.getMpUse(bs.level,bs.mp)) {
                return useAbilityToHighestUnit(bs, STONE6, them, themPet);
            }
            if (Utils.hit(rnd, 40, 100) &&
                bs.mp >= EAGLE6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, EAGLE6, them, themPet);
            }
            if (Utils.hit(rnd, 30, 100) &&
                bs.mp >= ICE6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, ICE6, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
