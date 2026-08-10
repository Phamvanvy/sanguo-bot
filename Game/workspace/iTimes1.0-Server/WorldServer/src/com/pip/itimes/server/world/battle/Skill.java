package com.pip.itimes.server.world.battle;


import java.util.Hashtable;
import java.util.Vector;
import java.util.Random;

import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.IEffectItem;
import com.pip.itimes.server.stage.Effect;
import com.pip.itimes.server.stage.PropertyEffect;
import com.pip.itimes.server.stage.Changed;


public class Skill implements SkillConstants{
    public byte type; //技能类型
    public String name; //技能名称
    public byte effect; //技能效果
    public byte status; //目标状态
    public byte position; //施法位置
    public byte coolDown; //冷却id
    public byte coolDownBout; //冷却回合
    public short id; //技能id
    public byte level; //技能级别
    public int parm1; //参数1
    public int parm2; //参数2
    public short effectBout; //影响回合
    public short mpUse; //消耗魔法值
    public byte hitRate; //技能命中率
    public int enmity; //基础仇恨
    public int adjust; //仇恨调整
    public int enmityType;//仇恨类型

    public byte speedMethod; //优先级算法

    public static Hashtable allSkill = new Hashtable();

    public static final Skill ATTACK_SKILL = new Skill(SKILL_ATTACK, TYPE_PHY, 100, ENMITY_SINGLE, 0, Skill.SPEED_METHOD_ORDER_3,"攻击");
    public static final Skill ITEM_SKILL = new Skill(SKILL_ITEM, TYPE_ITEM, 55, ENMITY_ALL, 0, Skill.SPEED_METHOD_ORDER_2,"物品");
    public static final Skill RUNAWAY_SKILL = new Skill(SKILL_RUN, TYPE_RUNAWAY, 0, ENMITY_ALL, 0, Skill.SPEED_METHOD_ORDER_1,"逃跑");
    public static final Skill CATCH_SKILL = new Skill(SKILL_CATCH, TYPE_CATCH, 100, ENMITY_ALL, 0, Skill.SPEED_METHOD_ORDER_3,"捕捉");
    public static final Skill STAY_SKILL = new Skill(SKILL_STAY, TYPE_STAY, 0, ENMITY_ALL, 0, Skill.SPEED_METHOD_FIRST,"发呆");
    public static final Skill NONE_SKILL = new Skill(SKILL_NONE,TYPE_STAY,0,ENMITY_ALL,0,Skill.SPEED_METHOD_FIRST,"");
    public static final Skill NOTREADY_SKILL = new Skill(SKILL_NOT_READY,TYPE_STAY,0,ENMITY_ALL,0,Skill.SPEED_METHOD_FIRST,"");


    public Skill(){

    }

    public Skill(short id, byte type, int enmity, int enmityType, int adjust){
        this.id = id;
        this.type = type;
        this.enmity = enmity;
        this.enmityType = enmityType;
        this.adjust = adjust;
    }

    public Skill(short id, byte type, int enmity, int enmityType, int adjust, byte speedMethod,String name){
        this(id, type, enmity, enmityType, adjust);
        this.speedMethod = speedMethod;
        this.name = name;
    }

    public Skill(byte type, String name, byte effect, byte status, byte postion, byte coolDown, byte coolDownBout){
        this.type = type;
        this.name = name;
        this.effect = effect;
        this.status = status;
        this.position = postion;
        this.coolDown = coolDown;
        this.coolDownBout = coolDownBout;
    }

    public Object clone(){
        Skill newSkill = new Skill();
        newSkill.type = type;
        newSkill.name = name;
        newSkill.effect = effect;
        newSkill.status = status;
        newSkill.position = position;
        newSkill.coolDown = coolDown;
        newSkill.coolDownBout = coolDownBout;
        newSkill.id = id;
        newSkill.level = level;
        newSkill.parm1 = parm1;
        newSkill.parm2 = parm2;
        newSkill.effectBout = effectBout;
        newSkill.mpUse = mpUse;
        newSkill.hitRate = hitRate;
        newSkill.enmity = enmity;
        newSkill.adjust = adjust;
        newSkill.enmityType = enmityType;
        newSkill.speedMethod = speedMethod;
        return newSkill;
    }

    public int initParm(Ability ability){
        id = (short)ability.getId();
        level = (byte)ability.getLevel();
        parm1 = ability.getValue1();
        parm2 = ability.getValue2();
        effectBout = (short)ability.getEffectTime();
        mpUse = (short)ability.getMana();
        speedMethod = (byte)ability.getArithmetic();
        hitRate = (byte)ability.getHit();

        //如果影响回合，如果未设置大于0的数，则用9999代替模拟持续整场战斗
        if(effectBout <= 0){
            effectBout = 9999;
        }

        enmity = ability.getEnmity();
        enmityType = ability.getEnmityType();
        adjust = ability.getAdjust();
        return id;
    }

    public short getMpUse(BattleSprite bs){
        short result;

        if(mpUse >= 0){
            result = mpUse;
        }else if(mpUse > -9999){
            result = (short)(-mpUse * bs.level / 100);
        }else{ //小于-9999的设置视为消耗全部mp
            result = (short)bs.mp;

            if(result <= 0){
                result = 1;
            }
        }

        return result;
    }

