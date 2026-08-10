package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

/**
 * 获得声望。
 * @author lighthu
 */
public class GetCreditEffect implements ItemEffect {
    protected float amount;
    protected int[] valueTable;
	
	public GetCreditEffect(float amount, int[] valueTable) {
	    this.amount = amount;
	    this.valueTable = valueTable;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("錯誤的目標");
		if(!(target instanceof Player))
			throw new UseItemException("錯誤的目標");
		Player p = (Player)source;
		int addValue;
		if (valueTable != null) {
		    addValue = valueTable[source.level - 1];
		} else if (amount >= 0) {
		    addValue = (int)amount;
		} else {
		    addValue = (int)(-(amount * source.level));
		}
		p.addHonor(addValue, tx, true);
	}
	
	public boolean isAsync(){
		return false;
	}
}
