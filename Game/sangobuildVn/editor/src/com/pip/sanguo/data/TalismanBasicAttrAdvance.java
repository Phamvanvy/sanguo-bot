package com.pip.sanguo.data;

import org.jdom.Element;

public class TalismanBasicAttrAdvance {
    public TalismanType owner;
    /**
     * 等级
     */
    public int level;
    /**
     * 力量
     */
    public int str;
    /**
     * 敏捷
     */
    public int agi;
    /**
     * 体力
     */
    public int sta;
    /**
     * 智力
     */
    public int wis;
    /**
     * 生命
     */
    public int hp;
    /**
     * 精力
     */
    public int mp;
    /**
     * 暴击
     */
    public int crit;
    /**
     * 命中
     */
    public int hit;
    /**
     * 物闪
     */
    public int dodge;
    /**
     * 法闪
     */
    public int magicDodge;
    /**
     * 物攻
     */
    public int atk;
    /**
     * 法攻
     */
    public int magicAtk;
    /**
     * 护甲
     */
    public int armor;
    /**
     * 法防
     */
    public int magicArmor;
    /**
     * 回血
     */
    public int hpResume;
    /**
     * 回气
     */
    public int mpResume;
    /**
     * 速度
     */
    public int speed;
    /**
     * 免暴
     */
    public int antiCrit;
    /**
     * 耐久
     */
    public int duration;
    /**
     * 经验
     */
    public int exp;
    
    /**
     * 法宝的基础属性类
     * @param owner 所属对象
     */
    public TalismanBasicAttrAdvance(TalismanType owner) {
        this.owner = owner;
    }
    
    public boolean equals(Object o) {
        return this == o;
    }
    
    public void load(Element elem) {
        try {
            level = Integer.parseInt(elem.getAttributeValue("level"));
        } catch (Exception e) {
            level = 0;
        }
        try {
            str = Integer.parseInt(elem.getAttributeValue("str"));
        } catch (Exception e) {
            str = 0;
        }
        try {
            agi = Integer.parseInt(elem.getAttributeValue("agi"));
        } catch (Exception e) {
            agi = 0;
        }
        try {
            sta = Integer.parseInt(elem.getAttributeValue("sta"));
        } catch (Exception e) {
            sta = 0;
        }
        try {
            wis = Integer.parseInt(elem.getAttributeValue("wis"));
        } catch (Exception e) {
            wis = 0;
        }
        try {
            hp = Integer.parseInt(elem.getAttributeValue("hp"));
        } catch (Exception e) {
            hp = 0;
        }
        try {
            mp = Integer.parseInt(elem.getAttributeValue("mp"));
        } catch (Exception e) {
            mp = 0;
        }
        try {
            crit = Integer.parseInt(elem.getAttributeValue("crit"));
        } catch (Exception e) {
            crit = 0;
        }
        try {
            hit = Integer.parseInt(elem.getAttributeValue("hit"));
        } catch (Exception e) {
            hit = 0;
        }
        try {
            dodge = Integer.parseInt(elem.getAttributeValue("dodge"));
        } catch (Exception e) {
            dodge = 0;
        }
        try {
            magicDodge = Integer.parseInt(elem.getAttributeValue("magicdodge"));
        } catch (Exception e) {
            magicDodge = 0;
        }
        try {
            atk = Integer.parseInt(elem.getAttributeValue("atk"));
        } catch (Exception e) {
            atk = 0;
        }
        try {
            magicAtk = Integer.parseInt(elem.getAttributeValue("magicatk"));
        } catch (Exception e) {
            magicAtk = 0;
        }
        try {
            armor = Integer.parseInt(elem.getAttributeValue("armor"));
        } catch (Exception e) {
            armor = 0;
        }
        try {
            magicArmor = Integer.parseInt(elem.getAttributeValue("magicarmor"));
        } catch (Exception e) {
            magicArmor = 0;
        }
        try {
            hpResume = Integer.parseInt(elem.getAttributeValue("hpresume"));
        } catch (Exception e) {
            hpResume = 0;
        }
        try {
            mpResume = Integer.parseInt(elem.getAttributeValue("mpresume"));
        } catch (Exception e) {
            mpResume = 0;
        }
        try {
            speed = Integer.parseInt(elem.getAttributeValue("speed"));
        } catch (Exception e) {
            speed = 0;
        }
        try {
            antiCrit = Integer.parseInt(elem.getAttributeValue("anticrit"));
        } catch (Exception e) {
            antiCrit = 0;
        }
        try {
            duration = Integer.parseInt(elem.getAttributeValue("duration"));
        } catch (Exception e) {
            duration = 0;
        }
        try {
            exp = Integer.parseInt(elem.getAttributeValue("exp"));
        } catch (Exception e) {
            exp = 0;
        }
    }
    
