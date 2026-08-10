package peony.service.account;

import peony.db.PlayerRenameCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.cmcc.CmccAccountRenameMessage;

import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.RenameMessage;
import com.pip.net.message.gameaccount.RenameOkMessage;

public class AccountRenameCall extends AccountAsyncCall {
	
	protected Account account;
	protected String name;
	protected String oldPlayerName;
	protected int serial;
	
	public AccountRenameCall(ClientSession session,int serial,String name){
		super(session);
		account = (Account)session.getIdentity();
		this.serial = serial;
		this.name = name;
		this.oldPlayerName = ((Player)session.getClient()).name;
	}

	public void callFinish() throws Exception {
		if (success) {
			Player p = (Player) session.getClient();
			if (p != null && p.name.equals(oldPlayerName)) {
				RenameOkMessage msg = (RenameOkMessage)message;
				Account a = (Account)session.getIdentity();
				if(a!=null){
					a.name = name;
				}
				Packet pt = new Packet(OpCode.CHANGE_NAME_SERVER);
				pt.putInt(serial);
				pt.putString(name);
				session.send(pt);
				Server.server.getServiceRegistry().getDbService().schedule(
						new PlayerRenameCall(session, p.id, name,-1,false));
			}
		}else{
		    ErrorMessage msg = (ErrorMessage) message;
			ErrorHandler.sendErrorMessage(session, serial, OpCode.CHANGE_NAME_CLIENT, ErrorMessages.getErrorMesssage(msg));
		}
	}

	public void run() {
		if ("CMCC".equals(Server.server.revision)||"CHINATEL".equals(Server.server.revision)) {
			Player p = (Player)session.getClient();
			if (p != null) {
				CmccAccountRenameMessage message = new CmccAccountRenameMessage(
						account.getName(), name, account.id, p.id);
				Server.server.getServiceRegistry().getAccountService()
						.sendAndRegister(message,this);
			}
		} else {
			RenameMessage message = new RenameMessage(account.getName(), name);
			Server.server.getServiceRegistry().getAccountService()
					.sendAndRegister(message, this);
		}
	}

}
