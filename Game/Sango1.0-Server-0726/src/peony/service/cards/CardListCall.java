package peony.service.cards;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class CardListCall extends ClientSessionAsyncCall {
	private int serial;
	private int personId;
	private ClientSession session;
	public CardListCall(ClientSession session) {
		super(session);
	}
	
	public CardListCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		personId = packet.getInt();
		this.session = session;
	}

	public void callFinish() throws Exception {
	}

	public void run() {
		Player p = (Player) session.getClient();
		if (p != null) {
			CardService cs = Server.server.getServiceRegistry().getCardService();
			Packet pt = new Packet(OpCode.CARD_LIST_SERVER);
			int size = cs.cardGroups.size();
			pt.putInt(serial);
			pt.putInt(size);
			Player person = (Player)ObjectAccessor.getPlayer(personId);
			if(person==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_LIST_CLIENT, "玩家已下线");
				return;
			}
			for (CardGroup group : cs.cardGroupList) {
				pt.putInt(group.groupId);
				pt.putUTF(group.cardGroupName);
				int cnt = person.pool.getInt(cs.getPropertyOfPlayerSuit(group.groupId), 0);
				pt.putInt(cnt);
				pt.putInt(group.cards.size());
				pt.putInt((int) Math.floor((cnt * 100)
						/ group.cards.size()));
			}
			int totalCnt = person.pool.getInt(CardService.PROPERTY_HAVECARD, 0);
			pt.putInt(totalCnt);
			pt.putInt(cs.totalcount);
			pt.putInt((int) Math.floor((totalCnt * 100) / cs.totalcount));
			p.send(pt);
		}
	}
}
