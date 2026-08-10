package peony.game.asyncbattle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.game.Server;
import peony.net.ClientSession;

public class AsyncBattleScoreSaveCall extends ClientSessionAsyncCall {
	private static final Logger log = Logger.getLogger(AsyncBattleScoreSaveCall.class);
	public AsyncBattleScoreSaveCall(ClientSession session) {
		super(session);
		
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		AsyncBattleService service=Server.server.getServiceRegistry().getAsyncBattleService();
		AsyncNormalBoardDao dao=Server.server.getServiceRegistry().getDbService().asyncnormalboardDao;
		List<AsyncNormalBoard> list = new ArrayList<AsyncNormalBoard>();
		HashMap<Integer, Integer> id2rankCache = new HashMap<Integer, Integer>();
		synchronized (service) {
			for(AsyncNormalBoard board:service.id2boards.values()){
				if(board==null)
					continue;
				list.add(board);
				id2rankCache.put(board.playerId, board.rank);
				try{
					StringBuffer sb=new StringBuffer();
					sb.append("ACHIEVEMENTSTATE[");
					for(int i=0;i<board.achievementStateNew.length;i++){
						String flag=",";
						if(i==0){
							flag="";
						}
						sb.append(flag+board.achievementStateNew[i]);
					}
					sb.append("]");
					log.info("[ASYNCBATTLESCORESAVECALL]PLAYERID["+board.playerId+"]RANK["+board.rank+"]UPRANK["+board.upRank+"]FACTION["+board.faction+"]UPRANKTIME["+board.upRankTime+"]LOGINDAY["+board.loginDay+"]CLAZZ["+board.clazz+"]LEVEL["+board.level+"]BATTLECOUNT["+board.battleCount+"]OFFICERINDEX["+board.officerIndex+"]DAYFLAG["+board.dayFlag+"]DAYFLAG_GETREWARDTIME["+board.dayFlag_GetRewardTime+"]OFFICERSCORE["+board.officerScore+"]"+sb.toString());
				}catch(Exception e){
				}
			}
		}
		for(AsyncNormalBoard board : list){
			AsyncNormalBoard boardTemp=dao.getAsyncNormalBoardById(board.playerId);
			if(boardTemp==null){
				dao.newEntity(board);
			}else{
				dao.updateEntity(board);
			}
			try {
				int cacheRank = id2rankCache.get(board.playerId);
				if(cacheRank!=board.rank){
					dao.update("update Asyncnormalboard a set a.rank=? where a.playerId=?", cacheRank, board.playerId);
					log.info("[UPDATEASYNCBOARD-SAMERANK]RANK[" + board.rank + "]");
				}
			} catch (Exception e) {
				log.error(e, e);
			}
		}
	}

}
