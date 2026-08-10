package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Jeffery
 * @version 1.0
 */
public class Scene{

    private byte id;
    private byte type;
    private byte width;
    private byte height;
    private String name;
    private List mapNpcs = new ArrayList();
    private List fixMonsterGroups = new ArrayList();
    private List dynMonsterGroups = new ArrayList();
    private List fixNpcs = new ArrayList();
    private List dynNpcs = new ArrayList();
    private Map resources = new TreeMap();
    private List doors = new ArrayList();
    private byte[] mapDesc;
    private byte[] addition;
    private int pkType;
    private byte safeType;		// «∑Òø…“‘π“∂‹

    private short mapId;

    public Scene() {
    }

    public void setMapId(short mapId){
        this.mapId = mapId;
    }

    public short getMapId(){
        return mapId;
    }

    public void setId(byte id){
        this.id = id;
    }

    public byte getId(){
        return id;
    }

    public void setType(byte type){
        this.type = type;
    }

    public byte getType() {
        return type;
    }

    public void setWidth(byte width){
        this.width = width;
    }

    public byte getWidth() {
        return width;
    }


    public void setHeight(byte height){
        this.height = height;
    }

    public byte getHeight() {
        return height;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addMapNpc(MapNpc mapNpc){
        mapNpcs.add(mapNpc);
    }

    public MapNpc[] getMapNpcs() {
        MapNpc[] ret = new MapNpc[mapNpcs.size()];
        mapNpcs.toArray(ret);
        return ret;
    }

    public void addMonsterGroup(MonsterGroup monsterGroup){
        if((monsterGroup.getType()&0x01)==0){
            fixMonsterGroups.add(monsterGroup);
        }else{
            dynMonsterGroups.add(monsterGroup);
        }
    }

    public MonsterGroup[] getFixMonsterGroups() {
        MonsterGroup[] ret = new MonsterGroup[fixMonsterGroups.size()];
        fixMonsterGroups.toArray(ret);
        return ret;
    }

    public MonsterGroup[] getDynMonsterGroups() {
        MonsterGroup[] ret = new MonsterGroup[dynMonsterGroups.size()];
        dynMonsterGroups.toArray(ret);
        return ret;
    }

    public MonsterGroup getMonsterGroup(int mgId){
        for(int i=0;i<dynMonsterGroups.size();i++){
            MonsterGroup mg = (MonsterGroup)dynMonsterGroups.get(i);
            if(mg.getId()==mgId){
                return mg;
            }
        }
        for(int i=0;i<fixMonsterGroups.size();i++){
            MonsterGroup mg = (MonsterGroup)fixMonsterGroups.get(i);
            if(mg.getId()==mgId){
                return mg;
            }
        }
        return null;
    }

    public void addNpc(Npc npc){
        if((npc.getFlag()&0x04)==0){
            fixNpcs.add(npc);
        }else{
            dynNpcs.add(npc);
        }
    }

    public Npc[] getFixNpcs() {
        Npc[] ret = new Npc[fixNpcs.size()];
        fixNpcs.toArray(ret);
        return ret;
    }


    public Npc[] getDynNpcs() {
        Npc[] ret = new Npc[dynNpcs.size()];
        dynNpcs.toArray(ret);
        return ret;
    }

    public void addResource(Resource resource){
        resources.put(new Integer(resource.getId()),resource);
    }

    public Resource[] getResources() {
        Resource[] ret = new Resource[resources.size()];
        resources.values().toArray(ret);
        return ret;
    }

    public Resource getResource(int resourceId){
        return (Resource)resources.get(new Integer(resourceId));
    }

    public void addDoor(Door door){
        doors.add(door);
    }

    public Door[] getDoors() {
        Door[] ret = new Door[doors.size()];
        doors.toArray(ret);
        return ret;

    }

    public void setMapDesc(byte[] mapDesc){
        this.mapDesc = mapDesc;
    }

    public byte[] getMapDesc(){
        return mapDesc;
    }

    public byte[] getAddition(){
        return addition;
    }

    public int getPkType() {
        return pkType;
    }

    public void setAddition(byte[] addition){
        this.addition = addition;
    }

    public void setPkType(int pkType) {
        this.pkType = pkType;
    }
    
    public byte getSafeType(){
    	return safeType;
    }
    
    public void setSafeType(byte safeType){
    	this.safeType = safeType;
    }
    
}
