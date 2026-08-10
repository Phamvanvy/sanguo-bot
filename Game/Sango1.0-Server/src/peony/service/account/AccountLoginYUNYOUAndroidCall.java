package peony.service.account;

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Server;
import peony.game.Version;
import peony.net.ClientSession;
import peony.net.Packet;
import com.bonc.YyGameSDK;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.AccountRegMessage;
import com.pip.net.message.gameaccount.AccountRegOkMessage;
import com.pip.net.message.gameaccount.LegacyLoginMessage;
import com.pip.net.message.gameaccount.LegacyLoginOkMessage;

/**
 * 云游android账号登陆
 * @author dchen
 */
public class AccountLoginYUNYOUAndroidCall extends AccountAsyncCall {
	
	protected static final Logger log = LoggerFactory.getLogger(AccountLoginYUNYOUAndroidCall.class);

	protected String sessionId,userId,version,model,uiModel,realPhone;
	protected String accountName;   // 实际在认证服务器注册的账号名，格式为yunyou:uid
	protected int serial,playerId;
	protected Version v;
	protected static final String PUBLIC_PASSWORD = "0f04z34jw";
	
	protected int balance;
	
	public AccountLoginYUNYOUAndroidCall(ClientSession session,String sessionId,String userId,
			String version,String model,String uiModel,String realPhone,int playerId,int serial){
		super(session);
		this.sessionId = sessionId;
		this.userId = userId;
		this.version = version;
		this.model = model;
		this.uiModel = uiModel;
		this.realPhone = realPhone;
		this.playerId = playerId;
		this.serial = serial;
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
    
    public String cutVersion(String value){
        String[] s = value.split("-");
        return s[0];
    }
    
	public void callFinish(){
		if (success) {
			if (message instanceof AccountRegOkMessage) {
				// 自动注册成功返回。再发起一个登录请求。
    			LegacyLoginMessage loginMsg = new LegacyLoginMessage(accountName, PUBLIC_PASSWORD, realPhone);
    			Server.server.getServiceRegistry().getAccountService().sendAndRegister(loginMsg, this);
    	        return;
    		}
			
			// 登录成功，或者自动注册后登录成功。
            LegacyLoginOkMessage msg = (LegacyLoginOkMessage) message;
           
            log.info("[ACCOUNTLOGIN]NAME[" + accountName + "]ACC[" + msg.getAccountId() + "]BALANCE[" + msg.getIMoney() + "]SESID[" +  LogUtil.getSessionIdBySession(getSession())+"]OK");
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
            account.setChannel(cutChannel(version));
            account.setVersion(v);
            account.setVersionString(version);
            account.setPassword(PUBLIC_PASSWORD);
            account.setRealPhone(realPhone);
            account.setYySessionId(sessionId);
            account.setYyUserId(userId);
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
            
            Packet pt1 = new Packet(OpCode.ACCOUNT_LOGIN_YUNYOU_ANDROID_SERVER);
            pt1.putInt(serial);
            pt1.putString(String.valueOf(balance));
            getSession().send(pt1);
            
		} else {
			ErrorMessage em = (ErrorMessage)message;
			
			// 如果是账号不存在错误，自动发送注册请求
			if (em.getCode() == ErrorMessages.UNKNOW_ACCOUNT || em.getCode() == ErrorMessages.ERROR_NAMEORPASSWORD) {
				this.setSuccess(true);
				String version2 = version;
				if (session.getClientIP() != null && session.getClientIP().length() > 0) {
		            version2 += "/" + session.getClientIP();
		        }
				AccountRegMessage regMsg = new AccountRegMessage(accountName,"","",-1,model,Server.server.gameCode,version2,realPhone,PUBLIC_PASSWORD);
				Server.server.getServiceRegistry().getAccountService().sendAndRegister(regMsg, this);
				return;
			}
			
			// 其他错误通知客户端
			ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.ACCOUNT_LOGIN_YUNYOU_ANDROID_CLIENT, ErrorMessages.getErrorMesssage(em));
		}
	}
	
	public void run() {
		log.info("[YUNYOUANDROIDACCLOGIN]CODE[" + sessionId + "]TRY");
    	if (!session.checkOnlineCount(0)) {
            ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_YUNYOU_ANDROID_CLIENT, "已经到达最大登录数量");
            return;
        }
		v = Server.server.getServiceRegistry().getVersionService().getVersion(cutVersion(version));
		
		balance = verifyFromYUNYOU(sessionId);
		boolean checkLegality = balance>-1;
		if(!checkLegality){
			ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.ACCOUNT_LOGIN_YUNYOU_ANDROID_CLIENT, "不合法的登陆");
			return;
		}
		
		accountName = "yunyou:" + userId;
		
		log.info("[ACCLOGIN]NAME[" + accountName + "]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]SESID["+LogUtil.getSessionIdBySession(getSession())+"]IP[" + getSession().getClientIP() + "]TRY");

		// 尝试登陆
		boolean partition = Server.server.getConfig().getBoolean("partition", false);
		if (partition) {
			LegacyLoginMessage message = new LegacyLoginMessage(accountName, PUBLIC_PASSWORD,
					realPhone, Server.server.gameCode,version+"/"+session.getClientIP(),model);
			Server.server.getServiceRegistry().getAccountService().sendAndRegister(message, this);
		} else {
			LegacyLoginMessage message = new LegacyLoginMessage(accountName, PUBLIC_PASSWORD,
					realPhone, "",version+"/"+session.getClientIP(),model);
			Server.server.getServiceRegistry().getAccountService().sendAndRegister(message, this);
		}
	}
	
	//验证云游平台sesisonId合法
	protected int verifyFromYUNYOU(String sessionId){
		YyGameSDK yyGameSDK = new YyGameSDK();
		int count = yyGameSDK.getYyCount(sessionId, userId);
		return count;
		
	}
	
	/*
	 * 登录成功后自动载入角色。由具体项目的服务器实现。
	 */
	protected void autoLoadActor() {
	}
	
	public static void main(String[] args){
		YyGameSDK yyGameSDK = new YyGameSDK();
		int count = yyGameSDK.getYyCount("14a6fca2-3c1d-4f90-ab9d-d4f3106ca9ac10876964500023446", "100130");
		System.out.println("云游币余额：" + count);
	}
	
}
