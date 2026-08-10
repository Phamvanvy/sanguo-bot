package com.pip.itimes.server.world.battle.ai;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import java.util.Random;
import java.util.Vector;

import com.pip.itimes.server.world.battle.BaseMonsterAI;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author Jeffrey
 * @version 1.0
 */
public class Ai90003_5 extends BaseMonsterAI {

    public static final Ability MA3 = Ability.getAbility(3); //缤纷连击3
    public static final Ability MA4 = Ability.getAbility(4); //缤纷连击4
    public static final Ability MA5 = Ability.getAbility(5); //缤纷连击4
    public static final Ability GOH5 = Ability.getAbility(173); //天堂圣盾5
    public static final Ability ROE5 = Ability.getAbility(202); //大地复生5

//    ⑴普通物理攻击；
//    ⑵隔1回合使用狂战技能3级“缤纷连击”；（魔法值够的情况下）
//    ⑶隔4回合使用狂战技能4级“缤纷连击”；（魔法值够的情况下）
//    ⑷自己HP第一次低于50％时，使用5级“天堂圣盾”。
//    (5)队友的HP第一次低于30％时，使用5级“大地复生”
//    (6) 自己HP低于20％时，使用狂战技能5级“缤纷连击”，100%暴击，伤害提高800％；（魔法值够的情况下,否则为普通物理攻击暴击）。


    public boolean life_used = false;
    public boolean goh5_used = false;
    public boolean crited = false;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (round == 1) {
            defenceAllDebuff(bs);
        }

        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         20)) {
                crited = true;
                bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                bs.attributes[BattleSprite.ATTR_PMAX] *= 4;
                bs.attributes[BattleSprite.ATTR_PMIN] *= 4;
                bs.attributes[BattleSprite.ATTR_MCRI] = 100;
                bs.attributes[BattleSprite.ATTR_MMAX] *= 4;
                bs.attributes[BattleSprite.ATTR_MMIN] *= 4;
            }
            if (crited && bs.mp >= MA5.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA5, them, themPet);
            }
            if(!goh5_used&&bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         50)){
                goh5_used = true;
                return useAbilityForSelf(bs,GOH5);
            }
            if (round % 5 == 0 && bs.mp >= MA4.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA4, them, themPet);
            }
            if (round % 2 == 0 && bs.mp >= MA3.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MA3, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
