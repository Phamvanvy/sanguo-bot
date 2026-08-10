package peony.service.cards;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
//import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
//import peony.game.buff.Buff;
//import peony.game.buff.BuffUtil;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.duel.DuelService;
import peony.service.fame.FameService;

import com.pip.sanguo.data.Card;

/**
 * 卡片详细列表
 */
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
			FameService fs = Server.server.getServiceRegistry().getFameService();
			DuelService ds = Server.server.getServiceRegistry().getDuelService();
			Packet pt = new Packet(OpCode.CARD_DETAILLIST_SERVER);
			CardGroup cardGroup = cs.cardGroups.get(groupId);
			pt.putInt(serial);
			pt.putInt(cardGroup.cards.size());
			Player person = (Player)ObjectAccessor.getPlayer(personId);
			if(person == null && fs != null){
				person = fs.getStatue(personId);
			}
			if(person == null && ds != null){
				person = ds.getStatue(personId);
			}
			if(person==null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_DETAILLIST_CLIENT, peony.Messages.STRING_00088);
				return;
			}
			// 判断是否显示卡片名称
//			boolean showName = person.pool.getInt(cs.getPropertyOfShowCardName(groupId), 0) == 1;
			for (Card cd : cardGroup.cards) {
				// 是否收集
				boolean hasMatch = person.pool.getInt(CardService.getPropertyOfPlayerCard(cd.id)) >= 1;
				pt.put(hasMatch ? 1 : 0);
				//是否闪卡
//				boolean isFlash = person.pool.getInt(cs.getPropertyOfCardQuality(cd.id)) == 1;
//				if (showName || hasMatch) {
//					pt.putUTF(cd.title + (isFlash?peony.Messages.STRING_00063:""));
//				} else {
//					pt.putUTF("??");
//				}
				pt.putUTF(cd.title);
				// 配方描述
//				String fd = cs.formulaDesc.get(cd.id);
//				if (fd == null || "".equals(fd)) {
//					pt.putUTF(peony.Messages.STRING_00693);
//				} else {
//					pt.putUTF(fd);
//				}
				//卡片id
				pt.putInt(cd.id);
				//卡片描述
				pt.putUTF(cd.description);
				//卡片品质
				int q = 0;
				try {
					q = ObjectAccessor.createGameItem(cd.itemId).template.quality;
				} catch (Exception e) {
				}
//				byte quality = (byte)(Card.QUALITY_COMMON);
				pt.put(q);
				//卡片资源
				pt.putUTF(cd.res);
				boolean isPictureCard = cs.isPictureCard(cd);
				pt.put(isPictureCard ? 1 : 0);
//				pt.putShort(cs.getCardEnergy(person, cd.id, false));
//				pt.putShort(cs.getCardEnergy(person, cd.id, true));
				pt.put(cd.star);
//				try{
//					int buffLevel = cd.buffLevel1;
//					if(isFlash){
//						buffLevel = cd.buffLevel2;
//					}
//					String buffDec = cs.getBuffDesc(cd.id, buffLevel);
//					pt.putUTF(buffDec);
//				}catch(Exception e){
//					pt.putUTF(peony.Messages.STRING_00064);
//				}
				CardInfo info = cs.getEquipCardInfo(person, cd.id);
				boolean hasEquip = false;
				if(info!=null)
					hasEquip = true;
				if(info==null){
					info = person.cards.getUnEquipCardInfo(cd.id);
				}
				if(info!=null){
					pt.put(hasEquip ? 1 : 0);
					pt.put(info.level);
					try {
						q = ObjectAccessor.createGameItem(cd.itemId).template.quality;
						pt.putInt(CardUpGradeCall.getUpGradeExp(q, info.level));
					} catch (Exception e) {
						pt.putInt(0);
					}
					pt.putUTF(cs.getEnhanceDesc(info.cardId, info.level));
					pt.putUTF(/*"下级属性:"+*/cs.getEnhanceDesc(info.cardId, info.level+1));
					if(info.level==0)
						pt.putInt(0);
					else
						pt.putInt(CardUpGradeCall.getTotalExp(info.level, q));
				}else{
					//旧的卡片
					pt.put(0);
					pt.put(0);
					try {
						q = ObjectAccessor.createGameItem(cd.itemId).template.quality;
						pt.putInt(CardUpGradeCall.getUpGradeExp(q, 0));
					} catch (Exception e) {
						pt.putInt(0);
					}
					pt.putUTF(cs.getEnhanceDesc(cd.id, 0));
					pt.putUTF(/*"下级属性:"+*/cs.getEnhanceDesc(cd.id, 1));
					pt.putInt(0);
				}
			}
			p.send(pt);
		}
	}
}
