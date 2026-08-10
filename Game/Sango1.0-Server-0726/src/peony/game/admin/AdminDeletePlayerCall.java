package peony.game.admin;

import java.text.MessageFormat;
import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.nation.Nation;
import peony.game.nation.NationService;
import peony.game.nation.Officer;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.player.PlayerService;
import peony.service.tong.Tong;
import peony.service.tong.TongService;

public class AdminDeletePlayerCall extends ClientSessionAsyncCall {

	private int playerId;
	private int serial;
	
	public AdminDeletePlayerCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.playerId = packet.getInt();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.ADMIN_DELETEPLAYER_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_DELETEPLAYER_CLIENT, errorMessage);
		}
	}

	public void run() {
		PlayerService playerSerivce = Server.server.getServiceRegistry().getPlayerService();
		Player player = playerSerivce.loadPlayerSilent(playerId);
		if (player == null) {
			error(null, "没有找到指定角色");
		} else {
			if (player.exist != 1) {
				error(null, "角色已经被删除");
			} else {
				//如果是国王不能删除账号
				NationService nationService = Server.server.getServiceRegistry().getNationService();
				if(nationService.isKing(player)){
					error(null, "此人是国公,不可删除账号");
					addToClientSession();
					return;
				}
				Nation nation = nationService.getNationByFaction(player.faction);
				for(Officer officer : nation.getOfficers()){
					if(officer!=null && officer.actor.id==player.id){
						error(null, MessageFormat.format("此人是{0},不可删除账号", officer.getName()));
						addToClientSession();
						return;
					}
				}
				TongService tongService = Server.server.getServiceRegistry().getTongService();
				Tong tong = tongService.getPlayerTong(player.id);
				if (tong == null) {
					if (player.systemState != Player.SYSTEMSTATE_LOAD) {
						error(null, "角色状态错误，请稍后再进行尝试");
						addToClientSession();
						return;
					}
					Server.server.getServiceRegistry().getRelationService().get(player.id).removeMarriageRelation();
					player.removeFromMap();
					ObjectAccessor.removeGameObject(player);
					player.exist = 0;
					Actor a = Server.server.getServiceRegistry().getActorCacheService().find(player.id);
					a.exist = 0;
					Server.server.getServiceRegistry().getDbService().playerDAO
							.updateEntity(player);
					Server.server.getServiceRegistry().getStatService().deleteActor(player.id);
				} else {
					error(null,"角色有所属军团，不能删除");
				}
			}
		}
		addToClientSession();
	}

}
