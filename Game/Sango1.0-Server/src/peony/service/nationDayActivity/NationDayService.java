package peony.service.nationDayActivity;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

import peony.db.RefreshNpcCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapUtil;
import peony.game.nation.Nation;
import peony.game.nation.NationService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;

public class NationDayService implements Service {

	public int[] mapId = { 272, 240, 352 };

	protected int posX[][] = { { 112, 112 },{ 103, 105 }, { 122,122 } };

	protected int posY[][] = { { 280, 280 },{ 214, 222 }, { 298, 298 } };

	protected int npcId[][] = { { 1114162,1114188 },{ 983113, 983114 }, { 1441862,1441863 } };

	public int[] faction = { GameObject.FACTION_WEI, GameObject.FACTION_SHU,
			GameObject.FACTION_WU };

	protected List<GameObject> npctemps = new ArrayList<GameObject>();

	protected Map<Integer, Integer> index = new HashMap<Integer, Integer>();

	protected static final Long ONEDAY = 24 * 60 * 60 * 1000L; // 一天时间

	public void shutdown() {

	}

	public void startup() throws Exception {
		Server.server.scheduExec.scheduleAtFixedRate(new Runnable() {
			public void run() {
				Server.server.syncRunner.add(new Runnable() {
					public void run() {
//						refreshNpc();
						RefreshNpcCall call = new RefreshNpcCall(RefreshNpcCall.NATIONDAY);
						Server.server.getWorld().schedule(call);
					}
				});
			}
		}, getScheduleTime(Calendar.getInstance()), ONEDAY, TimeUnit.MILLISECONDS);
		processFresh();
	}

	public long getScheduleTime(Calendar cal) {
		Calendar cal1 = Calendar.getInstance();
		cal1.set(Calendar.HOUR_OF_DAY, 0);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);
		if (cal1.before(cal)) {
			cal1.add(Calendar.DAY_OF_YEAR, 1);
			return cal1.getTime().getTime()-System.currentTimeMillis();
		} else {
			return cal1.getTime().getTime()-System.currentTimeMillis();
		}
	}

	/**
	 * 提交密信
	 */
	public void handIn(Packet packet, ClientSession session) {
		synchronized (this) {
			Player p = (Player) session.getClient();
			if (p != null) {
				int serial = packet.getInt();
				int count = packet.getInt();
				if (count <= 0) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HANDIN_LETTER_CLIENT, peony.Messages.STRING_00894);
					return;
				}
				PlayerTransaction tx = p.newTransaction("HANDINLETTER");
				int itemId = 1154;
				GameItem item = p.bag.removeGameItemIngoreInstanceId(itemId,
						count, tx, true);
				if (item == null) {
					tx.rollback();
					String msg = MessageFormat.format(peony.Messages.STRING_00895, ObjectAccessor
							.getItemTemplate(itemId).name);
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.HANDIN_LETTER_CLIENT, msg);
					return;
				}
				if(p.pool.getInt(Player.WELFARE_SUBMIT_LETTER,0)==0){
					p.pool.setInt(Player.WELFARE_SUBMIT_LETTER, 1);
					Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_WELFARE_FINISH,p));
				}
				p.addExp(count*p.level*20, tx, true);
				tx.commit();
				Nation nation = Server.server.getServiceRegistry()
						.getNationService().getNationByFaction(p.faction);
				int cnt = nation.pool.getInt(
						Nation.PROPERTY_SECRETLETTER_COUNT, 0);
				nation.pool.setInt(Nation.PROPERTY_SECRETLETTER_COUNT, cnt
						+ count);
				Packet pt = new Packet(OpCode.HANDIN_LETTER_SERVER);
				pt.putInt(serial);
				p.send(pt);
			}
		}
	}

	/**
	 * 刷新倭寇女王或侍女
	 */
	public void refreshNpc() {
		NationService service = Server.server.getServiceRegistry()
		.getNationService();
		removeNpc();
		npctemps.clear();
		getIndex();//获取各国的序号，0刷出女王，1刷出侍女
		processFresh();
		for(int i = 0; i < faction.length; i++) {
			Nation nation = service.getNationByFaction(faction[i]);
			nation.pool.setInt(Nation.PROPERTY_SECRETLETTER_COUNT, 0);
		}
	}
	
	protected void processFresh(){
		NationService service = Server.server.getServiceRegistry()
		.getNationService();
		for (int i = 0; i < faction.length; i++) {
			Nation nation = service.getNationByFaction(faction[i]);
			int index = nation.pool.getInt(Nation.PROPERTY_INDEX, -1);
			if (index != -1) {
				VMap map = ((NoInstanceVMapManager) Server.server.getWorld()
						.getVMapManager(mapId[i])).getVMaps(mapId[i])[0];
				ProjectData proj = Server.server.getServiceRegistry()
						.getDataService().data;
				GameMapObject gmo = GameMapObject.findByID(proj, npcId[i][index]);
				GameObject npc0 = VMapUtil.addCreature(map, posX[i][index],
						posY[i][index], (GameMapNPC) gmo, true, 0, null);
				npctemps.add(npc0);
			}
        }
	}

	public void getIndex() {
		NationService service = Server.server.getServiceRegistry()
				.getNationService();
		Nation nation1 = service.getNationByFaction(faction[0]);
		Nation nation2 = service.getNationByFaction(faction[1]);
		Nation nation3 = service.getNationByFaction(faction[2]);
		nation1.pool.setInt(Nation.PROPERTY_INDEX, -1);
		nation2.pool.setInt(Nation.PROPERTY_INDEX, -1);
		nation3.pool.setInt(Nation.PROPERTY_INDEX, -1);
		if (nation1.pool.getInt(Nation.PROPERTY_SECRETLETTER_COUNT, 0) == 0
				&& nation2.pool.getInt(Nation.PROPERTY_SECRETLETTER_COUNT, 0) == 0
				&& nation3.pool.getInt(Nation.PROPERTY_SECRETLETTER_COUNT, 0) == 0) {
			nation1.pool.setInt(Nation.PROPERTY_INDEX, -1);
			nation2.pool.setInt(Nation.PROPERTY_INDEX, -1);
			nation3.pool.setInt(Nation.PROPERTY_INDEX, -1);
		} else {
			for (int i = 0; i < faction.length; i++) {
				List<Integer> list = getFaction(faction[i]);
				int count = 0;
				Nation nation = service.getNationByFaction(faction[i]);
				int cnt = nation.pool.getInt(
						Nation.PROPERTY_SECRETLETTER_COUNT, 0);
				for (int j = 0; j < list.size(); j++) {
					Nation nations = service.getNationByFaction(list.get(j));
					int cnt2 = nations.pool.getInt(
							Nation.PROPERTY_SECRETLETTER_COUNT, 0);
					if (cnt >= cnt2)
						count++;
				}
				if (count == 2) {
					nation.pool.setInt(Nation.PROPERTY_INDEX, 0);
					for (int k = 0; k < list.size(); k++) {
						Nation na = service.getNationByFaction(list.get(k));
						na.pool.setInt(Nation.PROPERTY_INDEX, 1);
					}
					break;
				}
			}
		}
	}

	public List<Integer> getFaction(int faId) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < faction.length; i++) {
			if (faId != faction[i])
				list.add(faction[i]);
		}
		return list;
	}

	/** 移除NPC*/
	public void removeNpc() {
		for (GameObject npc : npctemps) {
			if (npc != null && npc.getVMap() != null) {
				npc.removeFromWorld();
			}
		}
	}

}
