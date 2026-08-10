package peony.game.battlefield;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import peony.game.Flag;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.GatherUnit;
import peony.game.Instance;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.game.buff.BuffUtil;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.util.Counter;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

import edu.emory.mathcs.backport.java.util.Collections;

public class FlagBattleFieldInstance implements Instance {
	
	protected static final AtomicInteger ids = new AtomicInteger(0);
	
	public static final int STATE_INIT = 0;
	public static final int STATE_STARTED = 1;
	public static final int STATE_ENDING = 2;
	public static final int STATE_END = 3;
	
	protected int id;
	protected VMap map;
	protected int state;
	
	protected int createTime;

	protected int startTime;
	
	protected int endTime;
	
	protected FlagBattleFieldDefinition def;
	
	protected Set<GameObjectRef> attachedRefs;
	
	protected List<ForceTran> trans = new ArrayList<ForceTran>(12);
	
	protected FlagBattleFieldVMapManager manager;
	
	protected Counter dieCount = new Counter();
	
	protected Counter killCount = new Counter();
	
	protected Counter factionKillCount = new Counter();
	
	protected Counter factionDieCount = new Counter();
	

	protected Counter factionFlagCount = new Counter();

	protected Counter flagCount = new Counter();
	
	protected Counter factionScore = new Counter();
	
	protected Counter flagCredit = new Counter();
	
	protected int[] factionCount = new int[2];
	
	protected int[] factionRealCount = new int[2];
	
	protected Flag flag = new BattleFieldFlag();
	
	protected GatherUnit u = null;
	
	protected LevelDef levelDef;
	
	protected Abort abort;
	
	protected static final int WIN_SCORE = 300;
	
	protected static final int INIT_TIME = 60 * 1000;
	
	public static float creditRatio = 1.0f;
	
	protected Set<Integer> inBattlePlayersId = new HashSet<Integer>();
	
	private static final Logger log = Logger.getLogger(FlagBattleFieldInstance.class);
	
	public FlagBattleFieldInstance(FlagBattleFieldVMapManager manager,
			FlagBattleFieldDefinition def, int durationSecond, LevelDef levelDef) {
		this.id = ids.incrementAndGet();
		this.manager = manager;
		this.def = def;
		this.state = STATE_INIT;
		this.createTime = Time.currTime;
		this.startTime = Time.currTime + INIT_TIME;
		this.endTime = Time.currTime + durationSecond * 1000 * 60;
		this.attachedRefs = new HashSet<GameObjectRef>();
		this.levelDef = levelDef;
		this.lastCheckTime = Time.currTime;
	}
	
	public int getPlayerCount(){
		return map.playerCount;
	}

	public void addPlayer(Player player) throws VMapException {
		incFactionRealCount(player.faction);
		player.clearReport();
	}
	
	public void incFactionRealCount(int faction){
		if(faction==def.faction1){
			factionRealCount[0]++;
		}
		else if(faction==def.faction2){
			factionRealCount[1]++;
		}
		else
			throw new IllegalStateException();
	}
	
	public void decFactionRealCount(int faction){
		if(faction==def.faction1){
			factionRealCount[0]--;
		}
		else if(faction==def.faction2){
			factionRealCount[1]--;
		}
		else 
			throw new IllegalStateException();
	}
	
	public int getFactionRealCount(int faction){
		if(faction==def.faction1)
			return factionRealCount[0];
		if(faction==def.faction2)
			return factionRealCount[1];
		throw new IllegalArgumentException();
	}
	
	public void incFactionCount(int faction){
		if(faction==def.faction1){
			factionCount[0]++;
		}
		else if(faction==def.faction2){
			factionCount[1]++;
		}
		else
			throw new IllegalStateException();
	}
	
	public void decFactionCount(int faction){
		if(faction==def.faction1){
			factionCount[0]--;
		}
		else if(faction==def.faction2){
			factionCount[1]--;
		}
		else 
			throw new IllegalStateException();
	}
	
