package com.pip.sanguo.data.skill;

import com.pip.sanguo.editor.skill.ParamIndicator;
import com.pip.util.Utils;

/**
 * 效果：影响技能/BUFF参数，最多可以影响10个参数。
 * @author lighthu
 */
public class Effect_ChangeParam extends EffectConfig {
    private int type;
    private ParamIndicator[][] paramInds = new ParamIndicator[10][0];
    private float[][] value = new float[10][0];
    private float[][] percent = new float[10][0];
    
    public Effect_ChangeParam(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        for (int i = 0; i < 10; i++) {
            paramInds[i] = realloc(paramInds[i], max);
            value[i] = Utils.realloc(value[i], max);
            percent[i] = Utils.realloc(percent[i], max);
        }
    }
    
    public static ParamIndicator[] realloc(ParamIndicator[] arr, int length) {
        ParamIndicator[] ret = new ParamIndicator[length];
        System.arraycopy(arr, 0, ret, 0, length > arr.length ? arr.length : length);
        for (int i = arr.length; i < ret.length; i++) {
            ret[i] = new ParamIndicator();
        }
        return ret;
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
        return 30;
    }
    
    /**
     * 取得参数的名字
     */
    public String getParamName(int index) {
        int group = (index / 3) + 1;
        index = index % 3;
        switch (index) {
        case 0:
            return "参数" + group;
        case 1:
            return "数额" + group;
        case 2:
            return "百分比" + group;
        }
        throw new IllegalArgumentException();
    }
    
    /**
     * 取得参数的类型。
     * @return 可能是Integer, Float或String
     */
    public Class getParamClass(int index) {
        index = index % 3;
        switch (index) {
        case 0:
            return ParamIndicator.class;
        case 1:
            return Float.class;
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
        int group = index / 3;
        index = index % 3;
        switch (index) {
        case 0:
            return paramInds[group];
        case 1:
            return value[group];
        case 2:
            return percent[group];
        }
        throw new IllegalArgumentException();
    }
}
