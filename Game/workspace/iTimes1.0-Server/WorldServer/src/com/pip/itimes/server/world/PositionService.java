package com.pip.itimes.server.world;

import java.util.*;
import java.util.Map.Entry;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.Utils;

import org.apache.log4j.Logger;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.world.game.HouseInstance;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PositionService  {
    private static final Logger log = Logger.getLogger(PositionService.class);

//    private StageService stageService;
    private ConnectService connectService;
    private PhizService phizService;

    static final int GRID_WIDTH = 240;
    static final int GRID_HEIGHT = 240;
    static final int MAX_PLAYER = 5;

    private Map managers = new HashMap();
    private Map player2position = new HashMap();

    private static final Integer OLD = new Integer(1);
    private static final Integer NEW = new Integer(2);
    private static final Integer ALL =  new Integer(3);

    private Map<Integer,PositionCache> caches = new HashMap<Integer,PositionCache>();

    private static final int LEVEL_COLORS[] = {
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xAAAAAA,
                                              0xEEEEEE,
                                              0xEEEEEE,
                                              0xEEEEEE,
                                              0xFFFF00,
                                              0xFFFF00,
                                              0xFFFF00,
                                              0xFFFF00,
                                              0xFFFF00,
                                              0xFFFF00,
                                              0xFFFF00,
                                              0xFFCC00,
                                              0xFFCC00,
                                              0xFFCC00,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600,
                                              0xFF6600

    };

    public PositionService() {
    }


//    public void setStageService(StageService stageService){
//        this.stageService = stageService;
//    }

    public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }
    
    public void setPhizService(PhizService phizService){
    	this.phizService = phizService;
    }

