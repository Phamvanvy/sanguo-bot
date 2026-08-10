package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.attendant.Attendant;

/**
 * 获取随从 物品使用效果
 * @author dchen
 */
public class AttendantItemEffect implements ItemEffect {

	public int attendantId;

	public AttendantItemEffect(int attendantId){
		this.attendantId = attendantId;
	}
	
	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player player = (Player)source;
		if(player!=null){
			Attendant attendant = new Attendant(attendantId, player);
			attendant.refreshProperties(false);
			if(player.attendantBag.isFull())
				throw new UseItemException("随从栏已满");
			player.attendantBag.addAttendant(attendant);
			attendant.setHp(attendant.maxhp, false);
			attendant.setMp(attendant.maxmp, false);
		}
	}
	
	public boolean isAsync() {
		return false;
	}

}
