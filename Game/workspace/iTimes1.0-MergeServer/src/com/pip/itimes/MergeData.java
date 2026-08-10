package com.pip.itimes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.pip.itimes.bean.Tbl_Arenateam;
import com.pip.itimes.bean.Tbl_Auction;
import com.pip.itimes.bean.Tbl_Bbs;
import com.pip.itimes.bean.Tbl_Blog;
import com.pip.itimes.bean.Tbl_Buy;
import com.pip.itimes.bean.Tbl_Camp;
import com.pip.itimes.bean.Tbl_Campcandidate;
import com.pip.itimes.bean.Tbl_Campqualification;
import com.pip.itimes.bean.Tbl_Camptech;
import com.pip.itimes.bean.Tbl_Charge;
import com.pip.itimes.bean.Tbl_Friend;
import com.pip.itimes.bean.Tbl_Gift;
import com.pip.itimes.bean.Tbl_Hopegrass;
import com.pip.itimes.bean.Tbl_House;
import com.pip.itimes.bean.Tbl_IMoneyCard;
import com.pip.itimes.bean.Tbl_Ibuy;
import com.pip.itimes.bean.Tbl_Leavemessage;
import com.pip.itimes.bean.Tbl_Mail;
import com.pip.itimes.bean.Tbl_Master;
import com.pip.itimes.bean.Tbl_Mate;
import com.pip.itimes.bean.Tbl_Oem;
import com.pip.itimes.bean.Tbl_Petmanager;
import com.pip.itimes.bean.Tbl_Shop;
import com.pip.itimes.bean.Tbl_Tong;
import com.pip.itimes.bean.Tbl_Treasure;
import com.pip.itimes.bean.Tbl_Userdata;
import com.pip.itimes.bean.Tbl_Vote;
import com.pip.itimes.bean.Tbl_Votecamp;
import com.pip.itimes.bean.Tbl_Votecontent;
import com.pip.itimes.dao.Tbl_ArenateamDao;
import com.pip.itimes.dao.Tbl_AuctionDao;
import com.pip.itimes.dao.Tbl_BbsDao;
import com.pip.itimes.dao.Tbl_BlogDao;
import com.pip.itimes.dao.Tbl_BuyDao;
import com.pip.itimes.dao.Tbl_CampDao;
import com.pip.itimes.dao.Tbl_CampcandidateDao;
import com.pip.itimes.dao.Tbl_CampqualificationDao;
import com.pip.itimes.dao.Tbl_CamptechDao;
import com.pip.itimes.dao.Tbl_ChargeDao;
import com.pip.itimes.dao.Tbl_FriendDao;
import com.pip.itimes.dao.Tbl_GiftDao;
import com.pip.itimes.dao.Tbl_HopegrassDao;
import com.pip.itimes.dao.Tbl_HouseDao;
import com.pip.itimes.dao.Tbl_IMoneyCardDao;
import com.pip.itimes.dao.Tbl_IbuyDao;
import com.pip.itimes.dao.Tbl_IdDao;
import com.pip.itimes.dao.Tbl_LeavemessageDao;
import com.pip.itimes.dao.Tbl_MailDao;
import com.pip.itimes.dao.Tbl_MasterDao;
import com.pip.itimes.dao.Tbl_MateDao;
import com.pip.itimes.dao.Tbl_OemDao;
import com.pip.itimes.dao.Tbl_PetmanagerDao;
import com.pip.itimes.dao.Tbl_ShopDao;
import com.pip.itimes.dao.Tbl_TongDao;
import com.pip.itimes.dao.Tbl_TreasureDao;
import com.pip.itimes.dao.Tbl_UserdataDao;
import com.pip.itimes.dao.Tbl_VoteDao;
import com.pip.itimes.dao.Tbl_VotecampDao;
import com.pip.itimes.dao.Tbl_VotecontentDao;
//import com.pip.itimes.server.stage.Technology;

public class MergeData{
    private static final Logger log = Logger.getLogger(MergeData.class);
    
