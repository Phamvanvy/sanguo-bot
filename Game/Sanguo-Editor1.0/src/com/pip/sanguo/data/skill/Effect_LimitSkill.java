package com.pip.sanguo.data.skill;

import com.pip.util.Utils;

/**
 * 效果：限制影响技能。
 * @author lighthu
 */
public class Effect_LimitSkill extends EffectConfig {
    private int type;
    private String[] affectSkills = new String[0];
    
    public Effect_LimitSkill(int t) {
        type = t;
    }
    
    /**
     * 设置级别数量
     */
    public void setLevelCount(int max) {
        affectSkills = Utils.realloc(affectSkills, max, "");
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
        return "技能表";
    }
    
    /**
     * 取得参数的类型。
     * @return 可能是Integer, Float或String
     */
    public Class getParamClass(int index) {
        return SkillConfig[].class;
    }
    
    /**
     * 取得某个参数各级别的参数值
     * @return 可能是int[], float[]或String[]
     */
    public Object getParam(int index) {
        return affectSkills;
    }
}
