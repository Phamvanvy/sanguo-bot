package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.BattleSprite;

public class Ai90008_2 extends BaseMonsterAI {

//    (1)30％使用6级“缤纷连击（魔法值够的情况下）
//    (2)70％使用6“开放之击”魔法值够的情况下）
//    (3)魔法值不足时物理攻击。
//    (4)实际伤害受同组boss(地精怨灵)生命值影响,实际伤害=原始伤害/boss生命余量百分比



    public static final Ability MA6 = Ability.getAbility(6); //缤纷连击6
    public static final Ability OA6 = Ability.getAbility(14); //开放之击6

    int oldpmax = 0;
    int oldpmin = 0;
    int oldmmax = 0;
    int oldmmin = 0;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if(round==1){
                oldpmax = bs.attributes[BattleSprite.ATTR_PMAX];
                oldpmin = bs.attributes[BattleSprite.ATTR_PMIN];
                oldmmax = bs.attributes[BattleSprite.ATTR_MMAX];
                oldmmin = bs.attributes[BattleSprite.ATTR_MMIN];
            }
            int c = ((bs.attributes[BattleSprite.ATTR_HPMAX]-bs.hp)/(bs.attributes[BattleSprite.ATTR_HPMAX]/5))*100;
            if(c!=0){
                bs.attributes[BattleSprite.ATTR_PMAX] = oldpmax*(100+c) / 100;
                bs.attributes[BattleSprite.ATTR_PMIN] = oldpmin*(100+c) / 100;
                bs.attributes[BattleSprite.ATTR_MMAX] = oldmmax*(100+c) / 100;
                bs.attributes[BattleSprite.ATTR_MMIN] = oldmmin*(100+c) / 100;
            }
            if (Utils.hit(rnd, 30, 100) &&
                bs.mp >= MA6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA6, them, themPet);
            } else if(bs.mp>=OA6.getMpUse(bs.level,bs.mp)) {
                return useAbilityToHighestUnit(bs, OA6, them, themPet);

            } else{
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }

}
