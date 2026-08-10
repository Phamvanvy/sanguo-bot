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
 * 当boss在第n回合累积受到伤害达到50万时会在第n回合对自己释放彩云遮日8并且在第n+1回合喊话：“去死吧！一群蝼蚁！”
 * 在第n+1回合秒杀当前血量最高的玩家的宠物。（血量最高的玩家如果没有宠物则这条过。）
 * 
 * 当boss在第n回合累积受到伤害达到100万时会在第n回合对自己释放彩云遮日8并且在第n+1回合喊话：“你弄疼我了！”
 * 在第n+1回合秒杀当前血量最高的玩家。（如果上场无玩家了则这条过。）
 * 
 * 当boss在第n回合累积受到伤害达到150万时会在第n回合对自己释放彩云遮日8并且在第n+1回合喊话：“你们都走开！”
 * 在第n+1回合boss随机到任何一个技能所造成的伤害乘以3（如果上场无任何宠物了则这条过。）
 * 
 * 当boss1死亡后，boss2(A),boss2(B)没有死亡时，boss2(C),为正常状态可以被玩家击杀，
 * 若boss2(A),boss2(B)其中一只死亡，则boss2(C)为狂暴状态触发战斗后气泡喊话“你杀掉了我的兄弟，就别想再杀掉我！”后秒杀玩家。
 */
public class Ai90017_6 extends BaseMonsterAI{
    private int state = 0;
    private boolean stateChange = false;
    private Skill sheild_cai;

    public Ai90017_6(){
        addSkill(79, 20, true);
        addSkill(80, 20, true);
        addSkill(7, 20, true);
        addSkill(8, 20, true);
        addSkill(112, 20, true);

        sheild_cai = (Skill) Skill.getSkill(144).clone();
    }

    public int getSpecialHp(){
        return 2000000;
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
	            if(monsterDie(them[0], 0xD12000) || monsterDie(them[0], 0xD12001)){
	                shout("你杀掉了我的兄弟，就别想再杀掉我！", bs, them);
	                return killAllPlayer(bs, them, themPet);
	            }
	        }
        }
        if(!bs.canAction()){
            defaultCannotActionAction(bs);
            return false;
        }else{
            antiDefence(bs);
            defenceAllDebuff_1_round(bs);
            
            if(stateChange){
                stateChange = false;

                if(state == 1){
                    BattleSprite target = selectHighestHpTeam(them);

                    if(themPet[target.groupIndex] != null){
                        return killOneSprite(bs, themPet[target.groupIndex]);
                    }
                }else if(state == 2){
                    BattleSprite target = selectHighestHpTeam(them);
                    return killOneSprite(bs, target);
                }else if(state == 3){
                    bs.AddAttrBuf(1, 300, 300, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30002);
                    return useSkillToHighestUnit(bs, pickSkill(), them, themPet);
                }

                return useSkillToHighestUnit(bs, pickSkill(), them, themPet);
            }else{
                if(state < 3 && bs.hurted > 1500000){
                    state = 3;
                    stateChange = true;
                    shout("你们都走开！", bs, them);
                    return useSkillForSelf(bs, sheild_cai);
                }else if(state < 2 && bs.hurted > 1000000){
                    state = 2;
                    stateChange = true;
                    shout("你弄疼我了！", bs, them);
                    return useSkillForSelf(bs, sheild_cai);
                }else if(state < 1 && bs.hurted > 500000){
                    state = 1;
                    stateChange = true;
                    shout("去死吧！一群蝼蚁！", bs, them);
                    return useSkillForSelf(bs, sheild_cai);
                }else{
                    return useSkillToHighestUnit(bs, pickSkill(), them, themPet);
                }
            }
        }
    }
}
