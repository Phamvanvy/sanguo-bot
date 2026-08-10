package com.pip.itimes.server.world.game;

import java.util.HashMap;
import java.util.Map;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.stage.Stage;
import com.pip.itimes.server.world.*;

import org.apache.log4j.Logger;
import java.util.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class WorldService implements IRefreshCallback {


    private final static Logger log = Logger.getLogger(WorldService.class);

    private StageService stageService;
    private PositionService positionService;
    private RefreshService refreshService = new RefreshService();
    private ConnectService connectService;
    private ChatService chatService;

    private Map noInstanceMap = new HashMap();


    private InstanceService instanceService = null;

    private Map instanceScene = new HashMap();

    private NormalInstanceModel normalInstanceModel = null;
    private BattleFieldInstanceModel battleFieldModel = null;
    private GuildBattleFieldInstanceModel guildBattleFieldModel = null;
    private HouseInstanceModel houseModel = null;
    private FarmInstanceModel farmInstanceModel = null;
    private BattleForResourcesInstanceModel resourcesModel = null;
    private Set teamForbidens = new HashSet();
//    private BattleFieldService battleFieldService = null;

    public WorldService(StageService stageService,
                        PositionService positionService,InstanceService instanceService) throws Exception {
        this.stageService = stageService;
        this.positionService = positionService;
        this.instanceService = instanceService;
        normalInstanceModel = new NormalInstanceModel(this,instanceService);
//        load();
        refreshService.start();
    }

    public void setConnectService(ConnectService connectService){
        this.connectService = connectService;
    }
    
    public ConnectService getConnectService(){
    	return connectService;
    }

    public RefreshService getRefreshService(){
        return refreshService;
    }
    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }
    public ChatService getChatService() {
        return chatService;
    }
//    public void setBattleFieldService(BattleFieldService battleFieldService){
//        this.battleFieldService = battleFieldService;
//    }
    
    public void setResourcesModel (BattleForResourcesInstanceModel resourcesModel) {
    	this.resourcesModel = resourcesModel;
    }
    
    public BattleForResourcesInstanceModel getResourcesModel () {
    	return resourcesModel;
    }

    public void setHouseModel(HouseInstanceModel model){
        this.houseModel = model;
    }
    
    public void setFarmInstanceModel(FarmInstanceModel farmInstanceModel){
    	this.farmInstanceModel = farmInstanceModel;
    }

    public void setBattleField(BattleFieldInstanceModel model){
        this.battleFieldModel = model;
    }

    public void setGuildBattleField(GuildBattleFieldInstanceModel model){
        this.guildBattleFieldModel = model;
    }

    public void load () {
        InstanceDefinition[] definitions = stageService.getInstance();
        for (int i=0;i<definitions.length;i++) {
            instanceService.addDefinition(definitions[i]);
            if ("normal".equals(definitions[i].getType())) {
                definitions[i].setModel(normalInstanceModel);
            } else if ("battlefield".equals(definitions[i].getType())) {
                definitions[i].setModel(battleFieldModel);
            } else if ("guildbattlefield".equals(definitions[i].getType())) {
                definitions[i].setModel(guildBattleFieldModel);
            } else if ("house".equals(definitions[i].getType())) {
                definitions[i].setModel(houseModel);
            } else if (CampBattlefieldConfig.CAMP_BATTLEFIELD_TYPE_RESOURCES.equals(definitions[i].getType())) {
            	definitions[i].setModel(resourcesModel);
            } else if ("farm".equals(definitions[i].getType())){
            	definitions[i].setModel(farmInstanceModel);
            }
        }
        Stage[] stages = stageService.getStages();
        for(int i=0;i<stages.length;i++){
            loadStage(stages[i]);
        }
        setMapDefaultPosition(definitions);
        teamForbidens = stageService.getTeamForbidens();
    }

    public void reload(){
        InstanceDefinition[] definitions = stageService.getInstance();
        for(int i=0;i<definitions.length;i++){
            if(instanceService.getInstanceDefinition(definitions[i].getId())==null){
                instanceService.addDefinition(definitions[i]);
            }
        }
        Stage[] stages = stageService.getStages();
        for(int i=0;i<stages.length;i++){
            loadStage(stages[i]);
        }
        setMapDefaultPosition(definitions);
    }

    private void setMapDefaultPosition(InstanceDefinition[] definitions) {
        for (int i = 0; i < definitions.length; i++) {
            short mapId = definitions[i].getEntrance();
            GameMap map = getNoInstanceMap(mapId);
            if (map != null) {
                map.setDefaultX(definitions[i].getEntrancePixelX());
                map.setDefaultY(definitions[i].getEntrancePixelY());
                map.setDefaultTileX(definitions[i].getEntranceX());
                map.setDefaultTileY(definitions[i].getEntranceY());
            }
        }
    }

    private void loadStage(Stage stage) {
        Scene[] scenes = stage.getScenes();
        for (int j = 0; j < scenes.length; j++) {
            short id = scenes[j].getMapId();
            if (getInstanceDefinition(id) == null) { //如果不是副本
                short defaultX = 0;
                short defaultY = 0;
                if (id == stage.getDefaultMapId()) {
                    defaultX = stage.getDefaultX();
                    defaultY = stage.getDefaultY();
                }
                GameMap map = (GameMap)noInstanceMap.get(new Short(id));
                if(map==null){
                    map = new GameMap(this, scenes[j], defaultX,
                                              defaultY);
                    map.setInstance(null);
                    noInstanceMap.put(new Short(map.getMapId()), map);
                    positionService.addMap(map);
                }else{
                    map.setScene(scenes[j]);
                }
            }
            if(instanceScene.get(new Short(id))==null)
                instanceScene.put(new Short(id), scenes[j]);
        }
    }

    public Instance createInstance (InstanceDefinition idf) {
        GameMap entrance = getNoInstanceMap(idf.getEntrance());
        int id = InstanceService.getNewInstanceId();
        Instance instance = new Instance(id,idf,instanceService);
        instance.setEntrance(entrance);
        short[] maps = idf.getMaps();
        for(int i=0;i<maps.length;i++){
            Scene scene = getInstanceScene(maps[i]);
            GameMap map = new GameMap(this,scene,(short)0,(short)0);
            map.setCanPk(false);
            instance.addMap(map);
            map.setInstance(instance);
            positionService.addMap(map);
        }
        instanceService.addInstance(instance);
        return instance;
    }

    public void instanceCreated(Instance instance){
        GameMap[] maps = instance.getMaps();
        for(int i=0;i<maps.length;i++){
            positionService.addMap(maps[i]);
        }
    }

    public void instanceRemoved(Instance instance){
        GameMap[] maps = instance.getMaps();
        for(int i=0;i<maps.length;i++){
            positionService.removeMap(maps[i]);
        }
    }

    public Instance createInstance(int instanceId){
        InstanceDefinition idf = instanceService.getInstanceDefinition(instanceId);
        return createInstance(idf);
    }

    public Scene getInstanceScene(short mapId){
        return (Scene)instanceScene.get(new Short(mapId));
    }

    public boolean canCreateTeam(int mapId){
        return !teamForbidens.contains(mapId);
    }

    //登陆的时候从此取Map，
