package peony.game.stepserver;

import org.apache.mina.common.IoSession;
import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.DispatchPacket;
import peony.net.Packet;

public class StepBattlePre16ListCall extends ClientSessionAsyncCall {

	protected IoSession ioSession;
	protected int playerId;
	protected int accountId;
	protected DispatchPacket dPacket;
	
	public StepBattlePre16ListCall(ClientSession session, IoSession ioSession, int playerId, int accountId, DispatchPacket dPacket) {
		super(session);
		this.ioSession = ioSession;
		this.playerId = playerId;
		this.accountId = accountId;
		this.dPacket = dPacket;
	}

	public void callFinish() throws Exception {

	}

	public void run() {
		StepBattleService service = Server.server.getServiceRegistry().getStepBattleService();
		synchronized (service) {
			Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SCORE_SERVER);
			pt.putInt(0);
			StepBattleScore[] serieswincount = service.top16;
			
			StepBattleScore sbs_Player = service.scores.get(StepServer.getStepBattleSessionId(accountId, playerId));				
			int winnerSize =0;
			for(int i=0;i<serieswincount.length;i++){
				if(serieswincount[i]!=null){
					winnerSize++;
				}
			}
			pt.putShort(winnerSize);
			if(winnerSize>0){
				int count = 0;
				for(int index=0;index<serieswincount.length;index++){
					StepBattleScore sbs=(StepBattleScore)serieswincount[index];
					if(sbs!=null){
						count++;
						pt.putShort(index+1);
						pt.putString(StepBattleService.getServerName(sbs.gameCode));
						pt.putString(sbs.name);
						pt.put(sbs.faction);
						pt.putShort(sbs.winCount);
					}
				}
			}
			StepBattleScoreDao dao = Server.server.getServiceRegistry().getDbService().stepbattlescoreDAO;
			if(sbs_Player==null){
				sbs_Player = dao.getPlayerStepBattleScoreInfo(playerId, accountId);
			}
			int wincounts=(sbs_Player==null?0:sbs_Player.winCount);
			pt.putShort(wincounts);//¸öÈËÅÅÃû
			DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
			dpt.accountId = accountId;
			dpt.playerId = playerId;
			ioSession.write(dpt);
		}
	}

}
