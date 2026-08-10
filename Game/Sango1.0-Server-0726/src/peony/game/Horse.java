package peony.game;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;
import peony.game.buff.BuffUtil;
import peony.game.changed.ChangedItem;
import peony.game.changed.HorseIntPropertyChangedItem;
import peony.game.changed.HorseSkillChangedItem;
import peony.game.changed.HorseStringPropertyChangedItem;
import peony.game.skill.Skill;
import peony.net.Packet;
import peony.service.ServiceEvent;

import com.pip.sanguo.data.HorseType;

public class Horse implements PropertyEnhancer,GameItemObject{
	
	private static final Random rnd = new Random();
	
	private static final Logger log = Logger.getLogger(Horse.class);
	
	public HorseType template;
	
	public static final int MAXDEGREE = 100;
	
	public int skillSize;
	public List<Skill> skills;

	public int strength;
	public int agility;
	public int intellect;
	public int stamina;
	public int speed;
	
	public int point;
	
	public int strengthAdded;
	public int agilityAdded;
	public int intellectAdded;
	public int staminaAdded;
	public int speedAdded;
	
	public int exp;
	
	public int degree;
	
	public int level;
	
	public int instanceId;
	
	public short score;
	
	public String name;
	
	public int imageId;
	public int iconId;
	public int agentHorse = 0; // 0非代理饲养 1代理饲养
	public long agentTime;
	public int leavingExp;
	public int lockSkillId;
	public int initLevel;
	
	public int itemId; //生成马的物品Id
	
	public int foodId;
	
	public HorseEquipments equs;
	
	//最后一次减去饱食度的时间
	public int lastDecDegreeTime; 
	
	public int notOnlineExpTime;
	
	public byte state; //坐骑状态    0：已过期未激活    1：已激活   2：未过期 未激活
	
	protected static int[] freeHorseItemIds = {2475, 2476, 2477, 2478}; //具有免费使用时间的物品ID
	
	protected static int[] freeTimes = {7*24*3600*1000,7*24*3600*1000,7*24*3600*1000,7*24*3600*1000};
	
	public long freeHorseEndTime; //免费使用结束时间
	
	public static int STATE_ACTIVE = 1; //激活状态
	
	public Horse(HorseType type, int instanceId) {
		this.template = type;
		this.instanceId = instanceId;
		skills = new ArrayList<Skill>();
		equs = new HorseEquipments(this);
		skillSize = 6;
		foodId = -1;
	}
	
	@Override
	public Horse clone(){
		Horse h = new Horse(template,instanceId);
		h.skillSize = skillSize;
		h.skills = new ArrayList<Skill>(skills);
		h.strength = strength;
		h.agility = agility;
		h.intellect = intellect;
		h.stamina = stamina;
		h.speed = speed;
		h.point = point;
		h.strengthAdded = strengthAdded;
		h.agilityAdded = agilityAdded;
		h.intellectAdded = intellectAdded;
		h.staminaAdded = staminaAdded;
		h.speedAdded = speedAdded;
		h.exp = exp;
		h.degree = degree;
		h.level = level;
		h.score = score;
		h.name = name;
		h.imageId = imageId;
		h.initLevel = initLevel;
		h.foodId = foodId;
		h.iconId = iconId;
		h.itemId = itemId;
		h.agentHorse = agentHorse;
		h.agentTime = agentTime;
		h.leavingExp = leavingExp;
		h.lockSkillId = lockSkillId;
		h.equs = equs.clone();
		return h;
	}
	
	public void enhance(PropertyCalculator pc) {
		pc.stamina += stamina;
		pc.strength += strength;
		pc.agility += agility;
		pc.intellect += intellect;
		double speedRateAdd;
		if (speed < 300) {
			speedRateAdd = speed / 400.0f;
		} else {
			speedRateAdd = 0.75f + Math.pow(speed - 300, 0.8) / 400;
		}
		pc.setHorseSpeed(0.15f + (float)speedRateAdd);
		this.equs.enhance(pc, false);
	}
	
