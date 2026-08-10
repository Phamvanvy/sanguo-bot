package peony.service.ranking;

import java.util.Date;

import peony.common.ClientSessionAsyncCall;
import peony.db.RankingDAO;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;

public class PlayerCycleRackingRecordCall extends ClientSessionAsyncCall {

	protected Player player;
	protected int value;
	
	public PlayerCycleRackingRecordCall(ClientSession session, Player player, int value) {
		super(session);
		this.player = player;
		this.value = value;
	}

	public void callFinish() throws Exception {
		// TODO Auto-generated method stub

	}

	public void run() {
		RankingService service = Server.server.getServiceRegistry().getRankingService();
		Ranking rk = service.playerRankings.get(player.id);
		RankingDAO dao = Server.server.getServiceRegistry().getDbService().rankingDAO;
		Ranking rank = service.isInTopTwenty(player);
		if(rank != null){//在前二十名中
			//比以前成绩好  更新数据库并重新排序
			if(rank.value < value){
				rank.value = value;
				dao.updateEntity(rank);
				service.updateGranking1(rank);
			}
		}else{
			if(rk == null){
				//是否有这个人记录 若有更新层数   若没有插入数据
				rk = dao.findRankingByPlayerId(player.id);
				if(rk == null){
					Ranking newRk = new Ranking();
					newRk.playerId = player.id;
					newRk.playerName = player.name;
					newRk.type = RankingService.TYPE_RONGYUTA;
					newRk.value = value;
					newRk.time = new Date();
					dao.newEntity(newRk);
					rk = newRk;
				}else{
					if(rk.value < value){
						rk.value = value;
						dao.updateEntity(rk);
					}
				}
			}else{
				if(rk.value < value){
					rk.value = value;
					dao.updateEntity(rk);
				}
			}
			if(rk!=null){
				service.updateGranking2(rk);
			}
		}
	}

}
