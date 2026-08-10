package peony.game.buff;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import peony.game.CombatContext;
import peony.game.CombatEffect;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PropertyCalculator;
import peony.game.PropertyEnhancer;
import peony.game.Server;
import peony.game.Skills;
import peony.game.Time;
import peony.game.Unit;
import peony.game.UnitEffect;
import peony.game.Updatable;
import peony.game.attendant.Attendant;
import peony.game.attendant.AttendantFixService;
import peony.game.itemeffect.KingItemEffect;
import peony.game.nation.CandidateService;
import peony.game.skill.Skill;
import peony.game.skill.SkillEnhancer;
import peony.net.Packet;
import peony.service.enhance.EquipLevelUpInfoCall;
import peony.service.fiveelement.FiveElementService;
import peony.service.stat.StatService;
import peony.util.IntHashSet;

/**
 * 人物BUFF集合。BUFF分为以下几种： 
 * 1. 被动技能BUFF，永久性的增益BUFF，ID和技能ID相同，只有在技能表变更的时候才更新。 
 * 2. 光环BUFF，队友或自己的光环技能增加的BUFF，ID和技能ID相同，在队友状态变化时更新。 
 * 3. 临时BUFF，被自己或其他单位使用技能/物品添加的BUFF，有独立的ID空间，通常有时间限制。
 * 
 * @author jeffrey
 */
public class Buffs implements PropertyEnhancer, CombatEffect, SkillEnhancer {
	
	
    private static Logger log = Logger.getLogger(Buffs.class);
	protected List<Buff> buffs = new ArrayList<Buff>();
	
	protected List<Buff> tempBuffs = new ArrayList<Buff>();
	
	protected List<Buff> pendingBuffs = new ArrayList<Buff>();
	protected ParamEnhanceSet paramEnhances = new ParamEnhanceSet();
//	protected boolean isBreak = false; //是否短路所有的buff，用在清空buff以后不让后续的buff再执行，如果在buff的update中清空buff，那么后续的buff是会执行的，这样就会带来问题
//	/*
//	 * 暂存移除的显示Buff，除了光环Buff
//	 */
//	protected List<Buff> removedBuffs = new LinkedList<Buff>();
//	/*
//	 * 暂存增加的显示Buff，除了光环Buff
//	 */
//	protected List<Buff> addedBuffs = new LinkedList<Buff>();
//	
//	/*
//	 * 暂存merged的显示Buff，除了光环Buff
//	 */
//	protected List<Buff> mergedBuffs = new LinkedList<Buff>();
	
	protected int updateTime;
	
	public Unit owner;
	
	protected int lastUpdateLogBuffTime;
	Map<Integer, Integer> logBuff = new HashMap<Integer, Integer>();
	
	protected static int[] noSingleSuiteBuffs = {407,650,654,655,656,
		662,659,660,661,662,663,664,665,666,667,668,669,670,
		671,672,673,674,675,676,677,678,679,680,681,682,701,703,714,715,712,716,717
	}; //非单例套装buff
	
	public Buffs(Unit owner){
		this.owner = owner;
	}
	
	public void allocationTempBuffs(int[] arr){
		for(int id : arr){
			List<Buff> removes = getBuffsById(id);
			for(Buff b : removes){
				tempBuffs.add(b);
			}
			removeBuff(id);
		}
		removeSpecialBuff();
	}
	
	public List<Buff> getBuffsById(int id){
		List<Buff> list = new ArrayList<Buff>();
		for(Buff b : buffs){
			if(b!=null && b.getId()==id)
				list.add(b);
		}
		return list;
	}
	
	public void restoreTempBuffs(){
		for(Buff b : tempBuffs){
			addBuff(b);
		}
		owner.refreshProperties(false);
		tempBuffs.clear();
	}
	
