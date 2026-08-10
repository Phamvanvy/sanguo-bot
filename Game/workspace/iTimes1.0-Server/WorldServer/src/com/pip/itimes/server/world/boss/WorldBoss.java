/**
 * @author leo
 */
package com.pip.itimes.server.world.boss;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.pip.itimes.server.stage.Monster;
import com.pip.itimes.server.stage.MonsterGroup;
import com.pip.itimes.server.stage.RoadPoint;
import com.pip.itimes.server.stage.Scene;
import com.pip.itimes.server.stage.Stage;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.StageService;
import com.pip.itimes.server.world.WorldPlayer;

/**
 * @author leo
 *
 */
public class WorldBoss{
    private MonsterGroup monsterGroup;
    private Monster[] monsters;
    private StageService stageService;

    private int mapId;
    
    public int getMapId() {
		return mapId;
	}
	public void setMapId(int mapId) {
		this.mapId = mapId;
	}

	private int groupId;

    public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	private short walkSpeed;
    private short catchSpeed;
    private List<byte[]> wayPointList = new ArrayList<byte[]>();
    private int range;
    private byte state;
    public byte getState() {
		return state;
	}
	public void setState(byte state) {
		this.state = state;
	}

	private int x;
    private int y;
    
    private int refreshTick = -1;
    
    private int sceneWidth;
    private int sceneHeight;
    private int tileWidth;
    private int tileHeight;
    
    private Random rand = new Random(System.currentTimeMillis());
    
    public static final byte STATE_SHOW = 0;
    public static final byte STATE_HIDE = 1;
    public static final byte STATE_DESTROY = 2;
    
    private byte trick;
    
    public WorldBoss(StageService stageService, BossDefine bossDefine){
    	this.stageService = stageService;
    	init(bossDefine);
    }
    
    public void init(BossDefine bossDefine){
    	groupId = bossDefine.getGroupId();
    	
        monsterGroup = stageService.getMonsterGroup(groupId);
        if(monsterGroup == null){//空怪物返回
        	return;
        }
        monsters = stageService.getMonsters(monsterGroup.getId());
        
        int monsterMaxLevel = 1;
        
        for(int i = 0; i < monsters.length; i++){
            if(monsters[i].getLevel() > monsterMaxLevel){
                monsterMaxLevel = monsters[i].getLevel();
            }
        }
        bossDefine.setMaxLevel(monsterMaxLevel);
        

        walkSpeed = 10;
        catchSpeed = 30;
        state = STATE_SHOW;
        int index = rand.nextInt(bossDefine.getBossDefineMapSize());
       
        int[] map = bossDefine.getBossMapDefine(index);
        
        if(map == null){
        	return;
        }
        bossDefine.setMapId(map[0]);
        mapId = (short) map[0];
        short stageId = Utils.getStageId((short) mapId);
        Stage stage = stageService.getStage(stageId);
        Scene scene = stage.getScene(mapId&0xF);
    
        sceneWidth = scene.getWidth();
        sceneHeight = scene.getHeight();
        
        tileWidth = 16;
        tileHeight = 16;
        
        if(scene.getType() == 1){ //精确地图
            tileHeight = 8;
        }
        //随机地标先做预留
      /*  x = correctTileX(map[1] + (rand.nextInt(map[3]) - 5));
        y = correctTileY(map[2] + (rand.nextInt(map[3]) - 5));*/
        x = correctTileX(map[1]);
        y = correctTileY(map[2]);
        
        wayPointList.clear();
        RoadPoint[] roadPoints = monsterGroup.getRoadPoints();
        int xdiff = x - monsterGroup.getTileX();
        int ydiff = y - monsterGroup.getTileY();
        for(int i = 0; i < roadPoints.length; i++){
            byte[] point = new byte[2];
            point[0] = (byte)correctTileX(roadPoints[i].x + xdiff);
            point[1] = (byte)correctTileY(roadPoints[i].y + ydiff);
            wayPointList.add(point);
        }
        
        range = correctRange();
    	
        trick = bossDefine.getTrick();
    }
    