    private int maxArenateamId;
    private int maxArenateamId_new;
    private HashMap<String, String> arenateamNames;
    private HashMap<String, String> arenateamNames_new;
    
    private int maxAuctionId;
    private int maxAuctionId_new;
    private int maxBbsId;
    private int maxBbsId_new;
    private int maxCampcandidateId;
    private int maxCampcandidateId_new;
    private int maxCampqualificatioId;
    private int maxCampqualificatioId_new;
    private int maxBlogId;
    private int maxBlogId_new;
    private int maxBuyId;
    private int maxBuyId_new;
    private int maxChargeId;
    private int maxChargeId_new;
    private int maxIMoneyCardId;
    private int maxIMoneyCardId_new;
    private int maxFriendId;
    private int maxFriendId_new;
    private int maxGiftId;
    private int maxGiftId_new;
    private int maxHopegrassId;
    private int maxHopegrassId_new;
    private int maxHouseId;
    private int maxHouseId_new;
    private int maxIbuyId;
    private int maxIbuyId_new;
    private int maxLeavemessageId;
    private int maxLeavemessageId_new;
    private int maxMailId;
    private int maxMailId_new;
	private int maxMasterId;
    private int maxMasterId_new;
    private int maxMateId;
    private int maxMateId_new;
    private int maxOemId;
    private int maxOemId_new;
    private int maxPetmanagerId;
    private int maxPetmanagerId_new;

    private int maxShopId;
    private int maxShopId_new;
    private HashMap<String, String> shopNames;
    private HashMap<String, String> shopNames_new;
    
    private int maxPlayerId;
    private int maxPlayerId_new;
    private HashMap<String, String> playerNames;
    private HashMap<String, String> playerNames_new;

    private int maxTongId;
    private int maxTongId_new;
    private HashMap<String, String> tongNames;
    private HashMap<String, String> tongNames_new;
    
    private int maxTreasureId;
    private int maxTreasureId_new;
    private int maxVoteId;
    private int maxVoteId_new;
    private int maxVotecampId;
    private int maxVotecampId_new;
    private int maxVotecontentId;
    private int maxVotecontentId_new;
    
    public HashMap<Integer, Tbl_Camp> campTable;
    private HashMap<Integer, Tbl_Camptech> camptechTable;

    public HashMap<Integer, String> renamePlayers;
    
    private int maxEquipmentId;
    public int maxEquipmentId_new;
    private int maxPetId;
    public int maxPetId_new;

    private String namePostfix;
    
    public ServerConfig mainServer = null;

