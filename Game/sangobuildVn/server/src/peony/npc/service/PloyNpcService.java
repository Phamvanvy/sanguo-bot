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
					npcList.add(new Npc(npcId, x, y, message));
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
				Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
					public void run() {
						refreshNpc(refresh.mapId,npcs);
					}
				}, TimeUtil.getScheduleTimeMills(new Date(), t, timeRandom), ONEDAY, TimeUnit.MILLISECONDS);
			}
		}
	}
	
	protected synchronized void refreshNpc(int mapId, List<Npc> npcs){
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
					npctemps.add(npc0);
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
		if(u1.id==3539092 || u1.id==3539117 || u1.id==4260021 || u1.id==4260031){
			String name = "";
			if(u1.id==3539092)
				name = "张角";
			else if(u1.id==3539117)
				name = "金环三结";
			else if(u1.id==4260021)
				name = "木鹿大王";
			else if(u1.id==4260031)
				name = "愚人";
			Creature creature = (Creature)u1;
			if(creature.battleContribList!=null){
				Object owner = creature.battleContribList.owner;
				if(owner!=null){
					ChatService chatService = Server.server.getServiceRegistry().getChatService();
					if(owner instanceof GameObjectRef){
						Player p = (Player)ObjectAccessor.getGameObject((GameObjectRef)owner);
						chatService.sendWorldMessage(MessageFormat.format("{0} anh dũng hơn người, đã kích sát phản tặc {1}", p.name, name));
					}else{
						Party party = (Party)owner;
						if(party.leader!=null){
							chatService.sendWorldMessage(MessageFormat.format("{0} anh dũng dẫn tiểu đội, kích sát thành công phản tặc {1}", party.leader.getName(), name));
						}
					}
				}
			}
		}
		if(u1.id==3473573 || u1.id==3342503 ||u1.id==3670249 ||u1.id==3539122){
			String map = "";
			if(u1.id==3473573)
				map = "Tây Vực";
			else if(u1.id==3342503)
				map = "Sóc Phương ";
			else if(u1.id==3670249)
				map = "江陵";
			else if(u1.id==3539122)
				map = "Nam Hải";
			Creature creature = (Creature)u1;
			if(creature.battleContribList!=null){
				Object owner = creature.battleContribList.owner;
				if(owner!=null){
					ChatService chatService = Server.server.getServiceRegistry().getChatService();
					if(owner instanceof GameObjectRef){
						Player p = (Player)ObjectAccessor.getGameObject((GameObjectRef)owner);
						chatService.sendFactionSystemMessage(u2.faction, MessageFormat.format("{0} anh hùng của nước ta đã kích sát người tuyết {1}, nhận được phù cảm tình năm mão 12 con giáp. ", p.name, map));
					}else{
						Party party = (Party)owner;
						if(party.leader!=null){
							chatService.sendFactionSystemMessage(u2.faction, MessageFormat.format("{0} của nước ta đã anh dũng dẫn tiểu đội kích sát {1} người tuyết, nhận được năm mão 12 con giáp cảm tình phù.", party.leader.getName(), map));
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

class Npc{
	public int npcId;
	public int x;
	public int y;
	public String message;
	public Npc(int npcId, int x, int y, String message) {
		this.npcId = npcId;
		this.x = x;
		this.y = y;
		this.message = message;
	}
}
