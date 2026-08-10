package peony.npc.service;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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

public class PloyNpcService implements Service, ServiceEventListener {

	public Map<Integer, Refresh> refreshs = new HashMap<Integer, Refresh>();
	protected List<GameObject> npctemps = new ArrayList<GameObject>();
	public static final long ONEDAY = 24 * 3600 * 1000L;
	
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
					npcList.add(new Npc(npcId, x, y));
				}
				Refresh refresh = new Refresh(npcList, periods);
				refreshs.put(mapId, refresh);
			}
		}
	}
	
	protected void processRefresh(){
		for(final Integer mapId : refreshs.keySet()){
			Refresh refresh = refreshs.get(mapId);
			List<Integer> periods = refresh.periods;
			final List<Npc> npcs = refresh.npcs;
//			refreshNpc(mapId, npcs);
			for(int t : periods){
				Calendar cal = Calendar.getInstance();
				new Timer().schedule(new TimerTask(){
					public void run() {
						refreshNpc(mapId,npcs);
					}
				}, getScheduleTime(cal, t), ONEDAY);
			}
		}
	}
	
	protected void refreshNpc(int mapId, List<Npc> npcs){
		removeNpcs();
		VMapManager manager = Server.server.getWorld().getVMapManager(mapId);
		VMap[] maps = ((NoInstanceVMapManager) manager).getVMaps(mapId);
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		for(Npc npc : npcs){
			GameMapObject gmo = GameMapObject.findByID(proj, npc.npcId);
			for (VMap map : maps) {
				GameObject npc0 = VMapUtil.addCreature(map, npc.x, 
						npc.y, (GameMapNPC) gmo, true, 0, null);
				npctemps.add(npc0);
			}
		}
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		chatService.sendWorldMessage("張角与其同党出現在南海的西南方向.");
	}
	
	protected void removeNpcs(){
		for(GameObject npc : npctemps){
			if(npc!=null && npc.getVMap() != null){
				npc.removeFromWorld();
			}
		}
		npctemps.clear();
	}
	
	public Date getScheduleTime(Calendar cal,int hour){
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY,hour);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		if(cal1.before(cal)){
			cal1.add(Calendar.DAY_OF_MONTH, 1);
			return cal1.getTime();
		}else{
			return cal1.getTime();
		}
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
		if(u1.id==3539092){
			Creature creature = (Creature)u1;
			if(creature.battleContribList!=null){
				Object owner = creature.battleContribList.owner;
				if(owner!=null){
					ChatService chatService = Server.server.getServiceRegistry().getChatService();
					if(owner instanceof GameObjectRef){
						Player p = (Player)ObjectAccessor.getGameObject((GameObjectRef)owner);
						chatService.sendWorldMessage(MessageFormat.format("{0}英勇難當,成功擊殺了反賊張角.", p.name));
					}else{
						Party party = (Party)owner;
						if(party.leader!=null){
							chatService.sendWorldMessage(MessageFormat.format("{0}帶領小隊英勇難當,成功擊殺了反賊張角.", party.leader.getName()));
						}
					}
				}
			}
		}
	}

}

class Refresh{
	public List<Integer> periods = new ArrayList<Integer>();
	public List<Npc> npcs = new ArrayList<Npc>();
	public Refresh(List<Npc> npcs, List<Integer> periods) {
		this.npcs = npcs;
		this.periods = periods;
	}
}

class Npc{
	public int npcId;
	public int x;
	public int y;
	public Npc(int npcId, int x, int y) {
		this.npcId = npcId;
		this.x = x;
		this.y = y;
	}
}