	public void removeSpecialBuff(){
		Iterator<Buff> it = buffs.iterator();
		while(it.hasNext()){
			Buff b = it.next();
			if(b!=null && b instanceof NationBuff || b.getId()==216){
				it.remove();
				buffRemoved(b);
			}
		}
		CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
		owner.buffs.removeBuff(216);
		owner.skills.removeSkill(candidateService.getKingSkillGroupId(owner.clazz), 1);
		owner.skills.removeSkill(candidateService.getKingSkillGroupId(owner.clazz), 0);
		owner.buffs.removeBuff(Skills.getSkillId(candidateService.getKingSkillGroupId(owner.clazz), 1));
	}
	
	/**
	 * 取得所有BUFF。
	 * @return
	 */
	public Buff[] getBuffs() {
		Buff[] ret = new Buff[buffs.size()];
		buffs.toArray(ret);
		return ret;
	}
	
	/**
	 * 清除所有BUFF。
	 */
	public synchronized void clear() {
		Iterator<Buff> ite = buffs.iterator();
		while (ite.hasNext()) {
			Buff buff = ite.next();
			if (!buff.keepOnDie()) { //有些buff死亡只有还需要存在
				ite.remove();
				buffRemoved(buff);
			}
		}
		// isBreak = true;
	}
	
	public synchronized void clearAllBuffs(){
		Iterator<Buff> ite = buffs.iterator();
		while (ite.hasNext()) {
			Buff buff = ite.next();
			ite.remove();
			buffRemoved(buff);
		}
	}
	
	public ParamEnhanceSet getParamEnhances() {
	    return paramEnhances;
	}
	
	/**
	 * 根据InstanceID查找BUFF
	 * @param instanceid
	 * @return
	 */
	public Buff getBuffByInstanceID(int id){
		for(Buff buff:buffs){
			if(buff.getInstanceID()==id){
				return buff;
			}
		}
		return null;
	}
	
	/**
	 * 根据id查找BUFF
	 * @param id
	 * @return
	 */
	public Buff getBuffByID(int id){
		for(Buff buff:buffs){
			if(buff.getId()==id){
				return buff;
			}
		}
		return null;
	}
	
	/*
	 * 是否是显示buff
	 */
	protected boolean isNeedNotifyClient(Buff buff){
		return buff.getIconID()!=-1||(buff.getId() == KingItemEffect.KINGWEAPON_BUFFID)||AttendantFixService.showBuff(buff);
	}

	/**
	 * 添加一个新BUFF。
	 * @param buff
	 * @return 如果这个BUFF可以被一个已有BUFF合并，返回合并者，否则返回新加的BUFF本身。
	 */
	public synchronized Buff addBuff(Buff buff) {
		if(buff==null)
			return null;
	    buff.setOwner(owner.ref());
	    buff.resetParams(owner);
		if (buff.isNeedMerge()) {
			for (Buff b : buffs) {
				if (b instanceof ParamEnhancer) {
					((ParamEnhancer)b).removeEnhanceParams(paramEnhances);
				}
				if (b.merge(buff)){
					if(b instanceof PropertyEnhancer){
						owner.refreshProperties(false);
					}
					if (b instanceof ParamEnhancer) {
					    // 如果添加了一个可以影响BUFF参数的BUFF，重新计算已有BUFF的属性
					    ((ParamEnhancer)b).getEnhanceParams(paramEnhances);
					    for (Buff existBuff : buffs) {
					        existBuff.resetParams(owner);
					    }
					}
					if(isNeedNotifyClient(b)){
//						mergedBuffs.add(b);
						owner.moveExtended |= GameObject.MOVEEXT_BUFFS;
					}
					return b;
				} else {
					if (b instanceof ParamEnhancer) {
						((ParamEnhancer)b).getEnhanceParams(paramEnhances);
					}
				}
			}
		}
		buffs.add(buff);
		if(buff instanceof UnitEffect){
			((UnitEffect)buff).effect(owner);
		}
		if(buff instanceof PropertyEnhancer){
			owner.refreshProperties(false);
		}
		if (buff instanceof ParamEnhancer) {
		    // 如果添加了一个可以影响BUFF参数的BUFF，重新计算已有BUFF的属性
		    ((ParamEnhancer)buff).getEnhanceParams(paramEnhances);
		    for (Buff existBuff : buffs) {
		        existBuff.resetParams(owner);
		    }
		}
		if(isNeedNotifyClient(buff)){
//			addedBuffs.add(buff);
			owner.moveExtended |= GameObject.MOVEEXT_BUFFS;
		}
		if(buff instanceof SkillEnhancer&&!(buff instanceof Updatable)){
			notifySkillChanged(buff);
		}
		return buff;
	}
	
