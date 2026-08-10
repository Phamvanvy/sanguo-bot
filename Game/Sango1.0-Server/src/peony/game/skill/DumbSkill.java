package peony.game.skill;

import peony.game.CombatContext;
import peony.game.CombatEffect;
import peony.game.CommonUtil;
import peony.game.Player;
import peony.game.buff.Buff;
import peony.game.buff.DumbDebuff;

/**
 * 辅助技能：沉默。
 * @author lighthu
 */
public class DumbSkill extends AbstractSkill {
	/*
	 * 持续时间(毫秒)
	 */
	protected int time;
	
	public DumbSkill(int time) {
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
                if(context.source.buffs.getBuffByID(51)!=null){//只有以静制动触时才计算
                	boolean hit=true;
                	float rate=0;
                	for(Buff buff:context.target.buffs.getBuffs()){
                    	if(buff!=null){
                    		if(buff.getId()==703||buff.getId()==715||buff.getId()==714){
//                    			System.out.println("------------------------:"+buff.getchange_dembuff_rate());
                    			rate+=buff.getchange_dembuff_rate();
                    		}
                    	}
                    }
                	hit = CommonUtil.hit(RND, (int)(100 * (100-rate)), 10000);
                	if(!hit){
//                		System.out.println("========================================");
                		context.attackResult = CombatContext.ATTACKRESULT_IMMUNE;
                	}
                }
            }
			
			public void preDamage(CombatContext context, boolean isActive) {}

            public void postDamage(CombatContext context, boolean isActive) {}
			
			public void finished(CombatContext context, boolean isActive) {
				// 命中后给对方加沉默DEBUFF
				if (context.hited()) {
					if(context.target instanceof Player){
						Player p = (Player)context.target;
				    	if(p.timeRatio[0]!=1.0f){
				    		time = Math.round(time*p.timeRatio[0]);
				    	}
					}
//					System.out.println("++++++++++++++++++++++++++++++++++++++++++");
					context.target.buffs.addBuff(new DumbDebuff(context.source, time));
				}
			}
		};
	}
}
