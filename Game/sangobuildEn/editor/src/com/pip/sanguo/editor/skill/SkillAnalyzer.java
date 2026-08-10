package com.pip.sanguo.editor.skill;

import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.equipment.AttributeCalculator;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.EffectConfig;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 技能平衡分析工具。
 * @author lighthu
 */
public class SkillAnalyzer {
    private SkillConfig skill;
    
    public SkillAnalyzer(SkillConfig skill) {
        this.skill = skill;
    }
    
    /**
     * 取得技能分析结果参数的名字。
     */
    public String[] getParamNames() {
        if (skill.clazz == 4) {
            return null;
        }
        if (skill.type == SkillConfig.TYPE_ATTACK) {
            return new String[] { "级别", "玩家级别", "伤害", "可用次数", "效率", "总伤害", "杀人需要次数", "可杀怪数" };
        } else if (skill.type == SkillConfig.TYPE_AID) {
            return new String[] { "级别", "玩家级别", "治疗", "可用次数", "效率", "总治疗", "可治疗满血次数" };
        } else {
            return null;
        }
    }
    
    /**
     * 取得某个技能级别的指定参数。
     * @param level
     * @return
     */
    public float getParamValue(int skillLevel, int paramIndex) {
        int reqLevel = skill.requireLevel[skillLevel];
        float smp = skill.mp[skillLevel];
        if (smp < 0) {
            smp = (-smp) * reqLevel;
        }
        if (skill.type == SkillConfig.TYPE_ATTACK) {
            switch (paramIndex) {
            case 0:
                 return skillLevel + 1;
            case 1:
                return reqLevel;
            case 2: // 伤害
                return getSkillDamage(skillLevel);
            case 3: // 可用次数
                return getStandardMP(reqLevel, skill.clazz) / smp;
            case 4: // 效率
                return getSkillDamage(skillLevel) / smp;
            case 5: // 总伤害
                return getStandardMP(reqLevel, skill.clazz) * getSkillDamage(skillLevel) / smp;
            case 6: // 杀人需要次数
                return getStandardHP(reqLevel, 0) / getSkillDamage(skillLevel);
            case 7: // 可杀怪数
                return getStandardMP(reqLevel, skill.clazz) * getSkillDamage(skillLevel) / smp / getMonsterHP(reqLevel);
            }
        } else if (skill.type == SkillConfig.TYPE_AID) {
            switch (paramIndex) {
            case 0:
                 return skillLevel + 1;
            case 1:
                return reqLevel;
            case 2: // 治疗
                return getSkillHeal(skillLevel);
            case 3: // 可用次数
                return getStandardMP(reqLevel, skill.clazz) / smp;
            case 4: // 效率
                return getSkillHeal(skillLevel) / smp;
            case 5: // 总治疗
                return getStandardMP(reqLevel, skill.clazz) * getSkillHeal(skillLevel) / smp;
            case 6: // 可治疗满血次数
                return getStandardMP(reqLevel, skill.clazz) * getSkillHeal(skillLevel) / smp / getStandardHP(reqLevel, 0);
            }
        }
        return 0.0f;
    }
    
    private float getSkillDamage(int skillLevel) {
        float ret = 0;
        switch (skill.damageType) {
        case SkillConfig.DAMAGE_PHYSICAL:
            ret = getStandardAttack(skill.requireLevel[skillLevel], skill.clazz);
            break;
        case SkillConfig.DAMAGE_MAGIC:
            ret = getStandardMagicAttack(skill.requireLevel[skillLevel], skill.clazz);
            break;
        }
        for (EffectConfig eff : skill.effects.getAllEffects()) {
            switch (eff.getType()) {
            case EffectConfig.CHANGE_PHYICAL_AP:
            case EffectConfig.CHANGE_MAGIC_AP:
                ret += ((int[])eff.getParam(0))[skillLevel];
                ret *= 1.0f + ((float[])eff.getParam(1))[skillLevel] / 100.0f;
                break;
            case EffectConfig.APPEND_MAGIC_DAMAGE:
                ret += ((int[])eff.getParam(0))[skillLevel];
                break;
            }
        }
        for (EffectConfig eff : skill.effects.getAllEffects()) {
            switch (eff.getType()) {
            case EffectConfig.TWO_HIT_ON_HIT:
                ret *= 3;
                break;
            }
        }
        
        // 如果加BUFF，计算BUFF伤害
        for (EffectConfig eff : skill.effects.getAllEffects()) {
            switch (eff.getType()) {
            case EffectConfig.ADD_DEBUFF_ON_HIT: {
                float rate = ((float[])eff.getParam(0))[skillLevel];
                int buffid = ((int[])eff.getParam(2))[skillLevel];
                int bufflvl = ((int[])eff.getParam(3))[skillLevel];
                BuffConfig bc = (BuffConfig)EditorApplication.getProj().findObject(BuffConfig.class, buffid);
                ret += getBuffDamage(bc, bufflvl - 1) * rate / 100.0f;
                break;
            }
            }
        }
        return ret;
    }
    
