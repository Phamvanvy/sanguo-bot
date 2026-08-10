package peony.game.itemeffect;

import java.text.MessageFormat;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.attendant.AttendantFixService;
import peony.game.changed.ChangedItem;

/**
 * 获得荣誉（战功）。
 * @author lighthu
 */
public class GetHonorEffect implements ItemEffect {
    protected float amount;
    protected int[] valueTable;
	
	public GetHonorEffect(float amount, int[] valueTable) {
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
		p.addCredit(addValue, tx, true);
	}
	
	public void bulkUseItem(Player p, int count, PlayerTransaction tx) throws UseItemException {
		int addValue;
		if (valueTable != null) {
            addValue = valueTable[p.level - 1];
        } else if (amount >= 0) {
		    addValue = (int)amount;
		} else {
		    addValue = (int)(-(amount * p.level));
		}
		addValue = addValue * count;
		p.addCredit(addValue, tx, true);
	}
	
	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
