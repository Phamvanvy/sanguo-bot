package com.pip.itimes.server.world.game;

import java.util.HashMap;
import java.util.Map;

import com.pip.itimes.server.bean.House;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.dao.HouseDao;
import java.util.List;
import com.pip.itimes.server.stage.HouseData;
import com.pip.itimes.server.world.PlayerService;
import java.util.Date;
import com.pip.itimes.server.dao.*;
import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.stage.Houses;
import com.pip.itimes.server.stage.HouseTemplate;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.MateService;
import com.pip.itimes.server.world.Team;

import org.apache.log4j.Logger;
import com.pip.itimes.server.stage.HousePart;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;


public class HouseInstanceModel implements InstanceModel {

    private static final Logger log = Logger.getLogger(HouseInstanceModel.class);

    private Map<Integer,HouseInstance> id2instance = new HashMap<Integer,HouseInstance>();
    private Map<Integer,HouseInstance> player2instance = new HashMap<Integer,HouseInstance>(); //用户需要去到的房间
    private Map<Integer,HouseInstance> player2self = new HashMap<Integer,HouseInstance>();  //用户自己的房间
    private Map<Integer,HouseData> player2house = new ConcurrentHashMap<Integer,HouseData>();
    private WorldService worldService;
    private InstanceService instanceService;
    private PlayerService playerService;
    private MateService mateService;
    private HouseDao dao;
    private Set<Integer> usedItem = new HashSet<Integer>();

    public HouseInstanceModel(){
        dao = new HouseDao();
    }

    public GameMap getGameMap(WorldPlayer player, short mapId) {
        HouseInstance instance = player2instance.get(player.getId());
        if(instance!=null){
            return instance.getMap(mapId);
        }
        return null;
    }

    public void setWorldService(WorldService worldService){
        this.worldService = worldService;

    }

    public void setInstanceService(InstanceService instanceService){
        this.instanceService = instanceService;
    }

    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

    public void setMateService(MateService mateService){
        this.mateService = mateService;
    }

    public HouseInstance getInstance(IPlayerData player, int instanceId) {
        return null;
    }

    public HouseData getHouseByPlayerId(int playerId) throws Exception{
    	if (!player2house.containsKey(playerId)){
    		loadOneHouse(playerId);
    	}
        return player2house.get(playerId);
    }

    public HouseInstance getselfHouseByPlayerId(int playerid) throws Exception{
    	if (!player2self.containsKey(playerid)){
    		House housetmp = dao.getOneHouses(playerid);
    		if (housetmp!=null){
    			HouseData h = new HouseData(housetmp);
//        		player2house.put(h.getPlayerId(),h);
        		HouseTemplate ht = Houses.getHouseTemplate(h.getLevel(),h.getStyle());
                InstanceDefinition idf = instanceService.getInstanceDefinition(ht.getInstanceId());
                HouseInstance hi = createInstance(idf,h.getPlayerId());
                player2self.put(h.getPlayerId(),hi);
                return hi;
    		}else{
    			return null;
    		}
    	}else{
    		return player2self.get(playerid);
    	}
    }
    public void loadOneHouse(int playerid) throws Exception{
    	if (!player2house.containsKey(playerid)){
    		House housetmp = dao.getOneHouses(playerid);
    		if (housetmp!=null){
    			HouseData h = new HouseData(housetmp);
        		player2house.put(h.getPlayerId(),h);
        		HouseTemplate ht = Houses.getHouseTemplate(h.getLevel(),h.getStyle());
                InstanceDefinition idf = instanceService.getInstanceDefinition(ht.getInstanceId());
                HouseInstance hi = createInstance(idf,h.getPlayerId());
                player2self.put(h.getPlayerId(),hi);
    		}
    	}
    }
    public void loadAllHouse() throws Exception{
        List l = dao.getAllHouses();
        for(int i=0;i<l.size();i++){
            HouseData h = new HouseData((House)l.get(i));
            player2house.put(h.getPlayerId(),h);
            HouseTemplate ht = Houses.getHouseTemplate(h.getLevel(),h.getStyle());
            InstanceDefinition idf = instanceService.getInstanceDefinition(ht.getInstanceId());
            HouseInstance hi = createInstance(idf,h.getPlayerId());
            player2self.put(h.getPlayerId(),hi);
        }
    }

