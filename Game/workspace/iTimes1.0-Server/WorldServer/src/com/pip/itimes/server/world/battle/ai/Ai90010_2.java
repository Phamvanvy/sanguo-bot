package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.*;

public class Ai90010_2 extends BaseMonsterAI {
    public static final Ability EALGE6 = Ability.getAbility(78); //群鹰6
    public static final Ability DM8 = Ability.getAbility(86); //致命之毒6


//    (1)40%使用普通物理攻击;
//    (2)30%使用6级 [群鹰出击]
//    (3)30%使用6级 [致命之毒]
//    (4)魔法值不足时使用普通物理攻击



    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (Utils.hit(rnd, 30, 100) &&
                bs.mp >= EALGE6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, EALGE6, them, themPet);
            }
            if (Utils.hit(rnd, 30, 100) &&
                bs.mp >= DM8.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, DM8, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
