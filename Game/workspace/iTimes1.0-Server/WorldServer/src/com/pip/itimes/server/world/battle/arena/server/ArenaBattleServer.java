package com.pip.itimes.server.world.battle.arena.server;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.ArenaRecord;
import com.pip.itimes.server.bean.ArenaRecordWorldWar;
import com.pip.itimes.server.bean.ArenaTeamTotal;
import com.pip.itimes.server.bean.ArenaTeamTotalWorldWar;
import com.pip.itimes.server.dao.ArenaRecordDao;
import com.pip.itimes.server.dao.ArenaTeamTotalDao;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.EquipmentTemplate;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.IItem;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ArenaServer;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.BattleDataProcess;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.BattleStrategy;
import com.pip.itimes.server.world.battle.Skill;
import com.pip.itimes.server.world.battle.arena.ArenaBattleStrategy;
import com.pip.itimes.server.world.battle.arena.ArenaConstants;
import com.pip.itimes.server.world.battle.arena.ArenaTools;

public class ArenaBattleServer implements BattleDataProcess{
    private static final Logger log = Logger.getLogger(ArenaBattleServer.class);

    protected BattleSprite[] side1;
    protected BattleSprite[] pet1;
    protected BattleSprite[] side2;
    protected BattleSprite[] pet2;
    protected int[] side1playerarenalevel;
    protected int[] side2playerarenalevel;
    protected int[] side1playerId;
    protected int[] side2playerId;
    protected String[] side1playername;
    protected String[] side2playername;

    protected ArenaBattleStrategy strategy;
    protected int type;
    protected int id = -1;
    protected int round = 1;

    protected int serverId1;
    protected int serverId2;
    protected int ownerId1;
    protected int ownerId2;
    protected String ownerName1;
    protected String ownerName2;
    protected int side1arenalevel;
    protected int side2arenalevel;
    protected String side1arenaname;
    protected String side2arenaname;
    protected String serverName1;
    protected String serverName2;
    protected String side1servernamepush;
    protected String side2servernamepush;
    //add worldwar mengjie 
    protected int side1arenalevelworldwar;
    protected int side2arenalevelworldwar;
    
    public int writeDB;
    protected ArenaServerService service;

    public Vector battleMovie;
    protected Vector battleRecorders;
    protected boolean battleOver = false;

    protected volatile STATUS status = STATUS.init;
    protected long lastTime;

    protected IPlayerData winner[] = null;
    protected IPlayerData failure[] = null;

    private ArenaRecordDao dao;
    private ArenaTeamTotalDao teamdao;

    protected static enum STATUS{
        init, wait_start, wait_fight, end
    };

    public ArenaBattleServer(int type, int id, ArenaServerService service, ArenaBattleStrategy strategy, ArenaRecordDao dao, ArenaTeamTotalDao teamdao){
        this.type = type;
        this.teamdao = teamdao;
        this.dao = dao;
        this.id = id;
        this.service = service;
        this.strategy = strategy;
    }

    public void setSide1(BattleSprite side1[], BattleSprite pet1[], int serverId1, int ownerId1, String ownerName1, int side1arenalevel, int side1playerarenalevel[], String servername,
                    String arenaname, int[] playerId, String playername[],int side1arenalevelworldwar){
        this.side1 = side1;
        this.pet1 = pet1;

        for(int i = 0; i < this.side1.length; i++){
            this.side1[i].bsType = BattleSprite.TYPE_PLAYER;
        }

        for(int i = 0; i < this.pet1.length; i++){
            if(this.pet1[i] != null){
                this.pet1[i].bsType = BattleSprite.TYPE_PLAYER_PET;
            }
        }

        this.serverId1 = serverId1;
        this.ownerId1 = ownerId1;
        this.ownerName1 = ownerName1;
        this.side1arenalevel = side1arenalevel;
        this.side1playerarenalevel = side1playerarenalevel;
        this.serverName1 = servername;
        this.side1arenaname = arenaname;
        this.side1playerId = playerId;
        this.side1playername = playername;
        this.side1servernamepush = service.getArenaService().getServerName(serverId1);
        
        this.side1arenalevelworldwar = side1arenalevelworldwar;
    }

    public void setSide2(BattleSprite side2[], BattleSprite pet2[], int serverId2, int ownerId2, String ownerName2, int side2arenalevel, int side2playerarenalevel[], String servername,
                    String arenaname, int[] playerId, String playername[],int side2arenalevelworldwar){
        this.side2 = side2;
        this.pet2 = pet2;

        for(int i = 0; i < this.side2.length; i++){
            this.side2[i].bsType = BattleSprite.TYPE_MONSTER;
        }

        for(int i = 0; i < this.pet2.length; i++){
            if(this.pet2[i] != null){
                this.pet2[i].bsType = BattleSprite.TYPE_MONSTER_PET;
            }
        }

        this.serverId2 = serverId2;
        this.ownerId2 = ownerId2;
        this.ownerName2 = ownerName2;
        this.side2arenalevel = side2arenalevel;
        this.side2playerarenalevel = side2playerarenalevel;
        this.serverName2 = servername;
        this.side2arenaname = arenaname;
        this.side2playerId = playerId;
        this.side2playername = playername;
        this.side2servernamepush = service.getArenaService().getServerName(serverId2);
        
        this.side2arenalevelworldwar = side2arenalevelworldwar;
    }

    public void start(){
        for(int i = 0; i < side1.length; i++){
            UWAPSegment seg = getPkStartSegment(ownerId1, side1[i].id, service.getArenaService().getServerName(serverId2));
            service.getArenaService().writeTo(serverId1, seg);
        }
        
        for(int i = 0; i < side2.length; i++){
            UWAPSegment seg = getPkStartSegment(ownerId2, side2[i].id, service.getArenaService().getServerName(serverId1));
            service.getArenaService().writeTo(serverId2, seg);
        }

        status = STATUS.wait_fight;
        lastTime = System.currentTimeMillis();
    }

