package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：一定几率反弹伤害，可反弹固定伤害，或反弹受到伤害的百分比。
 * @author lighthu
 */
public class Effect_Bounce extends EffectConfig {
    private int type;
    private float[] rate = new float[0];
    private int[] damageType = new int[0];
    private int[] value = new int[0];
    private float[] percent = new float[0];
    
    public Effect_Bounce(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        rate = Utils.realloc(rate, max);
        damageType = Utils.realloc(damageType, max);
        value = Utils.realloc(value, max);
        percent = Utils.realloc(percent, max);
    }
    
    /**
     * 取得效果类型ID
     */
    public int getType() {
        return type;
    }
    
    /**
     * 取得参数个数
     */
    public int getParamCount() {
        return 4;
    }
    
    /**
     * 取得参数的名字
     */
    public String getParamName(int index) {
        switch (index) {
        case 0:
            return "概率%";
        case 1:
            return "伤害类型";
        case 2:
            return "固定值";
        case 3:
            return "占伤害比例%";
        }
        throw new IllegalArgumentException();
    }
    
    /**
     * 取得参数的类型。
     * @return 可能是Integer, Float或String
     */
    public Class getParamClass(int index) {
        switch (index) {
        case 0:
            return Float.class;
        case 1:
            return Integer.class;
        case 2:
            return Integer.class;
        case 3:
            return Float.class;
        }
        throw new IllegalArgumentException();
    }
    
    /**
     * 取得某个参数各级别的参数值
     * @return 可能是int[], float[]或String[]
     */
    public Object getParam(int index) {
        switch (index) {
        case 0:
            return rate;
        case 1:
            return damageType;
        case 2:
            return value;
        case 3:
            return percent;
        }
        throw new IllegalArgumentException();
    }
}
