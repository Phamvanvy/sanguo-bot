package peony.game.skill;

import peony.game.CombatContext;
import peony.game.CombatEffect;
import peony.game.Player;
import peony.game.buff.FearDebuff;

/**
 * 辅助技能：恐惧。
 * @author lighthu
 */
public class FearSkill extends AbstractSkill {
	/*
	 * 持续时间(毫秒)
	 */
	protected int time;
	
	public FearSkill(int time) {
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
				// 命中后给对方加恐惧DEBUFF
				if (context.hited()) {
					if(context.target instanceof Player){
						Player p = (Player)context.target;
				    	if(p.timeRatio[1]!=1.0f){
				    		time = Math.round(time*p.timeRatio[1]);
				    	}
					}
					context.target.buffs.addBuff(new FearDebuff(context.source, time));
				}
			}
		};
	}
}
