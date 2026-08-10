package peony.game.stepserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;
import peony.common.ClientSessionAsyncCall;
import peony.game.Actor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.net.ClientSession;
import peony.net.DispatchClientSession;
import peony.net.DispatchClientSessionService;
import peony.net.DispatchPacket;
import peony.net.Packet;
import peony.net.PacketHandler;

public class StepBattlePacketCall extends ClientSessionAsyncCall {

	private static final Logger log = Logger.getLogger(StepBattlePacketCall.class);
	protected int accountId;
	protected int playerId;
	protected int sessionId;
	protected IoSession ioSession;
	protected DispatchPacket dPacket;
	
	public StepBattlePacketCall(ClientSession session, int accountId, int playerId, int sessionId, 
			IoSession ioSession, DispatchPacket dPacket) {
		super(session);
		this.accountId = accountId;
		this.playerId = playerId;
		this.sessionId = sessionId;
		this.ioSession = ioSession;
		this.dPacket = dPacket;
	}

	public void callFinish() throws Exception {
		if(processStepPacket(dPacket, ioSession, accountId, playerId)){
			return;
		}
		DispatchClientSession ds = processDispatchPacket(accountId, playerId, sessionId, ioSession);
		ds.addPacket(dPacket.packet);
	}

	public void run() {
		addToClientSession();
	}
	
