package peony.service.cards;

import com.pip.sanguo.data.Card;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * Õª³ýÒÑÏâÇ¶µÄ¿¨Æ¬
 * @author dchen
 */
public class CardUnEquipCall extends ClientSessionAsyncCall {

	protected int serial;
	protected Player player;
	protected int index;
	protected int type;
	protected int owner;
	protected int cardId;
	
	boolean ownerPlayer;
	boolean ownerHorse;
	int removeIndex;
	
	public static final int TYPE_FROM_EQUIP = 0; //´Ó×°±¸Î»Õª³ý¿¨Æ¬
	public static final int TYPE_FROM_CARDS = 1; //´Ó¿¨²áÕª³ý¿¨Æ¬
	
	public CardUnEquipCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.type = packet.getByte();
		if(this.type==TYPE_FROM_EQUIP){
			this.owner = packet.getByte();
			this.index = packet.getByte();
		}else if(this.type==TYPE_FROM_CARDS){
			this.cardId = packet.getInt();
		}
		this.player = (Player)session.getClient();
	}

	public void run() {
		addToClientSession();
	}
	
	public void callFinish() throws Exception {
		if(player!=null){
			try {
				if(type==TYPE_FROM_EQUIP){
					//´Ó×°±¸Î»Õª³ý¿¨Æ¬
					CardInfo info=null;
					if(owner==CardService.TYPE_PLAYER)
						info=player.cards.unequpPlayerCard(index); //½ÇÉ«¿¨Æ¬
					else if(owner==CardService.TYPE_HORSE)
						info=player.cards.unequpHorseCard(index); //×øÆï¿¨Æ¬
					if(info!=null){
						cardId=info.cardId;
					}
				}else if(type==TYPE_FROM_CARDS){
					//´Ó¿¨²áÕª³ý¿¨Æ¬
					CardInfo cardInfo = player.cards.getEquipCardInfoByCardId(cardId);
					if(cardInfo==null)
						throw new CardException("´Ë¿¨Æ¬Ã»ÓÐÏâÇ¶");
					removeIndex = cardInfo.index;
					cardInfo.unEquip();
					ownerPlayer = player.cards.removeEquipCardInfo(cardInfo);
					ownerHorse = player.cards.removeHorseEquipCardInfo(cardInfo);
					player.cards.cardInfos.put(cardInfo.cardId, cardInfo);
				}
				CardService service = Server.server.getServiceRegistry().getCardService();
				Card card=service.getCardByCardId(cardId);
				int buffId=card.buff2Id;
				Buff currBuf=player.buffs.getBuffByID(buffId);
				if(buffId!=-1&&currBuf!=null){
					player.buffs.removeBuff(currBuf);
				}
				Packet pt = new Packet(OpCode.CARD_UNEQUIPCARD_SERVER);
				pt.putInt(serial);
				if(type==TYPE_FROM_EQUIP){
					pt.put(owner);
					pt.put(index);
					LogUtil.logUnEquipCard(player, cardId, owner, index);
				}else{
					if(ownerPlayer){
						pt.put(0);
					}else if(ownerHorse){
						pt.put(1);
					}else{
						pt.put(0);
					}
					pt.put(removeIndex);
					LogUtil.logUnEquipCard(player, cardId, ownerPlayer?0:1, removeIndex);
				}
				pt.put(card.prorertyType);
				session.send(pt);
				if(player.horse!=null)
					player.horse.refreshProperties(false, player);
				player.refreshProperties(false);
			} catch (CardException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_UNEQUIPCARD_CLIENT, e.getMessage());
			}
		}
	}

}