    public void end() throws Exception{
    	boolean winarenarecord = false;
    	boolean lostarenarecord = false;
        if(writeDB == 0){
            if(failure != null){
                ArenaRecord arenarecord = new ArenaRecord();
                ArenaTeamTotal arenaTeamTotal1 = new ArenaTeamTotal();
                ArenaTeamTotal arenaTeamTotal2 = new ArenaTeamTotal();
                arenarecord.setType(type);
                //worldwar add mengjie 
//                ArenaRecordWorldWar arenarecordworldwar = new ArenaRecordWorldWar();
//                ArenaTeamTotalWorldWar arenaTeamTotalWorldWar1 = new ArenaTeamTotalWorldWar();
//                ArenaTeamTotalWorldWar arenaTeamTotalWorldWar2 = new ArenaTeamTotalWorldWar();
//                arenarecordworldwar.setType(type);

                //战斗后计算战队等级和个人等级增减
                int arenaLevelwin = side1arenalevel;
                int arenaLevellost = side1arenalevel;
                //WorldWar 战斗后计算战队等级和个人等级增减
//                int arenaLevelwinworldwar = side1arenalevelworldwar;
//                int arenaLevellostworldwar = side1arenalevelworldwar;
                if(failure[0].getId() == side2[0].id){
                    arenaLevellost = side2arenalevel;
                    
//                    arenaLevellostworldwar = side2arenalevelworldwar;
                }else{
                    arenaLevelwin = side2arenalevel;
                    
//                    arenaLevelwinworldwar = side2arenalevelworldwar;
                }

                int arenapointwin = ArenaTools.getArenaPoint(arenaLevelwin, arenaLevellost, true);
                int arenapointlost = ArenaTools.getArenaPoint(arenaLevellost, arenaLevelwin, false);

//                int arenapointwinworldwar = ArenaTools.getArenaPoint(arenaLevelwinworldwar, arenaLevellostworldwar, true);
//                int arenapointlostworldwar = ArenaTools.getArenaPoint(arenaLevellostworldwar, arenaLevelwinworldwar, false);
                //go on
                for(int i = 0; i < failure.length; i++){
                    int serverId = serverId1;
                    int playerId = side1[i].id;
                    String servername = serverName1;
                    int arenaLevel = side1arenalevel;
                    String arenaname = side1arenaname;
                    String servernamepush = side1servernamepush;
                    int ownerId = ownerId1;
                    String ownername = ownerName1;

                    int arenaLevelworldwar = side1arenalevelworldwar;
                    if(failure[i].getId() == side2[i].id){
                        serverId = serverId2;
                        playerId = side2[i].id;
                        servername = serverName2;
                        arenaLevel = side2arenalevel;
                        arenaname = side2arenaname;
                        servernamepush = side2servernamepush;
                        ownerId = ownerId2;
                        ownername = ownerName2;
                        
                        arenaLevelworldwar = side2arenalevelworldwar;
                    }

                    arenarecord.setLostserverId(servername);
                    arenarecord.setLostarenaLevel(arenaLevel);
                    //worldwar add mengjie 
//                    arenarecordworldwar.setLostserverId(servername);
//                    arenarecordworldwar.setLostarenaLevel(arenaLevelworldwar);

                    switch(i){
                        case 0:
                            arenarecord.setLostplayer1Id(failure[i].getId());
                            arenarecord.setLostplayer1Level(failure[i].getPlayer().getArenaLevel());
                            
//                            arenarecordworldwar.setLostplayer1Id(failure[i].getId());
//                            arenarecordworldwar.setLostplayer1Level(failure[i].getPlayer().getArenaLevel());
                            break;
                        case 1:
                            arenarecord.setLostplayer2Id(failure[i].getId());
                            arenarecord.setLostplayer2Level(failure[i].getPlayer().getArenaLevel());

//                            arenarecordworldwar.setLostplayer2Id(failure[i].getId());
//                            arenarecordworldwar.setLostplayer2Level(failure[i].getPlayer().getArenaLevel());
                            break;
                        case 2:
                            arenarecord.setLostplayer3Id(failure[i].getId());
                            arenarecord.setLostplayer3Level(failure[i].getPlayer().getArenaLevel());

//                            arenarecordworldwar.setLostplayer3Id(failure[i].getId());
//                            arenarecordworldwar.setLostplayer3Level(failure[i].getPlayer().getArenaLevel());
                            break;
                    }

                    switch(type){
                        case ArenaConstants.ARENA_TYPE_ONE:
                            arenarecord.setLostarenaId(failure[i].getPlayer().getArenaV1Id());
                            arenaTeamTotal1.setArenaid(failure[i].getPlayer().getArenaV1Id());

//                            arenarecordworldwar.setLostarenaId(failure[i].getPlayer().getArenaV1Id());
//                            arenaTeamTotalWorldWar1.setArenaid(failure[i].getPlayer().getArenaV1Id());
                            break;
                        case ArenaConstants.ARENA_TYPE_TWO:
                            arenarecord.setLostarenaId(failure[i].getPlayer().getArenaV2Id());
                            arenaTeamTotal1.setArenaid(failure[i].getPlayer().getArenaV2Id());

//                            arenarecordworldwar.setLostarenaId(failure[i].getPlayer().getArenaV2Id());
//                            arenaTeamTotalWorldWar1.setArenaid(failure[i].getPlayer().getArenaV2Id());
                            break;
                        case ArenaConstants.ARENA_TYPE_THREE:
                            arenarecord.setLostarenaId(failure[i].getPlayer().getArenaV3Id());
                            arenaTeamTotal1.setArenaid(failure[i].getPlayer().getArenaV3Id());

//                            arenarecordworldwar.setLostarenaId(failure[i].getPlayer().getArenaV3Id());
//                            arenaTeamTotalWorldWar1.setArenaid(failure[i].getPlayer().getArenaV3Id());
                            break;

                    }

                    arenaTeamTotal1.setArenalevel(arenaLevel + arenapointlost);
                    arenaTeamTotal1.setArenaname(arenaname);
                    arenaTeamTotal1.setOwnerid(ownerId);
                    arenaTeamTotal1.setOwnername(ownername);
                    arenaTeamTotal1.setServername(servernamepush);
                    arenaTeamTotal1.setServerid(servername);
                    arenaTeamTotal1.setType(type);
                    arenaTeamTotal1.setUpdatetime(new Date());

                    //worldwar add mengjie 
//                    arenaTeamTotalWorldWar1.setArenalevel(arenaLevelworldwar + arenapointlostworldwar);
//                    arenaTeamTotalWorldWar1.setArenaname(arenaname);
//                    arenaTeamTotalWorldWar1.setOwnerid(ownerId);
//                    arenaTeamTotalWorldWar1.setOwnername(ownername);
//                    arenaTeamTotalWorldWar1.setServername(servernamepush);
//                    arenaTeamTotalWorldWar1.setServerid(servername);
//                    arenaTeamTotalWorldWar1.setType(type);
//                    arenaTeamTotalWorldWar1.setUpdatetime(new Date());   
                    
                    ArenaServer.instance.arenaServerService.logNetWin(serverId, playerId, false);
                }

                for(int i = 0; i < winner.length; i++){
                    int serverId = serverId1;
                    int playerId = side1[i].id;
                    String servername = serverName1;
                    int arenaLevel = side1arenalevel;
                    String arenaname = side1arenaname;
                    String servernamepush = side1servernamepush;
                    int ownerId = ownerId1;
                    String ownername = ownerName1;

                    int arenaLevelworldwar = side1arenalevelworldwar;
                    if(winner[i].getId() == side2[i].id){
                        serverId = serverId2;
                        playerId = side2[i].id;
                        servername = serverName2;
                        arenaLevel = side2arenalevel;
                        arenaname = side2arenaname;
                        servernamepush = side2servernamepush;
                        ownerId = ownerId2;
                        ownername = ownerName2;
                        
                        arenaLevelworldwar = side2arenalevelworldwar;
                    }

                    arenarecord.setWinserverId(servername);
                    arenarecord.setWinarenaLevel(arenaLevel);
                    
//                    arenarecordworldwar.setWinserverId(servername);
//                    arenarecordworldwar.setWinarenaLevel(arenaLevelworldwar);

                    switch(i){
                        case 0:
                            arenarecord.setWinplayer1Id(winner[i].getId());
                            arenarecord.setWinplayer1Level(winner[i].getPlayer().getArenaLevel());

//                            arenarecordworldwar.setWinplayer1Id(winner[i].getId());
//                            arenarecordworldwar.setWinplayer1Level(winner[i].getPlayer().getArenaLevel());
                            break;
                        case 1:
                            arenarecord.setWinplayer2Id(winner[i].getId());
                            arenarecord.setWinplayer2Level(winner[i].getPlayer().getArenaLevel());

//                            arenarecordworldwar.setWinplayer2Id(winner[i].getId());
//                            arenarecordworldwar.setWinplayer2Level(winner[i].getPlayer().getArenaLevel());
                            break;
                        case 2:
                            arenarecord.setWinplayer3Id(winner[i].getId());
                            arenarecord.setWinplayer3Level(winner[i].getPlayer().getArenaLevel());

//                            arenarecordworldwar.setWinplayer3Id(winner[i].getId());
//                            arenarecordworldwar.setWinplayer3Level(winner[i].getPlayer().getArenaLevel());
                            break;
                    }

                    switch(type){
                        case ArenaConstants.ARENA_TYPE_ONE:
                            arenarecord.setWinarenaId(winner[i].getPlayer().getArenaV1Id());
                            arenaTeamTotal2.setArenaid(winner[i].getPlayer().getArenaV1Id());

//                            arenarecordworldwar.setWinarenaId(winner[i].getPlayer().getArenaV1Id());
//                            arenaTeamTotalWorldWar2.setArenaid(winner[i].getPlayer().getArenaV1Id());
                            break;
                        case ArenaConstants.ARENA_TYPE_TWO:
                            arenarecord.setWinarenaId(winner[i].getPlayer().getArenaV2Id());
                            arenaTeamTotal2.setArenaid(winner[i].getPlayer().getArenaV2Id());

//                            arenarecordworldwar.setWinarenaId(winner[i].getPlayer().getArenaV2Id());
//                            arenaTeamTotalWorldWar2.setArenaid(winner[i].getPlayer().getArenaV2Id());
                            break;
                        case ArenaConstants.ARENA_TYPE_THREE:
                            arenarecord.setWinarenaId(winner[i].getPlayer().getArenaV3Id());
                            arenaTeamTotal2.setArenaid(winner[i].getPlayer().getArenaV3Id());

//                            arenarecordworldwar.setWinarenaId(winner[i].getPlayer().getArenaV3Id());
//                            arenaTeamTotalWorldWar2.setArenaid(winner[i].getPlayer().getArenaV3Id());
                            break;

                    }

                    arenaTeamTotal2.setArenalevel(arenaLevel + arenapointwin);
                    arenaTeamTotal2.setArenaname(arenaname);
                    arenaTeamTotal2.setOwnerid(ownerId);
                    arenaTeamTotal2.setOwnername(ownername);
                    arenaTeamTotal2.setServername(servernamepush);
                    arenaTeamTotal2.setServerid(servername);
                    arenaTeamTotal2.setType(type);
                    arenaTeamTotal2.setUpdatetime(new Date());

//                    arenaTeamTotalWorldWar2.setArenalevel(arenaLevelworldwar + arenapointwinworldwar);
//                    arenaTeamTotalWorldWar2.setArenaname(arenaname);
//                    arenaTeamTotalWorldWar2.setOwnerid(ownerId);
//                    arenaTeamTotalWorldWar2.setOwnername(ownername);
//                    arenaTeamTotalWorldWar2.setServername(servernamepush);
//                    arenaTeamTotalWorldWar2.setServerid(servername);
//                    arenaTeamTotalWorldWar2.setType(type);
//                    arenaTeamTotalWorldWar2.setUpdatetime(new Date());
                    
                    ArenaServer.instance.arenaServerService.logNetWin(serverId, playerId, false);
                }
                switch(type){
                case ArenaConstants.ARENA_TYPE_ONE:
                	log.info("ArenaBattle end [1]. winplayerid[" + arenarecord.getWinplayer1Id() + "]winarenaid [" + arenarecord.getWinarenaId() + "]winarenalevel[" + arenarecord.getWinarenaLevel()
                            + "]winplayerlevel[" + arenarecord.getWinplayer1Level() + "]winserverid[" + arenarecord.getWinserverId() + "]---lostplayerid[" +

                            arenarecord.getLostplayer1Id() + "]lostarenaid [" + arenarecord.getLostarenaId() + "]lostarenalevel[" + arenarecord.getLostarenaLevel() + "]lostplayerlevel["
                            + arenarecord.getLostplayer1Level() + "]lostserverid[" + arenarecord.getLostserverId() + "]");

//                	log.info("ArenaBattle end [1] worldwar log. winplayerid[" + arenarecordworldwar.getWinplayer1Id() + "]winarenaid [" + arenarecordworldwar.getWinarenaId() + "]winarenalevel[" + arenarecordworldwar.getWinarenaLevel()
//                            + "]winplayerlevel[" + arenarecordworldwar.getWinplayer1Level() + "]winserverid[" + arenarecordworldwar.getWinserverId() + "]---lostplayerid[" +
//                            arenarecordworldwar.getLostplayer1Id() + "]lostarenaid [" + arenarecordworldwar.getLostarenaId() + "]lostarenalevel[" + arenarecordworldwar.getLostarenaLevel() + "]lostplayerlevel["
//                            + arenarecordworldwar.getLostplayer1Level() + "]lostserverid[" + arenarecordworldwar.getLostserverId() + "]");

                    break;
                case ArenaConstants.ARENA_TYPE_TWO:
                	log.info("ArenaBattle end [2]. winplayerid1[" + arenarecord.getWinplayer1Id() + "]" +
                			"winplayerid2[" + arenarecord.getWinplayer2Id() + "]winarenaid [" + arenarecord.getWinarenaId() + "]winarenalevel[" + arenarecord.getWinarenaLevel()
                            + "]winplayerlevel1[" + arenarecord.getWinplayer1Level() + "]" +
                            "winplayerlevel2[" + arenarecord.getWinplayer2Level() + "]winserverid[" + arenarecord.getWinserverId() + "]" +
                            
                            "---lostplayerid1[" + arenarecord.getLostplayer1Id() + "]" +
                            "lostplayerid2[" + arenarecord.getLostplayer2Id() + "]lostarenaid [" + arenarecord.getLostarenaId() + "]lostarenalevel[" + arenarecord.getLostarenaLevel() + "]" +
                            "lostplayerlevel1[" + arenarecord.getLostplayer1Level() + "]" +
                            "lostplayerlevel2[" + arenarecord.getLostplayer2Level() + "]lostserverid[" + arenarecord.getLostserverId() + "]");

//                	log.info("ArenaBattle end [2] worldwar log. winplayerid1[" + arenarecordworldwar.getWinplayer1Id() + "]" +
//                			"winplayerid2[" + arenarecordworldwar.getWinplayer2Id() + "]winarenaid [" + arenarecordworldwar.getWinarenaId() + "]winarenalevel[" + arenarecordworldwar.getWinarenaLevel()
//                            + "]winplayerlevel1[" + arenarecordworldwar.getWinplayer1Level() + "]" +
//                            "winplayerlevel2[" + arenarecordworldwar.getWinplayer2Level() + "]winserverid[" + arenarecordworldwar.getWinserverId() + "]" +
//                            
//                            "---lostplayerid1[" + arenarecordworldwar.getLostplayer1Id() + "]" +
//                            "lostplayerid2[" + arenarecordworldwar.getLostplayer2Id() + "]lostarenaid [" + arenarecordworldwar.getLostarenaId() + "]lostarenalevel[" + arenarecordworldwar.getLostarenaLevel() + "]" +
//                            "lostplayerlevel1[" + arenarecordworldwar.getLostplayer1Level() + "]" +
//                            "lostplayerlevel2[" + arenarecordworldwar.getLostplayer2Level() + "]lostserverid[" + arenarecordworldwar.getLostserverId() + "]");

                    break;
                case ArenaConstants.ARENA_TYPE_THREE:
                	log.info("ArenaBattle end [3]. winplayerid1[" + arenarecord.getWinplayer1Id() + "]" +
                			"winplayerid2[" + arenarecord.getWinplayer2Id() + "]" +
                			"winplayerid3[" + arenarecord.getWinplayer3Id() + "]winarenaid [" + arenarecord.getWinarenaId() + "]winarenalevel[" + arenarecord.getWinarenaLevel()
                            + "]winplayerlevel1[" + arenarecord.getWinplayer1Level() + "]" +
                            "winplayerlevel2[" + arenarecord.getWinplayer2Level() + "]" +
                            "winplayerlevel3[" + arenarecord.getWinplayer3Level() + "]winserverid[" + arenarecord.getWinserverId() + "]" +
                            
                            "---lostplayerid1[" + arenarecord.getLostplayer1Id() + "]" +
                            "lostplayerid2[" + arenarecord.getLostplayer2Id() + "]" +
                            "lostplayerid3[" + arenarecord.getLostplayer3Id() + "]lostarenaid [" + arenarecord.getLostarenaId() + "]lostarenalevel[" + arenarecord.getLostarenaLevel() + "]" +
                            "lostplayerlevel1[" + arenarecord.getLostplayer1Level() + "]" +
                            "lostplayerlevel2[" + arenarecord.getLostplayer2Level() + "]" +
                            "lostplayerlevel3[" + arenarecord.getLostplayer3Level() + "]lostserverid[" + arenarecord.getLostserverId() + "]");

//                	log.info("ArenaBattle end [3] worldwar log. winplayerid1[" + arenarecordworldwar.getWinplayer1Id() + "]" +
//                			"winplayerid2[" + arenarecordworldwar.getWinplayer2Id() + "]" +
//                			"winplayerid3[" + arenarecordworldwar.getWinplayer3Id() + "]winarenaid [" + arenarecordworldwar.getWinarenaId() + "]winarenalevel[" + arenarecordworldwar.getWinarenaLevel()
//                            + "]winplayerlevel1[" + arenarecordworldwar.getWinplayer1Level() + "]" +
//                            "winplayerlevel2[" + arenarecordworldwar.getWinplayer2Level() + "]" +
//                            "winplayerlevel3[" + arenarecordworldwar.getWinplayer3Level() + "]winserverid[" + arenarecordworldwar.getWinserverId() + "]" +
//                            
//                            "---lostplayerid1[" + arenarecordworldwar.getLostplayer1Id() + "]" +
//                            "lostplayerid2[" + arenarecordworldwar.getLostplayer2Id() + "]" +
//                            "lostplayerid3[" + arenarecordworldwar.getLostplayer3Id() + "]lostarenaid [" + arenarecordworldwar.getLostarenaId() + "]lostarenalevel[" + arenarecordworldwar.getLostarenaLevel() + "]" +
//                            "lostplayerlevel1[" + arenarecordworldwar.getLostplayer1Level() + "]" +
//                            "lostplayerlevel2[" + arenarecordworldwar.getLostplayer2Level() + "]" +
//                            "lostplayerlevel3[" + arenarecordworldwar.getLostplayer3Level() + "]lostserverid[" + arenarecordworldwar.getLostserverId() + "]");

                	break;

                }
                //战斗结束，插入记录表
                arenarecord.setRecordtime(new Date());
                dao.addArenaRecord(arenarecord);
                //WorldWar 战斗结束，插入记录表
//                arenarecordworldwar.setRecordtime(new Date());
//                dao.addArenaRecordWorldWar(arenarecordworldwar);
                
                ArenaTeamTotal arenaTeamTotaltmp = new ArenaTeamTotal();
                teamdao.deleteOwnerduplicate(arenaTeamTotal1);
                arenaTeamTotaltmp = teamdao.getArenaTeam(arenaTeamTotal1);

                if(arenaTeamTotaltmp != null){
                    arenaTeamTotal1.setId(arenaTeamTotaltmp.getId());
                    teamdao.addArenaTeamTotal(arenaTeamTotal1);
                }else{
                    teamdao.addArenaTeamTotal(arenaTeamTotal1);
                }
                teamdao.deleteOwnerduplicate(arenaTeamTotal2);
                arenaTeamTotaltmp = teamdao.getArenaTeam(arenaTeamTotal2);

                if(arenaTeamTotaltmp != null){
                    arenaTeamTotal2.setId(arenaTeamTotaltmp.getId());
                    teamdao.addArenaTeamTotal(arenaTeamTotal2);
                }else{
                    teamdao.addArenaTeamTotal(arenaTeamTotal2);
                }

                //worldwar
//                ArenaTeamTotalWorldWar arenaTeamTotalWorldWartmp = new ArenaTeamTotalWorldWar();
//                try{
//                	teamdao.deleteOwnerduplicateWorldWar(arenaTeamTotalWorldWar1);
//                }catch(Exception e){
//                    log.info("worldwar存储未删除：" + e);
//                    e.printStackTrace();
//                }
//                arenaTeamTotalWorldWartmp = teamdao.getWorldArenaTeam(arenaTeamTotalWorldWar1.getArenaid(),arenaTeamTotalWorldWar1.getServerid());
//
//                if(arenaTeamTotalWorldWartmp != null){
//                	arenaTeamTotalWorldWar1.setId(arenaTeamTotalWorldWartmp.getId());
//                    teamdao.addArenaTeamTotalWorldWar(arenaTeamTotalWorldWar1);
//                }else{
//                    teamdao.addArenaTeamTotalWorldWar(arenaTeamTotalWorldWar1);
//                }
//                try{
//                	teamdao.deleteOwnerduplicateWorldWar(arenaTeamTotalWorldWar2);
//                }catch(Exception e){
//                    log.info("worldwar存储未删除：" + e);
//                    e.printStackTrace();
//                }
//                arenaTeamTotalWorldWartmp = teamdao.getWorldArenaTeam(arenaTeamTotalWorldWar2.getArenaid(),arenaTeamTotalWorldWar2.getServerid());
//
//                if(arenaTeamTotalWorldWartmp != null){
//                	arenaTeamTotalWorldWar2.setId(arenaTeamTotalWorldWartmp.getId());
//                    teamdao.addArenaTeamTotalWorldWar(arenaTeamTotalWorldWar2);
//                }else{
//                    teamdao.addArenaTeamTotalWorldWar(arenaTeamTotalWorldWar2);
//                }
                
                writeDB = 1;
            }else{
                service.removeBattle(this);
            }
        }

    }

