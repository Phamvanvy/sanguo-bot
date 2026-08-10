package com.pip.itimes.server.world;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

import com.pip.accountskeleton.AccountSkeleton;
import com.pip.itimes.server.bean.Master;
import com.pip.itimes.server.bean.Petmanager;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.PlayerDao;
import com.pip.itimes.server.stage.PlayerData;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

import com.pip.itimes.server.stage.DropGroup;
import com.pip.itimes.server.stage.DropGroups;
import com.pip.itimes.server.stage.DropItem;
import com.pip.itimes.server.stage.Grid;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.MagicPosMessage;
import com.pip.itimes.server.stage.PlayerCreditWapper;
import com.pip.itimes.server.stage.Friend;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.util.KeywordsUtil;
import com.pip.itimes.server.util.PropertyPool;
import com.pip.itimes.server.util.Utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import com.pip.itimes.server.bean.TaskData;
import com.pip.itimes.server.world.boss.BossService;
import com.pip.itimes.server.world.game.CampBattlefieldConfig;
import com.pip.itimes.server.world.game.HouseInstanceModel;
import com.pip.itimes.server.world.message.ChannelGiftData;
import com.pip.itimes.server.world.message.WelcomeMessage;
import com.pip.itimes.server.world.unline.UnlineExpConfig;
import com.pip.itimes.server.stage.HouseData;
import com.pip.itimes.server.stage.Pet;
import com.pip.net.message.gameaccount.LevelUpNotifyMessage;

public class PlayerService implements Runnable{
	
	/**
	 * 杀人掉宝石掉落组
	 */
	private final int equDiamondDropGroup = 220;
	/**
	 * 杀人掉宝石掉落组
	 */
	private final int equDiamondTitle = 200843;
	
    private Logger log = Logger.getLogger(PlayerService.class);
    
    private static SimpleDateFormat format = new SimpleDateFormat ("yyyy-MM-dd HH:mm");

    private PlayerDao dao;
    private ConcurrentHashMap players = new ConcurrentHashMap();
    private ConcurrentHashMap names = new ConcurrentHashMap();
    private ConcurrentHashMap accounts = new ConcurrentHashMap();
    private ConcurrentHashMap masterPlayers = new ConcurrentHashMap();		//达到做师傅的角色表

    private ShopService shopService = null;
    private BufService bufService = null;
    private ConnectService connectService = null;
    private HouseInstanceModel houseModel = null;
    private BossService bossService;
    
    private MailService mailService;
    private PetmanagerService petmanagerService = null;
    
    private MasterService masterService = null;

    public int getPlayerCount(){
        return players.size();
    }
    
    public int getMasterPlayerCount(){
    	return masterPlayers.size();
    }
    
    public MailService getMailService() {
		return mailService;
	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}
	
	public void setMasterService(MasterService masterService){
		this.masterService = masterService;
	}
	
	public MasterService getMasetService(){
		return masterService;
	}
	
	public PetmanagerService getPetmanagerService () {
		return petmanagerService;
	}
	
	public void setPetmanagerService (PetmanagerService petmanagerService) {
		this.petmanagerService = petmanagerService;
	}
	private Map forbids = new HashMap();
    //mengjie add
    protected FriendsService friendsService;
    public void setFriendsService(FriendsService friendsService){
        this.friendsService = friendsService;
    }
    protected ChatService chatService;
    public void setChatService(ChatService chatService){
        this.chatService = chatService;
    }
    private AccountSkeleton accountSkeleton;
    public void setAccountSkeleton(AccountSkeleton accountSkeleton){
        this.accountSkeleton = accountSkeleton;
    }
    //mengjie add end
    private ConcurrentHashMap<Integer,VipBathRecorder> vips = new ConcurrentHashMap<Integer,VipBathRecorder>();

    public PlayerService(PlayerDao dao) {
        this.dao = dao;
    }

