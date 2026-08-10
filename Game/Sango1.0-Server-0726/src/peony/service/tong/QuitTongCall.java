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
 * 请求退出军团。
 * serial	int
 * 
 * 退出军团成功。
 * serial	int
 */
public class QuitTongCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(QuitTongCall.class);
	protected int serial;
	protected Player player;
	protected TongService tongService;

	public QuitTongCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 创建成功，下发确认包
			Packet pt = new Packet(OpCode.TONG_QUIT_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.TONG_QUIT_CLIENT, errorMessage);
		}
	}

	public void run() {
		// 请求退出
		try {
			tongService.exitTong(player.id);
		} catch (TongException e) {
			error(null, e.getMessage());
		}
		addToClientSession();
	}
}
