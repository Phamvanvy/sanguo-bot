package com.pip.sanguo.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Text;
import org.jdom.*;

import com.pip.sanguo.data.item.DropNode;
import com.pip.sanguo.editor.ai.AIRuleConfig;
import com.pip.sanguo.editor.util.Constants;

/**
 * 一个NPC的描述信息。NPC可能是一个友方目标，也可能是一只怪物。
 */
public class NPCTemplate extends DataObject {
    /** 所属项目。*/
    public ProjectData owner;
    /** NPC动画。*/
    public Animation image;
    /** NPC类型。*/
    public NPCType type;
    /** NPC职业 */
    public int clazz;
    /** 是否精英怪物 */
    public boolean powerful;
    /** 是否填队模式 */
    public boolean partyModel;
    /** 级别 */
    public int level;
    /** 生命 */
    public int hp;
    /** 法力 */
    public int mp;
    /** 护甲 */
    public int armor;
    /** 法防 */
    public int magicArmor;
    /** 耐力 */
    public int sta;
    /** 力量 */
    public int str;
    /** 敏捷 */
    public int agi;
    /** 智力 */
    public int inte;
    /** 武器攻击下限 */
    public int weaponAP1;
    /** 武器攻击上限 */
    public int weaponAP2;
    /** 武器法术攻击 */
    public int weaponMagicAP;
    /** 怪物掉落经验值 */
    public int exp;
    /** 怪物掉落金钱 */
    public int money;
    /** 怪物掉落声望 */
    public int credit;
    /** 怪物掉落战功 */
    public int rank;
    /** 填队模式增幅 */
    public int partyModelRatio;
    /** AI类 */
    public String aiClass = "";
    /** AI规则 */
    public List<AIRuleConfig> aiRules = new ArrayList<AIRuleConfig>();
    /** 追击速度 */
    public int speed;
    /** 巡逻速度 */
    public int walkSpeed;
    /** 视野 */
    public int eyeshot;
    /** 追击范围 */
    public int chaseDistance;
    /** 采集参数 */
    public int collectParam;
    /** 任务id */
    public int questId;
    /** 采集时间 */
    public int collectTime = 3000;
    /** 掉落组 */
    public List<DropNode> dropGroups = new ArrayList<DropNode>();

    public NPCTemplate(ProjectData owner) {
        this.owner = owner;
    }

    public int getID() {
        return id;
    }

    public String toString() {
        return id + ": " + title;
    }

    public boolean equals(Object o) {
        return this == o;
    }

    public void update(DataObject obj) {
        NPCTemplate oo = (NPCTemplate)obj;
        id = oo.id;
        image = oo.image;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        
        type = oo.type;
        clazz = oo.clazz;
        powerful = oo.powerful;
        partyModel = oo.partyModel;
        hp = oo.hp;
        mp = oo.mp;
        armor = oo.armor;
        magicArmor = oo.magicArmor;
        level = oo.level;
        sta = oo.sta;
        str = oo.str;
        agi = oo.agi;
        inte = oo.inte;
        weaponAP1 = oo.weaponAP1;
        weaponAP2 = oo.weaponAP2;
        weaponMagicAP = oo.weaponMagicAP;
        aiClass = oo.aiClass;
        this.aiRules = new ArrayList<AIRuleConfig>();
        for (AIRuleConfig rc : oo.aiRules) {
            this.aiRules.add(rc.duplicate());
        }
        speed = oo.speed;
        walkSpeed = oo.walkSpeed;
        eyeshot = oo.eyeshot;
        chaseDistance = oo.chaseDistance;
        collectParam = oo.collectParam;
        collectTime = oo.collectTime;
        questId = oo.questId;
        exp = oo.exp;
        money = oo.money;
        credit = oo.credit;
        rank = oo.rank;
        partyModelRatio = oo.partyModelRatio;
        
        dropGroups.clear();
        for(DropNode dropNode : oo.dropGroups){
            dropGroups.add(dropNode);
        }
        
        if (owner != oo.owner) {
            if (image != null) {
                image = (Animation)owner.findObject(Animation.class, image.id);
            }
            if (type != null) {
                type = (NPCType)owner.findDictObject(NPCType.class, type.id);
            }
        }
    }
    
