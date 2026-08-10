package peony.service.cards;

import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.item.Item;
import peony.common.ClientSessionAsyncCall;
import peony.game.GameItem;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.TransactionBagGrid;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;

public class CardAutoAddEnergyCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int groupId;
	protected Player p;

	public CardAutoAddEnergyCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.groupId = packet.getInt();
		this.p = (Player) session.getClient();
	}

	public void callFinish() throws Exception {
		if (success) {
			CardService cs = Server.server.getServiceRegistry().getCardService();
			Packet pt = new Packet(OpCode.CARD_AUTOADDENERGY_SERVER);
			pt.putInt(serial);
			//CardList
			int groupSize = cs.cardGroups.size();
			pt.putInt(groupSize);
			for (CardGroup group : cs.cardGroupList) {
				pt.putInt(group.groupId);
				pt.putUTF(group.cardGroupName);
				int cnt = p.pool.getInt(cs.getPropertyOfPlayerSuit(group.groupId), 0);
				pt.putInt(cnt);
				pt.putInt(group.cards.size());
				pt.putInt((int) Math.floor((cnt * 100)/ group.cards.size()));
			}
			int totalCnt = p.pool.getInt(CardService.PROPERTY_HAVECARD, 0);
			pt.putInt(totalCnt);
			pt.putInt(cs.totalcount);
			pt.putInt((int) Math.floor((totalCnt * 100) / cs.totalcount));
			//CardDetailList
			CardGroup group = cs.cardGroups.get(groupId);
			// 判断是否显示卡片名称
			boolean showName = p.pool.getInt(
					cs.getPropertyOfShowCardName(group.groupId), 0) == 1;
			pt.putInt(group.cards.size());
			for (Card cd : group.cards) {
				// 是否收集
				boolean hasMatch = p.pool.getInt(cs.getPropertyOfPlayerCard(cd.id)) == 1;
				pt.put(hasMatch == true ? 1 : 0);
				//是否闪卡
				boolean isFlash = p.pool.getInt(cs.getPropertyOfCardQuality(cd.id)) == 1;
				if (showName || hasMatch) {
					pt.putUTF(cd.title + (isFlash?peony.Messages.STRING_00063:""));
				} else {
					pt.putUTF("??");
				}
				// 配方描述
				String fd = cs.formulaDesc.get(cd.id);
				if (fd == null || "".equals(fd)) {
					pt.putUTF(peony.Messages.STRING_00693);
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
				boolean isPictureCard = cs.isPictureCard(cd);
				pt.put(isPictureCard ? 1 : 0);
				pt.putShort(cs.getCardEnergy(p, cd.id, false));
				pt.putShort(cs.getCardEnergy(p, cd.id, true));
				pt.put(cd.star);
				try{
					int buffLevel = cd.buffLevel1;
					if(isFlash){
						buffLevel = cd.buffLevel2;
					}
					String buffDec = cs.getBuffDesc(cd.id, buffLevel);
					pt.putUTF(buffDec);
				}catch(Exception e){
					pt.putUTF(peony.Messages.STRING_00064);
				}
			}
			session.send(pt);
		}
	}

	public void run() {
		if (p != null) {
			CardService cardService = Server.server.getServiceRegistry()
					.getCardService();
			synchronized (cardService) {
				for (TransactionBagGrid grid : p.bag.getGrids()) {
					GameItem item = grid.getItem();
					if (item != null) {
						if (item.template.itemType == Item.TYPE_CARD) {
							int count = p.bag.getGameItemCount(item.template.id);
							for (int i = 1; i <= count; i++) {
								Card card = cardService.getCardByItemId(item.template.id);
								PlayerTransaction tx = p.newTransaction("CARD");
								GameItem it = p.bag.removeGameItemIngoreInstanceId(item.template.id, 1, tx,false);
								if(it == null){
									tx.rollback();
									continue;
								} else {
									tx.commit();
									boolean isFlash = false;
									boolean hasMatch = p.pool.getInt(cardService.getPropertyOfPlayerCard(card.id), 0) == 1;
									if (!hasMatch) {// 此卡位还未收藏过
										int groupId = card.suiteId;
										if (groupId != -1) {
											p.pool.setInt(cardService.getPropertyOfPlayerSuit(groupId),p.pool.getInt(
																			cardService.getPropertyOfPlayerSuit(groupId),0) + 1);
										}
										p.pool.setInt(CardService.PROPERTY_HAVECARD,p.pool.getInt(CardService.PROPERTY_HAVECARD,0) + 1);
										p.pool.setInt(cardService.getPropertyOfPlayerCard(card.id),1);
										isFlash = cardService.generateFlashCard(card);
										if (isFlash) {
											p.pool.setInt(cardService.getPropertyOfCardQuality(card.id),1);
										} else {
											p.pool.setInt(cardService.getPropertyOfCardQuality(card.id),0);
										}
										// 卡片充能
										int cardEnergy = cardService.generateCardEnergy(p, card);
										cardService.addCardEnergy(p, card,cardEnergy, isFlash);
										// 卡片收藏事件
										Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_COLLECT_CARD,p, groupId));
									} else {// 此卡位已经藏过
											int qulity = p.pool.getInt(cardService.getPropertyOfCardQuality(card.id),0);
											isFlash = qulity == 1 ? true : false;
											if (!isFlash) {
												isFlash = cardService.generateFlashCard(card);
												if (isFlash) 
												{
													p.pool.setInt(cardService.getPropertyOfCardQuality(card.id),1);
												}
											}
											int enrgy = cardService.generateCardEnergy(p, card);
											cardService.addCardEnergy(p, card,enrgy, isFlash);
										}
									}
								}
							}
						}
					}
				}
			}
		addToClientSession();
	}	
}
