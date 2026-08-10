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

public class BindWeiboCall extends ClientSessionAsyncCall{
	
	int serial;
	Player p = null;
	String name;
	String password;
	

	public BindWeiboCall(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		name = packet.getString();
		password = packet.getString();
		p = (Player)session.getClient();
	    
	}

	public void callFinish() throws Exception {
		if(success){
			LogUtil.logBindWeiboResult(p,name,"SUCCESS");
			Packet pt = new Packet(OpCode.BIND_WEIBO_SERVER);
			pt.putInt(serial);
			pt.put(p.pool.getString(Player.PROPERTY_WEIBO_TOKEN).equals("")?0:1);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.BIND_WEIBO_CLIENT,errorMessage);
		}
	}

	public void run() {
		if(p!=null){
		    WeiboService weiboService = Server.server.getServiceRegistry().getWeiboService();
		    try {
				weiboService.bindWeibo(p, name, password);
				addToClientSession();
			} catch (WeiboException e) {
				LogUtil.logBindWeiboResult(p,name,"FAILD");
				ErrorHandler.sendErrorMessage(session, serial, OpCode.BIND_WEIBO_CLIENT,e.getMessage());
			}
		}
		
	}
}
