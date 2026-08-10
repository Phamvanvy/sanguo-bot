package com.pip.itimes.server.world.camp;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Camp;
import com.pip.itimes.server.bean.CampCandidate;
import com.pip.itimes.server.camp.CampConfig;
import com.pip.itimes.server.camp.CampData;
import com.pip.itimes.server.camp.CampOfficial;
import com.pip.itimes.server.camp.CampSilence;
import com.pip.itimes.server.camp.CampSkill;
import com.pip.itimes.server.camp.CampSkillData;
import com.pip.itimes.server.camp.CampSkillLevel;
import com.pip.itimes.server.dao.CampDao;
import com.pip.itimes.server.dao.PlayerDao;
import com.pip.itimes.server.stage.Buf;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Effect;
import com.pip.itimes.server.stage.IEffectItem;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.IShopTimeItem;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.PropertyEffect;
import com.pip.itimes.server.stage.RoleFaceData;
import com.pip.itimes.server.stage.RoleFaces;
import com.pip.itimes.server.stage.ShoutConfig;
import com.pip.itimes.server.stage.TwelfthLunarConfig;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.ChristmasProcessor;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.ConnectSession;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.TwelfthLunarService;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.ItemGroup.BossBattleTop;
import com.pip.itimes.server.world.ItemGroup.ItemConstants;
import com.pip.itimes.server.world.ItemGroup.ItemGroup;
import com.pip.itimes.server.world.ItemGroup.ItemTop;
import com.pip.itimes.server.world.ItemGroup.MagicPositionTop;
import com.pip.itimes.server.world.ItemGroup.PetDevelopTop;
import com.pip.itimes.server.world.ItemGroup.TrainLevelTop;
import com.pip.itimes.server.world.book.BookConfig;
import com.pip.itimes.server.world.chr.ChristmasConfig;
import com.pip.itimes.server.world.lyrics.LoveLyricsConfig;
import com.pip.itimes.server.world.lyrics.LyricsConfig;
import com.pip.itimes.server.world.noahsark.NoahsarkConfig;
import com.pip.itimes.server.world.rabbitRace.RabbitRaceConfig;
import com.pip.itimes.server.world.riddles.RiddlesConfig;
import com.pip.itimes.server.world.riddles.RiddlesConfig2;
import com.pip.itimes.server.world.top.GemTop;
import com.pip.itimes.server.world.worldboss.WorldBossConfig;

public class CampMainService implements Runnable{
    private static final Logger log = Logger.getLogger(CampMainService.class);

    private HashMap<Integer, CampJob> jobs = new HashMap<Integer, CampJob>();
    private HashMap<Integer, CampJob> needAddJobs = new HashMap<Integer, CampJob>();
    private CampAuctionService campAuctionService;
    private CampVoteService campVoteService;
    private TrainLevelTop trainleveltop;
    
    private ConnectService connectService;
    private PlayerService playerService;
    private ChatService chatService;
    private MailService mailService;

    private AtomicInteger bufId = new AtomicInteger(0);

    private CampDao campDao = new CampDao();
    private CampData darkCamp = null;
    private CampData lightCamp = null;
    private int darkPlayerCount;
    private int lightPlayerCount;

    private long lastSaveTime = Utils.getTodayStart();
    private long lastClearTime = Utils.getTodayStart();
    private long lastSaveIronChefXml = Utils.getTodayStart();

    public static final int CAMP_NONE = -1;
    public static final int CAMP_NO = 0;
    public static final int CAMP_DARK = 1;
    public static final int CAMP_LIGHT = 2;

    public static final int VOTE_ITEM_ID = 200455;
    public static final int VOTE_ISHOP_ITEM_ID = 200335;
    public static final int VOTE_EGG_ITEM_ID = 200928;

