package com.pip.itimes.server.world.battle.ai;

import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import java.util.Random;
import java.util.Vector;

import com.pip.itimes.server.world.battle.Skill;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Ai90004_4 extends BaseMonsterAI {

    public static final Ability MA4 = Ability.getAbility(4); //缤纷连击4
    public static final Ability EAGLE4 = Ability.getAbility(76); //4级群鹰
    public static final Ability FROST5 = Ability.getAbility(109); //5级霜冻
    public static final Ability chaos3 = Ability.getAbility(115); //混乱3
    public static final Ability DE6 = Ability.getAbility(22); //崩溃6
    public static final Ability ROE5 = Ability.getAbility(202); //大地复生5
    public static final Ability SOM6 = Ability.getAbility(126); //专注6
    public static final Ability relife = Ability.getAbility(207); //再生之灵5
    public static final Ability GM5 = Ability.getAbility(69); //聚法一击5

//        如果玩家没有佩戴了装备“梅葛之戒”
//        ⑴使用6级“崩溃一击”，100％出现暴击，伤害提高到800％。
//        ―――――――――――――――――――
//        如果玩家戴了装备“梅葛之戒”
//        ⑴使用5级“聚法一击” （魔法值够的情况下）
//        ⑵HP少于75％时，随机使用 3级“混乱魔法”、5级“霜冻魔法”和4级“群鹰出击”。
//        ⑶HP第一次少于50％时，使用“专注施法”6级；
//        ⑷队友两个都死亡时，使用5级“再生之灵”；
//        ⑸HP第一次少于20％时，使用5级“霜冻魔法”，伤害400％；
//        (6)魔法值不足时物理攻击。





    public boolean first = true;
    public boolean som_used = false;
    public boolean crited = false;
    public boolean equipmented = false;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (round == 1) {
            defenceAllDebuff(bs);
            if (hasEquipment(them, 1000331)) { //是否佩戴了装备“梅葛之戒”
                equipmented = true;
            } else {
                equipmented = false;
                bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                bs.attributes[BattleSprite.ATTR_PMAX] *= 4;
                bs.attributes[BattleSprite.ATTR_PMIN] *= 4;
                bs.attributes[BattleSprite.ATTR_MCRI] = 100;
                bs.attributes[BattleSprite.ATTR_MMAX] *= 4;
                bs.attributes[BattleSprite.ATTR_MMIN] *= 4;

            }
        }
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (equipmented) {
                if (!crited && bs.hp <=
                    getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                    20)) { //HP第一次少于20％时，使用5级“霜冻魔法”，伤害400％；
                    crited = true;
                    bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                    bs.attributes[BattleSprite.ATTR_PMAX] *= 2;
                    bs.attributes[BattleSprite.ATTR_PMIN] *= 2;
                    bs.attributes[BattleSprite.ATTR_MCRI] = 100;
                    bs.attributes[BattleSprite.ATTR_MMAX] *= 2;
                    bs.attributes[BattleSprite.ATTR_MMIN] *= 2;
                    if (bs.mp >= FROST5.getMpUse(bs.level, bs.mp)) {
                        return useAbilityToHighestUnit(bs, FROST5, them,
                                themPet);
                    }
                }
                if(hasTeamHpLeastAt(bs,our,ourPet,30)&&first){
                    first = false;
                    int hp = getMaxLostHp(our);
                    Skill roe = (Skill)Skill.getSkill(202).clone();
                    roe.parm1 = hp;
                    return useSkillForSelf(bs,roe);
                }
                if (isTeamAllDie(bs, our) && first) { //第一次队友全部死亡，复活一个
                    BattleSprite die = getDieTeam(bs, our);
                    first = false;
                    if (die != null)
                        return useAbilityForTeam(bs, die, relife);
                } else if (!som_used &&
                           bs.hp <=
                           getPercentValue(bs.attributes[BattleSprite.
                                           ATTR_HPMAX],
                                           50)) { //HP第一次少于50％时，使用“专注施法”6级；
                    if (!som_used && bs.mp >= SOM6.getMpUse(bs.level, bs.mp)) {
                        som_used = true;
                        return useAbilityForSelf(bs, SOM6);
                    }
                }

                if (bs.hp <=
                    getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                    75)) { //HP少于75％时，随机使用 3级“混乱魔法”、5级“霜冻魔法”和4级“群鹰出击”。
                    Ability ability = null;
                    Random rnd = new Random();
                    int i = rnd.nextInt(3);
                    if (i == 0 && bs.mp >= chaos3.getMpUse(bs.level, bs.mp))
                        ability = chaos3;
                    else if (i == 2 &&
                             bs.mp >= EAGLE4.getMpUse(bs.level, bs.mp))
                        ability = EAGLE4;
                    else if (i == 3 &&
                             bs.mp >= FROST5.getMpUse(bs.level, bs.mp))
                        ability = FROST5;
                    if (ability != null)
                        return useAbilityToHighestUnit(bs, ability, them,
                                themPet);
                    else
                        return useDefaultAttackToHighestUnit(bs, them, themPet);

                }
                if (bs.mp >= GM5.getMpUse(bs.level, bs.mp)) {
                    return useAbilityToHighestUnit(bs, GM5, them, themPet);
                } else {
                    return useDefaultAttackToHighestUnit(bs, them, themPet);
                }
            } else {
                if (bs.mp >= DE6.getMpUse(bs.level, bs.mp)) {
                    return useAbilityToHighestUnit(bs, DE6, them, themPet);
                } else {
                    return useDefaultAttackToHighestUnit(bs, them, themPet);
                }
            }
        }
    }
}