	public void addSkill(Skill skill, Player p){
		skills.add(skill);
		if(p!=null&&p.horse==this){
			p.buffs.addBuff(skill.newBuff());
		}
		HorseSkillChangedItem c = new HorseSkillChangedItem(this,skill,true,true);
		if(p!=null)
			p.changed.addChangedItem(c);

	}
	
	public Skill getSkill(int skillId){
		for(Skill skill:skills){
			if(skill.getId()==skillId)
				return skill;
		}
		return null;
	}
	
	public Skill removeSkill(int skillId,Player p){
		Iterator<Skill> ite = skills.iterator();
		while(ite.hasNext()){
			Skill skill = ite.next();
			if(skill.getId()==skillId){
				ite.remove();
				p.buffs.removeBuff(skill.getId());
				HorseSkillChangedItem c = new HorseSkillChangedItem(this,skill,false,true);
				p.changed.addChangedItem(c);
				return skill;
			}
		}
		return null;
	}
	
	/**
	 * 随机遗忘一个技能
	 * @param p
	 * @return
	 */
	public Skill removeSkill(Player p){
		int i = rnd.nextInt(skills.size());
		while(skills.get(i).getId()==lockSkillId){
			i = rnd.nextInt(skills.size());
		}
		Skill s = skills.remove(i);
		p.buffs.removeBuff(s.getId());
		HorseSkillChangedItem c = new HorseSkillChangedItem(this,s,false,true);
		p.changed.addChangedItem(c);
		return s;
	}
	
	public Skill removeSkill(Player p, Skill newSkill){
		int i = rnd.nextInt(skills.size());
		if(HorseUtil.getLockSkillCount(this)>0 && skills.size()>HorseUtil.getLockSkillCount(this)){
			while(((lockSkillId>>i)&1)==1){
				i = rnd.nextInt(skills.size());
			}
		}else if(HorseUtil.getLockSkillCount(this)>0 && skills.size()==HorseUtil.getLockSkillCount(this)){
			return null;
		}
		Skill s = skills.get(i);
		if (s!=null && newSkill != null) {
			p.buffs.removeBuff(s.getId());
			skills.set(i, newSkill);
			if(p!=null)
				p.message(-1, MessageFormat.format("习得新技能{0}", newSkill.getName()), -1, -1);
			if(p!=null&&p.horse==this){
				p.buffs.addBuff(newSkill.newBuff());
			}
		}
		if(skills!=null && skills.size()>0 && p!=null){
			Packet pt = new Packet(OpCode.HORSE_SKILLS_SERVER);
			pt.putInt(skills.size());
			for(Skill skill : skills){
				pt.put(skill.toClientBytes(p));
			}
			p.send(pt);
		}
		return s;
	}
	
