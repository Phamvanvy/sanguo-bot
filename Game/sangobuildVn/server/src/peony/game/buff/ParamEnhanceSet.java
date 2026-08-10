package peony.game.buff;

import java.util.*;

/**
 * 一个玩家身上所有ParamEnhance效果的集合。每个ParamEnhance可以作用于一个参数，通过参数
 * 名字来索引。参数名字的命名规范是：<SA/SP/BO/BS>_<技能/BUFF ID>_<参数名字>。
 * @author lighthu
 */
public class ParamEnhanceSet {
    public static final int TYPE_SKILL_ACTIVE = 0;
    public static final int TYPE_SKILL_PASSIVE = 1;
    public static final int TYPE_BUFF_OWNER = 2;
    public static final int TYPE_BUFF_SOURCE = 3;
    
    protected Map<String, ParamEnhance> enhances = new HashMap<String, ParamEnhance>();
    
    protected String getKey(int type, int id, String pname) {
        if (type == TYPE_SKILL_ACTIVE) {
            return "SA_" + id + "_" + pname;
        } else if (type == TYPE_SKILL_PASSIVE) {
            return "SB_" + id + "_" + pname;
        } else if (type == TYPE_BUFF_OWNER) {
            return "BO_" + id + "_" + pname;
        } else {
            return "BS_" + id + "_" + pname;
        }
    }
    
    public void add(int type, int id, String pname, float value, float percent) {
        String key = getKey(type, id, pname);
        ParamEnhance enh = enhances.get(key);
        if (enh == null) {
            enh = new ParamEnhance();
            enhances.put(key, enh);
        }
        enh.times++;
        enh.value += value;
        enh.percent += percent;
    }
    
    public void remove(int type, int id, String pname, float value, float percent) {
        String key = getKey(type, id, pname);
        ParamEnhance enh = enhances.get(key);
        if (enh == null) {
            return;
        }
        enh.times--;
        enh.value -= value;
        enh.percent -= percent;
        if (enh.times == 0) {
            enhances.remove(key);
        }
    }
    
    public void clear() {
        enhances.clear();
    }
    
    public float enhance(int type, int id, String pname, float currentValue) {
        String key = getKey(type, id, pname);
        ParamEnhance enh = enhances.get(key);
        if (enh == null) {
            return currentValue;
        }
        return (currentValue * (1.0f + enh.percent)) + enh.value;
    }
    
    public int enhance(int type, int id, String pname, int currentValue) {
        String key = getKey(type, id, pname);
        ParamEnhance enh = enhances.get(key);
        if (enh == null) {
            return currentValue;
        }
        return (int)((currentValue * (1.0f + enh.percent)) + enh.value);
    }
}
