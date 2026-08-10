package peony.game.stepserver;

import java.util.Map;

import peony.common.ClientSessionAsyncCall;
import peony.game.Server;
import peony.net.ClientSession;

public class StepBattleScore_FinalsCall extends ClientSessionAsyncCall {

	protected Map<Long, StepBattleScoreTop16> scores;
	
	public StepBattleScore_FinalsCall(ClientSession session, Map<Long, StepBattleScoreTop16> scores) {
		super(session);
		this.scores = scores;
	}
	
	public void callFinish() throws Exception {
	}

	public void run() {
		synchronized (Server.server.getServiceRegistry().getStepBattleService()) {
			StepBattleScore_FinalsDao dao = Server.server.getServiceRegistry().getDbService().stepbattlescore_FinalsDAO;
			for(StepBattleScoreTop16 sbs : scores.values()){
				if(sbs==null)
					continue;
				StepBattleScoreTop16 score = dao.getPlayerStepBattleScoreInfo(sbs.playerid, sbs.accountId);
				if(score!=null){
					dao.updateEntity(sbs);
				}
			}
			Server.server.getServiceRegistry().getStepBattleService().scores.clear();
		}
	}

}
