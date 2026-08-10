package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.Skill;
import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Ai90001_4 extends BaseMonsterAI {
    public static final Ability ability = Ability.getAbility(10003); //2´ÎÎï¹¥
    public static final Ability life5000 = Ability.getAbility(10002);//²¹Ñª5000

    private boolean crited = false;
    private boolean life_used = false;

    public Ai90001_4() {
    }

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                          BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if(round==1){
            defenceAllDebuff(bs);
        }
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (!crited && hasTeamHpLeastAt(bs, our, ourPet, 10)) {
                crited = true;
                bs.attributes[BattleSprite.ATTR_PCRI] = 100;
                if(Utils.hit(rnd,30,100)){
                    bs.attributes[BattleSprite.ATTR_PMAX] *= 6;
                    bs.attributes[BattleSprite.ATTR_PMIN] *= 6;
                }else{
                    bs.attributes[BattleSprite.ATTR_PMAX] *= 3;
                    bs.attributes[BattleSprite.ATTR_PMIN] *= 3;
                }
            }
            if (bs.hp < getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                        5) && !life_used) {
                life_used = true;
                return useAbilityForSelf(bs,life5000);
            }
            if (round % 3 == 0 && bs.mp >= ability.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, ability, them, themPet);
            }
            return super.useDefaultAttackToHighestUnit(bs, them, themPet);
        }
    }

}
