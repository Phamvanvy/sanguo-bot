package peony.service.duelmetting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import peony.game.GameObject;
import peony.game.Instance;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
/**
 * 天下第一武道会副本
 * @author pmeng
 */
public class DuelMettingInstance implements Instance,ServiceEventListener{
	
	protected final Logger log = Logger.getLogger(DuelMettingInstance.class);
	
	public int id;
	
	protected VMap map;
	
	protected DuelMettingService manager;
	
	protected List<Integer> players = new ArrayList<Integer>(); // 本副本中所有玩家
	
	protected boolean minorFactionswitch ;//同阵营战斗开关
	
	protected long createTime;//创建时间
	
	protected int state;
	
	protected static int STATE_INIT = 1;//玩家传送进入后
	protected static int STATE_FINISH = 2;//玩家传送进入后
	
	public DuelMettingInstance(int id, VMap map, DuelMettingService manager){
		this.id = id;
		this.map = map;
		this.manager = manager;
		map.manager = manager;
		map.instance = this;
		createTime = System.currentTimeMillis();
		Server.server.getEventManager().registerListener(this);
	}

	public void addPlayer(Player player) throws VMapException {
		boolean contain = false;
		for(int p : players){
			if(player.id == p)
				contain = true;
		}
		if(!contain)
			players.add(player.id);
		player.setMp(player.maxmp, true);
		player.setHp(player.maxhp, true);
		if(minorFactionswitch){
			player.minorFaction = 1;
		}else{
			player.minorFaction = 2;
		}
		minorFactionswitch = !minorFactionswitch;
	}

	public int getId() {
		return id;
	}

	public VMap getMap(int mapId) {
		if(map!=null && map.getId()==mapId)
			return map;
		return null;
	}

	public String getName() {
		if(map != null){
			return map.mapDef.mapInfo.name;
		}
		return null;
	}
	
	/** 传出副本中所有玩家 */
	public void transPlayers(){
		Iterator<Integer> it = players.iterator();
		try{
			while(it.hasNext()){
				Integer p = it.next();
				Player player = ObjectAccessor.getPlayer(p);
				if(player != null){
					player.minorFaction = 0;
					int[] point = manager.getRevivePoint(player);
					player.goMap(point[0], point[1], point[2]);
					log.info("[DUELMETTING]TRANSPLAYER[" + player.id + "]");
				}
			}
		}catch(VMapException e){
			e.printStackTrace();
		}
		this.state = STATE_FINISH;
		players.clear();
	}
	

	public void removePlayer(Player player) {
		player.minorFaction = 0;
		if(this.state == STATE_INIT && players.size() == 2 && players.contains(player.id)){//使用传送符的情况
			Integer failPlayer = new Integer(player.id);
			players.remove(failPlayer);
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, peony.Messages.STRING_00482);
			Player win = ObjectAccessor.getPlayer(players.get(0));
			if(win != null){
				Server.server.getServiceRegistry().getChatService().sendPrivateMessage(win.id, peony.Messages.STRING_00483);
				manager.giveRedMedicinal(win, 2);
			}
			manager.loseIds.add(player.id);
			manager.playerIds.add(players.get(0));
			transPlayers();
		}	
	}

	public void loadingFinished(Player player) {
		
	}
	
	public void update(int diff) {
		if(map != null){
			map.update(diff);
		}
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_UNIT_DIE,
				ServiceEvent.EVENT_PLAYER_LOGOUTED
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_UNIT_DIE:
			processDie((Unit)event.param1, (Unit)event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			playerLogouted((Player)event.param1);
			break;
		}
	}
	
	protected void playerLogouted(Player player){
		if(player.type==GameObject.TYPE_PLAYER && 
				player.map.getId()==map.getId()){
			if(players.contains(player.id)){
				ObjectAccessor.getPlayer(player.id).minorFaction = 0;
				manager.loseIds.add(player.id);
				Iterator<Integer> it = players.iterator();
				if(it.hasNext()){
					Integer id = it.next();
					if(id.intValue() == player.id){
						it.remove();
					}
				}
				if(players.size() == 1){
					Player win = ObjectAccessor.getPlayer(players.get(0));
					if(win != null){
						win.minorFaction = 0;
						manager.playerIds.add(win.id);
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(win.id, peony.Messages.STRING_00483);
						manager.giveRedMedicinal(win, 2);
						log.info("[DUELMETTING]WINER[" + win.id + "]");
					}
				}
				transPlayers();
			}
		}
	}
	
	protected void processDie(Unit die, Unit kill){
		if(die.type==GameObject.TYPE_PLAYER && kill.type==GameObject.TYPE_PLAYER && 
				kill.getVMap().getId()==map.getId() && kill.map.map.instance != null && kill.map.map.instance.getId() == this.id){
			Server.server.getServiceRegistry().getChatService().sendPrivateMessage(die.id, peony.Messages.STRING_00482);
			if(players.contains(kill.id)){
				Iterator<Integer> it = players.iterator();
				manager.playerIds.add(kill.id);
				manager.loseIds.add(die.id);
				if(it.hasNext()){
					Integer id = it.next();
					if(id.intValue() == die.id){
						it.remove();
					}
				}
				Player win = ObjectAccessor.getPlayer(players.get(0));
				if(win != null){
					Server.server.getServiceRegistry().getChatService().sendPrivateMessage(win.id, peony.Messages.STRING_00483);
					manager.giveRedMedicinal(win, 2);
					log.info("[DUELMETTING]WINER[" + win.id + "]");
				}
				transPlayers();
			}
		}
	}
	
}
