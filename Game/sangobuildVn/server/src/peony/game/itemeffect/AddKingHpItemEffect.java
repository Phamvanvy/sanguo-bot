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
			throw new UseItemException("错误的目标");
		}
		if (target instanceof Player) {
			Player p = (Player) target;
			if (p != null) {
				NationService nationService = Server.server
						.getServiceRegistry().getNationService();
				if (!nationService.isKing(p)) {
					throw new UseItemException("Chỉ có quốc công mới có thể sử dụng vật phẩm này");
				}
				if (target.hp == target.maxhp) {
					throw new UseItemException("Hiện tại nơi đó đã trong trạng thái đấy máu");
				}
				target.setHp(Math.min(target.hp + (target.maxhp * percent / 100),
						target.maxhp), true);
			}
		} else {
			throw new UseItemException("错误的目标");
		}
	}

	public boolean isAsync() {
		return false;
	}

}