//    public void init(){
//        Stage[] stages = stageService.getStages();
//        for (int i = 0; i < stages.length; i++) {
//            Scene[] scenes = stages[i].getScenes();
//            for(int j=0;j<scenes.length;j++){
//                int id = ((stages[i].getId()<<4)|scenes[j].getId());
//                SceneManager manager = new SceneManager(id,2000,2000,GRID_WIDTH,GRID_HEIGHT);
//                managers.put(new Integer(id),manager);
//            }
//        }
//    }

    public void addMap(GameMap map) {
        if (!managers.containsKey(map)) {
            SceneManager manager = null;
            if (map.getInstance() != null && map.getInstance() instanceof HouseInstance) {
                manager = new SceneManager(map, map.getWidth() * map.getTileWidth(),
                                           map.getHeight() * map.getTileHeight(), map.getWidth() * map.getTileWidth(),
                                           map.getHeight() * map.getTileHeight());
            } else {
                manager = new SceneManager(map, map.getWidth() * map.getTileWidth(),
                                           map.getHeight() * map.getTileHeight(), GRID_WIDTH, GRID_HEIGHT);
            }
            managers.put(map, manager);
        }
    }

    public void removeMap(GameMap map){
        managers.remove(map);
    }

    public void registry(WorldPlayer player) {
//        positionChanged(player,player.getMapId(),player.getX(),player.getY());
    }

    public void unRegistry(PositionSprite player) {
        synchronized (player) {
            Position position = (Position) player2position.remove(new Integer(player.getId()));
            if (position != null&&position.map!=null) {
                sendMove(player, position.groups, null, position.map.getMapId(),
                         position.x, position.y,true);
                Group[] groups = position.groups;
                for(int i=0;i<groups.length;i++){
                    if(groups[i]!=null){
                        groups[i].removePlayer(player);
                    }
                }
            }
//            System.out.println("unRegistry x:"+position.x+" y:"+position.y);
//            getSceneManager(player.getMapId()).print();
        }
    }
    
    /**
     * @param mapId
     * 返回该地图上的所有玩家id
     */
    public Vector getPlayer2Position(int mapId){
    	Vector playerVector = new Vector();
    	Iterator iter = player2position.entrySet().iterator();
        while(iter.hasNext()){
            Entry e = (Entry)iter.next();
            Position position = (Position) e.getValue();
           
            if(position.map.getMapId() == mapId){
            	playerVector.add(e.getKey());
            }
    
        }
        return playerVector;
    }
    
    public Position getPlayerPosition(int playerid){
    	return (Position) player2position.get(new Integer(playerid));
    }
    
    /**
     * 用于向该玩家所在地区的人，发升级动画拓展协议
     * @param player
     * @param map
     */
    public void levelChangedAnimate(WorldPlayer player,GameMap map){
    	 Iterator iter = player2position.entrySet().iterator();
         while(iter.hasNext()){
             Entry e = (Entry)iter.next();
             Position position = (Position) e.getValue();
            
             if(position.map == map){
            	 int playerId = (Integer) e.getKey();
            	 if(playerId != player.getId()){//不是自己发送升级协议
            		  UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
            		  seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PLAYERANIMATE);
            	      seg.writeInt(player.getId());
            	      connectService.writeTo(seg, playerId);
            	 }else{//自己的话-1
            		 UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
            		 seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PLAYERANIMATE);
            		 seg.writeInt(-1);
            		 connectService.writeTo(seg, playerId);
            	 }
             }
     
         }
    	
    }
    
    public void itemAnimate(WorldPlayer player,GameMap map,byte nameIndex,byte lifeCycle){
    	Iterator iter = player2position.entrySet().iterator();
        while(iter.hasNext()){
            Entry e = (Entry)iter.next();
            Position position = (Position) e.getValue();
           
            if(position.map == map){
           	 int playerId = (Integer) e.getKey();
           	 if(playerId != player.getId()){//不是自己
           		  UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
           		  seg.writeShort(ClientConstants.EXTEND_ITEMANIMATE);
           	      seg.writeInt(player.getId());
           	      seg.write(nameIndex);
           	      seg.write(lifeCycle);
           	      connectService.writeTo(seg, playerId);
           	 }else{//自己的话-1
           		 UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
           		 seg.writeShort(ClientConstants.EXTEND_ITEMANIMATE);
           		 seg.writeInt(-1);
           		 seg.write(nameIndex);
           		 seg.write(lifeCycle);
           		 connectService.writeTo(seg, playerId);
           	 }
            }
    
        }
    }
    
    /**
     * 用于向该玩家所在地区的人，发宝石特效动画协议
     * @param player	玩家
     * @param map		地图
     * @param gemLevel	发光等级
     */
    public void gemEffectAnimate (WorldPlayer player, GameMap map, int gemLevel) {
    	Iterator iter = player2position.entrySet().iterator();
        while (iter.hasNext()) {
            Entry e = (Entry)iter.next();
            Position position = (Position) e.getValue();
            if (position.map == map) {
            	int playerId = (Integer) e.getKey();
        		UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
      			seg.writeShort(ClientConstants.EXTEND_GEM_EFFECT);
      			if (playerId == player.getId()) {
      				seg.writeInt(-1);
      			} else {
      				seg.writeInt(player.getId());
      			}
      			seg.writeInt(gemLevel);
      			seg.write(player.getHolyGemLightLevel());
      			seg.write(player.getFantasyGemLightLevel());
      			connectService.writeTo(seg, playerId);
            }
        }
   }
    
    public void positionChanged(PositionSprite player,GameMap newMap,int newX,int newY){
        synchronized(player){
            Position position = (Position) player2position.get(new Integer(player.getId()));
            if (position == null) {//刚入地图
                position = new Position();
                position.map = newMap;
                position.x = newX;
                position.y = newY;
                GridManager[] grids = getGridManagers(newMap, newX,
                        newY);
                Group[] newGroups = getNewGroups(grids,player);
                position.groups = newGroups;
                player2position.put(new Integer(player.getId()),position);
                sendMove(player,null,newGroups,newMap.getMapId(),newX,newY,false);
                phizService.addChangePhiz(player, PhizService.PHIZ_TYPE_BATTLE, PhizService.PHIZ_STATE_DEFAULT);
                phizService.addChangePhiz(player, player.getPhizTitleType(), player.getPhizTitleIndex());
            } else {
                if (position.map != newMap) {//到了新地图
                    GridManager[] grids = getGridManagers(newMap, newX,
                            newY);
                    Group[] newGroups = getNewGroups(grids, player);
                    Group[] groups = new Group[4];
                    System.arraycopy(position.groups,0,groups,0,4);
                    position.groups = newGroups;
                    position.x = newX;
                    position.y = newY;
                    position.map = newMap;
                    for(int i=0;i<groups.length;i++){
                        if(groups[i]!=null)
                            groups[i].removePlayer(player);
                    }
                    sendMove(player, groups, newGroups, newMap.getMapId(), newX, newY,false);
                    phizService.addChangePhiz(player, PhizService.PHIZ_TYPE_BATTLE, PhizService.PHIZ_STATE_DEFAULT);
                    phizService.addChangePhiz(player, player.getPhizTitleType(), player.getPhizTitleIndex());
                } else {//同一张地图上在走动
                    if (position.x != newX || position.y != newY) {
                        GridManager[] grids = getGridManagers(newMap, newX,
                                newY); //获得附近正方形内的4个gridManage
                        Group[] newGroups = getNewGroups(grids, player); //获得人物所在的 4个人物的group
                        Group[] groups = new Group[4];
                        System.arraycopy(position.groups,0,groups,0,4);
                        position.groups = newGroups;
                        position.x = newX;
                        position.y = newY;
                        position.map = newMap;
                        Group[] oldGroups = getOldGroups(groups,newGroups);
                        for(int i=0;i<oldGroups.length;i++){
                            if(oldGroups[i]!=null)
                                oldGroups[i].removePlayer(player);
                        }
                        sendMove(player, groups, newGroups, newMap.getMapId(), newX,
                                 newY,false);
                        phizService.addChangePhiz(player, PhizService.PHIZ_TYPE_BATTLE, PhizService.PHIZ_STATE_DEFAULT);
                        phizService.addChangePhiz(player, player.getPhizTitleType(), player.getPhizTitleIndex());
                    }
                }
            }
        }
    }
