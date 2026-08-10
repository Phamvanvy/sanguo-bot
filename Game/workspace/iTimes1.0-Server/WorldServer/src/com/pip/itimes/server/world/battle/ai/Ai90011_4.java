package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.Server;

public class Ai90011_4 extends BaseMonsterAI {
    public static final Ability GM7 = Ability.getAbility(71); //聚法7
    public static final Ability ICE7 = Ability.getAbility(111); //霜冻7
    public static final Ability MS7 = Ability.getAbility(191);  //魔法吸收7

//    (1)使用技能7级”聚法一击”（魔法值够的情况下）
//    (2)隔两回合使用一次7级“霜冻魔法”（魔法值够的情况下）
//    (3)(3)生命低于50%（包括50%）后,生命每降低10%,受到的魔法伤害减少10%
//    (4)生命低于20%时(包括20%)伤害增加100%,生命低于10%时(包括10%)伤害增加200%直至死亡
//    (5)魔法值不足时使用普通物理攻击


    private boolean enhanced1 = false;
    private boolean enhanced2 = false;

    public boolean[] decMa = {false,false,false,false,false,true,true,true,true,true,true};

    private static final int EFFECTID = 10002;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
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
                bs.attributes[BattleSprite.ATTR_PMAX] *= 3;
                bs.attributes[BattleSprite.ATTR_PMIN] *= 3;
                bs.attributes[BattleSprite.ATTR_MMAX] *= 3;
                bs.attributes[BattleSprite.ATTR_MMIN] *= 3;
            }
            int i = getPrecentIndex(bs);
            if(!decMa[i]){
                decMa[i] = true;
                bs.AddAttrBuf(1000,0,0,0,0,0,0,0,0,-10,0,0,EFFECTID);
            }
            if (round % 3 == 0 && bs.mp >= ICE7.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, ICE7, them, themPet);
            }
            if (bs.mp >= GM7.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, GM7, them, themPet);
            }else{
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }

    public int getPrecentIndex(BattleSprite bs){
        return bs.hp*10/bs.attributes[BattleSprite.ATTR_HPMAX];
    }
}