    public void initMergeData(ServerConfig server) throws Exception{
        log.info("Init Merge Data.");
        
        if(mainServer == null){
        	mainServer = server;
        }

        Tbl_ArenateamDao arenateamDao = new Tbl_ArenateamDao(server);
        maxArenateamId = arenateamDao.getMaxId(Tbl_Arenateam.class.getSimpleName().toLowerCase());
        maxArenateamId_new = maxArenateamId;
        log.info("Max ArenaTeam Id : " + maxArenateamId);
        arenateamNames = new HashMap<String, String>();
        arenateamNames_new = new HashMap<String, String>();
        List<String> arenateamList = arenateamDao.getAllNames();
        for(String arenaName : arenateamList){
            if(arenaName != null && arenaName.trim().length() > 0){
                arenateamNames.put(arenaName, arenaName);
            }
        }
        log.info("Read arenateam name data completed : " + arenateamNames.size());
        
        maxAuctionId = new Tbl_AuctionDao(server).getMaxId(Tbl_Auction.class.getSimpleName().toLowerCase());
        maxAuctionId_new = maxAuctionId;
        log.info("Max Auction Id : " + maxAuctionId);

        maxBbsId = new Tbl_BbsDao(server).getMaxId(Tbl_Bbs.class.getSimpleName().toLowerCase());
        maxBbsId_new = maxBbsId;
        log.info("Max Bbs Id : " + maxBbsId);
        
        maxCampcandidateId = new Tbl_CampcandidateDao(server).getMaxId(Tbl_Campcandidate.class.getSimpleName().toLowerCase());
        maxCampcandidateId_new = maxCampcandidateId;
        log.info("Max Campcandidate Id : " + maxCampcandidateId);
        
        maxCampqualificatioId = new Tbl_CampqualificationDao(server).getMaxId(Tbl_Campqualification.class.getSimpleName().toLowerCase());
        maxCampqualificatioId_new = maxCampqualificatioId;
        log.info("Max Campqualificatio Id : " + maxCampqualificatioId);
        
        maxBlogId = new Tbl_BlogDao(server).getMaxId(Tbl_Blog.class.getSimpleName().toLowerCase());
        maxBlogId_new = maxBlogId;
        log.info("Max Blog Id : " + maxBlogId);
        
        maxBuyId = new Tbl_BuyDao(server).getMaxId(Tbl_Buy.class.getSimpleName().toLowerCase());
        maxBuyId_new = maxBuyId;
        log.info("Max Buy Id : " + maxBuyId);
        
        maxChargeId = new Tbl_ChargeDao(server).getMaxId(Tbl_Charge.class.getSimpleName().toLowerCase());
        maxChargeId_new = maxChargeId;
        log.info("Max Charge Id : " + maxChargeId);
        
        maxFriendId = new Tbl_FriendDao(server).getMaxId(Tbl_Friend.class.getSimpleName().toLowerCase());
        maxFriendId_new = maxFriendId;
        log.info("Max Friend Id : " + maxFriendId);

        maxGiftId = new Tbl_GiftDao(server).getMaxId(Tbl_Gift.class.getSimpleName().toLowerCase());
        maxGiftId_new = maxGiftId;
        log.info("Max Gift Id : " + maxGiftId);
        
        maxHopegrassId = new Tbl_HopegrassDao(server).getMaxId(Tbl_Hopegrass.class.getSimpleName().toLowerCase());
        maxHopegrassId_new = maxHopegrassId;
        log.info("Max Hopegrass Id : " + maxHopegrassId);
        
        maxHouseId = new Tbl_HouseDao(server).getMaxId(Tbl_House.class.getSimpleName().toLowerCase());
        maxHouseId_new = maxHouseId;
        log.info("Max House Id : " + maxHouseId);

        maxIbuyId = new Tbl_IbuyDao(server).getMaxId(Tbl_Ibuy.class.getSimpleName().toLowerCase());
        maxIbuyId_new = maxIbuyId;
        log.info("Max Ibuy Id : " + maxIbuyId);
        
        maxIMoneyCardId = new Tbl_IMoneyCardDao(server).getMaxId(Tbl_IMoneyCard.class.getSimpleName().toLowerCase());
        maxIMoneyCardId_new = maxIMoneyCardId;
        log.info("Max IMoneyCard Id : " + maxIMoneyCardId);
        
        maxLeavemessageId = new Tbl_LeavemessageDao(server).getMaxId(Tbl_Leavemessage.class.getSimpleName().toLowerCase());
        maxLeavemessageId_new = maxLeavemessageId;
        log.info("Max Leavemessage Id : " + maxLeavemessageId);

        maxMailId = new Tbl_MailDao(server).getMaxId(Tbl_Mail.class.getSimpleName().toLowerCase());
        maxMailId_new = maxMailId;
        log.info("Max Mail Id : " + maxMailId);
        
        maxMasterId = new Tbl_MasterDao(server).getMaxId(Tbl_Master.class.getSimpleName().toLowerCase());
        maxMasterId_new = maxMasterId;
        log.info("Max Master Id : " + maxMasterId);
        
        maxMateId = new Tbl_MateDao(server).getMaxId(Tbl_Mate.class.getSimpleName().toLowerCase());
        maxMateId_new = maxMateId;
        log.info("Max Mate Id : " + maxMateId);
        
        maxOemId = new Tbl_OemDao(server).getMaxId(Tbl_Oem.class.getSimpleName().toLowerCase());
        maxOemId_new = maxOemId;
        log.info("Max Oem Id : " + maxOemId);

        maxPetmanagerId = new Tbl_PetmanagerDao(server).getMaxId(Tbl_Petmanager.class.getSimpleName().toLowerCase());
        maxPetmanagerId_new = maxPetmanagerId;
        log.info("Max Petmanager Id : " + maxPetmanagerId);
        
        maxTreasureId = new Tbl_TreasureDao(server).getMaxId(Tbl_Treasure.class.getSimpleName().toLowerCase());
        maxTreasureId_new = maxTreasureId;
        log.info("Max Treasure Id : " + maxTreasureId);

        Tbl_ShopDao shopDao = new Tbl_ShopDao(server);
        maxShopId = shopDao.getMaxId(Tbl_Shop.class.getSimpleName().toLowerCase());
        maxShopId_new = maxShopId;
        log.info("Max Shop Id : " + maxShopId);
        shopNames = new HashMap<String, String>();
        shopNames_new = new HashMap<String, String>();
        List<String> shopNameList = shopDao.getAllNames();
        for(String shopName : shopNameList){
            if(shopName != null && shopName.trim().length() > 0){
                shopNames.put(shopName, shopName);
            }
        }
        log.info("Read shop name data completed : " + shopNames.size());
        
        playerNames = new HashMap<String, String>();
        playerNames_new = new HashMap<String, String>();
        Tbl_UserdataDao userdataDao = new Tbl_UserdataDao(server);
        maxPlayerId = userdataDao.getMaxId(Tbl_Userdata.class.getSimpleName().toLowerCase());
        maxPlayerId_new = maxPlayerId;
        log.info("Max Player Id : " + maxPlayerId);
        List<String> playerNameList = userdataDao.getAllNames();
        for(String playerName : playerNameList){
            if(playerName != null && playerName.trim().length() > 0){
                playerNames.put(playerName, playerName);
            }
        }
        log.info("Read player name data completed : " + playerNames.size());

        tongNames = new HashMap<String, String>();
        tongNames_new = new HashMap<String, String>();
        Tbl_TongDao tongDao = new Tbl_TongDao(server);
        maxTongId = tongDao.getMaxId(Tbl_Tong.class.getSimpleName().toLowerCase());
        maxTongId_new = maxTongId;
        log.info("Max Tong Id : " + maxTongId);
        List<String> tongNameList = tongDao.getAllNames();
        for(String tongName : tongNameList){
            if(tongName != null && tongName.trim().length() > 0){
                tongNames.put(tongName, tongName);
            }
        }
        log.info("Read Tong name data completed : " + tongNames.size());
        
        maxVoteId = new Tbl_VoteDao(server).getMaxId(Tbl_Vote.class.getSimpleName().toLowerCase());
        maxVoteId_new = maxVoteId;
        log.info("Max Vote Id : " + maxVoteId);
        
        maxVotecampId = new Tbl_VotecampDao(server).getMaxId(Tbl_Votecamp.class.getSimpleName().toLowerCase());
        maxVotecampId_new = maxVotecampId;
        log.info("Max Votecamp Id : " + maxVotecampId);
        
        maxVotecontentId = new Tbl_VotecontentDao(server).getMaxId(Tbl_Votecontent.class.getSimpleName().toLowerCase());
        maxVotecontentId_new = maxVotecontentId;
        log.info("Max Votecontent Id : " + maxVotecontentId);
        
        campTable = new HashMap<Integer, Tbl_Camp>();
        Tbl_CampDao campDao = new Tbl_CampDao(server);
        List<Tbl_Camp> campList = campDao.getList(0, Integer.MAX_VALUE);
        for(Tbl_Camp camp : campList){
        	if(camp.getValid() != 0){
        		campTable.put(camp.getCamp(), camp);
        	}
        }
        log.info("Read Camp data completed. : " + campTable.size());
        
        camptechTable = new HashMap<Integer, Tbl_Camptech>();
        Tbl_CamptechDao camptechDao = new Tbl_CamptechDao(server);
        List<Tbl_Camptech> camptechList = camptechDao.getList(0, Integer.MAX_VALUE);
        for(Tbl_Camptech camptech : camptechList){
        	camptechTable.put(camptech.getCamp(), camptech);
        }
        log.info("Read Camptech data completed. : " + camptechTable.size());
        
        Tbl_IdDao idDao = new Tbl_IdDao(server);
        maxEquipmentId = idDao.getCurrentEquipmentId();
        maxEquipmentId_new = maxEquipmentId;
        log.info("Max Equipment Id : " + maxEquipmentId);
        maxPetId = idDao.getCurrentPetId();
        maxPetId_new = maxPetId;
        log.info("Max Pet Id : " + maxPetId);
        
        renamePlayers = new HashMap<Integer, String>();
    }

