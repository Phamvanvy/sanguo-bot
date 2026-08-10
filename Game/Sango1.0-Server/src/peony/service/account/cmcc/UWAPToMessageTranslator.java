package peony.service.account.cmcc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import peony.service.account.ErrorMessages;

import com.pip.net.IMessage;
import com.pip.net.message.gameaccount.AccountInfoOkMessage;
import com.pip.net.message.gameaccount.AccountRegOkMessage;
import com.pip.net.message.gameaccount.ForceLogoutMessage;
import com.pip.net.message.gameaccount.LegacyBuyResultMessage;
import com.pip.net.message.gameaccount.LegacyQuickRegResultMessage;
import com.pip.net.uwap2.mina.UWAPData;

public class UWAPToMessageTranslator {
	private static Logger log = Logger.getLogger(UWAPToMessageTranslator.class);
	
	public IMessage translate(UWAPData data) throws Exception{
		switch(data.getAppType()){
			case ERROR:
				return error(data);
			case ACCOUNT_REG_OK:
				return accountRegOk(data);
			case LOGIN_OK:
				return accountLoginOk(data);
			case LOGIN_RESULT:
				return loginResult(data);
			case CHARGEUP_RESULT:
				return chargeResult(data);
			case MODIFY_PASSWORD_RESULT:
				return modifyPasswordResult(data);
			case BUY_RESULT:
				return ibuyResult(data);
			case MODIFY_ACCOUNT_NAME_RESULT:
				return modifyNameResult(data);
			case QUICK_REG_OK:
				return quickRegOk(data);
			case FORCELOGOUT:
				return forceLogout(data);
			case CMCC_GET_HISTORY_OK:
				return getHistoryOk(data);
			case ADMIN_ACCOUNTINFO:
				return getAdminAccountInfo(data);
			case CMCC_PUSH_DOWNLOAD:
				return cmccPushDownload(data);
			case CMCC_ANDROID_SMS_BUY_REQ_RESULT:
				return getCmccAndroidSmsBuyReqResult(data);
			case CMCC_ANDROID_SMS_BUY_SUCC:
				return getCmccAndroidSmsBuyReqOk(data);
			case BUY2_RESULT:
				return getCmccBuy2Result(data);
			default:
				return null;
		}
	}
	
	/**
     * 购买商品结果
     * requestId		int				请求ID
     * result			boolean			购买结果，true成功，false失败
     * balance			long			账户余额(单位1/100i)
     * cost				int				消耗i币(单位1/100i)
     * msg				String			如果失败，返回错误信息
     */
    public static final byte BUY2_RESULT = (byte)232;
    
    protected IMessage getCmccBuy2Result(UWAPData data) throws Exception {
    	int requestId = data.readInt();
    	boolean result = data.readBoolean();
    	long balance = data.readLong();
    	int cost = data.readInt();
    	String msg = data.readString();
    	return new CmccBuy2ResultMessage(requestId, requestId, result, balance, cost, msg);
    }
	
    /** 通知世界服务器用户需要通过卓望平台下载客户端。
    * userId			String			用户ID
    * accountId		int				帐号ID
    * playerId			int				角色ID
    * url				String			下载地址
    */
	public static final byte CMCC_PUSH_DOWNLOAD = (byte)226;
	
	protected IMessage cmccPushDownload(UWAPData data) throws Exception{
		return new CmccPushDownloadMessage(data.getSerial(), data.readString(), data.readInt(), data.readInt(), data.readString());
	}
	
	/**
	 * 通用错误
	 * appType			byte			错误包类型
	 * msg				String			错误信息
	 * 注：包序列号(sessionId)用于返回请求ID
	 */
    public static final byte ERROR = -1;
    
    protected IMessage error(UWAPData data) throws Exception{
    	byte type = data.readByte();
    	String msg = data.readString();
    	CmccErrorMessage message = new CmccErrorMessage(data.getSessionId(),ErrorMessages.UNKNOW,msg);
    	return message;
    }
    
    /**
     * 注册帐号成功
     * requestId		int				请求ID
     * phone			String			手机号
     * password			String			密码(自动生成)
     * needReturn		boolean			是否直接激活
     * accountID        int             帐号ID
     */
    public static final byte ACCOUNT_REG_OK = 2;
    
    protected IMessage accountRegOk(UWAPData data) throws Exception{
    	int serial = data.readInt();
    	String phone = data.readString();
    	String password = data.readString();
    	data.readBoolean();
    	int accountId = data.readInt();
    	AccountRegOkMessage message = new AccountRegOkMessage(serial,accountId,"",password);
    	return message;
    }
    