	public void update(Player p){
		if(lastDecDegreeTime!=0){
			if(p.systemState!=Player.SYSTEMSTATE_READY)
				return;
			if((Time.currTime-lastDecDegreeTime)>=60*1000L){
				int d = Math.max(degree-1, 0);
				setDegree(d, p);
				lastDecDegreeTime = Time.currTime;
			}
			if(this.degree<=20&&foodId!=-1){
				PlayerTransaction tx = p.newTransaction("AFD");
				GameItem item = p.bag.removeGameItem(foodId, -1, 1, tx, false);
				if(item!=null){
					tx.commit();
					int v = 50;
					if(item.template.id==ItemUtil.ITEM_HORSEFOOD_ADDBUFF){
						v = 70;
					}
					int degree = this.degree + v;
					setDegree(Math.min(degree, MAXDEGREE),p);
					if(item.template.id==ItemUtil.ITEM_HORSEFOOD_ADDBUFF){
						p.buffs.addBuff(BuffUtil.createBuff(164, 1, p, p, 0));
					}
				}else{
					tx.rollback();
				}
			}
			if(this.degree==0){
				p.horseUnride(-1);
			}
			if(freeHorseEndTime>0 && Time.currTime>Time.elapseTime(freeHorseEndTime)){
				p.horseUnride(-1);
				unActive();
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_STATE, state, false);
				Packet pt = new Packet(OpCode.OPENUI_SERVER);
				pt.putString("ui_npc_dialog");
				pt.putString("ACTIVE_HORSE|"+instanceId);
				p.send(pt);
			}
		}
	}
	
	public String getLeavingTime(){
		if(freeHorseEndTime<=0)
			return "";
		if(System.currentTimeMillis() > freeHorseEndTime){
			return MessageFormat.format("(过期了)", 0);
		}
		int leaving = Time.elapseTime(freeHorseEndTime) - Time.currTime;
		int day = leaving / (1000 * 3600 * 24);
		if(day>0)
			return MessageFormat.format("(剩余时间{0}天)", day);
		int hour = leaving / (1000 * 3600);
		if(hour>0)
			return MessageFormat.format("(剩余时间{0}时)", hour);
		return "";
	}
	
	public void setExp(int exp,Player p,String cause){
		if (this.exp != exp) {
			// 记录获得经验日志
			LogUtil.logGetHorseExp(p, this, this.exp, exp, cause);

			int inc = exp - this.exp;
			int upLevel = HorseUtil.getUpLevel(level, exp);
			addIntPropertyChangedItem(p.changed,ChangedItem.HORSE_GAINEXP, exp - this.exp,
					true, false);
			if (upLevel > 0) {
				int lvl1 = level;
				int exp1 = exp;
				int newLevel = level + upLevel;
				exp -= HorseUtil.getUpLevelExp(level, newLevel);
				this.exp = exp;
				addIntPropertyChangedItem(p.changed,ChangedItem.HORSE_EXP, this.exp, false,
						true);
				setLevel(newLevel, p);
//				setPoint(this.point + upLevel,p);
				int upExp = HorseUtil
						.getUpLevelExp(this.level, this.level + 1);
				addIntPropertyChangedItem(p.changed,ChangedItem.HORSE_UPLEVELEXP, upExp, false,
						true);
				refreshProperties(true,p);
				p.refreshProperties(false);
				notifyHorseUpLevel(lvl1,p);
				
				// 记录升级日志
				LogUtil.logHorseLevelUp(p, this, lvl1, exp1, this.level, this.exp);
			} else {
				this.exp = exp;
				addIntPropertyChangedItem(p.changed,ChangedItem.HORSE_EXP, this.exp, false,
						true);
			}
		}
	}
	
	public void notifyHorseUpLevel(int oldLevel,Player player){
		Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_HORSE_LEVELUP, player,oldLevel));
	}
	
	public void setStrength(int strength,Player p){
		if(this.strength!=strength){
			this.strength = strength;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_STRENGTH, this.strength, false);
			}
		}
	}
	
	public void setDegree(int degree,Player p){
		if(this.degree!=degree){
			this.degree = degree;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_DEGREE, this.degree, false);
			}
		}
	}
	
	public void setAgility(int agility,Player p){
		if(this.agility!=agility){
			this.agility = agility;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_AGILITY, this.agility, false);
			}
		}
	}
	
	public void setStamina(int stamina,Player p){
		if(this.stamina!=stamina){
			this.stamina = stamina;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_STAMINA, this.stamina, false);
			}
		}
	}
	public void setIntellect(int intellect,Player p){
		if(this.intellect!=intellect){
			this.intellect = intellect;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_INTELLECT, this.intellect, false);
			}
		}
	}
	
	public void setSpeed(int speed,Player p){
		if(this.speed!=speed){
			this.speed = speed;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_SPEED, this.speed, false);
			}
		}
	}
	
