package peony.service.account;

import java.text.MessageFormat;

import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.mail.MailService;
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
//			String m = MessageFormat.format("密码修改成功，" +
//					"您的账号为{0}，密码为{1}，为了避免造成不必要的损失，请牢记您的账户信息。", account.name,account.password);
			String m = "密码修改成功，我知道了";
			pt.putString(m);
			session.send(pt);
			Player p = (Player) session.getClient();
			if (p != null) {
//				p.message(-1, m, -1, -1);
				m = MessageFormat.format("密码修改成功，" +
						"您的账号为{0}，密码为{1}，为了避免造成不必要的损失，请牢记您的账户信息。", account.name,account.password);
				MailService mailService = Server.server.getServiceRegistry().getMailService();
				mailService.sendSystemMail(p.id, "系统", "密码修改成功", m, 0, null, 0, "CHAGEACCNAME");

			}
		} else {
		    ErrorMessage msg = (ErrorMessage) message;
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.CHANGE_PASSWORD_CLIENT, ErrorMessages.getErrorMesssage(msg));
		}
	}

	public void run() {
		Account account = (Account) session.getIdentity();
		if (account != null) {
			if (account.name.startsWith(peony.Messages.STRING_00872)) {
				oldPassword = account.password;
			}
			if (Server.server.REVISION_TYPE_CMCC.equals(Server.server.revision)||Server.server.REVISION_TYPE_TEL.equals(Server.server.revision)) {
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
			error(null, peony.Messages.STRING_00873);
		}
	}

}
