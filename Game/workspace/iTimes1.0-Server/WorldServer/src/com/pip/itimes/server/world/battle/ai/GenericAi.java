package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class GenericAi extends BaseMonsterAI{
    public GenericAi() {
    }

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                       BattleSprite[] them, BattleSprite[] ourPet,
                       BattleSprite[] themPet,Vector battleMovie,
                       BattleDataProcess battleDataProcess,int round) {
        if(!bs.canAction()){
            defaultCannotActionAction(bs);
        }else{
            bs.setSkill(Skill.ATTACK_SKILL);
            BattleSprite target = selectHighestEnmity(bs, them, themPet);
            if(target==null)
                return true;
            bs.setTarget(target, target.groupIndex);
            return false;
        }
        return false;
    }
}