    public Element save() {
        Element elem = new Element("attr");
        elem.addAttribute("level", String.valueOf(level));
        if (str != 0) {
            elem.addAttribute("str", String.valueOf(str));
        }
        if (agi != 0) {
            elem.addAttribute("agi", String.valueOf(agi));
        }
        if (sta != 0) {
            elem.addAttribute("sta", String.valueOf(sta));
        }
        if (wis != 0) {
            elem.addAttribute("wis", String.valueOf(wis));
        }
        if (hp != 0) {
            elem.addAttribute("hp", String.valueOf(hp));
        }
        if (mp != 0) {
            elem.addAttribute("mp", String.valueOf(mp));
        }
        if (crit != 0) {
            elem.addAttribute("crit", String.valueOf(crit));
        }
        if (hit != 0) {
            elem.addAttribute("hit", String.valueOf(hit));
        }
        if (dodge != 0) {
            elem.addAttribute("dodge", String.valueOf(dodge));
        }
        if (magicDodge != 0) {
            elem.addAttribute("magicdodge", String.valueOf(magicDodge));
        }
        if (atk != 0) {
            elem.addAttribute("atk", String.valueOf(atk));
        }
        if (magicAtk != 0) {
            elem.addAttribute("magicatk", String.valueOf(magicAtk));
        }
        if (armor != 0) {
            elem.addAttribute("armor", String.valueOf(armor));
        }
        if (magicArmor != 0) {
            elem.addAttribute("magicarmor", String.valueOf(magicArmor));
        }
        if (hpResume != 0) {
            elem.addAttribute("hpresume", String.valueOf(hpResume));
        }
        if (mpResume != 0) {
            elem.addAttribute("mpresume", String.valueOf(mpResume));
        }
        if (speed != 0) {
            elem.addAttribute("speed", String.valueOf(speed));
        }
        if (antiCrit != 0) {
            elem.addAttribute("anticrit", String.valueOf(antiCrit));
        }
        if (duration != 0) {
            elem.addAttribute("duration", String.valueOf(duration));
        }
        if (exp != 0) {
            elem.addAttribute("exp", String.valueOf(exp));
        }
        return elem;
    }
    
    public TalismanBasicAttrAdvance duplicate() {
        TalismanBasicAttrAdvance ret = new TalismanBasicAttrAdvance(owner);
        ret.level = level;
        ret.str = str;
        ret.agi = agi;
        ret.sta = sta;
        ret.wis = wis;
        ret.hp = hp;
        ret.mp = mp;
        ret.crit = crit;
        ret.hit = hit;
        ret.dodge = dodge;
        ret.magicDodge = magicDodge;
        ret.atk = atk;
        ret.magicAtk = magicAtk;
        ret.armor = armor;
        ret.magicArmor = magicArmor;
        ret.hpResume = hpResume;
        ret.mpResume = mpResume;
        ret.speed = speed;
        ret.antiCrit = antiCrit;
        ret.duration = duration;
        ret.exp = exp;
        return ret;
    }
}
