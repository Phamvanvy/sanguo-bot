package peony.service.cards;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

import com.pip.sanguo.data.Card;

public class CardListDetailCall extends ClientSessionAsyncCall {
	private int serial;
	private int personId;
	private int groupId;
	private ClientSession session;
	public CardListDetailCall(ClientSession session) {
		super(session);
	}
	
	public CardListDetailCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		personId = packet.getInt();
		groupId = packet.getInt();
		this.session = session;
	}

	public void callFinish() throws Exception {
	}

	public void run() {
		Player p = (Player) session.getClient();
		if (p != null) {
			CardService cs = Server.server.getServiceRegistry().getCardService();
			Packet pt = new Packet(OpCode.CARD_DETAILLIST_SERVER);
			CardGroup cardGroup = cs.cardGroups.get(groupId);
			pt.putInt(serial);
			pt.putInt(cardGroup.cards.size());
			Player person = (Player)ObjectAccessor.getPlayer(personId);
			if(person==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_DETAILLIST_CLIENT, "玩家已下线");
				return;
			}
			// 判断是否显示卡片名称
			boolean showName = person.pool.getInt(
					cs.getPropertyOfShowCardName(groupId), 0) == 1;
			for (Card cd : cardGroup.cards) {
				// 是否收集
				boolean hasMatch = person.pool.getInt(cs.getPropertyOfPlayerCard(cd.id)) == 1;
				pt.put(hasMatch == true ? 1 : 0);
				//是否闪卡
				boolean isFlash = person.pool.getInt(cs.getPropertyOfCardQuality(cd.id)) == 1;
				if (showName || hasMatch) {
					pt.putUTF(cd.title + (isFlash?"[闪]":""));
				} else {
					pt.putUTF("??");
				}
				// 配方描述
				String fd = cs.formulaDesc.get(cd.id);
				if (fd == null || "".equals(fd)) {
					pt.putUTF("此卡为基础卡，无需合成");
				} else {
					pt.putUTF(fd);
				}
				//卡片id
				pt.putInt(cd.id);
				//卡片描述
				pt.putUTF(cd.description);
				//卡片品质
				byte quality = (byte)(isFlash?Card.QUALITY_GLARE:Card.QUALITY_COMMON);
				pt.put(quality);
				//卡片资源
				pt.putUTF(cd.res);
			}
			p.send(pt);
		}
	}
}