	public int getFactionCount(int faction){
		if(faction==def.faction1)
			return factionCount[0];
		if(faction==def.faction2)
			return factionCount[1];
		throw new IllegalArgumentException();
	}
	
	public void setVMap(VMap map){
		this.map = map;
		this.map.instance = this;
	}

	public int getId() {
		return id;
	}
	
	public String getName() {
		return map.mapDef.mapInfo.name;
	}

	public VMap getMap(int mapId) {
		if(map!=null&&map.getId()==mapId){
			return map;
		}
		return null;
	}
	
	public VMap getMap() {
	    return map;
	}

	public void removePlayer(Player player) {
		if(player.flag!=null){
			throwFlag(player);
		}
		decFactionRealCount(player.faction);
//		manager.unbind(player, this);
		player.clearReport();
	}
	
	public int getStartTime(){
		return startTime;
	}

	public void update(int diff) {
		if (trans.size() > 0&&state==STATE_STARTED) {
			Iterator<ForceTran> ite = trans.iterator();
			while (ite.hasNext()) {
				ForceTran tran = ite.next();
				if (tran.tranTime <= Time.currTime) {
					Player p = ObjectAccessor.getPlayer(tran.ref.id);
					if (p != null&&p.systemState==Player.SYSTEMSTATE_READY&&p.isAlive()) {
						int[] in = def.getIn(p.faction);
						try {
							p.goMap(in[0], in[1], in[2]);
							ite.remove();
						} catch (VMapException e) {
							log.error(e,e);
						}
					} else {
						tran.times++;
						tran.tranTime += 1 * 60 * 1000;
						if(tran.times==4){
							ite.remove();
							log.info("[UNBINDUPDATE]ID["+tran.ref.id+"]");
							unAttach(tran.ref,tran.faction);
						}
					}
				}
			}
		}
		map.update(diff);
		if (state == STATE_INIT && Time.currTime >= startTime) {
			start();
		}
		if (state == STATE_STARTED){
			checkPlayerCount();
			checkFlagPosition();
			if(abort!=null&&abort.endTime<=Time.currTime){
				end();
			}else{
				shoutAbort();
			}
			scanPlayerBattleState();
		}
//		if (state == STATE_STARTED && Time.currTime >= endTime) {
//			end();
//		}
		if (state == STATE_ENDING && (Time.currTime - endTime) >= 5 * 60 * 1000L) {
			close();
		}
	}
	
	protected void scanPlayerBattleState() {
		if (Time.tick % 5 == 1) {
			for (GameObject o : map.instanceid2objects.values()) {
				if (o instanceof Player) {
					Player p = (Player) o;
					if (p.threats.getCount() > 0) {
						GameObjectRef[] refs = p.threats.getAllThreats();
						for(GameObjectRef ref:refs){
							inBattlePlayersId.add(ref.id);
						}
					}
				}
			}
		}
	}
	
	protected int lastCheckTime;
	
	protected void checkFlagPosition() {
		if (manager.checkFlagPosition
				&& (Time.currTime - lastCheckTime) >= 10 * 1000) {
			int flagX = -1, flagY = -1;
			GameObject flagObject = null;
			for (GameObject o : map.instanceid2objects.values()) {
				if (o.id == def.flagId) {
					flagX = o.x;
					flagY = o.y;
					flagObject = o;
					break;
				}
				if (o.hasFlag()) {
					flagX = o.x;
					flagY = o.y;
					flagObject = o;
					break;
				}
			}
			lastCheckTime = Time.currTime;
			if (!Server.server.getServiceRegistry().getDataService().data
					.getPathFinder().checkPath(def.mapId, def.x, def.y, flagX,
							flagY)) {
				if (flagObject instanceof Player) {
					try {
						((Player) flagObject).goMap(def.mapId, def.x, def.y);
						log.info("[FLAGPLAYERERROR]"
								+ LogUtil
										.getPlayerLogString((Player) flagObject));
					} catch (VMapException e) {
						log.info("[FLAGERROR]"
										+ LogUtil
												.getPlayerLogString((Player) flagObject));
					}
				} else {
					flagObject.move(def.x, def.y);
					log.info("[FLAGPOSITIONERROR]");
				}
			}
		}
	}
	
