package com.pip.sanguo.editor.ai;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Element;

import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.property.NPCPropertyDescriptor;
import com.pip.sanguo.editor.property.SkillPropertyDescriptor;

/**
 * 编辑器配置AI规则。
 * @author lighthu
 */
public abstract class AIRuleConfig {
    /**
     * 保存到XML文件
     * @return
     */
    public Element toXML() {
        Element elem = new Element("rule");
        elem.addAttribute("type", getRuleType());
        String[] name = getParamName();
        Class[] cls = getParamClass();
        for (int i = 0; i < name.length; i++) {
            if (cls[i] == int[].class) {
                int[] arr = (int[])getParamValue(i);
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < arr.length; j++) {
                    if (j > 0) {
                        sb.append(",");
                    }
                    sb.append(arr[j]);
                }
                elem.addAttribute(name[i], sb.toString());
            } else {
                elem.addAttribute(name[i], String.valueOf(getParamValue(i)));
            }
        }
        return elem;
    }
    
    /**
     * 从XML文件中读取
     * @param elem
     */
    public void loadFromXML(Element elem) {
        String[] name = getParamName();
        Class[] cls = getParamClass();
        for (int i = 0; i < name.length; i++) {
            String value = elem.getAttributeValue(name[i]);
            if (value == null) {
                continue;
            }
            if (cls[i] == SkillConfig.class || cls[i] == GameMapNPC.class) {
                setParamValue(i, new Integer(value));
            } else if (cls[i] == int[].class) {
                String[] secs = value.split(",");
                int[] vs = new int[secs.length];
                for (int j = 0; j < secs.length; j++) {
                    vs[j] = Integer.parseInt(secs[j]);
                }
                setParamValue(i, vs);
            } else {
                setParamValue(i, value);
            }
        }
    }

    /*
     * 取得规则类型ID。
     */
    protected abstract String getRuleType();
    /*
     * 取得参数类型。
     */
    protected abstract Class[] getParamClass();
    /*
     * 取得参数名称。
     */
    protected abstract String[] getParamName();
    /*
     * 取得参数描述。
     */
    protected abstract String[] getParamDesc();
    /*
     * 取得参数值。
     */
    protected abstract Object getParamValue(int index);
    /*
     * 设置参数值。
     */
    protected abstract void setParamValue(int index, Object newValue);
    /**
     * 复制
     * @return
     */
    public abstract AIRuleConfig duplicate();
    
    public static AIRuleConfig load(Element elem) {
        String type = elem.getAttributeValue("type");
        AIRuleConfig ret = null;
        if ("escape".equals(type)) {
            ret = new EscapeRuleConfig();
        } else if ("skillattack".equals(type)) {
            ret = new SkillAttackRuleConfig();
        } else if ("skillheal".equals(type)) {
            // ret = new SkillHealRuleConfig();
            ret = new SkillAttackRuleConfig();
            ((SkillAttackRuleConfig)ret).targetType.targetType = -7;
        } else if ("summon".equals(type)) {
            ret = new SummonRuleConfig();
        } else if ("walkshout".equals(type)) {
            ret = new WalkShoutRuleConfig();
        }
        ret.loadFromXML(elem);
        return ret;
    }
}
