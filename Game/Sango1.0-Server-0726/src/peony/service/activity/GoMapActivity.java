package peony.service.activity;

import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.buff.BuffUtil;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 传送从指定地图到指定目标地图增加指定BUFF活动
 * @author dchen
 */
public class GoMapActivity implements IActivityImpl, ServiceEventListener {

	public static int[] sourceMapId = {1395, 1395, 1395};
	public static int[] targetMapId = {1410, 1426, 1442};
	public static int BUFF = 396;
	public static String lastMap = "lastmapid";
	private Activity activity;
	
	public void clear() {
		
	}

	public GoMapActivity(Activity activity){
		this.activity = activity;
	}
	
	public Activity getActivity() {
		return activity;
	}

	public void load() {
		
	}

	public void save() {
		
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[]{
			ServiceEvent.EVENT_MAP_PLAYER_LOADED	
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_MAP_PLAYER_LOADED:
			processGoMap((VMap)event.param1, (Player)event.param2);
		}
	}
	
	private void processGoMap(VMap map, Player player){
		if(map!=null && player!=null){
			int lastMapId = player.pool.getInt(lastMap, 0);
			int currentMapId = map.getId();
			int sourceIndex = getindex(sourceMapId, currentMapId);
			int targetIndex = getindex(targetMapId, currentMapId);
			if(sourceIndex>-1)
				player.pool.setInt(lastMap, currentMapId);
			if(targetIndex>-1 && lastMapId==sourceMapId[targetIndex] && currentMapId==targetMapId[targetIndex]){
				player.buffs.addBuff(BuffUtil.createBuff(BUFF, 1, player, player, 0));
				String content = "新兵好礼享不停，恭喜您获得了1个小时的双倍经验时间及攻击增强时间，快去把自己磨练得更强大吧！";
				player.message(-1, content, -1, -1);
				player.pool.remove(lastMap);
			}
		}
	}
	
	private int getindex(int[] arr, int mapId){
		for(int i=0;i<arr.length;i++){
			if(arr[i]==mapId)
				return i;
		}
		return -1;
	}

}
