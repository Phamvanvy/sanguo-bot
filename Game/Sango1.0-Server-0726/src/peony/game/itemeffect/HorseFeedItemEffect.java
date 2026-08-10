package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

public class HorseFeedItemEffect implements ItemEffect {

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		if(source.horse==null)
			throw new UseItemException("指定的马不存在");
		if(source.horse.degree==100)
			throw new UseItemException("马已经在最大饱食度");
		source.horse.feed(item, (Player)source, -1);
	}

	public boolean isAsync(){
		return false;
	}
}
