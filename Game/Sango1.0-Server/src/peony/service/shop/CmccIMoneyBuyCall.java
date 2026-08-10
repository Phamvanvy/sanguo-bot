package peony.service.shop;

import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.changed.ChangedItem;
import peony.service.ServiceEvent;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;
import peony.service.account.AccountService;
import peony.service.account.ErrorMessages;
import peony.service.account.cmcc.CmccBuy2Message;
import peony.service.account.cmcc.CmccBuy2ResultMessage;
import peony.service.account.cmcc.CmccIBuyMessage;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.LegacyBuy1Message;
import com.pip.net.message.gameaccount.LegacyBuyResultMessage;

/**
 * 先暂时写一个为CMCC单写这个类，上线无问题后merge到ImoneyBuyCall中
 * @author dchen
 */
public class CmccIMoneyBuyCall extends AccountAsyncCall {

	private static final Logger log = Logger.getLogger(CmccIMoneyBuyCall.class);

	protected int accountId;
	protected BuyRequest request;
	protected int imoney;
	protected String consumeCode;
	protected IBuy ibuy;
	protected int level;
	protected int faction;
	protected boolean allowUntrustIMoney;
	
	protected static AtomicInteger requestIds = new AtomicInteger(1);

	public CmccIMoneyBuyCall(int accountId, int imoney, String consumeCode,
			BuyRequest request, int level, int faction, boolean allowUntrustIMoney) {
		super(null);
		this.accountId = accountId;
		this.imoney = imoney;
		this.consumeCode = consumeCode;
		this.request = request;
		this.level = level;
		this.faction = faction;
		this.allowUntrustIMoney = allowUntrustIMoney;
	}

	public void run() {
		AccountService service = Server.server.getServiceRegistry().getAccountService();
		Account a = service.getAccount(accountId);
		if (a != null) {
			if (Server.REVISION_TYPE_CMCC.equals(Server.server.revision) || Server.REVISION_TYPE_TEL.equals(Server.server.revision)) {
				LogUtil.logIMoneyBuyTry(request.player.id, accountId, request.buyObject);
//				CmccIBuyMessage message = new CmccIBuyMessage(a.getId(), "",
//						imoney, consumeCode, "", a.getCmccUserId(),
//						((ShopItemBuy) request.buyObject).count);
				//新的CMCC购买商品协议
				CmccBuy2Message message = new CmccBuy2Message(a.getId(), imoney, requestIds.incrementAndGet());
				service.sendAndRegister(message, this);
			} else {
				if (Server.REVISION_TYPE_PIP.equals(Server.server.revision) && this.consumeCode != null) {
					LogUtil.logIMoneyBuyTry(request.player.id, accountId,request.buyObject);
					Player player = ObjectAccessor.getPlayer(request.player.id);
					CmccIBuyMessage message = new CmccIBuyMessage(-1, "",imoney, consumeCode, 
							player == null ? "" : player.getAccount().getVersionString(), 
							a.getCmccUserId(), ((ShopItemBuy) request.buyObject).count);
					Server.server.getServiceRegistry().getSlaveAccountService().sendAndRegister(message, this);
				} else {
					LogUtil.logIMoneyBuyTry(request.player.id, accountId, request.buyObject);
					LegacyBuy1Message message = new LegacyBuy1Message(a.getId(), a.getKey(), imoney, !allowUntrustIMoney);
					service.sendAndRegister(message, this);
				}
			}
			try {
				int[] info = new int[]{Time.currTime, accountId, imoney};
				ShopService shopService = 	Server.server.getServiceRegistry().getShopService();
				shopService.registBuyLogInfo(request.buyID, info);
			} catch (Exception e) {
			}
		}
	}

