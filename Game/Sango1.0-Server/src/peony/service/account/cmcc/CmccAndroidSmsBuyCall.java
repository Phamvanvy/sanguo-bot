package peony.service.account.cmcc;

import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.AccountAsyncCall;
import peony.service.account.AccountService;

public class CmccAndroidSmsBuyCall extends AccountAsyncCall {

	protected int requestId;
	protected int accountId;
	protected Player player;
	protected int playerId;
	protected String consumeCode;
	
	public CmccAndroidSmsBuyCall(ClientSession session,Player player,int requestId,String consumeCode) {
		super(session);
		this.requestId = requestId;
		this.accountId = player.accountId;
		this.player = player;
		this.playerId = player.id;
		this.consumeCode = consumeCode;
	}

	public void callFinish() throws Exception {
		if(success){
			if(message instanceof CmccAndroidSmsBuyReqResultMessage){
				CmccAndroidSmsBuyReqResultMessage me = (CmccAndroidSmsBuyReqResultMessage)message;
				if(me.isResult()){
					Packet pt = new Packet(OpCode.CMCC_ANDROID_SMS_BUY_REQ_SERVER);
					pt.putInt(requestId);
					pt.putString(me.getSms());
					session.send(pt);
				}else{
					ErrorHandler.sendErrorMessage(session, requestId, OpCode.CMCC_ANDROID_SMS_BUY_REQ_CLIENT, me.getSms());
				}
		    }else{
			
		    }
		}
	}

	public void run() {
		AccountService service = Server.server.getServiceRegistry().getAccountService();
		
		service.sendAndRegister(new CmccAndroidSmsBuyReqMessage(requestId, accountId, playerId, 
				consumeCode, player.getAccount().getVersionString()), this);
	}

}
