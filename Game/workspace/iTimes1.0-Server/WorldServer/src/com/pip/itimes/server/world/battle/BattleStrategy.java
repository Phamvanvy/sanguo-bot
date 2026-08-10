package com.pip.itimes.server.world.battle;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.Utils;

import org.apache.log4j.Logger;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class BattleStrategy implements SkillConstants{


    private static final Logger log = Logger.getLogger(BattleStrategy.class);

    public void fillSpriteStatus(BattleSprite bs,BattleDataProcess battle){
        bs.endProcess(battle.battleMovie, battle);
    }

    public boolean doSkill(BattleSprite opp, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie, int bout, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        int groupIndex = opp.groupIndex;		//这个怪在他们组的索引
        int[] movie;
        BattleSprite targetSprite;
        boolean solidSkill = true;
        boolean win = false;

        targetSprite = null;

        for(int i = 0; i < them.length; i++){
            if(!them[i].testCannotBattle()){
                targetSprite = them[i];

                break;			//虽是一个for循环，但使用break，有可以战斗的玩家就跳出循环。
            }
        }

        if (targetSprite == null) {
            return true;
        }						//若对方全部死掉，就跳出方法。

        targetSprite = null;

        if (opp.target != null && opp.target.testCannotBattle()) {
    //            Skill skill = Skill.getSkill(opp.skillId);
            Skill skill = opp.skill;
            if (opp.target.testCannotBattle() && skill != null &&
                skill.effect != Skill.EFFECT_SAVE_LIFE) {
                targetSprite = selectTargetRandom(opp, them, themPet);

                if (targetSprite != null) {
                    opp.setTarget(targetSprite, targetSprite.groupIndex);
                } else {
                    win = true;
                }
            }
        } else if (opp.target == null) {
            int[] ret = opp.skill.getSkillStatus(opp, opp.skill.id);
            switch (ret[3]) {
                case Skill.CHOOSE_ENEMY:
                    log.info("Battle Target Error Skill["+opp.skill.id+"]");
                    targetSprite = selectTargetRandom(opp, them, themPet);

                    if (targetSprite != null) {
                        opp.setTarget(targetSprite, targetSprite.groupIndex);
                    } else {
                        win = true;
                    }
                    break;
                case Skill.CHOOSE_NONE:
                    break;
                case Skill.CHOOSE_OWNER:
                	if(opp.pet != null && our[groupIndex] != null && !our[groupIndex].testDie()){
            			opp.setTarget(our[groupIndex], groupIndex);
                	}else{
                		opp.setTarget(opp,opp.groupIndex);
                	}
                	break;
                default:
//                    log.info("Battle Target Error Skill["+opp.skill.id+"]");
                    opp.setTarget(opp,opp.groupIndex);
                    break;
            }
        }

        if(selectTargetRandom(opp, them, themPet) == null){
            win = true;
        }

        if(win){
            return win;
        }
        opp.interveneAttackTime = 0;
        //战斗选技能
        try{
            switch(opp.skill.id){
                case Skill.SKILL_ATTACK:
                    if(opp.target.testCannotBattle()){
                        targetSprite = selectTargetRandom(opp, them, themPet);

                        if(targetSprite != null){
                            opp.setTarget(targetSprite, targetSprite.groupIndex);
                        }else{
                            win = true;

                            break;
                        }
                    }

                    processAttack(opp, groupIndex, opp.skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_NORMAL,OVER_POSITION_BACK, battleDataProcess);
                    addBattleRecorder(battleRecorders, opp, Skill.ATTACK_SKILL);
                    processIntervene(opp, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_NORMAL,OVER_POSITION_BACK, battleDataProcess, battleRecorders);
                    break;
                case Skill.SKILL_ITEM:

                    Effect[] effect = ((IEffectItem)opp.usedItem).getEffects();
                    int hp = 0;
                    int mp = 0;
                    for(int i = 0; i < effect.length; i++){
                        int pro = ((PropertyEffect)effect[i]).getProperty();
                        int value = ((PropertyEffect)effect[i]).getValue();
                        if(pro == Changed.HP){
                            hp += value;
                        }else if(pro == Changed.MP){
                            mp += value;
                        }
                    }

                    if(opp.target.testCannotBattle()){
                        opp.setTarget(opp, groupIndex);
                    }
                    opp.target.changeHp(hp, battleMovie, battleDataProcess);
                    opp.target.changeMp(mp);
                    opp.used = true;

                    //TODO jlin to change it
                    //TODO is old code --> opp.target.player.completeRemoveItem(opp.usedItem, 1, null);
                    if(opp.player!=null)
                        opp.player.completeRemoveItem(opp.usedItem, 1, null);
                    opp.lastUsedRound = battleDataProcess.getRound();
                    opp.usedTimes++;
                    movie = makeMovieSub(opp.bsType, groupIndex, opp.target.bsType, opp.targetIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT,
                                    opp.target.getDebufStatus(), 0, 0, 0, hp, mp);
                    battleMovie.addElement(movie);
                    addBattleRecorder(battleRecorders, opp, Skill.ITEM_SKILL);

                    break;
                case Skill.SKILL_RUN:
                    int tmpLevel = 0;

                    for(int i = 0; i < them.length; i++){
                        if(them[i] == null){
                            continue;
                        }

                        if(!them[i].testCannotBattle()){
                            if(tmpLevel < them[i].level){
                                tmpLevel = them[i].level;
                            }
                        }
                    }

                    opp.setTarget(opp, groupIndex);
                    int tmp = testRun(opp,tmpLevel, bout)? Skill.HIT_HIT: Skill.HIT_MISS;

                    if(tmp == Skill.HIT_HIT){
                        opp.setDeBufStatus(1, Skill.STATUS_RUNAWAY, 0, 0, 0, opp.bsType, opp.groupIndex);
                    }

                    movie = makeMovieSub(opp.bsType, groupIndex, opp.bsType, groupIndex, opp.skill.id, Skill.ANIMATE_RUNAWAY, Skill.POSITION_STAY, Skill.OVER_POSITION_BACK, Skill.MOVIE_SPEED_NORMAL, tmp,
                                    opp.getDebufStatus(), Skill.ATTACK_NO_CRI, 0, 0, 0, 0);
                    battleMovie.addElement(movie);
                    addBattleRecorder(battleRecorders, opp, Skill.RUNAWAY_SKILL);
                    break;
                case Skill.SKILL_STAY:
                    if(opp.bsType == BattleSprite.TYPE_PLAYER_PET || opp.bsType == BattleSprite.TYPE_MONSTER_PET){ //宠物的防御减少10%伤害
                        opp.AddAttrBuf(1, 0, 0, 0, 0, 0, 0, 0, -10, -10, 0, 0, SkillConstants.EFFECT_DEFENCE);
                    }
                    
                    movie = makeMovieSub(opp.bsType, groupIndex, opp.bsType, groupIndex, opp.skill.id, Skill.ANIMATE_NONE, Skill.POSITION_STAY, Skill.OVER_POSITION_BACK, MOVIE_SPEED_NORMAL, HIT_HIT, opp
                                .getDebufStatus(), Skill.ATTACK_NO_CRI, 0, 0, 0, 0);
                    battleMovie.addElement(movie);
                    //processIntervene(opp, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_NORMAL,OVER_POSITION_BACK, battleDataProcess, battleRecorders);
                    break;
                case Skill.SKILL_CATCH:
                    int catchHit = opp.doCatch(our, ourPet, them, themPet)? HIT_HIT: HIT_MISS;
                    if(catchHit==HIT_HIT){
                        opp.catchedPet = opp.target;
                        opp.target.setIsCatch((byte)1);		// 抓宠成功
                    }
                    movie = makeMovieSub(opp.bsType, groupIndex, opp.target.bsType, opp.targetIndex, opp.skill.id, ANIMATE_STS_ATK, POSITION_DEST, OVER_POSITION_BACK, MOVIE_SPEED_NORMAL, catchHit,
                                    opp.target.getDebufStatus(), Skill.ATTACK_NO_CRI, 0, 0, 0, 0);
                    battleMovie.addElement(movie);

                    addBattleRecorder(battleRecorders, opp, Skill.CATCH_SKILL);
                    break;
                default:
                    solidSkill = false;

                    break;
            }

            if(solidSkill || win){
                return win;
            }

//            Skill skill = Skill.getSkill(opp.skillId);
            Skill skill = opp.skill;
            
            /**
             * 修改套装属性不可叠加
             */
            if(opp.battleSuitEffect != null){
                int skillPara1 = 0;
                int skillPara2 = 0;
                int skillBout = 0;
                int skillPercent = 0;
                int skillMpUse = 0;
                
                int maxSkillPara1 = 0;
                int maxSkillPara2 = 0;
                int maxSkillBout = 0;
                int maxSkillPercent = 0;
                int maxSkillMpUse = 0;
                 
                for(int i = 0; i < opp.battleSuitEffect.length; i++){
                    BattleSuitEffect effect = opp.battleSuitEffect[i];
                    
                    if(effect.effectSkill(skill.id, opp, our, ourPet, them, themPet)){
                        skillPara1 = effect.getSkillParm1();
                        skillPara2 = effect.getSkillParm2();
                        skillBout = effect.getSkillBout();
                        skillPercent = effect.getSkillPercent();
                        skillMpUse = effect.getSkillMpUse();
                        
                        boolean flag = false;
                        
                        if(Math.abs(skillPara1) > Math.abs(maxSkillPara1)){
                            flag = true;
                        }
                        
                        if(Math.abs(skillPara2) > Math.abs(maxSkillPara2)){
                            flag = true;
                        }
                        
                        if(Math.abs(skillBout) > Math.abs(maxSkillBout)){
                            flag = true;
                        }
                        
                        if(Math.abs(skillPercent) > Math.abs(maxSkillPercent)){
                            flag = true;
                        }
                        
                        if(Math.abs(skillMpUse) > Math.abs(maxSkillMpUse)){
                            flag = true;
                        }
                        
                        if(flag){
                            maxSkillPara1 = skillPara1;
                            maxSkillPara2 = skillPara2;
                            maxSkillBout = skillBout;
                            maxSkillPercent = skillPercent;
                            maxSkillMpUse = skillMpUse;
                        }
                        
                        effect.doEffect();
                    }
                }
                
                skill = (Skill)skill.clone();
                skill.parm1 += maxSkillPara1;
                skill.parm2 += maxSkillPara2;
                skill.effectBout += maxSkillBout;
                skill.hitRate += maxSkillPercent;
                maxSkillMpUse = 100 - maxSkillMpUse;
                
                if(maxSkillMpUse < 0){
                    maxSkillMpUse = 0;
                }
                
                if(skill.mpUse > -9999){
                    skill.mpUse = (short)(skill.mpUse * maxSkillMpUse / 100);
                }
            }

            int animateType = Skill.ANIMATE_NONE;

            switch(skill.type){
                case Skill.TYPE_PHY:
                    animateType = Skill.ANIMATE_PHY_START;

                    break;
                case Skill.TYPE_MGC:
                    animateType = Skill.ANIMATE_MGC_START;

                    break;
                case Skill.TYPE_DEF:
                    animateType = Skill.ANIMATE_DEF_START;

                    break;
                case Skill.TYPE_ASS:
                    animateType = Skill.ANIMATE_ASS_START;

                    break;
                case Skill.TYPE_PET:
                    animateType = Skill.ANIMATE_PHY_START;

                    break;
            }
            //mengjie modify
            opp.usedMp = skill.getMpUse(opp);
            
            if(opp.mp < opp.usedMp){
                movie = makeMovieSub(opp.bsType, groupIndex, opp.bsType, groupIndex, opp.skill.id, Skill.ANIMATE_NONE, Skill.POSITION_STAY, Skill.OVER_POSITION_BACK, MOVIE_SPEED_NORMAL, HIT_HIT, opp
                                .getDebufStatus(), Skill.ATTACK_NO_CRI, 0, 0, 0, 0);
                battleMovie.addElement(movie);

                return win;
            }
            opp.changeMp(-opp.usedMp);

            movie = makeMovieSub(opp.bsType, groupIndex, opp.bsType, groupIndex, opp.skill.id, animateType, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, opp.getDebufStatus(),
                            Skill.ATTACK_NO_CRI, 0, -opp.usedMp, 0, 0);
            battleMovie.addElement(movie);

            switch(skill.effect){
                case Skill.EFFECT_MULTI_ATK:
                    win = processMultiAttack(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleDataProcess);

                    break;
                case EFFECT_INC_ATK_INC_DMG:
                    win = processIncreaseAttackIncreaseDamage(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_ANTI_BUF_INC_ATK:
                    win = processAntiBufIncreaseAttack(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_FAINT:
                    win = processFaintAttack(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_INC_ATK_INC_CRI:
                    win = processIncreaseAttackIncreaseCri(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_INC_DMG_DEC_HIT:
                    win = processIncreaseDamageDecreaseHit(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_INC_ATK_STOP:
                    win = processIncreaseAttackStop(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_ALWAYS_INC_ATK_INC_DMG:
                    win = processAlwaysIncreaseAttackIncreaseDamage(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_MGC_ATK:
                    win = processMagicAttack(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_MULTI_MGC:
                    win = processMultiMagic(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_LET_POISON:
                    win = processLetPoison(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_LET_SLEEP:
                    win = processLetSleep(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_LET_STONE:
                    win = processLetStone(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_MAGIC_ALL:
                    win = processMagicAll(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_LET_CONFUSE:
                    win = processLetConfuse(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_INC_MGC_LET_POSION:
                    win = processIncreaseMagicLetPosion(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_MGC_USE_ALL_MP:
                    win = processMagicUseAllMp(opp, groupIndex, skill, our, ourPet, our, ourPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_DEC_ATK_INC_FLEE:
                    win = processDecreaseAttackIncreaseFlee(opp, groupIndex, skill, our, ourPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_BLOCK_ATK_DEC_DMG:
                    win = processBlockAttackDecreaseDamage(opp, groupIndex, skill, our, ourPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_DEC_PHY_DMG:
                    win = processDecreasePhyDamage(opp, groupIndex, skill, our, ourPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_DEC_MGC_DMG:
                    win = processDecreaseMagicDamage(opp, groupIndex, skill, our, ourPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_DEC_PHY_MGC_DMG:
                    win = processDecreasePhyMagicDamage(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PHY_DMG_TO_HP:
                    win = processSorbPhyDamage(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_MGC_DMG_TO_HP:
                    win = processSorbMagicDamage(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_RESTORE_HP:
                    win = processRestoreHp(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_RESTORE_ALL_HP:
                    win = processRestoreAllHp(opp, groupIndex, skill, our, ourPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_SAVE_LIFE:
                    win = processSaveLife(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_ANTI_PHY:
                    win = processAntiPhy(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_ANTI_MGC:
                    win = processAntiMagic(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_RESTORE_LOT_HP:
                    win = processRestoreLotHp(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_CLEAR_STS_AND_ANTI:
                    win = processClearStatusAndAnti(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_2_ATTACK:
                    win = processPet2Attack(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_2_ATTACK2:
                	win = processPet2Attack2(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);
                	
                	break;
                case EFFECT_PET_ADD_DMG_ADD_CRI_PHY:
                case EFFECT_PET_ADD_DMG_ADD_CRI_PHY2:
                    win = processPetAddDmgAddCriPhy(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_ADD_DMG_ADD_ATTACK:
                    win = processPetAddDmgAddAttack(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_ANTI_MGC:
                    win = processPetAntiMagic(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_ADD_DMG_DEC_HIT:
                case EFFECT_PET_ADD_DMG_DEC_HIT2:
                    win = processPetAddDmgDecHit(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_LET_POISON:
                    win = processPetLetPoison(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_MAGIC:
                    win = processPetMagic(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_2_MAGIC:
                    win = processPet2Magic(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_LET_FROST:
                    win = processPetLetFrost(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_ANTI_PHY:
                    win = processPetAntiPhy(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_DEC_DMG_ALL_BATTLE:
                case EFFECT_PET_DEC_DMG_ALL_BATTLE2:
                    win = processPetDecDmgAllBattle(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_DMG_TO_MGC:
                    win = processPetDmgToMgc(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_IMM_STATUS:
                    win = processPetImmStatus(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_PROTECT_OWNER:
                case EFFECT_PET_PROTECT_OWNER2:
                    win = processPetProtectOwner(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_DEC_DMG:
                    win = processPetDecDmg(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_ATT_DMG_TO_HP:
                    win = processPetAttackDamageToHp(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_AUTO_RELIFE:
                    win = processPetAutoRelife(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_ADD_OWNER_HP:
                case EFFECT_PET_ADD_OWNER_HP2:
                    win = processPetAddOwnerHp(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_ADD_OWNER_MP:
                    win = processPetAddOwnerMp(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_PET_UN_ALL_STATUS:
                    win = processPetUnAllStatus(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);

                    break;
                case EFFECT_DEFENCE:
                    win = processDefence(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
                    
                    break;
                case EFFECT_PET_BAIT:
                	win = processPetAddDemageAndPoison(opp, groupIndex, skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);
                	break;
                case EFFECT_PET_BAD_SEED:
                	win = processPetBadSeed(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
                	break;
                case EFFECT_PET_SACRIFICE_BLESS:
                	opp.setTarget(our[opp.index], our[opp.index].groupIndex);
                	win = processPetSacrificeBless(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
                	break;
            }

            opp.coolDownSkill(skill);
//            processIntervene(opp, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_NORMAL,OVER_POSITION_BACK, battleDataProcess, battleRecorders);
        }finally{
            opp.processSuitEffect(our, ourPet, them, themPet, battleMovie, battleDataProcess);
            battleDataProcess.spriteDoneSkill(opp, groupIndex, false);
        }

        return win;
    }

    /**
     * 宠物，物：双次物攻
     */
    protected boolean processPet2Attack(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        BattleSprite targetSprite;
        boolean win = false;

//        in.setSkill(Skill.NONE_SKILL);

        for(int i = 0; i < 2; i++){
            targetSprite = selectTargetRandom(in, them, themPet);

            if(targetSprite != null){
                if(in.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie, battleDataProcess);

                    break;
                }

                if(i == 2 - 1){
                    processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
                }else{
                    processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY, battleDataProcess);
                }
            }else{
                processEmptyLoop(in, index, battleMovie, battleDataProcess);

                win = true;

                break;
            }
        }

        in.setDeBufStatus(skill.parm1, Skill.STATUS_STOP, 0, 0, 0, in.bsType, in.groupIndex);
        processStatusUpdate(in, index, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return win;
    }
    
    /**
     * 宠物，物：双次物攻加伤害
     */
    protected boolean processPet2Attack2(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
    		BattleDataProcess battleDataProcess){
    	BattleSprite targetSprite;
    	boolean win = false;
    	
//        in.setSkill(Skill.NONE_SKILL);
    	in.AddAttrBuf(skill.effectBout, skill.parm2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
    	for(int i = 0; i < 2; i++){
    		targetSprite = selectTargetRandom(in, them, themPet);
    		
    		if(targetSprite != null){
    			if(in.testCannotBattle()){
    				processEmptyLoop(in, index, battleMovie, battleDataProcess);
    				
    				break;
    			}
    			
    			if(i == 2 - 1){
    				processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
    			}else{
    				processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY, battleDataProcess);
    			}
    		}else{
    			processEmptyLoop(in, index, battleMovie, battleDataProcess);
    			
    			win = true;
    			
    			break;
    		}
    	}
    	
    	in.setDeBufStatus(skill.parm1, Skill.STATUS_STOP, 0, 0, 0, in.bsType, in.groupIndex);
    	processStatusUpdate(in, index, battleMovie, battleDataProcess);
    	
    	addBattleRecorder(battleRecorders, in, skill);
    	
    	return win;
    }

    /**
     * 宠物，物：加伤加爆
     */
    protected boolean processPetAddDmgAddCriPhy(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, skill.parm2, skill.parm1, skill.parm1, 0, 0, skill.effect);
            processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
        }

        addBattleRecorder(battleRecorders, in, skill);

        return win;
    }

    /**
     * 宠物，物：加伤加攻
     */
    protected boolean processPetAddDmgAddAttack(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm2, 0, 0, 0, 0, 0, 0, skill.parm1, skill.parm1, 0, 0, skill.effect);
            processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
        }

        addBattleRecorder(battleRecorders, in, skill);

        return win;
    }

    /**
     * 宠物，物：反击魔法
     */
    protected boolean processPetAntiMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.setTarget(in, in.groupIndex);
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_MAGIC, skill.level, (in.level / 10 + (in.level < 10? 1: 0)) * 10 * 100 / skill.parm1, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，物：加伤降命
     */
    protected boolean processPetAddDmgDecHit(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, -skill.parm2, 0, 0, 0, 0, 0, 0, skill.effect);
            processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
        }

        addBattleRecorder(battleRecorders, in, skill);

        return win;
    }

    /**
     * 宠物，魔：上毒技能   使目标中毒，每回合结束时减少生命值，减少的量会随宠物等级或力量的提升而增加，持续3回合。</skill>
     */
    protected boolean processPetLetPoison(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;
        
        win = testTargetRandom(in, them, themPet);

        if(in.testHitPet(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && (in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !in.target.isImmuneControl())){
            //(100*INT(2+level宠/7)+宠物力量*1.2)*0.5
        	int hpDec = (100 * (int)(2+in.level/7)+ in.attributes[BattleSprite.ATTR_STR] * skill.parm1 / 100) / 2;
        	in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_POISON, skill.level, hpDec, 0, in.bsType, in.groupIndex);
            processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
        }

        addBattleRecorder(battleRecorders, in, skill);

        return win;
    }

    /**
     * 宠物，魔：降防加爆
     */
    protected boolean processPetMagic(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        processMagic(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，魔：双次魔攻
     */
    protected boolean processPet2Magic(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        BattleSprite targetSprite;
        boolean win = false;

//        in.setSkill(Skill.NONE_SKILL);
        in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, -skill.parm1, 0, 0, 0, 0, 0, 0, skill.effect);

        for(int i = 0; i < 2; i++){
            targetSprite = selectTargetRandom(in, them, themPet);

            if(targetSprite != null){
                if(in.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie, battleDataProcess);

                    break;
                }

                if(i == 2 - 1){
                    processMagic(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
                }else{
                    processMagic(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY, battleDataProcess);
                }
            }else{
                processEmptyLoop(in, index, battleMovie, battleDataProcess);

                win = true;

                break;
            }
        }

        return win;
    }

    /**
     * 宠物，魔：法力流失（幻觉）
     */
    protected boolean processPetLetFrost(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(in.testHitPet(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && (in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !in.target.isImmuneControl())){
        	//(100*INT(2+level宠/14)+宠物体力*x/100) * 0.8            INT(2+level宠/14)向下取整
        	int mpDec = (100 * (int)(2+in.level/14)+ in.attributes[BattleSprite.ATTR_VIT] * skill.parm1 / 100) * 8 / 10;
            in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_FROST, skill.level, mpDec, 0, in.bsType, in.groupIndex);
            processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
        }

        addBattleRecorder(battleRecorders, in, skill);

        return win;
    }

    /**
     * 宠物，魔：反击物理
     */
    protected boolean processPetAntiPhy(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.setTarget(in, in.groupIndex);
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_ATTACK, skill.level, (in.level / 10 + (in.level < 10? 1: 0)) * 10 * 100 / skill.parm1, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，防：减低伤害
     */
    protected boolean processPetDecDmgAllBattle(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.setTarget(in, in.groupIndex);
        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, -skill.parm1, 0, 0, skill.effect);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，防：伤害转魔
     */
    protected boolean processPetDmgToMgc(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.setTarget(in, in.groupIndex);
        in.setBufStatus(skill.effectBout, Skill.STATUS_DAMAGE_TO_MP, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，防：免疫状态
     */
    protected boolean processPetImmStatus(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.setTarget(in, in.groupIndex);
        in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, skill.parm1, skill.parm1, 0, 0, skill.effect);
        in.setBufStatus(skill.effectBout, Skill.STATUS_IMMUNITY_STATUS, skill.level, 0, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，防：替主抗伤
     */
    protected boolean processPetProtectOwner(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_PROTECTED, skill.level, skill.parm1, 1, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，防：减低伤害
     */
    protected boolean processPetDecDmg(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.setTarget(in, in.groupIndex);
        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, -skill.parm1, 0, 0, skill.effect);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，治：输出回血
     */
    protected boolean processPetAttackDamageToHp(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet,BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.setBufStatus(skill.effectBout, Skill.STATUS_ATTACK_DAMAGE_TO_HP, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
        processMagic(in, index, skill, our, ourPet, them, themPet, battleMovie, POSITION_DEST, OVER_POSITION_BACK, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，治：死后复生
     */
    protected boolean processPetAutoRelife(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.setTarget(in, in.groupIndex);
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_AUTO_RELIFE, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，治：给主加血 生命链接
     */
    protected boolean processPetAddOwnerHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
    	//(100*INT(2+level宠/7)+宠物智力*x/100) * 0.4
//        int hpAdd = in.level * skill.parm1 / 100;
    	int hpAdd = (100 * (int)(2 + in.level/7) + in.attributes[BattleSprite.ATTR_INT] * skill.parm1 / 100) * 4 / 10;
        int cri = ATTACK_NO_CRI;

        if(in.testMCri()){
            hpAdd *= BattleSprite.CRI_RATE;
            cri = ATTACK_CRI;
        }else{
            cri = ATTACK_NO_CRI;
        }

        in.target.changeHp(hpAdd, battleMovie, battleDataProcess);
        processRestore(in, index, hpAdd, 0, cri, battleMovie, battleDataProcess);

        in.setTarget(in, in.groupIndex);
        in.changeHp(hpAdd, battleMovie, battleDataProcess);
        processRestore(in, index, hpAdd, 0, cri, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，治：给主回魔 魔力链接
     */
    protected boolean processPetAddOwnerMp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
    	//(100*INT(2+level宠/5)+宠物智力*x/100) * 0.4
        int mpAdd = (100 * (int)(2 + in.level/5) + in.attributes[BattleSprite.ATTR_INT] * skill.parm1 / 100) * 4 / 10;

        if(in.testMCri()){
            mpAdd *= BattleSprite.CRI_RATE;
            processRestore(in, index, 0, mpAdd, ATTACK_CRI, battleMovie, battleDataProcess);
        }else{
            processRestore(in, index, 0, mpAdd, ATTACK_NO_CRI, battleMovie, battleDataProcess);
        }

        in.target.changeMp(mpAdd);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    /**
     * 宠物，治：解除状态
     */
    protected boolean processPetUnAllStatus(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, skill.parm1, skill.parm1, 0, 0, skill.effect);

        in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
        processUnStatus(in, index, HIT_HIT, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }
    /**
     * 宠物，撕咬
     */
    protected boolean processPetAddDemageAndPoison(BattleSprite in, int index, Skill skill, BattleSprite[] our,BattleSprite[] ourPet,BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
            BattleDataProcess battleDataProcess){
    	in.AddAttrBuf(1, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
    	boolean hit = processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
    	if(hit&&in.testHitPet(skill.hitRate, BattleSprite.ACTION_MATTACK) && (in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !in.target.isImmuneControl())){
        	int[] battleResult = in.doBattle(BattleSprite.ACTION_PATTACK, our, ourPet, them, themPet);
	    	in.target.AddAttrBuf(skill.effectBout-1, 0, 0, 0, 0, 0, 0, 0,0,0, battleResult[1], 0, skill.effect,1, 0, 0, 0, 0);
            //(100*INT(2+level宠/7)+宠物力量*1.2)*0.5
        	int hpDec = (100 * (int)(2+in.level/7)+ in.attributes[BattleSprite.ATTR_STR] * skill.parm1 / 100) / 2;
        	in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_POISON, skill.level, hpDec, 0, in.bsType, in.groupIndex);
            processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
            addBattleRecorder(battleRecorders, in, skill);
        }else{
        	processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
        }
		return false;
    }
    /**
     * 宠物，厄运种子
     */
    protected boolean processPetBadSeed(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
            BattleDataProcess battleDataProcess){
    	boolean hit = false;
    	
    	if(in.testHitPet(skill.hitRate, BattleSprite.ACTION_MATTACK) && (in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !in.target.isImmuneControl())){
        	in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_POISON, 0, 0, 0, in.bsType, in.groupIndex);
            processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
            int attMp = in.attributes[BattleSprite.ATTR_MMAX]*skill.parm1/100;
            in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, 0, skill.parm2, 0, 0, skill.effect, 1, 0, 0, 0, attMp);
            addBattleRecorder(battleRecorders, in, skill);
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
        }
    	return false;
    }
    /**
     * 宠物，牺牲祝福
     */
    protected boolean processPetSacrificeBless(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
            BattleDataProcess battleDataProcess){
    	int hpdesc = -in.attributes[BattleSprite.ATTR_HPMAX] * skill.parm1 / 100;

        int incress = (-hpdesc)*skill.parm1/100*skill.parm1/100;
   	 	in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect, 0, incress, incress, 0, 0);
   	 	addBattleRecorder(battleRecorders, in, skill);

   	 	in.setTarget(in, in.groupIndex);
   	 	in.changeHp(hpdesc, battleMovie, battleDataProcess);
//   	 	processRestore(in, index, hpdesc, 0, 0, battleMovie, battleDataProcess);
   	 	int[] movie = makeMovieSub(in.bsType, index, in.bsType, index, in.skill.id, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.getDebufStatus(), 0, 0, 0, hpdesc, 0);
   	 	battleMovie.addElement(movie);
        return false;
    }

    /**
     * 人物：防御
     */
    protected boolean processDefence(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, -skill.parm1, 0, 0, skill.effect);
        int[] movie = makeMovieSub(in.bsType, index, in.bsType, index, in.skill.id, Skill.ANIMATE_NONE, Skill.POSITION_STAY, Skill.OVER_POSITION_BACK, MOVIE_SPEED_NORMAL, HIT_HIT, in
                        .getDebufStatus(), Skill.ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);

        addBattleRecorder(battleRecorders, in, skill);
        
        return false;
    }

    protected boolean processAttack(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, int movieSpeed, int overPosition,
                    BattleDataProcess battleDataProcess){
        int[] battleResult = in.doBattle(BattleSprite.ACTION_PATTACK, our, ourPet, them, themPet);

        if(battleResult[3] == Skill.ATTACK_SORB && battleResult[0] == Skill.HIT_HIT && getPercentRate(battleResult[4])){
            int hpSorb = battleResult[1] * battleResult[5] / 100;

            if(hpSorb <= 0){
                hpSorb = 1;
            }

            in.target.changeHp(hpSorb, battleMovie, battleDataProcess);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
                            battleResult[2], 0, 0, 0, 0);
            battleMovie.addElement(movie);

            movie = makeMovieSub(in.target.bsType, in.targetIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.target
                            .getDebufStatus(), battleResult[2], 0, 0, hpSorb, 0);
            battleMovie.addElement(movie);

            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
            }

            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_ANTI && battleResult[0] == Skill.HIT_HIT){
            int antiDamage = battleResult[1] * battleResult[4] / 100;

            if(antiDamage <= 0){
                antiDamage = -1;
            }else{
                antiDamage = -antiDamage;
            }

            int overPos = overPosition;

            in.changeHp(antiDamage, battleMovie, battleDataProcess);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                            .getDebufStatus(), battleResult[2], 0, 0, 0, 0);
            battleMovie.addElement(movie);
            if(in.bsType != BattleSprite.TYPE_INTERVENE){
	            if(in.testDie()){
	                overPos = OVER_POSITION_BACK;
	            }
	
	            movie = makeMovieSub(in.bsType, index, in.bsType, index, SKILL_NONE, ANIMATE_HURT, POSITION_STAY, overPos, movieSpeed, HIT_HIT, in.getDebufStatus(), battleResult[2], antiDamage, 0,
	                            0, 0);
	            battleMovie.addElement(movie);
            }
            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
            }

            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_PROCTECT && battleResult[0] == Skill.HIT_HIT){
            int protectDamage = battleResult[1] * (100 - battleResult[4]) / 100;
            int protectSrcType = battleResult[6];
            int protectSrcIndex = battleResult[7];

            BattleSprite src = battleDataProcess.getSprite(protectSrcType, protectSrcIndex);

            if(src.testCannotBattle()){ //保护人已死亡，清除保护状态，按正常攻击处理
                in.target.setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);

                in.target.changeHp(-battleResult[1], battleMovie, battleDataProcess);

                int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target
                                .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
                battleMovie.addElement(movie);

                if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                    in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                    processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
                }

                if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP){
                    int[] bufInfo = in.getbufInfo();

                    int hpInc = (bufInfo[3] * battleResult[1]) / 100;

                    if(hpInc > 0){
                        in.changeHp(hpInc, battleMovie, battleDataProcess);

                        movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
                                        0, 0, hpInc, 0);
                        battleMovie.addElement(movie);
                    }
                }
            }else{
                if(protectDamage <= 0){
                    protectDamage = 1;
                }

                int restDamage = battleResult[1] - protectDamage;

                if(restDamage < 0){
                    restDamage = 0;
                }

                src.changeHp(-protectDamage, battleMovie, battleDataProcess);

                if(restDamage > 0 && battleResult[5] != 0){
                    in.target.changeHp(-restDamage, battleMovie, battleDataProcess);
                }

                int[] movie = makeMovieSub(src.bsType, src.groupIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                                .getDebufStatus(), 0, 0, 0, 0, 0);
                battleMovie.addElement(movie);

                if(restDamage > 0 && battleResult[5] != 0){
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);

                    movie = makeMovieSub(in.bsType, index, in.target.bsType, in.target.groupIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target.getDebufStatus(),
                                    battleResult[2], 0, 0, -restDamage, 0);
                    battleMovie.addElement(movie);
                }else{
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);
                }

                movie = makeMovieSub(src.bsType, src.groupIndex, src.bsType, src.groupIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_BACK, movieSpeed, HIT_HIT, src.getDebufStatus(), 0,
                                0, 0, 0, 0);
                battleMovie.addElement(movie);
            }

            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_PET_DAMAGE_TO_MP && battleResult[0] == Skill.HIT_HIT){
            int mpDamage = battleResult[1] * battleResult[4] / 100;

            if(mpDamage <= 0){
                mpDamage = 1;
            }

            if(mpDamage > in.target.mp){
                mpDamage = in.target.mp;
            }

            int hpDamage = battleResult[1] - mpDamage;

            if(hpDamage < 0){
                hpDamage = 0;
            }

            in.target.changeMp(-mpDamage);
            in.target.changeHp(-hpDamage, battleMovie, battleDataProcess);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
                            battleResult[2], 0, 0, -hpDamage, -mpDamage);
            battleMovie.addElement(movie);

            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
            }

            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_MASTER_PROCTECT && battleResult[0] == Skill.HIT_HIT){
            int protectDamage = battleResult[1] * battleResult[4] / 100;
            int protectSrcType = battleResult[6];
            int protectSrcIndex = battleResult[7];

            BattleSprite src = battleDataProcess.getSprite(protectSrcType, protectSrcIndex);

            if(src.testCannotBattle()){ //保护人已死亡，清除保护状态，按正常攻击处理
                in.target.setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);

                in.target.changeHp(-battleResult[1], battleMovie, battleDataProcess);

                int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target
                                .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
                battleMovie.addElement(movie);

                if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                    in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                    processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
                }

                if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP){
                    int[] bufInfo = in.getbufInfo();

                    int hpInc = (bufInfo[3] * battleResult[1]) / 100;

                    if(hpInc > 0){
                        in.changeHp(hpInc, battleMovie, battleDataProcess);

                        movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
                                        0, 0, hpInc, 0);
                        battleMovie.addElement(movie);
                    }
                }
            }else{
                if(protectDamage <= 0){
                    protectDamage = 1;
                }

                int restDamage = battleResult[1] - protectDamage;

                if(restDamage < 0){
                    restDamage = 0;
                }

                src.changeHp(-protectDamage, battleMovie, battleDataProcess);

                if(restDamage > 0 && battleResult[5] != 0){
                    in.target.changeHp(-restDamage, battleMovie, battleDataProcess);
                }

                int[] movie = makeMovieSub(src.bsType, src.groupIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                                .getDebufStatus(), 0, 0, 0, 0, 0);
                battleMovie.addElement(movie);

                if(restDamage > 0 && battleResult[5] != 0){
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);

                    movie = makeMovieSub(in.bsType, index, in.target.bsType, in.target.groupIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target.getDebufStatus(),
                                    battleResult[2], 0, 0, -restDamage, 0);
                    battleMovie.addElement(movie);
                }else{
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);
                }

                movie = makeMovieSub(src.bsType, src.groupIndex, src.bsType, src.groupIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_BACK, movieSpeed, HIT_HIT, src.getDebufStatus(), 0,
                                0, 0, 0, 0);
                battleMovie.addElement(movie);
            }

            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
        }else{
            in.target.changeHp(-battleResult[1], battleMovie, battleDataProcess);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target
                            .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
            battleMovie.addElement(movie);

            if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
            }

            if(in.bsType != BattleSprite.TYPE_INTERVENE && in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP){
                int[] bufInfo = in.getbufInfo();

                int hpInc = (bufInfo[3] * battleResult[1]) / 100;

                if(hpInc > 0){
                    in.changeHp(hpInc, battleMovie, battleDataProcess);

                    movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
                                    0, 0, hpInc, 0);
                    battleMovie.addElement(movie);
                }
            }
        }
        
        //命中时 增加属性攻伤害
        if(in.bsType != BattleSprite.TYPE_INTERVENE && battleResult[0] == Skill.HIT_HIT && in != null && in.target != null && !in.target.testCannotBattle()){
        	int vianyValue = Viany.getAttack(in.getVianyType(), in.getVianyAttack(), in.target.getVianyType(), in.target.getVianyDefense());
        	//缤纷连击 群鹰出击 伤害减半
        	if(vianyValue != 0 && skill != null && ((skill.id >= 1 && skill.id <= 8) || (skill.id >= 73 && skill.id <= 80))){
        		vianyValue >>= 1;
        	}
        	if(vianyValue > 0){
        		in.target.changeHp(-vianyValue, battleMovie, battleDataProcess);
        		int[] movie = makeMovieSub(in.bsType, in.groupIndex, in.target.bsType, in.targetIndex, Skill.SKILL_SUBMAGIC, Skill.ANIMATE_MGC_ATK, Skill.POSITION_STAY, Skill.OVER_POSITION_STAY, Skill.MOVIE_SPEED_NORMAL, Skill.HIT_HIT,
						in.target.getDebufStatus(), 0, 0, 0, -vianyValue, 0);
        		battleMovie.addElement(movie);
        	}else if(vianyValue < 0){
        		in.target.changeHp(-vianyValue, battleMovie, battleDataProcess);
        		int[] movie = BattleStrategy.makeMovieSub(in.bsType, in.groupIndex, in.target.bsType, in.targetIndex, Skill.SKILL_NONE, Skill.ANIMATE_INC_MGC, Skill.POSITION_STAY, Skill.OVER_POSITION_STAY, Skill.MOVIE_SPEED_NORMAL, Skill.HIT_HIT,
						in.target.getDebufStatus(), 0, 0, 0, -vianyValue, 0);
				battleMovie.addElement(movie);
        	}
        }

        return battleResult[0] == Skill.HIT_HIT;
    }

    protected void processMagic(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, int movieSpeed, int overPosition,
                    BattleDataProcess battleDataProcess){
        int[] battleResult = in.doBattle(BattleSprite.ACTION_MATTACK, our, ourPet, them, themPet);

        if(battleResult[3] == Skill.ATTACK_SORB && battleResult[0] == Skill.HIT_HIT && getPercentRate(battleResult[4])){
            int hpSorb = battleResult[1] * battleResult[5] / 100;

            if(hpSorb <= 0){
                hpSorb = 1;
            }

            in.target.changeHp(hpSorb, battleMovie, battleDataProcess);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_STAY, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
                            battleResult[2], 0, 0, 0, 0);
            battleMovie.addElement(movie);

            movie = makeMovieSub(in.target.bsType, in.targetIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                            .getDebufStatus(), battleResult[2], 0, 0, hpSorb, 0);
            battleMovie.addElement(movie);

            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
            }

            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_ANTI && battleResult[0] == Skill.HIT_HIT){
            int antiDamage = battleResult[1] * battleResult[4] / 100;

            if(antiDamage <= 0){
                antiDamage = -1;
            }else{
                antiDamage = -antiDamage;
            }

            int overPos = overPosition;

            in.changeHp(antiDamage, battleMovie, battleDataProcess);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_STAY, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                            .getDebufStatus(), battleResult[2], 0, 0, 0, 0);
            battleMovie.addElement(movie);
            if(in.bsType != BattleSprite.TYPE_INTERVENE){
	            if(in.testDie()){
	                overPos = OVER_POSITION_BACK;
	            }
	
	            movie = makeMovieSub(in.target.bsType, in.targetIndex, in.bsType, index, in.skill.id, ANIMATE_MGC_ATK, POSITION_STAY, overPos, movieSpeed, HIT_HIT, in.getDebufStatus(), battleResult[2], 0,
	                            0, antiDamage, 0);
	            battleMovie.addElement(movie);
            }
            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
            }

            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_PROCTECT && battleResult[0] == Skill.HIT_HIT){
            int protectDamage = battleResult[1] * (100 - battleResult[4]) / 100;
            int protectSrcType = battleResult[6];
            int protectSrcIndex = battleResult[7];

            BattleSprite src = battleDataProcess.getSprite(protectSrcType, protectSrcIndex);

            if(src.testCannotBattle()){ //保护人已死亡，清除保护状态，按正常攻击处理
                in.target.setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);

                in.target.changeHp(-battleResult[1], battleMovie, battleDataProcess);

                int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_STAY, overPosition, movieSpeed, battleResult[0], in.target
                                .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
                battleMovie.addElement(movie);

                if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                    in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                    processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
                }

                if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP){
                    int[] bufInfo = in.getbufInfo();

                    int hpInc = (bufInfo[3] * battleResult[1]) / 100;

                    if(hpInc > 0){
                        in.changeHp(hpInc, battleMovie, battleDataProcess);

                        movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
                                        0, 0, hpInc, 0);
                        battleMovie.addElement(movie);
                    }
                }
            }else{
                if(protectDamage <= 0){
                    protectDamage = 1;
                }

                int restDamage = battleResult[1] - protectDamage;

                if(restDamage < 0){
                    restDamage = 0;
                }

                src.changeHp(-protectDamage, battleMovie, battleDataProcess);

                if(restDamage > 0 && battleResult[5] != 0){
                    in.target.changeHp(-restDamage, battleMovie, battleDataProcess);
                }

                int[] movie = makeMovieSub(src.bsType, src.groupIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                                .getDebufStatus(), 0, 0, 0, 0, 0);
                battleMovie.addElement(movie);

                if(restDamage > 0 && battleResult[5] != 0){
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);

                    movie = makeMovieSub(in.bsType, index, in.target.bsType, in.target.groupIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target.getDebufStatus(),
                                    battleResult[2], 0, 0, -restDamage, 0);
                    battleMovie.addElement(movie);
                }else{
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);
                }

                movie = makeMovieSub(src.bsType, src.groupIndex, src.bsType, src.groupIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_BACK, movieSpeed, HIT_HIT, src.getDebufStatus(), 0,
                                0, 0, 0, 0);
                battleMovie.addElement(movie);
            }

            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_PET_DAMAGE_TO_MP && battleResult[0] == Skill.HIT_HIT){
            int mpDamage = battleResult[1] * battleResult[4] / 100;

            if(mpDamage <= 0){
                mpDamage = 1;
            }

            if(mpDamage > in.target.mp){
                mpDamage = in.target.mp;
            }

            int hpDamage = battleResult[1] - mpDamage;

            if(hpDamage < 0){
                hpDamage = 0;
            }

            in.target.changeMp(-mpDamage);
            in.target.changeHp(-hpDamage, battleMovie, battleDataProcess);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_DEST, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
                            battleResult[2], 0, 0, -hpDamage, -mpDamage);
            battleMovie.addElement(movie);

            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
            }

            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_MASTER_PROCTECT && battleResult[0] == Skill.HIT_HIT){
            int protectDamage = battleResult[1] * battleResult[4] / 100;
            int protectSrcType = battleResult[6];
            int protectSrcIndex = battleResult[7];

            BattleSprite src = battleDataProcess.getSprite(protectSrcType, protectSrcIndex);

            if(src.testCannotBattle()){ //保护人已死亡，清除保护状态，按正常攻击处理
                in.target.setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);

                in.target.changeHp(-battleResult[1], battleMovie, battleDataProcess);

                int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_STAY, overPosition, movieSpeed, battleResult[0], in.target
                                .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
                battleMovie.addElement(movie);

                if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                    in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                    processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
                }

                if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP){
                    int[] bufInfo = in.getbufInfo();

                    int hpInc = (bufInfo[3] * battleResult[1]) / 100;

                    if(hpInc > 0){
                        in.changeHp(hpInc, battleMovie, battleDataProcess);

                        movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
                                        0, 0, hpInc, 0);
                        battleMovie.addElement(movie);
                    }
                }
            }else{
                if(protectDamage <= 0){
                    protectDamage = 1;
                }

                int restDamage = battleResult[1] - protectDamage;

                if(restDamage < 0){
                    restDamage = 0;
                }

                src.changeHp(-protectDamage, battleMovie, battleDataProcess);

                if(restDamage > 0 && battleResult[5] != 0){
                    in.target.changeHp(-restDamage, battleMovie, battleDataProcess);
                }

                int[] movie = makeMovieSub(src.bsType, src.groupIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                                .getDebufStatus(), 0, 0, 0, 0, 0);
                battleMovie.addElement(movie);

                if(restDamage > 0 && battleResult[5] != 0){
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);

                    movie = makeMovieSub(in.bsType, index, in.target.bsType, in.target.groupIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target.getDebufStatus(),
                                    battleResult[2], 0, 0, -restDamage, 0);
                    battleMovie.addElement(movie);
                }else{
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);
                }

                movie = makeMovieSub(src.bsType, src.groupIndex, src.bsType, src.groupIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_BACK, movieSpeed, HIT_HIT, src.getDebufStatus(), 0,
                                0, 0, 0, 0);
                battleMovie.addElement(movie);
            }

            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
        }else{
            in.target.changeHp(-battleResult[1], battleMovie, battleDataProcess);
            
            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_STAY, overPosition, movieSpeed, battleResult[0], in.target
                            .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
            battleMovie.addElement(movie);

            if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
            }

            if(in.bsType != BattleSprite.TYPE_INTERVENE && in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP && battleResult[0] == Skill.HIT_HIT){
                int[] bufInfo = in.getbufInfo();

                int hpInc = (bufInfo[3] * battleResult[1]) / 100;

                if(hpInc > 0){
                    in.changeHp(hpInc, battleMovie, battleDataProcess);

                    movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
                                    0, 0, hpInc, 0);
                    battleMovie.addElement(movie);
                }
            }
        }
        
        //命中时 增加属性攻伤害
        if(in.bsType != BattleSprite.TYPE_INTERVENE && battleResult[0] == Skill.HIT_HIT && in != null && in.target != null && !in.target.testCannotBattle()){
        	int vianyValue = Viany.getAttack(in.getVianyType(), in.getVianyAttack(), in.target.getVianyType(), in.target.getVianyDefense());
        	//缤纷连击 群鹰出击 伤害减半
        	if(vianyValue != 0 && skill != null && ((skill.id >= 1 && skill.id <= 8) || (skill.id >= 73 && skill.id <= 80))){
        		vianyValue >>= 1;
        	}
        	if(vianyValue > 0){
        		in.target.changeHp(-vianyValue, battleMovie, battleDataProcess);
        		int[] movie = makeMovieSub(in.bsType, in.groupIndex, in.target.bsType, in.targetIndex, Skill.SKILL_SUBMAGIC, Skill.ANIMATE_MGC_ATK, Skill.POSITION_STAY, Skill.OVER_POSITION_STAY, Skill.MOVIE_SPEED_NORMAL, Skill.HIT_HIT,
						in.target.getDebufStatus(), 0, 0, 0, -vianyValue, 0);
        		battleMovie.addElement(movie);
        	}else if(vianyValue < 0){
        		in.target.changeHp(-vianyValue, battleMovie, battleDataProcess);
        		int[] movie = BattleStrategy.makeMovieSub(in.bsType, in.groupIndex, in.target.bsType, in.targetIndex, Skill.SKILL_NONE, Skill.ANIMATE_INC_MGC, Skill.POSITION_STAY, Skill.OVER_POSITION_STAY, Skill.MOVIE_SPEED_NORMAL, Skill.HIT_HIT,
						in.target.getDebufStatus(), 0, 0, 0, -vianyValue, 0);
				battleMovie.addElement(movie);
        	}
        }
        
    }

    public void processEmptyLoop(BattleSprite in, int index, Vector battleMovie, BattleDataProcess battleDataProcess){
        int[] movie = makeMovieSub(in.bsType, index, in.bsType, index, Skill.SKILL_NONE, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.getDebufStatus(),
                        ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
    }

    public void processStatusUpdate(BattleSprite in, int index, Vector battleMovie, BattleDataProcess battleDataProcess){
        int[] movie = makeMovieSub(in.bsType, index, in.bsType, index, Skill.SKILL_UPDATE_STATUS, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.getDebufStatus(),
                        ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
    }

    protected void processStatusAttack(BattleSprite in, int index, int hit, Vector battleMovie, BattleDataProcess battleDataProcess){
        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_STS_ATK, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, hit, in.target
                        .getDebufStatus(), ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
        movie = makeMovieSub(in.bsType, index, in.bsType, index, Skill.SKILL_UPDATE_STATUS, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.getDebufStatus(),
                        ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
    }

    protected void processUnStatus(BattleSprite in, int index, int hit, Vector battleMovie, BattleDataProcess battleDataProcess){
        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_UNS_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, hit, in.target
                        .getDebufStatus(), ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
        movie = makeMovieSub(in.bsType, index, in.bsType, index, Skill.SKILL_UPDATE_STATUS, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.getDebufStatus(),
                        ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
    }

    protected void processRestore(BattleSprite in, int index, int hpInc, int mpInc, int cri, Vector battleMovie, BattleDataProcess battleDataProcess){
        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.target
                        .getDebufStatus(), cri, 0, 0, hpInc, mpInc);
        battleMovie.addElement(movie);
        movie = makeMovieSub(in.bsType, index, in.bsType, index, Skill.SKILL_UPDATE_STATUS, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.getDebufStatus(),
                        ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
    }

    public void processSaveLifeMovie(BattleSprite in, int index, int hpInc, int cri, Vector battleMovie, BattleDataProcess battleDataProcess){
        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_SAV_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.target
                        .getDebufStatus(), cri, 0, 0, hpInc, 0);
        battleMovie.addElement(movie);
        movie = makeMovieSub(in.bsType, index, in.bsType, index, Skill.SKILL_UPDATE_STATUS, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.getDebufStatus(),
                        ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
    }

    protected boolean processMultiAttack(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, BattleDataProcess battleDataProcess){
        int times = skill.parm1;
        int type = skill.parm2;
        BattleSprite targetSprite;
        boolean win = false;

        //in.setSkill(Skill.NONE_SKILL);

        for(int i = 0; i < times; i++){
            targetSprite = selectTargetRandom(in, them, themPet);	//获取目标

            if(targetSprite != null){
                if(type == Skill.MULTI_ATTACK_RANDOM){
                    in.setTarget(targetSprite, targetSprite.groupIndex);
                }else if(in.target.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie, battleDataProcess);

                    return win;
                }

                if(in.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie, battleDataProcess);

                    return win;
                }

                if(i == times - 1){
                    processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
                }else{
                    processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY, battleDataProcess);
                }
            }else{
                processEmptyLoop(in, index, battleMovie, battleDataProcess);

                win = true;

                break;
            }
        }

        return win;
    }

    public void addBattleRecorder(Vector battleRecorders, BattleRecorder recorder){
        battleRecorders.add(recorder);
    }

    public void addBattleRecorder(Vector battleRecorders, BattleSprite bs, Skill skill){
        BattleRecorder recorder = new BattleRecorder(bs, skill);
        recorder.addDest(bs.target);
    }

    protected boolean processIncreaseAttackIncreaseDamage(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, skill.parm2, skill.parm2, 0, 0, skill.effect);
            processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
            addBattleRecorder(battleRecorders, in, skill);
        }

        return win;
    }

    protected boolean processAntiBufIncreaseAttack(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            if(in.target.HasBuf()){
                in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
            }else{
                in.AddAttrBuf(skill.effectBout, skill.parm2 - 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
            }

            processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
            addBattleRecorder(battleRecorders, in, skill);
        }

        return win;
    }

    protected boolean processFaintAttack(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
        	boolean hit = processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
        	if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && (in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !in.target.isImmuneControl()) && in.target.testControlHit()){
	            if(hit){
	                if(getPercentRate(skill.hitRate)){
	                    in.target.setDeBufStatus(skill.effectBout, STATUS_FAINT, skill.level, 0, 0, in.bsType, in.groupIndex);
	                    processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
	                }
	            }
        	}
            addBattleRecorder(battleRecorders, in, skill);
        }

        return win;
    }

    protected boolean processIncreaseAttackIncreaseCri(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, skill.parm2, 0, 0, 0, 0, skill.effect);
            processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
            addBattleRecorder(battleRecorders, in, skill);
        }

        return win;
    }

    protected boolean processIncreaseDamageDecreaseHit(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, -skill.parm2, 0, 0, 0, 0, 0, 0, skill.effect);
            processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
            addBattleRecorder(battleRecorders, in, skill);
        }

        return win;
    }

    protected boolean processIncreaseAttackStop(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
            processAttack(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
            in.setDeBufStatus(skill.effectBout, Skill.STATUS_STOP, 0, 0, 0, in.bsType, in.groupIndex);
            processStatusUpdate(in, index, battleMovie, battleDataProcess);
            addBattleRecorder(battleRecorders, in, skill);
        }

        return win;
    }

    protected boolean processAlwaysIncreaseAttackIncreaseDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
        in.setDeBufStatus(skill.effectBout, Skill.STATUS_POISON, skill.level, skill.parm2 * in.attributes[BattleSprite.ATTR_HPMAX] / 100, 0, in.bsType, in.groupIndex);
        in.setTarget(in, in.groupIndex);
//        processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, skill.parm2, skill.parm2, 0, 0, skill.effect);
//        }
//
//        addBattleRecorder(battleRecorders, in, skill);

        return win;
    }

    protected boolean processMagicAttack(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, 0, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
            processMagic(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
            addBattleRecorder(battleRecorders, in, skill);
        }

        return win;
    }

    protected boolean processMultiMagic(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        int times = skill.parm1;
        int type = skill.parm2;
        BattleSprite targetSprite;
        boolean win = false;

        //in.setSkill(Skill.NONE_SKILL);

        for(int i = 0; i < times; i++){
            targetSprite = selectTargetRandom(in, them, themPet);

            if(targetSprite != null){
                if(type == Skill.MULTI_ATTACK_RANDOM){
                    in.setTarget(targetSprite, targetSprite.groupIndex);
                }else if(in.target.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie, battleDataProcess);

                    return win;
                }

                if(in.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie, battleDataProcess);

                    return win;
                }

                if(i == times - 1){
                    processMagic(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
                }else{
                    processMagic(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY, battleDataProcess);
                }
            }else{
                win = true;

                break;
            }
        }

        return win;
    }

    protected boolean processLetPoison(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);
        int hpDec = in.level * skill.parm1 / 100;
        if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && (in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !in.target.isImmuneControl())){
            in.target.setDeBufStatus(skill.effectBout, STATUS_POISON, skill.level, hpDec, 0, in.bsType, in.groupIndex);
            processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
        }
        addBattleRecorder(battleRecorders, in, skill);
        return win;
    }

    protected boolean processLetSleep(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(getPercentRate(skill.hitRate)){
            if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && (in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !in.target.isImmuneControl()) && in.target.testControlHit()){
                in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_SLEEP, skill.level, 0, 0, in.bsType, in.groupIndex);
                processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
                
                in.target.controlHit();
            }else{
                processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
            }
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
        }
        addBattleRecorder(battleRecorders, in, skill);
        return win;
    }

    protected boolean processLetStone(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(getPercentRate(skill.hitRate)){
            if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && (in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !in.target.isImmuneControl()) && in.target.testControlHit()){
                in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_STONE, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);
                processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
                
                in.target.controlHit();
            }else{
                processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
            }
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
        }
        addBattleRecorder(battleRecorders, in, skill);
        return win;
    }

    protected boolean processMagicAll(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        in.AddAttrBuf(skill.effectBout, 0, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);

        for(int i = 0; i < them.length; i++){
            if(them[i] != null && !them[i].testCannotBattle()){
                in.setTarget(them[i], them[i].groupIndex);
                processMagic(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
            }
        }

        win = testTargetRandom(in, them, themPet);

        if(win){
            return win;
        }

        for(int i = 0; i < themPet.length; i++){
            if(themPet[i] != null && !themPet[i].testCannotBattle()){
                in.setTarget(themPet[i], themPet[i].groupIndex);
                processMagic(in, index, null, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
            }
        }

        addBattleRecorder(battleRecorders, in, skill);
        return win;
    }

    protected boolean processLetConfuse(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(getPercentRate(skill.hitRate)){
            if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && (in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !in.target.isImmuneControl()) && in.target.testControlHit()){
                in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_CONFUSE, skill.level, 0, 0, in.bsType, in.groupIndex);
                processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
                
                in.target.controlHit();
            }else{
                processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
            }
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
        }
        
        addBattleRecorder(battleRecorders, in, skill);
        return win;
    }

    protected boolean processIncreaseMagicLetPosion(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        in.AddAttrBuf(skill.effectBout, 0, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
        in.setDeBufStatus(skill.effectBout, Skill.STATUS_POISON, skill.level, skill.parm2 * in.attributes[BattleSprite.ATTR_HPMAX] / 100, 0, in.bsType, in.groupIndex);
        in.setTarget(in, in.groupIndex);
//        processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return win;
    }

    protected boolean processMagicUseAllMp(BattleSprite in, int index, Skill skill, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            if(!getPercentRate(skill.hitRate)){
                in.AddAttrBuf(skill.effectBout, 0, -100, 0, 0, -100, 0, 0, 0, 0, 0, skill.parm1 * in.usedMp / 100, skill.effect);
            }else{
                in.AddAttrBuf(skill.effectBout, 0, -100, 0, 0, 0, 0, 0, 0, 0, 0, skill.parm1 * in.usedMp / 100, skill.effect);
            }

            processMagic(in, index, skill, our, ourPet, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
        }

        addBattleRecorder(battleRecorders, in, skill);

        return win;
    }

    protected boolean processDecreaseAttackIncreaseFlee(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.setTarget(in, in.groupIndex);
        in.target.AddAttrBuf(skill.effectBout, skill.parm1, skill.parm1, 0, 0, 0, skill.parm2, 0, 0, 0, 0, 0, skill.effect);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
        addBattleRecorder(battleRecorders, in, skill);
        return false;
    }

    protected boolean processBlockAttackDecreaseDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_PROTECTED, skill.level, skill.parm1, 1, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);
        return false;
    }

    protected boolean processDecreasePhyDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, 0, 0, 0, skill.effect);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);
        return false;
    }

    protected boolean processDecreaseMagicDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, 0, 0, skill.effect);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);
        return false;
    }

    protected boolean processDecreasePhyMagicDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, -skill.parm1, 0, 0, skill.effect);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);
        return false;
    }

    protected boolean processSorbPhyDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.setTarget(in, in.groupIndex);
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_SORB_ATTACK, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);

        addBattleRecorder(battleRecorders, in, skill);
        return false;
    }

    protected boolean processSorbMagicDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.setTarget(in, in.groupIndex);
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_SORB_MAGIC, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);

        addBattleRecorder(battleRecorders, in, skill);
        return false;
    }

    protected boolean processRestoreHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        int hpAdd = skill.parm1;

        if(in.testMCri()){
            hpAdd *= BattleSprite.CRI_RATE;
            processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie, battleDataProcess);
        }else{
            processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
        }

        in.target.changeHp(hpAdd, battleMovie, battleDataProcess);

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    protected boolean processRestoreAllHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        for(int i = 0; i < them.length; i++){
            if(them[i] != null && !them[i].testCannotBattle()){
                int hpAdd = skill.parm1;

                in.setTarget(them[i], i);

                if(in.testMCri()){
                    hpAdd *= BattleSprite.CRI_RATE;
                    processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie, battleDataProcess);
                }else{
                    processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
                }

                them[i].changeHp(hpAdd, battleMovie, battleDataProcess);
            }
        }

        for(int i = 0; i < themPet.length; i++){
            if(themPet[i] != null && !themPet[i].testCannotBattle()){
                int hpAdd = skill.parm1;

                in.setTarget(themPet[i], i);

                if(in.testMCri()){
                    hpAdd *= BattleSprite.CRI_RATE;
                    processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie, battleDataProcess);
                }else{
                    processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
                }

                themPet[i].changeHp(hpAdd, battleMovie, battleDataProcess);
            }
        }

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    protected boolean processSaveLife(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        if(!in.target.testCannotBattle()){
            processUnStatus(in, index, HIT_MISS, battleMovie, battleDataProcess);
        }else{
            in.target.reLive();

            int hpAdd = skill.parm1;

            if(in.testMCri()){
                hpAdd *= BattleSprite.CRI_RATE;
                processSaveLifeMovie(in, index, hpAdd, ATTACK_CRI, battleMovie, battleDataProcess);
            }else{
                processSaveLifeMovie(in, index, hpAdd, ATTACK_NO_CRI, battleMovie, battleDataProcess);
            }

            in.target.changeHp(hpAdd, battleMovie, battleDataProcess);
        }

        addBattleRecorder(battleRecorders, in, skill);

        return false;
    }

    protected boolean processAntiPhy(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_ATTACK, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
        addBattleRecorder(battleRecorders, in, skill);
        return false;
    }

    protected boolean processAntiMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_MAGIC, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
        addBattleRecorder(battleRecorders, in, skill);
        return false;
    }

    protected boolean processRestoreLotHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        int hpAdd = skill.parm1 * in.level / 100;

        if(in.testMCri()){
            hpAdd *= BattleSprite.CRI_RATE;
            processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie, battleDataProcess);
        }else{
            processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
        }

        in.target.changeHp(hpAdd, battleMovie, battleDataProcess);
        addBattleRecorder(battleRecorders, in, skill);
        return false;
    }

    protected boolean processClearStatusAndAnti(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
                    BattleDataProcess battleDataProcess){
        if(!in.target.testCannotBattle() && in.target.getDebufStatus() != STATUS_NORMAL){
            in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
            processUnStatus(in, index, HIT_HIT, battleMovie, battleDataProcess);
        }

        if(skill.parm1 > 0 && getPercentRate(skill.parm1)){
            in.target.setBufStatus(skill.parm2, Skill.STATUS_IMMUNITY_STATUS, 1, 0, 0, in.bsType, in.groupIndex);
        }
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
        addBattleRecorder(battleRecorders, in, skill);
        return false;
    }

    protected BattleSprite selectTargetRandom(BattleSprite src, BattleSprite[] them, BattleSprite[] themPet){
        int idx = -1;
        boolean allDie = true;
        BattleSprite result = null;
        BattleSprite aa = null;
        for(int i = 0; i < them.length; i++){
            if(them[i] == null){
                continue;				//continue使用，若them是null,结束本次次循环，不走下面的代码，继续下次循环。
            }

            if(!them[i].testCannotBattle()){
                allDie = false;
                aa = them[i];
                break;					//break,找到一可以战斗的，就跳出循环，不在循环，即不在找了
            }
        }

        while(!allDie){
            result = null;
            					//TYPE_MONSTER 说明aa是怪物，
            if(aa.bsType == BattleSprite.TYPE_MONSTER || src.getDebufStatus() == Skill.STATUS_CONFUSE){
                if(random(0, 100) < 50){
                    idx = random(0, them.length - 1);
                    result = them[idx];
                }else{
                    idx = random(0, themPet.length - 1);
                    result = themPet[idx];
                }

                if(result == null){
                    continue;				
                }

                if(!result.testCannotBattle()){
                    break;
                }
            }else{
                int max = 0;
                int tmp = 0;
                
                for(int i = 0; i < them.length; i++){
                    if(!them[i].testCannotBattle()){
                        int e = them[i].getEnmity(src);

                        if(e > max){
                            tmp = 1;
                            max = e;
                            idx = i;
                        }else if(e == max&&Utils.hit(randGen,100 / (tmp + 1),100)){
                            tmp++;
                            idx = i;
                        }
                    }
                }
                
                if(themPet[idx] != null && !themPet[idx].testCannotBattle() && random(0, 100) < 50){
                    return themPet[idx];
                }else{
                    return them[idx];
                }
            }
        }

        return result;
    }

    static public int[] makeMovieSub(int srcType, int srcIndex, int dstType, int dstIndex, int skillId, int movieType, int posistion, int overPosition, int movieSpeed, int hit, int status, int cri,
                    int hpInc, int mpInc, int tagetHpInc, int tagetMpInc){
        int[] movie = new int[16];

        movie[0] = srcType;
        movie[1] = srcIndex;
        movie[2] = dstType;
        movie[3] = dstIndex;
        movie[4] = skillId;
        movie[5] = movieType;
        movie[6] = posistion;
        movie[7] = overPosition;
        movie[8] = movieSpeed;
        movie[9] = hit;
        movie[10] = status;
        movie[11] = cri;
        movie[12] = hpInc;
        movie[13] = mpInc;
        movie[14] = tagetHpInc;
        movie[15] = tagetMpInc;

        return movie;
    }

    public boolean chooseSkill(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie,
                    BattleDataProcess battleDataProcess,int round){
        if(!bs.canAction()){ //混乱状态特殊处理
            if(bs.getDebufStatus() == Skill.STATUS_CONFUSE){
                BattleSprite targetOur = selectTargetRandom(bs, our, ourPet);
                BattleSprite targetThem = selectTargetRandom(bs, them, themPet);

                if(targetOur != null && targetThem != null){
                    bs.setSkill(Skill.ATTACK_SKILL);

                    int ourCount = 0;
                    int themCount = 0;

                    for(int i = 0; i < our.length; i++){
                        if(our[i] != null && !our[i].testCannotBattle()){
                            ourCount++;
                        }
                    }

                    for(int i = 0; i < ourPet.length; i++){
                        if(ourPet[i] != null && !ourPet[i].testCannotBattle()){
                            ourCount++;
                        }
                    }

                    for(int i = 0; i < them.length; i++){
                        if(them[i] != null && !them[i].testCannotBattle()){
                            themCount++;
                        }
                    }

                    for(int i = 0; i < themPet.length; i++){
                        if(themPet[i] != null && !themPet[i].testCannotBattle()){
                            themCount++;
                        }
                    }

                    int tmp = ourCount * 100 / (ourCount + themCount);

                    if(getPercentRate(tmp)){
                        setTargetRandom(bs, our, ourPet);
                    }else{
                        setTargetRandom(bs, them, themPet);
                    }

                    return false;
                }else{
                    return true;
                }
            }else{
                if(selectTargetRandom(bs, them, themPet) == null){
                    return true;
                }else{
                    bs.setSkill(Skill.STAY_SKILL);

                    return false;
                }
            }
        }
        boolean solidSkill = true;
           boolean win = false;
        if(bs.ai!=null)
            win = bs.ai.action(bs,index,our,them,ourPet,themPet,battleMovie, battleDataProcess, round);
        else{
            while (true) {
                int skillIndex = random(Skill.SOLID_SKILL_BEGIN,
                                        bs.skillList.length - 1);

                if (skillIndex >= 0) {
                    int[] skillStatus = Skill.getSkillStatus(bs,
                            bs.skillList[skillIndex]);

                    if (skillStatus[0] == Skill.CANNOT_SELECT_SKILL) {
                        continue;
                    }

                    bs.setSkill(Skill.getSkill(bs.skillList[skillIndex]));
                } else {
                    bs.setSkill(Skill.getSkill(skillIndex));
                }

                break;
            }
//


            switch (bs.skill.id) {
                case SKILL_ATTACK:
                    win = setTargetRandom(bs, them, themPet);

                    break;
                case SKILL_ITEM:
                    win = setTargetRandom(bs, them, themPet);

                    break;
                case SKILL_RUN:
                    bs.setTarget(null, -1);

                    break;
                case SKILL_STAY:
                    bs.setTarget(null, -1);

                    break;
                default:
                    solidSkill = false;

                    break;
            }

            if (solidSkill || win) {
                return win;
            }
        }

        Skill skill = bs.skill;

        switch(skill.effect){
            case Skill.EFFECT_MULTI_ATK:
                if(skill.parm2 == Skill.MULTI_ATTACK_RANDOM){
                    bs.setTarget(null, -1);
                }else{
                    win = setTargetRandom(bs, them, themPet);
                }

                break;
            case EFFECT_INC_ATK_INC_DMG:
            case EFFECT_ANTI_BUF_INC_ATK:
            case EFFECT_FAINT:
            case EFFECT_INC_ATK_INC_CRI:
            case EFFECT_INC_DMG_DEC_HIT:
            case EFFECT_INC_ATK_STOP:
                win = setTargetRandom(bs, them, themPet);

                break;
            case EFFECT_ALWAYS_INC_ATK_INC_DMG:
                bs.setTarget(null, -1);

                break;
            case EFFECT_MGC_ATK:
                win = setTargetRandom(bs, them, themPet);

                break;
            case EFFECT_MULTI_MGC:
                if(skill.parm2 == Skill.MULTI_ATTACK_RANDOM){
                    bs.setTarget(null, -1);
                }else{
                    win = setTargetRandom(bs, them, themPet);
                }

                break;
            case EFFECT_LET_POISON:
            case EFFECT_LET_SLEEP:
            case EFFECT_LET_STONE:
            case EFFECT_MAGIC_ALL:
            case EFFECT_LET_CONFUSE:
                win = setTargetRandom(bs, them, themPet);

                break;
            case EFFECT_INC_MGC_LET_POSION:
            case EFFECT_MGC_USE_ALL_MP:
                bs.setTarget(null, -1);

                break;
            case EFFECT_DEC_ATK_INC_FLEE:
            case EFFECT_BLOCK_ATK_DEC_DMG:
            case EFFECT_DEC_PHY_DMG:
            case EFFECT_DEC_MGC_DMG:
            case EFFECT_DEC_PHY_MGC_DMG:
            case EFFECT_PHY_DMG_TO_HP:
            case EFFECT_MGC_DMG_TO_HP:
            case EFFECT_RESTORE_HP:
                setTargetRandom(bs, our, ourPet);

                break;
            case EFFECT_RESTORE_ALL_HP:
                bs.setTarget(null, -1);

                break;
            case EFFECT_SAVE_LIFE:
                setTargetRandom(bs, our, ourPet);

                break;
            case EFFECT_ANTI_PHY:
            case EFFECT_ANTI_MGC:
            case EFFECT_RESTORE_LOT_HP:
            case EFFECT_CLEAR_STS_AND_ANTI:
                setTargetRandom(bs, our, ourPet);

                break;
        }

        return win;
    }

    public boolean doPoisonFrost(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie,
                    BattleDataProcess battleDataProcess){
        int[] movie;

        if(bs.getDebufStatus() == Skill.STATUS_POISON){
            int[] bufInfo = bs.getDebufInfo();

            int protectSrcType = bufInfo[5];
            int protectSrcIndex = bufInfo[6];

            BattleSprite src = battleDataProcess.getSprite(protectSrcType, protectSrcIndex);

            int hpDec = bufInfo[3];

            if(hpDec <= 0){
                hpDec = -1;
            }else{
                hpDec = -hpDec;
            }

            bs.changeHp(hpDec, battleMovie, battleDataProcess);
            movie = makeMovieSub(bs.bsType, index, bs.bsType, index, SKILL_LIFE_MAGIC, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, bs.getDebufStatus(), ATTACK_NO_CRI,
                            hpDec, 0, 0, 0);
            battleMovie.addElement(movie);

            if(bs.testDie()){
                if(selectTargetRandom(bs, our, ourPet) == null){
                    return true;
                }else{
                    return false;
                }
            }
        }else if(bs.getDebufStatus() == Skill.STATUS_FROST){
            int[] bufInfo = bs.getDebufInfo();

            int protectSrcType = bufInfo[5];
            int protectSrcIndex = bufInfo[6];

            BattleSprite src = battleDataProcess.getSprite(protectSrcType, protectSrcIndex);

//            int mpDec = src.level * bufInfo[3] / 100;
            int mpDec = bufInfo[3];

            if(mpDec <= 0){
                mpDec = -1;
            }else{
                mpDec = -mpDec;
            }

            bs.changeMp(mpDec);
            movie = makeMovieSub(bs.bsType, index, bs.bsType, index, SKILL_LIFE_MAGIC, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, bs.getDebufStatus(), ATTACK_NO_CRI,
                            0, mpDec, 0, 0);
            battleMovie.addElement(movie);
        }

        //判断是否战斗已结束
        if(selectTargetRandom(bs, them, themPet) == null){
            return true;
        }

        return false;
    }

    protected  boolean setTargetRandom(BattleSprite bs, BattleSprite[] them, BattleSprite[] themPet){
        boolean win = false;
        BattleSprite result = selectTargetRandom(bs, them, themPet);

        if(result != null){
            if(bs.target == null){
                bs.setTarget(result, result.groupIndex);
            }
        }else{
            win = true;
        }

        return win;
    }

    protected boolean testTargetRandom(BattleSprite bs, BattleSprite[] them, BattleSprite[] themPet){
        boolean win = false;
        BattleSprite tmpSprite = selectTargetRandom(bs, them, themPet);

        if(tmpSprite == null){
            win = true;
        }

        return win;
    }

    public static String getSkillName(BattleSprite bs, int skillId, byte showMpUseType, boolean showLevel){
        Skill skill = Skill.getSkill(skillId);
        String result;

        if(skill == null){

            int idx = -skillId - 1;

            if(idx < 0 || idx >= solidSkillName.length){
                return "";
            }

            result = solidSkillName[-skillId - 1];

        }else{
            result = skill.name;

            if(showLevel&&skill.level!=0){
                result += skill.level;
            }

            if(bs != null){
                switch(showMpUseType){
                    case SHOW_MPUSE_LIST:
                        result += " " + skill.getMpUse(bs) + "MP";

                        break;
                    case SHOW_MPUSE_DESC:
                        result += "\n消耗魔法：" + skill.getMpUse(bs) + "MP";

                        break;
                }
            }
        }

        return result;
    }

    public static String getAnimateName(int animateType){
        return animateName[animateType];
    }

    public static String getStatusName(int status){
        return statusName[status];
    }

    /**
     * int[4] [0]是否可选 [1]冷却回合 [2]魔法不足 [3]目标选择
     */
    public int[] getSkillStatus(BattleSprite bs, int skillId){
        int[] status = new int[4];
        status[0] = Skill.CAN_SELECT_SKILL;

        if(!bs.canAction()){
            status[0] = Skill.CANNOT_SELECT_SKILL;
            status[1] = 0;
            status[2] = 0;
            status[3] = Skill.CHOOSE_NONE;

            return status;
        }

        status[3] = Skill.CHOOSE_NONE;

        boolean solidSkill = true;

        switch(skillId){
            case SKILL_ATTACK:
                status[3] = Skill.CHOOSE_ENEMY;

                break;
            case SKILL_ITEM:
                status[3] = Skill.CHOOSE_FRIEND;

                break;
            case SKILL_RUN:
                status[3] = Skill.CHOOSE_NONE;

                break;
            case SKILL_STAY:
                status[3] = Skill.CHOOSE_NONE;

                break;
            case SKILL_CATCH:
                status[3] = Skill.CHOOSE_ENEMY;

                break;
            default:
                solidSkill = false;

                break;
        }

        if(solidSkill){
            status[1] = 0;
            status[2] = 0;

            return status;
        }

        Skill skill = Skill.getSkill(skillId);

        int cdBout = bs.testCoolDown(skillId);

        if(cdBout > 0){
            status[1] = cdBout;
            status[3] = Skill.CHOOSE_NONE;
        }else{
            status[1] = 0;
        }

        int mpGap = 0;
        short mpuse_tmp=skill.getMpUse(bs);
        if(mpuse_tmp > bs.mp){
            mpGap = mpuse_tmp - bs.mp;
            status[2] = mpGap;
            status[3] = Skill.CHOOSE_NONE;

            return status;
        }

        switch(skill.effect){
            case Skill.EFFECT_MULTI_ATK:
                if(skill.parm2 == Skill.MULTI_ATTACK_RANDOM){
                    status[3] = Skill.CHOOSE_NONE;
                }else{
                    status[3] = Skill.CHOOSE_ENEMY;
                }

                break;
            case EFFECT_INC_ATK_INC_DMG:
            case EFFECT_ANTI_BUF_INC_ATK:
            case EFFECT_FAINT:
            case EFFECT_INC_ATK_INC_CRI:
            case EFFECT_INC_DMG_DEC_HIT:
            case EFFECT_INC_ATK_STOP:
                status[3] = Skill.CHOOSE_ENEMY;

                break;
            case EFFECT_ALWAYS_INC_ATK_INC_DMG:
                status[3] = Skill.CHOOSE_NONE;

                break;
            case EFFECT_MGC_ATK:
                status[3] = Skill.CHOOSE_ENEMY;

                break;
            case EFFECT_MULTI_MGC:
                if(skill.parm2 == Skill.MULTI_ATTACK_RANDOM){
                    status[3] = Skill.CHOOSE_NONE;
                }else{
                    status[3] = Skill.CHOOSE_ENEMY;
                }

                break;
            case EFFECT_LET_POISON:
            case EFFECT_LET_SLEEP:
            case EFFECT_LET_STONE:
                status[3] = Skill.CHOOSE_ENEMY;

                break;
            case EFFECT_MAGIC_ALL:
                status[3] = Skill.CHOOSE_NONE;

                break;
            case EFFECT_LET_CONFUSE:
                status[3] = Skill.CHOOSE_ENEMY;

                break;
            case EFFECT_INC_MGC_LET_POSION:
                status[3] = Skill.CHOOSE_NONE;

                break;
            case EFFECT_MGC_USE_ALL_MP:
                status[3] = Skill.CHOOSE_ENEMY;

                break;
            case EFFECT_DEC_ATK_INC_FLEE:
                status[3] = Skill.CHOOSE_NONE;

                break;
            case EFFECT_BLOCK_ATK_DEC_DMG:
            case EFFECT_DEC_PHY_DMG:
            case EFFECT_DEC_MGC_DMG:
            case EFFECT_DEC_PHY_MGC_DMG:
                status[3] = Skill.CHOOSE_FRIEND;

                break;
            case EFFECT_PHY_DMG_TO_HP:
            case EFFECT_MGC_DMG_TO_HP:
                status[3] = Skill.CHOOSE_NONE;

                break;
            case EFFECT_RESTORE_HP:
                status[3] = Skill.CHOOSE_FRIEND;

                break;
            case EFFECT_RESTORE_ALL_HP:
                status[3] = Skill.CHOOSE_NONE;

                break;
            case EFFECT_SAVE_LIFE:
                status[3] = Skill.CHOOSE_FRIEND_ALL;

                break;
            case EFFECT_ANTI_PHY:
            case EFFECT_ANTI_MGC:
            case EFFECT_RESTORE_LOT_HP:
            case EFFECT_CLEAR_STS_AND_ANTI:
                status[3] = Skill.CHOOSE_FRIEND;

                break;
            case EFFECT_PET_2_ATTACK:
            case EFFECT_PET_2_ATTACK2:
            case EFFECT_PET_ADD_DMG_ADD_CRI_PHY:
            case EFFECT_PET_ADD_DMG_ADD_CRI_PHY2:
            case EFFECT_PET_ADD_DMG_ADD_ATTACK:
            case EFFECT_PET_ADD_DMG_DEC_HIT:
            case EFFECT_PET_ADD_DMG_DEC_HIT2:
            case EFFECT_PET_LET_POISON:
            case EFFECT_PET_MAGIC:
            case EFFECT_PET_2_MAGIC:
            case EFFECT_PET_LET_FROST:
            case EFFECT_PET_ATT_DMG_TO_HP:
            case EFFECT_PET_BAIT:
            case EFFECT_PET_BAD_SEED:
                status[3] = Skill.CHOOSE_ENEMY;

                break;
            case EFFECT_PET_ANTI_PHY:
            case EFFECT_PET_ANTI_MGC:
            case EFFECT_PET_DEC_DMG_ALL_BATTLE:
            case EFFECT_PET_DEC_DMG_ALL_BATTLE2:
            case EFFECT_PET_IMM_STATUS:
            case EFFECT_PET_DEC_DMG:
            case EFFECT_PET_AUTO_RELIFE:
            
                status[3] = Skill.CHOOSE_NONE;

                break;
            case EFFECT_PET_PROTECT_OWNER:
            case EFFECT_PET_PROTECT_OWNER2:
            case EFFECT_PET_ADD_OWNER_HP:
            case EFFECT_PET_ADD_OWNER_HP2:
            case EFFECT_PET_ADD_OWNER_MP:
            case EFFECT_PET_UN_ALL_STATUS:
            case EFFECT_PET_SACRIFICE_BLESS:
                status[3] = Skill.CHOOSE_OWNER;

                break;
            case EFFECT_DEFENCE:
                status[3] = Skill.CHOOSE_NONE;
                
                break;
        }

        return status;
    }

    public static Random randGen = new Random();

    public static boolean getPercentRate(int percent) {
        int ran = randGen.nextInt(100);

        if (ran <= percent) {
            return true;
        } else {
            return false;
        }
    }

    public static int random(int min, int max) {
        int value = randGen.nextInt() % (max - min + 1);
        if (value < 0) {
            value = -value;
        }
        return min + value;
    }

    public boolean testRun(BattleSprite bs,int enemyLevel, int bout){
//        int tmp = 2 * enemyLevel - 2 * bs.level + 10;
//
//        if(tmp < 1){
//            tmp = 1;
//        }

//        int percent = 100 / tmp + 10 * (bout - 1) * (bout - 1);
    	//将人物遇怪逃跑公式改为：（100-怪物等级*0.8）/100；
    	int percent = 100 - enemyLevel * 8 / 10;

        if(Skill.getPercentRate(percent)){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * 宠物突袭
     * @param in
     * @param our
     * @param ourPet
     * @param them
     * @param themPet
     * @param battleMovie
     * @param movieSpeed
     * @param overPosition
     * @param battleDataProcess
     */
    protected void processIntervene(BattleSprite in, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, int movieSpeed, int overPosition,
            BattleDataProcess battleDataProcess, Vector battleRecorders){
    	if(BattleIntervene.open && in != null){
        	BattleSprite bsIntervene = in.getIntervene();
        	BattleSprite bsTarget;
        	if(bsIntervene != null && in.interveneAttackTime < BattleSprite.interveneAttackTimeMax){
        		Ability ability = bsIntervene.getInterveneSkill();
        		if(ability == null){
        			bsTarget = selectTargetRandom(bsIntervene, them, themPet);
        			if(bsTarget != null && bsTarget.getDebufStatus() != Skill.STATUS_SLEEP){
	        			bsIntervene.setTarget(bsTarget, bsTarget.groupIndex);
	        			bsIntervene.setSkill(Skill.ATTACK_SKILL);
	        			in.interveneAttackTime++;
	        			processAttack(bsIntervene, in.groupIndex, bsIntervene.skill, our, ourPet, them, themPet, battleMovie, movieSpeed, overPosition, battleDataProcess);
	        			addBattleRecorder(battleRecorders, bsIntervene, bsIntervene.skill);
        			}
        		}else{
//        			switch(ability.getId()){
//        			case 1018:
//        			case 1019:
//        			case 1020:
//        				bsTarget = selectTargetRandom(bsIntervene, new BattleSprite[]{in}, ourPet);
//        				break;
//        			default:
        				bsTarget = selectTargetRandom(bsIntervene, them, themPet);
        			//}
        			if(bsTarget != null&& bsTarget.getDebufStatus() != Skill.STATUS_SLEEP){
	        			bsIntervene.setTarget(bsTarget, bsTarget.groupIndex);
	        			bsIntervene.setSkill(Skill.getSkill(ability.getId()));
	        			//bsIntervene.setSkill(new Skill((short)ability.getId(), ability.getType(), 0, Skill.ENMITY_ALL, 0, Skill.SPEED_METHOD_ORDER_3, ability.getName()));
	        			in.interveneAttackTime++;
	        			//processMagic(bsIntervene, in.groupIndex, bsIntervene.skill, our, ourPet, them, themPet, battleMovie, movieSpeed, overPosition, battleDataProcess);
	        			processIntervene_petSkill(bsIntervene,in,our,ourPet,them,themPet,battleMovie,battleDataProcess,battleRecorders);
        			}
        		}
        	}
        }
    }
    
    protected void processIntervene_petSkill(BattleSprite bsIntervene, BattleSprite in, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie,
            BattleDataProcess battleDataProcess, Vector battleRecorders){
    	switch(bsIntervene.skill.effect){
//    	 case EFFECT_PET_2_ATTACK://二连突
//             processPet2Attack(bsIntervene, in.groupIndex, bsIntervene.skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//             break;
         case EFFECT_PET_ADD_DMG_ADD_CRI_PHY://愤怒
         case EFFECT_PET_ADD_DMG_ADD_CRI_PHY2://愤怒
             processPetAddDmgAddCriPhy(bsIntervene, in.groupIndex, bsIntervene.skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

             break;
         case EFFECT_PET_ADD_DMG_ADD_ATTACK://舍身突进
             processPetAddDmgAddAttack(bsIntervene, in.groupIndex, bsIntervene.skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

             break;
         case EFFECT_PET_ADD_DMG_DEC_HIT://狂暴
         case EFFECT_PET_ADD_DMG_DEC_HIT2://狂暴
             processPetAddDmgDecHit(bsIntervene, in.groupIndex, bsIntervene.skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

             break;
         case EFFECT_PET_MAGIC://精神创击
             processPetMagic(bsIntervene, in.groupIndex, bsIntervene.skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

             break;
//         case EFFECT_PET_2_MAGIC://双创击
//            processPet2Magic(bsIntervene, in.groupIndex, bsIntervene.skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//             break;
         case EFFECT_PET_ATT_DMG_TO_HP://生命吞噬
            processPetAttackDamageToHp(bsIntervene, in.groupIndex, bsIntervene.skill, our, ourPet, them, themPet, battleMovie, battleRecorders, battleDataProcess);

             break;
    	}
    	bsIntervene.resetBuff();
    }
}
