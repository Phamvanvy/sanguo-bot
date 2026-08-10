package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：一定几率反弹伤害，可反弹固定伤害，或反弹受到伤害的百分比。
 * @author lighthu
 */
public class Effect_Transport extends EffectConfig {
    private int type;
    private int[][] pos = new int[0][];
    
    public Effect_Transport(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        pos = realloc(pos, max);
    }

    public static int[][] realloc(int[][] arr, int length) {
        int[][] ret = new int[length][];
        System.arraycopy(arr, 0, ret, 0, length > arr.length ? arr.length : length);
        for (int i = 0; i < ret.length; i++) {
            if (ret[i] == null) {
                ret[i] = new int[3];
            }
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
        return 1;
    }
    
    /**
     * 取得参数的名字
     */
    public String getParamName(int index) {
        return "位置";
    }
    
    /**
     * 取得参数的类型。
     * @return 可能是Integer, Float或String
     */
    public Class getParamClass(int index) {
        return int[].class;
    }
    
    /**
     * 取得某个参数各级别的参数值
     * @return 可能是int[], float[]或String[]
     */
    public Object getParam(int index) {
        return pos;
    }
}
