package com.pip.sanguo.data.skill;

import java.lang.reflect.Constructor;
import java.util.List;

import org.jdom.Element;

import com.pip.sanguo.editor.skill.ParamIndicator;
import com.pip.util.Utils;

/**
 * 技能/BUFF效果配置。
 */
public abstract class EffectConfig implements Cloneable {
    public static final int CHANGE_PHYICAL_AP = 0;
    public static final int CHANGE_MAGIC_AP = 1;
    public static final int CHANGE_WEAPON_ATK = 2;
    public static final int CHANGE_WEAPON_MATK = 3;
    public static final int CHANGE_THREAT = 4;
    public static final int CHANGE_ARMOR = 5;
    public static final int CHANGE_PHYSICAL_HIT = 6;
    public static final int CHANGE_PHYSICAL_CRIT = 7;
    public static final int CHANGE_PHYSICAL_DODGE = 8;
    public static final int CHANGE_MAGIC_CRIT = 9;
    public static final int CHANGE_MP_RENEW = 10;
    public static final int CHANGE_HP_RENEW = 11;
    public static final int CHANGE_SPEED = 12;
    public static final int CHANGE_MAXHP = 13;
    public static final int CHANGE_CURE_EFFECT = 14;
    public static final int APPEND_MAGIC_DAMAGE = 15;
    public static final int IGNORE_ARMOR = 16;
    public static final int ADD_MP_ON_HIT = 17;
    public static final int ADD_DEBUFF_ON_HIT = 18;
    public static final int ADD_BUFF_ON_HIT = 19;
    public static final int FIRST_THREAT_ON_HIT = 20;
    public static final int FEAR_ON_HIT = 21;
    public static final int SLOW_ON_HIT = 22;
    public static final int PARALYZE_ON_HIT = 23;
    public static final int STAY_ON_HIT = 24;
    public static final int REPEAT_ON_HIT = 25;
    public static final int DOUBLE_DAMAGE_ON_HIT = 26;
    public static final int DEC_MP_ON_HIT = 27;
    public static final int ADD_HP_ON_HIT = 28;
    public static final int TWO_HIT_ON_HIT = 29;
    public static final int RELIVE_TARGET = 30;
    public static final int CURE_TARGET = 31;
    public static final int IMMUNE_PHYICAL_ATTACK = 32;
    public static final int IMMUNE_MAGIC_ATTACK = 33;
    public static final int IMMUNE_SLOW_ATTACK = 34;
    public static final int COUNTER_ATTACK = 35;
    public static final int BOUNCE = 36;
    public static final int CHANGE_MP_USE = 37;
    public static final int SET_VARIABLE = 38;
    public static final int HOT = 39;
    public static final int DOT = 40;
    public static final int MP_SHIELD = 41;
    public static final int VAMPIRE_ON_HIT = 42;
    public static final int CHANGE_CD_TIME = 43;
    public static final int CHANGE_DISTANCE = 44;
    public static final int CHANGE_ACT_TIME = 45;
    public static final int CHANGE_RANGE = 46; 
    public static final int CURE_TARGET_IGNORE_MAX = 47;
    public static final int CHANGE_MAGIC_ARMOR = 48;
    public static final int IGNORE_MAGIC_ARMOR = 49;
    public static final int REDUCE_PHYSICAL_DAMAGE = 50;
    public static final int REDUCE_MAGIC_DAMAGE = 51;
    public static final int CHANGE_MAGIC_HIT = 52;
    public static final int CHANGE_MAGIC_DODGE = 53;
    public static final int CANNOT_MOVE = 54;
    public static final int MPHOT = 55;
    public static final int MPDOT = 56;
    public static final int DISPEL_BUFF = 57;
    public static final int DISPEL_ALL_BUFF = 58;
    public static final int DISPEL_DEBUFF = 59;
    public static final int DISPEL_ALL_DEBUFF = 60;
    public static final int CHANGE_STA = 61;
    public static final int CHANGE_AGI = 62;
    public static final int CHANGE_STR = 63;
    public static final int CHANGE_INT = 64;
    public static final int HP_ACTIVE_BUFF = 65;
    public static final int CRIT_ACTIVE_BUFF = 66;
    public static final int CRITED_ACTIVE_BUFF = 67;
    public static final int DUMB_ON_HIT = 68;
    public static final int IMMUNE_FEAR = 69;
    public static final int IMMUNE_DUMB = 70;
    public static final int IMMUNE_PARALYZE = 71;
    public static final int IMMUNE_STAY = 72;
    public static final int CHANGE_BASIC_MAGIC_AP = 73;
    public static final int CHANGE_BASIC_HP = 74;
    public static final int CHANGE_BASIC_MP = 75;
    public static final int LIMIT_EFFECT_TIMES = 76;
    public static final int CHANGE_MAGIC_HEAL = 77;
    public static final int LIMIT_SKILL = 78;
    public static final int CHANGE_BATTLE_PHYICAL_AP = 79;
    public static final int CHANGE_BATTLE_MAGIC_AP = 80;
    public static final int CHANGE_BATTLE_WEAPON_ATK = 81;
    public static final int CHANGE_BATTLE_WEAPON_MATK = 82;
    public static final int CHANGE_BATTLE_PHYSICAL_HIT = 83;
    public static final int CHANGE_BATTLE_PHYSICAL_CRIT = 84;
    public static final int CHANGE_BATTLE_PHYSICAL_DODGE = 85;
    public static final int CHANGE_BATTLE_MAGIC_CRIT = 86;
    public static final int CHANGE_BATTLE_MAGIC_HIT = 87;
    public static final int CHANGE_BATTLE_MAGIC_DODGE = 88;
    public static final int CHANGE_BATTLE_PHYSICAL_CRITED = 89;
    public static final int CHANGE_BATTLE_MAGIC_CRITED = 90;
    public static final int CHANGE_BATTLE_ARMOR = 91;
    public static final int CHANGE_BATTLE_MAGIC_ARMOR = 92;
    public static final int CHANGE_EXP_RATE = 93;
    public static final int CHANGE_MONEY_RATE = 94;
    public static final int CHANGE_PHYSICAL_HIT_RATE = 95;
    public static final int CHANGE_PHYSICAL_CRIT_RATE = 96;
    public static final int CHANGE_PHYSICAL_DODGE_RATE = 97;
    public static final int CHANGE_MAGIC_CRIT_RATE = 98;
    public static final int CHANGE_MAGIC_HIT_RATE = 99;
    public static final int CHANGE_MAGIC_DODGE_RATE = 100;
    public static final int CHANGE_HORSE_EXP_RATE = 101;
    public static final int INTERRUPT = 102;
    public static final int ADD_DEBUFF_ON_HITED = 103;
    public static final int ADD_BUFF_ON_HITED = 104;
    public static final int CHANGE_PARAM = 105;
    public static final int CHANGE_THREAT_TOTAL = 106;
    public static final int FIRST_THREAT_TEMP = 107;
    public static final int TRANSPORT_TO_ME = 108;
    public static final int TRANSPORT_TO_POS = 109;
    public static final int REMOVE_ON_BATTLE_END = 110;
    public static final int SLOW_ON_HITED = 111;
    public static final int IMMUNE_BREAKATTACK = 112;
    public static final int ADD_SPEED = 113;
    public static final int DOUBLE_HEAL_ON_HIT = 114;
    public static final int LIMIT_MAP = 115;
    public static final int ADD_NATUAL_ENHANCE = 116;
    public static final int ADD_JEWEL_ENHANCE = 117;
    public static final int ADD_STAR_ENHANCE = 118;
    public static final int ADD_HP_ENHANCE = 119;
    public static final int ADD_MP_ENHANCE = 120;