//    public void positionChanged(WorldPlayer player,int newMapId,int newX,int newY) {
//        synchronized(player){
//            Position position = (Position) player2position.get(player);
//            if (position == null) {
//                position = new Position();
//                position.mapId = newMapId;
//                position.x = newX;
//                position.y = newY;
//                GridManager[] grids = getGridManagers(newMapId, newX,
//                        newY);
//                Group[] newGroups = getNewGroups(grids,player);
//                position.groups = newGroups;
//                player2position.put(player,position);
//                sendMove(player,null,newGroups,newMapId,newX,newY,false);
//            } else {
//                if (position.mapId != newMapId) {
//                    GridManager[] grids = getGridManagers(newMapId, newX,
//                            newY);
//                    Group[] newGroups = getNewGroups(grids, player);
//                    Group[] groups = new Group[4];
//                    System.arraycopy(position.groups,0,groups,0,4);
//                    position.groups = newGroups;
//                    position.x = newX;
//                    position.y = newY;
//                    position.mapId = newMapId;
//                    for(int i=0;i<groups.length;i++){
//                        if(groups[i]!=null)
//                            groups[i].removePlayer(player);
//                    }
//                    sendMove(player, groups, newGroups, newMapId, newX, newY,false);
//                } else {
//                    if (position.x != newX || position.y != newY) {
//                        GridManager[] grids = getGridManagers(newMapId, newX,
//                                newY);
//                        Group[] newGroups = getNewGroups(grids, player);
//                        Group[] groups = new Group[4];
//                        System.arraycopy(position.groups,0,groups,0,4);
//                        position.groups = newGroups;
//                        position.x = newX;
//                        position.y = newY;
//                        position.mapId = newMapId;
//                        Group[] oldGroups = getOldGroups(groups,newGroups);
//                        for(int i=0;i<oldGroups.length;i++){
//                            if(oldGroups[i]!=null)
//                                oldGroups[i].removePlayer(player);
//                        }
//                        sendMove(player, groups, newGroups, newMapId, newX,
//                                 newY,false);
//                    }
//                }
//            }
//        }
//    }


    public Group[] getOldGroups(Group[] groups,Group[] newGroups){
        List l = new ArrayList(4);
        for(int i=0;i<groups.length;i++){
            if(groups[i]!=null){
                boolean b = true;
                for(int j=0;j<newGroups.length;j++){
                    if(groups[i]==newGroups[j]){
                        b = false;
                        break;
                    }
                }
                if(b)
                    l.add(groups[i]);
            }
        }
        Group[] gs = new Group[l.size()];
        l.toArray(gs);
        return gs;
    }

    public void sendMove(PositionSprite player,Group[] groups,Group[] newGroups,int mapId,int x,int y,boolean leave){
        Map map = new HashMap();
        if(groups!=null){
            for (int i = 0; i < groups.length; i++) {
                if(groups[i]!=null){
//                    Set l = groups[i].players;
                    for (Iterator ite=groups[i].players.values().iterator();ite.hasNext();) {
                        PositionSprite p = (PositionSprite) ite.next();
                        if (p != player)
                            map.put(p, OLD);
                    }
                }
            }
        }
        if(newGroups!=null){
            for (int i = 0; i < newGroups.length; i++) {
                if(newGroups[i]!=null){
//                    Set l = newGroups[i].players;
                    for (Iterator ite=newGroups[i].players.values().iterator();ite.hasNext();) {
                    	PositionSprite p = (PositionSprite) ite.next();
                        if(p!=player){
                            Object o = map.get(p);
                            if(o==null){
                                map.put(p,NEW);
                            }
                            else if(o==OLD){
                                map.put(p,ALL);
                            }
                        }
                    }
                }
            }
        }
        if(player.getTeam()!=null){
        	PositionSprite[] players = player.getTeam().getPlayers();
            if(players.length>1){
                for(int i=0;i<players.length;i++){
                    if(players[i]!=player){
                        Integer state = (Integer)map.get(players[i]);
                        if(leave){
                            if(state==null){
                                map.put(players[i],OLD);
                            }
                        }else{
                            map.put(players[i],NEW);
                        }
                    }
                }
            }
        }
        Iterator ite = map.entrySet().iterator();
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            PositionSprite  p = (PositionSprite)entry.getKey();
            Integer state = (Integer)entry.getValue();
            if(state==OLD){
                sendPosition(player, p, mapId, x, y, true, state);
            }
            else if(state==NEW){
                sendPosition(player, p, mapId, x, y, false, state);
                Position position = (Position) player2position.get(new Integer(p.getId()));
                if(position!=null){
                    sendPosition(p, player,position.map.getMapId(), position.x, position.y, false, state);
                }
            }
            else{
                sendPosition(player, p, mapId, x, y, false, state);
            }
        }
    }

    public void sendPosition(PositionSprite src, PositionSprite dest, int mapId, int x,
                             int y, boolean leave, Integer state) {
        src.setPositionTime(System.currentTimeMillis());
        UWAPSegment seg = new UWAPSegment(ClientConstants.SEND_POSITION);
        seg.writeInt(src.getId());
        short flag = src.containsPosition(dest.getId())?(short)0:(short)1;
        
        byte lightLevel = 0;
        lightLevel = src.getLightLevel();
        
        if(leave){
            src.removePositionDest(dest.getId());
            dest.removePositionDest(src.getId());
            flag = 0;
            mapId = -1;
            lightLevel = 0;
        }
        int map = flag<<16|(mapId&0xFFFF);
        seg.writeInt(map);
        
        int position = ((short)x)<<16|(y&0xFFFF);
        
        Team team = src.getTeam();
        if(team != null && src != team.getLeader()){
	        WorldPlayer leader = (WorldPlayer)team.getLeader();
	        GameMap leaderMap = leader.getMap();
	        GameMap srcMap = src.getMap();
	        if(leaderMap != null && srcMap != null && src.getTeamState() == WorldPlayer.TEAM_FOLLOW && leaderMap.getInstance() != null && leaderMap.getMapId() != src.getMap().getMapId()){
	        	src.setTeamState(WorldPlayer.TEAM_NORMAL);
	        	UWAPSegment seg2 = new UWAPSegment(ClientConstants.TEAM_LEAVE, -1);
	            seg2.writeInt(team.getId());
	            seg2.writeInt(src.getId());
	            seg2.write((byte)WorldPlayer.TEAM_NORMAL);
	            PositionSprite[] members = team.getPlayers();
	            for (int i = 0; i < members.length; i++) {
	            	if(members[i] instanceof WorldPlayer){
	            		connectService.writeTo(seg2, members[i].getId());
	            	}
	            }
	            src.setNeedRefreshPosition(true);
	            log.info("Team follow error ID[" + src.getId() + "]");
	        }
        }
        
        seg.writeInt(position);
        if(flag==1||src.isNeedRefreshPosition()){
            src.addPositionDest(dest.getId());
            seg.writeString(src.getPlayerName());
            seg.write((byte)src.getFace());
            Client client = dest.getClient();
            if(client != null && client.getDataVersion() > 0){
            	 seg.write((byte)src.getSex());
            }
            seg.writeInt(src.getVipNewLevel());
            seg.writeShort((short) src.getLevel());
            byte teamState = 0;
            if (team != null) {
                if (team.getLeader() == src) {
                    teamState = 1;
                } else {
                    teamState = 2;
                }
            }
            seg.write(teamState);
            seg.writeString(src.getTongName());
            seg.write(src.getReturnTimes());
            Pet pet = src.getPet();
            if (pet == null) {
                seg.write((byte) - 1);
            } else {
                seg.write((byte) pet.getPetType());
                seg.writeString(pet.getName());
                seg.writeBoolean(pet.getBaby());
                seg.writeShort((short) pet.getLevel());
                //宠物的绑定类型 表示宠物的样子 第2代需求
                seg.write(pet.getBindType());
                //宠物颜色
                seg.writeShort(pet.getColorIndex());
                seg.writeInt(pet.getEvolutionLevel());
                seg.writeInt(pet.getEvolutionType());
            }
            seg.writeString(src.getTitle());
            seg.writeString(src.getCreditName());
            //modify
            //seg.writeInt(LEVEL_COLORS[src.getLevel()-dest.getLevel()+100]);
            seg.writeInt(Utils.getCampColor(src.getCamp(), dest.getCamp()));
            seg.writeBoolean(src.isInBattle());
            seg.write(src.hasBuf(Buf.GUARD)?1:(byte)0);
            //每个消息只发一个玩家，所有不用版本号区分
            seg.write(src.getCamp());
            src.setNeedRefreshPosition(false);
        }
        Client client = dest.getClient();
        if(client != null && client.getDataVersion() > 0){
        	 if (state == NEW || state == ALL) {
        		 seg.writeInt(1);
        		 seg.write(lightLevel);
        		//神圣宝辉等级和梦幻宝辉等级
        		 seg.write(src.getHolyGemLightLevel());
        		 seg.write(src.getFantasyGemLightLevel());
        	 } else {
        		 seg.writeInt(-1);
        	 }
        }
//        trySend(seg,dest.getId());
//        seg.write(src.hasBattle() ? (byte) 1 : (byte)0);
        connectService.writeTo(seg,dest.getId());
        if(leave){
            seg = new UWAPSegment(ClientConstants.SEND_POSITION);
            seg.writeInt(dest.getId());
            seg.writeInt(-1);
            position = ((short) dest.getX()) << 16 | (dest.getY() & 0xFFFF);
            seg.writeInt(position);
//            trySend(seg,src.getId());
            client = src.getClient();
            if (client != null && client.getDataVersion() > 0) {
            	seg.writeInt(1);
            	seg.writeInt(dest.getLightLevel());
            	//神圣宝辉等级和梦幻宝辉等级
            	seg.write(dest.getHolyGemLightLevel());
            	seg.write(dest.getFantasyGemLightLevel());
            }
//            seg.write(src.hasBattle() ? (byte) 1 : (byte)0);
            connectService.writeTo(seg,src.getId());
        }
    }


    private void trySend(UWAPSegment seg,int playerId){
        PositionCache cache = caches.get(playerId);
        if(cache==null){
            cache = new PositionCache();
            caches.put(playerId,cache);
        }
        if(System.currentTimeMillis()-cache.time<10000L){
            synchronized(cache.l){
                cache.l.add(seg);
            }
        }else{
            synchronized(cache.l){
                Iterator<UWAPSegment> ite = cache.l.iterator();
                while(ite.hasNext()){
                    connectService.writeTo(ite.next(),playerId);
                    ite.remove();
                }
                cache.time=System.currentTimeMillis();
            }
        }
    }


