package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.util.Utils;

public class Ai90007_1 extends BaseMonsterAI {

//    (1)30％使用5级[缤纷连击]（魔法值够的情况下）
//(2)70％使用5[开放之击]魔法值够的情况下）
//(3)生命每下降10％，其攻击力增加10%,直至死亡
//(4)魔法值不足时物理攻击。


    public static final Ability MA5 = Ability.getAbility(5); //缤纷连击5
    public static final Ability OA5 = Ability.getAbility(13); //开放之击5

    public int step = 10;
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
            int c = ((bs.attributes[BattleSprite.ATTR_HPMAX] - bs.hp) * 100 / bs.attributes[BattleSprite.ATTR_HPMAX]) / 10 * 10;
            if(c!=0){
                bs.attributes[BattleSprite.ATTR_PMAX] = oldpmax*(100+c) / 100;
                bs.attributes[BattleSprite.ATTR_PMIN] = oldpmin*(100+c) / 100;
                bs.attributes[BattleSprite.ATTR_MMAX] = oldmmax*(100+c) / 100;
                bs.attributes[BattleSprite.ATTR_MMIN] = oldmmin*(100+c) / 100;
            }

            if (Utils.hit(rnd, 30, 100) &&
                bs.mp >= MA5.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA5, them, themPet);
            } else if(bs.mp>=OA5.getMpUse(bs.level,bs.mp)) {
                return useAbilityToHighestUnit(bs, OA5, them, themPet);

            } else{
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
