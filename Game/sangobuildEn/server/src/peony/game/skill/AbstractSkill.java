package peony.game.skill;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.game.CombatEffect;
import peony.game.ObjectAccessor;
import peony.game.Skills;
import peony.game.Unit;
import peony.game.buff.Buff;

public abstract class AbstractSkill implements Skill{
	protected int groupId;
	protected int level;
	protected int distance;
	protected int actTime;
	protected int CDGroup;
	protected int CDTime;
	protected int range;
	protected int iconId;
	protected int prepareAnimation;
	protected int castAnimation;
	protected int hitAnimation;
	protected int type;
	protected int targetType;
	protected String name;
	protected int clazz;
	protected String desc;
	protected float mp;
	protected CombatEffect actEffect;
	
	protected static final Logger log = Logger.getLogger(AbstractSkill.class);
	protected static final Random RND = new Random();
	
	public AbstractSkill(int groupId,String name, int level) {
		this.groupId = groupId;
		this.name = name;
		this.level = level;
		this.desc = "暫時沒有描述";
		actEffect = createActEffect();
	}
	
	/*
	 * 构造模板方法：主动单体攻击技能。
	 * @param clazz 所属职业
	 * @param mp 消耗法力
	 * @param actt 施法时间(毫秒)
	 * @param cdg 冷却组ID
	 * @param cdt 冷却时间(毫秒)
	 * @param dist 攻击距离(码)
	 * @param iconid 图标ID
	 */
	protected void setupSingleAttack(int clazz, int mp, int actt, int cdg, int cdt, 
			int dist, int iconid) {
		this.clazz = clazz;
		this.mp = mp;
		this.actTime = actt;
		this.CDGroup = cdg;
		this.CDTime = cdt;
		this.distance = dist * 8;
		this.iconId = iconid;
		this.type = TYPE_ATTACK | TYPE_VISIBLE | TYPE_RIDE_USE;
		this.targetType = TARGET_SINGLE_ATTACK;
	}
	
	/*
	 * 构造模板方法：主动群体攻击技能。
	 * @param clazz 所属职业
	 * @param mp 消耗法力
	 * @param actt 施法时间(毫秒)
	 * @param cdg 冷却组ID
	 * @param cdt 冷却时间(毫秒)
	 * @param range 攻击范围(码)
	 * @param iconid 图标ID
	 * @param needTarget 是否需要选择一个目标（如果为否，以自己为中心）
	 * @param dist 如果needTarget为true，指定攻击距离(码) 
	 */
	protected void setupAreaAttack(int clazz, int mp, int actt, int cdg, int cdt,
			int range, int iconid, boolean needTarget, int dist) {
		this.clazz = clazz;
		this.mp = mp;
		this.actTime = actt;
		this.CDGroup = cdg;
		this.CDTime = cdt;
		this.range = range * 8;
		this.iconId = iconid;
		this.distance = dist * 8;
		type = TYPE_ATTACK | TYPE_VISIBLE | TYPE_RIDE_USE;
		if (needTarget) {
			targetType = TARGET_AOE_ATTACK_TARGET;
		} else {
			targetType = TARGET_AOE_ATTACK_SELF;
		}
	}
	
	/*
	 * 构造模板方法：主动辅助技能。
	 * @param clazz 所属职业
	 * @param mp 消耗法力
	 * @param actt 施法时间(毫秒)
	 * @param cdg 冷却组ID
	 * @param cdt 冷却时间(毫秒)
	 * @param iconid 图标ID
	 * @param needTarget 是否需要选择一个目标（如果为否，只能辅助自己）
	 * @param dist 如果needTarget为true，指定有效距离(码) 
	 */
	protected void setupSingleAid(int clazz, int mp, int actt, int cdg, int cdt,
			int iconid, boolean needTarget, int dist) {
		this.clazz = clazz;
		this.mp = mp;
		this.actTime = actt;
		this.CDGroup = cdg;
		this.CDTime = cdt;
		this.iconId = iconid;
		this.distance = dist * 8;
		type = TYPE_AID | TYPE_VISIBLE | TYPE_RIDE_USE;
		if (needTarget) {
			targetType = TARGET_SINGLE_AID;
		} else {
			targetType = TARGET_AID_SELF;
		}
	}
	
	/*
	 * 构造模板方法：主动群体辅助技能。
	 * @param clazz 所属职业
	 * @param mp 消耗法力
	 * @param actt 施法时间(毫秒)
	 * @param cdg 冷却组ID
	 * @param cdt 冷却时间(毫秒)
	 * @param range 攻击范围(码)
	 * @param iconid 图标ID
	 * @param needTarget 是否需要选择一个目标（如果为否，以自己为中心）
	 * @param dist 如果needTarget为true，指定攻击距离(码) 
	 */
	protected void setupAreaAid(int clazz, int mp, int actt, int cdg, int cdt,
			int range, int iconid, boolean needTarget, int dist) {
		this.clazz = clazz;
		this.mp = mp;
		this.actTime = actt;
		this.CDGroup = cdg;
		this.CDTime = cdt;
		this.range = range * 8;
		this.iconId = iconid;
		this.distance = dist * 8;
		type = TYPE_AID | TYPE_VISIBLE | TYPE_RIDE_USE;
		if (needTarget) {
			targetType = TARGET_AOE_AID_TARGET;
		} else {
			targetType = TARGET_AOE_AID_SELF;
		}
	}
	
