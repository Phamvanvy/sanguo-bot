package com.pip.itimes.server.stage;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class Buf {

    public final static byte DOUBLE_PET_EXP = -2;
    public final static byte GUARD = -3;
    public final static byte EXP = -4;
    public final static byte EXP_MONEY = -5;
    public final static byte ADD_BATHHOUSE_EXP = -6;
    public final static byte ENHANCE = -7;
    public final static byte DIMONDHOLE = -25;
    public final static byte SkillPOINT = -26;
    public final static byte PETArmorGemstone = -27;
    public final static byte HOPEOBJECT = -28;
    public final static byte CAMP_EXP = -29;
    public final static byte CAMP_EVA = -30;			//阵营鉴定
    public final static byte CAMP_STONE = -31;		//阵营宝石
    public final static byte CAMP_REFINE = -26;		//阵营精炼
    
    public final static byte EXP_LOGIN = -8;

    public final static byte UNIT_TIMES = 0;
    public final static byte UNIT_SECOND = 1;
    public final static byte UNIT_CAMP = 2;
    
    public final static int CAMP_BATTLE_BUFF_MARK = 10;
    
    public final static int BUFF_STR = 8;
    public final static int BUFF_AGI = 9;
    public final static int BUFF_VIT = 10;
    public final static int BUFF_INT = 11;
    public final static int BUFF_HPMAX = 13;
    public final static int BUFF_MPMAX = 14;
    public final static int BUFF_PHYSIC_ATT = 16;
    public final static int BUFF_PHYSIC_DEF = 17;
    public final static int BUFF_MAGIC_ATT = 18;
    public final static int BUFF_MAGIC_DEF = 19;
    public final static int BUFF_HIT = 20;
    public final static int BUFF_FLEE = 21;
    public final static int BUFF_PHYSIC_CRI = 22;
    public final static int BUFF_MAGIC_CRI = 23;
    
    public final static int BUFF_UNLIMIT = 100000000;

    public final static int[] BUF_PRO ={8, 9, 10, 11, 13, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, -2, -4, -5, -7, -8, -6, -3, -25, -26, -27, -28,-29, -30, -31};
    public final static String[] BUF_STRING = {
                                              "力量#",
                                              "敏捷#",
                                              "体力#",
                                              "智力#",
                                              "生命#",
                                              "魔法#",
                                              "物理攻击力#",
                                              "物理防御力#",
                                              "魔法攻击力#",
                                              "魔法防御力#",
                                              "命中等级#",
                                              "闪躲等级#",
                                              "物理爆击等级#",
                                              "魔法爆击等级#",
                                              "护甲#",
                                              "宠物经验获取#%",
                                              "经验获取#%，可与其他经验加成效果叠加",
                                              "经验和金钱获取#%",
                                              "精炼成功率#%",
                                              "登陆经验获取#%，可与其他经验加成效果叠加",
                                              "浴场经验加成#%",
                                              "保护盾",
                                              "阵营科技提高当前打孔成功率的#%",
//                                              "阵营科技获得生活技能熟练点数翻倍的几率#%",
                                              "阵营科技精炼成功几率提高#%",
                                              "阵营科技提升铠化宠物成功率的#%",
                                              "阵营科技节省学习和遗忘战斗技能费用的#%",
                                              "阵营科技提升阵营角色获得经验#%",
                                              "阵营科技装备鉴定所提供的属性加成效果提高#%",
                                              "阵营科技宝石所提供的属性加成效果提高#%",
    };

    public final static Map<Integer,String> BUFS =  new HashMap<Integer,String>();
    static{
        for(int i=0;i<BUF_PRO.length;i++){
            BUFS.put(BUF_PRO[i],BUF_STRING[i]);
        }
    }

    public static String getBufString(Buf buf){
        StringBuilder sb = new StringBuilder();
        String template = BUFS.get((int)buf.getProperty());
         sb.append("延续");
        if (buf.getUnit() == 0 || buf.getUnit() == 10) {
           sb.append(buf.getTime());
           sb.append("场战斗");
           sb.append(":");
        }
        else if (buf.getUnit() == 1 || buf.getUnit() == 11) {
            long time = System.currentTimeMillis() - buf.getTimestamp();
            sb.append(time/1000);
            sb.append("秒");
            sb.append(":");
        }
        if(template==null){

        }else{
            sb.append("(");
            String value = "增加"+buf.getValue();
            sb.append(template.replace("#",value));
            sb.append(")");
        }
        return sb.toString();
    }

    public static String getBufStringToClient(Buf buf){
        StringBuilder sb = new StringBuilder();
        String template = BUFS.get((int)buf.getProperty());
        if(template==null){

        }else{
        	String value;
        	if(buf.getUnit()==2){
        		//阵营科技
        		value = String.valueOf(buf.getValue());
        	}else{
        		value = "增加"+buf.getValue();
        	}
            if(buf.getTime() == BUFF_UNLIMIT){
            	sb.append("永久享受打怪双倍经验效果");
            }else{
            	sb.append(template.replace("#",value));
            }
        }
        if(buf.getUnit() == 0 || buf.getUnit() == 10){
           sb.append("(");
           sb.append("延续:");
           sb.append(buf.getTime());
           sb.append("场战斗");
           sb.append(")");
        }else if (buf.getUnit() == 1 || buf.getUnit() == 11) {
        	if(buf.getTime() == BUFF_UNLIMIT){
        	}else{
        		sb.append("(");
        		sb.append("剩余:");
        		long now = new Date().getTime();
//        	Date new_date = new Date(buf.getTimestamp()+buf.getTime()*1000L);
//        	String str = DateFormat.getDateTimeInstance().format(new_date);
        		sb.append((((buf.getTimestamp()+buf.getTime()*1000L) - now)/1000L) + "秒");
        		sb.append(")");
        	}
        }else if(buf.getUnit()==2){
        	// 阵营科技的buf
        }
        return sb.toString();
    }

    private int id;
    private byte pro;
    private int value;
    private int time;
    private byte unit;
    private long timestamp;


    public static Buf getBufCheckTime(int id,byte pro,int value,int time,byte unit,long timestamp,long current){
        if (unit == 0 || unit == 10) {  //次数计算buf
            return new Buf(id,pro,value,time,unit);
        }
        else if(unit == 1 || unit == 11) { //时间计算的buf
            if ((timestamp + time * 1000L)< (current - 60000L)) {  //小于一分钟的都算超时了
                return null;
            }
            Buf buf = new Buf(id,pro,value,time,unit);
            buf.setTimestamp(timestamp);
            return buf;
        }
        return null;
    }

    public Buf(int id,byte pro,int value,int time,byte unit) {
        this.id = id;
        this.pro = pro;
        this.value = value;
        this.time = time;
        this.unit = unit;
    }

    public int getId(){
        return id;
    }
    
    public void setProperty (byte pro) {
    	this.pro = pro;
    }

    public byte getProperty(){
        return pro;
    }

    public int getValue(){
        return value;
    }
    
    public void setValue(int value){
    	this.value = value;
    }

    public int getTime(){
        return time;
    }

    public void descTime(){
        time--;
    }

    public byte getUnit(){
        return unit;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public byte[] toClientBytes(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(id);
            dos.writeByte(pro);
            dos.writeInt(value);
            return bos.toByteArray();
        } catch (IOException ex) {
        }
        return null;
    }

    public byte[] toRemovedBytes(){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(id);
            dos.writeByte(-1);
            dos.writeInt(0);
            return bos.toByteArray();
        } catch (IOException ex) {
        }
        return null;
    }

    public byte[] toDbBytes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            dos.writeInt(id);
            dos.writeByte(pro);
            dos.writeInt(value);
            dos.writeInt(time);
            dos.writeByte(unit);
            dos.writeLong(timestamp);
            return bos.toByteArray();
        } catch (IOException ex) {
        }
        return null;
    }

}
