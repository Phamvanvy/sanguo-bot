package peony.service.cards;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
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
 * 卡片镶嵌到装备位
 * @author dchen
 */
public class EquipCardCall extends ClientSessionAsyncCall {

	protected Player player;
	protected int serial;
	protected int cardId;
	protected int index;
	protected int type;
	protected int instanceId;
	
	public EquipCardCall(ClientSession session, Packet packet) {
		super(session);
		this.player = (Player)session.getClient();
		this.serial = packet.getInt();
		this.cardId = packet.getInt();
		this.index = packet.getByte();
		this.type = packet.getByte();
		try {this.instanceId = packet.getInt();} catch (Exception e) {}
	}

	public void run() {
		addToClientSession();
	}
	
	public void callFinish() throws Exception {
		if(player!=null){
			CardService service = Server.server.getServiceRegistry().getCardService();
			try {
				CardInfo unEquipCardInfo=null;
				if(type==CardService.TYPE_HORSE){
					unEquipCardInfo=player.cards.horseEquipCard(cardId, index);
				}else if(type==CardService.TYPE_PLAYER){
					unEquipCardInfo=player.cards.playerEquipCard(cardId, index);
				}
				Card card=service.getCardByCardId(cardId);
				if(type==CardService.TYPE_PLAYER ||(type==CardService.TYPE_HORSE&&player.horse!=null) ){
					int buffId=card.buff2Id;
					if(buffId!=-1&&player.buffs.getBuffByID(buffId)==null){
						CardInfo info=service.getEquipCardInfo(player, cardId);
						Buff buff=BuffUtil.createBuff(buffId,info.level , player, player, 0);
						player.buffs.addBuff(buff);
					}
					if(unEquipCardInfo!=null){
						Card cardUnEquip=service.getCardByCardId(unEquipCardInfo.cardId);
						if(cardUnEquip.buff2Id!=-1){
							player.buffs.removeBuff(cardUnEquip.buff2Id);
						}
					}
				}
				Packet pt = new Packet(OpCode.CARD_ADDTOEQUINDEX_SERVER);
				pt.putInt(serial);
				pt.put(index);
				pt.putInt(cardId);
				pt.putUTF(service.getCardByCardId(cardId).title);
				int level = player.cards.getEquipCardInfoByCardId(cardId).level;
				pt.put(level);
				pt.putUTF(service.getEnhanceDesc(cardId, level));
				int quality =  ObjectAccessor.createGameItem(service.getCardByCardId(cardId).itemId).template.quality;
				pt.put(quality);
				pt.put(card.prorertyType);
				if(unEquipCardInfo!=null){
					Card cardUnEquip=service.getCardByCardId(unEquipCardInfo.cardId);
					if(cardUnEquip!=null){
						pt.putInt(cardUnEquip.prorertyType);
					}else{
						pt.putInt(-1);
					}
				}else{
					pt.putInt(-1);
				}
				session.send(pt);
				if(player.horse!=null)
					player.horse.refreshProperties(false, player);
				player.refreshProperties(false);
				LogUtil.logEquipCard(player, cardId, type, index);
			} catch (CardException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_ADDTOEQUINDEX_CLIENT, e.getMessage());
			}
		}
	}

}
