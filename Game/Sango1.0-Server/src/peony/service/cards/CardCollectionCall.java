package peony.service.cards;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.TransactionBagGrid;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;
import com.pip.sanguo.data.Card;

/**
 * 卡片收藏
 */
public class CardCollectionCall extends ClientSessionAsyncCall {
	private int serial;
	private int gridId;
	private int itemId;
	private ClientSession session;
	public CardCollectionCall(ClientSession session) {
		super(session);
	}
	
	public CardCollectionCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		gridId = packet.getInt();
		itemId = packet.getInt();
		this.session = session;
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		Player p = (Player)session.getClient();
		if(p!=null){
			CardService cardService = Server.server.getServiceRegistry().getCardService();
			Card card = cardService.getCardByItemId(itemId);
			if(card == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TOWERDEFEND_SIGNUP_CLIENT, peony.Messages.STRING_00503);
				return;
			}
			
			boolean hasMatch = p.pool.getInt(cardService.getPropertyOfPlayerCard(card.id),0) >= 1;
		    if(!hasMatch){//此卡位还未收藏过
				int groupId = card.suiteId;
				if(groupId != -1){
					p.pool.setInt(cardService.getPropertyOfPlayerSuit(groupId), p.pool.getInt(
							cardService.getPropertyOfPlayerSuit(groupId), 0) + 1);
				}
				p.pool.setInt(CardService.PROPERTY_HAVECARD, p.pool
						.getInt(CardService.PROPERTY_HAVECARD, 0) + 1);		
				p.pool.setInt(cardService.getPropertyOfPlayerCard(card.id), 1);
//				boolean isFlash = cardService.generateFlashCard(card);
//				if(isFlash){
//					p.pool.setInt(cardService.getPropertyOfCardQuality(card.id), 1);
//				} else {
//					p.pool.setInt(cardService.getPropertyOfCardQuality(card.id), 0);
//				}
				PlayerTransaction tx = p.newTransaction("CARD");
				TransactionBagGrid it = p.bag.removeGridGameItem(gridId, itemId,
						-1, 1, tx, true);
				if (it == null) {
					tx.rollback();
				} else {
					tx.commit();
					//卡片充能
//					int cardEnergy = cardService.generateCardEnergy(p, card);
//					cardService.addCardEnergy(p, card, cardEnergy, isFlash);
				}
				
				//卡片收藏事件
				Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_COLLECT_CARD,p,groupId));
				
				Packet pt = new Packet(OpCode.CARD_COLLECTION_SERVER);
				pt.putInt(serial);
				pt.put(Card.QUALITY_COMMON);
				p.send(pt);
		    } else {//此卡位已经藏过
		    	int enrgy = cardService.getCardEnergy(p, card.id, false);
		    	int maxEnergy = cardService.getCardEnergy(p, card.id, true);
		    	if(enrgy==maxEnergy){
		    		Packet pt = new Packet(OpCode.OPENUI_SERVER);
					pt.putString("ui_npc_dialog");
					pt.putString("ASK_FULL_CARD|" + card.id + "|" +card.title + "|" + gridId);
					p.send(pt);
		    	}else{
		    		Packet pt = new Packet(OpCode.OPENUI_SERVER);
					pt.putString("ui_npc_dialog");
					pt.putString("ASK_COVER_CARD|" + card.id + "|" +card.title + "|" + gridId);
					p.send(pt);
		    	}
		    }  
		}
	}
	
}
