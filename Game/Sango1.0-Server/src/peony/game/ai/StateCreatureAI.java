package peony.game.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.pip.sanguo.editor.ai.AIRuleConfig;
import com.pip.sanguo.editor.ai.AITargetType;
import com.pip.sanguo.editor.ai.EscapeRuleConfig;
import com.pip.sanguo.editor.ai.SkillAttackRuleConfig;
import com.pip.sanguo.editor.ai.SummonRuleConfig;
import com.pip.sanguo.editor.ai.WalkShoutRuleConfig;

import peony.game.Attack;
import peony.game.Creature;
import peony.game.CreatureAI;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.skill.Skill;
import peony.net.Packet;
import peony.service.feast.FeastInstanceService;

public class StateCreatureAI implements CreatureAI {
	
	private static final Logger log = Logger.getLogger(StateCreatureAI.class);

	protected Creature creature;
	protected int state = IDLE;
	
	protected static final int IDLE = 0;
	protected static final int PATROL = 1;
	protected static final int CHASE = 2;
	protected static final int ATTACK = 3;
	
	protected static final int PATROL_INIT = 0;
	protected static final int PATROL_IDLE = 1;
	protected static final int PATROL_RUNNING = 2;
	
	protected int backX,backY;
	protected int backState;
	
	protected int patrolIndex;
	protected int patrolIdleTime;
	protected int patrolState;
	protected int patrolNextX,patrolNextY;
	/*
     * 是否护送NPC AI。
     */
    protected boolean gohome = false;
    /*
     * 是否被动挨打不攻击。 
     */
    protected boolean passive = false;
	
	protected GameObjectRef lastChasingTarget;
	protected int lastAttackTime;
	private static final Random rnd = new Random();
	protected int lastChangeStateTime = 0;
	protected AIRule[] rules;
	protected PositionInfo pInfo;
	
	// 下面几个变量用来判断BOSS是否卡了
	protected int lastX, lastY;             // 上一cycle所在位置
	protected int idleCycleCount;           // 傻了多少cycle了
	
	protected int tick = 0;
	
	public static int[] SPECIAL_STOPTIME_CREATURE = {1310727,143364,458755};
	
	public static int[] SPECIAL_STOPTIME_CREATURE2= {880,881,882,883,884,885,893,888,887};
	
	public StateCreatureAI(Creature creature){
		this.creature = creature;
	}
	
	public void setGoHome(boolean value) {
	    gohome = value;
	}
	
	public void setPassive(boolean value) {
	    passive = value;
	}
	
	/**
	 * 配置战斗逻辑参数。
	 * @param cfg
	 */
	public void config(List<AIRuleConfig> cfg) {
	    List<AIRule> ruleList = new ArrayList<AIRule>();
	    for (AIRuleConfig rc : cfg) {
	        if (rc instanceof EscapeRuleConfig) {
	            EscapeRule rule = new EscapeRule(this, (EscapeRuleConfig)rc);
	            ruleList.add(rule);
	        } else if (rc instanceof SkillAttackRuleConfig) {
	            SkillAttackRule rule = new SkillAttackRule(this, (SkillAttackRuleConfig)rc);
	            ruleList.add(rule);
	        } else if (rc instanceof SummonRuleConfig) {
	            SummonRule rule = new SummonRule(this, (SummonRuleConfig)rc);
	            ruleList.add(rule);
	        } else if (rc instanceof WalkShoutRuleConfig) {
	            WalkShoutRule rule = new WalkShoutRule(this, (WalkShoutRuleConfig)rc);
	            ruleList.add(rule);
	        }
	    }
	    rules = new AIRule[ruleList.size()];
	    ruleList.toArray(rules);
	}
	
	public void addAIRule(AIRule rule){
		AIRule[] newRules = new AIRule[rule==null?1:rules.length+1];
		System.arraycopy(rules, 0, newRules, 0, rules.length);
		newRules[newRules.length-1] = rule;
		rules = newRules;
	}
	
