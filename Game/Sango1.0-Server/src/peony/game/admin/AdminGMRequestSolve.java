package peony.game.admin;

import peony.channel.Channel;
import peony.common.ClientSessionAsyncCall;
import peony.game.Admin;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminGMRequestSolve extends ClientSessionAsyncCall {

	protected int id;
	protected int playerId;
	protected String mailTitle;
	protected String solvent;

	public AdminGMRequestSolve(ClientSession session, Packet packet) {
		super(session);
		this.id = packet.getInt();
		this.playerId = packet.getInt();
		this.mailTitle = packet.getString();
		this.solvent = packet.getString();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		Admin admin = (Admin) session.getClient();
		if (playerId == 0) {
			GMRequest request = Server.server.getServiceRegistry()
					.getDbService().gmQuestDAO.getGMRequestById(id);
			request.setSolvent(solvent + "---" + admin.name);
			request.setState(GMRequest.STATE_RESOLVED);
			Server.server.getServiceRegistry().getDbService().gmQuestDAO.updateEntity(request);
			playerId = request.getPlayerId();
		}
		 Server.server.getServiceRegistry().getMailService().sendSystemMail(playerId,
				 peony.Messages.STRING_00004, mailTitle, solvent, 0, null, 0, "GM");
		Packet pt = new Packet(OpCode.ADMIN_GMREQUEST_SOLVE_SERVER);
		pt.putInt(id);
		pt.putInt(playerId);
		pt.putString(admin.name);
		pt.putString(solvent);
		Channel channel = Server.server.getServiceRegistry().getChannelService().getChannel("gm");
		channel.broadcast(pt, null);
	}

}
