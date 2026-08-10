package peony.service.cards;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

import com.pip.sanguo.data.Card;

public class CardInfoCall extends ClientSessionAsyncCall {
	private int serial;
	private int cardId;
	private ClientSession session;
	public CardInfoCall(ClientSession session) {
		super(session);
	}
	
	public CardInfoCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		cardId = packet.getInt();
		this.session = session;
	}

	public void callFinish() throws Exception {
	}

	public void run() {
		Player p = (Player) session.getClient();
		if (p != null) {
			CardService cs = Server.server.getServiceRegistry().getCardService();
			Card card = cs.allCards.get(cardId);
			if(card == null){
				ErrorHandler.sendErrorMessage(session, serial, 1, "Không tìm được thẻ");
				return;
			}
			Packet pt = new Packet(OpCode.CARD_INFO_SERVER);
			pt.putInt(serial);
			pt.putString(card.description);
			pt.putString(card.getFormulaDesc());
			p.send(pt);
		}
	}
}
