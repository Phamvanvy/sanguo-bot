package peony.service.friend;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.player.ActorCacheService;
import peony.service.tong.Tong;
import peony.service.tong.TongService;

/**
 * 请求添加好友/黑名单/仇人。
 * id		int			玩家ID，-1表示不使用此参数
 * name		String		玩家名称，空串表示不使用此参数
 * type		byte		类型：0 - 好友、1 - 黑名单、2 - 仇人
 * 
 * 添加好友/黑名单/仇人成功。
 * id		int			玩家ID
 * name		String		玩家名称
 * type		byte		类型：0 - 好友、1 - 黑名单、2 - 仇人
 * onine	boolean		是否在线，只有在线玩家的下面4个参数才有效
 * level	short		级别
 * sex		byte		性别
 * clazz	byte		职业
 * tong		String		军团
 */
public class AddFriendCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(AddFriendCall.class);
	protected int serial;
	protected int id;
	protected String name;
	protected byte type;
	protected Actor actor;

	public AddFriendCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.id = packet.getInt();
		this.name = packet.getString();
		this.type = packet.getByte();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 在主线程中进行关系列表修改操作
			RelationService rs = Server.server.getServiceRegistry().getRelationService();
			Player player = (Player)session.getClient();
			PlayerRelation relation = rs.get(player.id);
			if (relation == null) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ADD_FRIEND_CLIENT, "會話錯誤,請退出重新登錄");
				return;
			}
			if( player.getName().equalsIgnoreCase(name)){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ADD_FRIEND_CLIENT, "不能添加自己");
				return;
			}
			boolean addOK = false;
			int degree = 0;
			switch (type) {
			case 0:
                // 不允许重复添加
                if (relation.friends.exists(actor.id)) {
                    ErrorHandler.sendErrorMessage(session, serial,
                            OpCode.ADD_FRIEND_CLIENT, "已經在好友列表里了");
                    return;
                }
                if(actor.faction!=player.faction){
                    ErrorHandler.sendErrorMessage(session, serial,
                            OpCode.ADD_FRIEND_CLIENT, "不能添加敵對國家的人為好友");
                    return;               	
                }
				addOK = relation.addFriend(actor);
				if (addOK) {
					degree = relation.friends.getDegreeOfPlayer(actor.id);
				}
				break;
			case 1:
                // 不允许重复添加
                if (relation.blackList.exists(actor.id)) {
                    ErrorHandler.sendErrorMessage(session, serial,
                            OpCode.ADD_FRIEND_CLIENT, "已經在黑名單里了");
                    return;
                }
				addOK = relation.addBlackList(actor);
				if (addOK) {
					degree = relation.blackList.getDegreeOfPlayer(actor.id);
				}
				break;
			case 2:
                // 不允许重复添加
                if (relation.enemies.exists(actor.id)) {
                    ErrorHandler.sendErrorMessage(session, serial,
                            OpCode.ADD_FRIEND_CLIENT, "已經在仇人列表里了");
                    return;
                }
				addOK = relation.addEnemy(actor);
				if (addOK) {
					degree = relation.enemies.getDegreeOfPlayer(actor.id);
				}
				break;
			}
			if (!addOK) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ADD_FRIEND_CLIENT, "已到達數量上限");
				return;
			}
			
			// 添加成功，下发确认包
			Packet pt = new Packet(OpCode.ADD_FRIEND_SERVER);
			pt.putInt(serial);
			pt.putInt(actor.id);
			pt.putString(actor.name);
			pt.put(type);
			pt.putInt(degree);
			pt.put(actor.online ? 1 : 0);
			pt.putShort(actor.level);
			pt.put(actor.sex);
			pt.put(actor.clazz);
			TongService ts = Server.server.getServiceRegistry().getTongService();
			Tong tong = ts.getPlayerTong(actor.id);
			pt.putString(tong == null ? "" : tong.name);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ADD_FRIEND_CLIENT, errorMessage);
		}
	}

	public void run() {
//		Transaction tx = HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
//		try {
			// 查找目标角色
			ActorCacheService service = Server.server.getServiceRegistry()
					.getActorCacheService();
			if (id != -1) {
				actor = service.find(id);
			} else {
				actor = service.find(name);
			}
//			tx.commit();
//		} catch (Exception ex) {
//			tx.rollback();
//			log.error(ex, ex);
//		}
		if (actor == null) {
			error(null, "目標不存在");
		}
		addToClientSession();
	}
}
