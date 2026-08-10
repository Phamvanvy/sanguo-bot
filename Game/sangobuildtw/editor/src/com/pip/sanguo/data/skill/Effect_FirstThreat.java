package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：命中后成为目标的第一仇恨。
 * @author lighthu
 */
public class Effect_FirstThreat extends EffectConfig {
    private int type;
    
    public Effect_FirstThreat(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
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
        return 0;
    }
    
    /**
     * 取得参数的名字
     */
    public String getParamName(int index) {
        throw new IllegalArgumentException();
    }
    
    /**
     * 取得参数的类型。
     * @return 可能是Integer, Float或String
     */
    public Class getParamClass(int index) {
        throw new IllegalArgumentException();
    }
    
    /**
     * 取得某个参数各级别的参数值
     * @return 可能是int[], float[]或String[]
     */
    public Object getParam(int index) {
        throw new IllegalArgumentException();
    }
}
