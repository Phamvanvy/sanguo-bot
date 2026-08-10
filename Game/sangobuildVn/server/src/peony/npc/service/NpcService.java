package peony.npc.service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.db.DeletePlayerCall;
import peony.game.CommonUtil;
import peony.game.GameObject;
import peony.game.GatherUnit;
import peony.game.NoInstanceVMapManager;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapManager;
import peony.game.VMapUtil;
import peony.game.chat.ChatService;
import peony.service.Service;
import peony.util.TimeUtil;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

public class NpcService implements Service {

	protected Map<Integer, NpcRefresh> map2npc = new HashMap<Integer, NpcRefresh>();
	protected List<GameObject> npctemps = new ArrayList<GameObject>();
	public static final long ONEDAY = 24 * 3600 * 1000L;
	protected Map<Clock, List<RefreshNpcTask>> cache = new HashMap<Clock, List<RefreshNpcTask>>();
	
	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("npc.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		processRefreshNpc();
	}
	
	protected void processRefreshNpc(){
		Set<Integer> keys = map2npc.keySet();
		if(keys!=null && keys.size()>0){
			for(int mapId : keys){
				NpcRefresh npcRefresh = map2npc.get(mapId);
				List<Clock> clocks = npcRefresh.clocks;
				List<Integer> npcs = npcRefresh.npcs;
				List<Position> positions = npcRefresh.positions;
				for(final Clock clock : clocks){
					Calendar cal = Calendar.getInstance();
					final int beginTime = clock.beginTime;
					int endTime = clock.endTime;
					int timeDis = clock.timeDis;
					if(in(cal, beginTime, endTime)){
						Server.server.scheduExec.schedule(new RefreshNpcTask(clock,mapId,timeDis,npcs,positions,true), 0, TimeUnit.MILLISECONDS);
					}
					Server.server.scheduExec.scheduleAtFixedRate(new RefreshNpcTask(clock,mapId,timeDis,npcs,positions,true), TimeUtil.getScheduleTimeMills(new Date(), beginTime, 0), ONEDAY, TimeUnit.MILLISECONDS);
					Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
						public void run() {
							try {
								List<RefreshNpcTask> list = cache.get(clock);
								for(RefreshNpcTask task : list){
									task.on = false;
								}
								removeNpcs();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}, TimeUtil.getScheduleTimeMills(new Date(), endTime, 0), ONEDAY, TimeUnit.MILLISECONDS);
					Server.server.scheduExec.scheduleAtFixedRate(new Runnable(){
						public void run() {
							Server.server.getServiceRegistry().getChatService()
							.sendWorldMessage("天降横财，2分钟后在西域的西北方向将会出现宝箱，大家快前往挖宝吧。");
						}
					}, TimeUtil.getScheduleTimeMills(new Date(), (beginTime-1), 58), ONEDAY, TimeUnit.MILLISECONDS);
				}
			}
		}
	}
	
	/** 每隔一定时间处理刷npc的任务 */
	class RefreshNpcTask implements Runnable{

		protected int mapId;
		protected int timeDis;
		protected List<Integer> npcs;
		protected List<Position> positions;
		protected boolean on;
		
