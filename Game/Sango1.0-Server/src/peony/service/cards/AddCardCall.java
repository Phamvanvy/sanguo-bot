package peony.service.cards;

import java.text.MessageFormat;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.itemenhance.ItemEnhance;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * ¿¨Æ¬ÏâÇ¶£¨¸½Ä§£©
 * @author dchen
 */
public class AddCardCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int cardId;
	protected int equItemID;
	protected int equInstanceID;
	protected int hole;
	protected int mehtod;
	protected ItemEnhance itemEnh;
	protected Player player;
	protected int decEnergy;
	
	public AddCardCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.cardId = packet.getInt();
		this.equItemID = packet.getInt();
		this.equInstanceID = packet.getInt();
		this.hole = packet.getByte();
		this.mehtod = packet.getByte();
		this.player = (Player)session.getClient();
	}
	
	public void callFinish() throws Exception {
		try {
			go();
			Packet pt = new Packet(OpCode.CARD_ADDTOEQU_SERVER);
			pt.putInt(serial);
			pt.putShort(decEnergy);
			pt.put(itemEnh.toClientBytes());
			session.send(pt);
		} catch (Exception e) {
			String err = e.getMessage();
			ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_ADDTOEQU_CLIENT, err);
		}
	}
	
	public void run() {
		addToClientSession();
	}
	
	protected void go() throws Exception{
		if(player!=null){
			CardService service = Server.server.getServiceRegistry().getCardService();
		    Object[] obj = ItemUtil.findPlayerEquipment(player, equItemID, equInstanceID);
		    if (obj == null)
		        throw new Exception(peony.Messages.STRING_00015);
		    GameItem gi = (GameItem)obj[0];
		    if (gi.template.equipment == null)
		        throw new Exception(peony.Messages.STRING_00327);
		    if (gi.object == null)
		        gi.object = new ItemEnhance();
		    if (!(gi.object instanceof ItemEnhance))
		        throw new Exception(peony.Messages.STRING_00017);
		    
		    itemEnh = (ItemEnhance)gi.object;
		    if (hole < 0 || hole >= itemEnh.addCardHole + gi.template.equipment.initCardCount) {
		        throw new Exception(peony.Messages.STRING_00328);
		    }
		    
		    int currentEnergy = service.getCardEnergy(player, cardId, false);
		    int maxEnergy = service.getCardEnergy(player, cardId, true);
		    if(mehtod==1){
		    	if(currentEnergy<CardService.MAX_FLASHCARD_ENERGY || maxEnergy<CardService.MAX_FLASHCARD_ENERGY)
		    		throw new Exception(MessageFormat.format(peony.Messages.STRING_00329, CardService.MAX_FLASHCARD_ENERGY));
		    	service.setCardEnergy(player, cardId, 0, maxEnergy);
		    	this.decEnergy = CardService.MAX_FLASHCARD_ENERGY;
		    }else if(mehtod==0){
		    	if(currentEnergy<CardService.MAX_GENERALCARD_ENERGY || maxEnergy<CardService.MAX_GENERALCARD_ENERGY)
		    		throw new Exception(MessageFormat.format(peony.Messages.STRING_00329, CardService.MAX_GENERALCARD_ENERGY));
		    	service.setCardEnergy(player, cardId, currentEnergy-CardService.MAX_GENERALCARD_ENERGY, maxEnergy);
		    	this.decEnergy = CardService.MAX_GENERALCARD_ENERGY;
		    }
		    String qualityPool = service.getPropertyOfCardQuality(cardId);
		    int qulity = player.pool.getInt(qualityPool, 0);
//		    //Í³¼Æ¿¨Æ¬¸½Ä§³É¾Í
//		    Card tempCd = service.getCardByCardId(cardId);
//		    if(tempCd!=null){
//		    	StatService statService = Server.server.getServiceRegistry().getStatService();
//		    	statService.addCardAchieve(player, tempCd);  
//		    }
		    if(qulity==1 && mehtod==0)
		    	qulity = 0;
		    itemEnh.addCard(hole, cardId, qulity);
	    	String propertyAddToEquTime = service.getPropertyOfCardAddToEquTime(cardId, gi.template.id, gi.instanceId);
	    	String propertyAddCardQuality = service.getPropertyOfAddCardQuality(cardId, gi.template.id, gi.instanceId);
	    	player.refreshProperties(false);
		    player.pool.setLong(propertyAddToEquTime, System.currentTimeMillis());
		    player.pool.setInt(propertyAddCardQuality, qulity);
		    LogUtil.logAddCard(player, gi.template.id, gi.instanceId, hole, cardId, qulity);
		}
	}

}
