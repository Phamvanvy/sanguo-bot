package peony.service.enhance;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopException;
import peony.service.shop.ShopService;

public class EquipEnhanceCall extends ClientSessionAsyncCall implements NoItemShopBuyI{

	protected static final int MONEY = 200; 
	
	Player p = null;
	int serial ;
	int itemId ;
	int instanceId ;
	EnhanceService enhService;
	GameItem gameItem;
	Object owner;
	
	public EquipEnhanceCall(ClientSession session,Packet packet) {
		super(session);
		p = (Player) session.getClient();
		enhService = Server.server.getServiceRegistry().getEnhanceService();
		serial = packet.getInt();
		itemId = packet.getInt();
		instanceId = packet.getInt();
	}
	
	public void run() {
		if (p != null) {
			Object[] os = ItemUtil.findPlayerEquipment(p, itemId, instanceId);
			if (os != null) {
				gameItem = (GameItem) os[0];
				owner = os[1];
				if (gameItem == null) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.ENHANCE_EQUIP_CLIENT, "找不到物品");
					return;
				}
				if (!gameItem.template.isEquipment()) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.ENHANCE_EQUIP_CLIENT, "不是装备");
					return;
				}
				int enhanceTimes = enhService.getEnhanceTimes(p);
				if(enhanceTimes>=20){
					try {
						ShopService service = Server.server.getServiceRegistry().getShopService();
						int shopId = service.getShopByItemId(NoItemShopBuy.LIANGYUANBAO).id;
						NoItemShopBuy dib = new NoItemShopBuy(p,serial,shopId,NoItemShopBuy.LIANGYUANBAO,1,this,null);
						Server.server.getServiceRegistry().getShopService().buy(p, dib);
					} catch (ShopException e) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.ENHANCE_EQUIP_CLIENT, "您的元宝余额不足，无法强化装备");
						return;
					}
				}else{
					PlayerTransaction tx = p.newTransaction("ENHANCEEQU");
					try {
						p.decMoney(MONEY, tx, true);
						tx.commit();
						addToClientSession();
					} catch (NoEnoughValueException ex) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.ENHANCE_EQUIP_CLIENT, "您的金钱余额不足，无法强化装备");
						return;
					}
				}
			} else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ENHANCE_EQUIP_CLIENT, "没找到指定装备");
			}
		}
	}

	public void process(Object[] o) {
		addToClientSession();
	}

	public void procssFail(Object[] o) {
		error("扣费失败");
		addToClientSession();
	}

	public void callFinish() throws Exception {
		if(success){
			enhService.equipEnhance(p, gameItem, owner, serial);
		}else{
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ENHANCE_EQUIP_CLIENT, errorMessage);
		}
	}

	
	

}
