package com.pip.sanguo.data.item;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.equipment.AttributeCalculator;

/**
 * 物品的描述信息
 * 
 * @author Joy Yan
 * 
 */
public class Item extends DataObject {
    /**
     * 物品分类：一般物品。
     */
    public static final int TYPE_NORMAL = 0;
    /**
     * 物品分类：装备。
     */
    public static final int TYPE_EQUIP = 1;
    /**
     * 物品分类：技能书。
     */
    public static final int TYPE_SKILLBOOK = 2;
    /**
     * 物品分类：坐骑。
     */
    public static final int TYPE_HORSE = 3;
    /**
     * 物品分类：坐骑口粮。
     */
    public static final int TYPE_HORSEFOOD = 4;
    /**
     * 物品分类：称号。
     */
    public static final int TYPE_TITLE = 5;
    /**
     * 物品分类：采集材料。
     */
    public static final int TYPE_MATERIAL = 6;
    /**
     * 物品分类：打造配方。
     */
    public static final int TYPE_FORMULA = 7;
    /**
     * 物品分类：宝石。
     */
    public static final int TYPE_JEWEL = 8;
    /**
     * 物品分类：补给。
     */
    public static final int TYPE_RENEW = 9;
    /**
     * 物品分类：杂物。
     */
    public static final int TYPE_MISC = 10;
    /**
     * 物品分类：精炼。
     */
    public static final int TYPE_REFINE = 11;
    /**
     * 物品分类：卡片。
     */
    public static final int TYPE_CARD = 12;
    /**
     * 物品分类：法宝
     */
    public static final int TYPE_TALISMAN = 13;
    
    /**
     * 物品属性无效
     */
    public static final int ATTRIBUTE_NONE = -1;
    /**
     * 属性取值为否
     */
    public static final int ATTRIBUTE_VALUE_NO = 0;
    /**
     * 属性取值为是
     */
    public static final int ATTRIBUTE_VALUE_YES = 1;
    
    /**
     * 物品不可使用 
     */
    public static final int AVAILABLE_NO = 0;
    /**
     * 物品只有战斗中可使用 
     */
    public static final int AVAILABLE_BATTLE = 1;
    /**
     * 物品只有非战斗中可使用 
     */
    public static final int AVAILABLE_UN_BATTLE = 2;    
    /**
     * 物品任何时候都可使用 
     */
    public static final int AVAILABLE_EVER = 2;

    /**
     * 自动使用 
     * 默认自动：得到物品后，将给出系统默认是否使用的选择提示，玩家可以选择使用或则不使用;
     */
    public static final int AUTOUSE_DEFAULT = 0;
    /**
     * 自动使用
     * 定义自动：可针对此物品单独定义提示是否使用的描述和选项文字；
     */
    public static final int AUTOUSE_DEFINE = 1;
    /**
     * 自动使用
     * 后台自动：不做任何提示，得到物品后系统自动将其使用
     */
    public static final int AUTOUSE_BACKGROUND = 2;
    
    /**
     * 使用范围 
     * 0. 无目标 
     */
    public static final int AREA_UN_DEFINE = 0;
    /**
     * 使用范围 
     * 1. 自己 
     */
    public static final int AREA_SELF = 1;
    /**
     * 使用范围 
     * 2. 己方队友
     */
    public static final int AREA_TEAM = 2;
    /**
     * 使用范围 
     * 3. 敌方
     */
    public static final int AREA_ENEMY = 3;
    /**
     * 使用范围 
     * 4. 全部
     */
    public static final int AREA_ALL = 4;
    
    
    /**
     * 时间类型
     *  0:没有时效限制
     */
    public static final int TIME_TYPE_UNDEFINE = 0;
    /**
     * 时间类型
     *  1:绝对时效
     */
    public static final int TIME_TYPE_ABSOLUTELY = 1;
    /**
     * 时间类型
     *  2:相对时效
     */
    public static final int TIME_TYPE_RELATIVELY = 2;
    
    /**
     * 物品品质
     * 0.  普通（白）
     */
    public static final int QUALITY_WHITE = 0;
    /**
     * 物品品质
     * 1.  优良（绿）
     */
    public static final int QUALITY_GREEN = 1;
    /**
     * 物品品质
     * 2.  精良（蓝）
     */
    public static final int QUALITY_BLUE = 2;
    /**
     * 物品品质
     * 3.  史诗（紫）
     */
    public static final int QUALITY_PURPLE = 3;
    /**
     * 物品品质
     * 4.  传说（橙）
     */
    public static final int QUALITY_ORANGE = 4;
    /**
     * 物品品质
     * 5.  奖励（黄）
     */
    public static final int QUALITY_YELLOW = 5;
    
