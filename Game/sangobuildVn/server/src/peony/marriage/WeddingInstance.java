package peony.marriage;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import peony.game.Actor;
import peony.game.GameObject;
import peony.game.Instance;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.chat.ChatService;

public class WeddingInstance implements Instance {

	protected WeddingService manager = Server.server.getServiceRegistry().getWeddingService();
	
	protected VMap map;
	
	protected static AtomicInteger IDS = new AtomicInteger(1);
	
	protected int id;
	
	public Actor man;
	
	public Actor woman;
	
	protected Date startTime;
	
	protected Date endTime;
	
	protected int level;
	
	protected int guestLevel;
	
	protected int fetchCount = 0;
	
	protected int[] MAXCOUNT = {0,42,72,90};
	
	protected int count = 0;
	
	protected List<Player> players = new ArrayList<Player>();
	
	protected List<Actor> signIns = new ArrayList<Actor>();
	
	public List<Integer> getgift = new ArrayList<Integer>();
	
	public List<Integer> kicked = new ArrayList<Integer>();
	
	public List<Integer> deskgift = new ArrayList<Integer>();
	
	public Map<Integer,Long> playerentertime = new HashMap<Integer,Long>();
	
	public static int BEGIN = 0;
	public static int END = 1;
	
	public int stat = END;
	
	public static long WEDDINGUDRATION = 2 * 60 * 60 * 1000; 
	
	public WeddingInstance(Player man, Player woman, Date startTime){
		this.man = Server.server.getServiceRegistry().getActorCacheService().find(man.id);
		this.woman = Server.server.getServiceRegistry().getActorCacheService().find(woman.id);
		this.id = IDS.incrementAndGet();
		this.startTime = startTime;
		this.endTime = new Date(startTime.getTime()+WEDDINGUDRATION);
		this.stat = BEGIN;
	}
	
	public void addPlayer(Player player) throws VMapException {
		for(WeddingInstance instance : manager.instances){
			Iterator<Player> it = instance.players.iterator();
			while(it.hasNext()){
				Player p = it.next();
				if(p.id==player.id){
					it.remove();
				}
			}
		}
		players.add(player);
	}

	public int getId() {
		return id;
	}

	public VMap getMap(int mapId) {
		if(map.getId()==mapId){
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
		Iterator<Player> it = players.iterator();
		while(it.hasNext()){
			Player p = it.next();
			if(p.id==player.id){
				it.remove();
			}
		}
	}

	public void update(int diff) {
		if(endTime.getTime() - System.currentTimeMillis() <= 5*60*1000 && count == 0){
			count = 1;
			ChatService service= Server.server.getServiceRegistry().getChatService();
			String msg = MessageFormat.format("{0}和{1}的婚礼即将在五分钟以后结束",man.name,woman.name);
			service.sendAreaSystemMessage(msg, Integer.parseInt(map.getId()+""+getId()));
		}
		Date date = new Date();
		if(date.after(endTime) && stat==BEGIN){
			stat = END;
			transPlayers();
		}
		if(map!=null){
			map.update(diff);
		}
		
	}
	
	protected void transPlayers(){
		Iterator<GameObject> itor = map.instanceid2objects.iterator2();
		while (itor.hasNext()) {
			GameObject o = itor.next();
			if(o.type==GameObject.TYPE_PLAYER){
				Player p = (Player)o;
				Position po = manager.outInfo[p.faction];
				try {
					p.goMap(po.mapId, po.x, po.y);
				} catch (VMapException e) {
					
				}
			}
		}
	}

}
