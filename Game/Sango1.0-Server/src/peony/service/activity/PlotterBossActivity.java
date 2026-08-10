package peony.service.activity;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.CommonUtil;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapUtil;
import peony.game.chat.ChatService;


public class PlotterBossActivity implements IActivityImpl {

	private static Logger log = Logger.getLogger(PlotterBossActivity.class);

	protected Map<Integer, RefreshNpc> npcs = new HashMap<Integer, RefreshNpc>();

	protected List<GameObject> npctemps = new ArrayList<GameObject>();

	public static final int DAY_START = 0; // 每天的开始时间(分钟为单位)

	public static final int DAY_END = 24 * 60; // 每天的结束时间(分钟为单位)
	
	public static final long ONEDAY = 24 * 3600 * 1000L;

	protected Timer timer = new Timer();

	protected Activity activity;

	public VMap map = null;

	public PlotterBossActivity(Activity owner) {
		this.activity = owner;
	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("plotterboss.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			log.error(e, e);
		}
	}

	@SuppressWarnings("unchecked")
	public void parse(Document doc) {
		Element root = doc.getRootElement();
		if (root != null) {
			List<Element> list = root.elements();
			for (Element l : list) {
				int id = Integer.parseInt(l.attributeValue("id"));
				RefreshNpc refreshNpc = new RefreshNpc(id);
				npcs.put(id, refreshNpc);
				List<Element> position = l.elements("position");
				for (Element p : position) {
					int mapId = Integer.parseInt(p.attributeValue("mapid"));
					int x = Integer.parseInt(p.attributeValue("x"));
					int y = Integer.parseInt(p.attributeValue("y"));
					String pos = p.attributeValue("pos");
					Position posi = new Position(mapId, x, y, pos);
					refreshNpc.addPosition(posi);
				}
				List<Element> clock = l.elements("clock");
				for (Element c : clock) {
					int startHour = Integer.parseInt(c.attributeValue("beginHour"));
					int startMinute = Integer.parseInt(c.attributeValue("beginMinute"));
					int endHour = Integer.parseInt(c.attributeValue("endHour"));
					int endMinute = Integer.parseInt(c
							.attributeValue("endMinute"));
					Clock clo = new Clock(startHour, startMinute, endHour,
							endMinute);
					refreshNpc.addClock(clo);
				}
			}
		}
	}

	protected void processRefreshNpc() {
		Set<Integer> keys = npcs.keySet();
		if (keys != null && keys.size() > 0) {
			for (int npcId : keys) {
				RefreshNpc npc = npcs.get(npcId);
				Position pos = npc.position.get(0);
				Clock clo = npc.clock.get(0);
				Calendar cal = Calendar.getInstance();
				map = ((NoInstanceVMapManager) Server.server.getWorld()
						.getVMapManager(pos.mapId)).getVMaps(pos.mapId)[0];
				if (in(cal, clo.startHour, clo.endHour, clo.endHour,
						clo.endMinute)) {
					refrNpc(npcId, pos, map);
				}
				timer.schedule(new RefreshNpcTask(npcId, pos, map),
						getScheduleTime(cal, clo.startHour, clo.startMinute),
						ONEDAY);
				timer.schedule(new TimerTask(){
				   public void run() {
				     removeNpc();
				  }
				}, getScheduleTime(cal,clo.endHour,clo.endMinute), ONEDAY);
			}
		}
	}

	public Date getScheduleTime(Calendar cal, int hour, int min) {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY, hour);
		cal1.set(Calendar.MINUTE, min);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		if (cal1.before(cal)) {
			cal1.add(Calendar.DAY_OF_MONTH, 1);
			return cal1.getTime();
		} else {
			return cal1.getTime();
		}
	}

	/** 每隔一定时间处理刷npc的任务 */
	class RefreshNpcTask extends TimerTask {

		public int npcId;
		public Position pos;
		public VMap map;

		public RefreshNpcTask(int npcId, Position pos, VMap map) {
			this.npcId = npcId;
			this.pos = pos;
			this.map = map;
		}

		@Override
		public void run() {
			Server.server.syncRunner.add(new Runnable() {
				public void run() {
					refrNpc(npcId, pos, map);
				}
			});

		}

	}

	/** 刷出NPC */
	protected void refrNpc(int npcId, Position position, VMap map) {
		removeNpc();
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		GameMapObject gmo = GameMapObject.findByID(proj, npcId);
		GameObject npc0 = VMapUtil.addCreature(map, position.x, position.y,
				(GameMapNPC) gmo, true, 0, null);
		String msg = MessageFormat.format(peony.Messages.STRING_01823,
				position.pos);
		ChatService chatService = Server.server.getServiceRegistry()
				.getChatService();
		chatService.sendWorldMessage(msg);
		npctemps.add(npc0);
	}

	/** 移除NPC */
	protected void removeNpc() {
		for (GameObject npc : npctemps) {
			if (npc != null && npc.getVMap() != null) {
				npc.removeFromWorld();
			}
		}
	}

	protected boolean in(Calendar cal, int startHour, int startMinute,
			int endHour, int endMinute) {
		int start = startHour * 60 + startMinute;
		int end = endHour * 60 + endMinute;
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minu = cal.get(Calendar.MINUTE);
		int v = hour * 60 + minu;
		if (start <= end) {
			return v >= start && v <= end;
		} else {
			return (v >= start && v <= DAY_END) || (v >= DAY_START && v <= end);
		}

	}

	public void save() {

	}

	public void shutdown() {
	     timer.cancel();
	     removeNpc();
	}

	public void startup() throws Exception {
		processRefreshNpc();

	}

	public void clear() {
		removeNpc();
	}

}

class RefreshNpc {
	int id;
	List<Position> position = new ArrayList<Position>();
	List<Clock> clock = new ArrayList<Clock>();

	public RefreshNpc(int id) {
		this.id = id;
	}

	public void addPosition(Position pos) {
		position.add(pos);
	}

	public void addClock(Clock clo) {
		clock.add(clo);
	}
}

class Position {
	public int mapId;
	public int x;
	public int y;
	public String pos;

	public Position(int mapId, int x, int y, String pos) {
		this.mapId = mapId;
		this.x = x;
		this.y = y;
		this.pos = pos;
	}
}

class Clock {
	public int startHour;
	public int startMinute;
	public int endHour;
	public int endMinute;

	public Clock(int startHour, int startMinute, int endHour, int endMinute) {
		this.startHour = startHour;
		this.startMinute = startMinute;
		this.endHour = endHour;
		this.endMinute = endMinute;
	}
}