    /**
     * 用户登录成功
     * requestId		int				请求ID
     * id				int				帐号ID
     * name				String			帐号名称
     * password			String			密码
     * phone			String			手机号
     * mptimes			int				已修改密码次数
     * imoney			int				剩余i币(单位1/100i)/点数(单位1/100点)
     * reachFeeLimit	boolean			是否计时费用已达到月上限
     * subscribed		boolean			是否包月用户
     * errorTime		int				登录失败次数
     * region           String          用户所属地区（卓望版本才有）
     * balance			long			卓望版本，返回账户剩余i币(单位1/100i)
     */
    public static final byte LOGIN_OK = 78;
    
    protected IMessage accountLoginOk(UWAPData data) throws Exception{
    	int serial = data.readInt();
    	int accountId = data.readInt();
    	String name = data.readString();
    	String password = data.readString();
    	String phone = data.readString();
    	int mpTimes = data.readInt();
    	int imoney = data.readInt();
    	data.readBoolean();
    	data.readBoolean();
    	data.readInt();
    	String cityName = data.readString();
    	long balance = data.readLong();
    	String attr = data.readString();
    	log.info("ACC[" + accountId + "]CITY[" + cityName + "]ATTR[" + attr + "]");
    	CmccLoginOkMessage message = new CmccLoginOkMessage(serial,accountId,name,"",phone,mpTimes,imoney,false,false,0,new int[]{},cityName,balance,attr, 0);
    	return message;
    }
    
    /**
     * 用户登录失败
     * requestId		int				请求ID
     * cause			String			错误信息
     */
    public static final byte LOGIN_RESULT = (byte)213;
    
    protected IMessage loginResult(UWAPData data) throws Exception{
    	int serial = data.readInt();
    	String msg = data.readString();
    	CmccErrorMessage message = new CmccErrorMessage(serial,ErrorMessages.UNKNOW,msg);
    	return message;
    }
    
    /**
     * 卓望版本充值结果
     * id				int				充值请求ID
     * result			boolean			充值结果
     * balance			int				余额(单位1/100点)
     * msg				String			充值成功/失败消息
     */
    public static final byte CHARGEUP_RESULT = (byte)210;
    
    protected IMessage chargeResult(UWAPData data) throws Exception{
    	int serial = data.readInt();
    	boolean result = data.readBoolean();
    	int balance = data.readInt();
    	String msg = data.readString();
    	if(result){
    		CmccChargeOkMessage message = new CmccChargeOkMessage(serial, balance,msg);
    		return message;
    	}else{
    		CmccErrorMessage message = new CmccErrorMessage(serial,ErrorMessages.UNKNOW, msg);
    		return message;
    	}
    }
    
    /**
     * 修改密码结果
     * result			byte			0成功，1失败
     * playerId			int				请求修改密码的角色ID
     * msg				String			新密码(成功)/错误信息(失败)
     */
    public static final byte MODIFY_PASSWORD_RESULT = (byte)204;
    
    protected IMessage modifyPasswordResult(UWAPData data) throws Exception{
    	byte result = data.readByte();
    	int playerId = data.readInt();
    	String msg= data.readString();
    	return new CmccModifyPasswordResultMessage(data.getSerial(), result==0?true:false, playerId, msg);
    }
    
    /**
     * 购买商品结果
     * requestId		int				请求ID
     * result			boolean			购买结果，true成功，false失败
     * balance			int				账户余额(单位1/100点)
     * cost				int				消耗i币(单位1/100i)(卓望版本总是-1)
     * msg				String			如果失败，返回错误信息
     * balance2			long			账户余额(单位1/100i)
     */
    public static final byte BUY_RESULT = (byte)207;
    
    protected IMessage ibuyResult(UWAPData data) throws Exception{
    	int serial = data.readInt();
    	boolean result = data.readBoolean();
    	int balance = data.readInt();
    	int cost = data.readInt();
    	String msg = data.readString();
    	long balance2 = 0L;
    	try{balance2 = data.readLong();}catch(Exception e){}
    	return new SubLegacyBuyResultMessage(serial,result,balance,cost,msg,(long)balance,balance2);
    }
    
    /**
     * 修改帐号名称结果
     * result           byte            0成功，1失败
     * playerId         int             请求修改密码的角色ID
     * msg              String          新名称(成功)/错误信息(失败)
     */
    public static final byte MODIFY_ACCOUNT_NAME_RESULT = (byte)214;
    
