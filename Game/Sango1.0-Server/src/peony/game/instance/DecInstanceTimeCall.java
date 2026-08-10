package peony.game.instance;

import java.util.Iterator;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.salary.SalaryService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopService;

/**
 * 缩短副本扫荡时间
 * @author mfou
 *
 */
public class DecInstanceTimeCall extends ClientSessionAsyncCall implements NoItemShopBuyI{
	
	protected int serial;
	protected int instanceId;
    Player player = null;
    InstanceSweep is = null;
	
	InstanceSweepService service = Server.server.getServiceRegistry().getInstanceSweepService();

	public DecInstanceTimeCall(Packet packet,ClientSession session) {
		super(session);
		serial = packet.getInt();
		instanceId = packet.getInt();
		player = (Player)session.getClient();
		is = service.getSweepInstance(instanceId);
	}

	public void process(Object[] o) {
		int sweepTimes = player.pool.getInt(InstanceSweepService.getPropertyOfDayTimes(instanceId), 1);
		sweepTimes++;
		player.pool.setInt(InstanceSweepService.getPropertyOfDayTimes(instanceId), sweepTimes);
		LogUtil.logSweepEnd(player, is.instanceName, sweepTimes-1);
		Packet pt = new Packet(OpCode.INSTANCE_SWEEP_SERVER);
		pt.putInt(-1);
		pt.putInt(instanceId);
		pt.put(sweepTimes>is.dayTimes ? InstanceSweepService.TYPE_SWEEPED : InstanceSweepService.TYPE_UNSWEEP);
		pt.putInt(sweepTimes-1);
		ShopService shopService = Server.server.getServiceRegistry().getShopService();
		float itemPrice = InstanceSweepService.PAY_OPENSWEEP[sweepTimes]*shopService.getItemPrice(NoItemShopBuy.YIYUANBAO)/36f;
		pt.putString(String.valueOf(itemPrice));
		pt.putInt(is.time);
		pt.put(0);
		player.send(pt);
		LogUtil.logSweepPay(player, is.instanceName, sweepTimes-1,"SUCCESS");
		addToClientSession();
	}

	public void procssFail(Object[] o) {
	    error(peony.Messages.STRING_00911);
	    int sweepTimes = player.pool.getInt(InstanceSweepService.getPropertyOfDayTimes(instanceId), 1);
	    LogUtil.logSweepPay(player, is.instanceName, sweepTimes-1,"FAILD");
	    addToClientSession();
	}

	public void callFinish() throws Exception {
		if(success){
		    Iterator<Integer> it = player.sweepList.keySet().iterator();
			while(it.hasNext()){
				int key = it.next();
				if(key == instanceId){
					it.remove();
					if(player.freeSweep.contains(key)){
                        int index = player.freeSweep.indexOf(key);
                        player.freeSweep.remove(index);
                    }
				}
			}
			service.getReward(player,instanceId,is.instanceName);
			Packet pt = new Packet(OpCode.DEC_SWEEPTIME_SERVER);
			pt.putInt(serial);
			player.send(pt);
			//处理扫荡副本工资
			SalaryService salaryService = Server.server.getServiceRegistry().getSalaryService();
			salaryService.processSweepSalary(player);
		}
	}

	public void run() {
		if(player!=null){
			int sweepTimes = player.pool.getInt(InstanceSweepService.getPropertyOfDayTimes(instanceId), 0);
			if(sweepTimes-1>is.dayTimes){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DEC_SWEEPTIME_CLIENT, "您今天扫荡次数已用完，暂时不能使用加速功能");
				return;
			}
			if(!player.sweepList.keySet().contains(instanceId)){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DEC_SWEEPTIME_CLIENT, "您当前还没对该副本进行扫荡");
				return;
			}
			InstanceSweep instance = player.sweepList.get(instanceId);
			if(instance == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.DEC_SWEEPTIME_CLIENT, "找不到该副本");
				return;
			}
			ShopService service = Server.server.getServiceRegistry().getShopService();
			try{
				int shopId = service.getShopByItemId(NoItemShopBuy.WUYUANBAO).id;
				NoItemShopBuy ibuy = new NoItemShopBuy(player,serial,shopId,NoItemShopBuy.WUYUANBAO,3,this,null);
				service.buy(player, ibuy);
			}catch(Exception e){
				ErrorHandler.sendErrorMessage(session, serial, -1, peony.Messages.STRING_00911);
				return;
			}
		}
	}

}
