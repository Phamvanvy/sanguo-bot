package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.IItem;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Ai90001_2 extends BaseMonsterAI{

    public static final Ability stone = Ability.getAbility(10006);//3级石化
    public static final Ability eagle = Ability.getAbility(10004);//2次魔攻
    public static final IItem mana = Items.getTemplate(1).newInstance();//小魔法

    public boolean mana_used = false;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                   BattleSprite[] them, BattleSprite[] ourPet,
                   BattleSprite[] themPet,Vector battleMovie,
                   BattleDataProcess battleDataProcess,int round){
        if(!bs.canAction()){
            defaultCannotActionAction(bs);
            return false;
        }else{
            if (bs.mp <= getPercentValue(bs.attributes[BattleSprite.ATTR_MPMAX],
                                        10)&&!mana_used) {
                    mana_used = true;
                    useItemForSelf(bs, mana);
                    return false;
            }
            if(Utils.hit(rnd,10,100)&&bs.mp>=stone.getMpUse(bs.level, bs.mp)){
                return useAbilityToHighestUnit(bs,stone,them,themPet);
            }
            if(Utils.hit(20,100)&&bs.mp>=eagle.getMpUse(bs.level, bs.mp)){
                return useAbilityToHighestUnit(bs,eagle,them,themPet);
            }
            else{
                return useDefaultAttackToHighestUnit(bs,them,themPet);
            }
        }
    }
}
