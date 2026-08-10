package com.pip.accountskeleton;

import java.net.SocketAddress;

import org.apache.log4j.Logger;
import org.apache.mina.filter.codec.ProtocolCodecFilter;

import com.pip.itimes.server.world.BuyResult;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.LoginResult;
import com.pip.itimes.server.world.ReloginResult;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.fee.FeeService;
import com.pip.net.Connector;
import com.pip.net.IMessage;
import com.pip.net.IMessageHandler;
import com.pip.net.IRequestService;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.ServerMessageType;
import com.pip.net.message.gameaccount.AccountInfoOkMessage;
import com.pip.net.message.gameaccount.AccountRegOkMessage;
import com.pip.net.message.gameaccount.AddRecommendBalanceOkMessage;
import com.pip.net.message.gameaccount.CreateIMoneyCardOkMessage;
import com.pip.net.message.gameaccount.ForceLogoutMessage;
import com.pip.net.message.gameaccount.GameAccountMessageType;
import com.pip.net.message.gameaccount.GetAccountNameOkMessage;
import com.pip.net.message.gameaccount.LegacyBuyResultMessage;
import com.pip.net.message.gameaccount.LegacyLoginOkMessage;
import com.pip.net.message.gameaccount.LegacyQuickRegResultMessage;
import com.pip.net.message.gameaccount.MessageDecoder;
import com.pip.net.message.gameaccount.MessageEncoder;
import com.pip.net.message.gameaccount.ModifyPasswordOkMessage;
import com.pip.net.message.gameaccount.ModifyPhoneOkMessage;
import com.pip.net.message.gameaccount.RecommendRewardNotifyMessage;
import com.pip.net.message.gameaccount.SyncBalanceMessage;
import com.pip.net.message.gameaccount.UseIMoneyCardOkMessage;
import com.pip.net.uwap2.mina.UWAP2MessageFilter;
import com.pip.net.uwap2.mina.UWAPDecoder;
import com.pip.net.uwap2.mina.UWAPEncoder;

public class AccountSkeleton extends Connector implements IMessageHandler{
    private static final Logger log = Logger.getLogger(AccountSkeleton.class);

    private IRequestService requestService;
    private ConnectService connectService;
    private FeeService feeService;

    public AccountSkeleton(String id,SocketAddress address,IRequestService requestService,ConnectService connectService,FeeService feeService){
        super(id,address,true);
        setMessageHandler(this);
        this.requestService = requestService;
        this.connectService  = connectService;
        this.feeService = feeService;
    }

    public void init() {
        config.getFilterChain().addLast("uwap2codec", new ProtocolCodecFilter(new UWAPEncoder(), new UWAPDecoder()));
        config.getFilterChain().addLast("uwap2message",new UWAP2MessageFilter(new MessageDecoder(),new MessageEncoder()));
    }


    public void handle(IMessage message) throws Exception{
        short cmd = message.getCmd();
        switch(cmd){
            case GameAccountMessageType.ACCOUNT_REG_OK:
                accountRegOk((AccountRegOkMessage)message);
                break;
            case GameAccountMessageType.LEGACY_LOGIN_OK:
                loginOk((LegacyLoginOkMessage)message);
                break;
            case GameAccountMessageType.LEGACY_BUY_RESULT:
                buyResult((LegacyBuyResultMessage)message);
                break;
            case ServerMessageType.ERROR:
                error((ErrorMessage)message);
                break;
            case GameAccountMessageType.FORCE_LOGOUT:
                forceLogout((ForceLogoutMessage)message);
                break;
            case GameAccountMessageType.LEGACY_QUICKREG_RESULT:
                quickRegResult((LegacyQuickRegResultMessage)message);
                break;
            case GameAccountMessageType.SYNC_BALANCE:
                syncBalance((SyncBalanceMessage)message);
                break;
            case GameAccountMessageType.MODIFY_PASSWORD_OK:
                modifyPasswordOk((ModifyPasswordOkMessage)message);
                break;
            case GameAccountMessageType.MODIFY_PHONE_OK:
                modifyPhoneOk((ModifyPhoneOkMessage)message);
                break;
            case GameAccountMessageType.GET_ACCOUNTNAME_OK:
                getAccountNameOk((GetAccountNameOkMessage)message);
                break;
            case GameAccountMessageType.ACCOUNT_INFO_OK:
                accountInfoOk((AccountInfoOkMessage)message);
                break;
            //mengjie add
            case GameAccountMessageType.ADD_RECOMMEND_BALANCE_OK:
            	addRecommendbalanceOK((AddRecommendBalanceOkMessage)message);
                break;
            case GameAccountMessageType.RECOMMEND_REWARD_NOTIFY:
            	addPIPRecommendbalanceOK((RecommendRewardNotifyMessage)message);
	            break;
            case GameAccountMessageType.CREATE_IMONEY_CARD_OK:
                createImoneyCardResult((CreateIMoneyCardOkMessage)message);
                break;
            case GameAccountMessageType.USE_IMONEY_CARD_OK:
                useImoneyCardResult((UseIMoneyCardOkMessage)message);
                break;
        }
    }

    protected void accountInfoOk(AccountInfoOkMessage message) throws Exception{
        AccountInfoRequest request = (AccountInfoRequest)requestService.remove(message.getSerial());
        if(request!=null){
            request.getSession().accountInfoResult(message,request);
        }
    }

    protected void getAccountNameOk(GetAccountNameOkMessage message){
        GetAccountNameRequest request = (GetAccountNameRequest)requestService.remove(message.getSerial());
        if(request!=null){
            request.getSession().getAccountNameOk(request,message);
        }
    }

