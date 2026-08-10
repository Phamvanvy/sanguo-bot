package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：减少技能消耗。
 * @author lighthu
 */
public class Effect_SetVariable extends EffectConfig {
    private int type;
    private String[] name1 = new String[0];
    private float[] value1 = new float[0];
    private String[] name2 = new String[0];
    private float[] value2 = new float[0];
    private String[] name3 = new String[0];
    private float[] value3 = new float[0];
    
    public Effect_SetVariable(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        name1 = Utils.realloc(name1, max, "");
        value1 = Utils.realloc(value1, max);
        name2 = Utils.realloc(name2, max, "");
        value2 = Utils.realloc(value2, max);
        name3 = Utils.realloc(name3, max, "");
        value3 = Utils.realloc(value3, max);
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
            return "变量1";
        case 1:
            return "值1";
        case 2:
            return "变量2";
        case 3:
            return "值2";
        case 4:
            return "变量3";
        case 5:
            return "值3";
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
            return String.class;
        case 1:
            return Float.class;
        case 2:
            return String.class;
        case 3:
            return Float.class;
        case 4:
            return String.class;
        case 5:
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
            return name1;
        case 1:
            return value1;
        case 2:
            return name2;
        case 3:
            return value2;
        case 4:
            return name3;
        case 5:
            return value3;
        }
        throw new IllegalArgumentException();
    }
}
