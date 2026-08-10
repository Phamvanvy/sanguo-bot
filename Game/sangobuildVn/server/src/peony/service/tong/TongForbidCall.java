package peony.service.tong;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 请求禁言/解除禁言某军团成员。
 * serial	int
 * tid		int			目标角色ID
 * 
 * 禁言/解除禁言成功。
 * serial	int
 * tid		int			目标角色ID
 * forbid	byte		目标的新禁言状态，1 - 禁言、0 - 未禁言
 */
public class TongForbidCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(TongForbidCall.class);
	protected int serial;
	protected int targetID;
	protected Player player;
	protected TongService tongService;
	protected TongMember target;

	public TongForbidCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.targetID = packet.getInt();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 操作成功，下发确认包
			Packet pt = new Packet(OpCode.TONG_FORBID_SERVER);
			pt.putInt(serial);
			pt.putInt(targetID);
			pt.put(target.forbid ? 1 : 0);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.TONG_FORBID_CLIENT, errorMessage);
		}
	}

	public void run() {
		// 请求禁言/解除禁言
		try {
			tongService.forbid(player.id, targetID);
			target = tongService.getPlayerInfo(targetID);
		} catch (TongException e) {
			error(null, e.getMessage());
		}
		addToClientSession();
	}
}
