package com.pip.itimes.server.stage;

import java.io.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

/**
 * @author Jeffery
 * @version 1.0
 */
public class StageLoader {

    private static final Logger log = Logger.getLogger(StageLoader.class);

    private static final byte[] HEAD = {'R','P','G'};

    public static Stage getStage(File file) throws Exception{
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] data = new byte[(int)file.length()];
        bis.read(data);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
        byte[] head = new byte[3];
        dis.readFully(head);
        for(int i=0;i<3;i++){
                if(head[i]!=HEAD[i])
                        throw new Exception("Not stage file");
        }
        Stage stage = new Stage();
        byte retCode = dis.readByte();
        stage.setRetCode(retCode);
        int size = dis.readInt();
        short version = dis.readShort();
        stage.setVersion(version);
        short crc = dis.readShort();
        short segSize = dis.readShort(); //data size;
        short stageId = dis.readShort();
        stage.setId(stageId);
        String stageName = dis.readUTF();
        stage.setName(stageName);
        byte musicId = dis.readByte();
        stage.setMusicId(musicId);
        byte defaultMapId = dis.readByte();
        stage.setDefaultMapId(defaultMapId);
        byte defaultX = dis.readByte();
        stage.setDefaultX(defaultX);
        byte defaultY = dis.readByte();
        stage.setDefaultY(defaultY);
        segSize = dis.readShort(); // data size;
        short fileCount = dis.readShort();
        String[] fileNames = new String[fileCount];
        for (int i = 0; i < fileCount; i++) {
            String fileName = dis.readUTF();
            fileNames[i] = fileName;
        }
        for (int i = 0; i < fileCount; i++) {
            InPkgFile inPkgFile = new InPkgFile();
            int fileSize = dis.readShort();
            inPkgFile.setName(fileNames[i]);
            byte[] fileData = new byte[fileSize];
            dis.readFully(fileData);
            inPkgFile.setData(fileData);
            //log.info("load "+inPkgFile.getName());
            if(isSceneFile(inPkgFile)){
                Scene scene = loadScene(inPkgFile,stageId);
                stage.addScene(scene);
            }
            else if(isClassFile(inPkgFile)){
                classFile classFile = new classFile(inPkgFile.getData());
                stage.setClassFile(classFile);
            }
            else if(isMonsterFile(inPkgFile)){
                Monster[] monsters = loadMonsters(inPkgFile);
                for(int j=0;j<monsters.length;j++){
                    stage.addMonster(monsters[j]);
                }

            }
            else{
                stage.addInPkgFile(inPkgFile);
            }

        }
        bis.close();
        return stage;
    }



    private static boolean isMonsterFile(InPkgFile file){
        return file.getName().equals("m.d");
    }

    private static boolean isSceneFile(InPkgFile file){
        return file.getName().endsWith(".m");
    }

    private static boolean isClassFile(InPkgFile file){
        return file.getName().endsWith(".class");
    }

    private static Monster[] loadMonsters(InPkgFile file) throws IOException{
        ByteArrayInputStream bis = new ByteArrayInputStream(file.getData());
        DataInputStream dis = new DataInputStream(bis);
        int size = dis.readByte();
        Monster[] ret = new Monster[size];
        for(int i=0;i<size;i++){
            Monster monster = new Monster();
            short pngId = dis.readShort();
            monster.setPngId(pngId);
            String name = dis.readUTF();
            monster.setName(name);
            byte type = dis.readByte();
            monster.setType(type);
            short level = dis.readShort();
            monster.setLevel(level);
            short vit = dis.readShort();
            monster.setVit(vit);
            short str = dis.readShort();
            monster.setStr(str);
            short Int = dis.readShort();
            monster.setInt(Int);
            short agi = dis.readShort();
            monster.setAgi(agi);
            short pMinAttack = dis.readShort();
            monster.setPMinAttack(pMinAttack);
            short pMaxAttack = dis.readShort();
            monster.setPMaxAttack(pMaxAttack);
            short pDef = dis.readShort();
            monster.setPDef(pDef);
            short mMinAttack = dis.readShort();
            monster.setMMinAttack(mMinAttack);
            short mMaxAttack = dis.readShort();
            monster.setMMaxAttack(mMaxAttack);
            short mDef = dis.readShort();
            monster.setMDef(mDef);
            short parry = dis.readShort();
            monster.setParry(parry);
            short hit = dis.readShort();
            monster.setHit(hit);
            short pCritial = dis.readShort();
            monster.setPCritial(pCritial);
            short mCritial = dis.readShort();
            monster.setMCritial(mCritial);
            int hp = dis.readInt();
            monster.setHp(hp);
            monster.setMaxHp(hp);
            int mp = dis.readInt();
            monster.setMp(mp);
            monster.setMaxMp(mp);
            short exp = dis.readShort();
            monster.setExp(exp);
            byte petType = dis.readByte();
            monster.setPetType(petType);
            int babyRate = dis.readInt();
            monster.setBabyRate(babyRate);
            String aiClass = dis.readUTF();
            monster.setAiClass(aiClass);
            int n = dis.readByte();
            for(int j=0;j<n;j++){
                byte iType = dis.readByte();
                int iId = dis.readInt();
                if(iType==0){
                    byte nCount = dis.readByte();
                    monster.addItem(iId,nCount);
                }
                else if(iType==6){
                    monster.addAbility(iId);
                }
            }
            n = dis.readByte();
            for(int j=0;j<n;j++){
                FallItem fallItem = new FallItem();
                byte iType = dis.readByte();
                byte ver = 0;
                if(iType < 0){
                	ver = dis.readByte();
                	iType = dis.readByte();
                }
                fallItem.setType(iType);
                if(iType!=3&&iType!=4&&iType!=8){ //宠物，装备只有1个
                    int min = dis.readInt();
                    fallItem.setMin(min);
                    int max = dis.readInt();
                    fallItem.setMax(max);
                }
                if(iType!=5){
                    int iId = dis.readInt();
                    fallItem.setId(iId);
                }
                int chance = dis.readInt();
                fallItem.setChance(chance);
                
                if(ver > 0){
                	int dropType = dis.readInt();
                	fallItem.setDropType(dropType);
                }
                
                monster.addFallItem(fallItem);
            }
            ret[i] = monster;
        }
        return ret;
    }

    private static Scene loadScene(InPkgFile file,short stageId) throws IOException{
        ByteArrayInputStream bis = new ByteArrayInputStream(file.getData());
        DataInputStream dis = new DataInputStream(bis);
        byte id = getSceneId(file.getName());
        Scene scene = new Scene();
        scene.setId(id);
        short mapId = (short)((stageId<<4)|id);
        scene.setMapId(mapId);
        byte type = dis.readByte();
        scene.setType(type);
        byte width = dis.readByte();
        scene.setWidth(width);
        byte height = dis.readByte();
        scene.setHeight(height);
        String name = dis.readUTF();
        scene.setName(name);
        short len = dis.readShort();
        byte[] mapDesc = new byte[len];
        dis.readFully(mapDesc);
        scene.setMapDesc(mapDesc);
        loadMapNpcs(scene,dis);
        loadMonsterGroups(scene,dis);
        loadNpcs(scene,dis);
        loadResource(scene,dis);
        loadDoors(scene,dis);
        if(type==0){
            byte l = dis.readByte();
            byte[] data = new byte[l];
            dis.readFully(data);
            scene.setAddition(data);
        }
        byte pkType = dis.readByte();
        if(pkType >= 0){
        	scene.setPkType(pkType);
        }else{
        	//新增加了版本号
        	byte ver = dis.readByte();
        	pkType = dis.readByte();
        	scene.setPkType(pkType);
        	//是否可以挂盾
        	byte safeType = dis.readByte();
        	scene.setSafeType(safeType);
        }
        return scene;
    }

    private static void loadDoors(Scene scene,DataInputStream dis) throws IOException{
        short len = dis.readByte();
        for(int i=0;i<len;i++){
            Door door = new Door();
            byte x = dis.readByte();
            door.setX(x);
            byte y = dis.readByte();
            door.setY(y);
            short destId = dis.readShort();
            door.setDestMapId(destId);
            byte destX = dis.readByte();
            door.setDestX(destX);
            byte destY = dis.readByte();
            door.setDestY(destY);
            String name = dis.readUTF();
            door.setName(name);
            scene.addDoor(door);
        }
    }

    private static void loadResource(Scene scene,DataInputStream dis) throws IOException{
        short len = dis.readShort();
        for (int i = 0; i < len; i++) {
            Resource resource = new Resource();
            int id = dis.readInt();
            resource.setId(id);
            byte x = dis.readByte();
            resource.setTileX(x);
            byte y = dis.readByte();
            resource.setTileY(y);
            if(scene.getType()==0){ //模糊地图16*16
                resource.setX((short)(16*x));
                resource.setY((short)(16*y));
            }else{  //精确地图16*8
                resource.setX((short)(16*x));
                resource.setY((short)(8*y));
            }
            byte level = dis.readByte();
            resource.setLevel(level);
            byte b = dis.readByte();
            byte type = (byte)(b&0xF);
            resource.setType(type);
            resource.setPlaygame((b&0x10)!=0);
            byte itemId = dis.readByte();
            resource.setItemId(itemId);
            short refreshSecond = dis.readShort();
            resource.setRefreshSecond(refreshSecond);
            scene.addResource(resource);
        }
    }

    private static void loadNpcs(Scene scene,DataInputStream dis) throws IOException{
    	BbsInfo bbsInfo;
    	short len = dis.readShort();
        for(int i=0;i<len;i++){
            Npc npc = new Npc();
            int id = dis.readInt();
            npc.setId(id);
            short pngId = dis.readShort();
            npc.setPngId(pngId);
            String name = dis.readUTF();
            npc.setName(name);
            byte x = dis.readByte();
            npc.setX(x);
            byte y = dis.readByte();
            npc.setY(y);
            short refreshSecond = dis.readShort();
            npc.setRefreshSecond(refreshSecond);
            byte type = dis.readByte();
            npc.setType(type);
            int taskNpcType = dis.readInt();
            if(taskNpcType>0){
                TaskNpc taskNpc = new TaskNpc();
                taskNpc.setId(id);
                taskNpc.setType(taskNpcType);
                TaskNpcs.addTaskNpc(taskNpc);
            }
            byte flag = dis.readByte();
            npc.setFlag(flag);
            
            //leo add for whole version editor data
            if((flag & 0x40) != 0){
                dis.readUTF();
            }
            //leo add end
            
            scene.addNpc(npc);
            //mengjie add
            if (type == 1){//1:bbs
            	bbsInfo = new BbsInfo(BbsInfo.getBbscount(),name,id,scene.getMapId(),scene.getName());
            	BbsInfo.addBbsInfo(bbsInfo);
            	BbsInfo.setBbscount(BbsInfo.getBbscount()+1);
            }
            //mengjie add end
        }
    }

    private static void loadMapNpcs(Scene scene,DataInputStream dis) throws IOException{
        short len = dis.readShort();
        for(int i=0;i<len;i++){
            MapNpc mapNpc = new MapNpc();
            byte x = dis.readByte();
            mapNpc.setX(x);
            byte y = dis.readByte();
            mapNpc.setY(y);
            byte id = dis.readByte();
            mapNpc.setId(id);
            scene.addMapNpc(mapNpc);
        }
    }

    private static void loadMonsterGroups(Scene scene, DataInputStream dis) throws
            IOException {
        short len = dis.readShort();
        for (int i = 0; i < len; i++) {
            MonsterGroup mg = new MonsterGroup();
            int id = dis.readInt();
            mg.setId(id);
            short iconId = dis.readShort();
            mg.setIconId(iconId);
            byte type = dis.readByte();
            mg.setType(type);
            mg.setVisible((type&128)!=0);
            short refreshSecond = dis.readShort();
            mg.setRefreshSecond(
                    refreshSecond);
            byte x = dis.readByte();
            mg.setTileX(x);
            byte y = dis.readByte();
            mg.setTileY(y);
            if(scene.getType()==0){ //模糊地图16*16
                mg.setX((short)(16*x));
                mg.setY((short)(16*y));
            }else{  //精确地图16*8
                mg.setX((short)(16*x));
                mg.setY((short)(8*y));
            }
            byte side = dis.readByte();
            mg.setSide(side);
            byte eyeshot = dis.readByte();
            mg.setEyeshot(eyeshot);
            int range = dis.readInt();
            mg.setRange(range);
            byte len0 = dis.readByte();
            for(int j=0;j<len0;j++){
                RoadPoint roadPoint = new RoadPoint();
                roadPoint.x = dis.readByte();
                roadPoint.y = dis.readByte();
                mg.addRoadPoint(roadPoint);
            }
            byte len1 = dis.readByte();
            for (int j = 0; j < len1; j++) {
                byte mId = dis.readByte();
                short mProbability = dis.readShort();
                mg.addMonster(mId, mProbability);
            }
            scene.addMonsterGroup(mg);
        }
    }

    private static byte getSceneId(String fileName){
        String baseName = FilenameUtils.getBaseName(fileName);
        String idString = baseName.substring(4);
        return Byte.parseByte(idString);
    }
}
