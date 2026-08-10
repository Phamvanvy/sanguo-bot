package com.pip.itimes.server.world.battle.arena.client;

import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.Packet;
import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.net.Session;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.ArenaTeam;
import com.pip.itimes.server.bean.ArenaTeamTotal;
import com.pip.itimes.server.dao.PlayerDao;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.IEquipment;
import com.pip.itimes.server.stage.Pet;
import com.pip.itimes.server.util.KeywordsUtil;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.ConnectService;
import com.pip.itimes.server.world.IPlayerData;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.PhizService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.StageService;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.battle.Battle2;
import com.pip.itimes.server.world.battle.BattleService2;
import com.pip.itimes.server.world.battle.BattleSprite;
import com.pip.itimes.server.world.battle.arena.ArenaConstants;
import com.pip.itimes.server.world.battle.arena.ArenaService;
import com.pip.itimes.server.world.battle.arena.ArenaTools;
import com.pip.itimes.server.world.lyricsSystem.LyricData;
import com.pip.itimes.server.world.lyricsSystem.LyricsSystemConfig;
import com.pip.itimes.server.world.toplist.TopListService;

public class ArenaSession extends Session implements Runnable{
    private static final Logger log = Logger.getLogger(ArenaSession.class);

    private boolean arenaConnected = false;
    private boolean autoReconnect = true;

    private Configuration configuration;
    private StageService stageService;
    private ConnectService connectService;
    private PlayerService playerService;
    private ChatService chatService;
    private BattleService2 battleService;
    private ArenaService arenaService;
    private TopListService topListService;
    private PhizService phizService;
    private PlayerDao playerDao;

    private ArenaClient arena;

    public static boolean arenaBroadcasting = false;
    
    public ArenaSession(IoSession session){
        super(session);
        
        playerDao = new PlayerDao();
    }

    public void setStageService(StageService stageService){
        this.stageService = stageService;
    }

