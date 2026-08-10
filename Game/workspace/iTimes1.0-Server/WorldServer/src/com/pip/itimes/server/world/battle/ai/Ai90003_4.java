package com.pip.itimes.server.world.battle.ai;

import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.IItem;
import java.util.Random;
import java.util.Vector;

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
public class Ai90003_4 extends BaseMonsterAI {
    public static final Ability EM3 = Ability.getAbility(75); //群鹰3
    public static final Ability GM4 = Ability.getAbility(68); //聚法一击4
    public static final Ability chaos3 = Ability.getAbility(115); //混乱3
    public static final Ability relife = Ability.getAbility(205); //再生之灵3
    public static final IItem life = Items.getTemplate(6).newInstance(); //强效生命药剂

    public boolean life_used = false;
    public boolean first = true;
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
            if (isTeamAllDie(bs, our) && first) { //第一次队友全部死亡，复活一个
                BattleSprite die = getDieTeam(bs, our);
                first = false;
                if (die != null)
                    return useAbilityForTeam(bs, die, relife);
            }
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         20)) {
                crited = true;
                bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                bs.attributes[BattleSprite.ATTR_PMAX] *= 2;
                bs.attributes[BattleSprite.ATTR_PMIN] *= 2;
                bs.attributes[BattleSprite.ATTR_MCRI] = 100;
                bs.attributes[BattleSprite.ATTR_MMAX] *= 2;
                bs.attributes[BattleSprite.ATTR_MMIN] *= 2;
            }
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         40) && !life_used) { //生命小于40％使用一次生命
                life_used = true;
                useItemForSelf(bs, life);
                return false;
            }
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         75)) { //HP少于75％时，随机使用 3级“混乱魔法”、4级“聚法一击”和3级“群鹰出击”。
                Ability ability = EM3;
                Random rnd = new Random();
                int i = rnd.nextInt(3);
                if (i == 0 && bs.mp >= chaos3.getMpUse(bs.level, bs.mp))
                    ability = chaos3;
                else if (i == 2 && bs.mp >= GM4.getMpUse(bs.level, bs.mp))
                    ability = GM4;
                else if (i == 3 && bs.mp >= EM3.getMpUse(bs.level, bs.mp))
                    ability = EM3;
                return useAbilityToHighestUnit(bs, ability, them, themPet);
            }
            if (bs.mp >= GM4.getMpUse(bs.level, bs.mp)) { //聚法4
                return useAbilityToHighestUnit(bs, GM4, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
//    ⑴使用4级“聚法一击” （魔法值够的情况下）
//    ⑵HP少于75％时，随机使用 3级“混乱魔法”、4级“聚法一击”和3级“群鹰出击”。
//    ⑶HP第一次少于40％时，优先自动使用“特效生命药剂”1次；
//    ⑷队友两个都死亡时，使用3级“再生之灵”；
//    ⑸HP第一次少于20％时，暴击400％；
//    (6)魔法值不足时物理攻击。

}
