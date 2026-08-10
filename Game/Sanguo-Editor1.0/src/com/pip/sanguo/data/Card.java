package com.pip.sanguo.data;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import org.jdom.Document;
import org.jdom.Element;

import com.pip.sanguo.data.item.Item;

/**
 * 
 * @author zlguo
 * 
 */
public class Card extends DataObject {
    /**
     * 所属项目。
     * */
    public ProjectData owner;
    /**
     * 卡片类型
     */
    public int type;
    /**
     * 系列id
     */
    public int suiteId;
    /**
     * 位置ID
     */
    public int holeId;
    /**
     * 收藏出现概率(%)
     */
    public int rate;
    /**
     * 卡片星级
     */
    public int star;
    /**
     * 卡片品质类型（0普通卡1稀有卡）
     */
    public int quality;
    /**
     * 是否可以使用
     */
    public boolean canUse;
    /**
     * 对应物品id
     */
    public int itemId;
    /**
     * 资源名称
     */
    public String res = "";
    /**
     * 卡片合成材料或条件集合
     */
    public Material[] materials = null;
    /**
     * 卡片掉落对象集合
     */
    public DropObject[] dropObjects = null;
    /**
     * 卡片能量值下限
     */
    public int energy_min = 0;
    /**
     * 卡片能量值上限
     */
    public int energy_max = 0;
    /**
     * 卡片效果BUFF
     */
    public int buffId = 0;
    /**
     *  普通卡片BUFF级别
     */
    public int buffLevel1 = 1;
    /**
     *  闪卡BUFF级别
     */
    public int buffLevel2 = 2;
    /**
     * 卡片效果BUFF持续时间（小时）
     */
    public int buffDuration = 0;
    
    /**卡片技能buff*/
    public int buff2Id=-1;

