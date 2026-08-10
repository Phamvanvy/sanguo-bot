package peony.service.friend;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.player.ActorCacheService;
import peony.service.tong.Tong;
import peony.service.tong.TongMember;
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
	protected Tong tong = null;
	

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
						OpCode.ADD_FRIEND_CLIENT, peony.Messages.STRING_00682);
				return;
			}
			if( player.getName().equalsIgnoreCase(name)){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ADD_FRIEND_CLIENT, peony.Messages.STRING_00683);
				return;
			}
			boolean addOK = false;
			int degree = 0;
			Actor tempActor = null;
			switch (type) {
			case 0:
                // 不允许重复添加
                if (relation.friends.exists(actor.id)) {
                    ErrorHandler.sendErrorMessage(session, serial,
                            OpCode.ADD_FRIEND_CLIENT, peony.Messages.STRING_00684);
                    return;
                }
                if(actor.faction!=player.faction){
                    ErrorHandler.sendErrorMessage(session, serial,
                            OpCode.ADD_FRIEND_CLIENT, peony.Messages.STRING_00685);
                    return;               	
                }
				addOK = relation.addFriend(actor);
				if (addOK) {
					degree = relation.friends.getDegreeOfPlayer(actor.id);
					LogUtil.addFriendSuccess(player.name, actor.name);
				}else{
					 ErrorHandler.sendErrorMessage(session, serial,
	                            OpCode.ADD_FRIEND_CLIENT, peony.Messages.STRING_00686);
					 return;
				}
				break;
			case 1:
                // 不允许重复添加
                if (relation.blackList.exists(actor.id)) {
                    ErrorHandler.sendErrorMessage(session, serial,
                            OpCode.ADD_FRIEND_CLIENT, peony.Messages.STRING_00687);
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
                            OpCode.ADD_FRIEND_CLIENT, peony.Messages.STRING_00688);
                    return;
                }
				if(relation.enemies.getCount() >= PlayerRelation.MAX_ENEMIES){//需要删除
					tempActor = relation.enemies.getPlayerAt(PlayerRelation.MAX_ENEMIES - 1);
				}
				if(!relation.addEnemy(actor,player)){//列表全部被锁定  没有添加成功
					return;
				}
				degree = relation.enemies.getDegreeOfPlayer(actor.id);
				//添加仇人时改变状态
				Player targetPlayer = ObjectAccessor.getPlayer(actor.id);
				if(targetPlayer!=null)
					   rs.addEnemy(player, targetPlayer);
				
				break;
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
			pt.putString(tong == null ? "" : tong.name);
			pt.put(actor.faction);
			pt.put(0);
			if(tempActor != null){//需要删除
				pt.putInt(tempActor.id);
			}else{
				pt.putInt(-1);//不需要删除
			}
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
			error(null, peony.Messages.STRING_00270);
			addToClientSession();
			return;
		}
		TongService ts = Server.server.getServiceRegistry().getTongService();
		tong = ts.getPlayerTong(actor.id,true);
		addToClientSession();
	}
}
