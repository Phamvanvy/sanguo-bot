package peony.game;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import peony.game.skill.Skill;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.friend.PlayerRelation;

/**
 * 一次技能攻击请求。当玩家(或怪物)开始吟唱一个伤害技能的时候创建，并在吟唱结束时执行。
 * 
 * @author lighthu
 */
public class Attack {

	private static final Logger log = Logger.getLogger(Attack.class);
	private static final Random RND = new Random();

	protected Skill skill; // 使用技能
	protected int time; // 技能吟唱时间(毫秒)
	public GameObjectRef sourceRef; // 源对象
	public GameObjectRef targetRef; // 目标对象，如果是以自己为中心的AOE技能，target为null
	public GameObjectRef[] targets; // 群体攻击目标，只用于BOSS攻击，由BOSS AI指定，不同于普通AOE技能的目标选择模式
	// protected int timer; // 计时器，如果小于0说明应该施放技能，计算技能伤害
	protected int triggerTime;// trigger的时间

	protected boolean useBuffs; // 使用技能的时候是计算两方的buff

	protected boolean autoAttack; // 是否是自动攻击
	
	protected Lock lock = new ReentrantLock();

	/**
	 * 创建一个技能攻击请求。
	 * 
	 * @param skill
	 *            技能
	 * @param source
	 *            施法者
	 * @param target
	 *            目标（可能为null）
	 */
	public Attack(Skill skill, GameObject source, GameObject target,
			int timeOffset) {
		this(skill, source, target, timeOffset, true, false);
	}

	public Attack(Skill skill, GameObject source, GameObject target,
			int timeOffset, boolean useBuffs, boolean autoAttack) {
		this.skill = skill;
		this.time = skill.getActTime((Unit) source) - timeOffset;
		if (this.time < 0)
			this.time = 0;
		this.triggerTime = Time.currTime + time;
		this.sourceRef = source.ref();
		if (target != null)
			this.targetRef = target.ref();
		this.useBuffs = useBuffs;
		this.autoAttack = autoAttack;
	}
	
	/**
	 * 创建一个BOSS的攻击
	 * @param skill 技能
	 * @param source 施法者
	 * @param targets 选中的目标，可以多个
	 */
	public Attack(Skill skill, GameObject source, GameObjectRef[] targets) {
	    this.skill = skill;
        this.time = skill.getActTime((Unit) source);
        if (this.time < 0)
            this.time = 0;
        this.triggerTime = Time.currTime + time;
        this.sourceRef = source.ref();
        this.targets = targets;
        this.useBuffs = true;
        this.autoAttack = false;
    }

	/**
	 * 技能吟唱时间(毫秒)
	 */
	public int getTime() {
		return time;
	}

	/**
	 * 更新计时器，如果到时间则施放此技能。
	 * 
	 * @param diff
	 *            步长(毫秒)
	 * @return 如果技能被施放，返回0
	 */
	public int update(int diff) {
	    if (targets == null) {
	        // 非BOSS攻击，随时检查攻击目标是否还在
    		if ((skill.getTargetType() & Skill.TARGET_FLAG_SELF) == 0) {
    			if (targetRef == null)
    				return 4;
    			GameObject o = ObjectAccessor.getGameObject(targetRef);
    			if (o == null) {
    				return 3;
    			}
    			if (((skill.getType() & Skill.TYPE_RELIVE) == 0 && !o.isAlive())) {
    				return 3;
    			}
    			
    			// 每5个tick检查一次距离
    			if (Time.tick % 5 == 0) {
        			GameObject src = ObjectAccessor.getGameObject(sourceRef);
        			if (!o.inRange(src, skill.getDistance((Unit)src) * 2 + 40)) {
        			    // 目标跑出过远
        			    return 1;
        			}
    			}
    		}
	    }
		if (Time.currTime >= triggerTime) {
			return trigger();

		}
		return -1; // 正在攻击
	}