    // 常量
    /**
     * 卡片类型名称
     */
    public static final String[] TYPE_NAMES = { "名将卡", "美女卡", "江山卡", "宝马卡", "武器卡", "谋略卡", "瑞兽卡", "怪兽卡", "宝物卡", "特品卡",
            "机械卡", "阵法卡", "事件卡", "兵种卡", "天气卡", "妖术卡", "仙术卡" };
    // 卡片类型
    /**
     * 名将卡
     */
    public static final int TYPE_HERO = 0;
    /**
     * 美女卡
     */
    public static final int TYPE_BEAUTY = 1;
    /**
     * 江山卡
     */
    public static final int TYPE_LANDSCAPE = 2;
    /**
     * 宝马卡
     */
    public static final int TYPE_HORSE = 3;
    /**
     * 武器卡
     */
    public static final int TYPE_WEAPON = 4;
    /**
     * 策略卡
     */
    public static final int TYPE_STRATEGY = 5;
    /**
     * 瑞兽卡
     */
    public static final int TYPE_BEAST = 6;
    /**
     * 怪兽卡
     */
    public static final int TYPE_MONSTERS = 7;
    /**
     * 宝物卡
     */
    public static final int TYPE_TREASURE = 8;
    /**
     * 特品卡
     */
    public static final int TYPE_SPECIAL = 9;
    /**
     * 机械卡
     */
    public static final int TYPE_MACHINE = 10;
    /**
     * 阵法卡
     */
    public static final int TYPE_ZHEN = 11;
    /**
     * 事件卡
     */
    public static final int TYPE_EVENT = 12;
    /**
     * 兵种卡
     */
    public static final int TYPE_PROFESSION = 13;
    /**
     * 天气卡
     */
    public static final int TYPE_WEATHER = 14;
    /**
     * 妖术卡
     */
    public static final int TYPE_MAGIC = 15;
    /**
     * 仙术卡
     */
    public static final int TYPE_XIANSHU = 16;
    // 卡片品质类型
    /**
     * 卡片品质
     */
    public static final String[] QUALITY_NAMES = { "普通卡", "闪卡" };
    /**
     * 普通卡
     */
    public static final int QUALITY_COMMON = 0;
    /**
     * 稀有卡（闪卡）
     */
    public static final int QUALITY_GLARE = 1;
    // 合成材料名称
    /**
     * 合成材料名称
     */
    public static final String[] MATERIAL_NAMES = { "卡片(扣除背包卡片物品)", "扣除物品(非卡片)", "扣除金钱", "需要声望", "达到等级", "达到军衔",
            "达到战功", "具有称号" };
    /**
     * 卡片
     */
    public static final int MATERIAL_TYPE_CARD = 0;
    /**
     * 物品
     */
    public static final int MATERIAL_TYPE_ITEM = 1;
    /**
     * 金钱
     */
    public static final int MATERIAL_TYPE_MONEY = 2;
    /**
     * 声望
     */
    public static final int MATERIAL_TYPE_FAME = 3;
    /**
     * 等级
     */
    public static final int MATERIAL_TYPE_LEVEL = 4;
    /**
     * 军衔
     */
    public static final int MATERIAL_TYPE_MILITARY_RANK = 5;
    /**
     * 战功
     */
    public static final int MATERIAL_TYPE_CONTRIBUTION = 6;
    /**
     * 称号
     */
    public static final int MATERIAL_TYPE_TITLE = 7;
    /**
     * 掉落项目名称
     */
    public static final String[] DROP_NAMES = { "掉落物品", "掉落金钱", "增加经验", "增加声望", "更改军衔", "增加战功", "增加称号", "掉落组" };
    /**
     * 物品
     */
    public static final int DROP_TYPE_ITEM = 0;
    /**
     * 金钱
     */
    public static final int DROP_TYPE_MONEY = 1;
    /**
     * 经验
     */
    public static final int DROP_TYPE_EXP = 2;
    /**
     * 声望
     */
    public static final int DROP_TYPE_FAME = 3;
    /**
     * 军衔
     */
    public static final int DROP_TYPE_MILITARY_RANK = 4;
    /**
     * 战功
     */
    public static final int DROP_TYPE_CONTRIBUTION = 5;
    /**
     * 称号
     */
    public static final int DROP_TYPE_TITLE = 6;
    /**
     * 掉落组
     */
    public static final int DROP_TYPE_DROP_GROUP = 7;
    /**
     * 卡片星级名称
     */
    public static final String[] STAR_NAMES = { "1星", "2星", "3星", "4星", "5星", "6星", "7星", "8星", "9星" };
    /**
     * 卡片是否可以使用
     */
    public static final String[] CANUSE_NAMES = { "否", "是" };
    /**
     * NO
     */
    public static final int CANUSE_NO = 0;
    /**
     * YES
     */
    public static final int CANUSE_YES = 1;
    /**
     * 卡片目录
     */
    public static final String[] PATH_NAMES = {
            File.separatorChar + "Cards" + File.separatorChar + "176x208" + File.separatorChar,
            File.separatorChar + "Cards" + File.separatorChar + "240x320" + File.separatorChar };
    public static final int PATH_176x208 = 0;
    public static final int PATH_240x320 = 1;

    public static Document root;

    public static Map<Integer, String> suiteIds;
    public static Map<Integer, String> cardIds;
    
    //卡片属性类型
    public int prorertyType;
    
    //buffid
    public static final int[] buffIds = new int[]{443,441,444,445,446,447,448};
    
    //typeId
    public static final int[] buffTypeId = new int[]{0,1,2,3,4,5,6,7,8};
    
    //按照属性分类
    public static final String[] PROPERTY_TYPE_NAMES = {"力量", "敏捷", "智力", "命中","暴击","物攻","法攻","体力","技能"};
    //目前不需要发送的属性
    public static final String UNUSE_PROPERTY_TYPE="3456";
    
    public static final int PROPERTY_TYPE_SKILL=8;//技能卡片
    
    /** 属性基础值 */
    public int propertyBaseValue = 0;
    
    /** 成长属性价值 */
    public int propertyUpLevelValue = 0;
    
    protected static int[] CARDPROPERTYBASEVALUE = {1, 2, 4 ,8, 15, 15};
    protected static int[] CARDPROPERTYUPLEVELEVALUE = {1, 2, 4 ,6, 9, 9};
    
    public Card(ProjectData owner) {
        this.owner = owner;
    }
    
    /**
     * 根据buffId获得buff类型
     */
    public int getBufftypeById(int buffId){
        for(int i = 0; i < buffIds.length; i++){
            if(buffIds[i] == buffId){
                return buffTypeId[i];
            }
        }
        return 0;
    }
    
