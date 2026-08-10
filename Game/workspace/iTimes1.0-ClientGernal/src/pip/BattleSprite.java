package pip;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Graphics;


public abstract class BattleSprite{
    protected byte bsType;
    public int groupIndex;

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

    protected int[] attributes = new int[17];

    public static final String[] ATTR_NAMES = new String[]{
                    "力量", "敏捷", "体力", "智力", "攻击", "攻击", "防御", "魔攻", "魔攻", "魔防", "命中", "魔命", "闪躲", "暴击", "魔暴", "生命", "魔法"
    };

    protected short level; //等级
    //    protected short strength; //力量
    //    protected short agility; //敏捷
    //    protected short vitality; //体力
    //    protected short intelligence; //智力
    protected long luck; //幸运
    protected int hp; //剩余生命
    protected int mp; //剩余魔法
    protected int usedMp; //本次出招所消耗mp

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

    protected short[] skillList; //技能id列表
    protected int skillId; //使用的技能id

    public BattleSprite target; //目标
    protected GameItem useItem; //使用物品
    public int targetIndex; //目标索引下标
    public int targetType; //目标类型 0=无选择 1=队友 2=敌人
    
    public String useSkillName = "";

    public Hashtable bufTable = new Hashtable(); //buf列表 int[] [0] 剩余回合 [1] 状态 attackAdd [2] 状态级别 magicAttackAdd [3] 状态参数1 defenceAdd [4] 状态参数2 magicDefenceAdd [5] hitAdd [6] fleeAdd [7] criAdd [8]statusAdd
    private static Integer statusIndexDebuf = new Integer(0); //有害buf编号
    private static Integer statusIndexBuf = new Integer(1); //有益buf编号
    private int statusIndexAttrBuf = 2; //属性buf起始编号

    private Hashtable coolDownTable = new Hashtable(); //冷却列表

    public static final byte FRAMESEQUENCE_STAND = 0;
    public static final byte FRAMESEQUENCE_RUN = 1;
    public static final byte FRAMESEQUENCE_ATTACK = 2;
    public static final byte FRAMESEQUENCE_BEATED = 3;
    public static final byte FRAMESEQUENCE_RUNBACK = 4;
    public static final byte FRAMESEQUENCE_DIE = 5;

    //#if (Directory == NK-6681) || (Directory == MT-V300)
    //# public static final byte[][] EFFECTSEQUENCE_LEFT = new byte[][]{
    //0:物理攻击效果序列
    //#             {
    //#                             0, 0, 1, 1
    //#             },
    //1:魔法攻击效果序列
    //#             {
    //#                             0, 0, 1, 1
    //#             },
    //2:状态攻击效果序列
    //#             {
    //#                             3, 6, 7, 5, 4, 2, 2
    //#             },
    //3:补血增益效果序列
    //#             {
    //#                             0, 0, 1, 1
    //#             },
    //4:复活效果序列
    //#             {
    //#                             0, 0, 1, 1
    //#             },
    //5:解除效果序列
    //#             {
    //#                             0, 0, 1, 1
    //#             },
    //6:狂战起手效果序列
    //#             {
    //#                             0, 0, 1, 1
    //#             },
    //7:魔战起手效果序列
    //#             {
    //#                             0, 0, 1, 1
    //#             },
    //8:盾防起手效果序列
    //#             {
    //#                             0, 0, 1, 1
    //#             },
    //9:辅助起手效果序列
    //#             {
    //#                             0, 0, 1, 1
    //#             },
    //10:物品使用效果序列
    //#             {
    //#                             0, 0, 1, 1
    //#             }
    //# };
    //#else
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
    //#endif

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

    public static final int FLYSTRING_HEIGHT = GameState.LINE_HEIGHT + 5;

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

    public byte battleDirect = Sprite.RIGHT;

    //------------------

    public static final byte CRI_RATE = 2;

    public static final byte ACTION_PATTACK = 0;
    public static final byte ACTION_MATTACK = 1;

    public static final byte TYPE_PLAYER = 0;
    public static final byte TYPE_MONSTER = 1;
    public static final byte TYPE_NET_PLAYER = 2;
    public static final byte TYPE_PLAYER_PET = 3;
    public static final byte TYPE_MONSTER_PET = 4;

    public static final byte GROUP_OUR = 0;
    public static final byte GROUP_THEM = 1;
    public static final byte GROUP_OUR_PET = 2;
    public static final byte GROUP_THEM_PET = 3;

    public String name;

