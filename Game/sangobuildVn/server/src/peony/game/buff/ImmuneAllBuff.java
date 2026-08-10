package peony.game.buff;

import peony.game.CombatContext;
import peony.game.CombatEffect;
import peony.game.GameObjectRef;
import peony.game.Unit;
import peony.game.skill.BreakAttackSkill;
import peony.game.skill.DumbSkill;
import peony.game.skill.FearSkill;
import peony.game.skill.ParalyzeSkill;
import peony.game.skill.StaySkill;

public class ImmuneAllBuff implements Buff, CombatEffect  {
	public static final int IMMUNE_BUFFID = 10011;
	
	protected int instanceID;
	
	public ImmuneAllBuff(){
		instanceID = BuffUtil.getNextID();
	}

    // 用于从数据库load时创建buff对象
    public ImmuneAllBuff(int lvl, Unit src, Unit tgt, int dmg) {
        instanceID = BuffUtil.getNextID();
    }
	
	public void finished(CombatContext context, boolean isActive) {
	}

	public void postDamage(CombatContext context, boolean isActive) {
	}

	public void postHit(CombatContext context, boolean isActive) {
		if(!isActive){
			if(context.hited()){
				if(context.skill instanceof FearSkill||context.skill instanceof DumbSkill||context.skill instanceof ParalyzeSkill||context.skill instanceof StaySkill||context.skill instanceof BreakAttackSkill)
					context.attackResult = CombatContext.ATTACKRESULT_IMMUNE;
			}
		}
	}

	public void preDamage(CombatContext context, boolean isActive) {
		
	}

	public void preHit(CombatContext context, boolean isActive) {

	}
	
	public boolean dispelable() {
		return true;
	}

    /**
     * 是否死亡后保持。
     */
    public boolean keepOnDie() {
        return false;
    }

	public String getDesc() {
		return "免疫不良状态";
	}

	public int getEndTime() {
		return -1;
	}

	public int getIconID() {
		return 38;
	}

	public int getId() {
		return IMMUNE_BUFFID;
	}

	public int getInstanceID() {
		return instanceID;
	}

	public String getName() {
		return "Miễn dịch";
	}

	public boolean isAreaBuff() {
		return false;
	}

	public boolean isGood() {
		return true;
	}

	public boolean isNeedMerge() {
		return true;
	}

	public boolean merge(Buff buff) {
		if(buff instanceof GodBuff){
			return true;
		}
		return false;
	}

	public GameObjectRef getSource() {
	    return null;
	}

	/**
     * 设置BUFF所有者。在BUFF加到一个对象上时需要设置。
     */
    public void setOwner(GameObjectRef o) {
    }
    
    /**
     * 刷新BUFF属性。当BUFF所有者获得或失去一个ParamEnhancer类型的BUFF时调用以重算基本属性。
     */
    public void resetParams(Unit owner) {
    } 
}
