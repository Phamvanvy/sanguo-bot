package peony.npc.service;

import java.io.ByteArrayInputStream;
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
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.service.Service;

public class ExchangeNpcService implements Service {

	public Map<Integer, List<Refr>> Refrs = new HashMap<Integer, List<Refr>>();
	protected List<GameObject> npctemps = new ArrayList<GameObject>();
	public static final long ONEDAY = 24 * 3600 * 1000L;
	
	public void shutdown() {

	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("exchangenpc.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		processRefr();
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List<Element> list = root.elements();
		if(list!=null && list.size()>0){
			for(Element element0 : list){
				int mapId = Integer.parseInt(element0.attributeValue("id"));
				List<Element> ll = element0.elements();
				List<Refr> rfs = new ArrayList<Refr>();
				for(Element element : ll){
						List<Element> clocks = element.elements("clock");
						List<Time> periods = new ArrayList<Time>();
						for(Element clock : clocks){
							int time = Integer.parseInt(clock.attributeValue("beginTime"));
							int min = Integer.parseInt(clock.attributeValue("min"));
							periods.add(new Time(time, min));
						}
						List<Element> npcs = element.elements("npc");
						List<Npc> npcList = new ArrayList<Npc>();
						for(Element npc : npcs){
							int npcId = Integer.parseInt(npc.attributeValue("id"));
							int x = Integer.parseInt(npc.attributeValue("x"));
							int y = Integer.parseInt(npc.attributeValue("y"));
							npcList.add(new Npc(npcId, x, y));
						}
						Refr Refr = new Refr(npcList, periods);
						rfs.add(Refr);
				}
				Refrs.put(mapId, rfs);
			}
		}
	}
	
	protected void processRefr(){
		for(final Integer mapId : Refrs.keySet()){
			List<Refr> rfs = Refrs.get(mapId);
			for(Refr Refr : rfs){
				List<Time> periods = Refr.periods;
				final List<Npc> npcs = Refr.npcs;
				for(Time t : periods){
					Calendar cal = Calendar.getInstance();
					new Timer().schedule(new TimerTask(){
						public void run() {
							Server.server.syncRunner.add(new Runnable(){
								public void run() {
									RefrNpc(mapId,npcs);
								}
							});
						}
					}, getScheduleTime(cal, t.hour, t.min), ONEDAY);
				}
			}
		}
	}
	
	protected void RefrNpc(int mapId, List<Npc> npcs){
		if(npctemps.size()>=21){
			removeNpcs();
		}
//		removeNpcs();
		VMapManager manager = Server.server.getWorld().getVMapManager(mapId);
		VMap[] maps = null;
		if(manager instanceof PloyMapVMapManager){
			maps = new VMap[] { ((PloyMapVMapManager)manager).map };
		}else{
		    maps = ((NoInstanceVMapManager) manager).getVMaps(mapId);
		}
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		for(Npc npc : npcs){
			GameMapObject gmo = GameMapObject.findByID(proj, npc.npcId);
			for (VMap map : maps) {
				GameObject npc0 = VMapUtil.addCreature(map, npc.x, 
						npc.y, (GameMapNPC) gmo, true, 0, null);
				npctemps.add(npc0);
			}
		}
	}
	
	protected void removeNpcs(){
		for(GameObject npc : npctemps){
			if(npc!=null && npc.getVMap() != null){
				npc.removeFromWorld();
			}
		}
		npctemps.clear();
	}
	
	public Date getScheduleTime(Calendar cal,int hour, int min){
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY,hour);
		cal1.set(Calendar.MINUTE, min);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		if(cal1.before(cal)){
			cal1.add(Calendar.DAY_OF_MONTH, 1);
			return cal1.getTime();
		}else{
			return cal1.getTime();
		}
	}

}

class Refr{
	public List<Time> periods = new ArrayList<Time>();
	public List<Npc> npcs = new ArrayList<Npc>();
	public Refr(List<Npc> npcs, List<Time> periods) {
		this.npcs = npcs;
		this.periods = periods;
	}
}

class Time{
	
	public int hour;
	public int min;
	public Time(int hour, int min) {
		super();
		this.hour = hour;
		this.min = min;
	}
	
}
