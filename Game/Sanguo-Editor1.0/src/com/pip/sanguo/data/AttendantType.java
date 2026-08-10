package com.pip.sanguo.data;

import java.util.ArrayList;
import java.util.List;

import org.jdom.*;

/**
 * 一个随从类型的描述信息
 * @author dchen
 */
public class AttendantType extends DataObject {
    /** 所属项目。*/
    public ProjectData owner;
    /** NPC动画。*/
    public Animation image;
    /** 性别 0男 1女 3中性*/
    public int sex;
    /** 品质 (1-9品)*/
    public int qulity = 1;
    /** 普攻类型*/
    public int skillType = 0;
    /** 生命 */
    public int hp;
    /** 精力 */
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
    /** 物理暴击 */
    public int critical;
    /** 法术暴击 */
    public int spellcritical;
    /** 免暴 */
    public int decritical;
    /** 发呆间歇 */
    public int duration;
    /** 生命链接系数 */
    public int distance;
    /** 食量 */
    public int eat;
    /** 技能槽 */
    public boolean[] skillSwitch = new boolean[6];
    /** 是否允许激活 */
    public boolean[] canActive = new boolean[6];
    /** 技能ID */
    public int[] skillId = new int[6];
    /** 技能ID */
    public int[] specialSkillIds = new int[1];
    

    public AttendantType(ProjectData owner) {
        this.owner = owner;
        initData();
    }
    
    public void initData(){
        this.hp = getStandardHP();
        this.mp = getStandardMP();
        this.armor = getStandardArmor();
        this.magicArmor = getStandardMagicArmor();
        this.critical = getStandardCritical();
        this.spellcritical = getStandardSpellcritical();
        this.decritical = getStandardDecritical();
        this.sta = getStandardSTA();
        this.str = getStandardSTR();
        this.agi = getStandardAGI();
        this.inte = getStandardINT();
        this.weaponAP1 = getStandardWeaponAP1();
        this.weaponAP2 = getStandardWeaponAP2();
        this.weaponMagicAP = getStandardWeaponMagicAP();
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
        AttendantType oo = (AttendantType)obj;
        id = oo.id;
        image = oo.image;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        hp = oo.hp;
        mp = oo.mp;
        armor = oo.armor;
        magicArmor = oo.magicArmor;
        sta = oo.sta;
        str = oo.str;
        agi = oo.agi;
        inte = oo.inte;
        weaponAP1 = oo.weaponAP1;
        weaponAP2 = oo.weaponAP2;
        weaponMagicAP = oo.weaponMagicAP;
        critical = oo.critical;
        spellcritical = oo.spellcritical;
        decritical = oo.decritical;
        duration = oo.duration;
        distance = oo.distance;
        eat = oo.eat;
        sex = oo.sex;
        qulity = oo.qulity;
        skillType = oo.skillType;
        skillSwitch = oo.skillSwitch;
        canActive = oo.canActive;
        skillId = oo.skillId;
        specialSkillIds = oo.specialSkillIds;
        if (owner != oo.owner) {
            if (image != null) {
                image = (Animation)owner.findObject(Animation.class, image.id);
            }
        }
    }
    
