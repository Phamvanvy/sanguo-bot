package com.pip.sanguo.data.equipment;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jdom.Element;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.editor.property.BuffPropertyDescriptor;

/**
 * 装备数据属性
 * @author Joy
 */
public class Equipment extends Item {
    /**************************   常量   ***************************/
    
    /** 装备类型------武器 */
    public static final int EQUI_TYPE_WEAPON = 0;
    /** 装备类型------防具 */
    public static final int EQUI_TYPE_PROTECTOR = EQUI_TYPE_WEAPON + 1;
    /** 装备类型------饰品 */
    public static final int EQUI_TYPE_JEWELRY = EQUI_TYPE_PROTECTOR + 1;
    /** 装备类型------坐骑装备 */
    public static final int EQUI_TYPE_HORSE = EQUI_TYPE_JEWELRY + 1;
    
    /** 武器------无定义 */
    public static final int WEAPON_UNDEFINE = 0;
    /** 武器------枪 */
    public static final int WEAPON_SPEAR = WEAPON_UNDEFINE + 1;
    /** 武器------斧 */
    public static final int WEAPON_AXE = WEAPON_SPEAR + 1;
    /** 武器------长杆刀 */
    public static final int WEAPON_FALCHION = WEAPON_AXE + 1;
    /** 武器------刀 */
    public static final int WEAPON_KNIFE = WEAPON_FALCHION + 1;
    /** 武器------剑 */
    public static final int WEAPON_SWORD = WEAPON_KNIFE + 1;
    /** 武器------弓 */
    public static final int WEAPON_BOW = WEAPON_SWORD + 1;
    /** 武器------扇 */
    public static final int WEAPON_FAN = WEAPON_BOW + 1;
    
    /** 防具------无定义 */
    public static final int PROTECTOR_UNDEFINE = WEAPON_FAN + 1;
    /** 防具------头盔 */
    public static final int PROTECTOR_HELMETS = PROTECTOR_UNDEFINE + 1;
    /** 防具------衣服 */
    public static final int PROTECTOR_CLOTHES = PROTECTOR_HELMETS + 1;
    /** 防具------裤子 */
    public static final int PROTECTOR_TROUSERS = PROTECTOR_CLOTHES + 1;
    /** 防具------鞋子 */
    public static final int PROTECTOR_SHOES = PROTECTOR_TROUSERS + 1;
    /** 防具------副手 */
    public static final int PROTECTOR_SHIELD = PROTECTOR_SHOES + 1;
    
    /** 首饰------无定义 */
    public static final int JEWELRY_UNDEFINE = PROTECTOR_SHIELD + 1;
    /** 首饰------护腕 */
    public static final int JEWELRY_CUFF = JEWELRY_UNDEFINE + 1;
    /** 首饰------玉佩 */
    public static final int JEWELRY_YUPEI = JEWELRY_CUFF + 1;
    /** 首饰------护符 */
    public static final int JEWELRY_HUFU = JEWELRY_YUPEI + 1;
    /** 首饰------披风 */
    public static final int JEWELRY_PIFENG = JEWELRY_HUFU + 1;
    
    /** 坐骑------无定义 */
    public static final int HORSE_UNDEFINE = JEWELRY_PIFENG + 1;
    /** 坐骑------面具 */
    public static final int HORSE_HELMET = HORSE_UNDEFINE + 1;
    /** 坐骑------颈甲 */
    public static final int HORSE_NECK = HORSE_HELMET + 1;
    /** 坐骑------胸甲 */
    public static final int HORSE_BREAST = HORSE_NECK + 1;
    /** 坐骑------臀甲 */
    public static final int HORSE_ASS = HORSE_BREAST + 1;
    /** 坐骑------鞍 */
    public static final int HORSE_SADDLE = HORSE_ASS + 1;
    /** 坐骑------蹄掌 */
    public static final int HORSE_HOOF = HORSE_SADDLE + 1;
    /** 坐骑------脚蹬 */
    public static final int HORSE_PEDAL = HORSE_HOOF + 1;
    
