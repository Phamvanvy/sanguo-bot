package com.pip.itimes.server.suit;


import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SuitEffect{
    private Suit suit;

    private int count;
    private String desc;

    private int type;		//效果类型
    private int value;
    private int way;		//生效方式
    private int bout;
    private int percent;		//生效几率

    private ConcurrentHashMap<Integer, Integer> skills = new ConcurrentHashMap<Integer, Integer>();

    private int skillParm1;
    private int skillParm2;
    private int skillPercent;
    private int skillBout;
    private int skillMpUse;

    public static final int EFFECT_TYPE_ADD_PHY_ACT = 0;		// 增加物理攻击
    public static final int EFFECT_TYPE_ADD_MGC_ACT = 1;		// 增加魔法攻击
    public static final int EFFECT_TYPE_ADD_PHYMGC_ACT = 2;		// 增加物魔攻击
    public static final int EFFECT_TYPE_ADD_PHY_DEF = 3;		// 增加物理防御
    public static final int EFFECT_TYPE_ADD_MGC_DEF = 4;		// 增加魔法防御
    public static final int EFFECT_TYPE_ADD_PHYMGC_DEF = 5;		// 增加物魔防御
    public static final int EFFECT_TYPE_CHANGE_SKILL = 6;		// 改变技能
    public static final int EFFECT_TYPE_LET_POSION = 7;			// 使中毒
    public static final int EFFECT_TYPE_LET_STONE = 8;			// 使石化
    public static final int EFFECT_TYPE_LET_FROST = 9;			// 使霜冻
    public static final int EFFECT_TYPE_LET_CONFUSE = 10;		// 使混乱
    public static final int EFFECT_TYPE_LET_SLEEP = 11;			// 使昏睡
    public static final int EFFECT_TYPE_LET_FAINT = 12;			// 使眩晕
    public static final int EFFECT_TYPE_LET_STOP = 13;			// 使停行
    public static final int EFFECT_TYPE_ANTI_PHY = 14;			// 使物反
    public static final int EFFECT_TYPE_ANTI_MGC = 15;			// 使魔反
    public static final int EFFECT_TYPE_SORB_PHY = 16;			// 使物吸
    public static final int EFFECT_TYPE_SORB_MGC = 17;			// 使魔吸
    public static final int EFFECT_TYPE_ADD_PET_CATCH = 18;		// 加抓宠
    public static final int EFFECT_TYPE_SHINE = 19;				// 压制效果
    public static final int EFFECT_TYPE_REDUCE_PRI_CRI = 20;	// 减物爆率
    public static final int EFFECT_TYPE_REDUCE_MRI_CRI = 21;	// 减魔爆率
    public static final int EFFECT_TYPE_REDUCE_PRI_MRI_CRI = 22;// 减物魔爆率
    public static final int EFFECT_TYPE_ADD_PRI_CRI = 23;		// 加物爆率
    public static final int EFFECT_TYPE_ADD_MRI_CRI = 24;		// 加魔爆率
    public static final int EFFECT_TYPE_ADD_PRI_MRI_CRI = 25;	// 加物魔爆率
    public static final int EFFECT_TYPE_REDUCE_PRI_CRI_DAMAGE = 26;		// 减少物理暴击伤害
    public static final int EFFECT_TYPE_REDUCE_MRI_CRI_DAMAGE = 27;		// 减少魔法暴击伤害
    public static final int EFFECT_TYPE_REDUCE_PRI_MRI_CRI_DAMAGE = 28;	// 减少物理和魔法暴击伤害
    public static final int EFFECT_TYPE_ADD_PRI_CRI_DAMAGE = 29;		// 增加物理暴击伤害
    public static final int EFFECT_TYPE_ADD_MRI_CRI_DAMAGE = 30;		// 增加魔法暴击伤害
    public static final int EFFECT_TYPE_ADD_PRI_MRI_CRI_DAMAGE = 31;	// 增加物理和魔法暴击伤害
    public static final int EFFECT_TYPE_ADD_STR = -8;			// 增加力量(可转换成32)
    public static final int EFFECT_TYPE_ADD_AGI = -9;			// 增加敏捷(可转换成33)
    public static final int EFFECT_TYPE_ADD_VIT = -10;			// 增加体力(可转换成34)
    public static final int EFFECT_TYPE_ADD_INTE = -11;			// 增加智力(可转换成35)
    public static final int EFFECT_TYPE_ADD_DIAMOND = 36;		// 新套装宝石加成效果
    
    
    public static final int EFFECT_WAY_SELF_ALL = 0;
    public static final int EFFECT_WAY_PET_ALL = 1;
    public static final int EFFECT_WAY_ENEMY_ALL = 2;
    public static final int EFFECT_WAY_SELF_ONE = 3;
    public static final int EFFECT_WAY_PET_ONE = 4;
    public static final int EFFECT_WAY_ENEMY_ONE = 5;
    
    // 需要特殊处理的type
    public static final int[] specialType = new int[] {32, 33, 34, 35};
    public static final int[] resultType = new int[] {-8, -9, -10, -11};

    public void setSuit(Suit suit){
        this.suit = suit;
    }

    public Suit getSuit(){
        return suit;
    }

    public void setCount(int count){
        this.count = count;
    }

    public int getCount(){
        return count;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public void setType(int type){
    	for (int i = 0; i < specialType.length; i++) {
    		if (type == specialType[i]) {
    			type = resultType[i];
    			break;
    		}
    	}
        this.type = type;
    }

    public int getType(){
        return type;
    }

    public void setValue(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public void setWay(int way){
        this.way = way;
    }

    public int getWay(){
        return way;
    }

    public void setBout(int bout){
        this.bout = bout;
    }

    public int getBout(){
        return bout;
    }

    public void setPercent(int percent){
        this.percent = percent;
    }

    public int getPercent(){
        return percent;
    }

    public void setSkillParm1(int skillParm1){
        this.skillParm1 = skillParm1;
    }

    public int getSkillParm1(){
        return skillParm1;
    }

    public void setSkillParm2(int skillParm2){
        this.skillParm2 = skillParm2;
    }

    public int getSkillParm2(){
        return skillParm2;
    }

    public void setSkillPercent(int skillPercent){
        this.skillPercent = skillPercent;
    }

    public int getSkillPercent(){
        return skillPercent;
    }

    public void setSkillBout(int skillBout){
        this.skillBout = skillBout;
    }

    public int getSkillBout(){
        return skillBout;
    }

    public void setSkillMpUse(int skillMpUse){
        this.skillMpUse = skillMpUse;
    }

    public int getSkillMpUse(){
        return skillMpUse;
    }

    public void addSkill(int skillId){
        skills.put(skillId, skillId);
    }

    public boolean hasEffectSkill(int skillId){
        if(skills.get(skillId) == null){
            return false;
        }else{
            return true;
        }
    }

    public void clearSkills(){
        skills.clear();
    }

    public String getDesc(){
        String result = suit.getName() + "-" + count + "件\n" + desc;

        return result;
    }
    //mengjie add
    public String getPointDesc(){
        String result = desc;

        return result;
    }
    public boolean equals(Object other){
        if(!(other instanceof SuitEffect)){
            return super.equals(other);

        }

        SuitEffect suit = (SuitEffect)other;

        if(this.type != suit.type){
            return false;
        }

        if(this.value != suit.value){
            return false;
        }

        if(this.way != suit.way){
            return false;
        }

        if(this.bout != suit.way){
            return false;
        }

        if(this.percent != suit.percent){
            return false;
        }

        if(this.skillParm1 != suit.skillParm1){
            return false;
        }

        if(this.skillParm2 != suit.skillParm2){
            return false;
        }

        if(this.skillPercent != suit.skillPercent){
            return false;
        }

        if(this.skillBout != suit.skillBout){
            return false;
        }

        if(this.skillMpUse != suit.skillMpUse){
            return false;
        }

        if(this.skills.size() != suit.skills.size()){
            return false;
        }

        Enumeration<Integer> emu = this.skills.elements();

        while(emu.hasMoreElements()){
            int skillId = emu.nextElement();

            if(suit.skills.get(skillId) == null){
                return false;
            }
        }

        return true;
    }
}