    public void preProcess(String namePostfix){
        this.namePostfix = namePostfix;

        maxArenateamId = maxArenateamId_new;
        Iterator<String> itArenaName = arenateamNames_new.keySet().iterator();
        while(itArenaName.hasNext()){
            String key = itArenaName.next();
            arenateamNames.put(key, arenateamNames_new.get(key));
        }
        arenateamNames_new = new HashMap<String, String>();
        
        maxAuctionId = maxAuctionId_new;
        maxBbsId = maxBbsId_new;
        maxCampcandidateId = maxCampcandidateId_new;
        maxCampqualificatioId = maxCampqualificatioId_new;
        maxBlogId = maxBlogId_new;
        maxBuyId = maxBuyId_new;
        maxChargeId = maxChargeId_new;
        maxFriendId = maxFriendId_new;
        maxGiftId = maxGiftId_new;
        maxHopegrassId = maxHopegrassId_new;
        maxHouseId = maxHouseId_new;
        maxIbuyId = maxIbuyId_new;
        maxIMoneyCardId = maxIMoneyCardId_new;
        maxLeavemessageId = maxLeavemessageId_new;
        maxMailId = maxMailId_new;
        maxMasterId = maxMasterId_new;
        maxMateId = maxMateId_new;
        maxOemId = maxOemId_new;
        maxPetmanagerId = maxPetmanagerId_new;
        maxPlayerId = maxPlayerId_new;
        maxTongId = maxTongId_new;
        maxTreasureId = maxTreasureId_new;
        
        maxShopId = maxShopId_new;
        Iterator<String> itShopName = shopNames_new.keySet().iterator();
        while(itShopName.hasNext()){
            String key = itShopName.next();
            shopNames.put(key, shopNames_new.get(key));
        }
        shopNames_new = new HashMap<String, String>();

        Iterator<String> itPlayerName = playerNames_new.keySet().iterator();
        while(itPlayerName.hasNext()){
            String key = itPlayerName.next();
            playerNames.put(key, playerNames_new.get(key));
        }
        playerNames_new = new HashMap<String, String>();

        Iterator<String> itTongName = tongNames_new.keySet().iterator();
        while(itTongName.hasNext()){
            String key = itTongName.next();
            tongNames.put(key, tongNames_new.get(key));
        }
        tongNames_new = new HashMap<String, String>();
        
        maxVoteId = maxVoteId_new;
        maxVotecampId = maxVotecampId_new;
        maxVotecontentId = maxVotecontentId_new;
        
        maxEquipmentId = maxArenateamId_new;
        maxPetId = maxPetId_new;
    }
    
