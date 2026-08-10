package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.util.Utils;

public class Ai90007_2 extends BaseMonsterAI {

//    (1)60％普通物理攻击
//    (1)40％使用技能6级[霜冻魔法]；（魔法值够的情况下）
//    (3)每当生命值低于50%时使用4级[致晕攻击],当生命值低于20%时使用5级[聚法一击]
//    (4)组内有成员死亡后即使用5级[再生之灵]

    public static final Ability FREEZE6 = Ability.getAbility(110); //霜冻6
    public static final Ability FAINT4 = Ability.getAbility(28); //致晕攻击4
    public static final Ability MOG5 = Ability.getAbility(69); //聚法一击5
    public static final Ability RELIFE5 = Ability.getAbility(207); //再生之灵5

    private boolean use_faint = false;
    private boolean use_mog = false;

    public Ai90007_2() {
        super();
    }

    public boolean action(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            BattleSprite die = getDieTeam(bs, our);
            if (die != null) {
                return useAbilityForTeam(bs, die, RELIFE5);
            }
            if (!use_mog && bs.hp <=
                getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                20) && bs.mp >= MOG5.getMpUse(bs.level, bs.mp)) {
                use_mog = true;
                return useAbilityToHighestUnit(bs, MOG5, them,
                                               themPet);
            }
            if (!use_faint && bs.hp <=
                getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                50) && bs.mp >= FAINT4.getMpUse(bs.level, bs.mp)) {
                use_faint = true;
                return useAbilityToHighestUnit(bs, FAINT4, them,
                                               themPet);
            }
            if (Utils.hit(rnd, 40, 100) &&
                bs.mp >= FREEZE6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, FREEZE6, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
