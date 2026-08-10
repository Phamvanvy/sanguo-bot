package com.pip.sanguo.data.equipment;

import java.util.Hashtable;
import java.util.Random;

import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.util.Constants;

/**
 * 计算装备属性
 * 
 * @author Joy
 */
public class AttributeCalculator {
    /*
     * 可修改的属性
     */
    public static final int ATTRIBUTE_STR = 0;
    public static final int ATTRIBUTE_AGI = 1;
    public static final int ATTRIBUTE_STA = 2;
    public static final int ATTRIBUTE_INT = 3;
    public static final int ATTRIBUTE_HP = 4;
    public static final int ATTRIBUTE_MP = 5;
    public static final int ATTRIBUTE_CRIT = 6;
    public static final int ATTRIBUTE_HIT = 7;
    public static final int ATTRIBUTE_DODGE = 8;
    public static final int ATTRIBUTE_MAGICDODGE = 9;
    public static final int ATTRIBUTE_ATTACKPOWER = 10;
    public static final int ATTRIBUTE_MAGICPOWER = 11;
    public static final int ATTRIBUTE_ARMOR = 12;
    public static final int ATTRIBUTE_MAGICARMOR = 13;
    public static final int ATTRIBUTE_HPRENEW = 14;
    public static final int ATTRIBUTE_MPRENEW = 15;
    public static final int ATTRIBUTE_SPEED = 16;
    public static final int ATTRIBUTE_ANTICRIT = 17;
    
    /*
     * 武器的攻击上限和下限是通过武器类型、武器等级和武器的附加攻击力属性计算得来，不能直接修改。
     */
    public static final int ATTRIBUTE_MINATTACK = 100;
    public static final int ATTRIBUTE_MAXATTACK = 101;
    
    /*
     * 可修改的属性。
     */
    public static final EquipmentAttribute[] ATTRIBUTES = {
        new EquipmentAttribute("str", "力量", "力量", 50.0f),
        new EquipmentAttribute("agi", "敏捷", "敏捷", 50.0f),
        new EquipmentAttribute("sta", "体力", "体力", 60.0f),
        new EquipmentAttribute("int", "智力", "智力", 50.0f),
        new EquipmentAttribute("hp", "生命", "生命", 3.5f),
        new EquipmentAttribute("mp", "精力", "精力", 8.0f),
        new EquipmentAttribute("crit", "暴击等级", "暴击", 50.0f),
        new EquipmentAttribute("hit", "命中等级", "命中", 50.0f),
        new EquipmentAttribute("dodge", "物理闪避等级", "物闪", 50.0f),
        new EquipmentAttribute("magicdodge", "法术闪避等级", "法闪", 50.0f),
        new EquipmentAttribute("attackpower", "物理攻击力", "物攻", 32.0f),
        new EquipmentAttribute("magicpower", "法术攻击力", "法攻", 50.0f),
        new EquipmentAttribute("armor", "护甲", "护甲", 7f),
        new EquipmentAttribute("magicarmor", "法术防御力", "法防", 50.0f),
        new EquipmentAttribute("hprenew", "5秒回血", "回血", 100.0f),
        new EquipmentAttribute("mprenew", "5秒回气", "回气", 100.0f),
        new EquipmentAttribute("speed", "坐骑速度", "速度", 100.0f),
        new EquipmentAttribute("anticrit", "免暴等级", "免暴", 50.0f),
    };
    
    /**
     * 各种武器的攻击上下限和平均攻击力相比的比率。
     */
    public static final float[][] WEAPON_RANGE = {
        { 0.8f, 1.2f },//枪
        { 0.75f, 1.25f },//斧
        { 0.65f, 1.35f },//长杆刀
        { 0.85f, 1.15f },//刀
        { 0.80f, 1.0f },//剑
        { 0.95f, 1.05f },//弓
        { 0.3f, 0.6f },//扇
    };
    public static final float[][] WEAPON_MRANGE = {
        { 0.0f, 0.0f },//枪
        { 0.0f, 0.0f },//斧
        { 0.0f, 0.0f },//长杆刀
        { 0.0f, 0.0f },//刀
        { 1.0f, 1.0f },//剑
        { 0.0f, 0.0f },//弓
        { 0.9f, 0.9f },//扇
    };

    /** 每装备级别的标准价值点数 */
    private static final float LEVEL_POINT = 720.0f;
    /** 宝石：每级别的价值点数 */
    private static final float JEWEL_LEVEL_POINT = 36.0f;
    /** 各品质的标准加成系数 */
    public static final float[] QUALITY_ADDITION = {
        0,
        0.2f,
        0.4f,
        0.5f,
        0.7f,
        0.1f
    };
    /**
     * 各装备部位占总价值的比例
     */
    public static final float[] RATE_PLACE = {
        0.25f,//武器
        0.25f,
        0.25f,
        0.25f,
        0.25f,
        0.25f,
        0.25f,
        0.25f,
        0.48f,//防具
        0.16f,
        0.14f,
        0.11f,
        0.07f,
        0.11f,
        0.27f,//首饰
        0.045f,
        0.045f,
        0.09f,
        0.09f,
        0.30f,//坐骑
        0.05f,
        0.05f,
        0.05f,
        0.05f,
        0.05f,
        0.05f,
        0.05f,
    };
    
    /**
     * 计算一件装备的价值点数。计算公式为：
     * 每1装备等级的价值基准为720，乘上对应部位的比率，再乘以装备品质系数和附加品质系数的和。
     * @param level 等级（不是可装备等级）
     * @param quality 品质
     * @param place 部位
     * @param extraQuality 附加品质系数
     * @return
     */
    public static final float getValue(int level, int quality, int place, float extraQuality) {
        return level * LEVEL_POINT * RATE_PLACE[place] * (QUALITY_ADDITION[quality] + extraQuality);
    }
    
