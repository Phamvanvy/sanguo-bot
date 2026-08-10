package peony.service.activity;

import java.util.ArrayList;
import java.util.List;
import peony.game.GameObject;
import peony.game.GameQuest;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Server;
import peony.game.VMap;
import peony.game.VMapUtil;
import peony.game.drop.GroupDrop;
import peony.service.expansionbattle.ExpansionInstance;
import peony.service.expansionbattle.ExpansionService;
import peony.vm.ASMQuestUtil;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.util.Utils;
/**
 * 活动定时刷任务，npc，掉落组
 * @author pmeng
 */
public class QuestActivity implements IActivityImpl {

	protected Activity owner;
	protected int[] questIds;
	protected int[] npcIds;
	protected int[] mapIds;
	protected int[][] position; 
	protected int[] dropIds;
	protected List<GameObject> npcs = new ArrayList<GameObject>();
	
	/** configData格式    分左中右三部分    任务信息！npc信息！掉落组信息 （不管信息为不为空 必须2个！）
	 *  任务信息    		 questId1;questId2;...
	 *  npc信息      	 npcId1,mapId1,x1:y1;....
	 *  掉落组         	 groupDrop1;groupDrop2....
	 */
	public QuestActivity(Activity owner){
		this.owner = owner;
		String[] str = Utils.splitString(owner.configData, '!');
		if(str.length == 3 || str.length == 2){
			questIds = Utils.stringToIntArray(str[0], ';');
			String[] npcInfo = Utils.splitString(str[1], ';');
			npcIds = new int[npcInfo.length];
			mapIds = new int[npcInfo.length];
			position = new int[npcInfo.length][2];
			if(npcInfo.length > 0 && !npcInfo[0].equals("")){
				for(int i = 0;i < npcInfo.length;i++){
					String[] str3 = Utils.splitString(npcInfo[i], ',');
					npcIds[i] = Integer.parseInt(str3[0]);
					mapIds[i] = Integer.parseInt(str3[1]);
					position[i][0] = Integer.parseInt(Utils.splitString(str3[2], ':')[0]);
					position[i][1] = Integer.parseInt(Utils.splitString(str3[2], ':')[1]);
				}
			}else{
				npcIds = null;
			}
			if(str.length == 3){
				dropIds = Utils.stringToIntArray(str[2],';');
			}else{
				dropIds = null;
			}
		}
	}
	public void clear() {

	}

	public Activity getActivity() {
		return owner;
	}

	public void load() {

	}

	public void save() {

	}

	public void shutdown() {
		if(questIds != null && questIds.length > 0){
			for(int i:questIds){
				GameQuest quest = ASMQuestUtil.getGameQuest(i);
				if(quest != null){
					quest.closeQuest();
				}
			}
		}
		for(GameObject npc : npcs){
			npc.setInvisible();
			npc.removeFromWorld();
		}
		npcs.clear();
		if(dropIds != null && dropIds.length > 0){
			for(int k:dropIds){
				GroupDrop gd = ObjectAccessor.getGroupDrop(k);
				if(gd.isValid()){
					gd.setValid(false);
				}
			}
		}
	}
	
	public void startup() throws Exception {
		if(questIds != null && questIds.length > 0){
			for(int i:questIds){
				GameQuest quest = ASMQuestUtil.getGameQuest(i);
				if(quest != null){
					quest.openQuest();
				}
			}
		}
		if(npcIds != null && npcIds.length > 0){
			for(int i=0;i<npcIds.length;i++){
				int mapId = mapIds[i];
				int npcId = npcIds[i];
				ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
				GameMapObject gmo = GameMapObject.findByID(proj, npcId);
				VMap map = null;
				if(mapId == 2000){
					ExpansionService manager = (ExpansionService)Server.server.getWorld().getVMapManager(2000);
					map = manager.getMaps(2000).get(0);
				}else{
				    map = ((NoInstanceVMapManager) Server.server.getWorld()
						.getVMapManager(mapId)).getVMaps(mapId)[0];
				}
				if(map!=null){
					GameObject npc = VMapUtil.addCreature(map, position[i][0], position[i][1],
							(GameMapNPC) gmo, true, 0, null);
					npcs.add(npc);
					npc.setVisible();
				}
			}
		}
		if(dropIds != null && dropIds.length > 0){
			for(int k:dropIds){
				GroupDrop gd = ObjectAccessor.getGroupDrop(k);
				if(!gd.isValid()){
					gd.setValid(true);
				}
			}
		}
		
	}
}