    public byte getTrick() {
		return trick;
	}
	public void setTrick(byte trick) {
		this.trick = trick;
	}
	private int correctTileX(int tileX){
        if(tileX < 0){
            tileX = 0;
        }else if(tileX > sceneWidth){
            tileX = sceneWidth;
        }
        
        return tileX;
    }
    
    private int correctTileY(int tileY){
        if(tileY < 0){
            tileY = 0;
        }else if(tileY > sceneHeight){
            tileY = sceneHeight;
        }
        
        return tileY;
    }
    
    private int correctRange(){
        int mgr = monsterGroup.getRange();
        
        byte rx = (byte)((mgr >> 24) & 0xFF);
        byte ry = (byte)((mgr >> 16) & 0xFF);
        byte rw = (byte)((mgr >> 8) & 0xFF);
        byte rh = (byte)(mgr & 0xFF);
        
        byte rx1 = (byte)correctTileX(rx + (x - monsterGroup.getTileX()));
        byte ry1 = (byte)correctTileY(ry + (y - monsterGroup.getTileY()));
        byte rw1 = rw;
        byte rh1 = rh;
        
        if(rx1 + rw1 > sceneWidth){
            rw1 = (byte)(sceneWidth - rx1);
        }
        
        if(ry1 + rh1 > sceneHeight){
            rh1 = (byte)(sceneHeight - ry1);
        }
        
        return (rx1 << 24) | (ry1 << 16) | (rw1 << 8) | rh1;
    }

    
    public void destroy(){
        state = STATE_DESTROY;
    }

    public byte[] toClientBytes(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try{
            //boss的monsterGroup信息
            dos.writeInt(groupId);
            dos.writeShort(monsterGroup.getIconId());
            dos.writeByte(monsterGroup.getType());
            dos.writeShort(monsterGroup.getRefreshSecond());
            dos.writeByte(x);
            dos.writeByte(y);
            dos.writeByte(monsterGroup.getSide());
            dos.writeByte(monsterGroup.getEyeshot());
            dos.writeInt(range);
            dos.writeByte(wayPointList.size());
            for(int j = 0; j < wayPointList.size(); j++){
                byte[] point = wayPointList.get(j);
                dos.writeByte(point[0]);
                dos.writeByte(point[1]);
            }
            byte[] mIds = monsterGroup.getMonstersId();
            short[] probabilities = monsterGroup.getProbabilities();
            dos.writeByte(mIds.length);
            for(int j = 0; j < mIds.length; j++){
                dos.writeByte(mIds[j]);
                dos.writeShort(probabilities[j]);
            }
            dos.writeShort(walkSpeed); //新加的monsterGroup在地图上的移动速度，单位：像素/10个cycle
            dos.writeShort(catchSpeed); //新加的monsterGroup在地图上的追击速度，单位：像素/10个cycle

            //boss的monsterGroup中的小怪信息
            dos.writeByte((byte) monsters.length);
            for(int i = 0; i < monsters.length; i++){
                Monster monster = (Monster) monsters[i];

                dos.writeByte(monster.getIndex()); //新加，小怪的索引
                dos.writeShort(monster.getPngId());
                dos.writeUTF(monster.getName());
                dos.writeByte(monster.getType());
                dos.writeShort(monster.getLevel());
                dos.writeShort(monster.getVit());
                dos.writeShort(monster.getStr());
                dos.writeShort(monster.getInt());
                dos.writeShort(monster.getAgi());
                dos.writeShort(monster.getPMinAttack());
                dos.writeShort(monster.getPMaxAttack());
                dos.writeShort(monster.getPDef());
                dos.writeShort(monster.getMMinAttack());
                dos.writeShort(monster.getMMaxAttack());
                dos.writeShort(monster.getMDef());
                dos.writeShort(monster.getParry());
                dos.writeShort(monster.getHit());
                dos.writeShort(monster.getPCritial());
                dos.writeShort(monster.getMCritial());
                dos.writeInt(monster.getHp());
                dos.writeInt(monster.getMp());
                dos.writeByte(monster.getPetType());

                int[] abis = monster.getAbilities();
                dos.writeByte(abis.length);

                for(int j = 0; j < abis.length; j++){
                    dos.writeShort(abis[j]);
                }
            }
            return bos.toByteArray();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                dos.close();
            }catch(Exception e){
            }
        }

