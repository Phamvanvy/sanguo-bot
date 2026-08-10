package peony.game.weather;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.GameObject;
import peony.game.NoInstanceVMapManager;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.VMap;
import peony.game.buff.BuffUtil;
import peony.net.Packet;
import peony.service.Service;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;
import peony.util.TimeUtil;

import com.pip.sanguo.data.map.Period;

public class WeatherService implements Service, ServiceEventListener {

	public Map<Integer, Weather> weathers = new HashMap<Integer, Weather>(); // 当前的天气
	
	public static final long ONEDAY = 24 * 3600 * 1000L;

	public static final Weather LIGHTRAIN = new Weather(Weather.TYPE_RAIN, 3,
			50, 3, 3, 0xFFFFFF);
	public static final Weather MIDDLERAIN = new Weather(Weather.TYPE_RAIN, 4,
			70, 8, 3, 0xFFFFFF);
	public static final Weather HEAVYRAIN = new Weather(Weather.TYPE_RAIN, 5,
			110, 15, 3, 0xFFFFFF);

	public static final Weather LIGHTSNOW = new Weather(Weather.TYPE_SNOW, 3,
			50, 3, 3, 0xFFFFFF);
	public static final Weather MIDDLESNOW = new Weather(Weather.TYPE_SNOW, 4,
			70, 8, 3, 0xFFFFFF);
	public static final Weather HEAVYSNOW = new Weather(Weather.TYPE_SNOW, 5,
			110, 15, 3, 0xFFFFFF);
	
	public static final Weather LIGHTSANDSTORM = new Weather(Weather.TYPE_RAIN, 40,
			50, 5, 50, 0xE8C888);
	public static final Weather MIDDLESANDSTORM = new Weather(Weather.TYPE_RAIN, 4,
			70, 8, 3, 0xE8C888);
	public static final Weather HEAVYSANDSTORM = new Weather(Weather.TYPE_RAIN, 5,
			110, 15, 3, 0xE8C888);

	public static final Weather[] WEATHERS = { LIGHTRAIN, MIDDLERAIN,
			HEAVYRAIN, LIGHTSNOW, MIDDLESNOW, HEAVYSNOW, LIGHTSANDSTORM, MIDDLESANDSTORM, HEAVYSANDSTORM};

	// 小雨 type=0, size=3, count=50, speed=3, wind=3, color=0xFFFFFF(白色)
	// 中雨 type=0, size=4, count=70, speed=8, wind=3, color=0xFFFFFF(白色)
	// 大雨 type=0, size=5, count=110, speed=15, wind=3, color=0xFFFFFF(白色)
	//
	// 小雪 type=1, size=3, count=50, speed=3, wind=3, color=0xFFFFFF(白色)
	// 中雪 type=1, size=4, count=70, speed=8, wind=3, color=0xFFFFFF(白色)
	// 大雪 type=1, size=5, count=110, speed=15, wind=3, color=0xFFFFFF(白色)

	public void shutdown() {

	}

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("weather.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
		Server.server.getEventManager().registerListener(this);
	}

	public int[] getEventTypes() {
		return new int[] { ServiceEvent.EVENT_MAP_PLAYER_LOADED, // 角色在进入地图以后发送load信息
		};
	}

	public void handleEvent(ServiceEvent event) {
		switch (event.type) {
		case ServiceEvent.EVENT_MAP_PLAYER_LOADED:
			playerLoaded((Player) event.param2);
			break;
		}
	}

	public void changeWeatherAndAddBuff(int mapId, int type) {
		if (type == -1) {
			changeWeatherAndAddBuff(mapId, null);
		} else {
			changeWeatherAndAddBuff(mapId, WEATHERS[type]);
		}
	}
	

	public void changeWeatherAndAddBuff(int mapId, Weather weather) {
		weathers.put(mapId, weather);
		NoInstanceVMapManager manager = (NoInstanceVMapManager) Server.server
				.getWorld().getVMapManager(mapId);
		if (manager != null) {
			VMap[] maps = manager.getVMaps(mapId);
			for (VMap map : maps) {
				notifyWeather(map, weather);
				if(weather != null){
					for(GameObject o: map.instanceid2objects.values()){
						if(o.type == GameObject.TYPE_PLAYER){
							Player p = (Player)o;
							p.buffs.addBuff(BuffUtil.createBuff(220, 1, p, p, 0));
						}
					}
				}
			}
		}
	}

	public void notifyWeather(VMap map, Weather weather) {
		for (GameObject o : map.instanceid2objects.values()) {
			if (o.type == GameObject.TYPE_PLAYER) {
				Player p = (Player) o;
				notifyWeather(p, weather);
			}
		}
	}