    /**
     * 根据buffId取卡片类型名
     */
    public String getProrertyTypeByBuffId(int buffId){
        for(int i = 0;i < buffIds.length;i++){
            if(buffIds[i] == buffId){
                return PROPERTY_TYPE_NAMES[i];
            }
        }
        return "";
    }

    @Override
    public boolean changed(DataObject obj) {
        Card c = (Card) obj;
        if (c.canUse != this.canUse) {
            return true;
        }
        if (c.categoryName != this.categoryName) {
            return true;
        }
        if (c.description != this.description) {
            return true;
        }
        if (c.id != this.id) {
            return true;
        }
        if (c.itemId != this.itemId) {
            return true;
        }
        if (c.quality != this.quality) {
            return true;
        }
        if (c.rate != this.rate) {
            return true;
        }
        if (c.star != this.star) {
            return true;
        }
        if (c.suiteId != this.suiteId) {
            return true;
        }
        if (c.holeId != this.holeId) {
            return true;
        }
        if (c.type != this.type) {
            return true;
        }
        if (c.res != this.res) {
            return true;
        }
        if (c.energy_min != this.energy_min){
            return false;
        }
        if (c.energy_max != this.energy_max){
            return false;
        }
        if (c.buffId != this.buffId){
            return false;
        }
        if (c.buffLevel1 != this.buffLevel1){
            return false;
        }
        if (c.buffLevel2 != this.buffLevel2){
            return false;
        }
        if (c.buffDuration != this.buffDuration){
            return false;
        }
        if (c.materials.length != this.materials.length) {
            return true;
        }
        if (c.dropObjects.length != this.dropObjects.length) {
            return true;
        }
        for (int i = 0; i < materials.length; i++) {
            if (materials[i].type != c.materials[i].type) {
                return true;
            }
            if (materials[i].itemId != c.materials[i].itemId) {
                return true;
            }
            if (materials[i].value != c.materials[i].value) {
                return true;
            }
        }
        for (int i = 0; i < dropObjects.length; i++) {
            if (dropObjects[i].type != c.dropObjects[i].type) {
                return true;
            }
            if (dropObjects[i].itemId != c.dropObjects[i].itemId) {
                return true;
            }
            if (dropObjects[i].value != c.dropObjects[i].value) {
                return true;
            }
            if (dropObjects[i].value != c.dropObjects[i].value) {
                return true;
            }
        }
        if(c.prorertyType != this.prorertyType){
            return false;
        }
        if(c.propertyBaseValue != this.propertyBaseValue){
            return true;
        }
        if(c.propertyUpLevelValue != this.propertyUpLevelValue){
            return true;
        }
        return false;
    }

    @Override
    public boolean depends(DataObject obj) {
        return false;
    }

