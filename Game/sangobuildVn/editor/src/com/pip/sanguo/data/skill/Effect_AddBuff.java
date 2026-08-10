package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 技能命中后一定几率加BUFF/DEBUFF。
 * @author lighthu
 */
public class Effect_AddBuff extends EffectConfig {
    private int type;
    private float[] rate = new float[0];
    private String[] rateVar = new String[0];
    private int[] buffID = new int[0];
    private int[] buffLevel = new int[0];
    
    public Effect_AddBuff(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        rate = Utils.realloc(rate, max);
        rateVar = Utils.realloc(rateVar, max, "");
        buffID = Utils.realloc(buffID, max);
        buffLevel = Utils.realloc(buffLevel, max);
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
            return "概率变量";
        case 2:
            return "类型";
        case 3:
            return "级别";
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
            return BuffConfig.class;
        case 3:
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
            return buffID;
        case 3:
            return buffLevel;
        }
        throw new IllegalArgumentException();
    }
}