    public static Skill getSkill(int skillId){
        return (Skill)allSkill.get(new Integer(skillId));
    }

//    public static boolean doSkill(BattleSprite opp, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie, int bout, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        int groupIndex = opp.groupIndex;
//        int[] movie;
//        BattleSprite targetSprite;
//        boolean solidSkill = true;
//        boolean win = false;
//
//        if (opp.target != null && opp.target.testCannotBattle()) {
//    //            Skill skill = Skill.getSkill(opp.skillId);
//            Skill skill = opp.skill;
//            if (opp.target.testCannotBattle() && skill != null &&
//                skill.effect != Skill.EFFECT_SAVE_LIFE) {
//                targetSprite = selectTargetRandom(opp, them, themPet);
//
//                if (targetSprite != null) {
//                    opp.setTarget(targetSprite, targetSprite.groupIndex);
//                } else {
//                    win = true;
//                }
//            }
//        }else if(opp.target==null){
//            int[] ret = opp.skill.getSkillStatus(opp,opp.skill.id);
//            switch(ret[3]){
//                case Skill.CHOOSE_ENEMY:
//                    targetSprite = selectTargetRandom(opp, them, themPet);
//
//                if (targetSprite != null) {
//                    opp.setTarget(targetSprite, targetSprite.groupIndex);
//                } else {
//                    win = true;
//                }
//
//            }
//
//        }
//
//        if(selectTargetRandom(opp, them, themPet) == null){
//            win = true;
//        }
//
//        if(win){
//            return win;
//        }
//
//        try{
//            switch(opp.skill.id){
//                case SKILL_ATTACK:
//                    if(opp.target.testCannotBattle()){
//                        targetSprite = selectTargetRandom(opp, them, themPet);
//
//                        if(targetSprite != null){
//                            opp.setTarget(targetSprite, targetSprite.groupIndex);
//                        }else{
//                            win = true;
//
//                            break;
//                        }
//                    }
//
//                    processAttack(opp, groupIndex, null, them, themPet, battleMovie, MOVIE_SPEED_NORMAL, OVER_POSITION_BACK, battleDataProcess);
//                    addBattleRecorder(battleRecorders, opp, ATTACK_SKILL);
//                    break;
//                case SKILL_ITEM:
//
//                    Effect[] effect = ((IEffectItem)opp.usedItem).getEffects();
//                    int hp = 0;
//                    int mp = 0;
//                    for(int i = 0; i < effect.length; i++){
//                        int pro = ((PropertyEffect)effect[i]).getProperty();
//                        int value = ((PropertyEffect)effect[i]).getValue();
//                        if(pro == Changed.HP){
//                            hp += value;
//                        }else if(pro == Changed.MP){
//                            mp += value;
//                        }
//                    }
//
//                    if(opp.target.testCannotBattle()){
//                        opp.setTarget(opp, groupIndex);
//                    }
//                    opp.target.changeHp(hp, battleMovie, battleDataProcess);
//                    opp.target.changeMp(mp);
//                    opp.used = true;
//
//                    //TODO jlin to change it
//                    //TODO is old code --> opp.target.player.completeRemoveItem(opp.usedItem, 1, null);
//                    if(opp.player!=null)
//                        opp.player.completeRemoveItem(opp.usedItem, 1, null);
//
//                    movie = makeMovieSub(opp.bsType, groupIndex, opp.target.bsType, opp.targetIndex, Skill.SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT,
//                                    opp.target.getDebufStatus(), 0, 0, 0, hp, mp);
//                    battleMovie.addElement(movie);
//                    addBattleRecorder(battleRecorders, opp, ITEM_SKILL);
//
//                    break;
//                case SKILL_RUN:
//                    int tmpLevel = 0;
//
//                    for(int i = 0; i < them.length; i++){
//                        if(them[i] == null){
//                            continue;
//                        }
//
//                        if(!them[i].testCannotBattle()){
//                            if(tmpLevel < them[i].level){
//                                tmpLevel = them[i].level;
//                            }
//                        }
//                    }
//
//                    opp.setTarget(opp, groupIndex);
//                    int tmp = opp.testRun(tmpLevel, bout)? HIT_HIT: HIT_MISS;
//
//                    if(tmp == HIT_HIT){
//                        opp.setDeBufStatus(1, Skill.STATUS_RUNAWAY, 0, 0, 0, opp.bsType, opp.groupIndex);
//                    }
//
//                    movie = makeMovieSub(opp.bsType, groupIndex, opp.bsType, groupIndex, opp.skill.id, ANIMATE_RUNAWAY, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_NORMAL, tmp,
//                                    opp.getDebufStatus(), Skill.ATTACK_NO_CRI, 0, 0, 0, 0);
//                    battleMovie.addElement(movie);
//                    addBattleRecorder(battleRecorders, opp, RUNAWAY_SKILL);
//                    break;
//                case SKILL_STAY:
//                    movie = makeMovieSub(opp.bsType, groupIndex, opp.bsType, groupIndex, opp.skill.id, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_NORMAL, HIT_HIT, opp
//                                    .getDebufStatus(), Skill.ATTACK_NO_CRI, 0, 0, 0, 0);
//                    battleMovie.addElement(movie);
//
//                    break;
//                case SKILL_CATCH:
//                    int catchHit = opp.target.doCatch()? HIT_HIT: HIT_MISS;
//                    if(catchHit==HIT_HIT){
//                        opp.catchedPet = opp.target;
//                    }
//                    movie = makeMovieSub(opp.bsType, groupIndex, opp.target.bsType, opp.targetIndex, opp.skill.id, ANIMATE_STS_ATK, POSITION_DEST, OVER_POSITION_BACK, MOVIE_SPEED_NORMAL, catchHit,
//                                    opp.target.getDebufStatus(), Skill.ATTACK_NO_CRI, 0, 0, 0, 0);
//                    battleMovie.addElement(movie);
//
//                    addBattleRecorder(battleRecorders, opp, CATCH_SKILL);
//                    break;
//                default:
//                    solidSkill = false;
//
//                    break;
//            }
//
//            if(solidSkill || win){
//                return win;
//            }
//
////            Skill skill = Skill.getSkill(opp.skillId);
//            Skill skill = opp.skill;
//
//            int animateType = Skill.ANIMATE_NONE;
//
//            switch(skill.type){
//                case Skill.TYPE_PHY:
//                    animateType = Skill.ANIMATE_PHY_START;
//
//                    break;
//                case Skill.TYPE_MGC:
//                    animateType = Skill.ANIMATE_MGC_START;
//
//                    break;
//                case Skill.TYPE_DEF:
//                    animateType = Skill.ANIMATE_DEF_START;
//
//                    break;
//                case Skill.TYPE_ASS:
//                    animateType = Skill.ANIMATE_ASS_START;
//
//                    break;
//                case Skill.TYPE_PET:
//                    animateType = Skill.ANIMATE_PHY_START;
//
//                    break;
//            }
//
//            opp.usedMp = skill.getMpUse(opp);
//            opp.changeMp(-skill.getMpUse(opp));
//
//            movie = makeMovieSub(opp.bsType, groupIndex, opp.bsType, groupIndex, opp.skill.id, animateType, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, opp.getDebufStatus(),
//                            Skill.ATTACK_NO_CRI, 0, -skill.getMpUse(opp), 0, 0);
//            battleMovie.addElement(movie);
//
//            switch(skill.effect){
//                case Skill.EFFECT_MULTI_ATK:
//                    win = processMultiAttack(opp, groupIndex, skill, them, themPet, battleMovie, battleDataProcess);
//
//                    break;
//                case EFFECT_INC_ATK_INC_DMG:
//                    win = processIncreaseAttackIncreaseDamage(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_ANTI_BUF_INC_ATK:
//                    win = processAntiBufIncreaseAttack(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_FAINT:
//                    win = processFaintAttack(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_INC_ATK_INC_CRI:
//                    win = processIncreaseAttackIncreaseCri(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_INC_DMG_DEC_HIT:
//                    win = processIncreaseDamageDecreaseHit(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_INC_ATK_STOP:
//                    win = processIncreaseAttackStop(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_ALWAYS_INC_ATK_INC_DMG:
//                    win = processAlwaysIncreaseAttackIncreaseDamage(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_MGC_ATK:
//                    win = processMagicAttack(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_MULTI_MGC:
//                    win = processMultiMagic(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_LET_POISON:
//                    win = processLetPoison(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_LET_SLEEP:
//                    win = processLetSleep(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_LET_STONE:
//                    win = processLetStone(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_MAGIC_ALL:
//                    win = processMagicAll(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_LET_CONFUSE:
//                    win = processLetConfuse(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_INC_MGC_LET_POSION:
//                    win = processIncreaseMagicLetPosion(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_MGC_USE_ALL_MP:
//                    win = processMagicUseAllMp(opp, groupIndex, skill, our, ourPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_DEC_ATK_INC_FLEE:
//                    win = processDecreaseAttackIncreaseFlee(opp, groupIndex, skill, our, ourPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_BLOCK_ATK_DEC_DMG:
//                    win = processBlockAttackDecreaseDamage(opp, groupIndex, skill, our, ourPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_DEC_PHY_DMG:
//                    win = processDecreasePhyDamage(opp, groupIndex, skill, our, ourPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_DEC_MGC_DMG:
//                    win = processDecreaseMagicDamage(opp, groupIndex, skill, our, ourPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_DEC_PHY_MGC_DMG:
//                    win = processDecreasePhyMagicDamage(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PHY_DMG_TO_HP:
//                    win = processSorbPhyDamage(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_MGC_DMG_TO_HP:
//                    win = processSorbMagicDamage(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_RESTORE_HP:
//                    win = processRestoreHp(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_RESTORE_ALL_HP:
//                    win = processRestoreAllHp(opp, groupIndex, skill, our, ourPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_SAVE_LIFE:
//                    win = processSaveLife(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_ANTI_PHY:
//                    win = processAntiPhy(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_ANTI_MGC:
//                    win = processAntiMagic(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_RESTORE_LOT_HP:
//                    win = processRestoreLotHp(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_CLEAR_STS_AND_ANTI:
//                    win = processClearStatusAndAnti(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_2_ATTACK:
//                    win = processPet2Attack(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_ADD_DMG_ADD_CRI_PHY:
//                    win = processPetAddDmgAddCriPhy(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_ADD_DMG_ADD_ATTACK:
//                    win = processPetAddDmgAddAttack(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_ANTI_MGC:
//                    win = processPetAntiMagic(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_ADD_DMG_DEC_HIT:
//                    win = processPetAddDmgDecHit(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_LET_POISON:
//                    win = processPetLetPoison(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_MAGIC:
//                    win = processPetMagic(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_2_MAGIC:
//                    win = processPet2Magic(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_LET_FROST:
//                    win = processPetLetFrost(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_ANTI_PHY:
//                    win = processPetAntiPhy(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_DEC_DMG_ALL_BATTLE:
//                    win = processPetDecDmgAllBattle(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_DMG_TO_MGC:
//                    win = processPetDmgToMgc(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_IMM_STATUS:
//                    win = processPetImmStatus(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_PROTECT_OWNER:
//                    win = processPetProtectOwner(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_DEC_DMG:
//                    win = processPetDecDmg(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_ATT_DMG_TO_HP:
//                    win = processPetAttackDamageToHp(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_AUTO_RELIFE:
//                    win = processPetAutoRelife(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_ADD_OWNER_HP:
//                    win = processPetAddOwnerHp(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_ADD_OWNER_MP:
//                    win = processPetAddOwnerMp(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//                case EFFECT_PET_UN_ALL_STATUS:
//                    win = processPetUnAllStatus(opp, groupIndex, skill, them, themPet, battleMovie, battleRecorders, battleDataProcess);
//
//                    break;
//            }
//
//            opp.coolDownSkill(skill);
//        }finally{
//            battleDataProcess.spriteDoneSkill(opp, groupIndex, false);
//        }
//
//        return win;
//    }
//
//    /**
//     * 宠物，物：双次物攻
//     */
//    private static boolean processPet2Attack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        BattleSprite targetSprite;
//        boolean win = false;
//
//        in.setSkill(null);
//
//        for(int i = 0; i < 2; i++){
//            targetSprite = selectTargetRandom(in, them, themPet);
//
//            if(targetSprite != null){
//                if(in.testCannotBattle()){
//                    processEmptyLoop(in, index, battleMovie, battleDataProcess);
//
//                    break;
//                }
//
//                if(i == 2 - 1){
//                    processAttack(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//                }else{
//                    processAttack(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY, battleDataProcess);
//                }
//            }else{
//                processEmptyLoop(in, index, battleMovie, battleDataProcess);
//
//                win = true;
//
//                break;
//            }
//        }
//
//        in.setDeBufStatus(skill.parm1, Skill.STATUS_STOP, 0, 0, 0, in.bsType, in.groupIndex);
//        processStatusUpdate(in, index, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return win;
//    }
//
//    /**
//     * 宠物，物：加伤加爆
//     */
//    private static boolean processPetAddDmgAddCriPhy(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, skill.parm2, skill.parm1, skill.parm1, 0, 0, skill.effect);
//            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//        }
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return win;
//    }
//
//    /**
//     * 宠物，物：加伤加攻
//     */
//    private static boolean processPetAddDmgAddAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            in.AddAttrBuf(skill.effectBout, skill.parm2, 0, 0, 0, 0, 0, 0, skill.parm1, skill.parm1, 0, 0, skill.effect);
//            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//        }
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return win;
//    }
//
//    /**
//     * 宠物，物：反击魔法
//     */
//    private static boolean processPetAntiMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.setTarget(in, in.groupIndex);
//        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_MAGIC, skill.level, (in.level / 10 + (in.level < 10? 1: 0)) * 10 * 100 / skill.parm1, 0, in.bsType, in.groupIndex);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，物：加伤降命
//     */
//    private static boolean processPetAddDmgDecHit(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, -skill.parm2, 0, 0, 0, 0, 0, 0, skill.effect);
//            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//        }
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return win;
//    }
//
//    /**
//     * 宠物，魔：上毒技能
//     */
//    private static boolean processPetLetPoison(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
//            in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_POISON, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
//            processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
//        }else{
//            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
//        }
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return win;
//    }
//
//    /**
//     * 宠物，魔：降防加爆
//     */
//    private static boolean processPetMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        processMagic(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，魔：双次魔攻
//     */
//    private static boolean processPet2Magic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        BattleSprite targetSprite;
//        boolean win = false;
//
//        in.setSkill(null);
//        in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, -skill.parm1, 0, 0, 0, 0, 0, 0, skill.effect);
//
//        for(int i = 0; i < 2; i++){
//            targetSprite = selectTargetRandom(in, them, themPet);
//
//            if(targetSprite != null){
//                if(in.testCannotBattle()){
//                    processEmptyLoop(in, index, battleMovie, battleDataProcess);
//
//                    break;
//                }
//
//                if(i == 2 - 1){
//                    processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//                }else{
//                    processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY, battleDataProcess);
//                }
//            }else{
//                processEmptyLoop(in, index, battleMovie, battleDataProcess);
//
//                win = true;
//
//                break;
//            }
//        }
//
//        return win;
//    }
//
//    /**
//     * 宠物，魔：冰冻敌人
//     */
//    private static boolean processPetLetFrost(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
//            in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_FROST, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
//            processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
//        }else{
//            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
//        }
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return win;
//    }
//
//    /**
//     * 宠物，魔：反击物理
//     */
//    private static boolean processPetAntiPhy(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.setTarget(in, in.groupIndex);
//        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_ATTACK, skill.level, (in.level / 10 + (in.level < 10? 1: 0)) * 10 * 100 / skill.parm1, 0, in.bsType, in.groupIndex);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，防：减低伤害
//     */
//    private static boolean processPetDecDmgAllBattle(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.setTarget(in, in.groupIndex);
//        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, -skill.parm1, 0, 0, skill.effect);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，防：伤害转魔
//     */
//    private static boolean processPetDmgToMgc(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.setTarget(in, in.groupIndex);
//        in.setBufStatus(skill.effectBout, Skill.STATUS_DAMAGE_TO_MP, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，防：免疫状态
//     */
//    private static boolean processPetImmStatus(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.setTarget(in, in.groupIndex);
//        in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, skill.parm1, skill.parm1, 0, 0, skill.effect);
//        in.setBufStatus(skill.effectBout, Skill.STATUS_IMMUNITY_STATUS, skill.level, 0, 0, in.bsType, in.groupIndex);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，防：替主抗伤
//     */
//    private static boolean processPetProtectOwner(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.target.setBufStatus(skill.effectBout, Skill.STATUS_PROTECTED, skill.level, skill.parm1, 1, in.bsType, in.groupIndex);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，防：减低伤害
//     */
//    private static boolean processPetDecDmg(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.setTarget(in, in.groupIndex);
//        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, -skill.parm1, 0, 0, skill.effect);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，治：输出回血
//     */
//    private static boolean processPetAttackDamageToHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.setBufStatus(skill.effectBout, Skill.STATUS_ATTACK_DAMAGE_TO_HP, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
//        processMagic(in, index, skill, them, themPet, battleMovie, POSITION_DEST, OVER_POSITION_BACK, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，治：死后复生
//     */
//    private static boolean processPetAutoRelife(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.setTarget(in, in.groupIndex);
//        in.target.setBufStatus(skill.effectBout, Skill.STATUS_AUTO_RELIFE, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，治：给主加血
//     */
//    private static boolean processPetAddOwnerHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        int hpAdd = in.level * skill.parm1 / 100;
//        int cri = ATTACK_NO_CRI;
//
//        if(in.testMCri()){
//            hpAdd *= BattleSprite.CRI_RATE;
//            cri = ATTACK_CRI;
//        }else{
//            cri = ATTACK_NO_CRI;
//        }
//
//        in.target.changeHp(hpAdd, battleMovie, battleDataProcess);
//        processRestore(in, index, hpAdd, 0, cri, battleMovie, battleDataProcess);
//
//        in.setTarget(in, in.groupIndex);
//        in.changeHp(hpAdd, battleMovie, battleDataProcess);
//        processRestore(in, index, hpAdd, 0, cri, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，治：给主回魔
//     */
//    private static boolean processPetAddOwnerMp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        int mpAdd = in.level * skill.parm1 / 100;
//
//        if(in.testMCri()){
//            mpAdd *= BattleSprite.CRI_RATE;
//            processRestore(in, index, 0, mpAdd, ATTACK_CRI, battleMovie, battleDataProcess);
//        }else{
//            processRestore(in, index, 0, mpAdd, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//        }
//
//        in.target.changeMp(mpAdd);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    /**
//     * 宠物，治：解除状态
//     */
//    private static boolean processPetUnAllStatus(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, skill.parm1, skill.parm1, 0, 0, skill.effect);
//
//        in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//        processUnStatus(in, index, HIT_HIT, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    private static boolean processAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, int movieSpeed, int overPosition,
//                    BattleDataProcess battleDataProcess){
//        int[] battleResult = in.doBattle(BattleSprite.ACTION_PATTACK);
//
//        if(battleResult[3] == Skill.ATTACK_SORB && battleResult[0] == Skill.HIT_HIT && getPercentRate(battleResult[4])){
//            int hpSorb = battleResult[1] * battleResult[5] / 100;
//
//            if(hpSorb <= 0){
//                hpSorb = 1;
//            }
//
//            in.target.changeHp(hpSorb, battleMovie, battleDataProcess);
//
//            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
//                            battleResult[2], 0, 0, 0, 0);
//            battleMovie.addElement(movie);
//
//            movie = makeMovieSub(in.target.bsType, in.targetIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.target
//                            .getDebufStatus(), battleResult[2], 0, 0, hpSorb, 0);
//            battleMovie.addElement(movie);
//
//            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
//                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
//            }
//
//            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
//        }else if(battleResult[3] == Skill.ATTACK_ANTI && battleResult[0] == Skill.HIT_HIT){
//            int antiDamage = battleResult[1] * battleResult[4] / 100;
//
//            if(antiDamage <= 0){
//                antiDamage = -1;
//            }else{
//                antiDamage = -antiDamage;
//            }
//
//            int overPos = overPosition;
//
//            in.changeHp(antiDamage, battleMovie, battleDataProcess);
//
//            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
//                            .getDebufStatus(), battleResult[2], 0, 0, 0, 0);
//            battleMovie.addElement(movie);
//
//            if(in.testDie()){
//                overPos = OVER_POSITION_BACK;
//            }
//
//            movie = makeMovieSub(in.bsType, index, in.bsType, index, SKILL_NONE, ANIMATE_HURT, POSITION_STAY, overPos, movieSpeed, HIT_HIT, in.getDebufStatus(), battleResult[2], antiDamage, 0,
//                            0, 0);
//            battleMovie.addElement(movie);
//
//            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
//                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
//            }
//
//            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
//        }else if(battleResult[3] == Skill.ATTACK_PROCTECT && battleResult[0] == Skill.HIT_HIT){
//            int protectDamage = battleResult[1] * (100 - battleResult[4]) / 100;
//            int protectSrcType = battleResult[6];
//            int protectSrcIndex = battleResult[7];
//
//            BattleSprite src = battleDataProcess.getSprite(protectSrcType, protectSrcIndex);
//
//            if(src.testCannotBattle()){ //保护人已死亡，清除保护状态，按正常攻击处理
//                in.target.setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);
//
//                in.target.changeHp(-battleResult[1], battleMovie, battleDataProcess);
//
//                int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target
//                                .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
//                battleMovie.addElement(movie);
//
//                if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
//                    in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//                    processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
//                }
//
//                if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP){
//                    int[] bufInfo = in.getbufInfo();
//
//                    int hpInc = (bufInfo[3] * battleResult[1]) / 100;
//
//                    if(hpInc > 0){
//                        in.changeHp(hpInc, battleMovie, battleDataProcess);
//
//                        movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
//                                        0, 0, hpInc, 0);
//                        battleMovie.addElement(movie);
//                    }
//                }
//            }else{
//                if(protectDamage <= 0){
//                    protectDamage = 1;
//                }
//
//                int restDamage = battleResult[1] - protectDamage;
//
//                if(restDamage < 0){
//                    restDamage = 0;
//                }
//
//                src.changeHp(-protectDamage, battleMovie, battleDataProcess);
//
//                if(restDamage > 0 && battleResult[5] != 0){
//                    in.target.changeHp(-restDamage, battleMovie, battleDataProcess);
//                }
//
//                int[] movie = makeMovieSub(src.bsType, src.groupIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
//                                .getDebufStatus(), 0, 0, 0, 0, 0);
//                battleMovie.addElement(movie);
//
//                if(restDamage > 0 && battleResult[5] != 0){
//                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, battleResult[0], src.getDebufStatus(),
//                                    battleResult[2], 0, 0, -protectDamage, 0);
//                    battleMovie.addElement(movie);
//
//                    movie = makeMovieSub(in.bsType, index, in.target.bsType, in.target.groupIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target.getDebufStatus(),
//                                    battleResult[2], 0, 0, -restDamage, 0);
//                    battleMovie.addElement(movie);
//                }else{
//                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], src.getDebufStatus(),
//                                    battleResult[2], 0, 0, -protectDamage, 0);
//                    battleMovie.addElement(movie);
//                }
//
//                movie = makeMovieSub(src.bsType, src.groupIndex, src.bsType, src.groupIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_BACK, movieSpeed, HIT_HIT, src.getDebufStatus(), 0,
//                                0, 0, 0, 0);
//                battleMovie.addElement(movie);
//            }
//
//            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
//        }else if(battleResult[3] == Skill.ATTACK_PET_DAMAGE_TO_MP && battleResult[0] == Skill.HIT_HIT){
//            int mpDamage = battleResult[1] * battleResult[4] / 100;
//
//            if(mpDamage <= 0){
//                mpDamage = 1;
//            }
//
//            if(mpDamage > in.target.mp){
//                mpDamage = in.target.mp;
//            }
//
//            int hpDamage = battleResult[1] - mpDamage;
//
//            if(hpDamage < 0){
//                hpDamage = 0;
//            }
//
//            in.target.changeMp(-mpDamage);
//            in.target.changeHp(-hpDamage, battleMovie, battleDataProcess);
//
//            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
//                            battleResult[2], 0, 0, -hpDamage, -mpDamage);
//            battleMovie.addElement(movie);
//
//            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
//                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
//            }
//
//            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
//        }else{
//            in.target.changeHp(-battleResult[1], battleMovie, battleDataProcess);
//
//            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target
//                            .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
//            battleMovie.addElement(movie);
//
//            if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
//                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
//            }
//
//            if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP){
//                int[] bufInfo = in.getbufInfo();
//
//                int hpInc = (bufInfo[3] * battleResult[1]) / 100;
//
//                if(hpInc > 0){
//                    in.changeHp(hpInc, battleMovie, battleDataProcess);
//
//                    movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
//                                    0, 0, hpInc, 0);
//                    battleMovie.addElement(movie);
//                }
//            }
//        }
//
//        return battleResult[0] == Skill.HIT_HIT;
//    }
//
//    private static void processMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, int movieSpeed, int overPosition,
//                    BattleDataProcess battleDataProcess){
//        int[] battleResult = in.doBattle(BattleSprite.ACTION_MATTACK);
//
//        if(battleResult[3] == Skill.ATTACK_SORB && battleResult[0] == Skill.HIT_HIT && getPercentRate(battleResult[4])){
//            int hpSorb = battleResult[1] * battleResult[5] / 100;
//
//            if(hpSorb <= 0){
//                hpSorb = 1;
//            }
//
//            in.target.changeHp(hpSorb, battleMovie, battleDataProcess);
//
//            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_STAY, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
//                            battleResult[2], 0, 0, 0, 0);
//            battleMovie.addElement(movie);
//
//            movie = makeMovieSub(in.target.bsType, in.targetIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
//                            .getDebufStatus(), battleResult[2], 0, 0, hpSorb, 0);
//            battleMovie.addElement(movie);
//
//            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
//                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
//            }
//
//            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
//        }else if(battleResult[3] == Skill.ATTACK_ANTI && battleResult[0] == Skill.HIT_HIT){
//            int antiDamage = battleResult[1] * battleResult[4] / 100;
//
//            if(antiDamage <= 0){
//                antiDamage = -1;
//            }else{
//                antiDamage = -antiDamage;
//            }
//
//            int overPos = overPosition;
//
//            in.changeHp(antiDamage, battleMovie, battleDataProcess);
//
//            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_STAY, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
//                            .getDebufStatus(), battleResult[2], 0, 0, 0, 0);
//            battleMovie.addElement(movie);
//
//            if(in.testDie()){
//                overPos = OVER_POSITION_BACK;
//            }
//
//            movie = makeMovieSub(in.target.bsType, in.targetIndex, in.bsType, index, in.skill.id, ANIMATE_MGC_ATK, POSITION_STAY, overPos, movieSpeed, HIT_HIT, in.getDebufStatus(), battleResult[2], 0,
//                            0, antiDamage, 0);
//            battleMovie.addElement(movie);
//
//            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
//                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
//            }
//
//            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
//        }else if(battleResult[3] == Skill.ATTACK_PROCTECT && battleResult[0] == Skill.HIT_HIT){
//            int protectDamage = battleResult[1] * (100 - battleResult[4]) / 100;
//            int protectSrcType = battleResult[6];
//            int protectSrcIndex = battleResult[7];
//
//            BattleSprite src = battleDataProcess.getSprite(protectSrcType, protectSrcIndex);
//
//            if(src.testCannotBattle()){ //保护人已死亡，清除保护状态，按正常攻击处理
//                in.target.setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);
//
//                in.target.changeHp(-battleResult[1], battleMovie, battleDataProcess);
//
//                int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_STAY, overPosition, movieSpeed, battleResult[0], in.target
//                                .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
//                battleMovie.addElement(movie);
//
//                if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
//                    in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//                    processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
//                }
//
//                if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP){
//                    int[] bufInfo = in.getbufInfo();
//
//                    int hpInc = (bufInfo[3] * battleResult[1]) / 100;
//
//                    if(hpInc > 0){
//                        in.changeHp(hpInc, battleMovie, battleDataProcess);
//
//                        movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
//                                        0, 0, hpInc, 0);
//                        battleMovie.addElement(movie);
//                    }
//                }
//            }else{
//                if(protectDamage <= 0){
//                    protectDamage = 1;
//                }
//
//                int restDamage = battleResult[1] - protectDamage;
//
//                if(restDamage < 0){
//                    restDamage = 0;
//                }
//
//                src.changeHp(-protectDamage, battleMovie, battleDataProcess);
//
//                if(restDamage > 0 && battleResult[5] != 0){
//                    in.target.changeHp(-restDamage, battleMovie, battleDataProcess);
//                }
//
//                int[] movie = makeMovieSub(src.bsType, src.groupIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
//                                .getDebufStatus(), 0, 0, 0, 0, 0);
//                battleMovie.addElement(movie);
//
//                if(restDamage > 0 && battleResult[5] != 0){
//                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, battleResult[0], src.getDebufStatus(),
//                                    battleResult[2], 0, 0, -protectDamage, 0);
//                    battleMovie.addElement(movie);
//
//                    movie = makeMovieSub(in.bsType, index, in.target.bsType, in.target.groupIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target.getDebufStatus(),
//                                    battleResult[2], 0, 0, -restDamage, 0);
//                    battleMovie.addElement(movie);
//                }else{
//                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], src.getDebufStatus(),
//                                    battleResult[2], 0, 0, -protectDamage, 0);
//                    battleMovie.addElement(movie);
//                }
//
//                movie = makeMovieSub(src.bsType, src.groupIndex, src.bsType, src.groupIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_BACK, movieSpeed, HIT_HIT, src.getDebufStatus(), 0,
//                                0, 0, 0, 0);
//                battleMovie.addElement(movie);
//            }
//
//            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
//        }else if(battleResult[3] == Skill.ATTACK_PET_DAMAGE_TO_MP && battleResult[0] == Skill.HIT_HIT){
//            int mpDamage = battleResult[1] * battleResult[4] / 100;
//
//            if(mpDamage <= 0){
//                mpDamage = 1;
//            }
//
//            if(mpDamage > in.target.mp){
//                mpDamage = in.target.mp;
//            }
//
//            int hpDamage = battleResult[1] - mpDamage;
//
//            if(hpDamage < 0){
//                hpDamage = 0;
//            }
//
//            in.target.changeMp(-mpDamage);
//            in.target.changeHp(-hpDamage, battleMovie, battleDataProcess);
//
//            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_DEST, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
//                            battleResult[2], 0, 0, -hpDamage, -mpDamage);
//            battleMovie.addElement(movie);
//
//            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
//                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
//            }
//
//            battleDataProcess.spriteDoneSkill(in.target, in.targetIndex, true);
//        }else{
//            in.target.changeHp(-battleResult[1], battleMovie, battleDataProcess);
//
//            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skill.id, ANIMATE_MGC_ATK, POSITION_STAY, overPosition, movieSpeed, battleResult[0], in.target
//                            .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
//            battleMovie.addElement(movie);
//
//            if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
//                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//                processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
//            }
//
//            if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP && battleResult[0] == Skill.HIT_HIT){
//                int[] bufInfo = in.getbufInfo();
//
//                int hpInc = (bufInfo[3] * battleResult[1]) / 100;
//
//                if(hpInc > 0){
//                    in.changeHp(hpInc, battleMovie, battleDataProcess);
//
//                    movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
//                                    0, 0, hpInc, 0);
//                    battleMovie.addElement(movie);
//                }
//            }
//        }
//    }
//
//    public static void processEmptyLoop(BattleSprite in, int index, Vector battleMovie, BattleDataProcess battleDataProcess){
//        int[] movie = makeMovieSub(in.bsType, index, in.bsType, index, Skill.SKILL_NONE, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.getDebufStatus(),
//                        ATTACK_NO_CRI, 0, 0, 0, 0);
//        battleMovie.addElement(movie);
//    }
//
    public static void processStatusUpdate(BattleSprite in, int index, Vector battleMovie, BattleDataProcess battleDataProcess){
        int[] movie = makeMovieSub(in.bsType, index, in.bsType, index, Skill.SKILL_UPDATE_STATUS, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.getDebufStatus(),
                        ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
    }
//
//    private static void processStatusAttack(BattleSprite in, int index, int hit, Vector battleMovie, BattleDataProcess battleDataProcess){
//        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, Skill.SKILL_NONE, ANIMATE_STS_ATK, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, hit, in.target
//                        .getDebufStatus(), ATTACK_NO_CRI, 0, 0, 0, 0);
//        battleMovie.addElement(movie);
//    }
//
//    private static void processUnStatus(BattleSprite in, int index, int hit, Vector battleMovie, BattleDataProcess battleDataProcess){
//        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, Skill.SKILL_NONE, ANIMATE_UNS_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, hit, in.target
//                        .getDebufStatus(), ATTACK_NO_CRI, 0, 0, 0, 0);
//        battleMovie.addElement(movie);
//    }
//
//    private static void processRestore(BattleSprite in, int index, int hpInc, int mpInc, int cri, Vector battleMovie, BattleDataProcess battleDataProcess){
//        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, Skill.SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.target
//                        .getDebufStatus(), cri, 0, 0, hpInc, mpInc);
//        battleMovie.addElement(movie);
//    }
//
    public static void processSaveLifeMovie(BattleSprite in, int index, int hpInc, int cri, Vector battleMovie, BattleDataProcess battleDataProcess){
        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, Skill.SKILL_NONE, ANIMATE_SAV_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.target
                        .getDebufStatus(), cri, 0, 0, hpInc, 0);
        battleMovie.addElement(movie);
    }
//
//    private static boolean processMultiAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, BattleDataProcess battleDataProcess){
//        int times = skill.parm1;
//        int type = skill.parm2;
//        BattleSprite targetSprite;
//        boolean win = false;
//
//        in.setSkill(null);
//
//        for(int i = 0; i < times; i++){
//            targetSprite = selectTargetRandom(in, them, themPet);
//
//            if(targetSprite != null){
//                if(type == Skill.MULTI_ATTACK_RANDOM){
//                    in.setTarget(targetSprite, targetSprite.groupIndex);
//                }else if(in.target.testCannotBattle()){
//                    processEmptyLoop(in, index, battleMovie, battleDataProcess);
//
//                    return win;
//                }
//
//                if(in.testCannotBattle()){
//                    processEmptyLoop(in, index, battleMovie, battleDataProcess);
//
//                    return win;
//                }
//
//                if(i == times - 1){
//                    processAttack(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//                }else{
//                    processAttack(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY, battleDataProcess);
//                }
//            }else{
//                processEmptyLoop(in, index, battleMovie, battleDataProcess);
//
//                win = true;
//
//                break;
//            }
//        }
//
//        return win;
//    }
//
//    public static void addBattleRecorder(Vector battleRecorders, BattleRecorder recorder){
//        battleRecorders.add(recorder);
//    }
//
//    public static void addBattleRecorder(Vector battleRecorders, BattleSprite bs, Skill skill){
//        BattleRecorder recorder = new BattleRecorder(bs, skill);
//        recorder.addDest(bs.target);
//    }
//
//    private static boolean processIncreaseAttackIncreaseDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, skill.parm2, skill.parm2, 0, 0, skill.effect);
//            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//            addBattleRecorder(battleRecorders, in, skill);
//        }
//
//        return win;
//    }
//
//    private static boolean processAntiBufIncreaseAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            if(in.target.HasBuf()){
//                in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
//            }
//
//            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//            addBattleRecorder(battleRecorders, in, skill);
//        }
//
//        return win;
//    }
//
//    private static boolean processFaintAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            boolean hit = processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//
//            if(hit){
//                if(getPercentRate(skill.hitRate)){
//                    in.target.setDeBufStatus(skill.effectBout, STATUS_FAINT, skill.level, 0, 0, in.bsType, in.groupIndex);
//                    processStatusUpdate(in.target, in.targetIndex, battleMovie, battleDataProcess);
//                }
//            }
//
//            addBattleRecorder(battleRecorders, in, skill);
//        }
//
//        return win;
//    }
//
//    private static boolean processIncreaseAttackIncreaseCri(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, skill.parm2, 0, 0, 0, 0, skill.effect);
//            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//            addBattleRecorder(battleRecorders, in, skill);
//        }
//
//        return win;
//    }
//
//    private static boolean processIncreaseDamageDecreaseHit(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, -skill.parm2, 0, 0, 0, 0, 0, 0, skill.effect);
//            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//            addBattleRecorder(battleRecorders, in, skill);
//        }
//
//        return win;
//    }
//
//    private static boolean processIncreaseAttackStop(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
//            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//            in.setDeBufStatus(skill.effectBout, Skill.STATUS_STOP, 0, 0, 0, in.bsType, in.groupIndex);
//            processStatusUpdate(in, index, battleMovie, battleDataProcess);
//            addBattleRecorder(battleRecorders, in, skill);
//        }
//
//        return win;
//    }
//
//    private static boolean processAlwaysIncreaseAttackIncreaseDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, skill.parm2, skill.parm2, 0, 0, skill.effect);
//        }
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return win;
//    }
//
//    private static boolean processMagicAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            in.AddAttrBuf(skill.effectBout, 0, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
//            processMagic(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//            addBattleRecorder(battleRecorders, in, skill);
//        }
//
//        return win;
//    }
//
//    private static boolean processMultiMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        int times = skill.parm1;
//        int type = skill.parm2;
//        BattleSprite targetSprite;
//        boolean win = false;
//
//        in.setSkill(null);
//
//        for(int i = 0; i < times; i++){
//            targetSprite = selectTargetRandom(in, them, themPet);
//
//            if(targetSprite != null){
//                if(type == Skill.MULTI_ATTACK_RANDOM){
//                    in.setTarget(targetSprite, targetSprite.groupIndex);
//                }else if(in.target.testCannotBattle()){
//                    processEmptyLoop(in, index, battleMovie, battleDataProcess);
//
//                    return win;
//                }
//
//                if(in.testCannotBattle()){
//                    processEmptyLoop(in, index, battleMovie, battleDataProcess);
//
//                    return win;
//                }
//
//                if(i == times - 1){
//                    processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//                }else{
//                    processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY, battleDataProcess);
//                }
//            }else{
//                win = true;
//
//                break;
//            }
//        }
//
//        return win;
//    }
//
//    private static boolean processLetPoison(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
//            in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_POISON, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
//            processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
//        }else{
//            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
//        }
//        addBattleRecorder(battleRecorders, in, skill);
//        return win;
//    }
//
//    private static boolean processLetSleep(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(getPercentRate(skill.hitRate)){
//            if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
//                in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_SLEEP, skill.level, 0, 0, in.bsType, in.groupIndex);
//                processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
//            }else{
//                processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
//            }
//        }else{
//            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
//        }
//        addBattleRecorder(battleRecorders, in, skill);
//        return win;
//    }
//
//    private static boolean processLetStone(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        int tmpHitRate = skill.hitRate + (in.level - in.target.level) / 2;
//
//        if(getPercentRate(tmpHitRate) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
//            in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_STONE, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);
//            processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
//        }else{
//            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
//        }
//        addBattleRecorder(battleRecorders, in, skill);
//        return win;
//    }
//
//    private static boolean processMagicAll(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        in.AddAttrBuf(skill.effectBout, 0, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
//
//        for(int i = 0; i < them.length; i++){
//            if(them[i] != null && !them[i].testCannotBattle()){
//                in.setTarget(them[i], them[i].groupIndex);
//                processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//            }
//        }
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(win){
//            return win;
//        }
//
//        for(int i = 0; i < themPet.length; i++){
//            if(themPet[i] != null && !themPet[i].testCannotBattle()){
//                in.setTarget(themPet[i], themPet[i].groupIndex);
//                processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//            }
//        }
//
//        addBattleRecorder(battleRecorders, in, skill);
//        return win;
//    }
//
//    private static boolean processLetConfuse(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(getPercentRate(skill.hitRate) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
//            in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_CONFUSE, skill.level, 0, 0, in.bsType, in.groupIndex);
//            processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
//        }else{
//            processStatusAttack(in, index, HIT_MISS, battleMovie, battleDataProcess);
//        }
//        addBattleRecorder(battleRecorders, in, skill);
//        return win;
//    }
//
//    private static boolean processIncreaseMagicLetPosion(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        in.AddAttrBuf(skill.effectBout, 0, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
//        in.setDeBufStatus(skill.effectBout, Skill.STATUS_POISON, skill.level, skill.parm2, 0, in.bsType, in.groupIndex);
//        in.setTarget(in, in.groupIndex);
//        processStatusAttack(in, index, HIT_HIT, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return win;
//    }
//
//    private static boolean processMagicUseAllMp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        boolean win = false;
//
//        win = testTargetRandom(in, them, themPet);
//
//        if(!win){
//            if(!getPercentRate(skill.hitRate)){
//                in.AddAttrBuf(skill.effectBout, 0, -100, 0, 0, -100, 0, 0, 0, 0, 0, skill.parm1 * in.usedMp / 100, skill.effect);
//            }else{
//                in.AddAttrBuf(skill.effectBout, 0, -100, 0, 0, 0, 0, 0, 0, 0, 0, skill.parm1 * in.usedMp / 100, skill.effect);
//            }
//
//            processMagic(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK, battleDataProcess);
//        }
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return win;
//    }
//
//    private static boolean processDecreaseAttackIncreaseFlee(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.setTarget(in, in.groupIndex);
//        in.target.AddAttrBuf(skill.effectBout, skill.parm1, skill.parm1, 0, 0, 0, skill.parm2, 0, 0, 0, 0, 0, skill.effect);
//        addBattleRecorder(battleRecorders, in, skill);
//        return false;
//    }
//
//    private static boolean processBlockAttackDecreaseDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.target.setBufStatus(skill.effectBout, Skill.STATUS_PROTECTED, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//        return false;
//    }
//
//    private static boolean processDecreasePhyDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, 0, 0, 0, skill.effect);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//        return false;
//    }
//
//    private static boolean processDecreaseMagicDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, 0, 0, skill.effect);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//        return false;
//    }
//
//    private static boolean processDecreasePhyMagicDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, -skill.parm1, 0, 0, skill.effect);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//        return false;
//    }
//
//    private static boolean processSorbPhyDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.setTarget(in, in.groupIndex);
//        in.target.setBufStatus(skill.effectBout, Skill.STATUS_SORB_ATTACK, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);
//
//        addBattleRecorder(battleRecorders, in, skill);
//        return false;
//    }
//
//    private static boolean processSorbMagicDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.setTarget(in, in.groupIndex);
//        in.target.setBufStatus(skill.effectBout, Skill.STATUS_SORB_MAGIC, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);
//
//        addBattleRecorder(battleRecorders, in, skill);
//        return false;
//    }
//
//    private static boolean processRestoreHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        int hpAdd = skill.parm1;
//
//        if(in.testMCri()){
//            hpAdd *= BattleSprite.CRI_RATE;
//            processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie, battleDataProcess);
//        }else{
//            processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//        }
//
//        in.target.changeHp(hpAdd, battleMovie, battleDataProcess);
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    private static boolean processRestoreAllHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        for(int i = 0; i < them.length; i++){
//            if(them[i] != null && !them[i].testCannotBattle()){
//                int hpAdd = skill.parm1;
//
//                in.setTarget(them[i], i);
//
//                if(in.testMCri()){
//                    hpAdd *= BattleSprite.CRI_RATE;
//                    processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie, battleDataProcess);
//                }else{
//                    processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//                }
//
//                them[i].changeHp(hpAdd, battleMovie, battleDataProcess);
//            }
//        }
//
//        for(int i = 0; i < themPet.length; i++){
//            if(themPet[i] != null && !themPet[i].testCannotBattle()){
//                int hpAdd = skill.parm1;
//
//                in.setTarget(themPet[i], i);
//
//                if(in.testMCri()){
//                    hpAdd *= BattleSprite.CRI_RATE;
//                    processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie, battleDataProcess);
//                }else{
//                    processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//                }
//
//                themPet[i].changeHp(hpAdd, battleMovie, battleDataProcess);
//            }
//        }
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    private static boolean processSaveLife(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        if(!in.target.testCannotBattle()){
//            processUnStatus(in, index, HIT_MISS, battleMovie, battleDataProcess);
//        }else{
//            in.target.reLive();
//
//            int hpAdd = skill.parm1;
//
//            if(in.testMCri()){
//                hpAdd *= BattleSprite.CRI_RATE;
//                processSaveLifeMovie(in, index, hpAdd, ATTACK_CRI, battleMovie, battleDataProcess);
//            }else{
//                processSaveLifeMovie(in, index, hpAdd, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//            }
//
//            in.target.changeHp(hpAdd, battleMovie, battleDataProcess);
//        }
//
//        addBattleRecorder(battleRecorders, in, skill);
//
//        return false;
//    }
//
//    private static boolean processAntiPhy(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_ATTACK, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//        addBattleRecorder(battleRecorders, in, skill);
//        return false;
//    }
//
//    private static boolean processAntiMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_MAGIC, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
//        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//        addBattleRecorder(battleRecorders, in, skill);
//        return false;
//    }
//
//    private static boolean processRestoreLotHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        int hpAdd = skill.parm1 * in.level / 100;
//
//        if(in.testMCri()){
//            hpAdd *= BattleSprite.CRI_RATE;
//            processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie, battleDataProcess);
//        }else{
//            processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//        }
//
//        in.target.changeHp(hpAdd, battleMovie, battleDataProcess);
//        addBattleRecorder(battleRecorders, in, skill);
//        return false;
//    }
//
//    private static boolean processClearStatusAndAnti(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, Vector battleRecorders,
//                    BattleDataProcess battleDataProcess){
//        if(!in.target.testCannotBattle() && in.target.getDebufStatus() != STATUS_NORMAL){
//            in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
//            processUnStatus(in, index, HIT_HIT, battleMovie, battleDataProcess);
//        }
//
//        if(skill.parm1 > 0 && getPercentRate(skill.parm1)){
//            in.target.setBufStatus(skill.parm2, Skill.STATUS_IMMUNITY_STATUS, 1, 0, 0, in.bsType, in.groupIndex);
//            processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie, battleDataProcess);
//        }
//        addBattleRecorder(battleRecorders, in, skill);
//        return false;
//    }
//
//    private static BattleSprite selectTargetRandom(BattleSprite src, BattleSprite[] them, BattleSprite[] themPet){
//        int idx = -1;
//        boolean allDie = true;
//        BattleSprite result = null;
//        BattleSprite aa = null;
//        for(int i = 0; i < them.length; i++){
//            if(them[i] == null){
//                continue;
//            }
//
//            if(!them[i].testCannotBattle()){
//                allDie = false;
//                aa = them[i];
//                break;
//            }
//        }
//
//        while(!allDie){
//            result = null;
//
//            if(aa.bsType != BattleSprite.TYPE_MONSTER || aa.getDebufStatus() == Skill.STATUS_CONFUSE){
//                if(random(0, 100) < 50){
//                    idx = random(0, them.length - 1);
//                    result = them[idx];
//                }else{
//                    idx = random(0, themPet.length - 1);
//                    result = themPet[idx];
//                }
//
//                if(result == null){
//                    continue;
//                }
//
//                if(!result.testCannotBattle()){
//                    break;
//                }
//            }else{
//                int max = 0;
//                for(int i = 0; i < them.length; i++){
//                    if(!them[i].testCannotBattle()){
//                        int e = them[i].getEnmity(src);
//                        if(e >= max){
//                            max = e;
//                            idx = i;
//                        }
//                    }
//                }
//                return them[idx];
//            }
//        }
//
//        return result;
//    }
//
    public static int[] makeMovieSub(int srcType, int srcIndex, int dstType, int dstIndex, int skillId, int movieType, int posistion, int overPosition, int movieSpeed, int hit, int status, int cri,
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
//
//    public static boolean chooseSkill(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie,
//                    BattleDataProcess battleDataProcess,int round){
//        if(!bs.canAction()){ //混乱状态特殊处理
//            if(bs.getDebufStatus() == Skill.STATUS_CONFUSE){
//                BattleSprite targetOur = selectTargetRandom(bs, our, ourPet);
//                BattleSprite targetThem = selectTargetRandom(bs, them, themPet);
//
//                if(targetOur != null && targetThem != null){
//                    bs.setSkill(ATTACK_SKILL);
//
//                    int ourCount = 0;
//                    int themCount = 0;
//
//                    for(int i = 0; i < our.length; i++){
//                        if(our[i] != null && !our[i].testCannotBattle()){
//                            ourCount++;
//                        }
//                    }
//
//                    for(int i = 0; i < ourPet.length; i++){
//                        if(ourPet[i] != null && !ourPet[i].testCannotBattle()){
//                            ourCount++;
//                        }
//                    }
//
//                    for(int i = 0; i < them.length; i++){
//                        if(them[i] != null && !them[i].testCannotBattle()){
//                            themCount++;
//                        }
//                    }
//
//                    for(int i = 0; i < themPet.length; i++){
//                        if(themPet[i] != null && !themPet[i].testCannotBattle()){
//                            themCount++;
//                        }
//                    }
//
//                    int tmp = ourCount * 100 / (ourCount + themCount);
//
//                    if(getPercentRate(tmp)){
//                        setTargetRandom(bs, our, ourPet);
//                    }else{
//                        setTargetRandom(bs, them, themPet);
//                    }
//
//                    return false;
//                }else{
//                    return true;
//                }
//            }else{
//                if(selectTargetRandom(bs, them, themPet) == null){
//                    return true;
//                }else{
//                    bs.setSkill(STAY_SKILL);
//
//                    return false;
//                }
//            }
//        }
//        boolean solidSkill = true;
//           boolean win = false;
//        if(bs.ai!=null)
//            win = bs.ai.action(bs,index,our,them,ourPet,themPet,round);
//        else{
//            while (true) {
//                int skillIndex = random(Skill.SOLID_SKILL_BEGIN,
//                                        bs.skillList.length - 1);
//
//                if (skillIndex >= 0) {
//                    int[] skillStatus = Skill.getSkillStatus(bs,
//                            bs.skillList[skillIndex]);
//
//                    if (skillStatus[0] == Skill.CANNOT_SELECT_SKILL) {
//                        continue;
//                    }
//
//                    bs.setSkill(Skill.getSkill(bs.skillList[skillIndex]));
//                } else {
//                    bs.setSkill(Skill.getSkill(skillIndex));
//                }
//
//                break;
//            }
////
//
//
//            switch (bs.skill.id) {
//                case SKILL_ATTACK:
//                    win = setTargetRandom(bs, them, themPet);
//
//                    break;
//                case SKILL_ITEM:
//                    win = setTargetRandom(bs, them, themPet);
//
//                    break;
//                case SKILL_RUN:
//                    bs.setTarget(null, -1);
//
//                    break;
//                case SKILL_STAY:
//                    bs.setTarget(null, -1);
//
//                    break;
//                default:
//                    solidSkill = false;
//
//                    break;
//            }
//
//            if (solidSkill || win) {
//                return win;
//            }
//        }
//
//        Skill skill = bs.skill;
//
//        switch(skill.effect){
//            case Skill.EFFECT_MULTI_ATK:
//                if(skill.parm2 == Skill.MULTI_ATTACK_RANDOM){
//                    bs.setTarget(null, -1);
//                }else{
//                    win = setTargetRandom(bs, them, themPet);
//                }
//
//                break;
//            case EFFECT_INC_ATK_INC_DMG:
//            case EFFECT_ANTI_BUF_INC_ATK:
//            case EFFECT_FAINT:
//            case EFFECT_INC_ATK_INC_CRI:
//            case EFFECT_INC_DMG_DEC_HIT:
//            case EFFECT_INC_ATK_STOP:
//                win = setTargetRandom(bs, them, themPet);
//
//                break;
//            case EFFECT_ALWAYS_INC_ATK_INC_DMG:
//                bs.setTarget(null, -1);
//
//                break;
//            case EFFECT_MGC_ATK:
//                win = setTargetRandom(bs, them, themPet);
//
//                break;
//            case EFFECT_MULTI_MGC:
//                if(skill.parm2 == Skill.MULTI_ATTACK_RANDOM){
//                    bs.setTarget(null, -1);
//                }else{
//                    win = setTargetRandom(bs, them, themPet);
//                }
//
//                break;
//            case EFFECT_LET_POISON:
//            case EFFECT_LET_SLEEP:
//            case EFFECT_LET_STONE:
//            case EFFECT_MAGIC_ALL:
//            case EFFECT_LET_CONFUSE:
//                win = setTargetRandom(bs, them, themPet);
//
//                break;
//            case EFFECT_INC_MGC_LET_POSION:
//            case EFFECT_MGC_USE_ALL_MP:
//                bs.setTarget(null, -1);
//
//                break;
//            case EFFECT_DEC_ATK_INC_FLEE:
//            case EFFECT_BLOCK_ATK_DEC_DMG:
//            case EFFECT_DEC_PHY_DMG:
//            case EFFECT_DEC_MGC_DMG:
//            case EFFECT_DEC_PHY_MGC_DMG:
//            case EFFECT_PHY_DMG_TO_HP:
//            case EFFECT_MGC_DMG_TO_HP:
//            case EFFECT_RESTORE_HP:
//                setTargetRandom(bs, our, ourPet);
//
//                break;
//            case EFFECT_RESTORE_ALL_HP:
//                bs.setTarget(null, -1);
//
//                break;
//            case EFFECT_SAVE_LIFE:
//                setTargetRandom(bs, our, ourPet);
//
//                break;
//            case EFFECT_ANTI_PHY:
//            case EFFECT_ANTI_MGC:
//            case EFFECT_RESTORE_LOT_HP:
//            case EFFECT_CLEAR_STS_AND_ANTI:
//                setTargetRandom(bs, our, ourPet);
//
//                break;
//        }
//
//        return win;
//    }
//
//    public static boolean doPoisonFrost(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie,
//                    BattleDataProcess battleDataProcess){
//        int[] movie;
//
//        if(bs.getDebufStatus() == Skill.STATUS_POISON){
//            int[] bufInfo = bs.getDebufInfo();
//
//            int protectSrcType = bufInfo[5];
//            int protectSrcIndex = bufInfo[6];
//
//            BattleSprite src = battleDataProcess.getSprite(protectSrcType, protectSrcIndex);
//
//            int hpDec = src.level * bufInfo[3] / 100;
//
//            if(hpDec <= 0){
//                hpDec = -1;
//            }else{
//                hpDec = -hpDec;
//            }
//
//            bs.changeHp(hpDec, battleMovie, battleDataProcess);
//            movie = makeMovieSub(bs.bsType, index, bs.bsType, index, SKILL_LIFE_MAGIC, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, bs.getDebufStatus(), ATTACK_NO_CRI,
//                            hpDec, 0, 0, 0);
//            battleMovie.addElement(movie);
//
//            if(bs.testDie()){
//                if(selectTargetRandom(bs, our, ourPet) == null){
//                    return true;
//                }else{
//                    return false;
//                }
//            }
//        }else if(bs.getDebufStatus() == Skill.STATUS_FROST){
//            int[] bufInfo = bs.getDebufInfo();
//
//            int protectSrcType = bufInfo[5];
//            int protectSrcIndex = bufInfo[6];
//
//            BattleSprite src = battleDataProcess.getSprite(protectSrcType, protectSrcIndex);
//
//            int mpDec = src.level * bufInfo[3] / 100;;
//
//            if(mpDec <= 0){
//                mpDec = -1;
//            }else{
//                mpDec = -mpDec;
//            }
//
//            bs.changeMp(mpDec);
//            movie = makeMovieSub(bs.bsType, index, bs.bsType, index, SKILL_LIFE_MAGIC, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, bs.getDebufStatus(), ATTACK_NO_CRI,
//                            0, mpDec, 0, 0);
//            battleMovie.addElement(movie);
//        }
//
//        //判断是否战斗已结束
//        if(selectTargetRandom(bs, them, themPet) == null){
//            return true;
//        }
//
//        return false;
//    }
//
//    private static boolean setTargetRandom(BattleSprite bs, BattleSprite[] them, BattleSprite[] themPet){
//        boolean win = false;
//        BattleSprite result = selectTargetRandom(bs, them, themPet);
//
//        if(result != null){
//            if(bs.target == null){
//                bs.setTarget(result, result.groupIndex);
//            }
//        }else{
//            win = true;
//        }
//
//        return win;
//    }
//
//    private static boolean testTargetRandom(BattleSprite bs, BattleSprite[] them, BattleSprite[] themPet){
//        boolean win = false;
//        BattleSprite tmpSprite = selectTargetRandom(bs, them, themPet);
//
//        if(tmpSprite == null){
//            win = true;
//        }
//
//        return win;
//    }
//
    public static String getSkillName(BattleSprite bs, int skillId, byte showMpUseType, boolean showLevel){
        Skill skill = getSkill(skillId);
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
//
//    public static String getAnimateName(int animateType){
//        return animateName[animateType];
//    }
//
//    public static String getStatusName(int status){
//        return statusName[status];
//    }
//
//    /**
//     * int[4] [0]是否可选 [1]冷却回合 [2]魔法不足 [3]目标选择
//     */
    public static int[] getSkillStatus(BattleSprite bs, int skillId){
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
        //mengjie modify
        short mpuse_tmp=skill.getMpUse(bs);
        if(mpuse_tmp > bs.mp){
            mpGap = mpuse_tmp - bs.mp;
            status[0] = Skill.CANNOT_SELECT_SKILL;
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
                status[3] = Skill.CHOOSE_OWNER;

                break;
            case EFFECT_DEFENCE:
                status[3] = Skill.CHOOSE_NONE;
                
                break;
        }

        return status;
    }
//
    public static void addSkills(){

        Ability[] abilities = Ability.getAbilitites();
        for(int i = 0; i < abilities.length; i++){
            byte skillType = abilities[i].getType();
            String skillName = abilities[i].getName();
            byte skillEffect = (byte)abilities[i].getEffect();
            byte skillStatus = (byte)abilities[i].getStatus();
            byte skillPosition = (byte)abilities[i].getPosition();
            byte skillCDID = (byte)abilities[i].getCD();
            byte skillCDBout = (byte)abilities[i].getCDTime();
            byte learnLevel = (byte)abilities[i].getRequiredLevel();
            Skill skill;
            int id;
            for(int j = 0; j < learnLevel; j++){
                skill = new Skill(skillType, skillName, skillEffect, skillStatus, skillPosition, skillCDID, skillCDBout);
                id = skill.initParm(abilities[i]);
                allSkill.put(new Integer(id), skill);
            }

        }
        allSkill.put(new Integer(ATTACK_SKILL.id), ATTACK_SKILL);
        allSkill.put(new Integer(ITEM_SKILL.id), ITEM_SKILL);
        allSkill.put(new Integer(RUNAWAY_SKILL.id), RUNAWAY_SKILL);
        allSkill.put(new Integer(CATCH_SKILL.id), CATCH_SKILL);
        allSkill.put(new Integer(STAY_SKILL.id), STAY_SKILL);
        allSkill.put(new Integer(NONE_SKILL.id),NONE_SKILL);
        allSkill.put(new Integer(NOTREADY_SKILL.id),NOTREADY_SKILL);
    }
//
    public static Random randGen = new Random();

    public static boolean getPercentRate(int percent){
        int ran = randGen.nextInt(100);

        if(ran <= percent){
            return true;
        }else{
            return false;
        }
    }

    public static int random(int min, int max){
        int value = randGen.nextInt() % (max - min + 1);
        if(value < 0){
            value = -value;
        }
        return min + value;
    }
}
