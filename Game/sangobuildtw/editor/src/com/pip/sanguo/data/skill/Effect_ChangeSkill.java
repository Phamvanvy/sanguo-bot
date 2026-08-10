package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：修改技能参数。
 * @author lighthu
 */
public class Effect_ChangeSkill extends EffectConfig {
    private int type;
    private String[] affectSkills = new String[0];
    private float[] percent = new float[0];
    
    public Effect_ChangeSkill(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        affectSkills = Utils.realloc(affectSkills, max, "");
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
        return 2;
    }
    
    /**
     * 取得参数的名字
     */
    public String getParamName(int index) {
        if (index == 0) {
            return "影响技能";
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
            return SkillConfig[].class;
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
            return affectSkills;
        } else {
            return percent;
        }
    }
}
