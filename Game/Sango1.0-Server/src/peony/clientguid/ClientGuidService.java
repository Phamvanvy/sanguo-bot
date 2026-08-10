package peony.clientguid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.clientEvent.ClientEvent;
import com.pip.sanguo.data.clientEvent.EventTrigger;
import peony.clientguid.ClientGuid.Guid;
import peony.game.Player;
import peony.game.Server;
import peony.game.Unit;
import peony.game.VMap;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 客户端定制引导服务，辅助条件函数所需的数据
 * @author dchen
 */
public class ClientGuidService implements Service, ServiceEventListener {

	protected Map<String, Integer> function2triggerid = new HashMap<String, Integer>();
	protected Map<String, List<GuidTrigger>> function2trigger = new HashMap<String, List<GuidTrigger>>();
	
	public void startup() throws Exception {
		loadGuids(null);
		Server.server.getEventManager().registerListener(this);
	}
	
	/** 加载所有引导，以备监听 */
	public void loadGuids(ProjectData data){
		if(data==null)
			data = Server.server.getServiceRegistry().getDataService().data;
		List<DataObjectCategory> list = data.getCategoryListByType(ClientEvent.class);
		List<Guid> guids = new ArrayList<Guid>();
		for(DataObjectCategory cat : list){
			List<DataObject> objects = cat.objects;
			for(DataObject object : objects){
				if(object!=null && object instanceof ClientEvent){
					ClientEvent event = (ClientEvent)object;
					if(event.uiType == ClientEvent.UI_TYPE_NONE){
						continue;
					}
					Guid guid = new Guid();
					guid.id = event.id;
					guid.type = event.type;
					guid.uiType = event.uiType;
					guid.faction = event.faction;
					guid.eventItems = event.eventItems;
					for(EventTrigger et : event.triggers){
						GuidTrigger trigger = new GuidTrigger();
						trigger.id = et.type;
						trigger.functionName = et.condition;
						trigger.paramType = getParamType(et.paramType);
						trigger.paramValue = getParamValue(et.paramType, et.paramValue);
						trigger.paramSign = getParamSign(et.paramValue);
						guid.triggerList1.add(trigger);
						function2triggerid.put(trigger.functionName.toUpperCase(), trigger.id);
						List<GuidTrigger> triggers = function2trigger.get(trigger.functionName.toUpperCase());
						if(triggers==null)
							triggers = new ArrayList<GuidTrigger>();
						triggers.add(trigger);
					}
					for(EventTrigger et : event.triggers2){
						GuidTrigger trigger = new GuidTrigger();
						trigger.id = et.type;
						trigger.functionName = et.condition;
						trigger.paramType = getParamType(et.paramType);
						trigger.paramValue = getParamValue(et.paramType, et.paramValue);
						trigger.paramSign = getParamSign(et.paramValue);
						guid.triggerList2.add(trigger);
						function2triggerid.put(trigger.functionName.toUpperCase(), trigger.id);
						List<GuidTrigger> triggers = function2trigger.get(trigger.functionName.toUpperCase());
						if(triggers==null)
							triggers = new ArrayList<GuidTrigger>();
						triggers.add(trigger);
					}
					guid.repeatDuration = event.restartTime;
					guid.repeat = (guid.repeatDuration>0 ? true : false);
					guid.minLevel = event.suitLvlMin;
					guid.maxLevel = event.suitLvlMax;
					ClientGuid.id2guid.put(guid.id, guid);
					guids.add(guid);
				}
			}
			ClientGuid.guids = new Guid[guids.size()];
			ClientGuid.guids = guids.toArray(ClientGuid.guids);
		}
	}
	
	public void clearData(){
		function2triggerid.clear();
		function2trigger.clear();
		ClientGuid.id2guid.clear();
	}
	
	/** 解析参数类型 */
	public static int[] getParamType(String paramType){
		String[] str = paramType.split(",");
		int[] arr = new int[str.length];
		for(int i=0;i<str.length;i++){
			try {
				arr[i] = Integer.parseInt(str[i]);
			} catch (NumberFormatException e) {
			}
		}
		return arr;
	}
	