    public static final String[][] TYPE_NAMES = {
        { "改变物理攻击力", "物攻" },                  /* CHANGE_PHYICAL_AP */
        { "改变法术攻击力", "法攻" },                  /* CHANGE_MAGIC_AP */  
        { "改变武器物理攻击力", "武攻" },              /* CHANGE_WEAPON_ATK */
        { "改变武器法术攻击力", "武攻" },              /* CHANGE_WEAPON_MATK */ 
        { "改变威胁值", "威胁" },                      /* CHANGE_THREAT */ 
        { "改变护甲", "护甲" },                        /* CHANGE_ARMOR */ 
        { "改变物理命中率", "物命" },                  /* CHANGE_PHYSICAL_HIT */  
        { "改变物理暴击率", "物暴" },                  /* CHANGE_PHYSICAL_CRIT */ 
        { "改变物理闪避率", "物闪" },                  /* CHANGE_PHYSICAL_DODGE */  
        { "改变法术暴击率", "法暴" },                  /* CHANGE_MAGIC_CRIT */ 
        { "改变每5秒回蓝", "5秒回蓝" },                /* CHANGE_MP_RENEW */  
        { "改变每5秒回血", "5秒回血" },                /* CHANGE_HP_RENEW */  
        { "改变移动速度", "速度" },                    /* CHANGE_SPEED */ 
        { "改变生命上限", "生命" },                    /* CHANGE_MAXHP */  
        { "改变治疗量", "治疗" },                      /* CHANGE_CURE_EFFECT */  
        { "物理攻击附加法术伤害", "法攻" },             /* APPEND_MAGIC_DAMAGE */  
        { "无视护甲", "无视护甲" },                    /* IGNORE_ARMOR */  
        { "命中后回蓝", "回蓝" },                      /* ADD_MP_ON_HIT */ 
        { "命中后给目标加BUFF", "BUFF目标" },          /* ADD_DEBUFF_ON_HIT */  
        { "命中后给自己加BUFF", "BUFF自己" },            /* ADD_BUFF_ON_HIT */ 
        { "命中后成为第一仇恨", "仇恨" },              /* FIRST_THREAT_ON_HIT */  
        { "命中后恐惧", "恐惧" },                      /* FEAR_ON_HIT */  
        { "命中后减速", "减速" },                      /* SLOW_ON_HIT */ 
        { "命中后麻痹", "麻痹" },                      /* PARALYZE_ON_HIT */  
        { "命中后定身", "定身" },                      /* STAY_ON_HIT */  
        { "命中后连击", "连击" },                      /* REPEAT_ON_HIT */  
        { "命中后双倍伤害", "双倍伤害" },              /* DOUBLE_DAMAGE_ON_HIT */  
        { "命中后烧蓝", "烧蓝" },                      /* DEC_MP_ON_HIT */  
        { "命中后回血", "回血" },                      /* ADD_HP_ON_HIT */ 
        { "无条件连击2次", "3连击" },                  /* TWO_HIT_ON_HIT */  
        { "复活目标", "复活" },                        /* RELIVE_TARGET */  
        { "设置治疗量", "治疗" },                        /* CURE_TARGET */  
        { "免疫物理攻击", "物理免疫" },                /* IMMUNE_PHYICAL_ATTACK */  
        { "免疫法术攻击", "法术免疫" },                /* IMMUNE_MAGIC_ATTACK */  
        { "免疫减速", "免疫减速" },                    /* IMMUNE_SLOW_ATTACK */ 
        { "反击", "反击" },                            /* COUNTER_ATTACK */  
        { "反弹伤害", "反弹" },                        /* BOUNCE */  
        { "改变技能消耗", "MP消耗" },                  /* CHANGE_MP_USE */  
        { "设置战斗变量", "设变量" },                  /* SET_VARIABLE */  
        { "持续回血", "HOT" },                        /* HOT */  
        { "持续伤害", "DOT" },                        /* DOT */ 
        { "消耗蓝抵消伤害", "法力盾" },                /* MP_SHIELD */  
        { "被命中后为攻击者全队回血", "吸血鬼" },       /* VAMPIRE_ON_HIT */ 
        { "改变技能CD", "CD" },                        /* CHANGE_CD_TIME */ 
        { "改变技能攻击距离", "距离" },                /* CHANGE_DISTANCE */ 
        { "改变技能施法时间", "施法时间" },             /* CHANGE_ACT_TIME */ 
        { "改变AOE范围", "范围" },                      /* CHANGE_RANGE */ 
        { "治疗目标(无视上限)", "治疗" },                /* CURE_TARGET_IGNORE_MAX */
        { "改变法术防御", "法防" },                    /* CHANGE_MAGIC_ARMOR */
        { "无视法术防御", "无视法防" },                /* IGNORE_MAGIC_ARMOR */
        { "物理伤害减免", "物理减免" },                /* REDUCE_PHYSICIAL_DAMAGE */
        { "法术伤害减免", "法术减免" },                /* REDUCE_MAGIC_DAMAGE */
        { "改变法术命中率", "法命" },                  /* CHANGE_MAGIC_HIT */
        { "改变法术闪避率", "法闪" },                  /* CHANGE_MAGIC_DODGE */
        { "不可移动和战斗", "不可移动" },              /* CANNOT_MOVE */
        { "持续回蓝", "MPHOT" },                        /* MPHOT */  
        { "持续抽蓝", "MPDOT" },                        /* MPDOT */ 
        { "驱散一个有益状态", "驱散一个" },            /* DISPEL_BUFF */
        { "驱散所有有益状态", "驱散所有" },            /* DISPEL_ALL_BUFF */
        { "驱散一个有害状态", "驱散一个" },            /* DISPEL_DEBUFF */
        { "驱散所有有害状态", "驱散所有" },            /* DISPEL_ALL_DEBUFF */
        { "改变耐力", "耐力" },                       /* CHANGE_STA */
        { "改变敏捷", "敏捷" },                       /* CHANGE_AGI */
        { "改变力量", "力量" },                       /* CHANGE_STR */
        { "改变智力", "智力" },                       /* CHANGE_INT */
        { "一定血量下激活BUFF", "血量激活" },          /* HP_ACTIVE_BUFF */
        { "暴击激活BUFF", "暴击激活" },               /* CRIT_ACTIVE_BUFF */
        { "被暴击激活BUFF", "被暴击激活" },            /* CRITED_ACTIVE_BUFF */
        { "命中后沉默", "沉默" },                     /* DUMB_ON_HIT */
        { "免疫恐惧", "免疫恐惧" },                   /* IMMUNE_FEAR */
        { "免疫沉默", "免疫沉默" },                   /* IMMUNE_DUMB */
        { "免疫麻痹", "免疫麻痹" },                    /* IMMUNE_PARALYZE */
        { "免疫定身", "免疫定身" },                   /* IMMUNE_STAY */
        { "改变基础法术攻击力", "基础法攻" },          /* CHANGE_BASIC_MAGIC_AP */
        { "改变基础生命上限", "基础生命" },            /* CHANGE_BASIC_HP */
        { "改变基础法力上限", "基础法力" },            /* CHANGE_BASIC_MP */
        { "限制生效次数", "次数" },                   /* LIMIT_EFFECT_TIMES */
        { "改变治疗效果", "治疗效果" },               /* CHANGE_MAGIC_HEAL */
        { "限制影响的技能", "限制技能" },             /* LIMIT_SKILL */
        { "战斗时改变物理攻击力", "物攻" },           /* CHANGE_BATTLE_PHYICAL_AP */
        { "战斗时改变法术攻击力", "法攻" },           /* CHANGE_BATTLE_MAGIC_AP */  
        { "战斗时改变武器物理攻击力", "武攻" },       /* CHANGE_BATTLE_WEAPON_ATK */
        { "战斗时改变武器法术攻击力", "武攻" },       /* CHANGE_BATTLE_WEAPON_MATK */ 
        { "战斗时改变物理命中率", "物命" },           /* CHANGE_BATTLE_PHYSICAL_HIT */  
        { "战斗时改变物理暴击率", "物暴" },           /* CHANGE_BATTLE_PHYSICAL_CRIT */ 
        { "战斗时改变物理闪避率", "物闪" },           /* CHANGE_BATTLE_PHYSICAL_DODGE */  
        { "战斗时改变法术暴击率", "法暴" },           /* CHANGE_BATTLE_MAGIC_CRIT */ 
        { "战斗时改变法术命中率", "法命" },           /* CHANGE_BATTLE_MAGIC_HIT */
        { "战斗时改变法术闪避率", "法闪" },           /* CHANGE_BATTLE_MAGIC_DODGE */
        { "战斗时改变被物理暴击率", "被物暴" },       /* CHANGE_BATTLE_PHYSICAL_CRITED */ 
        { "战斗时改变被法术暴击率", "被法暴" },       /* CHANGE_BATTLE_MAGIC_CRITED */ 
        { "战斗时改变护甲", "护甲" },                 /* CHANGE_BATTLE_ARMOR */ 
        { "战斗时改变法术防御", "法防" },             /* CHANGE_BATTLE_MAGIC_ARMOR */
        { "改变经验值获得速度", "经验" },             /* CHANGE_EXP_RATE */
        { "改变金钱获得速度", "经验" },               /* CHANGE_MONEY_RATE */
        { "提高物理命中等级", "物命等级" },           /* CHANGE_PHYSICAL_HIT_RATE */
        { "提高物理暴击等级", "物暴等级" },           /* CHANGE_PHYSICAL_CRIT_RATE */
        { "提高物理闪避等级", "物闪等级" },           /* CHANGE_PHYSICAL_DODGE_RATE */
        { "提高法术暴击等级", "法暴等级" },           /* CHANGE_MAGIC_CRIT_RATE */
        { "提高法术命中等级", "法命等级" },           /* CHANGE_MAGIC_HIT_RATE */
        { "提高法术闪避等级", "法闪等级" },           /* CHANGE_MAGIC_DODGE_RATE */
        { "改变坐骑经验值获得速度", "坐骑经验" },     /* CHANGE_HORSE_EXP_RATE */
        { "打断施法", "打断" },                      /* INTERRUPT */
        { "被命中后给攻击者加BUFF", "BUFF攻击者" },   /* ADD_DEBUFF_ON_HITED */  
        { "被命中后给自己加BUFF", "BUFF自己" },       /* ADD_BUFF_ON_HITED */ 
        { "修改技能/BUFF参数", "修改参数" },          /* CHANGE_PARAM */
        { "改变累计威胁值", "改变威胁" },             /* CHANGE_THREAT_TOTAL */
        { "使目标临时成为第一仇恨", "临时仇恨" },      /* FIRST_THREAT_TEMP */  
        { "传送到自己身边", "传送" },                 /* TRANSPORT_TO_ME */
        { "传送到指定位置", "传送" },                 /* TRANSPORT_TO_POS */
        { "战斗结束后消失", "战斗结束消失" },         /* REMOVE_ON_BATTLE_END */
        { "被命中后减速", "减速敌人" },               /* SLOW_ON_HITED */
        { "免疫打断", "免疫打断" },                   /* IMMUNE_BREAKATTACK */
        { "增加移动速度", "加速" },                   /* ADD_SPEED */ 
        { "一定几率双倍治疗", "双倍治疗" },              /* DOUBLE_HEAL_ON_HIT */  
        { "限定生效场景", "限定场景" },              /* LIMIT_MAP */  
        { "增加资质鉴定效果", "资质增加幅度" },              /* ADD_NATUALENHANCE */ 
        { "增加宝石镶嵌效果", "宝石增加幅度" },              /* ADD_JEWELENHANCE */ 
        { "增加星级鉴定效果", "星级增加幅度" },              /* ADD_STARENHANCE */ 
        { "按比例加血", "按比例加血" },              /* ADD_STARENHANCE */ 
        { "按比例加蓝", "按比例加蓝" },              /* ADD_STARENHANCE */ 
    };
    public static final Class[] TYPE_CLASSES = {
        Effect_MultiAdd.class,                        /* CHANGE_PHYICAL_AP */
        Effect_MultiAdd.class,                        /* CHANGE_MAGIC_AP */ 
        Effect_PercentAdd.class,                      /* CHANGE_WEAPON_ATK */
        Effect_PercentAdd.class,                      /* CHANGE_WEAPON_MATK */ 
        Effect_MultiAdd.class,                        /* CHANGE_THREAT */ 
        Effect_MultiAdd.class,                        /* CHANGE_ARMOR */ 
        Effect_PercentAdd.class,                      /* CHANGE_PHYSICAL_HIT */  
        Effect_PercentAdd.class,                      /* CHANGE_PHYSICAL_CRIT */ 
        Effect_PercentAdd.class,                      /* CHANGE_PHYSICAL_DODGE */  
        Effect_PercentAdd.class,                      /* CHANGE_MAGIC_CRIT */
        Effect_FixValueAdd.class,                     /* CHANGE_MP_RENEW */  
        Effect_FixValueAdd.class,                     /* CHANGE_HP_RENEW */ 
        Effect_PercentAdd.class,                      /* CHANGE_SPEED */ 
        Effect_MultiAdd.class,                        /* CHANGE_MAXHP */  
        Effect_MultiAdd.class,                        /* CHANGE_CURE_EFFECT */  
        Effect_FixValueAdd.class,                     /* APPEND_MAGIC_DAMAGE */  
        Effect_MultiAdd.class,                        /* IGNORE_ARMOR */  
        Effect_CureOnHit.class,                       /* ADD_MP_ON_HIT */ 
        Effect_AddBuff.class,                         /* ADD_DEBUFF_ON_HIT */  
        Effect_AddBuff.class,                         /* ADD_BUFF_ON_HIT */ 
        Effect_FirstThreat.class,                     /* FIRST_THREAT_ON_HIT */  
        Effect_FearOnHit.class,                       /* FEAR_ON_HIT */  
        Effect_SlowOnHit.class,                       /* SLOW_ON_HIT */ 
        Effect_FearOnHit.class,                       /* PARALYZE_ON_HIT */  
        Effect_FearOnHit.class,                       /* STAY_ON_HIT */  
        Effect_PercentAdd.class,                      /* REPEAT_ON_HIT */  
        Effect_PercentAdd.class,                      /* DOUBLE_DAMAGE_ON_HIT */  
        Effect_CureOnHit.class,                       /* DEC_MP_ON_HIT */  
        Effect_CureOnHit.class,                       /* ADD_HP_ON_HIT */ 
        Effect_Hit3Times.class,                       /* TWO_HIT_ON_HIT */  
        Effect_PercentAdd.class,                      /* RELIVE_TARGET */  
        Effect_FixValueAdd.class,                     /* CURE_TARGET */  
        Effect_PercentAdd.class,                      /* IMMUNE_PHYICAL_ATTACK */  
        Effect_PercentAdd.class,                      /* IMMUNE_MAGIC_ATTACK */  
        Effect_PercentAdd.class,                      /* IMMUNE_SLOW_ATTACK */
        Effect_PercentAdd.class,                      /* COUNTER_ATTACK */  
        Effect_Bounce.class,                          /* BOUNCE */  
        Effect_ChangeSkill.class,                     /* CHANGE_MP_USE */  
        Effect_SetVariable.class,                     /* SET_VARIABLE */  
        Effect_HOT.class,                             /* HOT */  
        Effect_HOT.class,                             /* DOT */ 
        Effect_Shield.class,                          /* MP_SHIELD */  
        Effect_Vampire.class,                         /* VAMPIRE_ON_HIT */ 
        Effect_ChangeSkill.class,                     /* CHANGE_CD_TIME */ 
        Effect_ChangeSkill.class,                     /* CHANGE_DISTANCE */ 
        Effect_ChangeSkill.class,                     /* CHANGE_ACT_TIME */ 
        Effect_ChangeSkill.class,                     /* CHANGE_RANGE */
        Effect_CureOnHit.class,                       /* CURE_TARGET_IGNORE_MAX */
        Effect_MultiAdd.class,                        /* CHANGE_MAGIC_ARMOR */
        Effect_MultiAdd.class,                        /* IGNORE_MAGIC_ARMOR */
        Effect_MultiAdd.class,                        /* REDUCE_PHYSICIAL_DAMAGE */
        Effect_MultiAdd.class,                        /* REDUCE_MAGIC_DAMAGE */
        Effect_PercentAdd.class,                      /* CHANGE_MAGIC_HIT */  
        Effect_PercentAdd.class,                      /* CHANGE_MAGIC_DODGE */
        Effect_CannotMove.class,                      /* CANNOT_MOVE */
        Effect_HOT.class,                             /* MPHOT */  
        Effect_HOT.class,                             /* MPDOT */ 
        Effect_PercentAdd.class,                      /* DISPEL_BUFF */
        Effect_PercentAdd.class,                      /* DISPEL_ALL_BUFF */
        Effect_PercentAdd.class,                      /* DISPEL_DEBUFF */
        Effect_PercentAdd.class,                      /* DISPEL_ALL_DEBUFF */
        Effect_MultiAdd.class,                        /* CHANGE_STA */
        Effect_MultiAdd.class,                        /* CHANGE_AGI */
        Effect_MultiAdd.class,                        /* CHANGE_STR */
        Effect_MultiAdd.class,                        /* CHANGE_INT */
        Effect_HPActiveBuff.class,                    /* HP_ACTIVE_BUFF */
        Effect_CritActiveBuff.class,                  /* CRIT_ACTIVE_BUFF */
        Effect_CritActiveBuff.class,                  /* CRITED_ACTIVE_BUFF */
        Effect_FearOnHit.class,                      /* DUMB_ON_HIT */
        Effect_PercentAdd.class,                      /* IMMUNE_FEAR */
        Effect_PercentAdd.class,                      /* IMMUNE_DUMB */
        Effect_PercentAdd.class,                      /* IMMUNE_PARALYZE */
        Effect_PercentAdd.class,                      /* IMMUNE_STAY */
        Effect_MultiAdd.class,                        /* CHANGE_BASIC_MAGIC_AP */ 
        Effect_MultiAdd.class,                        /* CHANGE_BASIC_HP */ 
        Effect_MultiAdd.class,                        /* CHANGE_BASIC_MP */ 
        Effect_FixValueAdd.class,                     /* LIMIT_EFFECT_TIMES */ 
        Effect_MultiAdd.class,                        /* CHANGE_MAGIC_HEAL */
        Effect_LimitSkill.class,                      /* LIMIT_SKILL */
        Effect_MultiAdd.class,                        /* CHANGE_BATTLE_PHYICAL_AP */
        Effect_MultiAdd.class,                        /* CHANGE_BATTLE_MAGIC_AP */ 
        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_WEAPON_ATK */
        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_WEAPON_MATK */ 
        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_PHYSICAL_HIT */  
        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_PHYSICAL_CRIT */ 
        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_PHYSICAL_DODGE */  
        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_MAGIC_CRIT */
        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_MAGIC_HIT */  
        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_MAGIC_DODGE */
        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_PHYSICAL_CRITED */ 
        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_MAGIC_CRITED */
        Effect_MultiAdd.class,                        /* CHANGE_BATTLE_ARMOR */ 
        Effect_MultiAdd.class,                        /* CHANGE_BATTLE_MAGIC_ARMOR */
        Effect_PercentAdd.class,                      /* CHANGE_EXP_RATE */
        Effect_PercentAdd.class,                      /* CHANGE_MONEY_RATE */
        Effect_FixValueAdd.class,                     /* CHANGE_PHYSICAL_HIT_RATE */  
        Effect_FixValueAdd.class,                     /* CHANGE_PHYSICAL_CRIT_RATE */  
        Effect_FixValueAdd.class,                     /* CHANGE_PHYSICAL_DODGE_RATE */  
        Effect_FixValueAdd.class,                     /* CHANGE_MAGIC_CRIT_RATE */  
        Effect_FixValueAdd.class,                     /* CHANGE_MAGIC_HIT_RATE */  
        Effect_FixValueAdd.class,                     /* CHANGE_MAGIC_DODGE_RATE */  
        Effect_PercentAdd.class,                      /* CHANGE_HORSE_EXP_RATE */
        Effect_PercentAdd.class,                      /* INTERRUPT */
        Effect_AddBuff.class,                         /* ADD_DEBUFF_ON_HITED */  
        Effect_AddBuff.class,                         /* ADD_BUFF_ON_HITED */ 
        Effect_ChangeParam.class,                     /* CHANGE_PARAM */ 
        Effect_PercentAdd.class,                      /* CHANGE_THREAT_TOTAL */ 
        Effect_FirstThreatTemp.class,                 /* FIRST_THREAT_TEMP */
        Effect_CannotMove.class,                      /* TRANSPORT_TO_ME */
        Effect_Transport.class,                       /* TRANSPORT_TO_POS */
        Effect_CannotMove.class,                      /* REMOVE_ON_BATTLE_END */
        Effect_SlowOnHited.class,                     /* SLOW_ON_HITED */
        Effect_PercentAdd.class,                      /* IMMUNE_BREAKATTACK */
        Effect_PercentAdd.class,                      /* ADD_SPEED */
        Effect_PercentAdd.class,                      /* DOUBLE_HEAL_ON_HIT */  
        Effect_LimitMap.class,                      /* LIMIT_MAP */  
        Effect_PercentAdd.class,                      /* ADD_NATUALENHANCE */
        Effect_PercentAdd.class,                      /* ADD_JEWELENHANCE */
        Effect_PercentAdd.class,                      /* ADD_STARENHANCE */
        Effect_HP.class,
        Effect_HP.class
    };
    public static final String[][] TYPE_PARAMS = {
//        Effect_MultiAdd.class,                        /* CHANGE_PHYICAL_AP */
        { "change_physical_ap_value", "change_physical_ap_percent" },
//        Effect_MultiAdd.class,                        /* CHANGE_MAGIC_AP */
        { "change_magic_ap_value", "change_magic_ap_percent" },
//        Effect_PercentAdd.class,                      /* CHANGE_WEAPON_ATK */
        { "change_weapon_atk_value" },
//        Effect_PercentAdd.class,                      /* CHANGE_WEAPON_MATK */ 
        { "change_weapon_matk_value" },
//        Effect_MultiAdd.class,                        /* CHANGE_THREAT */
        { "change_threat_value", "change_threat_percent" },
//        Effect_MultiAdd.class,                        /* CHANGE_ARMOR */
        { "change_armor_value", "change_armor_percent" },
//        Effect_PercentAdd.class,                      /* CHANGE_PHYSICAL_HIT */
        { "change_physical_hit" },
//        Effect_PercentAdd.class,                      /* CHANGE_PHYSICAL_CRIT */
        { "change_physical_crit" },
//        Effect_PercentAdd.class,                      /* CHANGE_PHYSICAL_DODGE */
        { "change_physical_dodge" },
//        Effect_PercentAdd.class,                      /* CHANGE_MAGIC_CRIT */
        { "change_magic_crit" },
//        Effect_FixValueAdd.class,                     /* CHANGE_MP_RENEW */ 
        { "change_mp_renew" },
//        Effect_FixValueAdd.class,                     /* CHANGE_HP_RENEW */ 
        { "change_hp_renew" },
//        Effect_PercentAdd.class,                      /* CHANGE_SPEED */ 
        { "change_speed_percent" },
//        Effect_MultiAdd.class,                        /* CHANGE_MAXHP */
        { "change_maxhp_value", "change_maxhp_percent" },
//        Effect_MultiAdd.class,                        /* CHANGE_CURE_EFFECT */  
        { "change_cure_value", "change_cure_percent" },
//        Effect_FixValueAdd.class,                     /* APPEND_MAGIC_DAMAGE */
        { "append_magic_damage" },
//        Effect_MultiAdd.class,                        /* IGNORE_ARMOR */
        { "ignore_armor_value", "ignore_armor_percent" },
//        Effect_CureOnHit.class,                       /* ADD_MP_ON_HIT */
        { "add_mp_rate", "add_mp_value", "add_mp_percent_of_max", "add_mp_percent_of_damage" },
//        Effect_AddBuff.class,                         /* ADD_DEBUFF_ON_HIT */  
        { "add_debuff_rate", "add_debuff_rate_var", "add_debuff_id", "add_debuff_level" },
//        Effect_AddBuff.class,                         /* ADD_BUFF_ON_HIT */ 
        { "add_buff_rate", "add_buff_rate_var", "add_buff_id", "add_buff_level" },
//        Effect_FirstThreat.class,                     /* FIRST_THREAT_ON_HIT */  
        { },
//        Effect_FearOnHit.class,                       /* FEAR_ON_HIT */ 
        { "fear_rate", "fear_rate_var", "fear_time" },
//        Effect_SlowOnHit.class,                       /* SLOW_ON_HIT */ 
        { "slow_rate", "slow_rate_var", "slow_level", "slow_level_var", "slow_time", "slow_time_var" },
//        Effect_FearOnHit.class,                       /* PARALYZE_ON_HIT */  
        { "paralyze_rate", "paralyze_rate_var", "paralyse_time" },
//        Effect_FearOnHit.class,                       /* STAY_ON_HIT */  
        { "stay_rate", "stay_rate_var", "stay_time" },
//        Effect_PercentAdd.class,                      /* REPEAT_ON_HIT */
        { "repeat_rate" },
//        Effect_PercentAdd.class,                      /* DOUBLE_DAMAGE_ON_HIT */  
        { "double_damage_rate" },
//        Effect_CureOnHit.class,                       /* DEC_MP_ON_HIT */ 
        { "dec_mp_rate", "dec_mp_value", "dec_mp_percent_of_max", "dec_mp_percent_of_damage" },
//        Effect_CureOnHit.class,                       /* ADD_HP_ON_HIT */ 
        { "add_hp_rate", "add_hp_value", "add_hp_percent_of_max", "add_hp_percent_of_damage" },
//        Effect_Hit3Times.class,                       /* TWO_HIT_ON_HIT */  
        { },
//        Effect_PercentAdd.class,                      /* RELIVE_TARGET */  
        { "relive_restore_percent" },
//        Effect_FixValueAdd.class,                     /* CURE_TARGET */
        { "cure_value" },
//        Effect_PercentAdd.class,                      /* IMMUNE_PHYICAL_ATTACK */  
        { "immune_physical_attack_rate" },
//        Effect_PercentAdd.class,                      /* IMMUNE_MAGIC_ATTACK */  
        { "immune_magic_attack_rate" },
//        Effect_PercentAdd.class,                      /* IMMUNE_SLOW_ATTACK */
        { "immune_slow_rate" },
//        Effect_PercentAdd.class,                      /* COUNTER_ATTACK */  
        { "counter_attack_rate" },
//        Effect_Bounce.class,                          /* BOUNCE */  
        { "bounce_rate", "bounce_damage_type", "bounce_value", "bounce_percent_of_damage" },
//        Effect_ChangeSkill.class,                     /* CHANGE_MP_USE */
        { "change_mp_skills", "change_mp_percent" },
//        Effect_SetVariable.class,                     /* SET_VARIABLE */ 
        { "var_name_1", "var_value_1", "var_name_2", "var_value_2", "var_name_3", "var_value_3" },
//        Effect_HOT.class,                             /* HOT */
        { "hot_seconds", "hot_tick_time", "hot_value", "hot_percent_of_damage" },
//        Effect_HOT.class,                             /* DOT */ 
        { "dot_seconds", "dot_tick_time", "dot_value", "dot_percent_of_damage" },
//        Effect_Shield.class,                          /* MP_SHIELD */  
        { "mp_shield_total", "mp_shield_dpercent", "mp_shield_percent", "mp_shield_price" },
//        Effect_Vampire.class,                         /* VAMPIRE_ON_HIT */ 
        { "vampire_percent", "vampire_range" },
//        Effect_ChangeSkill.class,                     /* CHANGE_CD_TIME */ 
        { "change_cd_skills", "change_cd_percent" },
//        Effect_ChangeSkill.class,                     /* CHANGE_DISTANCE */ 
        { "change_distance_skills", "change_distance_percent" },
//        Effect_ChangeSkill.class,                     /* CHANGE_ACT_TIME */ 
        { "change_acttime_skills", "change_acttime_percent" },
//        Effect_ChangeSkill.class,                     /* CHANGE_RANGE */
        { "change_range_skills", "change_range_percent" },
//        Effect_CureOnHit.class                        /* CURE_TARGET_IGNORE_MAX */
        { "add_hpim_rate", "add_hpim_value", "add_hpim_percent_of_max", "add_hpim_percent_of_damage" },
//        Effect_MultiAdd.class,                        /* CHANGE_MAGIC_ARMOR */
        { "change_magic_armor_value", "change_magic_armor_percent" },
//        Effect_MultiAdd.class,                        /* IGNORE_MAGIC_ARMOR */
        { "ignore_magic_armor_value", "ignore_magic_armor_percent" },
//        Effect_MultiAdd.class,                        /* REDUCE_PHYSICIAL_DAMAGE */
        { "reduce_physical_damage_value", "reduce_physical_damage_percent" },
//        Effect_MultiAdd.class,                        /* REDUCE_MAGIC_DAMAGE */
        { "reduce_magic_damage_value", "reduce_magic_damage_percent" },
//      Effect_PercentAdd.class,                        /* CHANGE_MAGIC_HIT */
        { "change_magic_hit" },
//        Effect_PercentAdd.class,                      /* CHANGE_MAGIC_DODGE */
        { "change_magic_dodge" },
//        Effect_CannotMove.class                       /* CANNOT_MOVE */
        { },
//        Effect_HOT.class,                             /* MPHOT */
        { "mphot_seconds", "mphot_tick_time", "mphot_value", "mphot_percent_of_damage" },
//        Effect_HOT.class,                             /* MPDOT */ 
        { "mpdot_seconds", "mpdot_tick_time", "mpdot_value", "mpdot_percent_of_damage" },
//        Effect_PercentAdd.class,                      /* DISPEL_BUFF */
        { "dispel_buff_rate" },
//        Effect_PercentAdd.class,                      /* DISPEL_ALL_BUFF */
        { "dispel_all_buff_rate" },
//        Effect_PercentAdd.class,                      /* DISPEL_DEBUFF */
        { "dispel_debuff_rate" },
//        Effect_PercentAdd.class,                      /* DISPEL_ALL_DEBUFF */
        { "dispel_all_debuff_rate" },
//        Effect_MultiAdd.class,                        /* CHANGE_STA */
        { "change_sta_value", "change_sta_rate" },
//        Effect_MultiAdd.class,                        /* CHANGE_AGI */
        { "change_agi_value", "change_agi_rate" },
//        Effect_MultiAdd.class,                        /* CHANGE_STR */
        { "change_str_value", "change_str_rate" },
//        Effect_MultiAdd.class,                        /* CHANGE_INT */
        { "change_int_value", "change_int_rate" },
//        Effect_HPActiveBuff.class,                    /* HP_ACTIVE_BUFF */
        { "hp_buff_rate", "hp_buff_id", "hp_buff_level" },
//        Effect_CritActiveBuff.class,                  /* CRIT_ACTIVE_BUFF */
        { "crit_buff_rate", "crit_buff_id", "crit_buff_level" },
//        Effect_CritActiveBuff.class,                  /* CRITED_ACTIVE_BUFF */
        { "crited_buff_rate", "crited_buff_id", "crited_buff_level" },
//        Effect_FearOnHit.class,                       /* DUMB_ON_HIT */ 
        { "dumb_rate", "dumb_rate_var", "dumb_time" },
//        Effect_PercentAdd.class,                      /* IMMUNE_FEAR */
        { "immune_fear_rate" },
//        Effect_PercentAdd.class,                      /* IMMUNE_DUMB */
        { "immune_dumb_rate" },
//        Effect_PercentAdd.class,                      /* IMMUNE_PARALYZE */
        { "immune_paralyze_rate" },
//        Effect_PercentAdd.class,                      /* IMMUNE_STAY */
        { "immune_stay_rate" },
//        Effect_MultiAdd.class,                        /* CHANGE_BASIC_MAGIC_AP */ 
        { "change_basic_magic_ap_value", "change_basic_magic_ap_rate" },
//        Effect_MultiAdd.class,                        /* CHANGE_BASIC_HP */ 
        { "change_basic_hp_value", "change_basic_hp_rate" },
//        Effect_MultiAdd.class,                        /* CHANGE_BASIC_MP */ 
        { "change_basic_mp_value", "change_basic_mp_rate" },
//        Effect_FixValueAdd.class,                     /* LIMIT_EFFECT_TIMES */
        { "max_effect_times" },
//        Effect_MultiAdd.class,                        /* CHANGE_MAGIC_HEAL */
        { "magic_heal_value", "magic_heal_rate" },
//        Effect_LimitSkill.class,                      /* LIMIT_SKILL */
        { "limit_skills" },
//        Effect_MultiAdd.class,                        /* CHANGE_BATTLE_PHYICAL_AP */
        { "change_b_physical_ap_value", "change_b_physical_ap_percent" },
//        Effect_MultiAdd.class,                        /* CHANGE_BATTLE_MAGIC_AP */
        { "change_b_magic_ap_value", "change_b_magic_ap_percent" },
//        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_WEAPON_ATK */
        { "change_b_weapon_atk_value" },
//        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_WEAPON_MATK */ 
        { "change_b_weapon_matk_value" },
//        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_PHYSICAL_HIT */
        { "change_b_physical_hit" },
//        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_PHYSICAL_CRIT */
        { "change_b_physical_crit" },
//        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_PHYSICAL_DODGE */
        { "change_b_physical_dodge" },
//        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_MAGIC_CRIT */
        { "change_b_magic_crit" },
//      Effect_PercentAdd.class,                        /* CHANGE_BATTLE_MAGIC_HIT */
        { "change_b_magic_hit" },
//        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_MAGIC_DODGE */
        { "change_b_magic_dodge" },
//        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_PHYSICAL_CRITED */
        { "change_b_physical_crited" },
//        Effect_PercentAdd.class,                      /* CHANGE_BATTLE_MAGIC_CRITED */
        { "change_b_magic_crited" },
//        Effect_MultiAdd.class,                        /* CHANGE_BATTLE_ARMOR */
        { "change_b_armor_value", "change_b_armor_percent" },
//        Effect_MultiAdd.class,                        /* CHANGE_BATTLE_MAGIC_ARMOR */
        { "change_b_magic_armor_value", "change_b_magic_armor_percent" },
//        Effect_PercentAdd.class,                      /* CHANGE_EXP_RATE */
        { "change_exp_rate" },
//        Effect_PercentAdd.class,                      /* CHANGE_MONEY_RATE */
        { "change_money_rate" },
//        Effect_FixValueAdd.class,                     /* CHANGE_PHYSICAL_HIT_RATE */ 
        { "change_physical_hit_rate" },
//        Effect_FixValueAdd.class,                     /* CHANGE_PHYSICAL_CRIT_RATE */ 
        { "change_physical_crit_rate" },
//        Effect_FixValueAdd.class,                     /* CHANGE_PHYSICAL_DODGE_RATE */ 
        { "change_physical_dodge_rate" },
//        Effect_FixValueAdd.class,                     /* CHANGE_MAGIC_CRIT_RATE */ 
        { "change_magic_hit_rate" },
//        Effect_FixValueAdd.class,                     /* CHANGE_MAGIC_HIT_RATE */ 
        { "change_magic_crit_rate" },
//        Effect_FixValueAdd.class,                     /* CHANGE_MAGIC_DODGE_RATE */ 
        { "change_magic_dodge_rate" },
//        Effect_PercentAdd.class,                      /* CHANGE_HORSE_EXP_RATE */
        { "change_horse_exp_rate" },
//        Effect_PercentAdd.class,                      /* INTERRUPT */
        { "interrupt_rate" },
//        Effect_AddBuff.class,                         /* ADD_DEBUFF_ON_HITED */  
        { "add_debuff_hited_rate", "add_debuff_hited_rate_var", "add_debuff_hited_id", "add_debuff_hited_level" },
//        Effect_AddBuff.class,                         /* ADD_BUFF_ON_HITED */ 
        { "add_buff_hited_rate", "add_buff_hited_rate_var", "add_buff_hited_id", "add_buff_hited_level" },
//        Effect_ChangeParam.class,                     /* CHANGE_PARAM */ 
        { "change_param_1", "change_param_value_1", "change_param_percent_1", 
          "change_param_2", "change_param_value_2", "change_param_percent_2", 
          "change_param_3", "change_param_value_3", "change_param_percent_3", 
          "change_param_4", "change_param_value_4", "change_param_percent_4", 
          "change_param_5", "change_param_value_5", "change_param_percent_5", 
          "change_param_6", "change_param_value_6", "change_param_percent_6", 
          "change_param_7", "change_param_value_7", "change_param_percent_7", 
          "change_param_8", "change_param_value_8", "change_param_percent_8", 
          "change_param_9", "change_param_value_9", "change_param_percent_9", 
          "change_param_10", "change_param_value_10", "change_param_percent_10", },
//        Effect_PercentAdd.class,                      /* CHANGE_THREAT_TOTAL */
        { "change_threat_total_percent" },
//        Effect_FirstThreatTemp.class,                 /* FIRST_THREAT_TEMP */
        { "first_threat_temp_keeptime" },
//        Effect_CannotMove.class,                      /* TRANSPORT_TO_ME */
        { },
//        Effect_Transport.class,                       /* TRANSPORT_TO_POS */
        { "transport_pos" },
//        Effect_CannotMove.class,                      /* REMOVE_ON_BATTLE_END */
        { },
//        Effect_SlowOnHited.class,                     /* SLOW_ON_HITED */
        { "slow_rate", "slow_level", "slow_time" },
//        Effect_PercentAdd.class,                      /* IMMUNE_BREAKATTACK */
        { "immune_break_rate" },
//      Effect_PercentAdd.class,                        /* ADD_SPEED */ 
        { "add_speed_percent" },
//      Effect_PercentAdd.class,                      /* DOUBLE_HEAL_ON_HIT */  
        { "double_heal_rate" },
//      Effect_LimitMap.class,                      /* LIMIT_MAP */  
        { "limit_map" },
//      Effect_PercentAdd.class,                        /* ADD_NATUALENHANCE */ 
        { "add_natual_percent" },
//      Effect_PercentAdd.class,                        /* ADD_JEWELENHANCE */ 
        { "add_jewel_percent" },
//      Effect_PercentAdd.class,                        /* ADD_STARENHANCE */ 
        { "add_star_percent" },
//      Effect_PercentAdd.class,                        /* ADD_STARENHANCE */ 
        { "hp_seconds", "hp_tick_time", "maxhppercent" },
        { "mp_seconds", "mp_tick_time", "maxmppercent" },
    };
    
