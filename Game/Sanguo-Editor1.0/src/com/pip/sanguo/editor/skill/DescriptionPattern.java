package com.pip.sanguo.editor.skill;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.EffectConfig;
import com.pip.sanguo.data.skill.EffectConfigSet;
import com.pip.sanguo.data.skill.EffectParamRef;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.Utils;

public class DescriptionPattern {
    protected BuffConfig buff;
    protected SkillConfig skill;
    protected EffectConfigSet attrs;
    protected String pattern;
    
    public DescriptionPattern(BuffConfig buff) {
        this.buff = buff;
        pattern = buff.description;
        attrs = new EffectConfigSet();
        attrs.setLevelCount(buff.effects.getLevelCount());
        attrs.addGeneralEffect(buff.getGeneralConfig());
        for (EffectConfig eff : buff.effects.getAllEffects()) {
            attrs.addEffect(eff);
        }
    }
    
    public DescriptionPattern(SkillConfig skill) {
        this.skill = skill;
        pattern = skill.description;
        attrs = new EffectConfigSet();
        attrs.setLevelCount(skill.effects.getLevelCount());
        attrs.addGeneralEffect(skill.getGeneralConfig());
        for (EffectConfig eff : skill.effects.getAllEffects()) {
            attrs.addEffect(eff);
        }
    }
    
    public static String[] splitPattern(String pattern) {
        List<String> ret = new ArrayList<String>();
        int start = 0;
        while (true) {
            int cur = pattern.indexOf("${", start);
            if (cur == -1) {
                ret.add(pattern.substring(start));
                break;
            }
            int next = pattern.indexOf('}', cur);
            if (next == -1) {
                ret.add(pattern.substring(start));
                break;
            }
            ret.add(pattern.substring(start, cur));
            String token = pattern.substring(cur + 2, next);
            start = next + 1;
            if (start < pattern.length()) {
                char ch = pattern.charAt(start);
                if (ch == '%') {
                    token += '%';
                    start++;
                } else if (ch == 't') {
                    token += 't';
                    start++;
                } else if (ch == 'T') {
                    token += 'T';
                    start++;
                }
            }
            ret.add(token);
        }
        String[] ret2 = new String[ret.size()];
        ret.toArray(ret2);
        return ret2;
    }
    
    public String generate(int level) {
        StringBuffer buf = new StringBuffer();
        String[] secs = splitPattern(pattern);
        for (int i = 0; i < secs.length; i++) {
            if ((i & 1) == 0) {
                buf.append(secs[i]);
            } else {
                try {
                    if (secs[i].endsWith("%")) {
                        buf.append(translateVar(secs[i].substring(0, secs[i].length() - 1), 1, level));
                    } else if (secs[i].endsWith("t")) {
                        buf.append(translateVar(secs[i].substring(0, secs[i].length() - 1), 2, level));
                    } else if (secs[i].endsWith("T")) {
                        buf.append(translateVar(secs[i].substring(0, secs[i].length() - 1), 3, level));
                    } else {
                        buf.append(translateVar(secs[i], 0, level));
                    }
                } catch (Exception e) {
                    buf.append("error");
                }
            }
        }
        return buf.toString();
    }
    
