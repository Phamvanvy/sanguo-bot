package peony.service.activity;

import java.util.ArrayList;
import java.util.List;

import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapUtil;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;

public class BeautyActivity implements IActivityImpl {
	
	protected int[] mapIds = {272, 240, 352};
	
	protected int[] npcIds = {1114181,983074,1441838};
	
	protected int[][] position = {{666,338},{581,657},{810,303}};
	
	protected List<GameObject> npcs = new ArrayList<GameObject>();
	
	private Activity activity;
	
	public BeautyActivity(Activity owner){
		this.activity = owner;
	}

	public void clear() {
		
	}

	public Activity getActivity() {
		return activity;
	}

	public void load() {
		
	}

	public void save() {
		
	}

	public void shutdown() {
		for(GameObject npc : npcs){
			npc.removeFromWorld();
		}
	}

	public void startup() throws Exception {
		for(int i=0;i<npcIds.length;i++){
			int mapId = mapIds[i];
			int npcId = npcIds[i];
			ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
			GameMapObject gmo = GameMapObject.findByID(proj, npcId);
			VMap map = ((NoInstanceVMapManager) Server.server.getWorld()
					.getVMapManager(mapId)).getVMaps(mapId)[0];
			GameObject npc = VMapUtil.addCreature(map, position[i][0], position[i][1],
					(GameMapNPC) gmo, true, 0, null);
			npcs.add(npc);
		}
	}

}
