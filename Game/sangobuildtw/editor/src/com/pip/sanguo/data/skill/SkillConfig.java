package com.pip.sanguo.data.skill;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.SubDropGroup;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.skill.DescriptionPattern;
import com.pip.util.Utils;

/**
 * 技能配置数据。
 * @author lighthu
 */
public class SkillConfig extends DataObject {
    /**
     * 技能类型：主动攻击
     */
    public static final int TYPE_ATTACK = 0;
    /**
     * 技能类型：主动辅助
     */
    public static final int TYPE_AID = 1;
    /**
     * 技能类型：被动
     */
    public static final int TYPE_PASSIVE = 2;
    /**
     * 技能类型：光环
     */
    public static final int TYPE_BUFF = 3;
    /**
     * 技能类型：复活
     */
    public static final int TYPE_RELIVE = 4;

    public static final int DAMAGE_PHYSICAL = 0;  // 物理伤害
    public static final int DAMAGE_MAGIC = 1;    // 法术伤害
    public static final int DAMAGE_DECMP = 2;   // 抽蓝
    public static final int DAMAGE_DEBUFF = 3;  // 加DEBUFF
    public static final int DAMAGE_HEAL = 4;   // 治疗
    public static final int DAMAGE_ADDMP = 5;   // 回蓝
    public static final int DAMAGE_BUFF = 6;  // 加BUFF
    
    public static final int TARGET_SINGLE = 0;  // 单个目标
    public static final int TARGET_SELF = 1;    // 自己
    public static final int TARGET_AREA = 2;    // 指定目标点周围的所有目标
    public static final int TARGET_AROUND = 3;  // 自己周围的所有目标
    
    public ProjectData owner;

    /**
     * 技能类型
     */
    public int type = TYPE_ATTACK;
    /**
     * 目标类型
     */
    public int targetType = TARGET_SINGLE;
    /**
     * 最大级别
     */
    public int maxLevel = 1;
    /**
     * 需要武器，int[0]表示不要求武器
     */
    public int[] requireWeapon = new int[0];
    /**
     * 所属职业
     */
    public int clazz = 4;
    /**
     * 是否自动学习第一级。
     */
    public boolean autoLearn;
    /**
     * 学习级别
     */
    public int[] requireLevel = new int[1];
    /**
     * 耗蓝
     */
    public float[] mp = new float[1];
    /**
     * 施法时间(毫秒)
     */
    public int[] actTime = new int[1];
    /**
     * CD组
     */
    public int cdGroup;
    /**
     * CD时间(毫秒)
     */
    public int[] cdTime = new int[1];
    /**
     * 技能图标
     */
    public int iconID = -1;
    /**
     * 有效距离(码)
     */
    public float[] distance = new float[1];
    /**
     * 群体法术有效范围(码)
     */
    public float[] range = new float[1];
    /**
     * 伤害类型
     */
    public int damageType;
    /**
     * 准备动画
     */
    public int prepareAnimation = -1;
    /**
     * 施放动画
     */
    public int castAnimation = -1;
    /**
     * 命中动画
     */
    public int hitAnimation = -1;
    /**
     * 是否马上能用
     */
    public boolean rideUse = true;
    /**
     * 是否可放技能栏
     */
    public boolean visible = true;
    
    /**
     * 被动和光环技能特有：对应BUFF ID
     */
    public int passiveBuff;
    