    public static final int VOTE_COUNT_MAGIC = 6;
    public static final int VOTE_COUNT_ISHOP_ITEM = 50;
    public static final int VOTE_COUNT_NORMAL = 5;
    public static final int VOTE_COUNT_ITEM = 1;
    public static final int VOTE_COUNT_EGG = 1;
    
    // 禁言的单位时间
    public static final long UNIT_TIME = 1000L * 60L * 60L;

    public CampMainService(ConnectService connectService, PlayerService playerService, 
    		ChatService chatService,MailService mailService) throws Exception{
        this.connectService = connectService;
        this.playerService = playerService;
        this.chatService = chatService;
        this.mailService = mailService;
        campAuctionService = new CampAuctionService(this, playerService, connectService);
        loadCampData();
        campVoteService = new CampVoteService(this,this.mailService);
        trainleveltop = new TrainLevelTop(this.mailService);
        new Thread(this).start();
    }

    /**
     * 添加任务
     * @param job
     */
    public void addJob(CampJob job){
        synchronized(jobs){
        	jobs.put(job.getId(), job);
        }
    }

    /**
     * 移除任务
     * @param job
     */
    public void removeJob(CampJob job){
        synchronized(jobs){
            jobs.remove(job.getId());
        }
    }

    /**
     * 取得某一阵营的领袖id
     * @param camp
     * @return
     */
    public int getKingId(int camp){
        switch(camp){
            case CAMP_DARK:
                return darkCamp.getKingId();
            case CAMP_LIGHT:
                return lightCamp.getKingId();
        }

        return -1;
    }

    /**
     * 根据金额计算应缴的税款
     * @param money 金额
     * @param camp 阵营
     * @return
     */
    public int getTax(int money, int camp){
        CampData campData = getCampData(camp);
        int taxRate = CampConfig.taxNoCamp;
        if(campData != null){
        	taxRate = campData.getTaxRate();
        }
        long tax = (long)money * taxRate / 100;
        return (int)tax;
    }

    /**
     * 添加阵营金库
     * @param money
     */
    public void addCampMoney(int camp, int money){
        CampData campData = getCampData(camp);

        if(campData != null){
            campData.addMoney(money);
        }
    }

    /**
     * 发送世界聊
     * @param message
     * @param camp
     */
    public void sendWorldMessage(String message, int camp) throws Exception{
        chatService.sendWorldMessage(-1, "系统", replayMessage(message, camp));
    }

    /**
     * 发送私聊
     * @param message
     * @param camp
     * @param playerId
     * @throws Exception
     */
    public void sendPrivateMessage(String message, int camp, int playerId) throws Exception{
        chatService.sendPrivateMessage(-1, "系统", playerId, replayMessage(message, camp));
    }

    /**
     * 发送阵营聊
     * @param message
     * @param camp
     * @throws Exception
     */
    public void sendCampMessage(String message, int camp) throws Exception{
        chatService.sendCampMessage(-1, "系统", replayMessage(message, camp), camp);
    }

    /**
     * 发送狮子吼
     * @param message
     * @param camp
     * @throws Exception
     */
    public void sendRoarMessage(String message, int camp) throws Exception{
        chatService.sendRoarMessage(-1, "狮子吼", replayMessage(message, camp), true, 10, 50, Utils.CLR_RED_ROAR, Utils.WORLD, (short) 0);
    }

    /**
     * 获取竞拍服务
     * @return
     */
    public CampAuctionService getCampAuctionService(){
        return campAuctionService;
    }

    public CampVoteService getCampVoteService(){
        return campVoteService;
    }

