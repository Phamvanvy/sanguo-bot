package com.pip.itimes.server.connect;


import java.util.List;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.PlayerDao;
import com.pip.itimes.server.stage.AbilitiesLoader;

import java.util.Date;
import java.io.*;

import org.apache.commons.io.FilenameUtils;
import com.pip.itimes.server.stage.PlayerData;
import com.pip.itimes.server.bean.TaskData;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.util.KeywordsUtil;
import java.util.TreeMap;
import java.util.Map;
import java.util.Iterator;
import org.apache.log4j.Logger;


public class PlayerService implements Runnable{

    private static final Logger log = Logger.getLogger(PlayerService.class);

    private PlayerDao playerDao;
    private AuthSession authSession;
    private Map id2time = new TreeMap();

    public PlayerService(PlayerDao dao){
        this.playerDao = dao;
    }

    public void setAuthSession(AuthSession authSession){
        this.authSession = authSession;
    }


    public void start(){
        new Thread(this).start();
    }

    public void run(){
        try {
            Thread.sleep(100 * 1000L);
        } catch (InterruptedException ex) {
        }
        long currentTime = System.currentTimeMillis();
        Iterator ite = id2time.entrySet().iterator();
        while(ite.hasNext()){
            Map.Entry entry = (Map.Entry)ite.next();
            long time = ((Long)entry.getValue()).longValue();
            if(time<=currentTime)
                ite.remove();
        }
    }

    //time second
    public void forbid(int id, int time) {
        synchronized (id2time) {
            long t = System.currentTimeMillis() + time * 1000L;
            if (t == 0)
                id2time.remove(new Integer(id));
            else
                id2time.put(new Integer(id), new Long(t));
        }
    }

    public boolean removeforbid(int id){
        synchronized(id2time){
            Object o = id2time.remove(new Integer(id));
            return o != null;
        }
    }

    public boolean isForbid(int id){
        return id2time.containsKey(new Integer(id));
    }

    public Player getPlayerByName(String name){
        try{
            return playerDao.getPlayerByName(name);
        }catch(DataAccessException e){
            return null;
        }
    }

    public Player getPlayerByNameAndAccountId(String name, int accountId){
        try{
            return playerDao.getPlayerByNameAndAccountId(name, accountId);
        }catch(DataAccessException e){
            return null;
        }
    }

    public Player getPlayerById(int id){
        try{
            return playerDao.getPlayerById(id);
        }catch(DataAccessException e){
            return null;
        }
    }

    public int getPlayerId(String name,int accountId){
        try {
            return playerDao.getPlayerId(name, accountId);
        } catch (DataAccessException ex) {
            return -1;
        }
    }

    public void getPlayerList(ClientSession stub, UWAPData data) throws ITimesException{
        try{
            List l = playerDao.getPlayerList(stub.getAccountId());
            UWAPSegment seg = new UWAPSegment(ClientConstants.ACTOR_LIST, data.getSerial());
            seg.write((byte)l.size());
            for(int i = 0; i < l.size(); i++){
                Player player = (Player)l.get(i);
                seg.writeString(player.getPlayerName());
                seg.writeShort((short)player.getLevel());
                seg.write((byte)player.getFace());
                seg.write(player.getReturnTimes());
            }
            stub.write(seg);
        }catch(DataAccessException ex){
            throw new ITimesException("获取列表错误", data.getSerial(), data.getSessionId(),data.getAppType());
        }
    }

