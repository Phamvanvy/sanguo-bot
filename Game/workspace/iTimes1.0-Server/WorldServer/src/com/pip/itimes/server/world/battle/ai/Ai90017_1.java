package com.pip.itimes.server.world.battle.ai;

import java.util.Vector;

import com.pip.itimes.server.world.battle.BaseMonsterAI;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.Skill;

/**
 * @author leo
 * @version 1.0
 */

/**
 * 狂战怪
 * 技能： 缤纷连击7(20)，缤纷连击8(60)，致晕攻击8(10)，霜冻魔法8(10)
 * 魔战怪，狂战怪，辅战怪必须在2回合内一起死亡，若没有死亡，则已死的怪满血满魔复活。怪无限蓝
 * 怪的所有攻击无视，荆棘之墙，魔力镜，攻击吸收，魔法吸收，且狂战怪，魔战怪，辅战怪不受任何控制技能影响。
 */
public class Ai90017_1 extends BaseMonsterAI{
    public Ai90017_1(){
        addSkill(7, 20, true);
        addSkill(8, 60, true);
        addSkill(32, 10, true);
        addSkill(112, 10, true);
    }
    
    public int getSpecialHp(){
        return 1500000;
    }

    public boolean action(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie, BattleDataProcess battleDataProcess,
                    int round){
        //2回合内不一起死亡则全部复活并满血
        boolean needRelive = false;
        for(int i = 0; i < our.length; i++){
            if(bs != our[i] && our[i].dieRound + 2 <= round){
                needRelive = true;
            }
        }

        if(needRelive){
            for(int i = 0; i < our.length; i++){
                if(bs != our[i] && our[i].testDie()){
                    our[i].setTarget(our[i], our[i].groupIndex);
                    our[i].target.setBufStatus(1, Skill.STATUS_AUTO_RELIFE, 0, 100, 100, our[i].bsType, our[i].groupIndex);
                }
            }
        }
        
        if(!bs.canAction()){
            defaultCannotActionAction(bs);
            return false;
        }else{
            //免疫所有状态攻击，并且攻击不受任何种类护盾影响
            defenceAllDebuff_1_round(bs);
            antiDefence(bs);

            //挑选一个技能攻击
            return useSkillToHighestUnit(bs, pickSkill(), them, themPet);
        }
    }
}