	public void init() {
	    lastChasingTarget = null;
	    lastAttackTime = Time.currTime;
        lastX = creature.x;
        lastY = creature.y;
        idleCycleCount = 0;
        if(rules!=null){
        	for(AIRule rule:rules){
        		if(rule!=null){
        			rule.init();
        		}
        	}
        }
		if(needPatrol()){
			patrol();
		}else{
			idle();
		}
	}
	
	protected boolean needPatrol(){
		return creature.patrolPath.size()>1;
	}
	
	protected void patrol(){
		this.state = PATROL;
		creature.speed = creature.patrolSpeed;
		patrolState = PATROL_INIT;
		lastChangeStateTime = Time.currTime;
	}
	
	protected void idle(){
		this.state = IDLE;
		creature.idle();
		lastChangeStateTime = Time.currTime;
	}
	
	protected boolean isSpecialStopTimeCreature(int creatureId){
		for(int id : SPECIAL_STOPTIME_CREATURE){
			if(creatureId==id)
				return true;
		}
		return false;
	}
	
	public boolean isSpecialCreature2(Creature cre){
		for(int id : SPECIAL_STOPTIME_CREATURE2){
			if(cre.template.id==id && cre.getVMap().getId() == FeastInstanceService.MAPID)
				return true;
		}
		return false;
	}

	public void update() {
		tick++;
		switch(state){
		case IDLE:
			idleAction();
			break;
		case PATROL:
			patrolAction();
			break;
		case CHASE:
			chaseAction();
			break;
		case ATTACK:
			attackAction();
			break;
		}
	    if (rules != null) {
	        for (int i = rules.length - 1; i >= 0; i--) {
	            rules[i].update();
	        }
	    }
	    processPositionInfo();
		if (gohome && creature.cycle%10 == 0) {
			creature.moveType |= GameObject.MOVE_RUNNING_STATE;
		}
		
		// 判断BOSS是否卡住了。如果150个cycle之内，没有攻击动作，也没有移动出一个32x32的格子，
		// 则认为BOSS卡住了，直接传送回原点。
		if (this.state == CHASE || this.state == ATTACK) {
		    if ((lastX & 0xFFFFFFE0) == (creature.x & 0xFFFFFFE0) &&
		            (lastY & 0xFFFFFFE0) == (creature.y & 0xFFFFFFE0) &&
		            creature.attack == null && !creature.isChaosState()) {
		        // 没有移动出一个格子，并且也没有攻击
		        idleCycleCount++;
		        if (idleCycleCount >= 150) {
		            backState();
		            idleCycleCount = 0;
		        }
		    } else {
		        idleCycleCount = 0;
		    }
		} else {
            idleCycleCount = 0;
		}
        lastX = creature.x;
        lastY = creature.y;
	}
	
	protected void processPositionInfo() { //为了把发送position的包的频率降下来
		if (pInfo != null) {
			if (state == CHASE) {
				if ((Time.currTime - pInfo.time) > 2000) {
					int dx = Math.abs(creature.x-pInfo.x);
					int dy = Math.abs(creature.y-pInfo.y);
					if(dx>=16||dy>=16){
						creature.moveType |= GameObject.MOVE_RUNNING_STATE;
						pInfo.x = creature.x;
						pInfo.y = creature.y;
						pInfo.time = Time.currTime;
					}
				}
			}
		}
	}
	
	protected void backupPatrolInfo(){
		patrolNextX = creature.nextX;
		patrolNextY = creature.nextY;
	}
	
	protected void restorePatrolInfo(){
		creature.setNextPoint(patrolNextX, patrolNextY);
	}
	
	protected Unit hatedScan(){
		GameObjectRef hated = creature.getFirstThreat();
		Unit target = null;
		while(hated!=null){
			target = (Unit)ObjectAccessor.getGameObject(hated);
			
			// 如果第一目标消失，将其移出仇恨表
			if (target == null) {
				creature.removeThreatUnit(hated,true);
				hated = creature.getFirstThreat();
				continue;
			}
			
			// 如果第一目标逃出当前场景，将其移出仇恨表
			if (target.getVMap() != creature.getVMap()) {
				creature.removeThreatUnit(hated,true);
//				target.removeThreatUnit(creature.ref());
				hated = creature.getFirstThreat();
				continue;
			}
			
			// 如果第一目标不可攻击，将其移出仇恨表
			if (creature.canAttack(target) != 0) {
                creature.removeThreatUnit(hated,true);
                hated = creature.getFirstThreat();
                continue;
			}
			break;
		}
		return target;
	}
	