    public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }

    public void setConfiguration(Configuration configuration){
        this.configuration = configuration;
    }

    public void setPlayerService(PlayerService playerService){
        this.playerService = playerService;
    }

    public void setChatService(ChatService chatService){
        this.chatService = chatService;
    }

    public void setBattleService(BattleService2 battleService){
        this.battleService = battleService;
    }

    public void setArenaService(ArenaService arenaService){
        this.arenaService = arenaService;
    }

    public void setTopListService(TopListService topListService){
        this.topListService = topListService;
    }
    
    public void setPhizService (PhizService phizService) {
    	this.phizService = phizService;
    }
    
    public PhizService getPositionService () {
    	return phizService;
    }

    public void closed(){
        //竞技场服务器连接断开，启动重复连接过程
        arenaConnected = false;

        if(autoReconnect){
            Server.instance.arenaSessionClosed(this);
        }
    }

    public void created(){
    }

    public void handle(Packet packet){
        UWAPData data = packet.datas[0];
        byte type = data.getAppType();

        try{
            switch(type){
                case ArenaConstants.CONN_ARENA_WORLD_LOGIN_OK:
                    arenaLoginOk(data);
                    break;
                case ArenaConstants.CONN_ARENA_WORLD_LOGIN_FAIL:
                    arenaLoginFail(data);
                    break;
                case ArenaConstants.CONN_ARENA_NOTIFY_TIME:
                    notifyArenaTime(data);
                    break;
                case ArenaConstants.CONN_ARENA_QUEUE_OK:
                    queueOk(data);
                    break;
                case ArenaConstants.CONN_ARENA_QUEUE_FAIL:
                    queueFail(data);
                    break;
                case ArenaConstants.CONN_ARENA_CANCEL_QUEUE_OK:
                    cancelQueueOneOk(data);
                    break;
                case ArenaConstants.CONN_ARENA_CANCEL_QUEUE_FAIL:
                    cancelQueueOneFail(data);
                    break;
                case ArenaConstants.CONN_ARENA_SYNC_PLAYER:
                    syncPlayer(data);
                    break;
                case ArenaConstants.CONN_ARENA_BATTLE_START:
                    battleStart(data);
                    break;
                case ArenaConstants.CONN_ARENA_BATTLE_ABORT:
                    battleAbort(data);
                    break;
                case ArenaConstants.CONN_ARENA_ROUND_END:
                    battleRoundEnd(data);
                    break;
                case ArenaConstants.CONN_ARENA_REMOVE_BATTLE:
                    removeBattle(data);
                    break;
                case ArenaConstants.CONN_ARENA_TOP10_OK:
                    setArenaTeamTop10(data);
                    break;
                case ArenaConstants.CONN_ARENA_TOP10_WORLDWAR_OK:
                	setArenaTeamTop10WorldWar(data);
                    break;
                case ArenaConstants.CONN_ARENA_REMOVE_QUEUE_TIMEOUNT:
                    removeQueue(data);
                    break;
                case ArenaConstants.CONN_ARENA_SERVERLIST:
                	sendServers(data);
                	break;
                case ArenaConstants.CONN_ARENA_LYRIC_SEND:
                	sendLyric(data);
                	break;
            }
        }catch(Exception ex){
            log.error(ex, ex);
        }
    }

    public void idle(IdleStatus status){
    }

    public void opened(){
        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_WORLD_LOGIN);
        String serverId = (String) configuration.getProperty(ServerConstants.SERVERID);
        String serverName = (String) configuration.getProperty(ServerConstants.SERVERPASSWORD);
        String arenaShowName = (String) configuration.getProperty("arenashowname");
        seg.writeString(serverId);
        seg.writeString(serverName);
        seg.writeString(arenaShowName);
        write(seg);
    }

    public boolean arenaValid(){
    	if (arena.isValid()){
    		if (!arenaBroadcasting){
    			chatService.sendSystemMessage("跨服竞技场已开放,开放时间为12点至24点.玩家可通过瓦伊特镇战场传送师进入。");
    			arenaBroadcasting = true;
    		}    		
    	}else{
    		if (arenaBroadcasting){
    			arenaBroadcasting = false;
    		}
    	}
        return arena.isValid();
    }

    public int addArenaQueue(int type, WorldPlayer player, WorldPlayer[] players, ArenaTeam arenateam) throws Exception{
        int queueResult = ArenaConstants.ARENA_QUEUE_ERROR;
        
        switch(type){
            case ArenaConstants.ARENA_TYPE_ONE:
                queueResult = arena.addToQueue(ArenaConstants.ARENA_TYPE_ONE, arenateam.getOwner(), playerDao.getPlayerName(arenateam.getOwner()), player.getId(), players);
                break;
            case ArenaConstants.ARENA_TYPE_TWO:
                queueResult = arena.addToQueue(ArenaConstants.ARENA_TYPE_TWO, arenateam.getOwner(), playerDao.getPlayerName(arenateam.getOwner()), player.getId(), players);
                break;
            case ArenaConstants.ARENA_TYPE_THREE:
                queueResult = arena.addToQueue(ArenaConstants.ARENA_TYPE_THREE, arenateam.getOwner(), playerDao.getPlayerName(arenateam.getOwner()), player.getId(), players);
                break;
        }
        
        if(queueResult == ArenaConstants.ARENA_QUEUE_SUCCESSFUL){
            ArenaBattleClient battle = arena.getBattleByOwner(arenateam.getOwner());
            
            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_QUEUE);
            seg.writeInt(type);
            seg.writeInt(battle.getOwnerId());
            seg.writeString(battle.getOwnerName());
            seg.writeInt(player.getId());
            seg.writeInts(battle.getPlayerIds());
            seg.writeStrings(battle.getPlayerNames());
            seg.writeInts(battle.getPlayerLevels());
            seg.writeInts(battle.getPlayerArenaLevels()); //playerArenaLevel玩家个人等级  TODO change
            seg.writeInt(arenateam.getArenalevel()); //arenaLevel玩家战队等级 TODO change
            seg.writeString(arenateam.getArenaname()); //arenaLevel玩家战队名字 TODO change
            seg.writeString(configuration.getString("serverid"));
            
            switch(type){
            case ArenaConstants.ARENA_TYPE_ONE:
            	seg.writeInt(player.getArenaV1Id());
            	break;
            case ArenaConstants.ARENA_TYPE_TWO:
            	seg.writeInt(player.getArenaV2Id());
            	break;
            case ArenaConstants.ARENA_TYPE_THREE:
            	seg.writeInt(player.getArenaV3Id());
            	break;
            }
            write(seg);
        }

        
        return queueResult;
    }

    public void arenaLeave(IPlayerData player, boolean logout){
        if(logout && arena != null){
            ArenaBattleClient battle = arena.getBattleByPlayer(player.getId());

            if(battle != null && !battle.getBattleStarted()){
                arena.removeQueue(battle.getOwnerId());
                
                UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_REMOVE_QUEUE);
                seg.writeInt(battle.getType());
                seg.writeInt(battle.getOwnerId());
                seg.writeInt(player.getId());
                write(seg);

                int[] playerIds = battle.getPlayerIds();
                    
                for(int i = 0; i < playerIds.length; i++){
                    if(playerIds[i] != player.getId()){
                        switch(battle.getType()){
                            case ArenaConstants.ARENA_TYPE_TWO:
                                chatService.sendPrivateMessage(-1, "系统", playerIds[i], "由于您的队友下线，2v2竞技场排队已取消");
        
                                break;
                            case ArenaConstants.ARENA_TYPE_THREE:
                                chatService.sendPrivateMessage(-1, "系统", playerIds[i], "由于您的队友下线，3v3竞技场排队已取消");
        
                                break;
                        }
                    }
                }
            }
        }
    }
    
    private void removeQueue(UWAPData data) throws Exception{
        int ownerId = data.readInt();
        
        ArenaBattleClient battle = arena.getBattleByOwner(ownerId);

        if(battle != null && !battle.getBattleStarted()){
            arena.removeQueue(ownerId);
        }
    }

    private void queueOk(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        int queuePlayerId = data.readInt();
        connectService.sendMessage(queuePlayerId, "排队成功，请耐心等待");
    }

    private void queueFail(UWAPData data) throws Exception{
        int type = data.readInt();
        int queuePlayerId = data.readInt();
        int queueError = data.readInt();
        String errorMessage = "排队失败,每位参赛者在同一时间只能在一个队列中";

        switch(queueError){
            case ArenaConstants.ARENA_QUEUE_DUPLICATE:
                errorMessage = "不能重复排队";
                break;
            case ArenaConstants.ARENA_QUEUE_OTHER:
                if(type == ArenaConstants.ARENA_TYPE_ONE){
                    errorMessage = "不能重复排队";
                }else{
                    errorMessage = "您战队的其他成员已经排过队了，不能重复排队";
                }
                
                break;
        }

        connectService.sendMessage(queuePlayerId, errorMessage);
    }

    public int cancelArenaQueue(int type, WorldPlayer player, ArenaTeam arenateam){
        ArenaBattleClient battleClient = arena.getBattleByOwner(arenateam.getOwner());

        if(battleClient != null){
            int cancelResult = arena.cancelQueue(type, battleClient.getOwnerId(), player.getId());

            if(cancelResult == ArenaConstants.ARENA_CANCEL_QUEUE_SUCCESSFUL){
                UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_CANCEL_QUEUE);
                seg.writeInt(type);
                seg.writeInt(battleClient.getOwnerId());
                seg.writeInt(player.getId());
                write(seg);
            }

            return cancelResult;
        }

        return ArenaConstants.ARENA_CANCEL_QUEUE_ERROR;
    }

    private void cancelQueueOneOk(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        int cancelPlayerId = data.readInt();

        arena.removeQueue(ownerId);
        connectService.sendMessage(cancelPlayerId, "取消排队成功");
    }

    private void cancelQueueOneFail(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        int cancelPlayerId = data.readInt();
        int cancelError = data.readInt();

        String errorMessage = "您还未曾排队，不能取消";

        switch(cancelError){
            case ArenaConstants.ARENA_CANCEL_QUEUE_BATTLE:
                errorMessage = "战斗已经开始，不能取消";
                break;
            case ArenaConstants.ARENA_CANCEL_QUEUE_OTHER:
                errorMessage = "您战队的其他成员已经排过队了，不能由您取消";
                break;
        }

        connectService.sendMessage(cancelPlayerId, errorMessage);
    }

    private void syncPlayer(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        int[] playerIds = data.readInts();

        boolean failed = false;
        UWAPSegment[] segs = new UWAPSegment[playerIds.length];
        
        for(int i = 0; i < playerIds.length; i++){
            WorldPlayer player = playerService.getWorldPlayer(playerIds[i]);
            Battle2 battle = battleService.getBattleByPlayer(player.getId());

            if(battle != null){
                segs[i] = ArenaTools.getSyncFailSegment(type, ownerId, playerIds[i]);
                failed = true;
                
                break;
            }else{
                segs[i] = ArenaTools.getSyncSegment(type, ownerId, player);
            }
        }

        if(failed){
            for(int i = 0; i < playerIds.length; i++){
                segs[i] = ArenaTools.getSyncFailSegment(type, ownerId, playerIds[i]);

                switch(type){
                    case ArenaConstants.ARENA_TYPE_ONE:
                        chatService.sendPrivateMessage(-1, "系统", playerIds[i], "由于您或您的队友正在战斗，1v1竞技场战斗未能正确开始，请重新报名排队");

                        break;
                    case ArenaConstants.ARENA_TYPE_TWO:
                        chatService.sendPrivateMessage(-1, "系统", playerIds[i], "由于您或您的队友正在战斗，2v2竞技场战斗未能正确开始，请重新报名排队");

                        break;
                    case ArenaConstants.ARENA_TYPE_THREE:
                        chatService.sendPrivateMessage(-1, "系统", playerIds[i], "由于您或您的队友正在战斗，3v3竞技场战斗未能正确开始，请重新报名排队");

                        break;
                }
            }
                
            arena.removeQueue(ownerId);
        }
        
        for(int i = 0; i < segs.length; i++){
            write(segs[i]);
        }
    }

    private void battleStart(UWAPData data) throws Exception{
        String otherName1 = "";
        String otherName2 = "";
        boolean isSide1 = false;
        
        int arenaType = data.readInt();
        int ownerId = data.readInt();
        int playerId = data.readInt();
        int battleId = data.readInt();

        UWAPSegment seg = new UWAPSegment(ClientConstants.PK_START);
        seg.writeInt(data.readInt());
        
        byte tmpCount = data.readByte();
        seg.write(tmpCount);
        
        for(int i = 0; i < tmpCount; i++){
            int tmpId = data.readInt();
            seg.writeInt(tmpId);
            
            if(playerId == tmpId){
                isSide1 = true;
            }
        }
        
        tmpCount = data.readByte();
        seg.write(tmpCount);

        for(int i = 0; i < tmpCount; i++){
            int tmpId = data.readInt();
            String tmpName = data.readString();
            
            if(i > 0){
                otherName1 += "，";
            }
            
            otherName1 += tmpName;
            
            seg.writeInt(tmpId);
            seg.writeString(tmpName);
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.write(data.readByte());
            seg.write(data.readByte());
            seg.writeInt(data.readInt());
            seg.writeBoolean(data.readBoolean());
            seg.write(data.readByte());
    
            seg.write(data.readByte());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.writeString(data.readString());
            seg.writeInt(data.readInt());
            seg.writeBoolean(data.readBoolean());
            seg.write(data.readByte());
            seg.writeInt(data.readInt());
            seg.writeShort(data.readShort());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
        }
        
        tmpCount = data.readByte();
        seg.write(tmpCount);

        for(int i = 0; i < tmpCount; i++){
            int tmpId = data.readInt();
            String tmpName = data.readString();
            
            if(i > 0){
                otherName2 += "，";
            }
            
            otherName2 += tmpName;
            
            seg.writeInt(tmpId);
            seg.writeString(tmpName);
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.write(data.readByte());
            seg.write(data.readByte());
            seg.writeInt(data.readInt());
            seg.writeBoolean(data.readBoolean());
            seg.write(data.readByte());
    
            seg.write(data.readByte());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
            seg.writeString(data.readString());
            seg.writeInt(data.readInt());
            seg.writeBoolean(data.readBoolean());
            seg.write(data.readByte());
            seg.writeInt(data.readInt());
            seg.writeShort(data.readShort());
            seg.writeInt(data.readInt());
            seg.writeInt(data.readInt());
        }

        seg.writeShort(data.readShort());

        WorldPlayer player = playerService.getWorldPlayer(playerId);
        playerService.acquire(player);
        player.inArenaBattle = true;
        
        //战斗状态改变
        player.setBattle(true);
        phizService.addChangePhiz(player, PhizService.PHIZ_TYPE_BATTLE, PhizService.PHIZ_STATE_DEFAULT);
        
        connectService.writeTo(seg, playerId);

        ArenaBattleClient battle = arena.getBattleByOwner(ownerId);

        if(battle != null){
            battle.setId(battleId);
            battle.setBattleStarted(true);
        }

        String otherName = otherName1;

        if(isSide1){
            otherName = otherName2;
        }

        switch(arenaType){
            case ArenaConstants.ARENA_TYPE_ONE:
                chatService.sendPrivateMessage(-1, "系统", playerId, "您与" + otherName + "的1v1竞技场战斗已开始");
                break;
            case ArenaConstants.ARENA_TYPE_TWO:
                chatService.sendPrivateMessage(-1, "系统", playerId, "您与" + otherName + "的2v2竞技场战斗已开始");
                break;
            case ArenaConstants.ARENA_TYPE_THREE:
                chatService.sendPrivateMessage(-1, "系统", playerId, "您与" + otherName + "的3v3竞技场战斗已开始");
                break;
        }
    }

    private void battleAbort(UWAPData data) throws Exception{
        int arenaType = data.readInt();
        int ownerId = data.readInt();
        int playerId = data.readInt();

        UWAPSegment seg = new UWAPSegment(ClientConstants.BATTLE_ABORT);

        seg.writeInt(data.readInt());
        seg.writeInt(data.readInt());
        seg.writeInt(data.readInt());
        seg.writeInt(data.readInt());
        seg.writeInt(data.readInt());

        WorldPlayer player = playerService.getWorldPlayer(playerId);
        player.inArenaBattle = false;
        playerService.release(player);

        connectService.writeTo(seg, playerId);
    }

    private void battleRoundEnd(UWAPData data) throws Exception{
        int arenaType = data.readInt();
        int ownerId = data.readInt();
        int playerId = data.readInt();
        WorldPlayer player = playerService.getWorldPlayer(playerId);

        UWAPSegment seg = new UWAPSegment(ClientConstants.PK_ROUND_END);
        byte roundEndType = data.readByte();

        seg.write(roundEndType);
        seg.writeInt(data.readInt());
        short round = data.readShort();
        seg.writeShort(round);

        if(roundEndType == 0){
            byte movieCount = data.readByte();
            seg.write(movieCount);

            for(int i = 0; i < movieCount; i++){
                seg.writeInts(data.readInts());
            }
        }

        int count = data.readInt();
        
        for(int i = 0; i < count; i++){
            seg.write(data.readByte());
            int tmpId = data.readInt();
            seg.writeInt(tmpId);
            int tmpplayerId = data.readInt();
            int playerhp = data.readInt();
            //mengjie add
            seg.writeInt(playerhp);
            int playermp = data.readInt();
            seg.writeInt(playermp);
            
            if(tmpplayerId == playerId){
            	if(player == null){
            		log.error("player is null PlayerID[" + playerId + "]");
            	}else{
	                player.setHp(playerhp);
	                player.setMp(playermp);
            	}
            }

            seg.writeBoolean(data.readBoolean());
            seg.writeInt(data.readInt());
            seg.writeString(data.readString());
            Pet p = null;
            if(player != null){
            	p = player.getPet();
            }
            
            if(p != null){
                int playerpethp = data.readInt();
                seg.writeInt(playerpethp);
                int playerpetmp = data.readInt();
                seg.writeInt(playerpetmp);
                
                if(tmpplayerId == playerId){
                    p.setHp(playerpethp);
                    p.setMp(playerpetmp);
                }
                
                seg.writeInt(data.readInt());
                seg.writeBoolean(data.readBoolean());
                seg.writeString(data.readString());
            }else{
                seg.writeInt(data.readInt());
                seg.writeInt(data.readInt());
                seg.writeInt(data.readInt());
                seg.writeBoolean(data.readBoolean());
                seg.writeString(data.readString());
            }
        }

        if(roundEndType == 0){
            seg.write(data.readByte());
        }

        ArenaBattleClient battle = arena.getBattleByOwner(ownerId);

        if(battle != null){
            battle.setRound(round);
        }
        
        //Added by leo for sync CD Info
        byte cdInfoCount = data.readByte();
        seg.write(cdInfoCount);
        
        for(int i = 0; i < cdInfoCount ; i++){
            seg.writeInt(data.readInt());
            seg.write(data.readBytes());
        }
        //Added end

        connectService.writeTo(seg, playerId);
    }

    public void battleFight(UWAPData data, WorldPlayer player) throws Exception{
        ArenaBattleClient battle = arena.getBattleByPlayer(player.getId());

        if(battle != null){
            UWAPSegment seg = battle.fight(data, battle.getOwnerId(), player.getId());

            if(seg != null){
                write(seg);
            }
        }
    }

    public void catchToArenaOneBattle(ArenaBattleClient battle, WorldPlayer player){
        if(battle != null){
            UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_SYNC_PLAYER_CATCH_TO_BATTLE);
            seg.writeInt(battle.getType());
            seg.writeInt(battle.getOwnerId());
            seg.writeInt(player.getId());
            write(seg);

            player.inArenaBattle = false;
        }
    }

    public ArenaBattleClient getArenaBattle(WorldPlayer player) throws Exception{
        return arena.getBattleByPlayer(player.getId());
    }
    public ArenaBattleClient getArenaBattleByowner(WorldPlayer player) throws Exception{
        return arena.getBattleByOwner(player.getId());
    }
    private void removeBattle(UWAPData data) throws Exception{
        int type = data.readInt();
        int ownerId = data.readInt();
        String ownerName = data.readString();
        String arenaName = data.readString();
        int[] playerIds = data.readInts();
        int otherarenalevel = data.readInt();
        int[] otherplayerarenalevel = data.readInts();
        String otherservername = data.readString();
        String[] otherplayername = data.readStrings();
        String otherarenaname = data.readString();
        
        boolean addflagwin = false;
        boolean addflaglost = false;
        boolean iswiner = data.readBoolean();

        arena.removeQueue(ownerId);

        HashSet<Integer> mapIdSet = new HashSet<Integer>();
        Vector<String> sexList = new Vector<String>();
        Vector<String> nameList = new Vector<String>();

        for(int i = 0; i < playerIds.length; i++){
            WorldPlayer player = playerService.getWorldPlayerAndCatch(playerIds[i]);
            ArenaTeam arenaTeam = arenaService.findArenaTeam(ownerId,type);
            if((player != null) && (arenaTeam!= null)){
                player.inArenaBattle = false;
                int arenapoint = ArenaTools.getArenaPoint(arenaTeam.getArenalevel(), otherarenalevel, iswiner);
                //将战队等级和个人等级更新
                if (iswiner){
                	if (!addflagwin){
                		addflagwin = true;
                		arenaService.addArenaLevel(ownerId, arenapoint, type);
                	}
                }else{
                	if (!addflaglost){
                		addflaglost = true;
                		arenaService.addArenaLevel(ownerId, arenapoint, type);
                	}
                }
                
                if(player.getSex() == 0){
                    sexList.add("他");
                }else{
                    sexList.add("她");
                }
                if(player.online()){
	                nameList.add(player.getPlayerName());
	                mapIdSet.add(new Integer(player.getMapId()));
                }
                player.setArenaLevel((int) (player.getArenaLevel() + arenapoint));
                arenaService.updateArenaTeamPlayercatch(arenaTeam.getId(), type, ownerId, player.getArenaLevel(), player.getId());
                
                //改变战斗状态
                player.setBattle(false);
                playerService.release(player);
                phizService.addChangePhiz(player, PhizService.PHIZ_TYPE_BATTLE, PhizService.PHIZ_STATE_DEFAULT);
            }
            playerService.releasePlayer(player);
        }
        StringBuffer chatMessage = new StringBuffer();
        chatMessage.append("由“");
        for(int i = 0; i < nameList.size(); i++){
            if(i > 0){
                chatMessage.append("，");
            }

            chatMessage.append(nameList.get(i));
        }
        chatMessage.append("”组成的[");
        chatMessage.append(arenaName);
        if(iswiner){
            chatMessage.append("]战队在竞技场中战胜了");
        }else{
            chatMessage.append("]战队在竞技场中败给了");
        }
        chatMessage.append(otherservername);
        chatMessage.append("由“");
        for(int i = 0; i < otherplayername.length; i++){
            if(i > 0){
                chatMessage.append("，");
            }

            chatMessage.append(otherplayername[i]);
        }
        chatMessage.append("”组成的[");
        chatMessage.append(otherarenaname);
        if(iswiner){
            chatMessage.append("]战队。恭喜");
            if(sexList.size() > 1){
                chatMessage.append("他们吧!");
            }else{
                chatMessage.append(sexList.get(0));
                chatMessage.append("吧!");
            }
        }else{
            chatMessage.append("]战队。大家努力啊！");
        }

        for(Integer mapId : mapIdSet){
            chatService.sendMapMessage(mapId.shortValue(), -1, "系统", chatMessage.toString());
        }
    }

    private void arenaLoginOk(UWAPData data) throws Exception{
        arenaConnected = true;
        arena = new ArenaClient();
        getArenaTime(arena);

        new Thread(this).start();

        log.info("哈哈哈，连上啦！");
    }

    private void arenaLoginFail(UWAPData data) throws Exception{
        autoReconnect = false;
        close();

        log.error("日！！！");
    }

    private void getArenaTime(ArenaClient arenaClient){
        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_GET_TIME);
        write(seg);
    }

    private void notifyArenaTime(UWAPData data) throws Exception{
        long beginTime = data.readLong();
        long endTime = data.readLong();

        arena.setBeginTime(beginTime);
        arena.setEndTime(endTime);
    }

    public void run(){
        while(arenaConnected){
            try{
                Thread.sleep(60000L);

                getArenaTime(arena);
            }catch(Exception e){
            }
        }
    }

    private void setArenaTeamTop10(UWAPData data) throws Exception{
        boolean flag = data.readBoolean();
        if(flag){
        	int type = data.readInt();
            int size = data.readInt();
            ArenaTeamTotal[] arenateamlist = new ArenaTeamTotal[size];
            for(int i = 0; i < size; i++){
                arenateamlist[i] = new ArenaTeamTotal();
                arenateamlist[i].setServername(data.readString());
                arenateamlist[i].setArenaname(data.readString());
                arenateamlist[i].setArenalevel(data.readInt());
            }
            topListService.playerTopList.setAllServerArenaLevelTopList(arenateamlist,type);
        }
    }

    private void setArenaTeamTop10WorldWar(UWAPData data) throws Exception{
        boolean flag = data.readBoolean();
        if(flag){
        	int type = data.readInt();
            int size = data.readInt();
            ArenaTeamTotal[] arenateamlist = new ArenaTeamTotal[size];
            for(int i = 0; i < size; i++){
                arenateamlist[i] = new ArenaTeamTotal();
                arenateamlist[i].setServername(data.readString());
                arenateamlist[i].setArenaname(data.readString());
                arenateamlist[i].setArenalevel(data.readInt());
            }
            topListService.playerTopList.setAllServerArenaLevelTopListWorldWar(arenateamlist,type);
        }
    }
    public void getAllserverArenaTeam(int type, int limit){
        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_TOP10);
        seg.writeInt(type);
        seg.writeInt(limit);
        write(seg);
    }
    public void getAllserverArenaTeamWorldWar(int type, int limit){
        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_TOP10_WORLDWAR);
        seg.writeInt(type);
        seg.writeInt(limit);
        write(seg);
    }
    
    public void addLyric(LyricData lyricData, String blessings, String playerName, int destServerId, String destPlayerName) throws Exception{
        UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_LYRIC_ADD);
        seg.writeString(configuration.getString("serverid"));
        seg.writeString(playerName);
    	seg.writeInt(destServerId);
    	seg.writeString(destPlayerName);
        seg.writeString(blessings);
        seg.writeString(lyricData.getSinger());
        seg.writeString(lyricData.getName());
        seg.writeStrings(lyricData.getOtherTip());
        write(seg);
    }
    
    public void getServers(int playerId, int serial){
    	UWAPSegment seg = new UWAPSegment(ArenaConstants.CONN_ARENA_SERVERLIST);
    	seg.writeInt(playerId);
    	seg.writeInt(serial);
    	write(seg);
    }
    
    public void sendServers(UWAPData data) throws Exception{
    	int playerId = data.readInt();
    	int serial = data.readInt();
    	UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, serial, data.getSessionId());
		seg.writeShort(ClientConstants.EXTEND_LYRIC);
		seg.write(LyricsSystemConfig.LYRIC_GETSERVERS);
		seg.writeStrings(data.readStrings());
		connectService.writeTo(seg, playerId);
    }
    
    public void sendLyric(UWAPData data) throws Exception{
    	byte lyricType = data.readByte();
    	switch(lyricType){
    	case LyricsSystemConfig.LYRIC_CONTEXT:
    		String tmp = data.readString();
    		tmp = KeywordsUtil.filterKeywords(tmp);
    		tmp = tmp.replace('\n',' ');
    		chatService.sendWorldMessage(-1, "系统", tmp);
    		break;
    	case LyricsSystemConfig.LYRIC_LYRIC:
    		String srcPlayerName = data.readString();
    		KeywordsUtil.filterKeywords(srcPlayerName);
    		srcPlayerName = srcPlayerName.replace('\n',' ');
    		tmp = data.readString();
    		tmp = KeywordsUtil.filterKeywords(tmp);
    		tmp = tmp.replace('\n',' ');
    		chatService.sendWorldMessage(-1, srcPlayerName, tmp);
    		break;
    	}
    }
}
