package com.pip.sanguo.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

public class TalismanType extends DataObject {
    /**
     * 所属项目
     */
    public ProjectData owner;
    /**
     * 职业限制
     * 0-武士 1-刺客 2-谋士 3-方士
     */
    public int clazz;
    /**
     * 需求等级
     */
    public int playerLevel;
    /**
     * 饱食度
     */
    public int appetite = 100;
    /**
     * 灵气(经验)转化系数
     */
    public int expRatio = 100;
    /**
     * 对应主动技能ID
     */
    public int skillID;
    /**
     * 洗主动技能时对应的技能组ID
     */
    public int[] skillGroup;
    /**
     * 对应被动技能ID
     */
    public int passiveSkillID;
    /**
     * 洗被动技能时对应的技能组ID
     */
    public int[] passiveSkillGroup;
    /**
     * 重置剩余次数(宝石熔炼)
     */
    public int jewelResetTimes;
    /**
     * 重置剩余次数(属性重铸)
     */
    public int recastResetTimes;
    /**
     * 对应的图片索引
     */
    public int iconID;
    /**
     * 法宝属性:力量
     */
    public int str;
    /**
     * 法宝属性:敏捷
     */
    public int agi;
    /**
     * 法宝属性:体力
     */
    public int sta;
    /**
     * 法宝属性:智力
     */
    public int wis;
    /**
     * 法宝属性:生命
     */
    public int hp;
    /**
     * 法宝属性:精力
     */
    public int mp;
    /**
     * 法宝属性:暴击
     */
    public int crit;
    /**
     * 法宝属性:命中
     */
    public int hit;
    /**
     * 法宝属性:物闪
     */
    public int dodge;
    /**
     * 法宝属性:法闪
     */
    public int magicDodge;
    /**
     * 法宝属性:物攻
     */
    public int atk;
    /**
     * 法宝属性:法攻
     */
    public int magicAtk;
    /**
     * 法宝属性:护甲
     */
    public int armor;
    /**
     * 法宝属性:法防
     */
    public int magicArmor;
    /**
     * 法宝属性:回血
     */
    public int hpResume;
    /**
     * 法宝属性:回气
     */
    public int mpResume;
    /**
     * 法宝属性:速度
     */
    public int speed;
    /**
     * 法宝属性:免暴
     */
    public int antiCrit;
    /**
     * 法宝属性:耐久度
     */
    public int duration = 100;
    /**
     * 法宝属性:升级经验
     */
    public int exp;
    /**
     * 法宝等级提升对应的属性(力量,敏捷,体力,智力)数值
     */
    public List<TalismanBasicAttrAdvance> basicAttrAdvances = new ArrayList<TalismanBasicAttrAdvance>();
    
    public Map<Integer, TalismanBasicAttrAdvance> attrAdvances = new HashMap<Integer,TalismanBasicAttrAdvance>();
    
    /**
     * 法宝类
     * @param owner 所属项目
     */
    public TalismanType(ProjectData owner) {
        this.owner = owner;
    }
    
    public String toString() {
        return id + ": " + title;
    }
    
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public boolean changed(DataObject obj) {
        return changed(this, obj);
    }

    @Override
    public boolean depends(DataObject obj) {
        return false;
    }

    @Override
    public DataObject duplicate() {
        TalismanType ret = new TalismanType(this.owner);
        ret.update(this);
        return ret;
    }

    @Override
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        clazz = Integer.parseInt(elem.getAttributeValue("class"));
        playerLevel = Integer.parseInt(elem.getAttributeValue("playerlevel"));
        try {
            appetite = Integer.parseInt(elem.getAttributeValue("appetite"));
        } catch (Exception e) {
            appetite = 100;
        }
        try {
            expRatio = Integer.parseInt(elem.getAttributeValue("expratio"));
        } catch (Exception e) {
            expRatio= 1000;
        }
        try {
            skillID = Integer.parseInt(elem.getAttributeValue("skillid"));
        } catch (Exception e) {
            skillID = 0;
        }
        
