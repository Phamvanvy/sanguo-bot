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
 * 请求修改军团公告。
 * serial	int
 * slogan	String		新军团公告
 *
 * 修改军团公告成功。
 * serial	int
 * slogan	String		新军团公告
 */
public class SetTongSloganCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(SetTongSloganCall.class);
	protected int serial;
	protected String slogan;
	protected Player player;
	protected TongService tongService;
	protected Tong tong;

	public SetTongSloganCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.slogan = packet.getString();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 创建成功，下发确认包
			Packet pt = new Packet(OpCode.TONG_SET_SLOGAN_SERVER);
			pt.putInt(serial);
			pt.putString(tong.slogan);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.TONG_SET_SLOGAN_CLIENT, errorMessage);
		}
	}

	public void run() {
		// 请求加入
		try {
			tongService.setSlogon(player.id, slogan);
			tong = tongService.getPlayerTong(player.id);
		} catch (TongException e) {
			error(null, e.getMessage());
		}
		addToClientSession();
	}
}