	/*
	 * 针对单一目标施放技能，计算伤害/治疗并应用。
	 * @param source 施法者
	 * @param target 目标
	 * @param parent 如果是AOE，表示第一个目标的combatcontext
	 * @param targetIndex 在AOE中的伤害顺序，用于计算伤害递减
	 */
	protected CombatContext combat(Unit source, Unit target, CombatContext parent, int targetIndex) {
		CombatContext context = new CombatContext(source, target, skill, parent, targetIndex);
		combat(context, true, true);
		
		// 如果是玩家对玩家攻击，增加临时关系
		if (source.type == GameObject.TYPE_PLAYER && target.type == GameObject.TYPE_PLAYER && source.faction != target.faction) {
			// 为了提高效率，这里直接修改关系表，而不是按正常方式新建Event
			Player p1 = (Player)source;
			Player p2 = (Player)target;
			if (p1.relations != null && !p1.relations.tempList.exists(p2.id)) {
				Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(p2.id);
				p1.relations.addTempList(actor, PlayerRelation.INTERACT_ATTACK);
			}
			if (p2.relations != null && !p2.relations.tempList.exists(p1.id)) {
				Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(p1.id);
				p2.relations.addTempList(actor, PlayerRelation.INTERACT_ATTACK);
			}
		}
		return context;
	}

	/*
	 * 针对单一目标施放技能，计算伤害/治疗并应用。
	 * 
	 * @param context 伤害计算环境
	 * @param triggerActiveSkill 是否允许触发主动攻击
	 * @param triggerPassiveSkill 是否允许出发被动攻击
	 */
	protected void combat(CombatContext context, boolean triggerActiveSkill, boolean triggerPassiveSkill) {
		Unit source = context.source;
		Unit target = context.target;
		CombatEffect skillEff = null;
		if (context.skill != null) {
			skillEff = context.skill.getActEffect();
		}

		// 1. preHit过程
		if (skillEff != null) {
			skillEff.preHit(context, true);
		}
		if (useBuffs) {
			source.buffs.preHit(context, true);
			target.buffs.preHit(context, false);
		}

		// 2. 计算命中/爆击
		context.calculateHit();

		if (context.hited()) {
			// 3. postHit过程
			if (skillEff != null) {
				skillEff.postHit(context, true);
			}
			if (useBuffs) {
				source.buffs.postHit(context, true);
				target.buffs.postHit(context, false);
			}
			if (context.hited()) {
				// 4. preDamage过程
				if (skillEff != null) {
					skillEff.preDamage(context, true);
				}
				if (useBuffs) {
					source.buffs.preDamage(context, true);
					target.buffs.preDamage(context, false);
				}

				// 5. 计算伤害/治疗量
				context.calculateDamage();

				// 6. postDamage过程
				if (skillEff != null) {
					skillEff.postDamage(context, true);
				}
				if (useBuffs) {
					source.buffs.postDamage(context, true);
					target.buffs.postDamage(context, false);
				}

				// TODO
				// 处理新的伤害类型：治疗/回蓝
				// 处理治疗仇恨，当为某一目标治疗时，所有对此目标有仇恨的单位都需要添加对
				// 治疗者的仇恨。

				// 7. 应用伤害/治疗效果
				if (target.isAlive()) {
					if (context.damageType == CombatContext.DAMAGE_HEAL) {
						// 治疗
						int actCure = 0;
						if (context.damage > 0) {
							actCure = target.setHp(target.hp + context.damage, false);
						}

						// 增加所有敌视治疗目标的单位的仇恨
						addHealThreat(source, target, context.calcHealThreat(actCure), false);
					} else if (context.damageType == CombatContext.DAMAGE_ADDMP) {
						// 回蓝
						if (context.damage > 0) {
							target.setMp(target.mp + context.damage, false);
						}

						// 增加所有敌视治疗目标的单位的仇恨
						addHealThreat(source, target, context.threat, false);
					} else if (context.damageType == CombatContext.DAMAGE_BUFF) {
						// 加BUFF，增加所有敌视目标的单位的仇恨
						addHealThreat(source, target, 0.0f, false);
					} else if (context.damageType == CombatContext.DAMAGE_DECMP) {
						// 抽蓝
						if (context.damage > 0) {
							target.setMp(target.mp - context.damage, false);
						}

						// 增加和伤害目标之间的威胁值
						addDamageThreat(source, target, context.threat, true);

						// 回调
						source.attack(context);
						target.attacked(context);
					} else if (context.damageType == CombatContext.DAMAGE_DEBUFF) {
						// 加DEBUFF，增加和伤害目标之间的威胁值
						addDamageThreat(source, target, 0.0f, true);

						// 回调
						source.attack(context);
						target.attacked(context);
					} else {
						// 物理伤害/法术伤害
						if (context.damage > 0) {
							target.setHp(target.hp - context.damage, false);
						}

						// 增加和伤害目标之间的威胁值
						addDamageThreat(source, target, context.threat, true);

						// 回调
						source.attack(context);
						target.attacked(context);

						// 处理死亡
						if (target.hp <= 0) {
							target.die(source);
						}
					}
				}
				// 8. finish过程
				if (skillEff != null) {
					skillEff.finished(context, true);
				}
				if (useBuffs) {
					source.buffs.finished(context, true);
					target.buffs.finished(context, false);
				}
			}
		}
		if (context.isAttack() && !context.hited()) {
			// 不管是否击中，都增加仇恨
			addDamageThreat(source, target, 0.01f, true);
		}

		// 向客户端发送伤害结果
		context.sendResult();

		// 处理附加攻击
		if (triggerActiveSkill) {
		    for (Skill skill : context.activeSkills) {
                if (!source.isAlive() || !target.isAlive()) {
                    break;
                }
                CombatContext againstContext = new CombatContext(source,
                        target, skill);
                if ((skill.getTargetType() & Skill.TARGET_FLAG_ATTACK) == 0) {
                    againstContext.target = source;
                }
                combat(againstContext, false, true);
            }
		}
		
		// 处理反击
		if (triggerPassiveSkill) {
			for (Skill skill : context.passiveSkills) {
				if (!target.isAlive() || !source.isAlive()) {
					break;
				}
				CombatContext againstContext = new CombatContext(target,
						source, skill);
				if ((skill.getTargetType() & Skill.TARGET_FLAG_ATTACK) == 0) {
					againstContext.target = target;
				}
				combat(againstContext, false, false);
			}
		}
	}

