package peony.service.cards;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.net.ClientSession;
import peony.net.Packet;

import com.pip.sanguo.data.Card;

/**
 * 卡片信息
 */
public class CardInfoCall extends ClientSessionAsyncCall {
	private int serial;
	private int cardId;
	private int cardGroupId;
	private int level;
	private ClientSession session;
	public CardInfoCall(ClientSession session) {
		super(session);
	}
	
	public CardInfoCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		cardId = packet.getInt();
		level=packet.getByte();
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
				ErrorHandler.sendErrorMessage(session, serial, 1, peony.Messages.STRING_00772);
				return;
			}
			Packet pt = new Packet(OpCode.CARD_INFO_SERVER);
			pt.putInt(serial);
			pt.putString(card.description);
//			pt.putString(card.getFormulaDesc());
			pt.putString(cs.getFormulaDesc(card));
			String desc="等级："+level+"\n当前属性：";
			if(card.prorertyType==Card.PROPERTY_TYPE_SKILL){
				Buff skillBuffNext=BuffUtil.createBuff(card.buff2Id, level, p,p, 0);
				if(skillBuffNext!=null){
					desc+=skillBuffNext.getDesc();
				}
			}else{
				desc+=cs.getEnhanceDesc(cardId, level);
			}
			pt.putUTF(desc);
			int q=0;
			try{
				GameItem  item=ObjectAccessor.createGameItem(card.itemId);
				if(item!=null){
					q=item.template.quality;
				}
			}catch(Exception e){}
			pt.put(q);
			p.send(pt);
		}
	}
}