    /**
     * 处理新的阵营领袖
     * @param campCandidate
     */
    public void processNewKing(CampCandidate campCandidate) throws Exception{
        switch(campCandidate.getCamp()){
            case CAMP_DARK:
                CampData newDarkCamp = darkCamp.copyAndCreate();
                newDarkCamp.setKingId(campCandidate.getPlayerid());
                darkCamp.setValid(false);
                darkCamp.save();
                campDao.makePersistent(darkCamp.getCamp());

                if(darkCamp.getKingId() >= 0){
                    removeFaceOldKing(darkCamp.getKingId());
                }

                addFaceNewKing(campCandidate.getPlayerid());

                darkCamp = newDarkCamp;

                break;
            case CAMP_LIGHT:
                CampData newLightCamp = lightCamp.copyAndCreate();
                newLightCamp.setKingId(campCandidate.getPlayerid());
                lightCamp.setValid(false);
                lightCamp.save();
                campDao.makePersistent(lightCamp.getCamp());

                if(lightCamp.getKingId() >= 0){
                    removeFaceOldKing(lightCamp.getKingId());
                }

                addFaceNewKing(campCandidate.getPlayerid());

                lightCamp = newLightCamp;

                break;
        }
    }

    /**
     * 取得阵营数据
     * @param camp
     * @return
     */
    public CampData getCampData(int camp){
        switch(camp){
            case CAMP_DARK:
                return darkCamp;
            case CAMP_LIGHT:
                return lightCamp;
        }

        return null;
    }

    /**
     * 测试是否可以执行阵营科技升级
     * @param camp
     * @param effect
     * @return
     */
    public boolean testCanUpgradeSkill(int camp, int effect){
        CampData campData = getCampData(camp);
        List<CampSkillData> skillData = campData.getSkillDataList();
        long now = System.currentTimeMillis();
        int lastUpgradeSkillEffect = -1;
        boolean found = false;

        for(CampSkillData tmp : skillData){
            //找到24小时内升级过的科技
            if(now - tmp.getLastUpgradeTime() <= (24 * 3600 * 1000L)){
                lastUpgradeSkillEffect = tmp.getEffect();
                found = true;

                break;
            }
        }

        if(found){
            if(lastUpgradeSkillEffect == effect){
                return true;
            }else{
                return false;
            }
        }else{
            return true;
        }
    }

    /**
     * 测试是否可以执行阵营科技维护
     * @param camp
     * @param effect
     * @return
     */
    public boolean testCanMaintSkill(int camp, int effect){
        CampData campData = getCampData(camp);
        CampSkillData skillData = campData.getSkillData(effect);

        if((System.currentTimeMillis() - skillData.getLastMaintTime()) <= (24 * 3600 * 1000L)){
            return false;
        }else{
            return true;
        }
    }

