package peony.game.skill;

import peony.game.CombatContext;
import peony.game.CombatEffect;
import peony.game.EquipmentTemplate;
import peony.game.GameObject;
import peony.game.Unit;
import peony.game.buff.Buff;
import peony.game.skill.AbstractSkill;

/**
 * 自动攻击技能。
 * @author lighthu
 */
public class AutoAttackSkill extends AbstractSkill {
	public AutoAttackSkill(int level) {
		super(0, peony.Messages.STRING_00880, level);
		if (level > 1) {
			throw new IllegalArgumentException();
		}
		setupSingleAttack(Unit.CLASS_1, 0, 1000, 0, 1000, 5, 0);
		desc = peony.Messages.STRING_00881;
        prepareAnimation = -1;
        castAnimation = 0;
        hitAnimation = 1;
	}
	
    @Override
	public int getPrepareAnimation(Unit src) {
    	if(src != null){
	        if (src.clazz == Unit.CLASS_3 || src.clazz == Unit.CLASS_4) {
	            // 法术攻击
	            return (-1 << 16) | 2;
	        } else if(src.clazz == Unit.CLASS_2){
	            // 刺客，判断武器确定攻击动画
	            int ret = (-1 << 16) | 0;
	            if (src.equipments == null || src.equipments.getWeapon() == null) {
	                return ret;
	            }
	            int type = src.equipments.getWeapon().template.equipment.minorType;
	            if (type == EquipmentTemplate.MINORTYPE_BOW) {
	                ret = (-1 << 16) | 1;
	            }
	            return ret;
	        } else {
	            // 物理攻击
	            return (-1 << 16) | 0;
	        }
    	} 
    	return 0;
    }
    
    @Override
	public int getCastAnimation(Unit src) {
        if (src.clazz == Unit.CLASS_3 || src.clazz == Unit.CLASS_4) {
            // 法术攻击
            return (-1 << 16) | 2;
        } else if(src.clazz == Unit.CLASS_2){
            // 刺客，判断武器确定攻击动画
            int ret = (-1 << 16) | 0;
            if (src.equipments == null || src.equipments.getWeapon() == null) {
                return ret;
            }
            int type = src.equipments.getWeapon().template.equipment.minorType;
            if (type == EquipmentTemplate.MINORTYPE_BOW) {
                ret = (-1 << 16) | 1;
            }
            return ret;
        } else {
            // 物理攻击
            return (-1 << 16) | 0;
        }
    }

    @Override
	public int getHitAnimation(Unit src) {
        if (src.clazz == Unit.CLASS_3 || src.clazz == Unit.CLASS_4) {
            // 法术攻击
            return 1;
        }
        else if(src.clazz == Unit.CLASS_2){
            // 刺客，判断武器确定攻击动画
            int ret = 2;
            if (src.equipments == null || src.equipments.getWeapon() == null) {
                return ret;
            }
            int type = src.equipments.getWeapon().template.equipment.minorType;
            if (type == EquipmentTemplate.MINORTYPE_BOW) {
                ret = 3;
            }
            return ret;
        }
        else {
            // 物理攻击
            return 2;
        }
    }
	
	@Override
	public int getDistance(Unit owner) {
		if(owner!=null){
			int realDist = 5 * 8;
			if (owner.clazz == Unit.CLASS_3 || owner.clazz == Unit.CLASS_4) {
				realDist = 13 * 8;
			} else if (owner.clazz == Unit.CLASS_2) {
				if (owner.type == GameObject.TYPE_PLAYER && owner.equipments != null
						&& owner.equipments.getWeapon() != null
						&& owner.equipments.getWeapon().template.equipment.minorType == EquipmentTemplate.MINORTYPE_BOW) {
					realDist = 13 * 8;
				} else if (owner.type == GameObject.TYPE_CREATURE) {
					realDist = 13 * 8;
				}
			}
			return (int) owner.buffs.updateDistance(this, realDist);
			}
		return 0;
	}
	
	public int getRequireLevel() {
		return 1;
	}
	
	@Override
	protected CombatEffect createActEffect() {
		return new CombatEffect() {
			public void preHit(CombatContext context, boolean isActive) {
				// 武将和刺客用物攻，谋士和方士用法攻
				if (context.source.clazz == Unit.CLASS_3 || context.source.clazz == Unit.CLASS_4) {
					context.damageType = CombatContext.DAMAGE_MAGIC;
				} else {
				    context.damageType = CombatContext.DAMAGE_PHYSICAL;
				}
			}
			
            public void postHit(CombatContext context, boolean isActive) {}

            public void preDamage(CombatContext context, boolean isActive) {}
            
			public void postDamage(CombatContext context, boolean isActive) {}

		    public void finished(CombatContext context, boolean isActive){}
		};
	}
	
	@Override
	public Buff newBuff() {
		return null;
	}
}
