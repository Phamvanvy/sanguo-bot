package peony.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

import peony.game.buff.Buff;
import peony.game.buff.Buffs;
import peony.game.changed.ChangedItem;
import peony.game.changed.IntPropertyChangedItem;
import peony.game.changed.StringPropertyChangedItem;
import peony.game.pk.PkInfo;
import peony.game.skill.Skill;
import peony.net.Packet;
import peony.service.ServiceEvent;

@MappedSuperclass
@AccessType("field")
abstract public class Unit extends GameObject {
	
	private static final Logger log = Logger.getLogger(Unit.class);
//	初始物理攻击力	30	32	15	21
//	初始物理防御力	10	6	5	7
//	初始生命值	100	82	69	82
//	初始魔法值	34	46	71	55
//	初始魔法攻击力	0	0	4	3
//	初始魔法防御力	5	6	10	8
//	初始命中率	93%	98%	90%	90%
//	初始闪避率	%	10%	5%	5%
//	初始物理暴击率	1.5%	2.7%	1%	1%
//	初始魔法暴击率	0	0	3%	2.6%

	protected static final Random RND = new Random();
	
	public static final byte DIRECT_UP = 3;
	public static final byte DIRECT_DOWN = 0;
	public static final byte DIRECT_LEFT = 2;
	public static final byte DIRECT_RIGHT = 1;
	
	public static final int CLASS_1 = 0;  //武将
	public static final int CLASS_2 = 1;  //刺客
	public static final int CLASS_3 = 2;  //谋士
	public static final int CLASS_4 = 3;  //方士
	
	public static final String[] CLASS_NAME = {"武將","刺客","謀士","方士"};
	
	@Column(name="hp",nullable=false)
	public int hp;
	@Column(name="mp",nullable=false)
	public int mp;
	@Transient
	public byte direct;
	@Column(name="maxhp",nullable=false)
	public int maxhp;
	@Column(name="maxmp",nullable=false)
	public int maxmp;
	@Transient
	public int agility;
	@Transient
	public int strength;
	@Transient
	public int intellect;
	@Transient
	public int stamina;
	@Transient
	public int healthrestore;
	@Transient
	public int manarestore;
	@Transient
	public int lastRestoreTime;
	
	@Column(name="sex",nullable=false)
	public int sex;
	
	@Column(name="clazz",nullable=false)
	public int clazz;
	

	

	
	@Transient
	public float attackpowerup,attackpowerdown;
	@Transient
	public float spellpower;
	@Transient
	public float spellheal;
	@Transient
	public float defense;
	@Transient
	public float spelldefense;
	@Transient
	public float critical;
	@Transient
	public float spellcritical;
	@Transient
	public float hit;
	@Transient
	public float spellhit;
	@Transient
	public float dodge;
    @Transient
    public float spelldodge;
    @Transient
    public float anticrit;
    @Transient
    public float defensePercent;
	
	@Column(name="strength",nullable=false) //力量的额外加点
	public int strengthAdded;
	@Column(name="agility",nullable=false)
	public int agilityAdded;
	@Column(name="stamina",nullable=false)
	public int staminaAdded;
	@Column(name="intellect",nullable=false)
	public int intellectAdded;
	
	private static final String SEX_MALE = "男";
	private static final String SEX_FEMALE = "女";
	private static final String SEX_SHEMALE = "人妖";
	
	@Transient
	public int lastMoveTime;
	@Transient
	public int lastAttackTime;
	
	@Transient
	public Attack attack;
	@Transient
	public Changed changed;
	@Column(name="buffs")
	@Type(type="peony.game.BuffsUserType")
	public Buffs buffs;
	@Transient
	public ThreatList threats;
	
	@Column(name="equipments")
	@Type(type="peony.game.EquipmentsUserType")
	public Equipments equipments;
	
	@Transient
	public int head_score = 0; //头部的评分，实际就是头部的ImageId
	
	@Transient
	public int body_score = 0; //身体的评分，实际就是身体的ImageId

	@Transient
	public int weapon_score = 0; //武器的评分，前16位是武器的部位，后16位是武器的ImageId，第一位表明是否是有柄

	@Transient
	public int flashLevel = 0; //发光等级，0表示不发光，1~5表示从低到高的发光等级
	
	@Column(name="cooldowns")
	@Type(type="peony.game.CoolDownListUserType")
	public CoolDownList coolDowns;
	
	@Column(name="credit",nullable=false)
	protected int credit;          // 战功
	
	@Column(name="weekcredit",nullable=false)
	@AccessType("property")
	protected int weekCredit;
	
	@Column(name="rank",nullable=false)
	@AccessType("property")
	protected int rank;
	
	@Transient
	protected String creditString;
	
	@Transient
	protected float speedRating = 1.0f;

    @Transient
    public float expRatio = 1.0f;
    
    @Transient
    public float horseExpRatio = 1.0f;
    
    @Transient
    public float moneyRatio = 1.0f;
	
	@Transient
	public Horse horse; //当前装配的马
	
	@Transient
	protected DieInfo dieInfo;
	
	@Transient
	protected List<TempThreat> tempThreats;
	
	@Column(name="skills")
	@Type(type="peony.game.SkillsUserType")
	public Skills skills;
	
	@Transient
	public int minorFaction;
	
	protected Unit(byte type){
		super(type);
		skills = new Skills(Skills.DEFAULT_BOOKSKILL_SIZE);
		equipments = new Equipments(this);
		buffs = new Buffs(this);
		threats = new ThreatList();
		coolDowns = new CoolDownList();
		tempThreats = new ArrayList<TempThreat>();
	}
	