    /**
     * 是否绑定
     * 0.  不绑定
     */
    public static final int BIND_NO = 0;
    /**
     * 是否绑定
     * 1.  装备绑定
     */
    public static final int BIND_EQUIPMENT = 1;
    /**
     * 是否绑定
     * 2.  拾取绑定
     */
    public static final int BIND_PICK_UP = 2;

    /**
     * 所属项目。
     */
    public ProjectData owner;
    
    /**
     * 物品堆叠数量，必须大于0
     */
    public int addition = 1;
    
    /**
     * 是否实例类型，如果是实例的话，堆叠数量必须为1；
     */
    public boolean instance;

    /**
     * 物品类型
     */
    public int type = TYPE_NORMAL;
    
    /**
     * 是否可以批量使用
     */
    public boolean batchUseFlag;

    /**
     * 是否是任务物品
     */
    public boolean taskFlag;
    
    /**
     * 是否有瑕疵
     */
    public boolean isFlaw;

    /**
     * 物品是否可使用 
     * 0. 否：不可直接使用的物品，在物品管理中将不显示使用功能项 
     * 1. 战斗中可用 
     * 2. 非战斗中可用 
     * 3. 任何时候可用
     */
    public int available;
    
    /**
     * 使用限制职业：
     * 0 - 武将
     * 1 - 刺客
     * 2 - 谋士
     * 3 - 方士
     * 4 - 不限制
     */
    public int useClazz = 4;
    
    /**
     * 使用确认字符串，空串表示不需要确认。
     */
    public String useConfirm = "";
    
    /**
     * 物品品质
     * 0.  普通（白）
     * 1.  优良（绿）
     * 2.  精良（蓝）
     * 3.  史诗（紫）
     * 4.  传说（橙）
     * 5.  奖励（黄）
     */
    public int quality;

    /**
     * 是否消耗：
     *  0. 否：不消耗；
     *  1. 是：每次消耗一个；
     */
    public boolean waste;

    /**
     * 自动使用 
     * -1. 不可以自动使用 
     *  0. 默认自动：得到物品后，将给出系统默认是否使用的选择提示，玩家可以选择使用或则不使用； 
     *  1. 定义自动：可针对此物品单独定义提示是否使用的描述和选项文字；
     *  2. 后台自动：不做任何提示，得到物品后系统自动将其使用
     */
    public int autoUse;
    /**
     * 物品级别
     *  -1：不做限制；
     *  >0：限制等级
     */
    public int level;
    
    /**
     * 使用范围 
     * 0. 无目标 
     * 1. 自己 
     * 2. 己方队友
     * 3. 敌方
     * 4. 全部
     */
    public int area;

    /**
     * 使用该物品的玩家等级下限
     *  -1：没有限制
     *  >0: 实际等级
     */
    public int playerLevel = 1;
    
    /**
     * 使用次数，当变量available=1（可消耗物品时），规定了该物品的使用次数
     */
    public int count;

    /**
     * 冷却时间
     */
    public int coldDownTime;

    /**
     * 冷却组
     */
    public int coldDownGroup;

    /**
     * 时间类型
     *  0:没有时效限制
     *  1:绝对时效
     *  2:相对时效
     */
    public int timeType;

    /**
     * 使用时效；
     * 当实时效型为绝对时效时，存放的是指定日期时间根据Java标准得到的确定秒数；
     * 当实时效型为相对时效时，记录一个时间（单位：秒）；
     */
    public int time;
    
    /**
     * 施法时间，单位（毫秒）；
     * 物品使用后多长时间后才真正出效果，这段时间通过进度条来显示进度，
     * 中途可以按键取消使用，物品不消失，效果不出现。
     */
    public int schedule;

    /**
     * 价格
     */
    public int price;

    /**
     * 能否被出售
     *  0：否
     *  1：是
     */
    public boolean sale = true;

    /**
     * 是否绑定
     * 0.  不绑定
     * 1.  装备绑定
     * 2.  拾取绑定
     */
    public int bind;
    
    /**
     * 图标索引
     */
    public int iconIndex;
    
    /**
     * 当物品为任务物品时，是否复制为队中没人一个
     */
    public boolean taskMuti;
    
    /**
     * 使用距离
     */
    public int distance;
    
    /**
     * 宝石：是否坐骑宝石。
     */
    public boolean isHorseJewel;
    
    /**
     * 宝石：附加属性。
     */
    public int jewelAttrType;
    
    /**
     * 宝石：所属分类（根据附加属性类型归类）
     */
    public int jewelType;
    
