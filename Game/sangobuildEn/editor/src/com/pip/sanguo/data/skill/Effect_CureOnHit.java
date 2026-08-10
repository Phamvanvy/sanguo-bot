package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：命中后烧兰，回血，回蓝。
 * @author lighthu
 */
public class Effect_CureOnHit extends EffectConfig {
    private int type;
    private float[] rate = new float[0];
    private int[] value = new int[0];
    private float[] percent1 = new float[0];
    private float[] percent2 = new float[0];
    
    public Effect_CureOnHit(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        rate = Utils.realloc(rate, max);
        value = Utils.realloc(value, max);
        percent1 = Utils.realloc(percent1, max);
        percent2 = Utils.realloc(percent2, max);
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
            return "出现概率%";
        case 1:
            return "固定值";
        case 2:
            return "占总值比例%";
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
            return Float.class;
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
            return value;
        case 2:
            return percent1;
        case 3:
            return percent2;
        }
        throw new IllegalArgumentException();
    }
}
