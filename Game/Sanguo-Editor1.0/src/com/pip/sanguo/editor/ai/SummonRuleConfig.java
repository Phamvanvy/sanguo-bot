package com.pip.sanguo.editor.ai;

import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Element;

import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.SkillPropertyDescriptor;

/**
 * 召唤帮手规则。
 * @author lighthu
 */
public class SummonRuleConfig extends AIRuleConfig {
    /**
     * 触发血量百分比0-100
     */
    public int hp;
    /**
     * 平均召唤间隔（毫秒）
     */
    public int interval;
    /**
     * 召唤怪物，最多10个
     */
    public int[] monsters = new int[10];
    /**
     * 召唤怪物生存时间（毫秒），0表示不限制
     */
    public int liveTime;
    /**
     * 最多召唤次数
     */
    public int useTimes;
    /**
     * 召唤时喊话（空串表示不喊话）
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
        String ret = "血量少于" + hp + "%时召唤帮手";
        for (int i = 0; i < monsters.length; i++) {
            if (i > 0) {
                ret += ",";
            }
            ret += monsters[i];
        }
        ret += "，间隔" + interval + "毫秒，最多" + useTimes + "次";
        if (message != null && message.length() > 0) {
            ret += "，同时向" + messageDistance + "码范围内的玩家喊话：" + message;
        }
        return ret;
    }

    protected String getRuleType() {
        return "summon";
    }
    
    protected Class[] getParamClass() {
        return new Class[] { Integer.class, Integer.class, 
                GameMapNPC.class, GameMapNPC.class, GameMapNPC.class, GameMapNPC.class, GameMapNPC.class,
                GameMapNPC.class, GameMapNPC.class, GameMapNPC.class, GameMapNPC.class, GameMapNPC.class,
                Integer.class, Integer.class, String.class, Integer.class, java.awt.Color.class, Integer.class };
    }
    
    protected String[] getParamName() {
        return new String[] { "hp", "interval", 
                "monster1", "monster2", "monster3", "monster4", "monster5", 
                "monster6", "monster7", "monster8", "monster9", "monster10", 
                "livetime", "usetimes", "message", "messagedistance", "messagecolor", "messagetime" };
    }
    
    protected String[] getParamDesc() {
        return new String[] { "血量(%)", "平均间隔(毫秒)", 
                "怪物1", "怪物2", "怪物3", "怪物4", "怪物5", 
                "怪物6", "怪物7", "怪物8", "怪物9", "怪物10",
                "生存时间(毫秒)", "最多使用次数", "喊话", "喊话范围(码)", "喊话颜色", "喊话持续时间(毫秒)" };
    }
    
    protected Object getParamValue(int index) {
        switch (index) {
        case 0:
            return String.valueOf(hp);
        case 1:
            return String.valueOf(interval);
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
        case 9:
        case 10:
        case 11:
            return new Integer(monsters[index - 2]);
        case 12:
            return String.valueOf(liveTime);
        case 13:
            return String.valueOf(useTimes);
        case 14:
            return message;
        case 15:
            return String.valueOf(messageDistance);
        case 16:
            return String.valueOf(messageColor);
        case 17:
            return String.valueOf(messageTime);
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
                interval = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
        case 8:
        case 9:
        case 10:
        case 11:
            try {
                monsters[index - 2] = ((Integer)newValue).intValue();
            } catch (Exception e) {
            }
            break;
        case 12:
            try {
                liveTime = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 13:
            try {
                useTimes = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 14:
            message = (String)newValue;
            break;
        case 15:
            try {
                messageDistance = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 16:
            try {
                messageColor = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 17:
            try {
                messageTime = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        }
    }
    
    public AIRuleConfig duplicate() {
        SummonRuleConfig ret = new SummonRuleConfig();
        ret.hp = hp;
        ret.interval = interval;
        System.arraycopy(monsters, 0, ret.monsters, 0, monsters.length);
        ret.liveTime = liveTime;
        ret.useTimes = useTimes;
        ret.message = message;
        ret.messageDistance = messageDistance;
        ret.messageColor = messageColor;
        ret.messageTime = messageTime;
        return ret;
    }
}
