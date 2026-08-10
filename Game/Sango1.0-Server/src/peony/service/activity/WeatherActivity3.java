package peony.service.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import peony.game.GameObjectRef;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.VMap;
import peony.service.ServiceEvent;
import peony.service.ServiceEventListener;

public class WeatherActivity3 implements IActivityImpl, ServiceEventListener , Runnable{

	protected Activity activity;
	protected HashMap<Integer, List<GameObjectRef>> players = new HashMap<Integer, List<GameObjectRef>>();
	protected List<Integer> mapIds = new ArrayList<Integer>();
	protected int ADDCREDIT = 0;
	protected int TIMEDIS = 15*1000;
	protected HashMap<Integer, Long> lastGetTime = new HashMap<Integer, Long>();
	
	public void clear() {
		
		
	}
	
	public WeatherActivity3(Activity owner){
		this.activity = owner;
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
		String data = activity.configData;
		if(data!=null){
			String[] str0 = data.split(";");
			for(String str1 : str0){
				String[] str2 = str1.split(":");
				if(str2[0].equals("mapId")){
					String[] str3 = str2[1].split(",");
					for(String str4 : str3){
						mapIds.add(Integer.parseInt(str4));
					}	
				} else if(str2[0].equals("credit")){
					ADDCREDIT = Integer.parseInt(str2[1]);
				} else if(str2[0].equals("timedis")){
					TIMEDIS = Integer.parseInt(str2[1]);
				}
			}
		}
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
			for(Integer mapId : mapIds){
				if(map.getId() == mapId){
					List<GameObjectRef> list = players.get(map.getId());
					if(list!=null){
						list.add(p.ref());
					} else {
						list = new ArrayList<GameObjectRef>();
						list.add(p.ref());
						players.put(map.getId(), list);
					}
					lastGetTime.put(p.id, System.currentTimeMillis());
				}
			}
		}
	}
	
	protected void processRemoveMap(VMap map, Player p){
		if(p!=null){
			for(Integer mapId : mapIds){
				if(map.getId() == mapId){
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
		}
	}

	@SuppressWarnings("static-access")
	public void run() {
		while(this.activity.active && this.activity.getSchedule().in()){
			try {
				Thread.currentThread().sleep(10000);
			} catch (InterruptedException e) {
			}
			try{
				if(mapIds!=null){
					for(Integer mapId : mapIds){
						if(players!=null && players.size()>0){
							List<GameObjectRef> refs = players.get(mapId);
							if(refs!=null && refs.size()>0){
								for(GameObjectRef ref : refs){
									if(ref==null)
										continue;
									Player p = ObjectAccessor.getPlayer(ref.id);
									if(p!=null && p.isAlive() && !p.isInStep){
										long lastGetWeatherExpTime = lastGetTime.get(p.id);
										if((System.currentTimeMillis()-lastGetWeatherExpTime)>=TIMEDIS){
											PlayerTransaction tx = p.newTransaction("WEATHEREXP3");
											p.addExp(p.level*20, tx, true);
											p.addCredit(ADDCREDIT, tx, true);
											tx.commit();
											lastGetTime.put(p.id, System.currentTimeMillis());
										}
									}
								}
							}
						}
					}
				}
			}catch(Exception e){
				
			}
		}
	}
}
