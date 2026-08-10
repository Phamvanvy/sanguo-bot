package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

public class UseDecItemEffect implements ItemEffect {

	protected int itemId;
	
	public UseDecItemEffect(int itemId){
		this.itemId = itemId;
	}
	
	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		GameItem decItem = p.bag.removeGameItemIngoreInstanceId(itemId, 1, tx, true);
		if(decItem==null){
			throw new UseItemException(peony.Messages.STRING_01013);
		}
	}
	
	public boolean needRemove() {
		return false;
	}

}
