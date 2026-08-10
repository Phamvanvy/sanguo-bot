package peony.service.weibo;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import weibo4j.WeiboException;

public class WeiboLoginCall extends ClientSessionAsyncCall{
	
	int serial;
	String name;
	String password;
	Player p = null;

	public WeiboLoginCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		name = packet.getString();
		password = packet.getString();
		p = (Player)session.getClient();
		
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.LOGIN_WEIBO_SERVER);
			pt.putInt(serial);
			p.send(pt);
		}
	}

	public void run() {
		if(p!=null){
			WeiboService weiboService = Server.server.getServiceRegistry().getWeiboService();
			try {
				weiboService.loginWeibo(p, name, password);
			} catch (WeiboException e) {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.LOGIN_WEIBO_CLIENT,peony.Messages.STRING_00629);
			}
			addToClientSession();
		}
	}

}
