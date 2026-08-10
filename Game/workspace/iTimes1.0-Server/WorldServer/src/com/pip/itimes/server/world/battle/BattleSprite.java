package com.pip.itimes.server.world.battle;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.pip.itimes.server.bean.Master;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.ai.Ai90016_1;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.DiamondShineBuf;
import com.pip.itimes.server.stage.EquipmentTemplate;
import com.pip.itimes.server.stage.EvolutionData;
import com.pip.itimes.server.stage.EvolutionLoader;
import com.pip.itimes.server.stage.MagicPosMessage;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.Monster;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.suit.SuitEffect;
import com.pip.itimes.server.util.Utils;


public class BattleSprite{
    public byte bsType;
    public int groupIndex;
    
    public int groupId;
    public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public static final byte train_attack = 0;
	public static final byte train_pdef = 1;
	public static final byte train_mattack = 2;
	public static final byte train_mdef = 3;
	public static final byte train_hit = 4;
	public static final byte train_nocri = 5;
	
	public static final byte magic_water = 0;
	public static final byte magic_soil = 1;
	public static final byte magic_fire = 2;
	public static final byte magic_wind= 3;
	public static final byte magic_mind = 4;
	
	public static final byte ATTR_STR = 0;
    public static final byte ATTR_AGI = 1;
    public static final byte ATTR_VIT = 2;
    public static final byte ATTR_INT = 3;
    public static final byte ATTR_PMIN = 4;
    public static final byte ATTR_PMAX = 5;
    public static final byte ATTR_PDEF = 6;
    public static final byte ATTR_MMIN = 7;
    public static final byte ATTR_MMAX = 8;
    public static final byte ATTR_MDEF = 9;
    public static final byte ATTR_PHIT = 10;
    public static final byte ATTR_MHIT = 11;
    public static final byte ATTR_FLEE = 12;
    public static final byte ATTR_PCRI = 13;
    public static final byte ATTR_MCRI = 14;
    public static final byte ATTR_HPMAX = 15;
    public static final byte ATTR_MPMAX = 16;
    /**
     * 免爆
     */
    public static final byte ATTR_NOCRI = 17;

    public int[] attributes = new int[18];

    public static final String[] ATTR_NAMES = new String[]{
                    "力量", "敏捷", "体力", "智力", "攻击", "攻击", "防御", "魔攻", "魔攻", "魔防", "命中等级", "魔命等级", "闪躲等级", "暴击等级", "魔暴等级", "生命", "魔法", "免爆"
    };

    public byte face;
    public int id;
    public short level; //等级
    //    protected short strength; //力量
    //    protected short agility; //敏捷
    //    protected short vitality; //体力
    //    protected short intelligence; //智力
    protected long luck; //幸运
    public int hp; //剩余生命
    public int mp; //剩余魔法
    public int usedMp; //本次出招所消耗mp

    //    protected int pattackMin; //物理攻击力下限
    //    protected int pattackMax; //物理攻击力上限
    //    protected int pDefence; //物理防御力
    //    protected int mAttackMin; //魔法攻击力下限
    //    protected int mAttackMax; //魔法攻击力上限
    //    protected int mDefence; //魔法防御力
    //    protected int phit; //物理命中值
    //    protected int mhit; //魔法命中值
    //    protected int flee; //闪避值
    //    protected int pcri; //物理暴击率
    //    protected int mcri; //魔法暴击率
    //    protected int hpLimit; //生命值上限
    //    protected int mpLimit; //魔法值上限

    protected int hpShow;
    protected int mpShow;

    protected int debufStatus; //当前有害状态
    protected int bufStatus; //当前有益状态

    protected int weaponAttack; //武器攻击
    protected int equipDefence; //装备防御

    protected int attackAdd; //攻击力加成
    protected int magicAttackAdd; //魔法攻击力加成
    protected int defenceAdd; //防御力加成
    protected int magicDefenceAdd; //魔法防御力加成
    protected int hitAdd; //命中值加成
    protected int fleeAdd; //躲闪值加成
	protected int criRateAdd; //暴击率加成
    protected int phyDamageAdd; //受物理伤害加成
    protected int mgcDamageAdd; //受魔法伤害加成
    protected int letPhyDamageAdd; //增加固定物理攻击伤害数值
    protected int letMgcDamageAdd; //增加固定物理攻击伤害数值
    protected int attackMaxAdd;		//增加攻击力上限
    protected int defenceMaxAdd;	//增加防御力上限
    protected int skillparm1;	//技能参数1
    protected int skillparm2;	//技能参数2

    public int lastmissflag;
//    public int lastmissflagpet;
    public short[] skillList; //技能id列表
//    protected int skillId; //使用的技能id
    public Skill skill;

    public BattleSprite target; //目标
    public int targetIndex; //目标索引下标
    public int targetType; //目标类型 0=无选择 1=队友 2=敌人
    public IItem usedItem;
    public boolean used;
    public int lastUsedRound = 0;
    public int usedTimes = 0;

    public Hashtable bufTable = new Hashtable(); //buf列表 int[] [0] 剩余回合 [1] 状态 attackAdd [2] 状态级别 magicAttackAdd [3] 状态参数1 defenceAdd [4] 状态参数2 magicDefenceAdd [5] hitAdd [6] fleeAdd [7] criAdd [8]statusAdd
    private static Integer statusIndexDebuf = new Integer(0); //有害buf编号
    private static Integer statusIndexBuf = new Integer(1); //有益buf编号
    private int statusIndexAttrBuf = 2; //属性buf起始编号

    private Hashtable coolDownTable = new Hashtable(); //冷却列表

    public boolean ready = false;

    public static final byte FRAMESEQUENCE_STAND = 0;
    public static final byte FRAMESEQUENCE_RUN = 1;
    public static final byte FRAMESEQUENCE_ATTACK = 2;
    public static final byte FRAMESEQUENCE_BEATED = 3;
    public static final byte FRAMESEQUENCE_RUNBACK = 4;
    public static final byte FRAMESEQUENCE_DIE = 5;

    private Hashtable enmities = new Hashtable(3);

    //    public Values values = new Values();

    public IMonsterAI ai = null;

    public BattleSprite catchedPet = null;
    public int idleRound = 0;

    public BattleSuitEffect[] battleSuitEffect = null;
    
    //Added by leo for Control Skill Decreasing
    public int controlDecreasePercent = 0;
    public int controlDecreaseBout = 0;
    //Added end
    
    //Added by leo to record monster die
    public int dieRound = 999999;
    public int hurted = 0;

    public static final byte[][] EFFECTSEQUENCE_LEFT = new byte[][]{
                    //0:物理攻击效果序列
                    {
                                    16, 16, 17, 17, 16, 16, 17, 17
                    },
                    //1:魔法攻击效果序列
                    {
                                    14, 14, 15, 15
                    },
                    //2:状态攻击效果序列
                    {
                                    17, 20, 21, 19, 18, 16, 16
                    },
                    //3:补血增益效果序列
                    {
                                    7, 7, 6, 6
                    },
                    //4:复活效果序列
                    {
                                    0, 0, 1, 1
                    },
                    //5:解除效果序列
                    {
                                    4, 4, 5, 5
                    },
                    //6:狂战起手效果序列
                    {
                                    14, 14, 15, 15
                    },
                    //7:魔战起手效果序列
                    {
                                    13, 13, 12, 12
                    },
                    //8:盾防起手效果序列
                    {
                                    2, 2, 3, 3
                    },
                    //9:辅助起手效果序列
                    {
                                    10, 10, 11, 11
                    },
                    //10:物品使用效果序列
                    {

                                    8, 8, 9, 9
                    }
    };

    public static final byte[][] EFFECTSEQUENCE_RIGHT = EFFECTSEQUENCE_LEFT;
    /*new byte[][]{
     //0:物理攻击效果序列
     {
     16, 16, 17, 17, 16, 16, 17, 17
     },
     //1:魔法攻击效果序列
     {
     24
     },
     //2:状态攻击效果序列
     {
     29, 26, 26, 27, 28, 28, 27
     },
     //3:补血增益效果序列
     {
     10, 10, 11, 11, 11, 11, 10, 10
     },
     //4:复活效果序列
     {
     10, 10, 11, 11, 11, 11, 10, 10
     },
     //5:解除效果序列
     {
     10, 10, 11, 11, 11, 11, 10, 10
     },
     //6:狂战起手效果序列
     {
     0, 0, 1, 1, 0, 0, 1, 1
     , 0, 0, 1, 1, 0, 0, 1, 1
     },
     //7:魔战起手效果序列
     {
     2, 2, 3, 3, 2, 2
     , 31, 31, 30, 30, 31, 31
     },
     //8:盾防起手效果序列
     {
     4, 4, 5, 5, 4, 4
     },
     //9:辅助起手效果序列
     {
     6, 6, 7, 7, 7, 7, 6, 6
     },
     //10:物品使用效果序列
     {}
     };*/

    public byte[] effSeq;

    public short effX;

    public short effY;

    //-------------
    public short battleX, battleY;

    public byte index;

    public byte frame;

    public byte weaponIndex;

    public byte weaponFrame;

    public boolean showHp = true;

    public boolean showDie = false;

    public boolean showName = true;

    public boolean show = true;

    /**
     * 位置 1=上 2=中 3=下
     */
    public byte localIndex;

    public byte[] frameSequence;

    public byte[] weaponFrameSequence;

    //------------------

    public static final byte CRI_RATE = 2;

    public static final byte ACTION_PATTACK = 0;
    public static final byte ACTION_MATTACK = 1;

    public static final byte TYPE_PLAYER = 0;
    public static final byte TYPE_MONSTER = 1;
    public static final byte TYPE_NET_PLAYER = 2;
    public static final byte TYPE_PLAYER_PET = 3;
    public static final byte TYPE_MONSTER_PET = 4;
    public static final byte TYPE_NPC = 5;
    public static final byte TYPE_BATTLE = 6;
    public static final byte TYPE_INTERVENE = 7;

    public static final byte GROUP_OUR = 0;
    public static final byte GROUP_THEM = 1;
    public static final byte GROUP_OUR_PET = 2;
    public static final byte GROUP_THEM_PET = 3;

    public String name;

    public IPlayerData player;
    public Monster monster;
    public Pet pet;
    public EquipmentTemplate weapon;
    
    private boolean immuneControl = false;			//免疫控制 默认为无效
    
    private int vianyType = 0;			//属性攻属性
    private int vianyAttack = 0;		//属性攻攻击值
    private int vianyDefense = 0;		//属性攻防御值
    
    private int evaValue = 0;			//鉴定加成率
    private int stoneValue = 0;			//宝石加成率
    private int[] suitStone = null;		//套装宝石
    private int[] trainpoint = null;	//聚灵等级属性加成
    private int [] trainlevelstone = null;//聚灵等级宝石加成
    private int [] magicposlevel = null;//封印法阵等级
    private int [] magicposfloor = null;//阶层
    
    public static final byte SEAL_SKILL_ATTACK = (byte) 0x1; //人宠
    public static final byte SEAL_SKILL_SKILL = (byte) 0x2; //人宠
    public static final byte SEAL_SKILL_ITEM = (byte) 0x4; //人
    public static final byte SEAL_SKILL_CATCH = (byte) 0x8; //人
    public static final byte SEAL_SKILL_RUNAWAY = (byte) 0x10; //人
    public static final byte SEAL_SKILL_DEF = (byte) 0x20; //宠
    
    private short status = 0xFF;
	public int lastmissflagpet;
	
	//援护二代
	private List<BattleSprite> interveneAgeII = new ArrayList<BattleSprite>();
	//援护一代
	private List<BattleSprite> interveneAgeI = new ArrayList<BattleSprite>();
	//是否属于援护
	private boolean isIntervene = false;
	//援护使用的次数
	private int interveneTime = 0;
	//援护使用的最大次数
	private int interveneTimeMax = 0;
	//援护技能
	private Ability[] interveneAbility;
	//一次攻击触发援护次数
	public int interveneAttackTime = 0;
	//一次攻击触发援护的最大次数
	public static final int interveneAttackTimeMax = 1;
	
	//神圣宝辉减防
	protected int defEffect_curTime=0; //时间
	protected int defEffect_curRate=0; //减防百分比
	protected int defEffect_curIndex = 0; //当前对应的减防数组索引
	public static final int DEFEFFECT_MAXLEVEL = 10; //减防最大叠加层数
	public static final int DEFEFFECT_TIME = 4;//持续4回合
	public static final int[] DEFEFFECT_RATE = {1,3,4,5,6}; //减防百分比数组
	
	
	public int getCurDefEffectTime(){
		return defEffect_curTime;
	}
	
	public void setCurDefEffectTime(int time){
		defEffect_curTime = time;
	}
	
	public int getCurDefEffectRate(){
		return defEffect_curRate;
	}
	
	public void setCurDefEffectRate(int rate){
		defEffect_curRate = rate;
	}
	
	public int getCurDefEffectIndex(){
		return defEffect_curIndex;
	}
	
	public void setCurDefEffectIndex(int index){
		defEffect_curIndex = index;
	}
	
	public boolean isImmuneControl(){
		return immuneControl;
	}
	public void setImmuneControl(boolean immuneControl){
		this.immuneControl = immuneControl;
	}
	
	private byte isCatch = 0;		// 判断怪是否被抓

    public byte getIsCatch() {
		return isCatch;
	}

	public void setIsCatch(byte isCatch) {
		this.isCatch = isCatch;
	}

	public void setStatus(byte type,boolean b){
        if(b){
            status |= type;
        }else{
            status &= (~type);
        }
    }
	
	public void setVianyType(int vianyType){
		this.vianyType = vianyType;
	}
	
	public int getVianyType(){
		return vianyType;
	}
	
	public void setVianyAttack(int vianyAttack){
		this.vianyAttack = vianyAttack;
	}
	
	public int getVianyAttack(){
		return vianyAttack;
	}
	
	public void setVianyDefense(int vianyDefense){
		this.vianyDefense = vianyDefense;
	}
	
	public int getVianyDefense(){
		return vianyDefense;
	}
    
    public BattleSuitEffect[] splitePetEffect(){
        BattleSuitEffect[] result = null;
        
        if(battleSuitEffect == null){
            return null;
        }
        
        Vector<BattleSuitEffect> playerEffects = new Vector<BattleSuitEffect>();
        Vector<BattleSuitEffect> petEffects = new Vector<BattleSuitEffect>();
        
        for(int i = 0; i < battleSuitEffect.length; i++){
            if(battleSuitEffect[i].getWay() == SuitEffect.EFFECT_WAY_PET_ONE || battleSuitEffect[i].getWay() == SuitEffect.EFFECT_WAY_PET_ALL){
                petEffects.add(battleSuitEffect[i]);
            }else{
                playerEffects.add(battleSuitEffect[i]);
            }
        }
        
        if(playerEffects.size() > 0){
            battleSuitEffect = new BattleSuitEffect[playerEffects.size()];
            playerEffects.copyInto(battleSuitEffect);
        }else if(bsType != TYPE_INTERVENE){
            battleSuitEffect = null;
        }
        
        if(petEffects.size() > 0){
            result = new BattleSuitEffect[petEffects.size()];
            petEffects.copyInto(result);
        }
        
        return result;
    }
    
