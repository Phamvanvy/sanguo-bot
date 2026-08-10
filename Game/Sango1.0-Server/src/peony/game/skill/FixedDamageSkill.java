package peony.game.skill;

import peony.game.CombatContext;
import peony.game.CombatEffect;

/**
 * 固定伤害临时技能，用于实现反弹伤害。此技能造成的伤害无视防御。
 * @author lighthu
 */
public class FixedDamageSkill extends AbstractSkill {
	protected int damageType;
	protected int damage;
		
	public FixedDamageSkill(int tp, int dmg) {
		super(0, "", 0);
		damageType = tp;
		damage = dmg;
		type = TYPE_ATTACK;
		targetType = TARGET_SINGLE_ATTACK;
		prepareAnimation = -1;
		castAnimation = -1;
		hitAnimation = -1;
	}
	
	public int getRequireLevel() {
		return 0;
	}
	
	@Override
	protected CombatEffect createActEffect() {
		return new CombatEffect() {
			public void preHit(CombatContext context, boolean isActive) {
				// 设置伤害类型
				context.damageType = damageType;
			}
			
			public void postHit(CombatContext context, boolean isActive) {}
			
			public void preDamage(CombatContext context, boolean isActive) {}
			
			public void postDamage(CombatContext context, boolean isActive) {
				// 固定伤害，无视防御
				context.damage = damage;
				context.threat = damage;
			}
			
			public void finished(CombatContext context, boolean isActive) {}
		};
	}
}
