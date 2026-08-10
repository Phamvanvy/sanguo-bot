package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：按固定值提高或按比例提高。
 * @author lighthu
 */
public class Effect_MultiAdd extends EffectConfig {
    private int type;
    private int[] addValue = new int[0];
    private float[] addPercent = new float[0];
    
    public Effect_MultiAdd(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        addValue = Utils.realloc(addValue, max);
        addPercent = Utils.realloc(addPercent, max);
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
        if (index == 0) {
            return "数额";
        } else {
            return "比例%";
        }
    }
    
    /**
     * 取得参数的类型。
     * @return 可能是Integer, Float或String
     */
    public Class getParamClass(int index) {
        if (index == 0) {
            return Integer.class;
        } else {
            return Float.class;
        }
    }
    
    /**
     * 取得某个参数各级别的参数值
     * @return 可能是int[], float[]或String[]
     */
    public Object getParam(int index) {
        if (index == 0) {
            return addValue;
        } else {
            return addPercent;
        }
    }
}
