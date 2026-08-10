package peony.game;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.List;
import org.apache.log4j.Logger;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.skill.SkillConfig;
import peony.game.buff.BuffUtil;
import peony.game.nation.CandidateService;
import peony.game.skill.AutoAttackSkill;
import peony.game.skill.Skill;

public class SkillUtil {
    private final static Logger log = Logger.getLogger(SkillUtil.class);
	
	public static  void initSkills(){
        // 清除所有旧技能
        ObjectAccessor.clearSkills();
        
	    // 创建自动攻击技能，所有职业都会
	    Skill autoAttack = new AutoAttackSkill(1);
	    ObjectAccessor.addSkill(autoAttack);
	    ObjectAccessor.addInitSkill(Unit.CLASS_1, autoAttack);
        ObjectAccessor.addInitSkill(Unit.CLASS_2, autoAttack);
        ObjectAccessor.addInitSkill(Unit.CLASS_3, autoAttack);
        ObjectAccessor.addInitSkill(Unit.CLASS_4, autoAttack);
	    
	    // 创建所有数据目录中配置的技能
	    ProjectData data = Server.server.getServiceRegistry().getDataService().data;
	    List<DataObject> skillConfigs = data.getDataListByType(SkillConfig.class);
	    ObjectAccessor.addSkill(new AutoAttackSkill(1));
	    for (DataObject dobj : skillConfigs) {
	        SkillConfig skillConfig = (SkillConfig)dobj;
	        createSkillSet(skillConfig);
	    }
	}
	
	private static Class loadSkillClass(String name) throws Exception {
	    ClassLoader classLoader = BuffUtil.getClassLoader();
        if (classLoader != null) {
            return classLoader.loadClass(name);
        }
        return Class.forName(name);
    }
	
	public static void createSkillSet(SkillConfig skillConfig) {
	    try {
	        log.info("skill " + skillConfig.id + " class " + skillConfig.implClass);
            Class clazz = loadSkillClass(skillConfig.implClass);
            Constructor constructor = clazz.getConstructors()[0];
            
            // 创建0级技能，然后通过查找下级技能接口找出所有下一级的技能
            Skill skill = (Skill)constructor.newInstance(0);
            ObjectAccessor.addSkill(skill);
            if (skill.getClazz() >= Unit.CLASS_1 && skill.getClazz() <= Unit.CLASS_4) {
            	//国公技能不能初始化到所有玩家
            	if(!CandidateService.isKingSkill(skill.getGroupId()) && !(skill.getGroupId()==336 || 
            			skill.getGroupId()==337 || skill.getGroupId()==338 || skill.getGroupId()==339))
            		ObjectAccessor.addInitSkill(skill.getClazz(), skill);
            }
            while (true) {
                skill = skill.getNextLevel();
                if (skill != null) {
                    ObjectAccessor.addSkill(skill);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e, e);
        }
	}
	
	private static final int[] FIRST_SKILL_GROUPID = {1,21,41,61};
	
	public static Skill getFirstLearSkil(int clazz){
		return ObjectAccessor.getSkill(Skills.getSkillId(FIRST_SKILL_GROUPID[clazz], 1));
	}
	
	private static final String[] MANA_STRING = {peony.Messages.STRING_01063,peony.Messages.STRING_01063,peony.Messages.STRING_01064,peony.Messages.STRING_01064};
	
	public static String getSkillDesc(Skill skill, Unit owner){
//		StringBuilder sb = new StringBuilder(skill.getDesc(owner));
//		if ((skill.getType() & (Skill.TYPE_PASSIVE | Skill.TYPE_BUFF)) == 0) {
//		    // 主动技能，显示有效距离、冷却时间和消耗
//		    int dist = skill.getDistance(owner);
//		    if (dist > 0) {
//		        sb.append("；有效距离");
//		        sb.append(CommonUtil.formatValue(dist / 8.0f));
//		        sb.append("尺，");
//		    } else {
//		        sb.append("；");
//		    }
//		    int mana = skill.getMP(owner);
//		    sb.append("消耗");
//		    sb.append(MANA_STRING[owner.clazz]);
//		    sb.append(mana);
//            sb.append("点，冷却时间");
//            sb.append(CommonUtil.formatMillSecond(skill.getCDTime(owner)));
//            sb.append("。");
//		} else {
//		    sb.append("。");
//		}
//		return sb.toString();
		if(skill.getClazz()==5 && skill.getLevel()==0){
			skill = ObjectAccessor.getSkill(Skills.getSkillId(skill.getGroupId(), 1));
		}
		String desc = skill.getDesc(owner);
		if ((skill.getType() & (Skill.TYPE_PASSIVE | Skill.TYPE_BUFF)) == 0) {
			int dist = skill.getDistance(owner);
			String distString = "";
		    if (dist > 0) {
		        distString = MessageFormat.format(peony.Messages.STRING_01065, desc,CommonUtil.formatValue(dist / 8.0f));
		    } else {
		    	distString = MessageFormat.format("{0}；", desc);
		    }
		    int mana = skill.getMP(owner);
            desc = MessageFormat.format(peony.Messages.STRING_01066
            		, distString, MANA_STRING[owner.clazz],mana,CommonUtil.formatMillSecond(skill.getCDTime(owner)));
		} else {
			desc = MessageFormat.format("{0}。", desc);
		}
		return desc;
	}
}
