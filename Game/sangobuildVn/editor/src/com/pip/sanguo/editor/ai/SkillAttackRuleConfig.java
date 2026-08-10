package com.pip.sanguo.editor.ai;

import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Element;

import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.SkillPropertyDescriptor;

/**
 * 技能攻击规则。
 * @author lighthu
 */
public class SkillAttackRuleConfig extends AIRuleConfig {
    /**
     * 触发血量百分比0-100
     */
    public int hp;
    /**
     * 第一次使用前的间隔（毫秒）
     */
    public int firstInterval;
    /**
     * 平均使用间隔（毫秒）
     */
    public int interval;
    /**
     * 使用间隔标准差（毫秒）
     */
    public int intervalDeviation;
    /**
     * 使用技能
     */
    public int skill;
    /**
     * 技能级别
     */
    public int skillLevel;
    /**
     * 目标类型
     */
    public AITargetType targetType = new AITargetType();
    /**
     * 使用技能
     */
    public int skill2;
    /**
     * 技能级别
     */
    public int skillLevel2;
    /**
     * 目标类型
     */
    public AITargetType targetType2 = new AITargetType();
    /**
     * 使用技能
     */
    public int skill3;
    /**
     * 技能级别
     */
    public int skillLevel3;
    /**
     * 目标类型
     */
    public AITargetType targetType3 = new AITargetType();
    /**
     * 最多使用次数
     */
    public int useTimes;
    /**
     * 使用时喊话（空串表示不喊话）
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
        String skillDesc = "无效技能";
        SkillConfig sc = (SkillConfig)EditorApplication.getProj().findObject(SkillConfig.class, skill);
        if (sc != null) {
            skillDesc = skillLevel + "级" + sc.title;
        }
        String skillDesc2 = null;
        if (skill2 != 0) {
            sc = (SkillConfig)EditorApplication.getProj().findObject(SkillConfig.class, skill2);
            if (sc != null) {
                skillDesc2 = skillLevel2 + "级" + sc.title;
            }
        }
        String skillDesc3 = null;
        if (skill3 != 0) {
            sc = (SkillConfig)EditorApplication.getProj().findObject(SkillConfig.class, skill3);
            if (sc != null) {
                skillDesc3 = skillLevel3 + "级" + sc.title;
            }
        }
        String ret = "血量少于" + hp + "%时向" + targetType.getDisplayString() + "施放" + skillDesc;
        if (skillDesc2 != null) {
            ret += "然后向" + targetType2.getDisplayString() + "施放" + skillDesc2;
        }
        if (skillDesc3 != null) {
            ret += "然后向" + targetType3.getDisplayString() + "施放" + skillDesc3;
        }
        ret += "，平均间隔" + interval + "毫秒，最多" + useTimes + "次";
        if (message != null && message.length() > 0) {
            ret += "，同时向" + messageDistance + "码范围内的玩家喊话：" + message;
        }
        return ret;
    }
    
    protected String getRuleType() {
        return "skillattack";
    }
    
    protected Class[] getParamClass() {
        return new Class[] { Integer.class, Integer.class, Integer.class, Integer.class, SkillConfig.class, Integer.class, AITargetType.class, SkillConfig.class, Integer.class, AITargetType.class, SkillConfig.class, Integer.class, AITargetType.class, Integer.class, String.class, Integer.class, java.awt.Color.class, Integer.class };
    }
    
    protected String[] getParamName() {
        return new String[] { "hp", "firstinterval", "interval", "intervaldeviation", "skill", "skilllevel", "targettype", "skill2", "skilllevel2", "targettype2", "skill3", "skilllevel3", "targettype3", "usetimes", "message", "messagedistance", "messagecolor", "messagetime" };
    }
    
    protected String[] getParamDesc() {
        return new String[] { "血量(%)", "首次使用延时(毫秒)", "平均间隔(毫秒)", "间隔标准差(毫秒)", "施放技能1", "技能级别1", "攻击目标1", "施放技能2", "技能级别2", "攻击目标2", "施放技能3", "技能级别3", "攻击目标3", "最多使用次数", "喊话", "喊话范围(码)", "喊话颜色", "喊话持续时间(毫秒)" };
    }
    
    protected Object getParamValue(int index) {
        switch (index) {
        case 0:
            return String.valueOf(hp);
        case 1:
            return String.valueOf(firstInterval);
        case 2:
            return String.valueOf(interval);
        case 3:
            return String.valueOf(intervalDeviation);
        case 4:
            return new Integer(skill);
        case 5:
            return String.valueOf(skillLevel);
        case 6:
            return targetType.toString();
        case 7:
            return new Integer(skill2);
        case 8:
            return String.valueOf(skillLevel2);
        case 9:
            return targetType2.toString();
        case 10:
            return new Integer(skill3);
        case 11:
            return String.valueOf(skillLevel3);
        case 12:
            return targetType3.toString();
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
                firstInterval = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 2:
            try {
                interval = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 3:
            try {
                intervalDeviation = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 4:
            try {
                skill = ((Integer)newValue).intValue();
            } catch (Exception e) {
            }
            break;
        case 5:
            try {
                skillLevel = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 6:
            targetType.parse((String)newValue);
            break;
        case 7:
            try {
                skill2 = ((Integer)newValue).intValue();
            } catch (Exception e) {
            }
            break;
        case 8:
            try {
                skillLevel2 = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 9:
            targetType2.parse((String)newValue);
            break;
        case 10:
            try {
                skill3 = ((Integer)newValue).intValue();
            } catch (Exception e) {
            }
            break;
        case 11:
            try {
                skillLevel3 = Integer.parseInt((String)newValue);
            } catch (Exception e) {
            }
            break;
        case 12:
            targetType3.parse((String)newValue);
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
        SkillAttackRuleConfig ret = new SkillAttackRuleConfig();
        ret.hp = hp;
        ret.firstInterval = firstInterval;
        ret.interval = interval;
        ret.intervalDeviation = intervalDeviation;
        ret.skill = skill;
        ret.skillLevel = skillLevel;
        ret.targetType.targetType = targetType.targetType;
        ret.skill2 = skill2;
        ret.skillLevel2 = skillLevel2;
        ret.targetType2.targetType = targetType2.targetType;
        ret.skill3 = skill3;
        ret.skillLevel3 = skillLevel3;
        ret.targetType3.targetType = targetType3.targetType;
        ret.useTimes = useTimes;
        ret.message = message;
        ret.messageDistance = messageDistance;
        ret.messageColor = messageColor;
        ret.messageTime = messageTime;
        return ret;
    }
}
