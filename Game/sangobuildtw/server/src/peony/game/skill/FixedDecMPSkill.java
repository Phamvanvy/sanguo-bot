package peony.game.skill;

import peony.game.CombatContext;
import peony.game.CombatEffect;

/**
 * 吸蓝临时技能。
 * @author lighthu
 */
public class FixedDecMPSkill extends AbstractSkill {
	protected int amount;
		
	public FixedDecMPSkill(int amt) {
		super(0, "", 0);
		amount = amt;
		type = TYPE_ATTACK;
		targetType = TARGET_SINGLE_ATTACK;
        prepareAnimation = -1;
        castAnimation = -1;
        hitAnimation = 1;
	}
	
	public int getRequireLevel() {
		return 0;
	}
	
	@Override
	protected CombatEffect createActEffect() {
		return new CombatEffect() {
			public void preHit(CombatContext context, boolean isActive) {
				// 设置伤害类型
				context.damageType = CombatContext.DAMAGE_DECMP;
			}
			
			public void postHit(CombatContext context, boolean isActive) {
                // 因为是衍生效果，已经计算过命中率了，所以这里设为必中
                context.attackResult = CombatContext.ATTACKRESULT_HIT;
            }
			
			public void preDamage(CombatContext context, boolean isActive) {
				// 固定抽蓝
				context.attackPower = amount;
			}

            public void postDamage(CombatContext context, boolean isActive) {}
            
            public void finished(CombatContext context, boolean isActive) {}
		};
	}
}