    public void abort(){
        sendAbort();
        status = STATUS.end;
        service.removeBattle(this);
    }

    protected boolean isSide1(int playerId){
        for(int i = 0; i < side1.length; i++){
            if(side1[i].id == playerId)
                return true;
        }
        return false;
    }

    public void roundEnd(){
        for(int i = 0; i < side1.length; i++){
            strategy.fillSpriteStatus(side1[i], this);
        }

        for(int i = 0; i < pet1.length; i++){
            if(pet1[i] != null){
                strategy.fillSpriteStatus(pet1[i], this);
            }
        }

        for(int i = 0; i < side2.length; i++){
            strategy.fillSpriteStatus(side2[i], this);
        }

        for(int i = 0; i < pet2.length; i++){
            if(pet2[i] != null){
                strategy.fillSpriteStatus(pet2[i], this);
            }
        }

        for(int i = 0; i < side1.length; i++){
            UWAPSegment seg = getRoundEndSegment(side1[0].id, side1[i].id, (byte) 0);
            service.getArenaService().writeTo(serverId1, seg);
        }

        for(int i = 0; i < side2.length; i++){
            UWAPSegment seg = getRoundEndSegment(side2[0].id, side2[i].id, (byte) 0);
            service.getArenaService().writeTo(serverId2, seg);
        }

        resetFlag();
        battleMovie.clear();

        if(battleOver){
            if(isWinner(side1)){
                winner = getPlayers(side1);
                failure = getPlayers(side2);
            }else{
                winner = getPlayers(side2);
                failure = getPlayers(side1);
            }

            try{
                end();
            }catch(Exception e){
                log.info("存储竞技场结果出错：" + e);
                e.printStackTrace();
            }

            this.status = STATUS.end;
            lastTime = System.currentTimeMillis();
            service.removeBattle(this);
        }else{
            lastTime = System.currentTimeMillis();
            status = STATUS.wait_fight;
            this.round++;
        }
    }