    public void processSuitEffect(BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, BattleDataProcess battleDataProcess){
        if(battleSuitEffect == null){
            return;
        }
        
        if(!testCannotBattle()){
            /**
             * 修改套装属性不可叠加
             */
            //处理混乱
            int effectConfuseBout = 0;
            for(int i = 0;i < battleSuitEffect.length; i++){
                BattleSuitEffect effect = battleSuitEffect[i];
                if(!effect.effectStatus(this, our, ourPet, them, themPet)){
                    continue;
                }
                if(effect.getType() == SuitEffect.EFFECT_TYPE_LET_CONFUSE && target != null && !target.testCannotBattle() && target.getDebufStatus() != Skill.STATUS_CONFUSE && (target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !target.isImmuneControl())){
                    if(testControlHit()){
                        if(effect.getBout() > effectConfuseBout){
                            effectConfuseBout = effect.getBout();
                        }
                        effect.doEffect();
                    }
                }
            }
            if(effectConfuseBout > 0){
                target.setDeBufStatus(effectConfuseBout, Skill.STATUS_CONFUSE, 1, 0, 0, bsType, groupIndex);
                int[] movie = Skill.makeMovieSub(target.bsType, target.groupIndex, target.bsType, target.groupIndex, Skill.SKILL_UPDATE_STATUS, BattleStrategy.ANIMATE_NONE, BattleStrategy.POSITION_STAY, BattleStrategy.OVER_POSITION_BACK, BattleStrategy.MOVIE_SPEED_FAST, BattleStrategy.HIT_HIT, Skill.STATUS_CONFUSE, BattleStrategy.ATTACK_NO_CRI, 0, 0, 0, 0);
                battleMovie.addElement(movie);
                controlHit();
            }
            
            //处理眩晕
            int effectFaintBout = 0;
            for(int i = 0;i < battleSuitEffect.length; i++){
                BattleSuitEffect effect = battleSuitEffect[i];
                if(!effect.effectStatus(this, our, ourPet, them, themPet)){
                    continue;
                }
                if(effect.getType() == SuitEffect.EFFECT_TYPE_LET_CONFUSE && target != null && !target.testCannotBattle() && target.getDebufStatus() != Skill.STATUS_FAINT && (target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !target.isImmuneControl())){
                    if(effect.getBout() > effectFaintBout){
                        effectFaintBout = effect.getBout();
                    }
                    effect.doEffect();
                }
            }
            if(effectFaintBout > 0){
                target.setDeBufStatus(effectFaintBout, Skill.STATUS_FAINT, 1, 0, 0, bsType, groupIndex);
                int[] movie = Skill.makeMovieSub(target.bsType, target.groupIndex, target.bsType, target.groupIndex, Skill.SKILL_UPDATE_STATUS, BattleStrategy.ANIMATE_NONE, BattleStrategy.POSITION_STAY, BattleStrategy.OVER_POSITION_BACK, BattleStrategy.MOVIE_SPEED_FAST, BattleStrategy.HIT_HIT, Skill.STATUS_FAINT, BattleStrategy.ATTACK_NO_CRI, 0, 0, 0, 0);
                battleMovie.addElement(movie);
            }
            
            //处理霜冻
            int effectFrostBout = 0;
            int effectFrostValue = 0;
            for(int i = 0;i < battleSuitEffect.length; i++){
                BattleSuitEffect effect = battleSuitEffect[i];
                if(!effect.effectStatus(this, our, ourPet, them, themPet)){
                    continue;
                }
                if(effect.getType() == SuitEffect.EFFECT_TYPE_LET_FROST && target != null && !target.testCannotBattle() && target.getDebufStatus() != Skill.STATUS_FROST && (target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !target.isImmuneControl())){
                    if(effect.getBout() > effectFrostBout){
                        effectFrostBout = effect.getBout();
                        effectFrostValue = effect.getValue();
                    }
                    effect.doEffect();
                }
            }
            if(effectFrostBout > 0){
                target.setDeBufStatus(effectFrostBout, Skill.STATUS_FROST, 1, effectFrostValue, 0, bsType, groupIndex);
                int[] movie = Skill.makeMovieSub(target.bsType, target.groupIndex, target.bsType, target.groupIndex, Skill.SKILL_UPDATE_STATUS, BattleStrategy.ANIMATE_NONE, BattleStrategy.POSITION_STAY, BattleStrategy.OVER_POSITION_BACK, BattleStrategy.MOVIE_SPEED_FAST, BattleStrategy.HIT_HIT, Skill.STATUS_FROST, BattleStrategy.ATTACK_NO_CRI, 0, 0, 0, 0);
                battleMovie.addElement(movie);
            }
            
            //处理中毒
            int effectPoisonBout = 0;
            int effectPoisonValue = 0;
            for(int i = 0;i < battleSuitEffect.length; i++){
                BattleSuitEffect effect = battleSuitEffect[i];
                if(!effect.effectStatus(this, our, ourPet, them, themPet)){
                    continue;
                }
                if(effect.getType() == SuitEffect.EFFECT_TYPE_LET_POSION && target != null && !target.testCannotBattle() && target.getDebufStatus() != Skill.STATUS_POISON && (target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !target.isImmuneControl())){
                    if(effect.getBout() > effectPoisonBout){
                        effectPoisonBout = effect.getBout();
                        effectPoisonValue = effect.getValue();
                    }
                    effect.doEffect();
                }
            }
            if(effectPoisonBout > 0){
                target.setDeBufStatus(effectPoisonBout, Skill.STATUS_POISON, 1, effectPoisonValue * 100, 0, bsType, groupIndex);
                int[] movie = Skill.makeMovieSub(target.bsType, target.groupIndex, target.bsType, target.groupIndex, Skill.SKILL_UPDATE_STATUS, BattleStrategy.ANIMATE_NONE, BattleStrategy.POSITION_STAY, BattleStrategy.OVER_POSITION_BACK, BattleStrategy.MOVIE_SPEED_FAST, BattleStrategy.HIT_HIT, Skill.STATUS_POISON, BattleStrategy.ATTACK_NO_CRI, 0, 0, 0, 0);
                battleMovie.addElement(movie);
            }
            
            //处理昏睡
            int effectSleepBout = 0;
            for(int i = 0;i < battleSuitEffect.length; i++){
                BattleSuitEffect effect = battleSuitEffect[i];
                if(!effect.effectStatus(this, our, ourPet, them, themPet)){
                    continue;
                }
                if(effect.getType() == SuitEffect.EFFECT_TYPE_LET_SLEEP && target != null && !target.testCannotBattle() && target.getDebufStatus() != Skill.STATUS_SLEEP && (target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !target.isImmuneControl())){
                    if(testControlHit()){
                        if(effect.getBout() > effectSleepBout){
                            effectSleepBout = effect.getBout();
                        }
                        effect.doEffect();
                    }
                }
            }
            if(effectSleepBout > 0){
                target.setDeBufStatus(effectSleepBout, Skill.STATUS_SLEEP, 1, 0, 0, bsType, groupIndex);
                int[] movie = Skill.makeMovieSub(target.bsType, target.groupIndex, target.bsType, target.groupIndex, Skill.SKILL_UPDATE_STATUS, BattleStrategy.ANIMATE_NONE, BattleStrategy.POSITION_STAY, BattleStrategy.OVER_POSITION_BACK, BattleStrategy.MOVIE_SPEED_FAST, BattleStrategy.HIT_HIT, Skill.STATUS_SLEEP, BattleStrategy.ATTACK_NO_CRI, 0, 0, 0, 0);
                battleMovie.addElement(movie);
                controlHit();
            }
            
            //处理石化
            int effectStoneBout = 0;
            int effectStoneValue = 0;
            for(int i = 0;i < battleSuitEffect.length; i++){
                BattleSuitEffect effect = battleSuitEffect[i];
                if(!effect.effectStatus(this, our, ourPet, them, themPet)){
                    continue;
                }
                if(effect.getType() == SuitEffect.EFFECT_TYPE_LET_STONE && target != null && !target.testCannotBattle() && target.getDebufStatus() != Skill.STATUS_STONE && (target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !target.isImmuneControl())){
                    if(testControlHit()){
                        if(effect.getBout() > effectStoneBout){
                            effectStoneBout = effect.getBout();
                            effectStoneValue = effect.getValue();
                        }
                        effect.doEffect();
                    }
                }
            }
            if(effectStoneBout > 0){
                target.setDeBufStatus(effectStoneBout, Skill.STATUS_STONE, 1, 90, effectStoneValue, bsType, groupIndex);
                int[] movie = Skill.makeMovieSub(target.bsType, target.groupIndex, target.bsType, target.groupIndex, Skill.SKILL_UPDATE_STATUS, BattleStrategy.ANIMATE_NONE, BattleStrategy.POSITION_STAY, BattleStrategy.OVER_POSITION_BACK, BattleStrategy.MOVIE_SPEED_FAST, BattleStrategy.HIT_HIT, Skill.STATUS_STONE, BattleStrategy.ATTACK_NO_CRI, 0, 0, 0, 0);
                battleMovie.addElement(movie);
                controlHit();
            }
            
            //处理停行
            int effectStopBout = 0;
            for(int i = 0;i < battleSuitEffect.length; i++){
                BattleSuitEffect effect = battleSuitEffect[i];
                if(!effect.effectStatus(this, our, ourPet, them, themPet)){
                    continue;
                }
                if(effect.getType() == SuitEffect.EFFECT_TYPE_LET_STOP && target != null && !target.testCannotBattle() && target.getDebufStatus() != Skill.STATUS_STOP && (target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS && !target.isImmuneControl())){
                    if(effect.getBout() > effectStopBout){
                        effectStopBout = effect.getBout();
                    }
                    effect.doEffect();
                }
            }
            if(effectStopBout > 0){
                target.setDeBufStatus(effectStopBout, Skill.STATUS_STOP, 1, 0, 0, bsType, groupIndex);
                int[] movie = Skill.makeMovieSub(target.bsType, target.groupIndex, target.bsType, target.groupIndex, Skill.SKILL_UPDATE_STATUS, BattleStrategy.ANIMATE_NONE, BattleStrategy.POSITION_STAY, BattleStrategy.OVER_POSITION_BACK, BattleStrategy.MOVIE_SPEED_FAST, BattleStrategy.HIT_HIT, Skill.STATUS_STOP, BattleStrategy.ATTACK_NO_CRI, 0, 0, 0, 0);
                battleMovie.addElement(movie);
            }
        }
    }
    
    public boolean getStatus(byte type){
        return (status&type)!=0;
    }

