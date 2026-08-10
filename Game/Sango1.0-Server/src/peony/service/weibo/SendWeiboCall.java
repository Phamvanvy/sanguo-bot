package peony.service.weibo;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import weibo4j.WeiboException;

public class SendWeiboCall extends ClientSessionAsyncCall{
	
	int serial;
	Player p = null;
	byte type;
	int sourceId;
	String message;
	byte version;

	public SendWeiboCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		type = packet.get();
		sourceId = packet.getInt();
		message = packet.getString();
		version = packet.get();
		p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			LogUtil.logSendWeiboResult(p, p.pool.getString(Player.PROPERTY_WEIBO_NAME), message,"SUCCESS");
			Packet pt = new Packet(OpCode.SEND_WEIBO_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.SEND_WEIBO_CLIENT,errorMessage);
		}
	}

	public void run() {
		if(p!=null){
			WeiboService weiboService = Server.server.getServiceRegistry().getWeiboService();
			try {
				weiboService.sendWeibo(p, message,type,sourceId,version);
//				weiboService.sendWeibo(p, message,type,sourceId);
			} catch (WeiboException e) {
				LogUtil.logSendWeiboResult(p, p.pool.getString(Player.PROPERTY_WEIBO_NAME), message,"FAILD");
				error(peony.Messages.STRING_01167);
			}
		}
		addToClientSession();
	}
}
