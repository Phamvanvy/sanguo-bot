package peony.game.skill;

import peony.game.CombatContext;
import peony.game.CombatEffect;
import peony.game.buff.StayDebuff;

/**
 * 辅助技能：定身。
 * @author lighthu
 */
public class StaySkill extends AbstractSkill {
	/*
	 * 持续时间(毫秒)
	 */
	protected int time;
	
	public StaySkill(int time) {
		super(0, "", 0);
		this.time = time;
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
				// 命中后给对方加定身DEBUFF
				if (context.hited()) {
					context.target.buffs.addBuff(new StayDebuff(context.source, time));
				}
			}
		};
	}
}
