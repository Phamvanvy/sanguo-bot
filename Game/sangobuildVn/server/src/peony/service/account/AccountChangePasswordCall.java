package peony.service.account;

import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.cmcc.CmccModifyPasswordMessage;

import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.ModifyPasswordMessage;
import com.pip.net.message.gameaccount.ModifyPasswordOkMessage;

public class AccountChangePasswordCall extends AccountAsyncCall {
	
	protected int serial;
	protected String password;
	protected String oldPassword;
	
	public AccountChangePasswordCall(ClientSession session,int serial,String oldPassword,String password){
		super(session);
		this.serial = serial;
		this.oldPassword = oldPassword;
		this.password = password;
	}

	public void callFinish() throws Exception {
		if (success) {
			ModifyPasswordOkMessage msg = (ModifyPasswordOkMessage) message;
			Account account = (Account) session.getIdentity();
			if(account!=null){
				account.password = password;
			}
			Packet pt = new Packet(OpCode.CHANGE_PASSWORD_SERVER);
			pt.putInt(serial);
			pt.putString(password);
			session.send(pt);
		} else {
		    ErrorMessage msg = (ErrorMessage) message;
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.CHANGE_PASSWORD_CLIENT, ErrorMessages.getErrorMesssage(msg));
		}
	}

	public void run() {
		Account account = (Account) session.getIdentity();
		if (account != null) {
			if (account.name.startsWith("Du khách \n")) {
				oldPassword = account.password;
			}
			if ("CMCC".equals(Server.server.revision)||"CHINATEL".equals(Server.server.revision)) {
				Player p = (Player) session.getClient();
				if (p != null) {
					CmccModifyPasswordMessage msg = new CmccModifyPasswordMessage(
							account.name, "", oldPassword, password, account.id, p.id);
					Server.server.getServiceRegistry().getAccountService()
							.sendAndRegister(msg, this);
				}
			} else {
				ModifyPasswordMessage msg = new ModifyPasswordMessage(
						account.name, account.key, oldPassword, password);
				Server.server.getServiceRegistry().getAccountService()
						.sendAndRegister(msg, this);
			}
		} else {
			error(null, "修改密码错误");
		}
	}

}