    public void createPlayer(ClientSession stub, UWAPData data) throws ITimesException{
        try{
            String playerName = data.readString();
            byte sex = data.readByte();
            int version = data.readInt();
            String name = playerName.trim();
            if(name.length()==0)
                throw new ITimesException("角色名不能为空",data.getSerial(),data.getSessionId(),data.getAppType());
            if(name.getBytes("GBK").length > 16)
                throw new ITimesException("角色名太长",data.getSerial(),data.getSessionId(),data.getAppType());
            if(KeywordsUtil.isInvalidName(name.toLowerCase()))
                throw new ITimesException("角色名出现非法字符",data.getSerial(),data.getSessionId(),data.getAppType());
            if(!Utils.checkString(name,false))
                throw new ITimesException("角色名出现非法字符",data.getSerial(),data.getSessionId(),data.getAppType());
            String newName = KeywordsUtil.filterKeywords(name);
            if(!newName.equals(name))
                throw new ITimesException("角色名出现非法字符",data.getSerial(),data.getSessionId(),data.getAppType());
            Player player = playerDao.getPlayerByName(name);
            if(player != null){
                throw new ITimesException("存在同名角色", data.getSerial(), data.getSessionId(),data.getAppType());
            }else{
                byte[] option = null;
                if("MotoV300".equals(stub.model)){
                    option = MOTO_OPTION;
                }else if("NK-40-2".equals(stub.model)){
                    option = N402_OPTION;
                }

                Player newPlayer = createDefaultPlayer(stub.getAccountId(), name, sex,0,option);
                playerDao.addPlayer(newPlayer);
                UWAPSegment seg = new UWAPSegment(ClientConstants.ACTOR_CREATE_OK, data.getSerial());
                seg.writeInt(newPlayer.getId());
                seg.writeString(newPlayer.getPlayerName());
                seg.write(newPlayer.getData());
                seg.writeInt(0);
                seg.writeInt(100);
                stub.write(seg);
            }
        }catch(ITimesException e){
            throw e;
        }catch(Exception e){
            log.error(e,e);
            throw new ITimesException("创建角色失败", data.getSerial(), data.getSessionId(),data.getAppType());
        }
    }

//    private String QUICKNAME_PREFIX = "guest";
    private static final byte[] CHANGE_SEX_ITEM_BYTES = new byte[]{0,1,0,3,0x34,0x64,1}; //变性药水
//    private static final byte[] DEFAULT_OPTION = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0};//默认的配置
    private static final byte[] MOTO_OPTION = new byte[]{0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//000000001000moto机器的配置
    private static final byte[] N402_OPTION = new byte[]{0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//000000001000moto机器的配置

    public Player quickCreatePlayer(int accountId,String name,byte sex,int serial,String model) throws ITimesException{
        try {
            byte[] option = null;
            if ("MotoV300".equals(model)) {
                option = MOTO_OPTION;
            }

            Player newPlayer = createDefaultPlayer(accountId, name, sex,1,option);
            newPlayer.setMetaItems(getQuickRegMetaItems());
            playerDao.addPlayer(newPlayer);
            return newPlayer;
        } catch (DataAccessException ex) {
            throw new ITimesException("创建角色失败",serial,ClientConstants.QUICK_REG);
        }
    }

    public void savePlayer(Player player){
        try{
            playerDao.makePersistent(player);
        }catch(DataAccessException ex){
            ex.printStackTrace();
        }
    }

    public Player createDefaultPlayer(int accountId, String playerName, byte sex,int modifyNameTimes,byte[] option){


        Player player = new Player();
        player.setAccountId(accountId);
        player.setPlayerName(playerName);
        player.setSex(sex);
        player.setFace(sex);
        //出生点
        player.setMapId((short)3697);
        player.setX((short)(10*16));
        player.setY((short)(13*8));
//        player.setMapId((short)1569);
//        player.setX((short)(12*16));
//        player.setY((short)(21*8));
        player.setExp(0);
        player.setLevel(1);
        player.setMoeny(0);
        player.setReturnTimes((byte)0);
        player.setCreateTime(new Date());
        player.setLastLoginTime(new Date());
        player.setHouseLevel(1);
        player.setTongDuty(-1);
        player.setTongId(-1);
        player.setTongName("");
        player.setTongTitle("");
        player.setCredit(0);
        player.setData(new byte[0]);
        player.setStrength(1);
        player.setAgility(1);
        player.setVitality(1);
        player.setIntelligence(1);
        player.setLuck(1);
        player.setAbilityTimes(1);
        player.setHp(Utils.calculateMaxHp(player.getVitality(),player.getAgility(),player.getStrength(),player.getIntelligence(),1));
        player.setMp(Utils.calculateMaxMp(player.getVitality(),player.getAgility(),player.getStrength(),player.getIntelligence(),1));
        player.setLeavePoints(0);
        player.setAbilities(new byte[]{
                        0, 0, 0, 0
        });
        player.setTechSkills(new byte[0]);
        player.setBasicItems(getBasicItems());
        player.setMetaItems(getMetaItems());
        player.setEquipments(getEquipments());
        player.setUsedEquipments(getUsedEquipments());
        player.setTaskItems(getTaskItems());
        if(option==null)
            player.setOptions(new byte[0]);
        else
            player.setOptions(option);
        player.setPets(new byte[0]);
        player.setModifyNameTimes(modifyNameTimes);
        TaskData taskData = new TaskData();
        taskData.setCurrent(new byte[0]);
        taskData.setFinished(new byte[0]);
        taskData.setSaveData(new byte[0]);
        player.setTaskData(taskData);
        player.setGridSize((short)12);
        player.setValid(true);
        player.setMessageCount(0);
        player.setLastMessageTime(new Date());
        player.setTitle("");
        player.setJumpMapId((short)0);
        player.setJumpX((short)0);
        player.setJumpY((short)0);
        taskData.setPlayer(player);
        return player;
    }

    public static void main(String[] args){
        PlayerService aaa = new PlayerService(new PlayerDao());
        Player player = aaa.createDefaultPlayer(1, "大宝", (byte)0,1,null);

        try{
            String abilitiesDirName = FilenameUtils.concat("D:/eclipse/workspace/iTimes1.0-Editor/data/", "Skill/index.xml");
            AbilitiesLoader abilitiesLoader = new AbilitiesLoader(new File(abilitiesDirName));
            PlayerData bbb = new PlayerData(player);
            bbb.toClientBytes();
        }catch(Exception e){
            // TODO 自动生成 catch 块
            e.printStackTrace();
        }
    }

    private byte[] getBasicItems(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
//        大生命药剂*1（ID:3）
        try {
            dos.writeShort(1);
            dos.writeInt(111);
            dos.write(1);
        } catch (IOException ex) {
        }
        return bos.toByteArray();
    }

    private byte[] getMetaItems(){
        //        爱之门*1（ID:200126）
//传送门*1（ID:200125）
//宠物经验翻倍果*1（ID:210021）
//幸运时魔球*1（ID:200127）、改成了210032（双倍经验果）
//增力药剂*1（ID:200130）
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeShort(5);
            dos.writeInt(200126);
            dos.write(1);
            dos.writeInt(200125);
            dos.write(1);
            dos.writeInt(210021);
            dos.write(1);
            dos.writeInt(210032);
            dos.write(1);
            dos.writeInt(200130);
            dos.write(1);
        } catch (IOException ex) {
        }
        return bos.toByteArray();
    }

    private byte[] getQuickRegMetaItems(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeShort(6);
            dos.writeInt(200126);
            dos.write(1);
            dos.writeInt(200125);
            dos.write(1);
            dos.writeInt(210021);
            dos.write(1);
            dos.writeInt(210032);
            dos.write(1);
            dos.writeInt(200130);
            dos.write(1);
            dos.writeInt(210020);
            dos.write(1);
        } catch (IOException ex) {
        }
        return bos.toByteArray();
    }

