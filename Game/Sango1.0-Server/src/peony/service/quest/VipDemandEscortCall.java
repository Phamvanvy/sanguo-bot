package peony.service.quest;

import java.text.MessageFormat;

import peony.common.SyncIbuyCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.ShopService;

public class VipDemandEscortCall extends SyncIbuyCall {
	
	private final static int ITEM_ID_VIPBLUE 	= 4654;	//蓝色镖车2元宝
	private final static int ITEM_ID_VIPPURPLE 	= 4655;	//紫色镖车4元宝
	private final static int ITEM_ID_VIPORANGE 	= 4656;	//橙色镖车8元宝
	private final static int ITEM_ID_VIPMIL 	= 4655;	//双倍奖励=紫色镖车4元宝
	
	protected int serial;
	protected Player p;
	int convoyType;		//镖车类型（经验型，战功型）
	//int vipDemand;		//VIP要求(-1普通，1绿2蓝3紫4橙品质, 5双倍奖励)
	
	public VipDemandEscortCall(ClientSession session, Player player, int serial, int convoyType, int vipDemand) {
		super(session, null);
		this.serial = serial;
		this.p = player;
		this.convoyType = convoyType;
		//this.vipDemand = vipDemand;
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
			
			this.p.pool.setInt(Player.PROPERTY_ESCORTCAR_ISPANMONEY, 1);
			
			//int escortCarLv;
//			if(vipDemand == 2){	//VIP蓝色品质镖车
//				escortCarLv = 2;
//				p.pool.setInt(Player.PROPERTY_ESCORTCAR_LEVEL, escortCarLv);
//			}else if(vipDemand == 3){	//VIP紫色品质镖车
//				escortCarLv = 3;
//				p.pool.setInt(Player.PROPERTY_ESCORTCAR_LEVEL, escortCarLv);
//			}else if(vipDemand == 4){	//VIP橙色品质镖车
//				escortCarLv = 4;
//				p.pool.setInt(Player.PROPERTY_ESCORTCAR_LEVEL, escortCarLv);
//			}else if(vipDemand == 5){	//VIP双倍奖励
				p.pool.setInt(Player.PROPERTY_ESCORTCAR_ISVIPDOUBLE, 1);
			//}
			
			Packet pt = new Packet(OpCode.VIP_ESCORT_QUEST_SERVER);
			pt.putInt(serial);
			p.send(pt);
			
			try {
				EscortQuestService service = Server.server.getServiceRegistry().getEscortQuestService();
				service.startEscort(p, convoyType);
			} catch (EscortException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.VIP_ESCORT_QUEST_CLIENT, e.getMessage());
			}
		}
//		else{
//			ErrorHandler.sendErrorMessage(session, serial, OpCode.VIP_ESCORT_QUEST_CLIENT, errorMessage);
//		}
	}

	public void run() {
		//if(vipDemand > 0){
			try {
				int itemId = -1;
//				if(vipDemand == 2){	//VIP蓝色品质镖车
//					if(p.vipLevel < 4){
//						ErrorHandler.sendErrorMessage(session, serial, OpCode.VIP_ESCORT_QUEST_CLIENT, "您的VIP等级不够4级");
//						return;
//					}
//					itemId = ITEM_ID_VIPBLUE;
//				}else if(vipDemand == 3){	//VIP紫色品质镖车
//					if(p.vipLevel < 8){
//						ErrorHandler.sendErrorMessage(session, serial, OpCode.VIP_ESCORT_QUEST_CLIENT, "您的VIP等级不够8级");
//						return;
//					}
//					itemId = ITEM_ID_VIPPURPLE;
//				}else if(vipDemand == 4){	//VIP橙色品质镖车
//					if(p.vipLevel < 10){
//						ErrorHandler.sendErrorMessage(session, serial, OpCode.VIP_ESCORT_QUEST_CLIENT, "您的VIP等级不够10级");
//						return;
//					}
//					itemId = ITEM_ID_VIPORANGE;
//				}else if(vipDemand == 5){	//VIP双倍奖励
					if(p.vipLevel < 4){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.VIP_ESCORT_QUEST_CLIENT, "您的VIP等级不够4级");
						return;
					}
					itemId = ITEM_ID_VIPMIL;
				//}
				ShopService service = Server.server.getServiceRegistry().getShopService();
				int shopId = service.getShopByItemId(itemId).id;
				waitBuy(p, 0, shopId, itemId, 1, this);
			} catch (Exception e) {
				error(peony.Messages.STRING_00405);
			}
		//}
		addToClientSession();
	}
}
