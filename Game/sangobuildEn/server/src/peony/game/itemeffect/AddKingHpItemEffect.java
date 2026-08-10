package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.nation.NationService;

public class AddKingHpItemEffect implements ItemEffect {
	
	public int percent;
	
	public AddKingHpItemEffect(int percent) {
		this.percent = percent;
	}
	
	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		if (!ItemUtil.checkUseTarget(source, item, target)) {
			throw new UseItemException("錯誤的目標");
		}
		if (target instanceof Player) {
			Player p = (Player) target;
			if (p != null) {
				NationService nationService = Server.server
						.getServiceRegistry().getNationService();
				if (!nationService.isKing(p)) {
					throw new UseItemException("只有國公才可以使用該物品");
				}
				if (target.hp == target.maxhp) {
					throw new UseItemException("當前已經處于滿血狀態");
				}
				target.setHp(Math.min(target.hp + (target.maxhp * percent / 100),
						target.maxhp), true);
			}
		} else {
			throw new UseItemException("錯誤的目標");
		}
	}

	public boolean isAsync() {
		return false;
	}

}