    /** 宝石分类：根基类 */
    public static final int JEWEL_BASIC = 0;
    /** 宝石分类：剑罡类 */
    public static final int JEWEL_ATTACK = 1;
    /** 宝石分类：会心类 */
    public static final int JEWEL_CRIT = 2;
    /** 宝石分类：凝神类 */
    public static final int JEWEL_MP = 3;
    /** 宝石分类：生机类 */
    public static final int JEWEL_HP = 4;
    /** 宝石分类：轻身类 */
    public static final int JEWEL_DODGE = 5;
    /** 宝石分类：强身类 */
    public static final int JEWEL_DEFENSE = 6;
    /** 宝石分类：速度类 */
    public static final int JEWEL_SPEED = 7;
    public static final String[] JEWEL_TYPE_NAMES = {
        "根基类", "剑罡类", "会心类", "凝神类", "生机类", "轻身类", "强身类", "速度类"
    };

    /**
     * 使用效果列表
     */
    public List<ItemEffect> effects = new ArrayList<ItemEffect>();
    

    public Item(ProjectData owner) {
        this.owner = owner;
    }

    public boolean depends(DataObject obj) {
        // TODO Auto-generated method stub
        return false;
    }

    public DataObject duplicate() {
        Item itemCopy = new Item(owner);
        itemCopy.update(this);
        return itemCopy;
    }

    public void load(Element elem) {
        title = elem.getAttributeValue("title");
        description = elem.getAttributeValue("desc");
        categoryName = elem.getAttributeValue("category");
        if (categoryName == null) {
            categoryName = "";
        }
        
        id = Integer.parseInt(elem.getAttributeValue("id"));
        type = Byte.parseByte(elem.getAttributeValue("type"));
        batchUseFlag = Boolean.parseBoolean(elem.getAttributeValue("batchUseFlag"));
        addition = Integer.parseInt(elem.getAttributeValue("addition"));
        taskFlag = Boolean.parseBoolean(elem.getAttributeValue("taskflag"));
        if(taskFlag){
            try {
                taskMuti = Boolean.parseBoolean(elem.getAttributeValue("taskMuti"));
            }
            catch (Exception e) {
                taskMuti = true;
            }
        }
        bind = Integer.parseInt(elem.getAttributeValue("bind"));
        quality = Integer.parseInt(elem.getAttributeValue("quality"));
        
        try {
            iconIndex = Integer.parseInt(elem.getAttributeValue("iconIndex"));
        }
        catch (NumberFormatException e1) {
            iconIndex = 0;
        }
        
        sale = Boolean.parseBoolean(elem.getAttributeValue("sale"));
        if(sale){            
            price = Integer.parseInt(elem.getAttributeValue("price"));
        }
        
        instance = Boolean.parseBoolean(elem.getAttributeValue("instance"));
        
        available = Integer.parseInt(elem.getAttributeValue("available"));
        if(available != AVAILABLE_NO){
            try {
                useClazz = Integer.parseInt(elem.getAttributeValue("useclazz"));
            } catch (Exception e) {
                useClazz = 4;
            }
            useConfirm = elem.getAttributeValue("useconfirm");
            if (useConfirm == null) {
                useConfirm = "";
            }
            waste = Boolean.parseBoolean(elem.getAttributeValue("waste"));
            count = Integer.parseInt(elem.getAttributeValue("count"));
            area = Integer.parseInt(elem.getAttributeValue("area"));
            coldDownGroup = Integer.parseInt(elem.getAttributeValue("colddowngroup"));
            coldDownTime = Integer.parseInt(elem.getAttributeValue("coldDownTime"));
            autoUse = Integer.parseInt(elem.getAttributeValue("autouse"));
            schedule = Integer.parseInt(elem.getAttributeValue("schedule"));
            level = Integer.parseInt(elem.getAttributeValue("level"));
            playerLevel = Integer.parseInt(elem.getAttributeValue("playerlevel"));
            
            timeType = Integer.parseInt(elem.getAttributeValue("timetype"));
            if(timeType != TIME_TYPE_UNDEFINE){
                try {
                    time = Integer.parseInt(elem.getAttributeValue("time"));
                } catch (Exception e) {
                    time = 0;
                }
            }
            
            try {
                distance = Integer.parseInt(elem.getAttributeValue("distance"));
            }
            catch (NumberFormatException e1) {
                distance = 15*8;//默认值15码
            }
            
            List children = elem.getChildren("effect");
            for (int i = 0; children != null && i < children.size(); i++) {
                ItemEffect e = new ItemEffect();
                e.load((Element) children.get(i));
                effects.add(e);
            }
        }
        if (type == TYPE_JEWEL) {
            playerLevel = Integer.parseInt(elem.getAttributeValue("jewellevel"));
            try {
                level = Integer.parseInt(elem.getAttributeValue("level"));
            } catch (Exception e) {
                switch (playerLevel) {
                case 1:
                    level = 4;
                    break;
                case 2:
                    level = 7;
                    break;
                case 3:
                    level = 12;
                    break;
                case 4:
                    level = 20;
                    break;
                case 5:
                    level = 33;
                    break;
                case 6:
                    level = 57;
                    break;
                case 7:
                    level = 97;
                    break;
                }
            }
            isHorseJewel = "1".equals(elem.getAttributeValue("horsejewel"));
            try {
                setJewelAttrType(Integer.parseInt(elem.getAttributeValue("jewelattr")));
            } catch (Exception e) {
            }
            String s = elem.getAttributeValue("flaw");
            isFlaw = "1".equals(s);   
        }
    }
    
