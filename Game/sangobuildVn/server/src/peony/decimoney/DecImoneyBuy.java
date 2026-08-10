package peony.decimoney;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.Shop.BuyRequirement;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.service.account.Account;
import peony.service.account.AccountService;
import peony.service.shop.IBuyObject;
import peony.service.shop.ShopException;

public class DecImoneyBuy implements IBuyObject {
	GameObjectRef player;
	int price;
	List<BuyRequirement> reqs = new ArrayList<BuyRequirement>();

	public DecImoneyBuy(Player p, int price) {
		this.player = p.ref();
		this.price = price;
	}

	public void commit() {

	}

	public void rollback() {

	}

	public String getCause() {
		return null;
	}

	public int getCount() {
		return 1;
	}

	public int getDiscount() {
		return 100;
	}

	public List<BuyRequirement> getRequirements() {
		BuyRequirement req = new BuyRequirement();
		req.type = Shop.TYPE_IMONEY;
		req.amount = price;
		req.deduct = true;
		reqs.add(req);
		return reqs;
	}

	public void lock() throws ShopException {
		Player p = (Player) ObjectAccessor.getGameObject(player);
		if (p == null) {
			throw new ShopException("Trạng thái nhân vật di thường");
		}
		Iterator<BuyRequirement> reqItor = reqs.iterator();
		while (reqItor.hasNext()) {
			BuyRequirement req = reqItor.next();
			if(!req.deduct)
				continue;
			switch(req.type){
			case Shop.TYPE_IMONEY: 
				AccountService as = Server.server.getServiceRegistry()
						.getAccountService();
				Account account = as.getAccount(p.accountId);
				if (account == null || account.getIMoney() / 100 < req.amount) {
					throw new ShopException(MessageFormat.format(
							"{0}{1} Yêu cầu trong tài khoản có ít nhất {0}{1}", req.amount, Server.iMoneyString));
			    }
				break;
			}
		}
	}
	
	@Override
	public String toString() {
		return "TYPE[USEITEM]";
    }

	public void log() {

	}

	public void notifyClient(boolean succ, String message) {

	}

	public void receive(PlayerTransaction tx, boolean supportMail)
			throws ShopException {

	}

}
