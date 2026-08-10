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
 * 请求加入军团。
 * serial	int
 * invid	int			请柬ID

 * 加入军团成功。
 * serial	int
 * tid		int			军团ID
 * tname	String		军团名称
 * duty		int			军团职务
 */
public class JoinTongCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(JoinTongCall.class);
	protected int serial;
	protected int inviteID;
	protected Player player;
	protected TongService tongService;
	protected Tong tong;
	protected TongMember self;

	public JoinTongCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.inviteID = packet.getInt();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 创建成功，下发确认包
			Packet pt = new Packet(OpCode.TONG_JOIN_SERVER);
			pt.putInt(serial);
			pt.putInt(tong.id);
			pt.putString(tong.name);
			pt.putInt(self.duty);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.TONG_JOIN_CLIENT, errorMessage);
		}
	}

	public void run() {
		// 请求加入
		try {
			tongService.join(inviteID, player.id);
			self = tongService.getPlayerInfo(player.id);
			tong = tongService.getTong(self.tongID);
		} catch (TongException e) {
			error(null, e.getMessage());
		}
		addToClientSession();
	}
}