    @Override
    public DataObject duplicate() {
        Card ret = new Card(owner);
        ret.update(this);
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        type = Integer.parseInt(elem.getAttributeValue("type"));
        title = elem.getAttributeValue("title");
        categoryName = elem.getAttributeValue("category");
        description = elem.getAttributeValue("description");
        holeId = Integer.parseInt(elem.getAttributeValue("holeId"));
        suiteId = Integer.parseInt(elem.getAttributeValue("suiteId"));
        quality = Integer.parseInt(elem.getAttributeValue("quality"));
        rate = Integer.parseInt(elem.getAttributeValue("rate"));
        star = Integer.parseInt(elem.getAttributeValue("star"));
        canUse = "true".equals(elem.getAttributeValue("canUse")) ? true : false;
        itemId = Integer.parseInt(elem.getAttributeValue("itemId"));
        res = elem.getAttributeValue("res");
        try {
            energy_min = Integer.parseInt(elem.getAttributeValue("energy_min"));
        }
        catch (NumberFormatException e) {
        }
        try {
            energy_max = Integer.parseInt(elem.getAttributeValue("energy_max"));
        }
        catch (NumberFormatException e) {
        }
        try {
            buffId = Integer.parseInt(elem.getAttributeValue("buffId"));
        }
        catch (NumberFormatException e) {
        }
        if(elem.getAttributeValue("property_type") != null){
            prorertyType = Integer.parseInt(elem.getAttributeValue("property_type"));
        }else{
            prorertyType = getBufftypeById(buffId);
        }
        try {
            buffLevel1 = Integer.parseInt(elem.getAttributeValue("bufflevel1"));
        }
        catch (NumberFormatException e) {
        }
        try {
            buffLevel2 = Integer.parseInt(elem.getAttributeValue("bufflevel2"));
        }
        catch (NumberFormatException e) {
        }
        try {
            buffDuration = Integer.parseInt(elem.getAttributeValue("buffduration"));
        }
        catch (NumberFormatException e) {
        }
        List<Element> mts = elem.getChildren("material");
        materials = new Material[mts.size()];
        for (int j = 0; j < mts.size(); j++) {
            Element mate = mts.get(j);
            materials[j] = new Material();
            Material material = materials[j];
            material.type = Integer.parseInt(mate.getAttributeValue("type"));
            material.itemId = Integer.parseInt(mate.getAttributeValue("itemId"));
            material.value = Integer.parseInt(mate.getAttributeValue("value"));
            material.name = mate.getAttributeValue("name");
        }
        List<Element> drops = elem.getChildren("drop");
        dropObjects = new DropObject[drops.size()];
        for (int j = 0; j < drops.size(); j++) {
            Element drop = drops.get(j);
            dropObjects[j] = new DropObject();
            DropObject dobj = dropObjects[j];
            dobj.type = Integer.parseInt(drop.getAttributeValue("type"));
            dobj.itemId = Integer.parseInt(drop.getAttributeValue("itemId"));
            dobj.value = Integer.parseInt(drop.getAttributeValue("value"));
            dobj.rate = Integer.parseInt(drop.getAttributeValue("rate"));
            dobj.name = drop.getAttributeValue("name");
        }
        if (root == null) {
            root = elem.getDocument();
        }
        try {
            propertyBaseValue = Integer.parseInt(elem.getAttributeValue("propertybasevalue"));
        }catch(Exception e){
            propertyBaseValue = getDefaultPropertyBaseValue();
        }
        try {
            propertyUpLevelValue = Integer.parseInt(elem.getAttributeValue("propertyuplevelvalue"));
        }catch(Exception e){
            propertyUpLevelValue = getDefaultPropertyUpLevelValue();
        }
        try {
            buff2Id = Integer.parseInt(elem.getAttributeValue("buff2Id"));
        }
        catch (NumberFormatException e) {
        }
        // Cards tag
        //System.gc();
    }