		public void run() {
			this.on = true;
			while(on){
				if(npcs!=null && positions!=null){
					Calendar cal = Calendar.getInstance();
					Server.server.syncRunner.add(new Runnable(){
						public void run(){
							refreshNpc(mapId, npcs, positions);
						}
					});
					if(cal.get(Calendar.HOUR_OF_DAY)!=13 && cal.get(Calendar.HOUR_OF_DAY)!=21){
						Server.server.scheduExec.schedule(new Runnable(){
							public void run() {
								Server.server.getServiceRegistry().getChatService()
								.sendWorldMessage("天降横财，2分钟后在西域的西北方向将会出现宝箱，大家快前往挖宝吧。");
							}
						}, timeDis * 60 * 1000L - 2 * 60 * 1000L, TimeUnit.MILLISECONDS);
					}
					try {
						Thread.sleep(timeDis * 60 * 1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		public RefreshNpcTask(Clock clock, int mapId, int timeDis, List<Integer> npcs
				, List<Position> positions, boolean on) {
			this.mapId = mapId;
			this.npcs = npcs;
			this.positions = positions;
			this.timeDis = timeDis;
			this.on = on;
			List<RefreshNpcTask> tasks = cache.get(clock);
			if(tasks==null){
				List<RefreshNpcTask> list = new ArrayList<RefreshNpcTask>();
				list.add(this);
				cache.put(clock, list);
			}else{
				tasks.add(this);
			}
		}
		
		
	}

	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List<Element> list = root.elements();
		if(list!=null && list.size()>0){
			for(Element element : list){
				int mapId = Integer.parseInt(element.attributeValue("id"));
				NpcRefresh npcRefresh = new NpcRefresh();
				List<Element> npcs = element.elements("npc");
				for(Element element2 : npcs){
					int npcId = Integer.parseInt(element2.attributeValue("id"));
					npcRefresh.addNpc(npcId);
				}
				List<Element> positions = element.elements("position");
				for(Element element2 : positions){
					int x = Integer.parseInt(element2.attributeValue("x"));
					int y = Integer.parseInt(element2.attributeValue("y"));
					npcRefresh.addPosition(x, y);
				}
				List<Element> clocks = element.elements("clock");
				for(Element element2 : clocks){
					int beginTime = Integer.parseInt(element2.attributeValue("beginTime"));
					int endTime = Integer.parseInt(element2.attributeValue("endTime"));
					int timeDis = Integer.parseInt(element2.attributeValue("timeDis"));
					npcRefresh.addClock(beginTime, endTime, timeDis);
				}
				try {
					int deleteLimitLevel = Integer.parseInt(element.element("deleteLimitLevel").attributeValue("level"));
					DeletePlayerCall.DELTE_LIMIT_LEVEL = deleteLimitLevel;
				} catch (NumberFormatException e) {
	
				}
				map2npc.put(mapId, npcRefresh);
			}
		}
	}
	
	public void refreshNpc(int mapId, List<Integer> npcs, List<Position> positions) {
		removeNpcs();
		VMapManager manager = Server.server.getWorld().getVMapManager(mapId);
		VMap[] maps = ((NoInstanceVMapManager) manager).getVMaps(mapId);
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		List<Integer> temp = new ArrayList<Integer>();
		for(int npcId : npcs){
			GameMapObject gmo = GameMapObject.findByID(proj, npcId);
			int index = getRandomIndex(positions.size());
			while(temp.contains(index)){
				index = getRandomIndex(positions.size());
			}
			temp.add(index);
			for (VMap map : maps) {
				GameObject npc = VMapUtil.addCreature(map, positions.get(index).x, 
						positions.get(index).y, (GameMapNPC) gmo, true, 0, null);
				if(npc instanceof GatherUnit){
					((GatherUnit)npc).isPvp = true;
					((GatherUnit)npc).call = new NpcGatherEndCall();
				}
				npctemps.add(npc);
			}
		}
		ChatService chatService = Server.server.getServiceRegistry().getChatService();
		chatService.sendWorldMessage("Phía tây bắc Tây Vực phát ra ánh sang đỏ chói lòa, có người ở đó nhặt được bảo rương, mọi người đều đến đó để đào báu vật");
	}
	
	protected void removeNpcs(){
		for(GameObject npc : npctemps){
			if(npc!=null && npc.getVMap() != null){
				npc.removeFromWorld();
			}
		}
		npctemps.clear();
	}
	
	public int getRandomIndex(int size){
		Random random = new Random();
		return random.nextInt(size);
	}
	
	public Date getScheduleTime(Calendar cal,int hour,int min){
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
	
	public boolean in(Calendar cal, int beginTime, int endTime){
		int currentTime = cal.get(Calendar.HOUR_OF_DAY);
		return ((currentTime>beginTime) && (currentTime<endTime)) || (currentTime==beginTime);
	}
	
	public void shutdown() {

	}

}

class NpcRefresh{
	
	public List<Integer> npcs = new ArrayList<Integer>();
	public List<Position> positions = new ArrayList<Position>();
	public List<Clock> clocks = new ArrayList<Clock>();
	
	public void addNpc(int npcId){
		npcs.add(npcId);
	}
	
	public void addPosition(int x, int y){
		Position position = new Position(x, y);
		positions.add(position);
	}
	
	public void addClock(int beginTime, int endTime, int timeDis){
		Clock clock = new Clock(beginTime, endTime, timeDis);
		clocks.add(clock);
	}
	
}

class Position{
	
	public int x;
	public int y;
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
}

class Clock{
	
	public int beginTime;
	public int endTime;
	public int timeDis;
	
	public Clock(int beginTime, int endTime, int timeDis) {
		super();
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.timeDis = timeDis;
	}
	
}
