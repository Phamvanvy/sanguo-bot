package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：命中后减速目标。
 * @author lighthu
 */
public class Effect_SlowOnHit extends EffectConfig {
    private int type;
    private float[] rate = new float[0];
    private String[] rateVar = new String[0];
    private int[] slowLevel = new int[0];
    private String[] slowLevelVar = new String[0];
    private int[] slowTime = new int[0];
    private String[] slowTimeVar = new String[0];
    
    public Effect_SlowOnHit(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        rate = Utils.realloc(rate, max);
        rateVar = Utils.realloc(rateVar, max, "");
        slowLevel = Utils.realloc(slowLevel, max);
        slowLevelVar = Utils.realloc(slowLevelVar, max, "");
        slowTime = Utils.realloc(slowTime, max);
        slowTimeVar = Utils.realloc(slowTimeVar, max, "");
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
        return 6;
    }
    
    /**
     * 取得参数的名字
     */
    public String getParamName(int index) {
        switch (index) {
        case 0:
            return "概率%";
        case 1:
            return "概率变量";
        case 2:
            return "减速级别";
        case 3:
            return "减速级别变量";
        case 4:
            return "持续时间(毫秒)";
        case 5:
            return "持续时间变量";
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
            return String.class;
        case 2:
            return Integer.class;
        case 3:
            return String.class;
        case 4:
            return Integer.class;
        case 5:
            return String.class;
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
            return rateVar;
        case 2:
            return slowLevel;
        case 3:
            return slowLevelVar;
        case 4:
            return slowTime;
        case 5:
            return slowTimeVar;
        }
        throw new IllegalArgumentException();
    }
}
