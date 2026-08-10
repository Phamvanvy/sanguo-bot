package com.pip.sanguo.data;

import org.jdom.Element;

import com.pip.mapeditor.data.MapFile;
import com.pip.sanguo.data.equipment.AttributeCalculator;

/**
 * 坐骑类型。
 */
public class HorseType extends DataObject {
    public static final int ATTR_STR = 0;
    public static final int ATTR_AGI = 1;
    public static final int ATTR_STA = 2;
    public static final int ATTR_INT = 3;
    public static final int ATTR_SPEED = 4;
    public static final String[] ATTR_SHORTNAMES = {
        "str", "agi", "sta", "int", "speed"
    };
    public static final String[] ATTR_NAMES = {
        "力量", "敏捷", "耐力", "智力", "速度"
    };
    public static final float[] ATTR_VALUES = {
        50.0f, 50.0f, 60.0f, 50.0f, 100.0f
    };
    
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * 游戏内显示名称。
     */
    public String showName = "";
    /**
     * 召唤所需时间（毫秒）。
     */
    public int summonTime = 3000;
    /**
     * 食量（每1点饱食度能跑的时间，毫秒）。
     */
    public int eatingRate = 36000;
    /**
     * 最大技能数量。
     */
    public int maxSkill = 3;
    /**
     * 初始价值(1级)。
     */
    public float initValue;
    /**
     * 升级获得价值。
     */
    public float levelValue;
    /**
     * 合成价值。
     */
    public float mergeValue;
    /**
     * 每个属性值所占的权重。
     */
    public float[] attributePrior = new float[ATTR_NAMES.length];
    /**
     * 特殊技能，0表示没有。
     */
    public int specialSkill = 0;

    public HorseType(ProjectData owner) {
        this.owner = owner;
    }

    public int getID() {
        return id;
    }
    
    public String toString() {
        return id + ": " + title;
    }

    public boolean equals(Object o) {
        return this == o;
    }
    
    public void update(DataObject obj) {
        HorseType oo = (HorseType)obj;
        id = oo.id;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        
        showName = oo.showName;
        summonTime = oo.summonTime;
        eatingRate = oo.eatingRate;
        maxSkill = oo.maxSkill;
        initValue = oo.initValue;
        levelValue = oo.levelValue;
        mergeValue = oo.mergeValue;
        specialSkill = oo.specialSkill;
        System.arraycopy(oo.attributePrior, 0, attributePrior, 0, ATTR_NAMES.length);
    }
    
    public DataObject duplicate() {
        HorseType ret = new HorseType(owner);
        ret.update(this);
        return ret;
    }

    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
    
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        description = elem.getAttributeValue("description");
        categoryName = elem.getAttributeValue("category");
        if (categoryName == null) {
            categoryName = "";
        }
        
