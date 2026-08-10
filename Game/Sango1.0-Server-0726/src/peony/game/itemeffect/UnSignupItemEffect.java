package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;

public class UnSignupItemEffect implements ItemEffect {

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		Player p = (Player)source;
		if(!Server.server.getServiceRegistry().getFlagBattleFieldVMapManager().removeSignup(p))
			throw new UseItemException("您并未报名参加战场，无需特赦。");
		if(p!=null)
			p.message(-1, "您的战场排队已经取消，不用参加本次战场了。", -1, -1);
	}

}
