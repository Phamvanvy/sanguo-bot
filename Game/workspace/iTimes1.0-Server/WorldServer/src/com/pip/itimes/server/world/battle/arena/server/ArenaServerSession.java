package com.pip.itimes.server.world.battle.arena.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;

import com.pip.itimes.net.Packet;
import com.pip.itimes.net.Session;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.bean.ArenaTeamTotal;
import com.pip.itimes.server.bean.ArenaTeamTotalWorldWar;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.bean.TaskData;
import com.pip.itimes.server.stage.Ability;
import com.pip.itimes.server.stage.EquipmentTemplate;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.stage.PetEnhance;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.suit.SuitEffect;
import com.pip.itimes.server.suit.Suits;
import com.pip.itimes.server.world.StageService;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.BattleSuitEffect;
import com.pip.itimes.server.world.battle.arena.ArenaConstants;
import com.pip.itimes.server.world.battle.arena.ArenaTools;
import com.pip.itimes.server.world.battle.arena.ArenaWorldDetail;
import com.pip.itimes.server.world.lyricsSystem.LyricData;
import com.pip.itimes.server.world.lyricsSystem.LyricDataServer;
import com.pip.itimes.server.world.transfer.Equipment;

public class ArenaServerSession extends Session{
    private static final Logger log = Logger.getLogger(ArenaServerSession.class);

    private int id;
    private String name;
    private boolean shutdown = false;
    private ArenaService serverService;
    private StageService stageService;
    protected static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
    private ArenaServerService arenaServerService;

    public ArenaServerSession(IoSession session){
        super(session);
    }

    public void setArenaServerService(ArenaServerService arenaServerService){
        this.arenaServerService = arenaServerService;
    }

    public void setServerService(ArenaService serverService){
        this.serverService = serverService;
    }

    public void setStageService(StageService stageService){
        this.stageService = stageService;
    }

    public void closed(){
        if(!shutdown){
            arenaServerService.removeServer(getId());
            serverService.removeWorld(this);
        }
    }

    public void created(){
        serverService.addWorld(this);
    }

    public void handle(Packet packet){
        UWAPData data = packet.datas[0];
        byte type = data.getAppType();

        try{
            switch(type){
                case ArenaConstants.CONN_ARENA_WORLD_LOGIN:
                    worldLogin(data);
                    break;
                case ArenaConstants.CONN_ARENA_GET_TIME:
                    getArenaTime(data);
                    break;
                case ArenaConstants.CONN_ARENA_QUEUE:
                    queue(data);
                    break;
                case ArenaConstants.CONN_ARENA_CANCEL_QUEUE:
                    cancelQueue(data);
                    break;
                case ArenaConstants.CONN_ARENA_REMOVE_QUEUE:
                    removeQueue(data);
                    break;
                case ArenaConstants.CONN_ARENA_SYNC_PLAYER_DATA:
                    syncPlayerData(data);
                    break;
                case ArenaConstants.CONN_ARENA_BATTLE_FIGHT:
                    battleFight(data);
                    break;
                case ArenaConstants.CONN_ARENA_SYNC_PLAYER_CATCH_TO_BATTLE:
                    catchToBattle(data);
                    break;
                case ArenaConstants.CONN_ARENA_SYNC_PLAYER_FAIL:
                    syncPlayerFail(data);
                    break;
                case ArenaConstants.CONN_ARENA_TOP10:
                    getArenaLevelTop10(data);
                    break;
                case ArenaConstants.CONN_ARENA_TOP10_WORLDWAR:
                	getArenaLevelTop10WorldWar(data);
                    break;
                case ArenaConstants.CONN_ARENA_LYRIC_ADD:	//添加播放的歌曲
                	addLyric(data);
                	break;
                case ArenaConstants.CONN_ARENA_SERVERLIST:	//获取连接的服务器列表
                	getServerList(data);
                	break;
            }
        }catch(ITimesException ex){
            sendError(ex);
        }catch(Exception ex){
            log.error(ex, ex);
        }
    }

    public void idle(IdleStatus status){
    }

    public void opened(){
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    private void sendError(ITimesException ex){
        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_ERROR, ex.getSerial(), ex.getSessionId());
        seg.write(ex.getAppType());
        seg.writeString(ex.getMessage());
        write(seg);
    }