	protected Unit enemyScan(){
		if(tick % 5 != 0)
			return null;
		if(creature.eyeshot<=0){
			return null;
		}else{
			Unit target = getEnemyInRange(creature.eyeshot,8000);
			if(target!=null){
				Attack.addDamageThreat(target, creature, 0.0f, true);
				return target;
			}
			return null;
		}
	}
	
	protected void idleAction(){
//		System.out.println("idle");
	    if (passive) {
	        return;
	    }
		Unit target = hatedScan();
		if(target!=null){
			if(creature.inRange(target, creature.skill.getDistance(creature)-8)){ //如果在攻击范围内
				attack(target,creature.x,creature.y,IDLE,creature.skill);
//				if(target.type==GameObject.TYPE_PLAYER){
//					log.debug("IDLEATTACK["+creature.instanceId+"]");
//				}
			}else{
				chase(target,creature.x,creature.y,IDLE);
			}
		}else{
			enemyScan();
		}
	}
	
	protected void patrolAction(){
//		System.out.println("patrol");
	    if (passive) {
            continuePatrol();
	        return;
	    }
		Unit target = hatedScan();
		if(target!=null){
			if(creature.inRange(target,  creature.skill.getDistance(creature)-8)){ //如果在攻击范围内
				backupPatrolInfo();
//				if(target.type==GameObject.TYPE_PLAYER){
//					log.debug("PATROLATTACK["+creature.instanceId+"]");
//				}
				attack(target,creature.x,creature.y,PATROL,creature.skill);
			}else{
				backupPatrolInfo();
				chase(target,creature.x,creature.y,PATROL);
			}
		}else{
			if(enemyScan()==null){
				continuePatrol();
			}else{
				int i = 0;
			}
		}
	}
	
	protected int[] getNextPatrolPoint(int patrolIndex){
		patrolIndex++;
		if (patrolIndex >= creature.patrolPath.size()) {
			patrolIndex = 0;
		}
		return creature.patrolPath.get(patrolIndex);
	}
	
	protected void continuePatrol() {
		if(patrolState==PATROL_INIT){
//			System.out.println("init");
			int[] p = creature.patrolPath.get(patrolIndex);
			if(creature.x==p[0]&&creature.y==p[1]){
//				System.out.println("init to idle");
				patrolState = PATROL_IDLE;
				patrolIdleTime = Time.currTime;
				creature.stop(rnd.nextInt(360));
			}else{
//				System.out.println("init to running");
				patrolState = PATROL_RUNNING;
				creature.speed = creature.patrolSpeed;
				creature.go(true);
				int[] nextPoint = getNextPatrolPoint(patrolIndex);
				creature.setNextPoint(nextPoint[0], nextPoint[1]);
//				creature.moveType = creature.MOVE_RUNNING_STATE;
			}
		}
		else if(patrolState==PATROL_IDLE){
//			System.out.println("idle");
		    int delayTime = 30000;
		    if (this.gohome) {
		        delayTime = 5000;
		    }
		    if(isSpecialStopTimeCreature(creature.id) || isSpecialCreature2(creature))
		    	delayTime /= 10;
			if(Time.currTime-patrolIdleTime>=delayTime){
//				System.out.println("idle to running");
				patrolState = PATROL_RUNNING;
				creature.speed = creature.patrolSpeed;
				creature.go(true);
				int[] nextPoint = getNextPatrolPoint(patrolIndex);
				creature.setNextPoint(nextPoint[0], nextPoint[1]);
				if (gohome && patrolIndex >= creature.patrolPath.size() - 1) {
				    removeCreature();
				    return;
				}
//				creature.moveType = creature.MOVE_POINT_STATE;
			}
		}
		else if(patrolState==PATROL_RUNNING){
//			System.out.println("running");
			int[] nextPoint = getNextPatrolPoint(patrolIndex);
			if(creature.x==nextPoint[0]&&creature.y==nextPoint[1]){
//				System.out.println("running to idle");
				patrolState = PATROL_IDLE;
				patrolIdleTime = Time.currTime;
				creature.stop(rnd.nextInt(360));
				patrolIndex++;
				if (patrolIndex >= creature.patrolPath.size()) {
					patrolIndex = 0;
				}
			}
		}
	}
	