    public DataObject duplicate() {
        NPCTemplate ret = new NPCTemplate(owner);
        ret.update(this);
        return ret;
    }

    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
    
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        description = elem.getAttributeValue("description");
        if (description == null) {
            description = "";
        }
        categoryName = elem.getAttributeValue("category");
        if (categoryName == null) {
            categoryName = "";
        }
        
        int typeID = Integer.parseInt(elem.getAttributeValue("type"));
        type = (NPCType)owner.findDictObject(NPCType.class, typeID);
        
        image = (Animation)owner.findObject(Animation.class, Integer.parseInt(elem.getAttributeValue("image")));
        if(typeID != 3){
            clazz = Integer.parseInt(elem.getAttributeValue("clazz"));
            powerful = "1".equals(elem.getAttributeValue("powerful"));
            partyModel = "1".equals(elem.getAttributeValue("partyModel"));
            level = Integer.parseInt(elem.getAttributeValue("level"));
            hp = Integer.parseInt(elem.getAttributeValue("hp")) + getStandardHP();
            mp = Integer.parseInt(elem.getAttributeValue("mp")) + getStandardMP();
            armor = Integer.parseInt(elem.getAttributeValue("armor")) + getStandardArmor();
            magicArmor = Integer.parseInt(elem.getAttributeValue("magicarmor")) + getStandardMagicArmor();
            sta = Integer.parseInt(elem.getAttributeValue("sta")) + getStandardSTA();
            str = Integer.parseInt(elem.getAttributeValue("str")) + getStandardSTR();
            agi = Integer.parseInt(elem.getAttributeValue("agi")) + getStandardAGI();
            inte = Integer.parseInt(elem.getAttributeValue("int")) + getStandardINT();
            weaponAP1 = Integer.parseInt(elem.getAttributeValue("weaponap1")) + getStandardWeaponAP1();
            weaponAP2 = Integer.parseInt(elem.getAttributeValue("weaponap2")) + getStandardWeaponAP2();
            weaponMagicAP = Integer.parseInt(elem.getAttributeValue("weaponmagicap")) + getStandardWeaponMagicAP();
            
            speed = Integer.parseInt(elem.getAttributeValue("speed"));
            try {
                walkSpeed = Integer.parseInt(elem.getAttributeValue("walkspeed"));
            } catch (Exception e) {
                walkSpeed = speed / 8;
            }
            eyeshot = Integer.parseInt(elem.getAttributeValue("eyeshot"));
            chaseDistance = Integer.parseInt(elem.getAttributeValue("chasedistance"));
            exp = Integer.parseInt(elem.getAttributeValue("exp")) + getStandardExp();
            money = Integer.parseInt(elem.getAttributeValue("money")) + getStandardMoney();
            try {
                credit = Integer.parseInt(elem.getAttributeValue("credit"));
                rank = Integer.parseInt(elem.getAttributeValue("rank"));
                partyModelRatio = Integer.parseInt(elem.getAttributeValue("partymodelratio"));
            } catch (Exception e) {
            }

            aiRules.clear();
            Element aiElem = elem.getChild("ai");
            if (aiElem != null) {
                aiClass = aiElem.getAttributeValue("class");
                List ruleElems = aiElem.getChildren("rule");
                for (int i = 0; i < ruleElems.size(); i++) {
                    AIRuleConfig rc = AIRuleConfig.load((Element)ruleElems.get(i));
                    aiRules.add(rc);
                }
            }
        } else{
            collectParam = Integer.parseInt(elem.getAttributeValue("collectParam"));
            questId = Integer.parseInt(elem.getAttributeValue("questId"));
            collectTime = Integer.parseInt(elem.getAttributeValue("collectTime"));
            exp = Integer.parseInt(elem.getAttributeValue("exp"));
            money = Integer.parseInt(elem.getAttributeValue("money"));
            try{
                level = Integer.parseInt(elem.getAttributeValue("level"));
            }catch (Exception e){
                
            }
            try {
                credit = Integer.parseInt(elem.getAttributeValue("credit"));
                rank = Integer.parseInt(elem.getAttributeValue("rank"));
                partyModelRatio = Integer.parseInt(elem.getAttributeValue("partymodelratio"));
            } catch (Exception e) {
            }
        }
           
