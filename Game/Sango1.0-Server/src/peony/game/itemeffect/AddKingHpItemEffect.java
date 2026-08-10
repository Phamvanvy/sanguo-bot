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
			throw new UseItemException(peony.Messages.STRING_00014);
		}
		if (target instanceof Player) {
			Player p = (Player) target;
			if (p != null) {
				NationService nationService = Server.server
						.getServiceRegistry().getNationService();
				if (!nationService.isKing(p)) {
					throw new UseItemException(peony.Messages.STRING_01444);
				}
				if (target.hp == target.maxhp) {
					throw new UseItemException(peony.Messages.STRING_01445);
				}
				target.setHp(Math.min(target.hp + (target.maxhp * percent / 100),
						target.maxhp), true);
			}
		} else {
			throw new UseItemException(peony.Messages.STRING_00014);
		}
	}

	public boolean isAsync() {
		return false;
	}

	public boolean needRemove() {
		return false;
	}

}
