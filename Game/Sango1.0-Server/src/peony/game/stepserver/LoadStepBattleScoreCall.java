package peony.game.stepserver;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;

public class LoadStepBattleScoreCall extends ClientSessionAsyncCall {
	private static final Logger log = Logger.getLogger(LoadStepBattleScoreCall.class);
	protected Player player;
	
	public LoadStepBattleScoreCall(ClientSession session, Player player) {
		super(session);
		this.player = player;
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		if(player!=null && player.stepType==StepServer.STEPBATTLE_TYPE_16){
			StepBattleService service = Server.server.getServiceRegistry().getStepBattleService();
			synchronized (service) {
				StepBattleScore sbs = service.scores.get(StepServer.getStepBattleSessionId(player.accountId, player.id));
				if(sbs==null){
					StepBattleScoreDao dao = Server.server.getServiceRegistry().getDbService().stepbattlescoreDAO;
					sbs = dao.getPlayerStepBattleScoreInfo(player.id, player.accountId);
					service.scores.put(StepServer.getStepBattleSessionId(player.accountId, player.id), sbs);
					if(sbs!=null){
						log.info("[LOADSTEPBATTLESCORECALL1]PLAYERID["+sbs.playerid+"]acc["+sbs.accountId+"]GAMECODE["+sbs.gameCode+"]WINCOUNT["+sbs.winCount+"]TIMER["+sbs.time+"]");
					}
				}else{
					log.info("[LOADSTEPBATTLESCORECALL2]PLAYERID["+sbs.playerid+"]acc["+sbs.accountId+"]GAMECODE["+sbs.gameCode+"]WINCOUNT["+sbs.winCount+"]TIMER["+sbs.time+"]");
				}
			}
		}
	}

}
