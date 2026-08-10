package peony.game.stepserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.Logger;
import peony.game.Actor;
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
import peony.game.chat.ChatService;
import peony.net.DispatchClientSessionService;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 跨服战场副本
 * @author dchen
 */
public class StepBattleInstance implements Instance, ServiceEventListener {

	private static final Logger log = Logger.getLogger(StepBattleInstance.class);
	protected VMap map = null; //副本对应的地图
	protected List<Integer> players = new ArrayList<Integer>(); //副本中的玩家集合
	protected static AtomicInteger ids = new AtomicInteger(0); //副本instaceId生成器
	protected int instanceId; //副本instanceId
	protected int startTime; //副本创建时间
	protected int battleStartTime; //战斗开始时间
	protected int state; //当前战场状态
	protected static int STATE_WAIT = 0; //战场准备状态
	protected static int STATE_START = 1; //战场开始状态
	public static int STATE_END = 2; //战场结束状态
	protected StepBattleService manager; //副本管理器
	protected int lastUpdateTime; //上次检测时间
	protected Map<Integer, Integer> lastEnterTimes = new HashMap<Integer, Integer>();
	protected Map<Integer, Integer> totalTimes = new HashMap<Integer, Integer>();
	protected Map<Integer, Integer> dieTimes = new HashMap<Integer, Integer>(); //记录玩家死亡次数
	protected static int[][] relivePositions = new int[][]{{437,295},{493,537}};  //副本中复活位置
	protected static int safeDistance = 300; //安全距离
	protected Map<Integer, Boolean> safes = new HashMap<Integer, Boolean>();
	
	
	//每场跨服战结束的时间
	protected int instanceEndHour = 19; //战场结束时间-小时
	protected int instanceEndMin = 30; //战场结束时间-分钟
	
	public void setInstanceEndTime(int hour,int min){
		this.instanceEndHour=hour;
		this.instanceEndMin=min;
	}
	
	public StepBattleInstance(){
		
	}
	
	public StepBattleInstance(VMap map, StepBattleService manager){
		this.map = map;
		this.map.instance = this;
		this.instanceId = ids.incrementAndGet();
		this.startTime = Time.currTime;
		this.state = STATE_WAIT;
		this.manager = manager;
		dieTimes.clear();
		Server.server.getEventManager().registerListener(this);
	}
	