	public void removeUnitEffectBuffState(){
		for(Buff buff : buffs){
			if(buff!=null && buff instanceof UnitEffect){
				((UnitEffect)buff).unEffect(owner);
			}
		}
	}
	
	protected void notifySkillChanged(Buff buff){
		SkillEnhancer se = (SkillEnhancer)buff;
		IntHashSet skillIds = se.getAffectSkillIDs();
		List<Skill> ss = new ArrayList<Skill>(skillIds.size());
		for(int skillId:skillIds.getValues()){
			Skill s = owner.skills.getSkillByGroupId(skillId);
			if(s!=null)
				ss.add(s);
		}
		if (ss.size() > 0) {
			Packet pt = new Packet(OpCode.SKILL_INFO_CHANGED_SERVER);
			pt.put(ss.size());
			for (Skill skill:ss) {
				pt.putInt(skill.getId());
				pt.putInt(skill.getCDTime(owner));
				pt.putShort(skill.getDistance(owner));
				pt.putShort(skill.getActTime(owner));
				pt.putShort(skill.getMP(owner));
			}
			if (owner.type == GameObject.TYPE_PLAYER) {
				((Player) owner).send(pt);
			}
		}
	}

	/**
	 * 添加一组BUFF。
	 * @param buffs
	 * @return 返回添加后的BUFF集合。如果某个BUFF被已有BUFF合并，返回的集合中包含合并后的BUFF。
	 */
	public synchronized List<Buff> addBuffs(Buffs buffs) {
		HashSet<Buff> ret = new HashSet<Buff>();
		for (Buff b : buffs.buffs) {
			Buff added = addBuff(b);
			ret.add(added);
		}
		return new ArrayList<Buff>(ret);
	}

	/**
	 * 根据ID移除所有同类BUFF。通常用来移除被动技能BUFF。
	 * @param id
	 */
	public synchronized void removeBuff(int id) {
		int count = buffs.size();
		for (int i = count - 1; i >= 0; i--) {
			if (buffs.get(i).getId() == id) {
				Buff buff = buffs.remove(i);
				buffRemoved(buff);
			}
		}
	}
	
	/**
	 * 移除一个全局的buff,这种buff在服务器内只会存在一个实例
	 * @param buff
	 */
	public synchronized void removeBuff(Buff b){
		int count = buffs.size();
		for (int i = count - 1; i >= 0; i--) {
			if (buffs.get(i)==b) {
				Buff buff = buffs.remove(i);
				buffRemoved(buff);
			}
		}
	}
	
	public synchronized void removeUpdatableBuffs(){
		Iterator<Buff> ite = buffs.iterator();
		while(ite.hasNext()){
			Buff buff = ite.next();
			if(buff instanceof Updatable){
				ite.remove();
				buffRemoved(buff);
			}
		}
	}
	
	public static int MAX_TIME = 2*3600*1000;
	
