package com.pip.itimes;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.pip.itimes.bean.BaseTable;
import com.pip.itimes.bean.Tbl_Admin;
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
import com.pip.itimes.bean.Tbl_Id;
import com.pip.itimes.bean.Tbl_Leavemessage;
import com.pip.itimes.bean.Tbl_Mail;
import com.pip.itimes.bean.Tbl_Master;
import com.pip.itimes.bean.Tbl_Mate;
import com.pip.itimes.bean.Tbl_Oem;
import com.pip.itimes.bean.Tbl_Petmanager;
import com.pip.itimes.bean.Tbl_Question;
import com.pip.itimes.bean.Tbl_Shop;
import com.pip.itimes.bean.Tbl_Task;
import com.pip.itimes.bean.Tbl_Tong;
import com.pip.itimes.bean.Tbl_Tongisland;
import com.pip.itimes.bean.Tbl_Treasure;
import com.pip.itimes.bean.Tbl_Userdata;
import com.pip.itimes.bean.Tbl_Vote;
import com.pip.itimes.bean.Tbl_Votecamp;
import com.pip.itimes.bean.Tbl_Votecontent;
import com.pip.itimes.dao.BaseDao;
import com.pip.itimes.dao.Tbl_AdminDao;
import com.pip.itimes.dao.Tbl_ArenateamDao;
import com.pip.itimes.dao.Tbl_AuctionDao;
import com.pip.itimes.dao.Tbl_BbsDao;
import com.pip.itimes.dao.Tbl_BlogDao;
import com.pip.itimes.dao.Tbl_BuyDao;
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
import com.pip.itimes.dao.Tbl_QuestionDao;
import com.pip.itimes.dao.Tbl_ShopDao;
import com.pip.itimes.dao.Tbl_TaskDao;
import com.pip.itimes.dao.Tbl_TongDao;
import com.pip.itimes.dao.Tbl_TongislandDao;
import com.pip.itimes.dao.Tbl_TreasureDao;
import com.pip.itimes.dao.Tbl_UserdataDao;
import com.pip.itimes.dao.Tbl_VoteDao;
import com.pip.itimes.dao.Tbl_VotecampDao;
import com.pip.itimes.dao.Tbl_VotecontentDao;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.world.StageService;

public class MergeServer{
    private static final Logger log = Logger.getLogger(MergeServer.class);

    private static XMLConfiguration config;
    private static ServerConfig mainServer;
    private static List<ServerConfig> serverList;

    private static StageService stageService;
    
    public static void main(String[] args) throws Exception{
        launch();
    }

    private static void initData() throws Exception{
        log.info("loading project data.");

        String dataDir = config.getString("datadir");
        String revision = config.getString("revision");

        if("CMCC".equals(revision)){
        }else if("CHINATEL".equals(revision)){
        }
        
        stageService = new StageService(new File(dataDir));

        log.info("project data loaded successful.");
    }

    private static void launch() throws Exception{
        log.info("Merge Started.");

        long t1 = System.currentTimeMillis();
        config = new XMLConfiguration("merge.xml");

        //load data
        initData();

        List<SubnodeConfiguration> list = config.configurationsAt("server");
        serverList = new ArrayList<ServerConfig>();

        //load server configs;
        for(SubnodeConfiguration node : list){
            ServerConfig serverConfig = new ServerConfig(node);

            if(serverConfig.getType() == ServerConfig.SERVER_TYPE_MAIN){
                if(mainServer != null){
                    throw new Exception("Duplicated main server.");
                }else{
                    mainServer = serverConfig;
                }
            }else{
                serverList.add(serverConfig);
            }
        }

        MergeData mergeData = new MergeData();
        mergeData.initMergeData(mainServer);

        OutputFile outputFile = new OutputFile();
        outputFile.setOut(config.getString("outputfile"), false);

        //processTable
        log.info("processing " + mainServer.getName());
        log.info("processing " + mainServer.getName() + " end.");
        
        for(ServerConfig serverConfig : serverList){
            log.info("processing " + serverConfig.getName());
            mergeData.preProcess("*" + serverConfig.getName());

            processTable(Tbl_Admin.class, new Tbl_AdminDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Arenateam.class, new Tbl_ArenateamDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Auction.class, new Tbl_AuctionDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Bbs.class, new Tbl_BbsDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Blog.class, new Tbl_BlogDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Buy.class, new Tbl_BuyDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Charge.class, new Tbl_ChargeDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Campcandidate.class, new Tbl_CampcandidateDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Campqualification.class, new Tbl_CampqualificationDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Camptech.class, new Tbl_CamptechDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Friend.class, new Tbl_FriendDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Gift.class, new Tbl_GiftDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Hopegrass.class, new Tbl_HopegrassDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_House.class, new Tbl_HouseDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Ibuy.class, new Tbl_IbuyDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Id.class, new Tbl_IdDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_IMoneyCard.class, new Tbl_IMoneyCardDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Leavemessage.class, new Tbl_LeavemessageDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Mail.class, new Tbl_MailDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Master.class, new Tbl_MasterDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Mate.class, new Tbl_MateDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Oem.class, new Tbl_OemDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Petmanager.class, new Tbl_PetmanagerDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Question.class, new Tbl_QuestionDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Shop.class, new Tbl_ShopDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Task.class, new Tbl_TaskDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Tong.class, new Tbl_TongDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Tongisland.class, new Tbl_TongislandDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Treasure.class, new Tbl_TreasureDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Userdata.class, new Tbl_UserdataDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Vote.class, new Tbl_VoteDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Votecamp.class, new Tbl_VotecampDao(serverConfig), outputFile, mergeData, serverConfig);
            processTable(Tbl_Votecontent.class, new Tbl_VotecontentDao(serverConfig), outputFile, mergeData, serverConfig);
            
            //阵营特殊处理
            Tbl_Camp camp = new Tbl_Camp();
            camp.process(mergeData, serverConfig);
            
            log.info("process " + serverConfig.getName() + " end.");
        }
        
        //camp表修改
        updateCampTable(outputFile, mergeData);
        
        mergeData.preProcess("*over");
        processRenamePlayer(outputFile, mergeData);

        saveIdTable(outputFile, mergeData);
        
        mainServer.close();
        for(ServerConfig serverConfig : serverList){
            serverConfig.close();
        }

        log.info("spend : " + ((System.currentTimeMillis() - t1) / 1000));
        log.info("Merge Ended.");
        
        System.exit(0);
    }
    
