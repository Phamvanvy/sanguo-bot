package com.pip.sanguo.data.skill;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.editor.skill.DescriptionPattern;
import com.pip.sanguo.editor.skill.ParamIndicator;
import com.pip.util.Utils;

/**
 * 一个BUFF类的配置数据。
 * 
 * @author lighthu
 */
public class BuffConfig extends DataObject {
    /**
     * 动态BUFF，表示临时的有时间限制或其它到期限制的BUFF。
     */
    public static final int BUFF_TYPE_DYNAMIC = 0;
    /**
     * 静态BUFF，表示被动技能加上的永不消失的BUFF。
     */
    public static final int BUFF_TYPE_STATIC = 1;
    /**
     * 物品特效BUFF，表示装备、称号等系统添加的持久性BUFF。
     */
    public static final int BUFF_TYPE_EQUIP = 2;
    
    /** 合并规则：总是不合并 */
    public static final int MERGE_NONE = 0;
    /** 合并规则：叠加，最多3层 */
    public static final int MERGE_ADD = 1;
    /** 合并规则：高级或同级覆盖 */
    public static final int MERGE_LEVEL = 2;
    /** 合并规则：同一来源的覆盖，效果叠加 */
    public static final int MERGE_SAME_SOURCE = 3;
    /** 合并规则：总是覆盖 */
    public static final int MERGE_ALWAYS = 4;

    public static final int MINORTYPE_SWORD = 1; // 剑
    public static final int MINORTYPE_KNIFE = 2; // 刀
    public static final int MINORTYPE_AXE = 3; // 斧
    public static final int MINORTYPE_SPEAR = 4; // 枪
    public static final int MINORTYPE_POLEARM = 5; // 长柄
    public static final int MINORTYPE_FAN = 6; // 扇
    public static final int MINORTYPE_BOW = 7; // 弓
    
    /**
     * 所属项目。
     */
    public ProjectData owner;
    /**
     * BUFF类型。
     */
    public int buffType = BUFF_TYPE_DYNAMIC;
    /**
     * 最高级别（有效为1-x级）
     */
    public int maxLevel = 1;
    /**
     * 图标ID，-1表示不显示
     */
    public int iconID = -1;
    /**
     * 需要武器，int[0]表示不要求武器
     */
    public int[] requireWeapon = new int[0];
    /**
     * 持续时间(毫秒)，-1表示不过期
     */
    public int[] duration = new int[1];
    /**
     * 效果价值。
     */
    public int[] value = new int[1];
    /**
     * 是否良性
     */
    public boolean good = false;
    /**
     * 是否可驱散
     */
    public boolean dispelable = true;
    /**
     * 死亡后是否保持
     */
    public boolean keepOnDie = false;
    /**
     * 是否光环
     */
    public boolean isAreaBuff = false;
    /**
     * 合并规则
     */
    public int mergeStrategy = MERGE_NONE;
    /**
     * 下线是否计时
     */
    public boolean updateEvenOffline;
    /**
     * BUFF效果
     */
    public EffectConfigSet effects = new EffectConfigSet();
    /**
     * 对应class名
     */
    public String implClass;

    /**
     * 用于编辑器的临时对象
     */
    public class GeneralConfig extends EffectConfig {
        public void setLevelCount(int max) {
        }

        public int getType() {
            return -1;
        }

        public String getTypeName() {
            return "";
        }

        public String getShortName() {
            return "";
        }

        public int getParamCount() {
            if (buffType == BUFF_TYPE_STATIC) {
                return 0;
            }
            return 1;
        }

        public String getParamName(int index) {
            if (buffType == BUFF_TYPE_EQUIP) {
                return "特效价值";
            }
            return "持续时间(毫秒)";
        }

        public Class getParamClass(int index) {
            return Integer.class;
        }

        public Object getParam(int index) {
            if (buffType == BUFF_TYPE_EQUIP) {
                return value;
            }
            return duration;
        }
    }

    public BuffConfig(ProjectData owner) {
        this.owner = owner;
        effects.setLevelCount(maxLevel);
    }
    
    public int getID() {
        return id;
    }

    public boolean equals(Object o) {
        return this == o;
    }

    public String toString() {
        return id + ": " + title;
    }
    
    /**
     * 设置BUFF类型。
     * @param type
     */
    public void setBuffType(int type) {
        buffType = type;
        if (buffType == BUFF_TYPE_STATIC) {
            Arrays.fill(duration, -1);
            good = true;
            dispelable = false;
            isAreaBuff = false;
            mergeStrategy = MERGE_LEVEL;
        } else if (buffType == BUFF_TYPE_EQUIP) {
            Arrays.fill(duration, -1);
            good = true;
            dispelable = false;
            isAreaBuff = false;
            mergeStrategy = MERGE_LEVEL;
        } else {
            isAreaBuff = false;
        }
    }

    /**
     * 获得通用参数表。
     * 
     * @return
     */
    public EffectConfig getGeneralConfig() {
        return new GeneralConfig();
    }

    /**
     * 修改最大级别。
     */
    public void setMaxLevel(int newValue) {
        maxLevel = newValue;
        duration = Utils.realloc(duration, maxLevel);
        value = Utils.realloc(value, maxLevel);
        if (buffType == BUFF_TYPE_STATIC) {
            Arrays.fill(duration, -1);
        } else if (buffType == BUFF_TYPE_EQUIP) {
            Arrays.fill(duration, -1);
        }
        effects.setLevelCount(maxLevel);
    }

    /**
     * 保存数据对象。
     * @param obj 编辑器当前输入内容
     */
    public void update(DataObject obj) {
        BuffConfig oo = (BuffConfig) obj;
        id = oo.id;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        
        buffType = oo.buffType;
        maxLevel = oo.maxLevel;
        iconID = oo.iconID;
        requireWeapon = new int[oo.requireWeapon.length];
        System.arraycopy(oo.requireWeapon, 0, requireWeapon, 0, requireWeapon.length);
        duration = new int[oo.duration.length];
        System.arraycopy(oo.duration, 0, duration, 0, duration.length);
        value = new int[oo.value.length];
        System.arraycopy(oo.value, 0, value, 0, value.length);
        good = oo.good;
        dispelable = oo.dispelable;
        keepOnDie = oo.keepOnDie;
        isAreaBuff = oo.isAreaBuff;
        mergeStrategy = oo.mergeStrategy;
        updateEvenOffline = oo.updateEvenOffline;
        effects = oo.effects.duplicate();
        effects.removeEffect(-1);
        implClass = oo.implClass;
    }

    /**
     * 复制对象以用于编辑。
     */
    public DataObject duplicate() {
        BuffConfig ret = new BuffConfig(owner);
        ret.update(this);
        return ret;
    }

    @Override
    public boolean changed(DataObject obj) {
        BuffConfig oo = (BuffConfig)obj;
        return !implClass.equals(oo.implClass);
    }
    
