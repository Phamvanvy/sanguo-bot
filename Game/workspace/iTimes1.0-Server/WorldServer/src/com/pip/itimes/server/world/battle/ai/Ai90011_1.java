package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;

public class Ai90011_1 extends BaseMonsterAI {
    public static final Ability DM8 = Ability.getAbility(87); //致命之毒8
    public static final Ability eagle = Ability.getAbility(79); //群鹰7

    public boolean mana_used = false;

//    (1)50%使用普通物理攻击
//    (2)30％使用7级“群鹰出击（魔法值够的情况下）
//    (3)20％使用7级“致命之毒”(魔法值够的情况下）
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
                bs.mp >= eagle.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, eagle, them, themPet);
            }
            if (Utils.hit(rnd, 20, 100) &&
                bs.mp >= DM8.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, DM8, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
