package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;

public class Ai90006_2 extends BaseMonsterAI {
    public static final Ability chaos = Ability.getAbility(118); //6级混乱
    public static final Ability eagle = Ability.getAbility(79); //群鹰7
    public static final Ability CS8 = Ability.getAbility(144); //彩云遮日8
    public static final IItem life = Items.getTemplate(6).newInstance(); //强效生命药剂

    public boolean mana_used = false;
    public boolean life_used = false;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         20) && !life_used) {
                mana_used = true;
                life_used = true;
                useItemForSelf(bs, life);
                return false;
            }
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         40) && !mana_used) {
                mana_used = true;
                useAbilityForSelf(bs, CS8);
                return false;
            }
            if (Utils.hit(rnd, 30, 100) &&
                bs.mp >= chaos.getMpUse(bs.level, bs.mp)) { //30％使用混乱魔法
                return useAbilityToHighestUnit(bs, chaos, them, themPet);
            }
            if (bs.mp >= eagle.getMpUse(bs.level, bs.mp)) { //70％使用群鹰
                return useAbilityToHighestUnit(bs, eagle, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
