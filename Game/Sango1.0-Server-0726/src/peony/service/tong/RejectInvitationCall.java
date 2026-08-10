package peony.service.tong;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * ¾Ü¾ø¾üÍÅÑûÇë¡£
 * invid	int			Çë¼íID
 */
public class RejectInvitationCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(RejectInvitationCall.class);
	protected int inviteID;
	protected Player player;
	protected TongService tongService;

	public RejectInvitationCall(ClientSession session, Packet packet) {
		super(session);
		this.inviteID = packet.getInt();
		player = (Player)session.getClient();
		tongService = Server.server.getServiceRegistry().getTongService();
	}

	public void callFinish() throws Exception {
	}

	public void run() {
		// ¾Ü¾øÇëÇó
		tongService.reject(inviteID, player.id);
	}
}
