package peony.db;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.Identity;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.association.Association;
import peony.game.association.AssociationService;
import peony.game.nation.Nation;
import peony.game.nation.NationService;
import peony.game.nation.Officer;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.player.PlayerService;
import peony.service.tong.Tong;
import peony.service.tong.TongService;

public class DeletePlayerCall extends ClientSessionAsyncCall {
	
	public static int DELTE_LIMIT_LEVEL = 50; //允许删号的最高级别
	
	protected int actorId;
	
	public DeletePlayerCall(ClientSession session,int actorId){
		super(session);
		this.actorId = actorId;
	}
	
	public void callFinish() throws Exception {
		if (success) {
			Packet pt = new Packet(OpCode.ACTOR_DELETE_SERVER);
			pt.putInt(actorId);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, -1,
					OpCode.ACTOR_DELETE_CLIENT, errorMessage);
		}

	}

	public void run() {
		Identity identity = session.getIdentity();
		if (identity == null) {
			error(null, "没有登录");
		} else {
			PlayerService playerSerivce = Server.server.getServiceRegistry()
					.getPlayerService();
			Player player = playerSerivce.loadPlayer(identity.getId(), actorId);
			if (player == null) {
				error(null, "没有找到指定角色");
			} else {
				if (player.exist != 1) {
					error(null, "角色已经被删除");
				} else {
					if(player.level>=DELTE_LIMIT_LEVEL){
						error(null, MessageFormat.format("{0}级以上的玩家不允许删除角色", DELTE_LIMIT_LEVEL));
						addToClientSession();
						return;
					}
					//如果是国王不能删除账号
					NationService nationService = Server.server.getServiceRegistry().getNationService();
					if(nationService.isKing(player)){
						error(null, "您是国公,不可删除账号");
						addToClientSession();
						return;
					}
					Nation nation = nationService.getNationByFaction(player.faction);
					for(Officer officer : nation.getOfficers()){
						if(officer!=null && officer.actor.id==player.id){
							error(null, MessageFormat.format("您是{0},不可删除账号", officer.getName()));
							addToClientSession();
							return;
						}
					}
					AssociationService associationService = Server.server.getServiceRegistry().getAssociationService();
					Association association = associationService.getAssociationByPlayerId(player.id);
					if(association!=null){
						error(null, "血盟成员不能删除角色");
						addToClientSession();
						return;
					}
					TongService tongService = Server.server
							.getServiceRegistry().getTongService();
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
						Actor a = Server.server.getServiceRegistry()
								.getActorCacheService().find(player.id);
						a.exist = 0;
						Server.server.getServiceRegistry().getDbService().playerDAO
								.updateEntity(player);
						Server.server.getServiceRegistry().getStatService().deleteActor(player.id);
					} else {
						error(null,"角色有所属军团，不能删除");
					}
				}
			}
		}
		addToClientSession();
	}

}
