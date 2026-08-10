package com.pip.itimes.server.world.battle;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface SkillConstants {
    /*仇恨类型-单个仇恨*/
    public static final int ENMITY_SINGLE = 1;

    /*仇恨类型-全体仇恨*/
    public static final int ENMITY_ALL = 2;

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
     * 扣蓝
     */
    public static final byte SKILL_SUBMAGIC = -9;

    /**
     * 怪物使用固有技能区间
     */
    public static final byte SOLID_SKILL_BEGIN = -1;

    /**
     * 未选择技能，在战斗中此状态参战者将自动分配技能
     */
    public static final byte SKILL_NOT_READY = 0;

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
     * 宠物，治：给主加血  生命链接
     */
    public static final byte EFFECT_PET_ADD_OWNER_HP = 49;

    /**
     * 宠物，治：给主回魔  魔力链接
     */
    public static final byte EFFECT_PET_ADD_OWNER_MP = 50;

    /**
     * 宠物，治：解除状态
     */
    public static final byte EFFECT_PET_UN_ALL_STATUS = 51;
    
    /**
     * 人物：防御
     */
    public static final byte EFFECT_DEFENCE = 52;
    
    /**
     * 宠物，物：双次物攻加伤害
     */
    public static final byte EFFECT_PET_2_ATTACK2 = 53;
    
    /**
     * 宠物，物：加伤加爆
     */
    public static final byte EFFECT_PET_ADD_DMG_ADD_CRI_PHY2 = 54;
    
    /**
     * 宠物，物：加伤降命
     */
    public static final byte EFFECT_PET_ADD_DMG_DEC_HIT2 = 55;
    
    /**
     * 宠物，防：减低伤害
     */
    public static final byte EFFECT_PET_DEC_DMG_ALL_BATTLE2 = 56;
    
    /**
     * 宠物，防：替主抗伤
     */
    public static final byte EFFECT_PET_PROTECT_OWNER2 = 57;
    
    /**
     * 宠物，治：给主加血  生命链接
     */
    public static final byte EFFECT_PET_ADD_OWNER_HP2 = 58;
    /**
     * 宠物，（圣宠技能）撕咬
     */
    public static final byte EFFECT_PET_BAIT = 59;
    /**
     * 宠物，（圣宠技能）厄运钟子
     */
    public static final byte EFFECT_PET_BAD_SEED = 60;
    /**
     * 宠物，（圣宠技能）牺牲祝福
     */
    public static final byte EFFECT_PET_SACRIFICE_BLESS = 61;

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
    
    /**
     * 师傅分担徒弟的伤害
     */
    public static final byte ATTACK_MASTER_PROCTECT = 5;

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

    /**
     * 使用物品
     */
    public static final byte TYPE_ITEM = 5;

    /**
     * 逃跑
     */
    public static final byte TYPE_RUNAWAY = 6;

    /**
     * 抓取
     */
    public static final byte TYPE_CATCH = 7;

    /**
     * 静止
     */
    public static final byte TYPE_STAY = 8;
}