    private String translateVar(String token, int type, int level) {
        String[] secs = Utils.splitString(token, '.');
        EffectConfigSet curSet = attrs;
        Object value = null;
        for (int i = 0; i < secs.length; i++) {
            String tt = secs[i];
            if (tt.equals("ab")) {
                BuffConfig nextBuff = (BuffConfig)getProjectData().findObject(BuffConfig.class, skill.passiveBuff);
                curSet = new EffectConfigSet();
                curSet.setLevelCount(nextBuff.maxLevel);
                curSet.addEffect(nextBuff.getGeneralConfig());
                for (EffectConfig eff : nextBuff.effects.getAllEffects()) {
                    curSet.addEffect(eff);
                }
            } else {
                int index = Integer.parseInt(tt.substring(1)) - 1;
                EffectParamRef pr = curSet.getParamAt(index);
                if (pr.getParamClass() == BuffConfig.class) {
                    int bid = ((Integer)pr.getParamValue(level)).intValue();
                    BuffConfig nextBuff = (BuffConfig)getProjectData().findObject(BuffConfig.class, bid);
                    curSet = new EffectConfigSet();
                    curSet.setLevelCount(nextBuff.maxLevel);
                    curSet.addEffect(nextBuff.getGeneralConfig());
                    for (EffectConfig eff : nextBuff.effects.getAllEffects()) {
                        curSet.addEffect(eff);
                    }
                } else {
                    curSet = null;
                    value = pr.getParamValue(level);
                }
            }
        }
        if (type == 0) {
            if (value instanceof Integer) {
                int v = ((Integer)value).intValue();
                return String.valueOf(Math.abs(v));
            } else if (value instanceof Float) {
                float v = ((Float)value).floatValue();
                return formatFloat(Math.abs(v));
            } else {
                return String.valueOf(value);
            }
        } else if (type == 1) {
            // 百分比
            float f;
            if (value instanceof Float) {
                f = Math.abs(((Float)value).floatValue());
            } else {
                f = Math.abs(((Integer)value).floatValue());
            }
            return formatPercent(f);
        } else if (type == 2) {
            // 毫秒
            int sec = Math.abs(((Integer)value).intValue());
            return formatMillSecond(sec);
        } else if (type == 3) {
            // 秒
            int sec = Math.abs(((Integer)value).intValue());
            return formatSecond(sec);
        }
        return null;
    }
    
    public String varToCode(String varName) {
        if (buff != null) {
            return buffVarToCode(varName);
        } else {
            return skillVarToCode(varName);
        }
    }
    
    protected ProjectData getProjectData(){
        if(buff!=null)
            return buff.owner;
        else
            return skill.owner;
    }
    
    private String buffVarToCode(String varName) {
        StringBuilder sb = new StringBuilder();
        String[] secs = Utils.splitString(varName, '.');
        
        // 只有一节的需要特殊处理，${a1}表示剩余时间，${an}如果指向hot的时间字段，则表示剩余秒数，其他的直接指向局部变量
        if (secs.length == 1) {
            int index = Integer.parseInt(secs[0].substring(1)) - 1;
            EffectParamRef pr = attrs.getParamAt(index);
            return BuffConfig.getFieldName(pr.effect, pr.index, false);
        }
        
        // 如果有多节，找到最后一节的定义
        EffectConfigSet curSet = attrs;
        BuffConfig curBuff = buff;
        EffectParamRef lastLastRef = null;
        EffectParamRef lastRef = null;
        for (int i = 0; i < secs.length; i++) {
            String tt = secs[i];
            int index = Integer.parseInt(tt.substring(1)) - 1;
            EffectParamRef pr = curSet.getParamAt(index);
            if (pr.getParamClass() == BuffConfig.class) {
                int bid = ((Integer)pr.getParamValue(1)).intValue();
                lastLastRef = pr;
                curBuff = (BuffConfig)getProjectData().findObject(BuffConfig.class, bid);
                curSet = new EffectConfigSet();
                curSet.setLevelCount(curBuff.maxLevel);
                curSet.addEffect(curBuff.getGeneralConfig());
                for (EffectConfig eff : curBuff.effects.getAllEffects()) {
                    curSet.addEffect(eff);
                }
            } else {
                curSet = null;
                lastRef = pr;
            }
        }
        String levelVar = BuffConfig.getFieldName(lastLastRef.effect, lastLastRef.index + 1, false);
        String fieldName;
        if (lastRef.effect.getType() == -1) {
            fieldName = "DURATION";
        } else {
            fieldName = BuffConfig.getFieldName(lastRef.effect, lastRef.index, true);
        }
        return curBuff.implClass + "." + fieldName + "[" + levelVar + "]";
    }
    
