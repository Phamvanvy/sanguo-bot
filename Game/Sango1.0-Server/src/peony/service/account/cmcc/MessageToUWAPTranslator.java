package peony.service.account.cmcc;

import com.pip.net.IMessage;
import com.pip.net.message.gameaccount.AccountInfoMessage;
import com.pip.net.message.gameaccount.GameAccountMessageType;
import com.pip.net.message.gameaccount.Logout1Message;
import com.pip.net.uwap2.mina.UWAPSegment;

public class MessageToUWAPTranslator {
	
	public UWAPSegment translate(IMessage message) {
		switch (message.getCmd()) {
			case GameAccountMessageType.ACCOUNT_REG:
				return accountReg((CmccAccountRegMessage) message);
			case GameAccountMessageType.LEGACY_LOGIN:
				return accountLogin((CmccAccountLoginMessage) message);
			case GameAccountMessageType.LEGACY_CHARGEUP:
				return chargeUp((CmccChargeUpMessage) message);
			case GameAccountMessageType.LOGOUT1:
				return accountLogout((Logout1Message) message);
			case GameAccountMessageType.RENAME:
				return accountRename((CmccAccountRenameMessage) message);
			case GameAccountMessageType.MODIFY_PASSWORD:
				return accountModifyPassword((CmccModifyPasswordMessage) message);
			case GameAccountMessageType.LEGACY_BUY1:
				return ibuy((CmccIBuyMessage) message);
			case 601:
				return getHistory((CmccHistoryMessage)message);
			case GameAccountMessageType.LEGACY_QUICKREG:
				return quickReg((CmccAccountQuickRegMessage)message);
			case GameAccountMessageType.ACCOUNT_INFO:
				return adminAccountInfo((AccountInfoMessage)message);
			case CmccMessageType.CHECK_DOWNLOAD:
				return checkDownload((CmccCheckDownloadMessage)message);
			case CmccMessageType.DOWNLOADOK:
				return downloadOk((CmccDownloadOkMessage)message);
			case CmccMessageType.ANDROIDBUYREQ:
				return getCmccAndroidSmsBuyReq((CmccAndroidSmsBuyReqMessage)message);
			case CmccMessageType.CMCC_BUY2:
				return getCmccBuy2((CmccBuy2Message)message);
			default:
				return null;
		}
	}
	
	/**
     * 请求购买商品(扣i币)
     * accountId		int				帐号ID
     * cost				int				价格(单位1/100i)
     * requestId		int				请求ID
     */
    public static final byte BUY2 = (byte)231;
    
    protected UWAPSegment getCmccBuy2(CmccBuy2Message message){
    	UWAPSegment seg = new UWAPSegment(BUY2, message.getSerial());
    	seg.writeInt(message.getAccountId());
    	seg.writeInt(message.getCost());
    	seg.writeInt(message.getRequestId());
    	return seg;
    }
	
	/**
     * 卓望Android版本申请短信购买。
     * requestId        int             请求ID
     * accountId        int             帐号ID
     * playerId         int             请求购买的玩家ID
     * consumeCode      String          计费代码(卓望版本才有)
     * itemId			int				物品ID
     */
    public static final byte CMCC_ANDROID_SMS_BUY_REQ = (byte)228;
    
    protected UWAPSegment getCmccAndroidSmsBuyReq(CmccAndroidSmsBuyReqMessage message){
    	UWAPSegment seg = new UWAPSegment(CMCC_ANDROID_SMS_BUY_REQ, message.getSerial());
    	seg.writeInt(message.getRequestId());
    	seg.writeInt(message.getAccountId());
    	seg.writeInt(message.getPlayerId());
    	seg.writeString(message.getConsumeCode());
    	seg.writeInt(message.getItemId());
    	seg.writeString(message.getVersion());
    	return seg;
    }
	
    /**
     * 通知用户已经下载一次客户端成功，以后不需要再下载了。
     * userId			String			用户ID
     */
    public static final byte CMCC_DOWNLOAD_OK = (byte)227;
    
    protected UWAPSegment downloadOk(CmccDownloadOkMessage message){
    	UWAPSegment seg = new UWAPSegment(CMCC_DOWNLOAD_OK,message.getSerial());
    	seg.writeString(message.getUserId());
    	return seg;
    }
	