    @Override
    public Element save() {
        Element ret = new Element("card");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("type", String.valueOf(type));
        ret.addAttribute("property_type", String.valueOf(prorertyType));
        ret.addAttribute("title", title);
        ret.addAttribute("description", description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        else {
            ret.addAttribute("category", "");
        }
        ret.addAttribute("holeId", String.valueOf(holeId));
        ret.addAttribute("suiteId", String.valueOf(suiteId));
        ret.addAttribute("quality", String.valueOf(quality));
        ret.addAttribute("rate", String.valueOf(rate));
        ret.addAttribute("star", String.valueOf(star));
        ret.addAttribute("canUse", String.valueOf(canUse));
        ret.addAttribute("itemId", String.valueOf(itemId));
        ret.addAttribute("res", res);
        ret.addAttribute("energy_min", String.valueOf(energy_min));
        ret.addAttribute("energy_max", String.valueOf(energy_max));
        ret.addAttribute("buffId", String.valueOf(buffId));
        ret.addAttribute("bufflevel1", String.valueOf(buffLevel1));
        ret.addAttribute("bufflevel2", String.valueOf(buffLevel2));
        ret.addAttribute("buffduration", String.valueOf(buffDuration));
        for (int j = 0; j < materials.length; j++) {
            Element m = new Element("material");
            m.addAttribute("type", String.valueOf(materials[j].type));
            m.addAttribute("itemId", String.valueOf(materials[j].itemId));
            m.addAttribute("value", String.valueOf(materials[j].value));
            m.addAttribute("name", materials[j].name);
            ret.addContent(m);
        }
        for (int j = 0; j < dropObjects.length; j++) {
            Element d = new Element("drop");
            d.addAttribute("type", String.valueOf(dropObjects[j].type));
            d.addAttribute("itemId", String.valueOf(dropObjects[j].itemId));
            d.addAttribute("value", String.valueOf(dropObjects[j].value));
            d.addAttribute("rate", String.valueOf(dropObjects[j].rate));
            d.addAttribute("name", dropObjects[j].name);
            ret.addContent(d);
        }
        ret.addAttribute("propertybasevalue", String.valueOf(propertyBaseValue));
        ret.addAttribute("propertyuplevelvalue", String.valueOf(propertyUpLevelValue));
        ret.addAttribute("buff2Id", String.valueOf(buff2Id));
        return ret;
    }

    @Override
    public void update(DataObject obj) {
        Card oo = (Card) obj;
        id = oo.id;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        rate = oo.rate;
        type = oo.type;
        canUse = oo.canUse;
        itemId = oo.itemId;
        suiteId = oo.suiteId;
        star = oo.star;
        holeId = oo.holeId;
        res = oo.res;
        energy_min = oo.energy_min;
        energy_max = oo.energy_max;
        buffId = oo.buffId;
        buffLevel1 = oo.buffLevel1;
        buffLevel2 = oo.buffLevel2;
        prorertyType = oo.prorertyType;
        propertyBaseValue = oo.propertyBaseValue;
        propertyUpLevelValue = oo.propertyUpLevelValue;
        buff2Id = oo.buff2Id;
        buffDuration = oo.buffDuration;
        quality = oo.quality;
        materials = new Material[oo.materials.length];
        System.arraycopy(oo.materials, 0, materials, 0, materials.length);
        dropObjects = new DropObject[oo.dropObjects.length];
        System.arraycopy(oo.dropObjects, 0, dropObjects, 0, dropObjects.length);
        System.gc();
    }

    @Override
    public String toString() {
        return title;
    }

    public Material newMaterial() {
        return new Material();
    }

    public DropObject newDropObject() {
        return new DropObject();
    }

    /**
     * 卡片合成材料
     * 
     * @author zlguo
     * 
     */
    public class Material implements Comparable<Material> {
        /**
         * 材料类型
         */
        public int type;
        /**
         * 如果是物品，此字段有效
         */
        public int itemId;
        /**
         * 材料数值
         */
        public int value;
        /**
         * 扩展字段
         */
        public String name;

        public int compareTo(Material o) {
            if (this.type < o.type) {
                return -1;
            }
            else if (this.type > o.type) {
                return 1;
            }
            else {
                if (this.itemId < o.itemId) {
                    return -1;
                }
                else if (this.itemId > o.itemId) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
        }
    }

    /**
     * 卡片掉落对象
     * 
     * @author zlguo
     * 
     */
    public class DropObject extends Material {
        /**
         * 掉落几率(%)
         */
        public int rate;
    }

    /**
     * 得到卡片id组成的配方关键字
     * 
     * @return
     */
    public String getFormulaStr() {
        //经过排序的
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < materials.length; i++) {
            if(materials[i].type == Card.MATERIAL_TYPE_CARD){
                sb.append(materials[i].itemId);
                sb.append(',');
            }
        }
        String ret = sb.toString();
        if(ret.length() > 0){
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }
    /**
     * 得到卡片配方描述
     * @return
     */
    public String getFormulaDesc(){
        //经过排序的
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < materials.length; i++) {
            if(materials[i].type == Card.MATERIAL_TYPE_CARD){
                sb.append(MessageFormat.format("{0}卡{1}张", materials[i].name,materials[i].value));
//                sb.append(materials[i].name+"卡"+materials[i].value+"张");
                sb.append(" + ");
            }
        }
        String ret = sb.toString();
        if(ret.length() > 0){
            ret = ret.substring(0, ret.length() - 3);
        }
        return ret;
    }
    
    public int getDefaultPropertyBaseValue(){
        Item item = this.owner.findItem(itemId);
        if(item!=null){
            int quality = item.quality;
            return CARDPROPERTYBASEVALUE[quality];
        }
        return 0;
    }
    
    public int getDefaultPropertyUpLevelValue(){
        Item item = this.owner.findItem(itemId);
        if(item!=null){
            int quality = item.quality;
            return CARDPROPERTYUPLEVELEVALUE[quality];
        }
        return 0;
    }
    
}