//	public void setPoint(int point,Player p){
//		if(this.point!=point){
//			this.point = point;
//			if(p!=null){
//				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_POINT, this.speed, false);
//			}
//		}
//	}
	
	public void setLevel(int level,Player p){
		if(this.level!=level){
			this.level = level;
			if(p!=null){
				addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_LEVEL, this.level, false, true);
			}
			int skillLen = this.level / 15 + 1;
			skillLen = Math.min(skillLen, this.skillSize);
			if(skills.size()<skillLen){
				int currentSkillSize = skills.size();
				for(int i=0;i<skillLen-currentSkillSize;i++){
					int[] s = new int[skills.size()];
					for(int j=0;j<s.length;j++){
						s[j] = skills.get(j).getGroupId();
					}
					Skill skill = HorseUtil.getSkill(0, s);
					if(skill!=null){
						addSkill(skill,p);
					}
				}
			}
		}
	}
	
	public void setActive(){
		state = 1; 
	}
	
	public void unActive(){
		state = 0;
	}
	/**是否可骑乘**/
	public boolean isActive(){
		if(state == 1 || state == 2){
			return true;
		}else{
			return false;
		}
	}
	
	public void refreshProperties(boolean levelUp,Player p){
		PropertyCalculator calc = new PropertyCalculator(this);
		if(p!=null && p.buffs!=null)
			p.processTitleBuff(calc);
		equs.enhance(calc, true);
		setStrength(calc.strength,p);
		setAgility(calc.agility,p);
		setStamina(calc.stamina,p);
		setIntellect(calc.intellect,p);
		setSpeed(calc.speed,p);
	}
	
	public void refreshPropertiesExcepPlayer(boolean levelUp,Player p){
		PropertyCalculator calc = new PropertyCalculator(this);
		equs.enhance(calc, true);
		setStrength(calc.strength,p);
		setAgility(calc.agility,p);
		setStamina(calc.stamina,p);
		setIntellect(calc.intellect,p);
		setSpeed(calc.speed,p);
	}
	
	public void food(int itemId, Player p, int serial) {
		if (itemId == -1) {
			foodId = -1;
		} else {
			ItemTemplate t = ObjectAccessor.getItemTemplate(itemId);
			if (t != null) {
				foodId = itemId;
			}
		}
		if (p != null) {
			Packet pt = new Packet(OpCode.HORSE_FOOD_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	public void changeName(String name, Player p, int serial) {
		this.name = name;
		if (p != null) {
			String itemName = "";
			try {
				itemName = ObjectAccessor.getItemTemplate(itemId).name;
			} catch (Exception e) {
			}
			String comName = itemName + "\t" + name;
			addStringPropertyChangedItem(p.changed, ChangedItem.HORSE_NAME, comName+getLeavingTime(), false);
			Packet pt = new Packet(OpCode.HORSE_CHANGENAME_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	public void feed(GameItem item,Player p,int serial){
		int v = 50;
		if(item.template.id==ItemUtil.ITEM_HORSEFOOD_ADDBUFF){
			v = 70;
		}
		int degree = this.degree + v;
		setDegree(Math.min(degree, MAXDEGREE),p);
		if(item.template.id==ItemUtil.ITEM_HORSEFOOD_ADDBUFF){
			p.buffs.addBuff(BuffUtil.createBuff(164, 1, p, p, 0));
		}
		if(p!=null){
			Packet pt = new Packet(OpCode.HORSE_FEED_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}
	
	public void ride(Unit u){
		for(Skill s:skills){
			u.buffs.addBuff(s.newBuff());
		}
//		if(foodId==1242){
//			if(u.type==GameObject.TYPE_PLAYER){
//				if(((Player)u).bag.getGameItem(ItemUtil.ITEM_HORSEFOOD_ADDBUFF)!=null){
//					u.buffs.addBuff(BuffUtil.createBuff(164, 1, u, u, 0));
//				}
//			}
//		}
		lastDecDegreeTime = Time.currTime;
		u.refreshProperties(false);
	}
	
	public void unRide(Unit u){
		for(Skill s:skills){
			u.buffs.removeBuff(s.getId());
		}
		if(u instanceof Player)
			((Player)u).removeSuiteEquipmentBuffs(equs.equs);
//		u.buffs.removeBuff(164);
		lastDecDegreeTime = 0;
		u.refreshProperties(false);
		if(u instanceof Player)
			refreshPropertiesExcepPlayer(false, (Player)u);
	}
	
	public int getRepairMoney(){
		int ret = 0;
		for(int i=0;i<equs.equs.length;i++){
			GameItem item = equs.equs[i];
			if(item!=null){
				ret += item.getRepairMoney();
			}
		}
		return ret;
	}

	public void repair(Player owner){
		boolean needRefresh = false;
		for (int i = 0; i < equs.equs.length; i++) {
			GameItem item = equs.equs[i];
			if (item != null) {
			    if (item.duration == 0) {
			        needRefresh = true;
			    }
			    item.repair(owner);
			}
		}
		if (needRefresh&&owner.horse==this) {
			owner.refreshProperties(false);
		}
	}
	
	public static int getFreeTime(int horseId){
		int index = indexOfFreeHorse(horseId);
		if(index!=-1)
			return freeTimes[index];
		return 0;
	}
	
	private static int indexOfFreeHorse(int horseId){
		for(int i=0;i<freeHorseItemIds.length;i++){
			if(freeHorseItemIds[i]==horseId)
				return i;
		}
		return -1;
	}
	
	public void addStringPropertyChangedItem(Changed changed,int id,String value,boolean notify){
		if(changed!=null){
			HorseStringPropertyChangedItem sc = new HorseStringPropertyChangedItem(this,id,value,notify);
			changed.addChangedItem(sc);
		}
	}

	public void addIntPropertyChangedItem(Changed changed,int id, int oldValue, int newValue,
			boolean notify) {
		if (changed != null) {
			HorseIntPropertyChangedItem ic = new HorseIntPropertyChangedItem(this,id,
					newValue, false, true);
			changed.addChangedItem(ic);
			if (notify) {
				HorseIntPropertyChangedItem oic = new HorseIntPropertyChangedItem(this,id,
						newValue - oldValue, true);
				changed.addChangedItem(oic);
			}
		}
	}
	
	public void addIntPropertyChangedItem(Changed changed,int id,int value,boolean notify,boolean overwrite){
		if (changed != null) {
			HorseIntPropertyChangedItem ic = new HorseIntPropertyChangedItem(this,id, value,
					false, overwrite);
			changed.addChangedItem(ic);
			if (notify) {
				HorseIntPropertyChangedItem oic = new HorseIntPropertyChangedItem(this,id,
						value, true, overwrite);
				changed.addChangedItem(oic);
			}
		}
	}
	
	//有些属性是不需要知道原来的值的，比如sex，clazz
	public void addIntPropertyChangedItem(Changed changed,int id,int value,boolean notify){
		if (changed != null) {
			if (notify) {
				HorseIntPropertyChangedItem oic = new HorseIntPropertyChangedItem(this,id,
						value, true);
				changed.addChangedItem(oic);
			} else {
				HorseIntPropertyChangedItem ic = new HorseIntPropertyChangedItem(this,id, value,
						false);
				changed.addChangedItem(ic);
			}
		}
	}
	

	public static Horse fromDBBytes(DataInputStream dis){
		try{
			int version = dis.read();//version;
			Horse h = null;
			if(version==1){
				int horseType = dis.readInt();
				int itemId = dis.readInt();
				int instanceId = dis.readInt();
				h = new Horse(ObjectAccessor.getHorseType(horseType),instanceId);
				h.name = dis.readUTF();
				h.itemId = itemId;
				h.level = dis.read();
				h.exp = dis.readInt();
				h.point = dis.readShort();
				h.degree = dis.readShort();
				h.strengthAdded = dis.readShort();
				h.agilityAdded = dis.readShort();
				h.intellectAdded = dis.readShort();
				h.staminaAdded = dis.readShort();
				h.speedAdded = dis.readShort();
				h.imageId = dis.readShort();
				h.iconId = dis.readShort();
				h.initLevel = dis.read();
				h.foodId = dis.readInt();
				h.skillSize = dis.read();
				int size = dis.read();
				for(int i=0;i<size;i++){
					Skill skill = ObjectAccessor.getSkill(dis.readInt());
					if(!h.skills.contains(skill))
						h.skills.add(skill);
				}
				h.equs = ItemUtil.getHorseEquipmentsFromDB(dis, h);
				h.agentHorse = 0;
				h.agentTime = 0L;
				h.leavingExp = 0;
				h.lockSkillId = 0;
				h.setActive();
			}else if(version==2){
				int horseType = dis.readInt();
				int itemId = dis.readInt();
				int instanceId = dis.readInt();
				h = new Horse(ObjectAccessor.getHorseType(horseType),instanceId);
				h.name = dis.readUTF();
				h.itemId = itemId;
				h.level = dis.read();
				h.exp = dis.readInt();
				h.point = dis.readShort();
				h.degree = dis.readShort();
				h.strengthAdded = dis.readShort();
				h.agilityAdded = dis.readShort();
				h.intellectAdded = dis.readShort();
				h.staminaAdded = dis.readShort();
				h.speedAdded = dis.readShort();
				h.imageId = dis.readShort();
				h.iconId = dis.readShort();
				h.initLevel = dis.read();
				h.foodId = dis.readInt();
				h.skillSize = dis.read();
				int size = dis.read();
				for(int i=0;i<size;i++){
					Skill skill = ObjectAccessor.getSkill(dis.readInt());
					if(!h.skills.contains(skill))
						h.skills.add(skill);
				}
				h.equs = ItemUtil.getHorseEquipmentsFromDB(dis, h);
				h.agentHorse = dis.readByte();
				h.agentTime = dis.readLong();
				h.leavingExp = dis.readInt();
				h.lockSkillId = dis.readInt();
				h.setActive();
			}else if(version==3){
				int horseType = dis.readInt();
				int itemId = dis.readInt();
				int instanceId = dis.readInt();
				h = new Horse(ObjectAccessor.getHorseType(horseType),instanceId);
				h.name = dis.readUTF();
				h.itemId = itemId;
				h.level = dis.read();
				h.exp = dis.readInt();
				h.point = dis.readShort();
				h.degree = dis.readShort();
				h.strengthAdded = dis.readShort();
				h.agilityAdded = dis.readShort();
				h.intellectAdded = dis.readShort();
				h.staminaAdded = dis.readShort();
				h.speedAdded = dis.readShort();
				h.imageId = dis.readShort();
				h.iconId = dis.readShort();
				h.initLevel = dis.read();
				h.foodId = dis.readInt();
				h.skillSize = dis.read();
				int size = dis.read();
				for(int i=0;i<size;i++){
					Skill skill = ObjectAccessor.getSkill(dis.readInt());
					if(!h.skills.contains(skill))
						h.skills.add(skill);
				}
				h.equs = ItemUtil.getHorseEquipmentsFromDB(dis, h);
				h.agentHorse = dis.readByte();
				h.agentTime = dis.readLong();
				h.leavingExp = dis.readInt();
				h.lockSkillId = dis.readInt();
				h.freeHorseEndTime = dis.readLong();
				h.state = dis.readByte();
				if(h.freeHorseEndTime > 0 && System.currentTimeMillis() < h.freeHorseEndTime ){
					h.state = 2;
				}else if(h.freeHorseEndTime > 0 && System.currentTimeMillis() > h.freeHorseEndTime ){
					//由于数据溢出导致的状态改变
					h.state = 0;
				}
				if(h.freeHorseEndTime==0){
					h.setActive();
				}
			}
			h.refreshProperties(false, null);
			return h;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(3);//version
			dos.writeInt(template.id);
			dos.writeInt(itemId);
			dos.writeInt(instanceId);
			dos.writeUTF(name);
			dos.write(level);
			dos.writeInt(exp);
			dos.writeShort(point);
			dos.writeShort(degree);
			dos.writeShort(strengthAdded);
			dos.writeShort(agilityAdded);
			dos.writeShort(intellectAdded);
			dos.writeShort(staminaAdded);
			dos.writeShort(speedAdded);
			dos.writeShort(imageId); //imageId
			dos.writeShort(iconId);
			dos.write(initLevel);
			dos.writeInt(foodId);
			dos.write(skillSize);
			dos.write(skills.size());
			for(Skill skill:skills){
				dos.writeInt(skill.getId());
			}
			dos.write(ItemUtil.getHorseEquipmentsDBBytes(equs));
			dos.writeByte(agentHorse);
			dos.writeLong(agentTime);
			dos.writeInt(leavingExp);
			dos.writeInt(lockSkillId);
			dos.writeLong(freeHorseEndTime);
			dos.writeByte(state);
		}catch(Exception e){
			e.printStackTrace();
		}
		return baos.toByteArray();
		
	}

	public byte[] toClientBytes(Unit owner){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeInt(instanceId);
			String itemName = "";
			try {
				itemName = ObjectAccessor.getItemTemplate(itemId).name;
			} catch (Exception e) {
			}
			dos.writeUTF(itemName + "\t" + name + getLeavingTime());
			dos.write(level);
			dos.writeInt(exp);
			dos.writeInt(HorseUtil.getUpLevelExp(level, level+1));
			dos.writeShort(point);
			dos.writeShort(MAXDEGREE);
			dos.writeShort(degree);
			dos.writeInt(template.summonTime);
			dos.writeShort(strength);
			dos.writeShort(agility);
			dos.writeShort(intellect);
			dos.writeShort(stamina);
			dos.writeShort(speed);
			dos.writeShort(score);
			dos.writeShort(imageId); //imageId
			dos.writeShort(iconId);
			dos.write(skillSize);
			dos.write(skills.size());
			for(Skill skill:skills){
				dos.write(skill.toClientBytes(owner));
			}
			dos.write(equs.toClientBytes());
			dos.writeBoolean((foodId == -1 || foodId == 0)?false:true);
			dos.writeUTF(template.showName);
			dos.write((agentHorse<<7)|lockSkillId);
			dos.write(state);
		}catch(Exception e){
			
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	public String getDesc() {
		StringBuilder sb = new StringBuilder();
  		sb.append("名称:");
  		sb.append(name);
		sb.append("\n");
		sb.append("等级:");
		sb.append(level);
		sb.append("\n");
		sb.append("类型:");
		sb.append(template.showName);
		sb.append("\n");
		sb.append("<c008242>");
		int count = 0;
		if(strength > 0){
			sb.append("力 +");
			sb.append(strength);
			count++;
			if(count % 2 == 0)
				sb.append("\n");
			else
				sb.append(" ");
		}
		
		if(intellect > 0){
			sb.append("智 +");
			sb.append(intellect);
			count++;
			if(count % 2 == 0)
				sb.append("\n");
			else
				sb.append(" ");
		}
		if(agility > 0){
			sb.append("敏 +");
			sb.append(agility);
			count++;
			if(count % 2 == 0)
				sb.append("\n");
			else
				sb.append(" ");
		}
		if(stamina > 0){
			sb.append("体 +");
			sb.append(stamina);
			count++;
			if(count % 2 == 0)
				sb.append("\n");
			else
				sb.append(" ");
		}
		if(speed > 0){
			sb.append("速 +");
			sb.append(speed);
			count++;
			if(count % 2 == 0)
				sb.append("\n");
			else
				sb.append(" ");
		}
		if(count % 2 == 1)
			sb.append("\n");
		sb.append("</c>");
		sb.append("<c9370DB>");
		for (int i=0;i<skills.size();i++) {
			if(i == 0){
				sb.append("技能:\n");
			}
			Skill skill = skills.get(i);
			sb.append(skill.getName()+" ");
			sb.append(skill.getLevel());
			sb.append("级 ");
			sb.append("\n");
		}
		sb.append("</c>");
		return sb.toString();
	}
	
	public String logString() {
		StringBuilder sb = new StringBuilder(200);
		sb.append("[TEMPLATE[").append(template.id).append("]ITEMID[").append(
				itemId).append("]NAME[").append(name).append("]INSTANCE[")
				.append(instanceId).append("]LEVEL[").append(level).append("]SKILLS[");
		for(Skill skill:skills){
			sb.append(skill.getId()).append(",");
		}
		sb.append("]]");
		return sb.toString();
	}

	public Class<? extends Marshaller> marshallerClass() {
		return HorsePersistence.class;
	}

	public Class<? extends Serializer> serializerClass() {
		return HorsePersistence.class;
	}

	/**
	 * 把对象添加到一个日志字符串中。
	 */
	public void dump(StringBuilder out) {
		out.append("TID=").append(template.id).append(",NM=").append(LogUtil.filter(name));
		out.append(",IID=").append(itemId).append(",LVL=").append(level);
		out.append(",SKLS=");
		for (int i = 0; i < skills.size(); i++) {
			if (i > 0) {
				out.append('+');
			}
			out.append(skills.get(i).getId());
		}
	}
}
