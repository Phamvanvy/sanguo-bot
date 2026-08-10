package com.pip.sanguo.data.recast;

import org.jdom.*;

/**
 * 重铸的一个性格。
 * @author jxu
 */
public class RecastProperty {
    public Recast owner;
    /**
     * 性格类型
     * 类型数组:0-力量,1-敏捷,2-体力,3-智力,4-生命,5-精力,6-暴击,7-命中,8-物闪
     * 类型数组:9-法闪,10-物攻,11-法攻,12-护甲,13-法防,14-回血,15-回气,16-速度,17-免暴
     */
    public int type;
    /**
     * 增加的属性是否未百分比。
     */
    public int isPercentValue;
    /**
     * 增加的属性值。
     */
    public int value;
    /**
     * 增加的属性值精确数值
     */
    public int finalValue;
    /**
     * 属性区间
     */
    public int[] attrArea;
    
    public RecastProperty(Recast own) {
        owner = own;
    }

    public boolean equals(Object o) {
        return this == o;
    }
    
    public void load(Element elem) {
        type = Integer.parseInt(elem.getAttributeValue("type"));
        isPercentValue = Integer.parseInt(elem.getAttributeValue("ispercent"));
        try {
            value = Integer.parseInt(elem.getAttributeValue("value"));
        } catch (Exception e) {
            value = 0;
        }
        String area = elem.getAttributeValue("attrArea");
        if (area != null && area.length() > 0) {
            setArea(area);
        }
    }
    
    public Element save() {
        Element ret = new Element("property");
        ret.addAttribute("type", String.valueOf(type));
        ret.addAttribute("ispercent", String.valueOf(isPercentValue));
        ret.addAttribute("value", String.valueOf(value));
        if (attrArea != null && attrArea.length > 0) {
            String area = getAreaText();
            if (area.length() > 0) {
                ret.addAttribute("attrArea", area);
            }
        }
        return ret;
    }
    
    public RecastProperty duplicate() {
        RecastProperty ret = new RecastProperty(owner);
        ret.type = type;
        ret.isPercentValue = isPercentValue;
        ret.value = value;
        ret.finalValue = finalValue;
        ret.attrArea = attrArea;
        return ret;
    }
    
    public void setArea(String areaStr) {
        String[] area = areaStr.split(",");
        if (area != null && area.length > 0) {
            attrArea = new int[area.length];
            for (int i = 0; i < area.length; i++) {
                try {
                    attrArea[i] = Integer.parseInt(area[i]);
                } catch (Exception e) {
                    attrArea[i] = 0;
                }
            }
        }
    }
    
    public String getAreaText() {
        String area = "";
        if (attrArea != null && attrArea.length > 0) {
            for (int i = 0; i < attrArea.length; i++) {
                area += attrArea[i];
                if (i < attrArea.length - 1) {
                    area += ",";
                }
            }
        }
        return area;
    }
}