    public int procEquipmentId(int id){
        if(id >= 0){
            int result = id + maxEquipmentId + 1;
            maxEquipmentId_new = Math.max(maxEquipmentId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procPetId(int id){
        if(id >= 0){
            int result = id + maxPetId + 1;
            maxPetId_new = Math.max(maxPetId_new, result);
            return result;
        }else{
            return id;
        }
    }

    public int procArenaTeamId(int id){
        if(id >= 0){
            int result = id + maxArenateamId + 1;
            maxArenateamId_new = Math.max(maxArenateamId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public String procArenaTeamName(String name){
        if(name != null && name.trim().length() > 0){
            StringBuffer sb = new StringBuffer();
    
            sb.append(name);
    
            if(arenateamNames.containsKey(name)){
                sb.append(namePostfix);
            }else{
                arenateamNames_new.put(name, name);
            }
    
            return sb.toString();
        }else{
            return name;
        }
    }
    
    public int procAuctionId(int id){
        if(id >= 0){
            int result = id + maxAuctionId + 1;
            maxAuctionId_new = Math.max(maxAuctionId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procBbsId(int id){
        if(id >= 0){
            int result = id + maxBbsId + 1;
            maxBbsId_new = Math.max(maxBbsId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procCampcandidateId(int id){
    	if(id >= 0){
    		int result = id + maxCampcandidateId + 1;
    		maxCampcandidateId_new = Math.max(maxCampcandidateId_new, result);
    		return result;
    	}else{
    		return id;
    	}
    }
    
    public int procCampqualificatioId(int id){
    	if(id >= 0){
    		int result = id + maxCampqualificatioId + 1;
    		maxCampqualificatioId_new = Math.max(maxCampqualificatioId_new, result);
    		return result;
    	}else{
    		return id;
    	}
    }
    
    public int procBlogId(int id){
        if(id >= 0){
            int result = id + maxBlogId + 1;
            maxBlogId_new = Math.max(maxBlogId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procBuyId(int id){
        if(id >= 0){
            int result = id + maxBuyId + 1;
            maxBuyId_new = Math.max(maxBuyId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procChargeId(int id){
        if(id >= 0){
            int result = id + maxChargeId + 1;
            maxChargeId_new = Math.max(maxChargeId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procIMoneyCardId(int id){
    	if(id >= 0){
    		int result = id + maxIMoneyCardId + 1;
    		maxIMoneyCardId_new = Math.max(maxIMoneyCardId_new, result);
    		return result;
    	}else{
    		return id;
    	}
    }
    
    public int procFriendId(int id){
        if(id >= 0){
            int result = id + maxFriendId + 1;
            maxFriendId_new = Math.max(maxFriendId_new, result);
            return result;
        }else{
            return id;
        }
    }

    public int procPlayerId(int id){
        if(id >= 0){
            int result = id + maxPlayerId + 1;
            maxPlayerId_new = Math.max(maxPlayerId_new, result);
            return result;
        }else{
            return id;
        }
    }

    public String procPlayerName(String name){
        if(name != null && name.trim().length() > 0){
            StringBuffer sb = new StringBuffer();
    
            sb.append(name);
    
            if(playerNames.containsKey(name)){
                sb.append(namePostfix);
            }else{
                playerNames_new.put(name, name);
            }
    
            return sb.toString();
        }else{
            return name;
        }
    }

    public int procGiftId(int id){
        if(id >= 0){
            int result = id + maxGiftId + 1;
            maxGiftId_new = Math.max(maxGiftId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procHopegrassId(int id){
        if(id >= 0){
            int result = id + maxHopegrassId + 1;
            maxHopegrassId_new = Math.max(maxHopegrassId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procHouseId(int id){
        if(id >= 0){
            int result = id + maxHouseId + 1;
            maxHouseId_new = Math.max(maxHouseId_new, result);
            return result;
        }else{
            return id;
        }
    }

    public int procIbuyId(int id){
        if(id >= 0){
            int result = id + maxIbuyId + 1;
            maxIbuyId_new = Math.max(maxIbuyId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procLeavemessageId(int id){
        if(id >= 0){
            int result = id + maxLeavemessageId + 1;
            maxLeavemessageId_new = Math.max(maxLeavemessageId_new, result);
            return result;
        }else{
            return id;
        }
    }

    public int procMailId(int id){
        if(id >= 0){
            int result = id + maxMailId + 1;
            maxMailId_new = Math.max(maxMailId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procMasterId(int id){
        if(id >= 0){
            int result = id + maxMasterId + 1;
            maxMasterId_new = Math.max(maxMasterId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procMateId(int id){
        if(id >= 0){
            int result = id + maxMateId + 1;
            maxMateId_new = Math.max(maxMateId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procOemId(int id){
        if(id >= 0){
            int result = id + maxOemId + 1;
            maxOemId_new = Math.max(maxOemId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procPetmanagerId(int id){
        if(id >= 0){
            int result = id + maxPetmanagerId + 1;
            maxPetmanagerId_new = Math.max(maxPetmanagerId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procShopId(int id){
        if(id >= 0){
            int result = id + maxShopId + 1;
            maxShopId_new = Math.max(maxShopId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public String procShopName(String name){
        if(name != null && name.trim().length() > 0){
            StringBuffer sb = new StringBuffer();
    
            sb.append(name);
    
            if(shopNames.containsKey(name)){
                sb.append(namePostfix);
            }else{
                shopNames_new.put(name, name);
            }
    
            return sb.toString();
        }else{
            return name;
        }
    }

    public int procTongId(int id){
        if(id >= 0){
            int result = id + maxTongId + 1;
            maxTongId_new = Math.max(maxTongId_new, result);
            return result;
        }else{
            return id;
        }
    }

    public String procTongName(String name){
        if(name != null && name.trim().length() > 0){
            StringBuffer sb = new StringBuffer();
    
            sb.append(name);
    
            if(tongNames.containsKey(name)){
                sb.append(namePostfix);
            }else{
                tongNames_new.put(name, name);
            }
    
            return sb.toString();
        }else{
            return name;
        }
    }
    
    public int procTreasureId(int id){
        if(id >= 0){
            int result = id + maxTreasureId + 1;
            maxTreasureId_new = Math.max(maxTreasureId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procVoteId(int id){
        if(id >= 0){
            int result = id + maxVoteId + 1;
            maxVoteId_new = Math.max(maxVoteId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procVotecampId(int id){
        if(id >= 0){
            int result = id + maxVotecampId + 1;
            maxVotecampId_new = Math.max(maxVotecampId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procVotecontentId(int id){
        if(id >= 0){
            int result = id + maxVotecontentId + 1;
            maxVotecontentId_new = Math.max(maxVotecontentId_new, result);
            return result;
        }else{
            return id;
        }
    }
    
    public int procCamptechData(Tbl_Camptech camptech){
        int result = -1;

        Tbl_Camptech oldcamCamptech = camptechTable.get(camptech.getCamp());

        if(oldcamCamptech == null){
        	camptechTable.put(camptech.getCamp(), camptech);
        }else{
            //处理国家库（国库资金合并）
        	camptech.setCampmoeny(camptech.getCampmoeny() + oldcamCamptech.getCampmoeny());

//            //处理国家科技（国家科技取高合并）
//        	try{
//	        	Technology[] oldTechs = Tools.getTechnologys(oldcamCamptech.getTechnology());
//	        	Technology[] techs = Tools.getTechnologys(camptech.getTechnology());
//	        	HashMap<Integer, Technology> temp = new HashMap<Integer, Technology>();
//	        	
//	        	for(int i = 0; i < oldTechs.length; i++){
//	        		temp.put(oldTechs[i].getEffect(), oldTechs[i]);
//	        	}
//	        	
//	        	for(int i = 0; i < techs.length; i++){
//	        		Technology old = temp.remove(techs[i].getEffect());
//	        		
//	        		if(old == null){
//	        			old = techs[i];
//	        		}else{
//	        			if(old.getLevel() < techs[i].getLevel()){
//	        				old = techs[i];
//	        			}
//	        		}
//	        		
//	        		temp.put(old.getEffect(), old);
//	        		
//	        		Technology[] newTechs = new Technology[temp.size()];
//	        		temp.values().toArray(newTechs);
//	        		
//	        		camptech.setTechnology(Tools.saveTechnologys(newTechs));
//	        	}
//        	}catch(Exception e){
//        		log.error(e, e);
//        	}

        	camptech.setId(oldcamCamptech.getId());
        	camptech.setKingid(oldcamCamptech.getKingid());
        	camptech.setKingname(oldcamCamptech.getKingname());
        	camptech.setPercent(oldcamCamptech.getPercent());
        	
            result = oldcamCamptech.getId();
            
            camptechTable.put(camptech.getCamp(), camptech);
        }

        return result;
    }
}
