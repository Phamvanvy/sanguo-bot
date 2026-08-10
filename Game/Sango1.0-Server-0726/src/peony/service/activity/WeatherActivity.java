package peony.service.activity;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

/**
 * 活动 经验雨
 * @author dchen
 */
public class WeatherActivity implements IActivityImpl, ServiceEventListener, Runnable {

	private static Logger log = Logger.getLogger(WeatherActivity.class);
	
	protected Activity activity;
	
	protected List<WeatherGift> weatherGifts = new ArrayList<WeatherGift>();
	
	protected HashMap<Integer, List<GameObjectRef>> players = new HashMap<Integer, List<GameObjectRef>>();
	
	private Random random = new Random();

	public WeatherActivity(Activity owner){
		this.activity = owner;
	}
	
	public void clear() {
		
	}

	public Activity getActivity() {
		return null;
	}

	public void load() {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data.findFile("weathergift.xml");
		try {
			Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
			parse(doc);
		} catch (Exception e) {
			log.error(e, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List<Element> list = root.elements("gift");
		for (Element element : list) {
			int mapId = Integer.parseInt(element.attributeValue("mapId"));
			int weatherType = Integer.parseInt(element.attributeValue("weatherType"));
			int exp = Integer.parseInt(element.attributeValue("exp"));
			int expHZ = Integer.parseInt(element.attributeValue("expHZ"));
			String items = element.attributeValue("itemId");
			String[] itemStrs = items.split(",");
			int[] itemId = new int[itemStrs.length];
			for(int i=0;i<itemId.length;i++){
				itemId[i] = Integer.parseInt(itemStrs[i]);
			}
			String periods = element.attributeValue("period");
			WeatherGift gift = new WeatherGift(mapId, weatherType, exp, expHZ, itemId, periods);
			this.weatherGifts.add(gift);
			ArrayList<GameObjectRef> playerList = new ArrayList<GameObjectRef>();
			players.put(mapId, playerList);
		}
	}

	public void save() {
		
	}

	public void shutdown() {
		Server.server.getEventManager().unregisterListener(this);
	}

	public void startup() throws Exception {
//		for(WeatherGift gift : weatherGifts){
//			int mapId = gift.mapId;
//			int weatherType = gift.weatherType;
//			WeatherService weatherService = Server.server.getServiceRegistry().getWeatherService();
//			WeatherDef weatherDef = new WeatherDef(mapId, weatherType);
//			int[] persInt = gift.getPerInt();
//			for(int i=0;i<persInt.length/2;i+=2){
//				weatherDef.addPeriod(new Period(persInt[i], 0, persInt[i+1], 0));
//			}
//			weatherService.schedule(weatherDef);
//		}
		Server.server.getEventManager().registerListener(this);
		new Thread(this).start();
	}

	public int[] getEventTypes() {
		return new int[]{
				ServiceEvent.EVENT_MAP_PLAYER_ADDED,
				ServiceEvent.EVENT_MAP_PLAYER_REMOVED,
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch(event.type){
			case ServiceEvent.EVENT_MAP_PLAYER_ADDED:
				processGoMap((VMap)event.param1, (Player)event.param2);
				break;
			case ServiceEvent.EVENT_MAP_PLAYER_REMOVED:
				processRemoveMap((VMap)event.param1, (Player)event.param2);
				break;
		}
	}
	
	protected void processGoMap(VMap map, Player p){
		if(p!=null){
			List<GameObjectRef> list = players.get(map.getId());
			if(list!=null){
				list.add(p.ref());
				p.pool.setLong("WEATHEREXPTIME", System.currentTimeMillis());
			}
		}
	}
	
	protected void processRemoveMap(VMap map, Player p){
		if(p!=null){
			List<GameObjectRef> list = players.get(map.getId());
			if(list!=null){
				Iterator<GameObjectRef> it = list.iterator();
				while(it.hasNext()){
					if(it.next().id==p.id){
						it.remove();
					}
				}
			}
		}
	}
	
	public boolean isRainning(int mapId){
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		for(WeatherGift gift : weatherGifts){
			if(gift.mapId==mapId && gift.in(hour)){
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("static-access")
	public void run() {
		while(this.activity.active && this.activity.getSchedule().in()){
			try {
				Thread.currentThread().sleep(10000);
			} catch (InterruptedException e) {
			}
			for(WeatherGift gift : weatherGifts){
				int mapId = gift.mapId;
				if(isRainning(mapId)){
					for(GameObjectRef ref : players.get(mapId)){
						if(ref==null)
							continue;
						Player p = ObjectAccessor.getPlayer(ref.id);
						if(p!=null && p.isAlive()){
							long lastGetWeatherExpTime = p.pool.getLong("WEATHEREXPTIME", 0);
							if((System.currentTimeMillis()-lastGetWeatherExpTime)>=gift.expHZ){
								PlayerTransaction tx = p.newTransaction("WEATHEREXP");
								p.addExp(gift.exp*p.level, tx, true);
								int ran = random.nextInt(1000);
								if(ran==0){
									int[] items = gift.itemId;
									for(int id : items){
										p.bag.addGameItem(ObjectAccessor.createGameItem(id), 1, tx, true);
									}
								}
								tx.commit();
								p.pool.setLong("WEATHEREXPTIME", System.currentTimeMillis());
							}
						}
					}
				}
			}
		}
	}

}

class WeatherGift{
	
	public int mapId;
	public int weatherType;
	public int exp;
	public int expHZ;
	public int[] itemId;
	public String periods;
	
	public WeatherGift(int mapId, int weatherType, int exp, int expHZ, int[] itemId, String periods){
		this.mapId = mapId;
		this.weatherType = weatherType;
		this.exp = exp;
		this.expHZ = expHZ;
		this.itemId = itemId;
		this.periods = periods;
	}
	
	public int[] getPerInt(){
		String[] pers = periods.split(",");
		int[] persInt = new int[pers.length];
		for(int i=0;i<pers.length;i++){
			persInt[i] = Integer.parseInt(pers[i]);
		}
		return persInt;
	}
	
	public boolean in(int time){
		int[] persInt = getPerInt();
		for(int i=0;i<persInt.length;i+=2){
			if(persInt[i]<=time && persInt[i+1]>time){
				return true;
			}
		}
		return false;
	}
	
}
