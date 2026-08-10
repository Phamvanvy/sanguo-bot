package peony.service.tong;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class TongInfoCall extends ClientSessionAsyncCall {

	private int serial;
	private Player p;
	
	private String tongName;
	private String name;
	private int level;
	private String battleRecord;
	private int money;
	private int maxPlayer;
	
	public TongInfoCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.TONG_INFO_SERVER);
			pt.putInt(serial);
			pt.putString(tongName);
			pt.putString(name);
			pt.putInt(level);
			pt.putString(battleRecord);
			pt.putInt(money);
			pt.putInt(maxPlayer);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_INFO_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(p!=null){
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getPlayerTong(p.id);
			if(tong==null)
				error("你還沒有加入軍團");
			this.tongName = tong.name;
			this.name = tong.getChairmanName();
			this.level = (tong.level == 0 ? 1 : tong.level);
			this.battleRecord = MessageFormat.format("{0}胜{1}負", 
					tong.pool.getInt(Tong.PROPERTY_TONGBATTLE_WIN,0),tong.pool.getInt(Tong.PROPERTY_TONGBATTLE_FAIL));
			this.money = tong.money;
			this.maxPlayer = TongService.LEVEL_CONFIG[tong.level][0];
		}
		addToClientSession();
	}

}
