package peony.service.account;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.game.Version;
import peony.net.ClientSession;
import peony.net.Packet;

import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.AccountRegMessage;
import com.pip.net.message.gameaccount.AccountRegOkMessage;
import com.pip.net.message.gameaccount.LegacyLoginMessage;
import com.pip.net.message.gameaccount.LegacyLoginOkMessage;

/**
 * 联想乐逗账号登陆
 * @author lighthu
 */
public class AccountLoginLenovoCall extends AccountAsyncCall {
	
	protected static final Logger log = LoggerFactory.getLogger(AccountLoginLenovoCall.class);

	protected String game_id,openid,clientSessionId,version,model,uiModel,realPhone;
	protected String accountName;
	protected int serial,playerId;
	protected Version v;
	protected static final String PUBLIC_PASSWORD = "0f04z34jw";
	
	public AccountLoginLenovoCall(ClientSession session,String game_id,String openid,String clientSessionId,
			String version,String model,String uiModel,String realPhone,int playerId,int serial){
		super(session);
		this.game_id = game_id;
		this.openid = openid;
		this.accountName = "lvo:" + openid;
		this.clientSessionId = clientSessionId;
		this.version = version;
		this.model = model;
		this.realPhone = realPhone;
		this.playerId = playerId;
		this.serial = serial;
		this.uiModel = uiModel;
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
    
	public void callFinish() {
		if (success) {
			if (message instanceof AccountRegOkMessage) {
				// 自动注册成功返回。再发起一个登录请求。
    			LegacyLoginMessage loginMsg = new LegacyLoginMessage(accountName, PUBLIC_PASSWORD, realPhone);
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
            account.setChannel(cutChannel(version));
            account.setVersion(v);
            account.setVersionString(version);
            account.setPassword(PUBLIC_PASSWORD);
            account.setRealPhone(realPhone);
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
				String version2 = version;
				if (session.getClientIP() != null && session.getClientIP().length() > 0) {
		            version2 += "/" + session.getClientIP();
		        }
				AccountRegMessage regMsg = new AccountRegMessage(accountName,"","",-1,model,Server.server.gameCode,version2,realPhone,PUBLIC_PASSWORD);
				Server.server.getServiceRegistry().getAccountService().sendAndRegister(regMsg, this);
				return;
			}
			
			// 其他错误通知客户端
			ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.ACCOUNT_LOGIN_LENOVO_CLIENT, ErrorMessages.getErrorMesssage(em));
		}
	}
	
	public void run() {
		log.info("[ACCLOGIN]NAME[" + accountName + "]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]SESID["+getSession().getId()+"]IP[" + getSession().getClientIP() + "]TRY");
    	if (!session.checkOnlineCount(0)) {
            ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_LENOVO_CLIENT, "已经到达最大登录数量");
            return;
        }
		try {
			v = Server.server.getServiceRegistry().getVersionService().getVersion(version);
		} catch (Exception e) {
			ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.ACCOUNT_LOGIN_LENOVO_CLIENT, e.getMessage());
			return;
		}
		
		// 到认证服务器校验联想用户登录
		String result = verifyLogin();
		if (result != null) {
			ErrorHandler.sendErrorMessage(getSession(), serial, OpCode.ACCOUNT_LOGIN_LENOVO_CLIENT, result);
			return;
		}
		
		// 尝试登陆
		boolean partition = Server.server.getConfig().getBoolean("partition", false);
		if (partition) {
			LegacyLoginMessage message = new LegacyLoginMessage(accountName, PUBLIC_PASSWORD,
					realPhone, Server.server.gameCode,version+"/"+session.getClientIP(),model);
			Server.server.getServiceRegistry().getAccountService().sendAndRegister(message,this);
		} else {
			LegacyLoginMessage message = new LegacyLoginMessage(accountName, PUBLIC_PASSWORD,
					realPhone, "",version+"/"+session.getClientIP(),model);
			Server.server.getServiceRegistry().getAccountService().sendAndRegister(message,this);
		}
	}
	
	// 验证联想用户登录，如果成功返回null，错误返回错误信息。
	protected String verifyLogin() {
		PostMethod method;
		BufferedReader br = null;
		method = new PostMethod(Server.server.billingURL + "lenovo_login");
		method.addRequestHeader("Connection", "close");
		method.setParameter("gamecode", "6");
		method.setParameter("gameid", game_id);
		method.setParameter("openid", openid);
		method.setParameter("sessionid", clientSessionId);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
		int code = 0;
		try {
			HttpClient httpclient = new HttpClient();
			httpclient.getHttpConnectionManager().getParams()
					.setConnectionTimeout(30000);
			httpclient.getParams().setSoTimeout(30000);
			code = httpclient.executeMethod(method);
			br = new BufferedReader(new InputStreamReader(method
					.getResponseBodyAsStream(), "UTF-8"));
			if (code == 200) {
				String line = br.readLine();
				int retCode = Integer.parseInt(line);
				if (retCode != 0) {
					return br.readLine();
				}
				return null;
			} else {
				return "网络故障，请稍后重试";
			}
		} catch (Exception ex) {
			log.error(ex.toString(), ex);
			return "网络故障，请稍后重试";
		} finally {
			method.releaseConnection();
		}
	}

	/*
	 * 登录成功后自动载入角色。由具体项目的服务器实现。
	 */
	protected void autoLoadActor() {
	}
}
