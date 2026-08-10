package peony.db;

import java.util.ArrayList;
import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.cards.CardService;
import com.pip.sanguo.data.Card;

public class MergeCardCall extends ClientSessionAsyncCall {

	protected int serial;
	protected Player player;
	protected int size;
	protected List<Integer> cardIds = new ArrayList<Integer>();
	protected List<Integer> itemIds = new ArrayList<Integer>();
	protected int mergeId = 981;
	protected GameItem mergeItem = null;
	protected CardService cardService = Server.server.getServiceRegistry().getCardService();

	public MergeCardCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.size = packet.getInt();
		for (int i = 0; i < this.size; i++) {
			int itemId = packet.getInt();
			int cardId = CardService.getCardId(itemId);
			cardIds.add(cardId);
			itemIds.add(itemId);
		}
		this.player = (Player) session.getClient();
	}
	
	

	public void callFinish() throws Exception {
		try {
			process();
			Packet pt = new Packet(OpCode.CARD_MERGE_SERVER);
			pt.putInt(serial);
			pt.putInt(mergeItem.template.id);
			pt.putUTF(mergeItem.template.name);
			session.send(pt);
		} catch (Exception e) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.CARD_MERGE_SERVER, e.getMessage());
		}
	}

	public void run() {
		addToClientSession();
	}

	public void process() throws Exception {
		if (cardIds.size() < 2) {
			throw new Exception(peony.Messages.STRING_00487);
		}
		String str = cardService.getUserFormulaKey(cardIds);
		if(str == null || "".equals(str)){
			throw new Exception(peony.Messages.STRING_00488);
		}
		Card card = cardService.findCardFormula(str);
		if(card == null){
			throw new Exception(peony.Messages.STRING_00488);
		}
		PlayerTransaction tx = player.newTransaction("MJE");
		for (int i = 0; i < itemIds.size(); i++) {
			GameItem gi = player.bag.removeGameItemIngoreInstanceId(itemIds.get(i), cardService.getFormulaCount(itemIds.get(i), card.id), tx, true);
//			GameItem gi = player.bag.removeGameItem(itemIds.get(i),
//					GameItem.GENERAL_INSTANCEID, cardService.getFormulaCount(itemIds.get(i), card.id), tx, true);
			if (gi == null) {
				tx.rollback();
				throw new Exception(peony.Messages.STRING_00489);
			}
		}
		if (player.bag.removeGameItem(mergeId, GameItem.GENERAL_INSTANCEID, 1,
				tx, true) == null) {
			tx.rollback();
			throw new Exception(peony.Messages.STRING_00490);
		}
		
		try {
			int money = 10000;
			player.decMoney(money, tx, true);
		} catch (NoEnoughValueException ex) {
			tx.rollback();
			throw new Exception(peony.Messages.STRING_00129);
		}

		// ·ÅÈë±³°ü
		mergeItem = ObjectAccessor.createGameItem(card.itemId);
		if(mergeItem != null){
			if (!player.bag.addGameItem(mergeItem, 1, tx, true)) {
				tx.rollback();
				throw new Exception(peony.Messages.STRING_00491);
			}
			tx.commit();
		} else {
			tx.rollback();
			throw new Exception(peony.Messages.STRING_00492);
		}
	}
	
}