    protected void modifyPhoneOk(ModifyPhoneOkMessage message){
        ModifyPhoneRequest request = (ModifyPhoneRequest)requestService.remove(message.getSerial());
        if(request!=null){
            request.getSession().modifyPhoneOk(request.getPhone());
        }
    }

    protected void modifyPasswordOk(ModifyPasswordOkMessage message){
        ModifyPasswordRequest request = (ModifyPasswordRequest)requestService.remove(message.getSerial());
        if(request!=null){
            request.getSession().modifyPasswordOk(request.password,request.getSessionId(),request.getId());
        }
    }

    protected void syncBalance(SyncBalanceMessage message){
        feeService.synciMoney(message.getAccountId(),message.getBalance(),message.isMonth(),message.isSubscribe());
    }

    protected void quickRegResult(LegacyQuickRegResultMessage message){
        QuickRegRequest request = (QuickRegRequest)requestService.remove(message.getSerial());
        if(request!=null){
            try {
                request.getSession().quickRegResult(message.getAccountId(), message.getName(), message.getPassword(),
                        message.getResult(), request.getModel(), request.getSessionId(), request.getId());
            } catch (Exception ex) {
                log.error(ex,ex);
            }
        }
    }

    protected void forceLogout(ForceLogoutMessage message){
        connectService.forceLogout(message.getId(),message.getKey());
    }

    protected void error(ErrorMessage message){
        SessionRequest request = (SessionRequest)requestService.remove(message.getSerial());
        if(request!=null){
            request.getSession().error(message,request);
        }
    }

    protected void buyResult(LegacyBuyResultMessage message){
        StoreRequest rq = (StoreRequest)requestService.remove(message.getSerial());
        if(rq!=null){
            try {
                BuyResult result = new BuyResult();
                result.success = message.isSuccess();
                result.iMoney = message.getLongBalance();
                result.bBalance = message.getBBalance();
                result.cost = message.getCost();
                result.realCost = result.cost;
                result.cause = message.getCause();
                if(!result.success){
                    result.cause = "ÄãµÄ"+Server.iMoneyString+"²»¹»¡£";
                }
                rq.getSession().buyResult(result, rq.request);
            } catch (Exception ex) {
                log.error(ex,ex);
            }
        }
    }

    protected void accountRegOk(AccountRegOkMessage message){
        AccountRegRequest rq = (AccountRegRequest)requestService.remove(message.getSerial());
        if(rq!=null)
            rq.getSession().regOk(message.getPassword(), rq.getSessionId(),rq.getId());
    }

    protected void loginOk(LegacyLoginOkMessage message) throws Exception{
        log.info("loginOk");
        LoginRequest rq = (LoginRequest)requestService.remove(message.getSerial());
        if(rq!=null)
            if(!rq.isReglogin()){
                log.info("loginOk1");
                LoginResult result = new LoginResult();
                result.accountId = message.getAccountId();
                result.bRequest = rq;
                result.iMoney = message.getLongBalance();
                result.bBalance = message.getBBalance();
                result.isMonth = message.isMonth();
                result.isSubscribe = message.isSubscribe();
                result.key = message.getKey();
                result.loginErrorTime = message.getLoginErrorTimes();
                result.modifyPasswordTimes = message.getModifiedNameTimes();
                result.name = message.getName();
                result.password = null;
                result.phone = message.getPhone();
                result.purchased = message.getPurchasedCodes();
                rq.getSession().loginOk(result);
//                rq.getSession().loginOk(message, rq);
            }
            else{
                log.info("loginOk2");
                ReloginResult result = new ReloginResult();
                result.accountId = message.getAccountId();
                result.bRequest = rq;
                result.iMoney = message.getLongBalance();
                result.bBalance = message.getBBalance();
                result.isMonth = message.isMonth();
                result.isSubscribe = message.isSubscribe();
                result.key = message.getKey();
                result.modifyPasswordTimes = message.getModifiedNameTimes();
                result.name = message.getName();
                result.password = null;
                result.phone = message.getPhone();
                result.purchased = message.getPurchasedCodes();
                rq.getSession().reloginResult(result);
            }
    }
    //mengjie add
    protected void addRecommendbalanceOK(AddRecommendBalanceOkMessage message){
    	if (message.getResult()){
    		connectService.addRecommendbalanceresult(message.getAccountID(),message.getRecommendID());
    	}
    }
    protected void addPIPRecommendbalanceOK(RecommendRewardNotifyMessage message){
    	connectService.addPPIPRecommendbalanceresult(message.getAccountID(),message.getOwnerID());
    }
    
    protected void createImoneyCardResult(CreateIMoneyCardOkMessage message){
        CreateImoneyCardRequest request = (CreateImoneyCardRequest)requestService.remove(message.getSerial());
        
        if(request!=null){
            try {
                request.getSession().extend_create_imoney_card_result(request.getId(), request.getPlayerId(), message.getAccountID(), message.getCardno(), message.getPassword(), message.getCost(), message.getLongBalance());
            } catch (Exception ex) {
                log.error(ex,ex);
            }
        }
    }
    
    protected void useImoneyCardResult(UseIMoneyCardOkMessage message){
        UseImoneyCardRequest request = (UseImoneyCardRequest)requestService.remove(message.getSerial());
        
        if(request!=null){
            try {
                request.getSession().extend_use_imoney_card_result(request.getId(), request.getPlayerId(), message.getAccountID(), request.getCard(), message.getLongBalance());
            } catch (Exception ex) {
                log.error(ex,ex);
            }
        }
    }
}
