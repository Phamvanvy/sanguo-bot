package peony.game.skill;

import peony.game.CombatContext;
import peony.game.CombatEffect;

/**
 * 辅助技能：打断施法。
 * @author lighthu
 */
public class BreakAttackSkill extends AbstractSkill {
	public BreakAttackSkill() {
		super(0, "", 0);
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
				// 设置伤害类型为DEBUFF
				context.damageType = CombatContext.DAMAGE_DEBUFF;
			}
			
			public void postHit(CombatContext context, boolean isActive) {
                // 因为是衍生效果，已经计算过命中率了，所以这里设为必中
                context.attackResult = CombatContext.ATTACKRESULT_HIT;
            }
			
            public void preDamage(CombatContext context, boolean isActive) {}

            public void postDamage(CombatContext context, boolean isActive) {}

            public void finished(CombatContext context, boolean isActive) {
				// 命中后打断对方当前施法
				if (context.hited()) {
				    context.target.breakAttack();
				}
			}
		};
	}
}
