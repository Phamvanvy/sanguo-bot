package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.IMoneyCard;
import peony.game.ItemEffect;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.service.account.IMoneyCardUseCall;

public class IMoneyCardEffect implements ItemEffect {
	
	
	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		Player p = (Player) source;
		if (p != null) {
			IMoneyCard card = (IMoneyCard) item.object;
			Server.server.getServiceRegistry().getAccountService().schedule(
					new IMoneyCardUseCall(p.session, card.getCardno(), card
							.getPassword()));
		}
	}
}
