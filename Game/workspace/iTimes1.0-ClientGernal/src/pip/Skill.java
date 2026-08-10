package pip;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class Skill{
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

    public byte speedMethod; //优先级算法

    public static final byte SPEED_METHOD_ORDER_1 = 0;
    public static final byte SPEED_METHOD_ORDER_2 = 1;
    public static final byte SPEED_METHOD_ORDER_3 = 2;
    public static final byte SPEED_METHOD_ORDER_4 = 3;
    public static final byte SPEED_METHOD_FIRST = 4;
    public static final byte SPEED_METHOD_LAST = 5;

    public static final byte SHOW_MPUSE_NONE = 0;
    public static final byte SHOW_MPUSE_LIST = 1;
    public static final byte SHOW_MPUSE_DESC = 2;

    public static final String[] skillTypeName = {
                    "狂战技能", "魔战技能", "盾防技能", "辅战技能", "宠物技能"
    };

    public static Hashtable allSkill = new Hashtable();

    /**
     * 到目标处使用技能
     */
    public static final byte POSITION_DEST = 0;

    /**
     * 在原地使用技能
     */
    public static final byte POSITION_STAY = 1;

    /**
     * 使用技能完毕回到初始位置
     */
    public static final byte OVER_POSITION_BACK = 0;

    /**
     * 使用技能完毕留在使用位置
     */
    public static final byte OVER_POSITION_STAY = 1;

    /**
     * 正常战斗动画速度
     */
    public static final byte MOVIE_SPEED_NORMAL = 0;

    /**
     * 快速战斗动画速度
     */
    public static final byte MOVIE_SPEED_FAST = 1;

    /**
     * 技能命中
     */
    public static final byte HIT_HIT = 0;

    /**
     * 技能未命中
     */
    public static final byte HIT_MISS = 1;

    /**
     * 正常状态
     */
    public static final byte STATUS_NORMAL = 0;

    /**
     * 中毒状态
     */
    public static final byte STATUS_POISON = 1;

    /**
     * 石化状态
     */
    public static final byte STATUS_STONE = 2;

    /**
     * 霜冻状态
     */
    public static final byte STATUS_FROST = 3;

    /**
     * 混乱状态
     */
    public static final byte STATUS_CONFUSE = 4;

    /**
     * 昏睡状态
     */
    public static final byte STATUS_SLEEP = 5;

    /**
     * 眩晕状态
     */
    public static final byte STATUS_FAINT = 6;

    /**
     * 停止行动状态
     */
    public static final byte STATUS_STOP = 7;

    /**
     * 魔法反弹状态
     */
    public static final byte STATUS_ANTI_MAGIC = 8;

    /**
     * 物理反弹状态
     */
    public static final byte STATUS_ANTI_ATTACK = 9;

    /**
     * 魔法攻击吸收状态
     */
    public static final byte STATUS_SORB_MAGIC = 10;

    /**
     * 物理攻击吸收状态
     */
    public static final byte STATUS_SORB_ATTACK = 11;

    /**
     * 死亡状态
     */
    public static final byte STATUS_DIE = 12;

    /**
     * 逃跑成功状态
     */
    public static final byte STATUS_RUNAWAY = 13;

    /**
     * 被队友保护状态
     */
    public static final byte STATUS_PROTECTED = 14;

    /**
     * 被抓宠成功状态
     */
    public static final byte STATUS_CATCHED = 15;

    /**
     * 免疫状态
     */
    public static final byte STATUS_IMMUNITY_STATUS = 16;

    /**
     * 死后复生
     */
    public static final byte STATUS_AUTO_RELIFE = 17;

    /**
     * 伤害转魔
     */
    public static final byte STATUS_DAMAGE_TO_MP = 18;

    /**
     * 输出吸血
     */
    public static final byte STATUS_ATTACK_DAMAGE_TO_HP = 19;

    //固有技能
    /**
     * 普通物理攻击
     */
    public static final byte SKILL_ATTACK = -1;

    /**
     * 使用物品
     */
    public static final byte SKILL_ITEM = -2;

    /**
     * 逃跑
     */
    public static final byte SKILL_RUN = -3;

    /**
     * 发呆
     */
    public static final byte SKILL_STAY = -4;

    /**
     * 抓宠
     */
    public static final byte SKILL_CATCH = -5;

    /**
     * 中间动作，无需显示名字
     */
    public static final byte SKILL_NONE = -6;

    /**
     * 减血减魔
     */
    public static final byte SKILL_LIFE_MAGIC = -7;

    /**
     * 更新状态
     */
    public static final byte SKILL_UPDATE_STATUS = -8;

    /**
     * 怪物使用固有技能区间
     */
    public static final byte SOLID_SKILL_BEGIN = -1;

    /**
     * 未选择技能，在战斗中此状态参战者将自动分配技能
     */
    public static final byte SKILL_NOT_READY = 0;
    
    /**
     * 进入自动攻击状态
     */
    public static final byte SKILL_AUTO = -2;

    //特殊技能
    //狂战技能
    /**
     * 多次攻击：同一回合内对一个或多个对手进行多次物理攻击
     */
    public static final byte EFFECT_MULTI_ATK = 1;

    /**
     * 提攻加害：当前回合提升攻击力，受到伤害增加
     */
    public static final byte EFFECT_INC_ATK_INC_DMG = 2;

    /**
     * 反防加伤：如果目标身上有有益魔法，则给与更多的伤害
     */
    public static final byte EFFECT_ANTI_BUF_INC_ATK = 3;

    /**
     * 击晕：在普通攻击的基础上使对手有一定几率停止一定回合行动
     */
    public static final byte EFFECT_FAINT = 4;

    /**
     * 加攻加暴：伤害增加并提高暴击率
     */
    public static final byte EFFECT_INC_ATK_INC_CRI = 5;

    /**
     * 加伤降命：给予对象更多的伤害，命中降低
     */
    public static final byte EFFECT_INC_DMG_DEC_HIT = 6;

    /**
     * 加攻停行：增强攻击力，下一回合停止行动
     */
    public static final byte EFFECT_INC_ATK_STOP = 7;

    /**
     * 狂暴意志：整场战斗的物理攻击力和自己受到的伤害都会提高。
     */
    public static final byte EFFECT_ALWAYS_INC_ATK_INC_DMG = 8;

    //魔战技能
    /**
     * 魔法攻击：给与对象魔法伤害
     */
    public static final byte EFFECT_MGC_ATK = 9;

    /**
     * 群体攻击：对多个对象同时进行攻击
     */
    public static final byte EFFECT_MULTI_MGC = 10;

    /**
     * 使中毒：使对象中毒，每回合减少一定比例的生命力。回合数根据技能级别
     */
    public static final byte EFFECT_LET_POISON = 11;

    /**
     * 使昏睡：可能使对象多个回合陷入睡眠状态，不能行动。被攻击就会清醒。回合数根据技能级别
     */
    public static final byte EFFECT_LET_SLEEP = 12;

    /**
     * 使石化：可能使对象变成石化状态，而无法行动。石化状态中，所有损伤都降低。回合数根据技能级别
     */
    public static final byte EFFECT_LET_STONE = 13;

    /**
     * 霜冻魔法：使用魔法攻击全体敌人造成伤害
     */
    public static final byte EFFECT_MAGIC_ALL = 14;

    /**
     * 使混乱：使对象产生混乱，使用普通攻击方式随机攻击对手中一员。
     */
    public static final byte EFFECT_LET_CONFUSE = 15;

    /**
     * 专注施法：令自己中毒作为代价使自己本场战斗的魔法攻击力提高。
     */
    public static final byte EFFECT_INC_MGC_LET_POSION = 16;

    /**
     * 魔法飘零：消耗掉自己所有MP，50%几率造成敌人单体HP伤害
     */
    public static final byte EFFECT_MGC_USE_ALL_MP = 17;

    //盾防技能
    /**
     * 降攻加闪：降低攻击力，提高躲闪率
     */
    public static final byte EFFECT_DEC_ATK_INC_FLEE = 18;

    /**
     * 挡攻减伤：替某队友承受攻击，同时减少损伤
     */
    public static final byte EFFECT_BLOCK_ATK_DEC_DMG = 19;

    /**
     * 减物伤：减少受到的物理伤害
     */
    public static final byte EFFECT_DEC_PHY_DMG = 20;

    /**
     * 减魔伤：减少受到的魔法伤害
     */
    public static final byte EFFECT_DEC_MGC_DMG = 21;

    /**
     * 减物魔伤：同时减少受到的物理魔法伤害
     */
    public static final byte EFFECT_DEC_PHY_MGC_DMG = 22;

    /**
     * 物伤转命：将对手对自己的物理伤害转化为生命
     */
    public static final byte EFFECT_PHY_DMG_TO_HP = 23;

    /**
     * 魔伤转命：将对手对自己的魔法伤害转化为生命
     */
    public static final byte EFFECT_MGC_DMG_TO_HP = 24;

    //辅战技能
    /**
     * 回复单体：给自己或队友恢复一定的生命值
     */
    public static final byte EFFECT_RESTORE_HP = 25;

    /**
     * 回复全体：给全队恢复一定比例的生命值
     */
    public static final byte EFFECT_RESTORE_ALL_HP = 26;

    /**
     * 复活   将已经死去的队友复活并恢复一定的生命值
     */
    public static final byte EFFECT_SAVE_LIFE = 27;

    /**
     * 荆棘之墙：使己方目标1回合内反弹物理伤害，攻击者受到40%反弹伤害。
     */
    public static final byte EFFECT_ANTI_PHY = 28;

    /**
     * 魔力镜：使己方目标1回合内反弹魔法伤害，攻击者受到40%反弹伤害。
     */
    public static final byte EFFECT_ANTI_MGC = 29;

    /**
     * 强效治疗：恢复某一队友的生命值。恢复量与消耗量都随着玩家等级提升而增加。
     */
    public static final byte EFFECT_RESTORE_LOT_HP = 30;

    /**
     * 驱邪术：解除己方玩家的石化、昏睡、混乱、中毒、幻觉状态。
     */
    public static final byte EFFECT_CLEAR_STS_AND_ANTI = 31;

    /**
     * 宠物，物：双次物攻
     */
    public static final byte EFFECT_PET_2_ATTACK = 32;

    /**
     * 宠物，物：加伤加爆
     */
    public static final byte EFFECT_PET_ADD_DMG_ADD_CRI_PHY = 33;

    /**
     * 宠物，物：加伤加攻
     */
    public static final byte EFFECT_PET_ADD_DMG_ADD_ATTACK = 34;

    /**
     * 宠物，物：反击魔法
     */
    public static final byte EFFECT_PET_ANTI_MGC = 35;

    /**
     * 宠物，物：加伤降命
     */
    public static final byte EFFECT_PET_ADD_DMG_DEC_HIT = 36;

    /**
     * 宠物，魔：上毒技能
     */
    public static final byte EFFECT_PET_LET_POISON = 37;

    /**
     * 宠物，魔：魔法攻击
     */
    public static final byte EFFECT_PET_MAGIC = 38;

    /**
     * 宠物，魔：双次魔攻
     */
    public static final byte EFFECT_PET_2_MAGIC = 39;

    /**
     * 宠物，魔：冰冻敌人
     */
    public static final byte EFFECT_PET_LET_FROST = 40;

    /**
     * 宠物，魔：反击物理
     */
    public static final byte EFFECT_PET_ANTI_PHY = 41;

    /**
     * 宠物，防：减低伤害
     */
    public static final byte EFFECT_PET_DEC_DMG_ALL_BATTLE = 42;

    /**
     * 宠物，防：伤害转魔
     */
    public static final byte EFFECT_PET_DMG_TO_MGC = 43;

    /**
     * 宠物，防：免疫状态
     */
    public static final byte EFFECT_PET_IMM_STATUS = 44;

    /**
     * 宠物，防：替主抗伤
     */
    public static final byte EFFECT_PET_PROTECT_OWNER = 45;

    /**
     * 宠物，防：减低伤害
     */
    public static final byte EFFECT_PET_DEC_DMG = 46;

    /**
     * 宠物，治：输出回血
     */
    public static final byte EFFECT_PET_ATT_DMG_TO_HP = 47;

    /**
     * 宠物，治：死后复生
     */
    public static final byte EFFECT_PET_AUTO_RELIFE = 48;

    /**
     * 宠物，治：给主加血
     */
    public static final byte EFFECT_PET_ADD_OWNER_HP = 49;

    /**
     * 宠物，治：给主回魔
     */
    public static final byte EFFECT_PET_ADD_OWNER_MP = 50;

    /**
     * 宠物，治：解除状态
     */
    public static final byte EFFECT_PET_UN_ALL_STATUS = 51;

    //多次攻击
    /**
     * 多次攻击随机对手
     */
    public static final byte MULTI_ATTACK_RANDOM = 0;

    /**
     * 多次攻击一个对手
     */
    public static final byte MULTI_ATTACK_ONE = 1;

    //是否暴击
    /**
     * 暴击
     */
    public static final byte ATTACK_CRI = 0;

    /**
     * 没有暴击
     */
    public static final byte ATTACK_NO_CRI = 1;

    //反弹吸收
    /**
     * 无反弹吸收
     */
    public static final byte ATTACK_NORMAL = 0;

    /**
     * 反弹
     */
    public static final byte ATTACK_ANTI = 1;

    /**
     * 吸收
     */
    public static final byte ATTACK_SORB = 2;

    /**
     * 被保护
     */
    public static final byte ATTACK_PROCTECT = 3;

    /**
     * 宠物伤害转魔
     */
    public static final byte ATTACK_PET_DAMAGE_TO_MP = 4;

    //对手选择
    /**
     * 选择敌人
     */
    public static final byte CHOOSE_ENEMY = 0;

    /**
     * 选择活着的队友
     */
    public static final byte CHOOSE_FRIEND = 1;

    /**
     * 选择所有队友，包括已死
     */
    public static final byte CHOOSE_FRIEND_ALL = 2;

    /**
     * 选择主人，宠物特殊技能
     */
    public static final byte CHOOSE_OWNER = 3;

    /**
     * 无需选择
     */
    public static final byte CHOOSE_NONE = 9;

    //技能选择
    /**
     * 技能可以被选择
     */
    public static final byte CAN_SELECT_SKILL = 0;

    /**
     * 技能不可以被选择
     */
    public static final byte CANNOT_SELECT_SKILL = 1;

    //动画效果
    /**
     * 物理攻击<br>
     * [0]: 目标对象<br>
     * [1]: 是否命中<br>
     * [2]: 未命中为字符串"miss"，命中为血量变化<br>
     * [3]: 字符串显示颜色<br>
     * [4]: 状态改变<br>
     */
    public static final byte ANIMATE_PHY_ATK = 0;

    /**
     * 魔法攻击 <br>
     * [0]: 目标对象<br>
     * [1]: 是否命中<br>
     * [2]: 未命中为字符串"miss"，命中为血量变化<br>
     * [3]: 字符串显示颜色<br>
     * [4]: 当前状态<br>
     */
    public static final byte ANIMATE_MGC_ATK = 1;

    /**
     * 状态攻击<br>
     * [0]: target<br>
     * [1]: status<br>
     */
    public static final byte ANIMATE_STS_ATK = 2;

    /**
     * 补血增益<br>
     * [0]: 血量变化<br>
     * [1]: 显示颜色<br>
     * [2]: 魔法变化<br>
     * [3]: 显示颜色<br>
     */
    public static final byte ANIMATE_INC_MGC = 3;

    /**
     * 复活
     */
    public static final byte ANIMATE_SAV_MGC = 4;

    /**
     * 复活
     */
    public static final byte ANIMATE_NOTIFY_SAV_MGC = 104;

    /**
     * 解除
     */
    public static final byte ANIMATE_UNS_MGC = 5;

    /**
     * 解除
     */
    public static final byte ANIMATE_NOTIFY_UNS_MGC = 105;

    /**
     * 狂战系技能起手动画
     * param:none
     */
    public static final byte ANIMATE_PHY_START = 6;

    /**
     * 魔战系技能起手动画
     * param:none
     */
    public static final byte ANIMATE_MGC_START = 7;

    /**
     * 盾防系技能起手动画
     */
    public static final byte ANIMATE_DEF_START = 8;

    /**
     * 辅助系技能起手动画
     */
    public static final byte ANIMATE_ASS_START = 9;

    /**
     * 物品使用起手动画
     */
    public static final byte ANIMATE_ITEM_START = 10;

    /**
     * 无特殊动画
     */
    public static final byte ANIMATE_NONE = 11;

    /**
     * 被打动画<br>
     * [0]: 显示字符串<br>
     * [1]: 颜色<br>
     * [2]: 血量变化<br>
     * [3]: 当前状态<br>
     * [4]: 魔法颜色<br>
     * [5]: 魔法变化<br>
     */
    public static final byte ANIMATE_HURT = 12;

    /**
     * 逃跑动画<br>
     * [0]: 是否成功
     */
    public static final byte ANIMATE_RUNAWAY = 13;

    //技能分类
    /**
     * 狂战技能
     */
    public static final byte TYPE_PHY = 0;

    /**
     * 魔战技能
     */
    public static final byte TYPE_MGC = 1;

    /**
     * 盾防技能
     */
    public static final byte TYPE_DEF = 2;

    /**
     * 辅助技能
     */
    public static final byte TYPE_ASS = 3;

    /**
     * 宠物技能
     */
    public static final byte TYPE_PET = 4;

    //各项名称
    /**
     * 固有技能名称
     */
    public static final String solidSkillName[] = {
                    "攻击", "物品", "逃跑", "发呆", "抓宠", "技能中间步骤", "减血减魔", "更新状态"
    };

    /**
     * 动画类型名称
     */
    public static final String animateName[] = {
                    "物理攻击", "魔法攻击", "状态攻击", "补血增益", "复活", "解除", "狂战起手", "魔战起手", "盾防起手", "辅助起手", "物品使用", "无", "攻击反弹", "逃跑"
    };

    /**
     * 状态名称
     */
    public static final String statusName[] = {
                    "正常", "中毒", "石化", "霜冻", "混乱", "昏睡", "晕菜", "停止行动", "魔法反弹", "物理反弹", "魔法吸收", "物理吸收", "死亡", "已逃跑", "被保护", "已被抓", "状态免疫", "自动复活", "魔法转换", "吸血攻击"
    };

    public static BattleSprite[] players;
    public static BattleSprite[] playerPets;
    public static BattleSprite[] monsters;
    public static BattleSprite[] monsterPets;

    public Skill(byte type, String name, byte effect, byte status, byte postion, byte coolDown, byte coolDownBout){
        this.type = type;
        this.name = name;
        this.effect = effect;
        this.status = status;
        this.position = postion;
        this.coolDown = coolDown;
        this.coolDownBout = coolDownBout;
    }

    public int initParm(DataInputStream in) throws IOException{
        id = in.readShort();
        level = in.readByte();
        parm1 = in.readInt();
        parm2 = in.readInt();
        effectBout = in.readByte();
        mpUse = in.readShort();
        speedMethod = in.readByte();
        hitRate = in.readByte();

        //如果影响回合，如果未设置大于0的数，则用9999代替模拟持续整场战斗
        if(effectBout <= 0){
            effectBout = 9999;
        }

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

    public static BattleSprite getSprite(int spriteType, int spriteIndex){
        BattleSprite result = null;

        switch(spriteType){
            case BattleSprite.TYPE_PLAYER:
                result = players[spriteIndex];

                break;
            case BattleSprite.TYPE_PLAYER_PET:
                result = playerPets[spriteIndex];

                break;
            case BattleSprite.TYPE_NET_PLAYER:
            case BattleSprite.TYPE_MONSTER:
                result = monsters[spriteIndex];

                break;
            case BattleSprite.TYPE_MONSTER_PET:
                result = monsterPets[spriteIndex];

                break;

        }

        return result;
    }

    public static boolean doSkill(BattleSprite opp, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie, int bout){
        int groupIndex = opp.groupIndex;
        int[] movie;
        BattleSprite targetSprite;
        boolean solidSkill = true;
        boolean win = false;

        if(opp.target != null && opp.target.testCannotBattle()){
            Skill skill = Skill.getSkill(opp.skillId);
            
            if(opp.target.testCannotBattle() && skill != null && skill.effect != Skill.EFFECT_SAVE_LIFE){
                targetSprite = selectTargetRandom(opp, them, themPet);
    
                if(targetSprite != null){
                    opp.setTarget(targetSprite, targetSprite.groupIndex);
                }else{
                    win = true;
                }
            }
        }

        if(selectTargetRandom(opp, them, themPet) == null){
            win = true;
        }
        
        if(win){
            return win;
        }

        try{
            switch(opp.skillId){
                case SKILL_ATTACK:
                    if(opp.target.testCannotBattle()){
                        targetSprite = selectTargetRandom(opp, them, themPet);

                        if(targetSprite != null){
                            opp.setTarget(targetSprite, targetSprite.groupIndex);
                        }else{
                            win = true;

                            break;
                        }
                    }

                    processAttack(opp, groupIndex, null, them, themPet, battleMovie, MOVIE_SPEED_NORMAL, OVER_POSITION_BACK);

                    break;
                case SKILL_ITEM:
                    opp.target.useItem.use(opp.target);
                    int[] eff = opp.target.useItem.getEffect();

                    movie = makeMovieSub(opp.bsType, groupIndex, opp.target.bsType, opp.targetIndex, Skill.SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT,
                                    opp.target.getDebufStatus(), 0, 0, 0, eff[0], eff[1]);
                    battleMovie.addElement(movie);
                    opp.target.useItem = null;

                    break;
                case SKILL_RUN:
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
                    int tmp = opp.testRun(tmpLevel, bout)? HIT_HIT: HIT_MISS;

                    if(tmp == HIT_HIT){
                        opp.setDeBufStatus(1, Skill.STATUS_RUNAWAY, 0, 0, 0, opp.bsType, opp.groupIndex);
                    }

                    movie = makeMovieSub(opp.bsType, groupIndex, opp.bsType, groupIndex, opp.skillId, ANIMATE_RUNAWAY, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_NORMAL, tmp,
                                    opp.getDebufStatus(), Skill.ATTACK_NO_CRI, 0, 0, 0, 0);
                    battleMovie.addElement(movie);

                    break;
                case SKILL_STAY:
                    movie = makeMovieSub(opp.bsType, groupIndex, opp.bsType, groupIndex, opp.skillId, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_NORMAL, HIT_HIT, opp
                                    .getDebufStatus(), Skill.ATTACK_NO_CRI, 0, 0, 0, 0);
                    battleMovie.addElement(movie);

                    break;
                case SKILL_CATCH:
                    int catchHit = opp.target.doCatch()? HIT_HIT: HIT_MISS;

                    movie = makeMovieSub(opp.bsType, groupIndex, opp.target.bsType, opp.targetIndex, opp.skillId, ANIMATE_STS_ATK, POSITION_DEST, OVER_POSITION_BACK, MOVIE_SPEED_NORMAL, catchHit,
                                    opp.target.getDebufStatus(), Skill.ATTACK_NO_CRI, 0, 0, 0, 0);
                    battleMovie.addElement(movie);

                    break;
                default:
                    solidSkill = false;

                    break;
            }

            if(solidSkill || win){
                return win;
            }

            Skill skill = Skill.getSkill(opp.skillId);

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

            opp.usedMp = skill.getMpUse(opp);
            opp.changeMp(-skill.getMpUse(opp));

            movie = makeMovieSub(opp.bsType, groupIndex, opp.bsType, groupIndex, opp.skillId, animateType, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, opp.getDebufStatus(),
                            Skill.ATTACK_NO_CRI, 0, -skill.getMpUse(opp), 0, 0);
            battleMovie.addElement(movie);

            switch(skill.effect){
                case Skill.EFFECT_MULTI_ATK:
                    win = processMultiAttack(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_INC_ATK_INC_DMG:
                    win = processIncreaseAttackIncreaseDamage(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_ANTI_BUF_INC_ATK:
                    win = processAntiBufIncreaseAttack(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_FAINT:
                    win = processFaintAttack(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_INC_ATK_INC_CRI:
                    win = processIncreaseAttackIncreaseCri(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_INC_DMG_DEC_HIT:
                    win = processIncreaseDamageDecreaseHit(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_INC_ATK_STOP:
                    win = processIncreaseAttackStop(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_ALWAYS_INC_ATK_INC_DMG:
                    win = processAlwaysIncreaseAttackIncreaseDamage(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_MGC_ATK:
                    win = processMagicAttack(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_MULTI_MGC:
                    win = processMultiMagic(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_LET_POISON:
                    win = processLetPoison(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_LET_SLEEP:
                    win = processLetSleep(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_LET_STONE:
                    win = processLetStone(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_MAGIC_ALL:
                    win = processMagicAll(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_LET_CONFUSE:
                    win = processLetConfuse(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_INC_MGC_LET_POSION:
                    win = processIncreaseMagicLetPosion(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_MGC_USE_ALL_MP:
                    win = processMagicUseAllMp(opp, groupIndex, skill, our, ourPet, battleMovie);

                    break;
                case EFFECT_DEC_ATK_INC_FLEE:
                    win = processDecreaseAttackIncreaseFlee(opp, groupIndex, skill, our, ourPet, battleMovie);

                    break;
                case EFFECT_BLOCK_ATK_DEC_DMG:
                    win = processBlockAttackDecreaseDamage(opp, groupIndex, skill, our, ourPet, battleMovie);

                    break;
                case EFFECT_DEC_PHY_DMG:
                    win = processDecreasePhyDamage(opp, groupIndex, skill, our, ourPet, battleMovie);

                    break;
                case EFFECT_DEC_MGC_DMG:
                    win = processDecreaseMagicDamage(opp, groupIndex, skill, our, ourPet, battleMovie);

                    break;
                case EFFECT_DEC_PHY_MGC_DMG:
                    win = processDecreasePhyMagicDamage(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PHY_DMG_TO_HP:
                    win = processSorbPhyDamage(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_MGC_DMG_TO_HP:
                    win = processSorbMagicDamage(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_RESTORE_HP:
                    win = processRestoreHp(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_RESTORE_ALL_HP:
                    win = processRestoreAllHp(opp, groupIndex, skill, our, ourPet, battleMovie);

                    break;
                case EFFECT_SAVE_LIFE:
                    win = processSaveLife(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_ANTI_PHY:
                    win = processAntiPhy(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_ANTI_MGC:
                    win = processAntiMagic(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_RESTORE_LOT_HP:
                    win = processRestoreLotHp(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_CLEAR_STS_AND_ANTI:
                    win = processClearStatusAndAnti(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_2_ATTACK:
                    win = processPet2Attack(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_ADD_DMG_ADD_CRI_PHY:
                    win = processPetAddDmgAddCriPhy(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_ADD_DMG_ADD_ATTACK:
                    win = processPetAddDmgAddAttack(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_ANTI_MGC:
                    win = processPetAntiMagic(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_ADD_DMG_DEC_HIT:
                    win = processPetAddDmgDecHit(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_LET_POISON:
                    win = processPetLetPoison(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_MAGIC:
                    win = processPetMagic(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_2_MAGIC:
                    win = processPet2Magic(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_LET_FROST:
                    win = processPetLetFrost(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_ANTI_PHY:
                    win = processPetAntiPhy(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_DEC_DMG_ALL_BATTLE:
                    win = processPetDecDmgAllBattle(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_DMG_TO_MGC:
                    win = processPetDmgToMgc(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_IMM_STATUS:
                    win = processPetImmStatus(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_PROTECT_OWNER:
                    win = processPetProtectOwner(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_DEC_DMG:
                    win = processPetDecDmg(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_ATT_DMG_TO_HP:
                    win = processPetAttackDamageToHp(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_AUTO_RELIFE:
                    win = processPetAutoRelife(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_ADD_OWNER_HP:
                    win = processPetAddOwnerHp(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_ADD_OWNER_MP:
                    win = processPetAddOwnerMp(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
                case EFFECT_PET_UN_ALL_STATUS:
                    win = processPetUnAllStatus(opp, groupIndex, skill, them, themPet, battleMovie);

                    break;
            }

            opp.coolDownSkill(skill);
        }finally{
            World.spriteDoneSkill(opp, groupIndex, false);
        }

        return win;
    }

    /**
     * 宠物，物：双次物攻
     */
    private static boolean processPet2Attack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        BattleSprite targetSprite;
        boolean win = false;

        in.setSkill(SKILL_NONE);

        for(int i = 0; i < 2; i++){
            targetSprite = selectTargetRandom(in, them, themPet);

            if(targetSprite != null){
                if(in.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie);

                    break;
                }

                if(i == 2 - 1){
                    processAttack(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
                }else{
                    processAttack(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY);
                }
            }else{
                processEmptyLoop(in, index, battleMovie);

                win = true;

                break;
            }
        }
        
        in.setDeBufStatus(skill.parm1, Skill.STATUS_STOP, 0, 0, 0, in.bsType, in.groupIndex);
        processStatusUpdate(in, index, battleMovie);

        return win;
    }

    /**
     * 宠物，物：加伤加爆
     */
    private static boolean processPetAddDmgAddCriPhy(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, skill.parm2, skill.parm1, skill.parm1, 0, 0, skill.effect);
            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
        }

        return win;
    }

    /**
     * 宠物，物：加伤加攻
     */
    private static boolean processPetAddDmgAddAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm2, 0, 0, 0, 0, 0, 0, skill.parm1, skill.parm1, 0, 0, skill.effect);
            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
        }

        return win;
    }

    /**
     * 宠物，物：反击魔法
     */
    private static boolean processPetAntiMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.setTarget(in, in.groupIndex);
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_MAGIC, skill.level, (in.level / 10 + (in.level < 10? 1: 0)) * 10 * 100 / skill.parm1, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    /**
     * 宠物，物：加伤降命
     */
    private static boolean processPetAddDmgDecHit(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, -skill.parm2, 0, 0, 0, 0, 0, 0, skill.effect);
            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
        }

        return win;
    }

    /**
     * 宠物，魔：上毒技能
     */
    private static boolean processPetLetPoison(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
            in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_POISON, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
            processStatusAttack(in, index, HIT_HIT, battleMovie);
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie);
        }

        return win;
    }

    /**
     * 宠物，魔：降防加爆
     */
    private static boolean processPetMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        processMagic(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
        
        return false;
    }

    /**
     * 宠物，魔：双次魔攻
     */
    private static boolean processPet2Magic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        BattleSprite targetSprite;
        boolean win = false;

        in.setSkill(SKILL_NONE);
        in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, -skill.parm1, 0, 0, 0, 0, 0, 0, skill.effect);

        for(int i = 0; i < 2; i++){
            targetSprite = selectTargetRandom(in, them, themPet);

            if(targetSprite != null){
                if(in.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie);

                    break;
                }

                if(i == 2 - 1){
                    processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
                }else{
                    processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY);
                }
            }else{
                processEmptyLoop(in, index, battleMovie);

                win = true;

                break;
            }
        }

        return win;
    }

    /**
     * 宠物，魔：冰冻敌人
     */
    private static boolean processPetLetFrost(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
            in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_FROST, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
            processStatusAttack(in, index, HIT_HIT, battleMovie);
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie);
        }

        return win;
    }

    /**
     * 宠物，魔：反击物理
     */
    private static boolean processPetAntiPhy(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.setTarget(in, in.groupIndex);
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_ATTACK, skill.level, (in.level / 10 + (in.level < 10? 1: 0)) * 10 * 100 / skill.parm1, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    /**
     * 宠物，防：减低伤害
     */
    private static boolean processPetDecDmgAllBattle(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.setTarget(in, in.groupIndex);
        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, -skill.parm1, 0, 0, skill.effect);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    /**
     * 宠物，防：伤害转魔
     */
    private static boolean processPetDmgToMgc(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.setTarget(in, in.groupIndex);
        in.setBufStatus(skill.effectBout, Skill.STATUS_DAMAGE_TO_MP, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    /**
     * 宠物，防：免疫状态
     */
    private static boolean processPetImmStatus(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.setTarget(in, in.groupIndex);
        in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, skill.parm1, skill.parm1, 0, 0, skill.effect);
        in.setBufStatus(skill.effectBout, Skill.STATUS_IMMUNITY_STATUS, skill.level, 0, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    /**
     * 宠物，防：替主抗伤
     */
    private static boolean processPetProtectOwner(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_PROTECTED, skill.level, skill.parm1, 1, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    /**
     * 宠物，防：减低伤害
     */
    private static boolean processPetDecDmg(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.setTarget(in, in.groupIndex);
        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, -skill.parm1, 0, 0, skill.effect);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    /**
     * 宠物，治：输出回血
     */
    private static boolean processPetAttackDamageToHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.setBufStatus(skill.effectBout, Skill.STATUS_ATTACK_DAMAGE_TO_HP, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
        processMagic(in, index, skill, them, themPet, battleMovie, POSITION_DEST, OVER_POSITION_BACK);

        return false;
    }

    /**
     * 宠物，治：死后复生
     */
    private static boolean processPetAutoRelife(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.setTarget(in, in.groupIndex);
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_AUTO_RELIFE, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);
        
        return false;
    }

    /**
     * 宠物，治：给主加血
     */
    private static boolean processPetAddOwnerHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        int hpAdd = in.level * skill.parm1 / 100;
        int cri = ATTACK_NO_CRI;
        
        if(in.testMCri()){
            hpAdd *= BattleSprite.CRI_RATE;
            cri = ATTACK_CRI;
        }else{
            cri = ATTACK_NO_CRI;
        }

        in.target.changeHp(hpAdd, battleMovie);
        processRestore(in, index, hpAdd, 0, cri, battleMovie);
        
        in.setTarget(in, in.groupIndex);
        in.changeHp(hpAdd, battleMovie);
        processRestore(in, index, hpAdd, 0, cri, battleMovie);

        return false;
    }

    /**
     * 宠物，治：给主回魔
     */
    private static boolean processPetAddOwnerMp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        int mpAdd = in.level * skill.parm1 / 100;

        if(in.testMCri()){
            mpAdd *= BattleSprite.CRI_RATE;
            processRestore(in, index, 0, mpAdd, ATTACK_CRI, battleMovie);
        }else{
            processRestore(in, index, 0, mpAdd, ATTACK_NO_CRI, battleMovie);
        }

        in.target.changeMp(mpAdd);

        return false;
    }

    /**
     * 宠物，治：解除状态
     */
    private static boolean processPetUnAllStatus(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, skill.parm1, skill.parm1, 0, 0, skill.effect);
        
        in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
        processUnStatus(in, index, HIT_HIT, battleMovie);
        
        return false;
    }

    private static boolean processAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, int movieSpeed, int overPosition){
        int[] battleResult = in.doBattle(BattleSprite.ACTION_PATTACK);

        if(battleResult[3] == Skill.ATTACK_SORB && battleResult[0] == Skill.HIT_HIT && World.getPercentRate(battleResult[4])){
            int hpSorb = battleResult[1] * battleResult[5] / 100;

            if(hpSorb <= 0){
                hpSorb = 1;
            }

            in.target.changeHp(hpSorb, battleMovie);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skillId, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
                            battleResult[2], 0, 0, 0, 0);
            battleMovie.addElement(movie);

            movie = makeMovieSub(in.target.bsType, in.targetIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.target
                            .getDebufStatus(), battleResult[2], 0, 0, hpSorb, 0);
            battleMovie.addElement(movie);

            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie);
            }

            World.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_ANTI && battleResult[0] == Skill.HIT_HIT){
            int antiDamage = battleResult[1] * battleResult[4] / 100;

            if(antiDamage <= 0){
                antiDamage = -1;
            }else{
                antiDamage = -antiDamage;
            }

            int overPos = overPosition;

            in.changeHp(antiDamage, battleMovie);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skillId, ANIMATE_PHY_ATK, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                            .getDebufStatus(), battleResult[2], 0, 0, 0, 0);
            battleMovie.addElement(movie);

            if(in.testDie()){
                overPos = OVER_POSITION_BACK;
            }

            movie = makeMovieSub(in.bsType, index, in.bsType, index, SKILL_NONE, ANIMATE_HURT, POSITION_STAY, overPos, movieSpeed, HIT_HIT, in.getDebufStatus(), battleResult[2], antiDamage, 0,
                            0, 0);
            battleMovie.addElement(movie);

            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie);
            }

            World.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_PROCTECT && battleResult[0] == Skill.HIT_HIT){
            int protectDamage = battleResult[1] * (100 - battleResult[4]) / 100;
            int protectSrcType = battleResult[6];
            int protectSrcIndex = battleResult[7];

            BattleSprite src = getSprite(protectSrcType, protectSrcIndex);

            if(src.testCannotBattle()){ //保护人已死亡，清除保护状态，按正常攻击处理
                in.target.setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);

                in.target.changeHp(-battleResult[1], battleMovie);

                int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skillId, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target
                                .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
                battleMovie.addElement(movie);

                if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                    in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                    processStatusUpdate(in.target, in.targetIndex, battleMovie);
                }

                if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP){
                    int[] bufInfo = in.getbufInfo();

                    int hpInc = (bufInfo[3] * battleResult[1]) / 100;
                    
                    if(hpInc > 0){
                        in.changeHp(hpInc, battleMovie);
    
                        movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(),
                                        0, 0, 0, hpInc, 0);
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
                
                src.changeHp(-protectDamage, battleMovie);
                
                if(restDamage > 0 && battleResult[5] != 0){
                    in.target.changeHp(-restDamage, battleMovie);
                }

                int[] movie = makeMovieSub(src.bsType, src.groupIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                                .getDebufStatus(), 0, 0, 0, 0, 0);
                battleMovie.addElement(movie);
                
                if(restDamage > 0 && battleResult[5] != 0){
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skillId, ANIMATE_PHY_ATK, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);
                    
                    movie = makeMovieSub(in.bsType, index, in.target.bsType, in.target.groupIndex, in.skillId, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target.getDebufStatus(),
                                    battleResult[2], 0, 0, -restDamage, 0);
                    battleMovie.addElement(movie);
                }else{
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skillId, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);
                }

                movie = makeMovieSub(src.bsType, src.groupIndex, src.bsType, src.groupIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_BACK, movieSpeed, HIT_HIT, src.getDebufStatus(), 0,
                                0, 0, 0, 0);
                battleMovie.addElement(movie);
            }

            World.spriteDoneSkill(in.target, in.targetIndex, true);
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
            in.target.changeHp(-hpDamage, battleMovie);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skillId, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
                            battleResult[2], 0, 0, -hpDamage, -mpDamage);
            battleMovie.addElement(movie);

            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie);
            }

            World.spriteDoneSkill(in.target, in.targetIndex, true);
        }else{
            in.target.changeHp(-battleResult[1], battleMovie);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skillId, ANIMATE_PHY_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target
                            .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
            battleMovie.addElement(movie);

            if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie);
            }

            if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP){
                int[] bufInfo = in.getbufInfo();

                int hpInc = (bufInfo[3] * battleResult[1]) / 100;

                if(hpInc > 0){
                    in.changeHp(hpInc, battleMovie);
    
                    movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
                                    0, 0, hpInc, 0);
                    battleMovie.addElement(movie);
                }
            }
        }

        return battleResult[0] == Skill.HIT_HIT;
    }

    private static void processMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie, int movieSpeed, int overPosition){
        int[] battleResult = in.doBattle(BattleSprite.ACTION_MATTACK);

        if(battleResult[3] == Skill.ATTACK_SORB && battleResult[0] == Skill.HIT_HIT && World.getPercentRate(battleResult[4])){
            int hpSorb = battleResult[1] * battleResult[5] / 100;

            if(hpSorb <= 0){
                hpSorb = 1;
            }

            in.target.changeHp(hpSorb, battleMovie);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skillId, ANIMATE_MGC_ATK, POSITION_STAY, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
                            battleResult[2], 0, 0, 0, 0);
            battleMovie.addElement(movie);

            movie = makeMovieSub(in.target.bsType, in.targetIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                            .getDebufStatus(), battleResult[2], 0, 0, hpSorb, 0);
            battleMovie.addElement(movie);

            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie);
            }

            World.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_ANTI && battleResult[0] == Skill.HIT_HIT){
            int antiDamage = battleResult[1] * battleResult[4] / 100;

            if(antiDamage <= 0){
                antiDamage = -1;
            }else{
                antiDamage = -antiDamage;
            }

            int overPos = overPosition;

            in.changeHp(antiDamage, battleMovie);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skillId, ANIMATE_MGC_ATK, POSITION_STAY, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                            .getDebufStatus(), battleResult[2], 0, 0, 0, 0);
            battleMovie.addElement(movie);

            if(in.testDie()){
                overPos = OVER_POSITION_BACK;
            }

            movie = makeMovieSub(in.target.bsType, in.targetIndex, in.bsType, index, in.skillId, ANIMATE_MGC_ATK, POSITION_STAY, overPos, movieSpeed, HIT_HIT, in.getDebufStatus(), battleResult[2], 0,
                            0, antiDamage, 0);
            battleMovie.addElement(movie);

            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie);
            }

            World.spriteDoneSkill(in.target, in.targetIndex, true);
        }else if(battleResult[3] == Skill.ATTACK_PROCTECT && battleResult[0] == Skill.HIT_HIT){
            int protectDamage = battleResult[1] * (100 - battleResult[4]) / 100;
            int protectSrcType = battleResult[6];
            int protectSrcIndex = battleResult[7];

            BattleSprite src = getSprite(protectSrcType, protectSrcIndex);

            if(src.testCannotBattle()){ //保护人已死亡，清除保护状态，按正常攻击处理
                in.target.setBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, 0, 0);

                in.target.changeHp(-battleResult[1], battleMovie);

                int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skillId, ANIMATE_MGC_ATK, POSITION_STAY, overPosition, movieSpeed, battleResult[0], in.target
                                .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
                battleMovie.addElement(movie);

                if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                    in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                    processStatusUpdate(in.target, in.targetIndex, battleMovie);
                }

                if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP){
                    int[] bufInfo = in.getbufInfo();

                    int hpInc = (bufInfo[3] * battleResult[1]) / 100;

                    if(hpInc > 0){
                        in.changeHp(hpInc, battleMovie);
    
                        movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(),
                                        0, 0, 0, hpInc, 0);
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
                
                src.changeHp(-protectDamage, battleMovie);
                
                if(restDamage > 0 && battleResult[5] != 0){
                    in.target.changeHp(-restDamage, battleMovie);
                }

                int[] movie = makeMovieSub(src.bsType, src.groupIndex, in.target.bsType, in.targetIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, HIT_HIT, in.target
                                .getDebufStatus(), 0, 0, 0, 0, 0);
                battleMovie.addElement(movie);
                
                if(restDamage > 0 && battleResult[5] != 0){
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skillId, ANIMATE_MGC_ATK, POSITION_DEST, OVER_POSITION_STAY, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);
                    
                    movie = makeMovieSub(in.bsType, index, in.target.bsType, in.target.groupIndex, in.skillId, ANIMATE_MGC_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], in.target.getDebufStatus(),
                                    battleResult[2], 0, 0, -restDamage, 0);
                    battleMovie.addElement(movie);
                }else{
                    movie = makeMovieSub(in.bsType, index, src.bsType, src.groupIndex, in.skillId, ANIMATE_MGC_ATK, POSITION_DEST, overPosition, movieSpeed, battleResult[0], src.getDebufStatus(),
                                    battleResult[2], 0, 0, -protectDamage, 0);
                    battleMovie.addElement(movie);
                }

                movie = makeMovieSub(src.bsType, src.groupIndex, src.bsType, src.groupIndex, SKILL_NONE, ANIMATE_NONE, POSITION_DEST, OVER_POSITION_BACK, movieSpeed, HIT_HIT, src.getDebufStatus(), 0,
                                0, 0, 0, 0);
                battleMovie.addElement(movie);
            }

            World.spriteDoneSkill(in.target, in.targetIndex, true);
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
            in.target.changeHp(-hpDamage, battleMovie);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skillId, ANIMATE_MGC_ATK, POSITION_DEST, overPosition, movieSpeed, HIT_HIT, in.target.getDebufStatus(),
                            battleResult[2], 0, 0, -hpDamage, -mpDamage);
            battleMovie.addElement(movie);

            if(in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie);
            }

            World.spriteDoneSkill(in.target, in.targetIndex, true);
        }else{
            in.target.changeHp(-battleResult[1], battleMovie);

            int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, in.skillId, ANIMATE_MGC_ATK, POSITION_STAY, overPosition, movieSpeed, battleResult[0], in.target
                            .getDebufStatus(), battleResult[2], 0, 0, -battleResult[1], 0);
            battleMovie.addElement(movie);

            if(battleResult[0] == Skill.HIT_HIT && in.target.getDebufStatus() == Skill.STATUS_SLEEP){
                in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
                processStatusUpdate(in.target, in.targetIndex, battleMovie);
            }

            if(in.getBufStatus() == Skill.STATUS_ATTACK_DAMAGE_TO_HP && battleResult[0] == Skill.HIT_HIT){
                int[] bufInfo = in.getbufInfo();

                int hpInc = (bufInfo[3] * battleResult[1]) / 100;

                if(hpInc > 0){
                    in.changeHp(hpInc, battleMovie);
    
                    movie = makeMovieSub(in.bsType, in.groupIndex, in.bsType, in.groupIndex, SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, movieSpeed, HIT_HIT, in.getDebufStatus(), 0,
                                    0, 0, hpInc, 0);
                    battleMovie.addElement(movie);
                }
            }
        }
    }

    public static void processEmptyLoop(BattleSprite in, int index, Vector battleMovie){
        int[] movie = makeMovieSub(in.bsType, index, in.bsType, index, Skill.SKILL_NONE, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.getDebufStatus(),
                        ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
    }

    public static void processStatusUpdate(BattleSprite in, int index, Vector battleMovie){
        int[] movie = makeMovieSub(in.bsType, index, in.bsType, index, Skill.SKILL_UPDATE_STATUS, ANIMATE_NONE, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.getDebufStatus(),
                        ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
    }

    private static void processStatusAttack(BattleSprite in, int index, int hit, Vector battleMovie){
        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, Skill.SKILL_NONE, ANIMATE_STS_ATK, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, hit, in.target
                        .getDebufStatus(), ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
    }

    private static void processUnStatus(BattleSprite in, int index, int hit, Vector battleMovie){
        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, Skill.SKILL_NONE, ANIMATE_UNS_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, hit, in.target
                        .getDebufStatus(), ATTACK_NO_CRI, 0, 0, 0, 0);
        battleMovie.addElement(movie);
    }

    private static void processRestore(BattleSprite in, int index, int hpInc, int mpInc, int cri, Vector battleMovie){
        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, Skill.SKILL_NONE, ANIMATE_INC_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.target
                        .getDebufStatus(), cri, 0, 0, hpInc, mpInc);
        battleMovie.addElement(movie);
    }

    public static void processSaveLifeMovie(BattleSprite in, int index, int hpInc, int cri, Vector battleMovie){
        int[] movie = makeMovieSub(in.bsType, index, in.target.bsType, in.targetIndex, Skill.SKILL_NONE, ANIMATE_SAV_MGC, POSITION_STAY, OVER_POSITION_BACK, MOVIE_SPEED_FAST, HIT_HIT, in.target
                        .getDebufStatus(), cri, 0, 0, hpInc, 0);
        battleMovie.addElement(movie);
    }

    private static boolean processMultiAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        int times = skill.parm1;
        int type = skill.parm2;
        BattleSprite targetSprite;
        boolean win = false;

        in.setSkill(SKILL_NONE);

        for(int i = 0; i < times; i++){
            targetSprite = selectTargetRandom(in, them, themPet);

            if(targetSprite != null){
                if(type == Skill.MULTI_ATTACK_RANDOM){
                    in.setTarget(targetSprite, targetSprite.groupIndex);
                }else if(in.target.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie);

                    return win;
                }

                if(in.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie);

                    return win;
                }

                if(i == times - 1){
                    processAttack(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
                }else{
                    processAttack(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY);
                }
            }else{
                processEmptyLoop(in, index, battleMovie);

                win = true;

                break;
            }
        }

        return win;
    }

    private static boolean processIncreaseAttackIncreaseDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, skill.parm2, skill.parm2, 0, 0, skill.effect);
            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
        }

        return win;
    }

    private static boolean processAntiBufIncreaseAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            if(in.target.HasBuf()){
                in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
            }

            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
        }

        return win;
    }

    private static boolean processFaintAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            boolean hit = processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);

            if(hit){
                if(World.getPercentRate(skill.hitRate)){
                    in.target.setDeBufStatus(skill.effectBout, STATUS_FAINT, skill.level, 0, 0, in.bsType, in.groupIndex);
                    processStatusUpdate(in.target, in.targetIndex, battleMovie);
                }
            }
        }

        return win;
    }

    private static boolean processIncreaseAttackIncreaseCri(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, skill.parm2, 0, 0, 0, 0, skill.effect);
            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
        }

        return win;
    }

    private static boolean processIncreaseDamageDecreaseHit(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, -skill.parm2, 0, 0, 0, 0, 0, 0, skill.effect);
            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
        }

        return win;
    }

    private static boolean processIncreaseAttackStop(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
            processAttack(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
            in.setDeBufStatus(skill.effectBout, Skill.STATUS_STOP, 0, 0, 0, in.bsType, in.groupIndex);
            processStatusUpdate(in, index, battleMovie);
        }

        return win;
    }

    private static boolean processAlwaysIncreaseAttackIncreaseDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, skill.parm1, 0, 0, 0, 0, 0, 0, skill.parm2, skill.parm2, 0, 0, skill.effect);
        }

        return win;
    }

    private static boolean processMagicAttack(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            in.AddAttrBuf(skill.effectBout, 0, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
            processMagic(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
        }

        return win;
    }

    private static boolean processMultiMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        int times = skill.parm1;
        int type = skill.parm2;
        BattleSprite targetSprite;
        boolean win = false;

        in.setSkill(SKILL_NONE);

        for(int i = 0; i < times; i++){
            targetSprite = selectTargetRandom(in, them, themPet);

            if(targetSprite != null){
                if(type == Skill.MULTI_ATTACK_RANDOM){
                    in.setTarget(targetSprite, targetSprite.groupIndex);
                }else if(in.target.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie);

                    return win;
                }

                if(in.testCannotBattle()){
                    processEmptyLoop(in, index, battleMovie);

                    return win;
                }

                if(i == times - 1){
                    processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
                }else{
                    processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_STAY);
                }
            }else{
                win = true;

                break;
            }
        }

        return win;
    }

    private static boolean processLetPoison(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
            in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_POISON, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
            processStatusAttack(in, index, HIT_HIT, battleMovie);
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie);
        }

        return win;
    }

    private static boolean processLetSleep(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(World.getPercentRate(skill.hitRate)){
            if(in.testHit(in.target.getFlee(), BattleSprite.ACTION_MATTACK) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
                in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_SLEEP, skill.level, 0, 0, in.bsType, in.groupIndex);
                processStatusAttack(in, index, HIT_HIT, battleMovie);
            }else{
                processStatusAttack(in, index, HIT_MISS, battleMovie);
            }
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie);
        }

        return win;
    }

    private static boolean processLetStone(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        int tmpHitRate = skill.hitRate + (in.level - in.target.level) / 2;

        if(World.getPercentRate(tmpHitRate) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
            in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_STONE, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);
            processStatusAttack(in, index, HIT_HIT, battleMovie);
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie);
        }

        return win;
    }

    private static boolean processMagicAll(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        in.AddAttrBuf(skill.effectBout, 0, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);

        for(int i = 0; i < them.length; i++){
            if(them[i] != null && !them[i].testCannotBattle()){
                in.setTarget(them[i], them[i].groupIndex);
                processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
            }
        }

        win = testTargetRandom(in, them, themPet);

        if(win){
            return win;
        }

        for(int i = 0; i < themPet.length; i++){
            if(themPet[i] != null && !themPet[i].testCannotBattle()){
                in.setTarget(themPet[i], themPet[i].groupIndex);
                processMagic(in, index, null, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
            }
        }

        return win;
    }

    private static boolean processLetConfuse(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(World.getPercentRate(skill.hitRate) && in.target.getBufStatus() != Skill.STATUS_IMMUNITY_STATUS){
            in.target.setDeBufStatus(skill.effectBout, Skill.STATUS_CONFUSE, skill.level, 0, 0, in.bsType, in.groupIndex);
            processStatusAttack(in, index, HIT_HIT, battleMovie);
        }else{
            processStatusAttack(in, index, HIT_MISS, battleMovie);
        }

        return win;
    }

    private static boolean processIncreaseMagicLetPosion(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        in.AddAttrBuf(skill.effectBout, 0, skill.parm1, 0, 0, 0, 0, 0, 0, 0, 0, 0, skill.effect);
        in.setDeBufStatus(skill.effectBout, Skill.STATUS_POISON, skill.level, skill.parm2, 0, in.bsType, in.groupIndex);
        in.setTarget(in, in.groupIndex);
        processStatusAttack(in, index, HIT_HIT, battleMovie);

        return win;
    }

    private static boolean processMagicUseAllMp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        boolean win = false;

        win = testTargetRandom(in, them, themPet);

        if(!win){
            if(!World.getPercentRate(skill.hitRate)){
                in.AddAttrBuf(skill.effectBout, 0, -100, 0, 0, -100, 0, 0, 0, 0, 0, skill.parm1 * in.usedMp / 100, skill.effect);
            }else{
                in.AddAttrBuf(skill.effectBout, 0, -100, 0, 0, 0, 0, 0, 0, 0, 0, skill.parm1 * in.usedMp / 100, skill.effect);
            }
            
            processMagic(in, index, skill, them, themPet, battleMovie, MOVIE_SPEED_FAST, OVER_POSITION_BACK);
        }

        return win;
    }

    private static boolean processDecreaseAttackIncreaseFlee(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.setTarget(in, in.groupIndex);
        in.target.AddAttrBuf(skill.effectBout, skill.parm1, skill.parm1, 0, 0, 0, skill.parm2, 0, 0, 0, 0, 0, skill.effect);

        return false;
    }

    private static boolean processBlockAttackDecreaseDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_PROTECTED, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    private static boolean processDecreasePhyDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, 0, 0, 0, skill.effect);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    private static boolean processDecreaseMagicDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, 0, 0, skill.effect);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    private static boolean processDecreasePhyMagicDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.target.AddAttrBuf(skill.effectBout, 0, 0, 0, 0, 0, 0, 0, -skill.parm1, -skill.parm1, 0, 0, skill.effect);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    private static boolean processSorbPhyDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.setTarget(in, in.groupIndex);
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_SORB_ATTACK, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);

        return false;
    }

    private static boolean processSorbMagicDamage(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.setTarget(in, in.groupIndex);
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_SORB_MAGIC, skill.level, skill.parm1, skill.parm2, in.bsType, in.groupIndex);

        return false;
    }

    private static boolean processRestoreHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        int hpAdd = skill.parm1;

        if(in.testMCri()){
            hpAdd *= BattleSprite.CRI_RATE;
            processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie);
        }else{
            processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie);
        }

        in.target.changeHp(hpAdd, battleMovie);

        return false;
    }

    private static boolean processRestoreAllHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        for(int i = 0; i < them.length; i++){
            if(them[i] != null && !them[i].testCannotBattle()){
                int hpAdd = skill.parm1;

                in.setTarget(them[i], i);

                if(in.testMCri()){
                    hpAdd *= BattleSprite.CRI_RATE;
                    processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie);
                }else{
                    processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie);
                }

                them[i].changeHp(hpAdd, battleMovie);
            }
        }
        
        for(int i = 0; i < themPet.length; i++){
            if(themPet[i] != null && !themPet[i].testCannotBattle()){
                int hpAdd = skill.parm1;

                in.setTarget(themPet[i], i);

                if(in.testMCri()){
                    hpAdd *= BattleSprite.CRI_RATE;
                    processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie);
                }else{
                    processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie);
                }

                themPet[i].changeHp(hpAdd, battleMovie);
            }
        }

        return false;
    }

    private static boolean processSaveLife(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        if(!in.target.testCannotBattle()){
            processUnStatus(in, index, HIT_MISS, battleMovie);
        }else{
            in.target.reLive();
    
            int hpAdd = skill.parm1;
    
            if(in.testMCri()){
                hpAdd *= BattleSprite.CRI_RATE;
                processSaveLifeMovie(in, index, hpAdd, ATTACK_CRI, battleMovie);
            }else{
                processSaveLifeMovie(in, index, hpAdd, ATTACK_NO_CRI, battleMovie);
            }
    
            in.target.changeHp(hpAdd, battleMovie);
        }

        return false;
    }

    private static boolean processAntiPhy(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_ATTACK, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    private static boolean processAntiMagic(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        in.target.setBufStatus(skill.effectBout, Skill.STATUS_ANTI_MAGIC, skill.level, skill.parm1, 0, in.bsType, in.groupIndex);
        processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);

        return false;
    }

    private static boolean processRestoreLotHp(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        int hpAdd = skill.parm1 * in.level / 100;

        if(in.testMCri()){
            hpAdd *= BattleSprite.CRI_RATE;
            processRestore(in, index, hpAdd, 0, ATTACK_CRI, battleMovie);
        }else{
            processRestore(in, index, hpAdd, 0, ATTACK_NO_CRI, battleMovie);
        }

        in.target.changeHp(hpAdd, battleMovie);

        return false;
    }

    private static boolean processClearStatusAndAnti(BattleSprite in, int index, Skill skill, BattleSprite[] them, BattleSprite[] themPet, Vector battleMovie){
        if(!in.target.testCannotBattle() && in.target.getDebufStatus() != STATUS_NORMAL){
            in.target.setDeBufStatus(1, Skill.STATUS_NORMAL, 0, 0, 0, in.bsType, in.groupIndex);
            processUnStatus(in, index, HIT_HIT, battleMovie);
        }
        
        if(skill.parm1 > 0 && World.getPercentRate(skill.parm1)){
            in.target.setBufStatus(skill.parm2, Skill.STATUS_IMMUNITY_STATUS, 1, 0, 0, in.bsType, in.groupIndex);
            processRestore(in, index, 0, 0, ATTACK_NO_CRI, battleMovie);
        }

        return false;
    }

    private static BattleSprite selectTargetRandom(BattleSprite src, BattleSprite[] them, BattleSprite[] themPet){
        int idx = -1;
        boolean allDie = true;
        BattleSprite result = null;

        for(int i = 0; i < them.length; i++){
            if(them[i] == null){
                continue;
            }

            if(!them[i].testCannotBattle()){
                allDie = false;

                break;
            }
        }

        while(!allDie){
            result = null;

            if(World.getPercentRate(50)){
                idx = World.random(0, them.length - 1);
                result = them[idx];
            }else{
                idx = World.random(0, themPet.length - 1);
                result = themPet[idx];
            }

            if(result == null){
                continue;
            }

            if(!result.testCannotBattle()){
                break;
            }
        }

        return result;
    }

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

    public static boolean chooseSkill(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie){
        if(!bs.canAction()){ //混乱状态特殊处理
            if(bs.getDebufStatus() == Skill.STATUS_CONFUSE){
                BattleSprite targetOur = selectTargetRandom(bs, our, ourPet);
                BattleSprite targetThem = selectTargetRandom(bs, them, themPet);

                if(targetOur != null && targetThem != null){
                    bs.setSkill(SKILL_ATTACK);

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

                    if(World.getPercentRate(tmp)){
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
                    bs.setSkill(SKILL_STAY);

                    return false;
                }
            }
        }

        while(true){
            int skillIndex = World.random(Skill.SOLID_SKILL_BEGIN, bs.skillList.length - 1);

            if(skillIndex >= 0){
                int[] skillStatus = Skill.getSkillStatus(bs, bs.skillList[skillIndex]);

                if(skillStatus[0] == Skill.CANNOT_SELECT_SKILL){
                    continue;
                }

                bs.setSkill(bs.skillList[skillIndex]);
            }else{
                bs.setSkill(skillIndex);
            }

            break;
        }

        boolean solidSkill = true;
        boolean win = false;

        switch(bs.skillId){
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

        if(solidSkill || win){
            return win;
        }

        Skill skill = Skill.getSkill(bs.skillId);

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

    public static boolean doPoisonFrost(BattleSprite bs, int index, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, Vector battleMovie){
        int[] movie;

        if(bs.getDebufStatus() == Skill.STATUS_POISON){
            int[] bufInfo = bs.getDebufInfo();

            int protectSrcType = bufInfo[5];
            int protectSrcIndex = bufInfo[6];

            BattleSprite src = getSprite(protectSrcType, protectSrcIndex);

            int hpDec = src.level * bufInfo[3] / 100;

            if(hpDec <= 0){
                hpDec = -1;
            }else{
                hpDec = -hpDec;
            }

            bs.changeHp(hpDec, battleMovie);
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

            BattleSprite src = getSprite(protectSrcType, protectSrcIndex);
            
            int mpDec = src.level * bufInfo[3] / 100;;

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

    private static boolean setTargetRandom(BattleSprite bs, BattleSprite[] them, BattleSprite[] themPet){
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

    private static boolean testTargetRandom(BattleSprite bs, BattleSprite[] them, BattleSprite[] themPet){
        boolean win = false;
        BattleSprite tmpSprite = selectTargetRandom(bs, them, themPet);

        if(tmpSprite == null){
            win = true;
        }

        return win;
    }

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
            
            if(showLevel){
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
            status[0] = Skill.CANNOT_SELECT_SKILL;
            status[1] = cdBout;
        }else{
            status[1] = 0;
        }

        int mpGap = 0;

        if(skill.getMpUse(bs) > bs.mp){
            mpGap = skill.getMpUse(bs) - bs.mp;
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
            case EFFECT_PET_ADD_DMG_ADD_CRI_PHY:
            case EFFECT_PET_ADD_DMG_ADD_ATTACK:
            case EFFECT_PET_ADD_DMG_DEC_HIT:
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
            case EFFECT_PET_IMM_STATUS:
            case EFFECT_PET_DEC_DMG:
            case EFFECT_PET_AUTO_RELIFE:
                status[3] = Skill.CHOOSE_NONE;

                break;
            case EFFECT_PET_PROTECT_OWNER:
            case EFFECT_PET_ADD_OWNER_HP:
            case EFFECT_PET_ADD_OWNER_MP:
            case EFFECT_PET_UN_ALL_STATUS:
                status[3] = Skill.CHOOSE_OWNER;

                break;
        }

        return status;
    }

    public static void addSkills(byte[] in){
        ByteArrayInputStream bis = new ByteArrayInputStream(in);
        DataInputStream dis = new DataInputStream(bis);

        try{
            short skillNumber = dis.readShort();
            Skill skill;
            int id;

            for(int i = 0; i < skillNumber; i++){
                byte skillType = dis.readByte();
                String skillName = dis.readUTF();
                byte skillEffect = dis.readByte();
                byte skillStatus = dis.readByte();
                byte skillPosition = dis.readByte();
                byte skillCDID = dis.readByte();
                byte skillCDBout = dis.readByte();
                byte learnLevel = dis.readByte();

                for(int j = 0; j < learnLevel; j++){
                    skill = new Skill(skillType, skillName, skillEffect, skillStatus, skillPosition, skillCDID, skillCDBout);
                    id = skill.initParm(dis);
                    allSkill.put(new Integer(id), skill);
                }
            }
        }catch(Exception e){
            //#debug
            e.printStackTrace();
        }

        //TODO delete
        //#debug
        //giveMeSkills();
    }

    public static void giveMeSkills(){
        Vector playerSkills = new Vector();
        Enumeration emu = allSkill.elements();

        while(emu.hasMoreElements()){
            Skill skill = (Skill)emu.nextElement();

            if(skill.type == TYPE_ASS){
                playerSkills.addElement(skill);
            }
        }

        World.player.skillList = new short[playerSkills.size()];

        for(int i = 0; i < playerSkills.size(); i++){
            Skill skill = (Skill)playerSkills.elementAt(i);
            World.player.skillList[i] = skill.id;
        }
    }
}