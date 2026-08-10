package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.PlayerTransaction;
import peony.game.Unit;

public class EmptyItemEffect implements ItemEffect {

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) {
	}
	
	public boolean isAsync(){
		return false;
	}

}
