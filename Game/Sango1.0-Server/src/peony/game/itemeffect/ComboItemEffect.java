package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

public class ComboItemEffect implements ItemEffect {

	protected ItemEffect[] effects;
	
	public ComboItemEffect(ItemEffect[] effects){
		this.effects = effects;
	}
	
	public void use(Unit source, GameItem item, Unit target,PlayerTransaction tx) throws UseItemException{
		for(int i=0;i<effects.length;i++){
			effects[i].use(source, item, target, tx);
		}
	}

	public boolean isAsync(){
		for(int i=0;i<effects.length;i++){
			if(effects[i].isAsync())
				return true;
		}
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
