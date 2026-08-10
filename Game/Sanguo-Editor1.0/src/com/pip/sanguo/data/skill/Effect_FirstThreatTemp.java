package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：临时提升为第一仇恨。
 * @author lighthu
 */
public class Effect_FirstThreatTemp extends EffectConfig {
    private int type;
    private int[] keepTime = new int[0];
    
    public Effect_FirstThreatTemp(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        keepTime = Utils.realloc(keepTime, max);
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
        return "持续时间(毫秒)";
    }
    
    /**
     * 取得参数的类型。
     * @return 可能是Integer, Float或String
     */
    public Class getParamClass(int index) {
        return Integer.class;
    }
    
    /**
     * 取得某个参数各级别的参数值
     * @return 可能是int[], float[]或String[]
     */
    public Object getParam(int index) {
        return keepTime;
    }
}
