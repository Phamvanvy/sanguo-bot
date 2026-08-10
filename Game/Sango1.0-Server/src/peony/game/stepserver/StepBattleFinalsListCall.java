package peony.game.stepserver;

import java.util.List;

import org.apache.mina.common.IoSession;

import peony.common.ClientSessionAsyncCall;
import peony.game.OpCode;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.DispatchPacket;
import peony.net.Packet;

public class StepBattleFinalsListCall extends ClientSessionAsyncCall {

	protected IoSession ioSession;
	protected int playerId;
	protected int accountId;
	protected DispatchPacket dPacket;
	protected String gameCode;
	
	/**获取列表的类型*/
	protected int listType;
	public static int TYPE_TOP16=0;//16强列表(按胜场数排名)
	public static int TYPE_FINALS=1;//争霸赛列表(按最后结果排名ranking)
	
	/**争霸赛排名*/
	public static int finalsRanking[]={16,8,4,2,1};
	
	public StepBattleFinalsListCall(ClientSession session, IoSession ioSession, int playerId, int accountId,String gamecode, DispatchPacket dPacket,int listType) {
		super(session);
		this.ioSession = ioSession;
		this.playerId = playerId;
		this.gameCode = gamecode;
		this.dPacket = dPacket;
		this.listType=listType;
		this.accountId=accountId;
	}

	public void callFinish() throws Exception {
	}

	public void run() {
		synchronized (Server.server.getServiceRegistry().getStepBattleService()) {
			if(listType==TYPE_TOP16){
				StepBattleService service=Server.server.getServiceRegistry().getStepBattleService();
				Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SCORE_FINALS_SERVER);
				pt.putInt(0);
				int winnerSize = service.finalsPlayers.size();
				pt.putShort(winnerSize);
				if(winnerSize>0){
					for(StepBattleScoreTop16 score:service.finalsPlayers){
						pt.putUTF(StepBattleService.getServerName(score.gameCode));
						pt.putUTF(score.name);
						pt.putInt(score.playerid);
						pt.putInt(score.accountId);
						String bets=score.getBet()+"";
						pt.putUTF(bets);
						int betsOwner=score.getPlayerBetCoins(playerId, gameCode);
						pt.putInt(betsOwner);
					}
				}
				pt.put(0);//是否观战
				pt.putInt(0);//playerid
				pt.putInt(0);//accountid
				DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
				dpt.accountId = accountId;
				dpt.playerId = playerId;
				ioSession.write(dpt);
			}else if(listType==TYPE_FINALS){
				StepBattleScore_FinalsDao dao=Server.server.getServiceRegistry().getDbService().stepbattlescore_FinalsDAO;
				Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SCORE_SERVER);
				pt.putInt(0);
				List<StepBattleScoreTop16> scores = /*service.finalsPlayers;*/dao.getFinalsPlayersList();
				int winnerSize = 0;
				if(scores!=null){
					winnerSize = scores.size();
				}
//				int count=0;
//				for(int i=0;i<scores.size();i++){
//					if(scores.get(i).ranking>0){
//						count++;
//					}
//				}
//				if(count==0){
//					winnerSize=0;
//				}
				if(!StepBattleService.finalsEnd&&StepBattleService.currentWeek==3){
					winnerSize=0;
				}
				pt.putShort(winnerSize);
				if(winnerSize>0){
					int rankingnum=0;
					for(int i=0;i<winnerSize;i++){
						StepBattleScoreTop16 score=scores.get(i);
						if(score.ranking>0&&(i==0||i==1)){
							rankingnum=i+1;
						}else{
							rankingnum=3;
						}
						pt.putShort(rankingnum/*score.ranking*/);
						pt.putString(StepBattleService.getServerName(score.gameCode));
						pt.putString(score.name);
						pt.put(score.faction);
						pt.putShort(score.winCount);
					}
				}
				DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
				dpt.accountId = accountId;
				dpt.playerId = playerId;
				ioSession.write(dpt);
			
			}
		}
	}
}
