package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：命中后恐惧/麻痹/定身目标。
 * @author lighthu
 */
public class Effect_FearOnHit extends EffectConfig {
    private int type;
    private float[] rate = new float[0];
    private String[] rateVar = new String[0];
    private int[] fearTime = new int[0];
    
    public Effect_FearOnHit(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        rate = Utils.realloc(rate, max);
        rateVar = Utils.realloc(rateVar, max, "");
        fearTime = Utils.realloc(fearTime, max);
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
            return "概率变量";
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
            return String.class;
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
            return rateVar;
        case 2:
            return fearTime;
        }
        throw new IllegalArgumentException();
    }
}
