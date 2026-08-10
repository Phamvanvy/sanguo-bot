package com.pip.net.message.gameaccount;

import org.apache.log4j.Logger;

import com.pip.net.IMessage;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.ServerLoginMessage;
import com.pip.net.message.ServerLoginOkMessage;
import com.pip.net.message.ServerMessageType;
import com.pip.net.uwap2.mina.IMessageDecoder;
import com.pip.net.uwap2.mina.UWAPData;

public class MessageDecoder implements IMessageDecoder {
	
	protected static final Logger log = Logger.getLogger(MessageDecoder.class);

	public IMessage decode(UWAPData data) throws Exception {
		short type = data.getAppType();
		log.debug("receive message type:"+type);
		switch (type) {
		case ServerMessageType.ERROR:
			return getErrorMessage(data);
		case ServerMessageType.LOGIN:
			return getServerLoginMessage(data);
		case ServerMessageType.LOGINOK:
			return getServerLoginOkMessage(data);
		case GameAccountMessageType.ACCOUNT_REG:
			return getAccountRegMessage(data);
		case GameAccountMessageType.ACCOUNT_REG_OK:
			return getAccountRegOkMessage(data);
		case GameAccountMessageType.LEGACY_LOGIN:
			return getLegacyLoginMessage(data);
		case GameAccountMessageType._LEGACY_LOGIN_OK:
			return get_LegacyLoginOkMessage(data);
		case GameAccountMessageType.FORCE_LOGOUT:
			return getForceLogoutMessage(data);
		case GameAccountMessageType.LEGACY_FEE:
			return getLegacyFeeMessage(data);
		case GameAccountMessageType.BUY:
			return get_ModifyIMoneyMessage(data);
		case GameAccountMessageType.BUY_OK:
			return get_ModifyIMoneyOkMessage(data);
		case GameAccountMessageType.LEGACY_BUY:
			return getLegacyBuyMessage(data);
		case GameAccountMessageType.LEGACY_BUY_RESULT:
			return getLegacyBuyResultMessage(data);
		case GameAccountMessageType.LEGACY_LOGIN_OK:
			return getLegacyLoginOkMessage(data);
		case GameAccountMessageType.LEGACY_BUY1:
			return getLegacyBuy1Message(data);
		case GameAccountMessageType.LEGACY_QUICKREG:
			return getLegacyQuickRegMessage(data);
		case GameAccountMessageType.LEGACY_QUICKREG_RESULT:
			return getLegacyQuickRegResultMessage(data);
		case GameAccountMessageType._LEGACY_QUICKREG_RESULT:
			return get_LegacyQuickRegResultMessage(data);
		case GameAccountMessageType.LOGOUT1:
			return getLogout1Message(data);
		case GameAccountMessageType._LOGOUT:
			return get_LogoutMessage(data);
		case GameAccountMessageType.LEGACY_FEE1:
			return getLegacyFee1Message(data);
		case GameAccountMessageType.SYNC_BALANCE:
			return getSyncBalanceMessage(data);
		case GameAccountMessageType.DEC_BALANCE:
			return get_DecIMoneyMessage(data);
		case GameAccountMessageType.DEC_BALANCE_OK:
			return get_DecIMoneyOkMessage(data);
		case GameAccountMessageType.MODIFY_PASSWORD:
			return getModifyPasswordMessage(data);
		case GameAccountMessageType.MODIFY_PASSWORD_OK:
			return getModifyPasswordResultMessage(data);
		case GameAccountMessageType.MODIFY_PHONE:
			return getModifyPhoneMessage(data);
		case GameAccountMessageType.MODIFY_PHONE_OK:
			return getModifyPhoneOkMessage(data);
		case GameAccountMessageType.ADD_RECOMMEND_BALANCE:
			return getAddRecommendBalanceMessage(data);
        case GameAccountMessageType.ADD_RECOMMEND_BALANCE_OK:
            return getAddRecommendBalanceOkMessage(data);
		case GameAccountMessageType.CHANGE_STATUS:
			return getChangeStatusMessage(data);
		case GameAccountMessageType._CHANGE_STATUS:
			return get_ChangeStatusMessage(data);
		case GameAccountMessageType.ACCOUNT_INFO:
			return getAccountInfoMessage(data);
		case GameAccountMessageType.ACCOUNT_INFO_OK:
			return getAccountInfoOkMessage(data);
		case GameAccountMessageType.GET_ACCOUNTNAME:
			return getGetAccountNameMessage(data);
		case GameAccountMessageType.GET_ACCOUNTNAME_OK:
			return getGetAccountNameOkMessage(data);
		case GameAccountMessageType.LOGIN:
			return getLoginMessage(data);
		case GameAccountMessageType.QQ_BILLING:
			return getQQBillingMessage(data);
		case GameAccountMessageType.RENAME:
			return getRenameMessage(data);
		case GameAccountMessageType.RENAME_OK:
			return getRenameOkMessage(data);
		case GameAccountMessageType.ONLINE_TIME_NOTIFY:
		    return getOnlineTimeNotifyMessage(data);
		case GameAccountMessageType.CREDIT_CHANGE_NOTIFY:
		    return getCreditChangeNotifyMessage(data);
		case GameAccountMessageType.RECOMMEND_REQUEST:
		    return getRecommendRequestMessage(data);
		case GameAccountMessageType.LEVEL_UP_NOTIFY:
		    return getLevelUpNotifyMessage(data);
		case GameAccountMessageType.RECOMMEND_REWARD_NOTIFY:
		    return getRecommendRewardNotifyMessage(data);
		case GameAccountMessageType.CREATE_IMONEY_CARD:
			return getCreateIMoneyCardMessage(data);
		case GameAccountMessageType.CREATE_IMONEY_CARD_OK:
			return getCreateIMoneyCardOkMessage(data);
		case GameAccountMessageType.USE_IMONEY_CARD:
			return getUseIMoneyCardMessage(data);
		case GameAccountMessageType.USE_IMONEY_CARD_OK:
			return getUseIMoneyCardOkMessage(data);
		case GameAccountMessageType.ADD_BALANCE:
			return getAddBalanceMessage(data);
        case GameAccountMessageType.ADD_BALANCE_OK:
            return getAddBalanceOkMessage(data);
        case GameAccountMessageType.PHONE_NOTIFY:
        	return getPhoneNotifyMessage(data);
        case GameAccountMessageType.BIND_ACCOUNT:
        	return getBindAccountMessage(data);
        case GameAccountMessageType.GET_BACK_ACCOUNT:
        	return getGetBackAccountMessage(data);
        case GameAccountMessageType.GET_BACK_ACCOUNT_OK:
        	return getGetBackAccountOkMessage(data);
		default: {
			log.info("UWAP[" + type + "]miss");
			return null;
		}
		}
	}
	