    /**
     * 查询用户是否通过卓望平台下载过客户端。
     * userId			String			用户ID
     * accountId		int				请求帐号ID
     * playerId			int 			请求角色ID
     * jvmcode			String			客户端Java机型代码
     */
    public static final byte CMCC_CHECK_DOWNLOAD = (byte)226;
    
    protected UWAPSegment checkDownload(CmccCheckDownloadMessage message){
    	UWAPSegment seg = new UWAPSegment(CMCC_CHECK_DOWNLOAD,message.getSerial());
    	seg.writeString(message.getUserId());
    	seg.writeInt(message.getAccountId());
    	seg.writeInt(message.getPlayerId());
    	seg.writeString(message.getJvmcode());
    	return seg;
    }
	
    public static final byte ACCOUNT_REG = 1;
	protected UWAPSegment accountReg(CmccAccountRegMessage message){
		UWAPSegment seg = new UWAPSegment(ACCOUNT_REG,message.getSerial());
		seg.writeInt(message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getPhone());
		seg.writeString("");
		seg.writeInt(-1);
		seg.writeString(message.getModel());
		seg.writeString(message.getVersion());
		seg.writeStrings(new String[]{});
		seg.writeString("");
		seg.writeBoolean(true);
		seg.writeString(message.getCmccUserId());
		seg.writeString(message.cmccUserKey);
		seg.writeString(message.getService());
		seg.writeString(message.getRealPhone());
		seg.writeString(message.getInitPassword());
		return seg;
	}
	
    /**
     * 用户登录
     * requestId		int				请求ID
     * name				String			帐号名称
     * password			String			密码
     * cmccUserId		String			平台用户ID(卓望版本才有)
     * cmccKey			String			平台用户Key(卓望版本才有)
     * realPhone        String          实际手机号（可空）
     */
    public static final byte LOGIN = 77;
    protected UWAPSegment accountLogin(CmccAccountLoginMessage message){
    	UWAPSegment seg = new UWAPSegment(LOGIN, message.getSerial());
    	seg.writeInt(message.getSerial());
    	seg.writeString(message.getName());
    	seg.writeString(message.getPassword());
    	seg.writeString(message.getCmccUserId());
    	seg.writeString(message.cmccUserKey);
    	seg.writeString(message.getPhone());
    	return seg;
    }
    
    /**
     * 卓望版本游戏外充值
     * cmccUserId		String			平台用户ID
     * cmccKey			String			平台用户Key
     * amount			int				充值金额(元)
     * id				int				充值请求ID
     * amount			int				充值金额(分)
     */
    public static final byte CMCC_CHARGE = 13;
    protected UWAPSegment chargeUp(CmccChargeUpMessage message){
    	UWAPSegment seg = new UWAPSegment(CMCC_CHARGE, message.getSerial());
    	seg.writeString(message.getCmccUserId());
    	seg.writeString(message.getCmccUserKey());
    	seg.writeInt(message.getAmount());
    	seg.writeInt(message.getSerial());
    	seg.writeInt(message.getAmount());
    	return seg;
    }
    
    /**
     * 用户登出通知
     * accountId		int				帐号ID
     */
    public static final byte PLAYER_LOGOUT = (byte)188;
    
    protected UWAPSegment accountLogout(Logout1Message message){
    	UWAPSegment seg = new UWAPSegment(PLAYER_LOGOUT);
    	seg.writeInt(message.getAccountId());
    	return seg;
    }
    
    /**
     * 修改帐号名称
     * accountId        int             帐号ID
     * playerId         int             修改帐号名称的角色ID
     * name             String          新名字
     */
    public static final byte MODIFY_ACCOUNT_NAME = (byte)214;
    
    protected UWAPSegment accountRename(CmccAccountRenameMessage message){
    	UWAPSegment seg = new UWAPSegment(MODIFY_ACCOUNT_NAME);
    	seg.writeInt(message.getAccountId());
    	seg.writeInt(message.getPlayerId());
    	seg.writeString(message.getNewName());
    	return seg;
    }
    
    /**
     * 请求修改密码
     * accountId		int				帐号ID
     * playerId			int				请求修改密码的角色ID
     * old				String			旧密码
     * new1				String			新密码
     * new2				String			重复新密码
     */
    public static final byte MODIFY_PASSWORD = (byte)204;
    