	protected void chaseAction() {
//		System.out.println("chase");
		Unit target = hatedScan();
		if (target != null) {
			if (outOfBattle()) {
				creature.removeThreatUnit(target.ref(),true);
			} else {
				if (creature.inRange(target,  creature.skill.getDistance(creature)-8)) {
					attack(target, backX, backY, backState, creature.skill);
//					if(target.type==GameObject.TYPE_PLAYER){
//						log.debug("CHASEATTACK["+creature.instanceId+"]");
//					}
				} else {
					continueChase(target);
				}
			}
		} else {
			back();
		}
	}
	
	protected boolean outOfChaseRange(){
		return Math.abs(backX - creature.x) > creature.chaseDistance
		|| Math.abs(backY - creature.y) > creature.chaseDistance;
	}
	
	/*
	 * 查找指定类型的目标。
	 * @param targetType 目标类型
	 * @param firstThread 当前第一仇恨
	 * @param dist 有效距离
	 */
	private Unit[] findTargets(AITargetType targetType, Unit firstThreat, int dist) {
	    switch (targetType.targetType) {
        case 0:    // 第一仇恨
            if (creature.inRange(firstThreat, dist)) {
                return new Unit[] { firstThreat };
            } else {
                return new Unit[0];
            }
        case -1:   // 仇恨表中随机目标 
        {
            GameObjectRef[] threats = creature.getAllThreats();
            List<Unit> validThreats = new ArrayList<Unit>();
            for (GameObjectRef ref : threats) {
                Unit unit = (Unit)ObjectAccessor.getGameObject(ref);
                if (unit != null && creature.inRange(unit, dist)) {
                    validThreats.add(unit);
                }
            }
            if (validThreats.size() == 0) {
                return null;
            }
            Unit cand = validThreats.get(rnd.nextInt(validThreats.size()));
            return new Unit[] { cand };
        }
        case -2:   // 仇恨表中除第一仇恨外的随机目标
        {
            GameObjectRef[] threats = creature.getAllThreats();
            List<Unit> validThreats = new ArrayList<Unit>();
            for (GameObjectRef ref : threats) {
                Unit unit = (Unit)ObjectAccessor.getGameObject(ref);
                if (unit != null && unit != firstThreat && creature.inRange(unit, dist)) {
                    validThreats.add(unit);
                }
            }
            if (validThreats.size() == 0) {
                return null;
            }
            Unit cand = validThreats.get(rnd.nextInt(validThreats.size()));
            return new Unit[] { cand };
        }
        case -3:   // 所有武将
        case -4:   // 所有刺客
        case -5:   // 所有谋士
        case -6:   // 所有方士
        {
            List<Unit> cands = new ArrayList<Unit>();
            GameObjectRef[] threats = creature.getAllThreats();
            for (GameObjectRef ref : threats) {
                Unit unit = (Unit)ObjectAccessor.getGameObject(ref);
                if (unit != null && unit.clazz == -targetType.targetType - 3 && creature.inRange(unit, dist)) {
                    cands.add(unit);
                }
            }
            Unit[] ret = new Unit[cands.size()];
            cands.toArray(ret);
            return ret;
        }
        case -7:   // 自己
            return new Unit[] { creature };
        default:   // 大于0表示第2，3，4仇恨
        {
            int index = targetType.targetType;
            GameObjectRef[] threats = creature.getAllThreats();
            if (threats.length <= index) {
                return null;
            }
            GameObjectRef cand = threats[index];
            Unit unit = (Unit)ObjectAccessor.getGameObject(cand);
            if (unit != null && creature.inRange(unit, dist)) {
                return new Unit[] { unit };
            } else {
                return null;
            }
        }
        }
	}
	
