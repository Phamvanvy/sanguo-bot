package peony.game;

import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopService;
import peony.service.stat.StatService;

public class HorseActiveCall extends ClientSessionAsyncCall implements NoItemShopBuyI{
	private static final Logger log = Logger.getLogger(HorseActiveCall.class);
	
	Player player = null;
	Horse horse;
	int serial;
	int horseInstanceId;
	public static int[] shopBuyItemIds = {1275,1283,200,201,202,203,1286,1287,1288,1289};
	public static int[] horseItemIds1 = {2475, 2476, 2477, 2478};
	public static int[] horseItemIds2 = {2580, 2581, 2582, 2583};
	public static int[] horseItemIds3 = {2585, 2586, 2587, 2588};
	public static int[] horseItemIds4 = {2605, 2591, 2592, 2593};
	public static int[] horseItemIds5 = {2595, 2606, 2597, 2598};
	public static int[] horseItemIds6 = {2600, 2601, 2602, 2603};
	public static int[] horseItemIds7 = {4000, 4001, 4002, 4003}; //ÄÏÂùÕ½Ïó
	public static int[] horseItemIds8 = {4005, 4006, 4007, 4008}; //ÔÂ¹¬ÓñÍÃ
	public static int[] horseItemIds9 = {4010, 4011, 4012, 4547}; //·ÉÃ«ÍÈ
	public static int[] horseItemIds10 = {4549, 4550, 4551, 4552};//°µÒ¹±¼À×
	
	public HorseActiveCall(ClientSession session,Packet packet) {
		super(session);
		player = (Player)session.getClient();
		serial = packet.getInt();
		horseInstanceId = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.HORSE_ACTIVE_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.HORSE_ACTIVE_CLIENT, errorMessage);
		}
	}
	public void run() {
		if(player!=null){
			horse = player.horseBag.getHorse(horseInstanceId);
			if(horse!=null){
				ShopService service = Server.server.getServiceRegistry().getShopService();
				try{
					int activeItemId = getActiveItemByHorseId(horse.itemId);
					int shopId = service.getShopByItemId(activeItemId).id;
					NoItemShopBuy ibuy = new NoItemShopBuy(player,serial,shopId,activeItemId,1,this,null);
					service.buy(player, ibuy);
					log.info("[HORSEACTIVE]"+LogUtil.getPlayerLogString(player)+"STATE[OK]");
				}catch(Exception e){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_ACTIVE_CLIENT, peony.Messages.STRING_00911);
					log.info("[HORSEACTIVE]"+LogUtil.getPlayerLogString(player)+"STATE[FAIL]");
					return;
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_ACTIVE_CLIENT, peony.Messages.STRING_01706);
			}
		}
	}

	public void process(Object[] o) {
		horse.freeHorseEndTime = 0;
		horse.setActive();
		horse.addIntPropertyChangedItem(player.changed, ChangedItem.HORSE_STATE, horse.state, false);
		horse.addStringPropertyChangedItem(player.changed, ChangedItem.HORSE_NAME, horse.name, false);
		addToClientSession();
	}

	public void procssFail(Object[] o) {
		error(peony.Messages.STRING_00405);
		addToClientSession();
	}
	
	public static int getActiveItemByHorseId(int horseId){
	   if(isIn(horseItemIds1,horseId)){
		   return shopBuyItemIds[0];
	   } else if(isIn(horseItemIds2,horseId)){
		   return shopBuyItemIds[1];
	   }else if(isIn(horseItemIds3,horseId)){
		   return shopBuyItemIds[2];
	   }else if(isIn(horseItemIds4,horseId)){
		   return shopBuyItemIds[3];
	   }else if(isIn(horseItemIds5,horseId)){
		   return shopBuyItemIds[4];
	   }else if(isIn(horseItemIds6,horseId)){
		   return shopBuyItemIds[5];
	   }else if(isIn(horseItemIds7,horseId)){
		   return shopBuyItemIds[6];
	   }else if(isIn(horseItemIds8,horseId)){
		   return shopBuyItemIds[7];
	   }else if(isIn(horseItemIds9,horseId)){
		   return shopBuyItemIds[8];
	   }else if(isIn(horseItemIds10,horseId)){
		   return shopBuyItemIds[9];
	   }
	   return -1;
	}
	
	public static boolean isIn(int[] horseIds,int horseId){
		for(int i=0;i<horseIds.length;i++){
			if(horseId == horseIds[i]){
				return true;
			}
		}
		return false;
	}

}
