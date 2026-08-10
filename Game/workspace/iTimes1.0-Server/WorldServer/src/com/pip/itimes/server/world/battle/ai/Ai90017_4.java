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
 * 群鹰出击7，25%
 * 缤纷连击8，25%
 * 缤纷连击7，25%
 * 群鹰出击8，25%
 * 当Boss1没有死亡的时候，boss2(A),boss2(B),boss2(C)为狂暴状态触发战斗后气泡喊话“你还没有资格挑战我！”后秒杀玩家。
 * 当boss1死亡后，boss2(B),boss2(C)没有死亡时，boss2(A),为正常状态可以被玩家击杀，若boss2(B),boss2(C)其中一只死亡，
 * 则boss2(A)为狂暴状态触发战斗后气泡喊话“你杀掉了我的兄弟，就别想再杀掉我！”后秒杀玩家。
 */
public class Ai90017_4 extends BaseMonsterAI{
    /**普通姿态，50%
     * 
     */
    private Skill STATE_A = new Skill((short) 30001, Skill.TYPE_STAY, 0, Skill.ENMITY_ALL, 0, Skill.SPEED_METHOD_FIRST, "");
    /**狂暴姿态，15%
     * 当战士怪在当前第n回合随即到狂暴姿态效果时则在第n回合对自己释放彩云遮日8并且气泡喊话：“无法抑制的怒火！”，在n+1回合和n+2回合不再走姿态表，
     * 在狂暴姿态的2回合时怪物攻击伤害提升3倍，物防魔防减少为0。在狂暴姿态时怪物攻击可以被任何辅助技能吸收。
     */
    private Skill STATE_B = new Skill((short) 30002, Skill.TYPE_STAY, 0, Skill.ENMITY_ALL, 0, Skill.SPEED_METHOD_FIRST, "");
    /**防御姿态，10%
     * 当战士怪在当前第n回合随即到防御姿态效果时则在第n回合对自己释放彩云遮日8并且气泡喊话：“这就是你们可笑的攻击！”，在n+1回合和n+2回合不再走姿态表，
     * 在防御姿态的2回合时怪受到玩家的任何伤攻击害均为1，在防御姿态时怪物攻击可以被任何辅助技能吸收。
     */
    private Skill STATE_C = new Skill((short) 30003, Skill.TYPE_STAY, 0, Skill.ENMITY_ALL, 0, Skill.SPEED_METHOD_FIRST, "");
    /**战斗姿态，25%
     * 当战士怪在当前第n回合随即到战斗姿态效果时则在第n回合对自己释放天堂圣盾8并且气泡喊话：“永无止境的战斗！”，在n+1回合和n+2回合不再走姿态表，
     * 在战斗姿态的2回合时怪物攻击必暴击，怪物必会躲开玩家的任何攻击。
     */
    private Skill STATE_D = new Skill((short) 30004, Skill.TYPE_STAY, 0, Skill.ENMITY_ALL, 0, Skill.SPEED_METHOD_FIRST, "");

    private Skill sheild_cai;
    private Skill sheild_dun;
    private Skill currentState = STATE_A;

    private int state_round = -1;

    public Ai90017_4(){
        sheild_cai = (Skill) Skill.getSkill(144).clone();
        sheild_dun = (Skill) Skill.getSkill(176).clone();
    }

    public int getSpecialHp(){
        return 3000000;
    }
    