	/*
	 * 执行技能攻击。
	 */
	public int trigger() {
	    // 扣蓝
		Unit source = (Unit) ObjectAccessor.getGameObject(sourceRef);
		if (source == null || source.isChaosState()) {
		    return 11;
		}
		int mana = skill.getMP(source);
		if (source.mp < mana) {
			return 12;
		}
		if (this.targets == null) {
		    // 非BOSS攻击，查找技能目标
    		Unit target = null;
            if ((skill.getTargetType() & Skill.TARGET_FLAG_SELF) != 0) {
                target = source;
            } else if (targetRef != null) {
    			target = (Unit) ObjectAccessor.getGameObject(targetRef);
    			if (target == null || target.map.map == null
    					|| source.getVMap() != target.getVMap()) // 如果target.map.map==null那么就是下线的玩家,不处理那么在设置relive的时候就有问题
    				return 4;
    		}
            
            // 检查目标是否在跑出过远
            if (target != null && target != source && 
                    !target.inRange(source, skill.getDistance(source) * 2 + 40)) {
                // 目标跑出过远
                return 1;
            }
    
    		// 处理AOE技能目标转换
    		Unit[] targets = null;
    		if ((skill.getTargetType() & Skill.TARGET_FLAG_SINGLE) == 0) {
    			if ((skill.getTargetType() & Skill.TARGET_FLAG_ATTACK) != 0) {
    				// 攻击技能
    				targets = source.getEnemies(target, skill.getRange(source));
    				target = null;
    			} else {
    				// 辅助技能
    				targets = source.getAidUnits(target, skill.getRange(source));
    			}
    		}
    		if ((skill.getTargetType() & Skill.TARGET_FLAG_ATTACK) != 0) {
    			if (target != null && !target.isAlive()) {
    				return 3; // 目标已经死亡
    			}
    			if (target != null) {
    				int code = source.canAttack(target);
    				if (code != 0)
    					return code;
    			}
    		}
    		
    		// 扣蓝
    		if (mana > 0) {
                source.setMp(source.mp - mana, false);
            }
    
    		// 对每一个目标施放技能
    		CombatContext parent = null;
    		int targetIndex = 0;
    		if (target != null) {
    			parent = combat(source, target, null, targetIndex++);
    		}
    		if (targets != null) {
    			for (Unit t : targets) {
    				if (t == target) {
    					continue;
    				}
    				CombatContext cc = combat(source, t, parent, targetIndex++);
    				if (parent == null) // 有可能target为空，那么将第一个CombatContext做为parent
    					parent = cc;
    			}
    		}
    
    		// 特殊处理对自己的AOE并且没有目标的情况
    		if (target == null && (targets == null || targets.length == 0)) {
    			Packet pt = new Packet(OpCode.SKILL_ATTACK_SERVER);
    			pt.putInt(source.instanceId);
    			pt.putInt(source.instanceId);
    			pt.putInt(skill.getCastAnimation(source));
    
    			source.broadcast(pt,
    					source.type == GameObject.TYPE_PLAYER ? (Player) source
    							: null, null, true, false, true);
    		}
		} else {
		    // 扣蓝
            if (mana > 0) {
                source.setMp(source.mp - mana, false);
            }
            
		    // 对于已经指定了目标的BOSS攻击，直接对每个目标应用攻击即可，不用查找目标
		    CombatContext parent = null;
		    for (GameObjectRef ref : this.targets) {
		        // 检查目标是否还存在
		        Unit t = (Unit)ObjectAccessor.getGameObject(ref);
		        if (t == null || !t.isAlive() || t.getVMap() != source.getVMap()) {
		            continue;
		        }
		        
		        // 检查目标是否还可以攻击/辅助
		        if ((skill.getTargetType() & Skill.TARGET_FLAG_ATTACK) != 0) {
		            if (source.canAttack(t) != 0) {
		                continue;
		            }
                } else {
                    if (!source.canAid(t)) {
                        continue;
                    }
                }
		        
		        // 执行攻击
                CombatContext cc = combat(source, t, parent, 0);
                if (parent == null)
                    parent = cc;
            }
		}
		if (!autoAttack && source.type == GameObject.TYPE_PLAYER) { // 处理CD,如果是自动攻击，那么不应该加上CD
			int cdGroup = skill.getCDGroup();
			int t = skill.getCDTime(source);
			source.setCoolDown(cdGroup, Time.currTime, Time.currTime + t);
		}
		return 0;
	}

