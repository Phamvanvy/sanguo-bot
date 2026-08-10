package peony.service.account;

import org.apache.log4j.Logger;

import peony.db.DBService;
import peony.db.PlayerQuickCreateCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.cmcc.CmccAccountQuickRegMessage;

import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.LegacyQuickRegMessage;
import com.pip.net.message.gameaccount.LegacyQuickRegResultMessage;

public class AccountQuickRegisterCall extends AccountAsyncCall {
    private static Logger log = Logger.getLogger(AccountLoginCall.class);
    
	protected int serial;
	protected String phone;
	protected String model;
	protected String version;
	protected String realPhone;
	protected String cmccUserId;
	protected String cmccUserKey;
	protected String IMEI = "";	
	
	public AccountQuickRegisterCall(ClientSession session,Packet pt){
		super(session);
		this.serial = pt.getInt();
		this.phone = pt.getString();
		this.model = pt.getString();
		this.version = pt.getString();
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
		this.IMEI = pt.getString();
		log.info("[QUICKREG]VERSION[" + version + "]MODEL[" + model + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "TRY");
	}
	
	public void callFinish() throws Exception {
		if (success) {
		    LegacyQuickRegResultMessage msg = (LegacyQuickRegResultMessage) message;
		    if(!IMEI.equals("")){
		    	log.info("[QUICKREG]NAME[" + msg.getName() + "]MIEI["+IMEI+"]OK");
		    }else{
		    	log.info("[QUICKREG]NAME[" + msg.getName() + "]OK");
		    }
            DBService dbService = Server.server.getServiceRegistry().getDbService();
			dbService.schedule(new PlayerQuickCreateCall(session,msg, cutJvmCode(model)));
		} else {
		    ErrorMessage msg = (ErrorMessage) message;
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ACCOUNT_QUICK_REG_CLIENT, ErrorMessages.getErrorMesssage(msg));
		}
	}
	
	protected String cutJvmCode(String value) {
        int pos = value.indexOf('/');
        return value.substring(pos + 1);
    }

	public void run() {
		if ("TAIWAN".equals(Server.server.revision)) {
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ACCOUNT_QUICK_REG_CLIENT, "Công năng này chưa mở");
			return;
		}
		if ("CMCC".equals(Server.server.revision)||"CHINATEL".equals(Server.server.revision)) {
			CmccAccountQuickRegMessage message = new CmccAccountQuickRegMessage(
					phone, version, model, Server.server.gameCode, realPhone,
					cmccUserId, cmccUserKey);
			Server.server.getServiceRegistry().getAccountService()
					.sendAndRegister(message, this);
		} else {
			LegacyQuickRegMessage message = new LegacyQuickRegMessage(phone,
					version, model, Server.server.gameCode, realPhone);
			Server.server.getServiceRegistry().getAccountService()
					.sendAndRegister(message, this);
		}
	}

}