    /**
     * 主动技能特有：战斗效果集合
     */
    public EffectConfigSet effects = new EffectConfigSet();
    /**
     * 实现Class名
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
            if (type == TYPE_PASSIVE || type == TYPE_BUFF) {
                return 1;
            } else if (targetType == TARGET_SINGLE) {
                return 5;
            } else if (targetType == TARGET_SELF) {
                return 4;
            } else if (targetType == TARGET_AREA) {
                return 6;
            } else if (targetType == TARGET_AROUND) {
                return 5;
            } else {
                throw new IllegalArgumentException();
            }
        }

        public String getParamName(int index) {
            if (index == 0) {
                return "学习级别";
            } else if (index == 1) {
                return "消耗MP";
            } else if (index == 2) {
                return "施法时间(毫秒)";
            } else if (index == 3) {
                return "CD(毫秒)";
            } else if (index == 4) {
                if (targetType == TARGET_AROUND) {
                    return "有效半径(码)";
                } else {
                    return "有效距离(码)";
                }
            } else if (index == 5) {
                return "有效半径(码)";
            }
            throw new IllegalArgumentException();
        }

        public Class getParamClass(int index) {
            if (index == 0) {
                return Integer.class;
            } else if (index == 1) {
                return Float.class;
            } else if (index == 2) {
                return Integer.class;
            } else if (index == 3) {
                return Integer.class;
            } else if (index == 4) {
                if (targetType == TARGET_AROUND) {
                    return Float.class;
                } else {
                    return Float.class;
                }
            } else if (index == 5) {
                return Float.class;
            }
            throw new IllegalArgumentException();
        }

        public Object getParam(int index) {
            if (index == 0) {
                return requireLevel;
            } else if (index == 1) {
                return mp;
            } else if (index == 2) {
                return actTime;
            } else if (index == 3) {
                return cdTime;
            } else if (index == 4) {
                if (targetType == TARGET_AROUND) {
                    return range;
                } else {
                    return distance;
                }
            } else if (index == 5) {
                return range;
            }
            throw new IllegalArgumentException();
        }
    }
    
    public SkillConfig(ProjectData owner) {
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
     * 设置技能类型。
     * @param type
     */
    public void setType(int type) {
        this.type = type;
        targetType = TARGET_SINGLE;
        if (type == TYPE_ATTACK) {
            damageType = DAMAGE_PHYSICAL;
            passiveBuff = -1;
        } else if (type == TYPE_AID) {
            damageType = DAMAGE_HEAL;
            passiveBuff = -1;
        } else if (type == TYPE_PASSIVE || type == TYPE_BUFF) {
            damageType = DAMAGE_HEAL;
            rideUse = false;
            visible = false;
            effects.clear();
        } else if (type == TYPE_RELIVE) {
            damageType = DAMAGE_HEAL;
            passiveBuff = -1;
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
        requireLevel = Utils.realloc(requireLevel, maxLevel);
        mp = Utils.realloc(mp, maxLevel);
        actTime = Utils.realloc(actTime, maxLevel);
        cdTime = Utils.realloc(cdTime, maxLevel);
        range = Utils.realloc(range, maxLevel);
        distance = Utils.realloc(distance, maxLevel);
        effects.setLevelCount(maxLevel);
    }

    /**
     * 保存数据对象。
     * @param obj 编辑器当前输入内容
     */
    public void update(DataObject obj) {
        SkillConfig oo = (SkillConfig) obj;
        id = oo.id;
        title = oo.title;
        description = oo.description;
        categoryName = oo.categoryName;
        
        type = oo.type;
        targetType = oo.targetType;
        maxLevel = oo.maxLevel;
        requireWeapon = Utils.realloc(oo.requireWeapon, oo.requireWeapon.length);
        clazz = oo.clazz;
        autoLearn = oo.autoLearn;
        requireLevel = Utils.realloc(oo.requireLevel, oo.requireLevel.length);
        mp = Utils.realloc(oo.mp, oo.mp.length);
        actTime = Utils.realloc(oo.actTime, oo.actTime.length);
        cdGroup = oo.cdGroup;
        cdTime = Utils.realloc(oo.cdTime, oo.cdTime.length);
        iconID = oo.iconID;
        distance = Utils.realloc(oo.distance, oo.distance.length);
        range = Utils.realloc(oo.range, oo.range.length);
        damageType = oo.damageType;
        prepareAnimation = oo.prepareAnimation;
        castAnimation = oo.castAnimation;
        hitAnimation = oo.hitAnimation;
        rideUse = oo.rideUse;
        visible = oo.visible;
        if (type == TYPE_PASSIVE || type == TYPE_BUFF) {
            visible = false;
        }
        passiveBuff = oo.passiveBuff;
        effects = oo.effects.duplicate();
        effects.removeEffect(-1);
    }

    /**
     * 复制对象以用于编辑。
     */
    public DataObject duplicate() {
        SkillConfig ret = new SkillConfig(owner);
        ret.update(this);
        return ret;
    }

    @Override
    public boolean changed(DataObject obj) {
        SkillConfig oo = (SkillConfig)obj;
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
        
        type = Integer.parseInt(elem.getAttributeValue("type"));
        targetType = Integer.parseInt(elem.getAttributeValue("targettype"));
        maxLevel = Integer.parseInt(elem.getAttributeValue("maxlevel"));
        requireWeapon = Utils.stringToIntArray(elem.getAttributeValue("require-weapon"), ';');
        clazz = Integer.parseInt(elem.getAttributeValue("clazz"));
        autoLearn = "1".equals(elem.getAttributeValue("autolearn"));
        requireLevel = Utils.stringToIntArray(elem.getAttributeValue("requirelevel"), ';');
        mp = Utils.stringToFloatArray(elem.getAttributeValue("mp"), ';');
        actTime = Utils.stringToIntArray(elem.getAttributeValue("acttime"), ';');
        cdGroup = Integer.parseInt(elem.getAttributeValue("cdgroup"));
        cdTime = Utils.stringToIntArray(elem.getAttributeValue("cdtime"), ';');
        iconID = Integer.parseInt(elem.getAttributeValue("iconid"));
        int[] arr = Utils.stringToIntArray(elem.getAttributeValue("distance"), ';');
        distance = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            distance[i] = arr[i] / 8.0f;
        }
        arr = Utils.stringToIntArray(elem.getAttributeValue("range"), ';');
        range = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            range[i] = arr[i] / 8.0f;
        }
        damageType = Integer.parseInt(elem.getAttributeValue("damagetype"));
        prepareAnimation = Integer.parseInt(elem.getAttributeValue("prepareani"));
        castAnimation = Integer.parseInt(elem.getAttributeValue("castani"));
        hitAnimation = Integer.parseInt(elem.getAttributeValue("hitani"));
        rideUse = "true".equals(elem.getAttributeValue("rideuse"));
        visible = "true".equals(elem.getAttributeValue("visible"));
        passiveBuff = Integer.parseInt(elem.getAttributeValue("buffid"));
        try {
            effects.load(elem.getChild("effects"), maxLevel);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }
        effects.setLevelCount(maxLevel);
        
        implClass = elem.getAttributeValue("class");
        
