package peony.service.pluginstance;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMap;
import peony.game.VMapException;
import peony.game.instance.NormalInstance;
import peony.game.instance.NormalVMapManager;
import peony.game.mail.MailService;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * ÖÇÓÂ´ó³å¹Ø
 * @author dchen
 */
public class GoInstanceServcie implements Service, ServiceEventListener {

	protected static int[] maps = { 1872, 1888, 1904, 1920 };
	protected static int[] times = { 6 * 60 * 1000, 5 * 60 * 1000, 4 * 60 * 1000, 3 * 60 * 1000 };
	protected static int[] out = {816, 400, 250};
	protected static int rewardItem = 3614;
	protected static int[] count = {1, 1, 2, 3};
	protected long lastUpdateTime;
	protected Map<Integer, Boolean> shouts = new HashMap<Integer, Boolean>();
	protected Map<Integer, Boolean> instanceStat = new HashMap<Integer, Boolean>();
	
	public void startup() throws Exception {
		Server.server.getEventManager().registerListener(this);
	}
	
	public void update(){
		try {
			if(System.currentTimeMillis()-lastUpdateTime>5000){
				for(int i=0;i<maps.length;i++){
					int mapId = maps[i];
					NormalVMapManager manager = (NormalVMapManager) Server.server.getWorld().getVMapManager(mapId);
					List<VMap> maps = manager.getMaps(mapId);
					for(VMap map : maps){
						if(map!=null && map.instance!=null){
							NormalInstance instance = (NormalInstance) map.instance;
							if(Time.currTime-instance.createTime>=getInstanceDurationTime(mapId)-60000 && (shouts.get(instance.id)==null || 
									!shouts.get(instance.id))){
								for(GameObject go : map.instanceid2objects.values()){
									if(go!=null && go.type==GameObject.TYPE_PLAYER){
										Player player = (Player)go;
										Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, 
										peony.Messages.STRING_01002);
									}
								}
								shouts.put(instance.id, true);
							}
							if(Time.currTime-instance.createTime>=getInstanceDurationTime(mapId) && 
									(instanceStat.get(instance.id)==null || !instanceStat.get(instance.id))){
								out(map);
								instanceStat.put(instance.id, true);
								instance.timeOut = true;
							}
						}
					}
				}
				lastUpdateTime = System.currentTimeMillis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void out(VMap map){
		for(GameObject go : map.instanceid2objects.values()){
			try {
				if(go!=null && go.type==GameObject.TYPE_PLAYER){
					Player p = (Player)go;
//					PlayerTransaction tx = p.newTransaction("GOGOINSTANCE");
//					GameItem item = ObjectAccessor.createGameItem(rewardItem);
//					int mapIndex = getGoInstanceMapIndex(map.getId());
//					try {
//						p.bag.addGameItemComplete(item, count[mapIndex], tx, false);
//						tx.commit();
//					} catch (Exception e1) {
//						tx.rollback();
//						MailService service = Server.server.getServiceRegistry().getMailService();
//						service.sendSystemMail(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01003, "", 0, item, 1, "GOGOINSTANCE");
//					}
					try {
						p.goMap(out[0],  out[1], out[2]);
					} catch (VMapException e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_MAP_PLAYER_ADDED
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
		case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
			processPlayerGoMap((VMap)event.param1, (Player)event.param2);
			break;
		}
	}
	
	protected void processPlayerGoMap(VMap map0, Player player){
		if(map0!=null && player!=null && getGoInstanceMapIndex(map0.getId())!=-1){
			NormalVMapManager manager = (NormalVMapManager) Server.server.getWorld().getVMapManager(map0.getId());
			List<VMap> maps = manager.getMaps(map0.getId());
			for(VMap map : maps){
				if(map!=null && map.instance!=null){
					NormalInstance instance = (NormalInstance) map.instance;
					int insCreateTime = instance.createTime;
					int duration = getInstanceDurationTime(map.getId());
					int dur = Time.currTime - insCreateTime;
					if(dur<duration){
						if((duration-dur)/(1000*60)>0){
							Server.server.getServiceRegistry().getChatService().sendPrivateMessage(player.id, 
									MessageFormat.format(peony.Messages.STRING_01004, 
											(duration-dur)/(1000*60)));
						}
					}
				}
			}
		}
	}
	
	protected int getGoInstanceMapIndex(int mapId){
		for(int i=0;i<maps.length;i++){
			int m = maps[i];
			if(mapId==m)
				return i;
		}
		return -1;
	}
	
	protected int getInstanceDurationTime(int mapId){
		int index = getGoInstanceMapIndex(mapId);
		if(index!=-1)
			return times[index];
		return -1;
	}

}
