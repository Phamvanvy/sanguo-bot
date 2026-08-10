package com.pip.itimes.server.stage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;

import com.pip.gtl.etf.ETFFile;
import com.pip.gtl.etf.ETFUtil;
import org.apache.commons.collections.primitives.ArrayShortList;
import org.apache.commons.collections.primitives.ShortList;
import com.pip.itimes.server.util.IntHashSet;

/**
 * @author Jeffery
 * @version 1.0
 */
public class StageBuilder {
    private static final byte[] HEAD = {'R','P','G'};

//    private NpcPool npcPool;
//    private MonsterGroupPool mgPool;
//    private ResourcePool resourcePool;
    private TaskService taskService;

    public StageBuilder() {
    }

//    public void setNpcPool(NpcPool npcPool){
//        this.npcPool = npcPool;
//    }
//
//    public void setMonsterGroupPool(MonsterGroupPool mgPool){
//        this.mgPool = mgPool;
//    }
//
//    public void setResourcePool(ResourcePool resourcePool){
//        this.resourcePool = resourcePool;
//    }

    public void setTaskService(TaskService taskService){
        this.taskService = taskService;
    }
    
    
    /**
     * @param stage
     * @param parameters
     * @return 关卡转换后的脚本字节
     * @throws Exception
     */
    public byte[] toInnerBytes(Stage stage,Map parameters) throws Exception{
        classFile classFile = stage.getClassFile();
        short[] taskIds = classFile.getDefaultTasks(parameters);
        List etfFiles = new ArrayList();
        short[] cTaskIds = (short[])parameters.get("CURRENTTASKS");
        ShortList tasks = new ArrayShortList();
        Integer iLevel = (Integer)parameters.get("LEVEL");
        int level = iLevel.intValue();
        
        for (int i = 0; i < taskIds.length; i++) {
            ETFFile etfFile = taskService.findETF(taskIds[i],level);
            if (etfFile != null) {
                etfFile = ETFUtil.clone(etfFile);
                //针对关卡进行裁减
                if (ETFUtil.tailor(etfFile, stage.getId())) {
                	ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ETFUtil.save(etfFile, bos);
                    InPkgFile file = new InPkgFile();
                    file.setName(taskIds[i]+".etf");
                    file.setData(bos.toByteArray());
                    etfFiles.add(file);
                    tasks.add(taskIds[i]);
                }
            }
        }
        for(int i=0;i<cTaskIds.length;i++){
            ETFFile etfFile = taskService.findETF(cTaskIds[i],level);
            if (etfFile != null) {
                etfFile = ETFUtil.clone(etfFile);
                //针对关卡进行裁减
                if (ETFUtil.tailor(etfFile, stage.getId())) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ETFUtil.save(etfFile, bos);
                    InPkgFile file = new InPkgFile();
                    file.setName(cTaskIds[i]+".etf");
                    file.setData(bos.toByteArray());
                    etfFiles.add(file);
                    tasks.add(cTaskIds[i]);
                }
            }
        }
        parameters.put("TASKS",tasks.toArray());
        byte[] etf = getSeg3(stage,etfFiles);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.write(etf);
        return bos.toByteArray();
        /*byte[] dataSeg = bos.toByteArray();
        bos = new ByteArrayOutputStream();
        dos = new DataOutputStream(bos);
        //dos.write(HEAD);
        //dos.writeByte(stage.getRetCode());
        //dos.writeInt(dataSeg.length+12); //包括头
        //dos.writeShort(stage.getVersion());
        //dos.writeShort(0);
        dos.write(dataSeg);
        return bos.toByteArray();*/
        
    }
    