        if (type == TYPE_PASSIVE || type == TYPE_BUFF) {
            visible = false;
        }
    }

    /**
     * 保存成一个XML标签。
     */
    public Element save() {
        Element ret = new Element("skill");
        ret.addAttribute("id", String.valueOf(id));
        ret.addAttribute("title", title);
        ret.addAttribute("description", description);
        if (categoryName != null) {
            ret.addAttribute("category", categoryName);
        }
        
        ret.addAttribute("type", String.valueOf(type));
        ret.addAttribute("targettype", String.valueOf(targetType));
        ret.addAttribute("maxlevel", String.valueOf(maxLevel));
        ret.addAttribute("require-weapon", Utils.intArrayToString(requireWeapon, ';'));
        ret.addAttribute("clazz", String.valueOf(clazz));
        ret.addAttribute("autolearn", autoLearn ? "1" : "0");
        ret.addAttribute("requirelevel", Utils.intArrayToString(requireLevel, ';'));
        ret.addAttribute("mp", Utils.floatArrayToString(mp, ';'));
        ret.addAttribute("acttime", Utils.intArrayToString(actTime, ';'));
        ret.addAttribute("cdgroup", String.valueOf(cdGroup));
        ret.addAttribute("cdtime", Utils.intArrayToString(cdTime, ';'));
        ret.addAttribute("iconid", String.valueOf(iconID));

        int[] arr = new int[distance.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int)(distance[i] * 8.0f);
        }
        ret.addAttribute("distance", Utils.intArrayToString(arr, ';'));

        arr = new int[range.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int)(range[i] * 8.0f);
        }
        ret.addAttribute("range", Utils.intArrayToString(arr, ';'));

        ret.addAttribute("damagetype", String.valueOf(damageType));
        ret.addAttribute("prepareani", String.valueOf(prepareAnimation));
        ret.addAttribute("castani", String.valueOf(castAnimation));
        ret.addAttribute("hitani", String.valueOf(hitAnimation));
        ret.addAttribute("rideuse", rideUse ? "true" : "false");
        ret.addAttribute("visible", visible ? "true" : "false");
        ret.addAttribute("buffid", String.valueOf(passiveBuff));
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
        if (obj instanceof BuffConfig && obj.id == this.passiveBuff) {
            return true;
        }
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
     * 根据一个技能配置生成实现类。
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
        out.println();

        // class name & interfaces
        String className = getClassName(classPrefix);
        out.print("public class " + className + " extends AbstractSkill");
        if (type != TYPE_BUFF && type != TYPE_PASSIVE) {
            out.print(" implements CombatEffect");
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
        BuffConfig.generateStaticArray(out, "REQUIRE_LEVEL", requireLevel);
        BuffConfig.generateStaticArray(out, "MP_USE", mp);
        BuffConfig.generateStaticArray(out, "ACT_TIME", actTime);
        BuffConfig.generateStaticArray(out, "CD_TIME", cdTime);
        int[] arr = new int[distance.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int)(distance[i] * 8.0f);
        }
        BuffConfig.generateStaticArray(out, "DISTANCE", arr);
        arr = new int[range.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int)(range[i] * 8.0f);
        }
        BuffConfig.generateStaticArray(out, "RANGE", arr);
        for (EffectParamRef pr : effects.getAllParams()) {
            String name = BuffConfig.getFieldName(pr.effect, pr.index, true);
            BuffConfig.generateStaticArray(out, name, pr.effect.getParam(pr.index));
        }

        // local data definition
        for (EffectParamRef pr : effects.getAllParams()) {
            String name = BuffConfig.getFieldName(pr.effect, pr.index, false);
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
            else if (param instanceof int[][]) {
                out.println("    int[] " + name + ";");
            }
        }
        out.println();

        // constructor
        out.println("    public " + className + "(int lvl) {");
        out.println("        super(" + id + ", \"" + Utils.reverseConv(title) + "\", lvl);");
        out.println("        if (lvl > " + maxLevel + ") {");
        out.println("            throw new IllegalArgumentException();");
        out.println("        }");
        for (EffectParamRef pr : effects.getAllParams()) {
            String name = BuffConfig.getFieldName(pr.effect, pr.index, false);
            String staticName = BuffConfig.getFieldName(pr.effect, pr.index, true);
            out.println("        " + name + " = " + staticName + "[lvl];");
        }
        out.println("        distance = DISTANCE[lvl];");
        out.println("        actTime = ACT_TIME[lvl];");
        out.println("        CDGroup = " + cdGroup + ";");
        out.println("        CDTime = CD_TIME[lvl];");
        out.println("        range = RANGE[lvl];");
        out.println("        iconId = " + iconID + ";");
        out.println("        prepareAnimation = " + prepareAnimation + ";");
        out.println("        castAnimation = " + castAnimation + ";");
        out.println("        hitAnimation = " + hitAnimation + ";");
        switch (clazz) {
        case 0:
            out.println("        clazz = Unit.CLASS_1;");
            break;
        case 1:
            out.println("        clazz = Unit.CLASS_2;");
            break;
        case 2:
            out.println("        clazz = Unit.CLASS_3;");
            break;
        case 3:
            out.println("        clazz = Unit.CLASS_4;");
            break;
        case 4:
            out.println("        clazz = -1;");
            break;
        default:
            throw new IllegalArgumentException("非法职业");
        }
        out.println("        mp = MP_USE[lvl];");
        switch (type) {
        case TYPE_ATTACK:
            out.println("        type = TYPE_ATTACK;");
            break;
        case TYPE_AID:
            out.println("        type = TYPE_AID;");
            break;
        case TYPE_PASSIVE:
            out.println("        type = TYPE_PASSIVE;");
            break;
        case TYPE_BUFF:
            out.println("        type = TYPE_BUFF;");
            break;
        case TYPE_RELIVE:
            out.println("        type = TYPE_RELIVE;");
            break;
        default:
            throw new IllegalArgumentException("非法的技能类型");
        }
        if (visible) {
            out.println("        type |= TYPE_VISIBLE;");
        }
        if (rideUse) {
            out.println("        type |= TYPE_RIDE_USE;");
        }
        if (type == TYPE_PASSIVE || type == TYPE_BUFF) {
            out.println("        targetType = TARGET_AID_SELF;");
        } else {
            switch (targetType) {
            case TARGET_SINGLE:
                if (type == TYPE_ATTACK) {
                    out.println("        targetType = TARGET_SINGLE_ATTACK;");
                } else {
                    out.println("        targetType = TARGET_SINGLE_AID;");
                }
                break;
            case TARGET_SELF:
                if (type == TYPE_ATTACK) {
                    throw new IllegalArgumentException("攻击技能的目标类型不能是自己");
                } else {
                    out.println("        targetType = TARGET_AID_SELF;");
                }
                break;
            case TARGET_AREA:
                if (type == TYPE_ATTACK) {
                    out.println("        targetType = TARGET_AOE_ATTACK_TARGET;");
                } else {
                    out.println("        targetType = TARGET_AOE_AID_TARGET;");
                }
                break;
            case TARGET_AROUND:
                if (type == TYPE_ATTACK) {
                    out.println("        targetType = TARGET_AOE_ATTACK_SELF;");
                } else {
                    out.println("        targetType = TARGET_AOE_AID_SELF;");
                }
                break;
            default:
                throw new IllegalArgumentException("非法的目标类型");
            }
        }
        out.println("    }");
        out.println();
        
        // implementation of skill
        
        // getDesc(Unit owner)
        out.println("    public String getDesc(Unit owner) {");
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
                        paramStrings.add("CommonUtil.formatValue(" + varRef + ")");
                    } else if (type == 1) {
                        paramStrings.add("CommonUtil.formatPercent(" + varRef + ")");
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
        
        // getRequireWeapon()
        if (requireWeapon.length != 0) {
            out.println("    public int[] getRequireWeapon() {");
            out.println("        return WEAPON;");
            out.println("    }");
            out.println();
        }
        
        // isAutoLearn()
        if (autoLearn) {
            out.println("    public boolean isAutoLearn() {");
            out.println("        return true;");
            out.println("    }");
            out.println();
        }
        
        // getRequireLevel()
        out.println("    public int getRequireLevel() {");
        out.println("        return REQUIRE_LEVEL[level];");
        out.println("    }");
        out.println();

        // createActEffect()
        out.println("    protected CombatEffect createActEffect() {");
        if (type != TYPE_BUFF && type != TYPE_PASSIVE) {
            out.println("        if (level == 0) {");
            out.println("            return null;");
            out.println("        }");
            out.println("        return this;");
        } else {
            out.println("        return null;");
        }
        out.println("    }");
        out.println();
        
        // newBuff()
        out.println("    public Buff newBuff() {");
        if (type == TYPE_PASSIVE) {
            out.println("        if (level == 0) {");
            out.println("            return null;");
            out.println("        }");
            out.println("        return BuffUtil.createSkillBuff(" + passiveBuff + ", this);");
        } else {
            out.println("        return null;");
        }
        out.println("    }");
        out.println();
        
        // getAreaBuff()
        out.println("    public Buff getAreaBuff() {");
        if (type == TYPE_BUFF) {
            out.println("        if (level == 0) {");
            out.println("            return null;");
            out.println("        }");
            out.println("        return BuffUtil.createSkillBuff(" + passiveBuff + ", this);");
        } else {
            out.println("        return null;");
        }
        out.println("    }");
        out.println();
        
        // 实现CombatEffect接口
        if (type != TYPE_PASSIVE && type != TYPE_BUFF) {
            // public void preHit(CombatContext context, boolean isActive)
            out.println("    public void preHit(CombatContext context, boolean isActive) {");
            switch (damageType) {
            case DAMAGE_PHYSICAL:
                out.println("        context.damageType = CombatContext.DAMAGE_PHYSICAL;");
                break;
            case DAMAGE_MAGIC:
                out.println("        context.damageType = CombatContext.DAMAGE_MAGIC;");
                break;
            case DAMAGE_DECMP:
                out.println("        context.damageType = CombatContext.DAMAGE_DECMP;");
                break;
            case DAMAGE_DEBUFF:
                out.println("        context.damageType = CombatContext.DAMAGE_DEBUFF;");
                break;
            case DAMAGE_HEAL:
                out.println("        context.damageType = CombatContext.DAMAGE_HEAL;");
                break;
            case DAMAGE_ADDMP:
                out.println("        context.damageType = CombatContext.DAMAGE_ADDMP;");
                break;
            case DAMAGE_BUFF:
                out.println("        context.damageType = CombatContext.DAMAGE_BUFF;");
                break;
            }
            out.println("        if (!checkWeapon(context.source)) {");
            out.println("            return;");
            out.println("        }");
            for (EffectConfig eff : effects.getAllEffects()) {
                String p1 = BuffConfig.getFieldName(eff, 0, false);
                switch (eff.getType()) {
                case EffectConfig.CHANGE_PHYSICAL_HIT:
                    // Effect_PercentAdd: 百分比float
                    if (damageType == DAMAGE_PHYSICAL) {
                        out.println("        context.hitRate += context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f;");
                    } else {
                        throw new IllegalArgumentException("只有物理伤害允许使用改变物理命中率效果。");
                    }
                    break;
                case EffectConfig.CHANGE_PHYSICAL_CRIT:
                    // Effect_PercentAdd: 百分比float
                    if (damageType == DAMAGE_PHYSICAL) {
                        out.println("        context.critRate += context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f;");
                    } else {
                        throw new IllegalArgumentException("只有物理伤害允许使用改变物理暴击率效果。");
                    }
                    break;
                case EffectConfig.CHANGE_PHYSICAL_DODGE:
                    // Effect_PercentAdd: 百分比float
                    if (damageType == DAMAGE_PHYSICAL) {
                        out.println("        context.dodge += context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f;");
                    } else {
                        throw new IllegalArgumentException("只有物理伤害允许使用改变物理闪避率效果。");
                    }
                    break;
                case EffectConfig.CHANGE_MAGIC_CRIT:
                    // Effect_PercentAdd: 百分比float
                    if (damageType != DAMAGE_PHYSICAL) {
                        out.println("        context.critRate += context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f;");
                    } else {
                        throw new IllegalArgumentException("物理伤害不允许使用改变法术暴击率效果。");
                    }
                    break;
                case EffectConfig.CHANGE_MAGIC_HIT:
                    // Effect_PercentAdd: 百分比float
                    if (damageType != DAMAGE_PHYSICAL) {
                        out.println("        context.hitRate += context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f;");
                    } else {
                        throw new IllegalArgumentException("物理伤害不允许使用改变法术命中率效果。");
                    }
                    break;
                case EffectConfig.CHANGE_MAGIC_DODGE:
                    // Effect_PercentAdd: 百分比float
                    if (damageType != DAMAGE_PHYSICAL) {
                        out.println("        context.dodge += context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f;");
                    } else {
                        throw new IllegalArgumentException("物理伤害不允许使用改变法术闪避率效果。");
                    }
                    break;
                case EffectConfig.TWO_HIT_ON_HIT:
                    // Effect_Hit3Times: 无参数
                    out.println("        context.activeSkills.add(context.skill);");
                    out.println("        context.activeSkills.add(context.skill);");
                    break;
            }
            }
            out.println("    }");
            out.println();

            // public void postHit(CombatContext context, boolean isActive)
            out.println("    public void postHit(CombatContext context, boolean isActive) {}");
            out.println();

            // public void preDamage(CombatContext context, boolean isActive)
            out.println("    public void preDamage(CombatContext context, boolean isActive) {");
            out.println("        if (!checkWeapon(context.source)) {");
            out.println("            return;");
            out.println("        }");
            for (EffectConfig eff : effects.getAllEffects()) {
                String p1 = BuffConfig.getFieldName(eff, 0, false);
                String p2 = BuffConfig.getFieldName(eff, 1, false);
                switch (eff.getType()) {
                case EffectConfig.CURE_TARGET:
                    // Effect_FixValueAdd: 数额int
                    if (damageType != DAMAGE_HEAL && damageType != DAMAGE_ADDMP) {
                        throw new IllegalArgumentException("只有治疗/回蓝允许使用治疗目标效果。");
                    }
                    out.println("        context.attackPower += context.getSkillParam(this, \"" + p1 + "\", " + p1 + ");");
                    break;
                case EffectConfig.CHANGE_PHYICAL_AP:
                    // Effect_MultiAdd: 数额int，百分比float
                    if (damageType != DAMAGE_PHYSICAL) {
                        throw new IllegalArgumentException("只有物理伤害允许使用改变物理攻击力效果。");
                    }
                    out.println("        context.attackPower += context.getSkillParam(this, \"" + p1 + "\", " + p1 + ");");
                    out.println("        context.attackPowerRate += context.getSkillParam(this, \"" + p2 + "\", " + p2 + ") / 100.0f;");
                    break;
                case EffectConfig.CHANGE_MAGIC_AP:
                    // Effect_MultiAdd: 数额int，百分比float
                    if (damageType != DAMAGE_MAGIC && damageType != DAMAGE_DECMP) {
                        throw new IllegalArgumentException("只有法术伤害/抽蓝允许使用改变法术攻击力效果。");
                    }
                    out.println("        context.attackPower += context.getSkillParam(this, \"" + p1 + "\", " + p1 + ");");
                    out.println("        context.attackPowerRate += context.getSkillParam(this, \"" + p2 + "\", " + p2 + ") / 100.0f;");
                    break;
                case EffectConfig.CHANGE_CURE_EFFECT:
                    // Effect_MultiAdd: 数额int，百分比float
                    if (damageType != DAMAGE_HEAL && damageType != DAMAGE_BUFF) {
                        throw new IllegalArgumentException("只有治疗/BUFF允许使用改变治疗效果的效果。");
                    }
                    out.println("        context.attackPower += context.getSkillParam(this, \"" + p1 + "\", " + p1 + ");");
                    out.println("        context.attackPowerRate += context.getSkillParam(this, \"" + p2 + "\", " + p2 + ") / 100.0f;");
                    break;
                case EffectConfig.CHANGE_WEAPON_ATK:
                    // Effect_MultiAdd: 数额int，百分比float
                    if (damageType != DAMAGE_PHYSICAL) {
                        throw new IllegalArgumentException("只有物理伤害允许使用改变武器物理攻击力效果。");
                    }
                    out.println("        if (context.source.equipments != null && context.source.equipments.getWeapon() != null) {");
                    out.println("            GameItem weapon = context.source.equipments.getWeapon();");
                    out.println("            int addDamage = CommonUtil.getCount(RND, weapon.getMinAttack(), weapon.getMaxAttack());");
                    out.println("            context.attackPower += addDamage * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_WEAPON_MATK:
                    // Effect_MultiAdd: 数额int，百分比float
                    if (damageType != DAMAGE_MAGIC) {
                        throw new IllegalArgumentException("只有法术伤害允许使用改变武器法术攻击力效果。");
                    }
                    out.println("        if (context.source.equipments != null && context.source.equipments.getWeapon() != null) {");
                    out.println("            GameItem weapon = context.source.equipments.getWeapon();");
                    out.println("            context.attackPower += weapon.getMagicPower() * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f;");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_THREAT:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        context.threatAdd += context.getSkillParam(this, \"" + p1 + "\", " + p1 + ");");
                    out.println("        context.threatAddRate += context.getSkillParam(this, \"" + p2 + "\", " + p2 + ") / 100.0f;");
                    break;
                case EffectConfig.IGNORE_ARMOR:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (context.damageType == CombatContext.DAMAGE_PHYSICAL) {");
                    out.println("            context.armor -= context.getSkillParam(this, \"" + p1 + "\", " + p1 + ");");
                    out.println("            context.armorRate -= context.getSkillParam(this, \"" + p2 + "\", " + p2 + ") / 100.0f;");
                    out.println("        }");
                    break;
                case EffectConfig.IGNORE_MAGIC_ARMOR:
                    // Effect_MultiAdd: 数额int，百分比float
                    out.println("        if (context.damageType == CombatContext.DAMAGE_MAGIC) {");
                    out.println("            context.armor -= context.getSkillParam(this, \"" + p1 + "\", " + p1 + ");");
                    out.println("            context.armorRate -= context.getSkillParam(this, \"" + p2 + "\", " + p2 + ") / 100.0f;");
                    out.println("        }");
                    break;
                }
            }
            out.println("    }");
            out.println();

            // public void postDamage(CombatContext context, boolean isActive)
            out.println("    public void postDamage(CombatContext context, boolean isActive) {");
            out.println("        if (!checkWeapon(context.source)) {");
            out.println("            return;");
            out.println("        }");
            for (EffectConfig eff : effects.getAllEffects()) {
                String p1 = BuffConfig.getFieldName(eff, 0, false);
                switch (eff.getType()) {
                case EffectConfig.APPEND_MAGIC_DAMAGE:
                    // Effect_FixValueAdd: 数额int
                    out.println("        context.appendSpellDamage(context.getSkillParam(this, \"" + p1 + "\", " + p1 + "));");
                    break;
                case EffectConfig.DOUBLE_DAMAGE_ON_HIT:
                    // Effect_PercentAdd: 百分比float
                    out.println("        int rate = (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + "));");
                    out.println("        if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("            context.damage *= 2;");
                    out.println("            context.threat *= 2;");
                    out.println("        }");
                    break;
                }
            }
            out.println("    }");
            out.println();

            // public void finished(CombatContext context, boolean isActive)
            out.println("    public void finished(CombatContext context, boolean isActive) {");
            out.println("        if (!checkWeapon(context.source)) {");
            out.println("            return;");
            out.println("        }");
            for (EffectConfig eff : effects.getAllEffects()) {
                String p1 = BuffConfig.getFieldName(eff, 0, false);
                String p2 = BuffConfig.getFieldName(eff, 1, false);
                String p3 = BuffConfig.getFieldName(eff, 2, false);
                String p4 = BuffConfig.getFieldName(eff, 3, false);
                String p5 = BuffConfig.getFieldName(eff, 4, false);
                String p6 = BuffConfig.getFieldName(eff, 5, false);
                switch (eff.getType()) {
                case EffectConfig.ADD_MP_ON_HIT:
                    // Effect_CureOnHit: 概率float，固定值int，占上限比例float，占伤害比例float
                    out.println("        if (context.hited() && context.isDamage()) {");
                    out.println("            int rate = (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + "));");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                int addmp = context.getSkillParam(this, \"" + p2 + "\", " + p2 + ");");
                    out.println("                addmp += context.source.maxmp * context.getSkillParam(this, \"" + p3 + "\", " + p3 + ") / 100.0f;");
                    out.println("                addmp += context.damage * context.getSkillParam(this, \"" + p4 + "\", " + p4 + ") / 100.0f;");
                    out.println("                context.activeSkills.add(new FixedAddMPSkill(addmp));");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.ADD_DEBUFF_ON_HIT:
                    // Effect_AddBuff: 概率float，概率补充变量String，BUFFID int，BUFF级别int
                    out.println("        if (context.hited()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ")), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(100 * (vo.floatValue() + context.getSkillParam(this, \"" + p1 + "\", " + p1 + "))), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.target.buffs.addBuff(BuffUtil.createBuff(" + p3 + ", "
                            + p4 + ", context.source, context.target, context.damage));");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.ADD_BUFF_ON_HIT:
                    // Effect_AddBuff: 概率float，概率补充变量String，BUFFID int，BUFF级别int
                    out.println("        if (context.hited()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ")), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(100 * (vo.floatValue() + context.getSkillParam(this, \"" + p1 + "\", " + p1 + "))), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.source.buffs.addBuff(BuffUtil.createBuff(" + p3 + ", "
                            + p4 + ", context.source, context.source, context.damage));");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.FIRST_THREAT_ON_HIT:
                    // Effect_FirstThreat: 无参数
                    out.println("        if (context.hited()) {");
                    out.println("            Attack.makeFirstThreat(context.source, context.target, -1);");
                    out.println("        }");
                    break;
                case EffectConfig.FEAR_ON_HIT:
                    // Effect_FearOnHit: 概率float，概率补充变量String，持续时间int
                    out.println("        if (context.hited()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ")), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(100 * (vo.floatValue() + context.getSkillParam(this, \"" + p1 + "\", " + p1 + "))), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.activeSkills.add(new FearSkill(context.getSkillParam(this, \"" + p3 + "\", " + p3 + ")));");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.SLOW_ON_HIT:
                    // Effect_SlowOnHit:
                    // 概率float，概率补充变量String，减速级别int，减速级别补充变量String，持续时间int，持续时间变量float
                    out.println("        if (context.hited()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ")), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(100 * (vo.floatValue() + context.getSkillParam(this, \"" + p1 + "\", " + p1 + "))), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                int sl = context.getSkillParam(this, \"" + p3 + "\", " + p3 + ");");
                    out.println("                Float vo = context.skillParams.get(" + p4 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    sl += vo.intValue();");
                    out.println("                }");
                    out.println("                int tm = context.getSkillParam(this, \"" + p5 + "\", " + p5 + ");");
                    out.println("                vo = context.skillParams.get(" + p6 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    tm *= 1.0f + vo.floatValue() / 100.0f;");
                    out.println("                }");
                    out.println("                context.activeSkills.add(new SlowSkill(sl, tm));");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.PARALYZE_ON_HIT:
                    // Effect_FearOnHit: 概率float，概率补充变量String，持续时间int
                    out.println("        if (context.hited()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ")), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(100 * (vo.floatValue() + context.getSkillParam(this, \"" + p1 + "\", " + p1 + "))), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.activeSkills.add(new ParalyzeSkill(context.getSkillParam(this, \"" + p3 + "\", " + p3 + ")));");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.STAY_ON_HIT:
                    // Effect_FearOnHit: 概率float，概率补充变量String，持续时间int
                    out.println("        if (context.hited()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ")), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(100 * (vo.floatValue() + context.getSkillParam(this, \"" + p1 + "\", " + p1 + "))), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.activeSkills.add(new StaySkill(context.getSkillParam(this, \"" + p3 + "\", " + p3 + ")));");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.DUMB_ON_HIT:
                    // Effect_FearOnHit: 概率float，概率补充变量String，持续时间int
                    out.println("        if (context.hited()) {");
                    out.println("            boolean hit = false;");
                    out.println("            if (" + p2 + ".length() == 0) {");
                    out.println("                hit = CommonUtil.hit(RND, (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ")), 10000);");
                    out.println("            } else {");
                    out.println("                Float vo = context.skillParams.get(" + p2 + ");");
                    out.println("                if (vo != null) {");
                    out.println("                    hit = CommonUtil.hit(RND, (int)(100 * (vo.floatValue() + context.getSkillParam(this, \"" + p1 + "\", " + p1 + "))), 10000);");
                    out.println("                }");
                    out.println("            }");
                    out.println("            if (hit) {");
                    out.println("                context.activeSkills.add(new DumbSkill(context.getSkillParam(this, \"" + p3 + "\", " + p3 + ")));");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.REPEAT_ON_HIT:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (context.hited()) {");
                    out.println("            if (CommonUtil.hit(RND, (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ")), 10000)) {");
                    out.println("                context.activeSkills.add(context.skill);");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.DEC_MP_ON_HIT:
                    // Effect_CureOnHit: 概率float，固定值int，占上限比例float，占伤害比例float
                    out.println("        if (context.hited() && context.isDamage()) {");
                    out.println("            int rate = (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + "));");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                int decmp = context.getSkillParam(this, \"" + p2 + "\", " + p2 + ");");
                    out.println("                decmp += context.target.maxmp * context.getSkillParam(this, \"" + p3 + "\", " + p3 + ") / 100.0f;");
                    out.println("                decmp += context.damage * context.getSkillParam(this, \"" + p4 + "\", " + p4 + ") / 100.0f;");
                    out.println("                context.activeSkills.add(new FixedDecMPSkill(decmp));");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.ADD_HP_ON_HIT:
                    // Effect_CureOnHit: 概率float，固定值int，占上限比例float，占伤害比例float
                    out.println("        if (context.hited() && context.isDamage()) {");
                    out.println("            int rate = (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + "));");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                int cure = context.getSkillParam(this, \"" + p2 + "\", " + p2 + ");");
                    out.println("                cure += context.source.maxhp * context.getSkillParam(this, \"" + p3 + "\", " + p3 + ") / 100.0f;");
                    out.println("                cure += context.damage * context.getSkillParam(this, \"" + p4 + "\", " + p4 + ") / 100.0f;");
                    out.println("                context.activeSkills.add(new FixedHealSkill(cure));");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.RELIVE_TARGET:
                    // Effect_PercentAdd: 百分比float
                    out.println("        if (context.source == context.target) {");
                    out.println("            if (isActive && !context.source.isAlive()) {");
                    out.println("                int hp = (int)(context.source.maxhp * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f);");
                    out.println("                int mp = (int)(context.source.maxmp * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f);");
                    out.println("                context.source.relive(hp, mp);");
                    out.println("            }");
                    out.println("        } else {");
                    out.println("            if (isActive) {");
                    out.println("                if (!context.target.isAlive()) {");
                    out.println("                    // TODO: 向死亡的人发送一个复活请求");
                    out.println("                    if (context.target.type == GameObject.TYPE_PLAYER&&context.target.faction==context.source.faction) {");
                    out.println("                        ReliveOption option = new ReliveOption(");
                    out.println("                                ReliveOption.SKILL_ACTIVE, getName(), 14,");
                    out.println("                               context.target.map.id, context.source.x,");
                    out.println("                               context.source.y);");
                    out.println("                       option.context = context;");
                    out.println("                      ((Player)context.target).addReliveOption(option);");
                    out.println("                   }");
                    out.println("                 }");
                    out.println("                } else {");
                    out.println("                    if (!context.target.isAlive()) {");
                    out.println("                        int hp = (int)(context.target.maxhp * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f);");
                    out.println("                        int mp = (int)(context.target.maxmp * context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f);");
                    out.println("                        context.target.relive(hp, mp);");
                    out.println("                    }");
                    out.println("                }");
                    out.println("        }");
                    break;
                case EffectConfig.CURE_TARGET_IGNORE_MAX:
                    // Effect_CureOnHit: 概率float，固定值int，占上限比例float，占伤害比例float
                    out.println("        if (context.hited()) {");
                    out.println("            int rate = (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + "));");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                int addhp = context.getSkillParam(this, \"" + p2 + "\", " + p2 + ");");
                    out.println("                addhp += context.source.maxhp * context.getSkillParam(this, \"" + p3 + "\", " + p3 + ") / 100.0f;");
                    out.println("                addhp += context.damage * context.getSkillParam(this, \"" + p4 + "\", " + p4 + ") / 100.0f;");
                    out.println("                context.target.setHp(context.target.hp + addhp, true);");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.DISPEL_BUFF:
                    // Effect_PercentAdd: 百分比
                    out.println("        if (context.hited()) {");
                    out.println("            int rate = (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + "));");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.target.buffs.dispelGoodBuff(false);");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.DISPEL_ALL_BUFF:
                    // Effect_PercentAdd: 百分比
                    out.println("        if (context.hited()) {");
                    out.println("            int rate = (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + "));");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.target.buffs.dispelGoodBuff(true);");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.DISPEL_DEBUFF:
                    // Effect_PercentAdd: 百分比
                    out.println("        if (context.hited()) {");
                    out.println("            int rate = (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + "));");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.target.buffs.dispelBadBuff(false);");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.DISPEL_ALL_DEBUFF:
                    // Effect_PercentAdd: 百分比
                    out.println("        if (context.hited()) {");
                    out.println("            int rate = (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + "));");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.target.buffs.dispelBadBuff(true);");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.INTERRUPT:
                    // Effect_PercentAdd: 百分比
                    out.println("        if (context.hited()) {");
                    out.println("            int rate = (int)(100 * context.getSkillParam(this, \"" + p1 + "\", " + p1 + "));");
                    out.println("            if (CommonUtil.hit(RND, rate, 10000)) {");
                    out.println("                context.activeSkills.add(new BreakAttackSkill());");
                    out.println("            }");
                    out.println("        }");
                    break;
                case EffectConfig.CHANGE_THREAT_TOTAL:
                    // Effect_PercentAdd: 百分比
                    out.println("        if (context.hited()) {");
                    out.println("            float rate = 1.0f + context.getSkillParam(this, \"" + p1 + "\", " + p1 + ") / 100.0f;");
                    out.println("            context.source.changeThreat(target, rate);");
                    out.println("        }");
                    break;
                case EffectConfig.FIRST_THREAT_TEMP:
                    // Effect_FirstThreatTemp: 持续时间(毫秒)
                    out.println("        if (context.hited()) {");
                    out.println("            int keepTime = context.getSkillParam(this, \"" + p1 + "\", " + p1 + ");");
                    out.println("            Attack.makeFirstThreat(context.target, context.source, keepTime);");
                    out.println("        }");
                    break;
                case EffectConfig.TRANSPORT_TO_ME:
                    // Effect_CannotMove：无参数
                    out.println("        if (context.hited()) {");
                    out.println("            try{");
                    out.println("            context.target.goMap(context.source.map.id, context.source.x, context.source.y);");
                    out.println("            }catch(VMapException e){");
                    out.println("            }");
                    out.println("        }");