    public synchronized HouseInstance preTry(WorldPlayer player,int destPlayerId) throws HouseException{
    	try{
    		loadOneHouse(destPlayerId);						// add  load house  
    	}catch(Exception e)
    	{
    		
    	}
    	HouseInstance hi = player2self.get(destPlayerId);
//    	HouseInstance hi = null;
//		try {
//			hi = getselfHouseByPlayerId(destPlayerId);
//		} catch (Exception e1) {
//		}
//        HouseInstance hi = player2self.get(destPlayerId);
        if(hi==null){
            throw new HouseException("没有发现家园或者没有权限进入");
        }
        HouseData hd = player2house.get(destPlayerId);
//        HouseData hd = null;
//		try {
//			hd = getHouseByPlayerId(destPlayerId);
//		} catch (Exception e) {
//		}
//        HouseData hd = player2house.get(destPlayerId);
        if(hd==null){
            throw new HouseException("没有发现家园或者没有权限进入");
        }
        player2instance.put(player.getId(),hi);
        return hi;
    }

    public void UsedItem(WorldPlayer player){
        usedItem.add(player.getId());
    }

    public synchronized void createHouse(WorldPlayer player, int level, int style,short areaId,int cost) throws HouseException {
//        HouseInstance hi = player2self.get(player.getId());
    	HouseInstance hi = null;
		try {
			hi = getselfHouseByPlayerId(player.getId());
		} catch (Exception e1) {
		}
        if (hi != null) {
            int[] ids = hi.getActives();
            for (int j = 0; j < ids.length; j++) {
                WorldPlayer p = playerService.getWorldPlayer(ids[j]);
                if (p != null) {
                    GameMap map = player.getMap();
                    if (map != null && map.getInstance() == hi) {
                        player.getMap().removePlayer(player, true);
                        player.setMap(null);
                    }
                }
            }
            worldService.instanceRemoved(hi);
        }
        HouseTemplate ht = Houses.getHouseTemplate(level,style);
        if(ht==null)
            throw new HouseException("未找到房屋模板");
        InstanceDefinition idf = instanceService.getInstanceDefinition(ht.getInstanceId());
        if(idf==null)
            throw new HouseException("未找到房屋模板指定副本");
//        HouseData hd = player2house.get(player.getId());
        HouseData hd = null;
		try {
			hd = getHouseByPlayerId(player.getId());
		} catch (Exception e) {
		}
        if(hd==null){
            House h = new House();
            h.setPlayerId(player.getId());
            h.setPlayerName(player.getPlayerName());
            h.setLevel(level);
            h.setStyle(style);
            h.setGridSize(ht.getGridSize());
            h.setAreaId(areaId);
            h.setCreateTime(new Date());
            h.setItems(new byte[0]);
            h.setParts(new byte[0]);
            h.setWaiterId(0);
            h.setTitle(player.getPlayerName()+"的家园");
            h.setUsediMoney(cost);
            h.setVisitedTimes(0);
            h.setLeaveMessageTimes(0);
            h.setAutoBuyWaiter(0);
            h.setAddGridSize(0);		// 房屋扩展格数，默认为0
            try {
                dao.saveHouse(h);
            } catch (DataAccessException ex) {
                throw new HouseException("新建房屋错误");
            }
            try {
                hd = new HouseData(h);
            } catch (Exception ex2) {
            }
        }else{
            if(hd.getCurrentGridSize()>(ht.getGridSize()+ hd.getAddGridSize())){
                throw new HouseException("你现在仓库中的物品数量多于将要购买房屋能够存储的物品数量，请移除部分物品再次尝试！");
            }
            hd.setLevel(level);
            hd.setStyle(style);
            hd.setGirdSize(ht.getGridSize());
            hd.clearVisibleParts();
            hd.setAreaId(areaId);
            hd.incUsediMoney(cost);
            try {
                dao.saveHouse(hd.getHouse());
            } catch (DataAccessException ex1) {
                throw new HouseException("新建房屋错误");
            }
        }
        HouseInstance newHi = createInstance(idf,player.getId());
        player2self.put(player.getId(),newHi);
        player2house.put(player.getId(),hd);
    }

