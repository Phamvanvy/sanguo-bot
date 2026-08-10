package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.Skill;
import com.pip.itimes.server.world.battle.SkillConstants;

public class Ai90014_6 extends BaseMonsterAI {

	public static final Ability ability2 = Ability.getAbility(152);	//手足护卫

	public boolean action(BattleSprite bs, int index, BattleSprite[] our,
			BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet,
			Vector battleMovie,
            BattleDataProcess battleDataProcess,int round) {
		// 免疫状态
		if(round == 1){		//免疫所有状态
			defenceAllDebuff(bs);
		}
		
		if(index == 0){
			//1个boss加血
			if(our[0] != null){
				Skill skill = (Skill)Skill.getSkill(218).clone();
				skill.parm1 = our[1].attributes[BattleSprite.ATTR_HPMAX] - our[1].hp;
				skill.mpUse = 0;
				return useSkillForTeam(bs,our[1],skill);
			}else{
				return useDefaultAttackToHighestUnit(bs,them,themPet);
			}
			
		}else if(index == 2){
			//2个boss加防护
			if(our[0] != null && our[0].getDebufStatus()==Skill.STATUS_DIE){
				Skill skill = (Skill)Skill.getSkill(218).clone();
				skill.parm1 = our[1].attributes[BattleSprite.ATTR_HPMAX] - our[1].hp;
				skill.mpUse = 0;
				return useSkillForTeam(bs,our[1],skill);
			}else{	//加防护
				Skill skill = (Skill)Skill.getSkill(176).clone();
				return useSkillForTeam(bs,our[1],skill);
			}
		}
		return false;
	}

}