    protected void resetFlag(){
        for(int i = 0; i < side1.length; i++){
            side1[i].used = false;
            side1[i].usedItem = null;
            side1[i].ready = false;
        }

        for(int i = 0; i < side2.length; i++){
            side2[i].used = false;
            side2[i].usedItem = null;
            side2[i].ready = false;
        }
    }

    public synchronized void doTime(long time){
        if(status == STATUS.end)
            return;
        long t = time - lastTime;
        if(status == STATUS.wait_start && t > 60 * 1000L){
            refuse(side2[0].id, (byte) 1, "对方没有回应");
        }else if(status == STATUS.wait_fight && t > Utils.ROUND_TIME_LIMIT){
            //mengjie add 三回合自动出手，死亡。
            for(int i = 0; i < side1.length; i++){
                if(!side1[i].ready){
                    side1[i].idleRound++;

                    if(side1[i].idleRound >= ArenaConstants.MAX_IDLE){
                        if(!side1[i].testCannotBattle()){
                            side1[i].setSkill(Skill.STAY_SKILL);
                            side1[i].changeHp(-side1[i].attributes[BattleSprite.ATTR_HPMAX], battleMovie, this);
                        }

                        if(pet1[i] != null && !pet1[i].testCannotBattle()){
                            pet1[i].setSkill(Skill.STAY_SKILL);
                            pet1[i].changeHp(-pet1[i].attributes[BattleSprite.ATTR_HPMAX], battleMovie, this);
                        }
                    }else{
                        if(side1[i].canAction()){
                            side1[i].setSkill(Skill.ATTACK_SKILL);
                            side1[i].setTarget(side2[0], 0);
                        }

                        if(pet1[i] != null && pet1[i].canAction()){
                            pet1[i].setSkill(Skill.ATTACK_SKILL);
                            pet1[i].setTarget(side2[0], 0);
                        }
                    }

                    side1[i].ready = true;
                }
            }

            for(int i = 0; i < side2.length; i++){
                if(!side2[i].ready){
                    side2[i].idleRound++;

                    if(side2[i].idleRound >= ArenaConstants.MAX_IDLE){
                        if(!side2[i].testCannotBattle()){
                            side2[i].setSkill(Skill.STAY_SKILL);
                            side2[i].changeHp(-side2[i].attributes[BattleSprite.ATTR_HPMAX], battleMovie, this);
                        }

                        if(pet2[i] != null && !pet2[i].testCannotBattle()){
                            pet2[i].setSkill(Skill.STAY_SKILL);
                            pet2[i].changeHp(-pet2[i].attributes[BattleSprite.ATTR_HPMAX], battleMovie, this);
                        }
                    }else{
                        if(side2[i].canAction()){
                            side2[i].setSkill(Skill.ATTACK_SKILL);
                            side2[i].setTarget(side1[0], 0);
                        }

                        if(pet2[i] != null && pet2[i].canAction()){
                            pet2[i].setSkill(Skill.ATTACK_SKILL);
                            pet2[i].setTarget(side1[0], 0);
                        }
                    }

                    side2[i].ready = true;
                }
            }

            setDefaultAction(side1, pet1, side2);
            setDefaultAction(side2, pet2, side1);

            try{
                battleBout(side1, side2, pet1, pet2, round);
                roundEnd();
                clearBourt();
            }catch(Exception ex){
                caughtException(ex);
            }
        }

    }

    protected void setDefaultAction(BattleSprite[] bs1, BattleSprite[] bsPet, BattleSprite[] bs2){
        for(int i = 0; i < bs1.length; i++){
            if(!bs1[i].ready){
                if(bs1[i].canAction()){
                    bs1[i].setSkill(Skill.ATTACK_SKILL);
                    bs1[i].setTarget(bs2[0], 0);
                    if(bsPet[i] != null && bsPet[i].canAction()){
                        bsPet[i].setSkill(Skill.ATTACK_SKILL);
                        bsPet[i].setTarget(bs2[0], 0);
                    }
                }
            }
        }
    }

