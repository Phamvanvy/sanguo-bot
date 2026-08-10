package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

public class Effect_HP extends EffectConfig {

    private int type;
    private int[] time = new int[0];
    private int[] tick = new int[0];
    private float[] percent = new float[0];
    
    public Effect_HP(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        time = Utils.realloc(time, max);
        tick = Utils.realloc(tick, max);
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
        return 3;
    }
    
    /**
     * 取得参数的名字
     */
    public String getParamName(int index) {
        switch (index) {
        case 0:
            return "持续时间(秒)";
        case 1:
            return "每跳间隔(秒)";
        case 2:
            return "每跳百分比%";
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
            return Integer.class;
        case 1:
            return Integer.class;
        case 2:
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
            return time;
        case 1:
            return tick;
        case 2:
            return percent;
        }
        throw new IllegalArgumentException();
    }

}
