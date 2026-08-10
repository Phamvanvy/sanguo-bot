package peony.service.activity;

import java.util.Iterator;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.game.CycleListener;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.service.ServiceEvent;

public class WeatherActivity1 implements IActivityImpl, CycleListener {

	private static Logger log = Logger.getLogger(WeatherActivity.class);

	protected Activity activity;
	
	protected WeatherConfig config;
	
	protected boolean isRainning;
	
	protected long lastProTime = 0;
	
	protected static Random random = new Random();
	
	public WeatherActivity1(Activity owner){
		this.activity = owner;
	}
	
	protected void parseConfig(String config){
		int timeDis = 0;
		int[] maps = null;
		int credit = 0;
		int itemId = 0;
		int ratio = 0;
		String[] str0 = config.split(";");
		for(String str1 : str0){
			String[] str = str1.split(":");
			if(str[0].equals("timedis")){
				timeDis = Integer.parseInt(str[1]);
			}else if(str[0].equals("map")){
				String[] temp = str[1].split(",");
				maps = new int[temp.length];
				for(int i=0;i<temp.length;i++){
					maps[i] = Integer.parseInt(temp[i]);
				}
			}else if(str[0].equals("credit")){
				credit = Integer.parseInt(str[1]);
			}else if(str[0].equals("item")){
				itemId = Integer.parseInt(str[1]);
			}else if(str[0].equals("ratio")){
				ratio = Integer.parseInt(str[1]);
			}
		}
		this.config = new WeatherConfig(timeDis, maps, credit, itemId, ratio);
	}

	public void clear() {
		
	}

	public Activity getActivity() {
		return null;
	}

	public void load() {
		String config = activity.configData;
		parseConfig(config);
	}

	public void save() {
		
	}

	public void shutdown() {
		isRainning = false;
	}

	public void startup() throws Exception {
		isRainning = true;
	}
	
	protected void processRain(){
		for(int mapId : config.maps){
			VMap map = ((NoInstanceVMapManager)Server.server.getWorld().getVMapManager(mapId)).getVMaps(mapId)[0];
			Iterator<GameObject> it = map.instanceid2objects.iterator();
			while(it.hasNext()){
				GameObject ref = it.next();
				if(ref==null)
					continue;
				if(!(ref instanceof Player))
					continue;
				Player p = ObjectAccessor.getPlayer(ref.id);
				if(p!=null && p.isAlive()){
					long lastGetWEATHERACT1 = p.pool.getLong("WEATHERACT1", 0);
					if((System.currentTimeMillis()-lastGetWEATHERACT1)>=config.timeDis*60000){
						PlayerTransaction tx = p.newTransaction("WEATHERACT1");
						p.addCredit(config.credit, tx, true);
						if(random.nextInt(100)<=config.ratio){
							p.bag.addGameItem(ObjectAccessor.createGameItem(config.itemId), 1, tx, true);
						}
						tx.commit();
						p.pool.setLong("WEATHERACT1", System.currentTimeMillis());
					}
				}
			}
		}
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_MAP_PLAYER_ADDED,
				ServiceEvent.EVENT_MAP_PLAYER_REMOVED,
		};
	}

	public boolean update(int diff) {
		if(System.currentTimeMillis()-lastProTime>=10000){
			processRain();
			lastProTime = System.currentTimeMillis();
		}
		return false;
	}

}

class WeatherConfig{
	
	public int timeDis; //时间间隔
	public int[] maps; //生效地图ID
	public int credit; //战功
	public int itemId; //物品
	public int ratio; //掉率
	
	public WeatherConfig(int timeDis, int[] maps, int credit, int itemId, int ratio){
		this.timeDis = timeDis;
		this.maps = maps;
		this.credit = credit;
		this.itemId = itemId;
		this.ratio = ratio;
	}
	
}