	public void setAttackState(boolean value){
//		if (type == GameObject.TYPE_PLAYER)
//			log.info("setattack:" + value);
		boolean oldValue = (state&STATE_ATTACK)!=0;
		if(oldValue^value){
			if(value){
				state |= STATE_ATTACK;
			}else{
				state &= ~STATE_ATTACK;
			}
//			if(type==GameObject.TYPE_PLAYER)
//				log.info("state:"+((state&STATE_ATTACK)!=0));
			addIntPropertyChangedItem(ChangedItem.STATE, state, false,true);
		}
	}
	
	public boolean isAttack(){
		return (state&STATE_ATTACK) !=0;
	}
	
	public void setAttack(Attack attack) {
		this.attack = attack;
		this.lastAttackTime = Time.currTime;
		if (attack != null) {
			Packet pt = new Packet(OpCode.SKILL_PREPARE_ATTACK_SERVER);
			pt.putInt(instanceId);
			pt.putInt(attack.targetRef==null?-1:attack.targetRef.instanceId);
			pt.putInt(attack.skill.getPrepareAnimation(this));
			Player source = null;
			if(attack.sourceRef.type==GameObject.TYPE_PLAYER){
				source = ObjectAccessor.getPlayer(attack.sourceRef.id);
			}
			Player target = null;
			if(attack.targetRef!=null&&attack.targetRef.type==GameObject.TYPE_PLAYER){
				target = ObjectAccessor.getPlayer(attack.targetRef.id);
			}
			broadcast(pt,source,target,true,false,false);
//			mapCell.broadcastWithRelationCells(null, pt);
		}
	}
	
	
	@Override
	public boolean isAlive(){
		boolean isAlive = super.isAlive();
		if(isAlive){
			return dieInfo==null;
		}
		return isAlive;
	}
	
	public void processDie(){
		if(dieInfo!=null){
		    if (dieInfo.source == null) {
		        realDie(null);
		    } else {
    			Unit source = (Unit)ObjectAccessor.getGameObject(dieInfo.source);
    			realDie(source);
		    }
		}
	}
	
	public void die(Unit source){
		if (source == null) {
			dieInfo = new DieInfo(null);
		} else {
			dieInfo = new DieInfo(source.ref());
		}
//		if ((state & STATE_DIE) != 0) {
//			return;
//		}
//		// 清除所有关联仇恨对象对自己的仇恨
//		clearThreats();
//		// 清除BUFF
//		buffs.clear();
//		
//		setAttack(null);
//		state &= MASK_CLEAR;
//		state |= STATE_DIE;
//		// 发送事件
//		ServiceEvent evt = new ServiceEvent(ServiceEvent.EVENT_UNIT_DIE, this, source);
//		Server.server.getEventManager().fireEvent(evt);
	}
	
	protected void realDie(Unit source){
		dieInfo = null;
		if ((state & STATE_DIE) != 0) {
			return;
		}
		// 清除所有关联仇恨对象对自己的仇恨
		clearThreats();
		// 清除BUFF
		buffs.clear();
		
		setAttack(null);
		state &= MASK_CLEAR;
		state |= STATE_DIE;
		
		// 发送事件
		if (source != null) {
    		ServiceEvent evt = new ServiceEvent(ServiceEvent.EVENT_UNIT_DIE, this, source);
    		Server.server.getEventManager().fireEvent(evt);
		}
	}
	

	
	public boolean isRunning(){
		return (state&STATE_RUN) != 0;
	}
	
	
	public boolean isPvp(){
		return (state&STATE_PVP) != 0;
	}
	
	public boolean isPvpFaction(){
		return (state&STATE_PVP_FACTION) != 0;
	}
	
	public void idle(){
		stop();
		state &= MASK_CLEAR;
		state |= STATE_IDLE;
	}
	
	public void stop() {
		if (isRunning()) {
			state &= (~STATE_RUN);
			lastMoveTime = Time.currTime;
		}
	}
	
	public void dumb(){
		state |= STATE_DUMB;
	}
	
	public void unDumb(){
		state &= (~STATE_DUMB);
	}
	
	public void fear(){
		state |= STATE_FEAR;
	}
	
	public void unFear(){
		state &= (~STATE_FEAR);
	}
	
	public void paralyze(){
		state |= STATE_PARALYZE;
	}
	
	public void unParalyze(){
		state &= (~STATE_PARALYZE);
	}
	
	public void stay(){
		state |= STATE_STAY;
	}
	
	public void unStay(){
		state &= (~STATE_STAY);
	}
	
	public void pvp(){
		state |= STATE_PVP;
	}
	
	public void unPvp(){
		state &= (~STATE_PVP);
	}
	
	public void pvpFaction(){
		state |= STATE_PVP_FACTION;
	}
	
	public void unPvpFaction(){
		state &= (~STATE_PVP_FACTION);
	}
	
	public boolean breakAttack(){
		if(attack!=null){
			attack = null;
			return true;
		}
		return false;
	}
	
	public void go(){
		state |= STATE_RUN;
		lastMoveTime = Time.currTime;
	}
	
	public void setSpeedRatio(float rating) {
		if (this.speedRating != rating) {
			this.speedRating = rating;
			addIntPropertyChangedItem(ChangedItem.SPEED, getSpeed(), false, true);
//			log.info("speed:"+getSpeed());
			moveType |= MOVE_RUNNING;
		}
	}
	
