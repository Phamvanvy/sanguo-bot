package peony.service.friend;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.player.ActorCacheService;

/**
 * 锁定联系人
 * @author pmeng
 */

public class ChangeFriendLockCall extends ClientSessionAsyncCall{
	/**
	 * id		int			玩家ID
	 * type		byte		类型：0 - 好友、1 - 黑名单、2 - 仇人
	 * state    byte        类型：0 - 解除锁定   1 - 锁定
	 */
	protected int serial;
	protected int id;
	protected byte type;
	protected byte state;
	protected Actor actor;

	public ChangeFriendLockCall(ClientSession session,Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.id = packet.getInt();
		this.type = packet.getByte();
		this.state = packet.getByte();
	}

	public void callFinish() throws Exception {
		if(success){
			RelationService rs = Server.server.getServiceRegistry().getRelationService();
			Player player = (Player)session.getClient();
			PlayerRelation relation = rs.get(player.id);
			switch(type){
			case 0:
				break;
			case 1:
				break;
			case 2:
				if(state == 0){
					relation.enemies.unLockPlayer(id);
				}else if(state == 1){
					relation.enemies.lockPlayer(id);
				}
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.DEL_FRIEND_CLIENT, peony.Messages.STRING_01843);
				break;
			}
			Packet pt = new Packet(OpCode.CHANGE_FRIEND_LOCKSTATE_SERVER);
			pt.putInt(serial);
			pt.putInt(id);
			pt.put(relation.enemies.isLocks.get(id).byteValue());
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.DEL_FRIEND_CLIENT, errorMessage);
		}
	}

	public void run() {
		addToClientSession();
	}

}
