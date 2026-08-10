package peony.game.coordinate;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;

import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.AccountRegMessage;
import com.pip.net.message.gameaccount.AccountRegOkMessage;
import com.pip.net.message.gameaccount.LegacyLoginMessage;
import com.pip.net.message.gameaccount.LegacyLoginOkMessage;
import com.pip.partner.PartnerAPI;
import com.pip.partner.PartnerException;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.game.Version;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;
import peony.service.account.ErrorMessages;

public class AccountLogin360Call extends AccountAsyncCall {
	
	private static final Logger log = Logger.getLogger(AccountLogin360Call.class);

	protected int serial;
	protected String authorityCode;
	protected String model;
	protected String uiModel;
	protected String clientVersion;
	protected String clientPhone;
	protected int playerId;
	
	protected String accountName;
	protected String accountKey;
	
	protected Version v;
	
	public AccountLogin360Call(ClientSession session, int serial, String authorityCode, 
			String model, String uiModel, String clientVersion,String clientPhone, int playerId) {
		super(session);
		this.serial = serial;
		this.authorityCode = authorityCode;
		this.model = model;
		this.uiModel = uiModel;
		this.clientVersion = clientVersion;
		this.clientPhone = clientPhone;
		this.playerId = playerId;
		log.info("[360ACCLOGIN]AUTH["+authorityCode+"]MODEL["+model+"]UIMODEL["+uiModel+"]VERSION["+clientVersion+"]TRY");
	}

	public void callFinish() throws Exception {
		if (success) {
			if (message instanceof AccountRegOkMessage) {
				// 自动注册成功返回。再发起一个登录请求。
    			LegacyLoginMessage loginMsg = new LegacyLoginMessage(accountName, accountKey, clientPhone);
    			Server.server.getServiceRegistry().getAccountService().sendAndRegister(loginMsg, this);
    	        return;
    		}
			
			// 登录成功，或者自动注册后登录成功。
            LegacyLoginOkMessage msg = (LegacyLoginOkMessage) message;
           
            log.info("[ACCOUNTLOGIN]NAME[" + accountName + "]ACC[" + msg.getAccountId() + "]BALANCE[" + msg.getIMoney() + "]SESID[" +  getSession().getId()+"]OK");
            Account account = new Account(msg.getAccountId(), msg.getName(), msg.getKey());
            account.setLongIMoney(msg.getIMoney());
            account.setLoginErrorTimes(msg.getLoginErrorTimes());
            account.setModifiedNameTimes(msg.getModifiedNameTimes());
            account.setMonth(msg.isMonth());
            account.setPhone(msg.getPhone());
            account.setPurchasedCodes(msg.getPurchasedCodes());
            account.setModel(cutModel(model));
            account.setUiModel(uiModel);
            account.setJvmCode(cutJvmCode(model));
            account.setChannel(cutChannel(clientVersion));
            account.setVersion(v);
            account.setVersionString(clientVersion);
            account.setPassword(accountKey);
            account.setRealPhone(clientPhone);
            getSession().authenticate(account);
            
            // 向客户端发送登录成功通知
            Packet pt = new Packet(OpCode.ACCOUNT_LOGIN_SERVER);
            pt.putInt(serial);
            pt.putInt(account.getId());
            pt.putString(account.getName());
            double iMoney = (double)account.getLongIMoney() / 36 / 100;
            DecimalFormat df = new DecimalFormat("0.00");
			String showPrice = df.format(iMoney);
            pt.putString(showPrice);
            pt.putInt(account.getModifiedNameTimes());
            pt.putUTF(Server.server.gameCode);
            getSession().send(pt);

            // 如果带角色ID登录，转向自动登录角色流程
            if (playerId != -1 && playerId != 0) {
            	autoLoadActor();
            }
		} else {
			ErrorMessage em = (ErrorMessage)message;
			
			// 如果是账号不存在错误，自动发送注册请求
			if (em.getCode() == ErrorMessages.UNKNOW_ACCOUNT || em.getCode() == ErrorMessages.ERROR_NAMEORPASSWORD) {
				this.setSuccess(true);
				String version2 = clientVersion;
				if (session.getClientIP() != null && session.getClientIP().length() > 0) {
		            version2 += "/" + session.getClientIP();
		        }
				AccountRegMessage regMsg = new AccountRegMessage(accountName,"","",-1,model,Server.server.gameCode,version2,clientPhone,accountKey);
				Server.server.getServiceRegistry().getAccountService().sendAndRegister(regMsg, this);
				return;
			}
			
			// 其他错误通知客户端
			ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.ACCOUNT_LOGIN_360_CLIENT, ErrorMessages.getErrorMesssage(em));
			log.info("[360ACCLOGINEEOR]AUTH["+authorityCode+"]CAUSE["+ErrorMessages.getErrorMesssage(em)+"]");
		}
	}

	public void run() {
		try {
			v = Server.server.getServiceRegistry().getVersionService().getVersion(clientVersion);
		} catch (Exception e) {
			log.info("[360ACCLOGINEEOR]AUTH["+authorityCode+"]CAUSE[VERSION]");
			ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.ACCOUNT_LOGIN_360_CLIENT, e.getMessage());
			return;
		}
		
		String[] userInfo = new String[]{authorityCode};
		try {
			String[] accountInfo = PartnerAPI.verifyUser("360", userInfo, model, clientVersion);
			accountName = accountInfo[0];
			accountKey = accountInfo[1];
			
			// 尝试登陆
			boolean partition = Server.server.getConfig().getBoolean("partition", false);
			if (partition) {
				LegacyLoginMessage message = new LegacyLoginMessage(accountName, accountKey,
						clientPhone, Server.server.gameCode,clientVersion+"/"+session.getClientIP(),model);
				Server.server.getServiceRegistry().getAccountService().sendAndRegister(message,this);
			} else {
				LegacyLoginMessage message = new LegacyLoginMessage(accountName, accountKey,
						clientPhone, "",clientVersion+"/"+session.getClientIP(),model);
				Server.server.getServiceRegistry().getAccountService().sendAndRegister(message,this);
			}
		} catch (PartnerException e) {
			log.info("[360ACCLOGINEEOR]AUTH["+authorityCode+"]CAUSE["+e.getMessage()+"]");
			ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.ACCOUNT_LOGIN_360_CLIENT, e.getMessage());
			return;
		}
	}
	
	protected String cutChannel(String value){
        String[] s = value.split("-");
        if (s.length > 1) {
        	return s[1];
        } else {
        	return "";
        }
    }
    
    protected String cutModel(String value) {
        int pos = value.indexOf('/');
        if (pos == -1) {
        	return value;
        } else {
        	return value.substring(0, pos);
        }
    }
    
    protected String cutJvmCode(String value) {
        int pos = value.indexOf('/');
        if (pos == -1) {
        	return "";
        } else {
        	return value.substring(pos + 1);
        }
    }
    
    /*
	 * 登录成功后自动载入角色。由具体项目的服务器实现。
	 */
	protected void autoLoadActor() {
	}

}