	public CoolDown setCoolDown(int id,int startTime,int time){
		return coolDowns.setCoolDown(id, startTime, time);
	}
	
//	/**
//	 * 返回距离多少像素之内的Unit
//	 * @param dist
//	 * @return
//	 */
//	public Unit[] getUnits(int dist){
//		return map.getUnits(this, dist);
//	}
	
	/**
	 * 取得指定对象周围指定范围内允许被攻击的对象。最多可以攻击6个对象，在这6个对象中，
	 * 当前目标优先，其他随机选。
	 */
	public Unit[] getEnemies(GameObject ref, int dist){
	    Set<Unit> candidates = new HashSet<Unit>();

	    // 把选中目标加到队列中
	    if (canAttack(ref) == 0) {
	        candidates.add((Unit)ref);
	    }
        
        map.getEnemies(this, ref, dist, 6 - candidates.size(), candidates);
        Unit[] ret = new Unit[candidates.size()];
        candidates.toArray(ret);
        return ret;
	}
	
	/**
	 * 取得指定对象周围指定范围内允许被治疗的对象。最多可以治疗6个对象，在这6个对象中，
	 * 当前目标优先，其他随便选。
	 */
	public Unit[] getAidUnits(GameObject ref, int dist) {
	    Set<Unit> candidates = new HashSet<Unit>();
	    
	    // 把选中目标加到队列中
	    if (canAid(ref)) {
	        candidates.add((Unit)ref);
	    }
	    
	    // 其他目标在周围随机选取
	    if (candidates.size() < 6) {
	        map.getAidUnits(this, ref, dist, 6 - candidates.size(), candidates);
	    }
	    
        Unit[] ret = new Unit[candidates.size()];
        candidates.toArray(ret);
        return ret;
	}
	
	public void goMap(int mapId, int x, int y) throws VMapException{
	}
	
	public  boolean hasAttack(){
		return attack!=null;
	}
	
	public String getSexName(){
		if(sex==0)
			return SEX_MALE;
		else if(sex==1)
			return SEX_FEMALE;
		return SEX_SHEMALE;
	}
	
	public String getClassName(){
		return CLASS_NAME[clazz];
	}
	
	public void addStringPropertyChangedItem(int id,String value,boolean notify){
		if(changed!=null){
			StringPropertyChangedItem sc = new StringPropertyChangedItem(id,value,notify);
			changed.addChangedItem(sc);
		}
	}

	public void addIntPropertyChangedItem(int id, int oldValue, int newValue,
			boolean notify) {
		if (changed != null) {
			IntPropertyChangedItem ic = new IntPropertyChangedItem(id,
					newValue, false, true);
			changed.addChangedItem(ic);
			if (notify) {
				IntPropertyChangedItem oic = new IntPropertyChangedItem(id,
						newValue - oldValue, true);
				changed.addChangedItem(oic);
			}
		}
	}
	
	public void addIntPropertyChangedItem(int id,int value,boolean notify,boolean overwrite){
		if (changed != null) {
			IntPropertyChangedItem ic = new IntPropertyChangedItem(id, value,
					false, overwrite);
			changed.addChangedItem(ic);
			if (notify) {
				IntPropertyChangedItem oic = new IntPropertyChangedItem(id,
						value, true, overwrite);
				changed.addChangedItem(oic);
			}
		}
	}
	
	//有些属性是不需要知道原来的值的，比如sex，clazz
	public void addIntPropertyChangedItem(int id,int value,boolean notify){
		if (changed != null) {
			IntPropertyChangedItem ic = new IntPropertyChangedItem(id, value,
					false);
			changed.addChangedItem(ic);
			if (notify) {
				IntPropertyChangedItem oic = new IntPropertyChangedItem(id,
						value, true);
				changed.addChangedItem(oic);
			}
		}
	}
	

	
	public void setCredit(int credit, String cause) {
		if (this.credit != credit) {
			int oldCredit = this.credit;
			this.credit = credit;
			if (type == TYPE_PLAYER) {
				if (this.credit > oldCredit) {
					LogUtil.logGetCredit((Player)this, oldCredit, this.credit, cause);
				} else {
					LogUtil.logRemoveCredit((Player)this, oldCredit, this.credit, cause);
				}
			}
		}
	}
	
	public int getCredit(){
		return credit;
	}
	
	public void setWeekCredit(int credit) {
	    weekCredit = credit;
	}
	
	public int getWeekCredit() {
	    return weekCredit;
	}
	
	public void setRank(int rank) {
	    this.rank = rank;
	    creditString = CreditUtil.getCreditString(rank);
	}
	
	public int getRank() {
	    return rank;
	}
	
	public String getCreditString(){
		return creditString==null?"":creditString;
	}
	
	public void setCredit(int credit,boolean notify, String cause){
//		String oldCreditString = this.creditString;
		int oldCredit = this.credit;
		setCredit(credit, cause);
		if(this.credit!=oldCredit){
			addIntPropertyChangedItem(ChangedItem.CREDIT,oldCredit,this.credit,notify);
		}
//		if(!creditString.equals(oldCreditString)){
//			moveExtended |= MOVEEXT_CREDIT;
//			addStringPropertyChangedItem(ChangedItem.CREDIT_STRING, creditString, notify);
//		}
		
		// 如果荣誉变化是正，则还需要修改本周累计荣誉字段
		if (this.credit > oldCredit) {
		    this.weekCredit += this.credit - oldCredit;
		}
	}
	
//	protected void setHp(int hp){
//		this.hp = hp;
//	}

