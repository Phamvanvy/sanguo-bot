package peony.service.friend;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 请求删除好友/黑名单/仇人。
 * id		int			玩家ID
 * type		byte		类型：0 - 好友、1 - 黑名单、2 - 仇人
 * 
 * 删除好友/黑名单/仇人成功。
 * id		int			玩家ID
 * type		byte		类型：0 - 好友、1 - 黑名单、2 - 仇人
 */
public class DelFriendCall extends ClientSessionAsyncCall {
	protected int serial;
	protected int id;
	protected byte type;

	public DelFriendCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.id = packet.getInt();
		this.type = packet.getByte();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 在主线程中执行关系列表修改操作
			RelationService rs = Server.server.getServiceRegistry().getRelationService();
			Player player = (Player)session.getClient();
			PlayerRelation relation = rs.get(player.id);
			if (relation == null) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.DEL_FRIEND_CLIENT, peony.Messages.STRING_00436);
				return;
			}
			switch (type) {
			case 0:
				relation.removeFriend(id);
				break;
			case 1:
				relation.removeBlackList(id);
				break;
			case 2:
				if(relation.enemies.isLockedOfPlayer(id)){//仇人已被锁定
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.DEL_FRIEND_CLIENT, peony.Messages.STRING_01313);
					return;
				}else{
					relation.removeEnemy(id);
					Player targetPlayer = ObjectAccessor.getPlayer(id);
					if(targetPlayer!=null)
					   rs.delEnemy(player, targetPlayer);
				}
				break;
			}
			
			// 删除成功，下发确认包
			Packet pt = new Packet(OpCode.DEL_FRIEND_SERVER);
			pt.putInt(serial);
			pt.putInt(id);
			pt.put(type);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.DEL_FRIEND_CLIENT, errorMessage);
		}
	}

	public void run() {
		addToClientSession();
	}
}
