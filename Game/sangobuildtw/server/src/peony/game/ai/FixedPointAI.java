package peony.game.ai;

import java.util.List;
import java.util.Random;

import peony.game.Creature;
import peony.game.CreatureAI;
import peony.game.GameObject;
import peony.game.GameObjectFilter;
import peony.game.Player;
import peony.game.Time;
import peony.game.Unit;
import peony.game.skill.Skill;

/**
 * 固定位置的怪物的AI,比如箭塔
 * @author Jeffrey
 *
 */
public class FixedPointAI implements CreatureAI{

	protected int lastAttackTime;
	protected Creature creature;
	protected int state;
	protected Random rnd = new Random();
	
	protected GameObjectFilter filter = new EnemyFilter();
	
	public FixedPointAI(Creature creature){
		this.creature = creature;
	}
	
	public void backState() {
		creature.threatGroup = null;
		creature.battleContribList = null;
	    creature.setAttack(null);
	}

	public boolean canOutOfBattle() {
		return false;
	}

	public void init() {
		
	}

	public void update() {
		if(creature.attack == null && Time.currTime - lastAttackTime >= 3000){
			List<GameObject> objects = creature.getVMap().filter(filter);
			if(!objects.isEmpty()){
				int c = rnd.nextInt(objects.size());
				Unit u = (Unit)objects.get(c);
				attack(u,creature.skill);
			}
		}
	}
	
	protected void attack(Unit target,Skill skill){
		lastAttackTime = Time.currTime;
		creature.prepareSkillAttack(target, skill, 1000);
	}
	
	class EnemyFilter implements GameObjectFilter{

		public boolean apply(GameObject unit) {
			if (unit.isVisibleAndAlive()) {
				if (creature.minorFaction == 0) {
					return creature.isEnemy(unit);
				} else {
					if (unit instanceof Unit) {
						Unit u = (Unit) unit;
						boolean b = creature.minorFaction != u.minorFaction;
						if(b && u instanceof Player){
							Player p = (Player)u;
							if(p.systemState == Player.SYSTEMSTATE_READY){
								return p
										.inRange(creature, creature.eyeshot);
							}else{
								return false;
							}
						}else{
							if(b)
								return u.inRange(creature, creature.eyeshot);
							else
								return false;
						}
					} else {
						return false;
					}
				}
			}else{
				return false;
			}
		}
		
	}

}