	public void setHealthrestore(int value){
		if(this.healthrestore!=value){
			addIntPropertyChangedItem(ChangedItem.HEALTHRESTORE,value,false,true);
			this.healthrestore = value;
		}
	}
	
	public void setManarestore(int value){
		if(this.manarestore!=value){
			addIntPropertyChangedItem(ChangedItem.MANARESTORE,value,false,true);
			this.manarestore = value;
		}
	}
	
	public void setAttackpowerup(float value){
		if(this.attackpowerup!=value){
			int oldValue = Math.round(this.attackpowerup);
			this.attackpowerup = value;
			int newValue = Math.round(this.attackpowerup);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.ATTACKPOWERUP,newValue,false,true);
			}
		}
	}
	
	public void setAttackpowerdown(float value){
		if(this.attackpowerdown!=value){
			int oldValue = Math.round(this.attackpowerdown);
			this.attackpowerdown = value;
			int newValue = Math.round(this.attackpowerdown);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.ATTACKPOWERDOWN,newValue,false,true);
			}
		}
	}
	
	public void setSpellpower(float value){
		if(this.spellpower!=value){
			int oldValue = Math.round(this.spellpower);
			this.spellpower = value;
			int newValue = Math.round(this.spellpower);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.SPELLPOWER,newValue,false,true);
			}
		}
	}

    public void setSpellheal(float value){
        if(this.spellheal!=value){
            int oldValue = Math.round(this.spellheal);
            this.spellheal = value;
            int newValue = Math.round(this.spellheal);
            if(oldValue!=newValue){
                addIntPropertyChangedItem(ChangedItem.SPELLHEAL,newValue,false,true);
            }
        }
    }
	
	public void setDefense(float value){
		if(this.defense!=value){
			int oldValue = Math.round(this.defense);
			this.defense = value;
			int newValue = Math.round(this.defense);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.DEFENSE,newValue,false,true);
			}
		}
	}
	
	public void setSpelldefense(float value){
		if(this.spelldefense!=value){
			int oldValue = Math.round(this.spelldefense);
			this.spelldefense = value;
			int newValue = Math.round(this.spelldefense);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.SPELLDEFENSE,newValue,false,true);
			}
		}
	}
	
	public void setCritical(float value){
		if(this.critical!=value){
			int oldValue = Math.round(this.critical*100);
			this.critical = value;
			int newValue = Math.round(this.critical*100);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.CRITICAL,newValue,false,true);
			}
		}
	}
	
	public void setSpellCritical(float value){
		if(this.spellcritical!=value){
			int oldValue = Math.round(this.spellcritical*100);
			this.spellcritical = value;
			int newValue = Math.round(this.spellcritical*100);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.SPELLCRITICAL,newValue,false,true);
			}
		}
	}
	
	public void setHit(float value){
		if(this.hit!=value){
			int oldValue = Math.round(this.hit*100);
			this.hit = value;
			int newValue = Math.round(this.hit*100);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.HIT,newValue,false,true);
			}
		}
	}
	
	public void setSpellhit(float value){
		if(this.spellhit!=value){
			int oldValue = Math.round(this.spellhit*100);
			this.spellhit = value;
			int newValue = Math.round(this.spellhit*100);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.SPELLHIT,newValue,false,true);
			}
		}
	}
	
	public void setDodge(float value){
		if(this.dodge!=value){
			int oldValue = Math.round(this.dodge*100);
			this.dodge = value;
			int newValue = Math.round(this.dodge*100);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.DODGE,newValue,false,true);
			}
		}
	}
	
	public void setSpelldodge(float value){
		if(this.spelldodge!=value){
			int oldValue = Math.round(this.spelldodge*100);
			this.spelldodge = value;
			int newValue = Math.round(this.spelldodge*100);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.SPELLDODGE,newValue,false,true);
			}
		}
	}
	
	public void setAnticrit(float value){
        if(this.anticrit!=value){
            int oldValue = Math.round(this.anticrit*100);
            this.anticrit = value;
            int newValue = Math.round(this.anticrit*100);
            if(oldValue!=newValue){
                addIntPropertyChangedItem(ChangedItem.ANTICRIT,newValue,false,true);
            }
        }
    }
	
	public void setDefensePercent(float value){
		if(this.defensePercent!=value){
			int oldValue = Math.round(this.defensePercent*100);
			this.defensePercent = value;
			int newValue = Math.round(this.defensePercent*100);
			if(oldValue!=newValue){
				addIntPropertyChangedItem(ChangedItem.DEFENSEPERCENT,newValue,false,true);
			}
		}
	}
	
	/**
	 * 返回修改前后的血量差值。
	 * @param hp
	 * @param notify
	 * @return
	 */
	public int setHp(int hp,boolean notify) {
		int oldHp = this.hp;
		hp = Math.min(maxhp, hp);
		if(hp<0) hp = 0;
		if(this.hp!=hp){
			int changedValue = hp - this.hp;
			addIntPropertyChangedItem(ChangedItem.HP,this.hp,hp,notify);
			this.hp = hp;
			moveType |= MOVE_HPMP;
			if(notify){
				if(changedValue!=0){
					broadcastHPMP(0,changedValue);
				}
			}
		}
		return this.hp - oldHp;
	}
	
	/**
	 * 
	 * @param type 0 hp  1 mp
	 * @param value
	 */
	protected void broadcastHPMP(int type,int value){
		byte damageType = 0;
		if(type==0){
			if(value>=0){
				damageType = CombatContext.DAMAGE_HEAL;
			}else{
				damageType = CombatContext.DAMAGE_MAGIC;
			}
		}
		else if(type==1){
			if(value>=0){
				damageType = CombatContext.DAMAGE_ADDMP;
			}else{
				damageType = CombatContext.DAMAGE_DECMP;
			}
		}
		Packet pt = new Packet(OpCode.SHOW_HPMP_SERVER);
		pt.putInt(instanceId);
		pt.put(damageType);
		pt.putInt(value);
		broadcast(pt, null,null, true,false,false);
	}
	



