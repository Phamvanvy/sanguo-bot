package com.pip.net.message.gameaccount;

import org.apache.log4j.Logger;

import com.pip.net.IMessage;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.ServerLoginMessage;
import com.pip.net.message.ServerLoginOkMessage;
import com.pip.net.message.ServerMessageType;
import com.pip.net.uwap2.mina.IMessageEncoder;
import com.pip.net.uwap2.mina.UWAPSegment;

public class MessageEncoder implements IMessageEncoder {
	
	protected static final Logger log = Logger.getLogger(MessageEncoder.class);

	public UWAPSegment encode(IMessage message) throws Exception {
		short cmd = message.getCmd();
		log.debug("send message:"+cmd);
		switch (cmd) {
		case ServerMessageType.ERROR:
		return getErrorSeg((ErrorMessage)message);
		case ServerMessageType.LOGIN:
			return getServerLoginSeg((ServerLoginMessage)message);
		case ServerMessageType.LOGINOK:
			return getServerLoginOkSeg((ServerLoginOkMessage)message);
		case GameAccountMessageType.ACCOUNT_REG:
			return getAccountRegSeg((AccountRegMessage)message);
		case GameAccountMessageType.ACCOUNT_REG_OK:
			return getAccountRegOkSeg((AccountRegOkMessage)message);
		case GameAccountMessageType.LEGACY_LOGIN:
			return getLegacyLoginSeg((LegacyLoginMessage)message);
		case GameAccountMessageType.LEGACY_LOGIN_OK:
			return getLegacyLoginOkSeg((LegacyLoginOkMessage)message);
		case GameAccountMessageType.FORCE_LOGOUT:
			return getForceLogoutSeg((ForceLogoutMessage)message);
		case GameAccountMessageType.LEGACY_FEE:
			return getLegacyFeeSeg((LegacyFeeMessage)message);
		case GameAccountMessageType.BUY:
			return get_ModifyIMoneySeg((_BuyMessage)message);
		case GameAccountMessageType.BUY_OK:
			return get_ModifyIMoneyOkSeg((_BuyOkMessage)message);
		case GameAccountMessageType.LEGACY_BUY:
			return getLegacyBuySeg((LegacyBuyMessage)message);
		case GameAccountMessageType.LEGACY_BUY_RESULT:
			return getLegacyBuyResultSeg((LegacyBuyResultMessage)message);
		case GameAccountMessageType.LEGACY_BUY1:
			return getLegacyBuy1Seg((LegacyBuy1Message)message);
		case GameAccountMessageType.LEGACY_QUICKREG:
			return getLegacyQuickRegSeg((LegacyQuickRegMessage)message);
		case GameAccountMessageType.LEGACY_QUICKREG_RESULT:
			return getLegacyQuickRegResultSeg((LegacyQuickRegResultMessage)message);
		case GameAccountMessageType._LEGACY_QUICKREG_RESULT:
			return get_LegacyQuickRegResultSeg((_LegacyQuickRegResultMessage)message);
		case GameAccountMessageType.LOGOUT1:
			return getLogout1Seg((Logout1Message)message);
		case GameAccountMessageType._LOGOUT:
			return get_LogoutSeg((_LogoutMessage)message);
		case GameAccountMessageType.LEGACY_FEE1:
			return getLegacyFee1Seg((LegacyFee1Message)message);
		case GameAccountMessageType.SYNC_BALANCE:
			return getSyncBalanceSeg((SyncBalanceMessage)message);
		case GameAccountMessageType.DEC_BALANCE:
			return get_DecIMoneySeg((_DecBalanceMessage)message);
		case GameAccountMessageType.DEC_BALANCE_OK:
			return get_DecIMoneyOkSeg((_DecBalanceOkMessage)message);
		case GameAccountMessageType.MODIFY_PASSWORD:
			return getModifyPasswordSeg((ModifyPasswordMessage)message);
		case GameAccountMessageType.MODIFY_PASSWORD_OK:
			return getModifyPasswordResultSeg((ModifyPasswordOkMessage)message);
		case GameAccountMessageType.MODIFY_PHONE:
			return getModifyPhoneSeg((ModifyPhoneMessage)message);
		case GameAccountMessageType.MODIFY_PHONE_OK:
			return getModifyPhoneOkSeg((ModifyPhoneOkMessage)message);
		case GameAccountMessageType.ADD_RECOMMEND_BALANCE:
			return getAddRecommendBalanceSeg((AddRecommendBalanceMessage)message);
		case GameAccountMessageType.ADD_RECOMMEND_BALANCE_OK:
		    return getAddRecommendBalanceOkSeg((AddRecommendBalanceOkMessage)message);
		case GameAccountMessageType.CHANGE_STATUS:
			return getChangeStatusSeg((ChangeStatusMessage)message);
		case GameAccountMessageType.ACCOUNT_INFO:
			return getAccountInfoSeg((AccountInfoMessage)message);
		case GameAccountMessageType.ACCOUNT_INFO_OK:
			return getAccountInfoOkSeg((AccountInfoOkMessage)message);
		case GameAccountMessageType._CHANGE_STATUS:
			return get_ChangeStatusSeg((_ChangeStatusMessage)message);
		case GameAccountMessageType.GET_ACCOUNTNAME:
			return getGetAccountNameSeg((GetAccountNameMessage)message);
		case GameAccountMessageType.GET_ACCOUNTNAME_OK:
			return getGetAccountNameOkSeg((GetAccountNameOkMessage)message);
		case GameAccountMessageType._ACCOUNT_INFO:
			return get_AccountInfoSeg((_AccountInfoMessage)message);
		case GameAccountMessageType.LOGIN:
			return getLoginSeg((LoginMessage)message);
		case GameAccountMessageType.QQ_BILLING:
			return getQQBillingSeg((QQBillingMessage)message);
		case GameAccountMessageType.RENAME:
			return getRenameSeg((RenameMessage)message);
		case GameAccountMessageType.RENAME_OK:
			return getRenameOkSeg((RenameOkMessage)message);
		case GameAccountMessageType.ONLINE_TIME_NOTIFY:
		    return getOnlineTimeNotifySeg((OnlineTimeNotifyMessage)message);
        case GameAccountMessageType.CREDIT_CHANGE_NOTIFY:
            return getCreditChangeNotifySeg((CreditChangeNotifyMessage)message);
        case GameAccountMessageType.RECOMMEND_REQUEST:
            return getRecommendRequestSeg((RecommendRequestMessage)message);
        case GameAccountMessageType.LEVEL_UP_NOTIFY:
            return getLevelUpNotifySeg((LevelUpNotifyMessage)message);
        case GameAccountMessageType.RECOMMEND_REWARD_NOTIFY:
            return getRecommendRewardNotifySeg((RecommendRewardNotifyMessage)message);
        case GameAccountMessageType.CREATE_IMONEY_CARD:
			return getCreateIMoneyCardSeg((CreateIMoneyCardMessage)message);
		case GameAccountMessageType.CREATE_IMONEY_CARD_OK:
			return getCreateIMoneyCardOkSeg((CreateIMoneyCardOkMessage)message);
		case GameAccountMessageType.USE_IMONEY_CARD:
			return getUseIMoneyCardSeg((UseIMoneyCardMessage)message);
		case GameAccountMessageType.USE_IMONEY_CARD_OK:
			return getUseIMoneyCardOkSeg((UseIMoneyCardOkMessage)message);
		case GameAccountMessageType.ADD_BALANCE:
			return getAddBalanceSeg((AddBalanceMessage)message);
		case GameAccountMessageType.ADD_BALANCE_OK:
		    return getAddBalanceOkSeg((AddBalanceOkMessage)message);
		case GameAccountMessageType.PHONE_NOTIFY:
			return getPhoneNotifySeg((PhoneNotifyMessage)message);
		default:{
			log.info("Message["+cmd+"]miss");
			return null;
		}
		}
	}
	
