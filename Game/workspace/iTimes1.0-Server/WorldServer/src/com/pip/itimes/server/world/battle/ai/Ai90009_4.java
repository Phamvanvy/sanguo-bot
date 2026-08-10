package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.util.Utils;

public class Ai90009_4 extends BaseMonsterAI {
    public static final Ability MA8 = Ability.getAbility(8); //缤纷连击8
    public static final Ability DM8 = Ability.getAbility(88); //致命之毒8
    public static final Ability CM8 = Ability.getAbility(64);  //狂暴意志8
    public static final Ability relife = Ability.getAbility(207); //再生之灵5

//    (1)50%使用技能8级"缤纷连击"
//    (2)30％使用8级"致命之毒"（魔法值够的情况下）
//    (3)生命低于30%时(包括30%)使用8级"狂暴意志"(魔法值够的情况下）
//    (4)组内有成员死亡后即使用5级[再生之灵]
//    (5)生命低于20%时(包括20%)伤害增加100%,生命低于10%时(包括10%)伤害增加200%直至死亡
//    (6)魔法值不足时使用普通物理攻击

    private boolean used_mana = false;
    private boolean enhanced1 = false;
    private boolean enhanced2 = false;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         30) && !used_mana) {
                used_mana = true;
                return useAbilityForSelf(bs,CM8);
            }
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         20) && !enhanced1) {
                enhanced1 = true;
                bs.attributes[BattleSprite.ATTR_PMAX] *= 2;
                bs.attributes[BattleSprite.ATTR_PMIN] *= 2;
                bs.attributes[BattleSprite.ATTR_MMAX] *= 2;
                bs.attributes[BattleSprite.ATTR_MMIN] *= 2;
            }
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         10) && !enhanced2) {
                enhanced2 = true;
                bs.attributes[BattleSprite.ATTR_PMAX] *= 2;
                bs.attributes[BattleSprite.ATTR_PMIN] *= 2;
                bs.attributes[BattleSprite.ATTR_MMAX] *= 2;
                bs.attributes[BattleSprite.ATTR_MMIN] *= 2;
            }
            BattleSprite die = getDieTeam(bs, our);
            if (die != null) {
                return useAbilityForTeam(bs, die, relife);
            }
            if (Utils.hit(rnd, 50, 100) &&
                bs.mp >= MA8.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA8, them, themPet);
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