	protected void checkPlayerCount() {
		if (abort == null) {
			if (factionRealCount[0] < FlagBattleFieldVMapManager.MIN_PLAYER
					|| factionRealCount[1] < FlagBattleFieldVMapManager.MIN_PLAYER) {
				abort = new Abort(Time.currTime+5*60*1000);
			}
		}else{
			if(factionRealCount[0] >= FlagBattleFieldVMapManager.MIN_PLAYER
					&& factionRealCount[1] >= FlagBattleFieldVMapManager.MIN_PLAYER){
				abort = null;
			}
		}
	}
	
	protected void tran(Player p) {
		Iterator<ForceTran> ite = trans.iterator();
		while (ite.hasNext()) {
			ForceTran tran = ite.next();
			if (tran.ref.id == p.id) {
				int[] in = def.getIn(p.faction);
				if (p.isAlive() && p.systemState == Player.SYSTEMSTATE_READY) {
					try {
						p.goMap(in[0], in[1], in[2]);
						ite.remove();
					} catch (VMapException e) {
						log.error(e,e);
					}
				}
			}
		}
	}
	
	public void unTran(Player p){
		Iterator<ForceTran> ite = trans.iterator();
		while (ite.hasNext()) {
			ForceTran tran = ite.next();
			if (tran.ref.id == p.id) {
				if (p.isAlive() && p.systemState == Player.SYSTEMSTATE_READY) {
                     ite.remove();
				}
			}
		}
	}
	
	public void attach(Player player){
		incFactionCount(player.faction);
		attachedRefs.add(player.ref());
		manager.bind(player, this);
		Packet pt = new Packet(OpCode.BATTLEFIELD_TRAN_SERVER);
		pt.putInt(id);
		pt.putString(peony.Messages.STRING_01783);
		pt.putInt(Time.currTime+1*60*1000);
		player.send(pt);
		trans.add(new ForceTran(player.ref(),player.faction,Time.currTime+1*60*1000));
	}
	
	
	
	public void unAttach(GameObjectRef ref,int faction){
		decFactionCount(faction);
		attachedRefs.remove(ref);
		manager.unbind(ref, this);
	}
	
	
	public void start(){
		state = STATE_STARTED;
		refreshFlag(def.x, def.y);
		map.shout(peony.Messages.STRING_01784, 0xff5555, 5000);
	}
	
	protected void shoutAbort() {
		if (abort != null) {
			for (int i = 5; i >= 1; i--) {
				if (abort.notifyCount == i
						&& (abort.endTime - Time.currTime) < i * 60 * 1000) {
					map.shout(MessageFormat.format(peony.Messages.STRING_01785, i), 0xff5555, 5000);
					abort.notifyCount--;
				}
			}
		}
	}
	
	public void notifyScore(){
		map.notifyMapInfo(getScoreMessage());
	}
	
