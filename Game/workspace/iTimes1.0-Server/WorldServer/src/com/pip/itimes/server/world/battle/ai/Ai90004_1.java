package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Ai90004_1 extends BaseMonsterAI {
    public static final Ability chaos = Ability.getAbility(115);//3级混乱
    public static final Ability eagle = Ability.getAbility(76);//群鹰3

    public boolean mana_used = false;

//
//    ⑴30％使用3级“混乱魔法”；（魔法值够的情况下）
//⑵70％使用4级“群鹰出击”（魔法值够的情况下）
//⑶生命值第1次低于20％时，补充生命值到40％；
//⑷魔法值不足时物理攻击。

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                   BattleSprite[] them, BattleSprite[] ourPet,
                   BattleSprite[] themPet,Vector battleMovie,
                   BattleDataProcess battleDataProcess,int round){
        if(!bs.canAction()){
            defaultCannotActionAction(bs);
            return false;
        }else{
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                        20)&&!mana_used) {
                    mana_used = true;
                    Skill hp_skill = (Skill)Skill.getSkill(193).clone();
                    hp_skill.parm1 = bs.attributes[BattleSprite.ATTR_HPMAX]*40/100-bs.hp;
                    useSkillForSelf(bs,hp_skill);
                    return false;
            }
            if(Utils.hit(rnd,30,100)&&bs.mp>=chaos.getMpUse(bs.level, bs.mp)){  //30％使用混乱魔法
                return useAbilityToHighestUnit(bs,chaos,them,themPet);
            }
            if(bs.mp>=eagle.getMpUse(bs.level, bs.mp)){  //70％使用群鹰
                return useAbilityToHighestUnit(bs,eagle,them,themPet);
            }
            else{
                return useDefaultAttackToHighestUnit(bs,them,themPet);
            }
        }
    }
}