    /** 属性：基本品质系数 */
    public static final String PROPNAME_BASICRATE = "basicrate";
    /** 属性：附加品质系数 */
    public static final String PROPNAME_EXTRARATE = "extrarate";
    /** 属性：装备价值 */
    public static final String PROPNAME_EQUVALUE = "equvalue";
    
    /**************************   常量   ***************************/
    
    /** 装备耐久度 */
    public int durability;
    
    /** 职业 */
    public int job = -1;
    
    /** 装备类型 */
    public int equipmentType;
    
    /** 智力限制下限 */
    public int astrictInteligence;
    
    /** 耐力限制下限 */
    public int astrictStamina;
    
    /** 敏捷限制下限 */
    public int astrictAgility;
    
    /** 力量限制下限 */
    public int astrictPower;
    
    /** 前缀 */
    public EquipmentPrefix prefix;
    /** 附加品质系数 */
    public float extraQuality;
    /** 是否显示为随机前缀（仅用于显示配方产品）*/
    public boolean showRandom;
    /** 初始孔数 */
    public int holeCount = 1;
    /** 最大孔数 */
    public int maxHoleCount = 5;
    /** 是否允许鉴定星级 */
    public boolean canJudgeStar;
    /** 是否允许鉴定资质 */
    public boolean canJudgePotential;
    /** 是否允许被复制 */
    public boolean canCopy;
    
    /**
     * 允许刻字的数量
     */
    public int markCharCount;
    
    /** 附加属性 */
    public float[] appendAttributes = new float[AttributeCalculator.ATTRIBUTES.length];
    
    /** 装备部位 */
    public int place;
    
    /** 附加BUFF ID，-1表示没有 */
    public int buffID = -1;
    /** 附加BUFF级别 */
    public int buffLevel;
    
    /** 特殊图标索引 */
    public int specialIconIndex = 0;

    /** 属性：BUFF ID */
    public static final String PROPNAME_BUFFID = "buffid";
    /** 属性：BUFF级别 */
    public static final String PROPNAME_BUFFLEVEL = "bufflevel";
    /** 属性：BUFF价值 */
    public static final String PROPNAME_BUFFVALUE = "buffvalue";
    
    public Equipment(ProjectData owner) {
        super(owner);
        type = TYPE_EQUIP;
        instance = true;
        prefix = new EquipmentPrefix(owner);
        taskFlag = false;
    }
    
    public DataObject duplicate() {
        Equipment copy = new Equipment(owner);
        copy.update(this);
        return copy;
    }

    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        description = elem.getAttributeValue("desc");
        categoryName = elem.getAttributeValue("category");
        if (categoryName == null) {
            categoryName = "";
        }
        
        level = Integer.parseInt(elem.getAttributeValue("level"));
        durability = Integer.parseInt(elem.getAttributeValue("durability"));
        job = Integer.parseInt(elem.getAttributeValue("job"));
        equipmentType = Integer.parseInt(elem.getAttributeValue("equipmentType"));
        
