package com.pip.itimes.server.world.game;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.world.PositionSprite;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.Team;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.game.InstanceForbid.InstanceIdDate;



/**
 * @author Jeffrey
 * @version 1.0
 */
public class NormalInstanceModel implements InstanceModel{
	protected static final Logger log = Logger.getLogger(NormalInstanceModel.class);
    private WorldService worldService;
    private InstanceService instanceService;

    private Map<Integer,NormalInstance> id2instances = new HashMap<Integer,NormalInstance>();
    private Map<Integer,HashSet<NormalInstance>> playerid2instances = new HashMap<Integer,HashSet<NormalInstance>>();
    private Map playerid2maps = new HashMap();

    public NormalInstanceModel(WorldService worldService,InstanceService instanceService){
        this.worldService = worldService;
        this.instanceService = instanceService;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH,1);
        cal.set(Calendar.HOUR_OF_DAY,4);
        new Timer().scheduleAtFixedRate(new TimeOutTask(),cal.getTime(),24*3600*1000L);
    }
    
    public synchronized Instance tryGotoInstance(int instanceId,
			WorldPlayer player, int battleID) throws InstanceException {

		NormalInstance instance = getInstance(player, instanceId);
		if (instance != null && instance.isTimeOut()) {
			removeInstance(instance);
			instance = null;
		}
		if (instance != null) {
			// WorldPlayer[] players = new WorldPlayer[] {player};
			// Team team = player.getTeam();
			// if (team != null) {
			// players = team.getPlayers();
			// }
			if (player.getLevel() < instance.getMinLevel())
				throw new InstanceException("进入此副本需要" + instance.getMinLevel()
						+ "级");
			Team team = player.getTeam();
			if (team != null && team.getLeader() == player) {
				IPlayerData[] pds = team.getMembers(WorldPlayer.TEAM_FOLLOW);
				ArrayList<WorldPlayer> lstPlayer = new ArrayList<WorldPlayer>();
				for(int i=0; i<pds.length; i++){
					if(pds[i] instanceof WorldPlayer){
						lstPlayer.add((WorldPlayer)pds[i]);
					}
				}
				WorldPlayer[] players = new WorldPlayer[lstPlayer.size()];
				lstPlayer.toArray(players);
				for (int i = 0; i < players.length; i++) {
					if (players[i].getLevel() < instance.getMinLevel()) {
						throw new InstanceException("进入此副本需要"
								+ instance.getMinLevel() + "级");
					}
				}
				// 副本次数限制判断
				checkLastInstanceState(instanceId, players, instance);
				// 副本限制计数器累计
				addGotoInsTimer(instanceId, players, instance);
				instance.preAdd(players);
			} else {
				checkLastInstanceState(instanceId, new WorldPlayer[] { player }, instance);
				addGotoInsTimer(instanceId, new WorldPlayer[] { player }, instance);
				instance.preAdd(new WorldPlayer[] { player });
			}
			// if (!ret) {
			// throw new InstanceException("不能进入副本");
			// }
		} else {
			InstanceDefinition idf = instanceService
					.getInstanceDefinition(instanceId);
			if (player.getLevel() < idf.getMinLevel()){
				Utils.log(log, player.getId(), 81,
                        "GotoInstance[" + idf.getId() + "] Error level = " + player.getLevel() + " instancelevel = " + idf.getMinLevel());
				throw new InstanceException("进入此副本需要" + idf.getMinLevel() + "级");
			}
			Team team = player.getTeam();
			if (team != null && team.getLeader() == player) {
				IPlayerData[] players = team.getMembers(WorldPlayer.TEAM_FOLLOW);
				for (int i = 0; i < players.length; i++) {
					if (players[i].getLevel() < idf.getMinLevel()) {
						Utils.log(log, player.getId(), 81,
		                        "GotoInstance[" + idf.getId() + "] Error level = " + player.getLevel() + " instancelevel = " + idf.getMinLevel());
						throw new InstanceException("进入此副本需要"
								+ idf.getMinLevel() + "级");
					}
				}
			}
			instance = createInstance(instanceService
					.getInstanceDefinition(instanceId));
			if (team != null && team.getLeader() == player) {
				IPlayerData[] ps = team.getMembers(WorldPlayer.TEAM_FOLLOW);
				ArrayList<WorldPlayer> lstPlayer = new ArrayList<WorldPlayer>();
				for(int i=0; i<ps.length; i++){
					if(ps[i] instanceof WorldPlayer){
						lstPlayer.add((WorldPlayer)ps[i]);
					}
				}
				WorldPlayer[] players = new WorldPlayer[lstPlayer.size()];
				lstPlayer.toArray(players);
				// 副本次数限制判断
				checkLastInstanceState(instanceId, players, instance);
				// 副本限制计数器累计
				addGotoInsTimer(instanceId, players, instance);
				instance.preAdd(players);
			} else {
				// removeFromOldInstance(player.getId(),instanceId);
				checkLastInstanceState(instanceId, new WorldPlayer[] { player }, instance);
				addGotoInsTimer(instanceId, new WorldPlayer[] { player }, instance);
				instance.preAdd(new WorldPlayer[] { player });
			}
			/*if(team != null){
				WorldPlayer[] players = team.getPlayers();
					
				instance.setPlayer(players);
			}*/
			// WorldPlayer[] players = new WorldPlayer[] {player};
			// Team team = player.getTeam();
			// if (team != null) {
			// players = team.getPlayers();
			// }
			// List l = new ArrayList(players.length);
			// if(players.length>0){
			// for(int i=0;i<players.length;i++){
			// if(!players[i].inInstance(instanceId)){
			// l.add(players[i]);
			// }
			// }
			// players = new WorldPlayer[l.size()];
			// l.toArray(players);
			// }

			// instance.preAdd(players);
			// if(!ret)
			// throw new InstanceException("不能进入副本");
		}
		return instance;
	}
    
    public void checkLastInstanceState(int instanceId, IPlayerData[] players, NormalInstance instance) throws InstanceException {
    	for(int i = 0; i< players.length;i++){
    		// 判断下副本每小时list
        	if(Server.player_InstanceForbid.containsKey(players[i].getPlayer().getId())){		// 曾进入过某个副本
            	InstanceForbid instanceForb = Server.player_InstanceForbid.get(players[i].getPlayer().getId());
            	for(int j = instanceForb.instanceTime.size() - 1; j >= 0; j--){
        			InstanceIdDate iid = (InstanceIdDate)instanceForb.instanceTime.get(j);
        			InstanceDefinition idf = instanceService.getInstanceDefinition(iid.instanceid);
        			if(System.currentTimeMillis() - iid.date.getTime() > idf.getRefreshSecond() * 1000L){
        				instanceForb.instanceTime.remove(j);
        			}
        		}
            }
    		NormalInstance lastInstanceEach = null;
    		if(players.length > 1){
    			lastInstanceEach = getInstance(players[i], instanceId);
    		}else{
    			lastInstanceEach = getInstanceMe(players[i], instanceId);
    		}
    		if(lastInstanceEach == null){// 未进过此次副本
    			testCanGotoInstance(instanceId, players[i], instance.getId());
    		} else if(lastInstanceEach != null){// 进过此副本
    			//if(lastInstanceEach.getId() != instance.getId()){
    				testCanGotoInstance(instanceId, players[i], instance.getId());
    			//}
    		}
    	}
    }
    
    public void testCanGotoInstance(int instanceId, IPlayerData player, int id) throws InstanceException{
    	InstanceDefinition idf = instanceService.getInstanceDefinition(instanceId);
    	if(idf != null){
    		int maxTime = idf.getMaxTime();		// 副本最大进入次数
    		if(Server.player_InstanceForbid.containsKey(player.getPlayer().getId())){		// 曾进入过某个副本
    			InstanceForbid instanceForb = Server.player_InstanceForbid.get(player.getPlayer().getId());		// 从Map中取出每个player对应的类的对象
    			
    			//为旧的副本 则不计算
    			if(!instanceForb.testGotoInsNew(instanceId, id)){
    				return;
    			}
    			
    			// 每日上限
    			boolean countRet = instanceForb.testGotoInsCount(instanceId , maxTime);
    			// 每时上限
//    			int hourlimitTime = 3;
//    			boolean timeRet = instanceForb.testGotoInsHourTime2(instanceId, hourlimitTime);
    			boolean timeRet = instanceForb.testGotoInsHourTime(instanceId, new Date(System.currentTimeMillis()));
    			
    			if(!timeRet){
    				Utils.log(log, player.getId(), 81,
    						"GotoInstance[" + idf.getId() + "] Error countRet");
    				throw new InstanceException("您或您的队友进出副本的次数过份频繁、请1小时以后再来挑战副本吧!");
    			} else if(!countRet){
    				Utils.log(log, player.getId(), 81,
    						"GotoInstance[" + idf.getId() + "] Error timeRet");
    				throw new InstanceException("您或您的队友今日大显神威，把副本BOSS们教训了10遍，请给他们留点喘息之机，明天再来吧!");
    			}
    		}
    	}
    }
    
    public void addGotoInsTimer(int instanceId, IPlayerData[] players, Instance instance){
    	for(int i = 0; i < players.length ; i++){
    		NormalInstance lastInstanceEach = getInstance(players[i], instanceId);
			if(lastInstanceEach == null){
    			if(Server.player_InstanceForbid.containsKey(players[i].getPlayer().getId())){		// 进入过某个副本
    				InstanceForbid instanceForb = Server.player_InstanceForbid.get(players[i].getPlayer().getId());		// 从Map中取出每个player对应的类的对象
    				if(instanceForb.testGotoInsNew(instanceId, 0)){
    					instanceForb.addGotoInsCount(instanceId);
    					instanceForb.addGotoInsHourTime(instanceId, new Date(System.currentTimeMillis()), instance.getId());
    				}
    			} else {
    				InstanceForbid instanceForb = new InstanceForbid();
    				instanceForb.addGotoInsCount(instanceId);
    				instanceForb.addGotoInsHourTime(instanceId, new Date(System.currentTimeMillis()), instance.getId());
    				Server.player_InstanceForbid.put(players[i].getPlayer().getId(), instanceForb);
    			}
			} else {
				//if(lastInstanceEach.getId() != instance.getId()){
        			if(Server.player_InstanceForbid.containsKey(players[i].getPlayer().getId())){		// 进入过某个副本
        				InstanceForbid instanceForb = Server.player_InstanceForbid.get(players[i].getPlayer().getId());		// 从Map中取出每个player对应的类的对象
        				if(instanceForb.testGotoInsNew(instanceId, instance.getId())){
	        				instanceForb.addGotoInsCount(instanceId);
	        				instanceForb.addGotoInsHourTime(instanceId, new Date(System.currentTimeMillis()), instance.getId());
        				}
        			} else {
        				InstanceForbid instanceForb = new InstanceForbid();
        				instanceForb.addGotoInsCount(instanceId);
        				instanceForb.addGotoInsHourTime(instanceId, new Date(System.currentTimeMillis()), instance.getId());
        				Server.player_InstanceForbid.put(players[i].getPlayer().getId(), instanceForb);
        			}
				//}
			}
    	}
    }

    public NormalInstance createInstance(InstanceDefinition idf){
        GameMap entrance = worldService.getNoInstanceMap(idf.getEntrance());
        int id = instanceService.getNewInstanceId();
        NormalInstance instance = new NormalInstance(id,idf,instanceService);
        instance.setEntrance(entrance);
        short[] maps = idf.getMaps();
        for(int i=0;i<maps.length;i++){
            Scene scene = worldService.getInstanceScene(maps[i]);
            GameMap map = new GameMap(worldService,scene,(short)0,(short)0);
            map.setCanPk(false);
            instance.addMap(map);
            map.setInstance(instance);

        }
        addInstance(instance);
        worldService.instanceCreated(instance);
        return instance;
    }

    public void addInstance(NormalInstance instance) {
        id2instances.put(instance.getId(), instance);
    }


    public GameMap getGameMap(WorldPlayer player,short mapId){
        Map m = (Map)playerid2maps.get(new Integer(player.getId()));
        if(m==null)
            return null;
        return (GameMap)m.get(new Short(mapId));
    }

    public synchronized GameMap getLoginMap(WorldPlayer player,short mapId){
        GameMap map = getGameMap(player,mapId);
        if(map!=null){
            NormalInstance instance = (NormalInstance)map.getInstance();
            if(instance==null){
                return map;
            }else{
            	if(player.getHp() <= 0){
            		return instance.getEntrance();
            	}
                if(instance.isTimeOut()){
                    removeInstance(instance);
                    return instance.getEntrance();
                }else{
                    if(map.canAdd(player)){
                        return map;
                    }else{
                        return instance.getEntrance();
                    }
                }
            }
        }else{
            InstanceDefinition idf = instanceService.getInstanceDefintionByMap(mapId);
            return worldService.getNoInstanceMap(idf.getEntrance());
        }
    }

    public NormalInstance getInstance(IPlayerData player,int instanceId){
        Team team = player.getTeam();
        if(team!=null){
            PositionSprite[] players = team.getPlayers();
            for (int i = 0; i < players.length; i++) {
                if (players[i].getId() != player.getId() && players[i].getMap() != null) {
                    Instance instance = players[i].getMap().getInstance();
                    if (instance != null && instance.getInstanceId() == instanceId) {
                        return (NormalInstance) instance;
                    }
                }
            }
        }
        HashSet set = playerid2instances.get(player.getId());
        if(set!=null){
            Iterator<NormalInstance> ite = set.iterator();
            while(ite.hasNext()){
                NormalInstance instance = ite.next();
                if(instance.getInstanceId()==instanceId)
                    return instance;
            }
        }
        return null;
    }
    
    public NormalInstance getInstanceMe(IPlayerData player,int instanceId){
    	Team team = player.getTeam();
        if(team!=null){
            PositionSprite[] players = team.getPlayers();
            for (int i = 0; i < players.length; i++) {
                if (players[i].getId() == player.getId() && players[i].getMap() != null) {
                    Instance instance = players[i].getMap().getInstance();
                    if (instance != null && instance.getInstanceId() == instanceId) {
                        return (NormalInstance) instance;
                    }
                }
            }
        }
        HashSet set = playerid2instances.get(player.getId());
        if(set!=null){
            Iterator<NormalInstance> ite = set.iterator();
            while(ite.hasNext()){
                NormalInstance instance = ite.next();
                if(instance.getInstanceId()==instanceId)
                    return instance;
            }
        }
        return null;
    }

    protected void removeInstance(Instance instance){
        id2instances.remove(instance.getId());
        int[] players = instance.getPlayers();
        for(int i=0;i<players.length;i++){
            removeFromOldInstance(players[i],instance);
            if(Server.player_InstanceForbid.containsKey(players[i])){
            	InstanceForbid instanceForb = Server.player_InstanceForbid.get(players[i]);
            	//副本时间1小时，超时后不再删除存储进入副本的时间，每小时3次判断要用
            	if(instanceForb != null){
            		instanceForb.removeInstanceHourTime(instance.getInstanceId());
            	}
            }
        }
        worldService.instanceRemoved(instance);
    }

    protected void removeFromOldInstance(int playerId, Instance instance) {
         HashSet m =  playerid2instances.get(new Integer(playerId));
        if (m != null) {
            Iterator ite = m.iterator();
            while (ite.hasNext()) {
                Instance ins = (Instance) ite.next();
                if (ins != instance &&
                    ins.getInstanceId() == instance.getInstanceId()) {
                    ite.remove();
                    ins.kickPlayer(playerId);
                    GameMap[] maps = ins.getMaps();
                    for (int i = 0; i < maps.length; i++) {
                        Map mm = (Map) playerid2maps.get(new Integer(playerId));
                        if (mm != null) {
                            mm.remove(new Short(maps[i].getMapId()));
                        }
                    }
                }
            }
        }
    }

    protected void removeFromOldInstance(int playerId, int instanceId) {
         HashSet m =  playerid2instances.get(new Integer(playerId));
        if (m != null) {
            Iterator ite = m.iterator();
            while (ite.hasNext()) {
                Instance ins = (Instance) ite.next();
                if (instanceId == ins.getInstanceId()) {
                    ite.remove();
                    ins.kickPlayer(playerId);
                    GameMap[] maps = ins.getMaps();
                    for (int i = 0; i < maps.length; i++) {
                        Map mm = (Map) playerid2maps.get(new Integer(playerId));
                        if (mm != null) {
                            mm.remove(new Short(maps[i].getMapId()));
                        }
                    }
                }
            }
        }
    }

    public void playerAddedToInstance(IPlayerData player, Instance instance) {
        removeFromOldInstance(player.getId(), instance);
        addToInstances(player.getId(), (NormalInstance)instance);
        GameMap[] maps = instance.getMaps();
        for (int i = 0; i < maps.length; i++) {
            addToMap(player.getId(), maps[i]);
        }
    }

    protected void addToInstances(int playerId, NormalInstance instance) {

        HashSet set =  playerid2instances.get(playerId);
        if (set == null) {
            set = new HashSet();
            playerid2instances.put(playerId, set);
        }
        set.add(instance);
    }

    protected void addToMap(int playerId, GameMap gameMap) {
        Map m = (Map) playerid2maps.get(new Integer(playerId));
        if (m == null) {
            m = new HashMap();
            playerid2maps.put(new Integer(playerId), m);
        }
        m.put(new Short(gameMap.getMapId()), gameMap);
    }

    class TimeOutTask extends TimerTask{
        public void run(){
            synchronized(NormalInstanceModel.this){
                NormalInstance[] instances = new NormalInstance[id2instances.size()];
                id2instances.values().toArray(instances);
                for(int i=0;i<instances.length;i++){
                    if(instances[i]!=null&&instances[i].isTimeOut()){
                        removeInstance(instances[i]);
                    }
                }
            }
        }
    }
}