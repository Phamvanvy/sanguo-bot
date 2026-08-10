package peony.service.activity;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapUtil;

public class FetchGiftActivity implements IActivityImpl {
	

	/**
	 * 活动期间刷出NPC的信息
	 */
	private int npcId = 3473577;
	private int mapId = 848;
	private int x = 779;
	private int y = 920;
	private GameObject npc;
	
	protected Activity activity;

	public void clear() {

	}

	public Activity getActivity() {
		return activity;
	}

	public FetchGiftActivity(Activity owner) {
		this.activity = owner;
	}


	public void load() {

	}

	public void save() {

	}

	public void shutdown() {
		//活动关闭时移除NPC
		if (npc != null)
			npc.removeFromWorld();
	}

	public void startup() throws Exception {
		//活动开始时刷出NPC
		refrNpc();
	}

	/** 刷出NPC */
	protected void refrNpc() {
		if (npc != null)
			npc.removeFromWorld();
		ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
		GameMapObject gmo = GameMapObject.findByID(proj, npcId);
		VMap map = ((NoInstanceVMapManager) Server.server.getWorld()
				.getVMapManager(mapId)).getVMaps(mapId)[0];
		npc = VMapUtil.addCreature(map, x, y, (GameMapNPC) gmo, true, 0, null);
	}
	
}



