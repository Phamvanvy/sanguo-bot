package peony.service.account;

import org.apache.log4j.Logger;

import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.adapter.QmeAccount;
import peony.service.account.adapter.QmeAdapter;
import peony.service.account.adapter.QmeException;
import peony.service.account.cmcc.CmccAccountRegMessage;

import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.AccountRegMessage;
import com.pip.net.message.gameaccount.AccountRegOkMessage;

public class AccountRegisterCall extends AccountAsyncCall {
    private static Logger log = Logger.getLogger(AccountLoginCall.class);
    
	protected int serial;
	protected String name;
	protected String phone;
	protected String model;
	protected String version;
	protected String realPhone;
	protected String initPass;
	protected QmeAccount qmeAcc;
	protected String cmccUserId;
	protected String cmccUserKey;
	
	public AccountRegisterCall(ClientSession session,Packet pt){
		super(session);
		this.serial = pt.getInt();
		this.name = pt.getString();
		this.phone = pt.getString();
		this.model = pt.getString();
		this.version = pt.getString();
		this.initPass = pt.getString();
		if (session.getClientIP() != null && session.getClientIP().length() > 0) {
		    this.version += "/" + session.getClientIP();
		}
		try {
		    this.realPhone = AccountLoginCall.cutPhone(pt.getString());
		} catch (Exception e) {
		}
		if (this.realPhone == null) {
		    this.realPhone = "";
		}
		if("CMCC".equals(Server.server.revision)||"CHINATEL".equals(Server.server.revision)){
			this.cmccUserId = pt.getString();
			this.cmccUserKey = pt.getString();
		}
		log.info("[REGISTER]NAME[" + name + "]PHONE[" + phone + "]MODEL[" + model + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]TRY");
	}
	
	public void callFinish() throws Exception {
		if (success) {
		    log.info("[REGISTER]NAME[" + name + "]OK");
		    if ("TAIWAN".equals(Server.server.revision)) {
		    	Packet pt = new Packet(OpCode.ACCOUNT_REG_SERVER);
				pt.putInt(serial);
				pt.putString(name);
				pt.putInt(qmeAcc.qmeID);
				pt.putString(initPass);
				pt.putInt(-1);
				pt.putString(qmeAcc.smsCode);
				session.send(pt);
		    } else {
				AccountRegOkMessage msg = (AccountRegOkMessage) message;
				Packet pt = new Packet(OpCode.ACCOUNT_REG_SERVER);
				pt.putInt(serial);
				pt.putString(name);
				pt.putInt(msg.getAccountId());
				pt.putString(msg.getPassword());
				pt.putInt(-1);
				session.send(pt);
		    }
		} else {
		    ErrorMessage msg = (ErrorMessage) message;
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ACCOUNT_REG_CLIENT, ErrorMessages.getErrorMesssage(msg));
		}
	}

	public void run() {
//		if(true){
//			ErrorHandler.sendErrorMessage(session, serial,
//					OpCode.ACCOUNT_REG_CLIENT, "此功能暂未开放");
//			return;
//		}
		if(initPass.length()==0){
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_REG_CLIENT, "Mật mã không được để trống");
			return;
		}
		if ("TAIWAN".equals(Server.server.revision)) {
			try {
				qmeAcc = QmeAdapter.register(this.name, this.initPass, this.phone);
			} catch (QmeException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_REG_CLIENT, e.getMessage());
				return;
			}
			AccountRegMessage message = new AccountRegMessage(name,"","",-1,model,Server.server.gameCode,version,realPhone,"123");
			Server.server.getServiceRegistry().getAccountService().sendAndRegister(message,this);
		}
		else if("CMCC".equals(Server.server.revision)||"CHINATEL".equals(Server.server.revision)){
			CmccAccountRegMessage message = new CmccAccountRegMessage(name, phone, "", -1, model, Server.server.gameCode, version, realPhone, initPass, cmccUserId, cmccUserKey);
			Server.server.getServiceRegistry().getAccountService().sendAndRegister(message,this);
		}else {
		    AccountRegMessage message = new AccountRegMessage(name,phone,"",-1,model,Server.server.gameCode,version,realPhone,initPass);
			Server.server.getServiceRegistry().getAccountService().sendAndRegister(message,this);
		}
	}

}
