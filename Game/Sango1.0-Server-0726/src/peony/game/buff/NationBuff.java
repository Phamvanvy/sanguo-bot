package peony.game.buff;

import peony.game.*;
import peony.service.expansionbattle.ExpansionInstance;

public class NationBuff implements Buff,PropertyEnhancer{
	
	protected float expratio,defense,antiCrti;
	protected int instanceId;
	
	public NationBuff(float expratio,float defense,float antiCrti){
		this.instanceId = BuffUtil.getNextID();
		this.expratio = expratio;
		this.defense = defense;
		this.antiCrti = antiCrti;
	}

	public boolean dispelable() {
		return false;
	}

	public String getDesc() {
//		本周内护甲值和法术防御提高XX%
//		本周内经验获得速度提高了XX%
//		本周内免暴率提升了XX%
		StringBuilder sb = new StringBuilder(200);
		if(defense!=0.0f){
			sb.append(String.format("本周内护甲值和法术防御提高%3.1f%%",defense*100));
		}
		if(antiCrti!=0.0f){
			sb.append(String.format("本周内免暴率提升%3.1f%%",antiCrti*100));
		}
		if(expratio!=0.0f){
			sb.append(String.format("本周内经验获得速度提高%3.1f%%",expratio*100));
		}
		return sb.toString();
	}

	public int getEndTime() {
		return -1;
	}

	public int getIconID() {
		return 74;
	}

	public int getId() {
		return 1005;
	}

	public int getInstanceID() {
		return instanceId;
	}

	public String getName() {
		return "振兴";
	}

	public GameObjectRef getSource() {
		return null;
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

	public boolean keepOnDie() {
		return true;
	}

	public boolean merge(Buff buff) {
		if(buff instanceof NationBuff){
			return true;
		}else
			return false;
	}

	public void resetParams(Unit owner) {
		
	}

	public void setOwner(GameObjectRef o) {
		
	}

	public void enhance(PropertyCalculator pc) {
	    // 战场和副本无效
	    if (pc.unit instanceof Player) {
	        if (pc.unit.map.map != null && pc.unit.map.map.instance != null) {
	        	if(!(pc.unit.map.map.instance instanceof ExpansionInstance))
	        		return;
	        }
	    }
		pc.expRatio += expratio;
		pc.defenseRate += defense;
		pc.spellDefenseRate += defense;
		pc.anticrit += antiCrti;
	}

}
