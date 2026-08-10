package peony.game.stepserver;

import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.Server;
import peony.net.ClientSession;

public class StepBattleScoreFinalsSaveCall extends ClientSessionAsyncCall {

	public static int TYPE_TOP16=0;
	public static int TYPE_FINALS=1;
	public static int TYPE_SAVE=2;
	public static int TYPE_UPDATEFINALSRANKING=3;//更新争霸赛名次
	public int type;
	protected List<StepBattleScoreTop16> scores;
	public StepBattleScoreFinalsSaveCall(ClientSession session,List<StepBattleScoreTop16> scores,int type) {
		super(session);
		this.scores=scores;
		this.type=type;
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		synchronized (Server.server.getServiceRegistry().getStepBattleService()) {
			if(type==TYPE_FINALS){
				StepBattleScore_FinalsDao dao=Server.server.getServiceRegistry().getDbService().stepbattlescore_FinalsDAO;
				dao.deleteAllScore();
				for(StepBattleScoreTop16 sbs : scores){
					if(sbs==null)
						continue;
					StepBattleScoreTop16 score = dao.getPlayerStepBattleScoreInfo(sbs.playerid, sbs.accountId);
					if(score==null){
						dao.newEntity(sbs);
					}else{
						dao.updateEntity(sbs);
					}
				}
			}else if(type==TYPE_TOP16){
				StepBattleScoreDao dao=Server.server.getServiceRegistry().getDbService().stepbattlescoreDAO;
				dao.deleteAllScore();
			}else if(type==TYPE_SAVE){
				StepBattleScore_FinalsDao dao=Server.server.getServiceRegistry().getDbService().stepbattlescore_FinalsDAO;
				for(StepBattleScoreTop16 sbs : scores){
					if(sbs==null)
						continue;
					StepBattleScoreTop16 score = dao.getPlayerStepBattleScoreInfo(sbs.playerid, sbs.accountId);
					if(score==null){
						dao.newEntity(sbs);
					}else{
						dao.updateEntity(sbs);
					}
				}
			}else if(type==TYPE_UPDATEFINALSRANKING){
				StepBattleScore_FinalsDao dao=Server.server.getServiceRegistry().getDbService().stepbattlescore_FinalsDAO;
				for(StepBattleScoreTop16 sbs : scores){
					if(sbs==null)
						continue;
					StepBattleScoreTop16 score = dao.getPlayerStepBattleScoreInfo(sbs.playerid, sbs.accountId);
					if(score==null){
						dao.newEntity(sbs);
					}else{
						dao.updateEntity(sbs);
					}
				}
			}
		}
	}

}
