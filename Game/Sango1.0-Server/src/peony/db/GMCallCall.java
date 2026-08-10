package peony.db;

import java.util.HashMap;
import java.util.Map;

import peony.channel.Channel;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.admin.GMRequest;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;

public class GMCallCall extends ClientSessionAsyncCall {

	protected int serial;
	protected String cause;
	
	protected static Map<Integer, Integer> lastTimes = new HashMap<Integer, Integer>();
	public static int timeDis = 10 * 1000;
	
	public GMCallCall(ClientSession session,Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.cause = packet.getString();
	}
	
	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.GM_CALL_SERVER);
			pt.putInt(serial);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.GM_CALL_CLIENT, errorMessage);
		}
	}

	public void run() {
		Player p = (Player)session.getClient();
		if(p!=null){
			if(lastTimes.get(p.id)!=null && (Time.currTime-lastTimes.get(p.id))<timeDis){
				error("您发送的时间间隔较短，请稍后再试");
				addToClientSession();
				return;
			}
			Account a = (Account)session.getIdentity();
			GMRequest request = new GMRequest(0,p.id,p.name,cause,p.map.id,p.x,p.y,a.getModel());
			Server.server.getServiceRegistry().getDbService().gmQuestDAO.newEntity(request);
			Channel channel = Server.server.getServiceRegistry().getChannelService().getChannel("gm");
			Packet pt = new Packet(OpCode.ADMIN_GMREQUEST_ADDED_SERVER);
			pt.putInt(request.id);
			pt.put(request.getType());
			pt.putInt(request.getPlayerId());
			pt.putString(request.getPlayerName());
			pt.putString(request.getCause());
			pt.put(request.state);
			pt.putString(request.getSolvent());
			pt.putString(request.getModel());
			pt.putShort(request.getMapId());
			pt.putShort(request.getX());
			pt.putShort(request.getY());
			channel.broadcast(pt, null);
			lastTimes.put(p.id, Time.currTime);
			addToClientSession();
		}else{
			error(null,peony.Messages.STRING_00435);
		}
	}

}
