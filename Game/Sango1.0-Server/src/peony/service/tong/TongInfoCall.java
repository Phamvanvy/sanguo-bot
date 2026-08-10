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
	
	private int tongContribute;		//军团贡献度
	private int upLevelContribute;  //军团升级所需贡献度
	private byte ismaintain;  		//0:未维护            1：已维护
	private byte autoAccept;  		//0:关闭                  1：开启
	private int contribute;   		//个人贡献度
	private int contributeDay;		//今日贡献度
	private int contributeDayMax;	//当日最高贡献度
	
	
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
			//扩展添加
			pt.putInt(tongContribute);
			pt.putInt(upLevelContribute);
			pt.put(ismaintain);
			pt.put(autoAccept);
			pt.putInt(contribute);
			pt.putInt(contributeDay);
			pt.putInt(contributeDayMax);
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_INFO_CLIENT, errorMessage);
		}
	}

	public void run() {
		if(p!=null){
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			TongBattleApplyService tongBattleApplyService = Server.server.getServiceRegistry().getTongBattleApplyService();
			Tong tong = tongService.getPlayerTong(p.id,true);
			TongMember tm = tongService.getPlayerInfo(p.id);
			if(tm==null || tong==null){
				error(peony.Messages.STRING_00748);
				addToClientSession();
				return;
			}
			this.tongName = tong.name;
			this.name = tong.getChairmanName()==null?"":tong.getChairmanName();
			this.level = (tong.level == 0 ? 1 : tong.level);
			this.battleRecord = MessageFormat.format(peony.Messages.STRING_00805, 
					tong.pool.getInt(Tong.PROPERTY_TONGBATTLE_WIN,0),tong.pool.getInt(Tong.PROPERTY_TONGBATTLE_FAIL));
			this.money = tong.money;
			this.maxPlayer = TongService.LEVEL_CONFIG[tong.level][0];
			if(tongBattleApplyService.isWinner(tong.id)){
				this.cityName = tongBattleApplyService.getMapName(tongBattleApplyService.getWinnerMapId(tong.id));
			}else{
				this.cityName = peony.Messages.STRING_00806;
			}
			//扩展后信息
			this.tongContribute = tong.contribute;
			if(tong.level < 6){
				this.upLevelContribute = TongService.UPLEVEL_CONTRIBUTE[tong.level + 1];
			}else{
				this.upLevelContribute = TongService.UPLEVEL_CONTRIBUTE[6];
			}
			this.ismaintain = (byte)tong.ismaintain;
			this.autoAccept = (byte)tong.autoaccept;
			this.contribute = p.contribute;
			this.contributeDay = p.contributeDay;
			this.contributeDayMax = tongService.getContributeTop(tong);
		}
		addToClientSession();
	}

}
