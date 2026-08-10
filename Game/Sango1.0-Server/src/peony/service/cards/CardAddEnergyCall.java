package peony.service.cards;

import com.pip.sanguo.data.Card;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * ¿¨Æ¬³äÄÜ
 * @author dchen
 */
public class CardAddEnergyCall extends ClientSessionAsyncCall {

	protected int serial;
	protected Player player;
	protected int cardId;
	protected int energy;
	protected int maxEnergy;
	protected int energyType;
	
	public static int CARD_WANNENG_ENERGY = 3823;
	
	public CardAddEnergyCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.cardId = packet.getInt();
		this.energyType = packet.get();
		this.player = (Player)session.getClient();
	}
	
	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.CARD_ADDENERGY_SERVER);
			pt.putInt(serial);
			pt.putShort(energy);
			pt.putShort(maxEnergy);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_ADDENERGY_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(player!=null){
			CardService cardService = Server.server.getServiceRegistry().getCardService();
			synchronized (cardService) {
				Card cd = cardService.getCardByCardId(cardId);
				boolean isFlash = false;
				if(cd != null){
					int itemId = 0;
					if(energyType == 0){
						itemId = cd.itemId;
					}else if(energyType == 1){
						itemId = CARD_WANNENG_ENERGY;
					}
					PlayerTransaction tx = player.newTransaction("CARD");
					GameItem it = player.bag.removeGameItemIngoreInstanceId(itemId, 1, tx, false);
					if (it == null) {
						tx.rollback();
						error(peony.Messages.STRING_00982);
					} else {
						tx.commit();
						player.pool.setInt(cardService.getPropertyOfPlayerCard(cardId), 1);
						int qulity = player.pool.getInt(cardService.getPropertyOfCardQuality(cardId),0);
						isFlash = qulity==1 ? true : false;
						if(!isFlash){
							isFlash = cardService.generateFlashCard(cd);
							if(isFlash){
								player.pool.setInt(cardService.getPropertyOfCardQuality(cardId), 1);
							}
						}
						//¿¨Æ¬³äÄÜ
						int oldEnergy = cardService.getCardEnergy(player, cardId, false);;
						int oldMaxEnergy = cardService.getCardEnergy(player, cardId, true);;
						int cardEnergy = 0;
						if(energyType == 0){
							cardEnergy = cardService.generateCardEnergy(player, cd);
						}else if(energyType == 1){
							Card tempCd = cardService.getCardByCardId(cardId);
							if(tempCd != null){
								if(tempCd.star == 1){
									cardEnergy = 200;
								}else if(tempCd.star == 2){
									cardEnergy = 150;
								}else if(tempCd.star == 3){
									cardEnergy = 100;
								}else if(tempCd.star == 4){
									cardEnergy = 50;
								}else if(tempCd.star == 5){
									cardEnergy = 25;
								}
							}
						}
						cardService.addCardEnergy(player, cd, cardEnergy, isFlash);
						this.energy = cardService.getCardEnergy(player, cardId, false);
						this.maxEnergy = cardService.getCardEnergy(player, cardId, true);
						LogUtil.logAddCardEnergy(player, cardId, oldEnergy, oldMaxEnergy, this.energy, this.maxEnergy);
					}
				} else {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_ADDENERGY_CLIENT, peony.Messages.STRING_00503);
					return;
				}
			}
		}
		addToClientSession();
	}
}
