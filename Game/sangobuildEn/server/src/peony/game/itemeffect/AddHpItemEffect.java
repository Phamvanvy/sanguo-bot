package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

public class AddHpItemEffect implements ItemEffect {

	protected int value;
	
	public AddHpItemEffect(int value){
		this.value = value;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("錯誤的目標");
		if(target.hp==target.maxhp)
			throw new UseItemException("當前已經處于滿血狀態");
		target.setHp(Math.min(target.hp+value, target.maxhp), true);
	}
	
	public boolean isAsync(){
		return false;
	}
}
