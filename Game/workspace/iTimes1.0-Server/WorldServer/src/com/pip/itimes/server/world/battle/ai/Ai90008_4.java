package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.BossTips;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.Skill;

public class Ai90008_4 extends BaseMonsterAI{

//    ⑴普通物理攻击；
//    (2)隔3回合使用技能6级“群鹰出击”；（魔法值够的情况下）
//    (3)自己HP第一次低于50％时，使用7级“天堂圣盾”。
//    (4)生命值每下降15%则使用回血技能,补血5%
    public static final Ability EAGLE6 = Ability.getAbility(78); //6级群鹰
    public static final Ability GOH7 = Ability.getAbility(175); //天堂圣盾7

    public boolean useShield = false;
    public int lastHp = 0;
    public Skill hp_skill = null;

    public boolean action(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet,
                          BattleSprite[] themPet, Vector battleMovie,
                          BattleDataProcess battleDataProcess,int round) {
        if(round==1){
        	for(int i = 0; i < them.length; i++){
        		if(bs.monster.getName().equals("地精捣蛋鬼")){
        			Server.instance.chatService.sendPrivateRoarMessage(-10, "狮子吼", BossTips.getBossTip(bs.getGroupId()), 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0, them[i].id, bs.monster.getName());
        		} else if(bs.monster.getName().equals("血汗红宝书")){
        			Server.instance.chatService.sendPrivateRoarMessage(-10, "狮子吼", BossTips.getBossTip(bs.getGroupId()), 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0, them[i].id, bs.monster.getName());
        			
        		}
        	}
            defenceAllDebuff(bs);
            lastHp = bs.hp;
            hp_skill = (Skill)Skill.getSkill(193).clone();
            hp_skill.parm1 = bs.attributes[BattleSprite.ATTR_HPMAX]*5/100;
        }
        if (!bs.canAction()) {
            defaultCannotActionAction(bs);
            return false;
        } else {
            if (bs.hp <= getPercentValue(bs.attributes[BattleSprite.ATTR_HPMAX],
                                         50)&&!useShield) {
                useShield = true;
                return useAbilityForSelf(bs,GOH7);
            }
            if((lastHp-bs.hp)>=(bs.attributes[BattleSprite.ATTR_HPMAX]*15/100)){
                lastHp = bs.hp + bs.attributes[BattleSprite.ATTR_HPMAX]*5/100;
                return useSkillForSelf(bs,hp_skill);
            }
            if (round % 3 == 0 && bs.mp >= EAGLE6.getMpUse(bs.level, bs.mp)) {
                return useAbilityToHighestUnit(bs, EAGLE6, them, themPet);
            }else {
                return useDefaultAttackToHighestUnit(bs, them, themPet);
            }
        }
    }
}