        return null;
    }

    public byte[] getRefreshData(WorldBoss worldBoss){
        refreshTick++;
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try{
            dos.writeInt(groupId);
            dos.writeByte(state); //出现还是消失

            if(state == STATE_SHOW){
                dos.writeByte((byte) x);
                dos.writeByte((byte) y);
                dos.writeInt(range);
                dos.writeByte((byte) wayPointList.size());

                for(int i = 0; i < wayPointList.size(); i++){
                    byte[] point = wayPointList.get(i);
                    dos.writeByte(point[0]);
                    dos.writeByte(point[1]);
                }

                dos.writeShort(walkSpeed);
                dos.writeShort(catchSpeed);
                
                // 如果客户端没有此BOSS信息则创建。并同步
                dos.writeInt(worldBoss.groupId);
                dos.writeShort(worldBoss.monsterGroup.getIconId());
                dos.writeByte(worldBoss.monsterGroup.getType());
                dos.writeShort(worldBoss.monsterGroup.getRefreshSecond());
                dos.writeByte(worldBoss.x);
                dos.writeByte(worldBoss.y);
                dos.writeByte(worldBoss.monsterGroup.getSide());
                dos.writeByte(worldBoss.monsterGroup.getEyeshot());
                dos.writeInt(worldBoss.range);
                dos.writeByte(worldBoss.wayPointList.size());
                for(int j = 0; j < worldBoss.wayPointList.size(); j++){
                    byte[] point = worldBoss.wayPointList.get(j);
                    dos.writeByte(point[0]);
                    dos.writeByte(point[1]);
                }
                byte[] mIds = worldBoss.monsterGroup.getMonstersId();
                short[] probabilities = worldBoss.monsterGroup.getProbabilities();
                dos.writeByte(mIds.length);
                for(int j = 0; j < mIds.length; j++){
                    dos.writeByte(mIds[j]);
                    dos.writeShort(probabilities[j]);
                }
                dos.writeShort(worldBoss.walkSpeed); //新加的monsterGroup在地图上的移动速度，单位：像素/10个cycle
                dos.writeShort(worldBoss.catchSpeed); //新加的monsterGroup在地图上的追击速度，单位：像素/10个cycle

                //boss的monsterGroup中的小怪信息
                dos.writeByte((byte) worldBoss.monsters.length);
                for(int i = 0; i < worldBoss.monsters.length; i++){
                    Monster monsters = (Monster) worldBoss.monsters[i];

                    dos.writeByte(monsters.getIndex()); //新加，小怪的索引
                    dos.writeShort(monsters.getPngId());
                    dos.writeUTF(monsters.getName());
                    dos.writeByte(monsters.getType());
                    dos.writeShort(monsters.getLevel());
                    dos.writeShort(monsters.getVit());
                    dos.writeShort(monsters.getStr());
                    dos.writeShort(monsters.getInt());
                    dos.writeShort(monsters.getAgi());
                    dos.writeShort(monsters.getPMinAttack());
                    dos.writeShort(monsters.getPMaxAttack());
                    dos.writeShort(monsters.getPDef());
                    dos.writeShort(monsters.getMMinAttack());
                    dos.writeShort(monsters.getMMaxAttack());
                    dos.writeShort(monsters.getMDef());
                    dos.writeShort(monsters.getParry());
                    dos.writeShort(monsters.getHit());
                    dos.writeShort(monsters.getPCritial());
                    dos.writeShort(monsters.getMCritial());
                    dos.writeInt(monsters.getHp());
                    dos.writeInt(monsters.getMp());
                    dos.writeByte(monsters.getPetType());

                    int[] abis = monsters.getAbilities();
                    dos.writeByte(abis.length);

                    for(int j = 0; j < abis.length; j++){
                        dos.writeShort(abis[j]);
                    }
                }
            }
            
            return bos.toByteArray();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                dos.close();
            }catch(Exception e){
            }
        }

        return null;
    }
}
