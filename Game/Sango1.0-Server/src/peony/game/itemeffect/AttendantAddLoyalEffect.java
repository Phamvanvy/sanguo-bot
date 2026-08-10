package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.attendant.Attendant;

public class AttendantAddLoyalEffect implements ItemEffect{
	
	protected int loyal;
	
	public AttendantAddLoyalEffect(int loyal){
		this.loyal = loyal;
	}

	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if (!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if (!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player player = (Player) target;
		if(player != null){
			Attendant attendant = player.attendant;
			if(attendant!=null){
				int oldValue = attendant.loyal;
				int value = oldValue + 100;
				if(oldValue>0 && value<0){
					throw new UseItemException(peony.Messages.STRING_00098);
				}
				attendant.setLoyal(value);
			} else{
				throw new UseItemException(peony.Messages.STRING_00532);
			}
		}
	}
	
	public boolean isAsync() {
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}

}
