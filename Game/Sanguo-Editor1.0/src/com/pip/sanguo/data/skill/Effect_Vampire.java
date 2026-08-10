package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：被命中后为攻击者全队回血。
 * @author lighthu
 */
public class Effect_Vampire extends EffectConfig {
    private int type;
    private float[] percent = new float[0];
    private int[] range = new int[0];
    
    public Effect_Vampire(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        percent = Utils.realloc(percent, max);
        range = Utils.realloc(range, max);
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
        return 2;
    }
    
    /**
     * 取得参数的名字
     */
    public String getParamName(int index) {
        switch (index) {
        case 0:
            return "转换比例%";
        case 1:
            return "有效范围(码)";
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
            return percent;
        case 1:
            return range;
        }
        throw new IllegalArgumentException();
    }
}
