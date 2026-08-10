package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：按比例提高。
 * @author lighthu
 */
public class Effect_PercentAdd extends EffectConfig {
    private int type;
    private float[] addPercent = new float[0];
    
    public Effect_PercentAdd(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
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
        return 1;
    }
    
    /**
     * 取得参数的名字
     */
    public String getParamName(int index) {
        return "比例%";
    }
    
    /**
     * 取得参数的类型。
     * @return 可能是Integer, Float或String
     */
    public Class getParamClass(int index) {
        return Float.class;
    }
    
    /**
     * 取得某个参数各级别的参数值
     * @return 可能是int[], float[]或String[]
     */
    public Object getParam(int index) {
        return addPercent;
    }
}
