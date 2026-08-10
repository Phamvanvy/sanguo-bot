package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.util.Utils;

public class Ai90008_1 extends BaseMonsterAI {

//    (1)30％使用6级“缤纷连击（魔法值够的情况下）
//    (2)70％使用6“开放之击”魔法值够的情况下）
//    (3)当生命值低于10％时，其伤害增加50%,且生命值每降低1%其伤害增加10%直至死亡
//    (4)魔法值不足时物理攻击。
//。


    public static final Ability MA6 = Ability.getAbility(6); //缤纷连击6
    public static final Ability OA6 = Ability.getAbility(14); //开放之击6

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
            int c = 0;
            if(bs.hp<=bs.attributes[BattleSprite.ATTR_HPMAX]/10){
                c = (10-(bs.hp*100/ bs.attributes[BattleSprite.ATTR_HPMAX]))*10+50;
            }
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
