package com.pip.itimes.server.world.battle.ai;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.IItem;
import java.util.Random;
import java.util.Vector;

public class Ai90006_4 extends BaseMonsterAI{

     public static final Ability CS8 = Ability.getAbility(144); //彩云遮日8
    public static final Ability MA4 = Ability.getAbility(4); //缤纷连击4
    public static final Ability EAGLE7 = Ability.getAbility(79); //7级群鹰
    public static final Ability chaos6 = Ability.getAbility(118); //混乱6
    public static final Ability DE6 = Ability.getAbility(22); //崩溃6
    public static final Ability relife = Ability.getAbility(207); //再生之灵5
    public static final Ability GM7 = Ability.getAbility(71); //聚法一击7
    public static final Ability crash3 = Ability.getAbility(19); //3级崩溃一击
    public static final IItem life = Items.getTemplate(9).newInstance();
//    ⑴使用7级“聚法一击” （魔法值够的情况下）
//⑵HP少于75％时，随机使用 6级“混乱魔法”、7级“聚法一击”和7级“群鹰出击”。
//⑶HP第一次少于50％时，使用技能8级“彩云遮日”1次。
//⑷HP第一次少于40％时，优先自动使用“特效生命药剂”1次；
//⑸只要队友之中有死亡时，就使用5级“再生之灵”；
//(6)HP第一次少于20％时，100％暴击，伤害800％；

    public boolean crited = false;
    public boolean life_used = false;
    public boolean cs_used = false;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                      BattleSprite[] them, BattleSprite[] ourPet,
                      BattleSprite[] themPet, Vector battleMovie,
                      BattleDataProcess battleDataProcess,int round) {
        if (round == 1) {
            defenceAllDebuff(bs);
        }
        if (!crited && bs.hp <=
            getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                            20)) {
            crited = true;
            bs.attributes[BattleSprite.ATTR_PCRI] = 100;
            bs.attributes[BattleSprite.ATTR_PMAX] *= 8;
            bs.attributes[BattleSprite.ATTR_PMIN] *= 8;
            bs.attributes[BattleSprite.ATTR_MCRI] = 100;
            bs.attributes[BattleSprite.ATTR_MMAX] *= 8;
            bs.attributes[BattleSprite.ATTR_MMIN] *= 8;
        }
        if(crited){
            if(bs.mp>=crash3.getMpUse(bs.level,bs.mp)){
                return useAbilityToHighestUnit(bs, crash3, them,
                                               themPet);
            }else{
//                return useAbilityToHighestUnit(bs, crash3, them,
//                                               themPet);
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
        BattleSprite die = getDieTeam(bs, our);
        if (die!=null) {
                return useAbilityForTeam(bs, die, relife);
        }
        if(!life_used &&bs.hp <=
            getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                            40)){
            life_used = true;
            useItemForSelf(bs, life);
            return false;
        }
        if (!cs_used && bs.hp <=
            getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                            50)&&bs.mp >= CS8.getMpUse(bs.level, bs.mp)){
            cs_used = true;
            return useAbilityForSelf(bs, CS8);
        }
        if (bs.hp<=getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                            75)){  //HP少于75％时，随机使用 6级“混乱魔法”、7级“聚法一击”和7级“群鹰出击”。
            Ability ability = null;
            Random rnd = new Random();
            int i = rnd.nextInt(3);
            if (i == 0 && bs.mp >= chaos6.getMpUse(bs.level, bs.mp))
                ability = chaos6;
            else if (i == 1 &&
                     bs.mp >= EAGLE7.getMpUse(bs.level, bs.mp))
                ability = EAGLE7;
            else if (i == 2 &&
                     bs.mp >= GM7.getMpUse(bs.level, bs.mp))
                ability = GM7;
            if (ability != null)
                return useAbilityToHighestUnit(bs, ability, them,
                                               themPet);
            else
                return useDefaultAttackToHighestUnit(bs, them, themPet);
        }
        if (bs.mp>=GM7.getMpUse(bs.level, bs.mp)) {
            return useAbilityToHighestUnit(bs, GM7, them,
                                           themPet);
        }else{
//            return useAbilityToHighestUnit(bs, GM7, them,
//                                           themPet);
            return useDefaultAttackToHighestUnit(bs, them, themPet);
        }
    }
}
