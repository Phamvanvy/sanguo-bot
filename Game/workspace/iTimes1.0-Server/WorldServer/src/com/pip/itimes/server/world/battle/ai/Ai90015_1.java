package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.BossTips;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.Skill;
import com.pip.itimes.server.world.battle.SkillConstants;

public class Ai90015_1 extends BaseMonsterAI {

    public boolean action(BattleSprite bs, int index, BattleSprite[] our,
                       BattleSprite[] them, BattleSprite[] ourPet,
                       BattleSprite[] themPet,Vector battleMovie,
                       BattleDataProcess battleDataProcess,int round) {
    	if(round == 1){
        	for(int i = 0; i < them.length; i++){
            	if(bs.monster.getName().equals("¾ÞÁúÒ¹É·")){
            		Server.instance.chatService.sendPrivateRoarMessage(-10, "Ê¨×Óºð", BossTips.getBossTip(bs.getGroupId()), 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short)0, them[i].id, bs.monster.getName());
            	}
        	}
    	}
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