    public void saveHouse(HouseData hd){
        hd.reset();
        try {
            dao.saveHouse(hd.getHouse());
        } catch (DataAccessException ex) {
        }
    }

    public synchronized void changeStyle(WorldPlayer player,int style,int cost) throws HouseException{
//        HouseInstance hi = player2self.get(player.getId());
        HouseInstance hi = null;
		try {
			hi = getselfHouseByPlayerId(player.getId());
		} catch (Exception e1) {
		}
        if (hi != null) {
            int[] ids = hi.getActives();
            for (int j = 0; j < ids.length; j++) {
                WorldPlayer p = playerService.getWorldPlayer(ids[j]);
                if (p != null) {
                    GameMap map = player.getMap();
                    if (map != null && map.getInstance() == hi) {
                        player.getMap().removePlayer(player, true);
                        player.setMap(null);
                    }
                }
            }
            worldService.instanceRemoved(hi);
        }
//        HouseData hd = player2house.get(player.getId());
        HouseData hd = null;
		try {
			hd = getHouseByPlayerId(player.getId());
		} catch (Exception e) {
			
		}
        if(hd==null)
            throw new HouseException("你还没有房产");
        HouseTemplate ht = Houses.getHouseTemplate(hd.getLevel(),style);
        if(ht==null)
            throw new HouseException("未找到房屋模板");
        InstanceDefinition idf = instanceService.getInstanceDefinition(ht.getInstanceId());
        if(idf==null)
            throw new HouseException("未找到房屋模板指定副本");
        hd.setStyle(style);
        hd.incUsediMoney(cost);
        try {
            dao.saveHouse(hd.getHouse());
        } catch (DataAccessException ex) {
            throw new HouseException("房屋修改样式错误");
        }
        HouseInstance newHi = createInstance(idf,player.getId());
        player2self.put(player.getId(),newHi);
    }

    public synchronized void addPart(WorldPlayer player,HousePart part,int cost) throws Exception{
//        HouseData hd = player2house.get(player.getId());
    	HouseData hd = getHouseByPlayerId(player.getId());
        if(hd==null)
            throw new HouseException("你还没有房产");
        hd.addVisiblePart((byte)part.getId());
        hd.incUsediMoney(cost);
        try {
            dao.saveHouse(hd.getHouse());
        } catch (DataAccessException ex) {
            throw new HouseException("购买家具失败");
        }
    }

    protected HouseInstance createInstance(InstanceDefinition idf,int playerId){
        int id = instanceService.getNewInstanceId();
        HouseInstance instance = new HouseInstance(id,idf,instanceService);
        instance.setOwnerId(playerId);
        short[] maps = idf.getMaps();
        for(int i=0;i<maps.length;i++){
            Scene scene = worldService.getInstanceScene(maps[i]);
            GameMap map = new GameMap(worldService,scene,(short)0,(short)0);
            map.setCanPk(false);
            instance.addMap(map);
            map.setInstance(instance);
        }
        worldService.instanceCreated(instance);
        return instance;
    }

    public GameMap getLoginMap(WorldPlayer player, short mapId) {
        GameMap map = getGameMap(player,mapId);
        if(map!=null){
            HouseInstance instance = (HouseInstance)map.getInstance();
            if(instance==null){
                return map;
            }else{
//                HouseData house = player2house.get(instance.getOwnerId());
            	HouseData house = null;
				try {
					house = getHouseByPlayerId(instance.getOwnerId());
				} catch (Exception e) {
				}
                if(checkRule(player,house)){
                    return map;
                }else{
                    if(player.getJumpMapId()!=0){
                        return worldService.getNoInstanceMap(player.getJumpMapId());
                    }else{
                        return worldService.getNoInstanceMap((short)1617);
                    }
                }
            }
        }else{
            if(player.getJumpMapId()!=0){
                return worldService.getNoInstanceMap(player.getJumpMapId());
            }else{
                return worldService.getNoInstanceMap((short)1617);
            }

        }
    }

