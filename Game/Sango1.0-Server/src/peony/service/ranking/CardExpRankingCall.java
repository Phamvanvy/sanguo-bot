package peony.service.ranking;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.db.RankingDAO;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;

public class CardExpRankingCall extends ClientSessionAsyncCall{

	
	protected Player player;
	public CardExpRankingCall(ClientSession session,Player player) {
		super(session);
		this.player = player;
	}

	public void callFinish() throws Exception {

		
	}

	public void run() {
		RankingService service = Server.server.getServiceRegistry().getRankingService();
		synchronized(service.newCardExpTopTen){
			List<Ranking> topTen = service.getNewCardRanking();
			RankingDAO dao = Server.server.getServiceRegistry().getDbService().rankingDAO;
			if(topTen==null){
				topTen = new ArrayList<Ranking>();
			}
			Ranking rank = service.isInTopTen(player,topTen);
			if(rank!=null){
				if(rank.value < player.cardExpAdd){
					rank.setValue(player.cardExpAdd);
					rank.setValue2(player.rockCardCount);
					rank.setTime(new Date());
					dao.updateEntity(rank);
					service.updateRanking1(rank,topTen);
				}
			}else{
				if(topTen.size()<10){
					Ranking ranking = new Ranking();
					ranking.setPlayerId(player.id);
					ranking.setPlayerName(player.name);
					ranking.setTime(new Date());
					ranking.setType(RankingService.TYPE_ROCKCARD);
					ranking.setValue(player.cardExpAdd);
					ranking.setValue2(player.rockCardCount);
					ranking.setFaction(player.faction);
					dao.newEntity(ranking);
					topTen.add(ranking);
					if(topTen.size()>1){
						service.updateRanking1(ranking, topTen);
					}
				}else{
					Ranking lastRanking = topTen.get(topTen.size()-1);
					if(lastRanking.value<player.cardExpAdd){
						dao.makeTransient(lastRanking);
					    topTen.remove(topTen.size()-1);
					    Ranking ranking = new Ranking();
						ranking.setPlayerId(player.id);
						ranking.setPlayerName(player.name);
						ranking.setTime(new Date());
						ranking.setType(RankingService.TYPE_ROCKCARD);
						ranking.setValue(player.cardExpAdd);
						ranking.setValue2(player.rockCardCount);
						ranking.setFaction(player.faction);
						dao.newEntity(ranking);
						topTen.add(ranking);
						service.updateRanking1(ranking, topTen);
					}
				}
			}
		}
	}

}
