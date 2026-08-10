package com.pip.itimes.server.world.game;

import java.util.*;

import com.pip.itimes.server.stage.MonsterGroup;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.stage.Resource;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class GameMap {

    private Instance instance = null;
    private Scene scene = null;
    private short defaultX = 0;
    private short defaultY = 0;
    private short defaultTileX = 0;
    private short defaultTileY = 0;
    private short mapId = 0;
    private WorldService service = null;
    private Set players = new HashSet();

    private Map monsters = new HashMap(0);
    private Map resources = new HashMap(0);

    private boolean canCreateTeam = true;
    private boolean canPk = true;

    /**
     * “争夺地区”为随意偷袭地区
     */
    public static final int PK_TYPE_WAR = 2; 
    /**
     * “自由地区”为可偷袭地区
     */
    public static final int PK_TYPE_HALF = 0;
    
    /**
     * “安全地区”为受保护地区
     */
    public static final int PK_TYPE_GUARD = 1; 

    public GameMap(WorldService service,Scene scene,short defaultTileX,short defaultTileY) {
        this.defaultTileX = defaultTileX;
        this.defaultTileY = defaultTileY;
        this.service = service;
        this.scene = scene;
        this.mapId = scene.getMapId();
        MonsterGroup[] mgs = scene.getDynMonsterGroups();
        for(int i=0;i<mgs.length;i++){
            MonsterObject o = new MonsterObject(this,mgs[i],service);
            monsters.put(new Integer(o.getId()),o);
        }
        Resource[] rs = scene.getResources();
        for(int i=0;i<rs.length;i++){
            ResourceObject o = new ResourceObject(this,rs[i],service);
            resources.put(new Integer(o.getId()),o);
        }
    }

    public void setDefaultTileX(short defaultTileX){
        this.defaultTileX = defaultTileX;
    }

    public void setDefaultTileY(short defaultTileY){
        this.defaultTileY = defaultTileY;
    }

    public short getDefaultTileX(){
        return defaultTileX;
    }

    public short getDefaultTileY(){
        return defaultTileY;
    }

    public void setDefaultX(short defaultX){
        this.defaultX = defaultX;
    }

    public void setDefaultY(short defaultY){
        this.defaultY = defaultY;
    }

    public MonsterObject getMonsterGroup(int id){
        return (MonsterObject)monsters.get(new Integer(id));
    }

    public ResourceObject getResource(int id){
        return (ResourceObject)resources.get(new Integer(id));
    }

    public int getWidth(){
        return scene.getWidth();
    }

    public String getName() {
    	return scene.getName();
    }

    public int getHeight(){
        return scene.getHeight();
    }

    public void setInstance(Instance instance){
        this.instance = instance;
    }

    public void setCanPk(boolean canPk) {
        this.canPk = canPk;
    }

    public void setCanCreateTeam(boolean canCreateTeam) {
        this.canCreateTeam = canCreateTeam;
    }

    public Instance getInstance(){
        return instance;
    }

//    public boolean canEnter(int player){
//        return true;
//    }

    public short getMapId(){
        return mapId;
    }

    public short getDefaultX(){
        return defaultX;
    }

    public short getDefaultY(){
        return defaultY;
    }

    public boolean isCanPk() {
        return canPk;
    }

    public boolean isCanCreateTeam() {
        if(!service.canCreateTeam(getMapId()))
            return false;
        return canCreateTeam;
    }


    public boolean canAdd(WorldPlayer player){
        if(instance==null)
            return true;
        else
            return instance.canAdd(player);
    }

    public boolean preAddPlayers(WorldPlayer[] players){
        if(instance==null)
            return true;
        else{
            synchronized (instance) {
                try {
                    instance.preAdd(players);
                    return true;
                } catch (InstanceException ex) {
                    return false;
                }
            }
        }
    }

    public void addPlayer(WorldPlayer player){
        if(instance==null){
            players.add(player);
            GameMap oldMap = player.getMap();
            if(oldMap!=this){
                player.setMap(this);
                service.playerAddedToMap(this, player);
            }
//            return true;
        }else{
            synchronized (instance) {
//                if(instance.canAdd(player)){
                players.add(player);
                instance.addPlayer(player);
                instance.setActive(player.getId());
                GameMap oldMap = player.getMap();
                if (oldMap != this) {
//                    if (oldMap != null)
//                        oldMap.removePlayer(player, true);
//                    players.add(player);
                    player.setMap(this);
                    service.playerAddedToMap(this, player);
                }
//                    return true;
//                }else
//                    return false;
//            }
            }
        }
    }

    public void removePlayer(WorldPlayer player,boolean force){
        if(instance==null){
            players.remove(player);
        }else{
        	players.remove(player);
        	
            if(force && !(instance instanceof CampBattlefieldInstance)){
                synchronized (instance) {
                    instance.removeActive(player.getId());
                }
            }
        }
    }

    public void kickPlayer(int playerId){

    }


    public int hashCode(){
        int iTotal = 17*37+getMapId();
        iTotal = iTotal*37+(instance==null?0:instance.getId());
        return iTotal;
    }

    public WorldPlayer[] getPlayers(){
        WorldPlayer[] ret = new WorldPlayer[players.size()];
        players.toArray(ret);
        return ret;
    }

    public IServerObject[] getMonsters(int status){
        List l = new ArrayList(30);
        Iterator ite = monsters.values().iterator();
        while(ite.hasNext()){
            MonsterObject m = (MonsterObject)ite.next();
            if(m.getStatus()==status){
                l.add(m);
            }
        }
        IServerObject[] ret = new IServerObject[l.size()];
        l.toArray(ret);
        return ret;
    }

    public IServerObject[] getResources(int status){
        List l = new ArrayList(30);
        Iterator ite = resources.values().iterator();
        while (ite.hasNext()) {
            ResourceObject r = (ResourceObject) ite.next();
            if (r.getStatus() == status) {
                l.add(r);
            }
        }
        IServerObject[] ret = new IServerObject[l.size()];
        l.toArray(ret);
        return ret;
    }

    public int getTileWidth(){
        return 16;
    }

    public int getTileHeight(){
        if(scene.getType()==0){
            return 16;
        }
        return 8;
    }

    public void setScene(Scene scene){
        this.scene = scene;
    }
    
    public Scene getScene() {
    	return scene;
    }

    public int getPkType(){
        return scene.getPkType();
    }
    
    public byte getSafeType(){
    	return scene.getSafeType();
    }
}