    /**
     * 测试是否可以设置税率
     * @param camp
     * @return
     */
    public boolean testCanSetTaxRate(int camp){
        CampData campData = getCampData(camp);

        long todayStart = Utils.getTodayStart();

        if(todayStart > campData.getLastSetTaxRateTime()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 测试是否可以加入阵营（人数平衡）
     * @param camp
     * @return
     */
    public boolean testCanAddCamp(int camp){
        boolean result = false;

        switch(camp){
            case CAMP_DARK:
                if(darkPlayerCount < lightPlayerCount * 2){
                    result = true;
                }

                break;
            case CAMP_LIGHT:
                if(lightPlayerCount < darkPlayerCount * 2){
                    result = true;
                }

                break;
        }

        return result;
    }
    
    public int getMinCampType(){
    	if(darkPlayerCount < lightPlayerCount){
    		return CAMP_DARK;
    	}
    	return CAMP_LIGHT;
    }

    /**
     * 刷新阵营人数数据
     * @param camp
     */
    public void refreshCampPlayerCount(int camp){
        switch(camp){
            case CAMP_DARK:
                darkPlayerCount++;

                break;
            case CAMP_LIGHT:
                lightPlayerCount++;

                break;
        }
    }
    
    /**
     * 清除官员数据 换界的时候执行
     */
    public void clearOfficial(){
    	if(darkCamp != null){
    		clearOfficial(darkCamp);
    		darkCamp.clearOfficial();
    	}
    	if(lightCamp != null){
    		clearOfficial(lightCamp);
    		lightCamp.clearOfficial();
    	}
    }
    
    private void clearOfficial(CampData campData){
    	Iterator iter = campData.getOfficial().values().iterator();
		while(iter.hasNext()){
			CampOfficial official = (CampOfficial)iter.next();
//			WorldPlayer player = playerService.getWorldPlayer(official.getPlayerID());
//			boolean load = false;
//			if(player == null){
//				try{
//					player = playerService.loadWorldPlayer(official.getPlayerID());
//				}catch(Exception e){
//					log.error(e, e);
//				}
//				if(player == null){
//					continue;
//				}
//				load = true;
//			}
			WorldPlayer player = playerService.getWorldPlayerAndCatch(official.getPlayerID());
			if(player == null){
				continue;
			}
			String title = (player.getCamp() == 1 ? "黑暗" : "光明") + official.getPostName();
			synchronized (player) {
    			player.setTitle("");
    			player.removeRoleTitle(title);
//    			if(!load){
    			if(player.online()){
    				Changed changed = new Changed();
    				changed.setProperty(Changed.TITLE_STRING, "");
    				connectService.sendGetItem(changed, official.getPlayerID(), (byte)4);
    				UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
					seg.writeShort(ClientConstants.EXTEND_CAMPOFFICIAL);
					seg.write((byte)2);
					connectService.writeTo(seg, player.getId());
    			}
//    			if(load){
//    				playerService.savePlayer(player);
//    			}
			}
			playerService.releasePlayer(player);
			iter.remove();
		}
    }

    /**
     * 保存阵营数据
     */
    public void saveCamp() throws Exception{
    	//添加每次保存数据的时候 进行保存所捐的物资
    	saveTotal();
    	
        darkCamp.save();
        lightCamp.save();

        campDao.makePersistent(darkCamp.getCamp());
        campDao.makePersistent(lightCamp.getCamp());
    }
    
    /**
     * 保存所捐的物资
     * @throws Exception
     */
    public void saveTotal() throws Exception{
    	ChristmasProcessor.saveTotal();
    	//保存的时候 需要重新设置两个阵营的数据 同步一下
    	darkCamp.setChrItemTotal(ChristmasProcessor.getChrItemTotal(Utils.CAMP_DARK));
    	lightCamp.setChrItemTotal(ChristmasProcessor.getChrItemTotal(Utils.CAMP_BRIGHT));
    }

    /**
     * 阵营科技的buf添加
     * @param player
     */
    public void addBuf(WorldPlayer player){
        CampData campData = getCampData(player.getCamp());

        if(campData == null){
            return;
        }

        Changed changed = new Changed();
        List<CampSkillData> list = campData.getSkillDataList();

        for(int i = 0; i < list.size(); i++){
            CampSkillData temp = (CampSkillData) list.get(i);
            CampSkillLevel temp1 = CampConfig.campSkills.get(temp.getEffect()).getLevel(temp.getLevel());

            if(temp1 == null || temp1.getParm1() == 0){
                continue;
            }

            Buf buf = new Buf(bufId.incrementAndGet(), (byte) temp.getEffect(), temp1.getParm1(), 3600 * 24, Buf.UNIT_CAMP);
            buf.setTimestamp(System.currentTimeMillis());
            player.addBuf(buf, changed);
        }
//        if(player.getCampBuf(Buf.CAMP_EVA) != null || player.getCampBuf(Buf.CAMP_STONE) != null){
//        	player.adjustProperty();
//        }
        connectService.sendGetItem(changed, player.getId(), (byte) 4);
    }

    /**
     * 关闭服务
     */
    public void shutdown(){
        try{
            saveCamp();
            ItemGroup.save2File();
            ItemTop.save2File();
            BossBattleTop.saveplayerData();
            TrainLevelTop.saveplayerData();
            TrainLevelTop.save2File(false);
            //封印法阵各阵眼等级排行
            MagicPositionTop.saveMagicPosWaterData();
            MagicPositionTop.saveMagicPosSoilData();
            MagicPositionTop.saveMagicPosFireData();
            MagicPositionTop.saveMagicPosWindData();
            MagicPositionTop.saveMagicPosMindData();
            RabbitRaceConfig.givePlayerPrizeWhenShutDown();
            //宠物培养排行榜
            PetDevelopTop.saveplayerData();
        }catch(Exception e){
            log.error(e, e);
        }
    }
    
    public void run(){
        while(true){
            try{
                long now = System.currentTimeMillis();
                synchronized(jobs){
                	Iterator iter = jobs.values().iterator();
                	while(iter.hasNext()){
                		CampJob job = (CampJob)iter.next();
                		boolean result = job.process(now);
                		if(result){
                			iter.remove();
                		}
                	}
                }
                
                try{
                	//兔子赛跑活动
                	RabbitRaceConfig.action(now);
                	//记歌词活动
                	LyricsConfig.action(now);
                	//情歌对唱活动
                	LoveLyricsConfig.action(now);
                	//奥运问答活动
                	RiddlesConfig.action(now);
                	//咏春诗歌活动
                	RiddlesConfig2.action(now);
                	// 检查一次禁言名单
                	checkSilence(now);
                	
                	if(Server.iMoneyType == Server.IMONEY_TYPE_PIP){
	                	//世界BOSS
	                	WorldBossConfig.action(now);
                	}
                	
                	//宝石排行榜
                	GemTop.reset();
                	
                	//NoahsarkConfig.resetiShopLion(now);
                	//IShopTimeItem.goodsList(now);
                }catch(Exception e){
                	log.error(e, e);
                }
                
                
                //每10分钟保存一次阵营数据
                if(now - lastSaveTime > 60000L){
                    saveCamp();
                    lastSaveTime = now;
                }
                
                // 每一小时保存一次腊八（食神活动）排行榜数据
                if (now - lastSaveIronChefXml > 1000L * 3600L) {
                	TwelfthLunarService.saveIronChefActivityXml(TwelfthLunarConfig.playerDonateMap);
                	lastSaveIronChefXml = now;
                }

                //每天0点清理禁言和罚款信息，并维护技能
                if(Utils.getTodayStart() - lastClearTime >= (24 * 3600 * 1000L)){
                    clearAndMaintSkill(darkCamp, Utils.getTodayStart());
                    clearAndMaintSkill(lightCamp, Utils.getTodayStart());
                    saveCamp();   
                    //重置一下圣诞节的活动时间
                    ChristmasConfig.resetTime();
                    // 重置喊话活动的时间
                    ShoutConfig.resetTime();
                    // 重置腊八活动施粥的时间
                    TwelfthLunarConfig.resetTime();
                    //重置记歌词活动
                    LyricsConfig.resetTime();
                    LyricsConfig.reset();
                    //重置情歌对唱活动
                    LoveLyricsConfig.resetTime();
                    LoveLyricsConfig.reset();
                    //兔子赛跑活动
                    RabbitRaceConfig.resetTime();
                    RabbitRaceConfig.reset();
                    //重置猜灯谜活动
                    RiddlesConfig.resetTime();
                    RiddlesConfig.reset();
                    //重置咏春诗歌活动
                    RiddlesConfig2.resetTime();
                    RiddlesConfig2.reset();
                    
                    lastClearTime = Utils.getTodayStart();
                    
                    //把在线的师傅重置下被呼叫的次数
                    playerService.resetMasterCallCount();
                    
                    //指路宝典重置
                    BookConfig.reset();
                    
                    //刷新商店物品个数
                    ItemGroup.refreshGroup(ItemConstants.REFRESHTYPE_DAY, now);
                    
                    // 重置所有在线玩家杀戮点数
                    playerService.resetCampBattlefieldKillPoints();
                    
                    //将杀戮点数排行榜进行保存
                    ItemTop.save2File();
                    
                    //多层boss挑战排行榜保存
                    BossBattleTop.saveplayerData();
                    
                    //聚灵等级排行
                    TrainLevelTop.saveplayerData();
                
                    //使用聚灵点排行
                    TrainLevelTop.save2File(true);
                    
                    //封印法阵各阵眼等级排行
                    MagicPositionTop.saveMagicPosWaterData();
                    MagicPositionTop.saveMagicPosSoilData();
                    MagicPositionTop.saveMagicPosFireData();
                    MagicPositionTop.saveMagicPosWindData();
                    MagicPositionTop.saveMagicPosMindData();
                    
                    //宠物培养排行榜
                    PetDevelopTop.saveplayerData();                   
                    //每天零点重置捐献次数
                    NoahsarkConfig.reset(now);
                   
                }
            }catch(Exception e){
                log.error(e, e);
            }finally{
                try{
                    Thread.sleep(5000L);
                }catch(Exception e){
                }
            }
        }
    }

    /**
     * 执行清理，并维护技能
     */
    private void clearAndMaintSkill(CampData campData, long now){
        if(campData != null){
            campData.clear();
            maintSkills(campData, now, chatService);
        }
    }
    
    private void maintSkills(CampData campData, long now, ChatService chatService){
    	long dayTime = 24 * 3600 * 1000L;
        for(CampSkillData campSkillData : campData.getSkillDataList()){
        	//超过一天没维护的进行维护
            if(System.currentTimeMillis() - campSkillData.getLastMaintTime() >=dayTime){
                CampSkill campSkill = null;
                CampSkillLevel campSkillLevel = null;

                campSkill = CampConfig.campSkills.get(campSkillData.getEffect());

                if(campSkill != null){
                    campSkillLevel = campSkill.getLevel(campSkillData.getLevel());
                }

                if(campSkillLevel != null){
                    int needMoney = campSkillLevel.getMaint();

                    log.info("maintSkills needMoney[" + needMoney + "] current CampMoney[" + campData.getCamp().getMoney() + "]");
                    if(campData.getCamp().getMoney() >= needMoney){
                        campData.getCamp().setMoney(campData.getCamp().getMoney() - needMoney);
                        
                        //设置为无时间 下次进行维护时可以维护 手动也可以维护
                        campSkillData.setLastMaintTime(-1);

                        chatService.sendCampMessage(-1, "系统", "阵营科技(" + campSkill.getName() + campSkillLevel.getLevel() + "级)维护成功，阵营金库余额: " + campData.getCamp().getMoney(), campData.getCamp()
                                        .getCamp());
                    }else{
                        if(campSkillData.getLevel() > 0){
                            campSkillData.setLevel(campSkillData.getLevel() - 1);
                            chatService.sendCampMessage(-1, "系统", "阵营科技(" + campSkill.getName() + campSkillLevel.getLevel() + "级)维护失败被降级，阵营金库余额: " + campData.getCamp().getMoney(), campData.getCamp()
                                            .getCamp());
                        }
                    }
                }
            }else{
            	log.info("maintSkills campSkillData LastMaintTime[" + campSkillData.getLastMaintTime() + "]");
            }
        }
    }

    /**
     * 替换信息内容，将Campname或Kingname等等替换为实际的值
     * @param message
     * @param camp
     * @return
     */
    private String replayMessage(String message, int camp) throws Exception{
        PlayerDao playerDao = new PlayerDao();

        switch(camp){
            case CAMP_DARK:
                message = message.replaceAll("Campname", "黑暗");

                if(darkCamp.getKingId() >= 0){
                    message = message.replaceAll("Kingname", playerDao.getPlayerName(darkCamp.getKingId()));
                }

                break;
            case CAMP_LIGHT:
                message = message.replaceAll("Campname", "光明");

                if(lightCamp.getKingId() >= 0){
                    message = message.replaceAll("Kingname", playerDao.getPlayerName(lightCamp.getKingId()));
                }

                break;
        }

        return message;
    }

    /**
     * 读取阵营数据
     * @throws Exception
     */
    private void loadCampData() throws Exception{
        Camp darkDbData = campDao.getCamp(CAMP_DARK);
        darkCamp = new CampData(darkDbData, CAMP_DARK);
        darkPlayerCount = campDao.getCampPlayerCount(CAMP_DARK);

        Camp lightDbData = campDao.getCamp(CAMP_LIGHT);
        lightCamp = new CampData(lightDbData, CAMP_LIGHT);
        lightPlayerCount = campDao.getCampPlayerCount(CAMP_LIGHT);
    }

    private void addFaceNewKing(int kingId) throws Exception{
        //新国王的形象
        if(kingId != -1){
            Changed changed = new Changed();

            //没有选出国王
//            WorldPlayer player = playerService.loadWorldPlayer(kingId);
            WorldPlayer player = playerService.getWorldPlayerAndCatch(kingId);
            // 旧形象item
            RoleFaceData selfFace = RoleFaces.getRoleFace(player.getFace());
            IItemTemplate oldItemtemplate = Items.getTemplate(selfFace.getItemId());
            IItem oldFaceItem = oldItemtemplate.newInstance();
            Effect[] effectFace = ((IEffectItem) oldFaceItem).getEffects();
            byte oldFaceProperty = 0;
            int oldFaceBuffTime = 0;
            AtomicInteger bufId = new AtomicInteger(0);
            for(int m = 0; m < effectFace.length; m++){
                if(effectFace[m].getType() == 1){
                    PropertyEffect effectOld = (PropertyEffect) effectFace[m];
                    oldFaceProperty = effectOld.getProperty();
                    oldFaceBuffTime = effectOld.getTime();
                    // added by Jeremy:遍历下目前所有buff
                    Buf[] bufArray = player.getBufs();
                    for(int n = 0; n < bufArray.length; n++){
                        if(bufArray[n].getProperty() == oldFaceProperty){
                            long now = new Date().getTime();
                            long checkTime = (((bufArray[n].getTimestamp() + bufArray[n].getTime() * 1000L) - now) / 1000L);
                            if(checkTime > oldFaceBuffTime){
                                player.removeBuf(bufArray[n], changed);
                                Buf bufReplace = new Buf(bufId.incrementAndGet(), oldFaceProperty, effectOld.getValue(), (int) (checkTime - oldFaceBuffTime), effectOld.getUnit());
                                bufReplace.setTimestamp(now);
                                player.addBuf(bufReplace, changed);
                            }else{
                                player.removeBuf(bufArray[n], changed);
                            }
                            break;
                        }
                    }
                }
            }
            // 新形象
            if(player.getSex() == 0){ //改为男性
                // 阵营
                if(player.getCamp() == 1){ //黑暗
                    player.setTitle("黑暗阵营领袖");
                    player.setFace((short) 36);
                }else{
                    player.setTitle("光明阵营领袖");
                    player.setFace((short) 34);
                }
            }else{//改为女性
                  // 阵营
                if(player.getCamp() == 1){//黑暗
                    player.setTitle("黑暗阵营领袖");
                    player.setFace((short) 37);
                }else{
                    player.setTitle("光明阵营领袖");
                    player.setFace((short) 35);
                }
            }
            player.completeAddRoleFace(player.getFace(), 1, changed, -1);
            player.completeAddRoleTitle(player.getTitle());
            changed.setProperty(Changed.TITLE_STRING, player.getTitle());
            changed.setProperty(Changed.FACE, player.getFace()); //同步形象
            changed.setProperty(Changed.CAMPKING, 1); //同步国王的信息
            connectService.sendGetItem(changed, player.getId(), (byte) 22);
            log.info("King's RoleFace[" + player.getId() + "]Camp[" + player.getCamp() + "]face[" + player.getFace() + "]title[" + player.getTitle() + "]OK");
            WorldPlayer tmpPlayer = playerService.getWorldPlayer(player.getId());
            if(tmpPlayer!=null && tmpPlayer.online()){
            	UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
				seg.writeShort(ClientConstants.EXTEND_CAMPOFFICIAL);
				seg.write((byte)1);
				connectService.writeTo(seg, tmpPlayer.getId());
            }
            playerService.releasePlayer(player);
        }
    }

    private void removeFaceOldKing(int kingId) throws Exception{
        //旧国王的形象，更新
        if(kingId != -1){ //旧国王存在
//            WorldPlayer player = playerService.loadWorldPlayer(kingId);
        	WorldPlayer player = playerService.getWorldPlayerAndCatch(kingId);
            Changed changed1 = new Changed();
            if(player.getSex() == 0){ //改为男性
                // 阵营
                if(player.getCamp() == 1){ //黑暗
                    player.removeRoleFace(36); //删除橱窗里的形象  
                    if(player.getFace() == 36){
                        player.setFace((short) 30);
                    }
                }else{
                    player.removeRoleFace(34); //删除橱窗里的形象  
                    if(player.getFace() == 34){
                        player.setFace((short) 28);
                    }
                }
            }else{//改为女性
                if(player.getCamp() == 1){
                    player.removeRoleFace(37); //删除橱窗里的形象  
                    if(player.getFace() == 37){
                        player.setFace((short) 31);
                    }
                }else{
                    player.removeRoleFace(35); //删除橱窗里的形象  
                    if(player.getFace() == 35){
                        player.setFace((short) 29);
                    }
                }
            }
            if(player.getCamp() == 1){
                player.removeRoleTitle("黑暗阵营领袖");
                if("黑暗阵营领袖".equals(player.getTitle())){
                    player.setTitle("");
                    changed1.setProperty(Changed.TITLE_STRING, player.getTitle());
                }
            }else if(player.getCamp() == 2){
                player.removeRoleTitle("光明阵营领袖");
                if("光明阵营领袖".equals(player.getTitle())){
                    player.setTitle("");
                    changed1.setProperty(Changed.TITLE_STRING, player.getTitle());
                }
            }
            player.completeAddRoleFace(player.getFace(), 1, changed1, -1);
            changed1.setProperty(Changed.FACE, player.getFace());
            changed1.setProperty(Changed.CAMPKING, 0);
            connectService.sendGetItem(changed1, kingId, (byte) 22);
            WorldPlayer tmpPlayer = playerService.getWorldPlayer(player.getId());
            if(tmpPlayer!=null && tmpPlayer.online()){
            	UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
				seg.writeShort(ClientConstants.EXTEND_CAMPOFFICIAL);
				seg.write((byte)2);
				connectService.writeTo(seg, tmpPlayer.getId());
            }
            playerService.releasePlayer(player);
        }
    }
    
    /**
     * 检索禁言名单
     */
    private void checkSilence (long now) {
    	if (darkCamp != null) {
    		recoverySilence(darkCamp, now);
    	}
    	if (lightCamp != null) {
    		recoverySilence(lightCamp, now);
    	}
    }
    
    /**
     *	满一小时恢复发言权 官员的为30分钟
     */
    private void recoverySilence (CampData campData, long now) {
		Iterator ite = campData.getSilenceTimes();
		while (ite.hasNext()) {
//			long silenceTime = (Long) ite.next();
			CampSilence silence = (CampSilence) ite.next();
			if (now > silence.getEndTime()) {
				ite.remove();
			}
		}
    }
    
    public PlayerService getPlayerService(){
    	return playerService;
    }
    public ConnectService getConnectService(){
    	return connectService;
    }
}
