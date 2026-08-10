package peony.service.tong;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 请求移除军团成员。
 * serial	int
 * tid		int			目标角色ID
 *
 * 移除军团成员成功。
 * serial	int
 * tid		int			目标角色ID
 */
public class TongKickCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(TongKickCall.class);
	protected int serial;
	protected int targetID;
	protected Player player;
	protected TongService tongService;

	public TongKickCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.targetID = packet.getInt();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 操作成功，下发确认包
			Packet pt = new Packet(OpCode.TONG_KICK_SERVER);
			pt.putInt(serial);
			pt.putInt(targetID);
//			Tong tong = tongService.getPlayerTong(player.id,true);
			TongMember tm1 = tongService.getPlayerInfo(player.id);
			Tong tong = tongService.getTong(tm1.tongID);
			int onlines = tongService.getOnlineMembers(tong).size();
			int totalCount = tongService.getMemberCount(tong, TongService.NORMAL, TongService.CHAIRMAN);
			pt.putString(MessageFormat.format("{0}/{1}", onlines,totalCount));
			session.send(pt);
			
			// 如果对方在线，向他发一个TONG_MEMBER_CHANGE_SERVER通知
			Player targetPlayer =	ObjectAccessor.getPlayer(targetID);
			if (targetPlayer != null && targetPlayer.session != null) {
				pt = new Packet(OpCode.TONG_MEMBER_CHANGE_SERVER);
				pt.putInt(targetID);
				pt.putInt(-1);
				pt.putString("");
				pt.putInt(0);
				pt.putString("");
				pt.put(0);
				targetPlayer.session.send(pt);
			}			
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.TONG_KICK_CLIENT, errorMessage);
		}
	}

	public void run() {
		// 请求移除成员
		try {
			tongService.removeMember(player.id, targetID);
		} catch (TongException e) {
			error(null, e.getMessage());
		}
		addToClientSession();
	}
}