	/** 解析参数值 */
	public static Object[] getParamValue(String paramType, String paramValue){
		String[] str = paramValue.split(",");
		Object[] arr = new Object[str.length];
		for(int i=0;i<str.length;i++){
			int type = getParamType(paramType)[i];
			String str1 = str[i];
			if(!str1.contains(" ")){
				if(type==0){
					try {
						arr[i] = Integer.parseInt(str1);
					} catch (NumberFormatException e) {
						arr[i] = null;
					}
				}else if(type==1){
					arr[i] = str1;
				}
			}else{
				String[] str2 = str1.split(" ");
				if(type==0){
					try {
						arr[i] = Integer.parseInt(str2[1]);
					} catch (NumberFormatException e) {
						arr[i] = null;
					}
				}else if(type==1){
					arr[i] = str2[1];
				}
			}
		}
		return arr;
	}
	
	/** 解析参数符号 */
	public static int[] getParamSign(String paramValue){
		String[] str = paramValue.split(",");
		int[] arr = new int[str.length];
		for(int i=0;i<str.length;i++){
			String str1 = str[i];
			if(!str1.contains(" ")){
				arr[i] = -1;
			}else{
				String[] str2 = str1.split(" ");
				String sign = str2[0];
				if(sign.equalsIgnoreCase("<"))
					arr[i] = 0;
				else if(sign.equalsIgnoreCase("=="))
					arr[i] = 1;
				else if(sign.equalsIgnoreCase(">"))
					arr[i] = 2;
			}
		}
		return arr;
	}
	
	public int getIdByFunctionName(String functionName) throws Exception{
		Integer id = function2triggerid.get(functionName.toUpperCase());
		if(id==null)
			throw new Exception();
		return function2triggerid.get(functionName.toUpperCase());
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_PLAYER_LEVELUP,
				ServiceEvent.EVENT_MAP_PLAYER_ADDED,
				ServiceEvent.EVENT_GETITEM,
				ServiceEvent.EVENT_UNIT_DIE,
				ServiceEvent.EVENT_PLAYER_LOGOUTED,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			processPlayerEnterMap((VMap)event.param1,(Player)event.param2);
			break;
		case ServiceEvent.EVENT_GETITEM:
			processPlayerGetItem((Player)event.param1, ((Integer)event.param2).intValue());
			break;
		case ServiceEvent.EVENT_UNIT_DIE:
			processPlayerDie((Unit)event.param1, (Unit)event.param2);
			break;
		case ServiceEvent.EVENT_PLAYER_LOGOUTED:
			processPlayerLogOut((Player)event.param1);
			break;
		}
	}
	
	protected void processPlayerLogOut(Player player){
		if(player!=null){
			player.guid.clearLogOutData();
		}
	}
	
	protected void processPlayerDie(Unit dieUnit, Unit killUnit){
		if(dieUnit!=null && dieUnit instanceof Player && killUnit!=null && killUnit instanceof Player){
			Player diePlayer = (Player)dieUnit;
			Player killPlayer = (Player)killUnit;
			try {
				diePlayer.guid.setVarStore(String.valueOf(getIdByFunctionName("m_otherKillPlayer")), new int[]{1});
				killPlayer.guid.setVarStore(String.valueOf(getIdByFunctionName("m_playerKillOTHEN")), new int[]{1});
			} catch (Exception e) {
			}
		}
	}
	
	protected void processPlayerGetItem(Player player, int itemId){
		try {
			player.guid.setVarStore(String.valueOf(getIdByFunctionName("g_getItem")), new int[]{itemId});
		} catch (Exception e) {
		}
	}
	
	protected void processPlayerEnterMap(VMap map, Player player){
		if(map!=null && player!=null && player.id>0){
			try {
				player.guid.setVarStore(String.valueOf(getIdByFunctionName("m_enterScene")), new int[]{map.getId()});
			} catch (Exception e) {
			}
		}
	}
	
	public void recordTouchNpc(int npcId, Player player){
		try {
			player.guid.setVarStore(String.valueOf(getIdByFunctionName("g_openQuestGruide")), new int[]{npcId});
		} catch (Exception e) {
		}
	}

}