    protected UWAPSegment accountModifyPassword(CmccModifyPasswordMessage message){
    	UWAPSegment seg = new UWAPSegment(MODIFY_PASSWORD);
    	seg.writeInt(message.getAccountId());
    	seg.writeInt(message.getPlayerId());
    	seg.writeString(message.getOldPassword());
    	seg.writeString(message.getPassword());
    	seg.writeString(message.getPassword());
    	return seg;
    }
    
    /**
     * 请求购买商品(扣费)
     * accountId		int				帐号ID，<0表示PIP版本访问卓望认证
     * cost				int				价格(单位1/100i)(pip版本才有)
     * consumeCode		String			计费代码(卓望版本才有)
     * requestId		int				请求ID
     * version          String          客户端版本号，格式为：2.2-CPIP1000-xxxxxxx
     * cmccUserId       String          卓望平台用户ID，当accountId<0时传入
     * count			int				购买数量(可选)
     */
    public static final byte BUY = (byte)207;
    
    protected UWAPSegment ibuy(CmccIBuyMessage message){
    	UWAPSegment seg = new UWAPSegment(BUY);
    	seg.writeInt(message.getAccountId());
    	seg.writeString(message.getConsumeCode());
    	seg.writeInt(message.getSerial());
    	seg.writeString(message.getVersion());
    	seg.writeString(message.cmccUserId);
    	seg.writeInt(message.getCount());
    	return seg;
    }
    
    /**
     * 快速注册
     * requestId		int				请求ID
     * phone			String			手机号
     * version			String			版本号(格式为:x.x.x-渠道代码)
     * model			String			机型(格式为:软件机型/JVM版本)
     * cmccUserId		String			平台用户ID(卓望版本才有)
     * cmccKey			String			平台用户Key(卓望版本才有)
     * gameCode			String			游戏区代码
     * realPhone        String          实际手机号（可空）
     */
    public static final byte QUICK_REG = 30;
    
    protected UWAPSegment quickReg(CmccAccountQuickRegMessage message){
    	UWAPSegment seg = new UWAPSegment(QUICK_REG);
    	seg.writeInt(message.getSerial());
    	seg.writeString(message.getPhone());
    	seg.writeString(message.getVersion());
    	seg.writeString(message.getModel());
    	seg.writeString(message.getCmccUserId());
    	seg.writeString(message.getCmccUserKey());
    	seg.writeString(message.getServiceId());
    	seg.writeString(message.getRealPhone());
    	return seg;
    }
    
    
    /**
     * 卓望版本查询充值/消费历史
     * requestId		int				请求ID
     * type				byte			1消费历史，2充值历史
     * accountId		int				帐号ID，如为-1表示可忽略此参数
     * startDate        String          起始日期
     * endDate          String          结束日期
     * startSeq         int             起始记录号，1表示第一条
     * pageSize         int             每页数据条数
     * timeType         int             查询时间类型，可选，0 - 当日，1 - 指定月，2 - 10天内
     * queryType        int             查询类型，可选，充值历史：0 - 全部；消费历史：0 - 查询所有客户端网游，1 - 查询所有WAP网游，2 - 查询自己
     * cmccUserId       String          卓望平台用户ID（如果accountId不为-1，此条可选）
     */
    public static final byte CMCC_GET_HISTORY = (byte)217;
    
    protected UWAPSegment getHistory(CmccHistoryMessage message){
    	UWAPSegment seg = new UWAPSegment(CMCC_GET_HISTORY);
    	seg.writeInt(message.getSerial());
    	seg.write((byte)message.getType());
    	seg.writeInt(-1);
    	seg.writeString(message.getStartDate());
    	seg.writeString(message.getEndDate());
    	seg.writeInt(message.getStartSeq());
    	seg.writeInt(message.getPageSize());
    	seg.writeInt(message.getTimeType());
    	seg.writeInt(message.getQueryType());
    	seg.writeString(message.getCmccUserId());
    	return seg;
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
    
    protected UWAPSegment adminAccountInfo(AccountInfoMessage message){
    	UWAPSegment seg = new UWAPSegment(ADMIN_ACCOUNTINFO,message.getSerial());
    	seg.writeInt(message.getAccountId());
    	seg.writeString("");
    	return seg;
    }
    
}
