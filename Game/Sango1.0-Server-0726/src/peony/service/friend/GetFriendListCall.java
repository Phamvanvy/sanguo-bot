package peony.service.friend;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.tong.Tong;
import peony.service.tong.TongService;

/**
 * 取关联玩家列表。
 * 
 * 返回关联玩家列表。
 * count	int			列表大小
 * 	循环N次
 * 		id		int			玩家ID
 * 		name	String		玩家名称
 * 		type	byte		类型
 * 		degree	int			好友度/仇人度/临时好友交互类型
 * 		onine	byte		是否在线，只有在线玩家的下面4个参数才有效
 * 		level	short		级别
 * 		sex		byte		性别
 * 		clazz	byte		职业
 * 		tong	String		军团
 * 		isLock  byte	    是否锁定  0：未锁定     1：锁定
 */
public class GetFriendListCall extends ClientSessionAsyncCall {
	protected int serial;
	protected TongService ts;
	protected int type;

	public GetFriendListCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.type = packet.get();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 在主线程中执行列表查询操作
			RelationService rs = Server.server.getServiceRegistry().getRelationService();
			Player player = (Player)session.getClient();
			PlayerRelation relation = rs.get(player.id);
			if (relation == null) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.GET_FRIENDLIST_CLIENT, "会话错误，请退出重新登录。");
				return;
			}
			
			// 组织返回包
			ts = Server.server.getServiceRegistry().getTongService();
			Packet pt = new Packet(OpCode.GET_FRIENDLIST_SERVER);
			pt.putInt(serial);
			if(type==0){
				relation.friends.refreshPlayers();
				int count = relation.friends.getCount();
				pt.putInt(count);
				for(int i=0;i<count;i++){
					putPlayer(pt,relation.friends,i,0);
					pt.put(0);
				}
				
			}
			else if(type==1){
				relation.blackList.refreshPlayers();
				int count = relation.blackList.getCount();
				pt.putInt(count);
				for(int i=0;i<count;i++){
					putPlayer(pt,relation.blackList,i,1);
					pt.put(0);
				}
			}
			else if(type==2){
				relation.enemies.refreshPlayers();
				int count = relation.enemies.getCount();
				pt.putInt(count);
				Actor actor = null;
				for(int i=0;i<count;i++){
					putPlayer(pt,relation.enemies,i,2);
					actor = relation.enemies.players.get(i);//写入是否被锁定
					byte a = relation.enemies.isLocks.get(actor.id).byteValue();
					pt.put(relation.enemies.isLocks.get(actor.id).byteValue());
				}
			}
			else if(type==3){
				relation.tempList.refreshPlayers();
				int count = relation.tempList.getCount();
				pt.putInt(count);
				for(int i=0;i<count;i++){
					putPlayer(pt,relation.tempList,i,3);
					pt.put(0);
				}
			}
//			int count1 = relation.friends.getCount();
//			int count2 = relation.blackList.getCount();
//			int count3 = relation.enemies.getCount();
//			int count4 = relation.tempList.getCount();
//			pt.putInt(count1 + count2 + count3 + count4);
//			for (int i = 0; i < count1; i++) {
//				putPlayer(pt, relation.friends, i, 0);
//			}
//			for (int i = 0; i < count2; i++) {
//				putPlayer(pt, relation.blackList, i, 1);
//			}
//			for (int i = 0; i < count3; i++) {
//				putPlayer(pt, relation.enemies, i, 2);
//			}
//			for (int i = 0; i < count4; i++) {
//				putPlayer(pt, relation.tempList, i, 3);
//			}
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.GET_FRIENDLIST_CLIENT, errorMessage);
		}
	}
	
	private void putPlayer(Packet pt, RelationList list, int index, int type) {
		Actor actor = list.getPlayerAt(index);
		int degree = list.getDegreeOfPlayer(actor.id);
		pt.putInt(actor.id);
		pt.putString(actor.name);
		pt.put(type);
		pt.putInt(degree);
		pt.put(actor.online ? 1 : 0);
		pt.putShort(actor.level);
		pt.put(actor.sex);
		pt.put(actor.clazz);
		Tong tong = ts.getPlayerTong(actor.id);
		pt.putString(tong == null ? "" : tong.name);
		pt.put(actor.faction);
	}

	public void run() {
		addToClientSession();
	}
}