    public void process(UWAPData data, int playerId){
        byte type = data.getAppType();
        if(type == ArenaConstants.CONN_ARENA_BATTLE_FIGHT){
            try{
                short roundId = data.readShort();
                int action = data.readInt();
                byte target = data.readByte();
                int petAction = data.readInt();
                byte petTarget = data.readByte();
                fight(playerId, roundId, action, target, petAction, petTarget);
            }catch(IllegalAccessException ex){
            }
        }
    }

    public void catchToBattle(int playerId){
        if(status == STATUS.wait_fight){
            int serverId = serverId1;
            int otherServerId = serverId2;
            int ownerId = ownerId1;
            int sidePlayerId = side1[0].id;

            if(!isSide1(playerId)){
                serverId = serverId2;
                otherServerId = serverId1;
                ownerId = ownerId2;
                sidePlayerId = side2[0].id;
            }

            UWAPSegment seg = getPkStartSegment(ownerId, playerId, service.getArenaService().getServerName(otherServerId));
            service.getArenaService().writeTo(serverId, seg);
            seg = getRoundEndSegment(sidePlayerId, playerId, (byte) 1);
            service.getArenaService().writeTo(serverId, seg);
        }
    }

    public BattleSprite getSprite(int spriteType, int spriteIndex){
        BattleSprite result = null;
        switch(spriteType){
            case BattleSprite.TYPE_PLAYER:
                result = side1[spriteIndex];
                break;
            case BattleSprite.TYPE_PLAYER_PET:
                result = pet1[spriteIndex];
                break;
            case BattleSprite.TYPE_MONSTER:
                result = side2[spriteIndex];
                break;
            case BattleSprite.TYPE_MONSTER_PET:
                result = pet2[spriteIndex];
                break;
        }
        return result;
    }

    protected void sendAbort(){
        for(int i = 0; i < side1.length; i++){
            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_BATTLE_ABORT);
            seg.writeInt(type);
            seg.writeInt(ownerId1);
            seg.writeInt(side1[i].id);

            seg.writeInt(getId());
            seg.writeInt(side1[i].hp);
            seg.writeInt(side1[i].mp);
            seg.writeInt(pet1[i] == null? -1: pet1[i].hp);
            seg.writeInt(pet1[i] == null? -1: pet1[i].mp);
            service.getArenaService().writeTo(serverId1, seg);
        }

        for(int i = 0; i < side2.length; i++){
            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_BATTLE_ABORT);
            seg.writeInt(type);
            seg.writeInt(ownerId2);
            seg.writeInt(side2[i].id);

