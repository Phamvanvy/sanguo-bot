package com.pip.itimes.server.camp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class CampSkill{
    private int effect;
    private String name;
    private int upLimit;
    private String noLevelDesc;
    private String levelDesc;

    private TreeMap<Integer, CampSkillLevel> levels = new TreeMap<Integer, CampSkillLevel>();

    public int getEffect(){
        return effect;
    }

    public void setEffect(int effect){
        this.effect = effect;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getUpLimit(){
        return upLimit;
    }

    public void setUpLimit(int upLimit){
        this.upLimit = upLimit;
    }

    public String getNoLevelDesc(){
        return noLevelDesc;
    }

    public void setNoLevelDesc(String noLevelDesc){
        this.noLevelDesc = noLevelDesc;
    }

    public String getLevelDesc(int level){
        CampSkillLevel campSkillLevel = getLevel(level);
        
        if(campSkillLevel == null){
            return levelDesc;
        }else{
            String tmp = levelDesc.replaceAll("Parm1", String.valueOf(campSkillLevel.getParm1()));
            tmp = tmp.replaceAll("Parm2", String.valueOf(campSkillLevel.getParm2()));
            
            return tmp;
        }
    }

    public void setLevelDesc(String levelDesc){
        this.levelDesc = levelDesc;
    }

    public void initLevels(){
        levels.clear();
    }

    public void addLevel(CampSkillLevel level){
        levels.put(level.getLevel(), level);
    }

    public CampSkillLevel getLevel(int level){
        return levels.get(level);
    }
    
    public int getMaxLevel(){
        return levels.lastKey();
    }
    
    public static List<CampSkillData> fromDbBytes(byte[] data) throws Exception{
        List<CampSkillData> result = new ArrayList<CampSkillData>();
        
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);
        
        byte version = dis.readByte();
        
        switch(version){
            case 1:{
                short count = dis.readShort();
                
                for(int i = 0; i < count; i++){
                    int id = dis.readInt();
                    int[] newFormat = parseOldSkillData(id);
                    long upTime = dis.readLong();
                    long mtTime = dis.readLong();
                    
                    CampSkillData tmp = new CampSkillData();
                    tmp.setEffect(newFormat[0]);
                    tmp.setLevel(newFormat[1]);
                    tmp.setLastUpgradeTime(upTime);
                    tmp.setLastMaintTime(mtTime);
                    
                    result.add(tmp);
                }
            }
                break;
            case 2:{
                int count = dis.readByte();
                
                for(int i = 0; i < count; i++){
                    int effect = dis.readByte();
                    int level = dis.readByte();
                    long upTime = dis.readLong();
                    long mtTime = dis.readLong();
                    
                    CampSkillData tmp = new CampSkillData();
                    tmp.setEffect(effect);
                    tmp.setLevel(level);
                    tmp.setLastUpgradeTime(upTime);
                    tmp.setLastMaintTime(mtTime);
                    
                    result.add(tmp);
                }
            }
                break;
        }
        
        return result;
    }
    
    public static byte[] toDbBytes(List<CampSkillData> list) throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        
        dos.writeByte(0x2); //version
        dos.writeByte(list.size());
        
        for(CampSkillData tmp : list){
            dos.writeByte(tmp.getEffect());
            dos.writeByte(tmp.getLevel());
            dos.writeLong(tmp.getLastUpgradeTime());
            dos.writeLong(tmp.getLastMaintTime());
        }
        
        return bos.toByteArray();
    }
    
    private static int[] parseOldSkillData(int id){
        int[] result = new int[2];
        
        switch(id){
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                result[0] = -25;
                result[1] = id - 1;
                
                break;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                result[0] = -29;
                result[1] = id - 7;
                
                break;
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
                result[0] = -26;
                result[1] = id - 13;
                
                break;
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
                result[0] = -27;
                result[1] = id - 19;
                
                break;
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
                result[0] = -28;
                result[1] = id - 25;
                
                break;
        }
        
        return result;
    }
}
