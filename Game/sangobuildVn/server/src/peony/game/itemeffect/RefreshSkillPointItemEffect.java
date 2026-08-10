package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;

public class RefreshSkillPointItemEffect implements ItemEffect {

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player)source;
		p.refreshSkillPoint(-1);
	}

}