/*    public byte[] toBytes(Stage stage,Map parameters) throws Exception{
        classFile classFile = stage.getClassFile();
        short[] taskIds = classFile.getDefaultTasks(parameters);
        List etfFiles = new ArrayList();
        short[] cTaskIds = (short[])parameters.get("CURRENTTASKS");
        ShortList tasks = new ArrayShortList();
//        byte[] tasksave = (byte[])parameters.get("TASKSAVE");
//        TaskSaveDataBean taskSaveBean = new TaskSaveDataBean();
//        taskSaveBean.updateData(tasksave);
//        TaskSaveDataBean cutSaveBean = new TaskSaveDataBean();
//        Iterator itor = taskSaveBean.taskDataMap.entrySet().iterator();
//        while (itor.hasNext()) {
//            Map.Entry entry = (Map.Entry)itor.next();
//            short key = ((Short)entry.getKey()).shortValue();
//            cutSaveBean.addTaskSave(key, (byte[])entry.getValue());
//        }
//
//
//        params.put("tasksavecut", cutSaveBean.getData());
        Integer iLevel = (Integer)parameters.get("LEVEL");
        int level = iLevel.intValue();
        for (int i = 0; i < taskIds.length; i++) {
            ETFFile etfFile = taskService.findETF(taskIds[i],level);
            if (etfFile != null) {
                etfFile = ETFUtil.clone(etfFile);
                //针对关卡进行裁减
                if (ETFUtil.tailor(etfFile, stage.getId())) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ETFUtil.save(etfFile, bos);
                    InPkgFile file = new InPkgFile();
                    file.setName(taskIds[i]+".etf");
                    file.setData(bos.toByteArray());
                    etfFiles.add(file);
                    tasks.add(taskIds[i]);
                }
            }
        }
        for(int i=0;i<cTaskIds.length;i++){
            ETFFile etfFile = taskService.findETF(cTaskIds[i],level);
            if (etfFile != null) {
                etfFile = ETFUtil.clone(etfFile);
                //针对关卡进行裁减
                if (ETFUtil.tailor(etfFile, stage.getId())) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ETFUtil.save(etfFile, bos);
                    InPkgFile file = new InPkgFile();
                    file.setName(cTaskIds[i]+".etf");
                    file.setData(bos.toByteArray());
                    etfFiles.add(file);
                    tasks.add(cTaskIds[i]);
                }
            }
        }
        parameters.put("TASKS",tasks.toArray());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte[] seg1 = getSeg1(stage);
        byte[] seg2 = getSeg2(stage,etfFiles);
        dos.writeShort(seg1.length);
        dos.write(seg1);
        dos.writeShort(seg2.length);
        dos.write(seg2);
//        List dynNpcs = npcPool.getVisibleNpcs(stage.getId());
//        List dynMgs = mgPool.getVisibleMonsterGroup(stage.getId());
//        List dynResources = resourcePool.getVisibleResource(stage.getId());
        Scene[] scene = stage.getScenes();
        IntHashSet parts = (IntHashSet)parameters.get("parts");
        String playerName = (String)parameters.get("playername");
        int npcId =  -1;
        int imageId = -1;
        Integer i1 = (Integer)parameters.get("waiternpcid");
        Integer i2 = (Integer)parameters.get("waiterimageid");
        if(i1!=null){
            npcId = i1.intValue();
            imageId = i2.intValue();
        }
        for(int i=0;i<scene.length;i++){
            byte[] bytes = getSceneData(scene[i],parts,playerName,npcId,imageId);
            dos.writeShort(bytes.length);
            dos.write(bytes);
        }
        InPkgFile[] inPkgFiles = stage.getInPkgFiles();
        for(int i=0;i<inPkgFiles.length;i++){
            byte[] bytes = inPkgFiles[i].getData();
            dos.writeShort(bytes.length);
            dos.write(bytes);
        }
        for(int i=0;i<etfFiles.size();i++){
            InPkgFile file = (InPkgFile)etfFiles.get(i);
            byte[] bytes = file.getData();
            dos.writeShort(bytes.length);
            dos.write(bytes);
        }
        byte[] dataSeg = bos.toByteArray();
        bos = new ByteArrayOutputStream();
        dos = new DataOutputStream(bos);
        dos.write(HEAD);
        dos.writeByte(stage.getRetCode());
        dos.writeInt(dataSeg.length+12); //包括头
        dos.writeShort(stage.getVersion());
        dos.writeShort(0);
        dos.write(dataSeg);
        return bos.toByteArray();
    }*/
    
    
    /**
     * @param stage
     * @param parameters
     * @return 根据客户端返回协议
     * @throws Exception
     */
    public byte[] toBytesClientDataVersion(Stage stage,Map parameters, int dataVersion, short crc) throws Exception{
        classFile classFile = stage.getClassFile();
        short[] taskIds = classFile.getDefaultTasks(parameters);
        List etfFiles = new ArrayList();
        short[] cTaskIds = (short[])parameters.get("CURRENTTASKS");
        ShortList tasks = new ArrayShortList();
//        byte[] tasksave = (byte[])parameters.get("TASKSAVE");
//        TaskSaveDataBean taskSaveBean = new TaskSaveDataBean();
//        taskSaveBean.updateData(tasksave);
//        TaskSaveDataBean cutSaveBean = new TaskSaveDataBean();
//        Iterator itor = taskSaveBean.taskDataMap.entrySet().iterator();
//        while (itor.hasNext()) {
//            Map.Entry entry = (Map.Entry)itor.next();
//            short key = ((Short)entry.getKey()).shortValue();
//            cutSaveBean.addTaskSave(key, (byte[])entry.getValue());
//        }
//
//
//        params.put("tasksavecut", cutSaveBean.getData());
        Integer iLevel = (Integer)parameters.get("LEVEL");
        int level = iLevel.intValue();
        for (int i = 0; i < taskIds.length; i++) {
            ETFFile etfFile = taskService.findETF(taskIds[i],level);
            if (etfFile != null) {
                etfFile = ETFUtil.clone(etfFile);
                //针对关卡进行裁减
                if (ETFUtil.tailor(etfFile, stage.getId())) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ETFUtil.save(etfFile, bos);
                    InPkgFile file = new InPkgFile();
                    file.setName(taskIds[i]+".etf");
                    file.setData(bos.toByteArray());
                    etfFiles.add(file);
                    tasks.add(taskIds[i]);
                }
            }
        }
        for(int i=0;i<cTaskIds.length;i++){
            ETFFile etfFile = taskService.findETF(cTaskIds[i],level);
            if (etfFile != null) {
                etfFile = ETFUtil.clone(etfFile);
                //针对关卡进行裁减
                if (ETFUtil.tailor(etfFile, stage.getId())) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ETFUtil.save(etfFile, bos);
                    InPkgFile file = new InPkgFile();
                    file.setName(cTaskIds[i]+".etf");
                    file.setData(bos.toByteArray());
                    etfFiles.add(file);
                    tasks.add(cTaskIds[i]);
                }
            }
        }
        parameters.put("TASKS",tasks.toArray());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        byte[] seg1 = getSeg1(stage);
        byte[] seg2 = getSeg2(stage,etfFiles);
        dos.writeShort(seg1.length);
        dos.write(seg1);
        dos.writeShort(seg2.length);
        dos.write(seg2);