	protected GetBackAccountOkMessage getGetBackAccountOkMessage(UWAPData data) throws Exception {
		return new GetBackAccountOkMessage(data.getSerial(), data.readInt(), data.readString(), data.readString(), data.readString());
	}
	
	protected GetBackAccountMessage getGetBackAccountMessage(UWAPData data) throws Exception {
		return new GetBackAccountMessage(data.getSerial(), data.readInt(), data.readString());
	}
	
	protected BindAccountMessage getBindAccountMessage(UWAPData data) throws Exception {
		return new BindAccountMessage(data.getSerial(), data.readInt(), data.readInt(), data.readString());
	}
	
	protected PhoneNotifyMessage getPhoneNotifyMessage(UWAPData data) throws Exception{
		PhoneNotifyMessage msg = new PhoneNotifyMessage(data.readInt(), data.readString());
		return msg;
	}
	
	protected AddBalanceMessage getAddBalanceMessage(UWAPData data) throws Exception{
		int serial = data.getSerial();
		int accountID = data.readInt();
		int value = data.readInt();
		String reason = data.readString();
		String partition = "";
		int money = 0;
		try {
			partition = data.readString();
			money = data.readInt();
		} catch (Exception e) {
		}
		AddBalanceMessage msg = new AddBalanceMessage(serial, accountID, value, reason, partition, money);
		return msg;
	}

