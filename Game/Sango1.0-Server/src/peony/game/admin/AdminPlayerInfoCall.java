package peony.game.admin;



import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.VIP.VipPrivilegeService;
import peony.service.account.AccountProperty;

public class AdminPlayerInfoCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int playerId;
	protected String playerName;
	protected Player p;
	
	public AdminPlayerInfoCall(ClientSession session,Packet pt){
		super(session);
		this.serial = pt.getInt();
		this.playerId = pt.getInt();
		this.playerName = pt.getString();
	}
	
	public void callFinish() throws Exception {
		if (p != null) {
			byte[] bytes = p.toClientBytesAdmin();
			byte[] bytes1 = p.bag.toClientBytes();
			byte[] bytes2 = p.skills.toClientBytes(p);
			byte[] bytes3 = p.horseBag.toClientBytesAdmin();
			byte[] bytes4 = p.depot == null ? new byte[0] : p.depot.toClientBytes(); 
			byte[] bytes5 = p.attendantBag.toClientBytes(p);
			Packet pt = new Packet(OpCode.ADMIN_PLAYER_INFO_SERVER);
			pt.putInt(serial);
			pt.put(bytes);
			pt.put(bytes1);
			pt.put(bytes2);
			pt.put(bytes3);
			pt.putInt(p.getWeekCredit());
			pt.put(bytes4);
			pt.put(bytes5);
			session.send(pt);
		} else {
			ErrorHandler.sendAdminErrorMessage(session, serial,
					OpCode.ADMIN_PLAYER_INFO_CLIENT, peony.Messages.STRING_00529);
		}
	}

	public void run() {
		if(playerName.length()!=0){
			Actor actor = Server.server.getServiceRegistry().getActorCacheService().find(playerName);
			if(actor==null){
				ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_PLAYER_INFO_CLIENT, peony.Messages.STRING_00529);
				return;
			}
			playerId = actor.id;
		}
		p = Server.server.getServiceRegistry().getPlayerService().loadPlayerSilent(playerId);
		//导入VIP信息
		VipPrivilegeService vipService = Server.server.getServiceRegistry().getVipPrivilegeService();
		if(p!=null){
			AccountProperty ap = vipService.getAccountProperty(p.accountId);
			if(ap==null){
				synchronized (Server.server.getServiceRegistry().getDbService().accountPropertyDao) {
					ap = vipService.getAccountPropertyFromDB(p.accountId);
				}
				if(ap!=null){
					vipService.addAccountProperty(p.accountId, ap);
				}
			}
		}
		addToClientSession();
	}

}
