package peony.game.skill;

import peony.game.CombatContext;
import peony.game.CombatEffect;

/**
 * 固定回血临时技能，用于实现攻击/被攻击触发回血。
 * @author lighthu
 */
public class FixedHealSkill extends AbstractSkill {
	protected int amount;
		
	public FixedHealSkill(int amount) {
		super(0, "", 0);
		this.amount = amount;
		type = TYPE_AID;
		targetType = TARGET_AID_SELF;
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
				context.damageType = CombatContext.DAMAGE_HEAL;
			}
			
			public void postHit(CombatContext context, boolean isActive) {}
			
			public void preDamage(CombatContext context, boolean isActive) {
				// 固定回血值
				context.attackPower = amount;
			}

            public void postDamage(CombatContext context, boolean isActive) {}
            
            public void finished(CombatContext context, boolean isActive) {}
		};
	}
}
