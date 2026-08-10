package peony.service.duel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import peony.game.GameObject;
import peony.game.Instance;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapException;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 比武招亲副本
 * @author dchen
 */
public class DuelInstance implements Instance, ServiceEventListener {
	
	protected List<Player> players = new ArrayList<Player>(); // 本副本中所有玩家
	
	protected Map<Integer, Integer> scores = new HashMap<Integer, Integer>(); // 积分
	
	protected VMap map;
	
	protected long endTime;
	
	public int id;
	
	protected DuelService manager;
	
	public DuelInstance(int id, VMap map, DuelService manager){
		this.id = id;
		this.map = map;
		this.manager = manager;
		map.manager = manager;
		map.instance = this;
		Server.server.getEventManager().registerListener(this);
	}
	
	public void addPlayer(Player player) throws VMapException {
		int distributeMinorFaction = getMissingMinorFaction();
		player.minorFaction = distributeMinorFaction;
		boolean contain = false;
		for(Player p : players){
			if(p.id==player.id)
				contain = true;
		}
		if(!contain)
			players.add(player);
		if(id==manager.instances.size() && (manager.signUps.size()==0 || players.size()==DuelService.MAXPLAYERS)){
			manager.state = DuelService.STATE_INSTANCE_READY;
		}
		player.setMp(player.maxmp, true);
		player.setHp(player.maxhp, true);
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
		if(map!=null){
			return map.mapDef.mapInfo.name;
		}
		return "";
	}

	public void loadingFinished(Player player) {
		
	}

	public void removePlayer(Player player) {
		Iterator<Player> it = players.iterator();
		while(it.hasNext()){
			Player p = it.next();
			if(p.id==player.id){
				player.minorFaction = 0;
				it.remove();
			}
		}
	}

	public void update(int diff) {
		if(map!=null){
			map.update(diff);
		}
		if(manager.signUps.size()==0 && manager.state==DuelService.STATE_INSTANCE_READY){
			if(players.size()==1){
				Player player = players.get(0);
				DuelInstance instance = manager.getUsableDuelInstanceExcept(id);
				if(instance!=null && ObjectAccessor.getPlayer(player.id)!=null){
					player = ObjectAccessor.getPlayer(player.id);
					try {
						VMap map1 = Server.server.getWorld().addPlayerToMap(player, manager.mapInfo[0], 
								manager.mapInfo[1], manager.mapInfo[2], false);
						player.unMoving();
						Packet pt = new Packet(OpCode.FORCE_GOMAP_SERVER);
						pt.putInt(map1.getId());
						pt.putInt(map1.getInstanceId());
						pt.putInt(manager.mapInfo[1]);
						pt.putInt(manager.mapInfo[2]);
						pt.put(map1.allowFollow() ? 1 : 0);
						player.send(pt);
						instance.addPlayer(player);
						players.clear();
					} catch (Exception e) {
						
					}
				}
			}
		}
	}
	
	/** 获取当前副本下一个MINORFACTION */
	public int getMissingMinorFaction(){
		int[] minorFactions = {1,2,3,4,5,6,7};
		int missingFaction = 0;
ss:		for(int i=0;i<minorFactions.length;i++){
			int faction = minorFactions[i];
			for(Player p : players){
				if(p.minorFaction==faction){
					continue ss;
				}
			}
			missingFaction = faction;
			break;
		}
		return missingFaction;
	}
	
	public int[] getRelivePoint(int playerId){
		return manager.out;
	}
	
	/** 传送出本地图所有 */
	public void transPlayers(){
		Iterator<GameObject> itor = map.instanceid2objects.iterator2();
		while (itor.hasNext()) {
			GameObject o = itor.next();
			if(o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
				int[] out = manager.out;
				try {
					p.goMap(out[0], out[1], out[2]);
				} catch (VMapException e) {
					
				}
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
			processDie((Unit)event.param1, (Unit)event.param2);
		}
	}
	
	protected void processDie(Unit die, Unit kill){
		if(die.type==GameObject.TYPE_PLAYER && kill.type==GameObject.TYPE_PLAYER && 
				die.getVMap().getId()==map.getId() && kill.getVMap().getId()==map.getId()){
			// 为kill累计积分
			if(scores.get(kill.id)==null)
				scores.put(kill.id, 1);
			else
				scores.put(kill.id, scores.get(kill.id)+1);
		}
	}

}
