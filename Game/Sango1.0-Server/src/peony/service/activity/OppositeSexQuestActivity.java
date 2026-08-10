package peony.service.activity;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

import peony.game.ChatOption;
import peony.game.CommonUtil;
import peony.game.CreatureDieCallback;
import peony.game.DieCallback;
import peony.game.ErrorHandler;
import peony.game.GameMapDefinition;
import peony.game.GameObject;
import peony.game.MoveCallback;
import peony.game.NoInstanceVMapManager;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.VMapManager;
import peony.game.VMapUtil;

/**
 * 异性任务活动
 * @author dchen
 */
public class OppositeSexQuestActivity implements IActivityImpl, VMapManager {

	private static Logger log = Logger.getLogger(OppositeSexQuestActivity.class);
	
	protected Activity activity;
	
	private List<Player> players = new ArrayList<Player>();
	
	public VMap map = null;
	
	private static int[][] out = new int[][] { {}, { 896, 619, 148 },
			{ 896, 1312, 130 }, { 896, 87, 253 } };

	protected List<GameObject> npctemps = new ArrayList<GameObject>();
	
	@SuppressWarnings("unchecked")
	protected List[] npcs = new List[3];
	
	protected int currentCycle;

	public OppositeSexQuestActivity(Activity owner) {
		this.activity = owner;
	}

	public void clear() {

	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		try {
			map = ((NoInstanceVMapManager) Server.server.getWorld().getVMapManager(1361)).getVMaps(1361)[0];
		} catch (Exception e1) {
			map = ((OppositeSexQuestActivity) Server.server.getWorld().getVMapManager(1361)).map;
		}
		Server.server.getWorld().addVMapManager(this);
		Server.server.getWorld().registerVMapManager(1361, this);
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("oppositesex.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			log.error(e, e);
		}
		
	}

	public void save() {

	}

	/** 活动关闭 */
	public void shutdown() {
		transPlayers();
	}

	/** 活动开启 */
	@SuppressWarnings("unchecked")
	public void startup() throws Exception {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int time = hour * 60 + min;
		int num = (activity.schedule.timePeriods.length);
		int cycle = 0;
		for(int i=0;i<num;i+=2){
			if(between(activity.schedule.timePeriods[i], activity.schedule.timePeriods[i+1], time)){
				currentCycle = cycle;
			}
			cycle++;
		}
		RefrNpc(npcs[currentCycle]);
		Server.server.getServiceRegistry().getChatService().sendSystemMessage(
				ChatOption.SYSTEM, peony.Messages.STRING_00004,peony.Messages.STRING_00671);
	}

	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List<Element> list = root.elements();
		int count = 0;
		for (Element element : list) {
			List<Element> npcs = element.elements("npc");
			List<Npc> npcList = new ArrayList<Npc>();
			for (Element npc : npcs) {
				int npcId = Integer.parseInt(npc.attributeValue("id"));
				int x = Integer.parseInt(npc.attributeValue("x"));
				int y = Integer.parseInt(npc.attributeValue("y"));
				npcList.add(new Npc(npcId, x, y));
			}
			this.npcs[count] = npcList;
			count++;
		}
	}

	public VMap addToMap(Player player, int mapId, int x, int y, boolean check)
			throws VMapException {
		if (check) {
			if (activity.schedule.in()) {
				players.add(player);
				player.removeFromMap();
				map.addPlayer(player, x, y);
				return map;
			} else {
				int outMapId = out[player.faction][0];
				int outX = out[player.faction][1];
				int outY = out[player.faction][2];
				return Server.server.getWorld().addPlayerToMap(player, outMapId, outX, outY, check);
			}
		} else {
			if (activity.schedule.in()) {
				players.add(player);
				player.removeFromMap();
				map.addPlayer(player, x, y);
				return map;
			} else {
				ErrorHandler.sendErrorMessage(player.session, -1,
						OpCode.TELEPORT_CLIENT, peony.Messages.STRING_00672);
				return null;
			}
		}
	}

	/** 活动结束传送地图中所有人员 */
	protected void transPlayers() {
		for (GameObject o : map.instanceid2objects.values()) {
			if (o.type == GameObject.TYPE_PLAYER) {
				Player p = (Player) o;
				try {
					p.goMap(out[p.faction][0], out[p.faction][1], out[p.faction][2]);
				} catch (VMapException e) {
					e.printStackTrace();
				}
			}
		}
		players.clear();
	}

	/** 刷出NPC */
	protected void RefrNpc(List<Npc> npcs) {
		removeNpcs();
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		for (Npc npc : npcs) {
			GameMapObject gmo = GameMapObject.findByID(proj, npc.npcId);
			GameObject npc0 = VMapUtil.addCreature(map, npc.x, npc.y, (GameMapNPC) gmo, true, 0, null);
			npctemps.add(npc0);
		}
	}

	/** 移除NPC */
	protected void removeNpcs() {
		for (GameObject npc : npctemps) {
			if (npc != null && npc.getVMap() != null) {
				npc.removeFromWorld();
			}
		}
		npctemps.clear();
	}
	
	protected boolean between(int begin, int end, int cur){
		return ((cur>=begin) && (cur<=end));
	}

	public CreatureDieCallback creatureDieCallback() {
		return null;
	}

	public DieCallback dieCallback() {
		return null;
	}

	public void mapChanged(GameMapDefinition mapDef) {

	}

	public MoveCallback moveCallback() {
		return null;
	}

	public void outPrison(Player p) {

	}

	public void removeFromMap(Player player) {
		if(player!=null){
			Iterator<Player> it = players.iterator();
			while(it.hasNext()){
				Player p = it.next();
				if(p!=null && p.id==player.id)
					it.remove();
			}
		}
	}

	public void update(int diff) {

	}

}

class Npc {
	public int npcId;
	public int x;
	public int y;

	public Npc(int npcId, int x, int y) {
		this.npcId = npcId;
		this.x = x;
		this.y = y;
	}
}