            seg.writeInt(getId());
            seg.writeInt(side2[i].hp);
            seg.writeInt(side2[i].mp);
            seg.writeInt(pet2[i] == null? -1: pet2[i].hp);
            seg.writeInt(pet2[i] == null? -1: pet2[i].mp);
            service.getArenaService().writeTo(serverId2, seg);
        }
    }

    protected void prepareEnmities(BattleSprite[] our, BattleSprite[] ourPet, BattleSprite[] them, BattleSprite[] themPet){
        for(int i = 0; i < our.length; i++){
            Skill skill = our[i].skill;
            int enmity = skill.enmity;

            for(int j = 0; j < them.length; j++){
                them[j].addEnmity(our[i], enmity);

                if(themPet[j] != null){
                    themPet[j].addEnmity(our[i], enmity);
                }

                if(ourPet[i] != null){
                    Skill petSkill = ourPet[i].skill;
                    int petEnmity = petSkill.enmity;
                    them[j].addEnmity(ourPet[i], petEnmity);

                    if(themPet[j] != null){
                        themPet[j].addEnmity(our[i], enmity);
                    }
                }
            }
        }
    }

    protected boolean testOurSideFromOrder(int oppGroup){
        if(oppGroup == BattleSprite.GROUP_OUR || oppGroup == BattleSprite.GROUP_OUR_PET){
            return true;
        }else{
            return false;
        }
    }

    protected BattleSprite getSpriteFromOrder(int oppGroup, int oppIndex, BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet){
        BattleSprite result = null;

        switch(oppGroup){
            case BattleSprite.GROUP_OUR:
                result = our[oppIndex];
                break;
            case BattleSprite.GROUP_THEM:
                result = them[oppIndex];
                break;
            case BattleSprite.GROUP_OUR_PET:
                result = ourPet[oppIndex];
                break;
            case BattleSprite.GROUP_THEM_PET:
                result = themPet[oppIndex];
                break;
        }
        return result;
    }

    protected boolean battleBout(BattleSprite[] our, BattleSprite[] them, BattleSprite[] ourPet, BattleSprite[] themPet, int bout){
        try{
            battleMovie = new Vector();
            spriteDoneSkill.clear();

            prepareEnmities(our, ourPet, them, themPet);
            //神圣宝辉减防
            updateDefEffectTime(our);
            updateDefEffectTime(ourPet);
            updateDefEffectTime(them);
            updateDefEffectTime(themPet);
            
            int[][] battleOrder = new int[our.length + them.length + ourPet.length + themPet.length][2]; //[0] 0：our，1：them，2: ourPete, 3: themPet, [1] index
            int offset = 0;

            for(int i = 0; i < our.length; i++){
                battleOrder[i][0] = BattleSprite.GROUP_OUR;
                battleOrder[i][1] = i - offset;

                if(our[i - offset] != null){
                    our[i - offset].groupIndex = i - offset;
                }
            }

            offset += our.length;

            for(int i = offset; i < offset + them.length; i++){
                battleOrder[i][0] = BattleSprite.GROUP_THEM;
                battleOrder[i][1] = i - offset;

                if(them[i - offset] != null){
                    them[i - offset].groupIndex = i - offset;
                }
            }

            offset += them.length;

            for(int i = offset; i < offset + ourPet.length; i++){
                battleOrder[i][0] = BattleSprite.GROUP_OUR_PET;
                battleOrder[i][1] = i - offset;

                if(ourPet[i - offset] != null){
                    ourPet[i - offset].groupIndex = i - offset;
                }
            }

            offset += ourPet.length;

            for(int i = offset; i < offset + themPet.length; i++){
                battleOrder[i][0] = BattleSprite.GROUP_THEM_PET;
                battleOrder[i][1] = i - offset;

                if(themPet[i - offset] != null){
                    themPet[i - offset].groupIndex = i - offset;
                }
            }

            battleOver = false;

            for(int i = 0; i < battleOrder.length; i++){
                boolean flag = testOurSideFromOrder(battleOrder[i][0]);

                BattleSprite bs = getSpriteFromOrder(battleOrder[i][0], battleOrder[i][1], our, them, ourPet, themPet);

                if(bs == null){
                    continue;
                }

                if(bs.skill == Skill.NOTREADY_SKILL && !bs.testCannotBattle()){
                    if(flag){
                        battleOver = strategy.chooseSkill(bs, battleOrder[i][1], our, them, ourPet, themPet, battleMovie, this, round);
                    }else{
                        battleOver = strategy.chooseSkill(bs, battleOrder[i][1], them, our, themPet, ourPet, battleMovie, this, round);
                    }
                }

                if(battleOver){
                    break;
                }
            }

            if(battleOver){
                return true;
            }

            battleOver = false;

            for(int i = 0; i < battleOrder.length - 1; i++){
                for(int j = i; j < battleOrder.length; j++){
                    BattleSprite t1, t2;

                    t1 = getSpriteFromOrder(battleOrder[i][0], battleOrder[i][1], our, them, ourPet, themPet);
                    t2 = getSpriteFromOrder(battleOrder[j][0], battleOrder[j][1], our, them, ourPet, themPet);

                    if(t1 == null || t2 == null){
                        if(t1 == null){
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        }

                        continue;
                    }

                    int speed1 = t1.getSpeed();
                    int speed2 = t2.getSpeed();

                    if(speed1 < speed2){
                        if((t1.canAction() && t2.canAction()) || (!t1.canAction() && !t2.canAction())){
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        }else if(!t1.canAction()){
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        }
                    }else if(speed1 == speed2){
                        if(Skill.getPercentRate(50)){
                            int[] tmp = battleOrder[i];
                            battleOrder[i] = battleOrder[j];
                            battleOrder[j] = tmp;
                        }
                    }
                }
            }

            battleOver = false;
            battleRecorders = new Vector();

            for(int i = 0; i < battleOrder.length; i++){
                boolean flag = testOurSideFromOrder(battleOrder[i][0]);

                BattleSprite bs = getSpriteFromOrder(battleOrder[i][0], battleOrder[i][1], our, them, ourPet, themPet);

                if(bs == null){
                    continue;
                }

                if(bs.skill != Skill.NOTREADY_SKILL && !bs.testCannotBattle()){
                    if(flag){
                        battleOver = strategy.doPoisonFrost(bs, battleOrder[i][1], our, them, ourPet, themPet, battleMovie, this);
                    }else{
                        battleOver = strategy.doPoisonFrost(bs, battleOrder[i][1], them, our, themPet, ourPet, battleMovie, this);
                    }

                    if(bs.testCannotBattle()){
                        continue;
                    }else{
                        if(!bs.canAction()){
                            if(flag){
                                battleOver = strategy.chooseSkill(bs, battleOrder[i][1], our, them, ourPet, themPet, battleMovie, this, round);
                            }else{
                                battleOver = strategy.chooseSkill(bs, battleOrder[i][1], them, our, themPet, ourPet, battleMovie, this, round);
                            }

                            if(battleOver){
                                break;
                            }
                        }

                        if(flag){
                            battleOver = strategy.doSkill(bs, our, them, ourPet, themPet, battleMovie, bout, battleRecorders, this);
                        }else{
                            battleOver = strategy.doSkill(bs, them, our, themPet, ourPet, battleMovie, bout, battleRecorders, this);
                        }

                        if(battleOver){
                            break;
                        }
                    }
                }

                if(battleOver){
                    break;
                }
            }

            if(battleOver){
                return true;
            }

            battleOver = true;

            for(int i = 0; i < our.length; i++){
                if(our[i] == null){
                    continue;
                }

                if(!our[i].testCannotBattle()){
                    battleOver = false;

                    break;
                }
            }

            if(battleOver){
                return true;
            }

            battleOver = true;

            for(int i = 0; i < them.length; i++){
                if(them[i] == null){
                    continue;
                }

                if(!them[i].testCannotBattle()){
                    battleOver = false;

                    break;
                }
            }

            if(battleOver){
                return true;
            }

            return false;
        }finally{
            Enumeration emu = spriteDoneSkill.keys();

            while(emu.hasMoreElements()){
                BattleSprite bs = (BattleSprite) emu.nextElement();
                Integer groupIndex = (Integer) spriteDoneSkill.get(bs);

                bs.processBattleBuf(battleMovie, groupIndex.intValue(), this);
            }
        }
    }

    protected void clearBourt(){
        for(int i = 0; i < side1.length; i++){
            BattleSprite bs = side1[i];

            if(bs == null){
                continue;
            }
            bs.usedItem = null;
            bs.used = false;
            bs.clearBout(battleMovie, i, this);
        }

        for(int i = 0; i < side2.length; i++){
            BattleSprite bs = side2[i];

            if(bs == null){
                continue;
            }
            bs.usedItem = null;
            bs.used = false;
            bs.clearBout(battleMovie, i, this);
        }

        for(int i = 0; i < pet1.length; i++){
            BattleSprite bs = pet1[i];

            if(bs == null){
                continue;
            }

            bs.clearBout(battleMovie, i, this);
        }

        for(int i = 0; i < pet2.length; i++){
            BattleSprite bs = pet2[i];

            if(bs == null){
                continue;
            }

            bs.clearBout(battleMovie, i, this);
        }
    }

    public BattleStrategy getStrategy(){
        return strategy;
    }

    public int getId(){
        return id;
    }

    public BattleSprite[] getSide1(){
        return side1;
    }

    public BattleSprite[] getSide2(){
        return side2;
    }

    public int getRound(){
        return round;
    }

    public void spriteDoneSkill(BattleSprite bs, int index, boolean force){
        if(force || ((bs.getDebufStatus() != Skill.STATUS_NORMAL || bs.getBufStatus() != Skill.STATUS_NORMAL || bs.bufTable.size() > 2) && !bs.testCannotBattle())){
            spriteDoneSkill.put(bs, new Integer(index));
        }
    }

    protected int getItemFlag(BattleSprite bs){
        if(bs.used && bs.usedItem != null){
            return (bs.usedItem.getItemId() << 16) | 1;
        }
        return 0;
    }

    public int hashCode(){
        return id;
    }

    protected boolean checkPetAction(BattleSprite owner, BattleSprite bs, int action){
        if(action == Skill.SKILL_NOT_READY){
            bs.setSkill(Skill.STAY_SKILL);

            return false;
        }else{
            if(!bs.canAction()){
                if(action != Skill.SKILL_STAY){
                    bs.setSkill(Skill.ATTACK_SKILL);
                    bs.setTarget(bs, bs.groupIndex);
                    log.info("ID[" + owner.id + "]BattleError PetAction Skill[" + action + "]");

                    return false;
                }else{
                    bs.setSkill(Skill.STAY_SKILL);

                    return false;
                }
            }else{
                Skill skill = Skill.getSkill(action);
                if(skill == null){
                    bs.setSkill(Skill.ATTACK_SKILL);
                    bs.setTarget(bs, bs.groupIndex);
                    log.info("ID[" + owner.id + "]BattleError PetSkillNotFound Skill[" + action + "]");

                    return false;
                }else{
                    if(hasPetSkill(bs, skill)){
                        int[] status = Skill.getSkillStatus(bs, action);
                        if (status[0] != Skill.CAN_SELECT_SKILL || status[1] > 0 || status[2] > 0) {
                            bs.setSkill(Skill.ATTACK_SKILL);
                            bs.setTarget(bs, bs.groupIndex);
                            log.info("ID[" + owner.id + "]BattleError PetSkillCantSelect Skill[" + action + "]");

                            return false;
                        }else{
                            bs.setSkill(skill);
                            return true;
                        }
                    }else{
                        bs.setSkill(Skill.ATTACK_SKILL);
                        bs.setTarget(bs, bs.groupIndex);
                        log.info("ID[" + owner.id + "]BattleError PetSkillNotContain Skill[" + action + "]");

                        return false;
                    }
                }
            }
        }
    }

    protected boolean checkAction(BattleSprite bs, int action, int itemId){
        if(action == Skill.SKILL_NOT_READY){
            bs.setSkill(Skill.STAY_SKILL);

            return false;
        }

        if(!bs.canAction()){
            if(action != Skill.SKILL_STAY){
                bs.setSkill(Skill.ATTACK_SKILL);
                bs.setTarget(bs, bs.groupIndex);
                log.info("ID[" + bs.id + "]BattleError Action Skill[" + action + "]");

                return false;
            }else{
                bs.setSkill(Skill.STAY_SKILL);

                return false;
            }
        }else{
            if(action == Skill.SKILL_ITEM){
                IItemTemplate template = Items.getTemplate(itemId);

                if(template == null){
                    log.info("ID[" + bs.id + "]BattleError UseItemNotFound ItemId[" + itemId + "]");
                    bs.setSkill(Skill.ATTACK_SKILL);
                    bs.setTarget(bs, bs.groupIndex);

                    return false;
                }else{
                    IItem item = template.newInstance();

                    if(bs.player.hasItem(item, 1)){
                        if(bs.getStatus(BattleSprite.SEAL_SKILL_ITEM)){
                            bs.setSkill(Skill.ATTACK_SKILL);
                            bs.setTarget(bs, bs.groupIndex);
                            log.info("ID[" + bs.id + "]BattleError UseItemCannotUse ItemId[" + itemId + "]");

                            return false;
                        }else{
                            bs.usedItem = item;
                            bs.setSkill(Skill.getSkill(action));

                            return true;
                        }
                    }else{
                        bs.setSkill(Skill.ATTACK_SKILL);
                        bs.setTarget(bs, bs.groupIndex);
                        log.info("ID[" + bs.id + "]BattleError UseItemNumber ItemId[" + itemId + "]");

                        return false;
                    }
                }
            }else{
                Skill skill = Skill.getSkill(action);

                if(skill == null){
                    bs.setSkill(Skill.ATTACK_SKILL);
                    bs.setTarget(bs, bs.groupIndex);
                    log.info("ID[" + bs.id + "]BattleError SkillNotFound Skill[" + action + "]");

                    return false;
                }else{
                    if(hasSkill(bs, skill)){
                        int[] status = Skill.getSkillStatus(bs, action);

                        if (status[0] != Skill.CAN_SELECT_SKILL || status[1] > 0 || status[2] > 0) {
                            bs.setSkill(Skill.ATTACK_SKILL);
                            bs.setTarget(bs, bs.groupIndex);
                            log.info("ID[" + bs.id + "]BattleError SkillCantSelect Skill[" + action + "]");

                            return false;
                        }else{
                            bs.setSkill(skill);

                            return true;
                        }
                    }else{
                        bs.setSkill(Skill.ATTACK_SKILL);
                        bs.setTarget(bs, bs.groupIndex);
                        log.info("ID[" + bs.id + "]BattleError SkillNotContain Skill[" + action + "]");

                        return false;
                    }
                }
            }
        }
    }

    protected boolean hasSkill(BattleSprite bs, Skill skill){
        if(skill == Skill.ATTACK_SKILL || skill == Skill.CATCH_SKILL || skill == Skill.ITEM_SKILL || skill == Skill.NOTREADY_SKILL || skill == Skill.RUNAWAY_SKILL || skill == Skill.STAY_SKILL){
            return true;
        }

        Ability ability = Ability.getAbility(skill.id);

        if(ability == null){
            return false;
        }

        return bs.player.containsAbility(ability);
    }

    protected boolean hasPetSkill(BattleSprite bs, Skill skill){
        if(skill == Skill.ATTACK_SKILL || skill == Skill.NOTREADY_SKILL || skill == Skill.STAY_SKILL){
            return true;
        }

        Ability ability = Ability.getAbility(skill.id);

        if(ability == null){
            return false;
        }

        return bs.pet.hasAbility(ability);
    }

    protected UWAPSegment getPkStartSegment(int ownerId, int playerId, String otherServerName){
        boolean side1Segment = false;
        
        if(isSide1(playerId)){
            side1Segment = true;
        }
        
        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_BATTLE_START);
        seg.writeInt(type);
        seg.writeInt(ownerId);
        seg.writeInt(playerId);
        seg.writeInt(id);

        seg.writeInt(id);
        seg.write((byte) side1.length);
        
        for(int i = 0; i < side1.length; i++){
            seg.writeInt(side1[i].id);
        }

        seg.write((byte) side1.length);
        
        for(int i = 0; i < side1.length; i++){
            seg.writeInt(side1[i].id);
            
            if(side1Segment){
                seg.writeString(side1[i].name);
            }else{
                seg.writeString(side1[i].name + "(" + otherServerName + ")");
            }
            
            seg.writeInt(side1[i].hp);
            seg.writeInt(side1[i].mp);
            seg.writeInt(side1[i].attributes[BattleSprite.ATTR_HPMAX]);
            seg.writeInt(side1[i].attributes[BattleSprite.ATTR_MPMAX]);
            seg.write(side1[i].face);
        
            EquipmentTemplate weapon = side1[i].weapon;
            
            if(weapon == null){
                seg.write((byte) -1);
            }else{
                seg.write((byte) weapon.getProperty(30)); //武器类型
            }
            
            seg.writeInt(side1[i].getAllStatus());
            seg.writeBoolean(side1[i].canAction());
            seg.write(side1[i].player.getLightLevel());
            if(pet1[i] != null){
                seg.write((byte) pet1[i].pet.getPetType());
                seg.writeInt(pet1[i].hp);
                seg.writeInt(pet1[i].mp);
                seg.writeInt(pet1[i].pet.getMaxHp());
                seg.writeInt(pet1[i].pet.getMaxMp());
                seg.writeString(pet1[i].pet.getName());
                seg.writeInt(pet1[i].getAllStatus());
                seg.writeBoolean(pet1[i].canAction());
                //2代形象
                seg.write(pet1[i].pet.getBindType());
                seg.writeInt(pet1[i].level);
                seg.writeShort(pet1[i].pet.getColorIndex());
                //3代形象
                seg.writeInt(pet1[i].pet.getEvolutionLevel());
                seg.writeInt(pet1[i].pet.getEvolutionType());
            }else{
                seg.write((byte) -1);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeString("");
                seg.writeInt(0);
                seg.writeBoolean(false);
                seg.write((byte)0);
                seg.writeInt(0);
                seg.writeShort((short)0);
                seg.writeInt(0);
                seg.writeInt(0);
            }
        }
        
        seg.write((byte) side2.length);
        
        for(int i = 0; i < side2.length; i++){
            seg.writeInt(side2[i].id);
            
            if(side1Segment){
                seg.writeString(side2[i].name + "(" + otherServerName + ")");
            }else{
                seg.writeString(side2[i].name);
            }

            seg.writeInt(side2[i].hp);
            seg.writeInt(side2[i].mp);
            seg.writeInt(side2[i].attributes[BattleSprite.ATTR_HPMAX]);
            seg.writeInt(side2[i].attributes[BattleSprite.ATTR_MPMAX]);
            seg.write(side2[i].face);
            
            EquipmentTemplate weapon = side2[i].weapon;
            
            if(weapon == null){
                seg.write((byte) -1);
            }else{
                seg.write((byte) weapon.getProperty(30));
            }
            
            seg.writeInt(side2[i].getAllStatus());
            seg.writeBoolean(side2[i].canAction());
            seg.write(side2[i].player.getLightLevel());
            if(pet2[i] != null){
                seg.write((byte) pet2[i].pet.getPetType());
                seg.writeInt(pet2[i].hp);
                seg.writeInt(pet2[i].mp);
                seg.writeInt(pet2[i].pet.getMaxHp());
                seg.writeInt(pet2[i].pet.getMaxMp());
                seg.writeString(pet2[i].pet.getName());
                seg.writeInt(pet2[i].getAllStatus());
                seg.writeBoolean(pet2[i].canAction());
                //2代形象
                seg.write(pet2[i].pet.getBindType());
                seg.writeInt(pet2[i].level);
                seg.writeShort(pet2[i].pet.getColorIndex());
                //3代形象
                seg.writeInt(pet2[i].pet.getEvolutionLevel());
                seg.writeInt(pet2[i].pet.getEvolutionType());
            }else{
                seg.write((byte) -1);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeString("");
                seg.writeInt(0);
                seg.writeBoolean(false);
                seg.write((byte)0);
                seg.writeInt(0);
                seg.writeShort((short)0);
                seg.writeInt(0);
                seg.writeInt(0);
            }
        }
        
        seg.writeShort((short) round);
        
        return seg;
    }

    protected UWAPSegment getRoundEndSegment(int sidePlayerId, int playerId, byte type){
        BattleSprite[] p1 = side1;
        BattleSprite[] pe1 = pet1;
        BattleSprite[] p2 = side2;
        BattleSprite[] pe2 = pet2;
        int ownerId = ownerId1;

        if(sidePlayerId == side2[0].id){
            p1 = side2;
            pe1 = pet2;
            p2 = side1;
            pe2 = pet1;
            ownerId = ownerId2;
        }

        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_ROUND_END);

        seg.writeInt(this.type);
        seg.writeInt(ownerId);
        seg.writeInt(playerId);

        seg.write(type);
        seg.writeInt(getId());
        if(type == 1){
            seg.writeShort((short) (round - 1));
        }else
            seg.writeShort((short) round);
        if(type == 0){
            Vector tmpBattleMove = new Vector();
                
            for(int i = 0; i < battleMovie.size(); i++){
                int[] tmpArray = (int[])battleMovie.get(i);
                int[] tmpArray1 = new int[tmpArray.length];
                System.arraycopy(tmpArray, 0, tmpArray1, 0, tmpArray.length);
                tmpBattleMove.add(tmpArray1);
            }
            
            if(sidePlayerId==side2[0].id){
                makeMovie(tmpBattleMove);
            }
            seg.write((byte) tmpBattleMove.size());
            for (int i = 0; i < tmpBattleMove.size(); i++) {
                seg.writeInts((int[]) tmpBattleMove.get(i));
            }
        }
        
        seg.writeInt(p1.length + p2.length);

        byte index = 1;

        for(int i = 0; i < p1.length; i++){
            seg.write((byte) index++);
            seg.writeInt(p1[i].getAllStatus());
            //mengjie add
            seg.writeInt(p1[i].id);
            seg.writeInt(p1[i].hp);
            seg.writeInt(p1[i].mp);
            seg.writeBoolean(p1[i].canAction());
            seg.writeInt(getItemFlag(p1[i]));
            seg.writeString(p1[i].getSkillName());
            if(pe1[i] != null){
                seg.writeInt(pe1[i].hp);
                seg.writeInt(pe1[i].mp);
                seg.writeInt(pe1[i].getAllStatus());
                seg.writeBoolean(pe1[i].canAction());
                seg.writeString(pe1[i].getSkillName());
            }else{
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeBoolean(false);
                seg.writeString("");
            }
        }

        index = -1;

        for(int i = 0; i < p2.length; i++){
            seg.write((byte) index--);
            seg.writeInt(p2[i].getAllStatus());
            //mengjie add
            seg.writeInt(p2[i].id);
            seg.writeInt(p2[i].hp);
            seg.writeInt(p2[i].mp);
            seg.writeBoolean(p2[i].canAction());
            seg.writeInt(getItemFlag(p2[i]));
            seg.writeString(p2[i].getSkillName());
            if(pe2[i] != null){
                seg.writeInt(pe2[i].hp);
                seg.writeInt(pe2[i].mp);
                seg.writeInt(pe2[i].getAllStatus());
                seg.writeBoolean(pe2[i].canAction());
                seg.writeString(pe2[i].getSkillName());
            }else{
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeInt(0);
                seg.writeBoolean(false);
                seg.writeString("");
            }
        }

        if(type == 0){
            seg.write(getRoundStatus(p1));
        }
        
        syncCDInfo(seg);

        return seg;
    }
    
    //Added by leo for sync CD info
    public void syncCDInfo(UWAPSegment seg){
        seg.write((byte)(side1.length * 2 + side2.length * 2));
        for(int i = 0; i < side1.length; i++){
            seg.writeInt(side1[i].id);
            seg.write(side1[i].getCoolDownInfo());
            if(pet1[i] != null){
                seg.writeInt(pet1[i].id);
                seg.write(pet1[i].getCoolDownInfo());
            }else{
                seg.writeInt(-1);
                seg.write(new byte[0]);
            }
        }
        for(int i = 0; i < side2.length; i++){
            seg.writeInt(side2[i].id);
            seg.write(side2[i].getCoolDownInfo());
            if(pet2[i] != null){
                seg.writeInt(pet2[i].id);
                seg.write(pet2[i].getCoolDownInfo());
            }else{
                seg.writeInt(-1);
                seg.write(new byte[0]);
            }
        }
    }

    protected byte getRoundStatus(BattleSprite[] bs){
        if(!battleOver){
            return 0;
        }else{
            boolean win = true;
            for(int i = 0; i < bs.length; i++){
                if(!bs[i].testCannotBattle()){
                    win = false;
                }
            }
            if(win)
                return 1;
            else
                return 2;
        }
    }

    public void makeMovie(Vector movie){
        for(int i = 0; i < movie.size(); i++){
            int[] m = (int[]) movie.get(i);
            if(m[0] == m[2]){
                if(m[0] == BattleSprite.TYPE_PLAYER){
                    m[0] = m[2] = BattleSprite.TYPE_MONSTER;
                }else if(m[0] == BattleSprite.TYPE_MONSTER){
                    m[0] = m[2] = BattleSprite.TYPE_PLAYER;
                }else if(m[0] == BattleSprite.TYPE_PLAYER_PET){
                    m[0] = m[2] = BattleSprite.TYPE_MONSTER_PET;
                }else if(m[0] == BattleSprite.TYPE_MONSTER_PET){
                    m[0] = m[2] = BattleSprite.TYPE_PLAYER_PET;
                }
            }else{
                if(m[0] == BattleSprite.TYPE_PLAYER){
                    m[0] = BattleSprite.TYPE_MONSTER;
                }else if(m[0] == BattleSprite.TYPE_PLAYER_PET){
                    m[0] = BattleSprite.TYPE_MONSTER_PET;
                }else if(m[0] == BattleSprite.TYPE_MONSTER){
                    m[0] = BattleSprite.TYPE_PLAYER;
                }else if(m[0] == BattleSprite.TYPE_MONSTER_PET){
                    m[0] = BattleSprite.TYPE_PLAYER_PET;
                }

                if(m[2] == BattleSprite.TYPE_PLAYER){
                    m[2] = BattleSprite.TYPE_MONSTER;
                }else if(m[2] == BattleSprite.TYPE_PLAYER_PET){
                    m[2] = BattleSprite.TYPE_MONSTER_PET;
                }else if(m[2] == BattleSprite.TYPE_MONSTER){
                    m[2] = BattleSprite.TYPE_PLAYER;
                }else if(m[2] == BattleSprite.TYPE_MONSTER_PET){
                    m[2] = BattleSprite.TYPE_PLAYER_PET;
                }
            }
        }
    }

    protected void refuse(int playerId, byte code, String cause){
    }

    protected void caughtException(Exception ex){
        log.error(ex, ex);
        abort();
    }

    public synchronized void fight(int playerId, short roundId, int action, byte target, int petAction, byte petTarget){
        if(status != STATUS.wait_fight){
            return;
        }

        if(roundId != round){
            return;
        }
        BattleSprite bs = getBattlePlayer(playerId);

        if(bs != null){
            BattleSprite pet = getBattlePet(playerId);
            bs.idleRound = 0;
            int realAction = (short) (action & 0xFFFF);
            int itemId = (action >> 16) & 0xFFFF;

            if(checkAction(bs, realAction, itemId)){
                bs.setTarget(getTarget(playerId, target), getTargetIndex(target));
            }

            int realPetAction = (short) (petAction & 0xFFFF);

            if(realPetAction == Skill.SKILL_NOT_READY){
                realPetAction = Skill.SKILL_STAY;
            }

            if(pet != null){
                if(checkPetAction(bs, pet, realPetAction)){
                    pet.setTarget(getTarget(playerId, petTarget), getTargetIndex(petTarget));
                }
            }

            bs.ready = true;

            try{
                if(isReady()){
                	//竞技场记录玩家的战斗使用技能日志
                	StringBuffer battleRecordBuffer = new StringBuffer();
                	battleRecordBuffer.append("ArenaBattle records playerId [" + playerId + "] round [" + round + "] action [" + action + "] target[ "
                			+ target + "] petaction [" + petAction + "] pettarget [" + petTarget + "]");
                	log.info(battleRecordBuffer.toString());
                	
                    battleBout(side1, side2, pet1, pet2, round);
                    roundEnd();
                    clearBourt();
                }
            }catch(Exception ex){
                caughtException(ex);
            }
        }
    }

    protected boolean isReady(){
        for(int i = 0; i < side1.length; i++){
            if(!side1[i].ready){
                return false;
            }
        }

        for(int i = 0; i < side2.length; i++){
            if(!side2[i].ready){
                return false;
            }
        }
        return true;
    }

    protected BattleSprite getBattlePlayer(int id){
        for(int i = 0; i < side1.length; i++){
            if(side1[i].id == id){
                return side1[i];
            }
        }

        for(int i = 0; i < side2.length; i++){
            if(side2[i].id == id){
                return side2[i];
            }
        }

        return null;
    }

    protected BattleSprite getBattlePet(int id){
        for(int i = 0; i < side1.length; i++){
            if(side1[i].id == id){
                return pet1[i];
            }
        }

        for(int i = 0; i < side2.length; i++){
            if(side2[i].id == id){
                return pet2[i];
            }
        }

        return null;
    }

    private int getTargetIndex(byte index){
        if(index < -10){
            return (-index) - 10 - 1;
        }

        if(index > 10){
            return index - 10 - 1;
        }

        if(index < 0){
            return (-index) - 1;
        }

        if(index > 0){
            return index - 1;
        }

        return 0;
    }

    protected BattleSprite getTarget(int playerId, byte index){
        try{
            if(isSide1(playerId)){
                if(index < -10){
                    int ii = (-index) - 10;

                    return pet2[ii - 1];
                }

                if(index > 10){
                    return pet1[index - 10 - 1];
                }

                if(index < 0){
                    return side2[(-index) - 1];
                }

                if(index > 0){
                    return side1[index - 1];
                }
            }else{
                if(index < -10){
                    int ii = (-index) - 10;

                    return pet1[ii - 1];
                }

                if(index > 10){
                    return pet2[index - 10 - 1];
                }

                if(index < 0){
                    return side1[(-index) - 1];
                }

                if(index > 0){
                    return side2[index - 1];
                }
            }
        }catch(Exception ex){
        }

        return null;
    }

    protected boolean isWinner(BattleSprite[] side){
        for(int i = 0; i < side.length; i++){
            if(side[i].getDebufStatus() != Skill.STATUS_DIE)
                return true;
        }
        return false;
    }

    protected boolean isLoser(BattleSprite[] side){
        for(int i = 0; i < side.length; i++){
            if(side[i].getDebufStatus() == Skill.STATUS_DIE)
                return true;
        }
        return false;
    }

    protected IPlayerData[] getPlayers(BattleSprite[] bs){
    	IPlayerData[] ret = new IPlayerData[bs.length];

        for(int i = 0; i < ret.length; i++){
            ret[i] = bs[i].player;
        }

        return ret;
    }
    
    public void updateDefEffectTime(BattleSprite[] bs){
    	for(int i = 0; i < bs.length; i++){
    		if(bs[i] != null && !bs[i].testCannotBattle()){
    			bs[i].updateCurDefEffectRate();
    		}
    	}
    }
}
