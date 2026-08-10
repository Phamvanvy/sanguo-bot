package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：被命中后减速攻击者。
 * @author lighthu
 */
public class Effect_SlowOnHited extends EffectConfig {
    private int type;
    private float[] rate = new float[0];
    private int[] slowLevel = new int[0];
    private int[] slowTime = new int[0];
    
    public Effect_SlowOnHited(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        rate = Utils.realloc(rate, max);
        slowLevel = Utils.realloc(slowLevel, max);
        slowTime = Utils.realloc(slowTime, max);
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
        return 3;
    }
    
    /**
     * 取得参数的名字
     */
    public String getParamName(int index) {
        switch (index) {
        case 0:
            return "概率%";
        case 1:
            return "减速级别";
        case 2:
            return "持续时间(毫秒)";
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
            return slowLevel;
        case 2:
            return slowTime;
        }
        throw new IllegalArgumentException();
    }
}
