package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.IMoneyCard;
import peony.game.ItemEffect;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.service.account.IMoneyCardCall1;
import peony.service.account.IMoneyCardUseCall;

public class IMoneyCardEffect implements ItemEffect {
	
	protected static int mod = 0;
	
	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		Player p = (Player) source;
		if (p != null) {
			IMoneyCard card = (IMoneyCard) item.object;
			if(mod==1){
				Server.server.getServiceRegistry().getAccountService().schedule(
						new IMoneyCardUseCall(p.session, card.getCardno(), card
								.getPassword()));
			}else{
				Server.server.getServiceRegistry().getAccountService().schedule(
						new IMoneyCardCall1(p.session, card.getCardno(), card
								.getPassword(),item));
			}
		}
	}
	
	public boolean needRemove() {
		return false;
	}
}