    private static void saveIdTable(OutputFile outputFile, MergeData mergeData){
        Tbl_Id id = new Tbl_Id();
        id.rebuild(1, mergeData.maxEquipmentId_new + 1);
        outputFile.println(id.toSqlString());
        id.rebuild(2, mergeData.maxPetId_new + 1);
        outputFile.println(id.toSqlString());
    }
    
    //修改Camp表
    private static void updateCampTable(OutputFile outputFile, MergeData mergeData){
    	String tableName = Tbl_Camp.class.getSimpleName().toLowerCase();
    	BaseDao dao = new Tbl_TreasureDao(mergeData.mainServer);
    	int count = dao.getRecordCount(tableName);
    	for(Tbl_Camp camp : mergeData.campTable.values()){
    		camp.setStatus(BaseTable.STATUS_UPDATE);
    		outputFile.println("select \'" + tableName + " : " + count + "\' as \'\';");
    		outputFile.println(camp.toSqlString());
    	}
    }
    
    private static void processRenamePlayer(OutputFile outputFile, MergeData mergeData){
        int nextMailID = 1;
        for (int player : mergeData.renamePlayers.keySet()) {
            Tbl_Mail m = new Tbl_Mail();
            m.setId(mergeData.procMailId(nextMailID));
            nextMailID++;
            m.setSourceid(-1);
            m.setSourcename("系统");
            m.setDestid(player);
            m.setDestname(mergeData.renamePlayers.get(player));
            m.setTitle("改名符");
            m.setContent("非常抱歉，因角色名字冲突，您的角色需要改一个新名字。请提取附件中的改名符，使用它来修改角色名字。");

            IItem item = Items.getTemplate(210029).newInstance();
            m.setAttachment(ItemUtils.item2dbAttachment(item, 1));
            
            m.setPrice(0);
            m.setPosttime(new Date(System.currentTimeMillis()));
            m.setValidtime(new Date(System.currentTimeMillis() + 30L * 24L * 3600L * 1000L));
            
            outputFile.println(m.toSqlString());
        }
    }
    
    private static void processTable(Class tableClass, BaseDao dao, OutputFile outputFile, MergeData mergeData, ServerConfig serverConfig){
        String tableName = tableClass.getSimpleName().toLowerCase();

        int count = dao.getRecordCount(tableName);
        int c = 0;
        int max = Math.min(10000, count);

        log.info(tableName + " : " + count);

        outputFile.println("select \'" + tableName + " : " + count + "\' as \'\';");
        
        while(c < count){
            List<BaseTable> list = dao.getList(c, max);

            log.info("Processing table " + tableName + " : " + c + " , " + (c + list.size()));
            outputFile.println("select \'" + tableName + " : " + c + " , " + (c + list.size()) + "\' as \'\';");

            for(BaseTable baseTable : list){
                int orgId = baseTable.getId();

                try{
                    baseTable.process(mergeData, serverConfig);
                    outputFile.println(baseTable.toSqlString());
                }catch(Exception e){
                    log.error("process " + tableName + " record : " + orgId + " error", e);
                }
            }

            c += max;
        }
    }
}