    public void playerAddedToInstance(IPlayerData player, Instance instance) {
//        GameMap[] maps = instance.getMaps();
//        for (int i = 0; i < maps.length; i++) {
//            addToMap(player.getId(), maps[i]);
//        }
    }
//    protected void addToMap(int playerId, GameMap gameMap) {
//        Map m = (Map) playerid2maps.get(new Integer(playerId));
//        if (m == null) {
//            m = new HashMap();
//            playerid2maps.put(new Integer(playerId), m);
//        }
//        m.put(new Short(gameMap.getMapId()), gameMap);
//    }

    public HouseInstance tryGotoInstance(int instanceId, WorldPlayer player, int battleID) throws InstanceException {
        HouseInstance instance = player2instance.get(player.getId());
        if(instance!=null){
            GameMap map = player.getMap();
            if(map==null){
                throw new InstanceException("位置错误，不能进入");
            }
            //mengjie add
            if(map.getMapId() == 177){
            	throw new InstanceException("您目前不能回到家园。");
            }
            if(map.getInstance()!=null){
                if(map.getInstance()==instance){
                    throw new InstanceException("你已经在家园里了");
                }else
                    throw new InstanceException("位置错误，不能进入");
            }
            int ownerId = instance.getOwnerId();
//            HouseData house = player2house.get(ownerId);
            HouseData house = null;
			try {
				house = getHouseByPlayerId(ownerId);
			} catch (Exception e) {
			}
            if(player.getId()==house.getPlayerId()){
                if(!usedItem.contains(player.getId())){
                    if(house.getAreaId()!=map.getMapId()){
                        //throw new InstanceException("你的家园不在此处");
                        
                    }
                }else{
                    usedItem.remove(player.getId());
                }
            }
//            if((house.getAreaId()!=map.getMapId())&&(player.getId()==house.getPlayerId())&&!usedItem.contains(player.getId())){
//
//            }
            if(checkRule(player,house)){
                instance.preAdd(new WorldPlayer[]{player});
                player.setJumpMapId(map.getMapId());
                player.setJumpX(player.getX());
                player.setJumpY(player.getY());
                if(house.getPlayerId()!=player.getId()){
                    house.addVisited(player.getId());
                }
                return instance;
            }else{
                throw new InstanceException("房主暂不允许你游览房屋");
            }
        }else{
            throw new InstanceException("没有指定房屋");
        }
    }


    public HouseInstance getPreTryHouse(WorldPlayer player){
        return player2instance.get(player.getId());
    }


    protected boolean checkRule(WorldPlayer player,HouseData house){
       if(house.getPlayerId()==player.getId()){
           return true;
       }
       else if(mateService.getMateId(player)==house.getPlayerId()){
           return true;
       }
       if(house.getRule()==House.RULE_FREE){
           return true;
       }
       else if(house.getRule()==House.RULE_GUILD){
           if(player.getTongId()>0){
               return playerService.getPlayerTongId(house.getPlayerId())==player.getTongId();
           }
       }
       else if(house.getRule()==House.RULE_TEAM){
           Team team = player.getTeam();
           if(team==null)
               return false;
           return team.contains(house.getPlayerId());
       }
       else if(house.getRule()==House.RULE_FRIENDS){
           return playerService.isFriend(house.getPlayerId(),player.getId());
       }
       return false;
    }

    public void saveAll(){
        int i = 0;
        Iterator<HouseData> ite = player2house.values().iterator();
        while(ite.hasNext()){
            HouseData hd = ite.next();
            saveHouse(hd);
            i++;
        }
        log.info("All "+i+" houses saved");
    }
    
    /**
     * 每次存数据时候的存盘量
     */
    public final static short saveRate = 30;
    
    public void save(int round){
        Iterator<HouseData> ite = player2house.values().iterator();
        while(ite.hasNext()){
            HouseData hd = ite.next();
            if(hd.getId()%saveRate==round){
                saveHouse(hd);
//                log.info("Shop ["+shop.getId()+"] Saved round["+round+"]");
            }
        }
        log.info("Houses Saved round["+round+"]");
    }
}
