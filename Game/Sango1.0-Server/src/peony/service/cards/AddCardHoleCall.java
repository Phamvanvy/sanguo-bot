package peony.service.cards;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.itemenhance.ItemEnhance;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * ×°±¸¿¨Æ¬¸½Ä§Î»¼¤»î
 * @author dchen
 */
public class AddCardHoleCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int equItemID;
	protected int equInstanceID;
	protected Player player;
	protected ItemEnhance itemEnh;
	public static int ADDHOLE_DECITEM1 = 2550; //µÍ¼¶´¥Ä§·û
	public static int ADDHOLE_DECITEM2 = 2551; //¸ß¼¶´¥Ä§·û
	
	public AddCardHoleCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.equItemID = packet.getInt();
		this.equInstanceID = packet.getInt();
		this.player = (Player)session.getClient();
	}
	
	public void callFinish() throws Exception {
		try {
			go();
			Packet pt = new Packet(OpCode.CARD_ADDHOLE_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} catch (Exception e) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.CARD_ADDHOLE_CLIENT, e.getMessage());
		}
	}
	
	public void run() {
		addToClientSession();
	}
	
	protected void go() throws Exception {
		Object[] obj = ItemUtil.findPlayerEquipment(player, equItemID,equInstanceID);
		if (obj == null)
			throw new Exception(peony.Messages.STRING_00015);
		GameItem gi = (GameItem) obj[0];
		if (gi.template.equipment == null)
			throw new Exception(peony.Messages.STRING_01926);
		if (gi.object == null)
			gi.object = new ItemEnhance();
		if (!(gi.object instanceof ItemEnhance))
			throw new Exception(peony.Messages.STRING_00017);

		itemEnh = (ItemEnhance) gi.object;
		if (itemEnh.addCardHole + gi.template.equipment.initCardCount >= itemEnh.addMaxCardHole + gi.template.equipment.maxCardCount)
			throw new Exception(peony.Messages.STRING_01927);
		
		PlayerTransaction tx = player.newTransaction("ADDCARDHOLE");
		GameItem decItem = null;
		if(gi.template.useLevel<=59){
			decItem = player.bag.removeGameItemIngoreInstanceId(ADDHOLE_DECITEM1, 1, tx, false);
			if(decItem==null)
				decItem = player.bag.removeGameItemIngoreInstanceId(ADDHOLE_DECITEM2, 1, tx, false);
		}else{
			decItem = player.bag.removeGameItemIngoreInstanceId(ADDHOLE_DECITEM2, 1, tx, false);
		}
		if(decItem==null){
			tx.rollback();
			if(gi.template.useLevel<=59)
				throw new Exception(peony.Messages.STRING_01928);
			else
				throw new Exception(peony.Messages.STRING_01929);
		}
		tx.commit();
		itemEnh.addCardHole++;
		LogUtil.logAddCardHole(player, gi.template.id, gi.instanceId, itemEnh.addCardHole);
	}

}