//        List dynNpcs = npcPool.getVisibleNpcs(stage.getId());
//        List dynMgs = mgPool.getVisibleMonsterGroup(stage.getId());
//        List dynResources = resourcePool.getVisibleResource(stage.getId());
        Scene[] scene = stage.getScenes();
        IntHashSet parts = (IntHashSet)parameters.get("parts");
        String playerName = (String)parameters.get("playername");
        int npcId =  -1;
        int imageId = -1;
        Integer i1 = (Integer)parameters.get("waiternpcid");
        Integer i2 = (Integer)parameters.get("waiterimageid");
        if(i1!=null){
            npcId = i1.intValue();
            imageId = i2.intValue();
        }
        //最主要的区别就是在这里
        for(int i=0;i<scene.length;i++){
            byte[] bytes = getSceneDataClientDataVersion(scene[i],parts,playerName,npcId,imageId, dataVersion);
            dos.writeShort(bytes.length);
            dos.write(bytes);
        }
        InPkgFile[] inPkgFiles = stage.getInPkgFiles();
        for(int i=0;i<inPkgFiles.length;i++){
            byte[] bytes = inPkgFiles[i].getData();
            dos.writeShort(bytes.length);
            dos.write(bytes);
        }
        for(int i=0;i<etfFiles.size();i++){
            InPkgFile file = (InPkgFile)etfFiles.get(i);
            byte[] bytes = file.getData();
            dos.writeShort(bytes.length);
            dos.write(bytes);
        }
        byte[] dataSeg = bos.toByteArray();
        bos = new ByteArrayOutputStream();
        dos = new DataOutputStream(bos);
        dos.write(HEAD);
        dos.writeByte(stage.getRetCode());
        dos.writeInt(dataSeg.length+12); //包括头
        dos.writeShort(stage.getVersion());
        dos.writeShort(0);
        dos.write(dataSeg);
        return bos.toByteArray();
    }

    private byte[] getSeg1(Stage stage) throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(stage.getId());
        dos.writeUTF(stage.getName());
        dos.writeByte(stage.getMusicId());
        dos.writeByte(stage.getDefaultMapId());
        dos.writeByte(stage.getDefaultX());
        dos.writeByte(stage.getDefaultY());
        return bos.toByteArray();
    }

    public byte[] getSeg2(Stage stage,List etfFiles) throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        Scene[] scenes = stage.getScenes();
        InPkgFile[] inPkgFiles = stage.getInPkgFiles();
        dos.writeShort(scenes.length+inPkgFiles.length+etfFiles.size());
        for(int i=0;i<scenes.length;i++){
            dos.writeUTF("map_"+scenes[i].getId()+".m");
        }
        for(int i=0;i<inPkgFiles.length;i++){
            dos.writeUTF(inPkgFiles[i].getName());
//            System.out.println(inPkgFiles[i].getName());
        }
        for(int i=0;i<etfFiles.size();i++){
            InPkgFile file = (InPkgFile)etfFiles.get(i);
            dos.writeUTF(file.getName());
        }
        return bos.toByteArray();
    }
    
    
    /**
     * @param stage
     * @param etfFiles
     * @return 只包含脚本的数据 4.4版本后使用
     * @throws Exception
     */
    public byte[] getSeg3(Stage stage,List etfFiles) throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeShort(etfFiles.size());
        for(int i=0;i<etfFiles.size();i++){
            InPkgFile file = (InPkgFile)etfFiles.get(i);
            dos.writeUTF(file.getName());
            dos.writeShort(file.getData().length);
            dos.write(file.getData());
        }
        return bos.toByteArray();
    }
    
    