	public void end(){
		state = STATE_ENDING;
		endTime = Time.currTime;

		List<Score> scores = new ArrayList<Score>();
		List<Score> scores1 = new ArrayList<Score>();
		List<Score> scores2 = new ArrayList<Score>();
		List<Player> players = new ArrayList<Player>();
		for(GameObject o:map.instanceid2objects.values()){ //把当前存在玩家身上的旗去掉，并且计算分数
			if(o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
				if(p.flag!=null){
					p.setFlag(null);
					p.buffs.removeBuff(116);
					p.lastPosition = null;
				}
				Score score = new Score(p);
				score.dieCount = dieCount.value(p.id);
				score.killCount = killCount.value(p.id);
				score.flagCount = flagCount.value(p.id);
				scores.add(score);
				players.add(p);
				if(p.faction==def.faction1)
					scores1.add(score);
				else if(p.faction==def.faction2)
					scores2.add(score);
			}
		}
		for(Score score:scores){
			if(!score.p.isAlive()){
				score.p.relive(score.p.reliveOptions.getFirstOption());
			}
			if(score.p.flag!=null){
				score.p.setFlag(null);
				score.p.buffs.removeBuff(116);
				score.p.lastPosition = null;
			}
		}
		for(GameObjectRef ref:attachedRefs){
			log.info("[UNBINDEND]ID["+ref.id+"]");
			manager.unbind(ref, this);
		}
		Collections.sort(scores, new Comparator<Score>(){
			public int compare(Score o1, Score o2) {
				return (o2.killCount*3+o2.flagCount*20)-(o1.killCount*3+o1.flagCount*20);
			}
		});
		int winFaction = 0;
		boolean isRush = false;
		if(factionScore.value(def.faction1)!=factionScore.value(def.faction2))
			winFaction = factionScore.value(def.faction1)>factionScore.value(def.faction2)?def.faction1:def.faction2;
		int lostFaction = 0;
		lostFaction = def.faction1 == winFaction ? def.faction2:def.faction1;
		isRush = factionFlagCount.value(winFaction) == 15 && factionFlagCount.value(lostFaction) == 0; //战旗15:0被认为是rush掉的
		for (int i = 0, size = scores.size(); i < size; i++) {
			Score score = scores.get(i);
			score.credit = score.p.faction == winFaction ? levelDef.credit : 0;
			score.credit *= creditRatio;
			if (isRush || inBattlePlayersId.contains(score.p.id)||flagCount.value(score.p.id)!=0) {
				if (score.credit > 0) {
					PlayerTransaction tx = score.p.newTransaction("BTL");
					score.p.addCredit(score.credit, tx, true);
					tx.commit();
				}
			}else{
				score.credit = 0;
			}
			score.credit += flagCredit.value(score.p.id);
		}
		sendResult(scores,winFaction);
		for (GameObject o : map.instanceid2objects.values()) {
			if(o!=null && o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
				if(p.faction==winFaction){
					//战场胜利事件
					Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_BATTLE_WIN,p.id,1));
				}
			}
		}
		// 记录日志
		LogUtil.logFlagBattleEnd(id, def.faction1, def.faction2, players, winFaction, Time.currTime - createTime);
	}
	
	public boolean inLevel(int level){
		return levelDef.in(level);
	}
	
	protected void sendResult(List<Score> scores,int winFaction){
		Packet pt = new Packet(OpCode.BATTLEFIELD_INFO_CLIENT);
		pt.put(winFaction);
		pt.putShort(2);
		int[] factions = new int[2];
		if (winFaction != 0) {
			factions[0] = winFaction;
			factions[1] = (winFaction == def.faction1) ? def.faction2
					: def.faction1;
		} else {
			factions[0] = def.faction1;
			factions[1] = def.faction2;
		}
		for(int i=0;i<factions.length;i++){
			pt.put(factions[i]);
			pt.putString(GameObject.FACTION_NAME[factions[i]]);
			pt.putShort(factionKillCount.value(factions[i]));
			pt.putShort(factionDieCount.value(factions[i]));
			pt.putShort(factionFlagCount.value(factions[i]));
			pt.putInt(factionScore.value(factions[i]));
		}
		pt.putShort(scores.size());
		for(Score score:scores){
			pt.put(score.p.faction);
			pt.putString(score.p.name);
			pt.putShort(score.killCount);
			pt.putShort(score.dieCount);
			pt.putShort(score.flagCount);
			pt.putInt(score.credit);
		}
		for(Score score:scores){
			score.p.send(pt);
		}
	}
	
	public void close(){
		state = STATE_END;
		Iterator<GameObject> itor = map.instanceid2objects.iterator2();
		while (itor.hasNext()) {
			GameObject o = itor.next();
			if(o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
//				unAttach(p);
//				int[] out = manager.outs[o.faction-1];
//				try {
//					p.goMap(out[0], out[1], out[2]);
//				} catch (VMapException e) {
//					//不应该被执行到，出口都应该设置成非副本地图，不会抛出异常
//					log.error(e,e);
//				}
				String mapStr = p.pool.getString(Player.PROPERTY_FLAGBATTLE_SIGNUPMAP);
				if(mapStr!=null && !mapStr.equals("")){
					String[] strs = mapStr.split(",");
					int mapId = Integer.parseInt(strs[0]);
					int x = Integer.parseInt(strs[1]);
					int y = Integer.parseInt(strs[2]);
					try {
						p.goMap(mapId, x, y);
					} catch (VMapException e) {
						//不应该被执行到，出口都应该设置成非副本地图，不会抛出异常
						log.error(e,e);
					}
				}else{
					int[] out = manager.outs[o.faction-1];
					try {
						p.goMap(out[0], out[1], out[2]);
					} catch (VMapException e) {
						//不应该被执行到，出口都应该设置成非副本地图，不会抛出异常
						log.error(e,e);
					}
				}
			}
		}
	}
	
	public void quit(Player player){
		if(player.map.map.instance==this&&(state==STATE_ENDING||state==STATE_END)){
//			unAttach(player);
//			int[] out = manager.outs[player.faction-1];
//			try {
//				player.goMap(out[0], out[1], out[2]);
//			} catch (VMapException e) {
//				//不应该被执行到，出口都应该设置成非副本地图，不会抛出异常
//				log.error(e,e);
//			}
			String mapStr = player.pool.getString(Player.PROPERTY_FLAGBATTLE_SIGNUPMAP);
			if(mapStr!=null && !mapStr.equals("")){
				String[] strs = mapStr.split(",");
				int mapId = Integer.parseInt(strs[0]);
				int x = Integer.parseInt(strs[1]);
				int y = Integer.parseInt(strs[2]);
				try {
					player.goMap(mapId, x, y);
				} catch (VMapException e) {
					//不应该被执行到，出口都应该设置成非副本地图，不会抛出异常
					log.error(e,e);
				}
			}else{
				int[] out = manager.outs[player.faction-1];
				try {
					player.goMap(out[0], out[1], out[2]);
				} catch (VMapException e) {
					//不应该被执行到，出口都应该设置成非副本地图，不会抛出异常
					log.error(e,e);
				}
			}
		}
	}
	
	
	protected void catchedFlag(Player p){
		if(u!=null){
			BattleFieldFlag bf = (BattleFieldFlag)flag;
			if((System.currentTimeMillis()-bf.lastGiveTime) < 20*1000L){
				p.message(-1, peony.Messages.STRING_01786, -1, -1);
				p.cancelGather(5);
				u.removeFromWorld();
				refreshFlag(def.x, def.y);
				return;
			}
			p.setFlag(flag);
			p.buffs.addBuff(BuffUtil.createBuff(116, 1, p, p, 0));
			u.removeFromWorld();
			u = null;
			bf.lastCatchTime = System.currentTimeMillis();
			p.lastPosition = null;
			log.info("[CATCHFLAG]"+LogUtil.getPlayerLogString(p)+"X["+p.x+"]Y["+p.y+"]");
		}
	}
	
	
	public void throwFlag(Player p){
		throwFlag(p,p.x,p.y);
	}
	
	public void throwFlag(Player p,int x,int y){
		p.setFlag(null);
		p.buffs.removeBuff(116);
		p.lastPosition = null;
		refreshFlag(x,y);
	}
	
	public void died(Player p,Unit source){
		if(p.flag!=null){
			throwFlag(p);
		}
		dieCount.add(p.id, 1);
		factionDieCount.add(p.faction,1);
		if(source != null && source.type==GameObject.TYPE_PLAYER&&source.faction!=p.faction){
			killCount.add(source.id,1);
			factionKillCount.add(source.faction, 1);
			int score = factionScore.add(source.faction, 3);
			notifyScore();
			if(score>=WIN_SCORE){
				end();
			}
		}
		
	}
	
	public void loadingFinished(Player p){
		Packet pt = new Packet(OpCode.MAP_INFO_SERVER);
		pt.putShort(map.getId());
		pt.putString(getScoreMessage());
		p.send(pt);
	}
	
	public String getScoreMessage(){
		StringBuilder sb = new StringBuilder(40);
		sb.append(GameObject.FACTION_NAME[def.faction1]);
		sb.append("(");
		sb.append(factionScore.value(def.faction1));
		sb.append(")");
		sb.append("--");
		sb.append(GameObject.FACTION_NAME[def.faction2]);
		sb.append("(");
		sb.append(factionScore.value(def.faction2));
		sb.append(")");
		return sb.toString();
	}
	
	public void moveAt(Player p) {
		if (p.flag != null && atAim(p)) {
			BattleFieldFlag bf = (BattleFieldFlag)flag;
			long leavingTime = System.currentTimeMillis()-bf.lastCatchTime;
			if(leavingTime<20000L){
				int leaveSecond = (int) ((20000-leavingTime)/1000L);
				leaveSecond = (leaveSecond<=0? 1 : leaveSecond);
				p.message(-1, MessageFormat.format(peony.Messages.STRING_01787, leaveSecond), -1, -1);
				return;
			}
			bf.lastGiveTime = System.currentTimeMillis();
			p.setFlag(null);
			p.buffs.removeBuff(116);
			p.lastPosition = null;
			factionFlagCount.add(p.faction, 1);
			flagCount.add(p.id, 1);
			addHonorAndCredit(p.faction);
			map.shout(MessageFormat.format(peony.Messages.STRING_01788,p.getFactionName(),p.name), 0xff5555, 5000);
			int score = factionScore.add(p.faction, 20);
			notifyScore();
			if (score >= WIN_SCORE) {
				end();
			} else {
				refreshFlag(def.x, def.y);
			}
			log.info("[GIVEBACKFLAG]"+LogUtil.getPlayerLogString(p)+"X["+p.x+"]Y["+p.y+"]");
		}
	}
	
	protected void addHonorAndCredit(int faction){
		for(GameObject o:map.instanceid2objects.values()){
			if(o.type==GameObject.TYPE_PLAYER&&o.faction==faction){
				Player p = (Player)o;
				PlayerTransaction tx = p.newTransaction("BTL");
				p.addHonor(10, tx, true);
				p.addCredit((int)(10 * creditRatio), tx, true);
				flagCredit.add(p.id, (int)(10 * creditRatio));
				tx.commit();
			}
		}
	}
	
	protected void refreshFlag(int x,int y){
	    ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
	    GameMapObject gmo = GameMapObject.findByID(proj, def.flagId);
        u = (GatherUnit)VMapUtil.addCreature(map, x, y, (GameMapNPC)gmo, true, -1, Server.server.revision);
        u.call = manager.getGatherEndCall();
	}
	
	protected boolean atAim(Player p){
		int[] aim = def.getAim(p.faction);
		return p.distance(aim[0],aim[1]) <= 48*48;
	}
	
	class BattleFieldFlag implements Flag{
		
		public long lastGiveTime;
		
		public long lastCatchTime;

		public void bind(Player p) {
//			catchedFlag(p);
		}

		public void unbind(Player p) {
//			throwFlag(p);
		}
		
	}
	
	/** 判断玩家是否在战场中 */
	public int isInFlagBattle(Player player){
		if(player!=null){
			for(GameObject o : map.instanceid2objects.values()){
				if (o instanceof Player) {
					Player p = (Player) o;
					if (p.id==player.id && p.map.id==map.getId()) {
						return 1;
					}
				}
			}
		}
		return 0;
	}
}

class ForceTran{
	public GameObjectRef ref;
	public int faction;
	public int tranTime;
	public int times;
	
	public ForceTran(GameObjectRef ref,int faction,int tranTime){
		this.ref = ref;
		this.faction = faction;
		this.tranTime = tranTime;
	}
}

class Score{
	public Player p;
	public int killCount;
	public int dieCount;
	public int flagCount;
	public int credit;
	
	public Score(Player p){
		this.p = p;
	}
}


class Abort{
	public int endTime;
	public int notifyCount;
	public Abort(int endTime){
		this.endTime = endTime;
		this.notifyCount = 5;
	}
}