package peony.service.account;

import org.apache.log4j.Logger;

import peony.db.DBService;
import peony.db.PlayerLoadCall;
import peony.game.ErrorHandler;
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
    protected String version;
    protected int serial;
    protected Version v;
    protected String realPhone;
    protected int playerID;
    protected QmeAccount qmeAcc;
    protected String cmccUserId,cmccUserKey;
    protected String MIEI = "";

    public AccountLoginCall(ClientSession session, String name, String password, String model, String version, int serial, String realPhone, int playerID, String MIEI){
        super(session);
        this.name = name;
        this.password = password;
        this.model = model;
        this.version = version;
        this.serial = serial;
        this.realPhone = cutPhone(realPhone);
        this.playerID = playerID;
        this.MIEI = MIEI;
        if(!MIEI.equals("")){
        	log.info("[ACCLOGIN]NAME[" + name + "]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]MIEI["+MIEI+"TRY");
        }else{
        	log.info("[ACCLOGIN]NAME[" + name + "]MODEL[" + model + "]VERSION[" + version + "]PHONE[" + this.realPhone + "]IP[" + session.getClientIP() + "]TRY");
        }
    }

    protected String cutVersion(String value){
        String[] s = value.split("-");
        return s[0];
    }
    
    protected String cutChannel(String value){
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
        	if ("TAIWAN".equals(Server.server.revision)) {
        		// 台湾版本，第一次登录可能因为是首次登录而失败，向认证发送注册请求，所以这里也要处理注册成功的情况，再重新发送一次登录请求
        		if (message instanceof AccountRegOkMessage) {
        			LegacyLoginMessage loginMsg = new LegacyLoginMessage(name, "123", realPhone);
        	        Server.server.getServiceRegistry().getAccountService().sendAndRegister(loginMsg, this);
        	        return;
        		}
        	}
            LegacyLoginOkMessage msg = (LegacyLoginOkMessage) message;
            Account account = new Account(msg.getAccountId(), msg.getName(), msg.getKey());
            account.setIMoney(msg.getIMoney());
            log.info("[ACCLOGIN]NAME[" + name + "]IMONEY[" + msg.getIMoney() + "]OK");
            account.setLoginErrorTimes(msg.getLoginErrorTimes());
            account.setModifiedNameTimes(msg.getModifiedNameTimes());
            account.setMonth(msg.isMonth());
            account.setPhone(msg.getPhone());
            account.setPurchasedCodes(msg.getPurchasedCodes());
            account.setVersion(v);
            account.setModel(cutModel(model));
            account.setJvmCode(cutJvmCode(model));
            account.setChannel(cutChannel(version));
            account.setPassword(password);
            account.setRealPhone(realPhone);
            account.setCmccUserId(cmccUserId);
            account.setCmccUserKey(cmccUserKey);
            if(msg instanceof CmccLoginOkMessage){
            	account.setCity(((CmccLoginOkMessage)msg).getCityName());
            	log.info("NAME[" + name + "]SET CITY OK");
            }
            if ("TAIWAN".equals(Server.server.revision)) {
            	account.setMetaInfo(qmeAcc);
            }
            try{
                session.authenticate(account);
                Server.server.getServiceRegistry().getAccountService().registerClientSession(session);
                Packet pt = new Packet(OpCode.ACCOUNT_LOGIN_SERVER);
                pt.putInt(serial);
                pt.putInt(account.getId());
                pt.putString(account.getName());
                pt.putInt(account.getIMoney() / 100);
                pt.putInt(account.getModifiedNameTimes());
                if("TAIWAN".equals(Server.server.revision)){
                	pt.putString(qmeAcc.tsi);
                }
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
            if ("TAIWAN".equals(Server.server.revision)) {
            	// 台湾版本，如果登录是发现帐号不存在，则发送注册请求
	            if (msg.getCode() == ErrorMessages.UNKNOW_ACCOUNT || msg.getCode() == ErrorMessages.ERROR_NAMEORPASSWORD) {
        			this.setSuccess(true);
	    			AccountRegMessage regMsg = new AccountRegMessage(name,"","",-1,model,Server.server.gameCode,version,realPhone,"123");
	    			Server.server.getServiceRegistry().getAccountService().sendAndRegister(regMsg,this);
	    			return;
	            }
            }
            ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, ErrorMessages.getErrorMesssage(msg));
        }
    }

    public void run(){
    	if(ObjectAccessor.players.size()>=Server.server.maxPlayer){
            ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, "已經到達最大登錄數量");
            return;
    	}
    	if (!session.checkOnlineCount(0)) {
            ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, "已經到達最大登錄數量");
            return;
        }
        v = Server.server.getServiceRegistry().getVersionService().getVersion(cutVersion(version));
        if(v == null || v.status == Version.STATUS_OBSOLETE){
            ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, v == null? "版本號錯誤": v.message);
            if(v != null&&v.url != null){
            	Packet pt = new Packet(OpCode.SYMBIAN_UPDATE_URL_SERVER);
            	pt.putString(v.url);
            	pt.putString(cutSis(v.url));
            	session.send(pt);
            }
            return;
        }
        if ("TAIWAN".equals(Server.server.revision)) {
        	try {
        		qmeAcc = QmeAdapter.login(name, password);
        		if (qmeAcc.smsCode != null) {
        			ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, qmeAcc.smsCode);
                    return;
        		}
        	} catch (QmeException e) {
        		ErrorHandler.sendErrorMessage(session, serial, OpCode.ACCOUNT_LOGIN_CLIENT, e.getMessage());
                return;
        	}
        	
        	// 首先尝试登录，登录可能会返回UNKNOWN_ACCOUNT错误
        	LegacyLoginMessage message = new LegacyLoginMessage(name, "123", realPhone);
	        Server.server.getServiceRegistry().getAccountService().sendAndRegister(message, this);
        } 
        else if ("CMCC".equals(Server.server.revision)||"CHINATEL".equals(Server.server.revision)) {
        	CmccAccountLoginMessage message = new CmccAccountLoginMessage(name, password, realPhone, cmccUserId, cmccUserKey);
        	Server.server.getServiceRegistry().getAccountService().sendAndRegister(message, this);
		} else {
			LegacyLoginMessage message = new LegacyLoginMessage(name, password,
					realPhone);
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
