package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;

/**
 * @file Ai90018_1.java
 * @author zxyu
 * @version 1.0.0
 * @date 2012-8-14
 **/
public class Ai90018_1 extends BaseMonsterAI {

	private static final Ability ability1 = Ability.getAbility(79); 	//群鹰7
	
	public boolean action(BattleSprite bs, int index, BattleSprite[] our,
			BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet,
			Vector battleMovie,
            BattleDataProcess battleDataProcess,int round) {
		int flag = 0;
		if(round == 1){		//免疫所有状态
			defenceAllDebuff(bs);
		}
		bs.AddAttrBuf(1, 0, 0, 0, 0, 0, 0, 100000, 0, 0, 0, 0, 0);		// 所有的玩家都不能miss
		if(round <= 30){
			antiDefence(bs);				//本回合的任何攻击附带崩溃一击效果
			return useAbilityToHighestUnit(bs,ability1,them,themPet);
		}else{
			shout("亲，都三十回合了，受不了啦！",bs,them);
			antiDefence(bs);				//本回合的任何攻击附带崩溃一击效果
			bs.AddAttrBuf(1,0,0,0,0,100,0,1000000,0,0,1000000,1000000,100003);
			return useAbilityToHighestUnit(bs,ability1,them,themPet);
		}
	}
}