        List<Element> children = elem.getChildren("dropnode");
        for (Element child : children) {
            DropNode node = new DropNode();
            node.load(child);
            dropGroups.add(node);
        }
    }
    
    public Element save() {
        Element ret = new Element("npc");
        ret.addAttribute("id", String.valueOf(id));
        if (image == null) {
            ret.addAttribute("image", "-1");
        } else {
            ret.addAttribute("image", String.valueOf(image.id));
        }
        ret.addAttribute("title", title);
        ret.addAttribute("description",description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        if (type == null) {
            ret.addAttribute("type", "-1");
        } else {
            ret.addAttribute("type", String.valueOf(type.id));
        }
        
        if(type != null && type.id == 3){
            ret.addAttribute("collectTime", String.valueOf(collectTime));
            ret.addAttribute("collectParam", String.valueOf(collectParam));
            ret.addAttribute("questId", String.valueOf(questId));
            ret.addAttribute("exp", String.valueOf(exp));
            ret.addAttribute("money", String.valueOf(money));
            ret.addAttribute("credit", String.valueOf(credit));
            ret.addAttribute("rank", String.valueOf(rank));
            ret.addAttribute("partymodelratio", String.valueOf(partyModelRatio));
            ret.addAttribute("level", String.valueOf(level));
        }
        else{
            ret.addAttribute("clazz", String.valueOf(clazz));
            ret.addAttribute("powerful", powerful ? "1" : "0");
            ret.addAttribute("partyModel", partyModel ? "1" : "0");
            ret.addAttribute("level", String.valueOf(level));
            ret.addAttribute("hp", String.valueOf(hp - getStandardHP()));
            ret.addAttribute("mp", String.valueOf(mp - getStandardMP()));
            ret.addAttribute("armor", String.valueOf(armor - getStandardArmor()));
            ret.addAttribute("magicarmor", String.valueOf(magicArmor - getStandardMagicArmor()));
            ret.addAttribute("sta", String.valueOf(sta - getStandardSTA()));
            ret.addAttribute("str", String.valueOf(str - getStandardSTR()));
            ret.addAttribute("agi", String.valueOf(agi - getStandardAGI()));
            ret.addAttribute("int", String.valueOf(inte - getStandardINT()));
            ret.addAttribute("weaponap1", String.valueOf(weaponAP1 - getStandardWeaponAP1()));
            ret.addAttribute("weaponap2", String.valueOf(weaponAP2 - getStandardWeaponAP2()));
            ret.addAttribute("weaponmagicap", String.valueOf(weaponMagicAP - getStandardWeaponMagicAP()));
            ret.addAttribute("speed", String.valueOf(speed));
            ret.addAttribute("walkspeed", String.valueOf(walkSpeed));
            ret.addAttribute("eyeshot", String.valueOf(eyeshot));
            ret.addAttribute("chasedistance", String.valueOf(chaseDistance));
            ret.addAttribute("exp", String.valueOf(exp - getStandardExp()));
            ret.addAttribute("money", String.valueOf(money - getStandardMoney()));
            ret.addAttribute("credit", String.valueOf(credit));
            ret.addAttribute("rank", String.valueOf(rank));
            ret.addAttribute("partymodelratio", String.valueOf(partyModelRatio));

            Element aiElem = new Element("ai");
            aiElem.addAttribute("class", aiClass);
            for (AIRuleConfig rc : aiRules) {
                aiElem.addContent(rc.toXML());
            }
            ret.addContent(aiElem);
        }
        
        if (dropGroups != null && dropGroups.size() > 0) {
            for (DropNode node : dropGroups) {
                ret.getMixedContent().add(node.save());
            }
        }
        return ret;
    }
    
    public boolean depends(DataObject obj) {
        return obj == image;
    }
    
    /**
     * 根据模板ID查找一个模板并取得模板名称。
     * @param project
     * @param templateID
     * @return
     */
    public static String toString(ProjectData project, int templateID) {
        NPCTemplate t = (NPCTemplate)project.findObject(NPCTemplate.class, templateID);
        if (t == null) {
            return "无";
        } else {
            return t.toString();
        }
    }

    /*
     * 下面是根据级别和职业计算标准属性的方法。
     */
    
    public int getStandardHP() {
        // <= 30级
        // 武将怪 等级*4*8+35
        // 刺客怪 等级*3*8+30
        // 法师怪 等级*2*8+25
        // > 30级
        // 武将怪：0.72*级别平方+350
        // 刺客怪：0.5 *级别平方+300
        // 法系怪：0.45*级别平方+250
        return owner.npcTemplateConfig.getStandardHP(clazz, level);
    }
    
    public int getStandardMP() {
        // 等级*2*6
        // 等级*3*6
        // 等级*4*6
        return owner.npcTemplateConfig.getStandardMP(clazz, level);
    }
    
    public int getStandardArmor() {
        // 武将怪（等级*100+900）*0.3
        // 刺客怪（等级*100+900）*0.2
        // 法师怪（等级*100+900）*0.1
        return owner.npcTemplateConfig.getStandardArmor(clazz, level);
    }
    
    public int getStandardMagicArmor() {
        return owner.npcTemplateConfig.getStandardMagicArmor(clazz, level);
    }

    // Copy from PropertyCalculator
    public static final float[] STRENGTH_GROWING = {3.0F,2.0F,2.0F,1.0F};
    public static final float[] AGILITY_GROWING = {2.0F,4.0F,2.0F,2.0F};
    public static final float[] STAMINA_GROWING = {4.0F,2.0F,2.0F,3.0F};
    public static final float[] INTELLECT_GROWING = {1.0F,2.0F,4.0F,4.0F};
    
    public static final int[] getPlayerProperties(int clazz,int level){
        int[] ret = new int[4];
        ret[0] = (int)(STRENGTH_GROWING[clazz]*level);
        ret[1] = (int)(AGILITY_GROWING[clazz]*level);
        ret[2] = (int)(STAMINA_GROWING[clazz]*level);
        ret[3] = (int)(INTELLECT_GROWING[clazz]*level);
        return ret;
    }
    
    public static final int[] getMonsterProperties(int clazz,int level){
        int[] ret = new int[4];
        ret[0] = (int)(STRENGTH_GROWING[clazz]*level*0.6);
        ret[1] = (int)(AGILITY_GROWING[clazz]*level*0.6);
        ret[2] = (int)(STAMINA_GROWING[clazz]*level*0.4);
        ret[3] = (int)(INTELLECT_GROWING[clazz]*level*0.6);
        return ret;
    }
    // Copy from PropertyCalculator end

    public int getStandardSTA() {
        return owner.npcTemplateConfig.getStandardSTA(clazz, level);
    }
    
    public int getStandardSTR() {
        return owner.npcTemplateConfig.getStandardSTR(clazz, level);
    }
    
    public int getStandardAGI() {
        return owner.npcTemplateConfig.getStandardAGI(clazz, level);
    }
    
    public int getStandardINT() {
        return owner.npcTemplateConfig.getStandardINT(clazz, level);
    }
    
    public int getStandardWeaponAP1() {
        // 等级*6.5（进程物理攻击）
        // 等级*8（远程物理攻击）
        // 等级*5（法术攻击）
        return owner.npcTemplateConfig.getStandardWeaponAP1(clazz, level);
    }
    
    public int getStandardWeaponAP2() {
        // 等级*6.5（进程物理攻击）
        // 等级*8（远程物理攻击）
        // 等级*5（法术攻击）
        return owner.npcTemplateConfig.getStandardWeaponAP2(clazz, level);
    }
    
    public int getStandardWeaponMagicAP() {
        // 等级*6.5（进程物理攻击）
        // 等级*8（远程物理攻击）
        // 等级*5（法术攻击）
        return owner.npcTemplateConfig.getStandardWeaponMagicAP(clazz, level);
    }
    
    public int getStandardExp() {
        return owner.npcTemplateConfig.getStandardExp(clazz, level);
    }
    
    public int getStandardMoney() {
        return owner.npcTemplateConfig.getStandardMoney(clazz, level);
    }
    
    public String getAIClassDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.aiClass);
        if ("general".equals(this.aiClass) || "gohome".equals(this.aiClass)) {
            sb.append("\r\n");
            for (AIRuleConfig rc : aiRules) {
                sb.append(rc.toString());
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }
}
