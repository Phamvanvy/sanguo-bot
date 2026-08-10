package peony.service.shop;

import org.apache.log4j.Logger;

import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.service.ServiceEvent;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;
import peony.service.account.AccountService;
import peony.service.account.ErrorMessages;
import peony.service.account.cmcc.CmccIBuyMessage;

import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.LegacyBuy1Message;
import com.pip.net.message.gameaccount.LegacyBuyResultMessage;

public class IMoneyBuyCall extends AccountAsyncCall {

	private static final Logger log = Logger.getLogger(IMoneyBuyCall.class);

	protected int accountId;
	protected BuyRequest request;
	protected int imoney;
	protected String consumeCode;
	protected IBuy ibuy;
	protected int level;
	protected int faction;

	public IMoneyBuyCall(int accountId, int imoney, String consumeCode,
			BuyRequest request, int level, int faction) {
		super(null);
		this.accountId = accountId;
		this.imoney = imoney;
		this.consumeCode = consumeCode;
		this.request = request;
		this.level = level;
		this.faction = faction;
	}

	public void run() {
		AccountService service = Server.server.getServiceRegistry()
				.getAccountService();
		Account a = service.getAccount(accountId);
		if (a != null) {
			if ("CMCC".equals(Server.server.revision)
					|| "CHINATEL".equals(Server.server.revision)) {
				LogUtil.logIMoneyBuyTry(request.player.id, accountId,
						request.buyObject);
				CmccIBuyMessage message = new CmccIBuyMessage(a.getId(), "",
						imoney, consumeCode, "", a.getCmccUserId(),
						((ShopItemBuy) request.buyObject).count);
				service.sendAndRegister(message, this);
			} else {
				if ("PIP".equals(Server.server.revision)
						&& this.consumeCode != null) {
					LogUtil.logIMoneyBuyTry(request.player.id, accountId,
							request.buyObject);
					CmccIBuyMessage message = new CmccIBuyMessage(-1, "",
							imoney, consumeCode, "", a.getCmccUserId(),
							((ShopItemBuy) request.buyObject).count);
					Server.server.getServiceRegistry().getSlaveAccountService().sendAndRegister(message, this);
				} else {
					LogUtil.logIMoneyBuyTry(request.player.id, accountId,
							request.buyObject);
					LegacyBuy1Message message = new LegacyBuy1Message(
							a.getId(), a.getKey(), imoney);
					service.sendAndRegister(message, this);
				}
			}
		}
	}

	public void callFinish() throws Exception {
		if (message != null && message instanceof LegacyBuyResultMessage) {
			LegacyBuyResultMessage msg = (LegacyBuyResultMessage) message;
			if (msg.isSuccess()) {
				LogUtil.logIMoneyBuyOK(request.player.id, accountId,
						request.buyObject, true, msg.getBalance(), msg
								.getCost(), level);
				IBuy ibuy;
				if (request.buyObject instanceof ShopItemBuy) {
					ShopItemBuy sib = (ShopItemBuy) request.buyObject;
					ibuy = new IBuy(accountId, request.player.id,
							sib.itemTemplate.id, sib.itemTemplate.name,
							sib.count, 1, imoney, level, faction);
					// 广播事件
					ServiceEvent event = new ServiceEvent(ServiceEvent.EVENT_IBUY, request.player.id, imoney, sib.itemTemplate.id, sib.count);
					Server.server.getEventManager().addEvent(event);
				} else {
					ibuy = new IBuy(accountId, request.player.id, 0,
							request.buyObject.toString(), request.buyObject
									.getCount(), 1, imoney, level, faction);
					ServiceEvent event = new ServiceEvent(ServiceEvent.EVENT_IBUY, request.player.id, imoney, 0, 0);
					Server.server.getEventManager().addEvent(event);
				}
				Server.server.getServiceRegistry().getDbService().ibuyDAO
						.newEntity(ibuy);
				
				
				
				
				// 统计
				Server.server.getServiceRegistry().getRealtimeStatService().imoneyUseCounter += msg.getCost();
			} else {
				LogUtil.logIMoneyBuyOK(request.player.id, accountId,
						request.buyObject, false, msg.getBalance(), msg
								.getCost(), level);
			}
			Player p = ObjectAccessor.getPlayer(request.player.id);
			if (p != null && p.session != null) {
				if (msg.isSuccess()||("PIP".equals(Server.server.revision))) { //如果连移动的认证，那么购买失败以后可能传回0的余额
					if (!("PIP".equals(Server.server.revision) && this.consumeCode != null)) { //如果是pip版本，并且consumeCode不为空，那么就是话费专区
						Account a = (Account) p.session.getIdentity();
						int oldIMoney = a.getIMoney();
						if (oldIMoney != msg.getBalance()) {
							a.setIMoney(msg.getBalance());
							p.addIntPropertyChangedItem(ChangedItem.IMONEY, a
									.getIMoney() / 100, true, true);
						}
					}
				}
				if (msg.isSuccess()) {
					float creditRatio = Server.server.creditRatio;
					int credit = (int) (msg.getCost() / 3600 * creditRatio);
					if (credit > 0) {
						PlayerTransaction tx = p.newTransaction("BUY");

						p.addCredit(credit, tx, true);
						tx.commit();
					}
				}
			}
			Server.server.getServiceRegistry().getShopService()
					.processBuyResult(request.buyID, msg.isSuccess(),
							msg.getCause());
		} else {
			ErrorMessage msg = (ErrorMessage)message;
			LogUtil.logIMoneyBuyOK(request.player.id, accountId,
					request.buyObject, false, 0, 0, level);
			Server.server.getServiceRegistry().getShopService()
					.processBuyResult(request.buyID, false, msg == null ? null : ErrorMessages.getErrorMesssage(msg));
		}
	}

}