//                    out.println("        if (context.hited()) {");
//                    out.println("            context.target.goMap(context.source.map.id, context.source.x, context.source.y);");
//                    out.println("        }");
                    break;
                case EffectConfig.TRANSPORT_TO_POS:
                    // Effect_Transport：目标位置
                    out.println("        if (context.hited()) {");
                    out.println("            context.target.goMap(" + p1 + "[0], " + p1 + "[1], " + p2 + "[2]);");
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

    /**
     * 查找一个技能的名字。
     * @param project
     * @param questID
     * @return
     */
    public static String toString(ProjectData project, int skillID) {
        SkillConfig q = (SkillConfig)project.findObject(SkillConfig.class, skillID);
        if (q == null) {
            return "无效技能";
        } else {
            return q.toString();
        }
    }
    
    /**
     * 查找一组技能的名字
     * @param project
     * @param skillIDs 技能ID,用逗号分隔
     * @return
     */
    public static String toString(ProjectData project, String skillIDs) {
        String[] secs = skillIDs.split(",");
        StringBuilder names = new StringBuilder();
        for (String sec : secs) {
            try {
                int sid = Integer.parseInt(sec);
                SkillConfig sc = (SkillConfig)project.findObject(SkillConfig.class, sid);
                if (names.length() > 0) {
                    names.append(",");
                }
                if (sc == null) {
                    names.append("无效技能");
                } else {
                    names.append(sc.title);
                }
            } catch (Exception e) {
                continue;
            }
        }
        return names.toString();
    }
}