    private void worldLogin(UWAPData data) throws Exception{
        String serverId = data.readString();
        String serverPassword = data.readString();
        String serverName = data.readString();

        ArenaWorldDetail awd = serverService.arenaWorldListManager.getArenaWorldDetail(serverId);

        if(awd != null && awd.getPassWord().equals(serverPassword)){
            setName(serverName);

            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_WORLD_LOGIN_OK, data.getSerial(), data.getSessionId());
            seg.writeString("登录竞技场服务器成功");
            write(seg);

            log.info("WORLD LOGIN OK[" + serverId + " , " + serverPassword + " , " + serverName + "]");
        }else{
            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_WORLD_LOGIN_FAIL, data.getSerial(), data.getSessionId());
            seg.writeString("登录竞技场服务器失败");
            write(seg);
            close();

            log.info("WORLD LOGIN FAIL[" + serverId + " , " + serverPassword + " , " + serverName + "]");
        }
    }

    private void getArenaTime(UWAPData data) throws Exception{
        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_NOTIFY_TIME, data.getSerial(), data.getSessionId());
        
        //mengjie add
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        //开始时间
        seg.writeLong(cal.getTime().getTime());
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        //结束时间
        seg.writeLong(cal.getTime().getTime());

        write(seg);
    }

    private void queue(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        String ownerName = data.readString();
        int queuePlayerId = data.readInt();
        int[] playerId = data.readInts();
        String[] playerName = data.readStrings();
        int[] playerLevel = data.readInts();
        int[] playerArenaLevel = data.readInts();
        int arenaLevel = data.readInt();
        String arenaName = data.readString();
        String serverName = data.readString();

        int arenaID = data.readInt();
        int queueResult = arenaServerService.addQueue(type, getId(), ownerId, ownerName, queuePlayerId, playerId, playerLevel, playerArenaLevel, arenaLevel, playerName, arenaName, serverName,arenaID);

        if(queueResult != ArenaConstants.ARENA_QUEUE_SUCCESSFUL){
            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_QUEUE_FAIL, data.getSerial(), data.getSessionId());
            seg.writeInt(type);
            seg.writeInt(queuePlayerId);
            seg.writeInt(queueResult);
            write(seg);
        }else{
            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_QUEUE_OK, data.getSerial(), data.getSessionId());
            seg.writeInt(type);
            seg.writeInt(ownerId);
            seg.writeInt(queuePlayerId);
            write(seg);
        }
    }

    private void cancelQueue(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        int cancelPlayerId = data.readInt();

        ArenaQueueServer queue = arenaServerService.getArenaQueue(getId(), ownerId);

        if(queue != null){
            int[] playerIds = queue.getPlayerId();
            boolean found = false;
            
            for(int i = 0; i < playerIds.length; i++){
                if(cancelPlayerId == playerIds[i]){
                    found = true;
                    
                    break;
                }
            }
            
            if(found){
                if(queue.getState() != ArenaQueueServer.STATE_WAIT){
                    UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_CANCEL_QUEUE_FAIL, data.getSerial(), data.getSessionId());
                    seg.writeInt(type);
                    seg.writeInt(ownerId);
                    seg.writeInt(cancelPlayerId);
                    seg.writeInt(ArenaConstants.ARENA_CANCEL_QUEUE_BATTLE);
                    write(seg);
                }else{
                    arenaServerService.removeQueue(getId(), ownerId, false);
    
                    UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_CANCEL_QUEUE_OK, data.getSerial(), data.getSessionId());
                    seg.writeInt(type);
                    seg.writeInt(ownerId);
                    seg.writeInt(cancelPlayerId);
                    write(seg);
                }
            }else{
                UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_CANCEL_QUEUE_FAIL, data.getSerial(), data.getSessionId());
                seg.writeInt(type);
                seg.writeInt(ownerId);
                seg.writeInt(cancelPlayerId);
                seg.writeInt(ArenaConstants.ARENA_CANCEL_QUEUE_OTHER);
                write(seg);
            }
        }else{
            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_CANCEL_QUEUE_FAIL, data.getSerial(), data.getSessionId());
            seg.writeInt(type);
            seg.writeInt(ownerId);
            seg.writeInt(cancelPlayerId);
            seg.writeInt(ArenaConstants.ARENA_CANCEL_QUEUE_ERROR);
            write(seg);
        }
    }

    private void removeQueue(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        int playerId = data.readInt();

        arenaServerService.removeQueue(getId(), ownerId, false);
    }

    private void syncPlayerFail(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        int playerId = data.readInt();

        ArenaQueueServer queue = arenaServerService.getArenaQueue(getId(), ownerId);

        if(queue != null){
            arenaServerService.removeQueue(getId(), ownerId, false);
            ArenaQueueServer opp = queue.getOpponent();

            if(opp != null){
                arenaServerService.resetQueue(opp.getServerId(), opp.getOwnerId());
            }
        }
    }

    private void syncPlayerData(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        int playerId = data.readInt();

        BattleSprite[] dataResult = ArenaTools.readSyncPlayerData(type, playerId, data);

        ArenaQueueServer queue = arenaServerService.getArenaQueue(getId(), ownerId);

        if(queue != null){
            queue.setSprite(playerId, dataResult[0]);
            queue.setPet(playerId, dataResult[1]);
            
            if(queue.getState() == ArenaQueueServer.STATE_SYNC && queue.syncOK()){
                queue.setState(ArenaQueueServer.STATE_SYNC_OK);
                
                if(queue.getOpponent().getState() == ArenaQueueServer.STATE_SYNC_OK){
                    queue.setState(ArenaQueueServer.STATE_BATTLE);
                    queue.getOpponent().setState(ArenaQueueServer.STATE_BATTLE);
                    arenaServerService.addBattle(queue);
                }
            }
        }
    }

    private void battleFight(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        int playerId = data.readInt();

        ArenaQueueServer queue = arenaServerService.getArenaQueue(getId(), ownerId);

        if(queue != null){
            ArenaBattleServer battle = arenaServerService.getBattle(queue.getBattleId());

            if(battle != null){
                battle.process(data, playerId);
            }
        }
    }

    private void catchToBattle(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        int playerId = data.readInt();

        ArenaQueueServer queue = arenaServerService.getArenaQueue(getId(), ownerId);

        if(queue != null){
            ArenaBattleServer battle = arenaServerService.getBattle(queue.getBattleId());

            if(battle != null){
                battle.catchToBattle(playerId);
            }
        }
    }

    //mengjie add
    public void initArenaPlayer(Player player, byte[] abilities, int playerarenaid, int playerarenalevel){
        byte[] bytes = null;
        player.setAbilities(abilities);
        player.setBasicItems(bytes);
        player.setMetaItems(bytes);
        player.setTaskItems(bytes);
        player.setEquipments(bytes);
        player.setUsedEquipments(bytes);
        player.setOptions(bytes);
        //mengjie add 
        player.setKey9_options(null);
        
        player.setChatOptions(bytes);
        player.setTechSkills(bytes);
        player.setRecipes(bytes);
        player.setFriends(bytes);
        player.setBlackList(bytes);
        TaskData taskData = new TaskData();
        taskData.setCurrent(bytes);
        taskData.setFinished(bytes);
        player.setTaskData(taskData);
        player.setPets(bytes);
        player.setPetId(-1);
        player.setCreateTime(new Date());
        player.setArenaV1Id(playerarenaid);
        player.setArenaLevel(playerarenalevel);
    }

    private void getArenaLevelTop10(UWAPData data) throws Exception{
        int type = data.readInt();
        int limit = data.readInt();

        switch(type){
            case ArenaConstants.ARENA_TYPE_ONE:{
                ArenaTeamTotal[] arenaTeamTotal = arenaServerService.getArenaLevelTop10(type, limit);

                if(arenaTeamTotal != null){
                    UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_TOP10_OK);
                    seg.writeBoolean(true);
                    seg.writeInt(type);
                    seg.writeInt(arenaTeamTotal.length);
                    for(int i = 0; i < arenaTeamTotal.length; i++){
                        seg.writeString(arenaTeamTotal[i].getServername());
                        seg.writeString(arenaTeamTotal[i].getArenaname());
                        seg.writeInt(arenaTeamTotal[i].getArenalevel());
                    }
                    write(seg);
                }else{
                    UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_TOP10_OK, data.getSerial(), data.getSessionId());
                    seg.writeBoolean(false);
                    write(seg);
                }
            }
                break;
            case ArenaConstants.ARENA_TYPE_TWO:{
                ArenaTeamTotal[] arenaTeamTotal = arenaServerService.getArenaLevelTop10(type, limit);

                if(arenaTeamTotal != null){
                    UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_TOP10_OK);
                    seg.writeBoolean(true);
                    seg.writeInt(type);
                    seg.writeInt(arenaTeamTotal.length);
                    for(int i = 0; i < arenaTeamTotal.length; i++){
                        seg.writeString(arenaTeamTotal[i].getServername());
                        seg.writeString(arenaTeamTotal[i].getArenaname());
                        seg.writeInt(arenaTeamTotal[i].getArenalevel());
                    }
                    write(seg);
                }else{
                    UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_TOP10_OK, data.getSerial(), data.getSessionId());
                    seg.writeBoolean(false);
                    write(seg);
                }
            }
                break;
            case ArenaConstants.ARENA_TYPE_THREE:{
                ArenaTeamTotal[] arenaTeamTotal = arenaServerService.getArenaLevelTop10(type, limit);

                if(arenaTeamTotal != null){
                    UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_TOP10_OK);
                    seg.writeBoolean(true);
                    seg.writeInt(type);
                    seg.writeInt(arenaTeamTotal.length);
                    for(int i = 0; i < arenaTeamTotal.length; i++){
                        seg.writeString(arenaTeamTotal[i].getServername());
                        seg.writeString(arenaTeamTotal[i].getArenaname());
                        seg.writeInt(arenaTeamTotal[i].getArenalevel());
                    }
                    write(seg);
                }else{
                    UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_TOP10_OK, data.getSerial(), data.getSessionId());
                    seg.writeBoolean(false);
                    write(seg);
                }
            }
                break;
        }
    }
    
    private void getArenaLevelTop10WorldWar(UWAPData data) throws Exception{
        int type = data.readInt();
        int limit = data.readInt();

        switch(type){
            case ArenaConstants.ARENA_TYPE_ONE:{
            	ArenaTeamTotalWorldWar[] arenaTeamTotalWorldWar = arenaServerService.getArenaLevelTop10WorldWar(type, limit);

                if(arenaTeamTotalWorldWar != null){
                    UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_TOP10_WORLDWAR_OK);
                    seg.writeBoolean(true);
                    seg.writeInt(type);
                    seg.writeInt(arenaTeamTotalWorldWar.length);
                    for(int i = 0; i < arenaTeamTotalWorldWar.length; i++){
                        seg.writeString(arenaTeamTotalWorldWar[i].getServername());
                        seg.writeString(arenaTeamTotalWorldWar[i].getArenaname());
                        seg.writeInt(arenaTeamTotalWorldWar[i].getArenalevel());
                    }
                    write(seg);
                }else{
                    UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_TOP10_OK, data.getSerial(), data.getSessionId());
                    seg.writeBoolean(false);
                    write(seg);
                }
            }
                break;
            case ArenaConstants.ARENA_TYPE_TWO:{
                
            }
                break;
            case ArenaConstants.ARENA_TYPE_THREE:{
                
            }
                break;
        }
    }
    
    private void addLyric(UWAPData data) throws Exception{
    	String serverName = data.readString();
    	String playerName = data.readString();
    	int destServerId = data.readInt();
    	String destPlayerName = data.readString();
        String blessings = data.readString();
        String singer = data.readString();
        String singName = data.readString();
        String[] lyrics = data.readStrings();
        
        LyricData lyricData = new LyricData();
        lyricData.setSinger(singer);
        lyricData.setName(singName);
        lyricData.setOtherTip(lyrics);
        
        LyricDataServer lyricDataServer = new LyricDataServer();
        lyricDataServer.setLyricData(lyricData);
        lyricDataServer.setSrcServerId(getId());
        lyricDataServer.setSrcPlayerName(playerName);
        lyricDataServer.setDestServerId(destServerId);
        lyricDataServer.setDestPlayerName(destPlayerName);
        lyricDataServer.setBlessings(blessings);
        lyricDataServer.setCurrentIndex(-1);
        
        arenaServerService.addLyric(lyricDataServer);
    }
    
    private void getServerList(UWAPData data) throws Exception{
    	int playerId = data.readInt();
    	int serial = data.readInt();
    	UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_SERVERLIST, data.getSerial(), data.getSessionId());
    	seg.writeInt(playerId);
    	seg.writeInt(serial);
    	String[] servers = serverService.getServers(getId());
    	seg.writeStrings(servers);
        write(seg);
    }
}
