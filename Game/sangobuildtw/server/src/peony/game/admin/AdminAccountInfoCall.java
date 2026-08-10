package peony.game.admin;

import peony.game.Actor;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.AccountAsyncCall;

import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.AccountInfoMessage;
import com.pip.net.message.gameaccount.AccountInfoOkMessage;

public class AdminAccountInfoCall extends AccountAsyncCall {

	protected int playerId;
	protected int serial;

	protected Actor actor;

	public AdminAccountInfoCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.playerId = packet.getInt();
	}

	public void callFinish() throws Exception {
		if (success) {
			AccountInfoOkMessage msg = (AccountInfoOkMessage)message;
			Packet pt = new Packet(OpCode.ADMIN_ACCOUNT_INFO_SERVER);
			pt.putInt(serial);
			pt.putInt(msg.getAccountId());
			pt.putString(msg.getName());
			pt.putString(msg.getPassword());
			pt.putString(msg.getPhone());
			session.send(pt);
		} else {
			if (message == null) {
				ErrorHandler.sendAdminErrorMessage(session, serial,
						OpCode.ADMIN_ACCOUNT_INFO_CLIENT, errorMessage);
			} else {
				ErrorHandler.sendAdminErrorMessage(session, serial,
						OpCode.ADMIN_ACCOUNT_INFO_CLIENT,
						((ErrorMessage) message).getCode() + "");
			}
		}
	}

	
	public void run() {
		actor = Server.server.getServiceRegistry().getActorCacheService().find(
				playerId);
		if (actor == null) {
			error(null, "沒找到對應角色");
			addToClientSession();
			return;
		}
		Server.server.getServiceRegistry().getAccountService().sendAndRegister(
				new AccountInfoMessage(actor.accountId, ""), this);

	}

}
