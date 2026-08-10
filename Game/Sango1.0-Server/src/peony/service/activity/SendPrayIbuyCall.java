package peony.service.activity;

import peony.common.SyncIbuyCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationService;
import peony.service.shop.ShopService;

public class SendPrayIbuyCall extends SyncIbuyCall {
	protected Player player;
	protected int serial;
	protected int destId;
	
	public SendPrayIbuyCall(ClientSession session, Player player, int serial, int destId) {
		super(session, null);
		this.player = player;
		this.serial = serial;
		this.destId = destId;
	}

	public void callFinish() throws Exception {
		if(success){
			SendNewYearPrayService service = Server.server.getServiceRegistry().getSendNewYearPrayService();
			try{
				service.sendPrayPrivate(player, destId);
			}catch(Exception e){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.SEND_NEWYEAR_PRAY_CLIENT, e.getMessage());
			}
			Packet pt = new Packet(OpCode.SEND_NEWYEAR_PRAY_SERVER);
			pt.putInt(serial);
			pt.put(0);
			player.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, 0, OpCode.SEND_NEWYEAR_PRAY_CLIENT, errorMessage);
		}
	}

	public void run() {
		try {
			RelationService rs = Server.server.getServiceRegistry().getRelationService();
			PlayerRelation relation = rs.get(player.id);
			if (relation == null) {
				error(peony.Messages.STRING_00436);
				addToClientSession();
				return;
			}
			relation.friends.refreshPlayers();
			int count = relation.friends.getCount();
			Actor destActor = null;
			for(int i=0; i<count; i++){
				Actor temp = relation.friends.getPlayerAt(i);
				if(temp.id == destId){
					destActor = temp;
					break;
				}
			}
			if(destActor == null){
				error("该角色还不是您的好友");
				addToClientSession();
				return;
			}
			
			int itemId = SendNewYearPrayService.IMONEY_ITEM_SEND;
			ShopService service = Server.server.getServiceRegistry().getShopService();
			int shopId = service.getShopByItemId(itemId).id;
			waitBuy(player, 0, shopId, itemId, 1, this);
			addToClientSession();
		} catch (Exception e) {
			
		}
	}

}
