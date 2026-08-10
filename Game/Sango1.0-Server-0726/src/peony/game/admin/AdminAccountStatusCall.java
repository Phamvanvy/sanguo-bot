package peony.game.admin;

import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.AccountAsyncCall;

import com.pip.net.message.gameaccount.ChangeStatusMessage;

public class AdminAccountStatusCall extends AccountAsyncCall {

	protected int accountId;
	protected int status;
	protected String cause;
	protected int serial;

	public AdminAccountStatusCall(ClientSession session, Packet pt) {
		super(session);
		this.serial = pt.getInt();
		this.accountId = pt.getInt();
		this.status = pt.getInt();
		this.cause = pt.getString();
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		
		Server.server.getServiceRegistry().getAccountService().postMessage(
				new ChangeStatusMessage(accountId, status, cause));
		Packet pt = new Packet(OpCode.ADMIN_ACCOUNT_STATUS_SERVER);
		pt.putInt(serial);
		session.send(pt);
	}

}
