package peony.service.tong;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.tong.apply.TongBattleApplyService;

public class TongInfoCall extends ClientSessionAsyncCall {

	private int serial;
	private Player p;
	
	private String tongName;
	private String name;
	private int level;
	private String battleRecord;
	private int money;
	private int maxPlayer;
	private String cityName;
	
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
			pt.putString(cityName);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_INFO_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(p!=null){
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			TongBattleApplyService tongBattleApplyService = Server.server.getServiceRegistry().getTongBattleApplyService();
			Tong tong = tongService.getPlayerTong(p.id);
			if(tong==null)
				error("你还没有加入军团");
			this.tongName = tong.name;
			this.name = tong.getChairmanName()==null?"":tong.getChairmanName();
			this.level = (tong.level == 0 ? 1 : tong.level);
			this.battleRecord = MessageFormat.format("{0}胜{1}负", 
					tong.pool.getInt(Tong.PROPERTY_TONGBATTLE_WIN,0),tong.pool.getInt(Tong.PROPERTY_TONGBATTLE_FAIL));
			this.money = tong.money;
			this.maxPlayer = TongService.LEVEL_CONFIG[tong.level][0];
			if(tongBattleApplyService.isWinner(tong.id)){
				this.cityName = tongBattleApplyService.getMapName(tongBattleApplyService.getWinnerMapId(tong.id));
			}else{
				this.cityName = "无";
			}
		}
		addToClientSession();
	}

}