//    public void sendPosition(WorldPlayer src, WorldPlayer dest, int mapId, int x,
//                             int y, boolean leave) {
//        UWAPSegment seg = new UWAPSegment(ClientConstants.SEND_POSITION);
//        seg.writeInt(src.getId());
//        seg.writeString(src.getPlayerName());
//        seg.write(src.getSex());
//        seg.writeShort((short) src.getLevel());
//        if (leave) {
//            seg.writeShort((short) - 1);
//        } else {
//            seg.writeShort((short) mapId);
//        }
//        seg.writeShort((short) x);
//        seg.writeShort((short) y);
//        byte teamState = 0;
//        Team team = src.getTeam();
//        if(team!=null){
//            if(team.getLeader()==src){
//                teamState = 1;
//            }else{
//                teamState = 2;
//            }
//        }
//        seg.write(teamState);
//        seg.writeString(src.getTongName());
//        seg.write(src.getReturnTimes());
//        Pet pet = src.getPet();
//        if(pet==null){
//            seg.write((byte)-1);
//        }else{
//            seg.write((byte)pet.getPetType());
//            seg.writeString(pet.getName());
//            seg.writeBoolean(pet.getBaby());
//            seg.writeShort((short)pet.getLevel());
//        }
//        seg.writeString(src.getTitle());
//        seg.writeString(src.getCreditName());
//        connectService.writeTo(seg,dest.getId());
//    }

    public Group[] getNewGroups(GridManager[] grids,PositionSprite player){
        Group[] ret = new Group[grids.length];
        for(int i=0;i<grids.length;i++){
            if(grids[i]!=null){
                ret[i] = grids[i].addPlayer(player);
            }
        }
        return ret;
    }