    public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }

    public void setShopService(ShopService shopService){
        this.shopService = shopService;
    }

    public void setBufService(BufService bufService){
        this.bufService = bufService;
    }

    public void setHouserModel(HouseInstanceModel model){
        this.houseModel = model;
    }
    
    public void setBossService(BossService bossService){
        this.bossService = bossService;
    }

    public WorldPlayer[] getPlayers(){
        WorldPlayer[] ret = new WorldPlayer[players.size()];
        players.values().toArray(ret);
        return ret;
    }
    
    public WorldPlayer[] getMasterPlayers(){
    	WorldPlayer[] ret = new WorldPlayer[masterPlayers.size()];
    	masterPlayers.values().toArray(ret);
    	return ret;
    }

    public List getPlayerList(int accountId) throws DataAccessException{
        return dao.getPlayerList(accountId);
    }

    public Player getPlayerByNameAndAccountId(String name, int accountId) {
        try {
            return dao.getPlayerByNameAndAccountId(name, accountId);
        } catch (DataAccessException e) {
            return null;
        }
    }

    public Player getPlayerByName(String name){
        try {
            return dao.getPlayerByName(name);
        } catch (DataAccessException ex) {
            return null;
        }
    }


    public Player loadPlayerById(int id){
        try {
            return dao.getPlayerById(id);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public Player loadPlayerByName(String name,int accountId){
        try {
            return dao.getPlayerByNameAndAccountId(name, accountId);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public List<Player> getPlayerAdminVaildByName(String name){
        try {
            return dao.getPlayerAdminValidByName(name);
        } catch (DataAccessException ex) {
            return null;
        }
    }
    
    public List<Player> getPlayerAdminNoVaildByName(String name){
        try {
            return dao.getPlayerAdminNoValidByName(name);
        } catch (DataAccessException ex) {
            return null;
        }
    }
    
    public Player loadPlayerByName(String name){
        try {
            return dao.getPlayerByName(name);
        } catch (DataAccessException ex) {
            return null;
        }
    }

    public Player[] getPlayerByAccountId(int accountId){
        try {
            return dao.getPlayerByAccountid(accountId);
        } catch (DataAccessException ex) {
            return new Player[0];
        }
    }


    public WorldPlayer getWorldPlayer(int id) {
        return (WorldPlayer)players.get(new Integer(id));
    }
    
    public WorldPlayer loadWorldPlayer(int id) throws Exception{
        synchronized(this){
            WorldPlayer player = (WorldPlayer) players.get(new Integer(id));
            if (player == null) {
                Player p = loadPlayerById(id);
                if (p != null) {
                    player = new WorldPlayer(p);
                    registry(player);
                }
            }
            return player;
        }
    }
    
    public WorldPlayer getWorldPlayerAndCatch(int id) {
        try {
            WorldPlayer player = loadWorldPlayer(id);
            if (player != null) {
                acquire(player);
            }
            return player;
        } catch (Exception ex) {
            return null;
        }
    }
    
    public WorldPlayer getWorldPlayerAndCatch(String name) {
        try {
            WorldPlayer player = loadWorldPlayer(name);
            if (player != null) {
                acquire(player);
            }
            return player;
        } catch (Exception ex) {
            return null;
        }
    }
    
    public void releasePlayer(WorldPlayer player) {
        if (player != null)
            release(player);
    }

    public WorldPlayer loadWorldPlayer(String name) throws Exception{
        synchronized(this){
            WorldPlayer player = (WorldPlayer) names.get(name);
            if (player == null) {
                Player p = this.loadPlayerByName(name);
                if (p != null) {
                	//name可能存在大小写不一致的问题上 所以在names里面判定不到
                	//所以读取了角色数据库信息后 再次判断是否存在players里面
                	if(players.get(p.getId()) != null){
                		return (WorldPlayer)players.get(p.getId());
                	}
                    player = new WorldPlayer(p);
                    registry(player);
                    return player;
                }
            }
            return player;
        }
    }
    
    public WorldPlayer[] loadAdminWorldPlayer(String name) throws Exception{
    	 synchronized(this){
    		
    		 List<Player>  playerList=  this.getPlayerAdminVaildByName(name);
    		 List<Player>  playerNoValidList = this.getPlayerAdminNoVaildByName(name);
    		 WorldPlayer[] players = new WorldPlayer[playerList.size() + playerNoValidList.size()];
    		 for(int i = 0; i < playerList.size(); i++){
    			 Player player = playerList.get(i);
    			 players[i] = new WorldPlayer(player);
                 //registry(players[i]);
    		 }
    		 
    		 for(int i = 0; i < playerNoValidList.size(); i++){
    			 Player player = playerNoValidList.get(i);
    			 players[i + playerList.size()] = new WorldPlayer(player);
                 //registry(players[i + playerList.size()]);
    		 }
             return players;
         }
    }
    public WorldPlayer loadWorldPlayer(String name,int accountId) throws Exception{
        synchronized(this){
            WorldPlayer player = (WorldPlayer) names.get(name);
            if (player == null) {
                Player p = loadPlayerByName(name, accountId);
                if (p != null) {
                    player = new WorldPlayer(p);
                    registry(player);
                    log.info("ID["+player.getId()+"]Level["+player.getLevel()+"] load from db");
                    return player;
                }
            } else {
                if (player.getAccountId() == accountId) {
                    player.setLastFeeTime(System.currentTimeMillis());
                    log.info("ID["+player.getId()+"]Level["+player.getLevel()+"] load from cache");
                    return player;
                }
            }
            log.info("ACCOUNTID[" + accountId + "]FAIL TO LOGIN " + name);
            return null;
        }
    }

    public WorldPlayer getOnlinePlayerWithSex(int sex){
        return null;
    }

    public WorldPlayer getWorldPlayer(String name){
        return (WorldPlayer)names.get(name);
    }

    public Player getPlayer(String name){
        try {
            return dao.getPlayerByName(name);
        } catch (DataAccessException ex) {
            return null;
        }
    }
    
    public void setFriendsLoginTime(WorldPlayer player){
    	try{
    		Friend[] friends = player.getFriends();
    		List list = dao.getFriendsLastLoginTime(friends);
    		if(list != null && list.size() > 0){
    			for(int i=0; i<list.size(); i++){
    				Object[] object = (Object[])list.get(i);
    				int id = (Integer)object[0];
    				long loginTime = object[1] != null ? ((Date)object[1]).getTime() : 0;
    				for(int n=0; n<friends.length; n++){
    					if(friends[n].getId() == id){
    						friends[n].setLoginTime(loginTime);
    						break;
    					}
    				}
    			}
    		}
    	}catch(Exception e){
    		log.info(e, e);
    	}
    }
    
    /**
     * 添加角色到师傅列表 中
     * @param player
     */
    public void addMasterPlayer(WorldPlayer player, Changed changed){
    	if(player != null){
	    	synchronized (player) {
		    	//在此处添加出师检测
		    	if(player != null && player.getLevel() >= MasterService.MASTER_LEVEL_MIN && masterService.isPrentice(player) && player.online()){
		    		Changed changed2 = new Changed();
		    		try{
		    			Master mt = masterService.unRelation(player, changed, changed2);
//		    			WorldPlayer master = getWorldPlayer(mt.getMasterId());
		    			WorldPlayer master = getWorldPlayerAndCatch(mt.getMasterId());
		    			int credit = changed2.getProperty(Changed.CREDIT);
//		    			if(master == null || !master.online()){
//		    				master = loadWorldPlayer(mt.getMasterId());
//		    				if(master != null){
//			    				master.addCredit(credit, null);
//			    				master.setFame(master.getFame() + credit);
//			    				master.reset();
//			    				savePlayer(master);
//			    				masterService.setMasterFame(master, master.getFame());
//		    				}
//		    			}else{
		    			if(master != null){
		    				master.addCredit(credit, null);
		    				master.setFame(master.getFame() + credit);
		    				chatService.sendPrivateMessage(-1,"系统",mt.getMasterId(),
							"恭喜您的徒儿" + player.getPlayerName() + "出师了，您的荣誉和声望提升了" + credit + "点。");
		    				//设置徒弟的个数
		    				Master[] apprentices = master.getApprentices();
		    				if(apprentices == null){
		    					apprentices = new Master[1];
		    					apprentices[0] = mt;
		    					master.setApprentices(apprentices);
		    				}else{
		    					Master[] apps = new Master[apprentices.length + 1];
		    					System.arraycopy(apprentices, 0, apps, 0, apprentices.length);
		    					apps[apprentices.length] = mt;
		    					master.setApprentices(apps);
		    				}
		    				masterService.setMasterFame(master, master.getFame());
		    			}
		    			releasePlayer(master);
//		    			}
		    			if(credit > 0){
							mailService.sendMail(mt.getMasterId(), mt.getMasterName(), -1, "系统", "徒弟出师荣誉*" + credit, "你的徒弟" + player.getPlayerName() + "出师了，这是荣誉奖励。", null, 0, true);
							mailService.sendMail(mt.getMasterId(), mt.getMasterName(), -1, "系统", "徒弟出师声望*" + credit, "你的徒弟" + player.getPlayerName() + "出师了，这是声望奖励。", null, 0, true);
						}
		    			mailService.sendMail(mt.getMasterId(), mt.getMasterName(), -1, "系统", player.getPlayerName() + "出师了!", "尊敬的师傅，感谢您一直以来对我的关怀和帮助，不过现在也该到我学会自立的时候了，一日为师终身为父，我虽然出师了，但是永远是您的徒儿！徒儿敬上。", null, 0, true);
		    			mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", "恭喜你出师了!", "亲爱的徒儿，你已经60级了，该出师自己去历练一番了，师傅永远在你身后支持你鼓励你。", null, 0, true);
		    			
		    			if(changed == null){
	    					IItemTemplate template = Items.getTemplate(MasterService.APPITEM_ID);
	    	            	mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", "出师奖励", "您出师了，获得了出师奖励。", ItemUtils.item2dbAttachment(template.newInstance(), 2), 0, true);
	    	            	Changed tmpChanged = new Changed();
	    	            	player.addExp(300000, tmpChanged);
	    	            	connectService.sendGetItem(tmpChanged, player.getId(), (byte)20);
		    			}
		    			
		    			chatService.sendPrivateMessage(-1,"系统",player.getId(),
		    					"恭喜您出师了，得到300000经验和2个3级原石定向包。");
		    		}catch(Exception e){
		    			e.printStackTrace();
		    		}
		    	}
		    	
		    	if(player != null && player.getLevel() >= MasterService.MASTER_LEVEL_MIN){
		    		if(player.offlinemode()) return;	//省流量时不将角色加入师傅列表
		    		if(player.getCamp() == Utils.NO_CAMP) return;
		    		if(!masterPlayers.containsKey(new Integer(player.getId()))){
		    			if(masterService.getPrenticeCount(player) < MasterService.APPRENTICE_MAX){
		    				masterPlayers.put(new Integer(player.getId()), player);
		    				return;
		    			}
		    		}else{
		    			//存在时 已经到达收徒上限，则移除
		    			if(masterService.getPrenticeCount(player) >= MasterService.APPRENTICE_MAX){
		    				removeMasterPlayer(player);
		    			}
		    			return;
		    		}
		    	}else if(player != null && masterPlayers.containsKey(new Integer(player.getId()))){
		    		//在师傅表里 但等级由于其它原因低于师傅的等级，不能再当师傅了
		    		if(player.getLevel() < MasterService.MASTER_LEVEL_MIN){
		    			removeMasterPlayer(player);
		    		}
		    	}
	    	}
    	}
    }
    
    /**
     * 把角色从师傅列表中移除
     * @param player
     */
    public void removeMasterPlayer(IPlayerData player){
    	if(player != null && masterPlayers.containsKey(new Integer(player.getId()))){
    		masterPlayers.remove(new Integer(player.getId()));
    	}
    }
    
    public void resetMasterCallCount(){
    	WorldPlayer[] masters = getMasterPlayers();
    	for(WorldPlayer player : masters){
    		if(player != null){
    			player.setCallCount(0);
    		}
    	}
    }
    
    public void resetCampBattlefieldKillPoints () {
    	WorldPlayer[] players = getPlayers();
    	for (WorldPlayer player : players) {
    		if (player != null) {
    			Changed changed = new Changed();
    			int points = player.getCampBattlefieldKillPoints();
    			if (points > CampBattlefieldConfig.DEFAULT_POINTS) {
    				int sendPoints = points - CampBattlefieldConfig.DEFAULT_POINTS;
    				changed.addProperty(Changed.KILL_POINT,  - sendPoints);
    			} else {
    				int sendPoints = CampBattlefieldConfig.DEFAULT_POINTS - points;
    				changed.addProperty(Changed.KILL_POINT, sendPoints);
    			}
    			player.setCampBattlefieldKillingPoints(CampBattlefieldConfig.DEFAULT_POINTS);
    			player.setLastResetKillPointsTime(new Date());
    			connectService.sendGetItem(changed, player.getId(), (byte)3);
    		}
    	}
    }
    
    public WorldPlayer getMasterPlayer(int playerId){
    	return (WorldPlayer)masterPlayers.get(new Integer(playerId));
    }
    


    public void registry(WorldPlayer player) {
    	try{
    		player.setApprentices(masterService.loadMasterApprentices(Master.SUCCESS, player.getId()));
    	}catch(Exception e){
    	}
        players.put(new Integer(player.getId()), player);
        addMasterPlayer(player, null);
        names.put(player.getPlayerName(), player);
        accounts.put(new Integer(player.getAccountId()),player);
        bufService.registry(player);
    }

    public void unRegistry(IPlayerData player) {
//    	player.setUnlineOnlineLife(player.calcOnlineLife());
    	//由于存在会改变角色的亲密度,离线的时候进行角色的亲密度保存
    	try {
    		masterService.setMasterFame(player, player.getFame());
			masterService.saveRelation(player);
		} catch (Exception e) {
		}
		
    	player.setLastlogoutTime(new Date());
    	Server.player_lastlogout_time.remove(player.getId());
        players.remove(new Integer(player.getId()));
        removeMasterPlayer(player);
        names.remove(player.getPlayerName());
        accounts.remove(new Integer(player.getAccountId()));
        bufService.unRegistry(player);
        bossService.deleteBoss(player);
        if (Server.instance.arenaSession!=null){
        	Server.instance.arenaSession.arenaLeave(player, true);
        }
    }
    
    public void renametocatch(WorldPlayer player,String oldname) {
    	players.remove(new Integer(player.getId()));
    	removeMasterPlayer(player);
        names.remove(oldname);
        accounts.remove(new Integer(player.getAccountId()));
        bufService.unRegistry(player);
        bossService.deleteBoss(player);
        if (Server.instance.arenaSession!=null){
        	Server.instance.arenaSession.arenaLeave(player, true);
        }
    }

//    public void savePlayer(Player player){
//        try{
//            dao.makePersistent(player);
//        }catch(DataAccessException ex){
//            ex.printStackTrace();
//        }
//    }

    public void savePlayer(IPlayerData player){
        try {
            dao.makePersistent(player.getPlayer());
        } catch (DataAccessException ex) {
        }
    }

    public void savePlayer(Player player){
        try {
            dao.makePersistent(player);
        } catch (DataAccessException ex) {
        }
    }

    public int getPlayerId(String name){
        WorldPlayer player = (WorldPlayer)names.get(name);
        if(player!=null)
            return player.getId();
        int id = -1;
        try {
            id = dao.getPlayerId(name);
        } catch (DataAccessException ex) {
        }
        return id;
    }

    public int getPlayerTongId(int playerId){
        WorldPlayer player = getWorldPlayer(playerId);
        if(player!=null){
            return player.getTongId();
        }else{
            try {
                return dao.getPlayerTongId(playerId);
            } catch (DataAccessException ex) {
                return -1;
            }
        }
    }

    public Friend[] getPlayerFriends(int playerId){
        WorldPlayer player = getWorldPlayer(playerId);
        if(player!=null){
            return player.getFriends();
        }else{
            try {
                return dao.getPlayerFriends(playerId);
            } catch (DataAccessException ex) {
                return new Friend[0];
            }
        }
    }

    public boolean isFriend(int source,int dest){
        Friend[] fs = getPlayerFriends(source);
        for(int i=0;i<fs.length;i++){
            if(fs[i].getId()==dest)
                return true;
        }
        return false;
    }

    public void acquire(IPlayerData player) {
        synchronized (player) {
            if (players.containsKey(new Integer(player.getId()))) {
                player.acquire();
            }else{
            	if(player instanceof WorldPlayer){
		            players.put(new Integer(player.getId()),player);
		            player.acquire();
            	}
            }
        }
    }

    public boolean release(IPlayerData player) {
        synchronized(player){
            if (players.containsKey(new Integer(player.getId()))) {
                player.release();
                if (player.getRef() <= 0) {
                	try{
                		unRegistry(player);
                	} catch (Exception ex) {
                		log.info("ID["+player.getId()+"] unRegistry Error");
                		ex.printStackTrace();
                    }
                    player.reset();
                    savePlayer(player);
                    log.info(player.toString());
                    log.info("ID["+player.getId()+"]Saved Service");
                    return true;
                }
            }else{
                log.info("ID["+player.getId()+"] Release Error");
            }
        }
        return false;
    }

    public void checkPlayer(WorldPlayer player){
        if(!players.containsKey(new Integer(player.getId()))){
            player.reset();
            savePlayer(player);
            log.info("ID["+player.getId()+"]Saved in Checked");
        }
    }

    public WorldPlayer getWorldPlaqerByAccountId(int accountId){
        return (WorldPlayer)accounts.get(new Integer(accountId));
    }
    public int getAccountIdByPlayerName(String name){
        try {
            return dao.getAccountIdByPlayerName(name);
        } catch (DataAccessException ex) {
            return -1;
        }
    }
    public void saveAll(){
        WorldPlayer[] players = getPlayers();
        for(int i=0;i<players.length;i++){
            players[i].reset();
            savePlayer(players[i]);
            log.info(players[i].getPlayerName()+" saved");
        }
        log.info("All "+players.length+" player saved");
    }

    public void addForbiden(int id, int second) {
        if (second == 0) {
            forbids.remove(new Integer(id));
        } else {
            PlayerForbiden forbiden = new PlayerForbiden(id,
                    System.currentTimeMillis() + second * 1000L);
            forbids.put(new Integer(id), forbiden);
        }
    }

    public boolean isFrobiden(int id) {
        PlayerForbiden f = (PlayerForbiden) forbids.get(new Integer(id));
        if (f == null)
            return false;
        return System.currentTimeMillis() < f.validTime;
    }

    public int size(){
        return players.size();
    }

    public void start(){
        new Thread(this).start();
    }

    public Iterator players(){
        return players.values().iterator();
    }

    public List loadAllPlayers() throws DataAccessException{
        return dao.getAllPlayers();
    }

    private int round = 0;
    private int cmcc_msg_round = 0;
    private int cmcc_msg_type = 1;
    
    /**
     * 检测在线玩家装备
     */
    public void checkBossEquip(){
    	Map<Integer, WorldBossEquipInfo> delayMap = Server.player_Delay;
    	Server.delay.clear();
    	for(Map.Entry<Integer, WorldBossEquipInfo> equ: delayMap.entrySet()){
    		
    		WorldBossEquipInfo info = equ.getValue();
    		Integer playerId = equ.getKey();
    		if(info.isOnline()){
        		if(players.containsKey(playerId)){
        			WorldPlayer player = (WorldPlayer) players.get(playerId);
        			synchronized (player) {
        				Map<IItem, Integer> itemMap = info.getEquDiamondTimeMap();
        	    		for(Map.Entry<IItem, Integer> equDiamond: itemMap.entrySet()){
        	    			IItem item = equDiamond.getKey();
        	    			Grid grid = player.getEquipmentByInstanceid(item.getId());
        	    			if(grid == null){
        	    				bossService.resetWorldBossRefresh(item.getItemId());
        	    				itemMap.remove(equDiamond.getKey());
        	    				//mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", "下线通知", "你因为下线时间超过1分钟、违反规则而被扣除[" + item.getName() + "],[巨龙夜煞]将重现、再将锦盒赐予人类，你还有机会。" , null, 0, true);
                         		log.info("playerID["+player.getId()+"]equ transfer dimond no equ itemd[" + item.getItemId() + "] id [" + item.getId() + "]");
                         		break;
        	    			}else{//找到了该宝石
        	    				if(equDiamond.getValue() > 0){
        	    					if(!player.reduceEquDiamondTimeFlag(grid.item.getId())){
            	    					int time = equDiamond.getValue();
            	    					time -= cycleTime;
            	    					itemMap.put(item, time);
            	    				}
        	    				}else{//固化为宝石
        	    					Changed changed = new Changed();
                     				if(player.completeRemoveItem(item, item.getId(), changed) != null){
                     					DropGroup group = DropGroups.getDropGroup(equDiamondDropGroup, player.getLevel());
                     					Random rnd = new Random();
                         	    		if(group != null){
                         		    		int rate = rnd.nextInt(group.getRate());
                         		            DropItem dropItem = group.calcDropItem(
                         		                    rate);
                         		            player.addItem(dropItem.getItem(), 1,
                         		            		changed, player.getClientDataVersion());
                         		            player.addItem(equDiamondTitle, 1, changed, player.getClientDataVersion());
                         		            //chatService.sendWorldMessage(-1, "系统", "玩家" + player.getPlayerName() + "获得的" + grid.item.getName() + "已经固化成" + dropItem.getItem().getName() + "啦！，恭喜他吧！");
                         		            chatService.sendWorldMessage(-1, "系统"," 功夫不负有心人！玩家" + player.getPlayerName() + "打开了" + grid.item.getName() + "，获得了一颗璀璨夺目的" +  dropItem.getItem().getName() + "，这是他为" + Utils.getCampName(player.getCamp(), player.getPlayerName()) + "赢得的至高荣誉，为他欢呼吧！");
                         		            
                         		            log.info("equ transfer dimond success Id[" + grid.item.getId() + "] diamond Id[" + dropItem.getItem().getItemId() + "]");
                         	    		}
                     					connectService.sendGetItem(changed, player.getId(), (byte)36);
                     					itemMap.remove(equDiamond.getKey());
                     					break;
                     				}else{
                     					log.info("equ transfer dimond error Id[" + grid.item.getId() + "]");
                     				}
        	    				}
        	    			
        	    			}
        	    		}
        	    		
        	    		if(itemMap.size() == 0){
        	    			//Server.player_Delay.remove(equ.getKey());
        	    			Server.delay.add(equ.getKey());
        	    		}
    				}
        		}
    		}else{//玩家掉线延迟
    			WorldPlayer player = null;
				try {
//					player = loadWorldPlayer(playerId);
					player = getWorldPlayerAndCatch(playerId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					player = null;
				}
    			if(player != null){
		    		if(info.getDelayTime() < 0){
		    			//Server.player_Delay.remove(equ.getKey());
		    			Map<IItem, Integer> equMap = info.getEquDiamondTimeMap();
		            	for(Map.Entry<IItem, Integer> equ2: equMap.entrySet()){
		            		IItem item = equ2.getKey();
		            		bossService.resetWorldBossRefresh(item.getItemId());
		            		mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", "下线通知", "你因为下线时间超过1分钟、违反规则而被扣除[" + item.getName() + "],[巨龙夜煞]将重现、再将锦盒赐予人类，你还有机会。" , null, 0, true);
		            		log.info("playerID["+player.getId()+"] worldboss dimond time equ itemd[" + item.getItemId() + "] id [" + item.getId() + "]");
		            	}
		            	Server.delay.add(equ.getKey());
		    		}else{
		    			info.reduceDelayTime(cycleTime);
		    		}
    			}
    			releasePlayer(player);
    		}
    	}
    	
    	for(int i = 0; i < Server.delay.size(); i++){
    		Integer temp = (Integer) Server.delay.get(i);
    		Server.player_Delay.remove(temp);
    	}
    }
    /**
     * 每次存数据时候的存盘量
     */
    public final static short saveRate = 30;
    
    public final static short cycleTime = 2000;
    public void run(){
    	int trick = 0;
        while(true){
        	
        	//每分钟存一次，一次存1/30.默认每次存1分钟
            try {
                Thread.sleep(cycleTime * 1L);
            } catch (InterruptedException ex) {
            }
            
            trick++;
            if(trick != 60){
            	try{
            		checkBossEquip();
            	}catch(Exception bossExe){
            		log.error(bossExe,bossExe);
            	}
            }else{
            	trick = 0;
            	 if((++round)>=saveRate)
                     round = 0;
                 try {
                     savePlayers(round);
                 } catch (Exception ex1) {
                     log.error(ex1,ex1);
                 }
                 try {
                     shopService.save(round);
                 } catch (Exception ex) {
                     log.error(ex,ex);
                 }
                 try {
                     houseModel.save(round);
                 } catch (Exception ex) {
                     log.error(ex, ex);
                 }
                 try {
                     checkBathHouse();
                 } catch (Exception ex2) {
                     log.error(ex2,ex2);
                 }
                 try{
                     checkVipBathHouse();
                 } catch(Exception ex){
                     log.error(ex,ex);
                 }
            }
           
            //mengjie add
//            if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
//            	if ((++cmcc_msg_round)>=5){
//                	if (cmcc_sendmsg_jilin(cmcc_msg_type)){
//                		if (cmcc_msg_type > 1){
//                			cmcc_msg_type = 0;
//                		}
//                		cmcc_msg_type = cmcc_msg_type+1;
//                		cmcc_msg_round = 0;
//                	}
//                }
//            }
        }
    }

    private void checkBathHouse() throws DataAccessException {
        Iterator ite = players.values().iterator();
        long current = System.currentTimeMillis();
        while (ite.hasNext()) {
            WorldPlayer player = (WorldPlayer) ite.next();
            BathHouse bathHouse = BathHouse.getBathHouseByMapId(player.getMapId());
            if (bathHouse != null) {
                synchronized (player) {
                    tryAddBathHouseExp(player, current, bathHouse);
                }
            }
        }
    }

    private void checkVipBathHouse() throws DataAccessException {
        Iterator<VipBathRecorder> ite = vips.values().iterator();
        long current = System.currentTimeMillis();
        while (ite.hasNext()) {
            VipBathRecorder r = ite.next();
            if (r.logoutTime != 0 && (current - r.logoutTime) >= 300 * 1000L) {
                ite.remove();
            } else {
                WorldPlayer player = getWorldPlayer(r.id);
                if(player==null||!player.online()){  //离线
                    if(r.logoutTime==0)
                        r.logoutTime = current;
                }else{
                    r.logoutTime = 0;
                    BathHouse bathHouse = BathHouse.getBathHouseByMapId(player.getMapId());
                    if(bathHouse!=null){
                        if ((current - r.startTime)>=bathHouse.getTime()){
                            r.startTime = current;
                            tryAddVipBathHouseExp(player,current,bathHouse);
                        }
                    }
                }
            }
        }
    }

    private void tryAddVipBathHouseExp(WorldPlayer player,long current, BathHouse bathHouse) throws DataAccessException {
        Changed changed = new Changed();
	      	//mengjie add
	    	if (!player.hasItem(bathHouse.getItemId(), 1)){
	    		//没有澡票，解开澡票
	    		if (bathHouse.getItemId() == 550017)
	    			for(int i = 0; i < bathHouse.VIP_PACKAGE.length; i++){
	            		if(player.hasItem(bathHouse.VIP_PACKAGE[i],1)){
	            			if(player.getAllGridSize() > player.getCurrentGridSize()){
		            			player.completeRemoveItem(bathHouse.VIP_PACKAGE[i], 1, changed);
		            			player.addItem(bathHouse.getItemId(), bathHouse.PACKAGE_COUNT[i], changed, player.getClientDataVersion());
		            			break;
	            			}else{
	            				//背包满
	            				connectService.sendMessage(player.getId(), "你的背包满了，不能自动使用浴场幸运券套装.");
	            				break;
	            			}
	                	}
	            	}
	    		else if (bathHouse.getItemId() == 550015)
	        		for(int i = 0; i < bathHouse.PET_PACKAGE.length; i++){
	            		if(player.hasItem(bathHouse.PET_PACKAGE[i],1)){
	            			if(player.getAllGridSize() > player.getCurrentGridSize()){
		            			player.completeRemoveItem(bathHouse.PET_PACKAGE[i], 1, changed);
		            			player.addItem(bathHouse.getItemId(), bathHouse.PACKAGE_COUNT[i], changed, player.getClientDataVersion());
		            			break;
	            			}else{
	            				//背包满
	            				connectService.sendMessage(player.getId(), "你的背包满了，不能自动使用浴场幸运券套装.");
	            				break;
	            			}
	                	}
	            	}
	    	}
	    	int level_tmp = player.getLevel();
	    	//mengjie add end
            if (player.completeRemoveItem(bathHouse.getItemId(), 1, changed) != null) {
            	log.info("ID[" + player.getId() +
                        "] completeRemoveBathItem[" +bathHouse.getItemId()+"]");
                if (player.getMaxLevel() > player.getLevel()) {
                    int r = 0;
                    Buf buf = player.getBuf(Buf.ADD_BATHHOUSE_EXP);
                    //if (buf != null) {
                    //    r = buf.getValue();
                    //}
                    r += bathHouse.getRatio();
                    //int exp = (BathHouse.VIP_EXP[player.getLevel()] * (100 + r)) / 100;
                    //泡澡经验换算更改
                    int exp = (BathHouse.VIP_EXP[player.getLevel()] * r) / 100;
                    if (buf != null) {
                    	exp = exp * (100+buf.getValue()) / 100;
                    }
                    player.addExp(exp, changed);
                    //mengjie add
                    friendsService.killfriend(player.getId(), player.getLevel());
                    if (level_tmp<player.getLevel()){
                    	//推荐人通用函数
                    	recommendBalance(player, "Bathhouse Online");
                    	//尝试加到师傅的列表中
                    	addMasterPlayer(player, changed);
                    }
                    //mengjie add end
                    HouseData hd;
					try {
						hd = houseModel.getHouseByPlayerId(player.getId());
						if (hd != null && hd.isUsedWaiter()) {
	                        Pet p = player.getPet();
	                        if (p != null) {
	                        	//宠物泡澡经验双倍！
	                            player.tryAddPetExp(BathHouse.PET_EXP[p.getLevel()]*2, changed);
	                            log.info("ID[" + player.getId() +
                                        "] completeRemoveBathItem[" +bathHouse.getItemId()+"]Login add Pet");
	                        }
	                    }else if(hd != null && !hd.isUsedWaiter()){
	                    	//未雇佣管家查看是否正在续费 mengjie add
	                    	if (HouseData.getWaiterProcessing(player.getId()) > -1 ){
	                    		Pet p = player.getPet();
	                            if (p != null) {
	                            	HouseData.modifyProcessing(player.getId(), BathHouse.PET_EXP[p.getLevel()]*2, 1);
	                            }
	                    	}
	                    }
					} catch (Exception e) {
						log.info("ID[" + player.getId() + "][自动管家续费失败-playerservice1]"+e);
					}
                    if(player.getVipBathHouseTime() == null){
                    	player.setVipBathHouseTime(new Date(System.currentTimeMillis() + bathHouse.getTime()));
                    }else{
                    	player.setVipBathHouseTime(new Date(player.getVipBathHouseTime().getTime() + bathHouse.getTime()));
                    }
                }else{
                	//100级泡澡给规定经验:940500
                	player.addExp(940500, changed);
                    //int credit = 20*(100+bathHouse.getRatio())/100;
                    //泡澡经验换算更改
                    int credit = 20*(bathHouse.getRatio())/100;
                  //mengjie add 百级泡澡荣誉双倍
                    credit = credit * 2;
                    //credit = 60;
                    player.addCredit(credit,changed);
                    HouseData hd;
					try {
						hd = houseModel.getHouseByPlayerId(player.getId());
						if (hd != null && hd.isUsedWaiter()) {
	                        Pet p = player.getPet();
	                        if (p != null) {
	                            player.tryAddPetExp(BathHouse.PET_EXP[p.getLevel()]*2, changed);
	                            log.info("ID[" + player.getId() +
                                        "] completeRemoveBathItem[" +bathHouse.getItemId()+"]Login add Pet");
	                        }
	                    }else if(hd != null && !hd.isUsedWaiter()){
                        	//未雇佣管家查看是否正在续费 mengjie add
                        	if (HouseData.getWaiterProcessing(player.getId()) > -1 ){
                        		Pet p = player.getPet();
                                if (p != null) {
                                	HouseData.modifyProcessing(player.getId(), BathHouse.PET_EXP[p.getLevel()]*2, 1);
                                }
                        	}
                        }
					} catch (Exception e) {
						log.info("ID[" + player.getId() + "][百级玩家泡澡给宠物加经验错误]"+e);
					}
					if(player.getVipBathHouseTime() == null){
                    	player.setVipBathHouseTime(new Date(System.currentTimeMillis() + bathHouse.getTime()));
                    }else{
                    	player.setVipBathHouseTime(new Date(player.getVipBathHouseTime().getTime() + bathHouse.getTime()));
                    }
                }
            } else {
                player.setVipBathHouseTime(new Date(current));
            }
            if (player.getLevel() < player.getMaxLevel() && !player.hasItem(bathHouse.getItemId(), 3)) {
            	if (bathHouse.getMsg() != null && !"".equalsIgnoreCase(bathHouse.getMsg())){
            		connectService.sendMessage(player.getId(), bathHouse.getMsg());
            	}
            }
            if (player.online()) {
                connectService.sendGetItem(changed, player.getId(), (byte) 55);
            }

    }

    private void tryAddBathHouseExp(WorldPlayer player, long current, BathHouse bathHouse) throws DataAccessException {
//        if (player.getMaxLevel() > player.getLevel()){
        Date t = player.getBathHouseTime();
        if (t != null && current > t.getTime()) {
            int c = (int) ((current - t.getTime()) / (bathHouse.getTime()));
            Changed changed = new Changed();
            //mengjie add
        	if (!player.hasItem(bathHouse.getItemId(), 1)){
        		//没有澡票，解开澡票
        		if (bathHouse.getItemId() == 550017)
        			for(int i = 0; i < bathHouse.VIP_PACKAGE.length; i++){
	            		if(player.hasItem(bathHouse.VIP_PACKAGE[i],1)){
	            			if(player.getAllGridSize() > player.getCurrentGridSize()){
		            			player.completeRemoveItem(bathHouse.VIP_PACKAGE[i], 1, changed);
		            			player.addItem(bathHouse.getItemId(), bathHouse.PACKAGE_COUNT[i], changed, player.getClientDataVersion());
		            			break;
	            			}else{
	            				//背包满
	            				connectService.sendMessage(player.getId(), "你的背包满了，不能自动使用浴场幸运券套装.");
	            				break;
	            			}
	                	}
	            	}
        		else if (bathHouse.getItemId() == 550015)
	        		for(int i = 0; i < bathHouse.PET_PACKAGE.length; i++){
	            		if(player.hasItem(bathHouse.PET_PACKAGE[i],1)){
	            			if(player.getAllGridSize() > player.getCurrentGridSize()){
		            			player.completeRemoveItem(bathHouse.PET_PACKAGE[i], 1, changed);
		            			player.addItem(bathHouse.getItemId(), bathHouse.PACKAGE_COUNT[i], changed, player.getClientDataVersion());
		            			break;
	            			}else{
	            				//背包满
	            				connectService.sendMessage(player.getId(), "你的背包满了，不能自动使用浴场幸运券套装.");
	            				break;
	            			}
	                	}
	            	}
        	}
        	//mengjie add end
        	int level_tmp = player.getLevel();
            for (int i = 0; i < c; i++) {
                if (player.completeRemoveItem(bathHouse.getItemId(), 1, changed) != null) {
                	log.info("ID[" + player.getId() +
                            "] completeRemoveBathItem[" +bathHouse.getItemId()+"]");
                    if (player.getMaxLevel() > player.getLevel()) {
                        int r = 0;
                        Buf buf = player.getBuf(Buf.ADD_BATHHOUSE_EXP);
                        //if (buf != null) {
                        //    r = buf.getValue();
                        //}
                        r += bathHouse.getRatio();
                        //int exp = (BathHouse.EXP[player.getLevel()] * (100 + r)) / 100;
                        //泡澡经验换算更改
                        int exp = (BathHouse.EXP[player.getLevel()] * r) / 100;
                        if (buf != null) {
                        	exp = exp * (100+buf.getValue()) / 100;
                        }
                        long getExp = 1L * Discount.BATH_EXP_PERCENT * exp;
                        player.addExp((int)(getExp / 100), changed);
                        //mengjie add
                        friendsService.killfriend(player.getId(), player.getLevel());
                        if (level_tmp<player.getLevel()){
                        	//推荐人通用函数
                        	recommendBalance(player, "Bathhouse Online");
                        	//尝试加到师傅的列表中
                        	addMasterPlayer(player, changed);
                        }
                        //mengjie add end
                        HouseData hd;
						try {
							hd = houseModel.getHouseByPlayerId(player.getId());
							if (hd != null && hd.isUsedWaiter()) {
	                            Pet p = player.getPet();
	                            if (p != null) {
	                            	//宠物泡澡经验双倍！
	                                player.tryAddPetExp(Discount.BATH_EXP_PERCENT * BathHouse.PET_EXP[p.getLevel()] * 2 / 100, changed);
	                                log.info("ID[" + player.getId() +
	                                        "] completeRemoveBathItem[" +bathHouse.getItemId()+"]Login add Pet");
	                            }
	                        }else if(hd != null && !hd.isUsedWaiter()){
	                        	//未雇佣管家查看是否正在续费 mengjie add
	                        	if (HouseData.getWaiterProcessing(player.getId()) > -1 ){
	                        		Pet p = player.getPet();
	                                if (p != null) {
	                                	HouseData.modifyProcessing(player.getId(), Discount.BATH_EXP_PERCENT * BathHouse.PET_EXP[p.getLevel()] * 2 / 100, 1);
	                                }
	                        	}
	                        }
						} catch (Exception e) {
							log.info("ID[" + player.getId() + "][自动管家续费失败-playerservice2]"+e);
						}
                        player.setBathHouseTime(new Date(player.getBathHouseTime().getTime() +
                                bathHouse.getTime()));
                    } else {
                    	//100级泡澡给规定经验:940500
                    	player.addExp(940500, changed);
                        //int credit = 10*(100+bathHouse.getRatio())/100;
                        //泡澡经验换算更改
                    	int credit = 10*(bathHouse.getRatio())/100;
                    	//mengjie add 百级泡澡荣誉双倍
                    	credit = credit * 2;
                    	//credit = 60;
                        player.addCredit(Discount.BATH_CREDIT_PERCENT * credit / 100,changed);
                        HouseData hd;
    					try {
    						hd = houseModel.getHouseByPlayerId(player.getId());
    						if (hd != null && hd.isUsedWaiter()) {
    	                        Pet p = player.getPet();
    	                        if (p != null) {
    	                            player.tryAddPetExp(Discount.BATH_EXP_PERCENT * BathHouse.PET_EXP[p.getLevel()] * 2 / 100, changed);
    	                            log.info("ID[" + player.getId() +
                                            "] completeRemoveBathItem[" +bathHouse.getItemId()+"]Login add Pet");
    	                        }
    	                    }else if(hd != null && !hd.isUsedWaiter()){
	                        	//未雇佣管家查看是否正在续费 mengjie add
	                        	if (HouseData.getWaiterProcessing(player.getId()) > -1 ){
	                        		Pet p = player.getPet();
	                                if (p != null) {
	                                	HouseData.modifyProcessing(player.getId(), Discount.BATH_EXP_PERCENT * BathHouse.PET_EXP[p.getLevel()] * 2 / 100, 1);
	                                }
	                        	}
	                        }
    					} catch (Exception e) {
    						log.info("ID[" + player.getId() + "][百级玩家泡澡给宠物加经验错误]"+e);
    					}
                        player.setBathHouseTime(new Date(player.getBathHouseTime().getTime() + bathHouse.getTime()));
                    }
                } else {
                    player.setBathHouseTime(new Date(current));
                    break;
                }

            }
            if (player.getLevel() < player.getMaxLevel() && !player.hasItem(bathHouse.getItemId(), 3)) {
            	if (bathHouse.getMsg()!=null && !"".equalsIgnoreCase(bathHouse.getMsg())){
            		connectService.sendMessage(player.getId(), bathHouse.getMsg());
            	}
            }
            if (player.online())
                connectService.sendGetItem(changed, player.getId(), (byte) 55);
        }
//        }
//        else{
//            player.setBathHouseTime(new Date(current));
//        }
    }

    public void addTongBathHouseExp(int tongId,BathHouse bh){
        Iterator ite = players.values().iterator();
        while (ite.hasNext()) {
            WorldPlayer player = (WorldPlayer) ite.next();
            if(player.getTongId()==tongId&&player.getMaxLevel()>player.getLevel()&&player.online()&&player.getMapId()==bh.getMapId()){
                Changed changed = new Changed();
                int r = 0;
                Buf buf = player.getBuf(Buf.ADD_BATHHOUSE_EXP);
                //if (buf != null) {
                //    r = buf.getValue();
                //}
                r += bh.getRatio();
                //int exp = (BathHouse.EXP[player.getLevel()] * (100 + r)) / 100;
                //泡澡经验换算更改
                int exp = (BathHouse.EXP[player.getLevel()] * r) / 100;
                if (buf != null) {
                	exp = exp * (100+buf.getValue()) / 100;
                }
                int level_tmp = player.getLevel();
                player.addExp(exp, changed);
                if (level_tmp<player.getLevel()){
                	//推荐人通用函数
                	recommendBalance(player, "Question");
                	//尝试加到师傅的列表中
                	addMasterPlayer(player, changed);
                }
                connectService.sendGetItem(changed, player.getId(), (byte) 55);
            }
        }
    }

    private void savePlayers(int round){
        Iterator ite = players.values().iterator();
        while(ite.hasNext()){
            WorldPlayer player = (WorldPlayer)ite.next();
            if(player.getId()%saveRate==round)
            synchronized(player){
                player.reset();
                savePlayer(player);
//                log.info("ID["+player.getId()+"]Saved in "+round);
            }
        }
        log.info("Saved Round["+round+"]");
    }

    public String getCreditOrderString(int count,WorldPlayer player){
        try {
            PlayerCreditWapper[] ps = dao.getPlayerByCredit(count);
            StringBuilder sb = new StringBuilder(500);
            for(int i=0;i<ps.length;i++){
                sb.append(i+1);
                sb.append(". ");
                sb.append(ps[i].name);
                sb.append(" ");
                sb.append(ps[i].credit);
                sb.append("\n");
            }
            return sb.toString();
        } catch (DataAccessException ex) {
            return "查询错误";
        }
    }

    public PlayerCreditWapper[] getPlayerByCredit(int count,WorldPlayer player){
        try {
            PlayerCreditWapper[] ps = dao.getPlayerByCredit(count);
            return ps;
        } catch (DataAccessException ex) {
            return new PlayerCreditWapper[0];
        }
    }

    public Player createPlayer(Client client, String playerName,byte sex,int version) throws CreatePlayerException{
        try{
            String name = playerName.trim();
            if(name.length()==0)
                throw new CreatePlayerException("角色名不能为空");
            if(name.getBytes("GBK").length > 16)
                throw new CreatePlayerException("角色名太长");
            if(KeywordsUtil.isInvalidName(name.toLowerCase()))
                throw new CreatePlayerException("角色名出现非法字符");
            if(!Utils.checkString(name,false))
                throw new CreatePlayerException("角色名出现非法字符");
            String newName = KeywordsUtil.filterKeywords(name);
            if(!newName.equals(name))
                throw new CreatePlayerException("角色名出现非法字符");
            Player player = dao.getPlayerByName(name);
            if(player != null){
                throw new CreatePlayerException("存在同名角色");
            }else{
            	byte[] option = Server.instance.modelService.getModelConfig(client.getJvmCode()).toBytes();
                Player newPlayer = createDefaultPlayer(client, name, sex,0,40,option);
                dao.addPlayer(newPlayer);
                return newPlayer;
            }
        }catch(CreatePlayerException e){
            throw e;
        }catch(Exception e){
            log.error(e,e);
            throw new CreatePlayerException("创建角色失败");
        }
    }

//    private String QUICKNAME_PREFIX = "guest";
    private static final byte[] CHANGE_SEX_ITEM_BYTES = new byte[]{0,1,0,3,0x34,0x64,1}; //变性药水
//    private static final byte[] DEFAULT_OPTION = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0};//默认的配置
    // private static final byte[] MOTO_OPTION = new byte[]{0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//000000001000moto机器的配置
    // private static final byte[] N402_OPTION = new byte[]{0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//000000001000moto机器的配置

    protected String cutJvmCode(String value) {
    	if (value == null) {
    		return "Midp2";
    	}
        int pos = value.indexOf('/');
        return value.substring(pos + 1);
    }
    
    public Player quickCreatePlayer(Client client,String name,byte sex,int serial) throws CreatePlayerException{
        try {
            byte[] option = Server.instance.modelService.getModelConfig(client.getJvmCode()).toBytes();
            Player newPlayer = createDefaultPlayer(client, name, sex,1,40,option);
            newPlayer.setMetaItems(getMetaItems(client.channel));
//            newPlayer.setMetaItems(getQuickRegMetaItems());
            dao.addPlayer(newPlayer);
            return newPlayer;
        } catch (DataAccessException ex) {
            throw new CreatePlayerException("创建角色失败");
        }
    }



    public Player createDefaultPlayer(Client client, String playerName, byte sex,int modifyNameTimes,int packagesize,byte[] option){
        Player player = new Player();
        player.setAccountId(client.accountId);
        player.setPlayerName(playerName);
        player.setSex(sex);
        player.setFace(sex);
        //初始化的时候 默认为黑暗阵营
        player.setCamp((byte)1);
        player.setFace((byte)31);
        //出生点
        if(client.getDataVersion() > 0){
        	player.setMapId((short)113);
            player.setX((short)(7*16));
            player.setY((short)(31*8));
        }
//        else 
//        if (client.getDataVersion() > 0) {
//        	player.setMapId((short)6401);
//            player.setX((short)(9*16));
//            player.setY((short)(14*8));
//        }
    	else{
        	player.setMapId((short)3697);
            player.setX((short)(10*16));
            player.setY((short)(13*8));
        }
//        player.setMapId((short)1569);
//        player.setX((short)(12*16));
//        player.setY((short)(21*8));
        player.setExp(0);
        player.setLevel(1);
        player.setMoeny(0);
        player.setReturnTimes((byte)0);
        player.setCreateTime(new Date());
        player.setLastLoginTime(new Date());
        player.setHouseLevel(1);
        player.setTongDuty(-1);
        player.setTongId(-1);
        player.setTongName("");
        player.setTongTitle("");
        player.setCredit(0);
        player.setData(new byte[0]);
        player.setStrength(1);
        player.setAgility(1);
        player.setVitality(1);
        player.setIntelligence(1);
        player.setLuck(1);
        player.setAbilityTimes(1);
        int []magicpositionHpOrMp = MagicPosMessage.getMagicPosAttr(PlayerData.mindMagicPos, MagicPosMessage.defaultMinLevel, MagicPosMessage.defaultMinFloor);
        player.setHp(Utils.calculateMaxHp(player.getVitality(),player.getAgility(),player.getStrength(),player.getIntelligence(), 1, magicpositionHpOrMp[0]));
        player.setMp(Utils.calculateMaxMp(player.getVitality(),player.getAgility(),player.getStrength(),player.getIntelligence(), 1, magicpositionHpOrMp[1]));
        player.setLeavePoints(0);
        player.setAbilities(new byte[]{
                        0, 0, 0, 0
        });
        player.setTechSkills(new byte[0]);
        
    	player.setBasicItems(getBasicItems());
    	player.setMetaItems(getMetaItems(client.channel));
        player.setEquipments(getEquipments());
        player.setUsedEquipments(getUsedEquipments());
        player.setTaskItems(getTaskItems());
        if(option==null)
            player.setOptions(new byte[0]);
        else
            player.setOptions(option);
        //mengjie add
        player.setKey9_options(null);
        
        player.setPets(new byte[0]);
        player.setModifyNameTimes(modifyNameTimes);
        TaskData taskData = new TaskData();
        taskData.setCurrent(new byte[0]);
        taskData.setFinished(new byte[0]);
        taskData.setSaveData(new byte[0]);
        player.setTaskData(taskData);
        //player.setGridSize((short)12);
        player.setGridSize((short)packagesize);//包格数量
        player.setValid(true);
        player.setMessageCount(0);
        player.setLastMessageTime(new Date());
        player.setTitle("");
        player.setJumpMapId((short)0);
        player.setJumpX((short)0);
        player.setJumpY((short)0);
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMddHHmmss");
        String str="19000101000000";
        ParsePosition pos = new ParsePosition(0);
        Date dt=formatter.parse(str,pos);
        player.setTonginTime(dt);
        taskData.setPlayer(player);
        player.setArenaV1Id(-1);
        player.setArenaV2Id(-1);
        player.setArenaV3Id(-1);
        player.setEndVoteTime(dt);
        player.setPlayerPool("");
        PropertyPool pp = new PropertyPool();
        pp.setLong(PlayerData.OTHER_POOL_LIFEVALUE, UnlineExpConfig.LIFEVALUE_MAX);
        pp.setString(PlayerData.OTHER_POOL_UNLINEDATE, format.format(new Date()));
        //给20小时的当前等级的离线经验值
        int unlineLevelExp = 20 * UnlineExpConfig.getLevelExp(player.getLevel());
        pp.setInt(PlayerData.OTHER_POOL_UNLINEEXP, unlineLevelExp);
        player.setOtherPool(pp);
        return player;
    }
    private byte[] getBasicItems(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
//        特效急救生命药水*1（ID:111）	2011年7月18日 17:32:13 修改成3000点生命药水(ID:117)
        try {
    		dos.writeShort(1);
            dos.writeInt(117);
            dos.write(1);
        } catch (IOException ex) {
        }
        return bos.toByteArray();
    }

    private byte[] getMetaItems(String channel){
        //        爱之门*1（ID:200126）
//传送门*1（ID:200125）
//宠物经验翻倍果*1（ID:210021）
//幸运时魔球*1（ID:200127）、改成了210032（双倍经验果）
//增力药剂*1（ID:200130）
//20090218 add 10级宝盒（ID:211071）
//20101102 活动 礼包套盒（ID:211092）
    	// 变性药水(ID:210020)
    	// 指路宝典(ID:200176)
    	//20110303 去除 10级宝盒 指路宝典
    	//2011年6月30日 14:31:40 添加掌上明珠8周年注册大礼包 ID:201350
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
        	int addCount = 0;
        	boolean addChannelItem = false;
        	ChannelGiftData giftData = WelcomeMessage.getChannelGift(channel);
        	long now = System.currentTimeMillis();
        	if(giftData != null && giftData.getStartTime() <= now && giftData.getEndTime() >= now){
        		addCount += giftData.getItemCount();
        		addChannelItem = true;
        	}
        	if(Server.iMoneyType == Server.IMONEY_TYPE_PIP){
        		dos.writeShort(6 + addCount);
        	}else{
        		dos.writeShort(5 + addCount);
        	}
//            dos.writeInt(200176);
//            dos.write(1);
            dos.writeInt(200125);
            dos.write(1);
            dos.writeInt(210021);
            dos.write(1);
            dos.writeInt(210032);
            dos.write(1);
            dos.writeInt(200130);
            dos.write(1);
            //mengjie add
//            dos.writeInt(211071);
//            dos.write(1);    
            dos.writeInt(210020);
            dos.write(1); 
//            dos.writeInt(201350);
//            dos.write(1); 
            if(Server.iMoneyType == Server.IMONEY_TYPE_PIP){
            	dos.writeInt(211092);
                dos.write(1);
        	}
            if(addChannelItem){
            	int itemids[] = giftData.getItemId();
            	for(int i=0; i<itemids.length; i++){
	            	dos.writeInt(itemids[i]);
	            	dos.write(1);
            	}
            }
        } catch (IOException ex) {
        }
        return bos.toByteArray();
    }

    private byte[] getQuickRegMetaItems(int type){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
        	switch (type) {
        	case 0://普通
        		if(Server.iMoneyType == Server.IMONEY_TYPE_PIP){
        			dos.writeShort(6); 
            	} else {
            		dos.writeShort(5);
            	}        		
//                dos.writeInt(200176);
//                dos.write(1);
                dos.writeInt(200125);
                dos.write(1);
                dos.writeInt(210021);
                dos.write(1);
                dos.writeInt(210032);
                dos.write(1);
                dos.writeInt(200130);
                dos.write(1);
                //mengjie add
//                dos.writeInt(211071);
//                dos.write(1);
                dos.writeInt(210020);
                dos.write(1); 
//                dos.writeInt(201350);
//                dos.write(1); 
                if(Server.iMoneyType == Server.IMONEY_TYPE_PIP){
                	dos.writeInt(211092);
                    dos.write(1);
            	} 
        		break;
        	case 1://吉林
        		if(Server.iMoneyType == Server.IMONEY_TYPE_PIP){
        			dos.writeShort(6); 
            	} else {
            		dos.writeShort(5);
            	}  
//                dos.writeInt(200176);
//                dos.write(1);
                dos.writeInt(200125);
                dos.write(1);
                dos.writeInt(210021);
                dos.write(1);
                dos.writeInt(210032);
                dos.write(1);
                dos.writeInt(200130);
                dos.write(1);
                //mengjie add
//                dos.writeInt(211071);
//                dos.write(1);
                dos.writeInt(210020);
                dos.write(1);
//                dos.writeInt(201350);
//                dos.write(1); 
                if(Server.iMoneyType == Server.IMONEY_TYPE_PIP){
                	dos.writeInt(211092);
                    dos.write(1);
            	} 
        		break;
        	case 2://温州
            	dos.writeShort(1);
                dos.writeInt(200379);
                dos.write(1);
                break;
        	}       
        } catch (IOException ex) {
        }
        return bos.toByteArray();
    }

    private byte[] getEquipments(){
        return new byte[0];
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        DataOutputStream dos = new DataOutputStream(bos);
//        try{
//            dos.writeShort(1);
//            dos.writeInt(3); //item id
//            dos.writeInt(1); //id
//            dos.writeUTF("放在包里的大斧子"); //name
//            dos.writeByte(1); //级别
//            dos.writeByte(1); //需要级别
//            dos.writeByte(1); //装备级别
//            dos.writeByte(7); //装备部位
//            dos.writeShort(100); //耐久度
//            dos.writeShort(100); //剩余耐久
//            dos.writeInt(5000); //price
//            dos.writeByte(0); //bind
//            dos.writeByte(0); //打造次数
//
//            dos.writeByte(16); //附加属性数量
//
//            for(int i = 1; i <= 12; i++){
//                dos.writeByte(i); //属性类型
//                dos.writeShort(100); //附加值
//            }
//
//            dos.writeByte(20);
//            dos.writeShort(1000);
//            dos.writeByte(21);
//            dos.writeShort(2000);
//            dos.writeByte(22);
//            dos.writeShort(1000);
//            dos.writeByte(30);
//            dos.writeShort(1);
//        }catch(IOException ex){
//
//        }
//        return bos.toByteArray();
    }

    private byte[] getUsedEquipments(){
        return new byte[0];
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        DataOutputStream dos = new DataOutputStream(bos);
//        try{
//            dos.writeShort(1);
//            dos.writeInt(3); //item id
//            dos.writeInt(2); //id
//            dos.writeUTF("大斧子"); //name
//            dos.writeByte(1); //级别
//            dos.writeByte(1); //需要级别
//            dos.writeByte(1); //装备级别
//            dos.writeByte(7); //装备部位
//            dos.writeShort(100); //耐久度
//            dos.writeShort(100); //剩余耐久
//            dos.writeInt(5000); //price
//            dos.writeByte(0); //bind
//            dos.writeByte(0); //打造次数
//
//            dos.writeByte(16); //附加属性数量
//
//            for(int i = 1; i <= 12; i++){
//                dos.writeByte(i); //属性类型
//                dos.writeShort(100); //附加值
//            }
//
//            dos.writeByte(20);
//            dos.writeShort(1000);
//            dos.writeByte(21);
//            dos.writeShort(2000);
//            dos.writeByte(22);
//            dos.writeShort(1000);
//            dos.writeByte(30);
//            dos.writeShort(1);
//        }catch(IOException ex){
//
//        }
//        return bos.toByteArray();
    }

    private byte[] getTaskItems(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        return bos.toByteArray();
    }


    public void addVipBath(WorldPlayer player){
        VipBathRecorder r = new VipBathRecorder();
        r.id = player.getId();
        r.startTime = System.currentTimeMillis();
        vips.put(r.id,r);
        player.setVipBathHouseTime(new Date());
    }

    public void removeVipBath(WorldPlayer player){
        vips.remove(player.getId());
    }

    public boolean containsVipBath(WorldPlayer player){
        return vips.containsKey(player.getId());
    }

    //mengjie add
    private boolean cmcc_sendmsg_jilin(int type){
    	Iterator ite = players.values().iterator();
    	int count = 0;
        while(ite.hasNext()){
            WorldPlayer player = (WorldPlayer)ite.next();
            if (!"".equals(player.cityname)){
            	if (type == 1){
            		chatService.sendPrivateMessage(-1,"系统",player.getId(),
    				Server.Cmcc_msg1);
            	}else if (type == 2){
            		chatService.sendPrivateMessage(-1,"系统",player.getId(),
            		Server.Cmcc_msg2);
            	}
            	count++;
            }
        }
        log.info("cmcc_sendmsg count["+count+"] type["+type+"]");
        return true;
    }
    public String getPlayerName(int playerId) throws DataAccessException{
    	return dao.getPlayerName(playerId);
    }
    
    public void resetPlayerName(String oldplayername , String newplayername) throws DataAccessException{
    	WorldPlayer player = (WorldPlayer) names.get(oldplayername);
    	names.remove(oldplayername);
    	player.setPlayerName(newplayername);
    	names.put(newplayername, player);
    }
    
    public void recommendBalance(WorldPlayer player, String logEnd){
    	//edit zxyu 2012年6月29日11:17:14 去掉推荐人系统
//    	if (Server.iMoneyType == Server.IMONEY_TYPE_PIP){
//	    	int level = player.getLevel();
//	    	if((level==50)||(level==65)||(level==80)||(level==90)||(level==100)){
//	    	    try {
//					LevelUpNotifyMessage msg = new LevelUpNotifyMessage(player.getAccountId(), player.getId(), level, Server.getGameCode());
//					accountSkeleton.send(msg);
//
//					Utils.log(log, player.getId(), -1,
//					        "RecommendBalance--accountid[" + player.getAccountId() + "]--" +
//					        		"accountname[" + player.getAccountName() + "]" +
//					        				"LEVEL["+player.getLevel()+"]" + logEnd);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//	        }
//    	}
    }
    
    public void toDbPetPracticeTime (WorldPlayer player) throws BuyException {
    	// 获得上一次宠物的修炼时间
    	Petmanager[] petmanager = petmanagerService.getPets(player.getId());
    	if (petmanager != null) {
    		long playerLoginTime = player.getLastLoginTime().getTime();
    		long practiceTime;
    		for (int i = 0; i < petmanager.length; i ++) {
				long petBeganPracticeTime = petmanager[i].getEattime().getTime();
				if (petBeganPracticeTime >= playerLoginTime) {
					practiceTime = (new Date ()).getTime() - petBeganPracticeTime;
				} else {
					practiceTime = petmanager[i].getPracticeTime();
					long playerGameTime = (new Date ()).getTime() - playerLoginTime;
					practiceTime += playerGameTime;
				}
    			petmanager[i].setPracticeTime(practiceTime);
    			//存档
    			petmanagerService.updateTime(petmanager[i].getId(), practiceTime);
    		}
    	}
    }
}


class VipBathRecorder{
    int id;
    long logoutTime = 0;
    long startTime = 0;
}

class PlayerForbiden {
    int id;
    long validTime;
    public PlayerForbiden(int id, long validTime) {
        this.id = id;
        this.validTime = validTime;
    }
}