        int prefixID = Integer.parseInt(elem.getAttributeValue("prefix"));
        prefix = (EquipmentPrefix)owner.findObject(EquipmentPrefix.class, prefixID);
        if (prefix == null) {
            prefix = new EquipmentPrefix(owner);
        }
        showRandom = "true".equals(elem.getAttributeValue("showrandom"));
        astrictInteligence = Integer.parseInt(elem.getAttributeValue("astrictInteligence"));
        astrictStamina = Integer.parseInt(elem.getAttributeValue("astrictStamina"));
        astrictAgility = Integer.parseInt(elem.getAttributeValue("astrictAgility"));
        astrictPower = Integer.parseInt(elem.getAttributeValue("astrictPower"));
        playerLevel = Integer.parseInt(elem.getAttributeValue("playerLevel"));
        place = Integer.parseInt(elem.getAttributeValue("place"));
        quality = Integer.parseInt(elem.getAttributeValue("quality"));
        bind = Integer.parseInt(elem.getAttributeValue("bind"));
        sale = !"false".equals(elem.getAttributeValue("sale"));
        price = Integer.parseInt(elem.getAttributeValue("price"));
        try {
            holeCount = Integer.parseInt(elem.getAttributeValue("holecount"));
        } catch (Exception e) {
            holeCount = 1;
        }
        try {
            maxHoleCount = Integer.parseInt(elem.getAttributeValue("maxholecount"));
        } catch (Exception e) {
            maxHoleCount = 5;
        }
        String tmp = elem.getAttributeValue("judgestar");
        if (tmp == null) {
            // 缺省白装不能鉴定星级
            if (quality == QUALITY_WHITE) {
                canJudgeStar = false;
            } else {
                canJudgeStar = true;
            }
        } else {
            canJudgeStar = "1".equals(tmp);
        }
        tmp = elem.getAttributeValue("judgepotential");
        if (tmp == null) {
            // 缺省白装和饰品不能鉴定资质
            if (quality == QUALITY_WHITE) {
                canJudgePotential = false;
            } else {
                canJudgePotential = true;
            }
        } else {
            canJudgePotential = "1".equals(tmp);
        }
        tmp = elem.getAttributeValue("canCopy");
        if(tmp==null){
            canCopy = false;
        }else{
            canCopy = "1".equals(tmp);
        }
        tmp = elem.getAttributeValue("mark");
        if (tmp == null){
            markCharCount = 0;
        }else{
            markCharCount = Integer.parseInt(tmp);
        }
        // temp
//        if (getType(place) == EQUI_TYPE_JEWELRY || getType(place) == EQUI_TYPE_HORSE) {
//            if (quality == QUALITY_WHITE) {
//                canJudgeStar = false;
//            } else {
//                canJudgeStar = true;
//            }
//            canJudgePotential = false;
//        }

        switch(place){
        case WEAPON_AXE:
            iconIndex = 84;
            break;
        case WEAPON_BOW:
            iconIndex = 82;
            break;
        case WEAPON_FALCHION:
            iconIndex = 86;
            break;
        case WEAPON_FAN:
            iconIndex = 85;
            break;
        case WEAPON_KNIFE:
            iconIndex = 87;
            break;
        case WEAPON_SPEAR:
            iconIndex = 83;
            break;
        case WEAPON_SWORD:
            iconIndex = 57;
            break;
        case PROTECTOR_CLOTHES:
            iconIndex = 59;
            break;
        case PROTECTOR_HELMETS:
            iconIndex = 58;
            break;
        case PROTECTOR_SHOES:
            iconIndex = 60;
            break;
        case PROTECTOR_TROUSERS:
            iconIndex = 61;
            break;
        case PROTECTOR_SHIELD:
            iconIndex = 80;
            break;
        case JEWELRY_CUFF:
            iconIndex = 62;
            break;
        case JEWELRY_HUFU:
            iconIndex = 64;
            break;
        case JEWELRY_YUPEI:
            iconIndex = 63;
            break;
        case JEWELRY_PIFENG:
            iconIndex = 81;
            break;
        case HORSE_HELMET:
            iconIndex = 92;
            break;
        case HORSE_NECK:
            iconIndex = 91;
            break;
        case HORSE_BREAST:
            iconIndex = 96;
            break;
        case HORSE_ASS:
            iconIndex = 95;
            break;
        case HORSE_SADDLE:
            iconIndex = 93;
            break;
        case HORSE_HOOF:
            iconIndex = 94;
            break;
        case HORSE_PEDAL:
            iconIndex = 98;
            break;
        }
       
        timeType = Integer.parseInt(elem.getAttributeValue("timeType"));
        if (timeType != TIME_TYPE_UNDEFINE) {
            time = Integer.parseInt(elem.getAttributeValue("time"));
        }
        
        extraQuality = Float.parseFloat(elem.getAttributeValue("extraquality"));
        try {
            buffID = Integer.parseInt(elem.getAttributeValue("buffid"));
            buffLevel = Integer.parseInt(elem.getAttributeValue("bufflevel"));
        } catch (Exception e) {
        }
        
