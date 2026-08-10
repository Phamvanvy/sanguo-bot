package peony.service.quest;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Random;

import peony.common.SyncIbuyCall;
import peony.game.ErrorHandler;
import peony.game.HorseBag;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.ShopService;

public class ReFreshEscortCall extends SyncIbuyCall {
	
	private final static int ITEM_ID_VIPBLUE 	= 4654;	//蓝色镖车2元宝
	private final static int ITEM_ID_VIPPURPLE 	= 4655;	//紫色镖车4元宝
	private final static int ITEM_ID_VIPORANGE 	= 4656;	//橙色镖车8元宝
	
	protected static Random rnd = new Random();
	protected int serial;
	protected Player p;
	protected int count;
	protected int isVip;
	
	public ReFreshEscortCall(ClientSession session, Packet packet, Player player, int serial, int isVip) {
		super(session, null);
		this.serial = serial;
		this.p = player;
		this.isVip = isVip;
		count = this.p.pool.getInt(Player.PROPERTY_ESCORTCAR_REFRESHCOUNT, 0);	//刷新镖车次数
	}

	public void callFinish() throws Exception {
		if(success){
			int isPayMoney = p.pool.getInt(Player.PROPERTY_ESCORTCAR_ISPANMONEY, 0);
			if(isPayMoney == 0){	//第一次
				EscortQuestService.acceptCount++;
				if(EscortQuestService.acceptCount >= EscortQuestService.ESCORT_QUEST_MAX){
					Server.server.getServiceRegistry().getChatService().sendWorldMessage("本时段的押镖任务已全部被领取");
				}else if(EscortQuestService.acceptCount > EscortQuestService.ESCORT_QUEST_MAX - 10){
					Server.server.getServiceRegistry().getChatService().sendWorldMessage(
					MessageFormat.format("押镖任务只剩{0}次", EscortQuestService.ESCORT_QUEST_MAX - EscortQuestService.acceptCount));
				}
			}
			if(isVip == 0){
				count++;
			}
			this.p.pool.setInt(Player.PROPERTY_ESCORTCAR_REFRESHCOUNT, count);
			this.p.pool.setInt(Player.PROPERTY_ESCORTCAR_ISPANMONEY, 1);
			Packet pt = new Packet(OpCode.ACCEPT_REFRESH_ESCORT_SERVER);
			pt.putInt(serial);
			
			int escortCarLv;
			if(isVip == 1){
				if(p.vipLevel >= 10){	//VIP橙色品质镖车
					escortCarLv = 4;
				}else if(p.vipLevel >= 8){	//VIP紫色品质镖车
					escortCarLv = 3;
				}else if(p.vipLevel >= 4){	//VIP蓝色品质镖车
					escortCarLv = 2;
				}else{
					escortCarLv = 1;	//绿
				}
			}else{
				int randNum = rnd.nextInt(100);
				if (randNum < 75) {
			    	escortCarLv = 1;	//绿
			    }else if(randNum >= 75 && randNum < 94){
			    	escortCarLv = 2;	//蓝
			    }else if(randNum >= 94 && randNum < 98){
			    	escortCarLv = 3;	//紫
			    }else{
			    	escortCarLv = 4;	//橙
			    }
			}
			p.pool.setInt(Player.PROPERTY_ESCORTCAR_LEVEL, escortCarLv);
			pt.put(escortCarLv);
			
			pt.putString(EscortQuestService.getImoney(p, EscortQuestService.IMONEY_ITEM_REFRESH, count));
			
			p.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, 0, OpCode.ACCEPT_REFRESH_ESCORT_CLIENT, peony.Messages.STRING_00924);
		}
	}

	public void run() {
		try {
			ShopService service = Server.server.getServiceRegistry().getShopService();
			int itemId = -1;
			int itemCount = 1;
			if(isVip == 1){	//直接获得VIP镖车
				if(p.vipLevel >= 10){	//VIP橙色品质镖车
					itemId = ITEM_ID_VIPORANGE;
				}else if(p.vipLevel >= 8){	//VIP紫色品质镖车
					itemId = ITEM_ID_VIPPURPLE;
				}else if(p.vipLevel >= 4){	//VIP蓝色品质镖车
					itemId = ITEM_ID_VIPBLUE;
				}else {
					ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCEPT_REFRESH_ESCORT_CLIENT, "您的VIP等级不够4级");
					return;
				}
				itemCount = 1;
			}else{
				itemId = EscortQuestService.IMONEY_ITEM_REFRESH;
				float price = 0;
				try {
					price = service.getItemPriceInAppointShop(itemId, -1);
				} catch (Exception e) {
					price = service.getFilterItemPrice(itemId);
				}
				
				for(int i=0; i<count; i++){
					itemCount = itemCount * 2;
					float yaunbao = (price*itemCount) / 36;
					if(yaunbao > EscortQuestService.MAX_IB_COUNT){
						itemCount = itemCount / 2;
						break;
					}
				}
			}
			int shopId = service.getShopByItemId(itemId).id;
			waitBuy(p, 0, shopId, itemId, itemCount, this);
			addToClientSession();
		} catch (Exception e) {
			
		}
	}

}