	public void addPlayer(Player player) throws VMapException {
		players.add(player.id);
		StepBattleService stepbattleservice=Server.server.getServiceRegistry().getStepBattleService();
		if(stepbattleservice.getStepBattleType()==StepServer.STEPBATTLE_TYPE_NORMAL){
			if(players.size()==StepBattleService.minEnterPlayers){
				this.state = STATE_START;
				this.battleStartTime = Time.currTime;
			}
		}else if(stepbattleservice.getStepBattleType()==StepServer.STEPBATTLE_TYPE_16){
			if(players.size()==StepBattleService.minEnterPlayers_16){
				this.state = STATE_START;
				this.battleStartTime = Time.currTime;
			}
		}else if(stepbattleservice.getStepBattleType()==StepServer.STEPBATTLE_TYPE_TOURNAMENT){
			if(players.size()==StepBattleService.finalMaxPlayers){
				this.state = STATE_START;
				this.battleStartTime = Time.currTime;
			}
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

	public void removePlayer(Player player) {
		if(player!=null){
			players.remove(new Integer(player.id));
			manager.player2Instance.remove(new Integer(player.id));
			log.info("[STEPTREMOVEPLAYER2INSTANCE]PLAYERID["+player.id+"]");
		}
	}
	/**询问玩家是否进入下一轮比赛*/
	public void askPlayerEnterNextRound(Player p1){
		int round=StepBattleService.todaySignedTimes.get(p1.id);
		StepBattleService service=Server.server.getServiceRegistry().getStepBattleService();
		if(round<3&&service.canSignUp_EveryDay){//前两轮进行提示，第三轮不提示
			Packet pt = new Packet(OpCode.OPENUI_SERVER);
			pt.putString("ui_npc_dialog");
			pt.putString("STEPSERVER_BATTLE_1V1_NEXTROUND|"+(3-round));
			p1.send(pt);
//		}else{
//			ChatService chatService = Server.server.getServiceRegistry().getChatService();
//			chatService.sendPrivateMessage(p1.id, "跨服战消耗甚大，您今日已参加三场比赛，还请暂且休兵，待明日再战。");
//			log.info("[STEPTBATTLECHATSEND]PLAYERID["+p1.id+"]"+"["+p1.name+"]");
		}
	}
	
	public void update(int diff) {
		if(map != null){
			map.update(diff);
		}
		updatePlayers();
		StepBattleService stepservice=Server.server.getServiceRegistry().getStepBattleService();
		if(stepservice.getStepBattleType()==StepServer.STEPBATTLE_TYPE_NORMAL){//普通
			updateNormalBattle(diff);
		}else if(stepservice.getStepBattleType()==StepServer.STEPBATTLE_TYPE_16){
			updatePre16Battle(diff);
		}
		if(state==STATE_END){
			endInstance();
		}
	}
	
	protected void updateNormalBattle(int diff){
		if(Time.currTime-lastUpdateTime>10000){
			if(state==STATE_START){
				int currHour = Time.currentHour;
				int currMin = Time.currentMin;
				int leaving = players.size();
				if(leaving==1){
					Player player = ObjectAccessor.getPlayer(players.get(0));
					if(player!=null){
						ChatService chatService = Server.server.getServiceRegistry().getChatService();
						chatService.sendPrivateMessage(player.id, "恭喜百战不殆的勇士，挺到了最后，这些经验是对您最好的褒奖。");
						
						Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
						pt.putInt(StepServer.TYPE_BATTLE_END);
						pt.putString("1");
						player.send(pt);
						lastEnterTimes.remove(player.id);
						totalTimes.remove(player.id);
						state = STATE_END;
						removeAllGameObjects();
						
						Actor actor = new Actor();
						actor.id = player.id;
						actor.accountId = player.accountId;
						actor.clazz = player.clazz;
						actor.exist = player.exist;
						actor.faction = player.faction;
						actor.level = player.level;
						actor.name = player.name;
						actor.rank = player.getRank();
						actor.sex = player.sex;
						actor.gameCode = player.gameCode;
						manager.addWinner(actor);
						player.removeFromWorld();
						removePlayer(player);
						player.session.close();
						DispatchClientSessionService dispatchClientSessionService = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
						dispatchClientSessionService.removeClientSession(StepServer.getStepBattleSessionId(player.accountId, player.id));
					}
					
					return;
				}else if(leaving==0){
					state=STATE_END;
				}else if(currHour>instanceEndHour || currHour==instanceEndHour && currMin>instanceEndMin){
					Integer[] playerIds = new Integer[players.size()];
					playerIds = players.toArray(playerIds);
					for(int i=0;i<playerIds.length;i++){
						for(int j=i+1;j<playerIds.length;j++){
							Player player = ObjectAccessor.getPlayer(playerIds[i]);
							Player player1 = ObjectAccessor.getPlayer(playerIds[j]);
							int dieCount = 0;
							try{dieCount = dieTimes.get(playerIds[i]);}catch(Exception e){}
							int dieCount1 = 0;
							try{dieCount1 = dieTimes.get(playerIds[j]);}catch(Exception e){}
							if(player!=null && player1!=null){
								if(dieCount>dieCount1 || dieCount==dieCount1 && player.hp<player1.hp){
									Integer temp = playerIds[i];
									playerIds[i] = playerIds[j];
									playerIds[j] = temp;
								}
							}
						}
					}
					for(int i=playerIds.length-1;i>0;i--){
						Player player = ObjectAccessor.getPlayer(playerIds[i]);
						if(player!=null){
							playerBattleEnd(player);
						}
					}
				}
			}
			lastUpdateTime = Time.currTime;
		}
	}
	
	protected boolean isInTop16(StepBattleScore sbs){
		if(sbs!=null){
			StepBattleService service = Server.server.getServiceRegistry().getStepBattleService();
			for(int i=0;i<service.top16.length;i++){
				if(service.top16[i]!=null&&sbs.accountId==service.top16[i].accountId&&sbs.playerid==service.top16[i].playerid){
					service.top16[i]=sbs;
					return true;
				}
			}
		}
		return false;
	}
	
	protected int getInsertIndex(){
		StepBattleService service = Server.server.getServiceRegistry().getStepBattleService();
		for(int i=0;i<service.top16.length;i++){
			if(service.top16[i]==null){
				return i;
			}
		}
		return -1;
	}
	
	protected void updatePre16Battle(int diff){
		if(Time.currTime-lastUpdateTime>10000){
			StepBattleService service = Server.server.getServiceRegistry().getStepBattleService();
			if(state==STATE_START){
				int currHour = Time.currentHour;
				int currMin = Time.currentMin;
				int leaving = players.size();
				if(leaving==1){
					Player player = ObjectAccessor.getPlayer(players.get(0));
					if(player!=null){
						askPlayerEnterNextRound(player);//询问玩家是否进入下一轮
						StepBattleScore sbs = null;
						synchronized (service) {
							sbs = service.scores.get(StepServer.getStepBattleSessionId(player.accountId, player.id));
						}
						if(sbs!=null){
							sbs.winCount++;
							sbs.time+=Time.currTime-startTime;
						}else{
							sbs=new StepBattleScore();
							sbs.accountId=player.accountId;
							sbs.faction=player.faction;
							sbs.playerid=player.id;
							sbs.name=player.name;
							sbs.time=Time.currTime-startTime;
							sbs.winCount=1;
							sbs.setGameCode(player.gameCode);
							synchronized (service) {
								service.scores.put(StepServer.getStepBattleSessionId(player.accountId, player.id), sbs);
							}
						}
						log.info("[STEPBATTLEINSTANCE1V1WIN]PLAYERID["+player.id+"]GAMECODE["+player.gameCode+"]CURRENTWINCOUNT["+sbs.winCount+"]TIMER["+sbs.time+"]CURRENTROUNDTIMER["+(Time.currTime-startTime)+"]");
						//如果在top10
						boolean canSort=false;
						if(isInTop16(sbs)){
							canSort=true;
						}else{
							int insertIndex=getInsertIndex();
							if(insertIndex!=-1){
								service.top16[insertIndex]=sbs;
								canSort=true;
							}else{
								if(sbs.winCount>service.top16[service.top16.length-1].winCount
										||(sbs.winCount==service.top16[service.top16.length-1].winCount&&sbs.time<service.top16[service.top16.length-1].time)){
									service.top16[service.top16.length-1]=sbs;
									canSort=true;
								}
							}
						}
						if(canSort){
							for(int i=0;i<service.top16.length;i++){
								for(int j=i+1;j<service.top16.length;j++){
									if(service.top16[i]!=null&&service.top16[j]!=null){
										if(service.top16[i].winCount<service.top16[j].winCount||
												(service.top16[i].winCount==service.top16[j].winCount&&service.top16[i].time>service.top16[j].time)){
											StepBattleScore temp=service.top16[i];
											service.top16[i]=service.top16[j];
											service.top16[j]=temp;
										}
									}
								}
							}
						}
						Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
						pt.putInt(StepServer.TYPE_BATTLE_END);
						int round=StepBattleService.todaySignedTimes.get(player.id);
						pt.putString(StepBattleService.STEPBATTLE_HINT_MORETHEN3ROUNDS+"|"+"0"+"|"+round);
						player.send(pt);
						log.info("[STEPBATTLEINSTANCE1V1WINNOTIFYCLIENTSENDGIFT]PLAYERID["+player.id+"]GAMECODE["+player.gameCode+"]ROUND["+round+"]");
						lastEnterTimes.remove(player.id);
						totalTimes.remove(player.id);
						state = STATE_END;
						removeAllGameObjects();
						
						Actor actor = new Actor();
						actor.id = player.id;
						actor.accountId = player.accountId;
						actor.clazz = player.clazz;
						actor.exist = player.exist;
						actor.faction = player.faction;
						actor.level = player.level;
						actor.name = player.name;
						actor.rank = player.getRank();
						actor.sex = player.sex;
						actor.gameCode = player.gameCode;
						player.removeFromWorld();
						removePlayer(player);
						player.session.close();
//						DispatchClientSessionService dispatchClientSessionService = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
//						dispatchClientSessionService.removeClientSession(StepServer.getStepBattleSessionId(player.accountId, player.id));
					}
					return;
				}else if(leaving==0){
					state=STATE_END;
				}else if((currHour>instanceEndHour || currHour==instanceEndHour && currMin>instanceEndMin)
						||(Time.currTime>=startTime+StepBattleService.delayTime)){
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
						if(player!=null){
							log.info("[STEPBATTLEINSTANCE1V1WINOUTTIMER]PLAYERID["+player.id+"]GAMECODE["+player.gameCode+"]RANKING["+i);
							askPlayerEnterNextRound(player);
							diePlayerBattleEnd(player, i);
						}
					}
				}
			}
			lastUpdateTime = Time.currTime;
		}
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
					if(totalTimes.get(playerId)!=null && totalTimes.get(playerId)>=3000){
						if(player!=null&&!player.isFear()){
							int decHp = player.maxhp /4;
							player.setHp(player.hp - decHp < 0 ? 0 : player.hp - decHp, true);
							totalTimes.put(playerId, 0);
							if(player.hp<=0){
								if(player.isAlive())
									player.die(player);
							}
						}
					}
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
			if(manager.getStepBattleType()==StepServer.STEPBATTLE_TYPE_NORMAL){//普通
				if(isInInstance(dPlayer.id)){
					int dieCount = 0;
					try{dieCount = dieTimes.get(dPlayer.id);}catch(Exception e){}
					if(dieCount<2){
						dieTimes.put(dPlayer.id, dieCount+1);
						if (dPlayer.map.map != null) {
							int[] reliveOption = getRelivePosition(dPlayer.minorFaction);
							try {
								dPlayer.relive(dPlayer.maxhp, dPlayer.maxmp);
								dPlayer.refreshProperties(false);
								dPlayer.goMap(StepBattleService.mapId, reliveOption[0], reliveOption[1]);
							} catch (Exception e) {
								log.info(e, e);
							}
						}
					}else{
						playerBattleEnd(dPlayer);
					}
				}
			}else if(manager.getStepBattleType()==StepServer.STEPBATTLE_TYPE_16){
				if(isInInstance(dPlayer.id)){
					askPlayerEnterNextRound(dPlayer);
					diePlayerBattleEnd(dPlayer,1);
				}
			}else if(manager.getStepBattleType()==StepServer.STEPBATTLE_TYPE_TOURNAMENT){
				
			}
			
		}
	}
	