//	public int getHp() {
//		return hp;
//	}
//
//	protected void setMp(int mp){
//		this.mp = mp;
//	}

	public void setMp(int mp,boolean notify) {
		mp = Math.min(maxmp, mp);
		if (mp < 0) {
		    mp = 0;
		}
		if(this.mp!=mp){
			int changedValue = mp - this.mp;
			addIntPropertyChangedItem(ChangedItem.MP,this.mp,mp,notify);
			this.mp = mp;
			moveType |= MOVE_HPMP;
			if(notify){
				if(changedValue!=0){
					broadcastHPMP(1,changedValue);
				}
			}
		}
	}
	
	public void relive(int hp,int mp){
		setHp(hp,false);
		setMp(mp,false);
		state &= MASK_CLEAR;
		lastMoveTime = CommonUtil.currentMillis();
	}
	

//	public int getMp() {
//		return mp;
//	}


	public void setName(String name) {
		this.name = name;
		addStringPropertyChangedItem(ChangedItem.NAME, name, false);
		moveType |= MOVE_NAME|MOVE_DETAIL;
	}

	protected void processThreats(){
		if(threats.count>0){
			threats.update(this);
			if(threats.count==0){
				setAttackState(false);
			}
		}
		
		// 扫描是否有过期的临时威胁
		for (int i = 0; i < tempThreats.size(); i++) {
		    TempThreat tt = tempThreats.get(i);
		    if (tt.endTime < Time.currTime) {
		        // 到期，从对方威胁值表中扣除自己的威胁值
		        Unit target = (Unit)ObjectAccessor.getGameObject(tt.target);
		        if (target != null && target.isAlive() && target.threats.contains(ref())) {
		            target.threats.addUnit(ref(), -tt.value, false);
		        }
                tempThreats.remove(i);
                i--;
		    }
		}
	}

	public String getName() {
		return name;
	}

//	protected void setLevel(int level){
//		this.level = level;
//	}

	public void setLevel(int level,boolean notify) {
		if(this.level!=level){
//			log.debug(String.format("syncLevel:%d %d",this.level,level));
			addIntPropertyChangedItem(ChangedItem.LEVEL,this.level,level,notify);
			this.level = level;
//			addIntPropertyChangedItem(ChangedItem.UPLEVELEXP,0,CommonUtil.getUpLevelExp(this.level, this.level+1),false);
			moveType |= MOVE_LEVEL|MOVE_DETAIL;
		}
	}

	

	public void setMaxhp(int maxhp,boolean notify) {
		if(this.maxhp!=maxhp){
			addIntPropertyChangedItem(ChangedItem.MAXHP,this.maxhp,maxhp,notify);
			this.maxhp = maxhp;
			moveType |= MOVE_HPMP;
		}
	}


//	public int getMaxhp() {
//		return maxhp;
//	}
//
//	protected void setMaxmp(int maxmp){
//		this.maxmp = maxmp;
//	}

	public void setMaxmp(int maxmp,boolean notify) {
		if(this.maxmp!=maxmp){
			addIntPropertyChangedItem(ChangedItem.MAXMP,this.maxmp,maxmp,notify);
			this.maxmp = maxmp;
			moveType |= MOVE_HPMP;
		}
	}


//	public int getMaxmp() {
//		return maxmp;
//	}
//	
//	protected void setAgility(int agility){
//		this.agility = agility;
//	}

	public void setAgility(int agility,boolean notify) {
		if(this.agility!=agility){
			addIntPropertyChangedItem(ChangedItem.AGILITY,this.agility,agility,notify);
			this.agility = agility;
		}
	}


//	public int getAgility() {
//		return agility;
//	}
//
//	protected void setStrength(int strength){
//		this.strength = strength;
//	}

	public void setStrength(int strength,boolean notify) {
		if(this.strength!=strength){
			addIntPropertyChangedItem(ChangedItem.STRENGTH,this.strength,strength,notify);
			this.strength = strength;
		}
	}


//	public int getStrength() {
//		return strength;
//	}
//
//	protected void setIntelligence(int intelligence){
//		this.intelligence = intelligence;
//	}

	public void setIntellect(int intelligence,boolean notify) {
		if(this.intellect!=intelligence){
			addIntPropertyChangedItem(ChangedItem.INTELLECT,this.intellect,intelligence,notify);
			this.intellect = intelligence;
		}
	}


//	public int getIntelligence() {
//		return intelligence;
//	}
//
//	protected void setStamina(int stamina){
//		this.stamina = stamina;
//	}
	
	public void setStamina(int stamina,boolean notify) {
		if(this.stamina!=stamina){
			addIntPropertyChangedItem(ChangedItem.STAMINA,this.stamina,stamina,notify);
			this.stamina = stamina;
		}
	}


