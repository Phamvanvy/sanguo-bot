package com.pip.sanguo.editor.skill;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.EffectConfig;
import com.pip.sanguo.data.skill.EffectParamRef;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.util.Utils;

/**
 * 一个技能或BUFF参数的修改器。
 */
public class ParamIndicator {
    public static final int TYPE_SKILL_ACTIVE = 0;
    public static final int TYPE_SKILL_PASSIVE = 1;
    public static final int TYPE_BUFF_OWNER = 2;
    public static final int TYPE_BUFF_SOURCE = 3;
    
    public int type;
    public int id;
    public int paramIndex;
    
    public void update(ParamIndicator obj) {
        type = obj.type;
        id = obj.id;
        paramIndex = obj.paramIndex;
    }
    
    public void load(String str) {
        try {
            String[] secs = Utils.splitString(str, ',');
            type = Integer.parseInt(secs[0]);
            id = Integer.parseInt(secs[1]);
            paramIndex = Integer.parseInt(secs[2]);
        } catch (Exception e) {
        }
    }
    
    public String toString() {
        return type + "," + id + "," + paramIndex;
    }
    
    public String toString(ProjectData proj) {
        try {
            StringBuilder sb = new StringBuilder();
            if (type == TYPE_SKILL_ACTIVE) {
                SkillConfig skill = (SkillConfig)proj.findObject(SkillConfig.class, id);
                EffectParamRef param = getParamRef(proj);
                sb.append("施放技能");
                sb.append(skill.title);
                sb.append("时:");
                sb.append(param.getParamName());
            } else if (type == TYPE_SKILL_PASSIVE) {
                SkillConfig skill = (SkillConfig)proj.findObject(SkillConfig.class, id);
                EffectParamRef param = getParamRef(proj);
                sb.append("受到技能");
                sb.append(skill.title);
                sb.append("攻击/治疗时:");
                sb.append(param.getParamName());
            } else if (type == TYPE_BUFF_OWNER) {
                BuffConfig buff = (BuffConfig)proj.findObject(BuffConfig.class, id);
                EffectParamRef param = getParamRef(proj);
                sb.append("被加BUFF ");
                sb.append(buff.title);
                sb.append(":");
                sb.append(param.getParamName());
            } else if (type == TYPE_BUFF_SOURCE) {
                BuffConfig buff = (BuffConfig)proj.findObject(BuffConfig.class, id);
                EffectParamRef param = getParamRef(proj);
                sb.append("给别人加BUFF ");
                sb.append(buff.title);
                sb.append(":");
                sb.append(param.getParamName());
            }
            return sb.toString();
        } catch (Exception e) {
            return "无";
        }
    }
    
    public EffectParamRef getParamRef(ProjectData proj) {
        try {
            if (type == TYPE_SKILL_ACTIVE || type == TYPE_SKILL_PASSIVE) {
                SkillConfig skill = (SkillConfig)proj.findObject(SkillConfig.class, id);
                return skill.effects.getParamAt(paramIndex);
            } else if (type == TYPE_BUFF_OWNER || type == TYPE_BUFF_SOURCE) {
                BuffConfig buff = (BuffConfig)proj.findObject(BuffConfig.class, id);
                EffectConfig gc = buff.getGeneralConfig();
                if (paramIndex < gc.getParamCount()) {
                    return new EffectParamRef(gc, paramIndex);
                } else {
                    return buff.effects.getParamAt(paramIndex - gc.getParamCount());
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
