package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.IItem;

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
public class Ai90004_2 extends BaseMonsterAI {


    public static final Ability GOH6 = Ability.getAbility(174); //天堂圣盾6
    public static final Ability MA4 = Ability.getAbility(4); //缤纷连击4
    public static final Ability GOB5 = Ability.getAbility(149);//手足护卫5
    public static final IItem life = Items.getTemplate(6).newInstance(); //强效生命药剂

    public boolean GOB5_used = false;
    public boolean life_used = false;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                   BattleSprite[] them, BattleSprite[] ourPet,
                   BattleSprite[] themPet,Vector battleMovie,
                   BattleDataProcess battleDataProcess,int round){
        if (round == 1) { //第一回合使用天堂圣盾
            return useAbilityForSelf(bs,GOH6);
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
            if(Utils.hit(rnd,40,100)&&bs.mp>=MA4.getMpUse(bs.level, bs.mp)){  //40％缤纷连击
                return useAbilityToHighestUnit(bs,MA4,them,themPet);
            }
            if(!GOB5_used&&hasTeamHpLeastAt(bs,our,ourPet,30)){  //队友第一次生命小于30％时使用手足护卫5
                BattleSprite lowest = selectLowestHpTeam(bs,our,ourPet);
                if(lowest!=null){
                    GOB5_used = true;
                    return useAbilityForTeam(bs,lowest,GOB5);
                }else
                    return useDefaultAttackToHighestUnit(bs,them,themPet);
            }
            else{
                return useDefaultAttackToHighestUnit(bs,them,themPet);
            }
        }
    }
}
