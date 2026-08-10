package peony.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.game.buff.ParamEnhanceSet;
import peony.game.skill.Skill;
import peony.net.Packet;

/**
 * 一次战斗伤害的计算环境。一次战斗伤害分为下面6步计算：
 * 
 * 1. preHit过程：处理技能/BUFF对命中率/爆击率的修正。
 * 2. 命中/爆击计算。
 * 3. postHit过程：处理技能/BUFF对命中/爆击计算结果的修正。
 * 4. preDamage过程：处理技能/BUFF对攻击力/防御力/治疗效果的修正。
 * 5. 伤害/治疗计算。
 * 6. postDamage过程：处理技能/BUFF对伤害/治疗量的修正。
 * 
 * 以上6步执行完成后，一次技能攻击的结果计算完成，保存在本类的成员中。游戏循环应根据
 * 此计算结果应用伤害/治疗效果。如果本次攻击导致反击，反击技能保存在againstSkill成员
 * 中，调用者还需根据反击技能执行反击伤害/治疗计算。
 * 
 * @author lighthu
 */
public class CombatContext {
	private static final Logger log = Logger.getLogger(CombatContext.class);
	
	public static final int ATTACKRESULT_HIT = 0;  //命中
	public static final int ATTACKRESULT_MISS = 1; //miss
	public static final int ATTACKRESULT_IMMUNE = 2;  //免疫
	public static final int ATTACKRESULT_CRIT = 3; // 爆击且命中
	
	public static final int DAMAGE_PHYSICAL = 0;  // 物理伤害
	public static final int DAMAGE_MAGIC = 1;    // 法术伤害
	public static final int DAMAGE_DECMP = 2;	// 抽蓝
	public static final int DAMAGE_DEBUFF = 3;  // 加DEBUFF
	public static final int DAMAGE_HEAL = 4;   // 治疗
	public static final int DAMAGE_ADDMP = 5;   // 回蓝
	public static final int DAMAGE_BUFF = 6;  // 加BUFF
	
	public Unit source;
	public Unit target;
	public Skill skill;
	
	/** 当前命中计算结果 */
	public int attackResult;
	/** 当前爆击计算结果 */
	public boolean critical;
	/** 当前伤害/治疗类型 */
	public int damageType;
	/** 当前伤害/治疗量。只允许postDamage修改 */
	public int damage;
	/** 威胁值。只允许postDamage修改 */
	public int threat;
	
	/** 附加命中率。包括物理和法术（取决于damageType），只允许preHit修改 */
	public float hitRate;
	/** 附加爆击率。包括物理和法术（取决于damageType），只允许preHit修改 */
	public float critRate;
	/** 附加闪避率。包括物理和法术（取决于damageType）。只允许preHit修改 */
	public float dodge;
	
	/** 附加攻击力/治疗量(数值)。包括物理和法术（取决于damageType），只允许preDamage修改 */
	public int attackPower;
	/** 附加攻击力/治疗量(百分比)。包括物理和法术（取决于damageType），只允许preDamage修改 */
	public float attackPowerRate;
	
	/** 附加防御力(数值)。包括物理和法术（取决于damageType），只允许preDamage修改 */
	public int armor;
	/** 附加防御力(百分比)。包括物理和法术（取决于damageType），只允许preDamage修改 */
	public float armorRate;
	
	/** 附加威胁值(数值)。只允许preDamage修改 */
	public int threatAdd;
	/** 附加威胁值(百分比)。只允许preDamage修改 */
	public float threatAddRate;
	
	/** 技能临时变量，用于实现技能串联影响（比如技能2影响技能1效果出现的概率）*/
	public Map<String, Float> skillParams = new HashMap<String, Float>();
	
	/** 攻击触发技能 */
	public List<Skill> activeSkills = new ArrayList<Skill>();
	/** 被攻击触发技能 */
	public List<Skill> passiveSkills = new ArrayList<Skill>();
	
	private static final Random RND = new Random();
	
	/**又一个Attack可能引发多个CombatContext，只有目标或者第一个CombatContext是没有parent值的，其他的CombatContext都应该拿目标或者第一个CombatContext做为起parent*/
	public CombatContext parent;
	/** 如果是AOE技能，表示在AOE目标中的顺序，0表示第一个；对于怪物的AOE技能，所有目标的顺序都是0保证100%伤害 */
	public int targetIndex;
	
	/**
	 * 初始化伤害计算环境。
	 * @param source 技能使用者
	 * @param target 攻击目标
	 * @param skill 使用技能
	 */
	public CombatContext(Unit source, Unit target, Skill skill) {
		this(source,target,skill,null,0);
	}
	
	public CombatContext(Unit source, Unit target, Skill skill, CombatContext parent, int targetIndex){
		this.source = source;
		this.target = target;
		this.skill = skill;
		this.parent = parent;
		this.targetIndex = targetIndex;
	}
	
