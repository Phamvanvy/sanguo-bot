package peony.game.stepserver;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;

import peony.game.GameObject;
import peony.game.Instance;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.net.DispatchPacket;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class StepBattleInstanceFinals extends StepBattleInstance implements Instance, ServiceEventListener {

	private static final Logger log = Logger.getLogger(StepBattleInstanceFinals.class);
	protected VMap map = null; //副本对应的地图
	protected List<Integer> players = new ArrayList<Integer>(); //副本中的玩家集合
	protected int instanceId; //副本instanceId
	protected int startTime; //副本创建时间
	protected int battleStartTime; //战斗开始时间
	protected int state; //当前战场状态
	protected static int STATE_WAIT = 0; //战场准备状态
	protected static int STATE_START = 1; //战场开始状态
	public static int STATE_END = 2; //战场结束状态
	protected StepBattleService manager; //副本管理器
	protected int lastUpdateTime; //上次检测时间
	protected static int[][] relivePositions = new int[][]{{437,295},{493,537}};  //副本中复活位置
	protected static int safeDistance = 300; //安全距离
	protected Map<Integer, Boolean> safes = new HashMap<Integer, Boolean>();
	protected Map<Integer, Integer> lastEnterTimes = new HashMap<Integer, Integer>();
	protected Map<Integer, Integer> totalTimes = new HashMap<Integer, Integer>();
	
	/**失败者的被押注金钱数*/
	protected long loserBetCoins;
	public boolean hadGiveLoserRanking=false;//称号标志已经增加
	
	protected static int minPlayerLevel = 65; //允许报名跨服争霸赛的最低玩家级别(可以不用)
	protected static int minEnterPlayers = 2; //每场参战人数2人
	
	protected boolean isDispatch = false;
	protected boolean dispatchOk = false;
	
	protected boolean waitDispatch=false;
	
	public boolean hadSendMessage=false;//给胜利者私聊
	
	public static boolean isTrans = false;
	
	
	public StepBattleInstanceFinals(VMap map, StepBattleService manager){
		this.map = map;
		this.map.instance = this;
		this.instanceId = StepBattleInstance.ids.incrementAndGet();
		this.startTime = Time.currTime;
		this.state = STATE_WAIT;
		this.manager = manager;
		Server.server.getEventManager().registerListener(this);
	}
	
	public void addPlayer(Player player) throws VMapException {
		players.add(player.id);
		if(players.size()==StepBattleService.finalMaxPlayers){
			this.state = STATE_START;
			this.battleStartTime = Time.currTime;
		}
		this.safes.put(Integer.valueOf(player.id), Boolean.valueOf(false));
		lastEnterTimes.put(Integer.valueOf(player.id), startTime + 60000);
	}

	public int getId() {
		return this.instanceId;
	}

	public VMap getMap(int mapId) {
		if(map.getId() == mapId){
			return map;
		}
		return null;
	}

	public String getName() {
		return map.mapDef.mapInfo.name;
	}

	public void loadingFinished(Player player) {
		
	}
	

	public void removePlayer(Player player) {//处理掉线玩家称号
		if(player!=null){
			if(players.size()==2){//两个人有一个掉线即为失败
				if(!hadSend){//如果没有发送过奖金
					sendWinnerBetCoins(player);
					processLoser(player);
					String playerInfo=player.id+player.gameCode;
					if(manager.disConnetionPlayers.contains(playerInfo)){//奖金不会滚动到冠军头上
						manager.disConnetionPlayers.remove(playerInfo);
						log.info("[STEPBATTLEINSTANCEFINALSREMOVEDISCONNETIONPLAYER]PLAYERID["+player.id+"]GAMECODE["+player.gameCode+"]");
					}
				}
			}
		}
		if(player!=null){
			players.remove(new Integer(player.id));
			manager.player2Instance.remove(new Integer(player.id));
			log.info("[STEPTREMOVEPLAYER2INSTANCEFINALS]PLAYERID["+player.id+"]");
		}
	}
	public void update(int diff) {
		if(map != null){
			map.update(diff);
		}
		updatePlayers();
		updateNormalBattle(diff);
		if(state==STATE_END){
			endInstance();
		}
	}
	public int getPlaye(){
		for(int i=0;i<players.size();i++){
			if(players.get(i)!=null){
				return players.get(i);
			}
		}
		return -1;
	}
	
	public boolean hadSend=false;
	protected void updateNormalBattle(int diff){
		if(Time.currTime-lastUpdateTime>10000){
			if(state!=STATE_END){
				int leaving = players.size();
				if(leaving==1){//等待第传送至下一轮的战场
					startTime=Time.currTime;
					int playerId=getPlaye();
					Player player = ObjectAccessor.getPlayer(playerId);
					if(player!=null){
						if(!hadSendMessage){
							Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
							pt.putInt(StepClientPacketCall.TYPE_NOTIFYFINALSPLAYER);
							pt.putString("恭喜您在本场争霸赛中获得胜利，系统正在为您匹配下一场的对手，请您耐心等待，不要下线。");
							player.send(pt);
							hadSendMessage=true;
						}
						if(StepBattleService.allInstanceEnd/*manager.isCurrRoundInstancesFinalsEnd()*/){
							StepBattleInstanceFinals instance=manager.getUsableInstanceExcept(instanceId);
							if(instance!=null && !isDispatch && !isTrans){
								try {
									if(instance.players.get(0)!=null){
										Player otherPlayer = ObjectAccessor.getPlayer(instance.players.get(0));
										if(otherPlayer!=null){
											int otherMinorFaction = otherPlayer.minorFaction;
											player.minorFaction = (otherMinorFaction==1 ? 2 : 1);
											player.setHp(player.maxhp, true);
											player.setMp(player.maxmp, true);
											int[] positions = manager.getInitPosition(player.minorFaction);
											manager.currentInstances.put(player.id, this);
											manager.futureEnterInstances.put(player.id, instance);
											StepBattleFinalsGoMapCall call = new StepBattleFinalsGoMapCall(player,
													new int[]{StepBattleService.mapId,positions[0],positions[1]}, instance, this);
											Server.server.getWorld().schedule(call);
											isDispatch = true;
											instance.waitDispatch=true;
											isTrans = true;
											log.info("[STEPGOOTHERINSTANCE]PLAYERID["+player.id+"]PLAYERNAME["+player.name+"]INSTANCEID["+this.instanceId+","+instance.instanceId+"]");
										}
									}
								} catch (Exception e) {
								}
							}
						}
					}
					return;
				}else if(Time.currTime>startTime+StepBattleService.delayTime&&leaving==2){
					Integer[] playerIds = new Integer[players.size()];
					playerIds = players.toArray(playerIds);
					for(int i=0;i<playerIds.length;i++){
						for(int j=i+1;j<playerIds.length;j++){
							Player player = ObjectAccessor.getPlayer(playerIds[i]);
							Player player1 = ObjectAccessor.getPlayer(playerIds[j]);
							if(player!=null && player1!=null){
								if(player.hp<player1.hp){
									Integer temp = playerIds[i];
									playerIds[i] = playerIds[j];
									playerIds[j] = temp;
								}
							}
						}
					}
					for(int i=playerIds.length-1;i>0;i--){
						Player player = ObjectAccessor.getPlayer(playerIds[i]);
						if(player!=null&&i!=0){
							playerBattleEnd(player);
						}
					}
					log.info("[STEPFINALSOUTTIME]PLAYERID");
				}else if(leaving==2){
					hadSend=false;
				}
			}
			if(dispatchOk)
				players.clear();
			lastUpdateTime = Time.currTime;
		}
	}
	
	/**重置战场开始时间*/
	protected void refreshStartTime(){
		startTime=Time.currTime;
		this.loserBetCoins=0;//清空上一轮失败玩家的押注金钱
		this.hadGiveLoserRanking=false;//失败者称号重新开始计算,防止重复给称号
		hadSend=false;
		hadSendMessage=false;
		for(int i=0;i<players.size();i++){//血蓝加满
			Player player=ObjectAccessor.getPlayer(players.get(i));
			if(player!=null){
				player.setHp(player.maxhp, true);
				player.setMp(player.maxmp, true);
			}
		}
		Player player0=ObjectAccessor.getPlayer(players.get(0));
		if(player0!=null){
			int[] positions0 = manager.getInitPosition(player0.minorFaction);
			try {
				player0.goMap(StepBattleService.mapId, positions0[0], positions0[1]);
			} catch (VMapException e) {
			}
		}
	}
	
	/**
	 * 获取所有玩家的押注百分比
	 * @param playerId
	 * @param gameCode
	 * @return
	 */
	public List<String> getWinnerBetPlayersPer(int playerId,String gameCode){
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(10);
		List<String> allBetPlayers=new ArrayList<String>();
		StepBattleScoreTop16 winnerSbs=getPlayerBets(playerId, gameCode);
		if(winnerSbs!=null){
			long allbet=winnerSbs.getBet();
			String[] allPlayerBet=winnerSbs.getAllPlayersBetCoins();
			if(allPlayerBet!=null){
				for(int i=0;i<allPlayerBet.length;i++){
					if(allPlayerBet[i]!=null&&!allPlayerBet[i].equals("")){
						int betcoins=Integer.parseInt(allPlayerBet[i].split(",")[2]);
						String result = numberFormat.format((float)betcoins/(float)allbet);
						allBetPlayers.add(allPlayerBet[i]+","+new Float(result));
					}
				}
			}
		}
		return allBetPlayers;
	}
	
	/**
	 * 获取争霸赛玩家金钱押注数
	 * @param playerId
	 * @param gameCode
	 * @return
	 */
	public StepBattleScoreTop16 getPlayerBets(int playerId,String gameCode){
		for(StepBattleScoreTop16 sbsTemp:manager.finalsPlayers){
			if(sbsTemp!=null&&sbsTemp.playerid==playerId&&sbsTemp.gameCode.equals(gameCode)){
				return sbsTemp;
			}
		}
		return null;
	}
	
	
	
	protected void updatePlayers(){
		if(Time.currTime-startTime>60000){
			try {
				for(int playerId : players){
					Player player = ObjectAccessor.getPlayer(playerId);
					if(player!=null && !player.loadFinshed)
						continue;
					if(lastEnterTimes.get(playerId)!=null  && lastEnterTimes.get(playerId).intValue()>0){
						int lastTime = lastEnterTimes.get(playerId).intValue();
						if(totalTimes.get(playerId)!=null){
							totalTimes.put(playerId, totalTimes.get(playerId).intValue()+(Time.currTime-lastTime));
						}else{
							totalTimes.put(playerId, (Time.currTime-lastTime));
						}
						lastEnterTimes.put(playerId, Time.currTime);
					}
					//争霸赛取消限制区域失血判断
//					if(totalTimes.get(playerId)!=null && totalTimes.get(playerId)>=3000){
//						if(player!=null){
//							int decHp = player.maxhp /4;
//							player.setHp(player.hp - decHp < 0 ? 0 : player.hp - decHp, true);
//							totalTimes.put(playerId, totalTimes.get(playerId).intValue()-3000);
//							if(player.hp<=0){
//								if(player.isAlive())
//									player.die(player);
//							}
//						}
//					}
				}
			} catch (Exception e) {
			}
		}
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_UNIT_DIE
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_UNIT_DIE:
			processPlayerDie((Unit)event.param1, (Unit)event.param2);
			break;
		}
	}
	
	protected void processPlayerDie(Unit dier, Unit killer){
		if(dier!=null && dier instanceof Player){
			Player dPlayer = (Player)dier;
			if(isInInstance(dPlayer.id)){
				playerBattleEnd(dPlayer);
			}
		}
	}
	
	public void processLoser(Player dPlayer){/*
		//争霸赛单场比赛结束后，输的一方的支持者将收到私聊：您支持的xxx在争霸赛中不幸落败。
		StepBattleScoreTop16 sbs=getPlayerBets(dPlayer.id, dPlayer.gameCode);
		if(sbs!=null){
			String[] betPlayers=sbs.getAllPlayersBetCoins();
			if(betPlayers!=null&&betPlayers.length>0){
				Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
				pt.putInt(StepServer.TYPE_NOTYFYPLAYERSBACKER);
				pt.putString("");
				pt.putUTF(MessageFormat.format("您支持的{0}在争霸赛中不幸落败。",sbs.name));
				int size=betPlayers.length;
				pt.putInt(size);
				for(int i=0;i<betPlayers.length;i++){
					String[] playerInfo=betPlayers[i].split(",");
					if(playerInfo!=null&&playerInfo.length>0){
						if(playerInfo[0]!=null&&playerInfo[1]!=null){
							int playerId=Integer.parseInt(playerInfo[0]);//押注玩家的ID
							String gameCode=playerInfo[1];//押注玩家的GameCode
							pt.putInt(playerId);
							pt.putUTF(gameCode);
							log.info("[STEPBATTLEINSTANCEFINASNOTYFYPLAYERSBACKER]LOSERID["+dPlayer.id+"]LOSERGAMECODE["+dPlayer.gameCode+"]");
						}
					}
				}
				DispatchPacket dp = new DispatchPacket(0, pt);
				for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
					session.write(dp);
				}
			}
			
			//发送飞鸽通知输的玩家(鉴于您的精彩表现，将在比赛结束后通过飞鸽发送争霸赛16强的奖励，下个赛季期待您的更好表现。)
			Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
			pt.putInt(StepServer.TYPE_NOTYFYPLAYERSBACKER);
			pt.putString("");
			pt.putUTF("鉴于您的精彩表现，将在比赛结束后通过飞鸽发送争霸赛16强的奖励，下个赛季期待您的更好表现。");
			pt.putInt(1);
			pt.putInt(sbs.playerid);
			pt.putUTF(sbs.gameCode);
			DispatchPacket dp = new DispatchPacket(0, pt);
			for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
				session.write(dp);
			}
			log.info("[STEPBATTLEINSTANCEFINASNOTYFYPLAYER]LOSERID["+dPlayer.id+"]LOSERGAMECODE["+dPlayer.gameCode+"]");
		}
	*/}
	
	protected void playerBattleEnd(Player dPlayer){
		if(!hadGiveLoserRanking){
			hadGiveLoserRanking=true;
			for(StepBattleScoreTop16 sbs16:manager.finalsPlayers){//称号升级用于排序
				if(dPlayer.id==sbs16.playerid&&dPlayer.gameCode.equals(sbs16.gameCode)){
					sbs16.ranking+=1;
					log.info("[STEPLOSERRANKING]PLAYERID["+dPlayer.id+"]GAMECODE["+dPlayer.gameCode+"]PLAYERNAME["+dPlayer.name+"]");
					break;
				}
			}
		}
		sendWinnerBetCoins(dPlayer);
		dPlayer.setHp(dPlayer.maxhp/2, true);
		removePlayer(dPlayer);
		//通知client服务器玩家断开连接
		Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
		pt.putInt(StepServer.TYPE_BATTLE_END);
		pt.putString("");
		dPlayer.send(pt);
		dPlayer.refreshProperties(false);
		lastEnterTimes.remove(dPlayer.id);
		totalTimes.remove(dPlayer.id);
		dPlayer.removeFromWorld();
		removePlayer(dPlayer);
		dPlayer.session.close();
	}
	
	public void sendWinnerBetCoins(Player dPlayer) {
		StepBattleScoreTop16 sbs=getPlayerBets(dPlayer.id, dPlayer.gameCode);
		if(sbs!=null){
			loserBetCoins=sbs.getBet();
		}
		int playerId=-1;
		for(int id:players){
			if(id!=dPlayer.id){
				playerId=id;
				break;
			}
		}
		if(playerId!=-1&&!hadSend){
			Player player=ObjectAccessor.getPlayer(playerId);
			if(player!=null){
				for(StepBattleScoreTop16 sbs1:manager.finalsPlayers){//称号升级用于排序
					if(player.id==sbs1.playerid&&player.gameCode.equals(sbs1.gameCode)){
						sbs1.ranking+=2;
						log.info("[STEPWINNERRANKING]PLAYERID["+player.id+"]PLAYERNAME["+player.gameCode+"]");
					}
				}
				List<String> everyPlayerBet=getWinnerBetPlayersPer(player.id, player.gameCode);
				Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
				pt.putInt(StepServer.TYPE_FINALBATTLE_SENDREWARD);
				pt.putUTF("");
				//胜利者名字
				pt.putUTF(player.name);
				pt.putInt(everyPlayerBet.size());
				for(int i=0;i<everyPlayerBet.size();i++){
					String[] everyPlayerInfo=everyPlayerBet.get(i).split(",");
					if(everyPlayerInfo!=null){
						pt.putInt(Integer.parseInt(everyPlayerInfo[0]));//playerid
						pt.putUTF(everyPlayerInfo[1]);//gamecode
						pt.putInt(0);//betcoins
						int wincoinsPer=(int)(loserBetCoins*Float.parseFloat(everyPlayerInfo[3]));
						pt.putInt(wincoinsPer);//赢得的金币数量
						log.info("[SENDWINBETCOINS]WINNERPLAYERNAME["+player.name+"]TARGETPLAYERINFO["+everyPlayerBet.get(i)+"]LOSERBETCOINT["+loserBetCoins+"]");
					}
				}
				DispatchPacket dp = new DispatchPacket(0, pt);
				for(IoSession session : Server.server.getServiceRegistry().getStepServer().sessions){
					session.write(dp);
				}
			}
			hadSend=true;
		}
	}

	protected boolean isInInstance(int playerId){
		for(int id : players){
			if(id==playerId)
				return true;
		}
		return false;
	}
	
	protected int[] getRelivePosition(int minorFaction){
		if(Time.currTime%2==0)
			return relivePositions[0];
		return relivePositions[1];
	}
	
	protected void transPlayers(){
		
	}
	
	/** 副本结束时移除场景内NPC和怪物 */
	protected void removeAllGameObjects(){
		Iterator<GameObject> itor = map.instanceid2objects.iterator2();
		while (itor.hasNext()) {
			GameObject o = itor.next();
			if (o != null && o.getVMap() != null && o.type!=GameObject.TYPE_PLAYER) {
				o.removeFromWorld();
			}
		}
	}
	
	protected void endInstance(){
		removeAllGameObjects();
	}
	
	public void moveAt(Player player) {
		if(player!=null){
			int[] centers = this.map.getCenter();
			int distance = player.distance(centers[0], centers[1]);
			if(distance>safeDistance * safeDistance){
				boolean safe = ((Boolean) this.safes.get(Integer.valueOf(player.id))).booleanValue();
				if(safe){
					safes.put(Integer.valueOf(player.id), Boolean.valueOf(false));
					if(Time.currTime-this.battleStartTime>60000){
						lastEnterTimes.put(Integer.valueOf(player.id), new Integer(Time.currTime));
					}else{
						lastEnterTimes.put(Integer.valueOf(player.id), new Integer(startTime+60000));
					}
				}
			}else{
				boolean safe = ((Boolean) this.safes.get(Integer.valueOf(player.id))).booleanValue();
				if(!safe){
					safes.put(Integer.valueOf(player.id), Boolean.valueOf(true));
					lastEnterTimes.put(player.id, 0);
				}
			}
		}
	}

}
