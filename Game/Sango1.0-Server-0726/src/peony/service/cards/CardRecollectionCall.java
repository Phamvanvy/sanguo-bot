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

import com.pip.sanguo.data.Card;

public class CardRecollectionCall extends ClientSessionAsyncCall {
	private int serial;
	private int yes;
	private int cardId;
	private int gridId;
	private ClientSession session;
	public CardRecollectionCall(ClientSession session) {
		super(session);
	}
	
	public CardRecollectionCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		yes = packet.getByte();
		cardId = packet.getInt();
		gridId = packet.getShort();
		this.session = session;
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		Player p = (Player)session.getClient();
		CardService cardService = Server.server.getServiceRegistry().getCardService();
		int type = -1;
		if(p!=null){
			if(yes == 1){
				Card cd = cardService.getCardByCardId(cardId);
				boolean isFlash = false;
				if(cd != null){
					p.pool.setInt(cardService.getPropertyOfPlayerCard(cardId), 1);
					
					PlayerTransaction tx = p.newTransaction("CARD");
					isFlash = cardService.generateFlashCard(cd);
					if(isFlash){
						p.pool.setInt(cardService.getPropertyOfCardQuality(cd.id), 1);
					} else {
						p.pool.setInt(cardService.getPropertyOfCardQuality(cd.id), 0);
					}
					TransactionBagGrid it = p.bag.removeGridGameItem(gridId, cd.itemId,
							-1, 1, tx, true);
					if (it == null) {
						tx.rollback();
					} else {
						tx.commit();
					}
				} else {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.TOWERDEFEND_SIGNUP_CLIENT, "ÎÞÐ§¿¨Æ¬");
					return;
				}
				type = isFlash?Card.QUALITY_GLARE:Card.QUALITY_COMMON;
			}
			
			Packet pt = new Packet(OpCode.CARD_COLLECTAGAIN_SERVER);
			pt.putInt(serial);
			pt.put(type);
			p.send(pt);
		}
	}
}