        showName = elem.getAttributeValue("showname");
        summonTime = Integer.parseInt(elem.getAttributeValue("summontime"));
        eatingRate = Integer.parseInt(elem.getAttributeValue("eatingrate"));
        maxSkill = Integer.parseInt(elem.getAttributeValue("maxskill"));
        initValue = Float.parseFloat(elem.getAttributeValue("initvalue"));
        levelValue = Float.parseFloat(elem.getAttributeValue("levelvalue"));
        try {
            mergeValue = Float.parseFloat(elem.getAttributeValue("mergevalue"));
        }catch (Exception e1) {
            mergeValue = (levelValue - 80) * 50;
        }
        for (int i = 0; i < ATTR_NAMES.length; i++) {
            try {
                attributePrior[i] = Float.parseFloat(elem.getAttributeValue(ATTR_SHORTNAMES[i]));
            } catch (Exception e) {
            }
        }
        try {
            specialSkill = Integer.parseInt(elem.getAttributeValue("specialskill"));
        } catch (Exception e) {
            specialSkill = 0;
        }
    }
    
    public Element save() {
        Element ret = new Element("horsetype");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("description", description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        ret.addAttribute("showname", showName);
        ret.addAttribute("summontime", String.valueOf(summonTime));
        ret.addAttribute("eatingrate", String.valueOf(eatingRate));
        ret.addAttribute("maxskill", String.valueOf(maxSkill));
        ret.addAttribute("initvalue", String.valueOf(initValue));
        ret.addAttribute("levelvalue", String.valueOf(levelValue));
        ret.addAttribute("mergevalue", String.valueOf(mergeValue));
        for (int i = 0; i < ATTR_NAMES.length; i++) {
            ret.addAttribute(ATTR_SHORTNAMES[i], String.valueOf(attributePrior[i]));
        }
        if (specialSkill != 0) {
            ret.addAttribute("specialskill", String.valueOf(specialSkill));
        }
        return ret;
    }
    
    public boolean depends(DataObject obj) {
        return false;
    }
    
    /**
     * 根据参数配置算出指定级别坐骑的属性。
     * @param level 级别
     * @return 附加属性表，顺序和ATTR_NAMES一样
     */
    public float[] generateAttributes(int level, int fixCount) {
        float value = initValue + (level - 1) * levelValue + fixCount * mergeValue;
        float[] ret= new float[ATTR_NAMES.length];
        float totalPrior = 0.0f;
        for (int i = 0; i < attributePrior.length; i++) {
            totalPrior += attributePrior[i];
        }
        
        if (totalPrior == 0.0f) {
            return ret;
        }
        for (int i = 0; i < attributePrior.length; i++) {
            float thisValue = value * attributePrior[i] / totalPrior;
            ret[i] = thisValue / ATTR_VALUES[i];
        }
        return ret;
    }
    
    /**
     * 根据参数配置算出指定级别坐骑的合成附加属性。
     * @param level 级别
     * @return 附加属性表，顺序和ATTR_NAMES一样
     */
    public float[] fixAttributes(int level, int fixCount) {
        float[] ret = generateAttributes(level, fixCount);
        float[] retNoFix = generateAttributes(level, 0);
        for (int i = 0; i < attributePrior.length; i++) {
            ret[i] = (int)ret[i] - (int)retNoFix[i];
        }
        return ret;
    }

    /**
     * 计算每个属性的百分比。
     */
    public float[] getPercents() {
        float totalPrior = 0.0f;
        for (int i = 0; i < attributePrior.length; i++) {
            totalPrior += attributePrior[i];
        }
        float[] ret = new float[attributePrior.length];
        if (totalPrior != 0.0f) {
            for (int i = 0; i < attributePrior.length; i++) {
                ret[i] = attributePrior[i] / totalPrior;
            }
        }
        return ret;
    }
    
    /**
     * 调整配置表，使指定属性在总属性中的比例符合指定比例要求。
     * @param index
     * @param percent
     */
    public void setPriorByPercent(int index, float percent, boolean[] lockFlag) {
        // 计算锁定的列所占的百分比
        float[] pers = getPercents();
        float lockPer = 0.0f;
        for (int i = 0; i < pers.length; i++) {
            if (lockFlag[i]) {
                lockPer += pers[i];
            }
        }
        
        // 100%特殊处理
        if (percent + lockPer > 0.99999f) {
            for (int i = 0; i < attributePrior.length; i++) {
                if (!lockFlag[i] && i != index) {
                    attributePrior[index] += attributePrior[i];
                    attributePrior[i] = 0.0f;
                }
            }
            return;
        }
        
        // 重排新的权重值
        float unlockTotal = 1.0f - lockPer - pers[index];
        if (unlockTotal < 0.00001f) {
            // 加入除了拖动的剩下的都为0，则不允许拖动
            return;
        }
        for (int i = 0; i < pers.length; i++) {
            if (lockFlag[i]) {
                attributePrior[i] = 100.0f * pers[i];
            } else if (i == index) {
                attributePrior[i] = 100.0f * percent;
            } else {
                attributePrior[i] = 100.0f * pers[i] * (1.0f - lockPer - percent) / unlockTotal;
            }
        }
    }
}