    protected AddBalanceOkMessage getAddBalanceOkMessage(UWAPData data) throws Exception{
        AddBalanceOkMessage msg = new AddBalanceOkMessage(data.getSerial(),data.readInt(),data.readInt());
        return msg;
    }
	
	public UseIMoneyCardOkMessage getUseIMoneyCardOkMessage(UWAPData data) throws Exception {
		return new UseIMoneyCardOkMessage(data.getSerial(), data.readInt(), data.readInt(), data.readInt(), data.readLong(), data.readLong());
	}

	public UseIMoneyCardMessage getUseIMoneyCardMessage(UWAPData data) throws Exception {
		int serial = data.getSerial();
		String gameCode = data.readString();
		int accountID = data.readInt();
		String key = data.readString();
		String cardno = data.readString();
		String password = data.readString();
		int amount = 0;
		try {
			amount = data.readInt();
		} catch (Exception e) {
		}
		return new UseIMoneyCardMessage(serial, gameCode, accountID, key, cardno, password, amount);
	}
	
	public CreateIMoneyCardOkMessage getCreateIMoneyCardOkMessage(UWAPData data) throws Exception {
		return new CreateIMoneyCardOkMessage(data.getSerial(), data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString(), data.readLong(),data.readLong());
	}
	
	public CreateIMoneyCardMessage getCreateIMoneyCardMessage(UWAPData data) throws Exception {
		return new CreateIMoneyCardMessage(data.getSerial(), data.readString(), data.readInt(), data.readString(), data.readInt());
	}
	
	public RecommendRequestMessage getRecommendRequestMessage(UWAPData data) throws Exception {
	    return new RecommendRequestMessage(data.readInt(), data.readString(), data.readString());
	}

    public LevelUpNotifyMessage getLevelUpNotifyMessage(UWAPData data) throws Exception {
        return new LevelUpNotifyMessage(data.readInt(), data.readInt(), data.readInt(), data.readString());
    }

    public RecommendRewardNotifyMessage getRecommendRewardNotifyMessage(UWAPData data) throws Exception {
        return new RecommendRewardNotifyMessage(data.readInt(), data.readInt(), data.readInt(), data.readString(),
                data.readInt(), data.readInt(), data.readInt());
    }

    public CreditChangeNotifyMessage getCreditChangeNotifyMessage(UWAPData data) throws Exception{
        return new CreditChangeNotifyMessage(data.readInt(), data.readInt());
    }

    public OnlineTimeNotifyMessage getOnlineTimeNotifyMessage(UWAPData data) throws Exception{
        return new OnlineTimeNotifyMessage(data.readInt(), data.readInt());
    }

    public RenameOkMessage getRenameOkMessage(UWAPData data) throws Exception{
		RenameOkMessage msg = new RenameOkMessage(data.getSerial());
		return msg;
	}
	
	public RenameMessage getRenameMessage(UWAPData data) throws Exception{
		RenameMessage msg = new RenameMessage(data.getSerial(),data.readString(),data.readString());
		return msg;
	}
	
	
	
	protected LoginMessage getLoginMessage(UWAPData data) throws Exception{
		LoginMessage msg = new LoginMessage(data.getSerial(),data.readString(),data.readString());
		return msg;
	}
	
	protected GetAccountNameMessage getGetAccountNameMessage(UWAPData data) throws Exception{
		GetAccountNameMessage msg = new GetAccountNameMessage(data.getSerial(),data.readInt());
		return msg;
	}
	
	protected GetAccountNameOkMessage getGetAccountNameOkMessage(UWAPData data) throws Exception{
		GetAccountNameOkMessage msg = new GetAccountNameOkMessage(data.getSerial(),data.readString());
		return msg;
	}
	
