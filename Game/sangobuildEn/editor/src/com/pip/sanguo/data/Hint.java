package com.pip.sanguo.data;

import org.jdom.*;

/**
 * 小提示。
 * @author lighthu
 */
public class Hint {
    /**
     * 最低用户级别（包含）
     */
    public int minLevel;
    /**
     * 最高用户级别（包含）
     */
    public int maxLevel;
    /**
     * 权重
     */
    public int weight;
    /**
     * 内容
     */
    public String message;
    /**
     * 需求键盘类型，-1表示不需求
     */
    public int keyboardType;
    /**
     * 需求鼠标类型，-1表示不需求
     */
    public int mouseType;
    
    public void load(Element elem) {
        minLevel = Integer.parseInt(elem.getAttributeValue("minlevel"));
        maxLevel = Integer.parseInt(elem.getAttributeValue("maxlevel"));
        weight = Integer.parseInt(elem.getAttributeValue("weight"));
        try {
            keyboardType = Integer.parseInt(elem.getAttributeValue("keyboard"));
        } catch (Exception e) {
            keyboardType = -1;
        }
        try {
            mouseType = Integer.parseInt(elem.getAttributeValue("mouse"));
        } catch (Exception e) {
            mouseType = -1;
        }
        message = elem.getText();
    }
}