	public void notifyWeather(Player p, Weather weather) {
		if (weather != null) {
			Packet pt = new Packet(OpCode.WEATHER_SERVER);
			pt.putInt(p.getVMap().getId());
			pt.put(1);
			pt.put(weather.type);
			pt.put(weather.size);
			pt.put(weather.count);
			pt.put(weather.speed);
			pt.put(weather.wind);
			pt.putInt(weather.color);
			p.send(pt);
		} else {
			Packet pt = new Packet(OpCode.WEATHER_SERVER);
			pt.putInt(p.getVMap().getId());
			pt.put(0);
			p.send(pt);
		}
	}

	/**
	 * 天气变化 地图ID int 开关 byte (0 关 1 开) 类型 byte (0 雨 1 雪)如果是开的状态下才存在此字段 大小 byte
	 * 如果是开的状态下才存在此字段 数量 byte 如果是开的状态下才存在此字段 速度 byte 如果是开的状态下才存在此字段 风力 byte
	 * 如果是开的状态下才存在此字段 颜色 int 如果是开的状态下才存在此字段
	 */
	public static final short WEATHER_SERVER = 614;

	protected void playerLoaded(Player p) {
		Weather weather = weathers.get(p.getVMap().getId());
		if (weather != null) {
			notifyWeather(p, weather);
			p.buffs.addBuff(BuffUtil.createBuff(220, 1, p, p, 0));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void parse(Document doc){
		// 转化成Timer，如果当前时间在某个范围内，那么就用changeWeather方法。并且无论在不在都转化成Timer
		Element root = doc.getRootElement();
		List<WeatherDef> ws = new ArrayList<WeatherDef>();
		if (root != null) {
			List wea = root.elements("weather");
			for (int i = 0; i < wea.size(); i++) {
				int mapId = Integer.parseInt(((Element) wea.get(i))
						.attributeValue("mapId"));
				int type = Integer.parseInt(((Element) wea.get(i))
						.attributeValue("type"));
				WeatherDef def = new WeatherDef(mapId,type);
				ws.add(def);
				List pe = ((Element) wea.get(i)).elements("period");
				for (int j = 0; j < pe.size(); j++) {
					int startTime = Integer.parseInt(((Element) pe.get(j))
							.attributeValue("startTime"));
					int endTime = Integer.parseInt(((Element) pe.get(j))
							.attributeValue("endTime"));
					Period period = new Period(startTime,0,endTime,0);
					def.addPeriod(period);
				}
			}
			for(WeatherDef def:ws){
				schedule(def);
			}
		}
	}
	
	public void schedule(WeatherDef def){
		Calendar cal = Calendar.getInstance();
		for(Period p:def.periods){
			if(p.in(cal)){ //如果当前时间在定义的区间内,那么肯定是大于起始时间,小于终止时间
				changeWeatherAndAddBuff(def.mapId,def.type);
			}
			Server.server.scheduExec.scheduleAtFixedRate(new ChangeWeatherTask(def.mapId,def.type,true),TimeUtil.getScheduleTimeMills(new Date(),p.startHour,p.startMinute),ONEDAY,TimeUnit.MILLISECONDS);
			Server.server.scheduExec.scheduleAtFixedRate(new ChangeWeatherTask(def.mapId,def.type,false),TimeUtil.getScheduleTimeMills(new Date(),p.endHour,p.endMinute),ONEDAY,TimeUnit.MILLISECONDS);
		}
	}
	
	class ChangeWeatherTask implements Runnable{
		
		boolean on;
		int mapId;
		int type;
		
		public ChangeWeatherTask(int mapId,int type,boolean on){
			this.mapId = mapId;
			this.type = type;
			this.on = on;
		}
		
		public void run(){
			if(on)  //因为需要遍历map中所有的GameObject，所以需要在主循环中操作
				Server.server.syncRunner.add(new Runnable(){
					public void run(){
						changeWeatherAndAddBuff(mapId,type);
					}
				});
			else
				Server.server.syncRunner.add(new Runnable(){
					public void run(){
						changeWeatherAndAddBuff(mapId,-1);
					}
				});
		}
	}
	
}


class WeatherDef{
	public int mapId;
	public int type;
	public List<Period> periods = new ArrayList<Period>();
	
	public WeatherDef(int mapId,int type){
		this.mapId = mapId;
		this.type = type;
	}
	
	public void addPeriod(Period period){
		periods.add(period);
	}
}