        String activeGroup;
        try {
            activeGroup = elem.getAttributeValue("activeskillgroup");
        } catch (Exception e) {
            activeGroup = null;
        }
        if (activeGroup != null && activeGroup.length() > 0) {
            String[] ag = activeGroup.split(",");
            skillGroup = new int[ag.length];
            for (int i = 0; i < ag.length; i++) {
                skillGroup[i] = Integer.parseInt(ag[i]);
            }
        }
        try {
            passiveSkillID = Integer.parseInt(elem.getAttributeValue("passiveskillid"));
        } catch (Exception e) {
            passiveSkillID = 0;
        }
        String passiveGroup;
        try {
            passiveGroup = elem.getAttributeValue("passiveskillgroup");
        } catch (Exception e) {
            passiveGroup = null;
        }
        if (passiveGroup != null && passiveGroup.length() > 0) {
            String[] pg = passiveGroup.split(",");
            passiveSkillGroup = new int[pg.length];
            for (int i = 0; i < pg.length; i++) {
                passiveSkillGroup[i] = Integer.parseInt(pg[i]);
            }
        }
        iconID = Integer.parseInt(elem.getAttributeValue("iconid"));
        try {
            jewelResetTimes = Integer.parseInt(elem.getAttributeValue("jewelreset"));
        } catch (Exception e) {
            jewelResetTimes = 1;
        }
        try {
            recastResetTimes = Integer.parseInt(elem.getAttributeValue("recastreset"));
        } catch (Exception e) {
            recastResetTimes = 1;
        }
        List attrs = elem.getChildren("attr");
        if (attrs != null) {
            for (int i = 0; i < attrs.size(); i++) {
                TalismanBasicAttrAdvance attr = new TalismanBasicAttrAdvance(this);
                attr.load((Element)attrs.get(i));
                basicAttrAdvances.add(attr);
                attrAdvances.put(attr.level, attr);
                if (attr.level == 1) {
                    str = attr.str;
                    agi = attr.agi;
                    sta = attr.sta;
                    wis = attr.wis;
                    hp = attr.hp;
                    mp = attr.mp;
                    crit = attr.crit;
                    hit = attr.hit;
                    dodge = attr.dodge;
                    magicDodge = attr.magicDodge;
                    atk = attr.atk;
                    magicAtk = attr.magicAtk;
                    armor = attr.armor;
                    magicArmor = attr.magicArmor;
                    hpResume = attr.hpResume;
                    mpResume = attr.mpResume;
                    speed = attr.speed;
                    antiCrit = attr.antiCrit;
                    duration = attr.duration;
                    exp = attr.exp;
                }
            }
        }
    }

    @Override
    public Element save() {
        Element ret = new Element("talisman");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("class", String.valueOf(clazz));
        ret.addAttribute("appetite", String.valueOf(appetite));
        ret.addAttribute("expratio", String.valueOf(expRatio));
        ret.addAttribute("playerlevel", String.valueOf(playerLevel));
        ret.addAttribute("skillid", String.valueOf(skillID));
        String asg = "";
        if (skillGroup != null && skillGroup.length > 0) {
            for (int i = 0; i < skillGroup.length; i++) {
                if (i < skillGroup.length - 1) {
                    asg += skillGroup[i] + ",";
                } else {
                    asg += skillGroup[i];
                }
            }
            ret.addAttribute("activeskillgroup", asg);
        }
        ret.addAttribute("passiveskillid", String.valueOf(passiveSkillID));
        String psg = "";
        if (passiveSkillGroup != null && passiveSkillGroup.length > 0) {
            for (int i = 0; i < passiveSkillGroup.length; i++) {
                if (i < passiveSkillGroup.length - 1) {
                    psg += passiveSkillGroup[i] + ",";
                } else {
                    psg += passiveSkillGroup[i];
                }
            }
            ret.addAttribute("passiveskillgroup", psg);
        }

        ret.addAttribute("iconid", String.valueOf(iconID));
        ret.addAttribute("jewelreset", String.valueOf(jewelResetTimes));
        ret.addAttribute("recastreset", String.valueOf(recastResetTimes));
        for (TalismanBasicAttrAdvance attr : basicAttrAdvances) {
            ret.addContent(attr.save());
        }
        return ret;
    }

    @Override
    public void update(DataObject obj) {
        TalismanType oo = (TalismanType)obj;
        id = oo.id;
        title = oo.title;
        clazz = oo.clazz;
        appetite = oo.appetite;
        expRatio = oo.expRatio;
        playerLevel = oo.playerLevel;
        skillID = oo.skillID;
        skillGroup = oo.skillGroup;
        passiveSkillID = oo.passiveSkillID;
        passiveSkillGroup = oo.passiveSkillGroup;
        iconID = oo.iconID;
        str = oo.str;
        agi = oo.agi;
        sta = oo.sta;
        wis = oo.wis;
        hp = oo.hp;
        mp = oo.mp;
        crit = oo.crit;
        hit = oo.hit;
        dodge = oo.dodge;
        magicDodge = oo.magicDodge;
        atk = oo.atk;
        magicAtk = oo.magicAtk;
        armor = oo.armor;
        magicArmor = oo.magicArmor;
        hpResume = oo.hpResume;
        mpResume = oo.mpResume;
        speed = oo.speed;
        antiCrit = oo.antiCrit;
        jewelResetTimes = oo.jewelResetTimes;
        recastResetTimes = oo.recastResetTimes;
        basicAttrAdvances.clear();
        for (TalismanBasicAttrAdvance attr : oo.basicAttrAdvances) {
            TalismanBasicAttrAdvance newAttr = attr.duplicate();
            newAttr.owner = this;
            basicAttrAdvances.add(newAttr);
        }
    }

}