//	public int getStamina() {
//		return stamina;
//	}
//
//	protected void setSex(int sex){
//		this.sex = sex;
//	}
	
	public void setFaction(int faction,boolean notify){
		if(this.faction!=faction){
			addIntPropertyChangedItem(ChangedItem.FACTION,faction,notify);
			this.faction = faction;
			moveType |= MOVE_FACTION|MOVE_DETAIL;
		}
	}

	public void setSex(int sex,boolean notify) {
		if(this.sex!=sex){
			addIntPropertyChangedItem(ChangedItem.SEX,sex,notify);
			this.sex = sex;
			moveType |= MOVE_SEX|MOVE_DETAIL;
		}
	}


//	public int getSex() {
//		return sex;
//	}
//
//	protected void setClazz(int clazz){
//		this.clazz = clazz;
//	}

	public void setClazz(int clazz,boolean notify) {
		if(this.clazz!=clazz){
			addIntPropertyChangedItem(ChangedItem.CLAZZ,clazz,notify);
			this.clazz = clazz;
			moveType |= MOVE_CLAZZ|MOVE_DETAIL;
		}
	}
	
	public int prepareSkillAttack(Unit target,Skill skill,int offsetTime){
		int mana = skill.getMP(this);
		if(this.mp<mana)
			return 12;
		// 如果在骑乘状态，则只能使用马上允许使用的技能
		if ((this.state & GameObject.STATE_RIDE) != 0) {
			if ((skill.getType() & Skill.TYPE_RIDE_USE) == 0) {
				return 6;
			}
		}
		// 如果不是对自己使用的技能，则必须要选择一个目标
		if ((skill.getTargetType() & Skill.TARGET_FLAG_SELF) == 0) {
			if (target == null) {
				return 7;
			}
			
			// 目标必须存活，如果是复活技能则目标必须死亡
			if ((skill.getType() & Skill.TYPE_RELIVE) == 0) {
				if(!target.isAlive()){
					return 3;
				}
			} else {
				if (target.isAlive()) {
					return 9;
				}
			}
			
			// 目标必须在技能攻击范围内
			int distance = skill.getDistance(this);
			if(!inRange(target,(distance*2+40))){
				return 1;
			}
			
			// 判断目标是不是技能要求的目标类型
			if ((skill.getTargetType() & Skill.TARGET_FLAG_ATTACK) != 0) {
				int code = canAttack(target);
				if(code!=0)
					return code;
			} else {
				if (!canAid(target)) {
					return 8;
				}
			}
		} else {
			target = null;
		}
		setAttack(new Attack(skill,this,target,offsetTime));
		return 0;
	}
	
	/**
	 * 准备对一批目标施放攻击。注意这个方法只提供给怪物。此方法不再检测怪物是否允许
	 * 施放此技能。
	 * @param targets 选中的目标，可以多个
	 * @param skill 技能
	 * @return
	 */
	public int prepareSkillAttack(Unit[] targets, Skill skill) {
        // 如果是对自己使用的技能，则不能有目标
        if ((skill.getTargetType() & Skill.TARGET_FLAG_SELF) != 0) {
            targets = null;
        }
        if (targets == null) {
            setAttack(new Attack(skill,this,null,0));
        } else if (targets.length == 1) {
            setAttack(new Attack(skill,this,targets[0],0));
        } else {
            GameObjectRef[] refs = new GameObjectRef[targets.length];
            for (int i = 0; i < targets.length; i++) {
                refs[i] = targets[i].ref();
            }
            setAttack(new Attack(skill,this,refs));
        }
        return 0;
    }
	
	public void addSkillBuffs() {
		for (Skill skill : skills.getSkills()) {
			if (skill.getLevel() > 0) {
				Buff b = skill.newBuff();
				if (b != null)
					buffs.addBuff(b);
				b = skill.getAreaBuff();
				if (b != null)
					buffs.addBuff(b);
			}
		}
		for (Skill skill : skills.getBookSkills()){
			Buff b = skill.newBuff();
			if(b!=null){
				buffs.addBuff(b);
			}
		}
	}
	
	public int getNextPointX(){
		throw new UnsupportedOperationException();
	}
	
	public int getNextPointY(){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Packet getMovePacket(short moveType){
		Packet pt = new Packet(OpCode.UNIT_MOVE_SERVER);
		pt.put(type|moveType);
		pt.putInt(instanceId);
		if((moveType&MOVE_POINT)!=0){
			VMap vmap = getVMap();
			if(vmap==null){
				pt.putShort(map.id);
			}else{ //如果在副本里就发InstanceId
				int mapInstance = vmap.getInstanceId();
				if(mapInstance!=-1){
					pt.putShort(vmap.getId()|(1<<15));
					pt.putInt(mapInstance);
				}else{
					pt.putShort(vmap.getId());
				}
			}
			pt.putShort(x);
			pt.putShort(y);
		}
		if((moveType&MOVE_ANGLE)!=0){
			pt.put(getHalfAngle());
			pt.putInt(Time.currTime);
			pt.put(getSpeed());
			if(type!=GameObject.TYPE_PLAYER){
				pt.putShort(getNextPointX());
				pt.putShort(getNextPointY());
			}
		}
		if((moveType&MOVE_HPMP)!=0){
//			log.debug(String.format("send hp:%d mp:%d id:%d", hp*200/maxhp,mp*200/maxmp,id));
			pt.put(maxhp==0?200:hp*200/maxhp);
			pt.put(maxmp==0?200:mp*200/maxmp);			
		}
		if((moveType&MOVE_STATE)!=0){
			pt.putShort(state);
		}
		if((moveType&MOVE_DETAIL)!=0){
			pt.put(moveType>>8);
			if((moveType&MOVE_NAME)!=0){
				pt.putString(name);
			}
			if((moveType&MOVE_LEVEL)!=0){
				pt.put(level);
			}
			if((moveType&MOVE_FACTION)!=0){
				if(minorFaction!=0){
					pt.put(minorFaction<<5 | faction);
				}else{
					pt.put(faction);
				}
			}
			if((moveType&MOVE_EQUIPMENT)!=0){
				pt.putInt(head_score);
				pt.putInt(body_score);
				pt.putInt(weapon_score);
				pt.put(flashLevel);
			}
			if((moveType&MOVE_SEX)!=0){
				pt.put(sex);
			}
			if((moveType&MOVE_OWNER)!=0){
			    // 只有怪物才有owner
			    pt.putInt(-1);
			}
			if((moveType&MOVE_CLAZZ)!=0){
				pt.put(clazz);
			}
			if((moveType&MOVE_HORSE)!=0){
				pt.putInt(getHorseInt());
			}
		}
		return pt;
	}
	
	//最高位上马下马 高15位ImageId 低16位分数
	public int getHorseInt(){
		if(isRide()||horse!=null){
			return (horse.imageId<<16)|(1<<31);
		}else
			return 0;
	}
	
	protected Packet getBuffsPacket(){
		List<Buff> l = buffs.getShowBuffs();
		Packet pt = new Packet(OpCode.SYNC_BUFF_SERVER);
		pt.putInt(instanceId);
		pt.put(l.size());
		for(Buff buff:l){
			pt.putInt(buff.getInstanceID());
			pt.putInt(buff.getIconID());
			pt.putInt(buff.getEndTime());
		}
		return pt;
	}
	
	
	public static final int[] ANGLES = {45,0,90,135};
	
	public int getHalfAngle(){
		if(direct==-1)
			return ANGLES[0];
		return ANGLES[direct];
	}
	
	public static final int SPEED = 45;
	
	public int getSpeed() {
		return (int)(SPEED*speedRating);
	}
	
	public final static float RADTODEG = 180 / 3.14159265358979f;

	public static int calcAngle(int x, int y, int x1, int y1) {
		if(x==x1){
			return y>y1?270:90;
		}
		if(y==y1){
//			System.out.println(String.format("x:%d,y:%d,x1:%d,y%d,angle:%d", x,y,x1,y1,x>x1?180:0));
			return x>x1?180:0;
		}
		int angle = (int)Math.round(Math.toDegrees((float) Math.atan2(y1-y, x1-x)));
		if(angle<0)
			return (360 + angle);
		return angle;
//		float ang = (float) Math.atan((float) (y1 - y) / (float) (x1 - x))
//				* RADTODEG;
//		if (y1 < y && x1 > x)
//			return (int) ang;
//		else if ((y1 < y && x1 < x) || (y1 > y && x1 < x))
//			return (int) (ang + 180);
//		else
//			return (int) (ang + 360);

	}
	
//	public Unit[] getEnemy(Unit unit,int dist){
//		
//	}
//	public int getClazz() {
//		return clazz;
//	}
	
	
	
	public boolean isChaosState(){
		return (this.state&MASK_STATE_CHAOS)!=0;
	}
	
	public boolean cannotMove() {
	    return (this.state&MASK_STATE_STOP)!=0;
	}
	
	/**
	 * 判断是否允许攻击一个目标。如果满足以下条件的任何一个，则目标可以被攻击：
	 * 1. 目标处在敌对阵营(isEnemy返回true)
	 * 2. 目标和你正在PK
	 * 3. 目标在你的仇恨表中
	 */
	public int canAttack(GameObject unit) {
		if(pkInfo!=null&&unit.pkInfo==pkInfo&&pkInfo.state==PkInfo.STATE_STARTED)
			return 0;
	    if (this.type == TYPE_PLAYER && unit.type == TYPE_PLAYER) {
	    	if (this.map != null && this.map.map != null && this.map.map.isNeutral()) {
	    		return 8;
	    	}
	    }
	    if(unit.type == GameObject.TYPE_PLAYER && this.minorFaction != ((Player)unit).minorFaction){
	    	return 0;
		}
	    if(this.minorFaction!=0 && this.minorFaction == ((Unit)unit).minorFaction){
			return 8;
		}
//	    if(unit.type==GameObject.TYPE_CREATURE||unit.type==GameObject.TYPE_PLAYER){
//	    	Unit u = (Unit)unit;
//	    	if(this.minorFaction!=0&&u.minorFaction!=0)
//	    		return this.minorFaction != u.minorFaction?0:8;
//	    }
		if (isEnemy(unit)) {
			return 0;
		}


		return 8;
	}
	
	/**
	 * 判断是否允许辅助一个目标。如果满足以下条件的任何一个，则目标可以被辅助：
	 * 1. 目标是同一阵营的，并且没在PK
	 * 2. 目标在队伍中
	 */
	public boolean canAid(GameObject unit) {
		if (unit.faction == faction) {
			if(unit!=this&&unit.pkInfo!=null&&unit.pkInfo.state==PkInfo.STATE_STARTED)
				return false;
			return true;
		}
		// TODO: 查询是否在队伍中
		return false;
	}
	
	/**
	 * 攻击完的回调
	 */
	public void attack(CombatContext conext){
		
	}
	
	/**
	 * 被攻击完的回调
	 */
	public void attacked(CombatContext context){
		
	}
	
	public void setHeadScore(int score){
		if(this.head_score!=score){
			addIntPropertyChangedItem(ChangedItem.HEAD_SCORE,this.head_score,score,false);
			this.head_score = score;
			moveType |= MOVE_EQUIPMENT|MOVE_DETAIL;
		}
	}
	
	public void setBodyScore(int score){
		if(this.body_score!=score){
			addIntPropertyChangedItem(ChangedItem.BODY_SCORE,this.body_score,score,false);
			this.body_score = score;
			moveType |= MOVE_EQUIPMENT|MOVE_DETAIL;
		}
	}
	
	public void setWeaponScore(int score){
		if(this.weapon_score!=score){
			addIntPropertyChangedItem(ChangedItem.WEAPON_SCORE,this.weapon_score,score,false);
			this.weapon_score = score;
			moveType |= MOVE_EQUIPMENT|MOVE_DETAIL;
		}
	}
	
	public void setFlashLevel(int level){
		if(this.flashLevel != level){
			addIntPropertyChangedItem(ChangedItem.FLASHLEVEL,this.flashLevel,level,false);
			this.flashLevel = level;
			moveType |= MOVE_EQUIPMENT|MOVE_DETAIL;
		}
	}
	
	/**
	 * 所有可能改变属性的动作以后都应该执行次方法，重新刷新当前任务的属性
	 * @param levelUp  刷新属性动作是否由升级引起的，如果是，那么将会将血量以及蓝量加满
	 */
	public void refreshProperties(boolean levelUp){
		PropertyCalculator calc = new PropertyCalculator(this);
		equipments.enhance(calc);
		buffs.enhance(calc);
		if(horse!=null)
			horse.enhance(calc);
		setHeadScore(equipments.getHeadScore(level,clazz));
		setBodyScore(equipments.getBodyScore(level,clazz));
		setWeaponScore(equipments.getWeaponScore(level,clazz));
		setFlashLevel(equipments.getFlashLevel());
		calc.caculate();
		setStrength(calc.strength, false);
		setAgility(calc.agility,false);
		setStamina(calc.stamina,false);
		setIntellect(calc.intellect,false);
		setMaxhp(calc.hp,false);
		setMaxmp(calc.mp,false);
		if(levelUp){
			setHp(calc.hp,false);
			setMp(calc.mp,false);
		}else{
			if(hp>calc.hp){
				setHp(calc.hp,false);
			}
			if(mp>calc.mp){
				setMp(calc.mp,false);
			}
		}
		setAttackpowerup(calc.attackpowerup);
		setAttackpowerdown(calc.attackpowerdown);
		setSpellpower(calc.spellpower);
		setSpellheal(calc.spellheal);
		setDefense(calc.defense);
		setSpelldefense(calc.spelldefense);
		setCritical(calc.critical);
		setSpellCritical(calc.spellcritical);
		setHit(calc.hit);
		setSpellhit(calc.spellhit);
		setDodge(calc.dodge);
		setSpelldodge(calc.spelldodge);
		setAnticrit(calc.anticrit);
		setHealthrestore(calc.healthrestore);
		setManarestore(calc.manarestore);
		setDefensePercent(calc.defensePercent);
		setSpeedRatio(calc.getSpeed());
		expRatio = calc.expRatio;
		horseExpRatio = calc.horseExpRatio;
		moneyRatio = calc.moneyRatio;
	}
	
	public void addThreatUnit(Unit u, float initThreat, boolean direct) {
		threats.addUnit(u.ref(), initThreat, direct);
		setAttackState(true);
	}
	
	public void removeThreatUnit(GameObjectRef u, boolean bidirect) {
		threats.removeUnit(u);
		if (bidirect) {
			Unit u1 = (Unit) ObjectAccessor.getGameObject(u);
			if (u1 != null) {
				u1.removeThreatUnit(ref(),false);
			}
		}
		if (threats.count == 0) {
			setAttackState(false);
		}
	}
	
	public boolean containsThreat(GameObjectRef u){
		return threats.contains(u);
	}
	
	public int getThreatCount(){
		return threats.count;
	}
	
	public float getThreat(GameObjectRef u){
		return threats.getThreat(u);
	}
	
	public GameObjectRef getFirstThreat(){
		return threats.getFirstThreat();
	}
	
	public GameObjectRef[] getAllThreats() {
		return threats.getAllThreats();
	}
	
	public void clearThreats() {
		if (threats.count > 0) {
			GameObjectRef[] enemies = threats.getAllThreats();
			for (GameObjectRef enemy : enemies) {
				Unit unit = (Unit) ObjectAccessor.getGameObject(enemy);
				if (unit != null) {
					unit.removeThreatUnit(ref(),true);
				}
			}
			threats.clear();
			setAttackState(false);
		}
	}
	
	/**
	 * 改变某个单位对自己的威胁值。
	 * @param target 目标，如果目标不在仇恨表中，则不变化
	 * @param rate 改变比例，1.0表示不变
	 */
	public void changeThreat(Unit target, float rate) {
	    GameObjectRef ref = target.ref();
	    float threat = threats.getThreat(ref);
	    if (threat == 0.0f) {
	        return;
	    }
	    float addThreat = threat * (rate - 1.0f);
	    threats.addUnit(ref, addThreat, false);
	}
}

class DieInfo{
	public GameObjectRef source;
	public DieInfo(GameObjectRef source){
		this.source = source;
	}
}