	protected AccountInfoMessage getAccountInfoMessage(UWAPData data) throws Exception{
		AccountInfoMessage msg = new AccountInfoMessage(data.getSerial(),data.readInt(),data.readString());
		return msg;
	}
	
	protected AccountInfoOkMessage getAccountInfoOkMessage(UWAPData data) throws Exception{
		AccountInfoOkMessage msg = new AccountInfoOkMessage(data.getSerial(),data.readInt(),data.readString(),data.readString(),data.readString());
		return msg;
	}
	
	protected _ChangeStatusMessage get_ChangeStatusMessage(UWAPData data) throws Exception{
		_ChangeStatusMessage msg = new _ChangeStatusMessage(data.getSerial(),data.readString(),data.readInt(),data.readString());
		return msg;
	}
	
	protected ChangeStatusMessage getChangeStatusMessage(UWAPData data) throws Exception{
		ChangeStatusMessage msg = new ChangeStatusMessage(data.getSerial(),data.readInt(),data.readInt(),data.readString());
		return msg;
	}
	
	protected AddRecommendBalanceMessage getAddRecommendBalanceMessage(UWAPData data) throws Exception{
		AddRecommendBalanceMessage msg = new AddRecommendBalanceMessage(data.getSerial(),data.readInt(),data.readInt(),data.readInt());
		return msg;
	}

    protected AddRecommendBalanceOkMessage getAddRecommendBalanceOkMessage(UWAPData data) throws Exception{
        AddRecommendBalanceOkMessage msg = new AddRecommendBalanceOkMessage(data.getSerial(),data.readBoolean(),
                data.readInt(),data.readInt(), data.readInt(), data.readInt());
        return msg;
    }

    protected ModifyPhoneOkMessage getModifyPhoneOkMessage(UWAPData data) throws Exception{
		ModifyPhoneOkMessage msg = new ModifyPhoneOkMessage(data.getSerial());
		return msg;
	}
	
	protected ModifyPhoneMessage getModifyPhoneMessage(UWAPData data) throws Exception{
		ModifyPhoneMessage msg = new ModifyPhoneMessage(data.getSerial(),data.readString(),data.readString(),data.readString());
		return msg;
	}	
	
	protected ModifyPasswordOkMessage getModifyPasswordResultMessage(UWAPData data) throws Exception{
		ModifyPasswordOkMessage msg = new ModifyPasswordOkMessage(data.getSerial());
		return msg;
	}
	
	protected ModifyPasswordMessage getModifyPasswordMessage(UWAPData data) throws Exception{
		ModifyPasswordMessage msg = new ModifyPasswordMessage(data.getSerial(),data.readString(),data.readString(),data.readString(),data.readString());
		return msg;
	}
	
	protected _DecBalanceOkMessage get_DecIMoneyOkMessage(UWAPData data) throws Exception{
		_DecBalanceOkMessage msg = new _DecBalanceOkMessage(data.getSerial(),data.readInt(),data.readInt(),data.readLong(),data.readLong());
		return msg;
	}
	
	protected _DecBalanceMessage get_DecIMoneyMessage(UWAPData data) throws Exception{
		_DecBalanceMessage msg = new _DecBalanceMessage(data.getSerial(),data.readString(),data.readString(),data.readInt());
		return msg;
	}
	
	protected SyncBalanceMessage getSyncBalanceMessage(UWAPData data) throws Exception{
		SyncBalanceMessage msg = new SyncBalanceMessage(data.getSerial(),data.readInt(),data.readInt(),data.readBoolean(),data.readBoolean(),data.readLong(),data.readLong());
		return msg;
	}
	
	protected LegacyFee1Message getLegacyFee1Message(UWAPData data) throws Exception{
		LegacyFee1Message msg = new LegacyFee1Message(data.getSerial(),data.readInt(),data.readString(),data.readInt(),data.readInt());
		return msg;
	}
	