    public void initBattleData(byte bsType, int level, int vitality, int strength, int intelligence, int agility, long luck, int hp, int mp, int viany,
    		int evaValue, int stoneValue, int[] suitStone, int[] trainLevel,int[] trainLevelStone,int[]magicposlevel,int[]magicposfloor){
        this.bsType = bsType;
        this.level = (short)level;
        this.evaValue = evaValue;
        this.stoneValue = stoneValue;
        this.suitStone = suitStone;
        this.trainpoint = trainLevel;
        this.trainlevelstone = trainLevelStone;
        this.magicposlevel = magicposlevel;
        this.magicposfloor = magicposfloor;
        //        this.strength = (short)strength;
        //        this.agility = (short)agility;
        //        this.vitality = (short)vitality;
        //        this.intelligence = (short)intelligence;
        if(bsType == BattleSprite.TYPE_MONSTER || bsType == BattleSprite.TYPE_MONSTER_PET){
        	//attributes[ATTR_STR] = (short)strength * 70 / 100;
        	attributes[ATTR_STR] = (short)strength * 50 / 100;
            attributes[ATTR_AGI] = (short)agility * 50 / 100;
        }else{
        	attributes[ATTR_STR] = (short)strength;
            attributes[ATTR_AGI] = (short)agility;
        }
        
        attributes[ATTR_VIT] = (short)vitality ;
        attributes[ATTR_INT] = (short)intelligence;
        this.luck = luck;
        this.hp = hp;
        this.mp = mp;
        
        this.vianyType = viany;
        this.interveneAttackTime = 0;
        initAttribute();

        setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);
        setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);
        

    }
    
    public void initIntervene(IPlayerData player, BattleSprite sprite){
    	if(BattleIntervene.open){
	    	Pet[] pets = player.getOetherPet();
	    	if(pets != null && pets.length > 0){
	    		List<Pet> listPet = new ArrayList<Pet>(pets.length);
	    		for(int i=0; i<pets.length; i++){
	    			if(listPet.size() == 0){
	    				listPet.add(pets[i]);
	    			}else{
	    				boolean insert = false;
	    				for(int index=0; index < listPet.size(); index ++){
	    					Pet lp = listPet.get(index);
	    					if(pets[i].getPerceptionLevel() > lp.getPerceptionLevel()){
	    						listPet.add(index, pets[i]);
	    						insert = true;
	    						break;
	    					}
	    				}
	    				if(!insert){
	    					listPet.add(pets[i]);
	    				}
	    			}
	    		}
	    		for(int i=0; i<listPet.size(); i++){
	    			Pet pet = listPet.get(i);
	    			BattleSprite bs = new BattleSprite();
	    			bs.initBattleData(BattleSprite.TYPE_INTERVENE, pet.getLevel(), pet.getRealVitality(), 
	    					pet.getRealStrength(), pet.getRealIntelligence(), 
	    					pet.getRealAgility(), player.getLuck(), 
	    					pet.getMaxHp(), pet.getMaxMp(), player.getVianyType(), 0, 0, null,null,null,null,null);
	    			bs.id = pet.getId();
	    			bs.pet = pet;
	    			bs.skillList = new short[0];
	    			bs.setStatus(BattleSprite.SEAL_SKILL_ATTACK, false);
	    			bs.setStatus(BattleSprite.SEAL_SKILL_SKILL, false);
	    			bs.setStatus(BattleSprite.SEAL_SKILL_DEF,false);
	    			IEquipment[] equips = new IEquipment[pet.getUsedEquipments().length];
					for(int jj = 0;jj<equips.length;jj++){
						if (pet.getUsedEquipments()[jj] != null){
							equips[jj] = (IEquipment)pet.getUsedEquipments()[jj].item;
						}
					}
			        bs.initPetEquipData(equips,0,pet.getEvolutionLevel());
			        bs.level = (short)pet.getLevel();
			        bs.battleSuitEffect = null;//sprite.splitePetEffect();
			        //属于援护
			        bs.isIntervene = true;
			        Ability[] ability = pet.getAbilities();
			        if(ability != null){
			        	ArrayList<Ability> listAbility = new ArrayList<Ability>();
			        	for(int b=0; b<ability.length; b++){
			        		if(BattleIntervene.hasSkill(ability[b].getId())){
			        			listAbility.add(ability[b]);
			        		}
			        	}
			        	bs.interveneAbility = new Ability[listAbility.size()];
			        	listAbility.toArray(bs.interveneAbility);
			        }
	    			switch(pet.getSpiritualityLevel()){
	    			case 0:
	    			case 1:
	    				bs.interveneTimeMax = 1;
	    				break;
	    			case 2:
	    			case 3:
	    			case 4:
	    				bs.interveneTimeMax = 2;
	    				break;
	    			case 5:
	    			case 6:
	    			case 7:
	    				bs.interveneTimeMax = 3;
	    				break;
	    			case 8:
	    				bs.interveneTimeMax = 5;
	    				break;
	    			}
		    		switch(pet.getBindType()){
		    		case 1:
		    			interveneAgeII.add(bs);
		    			break;
		    		default:
		    			interveneAgeI.add(bs);
		    		}
	    		}
	    	}
    	}
    }
    
    public List<BattleSprite> getIntervene(int age){
    	switch(age){
    	case 1:
    		return interveneAgeII;
    	default:
    		return interveneAgeI;
    	}
    }
    
    public BattleSprite getIntervene(){
    	BattleSprite bs = getIntervene(interveneAgeII, 1);
    	if(bs != null){
    		return bs;
    	}
    	bs = getIntervene(interveneAgeI, 0);
    	return bs;
    }
    
    //获取援护对象
    public BattleSprite getIntervene(List<BattleSprite> interveneAge, int age){
    	if(BattleIntervene.open && interveneAge.size() > 0){
    		int rate = BattleIntervene.getRateInVersion(age);
    		if(Utils.hit(rate, 100)){
	    		//80%的机率从悟性高的宠物中
	    		if(Utils.hit(80, 100)){
		    		for(int i=0; i<interveneAge.size(); i++){
		    			BattleSprite bs = interveneAge.get(i);
		    			if(bs.interveneTime < bs.interveneTimeMax){
		    				bs.interveneTime++;
		    				return bs;
		    			}
		    		}
	    		}else{
	    			Random rnd = new Random();
	    			int rateid = Utils.getRandom(rnd, 0, interveneAge.size() - 1);
	    			int index = 0;
	    			while(index < interveneAge.size()){
	    				BattleSprite bs = interveneAge.get(rateid);
	    				if(bs.interveneTime < bs.interveneTimeMax){
		    				bs.interveneTime++;
		    				return bs;
		    			}else{
		    				rateid++;
		    				if(rateid >= interveneAge.size()){
		    					rateid = 0;
		    				}
		    			}
	    				index++;
	    			}
	    		}
    		}
    	}
    	return null;
    }
    
    public Ability getInterveneSkill(){
    	if(interveneAbility != null && interveneAbility.length > 0){
    		Random rnd = new Random();
    		int index = Utils.getRandom(rnd, 0, interveneAbility.length - 1);
    		return interveneAbility[index];
    	}else{
    		return null;
    	}
    }
    
    public boolean isIntervene(){
    	return isIntervene;
    }

    public void initEquipData(IEquipment[] equipments){
        int v = 1;
        lastmissflag = 0;
        //        int vit1, int1, str1, agi1, hp1, mp1;
        //
        //        vit1 = attributes[ATTR_VIT];
        //        int1 = attributes[ATTR_INT];
        //        str1 = attributes[ATTR_STR];
        //        agi1 = attributes[ATTR_AGI];
        //        hp1 = attributes[ATTR_HPMAX];
        //        mp1 = attributes[ATTR_MPMAX];
        //
        //        for(int i = 0; i < equipments.length; i++){
        //            IEquipment tmp = equipments[i];
        //            if(tmp == null)
        //                continue;
        //            vit1 += tmp.getProperty(IEquipment.EQUIP_ADD_VIT) * v;
        //            int1 += tmp.getProperty(IEquipment.EQUIP_ADD_INT) * v;
        //            str1 += tmp.getProperty(IEquipment.EQUIP_ADD_STR) * v;
        //            agi1 += tmp.getProperty(IEquipment.EQUIP_ADD_AGI) * v;
        //        }
        //
        //        initBattleData(bsType, level, vit1, str1, int1, agi1, luck, hp, mp);

        //        changeHp(hpLimit - hp1);
        //        changeMp(mpLimit - mp1);

        int[] evaValueAdd = new int[attributes.length];
        int[] stoneValueAdd = new int[attributes.length];
        for(int i = 0; i < equipments.length; i++){
            IEquipment tmp = equipments[i];
            if(tmp == null || !tmp.isValid())
                continue;
            attributes[ATTR_PMIN] += tmp.getProperty(IEquipment.EQUIP_ADD_ATTACK_MIN, level) * v;
            attributes[ATTR_PMAX] += tmp.getProperty(IEquipment.EQUIP_ADD_ATTACK_MAX, level) * v;
            weaponAttack += 0;
            equipDefence += tmp.getProperty(IEquipment.EQUIP_ADD_DEFENCE, level) * v;
            attributes[ATTR_PMIN] += tmp.getProperty(IEquipment.EQUIP_ADD_PATTACK, level) * v;
            attributes[ATTR_PMAX] += tmp.getProperty(IEquipment.EQUIP_ADD_PATTACK, level) * v;
            attributes[ATTR_MMIN] += tmp.getProperty(IEquipment.EQUIP_ADD_MATTACK, level) * v;
            attributes[ATTR_MMAX] += tmp.getProperty(IEquipment.EQUIP_ADD_MATTACK, level) * v;
            attributes[ATTR_PDEF] += tmp.getProperty(IEquipment.EQUIP_ADD_PDEFENCE, level) * v;
            attributes[ATTR_MDEF] += tmp.getProperty(IEquipment.EQUIP_ADD_MDEFENCE, level) * v;
            attributes[ATTR_PHIT] += tmp.getProperty(IEquipment.EQUIP_ADD_HIT, level) * v;
            //加上装备的附魔属性加成
            attributes[ATTR_PHIT] += tmp.getEnchanting().getProperty(IEquipment.EQUIP_ADD_HIT) * v;
            
            attributes[ATTR_MHIT] += tmp.getProperty(IEquipment.EQUIP_ADD_HIT, level) * v;
            attributes[ATTR_FLEE] += tmp.getProperty(IEquipment.EQUIP_ADD_FLEE, level) * v;
            attributes[ATTR_PCRI] += tmp.getProperty(IEquipment.EQUIP_ADD_PCRI, level) * v;
            attributes[ATTR_MCRI] += tmp.getProperty(IEquipment.EQUIP_ADD_MCRI, level) * v;
            attributes[ATTR_NOCRI] += tmp.getProperty(IEquipment.EQUIP_ADD_NOCRI, level) * v;
            attributes[ATTR_HPMAX] += tmp.getProperty(IEquipment.EQUIP_ADD_HPMAX, level) * v;
            attributes[ATTR_MPMAX] += tmp.getProperty(IEquipment.EQUIP_ADD_MPMAX, level) * v;
            
            //计算所有装备所带的属性攻的攻击和防御的值
            vianyAttack += tmp.getViany().getVianyAttack((byte)vianyType);
            vianyDefense += tmp.getViany().getVianyDefense((byte)vianyType);
            
            //鉴定加成
            if(evaValue > 0){
	            evaValueAdd[ATTR_VIT] += tmp.getDiamondProperty(IEquipment.EQUIP_ADD_VIT, level) * v;
	            evaValueAdd[ATTR_INT] += tmp.getDiamondProperty(IEquipment.EQUIP_ADD_INT, level) * v;
	            evaValueAdd[ATTR_STR] += tmp.getDiamondProperty(IEquipment.EQUIP_ADD_STR, level) * v;
	            evaValueAdd[ATTR_AGI] += tmp.getDiamondProperty(IEquipment.EQUIP_ADD_AGI, level) * v;
	            evaValueAdd[ATTR_PDEF] += tmp.getDiamondProperty(IEquipment.EQUIP_ADD_PDEFENCE, level) * v;
	            evaValueAdd[ATTR_PMIN] += tmp.getDiamondProperty(IEquipment.EQUIP_ADD_ATTACK_MIN, level) * v;
	            evaValueAdd[ATTR_PMAX] += tmp.getDiamondProperty(IEquipment.EQUIP_ADD_ATTACK_MAX, level) * v;
	            evaValueAdd[ATTR_MMIN] += tmp.getDiamondProperty(IEquipment.EQUIP_ADD_PATTACK, level) * v;
	            evaValueAdd[ATTR_MMAX] += tmp.getDiamondProperty(IEquipment.EQUIP_ADD_MATTACK, level) * v;
	            evaValueAdd[ATTR_MDEF] += tmp.getDiamondProperty(IEquipment.EQUIP_ADD_DEFENCE, level) * v;
            }
            
            //宝石加成
            if(stoneValue > 0){
	            stoneValueAdd[ATTR_VIT] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_VIT) * v;
	            stoneValueAdd[ATTR_INT] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_INT) * v;
	            stoneValueAdd[ATTR_STR] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_STR) * v;
	            stoneValueAdd[ATTR_AGI] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_AGI) * v;
	            stoneValueAdd[ATTR_MDEF] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_MDEFENCE) * v;
	            stoneValueAdd[ATTR_PDEF] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_PDEFENCE) * v;
	            stoneValueAdd[ATTR_PMIN] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_PATTACK) * v;
	            stoneValueAdd[ATTR_MMIN] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_MATTACK) * v;
	            stoneValueAdd[ATTR_PHIT] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_HIT) * v;
	            stoneValueAdd[ATTR_FLEE] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_FLEE) * v;
	            stoneValueAdd[ATTR_NOCRI] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_NOCRI) * v;
	            stoneValueAdd[ATTR_HPMAX] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_HPMAX) * v;
	            stoneValueAdd[ATTR_MPMAX] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_MPMAX) * v;
            }
        }
        
        if(evaValue > 0){
        	attributes[ATTR_PMIN] += evaValueAdd[ATTR_PMIN] * evaValue / 100;
        	attributes[ATTR_PMAX] += evaValueAdd[ATTR_PMAX] * evaValue / 100;
        	equipDefence += evaValueAdd[ATTR_MDEF] * evaValue / 100;;
        }
        
        if(stoneValue > 0){
        	attributes[ATTR_PDEF] += stoneValueAdd[ATTR_PDEF] * stoneValue / 100;
        	attributes[ATTR_MDEF] += stoneValueAdd[ATTR_MDEF] * stoneValue / 100;
        	attributes[ATTR_PMIN] += stoneValueAdd[ATTR_PMIN] * stoneValue / 100;
        	attributes[ATTR_PMAX] += stoneValueAdd[ATTR_PMIN] * stoneValue / 100;
        	attributes[ATTR_MMIN] += stoneValueAdd[ATTR_MMIN] * stoneValue / 100;
        	attributes[ATTR_MMAX] += stoneValueAdd[ATTR_MMIN] * stoneValue / 100;
        	attributes[ATTR_PHIT] += stoneValueAdd[ATTR_PHIT] * stoneValue / 100;
        	attributes[ATTR_MHIT] += stoneValueAdd[ATTR_PHIT] * stoneValue / 100;
        	attributes[ATTR_FLEE] += stoneValueAdd[ATTR_FLEE] * stoneValue / 100;
        	attributes[ATTR_NOCRI] += stoneValueAdd[ATTR_NOCRI] * stoneValue / 100;
        	attributes[ATTR_HPMAX] += stoneValueAdd[ATTR_HPMAX] * stoneValue / 100;
        	attributes[ATTR_MPMAX] += stoneValueAdd[ATTR_MPMAX] * stoneValue / 100;
        }
        attributes[ATTR_PDEF] +=  suitStone != null ? suitStone[PlayerData.PdefStone] : 0;
        attributes[ATTR_MDEF] += suitStone != null ? suitStone[PlayerData.MdefStone] : 0;
        attributes[ATTR_PMIN] += suitStone != null ? suitStone[PlayerData.PattackStone] : 0;
        attributes[ATTR_PMAX] += suitStone != null ? suitStone[PlayerData.PattackStone] : 0;
        attributes[ATTR_MMIN] += suitStone != null ? suitStone[PlayerData.MattackStone] : 0;
        attributes[ATTR_MMAX] += suitStone != null ? suitStone[PlayerData.MattackStone] : 0;
        attributes[ATTR_PHIT] += suitStone != null ? suitStone[PlayerData.HitStone] : 0;
        attributes[ATTR_MHIT] += suitStone != null ? suitStone[PlayerData.HitStone] : 0;
        //闪避特殊处理（除了本身加成额外再加敏捷加成）
        attributes[ATTR_FLEE] += suitStone != null ? suitStone[PlayerData.ParryStone]  + suitStone[PlayerData.AgiStone]: 0;
        attributes[ATTR_NOCRI] += suitStone != null ? suitStone[PlayerData.NocriStone] : 0;
        attributes[ATTR_HPMAX] += suitStone != null ? suitStone[PlayerData.HpStone] : 0;
        attributes[ATTR_MPMAX] += suitStone != null ? suitStone[PlayerData.MpStone] : 0;
        
        //聚灵属性(改成叠加效果)
        if(trainpoint != null){
        	int attacklevel = trainpoint[train_attack]; 
        	if(attacklevel > 0){
        		for(int i = 1;i<=attacklevel;i++){
        			int attributeAttack = 1 + (i - 1) * 1;
        			attributes[ATTR_PMIN] += attributeAttack;
        			attributes[ATTR_PMAX] += attributeAttack;
            	}
        	}
        	int pdeflevel = trainpoint[train_pdef];
        	if(pdeflevel > 0){
        		for(int i = 1;i<=pdeflevel;i++){
        			int attributePdef = 2 + (i - 1) * 3;
        			attributes[ATTR_PDEF] += attributePdef;
        		}
        	}
        	int mattacklevel = trainpoint[train_mattack];
        	if(mattacklevel > 0){
        		for(int i = 1;i<=mattacklevel;i++){
        			int attributeMattack =  1 + (i - 1) * 1;
        			attributes[ATTR_MMIN] += attributeMattack;
        			attributes[ATTR_MMAX] += attributeMattack;
        		}
        	}
        	int mdeflevel = trainpoint[train_mdef];
        	if(mdeflevel > 0){
        		for(int i = 1;i<=mdeflevel;i++){
        			int attributemdef = 2 + (i - 1) * 3; 
        			attributes[ATTR_MDEF] += attributemdef;
        		}
        	}
        	int hitlevel = trainpoint[train_hit];
        	if(hitlevel > 0){
        		for(int i = 1;i<=hitlevel;i++){
        			int attributehit = 7 + (i - 1) * 1;
        			attributes[ATTR_PHIT] += attributehit;
        		}
        	}
        	int nocrilevel = trainpoint[train_nocri];
        	if(nocrilevel > 0){
        		for(int i = 1;i<=nocrilevel;i++){
        			int attributenocri = 1 + (i - 1) * 1;
        			attributes[ATTR_NOCRI] += attributenocri;
        		}
        	}
        
        }
        
        //聚灵等级宝石加成
        attributes[ATTR_PDEF] +=  trainlevelstone != null ? trainlevelstone[PlayerData.PdefStone] : 0;
        attributes[ATTR_MDEF] += trainlevelstone != null ? trainlevelstone[PlayerData.MdefStone] : 0;
        attributes[ATTR_PMIN] += trainlevelstone != null ? trainlevelstone[PlayerData.PattackStone] : 0;
        attributes[ATTR_PMAX] += trainlevelstone != null ? trainlevelstone[PlayerData.PattackStone] : 0;
        attributes[ATTR_MMIN] += trainlevelstone != null ? trainlevelstone[PlayerData.MattackStone] : 0;
        attributes[ATTR_MMAX] += trainlevelstone != null ? trainlevelstone[PlayerData.MattackStone] : 0;
        attributes[ATTR_PHIT] += trainlevelstone != null ? trainlevelstone[PlayerData.HitStone] : 0;
        attributes[ATTR_MHIT] += trainlevelstone != null ? trainlevelstone[PlayerData.HitStone] : 0;
        //闪避特殊处理（除了本身加成额外再加敏捷加成）
        attributes[ATTR_FLEE] += trainlevelstone != null ? trainlevelstone[PlayerData.ParryStone] + trainlevelstone[PlayerData.AgiStone]: 0;
        attributes[ATTR_NOCRI] += trainlevelstone != null ? trainlevelstone[PlayerData.NocriStone] : 0;
        attributes[ATTR_HPMAX] += trainlevelstone != null ? trainlevelstone[PlayerData.HpStone] : 0;
        attributes[ATTR_MPMAX] += trainlevelstone != null ? trainlevelstone[PlayerData.MpStone] : 0;
        
        if(magicposlevel != null && magicposfloor != null){
        	//水元素加物攻魔攻
        	if(magicposlevel[magic_water] > 0 && magicposfloor[magic_water] > 0){
        		int []waterAttrPoint = MagicPosMessage.getMagicPosAttr(magic_water, magicposlevel[magic_water], magicposfloor[magic_water]);
        		if(waterAttrPoint != null){
        			attributes[ATTR_PMIN] += waterAttrPoint[0];
        			attributes[ATTR_PMAX] += waterAttrPoint[0];
        			attributes[ATTR_MMIN] += waterAttrPoint[1];
        			attributes[ATTR_MMAX] += waterAttrPoint[1];
        		}
        	}
        	//土元素加物防魔防
        	if(magicposlevel[magic_soil] > 0 && magicposfloor[magic_soil] > 0){
        		int []soilAttrPoint = MagicPosMessage.getMagicPosAttr(magic_soil, magicposlevel[magic_soil], magicposfloor[magic_soil]);
        		if(soilAttrPoint != null){
        			 attributes[ATTR_PDEF] += soilAttrPoint[0];
        			 attributes[ATTR_MDEF] += soilAttrPoint[1];
        		}
        	}
        	//火元素加命中暴击魔暴
        	if(magicposlevel[magic_fire] > 0 && magicposfloor[magic_fire] > 0){
        		int []fireAttrPoint = MagicPosMessage.getMagicPosAttr(magic_fire, magicposlevel[magic_fire], magicposfloor[magic_fire]);
        		if(fireAttrPoint != null){
        			attributes[ATTR_PHIT] += fireAttrPoint[0];
        			attributes[ATTR_MHIT] += fireAttrPoint[0];
        			attributes[ATTR_PCRI] += fireAttrPoint[1];
        			attributes[ATTR_MCRI] += fireAttrPoint[2];
        		}
        	}
        	//风元素加闪避和免爆
        	if(magicposlevel[magic_wind] > 0 && magicposfloor[magic_wind] > 0){
        		int [] windAttrPoint = MagicPosMessage.getMagicPosAttr(magic_wind, magicposlevel[magic_wind], magicposfloor[magic_wind]);
        		if(windAttrPoint != null){
        			attributes[ATTR_FLEE] += windAttrPoint[0];
        			attributes[ATTR_NOCRI] += windAttrPoint[1];
        		}
        	}
        	//精神元素加血蓝
        	if(magicposlevel[magic_mind] > 0 && magicposfloor[magic_mind] > 0){
        		int [] mindAttrPoint = MagicPosMessage.getMagicPosAttr(magic_mind, magicposlevel[magic_mind], magicposfloor[magic_mind]);
        		if(mindAttrPoint != null){
        			attributes[ATTR_HPMAX] += mindAttrPoint[0];
        			attributes[ATTR_MPMAX] += mindAttrPoint[1];
        		}
        	}
        }
        
        hpShow = hp;
        mpShow = mp;
    }
    
    public void initPetEquipData(IEquipment[] equipments,int pkflag, int evolutionLevel){
        int v = 1;
        lastmissflag = 0;
        int[] stoneValueAdd = new int[attributes.length];
        for(int i = 0; i < equipments.length; i++){
            IEquipment tmp = equipments[i];
            if(tmp == null || !tmp.isValid())
                continue;
            if (pkflag == 1){//pk 攻击减半
            	attributes[ATTR_PMIN] += (tmp.getProperty(IEquipment.EQUIP_ADD_ATTACK_MIN, level) * v * 0.4);
                attributes[ATTR_PMAX] += (tmp.getProperty(IEquipment.EQUIP_ADD_ATTACK_MAX, level) * v * 0.4);
            }else{
            	attributes[ATTR_PMIN] += (tmp.getProperty(IEquipment.EQUIP_ADD_ATTACK_MIN, level) * v * 0.5);
                attributes[ATTR_PMAX] += (tmp.getProperty(IEquipment.EQUIP_ADD_ATTACK_MAX, level) * v * 0.5);
            }
            
            weaponAttack += 0;
            equipDefence += tmp.getProperty(IEquipment.EQUIP_ADD_DEFENCE, level) * v;
            if (pkflag == 1){//pk 攻击减半
            	attributes[ATTR_PMIN] += (tmp.getProperty(IEquipment.EQUIP_ADD_PATTACK, level) * v * 0.4);
                attributes[ATTR_PMAX] += (tmp.getProperty(IEquipment.EQUIP_ADD_PATTACK, level) * v * 0.4);
                attributes[ATTR_MMIN] += (tmp.getProperty(IEquipment.EQUIP_ADD_MATTACK, level) * v * 0.4);
                attributes[ATTR_MMAX] += (tmp.getProperty(IEquipment.EQUIP_ADD_MATTACK, level) * v * 0.4);
            }else{
            	attributes[ATTR_PMIN] += (tmp.getProperty(IEquipment.EQUIP_ADD_PATTACK, level) * v * 0.5);
                attributes[ATTR_PMAX] += (tmp.getProperty(IEquipment.EQUIP_ADD_PATTACK, level) * v * 0.5);
                attributes[ATTR_MMIN] += (tmp.getProperty(IEquipment.EQUIP_ADD_MATTACK, level) * v * 0.5);
                attributes[ATTR_MMAX] += (tmp.getProperty(IEquipment.EQUIP_ADD_MATTACK, level) * v * 0.5);
            }
            attributes[ATTR_PDEF] += tmp.getProperty(IEquipment.EQUIP_ADD_PDEFENCE, level) * v;
            attributes[ATTR_MDEF] += tmp.getProperty(IEquipment.EQUIP_ADD_MDEFENCE, level) * v;
            attributes[ATTR_PHIT] += tmp.getProperty(IEquipment.EQUIP_ADD_HIT, level) * v;
            //加上装备的附魔属性加成
            attributes[ATTR_PHIT] += tmp.getEnchanting().getProperty(IEquipment.EQUIP_ADD_HIT) * v;
            
            attributes[ATTR_MHIT] += tmp.getProperty(IEquipment.EQUIP_ADD_HIT, level) * v;
            attributes[ATTR_FLEE] += tmp.getProperty(IEquipment.EQUIP_ADD_FLEE, level) * v;
            attributes[ATTR_PCRI] += tmp.getProperty(IEquipment.EQUIP_ADD_PCRI, level) * v;
            attributes[ATTR_MCRI] += tmp.getProperty(IEquipment.EQUIP_ADD_MCRI, level) * v;
            
            attributes[ATTR_NOCRI] += tmp.getProperty(IEquipment.EQUIP_ADD_NOCRI, level) * v;
            attributes[ATTR_HPMAX] += tmp.getProperty(IEquipment.EQUIP_ADD_HPMAX, level) * v;
            attributes[ATTR_MPMAX] += tmp.getProperty(IEquipment.EQUIP_ADD_MPMAX, level) * v;
            
            //计算所有装备所带的属性攻的攻击和防御的值
            vianyAttack += tmp.getViany().getVianyAttack((byte)vianyType);
            vianyDefense += tmp.getViany().getVianyDefense((byte)vianyType);
            
            //阵营宝石加成
            if(stoneValue > 0){
	            stoneValueAdd[ATTR_VIT] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_VIT) * v;
	            stoneValueAdd[ATTR_INT] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_INT) * v;
	            stoneValueAdd[ATTR_STR] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_STR) * v;
	            stoneValueAdd[ATTR_AGI] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_AGI) * v;
	            stoneValueAdd[ATTR_MDEF] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_MDEFENCE) * v;
	            stoneValueAdd[ATTR_PDEF] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_PDEFENCE) * v;
	            stoneValueAdd[ATTR_PMIN] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_PATTACK) * v;
	            stoneValueAdd[ATTR_MMIN] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_MATTACK) * v;
	            stoneValueAdd[ATTR_PHIT] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_HIT) * v;
	            stoneValueAdd[ATTR_FLEE] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_FLEE) * v;
	            stoneValueAdd[ATTR_NOCRI] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_NOCRI) * v;
	            stoneValueAdd[ATTR_HPMAX] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_HPMAX) * v;
	            stoneValueAdd[ATTR_MPMAX] += tmp.getDiamondMosiacProperty(IEquipment.EQUIP_ADD_MPMAX) * v;
            }
        }
        if(stoneValue > 0){
        	attributes[ATTR_PDEF] += stoneValueAdd[ATTR_PDEF] * stoneValue / 100;
        	attributes[ATTR_MDEF] += stoneValueAdd[ATTR_MDEF] * stoneValue / 100;
        	attributes[ATTR_PMIN] += stoneValueAdd[ATTR_PMIN] * stoneValue / 100;
        	attributes[ATTR_PMAX] += stoneValueAdd[ATTR_PMIN] * stoneValue / 100;
        	attributes[ATTR_MMIN] += stoneValueAdd[ATTR_MMIN] * stoneValue / 100;
        	attributes[ATTR_MMAX] += stoneValueAdd[ATTR_MMIN] * stoneValue / 100;
        	attributes[ATTR_PHIT] += stoneValueAdd[ATTR_PHIT] * stoneValue / 100;
        	attributes[ATTR_MHIT] += stoneValueAdd[ATTR_PHIT] * stoneValue / 100;
        	attributes[ATTR_FLEE] += stoneValueAdd[ATTR_FLEE] * stoneValue / 100;
        	attributes[ATTR_NOCRI] += stoneValueAdd[ATTR_NOCRI] * stoneValue / 100;
        	attributes[ATTR_HPMAX] += stoneValueAdd[ATTR_HPMAX] * stoneValue / 100;
        	attributes[ATTR_MPMAX] += stoneValueAdd[ATTR_MPMAX] * stoneValue / 100;
        }
        
        //宠物进化数据
        EvolutionData data = EvolutionLoader.evolutions.get(evolutionLevel);
        if(data != null){
        	attributes[ATTR_HPMAX] += data.hp;
        	attributes[ATTR_PMIN] += data.pa;
        	attributes[ATTR_PMAX] += data.pa;
        	attributes[ATTR_MMIN] += data.ma;
        	attributes[ATTR_MMAX] += data.ma;
        	attributes[ATTR_PDEF] += data.pd;
        	attributes[ATTR_MDEF] += data.md;
        }
        
    }
    
    public void AddAttrBuf(int bout, int attackAdd, int magicAttackAdd, int defenceAdd, int magicDefenceAdd, int hitAdd, int fleeAdd, int criRateAdd, int phyDamageAdd, int mgcDamageAdd,
            int letPhyDamageValue, int letMgcDamageValue, int effectId){
    	AddAttrBuf(bout, attackAdd, magicAttackAdd, defenceAdd, magicDefenceAdd, hitAdd, fleeAdd, criRateAdd, phyDamageAdd, mgcDamageAdd, letPhyDamageValue, letMgcDamageValue, effectId, 0, 0, 0, 0, 0);
    }
    
    public void AddAttrBuf(int bout, int attackAdd, int magicAttackAdd, int defenceAdd, int magicDefenceAdd, int hitAdd, int fleeAdd, int criRateAdd, int phyDamageAdd, int mgcDamageAdd,
                    int letPhyDamageValue, int letMgcDamageValue, int effectId,int bufWhi, int attackMaxAdd, int defenceMaxAdd,
                    int skillparm1, int skillparm2){
        int[] buf = new int[18];

        buf[0] = bout;
        buf[1] = attackAdd;
        buf[2] = magicAttackAdd;
        buf[3] = defenceAdd;
        buf[4] = magicDefenceAdd;
        buf[5] = hitAdd;
        buf[6] = fleeAdd;
        buf[7] = criRateAdd;
        buf[8] = phyDamageAdd;
        buf[9] = mgcDamageAdd;
        buf[10] = letPhyDamageValue;
        buf[11] = letMgcDamageValue;
        buf[12] = effectId;
        buf[13] = bufWhi;
        buf[14] = attackMaxAdd;
        buf[15] = defenceMaxAdd;
        buf[16] = skillparm1;
        buf[17] = skillparm2;

        //对于attrBuf，如果由相同effectId添加，则进行替换
        Enumeration bufs = bufTable.keys();
        boolean flag = true;

        while(bufs.hasMoreElements()){
            Integer bufId = (Integer)bufs.nextElement();
            int[] bufInfo = (int[])bufTable.get(bufId);

            if(!bufId.equals(statusIndexBuf) && !bufId.equals(statusIndexDebuf)){
                if(bufInfo.length > 12 && bufInfo[12] == effectId){
                    bufTable.put(bufId, buf);
                    flag = false;

                    break;
                }
            }
        }

        if(flag){
            bufTable.put(new Integer(statusIndexAttrBuf), buf);
            statusIndexAttrBuf++;
        }

        calculateBattleBuf();
    }

    public void setBufStatus(int bout, int status, int level, int parm1, int parm2, int srcType, int srcIndex){
        int[] oldBuf;

        oldBuf = (int[])bufTable.get(statusIndexBuf);

        if(oldBuf != null && oldBuf[1] == status){
            if(oldBuf[2] > level){
                calculateBattleBuf();

                return;
            }
        }

        int[] buf = new int[7];

        buf[0] = bout;
        buf[1] = status;
        buf[2] = level;
        buf[3] = parm1;
        buf[4] = parm2;
        buf[5] = srcType;
        buf[6] = srcIndex;

        bufTable.put(statusIndexBuf, buf);

        calculateBattleBuf();
    }

    public void resetBuff(){
        bufTable.clear();
        coolDownTable.clear();
        setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);
        setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);
    }

    public void setDeBufStatus(int bout, int status, int level, int parm1, int parm2, int srcType, int srcIndex){
    	setDeBufStatus(bout, status, level, parm1, parm2, srcType, srcIndex, true);
    }
    public void setDeBufStatus(int bout, int status, int level, int parm1, int parm2, int srcType, int srcIndex, boolean calc){
        int[] oldBuf;

        oldBuf = (int[])bufTable.get(statusIndexDebuf);

        if(oldBuf != null && (oldBuf[1] == Skill.STATUS_DIE || oldBuf[1] == Skill.STATUS_RUNAWAY || oldBuf[1] == Skill.STATUS_CATCHED)){
        	if(calc){
        		calculateBattleBuf();
        	}

            return;
        }

        if(oldBuf != null && oldBuf[1] == status){
            if(oldBuf[2] > level){
            	if(calc){
            		calculateBattleBuf();
            	}

                return;
            }
        }

        int[] buf = new int[7];

        buf[0] = bout;
        buf[1] = status;
        buf[2] = level;
        buf[3] = parm1;
        buf[4] = parm2;
        buf[5] = srcType;
        buf[6] = srcIndex;

        bufTable.put(statusIndexDebuf, buf);

        if(calc){
        	calculateBattleBuf();
        }
    }

    public void reLive(){
        int[] buf = new int[7];

        buf[0] = 1;
        buf[1] = Skill.STATUS_NORMAL;
        buf[2] = 0;
        buf[3] = 0;
        buf[4] = 0;
        buf[5] = 0;
        buf[6] = 0;

        bufTable.put(statusIndexDebuf, buf);

        calculateBattleBuf();
    }

    private void calculateBattleBuf(){
        attackAdd = 0;
        magicAttackAdd = 0;
        defenceAdd = 0;
        magicDefenceAdd = 0;
        hitAdd = 0;
        fleeAdd = 0;
        criRateAdd = 0;
        phyDamageAdd = 0;
        mgcDamageAdd = 0;
        letPhyDamageAdd = 0;
        letMgcDamageAdd = 0;
        attackMaxAdd = 0;
        defenceMaxAdd = 0;
        skillparm1 = 0;
        skillparm2 = 0;

        //处理Buf列表
        Enumeration bufs = bufTable.keys();
        while(bufs.hasMoreElements()){
            Integer bufId = (Integer)bufs.nextElement();
            int[] bufInfo = (int[])bufTable.get(bufId);

            if(bufId.equals(statusIndexDebuf)){
                debufStatus = bufInfo[1];
            }else if(bufId.equals(statusIndexBuf)){
                bufStatus = bufInfo[1];
            }else{
            	if(!checkEffect(bufInfo)){
		            attackAdd += bufInfo[1];
		            magicAttackAdd += bufInfo[2];
		            defenceAdd += bufInfo[3];
		            magicDefenceAdd += bufInfo[4];
		            hitAdd += bufInfo[5];
		            fleeAdd += bufInfo[6];
		            criRateAdd += bufInfo[7];
		            phyDamageAdd += bufInfo[8];
		            mgcDamageAdd += bufInfo[9];
		            letPhyDamageAdd += bufInfo[10];
		            letMgcDamageAdd += bufInfo[11];
		            attackMaxAdd += bufInfo[14];
		            defenceMaxAdd += bufInfo[15];
		            skillparm1 = bufInfo[16];
		            skillparm2 = bufInfo[17];
            	}
                
            }
        }
    }
    
    public boolean checkEffect(int[] bufInfo){
    	if(bufInfo[12] == SkillConstants.EFFECT_PET_BAIT){
        	if(bufInfo[13] == 1){
        		if(bufInfo[0] == 2){
            		letPhyDamageAdd += bufInfo[10] * 50 / 100;
        		}
        		if(bufInfo[0] == 1){
        			letPhyDamageAdd += bufInfo[10] * 80 / 100;
        		}
        	}else if(bufInfo[13] == 0){
        		attackAdd += bufInfo[1];
        	}
        	return true;
        }
    	if(bufInfo[12] == SkillConstants.EFFECT_PET_BAD_SEED){
    		if(bufInfo[13] == 0){   
    			switch(bufInfo[0]){
    				case 4:
    				case 3:
    					letMgcDamageAdd += 0;
    					break;
    				case 2:
    					letMgcDamageAdd += 0;
    					break;
    				case 1:
    					letMgcDamageAdd += bufInfo[11];
    					break;
    			}	
        	}else if(bufInfo[13] == 1){
        		switch(bufInfo[0]){
        			case 4: 
        				mgcDamageAdd += 0;
        				break;
        			case 3:
        				mgcDamageAdd += 0;
        				break;
        			case 2:
        				
        				int hpDec = bufInfo[17];
        				this.setDeBufStatus(1, Skill.STATUS_POISON, skill.level, hpDec, 0, this.bsType, this.groupIndex, false);
        				break;
        			case 1:
        				mgcDamageAdd += bufInfo[9];
        				this.setDeBufStatus(1, Skill.STATUS_POISON, skill.level, 0, mgcDamageAdd, this.bsType, this.groupIndex, false);
        				break;
        				
        		}
        	}
    		return true;
    	}
        return false;
    }

    public boolean HasBuf(){
        return this.getBufStatus() != Skill.STATUS_NORMAL;
    }

    public void processBattleBuf(Vector battleMovie, int index, BattleDataProcess battleDataProcess){
        //处理buf列表
        Enumeration bufs = bufTable.keys();
        Vector bufDelete = new Vector();
        boolean flag = false;

        while(bufs.hasMoreElements()){
            Integer bufId = (Integer)bufs.nextElement();
            int[] bufInfo = (int[])bufTable.get(bufId);
            int tmp = bufInfo[0] - 1;

            if(tmp <= 0){
                if(bufId.equals(statusIndexBuf)){
                    bufInfo[0] = 1;
                    bufInfo[1] = Skill.STATUS_NORMAL;
                }else if(bufId.equals(statusIndexDebuf)){
                    if(bufInfo[1] != Skill.STATUS_DIE && bufInfo[1] != Skill.STATUS_RUNAWAY && bufInfo[1] != Skill.STATUS_CATCHED && bufInfo[1] != Skill.STATUS_NORMAL){
                        bufInfo[0] = 1;
                        bufInfo[1] = Skill.STATUS_NORMAL;

                        flag = true; //是否需加入状态清除动画
                    }
                }else{
                    bufDelete.addElement(bufId);
                }
            }else{
                bufInfo[0] = tmp;

                if(bufInfo[1] == Skill.STATUS_STONE){ //石化特殊处理，有一定几率自行解除
                    if(Skill.getPercentRate(bufInfo[4])){
                        bufInfo[0] = 1;
                        bufInfo[1] = Skill.STATUS_NORMAL;

                        flag = true; //是否需加入状态清除动画
                    }
                }
            }
        }

        for(int i = 0; i < bufDelete.size(); i++){
            Integer bufId = (Integer)bufDelete.elementAt(i);
            bufTable.remove(bufId);
        }

        calculateBattleBuf();

        if(flag){
            Skill.processStatusUpdate(this, index, battleMovie, battleDataProcess);
        }
    }

    private void initAttribute(){
        //生命=INT(7 * 体力 * (INT(SQRT(等级 * 100)) + 30) / 40) + 50 + 血宝石
        //魔法=INT(3 * (智力 + 力量  / 8) *(INT(SQRT(等级 * 100)) + 30) / 40) + 50 +　蓝宝石
        //物理攻击下限=INT(0.8*力量*INT(SQRT(等级*100))+30)/40)+15
        //物理攻击上限=INT(力量*INT(SQRT(等级*100))+30)/40)+20
        //高低限平均加额外点
        //物理防御=装备防御值
        //魔法攻击下限=INT(0.6*智力*INT(SQRT(等级*100))+30)/40)+15
        //魔法攻击上限=INT(0.8*智力*INT(SQRT(等级*100))+30)/40)+20
        //高低限平均加额外点
        //魔法防御=装备魔法防御值
        //命中率=95-敌人敏捷*8/(100+敌人级别)-2*SIGN(敌方敏捷-我方敏捷)*(SQRT(4+ABS(敌方敏捷-我方敏捷))-2) //命中率最多100%
        //物理伤害吸收=SQRT(5*物理防御*(100/攻击者等级)) //伤害吸收最多80%
        //魔法伤害吸收=SQRT(5*魔法防御*(100/攻击者等级)) //伤害吸收最多80%

        //attributes[ATTR_HPMAX] = attributes[ATTR_VIT] * 6 * ((int)sqrt(level * 100) + 30) / 40 + 50;
        //attributes[ATTR_MPMAX] = attributes[ATTR_INT] * 3 * ((int)sqrt(level * 100) + 30) / 40 + 50;
    	
    	attributes[ATTR_HPMAX] = Utils.calculateMaxHp(attributes[ATTR_VIT], 0, 0, 0, level, 0);
    	attributes[ATTR_MPMAX] = Utils.calculateMaxMp(0, 0, attributes[ATTR_STR], attributes[ATTR_INT], level, 0);
       /* if(hp > attributes[ATTR_HPMAX])
            hp = attributes[ATTR_HPMAX];
        if(mp > attributes[ATTR_MPMAX])
            mp = attributes[ATTR_MPMAX];

        if(bsType == TYPE_MONSTER){
            hp = attributes[ATTR_HPMAX];
            mp = attributes[ATTR_MPMAX];
        }

        hpShow = hp;
        mpShow = mp;*/

        attributes[ATTR_PMIN] = attributes[ATTR_STR] * ((int)sqrt(level * 100) + 30) * 8 / 10 / 40 + (bsType == TYPE_PLAYER? 15: 0);
        attributes[ATTR_PMAX] = attributes[ATTR_STR] * ((int)sqrt(level * 100) + 30) / 40 + (bsType == TYPE_PLAYER? 20: 1);
        attributes[ATTR_PDEF] = 0;

        attributes[ATTR_MMIN] = attributes[ATTR_INT] * ((int)sqrt(level * 100) + 30) * 6 / 10 / 40 + (bsType == TYPE_PLAYER? 15: 0);
        attributes[ATTR_MMAX] = attributes[ATTR_INT] * ((int)sqrt(level * 100) + 30) * 8 / 10 / 40 + (bsType == TYPE_PLAYER? 20: 1);

        attributes[ATTR_MDEF] = 0;
        attributes[ATTR_PHIT] = 0;
        attributes[ATTR_MHIT] = 0;
        attributes[ATTR_FLEE] = attributes[ATTR_AGI];	//在客户端上是0
      /*  attributes[ATTR_PCRI] = 5 + (attributes[ATTR_AGI] - level) / 15;
        attributes[ATTR_MCRI] = 5 + attributes[ATTR_INT] / 20;*/
        attributes[ATTR_PCRI] = attributes[ATTR_AGI];
        attributes[ATTR_MCRI] = attributes[ATTR_INT];
        attributes[ATTR_NOCRI] = attributes[ATTR_VIT] / 8;
    }

    public void setTarget(BattleSprite target, int index){
        setTarget(target, index, 0);
    }

    public void setTarget(BattleSprite target, int index, int targetType){
        this.target = target;
        this.targetIndex = index;
        this.targetType = targetType;
    }

    public void setSkill(Skill skill){
        this.skill = skill;
    }

    public void clearBout(Vector battleMovie, int index, BattleDataProcess battleDataProcess){
        skill = Skill.NOTREADY_SKILL;
//        this.skillId = Skill.SKILL_NOT_READY;
        this.target = null;
        this.targetIndex = -1;
        this.targetType = 0;
        this.usedMp = 0;
        
        if(battleSuitEffect != null){
            for(int i = 0; i < battleSuitEffect.length; i++){
                battleSuitEffect[i].passBout();
            }
        }
    }

    public void endProcess(Vector battleMovie, BattleDataProcess battleDataProcess){
        processCoolDown();
        processControlDecrease();
        processAutoRelife(battleMovie, battleDataProcess);
        
        if(testDie()){
            if(dieRound > battleDataProcess.getRound()){
                dieRound = battleDataProcess.getRound();
            }
        }else{
            dieRound = 999999;
        }
    }

    public void processAutoRelife(Vector battleMovie, BattleDataProcess battleDataProcess){
        if(testDie() && getBufStatus() == Skill.STATUS_AUTO_RELIFE){
            int[] bufInfo = getbufInfo();

            int reLifePercent = bufInfo[3];
            int restoreHpPercent = bufInfo[4];

            if(Skill.getPercentRate(reLifePercent)){
                reLive();

                int hpAdd = this.attributes[BattleSprite.ATTR_HPMAX] * restoreHpPercent / 100;

                setTarget(this, groupIndex);

                if(testMCri()){
                    hpAdd *= BattleSprite.CRI_RATE;

                    Skill.processSaveLifeMovie(this, groupIndex, hpAdd, Skill.ATTACK_CRI, battleMovie, battleDataProcess);
                }else{
                    Skill.processSaveLifeMovie(this, groupIndex, hpAdd, Skill.ATTACK_NO_CRI, battleMovie, battleDataProcess);
                }

                hp = hpAdd;

                if(hp > attributes[ATTR_HPMAX]){
                    hp = attributes[ATTR_HPMAX];
                }

                this.target = null;
                this.targetIndex = -1;
                this.targetType = 0;
            }

            setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);
        }
    }

    public void processCoolDown(){
        //处理冷却列表
        Enumeration cdTable = coolDownTable.keys();
        Vector cdDelete = new Vector();

        while(cdTable.hasMoreElements()){
            Integer cdId = (Integer)cdTable.nextElement();
            Integer cdBout = (Integer)coolDownTable.get(cdId);
            int tmp = cdBout.intValue() - 1;

            if(tmp > 0){
                cdBout = new Integer(tmp);
                coolDownTable.put(cdId, cdBout);
            }else{
                cdDelete.addElement(cdId);
            }
        }

        for(int i = 0; i < cdDelete.size(); i++){
            Integer cdId = (Integer)cdDelete.elementAt(i);
            coolDownTable.remove(cdId);
        }
    }
    
    public byte[] getCoolDownInfo(){
        byte[] result = new byte[coolDownTable.size() * 2];
        
        Enumeration cdTable = coolDownTable.keys();
        int c = 0;
        
        while(cdTable.hasMoreElements()){
            Integer cdId = (Integer)cdTable.nextElement();
            Integer cdBout = (Integer)coolDownTable.get(cdId);
            result[c] = (byte)cdId.intValue();
            result[c + 1] = (byte)cdBout.intValue();
            c += 2;
        }
        
        return result;
    }
    
    public void processControlDecrease(){
        if(controlDecreaseBout > 0){
            controlDecreaseBout--;
            
            if(controlDecreaseBout == 0){
                controlDecreasePercent = 0;
            }
        }
    }
    
    public void controlHit(){
        if(controlDecreasePercent == 0){
            controlDecreasePercent = 50;
        }else{
            controlDecreasePercent = 100;
        }
        
        controlDecreaseBout = 10;
    }
    
    public boolean testControlHit(){
        return Skill.getPercentRate(99 - controlDecreasePercent);
    }

    public void coolDownSkill(Skill skill){
        coolDownTable.put(new Integer(skill.coolDown), new Integer(skill.coolDownBout));
    }

    public int testCoolDown(int skillId){
        int result = 0;

        Skill skill = Skill.getSkill(skillId);

        Integer cdid = new Integer(skill.coolDown);
        Integer cdBout = (Integer)coolDownTable.get(cdid);

        if(cdBout != null){
            result = cdBout.intValue();
        }else{
            result = 0;
        }

        return result;
    }

    public boolean doCatch(BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        boolean result = false;
        
        if(target == null){
            return false;
        }
        if(target.monster.getPetType()== 0){
        	// 没有配置掉落宠物
        	return false;
        }
        if(target.monster.getBabyRate() <= 0){
        	// 没有配置掉落宠物
        	return false;
        }

        int tmpHp = target.hp;

        if(tmpHp == 0){
            tmpHp = 1;
        }

        int catchRate = target.attributes[ATTR_HPMAX] / tmpHp + 4;

        if(catchRate > 30){
            catchRate = 30;
        }
        
        /**
         * 修改套装属性不可叠加
         */
        if(battleSuitEffect != null){
            int effectCatchRate = 0;
            
            for(int i = 0; i < battleSuitEffect.length; i++){
                if(battleSuitEffect[i].effectCatch(this, our, ourPet, them, themPet)){
                    if(battleSuitEffect[i].getValue() > effectCatchRate){
                        effectCatchRate = battleSuitEffect[i].getValue();
                    }
                    battleSuitEffect[i].doEffect();
                }
            }
            
            catchRate += effectCatchRate;
        }
        
        //套装效果加成后抓宠成功率最高限定为80％
        if(catchRate > 80){
            catchRate = 80;
        }

        if(Skill.getPercentRate(catchRate)){
            target.setDeBufStatus(1, Skill.STATUS_CATCHED, 0, 0, 0, 0, 0);
            result = true;
        }else{
            result = false;
        }

        return result;
    }

    public boolean testAntiBuf(){
        boolean result = false;

        //处理Buf列表
        Enumeration bufs = bufTable.keys();

        while(bufs.hasMoreElements()){
            Integer bufId = (Integer)bufs.nextElement();
            int[] bufInfo = (int[])bufTable.get(bufId);

            if(!bufId.equals(statusIndexDebuf) && !bufId.equals(statusIndexBuf)){
                if(bufInfo[12] == Skill.EFFECT_ANTI_BUF_INC_ATK){
                    result = true;

                    break;
                }
            }
        }

        return result;
    }

    public int[] doBattle(int action, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        int[] result = new int[8]; //[0]是否命中， [1]伤害值, [2]暴击, [3] 反弹，吸收 [4] 物理反弹吸收参数 [5] 魔法反弹吸收参数 [6] 保护人类型 [7]保护人索引
        int[] tmp;

        if(this.isIntervene || testHit(target.getFlee(), action)){
            //处理反弹吸收
            int buf = target.getBufStatus();
            int debuf = target.getDebufStatus();
            int[] bufInfo = target.getbufInfo();
            int[] debufInfo = target.getDebufInfo();

            if(action == ACTION_PATTACK){
                switch(buf){
                    case Skill.STATUS_ANTI_ATTACK:
                        result[3] = Skill.ATTACK_ANTI;

                        break;
                    case Skill.STATUS_SORB_ATTACK:
                        result[3] = Skill.ATTACK_SORB;

                        break;
                    case Skill.STATUS_PROTECTED:
                        result[3] = Skill.ATTACK_PROCTECT;

                        break;
                    case Skill.STATUS_DAMAGE_TO_MP:
                        result[3] = Skill.ATTACK_PET_DAMAGE_TO_MP;

                        break;
                    default:
                        result[3] = Skill.ATTACK_NORMAL;

                        break;
                }
            }else{
                switch(buf){
                    case Skill.STATUS_ANTI_MAGIC:
                        result[3] = Skill.ATTACK_ANTI;

                        break;
                    case Skill.STATUS_SORB_ATTACK:
                    case Skill.STATUS_SORB_MAGIC:
                        result[3] = Skill.ATTACK_SORB;

                        break;
                    case Skill.STATUS_PROTECTED:
                        result[3] = Skill.ATTACK_PROCTECT;

                        break;
                    case Skill.STATUS_DAMAGE_TO_MP:
                        result[3] = Skill.ATTACK_PET_DAMAGE_TO_MP;

                        break;
                    default:
                        result[3] = Skill.ATTACK_NORMAL;

                        break;
                }
            }

            if(testAntiBuf()){
                result[3] = Skill.ATTACK_NORMAL;
            }

            result[4] = bufInfo[3];
            result[5] = bufInfo[4];
            result[6] = bufInfo[5];
            result[7] = bufInfo[6];

            switch(action){
                case ACTION_PATTACK:
                    result[0] = Skill.HIT_HIT;
                    tmp = getPhysicalDamage(target, our, ourPet, them, themPet);
                    result[1] = tmp[0];
                    result[2] = tmp[1];

                    if(buf != Skill.STATUS_ANTI_ATTACK && buf != Skill.STATUS_SORB_ATTACK && buf != Skill.STATUS_PROTECTED && buf != Skill.STATUS_DAMAGE_TO_MP){
                        result[3] = Skill.ATTACK_NORMAL;
                        result[4] = 0;
                        result[5] = 0;
                        result[6] = 0;
                    }

                    result[1] = result[1] * (100 + target.phyDamageAdd) / 100;
                    result[1] = result[1] + letPhyDamageAdd;

                    break;
                case ACTION_MATTACK:
                    result[0] = Skill.HIT_HIT;
                    tmp = getMagicDamage(target, our, ourPet, them, themPet);
                    result[1] = tmp[0];
                    result[2] = tmp[1];

                    if(buf != Skill.STATUS_ANTI_MAGIC && buf != Skill.STATUS_SORB_MAGIC && buf != Skill.STATUS_PROTECTED && buf != Skill.STATUS_DAMAGE_TO_MP){
                        result[3] = Skill.ATTACK_NORMAL;
                        result[4] = 0;
                        result[5] = 0;
                        result[6] = 0;
                    }

                    result[1] = result[1] * (100 + target.mgcDamageAdd) / 100;
                    result[1] = result[1] + letMgcDamageAdd;

                    break;
            }

            if(debuf == Skill.STATUS_STONE){
                result[1] = result[1] * (100 - debufInfo[3]) / 100;
            }
            
            //如若是小年BOSSAI
            if(our != null && our.length > 1 && our[0].monster != null && our[0].monster.getAiClass().startsWith("Ai90016")){
            	Ai90016_1 ai = (Ai90016_1)our[0].ai;
            	//角色打怪
	            if(player != null || pet != null){
	            	//增加对BOSS的伤害值 
	            	result[1] = result[1] + result[1] * ai.addHurtPercent / 100;
	            }
	            if(monster != null){
	            	//增加BOSS伤害值 共同的加的伤害的值
	            	result[1] = result[1] + result[1] * ai.addHurtPercentBoss / 100;
	            	//有师徒关系时 伤害减少
	            	if(ai.hasRelation){
	            		result[1] = result[1] - result[1] * ai.subHurtPercent / 100;
	            		//若是打的是徒弟的话 则师傅需要分提伤害
	            		if(target.player != null && BaseMonsterAI.playerService.getMasetService().isPrentice(target.player)){
	            			Master master = BaseMonsterAI.playerService.getMasetService().getMasterRelation(target.player);
	            			for(int i=0; i<them.length; i++){
	            				if(them[i].player != null && them[i].player.getId() == master.getMasterId()){
            						//在没有任何反弹的情况下 设置师傅分担
            						if(result[3] == Skill.ATTACK_NORMAL){
            							result[3] = Skill.ATTACK_MASTER_PROCTECT;
            							result[4] = ai.hurtToMasterPercent;
            							result[6] = BattleSprite.TYPE_PLAYER;
            							result[7] = i;
            						}
	            					break;
	            				}
	            			}
	            		}
	            	}
	            }
            }

            if(result[1] <= 0){
                result[1] = 1;
            }
        }else{
            result[0] = Skill.HIT_MISS;
            result[1] = 0;
            result[2] = Skill.ATTACK_NO_CRI;
            result[3] = Skill.ATTACK_NORMAL;
            result[4] = 0;
            result[5] = 0;
            result[6] = 0;
        }
        
        return result;
    }

    public int getAllStatus(){
        return ((status&0xFF)<<16)|(debufStatus&0xFF);
    }

    public String getSkillName(){
        if(skill==null)
            return "";
        return Skill.getSkillName(this,skill.id,(byte)0,true);
    }

    public int getDebufStatus(){
        return debufStatus;
    }

    public int getBufStatus(){
        return bufStatus;
    }

    public int[] getDebufInfo(){
        return (int[])bufTable.get(statusIndexDebuf);
    }

    public int[] getbufInfo(){
        return (int[])bufTable.get(statusIndexBuf);
    }

    public int getMagicDefence(){
        return (attributes[ATTR_MDEF] + equipDefence) * (100 + magicDefenceAdd) / 100;
    }

    public int getDefence(){
        return (attributes[ATTR_PDEF] + equipDefence + defenceMaxAdd) * (100 + defenceAdd) / 100;
    }
    
    public int getEquipDefence(){
    	return equipDefence;
    }
    
    public int getCriRateAdd(){
    	return criRateAdd;
    }
    
    /**
     * @return装备的闪避等级 +　属性敏捷
     */
    public int getFlee(){
        return attributes[ATTR_FLEE] /*+ attributes[ATTR_AGI]*/ - level;
    }

    public int getPHit(){
        //return attributes[ATTR_PHIT] +  50;
    	return attributes[ATTR_PHIT];
    }

    public int getMHit(){
        //return attributes[ATTR_MHIT] + 50;
    	return attributes[ATTR_MHIT];

    }

    /**
     * @return新版的暴击率 暴击等级转换暴击率 加上额外暴击率加成（来自套装效果， 技能）
     * 暴击等级转换公式  = 1 - 1/(1 + (物暴等级/(级别 * 10)))
     * 转化后公式 = 物爆等级/ ((级别*10) + 1 +物爆等级)
     */
    public int getPCri(){
        /*return (attributes[ATTR_PCRI] * 10 + criRateAdd) / 10;*/
    	/*int criRate = attributes[ATTR_PCRI] /(level * 10);
    	
    	criRate = 1 - 1 / (1 + criRate);
    	return getPCriExtraRate() + criRate;*/
    	return  attributes[ATTR_PCRI] * 100 /((level  * 10) + 1 +  attributes[ATTR_PCRI]) + getPCriExtraRate();
    }
    
    /**
     * @return 额外暴击率加成（来自套装效果， 技能）
     */
    public int getPCriExtraRate(){
    	return criRateAdd / 10;
    }
    /**
    * @return新版的暴击率 暴击等级转换暴击率 加上额外暴击率加成（来自套装效果， 技能）
    * 暴击等级转换公式  = 1 - 1/(1 + (物暴等级/(级别 * 10)))
    * 魔法暴击公式为 = 魔爆等级/ ((级别*10) + 1 +魔爆等级)
    */
    public int getMCri(){
        /*return (attributes[ATTR_MCRI] * 10 + criRateAdd) / 10;*/
    	/*int criRate = attributes[ATTR_MCRI] /(level * 10);
    	
    	criRate = 1 - 1 / (1 + criRate);
    	return getPCriExtraRate() + criRate;*/
    	return attributes[ATTR_MCRI] * 100 /((level  * 10) + 1 +  attributes[ATTR_MCRI]) + getPCriExtraRate();
    }
    
    public int getMcriExtraRate(){
    	return criRateAdd / 10;
    }
  /*  public boolean testPCri(){
        return Skill.getPercentRate(getPCri());
    }*/

    public boolean testMCri(){
    	return Skill.getPercentRate(getMCri());
    }
    /**
     * @param target
     * @param our
     * @param ourPet
     * @param them
     * @param themPet
     * @return压制后事否成功
     */
    public boolean testPCriAttackFlag(BattleSprite target, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int percentRate = getPCri();//原始暴击
    	//mengjie add 增加暴击
    	percentRate = percentRate + testAddPCriAttack(our, ourPet, them, themPet);//星辉套装
//        if(testAddPCriAttackFlag(target, our, ourPet, them, themPet)){
//            damage *= CRI_RATE;
//            damage = damage * (100 + getSuitAddPriAttack(target, our, ourPet, them, themPet)) / 100;
//            cri = Skill.ATTACK_CRI;
//        }
    	percentRate  = percentRate - target.testPCriReduceAttack(our, ourPet, them, themPet);
    	if(percentRate < 0){
    		percentRate = 0;
    	}
    	return Skill.getPercentRate(percentRate);
    }
    
    /**
     * @param target
     * @return压制后的物理暴击率
     */
    public int testPCriReduceAttack(BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int result = 0;
        
        if(battleSuitEffect != null){
            /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getPriCriReduceRate(this, our, ourPet, them, themPet);
                if(tmp > result){
                    result = tmp;
                }
            }
        }
        //增加体力带来的免爆
        result += getReduceCriAttack();
        return result;
    	//if(percentRate < 0){
    		//percentRate = 0;
    	//}
    	//return Skill.getPercentRate(percentRate);
    }
    
    public boolean testMCriAttackFlag(BattleSprite target, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int percentRate = getMCri();
    	
    	percentRate = percentRate + testAddMCriAttack(our, ourPet, them, themPet);//星辉套装
    	
    	percentRate  = percentRate - target.testMCriReduceAttack(our, ourPet, them, themPet);
    	if(percentRate < 0){
    		percentRate = 0;
    	}
    	return Skill.getPercentRate(percentRate);
    }
    
    /**
     * @param target
     * @return压制后的魔法暴击率
     */
	public int testMCriReduceAttack(BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	/*int percentRate = getMCri();
    	BattleSuitEffect[] battleSuitEffects = target.battleSuitEffect;
    	if(battleSuitEffects != null){
    		 for(int i = 0; i < battleSuitEffects.length; i++){
    			 BattleSuitEffect battleSuitEffect = battleSuitEffects[i];
    			 if(battleSuitEffect.getType() == SuitEffect.EFFECT_TYPE_REDUCE_MRI_CRI || battleSuitEffect.getType() == SuitEffect.EFFECT_TYPE_REDUCE_PRI_MRI_CRI ){
    				 //percentRate = percentRate - battleSuitEffect.getValue()/10000;
    				 percentRate = percentRate - battleSuitEffect.getMagicCriReduceRate(this, our, ourPet, them, themPet);
    			 }
    		 }
    	}
    	if(percentRate < 0){
    		percentRate = 0;
    	}
        return Skill.getPercentRate(percentRate);*/
		int result = 0;
        
        if(battleSuitEffect != null){
            /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getMagicCriReduceRate(this, our, ourPet, them, themPet);
                if(tmp > result){
                    result = tmp;
                }
            }
        }
        
        //增加体力带来的免爆
        result += getReduceCriAttack();
        
        return result;
    }
    /**
     * @param target
     * @return 减少魔法暴击伤害
     */
    public int getReduceMriAttack(BattleSprite target, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int reduceReslut = 0;
    	
    	if(battleSuitEffect != null){
    	    /**
             * 修改套装属性不可叠加
             */
    	    for(int i = 0; i < battleSuitEffect.length; i++){
    	        int tmp = battleSuitEffect[i].getMagicCriReduceAttackRate(this, our, ourPet, them, themPet);
    	        
    	        if(tmp > reduceReslut){
    	            reduceReslut = tmp;
    	        }
            }
        }
    /*	BattleSuitEffect[] battleSuitEffects = target.battleSuitEffect;
    	if(battleSuitEffects != null){
    		 for(int i = 0; i < battleSuitEffects.length; i++){
    			 BattleSuitEffect battleSuitEffect = battleSuitEffects[i];
    			 if(battleSuitEffect.getType() == SuitEffect.EFFECT_TYPE_REDUCE_MRI_CRI || battleSuitEffect.getType() == SuitEffect.EFFECT_TYPE_REDUCE_PRI_MRI_CRI ){
    				 //reduceReslut = reduceReslut + (battleSuitEffect.getValue()%10000)/100;
    				 reduceReslut = reduceReslut + battleSuitEffect.getMagicCriReduceAttackRate(this, our, ourPet, them, themPet);
    			 }
    		 }
    	}*/
    	if(reduceReslut > 100){
    		reduceReslut = 99;
    	}
    	return reduceReslut;
    }
    
    /**
     * @param target
     * @return 减少物理暴击伤害 (套装的免伤， 体力带来的免爆引发的免伤)
     */
    public int getSuitReducePriAttack(BattleSprite target, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int reduceReslut = 0;
    	
    	if(battleSuitEffect != null){
    	    /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getPriCriReduceAttackRate(this, our, ourPet, them, themPet);
                if(tmp > reduceReslut){
                    reduceReslut = tmp;
                }
            }
        }
    	/*BattleSuitEffect[] battleSuitEffects = target.battleSuitEffect;
    	if(battleSuitEffects != null){
    		 for(int i = 0; i < battleSuitEffects.length; i++){
    			 BattleSuitEffect battleSuitEffect = battleSuitEffects[i];
    			 if(battleSuitEffect.getType() == SuitEffect.EFFECT_TYPE_REDUCE_PRI_CRI || battleSuitEffect.getType() == SuitEffect.EFFECT_TYPE_REDUCE_PRI_MRI_CRI ){
    				 //reduceReslut = reduceReslut + (battleSuitEffect.getValue()%10000)/100;
    				 reduceReslut = reduceReslut + battleSuitEffect.getPriCriReduceAttackRate(this, our, ourPet, them, themPet);
    			 }
    		 }
    	}*/
    	reduceReslut += getReduceCriAttack();
    	
    	if(reduceReslut > 100){
    		reduceReslut = 99;
    	}
    	return reduceReslut;
    }
    //mengjie add
    
    /**
     * @return 体力带来的免爆几率
     * 免爆公式为 = 免爆等级/ ((级别*10) + 1 +免爆等级)
     */
    public int getReduceCriAttack(){
    	int result = 0;
    	/*int vit = attributes[ATTR_NOCRI]/ (level * 10 + 1);
    	result = 1 - 1 / (1 + vit);
    	return result;*/
    	result = attributes[ATTR_NOCRI] * 100/ ((level * 10) + 1 + attributes[ATTR_NOCRI]);
    	return result;
    }
    /**
     * @param target
     * @return提升后的物理暴击率
     */
    public int testAddPCriAttack(BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int result = 0;
    	
        if(battleSuitEffect != null){
            /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getPriCriAddRate(this, our, ourPet, them, themPet);
                if(tmp > result){
                    result = tmp;
                }
            }
        }
        
        return result;
    }

    /**
     * @param target
     * @return增加魔法暴击率
     */
	public int testAddMCriAttack(BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int result = 0;
    	
        if(battleSuitEffect != null){
            /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getMagicCriAddRate(this, our, ourPet, them, themPet); 
                if(tmp > result){
                    result = tmp;
                }
            }
        }
        
        return result;
    }
    /**
     * @param target
     * @return 增加魔法暴击伤害
     */
    public int getSuitAddMriAttack(BattleSprite target, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int reduceReslut = 0;
    	
    	if(battleSuitEffect != null){
    	    /**
             * 修改套装属性不可叠加
             */
    	    for(int i = 0; i < battleSuitEffect.length; i++){
    	        int tmp = battleSuitEffect[i].getMagicCriAddAttackRate(this, our, ourPet, them, themPet); 
    	        if(tmp > reduceReslut){
    	            reduceReslut = tmp;
    	        }
            }
        }
    	
    	if(reduceReslut > 100){
    		reduceReslut = 99;
    	}
    	
    	return reduceReslut;
    }
    /**
     * @param target
     * @return 增加物理暴击伤害
     */
    public int getSuitAddPriAttack(BattleSprite target, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	int reduceReslut = 0;
    	
    	if(battleSuitEffect != null){
    	    /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getPriCriAddAttackRate(this, our, ourPet, them, themPet);
            	if(tmp > reduceReslut){
            	    reduceReslut = tmp;
            	}
            }
        }
    	
    	if(reduceReslut > 100){
    		reduceReslut = 99;
    	}
    	
    	return reduceReslut;
    }
    //mengjie add
    public int getSpeed(){
        int skillId = skill != null ? skill.id : -1;
//        Skill skill = Skill.getSkill(skillId);
        int result = 0;

        if(skill == null){
//            switch(skillId){
//                case Skill.SKILL_ATTACK:
//                case Skill.SKILL_CATCH:
//                    result = 0x6FFFFFF + attributes[ATTR_AGI];
//
//                    break;
//                case Skill.SKILL_RUN:
//                    result = Integer.MAX_VALUE;
//
//                    break;
//                case Skill.SKILL_ITEM:
//                    result = 0x7FFFFFF + attributes[ATTR_AGI];
//
//                    break;
//                case Skill.SKILL_STAY:
//                    result = Integer.MAX_VALUE;
//
//                    break;
//                default:
            result = Integer.MIN_VALUE;
//                    break;
//            }
        }else{
            switch(skill.speedMethod){
                case Skill.SPEED_METHOD_ORDER_1:
                    result = Integer.MAX_VALUE;

                    break;
                case Skill.SPEED_METHOD_ORDER_2:
                    result = 0x7FFFFFF + attributes[ATTR_AGI] + (attributes[ATTR_INT] + attributes[ATTR_STR]) * 50 / 100 + attributes[ATTR_VIT] * 20 / 100 + level * 5 + Skill.randGen.nextInt(120);

                    break;
                case Skill.SPEED_METHOD_ORDER_3:
                    result = 0x6FFFFFF + attributes[ATTR_AGI] + (attributes[ATTR_INT] + attributes[ATTR_STR]) * 50 / 100 + attributes[ATTR_VIT] * 20 / 100 + level * 5 + Skill.randGen.nextInt(120);

                    break;
                case Skill.SPEED_METHOD_ORDER_4:
                    result = 0x5FFFFFF + attributes[ATTR_AGI] + (attributes[ATTR_INT] + attributes[ATTR_STR]) * 50 / 100 + attributes[ATTR_VIT] * 20 / 100 + level * 5 + Skill.randGen.nextInt(120);

                    break;
                case Skill.SPEED_METHOD_FIRST:
                    result = Integer.MAX_VALUE;

                    break;
                case Skill.SPEED_METHOD_LAST:
                    result = Integer.MIN_VALUE;
                    break;
            }
        }

        return result;
    }

    private int getSuitPhysicalAttackAdd(BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        int result = 0;
        
        if(battleSuitEffect != null){
            /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getPhysicalAttackAdd(this, our, ourPet, them, themPet);
                if(tmp > result){
                    result = tmp;
                }
            }
        }
        
        return result;
    }
    
    private int getSuitMagicAttackAdd(BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        int result = 0;
        
        if(battleSuitEffect != null){
            /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getMagicAttackAdd(this, our, ourPet, them, themPet);
                if(tmp > result){
                    result = tmp;
                }
            }
        }
        
        return result;
    }
    
    private int getSuitPhysicalDefenceAdd(BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        int result = 0;
        
        if(battleSuitEffect != null){
            /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getPhysicalDefenceAdd(this, our, ourPet, them, themPet);
                if(tmp > result){
                    result = tmp;
                }
            }
        }
        
        return result;
    }
    
    private int getSuitMagicDefenceAdd(BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        int result = 0;
        
        if(battleSuitEffect != null){
            /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getMagicDefenceAdd(this, our, ourPet, them, themPet);
                if(tmp > result){
                    result = tmp;
                }
            }
        }
        
        return result;
    }
    
    private int getAntiShineHitAdd(){
        int result = 0;
        
        if(battleSuitEffect != null){
            /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getAntiShineHitAdd(this);
                
                if(tmp > result){
                    result = tmp;
                }
            }
        }
        
        return result;
    }
    
    private int getAntiShineHitAdd(int type){
        int result = 0;
        
        if(battleSuitEffect != null){
            /**
             * 修改套装属性不可叠加
             */
            for(int i = 0; i < battleSuitEffect.length; i++){
                int tmp = battleSuitEffect[i].getAntiShineHitAdd(this,type);
                
                if(tmp > result){
                    result = tmp;
                }
            }
        }
        
        return result;
    }
    
    private int getPetNormalDefence(BattleSprite pet){
        return 18+ pet.level * 2;
    }
    
    private int[] getPhysicalDamage(BattleSprite target, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        /*//物理伤害吸收=SQRT(5*物理防御*(100/攻击者等级)) //伤害吸收最多80%*/    	
    	
        int inDefence = target.getDefence();
        int calDefence = inDefence;
        
        int suitDefenceAdd = target.getSuitPhysicalDefenceAdd(them, themPet, our, ourPet);
        
        if((target.bsType ==  TYPE_PLAYER_PET || target.bsType == TYPE_MONSTER_PET) && suitDefenceAdd > 0){
        	calDefence += getPetNormalDefence(target);
        }
        
        calDefence = calDefence * (100 + suitDefenceAdd) / 100;
        //神圣宝辉减防
        int defEffectRate = getCurDefEffectRate(target);
        calDefence = calDefence - calDefence * defEffectRate / 100;
        
        int attack = Skill.random(attributes[ATTR_PMIN], attributes[ATTR_PMAX]);
        attack += attackMaxAdd;
        int cri = Skill.ATTACK_NO_CRI;

        attack = attack * (100 + attackAdd + getSuitPhysicalAttackAdd(our, ourPet, them, themPet)) / 100;
        //新版免伤 ( 物品伤害吸收    )  = 1 - 1/( 1+ (防御等级/((级别 +　9) * 11));
        //免伤转换公式 =防御等级 /((级别 +　９)　×11 +防御等级))

        int damageSorb  =  calDefence * 100 / ((level + 9) * 16  + calDefence);
        /*if(damageSorb > 80){
            damageSorb = 80;
        }*/
        
        if(damageSorb < 0){
        	damageSorb = 0;
        }
        int damage = attack * (100 - damageSorb) / 100;

        if(testPCriAttackFlag(target, our, ourPet, them, themPet)){
          /*  damage *= CRI_RATE;
          //mengjie add 增加暴击
            damage = damage * (100 + getSuitAddMriAttack(target, our, ourPet, them, themPet)) / 100;
            
            damage = damage * (100 - target.getSuitReducePriAttack(target, our, ourPet, them, themPet)) / 100;
            cri = Skill.ATTACK_CRI;*/ //放弃此方法的原因是 免伤， 这里如果60%的话，会有问题
        	
        	int extraDamage = damage;
        	extraDamage = extraDamage * (100 + getSuitAddPriAttack(target, our, ourPet, them, themPet)) / 100;
            
        	//这里是免伤
        	extraDamage = extraDamage * (100 - target.getSuitReducePriAttack(target, our, ourPet, them, themPet)) / 100;
        	if(extraDamage < 0){
        		extraDamage = 0;
        	}
        	damage += extraDamage;
        	if(testDiamondShinePCriAttackFlag(target, our, ourPet, them, themPet)){
        		damage += extraDamage;
        	}
        	cri = Skill.ATTACK_CRI;
        }
        
        return new int[]{
                        damage, cri
        };
    }
    
    public boolean testDiamondShinePCriAttackFlag(BattleSprite target, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	if(player == null){
    		return false;
    	}else{
	    	int percentRate = player.getDiamondShineBufAttri(DiamondShineBuf.SERVER_PHYSIC_CRI);
	    	return Skill.getPercentRate(percentRate);
    	}
    }
    
    public boolean testDiamondShineMCriAttackFlag(BattleSprite target, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
    	if(player == null){
    		return false;
    	}else{
	    	int percentRate = player.getDiamondShineBufAttri(DiamondShineBuf.SERVER_MAGIC_CRI);
	    	return Skill.getPercentRate(percentRate);
    	}
    }
    
    private int[] getMagicDamage(BattleSprite target, BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        //魔法伤害吸收=SQRT(5*魔法防御*(100/攻击者等级)) //伤害吸收最多80%
        int inMagicDefence = target.getMagicDefence();
        int calDefence = inMagicDefence;
        
        int suitDefenceAdd = target.getSuitMagicDefenceAdd(them, themPet, our, ourPet);
        
        if((target.bsType ==  TYPE_PLAYER_PET || target.bsType == TYPE_MONSTER_PET) && suitDefenceAdd > 0){
            calDefence += getPetNormalDefence(target);
        }
        
        calDefence = calDefence * (100 + suitDefenceAdd) / 100;
        //神圣宝辉减防
        int defEffectRate = getCurDefEffectRate(target);
        calDefence = calDefence - calDefence * defEffectRate / 100;
        
        int mattack = Skill.random(attributes[ATTR_MMIN], attributes[ATTR_MMAX]);
        int cri = Skill.ATTACK_NO_CRI;

        mattack = mattack * (100 + magicAttackAdd + getSuitMagicAttackAdd(our, ourPet, them, themPet)) / 100;

       // int damageSorb = (int)sqrt(5 * calDefence * 75 / target.level);
        //int damageSorb  = 1 - 1/ (1 + (calDefence/((level + 9) * 11)));
        int damageSorb  =  calDefence * 100 / ((level + 9) * 16 + calDefence);
      /*  if(damageSorb > 80){
            damageSorb = 80;
        }*/
        
        if(damageSorb < 0){
        	damageSorb = 0;
        }
        int damage = mattack * (100 - damageSorb) / 100;

        if(testMCriAttackFlag(target, our, ourPet, them, themPet)){
           /* damage *= CRI_RATE;
            //mengjie add 增加暴击
            damage = damage * (100 + getSuitAddMriAttack(target, our, ourPet, them, themPet)) / 100;
            
            damage = damage * (100 - target.getSuitReduceMriAttack(target, our, ourPet, them, themPet)) / 100;
            cri = Skill.ATTACK_CRI;*/
        	int extraDamage = damage;
        	extraDamage = extraDamage * (100 + getSuitAddMriAttack(target, our, ourPet, them, themPet)) / 100;
            
        	//这里是免伤
        	extraDamage = extraDamage * (100 - target.getReduceMriAttack(target, our, ourPet, them, themPet)) / 100;
        	if(extraDamage < 0){
        		extraDamage = 0;
        	}
        	damage += extraDamage;
        	
        	if(testDiamondShineMCriAttackFlag(target, our, ourPet, them, themPet)){
        		damage += extraDamage;
        	}
        	
        	cri = Skill.ATTACK_CRI;
        }
        
       
        
        return new int[]{
                        damage, cri
        };
    }

    public static final long START_BIT = (~Long.MAX_VALUE) >>> 1;

    public static final long sqrt(long x){
        if(x < 0){
            return 0;
        }

        long y = 0;
        long b = START_BIT;

        while(b > 0){
            if(x >= y + b){
                x -= y + b;
                y >>= 1;
                y += b;
            }else{
                y >>= 1;
            }

            b >>= 2;
        }

        return y;
    }

    public boolean testHit(int inFlee, int action){
        int tmp = calculateHitRate(inFlee, action);
        //mengjie add 压制套装效果
        if (player != null){
        	int suitshineeffect = getAntiShineHitAdd();
        	if (lastmissflag == 1){
        		tmp = tmp + suitshineeffect;
        	}
        	if (tmp>100){
        		tmp = 100;
        	}
        	boolean shineflag = Skill.getPercentRate(tmp);
            if (shineflag){
            	lastmissflag = 0;
            }else{
            	lastmissflag = 1;
            }
            return shineflag;
        }else{
        	int suitshineeffect = getAntiShineHitAdd(70);
        	if (lastmissflagpet == 1){
        		tmp = tmp + suitshineeffect;
        	}
        	if (tmp>100){
        		tmp = 100;
        	}
        	boolean shineflag = Skill.getPercentRate(tmp);
            if (shineflag){
            	lastmissflagpet = 0;
            }else{
            	lastmissflagpet = 1;
            }
            return shineflag;
        }
//        return Skill.getPercentRate(tmp);
    }
    public boolean testHitPet(int inFlee, int action){
        int tmp = calculateHitRate(inFlee, action);
        //mengjie add 压制套装效果
        if (player != null){
        	int suitshineeffect = getAntiShineHitAdd();
        	if (lastmissflag == 1){
        		tmp = tmp + suitshineeffect;
        	}
        	if (tmp>100){
        		tmp = 100;
        	}
        	boolean shineflag = Skill.getPercentRate(tmp);
            if (shineflag){
            	lastmissflag = 0;
            }else{
            	lastmissflag = 1;
            }
            return shineflag;
        }else{
        	int suitshineeffect = getAntiShineHitAdd(70);
        	if (lastmissflagpet == 1){
        		tmp = tmp + suitshineeffect;
        	}
        	if (tmp>100){
        		tmp = 100;
        	}
        	boolean shineflag = Skill.getPercentRate(tmp);
            if (shineflag){
            	lastmissflagpet = 0;
            }else{
            	lastmissflagpet = 1;
            }
            return shineflag;
        }
        //return Skill.getPercentRate(tmp);
    }
    public int calculateHitRate(int inFlee, int action){
     /*   //命中率=95-敌人敏捷*8/(100+敌人级别)-2*SIGN(敌方敏捷-我方敏捷)*(SQRT(4+ABS(敌方敏捷-我方敏捷))-2) //命中率最多100%
        
    	
    	int result = 0;
        int sign = (this.target.attributes[ATTR_AGI] - this.attributes[ATTR_AGI]) >= 0? 1: -1;
        int absAgi = (this.target.attributes[ATTR_AGI] - this.attributes[ATTR_AGI]) * sign;

        result = (int)(95 - this.target.attributes[ATTR_AGI] * 8 / (100 + this.target.level) - 2 * sign * ((int)sqrt(4 + absAgi) - 2));*/
    	
    	
    	//新版命中= 1 - 1 /((1 + (命中等级 - 50)/(级别 * 5 + 1)));  //命中只从装备获得
    	//命中转换公式=  （命中级别 -５０）/ (级别 *５　＋ 1 + 命中级别 -５０)  需要扩大100倍

    	//新版闪避 = 1 - 1/(1 + (闪避等级 - 50)/(级别 * 5 + 1))      //1个敏捷就是一个闪避等级

    	//闪避转换公式     	=  （闪避级别-５０）/ (级别 *５　＋ 1 +闪避级别-５０)  需要扩大100倍

    	//新版命中率 = 100 +　目标命中 - 目标闪避
    	int result = 0;
        switch(action){
            case ACTION_PATTACK:
                result += getPHit();

                break;
            case ACTION_MATTACK:
                result += getMHit();

                break;
        }
        int hitDenominator  = (level * 5 + 1 + result);
        if(hitDenominator < 1){
        	hitDenominator = 1;
        }

        result = result * 100/ hitDenominator;
        if(result < 0){ //命中最低为0
        	result = 0;
        }
       
      /*  int flee = ((this.target.getFlee() - 50)/(level * 5 + 1));
        if(flee <= 0){
        	flee = 1;
        }
        flee = 1 -1/(1 + flee);*/
        //增加对少的级别差命中
        result = result + (level - target.level)* 100 /500;
        
        result += 95; //基础命中为95
        int flee = 0;
        if(this.target.getFlee() < 200){
        	flee = this.target.getFlee() / 20;
        }else{
        	int fleeDenominator = (level * 5 + 1 +this.target.getFlee()- 50);
        	if(fleeDenominator < 1){
        		fleeDenominator = 1;
        	}
        	flee = (this.target.getFlee() - 50) * 100 / fleeDenominator;
        	if(flee < 0){
        	}else if(flee < 5){
        		flee = 5;
        	}
        }
        result -= flee;
        
        //未计算技能效果之前，闪避最高设定为80％，也就是命中率最低20％
        //if(result < 20){
        ///   result = 20;
        //}
        
        result += hitAdd;
        result -= this.target.fleeAdd;

        if(result > 100){
            result = 100;
        }
//        //更改闪避率上限！最高闪避70%
//        if(result < 30){
//            result = 30;
//        }
        return result;
    }

    public boolean testRun(int enemyLevel, int bout){
        int tmp = 2 * enemyLevel - 2 * level + 10;

        if(tmp < 1){
            tmp = 1;
        }

        int percent = 100 / tmp + 10 * (bout - 1) * (bout - 1);

        if(Skill.getPercentRate(percent)){
            return true;
        }else{
            return false;
        }
    }

    public boolean testCannotBattle(){
        return (debufStatus == Skill.STATUS_DIE || debufStatus == Skill.STATUS_RUNAWAY || debufStatus == Skill.STATUS_CATCHED);
    }

    public boolean testRunAway(){
        return debufStatus == Skill.STATUS_RUNAWAY;
    }

    public boolean testCatched(){
        return debufStatus == Skill.STATUS_CATCHED;
    }

    public boolean testDie(){
        return debufStatus == Skill.STATUS_DIE;
    }

    public void changeHp(int inc, Vector battleMovie, BattleDataProcess battleDataProcess){
        int oldhp = hp;

        if(inc < 0){
            hurted += Math.abs(inc);
        }
        
        hp += inc;

        if(hp > attributes[ATTR_HPMAX]){
            hp = attributes[ATTR_HPMAX];
        }

        if(hp <= 0){
            if(oldhp > 0 || hp < 0){
                setDeBufStatus(1, Skill.STATUS_DIE, 0, 0, 0, 0, 0);
            }

            hp = 0;
        }

        if(player != null){
            player.setHp(hp);
        }else if(pet != null){
            pet.setHp(hp);
        }
    }

    public void changeMp(int inc){
        mp += inc;

        if(mp > attributes[ATTR_MPMAX]){
            mp = attributes[ATTR_MPMAX];
        }

        if(mp <= 0){
            mp = 0;
        }
        if(player != null){
            player.setMp(mp);
        }else if(pet != null){
            pet.setMp(mp);
        }
    }

    public void initSpecial(int attackMin, int attackMax, int defence, int magicAttackMin, int magicAttackMax, int magicDefence, int flee, int hit, int pcri, int mcri, int hpLimit, int mpLimit){
        this.attributes[ATTR_PMIN] += attackMin;
        this.attributes[ATTR_PMAX] += attackMax;
        this.attributes[ATTR_PDEF] += defence;
        this.attributes[ATTR_MMIN] += magicAttackMin;
        this.attributes[ATTR_MMAX] += magicAttackMax;
        this.attributes[ATTR_MDEF] += magicDefence;
        this.attributes[ATTR_PHIT] += hit;
        this.attributes[ATTR_MHIT] += hit;
        this.attributes[ATTR_FLEE] += flee;
        this.attributes[ATTR_PCRI] += pcri;
        this.attributes[ATTR_MCRI] += mcri;
        this.attributes[ATTR_HPMAX] += hpLimit;
        this.attributes[ATTR_MPMAX] += mpLimit;
    }

    public boolean canAction(){
        boolean result = true;

        switch(debufStatus){
            case Skill.STATUS_NORMAL:
            case Skill.STATUS_POISON:
            case Skill.STATUS_FROST:
                result = true;

                break;
            case Skill.STATUS_STONE:
            case Skill.STATUS_CONFUSE:
            case Skill.STATUS_SLEEP:
            case Skill.STATUS_FAINT:
            case Skill.STATUS_STOP:
                result = false;

                break;
            case Skill.STATUS_ANTI_MAGIC:
            case Skill.STATUS_ANTI_ATTACK:
            case Skill.STATUS_SORB_MAGIC:
            case Skill.STATUS_SORB_ATTACK:
                result = true;

                break;
            case Skill.STATUS_DIE:
            case Skill.STATUS_RUNAWAY:
            case Skill.STATUS_CATCHED:
                result = false;

                break;
            case Skill.STATUS_PROTECTED:
                result = true;

                break;
        }

        return result;
    }

    public void addEnmity(BattleSprite bs, int enmity){
        Integer value = (Integer)enmities.get(bs);
        if(value != null){
            enmities.put(bs, new Integer(value.intValue() + enmity));
        }else{
            enmities.put(bs, new Integer(enmity));
        }
    }

    public void clearEnmity(BattleSprite bs){
        enmities.remove(bs);
    }

    public int getEnmity(BattleSprite bs){
        Integer ret = (Integer)enmities.get(bs);
        if(ret != null)
            return ret.intValue();
        return 0;
    }

    public int getAttribute(int attr){
        switch(attr){
            case ATTR_STR:
            case ATTR_AGI:
            case ATTR_VIT:
            case ATTR_INT:
            case ATTR_PMIN:
            case ATTR_PMAX:
            case ATTR_MMIN:
            case ATTR_MMAX:
            case ATTR_HPMAX:
            case ATTR_MPMAX:
                return attributes[attr];
            case ATTR_PDEF:
                return getDefence();
            case ATTR_MDEF:
                return getMagicDefence();
            case ATTR_PHIT:
                return getPHit();
            case ATTR_MHIT:
                return getMHit();
            case ATTR_FLEE:
                return getFlee();
            case ATTR_PCRI:
                return getPCri();
            case ATTR_MCRI:
                return getMCri();
        }

        return 0;
    }

    public int getShowAttribute(int attr){
        int result = 0;

        switch(attr){
            case ATTR_PHIT:
            case ATTR_MHIT:
            case ATTR_FLEE:
                BattleSprite tmpSprite = new BattleSprite();
                int tmpAttr = this.level + this.level / 4;

                tmpSprite.initBattleData(TYPE_PLAYER, this.level, tmpAttr, tmpAttr, tmpAttr, tmpAttr, 0, 0, 0, 0, 0, 0, null,null,null,null,null);
                setTarget(tmpSprite, 0);

                if(attr == ATTR_PHIT){
                    result = calculateHitRate(0, BattleSprite.ACTION_PATTACK);
                }else if(attr == ATTR_MHIT){
                    result = calculateHitRate(0, BattleSprite.ACTION_MATTACK);
                }else{
                    tmpSprite.setTarget(this, 0);

                    int p = tmpSprite.calculateHitRate(0, BattleSprite.ACTION_PATTACK);
                    int m = tmpSprite.calculateHitRate(0, BattleSprite.ACTION_MATTACK);

                    result = (200 - (p + m)) / 2;
                }

                target = null;
                targetIndex = -1;
                targetType = 0;

                break;
            default:
                result = getAttribute(attr);
        }

        return result;
    }
    
    public int getCurDefEffectRate(BattleSprite target){
    	if(player==null)
    		return 0;
    	int curRate = target.getCurDefEffectRate();
    	int curIndex = target.getCurDefEffectIndex();
    	int maxRate = DEFEFFECT_RATE[curIndex] * DEFEFFECT_MAXLEVEL;
    	if(curRate > maxRate){
    		curRate = maxRate;
    	}
    	int newIndex = player.getHolyGemLightLevel() - 3;
    	if(newIndex >= 0 && newIndex < DEFEFFECT_RATE.length){
    		int newRate = DEFEFFECT_RATE[newIndex];
    		curIndex = newIndex > curIndex?newIndex:curIndex;
    		maxRate = DEFEFFECT_RATE[curIndex] * DEFEFFECT_MAXLEVEL;
    		if(curRate < maxRate){
    			curRate = curRate + newRate > maxRate ? maxRate : curRate + newRate;
    		}
    		target.setCurDefEffectRate(curRate);
			target.setCurDefEffectIndex(curIndex);
			target.setCurDefEffectTime(DEFEFFECT_TIME);
    	}else if(target.getCurDefEffectTime() <=0 ){
    		return 0;  
    	}
    	return curRate;
    }
    
    public void updateCurDefEffectRate(){
    	int curTime = getCurDefEffectTime();
    	if(curTime > 0){
    		curTime--;
    		if(curTime<=0){
    			setCurDefEffectRate(0);
    			setCurDefEffectIndex(0);
    		}
    		setCurDefEffectTime(curTime);
    	}
    }
}