	/*
	 * 构造模板方法：被动辅助技能。
	 * @param clazz 所属职业
	 * @param iconid 图标ID
	 */
	protected void setupPassiveSkill(int clazz, int iconid) {
		this.clazz = clazz;
		this.iconId = iconid;
		type = TYPE_PASSIVE;
		targetType = TARGET_AID_SELF;
	}

	/*
	 * 构造模板方法：光环技能。
	 * @param clazz 所属职业
	 * @param iconid 图标ID
	 */
	protected void setupAreaBuffSkill(int clazz, int iconid) {
		this.clazz = clazz;
		this.iconId = iconid;
		type = TYPE_BUFF;
		targetType = TARGET_AOE_AID_SELF;
	}
	
	public int getGroupId() {
		return groupId;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return Skills.getSkillId(groupId, getLevel());
	}

	public boolean isPlayerSkill() {
		return this.clazz != -1;
	}

	public boolean isAutoLearn() {
	    return false;
	}
	
	public int getLevel() {
		return level;
	}

	public int getDistance(Unit owner) {
		return (int)owner.buffs.updateDistance(this, distance);
	}

	public int getActTime(Unit owner) {
		return (int)owner.buffs.updateActTime(this, actTime);
	}

	public int getCDGroup() {
		return CDGroup;
	}
	
	public int getCDTime(Unit owner) {
		return (int)owner.buffs.updateCDTime(this, CDTime);
	}

	public int getRange(Unit owner) {
		return (int)owner.buffs.updateRange(this, range);
	}

	public int getIconId() {
		return iconId;
	}
	
	public int getPrepareAnimation(Unit src) {
	    return prepareAnimation;
	}
	
    public int getCastAnimation(Unit src) {
        return castAnimation;
    }

    public int getHitAnimation(Unit src) {
        return hitAnimation;
    }

	public int getType() {
		return type;
	}

	public int getTargetType() {
		return targetType;
	}
	
	public int getClazz(){
		return clazz;
	}
	
	public int getPoint(){
		return 1;
	}
	
	/**
	 * 使用技能需要消耗的蓝
	 */
	public int getMP(Unit owner) {
	    float realmp = mp;
	    if (realmp < 0.0f) {
	        realmp = (-realmp) * owner.level;
	    }
        return (int)owner.buffs.updateMP(this, realmp);
	}
	
	/**
	 * 取得使用此技能需要的武器类型，null表示不限制。
	 */
	public int[] getRequireWeapon() {
		return null;
	}
	
	/**
	 * 取得本技能的下一级，如果没有下一级返回null。
	 */
	public Skill getNextLevel() {
		try {
			Constructor constructor = getClass().getConstructors()[0];
			return (Skill)constructor.newInstance(getLevel() + 1);
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getDesc(Unit owner){
		return desc;
	}
	
	public Buff newBuff(){
		return null;
	}
	
	public CombatEffect getActEffect() {
		return actEffect;
	}
	
	protected CombatEffect createActEffect() {
		return null;
	}
	
	public Buff getAreaBuff() {
		return null;
	}
	
	public boolean isAttackSkill(){
		return (type&TYPE_ATTACK) != 0;
	}
	
	public byte[] toClientBytes(Unit owner){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeShort(groupId);
			dos.write(level);
			dos.writeUTF(name);
			dos.writeShort(getDistance(owner));
			dos.writeShort(getActTime(owner));
			dos.writeShort(CDGroup);
			dos.writeInt(getCDTime(owner));
			dos.write(getRange(owner));
			dos.write(type);
			dos.write(targetType);
			if(!isPlayerSkill()){
				dos.write(0);
			}else{
				dos.write(getPoint());
			}
			dos.writeInt(getPrepareAnimation(owner));
			dos.writeInt(iconId);
			dos.writeShort(getMP(owner));
			int[] weapon = getRequireWeapon();
			if (weapon == null) {
				dos.writeByte(0);
			} else {
				dos.writeByte(weapon.length);
				for (int i = 0; i < weapon.length; i++) {
					dos.writeByte(weapon[i]);
				}
			}
			Skill sk = getNextLevel();
			if (sk == null ) {
				dos.writeBoolean(false);
				dos.writeShort(0);
			} else {
				dos.writeBoolean(true);
				dos.writeShort(sk.getRequireLevel());
			}
			dos.writeShort(ObjectAccessor.getSkillMaxLevel(groupId));
			//bookskill
			dos.write(clazz);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
}
