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
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.fame.Fame;
import peony.service.fame.FameService;

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
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "你需要20级才能领取此物品");
					return;
				}
				GameItem item = p.bag.getGameItem(ItemUtil.ITEM_ONLINEEXP_CLICK);
				if(item!=null){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "你已经拥有此物品");
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
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "背包已满,请清理当前背包");
				}
				
			}else if(requestId==2){
				if(p.getAccount().getCity()!=null && p.getAccount().getCity().equals("Ôn châu") && Server.server.revision.equals("CMCC")){
					if(p.level<40){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "你需要40级才能领取此物品");
						return;
					}
					GameItem item = p.bag.getGameItem(ItemUtil.ITEM_ONLINT_GETMONEY);
					if(item!=null){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "你已经拥有此物品");
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
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "背包已满,请清理当前背包");
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "不能领取物品!");
				}
			}else if(requestId==3){
				FameService service = Server.server.getServiceRegistry().getFameService();
				Fame fame = service.getFame(-p.id);
				if(fame!=null){
					int lastGetDay = p.pool.getInt(Player.PROPERTY_FAME_GETITEM_DAY, 0);
					if(lastGetDay==Time.day){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "每天只能领取一次");
						return;
					}
					int itemId = FameService.getItem;
					if(itemId<=0){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "暂时不能领取该物品");
						return;
					}
					PlayerTransaction tx = p.newTransaction("FAMEITEM");
					GameItem item = ObjectAccessor.createGameItem(itemId);
					try {
						p.bag.addGameItemComplete(item, 1, tx, true);
						tx.commit();
						p.pool.setInt(Player.PROPERTY_FAME_GETITEM_DAY, Time.day);
					} catch (NoEnoughSpaceException e) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "Túi đồ đã đầy");
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "无法获得演武奖励");
				}
			}else if(requestId>0){//此case是通用的下方物品没有什么限制，如要特殊处理吧特殊case写在上面
				int times = p.pool.getInt(Player.PROPERTY_GET_LIBAO_TIMES +requestId);
				if(times > 0){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "您已领取过该物品!");
					return;
				}
				PlayerTransaction tx = p.newTransaction("NPC");
				GameItem item = ObjectAccessor.createGameItem(requestId);
				try {
					p.bag.addGameItemComplete(item, 1, tx, true);
					tx.commit();
					Packet pt = new Packet(OpCode.GETITEM_FROM_NPC_SERVER);
					pt.putInt(serial);
					p.send(pt);
					p.pool.setInt(Player.PROPERTY_GET_LIBAO_TIMES +requestId,1);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "背包已满,请清理当前背包");
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "领取物品错误!");
			}
		}
	}

}