    /**
     * 查找指定属性的索引。
     * @param id
     * @return
     */
    public static int findIndexOfAttribute(String id) {
        for (int i = 0; i < ATTRIBUTES.length; i++) {
            if (ATTRIBUTES[i].id.equals(id)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 生成一件装备的附加属性表，但不包括：基础攻击力，基础法术攻击力，基础护甲。在调用本方法前，必须
     * 先设置装备的以下属性：物品等级、品质、部位、附加品质系数、前缀。
     */
    public static void generateAttributes(Equipment equ) {
        float value = getValue(equ.level, equ.quality, equ.place, equ.extraQuality);
        value -= equ.getBuffValue();         // 扣除特效价值
        float[] attrs = equ.prefix.generateAttributes(value);
        System.arraycopy(attrs, 0, equ.appendAttributes, 0, ATTRIBUTES.length);
    }
    
    /**
     * 根据装备属性重新计算装备的附加品质参数。
     * @param equ
     */
    public static void calculateExtraQuality(Equipment equ) {
        float totalValue = 0.0f;
        for (int i = 0; i < equ.appendAttributes.length; i++) {
            totalValue += equ.appendAttributes[i] * ATTRIBUTES[i].value;
        }
        totalValue += equ.getBuffValue();    // 加上特效价值
        float totalQuality = totalValue / (equ.level * LEVEL_POINT * RATE_PLACE[equ.place]);
        equ.extraQuality = totalQuality - QUALITY_ADDITION[equ.quality];
    }
                 
    /**
     * 计算装备基本攻击力。
     * 装备等级*6*部位比重
     * @param level 装备等级
     * @param place 部位
     * @return
     */
    public static float getBaseAttack(int level, int place){
        // 只有武器有基础攻击力
        if (place < Equipment.PROTECTOR_UNDEFINE) {
            return level * 6 * RATE_PLACE[place];
        } else {
            return 0;
        }
    }
    
    /**
     * 计算装备基本法术攻击力。
     * 装备等级*6*部位比重*0.6
     * @param level 装备等级
     * @param place 部位
     * @return
     */
    public static float getBaseMagicAttack(int level, int place){
        // 只有武器有基础法术攻击力
        if (place < Equipment.PROTECTOR_UNDEFINE) {
            float base = level * 6 * RATE_PLACE[place] * 0.6f;
            return base * WEAPON_MRANGE[place - Equipment.WEAPON_UNDEFINE - 1][0];
        } else {
            return 0;
        }
    }
    
    /**
     * 计算武器的攻击力下限。
     * @param level 物品等级
     * @param place 部位（武器类型）
     * @param addAttack 附加物理攻击力
     * @return
     */
    public static float getMinAttack(int level, int place, float addAttack) {
        float attack = getBaseAttack(level, place) + addAttack;
        return attack * WEAPON_RANGE[place - Equipment.WEAPON_UNDEFINE - 1][0];
    }

    /**
     * 计算武器的攻击力上限。
     * @param level 物品等级
     * @param place 部位（武器类型）
     * @param addAttack 附加物理攻击力
     * @return
     */
    public static float getMaxAttack(int level, int place, float addAttack) {
        float attack = getBaseAttack(level, place) + addAttack;
        return attack * WEAPON_RANGE[place - Equipment.WEAPON_UNDEFINE - 1][1];
    }
    
    /**
     * 计算装备基本防御力
     * 物品等级*144*部位比重
     * @param level 等级
     * @param place 部位
     * @return
     */
    public static float getBaseArmor(int level, int place) {
        // 护甲和坐骑装备有护甲
        int type = Equipment.getType(place);
        if (type == Equipment.EQUI_TYPE_PROTECTOR) {
            return (900 + 100 * level) * RATE_PLACE[place] * 0.35f;
        } else {
            return 0;
        }
    }
    
    /**
     * 计算装备基本法术防御力
     * 物品等级*144*部位比重
     * @param level 等级
     * @param place 部位
     * @return
     */
    public static float getBaseMagicArmor(int level, int place) {
        // 护甲和坐骑装备有法防
        int type = Equipment.getType(place);
        if (type == Equipment.EQUI_TYPE_PROTECTOR) {
            return (900 + 100 * level) * RATE_PLACE[place] * 0.35f / 200;
        } else {
            return 0;
        }
    }
    
    /**
     * 计算装备价格（买入价格，卖出价格应为买入价格的一半）
     * 物品等级*12*品质系数*部位比重*40
     * @param level 等级
     * @param quality 品质
     * @param place 部位
     * @return
     */
    public static int getPrice(int level, int quality, int place) {
        return (int)(level * 12 * (1 + QUALITY_ADDITION[quality]) * RATE_PLACE[place] * 1.5);
    }
    
    /**
     * 计算装备耐久度
     * 物品价格/10+物品等级/2*300
     * @param level
     * @param quality 品质
     * @param place 部位
     * @return
     */
    public static int getDurability(int level, int quality, int place) {
        // 首饰没有耐久
        if (Equipment.getType(place) == Equipment.EQUI_TYPE_JEWELRY) {
            return 0;
        }
        int price = getPrice(level, quality, place);
        return price / 4 + level * 100 / 2;
    }
    
    /**
     * 计算一个宝石能够附加的属性点数。
     * @param level 宝石物品级别
     * @param percent 物品价值比例
     * @param type 附加属性类型
     * @return 附加属性点数
     */
    public static int calcJewelAttr(int level, float percent, int type, boolean useRound) {
        float value = level * JEWEL_LEVEL_POINT * percent;
        float point = value / ATTRIBUTES[type].value;
        if (useRound) {
            return Math.round(point);
        } else {
            return (int)point;
        }
    }
}
