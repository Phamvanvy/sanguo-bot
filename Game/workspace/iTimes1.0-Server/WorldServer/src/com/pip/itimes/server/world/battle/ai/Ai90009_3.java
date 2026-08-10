package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.util.Utils;

public class Ai90009_3 extends BaseMonsterAI {
    public static final Ability EAGLE5 = Ability.getAbility(80); //群鹰8
    public static final Ability DM8 = Ability.getAbility(88); //致命之毒8


//    ⑴50%使用普通物理攻击；
//    (2)30%使用技能8级“群鹰出击”；（魔法值够的情况下）
//    (3)20%使用技能8级”致命之毒”
//    (4)生命低于20%时(包括20%)伤害增加30%直至死亡
//    (5)魔法值不足时使用普通物理攻击

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
                                         20) && !enhanced) {
                enhanced = true;
                bs.attributes[BattleSprite.ATTR_PMAX] = bs.attributes[BattleSprite.ATTR_PMAX]*130/100;
                bs.attributes[BattleSprite.ATTR_PMIN] = bs.attributes[BattleSprite.ATTR_PMIN]*130/100;
                bs.attributes[BattleSprite.ATTR_MMAX] = bs.attributes[BattleSprite.ATTR_MMAX]*130/100;
                bs.attributes[BattleSprite.ATTR_MMIN] = bs.attributes[BattleSprite.ATTR_MMIN]*130/100;
            }
            if (Utils.hit(rnd, 20, 100) &&
                bs.mp >= DM8.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, DM8, them, themPet);
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