/*    public byte[] getSceneData(Scene scene,IntHashSet parts,String playerName,int npcId,int imageId) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(scene.getType());
        dos.writeByte(scene.getWidth());
        dos.writeByte(scene.getHeight());
        if(playerName!=null){
            dos.writeUTF(playerName+"的"+scene.getName());
        }else{
            dos.writeUTF(scene.getName());
        }
        dos.writeShort(scene.getMapDesc().length);
        dos.write(scene.getMapDesc());
        MapNpc[] mapNpcs = scene.getMapNpcs();
        if(parts==null){
            dos.writeShort(mapNpcs.length);
            for (int i = 0; i < mapNpcs.length; i++) {
                dos.writeByte(mapNpcs[i].getX());
                dos.writeByte(mapNpcs[i].getY());
                dos.writeByte(mapNpcs[i].getId());
            }
        }else{
            List<MapNpc> l = new ArrayList<MapNpc>(mapNpcs.length);
            for(int i=0;i<mapNpcs.length;i++){
                if(parts.contains(mapNpcs[i].getId())){
                    l.add(mapNpcs[i]);
                }
            }
            dos.writeShort(l.size());
            for(int i=0;i<l.size();i++){
                MapNpc mn = l.get(i);
                dos.writeByte(mn.getX());
               dos.writeByte(mn.getY());
               dos.writeByte(mn.getId());
            }
        }
        byte[] bytes = getMonsterGroups(scene);
        dos.write(bytes);
        bytes = getNpcs(scene,npcId,imageId,scene.getMapId());
        dos.write(bytes);
        bytes = getResources(scene);
        dos.write(bytes);
        Door[] doors = scene.getDoors();
        dos.writeByte(doors.length);
        for(int i=0;i<doors.length;i++){
            dos.writeByte(doors[i].getX());
            dos.writeByte(doors[i].getY());
            dos.writeShort(doors[i].getDestMapId());
            dos.writeByte(doors[i].getDestX());
            dos.writeByte(doors[i].getDestY());
            dos.writeUTF(doors[i].getName());
        }
        byte[] addition = scene.getAddition();
        if(addition!=null){
            dos.writeByte(addition.length);
            dos.write(addition);
        }
        dos.writeByte(scene.getPkType());
        return bos.toByteArray();
    }*/
    
    
    public byte[] getSceneDataClientDataVersion(Scene scene,IntHashSet parts,String playerName,int npcId,int imageId, int dataVersion) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(scene.getType());
        dos.writeByte(scene.getWidth());
        dos.writeByte(scene.getHeight());
        if(playerName!=null){
            dos.writeUTF(playerName+"的"+scene.getName());
        }else{
            dos.writeUTF(scene.getName());
        }
        dos.writeShort(scene.getMapDesc().length);
        dos.write(scene.getMapDesc());
        MapNpc[] mapNpcs = scene.getMapNpcs();
        if(parts==null){
            dos.writeShort(mapNpcs.length);
            for (int i = 0; i < mapNpcs.length; i++) {
                dos.writeByte(mapNpcs[i].getX());
                dos.writeByte(mapNpcs[i].getY());
                dos.writeByte(mapNpcs[i].getId());
            }
        }else{
            List<MapNpc> l = new ArrayList<MapNpc>(mapNpcs.length);
            for(int i=0;i<mapNpcs.length;i++){
                if(parts.contains(mapNpcs[i].getId())){
                    l.add(mapNpcs[i]);
                }
            }
            dos.writeShort(l.size());
            for(int i=0;i<l.size();i++){
                MapNpc mn = l.get(i);
                dos.writeByte(mn.getX());
               dos.writeByte(mn.getY());
               dos.writeByte(mn.getId());
            }
        }
        //byte[] bytes = getMonsterGroups(scene);
        byte[] bytes = getMonsterGroupsClientDataVesrion(scene, dataVersion);

        dos.write(bytes);
        bytes = getNpcs(scene,npcId,imageId,scene.getMapId());
        dos.write(bytes);
        bytes = getResources(scene);
        dos.write(bytes);
        Door[] doors = scene.getDoors();
        dos.writeByte(doors.length);
        for(int i=0;i<doors.length;i++){
            dos.writeByte(doors[i].getX());
            dos.writeByte(doors[i].getY());
            dos.writeShort(doors[i].getDestMapId());
            dos.writeByte(doors[i].getDestX());
            dos.writeByte(doors[i].getDestY());
            dos.writeUTF(doors[i].getName());
        }
        byte[] addition = scene.getAddition();
        if(addition!=null){
            dos.writeByte(addition.length);
            dos.write(addition);
        }
        dos.writeByte(scene.getPkType());
        return bos.toByteArray();
    }
   /* public byte[] getMonsterGroups(Scene scene) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        MonsterGroup[] fixMgs = scene.getFixMonsterGroups();
        MonsterGroup[] dynMgs = scene.getDynMonsterGroups();
        dos.writeShort(fixMgs.length+dynMgs.length);
        for(int i=0;i<fixMgs.length;i++){
            MonsterGroup mg = (MonsterGroup)fixMgs[i];
            dos.writeInt(mg.getId());
            dos.writeShort(mg.getIconId());
            dos.writeByte(mg.getType());
            dos.writeShort(mg.getRefreshSecond());
            dos.writeByte(mg.getTileX());
            dos.writeByte(mg.getTileY());
            dos.writeByte(mg.getSide());
            dos.writeByte(mg.getEyeshot());
            dos.writeInt(mg.getRange());
            RoadPoint[] roadPoints = mg.getRoadPoints();
            dos.writeByte(roadPoints.length);
            for(int j=0;j<roadPoints.length;j++){
                dos.writeByte(roadPoints[j].x);
                dos.writeByte(roadPoints[j].y);
            }
            byte[] mIds = mg.getMonstersId();
            short[] probabilities = mg.getProbabilities();
            dos.writeByte(mIds.length);
            for (int j = 0; j < mIds.length; j++) {
                dos.writeByte(mIds[j]);
                dos.writeShort(probabilities[j]);
            }
        }
        for(int i=0;i<dynMgs.length;i++){
            MonsterGroup mg = (MonsterGroup)dynMgs[i];
            dos.writeInt(mg.getId());
            dos.writeShort(mg.getIconId());
            dos.writeByte(mg.getType());
//            byte type = mg.getType();
//            if(mgPool.isVisible(mg.getId())){
//                type |= 128;
//            }else{
//                type &= 127;
//            }
//            dos.writeByte(type);
            dos.writeShort(mg.getRefreshSecond());
            dos.writeByte(mg.getTileX());
            dos.writeByte(mg.getTileY());
            dos.writeByte(mg.getSide());
            dos.writeByte(mg.getEyeshot());
            dos.writeInt(mg.getRange());
            RoadPoint[] roadPoints = mg.getRoadPoints();
            dos.writeByte(roadPoints.length);
            for(int j=0;j<roadPoints.length;j++){
                dos.writeByte(roadPoints[j].x);
                dos.writeByte(roadPoints[j].y);
            }
            byte[] mIds = mg.getMonstersId();
            short[] probabilities = mg.getProbabilities();
            dos.writeByte(mIds.length);
            for (int j = 0; j < mIds.length; j++) {
                dos.writeByte(mIds[j]);
                dos.writeShort(probabilities[j]);
            }
        }
        return bos.toByteArray();
    }*/
    
    public byte[] getMonsterGroupsClientDataVesrion(Scene scene, int dataVersion) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        MonsterGroup[] fixMgs = scene.getFixMonsterGroups();
        MonsterGroup[] dynMgs = scene.getDynMonsterGroups();
        dos.writeShort(fixMgs.length+dynMgs.length);
        for(int i=0;i<fixMgs.length;i++){
            MonsterGroup mg = (MonsterGroup)fixMgs[i];
            dos.writeInt(mg.getId());
            dos.writeShort(mg.getIconId());
            if(dataVersion == 0){//老版本
            	dos.writeByte(0x01);
            }else{//新版本保持不变
            	dos.writeByte(mg.getType());
            }
            dos.writeShort(mg.getRefreshSecond());
            dos.writeByte(mg.getTileX());
            dos.writeByte(mg.getTileY());
            dos.writeByte(mg.getSide());
            dos.writeByte(mg.getEyeshot());
            dos.writeInt(mg.getRange());
            RoadPoint[] roadPoints = mg.getRoadPoints();
            dos.writeByte(roadPoints.length);
            for(int j=0;j<roadPoints.length;j++){
                dos.writeByte(roadPoints[j].x);
                dos.writeByte(roadPoints[j].y);
            }
            byte[] mIds = mg.getMonstersId();
            short[] probabilities = mg.getProbabilities();
            dos.writeByte(mIds.length);
            for (int j = 0; j < mIds.length; j++) {
                dos.writeByte(mIds[j]);
                dos.writeShort(probabilities[j]);
            }
        }
        for(int i=0;i<dynMgs.length;i++){
            MonsterGroup mg = (MonsterGroup)dynMgs[i];
            dos.writeInt(mg.getId());
            dos.writeShort(mg.getIconId());
            dos.writeByte(mg.getType());
//            byte type = mg.getType();
//            if(mgPool.isVisible(mg.getId())){
//                type |= 128;
//            }else{
//                type &= 127;
//            }
//            dos.writeByte(type);
            dos.writeShort(mg.getRefreshSecond());
            dos.writeByte(mg.getTileX());
            dos.writeByte(mg.getTileY());
            dos.writeByte(mg.getSide());
            dos.writeByte(mg.getEyeshot());
            dos.writeInt(mg.getRange());
            RoadPoint[] roadPoints = mg.getRoadPoints();
            dos.writeByte(roadPoints.length);
            for(int j=0;j<roadPoints.length;j++){
                dos.writeByte(roadPoints[j].x);
                dos.writeByte(roadPoints[j].y);
            }
            byte[] mIds = mg.getMonstersId();
            short[] probabilities = mg.getProbabilities();
            dos.writeByte(mIds.length);
            for (int j = 0; j < mIds.length; j++) {
                dos.writeByte(mIds[j]);
                dos.writeShort(probabilities[j]);
            }
        }
        return bos.toByteArray();
    }


    public byte[] getNpcs(Scene scene,int npcId,int imageId,int mapid) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        Npc[] fixNpcs = scene.getFixNpcs();
        Npc[] dynNpcs = scene.getDynNpcs();
        dos.writeShort(fixNpcs.length+dynNpcs.length);
        for(int i=0;i<fixNpcs.length;i++){
            Npc npc = fixNpcs[i];
            dos.writeInt(npc.getId());

            if(npcId==npc.getId()){
                dos.writeShort((short)imageId);
            }else{
            	//mengjie add
            	if (mapid == Instanceadd.getInstanceaddbytype(1).getMapid()){
            		//场景是名人堂
            		try{
    	            	int countnpctmp = Integer.valueOf(npc.getName()).intValue();
    	            	if (countnpctmp<11){
    	            		if (Instanceadd.getNpcsexby(countnpctmp-1) == 0){
    	            			dos.writeShort((short)241);
    	            		}else{
    	            			dos.writeShort((short)182);
    	            		}
                			
    	            	}
                	} catch (Exception ex) {
                		dos.writeShort(npc.getPngId());
            		}
            	}else{
            		dos.writeShort(npc.getPngId());
            	}
            }
            //mengjie add
            if (mapid == Instanceadd.getInstanceaddbytype(1).getMapid()){
            	//场景是名人堂
            	try{
	            	int countnpctmp = Integer.valueOf(npc.getName()).intValue();
	            	if (countnpctmp<11){
            			dos.writeUTF(Instanceadd.getNpcnameby(countnpctmp-1));
	            	}
            	} catch (Exception ex) {
            		dos.writeUTF(npc.getName());
        		}
            }else{
            	dos.writeUTF(npc.getName());
            }
            dos.writeByte(npc.getX());
            dos.writeByte(npc.getY());
            dos.writeShort(npc.getRefreshSecond());
            dos.writeByte(npc.getType());
            dos.writeByte(npc.getFlag());
        }
        for(int i=0;i<dynNpcs.length;i++){
            Npc npc = dynNpcs[i];
            dos.writeInt(npc.getId());
            if(npcId==npc.getId()){
                dos.writeShort((short)imageId);
            }else{
            dos.writeShort(npc.getPngId());
            }
            dos.writeUTF(npc.getName());
            dos.writeByte(npc.getX());
            dos.writeByte(npc.getY());
            dos.writeShort(npc.getRefreshSecond());

            dos.writeByte(npc.getType());
            dos.writeByte(npc.getFlag());
//            byte flag = npc.getFlag();
//            if(npcPool.isVisible(npc.getId())){
//                flag |= 128;
//            }else{
//                flag &= 127;
//            }
//            dos.writeByte(flag);
        }
        return bos.toByteArray();
    }


    public byte[] getResources(Scene scene) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        Resource[] resources = scene.getResources();
        dos.writeShort(resources.length);
        for(int i=0;i<resources.length;i++){
            Resource resource = resources[i];
            dos.writeInt(resource.getId());
            dos.writeByte(resource.getTileX());
            dos.writeByte(resource.getTileY());
            dos.writeByte(resource.getLevel());
            byte b = resource.getType();
            if(resource.getPlaygame()){
                b |= 0x10;
            }
            dos.writeByte(b);
            dos.writeByte(resource.getItemId());
//            byte type = resource.getType();
//            if(resourcePool.isVisible(resource.getId())){
//                type |= 128;
//            }else{
//                type &= 127;
//            }
//            dos.writeByte(type);
            dos.writeShort(resource.getRefreshSecond());
        }
        return bos.toByteArray();
    }

}