	public void callFinish() throws Exception {
		if (message != null && (message instanceof LegacyBuyResultMessage || message instanceof CmccBuy2ResultMessage)) {
			int cost = 0;
			boolean isSuccess = false;
			if(message instanceof LegacyBuyResultMessage){
				LegacyBuyResultMessage msg = (LegacyBuyResultMessage) message;
				isSuccess = msg.isSuccess();
				if (isSuccess) {
					LogUtil.logIMoneyBuyOK(request.player.id, accountId,request.buyObject, true, msg.getLongBalance(), msg.getCost(), level);
					cost = msg.getCost();
				}else{
					LogUtil.logIMoneyBuyOK(request.player.id, accountId,request.buyObject, false, 
							msg.getLongBalance(), msg.getCost(), level);
				}
			}else if(message instanceof CmccBuy2ResultMessage){
				CmccBuy2ResultMessage msg = (CmccBuy2ResultMessage)message;
				isSuccess = msg.isSuccess();
				if (isSuccess) {
					LogUtil.logIMoneyBuyOK(request.player.id, accountId,request.buyObject, true, msg.getBalance(), msg.getCost(), level);
					cost = msg.getCost();
				}else{
					LogUtil.logIMoneyBuyOK(request.player.id, accountId,request.buyObject, false, 
							msg.getBalance(), msg.getCost(), level);
				}
			}
			IBuy ibuy = null;
			if(isSuccess){
				if (request.buyObject instanceof ShopItemBuy) {
					ShopItemBuy sib = (ShopItemBuy) request.buyObject;
					ibuy = new IBuy(accountId, request.player.id,sib.itemTemplate.id, sib.itemTemplate.name,
							sib.count, 1, imoney, level, faction);
					
					// 广播事件
					ServiceEvent event = new ServiceEvent(ServiceEvent.EVENT_IBUY, request.player.id, imoney, sib.itemTemplate.id, sib.count);
					Server.server.getEventManager().addEvent(event);
				} else {
					ibuy = new IBuy(accountId, request.player.id, 0,request.buyObject.toString(), 
							request.buyObject.getCount(), 1, imoney, level, faction);
					ServiceEvent event = new ServiceEvent(ServiceEvent.EVENT_IBUY, request.player.id, imoney, 0, 0);
					Server.server.getEventManager().addEvent(event);
				}
				Server.server.getServiceRegistry().getDbService().ibuyDAO.newEntity(ibuy);
			}
			
			// 统计
			Server.server.getServiceRegistry().getRealtimeStatService().imoneyUseCounter += cost;
			
			Player p = ObjectAccessor.getPlayer(request.player.id);
			if (p != null && p.session != null) {
				if (isSuccess||(Server.REVISION_TYPE_PIP.equals(Server.server.revision))) { //如果连移动的认证，那么购买失败以后可能传回0的余额
					if (!(Server.REVISION_TYPE_PIP.equals(Server.server.revision) && this.consumeCode != null)) { //如果是pip版本，并且consumeCode不为空，那么就是话费专区
						Account a = (Account) p.session.getIdentity();
						long oldIMoney = a.getLongIMoney();
						long newBalance = 0;
						if(message instanceof LegacyBuyResultMessage)
							newBalance = ((LegacyBuyResultMessage)message).getLongBalance();
						else if(message instanceof CmccBuy2ResultMessage)
							newBalance = ((CmccBuy2ResultMessage)message).getBalance();
						if (oldIMoney != newBalance) {
							a.setLongIMoney(newBalance);
							//p.addIntPropertyChangedItem(ChangedItem.IMONEY, (int)(a.getLongIMoney() / 100), true, true);
							String showPrice = p.ibToYuanbao(a.getLongIMoney());
				 			p.addStringPropertyChangedItem(ChangedItem.YUANBAO, showPrice, true);
						}
					}
				}
				if (isSuccess) {
					float creditRatio = Server.server.creditRatio;
					int credit = (int) (cost / 3600 * creditRatio);
					if (credit > 0) {
						PlayerTransaction tx = p.newTransaction("BUY");

						p.addCredit(credit, tx, true);
						tx.commit();
					}
				}
			}
			String cause = "";
			if(message instanceof LegacyBuyResultMessage)
				cause = ((LegacyBuyResultMessage)message).getCause();
			else if(message instanceof CmccBuy2ResultMessage)
				cause = ((CmccBuy2ResultMessage)message).getCause();
			Server.server.getServiceRegistry().getShopService().processBuyResult(request.buyID,isSuccess,cause);
		} else {
			ErrorMessage msg = (ErrorMessage)message;
			LogUtil.logIMoneyBuyOK(request.player.id, accountId,request.buyObject, false, 0, 0, level);
			Server.server.getServiceRegistry().getShopService()
					.processBuyResult(request.buyID, false, msg == null ? null : ErrorMessages.getErrorMesssage(msg));
		}
		try {
			if(request!=null){
				ShopService shopService = Server.server.getServiceRegistry().getShopService();
				shopService.removeBuyLogInfo(request.buyID);
			}
		} catch (Exception e) {
		}
	}

}
