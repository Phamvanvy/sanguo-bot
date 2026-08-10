package peony.db;

import java.util.Date;
import java.util.Map;
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
import peony.game.nation.Nation;
import peony.game.nation.NationService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.VIP.VipPrivilegeService;
import peony.service.activity.Activity;
import peony.service.activity.ActivityService;
import peony.service.activity.FestivalConvoyActivity;
import peony.service.activity.FestivalGetItemActivity;
import peony.service.fame.Fame;
import peony.service.fame.FameService;

public class GetItemFromNpcCall extends ClientSessionAsyncCall {
	
	int serial;
//	int npcId;
	int requestId;
	public static int[] canGetItems = {3523, 3423 , 3906, 4385};
	
	public static int ID_JIAFUFEI = 3906;//假付费奖励物品
	
	public GetItemFromNpcCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
//		this.npcId = packet.getInt();
		this.requestId = packet.getInt();
	}

	public void callFinish() throws Exception {
	}

	public void run() {
		Player p = (Player)session.getClient();
		if(p!=null){
			if(requestId==1){
				if(p.level<15){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01067);
					return;
				}
				GameItem item = p.bag.getGameItem(ItemUtil.ITEM_ONLINEEXP_CLICK);
				if(item!=null){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01068);
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
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01069);
				}
				
			}else if(requestId==2){
				if(p.getAccount().getCity()!=null && p.getAccount().getCity().equals(peony.Messages.STRING_00385) && Server.REVISION_TYPE_CMCC.equals(Server.server.revision)){
					if(p.level<40){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01070);
						return;
					}
					GameItem item = p.bag.getGameItem(ItemUtil.ITEM_ONLINT_GETMONEY);
					if(item!=null){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01068);
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
						Server.server.getServiceRegistry().getMailService().sendSystemMail(
							p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01346, "", 0, item, 1, "ACTV");
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01071);
				}
			}else if(requestId==3){
				FameService service = Server.server.getServiceRegistry().getFameService();
				Fame fame = service.getFame(-p.id);
				if(fame!=null){
					int lastGetDay = p.pool.getInt(Player.PROPERTY_FAME_GETITEM_DAY, 0);
					if(lastGetDay==Time.day){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_00670);
						return;
					}
					int itemId = FameService.getItem;
					if(itemId<=0){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01072);
						return;
					}
					PlayerTransaction tx = p.newTransaction("FAMEITEM");
					GameItem item = ObjectAccessor.createGameItem(itemId);
					try {
						p.bag.addGameItemComplete(item, 1, tx, true);
						NationService nationService = Server.server.getServiceRegistry().getNationService();
						Nation nation = nationService.getNationByFaction(fame.faction);
						Map<Integer,Integer> topFame = service.topFame;
						if(topFame !=null && topFame.size()>0){
							if(topFame.get(p.faction)!=null && topFame.get(p.faction)== p.id && nation.pool.getInt(Nation.PROPERTY_GET_KINGBUFF, 0)==0){
								int buffItemId = FameService.rewardBuff;
								GameItem buffItem = ObjectAccessor.createGameItem(buffItemId);
								p.bag.addGameItemComplete(buffItem, 1, tx, true);
								nation.pool.setInt(Nation.PROPERTY_GET_KINGBUFF, 1);
							}
						}
						tx.commit();
						p.pool.setInt(Player.PROPERTY_FAME_GETITEM_DAY, Time.day);
						Packet pt = new Packet(OpCode.GETITEM_FROM_NPC_SERVER);
						pt.putInt(serial);
						p.send(pt);
					} catch (NoEnoughSpaceException e) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_00555);
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01073);
				}
			}else if(requestId == 4){
			    int itemId = VipPrivilegeService.getBuffItem(p.vipLevel);
			    if(itemId ==0){
			    	ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "VIP等级不够");
					return;
			    }
			    int lastGetDay = p.pool.getInt(VipPrivilegeService.PROPERTY_CYCLEINSTANCE_DAY,0);
			    if(lastGetDay == Time.day){
			    	ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_00670);
					return;
			    }
			    PlayerTransaction tx = p.newTransaction("VIPCYCLEINSTANCE");
				GameItem item = ObjectAccessor.createGameItem(itemId);
			    try {
					p.bag.addGameItemComplete(item, 1, tx, true);
					tx.commit();
					p.pool.setInt(VipPrivilegeService.PROPERTY_CYCLEINSTANCE_DAY, Time.day);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					Server.server.getServiceRegistry().getMailService().sendSystemMail(
						p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01346, "vip荣誉塔物品", 0, item, 1, "VIPCYCLEINSTANCE");
				}
				Packet pt = new Packet(OpCode.GETITEM_FROM_NPC_SERVER);
				pt.putInt(serial);
				p.send(pt);
			
			}else if(requestId == 5){//节日期间领取物品
				ActivityService activityService = Server.server.getServiceRegistry().getActivityService();
				Activity activity = activityService.getActivityByImpClass(FestivalGetItemActivity.class.getSimpleName());
				if(activity!=null){
					Date dateNow = new Date();
					try{
						if(dateNow.after(activity.getSchedule().stopTime)){
							ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "活动已经结束");
							return;
						}
					}catch(Exception e){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "活动已经结束");
						return;
					}
					try {
						FestivalGetItemActivity.getReward(p);
						Packet pt = new Packet(OpCode.GETITEM_FROM_NPC_SERVER);
						pt.putInt(serial);
						p.send(pt);
					} catch (Exception e) {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, e.getMessage());
						return;
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "活动尚未开始");
					return;
				}
			}else if(requestId == 6){//端午领取物品
				ActivityService activityService = Server.server.getServiceRegistry().getActivityService();
				Activity activity = activityService.getActivityByImpClass(FestivalConvoyActivity.class.getSimpleName());
				if(activity!=null){
					Date dateNow = new Date();
					try{
						if(dateNow.after(activity.getSchedule().stopTime)){
							ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "活动已经结束");
							return;
						}
					}catch(Exception e){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "活动已经结束");
						return;
					}
					try {
						FestivalConvoyActivity.getReward(p);
						Packet pt = new Packet(OpCode.GETITEM_FROM_NPC_SERVER);
						pt.putInt(serial);
						p.send(pt);
					} catch (Exception e) {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, e.getMessage());
						return;
					}
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "活动尚未开始");
					return;
				}
			}else if(requestId>0){//此case是通用的下方物品没有什么限制，如要特殊处理吧特殊case写在上面
				if(!canGet(requestId)){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01074);
					return;
				}