	protected UWAPSegment getPhoneNotifySeg(PhoneNotifyMessage message) {
        UWAPSegment seg = new UWAPSegment(message.getCmd(), message.getSerial());
        seg.writeInt(message.getAccountID());
        seg.writeString(message.getPhone());
        return seg;
	}
	
	protected UWAPSegment getAddBalanceSeg(AddBalanceMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountID());
		seg.writeInt(message.getValue());
		seg.writeString(message.getReason());
		return seg;
	}

    protected UWAPSegment getAddBalanceOkSeg(AddBalanceOkMessage message){
        UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
        seg.writeInt(message.getAccountID());
        seg.writeInt(message.getValue());
        return seg;
    }

    protected UWAPSegment getUseIMoneyCardOkSeg(UseIMoneyCardOkMessage message) {
		UWAPSegment seg = new UWAPSegment(message.getCmd(), message.getSerial());
		seg.writeInt(message.getAccountID());
		seg.writeInt(message.getAmount());
		seg.writeInt(message.getBalance());
		return seg;
	}
	
	protected UWAPSegment getUseIMoneyCardSeg(UseIMoneyCardMessage message) {
		UWAPSegment seg = new UWAPSegment(message.getCmd(), message.getSerial());
		seg.writeString(message.getGameCode());
		seg.writeInt(message.getAccountID());
		seg.writeString(message.getKey());
		seg.writeString(message.getCardno());
		seg.writeString(message.getPassword());
		return seg;
	}

	protected UWAPSegment getCreateIMoneyCardOkSeg(CreateIMoneyCardOkMessage message) {
		UWAPSegment seg = new UWAPSegment(message.getCmd(), message.getSerial());
		seg.writeInt(message.getAccountID());
		seg.writeInt(message.getCost());
		seg.writeInt(message.getBalance());
		seg.writeString(message.getCardno());
		seg.writeString(message.getPassword());
		return seg;
	}
	
	protected UWAPSegment getCreateIMoneyCardSeg(CreateIMoneyCardMessage message) {
		UWAPSegment seg = new UWAPSegment(message.getCmd(), message.getSerial());
		seg.writeString(message.getGameCode());
		seg.writeInt(message.getAccountID());
		seg.writeString(message.getKey());
		seg.writeInt(message.getAmount());
		return seg;
	}
	
	protected UWAPSegment getRecommendRequestSeg(RecommendRequestMessage message) {
	    UWAPSegment seg = new UWAPSegment(message.getCmd(), message.getSerial());
	    seg.writeInt(message.getAccountID());
	    seg.writeString(message.getPhone());
	    seg.writeString(message.getGameCode());
	    return seg;
	}
    
	protected UWAPSegment getLevelUpNotifySeg(LevelUpNotifyMessage message) {
        UWAPSegment seg = new UWAPSegment(message.getCmd(), message.getSerial());
        seg.writeInt(message.getAccountID());
        seg.writeInt(message.getPlayerID());
        seg.writeInt(message.getLevel());
        seg.writeString(message.getGameCode());
        return seg;
    }

    protected UWAPSegment getRecommendRewardNotifySeg(RecommendRewardNotifyMessage message) {
        UWAPSegment seg = new UWAPSegment(message.getCmd(), message.getSerial());
        seg.writeInt(message.getAccountID());
        seg.writeInt(message.getPlayerID());
        seg.writeInt(message.getLevel());
        seg.writeString(message.getGameCode());
        seg.writeInt(message.getGuestRewardValue());
        seg.writeInt(message.getOwnerID());
        seg.writeInt(message.getOwnerRewardValue());
        return seg;
    }
	
    protected UWAPSegment getCreditChangeNotifySeg(CreditChangeNotifyMessage message) {
        UWAPSegment seg = new UWAPSegment(message.getCmd(), message.getSerial());
        seg.writeInt(message.getAccountID());
        seg.writeInt(message.getCredit());
        return seg;
    }

    protected UWAPSegment getOnlineTimeNotifySeg(OnlineTimeNotifyMessage message) {
	    UWAPSegment seg = new UWAPSegment(message.getCmd(), message.getSerial());
	    seg.writeInt(message.getAccountID());
	    seg.writeInt(message.getDuration());
	    return seg;
	}

	protected UWAPSegment getRenameSeg(RenameMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getOldName());
		seg.writeString(message.getNewName());
		return seg;
	}
	
	protected UWAPSegment getRenameOkSeg(RenameOkMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		return seg;
	}
	
	protected UWAPSegment getLoginSeg(LoginMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getKey());
		return seg;
	}
	
	protected UWAPSegment get_AccountInfoSeg(_AccountInfoMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		return seg;
	}
	
	protected UWAPSegment getGetAccountNameSeg(GetAccountNameMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountId());
		return seg;
	}
	
	protected UWAPSegment getGetAccountNameOkSeg(GetAccountNameOkMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		return seg;
	}
	
	protected UWAPSegment getAccountInfoOkSeg(AccountInfoOkMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountId());
		seg.writeString(message.getName());
		seg.writeString(message.getPassword());
		seg.writeString(message.getPhone());
		return seg;
	}
	
	protected UWAPSegment getAccountInfoSeg(AccountInfoMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountId());
		seg.writeString(message.getName());
		return seg;
	}
	
	protected UWAPSegment get_ChangeStatusSeg(_ChangeStatusMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeInt(message.getStatus());
		seg.writeString(message.getMessage());
		return seg;
	}
	
	protected UWAPSegment getChangeStatusSeg(ChangeStatusMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountId());
		seg.writeInt(message.getStatus());
		seg.writeString(message.getMessage());
		return seg;
	}
	
	protected UWAPSegment getAddRecommendBalanceSeg(AddRecommendBalanceMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountID());
		seg.writeInt(message.getValue());
		seg.writeInt(message.getValue2());
		return seg;
	}

    protected UWAPSegment getAddRecommendBalanceOkSeg(AddRecommendBalanceOkMessage message){
        UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
        seg.writeBoolean(message.getResult());
        seg.writeInt(message.getAccountID());
        seg.writeInt(message.getValue());
        seg.writeInt(message.getRecommendID());
        seg.writeInt(message.getValue2());
        return seg;
    }
	
	protected UWAPSegment getModifyPhoneOkSeg(ModifyPhoneOkMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		return seg;
	}
	
	protected UWAPSegment getModifyPhoneSeg(ModifyPhoneMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getKey());
		seg.writeString(message.getPhone());
		return seg;
	}	
	
	protected UWAPSegment getModifyPasswordResultSeg(ModifyPasswordOkMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		return seg;
	}
	
	protected UWAPSegment getModifyPasswordSeg(ModifyPasswordMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getKey());
		seg.writeString(message.getOldPassword());
		seg.writeString(message.getPassword());
		return seg;
	}
	
	protected UWAPSegment get_DecIMoneyOkSeg(_DecBalanceOkMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getValue());
		seg.writeInt(message.getBalance());
		return seg;
	}
	
	protected UWAPSegment get_DecIMoneySeg(_DecBalanceMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getKey());
		seg.writeInt(message.getValue());
		return seg;
	}
	
	protected UWAPSegment getSyncBalanceSeg(SyncBalanceMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountId());
		seg.writeInt(message.getBalance());
		seg.writeBoolean(message.isMonth());
		seg.writeBoolean(message.isSubscribe());
		return seg;
	}
	
	protected UWAPSegment getLegacyFee1Seg(LegacyFee1Message message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountId());
		seg.writeString(message.getKey());
		seg.writeInt(message.getFee());
		seg.writeInt(message.getBalance());
		return seg;
	}
	
	protected UWAPSegment get_LogoutSeg(_LogoutMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getKey());
		return seg;
	}
	
	protected UWAPSegment getLogout1Seg(Logout1Message message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountId());
		seg.writeString(message.getKey());
		return seg;
	}
	
	protected UWAPSegment get_LegacyQuickRegResultSeg(_LegacyQuickRegResultMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getPassword());
		seg.write(message.getResult());
		return seg;
	}
	
	protected UWAPSegment getLegacyQuickRegResultSeg(LegacyQuickRegResultMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountId());
		seg.writeString(message.getName());
		seg.writeString(message.getPassword());
		seg.write(message.getResult());
		return seg;
	}
	
	protected UWAPSegment getLegacyQuickRegSeg(LegacyQuickRegMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getPhone());
		seg.writeString(message.getVersion());
		seg.writeString(message.getModel());
		seg.writeString(message.getServiceId());
		seg.writeString(message.getRealPhone());
		return seg;
	}
	
	protected UWAPSegment getLegacyBuy1Seg(LegacyBuy1Message message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountId());
		seg.writeString(message.getKey());
		seg.writeInt(message.getValue());
		return seg;
	}

	
	protected UWAPSegment getLegacyBuyResultSeg(LegacyBuyResultMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeBoolean(message.isSuccess());
		seg.writeInt(message.getBalance());
		seg.writeInt(message.getCost());
		seg.writeString(message.getCause());
		return seg;
	}
	
	protected UWAPSegment getLegacyBuySeg(LegacyBuyMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getKey());
		seg.writeInt(message.getValue());
		return seg;
	}
	
	protected UWAPSegment get_ModifyIMoneyOkSeg(_BuyOkMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getCost());
		seg.writeInt(message.getBalance());
		return seg;
	}
	
	protected UWAPSegment get_ModifyIMoneySeg(_BuyMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getKey());
		seg.writeInt(message.getValue());
		return seg;
	}
	protected UWAPSegment getLegacyFeeSeg(LegacyFeeMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getKey());
		seg.writeInt(message.getFee());
		seg.writeInt(message.getBalance());
		return seg;
	}
	
	protected UWAPSegment getForceLogoutSeg(ForceLogoutMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getId());
		seg.writeString(message.getName());
		seg.writeString(message.getKey());
		return seg;
	}
	
	protected UWAPSegment getErrorSeg(ErrorMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getCode());
		return seg;
	}
	
	protected UWAPSegment getServerLoginSeg(ServerLoginMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getServerId());
		seg.writeString(message.getPassword());
		return seg;
	}

	protected UWAPSegment getServerLoginOkSeg(ServerLoginOkMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		return seg;
	}
	
	protected UWAPSegment getAccountRegSeg(AccountRegMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getPhone());
		seg.writeString(message.getRecommend());
		seg.writeInt(message.getRecommendId());
		seg.writeString(message.getModel());
		seg.writeString(message.getService());
		seg.writeString(message.getVersion());
		seg.writeString(message.getRealPhone());
		seg.writeString(message.getInitPassword());
		return seg;
	}
	
	protected UWAPSegment getAccountRegOkSeg(AccountRegOkMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeInt(message.getAccountId());
		seg.writeString(message.getName());
		seg.writeString(message.getPassword());
		return seg;
	}
	
	protected UWAPSegment getLegacyLoginSeg(LegacyLoginMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getName());
		seg.writeString(message.getPassword());
		seg.writeString(message.getPhone());
		return seg;
	}
	
	protected UWAPSegment getLegacyLoginOkSeg(LegacyLoginOkMessage message) {
		UWAPSegment seg = new UWAPSegment(message.getCmd(), message.getSerial());
		seg.writeInt(message.getAccountId());
		seg.writeString(message.getName());
		seg.writeString(message.getKey());
		seg.writeString(message.getPhone());
		seg.writeInt(message.getModifiedNameTimes());
		seg.writeInt(message.getIMoney());
		seg.writeBoolean(message.isMonth());
		seg.writeBoolean(message.isSubscribe());
		seg.writeInt(message.getLoginErrorTimes());
		seg.writeInts(message.getPurchasedCodes());
		return seg;
	}
	
	protected UWAPSegment getQQBillingSeg(QQBillingMessage message){
		UWAPSegment seg = new UWAPSegment(message.getCmd(),message.getSerial());
		seg.writeString(message.getUin());
		seg.writeString(message.getLinkId());
		seg.writeInt(message.getGoodId());
		seg.writeInt(message.getCount());
		return seg;
	}
}