    public boolean action(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie, BattleDataProcess battleDataProcess,
                    int round){
    	if(them[0].player!=null && them[0].player.getMap()!=null && them[0].player.getMap().getMapId() == 209){
	    	//Boss1未死亡狂暴
	        if(!monsterDie(them[0], 0xD12003)){
	            shout("你还没资格挑战我!", bs, them);
	            return killAllPlayer(bs, them, them);
	
	        }else{
	            //另2个boss有死亡，则狂暴
	            if(monsterDie(them[0], 0xD12001) || monsterDie(them[0], 0xD12002)){
	                shout("你杀掉了我的兄弟，就别想再杀掉我！", bs, them);
	                return killAllPlayer(bs, them, themPet);
	            }
	        }
    	}
        if(!bs.canAction()){
            defaultCannotActionAction(bs);
            return false;
        }else{
            if(state_round >= 0){
                //两回合后重新进入状态选择
                if(round > state_round + 2){
                    state_round = -1;
                }
                
                switch(currentState.id){
                    case 30001:
                        //普通姿态，免疫控制，攻击破控
                        antiDefence(bs);
                        defenceAllDebuff_1_round(bs);
                        return useSkillToHighestUnit(bs, getAttakSkill(), them, themPet);
                    case 30002:
                        //狂暴姿态
                        //攻击力提高3倍，防御减为0，免疫控制，在狂暴姿态时怪物攻击可以被任何辅助技能吸收。
                        defenceAllDebuff_1_round(bs);
                        bs.AddAttrBuf(1, 300, 0, -100, -100, 0, 0, 0, 0, 0, 0, 0, 30002);
                        return useSkillToHighestUnit(bs, getAttakSkill(), them, themPet);
                    case 30003:
                        //防御姿态
                        //受到玩家任何伤害都为1，在防御姿态时怪物攻击可以被任何辅助技能吸收。
                        defenceAllDebuff_1_round(bs);
                        bs.AddAttrBuf(1, 0, 0, 100000, 100000, 0, 0, 0, 0, 0, 0, 0, 30003);
                        return useSkillToHighestUnit(bs, getAttakSkill(), them, themPet);
                    case 30004:
                        //战斗姿态
                        //攻击必暴击，怪物必会躲开玩家的任何攻击，免疫控制，攻击破控
                        antiDefence(bs);
                        defenceAllDebuff_1_round(bs);
                        bs.AddAttrBuf(1, 0, 0, 0, 0, 0, 100000, 100000, 0, 0, 0, 0, 30004);
                        return useSkillToHighestUnit(bs, getAttakSkill(), them, themPet);
                }
            }else{
                Skill stateskSkill = stateChange();
                currentState = stateskSkill;

                if(stateskSkill.id != 30001){
                    state_round = round;
                }

                switch(stateskSkill.id){
                    case 30001:
                        //普通姿态，免疫控制，攻击破控
                        antiDefence(bs);
                        defenceAllDebuff_1_round(bs);
                        return useSkillToHighestUnit(bs, getAttakSkill(), them, themPet);
                    case 30002:
                        //狂暴姿态
                        shout("无法抑制的怒火！", bs, them);
                        //攻击力提高3倍，防御减为0，免疫控制
                        defenceAllDebuff_1_round(bs);
                        bs.AddAttrBuf(1, 300, 0, -100, -100, 0, 0, 0, 0, 0, 0, 0, 30002);
                        return useSkillForSelf(bs, sheild_cai);
                    case 30003:
                        //防御姿态
                        shout("这就是你们可笑的攻击！", bs, them);
                        //受到玩家任何伤害都为1，免疫控制
                        defenceAllDebuff_1_round(bs);
                        bs.AddAttrBuf(1, 0, 0, 100000, 100000, 0, 0, 0, 0, 0, 0, 0, 30003);
                        return useSkillForSelf(bs, sheild_cai);
                    case 30004:
                        //战斗姿态
                        shout("永无止境的战斗！", bs, them);
                        //攻击必暴击，怪物必会躲开玩家的任何攻击，免疫控制，攻击破控
                        antiDefence(bs);
                        defenceAllDebuff_1_round(bs);
                        bs.AddAttrBuf(1, 0, 0, 0, 0, 0, 100000, 100000, 0, 0, 0, 0, 30004);
                        return useSkillForSelf(bs, sheild_dun);
                }
            }

            return false;
        }
    }

    public Skill stateChange(){
        clearSkill();
        addSkill(STATE_A, 50, true);
        addSkill(STATE_B, 15, true);
        addSkill(STATE_C, 10, true);
        addSkill(STATE_D, 25, true);

        return pickSkill();
    }

    public Skill getAttakSkill(){
        clearSkill();
        addSkill(79, 25, true);
        addSkill(80, 25, true);
        addSkill(7, 25, true);
        addSkill(8, 25, true);

        return pickSkill();
    }
}