	public DispatchPacket getSignErrorPacket(int errorCode){
		Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SIGNUP_SERVER);
		pt.putInt(StepServer.PACKET_SIGN_0);
		pt.put(errorCode);
		DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
		dpt.accountId = accountId;
		dpt.playerId = playerId;
		return dpt;
	}
	
	public DispatchPacket getSignOKPacket(String code){
		Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SIGNUP_SERVER);
		pt.putInt(StepServer.PACKET_SIGN_1);
		pt.putString(code);
		DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
		dpt.accountId = accountId;
		dpt.playerId = playerId;
		return dpt;
	}
	
	@SuppressWarnings("unchecked")
	public boolean processStepPacket(DispatchPacket dPacket, IoSession session, int accountId, int playerId){
		if(dPacket.packet.getOpCode()==OpCode.CLIENTSERVER_STEPSERVER_INFO_CLIENT){
			//玩家下线或者掉线
			DispatchClientSessionService dispatchClientSessionService = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
			DispatchClientSession dSession = dispatchClientSessionService.getSession(StepServer.getStepBattleSessionId(accountId, playerId));
			try {
				Server.server.getServiceRegistry().getStepBattleService().updateDisconnectedPlayerRanking(playerId, accountId);
				if(dSession!=null){
					((Player)dSession.getClient()).removeFromWorld();
					dSession.close();
				}
				Server.server.getServiceRegistry().getStepBattleService().updateAllFinalsInstancesState();
			} catch (Exception e) {
			}finally{
				dispatchClientSessionService.removeClientSession(StepServer.getStepBattleSessionId(accountId, playerId));
				Server.server.getServiceRegistry().getStepBattleService().removeFromQueue(playerId);
				Server.server.getServiceRegistry().getStepBattleService().enterIndtanceRecord.remove(playerId);
				Server.server.getServiceRegistry().getStepBattleService().todaySigns_EveryDay.remove(new Integer(playerId));
			}
			log.info("[STEPBATTLEDISCONNECTED]PLAYERID["+playerId+"]ACC["+accountId+"]");
			return true;
		}else if(dPacket.packet.getOpCode()==OpCode.STEPSERVER_BATTLE_SIGNUP_CLIENT){
			//玩家报名跨服战场
			dPacket.packet.getInt();
			int level = dPacket.packet.getShort();
			byte type=dPacket.packet.get();//跨服赛类型
			String gameCode=dPacket.packet.getString();//gameCode
			StepBattleService stepBattleService = Server.server.getServiceRegistry().getStepBattleService();
			if(type==StepServer.STEPBATTLE_TYPE_NORMAL){//0-普通跨服战报名
				if(stepBattleService.canSignUp){
					DispatchClientSessionService dispatchClientSessionService = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
					if(stepBattleService.todaySigns.contains(new Integer(playerId))){
						Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SIGNUP_SERVER);
						pt.putInt(StepServer.PACKET_SIGN_0);
						pt.put(StepServer.PACKET_CAUSE_SIGN_4);
						DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
						dpt.accountId = accountId;
						dpt.playerId = playerId;
						session.write(dpt);
					}else 
						if(dispatchClientSessionService.getSession(StepServer.getStepBattleSessionId(accountId, playerId))==null){
						if(level>=StepBattleService.minPlayerLevel){
							Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SIGNUP_SERVER);
							pt.putInt(StepServer.PACKET_SIGN_1);
							pt.putString("");
							DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
							dpt.accountId = accountId;
							dpt.playerId = playerId;
							session.write(dpt);
							log.info("[STEPBATTLESIGNOK]PLAYERID["+playerId+"]ACC["+accountId+"]");
						}else{
							Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SIGNUP_SERVER);
							pt.putInt(StepServer.PACKET_SIGN_0);
							pt.put(StepServer.PACKET_CAUSE_SIGN_3);
							DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
							dpt.accountId = accountId;
							dpt.playerId = playerId;
							session.write(dpt);
						}
					}else{
						Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SIGNUP_SERVER);
						pt.putInt(StepServer.PACKET_SIGN_1);
						pt.putString("");
						DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
						dpt.accountId = accountId;
						dpt.playerId = playerId;
						session.write(dpt);
						log.info("[STEPBATTLESIGNOK]PLAYERID["+playerId+"]ACC["+accountId+"]");
					}
				}else{
					Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SIGNUP_SERVER);
					pt.putInt(StepServer.PACKET_SIGN_0);
					pt.put(StepServer.PACKET_CAUSE_SIGN_1);
					DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
					dpt.accountId = accountId;
					dpt.playerId = playerId;
					session.write(dpt);
				}
			}else if(type==StepServer.STEPBATTLE_TYPE_16){//1-常规16强跨服战报名
				if(stepBattleService.player2Instance.get(playerId)!=null){
					return true;
				}
				if(stepBattleService.currentWeek>=3){//第3周时提示不是16强赛了
					DispatchPacket dpt = getSignErrorPacket(StepServer.PACKET_CAUSE_SING_NOTTOP16);
					session.write(dpt);
					return true;
				}
				if(stepBattleService.canSignUp_EveryDay){//可以报名
					DispatchClientSessionService dispatchClientSessionService = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
					//不限制报名次数
					if(stepBattleService.queues_EveryDay.contains(new Integer(playerId))){
						DispatchPacket dpt = getSignErrorPacket(StepServer.PACKET_CAUSE_SIGN_4);
						session.write(dpt);
						log.info("[STEPBATTLEALREADYSIGNUP]PLAYERID["+playerId+"]ACC["+accountId+"]");
					}else if(StepBattleService.todaySignedTimes.containsKey(new Integer(playerId))
							&&StepBattleService.todaySignedTimes.get(new Integer(playerId))>=3){
						DispatchPacket dpt = getSignErrorPacket(StepServer.PACKET_CAUSE_SING_OVER3TIMES);
						session.write(dpt);
						log.info("[STEPBATTLEPACKETCALL]PLAYERID["+playerId+"]ACC["+accountId+"]");
					}else 
						if(dispatchClientSessionService.getSession(StepServer.getStepBattleSessionId(accountId, playerId))==null){
						if(level>=StepBattleService.minPlayerLevel){
							DispatchPacket dpt = getSignOKPacket("");
							session.write(dpt);
							log.info("[ORDINARYSTEPBATTLESIGNOK]PLAYERID["+playerId+"]ACC["+accountId+"]");
						}else{
							DispatchPacket dpt = getSignErrorPacket(StepServer.PACKET_CAUSE_SIGN_3);
							session.write(dpt);
						}
					}else{
						DispatchPacket dpt = getSignOKPacket("");
						session.write(dpt);
						log.info("[ORDINARYSTEPBATTLESIGNOK]PLAYERID["+playerId+"]ACC["+accountId+"]");
					}
				}else{//报名时间没到
					DispatchPacket dpt = getSignErrorPacket(StepServer.PACKET_CAUSE_SIGN_1);
					session.write(dpt);
				}
			}else if(type==StepServer.STEPBATTLE_TYPE_TOURNAMENT){//2-争霸赛报名
				if(stepBattleService.canSignUp_Finals){//可以报名参加争霸赛
					boolean canSignUp=stepBattleService.isFinalsPlayer(playerId,gameCode);
					DispatchClientSessionService dispatchClientSessionService = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
					//不限制报名次数
					if(stepBattleService.todaySigns_Finlas.contains(new Integer(playerId))){
						DispatchPacket dpt = getSignErrorPacket(StepServer.PACKET_CAUSE_SIGN_4);
						session.write(dpt);
						log.info("[STEPBATTLEALREADYSIGNUPFINALS]PLAYERID["+playerId+"]ACC["+accountId+"]");
					}else if(!canSignUp){
						DispatchPacket dpt = getSignErrorPacket(StepServer.PACKET_CAUSE_SING_NOFINALS16);
						session.write(dpt);
						log.info("[STEPBATTLEFINALSCANNOTSIGNUPFINALS]PLAYERID["+playerId+"]ACC["+accountId+"]");
					}else if(dispatchClientSessionService.getSession(StepServer.getStepBattleSessionId(accountId, playerId))==null){
						if(level>=StepBattleService.minPlayerLevel){
							DispatchPacket dpt = getSignOKPacket("");
							session.write(dpt);
							log.info("[ORDINARYSTEPBATTLESIGNOKFINALS]PLAYERID["+playerId+"]ACC["+accountId+"]");
						}else{
							DispatchPacket dpt = getSignErrorPacket(StepServer.PACKET_CAUSE_SIGN_3);
							session.write(dpt);
						}
					}else{
						DispatchPacket dpt = getSignOKPacket("");
						session.write(dpt);
						log.info("[STEPFINALSSIGNOK]PLAYERID["+playerId+"]ACC["+accountId+"]");
					}
				}else{
					DispatchPacket dpt = getSignErrorPacket(StepServer.PACKET_CAUSE_SIGN_1);
					session.write(dpt);
				}
			}
			return true;
		}else if(dPacket.packet.getOpCode()==OpCode.STEPSERVER_BATTLE_SCORE_CLIENT){
			dPacket.packet.get();
			byte type=dPacket.packet.get();//常规赛
			if(type==StepServer.STEPBATTLE_TYPE_NORMAL){//0-普通跨服战报名
				//请求跨服战场排行榜
				StepBattleService battleService = Server.server.getServiceRegistry().getStepBattleService();
				Packet pt = new Packet(OpCode.STEPSERVER_BATTLE_SCORE_SERVER);
				pt.putInt(0);
				int winnerSize = battleService.winners.size();
				if(winnerSize>=10)
					winnerSize = 10;
				pt.putShort(winnerSize);
				if(winnerSize>0){
					Actor[] wins = new Actor[winnerSize];
					Integer[] scores = new Integer[winnerSize];
					wins = battleService.winners.toArray(wins);
					scores = battleService.winnerScore.toArray(scores);
					for(int i=0;i<wins.length;i++){
						for(int j=i+1;j<wins.length;j++){
							if(scores[i].intValue()<scores[j].intValue()){
								Integer temp = scores[i];
								scores[i] = scores[j];
								scores[j] = temp;
								
								Actor a = wins[i];
								wins[i] = wins[j];
								wins[j] = a;
							}
						}
					}
					int count = 0;
					for(int index=0;index<wins.length;index++){
						count++;
						Actor actor = wins[index];
						pt.putShort(index+1);
						pt.putString(StepBattleService.getServerName(actor.gameCode));
						pt.putString(actor.name);
						pt.put(actor.faction);
						pt.putShort(scores[index]);
						if(count==10)
							break;
					}
				}
				DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
				dpt.accountId = accountId;
				dpt.playerId = playerId;
				session.write(dpt);
			}else if(type==StepServer.STEPBATTLE_TYPE_16){
				Server.server.getServiceRegistry().getDbService().schedule(new StepBattlePre16ListCall(null, session, playerId, accountId, dPacket));
			}else if(type==StepServer.STEPBATTLE_TYPE_TOURNAMENT){
				Server.server.getServiceRegistry().getDbService().schedule(new StepBattleFinalsListCall(null, session, playerId,accountId, "", dPacket,StepBattleFinalsListCall.TYPE_FINALS));
			}
		}else if(dPacket.packet.getOpCode()==OpCode.STEPSERVER_BATTLE_SCORE_FINALS_CLIENT){//争霸赛押注观战排行榜
			//押注第三周周日不能押注
			//第三周1-6之外1.常规赛期间不显示。2.第三周周日不能押注
//			StepBattleService service=Server.server.getServiceRegistry().getStepBattleService();
//			if(Time.currentWeekDay==1||service.currentWeek!=3){//第三周周日不能押注
//				Packet pt = new Packet(OpCode.STEPBATTLE_FINALS_BETANDWATCH_SERVER);
//				pt.putInt(StepServer.STEPBATTLE_BET_CANNOTBET);//通知本地服务器结果
//				pt.putInt(0);//请求扣除的金钱数
//				pt.putInt(0);//被押注的
//				pt.putInt(0);//被押注的
//				pt.putUTF("");//gameCode
//				DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
//				dpt.accountId = accountId;
//				dpt.playerId = playerId;
//				session.write(dpt);
//				return false;
//			}
			
			if(StepBattleService.currentWeek!=3){
				return false;
			}
			String gameCode="";
			try {
				Packet pt=dPacket.packet.clone();
				pt.get();
				gameCode=pt.getString();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			Server.server.getServiceRegistry().getDbService().schedule(new StepBattleFinalsListCall(null, session, playerId, accountId,gameCode, dPacket,StepBattleFinalsListCall.TYPE_TOP16));			
		}else if(dPacket.packet.getOpCode()==OpCode.STEPBATTLE_FINALS_BETANDWATCH_CLIENT){//押注和观战
			dPacket.packet.getInt();
			byte type=dPacket.packet.get();//类型(0-观战，1-押注)
			int targetPlayerId=dPacket.packet.getInt();//观战或押注的playerId
			int targetPlayerAccountId=dPacket.packet.getInt();//观战或押注的playerAccountId
			int betCoins=dPacket.packet.getInt();
			String sourcePlayerGameCode=dPacket.packet.getString();
			int sourcePlayerMoney=dPacket.packet.getInt();
			
			int sourcePlayerId=dPacket.playerId;
			StepBattleScoreTop16 sbs=getBetPlayer(targetPlayerId, targetPlayerAccountId);
			if(sbs!=null){
				if(type==0){//观战
					StepBattleScoreTop16 lastWatchPlayer=hadWatch(sourcePlayerId, sourcePlayerGameCode);
					int betresult=-1;
					if(lastWatchPlayer!=null){//已经有预约(预约为同一个人，更换另一个人)
						if(lastWatchPlayer.playerid==targetPlayerId&&lastWatchPlayer.accountId==targetPlayerAccountId){//预约为同一个人
							betresult=StepServer.STEPBATTLE_BETANDWATCH_HADWATCH;
						}else{//换一个预约对象
							betresult=StepServer.STEPBATTLE_BETANDWATCH_WATCHCHANGE;
						}
					}else{//直接预约观战
						betresult=StepServer.STEPBATTLE_BETANDWATCH_CANWATCH;
					}
					Packet pt = new Packet(OpCode.STEPBATTLE_FINALS_BETANDWATCH_SERVER);
					pt.putInt(betresult);//通知本地服务器结果
					pt.putInt(-1);//请求扣除的金钱数观战时不需要此参数
					pt.putInt(targetPlayerId);//被观战的
					pt.putInt(targetPlayerAccountId);//被观战的
					pt.putUTF(sourcePlayerGameCode);//gameCode
					DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
					dpt.accountId = accountId;
					dpt.playerId = playerId;
					session.write(dpt);
				}else if(type==1){//押注
					int sourcePlayerBetCoins=sbs.getPlayerBetCoins(sourcePlayerId,sourcePlayerGameCode);
					int betresult=-1;
					if(Time.currentWeekDay==1||StepBattleService.currentWeek!=3){//第三周周日不能押注
						betresult=StepServer.STEPBATTLE_BET_CANNOTBET;
					}else
					if(betCoins>sourcePlayerMoney){//金币不足
						betresult=StepServer.STEPBATTLE_BETANDWATCH_LESSMONEY;
					}else if(sourcePlayerBetCoins+betCoins<=StepBattleService.maxBetCoins){//可以扣除
						betresult=StepServer.STEPBATTLE_BETANDWATCH_DECMONEYOK;
					}else if(betCoins>StepBattleService.maxBetCoins||(sourcePlayerBetCoins+betCoins>StepBattleService.maxBetCoins)){//押注额不能超过100万
						betresult=StepServer.STEPBATTLE_BETANDWATCH_MORETHEN100WAN;
					}
					Packet pt = new Packet(OpCode.STEPBATTLE_FINALS_BETANDWATCH_SERVER);
					pt.putInt(betresult);//通知本地服务器结果
					pt.putInt(betCoins);//请求扣除的金钱数
					pt.putInt(targetPlayerId);//被押注的
					pt.putInt(targetPlayerAccountId);//被押注的
					pt.putUTF(sourcePlayerGameCode);//gameCode
					pt.putInt(sourcePlayerBetCoins);//已经押注的金钱
					DispatchPacket dpt = new DispatchPacket(dPacket.id, pt);
					dpt.accountId = accountId;
					dpt.playerId = playerId;
					session.write(dpt);
					log.info("[STEPBATTLEDECMONEYSTEPFIRST]BETRESULT["+betresult+"]PLAYERID["+playerId+"]ACC["+accountId+"]PLAYERGAMECODE["+sourcePlayerGameCode+"]HADBETCOINS["+sourcePlayerBetCoins+"]BETCOINS["+betCoins+"]TARGETPLAYERID["+targetPlayerId+"]TARGETPLAYERACCOUNTID["+targetPlayerAccountId+"]");
				}
			}
		}else if(dPacket.packet.getOpCode()==OpCode.STEPBATTLE_FINALS_BETANDWATCH_CLIENTTOSTEPSERVER){//观战或押注成功
			StepBattleService service=Server.server.getServiceRegistry().getStepBattleService();
			dPacket.packet.getInt();
			byte type=dPacket.packet.get();
			int betcoins=dPacket.packet.getInt();
			int targetPlayerId=dPacket.packet.getInt();//观战或押注的playerId
			int targetPlayerAccountId=dPacket.packet.getInt();//观战或押注的playerAccountId
			String sourcePlayerGameCode=dPacket.packet.getString();//gamecode
			int sourcePlayerId=dPacket.playerId;
			if(type==1){//押注成功
				StepBattleScoreTop16 sbs=getBetPlayer(targetPlayerId, targetPlayerAccountId);
				if(sbs!=null){
					sbs.addBet(sourcePlayerId,sourcePlayerGameCode, betcoins);
					Collections.sort(service.finalsPlayers, new StepBattleScoreTop16Sort());
					Server.server.getServiceRegistry().getDbService().schedule(new StepBattleFinalsListCall(null, session, playerId,accountId, sourcePlayerGameCode, dPacket,StepBattleFinalsListCall.TYPE_TOP16));
					log.info("[STEPBATTLEDECMONEYOK]PLAYERID["+playerId+"]ACC["+accountId+"]SOURCEPLAYERGAMECODE["+sourcePlayerGameCode+"]DECMONEY["+betcoins+"]TARGETPLAYERID["+targetPlayerId+"]TARGETPLAYERACCOUNTID["+targetPlayerAccountId+"]TARGETPLAYERGAMECODE["+sbs.gameCode+"]");
				}
			}else if(type==0){//预约观战成功
				StepBattleScoreTop16 sbs=getBetPlayer(targetPlayerId, targetPlayerAccountId);
				if(!sbs.hadWatch(sourcePlayerId, sourcePlayerGameCode)){
					sbs.addWatchPlayer(sourcePlayerId, sourcePlayerGameCode);
				}
			}
			Server.server.getServiceRegistry().getDbService().schedule(new StepBattleScoreFinalsSaveCall(null, service.finalsPlayers,StepBattleScoreFinalsSaveCall.TYPE_SAVE));
		}else if(dPacket.packet.getOpCode()==OpCode.STEPBATTLE_CHECKSTEPCLIENT_PLAYERSTATE_STEPCLIENT){//检测本地服务器的玩家状态
			try {
				Packet pt=dPacket.packet.clone();
				synchronized (this) {
					int playerSize=pt.getInt();
					List<String> playerInfos=new ArrayList<String>();
					for(int i=0;i<playerSize;i++){
						int playerIdTemp=pt.getInt();
						String gameCodeTemp=pt.getString();
						int onLineState=pt.getInt();
						String info=playerIdTemp+","+gameCodeTemp+","+onLineState;
						log.info("[STEPBATTLE_CHECKSTEPCLIENT_PLAYERSTATE_STEPCLIENT]PLAYERINFO["+info+"]");
						playerInfos.add(info);
					}
					StepBattleService service=Server.server.getServiceRegistry().getStepBattleService();
					for(StepBattleScoreTop16 sbs16:service.signUpPlayers){
						for(String playerInfo:playerInfos){
							String[]  onePlayerInfo=playerInfo.split(",");
							int playerIdTemp=Integer.parseInt(onePlayerInfo[0]);
							String playerGameCode=onePlayerInfo[1];
							int onlineState=Integer.parseInt(onePlayerInfo[2]);
							if(sbs16.playerid==playerIdTemp&&playerGameCode.equals(sbs16.gameCode)){
								sbs16.onLineState=onlineState;
								log.info("[STEPBATTLEPACKETCALL_SETSIGNUPPLAYERSONLINESTATE]PLAYERID["+sbs16.playerid+"]GAMECODE["+sbs16.gameCode+"]ONLINESTATE["+sbs16.onLineState+"]");
							}
						}
					}
				}
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	/***
	 * 被押注玩家的信息
	 * @param playerId
	 * @param accountId
	 * @return
	 */
	public StepBattleScoreTop16 getBetPlayer(int playerId,int accountId){
		StepBattleService service=Server.server.getServiceRegistry().getStepBattleService();	
		for(StepBattleScoreTop16 sbs:service.finalsPlayers){
			if(sbs.playerid==playerId&&sbs.accountId==accountId){
				return sbs;
			}
		}
		return null;
	}
	/***
	 * 是否已经预约观战
	 * @param sourcePlayerId
	 * @param sourcePlayerGameCode
	 * @return
	 */
	public StepBattleScoreTop16 hadWatch(int sourcePlayerId,String sourcePlayerGameCode){
		StepBattleService service=Server.server.getServiceRegistry().getStepBattleService();	
		for(StepBattleScoreTop16 sbs:service.finalsPlayers){
			if(sbs.hadWatch(sourcePlayerId, sourcePlayerGameCode)){
				return sbs;
			}
		}
		return null;
	}
	
	
	public DispatchClientSession processDispatchPacket(int accountId, int playerId, int sessionId, IoSession session){
		DispatchClientSessionService dispatchClientSessionService = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
		DispatchClientSession ds = dispatchClientSessionService.getSession(StepServer.getStepBattleSessionId(accountId, playerId));
		PacketHandler handler = Server.server.getServiceRegistry().getPacketHandlerService().getPlayerHandler();
		if(ds==null){
			ds = new DispatchClientSession(StepServer.getStepBattleSessionId(accountId, playerId), dispatchClientSessionService, session, handler);
		}
		if(sessionId!=0){
			ds.id = sessionId;
		}
		ds.session = session;
		Player player = (Player)ds.getClient();
		if(player!=null && !player.acceptMoving)
			player.acceptMoving = true;
		if(player!=null && !player.isInStep)
			player.isInStep = true;
		return ds;
	}
}

@SuppressWarnings("unchecked")
class StepBattleScoreTop16Sort implements Comparator {
	public int compare(Object o1, Object o2) {
		StepBattleScoreTop16 p1 = (StepBattleScoreTop16) o1;
		StepBattleScoreTop16 p2 = (StepBattleScoreTop16) o2;
		if(p2.getBet()>p1.getBet()){
			return 1;
		}else if(p2.getBet()<p1.getBet()){
			return -1;
		}else{
			return 0;
		}
	}
}

@SuppressWarnings("unchecked")
class StepBattleScoreTop16SortRanking implements Comparator {
	public int compare(Object o1, Object o2) {
		StepBattleScoreTop16 p1 = (StepBattleScoreTop16) o1;
		StepBattleScoreTop16 p2 = (StepBattleScoreTop16) o2;
		if(p2.getRanking()>p1.getRanking()){
			return 1;
		}else if(p2.getRanking()<p1.getRanking()){
			return -1;
		}else{
			return 0;
		}
	}
}