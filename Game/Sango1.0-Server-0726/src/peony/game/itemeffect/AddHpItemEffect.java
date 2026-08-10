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
			throw new UseItemException("错误的目标");
		if(target.hp==target.maxhp)
			throw new UseItemException("当前已经处于满血状态");
		target.setHp(Math.min(target.hp+value, target.maxhp), true);
	}
	
	public boolean isAsync(){
		return false;
	}
}
