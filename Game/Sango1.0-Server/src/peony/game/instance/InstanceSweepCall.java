package peony.game.instance;

import java.text.MessageFormat;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopService;

/**
 * 开始扫荡副本
 * @author mfou
 *
 */

public class InstanceSweepCall extends ClientSessionAsyncCall implements NoItemShopBuyI{
	
	int serial;
	int instanceId;
	Player player = null;
	InstanceSweep instance = null;
	int sweepTimes = 0;
	InstanceSweepService service = Server.server.getServiceRegistry().getInstanceSweepService();

	public InstanceSweepCall(Packet packet,ClientSession session) {
		super(session);
		serial = packet.getInt();
		instanceId = packet.getInt();
		player = (Player)session.getClient();
		instance = service.getSweepInstance(instanceId);
		
	}

	public void callFinish() throws Exception {
	   if(success){
		   InstanceSweep sweepInstance = new InstanceSweep(instanceId,Time.currentTimeMillis(Time.currTime)+instance.time*60*1000l);
		   player.sweepList.put(instance.id, sweepInstance);
		   if(!player.freeSweep.contains(instance.id)){
		      player.freeSweep.add(instanceId);
		   }
		   Packet pt = new Packet(OpCode.INSTANCE_SWEEP_SERVER);
		   pt.putInt(serial);
		   pt.putInt(instanceId);
		   pt.put(InstanceSweepService.TYPE_SWEEP);
		   pt.putInt(sweepTimes == 0?0:sweepTimes-1);
		   ShopService shopService = Server.server.getServiceRegistry().getShopService();
		   float price = (InstanceSweepService.PAY_OPENSWEEP[sweepTimes] * shopService.getItemPrice(NoItemShopBuy.YIYUANBAO))/36f;
		   pt.putString(String.valueOf(price));
		   pt.putInt(instance.time);
		   pt.put(1);
		   player.send(pt);
	   }
	}

	public void run() {
		if(player!=null){
 			if(instance!=null){
			   sweepTimes = player.pool.getInt(InstanceSweepService.getPropertyOfDayTimes(instanceId), 0);
			   if(sweepTimes == 0){
				   ErrorHandler.sendErrorMessage(session, serial, OpCode.INSTANCE_SWEEP_CLIENT, MessageFormat.format("您还没有通关{0}，无法进行副本扫荡", instance.instanceName));
				   return;
			   }
			   if(sweepTimes>instance.dayTimes){
				   ErrorHandler.sendErrorMessage(session, serial, OpCode.INSTANCE_SWEEP_CLIENT, "今天该副本扫荡次数已用完");
				   return;
			   }
			   if(player.sweepList.containsKey(instanceId)){
				   ErrorHandler.sendErrorMessage(session, serial, OpCode.INSTANCE_SWEEP_CLIENT, "您正在对这个副本进行扫荡，不能重复扫荡");
				   return;
			   }
			   LogUtil.logSweepStart(player, instance.instanceName, sweepTimes);
			   if(sweepTimes <=1){
				   addToClientSession();
			   }else{
				   if(player.freeSweep.contains(instanceId)){
//					   player.message(-1, "由于上次因故未完成扫荡，所以本次扫荡不重复扣费", -1, -1);
					   addToClientSession();
				   }else{
					   ShopService service = Server.server.getServiceRegistry().getShopService();
					   try{
							int shopId = service.getShopByItemId(NoItemShopBuy.YIYUANBAO).id;
							int count = InstanceSweepService.PAY_OPENSWEEP[sweepTimes];
							NoItemShopBuy ibuy = new NoItemShopBuy(player,serial,shopId,NoItemShopBuy.YIYUANBAO,count,this,null);
							service.buy(player, ibuy);
					   }catch(Exception e){
							ErrorHandler.sendErrorMessage(session, serial, OpCode.INSTANCE_SWEEP_CLIENT, peony.Messages.STRING_00911);
							return;
					   }
				   }
			   }
			}
		}
	}

	public void process(Object[] o) {
       addToClientSession();
	}

	public void procssFail(Object[] o) {
		error(peony.Messages.STRING_00911);
		addToClientSession();
	}

}
