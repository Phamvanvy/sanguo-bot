package peony.game;

import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopService;

public class HorseActiveCall extends ClientSessionAsyncCall implements NoItemShopBuyI{
	private static final Logger log = Logger.getLogger(HorseActiveCall.class);
	
	Player player = null;
	Horse horse;
	int serial;
	int horseInstanceId;
	/** 激活坐骑购买物品Id */
    public static int HORSEACTIVE = 1275;
	
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
					int shopId = service.getShopByItemId(HORSEACTIVE).id;
					NoItemShopBuy ibuy = new NoItemShopBuy(player,serial,shopId,HORSEACTIVE,1,this,null);
					service.buy(player, ibuy);
					log.info("[HORSEACTIVE]"+LogUtil.getPlayerLogString(player)+"STATE[OK]");
				}catch(Exception e){
					ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_ACTIVE_CLIENT, "没有足够的元宝");
					log.info("[HORSEACTIVE]"+LogUtil.getPlayerLogString(player)+"STATE[FAIL]");
					return;
				}
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.HORSE_ACTIVE_CLIENT, "没有找到指定坐骑");
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
		error("扣费失败");
		addToClientSession();
	}

}
