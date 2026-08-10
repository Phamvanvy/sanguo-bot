package com.pip.sanguo.data.equipment;

/**
 * 装备的一个附加属性的描述。
 * @author lighthu
 */
public class EquipmentAttribute {
    /**
     * 属性ID
     */
    public String id;
    /**
     * 属性名字
     */
    public String name;
    /**
     * 缩写
     */
    public String shortName;
    /**
     * 每1点此属性占用的装备附加价值
     */
    public float value;
    
    public EquipmentAttribute(String id, String name, String shortName, float value) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.value = value;
    }
}
