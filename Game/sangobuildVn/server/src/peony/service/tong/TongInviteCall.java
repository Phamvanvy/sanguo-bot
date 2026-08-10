package peony.service.tong;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.friend.PlayerRelation;
import peony.service.player.ActorCacheService;

/**
 * 邀请加入军团。
 * serial	int
 * tid		int			目标角色ID，-1表示无效
 * tname	int			目标角色名称
 *
 * 邀请发送成功。
 * serial	int
 * 
 * 向被邀请加入军团的角色发送的通知。
 * sid		int			邀请人ID
 * sname	String		邀请人名称
 * tid		int			邀请人军团ID
 * tname	String		邀请人军团名称
 * invid	int			请柬ID
 */
public class TongInviteCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(TongInviteCall.class);
	protected int serial;
	protected int targetID;
	protected String targetName;
	protected Player player;
	protected TongService tongService;
	protected Tong tong;
	protected int inviteID;

	public TongInviteCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.targetID = packet.getInt();
		this.targetName = packet.getString();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 创建成功，下发确认包
			Packet pt = new Packet(OpCode.TONG_INVITE_SERVER);
			pt.putInt(serial);
			session.send(pt);
			
			// 向对方发送邀请
			Player targetPlayer = ObjectAccessor.getPlayer(targetID);
			if (targetPlayer != null && targetPlayer.session != null) {

				pt = new Packet(OpCode.TONG_INVITATION_SERVER);
				pt.putInt(player.id);
				pt.putString(player.name);
				pt.putInt(tong.id);
				pt.putString(tong.name);
				pt.putInt(inviteID);
				targetPlayer.session.send(pt);
			}
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.TONG_INVITE_CLIENT, errorMessage);
		}
	}

	public void run() {
		// 查找目标
		ActorCacheService acs = Server.server.getServiceRegistry().getActorCacheService();
		Actor actor;
		if (targetID != -1) {
			actor = acs.find(targetID);
		} else {
			actor = acs.find(targetName);
		}
		if (actor == null) {
			error(null, "Mục tiêu không tồn tại");
			addToClientSession();
			return;
		}
		if(actor.id==player.id){
			error(null, "Ko thể thêm riêng của bạn");
			addToClientSession();
			return;			
		}
		if(actor.faction!=player.faction){
			error(null, "不能添加敌对国家的玩家");
			addToClientSession();
			return;
		}
		if(actor.level<5){
			error(null, "目标等级小于5级，不能加入军团");
			addToClientSession();
			return;
		}
		PlayerRelation rel = Server.server.getServiceRegistry().getRelationService().get(actor.id);
		if(rel!=null&&rel.blackList.exists(player.id)){
			error(null, "目标拒绝你的入团邀请");
			addToClientSession();
			return;
		}
		targetID = actor.id;
		targetName = actor.name;
		
		// 创建申请
		try {
			inviteID = tongService.createInvitation(player.id, actor.name);
			tong = tongService.getPlayerTong(player.id);
		} catch (TongException e) {
			error(null, e.getMessage());
		}
		addToClientSession();
	}
}