//				Creature creature = p.map.map.getCreatureById(npcId);
//				if(creature==null || !hasItemInNpc(creature.functionScript, requestId)){
//					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, "不能领取该物品!");
//					return;
//				}
				int times = p.pool.getInt(Player.PROPERTY_GET_LIBAO_TIMES +requestId);
				if(times > 0){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01075);
					return;
				}
				PlayerTransaction tx = p.newTransaction("NPC");
				GameItem item = ObjectAccessor.createGameItem(requestId);
				try {
					p.bag.addGameItemComplete(item, 1, tx, true);
					tx.commit();
					if(requestId == ID_JIAFUFEI){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01076);
						p.pool.setInt(Player.PROPERTY_GET_LIBAO_TIMES +requestId,1);
						return;
					}
					Packet pt = new Packet(OpCode.GETITEM_FROM_NPC_SERVER);
					pt.putInt(serial);
					p.send(pt);
					p.pool.setInt(Player.PROPERTY_GET_LIBAO_TIMES +requestId,1);
				} catch (NoEnoughSpaceException e) {
					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01069);
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.GETITEM_FROM_NPC_CLIENT, peony.Messages.STRING_01077);
			}
		}
	}
	
	public static boolean canGet(int requestId){
		for(int itemId : canGetItems){
			if(itemId==requestId)
				return true;
		}
		return false;
	}
	
	public static boolean hasItemInNpc(String functionScript, int itemId){
		String[] funcs = functionScript.split(";;;;");
    	for (int i = 0; i < funcs.length; i++) {
            String[] args = funcs[i].split("\\s+");  //类名和参数用空格分割
            try {
                if (args.length>1) {
                    if (args[0].equals("GeneralTouchAction")) {
                        if (args.length == 3) {
                            String str = args[2];
                            String itemStr = str.substring(4);
                        	try {
								int item = Integer.parseInt(itemStr);
								if(item==itemId)
									return true;
							} catch (Exception e) {
							}
                        }
                    }
                }
            } catch (Exception e) {
            }
    	}
    	return false;
	}

}