	/**
	 * 尝试用一个技能攻击指定目标。如果攻击启动成功，返回true。
	 */
	public boolean tryAttack(Skill newSkill, AITargetType targetType) {
	    // 混乱状态不能攻击
	    if (creature.isChaosState()) {
	        return false;
	    }
	    
	    // 检查蓝是不是够
	    if (creature.mp < newSkill.getMP(creature)) {
	        return false;
	    }
	        
	    // 查找攻击目标
	    Unit target = hatedScan();
	    if (target == null) {
	        // 仇恨表空了
	        return false;
	    }
        Unit[] targets = findTargets(targetType, target, newSkill.getDistance(creature));
	    if (targets == null || targets.length == 0) {
	        // 没有找到正确的技能目标
	        return false;
	    }
	    
	    // 如果当前不是攻击状态，则需要保存当前返会信息，并进入攻击状态
        if (state != ATTACK) {
            if (state == IDLE || state == PATROL) {
                this.backState = state;
                this.backX = creature.x;
                this.backY = creature.y;
                if(state==PATROL){
                	backupPatrolInfo();
                }
            }
            state = ATTACK;
            creature.stop();
            creature.moveType |= GameObject.MOVE_RUNNING_STATE;
            lastChangeStateTime = Time.currTime;
    		if (creature.chaseDistance > 0) {
    			if (!target.ref().equals(lastChasingTarget)) {
    				lastChasingTarget = target.ref();
    				Packet pt = new Packet(OpCode.CHASE_SERVER);
    				pt.putInt(creature.instanceId);
    				pt.putInt(target.instanceId);
    				pt.putShort(creature.x);
    				pt.putShort(creature.y);
    				pt.put(creature.getSpeed());
    				pt.putShort(creature.skill.getDistance(creature) - 8);
    				creature.broadcast(pt, null, null, false, false,false);
    			}
    		}
        }
        lastAttackTime = Time.currTime;
        
        // 向选中的目标施放攻击
        creature.prepareSkillAttack(targets, newSkill);
	    return true;
	}
	
    /**
     * 删除宿主怪物。通常用于护送NPC消失。
     */
    public void removeCreature() {
        creature.removeFromMap();
        ObjectAccessor.removeGameObject(creature);
    }
    
	protected void attackAction(){
		if(creature.attack==null){
			Unit target = hatedScan();
			if(target != null){
				if(!creature.inRange(target,  creature.skill.getDistance(creature)-8)){
//					System.out.println("chaseattack");
					chase(target,backX,backY,backState);
				}else{
					if(Time.currTime-lastAttackTime>3000){
//						System.out.println("continueattack");
						continueAttack(target,creature.skill);
					}
				}
			}else{
				back();
			}
		}
	}
	
	protected void continueChase(Unit target){
		int[] p = creature.getVMap().mapDef.mapInfo.getPathFinder().findPath(creature.x,
				creature.y, target.x, target.y);
		if (p[0] == creature.x && p[1] == creature.y) {
		    back();
		    return;
		}
//		if(p[0]<0||p[1]<0)
//			log.debug("point:"+p[0]+"-"+p[1]);
		creature.setNextPoint(p[0], p[1]);
		if (!target.ref().equals(lastChasingTarget)) {
            lastChasingTarget = target.ref();
            Packet pt = new Packet(OpCode.CHASE_SERVER);
            pt.putInt(creature.instanceId);
            pt.putInt(target.instanceId);
            pt.putShort(creature.x);
            pt.putShort(creature.y);
            pt.put(creature.getSpeed());
            pt.putShort(creature.skill.getDistance(creature)-8);
            creature.broadcast(pt,null,null,false,false,false);
//			if(target.type==GameObject.TYPE_PLAYER){
//				log.debug("CHASE["+creature.instanceId+"]");
//			}
        }
	}
	
	protected void continueAttack(Unit target,Skill skill){
		creature.stop();
		lastAttackTime = Time.currTime;
		if(!creature.isChaosState()){
			creature.prepareSkillAttack(target, skill, 0);
		}
		if (outOfBattle()) {
			back();
		}
	}
	
