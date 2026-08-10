package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;

public class Ai90011_2 extends BaseMonsterAI {

    public static final Ability FA8 = Ability.getAbility(40); //狂暴8
    public static final Ability FURY7 = Ability.getAbility(63); //狂暴意志7

//    (1)使用普通物理攻击;
//    (2)生命每下降10%使用一次8级＂狂暴一击＂
//    (3)生命低于10%后（包括10%）使用7级“狂暴意志”直到死亡
//    (4)魔法值不足时使用普通物理攻击

    public boolean[] faUsed = {false,false,false,false,false,false,false,false,false,true,true};

    private boolean furyUsed = false;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         10)&&bs.mp>=FURY7.getMpUse(bs.level,bs.mp)&&!furyUsed) {
                furyUsed = true;
                return super.useAbilityForSelf(bs, FURY7);
            }
            int i = getPrecentIndex(bs);
            if (!faUsed[i]&&bs.mp>=FA8.getMpUse(bs.level,bs.mp)) {
                faUsed[i] = true;
                return useAbilityToHighestUnit(bs, FA8, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }

    public int getPrecentIndex(BattleSprite bs){
        return bs.hp*10/bs.attributes[BattleSprite.ATTR_HPMAX];
    }
}
