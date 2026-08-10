package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.world.battle.BaseMonsterAI;

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
public class Ai90003_3 extends BaseMonsterAI{
    public static final Ability GOH4 = Ability.getAbility(172); //天堂圣盾4
    public static final Ability MA5 = Ability.getAbility(5); //缤纷连击5
    public static final Ability LL4 = Ability.getAbility(196);//滴滴甘露4
    public static final IItem life = Items.getTemplate(6).newInstance(); //强效生命药剂

    public boolean LL4_used = false;
    public boolean life_used = false;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                   BattleSprite[] them, BattleSprite[] ourPet,
                   BattleSprite[] themPet,Vector battleMovie,
                   BattleDataProcess battleDataProcess,int round){
        if (round == 1) { //第一回合使用天堂圣盾
            return useAbilityForSelf(bs,GOH4);
        }

        if(!bs.canAction()){
            defaultCannotActionAction(bs);
            return false;
        }else{

            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                        20)&&!life_used) {
                    life_used = true;
                    useItemForSelf(bs, life);
                    return false;
            }
            if(Utils.hit(rnd,30,100)&&bs.mp>=MA5.getMpUse(bs.level, bs.mp)){  //30％缤纷连击
                return useAbilityToHighestUnit(bs,MA5,them,themPet);
            }
            if(!LL4_used&&hasTeamHpLeastAt(bs,our,ourPet,30)){  //队友第一次生命小于30％时使用滴滴甘露
                BattleSprite lowest = selectLowestHpTeam(bs,our,ourPet);
                if(lowest!=null){
                    LL4_used = true;
                    return useAbilityForTeam(bs,lowest,LL4);
                }else
                    return useDefaultAttackToHighestUnit(bs,them,themPet);
            }
            else{
                return useDefaultAttackToHighestUnit(bs,them,themPet);
            }
        }
    }


}
