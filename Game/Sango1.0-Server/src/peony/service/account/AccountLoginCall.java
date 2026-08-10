package peony.service.account;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import peony.db.DBService;
import peony.db.PlayerLoadCall;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Server;
import peony.game.Version;
import peony.game.World;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.adapter.QmeAccount;
import peony.service.account.adapter.QmeAdapter;
import peony.service.account.adapter.QmeException;
import peony.service.account.cmcc.CmccAccountLoginMessage;
import peony.service.account.cmcc.CmccLoginOkMessage;
import peony.service.player.PlayerService;

import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.AccountRegMessage;
import com.pip.net.message.gameaccount.AccountRegOkMessage;
import com.pip.net.message.gameaccount.LegacyLoginMessage;
import com.pip.net.message.gameaccount.LegacyLoginOkMessage;

public class AccountLoginCall extends AccountAsyncCall{
    private static Logger log = Logger.getLogger(AccountLoginCall.class);

    protected String name;
    protected String password;
    protected String model;
    protected String uiModel;	//UI实际机型版本
    protected String version;
    protected int serial;
    protected Version v;
    protected String realPhone;
    protected int playerID;
    protected QmeAccount qmeAcc;
    protected String cmccUserId,cmccUserKey;
    protected String IMEI = "";
    protected String qmeId = "0";

    public AccountLoginCall(ClientSession session, String name, String password, String model, String uiModel,
    			String version, int serial, String realPhone, int playerID, String IMEI, String qmeId){
        super(session);
        this.name = name;
        this.password = password;
        this.model = model;
        this.uiModel = uiModel;
        this.version = version;
        this.serial = serial;
        this.realPhone = cutPhone(realPhone);
        this.playerID = playerID;
        this.IMEI = IMEI;
        this.qmeId = qmeId;
        if(!IMEI.equals("")){
        	log.info("[ACCLOGIN]NAME[" + name + "]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]SESSIONID["+LogUtil.getSessionIdBySession(session)+"]IMEI["+IMEI+"]TRY");
        }else{
        	log.info("[ACCLOGIN]NAME[" + name + "]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]SESSIONID["+LogUtil.getSessionIdBySession(session)+"]TRY");
        }
    }

    public String cutVersion(String value){
        String[] s = value.split("-");
        return s[0];
    }
    
    protected static String cutChannel(String value){
        String[] s = value.split("-");
        return s[1];
    }
    
    protected String cutModel(String value) {
        int pos = value.indexOf('/');
        return value.substring(0, pos);
    }
    
    protected String cutJvmCode(String value) {
        int pos = value.indexOf('/');
        return value.substring(pos + 1);
    }
    
    public void setCmccUserId(String cmccUserId){
    	this.cmccUserId = cmccUserId;
    }
    
    public void setCmccUserKey(String cmccUserKey){
    	this.cmccUserKey = cmccUserKey;
    }