	protected _LogoutMessage get_LogoutMessage(UWAPData data) throws Exception{
		_LogoutMessage msg = new _LogoutMessage(data.getSerial(),data.readString(),data.readString());
		return msg;
	}
	
	protected Logout1Message getLogout1Message(UWAPData data) throws Exception{
		Logout1Message msg = new Logout1Message(data.getSerial(),data.readInt(),data.readString());
		return msg;
	}
	
	protected _LegacyQuickRegResultMessage get_LegacyQuickRegResultMessage(
			UWAPData data) throws Exception {
		_LegacyQuickRegResultMessage msg = new _LegacyQuickRegResultMessage(data
				.getSerial(), data.readInt(), data.readString(), data
				.readString(), data.readByte());
		return msg;
	}	
	
	
	protected LegacyQuickRegResultMessage getLegacyQuickRegResultMessage(
			UWAPData data) throws Exception {
		LegacyQuickRegResultMessage msg = new LegacyQuickRegResultMessage(data
				.getSerial(), data.readInt(), data.readString(), data
				.readString(), data.readByte());
		return msg;
	}
	
	protected LegacyQuickRegMessage getLegacyQuickRegMessage(UWAPData data)
			throws Exception {
	    int serial = data.getSerial();
	    String phone = data.readString();
	    String version = data.readString();
	    String model = data.readString();
	    String serviceId = data.readString();
	    String realPhone = "";
	    try {
	        realPhone = data.readString();
	    } catch (Exception e) {
	    }
		LegacyQuickRegMessage msg = new LegacyQuickRegMessage(serial, phone, version, model, serviceId, realPhone);
		return msg;
	}
	
	protected LegacyBuy1Message getLegacyBuy1Message(UWAPData data) throws Exception{
		int serial = data.getSerial();
		int accountId = data.readInt();
		String key = data.readString();
		int value = data.readInt();
		boolean trustOnly = false;
		try {
			trustOnly = data.readBoolean();
		} catch (Exception e) {
		}
		LegacyBuy1Message msg = new LegacyBuy1Message(serial, accountId, key, value, trustOnly);
		return msg;
	}
	
	protected LegacyBuyResultMessage getLegacyBuyResultMessage(UWAPData data)
			throws Exception {
		LegacyBuyResultMessage msg = new LegacyBuyResultMessage(data
				.getSerial(), data.readBoolean(), data.readInt(), data
				.readInt(), data.readString(), data.readLong(), data.readLong());
		return msg;
	}
	
	protected LegacyBuyMessage getLegacyBuyMessage(UWAPData data) throws Exception{
		int serial = data.getSerial();
		String name = data.readString();
		String key = data.readString();
		int value = data.readInt();
		boolean trustOnly = false;
		try {
			trustOnly = data.readBoolean();
		} catch (Exception e) {
		}
		LegacyBuyMessage msg = new LegacyBuyMessage(serial, name, key, value, trustOnly);
		return msg;
	}
	
	protected _BuyOkMessage get_ModifyIMoneyOkMessage(UWAPData data) throws Exception{
		_BuyOkMessage msg = new _BuyOkMessage(data.getSerial(),data.readInt(),data.readInt(),data.readLong(),data.readLong());
		msg.dataCreateTime = data.createTime;
		msg.messageCreateTime = System.currentTimeMillis();
		return msg;
	}
	
	protected _BuyMessage get_ModifyIMoneyMessage(UWAPData data) throws Exception{
		int serial = data.getSerial();
		String name = data.readString();
		String key = data.readString();
		int value = data.readInt();
		boolean trustOnly = false;
		try {
			trustOnly = data.readBoolean();
		} catch (Exception e) {
		}
		_BuyMessage msg = new _BuyMessage(serial, name, key, value, trustOnly);
		return msg;
	}
	
