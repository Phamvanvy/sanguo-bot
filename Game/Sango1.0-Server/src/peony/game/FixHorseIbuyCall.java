package peony.game;

import peony.common.SyncIbuyCall;
import peony.net.ClientSession;
import peony.net.Packet;

public class FixHorseIbuyCall extends SyncIbuyCall {

	protected int serial;
	protected Player player;
	protected int horseInstId;
	
	public FixHorseIbuyCall(ClientSession session, Packet packet, Player player, int serial, int horseInstId) {
		super(session, null);
		this.serial = serial;
		this.player = player;
		this.horseInstId = horseInstId;
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.HORSE_FIXFAILURE_SERVER);
			pt.putInt(serial);
			player.send(pt);
			ErrorHandler.sendErrorMessage(session, 0, OpCode.HORSE_FIXFAILURE_CLIENT, "保留成功");
			LogUtil.logHorseFixFail(player, horseInstId, 0);
		}else{
			player.horseBag.removeHorse(horseInstId);
			ErrorHandler.sendErrorMessage(session, 0, OpCode.HORSE_FIXFAILURE_CLIENT, "您的元宝不足，很遗憾坐骑消失了。");
			LogUtil.logHorseFixFail(player, horseInstId, 1);
		}
	}

	public void run() {
		PlayerTransaction tx = player.newTransaction("fixFail");
		try {
			int shopId = Server.server.getServiceRegistry().getShopService().getShopByItemId(HorseBag.decIMoneyItem).id;
			waitBuy(player, 0, shopId, HorseBag.decIMoneyItem, 1, this);
			addToClientSession();
		} catch (Exception e) {
			// 记录日志
			LogUtil.logHorseFixFail(player, horseInstId, 2);
			player.horseBag.removeHorse(horseInstId);
			tx.rollback();
		}
	}

}
