package com.pip.itimes.server.world.game;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import sun.text.IntHashtable;

import com.pip.itimes.server.bean.Farm;
import com.pip.itimes.server.dao.FarmDao;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Effect;
import com.pip.itimes.server.stage.HouseData;
import com.pip.itimes.server.stage.IEffectItem;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.stage.SeedEffect;
import com.pip.itimes.server.util.PropertyPool;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.InstanceDefinition;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.MateService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.farm.FarmConfig;
import com.pip.itimes.server.world.farm.FarmData;
import com.pip.itimes.server.world.farm.FarmDropItemData;
import com.pip.itimes.server.world.farm.FarmLandInfo;
import com.pip.itimes.server.world.farm.FarmSeedData;
import com.pip.itimes.server.world.farm.FarmStealPlayer;

public class FarmInstanceModel implements InstanceModel, Runnable {
	private static final Logger log = Logger.getLogger(FarmInstanceModel.class);
	
	public static final int FARM_INSTANCE = 90018;
	
	public static long startDay = 0;
	public static final long hour24 = 24 * 60 * 60 * 1000L;
	
	private ConcurrentHashMap<Integer,FarmInstance> player2instance = new ConcurrentHashMap<Integer,FarmInstance>();
	private ConcurrentHashMap<Integer,FarmInstance> player2self = new ConcurrentHashMap<Integer,FarmInstance>();
	private ConcurrentHashMap<Integer, FarmData> player2farm = new ConcurrentHashMap<Integer, FarmData>();
	private ConcurrentHashMap<Integer, Integer> player2randomfarm = new ConcurrentHashMap<Integer, Integer>();
	private ConcurrentHashMap<Integer, Integer> playerfarm2id = new ConcurrentHashMap<Integer, Integer>();
	private List<Integer> lstplayerfarm2id = new ArrayList<Integer>();
	private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> player2randomid = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer,Integer>>();

	private WorldService worldService;
    private InstanceService instanceService;
    private PlayerService playerService;
    private MateService mateService;
    private HouseInstanceModel houseInstanceModel;
    private MailService mailService;
    
    private FarmDao farmDao;
    
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
    
    public void setHouseInstanceModel(HouseInstanceModel houseInstanceModel){
    	this.houseInstanceModel = houseInstanceModel;
    }
    
    public void setMailService(MailService mailService){
    	this.mailService = mailService;
    }
    
