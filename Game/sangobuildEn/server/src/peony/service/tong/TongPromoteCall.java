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
 * 请求提升/降职（包括转让都督职务）。
 * serial	int
 * tid		int			目标角色ID
 * op		byte		0 - 升职、1 - 降职
 * 
 * 提升/降职成功。
 * serial	int
 * duty		int			请求者的新职务
 * tid		int			目标角色ID
 * tduty	int			目标角色的新职务
 */
public class TongPromoteCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(TongPromoteCall.class);
	protected int serial;
	protected int targetID;
	protected byte op;
	protected Player player;
	protected TongService tongService;
	protected Tong tong;
	protected TongMember self, target;

	public TongPromoteCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.targetID = packet.getInt();
		this.op = packet.getByte();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 操作成功，下发确认包
			Packet pt = new Packet(OpCode.TONG_PROMOTE_SERVER);
			pt.putInt(serial);
			pt.putInt(self.duty);
			pt.putInt(target.id);
			pt.putInt(target.duty);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.TONG_PROMOTE_CLIENT, errorMessage);
		}
	}

	public void run() {
		// 请求提升/降职
		try {
			if (op == 0) {
				tongService.promote(player.id, targetID);
			} else {
				tongService.demote(player.id, targetID);
			}
			tong = tongService.getPlayerTong(player.id);
			self = tongService.getPlayerInfo(player.id);
			target = tongService.getPlayerInfo(targetID);
		} catch (TongException e) {
			error(null, e.getMessage());
		}
		addToClientSession();
	}
}
