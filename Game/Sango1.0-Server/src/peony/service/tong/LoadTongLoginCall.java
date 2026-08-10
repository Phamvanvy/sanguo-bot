package peony.service.tong;

import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.db.DBService;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;

public class LoadTongLoginCall extends ClientSessionAsyncCall{
	
	private static final Logger log = Logger.getLogger(LoadTongLoginCall.class);
	
	protected Player player;
	TongService tongService = Server.server.getServiceRegistry().getTongService();

	public LoadTongLoginCall(ClientSession session,Player player) {
		super(session);
		this.player = player;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(player!=null){
			if (!tongService.tongMembers.containsKey(player.id)) {
				DBService dbs = Server.server.getServiceRegistry().getDbService();
				TongMember m = dbs.tongMemberDAO.findByPlayerID(player.id);
				if (m != null) {
					log.info("[LOADTONG]LoadTongLoginCall");
					Tong tong = tongService.loadTong(m.tongID);
					if(tong!=null){
						player.setGuildName(tong.name);
					}
				}else if(!player.getGuildName().equals("")){
					player.addStringPropertyChangedItem(ChangedItem.GUILD, "", false);
					player.moveExtended |= Player.MOVEEXT_GUILD;
				}
			}else{
				Tong tong = tongService.getPlayerTong(player.id,false);
				if(tong!=null){
					player.setGuildName(tong.name);
				}else{
					tong = tongService.loadTong(tongService.tongMembers.get(player.id).tongID);
					if(tong!=null){
						player.setGuildName(tong.name);
					}
				}
			}
			//若加入军团但没有百宝箱  加入
			if(tongService.getPlayerTong(player.id,false) != null && player.pool.getInt(TongService.PROPERTY_TONGBOX) == 0){
				tongService.giveTongBox(player);
			}
			//初始化每日贡献度	
			if(player.pool.getInt(TongService.PROPERTY_LASTINT_DAY, 0) == 0 || player.pool.getInt(TongService.PROPERTY_LASTINT_DAY, 0) != Time.day){
				tongService.initPlayerCon(player);
			}
		}
	}

}
