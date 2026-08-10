package peony.db;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class GetItemFromNpcCall extends ClientSessionAsyncCall {
	
	int serial;
	int requestId;
	
	
	public GetItemFromNpcCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.requestId = packet.getInt();
	}

	public void callFinish() throws Exception {
	}

	public void run() {
		Player p = (Player)session.getClient();
		if(p!=null){
			if(requestId==1){
				if(p.level<15){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "你需要20級才能領取此物品");
					return;
				}
				GameItem item = p.bag.getGameItem(ItemUtil.ITEM_ONLINEEXP_CLICK);
				if(item!=null){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "你已經擁有此物品");
					return;
				}
				PlayerTransaction tx = p.newTransaction("NPC");
				item = ObjectAccessor.createGameItem(ItemUtil.ITEM_ONLINEEXP_CLICK);
				try {
					p.bag.addGameItemComplete(item, 1, tx, true);
					tx.commit();
					p.pool.setLong(Player.PROPERTY_CLICKEXP_START_TIME, System.currentTimeMillis());
					p.pool.setLong(Player.PROPERTY_CLICKEXP_CUMULATE_TIME, 0L);
					Packet pt = new Packet(OpCode.GETITEM_FROM_NPC_SERVER);
					pt.putInt(serial);
					p.send(pt);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "背包已滿,請清理當前背包");
				}
				
			}else if(requestId==2){
				if(p.getAccount().getCity()!=null && p.getAccount().getCity().equals("溫州") && Server.server.revision.equals("CMCC")){
					if(p.level<40){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "你需要40級才能領取此物品");
						return;
					}
					GameItem item = p.bag.getGameItem(ItemUtil.ITEM_ONLINT_GETMONEY);
					if(item!=null){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "你已經擁有此物品");
						return;
					}
					PlayerTransaction tx = p.newTransaction("NPC");
					item = ObjectAccessor.createGameItem(ItemUtil.ITEM_ONLINT_GETMONEY);
					try {
						p.bag.addGameItemComplete(item, 1, tx, true);
						tx.commit();
						p.pool.setLong(Player.PROPERTY_CLICKMONEY_START_TIME, System.currentTimeMillis());
						p.pool.setLong(Player.PROPERTY_CLICKMONEY_CUMULATE_TIME, 0L);
						Packet pt = new Packet(OpCode.GETITEM_FROM_NPC_SERVER);
						pt.putInt(serial);
						p.send(pt);
					} catch (NoEnoughSpaceException e) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "背包已滿,請清理當前背包");
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "不能領取物品!");
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "領取物品錯誤!");
			}
		}
	}

}