    protected IMessage modifyNameResult(UWAPData data) throws Exception{
    	int result = data.readByte();
    	int playerId = data.readInt();
    	String msg = data.readString();
    	return new CmccRenameResultMessage(data.getSerial(), result==0?true:false, playerId , msg);
    }
    
    /**
     * 快速注册成功
     * requestId		int				请求ID
     * id				int				帐号ID
     * name				String			帐号名称
     * password			String			密码
     * playerName		String			角色名称(废弃)
     * isNew			byte			0表示新创建，1表示找到旧帐号
     */
    public static final byte QUICK_REG_OK = 30;
    
    protected IMessage quickRegOk(UWAPData data) throws Exception{
    	int serial = data.readInt();
    	int accountId = data.readInt();
    	String name = data.readString();
    	String password = data.readString();
    	data.readString();
    	byte type = data.readByte();
    	LegacyQuickRegResultMessage message = new LegacyQuickRegResultMessage(serial,accountId,name,password,type);
    	return message;
    }
    
    /**
     * 通知世界服务器强制用户下线
     * id				int				帐号ID
     */
    public static final byte FORCELOGOUT = (byte)199;
    
    protected IMessage forceLogout(UWAPData data) throws Exception{
    	return new ForceLogoutMessage(data.getSerial(), data.readInt(), "", "");
    }
    
    /**
     * 卓望版本返回充值/消费历史
     * requestId		int				请求ID
     * count			int				返回记录数量
     * 循环N次
     *   point			int				点数(单位1点)
     *   info			String			充值/消费信息
     */
    public static final byte CMCC_GET_HISTORY_OK = (byte)217;
    protected IMessage getHistoryOk(UWAPData data) throws Exception{
    	int serial = data.readInt();
    	int count = data.readInt();
    	List<CmccHistory> l = new ArrayList<CmccHistory>(count);
    	for(int i=0;i<count;i++){
    		int point = data.readInt();
    		String info = data.readString();
    		CmccHistory h = new CmccHistory(point, info);
    		l.add(h);
    	}
    	return new CmccHistoryOkMessage(serial,l);
    }
    
    /**
     * 卓望版本Android申请短信购买结果。
     * requestId        int             请求ID
     * accountId        int             帐号ID
     * playerId         int             玩家ID
     * result           boolean         true成功，false失败
     * sms				String			短信内容/如果出错，此字段是错误信息
     */
    public static final byte CMCC_ANDROID_SMS_BUY_REQ_RESULT = (byte)229;
    protected IMessage getCmccAndroidSmsBuyReqResult(UWAPData data) throws Exception{
    	int requestId = data.readInt();
    	int accountId = data.readInt();
    	int playerId = data.readInt();
    	boolean result = data.readBoolean();
    	String sms = data.readString();
    	CmccAndroidSmsBuyReqResultMessage message = new CmccAndroidSmsBuyReqResultMessage(data.getSerial(), requestId, 
    			accountId, playerId, result, sms);
    	return message;
    }

    /**
     * 卓望版本Android短信购买商品成功。
     * requestId        int             请求ID（CMCC_ANDROID_SMS_BUY_REQ传入）
     * accountId        int             帐号ID
     * playerId         int             玩家ID
     * consumeCode		String			计费代码
     * itemId			int				物品ID
     */
    public static final byte CMCC_ANDROID_SMS_BUY_SUCC = (byte)230;
    protected IMessage getCmccAndroidSmsBuyReqOk(UWAPData data) throws Exception{
    	int requestId = data.readInt();
    	int accountId = data.readInt();
    	int playerId = data.readInt();
    	String consumeCode = data.readString();
    	int itemId = data.readInt();
    	CmccAndroidSmsBuyReqOkMessage message = new CmccAndroidSmsBuyReqOkMessage(data.getSerial(), requestId, 
    			accountId, playerId, consumeCode, itemId);
    	return message;
    }
    
    /**
     * GM工具查询帐号信息（返回信息也用这个）
     * accountId		int				帐号ID
     * accountName		String			帐号ID传-1时用于指定帐号名称
     * 返回：
     * accountId		int				帐号ID
     * accountName		String			帐号名称
     * password			String			密码
     * phone			String			注册手机号				
     */
    public static final byte ADMIN_ACCOUNTINFO = (byte)247;
    protected IMessage getAdminAccountInfo(UWAPData data) throws Exception{
    	AccountInfoOkMessage message = new AccountInfoOkMessage(data.getSerial(), data.readInt(), data.readString(), data.readString(), data.readString());
    	return message;
    }
}
