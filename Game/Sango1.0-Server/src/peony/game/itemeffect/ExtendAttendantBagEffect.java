package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.attendant.AttendantBag;
import peony.game.changed.ChangedItem;

/**
 * À©Õ¹Ëæ´ÓÀ¸
 * @author dchen
 */
public class ExtendAttendantBagEffect implements ItemEffect {

	private int extendCount;
	
	public ExtendAttendantBagEffect(int extendCount){
		this.extendCount = extendCount;
	}
	
	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player player = (Player)source;
		if(player!=null){
			if(player.attendantBag.maxSize>=AttendantBag.MAX_BAG)
				throw new UseItemException(peony.Messages.STRING_01859);
			player.attendantBag.maxSize += extendCount;
			player.addIntPropertyChangedItem(ChangedItem.MAX_ATTENDAT_CNT, 
					player.attendantBag.maxSize, false, true);
		}
	}
	
	public boolean isAsync() {
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}

}
