package com.pip.sanguo.data.skill;

import java.util.*;

import org.jdom.Element;

/**
 * 一组效果的集合。每个ID的效果只能有1个。
 * @author lighthu
 */
public class EffectConfigSet {
    protected int levelCount;
    protected List<EffectConfig> effects = new ArrayList<EffectConfig>();
    
    /**
     * 取得级别数。
     * @return
     */
    public int getLevelCount() {
        return levelCount;
    }
    
    /**
     * 设置级别数。
     * @return
     */
    public void setLevelCount(int value) {
        levelCount = value;
        for (EffectConfig eff : effects) {
            eff.setLevelCount(value);
        }
    }
    
    /**
     * 取得当前集合中包含的所有效果。
     */
    public EffectConfig[] getAllEffects() {
        EffectConfig[] ret = new EffectConfig[effects.size()];
        effects.toArray(ret);
        return ret;
    }

    /**
     * 判断某一类型的效果是否已经存在。
     * @param type
     * @return
     */
    public boolean exists(int type) {
        for (EffectConfig eff : effects) {
            if (eff.getType() == type) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 清空。
     */
    public void clear() {
        effects.clear();
    }
    
    /**
     * 根据类型查找效果。
     * @param type
     * @return
     */
    public EffectConfig findEffect(int type) {
        for (EffectConfig eff : effects) {
            if (eff.getType() == type) {
                return eff;
            }
        }
        return null;
    }
    
    /**
     * 查找指定类型组的所有效果。
     * @param types
     * @return
     */
    public List<EffectConfig> findEffects(int[] types) {
        List<EffectConfig> ret = new ArrayList<EffectConfig>();
        for (EffectConfig eff : effects) {
            for (int i = 0; i < types.length; i++) {
                if (eff.getType() == types[i]) {
                    ret.add(eff);
                    break;
                }
            }
        }
        return ret;
    }
    
    /**
     * 添加一个新效果。
     * @param eff
     */
    public void addEffect(EffectConfig eff) {
        effects.add(eff);
    }
    
    /**
     * 添加特殊效果。
     */
    public void addGeneralEffect(EffectConfig eff) {
        effects.add(0, eff);
    }
    
    /**
     * 删除一个类型的效果。
     * @param type
     */
    public void removeEffect(int type) {
        Iterator<EffectConfig> itor = effects.iterator();
        while (itor.hasNext()) {
            if (itor.next().getType() == type) {
                itor.remove();
            }
        }
    }
    
    /**
     * 取得所有参数。
     * @return
     */
    public List<EffectParamRef> getAllParams() {
        List<EffectParamRef> ret = new ArrayList<EffectParamRef>();
        for (EffectConfig eff : effects) {
            for (int i = 0; i < eff.getParamCount(); i++) {
                ret.add(new EffectParamRef(eff, i));
            }
        }
        return ret;
    }
    
    /**
     * 取得某个位置的参数描述。
     * @param index 0开始的下标
     * @return
     */
    public EffectParamRef getParamAt(int index) {
        int start = 0;
        for (EffectConfig eff : effects) {
            if (index < eff.getParamCount() + start) {
                return new EffectParamRef(eff, index - start);
            }
            start += eff.getParamCount();
        }
        return null;
    }
    
    /**
     * 复制对象。
     */
    public EffectConfigSet duplicate() {
        EffectConfigSet ret = new EffectConfigSet();
        for (EffectConfig eff : effects) {
            ret.addEffect((EffectConfig)eff.clone());
        }
        ret.levelCount = levelCount;
        return ret;
    }
    
    /**
     * 从XML标签中载入对象属性。
     * @param elem
     */
    public void load(Element elem, int maxLevel) throws Exception {
        effects.clear();
        List list = elem.getChildren("effect");
        for (int i = 0; i < list.size(); i++) {
            addEffect(EffectConfig.load((Element)list.get(i), maxLevel));
        }
    }
    
    /**
     * 保存成一个XML标签。
     */
    public Element save() {
        Element ret = new Element("effects");
        for (EffectConfig eff : effects) {
            if (eff.getType() != -1) {
                ret.addContent(eff.save());
            }
        }
        return ret;
    }
}
