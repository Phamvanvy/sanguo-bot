package com.pip.battleskeleton;

import java.net.SocketAddress;

import com.pip.net.Connector;
import com.pip.net.message.gameaccount.MessageDecoder;
import com.pip.net.message.gameaccount.MessageEncoder;
import com.pip.net.uwap2.mina.*;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import com.pip.net.IMessageHandler;
import com.pip.net.IMessage;
import com.pip.net.message.gameaccount.GameAccountMessageType;
import com.pip.net.message.gameaccount.AccountRegOkMessage;
import com.pip.net.IRequestService;
import com.pip.net.message.gameaccount.LegacyLoginOkMessage;
import com.pip.net.message.gameaccount.LegacyBuyResultMessage;
import com.pip.net.message.ServerMessageType;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.ForceLogoutMessage;
import com.pip.itimes.server.world.ConnectService;
import com.pip.net.message.gameaccount.LegacyQuickRegResultMessage;
import org.apache.log4j.Logger;
import com.pip.itimes.server.world.fee.FeeService;
import com.pip.net.message.gameaccount.SyncBalanceMessage;
import com.pip.net.message.gameaccount.ModifyPasswordOkMessage;
import com.pip.net.message.gameaccount.ModifyPhoneOkMessage;
import com.pip.net.message.gameaccount.GetAccountNameOkMessage;
import com.pip.net.message.gameaccount.AccountInfoOkMessage;
import com.pip.itimes.server.world.BuyResult;
import com.pip.itimes.server.world.LoginResult;
import com.pip.itimes.server.world.ReloginResult;
import com.pip.itimes.server.world.Server;

public class BattleSkeleton extends Connector implements IMessageHandler{
    private static final Logger log = Logger.getLogger(BattleSkeleton.class);

    private IRequestService requestService;
    private ConnectService connectService;
    private FeeService feeService;

    public BattleSkeleton(String id,SocketAddress address,IRequestService requestService,ConnectService connectService,FeeService feeService){
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
//                accountRegOk((AccountRegOkMessage)message);
                break;
            
        }
    }

    
}
