package peony.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import peony.game.skill.Skill;

import com.pip.sanguo.data.DataChangeHandler;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.HorseType;


public class HorseUtil implements DataChangeHandler {
	
	public static final int[] LEVELUP_EXP = {
		0 ,
		1 ,
		1 ,
		1 ,
		1 ,
		1 ,
		2 ,
		2 ,
		3 ,
		3 ,
		4 ,
		5 ,
		7 ,
		10 ,
		14 ,
		19 ,
		25 ,
		32 ,
		41 ,
		52 ,
		65 ,
		80 ,
		97 ,
		116 ,
		137 ,
		160 ,
		185 ,
		212 ,
		242 ,
		275 ,
		311 ,
		350 ,
		392 ,
		438 ,
		486 ,
		537 ,
		591 ,
		649 ,
		711 ,
		777 ,
		848 ,
		924 ,
		1006 ,
		1094 ,
		1189 ,
		1293 ,
		1405 ,
		1527 ,
		1660 ,
		1805 ,
		2210 ,
		2975 ,
		4200 ,
		5985 ,
		8430 ,
		11635 ,
		15700 ,
		20725 ,
		26810 ,
		34056 ,
		42561 ,
		52826 ,
		65451 ,
		81236 ,
		101381 ,
		127786 ,
		163451 ,
		212976 ,
		256579 ,
		306008 ,
		538911 ,
		811376 ,
		1338701 ,
		2299778 ,
		3730960 ,
		6030737 ,
		9761697 ,
		15792435 ,
		25554132 ,
		41346567 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
		66900699 ,
	};
	
	/**
	 * 马的职业技能id
	 */
	public static final int[] HORSE_FACTION_SKILL= {72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91};
	
	/**
	 * 马的公共技能id
	 */
	public static final int[] HORSE_COMMON_SKILL = {92,102,103,104,105,106,107,108,109,110,111};
	
	private static final Random rnd = new Random();
	
	public static final int CHANGE_SKILL_ITEMID = 821;
	
	public static final int HORSE_SKILL_LOCK = 1817;
	
	public static final int HORSE_LOCKSKILL_COUNT = 1;
	
	public static final Skill getSkill(int faction,int[] skills){
		int n = rnd.nextInt(100);
		if(n>70){ //领悟职业技能
			List<Integer> l = filter(HORSE_FACTION_SKILL,skills);
			if(!l.isEmpty()){
				int i = rnd.nextInt(l.size());
				return ObjectAccessor.getSkill(Skills.getSkillId(l.get(i), 1));
			}else{
				l = filter(HORSE_COMMON_SKILL,skills);
				if(l.isEmpty()){
					return null;
				}
				int i = rnd.nextInt(l.size());
				return ObjectAccessor.getSkill(Skills.getSkillId(l.get(i), 1));
			}
		} else { // 领悟公共技能
			List<Integer> l = filter(HORSE_COMMON_SKILL, skills);
			if (!l.isEmpty()) {
				int i = rnd.nextInt(l.size());
				return ObjectAccessor.getSkill(Skills.getSkillId(l.get(i), 1));
			}else{
				l = filter(HORSE_FACTION_SKILL,skills);
				if(l.isEmpty()){
					return null;
				}
				int i = rnd.nextInt(l.size());
				return ObjectAccessor.getSkill(Skills.getSkillId(l.get(i), 1));
			}

		}
	}
	
	private static final List<Integer> filter(int[] v,int[] v1){
		List<Integer> l = new ArrayList<Integer>(v.length);
		for(int i:v){
			l.add(i);
		}
		if(v1.length==0)
			return l;
		for(int i:v1){
			l.remove(new Integer(i));
		}
		return l;
	}
	
	@SuppressWarnings("unchecked")
	public static void load() throws Exception{
		DataService dataService = Server.server.getServiceRegistry().getDataService();
		List templates = dataService.data.getDataListByType(HorseType.class);
		for(Object o:templates){
			HorseType type = (HorseType)o;
			ObjectAccessor.addHorseType(type);
		}
	}
	
    public static int getUpLevel(int oldLevel, int exp){
        int level = 0;
        int upLevelExp = LEVELUP_EXP[oldLevel];
        while(true){
            if(upLevelExp > exp)
                return level;
            exp -= upLevelExp;
            level++;
            upLevelExp = LEVELUP_EXP[oldLevel + level];
        }
    }

    public static int getUpLevelExp(int oldLevel, int newLevel){
    	if(newLevel==oldLevel+1)
    		return LEVELUP_EXP[oldLevel];
        int ret = 0;
        for(int i = oldLevel; i < newLevel; i++){
            ret += LEVELUP_EXP[i];
        }
        return ret;
    }
    
    /**
     * 添加新对象通知。
     * @param obj 新添加的对象
     */
    public void dataObjectAdded(DataObject obj) {
        if (obj instanceof HorseType) {
            ObjectAccessor.addHorseType((HorseType)obj);
        }
    }
    
    /**
     * 对象被删除通知。
     * @param obj 被删除的老对象
     */
    public void dataObjectRemoved(DataObject obj) {
        if (obj instanceof HorseType) {
            ObjectAccessor.horseTypes.remove(obj.id);
        }
    }
    
    /**
     * 对象即将被修改通知。
     * @param obj 修改前的对象
     */
    public void dataObjectChanging(DataObject obj) {
    }
    
    /**
     * 对象被修改通知。
     * @param newobj 修改后的新对象
     */
    public void dataObjectChanged(DataObject newobj) {
    }

	public static int getLockSkillCount(Horse h) {
		int lockSkillId = h.lockSkillId;
		int count = 0;
		for(int i=0;i<31;i++){
			if(((1<<i) & lockSkillId)==(1<<i))
				count++;
		}
		return count;
	}
}
