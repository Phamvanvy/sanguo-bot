package peony.game.directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.DirectoryType;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.clientEvent.EventTrigger;
import peony.clientguid.ClientGuidService;
import peony.clientguid.GuidTrigger;
import peony.game.Player;
import peony.game.Server;
import peony.game.Unit;
import peony.game.VMap;
import peony.game.directory.ClientDirectory.Directory;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 引导系统数据服务
 * @author dchen
 */
public class DirectoryService implements Service, ServiceEventListener {
	
	protected Map<String, Integer> function2triggerid = new HashMap<String, Integer>();
	protected Map<String, List<GuidTrigger>> function2trigger = new HashMap<String, List<GuidTrigger>>();
	
	public void startup() throws Exception {
		loadDirectorys(null);
		Server.server.getEventManager().registerListener(this);
	}
	
	public void loadDirectorys(ProjectData data){
		if(data==null)
			data = Server.server.getServiceRegistry().getDataService().data;
		ClientDirectory.id2directory.clear();
		function2triggerid.clear();
		function2trigger.clear();
		List<DataObjectCategory> list = data.getCategoryListByType(DirectoryType.class);
		List<Directory> list1 = new ArrayList<Directory>();
		for(DataObjectCategory cat : list){
			List<DataObject> objects = cat.objects;
			for(DataObject object : objects){
				if(object!=null && object instanceof DirectoryType){
					DirectoryType directoryType = (DirectoryType)object;
					Directory directory = new Directory();
					directory.id = directoryType.id;
					directory.name = directoryType.title;
					directory.description = directoryType.description1;
					directory.directoryList = directoryType.directoyList;
					directory.faction = directoryType.faction;
					directory.clazz = directoryType.clazz;
					directory.location = directoryType.location;
					for(int i=0;i<10;i+=2){
						int minScore = 0;
						try {minScore = directoryType.scores.get(i+1);} catch (Exception e) {}
						int maxScore = 0;
						try{maxScore = directoryType.scores.get(i+2);}catch(Exception e){}
						directory.scores[i] = minScore;
						directory.scores[i+1] = maxScore;
					}
					directory.timeScheduleHour = directoryType.timeScheduleHour;
					directory.timeScheduleMin = directoryType.timeScheduleMin;
					directory.weeks = directoryType.weeks;
					directory.exp = directoryType.exp;
					directory.money = directoryType.money;
					directory.rank = directoryType.rank;
					directory.pearl = directoryType.pearl;
					directory.item1 = directoryType.item1;
					directory.count1 = directoryType.count1;
					directory.item2 = directoryType.item2;
					directory.count2 = directoryType.count2;
					directory.item3 = directoryType.item3;
					directory.count3 = directoryType.count3;
					directory.item4 = directoryType.item4;
					directory.count4 = directoryType.count4;
					directory.rewardDesc = directoryType.rewardDesc;
					directory.minLevel = directoryType.minLevel;
					directory.maxLevel = directoryType.maxLevel;
					directory.difficulty = directoryType.difficulty;
					directory.salary = directoryType.salary;
					for(EventTrigger et : directoryType.triggers){
						GuidTrigger trigger = new GuidTrigger();
						trigger.id = et.type;
						trigger.functionName = et.condition;
						trigger.paramType = ClientGuidService.getParamType(et.paramType);
						trigger.paramValue = ClientGuidService.getParamValue(et.paramType, et.paramValue);
						trigger.paramSign = ClientGuidService.getParamSign(et.paramValue);
						directory.triggerList.add(trigger);
						function2triggerid.put(trigger.functionName.toUpperCase(), trigger.id);
						List<GuidTrigger> triggers = function2trigger.get(trigger.functionName.toUpperCase());
						if(triggers==null)
							triggers = new ArrayList<GuidTrigger>();
						triggers.add(trigger);
					}
					ClientDirectory.id2directory.put(directory.id, directory);
					list1.add(directory);
				}
			}
		}
		ClientDirectory.alldirectory = new Directory[list1.size()];
		ClientDirectory.alldirectory = list1.toArray(ClientDirectory.alldirectory);
		ClientDirectory.exp2directory = bubbleDirectorys(list1, 0);
		ClientDirectory.money2directory = bubbleDirectorys(list1, 1);
		ClientDirectory.rank2directory = bubbleDirectorys(list1, 2);
		ClientDirectory.pearl2directory = bubbleDirectorys(list1, 3);
	}
	
	private Directory[] bubbleDirectorys(List<Directory> list, int type){
		int var = 0;
		int var1 = 0;
		Directory[] directorytemps = new Directory[list.size()];
		directorytemps = list.toArray(directorytemps);
		for(int i=0;i<directorytemps.length;i++){
			for(int j=i+1;j<directorytemps.length;j++){
				Directory temp = null;
				if(type==0){
					var = directorytemps[i].exp;
					var1 = directorytemps[j].exp;
				}else if(type==1){
					var = directorytemps[i].money;
					var1 = directorytemps[j].money;
				}else if(type==2){
					var = directorytemps[i].rank;
					var1 = directorytemps[j].rank;
				}else if(type==3){
					var = directorytemps[i].pearl;
					var1 = directorytemps[j].pearl;
				}
				if(var<var1){
					temp = directorytemps[i];
					directorytemps[i] = directorytemps[j];
					directorytemps[j] = temp;
				}
			}
		}
		return directorytemps;
	}
	
	public int getIdByFunctionName(String functionName) throws Exception{
		Integer id = function2triggerid.get(functionName.toUpperCase());
		if(id==null)
			throw new Exception();
		return function2triggerid.get(functionName.toUpperCase());
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_MAP_PLAYER_ADDED,
				ServiceEvent.EVENT_UNIT_DIE,
				ServiceEvent.EVENT_GETITEM,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			processPlayerEnterMap((VMap)event.param1,(Player)event.param2);
			break;
		case ServiceEvent.EVENT_UNIT_DIE:
			processPlayerDie((Unit)event.param1, (Unit)event.param2);
			break;
		case ServiceEvent.EVENT_GETITEM:
			processPlayerGetItem((Player)event.param1, ((Integer)event.param2).intValue());
			break;
		}
	}
	
	protected void processPlayerGetItem(Player player, int itemId){
		try {
			player.directory.setVarStore(String.valueOf(getIdByFunctionName("g_getItem")), new int[]{itemId});
		} catch (Exception e) {
		}
	}
	
	protected void processPlayerDie(Unit dieUnit, Unit killUnit){
		if(dieUnit!=null && dieUnit instanceof Player && killUnit!=null && killUnit instanceof Player){
			Player diePlayer = (Player)dieUnit;
			Player killPlayer = (Player)killUnit;
			try {
				diePlayer.directory.setVarStore(String.valueOf(getIdByFunctionName("m_otherKillPlayer")), new int[]{1});
				killPlayer.directory.setVarStore(String.valueOf(getIdByFunctionName("m_playerKillOTHEN")), new int[]{1});
			} catch (Exception e) {
			}
		}
	}
	
	protected void processPlayerEnterMap(VMap map, Player player){
		if(map!=null && player!=null && player.id>0){
			try {
				player.directory.setVarStore(String.valueOf(getIdByFunctionName("m_enterScene")), new int[]{map.getId()});
			} catch (Exception e) {
			}
		}
	}
	
	public void recordTouchNpc(int npcId, Player player){
		try {
			player.directory.setVarStore(String.valueOf(getIdByFunctionName("g_openQuestGruide")), new int[]{npcId});
		} catch (Exception e) {
		}
	}
	
	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}
	
}