    public void setJewelAttrType(int t) {
        jewelAttrType = t;
            
        // 宝石加的属性决定了宝石类型
        switch (jewelAttrType) {
        case AttributeCalculator.ATTRIBUTE_STR:
        case AttributeCalculator.ATTRIBUTE_AGI:
        case AttributeCalculator.ATTRIBUTE_INT:
            jewelType = JEWEL_BASIC;
            break;
        case AttributeCalculator.ATTRIBUTE_ATTACKPOWER:
        case AttributeCalculator.ATTRIBUTE_MAGICPOWER:
            jewelType = JEWEL_ATTACK;
            break;
        case AttributeCalculator.ATTRIBUTE_CRIT:
        case AttributeCalculator.ATTRIBUTE_HIT:
            jewelType = JEWEL_CRIT;
            break;
        case AttributeCalculator.ATTRIBUTE_STA:
        case AttributeCalculator.ATTRIBUTE_HP:
        case AttributeCalculator.ATTRIBUTE_HPRENEW:
            jewelType = JEWEL_HP;
            break;
        case AttributeCalculator.ATTRIBUTE_MP:
        case AttributeCalculator.ATTRIBUTE_MPRENEW:
            jewelType = JEWEL_MP;
            break;
        case AttributeCalculator.ATTRIBUTE_ANTICRIT:
        case AttributeCalculator.ATTRIBUTE_DODGE:
        case AttributeCalculator.ATTRIBUTE_MAGICDODGE:
            jewelType = JEWEL_DODGE;
            break;
        case AttributeCalculator.ATTRIBUTE_ARMOR:
        case AttributeCalculator.ATTRIBUTE_MAGICARMOR:
            jewelType = JEWEL_DEFENSE;
            break;
        case AttributeCalculator.ATTRIBUTE_SPEED:
            jewelType = JEWEL_SPEED;
            break;
        }
    }

