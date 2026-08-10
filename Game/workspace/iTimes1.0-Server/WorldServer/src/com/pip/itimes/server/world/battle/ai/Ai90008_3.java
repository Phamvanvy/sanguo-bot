package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.Skill;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.util.Utils;

public class Ai90008_3 extends BaseMonsterAI{

//    (1)60％普通物理攻击;
//    (1)40％使用技能6级“群鹰出击”；（魔法值够的情况下）
//    (3)实际受到的伤害=伤害/同组所有成员剩余生命百分比之和
//    (4)生命值低于10%时使用6级”聚法一击”

    public static final Ability EAGLE6 = Ability.getAbility(78); //6级群鹰
    public static final Ability MOG6 = Ability.getAbility(70); //聚法一击6

    public boolean action(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {

        int c = getDamagePercent(our);
        bs.AddAttrBuf(1,0,0,0,0,0,0,0,c,c,0,0,100001);
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         10)&&bs.mp>=MOG6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, MOG6, them, themPet);
            }
            if (Utils.hit(rnd, 40, 100) &&
                bs.mp >= EAGLE6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, EAGLE6, them, themPet);
            } else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }

    public int getDamagePercent(BattleSprite[] our){
        int a = 0;
        for(int i=0;i<our.length;i++){
            if(our[i]!=null){
                a += our[i].hp*100/our[i].attributes[BattleSprite.ATTR_HPMAX];
            }
        }
        if(a == 0){
        	a = 1;
        }
        int c = 10000/a-100;
        return c;
    }
}
