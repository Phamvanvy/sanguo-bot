package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;

public class Ai90010_1 extends BaseMonsterAI {
    public static final Ability MA6 = Ability.getAbility(6); //缤纷连击6
    public static final Ability OA6 = Ability.getAbility(14); //开放之击6
    public static final Ability DA6 = Ability.getAbility(54); //孤注一掷6

//    (1)50%使用普通物理攻击
//    (2)30％使用6级 [缤纷连击]（魔法值够的情况下）
//    (3)20％使用6级 [开放之击](魔法值够的情况下）
//    (4)生命低于20%(包括20%)时使用6级 [孤注一掷]直至死亡
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
                                         30)&&bs.mp>=DA6.getMpUse(bs.level,bs.mp)) {
                return useAbilityToHighestUnit(bs, DA6, them, themPet);
            }
            if (Utils.hit(rnd, 20, 100) &&
                bs.mp >= OA6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, OA6, them, themPet);
            }
            if (Utils.hit(rnd, 30, 100) &&
                bs.mp >= MA6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA6, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
