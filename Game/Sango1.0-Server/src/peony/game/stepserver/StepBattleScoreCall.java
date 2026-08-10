package peony.game.stepserver;

import java.util.Map;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Server;
import peony.net.ClientSession;

public class StepBattleScoreCall extends ClientSessionAsyncCall {
	private static final Logger log = Logger.getLogger(StepBattleScoreCall.class);
	protected Map<Long, StepBattleScore> scores;
	
	public StepBattleScoreCall(ClientSession session, Map<Long, StepBattleScore> scores) {
		super(session);
		this.scores = scores;
	}
	
	public void callFinish() throws Exception {
		Server.server.getServiceRegistry().getStepBattleService().scores.clear();
	}

	public void run() {
		synchronized (Server.server.getServiceRegistry().getStepBattleService()) {
			StepBattleScoreDao dao = Server.server.getServiceRegistry().getDbService().stepbattlescoreDAO;
			for(StepBattleScore sbs : scores.values()){
				if(sbs==null)
					continue;
				log.info("[STEPBATTLESCORECALL]PLAYERID["+sbs.playerid+"]acc["+sbs.accountId+"]GAMECODE["+sbs.gameCode+"]WINCOUNT["+sbs.winCount+"]TIMER["+sbs.time+"]");
				StepBattleScore score = dao.getPlayerStepBattleScoreInfo(sbs.playerid, sbs.accountId);
				if(score==null){
					dao.newEntity(sbs);
				}else{
					dao.updateEntity(sbs);
				}
			}
			Server.server.getServiceRegistry().getStepBattleService().scores.clear();
		}
//		addToClientSession();
	}

}
