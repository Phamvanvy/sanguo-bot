package peony.npc.service;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

import peony.db.RefreshNpcCall;
import peony.game.CommonUtil;
import peony.game.Creature;
import peony.game.GameObject;
import peony.game.GameObjectRef;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.chat.ChatService;
import peony.game.party.Party;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.util.TimeUtil;

public class PloyNpcService implements Service, ServiceEventListener {

//	public Map<Integer, Refresh> refreshs = new HashMap<Integer, Refresh>();
	public List<Refresh> refreshs = new ArrayList<Refresh>();
	protected List<GameObject> npctemps = new ArrayList<GameObject>();
	public static final long ONEDAY = 24 * 3600 * 1000L;
	public static Random random = new Random();
	
	public void shutdown() {

	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("ploynpc.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		processRefresh();
	}
	 
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List<Element> list = root.elements();
		if(list!=null && list.size()>0){
			for(Element element : list){
				int mapId = Integer.parseInt(element.attributeValue("id"));
				String message = element.attributeValue("message");
				int timeRandom = Integer.parseInt(element.attributeValue("timeRandom"));
				String isWorldBoss=element.attributeValue("isWorldBoss");
				List<Element> clocks = element.elements("clock");
				List<Integer> periods = new ArrayList<Integer>();
				for(Element clock : clocks){
					int time = Integer.parseInt(clock.attributeValue("beginTime"));
					periods.add(time);
				}
				List<Element> npcs = element.elements("npc");
				List<Npc> npcList = new ArrayList<Npc>();
				for(Element npc : npcs){
					int npcId = Integer.parseInt(npc.attributeValue("id"));
					int x = Integer.parseInt(npc.attributeValue("x"));
					int y = Integer.parseInt(npc.attributeValue("y"));
//					npcList.add(new Npc(npcId, x, y, message));
					Npc npcTemp=new Npc(npcId,x,y,message);
					npcTemp.isWorldBoss=(isWorldBoss==null);
					npcList.add(npcTemp);
				}
				Refresh refresh = new Refresh(mapId, npcList, periods, timeRandom);
//				refreshs.put(mapId, refresh);
				refreshs.add(refresh);
			}
		}
	}
	
//	protected void processRefresh(){
//		for(final Integer mapId : refreshs.keySet()){
//			Refresh refresh = refreshs.get(mapId);
//			List<Integer> periods = refresh.periods;
//			final List<Npc> npcs = refresh.npcs;
////			refreshNpc(mapId, npcs);
//			for(int t : periods){
//				Calendar cal = Calendar.getInstance();
//				new Timer().schedule(new TimerTask(){
//					public void run() {
//						refreshNpc(mapId,npcs);
//					}
//				}, getScheduleTime(cal, t), ONEDAY);
//			}
//		}
//	}
	
