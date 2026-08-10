package com.pip.sanguo.editor.ai;

import org.jdom.Element;

/**
 * 怪物恐惧逃跑规则。
 * @author lighthu
 */
public class EscapeRuleConfig extends AIRuleConfig {
    /**
     * 逃跑血量百分比0-100。
     */
    public int hp;
    /**
     * 逃跑时间（毫秒）
     */
    public int duration;
    
    public String toString() {
        return "血量少于" + hp + "%时逃跑，持续" + duration + "毫秒";
    }

    protected String getRuleType() {
        return "escape";
    }
    
    protected Class[] getParamClass() {
        return new Class[] { Integer.class, Integer.class };
    }
    
    protected String[] getParamName() {
        return new String[] { "hp", "duration" };
    }
    
    protected String[] getParamDesc() {
        return new String[] { "血量(%)", "逃跑时间(毫秒)" };
    }
    
    protected Object getParamValue(int index) {
        switch (index) {
        case 0:
            return String.valueOf(hp);
        case 1:
            return String.valueOf(duration);
        }
        return null;
    }
    
    protected void setParamValue(int index, Object newValue) {
        switch (index) {
        case 0:
            try {
                hp = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 1:
            try {
                duration = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        }
    }
    
    public AIRuleConfig duplicate() {
        EscapeRuleConfig ret = new EscapeRuleConfig();
        ret.hp = hp;
        ret.duration = duration;
        return ret;
    }
}
