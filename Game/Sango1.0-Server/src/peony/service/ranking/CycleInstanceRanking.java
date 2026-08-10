package peony.service.ranking;

import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.db.RankingDAO;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

/**
 * 荣誉塔排行榜
 * @author pmeng
 */
public class CycleInstanceRanking extends ClientSessionAsyncCall{
	
	public static boolean useEnergySavingGrank = true;
	
	int serial;
	
	Player player;
	
	Ranking playerRank;
	
	int playerRankState;
	
	int trueRank;
	
	List<Ranking> topTen;

	public CycleInstanceRanking(ClientSession session,Packet packet) {
		super(session);
		serial = packet.getInt();
		player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			Packet pt = new Packet(OpCode.RANKING_RONGYUTA_SERVER);
			pt.putInt(serial);
			pt.put(playerRankState);
			if(playerRankState == RankingService.STATE_IN_TWENTY||playerRankState == RankingService.STATE_NO_GRANKING){
				pt.put(topTen.size());
			}else{
				pt.put(topTen.size() + 1);
			}
			for(int i = 0;i < topTen.size();i++){
				Ranking  r = topTen.get(i);
				pt.putString(r.playerName);
				pt.putInt(r.value);
				pt.putInt(i + 1);
			}
			if(playerRankState == RankingService.STATE_NOT_TWENTY){
				pt.putString(playerRank.playerName);
				pt.putInt(playerRank.value);
				pt.putInt(trueRank);
			}
			session.send(pt);
		}
	}

	public void run() {
		RankingService rs = Server.server.getServiceRegistry().getRankingService();
		if(player != null){
			playerRank = rs.isInTopTwenty(player);
			topTen = rs.getTopTwenty();
			if(playerRank != null){
				playerRankState = RankingService.STATE_IN_TWENTY;
			}else{
				playerRankState = RankingService.STATE_NOT_TWENTY;
				//在缓存中查
				playerRank = rs.getPlayerRankInCache(player.id);
				RankingDAO rd = Server.server.getServiceRegistry().getDbService().rankingDAO;
				if(playerRank == null){
					//在DB中查
					playerRank = rd.findRankingByPlayerId(player.id);
					//放入缓存
					if(playerRank != null){
						rs.putRankToCache(playerRank);
						if(useEnergySavingGrank && playerRank.value<60)
							trueRank = rd.getPlayerGrank(playerRank.playerId, playerRank.value);
						else
							trueRank = rd.getPlayerGrank(playerRank.playerId);
					}
				}else{
					if(useEnergySavingGrank && playerRank.value<60)
						trueRank = rd.getPlayerGrank(playerRank.playerId, playerRank.value);
					else
						trueRank = rd.getPlayerGrank(playerRank.playerId);
				}
				if(playerRank == null){
					playerRankState = RankingService.STATE_NO_GRANKING;
				}
			}
			addToClientSession();
		}
	}
}