    public FarmInstanceModel(){
    	farmDao = new FarmDao();
    	try {
    		lstplayerfarm2id = farmDao.getAllFarmPlayerID();
			for(int i=0; i<lstplayerfarm2id.size(); i++){
				Integer id = lstplayerfarm2id.get(i);
				playerfarm2id.put(id, id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
//    	try{
//    		List farms = farmDao.getAllFarm();
//    		for(int i=0; i<farms.size(); i++){
//    			Farm farm = (Farm)farms.get(i);
//    			FarmData farmData = new FarmData(farm);
//    			if(farmData != null){
//    				player2farm.put(farm.getPlayerid(), farmData);
//    			}
//    		}
//    	}catch(Exception e){
//    		log.info(e, e);
//    	}
    }
    
	public void start(){
		new Thread(this).start();
	}
    
	public FarmInstance tryGotoInstance(int instance, WorldPlayer player, int battleID) throws InstanceException {
		FarmInstance farmInstance = player2instance.get(player.getId());
		if(farmInstance != null){
			GameMap map = player.getMap();
            if(map == null){
                throw new InstanceException("位置错误，不能进入");
            }
            if(map.getInstance() != null){
                if(map.getInstance() == farmInstance){
                    throw new InstanceException("你已经庄园里了");
                }else{
                	if(!(map.getInstance() instanceof HouseInstance)){
                		throw new InstanceException("位置错误，不能进入");
                	}
                }
            }
            farmInstance.preAdd(new WorldPlayer[]{player});
            player.setJumpMapId(map.getMapId());
            player.setJumpX(player.getX());
            player.setJumpY(player.getY());
            return farmInstance;
		}else{
			throw new InstanceException("没有指定的庄园。");
		}
	}
	
	/**
	 * 尝试获取指定角色的庄园
	 * @param player
	 * @param destPlayerid
	 * @return
	 * @throws Exception
	 */
	public synchronized FarmInstance preTry(WorldPlayer player, int destPlayerid) throws Exception{
		FarmInstance instance = getFarmInstance(destPlayerid);
		if(instance == null){
			throw new Exception("没有找到对应的庄园。");
		}else{
			player2instance.put(player.getId(), instance);
		}
		return instance;
	}

	public GameMap getGameMap(WorldPlayer player, short mapId) {
		FarmInstance farmInstance = player2instance.get(player.getId());
		if(farmInstance != null){
			return farmInstance.getMap(mapId);
		}
		return null;
	}

	public GameMap getLoginMap(WorldPlayer player, short mapId) {
		GameMap map = getGameMap(player,mapId);
        if(map != null){
        	return map;
        }else{
        	if(player.getJumpMapId() != 0){
                return worldService.getNoInstanceMap(player.getJumpMapId());
            }else{
                return worldService.getNoInstanceMap((short)353);
            }
        }
	}

	public Instance getInstance(IPlayerData player, int instanceId) {
		return null;
	}

	public void playerAddedToInstance(IPlayerData player, Instance instance) {
		
	}
	
	public FarmData getFarmData(int playerid) throws Exception{
		if(player2farm.containsKey(playerid)){
			return player2farm.get(playerid);
		}else{
			HouseData hd = houseInstanceModel.getHouseByPlayerId(playerid);
			if(hd == null){
				throw new Exception("没有找到家园。");
			}
			Farm farm = farmDao.getFarm(playerid);
			if(farm == null){
				farm = createFarm(playerid, hd.getPlayerName(), hd.getLevel());
				farmDao.makePersistent(farm);
			}
			FarmData farmData = new FarmData(farm);
			if(farmData != null){
				if(farmData.checkInvade(startDay - hour24)){
					invade(farmData);
				}
				if(!playerfarm2id.containsKey(playerid)){
					Integer pi = playerid;
					lstplayerfarm2id.add(pi);
					playerfarm2id.put(pi, pi);
				}
				player2farm.put(playerid, farmData);
			}
			return farmData;
		}
	}
	
	public int getRandomFarmData(int playerid){
		if(lstplayerfarm2id.size() == 0){
			return -1;
		}
		int index = Utils.getRandom(0, lstplayerfarm2id.size() - 1);
		if(lstplayerfarm2id.get(index) == playerid){
			index ++;
			if(index >= lstplayerfarm2id.size()){
				index = 0;
			}
			if(lstplayerfarm2id.get(index) == playerid){
				return -2;
			}
		}
		int randomCount = 0;
		if(player2randomfarm.containsKey(playerid)){
			randomCount = player2randomfarm.get(playerid);
		}
		if(randomCount >= 10){
			return -3;
		}
		
		int selectplayerid = lstplayerfarm2id.get(index);
		if(player2randomid.containsKey(playerid)){
			ConcurrentHashMap<Integer, Integer> allfarmid = player2randomid.get(playerid);
			int subcount = lstplayerfarm2id.size() - randomCount;
			if(subcount <= 0){
				return -2;
			}
			int addcount = 0;
			while(allfarmid.containsKey(selectplayerid)){
				if(addcount++ >= subcount){
					return -2;
				}
				index++;
				if(index >= lstplayerfarm2id.size()){
					index = 0;
				}
				selectplayerid = lstplayerfarm2id.get(index);
			}
			allfarmid.put(selectplayerid, selectplayerid);
		}else{
			ConcurrentHashMap<Integer, Integer> allfarmid = new ConcurrentHashMap<Integer, Integer>();
			allfarmid.put(selectplayerid, selectplayerid);
			player2randomid.put(playerid, allfarmid);
		}
		
		player2randomfarm.put(playerid, randomCount + 1);
		return lstplayerfarm2id.get(index);
	}
	
	public FarmInstance getProTryFarm(WorldPlayer player){
		return player2instance.get(player.getId());
	}
	
	public Farm createFarm(int playerid, String playername, int houseLevel){
		Farm farm = new Farm();
		byte landCount = 5;
		switch(houseLevel){
		case 2:
			landCount = 6;
			break;
		case 3:
			landCount = 7;
			break;
		case 4:
			landCount = 8;
			break;
		case 5:
			landCount = 10;
			break;
		}
		farm.setLandcount((byte)landCount);
		farm.setLandinfo(null);
		farm.setPlayerid(playerid);
		farm.setPlayerName(playername);
		try{
			farm.setLandinfo(FarmData.createDefaultLandInfo(landCount));
		}catch(Exception e){
		}
		farm.setOtherPool(new PropertyPool());
		return farm;
	}
	
	public FarmInstance getFarmInstance(int playerid) throws Exception{
		if (!player2self.containsKey(playerid)){
			FarmData farmData = getFarmData(playerid);
			player2farm.put(playerid, farmData);
			InstanceDefinition idf = instanceService.getInstanceDefinition(FARM_INSTANCE);
			FarmInstance farmInstance = createFarmInstance(idf, playerid);
			player2self.put(playerid, farmInstance);
			return farmInstance;
		}else{
			return player2self.get(playerid);
		}
	}
	
	public FarmInstance createFarmInstance(InstanceDefinition idf, int playerId){
		int id = InstanceService.getNewInstanceId();
		FarmInstance farmInstance = new FarmInstance(id, idf, instanceService);
		farmInstance.setOwnerId(playerId);
		short[] maps = idf.getMaps();
        for(int i=0;i<maps.length;i++){
            Scene scene = worldService.getInstanceScene(maps[i]);
            GameMap map = new GameMap(worldService, scene, (short)0, (short)0);
            map.setCanPk(false);
            farmInstance.addMap(map);
            map.setInstance(farmInstance);
        }
        worldService.instanceCreated(farmInstance);
        return farmInstance;
	}
	
	public void inteeractive(WorldPlayer player, int landIndex, int action){
		
	}
	
	public String getLandInfo(WorldPlayer player, int landIndex){
		GameMap gameMap = player.getMap();
		if(gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return "所在位置无法查看土地信息。";
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return "土地不存在。";
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		if(farmLandInfo == null){
			return "土地不存在。";
		}
		return farmLandInfo.toString();
	}
	
	public static final byte SEED_INSTANCEERROR = 0;
	public static final byte SEED_NOFARM = 1;
	public static final byte SEED_NOLANDINFO = 2;
	public static final byte SEED_NOSEED = 3;
	public static final byte SEED_HASSEED = 4;
	public byte hasSeed(WorldPlayer player, int landIndex){
		GameMap gameMap = player.getMap();
		if(gameMap == null || gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return SEED_INSTANCEERROR;
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return SEED_NOFARM;
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		if(farmLandInfo == null){
			return SEED_NOLANDINFO;
		}
		if(farmLandInfo.getSeed() <= 0){
			return SEED_NOSEED;
		}
		return SEED_HASSEED;
	}
	
	public FarmLandInfo getFarmLandInfo(WorldPlayer player, int landIndex){
		GameMap gameMap = player.getMap();
		if(gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return null;
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return null;
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		return farmLandInfo;
	}
	
	public String stealLand(WorldPlayer player, int landIndex, Changed changed){
		GameMap gameMap = player.getMap();
		if(gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return "所在位置无法查看土地信息。";
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		if(ownerid == player.getId()){
			return "无法窃取自己的土地。";
		}
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return "庄园不存在。";
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		if(farmLandInfo == null){
			return "该土地还未开放。";
		}
		if(!farmLandInfo.getResults()){
			return "果实还不能窃取呢，每天凌晨4点吸血鬼入侵完以后才能窃取哦。";
		}
		if(farmLandInfo.checkStealPlayer(player.getId())){
			return "人要知足，你已经摘过了一次了，再摘会被发现的。";
		}
		byte stealType = farmLandInfo.canSteal();
		switch(stealType){
		case FarmLandInfo.STEAL_NOFRUIT:
			return "这块土地的果实已经被窃取的太多了，给主人留点吧。";
		case FarmLandInfo.STEAL_CANSTEAL:
			break;
		default:
			return "土地异常。请稍候再试。";
		}
		int fruitCurrentCount = farmLandInfo.getFruitCurrentCount();
		int stealCount = fruitCurrentCount * 10 / 100;
		FarmSeedData fds = FarmConfig.getFarmSeed(farmLandInfo.getSeed());
		boolean sendMail = false;
		int sendMailCount = 0;
		int addStealCount = 0;
		if(player.hasItem(fds.getResultsid())){
			if(player.getItemCount(fds.getResultsid()) + stealCount > 99){
//				return "要窃取的果实在您的背包中过多，已经装不下了。";
				sendMail = true;
				addStealCount = 99 - player.getItemCount(fds.getResultsid());
				sendMailCount = stealCount - addStealCount;
			}
		}else if(player.isFull()){
//			return "背包已满，请清空再来。";
			sendMail = true;
			sendMailCount = stealCount;
			addStealCount = 0;
		}else{
			addStealCount = stealCount;
		}
		farmLandInfo.setFruitCurrentCount(fruitCurrentCount - stealCount);
		farmLandInfo.addStealPlayer(player.getId());
		IItemTemplate template = Items.getTemplate(fds.getResultsid());
		player.completeAddItem(template.newInstance(), addStealCount, changed, player.getClientDataVersion());
		FarmStealPlayer stealplayer = new FarmStealPlayer();
		stealplayer.setstealId(player.getId());
		stealplayer.setPlayerName(player.getPlayerName());
		stealplayer.setstealTime(System.currentTimeMillis());
		farmData.addStealPlayer(stealplayer);
		if(sendMail){
			byte[] att = ItemUtils.item2dbAttachment(template.newInstance(), sendMailCount);
			String content = "由于您窃取的" + template.getName() + "过多，";
			if(addStealCount != 0){
				content += addStealCount + "个已经加入到您的背包中，";
			}
			content += sendMailCount + "个附加在邮件中。请查收。";
			mailService.sendMail(player.getId(), player.getPlayerName(), -1, "庄园系统", "窃取物品过多", content, att, 0, true);
		}
		log.info("FarmLandSteal playerID[" + farmData.getPlayerID() + "] stealPlayerID[" + player.getId() + "] StealCount[" + stealCount + "] itemID[" + template.getItemId() + "] itemName[" + template.getName() + "] LandIndex[" + landIndex + "]");
		return "你环顾四周，熟练的摘了" + stealCount + "个" + template.getName() + "，然后快速的装进背包里。";
	}
	
	public String canSowseeds(WorldPlayer player, int landIndex){
		GameMap gameMap = player.getMap();
		if(gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return "所在位置无法查看土地信息。";
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		if(ownerid != player.getId()){
			return "这不是自己的土地，不能播种。";
		}
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return "庄园不存在。";
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		if(farmLandInfo == null){
			return "该土地还未开放。";
		}
		if(farmLandInfo.getSeed() > 0){
			return "已经播种过了。";
		}
		return null;
	}
	
	public String landLevelUp(WorldPlayer player, int landIndex, boolean check, Changed changed){
		GameMap gameMap = player.getMap();
		if(gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return "不在庄园中。";
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		if(ownerid != player.getId()){
			return "这不是自己的土地，不能升级。";
		}
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return "庄园不存在。";
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		if(farmLandInfo == null){
			return "该土地还未开放。";
		}
		if(farmLandInfo.getLevel() >= FarmLandInfo.LEVEL_MAX){
			return "土地不能再升级了。";
		}
		int levelLandCount = farmData.getLevelLandCount(1);
		int farmLevel = levelLandCount + 6;
		int money = FarmConfig.LevelUpBaseMoney * farmLevel * farmLevel * farmLevel;
		if(player.getFarmMoney() < money){
			return "很遗憾，升级土地需要" + money + "吸血鬼金元，你的金元数量还不够哦。（植物成熟的时候会从打败的吸血鬼身上获得，当前金元" + player.getFarmMoney() + ")";
		}
		if(!check){
			farmLandInfo.setLevel(farmLandInfo.getLevel() + 1);
			player.setFarmMoney(player.getFarmMoney() - money);
			if(changed != null){
				changed.addProperty(Changed.FARMMONEY, -money);
			}
			if(farmLandInfo.getSeed() > 0 && !farmLandInfo.getResults()){
				int count = farmLandInfo.getFruitCurrentCount();
				count += count * FarmConfig.LevelPercent / 100;
				farmLandInfo.setFruitCurrentCount(count);
			}
			log.info("FarmLandLevel playerID[" + farmData.getPlayerID() + "] LandLevel[" + (farmLandInfo.getLevel() + 1) + "] LandIndex[" + landIndex + "]");
			return "土地升级成功，果实产量增加了（收获期时升级果实产量不增加）！";
		}
		return null;
	}
	
	public int landLevelUpMoney(WorldPlayer player, int landIndex){
		GameMap gameMap = player.getMap();
		if(gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return 0;
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		if(ownerid != player.getId()){
			return 0;
		}
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return 0;
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		if(farmLandInfo == null){
			return 0;
		}
		if(farmLandInfo.getLevel() >= FarmLandInfo.LEVEL_MAX){
			return 0;
		}
		int levelLandCount = farmData.getLevelLandCount(1);
		int farmLevel = levelLandCount + 6;
		int money = FarmConfig.LevelUpBaseMoney * farmLevel * farmLevel * farmLevel;
		return money;
	}
	
	public String landOpen(WorldPlayer player, int landIndex, boolean check, Changed changed){
		GameMap gameMap = player.getMap();
		if(gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return "不在庄园中。";
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		if(ownerid != player.getId()){
			return "这不是自己的土地，不能开放。";
		}
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return "庄园不存在。";
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		if(farmLandInfo != null){
			return "该土地已经开放。";
		}
		if(!check){
			int farmCount = farmData.getFarm().getLandcount() + 1;
			int money = FarmConfig.LevelOpenBaseMoney * farmCount * farmCount * farmCount;
			if(player.getFarmMoney() < money){
				return "很遗憾，开放土地需要" + money + "吸血鬼金元，你的金元数量还不够哦。（植物成熟的时候会从打败的吸血鬼身上获得，当前金元" + player.getFarmMoney() + ")";
			}
			farmLandInfo = new FarmLandInfo(0, 0, 0, (byte)0);
			farmData.addLandInfo(landIndex, farmLandInfo);
			farmData.getFarm().setLandcount((byte)(farmCount));
			player.setFarmMoney(player.getFarmMoney() - money);
			if(changed != null){
				changed.addProperty(Changed.FARMMONEY, -money);
			}
			log.info("FarmLandOpen playerID[" + farmData.getPlayerID() + "] LandCount[" + (farmCount + 1) + "] LandIndex[" + landIndex + "]");
			return "土地开启啦~你可以在上面播种了哦~";
		}
		return null;
	}
	
	public int landOpenMoney(WorldPlayer player, int landIndex){
		GameMap gameMap = player.getMap();
		if(gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return 0;
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		if(ownerid != player.getId()){
			return 0;
		}
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return 0;
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		if(farmLandInfo != null){
			return 0;
		}
		int farmCount = farmData.getFarm().getLandcount() + 1;
		int money = FarmConfig.LevelOpenBaseMoney * farmCount * farmCount * farmCount;
		return money;
	}
	
	public boolean sowseeds(WorldPlayer player, int landIndex, int itemid, Changed changed){
		GameMap gameMap = player.getMap();
		if(gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return false;
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		if(ownerid != player.getId()){
			return false;
		}
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return false;
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		if(farmLandInfo == null){
			return false;
		}
		if(!player.hasItem(itemid)){
			return false;
		}
		IItem item = player.completeRemoveItem(itemid, 1, changed);
		if(item == null){
			return false;
		}
		if(!(item instanceof IEffectItem)){
			return false;
		}
		IEffectItem effectItem = (IEffectItem)item;
		Effect[] effects = effectItem.getEffects();
		for(int i=0; i<effects.length; i++){
			SeedEffect effect = (SeedEffect)effects[i];
			farmLandInfo.setSeed(effect.getSeedID());
			farmLandInfo.setCreateTime(System.currentTimeMillis());
			farmLandInfo.setFertilize((byte)0);
			FarmSeedData fsd = FarmConfig.getFarmSeed(effect.getSeedID());
			int fruitCount = fsd.getFruitCount();
			int addFruit = fruitCount + farmLandInfo.getLevel() * (fruitCount * FarmConfig.LevelPercent / 100);
			farmLandInfo.setFruitCurrentCount(addFruit);
			farmLandInfo.setStealPlayer(null);
			farmLandInfo.setResults(false);
		}
		return true;
	}
	
	public String getResults(WorldPlayer player, int landIndex, Changed changed){
		GameMap gameMap = player.getMap();
		if(gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return "不在庄园。";
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		if(ownerid != player.getId()){
			return "不是主人。";
		}
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return "没有庄园。";
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		if(farmLandInfo == null){
			return "土地还没有开放。";
		}
		if(!farmLandInfo.getResults()){
			return "果实还不能收获呢，每天凌晨4点吸血鬼入侵完以后才能收获哦。";
		}
		synchronized (farmLandInfo) {
			int count = farmLandInfo.getFruitCurrentCount();
			FarmSeedData fsd = FarmConfig.getFarmSeed(farmLandInfo.getSeed());
			if(fsd == null){
				return "找不到种子信息。";
			}
			boolean sendMail = false;
			int sendMailCount = 0;
			int addStealCount = 0;
			IItemTemplate template = Items.getTemplate(fsd.getResultsid());
			if(player.hasItem(fsd.getResultsid())){
				if(player.getItemCount(fsd.getResultsid()) + count > 99){
//					return "您身上的" + template.getName() + "太多了。";
					sendMail = true;
					addStealCount = 99 - player.getItemCount(fsd.getResultsid());
					sendMailCount = count - addStealCount;
				}
			}else if(player.isFull()){
//					return "您的背包已满。";
				sendMail = true;
				sendMailCount = count;
				addStealCount = 0;
			}else{
				addStealCount = count;
			}
			player.addItem(template, count, changed, player.getClientDataVersion());
			farmLandInfo.setSeed(0);
			farmLandInfo.setFertilize((byte)0);
			farmLandInfo.setFruitCurrentCount(0);
			farmLandInfo.setResults(false);
			farmLandInfo.setStealPlayer(null);
			if(sendMail){
				byte[] att = ItemUtils.item2dbAttachment(template.newInstance(), sendMailCount);
				String content = "由于您收获的" + template.getName() + "过多，";
				if(addStealCount != 0){
					content += addStealCount + "个已经加入到您的背包中，";
				}
				content += sendMailCount + "个附加在邮件中。请查收。";
				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "庄园系统", "收获物品过多", content, att, 0, true);
			}
			return "收获的喜悦：恭喜你收获了" + count + "个" + template.getName() + "哦！";
		}
	}
	
	public String fertilize(WorldPlayer player, int landIndex, boolean check, Changed changed){
		GameMap gameMap = player.getMap();
		if(gameMap.getInstance() == null || !(gameMap.getInstance() instanceof FarmInstance)){
			return "不在庄园中。";
		}
		FarmInstance farmInstance = (FarmInstance)gameMap.getInstance();
		int ownerid = farmInstance.getOwnerId();
		if(ownerid != player.getId()){
			return "这不是自己的土地，不能施肥。";
		}
		FarmData farmData = player2farm.get(ownerid);
		if(farmData == null){
			return "庄园不存在。";
		}
		FarmLandInfo farmLandInfo = farmData.getLandInfo(landIndex);
		if(farmLandInfo == null){
			return "该土地还未开放。";
		}
		if(farmLandInfo.getSeed() <= 0){
			return "土地还没有播种。";
		}
		if(farmLandInfo.getResults()){
			return "土地不需要施肥了。";
		}
		if(farmLandInfo.getFertilize() + 1 > FarmLandInfo.FERTILIZE_MAXCOUNT){
			return "已经施过肥了。";
		}
		if(!player.hasItem(FarmConfig.FertilizeItemID)){
			return "你还没有施肥需要的化肥，你可以在商城里买到哦。";
		}
		if(!check){
			farmLandInfo.setFertilize((byte)(farmLandInfo.getFertilize() + 1));
			int count = farmLandInfo.getFruitCurrentCount();
			count += count * FarmConfig.FertilizeAddResultsPercent / 100;
			farmLandInfo.setFruitCurrentCount(count);
			player.completeRemoveItem(FarmConfig.FertilizeItemID, 1, changed);
			return "恭喜：施肥成功，植物的果实产量增加了哦";
		}
		return null;
	}
	
	public boolean isMaster(WorldPlayer player){
		if(player.getMap() != null && player.getMap().getInstance() instanceof FarmInstance){
			if(((FarmInstance)player.getMap().getInstance()).getOwnerId() == player.getId()){
				return true;
			}
		}
		return false;
	}
	
	public void register(WorldPlayer player){
		if(!player2farm.containsKey(player.getId())){
			try{
				getFarmData(player.getId());
			}catch(Exception e){
			}
		}
	}
	
	public void savePlayerFarm(int playerid){
		if(player2farm.containsKey(playerid)){
			saveFarm(player2farm.get(playerid));
		}
	}
	
	public void saveFarm(FarmData farmData){
		farmData.reset();
		try{
			farmDao.saveFarm(farmData.getFarm());
		}catch(Exception e){
		}
	}
	
	public void saveAll(){
		int count = 0;
		Iterator<FarmData> iter = player2farm.values().iterator();
		while(iter.hasNext()){
			FarmData farmData = iter.next();
			saveFarm(farmData);
			count ++;
		}
		log.info("All " + count + " Farm saved");
	}
	
	public void run(){
		final long sleep = 60000L;
		long tmpNow = new Date().getTime();
		startDay = Utils.getTodayStart() + FarmConfig.HourStart;
		if(startDay > tmpNow){
			startDay -= hour24;
		}
		while(true){
			long now = System.currentTimeMillis();
			if(startDay + hour24 < now){
				//每天4点处理庄园
				try {
					log.info("Farm clear try");
					Iterator<FarmData> iter = player2farm.values().iterator();
					while(iter.hasNext()){
						FarmData farmData = iter.next();
						invade(farmData);
					}
					player2randomfarm.clear();
					player2randomid.clear();
					log.info("Farm clear end");
				} catch (Exception e) {
					log.info(e, e);
				}
				startDay += hour24;
			}
			try{
				Thread.sleep(sleep);
			}catch (Exception e) {
			}
		}
	}
	
	public void invade(FarmData farmData) throws Exception{
		if(farmData != null){
			HashMap<Integer, FarmLandInfo> landInfo = farmData.getLandInfo();
			if(landInfo != null){
				Iterator<FarmLandInfo> iterLand = landInfo.values().iterator();
				int farmMoney = 0;
				int fruitAP = 0;
				int vampireHP = FarmConfig.vampireHP * FarmConfig.vampireCount;
				while(iterLand.hasNext()){
					FarmLandInfo land = iterLand.next();
					if(land != null){
						if(land.getSeed() > 0){
							FarmSeedData fsd = FarmConfig.getFarmSeed(land.getSeed());
							if(fsd != null){
								if(!land.getResults() && land.isMature()){
									fruitAP += fsd.getAP() * land.getFruitCurrentCount();
								}
							}
						}
					}
				}
				if(fruitAP <= 0){
					return;
				}
				farmMoney = fruitAP * 5;
				int landCount = landInfo.size();
				int type = landCount - 7 + 1;
				if(type < 1){
					type = 1;
				}
				if(type > 4){
					type = 4;
				}
//				boolean load = false;
//				WorldPlayer player = playerService.getWorldPlayer(farmData.getPlayerID());
//				if(player == null){
//					player = playerService.loadWorldPlayer(farmData.getPlayerID());
//					if(player != null){
//						load = true;
//					}
//				}
				WorldPlayer player = playerService.getWorldPlayerAndCatch(farmData.getPlayerID());
				if(player != null){
					player.setFarmMoney(player.getFarmMoney() + farmMoney);
				}
				StringBuilder message = new StringBuilder();
				message.append("您的庄园遭受到吸血鬼的袭击，不过在勇敢的植物们顽强抗战下击退了吸血鬼的攻势，共缴获战利品：");
				message.append("金元:" + farmMoney);
//				if(player != null){
//					player.reset();
//					playerService.savePlayer(player);
//				}
//				if(load){
//					playerService.unRegistry(player);
//				}
				playerService.releasePlayer(player);
				ArrayList<FarmDropItemData> items = FarmConfig.getFarmDropItem(type);
				if(items.size() > 0){
					for(int i=0; i<items.size(); i++){
						FarmDropItemData farmDropItemData = items.get(i);
						IItemTemplate item = Items.getTemplate(farmDropItemData.getItemid());
						int count = 0;
						for(int j=0; j<FarmConfig.vampireCount; j++){
							if(Utils.hit(fruitAP, vampireHP)){
								count += farmDropItemData.getCount();
							}
						}
						if(count > 99){
							count = 99;
						}
						if(count == 0){
							continue;
						}
						byte[] itemdb = ItemUtils.item2dbAttachment(item.newInstance(), count);
						mailService.sendMail(farmData.getPlayerID(), farmData.getPlayerName(), -1, "庄园系统", "战利品", "缴获的战利品。", itemdb, 0, true);
						message.append("\n" + item.getName() + "X" + count);
						log.info("FarmDropItem send mail playerID[" + farmData.getPlayerID() + "] itemID[" + item.getItemId() + "] itemName[" + item.getName() + "] count[" + count + "]");
					}
				}
				message.append("\n但是也损失了：");
				int percent = FarmConfig.getFarmResultsPercent(type);
				iterLand = landInfo.values().iterator();
				HashMap<Integer, Integer> itemCount = new HashMap<Integer, Integer>();
				while(iterLand.hasNext()){
					FarmLandInfo land = iterLand.next();
					if(land != null){
						if(land.getSeed() > 0){
							if(!land.getResults() && land.isMature()){
								FarmSeedData fsd = FarmConfig.getFarmSeed(land.getSeed());
								if(fsd != null){
									int currentCount = land.getFruitCurrentCount();
									land.setFruitCurrentCount(land.getFruitCurrentCount() * percent / 100);
									land.setResultsCount(land.getFruitCurrentCount() * FarmConfig.ResultsPercent / 100);
									land.setResults(true);
									int tmpCount = 0;
									if(itemCount.containsKey(fsd.getResultsid())){
										tmpCount = itemCount.get(fsd.getResultsid());
									}
									tmpCount += currentCount - land.getFruitCurrentCount();
									itemCount.put(fsd.getResultsid(), tmpCount);
								}
							}
						}
					}
				}
				if(itemCount.size() > 0){
					Iterator<Entry<Integer, Integer>> counts = itemCount.entrySet().iterator();
					while(counts.hasNext()){
						Entry<Integer, Integer> entry = counts.next();
						IItemTemplate template = Items.getTemplate(entry.getKey());
						message.append("\n" + template.getName() + "X" + entry.getValue() + "个");
						log.info("FarmFruit send mail playerID[" + farmData.getPlayerID() + "] itemID[" + template.getItemId() + "] itemName[" + template.getName() + "] count[" + entry.getValue() + "]");
					}
				}
				mailService.sendMail(farmData.getPlayerID(), farmData.getPlayerName(), -1, "庄园系统", "吸血鬼庄园战报", message.toString(), null, 0, true);
				String chat = FarmConfig.getRandomChat();
				if(chat != null){
					mailService.sendMail(farmData.getPlayerID(), farmData.getPlayerName(), -1, "庄园系统", "吸血鬼们还给你留下了一封挑衅信", chat, null, 0, true);
				}
			}
			saveFarm(farmData);
		}
	}
	
	public void changeHouse(WorldPlayer player) throws Exception{
		FarmData farmData = player2farm.get(player.getId());
		if(farmData != null){
			HouseData hd = houseInstanceModel.getHouseByPlayerId(player.getId());
			if(hd == null){
				return;
			}
			Farm farm = createFarm(player.getId(), player.getPlayerName(), hd.getLevel());
			HashMap<Integer, FarmLandInfo> landInfo = farmData.getLandInfo();
			if(landInfo.size() < farm.getLandcount()){
				int addCount = farm.getLandcount() - landInfo.size();
				while(addCount > 0){
					for(int i=0; i<FarmConfig.FARM_LAND_MAXCOUNT; i++){
						if(!landInfo.containsKey(i)){
							FarmLandInfo farmLandInfo = new FarmLandInfo(0, 0, 0, (byte)0);
							landInfo.put(i, farmLandInfo);
							addCount --;
							break;
						}
					}
				}
				farmData.getFarm().setLandcount(farm.getLandcount());
			}
		}
	}

}