//    public GameMap getLoginMap(WorldPlayer player,short mapId){
//        return getMap(player,mapId);
//        InstanceDefinition idf = instanceService.getInstanceDefintion(mapId);
//        if(idf==null){  //如果不是副本
//            return getNoInstanceMap(mapId);
//        }else{
//            return idf.getModel().getGameMap(player,mapId);
//        }
//        GameMap map = getMap(player,mapId);
//        if(map!=null){
//            Instance instance = map.getInstance();
//            if(instance==null){
//                return map;
//            }else{
//                if(instance.isTimeOut()){
//                    instanceService.removeInstance(instance);
//                    return instance.getEntrance();
//                }else{
//                    if(map.canAdd(player)){
//                        return map;
//                    }else{
//                        return instance.getEntrance();
//                    }
//                }
//            }
//        }else{
//            InstanceDefinition idf = getInstanceDefinition(mapId);
//            return getNoInstanceMap(idf.getEntrance());
//        }
//    }



//    public GameMap tryEnterMap(WorldPlayer player,short mapId){
//        GameMap map = getMap(player,mapId);
//        if(map!=null){
//            if(map.addPlayer(player)){
//                return map;
//            }else{
//                return map.getInstance().getEntrance();
//            }
//        }else{
//            InstanceDefinition idf = getInstanceDefinition(mapId);
//            return getNoInstanceMap(idf.getEntrance());
//        }
//    }

    public GameMap changeMap(WorldPlayer player,GameMap oldMap,short mapId){
        GameMap map = getMap(player,mapId,false);
        if(map==oldMap){
            return map;
        }else{
            if(map.getInstance()!=null&&(map.getInstance()==oldMap.getInstance())){
                oldMap.removePlayer(player,false);
                map.addPlayer(player);
            }else{
                oldMap.removePlayer(player,true);
                map.addPlayer(player);
            }
        }
        return map;
//        if(map!=null){
//            if(map.addPlayer(player)){
//                return map;
//            }else{
//                return map.getInstance().getEntrance();
//            }
//        }
//        return null;
//        else{
//            //有问题，副本应该在传送前建立好
//            InstanceDefinition definition = getInstanceDefinition(mapId);
//            Instance instance = createInstance(definition);
//            map =instance.getMap(mapId);
//            map.addPlayer(player);
//            return map;
//        }
    }


    public GameMap getMap(WorldPlayer player,short mapId,boolean login){
        InstanceDefinition definition = getInstanceDefinition(mapId);
        if(definition!=null){
            GameMap ret = null;
            if(login){
            	ret = definition.getModel().getLoginMap(player,mapId);  
            }else{
                ret = definition.getModel().getGameMap(player, mapId);
            }
            if(ret==null){
                return getNoInstanceMap(definition.getEntrance());
            }
            return ret;
        }else{
            return getNoInstanceMap(mapId);
        }
    }


    public GameMap getNoInstanceMap(short mapId){
        return (GameMap)noInstanceMap.get(new Short(mapId));
    }


    public InstanceDefinition getInstanceDefinition(short mapId){
        return instanceService.getInstanceDefintionByMap(mapId);
    }

    public Instance gotoInstance(int instanceId,WorldPlayer player) throws InstanceException{
        InstanceDefinition definition = instanceService.getInstanceDefinition(instanceId);
        if(definition==null){
            log.info("ID["+player.getId()+"]GoInstance["+instanceId+"] Error");
            throw new InstanceException("副本错误");
        }
        return definition.getModel().tryGotoInstance(instanceId, player, -1);
//        Instance instance = instanceService.getInstance(player,instanceId);
//        if(instance!=null&&instance.isTimeOut()){
//            instanceService.removeInstance(instance);
//            instance = null;
//        }
//        if(instance!=null){
//            WorldPlayer[] players = new WorldPlayer[]{player};
//            Team team = player.getTeam();
//            if(team!=null){
//                players = team.getPlayers();
//            }
//            boolean ret = instance.preAdd(players);
//            if(ret){
//                return instance;
//            }
//            return null;
//        }else{
//            instance = createInstance(instanceId);
//            WorldPlayer[] players = new WorldPlayer[]{player};
//            Team team = player.getTeam();
//            if(team!=null){
//                players = team.getPlayers();
//            }
//            boolean ret = instance.preAdd(players);
//            if(ret){
//                return instance;
//            }
//            return null;
//        }
    }
    
    public Instance gotoBattlefieldInstance (int ID, WorldPlayer player, int battleID) throws InstanceException {
    	Instance instance = instanceService.getInstance(ID);
    	if (instance == null) {
    		log.info("ID[" + player.getId() + "] GoInstance ID[" + ID + "] Error");
    		connectService.sendMessage(player.getId(), "阵营战场错误");
    		return null;
    	}
        InstanceDefinition definition = instanceService.getInstanceDefinition(instance.getInstanceId());
        if (definition == null) {
            log.info("ID[" + player.getId() + "] GoInstance instanceID[" + instance.getInstanceId() + "] Error");
            connectService.sendMessage(player.getId(), "阵营战场错误");
            return null;
        }
        
        return definition.getModel().tryGotoInstance(ID, player, battleID);
    }

    public void objectCreated(IServerObject object) {
        UWAPSegment seg = new UWAPSegment(ClientConstants.REFRESH);
        seg.writeShort((short) 1);
        seg.write((byte) 1);
        seg.writeInt(object.getId());
        //modify
        seg.writeInt((object.getX()&0xFFFF)<<16|(object.getY()&0xFFFF));
        GameMap map = object.getMap();
        WorldPlayer[] players = map.getPlayers();
        for(int i=0;i<players.length;i++){
            connectService.writeTo(seg,players[i].getId());
        }
    }


    public void objectDisappeared(IServerObject object) {
        UWAPSegment seg = new UWAPSegment(ClientConstants.REFRESH);
        seg.writeShort((short) 1);
        seg.write((byte) 0);
        seg.writeInt(object.getId());
        //modify
        seg.writeInt((object.getX()&0xFFFF)<<16|(object.getY()&0xFFFF));
        GameMap map = object.getMap();
        WorldPlayer[] players = map.getPlayers();
        for(int i=0;i<players.length;i++){
            connectService.writeTo(seg,players[i].getId());
        }
    }

    public void playerAddedToMap(GameMap map, WorldPlayer player) {
        IServerObject[] monsters = map.getMonsters(IServerObject.STATUS_INVISIBLE);
        IServerObject[] resources = map.getResources(IServerObject.STATUS_VISIBLE);
        if (monsters.length+resources.length > 0) {
            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                              REFRESH);
            seg.writeShort((short) (monsters.length+resources.length));
            for (int i = 0; i < monsters.length; i++) {
                seg.write((byte) 0);
                seg.writeInt(monsters[i].getId());
                seg.writeInt((monsters[i].getX()&0xFFFF)<<16|(monsters[i].getY()&0xFFFF));
            }
            for(int i=0;i< resources.length;i++){
                seg.write((byte)1);
                seg.writeInt(resources[i].getId());
                seg.writeInt((resources[i].getX()&0xFFFF)<<16|(resources[i].getY()&0xFFFF));
            }
            connectService.writeTo(seg, player.getId());
        }
    }

//    public void enter(WorldPlayer player) throws BattleFieldException{
//        battleFieldService.enter(player);
//    }
//
//    public void enterfor(WorldPlayer player) throws BattleFieldException{
//        battleFieldService.enterfor(player);
//    }

}
