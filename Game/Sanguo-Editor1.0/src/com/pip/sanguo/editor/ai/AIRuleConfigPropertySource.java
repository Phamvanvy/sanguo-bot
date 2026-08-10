package com.pip.sanguo.editor.ai;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Element;

import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.property.LocationPropertyDescriptor;
import com.pip.sanguo.editor.property.NPCPropertyDescriptor;
import com.pip.sanguo.editor.property.SkillPropertyDescriptor;
import com.pip.util.SWTUtils;

public class AIRuleConfigPropertySource implements IPropertySource {
    AIRuleConfig rule;
    
    public AIRuleConfigPropertySource(AIRuleConfig rule) {
        this.rule = rule;
    }

    public Object getEditableValue() {
        return null;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        Class[] cls = rule.getParamClass();
        String[] name = rule.getParamName();
        String[] desc = rule.getParamDesc();
        IPropertyDescriptor[] ret = new IPropertyDescriptor[cls.length];
        for (int i = 0; i < cls.length; i++) {
            if (cls[i] == Integer.class) {
                ret[i] = new TextPropertyDescriptor(name[i], desc[i]);
            } else if (cls[i] == Float.class) {
                ret[i] = new TextPropertyDescriptor(name[i], desc[i]);
            } else if (cls[i] == String.class) {
                ret[i] = new TextPropertyDescriptor(name[i], desc[i]);
            } else if (cls[i] == SkillConfig.class) {
                ret[i] = new SkillPropertyDescriptor(name[i], desc[i]);
            } else if (cls[i] == GameMapNPC.class) {
                ret[i] = new NPCPropertyDescriptor(name[i], desc[i]);
            } else if (cls[i] == java.awt.Color.class) {
                ret[i] = new ColorPropertyDescriptor(name[i], desc[i]);
            } else if (cls[i] == int[].class) {
                ret[i] = new LocationPropertyDescriptor(name[i], desc[i]);
            } else if (cls[i] == AITargetType.class) {
                ret[i] = new ComboBoxPropertyDescriptor(name[i], desc[i], AITargetType.TYPE_NAMES);
            }
        }
        return ret;
    }
    
    public Object getPropertyValue(Object id) {
        String[] name = rule.getParamName();
        Class[] cls = rule.getParamClass();
        for (int i = 0; i < name.length; i++) {
            if (name[i].equals(id)) {
                Object ret = rule.getParamValue(i);
                if (cls[i] == java.awt.Color.class) {
                    // 颜色，转换为RGB对象
                    ret = SWTUtils.getRGB(Integer.parseInt((String)ret));
                } else if (cls[i] == AITargetType.class) {
                    int index = AITargetType.type2index(Integer.parseInt((String)ret));
                    return new Integer(index);
                }
                return ret;
            }
        }
        return null;
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {}
    
    public void setPropertyValue(Object id, Object value) {
        String[] name = rule.getParamName();
        Class[] cls = rule.getParamClass();
        for (int i = 0; i < name.length; i++) {
            if (name[i].equals(id)) {
                if (cls[i] == java.awt.Color.class) {
                    RGB clr = (RGB)value;
                    int v = (clr.red << 16) | (clr.green << 8) | clr.blue;
                    rule.setParamValue(i, String.valueOf(v));
                } else if (cls[i] == AITargetType.class) {
                    int type = AITargetType.TYPE_MAP[((Integer)value).intValue()];
                    rule.setParamValue(i, String.valueOf(type));
                } else {
                    rule.setParamValue(i, value);
                }
            }
        }
    }
}