	protected LegacyFeeMessage getLegacyFeeMessage(UWAPData data) throws Exception{
		LegacyFeeMessage msg = new LegacyFeeMessage(data.getSerial(),data.readString(),data.readString(),data.readInt(),data.readInt());
		return msg;
	}
	
	protected ForceLogoutMessage getForceLogoutMessage(UWAPData data) throws Exception{
		ForceLogoutMessage msg = new ForceLogoutMessage(data.getSerial(),data.readInt(),data.readString(),data.readString());
		return msg;
	}
	
	protected ErrorMessage getErrorMessage(UWAPData data) throws Exception{
		ErrorMessage msg = new ErrorMessage(data.getSerial(),data.readInt());
		return msg;
	}
	
	protected ServerLoginMessage getServerLoginMessage(UWAPData data) throws Exception{
		ServerLoginMessage msg = new ServerLoginMessage(data.getSerial(),data.readString(),data.readString());
		return msg;
	}
	
	protected ServerLoginOkMessage getServerLoginOkMessage(UWAPData data){
		ServerLoginOkMessage msg = new ServerLoginOkMessage(data.getSerial());
		return msg;
	}

	protected AccountRegMessage getAccountRegMessage(UWAPData data)
			throws Exception {
	    int serial = data.getSerial();
	    String name = data.readString();
	    String phone = data.readString();
	    String recommend = data.readString();
	    int recommendId = data.readInt();
	    String model = data.readString();
	    String service = data.readString();
	    String version = data.readString();
	    String realPhone = "";
	    String initPass = "";
	    try {
	        realPhone = data.readString();
	        initPass = data.readString();
	    } catch (Exception e) {
	    }
		AccountRegMessage msg = new AccountRegMessage(serial, name, phone, recommend, recommendId,
		        model, service, version, realPhone, initPass);
		return msg;
	}
	
	protected AccountRegOkMessage getAccountRegOkMessage(UWAPData data) throws Exception{
		AccountRegOkMessage msg = new AccountRegOkMessage(data.getSerial(),data.readInt(),data.readString(),data.readString());
		return msg;
	}
	
	protected LegacyLoginMessage getLegacyLoginMessage(UWAPData data) throws Exception{
	    int serial = data.getSerial();
	    String name = data.readString();
	    String pass = data.readString();
	    String phone = "";
	    String partition = "";
	    String version = "";
	    String model = "";
	    try {
	    	phone = data.readString();
	    } catch (Exception e) {
	    }
	    try {
	    	partition = data.readString();
	    } catch (Exception e) {
	    }
	    try {
	    	version = data.readString();
	    } catch (Exception e) {
	    }
	    try {
	    	model = data.readString();
	    } catch (Exception e) {
	    }
		LegacyLoginMessage msg = new LegacyLoginMessage(serial, name, pass, phone,partition, version, model);
		return msg;
	}
	
	protected LegacyLoginOkMessage getLegacyLoginOkMessage(UWAPData data)
			throws Exception {
		LegacyLoginOkMessage msg = new LegacyLoginOkMessage(data.getSerial(),
				data.readInt(), data.readString(), data.readString(), data
						.readString(), data.readInt(), data.readInt(), data
						.readBoolean(), data.readBoolean(), data.readInt(),data.readInts(),data.readLong(),data.readLong());
		return msg;
	}
	
	protected _LegacyLoginOkMessage get_LegacyLoginOkMessage(UWAPData data)
			throws Exception {
		_LegacyLoginOkMessage msg = new _LegacyLoginOkMessage(data.getSerial(),data.readInt(),
				data.readString(), data.readString(), data.readString(), data
						.readInt(), data.readInt(), data.readInt(),data.readInts(),data.readLong(),data.readLong());
		return msg;
	}
	
	protected QQBillingMessage getQQBillingMessage(UWAPData data) throws Exception{
		QQBillingMessage msg = new QQBillingMessage(data.getSerial(),data.readString(),data.readString(),data.readInt(),data.readInt());
		return msg;
	}
}
