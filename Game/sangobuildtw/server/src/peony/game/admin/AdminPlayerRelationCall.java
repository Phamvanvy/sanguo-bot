package peony.game.admin;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.db.PlayerRelationDAO;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.friend.PlayerRelation;
import peony.service.friend.RelationList;
import peony.service.friend.RelationService;

/**
 * 获取玩家关系。
 * 请求：Admin客户端 》 世界服务器
 * 参数：
 *  1： int 序列号
 *  2： int 玩家Id
 *  
 * 回复：Admin客户端 《 世界服务器
 *  1： int 序列号
 *  2： int 玩家Id
 *  3：short  好友数量
 *  4：结构：好友信息（重复好友数量次）
 *    4.1 int 玩家id
 *    4.2 String 玩家名称
 *    4.3 int 关系度 
 *  5：short  黑名单数量
 *  6：结构：黑名单信息（重复黑名单数量次） 
 *    6.1 int 玩家id
 *    6.2 String 玩家名称
 *    6.3 int 关系度 
 *  7：short  仇人数量
 *  8：结构：仇人信息（重复仇人数量次） 
 *    8.1 int 玩家id
 *    8.2 String 玩家名称
 *    8.3 int 关系度 
 *  9：short  临时关系数量
 *  10：结构：临时关系信息（重复临时关系数量次） 
 *    10.1 int 玩家id
 *    10.2 String 玩家名称
 *    10.3 int 关系度 
 * 
 */
public class AdminPlayerRelationCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int playerId;
	PlayerRelation relation;
	
	public AdminPlayerRelationCall(ClientSession session,Packet pt){
		super(session);
		this.serial = pt.getInt();
		this.playerId = pt.getInt();
	}
	
	public void callFinish() throws Exception {
		if (relation != null) {
			 Packet pt = new Packet(OpCode.ADMIN_GM_PLAYER_RELATION);
			 pt.putInt(serial);
			 pt.putInt(playerId);
			 addRelationList(pt, relation.friends);
			 addRelationList(pt, relation.blackList);
			 addRelationList(pt, relation.enemies);
			 addRelationList(pt, relation.tempList);
			 session.send(pt);
		} else {
			ErrorHandler.sendAdminErrorMessage(session, serial, OpCode.ADMIN_GM_PLAYER_RELATION, MessageFormat.format("沒有找到指定玩家的社會關系：{0}", playerId));
		}
	}
	private void addRelationList(Packet pt, RelationList lst) {
		if (lst == null) {
			pt.putShort(0);
		} else {
			int n = lst.getCount();
			pt.putShort(n);
			for (int i = 0; i < n; i++) {
				Actor actor = lst.getPlayerAt(i);
				pt.putInt(actor.id);
				pt.putString(actor.name);
				pt.putInt(lst.getDegreeOfPlayer(actor.id));
			}
		}
	}
	public void run() {
		RelationService rs = Server.server.getServiceRegistry().getRelationService();
		relation = rs.get(playerId);
		if (relation == null) {
			PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
			relation = dao.findPlayerRelation(playerId);
		}
		addToClientSession();
	}

}