	/**
	 * 强制把source拉到target仇恨表的第一位。
	 * 
	 * @param source
	 * @param target
	 * @param keepTime -1表示永久
	 */
	public static void makeFirstThreat(Unit source, Unit target, int keepTime) {
		GameObjectRef ft = target.getFirstThreat();
		if (ft != null) {
			float maxt = target.getThreat(ft);
			float curt = target.getThreat(source.ref());
			if (maxt >= curt) {
				addDamageThreat(source, target, maxt - curt + 0.001f, true);
				if (keepTime != -1) {
				    TempThreat tt = new TempThreat();
				    tt.target = target.ref();
				    tt.value = maxt - curt + 0.001f;
				    tt.endTime = Time.currTime + keepTime;
				    source.tempThreats.add(tt);
				}
			}
		}
	}

	/*
	 * 添加source对target的威胁值。
	 */
	public static void addDamageThreat(Unit source, Unit target, float value,
			boolean direct) {
		if (source != target) {
			//对于玩家间仇恨有这样一个规则,如果6秒内无直接仇恨将会清除仇恨,所以两者的仇恨类型得一样,不然如果一方清除仇恨必然导致另外一方仇恨清除
			if (source.type == GameObject.TYPE_PLAYER
					&& target.type == GameObject.TYPE_PLAYER) {
				target.addThreatUnit(source, value, direct);
				source.addThreatUnit(target, 0.0f, direct);
			} else if (direct) {
				target.addThreatUnit(source, value, true);
				source.addThreatUnit(target, 0.0f, false);
			} else {
			    if (target.containsThreat(source.ref())) {
			        target.addThreatUnit(source, value, false);
			    }
			}
		}
	}

	/*
	 * source对target进行治疗时，增加source对所有敌视target的目标的威胁值。
	 */
	public static void addHealThreat(Unit source, Unit target, float value,
			boolean direct) {
		for (GameObjectRef ref : target.getAllThreats()) {
			Unit u = (Unit) ObjectAccessor.getGameObject(ref);
			if (u != null) {
				u.addThreatUnit(source, value, direct);
				source.addThreatUnit(u, 0.0f, false);
			}
		}
	}

	protected byte getAttackResultType(GameObject target) {
		int r = RND.nextInt(100);
		if (r <= 70) {
			return AttackResult.TYPE_HIT;
		} else if (r <= 90) {
			return AttackResult.TPPE_MISS;
		} else
			return AttackResult.TYPE_DODGE;
	}

	protected int getDamage() {
		return RND.nextInt(10);
	}

	public int getSourceInstanceId() {
		if (sourceRef == null)
			return -1;
		return sourceRef.instanceId;
	}

	public int getTargetInstanceId() {
		if (targetRef == null)
			return -1;
		return targetRef.instanceId;
	}
}