    public DataObject duplicate() {
        AttendantType ret = new AttendantType(owner);
        ret.update(this);
        return ret;
    }

    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }
    
    public void load(Element elem) {
        try {
            skillType = Integer.parseInt(elem.getAttributeValue("skilltype"));
        }catch (NumberFormatException e1) {
        }
        qulity = Integer.parseInt(elem.getAttributeValue("quality"));
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
        image = (Animation)owner.findObject(Animation.class, Integer.parseInt(elem.getAttributeValue("image")));
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
        critical = Integer.parseInt(elem.getAttributeValue("critical")) + getStandardCritical();
        spellcritical = Integer.parseInt(elem.getAttributeValue("spellcritical")) + getStandardSpellcritical();
        decritical = Integer.parseInt(elem.getAttributeValue("decritical")) + getStandardDecritical();
        duration = Integer.parseInt(elem.getAttributeValue("duration"));
        distance = Integer.parseInt(elem.getAttributeValue("distance"));
        eat = Integer.parseInt(elem.getAttributeValue("eat"));
        sex = Integer.parseInt(elem.getAttributeValue("sex"));
        skillSwitch = parseSkillSwitch(elem.getAttributeValue("skillswitch"));
        try {
            canActive = parseSkillSwitch(elem.getAttributeValue("canactive"));
        }
        catch (Exception e) {
        }
        skillId = parseSkillId(elem.getAttributeValue("skillid"));
        specialSkillIds = parseSpecialSkillId(elem.getAttributeValue("specialSkillId"));
    }
    
    private boolean[] parseSkillSwitch(String str){
        String[] s = str.split(",");
        boolean[] value = new boolean[6];
        for(int i=0;i<s.length;i++){
            if(s[i].equals("0"))
                value[i] = false;
            else if(s[i].equals("1"))
                value[i] = true;
        }
        return value;
    }
    
    private int[] parseSkillId(String str){
        String[] s = str.split(",");
        int[] value = new int[6];
        for(int i=0;i<s.length;i++){
            value[i] = Integer.parseInt(s[i]);
        }
        return value;
    }
    
    private int[] parseSpecialSkillId(String str){
       int[] value = new int[1];
       if(str!=null){
           String[] s = str.split(",");
           for(int i=0;i<s.length;i++){
               if(!s[i].equals("")){
                   value[i] = Integer.parseInt(s[i]);
               }
           }
       }
       return value;
    }
    
    
    private String parseSkillSwitchToString(boolean[] swich){
        StringBuffer sb = new StringBuffer();
        for(boolean b : swich){
            if(b)
                sb.append("1,");
            else
                sb.append("0,");
        }
        String value = sb.toString();
        return value.substring(0, value.length()-1);
    }
    
    private String parseSkillIdToString(int[] ids){
        StringBuffer sb = new StringBuffer();
        for(int id : ids){
            sb.append(id+",");
        }
        String value = sb.toString();
        return value.substring(0, value.length()-1);
    }
    
    private String parseSpecialSkillIdToString(int[] specialIds){
        StringBuffer sb = new StringBuffer();
        for(int id : specialIds){
            sb.append(id+",");
        }
        String value = sb.toString();
        return value.substring(0, value.length()-1);
    }
    
    public Element save() {
        Element ret = new Element("attendant");
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
        ret.addAttribute("critical", String.valueOf(critical - getStandardCritical()));
        ret.addAttribute("spellcritical", String.valueOf(spellcritical - getStandardSpellcritical()));
        ret.addAttribute("decritical", String.valueOf(decritical - getStandardDecritical()));
        ret.addAttribute("duration", String.valueOf(duration));
        ret.addAttribute("distance", String.valueOf(distance));
        ret.addAttribute("eat", String.valueOf(eat));
        ret.addAttribute("sex", String.valueOf(sex));
        ret.addAttribute("quality", String.valueOf(qulity));
        ret.addAttribute("skillswitch", parseSkillSwitchToString(skillSwitch));
        ret.addAttribute("canactive", parseSkillSwitchToString(canActive));
        ret.addAttribute("skillid", parseSkillIdToString(skillId));
        ret.addAttribute("skilltype", String.valueOf(skillType));
        ret.addAttribute("specialSkillId", parseSpecialSkillIdToString(specialSkillIds));
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
    
    public int getStandardHP() {
        return owner.attendantConfig.getStandardHP(qulity);
    }
    
    public int getStandardMP() {
        return owner.attendantConfig.getStandardMP(qulity);
    }
    
    public int getStandardArmor() {
        return owner.attendantConfig.getStandardArmor(qulity);
    }
    
    public int getStandardMagicArmor() {
        return owner.attendantConfig.getStandardMagicArmor(qulity);
    }
    
    public int getStandardCritical(){
        return owner.attendantConfig.getStandardCritical(qulity);
    }
    
    public int getStandardSpellcritical(){
        return owner.attendantConfig.getStandardSpellcritical(qulity);
    }
    
    public int getStandardDecritical(){
        return owner.attendantConfig.getStandardDecritical(qulity);
    }

    public int getStandardSTA() {
        return owner.attendantConfig.getStandardSTA(qulity);
    }
    
    public int getStandardSTR() {
        return owner.attendantConfig.getStandardSTR(qulity);
    }
    
    public int getStandardAGI() {
        return owner.attendantConfig.getStandardAGI(qulity);
    }
    
    public int getStandardINT() {
        return owner.attendantConfig.getStandardINT(qulity);
    }
    
    public int getStandardWeaponAP1() {
        return owner.attendantConfig.getStandardWeaponAP1(qulity);
    }
    
    public int getStandardWeaponAP2() {
        return owner.attendantConfig.getStandardWeaponAP2(qulity);
    }
    
    public int getStandardWeaponMagicAP() {
        return owner.attendantConfig.getStandardWeaponMagicAP(qulity);
    }
}
