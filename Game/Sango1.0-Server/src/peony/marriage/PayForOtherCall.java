package peony.marriage;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopService;

public class PayForOtherCall extends ClientSessionAsyncCall implements NoItemShopBuyI{
	
	AskForGiftService aService = Server.server.getServiceRegistry().getAskForGiftService();
	Player player = null;
	Player source = null;
	int serial;
	int requestId;
	int itemId;
	int count;
	int sourceId;
	

	public PayForOtherCall(ClientSession session,Packet packet) {
		super(session);
		player = (Player) session.getClient();
		serial = packet.getInt();
		requestId = packet.getInt();
		itemId = packet.getInt();
		count = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(!success){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.PAYFORME_INVIT_OK_CLIENT, errorMessage);
		}
		
	}

	public void run() {
		if(player!=null){
			GameItem item = ObjectAccessor.createGameItem(itemId);
			if(item != null){
				AskForGiftRequest request = aService.getAndRemoveRequest(requestId);
				Player source = (Player) ObjectAccessor.getGameObject(request.ref);
				if (request != null && (Time.currTime - request.time) < 60000) { // 一分钟之内有效
					if(canPay(request.ref.id)){
						try{
							sourceId = request.ref.id;
							ShopService service = Server.server.getServiceRegistry().getShopService();
							int shopId = service.getShopByItemId(itemId).id;
							NoItemShopBuy ibuy = new NoItemShopBuy(player,serial,shopId,itemId,count,this,null);
							service.buy(player, ibuy);
						}catch(Exception e){
							ErrorHandler.sendErrorMessage(session, serial, OpCode.PAYFORME_INVIT_OK_CLIENT, peony.Messages.STRING_00911);
							return;
						}
					}
				} else {
					if(source!=null){
						source.message(-1, peony.Messages.STRING_00912, -1, -1);
						aService.player2Request.remove(source.id);
					}
					ErrorHandler.sendErrorMessage(session, serial, OpCode.PAYFORME_INVIT_OK_CLIENT, peony.Messages.STRING_00913);
				}
			}
		}
	}
	
	public boolean canPay(int playerId){
		Player source = ObjectAccessor.getPlayer(playerId);
		if(source != null){
			return true;
		} else {
			Actor sourceActor = Server.server.getServiceRegistry().getActorCacheService().find(playerId);
			if(sourceActor != null){
				return true;
			}
		}
		return false;
	}
	
	public void process(Object[] o) {
		player.message(-1, peony.Messages.STRING_00914, -1, -1);
		GameItem item = ObjectAccessor.createGameItem(itemId);
		Player source = (Player) ObjectAccessor.getGameObject(sourceId);
		if(item != null){
			if(source!=null){
				aService.player2Request.remove(source.id);
				PlayerTransaction tx = source.newTransaction("ASKFORGIFT");
				if(!source.bag.addGameItem(item, count, tx, true)){
					tx.rollback();
					MailService mailService = Server.server.getServiceRegistry().getMailService();
					mailService.sendSystemMailAsync(source.id, peony.Messages.STRING_00004,peony.Messages.STRING_00915, peony.Messages.STRING_00916, 0, item, count,"ASKFORGIFT");
					source.message(-1, peony.Messages.STRING_00917, -1, -1);
				}else{
				    tx.commit();
				    source.message(-1, peony.Messages.STRING_00918, -1, -1);
				} 
			} else {
				Actor sourceActor = Server.server.getServiceRegistry().getActorCacheService().find(sourceId);
				if(sourceActor != null){
					aService.player2Request.remove(sourceId);
					MailService mailService = Server.server.getServiceRegistry().getMailService();
					mailService.sendSystemMailAsync(sourceActor.id, peony.Messages.STRING_00004,peony.Messages.STRING_00915, peony.Messages.STRING_00916, 0, item, count,"ASKFORGIFT");
					player.message(-1, peony.Messages.STRING_00919, -1, -1);
				}
			}
		}
		addToClientSession();
	}

	public void procssFail(Object[] o) {
		aService.player2Request.remove(sourceId);
		addToClientSession();
	}
	

}
