package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

public class ReliveItemEffect implements ItemEffect {

	protected int percent; //15%==15
	
	public ReliveItemEffect(int percent){
		this.percent = percent;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("錯誤的目標");
		if(target.isAlive()){
			throw new UseItemException("目標已經复活");
		}
		int hp = target.maxhp*percent/100;
		int mp = target.maxmp*percent/100;
		target.relive(hp, mp);
	}

	public boolean isAsync(){
		return false;
	}
}
