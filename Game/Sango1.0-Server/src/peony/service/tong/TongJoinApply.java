package peony.service.tong;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 通过自动加入军团系统申请加入军团
 * @author pmeng
 */
public class TongJoinApply extends ClientSessionAsyncCall {
	private int serial;
	private int tongID;
	private Player p;
	public TongJoinApply(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.tongID = packet.getInt();
		this.p = (Player)session.getClient();
	}
	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.TONG_APPLY_JOIN_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_APPLY_JOIN_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(p!=null){
			TongService tongService = Server.server.getServiceRegistry().getTongService(); 
			try{
				tongService.atuoJionTong(p, tongID);
			}catch(TongException e){
				error(e.getMessage());
			}
		}
		addToClientSession();
	}

}