	/**
	 * 计算是否命中和是否爆击。
	 * 物理命中率 = (人物命中率 + 级别差 + 技能附加命中率) * (1 - 对方闪避)
	 * 法术命中率 = min(100, max(0, 95 + (级别差^3 * (5 + 攻击方级别)) / 480))
	 * 物理暴击率 = 敏捷 / 40 + 装备加成 + 修正
	 * 魔法暴击率 = 智力 / 50 + 装备加成 + 修正
	 */
	public void calculateHit() {
		int temp;
		if (damageType == DAMAGE_PHYSICAL) {
			// 物理伤害，应用物理命中公式
			float h = (source.hit + hitRate) * 100f + (source.level - target.level);
			if (h < 70) {
				h = 70;
			} else if (h > 150) {
				h = 150;
			}
			float d = (1.0f - target.dodge - dodge) * 100f;
			if (d < 60) {
				d = 60;
			}
			temp = (int)(d * h);
		} else if (damageType == DAMAGE_MAGIC || damageType == DAMAGE_DECMP || damageType == DAMAGE_DEBUFF) {
			// 法术伤害/抽蓝/DEBUFF，应用法术命中公式
            float h = (source.spellhit + hitRate) * 100f + (source.level - target.level);
            if (h < 70) {
                h = 70;
            } else if (h > 150) {
                h = 150;
            }
            float d = (1.0f - target.spelldodge - dodge) * 100f;
            if (d < 60) {
                d = 60;
            }
            temp = (int)(d * h);
		} else {
			// 治疗/回蓝/BUFF，100%命中
			temp = 10000;
		}
		if (CommonUtil.hit(RND, temp, 10000)) {
			attackResult = ATTACKRESULT_HIT;
		} else {
			attackResult = ATTACKRESULT_MISS;
		}
		
		if (damageType == DAMAGE_PHYSICAL) {
			// 物理伤害，应用物理爆击公式，考虑免暴
			temp = (int)((source.critical + critRate - target.anticrit) * 10000);
			temp += Math.min(10, source.level - target.level) * 100;
		} else if (damageType == DAMAGE_MAGIC || damageType == DAMAGE_DECMP) {
		    // 法术伤害/抽蓝/DEBUFF，应用法术爆击公式，考虑免暴
		    temp = (int)((source.spellcritical + critRate - target.anticrit) * 10000);
		    temp += Math.min(10, source.level - target.level) * 100;
		} else if (damageType == DAMAGE_HEAL || damageType == DAMAGE_ADDMP) {
			// 治疗或回蓝，应用法术爆击公式
			temp = (int)((source.spellcritical + critRate) * 10000);
		} else {
			// BUFF/DEBUFF，不会暴击
			temp = 0;
		}
		critical = CommonUtil.hit(RND, temp, 10000);
		if (critical) {
			attackResult = ATTACKRESULT_CRIT;
		}
	}
	
	/**
	 * 计算伤害/治疗/威胁值。
	 * 物理防御力 = 人物物理防御 + 装备加成 + 修正
	 * 物理伤害 = 物理攻击力 * (100 - 防御方物理防御 / (9 + 攻击方级别)) / 100
	 * 魔法防御力 = 人物魔法防御 + 装备加成 + 修正
	 * 魔法伤害 = 魔法攻击力 * (1 - 0.75 * 防御方魔法防御 / 攻击方级别 * 5)
	 */
	public void calculateDamage() {
		if (damageType == DAMAGE_PHYSICAL) {
			// 物理攻击，应用物理伤害公式
			float ap = CommonUtil.getCount(RND, (int)source.attackpowerdown, (int)source.attackpowerup);
			ap *= 1f + attackPowerRate;
			ap += attackPower;
			float actArmor = target.defense * (1f + armorRate) + armor;
			float df = PropertyCalculator.calcDefensePercent(target.clazz, actArmor, source.level);
			damage = (int)(ap * (1 - df));
		} else if (damageType == DAMAGE_MAGIC || damage == DAMAGE_DEBUFF) {
			// 法术攻击，应用法术伤害公式
			float ap = source.spellpower;
			ap *= 1f + attackPowerRate;
			ap += attackPower;
			float actDef = target.spelldefense * (1f + armorRate) + armor;
			float df = PropertyCalculator.calcSpellDefensePercent(actDef, source.level);
			damage = (int)(ap * (1 - df));
		} else if (damageType == DAMAGE_HEAL || damageType == DAMAGE_BUFF) {
		    // 治疗，计算法术治疗效果
		    float heal = source.spellheal;
		    heal *= 1f + attackPowerRate;
		    heal += attackPower;
		    damage = (int)heal;
		} else if (damageType == DAMAGE_DECMP || damageType == DAMAGE_ADDMP){
			// 抽蓝/回蓝，技能决定数值
			damage = attackPower;
			damage *= 1f + attackPowerRate;
		}
		
		// 爆击伤害/治疗加倍，伤害需要考虑对方的免暴，免暴10%可以把暴击额外部分伤害削弱10%
		if (critical) {
		    if (isDamage()) {
		        damage *= 2.0f - target.anticrit;
		    } else {
		        damage *= 2;
		    }
		}
		
		// 如果是AOE技能，对第一目标以外的目标进行伤害扣减
		if (targetIndex > 0 && isDamage()) {
			damage *= 1.0f - Math.min(targetIndex * 0.1f, 0.4f);
		}
		
		// 威胁值
		switch (damageType) {
		case DAMAGE_PHYSICAL:
		case DAMAGE_MAGIC:
			threat = damage;
			break;
		case DAMAGE_DECMP:
			threat = (damage * 13) / 10;
			break;
		case DAMAGE_HEAL:
			threat = (damage * 6) / 10;
			break;
		case DAMAGE_ADDMP:
			threat = (damage * 8) / 10;
			break;
		default:
		    threat = 0;
		    break;
		}
		threat *= 1f + threatAddRate;
		threat += threatAdd;
	}
	