    /**
     * 设置级别数量
     */
    public abstract void setLevelCount(int max);
    /**
     * 取得效果类型ID
     */
    public abstract int getType();
    
    /**
     * 取得效果名称
     */
    public String getTypeName() {
        return TYPE_NAMES[getType()][0];
    }
    
    /**
     * 取得效果简称
     */
    public String getShortName() {
        return TYPE_NAMES[getType()][1];
    }

    /**
     * 取得参数个数
     */
    public abstract int getParamCount();
    /**
     * 取得参数的名字
     */
    public abstract String getParamName(int index);
    /**
     * 取得参数的类型。
     * @return 可能是Integer, Float或String
     */
    public abstract Class getParamClass(int index);
    /**
     * 取得某个参数各级别的参数值
     * @return 可能是int[], float[]或String[]
     */
    public abstract Object getParam(int index);
    
    /**
     * 克隆。
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 创建指定类型的效果对象
     * @param type
     * @param levelCount
     * @return
     * @throws Exception
     */
    public static EffectConfig create(int type, int levelCount) throws Exception {
        Constructor c = TYPE_CLASSES[type].getConstructors()[0];
        EffectConfig ret = (EffectConfig)c.newInstance(type);
        ret.setLevelCount(levelCount); 
        return ret;
    }
    
