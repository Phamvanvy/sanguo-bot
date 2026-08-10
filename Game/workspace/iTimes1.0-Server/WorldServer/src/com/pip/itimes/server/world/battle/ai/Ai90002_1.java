package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.util.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Ai90002_1 extends BaseMonsterAI{

    public static final IItem life = Items.getTemplate(3).newInstance(); //´óÉúÃüÒ©¼Á
    public static final Ability ability = Ability.getAbility(5); //çÍ·×Á¬»÷2

    private boolean life_used = false;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                   BattleSprite[] them, BattleSprite[] ourPet,
                   BattleSprite[] themPet,Vector battleMovie,
                   BattleDataProcess battleDataProcess,int round){
        if(!bs.canAction()){
            defaultCannotActionAction(bs);
            return false;
        }else{
            if(bs.hp<=getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],10)){
                if(!life_used){
                    life_used = true;
                    useItemForSelf(bs,life);
                    return false;
                }
            }
            if(Utils.hit(rnd,30,100)&&bs.mp>=ability.getMpUse(bs.level, bs.mp)){
                return useAbilityToHighestUnit(bs,ability,them,themPet);
            }else{
                return useDefaultAttackToHighestUnit(bs,them,themPet);
            }
        }
    }

}
