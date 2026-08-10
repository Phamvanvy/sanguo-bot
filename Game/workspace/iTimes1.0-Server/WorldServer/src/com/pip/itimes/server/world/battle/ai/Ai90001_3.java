package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.Ability;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Ai90001_3 extends BaseMonsterAI {

    public static final Ability eagle = Ability.getAbility(10004);//2次魔攻
    public static final Ability stone = Ability.getAbility(10005);//5级石化
    public static final Ability life500 = Ability.getAbility(10001);//补血500

    private boolean eagle_used = false;
    private boolean stone_used = false;
    private boolean crited = false;
    private boolean life_used = false;

    public Ai90001_3() {
    }

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
            if (bs.hp < getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                        5) && !crited) {
                crited = true;
                bs.attributes[BattleSprite.ATTR_PCRI] += bs.attributes[
                        BattleSprite.
                        ATTR_PCRI] / 2;
            }
            if (bs.hp < getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                        5) && !life_used) {
                life_used = true;
                return useAbilityForSelf(bs,life500);
            }
            if (bs.hp < getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                        50) && !stone_used&&bs.mp>=stone.getMpUse(bs.level, bs.mp)) {
                stone_used = true;
                return useAbilityToHighestUnit(bs,stone,them,themPet);
            }
            if(bs.hp < getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                        80) && !eagle_used&&bs.mp>=eagle.getMpUse(bs.level, bs.mp)){
                eagle_used = false;
                return useAbilityToHighestUnit(bs,eagle,them,themPet);
            }
            return useDefaultAttackToHighestUnit(bs,them,themPet);
        }
    }
}