    private float getBuffDamage(BuffConfig bc, int level) {
        float ret = 0;
        for (EffectConfig eff : bc.effects.getAllEffects()) {
            if (eff.getType() == EffectConfig.DOT) {
                ret += ((int[])eff.getParam(2))[level];
            }
        }
        return ret;
    }
    
    private float getSkillHeal(int skillLevel) {
        float ret = this.getStandardHeal(skill.requireLevel[skillLevel], skill.clazz);
        if (skill.targetType == SkillConfig.TARGET_AREA || skill.targetType == SkillConfig.TARGET_AROUND) {
            ret = ret * 2 / 3;
        }
        for (EffectConfig eff : skill.effects.getAllEffects()) {
            switch (eff.getType()) {
            case EffectConfig.CURE_TARGET:
                ret = ((int[])eff.getParam(0))[skillLevel];
                break;
            }
        }
        
        // 如果加BUFF，计算BUFF治疗
        for (EffectConfig eff : skill.effects.getAllEffects()) {
            switch (eff.getType()) {
            case EffectConfig.ADD_DEBUFF_ON_HIT: {
                float rate = ((float[])eff.getParam(0))[skillLevel];
                int buffid = ((int[])eff.getParam(2))[skillLevel];
                int bufflvl = ((int[])eff.getParam(3))[skillLevel];
                BuffConfig bc = (BuffConfig)EditorApplication.getProj().findObject(BuffConfig.class, buffid);
                ret += getBuffHeal(bc, bufflvl - 1) * rate / 100.0f;
                break;
            }
            }
        }
        return ret;
    }
    
    private float getBuffHeal(BuffConfig bc, int level) {
        float ret = 0;
        for (EffectConfig eff : bc.effects.getAllEffects()) {
            if (eff.getType() == EffectConfig.HOT) {
                ret += ((int[])eff.getParam(2))[level];
            }
        }
        return ret;
    }
    
    /*
     * 计算装备附加属性总数（全绿装）。
     */
    private int getEquipmentAttrCount(int level) {
        return (int)((level + 5) * 1.11 * 0.2 * 720 * 0.6 / 50);
    }
    
    /*
     * 计算某个级别玩家的标准力量属性（全绿装）。
     */
    private int getStandardSTR(int level, int clazz) {
        int base = NPCTemplate.getPlayerProperties(clazz, level)[0];
        int attrCount = (level - 1) * 2;        // 可加属性点
        int equAttrCount = getEquipmentAttrCount(level);  // 装备附加属性点
        switch (clazz) {
        case 0:
            // 假设武将的一半属性 加到力量
            return base + (attrCount + equAttrCount) / 2;
        case 1:
            // 假设刺客的20%属性加到力量
            return base + (attrCount + equAttrCount) * 2 / 10;
        case 2:
            // 假设谋士不加力量
            return base;
        case 3:
            // 假设方士不加力量
            return base;
        default:
            return 0;
        }
    }

    /*
     * 计算某个级别玩家的标准敏捷属性（全绿装）。
     */
    private int getStandardAGI(int level, int clazz) {
        int base = NPCTemplate.getPlayerProperties(clazz, level)[1];
        int attrCount = (level - 1) * 2;        // 可加属性点
        int equAttrCount = getEquipmentAttrCount(level);  // 装备附加属性点
        switch (clazz) {
        case 0:
            // 假设武将不加敏捷
            return base;
        case 1:
            // 假设刺客的40%属性加到敏捷
            return base + (attrCount + equAttrCount) * 4 / 10;
        case 2:
            // 假设谋士不加敏捷
            return base;
        case 3:
            // 假设方士不加敏捷
            return base;
        default:
            return 0;
        }
    }

