package peony.service.tong;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.util.StringUtil;

/**
 * 请求创建军团。
 * serial	int
 * name		String		军团名称
 * 
 * 创建军团成功。
 * serial	int
 * id		int			军团ID
 * name		int			军团名称
 * duty		int			军团职务
 */
public class CreateTongCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(CreateTongCall.class);
	protected int serial;
	protected Player player;
	protected String name;
	protected Tong tong;
	protected TongService tongService;

	public CreateTongCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.name = packet.getString();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
		if (success) {
			// 创建成功，下发确认包
			Packet pt = new Packet(OpCode.TONG_CREATE_SERVER);
			pt.putInt(serial);
			pt.putInt(tong.id);
			pt.putString(tong.name);
			TongMember tm = tongService.getPlayerInfo(player.id);
			pt.putInt(tm.duty);
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.TONG_CREATE_CLIENT, errorMessage);
		}
	}

	public void run() {
		// 创建军团必须达到25级，并扣除50000钱
		if (player.level < 25) {
			error(null, peony.Messages.STRING_01129);
			addToClientSession();
			return;
		}
		PlayerTransaction tx = player.newTransaction("CTG");
		try {
			player.decMoney(50000, tx, true);
		} catch (Exception e) {
			tx.rollback();
			error(null, peony.Messages.STRING_01130);
			addToClientSession();
			return;
		}

		// 创建军团
		try {
			tong = tongService.createTong(player.id, name);
			tx.commit();
		} catch (Exception ex) {
			tx.rollback();
			if (ex instanceof TongException) {
				error(null, ex.getMessage());
			} else {
				error(null, peony.Messages.STRING_01131);
			}
		}
		addToClientSession();
	}
}