    private String skillVarToCode(String varName) {
        StringBuilder sb = new StringBuilder();
        String[] secs = Utils.splitString(varName, '.');
        
        // 只有一节的需要特殊处理，${a1}表示剩余时间，${an}如果指向hot的时间字段，则表示剩余秒数，其他的直接指向局部变量
        if (secs.length == 1) {
            int index = Integer.parseInt(secs[0].substring(1)) - 1;
            EffectParamRef pr = attrs.getParamAt(index);
            if (pr.effect.getType() == -1) {
                // 基本属性
                String name = pr.getParamName();
                if (name.equals("学习级别")) {
                    return "getRequireLevel()";
                } else if (name.equals("消耗MP")) {
                    return "getMP(owner)";
                } else if (name.equals("施法时间(毫秒)")) {
                    return "getActTime(owner)";
                } else if (name.equals("CD(毫秒)")) {
                    return "getCDTime(owner)";
                } else if (name.equals("有效半径(码)")) {
                    return "getRange(owner) / 8.0f";
                } else if (name.equals("有效距离(码)")) {
                    return "getDistance(owner) / 8.0f";
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                // 效果属性
                return BuffConfig.getFieldName(pr.effect, pr.index, false);
            }
        }
        
        // 如果有多节，找到最后一节的定义
        EffectConfigSet curSet = attrs;
        BuffConfig curBuff = buff;
        EffectParamRef lastLastRef = null;
        EffectParamRef lastRef = null;
        String levelVar = null;
        for (int i = 0; i < secs.length; i++) {
            String tt = secs[i];
            if (tt.equals("ab")) {
                lastLastRef = null;
                curBuff = (BuffConfig)getProjectData().findObject(BuffConfig.class, skill.passiveBuff);
                curSet = new EffectConfigSet();
                curSet.setLevelCount(curBuff.maxLevel);
                curSet.addEffect(curBuff.getGeneralConfig());
                for (EffectConfig eff : curBuff.effects.getAllEffects()) {
                    curSet.addEffect(eff);
                }
                levelVar = "level";
            } else {
                int index = Integer.parseInt(tt.substring(1)) - 1;
                EffectParamRef pr = curSet.getParamAt(index);
                if (pr.getParamClass() == BuffConfig.class) {
                    if (levelVar == null) {
                        EffectParamRef lr = curSet.getParamAt(index + 1);
                        levelVar = BuffConfig.getFieldName(lr.effect, lr.index, false);
                    }
                    int bid = ((Integer)pr.getParamValue(1)).intValue();
                    lastLastRef = pr;
                    curBuff = (BuffConfig)getProjectData().findObject(BuffConfig.class, bid);
                    curSet = new EffectConfigSet();
                    curSet.setLevelCount(curBuff.maxLevel);
                    curSet.addEffect(curBuff.getGeneralConfig());
                    for (EffectConfig eff : curBuff.effects.getAllEffects()) {
                        curSet.addEffect(eff);
                    }
                } else {
                    curSet = null;
                    lastRef = pr;
                }
            }
        }
        if (lastLastRef != null) {
            String fieldName;
            if (lastRef.effect.getType() == -1) {
                fieldName = "DURATION";
            } else {
                fieldName = BuffConfig.getFieldName(lastRef.effect, lastRef.index, true);
            }
            return curBuff.implClass + "." + fieldName + "[" + levelVar + "]";
        } else {
            return curBuff.implClass + "." + BuffConfig.getFieldName(lastRef.effect, lastRef.index, true) + "[level]";
        }
    }

    private static final DecimalFormat percentFormat = new DecimalFormat("####.#"); 
    
    public static String formatPercent(double p) {
        return percentFormat.format(p) + "%";
    }

    public static String formatFloat(double p) {
        return percentFormat.format(p);
    }
    
    public static String formatSecond(int sec) {
        if (sec < 60) {
            return sec + "秒";
        } else if (sec < 3600) {
            return (sec / 60) + "分" + (sec % 60) + "秒";
        } else {
            return (sec / 3600) + "小时" + ((sec % 3600) / 60) + "分";
        }
    }
    
    public static String formatMillSecond(int ms) {
        int sec = ms / 1000;;
        float sec2 = ms / 1000.0f;
        if (sec < 60) {
            return formatFloat(sec2) + "秒";
        } else if (sec < 3600) {
            return (sec / 60) + "分" + (sec % 60) + "秒";
        } else {
            return (sec / 3600) + "小时" + ((sec % 3600) / 60) + "分";
        }
    }
}
