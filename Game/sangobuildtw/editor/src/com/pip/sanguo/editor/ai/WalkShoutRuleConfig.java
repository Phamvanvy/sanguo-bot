 package com.pip.sanguo.editor.ai;

import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Element;

import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.SkillPropertyDescriptor;

/**
 * 走到某点以后汉化规则。
 * @author lighthu
 */
public class WalkShoutRuleConfig extends AIRuleConfig {
    /**
     * 场景ID。
     */
    public int mapID;
    /**
     * X位置。
     */
    public int x;
    /**
     * Y位置。
     */
    public int y;
    /**
     * 喊话内容。
     */
    public String message = "";
    /**
     * 喊话范围半径
     */
    public int messageDistance;
    /**
     * 喊话颜色
     */
    public int messageColor;
    /**
     * 喊话持续时间（毫秒）
     */
    public int messageTime;
    
    public String toString() {
        return "到达" + mapID + ":" + x + "," + y + "时喊话：" + message;
    }

    protected String getRuleType() {
        return "walkshout";
    }
    
    protected Class[] getParamClass() {
        return new Class[] { int[].class, String.class, Integer.class, java.awt.Color.class, Integer.class };
    }
    
    protected String[] getParamName() {
        return new String[] { "location", "message", "messagedistance", "messagecolor", "messagetime" };
    }
    
    protected String[] getParamDesc() {
        return new String[] { "目标位置", "喊话", "喊话范围(码)", "喊话颜色", "喊话持续时间(毫秒)" };
    }
    
    protected Object getParamValue(int index) {
        switch (index) {
        case 0:
            return new int[] { mapID, x, y };
        case 1:
            return message;
        case 2:
            return String.valueOf(messageDistance);
        case 3:
            return String.valueOf(messageColor);
        case 4:
            return String.valueOf(messageTime);
        }
        return null;
    }
    
    protected void setParamValue(int index, Object newValue) {
        switch (index) {
        case 0: {
            int[] v = (int[])newValue;
            mapID = v[0];
            x = v[1];
            y = v[2];
            break;
        }
        case 1:
            message = (String)newValue;
            break;
        case 2:
            try {
                messageDistance = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 3:
            try {
                messageColor = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 4:
            try {
                messageTime = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        }
    }

    public AIRuleConfig duplicate() {
        WalkShoutRuleConfig ret = new WalkShoutRuleConfig();
        ret.mapID = mapID;
        ret.x = x;
        ret.y = y;
        ret.message = message;
        ret.messageDistance = messageDistance;
        ret.messageColor = messageColor;
        ret.messageTime = messageTime;
        return ret;
    }
}
