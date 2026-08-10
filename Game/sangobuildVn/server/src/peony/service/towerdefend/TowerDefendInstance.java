package peony.service.towerdefend;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.Creature;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.Instance;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.TransactionException;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapUtil;
import peony.game.buff.BuffUtil;

/**
 * 塔防战场副本
 * @author dchen
 */
public class TowerDefendInstance implements Instance {

	private static final AtomicInteger IDS = new AtomicInteger();
	protected int id;
	protected VMap map;
	protected TowerDefendConfig config;
	protected TowerDefendService manager;
	protected TowerDefend attack;
	protected TowerDefend defend;
	protected List<Player> defendPlayers = new ArrayList<Player>();
	protected List<Player> attackPlayers = new ArrayList<Player>();
	protected long startTime;
	protected boolean playerTransed;
	public List<int[]> npcs = new ArrayList<int[]>(); //小兵ID,x,y,塔ID,塔instanceId
	public List<Creature> attackNpcs = new ArrayList<Creature>(); //进攻方NPC
	public List<Creature> defendNpcs = new ArrayList<Creature>(); //防守方NPC
	public static long npcFreshPeriod = 20000; //刷兵的时间间隔
	public static int STATE_LOAD = 1;
	public int state;
	private int transAttackNum;
	private int transdefendNum;
	public Map<Integer, Integer> playerState = new HashMap<Integer, Integer>();
	public TowerDefend win;
	
	public TowerDefendInstance(final VMap map, final TowerDefendConfig config, TowerDefend attack, 
			TowerDefend defend, TowerDefendService manager){
		this.id = IDS.incrementAndGet();
		this.map = map;
		this.config = config;
		this.defend = defend;
		this.attack = attack;
		map.instance = this;
		this.manager = manager;
		this.startTime = System.currentTimeMillis();
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		GameMapObject gmo = GameMapObject.findByID(proj, config.flag[0]);
		GameObject npc0 = VMapUtil.addCreature(map, config.flag[1], config.flag[2], (GameMapNPC) gmo, true, 0, null);
		npc0.faction = defend.faction;
		((Creature)npc0).buffs.addBuff(BuffUtil.createBuff(TowerDefendConfig.BUFF, 1, 
				(Creature)npc0, (Creature)npc0, 0));
		defendNpcs.add((Creature)npc0);
		final Timer t = new Timer();
		t.schedule(new TimerTask(){
			public void run() {
				for(int[] npc : npcs){
					addToNpcFresh(npc);
				}
			}
		}, 0, npcFreshPeriod);
		new Timer().schedule(new TimerTask(){
			public void run() {
				if(playerTransed){
					endInstance();
					t.cancel();
					cancel();
				}
			}
		}, 0, 1000);
		//材料雨
		t.schedule(new TimerTask(){
			public void run() {
				for(GameObject o : map.instanceid2objects.values()){
					try {
						if(o.type==GameObject.TYPE_PLAYER){
							Player p = (Player)o;
							if(!p.isAlive())
								continue;
							int attackItemId = 964;
							int defendItemId = 962;
							if(getType(p)==0){
								PlayerTransaction tx = p.newTransaction("TDRAIN");
								try {
									p.bag.addGameItemComplete(ObjectAccessor.createGameItem(defendItemId), 1, tx, true);
									tx.commit();
								} catch (NoEnoughSpaceException e) {
									tx.rollback();
								}
							}else if(getType(p)==1){
								PlayerTransaction tx = p.newTransaction("TDRAIN");
								try {
									p.bag.addGameItemComplete(ObjectAccessor.createGameItem(attackItemId), 1, tx, true);
									tx.commit();
								} catch (NoEnoughSpaceException e) {
									tx.rollback();
								}
							}
						}
					} catch (TransactionException e) {
						
					}
				}
			}
		}, 0, 20*1000L);
	}
	
	protected void addToNpcFresh(int[] npc){
		if(map.getCreatureByIdAndInstanceId(npc[3],npc[4])!=null && 
				map.getCreatureByIdAndInstanceId(npc[3],npc[4]).isAlive() && 
				getTotalAlive(attackNpcs)<30){
			for(int i=0;i<2;i++){
				ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
				GameMapObject gmo = GameMapObject.findByID(proj, npc[0]);
				GameObject npc0 = VMapUtil.addCreature(map, npc[1]+10*i, npc[2], (GameMapNPC) gmo, true, 0, null);
				npc0.faction = attack.faction;
				attackNpcs.add((Creature)npc0);
				((Creature)npc0).buffs.addBuff(BuffUtil.createBuff(TowerDefendConfig.BUFF, 1, 
						(Creature)npc0, (Creature)npc0, 0));
			}
		}
	}
	
	protected int getTotalAlive(List<Creature> npcs){
		int count = 0;
		for(Creature c : npcs){
			if(c.isAlive())
				count++;
		}
		return count;
	}
	