    public void initBattleData(byte bsType, int level, int vitality, int strength, int intelligence, int agility, long luck, int hp, int mp){
        this.bsType = bsType;
        this.level = (short)level;
        //        this.strength = (short)strength;
        //        this.agility = (short)agility;
        //        this.vitality = (short)vitality;
        //        this.intelligence = (short)intelligence;

        attributes[ATTR_STR] = (short)strength;
        attributes[ATTR_AGI] = (short)agility;
        attributes[ATTR_VIT] = (short)vitality;
        attributes[ATTR_INT] = (short)intelligence;
        this.luck = luck;
        this.hp = hp;
        this.mp = mp;

        initAttribute();

        setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);
        setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);

        if(this instanceof Sprite){
            battleDirect = Sprite.LEFT;
        }

    }

    public abstract ImageSet getImageSet();

    public void reCalculateBattleData(){
        initBattleData(bsType, level, attributes[ATTR_VIT], attributes[ATTR_STR], attributes[ATTR_INT], attributes[ATTR_AGI], luck, hp, mp);
    }

    public void addHp(int inc){
        hp += inc;
        if(hp > attributes[ATTR_HPMAX])
            hp = attributes[ATTR_HPMAX];
    }

    public void addMp(int inc){
        mp += inc;
        if(mp > attributes[ATTR_MPMAX])
            mp = attributes[ATTR_MPMAX];
    }

    public void initEquipmentData(int weaponAttack, int equipDefence){
        this.weaponAttack = weaponAttack;
        this.equipDefence = equipDefence;
    }

    public void AddAttrBuf(int bout, int attackAdd, int magicAttackAdd, int defenceAdd, int magicDefenceAdd, int hitAdd, int fleeAdd, int criRateAdd, int phyDamageAdd, int mgcDamageAdd,
                    int letPhyDamageValue, int letMgcDamageValue, int effectId){
        int[] buf = new int[13];

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
        int[] oldBuf;

        oldBuf = (int[])bufTable.get(statusIndexDebuf);

        if(oldBuf != null && (oldBuf[1] == Skill.STATUS_DIE || oldBuf[1] == Skill.STATUS_RUNAWAY || oldBuf[1] == Skill.STATUS_CATCHED)){
            calculateBattleBuf();

            return;
        }

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

        bufTable.put(statusIndexDebuf, buf);

        calculateBattleBuf();
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
            }
        }
    }

    public boolean HasBuf(){
        return this.getBufStatus() != Skill.STATUS_NORMAL;
    }

    public void processBattleBuf(Vector battleMovie, int index){
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
                    if(World.getPercentRate(bufInfo[4])){
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
            Skill.processStatusUpdate(this, index, battleMovie);
        }
    }

    private void initAttribute(){
        //生命=INT(6*体力*(INT(SQRT(等级*100))+30)/40)+50   
        //魔法=INT(3*智力*(INT(SQRT(等级*100))+30)/40)+50   
        //物理攻击下限=INT(0.8*力量*INT(SQRT(等级*100))+30)/40)+15  
        //物理攻击上限=INT(力量*INT(SQRT(等级*100))+30)/40)+20  
        //高低限平均加额外点   
        //物理防御=装备防御值  
        //魔法攻击下限=INT(0.6*智力*INT(SQRT(等级*100))+30)/40)+15  
        //魔法攻击上限=INT(0.8*智力*INT(SQRT(等级*100))+30)/40)+20  
        //高低限平均加额外点   
        //魔法防御=装备魔法防御值    
        //命中率=95-敌人敏捷*8/(100+敌人级别)-2*SIGN(敌方敏捷-我方敏捷)*(SQRT(4+ABS(敌方敏捷-我方敏捷))-2) //命中率最多100%    
        //物理伤害吸收=SQRT(6*物理防御*(100/攻击者等级)) //伤害吸收最多75%   
        //魔法伤害吸收=SQRT(6*魔法防御*(100/攻击者等级)) //伤害吸收最多75%   

        attributes[ATTR_HPMAX] = attributes[ATTR_VIT] * 6 * ((int)sqrt(level * 100) + 30) / 40 + 50;
        attributes[ATTR_MPMAX] = attributes[ATTR_INT] * 3 * ((int)sqrt(level * 100) + 30) / 40 + 50;

        if(hp > attributes[ATTR_HPMAX])
            hp = attributes[ATTR_HPMAX];
        if(mp > attributes[ATTR_MPMAX])
            mp = attributes[ATTR_MPMAX];

        if(bsType == TYPE_MONSTER){
            hp = attributes[ATTR_HPMAX];
            mp = attributes[ATTR_MPMAX];
        }

        hpShow = hp;
        mpShow = mp;

        attributes[ATTR_PMIN] = attributes[ATTR_STR] * ((int)sqrt(level * 100) + 30) * 8 / 10 / 40 + (bsType == TYPE_PLAYER? 15: 0);
        attributes[ATTR_PMAX] = attributes[ATTR_STR] * ((int)sqrt(level * 100) + 30) / 40 + (bsType == TYPE_PLAYER? 20: 1);
        attributes[ATTR_PDEF] = 0;

        attributes[ATTR_MMIN] = attributes[ATTR_INT] * ((int)sqrt(level * 100) + 30) * 6 / 10 / 40 + (bsType == TYPE_PLAYER? 15: 0);
        attributes[ATTR_MMAX] = attributes[ATTR_INT] * ((int)sqrt(level * 100) + 30) * 8 / 10 / 40 + (bsType == TYPE_PLAYER? 20: 1);

        attributes[ATTR_MDEF] = 0;
        attributes[ATTR_PHIT] = 0;
        attributes[ATTR_MHIT] = 0;
        attributes[ATTR_FLEE] = 0;
        attributes[ATTR_PCRI] = 5 + (attributes[ATTR_AGI] - level) / 15;
        attributes[ATTR_MCRI] = 5 + attributes[ATTR_INT] / 20;
    }

    public void setTarget(BattleSprite target, int index){
        setTarget(target, index, 0);
    }

    public void setTarget(BattleSprite target, int index, int targetType){
        this.target = target;
        this.targetIndex = index;
        this.targetType = targetType;
    }

    public void setItem(GameItem item){
        this.useItem = item;
    }

    public void setSkill(int skillId){
        this.skillId = skillId;
    }

    public void clearBout(Vector battleMovie, int index){
        this.skillId = Skill.SKILL_NOT_READY;
        this.target = null;
        this.targetIndex = -1;
        this.targetType = 0;
        this.usedMp = 0;

        processCoolDown();
        processAutoRelife(battleMovie);
    }

    public void processAutoRelife(Vector battleMovie){
        if(testDie() && getBufStatus() == Skill.STATUS_AUTO_RELIFE){
            int[] bufInfo = getbufInfo();

            int reLifePercent = bufInfo[3];
            int restoreHpPercent = bufInfo[4];

            if(World.getPercentRate(reLifePercent)){
                reLive();

                int hpAdd = this.attributes[BattleSprite.ATTR_HPMAX] * restoreHpPercent / 100;

                setTarget(this, groupIndex);

                if(testMCri()){
                    hpAdd *= BattleSprite.CRI_RATE;

                    Skill.processSaveLifeMovie(this, groupIndex, hpAdd, Skill.ATTACK_CRI, battleMovie);
                }else{
                    Skill.processSaveLifeMovie(this, groupIndex, hpAdd, Skill.ATTACK_NO_CRI, battleMovie);
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

    public boolean doCatch(){
        boolean result = false;

        int tmpHp = hp;

        if(tmpHp == 0){
            tmpHp = 1;
        }

        int catchRate = attributes[ATTR_HPMAX] / tmpHp + 4;

        if(catchRate > 30){
            catchRate = 30;
        }

        //#debug
        catchRate += 100;

        if(World.getPercentRate(catchRate)){
            setDeBufStatus(1, Skill.STATUS_CATCHED, 0, 0, 0, 0, 0);
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

    public int[] doBattle(int action){
        int[] result = new int[8]; //[0]是否命中， [1]伤害值, [2]暴击, [3] 反弹，吸收 [4] 物理反弹吸收参数 [5] 魔法反弹吸收参数 [6] 保护人类型 [7]保护人索引
        int[] tmp;

        if(testHit(target.getFlee(), action)){
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
                    tmp = getPhysicalDamage(target.getDefence());
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
                    tmp = getMagicDamage(target.getMagicDefence());
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
        return (attributes[ATTR_PDEF] + equipDefence) * (100 + defenceAdd) / 100;
    }

    public int getFlee(){
        return attributes[ATTR_FLEE];
    }

    public int getPHit(){
        return attributes[ATTR_PHIT];
    }

    public int getMHit(){
        return attributes[ATTR_MHIT];
    }

    public int getPCri(){
        return (attributes[ATTR_PCRI] * 10 + criRateAdd) / 10;
    }

    public int getMCri(){
        return (attributes[ATTR_MCRI] * 10 + criRateAdd) / 10;
    }

    public boolean testPCri(){
        return World.getPercentRate(getPCri());
    }

    public boolean testMCri(){
        return World.getPercentRate(getMCri());
    }

    public int getSpeed(){
        Skill skill = Skill.getSkill(skillId);
        int result = 0;

        if(skill == null){
            switch(skillId){
                case Skill.SKILL_ATTACK:
                case Skill.SKILL_CATCH:
                    result = 0x6FFFFFF + attributes[ATTR_AGI];

                    break;
                case Skill.SKILL_RUN:
                    result = Integer.MAX_VALUE;

                    break;
                case Skill.SKILL_ITEM:
                    result = 0x7FFFFFF + attributes[ATTR_AGI];

                    break;
                case Skill.SKILL_STAY:
                    result = Integer.MAX_VALUE;

                    break;
                default:
                    result = Integer.MIN_VALUE;

                    break;
            }
        }else{
            switch(skill.speedMethod){
                case Skill.SPEED_METHOD_ORDER_1:
                    result = Integer.MAX_VALUE;

                    break;
                case Skill.SPEED_METHOD_ORDER_2:
                    result = 0x7FFFFFF + attributes[ATTR_AGI];

                    break;
                case Skill.SPEED_METHOD_ORDER_3:
                    result = 0x6FFFFFF + attributes[ATTR_AGI];

                    break;
                case Skill.SPEED_METHOD_ORDER_4:
                    result = 0x5FFFFFF + attributes[ATTR_AGI];

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

    private int[] getPhysicalDamage(int inDefence){
        //物理伤害吸收=SQRT(6*物理防御*(100/攻击者等级)) //伤害吸收最多75% 

        int attack = World.random(attributes[ATTR_PMIN], attributes[ATTR_PMAX]);
        int cri = Skill.ATTACK_NO_CRI;

        attack = attack * (100 + attackAdd) / 100;

        int damageSorb = (int)sqrt(6 * inDefence * 100 / level);

        if(damageSorb > 75){
            damageSorb = 75;
        }

        int damage = attack * (100 - damageSorb) / 100;

        if(testPCri()){
            damage *= CRI_RATE;
            cri = Skill.ATTACK_CRI;
        }

        return new int[]{
                        damage, cri
        };
    }

    private int[] getMagicDamage(int inMagicDefence){
        //魔法伤害吸收=SQRT(6*魔法防御*(100/攻击者等级)) //伤害吸收最多75% 
        int mattack = World.random(attributes[ATTR_MMIN], attributes[ATTR_MMAX]);
        int cri = Skill.ATTACK_NO_CRI;

        mattack = mattack * (100 + magicAttackAdd) / 100;

        int damageSorb = (int)sqrt(6 * inMagicDefence * 100 / level);

        if(damageSorb > 75){
            damageSorb = 75;
        }

        int damage = mattack * (100 - damageSorb) / 100;

        if(testMCri()){
            damage *= CRI_RATE;
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

        return World.getPercentRate(tmp);
    }

    public int calculateHitRate(int inFlee, int action){
        //命中率=95-敌人敏捷*8/(100+敌人级别)-2*SIGN(敌方敏捷-我方敏捷)*(SQRT(4+ABS(敌方敏捷-我方敏捷))-2) //命中率最多100%    
        int result = 0;

        int sign = (this.target.attributes[ATTR_AGI] - this.attributes[ATTR_AGI]) >= 0? 1: -1;
        int absAgi = (this.target.attributes[ATTR_AGI] - this.attributes[ATTR_AGI]) * sign;

        result = (int)(95 - this.target.attributes[ATTR_AGI] * 8 / (100 + this.target.level) - 2 * sign * ((int)sqrt(4 + absAgi) - 2));

        switch(action){
            case ACTION_PATTACK:
                result += getPHit();

                break;
            case ACTION_MATTACK:
                result += getMHit();

                break;
        }

        result -= this.target.getFlee();

        if(result > 100){
            result = 100;
        }

        result += hitAdd;
        result -= this.target.fleeAdd;

        if(result > 100){
            result = 100;
        }

        return result;
    }

    public boolean testRun(int enemyLevel, int bout){
        int tmp = 2 * enemyLevel - 2 * level + 10;

        if(tmp < 1){
            tmp = 1;
        }

        int percent = 100 / tmp + 10 * (bout - 1) * (bout - 1);

        if(World.getPercentRate(percent)){
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

    public void changeHp(int inc, Vector battleMovie){
        int oldhp = hp;

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
    }

    public void changeMp(int inc){
        mp += inc;

        if(mp > attributes[ATTR_MPMAX]){
            mp = attributes[ATTR_MPMAX];
        }

        if(mp <= 0){
            mp = 0;
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

    public void setIndex(byte in){
        frame = frameSequence[in];
    }

    public void setSequence(byte[] seq){
        frameSequence = seq;
        index = 0;
        frame = frameSequence[index];
    }

    public void setWeaponSequence(byte[] seq){
        weaponFrameSequence = seq;
        weaponIndex = -1;
        weaponFrame = -1;
    }

    public static final byte HP_WIDTH = 20;

    public void drawName(Graphics g){
        int fw = 0;
        int ty = battleY - getHeight(0) - 5;
        fw = GameState.font.stringWidth(name);
        int tx = battleX - (fw - getWidth(0)) / 2;
        if(tx < 0)
            tx = 0;
        g.setFont(GameState.font);

        int clr = Sprite.CLR_NAME;
        if(bsType == TYPE_MONSTER)
            clr = ArmySprite.CLR_NAME;

        if(ty - GameState.CHAR_HEIGHT < 0){
            ty = GameState.CHAR_HEIGHT;
        }

        World.draw3DString(g, name, tx, ty, Graphics.LEFT | Graphics.BOTTOM, clr);
    }

    public void draw(Graphics g){
        int fw = 0;
        if(debufID == 0 && flyString.size() == 0 && showName){
            drawName(g);
        }

        /*--------------draw fly string---------------*/
        for(int i = 0; i < flyString.size(); i++){
            Object[] obj = (Object[])flyString.elementAt(i);
            String str = (String)obj[0];
            short y = ((Short)obj[1]).shortValue();
            if(y == 0 && flyingHeight != 0){
                continue;
            }
            int color = ((Integer)obj[2]).intValue();

            fw = GameState.font.stringWidth(str);
            int x = battleX + (getWidth(0) - fw) / 2;
            if(x < 0)
                x = 0;
            if(x + fw > World.viewWidth)
                x = World.viewWidth - fw;
            g.setFont(GameState.font);
            World.draw3DString(g, str, x, (short)(battleY - World.player.getHeight(0) + 10) + y, Graphics.LEFT | Graphics.BOTTOM, color);
            y--;

            flyingHeight++;
            if(flyingHeight == FLYSTRING_HEIGHT / 2){
                flyingHeight = 0;
            }
            obj[1] = new Short(y);
            flyString.setElementAt(obj, i);
        }

        for(int i = flyString.size() - 1; i >= 0; i--){
            Object[] obj = (Object[])flyString.elementAt(i);
            short y = ((Short)obj[1]).shortValue();
            if(Math.abs(y) > FLYSTRING_HEIGHT){
                flyString.removeElementAt(i);
                flyingHeight = 0;
            }
        }

        if(flyString.size() == 0 && flyingHeight != 0){
            flyingHeight = 0;
        }

        if(showHp){
            /*-------------draw HP-------------*/
            int top = battleY + 3;
            int left = battleX + (getWidth(0) - HP_WIDTH) / 2;
            int hw = hpShow * 1000 / attributes[ATTR_HPMAX] * HP_WIDTH;
            hw /= 1000;

            g.setColor(0xffffff);
            g.fillRect(left, top, HP_WIDTH, 3);

            if(hw != 0)
                for(int i = 0; i < 3; i++){
                    g.setColor(Sprite.CLR_HP[i]);
                    g.drawLine(left, top + i, left + hw, top + i);
                }

            g.setColor(0x000000);
            g.drawLine(left, top + 3, left + HP_WIDTH - 1, top + 3);

            /*-------------draw MP-------------*/
            hw = mpShow * 1000 / attributes[ATTR_MPMAX] * HP_WIDTH;
            hw /= 1000;

            g.setColor(0xffffff);
            g.fillRect(left, top + 4, HP_WIDTH, 3);

            if(hw != 0)
                for(int i = 0; i < 3; i++){
                    g.setColor(Sprite.CLR_MP[i]);
                    g.drawLine(left, top + 4 + i, left + +hw, top + 4 + i);
                }
        }
    }

    public void drawEffect(Graphics g){
        if(effSeq != null){

            int frame = animateFrame - 1;
            if(frame >= effSeq.length)
                frame = 0;

            //            if(effX == -1)
            //                effX = (short)(battleX + (getWidth() - World.effectImageSet.getWidth(effSeq[animateFrame - 1])) / 2);
            //            if(effY == -1)
            //                effY = battleY;
            short[] merge = null;
            if(getImageSet().collision == null || getImageSet().collision[this.frame] == null){
                merge = new short[]{
                                (short)(getWidth() / 2 - 2), (short)(getHeight() / 2 - 2)
                };
            }else{
                merge = getImageSet().collision[this.frame];
            }
            short[] effectFocus = World.effectImageSet.collision[effSeq[frame]];

            int x = 0;
            int y = 0;

            if(merge != null && effectFocus != null){
                x = battleX + merge[0] - effectFocus[0];
                y = battleY - getHeight() + merge[1] - effectFocus[1];
            }

            if(effX == -1){
                effX = (short)x;
            }
            
            if(effY == -1){
                effY = (short)y;
            }

            World.effectImageSet.drawFrame(g, effSeq[frame], x, y, Graphics.LEFT | Graphics.TOP);
        }
        if(debufID != 0){

            int debufx = 0;
            int debufy = 0;

            byte[] effSequ = getSequence()[Skill.ANIMATE_STS_ATK];

            if(effSequ.length >= debufID && debufID > 0){
                int dw = World.effectImageSet.getWidth(effSequ[debufID - 1]);
                int dh = World.effectImageSet.getHeight(effSequ[debufID - 1]);

                debufx = battleX + (getWidth(0) - dw) / 2;
                debufy = battleY - getHeight(0) - 5;

                if(World.tick / 3 % 2 == 0){
                    debufy += 2;
                }

                World.effectImageSet.drawFrame(g, effSequ[debufID - 1], debufx, debufy, Graphics.LEFT | Graphics.BOTTOM);
            }
        }
    }

    public abstract short getWidth();

    public abstract short getHeight();

    public abstract short getWidth(int frame);

    public abstract short getHeight(int frame);

    public abstract short getLocalX(int localIndex);

    public abstract short getLocalY(int localIndex);

    public abstract void setSequenceIndex(int id);

    public abstract void setSequenceIndex(int id, boolean focus);

    public abstract void setLocalIndex(byte localIndex);

    public void setDie(){
        showDie = true;
        setSequenceIndex(FRAMESEQUENCE_DIE, true);
    }

    public void moveTo(int destx, int desty){
        moveTo(destx, desty, 12);
    }

    public void moveTo(int destx, int desty, int step){
        int[] ret = moveTo(battleX, battleY, destx, desty, step);
        battleX = (short)ret[0];
        battleY = (short)ret[1];
    }

    public int[] moveTo(int srcx, int srcy, int destx, int desty, int step){
        int dx, dy;
        int npcX = srcx;
        int npcY = srcy;

        step = 200;

        dx = step;
        dy = dx;

        int diffx = Math.abs(destx - npcX);
        int diffy = Math.abs(desty - npcY);

        if(diffx >= diffy && diffx != 0){
            int v = dx * 10000 / Math.abs(diffx);
            dy = diffy * v / 10000;
        }

        if(diffx < diffy && diffy != 0){
            int v = dy * 10000 / Math.abs(diffy);
            dx = diffx * v / 10000;
        }

        if(Math.abs(diffx) < dx){
            dx = Math.abs(diffx);
        }

        if(Math.abs(diffy) < dy){
            dy = Math.abs(diffy);
        }

        if(destx - npcX < 0){
            dx = -dx;
        }

        if(desty - npcY < 0){
            dy = -dy;
        }

        srcx += dx;
        srcy += dy;
        return new int[]{
                        srcx, srcy
        };
    }

    public boolean moveToTarget(BattleSprite bs){
        int destx, desty;

        if(bs.battleX == battleX && bs.battleY == battleY)
            return true;

        if(bs.battleDirect == Sprite.LEFT/*battleX < bs.battleX*/){
            destx = (short)(bs.battleX - getWidth());
            if(weaponFrameSequence != null){
                destx -= 10;
            }
        }else{
            destx = (short)(bs.battleX + bs.getWidth());
            if(weaponFrameSequence != null){
                destx += 10;
            }
        }
        desty = bs.battleY;
        moveTo(destx, desty);
        if(battleX == destx && battleY == desty)
            return true;
        else
            return false;
    }

    public boolean moveBack(){
        int destx = getLocalX(localIndex);
        int desty = getLocalY(localIndex);
        moveTo(destx, desty);
        if(battleX == destx && battleY == desty)
            return true;
        else
            return false;
    }

    /**
     * [1]: target<Br>
     * [2]: miss<br>
     * [3]: cri<br>
     * [4]: hp inc<br>
     * [5]: status<br>
     */
    public static final byte COMMAND_PHY_ATTACK = 0;
    /**
     * [1]: target<Br>
     * [2]: miss<br>
     * [3]: cri<br>
     * [4]: hp inc<br>
     * [5]: status<br>
     */
    public static final byte COMMAND_MGC_ATTACK = 1;
    /**
     * [1]:target<br>
     */
    public static final byte COMMAND_MOVETOTARGET = 2;
    /**
     * none
     */
    public static final byte COMMAND_MOVEBACK = 3;
    //    public static final byte COMMAND_MISS = 4;
    //    public static final byte COMMAND_CRI = 5;

    /**
     * [1]: 动画效果ID<br>
     * [2]~[n]: 动画所需参数<br>
     */
    public static final byte COMMAND_PLAYANIMATE = 6;

    /**
     * [1]: 显示的字符串<br>
     * [2]: 血量变化颜色<br>
     * [3]: 血量变化<br>
     * [4]: 当前状态<br>
     * [5]: 魔法变化颜色<br>
     * [6]: 魔法变化<br>
     */
    public static final byte COMMAND_BEATED = 7;

    /**
     * [1]: x<br>
     * [2]: y<br>
     */
    public static final byte COMMAND_MOVETO = 8;

    public static final byte COMMAND_HIDE = 9;

    /**
     * [1]: Idel frame<br>
     */
    public static final byte COMMAND_IDLE = 100;

    public Vector commandQueue = new Vector();

    public Vector flyString = new Vector();

    public int flyingHeight = 0;

    /**
     * command格式
     * [0] = command ID (Byte)
     * [1] .. [n] command param (Object)
     * @param command
     */
    public void addCommand(Object[] command){
        commandQueue.addElement(command);
    }

    public void insertCommand(Object[] command, int index){
        commandQueue.insertElementAt(command, index);
    }

    public void addFlyString(String str, int color){
        flyString.addElement(new Object[]{
                        str, new Short((short)0), new Integer(color)
        });
    }

    public byte[][] getSequence(){
        byte[][] seq;
        if(battleDirect == Sprite.LEFT)
            seq = EFFECTSEQUENCE_LEFT;
        else
            seq = EFFECTSEQUENCE_RIGHT;
        return seq;
    }

    public void setEffect(int seqIndex, short dx, short dy){
        byte[][] seq = getSequence();
        effSeq = seq[seqIndex];
        effX = dx;
        effY = dy;
    }

    public int getEffectFrame(int effectIndex, int frameIndex){
        byte[][] seq = getSequence();
        return seq[effectIndex][frameIndex];
    }

    public int getEffectLength(int seqIndex){
        byte[][] seq = getSequence();
        if(seqIndex < 0 || seqIndex > seq.length)
            return -1;
        return seq[seqIndex].length;
    }

    public void clearEffect(){
        effSeq = null;
        effX = (short)-1;
        effY = (short)-1;
    }

    public void clearWeaponImg(){
        weaponIndex = -1;
        weaponFrame = -1;
    }

    public static final Object[] makeCommand(byte command, Object[] param){
        Object[] ret = new Object[param == null? 1: param.length + 1];
        ret[0] = new Byte(command);
        if(param != null)
            System.arraycopy(param, 0, ret, 1, param.length);

        return ret;
    }

    public boolean cycleCommand(){

        if(commandQueue.size() == 0)
            return true;
        Object[] cmdObj = (Object[])commandQueue.elementAt(0);
        byte command = ((Byte)cmdObj[0]).byteValue();

        BattleSprite target = null;

        Object[] aniCmd = null;
        int miss = 0;

        switch(command){
            case COMMAND_IDLE: {
                int idelFrame = ((Integer)cmdObj[1]).intValue();
                idelFrame--;
                if(idelFrame == 0){
                    commandQueue.removeElementAt(0);
                }else{
                    cmdObj[1] = new Integer(idelFrame);
                }
            }

                break;
            case COMMAND_PHY_ATTACK:
                setSequenceIndex(FRAMESEQUENCE_STAND);
                target = (BattleSprite)cmdObj[1];
                aniCmd = new Object[9];

                aniCmd[0] = new Byte(COMMAND_PLAYANIMATE);
                aniCmd[1] = new Byte(Skill.ANIMATE_PHY_ATK);
                aniCmd[2] = target;

                miss = ((Integer)cmdObj[2]).intValue();
                aniCmd[3] = new Integer(miss);
                if(miss == 1){
                    aniCmd[4] = "miss";
                    aniCmd[5] = new Integer(0x0000ff);
                }else{
                    int cri = ((Integer)cmdObj[3]).intValue();
                    int dhp = ((Integer)cmdObj[4]).intValue();
                    int dmp = ((Integer)cmdObj[5]).intValue();
                    aniCmd[4] = new Integer(dhp);
                    aniCmd[5] = new Integer(cri == Skill.ATTACK_NO_CRI? World.CLR_HPDEC: World.CLR_CRI);
                    aniCmd[6] = new Integer(dmp);
                    aniCmd[7] = new Integer(cri == Skill.ATTACK_NO_CRI? World.CLR_MPDEC: World.CLR_CRI);
                }
                aniCmd[8] = cmdObj[6];
                //                commandQueue.removeElementAt(0);
                commandQueue.setElementAt(aniCmd, 0);
                animateFrame = 0;
                break;

            case COMMAND_MGC_ATTACK:
                setSequenceIndex(FRAMESEQUENCE_STAND);
                target = (BattleSprite)cmdObj[1];
                aniCmd = new Object[9];

                aniCmd[0] = new Byte(COMMAND_PLAYANIMATE);
                aniCmd[1] = new Byte(Skill.ANIMATE_MGC_ATK);
                aniCmd[2] = target;

                miss = ((Integer)cmdObj[2]).intValue();
                aniCmd[3] = new Integer(miss);
                if(miss == 1){
                    aniCmd[4] = "miss";
                    aniCmd[5] = new Integer(0x0000ff);
                }else{
                    int cri = ((Integer)cmdObj[3]).intValue();
                    int dhp = ((Integer)cmdObj[4]).intValue();
                    int dmp = ((Integer)cmdObj[5]).intValue();
                    aniCmd[4] = new Integer(dhp);
                    aniCmd[5] = new Integer(cri == Skill.ATTACK_NO_CRI? World.CLR_HPDEC: World.CLR_CRI);
                    aniCmd[6] = new Integer(dmp);
                    aniCmd[7] = new Integer(cri == Skill.ATTACK_NO_CRI? World.CLR_MPDEC: World.CLR_CRI);
                }
                aniCmd[8] = cmdObj[6];
                commandQueue.setElementAt(aniCmd, 0);
                animateFrame = 0;
                break;

            case COMMAND_MOVETOTARGET:
                setSequenceIndex(FRAMESEQUENCE_RUN);
                if(moveToTarget((BattleSprite)cmdObj[1])){
                    commandQueue.removeElementAt(0);
                }
                break;

            case COMMAND_MOVETO:
                setSequenceIndex(FRAMESEQUENCE_RUN);
                int tx = ((Integer)cmdObj[1]).intValue();
                int ty = ((Integer)cmdObj[2]).intValue();
                moveTo(tx, ty);
                if(battleX == tx && battleY == ty){
                    setSequenceIndex(FRAMESEQUENCE_STAND);
                    commandQueue.removeElementAt(0);
                }
                break;

            case COMMAND_MOVEBACK:
                setSequenceIndex(FRAMESEQUENCE_RUNBACK);
                if(moveBack()){
                    setSequenceIndex(FRAMESEQUENCE_STAND);
                    commandQueue.removeElementAt(0);
                }
                break;

            case COMMAND_BEATED:
                setSequenceIndex(FRAMESEQUENCE_BEATED);
                aniCmd = new Object[8];
                aniCmd[0] = new Byte(COMMAND_PLAYANIMATE);
                aniCmd[1] = new Byte(Skill.ANIMATE_HURT);
                aniCmd[2] = cmdObj[1]; // 显示字符串
                aniCmd[3] = cmdObj[2]; // 颜色
                aniCmd[4] = cmdObj[3]; // 血量变化
                aniCmd[5] = cmdObj[4]; // 状态
                if(cmdObj.length > 5){
                    aniCmd[6] = cmdObj[5]; // 魔法变化颜色
                    aniCmd[7] = cmdObj[6]; // 魔法变化
                }else{
                    aniCmd[6] = new Integer(0);
                    aniCmd[7] = new Integer(0);
                }
                commandQueue.setElementAt(aniCmd, 0);
                animateFrame = 0;
                break;
            case COMMAND_HIDE:
                target = (BattleSprite)cmdObj[1];
                target.show = false;
                commandQueue.removeElementAt(0);

                break;

            case COMMAND_PLAYANIMATE: {
                int ani = ((Byte)cmdObj[1]).byteValue();
                Object[] param = new Object[cmdObj.length - 2];
                if(param.length > 0){
                    System.arraycopy(cmdObj, 2, param, 0, param.length);
                }
                playAnimate(ani, param);
            }

                break;
        }
        return false;
    }

    private int animateFrame = 0;
    private boolean animateStop = false;
    public byte debufID = 0;

    private boolean playAnimate(int animate, Object[] param){
        BattleSprite target = null;
        int miss = 0;
        int status = 0;
        int dhp = 0;
        int dmp = 0;
        int w = 0;
        int h = 0;

        switch(animate){
            case Skill.ANIMATE_PHY_ATK:
                target = (BattleSprite)param[0];

                if(World.effectImageSet == null){
                    w = 0;
                    h = 0;
                }else{
                    w = World.effectImageSet.getWidth(getEffectFrame(Skill.ANIMATE_PHY_ATK, 0));
                    h = World.effectImageSet.getHeight(getEffectFrame(Skill.ANIMATE_PHY_ATK, 0));
                }
                if(animateFrame == 0){
                    setSequenceIndex(FRAMESEQUENCE_ATTACK);
                    //                    int srcx = target.battleX + (target.getWidth() - w) / 2;
                    //                    int srcy = target.battleY - target.getHeight() + h + (getHeight() - h) / 2;
                    //                    setEffect(Skill.ANIMATE_PHY_ATK, (short)srcx, (short)srcy);

                    if(weaponFrameSequence != null){
                        weaponIndex = 0;//开始播放武器切片动画
                        weaponFrame = weaponFrameSequence[weaponIndex];
                    }
                    //mpShow = mp;
                }else if((weaponFrameSequence != null) && weaponIndex == weaponFrameSequence.length - weaponFrameSequence.length / 3){
                    miss = ((Integer)param[1]).intValue();

                    if(miss == Skill.HIT_MISS){
                        target.addFlyString((String)param[2], ((Integer)param[3]).intValue());
                    }else{
                        dhp = ((Integer)param[2]).intValue();
                        dmp = ((Integer)param[4]).intValue();
                        if(dhp != 0){
                            target.addCommand(makeCommand(COMMAND_BEATED, new Object[]{
                                            String.valueOf(Math.abs(dhp)), param[3], new Integer(dhp), param[6], param[5], new Integer(dmp)
                            }));
                        }
                    }
                    //mpShow = mp;
                }else if(((bsType == TYPE_MONSTER && animateFrame > 5) || ((bsType == TYPE_PLAYER_PET || bsType == TYPE_MONSTER_PET) && animateFrame > 5))
                                || (weaponFrameSequence != null && weaponIndex == weaponFrameSequence.length - 1)){

                    if((bsType == TYPE_MONSTER && animateFrame > 5) || ((bsType == TYPE_PLAYER_PET || bsType == TYPE_MONSTER_PET) && animateFrame > 5)){
                        miss = ((Integer)param[1]).intValue();

                        if(miss == Skill.HIT_MISS){
                            target.addFlyString((String)param[2], ((Integer)param[3]).intValue());
                        }else{
                            dhp = ((Integer)param[2]).intValue();
                            dmp = ((Integer)param[4]).intValue();
                            if(dhp != 0){
                                target.addCommand(makeCommand(COMMAND_BEATED, new Object[]{
                                                String.valueOf(Math.abs(dhp)), param[3], new Integer(dhp), param[6], param[5], new Integer(dmp)
                                }));
                            }
                        }
                        //mpShow = mp;
                    }
                    clearEffect();
                    clearWeaponImg();
                    stopAnimate();
                    setSequenceIndex(FRAMESEQUENCE_STAND);

                }
                break;
            case Skill.ANIMATE_MGC_ATK:
                target = (BattleSprite)param[0];
                if(World.effectImageSet == null){
                    w = 0;
                    h = 0;
                }else{
                    w = World.effectImageSet.getWidth(getEffectFrame(Skill.ANIMATE_MGC_ATK, 0));
                    h = World.effectImageSet.getHeight(getEffectFrame(Skill.ANIMATE_MGC_ATK, 0));
                }
                if(animateFrame == 0){
                    setSequenceIndex(FRAMESEQUENCE_ATTACK);
                    int srcx = battleX + (battleDirect == Sprite.LEFT? -w: getWidth());
                    int srcy = battleY - getHeight() + h + (getHeight() - h) / 2;
                    setEffect(Skill.ANIMATE_MGC_ATK, (short)srcx, (short)srcy);
                    mpShow = mp;
                }else{
                    int destx = target.battleX + (target.battleDirect == Sprite.LEFT? -w / 2: target.getWidth() / 2);
                    int desty = target.battleY - target.getHeight() + h + (target.getHeight() - h) / 2;
                    int[] p = moveTo(effX, effY, destx, desty, 12);
                    effX = (short)p[0];
                    effY = (short)p[1];

                    if(effX == destx && effY == desty){
                        target = (BattleSprite)param[0];
                        miss = ((Integer)param[1]).intValue();

                        if(miss == Skill.HIT_MISS)
                            target.addFlyString((String)param[2], ((Integer)param[3]).intValue());
                        else{
                            dhp = ((Integer)param[2]).intValue();
                            if(dhp != 0){
                                target.addCommand(makeCommand(COMMAND_BEATED, new Object[]{
                                                String.valueOf(Math.abs(dhp)), param[3], new Integer(dhp), param[6], param[5], new Integer(dmp)
                                }));
                            }
                        }
                        clearEffect();
                        stopAnimate();
                    }
                }
                break;

            case Skill.ANIMATE_HURT: {
                //被打
                int step = 6 - animateFrame * 2;
                if(step <= 0)
                    step = 1;

                battleX += battleDirect == Sprite.RIGHT? -step: step;

                if(animateFrame == 0){
                    dhp = ((Integer)param[2]).intValue();
                    if(dhp != 0){
                        hpShow += dhp;
                        if(hpShow < 0)
                            hpShow = 0;
                        addFlyString((String)param[0], ((Integer)param[1]).intValue());
                    }
                    dmp = ((Integer)param[5]).intValue();
                    if(dmp != 0){
                        mpShow += dmp;
                        if(mpShow < 0)
                            mpShow = 0;
                        addFlyString(String.valueOf(dmp), ((Integer)param[4]).intValue());
                    }
                }

                if(animateFrame == 4){
                    status = ((Integer)param[3]).intValue();
                    if(status == Skill.STATUS_DIE){
                        debufID = 0;
                        setDie();
                        showHp = false;
                    }else if(status != Skill.STATUS_NORMAL){
                        debufID = (byte)status;
                    }

                    int dx = battleX + (battleDirect == Sprite.RIGHT? 13: -13);

                    insertCommand(makeCommand(COMMAND_MOVETO, new Object[]{
                                    new Integer(dx), new Integer(battleY)
                    }), 1);
                    stopAnimate();
                }
            }

                break;
            case Skill.ANIMATE_INC_MGC:
                if(animateFrame == 0){
                    setEffect(Skill.ANIMATE_INC_MGC, battleX, (short)(battleY + 5));
                    //mpShow = mp;
                }else if(animateFrame == getEffectLength(Skill.ANIMATE_INC_MGC)){
                    int hpinc = ((Integer)param[0]).intValue();
                    int color = ((Integer)param[1]).intValue();
                    if(hpinc != 0){
                        addFlyString(String.valueOf(hpinc), color);
                        hpShow += hpinc;
                        if(hpShow > attributes[ATTR_HPMAX])
                            hpShow = attributes[ATTR_HPMAX];
                    }

                    int mpinc = ((Integer)param[2]).intValue();
                    color = ((Integer)param[3]).intValue();

                    if(mpinc != 0){
                        addFlyString(String.valueOf(mpinc), color);
                        mpShow += mpinc;
                        if(mpShow > attributes[ATTR_MPMAX])
                            mpShow = attributes[ATTR_MPMAX];
                    }
                    //hpShow = hp;
                    //mpShow = mp;
                    clearEffect();
                    stopAnimate();
                }
                break;
            case Skill.ANIMATE_NOTIFY_SAV_MGC:
                target = (BattleSprite)param[0];
                target.addCommand(makeCommand(BattleSprite.COMMAND_PLAYANIMATE, new Object[]{
                    new Byte(Skill.ANIMATE_SAV_MGC)
                }));
                stopAnimate();
                break;
            case Skill.ANIMATE_SAV_MGC:
                if(animateFrame == 0){
                    setEffect(animate, (short)-1, (short)-1);
                }else if(animateFrame == getEffectLength(animate)){
                    hpShow = hp;
                    mpShow = mp;
                    clearEffect();
                    stopAnimate();
                    showDie = false;
                    showHp = true;
                    setSequenceIndex(FRAMESEQUENCE_STAND);
                    frame = 0;
                }
                break;
            case Skill.ANIMATE_STS_ATK:
                target = (BattleSprite)param[0];
                target.debufID = ((Integer)param[1]).byteValue();
                mpShow = mp;
                stopAnimate();
                break;
            case Skill.ANIMATE_NOTIFY_UNS_MGC:
                target = (BattleSprite)param[0];
                target.addCommand(makeCommand(BattleSprite.COMMAND_PLAYANIMATE, new Object[]{
                                new Byte(Skill.ANIMATE_UNS_MGC), param[1], param[2]
                }));
                stopAnimate();
                break;
            case Skill.ANIMATE_UNS_MGC:
                if(animateFrame == 0){
                    setEffect(animate, (short)-1, (short)-1);
                }else if(animateFrame == getEffectLength(animate)){
                    debufID = ((Integer)param[0]).byteValue();
                    clearEffect();
                    stopAnimate();
                    int hit = ((Integer)param[1]).intValue();
                    if(hit == Skill.HIT_MISS){
                        addFlyString("miss", 0x0000ff);
                    }
                }
                break;

            case Skill.ANIMATE_RUNAWAY:
                if(animateFrame < 10){
                    if(animateFrame % 2 == 0)
                        World.skillString += ".";
                }else if(animateFrame == 10){
                    int hit = ((Integer)param[0]).intValue();
                    if(hit == Skill.HIT_HIT){
                        World.skillString = "逃跑成功";
                    }else{
                        World.skillString = "逃跑失败";
                    }
                }else if(animateFrame == 15){
                    stopAnimate();
                }
                break;
            default:
                if(animateFrame == 0){
                    setEffect(animate, (short)-1, (short)-1);
                }else if(animateFrame == getEffectLength(animate)){
                    int hpInc = ((Integer)param[0]).intValue();
                    int mpInc = ((Integer)param[1]).intValue();
                    hpShow += hpInc;
                    mpShow += mpInc;
                    clearEffect();
                    stopAnimate();
                }
                break;
        }

        if(animateStop){
            animateStop = false;
        }else{
            animateFrame++;
        }
        return false;
    }

    private void stopAnimate(){
        animateStop = true;
        animateFrame = 0;
        commandQueue.removeElementAt(0);
    }

}