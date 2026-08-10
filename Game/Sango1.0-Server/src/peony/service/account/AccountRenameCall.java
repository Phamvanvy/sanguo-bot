package peony.service.account;

import java.text.MessageFormat;

import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.mail.MailService;
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
			if (p != null) {
				RenameOkMessage msg = (RenameOkMessage)message;
				Account a = (Account)session.getIdentity();
				if(a!=null){
					a.name = name;
				}
				Packet pt = new Packet(OpCode.CHANGE_NAME_SERVER);
				pt.putInt(serial);
				pt.putString(name);
				String m = "账号修改成功，我知道了";
				pt.putString(m);
				session.send(pt);
//				p.message(-1, m, -1, -1);
				m = MessageFormat.format("账号修改成功，" +
						"您的账号为{0}，密码为{1}，为了避免造成不必要的损失，请牢记您的账户信息。", account.name,account.password);
				MailService mailService = Server.server.getServiceRegistry().getMailService();
				mailService.sendSystemMail(p.id, "系统", "账号修改成功", m, 0, null, 0, "CHAGEACCNAME");
			}
		}else{
		    ErrorMessage msg = (ErrorMessage) message;
			ErrorHandler.sendErrorMessage(session, serial, OpCode.CHANGE_NAME_CLIENT, ErrorMessages.getErrorMesssage(msg));
		}
	}

	public void run() {
		if (Server.server.REVISION_TYPE_CMCC.equals(Server.server.revision)||Server.server.REVISION_TYPE_TEL.equals(Server.server.revision)) {
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
