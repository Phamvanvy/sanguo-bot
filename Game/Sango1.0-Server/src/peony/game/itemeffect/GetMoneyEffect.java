package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

/**
 * »ñµÃ½ðÇ®¡£
 * @author lighthu
 */
public class GetMoneyEffect implements ItemEffect {
    protected float amount;
    protected int[] valueTable;
	
	public GetMoneyEffect(float amount, int[] valueTable) {
	    this.amount = amount;
	    this.valueTable = valueTable;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		int addValue;
		if (valueTable != null) {
            addValue = valueTable[source.level - 1];
        } else if (amount >= 0) {
		    addValue = (int)amount;
		} else {
		    addValue = (int)(-(amount * source.level));
		}
		p.addMoney(addValue, tx, true);
	}
	
	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