    /**
     * 从XML中载入效果。
     * @param elem
     * @return
     */
    public static EffectConfig load(Element elem, int maxLevel) throws Exception {
        int type = Integer.parseInt(elem.getAttributeValue("type"));
        EffectConfig ret = create(type, maxLevel);
        List list = elem.getChildren("param");
        if (list.size() != ret.getParamCount()) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < ret.getParamCount(); i++) {
            Element paramElem = (Element)list.get(i);
            Object param = ret.getParam(i);
            if (param instanceof int[]) {
                int[] arr = Utils.stringToIntArray(paramElem.getText(), ';');
                System.arraycopy(arr, 0, (int[])param, 0, maxLevel);
            } else if (param instanceof float[]) {
                float[] arr = Utils.stringToFloatArray(paramElem.getText(), ';');
                System.arraycopy(arr, 0, (float[])param, 0, maxLevel);
            } else if (param instanceof String[]) {
                String[] arr = Utils.stringToStringArray(paramElem.getText(), ';');
                System.arraycopy(arr, 0, (String[])param, 0, maxLevel);
            } else if (param instanceof ParamIndicator[]) {
                String[] arr = Utils.stringToStringArray(paramElem.getText(), ';');
                ParamIndicator[] arr2 = (ParamIndicator[])param;
                for (int j = 0; j < maxLevel; j++) {
                    arr2[j] = new ParamIndicator();
                    arr2[j].load(arr[j]);
                }
            } else if (param instanceof int[][]) {
                String[] arr = Utils.stringToStringArray(paramElem.getText(), ';');
                int[][] arr2 = (int[][])param;
                for (int j = 0; j < maxLevel; j++) {
                    arr2[j] = Utils.stringToIntArray(arr[j], ',');
                }
            }
        }
        return ret;
    }
    
    /**
     * 把效果保存到XML中。
     * @return
     */
    public Element save() {
        Element ret = new Element("effect");
        ret.addAttribute("type", String.valueOf(getType()));
        for (int i = 0; i < getParamCount(); i++) {
            Object param = getParam(i);
            Element paramElem = new Element("param");
            if (param instanceof int[]) {
                int[] ia = (int[])param;
                paramElem.setText(Utils.intArrayToString(ia, ';'));
            } else if (param instanceof float[]) {
                float[] fa = (float[])param;
                paramElem.setText(Utils.floatArrayToString(fa, ';'));
            } else if (param instanceof String[]) {
                String[] sa = (String[])param;
                paramElem.setText(Utils.stringArrayToString(sa, ';'));
            } else if (param instanceof ParamIndicator[]) {
                ParamIndicator[] arr2 = (ParamIndicator[])param;
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < arr2.length; j++) {
                    if (j > 0) {
                        sb.append(';');
                    }
                    sb.append(arr2[j].toString());
                }
                paramElem.setText(sb.toString());
            } else if (param instanceof int[][]) {
                int[][] arr2 = (int[][])param;
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < arr2.length; j++) {
                    if (j > 0) {
                        sb.append(';');
                    }
                    sb.append(Utils.intArrayToString(arr2[j], ','));
                }
                paramElem.setText(sb.toString());
            }
            ret.addContent(paramElem);
        }
        return ret;
    }
}