	/**
	 * 16强发送奖励和私聊
	 * @param dPlayer
	 * @param winOrLose 0-胜利，1-失败
	 */
	protected void diePlayerBattleEnd(Player dPlayer,int winOrLose){
		dPlayer.setHp(1, true);
		removePlayer(dPlayer);
		//通知client服务器玩家断开连接
		Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
		pt.putInt(StepServer.TYPE_BATTLE_END);
		int round=StepBattleService.todaySignedTimes.get(dPlayer.id);
		pt.putString(StepBattleService.STEPBATTLE_HINT_MORETHEN3ROUNDS+"|"+winOrLose+"|"+round);
		dPlayer.send(pt);
		dPlayer.refreshProperties(false);
		lastEnterTimes.remove(dPlayer.id);
		totalTimes.remove(dPlayer.id);
		dPlayer.removeFromWorld();
		removePlayer(dPlayer);
		dieTimes.remove(new Integer(dPlayer.id));
		dPlayer.session.close();
//		DispatchClientSessionService dispatchClientSessionService = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
//		dispatchClientSessionService.removeClientSession(StepServer.getStepBattleSessionId(dPlayer.accountId, dPlayer.id));
	}
	
	protected void playerBattleEnd(Player dPlayer){
		dPlayer.setHp(1, true);
		int currSize = players.size();
		removePlayer(dPlayer);
		//通知client服务器玩家断开连接
		Packet pt = new Packet(OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER);
		pt.putInt(StepServer.TYPE_BATTLE_END);
		pt.putString(Integer.toString(currSize));
		dPlayer.send(pt);
		dPlayer.refreshProperties(false);
		lastEnterTimes.remove(dPlayer.id);
		totalTimes.remove(dPlayer.id);
		dPlayer.removeFromWorld();
		removePlayer(dPlayer);
		dieTimes.remove(new Integer(dPlayer.id));
		dPlayer.session.close();
		DispatchClientSessionService dispatchClientSessionService = (DispatchClientSessionService) Server.server.getServiceRegistry().getService(DispatchClientSessionService.class);
		dispatchClientSessionService.removeClientSession(StepServer.getStepBattleSessionId(dPlayer.accountId, dPlayer.id));
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
		dieTimes.clear();
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