//    public GridManager getGridManager(int mapId,int x,int y){
//        SceneManager manager = getSceneManager(mapId);
//        if(manager!=null){
//            return manager.getGridManager(x,y);
//        }
//        return null;
//    }

    public GridManager[] getGridManagers(GameMap map,int x,int y){
        SceneManager manager = getSceneManager(map);
        if(manager!=null){
            return manager.getGridManagers(x,y);
        }else{
            return null;
        }
    }

//    public GridManager[] getGridManagers(int mapId,int x,int y){
//        SceneManager manager = getSceneManager(mapId);
//        if(manager!=null){
//            return manager.getGridManagers(x,y);
//        }else{
//            return null;
//        }
//    }

//    public SceneManager getSceneManager(int mapId){
//        return (SceneManager)managers.get(new Integer(mapId));
//    }

    public SceneManager getSceneManager(GameMap map){
        return (SceneManager)managers.get(map);
    }

    class SceneManager{

        GameMap map;
        int width;
        int height;
        int gridWidth;
        int gridHeight;
        int col;
        int row;
        public GridManager[][] grids;

        public SceneManager(GameMap map,int width,int height,int gridWidth,int gridHeight){
            this.map = map;
            this.width = width;
            this.height = height;
            this.gridWidth = gridWidth;
            this.gridHeight = gridHeight;
            col = (width/gridWidth)+(width%gridWidth!=0?1:0);
            row = (height/gridHeight)+(height%gridHeight!=0?1:0);
            grids = new GridManager[col][row];
            int maxplayer = MAX_PLAYER;
            if(map.getMapId()==1617){
                maxplayer = 4;
            }else if(map.getMapId()==177){
            	maxplayer = 1;
            }
            for(int i=0;i<col;i++){
                for(int j=0;j<row;j++){
                    grids[i][j] = new GridManager(this,i*gridWidth,j*gridHeight,gridWidth,gridHeight,maxplayer);
                }
            }
        }

        int getMapId(){
            return map.getMapId();
        }

        int getWidth(){
            return width;
        }

        int getheight(){
            return height;
        }

        public int getGridWidth(){
            return gridWidth;
        }

        public int getGridHeight(){
            return gridHeight;
        }

        public boolean isChanged(int x,int y,int newX,int newY){
            int index = (x/gridWidth)*col+y/gridHeight;
            int v = x%gridWidth>=(gridWidth>>1)?1:-1;
            int h = y%gridHeight>=(gridHeight>>1)?1:-1;
            int index1 = (newX/gridWidth)*col+newY/gridHeight;
            int v1 = newX%gridWidth>=(gridWidth>>1)?1:-1;
            int h1 = newY%gridHeight>=(gridHeight>>1)?1:-1;
            return index!=index1||v!=v1||h!=h1;
        }

        public GridManager getGridManager(int x,int y){
            if(x<0||y<0||x>=col||y>=row)
                return null;
            return grids[x][y];
        }


        public GridManager[] getGridManagers(int x,int y){
            int c = x/gridWidth;
            int r = y/gridHeight;
            int v = x%gridWidth>=(gridWidth>>1)?1:-1;
            int h = y%gridHeight>=(gridHeight>>1)?1:-1;
            int[][] indexes = new int[4][2];
            if(v==1&&h==1){
                indexes[0][0] = c;
                indexes[0][1] = r;
                indexes[1][0] = c+1;
                indexes[1][1] = r;
                indexes[2][0] = c;
                indexes[2][1] = r+1;
                indexes[3][0] = c+1;
                indexes[3][1] = r+1;
            }
            else if(v==1&&h==-1){
                indexes[0][0] = c;
                indexes[0][1] = r-1;
                indexes[1][0] = c+1;
                indexes[1][1] = r-1;
                indexes[2][0] = c;
                indexes[2][1] = r;
                indexes[3][0] = c+1;
                indexes[3][1] = r;
            }
            else if(v==-1&&h==1){
                indexes[0][0] = c-1;
                indexes[0][1] = r;
                indexes[1][0] = c;
                indexes[1][1] = r;
                indexes[2][0] = c-1;
                indexes[2][1] = r+1;
                indexes[3][0] = c;
                indexes[3][1] = r+1;
            }
            else if(v==-1&&h==-1){
                indexes[0][0] = c-1;
                indexes[0][1] = r-1;
                indexes[1][0] = c;
                indexes[1][1] = r-1;
                indexes[2][0] = c-1;
                indexes[2][1] = r;
                indexes[3][0] = c;
                indexes[3][1] = r;
            }

            GridManager[] ret = new GridManager[4];
            for(int i=0;i<4;i++){
                ret[i] = getGridManager(indexes[i][0],indexes[i][1]);
            }
            return ret;
        }
        public void print(){
            for(int i=0;i<col;i++){
                for(int j=0;j<col;j++){
                    grids[i][j].print();
                }
            }
        }

        public int hashCode() {
            return map.getMapId() + (map.getInstance() == null ? 0 :
                    map.getInstance().getId());
        }
    }

    class GridManager{
        SceneManager manager;
        List groups = new ArrayList(10);
        int x,y,width,height;
        int maxPlayer;

        private Map players = new HashMap();

        public GridManager(SceneManager manager,int x,int y,int width,int height,int maxPlayer){
            this.manager = manager;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.maxPlayer = maxPlayer;
        }

        public Group addPlayer(PositionSprite player) {
            Group group = getGroup(player);
            if (group != null)
                return group;
            synchronized (groups) {
                for (int i = 0; i < groups.size(); i++) {
                    Group g = (Group) groups.get(i);
                    if (g.size() < maxPlayer) {
                        g.addPlayer(player);
                        players.put(new Integer(player.getId()), g);
                        return g;
                    }
                }
                group = new Group(this, x, y, width, height);
                groups.add(group);
                group.addPlayer(player);
                players.put(new Integer(player.getId()), group);
                return group;
            }
        }

        public Group getGroup(PositionSprite player){
            Group ret =  (Group)players.get(new Integer(player.getId()));
            if(ret!=null){
                ret.addPlayer(player);  //这里操作很特别，是做一个同步操作，防止某种情况下造成的group中的player跟现实中的player不一样
            }
            return ret;
        }

        public void removePlayer(PositionSprite player){
            players.remove(new Integer(player.getId()));
        }

        public void print(){
            if(players.size()!=0)
                System.out.println("x:"+x+" y:"+y+" players:"+players.size());
            for(int i=0;i<groups.size();i++){
                Group group = (Group)groups.get(i);
                if(group.size()!=0)
                    System.out.println("x:"+x+" y:"+y+" group "+i+":"+group.size());
            }
        }
    }

    class Group{
        GridManager grid;
        int x,y,width,height;
        Map players = new HashMap(10);
//        Set players = new HashSet(10);
        public Group(GridManager grid,int x,int y,int width,int height){
            this.grid = grid;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int size(){
            return players.size();
        }

        public void addPlayer(PositionSprite player){
            players.put(new Integer(player.getId()),player);
//            players.add(player);
        }

        public void removePlayer(PositionSprite player){
            players.remove(new Integer(player.getId()));
            grid.removePlayer(player);
        }

//        public void checkPlayer(PlayerData player){
//
//        }

    }

    class Position{
        Group[] groups = new Group[4];
        int x,y;
        GameMap map;
        public boolean changed(Group[] gs){
            return groups[0]==gs[0]&&groups[1]==gs[1]&&groups[2]==gs[2]&&groups[3]==gs[3];
        }
    }

}

class PositionCache{
    public List<UWAPSegment> l = new ArrayList<UWAPSegment>();
    public long time = System.currentTimeMillis();
}