	protected void chase(Unit target,int x,int y,int backState){
		this.backState = backState;
		backX = x;
		backY = y;
		if (creature.chaseDistance == 0) {
			back();
			return;
		} else {
			if (outOfBattle()) {
				back();
				return;
			}
		}
		state = CHASE;
		creature.speed = creature.chaseSpeed;
		creature.go(false);
		lastChangeStateTime = Time.currTime;
		pInfo = new PositionInfo(x,y,Time.currTime);
		if (!target.ref().equals(lastChasingTarget)) {
		    lastChasingTarget = target.ref();
    		Packet pt = new Packet(OpCode.CHASE_SERVER);
    		pt.putInt(creature.instanceId);
    		pt.putInt(target.instanceId);
    		pt.putShort(creature.x);
    		pt.putShort(creature.y);
    		pt.put(creature.getSpeed());
    		pt.putShort(creature.skill.getDistance(creature)-8);
    		creature.broadcast(pt, null ,null,false,false,false);
//			if(target.type==GameObject.TYPE_PLAYER){
//				log.debug("CHASE["+creature.instanceId+"]");
//			}
		}
	}
	
	protected boolean outOfBattle(){
		if(creature.threatGroup==null){
			return canOutOfBattle();
		}else{
			return creature.threatGroup.outOfBattle();
		}
	}
	
	public boolean canOutOfBattle(){
	    boolean b1 = outOfChaseRange();
	    if (b1) {
	        boolean b2 = !creature.threats.isLastDirectThreatTimeInRange(5000);
	        return b2;
	    } else {
	        return false;
	    }
	}
	
	protected void attack(Unit target, int x, int y, int backState, Skill skill) {
		this.backState = backState;
		state = ATTACK;
		backX = x;
		backY = y;
		lastAttackTime = Time.currTime;
		creature.stop();
		creature.speed = creature.chaseSpeed;
		if (!creature.isChaosState())
			creature.prepareSkillAttack(target, creature.skill, 0);
		creature.moveType |= GameObject.MOVE_RUNNING_STATE;
		lastChangeStateTime = Time.currTime;
		if (creature.chaseDistance > 0) {
			if (!target.ref().equals(lastChasingTarget)) {
				lastChasingTarget = target.ref();
				Packet pt = new Packet(OpCode.CHASE_SERVER);
				pt.putInt(creature.instanceId);
				pt.putInt(target.instanceId);
				pt.putShort(creature.x);
				pt.putShort(creature.y);
				pt.put(creature.getSpeed());
				pt.putShort(creature.skill.getDistance(creature) - 8);
				creature.broadcast(pt, null,null, false, false,false);
//				if(target.type==GameObject.TYPE_PLAYER){
//					log.debug("CHASE["+creature.instanceId+"]");
//				}
			}
		}
	}
	
	protected Unit getEnemyInRange(int dist, int lag) {
		VMap map = creature.getVMap();
		return map.getNearestEnemy(creature, dist);
	}
	
	protected void back(){
		if(creature.threatGroup==null)
			backState();
		else
			creature.threatGroup.backState();
	}
	
	public void backState(){
		creature.threatGroup = null;
	    creature.setAttack(null);
		creature.backState(backX, backY);
		int oldState = state;
		if(backState==IDLE){
			idle();
		}else if(backState==PATROL){
			restorePatrolInfo();
//			creature.runToNextPointTime += (Time.currTime - lastChangeStateTime);
			patrol();
		} else {
		    throw new IllegalArgumentException();
		}
//		if(lastChasingTarget!=null&&lastChasingTarget.type==GameObject.TYPE_PLAYER){
//			log.debug("BACKCHASE["+creature.instanceId+"]");
//		}
		lastChasingTarget = null;
		pInfo = null;
		Packet pt = new Packet(OpCode.CHASE_SERVER);
		pt.putInt(creature.instanceId);
		pt.putInt(-1);
		pt.putShort(creature.x);
		pt.putShort(creature.y);
		pt.put(creature.getSpeed());
		// pt.putShort(creature.skill.getDistance(creature) - 8);
		pt.putShort(0);
		creature.broadcast(pt, null, null, false, false, false);
		if(creature.disappearTime == -1){
			creature.removeFromWorld();
		}
	}
}

class PositionInfo{
	public int x,y,time;
	
	public PositionInfo(int x,int y,int time){
		this.x = x;
		this.y = y;
		this.time = time;
	}
}