    public void callFinish(){
        if(success){
        	if (Server.REVISION_TYPE_TW.equals(Server.server.revision)) {
        		// 台湾版本，第一次登录可能因为是首次登录而失败，向认证发送注册请求，所以这里也要处理注册成功的情况，再重新发送一次登录请求
        		if (message instanceof AccountRegOkMessage) {
        			LegacyLoginMessage loginMsg = new LegacyLoginMessage(name, "123", realPhone);
        	        Server.server.getServiceRegistry().getAccountService().sendAndRegister(loginMsg, this);
        	        return;
        		}
        	}
            LegacyLoginOkMessage msg = (LegacyLoginOkMessage) message;
            int accountId = msg.getAccountId();
    		boolean isAccountMuted = Server.server.getServiceRegistry().getPlayerService()
    			.isAccountMuted(accountId);
    		if(isAccountMuted){
    			ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, "您的账号已被封");
    			return;
    		}
            Account account = new Account(accountId, msg.getName(), msg.getKey());
            account.setLongIMoney(msg.getLongBalance());
            log.info("[ACCLOGIN]NAME[" + name + "]IMONEY[" + msg.getIMoney() + "]OK");
            account.setLoginErrorTimes(msg.getLoginErrorTimes());
            account.setModifiedNameTimes(msg.getModifiedNameTimes());
            account.setMonth(msg.isMonth());
            account.setPhone(msg.getPhone());
            account.setPurchasedCodes(msg.getPurchasedCodes());
            account.setVersion(v);
            account.setVersionString(version);
            account.setModel(cutModel(model));
            account.setUiModel(uiModel);
            account.setJvmCode(cutJvmCode(model));
            account.setChannel(cutChannel(version));
            account.setPassword(password);
            account.setRealPhone(realPhone);
            account.setCmccUserId(cmccUserId);
            account.setCmccUserKey(cmccUserKey);
            if(msg instanceof CmccLoginOkMessage){
            	account.setCity(((CmccLoginOkMessage)msg).getCityName());
            	account.setBalance2(((CmccLoginOkMessage)msg).getBalance2());
            	account.setCmccAttr(((CmccLoginOkMessage)msg).getAttr());
            	log.info("[CMCC-BALANCE]"+account.balance2);
            	log.info("NAME[" + name + "]SET CITY OK");
            }
            if (Server.REVISION_TYPE_TW.equals(Server.server.revision)) {
            	account.setMetaInfo(qmeAcc);
            	if(qmeId!=null)
            		try{qmeAcc.qmeID = Integer.parseInt(qmeId);}catch(Exception e){}
            }
            try{
                session.authenticate(account);
                Server.server.getServiceRegistry().getAccountService().registerClientSession(session);
                Packet pt = new Packet(OpCode.ACCOUNT_LOGIN_SERVER);
                pt.putInt(serial);
                pt.putInt(account.getId());
                pt.putString(account.getName());
                
                //pt.putInt((int)(account.getLongIMoney() / 100));
                double iMoney = (double)account.getLongIMoney() / 36 / 100;
                DecimalFormat df = new DecimalFormat("0.00");
    			String showPrice = df.format(iMoney);
                pt.putString(showPrice);
                
                pt.putInt(account.getModifiedNameTimes());
                if(Server.REVISION_TYPE_TW.equals(Server.server.revision)){
                	pt.putString(qmeAcc.tsi);
                }
                if(msg instanceof CmccLoginOkMessage)
                	pt.putInt((int)account.getBalance2());
                pt.putUTF(Server.server.gameCode);
                session.send(pt);
            }catch(SecurityException e){
                log.error(e, e);
            }
            
            // 如果传入了角色ID，直接登录
            if (playerID != -1&& playerID != 0) {
        		DBService dbService = Server.server.getServiceRegistry().getDbService();
        		PlayerService playerService = Server.server.getServiceRegistry().getPlayerService();
        		World world = Server.server.getWorld();
        		dbService.schedule(new PlayerLoadCall(playerService, session, playerID, world, serial, ""));
            }
        }else{
            ErrorMessage msg = (ErrorMessage) message;
            if (Server.REVISION_TYPE_TW.equals(Server.server.revision)) {
            	// 台湾版本，如果登录是发现帐号不存在，则发送注册请求
	            if (msg.getCode() == ErrorMessages.UNKNOW_ACCOUNT || msg.getCode() == ErrorMessages.ERROR_NAMEORPASSWORD) {
        			this.setSuccess(true);
					String version2 = version;
					if (session.getClientIP() != null && session.getClientIP().length() > 0) {
			            version2 += "/" + session.getClientIP();
			        }
	    			AccountRegMessage regMsg = new AccountRegMessage(name,"","",-1,model,Server.server.gameCode,version2,realPhone,"123");
	    			Server.server.getServiceRegistry().getAccountService().sendAndRegister(regMsg,this);
	    			return;
	            }
            }
            ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, ErrorMessages.getErrorMesssage(msg));
        }
    }

    public void run(){
    	if(ObjectAccessor.players.size()>=Server.server.maxPlayer){
            ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, peony.Messages.STRING_01363);
            log.info("[ACCLOGIN]NAME[" + name + "]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]FAIL[MAXCOUNT]");
            return;
    	}
    	if (!session.checkOnlineCount(0)) {
            ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, peony.Messages.STRING_01363);
            log.info("[ACCLOGIN]NAME[" + name + "]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]FAIL[MAXCOUNT]");
            return;
        }
    	String[] clientVersionStr = version.split("\\.");
		String[] clientVersionStr1 = version.split("-");
		if(clientVersionStr.length>=3 && !clientVersionStr[0].equals("0")){
			StringBuffer sb = new StringBuffer();
			if(clientVersionStr1.length<=1){
				version = sb.append(clientVersionStr[0]).append(".").append(clientVersionStr[1]).toString();
			}else if(clientVersionStr1.length==2){
				version = sb.append(clientVersionStr[0]).append(".").append(clientVersionStr[1]+"-").
				append(clientVersionStr1[1]).toString();
			}else if(clientVersionStr1.length==3){
				version = sb.append(clientVersionStr[0]).append(".").append(clientVersionStr[1]+"-").
				append(clientVersionStr1[1]).append("-").append(clientVersionStr1[2]).toString();
			}
		}
        v = Server.server.getServiceRegistry().getVersionService().getVersion(cutVersion(version));
        if(v == null || v.status == Version.STATUS_OBSOLETE){
            ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, v == null? peony.Messages.STRING_01364: v.message);
            if(v != null&&v.url != null){
            	Packet pt = new Packet(OpCode.SYMBIAN_UPDATE_URL_SERVER);
            	pt.putString(v.url);
            	pt.putString(cutSis(v.url));
            	session.send(pt);
            }
            log.info("[ACCLOGIN]NAME[" + name + "]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]FAIL[VERSION]");
            return;
        }
        if (Server.REVISION_TYPE_TW.equals(Server.server.revision)) {
        	try {
        		if(!QmeAdapter.useNewLoginProtocol)
        			qmeAcc = QmeAdapter.login(name, password);
        		else
        			qmeAcc = QmeAdapter.newLogin(name, password);
        		if (qmeAcc.smsCode != null) {
        			ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, qmeAcc.smsCode);
        			log.info("[ACCLOGIN]NAME[" + name + "]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]FAIL[QME]");
                    return;
        		}
        	} catch (QmeException e) {
        		ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, e.getMessage());
        		log.info("[ACCLOGIN]NAME[" + name + "]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]FAIL[QME]");
                return;
        	}
        	
        	// 首先尝试登录，登录可能会返回UNKNOWN_ACCOUNT错误
        	LegacyLoginMessage message = new LegacyLoginMessage(name, "123", realPhone);
	        Server.server.getServiceRegistry().getAccountService().sendAndRegister(message, this);
        } else if (Server.REVISION_TYPE_CMCC.equals(Server.server.revision)||Server.REVISION_TYPE_TEL.equals(Server.server.revision)) {
        	CmccAccountLoginMessage message = new CmccAccountLoginMessage(name, password, realPhone, cmccUserId, cmccUserKey);
        	Server.server.getServiceRegistry().getAccountService().sendAndRegister(message, this);
		} else if (Server.REVISION_TYPE_JAPAN.equals(Server.server.revision)) {
		    String checkUserUrl = Server.server.getConfig().configurationAt("hangame").getString("check_user_url");
		    String checkAccountUrl = Server.server.getConfig().configurationAt("hangame").getString("check_acc_url");
		    
		    //检测帐号是否已登录过hangame平台，如没有则返回失败
		    PostMethod method;
            method = new PostMethod(checkUserUrl);
            method.getParams().setContentCharset("utf-8");
            method.addRequestHeader("Connection", "close");
            method.setParameter("user_id", name);
            method.setParameter("owner_id", password);
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
            
            try {
                HttpClient httpclient = new HttpClient();
                httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
                httpclient.getParams().setSoTimeout(30000);
                int code = httpclient.executeMethod(method);
                if(code==200){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));
                    String result = reader.readLine();
                    String message = reader.readLine();
                    
                    if(result.contains("1")){
                        ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, message);
                        log.info("[ACCLOGIN]NAME[" + name + "]HANGAMEID[password]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]FAIL[HANGAME]");
                        return;
                    }
                }else{
                    ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, peony.Messages.STRING_00717);
                    log.info("[ACCLOGIN]NAME[" + name + "]HANGAMEID[password]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]FAIL[HANGAME]");
                    return;
                }
            } catch (Exception ex) {
                ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, peony.Messages.STRING_00717);
                log.info("[ACCLOGIN]NAME[" + name + "]HANGAMEID[password]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]FAIL[HANGAME]");
                return;
            } finally {
                method.releaseConnection();
            }
            
		    //检测帐号是否已经存在，如没有则创建新帐号，hangame用ownerid当帐号名注册(password)
	        method = new PostMethod(checkAccountUrl);
	        method.getParams().setContentCharset("utf-8");
	        method.addRequestHeader("Connection", "close");
	        method.setParameter("name", password);
	        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
	        try {
	            HttpClient httpclient = new HttpClient();
	            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
	            httpclient.getParams().setSoTimeout(30000);
	            int code = httpclient.executeMethod(method);
	            if(code==200){
	                BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));
	                String result = reader.readLine();
                    if(result.contains("1")){
                        //没有帐户，直接新建帐号
                        //hangame用平台的ownerId当作帐号名，客户端发上来的name记在phone字段里备用
						String version2 = version;
						if (session.getClientIP() != null && session.getClientIP().length() > 0) {
				            version2 += "/" + session.getClientIP();
				        }
                        AccountRegMessage regMsg = new AccountRegMessage(password, "", "", -1, model, Server.server.gameCode, version2, realPhone, "123");
                        Server.server.getServiceRegistry().getAccountService().postMessage(regMsg);
                        
                        Thread.sleep(100);
                    }
	            }else{
	                ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, peony.Messages.STRING_00717);
	                log.info("[ACCLOGIN]NAME[" + name + "]HANGAMEID[password]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]FAIL[HANGAME]");
	                return;
	            }
	        } catch (Exception ex) {
	            ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, peony.Messages.STRING_00717);
                log.info("[ACCLOGIN]NAME[" + name + "]HANGAMEID[password]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]FAIL[HANGAME]");
                return;
	        } finally {
	            method.releaseConnection();
	        }
	        
	        LegacyLoginMessage message = new LegacyLoginMessage(password, "123", realPhone);
	        Server.server.getServiceRegistry().getAccountService().sendAndRegister(message, this);
        } else {
			LegacyLoginMessage message = new LegacyLoginMessage(name, password,
					realPhone, Server.server.usePartitionBalance ? Server.server.gameCode : "",version+"/"+session.getClientIP(),model);
			Server.server.getServiceRegistry().getAccountService()
					.sendAndRegister(message, this);
		}
    }

    /**
    * 从取得的手机号信息文本中解析出手机号。
    * @param info <?xml version="1.0" encoding="utf-8" ?><config mobile-no="13910564907"></config>
    * @return 手机号
    */
    public static String cutPhone(String info){
        String search = "mobile-no=\"";
        int pos = info.indexOf(search);
        if(pos == -1){
            return "";
        }
        pos += search.length();
        int pos2 = info.indexOf('"', pos);
        if(pos2 == -1){
            return "";
        }
        return info.substring(pos, pos2);
    }
    
    public static String cutSis(String url){
    	int i = url.lastIndexOf('/');
    	return url.substring(i);
    }
}
