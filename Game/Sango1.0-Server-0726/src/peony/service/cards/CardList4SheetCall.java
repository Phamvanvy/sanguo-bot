package peony.service.cards;

import java.util.ArrayList;
import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class CardList4SheetCall extends ClientSessionAsyncCall {
	private int serial;
	private ClientSession session;
	public CardList4SheetCall(ClientSession session) {
		super(session);
	}
	
	public CardList4SheetCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		this.session = session;
	}

	public void callFinish() throws Exception {
	}

	public void run() {
		Player p = (Player) session.getClient();
		if (p != null) {
			CardService cs = Server.server.getServiceRegistry().getCardService();
			List<CardGroup> groups = new ArrayList<CardGroup>();
			for (CardGroup group : cs.cardGroupList) {
				if(p.pool.getInt(cs.getPropertyOfShowCardName(group.groupId), 0) == 0
						&& p.pool.getInt(cs.getPropertyOfPlayerSuit(group.groupId), 0) < group.cards.size()){
					groups.add(group);
				}
			}
			Packet pt = new Packet(OpCode.CARD_LIST_4SHEET_SERVER);
			int size = groups.size();
			pt.putInt(serial);
			pt.putInt(size);
			for (CardGroup group : groups) {
				pt.putInt(group.groupId);
				pt.putUTF(group.cardGroupName);
			}
			p.send(pt);
			groups = null;
		}
	}
}
