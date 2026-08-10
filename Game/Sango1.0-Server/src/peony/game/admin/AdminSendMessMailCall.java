package peony.game.admin;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminSendMessMailCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int playerId;
	protected String title;
	protected String content;
	
	public AdminSendMessMailCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.playerId = packet.getInt();
		this.title = packet.getString();
		this.content = packet.getString();
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		Server.server.getServiceRegistry().getMailService()
		.sendSystemMail(playerId, peony.Messages.STRING_00004, title, content,0, null, 0, "GM");
	}

}