        // 如果指定了前缀，则使用前缀重新生成属性，否则载入保存时手工编辑的属性
        if (prefix.id != -1) {
            AttributeCalculator.generateAttributes(this);
        } else {
            List children = elem.getChildren("attribute");
            for (Object child : children) {
                Element childElem = (Element)child;
                
                String id = childElem.getAttributeValue("id");
                float value = Float.parseFloat(childElem.getAttributeValue("value"));
                int index = AttributeCalculator.findIndexOfAttribute(id);
                if (index >= 0) {
                    appendAttributes[index] = value;
                }
            }
            
            // 使用载入的属性更新临时前缀配置
            prefix.updatePriors(appendAttributes);
        }
        
        try {
            specialIconIndex = Integer.parseInt(elem.getAttributeValue("specialIconIndex"));
        } catch (Exception e) {
            specialIconIndex = 0;
        }
    }

    public Element save() {
        Element ret = new Element("equipment");
        
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("desc",description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        ret.addAttribute("level",String.valueOf(level));
        ret.addAttribute("durability",String.valueOf(durability));
        ret.addAttribute("job",String.valueOf(job));
        ret.addAttribute("equipmentType",String.valueOf(equipmentType));
        ret.addAttribute("astrictInteligence",String.valueOf(astrictInteligence));
        ret.addAttribute("astrictStamina",String.valueOf(astrictStamina));
        ret.addAttribute("astrictAgility",String.valueOf(astrictAgility));
        ret.addAttribute("astrictPower",String.valueOf(astrictPower));
        ret.addAttribute("playerLevel",String.valueOf(playerLevel));
        
        ret.addAttribute("place",String.valueOf(place));
        ret.addAttribute("bind",String.valueOf(bind));
        ret.addAttribute("quality",String.valueOf(quality));
        
        ret.addAttribute("sale", sale ? "true" : "false");
        ret.addAttribute("price",String.valueOf(price));

        ret.addAttribute("holecount",String.valueOf(holeCount));
        ret.addAttribute("maxholecount",String.valueOf(maxHoleCount));
        
        ret.addAttribute("judgestar",canJudgeStar ? "1" : "0");
        ret.addAttribute("judgepotential",canJudgePotential ? "1" : "0");
        ret.addAttribute("canCopy",canCopy ? "1" : "0");
        ret.addAttribute("mark",String.valueOf(markCharCount));
        ret.addAttribute("timeType",String.valueOf(timeType));
        if(timeType > ATTRIBUTE_NONE){
            ret.addAttribute("time",String.valueOf(time));
        }
        
        ret.addAttribute("prefix", String.valueOf(prefix.id));
        ret.addAttribute("showrandom", showRandom ? "true" : "false");
        ret.addAttribute("extraquality", String.valueOf(extraQuality));
        ret.addAttribute("buffid", String.valueOf(buffID));
        ret.addAttribute("bufflevel", String.valueOf(buffLevel));
        
        ret.addAttribute("specialIconIndex", String.valueOf(specialIconIndex));
        
        // 如果指定了前缀，则不需要保存属性，否则保存属性
        if (prefix.id == -1) {
            for (int i = 0; i < appendAttributes.length; i++) {
                if (appendAttributes[i] <= 0.0f) {
                    continue;
                }
                Element childElem = new Element("attribute");
                childElem.addAttribute("id", AttributeCalculator.ATTRIBUTES[i].id);
                childElem.addAttribute("value", String.valueOf(appendAttributes[i]));
                ret.addContent(childElem);
            }
        }
        
        return ret;
    }

    public void update(DataObject obj) {
        Equipment equi = (Equipment)obj;
        id = equi.id;
        title = equi.title;
        categoryName = equi.categoryName;
        
        description = equi.description;
        level = equi.level;
        durability = equi.durability;
        job = equi.job;
        equipmentType = equi.equipmentType;
        astrictInteligence = equi.astrictInteligence;
        astrictStamina = equi.astrictStamina;
        astrictAgility = equi.astrictAgility;
        astrictPower = equi.astrictPower;
        playerLevel = equi.playerLevel;
        place = equi.place;
        bind = equi.bind;
        quality = equi.quality;
        sale = equi.sale;
        price = equi.price;
        timeType = equi.timeType;
        time = equi.time;
        holeCount = equi.holeCount;
        maxHoleCount = equi.maxHoleCount;
        canJudgeStar = equi.canJudgeStar;
        canJudgePotential = equi.canJudgePotential;
        canCopy = equi.canCopy;
        markCharCount = equi.markCharCount;
        if (equi.prefix.id != -1) {
            prefix = equi.prefix;
        } else if (prefix.id == -1) {
            prefix.update(equi.prefix);
        } else {
            prefix = new EquipmentPrefix(owner);
            prefix.update(equi.prefix);
        }
        showRandom = equi.showRandom;
        extraQuality = equi.extraQuality;
        buffID = equi.buffID;
        buffLevel = equi.buffLevel;
        specialIconIndex = equi.specialIconIndex;
        System.arraycopy(equi.appendAttributes, 0, appendAttributes, 0, appendAttributes.length);
        
        if (owner != equi.owner) {
            if (prefix.owner != owner) {
                prefix = (EquipmentPrefix)owner.findObject(EquipmentPrefix.class, prefix.id);
            }
        }
    }
    
    /**
     * 取得某项属性的值。
     * @param property 参数ID，取值见AttributeCalculator的常量，其中有几条特殊规则：
     *    武器，物理攻击上下限使用标准值加上附加攻击力计算得来，附加物理攻击力总是返回0
     *    武器，法术攻击力需要加上标准值
     *    防具，护甲使用标准值加上附加护甲计算得来
     * @return
     */
    public int getAttribute(int attrID) {
        return Math.round(getAttributeImpl(attrID));
    }

    public float getAttributeImpl(int attrID) {
        if (equipmentType == EQUI_TYPE_WEAPON) {
            float atk = appendAttributes[AttributeCalculator.ATTRIBUTE_ATTACKPOWER];
            if (attrID == AttributeCalculator.ATTRIBUTE_MINATTACK) {
                return AttributeCalculator.getMinAttack(level, place, atk);
            } else if (attrID == AttributeCalculator.ATTRIBUTE_MAXATTACK) {
                return AttributeCalculator.getMaxAttack(level, place, atk);
            } else if (attrID == AttributeCalculator.ATTRIBUTE_MAGICPOWER) {
                return AttributeCalculator.getBaseMagicAttack(level, place) + appendAttributes[attrID];
            } else if (attrID == AttributeCalculator.ATTRIBUTE_ATTACKPOWER) {
                return 0;
            }
        } else {
            if (attrID == AttributeCalculator.ATTRIBUTE_ARMOR) {
                return AttributeCalculator.getBaseArmor(level, place) + appendAttributes[attrID];
            } else if (attrID == AttributeCalculator.ATTRIBUTE_MAGICARMOR) {
                return AttributeCalculator.getBaseMagicArmor(level, place) + appendAttributes[attrID];
            }
        }
        if (attrID == AttributeCalculator.ATTRIBUTE_MINATTACK) {
            return 0;
        } else if (attrID == AttributeCalculator.ATTRIBUTE_MAXATTACK) {
            return 0;
        } else {
            return appendAttributes[attrID];
        }
    }
    
    public boolean equals(Object obj){
        if(obj instanceof Equipment){
            return ((Equipment)obj).id == id;
        }
        return false;
    }
    
    /**
     * 返回当前装备类型属性
     * @return
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] ret = new IPropertyDescriptor[AttributeCalculator.ATTRIBUTES.length + 6];
        ret[0] = new PropertyDescriptor(PROPNAME_BASICRATE, "基本品质系数");
        ret[1] = new TextPropertyDescriptor(PROPNAME_EXTRARATE, "附加品质系数");
        ret[2] = new PropertyDescriptor(PROPNAME_EQUVALUE, "装备价值");
        int c = AttributeCalculator.ATTRIBUTES.length;
        for (int i = 0; i < c; i++) {
            EquipmentAttribute attr = AttributeCalculator.ATTRIBUTES[i];
            ret[i + 3] = new TextPropertyDescriptor(attr.id, attr.name);
        }
        ret[c + 3] = new BuffPropertyDescriptor(PROPNAME_BUFFID, "特效类型");
        ret[c + 4] = new TextPropertyDescriptor(PROPNAME_BUFFLEVEL, "特效级别");
        ret[c + 5] = new PropertyDescriptor(PROPNAME_BUFFVALUE, "特效价值");
        return ret;
    }
    
    /**
     * 获得当前选中的部位值，部位常量定义按照所有部位递增，所以需要在选择基础上增加一个初始值
     * @param equiType
     * @param miniType
     * @return
     */
    public static int getPlace(int equiType, int miniType) {
        int place = -1;
        if (equiType != -1 && miniType != -1) {
            switch(equiType) {
            case EQUI_TYPE_WEAPON:
                place = WEAPON_UNDEFINE + 1 + miniType;
                break;
            case EQUI_TYPE_PROTECTOR:
                place = PROTECTOR_UNDEFINE + 1 + miniType;
                break;
            case EQUI_TYPE_JEWELRY:
                place = JEWELRY_UNDEFINE + 1 + miniType;
                break;
            case EQUI_TYPE_HORSE:
                place = HORSE_UNDEFINE + 1 + miniType;
                break;
            }
        }
        return place;
    }
    
    public void recalcPriceAndDurability() {
        price = AttributeCalculator.getPrice(level, quality, place) / 2;
        durability = AttributeCalculator.getDurability(level, quality, place);
    }
    
    /**
     * 根据装备部位得到装备类型
     * @param place
     * @return
     */
    public static int getType(int place){
        switch(place){
        case WEAPON_AXE:
        case WEAPON_BOW:
        case WEAPON_FALCHION:
        case WEAPON_FAN:
        case WEAPON_KNIFE:
        case WEAPON_SPEAR:
        case WEAPON_SWORD:
            return EQUI_TYPE_WEAPON;
        case PROTECTOR_CLOTHES:
        case PROTECTOR_HELMETS:
        case PROTECTOR_SHOES:
        case PROTECTOR_TROUSERS:
        case PROTECTOR_SHIELD:
            return EQUI_TYPE_PROTECTOR;
        case JEWELRY_CUFF:
        case JEWELRY_HUFU:
        case JEWELRY_YUPEI:
        case JEWELRY_PIFENG:
            return EQUI_TYPE_JEWELRY;
        case HORSE_HELMET:
        case HORSE_NECK:
        case HORSE_BREAST:
        case HORSE_ASS:
        case HORSE_SADDLE:
        case HORSE_HOOF:
        case HORSE_PEDAL:
            return EQUI_TYPE_HORSE;
        default:
            return -1;
        }
    }
    
    public String getTitle() {
        return title + "(" + level + "/" + playerLevel + ")";
    }
    
    /**
     * 计算装备价值。
     * @return
     */
    public float getValue() {
        float ret = AttributeCalculator.getValue(level, quality, place, extraQuality);
        if (ret < 1.0f) {
            return 1.0f;
        } else {
            return ret;
        }
    }
    
    /**
     * 计算装备附加特效的价值。
     */
    public float getBuffValue() {
        BuffConfig buff = (BuffConfig)owner.findObject(BuffConfig.class, buffID);
        if (buff == null) {
            return 0.0f;
        }
        if (buffLevel < 1 || buffLevel > buff.maxLevel) {
            return 0.0f;
        }
        return buff.value[buffLevel - 1];
    }
    
    /**
     * 取得一个装备的实际价值系数
     * @return
     */
    public float getQualityValue() {
        return 1.0f + AttributeCalculator.QUALITY_ADDITION[quality] + extraQuality;
    }
}
