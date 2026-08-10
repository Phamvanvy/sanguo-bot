package peony.game.buff;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.skill.BuffConfig;

import peony.game.Server;
import peony.game.Unit;
import peony.game.skill.Skill;
import peony.util.FileClassLoader;

public class BuffUtil {
    private static Logger log = Logger.getLogger(BuffUtil.class);
    private static AtomicInteger instanceIDGen = new AtomicInteger(1);
    private static Map<Integer, Class> standardBuffs = new HashMap<Integer, Class>(){{
        put(10001, DumbDebuff.class);
        put(10002, FearDebuff.class);
        put(10003, ParalyzeDebuff.class);
        put(10004, SlowDebuff.class);
        put(10005, StayDebuff.class);
    }};
    private static File jarFile;
    private static FileClassLoader classLoader;
    
    public static int getNextID() {
        return instanceIDGen.getAndIncrement();
    }
    
    /**
     * 取得载入技能和buff的classloader对象。
     * @return
     */
    public static ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public static void initBuffs() {
        // 创建载入Buff类的ClassLoader。如果当前目录下有skill.jar，则用这个jar文件载入，否则
        // 从classpath里载入。
        jarFile = new File("skill.jar");
        if (jarFile.exists()) {
            try {
                classLoader = new FileClassLoader(jarFile);
            } catch (Exception e) {
                log.error(e, e);
            }
        }
        
        // 载入所有Buff类
        ProjectData data = Server.server.getServiceRegistry().getDataService().data;
        List<DataObject> buffs = data.getDataListByType(BuffConfig.class);
        for (Object obj : buffs) {
            BuffConfig bc = (BuffConfig)obj;
            try {
                log.info("buff " + bc.id + " class " + bc.implClass);
                loadBuffClass(bc.implClass);
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }
    
    private static Class loadBuffClass(String name) throws Exception {
        if (classLoader != null) {
            return classLoader.loadClass(name);
        }
        return Class.forName(name);
    }
    
    private static Class findBuffClass(int id) throws Exception {
        if (standardBuffs.containsKey(id)) {
            return standardBuffs.get(id);
        }
        ProjectData data = Server.server.getServiceRegistry().getDataService().data;
        BuffConfig config = (BuffConfig)data.findObject(BuffConfig.class, id);
        return loadBuffClass(config.implClass);
    }

    /**
     * 创建一个带时间限制的临时BUFF对象
     * @param id BUFF ID
     * @param level BUFF 级别
     * @param src 施法者
     * @param tgt 加BUFF的目标
     * @param dmg 导致BUFF的攻击的伤害值（如果有的话）
     * @return
     */
    public static Buff createBuff(int id, int level, Unit src, Unit tgt, int dmg) {
        try {
            Class cls = findBuffClass(id);
            
            // 普通的自动生成BUFF
            Buff ret = null;
            try {
                Constructor c = cls.getConstructor(int.class, Unit.class, Unit.class, int.class);
                ret = (Buff)c.newInstance(level, src, tgt, dmg);
            } catch (Exception e) {
//            	e.printStackTrace();
            }
            if (ret != null) {
                return ret;
            }
            
            // 只需要一个时间参数的BUFF：DumbDebuff, FearDebuff, ParalyzeDebuff, StayDebuff
            try {
                Constructor c = cls.getConstructor(int.class);
                ret = (Buff)c.newInstance(0);
            } catch (Exception e) {
            }
            
            // 需要级别+时间参数的BUFF：SlowDebuff
            try {
                Constructor c = cls.getConstructor(int.class, int.class);
                ret = (Buff)c.newInstance(1, 0);
            } catch (Exception e) {
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 创建一个被动技能对应的BUFF。
     * @param id BUFF ID
     * @param skill 技能对象
     * @return
     */
    public static Buff createSkillBuff(int id, Skill skill) {
        try {
            Class cls = findBuffClass(id);
            Constructor c = cls.getConstructor(Skill.class, int.class);
            return (Buff)c.newInstance(skill, skill.getLevel());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 创建一个套装效果或称号效果对应的BUFF。
     * @param id BUFF ID
     * @return
     */
    public static Buff createSuiteBuff(int id, int level) {
        try {
            Class cls = findBuffClass(id);
            Constructor c = cls.getConstructor(int.class);
            return (Buff)c.newInstance(level);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 创建一个新装备套装效果或称号效果对应的BUFF。
     * @param id BUFF ID
     * @return
     */
    public static Buff createSuiteBuff(int id, int level, int weight) {
    	try {
    		Class cls = findBuffClass(id);
    		Constructor c = cls.getConstructor(int.class, int.class);
    		return (Buff)c.newInstance(level, weight);
    	} catch (Exception e) {
    		return null;
    	}
    }

    /**
     * 在实际BUFF加上身之前，根据源和目标的ParamEnhancer修正BUFF参数。
     * @param src 施法者
     * @param tgt 被施法者
     * @param id BUFF ID
     * @param name 参数名称
     * @param value 当前值
     * @return 修正后的值
     */
    public static int enhanceParam(Unit src, Unit tgt, int id, String name, int value) {
        if (src != null) {
            value = src.buffs.getParamEnhances().enhance(ParamEnhanceSet.TYPE_BUFF_SOURCE, id, name, value);
        }
        if (tgt != null) {
            value = tgt.buffs.getParamEnhances().enhance(ParamEnhanceSet.TYPE_BUFF_OWNER, id, name, value);
        }
        return value;
    }
    
    /**
     * 在实际BUFF加上身之前，根据源和目标的ParamEnhancer修正BUFF参数。
     * @param src 施法者
     * @param tgt 被施法者
     * @param id BUFF ID
     * @param name 参数名称
     * @param value 当前值
     * @return 修正后的值
     */
    public static float enhanceParam(Unit src, Unit tgt, int id, String name, float value) {
        if (src != null) {
            value = src.buffs.getParamEnhances().enhance(ParamEnhanceSet.TYPE_BUFF_SOURCE, id, name, value);
        }
        if (tgt != null) {
            value = tgt.buffs.getParamEnhances().enhance(ParamEnhanceSet.TYPE_BUFF_OWNER, id, name, value);
        }
        return value;
    }
}
