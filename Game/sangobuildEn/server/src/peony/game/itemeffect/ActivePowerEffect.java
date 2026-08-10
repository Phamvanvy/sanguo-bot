package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Time;
import peony.game.Unit;
import peony.game.UseItemException;

/**
 * 恢复行动力。
 * @author lighthu
 */
public class ActivePowerEffect implements ItemEffect {
	protected int amount;
	
	public ActivePowerEffect(int value) {
		this.amount = value;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("錯誤的目標");
		if(!(target instanceof Player))
			throw new UseItemException("錯誤的目標");
		Player p = (Player)source;
		if (p.activePower >= 100) {
			throw new UseItemException("你的行動力已經滿了");
		}
		p.addActivePower(amount);
	}
	
	public boolean isAsync(){
		return false;
	}
}