	public void refreshPointNpc(String npcName){
		for(final Refresh refresh : refreshs){
			final List<Npc> npcs = refresh.npcs;
			boolean ok = false;
			for(Npc n : npcs){
				if(n.message.contains(npcName)){
					ok = true;
					break;
				}
			}
			if(ok){
				RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.PLOYNPC);
				call.mapId = refresh.mapId;
				call.npcs = new ArrayList<Npc>();
				call.npcs = npcs;
				Server.server.getWorld().schedule(call);
			}
		}
	}
	
	protected void processRefresh(){
		for(final Refresh refresh : refreshs){
			List<Integer> periods = refresh.periods;
			final List<Npc> npcs = refresh.npcs;
			for(int t : periods){
				int r = random.nextInt(3);
				int timeRandom = refresh.timeRandom;
				if(timeRandom>0){
					if(r==0){
						t -= 1;
						timeRandom = 60 - timeRandom;
					}else if(r==1){
						timeRandom = 0;
					}
				}
				boolean isNeedsendChat = false;
				for(Npc npc:refresh.npcs){
					if(npc.npcId==8323137 ||npc.npcId==8323136 ||npc.npcId==8257717){
						isNeedsendChat = true;
						break;
					}
				}
				int hour = 0;
				int min = 0;
				if(isNeedsendChat){
					if(timeRandom == 59){
						hour = t;
						min = 54;
					}else if(timeRandom == 0){
						hour = t-1;
						min = 55;
					}else if(timeRandom == 1){
						hour = t-1;
						min = 56;
					}
					Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
						public void run() {
							ChatService chatService = Server.server.getServiceRegistry().getChatService();
							String message = "";
							for(Npc npc:refresh.npcs){
								if(npc.npcId==8323137){
									message = peony.Messages.STRING_00963;
									break;
								}else if(npc.npcId==8323136){
									message = peony.Messages.STRING_00964;
									break;
								}else if(npc.npcId==8257717){
									message = peony.Messages.STRING_00965;
									break;
								}
							}
							chatService.sendWorldMessage(message);
						}
					}, TimeUtil.getScheduleTimeMills(new Date(), hour, min), ONEDAY, TimeUnit.MILLISECONDS);
				}
				Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
					public void run() {
//						refreshNpc(refresh.mapId,npcs);
						RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.PLOYNPC);
						call.mapId = refresh.mapId;
						call.npcs = new ArrayList<Npc>();
						call.npcs = npcs;
						Server.server.getWorld().schedule(call);
					}
				}, TimeUtil.getScheduleTimeMills(new Date(), t, timeRandom), ONEDAY, TimeUnit.MILLISECONDS);
			}
		}
	}
	
	public synchronized void refreshNpc(int mapId, List<Npc> npcs){
		removeNpcs(npcs);
		VMapManager manager = Server.server.getWorld().getVMapManager(mapId);
		VMap[] maps = ((NoInstanceVMapManager) manager).getVMaps(mapId);
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		String message = "";
		for(Npc npc : npcs){
			if(message.equals(""))
				message = npc.message;
			GameMapObject gmo = GameMapObject.findByID(proj, npc.npcId);
			for (VMap map : maps) {
				if(map!=null){
					GameObject npc0 = VMapUtil.addCreature(map, npc.x, 
							npc.y, (GameMapNPC) gmo, true, 0, null);
					npc0.isWorldboss=npc.isWorldBoss;
					npctemps.add(npc0);
					if(npc.npcId==8257717 || npc.npcId==8323137 || npc.npcId==8323136){
						String name = "";
						if(npc.npcId==8323137){
							name = peony.Messages.STRING_00966;
						}
						else if(npc.npcId==8323136){
							name = peony.Messages.STRING_00967;
						}
						else if(npc.npcId==8257717){
							name = peony.Messages.STRING_00968;
						}
					}
				}
			}
		}
		if(message.equals(""))
			return;
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		if(mapId==272){
			chatService.sendFactionSystemMessage(1, message);
		}else if(mapId==240){
			chatService.sendFactionSystemMessage(2, message);
		}else if(mapId==352){
			chatService.sendFactionSystemMessage(3, message);
		}else{
			chatService.sendWorldMessage(message);
		}
	}
	
	protected void removeNpcs(List<Npc> npcs){
		for(GameObject npc : npctemps){
			for(Npc n : npcs){
				if(npc!=null && npc.getVMap() != null && npc!=null && npc.id==n.npcId){
				    if(npc instanceof Creature){
				    	((Creature) npc).clearThreats();
				    }
					npc.removeFromWorld();
				}
			}
		}
		Iterator<GameObject> it = npctemps.iterator();
		while(it.hasNext()){
			GameObject go = it.next();
			for(Npc n : npcs){
				if(n.npcId==go.id)
					it.remove();
			}
		}
//		npctemps.clear();
	}

	public int[] getEventTypes() {
		return new int[]{
			ServiceEvent.EVENT_UNIT_DIE,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_UNIT_DIE:
			unitDie((Unit)event.param1,(Unit)event.param2);
			break;
		}
	}
	
	protected void unitDie(Unit u1, Unit u2){
		if(u1.id==8257717 || u1.id==8323137 || u1.id==8323136 || u1.id == 8192136||u1.id==8192147||u1.id==8192148){
			String name = "";
			Creature creature = (Creature)u1;
			ChatService chatService = Server.server.getServiceRegistry().getChatService();
			if(u1.id==8323137)
				name = peony.Messages.STRING_00966;
			else if(u1.id==8323136)
				name = peony.Messages.STRING_00967;
			else if(u1.id==8257717)
				name = peony.Messages.STRING_00968;
			else if(u1.id == 8192136||u1.id==8192147||u1.id==8192148){
				name = "ндЁС";
				if(u1 instanceof Player){
					Player p = (Player)u1;
					if(p!=null){
						Party party = (Party)p.party;
						if(party == null){
							chatService.sendWorldMessage(MessageFormat.format(peony.Messages.STRING_00969,p.getFactionName(), p.name, name));
						}else{
							if(party.leader!=null){
								chatService.sendWorldMessage(MessageFormat.format(peony.Messages.STRING_00970, party.leader.player.getFactionName(),party.leader.getName(), name));
							}
						}
					}
				}
			}
			
			if(creature.battleContribList!=null){
				Object owner = creature.battleContribList.owner;
				if(owner!=null){
					if(owner instanceof GameObjectRef){
						Player p = (Player)ObjectAccessor.getGameObject((GameObjectRef)owner);
						chatService.sendWorldMessage(MessageFormat.format(peony.Messages.STRING_00969,p.getFactionName(), p.name, name));
					}else{
						Party party = (Party)owner;
						if(party.leader!=null){
							chatService.sendWorldMessage(MessageFormat.format(peony.Messages.STRING_00970, party.leader.player.getFactionName(),party.leader.getName(), name));
						}
					}
				}
			}
		}
		if(u1.id==3473573 || u1.id==3342503 ||u1.id==3670249 ||u1.id==3539122){
			String map = "";
			if(u1.id==3473573)
				map = peony.Messages.STRING_00971;
			else if(u1.id==3342503)
				map = peony.Messages.STRING_00972;
			else if(u1.id==3670249)
				map = peony.Messages.STRING_00973;
			else if(u1.id==3539122)
				map = peony.Messages.STRING_00974;
			Creature creature = (Creature)u1;
			if(creature.battleContribList!=null){
				Object owner = creature.battleContribList.owner;
				if(owner!=null){
					ChatService chatService = Server.server.getServiceRegistry().getChatService();
					if(owner instanceof GameObjectRef){
						Player p = (Player)ObjectAccessor.getGameObject((GameObjectRef)owner);
						chatService.sendFactionSystemMessage(u2.faction, MessageFormat.format(peony.Messages.STRING_00975, p.name, map));
					}else{
						Party party = (Party)owner;
						if(party.leader!=null){
							chatService.sendFactionSystemMessage(u2.faction, MessageFormat.format(peony.Messages.STRING_00976, party.leader.getName(), map));
						}
					}
				}
			}
		}
	}

}

class Refresh{
	public int mapId;
	public List<Integer> periods = new ArrayList<Integer>();
	public List<Npc> npcs = new ArrayList<Npc>();
	public int timeRandom;
	public Refresh(int mapId, List<Npc> npcs, List<Integer> periods, int timeRandom) {
		this.mapId = mapId;
		this.npcs = npcs;
		this.periods = periods;
		this.timeRandom = timeRandom;
	}
}
