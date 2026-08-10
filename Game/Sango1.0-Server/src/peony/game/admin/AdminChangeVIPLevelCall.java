package peony.game.admin;

import peony.common.ClientSessionAsyncCall;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.AccountProperty;

public class AdminChangeVIPLevelCall extends ClientSessionAsyncCall{

	int serial;
	int accountId;
	int playerId;
	int vipLevel;
	Player player;
	public AdminChangeVIPLevelCall(ClientSession session,Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.accountId = packet.getInt();
		this.playerId = packet.getInt();
		this.vipLevel = packet.getInt();	
	}

	public void callFinish() throws Exception {
		if(success){
		   Packet pt = new Packet(OpCode.ADMIN_CHANGE_VIPLEVEL_SERVER);
		   pt.putInt(serial);
		   session.send(pt);
		}
	}

	public void run() {
		VipPrivilegeService service = Server.server.getServiceRegistry().getVipPrivilegeService();
		AccountProperty ap = service.getAccountProperty(accountId);
		if(ap==null){
			synchronized (Server.server.getServiceRegistry().getDbService().accountPropertyDao) {
				ap = service.getAccountPropertyFromDB(accountId);
			}
		}
		Player player = ObjectAccessor.getPlayer(playerId);
		if(ap!=null){
			int iBuyValue = ap.pool.getInt(VipPrivilegeService.PROPERTY_VIP_IBUYVALUE, 0);
			int chargeValue = Server.server.getServiceRegistry().getDbService().chargeDao.getTotalChargesAfter(player.accountId,VipPrivilegeService.START_TIME)*10;
		    if(Server.server.revision.equals(Server.REVISION_TYPE_TW)){
		    	chargeValue /= 10;
		    }
		    chargeValue += iBuyValue;
		    int realVipLevel = service.getVIPLevel(chargeValue);
		    int needChargeValue = VipPrivilegeService.getVIPValue(vipLevel);
		    if(realVipLevel!=vipLevel){
		    	int newIBuyValue = iBuyValue;
			    if(realVipLevel > vipLevel){
			    	int redundantValue = chargeValue - needChargeValue;
			    	newIBuyValue = iBuyValue-redundantValue;
			    }else {
			    	int missingValue = needChargeValue - chargeValue;
			    	newIBuyValue = iBuyValue+missingValue;
			    	if(player!=null){
			    	    service.processReward(player, realVipLevel,vipLevel);
			    	    if(realVipLevel<1 && vipLevel>=1){
			    	    	service.processMonthPay(player, true);
			    	    }
			    	    if(player!=null){
			    	       player.pool.setInt(VipPrivilegeService.PROPERTY_VIP_IBUYVALUE, vipLevel);
			    	    }
			    	}
			    }
			    LogUtil.logVipLevelUp(player, realVipLevel, vipLevel, chargeValue, needChargeValue, "ADMINCHANGEVIP");
			    ap.pool.setInt(VipPrivilegeService.PROPERTY_VIP_IBUYVALUE, newIBuyValue);
			    if(player!=null){
			    	player.vipLevel = vipLevel;
			    	player.addIntPropertyChangedItem(ChangedItem.VIP_LEVEL,vipLevel,false,true);
			    	ap.pool.setInt(VipPrivilegeService.PROPERTY_VIP_CHARGELEVEL, vipLevel);
			    	service.addAccountProperty(accountId, ap);
			    }
			    Server.server.getServiceRegistry().getDbService().accountPropertyDao.updateEntity(ap);
		    }
		}
		addToClientSession();
	}
}
