package com.pip.sanguo.data.item;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Shop.BuyRequirement;
import com.pip.sanguo.data.skill.SkillConfig;

/**
 * 物品制造公式。
 */
public class Formula extends DataObject {
    public static final int PRODUCT_ITEM = 0;
    public static final int PRODUCT_DROPGROUP = 1;
    
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * 技能级别。
     */
    public int level;
    /**
     * 消耗行动力。
     */
    public int movePoint;
    /**
     * 制造需求 
     */
    public List<BuyRequirement> requirements = new ArrayList<BuyRequirement>();
    /**
     * 产品类型。
     */
    public int productType = PRODUCT_ITEM;
    /**
     * 最小出品量。
     */
    public int minAmount = 1;
    /**
     * 最大出品量。
     */
    public int maxAmount = 1;
    /**
     * 掉落组ID（如果产品类型为掉落组时使用）
     */
    public int groupID;
    /**
     * 物品ID（如果产品类型为物品，则指定物品ID；如果产品类型为掉落组，则指定用来显示结果的物品）。
     */
    public int itemID;

    public Formula(ProjectData owner) {
        this.owner = owner;
    }

    public boolean depends(DataObject obj) {
        return false;
    }

    public DataObject duplicate() {
        Formula copy = new Formula(owner);
        copy.update(this);
        return copy;
    }

    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
    
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        description = elem.getAttributeValue("description");
        categoryName = elem.getAttributeValue("category");
        if (categoryName == null) {
            categoryName = "";
        }

        level = Integer.parseInt(elem.getAttributeValue("level"));
        try {
            movePoint = Integer.parseInt(elem.getAttributeValue("movepoint"));
        } catch (Exception e) {
        }

        // 需求物品
        List reqElems = elem.getChildren("requirement");
        for (int i = 0; i < reqElems.size(); i++) {
            BuyRequirement req = BuyRequirement.load(owner, (Element)reqElems.get(i));
            if (req != null ){
                requirements.add(req);
            }
        }
        
        // 产品信息
        if ("dropgroup".equals(elem.getAttributeValue("producttype"))) {
            this.productType = PRODUCT_DROPGROUP;
        } else {
            this.productType = PRODUCT_ITEM;
        }
        minAmount = Integer.parseInt(elem.getAttributeValue("minamount"));
        maxAmount = Integer.parseInt(elem.getAttributeValue("maxamount"));
        itemID = Integer.parseInt(elem.getAttributeValue("itemid"));
        groupID = Integer.parseInt(elem.getAttributeValue("groupid"));
    }

    public Element save() {
        Element ret = new Element("formula");
        
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("description", description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        ret.addAttribute("level", String.valueOf(level));
        ret.addAttribute("movepoint", String.valueOf(movePoint));
        
        // 需求物品
        for (BuyRequirement br : requirements) {
            Element elem = br.save();
            if (elem != null) {
                ret.addContent(elem);
            }
        }
        
        // 产品信息
        if (productType == PRODUCT_DROPGROUP) {
            ret.addAttribute("producttype", "dropgroup");
        } else {
            ret.addAttribute("producttype", "item");
        }
        ret.addAttribute("minamount", String.valueOf(minAmount));
        ret.addAttribute("maxamount", String.valueOf(maxAmount));
        ret.addAttribute("itemid", String.valueOf(itemID));
        ret.addAttribute("groupid", String.valueOf(groupID));
        return ret;
    }

    public void update(DataObject obj) {
        Formula itemCopy = (Formula) obj;
        id = itemCopy.id;
        title = itemCopy.title;
        description = itemCopy.description;
        categoryName = itemCopy.categoryName;
        level = itemCopy.level;
        movePoint = itemCopy.movePoint;
        
        requirements.clear();
        for (BuyRequirement br : itemCopy.requirements) {
            requirements.add(br.dup());
        }
        
        productType = itemCopy.productType;
        minAmount = itemCopy.minAmount;
        maxAmount = itemCopy.maxAmount;
        itemID = itemCopy.itemID;
        groupID = itemCopy.groupID;
        
        if (owner != itemCopy.owner) {
            for (BuyRequirement br : requirements) {
                br.switchProject(owner);
            }
        }
    }
    
    public String toString() {
        return id + ":" + title;
    }

    public boolean equals(Object obj){
        if(obj instanceof Formula){
            return ((Formula)obj).id == id;
        }
        return false;
    }
    
    /**
     * 根据配方ID查找配方的名字。
     * @return
     */
    public static String toString(ProjectData project, int id) {
        Formula ret = (Formula)project.findObject(Formula.class, id);
        if (ret == null) {
            return "无效配方";
        }
        return ret.toString();
    }
}