    /**
     * 从XML标签中载入对象属性。
     * @param elem
     */
    public void load(Element elem) {
        id = Integer.parseInt(elem.getAttributeValue("id"));
        title = elem.getAttributeValue("title");
        description = elem.getAttributeValue("description");
        categoryName = elem.getAttributeValue("category");
        if (categoryName == null) {
            categoryName = "";
        }
        
        buffType = Integer.parseInt(elem.getAttributeValue("type"));
        maxLevel = Integer.parseInt(elem.getAttributeValue("maxlevel"));
        iconID = Integer.parseInt(elem.getAttributeValue("iconid"));
        requireWeapon = Utils.stringToIntArray(elem.getAttributeValue("require-weapon"), ';');
        duration = Utils.stringToIntArray(elem.getAttributeValue("duration"), ';');
        try {
            value = Utils.stringToIntArray(elem.getAttributeValue("value"), ';');
        } catch (Exception e) {
            value = new int[maxLevel];
        }
        good = "true".equals(elem.getAttributeValue("good"));
        dispelable = "true".equals(elem.getAttributeValue("dispelable"));
        keepOnDie = "true".equals(elem.getAttributeValue("keepondie"));
        isAreaBuff = "true".equals(elem.getAttributeValue("isareabuff"));
        mergeStrategy = Integer.parseInt(elem.getAttributeValue("merge-strategy"));
        updateEvenOffline = "true".equals(elem.getAttributeValue("update-offline"));

        try {
            effects.load(elem.getChild("effects"), maxLevel);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
        effects.setLevelCount(maxLevel);
        
        implClass = elem.getAttributeValue("class");
    }

    /**
     * 保存成一个XML标签。
     */
    public Element save() {
        Element ret = new Element("buff");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("description", description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        ret.addAttribute("type", String.valueOf(buffType));
        ret.addAttribute("maxlevel", String.valueOf(maxLevel));
        ret.addAttribute("iconid", String.valueOf(iconID));
        ret.addAttribute("require-weapon", Utils.intArrayToString(requireWeapon, ';'));
        ret.addAttribute("duration", Utils.intArrayToString(duration, ';'));
        ret.addAttribute("value", Utils.intArrayToString(value, ';'));
        ret.addAttribute("good", good ? "true" : "false");
        ret.addAttribute("dispelable", dispelable ? "true" : "false");
        ret.addAttribute("keepondie", keepOnDie ? "true" : "false");
        ret.addAttribute("isareabuff", isAreaBuff ? "true" : "false");
        ret.addAttribute("merge-strategy", String.valueOf(mergeStrategy));
        ret.addAttribute("update-offline", updateEvenOffline ? "true" : "false");
        ret.addContent(effects.save());
        if (implClass != null) {
            ret.addAttribute("class", implClass);
        }
        return ret;
    }

    /**
     * 判断本对象是否依赖于另外一个对象。
     */
    public boolean depends(DataObject obj) {
        return false;
    }
    
    /**
     * 计算自动生成的类名
     * @param classPrefix
     * @return
     */
    public String getClassName(String classPrefix) {
        String idStr = String.valueOf(id);
        while (idStr.length() < 3) {
            idStr = "0" + idStr;
        }
        return classPrefix + idStr;
    }

    /**
     * 根据一个BUFF配置生成实现类。
     * @param out 输出流
     * @param packageName 包名
     * @param classPrefix 类名前缀
     */
    public void generateJava(PrintWriter out, String packageName, String classPrefix) {
        // package & import
        out.println("package " + packageName + ";");
        out.println();
        out.println("import java.util.*;");
        out.println("import peony.game.*;");
        out.println("import peony.game.buff.*;");
        out.println("import peony.game.skill.*;");
        out.println("import peony.util.*;");
        out.println("import com.pip.util.*;");
        out.println("import org.apache.mina.common.ByteBuffer;");
        out.println();

        // class name & interfaces
        Set<String> ifs = getJavaInterface();
        String className = getClassName(classPrefix);
        out.print("public class " + className + " implements Buff");
        for (String ifname : ifs) {
            out.print(", " + ifname);
        }
        out.println(" {");

        // static data definition
        out.println("    private static final int[] WEAPON = {");
        out.print("        ");
        for (int i = 0; i < requireWeapon.length; i++) {
            if (i > 0) {
                out.print(", ");
            }
            out.print(requireWeapon[i]);
        }
        out.println();
        out.println("    };");
        generateStaticArray(out, "DURATION", duration);
        for (EffectParamRef pr : effects.getAllParams()) {
            String name = getFieldName(pr.effect, pr.index, true);
            generateStaticArray(out, name, pr.effect.getParam(pr.index));
        }

        // local data definition
        if (buffType == BUFF_TYPE_STATIC) {
            out.println("    Skill skill;");
        }
        out.println("    int instanceID;");
        out.println("    int level;");
        out.println("    int endTime;");
        out.println("    int multiple;");
        out.println("    int remainSeconds;");
        out.println("    int tickInterval;");
        out.println("    int remainCure;");
        out.println("    int remainDamage;");
        out.println("    int remainMPCure;");
        out.println("    int remainMPDamage;");
        out.println("    int remainAbsorb;");
        out.println("    int effectTimes;");
        out.println("    GameObjectRef owner;");
        out.println("    GameObjectRef source;");
        
        // 如果有不能移动的效果，需要记录此时玩家的位置
        if (effects.findEffect(EffectConfig.CANNOT_MOVE) != null) {
            out.println("    int ownerMap;");
            out.println("    int ownerX;");
            out.println("    int ownerY;");
        }
        
        // 如果有血量激活BUFF的效果，需要记录是否已经加过BUFF了
        if (effects.findEffect(EffectConfig.HP_ACTIVE_BUFF) != null) {
            out.println("    boolean hpBuffActive;");
        }
        
        // 如果有限制技能的效果，定义技能表
        EffectConfig limitSkillConfig = effects.findEffect(EffectConfig.LIMIT_SKILL);
        if (limitSkillConfig != null) {
            out.println("    IntHashSet limitSkills = new IntHashSet();");
        }
        
        // 所有效果的参数
        for (EffectParamRef pr : effects.getAllParams()) {
            String name = getFieldName(pr.effect, pr.index, false);
            Object param = pr.effect.getParam(pr.index);
            if (param instanceof int[]) {
                out.println("    int " + name + ";");
            }
            else if (param instanceof float[]) {
                out.println("    float " + name + ";");
            }
            else if (param instanceof String[]) {
                out.println("    String " + name + ";");
            }
            else if (param instanceof ParamIndicator[]) {
                out.println("    String " + name + ";");
            }
            else if (param instanceof int[][]) {
                out.println("    int[] " + name + ";");
            }
        }
        out.println();

        // constructor
        if (buffType == BUFF_TYPE_STATIC) {
            out.println("    public " + className + "(Skill skl, int lvl) {");
            out.println("        int dmg = 0;");
            out.println("        skill = skl;");
            out.println("        instanceID = BuffUtil.getNextID();");
            out.println("        level = lvl;");
            out.println("        multiple = 1;");
            for (EffectParamRef pr : effects.getAllParams()) {
                String name = getFieldName(pr.effect, pr.index, false);
                String staticName = getFieldName(pr.effect, pr.index, true);
                out.println("        " + name + " = " + staticName + "[level];");
            }
        } else if (buffType == BUFF_TYPE_EQUIP) {
            out.println("    public " + className + "(int lvl) {");
            out.println("        int dmg = 0;");
            out.println("        instanceID = BuffUtil.getNextID();");
            out.println("        level = lvl;");
            out.println("        multiple = 1;");
            for (EffectParamRef pr : effects.getAllParams()) {
                String name = getFieldName(pr.effect, pr.index, false);
                String staticName = getFieldName(pr.effect, pr.index, true);
                out.println("        " + name + " = " + staticName + "[level];");
            }
        } else {
            out.println("    public " + className + "(int lvl, Unit src, Unit tgt, int dmg) {");
            out.println("        if (src != null) {");
            out.println("            source = src.ref();");
            out.println("        }");
            out.println("        if (tgt != null) {");
            out.println("            owner = tgt.ref();");
            out.println("        }");
            out.println("        instanceID = BuffUtil.getNextID();");
            out.println("        level = lvl;");
            out.println("        endTime = Time.currTime + BuffUtil.enhanceParam(src, tgt, " + id + ", \"buffTime\", DURATION[level]);");
            out.println("        multiple = 1;");
            for (EffectParamRef pr : effects.getAllParams()) {
                String name = getFieldName(pr.effect, pr.index, false);
                String staticName = getFieldName(pr.effect, pr.index, true);
                out.println("        " + name + " = " + staticName + "[level];");
                if (pr.getParamClass() == Integer.class || pr.getParamClass() == Float.class) {
                    out.println("        " + name + " = BuffUtil.enhanceParam(src, tgt, " + id + ", \"" + name + "\", " + name + ");");
                }
            }
        }
        
        // 处理dot/hot的初始值：时间、间隔、剩余伤害、剩余治疗
        List<EffectConfig> hots = effects.findEffects(new int[] { EffectConfig.DOT, EffectConfig.HOT, EffectConfig.MPDOT, 
                EffectConfig.MPHOT });
        if (hots.size() != 0) {
            if (buffType == BUFF_TYPE_STATIC || buffType == BUFF_TYPE_EQUIP) {
                throw new IllegalArgumentException("永久性BUFF不允许使用可能会消失的效果。");
            }
            String varNameSec = getFieldName(hots.get(0), 0, false);
            String varNameInt = getFieldName(hots.get(0), 1, false);
            out.println("        remainSeconds = " + varNameSec + ";");
            out.println("        tickInterval = " + varNameInt + ";");
            Iterator<EffectConfig> itor = hots.iterator();
            while (itor.hasNext()) {
                EffectConfig hot = itor.next();
                String varNameT = getFieldName(hot, 2, false);
                String varNameP = getFieldName(hot, 3, false);
                if (hot.getType() == EffectConfig.DOT) {
                    out.println("        remainDamage = " + varNameT + ";");
                    out.println("        remainDamage += " + varNameP + " * dmg / 100.0f;");
                } else if (hot.getType() == EffectConfig.HOT) {
                    out.println("        remainCure = " + varNameT + ";");
                    out.println("        remainCure += " + varNameP + " * dmg / 100.0f;");
                } else if (hot.getType() == EffectConfig.MPDOT) {
                    out.println("        remainMPDamage = " + varNameT + ";");
                    out.println("        remainMPDamage += " + varNameP + " * dmg / 100.0f;");
                } else if (hot.getType() == EffectConfig.MPHOT) {
                    out.println("        remainMPCure = " + varNameT + ";");
                    out.println("        remainMPCure += " + varNameP + " * dmg / 100.0f;");
                }
            }
        }
        if (effects.findEffects(new int[] { EffectConfig.CANNOT_MOVE, EffectConfig.LIMIT_EFFECT_TIMES }).size() > 0) {
            if (buffType == BUFF_TYPE_STATIC || buffType == BUFF_TYPE_EQUIP) {
                throw new IllegalArgumentException("永久性BUFF不允许使用可能会消失的效果。");
            }
        }

        // 处理法力护盾的初始值：剩余吸收
        EffectConfig shield = effects.findEffect(EffectConfig.MP_SHIELD);
        if (shield != null) {
            if (buffType == BUFF_TYPE_STATIC || buffType == BUFF_TYPE_EQUIP) {
                throw new IllegalArgumentException("永久性BUFF不允许使用护盾效果。");
            }
            String varNameT = getFieldName(shield, 0, false);
            String varNameP = getFieldName(shield, 1, false);
            out.println("        remainAbsorb = " + varNameT + ";");
            out.println("        remainAbsorb += " + varNameP + " * dmg / 100.0f;");
        }
        
        // 如果有不能移动的效果，需要记录此时玩家的位置
        if (effects.findEffect(EffectConfig.CANNOT_MOVE) != null) {
            out.println("        try {");
            out.println("            ownerMap = tgt.map.id;");
            out.println("        } catch (Exception e) {}");
            out.println("        ownerX = tgt.x;");
            out.println("        ownerY = tgt.y;");
        }

        // 处理技能限制
        if (limitSkillConfig != null) {
            String name = getFieldName(limitSkillConfig, 0, false);
            out.println("        String[] secs = " + name + ".split(\",\");");
            out.println("        for (String s : secs) {");
            out.println("            limitSkills.add(Integer.parseInt(s));");
            out.println("        }");
        }
        
        out.println("    }");
        out.println("");
        
        
        // implementation of Buff

        // getId()
        out.println("    public int getId() {");
        if (buffType == BUFF_TYPE_STATIC) {
            out.println("        return skill.getId();");
        } else {
            out.println("        return " + id + ";");
        }
        out.println("    }");
        out.println();
        
        // getInstanceID
        out.println("    public int getInstanceID() {");
        out.println("        return instanceID;");
        out.println("    }");
        out.println();
        
        // getName
        out.println("    public String getName() {");
        out.println("        return \"" + Utils.reverseConv(title) + "\";");
        out.println("    }");
        out.println();

        // getDesc()
        out.println("    public String getDesc() {");
        String[] secs = DescriptionPattern.splitPattern(description);
        if (secs.length <= 1) {
            out.println("        return \"" + Utils.reverseConv(description) + "\";");
        } else {
            DescriptionPattern pattern = new DescriptionPattern(this);
            String formatString = "";
            List<String> paramStrings = new ArrayList<String>();
            for (int i = 0; i < secs.length; i++) {
                if ((i & 1) == 0) {
                    // 非变量
                    if (secs[i].length() > 0) {
                        formatString += Utils.reverseConv(secs[i]);
                    }
                } else {
                    // 变量
                    int type = 0;
                    String varName = secs[i];
                    if (varName.endsWith("%")) {
                        type = 1;
                        varName = varName.substring(0, varName.length() - 1);
                    } else if (varName.endsWith("t")) {
                        type = 2;
                        varName = varName.substring(0, varName.length() - 1);
                    } else if (varName.endsWith("T")) {
                        type = 3;
                        varName = varName.substring(0, varName.length() - 1);
                    }
                    String varRef = pattern.varToCode(varName);
                    formatString += "{" + paramStrings.size() + "}";
                    if (type == 0) {
                        paramStrings.add("CommonUtil.formatValue(" + varRef + " * multiple)");
                    } else if (type == 1) {
                        paramStrings.add("CommonUtil.formatPercent(" + varRef + " * multiple)");
                    } else if (type == 2) {
                        paramStrings.add("CommonUtil.formatMillSecond(" + varRef + ")");
                    } else if (type == 3) {
                        paramStrings.add("CommonUtil.formatSecond(" + varRef + ")");
                    }
                }
            }
            out.print("        return java.text.MessageFormat.format(\"" + formatString + "\"");
            for (String param : paramStrings) {
                out.print(", " + param);
            }
            out.println(");");
        }
        out.println("    }");
        out.println();
        
        // getSkill()
        if (buffType == BUFF_TYPE_STATIC) {
            out.println("    public Skill getSkill() {");
            out.println("        return skill;");
            out.println("    }");
            out.println();
        }

        // checkWeapon()
        out.println("    private boolean checkWeapon(Unit unit) {");
        if (requireWeapon.length == 0) {
            out.println("        return true;");
        } else {
            out.println("        if (unit == null || unit.equipments == null || unit.equipments.getWeapon() == null) {");
            out.println("            return false;");
            out.println("        }");
            out.println("        int type = unit.equipments.getWeapon().template.equipment.minorType;");
            out.println("        for (int t : WEAPON) {");
            out.println("            if (t == type) {");
            out.println("                return true;");
            out.println("            }");
            out.println("        }");
            out.println("        return false;");
        }
        out.println("    }");
        out.println();

        // getEndTime()
        out.println("    public int getEndTime() {");
        if (buffType == BUFF_TYPE_STATIC || buffType == BUFF_TYPE_EQUIP) {
            out.println("        return -1;");
        } else {
            if (hots.size() != 0) {
                out.println("        return Time.currTime + remainSeconds * 1000;");
            } else if (duration[0] >= 0) {
                out.println("        return endTime;");
            } else {
                out.println("        return -1;");
            }
        }
        out.println("    }");
        out.println();

        // isGood()
        out.println("    public boolean isGood() {");
        out.println("        return " + (good ? "true" : "false") + ";");
        out.println("    }");
        out.println();

        // dispelable()
        out.println("    public boolean dispelable() {");
        out.println("        return " + (dispelable ? "true" : "false") + ";");
        out.println("    }");
        out.println();
        
        // keepOnDie()
        out.println("    public boolean keepOnDie() {");
        out.println("        return " + (keepOnDie ? "true" : "false") + ";");
        out.println("    }");
        out.println();

        // getIconID()
        out.println("    public int getIconID() {");
        if (iconID == -1) {
            out.println("        return " + iconID + ";");
        } else if (this.mergeStrategy == MERGE_ADD) {
            // 叠加3层BUFF特殊处理
            out.println("        return " + iconID + " | (multiple << 24);");
        } else {
            out.println("        return " + iconID + " | 0x1000000;");
        }
        out.println("    }");
        out.println();

        // isAreaBuff()
        out.println("    public boolean isAreaBuff() {");
        out.println("        return " + (isAreaBuff ? "true" : "false") + ";");
        out.println("    }");
        out.println();

        // isNeedMerge()
        out.println("    public boolean isNeedMerge() {");
        if (this.mergeStrategy == MERGE_NONE) {
            out.println("        return false;");
        } else {
            out.println("        return true;");
        }
        out.println("    }");
        out.println();

        // merge(Buff buff)
        out.println("    public boolean merge(Buff buff) {");
        if (this.mergeStrategy == MERGE_ADD) {
            // 如果新BUFF较高级，则用新的覆盖旧的；如果同级，则增加叠加层数并
            // 刷新时间；如果新BUFF较低级，直接丢弃。
            out.println("        if (buff instanceof " + className + ") {");
            out.println("            " + className + " other = (" + className + ")buff;");
            out.println("            if (level < other.level) {");
            out.println("                level = other.level;");
            out.println("                endTime = other.endTime;");
            out.println("                multiple = 1;");
            out.println("                remainSeconds = other.remainSeconds;");
            out.println("                tickInterval = other.tickInterval;");
            out.println("                remainCure = other.remainCure;");
            out.println("                remainDamage = other.remainDamage;");
            out.println("                remainMPCure = other.remainMPCure;");
            out.println("                remainMPDamage = other.remainMPDamage;");
            out.println("                remainAbsorb = other.remainAbsorb;");
            out.println("                effectTimes = 0;");
            out.println("                owner = other.owner;");
            out.println("                source = other.source;");
            generateFieldsCopy(out, "                ");
            out.println("            } else if (level == other.level) {");
            out.println("                endTime = other.endTime;");
            out.println("                if (multiple < 3) {");
            out.println("                    multiple++;");
            out.println("                }");
            out.println("            }");
            out.println("            return true;");
            out.println("        }");
            out.println("        return false;");
        }
        else if (this.mergeStrategy == MERGE_LEVEL) {
            // 高级或同级新BUFF覆盖旧的BUFF，低级BUFF丢弃
            out.println("        if (buff instanceof " + className + ") {");
            out.println("            " + className + " other = (" + className + ")buff;");
            out.println("            if (level <= other.level) {");
            out.println("                level = other.level;");
            out.println("                endTime = other.endTime;");
            out.println("                multiple = 1;");
            out.println("                remainSeconds = other.remainSeconds;");
            out.println("                tickInterval = other.tickInterval;");
            out.println("                remainCure = other.remainCure;");
            out.println("                remainDamage = other.remainDamage;");
            out.println("                remainMPCure = other.remainMPCure;");
            out.println("                remainMPDamage = other.remainMPDamage;");
            out.println("                remainAbsorb = other.remainAbsorb;");
            out.println("                effectTimes = 0;");
            out.println("                owner = other.owner;");
            out.println("                source = other.source;");
            generateFieldsCopy(out, "                ");
            out.println("            }");
            out.println("            return true;");
            out.println("        }");
            out.println("        return false;");
        }
        else if (this.mergeStrategy == MERGE_SAME_SOURCE) {
            // 相同来源的BUFF合并，伤害效果叠加
            out.println("        if (buff instanceof " + className + ") {");
            out.println("            " + className + " other = (" + className + ")buff;");
            out.println("            if (source.equals(other.source)) {");
            out.println("                level = other.level;");
            out.println("                endTime = other.endTime;");
            out.println("                multiple = 1;");
            out.println("                remainSeconds = other.remainSeconds;");
            out.println("                tickInterval = other.tickInterval;");
            out.println("                remainCure += other.remainCure;");
            out.println("                remainDamage += other.remainDamage;");
            out.println("                remainMPCure += other.remainMPCure;");
            out.println("                remainMPDamage += other.remainMPDamage;");
            out.println("                remainAbsorb = other.remainAbsorb;");
            generateFieldsCopy(out, "                ");
            out.println("                return true;");
            out.println("            }");
            out.println("        }");
            out.println("        return false;");
        }
        else if (this.mergeStrategy == MERGE_ALWAYS) {
            // 始终覆盖
            out.println("        if (buff instanceof " + className + ") {");
            out.println("            " + className + " other = (" + className + ")buff;");
            out.println("            level = other.level;");
            out.println("            endTime = other.endTime;");
            out.println("            multiple = 1;");
            out.println("            remainSeconds = other.remainSeconds;");
            out.println("            tickInterval = other.tickInterval;");
            out.println("            remainCure = other.remainCure;");
            out.println("            remainDamage = other.remainDamage;");
            out.println("            remainMPCure = other.remainMPCure;");
            out.println("            remainMPDamage = other.remainMPDamage;");
            out.println("            remainAbsorb = other.remainAbsorb;");
            out.println("            effectTimes = 0;");
            out.println("            owner = other.owner;");
            out.println("            source = other.source;");
            generateFieldsCopy(out, "            ");
            out.println("            return true;");
            out.println("        }");
            out.println("        return false;");
        } else {
            out.println("        return false;");
        }
        out.println("    }");
        out.println();
        
        // getSource()
        out.println("    public GameObjectRef getSource() {");
        out.println("        return source;");
        out.println("    }");
        out.println();
        
        // setOwner()
        out.println("    public void setOwner(GameObjectRef o) {");
        out.println("        owner = o;");
        out.println("        if (source == null) {");
        out.println("            source = owner;");
        out.println("        }");
        out.println("    }");
        out.println();
        
        // resetParams(Unit owner)
        out.println("    public void resetParams(Unit owner) {");
        if (buffType != BUFF_TYPE_DYNAMIC) {
            for (EffectParamRef pr : effects.getAllParams()) {
                if (pr.getParamClass() == Integer.class || pr.getParamClass() == Float.class) { 
                    String name = getFieldName(pr.effect, pr.index, false);
                    String staticName = getFieldName(pr.effect, pr.index, true);
                    out.println("        " + name + " = " + staticName + "[level];");
                    out.println("        " + name + " = owner.buffs.getParamEnhances().enhance(ParamEnhanceSet.TYPE_BUFF_OWNER, " + id + ", \"" + name + "\", " + name + ");");
                }
            }
        }
        out.println("    }");
        out.println();

        // 实现Updatable接口
        if (ifs.contains("Updatable")) {
            // update()
            out.println("    public boolean update(int diff) {");
            if (hots.size() != 0) {
                out.println("        if (((remainSeconds - 1) % tickInterval) == 0) {");
                out.println("            int remainTicks = (remainSeconds - 1) / tickInterval + 1;");
                out.println("            int cure = remainCure / remainTicks;");
                out.println("            int dmg = remainDamage / remainTicks;");
                out.println("            int mpcure = remainMPCure / remainTicks;");
                out.println("            int mpdmg = remainMPDamage / remainTicks;");
                out.println("            remainCure -= cure;");
                out.println("            remainDamage -= dmg;");
                out.println("            remainMPCure -= mpcure;");
                out.println("            remainMPDamage -= mpdmg;");
                out.println("            if (cure > 0) {");
                out.println("                Unit healerUnit = (Unit)ObjectAccessor.getGameObject(source);");
                out.println("                Unit targetUnit = (Unit)ObjectAccessor.getGameObject(owner);");
                out.println("                if (targetUnit != null && targetUnit.isAlive()) {");
                out.println("                    cure = targetUnit.setHp(targetUnit.hp + cure, true);");
                out.println("                    if (healerUnit != null && healerUnit.isAlive()) {");
                out.println("                        Attack.addHealThreat(healerUnit, targetUnit, CombatContext.calcHotThreat(cure), false);");
                out.println("                    }");
                out.println("                }");
                out.println("            }");
                out.println("            if (dmg > 0) {");
                out.println("                Unit sourceUnit = (Unit)ObjectAccessor.getGameObject(source);");
                out.println("                Unit targetUnit = (Unit)ObjectAccessor.getGameObject(owner);");
                out.println("                if (targetUnit != null && targetUnit.isAlive()) {");
                out.println("                    targetUnit.setHp(targetUnit.hp - dmg, true);");
                out.println("                    if (sourceUnit != null && sourceUnit.isAlive()) {");
                out.println("                        Attack.addDamageThreat(sourceUnit, targetUnit, dmg, false);");
                out.println("                    }");
                out.println("                    if (targetUnit.hp <= 0) {");
                out.println("                        targetUnit.die(sourceUnit);");
                out.println("                    }");
                out.println("                }");
                out.println("            }");
                out.println("            if (mpcure > 0) {");
                out.println("                Unit healerUnit = (Unit)ObjectAccessor.getGameObject(source);");
                out.println("                Unit targetUnit = (Unit)ObjectAccessor.getGameObject(owner);");
                out.println("                if (targetUnit != null && targetUnit.isAlive()) {");
                out.println("                    targetUnit.setMp(targetUnit.mp + mpcure, true);");
                out.println("                    if (healerUnit != null && healerUnit.isAlive()) {");
                out.println("                        Attack.addHealThreat(healerUnit, targetUnit, mpcure, false);");
                out.println("                    }");
                out.println("                }");
                out.println("            }");
                out.println("            if (mpdmg > 0) {");
                out.println("                Unit sourceUnit = (Unit)ObjectAccessor.getGameObject(source);");
                out.println("                Unit targetUnit = (Unit)ObjectAccessor.getGameObject(owner);");
                out.println("                if (targetUnit != null && targetUnit.isAlive()) {");
                out.println("                    targetUnit.setMp(targetUnit.mp - mpdmg, true);");
                out.println("                    if (sourceUnit != null && sourceUnit.isAlive()) {");
                out.println("                        Attack.addDamageThreat(sourceUnit, targetUnit, mpdmg, false);");
                out.println("                    }");
                out.println("                }");
                out.println("            }");
                out.println("        }");
                out.println("        remainSeconds--;");
                out.println("        if (remainSeconds <= 0) {");
                out.println("            return true;");
                out.println("        }");
            }
            if (duration[0] >= 0) {
                out.println("        if (endTime <= Time.currTime) {");
                out.println("            return true;");
                out.println("        }");
            }
            if (shield != null) {
                out.println("        if (remainAbsorb <= 0) {");
                out.println("            return true;");
                out.println("        }");
            }
            if (effects.findEffect(EffectConfig.CANNOT_MOVE) != null) {
                out.println("        Unit targetUnit = (Unit)ObjectAccessor.getGameObject(owner);");
                out.println("        if (targetUnit == null || targetUnit.map == null) {");
                out.println("            return true;");
                out.println("        }");
                out.println("        if (targetUnit.getThreatCount() > 0) {");
                out.println("            return true;");
                out.println("        }");
//                out.println("        if (targetUnit.map.id != ownerMap) {");
//                out.println("            return true;");
//                out.println("        }");
//                out.println("        int offx = Math.abs(targetUnit.x - ownerX);");
//                out.println("        int offy = Math.abs(targetUnit.y - ownerY);");
//                out.println("        if (offx >= 32 || offy >= 32) {");
//                out.println("            return true;");
//                out.println("        }");
            };
            if (effects.findEffect(EffectConfig.REMOVE_ON_BATTLE_END) != null) {
                out.println("        Unit targetUnit = (Unit)ObjectAccessor.getGameObject(owner);");
                out.println("        if (targetUnit == null || targetUnit.map == null) {");
                out.println("            return true;");
                out.println("        }");
                out.println("        if (targetUnit.getThreatCount() == 0) {");
                out.println("            return true;");
                out.println("        }");
            }
            if (effects.findEffect(EffectConfig.HP_ACTIVE_BUFF) != null) {
                EffectConfig eff = effects.findEffect(EffectConfig.HP_ACTIVE_BUFF);
                String p1 = getFieldName(eff, 0, false);
                String p2 = getFieldName(eff, 1, false);
                String p3 = getFieldName(eff, 2, false);
                out.println("        Unit targetUnit = (Unit)ObjectAccessor.getGameObject(owner);");
                out.println("        if (targetUnit == null) {");
                out.println("            return true;");
                out.println("        }");
                out.println("        if (hpBuffActive && targetUnit.hp >= targetUnit.maxhp * " + p1 + " / 100.0f) {");
                out.println("            targetUnit.buffs.removeBuff(" + p2 + ");");
                out.println("        } else if (!hpBuffActive && targetUnit.hp < targetUnit.maxhp * " + p1 + " / 100.0f) {");
                out.println("            targetUnit.buffs.addBuff(BuffUtil.createBuff(" + p2 + ", "
                            + p3 + ", targetUnit, targetUnit, 0));");
                out.println("        }");
                
            }
            if (effects.findEffect(EffectConfig.LIMIT_EFFECT_TIMES) != null) {
                EffectConfig eff = effects.findEffect(EffectConfig.LIMIT_EFFECT_TIMES);
                String p1 = getFieldName(eff, 0, false);
                out.println("        if (effectTimes >= " + p1 + ") {");
                out.println("            return true;");
                out.println("        }");
            }
            out.println("        return false;");
            out.println("    }");
            out.println();
            
            // load(byte[])
            out.println("    public void load(byte[] bytes) {");
            out.println("        ByteBuffer buf = ByteBuffer.wrap(bytes);");
            out.println("        owner = new GameObjectRef(buf.get(), buf.getInt(), buf.getInt());");
            out.println("        source = new GameObjectRef(buf.get(), buf.getInt(), buf.getInt());");
            out.println("        level = buf.getInt();");
            out.println("        endTime = Time.elapseTime(buf.getLong());");
            out.println("        multiple = buf.getInt();");
            for (EffectParamRef pr : effects.getAllParams()) {
                String name = getFieldName(pr.effect, pr.index, false);
                String staticName = getFieldName(pr.effect, pr.index, true);
                out.println("        " + name + " = " + staticName + "[level];");
            }
            out.println("        remainSeconds = buf.getInt();");
            out.println("        tickInterval = buf.getInt();");
            out.println("        remainCure = buf.getInt();");
            out.println("        remainDamage = buf.getInt();");
            out.println("        remainMPCure = buf.getInt();");
            out.println("        remainMPDamage = buf.getInt();");
            out.println("        remainAbsorb = buf.getInt();");
            out.println("        effectTimes = buf.getInt();");
            out.println("    }");
            out.println();
            
            // save()
            out.println("    public byte[] save() {");
            out.println("        ByteBuffer buf = ByteBuffer.allocate(66, false);");
            out.println("        buf.put(owner.type);");
            out.println("        buf.putInt(owner.id);");
            out.println("        buf.putInt(owner.instanceId);");
            out.println("        buf.put(source.type);");
            out.println("        buf.putInt(source.id);");
            out.println("        buf.putInt(source.instanceId);");
            out.println("        buf.putInt(level);");
            out.println("        buf.putLong(Time.currentTimeMillis(endTime));");
            out.println("        buf.putInt(multiple);");
            out.println("        buf.putInt(remainSeconds);");
            out.println("        buf.putInt(tickInterval);");
            out.println("        buf.putInt(remainCure);");
            out.println("        buf.putInt(remainDamage);");
            out.println("        buf.putInt(remainMPCure);");
            out.println("        buf.putInt(remainMPDamage);");
            out.println("        buf.putInt(remainAbsorb);");
            out.println("        buf.putInt(effectTimes);");
            out.println("        return buf.array();");
            out.println("    }");
            out.println();
            
            // update2(int)
            out.println("    public boolean update2(int time) {");
            if (hots.size() != 0) {
                if (updateEvenOffline) {
                    out.println("        remainSeconds -= time / 1000;");
                }
                out.println("        if (remainSeconds <= 0) {");
                out.println("            return false;");
                out.println("        }");
            }
            if (duration[0] >= 0) {
                if (!updateEvenOffline) {
                    out.println("        endTime += time;");
                }
                out.println("        if (endTime <= Time.currTime) {");
                out.println("            return false;");
                out.println("        }");
            }
            out.println("        return true;");
            out.println("    }");
            out.println();
        }

        // 实现SkillEnhancer接口
        if (ifs.contains("SkillEnhancer")) {
            List<EffectConfig> skillEffs = effects.findEffects(new int[] { EffectConfig.CHANGE_MP_USE,
                    EffectConfig.CHANGE_CD_TIME, EffectConfig.CHANGE_DISTANCE, EffectConfig.CHANGE_ACT_TIME,
                    EffectConfig.CHANGE_RANGE });
            
            // getAffectSkillIDs()
            out.println("    private IntHashSet changeSkills;");
            out.println("    public IntHashSet getAffectSkillIDs() {");
            out.println("        if (changeSkills == null) {");
            out.println("            int[] idarr;");
            out.println("            changeSkills = new IntHashSet();");
            for (EffectConfig eff : skillEffs) {
                String varName = getFieldName(eff, 0, false);
                out.println("            idarr = Utils.stringToIntArray(" + varName + ", ',');");
                out.println("            for (int sid : idarr) {");
                out.println("                changeSkills.add(sid);");
                out.println("            }");
            }
            out.println("        }");
            out.println("        return changeSkills;");
            out.println("    }");
            out.println();

            // updateCDTime(Skill skill, float cd)
            out.println("    public float updateCDTime(Skill skill, float cd) {");
            EffectConfig cdEff = effects.findEffect(EffectConfig.CHANGE_CD_TIME);
            if (cdEff != null) {
                String varName = getFieldName(cdEff, 1, false);
                out.println("            return cd * (1.0f + " + varName + " / 100.0f);");
            }
            else {
                out.println("            return cd;");
            }
            out.println("    }");
            out.println();

            // updateDistance(Skill skill, float distance)
            out.println("    public float updateDistance(Skill skill, float distance) {");
            EffectConfig distEff = effects.findEffect(EffectConfig.CHANGE_DISTANCE);
            if (distEff != null) {
                String varName = getFieldName(distEff, 1, false);
                out.println("            return distance * (1.0f + " + varName + " / 100.0f);");
            }
            else {
                out.println("            return distance;");
            }
            out.println("    }");
            out.println();

            // updateActTime(Skill skill, float actTime)
            out.println("    public float updateActTime(Skill skill, float actTime) {");
            EffectConfig actTimeEff = effects.findEffect(EffectConfig.CHANGE_ACT_TIME);
            if (actTimeEff != null) {
                String varName = getFieldName(actTimeEff, 1, false);
                out.println("            return actTime * (1.0f + " + varName + " / 100.0f);");
            }
            else {
                out.println("            return actTime;");
            }
            out.println("    }");
            out.println();

            // updateRange(Skill skill, float range)
            out.println("    public float updateRange(Skill skill, float range) {");
            EffectConfig rangeEff = effects.findEffect(EffectConfig.CHANGE_RANGE);
            if (rangeEff != null) {
                String varName = getFieldName(rangeEff, 1, false);
                out.println("            return range * (1.0f + " + varName + " / 100.0f);");
            }
            else {
                out.println("            return range;");
            }
            out.println("    }");
            out.println();

            // updateMP(Skill skill, float mp)
            out.println("    public float updateMP(Skill skill, float mp) {");
            EffectConfig mpEff = effects.findEffect(EffectConfig.CHANGE_MP_USE);
            if (mpEff != null) {
                String varName = getFieldName(mpEff, 1, false);
                out.println("            return mp * (1.0f + " + varName + " / 100.0f);");
            }
            else {
                out.println("            return mp;");
            }
            out.println("    }");
            out.println();
        }

        // 实现PropertyEnhancer接口
        if (ifs.contains("PropertyEnhancer")) {
            out.println("    public void enhance(PropertyCalculator pc) {");
            out.println("        if (!checkWeapon(pc.unit)) {");
            out.println("            return;");
            out.println("        }");
            for (EffectConfig eff : effects.getAllEffects()) {
                String p1 = getFieldName(eff, 0, false);
                String p2 = getFieldName(eff, 1, false);
                switch (eff.getType()) {
                case EffectConfig.CHANGE_PHYICAL_AP:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.attackpowerup += multiple * " + p1 + ";");
                    out.println("        pc.attackpowerdown += multiple * " + p1 + ";");
                    out.println("        pc.attackpowerRate += multiple * " + p2 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_MAGIC_AP:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.spellpowerRate += multiple * " + p2 + " / 100.0f;");
                    out.println("        pc.spellpower += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_WEAPON_ATK:
                    // Effect_PercentAdd：百分比float
                    out.println("        if (pc.unit.equipments.getWeapon() != null) {");
                    out.println("            GameItem t = pc.unit.equipments.getWeapon();");
                    out.println("            pc.attackpowerup += t.getMaxAttack() * multiple * " + p1 + " / 100.0f;");
                    out.println("            pc.attackpowerdown += t.getMinAttack() * multiple * " + p1 + " / 100.0f;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_WEAPON_MATK:
                    // Effect_PercentAdd：百分比float
                    out.println("        if (pc.unit.equipments.getWeapon() != null) {");
                    out.println("            GameItem t = pc.unit.equipments.getWeapon();");
                    out.println("            pc.spellpower += t.getMagicPower() * multiple * " + p1 + " / 100.0f;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_ARMOR:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.defenseRate += multiple * " + p2 + " / 100.0f;");
                    out.println("        pc.defense += multiple * " + p1 + ";");
                    out.println("        if (pc.defense < 0) {");
                    out.println("            pc.defense = 0;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_PHYSICAL_HIT:
                    // Effect_PercentAdd：百分比float
                    out.println("        pc.hit += multiple * " + p1 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_PHYSICAL_CRIT:
                    // Effect_PercentAdd：百分比float
                    out.println("        pc.critical += multiple * " + p1 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_PHYSICAL_DODGE:
                    // Effect_PercentAdd：百分比float
                    out.println("        pc.dodge += multiple * " + p1 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_MAGIC_CRIT:
                    // Effect_PercentAdd：百分比float
                    out.println("        pc.spellcritical += multiple * " + p1 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_MAGIC_HIT:
                    // Effect_PercentAdd：百分比float
                    out.println("        pc.spellhit += multiple * " + p1 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_MAGIC_DODGE:
                    // Effect_PercentAdd：百分比float
                    out.println("        pc.spelldodge += multiple * " + p1 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_PHYSICAL_HIT_RATE:
                    // Effect_FixValueAdd：数额int
                    out.println("        pc.hitrating += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_PHYSICAL_CRIT_RATE:
                    // Effect_FixValueAdd：数额int
                    out.println("        pc.criticalrating += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_PHYSICAL_DODGE_RATE:
                    // Effect_FixValueAdd：数额int
                    out.println("        pc.dodgerating += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_MAGIC_CRIT_RATE:
                    // Effect_FixValueAdd：数额int
                    out.println("        pc.spellcriticalrating += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_MAGIC_HIT_RATE:
                    // Effect_FixValueAdd：数额int
                    out.println("        pc.spellhitrating += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_MAGIC_DODGE_RATE:
                    // Effect_FixValueAdd：数额int
                    out.println("        pc.spelldodgerating += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_MP_RENEW:
                    // Effect_FixValueAdd：数额int
                    out.println("        pc.manarestore += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_HP_RENEW:
                    // Effect_FixValueAdd：数额int
                    out.println("        pc.healthrestore += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_SPEED:
                    // Effect_PercentAdd：百分比float
                    out.println("        if (" + p1 + " > 0.0f) {");
                    out.println("            pc.fast(multiple * " + p1 + " / 100.0f);");
                    out.println("        } else {");
                    out.println("            pc.slow(multiple * " + p1 + " / -100.0f);");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_MAXHP:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.hp += multiple * " + p1 + ";");
                    out.println("        pc.hpRate += multiple * " + p2 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_MAGIC_ARMOR:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.spellDefenseRate += multiple * " + p2 + " / 100.0f;");
                    out.println("        pc.spelldefense += multiple * " + p1 + ";");
                    out.println("        if (pc.spelldefense < 0) {");
                    out.println("            pc.spelldefense = 0;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_STA:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.stamina *= 1.0f + multiple * " + p2 + " / 100.0f;");
                    out.println("        pc.stamina += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_AGI:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.agility *= 1.0f + multiple * " + p2 + " / 100.0f;");
                    out.println("        pc.agility += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_STR:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.strength *= 1.0f + multiple * " + p2 + " / 100.0f;");
                    out.println("        pc.strength += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_INT:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.intellect *= 1.0f + multiple * " + p2 + " / 100.0f;");
                    out.println("        pc.intellect += multiple * " + p1 + ";");
                    break;
                case EffectConfig.CHANGE_BASIC_MAGIC_AP:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.spellpower += multiple * " + p1 + ";");
                    out.println("        pc.basicSpellPowerRate += multiple * " + p2 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_BASIC_HP:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.hp += multiple * " + p1 + ";");
                    out.println("        pc.basicHpRate += multiple * " + p2 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_BASIC_MP:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.mp += multiple * " + p1 + ";");
                    out.println("        pc.basicMpRate += multiple * " + p2 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_MAGIC_HEAL:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        pc.spellheal += multiple * " + p1 + ";");
                    out.println("        pc.spellhealRate += multiple * " + p2 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_EXP_RATE:
                    // Effect_PercentAdd: 百分比float
                    out.println("        pc.expRatio += multiple * " + p1 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_HORSE_EXP_RATE:
                    // Effect_PercentAdd: 百分比float
                    out.println("        pc.horseExpRatio += multiple * " + p1 + " / 100.0f;");
                    break;
                case EffectConfig.CHANGE_MONEY_RATE:
                    // Effect_PercentAdd: 百分比float
                    out.println("        pc.moneyRatio += multiple * " + p1 + " / 100.0f;");
                    break;
                }
            }
            out.println("    }");
            out.println();
        }

        // 实现CombatEffect接口
        if (ifs.contains("CombatEffect")) {
            // public void preHit(CombatContext context, boolean isActive)
            out.println("    public void preHit(CombatContext context, boolean isActive) {");
            out.println("        if (!checkWeapon(isActive ? context.source : context.target)) {");
            out.println("            return;");
            out.println("        }");
            if (limitSkillConfig != null) {
                out.println("        if (!limitSkills.contains(context.skill.getGroupId())) {");
                out.println("            return;");
                out.println("        }");
            }
            for (EffectConfig eff : effects.getAllEffects()) {
                String p1 = getFieldName(eff, 0, false);
                String p2 = getFieldName(eff, 1, false);
                String p3 = getFieldName(eff, 2, false);
                String p4 = getFieldName(eff, 3, false);
                String p5 = getFieldName(eff, 4, false);
                String p6 = getFieldName(eff, 5, false);
                switch (eff.getType()) {
                case EffectConfig.CHANGE_BATTLE_PHYSICAL_HIT:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (isActive && context.damageType == CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.hitRate += " + p1 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_PHYSICAL_CRIT:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (isActive && context.damageType == CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.critRate += " + p1 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_PHYSICAL_DODGE:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.damageType == CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.dodge += " + p1 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_PHYSICAL_CRITED:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.damageType == CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.critRate += " + p1 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_MAGIC_CRIT:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (isActive && context.damageType != CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.critRate += " + p1 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_MAGIC_HIT:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (isActive && context.damageType != CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.hitRate += " + p1 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_MAGIC_DODGE:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.damageType != CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.dodge += " + p1 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_MAGIC_CRITED:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.damageType != CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.critRate += " + p1 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.SET_VARIABLE:
                    // Effect_SetVariable：变量名string，变量值float(重复3次)
                    out.println("        if (isActive) {");
                    out.println("            if (" + p1 + ".length() > 0) {");
                    out.println("                context.skillParams.put(" + p1 + ", new Float(" + p2 + "));");
                    out.println("            }");
                    out.println("            if (" + p3 + ".length() > 0) {");
                    out.println("                context.skillParams.put(" + p3 + ", new Float(" + p4 + "));");
                    out.println("            }");
                    out.println("            if (" + p5 + ".length() > 0) {");
                    out.println("                context.skillParams.put(" + p5 + ", new Float(" + p6 + "));");
                    out.println("            }");
                    out.println("        }");
                    break;
                }
            }
            out.println("    }");
            out.println();

            // public void postHit(CombatContext context, boolean isActive)
            out.println("    public void postHit(CombatContext context, boolean isActive) {");
            out.println("        if (!checkWeapon(isActive ? context.source : context.target)) {");
            out.println("            return;");
            out.println("        }");
            if (limitSkillConfig != null) {
                out.println("        if (!limitSkills.contains(context.skill.getGroupId())) {");
                out.println("            return;");
                out.println("        }");
            }
            for (EffectConfig eff : effects.getAllEffects()) {
                String p1 = getFieldName(eff, 0, false);
                String p2 = getFieldName(eff, 1, false);
                switch (eff.getType()) {
                case EffectConfig.IMMUNE_PHYICAL_ATTACK:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.hited() && context.damageType == CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.attackResult = CombatContext.ATTACKRESULT_IMMUNE;");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.IMMUNE_MAGIC_ATTACK:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.hited() && context.damageType == CombatContext.DAMAGE_MAGIC) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.attackResult = CombatContext.ATTACKRESULT_IMMUNE;");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.IMMUNE_SLOW_ATTACK:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.hited() && context.skill instanceof SlowSkill) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.attackResult = CombatContext.ATTACKRESULT_IMMUNE;");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.IMMUNE_FEAR:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.hited() && context.skill instanceof FearSkill) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.attackResult = CombatContext.ATTACKRESULT_IMMUNE;");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.IMMUNE_DUMB:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.hited() && context.skill instanceof DumbSkill) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.attackResult = CombatContext.ATTACKRESULT_IMMUNE;");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.IMMUNE_PARALYZE:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.hited() && context.skill instanceof ParalyzeSkill) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.attackResult = CombatContext.ATTACKRESULT_IMMUNE;");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.IMMUNE_STAY:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.hited() && context.skill instanceof StaySkill) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.attackResult = CombatContext.ATTACKRESULT_IMMUNE;");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.IMMUNE_BREAKATTACK:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.hited() && context.skill instanceof BreakAttackSkill) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.attackResult = CombatContext.ATTACKRESULT_IMMUNE;");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                }
            }
            out.println("    }");
            out.println();

            // public void preDamage(CombatContext context, boolean isActive)
            out.println("    public void preDamage(CombatContext context, boolean isActive) {");
            out.println("        if (!checkWeapon(isActive ? context.source : context.target)) {");
            out.println("            return;");
            out.println("        }");
            if (limitSkillConfig != null) {
                out.println("        if (!limitSkills.contains(context.skill.getGroupId())) {");
                out.println("            return;");
                out.println("        }");
            }
            for (EffectConfig eff : effects.getAllEffects()) {
                String p1 = getFieldName(eff, 0, false);
                String p2 = getFieldName(eff, 1, false);
                switch (eff.getType()) {
                case EffectConfig.CHANGE_BATTLE_PHYICAL_AP:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (isActive && context.damageType == CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.attackPower += " + p1 + ";");
                    out.println("            context.attackPowerRate += " + p2 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_MAGIC_AP:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (isActive && (context.damageType == CombatContext.DAMAGE_MAGIC || context.damageType == CombatContext.DAMAGE_DECMP)) {");
                    out.println("            context.attackPower += " + p1 + ";");
                    out.println("            context.attackPowerRate += " + p2 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_WEAPON_ATK:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (isActive && context.damageType == CombatContext.DAMAGE_PHYSICAL && context.source.equipments != null && context.source.equipments.getWeapon() != null) {");
                    out.println("            GameItem weapon = context.source.equipments.getWeapon();");
                    out.println("            int addDamage = CommonUtil.getCount(RND, weapon.getMinAttack(), weapon.getMaxAttack());");
                    out.println("            context.attackPower += addDamage * " + p1 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_WEAPON_MATK:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (isActive && context.damageType == CombatContext.DAMAGE_MAGIC && context.source.equipments != null && context.source.equipments.getWeapon() != null) {");
                    out.println("            GameItem weapon = context.source.equipments.getWeapon();");
                    out.println("            context.attackPower += weapon.getMagicPower() * " + p1 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_THREAT:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (isActive) {");
                    out.println("            context.threatAdd += multiple * " + p1 + ";");
                    out.println("            context.threatAddRate += multiple * " + p2 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.IGNORE_ARMOR:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (isActive && context.damageType == CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.armor -= multiple * " + p1 + ";");
                    out.println("            context.armorRate -= multiple * " + p2 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.IGNORE_MAGIC_ARMOR:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (isActive && context.damageType == CombatContext.DAMAGE_MAGIC) {");
                    out.println("            context.armor -= multiple * " + p1 + ";");
                    out.println("            context.armorRate -= multiple * " + p2 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_ARMOR:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (!isActive && context.damageType == CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.armor += multiple * " + p1 + ";");
                    out.println("            context.armorRate += multiple * " + p2 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_BATTLE_MAGIC_ARMOR:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (!isActive && context.damageType == CombatContext.DAMAGE_MAGIC) {");
                    out.println("            context.armor += multiple * " + p1 + ";");
                    out.println("            context.armorRate += multiple * " + p2 + " / 100.0f;");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                }
            }
            out.println("    }");
            out.println();

            // public void postDamage(CombatContext context, boolean isActive)
            out.println("    public void postDamage(CombatContext context, boolean isActive) {");
            out.println("        if (!checkWeapon(isActive ? context.source : context.target)) {");
            out.println("            return;");
            out.println("        }");
            if (limitSkillConfig != null) {
                out.println("        if (!limitSkills.contains(context.skill.getGroupId())) {");
                out.println("            return;");
                out.println("        }");
            }
            for (EffectConfig eff : effects.getAllEffects()) {
                String p1 = getFieldName(eff, 0, false);
                String p2 = getFieldName(eff, 1, false);
                String p3 = getFieldName(eff, 2, false);
                String p4 = getFieldName(eff, 3, false);
                switch (eff.getType()) {
                case EffectConfig.APPEND_MAGIC_DAMAGE:
                    // Effect_FixValueAdd: 数额int
                    out.println("        if (isActive && context.isDamage()) {");
                    out.println("            context.appendSpellDamage(multiple * " + p1 + ");");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.DOUBLE_DAMAGE_ON_HIT:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (isActive && context.isDamage()) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.damage *= 2;");
                    out.println("                context.threat *= 2;");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.MP_SHIELD:
                    // Effect_Shield: 总量int，抵消比例float，消耗率float
                    out.println("        if (!isActive && context.isDamage()) {");
                    out.println("            int absorb = (int)(context.damage * " + p3 + " / 100.0f);");
                    out.println("            if (absorb > remainAbsorb) {");
                    out.println("                absorb = remainAbsorb;");
                    out.println("            }");
                    out.println("            int needMp = (int)(absorb * " + p4 + " / 100.0f);");
                    out.println("            if (needMp > context.target.mp) {");
                    out.println("                needMp = context.target.mp;");
                    out.println("                absorb = (int)(needMp * 100.0f / " + p4 + ");");
                    out.println("            }");
                    out.println("            if (absorb > 0) {");
                    out.println("                context.damage -= absorb;");
                    out.println("                remainAbsorb -= absorb;");
                    out.println("                context.target.setMp(context.target.mp - needMp, true);");
                    out.println("            }");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.REDUCE_PHYSICAL_DAMAGE:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (!isActive && context.damageType == CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            int value = multiple * " + p1 + ";");
                    out.println("            value += context.damage * multiple * " + p2 + " / 100.0f;");
                    out.println("            if (value > context.damage) {");
                    out.println("                value = context.damage;");
                    out.println("            }");
                    out.println("            if (context.damage > 0) {");
                    out.println("                context.threat -= value * context.threat / context.damage;");
                    out.println("                context.damage -= value;");
                    out.println("            }");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.REDUCE_MAGIC_DAMAGE:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (!isActive && context.damageType == CombatContext.DAMAGE_MAGIC) {");
                    out.println("            int value = multiple * " + p1 + ";");
                    out.println("            value += context.damage * multiple * " + p2 + " / 100.0f;");
                    out.println("            if (value > context.damage) {");
                    out.println("                value = context.damage;");
                    out.println("            }");
                    out.println("            if (context.damage > 0) {");
                    out.println("                context.threat -= value * context.threat / context.damage;");
                    out.println("                context.damage -= value;");
                    out.println("            }");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_CURE_EFFECT:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (isActive && context.damageType == CombatContext.DAMAGE_HEAL) {");
                    out.println("            context.damage += context.damage * multiple * " + p2 + " / 100.0f;");
                    out.println("            context.damage += multiple * " + p1 + ";");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                }
            }
            out.println("    }");
            out.println();

            // public void finished(CombatContext context, boolean isActive)
            out.println("    public void finished(CombatContext context, boolean isActive) {");
            out.println("        if (!checkWeapon(isActive ? context.source : context.target)) {");
            out.println("            return;");
            out.println("        }");
            if (limitSkillConfig != null) {
                out.println("        if (!limitSkills.contains(context.skill.getGroupId())) {");
                out.println("            return;");
                out.println("        }");
            }
            for (EffectConfig eff : effects.getAllEffects()) {
                String p1 = getFieldName(eff, 0, false);
                String p2 = getFieldName(eff, 1, false);
                String p3 = getFieldName(eff, 2, false);
                String p4 = getFieldName(eff, 3, false);
                String p5 = getFieldName(eff, 4, false);
                String p6 = getFieldName(eff, 5, false);
                switch (eff.getType()) {
                case EffectConfig.ADD_MP_ON_HIT:
                    // Effect_CureOnHit: 概率float，固定值int，占上限比例float，占伤害比例float
                    out.println("        if (isActive && context.hited() && context.isDamage()) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                int addmp = " + p2 + ";");
                    out.println("                addmp += context.source.maxmp * " + p3 + " / 100.0f;");
                    out.println("                addmp += context.damage * " + p4 + " / 100.0f;");
                    out.println("                context.activeSkills.add(new FixedAddMPSkill(addmp));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.ADD_DEBUFF_ON_HIT:
                    // Effect_AddBuff: 概率float，概率补充变量String，BUFFID int，BUFF级别int
                    out.println("        if (isActive && context.hited() && context.isAttack()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(multiple * 100 * (vo.floatValue() + " + p1 + ")), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.target.buffs.addBuff(BuffUtil.createBuff(" + p3 + ", "
                            + p4 + ", context.source, context.target, context.damage));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.ADD_BUFF_ON_HIT:
                    // Effect_AddBuff: 概率float，概率补充变量String，BUFFID int，BUFF级别int
                    out.println("        if (isActive && context.hited() && context.isAttack()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(multiple * 100 * (vo.floatValue() + " + p1 + ")), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.source.buffs.addBuff(BuffUtil.createBuff(" + p3 + ", "
                            + p4 + ", context.source, context.source, context.damage));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.CRIT_ACTIVE_BUFF:
                    // Effect_CritActiveBuff: 概率float，BUFFID int，BUFF级别int
                    out.println("        if (isActive && context.critical() && context.isAttack()) {");
                    out.println("            boolean hit = CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000);");
                    out.println("            if (hit) {");
                    out.println("                context.source.buffs.addBuff(BuffUtil.createBuff(" + p2 + ", "
                            + p3 + ", context.source, context.source, context.damage));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.CRITED_ACTIVE_BUFF:
                    // Effect_CritActiveBuff: 概率float，BUFFID int，BUFF级别int
                    out.println("        if (!isActive && context.critical() && context.isAttack()) {");
                    out.println("            boolean hit = CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000);");
                    out.println("            if (hit) {");
                    out.println("                context.target.buffs.addBuff(BuffUtil.createBuff(" + p2 + ", "
                            + p3 + ", context.target, context.target, context.damage));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.FIRST_THREAT_ON_HIT:
                    // Effect_FirstThreat: 无参数
                    out.println("        if (isActive && context.hited() && context.isAttack()) {");
                    out.println("            Attack.makeFirstThreat(context.source, context.target);");
                    out.println("            effectTimes++;");
                    out.println("        }");
                    break;
                case EffectConfig.FEAR_ON_HIT:
                    // Effect_FearOnHit: 概率float，概率补充变量String，持续时间int
                    out.println("        if (isActive && context.hited() && context.isDamage()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(multiple * 100 * (vo.floatValue() + " + p1 + ")), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.activeSkills.add(new FearSkill(" + p3 + "));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.SLOW_ON_HIT:
                    // Effect_SlowOnHit:
                    // 概率float，概率补充变量String，减速级别int，减速级别补充变量String，持续时间int，持续时间变量float
                    out.println("        if (isActive && context.hited() && context.isDamage()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(multiple * 100 * (vo.floatValue() + " + p1 + ")), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                int sl = " + p3 + ";");
                    out.println("                Float vo = context.skillParams.get(" + p4 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    sl += vo.intValue();");
                    out.println("                }");
                    out.println("                int tm = " + p5 + ";");
                    out.println("                vo = context.skillParams.get(" + p6 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    tm *= 1.0f + vo.floatValue() / 100.0f;");
                    out.println("                }");
                    out.println("                context.activeSkills.add(new SlowSkill(sl, tm));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.PARALYZE_ON_HIT:
                    // Effect_FearOnHit: 概率float，概率补充变量String，持续时间int
                    out.println("        if (isActive && context.hited() && context.isDamage()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(multiple * 100 * (vo.floatValue() + " + p1 + ")), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.activeSkills.add(new ParalyzeSkill(" + p3 + "));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.STAY_ON_HIT:
                    // Effect_FearOnHit: 概率float，概率补充变量String，持续时间int
                    out.println("        if (isActive && context.hited() && context.isDamage()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(multiple * 100 * (vo.floatValue() + " + p1 + ")), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.activeSkills.add(new StaySkill(" + p3 + "));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.DUMB_ON_HIT:
                    // Effect_FearOnHit: 概率float，概率补充变量String，持续时间int
                    out.println("        if (isActive && context.hited() && context.isDamage()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(multiple * 100 * (vo.floatValue() + " + p1 + ")), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.activeSkills.add(new DumbSkill(" + p3 + "));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.REPEAT_ON_HIT:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (isActive && context.hited() && context.isDamage()) {");
                    out.println("            if (CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000)) {");
                    out.println("                context.activeSkills.add(context.skill);");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.DEC_MP_ON_HIT:
                    // Effect_CureOnHit: 概率float，固定值int，占上限比例float，占伤害比例float
                    out.println("        if (isActive && context.hited() && context.isDamage()) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                int decmp = " + p2 + ";");
                    out.println("                decmp += context.target.maxmp * " + p3 + " / 100.0f;");
                    out.println("                decmp += context.damage * " + p4 + " / 100.0f;");
                    out.println("                context.activeSkills.add(new FixedDecMPSkill(decmp));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.ADD_HP_ON_HIT:
                    // Effect_CureOnHit: 概率float，固定值int，占上限比例float，占伤害比例float
                    out.println("        if (isActive && context.hited() && context.isDamage()) {");
                    out.println("            int rate = (int)(multiple * 100 * " + p1 + ");");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                int cure = " + p2 + ";");
                    out.println("                cure += context.source.maxhp * " + p3 + " / 100.0f;");
                    out.println("                cure += context.damage * " + p4 + " / 100.0f;");
                    out.println("                context.activeSkills.add(new FixedHealSkill(cure));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.TWO_HIT_ON_HIT:
                    // Effect_Hit3Times: 无参数
                    out.println("        context.activeSkills.add(context.skill);");
                    out.println("        context.activeSkills.add(context.skill);");
                    out.println("        effectTimes++;");
                    break;
                case EffectConfig.RELIVE_TARGET:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (context.source == context.target) {");
                    out.println("            if (isActive && !context.source.isAlive()) {");
                    out.println("                int hp = (int)(context.source.maxhp * " + p1 + " / 100.0f);");
                    out.println("                int mp = (int)(context.source.maxmp * " + p1 + " / 100.0f);");
                    out.println("                context.source.relive(hp, mp);");
                    out.println("            }");
                    out.println("        } else {");
                    out.println("            if (isActive) {");
                    out.println("                if (!context.target.isAlive()) {");
                    out.println("                    // TODO: 向死亡的人发送一个复活请求");
                    out.println("                } else {");
                    out.println("                    if (!context.target.isAlive()) {");
                    out.println("                        int hp = (int)(context.target.maxhp * " + p1 + " / 100.0f);");
                    out.println("                        int mp = (int)(context.target.maxmp * " + p1 + " / 100.0f);");
                    out.println("                        context.target.relive(hp, mp);");
                    out.println("                    }");
                    out.println("                }");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.COUNTER_ATTACK:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (!isActive && context.hited() && context.isDamage()) {");
                    out.println("            if (CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000)) {");
                    out.println("                context.passiveSkills.add(new AutoAttackSkill(1));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.BOUNCE:
                    // Effect_Bounce: 概率float，伤害类型int，固定值int，占伤害比例float
                    out.println("        if (!isActive && context.hited() && context.isDamage()) {");
                    out.println("            if (CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000)) {");
                    out.println("                int dmg = " + p3 + ";");
                    out.println("                dmg += context.damage * " + p4 + " / 100.0f;");
                    out.println("                context.passiveSkills.add(new FixedDamageSkill(" + p2 + ", dmg));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.VAMPIRE_ON_HIT:
                    // Effect_Vampire: 转换比例float，有效范围int
                    out.println("        if (!isActive && context.hited() && context.isDamage() && context.source.ref().equals(source)) {");
                    out.println("            Player p = (Player)ObjectAccessor.getGameObject(source);");
                    out.println("            if (p != null && p.isAlive()) {");
                    out.println("                int value = (int)(context.damage * multiple * " + p1 + " / 100.0f);");
                    out.println("                if (p.party == null) {");
                    out.println("                    p.setHp(p.hp + value, true);");
                    out.println("                } else {");
                    out.println("                    List<Player> ps = p.party.getPlayerInRange(context.target.map.map, "
                            + p2 + " * 8, context.target.x, context.target.y);");
                    out.println("                    for (Player pp : ps) {");
                    out.println("                        if (pp.isAlive()) {");
                    out.println("                         pp.setHp(pp.hp + value, true);");
                    out.println("                        }");
                    out.println("                    }");
                    out.println("                }");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.ADD_DEBUFF_ON_HITED:
                    // Effect_AddBuff: 概率float，概率补充变量String，BUFFID int，BUFF级别int
                    out.println("        if (!isActive && context.hited() && context.isAttack()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(multiple * 100 * (vo.floatValue() + " + p1 + ")), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.source.buffs.addBuff(BuffUtil.createBuff(" + p3 + ", "
                            + p4 + ", context.target, context.source, context.damage));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.ADD_BUFF_ON_HITED:
                    // Effect_AddBuff: 概率float，概率补充变量String，BUFFID int，BUFF级别int
                    out.println("        if (!isActive && context.hited() && context.isAttack()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(multiple * 100 * (vo.floatValue() + " + p1 + ")), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.target.buffs.addBuff(BuffUtil.createBuff(" + p3 + ", "
                            + p4 + ", context.target, context.target, context.damage));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.SLOW_ON_HITED:
                    // Effect_SlowOnHited: 概率float，减速级别 int，减速时间int
                    out.println("        if (!isActive && context.hited() && context.isDamage()) {");
                    out.println("            if (CommonUtil.hit(RND, (int)(multiple * 100 * " + p1 + "), 10000)) {");
                    out.println("                context.passiveSkills.add(new SlowSkill(" + p2 + ", " + p3 + "));");
                    out.println("                effectTimes++;");
                    out.println("            }");
                    out.println("        }");
                    break;
                }
            }
            out.println("    }");
            out.println();
        }
        
        // 实现ParamEnhancer接口
        if (ifs.contains("ParamEnhancer")) {
            // void getEnhanceParams(ParamEnhanceSet enhanceSet);
            out.println("    public void getEnhanceParams(ParamEnhanceSet enhanceSet) {");
            for (EffectConfig eff : effects.getAllEffects()) {
                switch (eff.getType()) {
                case EffectConfig.CHANGE_PARAM:
                    out.println("        switch (level) {");
                    for (int lvl = 0; lvl < maxLevel; lvl++) {
                        // Effect_ChangeParam: 重复10次（影响参数ParamIndicator，数额float，比例float）
                        out.println("        case " + (lvl + 1) + ":");
                        for (int i = 0; i < 10; i++) {
                            String p1 = getFieldName(eff, i * 3, false);
                            String p2 = getFieldName(eff, i * 3 + 1, false);
                            String p3 = getFieldName(eff, i * 3 + 2, false);
                            ParamIndicator[] parr = (ParamIndicator[])eff.getParam(i * 3);
                            EffectParamRef pref = parr[lvl].getParamRef(owner);
                            if (pref == null) {
                                continue;
                            }
                            out.print("            enhanceSet.add(ParamEnhanceSet.");
                            switch (parr[0].type) {
                            case ParamIndicator.TYPE_SKILL_ACTIVE:
                                out.print("TYPE_SKILL_ACTIVE");
                                break;
                            case ParamIndicator.TYPE_SKILL_PASSIVE:
                                out.print("TYPE_SKILL_PASSIVE");
                                break;
                            case ParamIndicator.TYPE_BUFF_OWNER:
                                out.print("TYPE_BUFF_OWNER");
                                break;
                            case ParamIndicator.TYPE_BUFF_SOURCE:
                                out.print("TYPE_BUFF_SOURCE");
                                break;
                            }
                            out.print(", " + parr[0].id);
                            if (pref.effect instanceof GeneralConfig) {
                                out.print(", \"buffTime\"");
                            } else {
                                out.print(", \"" + getFieldName(pref.effect, pref.index, false) + "\"");
                            }
                            out.print(", " + p2);
                            out.println(", " + p3 + " / 100.0f);");
                        }
                        out.println("            break;");
                    }
                    out.println("        }");
                    break;
                }
            }
            out.println("    }");
            out.println();
            
            // void removeEnhanceParams(ParamEnhanceSet enhanceSet);
            out.println("    public void removeEnhanceParams(ParamEnhanceSet enhanceSet) {");
            for (EffectConfig eff : effects.getAllEffects()) {
                switch (eff.getType()) {
                case EffectConfig.CHANGE_PARAM:
                    out.println("        switch (level) {");
                    for (int lvl = 0; lvl < maxLevel; lvl++) {
                        // Effect_ChangeParam: 重复10次（影响参数ParamIndicator，数额float，比例float）
                        out.println("        case " + (lvl + 1) + ":");
                        for (int i = 0; i < 10; i++) {
                            String p1 = getFieldName(eff, i * 3, false);
                            String p2 = getFieldName(eff, i * 3 + 1, false);
                            String p3 = getFieldName(eff, i * 3 + 2, false);
                            ParamIndicator[] parr = (ParamIndicator[])eff.getParam(i * 3);
                            EffectParamRef pref = parr[lvl].getParamRef(owner);
                            if (pref == null) {
                                continue;
                            }
                            out.print("            enhanceSet.remove(ParamEnhanceSet.");
                            switch (parr[0].type) {
                            case ParamIndicator.TYPE_SKILL_ACTIVE:
                                out.print("TYPE_SKILL_ACTIVE");
                                break;
                            case ParamIndicator.TYPE_SKILL_PASSIVE:
                                out.print("TYPE_SKILL_PASSIVE");
                                break;
                            case ParamIndicator.TYPE_BUFF_OWNER:
                                out.print("TYPE_BUFF_OWNER");
                                break;
                            case ParamIndicator.TYPE_BUFF_SOURCE:
                                out.print("TYPE_BUFF_SOURCE");
                                break;
                            }
                            out.print(", " + parr[0].id);
                            if (pref.effect instanceof GeneralConfig) {
                                out.print(", \"buffTime\"");
                            } else {
                                out.print(", \"" + getFieldName(pref.effect, pref.index, false) + "\"");
                            }
                            out.print(", " + p2);
                            out.println(", " + p3 + " / 100.0f);");
                        }
                        out.println("            break;");
                    }
                    out.println("        }");
                    break;
                }
            }
            out.println("    }");
            out.println();
        }

        out.println("}");
        
        this.implClass = packageName + "." + className;
    }

    /*
     * 生成参数局部变量全部拷贝的代码。
     */
    private void generateFieldsCopy(PrintWriter out, String indent) {
        for (EffectParamRef pr : effects.getAllParams()) {
            String name = getFieldName(pr.effect, pr.index, false);
            out.println(indent + name + " = other." + name + ";");
        }
    }

    /**
     * 取得存储某个效果参数的变量名。
     */
    public static String getFieldName(EffectConfig eff, int index, boolean isStatic) {
        try {
            String name = EffectConfig.TYPE_PARAMS[eff.getType()][index];
            if (isStatic) {
                return name.toUpperCase();
            } else {
                return name;
            }
        } catch (Exception e) {
            return "";
        }
    }

    /*
     * 根据BUFF配置的效果判断此BUFF类需要实现的接口。
     */
    private Set<String> getJavaInterface() {
        HashSet<String> ret = new HashSet<String>();
        if (buffType == BUFF_TYPE_STATIC) {
            ret.add("SkillBuff");
        }
        if (duration[0] >= 0) {
            ret.add("Updatable");
        }
        for (EffectConfig eff : effects.getAllEffects()) {
            switch (eff.getType()) {
            case EffectConfig.CHANGE_PHYICAL_AP:
            case EffectConfig.CHANGE_MAGIC_AP:
            case EffectConfig.CHANGE_WEAPON_ATK:
            case EffectConfig.CHANGE_WEAPON_MATK:
                ret.add("PropertyEnhancer");
                break;
            case EffectConfig.CHANGE_THREAT:
                ret.add("CombatEffect");
                break;
            case EffectConfig.CHANGE_ARMOR:
            case EffectConfig.CHANGE_PHYSICAL_HIT:
            case EffectConfig.CHANGE_PHYSICAL_CRIT:
            case EffectConfig.CHANGE_PHYSICAL_DODGE:
            case EffectConfig.CHANGE_MAGIC_CRIT:
            case EffectConfig.CHANGE_MP_RENEW:
            case EffectConfig.CHANGE_HP_RENEW:
            case EffectConfig.CHANGE_SPEED:
            case EffectConfig.CHANGE_MAXHP:
                ret.add("PropertyEnhancer");
                break;
            case EffectConfig.CHANGE_CURE_EFFECT:
            case EffectConfig.APPEND_MAGIC_DAMAGE:
            case EffectConfig.IGNORE_ARMOR:
            case EffectConfig.ADD_MP_ON_HIT:
            case EffectConfig.ADD_DEBUFF_ON_HIT:
            case EffectConfig.ADD_BUFF_ON_HIT:
            case EffectConfig.FIRST_THREAT_ON_HIT:
            case EffectConfig.FEAR_ON_HIT:
            case EffectConfig.SLOW_ON_HIT:
            case EffectConfig.PARALYZE_ON_HIT:
            case EffectConfig.STAY_ON_HIT:
            case EffectConfig.REPEAT_ON_HIT:
            case EffectConfig.DOUBLE_DAMAGE_ON_HIT:
            case EffectConfig.DEC_MP_ON_HIT:
            case EffectConfig.ADD_HP_ON_HIT:
            case EffectConfig.TWO_HIT_ON_HIT:
            case EffectConfig.RELIVE_TARGET:
                ret.add("CombatEffect");
                break;
            case EffectConfig.CURE_TARGET:
                throw new IllegalArgumentException("不能使用治疗目标效果，如果想提高治疗效果，请使用提高治疗量效果。");
            case EffectConfig.IMMUNE_PHYICAL_ATTACK:
            case EffectConfig.IMMUNE_MAGIC_ATTACK:
            case EffectConfig.IMMUNE_SLOW_ATTACK:
            case EffectConfig.COUNTER_ATTACK:
            case EffectConfig.BOUNCE:
                ret.add("CombatEffect");
                break;
            case EffectConfig.CHANGE_MP_USE:
                ret.add("SkillEnhancer");
                break;
            case EffectConfig.SET_VARIABLE:
                ret.add("CombatEffect");
                break;
            case EffectConfig.HOT:
            case EffectConfig.DOT:
                ret.add("Updatable");
                break;
            case EffectConfig.MP_SHIELD:
            case EffectConfig.VAMPIRE_ON_HIT:
                ret.add("CombatEffect");
                break;
            case EffectConfig.CHANGE_CD_TIME:
            case EffectConfig.CHANGE_DISTANCE:
            case EffectConfig.CHANGE_ACT_TIME:
            case EffectConfig.CHANGE_RANGE:
                ret.add("SkillEnhancer");
                break;
            case EffectConfig.CURE_TARGET_IGNORE_MAX:    
                throw new IllegalArgumentException("不能使用治疗目标效果，如果想提高治疗效果，请使用提高治疗量效果。");
            case EffectConfig.CHANGE_MAGIC_ARMOR:
                ret.add("PropertyEnhancer");
                break;
            case EffectConfig.IGNORE_MAGIC_ARMOR:
            case EffectConfig.REDUCE_PHYSICAL_DAMAGE:
            case EffectConfig.REDUCE_MAGIC_DAMAGE:
                ret.add("CombatEffect");
                break;
            case EffectConfig.CHANGE_MAGIC_HIT:
            case EffectConfig.CHANGE_MAGIC_DODGE:
                ret.add("PropertyEnhancer");
                break;
            case EffectConfig.CANNOT_MOVE:
            case EffectConfig.MPHOT:
            case EffectConfig.MPDOT:
                ret.add("Updatable");
                break;
            case EffectConfig.CHANGE_STA:
            case EffectConfig.CHANGE_AGI:
            case EffectConfig.CHANGE_STR:
            case EffectConfig.CHANGE_INT:
                ret.add("PropertyEnhancer");
                break;
            case EffectConfig.HP_ACTIVE_BUFF:
            case EffectConfig.LIMIT_EFFECT_TIMES:
                ret.add("Updatable");
                break;
            case EffectConfig.CRIT_ACTIVE_BUFF:
            case EffectConfig.CRITED_ACTIVE_BUFF:
            case EffectConfig.DUMB_ON_HIT:
            case EffectConfig.IMMUNE_FEAR:
            case EffectConfig.IMMUNE_DUMB:
            case EffectConfig.IMMUNE_PARALYZE:
            case EffectConfig.IMMUNE_STAY:
                ret.add("CombatEffect");
                break;
            case EffectConfig.CHANGE_BASIC_MAGIC_AP:
            case EffectConfig.CHANGE_BASIC_HP:
            case EffectConfig.CHANGE_BASIC_MP:
            case EffectConfig.CHANGE_MAGIC_HEAL:
                ret.add("PropertyEnhancer");
                break;
            case EffectConfig.LIMIT_SKILL:
            case EffectConfig.CHANGE_BATTLE_PHYICAL_AP:
            case EffectConfig.CHANGE_BATTLE_MAGIC_AP:
            case EffectConfig.CHANGE_BATTLE_WEAPON_ATK:
            case EffectConfig.CHANGE_BATTLE_WEAPON_MATK:
            case EffectConfig.CHANGE_BATTLE_PHYSICAL_HIT:
            case EffectConfig.CHANGE_BATTLE_PHYSICAL_CRIT:
            case EffectConfig.CHANGE_BATTLE_PHYSICAL_DODGE:
            case EffectConfig.CHANGE_BATTLE_MAGIC_CRIT:
            case EffectConfig.CHANGE_BATTLE_MAGIC_HIT:
            case EffectConfig.CHANGE_BATTLE_MAGIC_DODGE:
            case EffectConfig.CHANGE_BATTLE_PHYSICAL_CRITED:
            case EffectConfig.CHANGE_BATTLE_MAGIC_CRITED:
            case EffectConfig.CHANGE_BATTLE_ARMOR:
            case EffectConfig.CHANGE_BATTLE_MAGIC_ARMOR:
                ret.add("CombatEffect");
                break;
            case EffectConfig.CHANGE_EXP_RATE:
            case EffectConfig.CHANGE_HORSE_EXP_RATE:
            case EffectConfig.CHANGE_MONEY_RATE:
                ret.add("PropertyEnhancer");
                break;
            case EffectConfig.CHANGE_PHYSICAL_CRIT_RATE:
            case EffectConfig.CHANGE_PHYSICAL_HIT_RATE:
            case EffectConfig.CHANGE_PHYSICAL_DODGE_RATE:
            case EffectConfig.CHANGE_MAGIC_CRIT_RATE:
            case EffectConfig.CHANGE_MAGIC_HIT_RATE:
            case EffectConfig.CHANGE_MAGIC_DODGE_RATE:
                ret.add("PropertyEnhancer");
                break;
            case EffectConfig.ADD_DEBUFF_ON_HITED:
            case EffectConfig.ADD_BUFF_ON_HITED:
            case EffectConfig.SLOW_ON_HITED:
                ret.add("CombatEffect");
                break;
            case EffectConfig.CHANGE_PARAM:
                ret.add("ParamEnhancer");
                break;
            case EffectConfig.REMOVE_ON_BATTLE_END:
                ret.add("Updatable");
                break;
            case EffectConfig.IMMUNE_BREAKATTACK:
                ret.add("CombatEffect");
                break;
            }
        }
        return ret;
    }

    /**
     * 生成静态数组变量定义。
     */
    public static void generateStaticArray(PrintWriter out, String name, Object arr) {
        if (arr instanceof int[]) {
            int[] iarr = (int[]) arr;
            out.println("    public static int[] " + name + " = {");
            out.print("        0");
            for (int v : iarr) {
                out.print(", " + v);
            }
            out.println();
            out.println("    };");
        }
        else if (arr instanceof float[]) {
            float[] farr = (float[]) arr;
            out.println("    public static float[] " + name + " = {");
            out.print("        0.0f");
            for (float v : farr) {
                out.print(", " + v + "f");
            }
            out.println();
            out.println("    };");
        }
        else if (arr instanceof String[]) {
            String[] sarr = (String[]) arr;
            out.println("    public static String[] " + name + " = {");
            out.print("        \"\"");
            for (String v : sarr) {
                out.print(", \"" + Utils.reverseConv(v) + "\"");
            }
            out.println();
            out.println("    };");
        }
        else if (arr instanceof ParamIndicator[]) {
            ParamIndicator[] sarr = (ParamIndicator[]) arr;
            out.println("    public static String[] " + name + " = {");
            out.print("        \"\"");
            for (ParamIndicator v : sarr) {
                out.print(", \"" + Utils.reverseConv(v.toString()) + "\"");
            }
            out.println();
            out.println("    };");
        }
        else if (arr instanceof int[][]) {
            int[][] iarr = (int[][])arr;
            out.println("    public static int[][] " + name + " = {");
            out.print("        {}");
            for (int[] v : iarr) {
                out.print(", { ");
                for (int i = 0; i < v.length; i++) {
                    if (i > 0) {
                        out.print(", ");
                    }
                    out.print(v[i]);
                }
            }
            out.println(" }");
            out.println("    };");
        }
    }

    /**
     * 查找一个BUFF的名字。
     * @param project
     * @param buffID
     * @return
     */
    public static String toString(ProjectData project, int buffID) {
        BuffConfig q = (BuffConfig)project.findObject(BuffConfig.class, buffID);
        if (q == null) {
            return "无效效果";
        } else {
            return q.toString();
        }
    }
}
