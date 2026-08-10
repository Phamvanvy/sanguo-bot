package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

public class RideItemEffect implements ItemEffect {

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
//		if(source!=target){
//			return ERROR_TARGET;
//		}
//		Player p = (Player)source;
//		if(!p.isRide())
//			p.ride();
//		else
//			p.unRide();
//		return OK;
	}

	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