	public void addPlayer(Player player) throws VMapException {
		if(player!=null){
			if(player.party!=null && player.party.id==attack.partyId){
				attackPlayers.add(player);
				transAttackNum++;
			}
			else if(player.party!=null && player.party.id==defend.partyId){
				defendPlayers.add(player);
				transdefendNum++;
			}
		}
		if((transAttackNum==1 && transdefendNum>=1) || (transAttackNum>=1 && transdefendNum==1))
			state = STATE_LOAD;
	}

	public int getId() {
		return this.id;
	}

	public VMap getMap(int mapId) {
		return null;
	}

	public String getName() {
		if(map!=null)
			return map.mapDef.mapInfo.name;
		return "";
	}

	public void loadingFinished(Player player) {
		
	}

	public void removePlayer(Player player) {
		if(player!=null && playerState.get(player.id)!=null && playerState.get(player.id)==1)
			clearReferItem(player);
		Iterator<Player> it = defendPlayers.iterator();
		while(it.hasNext()){
			Player p = it.next();
			if(p.id==player.id)
				it.remove();
		}
		Iterator<Player> it1 = attackPlayers.iterator();
		while(it1.hasNext()){
			Player p = it1.next();
			if(p.id==player.id)
				it1.remove();
		}
	}

	public void update(int diff) {
		map.update(diff);
		if(System.currentTimeMillis()-startTime>=config.duration && !playerTransed){
			boolean b = false;
			for(Creature defend : defendNpcs){
				if(defend.isAlive()){
					b = true;
				}
			}
			if(b){
				win(defend);
			}
			transPlayers();
			return;
		}
		if(state==STATE_LOAD){
			checkEnd();
		}
	}
	
	protected void checkEnd(){
		boolean b = true;
		for(Creature defend : defendNpcs){
			if(defend.isAlive()){
				b = false;
			}
		}
		if(b){
			win(attack);
			transPlayers();
		}
		if(attackPlayers.size()==0){
			win(defend);
			transPlayers();
		}
	}
	
	protected void win(TowerDefend td){
		this.win = td;
		if(td.type==0){
			Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(td.faction, 
					MessageFormat.format("布防四野固若金汤，{0}率队作为防守方取得了斗阵的胜利！", td.leaderName));
		}else if(td.type==1){
			Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(td.faction, 
					MessageFormat.format("攻无不克战无不胜，{0}率队作为攻击方取得了斗阵的胜利！", td.leaderName));
		}
		LogUtil.logTowerDefendWinner(win);
	}
	
	public void endInstance(){
		attackNpcs.clear();
		defendNpcs.clear();
		manager.removeInstance(this);
	}
	
	public void transPlayers(){
		Iterator<GameObject> itor = map.instanceid2objects.iterator2();
		while (itor.hasNext()) {
			GameObject o = itor.next();
			if(o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
				clearReferItem(p);
				int[] out = config.out[p.faction-1];
				try {
					PlayerTransaction tx = p.newTransaction("TDWIN");
					Reward re = null;
					if(getType(p)==win.type)
						re = config.winReward;
					else
						re = config.failReward;
					if(re.itemIds!=null && re.count!=null){
						for(int i=0;i<re.itemIds.size();i++){
							try {
								p.bag.addGameItemComplete(ObjectAccessor.createGameItem(re.itemIds.get(i)), re.count.get(i), tx, false);
								tx.commit();
							} catch (NoEnoughSpaceException e) {
								tx.rollback();
							}
						}
					}
					p.setHonor(p.honor+re.rank, true, "TD");
					p.goMap(out[0], out[1], out[2]);
				} catch (VMapException e) {
				}
			}
		}
		playerTransed = true;
		state = 0;
	}
	
	protected int getType(Player p){
		if(p.party!=null){
			if(attack.partyId==p.party.id)
				return attack.type;
			if(defend.partyId==p.party.id)
				return defend.type;
		}else{
			if(isInInstance(p)==0)
				return defend.type;
			if(isInInstance(p)==1)
				return attack.type;
		}
		return -1;
	}
	
	protected void clearReferItem(Player p){
		try {
			p.buffs.removeBuff(TowerDefendConfig.BUFF);
			for(int itemId : config.referItems){
				int count = p.bag.getGameItemCount(itemId);
				if(count>0){
					PlayerTransaction tx = p.newTransaction("TOWERDEFEND");
					GameItem item = p.bag.removeGameItemIngoreInstanceId(itemId, count, tx, false);
					if(item!=null)
						tx.commit();
					else
						tx.rollback();
				}
			}
		} catch (TransactionException e) {
		}
	}
	
	protected int isInInstance(Player player){
		for(Player p : attackPlayers){
			if(p.id==player.id)
				return 1;
		}
		for(Player p : defendPlayers){
			if(p.id==player.id)
				return 0;
		}
		return -1;
	}
	
	public int[] getRelivePoint(Player player){
		int type = getType(player);
		int[] inPosition = new int[3];
		int[] in = config.getInPosition(type);
		inPosition[0] = config.mapId;
		inPosition[1] = in[0];
		inPosition[2] = in[1];
		return inPosition;
	}

}
