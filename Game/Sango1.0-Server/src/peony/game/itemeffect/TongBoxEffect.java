package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.service.tong.TongException;
import peony.service.tong.TongService;

public class TongBoxEffect implements ItemEffect {

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)target;
		TongService ts = Server.server.getServiceRegistry().getTongService();
		try {
			ts.useBaibaoBox(p);
		} catch (TongException e) {
			throw new UseItemException(e.getMessage());
		}
	}
	
	public boolean needRemove() {
		return false;
	}
}
