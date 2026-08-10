package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.Ability;

public class Ai90007_3 extends BaseMonsterAI {

//    ⑴普通物理攻击；
//    (2)隔2回合使用技能6级[霜冻魔法]；（魔法值够的情况下）
//    (3)隔3回合使用6级[攻击吸收]
//    (4)组内有成员死亡后即使用6级[再生之灵]
//    (5)生命低于20%后剩余回合对敌人造成的伤害加倍

    public boolean crited = false;

    public static final Ability FREEZE6 = Ability.getAbility(110); //霜冻6
    public static final Ability RELIFE6 = Ability.getAbility(207); //再生之灵5
    public static final Ability DECATTACK6 = Ability.getAbility(182);//攻击吸收6



    public boolean action(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if(round==1){
            defenceAllDebuff(bs);
        }
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         20)&&!crited) {
                crited = true;
                bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                bs.attributes[BattleSprite.ATTR_PMAX] *= 2;
                bs.attributes[BattleSprite.ATTR_PMIN] *= 2;
                bs.attributes[BattleSprite.ATTR_MCRI] = 100;
                bs.attributes[BattleSprite.ATTR_MMAX] *= 2;
                bs.attributes[BattleSprite.ATTR_MMIN] *= 2;
            }
            BattleSprite die = getDieTeam(bs, our);
            if (die != null) {
                return useAbilityForTeam(bs, die, RELIFE6);
            }

            if (round % 3 == 0 && bs.mp >= DECATTACK6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, DECATTACK6, them, themPet);
            }
            if (round % 2 == 0 && bs.mp >= FREEZE6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, FREEZE6, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
