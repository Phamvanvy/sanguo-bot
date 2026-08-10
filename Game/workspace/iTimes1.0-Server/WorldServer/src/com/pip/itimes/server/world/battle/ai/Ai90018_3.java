package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.Skill;
import com.pip.itimes.server.world.worldboss.WorldBossConfig;

/**
 * @file Ai90018_3.java
 * @author zxyu
 * @version 1.0.0
 * @date 2012-9-20
 **/
public class Ai90018_3 extends BaseMonsterAI {
	public static final Skill skill = (Skill) Skill.getSkill(112).clone(); 	//Ëª¶³Ä§·¨
	
	public int getSpecialHp(){
        return WorldBossConfig.getBossMaxHp();
    }
	
	public int getSpecialMp(){
		return WorldBossConfig.getBossMaxMp();
	}
	
	public boolean action(BattleSprite bs, int index, BattleSprite[] our,
			BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet,
			Vector battleMovie,
            BattleDataProcess battleDataProcess,int round) {
		defenceAllDebuff(bs);
        antiDefence(bs);
        bs.AddAttrBuf(1, 0, 0, 0, 0, 100, 0, 1000000, 0, 0, 1000000, 1000000, 100003);
        return useSkillToHighestUnit(bs, skill, them, themPet);
	}
}