	/**
	 * 用在重新载入角色的时候，需要重置身上的updatable接口的buff
	 * @param diffTime
	 */
	public void update2(int diffTime){
		List<Buff> bs = new ArrayList<Buff>(buffs);
		for(Buff buff:bs){
			if(buff instanceof Updatable){
				boolean b = ((Updatable)buff).update2(diffTime);
				if(!b){
					removeBuff(buff.getId());
				}else{
					if(buff instanceof Buff){
						Buff nb = (Buff)buff;
						int endTime = nb.getEndTime();
						int v = endTime - Time.currTime; 
						if(v > MAX_TIME){  //出现过一个bug，会导致endtime一直被累加，这里是要把数据库里错误的扣回来
							v -= MAX_TIME;
							((Updatable)buff).update2(-v);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 删除所有由某人加上的不良buff，用在PK以后胜利方移除不良debuff
	 * @param ref
	 */
	public synchronized void removeDebuffs(GameObjectRef ref){
		Iterator<Buff> ite = buffs.iterator();
		while(ite.hasNext()){
			Buff buff = ite.next();
			if(!buff.isGood()&&buff.getSource()!=null){
				if(buff.getSource().equals(ref)){
					ite.remove();
					buffRemoved(buff);
				}
			}
		}
	}

	/**
	 * 移除所有光环BUFF。
	 * @return 被移除的所有BUFF。
	 */
	public synchronized void removeAreaBuffs() {
		Iterator<Buff> ite = buffs.iterator();
		while (ite.hasNext()) {
			Buff buff = ite.next();
			if (buff.isAreaBuff()) {
				ite.remove();
				buffRemoved(buff);
			}
		}
	}
	
	/**
	 * 驱散一个或所有临时有益BUFF。
	 * @param removeAll
	 */
	public synchronized void dispelGoodBuff(boolean removeAll) {
	    int count = buffs.size();
        for (int i = count - 1; i >= 0; i--) {
            Buff buff = buffs.get(i);
            if (buff instanceof Updatable && buff.isGood() && buff.dispelable()) {
                buffs.remove(i);
                buffRemoved(buff);
                if (!removeAll) {
                    break;
                }
            }
        }
	}

    /**
     * 驱散一个或所有临时有害BUFF。
     * @param removeAll
     */
    public synchronized void dispelBadBuff(boolean removeAll) {
        int count = buffs.size();
        for (int i = count - 1; i >= 0; i--) {
            Buff buff = buffs.get(i);
            if (buff instanceof Updatable && !buff.isGood() && buff.dispelable()) {
                buffs.remove(i);
                buffRemoved(buff);
                if (!removeAll) {
                    break;
                }
            }
        }
    }
	
	protected synchronized void buffRemoved(Buff buff){
		if(buff instanceof UnitEffect){
			((UnitEffect)buff).unEffect(owner);
		}
		if(buff instanceof PropertyEnhancer){
			owner.refreshProperties(false);
		}
		if (buff instanceof ParamEnhancer) {
		    // 如果移除了一个可以影响BUFF参数的BUFF，重新计算已有BUFF的属性
            ((ParamEnhancer)buff).removeEnhanceParams(paramEnhances);
            for (Buff existBuff : buffs) {
                existBuff.resetParams(owner);
            }
		}
		if (isNeedNotifyClient(buff)) {
//			removedBuffs.add(buff);
			owner.moveExtended |= GameObject.MOVEEXT_BUFFS;
		}
		if(buff instanceof SkillEnhancer &&!(buff instanceof Updatable)){
			notifySkillChanged(buff);
		}
		
		//TODO:会引起ConcurrentModificationException
		try {
			Field f_id = buff.getClass().getDeclaredField("buffover_addbuff");
			Field f_level = buff.getClass().getDeclaredField("buffover_addbuff_level");
			int addBuffId = f_id.getInt(buff);
			int addBuffLevel = f_level.getInt(buff);
			if(addBuffId>0 && addBuffLevel>0){
				Buff addBuff = BuffUtil.createBuff(addBuffId, addBuffLevel, owner, owner, 0);
				pendingBuffs.add(addBuff);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 更新所有BUFF的状态，在游戏循环中调用。返回是否需要重新计算属性
	 * 返回被移除的buff集合
	 */
	public synchronized boolean update(int diff) {
		boolean ret = false;
		if (pendingBuffs.size() > 0) {
			Object[] arr = pendingBuffs.toArray();
			pendingBuffs.clear();
			for (int i = 0; i < arr.length; i++) {
				addBuff((Buff)arr[i]);
			}
		}
		if (Time.currTime - updateTime > 1000) {
			try {
				int size = buffs.size();
				for (int i = 0; i < size; i++) {
					Buff buff = (Buff)buffs.get(i);
					if (buff instanceof Updatable) {
						if (((Updatable) buff).update(diff)) {
							buffs.remove(i);
							buffRemoved(buff);
							i--;
							size--;
						}
					}
				}
			} catch (Exception e) {
				log.error(e, e);
			}
			updateTime = Time.currTime;
		}
		return ret;
	}
	
	public void updateLogBuff(){
		if(owner instanceof Player){
			if(Time.currTime-lastUpdateLogBuffTime>30000){
				try {
					Iterator<Buff> it = buffs.iterator();
					while(it.hasNext()){
						Buff buff = it.next();
						int count = logBuff.get(buff.getId())==null ? 0 : logBuff.get(buff.getId()).intValue();
						logBuff.put(buff.getId(), count+1);
					}
					for(int key : logBuff.keySet()){
						int total = logBuff.get(key);
						if(total>1)
							log.info("[DOUBTBUFF]"+LogUtil.getPlayerLogString((Player)owner)+"BUFFID["+key+"]COUNT["+logBuff.get(key).intValue()+"]");
					}
				} catch (Exception e) {
				}
				logBuff.clear();
				lastUpdateLogBuffTime = Time.currTime;
			}
		}
	}

	/**
	 * 计算BUFF对人物属性的影响。
	 * @param pc 人物属性计算环境
	 */
	public void enhance(PropertyCalculator pc) {
		for (Buff buff : buffs) {
			if(pc.unit instanceof Player || pc.unit instanceof Attendant)
			if (buff instanceof PropertyEnhancer) {
				if(EquipLevelUpInfoCall.isLevelUpEquip(buff.getId())){
					continue;
				}
				if(Unit.isPreBuff(buff.getId()))
					continue;
				((PropertyEnhancer) buff).enhance(pc);
			}
		}
	}

	/*
	 * 下面是CombatEffect接口的实现。
	 */
	
	public void finished(CombatContext context, boolean isActive) {
		List<Buff> bs = new ArrayList<Buff>(buffs);
		for (Buff buff : bs) {
			if (buff instanceof CombatEffect) {
				((CombatEffect) buff).finished(context, isActive);
			}
		}
	}

	public void postDamage(CombatContext context, boolean isActive) {
		for (Buff buff : buffs) {
			if (buff instanceof CombatEffect) {
				((CombatEffect) buff).postDamage(context, isActive);
			}
		}
	}

	public void postHit(CombatContext context, boolean isActive) {
		for (Buff buff : buffs) {
			if (buff instanceof CombatEffect) {
				((CombatEffect) buff).postHit(context, isActive);
			}
		}
	}

	public void preDamage(CombatContext context, boolean isActive) {
		for (Buff buff : buffs) {
			if (buff instanceof CombatEffect) {
				//特殊BUFF的preDamage
				if(isProcessFiveElementBuffs(context,buff))
					return;
				((CombatEffect) buff).preDamage(context, isActive);
			}
		}
	}
	
	/** 特殊处理袁绍副本buff */
	protected boolean isProcessFiveElementBuffs(CombatContext context, Buff buff){
		if(context.source.type == GameObject.TYPE_PLAYER && context.target.type == GameObject.TYPE_CREATURE){
			FiveElementService service = Server.server.getServiceRegistry().getFiveElementService();
			if(StatService.isInArray(FiveElementService.FIVEELE_BUFFS, buff.getId())!=-1){
				if(!service.checkDamage(context,buff))
					return true;
			}
		}
		return false;
	}

	public void preHit(CombatContext context, boolean isActive) {
		for (Buff buff : buffs) {
			if (buff instanceof CombatEffect) {
				((CombatEffect) buff).preHit(context, isActive);
				logDoubtBuff(context);
			}
		}
	}
	
	protected void logDoubtBuff(CombatContext context){
		try {
			if(context!=null && owner!=null && owner instanceof Player){
				if(context.critRate>0.9)
					log.info("[DOUBTBUFFCRITRATE]"+LogUtil.getPlayerLogString((Player)owner)+context.critRate);
				if(context.dodge>0.9)
					log.info("[DOUBTBUFFDODGERATE]"+LogUtil.getPlayerLogString((Player)owner)+context.dodge);
			}
		} catch (Exception e) {
		}
	}

	/* 
	 * 下面是SkillEnhancer接口的实现
	 */

	public IntHashSet getAffectSkillIDs() {
		IntHashSet ret = new IntHashSet();
		for (Buff buff : buffs) {
			if (buff instanceof SkillEnhancer) {
				int[] ids = ((SkillEnhancer) buff).getAffectSkillIDs().getValues();
				for (int id : ids) {
					ret.add(id);
				}
			}
		}
		return ret;
	}

	public float updateCDTime(Skill skill, float cd) {
		for (Buff buff : buffs) {
			if (buff instanceof SkillEnhancer && ((SkillEnhancer)buff).getAffectSkillIDs().contains(skill.getGroupId())) {
				cd = ((SkillEnhancer) buff).updateCDTime(skill, cd);
			}
		}
		return cd;
	}

	public float updateDistance(Skill skill, float distance) {
		for (Buff buff : buffs) {
            if (buff instanceof SkillEnhancer && ((SkillEnhancer)buff).getAffectSkillIDs().contains(skill.getGroupId())) {
				distance = ((SkillEnhancer) buff).updateDistance(skill, distance);
			}
		}
		return distance;
	}

	public float updateActTime(Skill skill, float actTime) {
		for (Buff buff : buffs) {
            if (buff instanceof SkillEnhancer && ((SkillEnhancer)buff).getAffectSkillIDs().contains(skill.getGroupId())) {
				actTime = ((SkillEnhancer) buff).updateActTime(skill, actTime);
			}
		}
		return actTime;
	}

	public float updateRange(Skill skill, float range) {
		for (Buff buff : buffs) {
            if (buff instanceof SkillEnhancer && ((SkillEnhancer)buff).getAffectSkillIDs().contains(skill.getGroupId())) {
				range = ((SkillEnhancer) buff).updateRange(skill, range);
			}
		}
		return range;
	}

	public float updateMP(Skill skill, float mp) {
		for (Buff buff : buffs) {
            if (buff instanceof SkillEnhancer && ((SkillEnhancer)buff).getAffectSkillIDs().contains(skill.getGroupId())) {
				mp = ((SkillEnhancer) buff).updateMP(skill, mp);
			}
		}
		return mp;
	}
	
//	public void clearHistory(){
//		removedBuffs.clear();
//		addedBuffs.clear();
//		mergedBuffs.clear();
//	}
	
//	public List<Buff> getRemovedBuffs(){
//		return removedBuffs;
//	}
//	
//	public List<Buff> getAddedBuffs(){
//		return addedBuffs;
//	}
//
//	public List<Buff> getMergedBuffs(){
//		return mergedBuffs;
//	}
	
	/**
	 * 取所有客户端需要显示的Buff
	 * @return
	 */
	public List<Buff> getShowBuffs(){
		List<Buff> ret = new LinkedList<Buff>();
		for(Buff buff:buffs){
			if(buff.getIconID()!=-1 || buff.getId() == KingItemEffect.KINGWEAPON_BUFFID || AttendantFixService.showBuff(buff)){
				ret.add(buff);
			}
		}
		return ret;
	}
	
	
	
	public byte[] toClientBytes(){
		List<Buff> l = getShowBuffs();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try{
			dos.write(l.size());
			for(Buff buff:getShowBuffs()){
				dos.writeInt(buff.getId());
				if(buff.getId() == KingItemEffect.KINGWEAPON_BUFFID){
					dos.writeInt(2);
					dos.writeInt(-1);
				}else if(AttendantFixService.showBuff(buff)){
					Skill skill = ObjectAccessor.getSkill(buff.getId());
					if(skill!=null){
						dos.writeInt(skill.getIconId());
						dos.writeInt(-1);
					}else{
						dos.writeInt(2);
						dos.writeInt(-1);
					}
				}else{
					dos.writeInt(buff.getIconID());
					dos.writeInt(buff.getEndTime());
				}
			}
		}catch(Exception ex){
			log.error(ex, ex);
		}
		return bos.toByteArray();
	}
	
	public byte[] toDBBytes(){
		List<Buff> l = new LinkedList<Buff>();
		for(Buff buff:buffs){
			if(buff instanceof Updatable&&!(buff instanceof SkillBuff)){
				l.add(buff);
			}
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try{
			if(l.size()==0){
				return new byte[0];
			}
			dos.writeShort(l.size());
			for(Buff buff:l){
				dos.writeInt(buff.getId());
				byte[] bytes = ((Updatable)buff).save();
				dos.writeShort(bytes.length);
				dos.write(bytes);
			}
		}catch(Exception ex){
		    log.error(ex, ex);
		}
		return bos.toByteArray();
	}
	
	public static Buff[] getBuffs(byte[] bytes, Player unit) {
		if (bytes.length == 0)
			return new Buff[0];
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bis);
		List<Buff> l = new ArrayList<Buff>();
		int diffTime = (unit.lastLogoutTime==null)?0:(int)(System.currentTimeMillis() - unit.lastLogoutTime.getTime());
//		int logoutTime = unit.lastLogoutTime==null?(Time.currTime-3600*1000):Time.elapseTime(unit.lastLogoutTime.getTime());
		if(diffTime<0){
			log.error("[BUFFTIMEERROR]");
			diffTime = 0;
		}
		try {
			short size = dis.readShort();
			for (int i = 0; i < size; i++) {
				int buffId = dis.readInt();
				short len = dis.readShort();
				byte[] bs = new byte[len];
				dis.read(bs);
				Buff buff = null;
				if(buffId>10000){
					if(buffId==10002)
						buff = new FearDebuff(unit, 0);
					else if(buffId==10003)
						buff = new ParalyzeDebuff(unit, 0);
					else if(buffId==10005)
						buff = new StayDebuff(unit, 0);
					else if(buffId==10001)
						buff = new DumbDebuff(unit, 0);
				}else
					buff = BuffUtil.createBuff(buffId, 1, unit, unit, 0);
				if (buff != null) {
					Updatable u = (Updatable) buff;
					u.load(bs);
					l.add(buff);
//					if (u.update2(diffTime)) {
//						l.add(buff);
//					}
				}
			}
		} catch (Exception ex) {
			log.error(ex, ex);
		}
		Buff[] ret = new Buff[l.size()];
		l.toArray(ret);
		return ret;
	}
	
	@Override
	public Buffs clone(){
		Buffs ret = new Buffs(owner);
		ret.buffs = new ArrayList<Buff>(buffs);
		return ret;
	}
	
	public boolean isNoSingleSuiteBuff(int buffId){
		for(int id : noSingleSuiteBuffs){
			if(id==buffId)
				return true;
		}
		return false;
	}
	
}
