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
 * 辅战怪
 * 技能： 天堂圣盾8(30)，彩云遮日8(20)，致晕攻击8(10)，群鹰出击7(10)，特殊效果A(30)
 * 魔战怪，狂战怪，辅战怪必须在2回合内一起死亡，若没有死亡，则已死的怪满血满魔复活。怪无限蓝
 * 怪的所有攻击无视，荆棘之墙，魔力镜，攻击吸收，魔法吸收，且狂战怪，魔战怪，辅战怪不受任何控制技能影响。
 * 当(呦哎喂)在当前第n回合随即到特殊技效果A时则在第n回合对自己释放天堂圣盾8并且气泡喊话：“我还没有疲倦！”，在n+1回合将(喂呦哎)，(哎呦喂)，(呦哎喂)血蓝回满，
 * 若玩家在第n回合和n+1回合释放任何控制技能必生效，打断(呦哎喂)的特殊技效果
 */
public class Ai90017_3 extends BaseMonsterAI{
    private Skill shield;
    private Skill heal;

    /**
     * 特殊技能A
     */
    private Skill SPECIAL_SKILL_A = new Skill((short) 30001, Skill.TYPE_STAY, 0, Skill.ENMITY_ALL, 0, Skill.SPEED_METHOD_FIRST, "");
    private int special_a_round = -1;

    public Ai90017_3(){
        addSkill(176, 25, true);
        addSkill(144, 20, true);
        addSkill(32, 10, true);
        addSkill(79, 20, true);
        addSkill(SPECIAL_SKILL_A, 25, true);

        shield = (Skill) Skill.getSkill(176).clone();
        heal = (Skill) Skill.getSkill(202).clone();
        heal.speedMethod = Skill.SPEED_METHOD_LAST;
    }

    public int getSpecialHp(){
        return 1000000;
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
            //攻击不受任何种类护盾影响
            antiDefence(bs);

            if(special_a_round >= 0){
                //退出特殊技能状态
                special_a_round = -1;

                //补满所有人的血
                heal.parm1 = getHealHp(our);
                return useSkillForSelf(bs, heal);
            }else{
                Skill skill = pickSkill();
                
                //特殊技能A
                if(skill.id == SPECIAL_SKILL_A.id){
                    //喊话
                    shout("我还没有疲倦！", bs, them);
                    special_a_round = round;
                    return useSkillForSelf(bs, shield);
                }else{
                    //特殊技能A没触发时，免疫控制
                    defenceAllDebuff_1_round(bs);

                    if(skill.id == 176 || skill.id == 144){
                        return useSkillForSelf(bs, skill);
                    }else{
                        return useSkillToHighestUnit(bs, skill, them, themPet);
                    }
                }
            }
        }
    }
}