    /*
     * 计算某个级别玩家的标准智力属性（全绿装）。
     */
    private int getStandardINT(int level, int clazz) {
        int base = NPCTemplate.getPlayerProperties(clazz, level)[3];
        int attrCount = (level - 1) * 2;        // 可加属性点
        int equAttrCount = getEquipmentAttrCount(level);  // 装备附加属性点
        switch (clazz) {
        case 0:
            // 假设武将不加智力
            return base;
        case 1:
            // 假设刺客不加智力
            return base;
        case 2:
            // 假设谋士的50%属性加到智力
            return base + (attrCount + equAttrCount) / 2;
        case 3:
            // 假设方士的50%属性加到智力
            return base + (attrCount + equAttrCount) / 2;
        default:
            return 0;
        }
    }

    /*
     * 计算某个级别玩家的标准体力属性（全绿装）。
     */
    private int getStandardSTA(int level, int clazz) {
        int base = NPCTemplate.getPlayerProperties(clazz, level)[2];
        int attrCount = (level - 1) * 2;        // 可加属性点
        int equAttrCount = getEquipmentAttrCount(level);  // 装备附加属性点
        switch (clazz) {
        case 0:
            // 假设武将的50%属性加到体力
            return base + (attrCount + equAttrCount) / 2;
        case 1:
            // 假设刺客的40%属性加到体力
            return base + (attrCount + equAttrCount) * 4 / 10;
        case 2:
            // 假设谋士的50%属性加到体力
            return base + (attrCount + equAttrCount) / 2;
        case 3:
            // 假设方士的50%属性加到体力
            return base + (attrCount + equAttrCount) / 2;
        default:
            return 0;
        }
    }

    /*
     * 计算某个级别玩家的标准生命上限（全绿装）。
     */
    private int getStandardHP(int level, int clazz) {
        int sta = getStandardSTA(level, clazz);
        if (clazz == 0) {
            return sta * 16 + level * 15;
        } else if (clazz == 1) {
            return sta * 13 + level * 15;
        } else if (clazz == 2) {
            return sta * 13 + level * 15;
        } else {
            return sta * 13 + level * 15;
        }
    }

    /*
     * 计算某个级别玩家的标准内力上限（全绿装）。
     */
    private int getStandardMP(int level, int clazz) {
        switch (clazz) {
        case 0:
        case 1:
            return getStandardSTR(level, clazz) * 6 + level * 15;
        case 2:
        case 3:
            return getStandardINT(level, clazz) * 6 + level * 15;
        default:
            return 0;
        }
    }
    
    /*
     * 计算某个级别的怪物血量。
     */
    private int getMonsterHP(int level) {
        return level * 24 + 30;
    }
    
    /*
     * 计算某个级别的玩家的物理攻击力（全绿装）。
     */
    private float getStandardAttack(int level, int clazz) {
        float patk = (int)AttributeCalculator.getBaseAttack(level + 5, Equipment.WEAPON_AXE);
        switch (clazz) {
        case 0:
            return getStandardSTR(level, clazz) + patk;
        case 1:
            return getStandardAGI(level, clazz) + getStandardSTR(level, clazz) / 4 + patk;
        case 2:
            return getStandardSTR(level, clazz) / 2 + patk;
        case 3:
            return getStandardSTR(level, clazz) / 2 + patk;
        default:
            return 0;
        }
    }
    
    /*
     * 计算某个级别的玩家的法术攻击力（全绿装）。
     */
    private float getStandardMagicAttack(int level, int clazz) {
        float matk = (int)AttributeCalculator.getBaseMagicAttack(level + 5, Equipment.WEAPON_AXE);
        switch (clazz) {
        case 0:
            return matk;
        case 1:
            return matk;
        case 2:
            return getStandardINT(level, clazz) + matk;
        case 3:
            return getStandardINT(level, clazz) + matk;
        default:
            return 0;
        }
    }
    
    /*
     * 计算某个级别玩家的法术治疗。
     */
    private float getStandardHeal(int level, int clazz) {
        float matk = (int)AttributeCalculator.getBaseMagicAttack(level + 5, Equipment.WEAPON_AXE);
        return matk;
    }
}