    private byte[] getEquipments(){
        return new byte[0];
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        DataOutputStream dos = new DataOutputStream(bos);
//        try{
//            dos.writeShort(1);
//            dos.writeInt(3); //item id
//            dos.writeInt(1); //id
//            dos.writeUTF("放在包里的大斧子"); //name
//            dos.writeByte(1); //级别
//            dos.writeByte(1); //需要级别
//            dos.writeByte(1); //装备级别
//            dos.writeByte(7); //装备部位
//            dos.writeShort(100); //耐久度
//            dos.writeShort(100); //剩余耐久
//            dos.writeInt(5000); //price
//            dos.writeByte(0); //bind
//            dos.writeByte(0); //打造次数
//
//            dos.writeByte(16); //附加属性数量
//
//            for(int i = 1; i <= 12; i++){
//                dos.writeByte(i); //属性类型
//                dos.writeShort(100); //附加值
//            }
//
//            dos.writeByte(20);
//            dos.writeShort(1000);
//            dos.writeByte(21);
//            dos.writeShort(2000);
//            dos.writeByte(22);
//            dos.writeShort(1000);
//            dos.writeByte(30);
//            dos.writeShort(1);
//        }catch(IOException ex){
//
//        }
//        return bos.toByteArray();
    }

    private byte[] getUsedEquipments(){
        return new byte[0];
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        DataOutputStream dos = new DataOutputStream(bos);
//        try{
//            dos.writeShort(1);
//            dos.writeInt(3); //item id
//            dos.writeInt(2); //id
//            dos.writeUTF("大斧子"); //name
//            dos.writeByte(1); //级别
//            dos.writeByte(1); //需要级别
//            dos.writeByte(1); //装备级别
//            dos.writeByte(7); //装备部位
//            dos.writeShort(100); //耐久度
//            dos.writeShort(100); //剩余耐久
//            dos.writeInt(5000); //price
//            dos.writeByte(0); //bind
//            dos.writeByte(0); //打造次数
//
//            dos.writeByte(16); //附加属性数量
//
//            for(int i = 1; i <= 12; i++){
//                dos.writeByte(i); //属性类型
//                dos.writeShort(100); //附加值
//            }
//
//            dos.writeByte(20);
//            dos.writeShort(1000);
//            dos.writeByte(21);
//            dos.writeShort(2000);
//            dos.writeByte(22);
//            dos.writeShort(1000);
//            dos.writeByte(30);
//            dos.writeShort(1);
//        }catch(IOException ex){
//
//        }
//        return bos.toByteArray();
    }

    private byte[] getTaskItems(){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        return bos.toByteArray();
    }

    public int getAccountIdByPlayerName(String name){
        try {
            return playerDao.getAccountIdByPlayerName(name);
        } catch (DataAccessException ex) {
            return -1;
        }
    }

//    public void removePlayer(PlayerData player){
//        playerDao.evict(Player.class,new Integer(player.getId()));
//    }

}