    public Element save() {
        Element ret = new Element("item");
        
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("desc",description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        ret.addAttribute("type",String.valueOf(type));
        ret.addAttribute("batchUseFlag", String.valueOf(batchUseFlag));
        ret.addAttribute("addition",String.valueOf(addition));
        ret.addAttribute("taskflag",String.valueOf(taskFlag));
        ret.addAttribute("iconIndex",String.valueOf(iconIndex));
        if(taskFlag){
            ret.addAttribute("taskMuti",String.valueOf(taskMuti));
        }
        ret.addAttribute("bind",String.valueOf(bind));
        ret.addAttribute("quality",String.valueOf(quality));
        ret.addAttribute("instance",String.valueOf(instance));
        
        ret.addAttribute("sale",String.valueOf(sale));
        if(sale){
            ret.addAttribute("price",String.valueOf(price));
        }
        
        ret.addAttribute("available",String.valueOf(available));
        if(available != AVAILABLE_NO){
            ret.addAttribute("useclazz", String.valueOf(useClazz));
            ret.addAttribute("useconfirm", useConfirm);
            ret.addAttribute("waste",String.valueOf(waste));
            ret.addAttribute("count",String.valueOf(count));
            ret.addAttribute("area",String.valueOf(area));
            
            ret.addAttribute("colddowngroup",String.valueOf(coldDownGroup));
            ret.addAttribute("coldDownTime",String.valueOf(coldDownTime));
            
            ret.addAttribute("autouse",String.valueOf(autoUse));
            ret.addAttribute("schedule",String.valueOf(schedule));
            ret.addAttribute("level",String.valueOf(level));
            ret.addAttribute("playerlevel",String.valueOf(playerLevel));
            
            ret.addAttribute("timetype",String.valueOf(timeType));
            if(timeType > ATTRIBUTE_NONE){
                ret.addAttribute("time",String.valueOf(time));
            }
            ret.addAttribute("distance",String.valueOf(distance));
            
            for (int i = 0; i < effects.size(); i++) {
                Element child = effects.get(i).save();
                if(child != null){
                    ret.getMixedContent().add(child);
                }
            }
        }
        if (type == TYPE_JEWEL) {
            ret.addAttribute("level",String.valueOf(level));
            ret.addAttribute("jewellevel",String.valueOf(playerLevel));
            ret.addAttribute("horsejewel", isHorseJewel ? "1" :"0");
            ret.addAttribute("jewelattr", String.valueOf(jewelAttrType));
            ret.addAttribute("flaw",isFlaw ? "1" :"0");
        }
        return ret;
    }

    public void update(DataObject obj) {
        Item itemCopy = (Item) obj;
        id = itemCopy.id;
        addition = itemCopy.addition;
        instance = itemCopy.instance;
        area = itemCopy.area;
        autoUse = itemCopy.autoUse;
        waste = itemCopy.waste;
        available = itemCopy.available;
        useClazz = itemCopy.useClazz;
        useConfirm = itemCopy.useConfirm;
        quality = itemCopy.quality;
        bind = itemCopy.bind;
        count = itemCopy.count;
        description = itemCopy.description;
        level = itemCopy.level;
        playerLevel = itemCopy.playerLevel;
        coldDownTime = itemCopy.coldDownTime;
        coldDownGroup = itemCopy.coldDownGroup;
        price = itemCopy.price;
        sale = itemCopy.sale;
        taskFlag = itemCopy.taskFlag;
        taskMuti = itemCopy.taskMuti;
        iconIndex = itemCopy.iconIndex;
        time = itemCopy.time;
        distance = itemCopy.distance;
        schedule = itemCopy.schedule;
        title = itemCopy.title;
        timeType = itemCopy.timeType;
        type = itemCopy.type;
        batchUseFlag = itemCopy.batchUseFlag;
        categoryName = itemCopy.categoryName;
        
        effects.clear();
        for (int i = 0; i < itemCopy.effects.size(); i++) {
            effects.add(itemCopy.effects.get(i));
        }
        
        isHorseJewel = itemCopy.isHorseJewel;
        jewelAttrType = itemCopy.jewelAttrType;
        jewelType = itemCopy.jewelType;
        isFlaw = itemCopy.isFlaw;
    }

    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
    
    public String toString() {
        return id + ":" + title;
    }

    public boolean equals(Object obj){
        if(obj instanceof Item){
            return ((Item)obj).id == id;
        }
        return false;
    }
    
    /**
     * 计算宝石的附加属性值。
     * @return
     */
    public int calcJewelAttr() {
        return AttributeCalculator.calcJewelAttr(level, 1.0f, jewelAttrType, true);
    }
    
    /**
     * 计算宝石的附加属性值（多属性版本）。
     */
    public int[][] calcJewelAttrs() {
        JewelConfig.JewelAttr[] attrs = owner.jewelConfig.jewelAttrs.get(jewelAttrType);
        List<int[]> list = new ArrayList<int[]>();
        for (int i = 0; i < attrs.length; i++) {
            int[] tmp = new int[2];
            tmp[0] = attrs[i].attrType;
            tmp[1] = AttributeCalculator.calcJewelAttr(level, attrs[i].attrRatio, attrs[i].attrType, true);
            if (tmp[1] > 0) {
                list.add(tmp);
            }
        }
        int[][] ret = new int[list.size()][];
        list.toArray(ret);
        return ret;
    }
    
    /**
     * 重置宝石描述信息。
     */
    public void resetDescription() {
        int[][] attrs = calcJewelAttrs();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attrs.length; i++) {
            sb.append(AttributeCalculator.ATTRIBUTES[attrs[i][0]].shortName);
            sb.append(" +");
            sb.append(attrs[i][1]);
            sb.append("，");
        }
        sb.append(Item.JEWEL_TYPE_NAMES[jewelType]);
        sb.append("。");
        if (playerLevel < 7) {
            if ("flash".equals(owner.config.getProperty("project_type"))) {
                sb.append("使用打造中的宝石合成功能可以把3-5个此宝石合成1个高一级别的宝石。");
            } else {
                if(this.isFlaw == true){
                    sb.append("有瑕疵的宝石没有合成功能");
                } else {
                    sb.append("到都城的珠宝工匠处可以将3-5个此宝石合成1个高一级别的宝石。");
                }
            }
        }
        description = sb.toString();
    }
}