	/**
	 * 计算治疗仇恨，按实际的治疗量计算。
	 */
	public int calcHealThreat(int heal) {
		int t = (heal * 6) /10;
		t *= 1f + threatAddRate;
		t += threatAdd;
		return t;
	}
	
	/**
	 * 计算HOT的治疗仇恨，按实际的治疗量计算。
	 */
	public static int calcHotThreat(int heal) {
		return (heal * 6) / 10;
	}
	
	/**
	 * 在物理伤害计算完成后附加一次法术伤害，只用于夺命连环三仙剑技能。
	 * @param ap
	 */
	public void appendSpellDamage(float ap) {
        float df = target.spelldefense;
        df *= 75 / (source.level * 5);
        if (df > 75) {
            df = 75;
        }
        int dmg = (int)(ap * (1 - df / 100f));
        damage += dmg;
        threat += dmg;
	}
	
	/**
	 * 判断是否击中。
	 */
	public boolean hited() {
		return attackResult == ATTACKRESULT_HIT || attackResult == ATTACKRESULT_CRIT;
	}
	
	/**
	 * 判断是否暴击。
	 */
	public boolean critical() {
	    return attackResult == ATTACKRESULT_CRIT;
	}
	
	/**
	 * 判断是否伤害。
	 */
	public boolean isDamage() {
		return damageType == DAMAGE_PHYSICAL || damageType == DAMAGE_MAGIC; 
	}
	
	/**
	 * 判断是否攻击。
	 */
	public boolean isAttack() {
	    return damageType == DAMAGE_PHYSICAL || damageType == DAMAGE_MAGIC || damageType == DAMAGE_DECMP || damageType == DAMAGE_DEBUFF;
	}
	
	/**
	 * 向客户端发送这一个伤害的结果。
	 */
	public void sendResult() {
		if (parent == null) {  //只有没有parent的CombatContext需要发送Attack包，比如AOE时，只需要发送一次AOE成功
			Packet pt = new Packet(OpCode.SKILL_ATTACK_SERVER);
			pt.putInt(source.instanceId);
			if ((skill.getTargetType() & Skill.TARGET_FLAG_SELF) != 0) {
				pt.putInt(source.instanceId);
			} else {
				pt.putInt(target.instanceId);
			}
			pt.putInt(skill.getCastAnimation(source));
//			source.broadcast(pt, null, true,false);
			source.broadcast(pt, source.type==GameObject.TYPE_PLAYER?(Player)source:null, target.type==GameObject.TYPE_PLAYER?(Player)target:null,true,false,true);
		}
		Packet pt = new Packet(OpCode.SKILL_ATTACKED_SERVER);
		pt.putInt(target.instanceId);
		pt.putInt(CommonUtil.currentMillis());
		pt.putInt(source.instanceId);
		pt.put(attackResult);
		pt.put(damageType);
		pt.putInt(damage);
		pt.putInt(skill.getHitAnimation(source));
		if(target!=null){
//			target.broadcast(pt, null, true, false);
			target.broadcast(pt, source.type==GameObject.TYPE_PLAYER?(Player)source:null,target.type==GameObject.TYPE_PLAYER?(Player)target:null, true, false,true);
		}
	}
	
	/**
     * 在实际战斗中计算技能参数值。
     * @param skill 技能
     * @param name 技能名称
     * @param value 当前值
     * @return 修正后的值
     */
    public int getSkillParam(Skill skill, String name, int value) {
        int v1 = source.buffs.getParamEnhances().enhance(ParamEnhanceSet.TYPE_SKILL_ACTIVE, skill.getGroupId(), name, value);
        int v2 = target.buffs.getParamEnhances().enhance(ParamEnhanceSet.TYPE_SKILL_PASSIVE, skill.getGroupId(), name, v1);
        return v2;
    }
    
    /**
     * 在实际战斗中计算技能参数值。
     * @param skill 技能
     * @param name 技能名称
     * @param value 当前值
     * @return 修正后的值
     */
    public float getSkillParam(Skill skill, String name, float value) {
        float v1 = source.buffs.getParamEnhances().enhance(ParamEnhanceSet.TYPE_SKILL_ACTIVE, skill.getGroupId(), name, value);
        float v2 = target.buffs.getParamEnhances().enhance(ParamEnhanceSet.TYPE_SKILL_PASSIVE, skill.getGroupId(), name, v1);
        return v2;
    }
}
